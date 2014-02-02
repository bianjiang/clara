package edu.uams.clara.webapp.protocol.web.protocolform.email.ajax;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import edu.uams.clara.webapp.common.businesslogic.BusinessObjectStatusHelperContainer;
import edu.uams.clara.webapp.common.dao.usercontext.UserDao;
import edu.uams.clara.webapp.common.domain.email.EmailTemplate;
import edu.uams.clara.webapp.common.domain.usercontext.User;
import edu.uams.clara.webapp.common.domain.usercontext.enums.Committee;
import edu.uams.clara.webapp.common.util.response.JsonResponse;
import edu.uams.clara.webapp.protocol.dao.protocolform.ProtocolFormDao;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolForm;
import edu.uams.clara.webapp.protocol.service.email.ProtocolEmailDataService;

@Controller
public class ProtocolFormEmailTemplateAjaxController {
	private final static Logger logger = LoggerFactory
			.getLogger(ProtocolFormEmailTemplateAjaxController.class);
	
	private ProtocolFormDao protocolFormDao;
	private UserDao userDao;
	
	private ProtocolEmailDataService protocolEmailDataService;
	
	private BusinessObjectStatusHelperContainer businessObjectStatusHelperContainer;	
	
	@RequestMapping(value = "/ajax/protocols/{protocolId}/protocol-forms/{protocolFormId}/email-templates/{committee}/{decision}", method = RequestMethod.POST)
	public @ResponseBody JsonResponse getProtocolFormEmailTemplate(@PathVariable("protocolId") long protocolId,
			@PathVariable("protocolFormId") long protocolFormId,
			@PathVariable("committee") Committee committee,
			@PathVariable("decision") String decision,
			@RequestParam("userId") long userId){
		
		ProtocolForm protocolForm = protocolFormDao.findById(protocolFormId);
		User user = userDao.findById(userId);
		
		try {
			String emailIdentifier = businessObjectStatusHelperContainer
					.getBusinessObjectStatusHelper(protocolForm.getProtocolFormType().toString())
					.getEmailIdentifierByDecision(protocolForm, committee, decision);
			
			//String identifier = protocolFormStatus.getProtocolFormStatus() + "_" + committee.toString() + "_" + action;
			
			//logger.debug("emailTemplate identifier: " + identifier);

			return new JsonResponse(false, protocolEmailDataService.loadEmailTemplate(emailIdentifier, protocolForm, committee, null, user, ""));
		} catch (Exception e) {
			e.printStackTrace();
			return new JsonResponse(true, "Failed to load email template!", "", false);
		}
		
	}

	public ProtocolEmailDataService getProtocolEmailDataService() {
		return protocolEmailDataService;
	}
	
	@Autowired(required=true)
	public void setProtocolEmailDataService(ProtocolEmailDataService protocolEmailDataService) {
		this.protocolEmailDataService = protocolEmailDataService;
	}

	public ProtocolFormDao getProtocolFormDao() {
		return protocolFormDao;
	}

	@Autowired(required=true)
	public void setProtocolFormDao(ProtocolFormDao protocolFormDao) {
		this.protocolFormDao = protocolFormDao;
	}

	public UserDao getUserDao() {
		return userDao;
	}

	@Autowired(required=true)
	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
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
