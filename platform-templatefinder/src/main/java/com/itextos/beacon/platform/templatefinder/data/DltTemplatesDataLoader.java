package com.itextos.beacon.platform.templatefinder.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.commondbpool.DBDataSourceFactory;
import com.itextos.beacon.commonlib.commondbpool.DatabaseSchema;
import com.itextos.beacon.commonlib.commondbpool.JndiInfoHolder;
import com.itextos.beacon.commonlib.constants.TimerIntervalConstant;
import com.itextos.beacon.commonlib.prometheusmetricsutil.PrometheusMetrics;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.timer.ITimedProcess;
import com.itextos.beacon.commonlib.utility.timer.TimedProcessor;
import com.itextos.beacon.commonlib.utility.tp.ExecutorSheduler;
import com.itextos.beacon.platform.templatefinder.TemplateScrubber;
import com.itextos.beacon.platform.templatefinder.utility.DltTemplateProperties;

public class DltTemplatesDataLoader
        implements
        ITimedProcess
{

    private static final Log    log          = LogFactory.getLog(DltTemplatesDataLoader.class);

    private static final String DISTINCT_SQL = "select distinct(entity_id) FROM dlt_template_group_header_entity_map";

    private static class SingletonHolder
    {

        static final DltTemplatesDataLoader INSTANCE = new DltTemplatesDataLoader();

    }

    public static DltTemplatesDataLoader getInstance()
    {
        return SingletonHolder.INSTANCE;
    }

    private boolean               mCanContinue        = true;
    private boolean               isFirstLoadComplete = false;
    private final TimedProcessor  mTimedProcessor;
    private final ExecutorService pool                = Executors.newFixedThreadPool(DltTemplateProperties.getInstance().getDataloaderThreadPoolSize());

    private DltTemplatesDataLoader()
    {
    	
        mTimedProcessor = new TimedProcessor("DltTemplatesDataLoader", this, TimerIntervalConstant.DATA_REFRESHER_RELOAD_INTERVAL);
        ExecutorSheduler.getInstance().addTask(mTimedProcessor, "DltTemplatesDataLoader");
    }

    @Override
    public boolean canContinue()
    {
        return mCanContinue;
    }

    @Override
    public boolean processNow()
    {
        loadDistinctEntity();
        return false;
    }

    public void loadDistinctEntity()
    {
        ResultSet  rs        = null;
        final long startTime = System.currentTimeMillis();

        try (
                final Connection con = DBDataSourceFactory.getConnection(JndiInfoHolder.getJndiInfoUsingName(DatabaseSchema.ACCOUNTS.getKey()));
                final PreparedStatement pstmt = con.prepareStatement(DISTINCT_SQL, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);)
        {
            pstmt.setFetchSize(5000);
            rs = pstmt.executeQuery();

            final List<TemplateLoaderTask> mLoaderTasks = new ArrayList<>();

            while (rs.next())
            {
                final String             entityId   = rs.getString(1);

                final TemplateLoaderTask loaderTask = new TemplateLoaderTask(entityId);
                mLoaderTasks.add(loaderTask);
                pool.execute(loaderTask);
            }

            boolean notCompleted = true;

            while (notCompleted)
            {

                for (final TemplateLoaderTask loaderTask : mLoaderTasks)
                {
                    notCompleted = !loaderTask.isCompleted();

                    if (notCompleted)
                        break;
                }
                if (notCompleted)
                    CommonUtility.sleepForAWhile();
            }

            printErrorFromLoaderTask(mLoaderTasks);

            final Map<String, List<TemplateContents>> tempAllTemplates = mergeAllTemplates(mLoaderTasks);

            sortBasedOnStaticWords(tempAllTemplates);

            isFirstLoadComplete = true;
        }
        catch (final Exception e)
        {
            log.error("Exception while getting the template details from database.", e);
            PrometheusMetrics.incrementGenericError(TemplateScrubber.getClusterType(), TemplateScrubber.getComponent(), CommonUtility.getApplicationServerIp(), "TMCHK-001",
                    "Inmemory Template Data Loading Issue Global Level.");
        }
        finally
        {
            CommonUtility.closeResultSet(rs);
        }

        try
        {
            if (log.isDebugEnabled())
                log.debug("Completed loading the DLT Templates. Time taken : '" + String.format("%10s", (System.currentTimeMillis() - startTime)) + "' Mills loaded size '"
                        + String.format("%10.4f", TemplateDataHolder.getInstance().calculateSize()) + "' MB");
        }
        catch (final Exception e)
        {
            // ignore
        }
    }

    private static void sortBasedOnStaticWords(
            Map<String, List<TemplateContents>> aTempAllTemplates)
    {

        if (aTempAllTemplates.size() > 0)
        {
            final Collection<List<TemplateContents>> lValues = aTempAllTemplates.values();

            for (final List<TemplateContents> list : lValues)
                Collections.sort(list, new ListComparator());

            TemplateDataHolder.getInstance().addTemplateInfo(aTempAllTemplates);
        }
    }

    private static void printErrorFromLoaderTask(
            List<TemplateLoaderTask> aMLoaderTasks)
    {
        for (final TemplateLoaderTask loaderTask : aMLoaderTasks)
            if (loaderTask.hasError())
                log.fatal("Exception while getting the template details from database for entityID: '" + loaderTask.getEntityId() + "'");
    }

    private static Map<String, List<TemplateContents>> mergeAllTemplates(
            List<TemplateLoaderTask> aLoaderTasks)

    {
        final Map<String, List<TemplateContents>> tempAllTemplates = new ConcurrentHashMap<>();

        for (final TemplateLoaderTask tlt : aLoaderTasks)
        {
            final Map<String, List<TemplateContents>> templatesMap = tlt.getTemplatesMap();

            for (final Entry<String, List<TemplateContents>> templatesEntry : templatesMap.entrySet())
            {
                final String                 groupHeaderKey   = templatesEntry.getKey();
                final List<TemplateContents> templates        = templatesEntry.getValue();
                final List<TemplateContents> lComputeIfAbsent = tempAllTemplates.computeIfAbsent(groupHeaderKey, k -> new ArrayList<>());

                if (!lComputeIfAbsent.isEmpty())
                    lComputeIfAbsent.removeAll(templates);

                lComputeIfAbsent.addAll(templates);
            }
        }

        return tempAllTemplates;
    }

    @Override
    public void stopMe()
    {
        mCanContinue = false;
    }

    public boolean isFirstLoadCompleted()
    {
        return isFirstLoadComplete;
    }

}

