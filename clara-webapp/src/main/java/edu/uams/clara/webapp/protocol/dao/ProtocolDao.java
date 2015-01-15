package edu.uams.clara.webapp.protocol.dao;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.bouncycastle.util.encoders.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Element;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import edu.uams.clara.core.dao.AbstractDomainDao;
import edu.uams.clara.core.util.xml.XmlHandler;
import edu.uams.clara.core.util.xml.XmlHandlerFactory;
import edu.uams.clara.webapp.common.domain.usercontext.User;
import edu.uams.clara.webapp.common.domain.usercontext.UserRole;
import edu.uams.clara.webapp.common.domain.usercontext.enums.Permission;
import edu.uams.clara.webapp.common.objectwrapper.PagedList;
import edu.uams.clara.webapp.contract.domain.Contract;
import edu.uams.clara.webapp.fileserver.domain.UploadedFile;
import edu.uams.clara.webapp.fileserver.service.FileGenerateAndSaveService;
import edu.uams.clara.webapp.protocol.domain.Protocol;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.ProtocolStatus;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.enums.ProtocolFormStatusEnum;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.enums.ProtocolStatusEnum;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolForm;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolFormXmlData;
import edu.uams.clara.webapp.protocol.domain.protocolform.enums.ProtocolFormType;
import edu.uams.clara.webapp.protocol.domain.protocolform.enums.ProtocolFormXmlDataType;
import edu.uams.clara.webapp.protocol.objectwrapper.ProtocolReportSearchCritieria;
import edu.uams.clara.webapp.protocol.objectwrapper.ProtocolSearchCriteria;
import edu.uams.clara.webapp.protocol.objectwrapper.ProtocolSearchCriteria.ProtocolSearchField;

@Repository
public class ProtocolDao extends AbstractDomainDao<Protocol> {

	private static final long serialVersionUID = 7492131024720753362L;

	private final static Logger logger = LoggerFactory
			.getLogger(ProtocolDao.class);
	
	private MessageDigest messageDigest = null;
	
	@Value("${fileserver.url}")
	private String fileServer;

	private FileGenerateAndSaveService fileGenerateAndSaveService;
	/**
	 * TODO: index the xml...
	 * 
	 * @param fields
	 * @param query
	 * @return
	 */
	private List<String> subStudyTypeList = Lists.newArrayList();{
		subStudyTypeList.add("local-faculty");
		subStudyTypeList.add("non-local-faculty");
		subStudyTypeList.add("student-fellow-resident-post-doc");
		subStudyTypeList.add("other");
	}
	
	public String exportBookmarkSearchResultFile(String xmlData,User user){
		String fileUrl = "";
		try {
			HSSFWorkbook wb = new HSSFWorkbook();
			CreationHelper createHelper = wb.getCreationHelper();
			Sheet sheet = wb.createSheet("bookmark-search-result");
			Row titleRow = sheet.createRow(0);
			String[] titles = {"IRB#","Title","PI","Status","Study Nature","College","Department"};
			for(int i=0;i<titles.length;i++){
				Cell infoCell = titleRow.createCell(i);
				infoCell.setCellValue(createHelper.createRichTextString(titles[i]));
			}
			
			
			XmlHandler xmlHandler =  XmlHandlerFactory.newXmlHandler();
			List<Element> protocolEles = xmlHandler.listElementsByXPath(xmlData, "/list/protocol");
			
			
			for(int j=0;j<protocolEles.size();j++){
				Row dataRow = sheet.createRow(j+1);
				Element protocolEle = protocolEles.get(j);
				String irb = protocolEle.getAttribute("id");
				Cell infoCell0 = dataRow.createCell(0);
				infoCell0.setCellValue(createHelper.createRichTextString(irb));
				
				String title = xmlHandler.getSingleStringValueByXPath(xmlData, "/list/protocol[@id="+irb+"]/title/text()");
				Cell infoCell1 = dataRow.createCell(1);
				infoCell1.setCellValue(createHelper.createRichTextString(title));
				
				String piName = xmlHandler.getSingleStringValueByXPath(xmlData, "/list/protocol[@id="+irb+"]/staffs/staff/user[roles/role/text()=\"Principal Investigator\"]/lastname/text()")+","+
						xmlHandler.getSingleStringValueByXPath(xmlData, "/list/protocol[@id="+irb+"]/staffs/staff/user[roles/role/text()=\"Principal Investigator\"]/firstname/text()");
				Cell infoCell2 = dataRow.createCell(2);
				if(piName.endsWith(",")){
					piName="";
				}
				infoCell2.setCellValue(createHelper.createRichTextString(piName));
				
				
				String status = xmlHandler.getSingleStringValueByXPath(xmlData, "/list/protocol[@id="+irb+"]/status/text()");
				Cell infoCell3 = dataRow.createCell(3);
				infoCell3.setCellValue(createHelper.createRichTextString(status));
				
				
				String studyNature = xmlHandler.getSingleStringValueByXPath(xmlData, "/list/protocol[@id="+irb+"]/study-nature/text()");
				Cell infoCell4 = dataRow.createCell(4);
				infoCell4.setCellValue(createHelper.createRichTextString(studyNature));
				
				
				String college = xmlHandler.getSingleStringValueByXPath(xmlData, "/list/protocol[@id="+irb+"]/responsible-department/@collegedesc");
				Cell infoCell5 = dataRow.createCell(5);
				infoCell5.setCellValue(createHelper.createRichTextString(college));
				
				
				String department = xmlHandler.getSingleStringValueByXPath(xmlData, "/list/protocol[@id="+irb+"]/responsible-department/@deptdesc");
				Cell infoCell6 = dataRow.createCell(6);
				infoCell6.setCellValue(createHelper.createRichTextString(department));
			}
			
			int tryUploadTime =5;
			
			ByteArrayOutputStream fileData = new ByteArrayOutputStream();;
			wb.write(fileData);
			UploadedFile uploadedFile = null;
			while(uploadedFile == null&&tryUploadTime>0){
				uploadedFile = fileGenerateAndSaveService
						.processFileGenerateAndSave(user, "Bookmark Search Result", new ByteArrayInputStream(fileData.toByteArray()), "xls",
								"bookmark search result");
			tryUploadTime--;
			}
			messageDigest = MessageDigest.getInstance("SHA-256",
					new org.bouncycastle.jce.provider.BouncyCastleProvider());

			messageDigest.update(fileData.toByteArray());
			String hashFileName = new String(Hex.encode(messageDigest.digest()))+".xls";
			fileUrl = fileServer+"/user/"+user.getId()+"/"+hashFileName;
		} catch (Exception e) {
			e.printStackTrace();
		}
		logger.debug(fileUrl);
		return fileUrl;
		
		
	}

