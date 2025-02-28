package com.itextos.beacon.commonlib.messageprocessor.data;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.constants.InterfaceGroup;
import com.itextos.beacon.commonlib.constants.ItextosEnum;
import com.itextos.beacon.commonlib.constants.MessagePriority;
import com.itextos.beacon.commonlib.constants.MessageType;
import com.itextos.beacon.commonlib.utility.CommonUtility;

public final class KafkaDataLoaderUtility
{

    private static final Log log = LogFactory.getLog(KafkaDataLoaderUtility.class);

    private KafkaDataLoaderUtility()
    {}

    public static String getKey(
            Component aComponent,
            ClusterType aPlatformClusterType)
    {
        return CommonUtility.combine(getKey(aComponent), getKey(aPlatformClusterType));
    }

    public static String getKey(
            InterfaceGroup aInterfaceGroup,
            MessageType aMessageType,
            MessagePriority aMessagePriority)
    {
        return CommonUtility.combine(getKey(aInterfaceGroup), getKey(aMessageType), getKey(aMessagePriority));
    }

    private static String getKey(
            ItextosEnum aType)
    {
        return aType == null ? "null" : aType.getKey();
    }

    public static void addToKeyList(
            StringJoiner aStringJoiner,
            ItextosEnum aEnum)
    {
        if (aEnum != null)
            aStringJoiner.add(aEnum.getKey());
        else
            aStringJoiner.add("null");
    }

    public static void addToTopic(
            StringJoiner aStringJoiner,
            ItextosEnum aEnum)
    {
        if (aEnum != null)
            aStringJoiner.add(aEnum.getKey());
    }

    public static List<ItextosEnum> getPriorities(
            String aMsgPriName)
    {
        final List<ItextosEnum> returnValue = new ArrayList<>();
        final String            priorities  = CommonUtility.nullCheck(aMsgPriName, true);

        if (!priorities.isEmpty())
        {
            final String[] lSplit = priorities.split(",");

            for (final String s : lSplit)
            {
                final MessagePriority lMessagePriority = MessagePriority.getMessagePriority(s);
                if (lMessagePriority == null)
                    log.error("Unable to find IMessage Priority for '" + s + "'");
                else
                    returnValue.add(lMessagePriority);
            }
        }
        return returnValue;
    }

    public static String updateTopicName(
            String aTopicName)
    {
        if (aTopicName != null)
            aTopicName = aTopicName.replace('_', '-');
        return aTopicName;
    }

    public static List<ItextosEnum> getClusters(
            String aPfClusterName)
    {
        final List<ItextosEnum> returnValue = new ArrayList<>();
        final String            clusters    = CommonUtility.nullCheck(aPfClusterName, true);

        if (!clusters.isEmpty())
        {
            final String[] lSplit = clusters.split(",");

            for (final String s : lSplit)
            {
                final ClusterType lClusterType = ClusterType.getCluster(s);
                if (lClusterType == null)
                    log.error("Unable to find Cluster for '" + s + "'");
                else
                    returnValue.add(lClusterType);
            }
        }
        return returnValue;
    }

    public static List<ItextosEnum> getInterfaceGroups(
            String aInterfaceGroups)
    {
        final List<ItextosEnum> returnValue     = new ArrayList<>();
        final String            interfaceGroups = CommonUtility.nullCheck(aInterfaceGroups, true);

        if (!interfaceGroups.isEmpty())
        {
            final String[] lSplit = interfaceGroups.split(",");

            for (final String s : lSplit)
            {
                final InterfaceGroup lInterfaceGroup = InterfaceGroup.getType(s);
                if (lInterfaceGroup == null)
                    log.error("Unable to find Cluster for '" + s + "'");
                else
                    returnValue.add(lInterfaceGroup);
            }
        }
        return returnValue;
    }

    public static List<String> getNames(
            List<ItextosEnum> aEnumList)
    {
        final List<String> returnValue = new ArrayList<>();

        if (aEnumList.isEmpty())
            returnValue.add(KafkaDBConstants.DEFAULT);
        else
            for (final ItextosEnum ne : aEnumList)
                returnValue.add(ne.getKey());

        return returnValue;
    }

    public static String getNameOrDefault(
            ItextosEnum aItextosEnum)
    {
    	if(aItextosEnum instanceof ClusterType ) {
    		
            return aItextosEnum == null ? ClusterType.BULK.getKey() : aItextosEnum.getKey();

    	}else {
    	
            return aItextosEnum == null ? KafkaDBConstants.DEFAULT : aItextosEnum.getKey();

    	}
    }

}
