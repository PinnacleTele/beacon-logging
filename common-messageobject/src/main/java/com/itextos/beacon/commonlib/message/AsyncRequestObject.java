package com.itextos.beacon.commonlib.message;

import org.json.simple.JSONObject;

import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.constants.InterfaceType;
import com.itextos.beacon.commonlib.constants.MessagePriority;
import com.itextos.beacon.commonlib.constants.MessageType;
import com.itextos.beacon.commonlib.constants.MiddlewareConstant;
import com.itextos.beacon.commonlib.constants.RouteType;

public class AsyncRequestObject
        extends
        AbstractMessage
{

    private static final long   serialVersionUID = -7710785325325342718L;

    private final ClusterType   mCluster;
    private final InterfaceType mInterfaceType;
    private final MessageType   mMessageType;
    private final String        mInstanceId;
    private final String        mCustomerId;
    private final String        mMessageId;
    private final String        mCustomerIp;
    private final String        mMessageContent;
    private final long          mRequestedTime;
    private String              mNextComponent;
    private String              mFromComponent;
    private String              mProcessorComponent;
    
    private StringBuffer        mLogBuffer;

    private final String        mMessageSource;

    public AsyncRequestObject(
            ClusterType aCluster,
            InterfaceType aRequestType,
            MessageType aMessageType,
            String aInstanceId,
            String aCustomerId,
            String aMessageId,
            String aCustomerIp,
            String aMessageContent,
            long aRequestedTime,
            String aMessageSource)
    {
        mCluster        = aCluster;
        mInterfaceType  = aRequestType;
        mMessageType    = aMessageType;
        mInstanceId     = aInstanceId;
        mCustomerId     = aCustomerId;
        mMessageId      = aMessageId;
        mCustomerIp     = aCustomerIp;
        mMessageContent = aMessageContent;
        mRequestedTime  = aRequestedTime;
        mMessageSource  = aMessageSource;
    }

    public ClusterType getClusterType()
    {
        return mCluster;
    }

    public String getInstanceId()
    {
        return mInstanceId;
    }

    public String getCustomerId()
    {
        return mCustomerId;
    }

    public String getMessageId()
    {
        return mMessageId;
    }

    public String getCustomerIp()
    {
        return mCustomerIp;
    }

    public long getRequestedTime()
    {
        return mRequestedTime;
    }

    public InterfaceType getRequestType()
    {
        return mInterfaceType;
    }

    public String getMessageContent()
    {
        return mMessageContent;
    }

    public MessageType getMessageType()
    {
        return mMessageType;
    }

    @Override
    public String getClientId()
    {
        return null;
    }

    @Override
    public String getNextComponent()
    {
        return mNextComponent;
    }
    
    @Override
    public String getProcessorComponent()
    {
        return mProcessorComponent;
    }
    
    @Override
    public String getFromComponent()
    {
        return mFromComponent;
    }

    @Override
    public void setNextComponent(
            String aNextComponentKey)
    {
        mNextComponent = aNextComponentKey;
    }
    
    @Override
    public void setProcessorComponent(
            String aProcessorComponentKey)
    {
        mProcessorComponent = aProcessorComponentKey;
    }
    
    
    @Override
    public void setFromComponent(
            String aFromComponentKey)
    {
        mFromComponent = aFromComponentKey;
    }

    public String getMessageSource()
    {
        return mMessageSource;
    }

    @Override
    public Object clone()
            throws CloneNotSupportedException
    {
        throw new CloneNotSupportedException("AsyncRequestObject doesnt support clone method.");
    }

    @Override
    public String toString()
    {
        return "AsyncRequestObject [mCluster=" + mCluster + ", mInstanceId=" + mInstanceId + ", mCustomerId=" + mCustomerId + ", mMessageId=" + mMessageId + ", mCustomerIp=" + mCustomerIp
                + ", mRequestedTime=" + mRequestedTime + ", mInterfaceType=" + mInterfaceType + ", mMessageContent=" + mMessageContent + ", mMessageType=" + mMessageType + "]";
    }

    @Override
    public String getJsonString()
    {

        try
        {
            final JSONObject jsonObj = new JSONObject();
            jsonObj.put(MiddlewareConstant.MW_PLATFORM_CLUSTER.getName(), mCluster.getKey());
            jsonObj.put(MiddlewareConstant.MW_INTERFACE_TYPE.getName(), mInterfaceType.getKey());
            jsonObj.put(MiddlewareConstant.MW_INTERFACE_GROUP_TYPE.getName(), mInterfaceType.getGroup().getKey());
            jsonObj.put(MiddlewareConstant.MW_MSG_TYPE.getName(), mMessageType.getKey());
            jsonObj.put(MiddlewareConstant.MW_SMS_PRIORITY.getName(), MessagePriority.PRIORITY_5.getKey());
            jsonObj.put(MiddlewareConstant.MW_INTL_MESSAGE.getName(), RouteType.DOMESTIC.getKey());
            jsonObj.put(MiddlewareConstant.MW_MESSAGE_CREATED_TIMESTAMP.getName(), Long.toString(mRequestedTime));
            jsonObj.put(MiddlewareConstant.MW_NEXT_COMPONENT.getKey(), mNextComponent);
            jsonObj.put(MiddlewareConstant.MW_PROGRAM_MESSAGE_TYPE.getKey(), "AsyncRequestObject");
            jsonObj.put("RequestString", mMessageContent);
            return jsonObj.toJSONString();
        }
        catch (final Exception e)
        {
            return "INVALID JSON";
        }
    }

	
}