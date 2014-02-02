package edu.uams.clara.webapp.report.service.customreport.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.google.common.collect.Maps;

import edu.uams.clara.webapp.common.domain.usercontext.User;
import edu.uams.clara.webapp.common.service.form.FormService;
import edu.uams.clara.webapp.common.service.form.impl.FormServiceImpl.UserSearchField;
import edu.uams.clara.webapp.common.util.DateFormatUtil;
import edu.uams.clara.webapp.contract.dao.ContractDao;
import edu.uams.clara.webapp.contract.domain.Contract;
import edu.uams.clara.webapp.report.domain.ReportTemplate;
import edu.uams.clara.webapp.report.service.customreport.CustomReportService;
import edu.uams.clara.webapp.xml.processor.XmlProcessor;

public class ContractOverDueReportServiceImpl extends CustomReportService {
	private final static Logger logger = LoggerFactory.getLogger(ContractOverDueReportServiceImpl.class);
	
	private FormService formService;
	
	private XmlProcessor xmlProcessor;
	
	private ContractDao contractDao;
	
	private String generateQuery(String contractType, long days) {
		String query = "SELECT id AS contractId, contract_identifier AS contractIdentifier, meta_data_xml.value('(/contract/status/text())[1]','varchar(50)') AS contractStatus, tempContract.tempDays AS daysInProcess, "
				+ " meta_data_xml.value('(/contract/protocol/text())[1]','varchar(10)') AS protocolId, "
				+ " meta_data_xml.value('(/contract/staffs/staff/user[roles/role = \"Principal Investigator\"]/lastname)[1]','varchar(20)') + ',' + meta_data_xml.value('(/contract/staffs/staff/user[roles/role = \"Principal Investigator\"]/firstname)[1]','varchar(20)') as PI "
				+ " FROM contract, "
				+ " (SELECT DISTINCT contract_id as tempContractId, tempContractForm.days as tempDays FROM contract_form, ( "
				+ " SELECT startpoint.startContractFormId as tempContractFormId,  DATEDIFF(D,startpoint.startModified,endpoint.endModified) as days FROM "
				+ " (SELECT contract_form_id as startContractFormId, modified as startModified FROM contract_form_committee_status WHERE retired = 0 "
				+ " AND contract_form_committee_status = 'REVIEWER_ASSIGNED') startpoint, "
				+ " (SELECT contract_form_id as endContractFormId, modified as endModified FROM contract_form_status WHERE retired = 0 AND id IN ( "
				+ " SELECT MAX(id) FROM contract_form_status WHERE retired = 0 "
				+ " AND contract_form_id IN ( "
				+ " SELECT DISTINCT contract_form_id FROM contract_form_committee_status WHERE retired = 0 "
				+ " AND contract_form_committee_status = 'REVIEWER_ASSIGNED') "
				+ " GROUP BY contract_form_id) "
				+ " AND contract_form_status <> 'CONTRACT_EXECUTED') endpoint "
				+ " WHERE startpoint.startContractFormId = endpoint.endContractFormId "
				+ " and DATEDIFF(D,startpoint.startModified,endpoint.endModified) > "+ days +") tempContractForm "
				+ " WHERE retired = 0 "
				+ " AND id = tempContractForm.tempContractFormId) tempContract "
				+ " WHERE retired = 0 "
				+ " AND id = tempContract.tempContractId "
				+ " AND meta_data_xml.exist('/contract/type[text()=\""+ contractType +"\"]')=1";
		
		return query;
	}
	
	private Map<String, String> fieldToAliaMap = Maps.newHashMap();{
		fieldToAliaMap.put("contractid", "contractIdentifier");
		fieldToAliaMap.put("contractstatus", "contractStatus");
		fieldToAliaMap.put("daysinprocess", "daysInProcess");
		fieldToAliaMap.put("protocolid", "protocolId");
		fieldToAliaMap.put("piname", "PI");
	}
	
