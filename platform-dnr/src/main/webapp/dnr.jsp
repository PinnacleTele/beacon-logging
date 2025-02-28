<%@page import="com.itextos.beacon.commonlib.constants.DateTimeFormat"%>
<%@page import="com.itextos.beacon.commonlib.utility.DateTimeUtility"%>
<%@page import="com.itextos.beacon.commonlib.utility.CommonUtility"%>
<%@page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@page import="com.itextos.beacon.platform.dnr.process.*,java.util.*,java.text.SimpleDateFormat,java.util.concurrent.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>DLR Receiver</title>
</head>
<body>
    <table  border="1" width="100%" style="background-color:#8BA7C7">
     <tr><td align="center"> DLR Receiver Counter</td></tr>  
    </table>
	<table  border="1" width="100%" style="background-color:#8BA7C7">		
	<%
		
		String currDate = DateTimeUtility.getFormattedCurrentDateTime(DateTimeFormat.DEFAULT_DATE_ONLY);
		
		long currCounter = ReceivedCounter.getInstance().get();
	
	%>
		<tr width="100%">
			<td align="center" width="40%"> Date </td>
			<td align="center" width="60%"> Dlr Received Count </td>
		</tr>
		
		<tr>
			<td><%= currDate %></td>
			<td><%= currCounter %> </td>
		</tr>
	
	<%
		LinkedHashMap<String, String> prevList = ReceivedCounter.getInstance().getPrvList();
		
		for(Map.Entry<String, String> anEntry:prevList.entrySet()) {	
	%>
		<tr>
			<td><%=anEntry.getKey() %></td>
			<td><%=anEntry.getValue() %></td>
		</tr>
	<%
		}
	%>
	</table>

</body>
</html>