	private List<String> protocolSearchCriteriaResolver(
			List<ProtocolSearchCriteria> searchCriteria) {
		List<String> xPathCriteria = new ArrayList<String>();

		for (ProtocolSearchCriteria p : searchCriteria) {
			logger.debug("searchCriteria Field: " + p.getSearchField());
			switch (p.getSearchField()) {
			case IDENTIFIER: {
				logger.debug("it thinks theres an IDENTIFIER..");
				
				try{
					long id = Long.parseLong(p.getKeyword().trim());
					
					xPathCriteria.add("p.id = " + id);
//					
				}catch(Exception e){
					//not a number, who cares...
				}
//				switch (p.getSearchOperator()) {
//				case CONTAINS:
//					xPathCriteria
//							.add("meta_data_xml.exist('/protocol[fn:contains(@identifier,\""
//									+ p.getKeyword().toUpperCase()
//									+ "\")]') = 1");
//					// xPathCriteria.add("meta_data_xml.exist('/protocol[@identifier[fn:contains(fn:upper-case(.), \""
//					// + p.getKeyword().toUpperCase() + "\")]]') = 1");
//					break;
//				case EQUALS:
//					xPathCriteria
//							.add("meta_data_xml.exist('/protocol[@identifier[. = \""
//									+ p.getKeyword() + "\"]]') = 1");
//					break;
//				case DOES_NOT_CONTAIN:
//					xPathCriteria
//							.add("meta_data_xml.exist('/protocol[@identifier[fn:contains(fn:upper-case(.), \""
//									+ p.getKeyword().toUpperCase()
//									+ "\")]]') = 0");
//				default:
//					break;
//				}
			}
				break;
			case TITLE: {
				switch (p.getSearchOperator()) {
				case CONTAINS:
					xPathCriteria
							.add("meta_data_xml.exist('/protocol/title/text()[fn:contains(fn:upper-case(.), \""
									+ p.getKeyword().toUpperCase()
									+ "\")]') = 1");
					break;
				case EQUALS:
					xPathCriteria
							.add("meta_data_xml.exist('/protocol/title[. = \""
									+ p.getKeyword() + "\"]') = 1");
					break;
				case DOES_NOT_CONTAIN:
					xPathCriteria
							.add("meta_data_xml.exist('/protocol/title/text()[fn:contains(fn:upper-case(.), \""
									+ p.getKeyword().toUpperCase()
									+ "\")]') = 0");
				default:
					break;
				}
			}
				break;
			case PROTOCOL_APPROVAL_STATUS: {
				switch (p.getSearchOperator()) {
				case EQUALS:
					xPathCriteria
							.add("meta_data_xml.exist('/protocol/most-recent-study/approval-status/text()[. = \""
									+ p.getKeyword() + "\"]') = 1");
					break;
				default:
					break;
				}
			}
				break;
			case PRIMARY_SITE: {
				switch (p.getSearchOperator()) {
				case CONTAINS:
					xPathCriteria
							.add("meta_data_xml.exist('/protocol/site-responsible/text()[fn:contains(fn:upper-case(.), \""
									+ p.getKeyword().toUpperCase()
									+ "\")]') = 1");
					break;
				case EQUALS:
					xPathCriteria
							.add("meta_data_xml.exist('/protocol/site-responsible/text()[. = \""
									+ p.getKeyword() + "\"]') = 1");
					break;
				case DOES_NOT_CONTAIN:
					xPathCriteria
							.add("meta_data_xml.exist('/protocol/site-responsible/text()[fn:contains(fn:upper-case(.), \""
									+ p.getKeyword().toUpperCase()
									+ "\")]') = 0");
				default:
					break;
				}
			}
				break;
			case DRUG_NAME: {
				switch (p.getSearchOperator()) {
				case CONTAINS:
					xPathCriteria
							.add("meta_data_xml.exist('/protocol/drugs/drug[@name [contains(fn:upper-case(.), \""
									+ p.getKeyword().toUpperCase()
									+ "\")]]') = 1");
					break;
				case EQUALS:
					xPathCriteria
							.add("meta_data_xml.exist('/protocol/drugs/drug[@name = \""
									+ p.getKeyword() + "\"]') = 1");
					break;
				case DOES_NOT_CONTAIN:
					xPathCriteria
							.add("meta_data_xml.exist('/protocol/drugs/drug[@name [contains(fn:upper-case(.), \""
									+ p.getKeyword().toUpperCase()
									+ "\")]]') = 0");
				default:
					break;
				}
			}
				break;
			case LOCATION: {
				switch (p.getSearchOperator()) {
				case CONTAINS:
					xPathCriteria
							.add("meta_data_xml.exist('/protocol/study-sites/site/site-name/text()[fn:contains(fn:upper-case(.), \""
									+ p.getKeyword().toUpperCase()
									+ "\")]') = 1");
					break;
				case EQUALS:
					xPathCriteria
							.add("meta_data_xml.exist('/protocol/study-sites/site/site-name/text()[. = \""
									+ p.getKeyword() + "\"]') = 1");
					break;
				case DOES_NOT_CONTAIN:
					xPathCriteria
							.add("meta_data_xml.exist('/protocol/study-sites/site/site-name/text()[fn:contains(fn:upper-case(.), \""
									+ p.getKeyword().toUpperCase()
									+ "\")]') = 0");
				default:
					break;
				}
			}
				break;
			case FUNDING_SOURCE: {
				switch (p.getSearchOperator()) {
				case CONTAINS:
					xPathCriteria
							.add("meta_data_xml.exist('/protocol/funding/funding-source[@entityname[contains(fn:upper-case(.), \""
									+ p.getKeyword().toUpperCase()
									+ "\")]]') = 1");
					break;
				case EQUALS:
					xPathCriteria
					.add("meta_data_xml.exist('/protocol/funding/funding-source[@entityname[fn:upper-case(.) = \""
							+ p.getKeyword().toUpperCase()
							+ "\"]]') = 1");
					break;
				case DOES_NOT_CONTAIN:
					xPathCriteria
					.add("meta_data_xml.exist('/protocol/funding/funding-source[@entityname[contains(fn:upper-case(.), \""
							+ p.getKeyword().toUpperCase()
							+ "\")]]') = 0");
				default:
					break;
				}
			}
				break;
			case PI_USERID: {
				switch (p.getSearchOperator()) {
				case CONTAINS:
					xPathCriteria
							.add("meta_data_xml.exist('/protocol/staffs/staff/user[roles/role=\"Principal Investigator\" and @id = \""
									+ p.getKeyword() + "\"]') = 1");
					break;
				case EQUALS:
					xPathCriteria
							.add("meta_data_xml.exist('/protocol/staffs/staff/user[roles/role=\"Principal Investigator\" and @id = \""
									+ p.getKeyword() + "\"]') = 1");
					break;
				case DOES_NOT_CONTAIN:
					xPathCriteria
							.add("meta_data_xml.exist('/protocol/staffs/staff/user[roles/role=\"Principal Investigator\" and @id = \""
									+ p.getKeyword() + "\"]') = 0");
				default:
					break;
				}
			}
				break;
			case PROTOCOL_STATUS: {
				switch (p.getSearchOperator()) {
				case CONTAINS:
					xPathCriteria
							.add("p.id IN (SELECT ps.protocol_id FROM protocol_status ps WHERE id IN (SELECT MAX(pst.id) FROM protocol_status pst WHERE pst.retired = :retired GROUP BY pst.protocol_id) AND ps.protocol_status = \'"
									+ p.getKeyword() + "\')");
					break;
				case EQUALS:
					xPathCriteria
					.add("p.id IN (SELECT ps.protocol_id FROM protocol_status ps WHERE id IN (SELECT MAX(pst.id) FROM protocol_status pst WHERE pst.retired = :retired GROUP BY pst.protocol_id) AND ps.protocol_status = \'"
							+ p.getKeyword() + "\')");
					break;
				case DOES_NOT_CONTAIN:
					xPathCriteria
					.add("p.id IN (SELECT ps.protocol_id FROM protocol_status ps WHERE id IN (SELECT MAX(pst.id) FROM protocol_status pst WHERE pst.retired = :retired GROUP BY pst.protocol_id) AND ps.protocol_status = \'"
							+ p.getKeyword() + "\')");
				default:
					break;
				}
			}
				break;
			case PROTOCOL_FORM_STATUS: {
				switch (p.getSearchOperator()) {
				case CONTAINS:
					xPathCriteria
							.add("p.id IN (SELECT DISTINCT protocol_id FROM protocol_form WHERE retired = :retired AND id IN (SELECT DISTINCT protocol_form_id FROM protocol_form_status WHERE id IN ("
									+ " SELECT MAX(id) FROM protocol_form_status WHERE protocol_form_id IN ("
									+ " SELECT MAX(id) FROM protocol_form WHERE retired = :retired GROUP BY parent_id)"
									+ " AND retired = :retired"
									+ " GROUP BY protocol_form_id) AND protocol_form_status = \'"+ p.getKeyword() + "\' AND retired = :retired))");
					break;
				case EQUALS:
					xPathCriteria
					.add("p.id IN (SELECT DISTINCT protocol_id FROM protocol_form WHERE retired = :retired AND id IN (SELECT DISTINCT protocol_form_id FROM protocol_form_status WHERE id IN ("
							+ " SELECT MAX(id) FROM protocol_form_status WHERE protocol_form_id IN ("
							+ " SELECT MAX(id) FROM protocol_form WHERE retired = :retired GROUP BY parent_id)"
							+ " AND retired = :retired"
							+ " GROUP BY protocol_form_id) AND protocol_form_status = \'"+ p.getKeyword() + "\' AND retired = :retired))");
					break;
				case DOES_NOT_CONTAIN:
					xPathCriteria
					.add("p.id NOT IN (SELECT DISTINCT protocol_id FROM protocol_form WHERE retired = :retired AND id IN (SELECT DISTINCT protocol_form_id FROM protocol_form_status WHERE id IN ("
							+ " SELECT MAX(id) FROM protocol_form_status WHERE protocol_form_id IN ("
							+ " SELECT MAX(id) FROM protocol_form WHERE retired = :retired GROUP BY parent_id)"
							+ " AND retired = :retired"
							+ " GROUP BY protocol_form_id) AND protocol_form_status = \'"+ p.getKeyword() + "\' AND retired = :retired))");
				default:
					break;
				}
			}
				break;
			case FORMER_STAFF_USERID: {
				switch (p.getSearchOperator()) {
				case CONTAINS:
					xPathCriteria
							.add("meta_data_xml.exist('/protocol/staffs/staff/user[@id = \""
									+ p.getKeyword()
									+ "\" and roles/role/text()[fn:contains(fn:upper-case(.),\"FORMER\")]]') = 1");
					break;
				case EQUALS:
					xPathCriteria
							.add("meta_data_xml.exist('/protocol/staffs/staff/user[@id = \""
									+ p.getKeyword()
									+ "\" and roles/role/text()[fn:contains(fn:upper-case(.),\"FORMER\")]]') = 1");
					break;
				case DOES_NOT_CONTAIN:
					xPathCriteria
							.add("meta_data_xml.exist('/protocol/staffs/staff/user[@id = \""
									+ p.getKeyword()
									+ "\" and roles/role/text()[fn:contains(fn:upper-case(.),\"FORMER\")]]') = 0");
				default:
					break;
				}
			}
				break;
			case STAFF_USERID: {
				switch (p.getSearchOperator()) {
				case CONTAINS:
					xPathCriteria
							.add("meta_data_xml.exist('/protocol/staffs/staff/user[@id = \""
									+ p.getKeyword()
									+ "\" and not(roles/role/text()[fn:contains(fn:upper-case(.),\"FORMER\")])]') = 1");
					break;
				case EQUALS:
					xPathCriteria
							.add("meta_data_xml.exist('/protocol/staffs/staff/user[@id = \""
									+ p.getKeyword()
									+ "\" and not(roles/role/text()[fn:contains(fn:upper-case(.),\"FORMER\")])]') = 1");
					break;
				case DOES_NOT_CONTAIN:
					xPathCriteria
							.add("meta_data_xml.exist('/protocol/staffs/staff/user[@id = \""
									+ p.getKeyword()
									+ "\" and not(roles/role/text()[fn:contains(fn:upper-case(.),\"FORMER\")])]') = 0");
				default:
					break;
				}
			}
				break;
			case PHARMACY_FEE_WAIVED: {
				switch (p.getSearchOperator()) {
				case EQUALS:
					xPathCriteria
							.add("meta_data_xml.exist('/protocol/summary/pharmacy-determination/pharmacy-fee-waived[text()=\""+ p.getKeyword() +"\"]') = 1");
					break;
				default:
					break;
				}
			}
				break;
			case STUDY_TYPE: {
				switch (p.getSearchOperator()) {
				case CONTAINS:
					xPathCriteria
							.add("meta_data_xml.exist('/protocol/study-type/text()[fn:contains(fn:upper-case(.), \""
									+ p.getKeyword().toUpperCase()
									+ "\")]') = 1");
					break;
				case EQUALS:{
					if(subStudyTypeList.contains(p.getKeyword())){
						xPathCriteria
						.add("meta_data_xml.exist('/protocol/study-type[text() = \"investigator-initiated\"]/investigator-initiated/investigator-description/text()[. = \""+ p.getKeyword() + "\"]')=1");
					}else{
						xPathCriteria
						.add("meta_data_xml.exist('/protocol/study-type/text()[. = \""
								+ p.getKeyword() + "\"]') = 1");
					}
					
					break;
					}
				case DOES_NOT_CONTAIN:
					xPathCriteria
							.add("meta_data_xml.exist('/protocol/study-type/text()[fn:contains(fn:upper-case(.), \""
									+ p.getKeyword().toUpperCase()
									+ "\")]') = 0");
				default:
					break;
				}
			}
				break;
			case COLLEGE: {
				String combSearchStr = "";
				
				if (!p.getKeyword().contains(",")) {
					combSearchStr = "@collegeid = \""+ p.getKeyword() + "\"";
				} else {
					List<String> combList = Arrays.asList(p.getKeyword().split(","));
					
					combSearchStr = "@collegeid = \""+ combList.get(0) + "\" and @deptid = \""+ combList.get(1) + "\"";
					
					if (combList.size() == 3) {
						combSearchStr = combSearchStr + " and @subdeptid = \""+ combList.get(2) + "\"";
					}
				}
				
				switch (p.getSearchOperator()) {
				case CONTAINS:
					xPathCriteria
							.add("meta_data_xml.exist('/protocol/responsible-department["+ combSearchStr +"]') = 1");
					break;
				case EQUALS:
					xPathCriteria
					.add("meta_data_xml.exist('/protocol/responsible-department["+ combSearchStr +"]') = 1");
					break;
				case DOES_NOT_CONTAIN:
					xPathCriteria
					.add("meta_data_xml.exist('/protocol/responsible-department["+ combSearchStr +"]') = 0");
				default:
					break;
				}
			}
				break;
				/* combined with one search rule
			case DEPARTMENT: {
				switch (p.getSearchOperator()) {
				case CONTAINS:
					xPathCriteria
							.add("meta_data_xml.exist('/protocol/responsible-department[@deptdesc[fn:contains(fn:upper-case(.), \""
									+ p.getKeyword().toUpperCase()
									+ "\")]]') = 1");
					break;
				case EQUALS:
					xPathCriteria
							.add("meta_data_xml.exist('/protocol/responsible-department[@deptdesc = \""
									+ p.getKeyword() + "\"]') = 1");
					break;
				case DOES_NOT_CONTAIN:
					xPathCriteria
							.add("meta_data_xml.exist('/protocol/responsible-department[@deptdesc[fn:contains(fn:upper-case(.), \""
									+ p.getKeyword().toUpperCase()
									+ "\")]]') = 0");
				default:
					break;
				}
			}
				break;
			case DIVISION: {
				switch (p.getSearchOperator()) {
				case CONTAINS:
					xPathCriteria
							.add("meta_data_xml.exist('/protocol/responsible-department[@subdeptdesc[fn:contains(fn:upper-case(.), \""
									+ p.getKeyword().toUpperCase()
									+ "\")]]') = 1");
					break;
				case EQUALS:
					xPathCriteria
							.add("meta_data_xml.exist('/protocol/responsible-department[@subdeptdesc = \""
									+ p.getKeyword() + "\"]') = 1");
					break;
				case DOES_NOT_CONTAIN:
					xPathCriteria
							.add("meta_data_xml.exist('/protocol/responsible-department[@subdeptdesc[fn:contains(fn:upper-case(.), \""
									+ p.getKeyword().toUpperCase()
									+ "\")]]') = 0");
				default:
					break;
				}
			}
				break;
				*/
			case FORM_TYPE: {
				switch (p.getSearchOperator()) {
				case CONTAINS:
					xPathCriteria
							.add("p.id IN (SELECT DISTINCT pf.protocol_id FROM protocol_form pf WHERE pf.retired=:retired AND pf.protocol_form_type LIKE \'%"
									+ p.getKeyword() + "%\')");
					break;
				case EQUALS:
					xPathCriteria
							.add("p.id IN (SELECT DISTINCT pf.protocol_id FROM protocol_form pf WHERE pf.retired=:retired AND pf.protocol_form_type = \'"
									+ p.getKeyword() + "\')");
					break;
				case DOES_NOT_CONTAIN:
					xPathCriteria
							.add("p.id IN (SELECT DISTINCT pf.protocol_id FROM protocol_form pf WHERE pf.retired=:retired AND pf.protocol_form_type NOT LIKE \'%"
									+ p.getKeyword() + "%\')");
				default:
					break;
				}
			}
				break;
			case MY_PROTOCOLS: {
				xPathCriteria.add("1 = 1");
			}
				break;
			case PENDING_PI_ACTION: {
				switch (p.getSearchOperator()) {
				case IS:
					/*
					xPathCriteria
							.add("p.id IN (SELECT DISTINCT pform.protocol_id FROM protocol_form pform WHERE pform.id in (SELECT MAX(pf.id) FROM protocol_form pf where retired =0 group by pf.parent_id)"
									+ " and pform.id in (SELECT pfstatus.protocol_form_id FROM protocol_form_status pfstatus WHERE pfstatus.id  IN (SELECT MAX(pfs.id) FROM protocol_form pf, protocol_form_status pfs"
									+ " WHERE pf.retired = :retired AND pfs.retired = :retired AND pf.id = pfs.protocol_form_id GROUP BY pfs.protocol_form_id)"
									+ " AND pfstatus.protocol_form_status IN ('PENDING_PI_ENDORSEMENT','REVISION_PENDING_PI_ENDORSEMENT','PENDING_PI_SIGN_OFF','IRB_DEFERRED_WITH_MAJOR_CONTINGENCIES','IRB_DEFERRED_WITH_MINOR_CONTINGENCIES',"
									+ "'REVISION_WITH_MAJOR_PENDING_PI_ENDORSEMENT','REVISION_WITH_MINOR_PENDING_PI_ENDORSEMENT','PENDING_TP_ENDORSEMENT')))");
									*/
					xPathCriteria
					.add("meta_data_xml.exist('/protocol/form-pending-pi-action/text()[. = \"y\"]') = 1");
					break;
				default:
					break;
				}
			}
				break;
			case ASSIGNED_REVIEWER_USERID: {
				switch (p.getSearchOperator()) {
				case CONTAINS:
					xPathCriteria
							.add("meta_data_xml.exist('//assigned-reviewers/assigned-reviewer[@user-id = \""
									+ p.getKeyword() + "\"]') = 1");
					break;
				case EQUALS:
					xPathCriteria
							.add("meta_data_xml.exist('//assigned-reviewers/assigned-reviewer[@user-id = \""
									+ p.getKeyword() + "\"]') = 1");
					break;
				case DOES_NOT_CONTAIN:
					xPathCriteria
							.add("meta_data_xml.exist('//assigned-reviewers/assigned-reviewer[@user-id = \""
									+ p.getKeyword() + "\"]') = 0");
				default:
					break;
				}
			}
				break;
			default:
				break;
			}
		}

		/*
		 * for (ProtocolSearchField field : fields) { switch (field) { case
		 * TITLE:
		 * xPathCriteria.add("meta_data.exist('/protocol/title[fn:contains(., \""
		 * + keyword + "\")]') = 1"); break; case PI_NAME: xPathCriteria.add(
		 * "meta_data.exist('/protocol/staffs/staff/user/roles/role[.=\"Principal Investigator\" and (../../user/lastname[fn:contains(fn:upper-case(.), \""
		 * + keyword.toUpperCase() +
		 * "\")] or ../../user/firstname[fn:contains(fn:upper-case(.), \"" +
		 * keyword.toUpperCase() + "\")])]') = 1"); break; case PROTOCOL_STATUS:
		 * break; case STAFF_NAME: xPathCriteria.add(
		 * "meta_data.exist('/protocol/staffs/staff/user/lastname[fn:contains(fn:upper-case(.), \""
		 * + keyword.toUpperCase() + "\")]') = 1"); xPathCriteria.add(
		 * "meta_data.exist('/protocol/staffs/staff/user/firstname[fn:contains(fn:upper-case(.), \""
		 * + keyword.toUpperCase() + "\")]') = 1"); break; default: break; } }
		 */

		return xPathCriteria;
	}

