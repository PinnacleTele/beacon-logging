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

public class SingleDnProcessorAnyFailure
        extends
        AbstractSingleDnProcess
{

    private static final Log log = LogFactory.getLog(SingleDnProcessorAnyFailure.class);

    public SingleDnProcessorAnyFailure(
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
                log.debug("Single DN Map size : '" + singleDnMap.size() + "' Total Parts : '" + mTotalNumberOfParts + "' Dn Status " + aSingleDnInfo.getDnStatus());

            if (aSingleDnInfo.getDnStatus() == DnStatus.FAILURE)
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
        validSuccess.add(DNDeliveryMode.EARLIEST_FAILURE_DELIVERED);
    }

    @Override
    public void setValidFailureStatus()
    {
        validFailure.add(DNDeliveryMode.AVAILABLE_FIRST_SUCCESS_PART);
        validFailure.add(DNDeliveryMode.AVAILABLE_LAST_SUCCESS_PART);
        validFailure.add(DNDeliveryMode.EARLIEST_SUCCESS_DELIVERED);
        validFailure.add(DNDeliveryMode.LATEST_SUCCESS_DELIVERED);
    }

}