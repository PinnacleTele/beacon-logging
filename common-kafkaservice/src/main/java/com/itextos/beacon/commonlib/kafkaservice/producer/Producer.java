package com.itextos.beacon.commonlib.kafkaservice.producer;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.KafkaException;

import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.constants.exception.ItextosException;
import com.itextos.beacon.commonlib.kafkaservice.common.KafkaCustomProperties;
import com.itextos.beacon.commonlib.kafkaservice.common.KafkaRedisHandler;
import com.itextos.beacon.commonlib.kafkaservice.common.KafkaUtility;
import com.itextos.beacon.commonlib.message.DeliveryObject;
import com.itextos.beacon.commonlib.message.IMessage;
import com.itextos.beacon.commonlib.message.MessagePart;
import com.itextos.beacon.commonlib.message.MessageRequest;
import com.itextos.beacon.commonlib.message.SubmissionObject;
import com.itextos.beacon.commonlib.prometheusmetricsutil.PrometheusMetrics;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.tp.ExecutorKafkaProducer;
import com.itextos.beacon.smslog.ProducerFlushLog;
import com.itextos.beacon.smslog.PromosenderLog;
import com.itextos.beacon.smslog.StartupFlowLog;
import com.itextos.beacon.smslog.TranssenderLog;
import com.itextos.beacon.smslog.ProducerTPLog;

public class Producer
{

    private static final Log                         log           = LogFactory.getLog(Producer.class);

    private final Component                          mComponent;
    private final String                             mTopicName;
    private final Properties                         mKafkaProperties;
    private final String                             mLogTopicName;
    private final ProducerInMemCollection            mProducerInMemCollection;

    private KafkaProducer<String, IMessage>          mProducer;
    private boolean                                  mInitialized  = false;
    private int                                      mBatchCounter = 0;
    private long                                     lastFlushed   = System.currentTimeMillis();
    private long                                     totalCount    = 0;
    private boolean                                  isCompleted   = false;
    private final List<ProducerCallbackForInterface> callBackList  = new ArrayList<>();

    public Producer(
            Component aComponent,
            String aTopicName,
            KafkaProducerProperties aKafkaConsumerProperties,
            ProducerInMemCollection aProducerInMemCollection)
            throws ItextosException
    {
        mComponent               = aComponent;
        mTopicName               = aTopicName;
        mKafkaProperties         = aKafkaConsumerProperties.getProperties();
        mLogTopicName            = "Topic Name : '" + mTopicName + "' ";
        mProducerInMemCollection = aProducerInMemCollection;
        createProducer();

      
        
        ExecutorKafkaProducer.getInstance().addTask(new FlushMonitor(this), aTopicName);
  
    }

    
    public String getTopicName() {
    	
    	return mTopicName;
    }
    
    
    private void createProducer()
            throws ItextosException
    {
        int counter = 0;

        while (!mInitialized)
        {
            counter++;

            try
            {
                if (log.isDebugEnabled())
                    log.debug(mLogTopicName);

                KafkaUtility.printProperties(mTopicName + " Producer", mKafkaProperties);

                mProducer = new KafkaProducer<>(mKafkaProperties);

                if (log.isDebugEnabled())
                {
                    log.debug(mLogTopicName + "Kafka Non-Trans Producer initialized successfully ");
                    log.debug(mLogTopicName + "Kafka Non-Trans Producer Configuration : " );
                }
                mInitialized = true;
            }
            catch (final Exception exp)
            {
                log.error(mLogTopicName + "Attempt " + counter + ": Problem in initializing Kafka Non-Trans Producer. Will retry after 100 millis...", exp);
                CommonUtility.sleepForAWhile(100);
            }

            if (counter > 10)
            {
                log.error("Problem in recreating the Kafka Server more than 10 times. Throwing error to callerr. Caller has to handle it..");
                // System.exit(-1);
                throw new ItextosException("Unable to connect to Kafka servers.");
            }
        }

        if (log.isDebugEnabled())
            log.debug("Started Producer successfully for topic " + mTopicName);
    }

