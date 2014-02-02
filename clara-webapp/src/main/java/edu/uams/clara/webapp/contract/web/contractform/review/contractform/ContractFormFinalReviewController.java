package edu.uams.clara.webapp.contract.web.contractform.review.contractform;


import java.io.IOException;

import javax.xml.xpath.XPathExpressionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.xml.sax.SAXException;

import edu.uams.clara.webapp.common.businesslogic.BusinessObjectStatusHelperContainer;
import edu.uams.clara.webapp.common.domain.usercontext.User;
import edu.uams.clara.webapp.common.domain.usercontext.enums.Committee;
import edu.uams.clara.webapp.contract.dao.ContractDao;
import edu.uams.clara.webapp.contract.dao.contractform.ContractFormDao;
import edu.uams.clara.webapp.contract.dao.contractform.ContractFormXmlDataDao;
import edu.uams.clara.webapp.contract.domain.contractform.ContractForm;
import edu.uams.clara.webapp.xml.processor.XmlProcessor;

@Controller
public class ContractFormFinalReviewController {

	private final static Logger logger = LoggerFactory
			.getLogger(ContractFormFinalReviewController.class);
	private ContractFormDao contractFormDao;
	
	private ContractFormXmlDataDao contractFormXmlDataDao;
	
	private ContractDao contractDao;
	
	private XmlProcessor xmlProcessor; 
	
	private BusinessObjectStatusHelperContainer businessObjectStatusHelperContainer;
	
	
	@RequestMapping(value = "/contracts/{contractId}/contract-forms/{contractFormId}/review/{contractFormUrlName}/{committeeReviewPage}", method = RequestMethod.GET)
	public String getContractFormReviewPage(
			@PathVariable("contractId") long contractId,
			@PathVariable("contractFormId") long contractFormId,
			@RequestParam("committee") Committee committee,
			@RequestParam(value="fromQueue", required=false) String fromQueue,
			@PathVariable("committeeReviewPage") String committeeReviewPage,
			@PathVariable("contractFormUrlName") String contractFormUrlName,
			ModelMap modelMap) throws XPathExpressionException, IOException, SAXException{
		ContractForm contractForm = contractFormDao.findById(contractFormId);
		
		//ContractFormXmlData xmlData = contractForm.getTypedContractFormXmlDatas().get(ContractFormXmlDataType.PROTOCOL);
				
		modelMap.put("contractForm", contractForm);
		
		//contractForm.getTypedContractFormXmlDatas().get(contractForm.getContractFormType().getDefaultContractFormXmlDataType());
		//modelMap.put("contractFormXmlData", xmlData);		
		modelMap.put("committee", committee);
		modelMap.put("committeeReviewPage", committeeReviewPage);
		
		String reviewPageXml = businessObjectStatusHelperContainer.getBusinessObjectStatusHelper(contractForm.getFormType()).getReviewPageXml(contractForm, committee, committeeReviewPage);
		logger.debug("reviewPageXml: " + reviewPageXml);
		modelMap.put("reviewPageXml", reviewPageXml);
		modelMap.put("fromQueue", fromQueue);
		modelMap.put("user", (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal());
		
		return "contract/contractform/review/" + contractFormUrlName + "/committee-review";
	}
	

	@Autowired(required=true)
	public void setContractDao(ContractDao contractDao) {
		this.contractDao = contractDao;
	}

	public ContractDao getContractDao() {
		return contractDao;
	}

	@Autowired(required=true)
	public void setContractFormDao(ContractFormDao contractFormDao) {
		this.contractFormDao = contractFormDao;
	}

	public ContractFormDao getContractFormDao() {
		return contractFormDao;
	}

	@Autowired(required=true)
	public void setContractFormXmlDataDao(ContractFormXmlDataDao contractFormXmlDataDao) {
		this.contractFormXmlDataDao = contractFormXmlDataDao;
	}

	public ContractFormXmlDataDao getContractFormXmlDataDao() {
		return contractFormXmlDataDao;
	}


	public XmlProcessor getXmlProcessor() {
		return xmlProcessor;
	}

	@Autowired(required=true)
	public void setXmlProcessor(XmlProcessor xmlProcessor) {
		this.xmlProcessor = xmlProcessor;
	}

	public BusinessObjectStatusHelperContainer getBusinessObjectStatusHelperContainer() {
		return businessObjectStatusHelperContainer;
	}

	@Autowired(required=true)
	public void setBusinessObjectStatusHelperContainer(
			BusinessObjectStatusHelperContainer businessObjectStatusHelperContainer) {
		this.businessObjectStatusHelperContainer = businessObjectStatusHelperContainer;
	}
}
