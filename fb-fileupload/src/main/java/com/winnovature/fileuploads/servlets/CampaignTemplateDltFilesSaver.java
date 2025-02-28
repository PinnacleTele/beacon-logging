package com.winnovature.fileuploads.servlets;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
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

import com.winnovature.fileuploads.services.FileReadService;
import com.winnovature.fileuploads.utils.Constants;
import com.winnovature.fileuploads.utils.Utility;
import com.winnovature.fileuploads.utils.ZipHandler;
import com.winnovature.utils.singletons.ConfigParamsTon;
import com.winnovature.utils.utils.JsonUtility;

@WebServlet(name = "CampaignTemplateDLTFilesSaver", urlPatterns = "/dlttemplateplaceholders")
@MultipartConfig
public class CampaignTemplateDltFilesSaver extends HttpServlet {

	private static final long serialVersionUID = 1L;
	static Log log = LogFactory.getLog(Constants.FileUploadLogger);
	Map<String, String> configMap = null;

	public CampaignTemplateDltFilesSaver() {
		super();
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		if (log.isDebugEnabled()) {
			log.debug("[CampaignTemplateDltFilesSaver] [doPost] request received.");
		}
		List<String> filesList = new ArrayList<String>();
		boolean sentToTrackingRedis = false;
		String username = null;
		String requestFrom = Constants.CAMPAIGN;
		Instant startTime = Instant.now();
		List<Map<String, Object>> response = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> failedFiles = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> successFiles = new ArrayList<Map<String, Object>>();
		Map<String, Object> finalResponse = new HashMap<String, Object>();
		resp.setContentType("application/json; charset=utf-8");
		PrintWriter out = resp.getWriter();
		String originalFileName = null;
		String badRequest = "Bad Request";
		String internalServerError = "Internal Server Error";
		String serverError = "Server Error";
		try {
			username = req.getParameter("username");
			String ttype = req.getParameter("t_type");
			String mobileColumn = req.getParameter("mobile");
			resp.setStatus(400);
			if (StringUtils.isBlank(username)) {
				finalResponse.put("statusCode", Constants.ERROR_CODE_REQUIRED_PARAMS_MISSING);
				finalResponse.put("error", badRequest);
				finalResponse.put("message", "username is required");
				if (log.isDebugEnabled()) {
					log.debug("[CampaignTemplateDltFilesSaver] [doPost] username not found in the request.");
				}
			} else if (StringUtils.isBlank(ttype)) {
				finalResponse.put("statusCode", Constants.ERROR_CODE_REQUIRED_PARAMS_MISSING);
				finalResponse.put("error", badRequest);
				finalResponse.put("message", "t_type is required");
				if (log.isDebugEnabled()) {
					log.debug("[CampaignTemplateDltFilesSaver] [doPost] mobile column/index not found in the request.");
				}
			} else if (StringUtils.isBlank(mobileColumn)) {
				finalResponse.put("statusCode", Constants.ERROR_CODE_REQUIRED_PARAMS_MISSING);
				finalResponse.put("error", badRequest);
				finalResponse.put("message", "mobile column/index is required");
				if (log.isDebugEnabled()) {
					log.debug("[CampaignTemplateDltFilesSaver] [doPost] mobile column/index not found in the request.");
				}
			} else if (isInputInvalid(ttype, mobileColumn)) {
				finalResponse.put("statusCode", Constants.ERROR_CODE_REQUIRED_PARAMS_MISSING);
				finalResponse.put("error", badRequest);
				finalResponse.put("message", "Invalid mobile index "+mobileColumn);
				if (log.isDebugEnabled()) {
					log.debug("[CampaignTemplateDltFilesSaver] [doPost] mobile column/index not found in the request.");
				}
			} else {
				resp.setStatus(500);

				String mobileColumnName = mobileColumn.trim().toLowerCase();

				configMap = (HashMap<String, String>) ConfigParamsTon.getInstance().getConfigurationFromconfigParams();
				String fileStoreLocation = configMap.get(Constants.CAMPAIGNS_FILE_STORE_PATH);

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
								.concat("_" + uuid.toString())
								.concat("_"
										+ com.winnovature.utils.utils.Utility.getCustomDateAsString("yyyy-MM-dd_HHmmssSSS"))
								.concat(extension);
						part.write(fileStoreLocation + tempFileName);
						com.winnovature.utils.utils.Utility.storeCSVFile(fileStoreLocation + tempFileName,
								fileStoreLocation + storedFileName);
						filesList.add(fileStoreLocation + tempFileName);
					} else {
						part.write(fileStoreLocation + storedFileName);
					}

					if (log.isDebugEnabled()) {
						log.debug("[CampaignTemplateDltFilesSaver] [doPost] time taken to save " + originalFileName
								+ " is " + Utility.getTimeDifference(startTime) + " milliseconds.");
					}
					if (originalFileName.endsWith(".zip")) {
						Instant zipExtractStartTime = Instant.now();
						List<Map<String, Object>> zipContent = new ZipHandler()
								.extractZipFileContent(fileStoreLocation + storedFileName, fileStoreLocation);
						if (zipContent.size() > 0) {
							response.addAll(zipContent);
						}
						if (log.isDebugEnabled()) {
							log.debug("[CampaignTemplateDltFilesSaver] [doPost] time taken to extract "
									+ originalFileName + " is " + Utility.getTimeDifference(zipExtractStartTime)
									+ " milliseconds.");
						}
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

				List<FutureTask<Map<String, Object>>> taskList = new ArrayList<FutureTask<Map<String, Object>>>();
				int index = 0;
				for (Map<String, Object> map : response) {
					Callable<Map<String, Object>> callable = new FileReadService(map, fileStoreLocation, true);
					// Create the FutureTask with Callable
					taskList.add(index, new FutureTask<Map<String, Object>>(callable));
					// As it implements Runnable, create Thread with FutureTask
					Thread t = new Thread(taskList.get(index));
					t.start();
					index++;
				}
				
				// push filenames to redis for cleaning purpose
				sentToTrackingRedis = Utility.sendFilesToTrackingRedis(requestFrom, username, filesList);

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
							result.put("message", Constants.UNSUPPORTED_FILE_TYPE);
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
				// remove below files with 0 rows and add them to failedFiles
				Iterator<Map<String, Object>> itr = successFiles.listIterator();
				while (itr.hasNext()) {
					Map<String, Object> map = itr.next();
					long thisFileCount = Long.parseLong(map.get("count").toString());
					if (thisFileCount < 1) {
						// remove files with 0 rows
						Map<String, Object> result = new HashMap<String, Object>();
						result.put("error", "Invalid File");
						result.put("message", "File is empty");
						result.put("filename", map.get("filename"));
						failedFiles.add(result);
						itr.remove();
					}
				}

				itr = successFiles.listIterator();
				while (itr.hasNext()) {
					Map<String, Object> map = itr.next();
					long thisFileCount = Long.parseLong(map.get("count").toString());
					boolean mobileColumnFound = true;
					if (thisFileCount > 0) {
						total += thisFileCount;
						map.remove("statusCode");
						if (map.get("file_contents_column") != null) {
							List<List<String>> columns = (List<List<String>>) map.remove("file_contents_column");
							List<String> headers = columns.get(0);
							List<String> data = columns.get(1);
							Map<String, String> headerAndDataPairFromFile = new HashMap<String, String>();
							for (int i = 0; i < headers.size(); i++) {
								headerAndDataPairFromFile.put(headers.get(i).toLowerCase(), data.get(i));
							}
							map.put("placeholders_column", headerAndDataPairFromFile);
							if(ttype.equalsIgnoreCase("column")) {
								headers.replaceAll(String::toLowerCase);
								if (!headers.contains(mobileColumnName)) {
									mobileColumnFound = false;
									map.put("message", "Missing Mobile Column " + mobileColumnName);
								}
							}
						}

						if (map.get("file_contents_index") != null) {
							List<List<String>> columns = (List<List<String>>) map.remove("file_contents_index");
							List<String> headers = columns.get(0);
							List<String> data = columns.get(1);
							Map<String, String> headerAndDataPairFromFile = new HashMap<String, String>();
							for (int i = 0; i < headers.size(); i++) {
								headerAndDataPairFromFile.put(headers.get(i), data.get(i));
							}
							map.put("placeholders_index", headerAndDataPairFromFile);
							if(ttype.equalsIgnoreCase("index")) {
								headers.replaceAll(String::toLowerCase);
								if (!headers.contains(mobileColumnName)) {
									mobileColumnFound = false;
									map.put("message", "Missing Mobile Index " + mobileColumnName);
								}
							}
						}
						
						// remove files do not have mobile column and add to failedFiles
						if (!mobileColumnFound) {
							map.put("error", "Invalid File");
							//map.put("message", "Missing Mobile Column : " + mobileColumnName);
							failedFiles.add(map);
							itr.remove();
						}
					}
				}

				String total_human = Utility.humanReadableNumberFormat(total);
				Map<String, Object> nestedResponse = new HashMap<String, Object>();
				nestedResponse.put("success", successFiles);
				nestedResponse.put("failed", failedFiles);

				finalResponse.put("total", total);
				finalResponse.put("total_human", total_human);
				finalResponse.put("uploaded_files", nestedResponse);
				finalResponse.put("statusCode", Constants.SUCCESS_STATUS_CODE);
				resp.setStatus(200);

			}

			String json = new JsonUtility().mapToJson(finalResponse);
			out.print(json);
			if (log.isDebugEnabled()) {
				log.debug("[CampaignTemplateDltFilesSaver] [doPost] time taken to process request is "
						+ Utility.getTimeDifference(startTime) + " milliseconds and response is " + json);
			}
		} catch (Exception e) {
			log.error("[CampaignTemplateDltFilesSaver] [doPost] Exception", e);
			if (!sentToTrackingRedis) {
				// push filenames to redis for cleaning purpose
				Utility.sendFilesToTrackingRedis(requestFrom, username, filesList);
			}
			resp.setStatus(500);
			Map<String, Object> errorResponse = new HashMap<String, Object>();
			errorResponse.put("statusCode", Constants.INTERNAL_SERVER_ERROR_STATUS_CODE);
			errorResponse.put("error", internalServerError);
			errorResponse.put("message", serverError);
			String json = new JsonUtility().mapToJson(errorResponse);
			out.print(json);
		}
		out.flush();
	}
	
	private boolean isInputInvalid(String ctype, String input) {
		boolean result = false;
		if (ctype.equalsIgnoreCase("index")) {
			try {
				Integer.parseInt(input);
			}catch(Exception e) {
				result = true;
			}
		}
		return result;
	}

	private String getFileName(Part part) {
		for (String content : part.getHeader("content-disposition").split(";")) {
			if (content.trim().startsWith("filename"))
				return content.substring(content.indexOf("=") + 2, content.length() - 1);
		}
		return null;
	}

}
