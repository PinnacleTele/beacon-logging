package com.winnovature.fileuploads.fileparser;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.hssf.usermodel.HSSFDataFormatter;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import com.itextos.beacon.errorlog.FileUploadLog;
import com.winnovature.fileuploads.utils.Constants;
import com.winnovature.fileuploads.utils.Utility;

public class XlsFileParser implements FileParser {

	private static Log log = LogFactory.getLog(Constants.FileUploadLogger);
	private File file;
	private InputStream stream;
	private HSSFWorkbook workbook;
	private HSSFSheet sheet = null;
	private long fileRowsCount = 0;
	List<String> columnDataIndex = new ArrayList<String>();
	private List<List<String>> indexBasedPreviewList = new ArrayList<List<String>>();
	private List<List<String>> columnBasedPreviewList = new ArrayList<List<String>>();
	private boolean isTemplate = false;

	public XlsFileParser(File file, boolean isTemplate) throws Exception {
		this.file = file;
		this.isTemplate = isTemplate;
	}

	@Override
	public long parse() throws Exception {
		String loggerName = "[XlsFileParser] [parse] ";
		if (log.isDebugEnabled()) {
			log.debug(loggerName + "  Begin.");
		}
		Instant startTime = Instant.now();
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(file);
			if (file != null) {
				workbook = new HSSFWorkbook(fis);
			} else if (stream != null) {
				workbook = new HSSFWorkbook(stream);
			} else {
				if (fis != null) {
					fis.close();
				}
				throw new Exception("No source file provided");
			}
			
			int headerLastColumnNumber = 0;
			Map<Integer, String> lineDataMap = new HashMap<Integer, String>();

			sheet = workbook.getSheetAt(0);
			// Iterate through each rows from first sheet
			Iterator<Row> rowIterator = sheet.iterator();
			while (rowIterator.hasNext()) {
				Row row = rowIterator.next();
				Iterator<Cell> cellIterator = row.cellIterator();
				
				while (cellIterator.hasNext()) {
					Cell cell = (Cell) cellIterator.next();
					String strData = "";
					switch (cell.getCellType()) {
					case STRING:
						strData = cell.getStringCellValue().trim();
						break;
					case NUMERIC:
						strData = new HSSFDataFormatter().formatCellValue(cell);
						break;
					case FORMULA:
						strData = cell.getStringCellValue().trim();
						break;
					} // end of switch
					if (StringUtils.isNotBlank(strData)) {
						lineDataMap.put(cell.getColumnIndex(), strData);
					}
				} // end of while cell Iterator
				
				if(lineDataMap.size() > 0) {
					fileRowsCount++;
				}
				
				if(isTemplate) {
					
					List<String> indexData = new ArrayList<String>();
					List<String> columnData = new ArrayList<String>();
					List<String> indexData1 = new ArrayList<String>();
					
					if (fileRowsCount == 1) {
						headerLastColumnNumber = row.getLastCellNum();
						for (int i = 0; i < headerLastColumnNumber; i++) {
							indexData.add(String.valueOf(i + 1));
							if (lineDataMap.get(i) != null) {
								columnData.add(lineDataMap.get(i));
								columnDataIndex.add(String.valueOf(i));
								indexData1.add(lineDataMap.get(i));
							}else {
								indexData1.add("");
							}
						}
						indexBasedPreviewList.add(indexData);
						indexBasedPreviewList.add(indexData1);
						columnBasedPreviewList.add(columnData);
					}
					
					if (fileRowsCount > 1 && fileRowsCount <= FileParser.previewCount && isTemplate) {
						for (int i = 0; i < headerLastColumnNumber; i++) {
							if (lineDataMap.get(i) != null) {
								indexData.add(lineDataMap.get(i));
							} else {
								indexData.add("");
							}
							if (columnDataIndex.contains(String.valueOf(i))) {
								if (lineDataMap.get(i) != null) {
									columnData.add(lineDataMap.get(i));
								} else {
									columnData.add("");
								}
							}
						}
						indexBasedPreviewList.add(indexData);
						columnBasedPreviewList.add(columnData);
					}
					
				}
				
				lineDataMap.clear();
			} // end of while row iterator

		} catch (Exception e) {
			log.error(loggerName + " Exception", e);
			throw e;
		} finally {
			try {
				workbook.close();
				fis.close();
			} catch (Exception e) {
			}
		}
		
		if (log.isDebugEnabled()) {
			log.debug(" [XlsFileParser] [parse] rows count:" + fileRowsCount);
			log.debug(loggerName + " end. Time taken to read "+file.getName()+" is "+ Utility.getTimeDifference(startTime)+" milliseconds.");
		}
		FileUploadLog.getInstance().debug(" [XlsFileParser] [parse] rows count:" + fileRowsCount);
		FileUploadLog.getInstance().debug(" end. Time taken to read "+file.getName()+" is "+ Utility.getTimeDifference(startTime)+" milliseconds.");

		return fileRowsCount;
	}

	@Override
	public List<List<String>> getIndexBasedPreviewList() {
		return indexBasedPreviewList;
	}

	@Override
	public List<List<String>> getColumnBasedPreviewList() {
		return columnBasedPreviewList;
	}
}