    public boolean sendAsync(
            IMessage aMessage,
            BlockingQueue<IMessage> aFallBackHolder)
            throws ItextosException
    {
        boolean sent = false;

        try
        {
            String threadName = Thread.currentThread().getName();

        	StringBuffer msgidparts=new StringBuffer();

        	String msgid="notfind";
        	
        	String msgtype="notfind";
        	
        	if(aMessage instanceof DeliveryObject) {
        		
        		msgtype=((DeliveryObject)aMessage).getMessageType().getKey();

        		msgid =((DeliveryObject)aMessage).getMessageId()+" msgtype : "+msgtype+ " getClusterType : "+((DeliveryObject)aMessage).getClusterType().toString()+ " getSmsPriority : "+((DeliveryObject)aMessage).getSmsPriority()+ " getMessagePriority : "+((DeliveryObject)aMessage).getMessagePriority();
        		if(msgtype!=null&&msgtype.equals("0")) {
        			
        			PromosenderLog.log(new Date()+" : "+threadName+" : "+ ((DeliveryObject)aMessage).getUser()+" fileid :  "+ ((DeliveryObject)aMessage).getFileId() +" : msgid[ "+msgid+" ] "+ mLogTopicName + " IMessage sent successfully in Non-Trans mode (Async)");

        		}else {
        			
        			TranssenderLog.log(new Date()+" : "+threadName+" : "+ ((DeliveryObject)aMessage).getUser()+" fileid :  "+ ((DeliveryObject)aMessage).getFileId()+" : msgid[  "+msgid+ " ] "+ mLogTopicName + " IMessage sent successfully in Non-Trans mode (Async)");
        		}
        	}else if(aMessage instanceof SubmissionObject ) {
        		
        		msgid =((SubmissionObject)aMessage).getFileId();

        	}else if(aMessage instanceof MessageRequest ) {
        		
        		

        		
  		List<MessagePart> parts=((MessageRequest)aMessage).getMessageParts();
        		
        		if(parts !=null) {
        		parts.forEach((p)->{
        			
        			msgidparts.append(": "+p.getMessageId());
        			
        		});
        		}

        		msgid=msgidparts.toString();
        		msgtype=((MessageRequest)aMessage).getMessageType().getKey();
       
        		if(msgtype!=null&&msgtype.equals("0")) {
        			
        			PromosenderLog.log(new Date()+" : "+threadName+" : "+ ((MessageRequest)aMessage).getUser()+" fileid :  "+ ((MessageRequest)aMessage).getFileId() +" : msgid[ "+msgid+" ] "+ mLogTopicName + " IMessage sent successfully in Non-Trans mode (Async)");

        		}else {
        			
        			TranssenderLog.log(new Date()+" : "+threadName+" : "+ ((MessageRequest)aMessage).getUser()+" fileid :  "+ ((MessageRequest)aMessage).getFileId()+" : msgid[  "+msgid+ " ] "+ mLogTopicName + " IMessage sent successfully in Non-Trans mode (Async)");
        		}
       
        	

        	}
        	
            addToInMemory(aMessage);
            final ProducerRecord<String, IMessage> kafkaRecord                   = getProducerRecord(aMessage);
            final ProducerCallbackForInterface     lProducerCallbackForInterface = new ProducerCallbackForInterface(this, mTopicName, aMessage, aFallBackHolder);
            callBackList.add(lProducerCallbackForInterface);
            mProducer.send(kafkaRecord, lProducerCallbackForInterface);

            mBatchCounter++;
            totalCount++;

            flushBasedOnCount();

            PrometheusMetrics.kafkaProducerIncrement(mTopicName, 1);

            if (log.isDebugEnabled())
                log.debug(msgid+ " "+ mLogTopicName + " IMessage sent successfully in Non-Trans mode (Async)");
            sent = true;
        }
        catch (final KafkaException exp)
        {
            log.error(mLogTopicName + " Unrecoverable exception thrown during sending message to kafka. Recreating producer...", exp);
            recoverProducer();
        }
        catch (final Exception exp)
        {
            log.error("Problem sending message failing this time...", exp);
            throw new ItextosException("Problem sending message failing this time...", exp);
        }
        return sent;
    }

    void removeCompletedCallback(
            ProducerCallbackForInterface aCompletedCallBack)
    {
        final boolean lRemove = callBackList.remove(aCompletedCallBack);
        if (!lRemove)
            log.error("Something is not right here. Unable to remove the call back from the list '" + aCompletedCallBack + "'");
    }

