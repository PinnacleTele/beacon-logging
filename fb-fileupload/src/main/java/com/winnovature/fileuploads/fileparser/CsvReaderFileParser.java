package com.winnovature.fileuploads.fileparser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.winnovature.fileuploads.utils.Constants;
import com.winnovature.fileuploads.utils.Utility;

import au.com.bytecode.opencsv.CSVReader;

public class CsvReaderFileParser implements FileParser {

	private static Log log = LogFactory.getLog(Constants.FileUploadLogger);
	private File file;
	private String delimiter;
	private int limit = -1;
	private int count;
	List<String> columnDataIndex = new ArrayList<String>();
	private List<List<String>> indexBasedPreviewList = new ArrayList<List<String>>();
	private List<List<String>> columnBasedPreviewList = new ArrayList<List<String>>();
	private boolean isTemplate = false;

	public CsvReaderFileParser(File file, boolean isTemplate) throws Exception {
		this.file = file;
		this.count = 0;
		this.isTemplate = isTemplate;
	}

	@Override
	public long parse() throws Exception {
		String loggerName = " [CsvReaderFileParser] [parse] ";
		if (log.isDebugEnabled()) {
			log.debug(loggerName + " begin.");
		}
		Instant startTime = Instant.now();
		CSVReader csvReader = null;
		long fileRowsCount = 0;

		try {
			delimiter = FileParser.delimiter;

			char delim = delimiter.charAt(0);

			if (file != null) {
				csvReader = new CSVReader(new FileReader(file), delim, '"', 0);
			} else {
				throw new FileNotFoundException("No source file provided");
			}

			int headerLastColumnNumber = 0;

			String[] nextLine = null;

			while ((nextLine = csvReader.readNext()) != null) {

				if (limit != -1 && count > limit) {
					break;
				}

				if (nextLine != null && nextLine.length > 0 && StringUtils.isBlank(nextLine[0])) {
					// empty row
					continue;
				} else if (nextLine != null && nextLine.length > 0) {
					fileRowsCount++;
					if (indexBasedPreviewList.size() <= FileParser.previewCount && isTemplate) {
						List<String> indexData = new ArrayList<String>();
						List<String> columnData = new ArrayList<String>();
						List<String> indexData1 = new ArrayList<String>();
						Map<Integer, String> lineDataMap = new HashMap<Integer, String>();
						String[] splits = nextLine;
						int columnIndex = 0;
						for (String split : splits) {
							lineDataMap.put(columnIndex++, split);
						}

						if (fileRowsCount == 1) {
							headerLastColumnNumber = columnIndex;
							for (int i = 0; i < headerLastColumnNumber; i++) {
								indexData.add(String.valueOf(i + 1));
								if (StringUtils.isNotBlank(lineDataMap.get(i))) {
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

						if (fileRowsCount > 1 && fileRowsCount <= FileParser.previewCount) {
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
				} else {
					// empty row
					continue;
				}
			}

			if (log.isDebugEnabled())
				log.debug(loggerName + " filename:" + file + " Count:" + fileRowsCount);

		} catch (FileNotFoundException fnfe) {
			log.error(loggerName + " FileNotFoundException occured. Check " + file + " is there in disc or not.");
			throw fnfe;
		} catch (Exception e) {
			log.error(loggerName + " Exception occured", e);
			throw e;
		} finally {
			if (csvReader != null) {
				csvReader.close();
			}
		}

		if (log.isDebugEnabled()) {
			log.debug(loggerName + " end. Time taken to read file " + file.getName() + " is "
					+ Utility.getTimeDifference(startTime) + " milliseconds.");
		}

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
