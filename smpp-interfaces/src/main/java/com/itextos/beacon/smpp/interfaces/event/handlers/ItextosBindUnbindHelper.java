package com.itextos.beacon.smpp.interfaces.event.handlers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.cloudhopper.commons.util.HexUtil;
import com.cloudhopper.smpp.SmppBindType;
import com.cloudhopper.smpp.SmppConstants;
import com.cloudhopper.smpp.SmppServerSession;
import com.cloudhopper.smpp.SmppSessionHandler;
import com.cloudhopper.smpp.pdu.BaseBindResp;
import com.cloudhopper.smpp.type.SmppProcessingException;
import com.itextos.beacon.commonlib.constants.DateTimeFormat;
import com.itextos.beacon.commonlib.constants.exception.ItextosException;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.DateTimeUtility;
import com.itextos.beacon.smpp.interfaces.event.ItextosBindUnbindInterface;
import com.itextos.beacon.smpp.interfaces.sessionhandlers.ItextosSessionManager;
import com.itextos.beacon.smpp.interfaces.util.Communicator;
import com.itextos.beacon.smpp.interfaces.util.counters.BindUnbindCounter;
import com.itextos.beacon.smpp.objects.SessionDetail;
import com.itextos.beacon.smpp.objects.SmppRequestType;
import com.itextos.beacon.smpp.objects.bind.BindInfoValid;
import com.itextos.beacon.smpp.redisoperations.BindCounter;
import com.itextos.beacon.smpp.redisoperations.RedisBindOperation;
import com.itextos.beacon.smpp.redisoperations.SessionInfoRedisUpdate;
import com.itextos.beacon.smpp.utils.properties.SmppProperties;

