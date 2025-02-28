package com.itextos.beacon.platform.dnpcore.poller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.constants.ConfigParamConstants;
import com.itextos.beacon.commonlib.constants.MiddlewareConstant;
import com.itextos.beacon.commonlib.constants.TimerIntervalConstant;
import com.itextos.beacon.commonlib.message.DeliveryObject;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.timer.ITimedProcess;
import com.itextos.beacon.commonlib.utility.timer.TimedProcessor;
import com.itextos.beacon.commonlib.utility.tp.ExecutorSheduler;
import com.itextos.beacon.errorlog.SMSLog;
import com.itextos.beacon.platform.dnpayloadutil.PayloadProcessor;
import com.itextos.beacon.platform.dnpcore.dao.NoPayloadRetryDao;
import com.itextos.beacon.platform.dnpcore.inmem.NoPayloadRetryUpdateQ;
import com.itextos.beacon.platform.dnpcore.process.DlrProcessUtil;
import com.itextos.beacon.platform.dnpcore.util.DNPProducer;
import com.itextos.beacon.platform.dnpcore.util.DNPUtil;

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
      
        mTimedProcessor = new TimedProcessor("NoPayloadRetryTableReader-" + mClusterType, this, TimerIntervalConstant.NO_PAYLOAD_RETRY_TABLE_READER);
        
        ExecutorSheduler.getInstance().addTask(mTimedProcessor, "NoPayloadRetryTableReader-" + mClusterType);
    }

    @Override
    public boolean canContinue()
    {
        return canContinue;
    }

    @Override
    public boolean processNow()
    {
        doProcess();
        return false;
    }

    private void doProcess()
    {

        try
        {
            final Map<String, DeliveryObject> lRecords       = NoPayloadRetryDao.getNoPayloadData(mClusterType.getKey());
            final List<String>                toDelete       = new ArrayList<>(lRecords.keySet());
            final List<DeliveryObject>        toProcess      = new ArrayList<>(lRecords.values());

            final List<String>                failedMessages = sendToNextQueue(toProcess,SMSLog.getInstance());

            if (!failedMessages.isEmpty())
                toDelete.removeAll(failedMessages);

            NoPayloadRetryDao.deleteRecords(toDelete);
        }
        catch (final Exception e)
        {
            log.error("Exception while sending the message to " + Component.DNP, e);
        }
    }

    private static List<String> sendToNextQueue(
            List<DeliveryObject> aToProcess,SMSLog sb)
    {
        final List<String> lSNoList          = new ArrayList<>();

        final int          lMaxPayloadExpVal = DNPUtil.getAppConfigValueAsInt(ConfigParamConstants.MAX_NO_PAYLOAD_RETRY_ATTEMPT_COUNT);

        if (log.isDebugEnabled())
            log.debug("Max NoPayload Retry attempt count :" + lMaxPayloadExpVal);

        for (final DeliveryObject lDeliveryObject : aToProcess)
        {
            if (log.isDebugEnabled())
                log.debug("Delivery Object :" + lDeliveryObject);

            try
            {
                final DeliveryObject lDelvObj = PayloadProcessor.retrivePayload(lDeliveryObject);

                if (log.isDebugEnabled())
                    log.debug("Payload Message - " + lDelvObj.getJsonString());

                final String lPayloadStatus = lDelvObj.getDnPayloadStatus();

                if ("1".equals(lPayloadStatus))
                {
                    final Map<Component, DeliveryObject> lProcessDnReceiverQ = DlrProcessUtil.processDnReceiverQ(lDelvObj);

                    if ((lProcessDnReceiverQ != null) && !lProcessDnReceiverQ.isEmpty())
                    {
                        if (log.isDebugEnabled())
                            log.debug("Sending to " + lProcessDnReceiverQ.keySet() + " BaseMessage:" + lDelvObj.getJsonString());

                        DNPProducer.sendToNextComponents(lProcessDnReceiverQ,SMSLog.getInstance());
                    }
                }
                else
                {
                    final Map<Component, DeliveryObject> processDNQueues = new HashMap<>();

                    // -1, 0
                    if ("-1".equals(lPayloadStatus))
                    {
                        DNPUtil.setPlatformErrorCodeBasedOnCarrierErrorCode(lDeliveryObject);

                        processDNQueues.put(Component.T2DB_NO_PAYLOAD_DN, lDeliveryObject);
                    }
                    else
                    {
                        if (log.isDebugEnabled())
                            log.debug("Sending to NoPayloadRetry Queue......." + lDeliveryObject.getMessageId());

                        if ("0".equals(lPayloadStatus))
                        {
                            final int lNoPayloadRetryAttemptCnt = CommonUtility.getInteger(lDeliveryObject.getValue(MiddlewareConstant.MW_NO_PAYLOD_RETRY_EXPIRY_COUNT));

                            if (lNoPayloadRetryAttemptCnt < lMaxPayloadExpVal)
                            {
                                lSNoList.add(lDeliveryObject.getValue(MiddlewareConstant.MW_SNO));
                                lDeliveryObject.putValue(MiddlewareConstant.MW_NO_PAYLOD_RETRY_EXPIRY_COUNT, String.valueOf((lNoPayloadRetryAttemptCnt + 1)));
                                NoPayloadRetryUpdateQ.getInstance().addMessage(lDeliveryObject);
                            }
                            else
                            {
                                DNPUtil.setPlatformErrorCodeBasedOnCarrierErrorCode(lDeliveryObject);
                                processDNQueues.put(Component.T2DB_NO_PAYLOAD_DN, lDeliveryObject);
                            }
                        }
                    }

                    DNPProducer.sendToNextComponents(processDNQueues,sb);
                }
            }
            catch (final Exception e)
            {
                log.error("Exception occer while processing NoPayload Processor....", e);
                DNPProducer.sendToErrorLog(lDeliveryObject, e);
            }
        }

        return lSNoList;
    }

    @Override
    public void stopMe()
    {
        canContinue = false;
    }

}