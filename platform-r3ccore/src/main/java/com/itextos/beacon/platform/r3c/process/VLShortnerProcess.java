package com.itextos.beacon.platform.r3c.process;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.message.MessageRequest;
import com.itextos.beacon.platform.r3c.util.R3CUtil;
import com.itextos.beacon.platform.r3c.util.VLRepository;

public class VLShortnerProcess
{

    private static final Log log = LogFactory.getLog(VLShortnerProcess.class);

    private VLShortnerProcess()
    {}

    public static String doVLShortner(
            MessageRequest aMessageRequest,
            String aUrlToConvert)
    {

        try
        {
            final String lClientId    = aMessageRequest.getClientId();
            final int    lVLShortner  = aMessageRequest.getVlShortner();

            final String lSmartLinkId = ShortnerCache.getVLSmartlinkId(lClientId, aUrlToConvert, lVLShortner);

            if (log.isDebugEnabled())
                log.debug("SmartLink Id  : '" + lSmartLinkId + "'");

            if (lSmartLinkId != null)
            {
                final VLRepository lRepository = R3CUtil.createVlRepoObject(aMessageRequest, aUrlToConvert, lSmartLinkId, null, false);
                if (log.isDebugEnabled())
                    log.debug("VLRepository : " + lRepository);

                if ((lRepository != null))
                {
                    if (log.isDebugEnabled())
                        log.debug("Available Shorten Url : " + lRepository.getShortenUrl());

                    if (!lRepository.isIsRedirectUrlForShortner())
                    {
                        if (!aMessageRequest.isUrlShortned())
                            aMessageRequest.setUrlShortned(true);

                        R3CUtil.addSmartLinkId(aMessageRequest, lSmartLinkId);
                    }

                    return lRepository.getShortenUrl();
                }
            }
        }
        catch (final Exception e)
        {
            log.error("Exception occer while getting smartlinkIds...", e);
        }

        return null;
    }

}
