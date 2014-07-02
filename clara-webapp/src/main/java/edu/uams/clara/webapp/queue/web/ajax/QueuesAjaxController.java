package edu.uams.clara.webapp.queue.web.ajax;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.collect.Sets;

import edu.uams.clara.webapp.common.dao.usercontext.UserDao;
import edu.uams.clara.webapp.common.dao.usercontext.UserRoleDao;
import edu.uams.clara.webapp.common.domain.form.Form;
import edu.uams.clara.webapp.common.domain.usercontext.User;
import edu.uams.clara.webapp.common.domain.usercontext.UserRole;
import edu.uams.clara.webapp.common.domain.usercontext.enums.Committee;
import edu.uams.clara.webapp.common.domain.usercontext.enums.Permission;
import edu.uams.clara.webapp.common.exception.ajax.AjaxResponseException;
import edu.uams.clara.webapp.common.util.response.JsonResponse;
import edu.uams.clara.webapp.queue.service.QueueService;
import edu.uams.clara.webapp.queue.service.QueueServiceContainer;
import edu.uams.clara.webapp.xml.processor.XmlProcessor;

@Controller
public class QueuesAjaxController {

	private final static Logger logger = LoggerFactory
			.getLogger(QueuesAjaxController.class);

	private UserDao userDao;
	
	private UserRoleDao userRoleDao;

	private QueueServiceContainer queueServiceContainer;
	
	private XmlProcessor xmlProcessor;

	@Value("${queue.template.xml.uri}")
	private String queueTemplateXmlUri;

	/**
	 * list queues where the user has access to according to the user's role
	 * 
	 * @param userId
	 * @return
	 */
	@Cacheable(value = "edu.uams.clara.webapp.queue.web.ajax.QueuesAjaxController.listUserQueues")
	@RequestMapping(value = "/ajax/queues/list-user-queues.xml", method = RequestMethod.GET)
	public @ResponseBody
	String listUserQueues(@RequestParam("userId") long userId) {

		User sessionUser = (User) SecurityContextHolder.getContext()
				.getAuthentication().getPrincipal();

		User user = userDao.findById(userId);

		if (sessionUser.getId() != user.getId()) {
			throw new AjaxResponseException(
					"the userId of the request param, does not match the session user! illegal access!");
		}

		Set<String> lookupPaths = new HashSet<String>();

		for (UserRole ur : user.getUserRoles()) {
			if (!ur.isRetired() && ur.getRole().getCommitee() != null) {
				//logger.debug("ur: " + ur.getRole().getName());
				
				//eliminate duplicate roles sent back to client
				Permission p = ur.getRole().getRolePermissionIdentifier();
				if (p.equals(Permission.ROLE_BUDGET_MANAGER) || p.equals(Permission.ROLE_BUDGET_REVIEWER)){
					p = Permission.ROLE_BUDGET_REVIEWER;
				}
				
				if (p.equals(Permission.ROLE_COVERAGE_MANAGER) || p.equals(Permission.ROLE_COVERAGE_REVIEWER)){
					p = Permission.ROLE_COVERAGE_REVIEWER;
				}
				
				if (p.equals(Permission.ROLE_IRB_PREREVIEW) || p.equals(Permission.ROLE_IRB_PROTOCOL_REVIEWER) || p.equals(Permission.ROLE_IRB_CONSENT_REVIEWER)){
					p = Permission.ROLE_IRB_PREREVIEW;
				}
				
				if (p.equals(Permission.ROLE_REGULATORY_MANAGER) || p.equals(Permission.ROLE_MONITORING_REGULATORY_QA_REVIEWER)){
					p = Permission.ROLE_MONITORING_REGULATORY_QA_REVIEWER;
				}
				
				if (p.equals(Permission.ROLE_COLLEGE_DEAN) || p.equals(Permission.ROLE_DEPARTMENT_CHAIR)){
					p = Permission.ROLE_COLLEGE_DEAN;
				}
				
				lookupPaths.add("/queues/queue/roles/role[@identifier='"
						+ p
						+ "']/../..");
			}
		}
		
		String resultXml = "<list>";

		try {
			String queueTemplateXml = xmlProcessor
					.loadXmlFile(queueTemplateXmlUri);
			
			List<String> queues = xmlProcessor.listElementDomStringsByPaths(lookupPaths, queueTemplateXml, false);
			
			Set<String> uQueues = Sets.newHashSet(queues);
			
			for (String q: uQueues){
				resultXml += q;
			}
			
			resultXml += "</list>";
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new AjaxResponseException(
					"something wrong when loading the queue-template xml...");

		}
		

		return resultXml;
	}
	
	public static int listFormsInQueueCacheKey(String queueIdentifier, String objectType, long userId, Boolean showHistory)
    {
		
		if (showHistory == null) showHistory = false;
		int hash = 17;
		hash = (int) (31 * hash + userId);
		hash = 31 * hash + queueIdentifier.hashCode();
		hash = 31 * hash + objectType.hashCode();
		hash = 31 * hash + showHistory.hashCode();
		return hash;
    }

