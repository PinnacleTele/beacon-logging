package com.winnovature.utils.utils;

import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.itextos.beacon.commonlib.utility.MessageConvertionUtility;
import com.itextos.beacon.platform.messagetool.MsgIdentifierUtil;
import com.itextos.beacon.platform.messagetool.Response;


public enum UnicodeUtil {
	INSTANCE;
	
	public String convertHexStringToString(String hexString) {
		if(StringUtils.isNotBlank(hexString)) {
			return MessageConvertionUtility.convertHex2String(hexString);
		}
		return hexString;
	}

	public String convertStringIntoHex(String str) {
		if(StringUtils.isNotBlank(str)) {
			return MessageConvertionUtility.convertString2HexString(str);
		}
		return str;
	}

	public boolean isUnicode_old(String s1) {
		Charset encoding = Charset.forName("Cp1252");
		byte[] b = s1.getBytes(encoding);
		String s2 = new String(b, encoding);
		if (s1.equals(s2))
			return false;
		else
			return true;
	}
	
	public boolean isUnicode(String message) {
		boolean isMsgUnicode = false;
		String ENCODER_FORMAT = "UTF-8";
		for (final char lChar : message.toCharArray()) {
			try {
				final String lEncodedString = URLEncoder.encode(String.valueOf(lChar), ENCODER_FORMAT);

				if (lEncodedString.length() > 3) {
					isMsgUnicode = true;
				}

			} catch (final Exception e) {}
		}
		return isMsgUnicode;
	}
	
	public boolean isHexString(String s1) {
		if(StringUtils.isNotBlank(s1)) {
			return MessageConvertionUtility.isHexContent(s1.trim());
		}
		return false;
	}
	
	public String convertToHexString(List<String> splitData, String delimiter) {
		List<String> splittedMsg = new ArrayList<String>();

		for (String data : splitData) {
			splittedMsg.add(convertStringIntoHex(data));
		}

		return StringUtils.join(splittedMsg, delimiter);
	}
	
	public Response identifyMessage(String aClientId, int aAccountLevelSplCharLength, int aAccountLevelOccuranceCount,
			boolean aRemoveUcCharsInPlainMessage, String aMessage) {
		Response messageIdentifier = null;
		try {
			messageIdentifier = MsgIdentifierUtil.messageIdentifier(aClientId, aAccountLevelSplCharLength,
					aAccountLevelOccuranceCount, aRemoveUcCharsInPlainMessage, aMessage);
		} catch (Exception e) {
			throw e;
		} catch (Throwable t) {
			throw t;
		}
		return messageIdentifier;
	}
	
	public String toHex(String msg, List<String> list, String delimiter, Map<String, String> metadata) {
		boolean aRemoveUcCharsInPlainMessage = false;
		String hex = "";
		try {
			if (StringUtils.isNotBlank(metadata.get("is_remove_uc_chars"))
					&& metadata.get("is_remove_uc_chars").equalsIgnoreCase("1")) {
				aRemoveUcCharsInPlainMessage = true;
			}
			if (StringUtils.isNotBlank(msg)) {
				Response response = identifyMessage(metadata.get("cli_id"), Integer.parseInt(metadata.get("uc_iden_char_len")),
						Integer.parseInt(metadata.get("uc_iden_occur")), aRemoveUcCharsInPlainMessage, msg);
				if(response.isIsUniCode()) {
					hex = response.getMessage();
				}
			} else if (list != null && list.size() > 0) {
				List<String> splittedMsg = new ArrayList<String>();
				for (String data : list) {
					Response resp = identifyMessage(metadata.get("cli_id"), Integer.parseInt(metadata.get("uc_iden_char_len")),
							Integer.parseInt(metadata.get("uc_iden_occur")), aRemoveUcCharsInPlainMessage, data);
					if(resp.isIsUniCode()) {
						splittedMsg.add(resp.getMessage());
					}
				}
				hex = StringUtils.join(splittedMsg, delimiter);
			}
		} catch (Exception e) {
			throw e;
		} catch (Throwable t) {
			throw t;
		}
		return hex;
	}
	
	
	public String toHexString(String msg, List<String> list, String delimiter, Map<String, String> metadata) {
		boolean aRemoveUcCharsInPlainMessage = false;
		String hex = "false~";
		try {
			if (StringUtils.isNotBlank(metadata.get("is_remove_uc_chars"))
					&& metadata.get("is_remove_uc_chars").equalsIgnoreCase("1")) {
				aRemoveUcCharsInPlainMessage = true;
			}
			if (StringUtils.isNotBlank(msg)) {
				Response response = identifyMessage(metadata.get("cli_id"), Integer.parseInt(metadata.get("uc_iden_char_len")),
						Integer.parseInt(metadata.get("uc_iden_occur")), aRemoveUcCharsInPlainMessage, msg);
				if(response.isIsUniCode()) {
					hex = "true~"+response.getMessage();
				}else {
					hex = "false~"+msg;
				}
			} else if (list != null && list.size() > 0) {
				List<String> splittedMsg = new ArrayList<String>();
				boolean isUC = false;
				for (String data : list) {
					Response resp = identifyMessage(metadata.get("cli_id"), Integer.parseInt(metadata.get("uc_iden_char_len")),
							Integer.parseInt(metadata.get("uc_iden_occur")), aRemoveUcCharsInPlainMessage, data);
					if(resp.isIsUniCode()) {
						splittedMsg.add(resp.getMessage());
						isUC = true;
					}else {
						splittedMsg.add(data);
					}
				}
				hex = StringUtils.join(splittedMsg, delimiter);
				if(isUC) {
					hex = "true~"+hex;
				}else {
					hex = "false~"+hex;
				}
			}
		} catch (Exception e) {
			throw e;
		} catch (Throwable t) {
			throw t;
		}
		return hex;
	}

