package com.winnovature.fileuploads.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
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

import com.winnovature.fileuploads.services.FileReadService;
import com.winnovature.fileuploads.utils.Constants;
import com.winnovature.fileuploads.utils.Utility;
import com.winnovature.utils.singletons.ConfigParamsTon;
import com.winnovature.utils.utils.JsonUtility;

@WebServlet(name = "TemplateFilesSaver", urlPatterns = "/template")
@MultipartConfig
public class TemplateFilesSaver extends HttpServlet {

	private static final long serialVersionUID = 1L;
	static Log log = LogFactory.getLog(Constants.FileUploadLogger);
	Map<String, String> configMap = null;

	public TemplateFilesSaver() {
		super();
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		if(log.isDebugEnabled()) {
			log.debug("[TemplateFilesSaver] [doPost] request received.");
		}
		List<String> filesList = new ArrayList<String>();
		List<Map<String, Object>> response = new ArrayList<Map<String, Object>>();
		Map<String, Object> result = new HashMap<String, Object>();
		resp.setContentType("application/json; charset=utf-8");
		//resp.setCharacterEncoding("utf-8");
		PrintWriter out = resp.getWriter();
		boolean supportedFileType = true;
		String unsupportedFile = null;
		boolean sentToTrackingRedis = false;
		String username = null;
		String requestFrom = Constants.TEMPLATE;
		try {
			username = req.getParameter("username");
			//requestFrom = req.getParameter("frompage");
			if (StringUtils.isBlank(username)) {
				result.put("statusCode", Constants.APPLICATION_ERROR_CODE);
				result.put("code", Constants.APPLICATION_ERROR_CODE);
				result.put("error", Constants.APPLICATION_ERROR);
				result.put("message", "username is required");
			} else if (StringUtils.isBlank(requestFrom)) {
				result.put("statusCode", Constants.APPLICATION_ERROR_CODE);
				result.put("code", Constants.APPLICATION_ERROR_CODE);
				result.put("error", Constants.APPLICATION_ERROR);
				result.put("message", "frompage is required");
			}else {
				configMap = (HashMap<String, String>) ConfigParamsTon.getInstance().getConfigurationFromconfigParams();
				String fileStoreLocation = configMap.get(Constants.TEMPLATE_FILE_STORE_PATH);
				// creating folder with username and storing files inside it.
				fileStoreLocation = fileStoreLocation + username.toLowerCase() + "/";
				Files.createDirectories(Paths.get(fileStoreLocation));
				
				for (Part part : req.getParts()) {
					String originalFileName = getFileName(part);
					if(originalFileName == null) {
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
						com.winnovature.utils.utils.Utility.storeCSVFile(fileStoreLocation + tempFileName, fileStoreLocation + storedFileName);
						filesList.add(fileStoreLocation + tempFileName);
					} else {
						part.write(fileStoreLocation + storedFileName);
					}
					
					filesList.add(fileStoreLocation + storedFileName);
					
					if (originalFileName.endsWith(".zip") || originalFileName.endsWith(".txt")) {
						supportedFileType = false;
						unsupportedFile = originalFileName;
						break;
					} else {
						Map<String, Object> data = new HashMap<String, Object>();
						data.put("filename", originalFileName);
						data.put("r_filename", storedFileName);
						response.add(data);
					}
				}
				
				if(supportedFileType) {
					Map<String, Object> data = response.get(0);
					Callable<Map<String, Object>> callable = new FileReadService(data, fileStoreLocation, true);
					FutureTask<Map<String, Object>> task = new FutureTask<Map<String, Object>>(callable);
					Thread t = new Thread(task);
				    t.start();
				    
				    // push filenames to redis for cleaning purpose
					sentToTrackingRedis = Utility.sendFilesToTrackingRedis(requestFrom, username, filesList);
					
				    while(!task.isDone()) {
				        Thread.sleep(100);
				    }
				    
				    try {
				    	result = task.get();
				    }catch(Exception e) {
				    	result = new HashMap<String, Object>();
				    	result.put("statusCode", Constants.APPLICATION_ERROR_CODE);
				    	result.put("code", Constants.APPLICATION_ERROR_CODE);
						result.put("filename", data.get("filename"));
						if(e.getMessage().contains(Constants.UNSUPPORTED_FILE_TYPE)) {
							result.put("error", Constants.APPLICATION_ERROR);
							result.put("message", Constants.UNSUPPORTED_FILE_TYPE);
						}else {
							throw e;
						}
				    }
				}else {
					result.put("statusCode", Constants.APPLICATION_ERROR_CODE);
					result.put("code", Constants.APPLICATION_ERROR_CODE);
					result.put("error", Constants.APPLICATION_ERROR);
					result.put("message", Constants.UNSUPPORTED_FILE_TYPE);
					result.put("filename", unsupportedFile);
				}
			}
			
			
		    String json = new JsonUtility().mapToJson(result);
			out.print(json);
		} catch (Exception e) {
			log.error("[TemplateFilesSaver] [doPost] Exception", e);
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
