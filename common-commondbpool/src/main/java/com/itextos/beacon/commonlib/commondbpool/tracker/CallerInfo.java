package com.itextos.beacon.commonlib.commondbpool.tracker;

public class CallerInfo
{

    public final String mClassName;
    public final String mMethodName;
    public final int    mLineNo;

    public CallerInfo(
            String aClassName,
            String aMethodName,
            int aLineNo)
    {
        this.mClassName  = aClassName;
        this.mMethodName = ConnectionsTracker.INIT.equals(aMethodName) ? ConnectionsTracker.INIT_CONSTRUCTOR : aMethodName;
        this.mLineNo     = aLineNo;
    }

    public String getClassName()
    {
        return mClassName;
    }

    public String getMethodName()
    {
        return mMethodName;
    }

    public int getLineNo()
    {
        return mLineNo;
    }

    public String getXML()
    {
        final StringBuilder sb = new StringBuilder();
        sb.append("<CallerInfo");
        sb.append(" ClassName=\'").append(mClassName).append("\'");
        sb.append(" MethodName=\'").append(mMethodName).append("\'");
        sb.append(" LineNo=\'").append(mLineNo).append("\' />");
        return sb.toString();
    }

    @Override
    public String toString()
    {
        return "CLASS:'" + mClassName + "', METHOD:'" + mMethodName + "' @ Line No : " + mLineNo;
    }

}