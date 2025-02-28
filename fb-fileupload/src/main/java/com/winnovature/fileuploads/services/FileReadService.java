package com.winnovature.fileuploads.services;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.errorlog.FileUploadLog;
import com.winnovature.fileuploads.fileparser.CsvReaderFileParser;
import com.winnovature.fileuploads.fileparser.XlsFileParser;
import com.winnovature.fileuploads.fileparser.XlsxFileParser;
import com.winnovature.fileuploads.utils.Constants;
import com.winnovature.fileuploads.utils.Utility;

public class FileReadService implements Callable<Map<String, Object>> {
	static Log log = LogFactory.getLog(Constants.FileUploadLogger);
	private String fileToBeRead = null;
	Map<String, Object> data = null;
	private boolean isTemplate = false;

	public FileReadService(Map<String, Object> data, String path, boolean isTemplate) {
		this.data = data;
		this.fileToBeRead = path + data.get("r_filename");
		this.isTemplate = isTemplate;
	}

	@Override
	public Map<String, Object> call() throws Exception {
		long count = 0;
		List<List<String>> indexPreviewList = null;
		List<List<String>> columnPreviewList = null;
		try {
			String extension = FilenameUtils.getExtension(fileToBeRead);
			FileUploadLog.getInstance().debug("extension : "+extension);

			switch (extension) {
			case "csv":
				CsvReaderFileParser txtParser = new CsvReaderFileParser(new File(fileToBeRead), isTemplate);
				count = txtParser.parse();
				indexPreviewList = txtParser.getIndexBasedPreviewList();
				columnPreviewList = txtParser.getColumnBasedPreviewList();
				break;
			case "xls":
				XlsFileParser xlsParser = new XlsFileParser(new File(fileToBeRead), isTemplate);
				count = xlsParser.parse();
				indexPreviewList = xlsParser.getIndexBasedPreviewList();
				columnPreviewList = xlsParser.getColumnBasedPreviewList();
				break;
			case "xlsx":
				XlsxFileParser xlsxParser = new XlsxFileParser(new File(fileToBeRead), isTemplate);
				count = xlsxParser.parse();
				indexPreviewList = xlsxParser.getIndexBasedPreviewList();
				columnPreviewList = xlsxParser.getColumnBasedPreviewList();
				break;
			default:
				throw new Exception(Constants.UNSUPPORTED_FILE_TYPE+"~"+data.get("filename"));
			}
			data.put("statusCode", Constants.SUCCESS_STATUS_CODE);
			data.put("count", "" + count);
			data.put("count_human", Utility.humanReadableNumberFormat(count));
			
			FileUploadLog.getInstance().debug("data : "+data);

			
			// added for preview case where file has only 1 row
			if(count > 0 && columnPreviewList!=null && columnPreviewList.size() == 1) {
				List<String> row1 = columnPreviewList.get(0);
				List<String> row2 = new ArrayList<String>();
				for (String cell : row1) {
					row2.add("");
				}
				columnPreviewList.add(row2);
			}
			
			if(isTemplate) {
				data.put("file_contents_index", indexPreviewList);
				data.put("file_contents_column", columnPreviewList);
			}
			
			FileUploadLog.getInstance().debug("data : "+data);

		} catch (Exception e) {
			log.error("[FileReadService] [call] Exception", e);
			FileUploadLog.getInstance().error("[FileReadService] [call] Exception",e);

			throw e;
		}
		return data;
	}

}