	@Transactional(readOnly = true)
	private List<String> protocolReportSearchCriteriaResolver(
			ProtocolReportSearchCritieria reportSearchCriteria) {
		List<String> xPathCriteria = new ArrayList<String>();

		switch (reportSearchCriteria.getProtocolReportSearchField()) {
		case PI:
			xPathCriteria
					.add("meta_data_xml.exist('/protocol/staffs/staff/user[@id=\""
							+ reportSearchCriteria.getKeyword()
							+ "\"][roles/role=\"Principal Investigator\"]') = 1");
			break;
		default:
			break;
		}

		if (!reportSearchCriteria.getStudyType().isEmpty()) {
			xPathCriteria
					.add("meta_data_xml.exist('/protocol/study-type[. = \""
							+ reportSearchCriteria.getStudyType() + "\"]') = 1");
		}

		return xPathCriteria;
	}


	@Transactional(readOnly = true)
	public List<ProtocolForm> listProtocolFormByReportSearchCriteriaAndStartAndEndDate(
			ProtocolReportSearchCritieria reportSearchCriteria,
			ProtocolFormStatusEnum protocolFormStatus) {
		String xpathWhereClause = "";

		if (reportSearchCriteria != null) {
			List<String> xPathCriterias = protocolReportSearchCriteriaResolver(reportSearchCriteria);

			int c = xPathCriterias.size();

			int i = 0;

			for (String xc : xPathCriterias) {
				xpathWhereClause += xc;

				if (i != c - 1) {
					xpathWhereClause += " AND ";
				}

				i++;
			}
		}

		String query = "SELECT pf FROM ProtocolForm pf, ProtocolFormStatus pfs WHERE pf.retired = :retired "
				+ " AND pfs.retired = :retired AND pf.id = pfs.protocolForm.id "
				+ " AND pf.protocolFormType = :protocolFormType"
				+ " AND pfs.protocolFormStatus = :protocolFormStatus"
				+ " AND pfs.modified >= :startDate AND pfs.modified <= :endDate"
				+ ((reportSearchCriteria != null) ? " AND  ("
						+ xpathWhereClause + ") " : "");

		TypedQuery<ProtocolForm> q = getEntityManager().createQuery(query,
				ProtocolForm.class);
		q.setHint("org.hibernate.cacheable", true);
		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("protocolFormStatus", protocolFormStatus);
		q.setParameter("startDate", reportSearchCriteria.getStartDate());
		q.setParameter("endDate", reportSearchCriteria.getEndDate());
		q.setParameter("protocolFormType", ProtocolFormType.NEW_SUBMISSION);

		return q.getResultList();
	}

