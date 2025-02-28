package com.itextos.beacon.commonlib.messageprocessor.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.constants.InterfaceGroup;
import com.itextos.beacon.commonlib.constants.MessagePriority;
import com.itextos.beacon.commonlib.constants.MessageType;
import com.itextos.beacon.commonlib.utility.CommonUtility;

public class StartupRuntimeArguments
{

    private static final Log            log                       = LogFactory.getLog(StartupRuntimeArguments.class);

    private static final String         RUNTIME_ARG_PF_CLUSTER    = "pf.cluster";
    private static final String         RUNTIME_ARG_PF_INTF_GROUP = "intf.group";
    private static final String         RUNTIME_ARG_MSG_TYPE      = "msg.type";
    private static final String         RUNTIME_ARG_MSG_PRIORITY  = "msg.pri";
    private static final String         RUNTIME_ARG_CLIENT_IDS    = "cli.ids";

    private static final String         STRING_PF_CLUSTER         = "Platform Cluster";
    private static final String         STRING_INTF_GROUP         = "Interface Group";
    private static final String         STRING_MSG_TYPE           = "IMessage Type";
    private static final String         STRING_MSG_PRIORITY       = "IMessage Priority";
    private static final String         STRING_CLIENT_IDS         = "Client Ids";

    private final List<ClusterType>     mPlatformCluster;
    private final List<InterfaceGroup>  mInterfaceGroup;
    private final MessageType           mMessageType;
    private final List<MessagePriority> mMessagePriority;
    private final List<String>          mClientIds;

    private final boolean               isClusterSpecific;
    private final boolean               isClientSpecific;
    private final boolean               isPrioritySpecific;

    public StartupRuntimeArguments()
    {
        mPlatformCluster   = readCluster();
        mInterfaceGroup    = readIntfGroup();
        mMessageType       = readMessageType();
        mMessagePriority   = readMessagePriority();
        mClientIds         = readClientIds();

        isClusterSpecific  = !mPlatformCluster.isEmpty();
        isClientSpecific   = (!mClientIds.isEmpty());
        isPrioritySpecific = (!mInterfaceGroup.isEmpty()) || (mMessageType != null) || !mMessagePriority.isEmpty();
    }

    public boolean isClusterSpecific()
    {
        return isClusterSpecific;
    }

    public boolean isClientSpecific()
    {
        return isClientSpecific;
    }

    public boolean isPrioritySpecific()
    {
        return isPrioritySpecific;
    }

    public List<ClusterType> getPlatformCluster()
    {
        return mPlatformCluster;
    }

    public List<InterfaceGroup> getInterfaceGroup()
    {
        return mInterfaceGroup;
    }

    public MessageType getMessageType()
    {
        return mMessageType;
    }

    public List<MessagePriority> getMessagePriority()
    {
        return mMessagePriority;
    }

    public List<String> getClientIds()
    {
        return mClientIds;
    }

    private static List<String> readClientIds()
    {
        final String       clientIds   = readProperty(RUNTIME_ARG_CLIENT_IDS, STRING_CLIENT_IDS);
        final List<String> returnValue = new ArrayList<>();

        if (!clientIds.isBlank())
        {
            final String[] cliArray = clientIds.split(",");
            Collections.addAll(returnValue, cliArray);
        }
        return returnValue;
    }

    private static List<MessagePriority> readMessagePriority()
    {
        final String                priorities  = readProperty(RUNTIME_ARG_MSG_PRIORITY, STRING_MSG_PRIORITY);
        final List<MessagePriority> returnValue = new ArrayList<>();

        if (!priorities.isBlank())
        {
            final String[] priArray = priorities.split(",");

            for (final String p : priArray)
            {
                final MessagePriority mp = MessagePriority.getMessagePriority(p);
                if (mp != null)
                    returnValue.add(mp);
                else
                    printNullLog(STRING_MSG_PRIORITY, p);
            }
        }
        return returnValue;
    }

    private static MessageType readMessageType()
    {
        final String mt = readProperty(RUNTIME_ARG_MSG_TYPE, STRING_MSG_TYPE);

        if (!mt.isEmpty())
        {
            final MessageType lMessageType = MessageType.getMessageType(mt);
            if (lMessageType == null)
                printNullLog(STRING_MSG_TYPE, mt);
            else
                return lMessageType;
        }
        return null;
    }

    private static List<InterfaceGroup> readIntfGroup()
    {
        final String               intfGroup   = readProperty(RUNTIME_ARG_PF_INTF_GROUP, STRING_INTF_GROUP);
        final List<InterfaceGroup> returnValue = new ArrayList<>();

        if (!intfGroup.isEmpty())
        {
            final String[] lSplit = intfGroup.split(",");

            for (final String key : lSplit)
            {
                final InterfaceGroup lType = InterfaceGroup.getType(key);
                if (lType == null)
                    printNullLog(STRING_INTF_GROUP, intfGroup);
                else
                    returnValue.add(lType);
            }
        }
        return returnValue;
    }

    public ClusterType getCluster() {
    	
    	return ClusterType.getCluster(System.getenv("cluster"));
    }
    
    private static List<ClusterType> readCluster()
    {
        final String            pfCluster   = readProperty(RUNTIME_ARG_PF_CLUSTER, STRING_PF_CLUSTER);
        final List<ClusterType> returnValue = new ArrayList<>();

        if (!pfCluster.isEmpty())
        {
            final String[] lSplit = pfCluster.split(",");

            for (final String key : lSplit)
            {
                final ClusterType lCluster = ClusterType.getCluster(key);
                if (lCluster == null)
                    printNullLog(STRING_PF_CLUSTER, key);
                else
                    returnValue.add(lCluster);
            }
        }
        return returnValue;
    }

    private static String readProperty(
            String aRuntimeArg,
            String aTypeString)
    {
        final String lProperty = System.getProperty(aRuntimeArg);
  
        if (log.isInfoEnabled())
            log.info("Startup argument for '" + aTypeString + "' is : '" + lProperty + "'");
   
        if(RUNTIME_ARG_PF_CLUSTER.equals(aRuntimeArg)&&(lProperty==null||lProperty.trim().length()<1)) {
        	
            return CommonUtility.nullCheck(System.getenv("cluster"), true);

        }
        if (log.isInfoEnabled())
            log.info("Startup argument for '" + aTypeString + "' is : '" + lProperty + "'");
        return CommonUtility.nullCheck(lProperty, true);
    }

    private static void printNullLog(
            String aString,
            String aValue)
    {
        log.error("Unable to find the " + aString + "for '" + aValue + "'");
    }

    @Override
    public String toString()
    {
        return "StartupRuntimeArguments [mPlatformCluster=" + mPlatformCluster + ", mInterfaceGroup=" + mInterfaceGroup + ", mMessageType=" + mMessageType + ", mMessagePriority=" + mMessagePriority
                + ", mClientIds=" + mClientIds + "]";
    }

}