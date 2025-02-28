package com.itextos.beacon.platform.ic.util;

import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.PlatformStatusCode;
import com.itextos.beacon.commonlib.message.MessageRequest;
import com.itextos.beacon.commonlib.utility.ItextosClient;
import com.itextos.beacon.inmemory.blocklistnumbers.BlockListNumberCheck;
import com.itextos.beacon.inmemory.blocklistnumbers.ClientBlockListNumberCheck;
import com.itextos.beacon.inmemory.loader.InmemoryLoaderCollection;
import com.itextos.beacon.inmemory.loader.process.InmemoryId;

public class MobileBlockListCheck
{

    private static final Log log = LogFactory.getLog(MobileBlockListCheck.class);

    private MobileBlockListCheck()
    {}

    public static boolean validateMobileBlockList(
            MessageRequest aMessageRequest)
    {
        final int lBlockListCategoryValue = aMessageRequest.getBlacklistCheck();

        if (log.isDebugEnabled())
            log.debug(aMessageRequest.getBaseMessageId()+" : BlockList Enabled :" + lBlockListCategoryValue);

        final BlockListCategory lBlockListCategory = BlockListCategory.getBlockListCategory(String.valueOf(lBlockListCategoryValue));

        if (log.isDebugEnabled())
            log.debug(aMessageRequest.getBaseMessageId()+" : BlockList Category :" + lBlockListCategory);

        final ItextosClient lTextosClient = new ItextosClient(aMessageRequest.getClientId());
        final String        lMobileNo     = aMessageRequest.getMobileNumber();

        boolean             lStatus       = false;

        switch (lBlockListCategory)
        {
            case GLOBAL:
                lStatus = validateGlobal(aMessageRequest);
                break;

            case NONE:
                break;

            case USER:
                lStatus = validateUserOnly(lTextosClient.getClientId(), lMobileNo);
                if (lStatus)
                    rejectMobileBlockList(aMessageRequest, PlatformStatusCode.CUSTOM_MOBILE_NUMBER_BLACK_FAILED);
                break;

            case USER_ADMIN_SUPERADMIN:
                lStatus = validateUserAndAdminAndSuperAdmin(lTextosClient, lMobileNo);
                if (lStatus)
                    rejectMobileBlockList(aMessageRequest, PlatformStatusCode.CUSTOM_MOBILE_NUMBER_BLACK_FAILED);
                break;

            case USER_ADMIN_SUPERADMIN_GLOBAL:
                lStatus = validateUserAndAdminAndSuperAdminAndGlobal(aMessageRequest, lTextosClient, lMobileNo);
                break;

            case USER_GLOBAL:
                lStatus = validateUserAndGlobal(aMessageRequest, lTextosClient, lMobileNo);
                break;

            case USER_PARENT_GRANDPARENT:
                lStatus = validateUserAndParentAndGrandParent(aMessageRequest, lMobileNo);
                break;

            case USER_PARENT_GRANDPARENT_GLOBAL:
                lStatus = validateUserAndParentAndGrandParentAndGlobal(aMessageRequest, lMobileNo);
                break;

            default:
                break;
        }

        if (log.isDebugEnabled())
            log.debug(aMessageRequest.getBaseMessageId()+ " : Mobile BlockList Status for Category :'" + lBlockListCategory + "', and Mobile :'" + lMobileNo + "', Status:'" + lStatus + "'");

        return lStatus;
    }

    private static boolean validateUserAndGlobal(
            MessageRequest aMessageRequest,
            ItextosClient aClient,
            String aDest)
    {
        if (!validateUserOnly(aClient.getClientId(), aDest))
            return validateGlobal(aMessageRequest);

        rejectMobileBlockList(aMessageRequest, PlatformStatusCode.CUSTOM_MOBILE_NUMBER_BLACK_FAILED);

        return true;
    }

    private static boolean validateUserAndParentAndGrandParent(
            MessageRequest aMessageRequest,
            String aDest)
    {
        boolean lStatus = validateUserOnly(aMessageRequest.getClientId(), aDest);
        if (!lStatus)
            lStatus = getParentAndGrandParentOnly(aMessageRequest, aDest);

        if (lStatus)
            rejectMobileBlockList(aMessageRequest, PlatformStatusCode.CUSTOM_MOBILE_NUMBER_BLACK_FAILED);

        return lStatus;
    }

    private static boolean validateUserAndParentAndGrandParentAndGlobal(
            MessageRequest aMessageRequest,
            String aDest)
    {
        if (!validateUserAndParentAndGrandParent(aMessageRequest, aDest))
            return validateGlobal(aMessageRequest);

        rejectMobileBlockList(aMessageRequest, PlatformStatusCode.CUSTOM_MOBILE_NUMBER_BLACK_FAILED);

        return true;
    }