	@Transactional(readOnly = true)
	public List<ProtocolForm> listLatestProtocolFormsByProtocolId(
			long protocolId) {
		// String query =
		// "SELECT pf FROM ProtocolForm pf WHERE pf.retired = :retired AND pf.id IN ("
		// + " SELECT MAX(ppf.id) FROM ProtocolForm ppf "
		// + " WHERE ppf.retired = :retired AND ppf.protocol.id = :protocolId "
		// + " GROUP BY ppf.parent.id ) ";
		
		/*
		String query = "SELECT pf FROM ProtocolForm pf WHERE pf.retired = :retired AND pf.protocol.id = :protocolId"
				+ " AND pf.id in (SELECT MAX(p.id) FROM ProtocolForm p where p.protocol.id = :protocolId AND p.retired = :retired GROUP BY p.parent.id, p.protocolFormType)";
		*/
		String query = "SELECT pf FROM ProtocolForm pf WHERE pf.retired = :retired "
				+ " AND pf.id in (SELECT MAX(p.id) FROM ProtocolForm p where p.protocol.id = :protocolId AND p.retired = :retired GROUP BY p.parent.id, p.protocolFormType)";

		TypedQuery<ProtocolForm> q = getEntityManager().createQuery(query,
				ProtocolForm.class);
		q.setHint("org.hibernate.cacheable", true);
		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("protocolId", protocolId);

		return q.getResultList();
	}

