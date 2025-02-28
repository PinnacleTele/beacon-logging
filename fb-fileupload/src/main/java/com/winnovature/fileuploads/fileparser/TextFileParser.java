package com.winnovature.fileuploads.fileparser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.winnovature.fileuploads.utils.Constants;

public class TextFileParser implements FileParser {

	private static Log log = LogFactory.getLog(Constants.FileUploadLogger);
	private File file;
	private InputStream stream;
	private List<List<String>> indexBasedPreviewList = new ArrayList<List<String>>();
	private List<List<String>> columnBasedPreviewList = new ArrayList<List<String>>();

	public TextFileParser(File file) throws Exception {
		this.file = file;
	}

	@Override
	public long parse() throws Exception {
		String loggerName = " [TextFileParser] [parse] ";
		if (log.isDebugEnabled()) {
			log.debug(loggerName + " begin.");
		}
		BufferedReader reader = null;
		UnicodeReader unocodeReader;
		long fileRowsCount = 0;

		try {
			if (file != null) {
				unocodeReader = new UnicodeReader(new FileInputStream(file), null);
			} else if (stream != null) {
				unocodeReader = new UnicodeReader(stream, null);
			} else {
				throw new Exception("No source file provided");
			}

			unocodeReader.getEncoding();
			unocodeReader.getDefaultEncoding();

			reader = new BufferedReader(unocodeReader);

			String line = null;

			while ((line = reader.readLine()) != null) {
				if (StringUtils.isNotBlank(line)) {
					fileRowsCount++;
				}
			}

			if (log.isDebugEnabled())
				log.debug(loggerName + " filename:" + file + " Count:" + fileRowsCount);

		} catch (FileNotFoundException fnfe) {
			log.error(loggerName + " FileNotFoundException" + " occured. Check " + file + " is there in disc or not.");
			throw fnfe;
		} catch (Exception e) {
			log.error(loggerName + " Exception ", e);
			throw e;
		} finally {
			if (reader != null)
				reader.close();
		}

		if (log.isDebugEnabled()) {
			log.debug(loggerName + " end.");
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