abstract class ItextosBindUnbindHelper
        implements
        ItextosBindUnbindInterface
{

    ItextosBindUnbindHelper()
    {}

    private static final Log log = LogFactory.getLog(ItextosBindUnbindHelper.class);

    static SmppSessionHandler handleSessionCreated(
            Long aSessionId,
            SmppServerSession aSession,
            BaseBindResp aBindResponse)
            throws SmppProcessingException
    {
        ItextosSmppSessionHandler sessionHandler = null;

        final SessionDetail       aSessionDetail = new SessionDetail(aSessionId, aSession);
        if (log.isInfoEnabled())
            log.info("SessionCreated System Id = '" + aSessionDetail.getSystemId() + "' Host '" + aSessionDetail.getHost() + "' Bind Type '" + aSessionDetail.getBindName() + "'");

        try
        {
            aSessionDetail.updateUserInfo();

            log.fatal("User account information loaded for systemid:'" + aSessionDetail.getSystemId() + "'");

            sessionHandler = new ItextosSmppSessionHandler(aSessionDetail);

            final BindCounter lIncrementBindCounters = incrementBindCounters(aSessionDetail, sessionHandler);

            validateMaxBindCounts(aSessionDetail, lIncrementBindCounters, sessionHandler);

            incrementCounterBasedOnBindType(aSessionDetail, sessionHandler);

            ItextosSessionManager.getInstance().addSessionHandler(sessionHandler);

            sendBindInfo(aSessionDetail, "");

            aBindResponse.setSystemId(aSessionDetail.getSystemId());

            Communicator.sendBindActiveLog(aSessionDetail);
        }
        catch (final Exception e)
        {
            log.error("Exception while binding for user '" + aSessionDetail.getSystemId() + "'", e);

            if (!(e instanceof SmppProcessingException))
                Communicator.sendBindFailureLog(aSessionDetail, "0x" + HexUtil.toHexString(SmppConstants.STATUS_SYSERR), "Internal Error");
            else
                Communicator.sendBindResposeLog(aSessionDetail);

            sendBindInfo(aSessionDetail, e.getMessage());
            throw new SmppProcessingException(SmppConstants.STATUS_SYSERR, "Internal Error");
        }

        return sessionHandler;
    }

    private static void sendBindInfo(
            SessionDetail aSessionDetail,
            String aInformation)
    {
        final String        sourceIp      = aSessionDetail.getHost();

        final String        lInstanceId   = aSessionDetail.getInstanceId();
        final String        lSystemId     = aSessionDetail.getSystemId();

        final String        lClientId     = aSessionDetail.getClientId();
        final String        lBindId       = aSessionDetail.getBindId();
        final SmppBindType  lBindType     = aSessionDetail.getBindType();
        final int           lServerPort   = SmppProperties.getInstance().getApiListenPort();
        final String        lThreadName   = aSessionDetail.getThreadName();

        final BindInfoValid bindInfoValid = new BindInfoValid(lInstanceId, lClientId, SmppRequestType.BIND, lBindType, lBindId, CommonUtility.getApplicationServerIp(), lServerPort, lSystemId,
                sourceIp, lThreadName);

        bindInfoValid.setBindTime(DateTimeUtility.getFormattedDateTime(aSessionDetail.getBindTime(), DateTimeFormat.DEFAULT_WITH_MILLI_SECONDS));
        bindInfoValid.setBindDate(DateTimeUtility.getFormattedDateTime(aSessionDetail.getBindDate(), DateTimeFormat.DEFAULT_DATE_ONLY));

        if (log.isDebugEnabled())
        {
            log.debug("Bind Time :" + bindInfoValid.getBindTime());
            log.debug("Bind Date :" + bindInfoValid.getBindDate());
        }

        Communicator.sendBindLog(aSessionDetail, bindInfoValid);
    }

    private static void validateMaxBindCounts(
            final SessionDetail aSessionDetail,
            BindCounter aBindCounters,
            ItextosSmppSessionHandler aSessionHandler)
            throws SmppProcessingException
    {

        try
        {
            final int maxConnectionsAllowedForUser = aSessionDetail.getMaxBindAllowed();
            final int maxConnectionsForInstance    = SmppProperties.getInstance().getMaxBindAllowed();

            if (maxConnectionsAllowedForUser == -1)
                throw new ItextosException("'max_allowed_connections' is not properly set for user '" + aSessionDetail.getSystemId() + "' and Client Id '" + aSessionDetail.getClientId() + "'");

            if (aBindCounters.getClientsTotalCount() > maxConnectionsAllowedForUser)
                throw new ItextosException("Max Connections per user reached. Max Connections for user '" + maxConnectionsAllowedForUser + "'");

            if (aBindCounters.getInstanceWiseClientCount() > maxConnectionsForInstance)
                throw new ItextosException("Max Connections per instance reached. Max Connections for instance '" + maxConnectionsForInstance + "'");
        }
        catch (final Exception e)
        {
            log.error("Exception while validating the max counts. SystemId '" + aSessionDetail.getSystemId() + "' Client Id '" + aSessionDetail.getClientId() + "' Instance Id '"
                    + aSessionDetail.getInstanceId() + "'", e);
            RedisBindOperation.decreaseBindCount(aSessionDetail.getClientId(), aSessionDetail.getInstanceId());
            aSessionHandler.setBindFailuerError(SmppConstants.STATUS_ALYBND);
            Communicator.sendBindFailureLog(aSessionDetail, "0x" + HexUtil.toHexString(SmppConstants.STATUS_ALYBND), "Max Bind Exceeds");
            throw new SmppProcessingException(SmppConstants.STATUS_ALYBND, "Max Bind Exceeds");
        }
    }

    
    private static BindCounter incrementBindCounters(
            SessionDetail aSessionDetail,
            ItextosSmppSessionHandler aSessionHandler)
            throws SmppProcessingException
    {

        try
        {
            BindUnbindCounter.getInstance().addBind(aSessionDetail.getSystemId());
            return RedisBindOperation.increaseBindCount(aSessionDetail.getClientId(), aSessionDetail.getInstanceId());
        }
        catch (final Exception e)
        {
            log.error("Exception while updating counts. SystemId '" + aSessionDetail.getSystemId() + "' Client Id '" + aSessionDetail.getClientId() + "' Instance Id '" + aSessionDetail.getInstanceId()
                    + "'", e);
            aSessionHandler.setBindFailuerError(SmppConstants.STATUS_SYSERR);
            Communicator.sendBindFailureLog(aSessionDetail, "0x" + HexUtil.toHexString(SmppConstants.STATUS_SYSERR), "Redis Bind increment exception");

            throw new SmppProcessingException(SmppConstants.STATUS_SYSERR, "Redis Bind increment exception");
        }
    }


    private static void incrementCounterBasedOnBindType(
            SessionDetail aSessionDetail,
            ItextosSmppSessionHandler aSessionHandler)
            throws SmppProcessingException
    {

        try
        {

            switch (aSessionDetail.getBindType())
            {
                case TRANSCEIVER:
                case RECEIVER:
                    SessionInfoRedisUpdate.increaseTransactionBindCount(aSessionDetail.getClientId(), aSessionDetail.getInstanceId(), true);
                    break;

                case TRANSMITTER:
                default:
                    SessionInfoRedisUpdate.increaseTransactionBindCount(aSessionDetail.getClientId(), aSessionDetail.getInstanceId(), false);
            }
        }
        catch (final Exception e)
        {
            log.error("Exception while updating Bind based counts. SystemId '" + aSessionDetail.getSystemId() + "' Client Id '" + aSessionDetail.getClientId() + "' Instance Id '"
                    + aSessionDetail.getInstanceId() + "'", e);
            aSessionHandler.setBindFailuerError(SmppConstants.STATUS_SYSERR);
            Communicator.sendBindFailureLog(aSessionDetail, "0x" + HexUtil.toHexString(SmppConstants.STATUS_SYSERR), "Redis Bind increment exception");

            throw new SmppProcessingException(SmppConstants.STATUS_SYSERR, "Redis BindType increment exception");
        }
    }

    static void handleSessionDestroyed(
            Long aSessionId,
            SmppServerSession aSession)
    {

        try
        {
            if (!aSession.isBinding())
                ItextosSessionManager.getInstance().removeSession(true, aSessionId, aSession);
        }
        catch (final Exception e)
        {
            log.error("Problem while removing the destroyed session. Session id '" + aSessionId + "'", e);
        }
    }

}