	@Transactional(readOnly = true)
	public List<Protocol> listExpiredProtocolByMonths(int number) {
		// String query =
		// "SELECT pf FROM ProtocolForm pf WHERE pf.retired = :retired AND pf.id IN ("
		// + " SELECT MAX(ppf.id) FROM ProtocolForm ppf "
		// + " WHERE ppf.retired = :retired AND ppf.protocol.id = :protocolId "
		// + " GROUP BY ppf.parent.id ) ";

		String query = "SELECT * FROM protocol WHERE retired = :retired"
				+ " AND meta_data_xml.exist('/protocol/most-recent-study/approval-end-date') = 1"
				+ " AND meta_data_xml.value('(/protocol/most-recent-study/approval-end-date/text())[1]','varchar(10)') = CONVERT(varchar(10), DATEADD(M,:number,GETDATE()), 101)";

		TypedQuery<Protocol> q = (TypedQuery<Protocol>) getEntityManager()
				.createNativeQuery(query, Protocol.class);
		q.setHint("org.hibernate.cacheable", true);
		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("number", number);

		return q.getResultList();
	}
	
	@Transactional(readOnly = true)
	public List<Protocol> listExpiredProtocol() {
		// String query =
		// "SELECT pf FROM ProtocolForm pf WHERE pf.retired = :retired AND pf.id IN ("
		// + " SELECT MAX(ppf.id) FROM ProtocolForm ppf "
		// + " WHERE ppf.retired = :retired AND ppf.protocol.id = :protocolId "
		// + " GROUP BY ppf.parent.id ) ";

		String query = "SELECT * FROM protocol"
				+ " WHERE retired = :retired"
				+ " AND id IN"
				+ " (SELECT DISTINCT protocol_id FROM protocol_status WHERE id IN ("
				+ " SELECT MAX(id) FROM protocol_status WHERE retired = :retired GROUP BY protocol_id)"
				+ " AND protocol_status = 'OPEN')"
				+ " AND meta_data_xml.exist('/protocol/most-recent-study/approval-end-date') = 1"
				+ " AND meta_data_xml.value('(/protocol/most-recent-study/approval-end-date/text())[1]','Date') < GETDATE()";

		TypedQuery<Protocol> q = (TypedQuery<Protocol>) getEntityManager()
				.createNativeQuery(query, Protocol.class);
		q.setHint("org.hibernate.cacheable", true);
		q.setParameter("retired", Boolean.FALSE);

		return q.getResultList();
	}
	
	@Transactional(readOnly = true)
	public List<Protocol> listExpiredForSixteenDaysProtocol() {
		// String query =
		// "SELECT pf FROM ProtocolForm pf WHERE pf.retired = :retired AND pf.id IN ("
		// + " SELECT MAX(ppf.id) FROM ProtocolForm ppf "
		// + " WHERE ppf.retired = :retired AND ppf.protocol.id = :protocolId "
		// + " GROUP BY ppf.parent.id ) ";

		String query = "SELECT * from protocol WHERE id IN ("
				+ " SELECT DISTINCT protocol_id FROM protocol_status WHERE id IN("
				+ " SELECT MAX(id) FROM protocol_status WHERE retired = :retired"
				+ " GROUP BY protocol_id)"
				+ " AND protocol_status = 'EXPIRED'"
				+ " AND DATEDIFF(DAY, modified, GETDATE()) = 16)"
				+ " AND retired = :retired";

		TypedQuery<Protocol> q = (TypedQuery<Protocol>) getEntityManager()
				.createNativeQuery(query, Protocol.class);
		q.setHint("org.hibernate.cacheable", true);
		q.setParameter("retired", Boolean.FALSE);

		return q.getResultList();
	}
	
	@Transactional(readOnly = true)
	public List<Protocol> listExpiredMoreThanOneMonthProtocol() {
		// String query =
		// "SELECT pf FROM ProtocolForm pf WHERE pf.retired = :retired AND pf.id IN ("
		// + " SELECT MAX(ppf.id) FROM ProtocolForm ppf "
		// + " WHERE ppf.retired = :retired AND ppf.protocol.id = :protocolId "
		// + " GROUP BY ppf.parent.id ) ";

		String query = "SELECT * from protocol WHERE id IN ("
				+ " SELECT DISTINCT protocol_id FROM protocol_status WHERE id IN("
				+ " SELECT MAX(id) FROM protocol_status WHERE retired = :retired"
				+ " GROUP BY protocol_id)"
				+ " AND protocol_status = 'EXPIRED'"
				//+ " AND modified <= DATEADD(D,-30,GETDATE()))"
				+ " AND modified <= DATEADD(M,-1,GETDATE()))"
				+ " AND retired = :retired";

		TypedQuery<Protocol> q = (TypedQuery<Protocol>) getEntityManager()
				.createNativeQuery(query, Protocol.class);
		q.setHint("org.hibernate.cacheable", true);
		q.setParameter("retired", Boolean.FALSE);

		return q.getResultList();
	}
	
