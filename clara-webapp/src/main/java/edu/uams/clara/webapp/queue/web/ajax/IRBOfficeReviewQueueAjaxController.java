package edu.uams.clara.webapp.queue.web.ajax;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import edu.uams.clara.webapp.common.businesslogic.BusinessObjectStatusHelperContainer;
import edu.uams.clara.webapp.common.dao.usercontext.UserDao;
import edu.uams.clara.webapp.common.dao.usercontext.UserRoleDao;
import edu.uams.clara.webapp.common.domain.usercontext.User;
import edu.uams.clara.webapp.common.domain.usercontext.UserRole;
import edu.uams.clara.webapp.common.domain.usercontext.enums.Committee;
import edu.uams.clara.webapp.common.util.response.JsonResponse;
import edu.uams.clara.webapp.protocol.dao.protocolform.ProtocolFormDao;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolForm;
import edu.uams.clara.webapp.queue.service.QueueService;
import edu.uams.clara.webapp.queue.service.QueueServiceContainer;

@Controller
public class IRBOfficeReviewQueueAjaxController {

	private final static Logger logger = LoggerFactory
			.getLogger(IRBOfficeReviewQueueAjaxController.class);

	private ProtocolFormDao protocolFormDao;

	private BusinessObjectStatusHelperContainer businessObjectStatusHelperContainer;

	private QueueServiceContainer queueServiceContainer;
	
	private UserDao userDao;
	
	private UserRoleDao userRoleDao;
	
	
	/***
	 * Assign the form different IRB Committee for review
	 * if it's NEW SUBMISSION, this handles EXEMPT and EXPEDITED cases
	 * 
	 * @param protocolFormId
	 * @param action
	 * @param reviewerUserRoleId
	 * @param userId
	 * @return
	 */
	@RequestMapping(value = "/ajax/queues/committees/irb-office/process", method = RequestMethod.GET)
	public @ResponseBody
	JsonResponse processReviewItem(
			@RequestParam("protocolFormId") long protocolFormId,
			@RequestParam("itemCategory") String action,
			@RequestParam(value = "reviewerUserRoleId", required=false) long reviewerUserRoleId,
			@RequestParam("userId") long userId) {

		
		try {
			ProtocolForm protocolForm = protocolFormDao
					.findById(protocolFormId);

			User user = userDao.findById(userId);
			
			UserRole assignedReviewerUserRole = null;
			if(reviewerUserRoleId > 0){
				assignedReviewerUserRole = userRoleDao.findById(reviewerUserRoleId);			
			}			
			
			QueueService queueService = queueServiceContainer.getQueueService("Protocol");
						
			queueService.addAssignedReviewerToMetaData(protocolForm, Committee.IRB_OFFICE, user, assignedReviewerUserRole);
			businessObjectStatusHelperContainer.getBusinessObjectStatusHelper(protocolForm.getProtocolFormType().toString()).triggerAction(protocolForm, Committee.IRB_OFFICE, user, action, null, null);
		
			
		} catch (Exception ex) {
			ex.printStackTrace();
			return new JsonResponse(true, "server side exception, the operation was not successful!", null, false, null);
		}
		return new JsonResponse(false, "the study has been successfully processed!", null, false, null);	

	}

	@Autowired(required = true)
	public void setProtocolFormDao(ProtocolFormDao protocolFormDao) {
		this.protocolFormDao = protocolFormDao;
	}

	public ProtocolFormDao getProtocolFormDao() {
		return protocolFormDao;
	}

	@Autowired(required = true)
	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}

	public UserDao getUserDao() {
		return userDao;
	}

	public UserRoleDao getUserRoleDao() {
		return userRoleDao;
	}

	@Autowired(required = true)
	public void setUserRoleDao(UserRoleDao userRoleDao) {
		this.userRoleDao = userRoleDao;
	}

	public BusinessObjectStatusHelperContainer getBusinessObjectStatusHelperContainer() {
		return businessObjectStatusHelperContainer;
	}
	
	@Autowired(required = true)
	public void setBusinessObjectStatusHelperContainer(
			BusinessObjectStatusHelperContainer businessObjectStatusHelperContainer) {
		this.businessObjectStatusHelperContainer = businessObjectStatusHelperContainer;
	}

	public QueueServiceContainer getQueueServiceContainer() {
		return queueServiceContainer;
	}

	@Autowired(required = true)
	public void setQueueServiceContainer(QueueServiceContainer queueServiceContainer) {
		this.queueServiceContainer = queueServiceContainer;
	}

}
