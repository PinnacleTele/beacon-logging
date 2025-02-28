package com.itextos.beacon.platform.ch.processor;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.*; 
import java.security.MessageDigest; 

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.componentconsumer.processor.AbstractKafkaComponentProcessor;
import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.constants.ConfigParamConstants;
import com.itextos.beacon.commonlib.constants.Constants;
import com.itextos.beacon.commonlib.constants.DateTimeFormat;
import com.itextos.beacon.commonlib.constants.ErrorMessage;
import com.itextos.beacon.commonlib.constants.PlatformStatusCode;
import com.itextos.beacon.commonlib.httpclient.BasicHttpConnector;
import com.itextos.beacon.commonlib.httpclient.HttpResult;
import com.itextos.beacon.commonlib.kafkaservice.consumer.ConsumerInMemCollection;
import com.itextos.beacon.commonlib.message.BaseMessage;
import com.itextos.beacon.commonlib.message.IMessage;
import com.itextos.beacon.commonlib.message.MessageRequest;
import com.itextos.beacon.commonlib.message.SubmissionObject;
import com.itextos.beacon.commonlib.message.utility.MessageUtil;
import com.itextos.beacon.commonlib.prometheusmetricsutil.PrometheusMetrics;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.DateTimeUtility;
import com.itextos.beacon.commonlib.utility.Name;
import com.itextos.beacon.inmemory.carrierhandover.RouteKannelInfo;
import com.itextos.beacon.inmemory.carrierhandover.util.ICHUtil;
import com.itextos.beacon.inmemory.routeinfo.cache.RouteConfigInfo;
import com.itextos.beacon.inmemory.routeinfo.util.RouteUtil;
import com.itextos.beacon.platform.carrierhandoverutility.util.CHUtil;
import com.itextos.beacon.platform.carrierhandoverutility.util.GenerateDNUrl;
import com.itextos.beacon.platform.ch.util.CHProcessUtil;
import com.itextos.beacon.platform.ch.util.CHProducer;
import com.itextos.beacon.platform.dnpayloadutil.PayloadProcessor;
import com.itextos.beacon.platform.kannelstatusupdater.process.response.KannelStatsCollector;
import com.itextos.beacon.smslog.KannelURLLog;

import io.prometheus.client.Histogram.Timer;

