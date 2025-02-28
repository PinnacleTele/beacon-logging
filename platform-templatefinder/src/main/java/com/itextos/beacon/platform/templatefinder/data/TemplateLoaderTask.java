package com.itextos.beacon.platform.templatefinder.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.commondbpool.DBDataSourceFactory;
import com.itextos.beacon.commonlib.commondbpool.DatabaseSchema;
import com.itextos.beacon.commonlib.commondbpool.JndiInfoHolder;
import com.itextos.beacon.commonlib.prometheusmetricsutil.PrometheusMetrics;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.platform.templatefinder.TemplateScrubber;

public class TemplateLoaderTask
        implements
        Runnable
{

    private static final Log                          log                          = LogFactory.getLog(TemplateLoaderTask.class);

    private String                                    entityId                     = null;

    private static final String                       SELECT_SQL                   = "SELECT " //
            + "    dtghem.template_group_id, " //
            + "    dtghem.header, " //
            + "    dti.template_id, " //
            + "    dti.template_content " //
            + " FROM " //
            + "    dlt_template_info dti, " //
            + "    dlt_template_group_header_entity_map dtghem " //
            + " where " //
            + "    dti.template_id = dtghem.template_id " //
            + "    and dtghem.entity_id = ? " //
            + " order by " //
            + "    length(REPLACE(template_content, '{#var#}', '')) desc";

    private static final String                       COL_INDEX_TEMPLATE_GROUUP_ID = "template_group_id";
    private static final String                       COL_INDEX_HEADER             = "header";
    private static final String                       COL_INDEX_TEMPLATE_ID        = "template_id";
    private static final String                       COL_INDEX_TEMPLATE_CONTENT   = "template_content";

    private boolean                                   isComplete                   = false;
    private boolean                                   hasError                     = false;
    private final Map<String, List<TemplateContents>> mTemplatesMap                = new ConcurrentHashMap<>();

    public TemplateLoaderTask(
            String aEntityId)
    {
        entityId = aEntityId;
    }

    @Override
    public void run()
    {
        ResultSet  rs        = null;
        final long startTime = System.currentTimeMillis();

        try (
                final Connection con = DBDataSourceFactory.getConnection(JndiInfoHolder.getJndiInfoUsingName(DatabaseSchema.ACCOUNTS.getKey()));
                final PreparedStatement pstmt = con.prepareStatement(SELECT_SQL, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);)
        {
            pstmt.setFetchSize(5000);
            pstmt.setString(1, entityId);
            rs = pstmt.executeQuery();

            while (rs.next())
            {
                final TemplateDbInfo tbi = new TemplateDbInfo(rs.getString(COL_INDEX_TEMPLATE_GROUUP_ID), rs.getString(COL_INDEX_HEADER), rs.getString(COL_INDEX_TEMPLATE_ID),
                        rs.getString(COL_INDEX_TEMPLATE_CONTENT));

                if (!tbi.validateInputs())
                {
                    final TemplateDbInfo tempTemplate = new TemplateDbInfo(rs.getString(COL_INDEX_TEMPLATE_GROUUP_ID), rs.getString(COL_INDEX_HEADER), rs.getString(COL_INDEX_TEMPLATE_ID),
                            rs.getString(COL_INDEX_TEMPLATE_CONTENT));

                    log.fatal("Invalid entries in the template related tables for the template " + tempTemplate);
                    continue;
                }

                final TemplateContents       tci       = new TemplateContents(tbi.getTemplateId(), tbi.getTemplateContent());
                final List<TemplateContents> lTempList = mTemplatesMap.computeIfAbsent(tbi.getKey(), k -> new ArrayList<>());
                lTempList.add(tci);
            }
        }
        catch (final Exception e)
        {
            hasError = true;
            log.error("Exception while getting the template details from database.", e);
            PrometheusMetrics.incrementGenericError(TemplateScrubber.getClusterType(), TemplateScrubber.getComponent(), CommonUtility.getApplicationServerIp(), "TMCHK-002",
                    "Inmemory Template Data Loading Issue Entity Level. Entity '" + entityId + "'");
        }
        finally
        {
            isComplete = true;

            CommonUtility.closeResultSet(rs);

            if (log.isDebugEnabled())
                log.debug("Time taken for to load tempaltes of Entity '" + entityId + "' is " + (System.currentTimeMillis() - startTime));
        }
    }

    public Map<String, List<TemplateContents>> getTemplatesMap()
    {
        return mTemplatesMap;
    }

    public boolean hasError()
    {
        return hasError;
    }

    public String getEntityId()
    {
        return entityId;
    }

    public boolean isCompleted()
    {
        return isComplete;
    }

}