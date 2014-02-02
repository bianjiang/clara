package edu.uams.clara.webapp.protocol.web.protocolform.review.checklist.ajax;

import java.io.FileNotFoundException;
import java.io.IOException;
import javax.xml.xpath.XPathExpressionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.xml.sax.SAXException;

import edu.uams.clara.webapp.common.domain.usercontext.enums.Committee;
import edu.uams.clara.webapp.common.util.XMLResponseHelper;
import edu.uams.clara.webapp.protocol.dao.businesslogicobject.ProtocolFormCommitteeChecklistXmlDataDao;
import edu.uams.clara.webapp.protocol.dao.businesslogicobject.ProtocolFormCommitteeStatusDao;
import edu.uams.clara.webapp.protocol.dao.protocolform.ProtocolFormDao;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.ProtocolFormCommitteeChecklistXmlData;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.ProtocolFormCommitteeStatus;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.enums.ChecklistQuestionAnswer;
import edu.uams.clara.webapp.protocol.domain.protocolform.enums.ProtocolFormType;
import edu.uams.clara.webapp.xml.processor.XmlProcessor;

@Controller
public class ProtocolFormCommitteeChecklistAjaxController {

	private final static Logger logger = LoggerFactory
	.getLogger(ProtocolFormCommitteeChecklistAjaxController.class);
		
	private ProtocolFormDao protocolFormDao;
	
	private ProtocolFormCommitteeChecklistXmlDataDao protocolFormCommitteeChecklistXmlDataDao;
	
	private ProtocolFormCommitteeStatusDao protocolFormCommitteeStatusDao;
	
	private ResourceLoader resourceLoader;
	
	@Value("${checklistXmlTemplate.url}")
	private String checklistXmlTemplatePath;
	
	private XmlProcessor xmlProcessor;

