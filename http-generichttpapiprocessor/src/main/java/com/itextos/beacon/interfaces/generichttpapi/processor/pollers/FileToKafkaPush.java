package com.itextos.beacon.interfaces.generichttpapi.processor.pollers;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.exception.ItextosException;
import com.itextos.beacon.commonlib.constants.exception.ItextosRuntimeException;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.http.generichttpapi.common.data.QueueObject;
import com.itextos.beacon.http.generichttpapi.common.interfaces.IRequestProcessor;
import com.itextos.beacon.http.generichttpapi.common.utils.APIConstants;
import com.itextos.beacon.http.generichttpapi.common.utils.FileGenUtil;
import com.itextos.beacon.http.interfaceutil.MessageSource;
import com.itextos.beacon.interfaces.generichttpapi.processor.request.JSONRequestProcessor;
import com.itextos.beacon.interfaces.generichttpapi.processor.request.XMLRequestProcessor;

public class FileToKafkaPush
        implements
        Runnable
{

    private static final Log log = LogFactory.getLog(FileToKafkaPush.class);

    private final String     fileName;

    public FileToKafkaPush(
            String aFileName)
    {
        fileName = aFileName;
    }

    @Override
    public void run()
    {

        try
        {
            final String filePath = getFileName(FolderType.TOBE_PROCESS_FOLDER) + fileName;

            if (log.isDebugEnabled())
                log.debug("Processing File ......... " + filePath);

            final String content = CommonUtility.nullCheck(FileGenUtil.readFileContent(filePath), true);

            if (content.isBlank())
            {
                moveToErrorFolder(filePath, "Empty Content.");
                return;
            }

            final QueueObject qObj = getData(content);

            if (qObj == null)
            {
                moveToErrorFolder(filePath, "Unable to create request object.");
                return;
            }

            if (log.isDebugEnabled())
                log.debug("Request Polled From File, QueObject is " + qObj);

            processFile(filePath, qObj);
        }
        catch (final Exception e1)
        {
            log.error("Exception occer while handover to Kafka- ", e1);
        }
    }

    private void processFile(
            String aFilePath,
            QueueObject aQueueObject)
            throws ItextosException, ItextosRuntimeException
    {
        IRequestProcessor reqHandler;
        
        StringBuffer sb=new StringBuffer();

        if (MessageSource.GENERIC_XML.equalsIgnoreCase(aQueueObject.getReqType()))
            reqHandler = new XMLRequestProcessor(aQueueObject.getRequestMag(), aQueueObject.getCustIp(), aQueueObject.getRequestedTime(),sb);
        else
            reqHandler = new JSONRequestProcessor(aQueueObject.getRequestMag(), aQueueObject.getCustIp(), aQueueObject.getRequestedTime(), aQueueObject.getReqType(), aQueueObject.getReqType(),sb);

        reqHandler.parseBasicInfo("");

        final boolean isQSuccess = reqHandler.pushRRQueue(aQueueObject, "Queue");

        if (log.isDebugEnabled())
            log.debug("Push to Kafka Status : " + isQSuccess);

        if (isQSuccess)
            FileGenUtil.fileMove(aFilePath, getFileName(FolderType.PROCESSED_FOLDER), fileName);
        else
            moveToErrorFolder(aFilePath, "Unknown Error while processing the file.");
    }

    private void moveToErrorFolder(
            String aFilepath,
            String aErrorMessage) throws ItextosRuntimeException
    {
        log.error("Unable to process the file " + aFilepath + ". Reason : " + aErrorMessage);
        FileGenUtil.fileMove(aFilepath, getFileName(FolderType.ERROR_FOLDER), fileName);
    }

    private static String getFileName(
            FolderType aType) throws ItextosRuntimeException
    {
        String folderName;

        switch (aType)
        {
            case TOBE_PROCESS_FOLDER:
                folderName = APIConstants.REQUEST_FILE_PATH;
                break;

            case PROCESSED_FOLDER:
                folderName = APIConstants.PROCESSED_REQUEST_FILE_PATH;
                break;

            case ERROR_FOLDER:
                folderName = APIConstants.ERROR_REQUEST_FILE_PATH;
                break;

            default:
                throw new ItextosRuntimeException("Invalid Type specified. type " + aType);
        }

        final StringBuilder sb = new StringBuilder();
        sb.append(folderName).append(File.separator).append(APIConstants.CLUSTER_INSTANCE).append(File.separator);
        return sb.toString();
    }

    private static QueueObject getData(
            String fileContent)
    {
        QueueObject queueObj = null;

        try

        {
            final String[] data          = fileContent.split("" + APIConstants.PREFIX);
            final String   reqType       = CommonUtility.nullCheck(replacePrefixVal(data[0]), true);
            final String   custIP        = CommonUtility.nullCheck(replacePrefixVal(data[1]), true);
            final String   mid           = CommonUtility.nullCheck(replacePrefixVal(data[2]), true);
            final String   ClientId      = CommonUtility.nullCheck(replacePrefixVal(data[3]), true);
            final String   Cluster       = CommonUtility.nullCheck(replacePrefixVal(data[4]), true);
            final String   MsgType       = CommonUtility.nullCheck(replacePrefixVal(data[5]), true);
            final long     requestedTime = Long.parseLong(CommonUtility.nullCheck(replacePrefixVal(data[6]), true));
            final String   request       = replacePrefixVal(data[7]);

            queueObj = new QueueObject(mid, custIP, request, reqType, requestedTime, ClientId, Cluster, MsgType);
        }
        catch (final Exception e)
        {
            log.error("Exception occer while custruct the QueueObject Object..", e);
        }
        return queueObj;
    }

    private static String replacePrefixVal(
            String value)
    {
        if (log.isDebugEnabled())
            log.debug("Actual Value --->" + value);

        String replaceString = "";

        if (value != null)
            replaceString = value.trim().replace("" + APIConstants.SUFFIX, "");

        if (log.isDebugEnabled())
            log.debug("Replace Value --->" + replaceString);

        return replaceString;
    }

    enum FolderType
    {

        TOBE_PROCESS_FOLDER,
        PROCESSED_FOLDER,
        ERROR_FOLDER

    }

}