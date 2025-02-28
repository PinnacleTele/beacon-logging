<%@ page import="com.itextos.beacon.commonlib.utility.CommonUtility"%>
<%@ page import="com.itextos.beacon.platform.prepaiddata.kannelstatus.KannelInfo"%>
<%@ page
	import="com.itextos.beacon.platform.prepaiddata.kannelstatus.KannelInfoLoader"%>
<%@ page
	import="com.itextos.beacon.platform.prepaiddata.kannelstatus.ReadKannelInfo"%>
<%@ page import="java.text.SimpleDateFormat"%>
<%@ page import="java.util.ArrayList"%>
<%@ page import="java.util.Arrays"%>
<%@ page import="java.util.Date"%>
<%@ page import="java.util.List"%>
<%@ page import="java.util.Map"%>
<%@ page import="java.util.Set"%>
<%@ page import="java.util.TreeSet"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="ISO-8859-1">
<title>kannel info</title>
<style>
table {
	font-family: arial, sans-serif;
	border-collapse: collapse;
	width: 100%;
}

th {
	border: 1px solid #808080;
	background-color: #dddddd;
	padding: 8px;
}

td {
	border: 1px solid #808080;
	padding: 8px;
}
</style>
</head>
<link rel="stylesheet" href="css/common.css">
<body class="bodycolor">
	<table align="center" width="100%">
		<tr>
			<td align="center"><font face="arial, sans-serif"><h1>Beacon
						Kannel Details</h1></font></td>
		</tr>
		<tr>
			<td align="right"><font face="arial, sans-serif">Last
					refreshed at : </font> <font face="arial, sans-serif" color="blue"><%=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())%></font>
			</td>
		</tr>
	</table>

	<br>

	<%
	Map<String, List<KannelInfo>>             lKannelInfoMap = (Map<String, List<KannelInfo>>) session.getAttribute("kannelinfo");
	List<String>                              lOperators     = (List<String>) session.getAttribute("operators");
	List<String>                              lRouteList     = (List<String>) session.getAttribute("routes");
	Set<String>                               lAllOperators  = (Set<String>) KannelInfoLoader.getInstance().getAllOperators();

	System.out.println("selected Operators '" + lOperators + "'");
	System.out.println("selected Routes    '" + lRouteList + "'");

	if (lOperators == null)
	    lOperators = new ArrayList<>();

	if (lRouteList == null)
	    lRouteList = new ArrayList<>();
	%>

	<form name="kannelinfoform" id="kannelinfoform" method="post"
		action="kannelinfofetchservlet">
		<table align="center" width="100%">
			<%
			String refreshIntervalObj = (String) request.getAttribute("refresh");
			refreshIntervalObj = refreshIntervalObj != null ? refreshIntervalObj : "30";

			System.out.println("refreshIntervalObj >>> " + refreshIntervalObj);

			if (!("-1".equals(refreshIntervalObj)))
			    response.setIntHeader("Refresh", CommonUtility.getInteger(refreshIntervalObj, 30));
			%>
			<tr>
				<td align="right" colspan='4'><font face="arial, sans-serif">
						Refresh Interval &nbsp; <select name="refresh" id="refresh">
							<option value="5" <%if ("5".equals(refreshIntervalObj))
{%>
								selected <%}%>>Every 5 seconds</option>
							<option value="10" <%if ("10".equals(refreshIntervalObj))
{%>
								selected <%}%>>Every 10 seconds</option>
							<option value="30" <%if ("30".equals(refreshIntervalObj))
{%>
								selected <%}%>>Every 30 seconds</option>
							<option value="-1" <%if ("-1".equals(refreshIntervalObj))
{%>
								selected <%}%>>Don't refresh</option>
					</select>
				</font></td>
			</tr>
			<tr>
				<td align="center"><font face="arial, sans-serif">Operator</font></td>
				<td><font face="arial, sans-serif"> <select
						name="operator" id="operator" multiple
						onchange="getSelectedOperator()">
							<%
							for (String operator : lAllOperators)
							{
							%>
							<option value="<%=operator%>"
								<%if (lOperators.contains(operator))
{%> selected <%}%>><%=operator%></option>
							<%
							}
							%>

					</select>

				</font></td>
				<td align="center"><font face="arial, sans-serif">Route</font></td>
				<td><font face="arial, sans-serif"> <select name="route"
						id="route" multiple onchange="getSelectedRoute()">

							<%
							if (lOperators.isEmpty())
							{

							    if (lRouteList.isEmpty())
							    {

							        for (String operator : lAllOperators)
							        {
							    List<String> routeIds = KannelInfoLoader.getInstance().getRoutesForOperator(operator);

							    for (String routeId : routeIds)
							    {
							%>
							<option value="<%=routeId%>"><%=routeId%></option>

							<%
							}
							}
							}
							}
							else
							{

							if (lRouteList.isEmpty())
							{

							for (String operator : lOperators)
							{
							List<String> routeIds = KannelInfoLoader.getInstance().getRoutesForOperator(operator);

							for (String routeId : routeIds)
							{
							%>
							<option value="<%=routeId%>"><%=routeId%></option>
							<%
							}
							}
							}
							else
							{

							for (String operator : lOperators)
							{
							List<String> routeIds = KannelInfoLoader.getInstance().getRoutesForOperator(operator);

							for (String routeId : routeIds)
							{
							%>
							<option value="<%=routeId%>"
								<%if (lRouteList.contains(routeId))
{%> selected <%}%>><%=routeId%></option>
							<%
							}
							}
							}
							}
							%>
					</select>

				</font></td>
			</tr>
			<tr>
				<td align="center" colspan='2'><font face="arial, sans-serif">
						<input type="reset" id="reset" value="Reset" onClick="reset()">
				</font></td>
				<td align="center" colspan='2'><font face="arial, sans-serif">
						<input type="submit" id="submit" value="Submit"
						onClick="submitForm()">
				</font></td>
			</tr>
		</table>
	</form>
	<br>
	<br>

	<table align="center" width="100%">
		<tr>
			<th>Operator</th>
			<th>Route</th>
			<th>Available Status</th>
			<th align='right'>Store Size</th>
			<th align='center'>Kannel IP</th>
			<th align='right'>Kannel Port</th>
			<th align='right'>Kannel Status Port</th>
			<th align='center'>Last Updated</th>
		</tr>
		<%
		if (lKannelInfoMap != null)
		{
		    List<KannelInfo>             unAvailableKannels = lKannelInfoMap.get(ReadKannelInfo.UNAVAILABLE_KANNELS);
		    List<KannelInfo>             availableKannels   = lKannelInfoMap.get(ReadKannelInfo.AVAILABLE_KANNELS);
		%>
		<tr>
			<td colspan='4' align="center" bgcolor="#ccffcc"><font
				face="arial, sans-serif" color="red"><b>Unavailable : <%=unAvailableKannels == null ? "0" : unAvailableKannels.size()%></b></font></td>
			<td colspan='4' align="center" bgcolor="#ccffcc"><font
				face="arial, sans-serif" color="green"><b>Available : <%=availableKannels == null ? "0" : availableKannels.size()%></b></font></td>
		</tr>
		<%
		if (unAvailableKannels != null && !unAvailableKannels.isEmpty())
		{

		    for (KannelInfo lKannelInfo : unAvailableKannels)
		    {
		%>
		<tr>
			<td><font face="arial, sans-serif" color="red"><%=lKannelInfo.getOperator()%></font></td>
			<td><font face="Courier New, Courier, Consolas, Verdana"
				color="red"><%=lKannelInfo.getRoute()%></font></td>
			<td><font face="arial, sans-serif" color="red">Unavailable</font></td>
			<td align='right'><font face="arial, sans-serif" color="red"><%=lKannelInfo.getStoreSize()%></font></td>
			<td align='center'><font face="arial, sans-serif" color="red"><%=lKannelInfo.getKannalIp()%></font></td>
			<td align='right'><font face="arial, sans-serif" color="red"><%=lKannelInfo.getKannelPort()%></font></td>
			<td align='right'><font face="arial, sans-serif" color="red"><%=lKannelInfo.getKannelStatusPort()%></font></td>
			<td align='center'><font face="arial, sans-serif" color="red"><%=lKannelInfo.getLastUpdated()%></font></td>
		</tr>
		<%
		}
		}

		if (availableKannels != null && !availableKannels.isEmpty())
		{

		for (KannelInfo lKannelInfo : availableKannels)
		{
		String color = lKannelInfo.getStoreSize() < 0 ? "blue" : "green";
		%>
		<tr>
			<td><font face="arial, sans-serif" color="<%=color%>"><%=lKannelInfo.getOperator()%></font></td>
			<td><font face="Courier New, Courier, Consolas, Verdana"
				color="<%=color%>"><%=lKannelInfo.getRoute()%></font></td>
			<td><font face="arial, sans-serif" color="<%=color%>">Available</font></td>
			<td align='right'><font face="arial, sans-serif"
				color="<%=color%>"><%=lKannelInfo.getStoreSize()%></font></td>
			<td align='center'><font face="arial, sans-serif"
				color="<%=color%>"><%=lKannelInfo.getKannalIp()%></font></td>
			<td align='right'><font face="arial, sans-serif"
				color="<%=color%>"><%=lKannelInfo.getKannelPort()%></font></td>
			<td align='right'><font face="arial, sans-serif"
				color="<%=color%>"><%=lKannelInfo.getKannelStatusPort()%></font></td>
			<td align='center'><font face="arial, sans-serif"
				color="<%=color%>"><%=lKannelInfo.getLastUpdated()%></font></td>
		</tr>
		<%
		}
		}
		%>
		<%
		}
		else
		{
		%>
		<tr>
			<td colspan='8' align="center">No data found</td>
		</tr>
		<%
		}
		%>
	</table>
</body>
</html>