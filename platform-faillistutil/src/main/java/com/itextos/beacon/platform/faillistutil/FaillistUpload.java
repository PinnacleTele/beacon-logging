package com.itextos.beacon.platform.faillistutil;

import com.itextos.beacon.platform.faillistutil.process.DomesticBlocklistUploader;
import com.itextos.beacon.platform.faillistutil.process.IFaillistUploader;
import com.itextos.beacon.platform.faillistutil.process.InternationalFaillistUploader;

/**
 * Class to load the file content into Redis.
 * <p>
 * This will load the international block list number files first then then
 * domestic block list number files.
 */
public class FaillistUpload
{

    /**
     * <code>main</code> method to load the data files into Redis.
     *
     * @param args
     */
    public static void main(
            String[] args)
    {
        final IFaillistUploader internationalDataloader = new InternationalFaillistUploader();
        internationalDataloader.process();

        final IFaillistUploader domesticDataloader = new DomesticBlocklistUploader();
        domesticDataloader.process();
    }

}