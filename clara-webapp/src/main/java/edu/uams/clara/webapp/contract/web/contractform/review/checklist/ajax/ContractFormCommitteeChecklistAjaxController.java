package edu.uams.clara.webapp.contract.web.contractform.review.checklist.ajax;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;

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
import edu.uams.clara.webapp.common.exception.InconsistentStateException;
import edu.uams.clara.webapp.common.util.XMLResponseHelper;
import edu.uams.clara.webapp.contract.dao.businesslogicobject.ContractFormCommitteeChecklistXmlDataDao;
import edu.uams.clara.webapp.contract.dao.contractform.ContractFormDao;
import edu.uams.clara.webapp.contract.domain.businesslogicobject.ContractFormCommitteeChecklistXmlData;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.enums.ChecklistQuestionAnswer;
import edu.uams.clara.webapp.contract.domain.contractform.ContractForm;
import edu.uams.clara.webapp.contract.domain.contractform.enums.ContractFormType;
import edu.uams.clara.webapp.xml.processor.XmlProcessor;

@Controller
public class ContractFormCommitteeChecklistAjaxController {

	private final static Logger logger = LoggerFactory
	.getLogger(ContractFormCommitteeChecklistAjaxController.class);
		
	private ContractFormDao contractFormDao;
	
	private ContractFormCommitteeChecklistXmlDataDao contractFormCommitteeChecklistXmlDataDao;
	
	private ResourceLoader resourceLoader;
	
	@Value("${checklistXmlTemplate.url}")
	private String checklistXmlTemplatePath;
	
	private XmlProcessor xmlProcessor;

	@RequestMapping(value = "/ajax/contracts/{contractId}/contract-forms/{contractFormId}/review/checklists/committee-checklist.xml", method = RequestMethod.GET)
	public @ResponseBody String getFormCommitteeChecklist(
			@PathVariable("contractFormId") long contractFormId,
			@RequestParam("committee") Committee committee,
			@RequestParam("formType") ContractFormType contractFormType) throws FileNotFoundException, IOException, XPathExpressionException, SAXException{
		
		ContractForm contractForm = contractFormDao.findById(contractFormId);
		
		if(!contractForm.getContractFormType().equals(contractFormType)){
			throw new InconsistentStateException("contractFormId: " + contractFormId + "; dbContractFormType: " + contractForm.getContractFormType() + "; incoming contractFormType: " + contractFormType);
		}
		
		ContractFormCommitteeChecklistXmlData contractFormCommitteeChecklistXmlData = null;
		try{
			contractFormCommitteeChecklistXmlData = contractFormCommitteeChecklistXmlDataDao.findLatestByContractFormIdAndCommittee(contractFormId, committee);
			//logger.debug(contractFormCommitteeChecklistXmlData.getXmlData().toString());
		}catch(Exception ex){
			
			contractFormCommitteeChecklistXmlData = new ContractFormCommitteeChecklistXmlData();
			contractFormCommitteeChecklistXmlData.setContractForm(contractForm);
			contractFormCommitteeChecklistXmlData.setCommittee(committee);
			contractFormCommitteeChecklistXmlData.setCreated(new Date());
			contractFormCommitteeChecklistXmlData.setLocked(false);
			contractFormCommitteeChecklistXmlData.setParent(contractFormCommitteeChecklistXmlData);			
			
			Resource  checklistXmlFileResource = resourceLoader.getResource(checklistXmlTemplatePath);

			String checklistXmlString = xmlProcessor.loadXmlFile(checklistXmlFileResource.getFile());
			
			//logger.debug(checklistXmlString);
			contractFormCommitteeChecklistXmlData.setXmlData(xmlProcessor.listElementsByPath("/checklists/checklist-group[@committee='" + committee.toString() + "' and @contract-form-type='" + contractFormType.toString() + "']", checklistXmlString, false));
			
			contractFormCommitteeChecklistXmlData = contractFormCommitteeChecklistXmlDataDao.saveOrUpdate(contractFormCommitteeChecklistXmlData);
			
		}

		return contractFormCommitteeChecklistXmlData.getXmlData();

	}
	
	
	@RequestMapping(value = "/ajax/contracts/{contractId}/contract-forms/{contractFormId}/review/checklists/{checklistId}/questions/{questionId}/answers/save", method = RequestMethod.POST)
	public @ResponseBody String saveChecklistQuestionAnswer(
			@PathVariable("contractFormId") long contractFormId,
			@PathVariable("checklistId") long checklistId,
			@PathVariable("questionId") long questionId,
			@RequestParam("committee") Committee committee,
			@RequestParam("contractFormType") ContractFormType contractFormType,
			@RequestParam("answer") ChecklistQuestionAnswer checklistQuestionAnswer) throws FileNotFoundException, IOException, XPathExpressionException, SAXException{
		
		//exception should not happen
		ContractFormCommitteeChecklistXmlData contractFormCommitteeChecklistXmlData = contractFormCommitteeChecklistXmlDataDao.findLatestByContractFormIdAndCommittee(contractFormId, committee);
		
		String xmlData = xmlProcessor.replaceOrAddNodeValueByPath("/checklists/checklist-group[@committee='" + committee.toString() + "' and @contract-form-type='" + contractFormType.toString() + "']/checklist[@id='" + checklistId + "']/question[@id='" + questionId + "']/answer", contractFormCommitteeChecklistXmlData.getXmlData(), checklistQuestionAnswer.toString());

		contractFormCommitteeChecklistXmlData.setXmlData(xmlData);
		
		contractFormCommitteeChecklistXmlData = contractFormCommitteeChecklistXmlDataDao.saveOrUpdate(contractFormCommitteeChecklistXmlData);
		
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
	public void setContractFormDao(ContractFormDao contractFormDao) {
		this.contractFormDao = contractFormDao;
	}

	public ContractFormDao getContractFormDao() {
		return contractFormDao;
	}

	@Autowired(required=true)
	public void setContractFormCommitteeChecklistXmlDataDao(
			ContractFormCommitteeChecklistXmlDataDao contractFormCommitteeChecklistXmlDataDao) {
		this.contractFormCommitteeChecklistXmlDataDao = contractFormCommitteeChecklistXmlDataDao;
	}

	public ContractFormCommitteeChecklistXmlDataDao getContractFormCommitteeChecklistXmlDataDao() {
		return contractFormCommitteeChecklistXmlDataDao;
	}
	
}
