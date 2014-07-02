package edu.uams.clara.webapp.protocol.service.protocolform;

import java.util.HashMap;
import java.util.Map;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.google.common.collect.Maps;

import edu.uams.clara.webapp.common.domain.usercontext.enums.Committee;
import edu.uams.clara.webapp.protocol.dao.businesslogicobject.ProtocolFormCommitteeStatusDao;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.ProtocolFormCommitteeStatus;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolForm;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolFormXmlData;
import edu.uams.clara.webapp.xml.processor.XmlProcessor;


public abstract class ProtocolFormReviewLogicService {
	
	private final static Logger logger = LoggerFactory
			.getLogger(ProtocolFormReviewLogicService.class);
	
	private XmlProcessor xmlProcessor;
	
	private ProtocolFormCommitteeStatusDao protocolFormCommitteeStatusDao;
	
	private Map<String, String> isInvolvedByTypeRules = new HashMap<String, String>();
	{
		isInvolvedByTypeRules.put("Budget", "boolean(count(/protocol/need-budget[text()='y'])>0)");
		isInvolvedByTypeRules.put("Contract", "boolean(count(/protocol/contract/have-new-contract[text()='y'])>0)");
		isInvolvedByTypeRules.put("ContractAmendment", "boolean(count(/protocol/modification/notify-contract[text()='y'])>0)");
		//isInvolvedByTypeRules.put("Biosafety", "not(count(/protocol/misc/biosafety/bio-hazard-materials/material[text()='na'])>0)");
		isInvolvedByTypeRules.put("Biosafety", "boolean(count(/protocol/misc/biosafety/bio-hazard-materials/material)>0 and count(/protocol/misc/biosafety/bio-hazard-materials/material[text()='na'])=0)");
		isInvolvedByTypeRules
				.put("Radiation","boolean(count(/protocol/misc/radiation-safety/involve-the-use-of-radiation/y/exceed-standard-of-care[text()='y'])>0)");
		isInvolvedByTypeRules.put("PRMC", "boolean(count(/protocol/misc/is-cancer-study[text()='y'])>0)");
		//isInvolvedByTypeRules.put("Drug", "boolean(count(/protocol/drugs/drug[@type[.=\"investigational\"]])>0)");
		isInvolvedByTypeRules.put("Drug", "boolean(count(/protocol/drugs/drug)>0)");
		isInvolvedByTypeRules.put("Device", "boolean(count(/protocol/devices/device)>0)");
		isInvolvedByTypeRules.put("ClinicalTrials", "boolean(count(/protocol/misc/is-registered-at-clinical-trials[text()='y'])>0 and count(/protocol/study-type[text()='industry-sponsored'])=0)");
		
		isInvolvedByTypeRules.put("BudgetModification", "boolean(count(/protocol/modification/require-budget-review[text()='y'])>0 and count(/protocol/crimson/has-budget[text()='yes'])=0)");
		isInvolvedByTypeRules.put("ComplianceModification", "boolean(count(/protocol/modification/to-modify-section/submit-to-medicare[text()='y'])>0)");
		isInvolvedByTypeRules.put("RegulatoryModification", "boolean(count(/protocol/modification/to-modify-section/conduct-under-uams[text()='y'])>0)");
		isInvolvedByTypeRules.put("LegalModification", "boolean(count(/protocol/modification/to-modify-section/involve-change-in/contract-modified[text()='y'])>0)");
	}

	public boolean isInvolvedByType(ProtocolForm protocolForm, String type) {

		ProtocolFormXmlData protocolFormXmlData = protocolForm
				.getTypedProtocolFormXmlDatas().get(
						protocolForm.getProtocolFormType()
								.getDefaultProtocolFormXmlDataType());

		String xPath = isInvolvedByTypeRules.get(type);

		if (xPath == null) {
			return false;
		}		

		try {
			Document assertXml = xmlProcessor.loadXmlStringToDOM(protocolFormXmlData.getXmlData());
			
			XPath xpathInstance = xmlProcessor.getXPathInstance();
			
			Boolean isInvovled = (Boolean) (xpathInstance
					.evaluate(
							xPath,
							assertXml, XPathConstants.BOOLEAN));
			
			return (isInvovled != null && isInvovled);
				
			
		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}
	
	public Map<String, String> getAssignedCommittee(ProtocolForm protocolForm) {
		Map<String, String> assignedCommitteeMap = Maps.newHashMap();
		
		String protocolFormMetaData = protocolForm.getMetaDataXml();
		
		try {
			Document doc = getXmlProcessor().loadXmlStringToDOM(protocolFormMetaData);
			
			XPath xpath = getXmlProcessor().getXPathInstance();
			
			NodeList assignedCommitteeNLst = (NodeList) xpath.evaluate("/"
											+ protocolForm.getProtocolFormType()
													.getBaseTag()
											+ "/assigned-committee/individual-committee", doc, XPathConstants.NODESET);
			
			if (assignedCommitteeNLst != null && assignedCommitteeNLst.getLength() > 0) {
				for (int i = 0; i < assignedCommitteeNLst.getLength(); i++){
					Element currentEl = (Element) assignedCommitteeNLst.item(i);
					
					String committeeName = currentEl.getAttribute("name");
					
					String committeeStatus = "";
					
					try {
						ProtocolFormCommitteeStatus pfcs = getProtocolFormCommitteeStatusDao().getLatestByCommitteeAndProtocolFormId(Committee.valueOf(committeeName), protocolForm.getId());
						
						if (committeeName.equals("REGULATORY_MANAGER")){
							committeeName = "MONITORING_REGULATORY_QA";
							
							try {
								ProtocolFormCommitteeStatus monitoringPfcs = getProtocolFormCommitteeStatusDao().getLatestByCommitteeAndProtocolFormId(Committee.valueOf(committeeName), protocolForm.getId());
								
								committeeStatus = monitoringPfcs.getProtocolFormCommitteeStatus().getDescription();
							} catch (Exception e) {
								committeeStatus = pfcs.getProtocolFormCommitteeStatus().getDescription();
							}
						} else {
							committeeStatus = pfcs.getProtocolFormCommitteeStatus().getDescription();
						}
					} catch (Exception ex) {
						ex.printStackTrace();
						committeeStatus = "";
					}
					
					assignedCommitteeMap.put(committeeName, committeeStatus);
					
				}
			}
		} catch (Exception e){
			//don't care
		}
		
		return assignedCommitteeMap;
	}
	
	public abstract String getExtraContent(long protocolFormId, String reviewFormIdentifier);

	public XmlProcessor getXmlProcessor() {
		return xmlProcessor;
	}
	
	@Autowired(required=true)
	public void setXmlProcessor(XmlProcessor xmlProcessor) {
		this.xmlProcessor = xmlProcessor;
	}

	public ProtocolFormCommitteeStatusDao getProtocolFormCommitteeStatusDao() {
		return protocolFormCommitteeStatusDao;
	}
	
	@Autowired(required=true)
	public void setProtocolFormCommitteeStatusDao(
			ProtocolFormCommitteeStatusDao protocolFormCommitteeStatusDao) {
		this.protocolFormCommitteeStatusDao = protocolFormCommitteeStatusDao;
	}
}
