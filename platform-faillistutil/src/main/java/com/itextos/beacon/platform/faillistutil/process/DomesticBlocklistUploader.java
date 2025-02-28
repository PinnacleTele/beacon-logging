package com.itextos.beacon.platform.faillistutil.process;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.platform.faillistutil.util.FaillistPropertyLoader;

/**
 * A class to have the Domestic specific data loading.
 */
public class DomesticBlocklistUploader
        extends
        AbstractBlocklistUploader
{

    private static final Log log = LogFactory.getLog(DomesticBlocklistUploader.class);

    public DomesticBlocklistUploader()
    {
        super(FaillistPropertyLoader.getInstance().getDomesticConfig());

        if (log.isDebugEnabled())
            log.debug("Starting process for Domestic");
    }

    @Override
    public String getProcessType()
    {
        return "Domestic Process";
    }

}