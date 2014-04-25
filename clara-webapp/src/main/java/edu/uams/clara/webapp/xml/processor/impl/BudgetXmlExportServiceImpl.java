package edu.uams.clara.webapp.xml.processor.impl;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFPalette;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.usermodel.HeaderFooter;
import org.apache.poi.hssf.util.CellRangeAddress;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Footer;
import org.apache.poi.ss.usermodel.Header;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import edu.uams.clara.webapp.common.dao.usercontext.UserDao;
import edu.uams.clara.webapp.common.domain.usercontext.User;
import edu.uams.clara.webapp.protocol.dao.budget.code.HospitalChargeProcedureDao;
import edu.uams.clara.webapp.protocol.dao.protocolform.ProtocolFormXmlDataDao;
import edu.uams.clara.webapp.protocol.domain.Protocol;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolFormXmlData;
import edu.uams.clara.webapp.protocol.domain.protocolform.enums.ProtocolFormXmlDataType;
import edu.uams.clara.webapp.xml.processor.BudgetXmlExportService;
import edu.uams.clara.webapp.xml.processor.XmlProcessor;

public class BudgetXmlExportServiceImpl implements BudgetXmlExportService {
	private final static Logger logger = LoggerFactory
			.getLogger(BudgetXmlExportServiceImpl.class);
	private XmlProcessor xmlProcessor;

	private ProtocolFormXmlDataDao protocolFormXmlDataDao;
	private HospitalChargeProcedureDao hospitalChargeProcedureDao;

	private UserDao userDao;
	
	class CycleObj {
		  public String name;
		  public long id;
		  public int stratDay;
		  public Element cycleEle;
		}
	
	private int compare(CycleObj c1, CycleObj c2){
	     if (c1.stratDay!=c2.stratDay) 
	        return c1.stratDay-c2.stratDay;
	     else if (c1.name.compareTo(c2.name) != 0) 
	    	 return c1.name.compareTo(c2.name);
	     else
	    	 return (int) (c1.id - c2.id);
	  }
	
	//sort cycles
    private CycleObj[]  sortCycle(CycleObj[] CycleObjs)  
    {  
    	CycleObj tmp=null ;  
          for(int i=1;i<CycleObjs.length;i++){  
              tmp = CycleObjs[i];  
              int smallpoint=0;   
              int bigpoint=i-1;  
                  
              while(bigpoint>=smallpoint){  
                  int mid=(smallpoint+bigpoint)/2;  
                  if(compare(tmp,CycleObjs[mid])>0){  
                      smallpoint=mid+1;  
                  }else{  
                      bigpoint=mid-1;  
                  }  
              }  
              for(int j=i;j>smallpoint;j--){  
            	  CycleObjs[j]=CycleObjs[j-1];  
              }  
              CycleObjs[bigpoint+1]=tmp;  
          }  
          
          return CycleObjs;
    }  

