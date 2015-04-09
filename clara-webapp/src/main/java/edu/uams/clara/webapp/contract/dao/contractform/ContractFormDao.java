package edu.uams.clara.webapp.contract.dao.contractform;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import edu.uams.clara.core.dao.AbstractDomainDao;
import edu.uams.clara.core.util.xml.XmlHandler;
import edu.uams.clara.core.util.xml.XmlHandlerFactory;
import edu.uams.clara.webapp.common.domain.usercontext.User;
import edu.uams.clara.webapp.common.domain.usercontext.enums.Permission;
import edu.uams.clara.webapp.common.objectwrapper.PagedList;
import edu.uams.clara.webapp.common.util.RawvalueLookupService;
import edu.uams.clara.webapp.contract.domain.Contract;
import edu.uams.clara.webapp.contract.domain.businesslogicobject.ContractFormStatus;
import edu.uams.clara.webapp.contract.domain.businesslogicobject.enums.ContractFormStatusEnum;
import edu.uams.clara.webapp.contract.domain.contractform.ContractForm;
import edu.uams.clara.webapp.contract.domain.contractform.enums.ContractFormType;
import edu.uams.clara.webapp.contract.objectwrapper.ContractSearchCriteria;
import edu.uams.clara.webapp.fileserver.domain.UploadedFile;
import edu.uams.clara.webapp.fileserver.service.FileGenerateAndSaveService;

@Repository
public class ContractFormDao extends AbstractDomainDao<ContractForm> {
	private final static Logger logger = LoggerFactory
			.getLogger(ContractFormDao.class);

	private static final long serialVersionUID = 7492131024720753963L;
	
	private MessageDigest messageDigest = null;
	
	@Value("${fileserver.url}")
	private String fileServer;

	private FileGenerateAndSaveService fileGenerateAndSaveService;
	
	private RawvalueLookupService rawvalueLookupService;

	@Transactional(readOnly = true)
	public ContractFormStatus getLatestContractFormStatusByContractFormId(long contractFormId){
		String query = "SELECT pfs FROM ContractFormStatus pfs "
			+ " WHERE pfs.contract.id = :contractFormId AND pfs.retired = :retired ORDER BY pfs.id DESC";

		TypedQuery<ContractFormStatus> q = getEntityManager().createQuery(query,
				ContractFormStatus.class);
		q.setFirstResult(0);
		q.setMaxResults(1);
		q.setHint("org.hibernate.cacheable", true);
		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("contractFormId", contractFormId);
	
		return q.getSingleResult();
	}
	
	@Transactional(readOnly = true)
	public List<ContractForm> listContractFormsByContractIdAndContractFormType(
			long contractId, ContractFormType contractFormType) {

		String query = "SELECT cf FROM ContractForm cf "
			+ " WHERE cf.contract.id = :contractId AND cf.contractFormType = :contractFormType AND cf.retired = :retired";

		TypedQuery<ContractForm> q = getEntityManager()
				.createQuery(query, ContractForm.class);

		q.setHint("org.hibernate.cacheable", true);

		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("contractId", contractId);
		q.setParameter("contractFormType", contractFormType);
		return q.getResultList();
	}
	
	@Transactional(readOnly = true)
	public List<ContractForm> listContractFormsByContractId(
			long contractId) {

		String query = "SELECT cf FROM ContractForm cf "
			+ " WHERE cf.contract.id = :contractId AND cf.retired = :retired";

		TypedQuery<ContractForm> q = getEntityManager()
				.createQuery(query, ContractForm.class);

		q.setHint("org.hibernate.cacheable", true);

		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("contractId", contractId);
		return q.getResultList();
	}
	
	@Transactional(readOnly = true)
	public ContractForm getContractFormByContractIdAndContractFormType(long contractId, ContractFormType contractFormType){
		String query = "SELECT cf FROM ContractForm cf "
			+ " WHERE cf.contract.id = :contractId AND cf.contractFormType = :contractFormType AND cf.retired = :retired";

		TypedQuery<ContractForm> q = getEntityManager().createQuery(query,
				ContractForm.class);
		q.setFirstResult(0);
		q.setMaxResults(1);
		q.setHint("org.hibernate.cacheable", true);
		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("contractId", contractId);
		q.setParameter("contractFormType", contractFormType);
	
		return q.getSingleResult();
	}
	
	@Transactional(readOnly = true)
	public ContractForm getLatestContractFormByContractIdAndContractFormType(long contractId, ContractFormType contractFormType){
		String query = "SELECT cf FROM ContractForm cf "
			+ " WHERE cf.contract.id = :contractId AND cf.contractFormType = :contractFormType AND cf.retired = :retired "
			+ " ORDER BY cf.id DESC ";

		TypedQuery<ContractForm> q = getEntityManager().createQuery(query,
				ContractForm.class);
		q.setFirstResult(0);
		q.setMaxResults(1);
		q.setHint("org.hibernate.cacheable", true);
		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("contractId", contractId);
		q.setParameter("contractFormType", contractFormType);
	
		return q.getSingleResult();
	}
	
