package com.winnovature.fileuploads.fileparser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.openxml4j.opc.ZipPackage;
import org.apache.poi.ss.usermodel.BuiltinFormats;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import com.itextos.beacon.errorlog.FileUploadLog;
import com.winnovature.fileuploads.utils.Constants;
import com.winnovature.fileuploads.utils.Utility;

public class XlsxFileParser implements FileParser {

	private static Log log = LogFactory.getLog(Constants.FileUploadLogger);
	private OPCPackage xlsxPackage;
	private int limit = -1;
	private int columnsLimit = -1;
	private List<List<String>> indexPreviewList;
	private List<List<String>> columnPreviewList;
	boolean isTemplate = false;
	private File file = null;

	public XlsxFileParser(File file, boolean isTemplate) throws InvalidFormatException, FileNotFoundException, IOException {
		this.xlsxPackage = OPCPackage.open(file, PackageAccess.READ);
		this.isTemplate = isTemplate;
		this.file = file;
	}

	@SuppressWarnings("deprecation")
	public XlsxFileParser(InputStream stream) throws InvalidFormatException, FileNotFoundException, IOException {

		if (stream instanceof ZipInputStream) {
			this.xlsxPackage = ZipPackage.open(stream);
		} else {
			this.xlsxPackage = OPCPackage.open(stream);
		}
	}

	/**
	 * Initiates the processing of the XLS workbook file to CSV.
	 * 
	 * @throws IOException
	 * @throws OpenXML4JException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws org.apache.poi.openxml4j.exceptions.OpenXML4JException
	 */
	@Override
	public long parse() throws IOException, OpenXML4JException, ParserConfigurationException, SAXException,
			org.apache.poi.openxml4j.exceptions.OpenXML4JException, Exception {

		String loggerName = " [XlsxFileParser] [parse] ";

		if (log.isDebugEnabled()) {
			log.debug(loggerName + " Begin.");
		}
		long sourceFileCount = 0;
		Instant startTime = Instant.now();

		ReadOnlySharedStringsTable strings = new ReadOnlySharedStringsTable(xlsxPackage);
		XSSFReader xssfReader = new XSSFReader(xlsxPackage);
		StylesTable styles = xssfReader.getStylesTable();
		XSSFReader.SheetIterator iter = (XSSFReader.SheetIterator) xssfReader.getSheetsData();
		if (iter.hasNext()) {
			InputStream stream = iter.next();
			InputSource sheetSource = new InputSource(stream);
			SAXParserFactory saxFactory = SAXParserFactory.newInstance();
			SAXParser saxParser = saxFactory.newSAXParser();
			XMLReader sheetParser = saxParser.getXMLReader();
			String fileName = "";
			try {
				XLSXFileHandler handler = new XLSXFileHandler(styles, strings, -1, limit, columnsLimit, isTemplate);
				sheetParser.setContentHandler(handler);
				sheetParser.parse(sheetSource);
				sourceFileCount = handler.getTotal();
				indexPreviewList = handler.getIndexBasedPreviewList();
				columnPreviewList = handler.getColumnBasedPreviewList();
			} catch (FileNotFoundException fnfe) {
				log.error(loggerName + " FileNotFoundException" + " occured. Check file is there in disc or not."
						+ fileName + fnfe);
				throw fnfe;
			} catch (Exception e) {
				log.error(loggerName + " Exception", e);
				throw e;
			} finally {
				stream.close();
			}
		}

		if(log.isDebugEnabled()) {
			log.debug(" [XlsxFileParser] [parse] Count:" + sourceFileCount);
			log.debug(loggerName + " end. Time taken to read file "+file.getName()+" is "+ Utility.getTimeDifference(startTime)+" milliseconds.");
		}

		FileUploadLog.getInstance().debug(" [XlsxFileParser] [parse] Count:" + sourceFileCount);
		FileUploadLog.getInstance().debug( " end. Time taken to read file "+file.getName()+" is "+ Utility.getTimeDifference(startTime)+" milliseconds.");


		return sourceFileCount;
	}

	@Override
	public List<List<String>> getIndexBasedPreviewList() {
		return indexPreviewList;
	}

