package com.itextos.beacon.platform.faillistutil.process;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.platform.faillistutil.util.FaillistPropertyLoader;

/**
 * A class to have the International specific data loading.
 */
public class InternationalFaillistUploader
        extends
        AbstractBlocklistUploader
{

    private static final Log log = LogFactory.getLog(InternationalFaillistUploader.class);

    public InternationalFaillistUploader()
    {
        super(FaillistPropertyLoader.getInstance().getInternationConfig());

        if (log.isDebugEnabled())
            log.debug("Starting process for International");
    }

    @Override
    public String getProcessType()
    {
        return "International Process";
    }

}
