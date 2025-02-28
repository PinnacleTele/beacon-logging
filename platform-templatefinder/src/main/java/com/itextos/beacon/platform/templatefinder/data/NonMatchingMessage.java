package com.itextos.beacon.platform.templatefinder.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.platform.templatefinder.utility.DltTemplateProperties;

class NonMatchingMessage
        implements
        Serializable
{

    private static final long           serialVersionUID          = -3713052306306153177L;

    private static final Log            log                       = LogFactory.getLog(NonMatchingMessage.class);

    private final BlockingQueue<String> mNonMatchingTemplateQueue = new LinkedBlockingQueue<>();

    void clear()
    {
        mNonMatchingTemplateQueue.clear();
    }

    String addNonMatchingTemplate(
            String aNewMessage)
    {

        if (mNonMatchingTemplateQueue.contains(aNewMessage))
        {
            if (log.isDebugEnabled())
                log.debug("'" + aNewMessage + "' is already in the non matching list.");
            return null;
        }

        mNonMatchingTemplateQueue.add(aNewMessage);

        if (mNonMatchingTemplateQueue.size() >= DltTemplateProperties.getInstance().getNonMatchingMessageCreateTemplateMaxCount())
        {
            final TemplateFramer tf       = new TemplateFramer(new ArrayList<>(mNonMatchingTemplateQueue));
            final boolean        isFramed = tf.frameTemplate();

            if (!isFramed)
                mNonMatchingTemplateQueue.poll();
            else
            {
                mNonMatchingTemplateQueue.clear();
                final String newTemplate = tf.getFramedTemplate();
                if (log.isDebugEnabled())
                    log.debug("New Framed Template : '" + newTemplate + "'");
            }

            return tf.getFramedTemplate();
        }
        return null;
    }

    boolean isAvailable(
            String aMessage)
    {
        return mNonMatchingTemplateQueue.contains(aMessage);
    }

    @Override
    public String toString()
    {
        return "NonMatchingMessage [mNonMatchingTemplateQueue=" + mNonMatchingTemplateQueue.size() + "]";
    }

}