    public boolean sendAsync(
            IMessage aMessage)
            throws ItextosException
    {
        boolean sent = false;

        try
        {
            String threadName = Thread.currentThread().getName();

        	
        	StringBuffer msgidparts=new StringBuffer();
        	String msgid="notfind";
        	
        	String msgtype="notfind";
        	if(aMessage instanceof DeliveryObject) {
        		
        		msgtype=((DeliveryObject)aMessage).getMessageType().getKey();

        		msgid =((DeliveryObject)aMessage).getMessageId()+" msgtype : "+msgtype+ " getClusterType : "+((DeliveryObject)aMessage).getClusterType().toString()+ " getSmsPriority : "+((DeliveryObject)aMessage).getSmsPriority()+ " getMessagePriority : "+((DeliveryObject)aMessage).getMessagePriority();
        		if(msgtype!=null&&msgtype.equals("0")) {
        			
        			PromosenderLog.log(new Date()+" : "+threadName+" : "+ ((DeliveryObject)aMessage).getUser()+" fileid :  "+ ((DeliveryObject)aMessage).getFileId() +" : msgid[ "+msgid+" ] "+ mLogTopicName + " IMessage sent successfully in Non-Trans mode (Async)");

        		}else {
        			
        			TranssenderLog.log(new Date()+" : "+threadName+" : "+ ((DeliveryObject)aMessage).getUser()+" fileid :  "+ ((DeliveryObject)aMessage).getFileId()+" : msgid[  "+msgid+ " ] "+ mLogTopicName + " IMessage sent successfully in Non-Trans mode (Async)");
        		}
        	}else if(aMessage instanceof SubmissionObject ) {
        		
        		msgid =((SubmissionObject)aMessage).getFileId();

        	}else if(aMessage instanceof MessageRequest ) {
        		
        		

        		
  		List<MessagePart> parts=((MessageRequest)aMessage).getMessageParts();
        		
        		if(parts !=null) {
        		parts.forEach((p)->{
        			
        			msgidparts.append(": "+p.getMessageId());
        			
        		});
        		}

        		msgid=msgidparts.toString();
        		msgtype=((MessageRequest)aMessage).getMessageType().getKey();
       
        		if(msgtype!=null&&msgtype.equals("0")) {
        			
        			PromosenderLog.log(new Date()+" : "+threadName+" : "+ ((MessageRequest)aMessage).getUser()+" fileid :  "+ ((MessageRequest)aMessage).getFileId() +" : msgid[ "+msgid+" ] "+ mLogTopicName + " IMessage sent successfully in Non-Trans mode (Async)");

        		}else {
        			
        			TranssenderLog.log(new Date()+" : "+threadName+" : "+ ((MessageRequest)aMessage).getUser()+" fileid :  "+ ((MessageRequest)aMessage).getFileId()+" : msgid[  "+msgid+ " ] "+ mLogTopicName + " IMessage sent successfully in Non-Trans mode (Async)");
        		}
       
        	

        	}
        	
            addToInMemory(aMessage);
            final ProducerRecord<String, IMessage> kafkaRecord = getProducerRecord(aMessage);
            mProducer.send(kafkaRecord, new ProducerCallback(this, mTopicName, aMessage));

            mBatchCounter++;
            totalCount++;

            flushBasedOnCount();

            PrometheusMetrics.kafkaProducerIncrement(mTopicName, 1);

            if (log.isDebugEnabled())
                log.debug(msgid+ " "+ mLogTopicName + " IMessage sent successfully in Non-Trans mode (Async)");
            sent = true;
        }
        catch (final KafkaException exp)
        {
            log.error(mLogTopicName + " Unrecoverable exception thrown during sending message to kafka. Recreating producer...", exp);
            recoverProducer();
        }
        catch (final Exception exp)
        {
            log.error("Problem sending message failing this time...", exp);
            throw new ItextosException("Problem sending message failing this time...", exp);
        }
        return sent;
    }

    private void addToInMemory(
            IMessage aMessage)
            throws ItextosException
    {
        mProducerInMemCollection.addMessage(aMessage);
    }

    void removeFromInMemory(
            IMessage aMessage)
    {
        mProducerInMemCollection.removeMessage(aMessage);
    }

    void flushBasedOnCount()
    {
        if (mBatchCounter >= KafkaCustomProperties.getInstance().getProducerMaxFlushCount())
            flush();
    }

    public synchronized void flush()
    {
        flush(null);
    }