	@Transactional(readOnly = true)
	public List<Protocol> listOpenProtocol() {
		// String query =
		// "SELECT pf FROM ProtocolForm pf WHERE pf.retired = :retired AND pf.id IN ("
		// + " SELECT MAX(ppf.id) FROM ProtocolForm ppf "
		// + " WHERE ppf.retired = :retired AND ppf.protocol.id = :protocolId "
		// + " GROUP BY ppf.parent.id ) ";

		String query = "SELECT * FROM protocol"
				+ " WHERE retired = :retired"
				+ " AND id IN"
				+ " (SELECT DISTINCT protocol_id FROM protocol_status WHERE id IN ("
				+ " SELECT MAX(id) FROM protocol_status WHERE retired = :retired GROUP BY protocol_id)"
				+ " AND protocol_status = 'OPEN')";

		TypedQuery<Protocol> q = (TypedQuery<Protocol>) getEntityManager()
				.createNativeQuery(query, Protocol.class);
		q.setHint("org.hibernate.cacheable", true);
		q.setParameter("retired", Boolean.FALSE);

		return q.getResultList();
	}
	
	@Transactional(readOnly = true)
	public List<Protocol> listClosedProtocol() {
		// String query =
		// "SELECT pf FROM ProtocolForm pf WHERE pf.retired = :retired AND pf.id IN ("
		// + " SELECT MAX(ppf.id) FROM ProtocolForm ppf "
		// + " WHERE ppf.retired = :retired AND ppf.protocol.id = :protocolId "
		// + " GROUP BY ppf.parent.id ) ";

		String query = "SELECT * FROM protocol"
				+ " WHERE retired = :retired"
				+ " AND id IN"
				+ " (SELECT DISTINCT protocol_id FROM protocol_status WHERE id IN ("
				+ " SELECT MAX(id) FROM protocol_status WHERE retired = :retired GROUP BY protocol_id)"
				+ " AND protocol_status = 'CLOSED')";

		TypedQuery<Protocol> q = (TypedQuery<Protocol>) getEntityManager()
				.createNativeQuery(query, Protocol.class);
		q.setHint("org.hibernate.cacheable", true);
		q.setParameter("retired", Boolean.FALSE);

		return q.getResultList();
	}
	
	@Transactional(readOnly = true)
	public List<Protocol> listNotClosedProtocol() {
		// String query =
		// "SELECT pf FROM ProtocolForm pf WHERE pf.retired = :retired AND pf.id IN ("
		// + " SELECT MAX(ppf.id) FROM ProtocolForm ppf "
		// + " WHERE ppf.retired = :retired AND ppf.protocol.id = :protocolId "
		// + " GROUP BY ppf.parent.id ) ";

		String query = "SELECT * FROM protocol"
				+ " WHERE retired = :retired"
				+ " AND id IN"
				+ " (SELECT DISTINCT protocol_id FROM protocol_status WHERE id IN ("
				+ " SELECT MAX(id) FROM protocol_status WHERE retired = :retired GROUP BY protocol_id)"
				+ " AND protocol_status NOT IN ('CLOSED', 'CANCELLED'))";

		TypedQuery<Protocol> q = (TypedQuery<Protocol>) getEntityManager()
				.createNativeQuery(query, Protocol.class);
		q.setHint("org.hibernate.cacheable", true);
		q.setParameter("retired", Boolean.FALSE);

		return q.getResultList();
	}
	
	@Transactional(readOnly = true)
	public List<Protocol> listOpenNotPushedToEpicProtocol() {
		// String query =
		// "SELECT pf FROM ProtocolForm pf WHERE pf.retired = :retired AND pf.id IN ("
		// + " SELECT MAX(ppf.id) FROM ProtocolForm ppf "
		// + " WHERE ppf.retired = :retired AND ppf.protocol.id = :protocolId "
		// + " GROUP BY ppf.parent.id ) ";

		String query = "SELECT * FROM protocol"
				+ " WHERE retired = :retired"
				+ " AND id IN"
				+ " (SELECT DISTINCT protocol_id FROM protocol_status WHERE id IN ("
				+ " SELECT MAX(id) FROM protocol_status WHERE retired = :retired GROUP BY protocol_id)"
				+ " AND protocol_status = 'OPEN') AND meta_data_xml.exist('/protocol/pushed-to-epic')=0";

		TypedQuery<Protocol> q = (TypedQuery<Protocol>) getEntityManager()
				.createNativeQuery(query, Protocol.class);
		q.setHint("org.hibernate.cacheable", true);
		q.setParameter("retired", Boolean.FALSE);

		return q.getResultList();
	}

	@Transactional(readOnly = true)
	public ProtocolForm getLatestProtocolFormByProtocolIdAndProtocolFormType(
			long protocolId, ProtocolFormType protocolFormType) {
		String query = "SELECT pf FROM ProtocolForm pf WHERE pf.retired = :retired AND pf.protocolFormType = :protocolFormType AND pf.id IN ("
				+ " SELECT MAX(ppf.id) FROM ProtocolForm ppf "
				+ " WHERE ppf.retired = :retired AND ppf.protocol.id = :protocolId "
				+ " GROUP BY ppf.parent.id ) ";

		TypedQuery<ProtocolForm> q = getEntityManager().createQuery(query,
				ProtocolForm.class);
		q.setFirstResult(0);
		q.setMaxResults(1);
		q.setHint("org.hibernate.cacheable", true);
		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("protocolId", protocolId);
		q.setParameter("protocolFormType", protocolFormType);

		return q.getSingleResult();
	}

	@Transactional(readOnly = true)
	public List<Protocol> listProtocolsByUser(User user) {
		String query = "SELECT p FROM Protocol p, SecurableObjectAcl soa "
				+ " WHERE p.retired = :retired AND soa.retired = :retired AND soa.ownerClass = :ownerClass AND soa.ownerId = :ownerId "
				+ " AND soa.securableObject.objectClass = :objectClass AND soa.securableObject.objectId = p.id ";

		TypedQuery<Protocol> q = getEntityManager().createQuery(query,
				Protocol.class);

		q.setHint("org.hibernate.cacheable", true);
		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("ownerClass", User.class);
		q.setParameter("ownerId", user.getId());
		q.setParameter("objectClass", Protocol.class);

		return q.getResultList();
	}
	
	