	private List<String> splitStringAndPlaceholders(String original) {
		List<String> arrayList = new ArrayList<String>();
		String placeHolder = "{#var#}";

		if (StringUtils.contains(original, placeHolder)) {
			String originalTemp = original;

			while (originalTemp.length() > 0) {
				int openIndex = StringUtils.indexOf(originalTemp, placeHolder);
				int closeIndex = openIndex + placeHolder.length();

				if (openIndex < 0 || closeIndex < 0) {
					if (originalTemp.length() > 0) {
						arrayList.add(originalTemp);
					}
					break;
				}

				arrayList.add(originalTemp.substring(0, openIndex));
				arrayList
						.add(originalTemp.substring(openIndex, closeIndex));

				originalTemp = originalTemp.substring(closeIndex);
			}

		}else {
			arrayList.add(original);
		}

		return arrayList;
	}
	
	public String convertToHexStringIgnoringPlaceholders(String message) {
		List<String> arrayList = splitStringAndPlaceholders(message.trim());
		StringBuffer stringBuf = new StringBuffer();

		for (String data : arrayList) {
			if (StringUtils.contains(data, "{#var#}")) {
				stringBuf.append(data);
			} else {
				stringBuf.append(convertStringIntoHex(data));
			}
		}

		return stringBuf.toString();
	}
	
	public String convertToStringIgnoringPlaceholders(String message) {
		List<String> arrayList = splitStringAndPlaceholders(message.trim());
		StringBuffer stringBuf = new StringBuffer();

		for (String data : arrayList) {
			if (StringUtils.contains(data, "{#var#}")) {
				stringBuf.append(data);
			} else {
				stringBuf.append(convertHexStringToString(data));
			}
		}

		return stringBuf.toString();
	}
	
	public static void main(String[] args) {
		/*
		String msg = "Dear Partner, your complaint is under review. Ref id: {#வணக்கம்#}. -Kurlon";
		System.out.println(msg);
		msg = UnicodeUtil.INSTANCE.convertToHexStringIgnoringPlaceholders(msg);
		System.out.println(msg);
		msg = "004400650061007200200050006100720074006E00650072002C00200079006F0075007200200063006F006D0070006C00610069006E007400200069007300200075006E0064006500720020007200650076006900650077002E0020005200650066002000690064003A0020007B00230BB50BA30B950BCD0B950BAE0BCD0023007D002E0020002D004B00750072006C006F006E";
		msg = UnicodeUtil.INSTANCE.convertToStringIgnoringPlaceholders(msg);
		System.out.println(msg);
		*/
		
		boolean isUnicode = UnicodeUtil.INSTANCE.isUnicode("Your username {#var#} and password is {#var#} – KURLON");
		System.out.println(isUnicode);
		
	}
	
	
}