	@Transactional(readOnly = true)
	public String exportBookmarkSearchResultFile(String xmlData,User user){
		String fileUrl = "";
		try {
			HSSFWorkbook wb = new HSSFWorkbook();
			CreationHelper createHelper = wb.getCreationHelper();
			Sheet sheet = wb.createSheet("bookmark-search-result");
			Row titleRow = sheet.createRow(0);
			String[] titles = {"Contract Identifier","Contract Type","Contract Form Type","IRB","PI","Entity","Status","Created","Contract Legal Review","Contract Admin"};
			for(int i=0;i<titles.length;i++){
				Cell infoCell = titleRow.createCell(i);
				infoCell.setCellValue(createHelper.createRichTextString(titles[i]));
			}
			
			
			XmlHandler xmlHandler =  XmlHandlerFactory.newXmlHandler();
			List<Element> protocolEles = xmlHandler.listElementsByXPath(xmlData, "/list/contract");
			for(int j=0;j<protocolEles.size();j++){
				Row dataRow = sheet.createRow(j+1);
				Element contractFormEle = protocolEles.get(j);
				String cfid = contractFormEle.getAttribute("id");
				String contractIdentifier = contractFormEle.getAttribute("identifier");
				String contractFormType =  contractFormEle.getAttribute("type");
				String contractFormIndex =  contractFormEle.getAttribute("index");
				
				Cell infoCell0 = dataRow.createCell(0);
				infoCell0.setCellValue(createHelper.createRichTextString(contractIdentifier));
				
				String type = xmlHandler.getSingleStringValueByXPath(xmlData, "/list/contract[@id=\""+cfid+"\"][@type=\""+contractFormType+"\"][@index = \""+contractFormIndex+"\"]/type/text()");
				String subType = xmlHandler.getSingleStringValueByXPath(xmlData, "/list/contract[@id=\""+cfid+"\"][@type=\""+contractFormType+"\"][@index = \""+contractFormIndex+"\"]/type/"+type+"/sub-type/text()");
				
				type = rawvalueLookupService.rawvalueLookUp(type);
				
				if(!subType.isEmpty()){
					subType = rawvalueLookupService.rawvalueLookUp(subType);
					type +=" ("+subType+")";
				}
				
				Cell infoCell1 = dataRow.createCell(1);
				infoCell1.setCellValue(createHelper.createRichTextString(type));
				
				
				if(contractFormType.equals("Amendment")){
					contractFormType +=" "+contractFormIndex;
				}
				
				Cell infoCell2 = dataRow.createCell(2);
				infoCell2.setCellValue(createHelper.createRichTextString(contractFormType));

				String irb = xmlHandler.getSingleStringValueByXPath(xmlData, "/list/contract[@id=\""+cfid+"\"][@type=\""+contractFormType+"\"][@index = \""+contractFormIndex+"\"]/protocol/text()");
				
				Cell infoCell3 = dataRow.createCell(3);
				infoCell3.setCellValue(createHelper.createRichTextString(irb));
				
				
				
				String piName = xmlHandler.getSingleStringValueByXPath(xmlData, "/list/contract[@id=\""+cfid+"\"][@type=\""+contractFormType+"\"][@index = \""+contractFormIndex+"\"]/staffs/staff/user[roles/role/text()=\"Principal Investigator\"]/lastname/text()")+","+
						xmlHandler.getSingleStringValueByXPath(xmlData, "/list/contract[@id=\""+cfid+"\"][@type=\""+contractFormType+"\"][@index = \""+contractFormIndex+"\"]/staffs/staff/user[roles/role/text()=\"Principal Investigator\"]/firstname/text()");
				Cell infoCell4 = dataRow.createCell(4);
				if(piName.endsWith(",")){
					piName="";
				}
				infoCell4.setCellValue(createHelper.createRichTextString(piName));
				
				List<Element> sponsors =  xmlHandler.listElementsByXPath(xmlData, "/list/contract[@id=\""+cfid+"\"][@type=\""+contractFormType+"\"][@index = \""+contractFormIndex+"\"]/sponsors/sponsor");
				String entity = "";
				for(int i = 0;i<sponsors.size();i++){
					Element sponsor = sponsors.get(i);
					String record = "";
					try{
						record = sponsor.getElementsByTagName("company").item(0).getTextContent();
						record+= " : "+sponsor.getElementsByTagName("name").item(0).getTextContent();
					}catch(Exception e){
						record+= sponsor.getElementsByTagName("name").item(0).getTextContent();
					}
					
					if(!record.isEmpty()){
						entity+=(i+1)+" : " +record +"  ";
					}
				}
				
				Cell infoCell5 = dataRow.createCell(5);
				infoCell5.setCellValue(createHelper.createRichTextString(entity));
				
				String status = xmlHandler.getSingleStringValueByXPath(xmlData, "/list/contract[@id=\""+cfid+"\"][@type=\""+contractFormType+"\"][@index = \""+contractFormIndex+"\"]/status/text()");
				Cell infoCell6 = dataRow.createCell(6);
				infoCell6.setCellValue(createHelper.createRichTextString(status));
				
				
				String created = xmlHandler.getSingleStringValueByXPath(xmlData, "/list/contract[@id=\""+cfid+"\"][@type=\""+contractFormType+"\"][@index = \""+contractFormIndex+"\"]/@created");
				Cell infoCell7 = dataRow.createCell(7);
				infoCell7.setCellValue(createHelper.createRichTextString(created));
				
				String legalreview = "";
				List<Element> legalEles = xmlHandler.listElementsByXPath(xmlData, "/list/contract[@id=\""+cfid+"\"][@type=\""+contractFormType+"\"][@index = \""+contractFormIndex+"\"]/committee-review/committee[@type=\"CONTRACT_LEGAL_REVIEW\"]/assigned-reviewers/assigned-reviewer");
				for(int i = 0;i<legalEles.size();i++){
					Element legalEle = legalEles.get(i);
					legalreview += (i+1)+": "+legalEle.getAttribute("user-fullname")+" ";
					
				}
				
				Cell infoCell8 = dataRow.createCell(8);
				infoCell8.setCellValue(createHelper.createRichTextString(legalreview));
				
				
				String contractadmin = "";
				List<Element> adminEles = xmlHandler.listElementsByXPath(xmlData, "/list/contract[@id=\""+cfid+"\"][@type=\""+contractFormType+"\"][@index = \""+contractFormIndex+"\"]/committee-review/committee[@type=\"CONTRACT_ADMIN\"]/assigned-reviewers/assigned-reviewer");
				for(int i = 0;i<adminEles.size();i++){
					Element adminEle = adminEles.get(i);
					contractadmin += (i+1)+": "+adminEle.getAttribute("user-fullname")+" ";
					
				}
				
				Cell infoCell9 = dataRow.createCell(9);
				infoCell9.setCellValue(createHelper.createRichTextString(contractadmin));
				
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
		return fileUrl;
		
		
	}
	
	
	@Transactional(readOnly = true)
	private List<String> contractSearchCriteriaResolver(List<ContractSearchCriteria> searchCriteria){
		List<String> xPathCriteria = new ArrayList<String>();
		
		for (ContractSearchCriteria p:searchCriteria){
			switch (p.getSearchField()){
			case IDENTIFIER:{
				switch (p.getSearchOperator()){
				case CONTAINS:
					xPathCriteria.add("meta_data_xml.exist('/contract[fn:contains(@identifier,\""+p.getKeyword().toUpperCase()+"\")]') = 1");
					//xPathCriteria.add("meta_data_xml.exist('/protocol[@identifier[fn:contains(fn:upper-case(.), \"" + p.getKeyword().toUpperCase() + "\")]]') = 1");
					break;				
				case EQUALS:
					xPathCriteria.add("meta_data_xml.exist('/contract[@identifier[. = \"" + p.getKeyword() + "\"]]') = 1");
					break;
				case DOES_NOT_CONTAIN:
					xPathCriteria.add("meta_data_xml.exist('/contract[@identifier[fn:contains(fn:upper-case(.), \"" + p.getKeyword().toUpperCase() + "\")]]') = 0");
				default:
					break;
				}
			}
			break;
			case TITLE:{
				switch (p.getSearchOperator()){
				case CONTAINS:
					xPathCriteria.add("CONTAINS(meta_data_xml, '"+ p.getKeyword() +"') AND meta_data_xml.exist('/contract/title/text()[fn:contains(fn:upper-case(.), \"" + p.getKeyword().toUpperCase() + "\")]') = 1");
					break;				
				case EQUALS:
					xPathCriteria.add("CONTAINS(meta_data_xml, '"+ p.getKeyword() +"') AND meta_data_xml.exist('/contract/title[. = \"" + p.getKeyword() + "\"]') = 1");
					break;
				case DOES_NOT_CONTAIN:
					xPathCriteria.add("meta_data_xml.exist('/contract/title/text()[fn:contains(fn:upper-case(.), \"" + p.getKeyword().toUpperCase() + "\")]') = 0");
				default:
					break;
				}
			}
			break;
			case PI_USERID:{
				switch (p.getSearchOperator()){
				case CONTAINS:
					xPathCriteria
					.add("meta_data_xml.exist('/contract/staffs/staff/user[roles/role=\"Principal Investigator\" and @id = \""
							+ p.getKeyword() + "\"]') = 1");
					break;				
				case EQUALS:
					xPathCriteria
					.add("meta_data_xml.exist('/contract/staffs/staff/user[roles/role=\"Principal Investigator\" and @id = \""
							+ p.getKeyword() + "\"]') = 1");
					break;
				case DOES_NOT_CONTAIN:
					xPathCriteria
					.add("meta_data_xml.exist('/contract/staffs/staff/user[roles/role=\"Principal Investigator\" and @id = \""
							+ p.getKeyword() + "\"]') = 0");
				default:
					break;
				}
			}
			break;
			case ASSIGNED_REVIEWER_USERID:{
				switch (p.getSearchOperator()){
				case CONTAINS:
					xPathCriteria
					.add("meta_data_xml.exist('/contract/committee-review/committee/assigned-reviewers/assigned-reviewer[@user-id = \""
							+ p.getKeyword() + "\"]') = 1");
					break;				
				case EQUALS:
					xPathCriteria
					.add("meta_data_xml.exist('/contract/committee-review/committee/assigned-reviewers/assigned-reviewer[@user-id = \""
							+ p.getKeyword() + "\"]') = 1");
					break;
				case DOES_NOT_CONTAIN:
					xPathCriteria
					.add("meta_data_xml.exist('/contract/committee-review/committee/assigned-reviewers/assigned-reviewer[@user-id = \""
							+ p.getKeyword() + "\"]') = 0");
				default:
					break;
				}
			}
			break;
			case CONTRACT_ADMIN_USERID:{
				switch (p.getSearchOperator()){
				case CONTAINS:
					xPathCriteria
					.add("meta_data_xml.exist('/contract/committee-review/committee[@type=\"CONTRACT_ADMIN\"]/assigned-reviewers/assigned-reviewer[@user-id = \""
							+ p.getKeyword() + "\"]') = 1");
					break;				
				case EQUALS:
					xPathCriteria
					.add("meta_data_xml.exist('/contract/committee-review/committee[@type=\"CONTRACT_ADMIN\"]/assigned-reviewers/assigned-reviewer[@user-id = \""
							+ p.getKeyword() + "\"]') = 1");
					logger.debug("meta_data_xml.exist('/contract/committee-review/committee[@type=\"CONTRACT_ADMIN\"]/assigned-reviewers/assigned-reviewer[@user-id = \""
							+ p.getKeyword() + "\"]') = 1");
					break;
				case DOES_NOT_CONTAIN:
					xPathCriteria
					.add("meta_data_xml.exist('/contract/committee-review/committee[@type=\"CONTRACT_ADMIN\"]/assigned-reviewers/assigned-reviewer[@user-id = \""
							+ p.getKeyword() + "\"]') = 0");
				default:
					break;
				}
			}
			break;
			case CONTRACT_LEGAL_REVIEW_USERID:{
				switch (p.getSearchOperator()){
				case CONTAINS:
					xPathCriteria
					.add("meta_data_xml.exist('/contract/committee-review/committee[@type=\"CONTRACT_LEGAL_REVIEW\"]/assigned-reviewers/assigned-reviewer[@user-id = \""
							+ p.getKeyword() + "\"]') = 1");
					break;				
				case EQUALS:
					xPathCriteria
					.add("meta_data_xml.exist('/contract/committee-review/committee[@type=\"CONTRACT_LEGAL_REVIEW\"]/assigned-reviewers/assigned-reviewer[@user-id = \""
							+ p.getKeyword() + "\"]') = 1");
					break;
				case DOES_NOT_CONTAIN:
					xPathCriteria
					.add("meta_data_xml.exist('/contract/committee-review/committee[@type=\"CONTRACT_LEGAL_REVIEW\"]/assigned-reviewers/assigned-reviewer[@user-id = \""
							+ p.getKeyword() + "\"]') = 0");
				default:
					break;
				}
			}
			break;
			case PI_NAME:{
				switch (p.getSearchOperator()){
				case CONTAINS:
					xPathCriteria.add("meta_data_xml.exist('/contract/staffs/staff/user[roles/role=\"Principal Investigator\" and (lastname[fn:contains(fn:upper-case(.), \"" + p.getKeyword().toUpperCase() + "\")] or firstname[fn:contains(fn:upper-case(.), \"" + p.getKeyword().toUpperCase() + "\")])]') = 1");
					break;				
				case EQUALS:
					xPathCriteria.add("meta_data_xml.exist('/contract/staffs/staff/user[roles/role=\"Principal Investigator\" and (lastname[fn:contains(fn:upper-case(.), \"" + p.getKeyword().toUpperCase() + "\")] or firstname[fn:contains(fn:upper-case(.), \"" + p.getKeyword().toUpperCase() + "\")])]') = 1");
					break;
				case DOES_NOT_CONTAIN:
					xPathCriteria.add("meta_data_xml.exist('/contract/staffs/staff/user[roles/role=\"Principal Investigator\" and (lastname[fn:contains(fn:upper-case(.), \"" + p.getKeyword().toUpperCase() + "\")] or firstname[fn:contains(fn:upper-case(.), \"" + p.getKeyword().toUpperCase() + "\")])]') = 0");
				default:
					break;
				}
			}
			break;
			case CONTRACT_STATUS:{
				String status = ContractFormStatusEnum.valueOf(p.getKeyword()).getDescription();
				switch (p.getSearchOperator()){
				case CONTAINS:
					xPathCriteria
					.add("meta_data_xml.exist('/contract/status[. = \""+ status +"\"]')=1");
					break;				
				case EQUALS:
					xPathCriteria
					.add("meta_data_xml.exist('/contract/status[. = \""+ status +"\"]')=1");
					break;
				case DOES_NOT_CONTAIN:
					xPathCriteria
					.add("meta_data_xml.exist('/contract/status[. = \""+ status +"\"]') = 0");
				default:
					break;
				}
			}
			break;
			case STAFF_USERID:{
				switch (p.getSearchOperator()){
				case CONTAINS:
					xPathCriteria.add("meta_data_xml.exist('/contract/staffs/staff/user[@id = \""+ p.getKeyword() +"\"]') = 1");
					break;				
				case EQUALS:
					xPathCriteria.add("meta_data_xml.exist('/contract/staffs/staff/user[@id = \""+ p.getKeyword() +"\"]') = 1");
					break;
				case DOES_NOT_CONTAIN:
					xPathCriteria.add("meta_data_xml.exist('/contract/staffs/staff/user[@id = \""+ p.getKeyword() +"\"]') = 0");
				default:
					break;
				}
			}
			break;	
			case CONTRACT_TYPE:{
				switch (p.getSearchOperator()){
				case CONTAINS:
					xPathCriteria.add("CONTAINS(meta_data_xml, '"+ p.getKeyword() +"') AND meta_data_xml.exist('/contract/type/text()[fn:contains(fn:upper-case(.), \"" + p.getKeyword().toUpperCase() + "\")]') = 1");
					break;				
				case EQUALS:
					xPathCriteria.add("CONTAINS(meta_data_xml, '"+ p.getKeyword() +"') AND meta_data_xml.exist('/contract/type/text()[. = \"" + p.getKeyword() + "\"]') = 1");
					break;
				case DOES_NOT_CONTAIN:
					xPathCriteria.add("meta_data_xml.exist('/contract/type/text()[fn:contains(fn:upper-case(.), \"" + p.getKeyword().toUpperCase() + "\")]') = 0");
				default:
					break;
				}
			}
			break;
/*			case CONTRACT_FORM_TYPE:{
				switch (p.getSearchOperator()){
				case CONTAINS:
					xPathCriteria.add("meta_data_xml.exist('/contract/[@type = \""+ p.getKeyword() +"\"]') = 1");
					break;				
				case EQUALS:
					xPathCriteria.add("meta_data_xml.exist('/contract/[@type = \""+ p.getKeyword() +"\"]') = 1");
					break;
				case DOES_NOT_CONTAIN:
					xPathCriteria.add("meta_data_xml.exist('/contract/[@type = \""+ p.getKeyword() +"\"]') = 0");
				default:
					break;
				}
			}
			break;*/
			case ENTITY_NAME:{
				switch (p.getSearchOperator()){
				case CONTAINS:
					xPathCriteria.add("CONTAINS(meta_data_xml, '"+ p.getKeyword() +"') AND meta_data_xml.exist('/contract/sponsors/sponsor/name[fn:contains(fn:upper-case(.), \"" + p.getKeyword().toUpperCase() + "\") or /contract/sponsors/sponsor/company[fn:contains(fn:upper-case(.), \"" + p.getKeyword().toUpperCase() + "\")]]') = 1");
					break;				
				case EQUALS:
					xPathCriteria.add("CONTAINS(meta_data_xml, '"+ p.getKeyword() +"') AND meta_data_xml.exist('/contract/sponsors/sponsor/name[fn:contains(fn:upper-case(.), \"" + p.getKeyword().toUpperCase() + "\") or /contract/sponsors/sponsor/company[fn:contains(fn:upper-case(.), \"" + p.getKeyword().toUpperCase() + "\")]]') = 1");
					break;
				case DOES_NOT_CONTAIN:
					xPathCriteria.add("meta_data_xml.exist('/contract/sponsors/sponsor/name[fn:contains(fn:upper-case(.), \"" + p.getKeyword().toUpperCase() + "\") or /contract/sponsors/sponsor/company[fn:contains(fn:upper-case(.), \"" + p.getKeyword().toUpperCase() + "\")]]') = 0");
				default:
					break;
				}
			}
			break;
			case PROTOCOL_ID:{
				switch (p.getSearchOperator()){
				/*
				case CONTAINS:
					xPathCriteria.add("meta_data_xml.exist('/contract/protocol/text()[fn:contains(fn:upper-case(.), \"" + p.getKeyword().toUpperCase() + "\")]') = 1");
					break;				
				case EQUALS:
					xPathCriteria.add("meta_data_xml.exist('/contract/protocol[. = \"" + p.getKeyword() + "\"]') = 1");
					break;
				case DOES_NOT_CONTAIN:
					xPathCriteria.add("meta_data_xml.exist('/contract/protocol/text()[fn:contains(fn:upper-case(.), \"" + p.getKeyword().toUpperCase() + "\")]') = 0");
				*/
				case CONTAINS:
					xPathCriteria.add("contract_id IN (SELECT related_object_id FROM related_object WHERE related_object_type = 'contract' AND object_id = \""+ p.getKeyword() +"\" AND retired = 0)");
					break;
				case EQUALS:
					xPathCriteria.add("contract_id IN (SELECT related_object_id FROM related_object WHERE related_object_type = 'contract' AND object_id = \""+ p.getKeyword() +"\" AND retired = 0)");
					break;
				case DOES_NOT_CONTAIN:
					xPathCriteria.add("contract_id IN (SELECT related_object_id FROM related_object WHERE related_object_type = 'contract' AND object_id = \""+ p.getKeyword() +"\" AND retired = 0)");
					break;
				default:
					break;
				}
			}
			break;
			default:
				break;
			}
		}

		/*for (ContractSearchField field : fields) {
			switch (field) {
			case TITLE:
				xPathCriteria.add("meta_data.exist('/contract/title[fn:contains(., \"" + keyword + "\")]') = 1");
				break;
			case PI_NAME:
				xPathCriteria.add("meta_data.exist('/contract/staffs/staff/user/roles/role[.=\"Principal Investigator\" and (../../user/lastname[fn:contains(fn:upper-case(.), \"" + keyword.toUpperCase() + "\")] or ../../user/firstname[fn:contains(fn:upper-case(.), \"" + keyword.toUpperCase() + "\")])]') = 1");
				break;
			case PROTOCOL_STATUS:				
				break;
			case STAFF_NAME:
				xPathCriteria.add("meta_data.exist('/contract/staffs/staff/user/lastname[fn:contains(fn:upper-case(.), \"" + keyword.toUpperCase() + "\")]') = 1");
				xPathCriteria.add("meta_data.exist('/contract/staffs/staff/user/firstname[fn:contains(fn:upper-case(.), \"" + keyword.toUpperCase() + "\")]') = 1");
				break;
			default:
				break;
			}
		}*/

		return xPathCriteria;		
	}
	
	private static boolean isInteger(String s) {
	    try { 
	        Integer.parseInt(s); 
	    } catch(NumberFormatException e) { 
	        return false; 
	    }
	    // only got here if we didn't return false
	    return true;
	}
	
	private Map<String, String> sortFieldMap = Maps.newHashMap();{
		sortFieldMap.put("id", "id");
		sortFieldMap.put("contractType", "id");
		sortFieldMap.put("status", "meta_data_xml.value('(//status/text())[1]', 'varchar(100)')");
		sortFieldMap.put("entity", "meta_data_xml.value('(//sponsors/sponsor/company/text())[1]', 'varchar(100)')");
		sortFieldMap.put("studyIdentifier", "meta_data_xml.value('(//protocol/text())[1]', 'varchar(100)')");
	} 
	
	@Transactional(readOnly = true)
	public PagedList<ContractForm> listPagedContractMetaDatasByUserAndSearchCriteriaAndContractStatusFilter(User user, int start, int limit, List<ContractSearchCriteria> searchCriterias, ContractFormStatusEnum filter, String quickSearchKeyword, String sortField, String dir) {
		PagedList<ContractForm> pagedList = new PagedList<ContractForm>();
		pagedList.setStart(start);
		pagedList.setLimit(limit);
		
		String xpathWhereClause = "";
		
		if (quickSearchKeyword != null && !quickSearchKeyword.isEmpty()) {
			xpathWhereClause = "CONTAINS(meta_data_xml, '"+ quickSearchKeyword.replace(" ", "%") +"')";
			
			//@tickt #2869 
			//if quick search by protocol id, should search by both meta data and related object table
			if (isInteger(quickSearchKeyword)) {
				xpathWhereClause += " OR contract_id IN (SELECT related_object_id FROM related_object WHERE related_object_type = 'contract' AND object_id = "+ quickSearchKeyword +" AND retired = 0)";
			}
		} else {
			if(searchCriterias != null){
				List<String> xPathCriterias = contractSearchCriteriaResolver(searchCriterias);
				
				
				int c = xPathCriterias.size();
				
				int i = 0;
			
				for(String xc:xPathCriterias){
					xpathWhereClause += xc;
					
					if(i != c - 1){
						xpathWhereClause += " AND ";
					}
					
					i ++;
				}
			}
		}
		
		boolean viewAllContract = false;
		
		if (user.getAuthorities().contains(Permission.VIEW_ALL_CONTRACT)){
			viewAllContract = true;
		} 
		
		String queryTotal = "";
		String query = "";
		
		if (viewAllContract){
			/*
			queryTotal = " SELECT COUNT(*) FROM contract c "
					+ (filter !=  null?", contract_status cs":"")
					+ " WHERE c.retired = :retired "
					+ (filter !=  null?" AND cs.id = (SELECT MAX(cs.id) FROM contract_status cs WHERE cs.contract_id = c.id AND cs.contract_status = :contractStatus) ":"")
					+ ((searchCriterias != null)?" AND  (" + xpathWhereClause + ") ": "");
			*/
			
			queryTotal = " SELECT COUNT(DISTINCT cf.contract_id) FROM contract_form cf WHERE cf.id IN (SELECT MAX(c.id) FROM contract_form c "
					+ (filter !=  null?", contract_form_status cs":"")
					+ " WHERE c.retired = :retired "
					+ (filter !=  null?" AND cs.id = (SELECT MAX(cs.id) FROM contract_form_status cs WHERE cs.contract_form_id = c.id AND cs.contract_form_status = :contractFormStatus) ":"")
					+ ((searchCriterias != null)?" AND  (" + xpathWhereClause + ") ": "")
					+ " GROUP BY c.parent_id)";
			
			query = " SELECT cf.* FROM contract_form cf WHERE cf.id IN (SELECT MAX(c.id) FROM contract_form c "
					+ (filter !=  null?", contract_form_status cs":"")
					+ " WHERE c.retired = :retired "
					+ (filter !=  null?" AND cs.id = (SELECT MAX(cs.id) FROM contract_form_status cs WHERE cs.contract_form_id = c.id AND cs.contract_form_status = :contractFormStatus) ":"")
					+ ((searchCriterias != null)?" AND  (" + xpathWhereClause + ") ": "")
					+ " GROUP BY c.parent_id) "
					+ " AND cf.contract_form_type = 'NEW_CONTRACT'"
					+ ((sortField != null)?" ORDER BY "+ sortFieldMap.get(sortField) + " " + dir + ";":" ORDER BY cf.id DESC");
		} else {
			/*
			queryTotal = " SELECT COUNT(*) FROM contract cl WHERE cl.id IN ("
					+ " SELECT DISTINCT c.id FROM contract c, securable_object_acl soa, securable_object so "
					+ (filter != null ? ", contract_status cs" : "")
					+ " WHERE soa.retired = :retired "
					+ " AND so.retired = :retired "
					+ " AND c.retired = :retired "
					+ " AND so.id = soa.securable_object_id "
					+ " AND soa.owner_class = :ownerClass AND soa.owner_id = :ownerId "
					// need to modify it if more permissions added in the future
					+ " AND (soa.permission = 'READ' OR soa.permission = 'ACCESS')"
					+ " AND so.object_class = :objectClass AND so.object_id = c.id "
					+ (filter != null ? " AND cs.id = (SELECT MAX(cs.id) FROM contract_status cs WHERE cs.contract_id = c.id AND cs.contract_status = :contractStatus) "
							: "")
					+ ((searchCriterias != null) ? " AND  (" + xpathWhereClause
							+ ") " : "")
					+ ")";
			*/
			String permissionConditionString = "1<>1";
			
			if (user.getAuthorities().contains(Permission.VIEW_COM_CONTRACT)) {
				String CTACondition = "retired = 0 AND meta_data_xml.value('(/contract/staffs/staff/user[roles/role = \"Principal Investigator\"]/@id)[1]','bigint') IN (SELECT id FROM user_account WHERE retired = 0 AND person_id IN "
								+ " (SELECT id FROM person WHERE department LIKE '%COM%' AND retired = 0)) "
								+ " AND meta_data_xml.exist('/contract/type[text() = \"clinical-trial-agreement\"]')=1";
				
				permissionConditionString = CTACondition;
			}
			
			String permissionPIDQuery = " (SELECT DISTINCT c.id FROM contract_form c, securable_object_acl soa, securable_object so "
					+ (filter != null ? ", contract_form_status cfs" : "")
					+ " WHERE soa.retired = :retired "
					+ " AND so.retired = :retired "
					+ " AND c.retired = :retired "
					+ " AND so.id = soa.securable_object_id "
					+ " AND soa.owner_class = :ownerClass AND soa.owner_id = :ownerId "
					// need to modify it if more permissions added in the future
					+ " AND (soa.permission = 'READ' OR soa.permission = 'ACCESS') "
					+ " AND so.object_class = :objectClass AND so.object_id = c.contract_id "
					+ (filter != null ? " AND cfs.id = (SELECT MAX(cfs.id) FROM contract_form_status cfs WHERE cfs.contract_form_id = c.id AND cfs.contract_status = :contractStatus) "
							: "") + ")"
					+ " UNION SELECT DISTINCT id FROM contract_form WHERE ("
					+ permissionConditionString + ")";
			
			String searchPIDQuery = null;
			
			if(!xpathWhereClause.trim().isEmpty()) {
				searchPIDQuery = "(SELECT DISTINCT id FROM contract_form WHERE "
						// search should be performed based on the permission
						// constrains.
						+ ((searchCriterias != null) ? " (" + xpathWhereClause
								+ ")) " : "");
			}
			
			logger.debug("searchPIDQuery: " + searchPIDQuery);

			queryTotal = " SELECT COUNT(DISTINCT cform.contract_id) FROM contract_form cform WHERE cform.id IN ("
					+ permissionPIDQuery + ")" + (searchPIDQuery == null?"":" AND cform.id IN (" + searchPIDQuery + ")");
			logger.debug("query:" + queryTotal);

			query = " SELECT cform.* FROM contract_form cform WHERE cform.id IN ("
					+ permissionPIDQuery + ")" + (searchPIDQuery == null?"":" AND cform.id IN (" + searchPIDQuery + ")") 
					+ ((sortField != null)?" ORDER BY "+ sortFieldMap.get(sortField) + " " + dir + ";":" ORDER BY cf.id DESC");
			
			/*
			queryTotal = " SELECT COUNT(DISTINCT cf.contract_id) FROM contract_form cf WHERE cf.id IN ("
					+ " SELECT DISTINCT c.id FROM contract_form c, securable_object_acl soa, securable_object so "
					+ (filter != null ? ", contract_form_status cfs" : "")
					+ " WHERE soa.retired = :retired "
					+ " AND so.retired = :retired "
					+ " AND c.retired = :retired "
					+ " AND so.id = soa.securable_object_id "
					+ " AND soa.owner_class = :ownerClass AND soa.owner_id = :ownerId "
					// need to modify it if more permissions added in the future
					+ " AND (soa.permission = 'READ' OR soa.permission = 'ACCESS') "
					+ " AND so.object_class = :objectClass AND so.object_id = c.contract_id "
					+ (filter != null ? " AND cfs.id = (SELECT MAX(cfs.id) FROM contract_form_status cfs WHERE cfs.contract_form_id = c.id AND cfs.contract_status = :contractStatus) "
							: "")
					+ ((searchCriterias != null) ? " AND  (" + xpathWhereClause
							+ ") " : "") + ")";
			
			query = " SELECT cf.* FROM contract_form cf WHERE cf.id IN ("
					+ " SELECT DISTINCT c.id FROM contract_form c, securable_object_acl soa, securable_object so "
					+ (filter != null ? ", contract_form_status cfs" : "")
					+ " WHERE soa.retired = :retired "
					+ " AND so.retired = :retired "
					+ " AND c.retired = :retired "
					+ " AND so.id = soa.securable_object_id "
					+ " AND soa.owner_class = :ownerClass AND soa.owner_id = :ownerId "
					// need to modify it if more permissions added in the future
					+ " AND (soa.permission = 'READ' OR soa.permission = 'ACCESS') "
					+ " AND so.object_class = :objectClass AND so.object_id = c.contract_id "
					+ (filter != null ? " AND cfs.id = (SELECT MAX(cfs.id) FROM contract_form_status cfs WHERE cfs.contract_form_id = c.id AND cfs.contract_status = :contractStatus) "
							: "")
					+ ((searchCriterias != null) ? " AND  (" + xpathWhereClause
							+ ") " : "") + ") AND cf.contract_form_type = 'NEW_CONTRACT' ORDER BY cf.id DESC";
			*/
		}
		Query tq = getEntityManager().createNativeQuery(queryTotal);
		//tq.setHint("org.hibernate.cacheable", true);
		tq.setParameter("retired", Boolean.FALSE);
		if (!viewAllContract) {
			tq.setParameter("ownerClass", User.class);
			tq.setParameter("ownerId", user.getId());
			tq.setParameter("objectClass", Contract.class);
		}
		if (filter != null) {
			tq.setParameter("contractStatus", filter);
		}

		long total = Long.valueOf(tq.getSingleResult().toString());
		pagedList.setTotal(total);
		
		TypedQuery<ContractForm> q = (TypedQuery<ContractForm>) getEntityManager().createNativeQuery(query,
				ContractForm.class);
		q.setFirstResult(start).setMaxResults(limit);

		//q.setHint("org.hibernate.cacheable", true);
		q.setParameter("retired", Boolean.FALSE);
		if (!viewAllContract) {
			q.setParameter("ownerClass", User.class);
			q.setParameter("ownerId", user.getId());
			q.setParameter("objectClass", Contract.class);
		}
		if (filter != null) {
			q.setParameter("contractStatus", filter);
		}
		
		//#2599 make sure Amendments are showing up correctly in the list, might need to find a better way later ...
		List<ContractForm> resultList = q.getResultList();
		Set<Contract> contractList = Sets.newHashSet();
		List<ContractForm> amendmentList = Lists.newArrayList();
		
		for (ContractForm cf : resultList) {
			contractList.add(cf.getContract());
		}
		
		for (Contract c : contractList) {
			List<ContractForm> aList = this.listContractFormsByContractIdAndContractFormType(c.getId(), ContractFormType.AMENDMENT);
			
			if (aList != null && !aList.isEmpty()) {
				amendmentList.addAll(aList);
			}
		}
		
		if (amendmentList != null && !amendmentList.isEmpty()) {
			resultList.addAll(amendmentList);
		}

		pagedList.setList(resultList);
		return pagedList;
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

	public RawvalueLookupService getRawvalueLookupService() {
		return rawvalueLookupService;
	}

	@Autowired(required=true)
	public void setRawvalueLookupService(RawvalueLookupService rawvalueLookupService) {
		this.rawvalueLookupService = rawvalueLookupService;
	}

}