	@SuppressWarnings("deprecation")
	private void addInfo(Document budget, HSSFWorkbook wb,
			CreationHelper createHelper, BudgetDocumentType budgetDocumentType,
			long IRB, Protocol protocol) {

		// int armNum = 0;
		int visitNum = 0;
		int placeForNotes = 0;
		int procedureCellWidth = 6000;

		NodeList budgetList = budget.getElementsByTagName("budget");
		Element budgetForfa = (Element) budgetList.item(0);
		String fa = budgetForfa.getAttribute("fa");

		Map<Integer, CellStyle> color = new HashMap<Integer, CellStyle>();
		colorStyle(wb, color);
		CellStyle style = wb.createCellStyle();
		CellStyle styleForProList = wb.createCellStyle();
		CellStyle styleForProList2 = wb.createCellStyle();
		CellStyle styleForTitle = wb.createCellStyle();

		styleForTitle = color.get(10);

		NodeList epochList = budget.getElementsByTagName("epoch");

		List<Element> sortedepochList = new ArrayList<Element>();
		List<Integer> sortepochOrnderList = new ArrayList<Integer>();

		// sort the epoch by index
		Map<Integer, Element> epochSortMap = new HashMap<Integer, Element>();
		try {
			for (int i = 0; i < epochList.getLength(); i++) {
				Element epoch = (Element) epochList.item(i);
				int epochOrder = Integer.valueOf(epoch.getAttribute("index"));
				epochSortMap.put(epochOrder, epoch);
				sortepochOrnderList.add(epochOrder);
			}
			Collections.sort(sortepochOrnderList);
			for (int i = 0; i < sortepochOrnderList.size(); i++) {
				sortedepochList
						.add(epochSortMap.get(sortepochOrnderList.get(i)));
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		for (int eopchLen = 0; eopchLen < sortedepochList.size(); eopchLen++) {

			Element epoch = sortedepochList.get(eopchLen);
			// get arms for epoch
			NodeList armList = epoch.getElementsByTagName("arm");

			List<Element> sortedarmList = new ArrayList<Element>();
			List<Integer> sortarmOrnderList = new ArrayList<Integer>();

			// sort the arm by index
			Map<Integer, Element> armSortMap = new HashMap<Integer, Element>();
			try {
				for (int i = 0; i < armList.getLength(); i++) {
					Element arm = (Element) armList.item(i);
					int armOrder = Integer.valueOf(arm.getAttribute("index"));
					armSortMap.put(armOrder, arm);
					sortarmOrnderList.add(armOrder);
				}
				Collections.sort(sortarmOrnderList);
				for (int i = 0; i < sortarmOrnderList.size(); i++) {
					sortedarmList.add(armSortMap.get(sortarmOrnderList.get(i)));

				}

			} catch (Exception e) {
				e.printStackTrace();
			}

			List<String> sheetNameList = new ArrayList<String>();
			int sheetNameIndex = 2;
			for (int i = 0; i < sortedarmList.size(); i++) {
				Map<String, Row> proForVp = new HashMap<String, Row>();
				Map<Integer, Row> proForColor = new HashMap<Integer, Row>();
				Element armEle = sortedarmList.get(i);

				// create sheet
				String sheetName = "Phase "+(eopchLen+1)+"-"+"Arm "+(i+1);
				/*String sheetName = epoch.getAttribute("name") + "-"
						+ armEle.getAttribute("name");

				if (sheetName.length() > 30) {
					sheetName = sheetName.substring(0, 29);
				}
				if (sheetNameList.contains(sheetName)) {
					sheetName = sheetName + "" + sheetNameIndex;
					sheetNameIndex++;
				}

				if (sheetName.contains("/")) {
					sheetName = sheetName.replace("/", "-");
				}

				if (sheetName.contains("'")) {
					sheetName = sheetName.replace("\'", "");
				}*/

				sheetNameList.add(sheetName);
				
				Sheet sheet = wb.createSheet(sheetName);
				Drawing drawing = sheet.createDrawingPatriarch();
				ClientAnchor anchor = createHelper.createClientAnchor();
				
				// create row for budget info
				Header header = sheet.getHeader();
				header.setCenter(getProtocolTitle(protocol));
				Footer footer = sheet.getFooter();

				// Get PIname
				List<String> PIList = getProtocolPI(protocol);
				String PIName = "";
				for (int nameLen = 0; nameLen < PIList.size(); nameLen++) {
					PIName = PIName + PIList.get(nameLen);
				}

				footer.setLeft("#IRB" + IRB + " " + PIName);
				// footer.setLeft("#IRB" + IRB);
				// get generated time
				Date curDate = new Date();
				/*
				 * SimpleDateFormat bartDateFormat = new SimpleDateFormat(
				 * "MM/dd/yy hh:mm");
				 */
				footer.setCenter("Generated: "
						+ DateFormat.getInstance().format(curDate));

				footer.setRight("Printed: " + HeaderFooter.date() + " "
						+ HeaderFooter.time() + "  Page " + HeaderFooter.page()
						+ " of " + HeaderFooter.numPages());

				// create row for cycle title
				Row cycleRow = sheet.createRow(2);

				int indexBeforeTotal = 0;

				// NodeList armForNumList = epoch.getElementsByTagName("arm");
				// armNum = armForNumList.getLength();
				// NodeList visitForNumList =
				// epoch.getElementsByTagName("visit");
				NodeList visitForNumList = armEle.getElementsByTagName("visit");
				visitNum = visitForNumList.getLength();
				placeForNotes = 2 + visitNum;
				if (budgetDocumentType.equals(BudgetDocumentType.NOTES_ONLY)) {
					placeForNotes = 2;
				}
				// set default cell
				Row rowForTitle = sheet.createRow(4);
				if (budgetDocumentType.equals(BudgetDocumentType.NOTES_ONLY)) {
					rowForTitle = sheet.createRow(1);
				}
				Cell cellForProTitle = rowForTitle.createCell(1);
				cellForProTitle.setCellValue(createHelper
						.createRichTextString("Procedure"));
				// set height for visit title row
				rowForTitle.setHeightInPoints((3 * sheet
						.getDefaultRowHeightInPoints()));

				/*
				 * style.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex
				 * ()) ; style.setFillPattern(CellStyle.SOLID_FOREGROUND);
				 */
				cellForProTitle.setCellStyle(styleForTitle);

				// this part works only for full document
				if (budgetDocumentType.equals(BudgetDocumentType.FULL)
						|| budgetDocumentType
								.equals(BudgetDocumentType.NO_NOTES)
						|| budgetDocumentType.equals(BudgetDocumentType.ALL)) {

					// here we change 18 to 6, when sponsor and price are not
					// set
					// placeForNotes = 6 + visitNum + armNum * 6;
					placeForNotes = 6 + visitNum + 6;

					Cell cellForCostFullTitle = rowForTitle.createCell(0);
					cellForCostFullTitle.setCellValue(createHelper
							.createRichTextString("CPT"));
					cellForCostFullTitle.setCellStyle(styleForTitle);

					// cell for final boarder format
					Cell boarderCell = rowForTitle.createCell(placeForNotes);
					if (budgetDocumentType.equals(BudgetDocumentType.NO_NOTES)) {
						boarderCell.setCellStyle(color.get(11));
					} else {
						boarderCell.setCellStyle(styleForTitle);
					}

					Cell cellForCostTitle = rowForTitle.createCell(2);
					Cell cellForSponTitle = rowForTitle.createCell(3);
					Cell cellForPriceTitle = rowForTitle.createCell(4);
					Cell cellForResidualTitle = rowForTitle.createCell(5);
					cellForCostTitle.setCellValue(createHelper
							.createRichTextString("Cost"));
					cellForSponTitle.setCellValue(createHelper
							.createRichTextString("Sponsor"));
					cellForPriceTitle.setCellValue(createHelper
							.createRichTextString("Price"));
					cellForResidualTitle.setCellValue(createHelper
							.createRichTextString("Residual"));
					cellForCostTitle.setCellStyle(styleForTitle);
					cellForSponTitle.setCellStyle(styleForTitle);
					cellForPriceTitle.setCellStyle(styleForTitle);
					cellForResidualTitle.setCellStyle(styleForTitle);

					/*
					 * sheet.addMergedRegion(new CellRangeAddress(1, // first
					 * row // (0-based) 1, // last row (0-based) 1, // first
					 * column (0-based) 4 // last column // (0-based) ));
					 */

				}
				if (budgetDocumentType.equals(BudgetDocumentType.PRICE_ONLY)) {

					// placeForNotes = 3 + visitNum + armNum * 6;
					placeForNotes = 3 + visitNum + 6;

					Cell cellForCostFullTitle = rowForTitle.createCell(0);
					cellForCostFullTitle.setCellValue(createHelper
							.createRichTextString("CPT"));
					cellForCostFullTitle.setCellStyle(styleForTitle);

					// cell for final boarder format
					Cell boarderCell = rowForTitle.createCell(placeForNotes);
					boarderCell.setCellStyle(styleForTitle);

					/*
					 * Cell cellForCostFullTitle = cycleRow.createCell(1);
					 * cellForCostFullTitle.setCellValue(createHelper
					 * .createRichTextString("Procedure"));
					 */

					Cell cellForPriceTitle = rowForTitle.createCell(2);
					cellForPriceTitle.setCellValue(createHelper
							.createRichTextString("Price"));
					cellForPriceTitle.setCellStyle(styleForTitle);
				}
				// create places for phase title and arm title
				Row phaseTitleRow = sheet.createRow(0);
				int tagForNoNotes = 0;
				if (budgetDocumentType.equals(BudgetDocumentType.NO_NOTES))
					tagForNoNotes = 1;
				sheet.addMergedRegion(new CellRangeAddress(0, // first row
						// (0-based)
						0, // last row (0-based)
						0, // first column (0-based)
						placeForNotes - tagForNoNotes // last column (0-based)
				));
				for (int j = 0; j < placeForNotes + 1 - tagForNoNotes; j++) {
					Cell cellForPhaseTitle = phaseTitleRow.createCell(j);
					cellForPhaseTitle.setCellStyle(styleForTitle);
					if (j == 0) {
						cellForPhaseTitle.setCellValue(createHelper
								.createRichTextString("Phase: "
										+ epoch.getAttribute("name") + " Arm: "
										+ armEle.getAttribute("name")));
						String armNotes = armEle.getElementsByTagName("notes").item(0).getTextContent();
						if(!armNotes.isEmpty()){
						anchor.setCol1(cellForPhaseTitle.getColumnIndex());
					    anchor.setCol2(cellForPhaseTitle.getColumnIndex()+1);
					    anchor.setRow1(phaseTitleRow.getRowNum());
					    anchor.setRow2(phaseTitleRow.getRowNum()+3);
						Comment comment = drawing.createCellComment(anchor);
						RichTextString str = createHelper.createRichTextString(armNotes);
					    comment.setString(str);
					    comment.setAuthor("CLARA");
					    cellForPhaseTitle.setCellComment(comment);
						}
					}
				}
				

				/*
				 * if (budgetDocumentType.equals(BudgetDocumentType.ALL)) {
				 * 
				 * placeForNotes = 4 + visitNum + armNum * 6;
				 * 
				 * Cell cellForCostFullTitle = rowForTitle.createCell(0);
				 * cellForCostFullTitle.setCellValue(createHelper
				 * .createRichTextString("CPT"));
				 * cellForCostFullTitle.setCellStyle(styleForTitle);
				 * 
				 * 
				 * Cell cellForCostFullTitle = cycleRow.createCell(1);
				 * cellForCostFullTitle.setCellValue(createHelper
				 * .createRichTextString("CostProcedure"));
				 * 
				 * 
				 * Cell cellForCostTitle = rowForTitle.createCell(2);
				 * cellForCostTitle.setCellValue(createHelper
				 * .createRichTextString("Cost"));
				 * cellForCostTitle.setCellStyle(styleForTitle);
				 * 
				 * Cell cellForPriceTitle = rowForTitle.createCell(3);
				 * cellForPriceTitle.setCellValue(createHelper
				 * .createRichTextString("Price"));
				 * cellForPriceTitle.setCellStyle(styleForTitle); }
				 */

				indexBeforeTotal = addProduce(epoch, sheet, createHelper,
						proForVp, proForColor, styleForProList, wb,
						styleForProList2, styleForTitle, budgetDocumentType,
						indexBeforeTotal, placeForNotes, fa,
						procedureCellWidth, color);

				if (!budgetDocumentType.equals(BudgetDocumentType.NOTES_ONLY)) {
					addArm(epoch, armEle, sheet, createHelper, rowForTitle,
							proForVp, proForColor, style, color, wb,
							budgetDocumentType, indexBeforeTotal, fa, cycleRow,
							placeForNotes);
				}

				// set width for column 0
				int autocellpalce = placeForNotes;
				while (autocellpalce >= 1) {
					sheet.autoSizeColumn(autocellpalce - 1);
					sheet.setColumnWidth(autocellpalce - 1, 3000);
					autocellpalce--;
				}
				sheet.setColumnWidth(placeForNotes, 10000);
				sheet.setColumnWidth(1, procedureCellWidth);
				// set border for clender only
				Cell cellforBorder = rowForTitle.createCell(0);
				Cell cellforBorderNotes = rowForTitle.createCell(placeForNotes);

				cellforBorder.setCellStyle(styleForTitle);
				if (budgetDocumentType.equals(BudgetDocumentType.NO_NOTES)) {
					cellforBorderNotes.setCellStyle(color.get(11));
				} else {
					cellforBorderNotes.setCellStyle(styleForTitle);
				}
			}

		}

	}

	private String getProtocolTitle(Protocol protocol) {
		String metaDataXml = protocol.getMetaDataXml();
		String title = null;
		try {
			Document metaData = xmlProcessor.loadXmlStringToDOM(metaDataXml);
			NodeList budgetList = metaData.getElementsByTagName("title");
			Node priceNode = budgetList.item(0);
			title = priceNode.getTextContent();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return title;

	}

	private List<String> getProtocolPI(Protocol protocol) {
		String metaDataXml = protocol.getMetaDataXml();
		List<String> piList = new ArrayList<String>();
		try {
			Document metaDataDoc = xmlProcessor.loadXmlStringToDOM(metaDataXml);
			XPath xpath = xmlProcessor.getXPathInstance();
			NodeList staffLst = (NodeList) xpath
					.evaluate(
							"//staffs/staff/user[roles/role='Principal Investigator' or roles/role='Treating Physician']",
							metaDataDoc, XPathConstants.NODESET);

			if (staffLst.getLength() > 0) {
				for (int i = 0; i < staffLst.getLength(); i++) {
					Element userEl = (Element) staffLst.item(i);

					long userId = Long.valueOf(userEl.getAttribute("id"));
					User piUser = getUserDao().findById(userId);

					piList.add(piUser.getPerson().getFullname());
				}
			}

			/*
			 * for (int PIlength = 0; PIlength < firstNameList.getLength();
			 * PIlength++) { Node firstName = firstNameList.item(PIlength); Node
			 * lastName = lastNameList.item(PIlength); String PIname =
			 * firstName.getTextContent() + " " + lastName.getTextContent() +
			 * "; "; PIList.add(PIname); }
			 */

		} catch (Exception e) {
			e.printStackTrace();
		}
		return piList;
	}

	private void colorStyle(HSSFWorkbook wb, Map<Integer, CellStyle> color) {

		HSSFPalette palette = wb.getCustomPalette();
		// for green
		CellStyle lightGreen = wb.createCellStyle();
		lightGreen.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
		palette.setColorAtIndex(HSSFColor.LIGHT_GREEN.index, (byte) 204,
				(byte) 255, (byte) 204);
		lightGreen.setFillForegroundColor(HSSFColor.LIGHT_GREEN.index);

		lightGreen.setBorderBottom(CellStyle.BORDER_THIN);
		lightGreen.setBorderLeft(CellStyle.BORDER_THIN);
		lightGreen.setBorderRight(CellStyle.BORDER_THIN);
		lightGreen.setBorderTop(CellStyle.BORDER_THIN);
		lightGreen.setAlignment(CellStyle.ALIGN_CENTER);
		lightGreen.setVerticalAlignment(CellStyle.VERTICAL_CENTER);

		color.put(1, lightGreen);

		/*
		 * CellStyle darkGreen = wb.createCellStyle();
		 * darkGreen.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
		 * palette.setColorAtIndex(HSSFColor.GREEN.index, (byte) 151, (byte)
		 * 206, (byte) 131);
		 * darkGreen.setFillForegroundColor(HSSFColor.GREEN.index);
		 * 
		 * darkGreen.setBorderBottom(CellStyle.BORDER_THIN);
		 * darkGreen.setBorderLeft(CellStyle.BORDER_THIN);
		 * darkGreen.setBorderRight(CellStyle.BORDER_THIN);
		 * darkGreen.setBorderTop(CellStyle.BORDER_THIN);
		 * darkGreen.setAlignment(CellStyle.ALIGN_CENTER);
		 * darkGreen.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
		 */

		color.put(2, lightGreen);

		CellStyle lightlightGreen = wb.createCellStyle();
		lightlightGreen.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
		/*
		 * palette.setColorAtIndex(HSSFColor.TURQUOISE.index, (byte) 131, (byte)
		 * 100, (byte) 131);
		 * lightlightGreen.setFillForegroundColor(HSSFColor.TURQUOISE.index);
		 */
		palette.setColorAtIndex(HSSFColor.GREY_25_PERCENT.index, (byte) 255,
				(byte) 255, (byte) 204);
		lightlightGreen.setFillForegroundColor(HSSFColor.WHITE.index);

		lightlightGreen.setBorderBottom(CellStyle.BORDER_THIN);
		lightlightGreen.setBorderLeft(CellStyle.BORDER_THIN);
		lightlightGreen.setBorderRight(CellStyle.BORDER_THIN);
		lightlightGreen.setBorderTop(CellStyle.BORDER_THIN);
		lightlightGreen.setAlignment(CellStyle.ALIGN_CENTER);
		lightlightGreen.setVerticalAlignment(CellStyle.VERTICAL_CENTER);

		color.put(3, lightlightGreen);
		// for blue
		CellStyle lightBlue = wb.createCellStyle();
		lightBlue.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
		palette.setColorAtIndex(HSSFColor.LIGHT_BLUE.index, (byte) 204,
				(byte) 236, (byte) 253);
		lightBlue.setFillForegroundColor(HSSFColor.LIGHT_BLUE.index);

		lightBlue.setBorderBottom(CellStyle.BORDER_THIN);
		lightBlue.setBorderLeft(CellStyle.BORDER_THIN);
		lightBlue.setBorderRight(CellStyle.BORDER_THIN);
		lightBlue.setBorderTop(CellStyle.BORDER_THIN);
		lightBlue.setAlignment(CellStyle.ALIGN_CENTER);
		lightBlue.setVerticalAlignment(CellStyle.VERTICAL_CENTER);

		color.put(4, lightBlue);

		/*
		 * CellStyle darkBlue = wb.createCellStyle();
		 * darkBlue.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
		 * palette.setColorAtIndex(HSSFColor.LIME.index, (byte) 102, (byte) 204,
		 * (byte) 253); darkBlue.setFillForegroundColor(HSSFColor.LIME.index);
		 * 
		 * darkBlue.setBorderBottom(CellStyle.BORDER_THIN);
		 * darkBlue.setBorderLeft(CellStyle.BORDER_THIN);
		 * darkBlue.setBorderRight(CellStyle.BORDER_THIN);
		 * darkBlue.setBorderTop(CellStyle.BORDER_THIN);
		 * darkBlue.setAlignment(CellStyle.ALIGN_CENTER);
		 * darkBlue.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
		 */

		color.put(5, lightBlue);

		CellStyle lightlightBlue = wb.createCellStyle();
		lightlightBlue.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
		/*
		 * palette.setColorAtIndex(HSSFColor.TEAL.index, (byte) 102, (byte) 101,
		 * (byte) 123);
		 * lightlightBlue.setFillForegroundColor(HSSFColor.TEAL.index);
		 */
		palette.setColorAtIndex(HSSFColor.GREY_25_PERCENT.index, (byte) 255,
				(byte) 255, (byte) 204);
		lightlightBlue.setFillForegroundColor(HSSFColor.WHITE.index);

		lightlightBlue.setBorderBottom(CellStyle.BORDER_THIN);
		lightlightBlue.setBorderLeft(CellStyle.BORDER_THIN);
		lightlightBlue.setBorderRight(CellStyle.BORDER_THIN);
		lightlightBlue.setBorderTop(CellStyle.BORDER_THIN);
		lightlightBlue.setAlignment(CellStyle.ALIGN_CENTER);
		lightlightBlue.setVerticalAlignment(CellStyle.VERTICAL_CENTER);

		color.put(6, lightlightBlue);

		// for red
		CellStyle lightRed = wb.createCellStyle();
		lightRed.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
		palette.setColorAtIndex(HSSFColor.PINK.index, (byte) 255, (byte) 204,
				(byte) 204);
		lightRed.setFillForegroundColor(HSSFColor.PINK.index);

		lightRed.setBorderBottom(CellStyle.BORDER_THIN);
		lightRed.setBorderLeft(CellStyle.BORDER_THIN);
		lightRed.setBorderRight(CellStyle.BORDER_THIN);
		lightRed.setBorderTop(CellStyle.BORDER_THIN);
		lightRed.setAlignment(CellStyle.ALIGN_CENTER);
		lightRed.setVerticalAlignment(CellStyle.VERTICAL_CENTER);

		color.put(7, lightRed);

		/*
		 * CellStyle darkRed = wb.createCellStyle();
		 * darkRed.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
		 * palette.setColorAtIndex(HSSFColor.RED.index, (byte) 218, (byte) 165,
		 * (byte) 165); darkRed.setFillForegroundColor(HSSFColor.RED.index);
		 * 
		 * darkRed.setBorderBottom(CellStyle.BORDER_THIN);
		 * darkRed.setBorderLeft(CellStyle.BORDER_THIN);
		 * darkRed.setBorderRight(CellStyle.BORDER_THIN);
		 * darkRed.setBorderTop(CellStyle.BORDER_THIN);
		 * darkRed.setAlignment(CellStyle.ALIGN_CENTER);
		 * darkRed.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
		 */

		color.put(8, lightRed);

		CellStyle lightlightRed = wb.createCellStyle();
		lightlightRed.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
		/*
		 * palette.setColorAtIndex(HSSFColor.YELLOW.index, (byte) 100, (byte)
		 * 165, (byte) 165);
		 */
		palette.setColorAtIndex(HSSFColor.GREY_25_PERCENT.index, (byte) 255,
				(byte) 255, (byte) 204);
		lightlightRed.setFillForegroundColor(HSSFColor.WHITE.index);
		// lightlightRed.setFillForegroundColor(HSSFColor.YELLOW.index);

		lightlightRed.setBorderBottom(CellStyle.BORDER_THIN);
		lightlightRed.setBorderLeft(CellStyle.BORDER_THIN);
		lightlightRed.setBorderRight(CellStyle.BORDER_THIN);
		lightlightRed.setBorderTop(CellStyle.BORDER_THIN);
		lightlightRed.setAlignment(CellStyle.ALIGN_CENTER);
		lightlightRed.setVerticalAlignment(CellStyle.VERTICAL_CENTER);

		color.put(9, lightlightRed);

		CellStyle lightGrey = wb.createCellStyle();
		lightGrey.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
		palette.setColorAtIndex(HSSFColor.GREY_25_PERCENT.index, (byte) 255,
				(byte) 255, (byte) 204);
		lightGrey.setFillForegroundColor(HSSFColor.GREY_25_PERCENT.index);
		lightGrey.setBorderBottom(CellStyle.BORDER_THIN);
		lightGrey.setBorderLeft(CellStyle.BORDER_THIN);
		lightGrey.setBorderRight(CellStyle.BORDER_THIN);
		lightGrey.setBorderTop(CellStyle.BORDER_THIN);
		lightGrey.setAlignment(CellStyle.ALIGN_CENTER);
		lightGrey.setVerticalAlignment(CellStyle.VERTICAL_CENTER);

		CellStyle BorderStyle = wb.createCellStyle();
		BorderStyle.setBorderBottom(CellStyle.BORDER_THIN);
		BorderStyle.setBorderLeft(CellStyle.BORDER_THIN);
		BorderStyle.setBorderRight(CellStyle.BORDER_THIN);
		BorderStyle.setBorderTop(CellStyle.BORDER_THIN);
		BorderStyle.setAlignment(CellStyle.ALIGN_CENTER);
		Font font = wb.createFont();
		font.setBoldweight(Font.BOLDWEIGHT_BOLD);
		BorderStyle.setFont(font);
		BorderStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
		BorderStyle.setWrapText(true);
		BorderStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat("0.00"));
		color.put(10, BorderStyle);

		CellStyle noNotesStyle = wb.createCellStyle();
		noNotesStyle.setBorderLeft(CellStyle.BORDER_THIN);
		color.put(11, noNotesStyle);

		CellStyle cycleNotesStyle = wb.createCellStyle();
		cycleNotesStyle.setBorderBottom(CellStyle.BORDER_THIN);
		cycleNotesStyle.setBorderLeft(CellStyle.BORDER_THIN);
		cycleNotesStyle.setBorderRight(CellStyle.BORDER_THIN);
		cycleNotesStyle.setBorderTop(CellStyle.BORDER_THIN);
		cycleNotesStyle.setAlignment(CellStyle.ALIGN_CENTER);
		cycleNotesStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
		cycleNotesStyle.setWrapText(true);
		color.put(12, cycleNotesStyle);

	}

	@SuppressWarnings("deprecation")
	private List<Integer> addTotal(int curRow, Sheet sheet,
			CreationHelper createHelper, Row totalName, Cell cellForTotal,
			Map<Integer, Row> proForColor, int colorRowIndex, HSSFWorkbook wb,
			String fa, BudgetDocumentType budgetDocumentType, int placeForNotes) {
		CellStyle BorderStyle = wb.createCellStyle();
		BorderStyle.setBorderBottom(CellStyle.BORDER_THIN);
		BorderStyle.setBorderLeft(CellStyle.BORDER_THIN);
		BorderStyle.setBorderRight(CellStyle.BORDER_THIN);
		BorderStyle.setBorderTop(CellStyle.BORDER_THIN);
		BorderStyle.setAlignment(CellStyle.ALIGN_RIGHT);
		BorderStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);

		CellStyle noNotesBoder = wb.createCellStyle();
		noNotesBoder.setBorderLeft(CellStyle.BORDER_THIN);

		int totalCostPlace = 1;
		if (budgetDocumentType == BudgetDocumentType.FULL
				|| budgetDocumentType == BudgetDocumentType.ALL
				|| budgetDocumentType == BudgetDocumentType.NO_NOTES)
			totalCostPlace = 5;
		else if (budgetDocumentType == BudgetDocumentType.PRICE_ONLY)
			totalCostPlace = 2;
		/*
		 * else if (budgetDocumentType == BudgetDocumentType.ALL) totalCostPlace
		 * = 3;
		 */

		List<Integer> tempReturn = new ArrayList<Integer>();
		totalName = sheet.createRow(curRow);
		cellForTotal = totalName.createCell(0);
		// cell for boarder uses

		Cell cellForBoarders = totalName.createCell(placeForNotes);
		if (budgetDocumentType != BudgetDocumentType.NO_NOTES) {
			cellForBoarders.setCellStyle(BorderStyle);
		} else {
			cellForBoarders.setCellStyle(noNotesBoder);
		}
		for (int cellNum = 1; cellNum <= totalCostPlace; cellNum++) {
			Cell cellForTotals = totalName.createCell(cellNum);
			cellForTotals.setCellStyle(BorderStyle);
		}

		sheet.addMergedRegion(new CellRangeAddress(curRow, // first row
															// (0-based)
				curRow, // last row (0-based)
				0, // first column (0-based)
				totalCostPlace // last column (0-based)
		));

		cellForTotal.setCellValue(createHelper
				.createRichTextString("Per Subject Direct"));

		cellForTotal.setCellStyle(BorderStyle);
		proForColor.put(colorRowIndex, totalName);
		colorRowIndex++;
		curRow++;

		for (int cellNum = 1; cellNum <= totalCostPlace; cellNum++) {
			Cell cellForTotals = totalName.createCell(cellNum);
			cellForTotals.setCellStyle(BorderStyle);
		}

		sheet.addMergedRegion(new CellRangeAddress(curRow, // first row
															// (0-based)
				curRow, // last row (0-based)
				0, // first column (0-based)
				totalCostPlace // last column (0-based)
		));

		totalName = sheet.createRow(curRow);
		// cell for boarder uses
		cellForBoarders = totalName.createCell(placeForNotes);
		if (budgetDocumentType != BudgetDocumentType.NO_NOTES) {
			cellForBoarders.setCellStyle(BorderStyle);
		} else {
			cellForBoarders.setCellStyle(noNotesBoder);
		}

		cellForTotal = totalName.createCell(0);
		cellForTotal.setCellValue(createHelper
				.createRichTextString("Facilities and Admin(F & A)% " + fa));
		cellForTotal.setCellStyle(BorderStyle);
		proForColor.put(colorRowIndex, totalName);
		colorRowIndex++;
		curRow++;

		for (int cellNum = 1; cellNum <= totalCostPlace; cellNum++) {
			Cell cellForTotals = totalName.createCell(cellNum);
			cellForTotals.setCellStyle(BorderStyle);
		}

		sheet.addMergedRegion(new CellRangeAddress(curRow, // first row
															// (0-based)
				curRow, // last row (0-based)
				0, // first column (0-based)
				totalCostPlace // last column (0-based)
		));

		totalName = sheet.createRow(curRow);
		cellForTotal = totalName.createCell(0);
		// cell for boarder uses
		cellForBoarders = totalName.createCell(placeForNotes);
		if (budgetDocumentType != BudgetDocumentType.NO_NOTES) {
			cellForBoarders.setCellStyle(BorderStyle);
		} else {
			cellForBoarders.setCellStyle(noNotesBoder);
		}

		cellForTotal.setCellValue(createHelper
				.createRichTextString("Per Subject Total"));
		cellForTotal.setCellStyle(BorderStyle);
		proForColor.put(colorRowIndex, totalName);
		colorRowIndex++;
		curRow++;

		for (int cellNum = 1; cellNum <= totalCostPlace; cellNum++) {
			Cell cellForTotals = totalName.createCell(cellNum);
			cellForTotals.setCellStyle(BorderStyle);
		}

		sheet.addMergedRegion(new CellRangeAddress(curRow, // first row
															// (0-based)
				curRow, // last row (0-based)
				0, // first column (0-based)
				totalCostPlace // last column (0-based)
		));

		totalName = sheet.createRow(curRow);
		cellForTotal = totalName.createCell(0);
		// cell for boarder uses
		cellForBoarders = totalName.createCell(placeForNotes);
		if (budgetDocumentType != BudgetDocumentType.NO_NOTES) {
			cellForBoarders.setCellStyle(BorderStyle);
		} else {
			cellForBoarders.setCellStyle(noNotesBoder);
		}

		cellForTotal.setCellValue(createHelper
				.createRichTextString("# Subjects on this Arm"));
		cellForTotal.setCellStyle(BorderStyle);
		proForColor.put(colorRowIndex, totalName);
		colorRowIndex++;
		curRow++;

		for (int cellNum = 1; cellNum <= totalCostPlace; cellNum++) {
			Cell cellForTotals = totalName.createCell(cellNum);
			cellForTotals.setCellStyle(BorderStyle);
		}

		sheet.addMergedRegion(new CellRangeAddress(curRow, // first row
															// (0-based)
				curRow, // last row (0-based)
				0, // first column (0-based)
				totalCostPlace // last column (0-based)
		));

		// cell for boarder uses
		cellForBoarders = totalName.createCell(placeForNotes);
		if (budgetDocumentType != BudgetDocumentType.NO_NOTES) {
			cellForBoarders.setCellStyle(BorderStyle);
		} else {
			cellForBoarders.setCellStyle(noNotesBoder);
		}

		totalName = sheet.createRow(curRow);

		cellForTotal = totalName.createCell(0);
		// cell for boarder uses
		cellForBoarders = totalName.createCell(placeForNotes);
		if (budgetDocumentType != BudgetDocumentType.NO_NOTES) {
			cellForBoarders.setCellStyle(BorderStyle);
		} else {
			cellForBoarders.setCellStyle(noNotesBoder);
		}

		cellForTotal.setCellValue(createHelper
				.createRichTextString("Final Total"));
		cellForTotal.setCellStyle(BorderStyle);
		proForColor.put(colorRowIndex, totalName);
		colorRowIndex++;
		curRow++;
		for (int cellNum = 1; cellNum <= totalCostPlace; cellNum++) {
			Cell cellForTotals = totalName.createCell(cellNum);
			cellForTotals.setCellStyle(BorderStyle);
		}

		tempReturn.add(curRow);
		tempReturn.add(colorRowIndex);
		return tempReturn;
	}

	@SuppressWarnings("deprecation")
	private void addProTotal(int curRow, Sheet sheet,
			BudgetDocumentType budgetDocumentType, CreationHelper createHelper,
			Map<Integer, Row> proForColor, int colorRowIndex, HSSFWorkbook wb,
			String fa, int placeForNotes) {
		List<Integer> tempReturn = new ArrayList<Integer>();

		CellStyle BorderStyle = wb.createCellStyle();
		BorderStyle.setBorderBottom(CellStyle.BORDER_THIN);
		BorderStyle.setBorderLeft(CellStyle.BORDER_THIN);
		BorderStyle.setBorderRight(CellStyle.BORDER_THIN);
		BorderStyle.setBorderTop(CellStyle.BORDER_THIN);
		BorderStyle.setAlignment(CellStyle.ALIGN_RIGHT);
		BorderStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);

		CellStyle noNotesBoder = wb.createCellStyle();
		noNotesBoder.setBorderLeft(CellStyle.BORDER_THIN);

		if (budgetDocumentType.equals(BudgetDocumentType.PRICE_ONLY)) {
			Row totalName = sheet.createRow(curRow);
			Cell cellForTotal1 = totalName.createCell(1);
			Cell cellForTotal2 = totalName.createCell(2);

			sheet.addMergedRegion(new CellRangeAddress(curRow, // first row
																// (0-based)
					curRow, // last row (0-based)
					0, // first column (0-based)
					2 // last column (0-based)
			));
			Cell cellForTotal = totalName.createCell(0);

			// cell for notes boarder
			Cell cellNoteBoarder = totalName.createCell(placeForNotes);
			if (budgetDocumentType != BudgetDocumentType.NO_NOTES) {
				cellNoteBoarder.setCellStyle(BorderStyle);
			} else {
				cellNoteBoarder.setCellStyle(noNotesBoder);
			}

			cellForTotal.setCellValue(createHelper
					.createRichTextString("Price Total"));
			cellForTotal.setCellStyle(BorderStyle);
			cellForTotal1.setCellStyle(BorderStyle);
			cellForTotal2.setCellStyle(BorderStyle);
			curRow++;
			addTotal(curRow, sheet, createHelper, totalName, cellForTotal,
					proForColor, colorRowIndex, wb, fa, budgetDocumentType,
					placeForNotes);
			curRow++;
		}
		/*
		 * if (budgetDocumentType.equals(BudgetDocumentType.ALL)) { Row
		 * totalName = sheet.createRow(curRow); Cell cellForTotal =
		 * totalName.createCell(0); Cell cellForTotal1 =
		 * totalName.createCell(1); Cell cellForTotal2 =
		 * totalName.createCell(2); Cell cellForTotal3 =
		 * totalName.createCell(3);
		 * 
		 * sheet.addMergedRegion(new CellRangeAddress( curRow, //first row
		 * (0-based) curRow, //last row (0-based) 0, //first column (0-based) 3
		 * //last column (0-based) ));
		 * 
		 * cellForTotal.setCellValue(createHelper
		 * .createRichTextString("Cost Total"));
		 * cellForTotal.setCellStyle(BorderStyle);
		 * cellForTotal1.setCellStyle(BorderStyle);
		 * cellForTotal2.setCellStyle(BorderStyle);
		 * cellForTotal3.setCellStyle(BorderStyle); curRow++; addTotal(curRow,
		 * sheet, createHelper, totalName, cellForTotal, proForColor,
		 * colorRowIndex, wb, fa, budgetDocumentType); curRow++; }
		 */
		if (budgetDocumentType.equals(BudgetDocumentType.FULL)
				|| budgetDocumentType.equals(BudgetDocumentType.NO_NOTES)
				|| budgetDocumentType.equals(BudgetDocumentType.ALL)) {
			Row totalName = sheet.createRow(curRow);
			Cell cellForTotal = totalName.createCell(0);

			Cell cellForTotal1 = totalName.createCell(1);
			Cell cellForTotal2 = totalName.createCell(2);
			Cell cellForTotal3 = totalName.createCell(3);
			Cell cellForTotal4 = totalName.createCell(4);
			Cell cellForTotal5 = totalName.createCell(5);

			// cell for notes boarder
			Cell cellNoteBoarder = totalName.createCell(placeForNotes);
			if (budgetDocumentType != BudgetDocumentType.NO_NOTES) {
				cellNoteBoarder.setCellStyle(BorderStyle);
			} else {
				cellNoteBoarder.setCellStyle(noNotesBoder);
			}

			sheet.addMergedRegion(new CellRangeAddress(curRow, // first row
																// (0-based)
					curRow, // last row (0-based)
					0, // first column (0-based)
					5 // last column (0-based)
			));

			cellForTotal.setCellValue(createHelper
					.createRichTextString("Cost Total"));
			cellForTotal.setCellStyle(BorderStyle);
			cellForTotal1.setCellStyle(BorderStyle);
			cellForTotal2.setCellStyle(BorderStyle);
			cellForTotal3.setCellStyle(BorderStyle);
			cellForTotal4.setCellStyle(BorderStyle);
			cellForTotal5.setCellStyle(BorderStyle);
			curRow++;

			tempReturn = addTotal(curRow, sheet, createHelper, totalName,
					cellForTotal, proForColor, colorRowIndex, wb, fa,
					budgetDocumentType, placeForNotes);
			curRow = tempReturn.get(0);
			colorRowIndex = tempReturn.get(1);

			// this part is for sponer
			/*
			 * totalName = sheet.createRow(curRow); cellForTotal =
			 * totalName.createCell(0); cellForTotal.setCellValue(createHelper
			 * .createRichTextString("Price Total")); curRow++;
			 * 
			 * tempReturn = addTotal(curRow, sheet, createHelper, totalName,
			 * cellForTotal, proForColor, colorRowIndex); curRow =
			 * tempReturn.get(0); colorRowIndex = tempReturn.get(1);
			 * 
			 * totalName = sheet.createRow(curRow); cellForTotal =
			 * totalName.createCell(0); cellForTotal.setCellValue(createHelper
			 * .createRichTextString("Sponsor Total")); curRow++; tempReturn =
			 * addTotal(curRow, sheet, createHelper, totalName, cellForTotal,
			 * proForColor, colorRowIndex); curRow = tempReturn.get(0);
			 * colorRowIndex = tempReturn.get(1);
			 */
			curRow++;

		}

	}

