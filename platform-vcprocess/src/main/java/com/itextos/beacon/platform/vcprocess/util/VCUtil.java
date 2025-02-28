package com.itextos.beacon.platform.vcprocess.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.constants.ConfigParamConstants;
import com.itextos.beacon.commonlib.constants.CustomFeatures;
import com.itextos.beacon.commonlib.constants.ErrorMessage;
import com.itextos.beacon.commonlib.constants.MiddlewareConstant;
import com.itextos.beacon.commonlib.constants.PlatformStatusCode;
import com.itextos.beacon.commonlib.message.MessageRequest;
import com.itextos.beacon.commonlib.message.utility.MessageUtil;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.Name;
import com.itextos.beacon.inmemory.configvalues.ApplicationConfiguration;
import com.itextos.beacon.inmemory.customfeatures.InmemCustomFeatures;
import com.itextos.beacon.inmemory.loader.InmemoryLoaderCollection;
import com.itextos.beacon.inmemory.loader.process.InmemoryId;
import com.itextos.beacon.inmemory.userheader.DomesticUserHeaderInfo;
import com.itextos.beacon.platform.cappingcheck.CappingMessageChecker;
import com.itextos.beacon.platform.duplicatecheckprocessor.DuplicateCheck;
import com.itextos.beacon.platform.msgtimeboundcheck.TimeBoundMessageChecker;
import com.itextos.beacon.platform.templatefinder.Result;
import com.itextos.beacon.platform.templatefinder.TemplateResult;
import com.itextos.beacon.platform.templatefinder.TemplateScrubber;

public class VCUtil
{

    private static final Log log                            = LogFactory.getLog(VCUtil.class);

    private static final int DUPLICATE_BASED_ON_CUST_MID    = 1;
    private static final int DUPLICATE_BASED_ON_MSG_MNUMBER = 2;

    private VCUtil()
    {}

    public static boolean doDuplicateChk(
            MessageRequest aMessageRequest)
    {
        final int    lDuplicateType        = aMessageRequest.getDuplicateCheckEnabled();
        final String lClientId             = aMessageRequest.getClientId();
        final String lMessageId            = aMessageRequest.getBaseMessageId();
        final int    lDuplicateChkInterval = aMessageRequest.getDuplicateCheckInterval();

      
        aMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(aMessageRequest.getFileId()+" : Duplicate Checking for Client : " + lClientId );

        if (aMessageRequest.getDupCheckForUI() == 1)
        {
            final String lCampId = CommonUtility.nullCheck(aMessageRequest.getCampaignId(), true);

      
            aMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(aMessageRequest.getFileId()+" :: Duplicate Checking for Client : '" + lClientId + "' :: Campiagn Id : '" + lCampId + "'" );

            final String lMobileNumber = aMessageRequest.getMobileNumber();
            return DuplicateCheck.isDuplicatCampiagn(lClientId, lMobileNumber, lCampId);
        }

  
        aMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(aMessageRequest.getFileId()+"Duplicate Check for other applications .." );

        if (DUPLICATE_BASED_ON_CUST_MID == lDuplicateType)
        {
            final String lCustMid = CommonUtility.nullCheck(aMessageRequest.getClientMessageId(), true);
            if (lCustMid.isBlank())
                return false;

            return DuplicateCheck.isDuplicateCustRef(lClientId, lCustMid.toLowerCase(), lDuplicateChkInterval * 60);
        }
        else
            if (DUPLICATE_BASED_ON_MSG_MNUMBER == lDuplicateType)
            {
                final String lMessage      = CommonUtility.nullCheck(aMessageRequest.getLongMessage());
                final String lMobileNumber = aMessageRequest.getMobileNumber();

                return DuplicateCheck.isDuplicateMessage(lClientId, lMobileNumber, lMessage.toLowerCase(), lDuplicateChkInterval * 60);
            }

        return false;
    }

    public static boolean doTimeboundChk(
            MessageRequest aMessageRequest)
    {
        final String  lClientId         = aMessageRequest.getClientId();
        final String  lMobileNumber     = aMessageRequest.getMobileNumber();

        final boolean isTimeBoundEnable = aMessageRequest.getTimeboundCheck();

  
        aMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(aMessageRequest.getFileId()+" : Time boundCheck Enable :" + isTimeBoundEnable );


        if (isTimeBoundEnable)
        {
            final int lTimeBoundInterval = aMessageRequest.getTimeboundInterval();
            final int lTimeBoundMaxCount = aMessageRequest.getTimeboundMaxReqCount();

            aMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(aMessageRequest.getFileId()+" : Timebound interval : '" + lTimeBoundInterval + "', TimeBound Max Count:'" + lTimeBoundMaxCount + "'");

            return TimeBoundMessageChecker.increaseMsgCounter(lClientId, CommonUtility.getLong(lMobileNumber), lTimeBoundInterval, lTimeBoundMaxCount);
        }

        return true;
    }

