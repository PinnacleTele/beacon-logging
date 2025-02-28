package com.winnovature.dltfileprocessor.fileparser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.configuration.PropertiesConfiguration;
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

import com.winnovature.dltfileprocessor.daos.DltTemplateMasterDAO;
import com.winnovature.dltfileprocessor.daos.DltTemplateRequestDAO;
import com.winnovature.dltfileprocessor.singletons.DltFileProcessorPropertiesTon;
import com.winnovature.dltfileprocessor.utils.Constants;
import com.winnovature.dltfileprocessor.utils.Utility;
import com.winnovature.utils.utils.UploadedFilesTrackingUtility;

import au.com.bytecode.opencsv.CSVWriter;

public class XlsxFileParser implements FileParser {

	private static Log log = LogFactory.getLog(Constants.FileUploadLogger);
	private OPCPackage xlsxPackage;
	private int limit = -1;
	private int columnsLimit = -1;
	boolean isFileUpload = false;
	private File file = null;
	private List<String> fileHeaders = new ArrayList<String>();
	private int startIndex = 0;
	Map<String, String> columnMapping = null;
	Map<String, String> requestMap = null;
	List<String> clientIds = null;
	private String telco = null;
	private String username = null;

	public XlsxFileParser(File file, boolean isFileUpload, int startIndex, String telco, String username)
			throws InvalidFormatException, FileNotFoundException, IOException {
		this.xlsxPackage = OPCPackage.open(file, PackageAccess.READ);
		this.isFileUpload = isFileUpload;
		this.file = file;
		this.startIndex = startIndex;
		this.telco = telco;
		this.username = username;
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
		CSVWriter csvWriter = null;
		List<String> filesList = new ArrayList<String>();
		String requestFrom = Constants.DLT;

		PropertiesConfiguration props = DltFileProcessorPropertiesTon.getInstance().getPropertiesConfiguration();
		UUID uuid = UUID.randomUUID();
		char delim = FileParser.delimiter.charAt(0);
		String failedDataFile = file.getParent() + "/" + uuid.toString() + ".csv";
		csvWriter = new CSVWriter(new FileWriter(failedDataFile, true), delim, '"');
		filesList.add(failedDataFile);

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
				XLSXFileHandler handler = new XLSXFileHandler(styles, strings, -1, limit, columnsLimit, isFileUpload,
						startIndex, fileHeaders, columnMapping, requestMap, csvWriter, clientIds, telco);
				sheetParser.setContentHandler(handler);
				sheetParser.parse(sheetSource);
				sourceFileCount = handler.getTotal();
				fileHeaders = handler.getHeadersList();
				Map<String, String> summary = handler.getCounts();
				long total = Long.parseLong(summary.get("total"));
				String valid = summary.get("valid");
				String invalid = summary.get("invalid");
				String failed = summary.get("failed");
				String duplicate = summary.get("duplicate");
				Map<String, String> requestSummary = null;
				if (!isFileUpload && Long.parseLong(failed) > 0) {
					// total rows include header also, remove it from total
					total--;

					Map<String, String> request = new HashMap<String, String>();
					request.put("dtr_id", requestMap.get("dtr_id"));
					request.put("dtf_id", requestMap.get("dtf_id"));
					request.put("total", total + "");
					request.put("valid", valid);
					request.put("invalid", invalid);
					request.put("failed_fileloc", failedDataFile);
					request.put("failed", failed);
					request.put("duplicate", duplicate);
					new DltTemplateRequestDAO().reprocessFailedRows(request);
					requestSummary = request;
				} else if (!isFileUpload) {
					// total rows include header also, remove it from total
					total--;

					Map<String, String> campaignFilesData = new HashMap<String, String>();
					campaignFilesData.put("dtf_id", requestMap.get("dtf_id"));
					campaignFilesData.put("status", props.getString("status.completed"));
					campaignFilesData.put("instance_id", props.getString("instance.monitoring.id"));
					campaignFilesData.put("total", total + "");
					campaignFilesData.put("valid", valid);
					campaignFilesData.put("invalid", invalid);
					campaignFilesData.put("failed", failed);
					campaignFilesData.put("duplicate", duplicate);
					new DltTemplateRequestDAO().updateDltFilesStatus(campaignFilesData);
					requestSummary = campaignFilesData;
				}
				
				UploadedFilesTrackingUtility.setUploadedFilesInfo(requestFrom, username, filesList);
				
				if (log.isDebugEnabled())
					log.debug(loggerName + " RequestSummary:" + requestSummary);
				
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

		if (log.isDebugEnabled()) {
			log.debug(" [XlsxFileParser] [parse] Count:" + sourceFileCount);
			log.debug(loggerName + " end. Time taken to read file " + file.getName() + " is "
					+ Utility.getTimeDifference(startTime) + " milliseconds.");
		}

		return sourceFileCount;
	}

	@Override
	public List<String> getHeadersList() {
		return fileHeaders;
	}

	@Override
	public void setTemplateHeaders(List<String> headers) {
		this.fileHeaders = headers;
	}

	@Override
	public void setColumnMapping(Map<String, String> columnMapping) {
		this.columnMapping = columnMapping;
	}

	@Override
	public void setRequestObject(Map<String, String> requestMap) {
		this.requestMap = requestMap;
	}

