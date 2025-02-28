package com.itextos.beacon.platform.singledn;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.exception.ItextosException;
import com.itextos.beacon.inmemory.customfeatures.pojo.DNDeliveryMode;
import com.itextos.beacon.inmemory.customfeatures.pojo.DlrTypeInfo;
import com.itextos.beacon.platform.singledn.data.SingleDnComparator;
import com.itextos.beacon.platform.singledn.data.SingleDnInfo;
import com.itextos.beacon.platform.singledn.data.SingleDnRequest;
import com.itextos.beacon.platform.singledn.enums.DnStatus;
import com.itextos.beacon.platform.singledn.enums.ValidationStatus;

public abstract class AbstractSingleDnProcess
        implements
        ISingleDnProcess
{

    private static final Log                   log                  = LogFactory.getLog(AbstractSingleDnProcess.class);
    private static final Integer               FIRST_PART           = 1;

    protected final List<DNDeliveryMode>       validSuccess         = new ArrayList<>();
    protected final List<DNDeliveryMode>       validFailure         = new ArrayList<>();
    protected final Map<Integer, SingleDnInfo> singleDnMap          = new TreeMap<>();
    protected SingleDnRequest                  mSingleDnRequest;
    protected DlrTypeInfo                      mDlrTypeInfo;
    protected int                              mTotalNumberOfParts  = -1;
    protected ValidationStatus                 mIsValidationSuccess = ValidationStatus.IN_COMPLETE;

    protected AbstractSingleDnProcess(
            SingleDnRequest aSingleDnRequest,
            DlrTypeInfo aDlrTypeInfo)
            throws ItextosException
    {
        mSingleDnRequest = aSingleDnRequest;
        mDlrTypeInfo     = aDlrTypeInfo;

        setValidSuccessStatus();
        setValidFailureStatus();

        validateExpectedResult();
    }

    protected abstract void setValidSuccessStatus();

    protected abstract void setValidFailureStatus();

    protected boolean addSingleDnInfoLocal(
            SingleDnInfo aSingleDnInfo)
            throws ItextosException
    {
        if (aSingleDnInfo == null)
            throw new ItextosException("Single DN info cannot be null.");

        if (mTotalNumberOfParts == -1)
            mTotalNumberOfParts = aSingleDnInfo.getTotalPartNos();
        else
            if (mTotalNumberOfParts != aSingleDnInfo.getTotalPartNos())
                throw new ItextosException("Mismatch Total number received. Exisiting total parts : '" + mTotalNumberOfParts + "'. Received Dn : '" + aSingleDnInfo + "'");

        singleDnMap.put(aSingleDnInfo.getPartNo(), aSingleDnInfo);
        return true;
    }

    @Override
    public SingleDnInfo getResult()
    {
        final DNDeliveryMode resultType  = mIsValidationSuccess == ValidationStatus.SUCCESS ? mDlrTypeInfo.getDnHandoverMode() : mDlrTypeInfo.getAltHandoverMode();

        SingleDnInfo         returnValue = null;

        switch (resultType)
        {
            case AVAILABLE_FIRST_FAILURE_PART:
                returnValue = getAvailableFirstFailurePart();
                break;

            case AVAILABLE_FIRST_SUCCESS_PART:
                returnValue = getAvailableFirstSuccessPart();
                break;

            case AVAILABLE_LAST_FAILURE_PART:
                returnValue = getAvailableLastFailurePart();
                break;

            case AVAILABLE_LAST_SUCCESS_PART:
                returnValue = getAvailableLastSuccessPart();
                break;

            case EARLIEST_FAILURE_DELIVERED:
                returnValue = getEarliestFailureReceived();
                break;

            case EARLIEST_DELIVERED:
                returnValue = getEarliestReceived();
                break;

            case EARLIEST_SUCCESS_DELIVERED:
                returnValue = getEarliestSuccessReceived();
                break;

            case FIRST_PART:
                returnValue = getFirstPart();
                break;

            case LAST_PART:
                returnValue = getLastPart();
                break;

            case LATEST_FAILURE_DELIVERED:
                returnValue = getLatestFailureReceived();
                break;

            case LATEST_DELIVERED:
                returnValue = getLatestReceived();
                break;

            case LATEST_SUCCESS_DELIVERED:
                returnValue = getLatestSuccessReceived();
                break;

            default:
                break;
        }
        return returnValue;
    }

    private void validateExpectedResult()
            throws ItextosException
    {
        validate(validSuccess, mDlrTypeInfo.getDnHandoverMode());
        validate(validFailure, mDlrTypeInfo.getAltHandoverMode());
    }

    private void validate(
            List<DNDeliveryMode> aValidList,
            DNDeliveryMode aExpectedResult)
            throws ItextosException
    {

        if (!aValidList.contains(aExpectedResult))
        {
            final String s = "For Client id '" + mSingleDnRequest.getClientId() + "' Single Dn Process Type '" + mDlrTypeInfo.getSingleDnProcessType() + "' Expected result '" + aExpectedResult
                    + "' cannot be one of the possible expected Results. Expected Results : " + aValidList;
            log.error(s);
            throw new ItextosException(s);
        }
    }

    private SingleDnInfo getFirstPart()
    {
        if (log.isDebugEnabled())
            log.debug("Single DN Map Size '" + singleDnMap.size() + "'");

        return singleDnMap.get(FIRST_PART);
    }

    private SingleDnInfo getLastPart()
    {
        if (log.isDebugEnabled())
            log.debug("Single DN Map Size '" + singleDnMap.size() + "'");

        return singleDnMap.get(mTotalNumberOfParts);
    }

    private SingleDnInfo getAvailableFirstSuccessPart()
    {
        SingleDnInfo returnValue = null;

        if (!singleDnMap.isEmpty())
        {
            final List<SingleDnInfo> list = getFilteredList(true);
            if (!list.isEmpty())
                returnValue = list.get(0);
        }

        if (log.isDebugEnabled())
            log.debug("Single DN Map Size '" + singleDnMap.size() + "' Returning '" + returnValue + "'");

        return returnValue;
    }

    private SingleDnInfo getAvailableLastSuccessPart()
    {
        SingleDnInfo returnValue = null;

        if (!singleDnMap.isEmpty())
        {
            final List<SingleDnInfo> list = getFilteredList(true);
            if (!list.isEmpty())
                returnValue = list.get(list.size() - 1);
        }

        if (log.isDebugEnabled())
            log.debug("Single DN Map Size '" + singleDnMap.size() + "' Returning '" + returnValue + "'");

        return returnValue;
    }

    private SingleDnInfo getAvailableFirstFailurePart()
    {
        SingleDnInfo returnValue = null;

        if (!singleDnMap.isEmpty())
        {
            final List<SingleDnInfo> list = getFilteredList(false);
            if (!list.isEmpty())
                returnValue = list.get(0);
        }

        if (log.isDebugEnabled())
            log.debug("Single DN Map Size '" + singleDnMap.size() + "' Returning '" + returnValue + "'");

        return returnValue;
    }

    private SingleDnInfo getAvailableLastFailurePart()
    {
        SingleDnInfo returnValue = null;

        if (!singleDnMap.isEmpty())
        {
            final List<SingleDnInfo> list = getFilteredList(false);
            if (!list.isEmpty())
                returnValue = list.get(list.size() - 1);
        }

        if (log.isDebugEnabled())
            log.debug("Single DN Map Size '" + singleDnMap.size() + "' Returning '" + returnValue + "'");

        return returnValue;
    }

    private SingleDnInfo getEarliestReceived()
    {
        SingleDnInfo returnValue = null;

        if (!singleDnMap.isEmpty())
        {
            final List<SingleDnInfo> list = getSortedList(getFullList());
            if (!list.isEmpty())
                returnValue = list.get(0);
        }

        if (log.isDebugEnabled())
            log.debug("Single DN Map Size '" + singleDnMap.size() + "' Returning '" + returnValue + "'");

        return returnValue;
    }

    private SingleDnInfo getLatestReceived()
    {
        SingleDnInfo returnValue = null;

        if (!singleDnMap.isEmpty())
        {
            final List<SingleDnInfo> list = getSortedList(getFullList());
            if (!list.isEmpty())
                returnValue = list.get(list.size() - 1);
        }

        if (log.isDebugEnabled())
            log.debug("Single DN Map Size '" + singleDnMap.size() + "' Returning '" + returnValue + "'");

        return returnValue;
    }

    private SingleDnInfo getEarliestSuccessReceived()
    {
        SingleDnInfo returnValue = null;

        if (!singleDnMap.isEmpty())
        {
            final List<SingleDnInfo> list = getSortedList(getFilteredList(true));
            if (!list.isEmpty())
                returnValue = list.get(0);
        }

        if (log.isDebugEnabled())
            log.debug("Single DN Map Size '" + singleDnMap.size() + "' Returning '" + returnValue + "'");

        return returnValue;
    }

    private SingleDnInfo getLatestSuccessReceived()
    {
        SingleDnInfo returnValue = null;

        if (!singleDnMap.isEmpty())
        {
            final List<SingleDnInfo> list = getSortedList(getFilteredList(true));
            if (!list.isEmpty())
                returnValue = list.get(list.size() - 1);
        }

        if (log.isDebugEnabled())
            log.debug("Single DN Map Size '" + singleDnMap.size() + "' Returning '" + returnValue + "'");

        return returnValue;
    }

    private SingleDnInfo getEarliestFailureReceived()
    {
        SingleDnInfo returnValue = null;

        if (!singleDnMap.isEmpty())
        {
            final List<SingleDnInfo> list = getSortedList(getFilteredList(false));
            if (!list.isEmpty())
                returnValue = list.get(0);
        }

        if (log.isDebugEnabled())
            log.debug("Single DN Map Size '" + singleDnMap.size() + "' Returning '" + returnValue + "'");

        return returnValue;
    }

    private SingleDnInfo getLatestFailureReceived()
    {
        SingleDnInfo returnValue = null;

        if (!singleDnMap.isEmpty())
        {
            final List<SingleDnInfo> list = getSortedList(getFilteredList(false));
            if (!list.isEmpty())
                returnValue = list.get(list.size() - 1);
        }

        if (log.isDebugEnabled())
            log.debug("Single DN Map Size '" + singleDnMap.size() + "' Returning '" + returnValue + "'");

        return returnValue;
    }

    private Integer getFirst()
    {
        return getKey(true);
    }

    private Integer getLast()
    {
        return getKey(false);
    }

    private Integer getKey(
            boolean aIsFirst)
    {
        Integer returnValue = -1;

        if (log.isDebugEnabled())
            log.debug("Single DN Map Size '" + singleDnMap.size() + "' Is First " + aIsFirst + "'");

        final Set<Integer> lKeySet = singleDnMap.keySet();

        if (!lKeySet.isEmpty())
        {
            final Object[] lArray = lKeySet.toArray();
            returnValue = (Integer) (aIsFirst ? lArray[0] : lArray[lArray.length - 1]);
        }

        if (log.isDebugEnabled())
            log.debug("Single DN Map Size '" + singleDnMap.size() + "' Is First " + aIsFirst + "' Return Index '" + returnValue + "'");

        return returnValue;
    }

    private static List<SingleDnInfo> getSortedList(
            List<SingleDnInfo> aList)
    {
        Collections.sort(aList, new SingleDnComparator());

        return aList;
    }

    List<SingleDnInfo> getFullList()
    {
        final List<SingleDnInfo> list = new ArrayList<>();
        for (final SingleDnInfo sdi : singleDnMap.values())
            list.add(sdi);

        if (log.isDebugEnabled())
            log.debug("Single DN Map Size '" + singleDnMap.size() + "' List Size '" + list.size() + "'");

        return list;
    }

    List<SingleDnInfo> getFilteredList(
            boolean aIsSuccess)
    {
        if (log.isDebugEnabled())
            log.debug("Single DN Map Size '" + singleDnMap.size() + "'");

        final List<SingleDnInfo> list = new ArrayList<>();

        for (final SingleDnInfo sdi : singleDnMap.values())
        {
            if (log.isDebugEnabled())
                log.debug("Single DN :'" + sdi + "'");

            if (aIsSuccess)
            {
                if (sdi.getDnStatus() == DnStatus.SUCCESS)
                    list.add(sdi);
            }
            else
                if (sdi.getDnStatus() == DnStatus.FAILURE)
                    list.add(sdi);
        }
        if (log.isDebugEnabled())
            log.debug("Single DN Map Size '" + singleDnMap.size() + "' return List size " + list.size());

        return list;
    }

    public boolean isAllSuccess()
    {
        return isAllSuccess(true);
    }

    public boolean isAllFailed()
    {
        return isAllSuccess(false);
    }

    private boolean isAllSuccess(
            boolean aCheckForSuccess)
    {
        return getFilteredList(aCheckForSuccess).size() == mTotalNumberOfParts;
    }

}