	@Transactional(readOnly = true)
	public PagedList<Protocol> listPagedProtocolMetaDatasByUserAndSearchCriteriaAndProtocolStatusFilter(
			User user, int start, int limit,
			List<ProtocolSearchCriteria> searchCriterias, boolean quickSearch) {

		PagedList<Protocol> pagedList = new PagedList<Protocol>();
		pagedList.setStart(0);
		pagedList.setLimit(limit);

		String xpathWhereClause = "";

		boolean listMyProotcolOnly = false;
		
		
		if (searchCriterias != null && !searchCriterias.isEmpty()) {
			for (ProtocolSearchCriteria psct : searchCriterias) {
				if (psct.getSearchField().equals(
						ProtocolSearchField.MY_PROTOCOLS)) {
					listMyProotcolOnly = true;
				}

				if (psct.getSearchField().equals(
						ProtocolSearchField.PENDING_PI_ACTION)) {
					listMyProotcolOnly = true;
				}
			}

			List<String> xPathCriterias = protocolSearchCriteriaResolver(searchCriterias);

			Joiner joiner = Joiner.on(" AND ").skipNulls();
			if (quickSearch) { // quick search is the only one has or
								// condition... probably need to change this...
				joiner = Joiner.on(" OR ").skipNulls();
			}

			xpathWhereClause += joiner.join(xPathCriterias);	

		}

		logger.debug("xpathWhereClause: " + xpathWhereClause);

		boolean viewAllStudy = false;

		if (user.getAuthorities().contains(Permission.VIEW_ALL_STUDY)) {
			viewAllStudy = true;
		}

		String queryTotal = "";
		String query = "";

		if (viewAllStudy && !listMyProotcolOnly) {
			queryTotal = " SELECT COUNT(*) FROM protocol p "
					+ " WHERE p.retired = :retired "
					+ ((searchCriterias != null) ? " AND  (" + xpathWhereClause
							+ ") " : "");

			query = " SELECT p.* FROM protocol p "
					+ " WHERE p.retired = :retired "
					+ ((searchCriterias != null) ? " AND  (" + xpathWhereClause
							+ ") " : "") + " ORDER BY p.id DESC";
		} else {

			Set<String> permissionConditions = Sets.newHashSet();
			permissionConditions.add("1<>1");
			for (UserRole ur : user.getUserRoles()) {
				if (ur.getRole().getCondition() != null) {
					// you used to add the same condition to the list twice...
					String permissionCondition = ur.getRole().getCondition();

					if (permissionCondition.contains("realCollegeId")) {
						permissionCondition = permissionCondition.replace(
								"{realCollegeId}",
								String.valueOf(ur.getCollege().getId()));
					}

					if (permissionCondition.contains("realDeptId")) {
						permissionCondition = permissionCondition.replace(
								"{realDeptId}",
								String.valueOf(ur.getDepartment().getId()));
					}

					if (permissionCondition.contains("realSubDeptId")) {
						permissionCondition = permissionCondition.replace(
								"{realSubDeptId}",
								String.valueOf(ur.getSubDepartment().getId()));
					}

					permissionConditions.add(permissionCondition);
				}
			}

			Joiner joiner = Joiner.on(" OR ").skipNulls();

			String permissionConditionString = joiner
					.join(permissionConditions);

			logger.debug("permissionConditionString: "
					+ permissionConditionString);

			String permissionPIDQuery = " (SELECT DISTINCT p.id FROM protocol p, securable_object_acl soa, securable_object so "
					// condition and soa are both permission related, so group
					// these two first...
					+ " WHERE ((soa.retired = :retired "
					+ " AND so.retired = :retired "
					+ " AND p.retired = :retired "
					+ " AND so.id = soa.securable_object_id "
					+ " AND soa.owner_class = :ownerClass AND soa.owner_id = :ownerId "
					// need to modify it if more permissions added in the future
					+ " AND (soa.permission = 'READ' OR soa.permission = 'ACCESS')"
					+ " AND so.object_class = :objectClass AND so.object_id = p.id)) "
					+ " UNION SELECT DISTINCT p.id FROM protocol p WHERE ("
							+ permissionConditionString + ")"
					+ ")";
			
			String searchPIDQuery = null;
			
			if(!xpathWhereClause.trim().isEmpty()) {
				searchPIDQuery = "(SELECT DISTINCT p.id FROM protocol p WHERE "
						// search should be performed based on the permission
						// constrains.
						+ ((searchCriterias != null) ? " (" + xpathWhereClause
								+ ")) " : "");
			}else{
			
				// need to have a default condition for "All Protocols" when I'm not a reviewer, it should be equaivalent to "My Protocols"
			}
			
			
			logger.debug("searchPIDQuery: " + searchPIDQuery);

			queryTotal = " SELECT COUNT(*) FROM protocol pol WHERE pol.id IN ("
					+ permissionPIDQuery + ")" + (searchPIDQuery == null?"":" AND pol.id IN (" + searchPIDQuery + ")");
			logger.debug("query:" + queryTotal);

			query = " SELECT pol.* FROM protocol pol WHERE pol.id IN ("
					+ permissionPIDQuery + ")" + (searchPIDQuery == null?"":" AND pol.id IN (" + searchPIDQuery + ")") + " ORDER BY pol.id DESC";

		}

		Query tq = getEntityManager().createNativeQuery(queryTotal);
		// tq.setHint("org.hibernate.cacheable", true);
		tq.setParameter("retired", Boolean.FALSE);
		if (!viewAllStudy || listMyProotcolOnly) {
			tq.setParameter("ownerClass", User.class);
			tq.setParameter("ownerId", user.getId());
			tq.setParameter("objectClass", Protocol.class);
		}

		long total = Long.valueOf(tq.getSingleResult().toString());
		pagedList.setTotal(total);

		TypedQuery<Protocol> q = (TypedQuery<Protocol>) getEntityManager()
				.createNativeQuery(query, Protocol.class);
		q.setFirstResult(start).setMaxResults(limit);

		q.setHint("org.hibernate.cacheable", true);
		q.setParameter("retired", Boolean.FALSE);
		if (!viewAllStudy || listMyProotcolOnly) {
			q.setParameter("ownerClass", User.class);
			q.setParameter("ownerId", user.getId());
			q.setParameter("objectClass", Protocol.class);
		}

		pagedList.setList(q.getResultList());
		return pagedList;
	}

	@Transactional(readOnly = true)
	public List<ProtocolFormXmlData> listLastestProtocolXmlDatas() {

		String query = "SELECT pfxd FROM ProtocolFormXmlData pfxd WHERE pfxd.id IN ("
				+ " SELECT MAX(pfxdd.id) FROM ProtocolFormXmlData pfxdd "
				+ " WHERE pfxdd.retired = :retired"
				+ " AND pfxdd.protocolForm.retired = :retired"
				+ " AND pfxdd.protocolForm.protocol.retired = :retired "
				+ " AND pfxdd.protocolFormXmlDataType = :protocolFormXmlDataType "
				+ " GROUP BY pfxdd.protocolForm.protocol.id, pfxdd.parent) ";

		TypedQuery<ProtocolFormXmlData> q = getEntityManager().createQuery(
				query, ProtocolFormXmlData.class);
		q.setHint("org.hibernate.cacheable", true);
		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("protocolFormXmlDataType",
				ProtocolFormXmlDataType.PROTOCOL);

		return q.getResultList();
	}

	@Transactional(readOnly = true)
	public ProtocolFormXmlData getLastestProtocolXmlDataByProtocolId(
			long protocolId) {

		String query = "SELECT pfxd FROM ProtocolFormXmlData pfxd "
				+ " WHERE pfxd.retired = :retired"
				+ " AND pfxd.protocolForm.retired = :retired"
				+ " AND pfxd.protocolForm.protocol.retired = :retired "
				+ " AND pfxd.protocolFormXmlDataType = :protocolFormXmlDataType "
				+ " AND pfxd.protocolForm.protocol.id = :protocolId"
				+ " ORDER BY pfxd.id DESC";

		TypedQuery<ProtocolFormXmlData> q = getEntityManager().createQuery(
				query, ProtocolFormXmlData.class);

		q.setFirstResult(0);
		q.setMaxResults(1);
		q.setHint("org.hibernate.cacheable", true);
		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("protocolFormXmlDataType",
				ProtocolFormXmlDataType.PROTOCOL);
		q.setParameter("protocolId", protocolId);

		return q.getSingleResult();
	}

