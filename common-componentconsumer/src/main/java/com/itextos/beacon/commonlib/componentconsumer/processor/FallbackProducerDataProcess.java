package com.itextos.beacon.commonlib.componentconsumer.processor;
// package com.itextos.beacon.commonlib.kafka.processor;
//
// import java.lang.reflect.Constructor;
// import java.util.ArrayList;
// import java.util.HashMap;
// import java.util.List;
// import java.util.Map;
// import java.util.Map.Entry;
//
// import org.apache.commons.logging.Log;
// import org.apache.commons.logging.LogFactory;
// import org.json.simple.JSONObject;
// import org.json.simple.parser.JSONParser;
//
// import com.itextos.beacon.commonlib.constants.Component;
// import com.itextos.beacon.commonlib.constants.MiddlewareConstant;
// import com.itextos.beacon.commonlib.constants.TimerIntervalConstant;
// import com.itextos.beacon.commonlib.kafka.process.MessageProcessor;
// import com.itextos.beacon.commonlib.kafka.processor.extend.Utility;
// import com.itextos.beacon.commonlib.kafka.service.common.KafkaUtility;
// import com.itextos.beacon.commonlib.message.IMessage;
// import com.itextos.beacon.commonlib.utility.timer.ITimedProcess;
// import com.itextos.beacon.commonlib.utility.timer.TimedProcessor;
//
// public class FallbackProducerDataProcess
// implements
// ITimedProcess
// {
//
// private static final Log log =
// LogFactory.getLog(FallbackProducerDataProcess.class);
//
// private final TimedProcessor mTimedProcessor;
// private boolean canContinue = true;
// private final Component mComponent;
//
// FallbackProducerDataProcess(
// Component aComponent)
// {
// mComponent = aComponent;
// mTimedProcessor = new TimedProcessor("TimerThread-FallbackDataProcess-" +
// aComponent, this, TimerIntervalConstant.KAFKA_PRODUCER_FALLBACK_DATA);
// mTimedProcessor.start();
// }
//
// @Override
// public boolean processNow()
// {
// final Map<String, List<String>> lFallbackProducerData =
// KafkaUtility.getFallbackProducerData(mComponent);
// handleProducerData(lFallbackProducerData);
// return false;
// }
//
// @Override
// public boolean canContinue()
// {
// return canContinue;
// }
//
// @Override
// public void stopMe()
// {
// canContinue = false;
// }
//
// private void handleProducerData(
// Map<String, List<String>> aFallbackProducerData)
// {
// if (aFallbackProducerData.isEmpty())
// return;
//
// // This may have multiple topics related messages.
// final Map<String, List<FallbackMessage>> lDataFromMap =
// getDataFromMap(aFallbackProducerData);
//
// for (final Entry<String, List<FallbackMessage>> entry :
// lDataFromMap.entrySet())
// {
// final String topicName = entry.getKey();
// final List<FallbackMessage> lValue = entry.getValue();
//
// for (final FallbackMessage msg : lValue)
// try
// {
// MessageProcessor.writeMessage(mComponent, msg.getNextComponent(),
// msg.getMessage());
// }
// catch (final Exception e)
// {
// log.error("Exception while producing the message. Topic '" + topicName + "'.
// Message '" + msg + "'", e);
// }
// }
// }
//
// private Map<String, List<FallbackMessage>> getDataFromMap(
// Map<String, List<String>> aFallbackData)
// {
// final Map<String, List<FallbackMessage>> data = new HashMap<>();
//
// for (final Entry<String, List<String>> entry : aFallbackData.entrySet())
// {
// final String topicName = entry.getKey();
// final List<String> messageList = entry.getValue();
//
// if (messageList.isEmpty())
// continue;
//
// int count = 0;
// final List<FallbackMessage> list = data.computeIfAbsent(topicName, k -> new
// ArrayList<>());
// for (final String message : messageList)
// try
// {
// final FallbackMessage messageToPush = getMessage(message);
// list.add(messageToPush);
// count++;
// }
// catch (final Exception e)
// {
// log.fatal("Cannot continue with the messages in this topic '" + topicName +
// "' Check the 'configuration.kafak_topic_class_reference' table."
// + " Somthing went wrong while converting the Json to Java Objects." + " Json
// String '" + message + "'", e);
// }
//
// if (log.isDebugEnabled())
// log.debug("Component '" + mComponent + "' Topic Name '" + topicName + "'
// total records loaded '" + count + "'");
// }
// return data;
// }
//
// private static FallbackMessage getMessage(
// String aMessage)
// throws Exception
// {
// final JSONParser parser = new JSONParser();
// final JSONObject jsonObj = (JSONObject) parser.parse(aMessage);
// final String programMessageType = (String)
// jsonObj.get(MiddlewareConstant.MW_PROGRAM_MESSAGE_TYPE.getKey());
// final Component nextComponent = Component.getComponent((String)
// jsonObj.get(MiddlewareConstant.MW_NEXT_COMPONENT.getKey()));
// final String className = Utility.getClassName(programMessageType);
// final Class<?> cls = Class.forName(className);
// final Constructor<?> constructor = cls.getDeclaredConstructor(String.class);
// final IMessage iMessage = (IMessage) constructor.newInstance(aMessage);
// return new FallbackMessage(iMessage, nextComponent);
// }
//
// }