    public synchronized void flush(
            String aEvent)
    {
    	
        String threadName = Thread.currentThread().getName();

        if (aEvent != null) {
            log.fatal(threadName+" aEvent "+aEvent + " Producer " + KafkaUtility.formatTopicName(mTopicName) + " Batch count :" + String.format("%8s", mBatchCounter));
       
            ProducerFlushLog.log("mTopicName : "+mTopicName+ " : "+threadName+" aEvent "+aEvent + " Producer " + KafkaUtility.formatTopicName(mTopicName) + " Batch count :" + String.format("%8s", mBatchCounter));
        } else {
            if (log.isDebugEnabled())
                log.debug("Producer " + KafkaUtility.formatTopicName(mTopicName) + " Batch count :" + String.format("%8s", mBatchCounter));
        
            ProducerFlushLog.log("mTopicName : "+mTopicName+ " : "+threadName+" aEvent "+aEvent + " Producer " + KafkaUtility.formatTopicName(mTopicName) + " Batch count :" + String.format("%8s", mBatchCounter));

        }
        mProducer.flush();
        mBatchCounter = 0;
        lastFlushed   = System.currentTimeMillis();
        
    }

    void flushBaesdOnTime()
    {
        if (log.isDebugEnabled())
            log.debug("Total Counts " + totalCount + " Last Batch count " + mBatchCounter);

        if ((totalCount>0) && ((System.currentTimeMillis() - lastFlushed) > KafkaCustomProperties.getInstance().getProducerMaxFlushTimeInterval())) {
        	
        	flush();
        }
            
    }

    private ProducerRecord<String, IMessage> getProducerRecord(
            IMessage aMessage)
    {
        return new ProducerRecord<>(mTopicName, aMessage);
    }

    private void recoverProducer()
            throws ItextosException
    {
        boolean done = false;

        while (!done)
        {
            closeProducer();
            mInitialized = false;
            createProducer();
            done = true;
        }
    }

    private void closeProducer()
    {

        try
        {
            mProducer.close();
        }
        catch (final Exception e)
        {
            log.error(mLogTopicName + "Problem closing producer Non-Trans.", e);
        }
    }

    public void flushMessages()
    {
        log.fatal(mLogTopicName + " Flushing INVOKED");

        try
        {
            waitForCallbacksToComplete();
        }
        catch (final Exception e)
        {
            log.error("Exception while waiting for the Callback processes to complete.", e);
        }

        try
        {
            flush("FromShutdown");

            // Should not stop the producers to be used in the inmemory messages.
            // mProducer.close();
        }
        catch (final Exception e)
        {
            log.error(mLogTopicName + "Problem Flushing producer Non-Trans.", e);
        }

        processInMemMessages();
    }

    private void waitForCallbacksToComplete()
    {
        int count = 0;

        while (!callBackList.isEmpty())
        {
            log.fatal("Wait for all the callbacks to complete processing ..." + (count++) + " Inmem size : " + mProducerInMemCollection.getInMemSize());
            CommonUtility.sleepForAWhile(10);
        }
        log.fatal("Completed all the callback process.");
    }

    private void processInMemMessages()
    {
        final List<IMessage> lMessages = mProducerInMemCollection.getRemainingMessages();

        if (!lMessages.isEmpty())
        {
            log.fatal("Ideally we should not have any data here from PRODUCER. Please check the handling parts....", new Exception("NEED TO CHECK THIS POINT IN PRODUCER"));
            KafkaRedisHandler.addToProducerRedis(mComponent, mTopicName, lMessages);
        }

        log.fatal(KafkaUtility.formatTopicName(mTopicName) + " On Producer shutdown pushed messages to Redis count " + lMessages.size());

        isCompleted = true;
    }

    public boolean isCompleted()
    {
        return isCompleted;
    }

    public Component getComponent()
    {
        return mComponent;
    }

    @Override
    public String toString()
    {
        return "Producer [mComponent=" + mComponent + ", mTopicName=" + mTopicName + ", lastFlushed=" + lastFlushed + "]";
    }

}

class FlushMonitor
        implements
        Runnable
{

    private static final Log log          = LogFactory.getLog(FlushMonitor.class);

    private final Producer   mProducer;
    private boolean          mCanContinue = true;

    FlushMonitor(
            Producer aProducer)
    {
        mProducer = aProducer;
    }

    @Override
    public void run()
    {

    	while (mCanContinue)
        {
        	
    		
    		ProducerTPLog.getInstance(mProducer.getTopicName()).log(mProducer.getTopicName()+ " : "+new Date());
        	
         	
            if (log.isDebugEnabled())
                log.debug("Calling the Producer.doFlushCheck() method");
            mProducer.flushBaesdOnTime();
       
            CommonUtility.sleepForAWhile(5L * KafkaCustomProperties.getInstance().getProducerMaxFlushTimeInterval());
        
         
        }
    }

    public void stopMe()
    {
        mCanContinue = false;
    }
    
    
    

}