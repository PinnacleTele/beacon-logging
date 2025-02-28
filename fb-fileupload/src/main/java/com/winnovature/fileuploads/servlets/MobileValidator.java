package com.winnovature.fileuploads.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.utility.mobilevalidation.MobileNumberValidator;
import com.itextos.beacon.inmemdata.account.UserInfo;
import com.winnovature.fileuploads.utils.Constants;
import com.winnovature.fileuploads.utils.Utility;
import com.winnovature.utils.utils.JsonUtility;
import com.winnovature.utils.utils.UserDetails;

@WebServlet(name = "MobileValidator", urlPatterns = "/validatemobile")
@MultipartConfig
public class MobileValidator extends HttpServlet {

	private static final long serialVersionUID = 1L;
	static Log log = LogFactory.getLog(Constants.FileUploadLogger);
	Map<String, String> configMap = null;

	public MobileValidator() {
		super();
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		if (log.isDebugEnabled()) {
			log.debug("[MobileValidator] [doPost] request received.");
		}
		Instant startTime = Instant.now();
		Map<String, Object> finalResponse = new HashMap<String, Object>();
		resp.setContentType("application/json; charset=utf-8");
		PrintWriter out = resp.getWriter();
		MobileNumberValidator mobileNumberValidator = null;
		UserInfo userInfo = null;
		String clientId = null, mobile = null;
		List<String> validList = new ArrayList<String>();
		List<String> duplicateList = new ArrayList<String>();
		List<String> invalidList = new ArrayList<String>();
		try {
			clientId = req.getParameter("cli_id");
			mobile = req.getParameter("mobile");

			List<String> mobiList = new ArrayList<String>();

			if (StringUtils.isNotBlank(mobile)) {
				String[] mobiles = StringUtils.split(mobile, ",");
				for (int i = 0; i < mobiles.length; i++) {
					String[] mobi = StringUtils.split(mobiles[i], "\n");
					for (int j = 0; j < mobi.length; j++) {
						mobiList.add(mobi[j].trim());
					}
				}
			}

			if (mobiList.size() > 0) {
				userInfo = UserDetails.getUserInfo(clientId);
				if (log.isDebugEnabled()) {
					log.debug("[MobileValidator] [doPost]  AccountDetails - " + userInfo.getAccountDetails());
				}

				Map<String, Object> info = com.winnovature.utils.utils.MobileValidator.getRequiredInfo(userInfo);
				boolean isIntlServiceEnabled = (boolean) info.get("isIntlServiceEnabled");
				boolean considerDefaultLengthAsDomestic = (boolean) info.get("considerDefaultLengthAsDomestic");
				boolean isDomesticSpecialSeriesAllow = (boolean) info.get("isDomesticSpecialSeriesAllow");
				String countryCode = (String) info.get("countryCode");

				for (String mn : mobiList) {
					mobileNumberValidator = com.winnovature.utils.utils.MobileValidator.validate(mn, countryCode,
							isIntlServiceEnabled, considerDefaultLengthAsDomestic, isDomesticSpecialSeriesAllow);
					if (mobileNumberValidator.isValidMobileNumber()) {
						if(validList.contains(mobileNumberValidator.getMobileNumber())) {
							duplicateList.add(mn);
						}else {
							validList.add(mobileNumberValidator.getMobileNumber());
						}
					} else {
						if(invalidList.contains(mn)) {
							duplicateList.add(mn);
						}else {
							invalidList.add(mn);
						}
					}
				}
			}

			finalResponse.put("total", mobiList.size());
			finalResponse.put("valid", validList);
			finalResponse.put("valid_cnt", validList.size());
			finalResponse.put("invalid", invalidList);
			finalResponse.put("invalid_cnt", invalidList.size());
			finalResponse.put("duplicate", duplicateList);
			finalResponse.put("duplicate_cnt", duplicateList.size());
			finalResponse.put("statusCode", Constants.SUCCESS_STATUS_CODE);
			resp.setStatus(200);

			String json = new JsonUtility().mapToJson(finalResponse);
			out.print(json);
			if (log.isDebugEnabled()) {
				log.debug("[MobileValidator] [doPost] time taken to process request " + mobile + " is "
						+ Utility.getTimeDifference(startTime) + " milliseconds.   Response = "+json);
			}
		} catch (Exception e) {
			log.error("[MobileValidator] [doPost] Exception", e);
			resp.setStatus(500);
			Map<String, Object> errorResponse = new HashMap<String, Object>();
			errorResponse.put("statusCode", Constants.INTERNAL_SERVER_ERROR_STATUS_CODE);
			errorResponse.put("code", Constants.INTERNAL_SERVER_ERROR_STATUS_CODE);
			errorResponse.put("error", Constants.INTERNAL_SERVER_ERROR);
			errorResponse.put("message", Constants.GENERAL_ERROR_MESSAGE);
			String json = new JsonUtility().mapToJson(errorResponse);
			out.print(json);
		}
		out.flush();
	}

}