	@Transactional(readOnly = true)
	public List<Protocol> listProtocolswithLatestStatus(ProtocolStatusEnum protocolStatus) {
		
		String query = "SELECT p FROM  Protocol p, ProtocolStatus ps "
				+ " WHERE p.id = ps.protocol.id AND ps.retired = :retired AND p.retired = :retired"
				+ " AND ps.id IN (SELECT MAX(ps2.id) FROM ProtocolStatus ps2 WHERE ps2.retired = :retired  GROUP BY ps2.protocol.id )"
				+ " AND ps.protocolStatus = :protocolStatus";
		TypedQuery<Protocol> q = getEntityManager().createQuery(query,
				Protocol.class);
		
		q.setHint("org.hibernate.cacheable", true);
		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("protocolStatus", protocolStatus);
		return q.getResultList();
	}
	
	@Transactional(readOnly = true)
	public ProtocolStatus getLatestProtocolStatusByProtocolId(long protocolId) {
		String query = "SELECT ps FROM ProtocolStatus ps "
				+ " WHERE ps.protocol.id = :protocolId AND ps.retired = :retired ORDER BY ps.id DESC";

		TypedQuery<ProtocolStatus> q = getEntityManager().createQuery(query,
				ProtocolStatus.class);
		q.setFirstResult(0);
		q.setMaxResults(1);
		q.setHint("org.hibernate.cacheable", true);
		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("protocolId", protocolId);

		return q.getSingleResult();
	}

	/*
	 * @Transactional(readOnly = true) public List<ProtocolFormXmlData>
	 * getPath() { String query = "SELECT * FROM protocol_form_xml_data" +
	 * " WHERE retired = :retired " +
	 * " AND xml_data.exist('/protocol/misc/epic-desc')=1";
	 * 
	 * TypedQuery<ProtocolFormXmlData> q = (TypedQuery<ProtocolFormXmlData>)
	 * getEntityManager().createNativeQuery(query, ProtocolFormXmlData.class);
	 * 
	 * q.setHint("org.hibernate.cacheable", true); q.setParameter("retired",
	 * Boolean.FALSE);
	 * 
	 * return q.getResultList(); }
	 */

	@Transactional(readOnly = true)
	public PagedList<Contract> listPagedContractMetaDatasByUserAndProtocolId(
			User user, int start, int limit, long protocolId) {
		PagedList<Contract> pagedList = new PagedList<Contract>();
		pagedList.setStart(start);
		pagedList.setLimit(limit);

		String queryTotal = " SELECT COUNT(*) FROM contract c "
				+ " WHERE c.retired = :retired "
				+ " AND c.id IN (SELECT ro.related_object_id FROM related_object ro WHERE ro.object_id = :protocolId AND ro.object_type = 'Protocol' AND ro.related_object_type = 'Contract' AND ro.retired = :retired GROUP BY ro.related_object_id UNION "
				+ " SELECT rlc.object_id FROM related_object rlc WHERE rlc.related_object_id = :protocolId AND rlc.object_type = 'Contract' AND rlc.related_object_type = 'Protocol' AND rlc.retired = :retired GROUP BY rlc.object_id) ";

		Query tq = getEntityManager().createNativeQuery(queryTotal);
		// tq.setHint("org.hibernate.cacheable", true);
		tq.setParameter("retired", Boolean.FALSE);
		tq.setParameter("protocolId", protocolId);

		long total = Long.valueOf(tq.getSingleResult().toString());

		pagedList.setTotal(total);

		String query = " SELECT c.* FROM contract c "
				+ " WHERE c.retired = :retired "
				+ " AND c.id IN (SELECT ro.related_object_id FROM related_object ro WHERE ro.object_id = :protocolId AND ro.object_type = 'Protocol' AND ro.related_object_type = 'Contract' AND ro.retired = :retired GROUP BY ro.related_object_id UNION "
				+ " SELECT rlc.object_id FROM related_object rlc WHERE rlc.related_object_id = :protocolId AND rlc.object_type = 'Contract' AND rlc.related_object_type = 'Protocol' AND rlc.retired = :retired GROUP BY rlc.object_id) ";

		TypedQuery<Contract> q = (TypedQuery<Contract>) getEntityManager()
				.createNativeQuery(query, Contract.class);
		q.setFirstResult(start);
		q.setMaxResults(limit);
		// q.setHint("org.hibernate.cacheable", true);

		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("protocolId", protocolId);

		pagedList.setList(q.getResultList());
		return pagedList;
	}
	
	@Transactional(readOnly = true)
	public List<Contract> listRelatedContractsByProtocolId(long protocolId) {

		String query = " SELECT c.* FROM contract c "
				+ " WHERE c.retired = :retired "
				+ " AND c.id IN (SELECT ro.related_object_id FROM related_object ro WHERE ro.object_id = :protocolId AND ro.object_type = 'Protocol' AND ro.related_object_type = 'Contract' AND ro.retired = :retired GROUP BY ro.related_object_id UNION "
				+ " SELECT rlc.object_id FROM related_object rlc WHERE rlc.related_object_id = :protocolId AND rlc.object_type = 'Contract' AND rlc.related_object_type = 'Protocol' AND rlc.retired = :retired GROUP BY rlc.object_id) ";

		TypedQuery<Contract> q = (TypedQuery<Contract>) getEntityManager()
				.createNativeQuery(query, Contract.class);
		// q.setHint("org.hibernate.cacheable", true);

		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("protocolId", protocolId);


		return q.getResultList();
	}

	// mostly for data integration... including the retired ones
	@Transactional(readOnly = true)
	public List<Protocol> listProtocolsByIdRange(long startId, long endId) {
		TypedQuery<Protocol> query = getEntityManager()
				.createQuery(
						"SELECT p FROM Protocol p WHERE p.id >= :startId AND p.id <= :endId AND p.retired = :retired",
						Protocol.class);
		query.setHint("org.hibernate.cacheable", true);
		query.setParameter("startId", startId);
		query.setParameter("endId", endId);
		query.setParameter("retired", Boolean.FALSE);

		return query.getResultList();
	}

	@Transactional(readOnly = true)
	public List<Protocol> listAllProtocols() {
		TypedQuery<Protocol> query = getEntityManager().createQuery(
				"SELECT p FROM Protocol p", Protocol.class);
		query.setHint("org.hibernate.cacheable", true);

		return query.getResultList();
	}

	public FileGenerateAndSaveService getFileGenerateAndSaveService() {
		return fileGenerateAndSaveService;
	}

	@Autowired(required=true)
	public void setFileGenerateAndSaveService(FileGenerateAndSaveService fileGenerateAndSaveService) {
		this.fileGenerateAndSaveService = fileGenerateAndSaveService;
	}

	public String getFileServer() {
		return fileServer;
	}

	public void setFileServer(String fileServer) {
		this.fileServer = fileServer;
	}

}
