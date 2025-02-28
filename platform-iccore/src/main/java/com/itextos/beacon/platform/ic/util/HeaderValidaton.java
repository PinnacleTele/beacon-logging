package com.itextos.beacon.platform.ic.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.PlatformStatusCode;
import com.itextos.beacon.commonlib.message.MessageRequest;
import com.itextos.beacon.commonlib.message.utility.MessageUtil;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.Name;
import com.itextos.beacon.inmemory.governmentheaders.GovtHeaderBlockCheck;
import com.itextos.beacon.inmemory.loader.InmemoryLoaderCollection;
import com.itextos.beacon.inmemory.loader.process.InmemoryId;
import com.itextos.beacon.platform.msgflowutil.util.PlatformUtil;

public class HeaderValidaton
{

    private static Log           log = LogFactory.getLog(HeaderValidaton.class);
    private final MessageRequest mMessageRequest;

    public HeaderValidaton(
            MessageRequest aMessageRequest)
    {
        this.mMessageRequest = aMessageRequest;
    }

    public boolean validate()
    {
        boolean isGovtHeader = false;
        isGovtHeader = isGovernmentHeader();

        
        /**
         * Bye pass the other Header validation for DLT Template & Entity in customer
         * request.
         */
        /*
        if (!isGovtHeader && mMessageRequest.isBypassDltCheck())
        {
            final boolean isHeaderPatternFailed = PlatformUtil.isHeaderPatternFailed(mMessageRequest);

     
            
            mMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(mMessageRequest.getFileId()+" : Transactional/Promotioanl Header pattern check status : " + isHeaderPatternFailed);

            if (!isHeaderPatternFailed)
            {
            
                mMessageRequest.setSubOriginalStatusCode(PlatformStatusCode.HEADER_PATTERN_CHECK_FAILED.getStatusCode());
                mMessageRequest.setPlatfromRejected(true);
                return false;
            }
            return true;
        }*/

        /*
        if (!isGovtHeader)
        {
            final boolean isValidHeaderForDelivery = isUserHeaderAvailable();

            if (log.isDebugEnabled())
                log.debug("After validate UserHeaderAvailable: " + MessageUtil.getHeaderId(mMessageRequest));

            if (!isValidHeaderForDelivery)
            {
                mMessageRequest.setSubOriginalStatusCode(PlatformStatusCode.INVALID_HEADER.getStatusCode());
                mMessageRequest.setPlatfromRejected(true);
                return false;
            }

            isGovtHeader = isGovernmentHeader();

            if (!isGovtHeader)
            {
                boolean isHeaderPatternFailed = PlatformUtil.isHeaderPatternFailed(mMessageRequest);

                if (log.isDebugEnabled())
                    log.debug("Transactional/Promotioanl Header pattern check status : " + isHeaderPatternFailed);

                if (!isHeaderPatternFailed)
                {
                    isGovtHeader = isGovernmentHeader();

                    if (!isGovtHeader)
                    {
                        isHeaderPatternFailed = PlatformUtil.isHeaderPatternFailed(mMessageRequest);

                        if (!isHeaderPatternFailed)
                        {
                            if (log.isDebugEnabled())
                                log.debug("Final Transactional/Promotioanl Header pattern check status : " + isHeaderPatternFailed);

                            mMessageRequest.setSubOriginalStatusCode(PlatformStatusCode.HEADER_PATTERN_CHECK_FAILED.getStatusCode());
                            mMessageRequest.setPlatfromRejected(true);
                            return false;
                        }
                    }
                }
            }
        }
	*/
        return true;
    }

    private boolean isGovernmentHeader()
    {
        if (log.isDebugEnabled())
            log.debug("Base Message Id : " + mMessageRequest.getBaseMessageId());

        final String lHeader = MessageUtil.getHeaderId(mMessageRequest);
        boolean      result  = false;

        if (!lHeader.isEmpty())
        {
            final GovtHeaderBlockCheck lGovtHeaderBlockCheck = (GovtHeaderBlockCheck) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.GOVT_HEADER_BLOCK);
            result = lGovtHeaderBlockCheck.isGovernmentHeader(lHeader);
        }

        mMessageRequest.setGovermentHeader(result == true);

        return result;
    }

    private boolean isUserHeaderAvailable()
    {
        if (log.isDebugEnabled())
            log.debug("Base Message Id" + mMessageRequest.getBaseMessageId());

        final boolean lDefaultHeaderCheck    = mMessageRequest.isDefailtHeaderEnable();
        final boolean lDefautHeaderCheckFail = mMessageRequest.isDefailtHeaderFailEnable();
        final String  lDefaultHeader         = CommonUtility.nullCheck(mMessageRequest.getClientDefaultHeader(), true);
        final String  lHeader                = MessageUtil.getHeaderId(mMessageRequest);
        final String  lTemplateGroupId       = mMessageRequest.getDltTemplateGroupId();

        if (log.isDebugEnabled())
        {
            log.debug("Account Header check : '" + lDefaultHeaderCheck + "'");
            log.debug("Account Level header : '" + lDefaultHeader + "'");
            log.debug("lTemplateGroupId : '" + lTemplateGroupId + "'");

        }
        
        if(!mMessageRequest.isIsIntl()&&mMessageRequest.isIldo()) {
        
        	return true;
        }

        if (lDefaultHeaderCheck && !lDefaultHeader.isBlank())
        {
            MessageUtil.setHeaderId(mMessageRequest, lDefaultHeader);
            mMessageRequest.setClientHeader(lHeader);
        }

        boolean isValidHeader = ICUtility.isValidUserHeader(lTemplateGroupId, mMessageRequest.getHeader());

        if (log.isDebugEnabled())
            log.debug("Is Header '" + mMessageRequest.getHeader() + "' valiid :  " + isValidHeader);

        if (isValidHeader)
            return true;

        if (lDefautHeaderCheckFail && !lDefaultHeaderCheck)
        {
            mMessageRequest.setClientHeader(lHeader);
            MessageUtil.setHeaderId(mMessageRequest, lDefaultHeader);
            isValidHeader = ICUtility.isValidUserHeader(lTemplateGroupId, mMessageRequest.getHeader());

            if (log.isDebugEnabled())
                log.debug("Is Header '" + mMessageRequest.getHeader() + "' valiid :  " + isValidHeader);

            if (isValidHeader)
                return true;
        }

        mMessageRequest.setSubOriginalStatusCode(PlatformStatusCode.INVALID_HEADER.getStatusCode());
        return false;
    }

}