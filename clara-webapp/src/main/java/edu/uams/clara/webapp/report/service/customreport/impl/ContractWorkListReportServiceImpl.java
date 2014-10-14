package edu.uams.clara.webapp.report.service.customreport.impl;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import edu.uams.clara.core.util.xml.XmlHandler;
import edu.uams.clara.core.util.xml.XmlHandlerFactory;
import edu.uams.clara.webapp.common.domain.usercontext.User;
import edu.uams.clara.webapp.common.service.form.FormService;
import edu.uams.clara.webapp.common.service.form.impl.FormServiceImpl.UserSearchField;
import edu.uams.clara.webapp.common.util.DateFormatUtil;
import edu.uams.clara.webapp.contract.dao.contractform.ContractFormDao;
import edu.uams.clara.webapp.contract.domain.contractform.ContractForm;
import edu.uams.clara.webapp.report.domain.ReportCriteria;
import edu.uams.clara.webapp.report.domain.ReportFieldTemplate;
import edu.uams.clara.webapp.report.domain.ReportTemplate;
import edu.uams.clara.webapp.report.service.customreport.CustomReportService;
import edu.uams.clara.webapp.xml.processor.XmlProcessor;

public class ContractWorkListReportServiceImpl extends CustomReportService {
	private final static Logger logger = LoggerFactory.getLogger(ContractWorkListReportServiceImpl.class);
	
	private ContractFormDao contractFormDao;
	
	private FormService formService;
	
	private XmlProcessor xmlProcessor;
	
	private final static String customReportQuery = " id AS contractFormId, meta_data_xml.value('(/contract/@identifier)[1]','varchar(20)') AS contractIdentifier, "
												+ " meta_data_xml.value('(/contract/status/text())[1]','varchar(20)') AS contractFormStatus, "
												+ " meta_data_xml.value('(/contract/staffs/staff/user[roles/role[fn:contains(fn:upper-case(.),\"PRINCIPAL INVESTIGATOR\")]]/lastname/text())[1]','varchar(50)')+ ',' + meta_data_xml.value('(/contract/staffs/staff/user[roles/role[fn:contains(fn:upper-case(.),\"PRINCIPAL INVESTIGATOR\")]]/firstname/text())[1]','varchar(50)') AS piName, "
												+ " meta_data_xml.value('(/contract/type/text())[1]','varchar(20)') AS contractType, "
												+ " meta_data_xml.value('(/contract/title/text())[1]','varchar(100)') AS contractDesc, "
												+ " meta_data_xml.value('(/contract/protocol/text())[1]','varchar(10)') AS protocolId ";
	
