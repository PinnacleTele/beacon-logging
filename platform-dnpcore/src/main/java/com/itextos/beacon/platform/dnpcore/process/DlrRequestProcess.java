package com.itextos.beacon.platform.dnpcore.process;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.constants.ConfigParamConstants;
import com.itextos.beacon.commonlib.constants.ErrorMessage;
import com.itextos.beacon.commonlib.constants.MessageType;
import com.itextos.beacon.commonlib.constants.MiddlewareConstant;
import com.itextos.beacon.commonlib.message.DeliveryObject;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.inmemory.customfeatures.pojo.DlrTypeInfo;
import com.itextos.beacon.platform.billing.DeliveryProcess;
import com.itextos.beacon.platform.dnpcore.dlrclienthandover.process.DlrClientHandover;
import com.itextos.beacon.platform.dnpcore.log.DNPLog;
import com.itextos.beacon.platform.dnpcore.util.DNPUtil;
import com.itextos.beacon.platform.dnpcore.util.DlrConstants;

public class DlrRequestProcess
{

    private static final Log log = LogFactory.getLog(DlrRequestProcess.class);

    private DlrRequestProcess()
    {}

    public static void processDNQueueReq(
            DeliveryObject aDeliveryObject,
            Map<Component, DeliveryObject> nextComponentMap)
            throws Exception
    {
    	aDeliveryObject.getLogBufferValue(MiddlewareConstant.MW_LOG_BUFFER).append("\n").append(" LOG START");

        if (log.isDebugEnabled())
            log.debug(" Begin Message Id:" + aDeliveryObject.getMessageId());
        final DeliveryObject lNewDeliveryObj = aDeliveryObject.getClonedDeliveryObject();
        final String         lMid            = lNewDeliveryObj.getMessageId();

      
        if (log.isDebugEnabled())
            log.debug(aDeliveryObject.getMessageId() + " : Before DlrDataUpdater the Message Object : " + lNewDeliveryObj.getJsonString());

        // The following logic will be in biller earlier and moved here

        processDeliveryAdjustments(lNewDeliveryObj);

        if (log.isDebugEnabled())
            log.debug(aDeliveryObject.getMessageId() + " : After DlrDataUpdater the Message Object : " + lNewDeliveryObj.getJsonString());

     
        
        final boolean lNoPayloadForPromoMsg = CommonUtility.isEnabled(DNPUtil.getAppConfigValueAsString(ConfigParamConstants.NOPAYLOAD_FOR_PROMO_MSG));

        //        if (lNoPayloadForPromoMsg && (lNewDeliveryObj.getMessageType() != MessageType.PROMOTIONAL)) {

        /*
        if (lNoPayloadForPromoMsg) {
        		
        		if (log.isDebugEnabled())
                   log.debug(aDeliveryObject.getMessageId() + " : nextComponentMap set T2DB_DELIVERIES to  : "+Component.T2DB_DELIVERIES );
                		   ;

        	nextComponentMap.put(Component.T2DB_DELIVERIES, lNewDeliveryObj);
        }else {
        	
        	if (log.isDebugEnabled())
                log.debug(aDeliveryObject.getMessageId() + " : nextComponentMap not to set T2DB_DELIVERIES : lNoPayloadForPromoMsg "+lNoPayloadForPromoMsg+" : MessageType :  "+lNewDeliveryObj.getMessageType() );
             
        }

*/
        
    	nextComponentMap.put(Component.T2DB_DELIVERIES, lNewDeliveryObj);

        final DlrTypeInfo lDlrTypeInfo = DNPUtil.getDnTypeInfo(lNewDeliveryObj.getClientId());

        if (log.isDebugEnabled())
            log.debug(aDeliveryObject.getMessageId() +" : DlrType Confiug : " + lDlrTypeInfo);
        
        DNPLog.getInstance(aDeliveryObject.getClientId()).log(aDeliveryObject.getClientId(),aDeliveryObject.getFileId()+ " : "+ aDeliveryObject.getMessageId() + " : DlrType Confiug : " + lDlrTypeInfo );


        int lTotalMsgParts = 0;

        try
        {
            lTotalMsgParts = lNewDeliveryObj.getMessageTotalParts();
        }
        catch (final Exception e)
        {}

        // Interface Rejections & it is Sync request error case will not handover to
        // client.

        boolean isProcessClientHandover = !aDeliveryObject.isSyncRequest();

        if ((aDeliveryObject.getDlrFromInternal() != null) && aDeliveryObject.getDlrFromInternal().equals("dummyroute_dlr_came_from_MW"))
            isProcessClientHandover = true;


        DNPLog.getInstance(aDeliveryObject.getClientId()).log(aDeliveryObject.getClientId(),aDeliveryObject.getFileId()+ " : "+ aDeliveryObject.getMessageId() + " :isProcessClientHandover :: " + isProcessClientHandover );
        

    	aDeliveryObject.getLogBufferValue(MiddlewareConstant.MW_LOG_BUFFER).append("\n").append(aDeliveryObject.getMessageId()+" :isProcessClientHandover :: " + isProcessClientHandover);

        if (isProcessClientHandover || aDeliveryObject.isPlatfromRejected())
        {
          
            final boolean isRejectedRequest = aDeliveryObject.isPlatfromRejected() || aDeliveryObject.isInterfaceRejected();

            	aDeliveryObject.getLogBufferValue(MiddlewareConstant.MW_LOG_BUFFER).append("\n").append(aDeliveryObject.getMessageId() + " : Is Rejected Dlr ? '" + isRejectedRequest + "', If TRUE then bypass the Single DN..");


            if ((lDlrTypeInfo != null) && DlrConstants.SINGLE_DN_ENABLE.equals(lDlrTypeInfo.getDnType()) && (lTotalMsgParts > 1) && !isRejectedRequest)
                nextComponentMap.put(Component.SDNP, lNewDeliveryObj);
            else
            {
                if (log.isDebugEnabled())
                    log.debug(aDeliveryObject.getMessageId() + " : Sending request to Client Handover Process..");
                
                DNPLog.getInstance(aDeliveryObject.getClientId()).log(aDeliveryObject.getClientId(),aDeliveryObject.getFileId()+ " : "+ aDeliveryObject.getMessageId() + " : Sending request to Client Handover Process.." );

                DlrClientHandover.processClientHandover(lNewDeliveryObj, nextComponentMap);
            }
        }
        if (log.isDebugEnabled())
            log.debug(aDeliveryObject.getMessageId() + " End mid:" + lMid + " nextQueueMap ket set:" + (nextComponentMap != null ? nextComponentMap.keySet() : nextComponentMap));
    }

    private static void processDeliveryAdjustments(
            DeliveryObject aDeliveryObject)
    {
        // TimeAdjustmentUtility.maskFailToSuccessCode(aNewNunMessage);
        // TimeAdjustmentUtility.adjustAndSetDTime(aNewNunMessage);

        final DeliveryProcess lDeliveryProcess = new DeliveryProcess(aDeliveryObject);
        lDeliveryProcess.process(aDeliveryObject.getLogBufferValue(MiddlewareConstant.MW_LOG_BUFFER));
    }

}
