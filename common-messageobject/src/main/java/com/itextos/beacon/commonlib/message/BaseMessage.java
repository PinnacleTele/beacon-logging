package com.itextos.beacon.commonlib.message;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.constants.Constants;
import com.itextos.beacon.commonlib.constants.DateTimeFormat;
import com.itextos.beacon.commonlib.constants.InterfaceGroup;
import com.itextos.beacon.commonlib.constants.InterfaceType;
import com.itextos.beacon.commonlib.constants.ItextosEnum;
import com.itextos.beacon.commonlib.constants.MessagePriority;
import com.itextos.beacon.commonlib.constants.MessageType;
import com.itextos.beacon.commonlib.constants.MiddlewareConstant;
import com.itextos.beacon.commonlib.constants.RouteType;
import com.itextos.beacon.commonlib.constants.exception.ItextosRuntimeException;
import com.itextos.beacon.commonlib.message.utility.MessageUtil;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.DateTimeUtility;
import com.itextos.beacon.errorlog.RemoveLogBuffer;
import com.itextos.beacon.errorlog.SMSLog;

public abstract class BaseMessage
        extends
        AbstractMessage
{

    private static final long                    serialVersionUID       = 8952973312761219720L;

    private static final Log                     log                    = LogFactory.getLog(BaseMessage.class);
    private static final Set<MiddlewareConstant> IGNORE_CONSTANT        = new HashSet<>();
    private static final Set<String>             IGNORE_JSON_OR_MAPKEYS = new HashSet<>();
    static final String                          CHILDREN               = "children";

    static
    {
        IGNORE_CONSTANT.add(MiddlewareConstant.MW_PLATFORM_CLUSTER);
        IGNORE_CONSTANT.add(MiddlewareConstant.MW_INTERFACE_TYPE);
        IGNORE_CONSTANT.add(MiddlewareConstant.MW_INTERFACE_GROUP_TYPE);
        IGNORE_CONSTANT.add(MiddlewareConstant.MW_MSG_TYPE);
        IGNORE_CONSTANT.add(MiddlewareConstant.MW_SMS_PRIORITY);
        IGNORE_CONSTANT.add(MiddlewareConstant.MW_INTL_MESSAGE);
        IGNORE_CONSTANT.add(MiddlewareConstant.MW_MESSAGE_CREATED_TIMESTAMP);
        IGNORE_CONSTANT.add(MiddlewareConstant.MW_LOG_BUFFER);

        //
        IGNORE_JSON_OR_MAPKEYS.add("REDIS_LAST_UPDATED");
        IGNORE_JSON_OR_MAPKEYS.add(MiddlewareConstant.MW_LOG_BUFFER.getKey());

    }

    private final ClusterType         mClusterType;
    private final InterfaceType       mInterfaceType;
    private final InterfaceGroup      mInterfaceGroupType;
    private final MessageType         mMessageType;
    private final MessagePriority     mMessagePriority;
    private final RouteType           mRouteType;
    private final long                createdTs;
    private final Map<String, Object> messageAttributes = new HashMap<>();

    BaseMessage(
            ClusterType aClusterType,
            InterfaceType aInterfaceType,
            InterfaceGroup aInterfaceGroup,
            MessageType aMessageType,
            MessagePriority aMessagePriority,
            RouteType aRouteType,
            String aProgramMessageType) throws ItextosRuntimeException
    {
        this(aClusterType, aInterfaceType, aInterfaceGroup, aMessageType, aMessagePriority, aRouteType, aProgramMessageType, null);
    }

    BaseMessage(
            ClusterType aClusterType,
            InterfaceType aInterfaceType,
            InterfaceGroup aInterfaceGroup,
            MessageType aMessageType,
            MessagePriority aMessagePriority,
            RouteType aRouteType,
            String aProgramMessageType,
            String aAccountJsonString) throws ItextosRuntimeException
    {
        mClusterType        = aClusterType;
        mInterfaceType      = aInterfaceType;
        mInterfaceGroupType = aInterfaceGroup;
        mMessageType        = aMessageType;
        mMessagePriority    = aMessagePriority;
        mRouteType          = aRouteType;
        createdTs           = DateTimeUtility.getCurrentTimeInMillis();

        validate(mClusterType, mInterfaceType, mInterfaceGroupType, mMessageType, mMessagePriority, createdTs, aProgramMessageType);

        putValueLocally(MiddlewareConstant.MW_MESSAGE_CREATED_TIMESTAMP, Long.toString(createdTs));
        putValueLocally(MiddlewareConstant.MW_PLATFORM_CLUSTER, mClusterType);
        putValueLocally(MiddlewareConstant.MW_INTERFACE_TYPE, mInterfaceType);
        putValueLocally(MiddlewareConstant.MW_INTERFACE_GROUP_TYPE, mInterfaceGroupType);
        putValueLocally(MiddlewareConstant.MW_MSG_TYPE, mMessageType);
        putValueLocally(MiddlewareConstant.MW_SMS_PRIORITY, mMessagePriority);
        putValueLocally(MiddlewareConstant.MW_INTL_MESSAGE, aRouteType);
        putValueLocally(MiddlewareConstant.MW_PROGRAM_MESSAGE_TYPE, aProgramMessageType);

        try
        {

            if (aAccountJsonString != null)
            {
                final JSONParser parser     = new JSONParser();
                final JSONObject jsonObject = (JSONObject) parser.parse(aAccountJsonString);
                putAllByName(jsonObject);
            }
        }
        catch (final Exception e)
        {
            // TODO log here
        }
    }

    BaseMessage(
            String aCompleteJsonString,
            String aProgramMessageType)
            throws Exception
    {
        final JSONParser          parser     = new JSONParser();
        final JSONObject          jsonObject = (JSONObject) parser.parse(aCompleteJsonString);
        final Map<String, Object> map        = new HashMap<>();
        map.putAll(jsonObject);

        mClusterType        = ClusterType.getCluster(MessageUtil.getValueFromMap(map, MiddlewareConstant.MW_PLATFORM_CLUSTER));
        mInterfaceType      = InterfaceType.getType(MessageUtil.getValueFromMap(map, MiddlewareConstant.MW_INTERFACE_TYPE));
        mInterfaceGroupType = InterfaceGroup.getType(MessageUtil.getValueFromMap(map, MiddlewareConstant.MW_INTERFACE_GROUP_TYPE));
        mMessageType        = MessageType.getMessageType(MessageUtil.getValueFromMap(map, MiddlewareConstant.MW_MSG_TYPE));
        mMessagePriority    = MessagePriority.getMessagePriority(MessageUtil.getValueFromMap(map, MiddlewareConstant.MW_SMS_PRIORITY));
        mRouteType          = RouteType.getRouteType(MessageUtil.getValueFromMap(map, MiddlewareConstant.MW_INTL_MESSAGE));
        createdTs           = CommonUtility.getLong(MessageUtil.getValueFromMap(map, MiddlewareConstant.MW_MESSAGE_CREATED_TIMESTAMP), -1);

        validate(mClusterType, mInterfaceType, mInterfaceGroupType, mMessageType, mMessagePriority, createdTs, aProgramMessageType);

        putValueLocally(MiddlewareConstant.MW_MESSAGE_CREATED_TIMESTAMP, Long.toString(createdTs));
        putValueLocally(MiddlewareConstant.MW_PLATFORM_CLUSTER, mClusterType);
        putValueLocally(MiddlewareConstant.MW_INTERFACE_TYPE, mInterfaceType);
        putValueLocally(MiddlewareConstant.MW_INTERFACE_GROUP_TYPE, mInterfaceGroupType);
        putValueLocally(MiddlewareConstant.MW_MSG_TYPE, mMessageType);
        putValueLocally(MiddlewareConstant.MW_SMS_PRIORITY, mMessagePriority);
        putValueLocally(MiddlewareConstant.MW_INTL_MESSAGE, mRouteType);
        putValueLocally(MiddlewareConstant.MW_PROGRAM_MESSAGE_TYPE, aProgramMessageType);

        putAllByKey(map);
    }

    private static void validate(
            ClusterType aClusterType,
            InterfaceType aInterfaceType,
            InterfaceGroup aInterfaceGroupType,
            MessageType aMessageType,
            MessagePriority aMessagePriority,
            long aCreatedTs,
            String aProgramMessageType) throws ItextosRuntimeException
    {
        if (aClusterType == null)
            throw new ItextosRuntimeException("Invalid Cluster spcified.");
        if (aInterfaceType == null)
            throw new ItextosRuntimeException("Invalid InterfaceType spcified.");
        if (aInterfaceGroupType == null)
            throw new ItextosRuntimeException("Invalid InterfaceGroup spcified.");
        if (aMessageType == null)
            throw new ItextosRuntimeException("Invalid MessageType spcified.");
        if (aMessagePriority == null)
            throw new ItextosRuntimeException("Invalid MessagePriority spcified.");
        if (aCreatedTs <= 0)
            throw new ItextosRuntimeException("Invalid Create Time spcified.");
        if ((aProgramMessageType == null) || aProgramMessageType.isBlank())
            throw new ItextosRuntimeException("Invalid Programatic Message Type spcified.");
    }

    public ClusterType getClusterType()
    {
        return mClusterType;
    }

    public InterfaceType getInterfaceType()
    {
        return mInterfaceType;
    }

    public InterfaceGroup getInterfaceGroupType()
    {
        return mInterfaceGroupType;
    }

    public MessageType getMessageType()
    {
        return mMessageType;
    }

    public MessagePriority getMessagePriority()
    {
        return mMessagePriority;
    }

    public RouteType getMessageRouteType()
    {
        return mRouteType;
    }

    public boolean isIsIntl()
    {
        return mRouteType == RouteType.INTERNATIONAL;
    }

    @Override
    /**
     * use {@link #getClonedObject()} method to get the cloned object.
     */
    public Object clone()
            throws CloneNotSupportedException
    {
        throw new CloneNotSupportedException(BaseMessage.class.getName() + " doesnt support clone method. Use getClonedObject() to get a new BaseMessage Object.");
    }

    public String getValue(
            MiddlewareConstant aKey)
    {
        if (aKey == null)
            return null;

        final Object value = messageAttributes.get(aKey.getKey());

        if (value == null)
            return null;

        if (value instanceof ItextosEnum)
            return ((ItextosEnum) value).getKey();

        return (String) value;
    }

    // @Deprecated
    // Object getValueObject(
    // MiddlewareConstant aKey)
    // {
    // return aKey == null ? null : messageAttributes.get(aKey.getKey());
    // }

    public String getServiceInfo(
            String aService,
            String aSubService)
    {
        final String tempService    = CommonUtility.nullCheck(aService, true);
        final String tempSubService = CommonUtility.nullCheck(aSubService, true);

        if (tempService.isBlank() || tempSubService.isBlank())
            return null;
        final String key = CommonUtility.combine(tempService, tempSubService);

        return (String) messageAttributes.get(key);
    }

    void putAllValues(
            Map<MiddlewareConstant, String> aExistingValues)
    {
        for (final Entry<MiddlewareConstant, String> entry : aExistingValues.entrySet())
            putValue(entry.getKey(), entry.getValue());
    }

    public void putValue(
            MiddlewareConstant aKey,
            String aValue)
    {
        putValueLocally(aKey, aValue);
    }

    private void putValueLocally(
            MiddlewareConstant aKey,
            Object aValue)
    {
        messageAttributes.put(aKey.getKey(), aValue);
    }

    private void putAllByName(
            Map<String, Object> aExistingValues)
    {
        putAll(aExistingValues, false);
    }

    private void putByName(
            String key,
            String aValue)
    {
        messageAttributes.put(key, aValue);
    }

    private void putAllByKey(
            Map<String, Object> aMap)
    {
        putAll(aMap, true);
    }

    private void putAll(
            Map<String, Object> aMap,
            boolean aUseKeytoGetConstants)
    {

        for (final Entry<String, Object> entry : aMap.entrySet())
        {
            final String key = entry.getKey();

            if (CHILDREN.equals(key) || IGNORE_JSON_OR_MAPKEYS.contains(key))
            {
                // Don't do anything here. MessageRequest will take care of it.
                if (log.isDebugEnabled())
                    log.debug("Ignoring key '" + key + "'");
                continue;
            }

            final MiddlewareConstant mc = aUseKeytoGetConstants ? MiddlewareConstant.getMiddlewareConstantByKey(key) : MiddlewareConstant.getMiddlewareConstantByName(key);

            if (mc != null)
            {
                if (!IGNORE_CONSTANT.contains(mc))
                    putValue(mc, (String) entry.getValue());
            }
            else
                if (key.contains("" + Constants.DEFAULT_CONCATENATE_CHAR))
                {
                    if (log.isDebugEnabled())
                        log.debug("For the cases of the services sub service, directy add them to the map. Key '" + key + "' Value  '" + entry.getValue() + "'");
                    putByName(key, (String) entry.getValue());
                }
                else
                {

                    if (!key.equals("REDIS_LAST_UPDATED"))
                    {
                        final String                  s                       = "Middleware constant is not availale for the key '" + key + "'. Check the configuration ...";
                        final ItextosRuntimeException ltextosRuntimeException = new ItextosRuntimeException(s);
                     //   log.error(s, ltextosRuntimeException);
                    }
                    messageAttributes.put(key, entry.getValue());
                }
        }
    }

    @Override
    public String getClientId()
    {
        return getValue(MiddlewareConstant.MW_CLIENT_ID);
    }

    @Override
    public String getNextComponent()
    {
        return getValue(MiddlewareConstant.MW_NEXT_COMPONENT);
    }
    
    @Override
    public String getFromComponent()
    {
        return getValue(MiddlewareConstant.MW_FROM_COMPONENT);
    }
    
    @Override
    public String getProcessorComponent()
    {
        return getValue(MiddlewareConstant.MW_PRCOESSOR_COMPONENT);
    }
    
    
 public SMSLog getLogBuffer() {
    	
    	return getLogBufferValue(MiddlewareConstant.MW_LOG_BUFFER);
    }
 
    public SMSLog getLogBufferValue(MiddlewareConstant aLogBufferKey) {
    	
    	/*
    	  if (aLogBufferKey == null)
              return null;

          Object value = messageAttributes.get(aLogBufferKey.getKey());
          
          if(value==null) {
        	  
        	  value=new StringBuffer();
        	  putValueLocally(aLogBufferKey, value);
          }
          return (StringBuffer)value;
		*/
    	return SMSLog.getInstance();
	}
    
  
    public void setLogBufferValue(StringBuffer obj) {

      	  
      	  messageAttributes.put(MiddlewareConstant.MW_LOG_BUFFER.getKey(), obj);
       

	}
	@Override
    public void setNextComponent(
            String aNextComponentKey)
    {
        putValue(MiddlewareConstant.MW_NEXT_COMPONENT, aNextComponentKey);
    }
    
    @Override
    public void setProcessorComponent(
            String aProcessorComponentKey)
    {
        putValue(MiddlewareConstant.MW_PRCOESSOR_COMPONENT, aProcessorComponentKey);
    }
    
    @Override
    public void setFromComponent(
            String aFromComponentKey)
    {
        putValue(MiddlewareConstant.MW_FROM_COMPONENT, aFromComponentKey);
    }

    @Override
    public String getJsonString()
    {
        return getJson().toJSONString();
    }

    JSONObject getJson()
    {
        final JSONObject jsonObj = new JSONObject();
        jsonObj.putAll(messageAttributes);
        jsonObj.remove(MiddlewareConstant.MW_LOG_BUFFER.getKey());
        jsonObj.remove(MiddlewareConstant.MW_PLATFORM_CLUSTER.getKey());
        jsonObj.remove(MiddlewareConstant.MW_INTERFACE_TYPE.getKey());
        jsonObj.remove(MiddlewareConstant.MW_INTERFACE_GROUP_TYPE.getKey());
        jsonObj.remove(MiddlewareConstant.MW_MSG_TYPE.getKey());
        jsonObj.remove(MiddlewareConstant.MW_SMS_PRIORITY.getKey());
        jsonObj.remove(MiddlewareConstant.MW_INTL_MESSAGE.getKey());
        jsonObj.remove(MiddlewareConstant.MW_MESSAGE_CREATED_TIMESTAMP.getKey());

        jsonObj.put(MiddlewareConstant.MW_PLATFORM_CLUSTER.getName(), mClusterType.getKey());
        jsonObj.put(MiddlewareConstant.MW_INTERFACE_TYPE.getName(), mInterfaceType.getKey());
        jsonObj.put(MiddlewareConstant.MW_INTERFACE_GROUP_TYPE.getName(), mInterfaceGroupType.getKey());
        jsonObj.put(MiddlewareConstant.MW_MSG_TYPE.getName(), mMessageType.getKey());
        jsonObj.put(MiddlewareConstant.MW_SMS_PRIORITY.getName(), mMessagePriority.getKey());
        jsonObj.put(MiddlewareConstant.MW_INTL_MESSAGE.getName(), mRouteType.getKey());
        jsonObj.put(MiddlewareConstant.MW_MESSAGE_CREATED_TIMESTAMP.getName(), Long.toString(createdTs));

        return jsonObj;
    }

    @Override
    public String toString()
    {
        return "BaseMessage [mClusterType=" + mClusterType + ", mInterfaceType=" + mInterfaceType + ", mInterfaceGroupType=" + mInterfaceGroupType + ", mMessageType=" + mMessageType
                + ", mMessagePriority=" + mMessagePriority + ", mRouteType=" + mRouteType + ", messageAttributes=" + messageAttributes + "]";
    }

    Map<String, Boolean> removeByName(
            List<String> aMiddlewareConstantNames)
    {
        final Map<String, Boolean> returnValue = new HashMap<>();

        try
        {
            if ((aMiddlewareConstantNames == null) || aMiddlewareConstantNames.isEmpty())
                return returnValue;

            for (final String s : aMiddlewareConstantNames)
                returnValue.put(s, removeByName(s));
            return returnValue;
        }
        catch (final Exception e)
        {
            log.error("Exception while removing data from message " + toString() + " Keys to remove " + aMiddlewareConstantNames, e);
        }
        return null;
    }

    public Map<MiddlewareConstant, Boolean> removeByConstant(
            List<MiddlewareConstant> aMcs)
    {
    	
        final Map<MiddlewareConstant, Boolean> returnValue = new EnumMap<>(MiddlewareConstant.class);

        removeByConstant(MiddlewareConstant.MW_LOG_BUFFER);
    	/*

        try
        {
            if (aMcs == null)
                return returnValue;

            for (final MiddlewareConstant mc : aMcs)
                returnValue.put(mc, removeByConstant(mc));
        }
        catch (final Exception e)
        {
            log.error("Exception while removing data from message " + toString() + " Keys to remove " + aMcs, e);
        }
        */
        return returnValue;

    }

    boolean removeByName(
            String aMiddlewareConstantName)
    {
        return removeByConstant(MiddlewareConstant.getMiddlewareConstantByName(aMiddlewareConstantName));
    }

    boolean removeByConstant(
            MiddlewareConstant aMiddlewareConstant)
    {
        boolean returnValue = false;

        if (aMiddlewareConstant == null)
            log.error("Cannot remove null key from BaseMesssage.", new Exception("Key cannot be null in BaseMessage"));
        else
            returnValue = messageAttributes.remove(aMiddlewareConstant.getKey()) != null;

        RemoveLogBuffer.log(" LOG BUFFER Remove : "+returnValue);
        
        
        return returnValue;
    }

    public static void main(
            String[] args)
    {}

    public BaseMessage getClonedObject()
    {

        try
        {
            final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            new ObjectOutputStream(outputStream).writeObject(this);

            final ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
            return (BaseMessage) new ObjectInputStream(inputStream).readObject();
        }
        catch (final Exception e)
        {
            log.fatal("Problem while creating a cloned object. Cannot use the cloned object here.", e);
        }
        return null;
    }

    public void resetRetryAttempt()
    {
        messageAttributes.remove(MiddlewareConstant.MW_TOPIC_RETRY_ATTEMPT.getKey());
        messageAttributes.remove(MiddlewareConstant.MW_LAST_TOPIC_RETRY_ATTEMPT_TIME.getKey());
    }

    public int incrementRetryAttempt()
    {
        int retryCount = CommonUtility.getInteger(getValue(MiddlewareConstant.MW_TOPIC_RETRY_ATTEMPT), 0);
        ++retryCount;
        putValue(MiddlewareConstant.MW_TOPIC_RETRY_ATTEMPT, Integer.toString(retryCount));
        putValue(MiddlewareConstant.MW_LAST_TOPIC_RETRY_ATTEMPT_TIME, DateTimeUtility.getFormattedCurrentDateTime(DateTimeFormat.DEFAULT_WITH_MILLI_SECONDS));
        return retryCount;
    }

    public ErrorObject getErrorObject(
            Component aComponent,
            Throwable aException) throws ItextosRuntimeException
    {
        final ErrorObject errorObject = new ErrorObject(mClusterType, mInterfaceType, mInterfaceGroupType, mMessageType, mMessagePriority, mRouteType);
        errorObject.putValue(MiddlewareConstant.MW_COMPONENT_NAME, aComponent.getKey());
        errorObject.putValue(MiddlewareConstant.MW_CLIENT_ID, getValue(MiddlewareConstant.MW_CLIENT_ID));
        errorObject.putValue(MiddlewareConstant.MW_MESSAGE_ID, getValue(MiddlewareConstant.MW_MESSAGE_ID));
        errorObject.putValue(MiddlewareConstant.MW_FILE_ID, getValue(MiddlewareConstant.MW_FILE_ID));
        errorObject.putValue(MiddlewareConstant.MW_BASE_MESSAGE_ID, getValue(MiddlewareConstant.MW_BASE_MESSAGE_ID));
        errorObject.putValue(MiddlewareConstant.MW_ERROR_STACKTRACE, getFullStackTrace(aException));
        errorObject.putValue(MiddlewareConstant.MW_FULL_ITEXTO_MESSAGE, getJsonString());
        errorObject.putValue(MiddlewareConstant.MW_ERROR_SERVER_IP, CommonUtility.getApplicationServerIp());
        errorObject.putValue(MiddlewareConstant.MW_ERROR_GENERATED_TIME, DateTimeUtility.getFormattedCurrentDateTime(DateTimeFormat.DEFAULT_WITH_MILLI_SECONDS));
        return errorObject;
    }

    private String getFullStackTrace(
            Throwable aException)
    {

        try
        {
            final String oldTrace = getValue(MiddlewareConstant.MW_ERROR_STACKTRACE);

            if (oldTrace != null)
            {
                final Throwable newThrowable = new Throwable("OLD TRACE " + oldTrace, aException);
                return CommonUtility.getStackTrace(newThrowable);
            }
            return CommonUtility.getStackTrace(aException);
        }
        catch (final Exception e)
        {
            // ignore it.
        }
        return "hahaha";
    }

}