	private int addProduce(Element epoch, Sheet sheet,
			CreationHelper createHelper, Map<String, Row> proForVp,
			Map<Integer, Row> proForColor, CellStyle style, HSSFWorkbook wb,
			CellStyle styleForProList2, CellStyle styleForTitle,
			BudgetDocumentType budgetDocumentType, int indexBeforeTotal,
			int placeForNotes, String fa, int procedureCellWidth,
			Map<Integer, CellStyle> color) {
		NodeList procedureList = epoch.getElementsByTagName("procedure");
		List<String> procedureTypeList = new ArrayList<String>();
		List<String> subprocedureTypeList = new ArrayList<String>();

		int curRow = 5;
		if (budgetDocumentType.equals(BudgetDocumentType.NOTES_ONLY)) {
			curRow = 2;
		}
		int colorRowIndex = 0;
		// set accuracy
		int scale = 2;
		int roundingMode = 4;

		// this string is set for the judge the digit of cost from budget xml
		// String costString = null;
		// create procedure category list
		for (int proLen = 0; proLen < procedureList.getLength(); proLen++) {
			Element procedure = (Element) procedureList.item(proLen);
			/*
			 * NodeList subprocedureList = procedure
			 * .getElementsByTagName("subprocedures");
			 * 
			 * proLen = proLen + subprocedureList.getLength();
			 */
			String addType = procedure.getAttribute("category");
			if (addType.equals("")) {
				addType = "NONE";
			}
			if (addType.equals("Misc.")) {
				addType = "MISC";
			}
			procedureTypeList.add(addType);
			// logger.debug(epoch.getAttribute("name")+" "+procedure.getAttribute("cptcode"));

		}

		List<String> subproIDForRemoveList = new ArrayList<String>();
		// create subprocedure category
		NodeList subproIDList = epoch.getElementsByTagName("subprocedures");
		for (int subproLen = 0; subproLen < subproIDList.getLength(); subproLen++) {
			Element subPro = (Element) subproIDList.item(subproLen);
			NodeList proforsubIDList = subPro.getElementsByTagName("procedure");

			for (int proforsubLen = 0; proforsubLen < proforsubIDList
					.getLength(); proforsubLen++) {

				Element proForSub = (Element) proforsubIDList
						.item(proforsubLen);
				subproIDForRemoveList.add(proForSub.getAttribute("id"));
				String addsubType = proForSub.getAttribute("category");
				if (addsubType.equals("")) {
					addsubType = "NONE";
				}
				if (addsubType.equals("Misc.")) {
					addsubType = "MISC";
				}
				subprocedureTypeList.add(addsubType);
			}
		}

		// remove subprocedure category from pro list
		for (int subcatLen = 0; subcatLen < subprocedureTypeList.size(); subcatLen++) {
			if (procedureTypeList.contains(subprocedureTypeList.get(subcatLen))) {
				procedureTypeList.remove(subprocedureTypeList.get(subcatLen));
			}
		}
		HashSet<String> hashset = new HashSet<String>(procedureTypeList);
		procedureTypeList.clear();
		procedureTypeList.addAll(hashset);
		// sort the procedureTypeList according to the categroty name

		int hasNone = 0;
		// none alwasys be the first one
		if (procedureTypeList.contains("NONE")) {
			procedureTypeList.remove(procedureTypeList.indexOf("NONE"));
			hasNone = 1;
		}

		// temper change MISC to Misc for sort purpose
		if (procedureTypeList.contains("MISC")) {
			int miscIndex = procedureTypeList.indexOf("MISC");
			procedureTypeList.set(miscIndex, "Misc");
		}

		List<String> ListForTypeSort = new ArrayList<String>();
		for (int i = 0; i < procedureTypeList.size(); i++) {
			ListForTypeSort.add(procedureTypeList.get(i));
		}

		Collections.sort(ListForTypeSort);

		// since none is removed at begin, thus new space is needed when get it
		// back
		if (hasNone == 1) {
			procedureTypeList.add("increaseSpaceOnly");
			procedureTypeList.set(0, "NONE");
		}

		for (int i = 0; i < ListForTypeSort.size(); i++) {
			procedureTypeList.set(i + hasNone, ListForTypeSort.get(i));
		}

		// change Misc back to MISC
		if (procedureTypeList.contains("Misc")) {
			int miscIndex = procedureTypeList.indexOf("Misc");
			procedureTypeList.set(miscIndex, "MISC");
		}

		// create cellstyle for cpt category
		CellStyle cptCategoryStyle = wb.createCellStyle();
		cptCategoryStyle.setBorderBottom(CellStyle.BORDER_THIN);
		cptCategoryStyle.setBorderLeft(CellStyle.BORDER_THIN);
		cptCategoryStyle.setBorderRight(CellStyle.BORDER_THIN);
		cptCategoryStyle.setBorderTop(CellStyle.BORDER_THIN);
		cptCategoryStyle.setAlignment(CellStyle.ALIGN_LEFT);
		Font font = wb.createFont();
		font.setBoldweight(Font.BOLDWEIGHT_BOLD);
		cptCategoryStyle.setFont(font);
		cptCategoryStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
		cptCategoryStyle.setWrapText(true);

		List<Element> sortedprocedureList = new ArrayList<Element>();
		List<Integer> sortprocedureOrnderList = new ArrayList<Integer>();

		// sort the procedure by index
		Map<Integer, Element> procedureSortMap = new HashMap<Integer, Element>();
		try {
			for (int i = 0; i < procedureList.getLength(); i++) {
				Element procedure = (Element) procedureList.item(i);
				int procedureOrder = Integer.valueOf(procedure
						.getAttribute("id"));
				procedureSortMap.put(procedureOrder, procedure);
				sortprocedureOrnderList.add(procedureOrder);

			}
			Collections.sort(sortprocedureOrnderList);
			for (int i = 0; i < sortprocedureOrnderList.size(); i++) {
				sortedprocedureList.add(procedureSortMap
						.get(sortprocedureOrnderList.get(i)));
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		for (int i = 0; i < sortedprocedureList.size(); i++) {
			Element sortedProcedure = sortedprocedureList.get(i);
			if (subproIDForRemoveList.contains(sortedProcedure
					.getAttribute("id"))) {
				sortedprocedureList.remove(sortedProcedure);
				i--;
			}
		}

		for (int proTypeLen = 0; proTypeLen < procedureTypeList.size(); proTypeLen++) {

			Row procedureNameRow = sheet.createRow(curRow);
			
			
			
			// cerate cell 0 for boarder use and cell places for boarder
			Cell boarderCell = procedureNameRow.createCell(0);
			boarderCell.setCellStyle(styleForTitle);
			Cell boarderNotesCell = procedureNameRow.createCell(placeForNotes);
			if (budgetDocumentType != BudgetDocumentType.NO_NOTES) {
				boarderNotesCell.setCellStyle(styleForTitle);
			} else {
				boarderNotesCell.setCellStyle(color.get(11));
			}

			Cell cellForPro = procedureNameRow.createCell(1);
			cellForPro.setCellValue(createHelper
					.createRichTextString(procedureTypeList.get(proTypeLen)
							.toUpperCase()));
			String[] splitProType = procedureTypeList.get(proTypeLen)
					.split(" ");
			int wordLength = 0;
			int rowHight = 1;
			for (int wordNum = 0; wordNum < splitProType.length; wordNum++) {
				wordLength = wordLength + splitProType[wordNum].length() * 256;
				// in case the words is too long and directly start from the
				// next line
				if (splitProType[wordNum].length() * 256 > procedureCellWidth
						&& wordNum > 0)
					wordLength += procedureCellWidth
							- splitProType[wordNum - 1].length() * 256;
			}
			if (wordLength > procedureCellWidth) {
				rowHight += wordLength / procedureCellWidth;
			}
			procedureNameRow.setHeightInPoints((rowHight * sheet
					.getDefaultRowHeightInPoints()));
			cellForPro.setCellStyle(cptCategoryStyle);

			for (int proLen = 0; proLen < sortedprocedureList.size(); proLen++) {

			}

			for (int proLen = 0; proLen < sortedprocedureList.size(); proLen++) {
				Element procedure = sortedprocedureList.get(proLen);
				String proCabudgetDocumentTypeory = procedure
						.getAttribute("category");

				if (proCabudgetDocumentTypeory.equals("")) {
					proCabudgetDocumentTypeory = "NONE";
				}
				if (proCabudgetDocumentTypeory.equals("Misc.")) {
					proCabudgetDocumentTypeory = "MISC";
				}
				
				String billingNotes = "";
				String clinicalNotes = "";
				try{
				Element procedureClinicalNoteEle = (Element) procedure.getElementsByTagName("clinical-notes").item(0);
				clinicalNotes= procedureClinicalNoteEle.getTextContent();
				}catch(Exception e){
					//no clinical notes
				}
				
				try{
					Element procedureBillingNoteEle = (Element) procedure.getElementsByTagName("notes").item(0);
					billingNotes= procedureBillingNoteEle.getTextContent();
				}catch(Exception e){
						//no billing notes
				}
				String procedureNotes = "";
				if(!billingNotes.isEmpty()){
					procedureNotes +=  "Billing Notes: "+billingNotes+"\n";
				}
				if(!clinicalNotes.isEmpty()){
					procedureNotes +=  "Clinical Notes: "+clinicalNotes;
				}
				
				if (proCabudgetDocumentTypeory.equals(procedureTypeList
						.get(proTypeLen))) {

					curRow++;

					procedureNameRow = sheet.createRow(curRow);

					Cell cellForNotes = procedureNameRow
							.createCell(placeForNotes);
					if (budgetDocumentType != BudgetDocumentType.NO_NOTES) {
						cellForNotes.setCellStyle(styleForTitle);
					} else {
						cellForNotes.setCellStyle(color.get(11));
					}

					Cell cellForCpt = procedureNameRow.createCell(0);
					cellForCpt.setCellStyle(styleForTitle);
					cellForPro = procedureNameRow.createCell(1);
					if (curRow % 2 == 0) {
						Font font2 = wb.createFont();
						font2.setColor(HSSFColor.BLUE.index);
						style.setFont(font2);
						style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT
								.getIndex());
						style.setFillPattern(CellStyle.SOLID_FOREGROUND);
						style.setBorderBottom(CellStyle.BORDER_THIN);
						style.setBorderLeft(CellStyle.BORDER_THIN);
						style.setBorderRight(CellStyle.BORDER_THIN);
						style.setBorderTop(CellStyle.BORDER_THIN);
						style.setVerticalAlignment(CellStyle.VERTICAL_TOP);
						style.setWrapText(true);
						cellForPro.setCellStyle(style);
					} else {
						Font font2 = wb.createFont();
						font2.setColor(HSSFColor.BLUE.index);
						styleForProList2.setFont(font2);
						styleForProList2
								.setFillForegroundColor(IndexedColors.WHITE
										.getIndex());
						styleForProList2
								.setFillPattern(CellStyle.SOLID_FOREGROUND);
						styleForProList2.setBorderBottom(CellStyle.BORDER_THIN);
						styleForProList2.setBorderLeft(CellStyle.BORDER_THIN);
						styleForProList2.setBorderRight(CellStyle.BORDER_THIN);
						styleForProList2.setBorderTop(CellStyle.BORDER_THIN);
						styleForProList2
								.setVerticalAlignment(CellStyle.VERTICAL_TOP);
						styleForProList2.setWrapText(true);
						cellForPro.setCellStyle(styleForProList2);
					}

					proForVp.put(procedure.getAttribute("id"), procedureNameRow);

					proForColor.put(colorRowIndex, procedureNameRow);
					colorRowIndex++;

					// create cellstyle for cptcode
					CellStyle cptCodeCellStyle = wb.createCellStyle();
					cptCodeCellStyle.setBorderBottom(CellStyle.BORDER_THIN);
					cptCodeCellStyle.setBorderLeft(CellStyle.BORDER_THIN);
					cptCodeCellStyle.setBorderRight(CellStyle.BORDER_THIN);
					cptCodeCellStyle.setBorderTop(CellStyle.BORDER_THIN);
					cptCodeCellStyle.setAlignment(CellStyle.ALIGN_CENTER);
					cptCodeCellStyle
							.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
					cptCodeCellStyle.setWrapText(true);
					
					

					if (procedure.getAttribute("cptcode").equals("0")) {
						cellForPro.setCellValue(createHelper
								.createRichTextString(procedure
										.getAttribute("description")));

						String[] splitProName = procedure.getAttribute(
								"description").split(" ");
						wordLength = 0;
						rowHight = 1;
						for (int wordNum = 0; wordNum < splitProName.length; wordNum++) {
							wordLength = wordLength
									+ splitProName[wordNum].length() * 256;
							// in case the words is too long and directly start
							// from the next line
							if (splitProName[wordNum].length() * 256 > procedureCellWidth
									&& wordNum > 0)
								wordLength += procedureCellWidth
										- splitProName[wordNum - 1].length()
										* 256;
						}
						if (wordLength > procedureCellWidth) {
							rowHight += wordLength / procedureCellWidth;
						}
						procedureNameRow.setHeightInPoints((rowHight * sheet
								.getDefaultRowHeightInPoints()));
					}

					else if ("Misc.".equals(procedure.getAttribute("category"))) {
						String conditionalTag = "";
						if(procedure.getAttribute("alternative").trim().endsWith("true")){
							conditionalTag +=" [ ";
						}
						else if(procedure.getAttribute("conditional").trim().endsWith("true")){
							conditionalTag +=" * ";
						}
						
						cellForCpt.setCellValue(createHelper
								.createRichTextString(conditionalTag+"MISC"));
						
						cellForCpt.setCellStyle(cptCodeCellStyle);

						cellForPro.setCellValue(createHelper
								.createRichTextString(procedure
										.getAttribute("description")));
						String[] splitProName = procedure.getAttribute(
								"description").split(" ");
						wordLength = 0;
						rowHight = 1;
						for (int wordNum = 0; wordNum < splitProName.length; wordNum++) {
							wordLength = wordLength
									+ splitProName[wordNum].length() * 256;
							// in case the words is too long and directly start
							// from the next line
							if (splitProName[wordNum].length() * 256 > procedureCellWidth
									&& wordNum > 0)
								wordLength += procedureCellWidth
										- splitProName[wordNum - 1].length()
										* 256;
						}
						if (wordLength > procedureCellWidth) {
							rowHight += wordLength / procedureCellWidth;
						}

						procedureNameRow.setHeightInPoints((rowHight * sheet
								.getDefaultRowHeightInPoints()));

					} else {
						String conditionalTag = "";
						if(procedure.getAttribute("alternative").trim().endsWith("true")){
							conditionalTag +=" [ ";
						}
						else if(procedure.getAttribute("conditional").trim().endsWith("true")){
							conditionalTag +=" * ";
						}
						
						cellForCpt.setCellValue(createHelper
								.createRichTextString(conditionalTag+procedure
										.getAttribute("cptcode")));
						
						
						cellForCpt.setCellStyle(cptCodeCellStyle);

						BigDecimal hostpitalChargeDec = null;
						String hostpitalChargeStr = "";
						String description = "";

						if (budgetDocumentType.equals(BudgetDocumentType.FULL)
								|| budgetDocumentType
										.equals(BudgetDocumentType.NO_NOTES)) {
							if (!procedure.getAttribute("cptcode").isEmpty()) {
								if (hospitalChargeProcedureDao.findByCptCode(
										procedure.getAttribute("cptcode"))
										.size() > 0)
									hostpitalChargeDec = hospitalChargeProcedureDao
											.findByCptCode(
													procedure
															.getAttribute("cptcode"))
											.get(0).getCost();
								if (hostpitalChargeDec != null)
									hostpitalChargeStr = hostpitalChargeDec
											.toString();

							}

							if (!hostpitalChargeStr.isEmpty())
								description = procedure
										.getAttribute("description")
										+ " (H $"
										+ hostpitalChargeStr + ")";
							else
								description = procedure
										.getAttribute("description");

						} else
							description = procedure.getAttribute("description")
									.toUpperCase();

						cellForPro.setCellValue(createHelper
								.createRichTextString(description));

						String[] splitProName = description.split(" ");

						wordLength = 0;
						rowHight = 1;
						for (int wordNum = 0; wordNum < splitProName.length; wordNum++) {
							wordLength = wordLength
									+ splitProName[wordNum].length() * 256;
							// in case the words is too long and directly start
							// from the next line
							if (splitProName[wordNum].length() * 256 > procedureCellWidth
									&& wordNum > 0)
								wordLength += procedureCellWidth
										- splitProName[wordNum - 1].length()
										* 256;
						}
						if (wordLength > procedureCellWidth) {
							rowHight += wordLength / procedureCellWidth;
						}
						procedureNameRow.setHeightInPoints((rowHight * sheet
								.getDefaultRowHeightInPoints()));
					}

					// this part works for full budget and price only
					CellStyle moneyCellStyle = wb.createCellStyle();
					moneyCellStyle.setBorderBottom(CellStyle.BORDER_THIN);
					moneyCellStyle.setBorderLeft(CellStyle.BORDER_THIN);
					moneyCellStyle.setBorderRight(CellStyle.BORDER_THIN);
					moneyCellStyle.setBorderTop(CellStyle.BORDER_THIN);
					moneyCellStyle.setAlignment(CellStyle.ALIGN_CENTER);
					moneyCellStyle
							.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
					moneyCellStyle.setWrapText(true);
					moneyCellStyle.setDataFormat(HSSFDataFormat
							.getBuiltinFormat("0.00"));

					CellStyle notesCellStyle = wb.createCellStyle();
					notesCellStyle.setBorderBottom(CellStyle.BORDER_THIN);
					notesCellStyle.setBorderLeft(CellStyle.BORDER_THIN);
					notesCellStyle.setBorderRight(CellStyle.BORDER_THIN);
					notesCellStyle.setBorderTop(CellStyle.BORDER_THIN);
					notesCellStyle.setWrapText(true);
					notesCellStyle.setVerticalAlignment(CellStyle.VERTICAL_TOP);
					notesCellStyle.setDataFormat(HSSFDataFormat
							.getBuiltinFormat("0.00"));

					if (budgetDocumentType
							.equals(BudgetDocumentType.PRICE_ONLY)) {
						Cell cellForPrice = procedureNameRow.createCell(2);

						NodeList priceList = procedure
								.getElementsByTagName("price");
						Node priceNode = priceList.item(0);

						float priceCost = Float.valueOf(priceNode
								.getTextContent());

						BigDecimal bd = new BigDecimal((double) priceCost);
						priceCost = bd.floatValue();

						NumberFormat nf = NumberFormat.getNumberInstance();
						DecimalFormat df = (DecimalFormat) nf;
						df.applyPattern("##0.00");
						String tempString = df.format(priceCost);
						/*
						 * String tempString = String.valueOf(priceCost);
						 * 
						 * if ((tempString + " ").indexOf(".") >= 0) { if
						 * ((tempString + " ").length() - (tempString +
						 * " ").indexOf(".") - 2 == 0) { tempString = tempString
						 * + ".00"; } else if ((tempString + " ").length() -
						 * (tempString + " ").indexOf(".") - 2 == 1) {
						 * tempString = tempString + "0"; } }
						 */

						cellForPrice.setCellValue(createHelper
								.createRichTextString("$" + tempString));

						cellForPrice.setCellStyle(moneyCellStyle);

					}
					/*
					 * if (budgetDocumentType.equals(BudgetDocumentType.ALL)) {
					 * Cell cellForCost = procedureName.createCell(2); Cell
					 * cellForPrice = procedureName.createCell(3);
					 * 
					 * NodeList hospList = procedure
					 * .getElementsByTagName("hosp"); NodeList physList =
					 * procedure .getElementsByTagName("phys"); Element hosp =
					 * (Element) hospList.item(0); Element phys = (Element)
					 * physList.item(0);
					 * 
					 * float cost = Float.valueOf(hosp.getAttribute("cost")) +
					 * Float.valueOf(phys.getAttribute("cost")); // cut two
					 * digit BigDecimal bd = new BigDecimal((double) cost); bd =
					 * bd.setScale(scale, roundingMode); cost = bd.floatValue();
					 * 
					 * String costStr = ""; costStr = costStr + cost;
					 * 
					 * NodeList priceList = procedure
					 * .getElementsByTagName("price"); Node priceNode =
					 * priceList.item(0);
					 * 
					 * cellForCost.setCellValue(createHelper
					 * .createRichTextString(costStr));
					 * cellForCost.setCellStyle(styleForTitle);
					 * 
					 * float priceCost = Float.valueOf(priceNode
					 * .getTextContent());
					 * 
					 * priceCost = bd.floatValue(); String tempString =
					 * String.valueOf(priceCost);
					 * cellForPrice.setCellValue(createHelper
					 * .createRichTextString(tempString));
					 * 
					 * cellForPrice.setCellStyle(styleForTitle);
					 * 
					 * }
					 */

					if (budgetDocumentType.equals(BudgetDocumentType.FULL)
							|| budgetDocumentType
									.equals(BudgetDocumentType.NO_NOTES)
							|| budgetDocumentType
									.equals(BudgetDocumentType.ALL)) {
						Cell cellForCost = procedureNameRow.createCell(2);
						Cell cellForSpon = procedureNameRow.createCell(3);
						Cell cellForPrice = procedureNameRow.createCell(4);
						Cell cellForResidual = procedureNameRow.createCell(5);

						NodeList hospList = procedure
								.getElementsByTagName("hosp");
						NodeList physList = procedure
								.getElementsByTagName("phys");
						Element hosp = (Element) hospList.item(0);
						Element phys = (Element) physList.item(0);

						Element costNodes = (Element) procedure
								.getElementsByTagName("cost").item(0);
						NodeList miscList = costNodes
								.getElementsByTagName("misc");
						Element miscEle = (Element) miscList.item(0);

						float cost = Float.valueOf(hosp.getAttribute("cost"))
								+ Float.valueOf(phys.getAttribute("cost"))
								+ Float.valueOf(miscEle.getTextContent());

						BigDecimal bd = new BigDecimal((double) cost);
						bd = bd.setScale(scale, roundingMode);
						cost = bd.floatValue();

						NumberFormat nf = NumberFormat.getNumberInstance();
						DecimalFormat df = (DecimalFormat) nf;
						df.applyPattern("##0.00");

						String costStr = df.format(cost);
						// costStr = costStr + cost;

						NodeList sponsorList = procedure
								.getElementsByTagName("sponsor");
						Node sponsorNode = sponsorList.item(0);

						NodeList priceList = procedure
								.getElementsByTagName("price");
						Node priceNode = priceList.item(0);

						NodeList residualList = procedure
								.getElementsByTagName("residual");
						Node residualNode = residualList.item(0);

						/*
						 * if ((costStr + " ").indexOf(".") >= 0) { if ((costStr
						 * + " ").length() - (costStr + " ").indexOf(".") - 2 ==
						 * 0) { costStr = costStr + ".00"; } else if ((costStr +
						 * " ").length() - (costStr + " ").indexOf(".") - 2 ==
						 * 1) { costStr = costStr + "0"; } }
						 */

						cellForCost.setCellValue(createHelper
								.createRichTextString("$" + costStr));

						cellForCost.setCellStyle(moneyCellStyle);

						float sponsorCost = 0;
						float priceCost = 0;
						float residualCost = 0;

						if (!sponsorNode.getTextContent().isEmpty())
							sponsorCost = Float.valueOf(sponsorNode
									.getTextContent());

						bd = new BigDecimal((double) sponsorCost);
						sponsorCost = bd.floatValue();

						// String tempString = String.valueOf(sponsorCost);
						String tempString = df.format(sponsorCost);

						// logger.debug(tempString);

						/*
						 * if ((tempString + " ").indexOf(".") >= 0) { if
						 * ((tempString + " ").length() - (tempString +
						 * " ").indexOf(".") - 2 == 0) { tempString = tempString
						 * + ".00"; } else if ((tempString + " ").length() -
						 * (tempString + " ").indexOf(".") - 2 == 1) {
						 * tempString = tempString + "0"; } }
						 */

						cellForSpon.setCellValue(createHelper
								.createRichTextString("$" + tempString));

						cellForSpon.setCellStyle(moneyCellStyle);

						if (!priceNode.getTextContent().isEmpty())
							priceCost = Float.valueOf(priceNode
									.getTextContent());

						bd = new BigDecimal((double) priceCost);
						priceCost = bd.floatValue();
						tempString = df.format(priceCost);
						/*
						 * tempString = String.valueOf(priceCost);
						 * 
						 * if ((tempString + " ").indexOf(".") >= 0) { if
						 * ((tempString + " ").length() - (tempString +
						 * " ").indexOf(".") - 2 == 0) { tempString = tempString
						 * + ".00"; } else if ((tempString + " ").length() -
						 * (tempString + " ").indexOf(".") - 2 == 1) {
						 * tempString = tempString + "0"; } }
						 */
						cellForPrice.setCellValue(createHelper
								.createRichTextString("$" + tempString));

						cellForPrice.setCellStyle(moneyCellStyle);

						if (!residualNode.getTextContent().isEmpty())
							residualCost = Float.valueOf(residualNode
									.getTextContent());

						bd = new BigDecimal((double) residualCost);
						residualCost = bd.floatValue();
						String residualString = df.format(residualCost);
						/*
						 * tempString = String.valueOf(residualCost);
						 * 
						 * if ((tempString + " ").indexOf(".") >= 0) { if
						 * ((tempString + " ").length() - (tempString +
						 * " ").indexOf(".") - 2 == 0) { tempString = tempString
						 * + ".00"; } else if ((tempString + " ").length() -
						 * (tempString + " ").indexOf(".") - 2 == 1) {
						 * tempString = tempString + "0"; } }
						 */

						cellForResidual.setCellValue(createHelper
								.createRichTextString("$" + residualString));

						cellForResidual.setCellStyle(moneyCellStyle);
					}
					if (!budgetDocumentType.equals(BudgetDocumentType.NO_NOTES)) {
						cellForNotes.setCellValue(procedureNotes);
						// calculate space for notes
						String[] splitNotes = procedureNotes.split(" ");
						wordLength = 0;
						rowHight = 1;
						for (int wordNum = 0; wordNum < splitNotes.length; wordNum++) {
							wordLength = wordLength
									+ splitNotes[wordNum].length() * 256;
							// in case the words is too long and directly start
							// from
							// the
							// next line
							if(splitNotes[wordNum].contains("\n")){
								wordLength+=20000;
							}
							if (splitNotes[wordNum].length() * 256 > 10000
									&& wordNum > 0)
								wordLength += 10000 - splitNotes[wordNum - 1]
										.length() * 256;
						}
						if (wordLength > 10000) {
							rowHight += wordLength / 10000;
						}
						rowHight++;
						if (rowHight > 1)
							procedureNameRow
									.setHeightInPoints((rowHight * sheet
											.getDefaultRowHeightInPoints()));

						cellForNotes.setCellStyle(notesCellStyle);
					} else {
						cellForNotes.setCellStyle(color.get(11));
					}
					//
				}
				// for sub procedure
				/*
				 * if (subproceduresList.getLength() > 0) {
				 * 
				 * for (int subProLen = 0; subProLen < subproceduresList
				 * .getLength(); subProLen++) {
				 * 
				 * Element subprocedures = (Element) subproceduresList
				 * .item(subProLen); NodeList subprocedureList = subprocedures
				 * .getElementsByTagName("procedure"); for (int procedureLen =
				 * 0; procedureLen < subprocedureList .getLength();
				 * procedureLen++) { Element subProcudure = (Element)
				 * subprocedureList .item(procedureLen); if
				 * (subprocedureTypeList.contains(subProcudure
				 * .getAttribute("id"))) continue;
				 * 
				 * subprocedureTypeList.add(subProcudure .getAttribute("id"));
				 * curRow++;
				 * 
				 * procedureName = sheet.createRow(curRow); cellForPro =
				 * procedureName.createCell(0); if (curRow % 2 == 0) {
				 * style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT
				 * .getIndex()); HSSFFont font = wb.createFont();
				 * font.setColor(HSSFColor.BLUE.index); style.setFont(font);
				 * style.setFillPattern(CellStyle.SOLID_FOREGROUND);
				 * cellForPro.setCellStyle(style); } else { HSSFFont font =
				 * wb.createFont(); font.setColor(HSSFColor.BLUE.index);
				 * styleForProList2.setFont(font); styleForProList2
				 * .setFillForegroundColor(IndexedColors.WHITE .getIndex());
				 * styleForProList2 .setFillPattern(CellStyle.SOLID_FOREGROUND);
				 * cellForPro.setCellStyle(styleForProList2); }
				 * proForVp.put(subProcudure.getAttribute("id"), procedureName);
				 * proForColor.put(colorRowIndex, procedureName);
				 * colorRowIndex++;
				 * 
				 * if (subProcudure.getAttribute("cptcode") .equals("0"))
				 * cellForPro.setCellValue(createHelper
				 * .createRichTextString(subProcudure
				 * .getAttribute("description"))); else cellForPro
				 * .setCellValue(createHelper.createRichTextString(subProcudure
				 * .getAttribute("cptcode") + ": " + subProcudure
				 * .getAttribute("description"))); } }
				 * cellForNotes.setCellValue(notes.getTextContent()); }
				 */

			}
			curRow++;
		}
		indexBeforeTotal = proForColor.size();
		addProTotal(curRow, sheet, budgetDocumentType, createHelper,
				proForColor, colorRowIndex, wb, fa, placeForNotes);
		return indexBeforeTotal;

	}

	private int addVisit(Element arm, Element epoch, Sheet sheet,
			CreationHelper createHelper, Row rowForTitle,
			Map<String, Row> proForVp, CellStyle style,
			Map<Integer, CellStyle> color, Map<Integer, Row> proForColor,
			int armIndexForColor, int visitCellNum, HSSFWorkbook wb,
			int indexBeforeTotal, BudgetDocumentType budgetDocumentType,
			String fa, List<String> subprocedureIdList,
			Map<String, Float> totalMapResult) {
		NodeList visitsList = arm.getElementsByTagName("visits");
		HSSFCellStyle styleForFont = wb.createCellStyle();
		HSSFFont font = wb.createFont();
		font.setColor(HSSFColor.BLUE.index);
		styleForFont.setFont(font);
		styleForFont.setBorderBottom(CellStyle.BORDER_THIN);
		styleForFont.setBorderLeft(CellStyle.BORDER_THIN);
		styleForFont.setBorderRight(CellStyle.BORDER_THIN);
		styleForFont.setBorderTop(CellStyle.BORDER_THIN);
		styleForFont.setAlignment(CellStyle.ALIGN_CENTER);
		styleForFont.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
		styleForFont.setWrapText(true);

		while (armIndexForColor > 9)
			armIndexForColor = armIndexForColor - 8;

		int scale = 2;
		int roundingMode = 4;
		float iniCost = 0.00f;
		NumberFormat nf = NumberFormat.getNumberInstance();
		DecimalFormat df = (DecimalFormat) nf;
		df.applyPattern("##0.00");
		df.format(iniCost);
		BigDecimal bd = new BigDecimal((double) iniCost);
		bd = bd.setScale(scale, roundingMode);
		iniCost = bd.floatValue();

		totalMapResult.put("R", iniCost);
		totalMapResult.put("C", iniCost);
		totalMapResult.put("I", iniCost);
		totalMapResult.put("CNMS", iniCost);
		totalMapResult.put("RNS", iniCost);
		totalMapResult.put("CL", iniCost);

		for (int visitsLen = 0; visitsLen < visitsList.getLength(); visitsLen++) {
			Element visits = (Element) visitsList.item(visitsLen);
			NodeList visitList = visits.getElementsByTagName("visit");

			// sort visit by cycleindex
			// List<Element> sortedVisitList = new ArrayList<Element>();
			// List<Float> sortvisitOrnderList = new ArrayList<Float>();

			/*
			 * // sort the visit by index Map<Float, Element> visitSortMap = new
			 * HashMap<Float, Element>(); try { for (int i = 0; i <
			 * visitList.getLength(); i++) { Element visit = (Element)
			 * visitList.item(i); float visitOrder = Float.valueOf(visit
			 * .getAttribute("cycleindex")); visitSortMap.put(visitOrder,
			 * visit); sortvisitOrnderList.add(visitOrder); }
			 * Collections.sort(sortvisitOrnderList); //second round sort, sort
			 * visits in the same day by name
			 * 
			 * for (int i = 0; i < sortvisitOrnderList.size(); i++) {
			 * sortedVisitList.add(visitSortMap.get(sortvisitOrnderList
			 * .get(i))); }
			 * 
			 * } catch (Exception e) { e.printStackTrace(); }
			 */
			// List<Element> sortedVisitList = new ArrayList<Element>();
			Element[] visitArray = new Element[visitList.getLength()];
			try {

				for (int i = 0; i < visitList.getLength(); i++) {
					Element visit = (Element) visitList.item(i);
					visitArray[i] = visit;
				}

				// begin to sort by index
				for (int i = 0; i < visitArray.length; i++) {
					float visitDayIdex1 = Float.valueOf(visitArray[i]
							.getAttribute("cycleindex"));
					for (int j = i + 1; j < visitArray.length; j++) {
						float visitDayIdex2 = Float.valueOf(visitArray[j]
								.getAttribute("cycleindex"));
						if (visitDayIdex1 > visitDayIdex2) {
							Element tempEle = visitArray[i];

							visitArray[i] = visitArray[j];

							visitArray[j] = tempEle;
						}
					}
				}

				// second round sort, sort by name for those with same index
				for (int i = 0; i < visitArray.length; i++) {
					String visitName1 = visitArray[i].getAttribute("name");
					float visitDayIdex1 = Float.valueOf(visitArray[i]
							.getAttribute("cycleindex"));

					for (int j = i + 1; j < visitArray.length; j++) {
						String visitName2 = visitArray[j].getAttribute("name");
						float visitDayIdex2 = Float.valueOf(visitArray[j]
								.getAttribute("cycleindex"));
						if (visitDayIdex1 == visitDayIdex2) {
							if (visitName1.compareTo(visitName2) > 0) {
								Element tempEle = visitArray[i];

								visitArray[i] = visitArray[j];

								visitArray[j] = tempEle;
							}
						}
					}
				}

			} catch (Exception e) {
				e.printStackTrace();
			}

			int rowHightTotal = 1;
			for (int visitLen = 0; visitLen < visitArray.length; visitLen++) {
				Element visit = visitArray[visitLen];
				Cell visitTitleCell = rowForTitle.createCell(visitCellNum);
				if (armIndexForColor % 3 == 1) {
					style = color.get(armIndexForColor);
					armIndexForColor++;
				} else if (armIndexForColor % 3 == 2) {
					style = color.get(armIndexForColor);
					armIndexForColor = armIndexForColor - 1;
				}
				visitTitleCell.setCellValue(visit.getAttribute("name"));

				String[] splitVisitName = visit.getAttribute("name").split(" ");
				int wordLength = 0;
				int rowHight = 1;
				int procedureCellWidth = 1000;
				for (int wordNum = 0; wordNum < splitVisitName.length; wordNum++) {
					wordLength = wordLength + splitVisitName[wordNum].length()
							* 256 + 256;
					if (wordLength + 400 > procedureCellWidth) {
						rowHight++;
						wordLength = splitVisitName[wordNum].length();
					}

				}
				if (rowHightTotal < rowHight)
					rowHightTotal = rowHight;
				rowForTitle.setHeightInPoints((rowHightTotal * sheet
						.getDefaultRowHeightInPoints()));

				visitTitleCell.setCellStyle(styleForFont);

				for (int k = 0; k < proForColor.size(); k++) {
					Row RowForColor = proForColor.get(k);
					Cell colorCell = RowForColor.createCell(visitCellNum);
					if (k < indexBeforeTotal)
						colorCell.setCellValue("");
					else
						colorCell.setCellValue("$0.00");

					colorCell.setCellStyle(style);

				}
				addvp(visitCellNum, visit, proForVp, style, subprocedureIdList);
				totalMapResult = totalCalculation(visitCellNum, visit, epoch,
						subprocedureIdList, totalMapResult);
				// totalMapResult.put("C",totalMapResult.get("C")+totalMapForTempCla.get("C"));
				if (budgetDocumentType.equals(BudgetDocumentType.FULL)
						|| budgetDocumentType
								.equals(BudgetDocumentType.NO_NOTES)
						|| budgetDocumentType
								.equals(BudgetDocumentType.PRICE_ONLY)
						|| budgetDocumentType.equals(BudgetDocumentType.ALL)) {

					addTotalButton(visitCellNum, visit, budgetDocumentType,
							indexBeforeTotal, proForColor, style, fa);

				}
				visitCellNum++;

			}

		}
		// since only I and R has fa
		Float faFloat = Float.valueOf(fa) / 100;
		totalMapResult.put("I", totalMapResult.get("I") * (1 + faFloat));
		totalMapResult.put("R", totalMapResult.get("R") * (1 + faFloat));
		return visitCellNum;
	}

	private Map<String, Float> totalCalculation(int visitCellNum,
			Element visit, Element epoch, List<String> subprocedureIdList,
			Map<String, Float> totalMapForCal) {
		NodeList vpList = visit.getElementsByTagName("vp");
		NodeList procedureList = epoch.getElementsByTagName("procedure");
		int scale = 2;
		int roundingMode = 4;
		NumberFormat nf = NumberFormat.getNumberInstance();
		DecimalFormat df = (DecimalFormat) nf;
		df.applyPattern("##0.00");

		for (int vpLen = 0; vpLen < vpList.getLength(); vpLen++) {
			Element vp = (Element) vpList.item(vpLen);
			if (subprocedureIdList.contains(vp.getAttribute("pid"))) {
				continue;
			}

			String type = vp.getAttribute("t");
			String repeatStr = vp.getAttribute("r");
			float repeatNum = Float.valueOf(repeatStr);
			int subjectNum = 0;
			String subjectNumStr = visit.getAttribute("subj");
			if (!subjectNumStr.equals(""))
				subjectNum = Integer.valueOf(subjectNumStr);
			if (totalMapForCal.containsKey(type)) {
				String pid = vp.getAttribute("pid");
				for (int j = 0; j < procedureList.getLength(); j++) {
					Element procedureEle = (Element) procedureList.item(j);
					if (procedureEle.getAttribute("id").equals(pid)) {
						NodeList priceList = procedureEle
								.getElementsByTagName("price");
						Element priceTEle = (Element) priceList.item(0);
						float priceCost = 0;
						if (!priceTEle.getTextContent().isEmpty())
							priceCost = Float.valueOf(priceTEle
									.getTextContent());
						BigDecimal bd = new BigDecimal((double) priceCost);
						bd = bd.setScale(scale, roundingMode);
						priceCost = bd.floatValue();
						priceCost = priceCost * repeatNum;
						df.format(priceCost);
						totalMapForCal.put(type, totalMapForCal.get(type)
								+ priceCost * subjectNum);
					}
				}
			}

		}
		return totalMapForCal;
	}

	private int addTypeForTotal(int visitCellNum, Row rowForTitle,
			CreationHelper createHelper, BudgetDocumentType budgetDocumentType,
			Element arm, Map<String, Row> proForVp, CellStyle style,
			int indexBeforeTotal, Map<Integer, Row> proForColor, String fa,
			int armIndexForColor, Map<Integer, CellStyle> color,
			HSSFWorkbook wb, Map<String, Float> totalMapResult) {
		String visitTypeStr;
		// only R and I has f&a
		String fa2 = "0";

		CellStyle styleForTitle = wb.createCellStyle();
		styleForTitle = color.get(10);

		while (armIndexForColor > 9)
			armIndexForColor = armIndexForColor - 9;
		style = color.get(armIndexForColor + 2);

		if (budgetDocumentType.equals(BudgetDocumentType.PRICE_ONLY)) {
			Cell cellForTotal = rowForTitle.createCell(visitCellNum);
			cellForTotal.setCellValue("Total R");
			cellForTotal.setCellStyle(styleForTitle);
			visitTypeStr = "r";

			addpt(visitCellNum, arm, proForVp, style, "price", visitTypeStr,
					indexBeforeTotal, proForColor, fa, budgetDocumentType,
					totalMapResult);
			visitCellNum++;

			cellForTotal = rowForTitle.createCell(visitCellNum);
			cellForTotal.setCellValue("Total C");
			cellForTotal.setCellStyle(styleForTitle);

			visitTypeStr = "c";
			addpt(visitCellNum, arm, proForVp, style, "price", visitTypeStr,
					indexBeforeTotal, proForColor, fa2, budgetDocumentType,
					totalMapResult);
			visitCellNum++;

			cellForTotal = rowForTitle.createCell(visitCellNum);
			cellForTotal.setCellValue("Total I");
			cellForTotal.setCellStyle(styleForTitle);

			visitTypeStr = "i";
			addpt(visitCellNum, arm, proForVp, style, "price", visitTypeStr,
					indexBeforeTotal, proForColor, fa, budgetDocumentType,
					totalMapResult);

			visitCellNum++;

			cellForTotal = rowForTitle.createCell(visitCellNum);
			cellForTotal.setCellValue("Total RNS");
			cellForTotal.setCellStyle(styleForTitle);

			visitTypeStr = "rns";
			addpt(visitCellNum, arm, proForVp, style, "price", visitTypeStr,
					indexBeforeTotal, proForColor, fa2, budgetDocumentType,
					totalMapResult);

			visitCellNum++;

			cellForTotal = rowForTitle.createCell(visitCellNum);
			cellForTotal.setCellValue("Total CNMS");
			cellForTotal.setCellStyle(styleForTitle);

			visitTypeStr = "cnms";
			addpt(visitCellNum, arm, proForVp, style, "price", visitTypeStr,
					indexBeforeTotal, proForColor, fa2, budgetDocumentType,
					totalMapResult);

			visitCellNum++;

			cellForTotal = rowForTitle.createCell(visitCellNum);
			cellForTotal.setCellValue("Total CL");
			cellForTotal.setCellStyle(styleForTitle);

			visitTypeStr = "cl";
			addpt(visitCellNum, arm, proForVp, style, "price", visitTypeStr,
					indexBeforeTotal, proForColor, fa2, budgetDocumentType,
					totalMapResult);

			visitCellNum++;
		}

		if (budgetDocumentType.equals(BudgetDocumentType.FULL)
				|| budgetDocumentType.equals(BudgetDocumentType.NO_NOTES)
				|| budgetDocumentType.equals(BudgetDocumentType.ALL)) {
			List<String> ListforCost = new ArrayList<String>();
			ListforCost.add("price");
			/*
			 * ListforCost.add("price"); ListforCost.add("sponsor");
			 */

			for (int i = 0; i < ListforCost.size(); i++) {
				Cell cellForTotal = rowForTitle.createCell(visitCellNum);
				cellForTotal.setCellValue("Total R");
				cellForTotal.setCellStyle(styleForTitle);

				visitTypeStr = "r";

				addpt(visitCellNum, arm, proForVp, style, ListforCost.get(i),
						visitTypeStr, indexBeforeTotal, proForColor, fa,
						budgetDocumentType, totalMapResult);
				visitCellNum++;

				cellForTotal = rowForTitle.createCell(visitCellNum);
				cellForTotal.setCellValue("Total C");
				cellForTotal.setCellStyle(styleForTitle);

				visitTypeStr = "c";
				addpt(visitCellNum, arm, proForVp, style, ListforCost.get(i),
						visitTypeStr, indexBeforeTotal, proForColor, fa2,
						budgetDocumentType, totalMapResult);
				visitCellNum++;

				cellForTotal = rowForTitle.createCell(visitCellNum);
				cellForTotal.setCellValue("Total I");
				cellForTotal.setCellStyle(styleForTitle);

				visitTypeStr = "i";
				addpt(visitCellNum, arm, proForVp, style, ListforCost.get(i),
						visitTypeStr, indexBeforeTotal, proForColor, fa,
						budgetDocumentType, totalMapResult);

				visitCellNum++;

				cellForTotal = rowForTitle.createCell(visitCellNum);
				cellForTotal.setCellValue("Total RNS");
				cellForTotal.setCellStyle(styleForTitle);

				visitTypeStr = "rns";
				addpt(visitCellNum, arm, proForVp, style, ListforCost.get(i),
						visitTypeStr, indexBeforeTotal, proForColor, fa2,
						budgetDocumentType, totalMapResult);

				visitCellNum++;

				cellForTotal = rowForTitle.createCell(visitCellNum);
				cellForTotal.setCellValue("Total CNMS");
				cellForTotal.setCellStyle(styleForTitle);

				visitTypeStr = "cnms";
				addpt(visitCellNum, arm, proForVp, style, ListforCost.get(i),
						visitTypeStr, indexBeforeTotal, proForColor, fa2,
						budgetDocumentType, totalMapResult);

				visitCellNum++;

				cellForTotal = rowForTitle.createCell(visitCellNum);
				cellForTotal.setCellValue("Total CL");
				cellForTotal.setCellStyle(styleForTitle);

				visitTypeStr = "cl";
				addpt(visitCellNum, arm, proForVp, style, ListforCost.get(i),
						visitTypeStr, indexBeforeTotal, proForColor, fa2,
						budgetDocumentType, totalMapResult);

				visitCellNum++;

			}

		}

		return visitCellNum;
	}

	private void addvp(int visitCellNum, Element visit,
			Map<String, Row> proForVp, CellStyle style,
			List<String> subprocedureIdList) {
		NodeList vpList = visit.getElementsByTagName("vp");
		for (int vpLen = 0; vpLen < vpList.getLength(); vpLen++) {
			Element vp = (Element) vpList.item(vpLen);
			if (subprocedureIdList.contains(vp.getAttribute("pid"))) {
				continue;
			}

			// System.out.println(vp.getAttribute("pid")); //for debug
			if (!proForVp.containsKey(vp.getAttribute("pid")))
				continue;
			Row RowForvp = proForVp.get(vp.getAttribute("pid"));

			// logger.debug("vplen is "+ vpLen);
			/*
			 * logger.debug("vpList.getLength() is "+
			 * subprocedureIdList.size()); for (int
			 * tett=0;tett<subprocedureIdList.size();tett++){
			 * logger.debug("subprocedureId  is "+
			 * subprocedureIdList.get(tett)); }
			 */
			// logger.debug("this is test"+vp.getAttribute("pid"));
			Cell vpCell = RowForvp.createCell(visitCellNum);

			vpCell.setCellStyle(style);
			if (vp.getAttribute("r").equals("1"))
				vpCell.setCellValue(vp.getAttribute("t"));
			else
				vpCell.setCellValue(vp.getAttribute("t") + "("
						+ vp.getAttribute("r") + ")");

		}
	}

	private void addpt(int visitCellNum, Element arm,
			Map<String, Row> proForPt, CellStyle style, String TotleType,
			String visitTypeStr, int indexBeforeTotal,
			Map<Integer, Row> proForColor, String fa,
			BudgetDocumentType budgetDocumentType,
			Map<String, Float> totalMapResult) {

		NodeList ptList = arm.getElementsByTagName("pt");
		NodeList amontTotalList = arm.getElementsByTagName("armtotals");
		Element ptForTotal = (Element) amontTotalList.item(0);
		NodeList typeTotalList = ptForTotal.getElementsByTagName("t");

		NumberFormat nf = NumberFormat.getNumberInstance();
		DecimalFormat df = (DecimalFormat) nf;
		df.applyPattern("##0.00");

		for (int ptLen = 0; ptLen < ptList.getLength(); ptLen++) {
			try {
				Element pt = (Element) ptList.item(ptLen);
				Row RowForPt = proForPt.get(pt.getAttribute("pid"));
				Cell ptCell = RowForPt.createCell(visitCellNum);
				NodeList tForPtList = pt.getElementsByTagName("t");
				for (int tForPtLen = 0; tForPtLen < tForPtList.getLength(); tForPtLen++) {
					Element tForPt = (Element) tForPtList.item(tForPtLen);
					if (tForPt.getAttribute("by").equals(TotleType)) {

						float tForPtCost = Float.valueOf(tForPt
								.getAttribute(visitTypeStr));

						int scale = 2;
						int roundingMode = 4;
						BigDecimal bd = new BigDecimal((double) tForPtCost);
						bd = bd.setScale(scale, roundingMode);
						tForPtCost = bd.floatValue();

						String tForPtCostStr = df.format(tForPtCost);

						ptCell.setCellValue("$" + tForPtCostStr);
						ptCell.setCellStyle(style);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				continue;
			}

		}

		if (budgetDocumentType.equals(BudgetDocumentType.PRICE_ONLY)
				|| budgetDocumentType.equals(BudgetDocumentType.FULL)
				|| budgetDocumentType.equals(BudgetDocumentType.NO_NOTES)
				|| budgetDocumentType.equals(BudgetDocumentType.ALL)) {
			Element typeTotal = (Element) typeTotalList.item(1);
			Row RowForptTotal = proForColor.get(indexBeforeTotal);
			Cell ptTotalCell = RowForptTotal.createCell(visitCellNum);
			if (typeTotal.getAttribute("by").equals(TotleType)) {
				float perSubDirCostTotal = Float.valueOf(typeTotal
						.getAttribute(visitTypeStr));

				int scale = 2;
				int roundingMode = 4;
				BigDecimal bd = new BigDecimal((double) perSubDirCostTotal);
				bd = bd.setScale(scale, roundingMode);
				perSubDirCostTotal = bd.floatValue();
				String perSubDirectTotalStr = df.format(perSubDirCostTotal);

				ptTotalCell.setCellValue("$" + perSubDirectTotalStr);
				ptTotalCell.setCellStyle(style);
				indexBeforeTotal++;
				ArmTotalCal(visitCellNum, proForColor, indexBeforeTotal, fa,
						style, perSubDirectTotalStr, totalMapResult,
						visitTypeStr);
			}
		}
	}

	private void ArmTotalCal(int visitCellNum, Map<Integer, Row> proForColor,
			int indexBeforeTotal, String fa, CellStyle style,
			String perSubDirectTotalStr, Map<String, Float> totalMapResult,
			String visitTypeStr) {
		float percenbudgetDocumentTypee = Float.valueOf(fa) / 100;
		float perSubDirectTotal;
		float FandA;
		float perSubDirectTotalTotal;
		String tempData;

		NumberFormat nf = NumberFormat.getNumberInstance();
		DecimalFormat df = (DecimalFormat) nf;

		int scale = 2;
		int roundingMode = 4;

		df.applyPattern("##0.00");
		Row RowForTotal = proForColor.get(indexBeforeTotal);
		Cell totalCell = RowForTotal.createCell(visitCellNum);

		RowForTotal = proForColor.get(indexBeforeTotal);
		totalCell = RowForTotal.createCell(visitCellNum);
		perSubDirectTotal = Float.valueOf(perSubDirectTotalStr);
		FandA = perSubDirectTotal * percenbudgetDocumentTypee;

		BigDecimal bd = new BigDecimal((double) FandA);
		bd = bd.setScale(scale, roundingMode);
		FandA = bd.floatValue();
		tempData = df.format(FandA);

		totalCell.setCellValue("$" + tempData);
		totalCell.setCellStyle(style);
		indexBeforeTotal++;

		RowForTotal = proForColor.get(indexBeforeTotal);
		totalCell = RowForTotal.createCell(visitCellNum);
		perSubDirectTotalTotal = perSubDirectTotal + FandA;

		bd = new BigDecimal((double) perSubDirectTotalTotal);
		bd = bd.setScale(scale, roundingMode);
		perSubDirectTotalTotal = bd.floatValue();

		tempData = df.format(perSubDirectTotalTotal);

		totalCell.setCellValue("$" + tempData);
		// logger.debug(tempData);
		totalCell.setCellStyle(style);
		indexBeforeTotal++;

		RowForTotal = proForColor.get(indexBeforeTotal);
		totalCell = RowForTotal.createCell(visitCellNum);
		indexBeforeTotal++;

		RowForTotal = proForColor.get(indexBeforeTotal);
		totalCell = RowForTotal.createCell(visitCellNum);

		totalCell.setCellValue("$"
				+ df.format(totalMapResult.get(visitTypeStr.toUpperCase())));
		totalCell.setCellStyle(style);
		indexBeforeTotal++;
	}

	private int totalCal(Element visit, int visitCellNum, Element cost,
			Map<Integer, Row> proForColor, int indexBeforeTotal, String fa,
			CellStyle style) {
		float faFloat = Float.valueOf(fa) / 100;
		float perSubDirect;
		float facAndAdmain;
		float perSubDirectTotal;
		float finalTotal;
		String tempData;
		int subjectNum = 0;
		String subject = visit.getAttribute("subj");
		if (!subject.equals(""))
			subjectNum = Integer.valueOf(subject);
		int scale = 2;
		int roundingMode = 4;

		NumberFormat nf = NumberFormat.getNumberInstance();
		DecimalFormat df = (DecimalFormat) nf;
		df.applyPattern("##0.00");

		Row RowForTotal = proForColor.get(indexBeforeTotal);
		Cell totalCell = RowForTotal.createCell(visitCellNum);

		Float tempValue = Float.valueOf(cost.getAttribute("psd"));
		BigDecimal bd = new BigDecimal((double) tempValue);
		bd = bd.setScale(scale, roundingMode);
		tempValue = bd.floatValue();

		tempData = df.format(tempValue);

		totalCell.setCellValue("$" + tempData);
		totalCell.setCellStyle(style);
		indexBeforeTotal++;

		RowForTotal = proForColor.get(indexBeforeTotal);
		totalCell = RowForTotal.createCell(visitCellNum);
		perSubDirect = Float.valueOf(cost.getAttribute("psd"));
		facAndAdmain = perSubDirect * faFloat;

		bd = new BigDecimal((double) facAndAdmain);
		bd = bd.setScale(scale, roundingMode);
		facAndAdmain = bd.floatValue();

		tempData = df.format(facAndAdmain);

		totalCell.setCellValue("$" + tempData);
		totalCell.setCellStyle(style);
		indexBeforeTotal++;

		RowForTotal = proForColor.get(indexBeforeTotal);
		totalCell = RowForTotal.createCell(visitCellNum);
		perSubDirectTotal = perSubDirect + facAndAdmain;

		bd = new BigDecimal((double) perSubDirectTotal);
		bd = bd.setScale(scale, roundingMode);
		perSubDirectTotal = bd.floatValue();

		tempData = df.format(perSubDirectTotal);

		totalCell.setCellValue("$" + tempData);
		totalCell.setCellStyle(style);
		indexBeforeTotal++;

		RowForTotal = proForColor.get(indexBeforeTotal);
		totalCell = RowForTotal.createCell(visitCellNum);

		// subject number do not needs.00
		df.applyPattern("##0");
		tempData = df.format(subjectNum);

		totalCell.setCellValue(tempData);
		totalCell.setCellStyle(style);
		indexBeforeTotal++;

		RowForTotal = proForColor.get(indexBeforeTotal);
		totalCell = RowForTotal.createCell(visitCellNum);
		finalTotal = perSubDirectTotal * subjectNum;

		bd = new BigDecimal((double) finalTotal);
		bd = bd.setScale(scale, roundingMode);
		finalTotal = bd.floatValue();
		// subject number do not needs.00
		df.applyPattern("##0.00");
		tempData = df.format(finalTotal);

		totalCell.setCellValue("$" + tempData);
		totalCell.setCellStyle(style);
		indexBeforeTotal++;

		return indexBeforeTotal;
	}

	private void addTotalButton(int visitCellNum, Element visit,
			BudgetDocumentType budgetDocumentType, int indexBeforeTotal,
			Map<Integer, Row> proForColor, CellStyle style, String fa) {

		if (budgetDocumentType.equals(BudgetDocumentType.PRICE_ONLY)
				|| budgetDocumentType.equals(BudgetDocumentType.FULL)
				|| budgetDocumentType.equals(BudgetDocumentType.NO_NOTES)
				|| budgetDocumentType.equals(BudgetDocumentType.ALL)) {
			NodeList totalList = visit.getElementsByTagName("total");
			Element price = (Element) totalList.item(0);
			indexBeforeTotal = totalCal(visit, visitCellNum, price,
					proForColor, indexBeforeTotal, fa, style);
		}

	}

	@SuppressWarnings("deprecation")
	private int addCycle(Element arm, Sheet sheet, CreationHelper createHelper,
			BudgetDocumentType budgetDocumentType, Row cycleRow,
			int cycleIndex, Map<Integer, CellStyle> color, HSSFWorkbook wb,
			int placeForNotes) {
		CellStyle styleForTitle = wb.createCellStyle();
		CellStyle styleForNotes = wb.createCellStyle();
		styleForTitle = color.get(10);
		styleForNotes = color.get(12);

		NodeList cycleList = arm.getElementsByTagName("cycle");

		Row rowForCycleNotes = sheet.createRow(3);
		// left bound stytle
		Cell leftBounderCell = rowForCycleNotes.createCell(0);
		leftBounderCell.setCellStyle(color.get(11));

		// sort the cycles by index
		//List<CycleObj> cycleObjs = Lists.newArrayList();
		CycleObj[] cycleObjs;
		cycleObjs= new CycleObj[cycleList.getLength()];
			for (int i = 0; i < cycleList.getLength(); i++) {
				try {Element cycle = (Element) cycleList.item(i);
				CycleObj cycleObj = new CycleObj();
				
				cycleObj.cycleEle = (Element) cycleList.item(i);
				cycleObj.id = Long
						.valueOf(cycle.getAttribute("id"));
				cycleObj.stratDay = Integer
						.valueOf(cycle.getAttribute("startday"));
				cycleObj.name = cycle.getAttribute("name");
				cycleObjs[i]=cycleObj;
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
			cycleObjs = sortCycle(cycleObjs);
			
		int rowHightTotal = 1;
		int rowHightForNotes = 1;
		for (int cycleLen = 0; cycleLen < cycleObjs.length; cycleLen++) {
			CycleObj orderedCycleObj =cycleObjs[cycleLen];
			Element cycle =orderedCycleObj.cycleEle;
			NodeList visitList = cycle.getElementsByTagName("visit");

			for (int MergeCycleIndex = cycleIndex; MergeCycleIndex < cycleIndex
					+ visitList.getLength(); MergeCycleIndex++) {
				Cell MergeCell = cycleRow.createCell(MergeCycleIndex);
				MergeCell.setCellStyle(styleForTitle);
			}

			Cell cycleCell = cycleRow.createCell(cycleIndex);
			if (!cycle.getAttribute("name").isEmpty()) {
				
				String cycleName = cycle.getAttribute("name") + ":" + " "
						+ cycle.getAttribute("duration")
						+ " "+cycle.getAttribute("durationunit") + "s";
				cycleCell.setCellValue(createHelper
						.createRichTextString(cycleName));

				String[] splitCycleName = cycleName.split(" ");
				int wordLength = 0;
				int rowHight = 1;
				int procedureCellWidth = 3000;
				for (int wordNum = 0; wordNum < splitCycleName.length; wordNum++) {
					wordLength = wordLength + splitCycleName[wordNum].length()
							* 256;
					// in case the words is too long and directly start from the
					// next line
					if (splitCycleName[wordNum].length() * 256 > procedureCellWidth
							&& wordNum > 0)
						wordLength += procedureCellWidth
								- splitCycleName[wordNum - 1].length() * 256;
				}
				if (wordLength > procedureCellWidth) {
					rowHight++;
					rowHight += wordLength / procedureCellWidth;
				}
				if (rowHightTotal < rowHight)
					rowHightTotal = rowHight;
				cycleRow.setHeightInPoints((rowHightTotal * sheet
						.getDefaultRowHeightInPoints()));
			}
			cycleCell.setCellStyle(styleForTitle);

			sheet.addMergedRegion(new CellRangeAddress(2, 2, cycleIndex,
					cycleIndex + visitList.getLength() - 1));

			// add notes for cycles

			Cell cycleNotesCell = rowForCycleNotes.createCell(cycleIndex);
			Node cycleNotes = cycle.getElementsByTagName("notes").item(0);
			String notesStr = cycleNotes.getTextContent();

			if (!notesStr.isEmpty()) {
				cycleNotesCell.setCellValue(notesStr);
				String[] splitCycleNotes = notesStr.split(" ");
				int wordLength = 0;
				int rowHight = 1;
				int procedureCellWidth = 3000;
				for (int wordNum = 0; wordNum < splitCycleNotes.length; wordNum++) {
					wordLength = wordLength + splitCycleNotes[wordNum].length()
							* 256;
					// in case the words is too long and directly start from the
					// next line
					if (splitCycleNotes[wordNum].length() * 256 > procedureCellWidth
							&& wordNum > 0)
						wordLength += procedureCellWidth
								- splitCycleNotes[wordNum - 1].length() * 256;
				}
				if (wordLength > procedureCellWidth) {
					rowHight++;
					rowHight += wordLength / procedureCellWidth;
				}
				if (rowHightForNotes < rowHight)
					rowHightForNotes = rowHight;
				rowForCycleNotes.setHeightInPoints((rowHightForNotes * sheet
						.getDefaultRowHeightInPoints()));
			}
			cycleNotesCell.setCellStyle(styleForNotes);
			sheet.addMergedRegion(new CellRangeAddress(3, 3, cycleIndex,
					cycleIndex + visitList.getLength() - 1));

			cycleIndex += visitList.getLength();

		}
		/*
		 * if (budgetDocumentType.equals(BudgetDocumentType.FULL)) cycleIndex +=
		 * 6; else if (budgetDocumentType.equals(BudgetDocumentType.PRICE_ONLY)
		 * || budgetDocumentType.equals(BudgetDocumentType.ALL)) { cycleIndex +=
		 * 6; }
		 */

		// for systel
		int setCellEndPlace = placeForNotes + 1;
		if (budgetDocumentType.equals(BudgetDocumentType.NO_NOTES)) {
			setCellEndPlace = placeForNotes;
		}

		for (int i = cycleIndex; i < setCellEndPlace; i++) {
			Cell tempCell = rowForCycleNotes.createCell(i);
			tempCell.setCellStyle(styleForNotes);
		}
		/*
		 * sheet.addMergedRegion(new CellRangeAddress(3, 3, cycleIndex,
		 * cycleIndex+6));
		 */
		return cycleIndex;
	}

	@SuppressWarnings("deprecation")
	private void addArm(Element epochEle, Element armEle, Sheet sheet,
			CreationHelper createHelper, Row rowForTitle,
			Map<String, Row> proForVp, Map<Integer, Row> proForColor,
			CellStyle style, Map<Integer, CellStyle> color, HSSFWorkbook wb,
			BudgetDocumentType budgetDocumentType, int indexBeforeTotal,
			String fa, Row cycleRow, int placeForNotes) {
		Row armRow = sheet.createRow(1);

		// cells for final boarder foramt
		Cell boarderCell = armRow.createCell(placeForNotes);
		if (budgetDocumentType.equals(BudgetDocumentType.NO_NOTES)) {
			boarderCell.setCellStyle(color.get(11));
		} else {
			boarderCell.setCellStyle(color.get(10));
		}
		Cell boarderCell2 = cycleRow.createCell(placeForNotes);
		if (budgetDocumentType.equals(BudgetDocumentType.NO_NOTES)) {
			boarderCell2.setCellStyle(color.get(11));
		} else {
			boarderCell2.setCellStyle(color.get(10));
		}
		sheet.addMergedRegion(new CellRangeAddress(1, // first row //
				3, // last row (0-based) armIndex, // first column
				placeForNotes, placeForNotes // last
												// column
		));

		int cycleIndex = 2;
		if (budgetDocumentType.equals(BudgetDocumentType.PRICE_ONLY))
			cycleIndex = 3;
		else if (budgetDocumentType.equals(BudgetDocumentType.FULL))
			cycleIndex = 6;
		else if (budgetDocumentType.equals(BudgetDocumentType.NO_NOTES))
			cycleIndex = 6;
		else if (budgetDocumentType.equals(BudgetDocumentType.ALL))
			cycleIndex = 6;

		cycleIndex = addCycle(armEle, sheet, createHelper, budgetDocumentType,
				cycleRow, cycleIndex, color, wb, placeForNotes);

		int armIndex = 2;
		int armIndexForColor = 0;
		int armIndexForColor2 = 0;
		int visitCellNum = 2;
		if (budgetDocumentType.equals(BudgetDocumentType.PRICE_ONLY)) {
			visitCellNum = 3;
			armIndex = 3;

		} else if (budgetDocumentType.equals(BudgetDocumentType.FULL)
				|| budgetDocumentType.equals(BudgetDocumentType.NO_NOTES)
				|| budgetDocumentType.equals(BudgetDocumentType.ALL)) {
			visitCellNum = 6;
			armIndex = 6;
		} /*
		 * else if (budgetDocumentType.equals(BudgetDocumentType.ALL)) {
		 * visitCellNum = 4; armIndex = 4; }
		 */

		// merge cells for boders
		if (budgetDocumentType.equals(BudgetDocumentType.CALENDAR_ONLY)
				|| budgetDocumentType.equals(BudgetDocumentType.PRICE_ONLY)
				|| budgetDocumentType.equals(BudgetDocumentType.ALL)
				|| budgetDocumentType.equals(BudgetDocumentType.FULL)
				|| budgetDocumentType.equals(BudgetDocumentType.NO_NOTES)) {
			sheet.addMergedRegion(new CellRangeAddress(1, // first row //
					2, // last row (0-based) armIndex, // first column
					0, armIndex - 1 // last
									// column
			));
			for (int armMergeNum = 0; armMergeNum < armIndex; armMergeNum++) {
				Cell armCellMerge = armRow.createCell(armMergeNum);
				Cell titleCellMerge2 = cycleRow.createCell(armMergeNum);
				armCellMerge.setCellStyle(color.get(10));
				titleCellMerge2.setCellStyle(color.get(10));
			}

		}

		// NodeList armList = epoch.getElementsByTagName("arm");

		// for (int armLen = 0; armLen < armList.getLength(); armLen++) {
		// logger.debug("arm number:  "+armLen);
		// Element arm = (Element) armList.item(armLen);
		// armIndexForColor = 3 * armLen + 1;
		// armIndexForColor2 = 3 * armLen + 1;
		armIndexForColor = 1;
		armIndexForColor2 = 1;
		NodeList visitList = armEle.getElementsByTagName("visit");
		Cell armCell = armRow.createCell(armIndex);
		armCell.setCellValue(createHelper.createRichTextString(armEle
				.getAttribute("name")));
		CellStyle styleForTitle = wb.createCellStyle();
		styleForTitle = color.get(10);
		armCell.setCellStyle(styleForTitle);

		/*
		 * sheet.addMergedRegion(new CellRangeAddress(0, // first row (0-based)
		 * 0, // last row (0-based) armIndex, // first column (0-based) armIndex
		 * + visitList.getLength() - 1 // last column // (0-based) ));
		 */

		if (budgetDocumentType.equals(BudgetDocumentType.CALENDAR_ONLY)
				|| budgetDocumentType.equals(BudgetDocumentType.PRICE_ONLY)
				|| budgetDocumentType.equals(BudgetDocumentType.ALL)
				|| budgetDocumentType.equals(BudgetDocumentType.FULL)
				|| budgetDocumentType.equals(BudgetDocumentType.NO_NOTES)) {

			if (!budgetDocumentType.equals(BudgetDocumentType.CALENDAR_ONLY)) {
				sheet.addMergedRegion(new CellRangeAddress(2, // first row //
						2, // last row (0-based) armIndex, // first column
						cycleIndex, placeForNotes - 1 // last
														// column
				));
			}
			sheet.addMergedRegion(new CellRangeAddress(1, // first row //
					1, // last row (0-based) armIndex, // first column
					armIndex, placeForNotes - 1 // last
												// column
			));
			int armMergeNum = armIndex + 1;
			int cycleMergeNum = cycleIndex + 1;
			while (armMergeNum < placeForNotes) {
				Cell armCellMerge = armRow.createCell(armMergeNum);
				armCellMerge.setCellStyle(styleForTitle);
				armMergeNum++;
				if (cycleMergeNum < placeForNotes - 1) {
					Cell cycleCellMerge = cycleRow.createCell(cycleMergeNum);
					cycleCellMerge.setCellStyle(styleForTitle);
					cycleMergeNum++;
				}

			}

		}

		// add for judge sub pro
		List<String> subprocedureIdList = new ArrayList<String>();
		NodeList subproIDList = epochEle.getElementsByTagName("subprocedures");
		for (int subproLen = 0; subproLen < subproIDList.getLength(); subproLen++) {
			Element subPro = (Element) subproIDList.item(subproLen);
			NodeList proforsubIDList = subPro.getElementsByTagName("procedure");
			for (int proforsubLen = 0; proforsubLen < proforsubIDList
					.getLength(); proforsubLen++) {
				Element proForSub = (Element) proforsubIDList
						.item(proforsubLen);
				subprocedureIdList.add(proForSub.getAttribute("id"));
			}
		}

		// this MAP is used to calculated the total for C I CMS......etc
		Map<String, Float> totalMapResult = new HashMap<String, Float>();
		visitCellNum = addVisit(armEle, epochEle, sheet, createHelper,
				rowForTitle, proForVp, style, color, proForColor,
				armIndexForColor, visitCellNum, wb, indexBeforeTotal,
				budgetDocumentType, fa, subprocedureIdList, totalMapResult);

		if (budgetDocumentType.equals(BudgetDocumentType.PRICE_ONLY)) {
			armIndex += visitList.getLength() + 6;
		} else if (budgetDocumentType.equals(BudgetDocumentType.FULL)
				|| budgetDocumentType.equals(BudgetDocumentType.NO_NOTES)
				|| budgetDocumentType.equals(BudgetDocumentType.ALL)) {
			armIndex += visitList.getLength() + 6;
		} else
			armIndex += visitList.getLength();

		if (budgetDocumentType.equals(BudgetDocumentType.FULL)
				|| budgetDocumentType.equals(BudgetDocumentType.NO_NOTES)
				|| budgetDocumentType.equals(BudgetDocumentType.PRICE_ONLY)
				|| budgetDocumentType.equals(BudgetDocumentType.ALL)) {
			visitCellNum = addTypeForTotal(visitCellNum, rowForTitle,
					createHelper, budgetDocumentType, armEle, proForVp, style,
					indexBeforeTotal, proForColor, fa, armIndexForColor2,
					color, wb, totalMapResult);
		}

	}

	private String toTitleCase(String input) {

		String splitString[] = input.split(" ");
		String result = "";
		for (int i = 0; i < splitString.length; i++) {
			char[] charforStr = splitString[i].toCharArray();
			for (int j = 0; j < charforStr.length; j++) {
				if (j == 0)
					result += String.valueOf(charforStr[j]).toUpperCase();
				else
					result += String.valueOf(charforStr[j]).toLowerCase();
			}
			result += " ";
		}

		return result;
	}

	@Override
	public ByteArrayOutputStream generateBudgetExcelDocument(String budgetXml,
			BudgetDocumentType budgetDocumentType, long protocolFormId) {

		ByteArrayOutputStream baos = null;
		try {

			Document budget = xmlProcessor.loadXmlStringToDOM(budgetXml);
			baos = new ByteArrayOutputStream();

			// creat workbook
			HSSFWorkbook wb = new HSSFWorkbook();
			CreationHelper createHelper = wb.getCreationHelper();

			ProtocolFormXmlData protocolFormXmlData = protocolFormXmlDataDao
					.getLastProtocolFormXmlDataByProtocolFormIdAndType(
							protocolFormId, ProtocolFormXmlDataType.BUDGET);

			// get the protocol id
			long IRB = getProtocolFormXmlDataDao()
					.findById(protocolFormXmlData.getId()).getProtocolForm()
					.getProtocol().getId();
			Protocol protocol = getProtocolFormXmlDataDao()
					.findById(protocolFormXmlData.getId()).getProtocolForm()
					.getProtocol();

			// add info into work book
			addInfo(budget, wb, createHelper, budgetDocumentType, IRB, protocol);

			wb.write(baos);

			// this part is only kept for test

			/*FileOutputStream fileOut = null;
			String file = "C:\\DOCUME~1\\yuanjiawei\\Desktop\\budgetFull.xls";
			fileOut = new FileOutputStream(file);
			wb.write(fileOut);*/

		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return baos;

	}

	public XmlProcessor getXmlProcessor() {
		return xmlProcessor;
	}

	@Autowired(required = true)
	public void setXmlProcessor(XmlProcessor xmlProcessor) {
		this.xmlProcessor = xmlProcessor;
	}

	public ProtocolFormXmlDataDao getProtocolFormXmlDataDao() {
		return protocolFormXmlDataDao;
	}

	@Autowired(required = true)
	public void setProtocolFormXmlDataDao(
			ProtocolFormXmlDataDao protocolFormXmlDataDao) {
		this.protocolFormXmlDataDao = protocolFormXmlDataDao;
	}

	public HospitalChargeProcedureDao getHospitalChargeProcedureDao() {
		return hospitalChargeProcedureDao;
	}

	@Autowired(required = true)
	public void setHospitalChargeProcedureDao(
			HospitalChargeProcedureDao hospitalChargeProcedureDao) {
		this.hospitalChargeProcedureDao = hospitalChargeProcedureDao;
	}

	public UserDao getUserDao() {
		return userDao;
	}

	@Autowired(required = true)
	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}

}
