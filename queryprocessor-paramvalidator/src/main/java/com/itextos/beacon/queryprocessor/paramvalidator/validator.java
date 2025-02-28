package com.itextos.beacon.queryprocessor.paramvalidator;

import com.itextos.beacon.queryprocessor.commonutils.Utility;

import jakarta.servlet.http.HttpServletRequest;

public class validator
{

    public static Boolean ValidateRequest(
            HttpServletRequest req)
    {
        final String response_type = Utility.nullCheck(req.getParameter("output_format"), true);

        return response_type != "";
    }

    public static Boolean ValidateQueueRequest(
            HttpServletRequest req)
    {
        if ((Utility.nullCheck(req.getParameter("cli_id"), true) == "") || (Utility.nullCheck(req.getParameter("recv_date_from"), true) == "")
                || (Utility.nullCheck(req.getParameter("recv_date_to"), true) == ""))
            return false;

        return true;
    }

}
