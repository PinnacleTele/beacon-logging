<%@page import="com.itextos.beacon.commonlib.utility.CommonUtility"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="ISO-8859-1">
<title>encryption info</title>
</head>
<link rel="stylesheet" href="css/common.css">
<body>
	<h2 align="center">Encryption Info</h2>
	<%
	    String cryptoType = CommonUtility.nullCheck(session.getAttribute("cryptotype"), true);
	String encodeText = CommonUtility.nullCheck(session.getAttribute("encodeText"), true);
	String encodedText = CommonUtility.nullCheck(session.getAttribute("encodedText"), true);

	String etext = CommonUtility.nullCheck(session.getAttribute("etext"), true);
	String eKey = CommonUtility.nullCheck(session.getAttribute("ekey"), true);
	String enryptedText = CommonUtility.nullCheck(session.getAttribute("enryptedText"), true);
	
	String decodeText = CommonUtility.nullCheck(session.getAttribute("decodeText"), true);
	String decodedText = CommonUtility.nullCheck(session.getAttribute("decodedText"), true);


	String dKey = CommonUtility.nullCheck(session.getAttribute("dkey"), true);
	String dtext = CommonUtility.nullCheck(session.getAttribute("dtext"), true);
	String decryptedText = CommonUtility.nullCheck(session.getAttribute("decryptedText"), true);
	%>
	<div class="container">
		<form action="encryption"  method="post">

			<div id="encrypttype" class="row" align="center">
				<div class="col-25">
					<input type="radio" id="encode" name="cryptotype" value="encode"
						onclick="handleEncryptProcess()"
						<%if ("encode".equals(cryptoType)) {%> checked <%}%>> <label
						for="encode">Base64 Encode</label>
				</div>
				<div class="col-25">
					<input type="radio" id="encrypt" name="cryptotype" value="encrypt"
						onclick="handleEncryptProcess()"
						<%if ("encrypt".equals(cryptoType)) {%> checked <%}%>> <label
						for="encrypt">Encrypt</label>
				</div>
				<div class="col-25">
					<input type="radio" id="decode" name="cryptotype" value="decode"
						onclick="handleEncryptProcess()"
						<%if ("decode".equals(cryptoType)) {%> checked <%}%>> <label
						for="decode">Base64 Decode</label>
				</div>
				<div class="col-25">
					<input type="radio" id="decrypt" name="cryptotype" value="decrypt"
						onclick="handleEncryptProcess()"
						<%if ("decrypt".equals(cryptoType)) {%> checked <%}%>> <label
						for="decrypt">Decrypt</label>
				</div>
			</div>
			<!-- Encode Part -->
			<div id="encodediv">
				<div class="row">
					<div class="col-25">
						<label for="encodetext">Text To Encode</label>
					</div>
					<div class="col-75">
						<input type="text" id="encodetext" name="encodetext"
							value="<%=encodeText%>">
					</div>
				</div>
				<div class="row">
					<div class="col-25">
						<label for="encodedtext">Encoded Text</label>
					</div>
					<div class="col-75">
						<textarea id="encodedtext" style="height: 200px"><%=encodedText%></textarea>
					</div>
				</div>
			</div>

			<!-- Encrypt Part -->
			<div id="encryptdiv">
				<div class="row">
					<div class="col-25">
						<label for="encrypttext">Text To Encrypt</label>
					</div>
					<div class="col-75">
						<input type="text" id="encrypttext" name="encrypttext"
							value="<%=etext%>">
					</div>
				</div>
				<div class="row">
					<div class="col-25">
						<label for="ekey">Encryption Key</label>
					</div>
					<div class="col-75">
						<input type="text" id="ekey" name="ekey" value="<%=eKey%>">
					</div>
				</div>
				<div class="row">
					<div class="col-25">
						<label for="encryptedtext">Encrypted Text</label>
					</div>
					<div class="col-75">
						<textarea id="encryptedtext" style="height: 200px"><%=enryptedText%></textarea>
					</div>
				</div>
			</div>
			
			<!-- Decode Part -->
			<div id="decodediv">
				<div class="row">
					<div class="col-25">
						<label for="decodetext">Text To Decode</label>
					</div>
					<div class="col-75">
						<input type="text" id="decodetext" name="decodetext"
							value="<%=decodeText%>">
					</div>
				</div>
				<div class="row">
					<div class="col-25">
						<label for="decodedtext">Decoded Text</label>
					</div>
					<div class="col-75">
						<textarea id="decodedtext" style="height: 200px"><%=decodedText%></textarea>
					</div>
				</div>
			</div>
			
			<!-- Decrypt Part -->
			<div id="decryptdiv">
				<div class="row">
					<div class="col-25">
						<label for="decrypttext">Text To Decrypt</label>
					</div>
					<div class="col-75">
						<input type="text" id="decrypttext" name="decrypttext"
							value="<%=dtext%>">
					</div>
				</div>
				<div class="row">
					<div class="col-25">
						<label for="dkey">Decryption Key</label>
					</div>
					<div class="col-75">
						<input type="text" id="dkey" name="dkey" value="<%=dKey%>">
					</div>
				</div>
				<div id="dtext" class="row">
					<div class="col-25">
						<label for="decryptedtext">Decrypted Text</label>
					</div>
					<div class="col-75">
						<textarea id="decryptedtext" style="height: 200px"><%=decryptedText%></textarea>
					</div>
				</div>
			</div>
			<div class="row">
				<div class="col-50">
					<input type="reset" value="Reset">
				</div>
				<div class="col-10">
					<input onclick="return validateForm()" type="submit" value="Submit" onclick="return validateForm()">
				</div>
			</div>
		</form>
	</div>
	<script type="text/javascript">
		window.onload = loadData();

		function loadData() {
			handleEncryptProcess()
		}

		function handleEncryptProcess() {
			if (document.getElementById("encode").checked) {
				document.getElementById("encodediv").style.display = "block";
				document.getElementById("decodediv").style.display = "none";
				document.getElementById("encryptdiv").style.display = "none";
				document.getElementById("decryptdiv").style.display = "none";
				document.getElementById("decodetext").value = "";
				document.getElementById("decodedtext").value = "";
				document.getElementById("encrypttext").value = "";
				document.getElementById("ekey").value = "";
				document.getElementById("encryptedtext").value = "";
				document.getElementById("decrypttext").value = "";
				document.getElementById("dkey").value = "";
				document.getElementById("decryptedtext").value = "";
			} else if (document.getElementById("encrypt").checked) {
				document.getElementById("encodediv").style.display = "none";
				document.getElementById("encryptdiv").style.display = "block";
				document.getElementById("decodediv").style.display = "none";
				document.getElementById("decryptdiv").style.display = "none";
				document.getElementById("decodetext").value = "";
				document.getElementById("decodedtext").value = "";
				document.getElementById("encodetext").value = "";
				document.getElementById("encodedtext").value = "";
				document.getElementById("decrypttext").value = "";
				document.getElementById("dkey").value = "";
				document.getElementById("decryptedtext").value = "";

			} else if (document.getElementById("decrypt").checked) {
				document.getElementById("encodediv").style.display = "none";
				document.getElementById("encryptdiv").style.display = "none";
				document.getElementById("decodediv").style.display = "none";
				document.getElementById("decryptdiv").style.display = "block";
				document.getElementById("decodetext").value = "";
				document.getElementById("decodedtext").value = "";
				document.getElementById("encodetext").value = "";
				document.getElementById("encodedtext").value = "";
				document.getElementById("encrypttext").value = "";
				document.getElementById("ekey").value = "";
				document.getElementById("encryptedtext").value = "";
			} else if (document.getElementById("decode").checked) {
				document.getElementById("decodediv").style.display = "block";
				document.getElementById("encodediv").style.display = "none";
				document.getElementById("encryptdiv").style.display = "none";
				document.getElementById("decryptdiv").style.display = "none";
				document.getElementById("encodetext").value = "";
				document.getElementById("encodedtext").value = "";
				document.getElementById("encrypttext").value = "";
				document.getElementById("ekey").value = "";
				document.getElementById("encryptedtext").value = "";
				document.getElementById("decrypttext").value = "";
				document.getElementById("dkey").value = "";
				document.getElementById("decryptedtext").value = "";
			}else {
				document.getElementById("encodediv").style.display = "none";
				document.getElementById("encryptdiv").style.display = "none";
				document.getElementById("decryptdiv").style.display = "none";
				document.getElementById("decodediv").style.display = "none";
			}
		}
		
		function validateForm()
		{
			if (document.getElementById("encode").checked) {
				var encodeText = document.getElementById("encodetext").value.trim();
				if (encodeText == null || encodeText == "") {
					alert("Please enter encodeText  ");
					return false;
				}
			}
			else if (document.getElementById("decode").checked) {
				var decodeText = document.getElementById("decodetext").value.trim();
				if (decodeText == null || decodeText == "") {
					alert("Please enter decodeText  ");
					return false;
				}
			}else if (document.getElementById("encrypt").checked) {
				var encryptText = document.getElementById("encrypttext").value.trim();
				var encryptKey = document.getElementById("ekey").value.trim();
				if (encryptText == null || encryptText == "") {
					alert("Please enter encryptText  ");
					return false;
				}
				else if(encryptKey == null || encryptKey == "") {
					alert("Please enter encryptKey  ");
					return false;
				}
				

			} else if (document.getElementById("decrypt").checked) {
				var decryptText = document.getElementById("decrypttext").value.trim();
				var decryptKey = document.getElementById("dkey").value.trim();
				if (decryptText == null || decryptText == "") {
					alert("Please enter decryptText  ");
					return false;
				}
				else if(decryptKey == null || decryptKey == "") {
					alert("Please enter decryptKey  ");
					return false;
				}
				
			} 
		}
	</script>
</body>
</html>