	@Override
	public void setSharedClientIds(List<String> clientIds) {
		this.clientIds = clientIds;
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
	private List<String> fileHeaders = new ArrayList<String>();
	List<String> columnDataIndex = new ArrayList<String>();
	private boolean isFileUpload = false;
	int startIndex = 0;
	private List<String> requiredColumns = new ArrayList<String>();
	private Map<Integer, String> indexedColumns = new HashMap<Integer, String>();
	private Map<String, String> requiredColumnsData = new HashMap<String, String>();
	private Map<String, String> requestMap = new HashMap<String, String>();
	private boolean isHeaderRow = false;
	String[] headerRow = null;
	private List<Integer> headerIndexes = new ArrayList<Integer>();
	DltTemplateMasterDAO dltTemplateMasterDAO = new DltTemplateMasterDAO();
	CSVWriter csvWriter = null;
	long failedRowsCount = 0;
	long successRowsCount = 0;
	long invalidCount = 0;
	long duplicateCount = 0;
	Map<String, String> columnMapping = null;
	List<String> clientIds = null;
	String telco = null;

	/**
	 * Accepts objects needed while parsing.
	 *
	 * @param styles  Table of styles
	 * @param strings Table of shared strings
	 * @param cols    Minimum number of columns to show
	 * @param target  Sink for output
	 */
	public XLSXFileHandler(StylesTable styles, ReadOnlySharedStringsTable strings, int cols, int limit,
			int columnsLimit, boolean isFileUpload, int startIndex, List<String> fileHeaders,
			Map<String, String> columnMapping, Map<String, String> requestMap, CSVWriter csvWriter,
			List<String> clientIds, String telco) {
		this.stylesTable = styles;
		this.sharedStringsTable = strings;
		this.value = new StringBuffer();
		this.nextDataType = xssfDataType.NUMBER;
		this.formatter = new DataFormatter();
		this.minColumns = cols;
		this.limit = limit;
		this.isFileUpload = isFileUpload;
		this.startIndex = startIndex;
		this.fileHeaders = fileHeaders;
		this.columnMapping = columnMapping;
		this.requestMap = requestMap;
		if (this.fileHeaders != null && this.fileHeaders.size() > 0) {
			this.requiredColumns = fileHeaders;
		}
		this.csvWriter = csvWriter;
		this.clientIds = clientIds;
		this.telco = telco;
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
					// thisStr = new BigDecimal(value.toString()).toPlainString();
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
				lineDataMap.put(lastColumnNumber, thisStr.trim());
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
			
			if (!isFileUpload) {
				for (Integer ind : lineDataMap.keySet()) {
					String data = lineDataMap.get(ind);
					if(data!=null) {
						data = data.trim();
					}
					if (requiredColumns.contains(data.toUpperCase()) && fileRowsCount == startIndex) {
						indexedColumns.put(ind, data.toUpperCase());
						requiredColumnsData.put(data.toUpperCase(), data);
						isHeaderRow = true;
						headerIndexes.add(ind);
					} else if (indexedColumns.containsKey(ind)) {
						requiredColumnsData.put(indexedColumns.get(ind), data);
						isHeaderRow = false;
					} else if (requiredColumns.size() > 0 && fileRowsCount == startIndex) {
						isHeaderRow = true;
					}
				}
			}

			if (isHeaderRow) {
				int index = 0;
				headerRow = new String[headerIndexes.size()];
				for (int ind : headerIndexes) {
					headerRow[index++] = indexedColumns.get(ind);
				}
			}

			if (isFileUpload) {
				if (fileRowsCount == startIndex) {
					for (int index : lineDataMap.keySet()) {
						fileHeaders.add(lineDataMap.get(index).trim().toLowerCase());
					}
				}
			} else {
				if (!isHeaderRow && fileRowsCount > startIndex && requiredColumns.size() > 0) {
					if(telco.equalsIgnoreCase("custom")) {
						requiredColumnsData.put("entity_id", requiredColumnsData.get("ENTITY_ID"));
					}else {
						requiredColumnsData.put("entity_id", requestMap.get("entity_id"));
					}
					requiredColumnsData.put("telco", telco);
					requiredColumnsData.put("template_group_id", requestMap.get("template_group_id"));
					requiredColumnsData.put("fileloc", requestMap.get("fileloc"));
					requiredColumnsData.put("cli_id", requestMap.get("cli_id"));
					requiredColumnsData.put("dtf_id", requestMap.get("dtf_id"));
					try {
						int invalid = dltTemplateMasterDAO.insertIntoDltTemplateGroupHeaderEntityMapAndDltTemplateInfo(
								requiredColumnsData, columnMapping);
						if (invalid > 0) {
							invalidCount++;
						} else {
							if (requiredColumnsData.get("is_duplicate") != null) {
								duplicateCount++;
							} else {
								successRowsCount++;
							}
						}
					} catch (Exception e) {
						if (failedRowsCount == 0) {
							csvWriter.writeNext(headerRow);
						}
						failedRowsCount++;
						int index = 0;
						String[] nextLine = new String[headerIndexes.size()];
						for (int ind : headerIndexes) {
							nextLine[index++] = requiredColumnsData.get(indexedColumns.get(ind));
						}
						csvWriter.writeNext(nextLine);
					}
				}
			}

			lineDataMap.clear();
			requiredColumnsData.clear();
			lastColumnNumber = -1;
		}
	}

	public long getTotal() {
		return fileRowsCount;
	}

	public List<String> getHeadersList() {
		return fileHeaders;
	}

	public Map<String, String> getCounts() {
		Map<String, String> summary = new HashMap<String, String>();
		summary.put("total", fileRowsCount + "");
		summary.put("valid", successRowsCount + "");
		summary.put("invalid", invalidCount + "");
		summary.put("failed", failedRowsCount + "");
		summary.put("duplicate", duplicateCount + "");
		return summary;
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