public class CarrierHandoverProcess
        extends
        AbstractKafkaComponentProcessor
{

    private static final Log log = LogFactory.getLog(CarrierHandoverProcess.class);

    static final int TELEMARKETERID_TLV_VALUE_NO=0;

    static final int TELEMARKETERID_TLV_VALUE_HASHED=1;
    
    static final int TELEMARKETERID_TLV_VALUE_NONHASHED=2;
    
    static final int TELEMARKETERID_TLV_VALUE_TELEMARKETERID=3;

    public CarrierHandoverProcess(
            String aThreadName,
            Component aComponent,
            ClusterType aPlatformCluster,
            String aTopicName,
            ConsumerInMemCollection aConsumerInMemCollection,
            int aSleepInMillis)
    {
        super(aThreadName, aComponent, aPlatformCluster, aTopicName, aConsumerInMemCollection, aSleepInMillis);
    }

    @Override
    public void doProcess(
            BaseMessage aBaseMessage)
    {
        final MessageRequest lMessageRequest = (MessageRequest) aBaseMessage;

     
    	lMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(lMessageRequest.getBaseMessageId()+" : Message Received ");

        CarrierHandoverProcess.forCH(lMessageRequest,mPlatformCluster);
    }

    public static void forCH(MessageRequest lMessageRequest,ClusterType mPlatformCluster) {
    	
    	   try
           {
               final Timer   lPlatformRejection = PrometheusMetrics.componentMethodStartTimer(Component.CH, mPlatformCluster, "platformRejection");
               final boolean isHexMsg           = lMessageRequest.isHexMessage();

               if (isHexMsg)
               {
                   final boolean isValidHexMessage = CHUtil.isValidHexMessage(lMessageRequest.getLongMessage());

                   if (!isValidHexMessage)
                   {
                   	lMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(lMessageRequest.getBaseMessageId()+" :: Invalid HEX Message : ");

                       sendToPlatfromRejection(lMessageRequest, PlatformStatusCode.INVALID_HEX_MESSAGE);
                       PrometheusMetrics.componentMethodEndTimer(Component.CH, lPlatformRejection);
                       return;
                   }
               }

               final String lFeatureCode = CommonUtility.nullCheck(lMessageRequest.getFeatureCode(), true);
               final String lRouteId     = CommonUtility.nullCheck(lMessageRequest.getRouteId(), true);

               if (lRouteId.isEmpty())
               {
               	lMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(lMessageRequest.getBaseMessageId()+" :: Unable to find out the Route Id : ");

                   sendToPlatfromRejection(lMessageRequest, PlatformStatusCode.EMPTY_ROUTE_ID);
                   PrometheusMetrics.componentMethodEndTimer(Component.CH, lPlatformRejection);
                   return;
               }

               if (lFeatureCode.isBlank())
               {
            	   
                  	lMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(lMessageRequest.getBaseMessageId()+" :: Unable to find out the Feature Code : ");

                   sendToPlatfromRejection(lMessageRequest, PlatformStatusCode.EMPTY_FEATURE_CODE);
                   PrometheusMetrics.componentMethodEndTimer(Component.CH, lPlatformRejection);
                   return;
               }
               PrometheusMetrics.componentMethodEndTimer(Component.CH, lPlatformRejection);

               final Timer   lBlockoutTimer = PrometheusMetrics.componentMethodStartTimer(Component.CH, mPlatformCluster, "isMessageBlockout");
               final boolean isBlockout     = CHProcessUtil.isMessageBlockout(lMessageRequest);
               PrometheusMetrics.componentMethodEndTimer(Component.CH, lBlockoutTimer);

               if (isBlockout)
                   return;

               final Timer   lExpiredtimer = PrometheusMetrics.componentMethodStartTimer(Component.CH, mPlatformCluster, "isExpired");
               final boolean isExpired     = CHUtil.isExpired(lMessageRequest);
               PrometheusMetrics.componentMethodEndTimer(Component.CH, lExpiredtimer);

               if (isExpired)
               {
                  
                 	lMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(lMessageRequest.getBaseMessageId()+" :: Message Expired :" + isExpired);

                   sendToPlatfromRejection(lMessageRequest, PlatformStatusCode.EXPIRED_MESSAGE);
                   return;
               }

               final Timer           lDeliveryRouteInfo = PrometheusMetrics.componentMethodStartTimer(Component.CH, mPlatformCluster, "getDeliveryRouteInfo");
               final RouteKannelInfo lKannelRouteInfo   = ICHUtil.getDeliveryRouteInfo(lRouteId, lFeatureCode);
               PrometheusMetrics.componentMethodEndTimer(Component.CH, lDeliveryRouteInfo);

               if (lKannelRouteInfo == null)
               {
                	lMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(lMessageRequest.getBaseMessageId()+" :: Unable to find  Route Kannel Template for  route : " + lRouteId + " feature cd : " + lFeatureCode );

                   sendToPlatfromRejection(lMessageRequest, PlatformStatusCode.KANNEL_TEMPLATE_NOT_FOUND);
                   return;
               }

               lMessageRequest.setRouteType(lKannelRouteInfo.getRouteType());

               final List<BaseMessage> lBaseMessageList  = lMessageRequest.getSubmissions();

               boolean                 isFirstPartFailed = false;
               boolean                 isPartialSuccess  = false;

               final boolean           canDoMsgRetry     = CHUtil.canMsgRetry(lMessageRequest);

               for (final BaseMessage baseMssage : lBaseMessageList)
               {
                   final SubmissionObject lSubmissionObject = (SubmissionObject) baseMssage;
                  
                   try
                   {

                       if (isFirstPartFailed && !isPartialSuccess)
                       {
                           // log.fatal("First part carrier handover is failed, Hence ignoring the remining
                           // part messages...Message Id :" + lSubmissionObject.getMessageId());
                       	lMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(lMessageRequest.getBaseMessageId()+" :; First part carrier handover is failed, Hence ignoring the remining part messages...Message Id :" + lSubmissionObject.getMessageId() );

                           return;
                       }

                       if (isPartialSuccess && isFirstPartFailed)
                       {
                           	lMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(lMessageRequest.getBaseMessageId()+" :: Unable to process the Multipart request to kannel for the route '" + lRouteId + ", partially failed' , Hence rejecting the request.." );

                           sendToPlatfromRejection(lSubmissionObject, PlatformStatusCode.PARTIALLY_CARRIER_HANDOVER_FAILED);
                           continue;
                       }

                       final String lMessageId = lSubmissionObject.getMessageId();

                       String       lUdh       = CommonUtility.nullCheck(lSubmissionObject.getUdh(), true);

                       if (!lUdh.isEmpty())
                       {
                          

                          	lMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(lMessageRequest.getBaseMessageId()+" :: Udh Value : " + lUdh );

                           if (CHUtil.isValidUDH(lUdh))
                               lUdh = CHUtil.addKannelSpecCharToHex(lUdh);
                           else
                           {
                             	lMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(lMessageRequest.getBaseMessageId()+" :: Udh Value : " + lUdh +" ::: Invalid UDH : ");

                               sendToPlatfromRejection(lSubmissionObject, PlatformStatusCode.INVALID_UDH);
                               continue;
                           }
                       }

                       // msg_replace_chk value from account table.
                       final boolean isDLTEnable = CommonUtility.isEnabled(CHUtil.getAppConfigValueAsString(ConfigParamConstants.DLT_ENABLE));

                       if (!isDLTEnable)
                           messageReplaceCheck(lSubmissionObject, lMessageRequest, lRouteId);

                       int           lRetryAttempt          = lMessageRequest.getRetryAttempt();
                  
                       if (lKannelRouteInfo.isDummyRoute() && (!lKannelRouteInfo.getCarrierFullDn().isEmpty()))
                       {
                       	sendDummyRoute(lSubmissionObject,lKannelRouteInfo,lMessageRequest,mPlatformCluster);
                       	continue;
                       }

                       final boolean isKannelAvailable = CHProcessUtil.isKannelAvailable(lRouteId);

        
                     	lMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(lMessageRequest.getBaseMessageId()+" :: Kannel Available Status : " + isKannelAvailable );

                       final String lActualRouteId = lMessageRequest.getActualRouteId();

                       if ((lSubmissionObject.getMtMessageRetryIdentifier() == null) || !lRouteId.equals(lActualRouteId))
                           setCallBackUrl(lMessageRequest, lSubmissionObject);

                       final String lKannelUrl =getKannelUrlByHardcoded( makelowercase(getKannelUrl(lKannelRouteInfo, lSubmissionObject, lMessageRequest, lUdh, lRetryAttempt, isDLTEnable)),lMessageRequest);

                    
                       KannelURLLog.log(" Featurecd : "+lKannelRouteInfo.getFeatureCode()+" :  lKannelUrl : "+ lKannelUrl);
                       
                    	lMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(lMessageRequest.getBaseMessageId()+" :: kannel URL--->" + lKannelUrl );

                       if (!isKannelAvailable && canDoMsgRetry)
                       {
                           // Set the isFirstPartFailed flag to 'true' for Multipart Request.
                           if (lMessageRequest.getMessageTotalParts() > 1)
                               isFirstPartFailed = true;

                           doMessageRetry(lMessageRequest, lSubmissionObject);
                           continue;
                       }

                       final Timer      lKannelConnect = PrometheusMetrics.componentMethodStartTimer(Component.CH, mPlatformCluster, "KannelConnect");

                       final HttpResult lHttpResult    = BasicHttpConnector.connect(lKannelUrl);
                       final boolean    lResponse      = lHttpResult.isSuccess();

                       PrometheusMetrics.componentMethodEndTimer(Component.CH, lKannelConnect);

                      
                   	lMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(lMessageRequest.getBaseMessageId()+" :: kannel URL--->" + lKannelUrl + "', Response : '" + lResponse + "'");

                       setKannelResponseTime(lMessageRequest,lKannelUrl, lRouteId, lResponse);

                       if (lResponse)
                       {
                           isPartialSuccess = true;
                           lRetryAttempt    = lSubmissionObject.getRetryAttempt();
                           final String lRoute_Id = lMessageRequest.getRouteId();
                           	lMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(lMessageRequest.getBaseMessageId()+" :: Route ID : " + lRoute_Id);

                           if (lRetryAttempt != 0)
                           {
                               // retry msg send to INTERIM_FAILURE topic
                               CHProducer.sendToInterim(lSubmissionObject);
                               
                              	lMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(lMessageRequest.getBaseMessageId()+" :: send to interm queue success :"  );

                            
                           }

                           lSubmissionObject.setSubOriginalStatusCode(PlatformStatusCode.SUCCESS.getStatusCode());

                   
                         	lMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(lMessageRequest.getBaseMessageId()+" :: Retry Attempt Count : " + lRetryAttempt  );

                           if (lRetryAttempt == 0)
                           {
                               CHProducer.sendToSubBilling(lSubmissionObject,lMessageRequest.getLogBuffer());
                               
                            	lMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(lMessageRequest.getBaseMessageId()+" :: Sent to submission biller topic.. success"  );

                           
                           }
                       }
                       else
                       {
                          

                       	lMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(lMessageRequest.getBaseMessageId()+"  url : " + lKannelUrl + " : \n response : " + lResponse );

                           isFirstPartFailed = true;

                           if (isPartialSuccess && isFirstPartFailed)
                           {
                             
                              
                              	lMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(lMessageRequest.getBaseMessageId()+" :: Unable to process the Multipart request to kannel for the route '" + lRouteId + ", partially failed' , Hence rejecting the request.." );

                               
                               sendToPlatfromRejectionWithRemovePayload(lSubmissionObject, PlatformStatusCode.PARTIALLY_CARRIER_HANDOVER_FAILED);
                           }
                           else
                           {
                           
                               if (isFirstPartFailed)
                               {
                                   	lMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(lMessageRequest.getBaseMessageId()+" :: First part failed ..." );

                                   if (canDoMsgRetry)
                                   {
                                  
                                      	lMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(lMessageRequest.getBaseMessageId()+" :: Message Retry Enabled & First part failed ...:'" + isFirstPartFailed + "', Hence sending to Message Retry.." );

                                       doMessageRetry(lMessageRequest, lSubmissionObject);
                                   }
                                   else
                                   {
                                  
                                     	lMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(lMessageRequest.getBaseMessageId()+" :: Message Retry Disabled, Unable to send the Multipart request to kannel for the route '" + lRouteId + "' , Hence rejecting the request.." );

                                       sendToPlatfromRejectionWithRemovePayload(lSubmissionObject, lMessageRequest, PlatformStatusCode.CARRIER_HANDOVER_FAILED);
                                   }
                               }
                           }
                       }
                    	lMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(lMessageRequest.getBaseMessageId()+" udh : " + lUdh );

                   }
                   catch (final Exception e2)
                   {
                      
                   	lMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(lMessageRequest.getBaseMessageId()+" :: Exception occer while processing Carrier Handover ..."+ErrorMessage.getStackTraceAsString(e2) );

                       
                       isFirstPartFailed = true;

                       if (isPartialSuccess && isFirstPartFailed)
                       {
                           lSubmissionObject.setSubOriginalStatusCode(PlatformStatusCode.PARTIALLY_CARRIER_HANDOVER_FAILED.getStatusCode());
                           lSubmissionObject.setAdditionalErrorInfo("CH :" + e2.getMessage());
                           CHProducer.sendToNextLevel(lSubmissionObject, lMessageRequest, true);
                       }
                       else
                       {
                        
                           if (isFirstPartFailed)
                           {
                           	lMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(lMessageRequest.getBaseMessageId()+" :: Due to exception sending to PRC..., Base Mid :'" );

                               lMessageRequest.setSubOriginalStatusCode(PlatformStatusCode.CARRIER_HANDOVER_FAILED.getStatusCode());
                               lMessageRequest.setAdditionalErrorInfo("CH :" + e2.getMessage());

                               CHProducer.sendToNextLevel(lSubmissionObject, lMessageRequest, false);
                           }
                       }
                   }
               }
           }
           catch (final Exception e)
           {

              	lMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(lMessageRequest.getBaseMessageId()+" :: Exception occer while processing Carrier Handover ..."+ErrorMessage.getStackTraceAsString(e) );
               try
               {
                   CHProducer.sendToErrorLog(lMessageRequest, e);
               }
               catch (final Exception e1)
               {
                   e1.printStackTrace();
               }
           }
    	   
     //	   log.debug(" smslog : "+lMessageRequest.getLogBuffer().toString());


    }
 
       private static String makelowercase(String kannelUrl) {
			
 		   // 	="http://192.168.1.80:34003/cgi-bin/sendsms?user=Net4&password=Netin&smsc=VNSTBA-1&smsc-id=VNSTBA-1&cliid=4000003600000002&sdate=2024-09-16&to=916206851089&from=APOHOS&text=Dear+User%2C+%0A%0AUse+OTP+656491+to+verify+and+submit+your+details+securely+with+Apollo+Hospitals.&priority=1&validity=1440&coding=0&dlr-url=http%3A%2F%2Fdnreceiver%3A8480%2Fdnr%2Fdlrreceiver%3Fdr%3D%25a%26smscid%3D%25i%26statuscd%3D%25d%26add_info%3D%257B%2522car_ts_format%2522%253A%2522yyMMddHHmmss%2522%252C%2522msg_create_ts%2522%253A%25221726475693183%2522%252C%2522pl_rds_id%2522%253A%25222%2522%252C%2522intl_msg%2522%253A%25220%2522%252C%2522intf_type%2522%253A%2522http_japi%2522%252C%2522pl_exp%2522%253A%252224091621%2522%252C%2522rty_atmpt%2522%253A%25220%2522%252C%2522sms_priority%2522%253A%25225%2522%252C%2522recv_ts%2522%253A%25222024-09-16%2B14%253A06%253A13.693%2522%252C%2522platform_cluster%2522%253A%2522otp%2522%252C%2522intf_grp_type%2522%253A%2522api%2522%252C%2522msg_type%2522%253A%25221%2522%252C%2522rute_id%2522%253A%2522VNSTBA%2522%252C%2522c_id%2522%253A%25224000003600000002%2522%252C%2522m_id%2522%253A%2522292093740613693302107700%2522%257D%26systemid%3D%25o&dlr-mask=3&accpriority=5&rp=0&meta-data=%3Fsmpp%3Fentityid=1001315801901941875%26templateid=1107172596401737054%26telemarketerid=1702166693997804050";
 		    	String response=null;
 		    	try {
 					URL url=new URL(kannelUrl);
 					StringBuffer sb=new StringBuffer();
 					sb.append(url.getProtocol()).append("://").append(url.getHost()).append(":").append(url.getPort()).append(url.getPath().toLowerCase()).append("?");

 					Map<String,String> reqmap=new HashMap<String,String>();
 					
 					StringTokenizer st=new StringTokenizer(url.getQuery(),"&");
 		    	
 					String metadata="";
 					while(st.hasMoreTokens()) {
 						
 						String param=st.nextToken();
 						
 						if(!param.startsWith("meta-data")) {
 							
 							StringTokenizer st1=new StringTokenizer(param,"=");
 	 						
 	 						
 	 						reqmap.put(st1.nextToken().toLowerCase(), st1.nextToken());

 						}else {
 							
 							metadata=param;
 						}
 						
 						
 					}
 					
 					reqmap.forEach((k,v)->{
 						
 						sb.append(k).append("=").append(v).append("&");
 					});
 					
 					sb.append(metadata);
 		    	
 					return sb.toString();

 		    	} catch (MalformedURLException e) {
 					
 					
 					return kannelUrl;

 				}
 		    	
 			}
 		   

	

	private static void sendDummyRoute(final SubmissionObject lSubmissionObject,final RouteKannelInfo lKannelRouteInfo,final MessageRequest lMessageRequest,ClusterType mPlatformCluster) {
		

        lSubmissionObject.setCarrierFullDn(lKannelRouteInfo.getCarrierFullDn());

        final Timer setCarrierSMTime = PrometheusMetrics.componentMethodStartTimer(Component.CH, mPlatformCluster, "SetCarrierSMTime");

        // Set the Retry-attempt
        GenerateDNUrl.setDlrUrl(lSubmissionObject, lMessageRequest.getRetryAttempt());

        PrometheusMetrics.componentMethodEndTimer(Component.CH, setCarrierSMTime);

        final Timer dummyHandover = PrometheusMetrics.componentMethodStartTimer(Component.CH, mPlatformCluster, "DummyHandover");

 
      	lMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(lMessageRequest.getBaseMessageId()+" Sending to dummy route q:" + lSubmissionObject.getMessageId() + " retry attempt:" + lMessageRequest.getRetryAttempt() );

   
           CHProducer.sendToDummyRoute(lSubmissionObject,lMessageRequest.getLogBuffer());
       

        if (lMessageRequest.getRetryAttempt() == 0)
        {
            lSubmissionObject.setSubOriginalStatusCode(PlatformStatusCode.SUCCESS.getStatusCode());
            CHProducer.sendToSubBilling(lSubmissionObject,lMessageRequest.getLogBuffer());
        }

        PrometheusMetrics.componentMethodEndTimer(Component.CH, dummyHandover);


		
	}

	private static void setKannelResponseTime(
            MessageRequest lMessageRequest, String aKannelUrl,
            String aRouteId,
            boolean aResponse)
    {

        try
        {
            final long    lKannelHitStartTime = System.currentTimeMillis();
            final long    lKannelHitEndTime   = System.currentTimeMillis();

            final boolean isResponseCheck     = CommonUtility.isEnabled(CHProcessUtil.getAppConfigValueAsString(ConfigParamConstants.KANNEL_CONN_RESP_CHK));

        

          	lMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(lMessageRequest.getBaseMessageId()+"  :; Check Respone Time '" + isResponseCheck + "'" );

            if (isResponseCheck)
                KannelStatsCollector.getInstance().verifyKannelResponseTime(aKannelUrl, aRouteId, lKannelHitStartTime, lKannelHitEndTime, aResponse);
        }
        catch (final Exception e)
        {
        	
          	lMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(lMessageRequest.getBaseMessageId()+" :: Some error in calculating the times. :: "+ErrorMessage.getStackTraceAsString(e) );

        }
    }

    private static void setCallBackUrl(
            MessageRequest aMessageRequest,
            SubmissionObject aSubmissionObject)
            throws UnsupportedEncodingException,
            InterruptedException
    {
        final String lClusterDNReceiverInfo = ICHUtil.getClusterDNReceiverInfo(aMessageRequest.getClusterType().getKey());
        String       lDlrUrl                = null;

        final Timer  removePayloadTimer     = PrometheusMetrics.componentMethodStartTimer(Component.CH, aMessageRequest.getClusterType(), "removeAndStorePayload");
        removeAndStorePayload(aSubmissionObject);
        PrometheusMetrics.componentMethodEndTimer(Component.CH, removePayloadTimer);

        final String additionalInfoString = CHUtil.getCallBackParams(aSubmissionObject);


        aMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(aMessageRequest.getFileId()+" :: additionalInfoString===>" + additionalInfoString );

        final String encodedAdditionalInfo = URLEncoder.encode(additionalInfoString, Constants.ENCODER_FORMAT);
        lDlrUrl = CHUtil.generateCallBackUrl(lClusterDNReceiverInfo, encodedAdditionalInfo);
       
        aMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(aMessageRequest.getFileId()+" :: Kannel dn URL--->" + lDlrUrl );

        aSubmissionObject.setCallBackUrl(lDlrUrl);
    }

    private static void removeAndStorePayload(
            SubmissionObject aSubmissionObject)
            throws InterruptedException
    {
        String lPayloadRid = null;

        aSubmissionObject.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(aSubmissionObject.getBaseMessageId()+" :; Attempting to remove payload...." );

        PayloadProcessor.removePayload(aSubmissionObject);
  
        aSubmissionObject.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(aSubmissionObject.getBaseMessageId()+" :; Attempting to remove payload.... finished" );


        while (lPayloadRid == null)
        {
            lPayloadRid = PayloadProcessor.storePayload(aSubmissionObject);

            if (lPayloadRid == null)
            {
               
                aSubmissionObject.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(aSubmissionObject.getBaseMessageId()+" :: payload rid null retrying after 100 millis..." );

                Thread.sleep(100);
            }
        }

        aSubmissionObject.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(aSubmissionObject.getBaseMessageId()+" :: payload storing to redis succesful for payloadrid=" + lPayloadRid );

        aSubmissionObject.setPayloadRedisId(lPayloadRid);
    }

    private static void messageReplaceCheck(
            SubmissionObject aSubmissionObject,
            MessageRequest aMessageRequest,
            String aRouteId)
    {
        final boolean lMsgReplaceChk = CommonUtility.isEnabled(CommonUtility.nullCheck(aMessageRequest.getMsgAlertCheck(), true));

        if (lMsgReplaceChk)
        {
            final String lCarrier = aMessageRequest.getCarrier();
            final String lCircle  = aMessageRequest.getCircle();

            if (ICHUtil.canReplaceKeywordInMessage(aMessageRequest.getClientId(), lCarrier, lCircle, aRouteId))
            {
                final String lReplaceMsg = ICHUtil.getReplacedMessage(aMessageRequest.getClientId(), aSubmissionObject.getMessage());

              

                aSubmissionObject.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(aSubmissionObject.getBaseMessageId()+" :: Altered Message :" + lReplaceMsg );

                aSubmissionObject.setAlterMessage(lReplaceMsg);
            }
        }
    }

    private static String getKannelUrl(
            RouteKannelInfo aKannelRouteInfo,
            SubmissionObject aSubmissionObject,
            MessageRequest aMessageRequest,
            String aUdh,
            int aRetryAttempt,
            boolean isDLTEnable)
            throws UnsupportedEncodingException
    {
        final String   lMessage   = CHUtil.getMessage(aSubmissionObject);
        final String   lClientId  = aMessageRequest.getClientId();

        String[]       lUrlparams = new String[]
        { URLEncoder.encode(aKannelRouteInfo.getKannelIp(), Constants.ENCODER_FORMAT), // 0
                URLEncoder.encode(aKannelRouteInfo.getKannelPort(), Constants.ENCODER_FORMAT), // 1
                URLEncoder.encode(aKannelRouteInfo.getSmscId(), Constants.ENCODER_FORMAT), // 2
                URLEncoder.encode(aSubmissionObject.getMobileNumber(), Constants.ENCODER_FORMAT), // 3
                URLEncoder.encode(ICHUtil.getHeader(aKannelRouteInfo, MessageUtil.getHeaderId(aMessageRequest)), Constants.ENCODER_FORMAT), // 4
                lMessage, // 5
                aUdh, // 6
                URLEncoder.encode(aSubmissionObject.getCallBackUrl(), Constants.ENCODER_FORMAT), // 7
                // chn
                // kannel
                // dlr_url
                // or
                // hexcode
                // value

                URLEncoder.encode(CHUtil.getPriority(aMessageRequest), Constants.ENCODER_FORMAT), // 8
                URLEncoder.encode((String.valueOf(aMessageRequest.getMaxValidityInSec() / 60)), Constants.ENCODER_FORMAT), // 9
                URLEncoder.encode(lClientId, Constants.ENCODER_FORMAT), // 10
                URLEncoder.encode(DateTimeUtility.getFormattedDateTime(aSubmissionObject.getMessageReceivedDate(), DateTimeFormat.DEFAULT_DATE_ONLY), Constants.ENCODER_FORMAT), // 11
                URLEncoder.encode(aMessageRequest.getMessagePriority().getKey(), Constants.ENCODER_FORMAT), // 12
                URLEncoder.encode(aMessageRequest.getMessagePriority().getKey(), Constants.ENCODER_FORMAT), // 13
                URLEncoder.encode(String.valueOf(aRetryAttempt), Constants.ENCODER_FORMAT) // 14

        };

        final String[] lDltParams = new String[2];

        if (isDLTEnable)
        {
            lDltParams[0] = URLEncoder.encode(CommonUtility.nullCheck(aMessageRequest.getDltEntityId(), true), Constants.ENCODER_FORMAT);
            lDltParams[1] = URLEncoder.encode(CommonUtility.nullCheck(aMessageRequest.getDltTemplateId(), true), Constants.ENCODER_FORMAT);

            final String[] lMerged = new String[lUrlparams.length + lDltParams.length];
            System.arraycopy(lUrlparams, 0, lMerged, 0, lUrlparams.length);
            System.arraycopy(lDltParams, 0, lMerged, lUrlparams.length, lDltParams.length);
            lUrlparams = lMerged;
        }

        return MessageFormat.format(aKannelRouteInfo.getUrlTemplate(), lUrlparams);
    }

    /*
     * 
     * 
     * MariaDB [carrier_handover]> select * from kannel_url_config where route_id='VNSPBA'\G;
*************************** 1. row ***************************
  route_id: VNSPBA
feature_cd: PMS
kannel_url: http://{0}:{1}/cgi-bin/sendsms?user=Net4&password=Netin&smsc={2}&smsc-id={2}&cliid={10}&sdate={11}&to={3}&from={4}&text={5}&priority={8}&validity={9}&coding=0&dlr-url={7}&dlr-mask=3&accpriority={13}&rp={14}&meta-data=%3Fsmpp%3Fentityid={15}%26templateid={16}%26telemarketerid=1702166693997804050
*************************** 2. row ***************************
  route_id: VNSPBA
feature_cd: PMM
kannel_url: http://{0}:{1}/cgi-bin/sendsms?user=Net4&password=Netin&smsc={2}&smsc-id={2}&cliid={10}&sdate={11}&to={3}&from={4}&text={5}&priority={8}&validity={9}&coding=0&udh={6}&dlr-url={7}&dlr-mask=3&accpriority={13}&rp={14}&meta-data=%3Fsmpp%3Fentityid={15}%26templateid={16}%26telemarketerid=1702166693997804050
*************************** 3. row ***************************
  route_id: VNSPBA
feature_cd: US
kannel_url: http://{0}:{1}/cgi-bin/sendsms?user=Net4&password=Netin&smsc={2}&smsc-id={2}&cliid={10}&sdate={11}&to={3}&from={4}&text={5}&priority={8}&validity={9}&coding=2&dlr-url={7}&dlr-mask=3&alt-dcs=1&accpriority={13}&rp={14}&meta-data=%3Fsmpp%3Fentityid={15}%26templateid={16}%26telemarketerid=1702166693997804050
*************************** 4. row ***************************
  route_id: VNSPBA
feature_cd: UM
kannel_url: http://{0}:{1}/cgi-bin/sendsms?user=Net4&password=Netin&smsc={2}&smsc-id={2}&cliid={10}&sdate={11}&to={3}&from={4}&text={5}&priority={8}&validity={9}&coding=2&udh={6}&dlr-url={7}&dlr-mask=3&accpriority={13}&rp={14}&meta-data=%3Fsmpp%3Fentityid={15}%26templateid={16}%26telemarketerid=1702166693997804050
*************************** 5. row ***************************
  route_id: VNSPBA
feature_cd: FLS
kannel_url: http://{0}:{1}/cgi-bin/sendsms?user=Net4&password=Netin&smsc={2}&smsc-id={2}&cliid={10}&sdate={11}&to={3}&from={4}&text={5}&priority={8}&validity={9}&coding=0&dlr-url={7}&dlr-mask=3&accpriority={13}&rp={14}&meta-data=%3Fsmpp%3Fentityid={15}%26templateid={16}%26telemarketerid=1702166693997804050&msgclass=0
*************************** 6. row ***************************
  route_id: VNSPBA
feature_cd: FLM
kannel_url: http://{0}:{1}/cgi-BIN/sendsms?user=Net4&password=Netin&smsc={2}&smsc-id={2}&cliid={10}&sdate={11}&to={3}&from={4}&TEXT={5}&priority={8}&validity={9}&coding=0&udh={6}&dlr-url={7}&dlr-mask=3&accpriority={13}&rp={14}&meta-data=%3Fsmpp%3Fentityid={15}%26templateid={16}%26telemarketerid=1702166693997804050&msgclass=0
*************************** 7. row ***************************
  route_id: VNSPBA
feature_cd: FLUM
kannel_url: http://{0}:{1}/cgi-bin/sendsms?user=Net4&password=Netin&smsc={2}&smsc-id={2}&cliid={10}&sdate={11}&to={3}&from={4}&text={5}&priority={8}&validity={9}&coding=2&udh={6}&dlr-url={7}&dlr-mask=3&accpriority={13}&rp={14}&meta-data=%3Fsmpp%3Fentityid={15}%26templateid={16}%26telemarketerid=1602100000000006745&mclass=0
*************************** 8. row ***************************
  route_id: VNSPBA
feature_cd: FLUS
kannel_url: http://{0}:{1}/cgi-bin/sendsms?user=Net4&password=Netin&smsc={2}&smsc-id={2}&cliid={10}&sdate={11}&to={3}&from={4}&text={5}&priority={8}&validity={9}&coding=2&dlr-url={7}&dlr-mask=3&alt-dcs=1&accpriority={13}&rp={14}&meta-data=%3Fsmpp%3Fentityid={15}%26templateid={16}%26telemarketerid=1602100000000006745&mclass=0
*************************** 9. row ***************************
  route_id: VNSPBA
feature_cd: FLUM
kannel_url: http://{0}:{1}/cgi-bin/sendsms?user=Net4&password=Netin&smsc={2}&smsc-id={2}&cliid={10}&sdate={11}&to={3}&from={4}&text={5}&priority={8}&validity={9}&coding=2&udh={6}&dlr-url={7}&dlr-mask=3&accpriority={13}&rp={14}&meta-data=%3Fsmpp%3Fentityid={15}%26templateid={16}%26telemarketerid=1602100000000006745&mclass=0
*************************** 10. row ***************************
  route_id: VNSPBA
feature_cd: FLUS
kannel_url: http://{0}:{1}/cgi-bin/sendsms?user=Net4&password=Netin&smsc={2}&smsc-id={2}&cliid={10}&sdate={11}&to={3}&from={4}&text={5}&priority={8}&validity={9}&coding=2&dlr-url={7}&dlr-mask=3&alt-dcs=1&accpriority={13}&rp={14}&meta-data=%3Fsmpp%3Fentityid={15}%26templateid={16}%26telemarketerid=1602100000000006745&mclass=0

     */
    
    
    private static String getKannelUrlByHardcoded(
    		String kannelUrl,
            MessageRequest aMessageRequest
           )
            throws UnsupportedEncodingException
    {
    	
    	try {
			URL url=new URL(kannelUrl);
			StringBuffer sb=new StringBuffer();
			sb.append(url.getProtocol()).append("://").append(url.getHost()).append(":").append(url.getPort()).append(url.getPath().toLowerCase()).append("?");

	        Map<String,String> reqmap= getDataMap(kannelUrl,aMessageRequest);

			
			reqmap.forEach((k,v)->{
				
				sb.append(k).append("=").append(v).append("&");
			});
			

			return sb.toString();

    	} catch (MalformedURLException e) {
			
			

		}
    	
			return null;

   
    }
    
    private static Map<String, String> getDataMap(String kannelUrl,MessageRequest aMessageRequest) throws UnsupportedEncodingException {

		Map<String,String> reqmap=new HashMap<String,String>();

			    	try {
					URL url=new URL(kannelUrl);
			
					
					StringTokenizer st=new StringTokenizer(url.getQuery(),"&");
		    	
					while(st.hasMoreTokens()) {
						
						String param=st.nextToken();
						
						if(!param.startsWith("meta-data")) {
							
							StringTokenizer st1=new StringTokenizer(param,"=");
	 						
	 						
	 						reqmap.put(st1.nextToken().toLowerCase(), st1.nextToken());

						}
						
						
					}
					
					String entityid=URLEncoder.encode(CommonUtility.nullCheck(aMessageRequest.getDltEntityId(), true), Constants.ENCODER_FORMAT);
					
					String templateid=URLEncoder.encode(CommonUtility.nullCheck(aMessageRequest.getDltTemplateId(), true), Constants.ENCODER_FORMAT);
					
					
			        final RouteConfigInfo lRouteConfigInfo = RouteUtil.getRouteConfiguration(aMessageRequest.getRouteId());

			        String telemarketerTLVOption=lRouteConfigInfo.getTelemarketerTLVOption();
			        
					String metadata=null;

					/*
					 * telemarketerTLVOption==0 --> no value passed to 1402 TLV
						telemarketerTLVOption==1 --> hash value passed to 1402 TLV --Hash Of
						telemarketerTLVOption==2 --> non hash value passed to 1402 TLV --> example (entityid,TMAID)
						telemarketerTLVOption==3 --> telemarketerid value alone passed to 1402 TLV --> (telemarketerid)
						
						Possible TLV Value 
							1. novalue
							2. telemar
					 */
			        if(telemarketerTLVOption==null||telemarketerTLVOption.trim().length()<1){
			        	
			        	telemarketerTLVOption="2";
			        }
			        
		        	int outgoingCarrierTelemarketerTLVOption=TELEMARKETERID_TLV_VALUE_NONHASHED;

			        
			        try {
				         outgoingCarrierTelemarketerTLVOption=Integer.parseInt(telemarketerTLVOption);

			        	
			        }catch(Exception e) {
			        	

			        }
			        
			    
						        String platformTelemarkerid=lRouteConfigInfo.getTelemartkerId();
								
						        if(platformTelemarkerid==null) {
						        	
						        	platformTelemarkerid="1234";
						        }
								String customerTelemarketerId=aMessageRequest.getDltTelemarketerId();
								
								if(customerTelemarketerId==null) {
									customerTelemarketerId="";
								}
								
								int incomingCustomerTelemarketerTLVOption=getTelemarketerIdTLVOption(customerTelemarketerId);
								
								String finalOutgoingTelemarkerId=getTelemarketerId( incomingCustomerTelemarketerTLVOption , outgoingCarrierTelemarketerTLVOption , customerTelemarketerId, platformTelemarkerid, entityid);

							
								KannelURLLog.log("incomingCustomerTelemarketerTLVOption : "+incomingCustomerTelemarketerTLVOption);
								KannelURLLog.log("outgoingCarrierTelemarketerTLVOption : "+outgoingCarrierTelemarketerTLVOption);
								KannelURLLog.log("customerTelemarketerId : "+customerTelemarketerId);
								KannelURLLog.log("platformTelemarkerid : "+platformTelemarkerid);
								KannelURLLog.log("entityid : "+entityid);
								KannelURLLog.log("finalOutgoingTelemarkerId : "+finalOutgoingTelemarkerId);

							if(finalOutgoingTelemarkerId!=null) {
								metadata="%3Fsmpp%3Fentityid="+entityid+"%26templateid="+templateid+"%26telemarketerid="+finalOutgoingTelemarkerId;
							}else {			    	
								metadata="%3Fsmpp%3Fentityid="+entityid+"%26templateid="+templateid+"%26";
							}
					
					
			        		reqmap.put("meta-data", metadata);
				
		    	} catch (MalformedURLException e) {
					
					

				}
		    	
					return reqmap;

			
	}

	private static String getTelemarketerId(int incomingCustomerTelemarketerTLVOption ,int outgoingCarrierTelemarketerTLVOption ,String customertelemartkerid,String platformtelemartkerid,String entityid) throws UnsupportedEncodingException {
		
		/*
		 * telemarketerTLVOption==0 --> no value passed to 1402 TLV
			telemarketerTLVOption==1 --> hash value passed to 1402 TLV --Hash Of
			telemarketerTLVOption==2 --> non hash value passed to 1402 TLV --> example (entityid,TMAID)
			telemarketerTLVOption==3 --> telemarketerid value alone passed to 1402 TLV --> (telemarketerid)
			
			Possible TLV Value 
				1. novalue
				2. telemar
		 */
		String telemarketerid=null;
		
		switch(outgoingCarrierTelemarketerTLVOption) {
		
		case TELEMARKETERID_TLV_VALUE_NO:{
			telemarketerid=null; //nothing to pass
			KannelURLLog.log(" out: TELEMARKETERID_TLV_VALUE_NO "+TELEMARKETERID_TLV_VALUE_NO );
			break;
		}
		case TELEMARKETERID_TLV_VALUE_TELEMARKETERID:{
						telemarketerid=platformtelemartkerid; //customer telemarketerid will be ignored
						KannelURLLog.log(" out: TELEMARKETERID_TLV_VALUE_TELEMARKETERID "+TELEMARKETERID_TLV_VALUE_TELEMARKETERID );
						break;
		}
		case TELEMARKETERID_TLV_VALUE_HASHED:{
			
			switch(incomingCustomerTelemarketerTLVOption) {
			
			case TELEMARKETERID_TLV_VALUE_NO:{
				telemarketerid=entityid+","+platformtelemartkerid;
				telemarketerid=getHashValue(telemarketerid);
				KannelURLLog.log(" out: TELEMARKETERID_TLV_VALUE_HASHED "+TELEMARKETERID_TLV_VALUE_HASHED +" in TELEMARKETERID_TLV_VALUE_NO : "+TELEMARKETERID_TLV_VALUE_NO );
				break;
			}
			case TELEMARKETERID_TLV_VALUE_TELEMARKETERID:{
				telemarketerid=entityid+","+customertelemartkerid+","+platformtelemartkerid;
				telemarketerid=getHashValue(telemarketerid);
				KannelURLLog.log(" out: TELEMARKETERID_TLV_VALUE_HASHED "+TELEMARKETERID_TLV_VALUE_HASHED +" in TELEMARKETERID_TLV_VALUE_TELEMARKETERID : "+TELEMARKETERID_TLV_VALUE_TELEMARKETERID );

			}
				break;
			case TELEMARKETERID_TLV_VALUE_HASHED:{
				telemarketerid=customertelemartkerid;  //plaform value will be ignored customer value will be passed to carrier
				KannelURLLog.log(" out: TELEMARKETERID_TLV_VALUE_HASHED "+TELEMARKETERID_TLV_VALUE_HASHED +" in TELEMARKETERID_TLV_VALUE_HASHED : "+TELEMARKETERID_TLV_VALUE_HASHED );

				break;
			}
			case TELEMARKETERID_TLV_VALUE_NONHASHED:{
				telemarketerid=customertelemartkerid+","+platformtelemartkerid;
				telemarketerid=getHashValue(telemarketerid);
				KannelURLLog.log(" out: TELEMARKETERID_TLV_VALUE_HASHED "+TELEMARKETERID_TLV_VALUE_HASHED +" in TELEMARKETERID_TLV_VALUE_NONHASHED : "+TELEMARKETERID_TLV_VALUE_NONHASHED );

				break;
			}
				
		}
			break;
		}
		case TELEMARKETERID_TLV_VALUE_NONHASHED:
		{
					switch(incomingCustomerTelemarketerTLVOption) {
					
						case TELEMARKETERID_TLV_VALUE_NO:{
							telemarketerid=entityid+","+platformtelemartkerid;
							KannelURLLog.log(" out: TELEMARKETERID_TLV_VALUE_NONHASHED "+TELEMARKETERID_TLV_VALUE_NONHASHED +" in TELEMARKETERID_TLV_VALUE_NO : "+TELEMARKETERID_TLV_VALUE_NO );
							break;
						}
						case TELEMARKETERID_TLV_VALUE_TELEMARKETERID:{
							telemarketerid=entityid+","+customertelemartkerid+","+platformtelemartkerid;
							KannelURLLog.log(" out: TELEMARKETERID_TLV_VALUE_NONHASHED "+TELEMARKETERID_TLV_VALUE_NONHASHED +" in TELEMARKETERID_TLV_VALUE_TELEMARKETERID : "+TELEMARKETERID_TLV_VALUE_TELEMARKETERID );

							break;
						}
						case TELEMARKETERID_TLV_VALUE_HASHED:{
							telemarketerid=customertelemartkerid; //plaform value will be ignored customer value will be passed to carrier
							KannelURLLog.log(" out: TELEMARKETERID_TLV_VALUE_NONHASHED "+TELEMARKETERID_TLV_VALUE_NONHASHED +" in TELEMARKETERID_TLV_VALUE_HASHED : "+TELEMARKETERID_TLV_VALUE_HASHED );

							break;
						}
						case TELEMARKETERID_TLV_VALUE_NONHASHED:{
							telemarketerid=customertelemartkerid+","+platformtelemartkerid;
							KannelURLLog.log(" out: TELEMARKETERID_TLV_VALUE_NONHASHED "+TELEMARKETERID_TLV_VALUE_NONHASHED +" in TELEMARKETERID_TLV_VALUE_NONHASHED : "+TELEMARKETERID_TLV_VALUE_NONHASHED );

							break;
						}
					}
					break;
		}
		}

		if(telemarketerid!=null) {
			
			telemarketerid=URLEncoder.encode(CommonUtility.nullCheck(telemarketerid, true), Constants.ENCODER_FORMAT);
		}
		
		return telemarketerid;
	}

	private static boolean isClientPassingTelemarketerIdHashed(String telemarketerid) {
		
		
		return !isNumeric(telemarketerid);
	}
	
	public static boolean isNumeric(String str) {
        // Check if the string is null or empty
        if (str == null || str.isEmpty()) {
            return false;
        }

        // Check if all characters are digits
        for (char c : str.toCharArray()) {
            if (!Character.isDigit(c)) {
                return false;
            }
        }

        return true;
    }

	private static int getTelemarketerIdTLVOption(String temptelemartkerid) {
		
		StringTokenizer st=new StringTokenizer(temptelemartkerid,",");
		
	

		if(st.hasMoreTokens()) {
			
			st.nextToken();
		}else {
			
			return TELEMARKETERID_TLV_VALUE_NO;
		}
		
		if(st.hasMoreTokens()) {
			
			return TELEMARKETERID_TLV_VALUE_NONHASHED;
			
		}else {
			
			if(temptelemartkerid==null||temptelemartkerid.trim().length()<1) {
				
				return TELEMARKETERID_TLV_VALUE_NO;
				
			}else {
				
				if(isClientPassingTelemarketerIdHashed(temptelemartkerid)) {
				
				return TELEMARKETERID_TLV_VALUE_HASHED;
				}else {
				
				return TELEMARKETERID_TLV_VALUE_TELEMARKETERID;

				}
			}
		}
	}

	public static String getHashValue(String telemarketerid) {
		
		KannelURLLog.log(" do Hash");

		  String hash = ""; 
		    try { 
		      MessageDigest digest = MessageDigest.getInstance("SHA-256"); 
		      byte[] valbyte = digest.digest(telemarketerid.getBytes()); 
		 
		 
		      StringBuilder hexString = new StringBuilder(2 * valbyte.length); 
		      for (byte b : valbyte) { 
		        String hex = Integer.toHexString(0xff & b); 
		        if (hex.length() == 1) { 
		          hexString.append('0'); 
		        } 
		        hexString.append(hex); 
		      } 
		      hash = hexString.toString(); 
		    }  
		    catch (Exception e) { 
		    } 
		    
			KannelURLLog.log(" do Hash Telemarketer Id : "+telemarketerid+ " hash : "+ hash);

		    return hash; 

	}

	private static void doMessageRetry(
            MessageRequest aMessageRequest,
            SubmissionObject aSubmissionObject)
    {
    	aMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(aMessageRequest.getFileId()+" :: Sending to retry queue due to kannel down/storesize/latency route:" + aMessageRequest.getRouteId());


        try
        {
            aSubmissionObject.setMtMessageRetryIdentifier(Constants.ENABLED);
            PayloadProcessor.removePayload(aSubmissionObject);
        }
        catch (final Exception e)
        {
            
        	aMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(aMessageRequest.getFileId()+" :: Exception while removing payload .. :: "+ErrorMessage.getStackTraceAsString(e));

        }

        CHProducer.sendToRetryRoute(aMessageRequest);
    }

    private static void sendToPlatfromRejectionWithRemovePayload(
            SubmissionObject aSubmissionObject,
            PlatformStatusCode aStatusId)
    {

        try
        {
            aSubmissionObject.setMtMessageRetryIdentifier(Constants.ENABLED);
            PayloadProcessor.removePayload(aSubmissionObject);
        }
        catch (final Exception e)
        {
            
            aSubmissionObject.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(aSubmissionObject.getBaseMessageId()+" Exception while removing payload .. :: "+ErrorMessage.getStackTraceAsString(e));

        }

        sendToPlatfromRejection(aSubmissionObject, aStatusId);
    }

    private static void sendToPlatfromRejectionWithRemovePayload(
            SubmissionObject aSubmissionObject,
            MessageRequest aMessageRequest,
            PlatformStatusCode aStatusId)
    {

        try
        {
            aSubmissionObject.setMtMessageRetryIdentifier(Constants.ENABLED);
            PayloadProcessor.removePayload(aSubmissionObject);
        }
        catch (final Exception e)
        {
            
            aSubmissionObject.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(aSubmissionObject.getBaseMessageId()+" Exception while removing payload .. :: "+ErrorMessage.getStackTraceAsString(e));

        }

        sendToPlatfromRejection(aMessageRequest, aStatusId);
    }

    private static void sendToPlatfromRejection(
            SubmissionObject aSubmissionObject,
            PlatformStatusCode aStatusId)
    {
        aSubmissionObject.setSubOriginalStatusCode(aStatusId.getStatusCode());
        CHProducer.sendToPlatfromRejection(aSubmissionObject);
    }

    private static void sendToPlatfromRejection(
            MessageRequest aMessageRequest,
            PlatformStatusCode aStatusId)
    {
        aMessageRequest.setSubOriginalStatusCode(aStatusId.getStatusCode());
        CHProducer.sendToPlatfromRejection(aMessageRequest);
    }

    @Override
    public void doCleanup()
    {
        // TODO Auto-generated method stub
    }

    @Override
    protected void updateBeforeSendBack(
            IMessage aMessage)
    {
        // TODO Auto-generated method stub
    }

}