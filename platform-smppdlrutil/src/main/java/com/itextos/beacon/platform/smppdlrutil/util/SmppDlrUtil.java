package com.itextos.beacon.platform.smppdlrutil.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.MiddlewareConstant;
import com.itextos.beacon.commonlib.constants.exception.ItextosException;
import com.itextos.beacon.commonlib.message.DeliveryObject;
import com.itextos.beacon.platform.msgflowutil.billing.BillingDatabaseTableIndentifier;
import com.itextos.beacon.platform.smppdlrutil.dao.SmppDlrFallBackDao;
import com.itextos.beacon.smslog.PromoConsumerLog;
import com.itextos.beacon.smslog.TransConsumerLog;

public class SmppDlrUtil
{

    private SmppDlrUtil()
    {}

    private static final Log log = LogFactory.getLog(SmppDlrUtil.class);

    public static Map<String, List<DeliveryObject>> groupByClientId(
            List<DeliveryObject> aDeliveryObjectList)
    {
        final Map<String, List<DeliveryObject>> result = new HashMap<>();

        for (final DeliveryObject aDeliveryObject : aDeliveryObjectList)
        {
            final String lClientId = aDeliveryObject.getClientId();
            if (aDeliveryObject.getValue(MiddlewareConstant.MW_RETRY_INIT_TIME) == null)
                aDeliveryObject.putValue(MiddlewareConstant.MW_RETRY_INIT_TIME, "" + System.currentTimeMillis());

            ClientWiseCounter.getInstance();

            if (ClientWiseCounter.canProcessMessage(aDeliveryObject))
            {
                List<DeliveryObject> lClientWiseDNList = result.get(lClientId);
                if (lClientWiseDNList == null)
                    lClientWiseDNList = new ArrayList<>();
                lClientWiseDNList.add(aDeliveryObject);
                result.put(lClientId, lClientWiseDNList);
            }
            else
            {
                log.error("DN Expiry ..RX/TRX Session not available.., Hence sending to PostLog..");

                aDeliveryObject.putValue(MiddlewareConstant.MW_SMPP_PROTOCAL, "SMPP");
                aDeliveryObject.putValue(MiddlewareConstant.MW_SMPP_STATUS, "PF_EXPIRED");
                aDeliveryObject.putValue(MiddlewareConstant.MW_SMPP_SYSTEM_ID, aDeliveryObject.getUser());

                // aDeliveryObject.putValue(MiddlewareConstant.MW_CLIENT_SOURCE_IP, "");

                identifySuffix(aDeliveryObject);

                SmppDlrProducer.sendToPostLog(aDeliveryObject);
            }
        }
        if (log.isInfoEnabled())
            log.info("Client wise map>>>" + result.size());
        return result;
    }

    public static void smppDeliveryProcess(
            List<DeliveryObject> lDeliveryObjectList)
    {
        final Map<String, List<DeliveryObject>> result = SmppDlrUtil.groupByClientId(lDeliveryObjectList);

        for (final Entry<String, List<DeliveryObject>> anEntry : result.entrySet())
        {
            final String                    lClientId            = anEntry.getKey();
            final List<DeliveryObject>      lDeliveryObjectsList = anEntry.getValue();
            final long                      longClientId         = Long.parseLong(lClientId);
            final List<Map<String, String>> alist                = SmppDlrRedis.getLiveSessionInfo().get(lClientId);

            if ((alist != null) && (alist.size() != 0))
            {
                if (log.isDebugEnabled())
                    log.debug("Client ID :" + lClientId);

                if (log.isInfoEnabled())
                    log.info("lClientId : "+lClientId+" : attempting to push to customer redis queue...");

                if (!SmppDlrRedisOperation.rpush(longClientId, lDeliveryObjectsList))
                {
                    log.warn("lClientId : "+lClientId+" Storing into DB for redis rpush fail...");

                    try
                    {
                        SmppDlrFallBackDao.insertRecords(lDeliveryObjectsList);
                    }
                    catch (final ItextosException e)
                    {
                        e.printStackTrace();
                    }
                }else {
                	
                	lDeliveryObjectsList.forEach((obj)->{
                		
                		
                	 	String msgid="notfind";
                    	
                    	String msgtype="notfind";
                    
                    	msgtype=((DeliveryObject)obj).getMessageType().getKey();

                		msgid =((DeliveryObject)obj).getMessageId()+" msgtype : "+msgtype+ " getClusterType : "+((DeliveryObject)obj).getClusterType().toString()+ " getSmsPriority : "+((DeliveryObject)obj).getSmsPriority()+ " getMessagePriority : "+((DeliveryObject)obj).getMessagePriority();
                	
                	
                        try
                        {
                            
                        	if(msgtype!=null&&msgtype.equals("0")) {
                    			
                    			PromoConsumerLog.log(msgid+ " Inmemory  Consumed successfully sent to redis");

                    		}else {
                    			
                    			TransConsumerLog.log(msgid+ " Inmemory  Consumed successfully sent to redis");
                    		}
                        }
                        catch (final Exception e)
                        {
                        }
                	});
                }
            }
            else
            {
                if (log.isDebugEnabled())
                    log.debug("Storing into DB for Client :'" + lClientId + "' RX/TRX session is unavailable..");

                try
                {
                    SmppDlrFallBackDao.insertRecords(lDeliveryObjectsList);
                }
                catch (final ItextosException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    private static void identifySuffix(
            DeliveryObject aDeliveryObject)
    {

        try
        {
            final BillingDatabaseTableIndentifier lBillingDatabaseTableIndentifier = new BillingDatabaseTableIndentifier(aDeliveryObject);
            lBillingDatabaseTableIndentifier.identifySuffix();
        }
        catch (final Exception e)
        {
            log.error("Exception occer while identifying table suffix...", e);
        }
    }

}