    public static String validateDLTReq(
            MessageRequest aMessageRequest)
            throws Exception
    {
        final String lCustDltEntityId   = CommonUtility.nullCheck(aMessageRequest.getDltEntityId(), true);
        final String lCustDltTemplateId = CommonUtility.nullCheck(aMessageRequest.getDltTemplateId(), true);
        final String lTemplateGroupId   = CommonUtility.nullCheck(aMessageRequest.getDltTemplateGroupId(), true);
        final String lHeader            = MessageUtil.getHeaderId(aMessageRequest);

        aMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(aMessageRequest.getFileId()+" :: cust_dlt_entityId:'" + lCustDltEntityId + "', cust_dlt_templateId:'" + lCustDltTemplateId + "', TemplateGroupId :'" + lTemplateGroupId + "', header:'" + lHeader + "'" );

        final DomesticUserHeaderInfo lDomesticUserHeaderInfo = (DomesticUserHeaderInfo) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.DOMESTIC_USER_HEADERS);
        final String                 lHeaderBasedEntity      = CommonUtility.nullCheck(lDomesticUserHeaderInfo.getEntityId(lTemplateGroupId, lHeader.toLowerCase()));

        /*
         * if (!lCustDltEntityId.isBlank())
         * lHeaderBasedEntity = lCustDltEntityId;
         * else
         * {
         * final DomesticUserHeaderInfo lDomesticUserHeaderInfo =
         * (DomesticUserHeaderInfo)
         * InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.
         * DOMESTIC_USER_HEADERS);
         * lHeaderBasedEntity =
         * CommonUtility.nullCheck(lDomesticUserHeaderInfo.getEntityId(lTemplateGroupId,
         * lHeader.toLowerCase()));
         * if (log.isDebugEnabled())
         * log.
         * debug("Header EntityId from dlt_template_group_header_entity_map table : '" +
         * lHeaderBasedEntity + "'");
         * }
         */

        if (CommonUtility.nullCheck(lHeaderBasedEntity, true).equals(""))
        {
        	aMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(aMessageRequest.getFileId()+" :: No entity id found in dlt_template_group_header_entity_map against Template Group Id:'" + lTemplateGroupId + "', Header:'" + lHeader + "'" );

            
            return PlatformStatusCode.INVALID_DLT_ENTITY_ID.getStatusCode();
        }

        if (!lCustDltEntityId.isBlank() && !lCustDltEntityId.equals(lHeaderBasedEntity))
            aMessageRequest.setAdditionalErrorInfo("Customer DltEntityId '" + lCustDltEntityId + "' not maching with the header-entityid map.");
        aMessageRequest.setDltEntityId(lHeaderBasedEntity);

        final boolean isFetchTemplateId = scrubTemplateId(aMessageRequest);

        if (!isFetchTemplateId)
        {
        	aMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(aMessageRequest.getFileId()+"  :: No template id found in DLT_Templates table against entityid:'" + lHeaderBasedEntity + "', header:'" + lHeader + "', dlt_templte_group_id:'" + lTemplateGroupId + "'" );

             return PlatformStatusCode.REJECTED_IN_DLT_TEMPLATE_CHECK.getStatusCode();
        }