	@Override
	public List<List<String>> getColumnBasedPreviewList() {
		return columnPreviewList;
	}

}

class XLSXFileHandler extends DefaultHandler {
	private static Log log = LogFactory.getLog(Constants.FileUploadLogger);

	enum xssfDataType {
		BOOL, ERROR, FORMULA, INLINESTR, SSTINDEX, NUMBER,
	}

	/**
	 * Table with styles
	 */
	private StylesTable stylesTable;

	/**
	 * Table with unique strings
	 */
	private ReadOnlySharedStringsTable sharedStringsTable;

	// Set when V start element is seen
	private boolean vIsOpen;

	// Set when cell start element is seen;
	// used when cell close element is seen.
	private xssfDataType nextDataType;

	// Used to format numeric cell values.
	private short formatIndex;
	private String formatString;
	private final DataFormatter formatter;

	private int thisColumn = -1;
	// The last column printed to the output stream
	private int lastColumnNumber = -1;
	private int totalCount = -1;
	private int limit = -1;
	private int columnsLimit = -1;

	// Gathers characters as they are seen.
	private StringBuffer value;
	private int minColumns = -1;

	Map<Integer, String> lineDataMap = new HashMap<Integer, String>();
	long fileRowsCount = 0;
	private int headerLastColumnNumber = -1;
	private List<List<String>> indexBasedPreviewList = new ArrayList<List<String>>();
	private List<List<String>> columnBasedPreviewList = new ArrayList<List<String>>();
	List<String> columnDataIndex = new ArrayList<String>();
	private boolean isTemplate = false;

