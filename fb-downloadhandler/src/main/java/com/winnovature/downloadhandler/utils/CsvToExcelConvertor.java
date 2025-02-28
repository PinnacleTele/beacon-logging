package com.winnovature.downloadhandler.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.streaming.SXSSFRow;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import com.winnovature.downloadhandler.daos.DownloadReqDAO;
import com.winnovature.utils.utils.JsonUtility;

import au.com.bytecode.opencsv.CSVReader;
import redis.clients.jedis.Jedis;

public class CsvToExcelConvertor {

	static Log log = LogFactory.getLog(Constants.DownloadHandlerLogger);
	private static final String className = "CsvToExcelConvertor";

	int filesCount = 0;
	int writtenRowsCount = 0;
	int totalWrittenRowsCount = 0;
	int rowsPerFile = 0;

	public void process(Map<String, String> downloadReq) throws Exception {
		String logname = className + " [process] ";
		String zipFile = null;
		List<String> excelFiles = null;

		if (log.isDebugEnabled()) {
			log.debug(logname + " Begin, request info " + downloadReq);
		}

		try {
			rowsPerFile = Integer.parseInt(downloadReq.get("excel_rows_limit"));

			String csvFile = downloadReq.get("csv_download_path");
			String path = FilenameUtils.getFullPath(csvFile);
			zipFile = path + FilenameUtils.getBaseName(csvFile) + ".zip";

			excelFiles = convertCsvFileToExcel(csvFile);

			ZipUtility.archive(excelFiles, zipFile);

			new DownloadReqDAO().updateDownloadReqStatus(downloadReq.get("id"), Constants.PROCESS_STATUS_COMPLETED,
					null, zipFile);

			deleteFiles(excelFiles);

		} catch (Exception e) {
			log.error(logname + " Exception ", e);
			deleteFiles(excelFiles);
			throw e;
		}

		if (log.isDebugEnabled()) {
			log.debug(logname + " End, Zip file " + zipFile);
		}

	}

	public List<String> convertCsvFileToExcel(String csvFile) throws Exception {
		String logname = className + " [convertCsvFileToExcel] ";
		CSVReader csvReader = null;
		String[] headerRow = null;
		boolean isHeaderRow = true;
		List<String> splitFiles = new ArrayList<String>();
		writtenRowsCount = 0;
		totalWrittenRowsCount = 0;
		filesCount = 0;

		try {
			Charset charset = java.nio.charset.Charset.forName("UTF-8");
			if (StringUtils.isNotBlank(csvFile)) {
				csvReader = new CSVReader(new FileReader(csvFile, charset), ',', '"', 0);
			} else {
				throw new Exception("No source file provided");
			}

			SXSSFWorkbook workbook = new SXSSFWorkbook();
			SXSSFSheet spreadsheet = workbook.createSheet();

			String[] row = null;
			while ((row = csvReader.readNext()) != null) {
				if (!isEmptyRow(row)) {

					if (isHeaderRow) {
						headerRow = row;
						writeDataToExcel(spreadsheet, headerRow);
						totalWrittenRowsCount--;
						isHeaderRow = false;
						continue;
					}

					if (writtenRowsCount > rowsPerFile) {
						String newFileName = csvFile.substring(0, csvFile.length() - 4) + "_" + (++filesCount)
								+ ".xlsx";
						FileOutputStream out = new FileOutputStream(new File(newFileName));
						workbook.write(out);
						out.flush();
						out.close();
						workbook.close();

						splitFiles.add(newFileName);

						workbook = new SXSSFWorkbook();
						spreadsheet = workbook.createSheet();

						writtenRowsCount = 0;

						writeDataToExcel(spreadsheet, headerRow);

						totalWrittenRowsCount--;
					}

					writeDataToExcel(spreadsheet, row);
				}
			}

			if (writtenRowsCount > 0) {
				String newFileName = csvFile.substring(0, csvFile.length() - 4) + "_" + (++filesCount) + ".xlsx";
				FileOutputStream out = new FileOutputStream(new File(newFileName));
				workbook.write(out);
				out.flush();
				out.close();
				workbook.close();
				splitFiles.add(newFileName);
			}

		} catch (Exception e) {
			throw e;
		}

		if (log.isDebugEnabled()) {
			log.debug(logname + " Excel (" + splitFiles.size() + ") files created " + splitFiles);
		}
		return splitFiles;
	}

	public boolean isEmptyRow(String[] nextLine) {
		String data = null;
		try {
			List<String> list = Arrays.asList(nextLine);
			data = list.toString().replace("[", "").replaceAll("]", "");
			data = data.replaceAll(",", "").trim();
		} catch (Exception e) {
		}
		return StringUtils.isBlank(data);
	}

	public void writeDataToExcel(SXSSFSheet spreadsheet, Object[] objectArr) throws Exception {
		try {
			SXSSFRow row = spreadsheet.createRow(writtenRowsCount);
			int cellid = 0;
			// writing the data into the sheets...
			for (Object obj : objectArr) {
				Cell cell = row.createCell(cellid++);
				if (obj instanceof String) {
					cell.setCellValue((String) obj);
				} else if (obj instanceof Long) {
					cell.setCellValue((Long) obj);
				} else if (obj instanceof Double) {
					cell.setCellValue((Double) obj);
				}
			}
			writtenRowsCount++;
			totalWrittenRowsCount++;
		} catch (Exception e) {
			throw e;
		}
	}

	public String handleExponentialNumber(String mobile) {
		try {
			if (mobile.toUpperCase().contains("E+") || mobile.toUpperCase().contains("E-")) {
				// could be exponential value. check if it is parsable double value.
				Double.parseDouble(mobile);
				mobile = new BigDecimal(mobile.trim()).toPlainString();
			}
		} catch (Exception e) {
		}
		return mobile;
	}

	public void pushBackToRedis(Map<String, String> downloadReq, Jedis jedis, String queueName, int retryLimit) {
		String logname = className + " [pushBackToRedis] ";

		if (log.isDebugEnabled()) {
			log.debug(logname + " Begin, request info " + downloadReq);
		}
		int retryCount = 1;
		try {
			if (StringUtils.isNotBlank(downloadReq.get("retry_count"))) {
				retryCount = Integer.parseInt(downloadReq.get("retry_count"));
			}

			if (retryCount <= retryLimit) {
				retryCount++;
				downloadReq.put("retry_count", retryCount + "");
				String json = new JsonUtility().convertMapToJSON(downloadReq);
				jedis.rpush(queueName, json);
				if (log.isDebugEnabled()) {
					log.debug(logname + "End, pushed back to Q[" + queueName + "], json=" + json);
				}
			} else {
				new DownloadReqDAO().updateDownloadReqStatus(downloadReq.get("id"), Constants.PROCESS_STATUS_FAILED,
						"Max retry reached", null);

				if (log.isDebugEnabled()) {
					log.debug(logname + "End, updated status to " + Constants.PROCESS_STATUS_FAILED
							+ " in download_req since max retry (" + retryLimit + ") finished.");
				}
			}

		} catch (Exception e) {
			log.error(logname + " Exception ", e);
		}
	}

	public void deleteFiles(List<String> excelFiles) {
		try {
			for (String xl : excelFiles) {
				File file = new File(xl);
				file.delete();
			}
		} catch (Exception e) {
		}
	}

}
