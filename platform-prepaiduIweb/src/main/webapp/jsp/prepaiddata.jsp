<%@page import="java.text.DecimalFormat"%>
<%@page import="java.util.Set"%>
<%@page import="java.util.Map"%>
<%@page
	import="com.itextos.beacon.platform.prepaiddata.inmemory.CurrencyData"%>
<%@page import="com.itextos.beacon.platform.prepaiddata.PrepaidData"%>
<%@page import="com.itextos.beacon.platform.prepaiddata.ReadRedisData"%>
<%@page import="com.itextos.beacon.commonlib.utility.CommonUtility"%>
<%@page import="java.util.Date"%>
<%@page import="java.text.SimpleDateFormat"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="ISO-8859-1">
<title>Prepaid Data Sheet</title>
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
	<%
	response.setIntHeader("Refresh", 5 * 60);
	String             CLI_ID_OR_USERNAME   = "cliid_user";
	String             RESULT               = "result";
	String             color_nouser_bgcolor = "#ff0000";
	String             color_nouser         = "#ffffff";
	String             color_nonactive      = "#3333ff";
	String             color_lowBalance     = "#ff0000";
	String             color_others         = "#339900";
	%>
	<table align="center" width="100%">
		<tr>
			<td align="center"><font face="arial, sans-serif"><h1>Beacon
						Prepaid Details</h1></font></td>
		</tr>
		<tr>
			<font face="arial, sans-serif">
				<td align="right">Last refreshed at
			</font>
			<font face="arial, sans-serif" color="blue"><%=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())%></font>
			</td>
		</tr>
	</table>
	<br>
	<br>
	<form name="interfaceform" method="post"
		action="prepaiddatafetchservlet">
		<%
		String                                    cliidUser = CommonUtility.nullCheck(session.getAttribute(CLI_ID_OR_USERNAME), true);
		Map<String, Set<PrepaidData>>             result    = (Map<String, Set<PrepaidData>>) session.getAttribute(RESULT);
		%>
		<table align="center" width="100%">
			<tr>
				<td align="right"><font face="arial, sans-serif">Enter
						Client Id / Username :</font></td>
				<td><font face="arial, sans-serif"><input type="text"
						value="<%=cliidUser%>" name="<%=CLI_ID_OR_USERNAME%>"
						id="<%=CLI_ID_OR_USERNAME%>"></font>
					<button onclick="resetfield()" type="reset" name="reset">Reset</button>
					<button onclick="formValidation()" type="submit"
						name="prepaiddatafetchservlet">Submit</button></td>
		</table>
		<br> <br>
		<table align="center" width="100%" border="1">
			<tr>
				<th>Client Id</th>
				<th>User</th>
				<th>Currency</th>
				<th>Balance</th>
				<th>Status</th>
			</tr>
			<%
			if (result != null)
			{
			    DecimalFormat                df         = new DecimalFormat("###,###,##0.000000");
			    Set<PrepaidData>             noUserData = result.get(ReadRedisData.NO_USER);
			%>
			<%
			if (noUserData != null && !noUserData.isEmpty())
			{

			    for (PrepaidData pd : noUserData)
			    {
			        CurrencyData cd = pd.getCurrencyInfo();
			%>
			<tr>
				<td bgcolor="<%=color_nouser_bgcolor%>"><font
					color="<%=color_nouser%>"><%=pd.getCliId()%></font></td>
				<td bgcolor="<%=color_nouser_bgcolor%>"><font
					color="<%=color_nouser%>"><%=CommonUtility.nullCheck(pd.getUserName(), true)%></font></td>
				<td bgcolor="<%=color_nouser_bgcolor%>"><font
					color="<%=color_nouser%>"><%=cd == null ? "N/A" : cd.getCode() + "-" + cd.getDesc()%></font></td>
				<td bgcolor="<%=color_nouser_bgcolor%>" align='right'><font
					color="<%=color_nouser%>"><%=df.format(pd.getPrepaidBalance())%></font></td>
				<td bgcolor="<%=color_nouser_bgcolor%>" align='center'><font
					color="<%=color_nouser%>">No User Data Found</font></td>
			</tr>
			</font>
			<%
			} //End of noUserData for
			} //End of noUserData if
			%>

			<%
			Set<PrepaidData> nonActiveUserData = result.get(ReadRedisData.NON_ACTIVE);

			if (nonActiveUserData != null && !nonActiveUserData.isEmpty())
			{

			    for (PrepaidData pd : nonActiveUserData)
			    {
			        CurrencyData cd = pd.getCurrencyInfo();
			%>
			<tr>
				<td><font color="<%=color_nonactive%>"><%=pd.getCliId()%></font></td>
				<td><font color="<%=color_nonactive%>"><%=CommonUtility.nullCheck(pd.getUserName(), true)%></font></td>
				<td><font color="<%=color_nonactive%>"><%=cd == null ? "N/A" : cd.getCode() + "-" + cd.getDesc()%></font></td>
				<td align='right'><font color="<%=color_nonactive%>"><%=df.format(pd.getPrepaidBalance())%></font></td>
				<td align='center'><font color="<%=color_nonactive%>"><%=pd.getAccountStatus()%></font></td>
			</tr>
			</font>
			<%
			} //End of nonActiveUserData for
			} //End of nonActiveUserData if
			%>

			<%
			Set<PrepaidData> lowBalanceUserData = result.get(ReadRedisData.LOW_BALANCE);

			if (lowBalanceUserData != null && !lowBalanceUserData.isEmpty())
			{

			    for (PrepaidData pd : lowBalanceUserData)
			    {
			        CurrencyData cd = pd.getCurrencyInfo();
			%>
			<tr>
				<td><font color="<%=color_lowBalance%>"><%=pd.getCliId()%></font></td>
				<td><font color="<%=color_lowBalance%>"><%=CommonUtility.nullCheck(pd.getUserName(), true)%></font></td>
				<td><font color="<%=color_lowBalance%>"><%=cd == null ? "N/A" : cd.getCode() + "-" + cd.getDesc()%></font></td>
				<td align='right'><font color="<%=color_lowBalance%>"><%=df.format(pd.getPrepaidBalance())%></font></td>
				<td align='center'><font color="<%=color_lowBalance%>"><%=pd.getAccountStatus()%></font></td>
			</tr>

			<%
			} //End of lowBalanceUserData for
			} //End of lowBalanceUserData if
			%>
			<%
			Set<PrepaidData> otherBalanceUserData = result.get(ReadRedisData.OTHER_BALANCE);

			if (otherBalanceUserData != null && !otherBalanceUserData.isEmpty())
			{

			    for (PrepaidData pd : otherBalanceUserData)
			    {
			        CurrencyData cd = pd.getCurrencyInfo();
			%><tr>
				<td><font color="<%=color_others%>"><%=pd.getCliId()%></font></td>
				<td><font color="<%=color_others%>"><%=CommonUtility.nullCheck(pd.getUserName(), true)%></font></td>
				<td><font color="<%=color_others%>"><%=cd == null ? "N/A" : cd.getCode() + "-" + cd.getDesc()%></font></td>
				<td align='right'><font color="<%=color_others%>"><%=df.format(pd.getPrepaidBalance())%></font></td>
				<td align='center'><font color="<%=color_others%>"><%=pd.getAccountStatus()%></font></td>
			</tr>
			<%
			} //End of otherBalanceUserData for
			} //End of otherBalanceUserData if
			%>
			<%
			}
			else
			{
			%>
			<tr>
				<td colspan='5' align="center">Not data found</td>
			</tr>
			<%
			}
			%>
		</table>
	</form>
</body>
</html>