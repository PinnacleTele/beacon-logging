package com.itextos.beacon.platform.templatefinder.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.itextos.beacon.platform.templatefinder.utility.DltTemplateProperties;

class RecentlyUsedTemplates
        implements
        Serializable
{

    private static final long           serialVersionUID          = 573576269293998643L;

    private final BlockingQueue<String> mRecentlyTemplateIdsQueue = new LinkedBlockingQueue<>();

    synchronized void addRecentlyUsedTemplate(
            String aTemplateId)
    {
        if (mRecentlyTemplateIdsQueue.contains(aTemplateId))
            mRecentlyTemplateIdsQueue.remove(aTemplateId);

        if (mRecentlyTemplateIdsQueue.size() >= DltTemplateProperties.getInstance().getRecentlyUsedTemplateIdCount())
            mRecentlyTemplateIdsQueue.poll();

        mRecentlyTemplateIdsQueue.offer(aTemplateId);
    }

    /**
     * Always process from to use the most recent template.
     *
     * @return
     */
    List<String> getRecentlyUsedTemplateIds()
    {
        return new ArrayList<>(mRecentlyTemplateIdsQueue);
    }

    void clear()
    {
        mRecentlyTemplateIdsQueue.clear();
    }

    @Override
    public String toString()
    {
        return "RecentlyUsedTemplates [mRecentlyTemplateIds=" + mRecentlyTemplateIdsQueue.size() + "]";
    }

}