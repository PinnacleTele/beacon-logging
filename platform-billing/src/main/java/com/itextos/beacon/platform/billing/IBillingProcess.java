package com.itextos.beacon.platform.billing;

import com.itextos.beacon.commonlib.constants.exception.ItextosException;
import com.itextos.beacon.errorlog.SMSLog;

public interface IBillingProcess
{

    // Common
    void process(SMSLog sb)
            throws ItextosException;

    // Submission
    // void updatePartNumberDetails();
    //
    // void prepaidRefund();
    //
    // void sendToFullMessageTopic();
    //
    // void encryptMessageAndMobile();
    //
    // void updateAlpha();
    //
    // void updateHeaders();
    //
    // void checkForGdprCompilance();
    //
    // void identifySuffix();
    //
    // void updateSubmitDate();
    //
    // void updateSubmitTime();
    //
    // void updateSubmissionCarrierCircle();
    //
    // void updateScheduleTime();
    //
    // void updateSubmissionStatus();
    //
    // void updateSTS();
    //
    // void updateActualSTS();
    //
    // void updateFilename();
    //
    // void updateCountry();
    //
    // void updateSubmissionLatencies();

    // Deliveries
    // void updateDTime();
    //
    // void updateTerminatorCarrierCircle();
    //
    // void updateDeliveriesStatus();
    //
    // void updateCarrierReceivedTime();
    //
    // void updateDeliveryLatencies();

}