	/**
	 * Accepts objects needed while parsing.
	 *
	 * @param styles  Table of styles
	 * @param strings Table of shared strings
	 * @param cols    Minimum number of columns to show
	 * @param target  Sink for output
	 */
	public XLSXFileHandler(StylesTable styles, ReadOnlySharedStringsTable strings, int cols, int limit,
			int columnsLimit, boolean isTemplate) {
		this.stylesTable = styles;
		this.sharedStringsTable = strings;
		this.value = new StringBuffer();
		this.nextDataType = xssfDataType.NUMBER;
		this.formatter = new DataFormatter();
		this.minColumns = cols;
		this.limit = limit;
		this.isTemplate = isTemplate;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String,
	 * java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	@Override
	public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {

		if (limit != -1) {
			if (totalCount == limit)
				throw new NumberFormatException();
		}

		if ("inlineStr".equals(name) || "v".equals(name)) {
			vIsOpen = true;
			value.setLength(0);
		}
		// c => cell
		else if ("c".equals(name)) {
			// Get the cell reference
			String r = attributes.getValue("r");
			int firstDigit = -1;
			for (int c = 0; c < r.length(); ++c) {
				if (Character.isDigit(r.charAt(c))) {
					firstDigit = c;
					break;
				}
			}
			thisColumn = nameToColumn(r.substring(0, firstDigit));

			// Set up defaults.
			this.nextDataType = xssfDataType.NUMBER;
			this.formatIndex = -1;
			this.formatString = null;
			String cellType = attributes.getValue("t");
			String cellStyleStr = attributes.getValue("s");
			if ("b".equals(cellType)) {
				nextDataType = xssfDataType.BOOL;
			} else if ("e".equals(cellType)) {
				nextDataType = xssfDataType.ERROR;
			} else if ("inlineStr".equals(cellType)) {
				nextDataType = xssfDataType.INLINESTR;
			} else if ("s".equals(cellType)) {
				nextDataType = xssfDataType.SSTINDEX;
			} else if ("str".equals(cellType)) {
				nextDataType = xssfDataType.FORMULA;
			} else if (cellStyleStr != null) {
				// It's a number, but almost certainly one
				// with a special style or format
				int styleIndex = Integer.parseInt(cellStyleStr);
				XSSFCellStyle style = stylesTable.getStyleAt(styleIndex);
				this.formatIndex = style.getDataFormat();
				this.formatString = style.getDataFormatString();
				if (this.formatString == null) {
					this.formatString = BuiltinFormats.getBuiltinFormat(this.formatIndex);
				}
			}
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String,
	 * java.lang.String, java.lang.String)
	 */
	@Override
	public void endElement(String uri, String localName, String name) throws SAXException {
		String loggerName = " XlsxFileParser [endElement] ";
		String thisStr = null;

		// v => contents of a cell
		if ("v".equals(name)) {
			// Process the value contents as required.
			// Do now, as characters() may be called more than once
			switch (nextDataType) {

			case BOOL:
				char first = value.charAt(0);
				thisStr = first == '0' ? "FALSE" : "TRUE";
				break;

			case ERROR:
				thisStr = value.toString();
				break;

			case FORMULA:
				// A formula could result in a string value,
				// so always add double-quote characters.
				// thisStr = value.toString();
				thisStr = value.toString();
				break;

			case INLINESTR:
				// TODO: have seen an example of this, so it's untested.
				XSSFRichTextString rtsi = new XSSFRichTextString(value.toString());
				thisStr = rtsi.toString();
				break;

			case SSTINDEX:
				String sstIndex = value.toString();
				try {
					int idx = Integer.parseInt(sstIndex);
					XSSFRichTextString rtss = new XSSFRichTextString(sharedStringsTable.getEntryAt(idx));
					thisStr = rtss.toString();
				} catch (NumberFormatException ex) {
					log.error(loggerName + "Failed to parse SST index '" + sstIndex + "': " + ex.toString());
				}
				break;

			case NUMBER:
				String n = value.toString();
				if (this.formatString != null) {
					thisStr = formatter.formatRawCellContents(Double.parseDouble(n), this.formatIndex,
							this.formatString);
				} else {
					thisStr = n;
				}
				break;

			default:
				thisStr = "(TODO: Unexpected type: " + nextDataType + ")";
				break;
			}

			// Output after we've seen the string contents
			// Emit commas for any fields that were missing on this row
			if (lastColumnNumber == -1) {
				lastColumnNumber = 0;
			}

			// Might be the empty string.
			// output.print(thisStr);

			// Update column
			if (thisColumn > -1) {
				lastColumnNumber = thisColumn;
			}

			if (columnsLimit == -1 || lineDataMap.size() < columnsLimit) {
				lineDataMap.put(lastColumnNumber, thisStr);
			}

		} else if ("row".equals(name)) {

			// Print out any missing commas if needed
			if (minColumns > 0) {
				// Columns are 0 based
				if (lastColumnNumber == -1) {
					lastColumnNumber = 0;
				}
			}

			if (lineDataMap.size() == 0) {
				lastColumnNumber = -1;
				return;
			}

			fileRowsCount++;
			
			if (isTemplate && indexBasedPreviewList.size() <= FileParser.previewCount) {
				
				List<String> indexData = new ArrayList<String>();
				List<String> columnData = new ArrayList<String>();
				List<String> indexData1 = new ArrayList<String>();

				if (fileRowsCount == 1) {
					headerLastColumnNumber = lastColumnNumber;
					for (int i = 0; i <= headerLastColumnNumber; i++) {
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

				if (fileRowsCount > 1 && fileRowsCount <= FileParser.previewCount) {
					for (int i = 0; i <= headerLastColumnNumber; i++) {
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
			lastColumnNumber = -1;
		}
	}

	public long getTotal() {
		return fileRowsCount;
	}

	public List<List<String>> getIndexBasedPreviewList() {
		return indexBasedPreviewList;
	}

	public List<List<String>> getColumnBasedPreviewList() {
		return columnBasedPreviewList;
	}

	/**
	 * Captures characters only if a suitable element is open. Originally was just
	 * "v"; extended for inlineStr also.
	 */
	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		if (vIsOpen) {
			value.append(ch, start, length);
		}
	}

	/**
	 * Converts an Excel column name like "C" to a zero-based index.
	 *
	 * @param name
	 * @return Index corresponding to the specified name
	 */
	private int nameToColumn(String name) {
		int column = -1;
		for (int i = 0; i < name.length(); ++i) {
			int c = name.charAt(i);
			column = (column + 1) * 26 + c - 'A';
		}
		return column;
	}

}