	private String getCommitteeNoteQuery(long contractFormId) {
		String query = "SELECT CAST(ROW_NUMBER() OVER( ORDER BY modified) AS VARCHAR(10)) + '. (' + CONVERT(VARCHAR(20), modified, 100) + ')' + ' Notes: ' + note AS committeeNote "
				+ " FROM contract_form_committee_status"
				+ " WHERE contract_form_id = "+ contractFormId +" AND retired = 0 AND note IS NOT NULL AND note <> ''"
				+ " ORDER BY modified";
		
		return query;
	}
	
	
	private String generateContractRawQeury(ReportTemplate reportTemplate, Map<String, String> fieldsRealXPathMap) {
		String rawQeury = "";
		
		try {
			XmlHandler xmlHandler = XmlHandlerFactory.newXmlHandler();
			
			//Document reportDoc = xmlHandler.loadXmlFileToDOM(resourceLoader.getResource(reportXml).getFile());
			
			Document reportDoc = getReportDoc();
			
			String reportIdentifier = reportTemplate.getTypeDescription();
			
			XPath xpathInstance = xmlHandler.newXPathInstance();
			
			Set<String> mapKeyList = fieldsRealXPathMap.keySet();
			
			NodeList reportConditions = (NodeList) xpathInstance
					.evaluate(
							"/reports/report[@type='"+ reportIdentifier +"']/queries/conditions/condition",
							reportDoc, XPathConstants.NODESET);
			
			List<String> conditionLst = Lists.newArrayList();
			
			for (int i = 0; i < reportConditions.getLength(); i++) {
				Element currentEl = (Element) reportConditions.item(i);

				if (mapKeyList.contains(currentEl.getAttribute("involve"))) {
					conditionLst.add(currentEl.getTextContent());
				}
			}
			
			String conditions = "";
			
			int c = conditionLst.size();
			
			int i = 0;
		
			for(String condition : conditionLst){
				conditions += condition;
				
				if(i != c - 1){
					conditions += " " + reportTemplate.getGlobalOperator().toString() + " ";
				}
				
				i ++;
			}
			
			xpathInstance.reset();
			
			Element reportMainQuery = (Element) xpathInstance
					.evaluate(
							"/reports/report[@type='"+ reportIdentifier +"']/queries/query[@type=\"main\"]",
							reportDoc, XPathConstants.NODE);
			
			rawQeury = reportMainQuery.getTextContent().replace("{conditions}", conditions);			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return rawQeury;
	}
	
	private Map<String, String> fieldToAliaMap = Maps.newHashMap();{
		fieldToAliaMap.put("contractid", "contractIdentifier");
		fieldToAliaMap.put("contractformstatus", "contractFormStatus");
		fieldToAliaMap.put("contractDesc", "contractDesc");
		fieldToAliaMap.put("contractType", "contractType");
		fieldToAliaMap.put("protocolid", "protocolId");
		fieldToAliaMap.put("piname", "piName");
	}

	@Override
	public String generateReportResult(ReportTemplate reportTemplate) {
		String finalResultXml = "<report-results>";
		finalResultXml += "<report-result id=\""+ reportTemplate.getTypeDescription() +"\"  created=\""+ DateFormatUtil.formateDateToMDY(new Date()) +"\">";
		finalResultXml += "<title>"+ reportTemplate.getDescription() +"</title>";
		
		finalResultXml += "<fields>";
		finalResultXml += "<field id=\"contractid\" desc=\"Contract #\" hidden=\"false\" />";
		finalResultXml += "<field id=\"contractformstatus\" desc=\"Current Status\" hidden=\"false\" />";
		finalResultXml += "<field id=\"piname\" desc=\"Principal Investigator\" hidden=\"false\" />";
		finalResultXml += "<field id=\"contracttype\" desc=\"Contract Type\" hidden=\"false\" />";
		finalResultXml += "<field id=\"contractentity\" desc=\"Contact Entity\" hidden=\"false\" />";
		finalResultXml += "<field id=\"contractdesc\" desc=\"Contact Description\" hidden=\"false\" />";
		finalResultXml += "<field id=\"studycontact\" desc=\"Study Contact\" hidden=\"false\" />";
		finalResultXml += "<field id=\"budgetmanager\" desc=\"Budget Manager\" hidden=\"false\" />";
		finalResultXml += "<field id=\"protocolid\" desc=\"IRB #\" hidden=\"false\" />";
		finalResultXml += "<field id=\"committeenote\" desc=\"Notes\" hidden=\"false\" />";
		finalResultXml += "</fields>";
		
		List<ReportCriteria> criterias = reportTemplate.getReportCriterias();
		
		ObjectMapper objectMapper = new ObjectMapper();
		
		Map<String, String> fieldsRealXPathMap = Maps.newHashMap();
		
		for (ReportCriteria rc : criterias) {
			ReportFieldTemplate reportField = new ReportFieldTemplate();
			
			try {
				reportField = objectMapper.readValue(rc.getCriteria(), ReportFieldTemplate.class);
				
				String fieldIdentifier = reportField.getFieldIdentifier();
				
				String value = reportField.getValue();
				
				if (value != null && !value.isEmpty()) {
					String realXpath = "";
					
					if (reportField.getNodeXPath().contains(".exist") || reportField.getNodeXPath().contains(".value")) {
						realXpath = reportField.getNodeXPath().replace("{value}", "\""+ value.toUpperCase() +"\"");
					} else {
						realXpath = reportField.getNodeXPath().replace("{value}", "'"+ value.toUpperCase() +"'");
					}
					
					realXpath = realXpath.replace("{operator}", reportField.getOperator().getRealOperator());
					
					fieldsRealXPathMap.put("{" + fieldIdentifier + ".search-xpath}" , realXpath);
					//fieldsRealXPathMap.put("{" + fieldIdentifier + ".report-xpath}", reportField.getReportableXPath());
				}
			} catch (JsonParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JsonMappingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		String rawQeury = this.generateContractRawQeury(reportTemplate, fieldsRealXPathMap);
		
		rawQeury = rawQeury.replace("{customreportstatment}", customReportQuery);
		
		String realQeury = fillMessage(rawQeury, fieldsRealXPathMap);

		logger.debug("real query: " + realQeury);
		
		finalResultXml += "<report-items>";
		
		List resultObjectLst = getReportResultDao().generateResult(realQeury);
		
		for (Object resultObject: resultObjectLst) {
			finalResultXml += "<report-item>";
			
			Map row = (Map) resultObject;
			
			long contractFormId = Long.valueOf(row.get("contractFormId").toString());
			
			ContractForm contractForm = contractFormDao.findById(contractFormId);
			
			String contractFormMetaData = contractForm.getMetaDataXml();
			
			for (Entry<String, String> entry : fieldToAliaMap.entrySet()) {
				finalResultXml += "<field id=\""+ entry.getKey() +"\">";
				
				finalResultXml += "<![CDATA[" + row.get(entry.getValue()) + "]]>";
				
				finalResultXml += "</field>";
			}
			
			//get study contact list
			try {
				List<User> studyContactUsers = formService.getUsersByKeywordAndSearchField("Study Coordinator", contractFormMetaData, UserSearchField.ROLE);
				
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
			
			//get contract entity list
			try {
				Document doc = xmlProcessor.loadXmlStringToDOM(contractFormMetaData);
				
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
			
			//get committee note list
			finalResultXml += "<field id=\"committeenote\">";
			
			finalResultXml += "<list>";
			
			List committeeNoteLst = getReportResultDao().generateResult(this.getCommitteeNoteQuery(contractFormId));
			
			for (Object committeeNoteObject: committeeNoteLst) {
				Map noteRow = (Map) committeeNoteObject;
				
				finalResultXml += "<item>" + noteRow.get("committeeNote") + "</item>";
			}
			
			finalResultXml += "</list>";
			finalResultXml += "</field>";
			
			finalResultXml += "</report-item>";
		}
		
		finalResultXml += "</report-items>";
		finalResultXml += "</report-result>";
		
		if(finalResultXml.contains("&")){
			finalResultXml=finalResultXml.replaceAll("&", "&amp;");
		}
		return finalResultXml;
	}

	public ContractFormDao getContractFormDao() {
		return contractFormDao;
	}
	
	@Autowired(required = true)
	public void setContractFormDao(ContractFormDao contractFormDao) {
		this.contractFormDao = contractFormDao;
	}

	public FormService getFormService() {
		return formService;
	}
	
	@Autowired(required = true)
	public void setFormService(FormService formService) {
		this.formService = formService;
	}

	public XmlProcessor getXmlProcessor() {
		return xmlProcessor;
	}
	
	@Autowired(required = true)
	public void setXmlProcessor(XmlProcessor xmlProcessor) {
		this.xmlProcessor = xmlProcessor;
	}

}
