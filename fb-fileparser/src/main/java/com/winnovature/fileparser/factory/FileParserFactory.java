package com.winnovature.fileparser.factory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

import com.winnovature.fileparser.impl.CsvReaderFileParser;
import com.winnovature.fileparser.impl.TextFileParser;
import com.winnovature.fileparser.impl.XlsFileParser;
import com.winnovature.fileparser.impl.XlsxFileParser;
import com.winnovature.fileparser.impl.ZipFileParser;
import com.winnovature.fileparser.interfaces.FileParser;
import com.winnovature.fileparser.interfaces.ParseFileHandler;

public class FileParserFactory {

	private FileParserFactory() {
	}

	/**
	 * @param string
	 * @param listener
	 * @return
	 * @throws IOException
	 * @throws FileNotFoundException
	 * @throws InvalidFormatException
	 */
	public static FileParser get(String fileName,
			ParseFileHandler listener) throws Exception,
			IOException, InvalidFormatException {
		String extension = fileName.substring(fileName.lastIndexOf(".") + 1,
				fileName.length());
		FileParser parser = null;
		File file = new File(fileName);

		if ("txt".equalsIgnoreCase(extension)
				|| "csv".equalsIgnoreCase(extension))
			parser = new CsvReaderFileParser(file, listener);
		else if ("xls".equalsIgnoreCase(extension))
			parser = new XlsFileParser(file, listener);
		else if ("xlsx".equalsIgnoreCase(extension))
			parser = new XlsxFileParser(file, listener);
		else if ("zip".equalsIgnoreCase(extension))
			parser = new ZipFileParser(file, listener);

		if (parser == null) {
			throw new RuntimeException("no parser found for the file type: "
					+ extension);
		}
		return parser;
	}

	/**
	 * @param file
	 * @param listener
	 * @return
	 * @throws IOException
	 * @throws FileNotFoundException
	 * @throws InvalidFormatException
	 */
	public static FileParser get(File file, ParseFileHandler listener)
			throws Exception, IOException, InvalidFormatException {
		String extension = file.getName().substring(
				file.getName().lastIndexOf(".") + 1, file.getName().length());
		FileParser parser = null;

		if ("txt".equalsIgnoreCase(extension)
				|| "csv".equalsIgnoreCase(extension))
			parser = new TextFileParser(file, listener);
		else if ("xls".equalsIgnoreCase(extension))
			parser = new XlsFileParser(file, listener);
		else if ("xlsx".equalsIgnoreCase(extension))
			parser = new XlsxFileParser(file, listener);
		else if ("zip".equalsIgnoreCase(extension))
			parser = new ZipFileParser(file, listener);

		if (parser == null) {
			throw new RuntimeException("no parser found for the file type: "
					+ extension);
		}

		return parser;
	}

	/**
	 * @param file
	 * @param listener
	 * @param type
	 * @return
	 * @throws IOException
	 * @throws FileNotFoundException
	 * @throws InvalidFormatException
	 */
	public static FileParser get(InputStream stream, String extension,
			ParseFileHandler listener) throws Exception,
			IOException, InvalidFormatException {
		FileParser parser = null;

		if ("txt".equalsIgnoreCase(extension)
				|| "csv".equalsIgnoreCase(extension))
			parser = new TextFileParser(stream, listener);
		else if ("xls".equalsIgnoreCase(extension))
			parser = new XlsFileParser(stream, listener);
		else if ("xlsx".equalsIgnoreCase(extension))
			parser = new XlsxFileParser(stream, listener);
		else if ("zip".equalsIgnoreCase(extension))
			parser = new ZipFileParser(stream, listener);

		if (parser == null) {
			throw new RuntimeException("no parser found for the file type: "
					+ extension);
		}

		return parser;
	}

	/**
	 * @param file
	 * @param listener
	 * @param type
	 * @return
	 * @throws IOException
	 * @throws FileNotFoundException
	 * @throws InvalidFormatException
	 */
	public static FileParser get(File file, String extension,
			ParseFileHandler listener) throws Exception,
			IOException, InvalidFormatException {
		FileParser parser = null;

		if ("txt".equalsIgnoreCase(extension)
				|| "csv".equalsIgnoreCase(extension))
			parser = new TextFileParser(file, listener);
		else if ("xls".equalsIgnoreCase(extension))
			parser = new XlsFileParser(file, listener);
		else if ("xlsx".equalsIgnoreCase(extension))
			parser = new XlsxFileParser(file, listener);
		else if ("zip".equalsIgnoreCase(extension))
			parser = new ZipFileParser(file, listener);

		if (parser == null) {
			throw new RuntimeException("no parser found for the file type: "
					+ extension);
		}
		return parser;
	}
}
