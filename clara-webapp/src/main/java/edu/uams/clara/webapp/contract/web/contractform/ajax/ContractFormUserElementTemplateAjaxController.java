package edu.uams.clara.webapp.contract.web.contractform.ajax;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import edu.uams.clara.webapp.common.dao.usercontext.UserDao;
import edu.uams.clara.webapp.common.domain.usercontext.User;
import edu.uams.clara.webapp.contract.dao.contractform.ContractFormUserElementTemplateDao;
import edu.uams.clara.webapp.contract.domain.contractform.ContractFormUserElementTemplate;
import edu.uams.clara.webapp.contract.domain.contractform.ContractFormUserElementTemplate.TemplateType;
import edu.uams.clara.webapp.contract.service.ContractFormUserElementTemplateService;

@Controller
public class ContractFormUserElementTemplateAjaxController {

	private final static Logger logger = LoggerFactory
	.getLogger(ContractFormUserElementTemplateAjaxController.class);
	
	private ContractFormUserElementTemplateDao contractFormUserElementTemplateDao;
	
	private UserDao userDao;
	
	private ContractFormUserElementTemplateService contractFormUserElementTemplateService;
	
	@RequestMapping(value = "/ajax/contracts/contract-forms/user-templates/list-by-type-and-user")
	public @ResponseBody
	List<ContractFormUserElementTemplate> listContractFormUserElementTemplateByTemplateTypeAndUserId(
			@RequestParam("templateType") ContractFormUserElementTemplate.TemplateType templateType,
			@RequestParam("userId") long userId) {
			
		List<ContractFormUserElementTemplate> contractFormUserElementTemplates = new ArrayList<ContractFormUserElementTemplate>(0);
		try{
			contractFormUserElementTemplates = contractFormUserElementTemplateDao.listContractFormUserElementTemplateByTemplateTypeAndUserId(templateType, userId);
		}catch(Exception ex){
			ex.printStackTrace();
		}
			
		return contractFormUserElementTemplates;
	}
	
	@RequestMapping(value = "/ajax/contracts/contract-forms/user-templates/{contractFormUserElementTemplateId}/get-xml-data")
	public @ResponseBody
	String getContractFormUserElementTemplateXmlData(
			@PathVariable("contractFormUserElementTemplateId") long contractFormUserElementTemplateId) {
		ContractFormUserElementTemplate contractFormUserElementTemplate = contractFormUserElementTemplateDao.findById(contractFormUserElementTemplateId);

		String updatedXmlData = contractFormUserElementTemplateService.updateTemplateXMLData(contractFormUserElementTemplate.getXmlData(), contractFormUserElementTemplate.getTemplateType());

		return updatedXmlData;
	}
	
	@RequestMapping(value = "/ajax/contracts/contract-forms/user-templates/add")
	public @ResponseBody
	ContractFormUserElementTemplate addContractFormUserElementTemplate(
			@RequestParam("templateType") ContractFormUserElementTemplate.TemplateType templateType,
			@RequestParam("userId") long userId,
			@RequestParam("xmlData") String xmlData,
			@RequestParam("templateName") String templateName
			){
		
		User user = userDao.findById(userId);
		
		ContractFormUserElementTemplate contractFormUserElementTemplate = new ContractFormUserElementTemplate();
		contractFormUserElementTemplate.setUser(user);
		contractFormUserElementTemplate.setCreated(new Date());
		contractFormUserElementTemplate.setXmlData(xmlData);
		contractFormUserElementTemplate.setTemplateType(templateType);
		contractFormUserElementTemplate.setTemplateName(templateName);
		
		return contractFormUserElementTemplateDao.saveOrUpdate(contractFormUserElementTemplate);
	
	}
	
	@RequestMapping(value = "/ajax/contracts/contract-forms/user-templates/{contractFormUserElementTemplateId}/update")
	public @ResponseBody
	Boolean updateContractFormUserElementTemplate(
			@PathVariable("contractFormUserElementTemplateId") long contractFormUserElementTemplateId,
			@RequestParam("xmlData") String xmlData,
			@RequestParam("name") String templateName){
		
		ContractFormUserElementTemplate contractFormUserElementTemplate = contractFormUserElementTemplateDao.findById(contractFormUserElementTemplateId);
		contractFormUserElementTemplate.setXmlData(xmlData);
		contractFormUserElementTemplate.setTemplateName(templateName);
		try{
			contractFormUserElementTemplate = contractFormUserElementTemplateDao.saveOrUpdate(contractFormUserElementTemplate);
			return Boolean.TRUE;
		}catch(Exception ex){
			ex.printStackTrace();
			return Boolean.FALSE;
		}
	}
	
	
	@RequestMapping(value = "/ajax/contracts/contract-forms/user-templates/{contractFormUserElementTemplateId}/remove")
	public @ResponseBody
	Boolean removeContractFormUserElementTemplate(
			@PathVariable("contractFormUserElementTemplateId") long contractFormUserElementTemplateId){
		
		ContractFormUserElementTemplate contractFormUserElementTemplate = contractFormUserElementTemplateDao.findById(contractFormUserElementTemplateId);
		contractFormUserElementTemplate.setRetired(true);
		try{
			
			contractFormUserElementTemplate = contractFormUserElementTemplateDao.saveOrUpdate(contractFormUserElementTemplate);
			return Boolean.TRUE;
		}catch(Exception ex){
			ex.printStackTrace();
			return Boolean.FALSE;
		}
	}
	
	@Autowired(required=true)
	public void setContractFormUserElementTemplateDao(
			ContractFormUserElementTemplateDao contractFormUserElementTemplateDao) {
		this.contractFormUserElementTemplateDao = contractFormUserElementTemplateDao;
	}

	public ContractFormUserElementTemplateDao getContractFormUserElementTemplateDao() {
		return contractFormUserElementTemplateDao;
	}

	@Autowired(required=true)
	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}

	public UserDao getUserDao() {
		return userDao;
	}

	public ContractFormUserElementTemplateService getContractFormUserElementTemplateService() {
		return contractFormUserElementTemplateService;
	}

	@Autowired(required=true)
	public void setContractFormUserElementTemplateService(
			ContractFormUserElementTemplateService contractFormUserElementTemplateService) {
		this.contractFormUserElementTemplateService = contractFormUserElementTemplateService;
	}
	
	
}