    private static boolean validateUserAndAdminAndSuperAdminAndGlobal(
            MessageRequest aMessageRequest,
            ItextosClient aClient,
            String aDest)
    {
        if (!getAllClientsLevelsOnly(aClient, aDest))
            return validateGlobal(aMessageRequest);

        rejectMobileBlockList(aMessageRequest, PlatformStatusCode.CUSTOM_MOBILE_NUMBER_BLACK_FAILED);

        return true;
    }

    private static boolean validateUserAndAdminAndSuperAdmin(
            ItextosClient aClient,
            String aDest)
    {
        return getAllClientsLevelsOnly(aClient, aDest);
    }

    private static boolean validateUserOnly(
            String aClientId,
            String aDest)
    {
        final Map<String, Set<String>> lMap = getCustomBlackListMobileNumber().get(aClientId);

        if (log.isDebugEnabled())
            log.debug("Client BlockList Numbers :" + lMap);

        if (lMap == null)
            return false;

        return isMobileBlocketed(lMap, aDest);
    }

    private static boolean validateGlobal(
            MessageRequest aMessageRequest)
    {
        final boolean isStatus = isGlobalBlacklistMobileNumber(aMessageRequest);

        if (isStatus)
            rejectMobileBlockList(aMessageRequest, PlatformStatusCode.GLOBAL_MOBILE_NUMBER_BLOCK_FAILED);

        return isStatus;
    }

    private static boolean isMobileBlocketed(
            Map<String, Set<String>> aMap,
            String aDest)
    {
        final Set<String> lSet = aMap.get(aDest.substring(0, 5));

        if (log.isDebugEnabled())
            log.debug("Mobile Set list :" + lSet);

        if (lSet != null)
            return lSet.contains(aDest);

        return false;
    }

    private static boolean getAllClientsLevelsOnly(
            ItextosClient aClient,
            String aDest)
    {
        boolean                  lStatus = false;
        Map<String, Set<String>> lMap    = getCustomBlackListMobileNumber().get(aClient.getClientId());

        if (lMap != null)
            lStatus = isMobileBlocketed(lMap, aDest);

        if (!lStatus)
        {
            lMap = getCustomBlackListMobileNumber().get(aClient.getAdmin());

            if (lMap != null)
                lStatus = isMobileBlocketed(lMap, aDest);
        }

        if (!lStatus)
        {
            lMap = getCustomBlackListMobileNumber().get(aClient.getSuperAdmin());

            if (lMap == null)
                lStatus = false;
            else
                lStatus = isMobileBlocketed(lMap, aDest);
        }

        return lStatus;
    }

    private static boolean getParentAndGrandParentOnly(
            MessageRequest aMessageRequest,
            String aDest)
    {
        boolean lStatus = false;

        if (log.isDebugEnabled())
            log.debug("Parent User Id: " + aMessageRequest.getParentUserId());

        Map<String, Set<String>> lMap = getCustomBlackListMobileNumber().get(aMessageRequest.getParentUserId());

        if (log.isDebugEnabled())
            log.debug("Parent User Info :" + lMap);

        if (lMap != null)
            lStatus = isMobileBlocketed(lMap, aDest);

        if (!lStatus)
        {
            if (log.isDebugEnabled())
                log.debug("Parent User Id: " + aMessageRequest.getSuperUserId());

            lMap = getCustomBlackListMobileNumber().get(aMessageRequest.getSuperUserId());

            if (log.isDebugEnabled())
                log.debug("Grand Parent User Info :" + lMap);

            if (lMap == null)
                lStatus = false;
            else
                lStatus = isMobileBlocketed(lMap, aDest);
        }

        return lStatus;
    }

    private static void rejectMobileBlockList(
            MessageRequest aMessageRequest,
            PlatformStatusCode aPlatformStatusCode)
    {
        aMessageRequest.setPlatfromRejected(true);
        aMessageRequest.setSubOriginalStatusCode(aPlatformStatusCode.getStatusCode());
    }

    private static boolean isGlobalBlacklistMobileNumber(
            MessageRequest aMessageRequest)
    {
        if (log.isDebugEnabled())
            log.debug("Global Blacklist Mobile Number Check Message Id : " + aMessageRequest.getBaseMessageId());

        final BlockListNumberCheck lNumberBlockList = (BlockListNumberCheck) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.BLOCK_LIST_NUMBERS);
        return lNumberBlockList.isMobileListedInGlobalBlock(aMessageRequest.getMobileNumber());
    }

    private static Map<String, Map<String, Set<String>>> getCustomBlackListMobileNumber()
    {
        final ClientBlockListNumberCheck lClientBlockListNumberCheck = (ClientBlockListNumberCheck) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.CLIENT_BLOCK_LIST_NUMBERS);
        return lClientBlockListNumberCheck.getClientBlockList();
    }

}