	@RequestMapping(value = "/ajax/protocols/{protocolId}/protocol-forms/{protocolFormId}/review/checklists/committee-checklist.xml", method = RequestMethod.GET)
	public @ResponseBody String getFormCommitteeChecklist(
			@PathVariable("protocolFormId") long protocolFormId,
			@RequestParam("committee") Committee committee,
			@RequestParam("formType") ProtocolFormType protocolFormType) throws FileNotFoundException, IOException, XPathExpressionException, SAXException{
		/*
		ProtocolForm protocolForm = protocolFormDao.findById(protocolFormId);
		
		if(!protocolForm.getProtocolFormType().equals(protocolFormType)){
			throw new InconsistentStateException("protocolFormId: " + protocolFormId + "; dbProtocolFormType: " + protocolForm.getProtocolFormType() + "; incoming protocolFormType: " + protocolFormType);
		}
		
		ProtocolFormCommitteeChecklistXmlData protocolFormCommitteeChecklistXmlData = null;
		try{
			protocolFormCommitteeChecklistXmlData = protocolFormCommitteeChecklistXmlDataDao.findLatestByProtocolFormIdAndCommittee(protocolFormId, committee);
			//logger.debug(protocolFormCommitteeChecklistXmlData.getXmlData().toString());
		}catch(Exception ex){
			
			protocolFormCommitteeChecklistXmlData = new ProtocolFormCommitteeChecklistXmlData();
			protocolFormCommitteeChecklistXmlData.setProtocolForm(protocolForm);
			protocolFormCommitteeChecklistXmlData.setCommittee(committee);
			protocolFormCommitteeChecklistXmlData.setCreated(new Date());
			protocolFormCommitteeChecklistXmlData.setLocked(false);
			protocolFormCommitteeChecklistXmlData.setParent(protocolFormCommitteeChecklistXmlData);			
			
			Resource  checklistXmlFileResource = resourceLoader.getResource(checklistXmlTemplatePath);

			String checklistXmlString = xmlProcessor.loadXmlFile(checklistXmlFileResource.getFile());
			
			//logger.debug(checklistXmlString);
			protocolFormCommitteeChecklistXmlData.setXmlData(xmlProcessor.listElementsByPath("/checklists/checklist-group[@committee='" + committee.toString() + "' and @protocol-form-type='" + protocolFormType.toString() + "']", checklistXmlString, false));
			
			protocolFormCommitteeChecklistXmlData = protocolFormCommitteeChecklistXmlDataDao.saveOrUpdate(protocolFormCommitteeChecklistXmlData);
			
		}

		return protocolFormCommitteeChecklistXmlData.getXmlData();
		*/
		
		try{
			ProtocolFormCommitteeStatus protocolFormCommitteeStatus = protocolFormCommitteeStatusDao.getLatestByCommitteeAndProtocolFormId(committee, protocolFormId);
			
			Resource  checklistXmlFileResource = resourceLoader.getResource(checklistXmlTemplatePath);
			
			//Document checkListDoc = xmlProcessor.loadXmlFileToDOM(checklistXmlFileResource.getFile());
			
			String checkListXml = xmlProcessor.loadXmlFile(checklistXmlFileResource.getFile());
			
			String path = "/checklists/checklist-group[conditions/condition[@protocol-form-type=\""+ protocolFormType.toString() +"\" and @committee-name=\""+ committee.toString() +"\" and @protocol-form-committee-status=\""+ protocolFormCommitteeStatus.getProtocolFormCommitteeStatus().toString() +"\"]]";
			
			//Element checkListGroupEl = (Element) xPath.evaluate(path,
					//checkListDoc, XPathConstants.NODE);
			
			String checkListXmlString = xmlProcessor.listElementsByPath(path, checkListXml, false);
			
			if (checkListXmlString != null && !checkListXmlString.isEmpty()){
				return XMLResponseHelper.xmlResult(Boolean.TRUE);
			} else {
				return XMLResponseHelper.xmlResult(Boolean.FALSE);
			}

		}catch(Exception ex){
			ex.printStackTrace();
			
			return XMLResponseHelper.xmlResult(Boolean.FALSE);
		}

	}
	
	
	@RequestMapping(value = "/ajax/protocols/{id}/protocol-forms/{formId}/review/checklists/{checklistId}/questions/{questionId}/answers/save", method = RequestMethod.POST)
	public @ResponseBody String saveChecklistQuestionAnswer(
			@PathVariable("id") long protocolFormId,
			@PathVariable("checklistId") long checklistId,
			@PathVariable("questionId") long questionId,
			@RequestParam("committee") Committee committee,
			@RequestParam("formType") ProtocolFormType protocolFormType,
			@RequestParam("answer") ChecklistQuestionAnswer checklistQuestionAnswer) throws FileNotFoundException, IOException, XPathExpressionException, SAXException{
		
		//exception should not happen
		ProtocolFormCommitteeChecklistXmlData protocolFormCommitteeChecklistXmlData = protocolFormCommitteeChecklistXmlDataDao.findLatestByProtocolFormIdAndCommittee(protocolFormId, committee);
		
		String xmlData = xmlProcessor.replaceOrAddNodeValueByPath("/checklists/checklist-group[@committee='" + committee.toString() + "' and @protocol-form-type='" + protocolFormType.toString() + "']/checklist[@id='" + checklistId + "']/question[@id='" + questionId + "']/answer", protocolFormCommitteeChecklistXmlData.getXmlData(), checklistQuestionAnswer.toString());

		protocolFormCommitteeChecklistXmlData.setXmlData(xmlData);
		
		protocolFormCommitteeChecklistXmlData = protocolFormCommitteeChecklistXmlDataDao.saveOrUpdate(protocolFormCommitteeChecklistXmlData);
		
		return XMLResponseHelper.xmlResult(Boolean.TRUE);
	}
	
	
	@Autowired(required=true)
	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}
	
	public ResourceLoader getResourceLoader() {
		return resourceLoader;
	}

	@Autowired(required=true)
	public void setXmlProcessor(XmlProcessor xmlProcessor) {
		this.xmlProcessor = xmlProcessor;
	}

	public XmlProcessor getXmlProcessor() {
		return xmlProcessor;
	}

	@Autowired(required=true)
	public void setProtocolFormDao(ProtocolFormDao protocolFormDao) {
		this.protocolFormDao = protocolFormDao;
	}

	public ProtocolFormDao getProtocolFormDao() {
		return protocolFormDao;
	}

	@Autowired(required=true)
	public void setProtocolFormCommitteeChecklistXmlDataDao(
			ProtocolFormCommitteeChecklistXmlDataDao protocolFormCommitteeChecklistXmlDataDao) {
		this.protocolFormCommitteeChecklistXmlDataDao = protocolFormCommitteeChecklistXmlDataDao;
	}

	public ProtocolFormCommitteeChecklistXmlDataDao getProtocolFormCommitteeChecklistXmlDataDao() {
		return protocolFormCommitteeChecklistXmlDataDao;
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
