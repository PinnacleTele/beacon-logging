package com.itextos.beacon.platform.dnpcore.dlrclienthandover.process;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.constants.DlrHandoverMode;
import com.itextos.beacon.commonlib.constants.ErrorMessage;
import com.itextos.beacon.commonlib.constants.MiddlewareConstant;
import com.itextos.beacon.commonlib.message.DeliveryObject;
import com.itextos.beacon.commonlib.message.utility.MessageUtil;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.ItextosClient;
import com.itextos.beacon.inmemory.clidlrpref.ClientDlrAdminDelivery;
import com.itextos.beacon.inmemory.clidlrpref.ClientDlrConfig;
import com.itextos.beacon.inmemory.clidlrpref.ClientDlrConfigUtil;
import com.itextos.beacon.platform.dnpcore.log.DNPLog;

public class DlrClientHandover
{

    private static final Log log = LogFactory.getLog(DlrClientHandover.class);

    private DlrClientHandover()
    {}

    public static void processClientHandover(
            DeliveryObject aDeliveryObject,
            Map<Component, DeliveryObject> lNextComponent)
    {
    
        try
        {
            generateClientReq(aDeliveryObject, lNextComponent);

         }
        catch (final Exception e)
        {
        	
        	aDeliveryObject.getLogBufferValue(MiddlewareConstant.MW_LOG_BUFFER).append("\n").append(aDeliveryObject.getMessageId()+" :Error :: " + ErrorMessage.getStackTraceAsString(e));

            DNPLog.getInstance(aDeliveryObject.getClientId()).log(aDeliveryObject.getClientId(),aDeliveryObject.getFileId()+ " : "+ aDeliveryObject.getMessageId() + " :Error :: " + ErrorMessage.getStackTraceAsString(e) );

        }
    }

    private static void generateClientReq(
            DeliveryObject aDeliveryObject,
            Map<Component, DeliveryObject> aNextComponentMap)
            throws Exception
    {

        try
        {
            if (aDeliveryObject == null) {
                DNPLog.getInstance(aDeliveryObject.getClientId()).log(aDeliveryObject.getClientId(),aDeliveryObject.getFileId()+ " : "+ aDeliveryObject.getMessageId() + " : aDeliveryObject :: is null ");

                return;
            }
          
            
            final ClientDlrConfig lClientDlrConfig = ClientDlrConfigUtil.getDlrHandoverConfig(aDeliveryObject.getClientId(), "sms", aDeliveryObject.getInterfaceType(),
                    aDeliveryObject.isDlrRequestFromClient());
            
            
            DNPLog.getInstance(aDeliveryObject.getClientId()).log(aDeliveryObject.getClientId(),aDeliveryObject.getFileId()+ " : "+ aDeliveryObject.getMessageId() + " :lClientDlrConfig :: " + lClientDlrConfig);

            
        	aDeliveryObject.getLogBufferValue(MiddlewareConstant.MW_LOG_BUFFER).append("\n").append(aDeliveryObject.getMessageId()+" :lClientDlrConfig :: " + lClientDlrConfig);


            if (lClientDlrConfig == null)
                return;

     
        	aDeliveryObject.getLogBufferValue(MiddlewareConstant.MW_LOG_BUFFER).append("\n").append(aDeliveryObject.getMessageId()+" : Client Dlr Preferences :: " + lClientDlrConfig.toString());

            
            DeliveryObject lNewDeliveryObject = null;
          
            String         lHeader            = MessageUtil.getHeaderId(aDeliveryObject);
            final String   lMaskedHeader      = CommonUtility.nullCheck(aDeliveryObject.getMaskedHeader(), true);

            if (!lMaskedHeader.isEmpty())
                // header will be set in routing when header masked
                lHeader = aDeliveryObject.getClientHeader();

            MessageUtil.setHeaderId(aDeliveryObject, lHeader);

            final ClientDlrAdminDelivery lDlrToSu = lClientDlrConfig.getDlrToSu();
            lNewDeliveryObject = aDeliveryObject.getClonedDeliveryObject();

            switch (lDlrToSu)
            {
                case ADMIN_USER:
                    setParentUser(lNewDeliveryObject, ClientDlrAdminDelivery.ADMIN_USER);
                    break;

                case SUPER_USER:
                    setParentUser(lNewDeliveryObject, ClientDlrAdminDelivery.SUPER_USER);
                    break;

                case NONE:
                default:
                    break;
            }

            final DlrHandoverMode lDlrHandoverMode = lClientDlrConfig.getDlrHandoverMode();

            DNPLog.getInstance(aDeliveryObject.getClientId()).log(aDeliveryObject.getClientId(),aDeliveryObject.getFileId()+ " : "+ aDeliveryObject.getMessageId() + " : Dlr Handover Mode : " + lDlrHandoverMode );
          
            aDeliveryObject.getLogBufferValue(MiddlewareConstant.MW_LOG_BUFFER).append("\n").append(aDeliveryObject.getMessageId()+" : Dlr Handover Mode : " + lDlrHandoverMode);


            switch (lDlrHandoverMode)
            {
                case API:
              
                    aNextComponentMap.put(Component.HTTP_DLR, lNewDeliveryObject);
                    break;

                case SMPP:
                    final DeliveryObject lSmppDLRObj = DlrSmppGenerator.generateDlrQueue(lNewDeliveryObject);

              
                    aNextComponentMap.put(Component.SMPP_DLR, lSmppDLRObj);
                    break;

                case FTP:
                case NODLR:
                default:
                    break;
            }

            if (lClientDlrConfig.isDlrQueryEnabled())
            {
                if (log.isDebugEnabled())
                    log.debug(" Processing DLR_QUERY_DN_TOPIC for Message Id " + aDeliveryObject.getMessageId());
                aNextComponentMap.put(Component.DLRQDN, aDeliveryObject);
            }
        }
        catch (final Exception e1)
        {
            log.error(" Exception while process message:", e1);
            e1.printStackTrace();
            throw e1;
        }
    }

    private static void setParentUser(
            DeliveryObject aDeliveryObject,
            ClientDlrAdminDelivery flag)
    {

        try
        {
            final String        lCliemtId = aDeliveryObject.getClientId();
            final ItextosClient lClient   = new ItextosClient(lCliemtId);

            if (flag == ClientDlrAdminDelivery.SUPER_USER)
                aDeliveryObject.setClientId(lClient.getSuperAdmin() + "00000000");
            else
                aDeliveryObject.setClientId(lClient.getAdmin() + "0000");

            log.info("changeClientAsRequired is ::" + aDeliveryObject.getClientId());
        }
        catch (final Exception e)
        {
            e.printStackTrace();
            throw e;
        }
    }

}
