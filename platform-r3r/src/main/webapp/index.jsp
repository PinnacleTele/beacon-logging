<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@ page import="com.itextos.beacon.r3r.utils.R3RConstants"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="ISO-8859-1">
<title>iTextos Smart Link Application</title>
</head>
<body>
	<h1>
		<center>Welcome to iTextos Smart Link Application</center>
	</h1>

	<br>
	<br>
	<center>
		<font color="blue"> If you are landing in this page, please
			check the link you have received in SMS or check with the vendor.</font>
	</center>
	<br>
	<br>

	<%
	    String             errorCode = (String) session.getAttribute("errorcode");
	String             url       = (String) session.getAttribute("receivedurl");

	if (R3RConstants.REDIRECT_URL_NOT_VALID.equals(errorCode))
	    out.print("<center>" + errorCode + ": Request Url is Not Valid :  Url is <font color=red><b>" + url + "</b></font></center>");
	else
	    if (R3RConstants.REDIRECT_URL_NOT_AVAILABLE.equals(errorCode))
	        out.print("<center>" + errorCode + ": Redirect URL is not available, Url is <font color=red><b>" + url + "</b></font></center>");
	    else
	        if (R3RConstants.REQUEST_SHORTCODE_NOT_AVAILABLE.equals(errorCode))
	    out.print("<center>" + errorCode + ": Request Shortcode is not available in System , Url is <font color=red><b>" + url + "</b></font></center>");
	        else
	    if (R3RConstants.SHORTCODE_NOT_PROVIDED.equals(errorCode))
	        out.print("<center>" + errorCode + ": Request Shortcode is not available in Url , Url is <font color=red><b>" + url + "</b></font></center>");
	    else
	        if (R3RConstants.REDIRECT_URL_EXCEPTION.equals(errorCode))
	            out.print("<center>" + errorCode + ": Redirect Url is Not Valid , Url is <font color=red><b>" + url + "</b></font></center>");
	%>
</body>
</html>