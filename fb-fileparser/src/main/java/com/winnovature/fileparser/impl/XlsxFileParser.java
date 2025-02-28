package com.winnovature.fileparser.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.lang.StringUtils;
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

//import com.karix.upfileprocessor.utils.dto.SendSMSTypes;
import com.winnovature.fileparser.interfaces.FileParser;
import com.winnovature.fileparser.interfaces.ParseFileHandler;
import com.winnovature.fileparser.util.Constants;
import com.winnovature.fileparser.util.MessageUtil;
import com.winnovature.utils.dtos.SendSMSTypes;

public class XlsxFileParser implements FileParser {

	private static Log log = LogFactory.getLog(Constants.loggerName);
	private static Log loggerCheck = LogFactory.getLog(Constants.loggerNameCheck);
	private ParseFileHandler listener;
	private OPCPackage xlsxPackage;
	private int limit = -1;
	private int columnsLimit = -1;
	private boolean chkMsgType = true;
	private List<String> headers;
	private String mobileColumnName;
	private Map<String,String> headersFound = new HashMap<String,String>();
	private String numericValuePattern;
	private boolean isIndexBasedTemplate = false;
	
	public XlsxFileParser(File file, ParseFileHandler listener)
			throws InvalidFormatException, FileNotFoundException, IOException {
		this.listener = listener;
		this.xlsxPackage = OPCPackage.open(file, PackageAccess.READ);
	}

	@SuppressWarnings("deprecation")
	public XlsxFileParser(InputStream stream, ParseFileHandler listener)
			throws InvalidFormatException, FileNotFoundException, IOException {
		this.listener = listener;

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
	public void parse() throws IOException, OpenXML4JException,
			ParserConfigurationException, SAXException,
			org.apache.poi.openxml4j.exceptions.OpenXML4JException, Exception {

		String loggerName = " XlsxFileParser [parse] ";
		
		if(log.isDebugEnabled()){
			log.debug(" [XlsxFileParser] [parse] Begin.");
			log.debug(" [XlsxFileParser] [parse] chkMsgType:"+chkMsgType);
		}
		long sourceFileCount = 0;
		
		ReadOnlySharedStringsTable strings = new ReadOnlySharedStringsTable(
				xlsxPackage);
		XSSFReader xssfReader = new XSSFReader(xlsxPackage);
		StylesTable styles = xssfReader.getStylesTable();
		XSSFReader.SheetIterator iter = (XSSFReader.SheetIterator) xssfReader
				.getSheetsData();
		if (iter.hasNext()) {
			InputStream stream = iter.next();
			InputSource sheetSource = new InputSource(stream);
			SAXParserFactory saxFactory = SAXParserFactory.newInstance();
			SAXParser saxParser = saxFactory.newSAXParser();
			XMLReader sheetParser = saxParser.getXMLReader();
			String fileName = "";
			try {
				// setting filewriter 
				fileName = listener.start();
				listener.setFileWriter(fileName);
				XLSXFileHandler handler = new XLSXFileHandler(styles, strings,
						-1, limit, columnsLimit, listener,chkMsgType,headers,mobileColumnName,numericValuePattern,isIndexBasedTemplate);
				sheetParser.setContentHandler(handler);
				sheetParser.parse(sheetSource);
				listener.end();
				sourceFileCount = handler.getTotal();
			} catch(FileNotFoundException fnfe){
				log.error(loggerName+" FileNotFoundException"
						+ " occured. Check file is there in disc or not."+fileName+fnfe);
				deleteFile(fileName);
				throw fnfe;
			} catch (Exception e) {
				log.error(loggerName+" Exception",e);
				deleteFile(fileName);
				throw e;
			}finally {
				stream.close();
			}
		}
		
		if(loggerCheck.isDebugEnabled())
			loggerCheck.debug(loggerName+" Count:"+sourceFileCount);
		
		
		if(log.isDebugEnabled()){
			log.debug(" [XlsxFileParser] [parse] End.");
		}
	}

	@Override
	public void registerEventListener(ParseFileHandler listener) {
		this.listener = listener;
	}

	@Override
	public void setDelimiter(String delimiter) {
		// TODO Auto-generated method stub

	}
	
	@Override
	public void setLimit(int limit) {
		this.limit = limit;

	}

	@Override
	public void setColumnsLimit(int columnsLimit) {
		this.columnsLimit = columnsLimit;
		
	}
	
	@Override
	public void setMsgTypeCheck(boolean chkmsgType){
		this.chkMsgType = chkmsgType;
	}

	@Override
	public void setTemplateHeaders(List<String> headers) {
		this.headers = headers;
	}

	@Override
	public void setMobileColumnName(String name) {
		this.mobileColumnName = name;		
	}
	
	@Override
	public void deleteFile(String filename) {
		String loggerName = " [TextFileParser] [deleteFile] ";
		try {
			boolean fileDeletion = listener.deleteFile(filename);
			if (fileDeletion) {
				log.error(loggerName + " " + filename + " Deletion success");
			} else {
				log.error(loggerName + " Unable to delete file " + filename);
			}
		} catch (Exception e1) {
			log.error(loggerName + " Unable to delete file " + filename);
		}
	}
	
	
	@Override
	public void setNumericValuePattern(String numericValuePattern) {
		this.numericValuePattern = numericValuePattern;
	}

	@Override
	public void setIndexBasedTemplateFlag(boolean isIndexBasedTemplate) {
		this.isIndexBasedTemplate = isIndexBasedTemplate;		
	}

}

class XLSXFileHandler extends DefaultHandler {
	private static Log log = LogFactory.getLog(Constants.loggerName);
	/**
	 * Derived from http://poi.apache.org/spreadsheet/how-to.html#xssf_sax_api
	 * <p/>
	 * Also see Standard ECMA-376, 1st edition, part 4, pages 1928ff, at
	 * http://www.ecma-international.org/publications/standards/Ecma-376.htm
	 * <p/>
	 * A web-friendly version is http://openiso.org/Ecma/376/Part4
	 */

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
	private boolean chkMsgType = true;
	
