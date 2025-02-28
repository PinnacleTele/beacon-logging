package com.itextos.beacon.commonlib.apperrorhandler.servlets;

import java.util.HashMap;
import java.util.Map;

public class ErrorCodeStatus
{

    private ErrorCodeStatus()
    {}

    private static final Map<String, String> errorCodes = new HashMap<>();
    private static String                    htmlHeader = null;
    static
    {
        errorCodes.put("400", "Bad Request");
        errorCodes.put("401", "Unauthorized");
        errorCodes.put("402", "Payment Required");
        errorCodes.put("403", "Forbidden");
        errorCodes.put("404", "Not Found");
        errorCodes.put("405", "Method Not Allowed");
        errorCodes.put("406", "Not Acceptable");
        errorCodes.put("407", "Proxy Authentication Required");
        errorCodes.put("408", "Request Timeout");
        errorCodes.put("409", "Conflict");
        errorCodes.put("410", "Gone");
        errorCodes.put("411", "Length Required");
        errorCodes.put("412", "Precondition Failed");
        errorCodes.put("413", "Payload Too Large");
        errorCodes.put("414", "Request-URI Too Long");
        errorCodes.put("415", "Unsupported Media Type");
        errorCodes.put("416", "Requested Range Not Satisfiable");
        errorCodes.put("417", "Expectation Failed");
        errorCodes.put("418", "I'm a teapot");
        errorCodes.put("421", "Misdirected Request");
        errorCodes.put("422", "Unprocessable Entity");
        errorCodes.put("423", "Locked");
        errorCodes.put("424", "Failed Dependency");
        errorCodes.put("426", "Upgrade Required");
        errorCodes.put("428", "Precondition Required");
        errorCodes.put("429", "Too Many Requests");
        errorCodes.put("431", "Request Header Fields Too Large");
        errorCodes.put("444", "Connection Closed Without Response");
        errorCodes.put("451", "Unavailable For Legal Reasons");
        errorCodes.put("499", "Client Closed Request");
        errorCodes.put("500", "Internal Server Error");
        errorCodes.put("501", "Not Implemented");
        errorCodes.put("502", "Bad Gateway");
        errorCodes.put("503", "Service Unavailable");
        errorCodes.put("504", "Gateway Timeout");
        errorCodes.put("505", "HTTP Version Not Supported");
        errorCodes.put("506", "Variant Also Negotiates");
        errorCodes.put("507", "Insufficient Storage");
        errorCodes.put("508", "Loop Detected");
        errorCodes.put("510", "Not Extended");
        errorCodes.put("511", "Network Authentication Required");
        errorCodes.put("599", "Network Connect Timeout Error");

        final StringBuilder sb = new StringBuilder();
        sb.append("<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=ISO-8859-1\">");
        sb.append("<title>iTexTos Error Page</title>");
        sb.append("<style>");
        sb.append("table{display:table;border-collapse:separate;border-spacing:2px;border-color:gray;border:1px;}");
        sb.append("p.regular{font:15px Arial, sans-serif;}");
        sb.append("p.bold{font:bold 15px Arial, sans-serif;}");
        sb.append("</style>");
        sb.append("<link rel=\"shortcut icon\" type=\"image/jpg\" href=\"favicon.jpg\" />");
        sb.append("</head><body>");
        sb.append("<Font family:'arial'>");
        sb.append("<table border='1' align='center'>");
        sb.append("<tr><td colspan='3' align='center'><font color='red'><p class='bold'>Something went wrong while processing the request.</p></font></td></tr>");
        sb.append("<tr><td colspan='3' align='center'><p class='bold'>Response Time : ");
        htmlHeader = sb.toString();
    }

    public static final String getErrorDescription(
            String aErrorCode)
    {
        final String desc = errorCodes.get(aErrorCode);
        return desc == null ? "Unknown" : desc;
    }

    public static String getHTMLHeader()
    {
        return htmlHeader;
    }

}