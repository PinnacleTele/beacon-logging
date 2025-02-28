package com.itextos.beacon.platform.agingdn.processor;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;

import com.itextos.beacon.commonlib.constants.TimerIntervalConstant;
import com.itextos.beacon.commonlib.message.DeliveryObject;
import com.itextos.beacon.commonlib.utility.timer.ITimedProcess;
import com.itextos.beacon.commonlib.utility.timer.TimedProcessor;
import com.itextos.beacon.commonlib.utility.tp.ExecutorSheduler;

public class AbstractAgingDlrGen
        implements
        ITimedProcess
{

    private static final Log     log          = LogFactory.getLog(AbstractAgingDlrGen.class);

    private final TimedProcessor mTimedProcessor;
    private boolean              canContinue  = true;
    private final int            divisorVal   = 0;
    private final int            remainderVal = 0;
    public static String         FAST_DN      = "fastdn";

    protected AbstractAgingDlrGen()
    {
        super();
       
        mTimedProcessor = new TimedProcessor("TimerThread-AgingDlrGenrator", this, TimerIntervalConstant.SCHEDULE_MESSAGE_TABLE_READER);
     
        ExecutorSheduler.getInstance().addTask(mTimedProcessor, "TimerThread-AgingDlrGenrator");
    }

    @Override
    public boolean canContinue()
    {
        return canContinue;
    }

    @Override
    public boolean processNow()
    {
        // TODO Auto-generated method stub
        return false;
    }

    private void doProcess()
    {
        final List<JSONObject> dnInfoLst = getData();

        if (!dnInfoLst.isEmpty())
            for (final JSONObject agingInfo : dnInfoLst)
            {
                // doProcessMessage();
            }
    }

    private List<JSONObject> getData()
    {
        if (log.isDebugEnabled())
            log.debug(" getData() - Calling ...");
        final List<JSONObject> dnInfoLst = AgingElasticSearchUtil.getAgingDNInfo(divisorVal, remainderVal);
        return dnInfoLst;
    }

    /*
     * private Map<String, BaseMessage> doProcessMessage(
     * BaseMessage aNunMessage)
     * throws Exception
     * {
     * Map<String, BaseMessage> lsendToNextTopic = new HashMap<>();
     * try
     * {
     * if (log.isDebugEnabled())
     * log.debug("Map from loop - ClientId : " +
     * aNunMessage.getValue(MiddlewareConstant.MW_CLIENT_ID) + ":: DEST :" +
     * aNunMessage.getValue(MiddlewareConstant.MW_MOBILE_NUMBER));
     * BaseMessage lAgingDn = new BaseMessage(ClusterType.OTP);
     * lAgingDn = parseFullMsg((String) aNunMessage.get(MapKey.FULL_MAP_MSG),
     * lAgingDn);
     * lAgingDn.putValue(MiddlewareConstant.MW_AGING_TYPE,
     * aNunMessage.getValue(MiddlewareConstant.MW_AGING_TYPE));
     * lAgingDn.putValue(MiddlewareConstant.MW_DN_ORI_STATUS_CODE,
     * aNunMessage.get(MapKey.STATUS_ID));
     * final String agingType =
     * aNunMessage.getValue(MiddlewareConstant.MW_AGING_TYPE);
     * if (log.isDebugEnabled())
     * log.debug(" Aging Type - " + agingType);
     * lAgingDn.putValue(MiddlewareConstant.MW_CARRIER_ORI_STATUS_CODE,
     * aNunMessage.getValue(MiddlewareConstant.MW_DN_ORI_STATUS_CODE));
     * if (FAST_DN.equalsIgnoreCase(agingType))
     * {
     * final String dummyRouteId = PlatformUtil.getAppConfigValueAsString(null); //
     * ConfigParams.INSTANCE.getKeyValue(Constants.FASTDN_DUMMY_ROUTEID);
     * if (log.isDebugEnabled())
     * log.debug(" Fast DN Dummy RouteID :" + dummyRouteId);
     * lAgingDn.putValue(MiddlewareConstant.MW_ROUTE_ID, dummyRouteId);
     * if (log.isDebugEnabled())
     * log.debug(" BaseMessage Object Before sending to DN_FROM_INTERNAL - " +
     * lAgingDn.getJsonString());
     * lAgingDn.putValue(MiddlewareConstant.MW_DLR_FROM_INTERNAL,
     * Component.AGING_DN.getKey());
     * lsendToNextTopic.put(MiddlewareConstant.MW_DLR_FROM_INTERNAL.getKey(),
     * lAgingDn);
     * }
     * else
     * {
     * if (log.isDebugEnabled())
     * log.debug(" BaseMessage Object Before sending to DN_Retry logic - " +
     * lAgingDn.getJsonString());
     * try
     * {
     * lsendToNextTopic = DNRetryUtil.processDNRetry(lAgingDn);
     * if (log.isDebugEnabled())
     * log.debug(" Next Retry Queue info -" + lsendToNextTopic);
     * }
     * catch (final Exception e)
     * {
     * try
     * {
     * AgingUtil.sendToErrorLog(lAgingDn, e);
     * }
     * catch (final Exception error)
     * {
     * log.error(" problem sending to error-q queue - " + lAgingDn, error);
     * }
     * }
     * }
     * if (aNunMessage != null)
     * doDeleteRecordsFromElasticSearch(aNunMessage);
     * }
     * catch (final Exception e)
     * {
     * log.
     * error(" Exception occer while processing the Aging DN message to Queues..",
     * e);
     * AgingUtil.sendToErrorLog(aNunMessage, e);
     * }
     * return lsendToNextTopic;
     * }
     */

    private static void doDeleteRecordsFromElasticSearch(
            DeliveryObject aDeliveryObject)
    {
        boolean isElasticSearchTransSuccess = false;

        while (!isElasticSearchTransSuccess)
        {
            isElasticSearchTransSuccess = AgingElasticSearchUtil.deleteRecordFromElasticSearch(aDeliveryObject);
            if (log.isDebugEnabled())
                log.debug("  Records deleted status .." + isElasticSearchTransSuccess);
        }
    }

    /*
     * private JSONObject parseFullMsg(
     * String fullMag,
     * JSONObject jsonAgingDn)
     * throws Exception
     * {
     * final JSONParser parse = new JSONParser();
     * jsonAgingDn = (JSONObject) parse.parse(fullMag);
     * return jsonAgingDn;
     * }
     */
    @Override
    public void stopMe()
    {
        canContinue = false;
    }

}