	// Gathers characters as they are seen.
	private StringBuffer value;
	private int minColumns = -1;
	public String line = "";
	private ArrayList<String> lineList = new ArrayList<String>();
	private ParseFileHandler listener;
	
	private List<String> requiredColumns = new ArrayList<String>();
	private Map<Integer,String> indexedColumns = new HashMap<Integer,String>();
	private Map<String,String> requiredColumnsData = new HashMap<String,String>();
	private boolean isHeaderRow = false;
	Map<Integer, String> lineDataMap = new HashMap<Integer, String>();
	private String mobileColumnName;
	private Map<String,String> headersFound = new HashMap<String,String>();
	boolean isMobileColumnFound = false;
	long fileRowsCount = 0;
	String numericValuePattern = null;
	int mobileColumnIndex = -1;
	boolean isIndexBasedTemplate = false;
	/**
	 * Accepts objects needed while parsing.
	 *
	 * @param styles
	 *            Table of styles
	 * @param strings
	 *            Table of shared strings
	 * @param cols
	 *            Minimum number of columns to show
	 * @param target
	 *            Sink for output
	 */
	public XLSXFileHandler(StylesTable styles,
			ReadOnlySharedStringsTable strings, int cols, int limit, int columnsLimit,
			ParseFileHandler listener,boolean chkMsgType, List<String> headers, String mobileColumn, String numericValuePattern,
			boolean isIndexBasedTemplate) {
		this.stylesTable = styles;
		this.sharedStringsTable = strings;
		this.value = new StringBuffer();
		this.nextDataType = xssfDataType.NUMBER;
		this.formatter = new DataFormatter();
		this.minColumns = cols;
		this.limit = limit;
		this.listener = listener;
		this.columnsLimit = columnsLimit;
		this.chkMsgType = chkMsgType;
		this.requiredColumns = headers;
		this.mobileColumnName = mobileColumn;
		this.numericValuePattern = numericValuePattern;
		this.isIndexBasedTemplate = isIndexBasedTemplate;
		
		if(isIndexBasedTemplate) {
			try {
				isMobileColumnFound = true;
				mobileColumnIndex = Integer.parseInt(mobileColumnName);
				for(String col : requiredColumns) {
					if(!col.equalsIgnoreCase(com.winnovature.utils.utils.Constants.UNSUPPORTED_ROW_HEADER)) {
						int ind = Integer.parseInt(col);
						indexedColumns.put(ind, String.valueOf(ind));
						headersFound.put(String.valueOf(ind), String.valueOf(ind));
					}
				}
				listener.setHeaders(requiredColumns);
				listener.writeHeaders();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String,
	 * java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	@Override
	public void startElement(String uri, String localName, String name,
			Attributes attributes) throws SAXException {

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
					this.formatString = BuiltinFormats
							.getBuiltinFormat(this.formatIndex);
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
	public void endElement(String uri, String localName, String name)
			throws SAXException {
		String loggerName = " XlsxFileParser [endElement] ";
		String thisStr = null;
		String logName = "XlsxFileParser" + "[endElement]";
		

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
				//thisStr = value.toString();
				if(mobileColumnIndex >= 0 && mobileColumnIndex == thisColumn) {
					try {
						thisStr = new DecimalFormat(numericValuePattern).format(value.toString());
					}catch(Exception e) {
						log.error(loggerName+" Unable to parse cell value "+value.toString()+" using DecimalFormat, Exception:"+e.getMessage());
						thisStr = value.toString();
					}
				}else {
					thisStr = value.toString();
				}
				
				break;

			case INLINESTR:
				// TODO: have seen an example of this, so it's untested.
				XSSFRichTextString rtsi = new XSSFRichTextString(
						value.toString());
				thisStr = rtsi.toString();
				break;

			case SSTINDEX:
				String sstIndex = value.toString();
				try {
					int idx = Integer.parseInt(sstIndex);
					XSSFRichTextString rtss = new XSSFRichTextString(
							sharedStringsTable.getEntryAt(idx));
					thisStr = rtss.toString();
				} catch (NumberFormatException ex) {
					log.error(loggerName+"Failed to parse SST index '" + sstIndex
							+ "': " + ex.toString());
				}
				break;

			case NUMBER:
				//thisStr = value.toString();
				// dont do any formating(to handle exponential numbers). Let them go to platform as it is.
				
				String n = value.toString();
				if(mobileColumnIndex >= 0 && mobileColumnIndex == thisColumn) {
					try {
						thisStr = new DecimalFormat(numericValuePattern).format(Double.parseDouble(n));
					}catch(Exception e) {
						log.error(loggerName+" Unable to parse cell value "+n+" using DecimalFormat,  Exception:"+e.getMessage());
						thisStr = n;
					}
				}else {
					if (this.formatString != null) {
						thisStr = formatter.formatRawCellContents(Double.parseDouble(n), this.formatIndex, this.formatString);
					} else {
						thisStr = n;
					}
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
			
			if(isIndexBasedTemplate) {
				lastColumnNumber = lastColumnNumber+1;
				// pls note that file index(lastColumnNumber) starts from zero where as in template placeholders(requiredColumns) starts from one
				if(indexedColumns.containsKey(lastColumnNumber)) {
					requiredColumnsData.put(indexedColumns.get(lastColumnNumber), thisStr.trim());
					isHeaderRow = false;
				}
			}else {
				if(requiredColumns.contains(thisStr.trim().toUpperCase()) && totalCount == -1) {
					indexedColumns.put(lastColumnNumber, thisStr.trim().toUpperCase());
					requiredColumnsData.put(thisStr.trim().toUpperCase(), thisStr.trim());
					isHeaderRow = true;
					if(mobileColumnName!=null && mobileColumnName.equalsIgnoreCase(thisStr.trim().toUpperCase())) {
						isMobileColumnFound = true;
						mobileColumnIndex = thisColumn;
					}
					headersFound.put(thisStr.toUpperCase(), thisStr);
				}else if(indexedColumns.containsKey(lastColumnNumber)) {
					requiredColumnsData.put(indexedColumns.get(lastColumnNumber), thisStr.trim());
					isHeaderRow = false;
				}else if(requiredColumns.size() > 0 && fileRowsCount == 0) {
					isHeaderRow = true;
				}
			}

			if (columnsLimit == -1 || lineList.size() < columnsLimit) {
				lineList.add(thisStr);
				lineDataMap.put(lastColumnNumber, thisStr);
			}
			
			SendSMSTypes smsType = listener.getFileDetails().getSendSMSTypes();
			if(totalCount == -1 && (smsType == SendSMSTypes.MTM ||  smsType == SendSMSTypes.OTM)) {
				mobileColumnIndex = 0;
			}
			
			
		} else if ("row".equals(name)) {

			// Print out any missing commas if needed
			if (minColumns > 0) {
				// Columns are 0 based
				if (lastColumnNumber == -1) {
					lastColumnNumber = 0;
				}
			}
			
			if(lineList.size() == 0) {
				lineList.clear();
				lineDataMap.clear();
				lastColumnNumber = -1;
				requiredColumnsData.clear();
				return;
			}
			
			SendSMSTypes smsType = listener.getFileDetails().getSendSMSTypes();
			fileRowsCount++;
			String mobile = "";
			String specialChar = "";
			
			if(isHeaderRow) {
				try {
					if(!isMobileColumnFound) {
						throw new Exception(Constants.MOBILE_NOTFOUND_IN_FILE);
					}else {
						// remove unmatched (not available in file) headers
						if(requiredColumns.size() > requiredColumnsData.keySet().size()) {
							requiredColumns.retainAll(requiredColumnsData.keySet());
							requiredColumns.add(0,com.winnovature.utils.utils.Constants.UNSUPPORTED_ROW_HEADER);
						}
					}
					listener.setHeaders(requiredColumns);
					listener.writeHeaders();
					requiredColumnsData.clear();
				}catch(Exception e) {
					log.error(logName + "Exception: ",e);
					if(com.winnovature.fileparser.util.Constants.MOBILE_NOTFOUND_IN_FILE.equalsIgnoreCase(e.getMessage())) {
						throw new SAXException(Constants.MOBILE_NOTFOUND_IN_FILE);
					}
				}
			}else {
				if(requiredColumns.size() > 0) {
					lineList = new ArrayList<String>();
					for(String col : requiredColumns) {
						// do not include data for row status column as it should be decided by ChopHandler by verifying content
						if(!col.equalsIgnoreCase(com.winnovature.utils.utils.Constants.UNSUPPORTED_ROW_HEADER)) {
							String data = requiredColumnsData.get(col);
							if(data!=null) {
								lineList.add(requiredColumnsData.get(col));
							}else if(col.equalsIgnoreCase(mobileColumnName)) {
								lineList.add("");
							}else {
								//  Write empty column
								lineList.add("");
							}
						}
					}
					requiredColumnsData.clear();
				}
			}
			
			// For FMM - 1st column = mobile,  2nd column = message
			// For FOM - 1st column = mobile
			
			if(smsType == SendSMSTypes.MTM) {
				lineList.clear();
				if(lineDataMap.containsKey(0)) {
					lineList.add(lineDataMap.get(0));
				}else {
					lineList.add("");
				}
				
				if(lineDataMap.containsKey(1)) {
					lineList.add(lineDataMap.get(1));
				}else {
					lineList.add("");
				}
			}else if(smsType == SendSMSTypes.OTM) {
				lineList.clear();
				if(lineDataMap.containsKey(0)) {
					lineList.add(lineDataMap.get(0));
				}
			}
			
			boolean fmmOrFomWithHeader = false;
			if(totalCount == -1 && (smsType == SendSMSTypes.MTM ||  smsType == SendSMSTypes.OTM)) {
				if(lineList != null && lineList.size() > 0 && MessageUtil.isHeader(lineList.get(0))) {
					// don't write header to split file. ignore the row and continue.
					lineList.clear();
					fmmOrFomWithHeader = true;
				}
			}
			
			if(lineList != null && lineList.size() > 0 && !isHeaderRow) {
				
				mobile = lineList.get(0);
				// no need to lookup for spl chars in mobile
				/*
				if (!mobile.matches("[0-9]+"))// if mobile contains special characters
				{
					char allCharacters[] = removeDuplicates(mobile.replaceAll("[a-zA-Z0-9]", "")).toCharArray();// get characters
					int maxOcurrence = 0;

					for (char temp : allCharacters) {
						if (StringUtils.countMatches(mobile,String.valueOf(temp)) > maxOcurrence) {
							specialChar = "\\" + String.valueOf(temp);
							maxOcurrence = StringUtils.countMatches(mobile,	String.valueOf(temp));
						}
					}
				}
				*/
				
				List<String> strMobileNumbers = new ArrayList<String>();
				if (!specialChar.isEmpty()) {
					strMobileNumbers = Arrays.asList(mobile.split(specialChar));
				} else {
					strMobileNumbers.add(mobile);
				}
				
				for (int n = 0; n < strMobileNumbers.size(); n++) {
					try {
						String mobileString = strMobileNumbers.get(n);
						lineList.set(0, mobileString.trim());
						if(chkMsgType) { // normal
							listener.newLine(lineList.toArray(new String[lineList.size()]));
						}else{
							listener.newLineUC(lineList.toArray(new String[lineList.size()]));
						}	
					}catch(Exception e) {
						log.error(logName + "Exception: ",e);
					}
				}
			}
			
			if(isHeaderRow || fmmOrFomWithHeader) {
				totalCount = 0;
			}else {
				totalCount++;
			}
			
			lineList.clear();
			lineDataMap.clear();
			lastColumnNumber = -1;
		}
	}
	
	private String removeDuplicates(String s) {
		StringBuilder noDupes = new StringBuilder();
		for (int i = 0; i < s.length(); i++) {
			String si = s.substring(i, i + 1);
			if (noDupes.indexOf(si) == -1) {
				noDupes.append(si);
			}
		}
		return noDupes.toString();
	}

	public int getTotal() {
		return totalCount;
	}

	/**
	 * Captures characters only if a suitable element is open. Originally was
	 * just "v"; extended for inlineStr also.
	 */
	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
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
