package com.itextos.beacon.platform.dnrfallbackpoller.process;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.constants.ConfigParamConstants;
import com.itextos.beacon.commonlib.constants.TimerIntervalConstant;
import com.itextos.beacon.commonlib.message.DeliveryObject;
import com.itextos.beacon.commonlib.messageprocessor.process.MessageProcessor;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.timer.ITimedProcess;
import com.itextos.beacon.commonlib.utility.timer.TimedProcessor;
import com.itextos.beacon.commonlib.utility.tp.ExecutorSheduler;
import com.itextos.beacon.platform.dnrfallback.dao.DlrFallBackDao;

public abstract class AbstractDataPoller
        implements
        ITimedProcess
{

    private static final Log     log         = LogFactory.getLog(AbstractDataPoller.class);

    private final TimedProcessor mTimedProcessor;
    private boolean              canContinue = true;
    private final ClusterType    mClusterType;

    protected AbstractDataPoller(
            ClusterType aClusterType)
    {
        super();
        mClusterType    = aClusterType;
       
        mTimedProcessor = new TimedProcessor("DlrFallbackTableReader-" + mClusterType, this, TimerIntervalConstant.INTERFACE_FALLBACK_TABLE_READER);

        ExecutorSheduler.getInstance().addTask(mTimedProcessor, "DlrFallbackTableReader-" + mClusterType);
     }

    @Override
    public boolean canContinue()
    {
        return canContinue;
    }

    @Override
    public boolean processNow()
    {
        final boolean isKafkaAvailable = CommonUtility.isEnabled(DLRFBUtil.getConfigParamsValueAsString(ConfigParamConstants.IS_KAFKA_AVAILABLE));

        if (isKafkaAvailable)
            doProcess();
        else
            log.fatal("Kafka Service is disabled in app_config_values table..");

        return false;
    }

    private void doProcess()
    {

        try
        {
            final Map<String, DeliveryObject> lRecords       = DlrFallBackDao.getFallbackData(mClusterType.getKey());
            final List<String>                toDelete       = new ArrayList<>(lRecords.keySet());
            final List<DeliveryObject>        toProcess      = new ArrayList<>(lRecords.values());

            final List<String>                failedMessages = sendToNextQueue(toProcess);

            if (!failedMessages.isEmpty())
                toDelete.removeAll(failedMessages);

            DlrFallBackDao.deleteRecords(toDelete);
        }
        catch (final Exception e)
        {
            log.error("Exception while sending the message to " + Component.DNP, e);
        }
    }

    private static List<String> sendToNextQueue(
            List<DeliveryObject> aToProcess)
    {
        final List<String> errorInWritingToKafka = new ArrayList<>();
        for (final DeliveryObject lDeliveryObject : aToProcess)
            try
            {
                MessageProcessor.writeMessage(Component.FBP, Component.DNP, lDeliveryObject);
            }
            catch (final Exception e)
            {
                errorInWritingToKafka.add(lDeliveryObject.getMessageId());
                log.error("Exception while writing to the kafka. Loosing the message.", e);
            }
        return errorInWritingToKafka;
    }

    @Override
    public void stopMe()
    {
        canContinue = false;
    }

}