class TemplateDbInfo
{

    private final String mTemplateGroupId;
    private final String mHeader;
    private final String mTemplateId;
    private final String mTemplateContent;
    private final String mKey;

    TemplateDbInfo(
            String aTemplateGroupId,
            String aHeader,
            String aTemplateId,
            String aTemplateContent)
    {
        super();
        mTemplateGroupId = CommonUtility.nullCheck(aTemplateGroupId, true);
        mHeader          = CommonUtility.nullCheck(aHeader, true).toLowerCase();
        mTemplateId      = CommonUtility.nullCheck(aTemplateId, true);
        mTemplateContent = CommonUtility.nullCheck(aTemplateContent, true).toLowerCase();
        mKey             = CommonUtility.combine(mTemplateGroupId, mHeader);
    }

    boolean validateInputs()
    {
        if (mTemplateGroupId.isBlank())
            return false;

        if (mHeader.isBlank())
            return false;

        if (mTemplateId.isBlank())
            return false;

        return !(mTemplateContent.isBlank());
    }

    String getKey()
    {
        return mKey;
    }

    String getTemplateId()
    {
        return mTemplateId;
    }

    String getTemplateContent()
    {
        return mTemplateContent;
    }

    @Override
    public String toString()
    {
        return "TemplateDbInfo [mTemplateGroupId=" + mTemplateGroupId + ", mHeader=" + mHeader + ", mTemplateId=" + mTemplateId + ", mTemplateContent=" + mTemplateContent + "]";
    }

}