        return null;
    }

    private static boolean scrubTemplateId(
            MessageRequest aMessageRequest)
    {
        boolean templateMatchStatus = false;

        try
        {
            final String lHeader           = MessageUtil.getHeaderId(aMessageRequest);
            final String lDltTemplateGrpId = CommonUtility.nullCheck(aMessageRequest.getDltTemplateGroupId(), true);

            final String lLongMessage      = CommonUtility.nullCheck(aMessageRequest.getLongMessage(), true);

            aMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(aMessageRequest.getFileId()+" :: DltTemplateGrpId:'" + lDltTemplateGrpId + "',  Header:'" + lHeader + "', Long Message:'" + lLongMessage + "'" );

            TemplateResult lTemplateResult = null;

            final boolean  lIsHexMsg       = aMessageRequest.isHexMessage();

            if (lIsHexMsg)
                lTemplateResult = TemplateScrubber.getTemplateHexMessage(lDltTemplateGrpId, lHeader, lLongMessage);
            else
                lTemplateResult = TemplateScrubber.getTemplate(lDltTemplateGrpId, lHeader, lLongMessage);

      


            if (lTemplateResult != null)
            {
            	aMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(aMessageRequest.getFileId()+"lTemplateRresult==" + lTemplateResult );

                final Result lResult = lTemplateResult.getResult();

                aMessageRequest.setAdditionalErrorInfo(getDltErrorReason(lResult));

                if ((Result.TEMPLATE_FOUND == lResult) || (Result.TEMPLATE_MATCHES_WITH_NO_VAR_MESSAGES == lResult))
                {
                    final String lTempTemplateId = CommonUtility.nullCheck(lTemplateResult.getTemplateId(), true);

                    if (!lTempTemplateId.isEmpty())
                    {
                        aMessageRequest.setDltTemplateId(lTempTemplateId);
                        templateMatchStatus = true;
                    }
                    else
                        aMessageRequest.setDltTemplateId("-1");
                }
                else
                    aMessageRequest.setDltTemplateId("-1");
            }
        }
        catch (final Exception exp)
        {
            aMessageRequest.setAdditionalErrorInfo(exp.getMessage());

            aMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(aMessageRequest.getFileId()+" ::  problem finding the templates due to..."+ ErrorMessage.getStackTraceAsString(exp));

            log.error("", exp);
        }
        return templateMatchStatus;
    }

    public static String getAppConfigValueAsString(
            ConfigParamConstants aConfigParamConstant)
    {
        final ApplicationConfiguration lAppConfiguration = (ApplicationConfiguration) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.APPLICATION_CONFIG);
        return lAppConfiguration.getConfigValue(aConfigParamConstant.getKey());
    }

    public static String getAppConfigValueAsString(
            String aKey)
    {
        final ApplicationConfiguration lAppConfiguration = (ApplicationConfiguration) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.APPLICATION_CONFIG);
        return lAppConfiguration.getConfigValue(aKey);
    }

    public static String getCutomFeatureValue(
            String aClientId,
            CustomFeatures aFeature)
    {
        final InmemCustomFeatures lCustomFeatures = (InmemCustomFeatures) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.CUSTOM_FEATURES);
        return lCustomFeatures.getValueOfCustomFeature(aClientId, aFeature.getKey());
    }

    private static String getDltErrorReason(
            Result aResult)
    {
        String lReason = "";

        switch (aResult)
        {
            case TEMPLATE_FOUND:
                // lReason = "Template Found";
                break;

            case TEMPLATE_MATCHES_WITH_EXISTING_FAILED_MESSAGES:
                lReason = "Template Matches With Existing Faild Messages";
                break;

            case TEMPLATE_MATCHES_WITH_NO_VAR_MESSAGES:
                lReason = "Template Matches With Not Variable Present in Messages";
                break;

            case TEMPLATE_INVALID_INPUTS:
                lReason = "Invalid Template Inputs";
                break;

            case TEMPLATE_NOT_FOUND:
                lReason = "Template Not Found";
                break;

            default:
                lReason = "";
                break;
        }
        return lReason;
    }

    private static boolean doCappingChk(
            MessageRequest aMessageRequest)
    {
        final String  lClientId       = aMessageRequest.getClientId();

        final boolean isCappingEnable = CommonUtility.isEnabled(aMessageRequest.getValue(MiddlewareConstant.MW_CAPPING_CHK_ENABLED));

     
        aMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(aMessageRequest.getFileId()+" :: Capping Enable :" + isCappingEnable);

         

        if (isCappingEnable)
        {
            final long lCappingMaxReqCount = CommonUtility.getLong(aMessageRequest.getValue(MiddlewareConstant.MW_CAPPING_MAX_REQ_COUNT));
            int        lTotalMsgParts      = aMessageRequest.getMessageTotalParts();
            lTotalMsgParts = (lTotalMsgParts == 0) ? 1 : lTotalMsgParts;

            aMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(aMessageRequest.getFileId()+" :: Capping Max Request Allow Count : '" + lCappingMaxReqCount + "', Message Part Count :'" + lTotalMsgParts + "'");

            return CappingMessageChecker.doCappingCheck(lClientId, lCappingMaxReqCount, lTotalMsgParts);
        }

        return true;
    }

    public static boolean doCappingCheck(
            MessageRequest aMessageRequest,
            Component aSourceComponent)
    {

        if (!doCappingChk(aMessageRequest))
        {
        	aMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(aMessageRequest.getFileId()+" Message Rejected Capping Check Failed");

              aMessageRequest.setSubOriginalStatusCode(PlatformStatusCode.REJECT_CAPPING_CHECK.getStatusCode());
            VCProducer.sendToPlatformRejection(aSourceComponent, aMessageRequest);
            return false;
        }

        return true;
    }

}
