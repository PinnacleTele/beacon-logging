export const ERROR= {

    SOMETHING_WENT_WRONG: {
        message : "Something went wrong!.. please Try Again",
        statusCode: "500",
        error : " Something went wrong!"
    },
    REQUEST_NOT_SEND: {
        message : "Request cannnot be sent, check your network connection. please try again",
        statusCode: "500",
        error : " Request cannot be sent"
    },
    ENTITY_ID_ERROR: {
        message : "Can't get Entity Id Please try again",
        statusCode: "500",
        error : "entityId"
    },
    SENDER_ID_ERROR: {
        message : "Sender-ID Request Timed Out",
        statusCode: "500",
        error : "Sender-ID Request Timed Out"
    },
    UNAUTHORIZED: {
        message : "Unauthorized Request",
        statusCode: "401",
        error : " Unauthorized Request"
    },

    DontHaveBalance: {
        message: "You do not have wallet balance to send any campaign. Please add amount.",
        statusCode: -602,
        error : "dont have enough balance"
    }

}