package com.itextos.beacon.commonlib.commondbpool.tracker;

import java.util.ArrayList;

public class StackTraceCollection
{

    private final ArrayList<CallerInfo> m_StacktraceInfo;

    public StackTraceCollection(
            Thread T)
    {
        m_StacktraceInfo = new ArrayList<>();
        populate(T);
    }

    private void populate(
            Thread t)
    {

        if (t != null)
        {
            final StackTraceElement[] ste = t.getStackTrace();

            if (ste != null)
                for (final StackTraceElement st : ste)
                    if (!ConnectionsTracker.IGNORE_CLASS_LIST.contains(st.getClassName()))
                        add(new CallerInfo(st.getClassName(), st.getMethodName(), st.getLineNumber()));
        }
    }

    public void add(
            CallerInfo aCallerInfo)
    {
        m_StacktraceInfo.add(aCallerInfo);
    }

    public ArrayList<CallerInfo> getCallerInfoCollection()
    {
        return m_StacktraceInfo;
    }

    public String getXML()
    {
        final StringBuffer sb = new StringBuffer();

        try
        {
            for (final CallerInfo ci : m_StacktraceInfo)
                sb.append(ci.getXML());
        }
        catch (final Exception e)
        {
            e.printStackTrace();
        }
        return sb.toString();
    }

    @Override
    public String toString()
    {
        return m_StacktraceInfo.toString();
    }

}