	private String generateTable(ReportTemplate reportTemplate, List resultLst, String tableTitle){
		String finalResultXml = "<report-result id=\""+ reportTemplate.getTypeDescription() +"\" created=\""+ DateFormatUtil.formateDateToMDY(new Date()) +"\">";
		finalResultXml += "<title>"+ tableTitle +"</title>";
		
		finalResultXml += "<fields>";
		finalResultXml += "<field id=\"contractid\" desc=\"Contract #\" hidden=\"false\" />";
		finalResultXml += "<field id=\"contracttype\" desc=\"Contract Type\" hidden=\"false\" />";
		finalResultXml += "<field id=\"contractstatus\" desc=\"Contract Status\" hidden=\"false\" />";
		finalResultXml += "<field id=\"daysinprocess\" desc=\"Days In Process\" hidden=\"false\" />";
		finalResultXml += "<field id=\"protocolid\" desc=\"IRB #\" hidden=\"false\" />";
		finalResultXml += "<field id=\"legalreviewer\" desc=\"Legal Reviewer\" hidden=\"false\" />";
		finalResultXml += "<field id=\"contractadmin\" desc=\"Contract Administrator\" hidden=\"false\" />";
		finalResultXml += "<field id=\"piname\" desc=\"PI\" hidden=\"false\" />";
		finalResultXml += "<field id=\"studycontact\" desc=\"Study Contact\" hidden=\"false\" />";
		finalResultXml += "<field id=\"budgetmanager\" desc=\"Budget Manager\" hidden=\"false\" />";
		finalResultXml += "<field id=\"contractentity\" desc=\"Contact Entity\" hidden=\"false\" />";
		finalResultXml += "</fields>";
		
		finalResultXml += "<report-items>";
		for (Object resultObject: resultLst) {
			finalResultXml += "<report-item>";
			
			Map row = (Map) resultObject;
			
			long contractId = Long.valueOf(row.get("contractId").toString());
			
			Contract contract = contractDao.findById(contractId);
			
			String contractMetaData = contract.getMetaDataXml();
			
			for (Entry<String, String> entry : fieldToAliaMap.entrySet()) {
				finalResultXml += "<field id=\""+ entry.getKey() +"\">";
				
				finalResultXml += "<![CDATA[" + row.get(entry.getValue()) + "]]>";
				
				finalResultXml += "</field>";
			}
			
			finalResultXml += "<field id=\"contracttype\">Confidential Disclosure Agreement (CDA)</field>";
			
			try {
				List<User> studyContactUsers = formService.getUsersByKeywordAndSearchField("Study Coordinator", contractMetaData, UserSearchField.ROLE);
				
				finalResultXml += "<field id=\"studycontact\">";
				finalResultXml += "<list>";
				
				for (User scUser : studyContactUsers) {
					finalResultXml += "<item>"+ scUser.getPerson().getFullname() +"</item>";
				}
				finalResultXml += "</list>";
				finalResultXml += "</field>";
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			try {
				List<String> legalReviewerLst = xmlProcessor.getAttributeValuesByPathAndAttributeName("/contract/committee-review/committee/assigned-reviewers/assigned-reviewer[@user-role=\"ROLE_CONTRACT_LEGAL_REVIEW\"]", contractMetaData, "user-fullname");
				
				finalResultXml += "<field id=\"legalreviewer\">";
				finalResultXml += "<list>";
				
				for (String legalReviewer : legalReviewerLst) {
					finalResultXml += "<item>"+ legalReviewer +"</item>";
				}
				finalResultXml += "</list>";
				finalResultXml += "</field>";
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			try {
				List<String> contractAdminLst = xmlProcessor.getAttributeValuesByPathAndAttributeName("/contract/committee-review/committee/assigned-reviewers/assigned-reviewer[@user-role=\"ROLE_CONTRACT_ADMIN\"]", contractMetaData, "user-fullname");
				
				finalResultXml += "<field id=\"contractadmin\">";
				finalResultXml += "<list>";
				
				for (String contractAdmin : contractAdminLst) {
					finalResultXml += "<item>"+ contractAdmin +"</item>";
				}
				finalResultXml += "</list>";
				finalResultXml += "</field>";
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			try {
				Document doc = xmlProcessor.loadXmlStringToDOM(contractMetaData);
				
				NodeList entityNodeLst = doc.getElementsByTagName("sponsor");
				
				finalResultXml += "<field id=\"contractentity\">";
				finalResultXml += "<list>";

				if (entityNodeLst != null && entityNodeLst.getLength() > 0) {
					for (int i = 0; i < entityNodeLst.getLength(); i++) {
						Element currentEl = (Element) entityNodeLst.item(i);
						
						String companyName = currentEl.getElementsByTagName("company").item(0).getTextContent();
						
						String contactName = currentEl.getElementsByTagName("name").item(0).getTextContent();
						
						finalResultXml += "<item>"+ companyName + "("+ contactName +")" + "</item>";
					}
				}
				
				finalResultXml += "</list>";
				finalResultXml += "</field>";
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			finalResultXml += "</report-item>";

		}
		
		finalResultXml += "</report-items>";
		finalResultXml += "</report-result>";
		
		return finalResultXml;
	}
	
	@Override
	public String generateReportResult(ReportTemplate reportTemplate) {
		String finalResultXml = "<report-results>";
		
		List overDueCDALst = getReportResultDao().generateResult(generateQuery("confidentiality-disclosure-agreement", 21));
		finalResultXml += generateTable(reportTemplate, overDueCDALst, "CDA: In process greater than 21 days.");
		
		List overDueCTALst = getReportResultDao().generateResult(generateQuery("clinical-trial-agreement", 90));
		finalResultXml += generateTable(reportTemplate, overDueCTALst, "CTA: In process greater than 90 days.");
		
		finalResultXml += "</report-results>";
		
		logger.debug("finalXml: " + finalResultXml);
		return finalResultXml;
	}

	public ContractDao getContractDao() {
		return contractDao;
	}
	
	@Autowired(required = true)
	public void setContractDao(ContractDao contractDao) {
		this.contractDao = contractDao;
	}

	public XmlProcessor getXmlProcessor() {
		return xmlProcessor;
	}
	
	@Autowired(required = true)
	public void setXmlProcessor(XmlProcessor xmlProcessor) {
		this.xmlProcessor = xmlProcessor;
	}

	public FormService getFormService() {
		return formService;
	}
	
	@Autowired(required = true)
	public void setFormService(FormService formService) {
		this.formService = formService;
	}
}
