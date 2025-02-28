package com.itextos.beacon.platform.singledn.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.exception.ItextosException;
import com.itextos.beacon.inmemory.customfeatures.pojo.DNDeliveryMode;
import com.itextos.beacon.inmemory.customfeatures.pojo.DlrTypeInfo;
import com.itextos.beacon.platform.singledn.AbstractSingleDnProcess;
import com.itextos.beacon.platform.singledn.data.SingleDnInfo;
import com.itextos.beacon.platform.singledn.data.SingleDnRequest;
import com.itextos.beacon.platform.singledn.enums.DnStatus;
import com.itextos.beacon.platform.singledn.enums.ValidationStatus;

public class SingleDnProcessorPartialSuccess
        extends
        AbstractSingleDnProcess
{

    private static final Log log = LogFactory.getLog(SingleDnProcessorPartialFailure.class);
    private boolean          successAvailable;
    private boolean          failureAvailable;

    public SingleDnProcessorPartialSuccess(
            SingleDnRequest aSingleDnRequest,
            DlrTypeInfo aDlrTypeInfo)
            throws ItextosException
    {
        super(aSingleDnRequest, aDlrTypeInfo);
    }

    @Override
    public boolean addSingleDnInfo(
            SingleDnInfo aSingleDnInfo)
            throws ItextosException
    {
        boolean returnValue = super.addSingleDnInfoLocal(aSingleDnInfo);

        if (returnValue)
        {
            if (log.isDebugEnabled())
                log.debug("Single DN Map size : '" + singleDnMap.size() + "' Total Parts : '" + mTotalNumberOfParts + "' Dn Status " + aSingleDnInfo.getDnStatus() + " Success Available "
                        + successAvailable + " Failure Available " + failureAvailable);

            if (aSingleDnInfo.getDnStatus() == DnStatus.SUCCESS)
                successAvailable = true;

            if (aSingleDnInfo.getDnStatus() == DnStatus.FAILURE)
                failureAvailable = true;

            if (successAvailable && failureAvailable)
            {
                mIsValidationSuccess = ValidationStatus.SUCCESS;
                returnValue          = true;
            }
            else
                if (singleDnMap.size() != mTotalNumberOfParts)
                {
                    mIsValidationSuccess = ValidationStatus.IN_COMPLETE;
                    returnValue          = false;
                }
                else
                {
                    mIsValidationSuccess = ValidationStatus.FAILED;
                    returnValue          = true;
                }
        }

        if (log.isInfoEnabled())
            log.info("For Client Id '" + mSingleDnRequest.getClientId() + "' Validation status '" + mIsValidationSuccess + "'");

        return returnValue;
    }

    @Override
    public void setValidSuccessStatus()
    {
        validSuccess.add(DNDeliveryMode.AVAILABLE_FIRST_SUCCESS_PART);
        validSuccess.add(DNDeliveryMode.AVAILABLE_LAST_SUCCESS_PART);
        validSuccess.add(DNDeliveryMode.EARLIEST_SUCCESS_DELIVERED);
        validSuccess.add(DNDeliveryMode.LATEST_SUCCESS_DELIVERED);
    }

    @Override
    public void setValidFailureStatus()
    {
        validFailure.add(DNDeliveryMode.FIRST_PART);
        validFailure.add(DNDeliveryMode.LAST_PART);
        validFailure.add(DNDeliveryMode.EARLIEST_DELIVERED);
        validFailure.add(DNDeliveryMode.LATEST_DELIVERED);
    }

}