	@Cacheable(value = "edu.uams.clara.webapp.queue.web.ajax.QueuesAjaxController.listFormsInQueue", key="T(edu.uams.clara.webapp.queue.web.ajax.QueuesAjaxController).listFormsInQueueCacheKey(#root.args[0], #root.args[1], #root.args[2], #root.args[3])")
	@RequestMapping(value = "/ajax/queues/forms/list.xml", method = RequestMethod.GET)
	public @ResponseBody
	String listFormsInQueue(@RequestParam("queue") String queueIdentifier,
			@RequestParam("objectType") String objectType,
			@RequestParam("userId") long userId,
			@RequestParam(value="showHistory", required=false) Boolean showHistory) {

		User sessionUser = (User) SecurityContextHolder.getContext()
				.getAuthentication().getPrincipal();

		User user = userDao.findById(userId);

		if (sessionUser.getId() != user.getId()) {
			throw new AjaxResponseException(
					"the userId of the request param, does not match the session user! illegal access!");
		}
		
		QueueService queueService = null;
		try {
			if (showHistory == null) showHistory = false;
			queueService = queueServiceContainer.getQueueService(objectType);
			
			
			return queueService.getFormsInQueueByUser(
						queueIdentifier, user, showHistory);
			

		} catch (Exception ex) {
			
			logger.error("queue: getting queue for objectType:" + objectType + "; userId: " + userId + "; showHistory: " + showHistory +"; Null point exception debug: (queueService)" + queueService, ex);
			throw new AjaxResponseException(
					"something wrong when loading the forms in review queue...");

		}
	}
	
	@RequestMapping(value = "/ajax/queues/assign-reviewer", method = RequestMethod.GET)
	public @ResponseBody
	JsonResponse assignReviewer(@RequestParam("objectType") String objectType,
			@RequestParam("formId") long formId,
			@RequestParam("committee") Committee committee,
			@RequestParam("userId") long userId,
			@RequestParam("reviewerUserRoleId") long reviewerUserRoleId) {
		
		User sessionUser = (User) SecurityContextHolder.getContext()
				.getAuthentication().getPrincipal();

		User user = userDao.findById(userId);

		if (sessionUser.getId() != user.getId()) {
			throw new AjaxResponseException(
					"the userId of the request param, does not match the session user! illegal access!");
		}
		
		UserRole reviewerUserRole = userRoleDao.findById(reviewerUserRoleId);

		QueueService queueService = queueServiceContainer.getQueueService(objectType);
		
		Form form = queueService.getForm(formId);
		
		try {
			queueService.addAssignedReviewerToMetaData(form, committee, user, reviewerUserRole);
		} catch (Exception ex){
			ex.printStackTrace();
			return new JsonResponse(true, "error when assigning reviewer to this queue item...", null, false, null);
		}
				
		return new JsonResponse(false, "assigned", null, false, null);
		
	}
	
	@RequestMapping(value = "/ajax/queues/remove-reviewer", method = RequestMethod.GET)
	public @ResponseBody
	JsonResponse removeReviewer(@RequestParam("objectType") String objectType,
			@RequestParam("formId") long formId,
			@RequestParam("committee") Committee committee,
			@RequestParam("userId") long userId,
			@RequestParam("reviewerUserRoleId") long reviewerUserRoleId) {
		
		User sessionUser = (User) SecurityContextHolder.getContext()
				.getAuthentication().getPrincipal();

		User user = userDao.findById(userId);

		if (sessionUser.getId() != user.getId()) {
			throw new AjaxResponseException(
					"the userId of the request param, does not match the session user! illegal access!");
		}
		
		UserRole reviewerUserRole = userRoleDao.findById(reviewerUserRoleId);

		QueueService queueService = queueServiceContainer.getQueueService(objectType);
		
		
		Form form = queueService.getForm(formId);
		
		try {
			queueService.removeAssignedReviewerFromMetaData(form, committee, user, reviewerUserRole);
		} catch (Exception ex){
			ex.printStackTrace();
			return new JsonResponse(true, "error when removing reviewer...", null, false, null);
		}
		
		return new JsonResponse(false, "removed", null, false, null);
	}
	
	@RequestMapping(value = "/ajax/queues/complete-assign-reviewer", method = RequestMethod.GET)
	public @ResponseBody
	JsonResponse completeAssignReviewer(@RequestParam("objectType") String objectType,
			@RequestParam("formId") long formId,
			@RequestParam("committee") Committee committee,
			@RequestParam("userId") long userId,
			@RequestParam("action") String action) {
		
		User sessionUser = (User) SecurityContextHolder.getContext()
				.getAuthentication().getPrincipal();

		User user = userDao.findById(userId);

		if (sessionUser.getId() != user.getId()) {
			throw new AjaxResponseException(
					"the userId of the request param, does not match the session user! illegal access!");
		}

		QueueService queueService = queueServiceContainer.getQueueService(objectType);
		
		Form form = queueService.getForm(formId);
		
		try {
			queueService.triggerAssignReviewerAction(form, committee, user, action);
		} catch (Exception ex){
			ex.printStackTrace();
			return new JsonResponse(true, "error when completing assigning reviewers ...", null, false, null);
		}
		
		return new JsonResponse(false, "completed", null, false, null);
	}

	@Autowired(required = true)
	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}

	public UserDao getUserDao() {
		return userDao;
	}

	public XmlProcessor getXmlProcessor() {
		return xmlProcessor;
	}

	@Autowired(required = true)
	public void setXmlProcessor(XmlProcessor xmlProcessor) {
		this.xmlProcessor = xmlProcessor;
	}

	public String getQueueTemplateXmlUri() {
		return queueTemplateXmlUri;
	}

	public void setQueueTemplateXmlUri(String queueTemplateXmlUri) {
		this.queueTemplateXmlUri = queueTemplateXmlUri;
	}

	public UserRoleDao getUserRoleDao() {
		return userRoleDao;
	}

	@Autowired(required = true)
	public void setUserRoleDao(UserRoleDao userRoleDao) {
		this.userRoleDao = userRoleDao;
	}

	public QueueServiceContainer getQueueServiceContainer() {
		return queueServiceContainer;
	}

	@Autowired(required=true)
	public void setQueueServiceContainer(QueueServiceContainer queueServiceContainer) {
		this.queueServiceContainer = queueServiceContainer;
	}
}
