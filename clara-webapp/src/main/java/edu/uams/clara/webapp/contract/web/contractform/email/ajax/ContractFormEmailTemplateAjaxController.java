package edu.uams.clara.webapp.contract.web.contractform.email.ajax;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import edu.uams.clara.webapp.common.dao.usercontext.UserDao;
import edu.uams.clara.webapp.common.domain.email.EmailTemplate;
import edu.uams.clara.webapp.common.domain.usercontext.User;
import edu.uams.clara.webapp.common.domain.usercontext.enums.Committee;
import edu.uams.clara.webapp.common.service.UserAuthenticationService;
import edu.uams.clara.webapp.common.util.response.JsonResponse;
import edu.uams.clara.webapp.contract.dao.contractform.ContractFormDao;
import edu.uams.clara.webapp.contract.domain.contractform.ContractForm;
import edu.uams.clara.webapp.contract.service.email.ContractEmailDataService;

@Controller
public class ContractFormEmailTemplateAjaxController {
	private final static Logger logger = LoggerFactory
			.getLogger(ContractFormEmailTemplateAjaxController.class);
	
	private ContractFormDao contractFormDao;
	private UserDao userDao;
	
	private ContractEmailDataService contractEmailDataService;
	
	private UserAuthenticationService userAuthenticationService;
	
	@RequestMapping(value = "/ajax/contracts/{contractId}/contract-forms/{contractFormId}/email-templates/{committee}/{emailTemplateIdentifier}", method = RequestMethod.POST)
	public @ResponseBody JsonResponse getContractFormEmailTemplate(@PathVariable("contractId") long contractId,
			@PathVariable("contractFormId") long contractFormId,
			@PathVariable("committee") Committee committee,
			@PathVariable("emailTemplateIdentifier") String emailTemplateIdentifier,
			@RequestParam("userId") long userId){
		
		ContractForm contractForm = contractFormDao.findById(contractFormId);
		User user = userDao.findById(userId);
		
		try{
			EmailTemplate emailTemplate = contractEmailDataService.loadEmailTemplate(emailTemplateIdentifier, contractForm, committee, null, user, "");
			return new JsonResponse(false, "", "", false, emailTemplate);
		} catch (Exception e){
			e.printStackTrace();
			return new JsonResponse(true, "Failed to load email template.", "", false, null);
		} 
		
	}

	public ContractEmailDataService getContractEmailDataService() {
		return contractEmailDataService;
	}
	
	@Autowired(required=true)
	public void setContractEmailDataService(ContractEmailDataService contractEmailDataService) {
		this.contractEmailDataService = contractEmailDataService;
	}

	public ContractFormDao getContractFormDao() {
		return contractFormDao;
	}

	@Autowired(required=true)
	public void setContractFormDao(ContractFormDao contractFormDao) {
		this.contractFormDao = contractFormDao;
	}

	public UserDao getUserDao() {
		return userDao;
	}

	@Autowired(required=true)
	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}

	public UserAuthenticationService getUserAuthenticationService() {
		return userAuthenticationService;
	}
	
	@Autowired(required=true)
	public void setUserAuthenticationService(UserAuthenticationService userAuthenticationService) {
		this.userAuthenticationService = userAuthenticationService;
	}
}
