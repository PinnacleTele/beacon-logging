package com.winnovature.utils.utils;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.MiddlewareConstant;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.mobilevalidation.MobileNumberValidator;
import com.itextos.beacon.http.interfaceutil.InterfaceUtil;
import com.itextos.beacon.inmemdata.account.UserInfo;

public class MobileValidator {
	static Log log = LogFactory.getLog(Constants.UtilsLogger);
	private static final String className = "[MobileValidator]";

	public static MobileNumberValidator validate(String mobile, String country, boolean isIntlServiceEnabled,
			boolean considerDefaultLengthAsDomestic, boolean isDomesticSpecialSeriesAllow) {
		String methodName = " [validate] ";
		MobileNumberValidator mobileNumberValidator = null;
		try {
			mobileNumberValidator = InterfaceUtil.validateMobile(mobile, country, isIntlServiceEnabled,
					considerDefaultLengthAsDomestic, false, "", isDomesticSpecialSeriesAllow);
		} catch (Exception e) {
			log.error(className + methodName + " Exception ::: ", e);
		} catch (Throwable t) {
			log.error(className + methodName + " Throwable ::: ", t);
			throw t;
		}
		return mobileNumberValidator;
	}

	public static Map<String, Object> getRequiredInfo(UserInfo userInfo) throws Exception {

		boolean isIntlServiceEnabled = false, considerDefaultLengthAsDomestic = false,
				isDomesticSpecialSeriesAllow = false;
		Map<String, String> userDetails = null;
		Map<String, Object> response = new HashMap<String, Object>();

		try {
			userDetails = new JsonUtility().convertJsonStringToMap(userInfo.getAccountDetails());
		} catch (Exception e) {
			throw e;
		}

		String res = userDetails.get("sms~international");
		if (StringUtils.isNotBlank(res)) {
			try {
				isIntlServiceEnabled = Integer.parseInt(res.trim()) == 1 ? true : false;
			} catch (Exception e) {
			}
		}
		res = userDetails.get("considerdefaultlength_as_domestic");
		if (StringUtils.isNotBlank(res)) {
			try {
				considerDefaultLengthAsDomestic = Integer.parseInt(res.trim()) == 1 ? true : false;
			} catch (Exception e) {
			}
		}

		try {
			isDomesticSpecialSeriesAllow = CommonUtility.isEnabled(CommonUtility
					.nullCheck(userDetails.get(MiddlewareConstant.MW_DOMESTIC_SPECIAL_SERIES_ALLOW.getName()), true));
		} catch (Exception e) {
			throw e;
		} catch (Throwable t) {
			throw new Exception(t.getMessage());
		}

		String countryCode = null;
		try {
			countryCode = com.winnovature.utils.utils.Utility.getDefaultCountryCode1();
		} catch (Exception e) {
			throw e;
		}

		response.put("isIntlServiceEnabled", isIntlServiceEnabled);
		response.put("considerDefaultLengthAsDomestic", considerDefaultLengthAsDomestic);
		response.put("isDomesticSpecialSeriesAllow", isDomesticSpecialSeriesAllow);
		response.put("countryCode", countryCode);

		return response;
	}

}
