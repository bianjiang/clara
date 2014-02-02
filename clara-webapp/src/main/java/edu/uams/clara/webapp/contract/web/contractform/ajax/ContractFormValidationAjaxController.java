package edu.uams.clara.webapp.contract.web.contractform.ajax;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.xpath.XPathExpressionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.xml.sax.SAXException;

import edu.uams.clara.webapp.common.businesslogic.form.validator.ValidationResponse;
import edu.uams.clara.webapp.common.businesslogic.form.validator.ValidationRuleContainer;
import edu.uams.clara.webapp.common.businesslogic.form.validator.ValidationRuleHandler;
import edu.uams.clara.webapp.common.businesslogic.form.validator.constraint.Constraint;
import edu.uams.clara.webapp.common.businesslogic.form.validator.constraint.enums.ConstraintLevel;
import edu.uams.clara.webapp.common.businesslogic.form.validator.rule.Rule;
import edu.uams.clara.webapp.common.domain.usercontext.User;
import edu.uams.clara.webapp.common.domain.usercontext.enums.Permission;
import edu.uams.clara.webapp.contract.dao.contractform.ContractFormDao;
import edu.uams.clara.webapp.contract.dao.contractform.ContractFormXmlDataDao;
import edu.uams.clara.webapp.contract.dao.contractform.ContractFormXmlDataDocumentDao;
import edu.uams.clara.webapp.contract.domain.contractform.ContractFormXmlData;
import edu.uams.clara.webapp.contract.service.contractform.ContractFormXmlDataDocumentService;
import edu.uams.clara.webapp.xml.processor.XmlProcessor;

@Controller
public class ContractFormValidationAjaxController {
	
	private final static Logger logger = LoggerFactory
	.getLogger(ContractFormValidationAjaxController.class);

	private ContractFormDao contractFormDao;

	private ContractFormXmlDataDao contractFormXmlDataDao;

	private ContractFormXmlDataDocumentDao contractFormXmlDataDocumentDao;

	private XmlProcessor xmlProcessor;

	private ValidationRuleHandler validationRuleHandler;

	private ValidationRuleContainer validationRuleContainer;
	
	private ContractFormXmlDataDocumentService contractFormXmlDataDocumentService;
		
	@RequestMapping(value = "/ajax/contracts/{contractId}/contract-forms/{contractFormId}/{contractFormTypeUrl}/contract-form-xml-datas/{contractFormXmlDataId}/validate", method = RequestMethod.GET)
	public @ResponseBody
	List<ValidationResponse> validateContractForm(
			@PathVariable("contractFormXmlDataId") long contractFormXmlDataId) {
		
		logger.info("whatever");
		User u = (User) SecurityContextHolder.getContext().getAuthentication()
				.getPrincipal();

		ContractFormXmlData contractXmlData = contractFormXmlDataDao.findById(contractFormXmlDataId);

		String xmldata = contractXmlData.getXmlData();
		
		logger.info(xmldata);
		
		List<ValidationResponse> validationResponses = new ArrayList<ValidationResponse>();
		
		if (!u.getAuthorities().contains(Permission.ROLE_CONTRACT_MANAGER) && !u.getAuthorities().contains(Permission.ROLE_CONTRACT_ADMIN) && !u.getAuthorities().contains(Permission.ROLE_CONTRACT_LEGAL_REVIEW)){
			if(StringUtils.hasText(xmldata)){

				List<Rule> contractValidationRules = getValidationRuleContainer().getValidationRules("contractValidationRules");

				Assert.notNull(contractValidationRules);

				Set<String> valueKeys = getValidationRuleContainer().getCachedValueKeys("contractValidationRules");

				Assert.notNull(valueKeys);

				Map<String, List<String>> values = null;

				//setup values
				try {
					values = xmlProcessor.listElementStringValuesByPaths(valueKeys, xmldata);
				} catch (Exception e) {
					logger.error("contract form validation xml error", e);
				}

				//List<String> documentTypeValueList = contractFormXmlDataDocumentDao.listContractFormXmlDataDocumentCategories(contractFormXmlDataId);
				//values.put("/contract/documents/document/document-type", documentTypeValueList);
				
				validationResponses = validationRuleHandler.validate(contractValidationRules, values);
				
				try{
					Map<String, Boolean> documentMap = contractFormXmlDataDocumentService.checkRequiredDocuments(contractXmlData);
					
					for(Map.Entry<String, Boolean> entry : documentMap.entrySet()){
						
						if (!entry.getValue()){
							Constraint documentConstraint = new Constraint();
							Map<String, Object> documentAdditionalData = new HashMap<String, Object>();
							
							documentConstraint.setConstraintLevel(ConstraintLevel.ERROR);
							documentConstraint.setErrorMessage(entry.getKey() + " is required!");
							
							documentAdditionalData.put("pagename", "Documents");
							documentAdditionalData.put("pageref", "documents");
							
							ValidationResponse documentVP = new ValidationResponse(documentConstraint, documentAdditionalData);
							
							validationResponses.add(documentVP);
						}
					}
				} catch (Exception e){
					e.printStackTrace();
				}
			}
		}
		
		return validationResponses;
	}

	@Autowired(required=true)
	public void setXmlProcessor(XmlProcessor xmlProcessor) {
		this.xmlProcessor = xmlProcessor;
	}

	public XmlProcessor getXmlProcessor() {
		return xmlProcessor;
	}

	@Autowired(required=true)
	public void setValidationRuleContainer(ValidationRuleContainer validationRuleContainer) {
		this.validationRuleContainer = validationRuleContainer;
	}

	public ValidationRuleContainer getValidationRuleContainer() {
		return validationRuleContainer;
	}

	@Autowired(required=true)
	public void setValidationRuleHandler(ValidationRuleHandler validationRuleHandler) {
		this.validationRuleHandler = validationRuleHandler;
	}

	public ValidationRuleHandler getValidationRuleHandler() {
		return validationRuleHandler;
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

	public ContractFormXmlDataDocumentService getContractFormXmlDataDocumentService() {
		return contractFormXmlDataDocumentService;
	}
	
	@Autowired(required=true)
	public void setContractFormXmlDataDocumentService(
			ContractFormXmlDataDocumentService contractFormXmlDataDocumentService) {
		this.contractFormXmlDataDocumentService = contractFormXmlDataDocumentService;
	}

	public ContractFormXmlDataDocumentDao getContractFormXmlDataDocumentDao() {
		return contractFormXmlDataDocumentDao;
	}
	
	@Autowired(required=true)
	public void setContractFormXmlDataDocumentDao(
			ContractFormXmlDataDocumentDao contractFormXmlDataDocumentDao) {
		this.contractFormXmlDataDocumentDao = contractFormXmlDataDocumentDao;
	}
}
