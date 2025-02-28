package com.winnovature.fileuploads.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.errorlog.FileUploadLog;
import com.winnovature.fileuploads.services.FileReadService;
import com.winnovature.fileuploads.utils.Constants;
import com.winnovature.fileuploads.utils.Utility;
import com.winnovature.fileuploads.utils.ZipHandler;
import com.winnovature.utils.singletons.ConfigParamsTon;
import com.winnovature.utils.utils.JsonUtility;

@WebServlet(name = "FilesSaver", urlPatterns = "/save")
@MultipartConfig
public class FilesSaver extends HttpServlet {

	private static final long serialVersionUID = 1L;
	Map<String, String> configMap = null;

	public FilesSaver() {
		super();
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
	
		
		FileUploadLog.getInstance().debug("[FilesSaver] [doPost] request received.");
		List<String> filesList = new ArrayList<String>();
		boolean sentToTrackingRedis = false;
		String username = null;
		String requestFrom = null;
		Instant startTime = Instant.now();
		List<Map<String, Object>> response = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> failedFiles = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> successFiles = new ArrayList<Map<String, Object>>();
		Map<String, Object> finalResponse = new HashMap<String, Object>();
		resp.setContentType("application/json; charset=utf-8");
		PrintWriter out = resp.getWriter();
		String originalFileName = null;
		try {

			username = req.getParameter("username");
			requestFrom = req.getParameter("frompage");
			if (StringUtils.isBlank(username)) {
				finalResponse.put("statusCode", Constants.APPLICATION_ERROR_CODE);
				finalResponse.put("code", Constants.APPLICATION_ERROR_CODE);
				finalResponse.put("error", Constants.APPLICATION_ERROR);
				finalResponse.put("message", "username is required");
			} else if (StringUtils.isBlank(requestFrom)) {
				finalResponse.put("statusCode", Constants.APPLICATION_ERROR_CODE);
				finalResponse.put("code", Constants.APPLICATION_ERROR_CODE);
				finalResponse.put("error", Constants.APPLICATION_ERROR);
				finalResponse.put("message", "frompage is required");
			} else {
				configMap = (HashMap<String, String>) ConfigParamsTon.getInstance().getConfigurationFromconfigParams();
				String fileStoreLocation = configMap.get(Constants.FILE_STORE_PATH);
				if (requestFrom.equalsIgnoreCase(Constants.CAMPAIGN)) {
					fileStoreLocation = configMap.get(Constants.CAMPAIGNS_FILE_STORE_PATH);
				} else if (requestFrom.equalsIgnoreCase(Constants.GROUP)) {
					fileStoreLocation = configMap.get(Constants.GROUP_FILE_STORE_PATH);
				}
				// creating folder with username and storing files inside it.
				fileStoreLocation = fileStoreLocation + username.toLowerCase() + "/";
				Files.createDirectories(Paths.get(fileStoreLocation));

				for (Part part : req.getParts()) {
					originalFileName = getFileName(part);
					if (originalFileName == null) {
						continue;
					}
					String extension = "." + FilenameUtils.getExtension(originalFileName);
					UUID uuid = UUID.randomUUID();
					String storedFileName = StringUtils.replace(originalFileName, extension, "")
							.concat("_" + uuid.toString()).concat(extension);

					if (extension.equalsIgnoreCase(".csv")) {
						String tempFileName = StringUtils.replace(originalFileName, extension, "")
								.concat("_" + uuid.toString()).concat("_" + com.winnovature.utils.utils.Utility
										.getCustomDateAsString("yyyy-MM-dd_HHmmssSSS"))
								.concat(extension);
						part.write(fileStoreLocation + tempFileName);
						com.winnovature.utils.utils.Utility.storeCSVFile(fileStoreLocation + tempFileName,
								fileStoreLocation + storedFileName);
						filesList.add(fileStoreLocation + tempFileName);
					} else {
						part.write(fileStoreLocation + storedFileName);
					}

				
					FileUploadLog.getInstance().debug("[FilesSaver] [doPost] time taken to save " + originalFileName + " is "
							+ Utility.getTimeDifference(startTime) + " milliseconds.");


					if (originalFileName.endsWith(".zip")) {
						Instant zipExtractStartTime = Instant.now();
						List<Map<String, Object>> zipContent = new ZipHandler()
								.extractZipFileContent(fileStoreLocation + storedFileName, fileStoreLocation);
						if (zipContent.size() > 0) {
							response.addAll(zipContent);
						}
					
						FileUploadLog.getInstance().debug("[FilesSaver] [doPost] time taken to extract " + originalFileName + " is "
								+ Utility.getTimeDifference(zipExtractStartTime) + " milliseconds.");
						
						if (storedFileName != null && storedFileName.trim().length() > 0) {
							filesList.add(fileStoreLocation + storedFileName);
						}
					} else {
						Map<String, Object> data = new HashMap<String, Object>();
						data.put("filename", originalFileName);
						data.put("r_filename", storedFileName);
						response.add(data);
					}
				}

				// to handle exception cases (issue starting future tasks) gathering filenames
				// separately
				for (Map<String, Object> map : response) {
					if (map != null && map.get("r_filename") != null
							&& map.get("r_filename").toString().trim().length() > 0) {
						filesList.add(fileStoreLocation + map.get("r_filename"));
					}
				}

				
				FileUploadLog.getInstance().debug("filesList : "+filesList);
				List<FutureTask<Map<String, Object>>> taskList = new ArrayList<FutureTask<Map<String, Object>>>();
				int index = 0;
				for (Map<String, Object> map : response) {
					Callable<Map<String, Object>> callable = new FileReadService(map, fileStoreLocation, false);
					// Create the FutureTask with Callable
					taskList.add(index, new FutureTask<Map<String, Object>>(callable));
					// As it implements Runnable, create Thread with FutureTask
					Thread t = new Thread(taskList.get(index));
					t.start();
					index++;
				}

				// push filenames to redis for cleaning purpose
				sentToTrackingRedis = Utility.sendFilesToTrackingRedis(requestFrom, username, filesList);

				FileUploadLog.getInstance().debug("sentToTrackingRedis : "+sentToTrackingRedis);
				int completedTasks = 0;
				while (taskList.size() > completedTasks) {
					for (FutureTask<Map<String, Object>> futureTask : taskList) {
						if (futureTask.isDone()) {
							completedTasks++;
						}
					}
					if (completedTasks >= taskList.size()) {
						break;
					} else {
						completedTasks = 0;
						Thread.sleep(100);
					}
				}

				for (FutureTask<Map<String, Object>> futureTask : taskList) {
					try {
						Map<String, Object> result = futureTask.get();
						successFiles.add(result);
					} catch (Exception e) {
						Map<String, Object> result = new HashMap<String, Object>();
						if (e.getMessage().contains(Constants.UNSUPPORTED_FILE_TYPE)) {
							result.put("error", Constants.UNSUPPORTED_FILE_TYPE);
							if (StringUtils.split(e.getMessage(), "~").length > 1) {
								result.put("filename", StringUtils.split(e.getMessage(), "~")[1].trim());
							}
						} else {
							throw e;
						}
						failedFiles.add(result);
					}
				}

				long total = 0;
				for (Map<String, Object> map : successFiles) {
					total += Long.parseLong(map.get("count").toString());
				}
				String total_human = Utility.humanReadableNumberFormat(total);
				Map<String, Object> nestedResponse = new HashMap<String, Object>();
				nestedResponse.put("success", successFiles);
				nestedResponse.put("failed", failedFiles);

				finalResponse.put("total", total);
				finalResponse.put("total_human", total_human);
				finalResponse.put("uploaded_files", nestedResponse);
				finalResponse.put("statusCode", Constants.SUCCESS_STATUS_CODE);
				
				FileUploadLog.getInstance().debug("finalResponse : "+finalResponse);

			}

			String json = new JsonUtility().mapToJson(finalResponse);
			out.print(json);
			FileUploadLog.getInstance().debug("[FilesSaver] response json :  "+json);

	
			FileUploadLog.getInstance().debug("[FilesSaver] [doPost] time taken to process request " + originalFileName + " is "
					+ Utility.getTimeDifference(startTime) + " milliseconds.");
	
			
		} catch (Exception e) {
			FileUploadLog.getInstance().error("[FilesSaver] [doPost] Exception", e);
	

			if (!sentToTrackingRedis) {
				// push filenames to redis for cleaning purpose
				Utility.sendFilesToTrackingRedis(requestFrom, username, filesList);
			}
			resp.setStatus(500);
			Map<String, Object> errorResponse = new HashMap<String, Object>();
			errorResponse.put("statusCode", Constants.INTERNAL_SERVER_ERROR_STATUS_CODE);
			errorResponse.put("code", Constants.INTERNAL_SERVER_ERROR_STATUS_CODE);
			errorResponse.put("error", Constants.INTERNAL_SERVER_ERROR);
			errorResponse.put("message", Constants.GENERAL_ERROR_MESSAGE);
			String json = new JsonUtility().mapToJson(errorResponse);
			out.print(json);
			FileUploadLog.getInstance().error("[FilesSaver] response json :  "+json);

		}
		out.flush();
	}

	private String getFileName(Part part) {
		for (String content : part.getHeader("content-disposition").split(";")) {
			if (content.trim().startsWith("filename"))
				return content.substring(content.indexOf("=") + 2, content.length() - 1);
		}
		return null;
	}

}
