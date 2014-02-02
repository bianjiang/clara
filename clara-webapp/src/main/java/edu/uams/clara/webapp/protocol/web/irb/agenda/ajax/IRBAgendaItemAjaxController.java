package edu.uams.clara.webapp.protocol.web.irb.agenda.ajax;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

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

import edu.uams.clara.core.util.xml.XmlHandler;
import edu.uams.clara.core.util.xml.XmlHandlerFactory;
import edu.uams.clara.webapp.common.businesslogic.BusinessObjectStatusHelperContainer;
import edu.uams.clara.webapp.common.dao.usercontext.UserDao;
import edu.uams.clara.webapp.common.domain.email.EmailTemplate;
import edu.uams.clara.webapp.common.domain.usercontext.User;
import edu.uams.clara.webapp.common.domain.usercontext.enums.Committee;
import edu.uams.clara.webapp.common.service.UserAuthenticationService;
import edu.uams.clara.webapp.common.service.audit.AuditService;
import edu.uams.clara.webapp.common.util.XMLResponseHelper;
import edu.uams.clara.webapp.common.util.response.JsonResponse;
import edu.uams.clara.webapp.protocol.dao.businesslogicobject.ProtocolFormStatusDao;
import edu.uams.clara.webapp.protocol.dao.irb.AgendaDao;
import edu.uams.clara.webapp.protocol.dao.irb.AgendaItemDao;
import edu.uams.clara.webapp.protocol.dao.irb.AgendaItemReviewerDao;
import edu.uams.clara.webapp.protocol.dao.protocolform.ProtocolFormDao;
import edu.uams.clara.webapp.protocol.dao.protocolform.ProtocolFormXmlDataDao;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.enums.AgendaStatusEnum;
import edu.uams.clara.webapp.protocol.domain.irb.Agenda;
import edu.uams.clara.webapp.protocol.domain.irb.AgendaItem;
import edu.uams.clara.webapp.protocol.domain.irb.AgendaItemReviewer;
import edu.uams.clara.webapp.protocol.domain.irb.AgendaItemWrapper;
import edu.uams.clara.webapp.protocol.domain.irb.IRBReviewer;
import edu.uams.clara.webapp.protocol.domain.irb.AgendaItem.AgendaItemCategory;
import edu.uams.clara.webapp.protocol.domain.irb.AgendaItem.AgendaItemStatus;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolForm;
import edu.uams.clara.webapp.protocol.service.ProtocolFormDetailContentService;
import edu.uams.clara.webapp.protocol.service.email.ProtocolEmailDataService;
import edu.uams.clara.webapp.protocol.service.irb.agenda.MeetingService;

@Controller
public class IRBAgendaItemAjaxController {
	
	private final static Logger logger = LoggerFactory
	.getLogger(IRBAgendaItemAjaxController.class);
	
	private AgendaDao agendaDao;
	
	private AgendaItemDao agendaItemDao;

	private AgendaItemReviewerDao agendaItemReviewerDao;
	
	private ProtocolFormDao protocolFormDao;

	private ProtocolFormXmlDataDao protocolFormXmlDataDao;

	private ProtocolFormStatusDao protocolFormStatusDao;
	
	private ProtocolEmailDataService protocolEmailDataService;
	
	private UserDao userDao;
	
	private MeetingService meetingService;
	
	private BusinessObjectStatusHelperContainer businessObjectStatusHelperContainer;	
	
	private UserAuthenticationService userAuthenticationService;
	
	private ProtocolFormDetailContentService protocolFormDetailContentService;
	
	private AuditService auditService;
	
	
	/**
	 * need optimization... need to find a query to update order at once...
	 * @param agendaId
	 * @param agendaItemIds
	 * @return
	 */
	@RequestMapping(value = "/ajax/agendas/{agendaId}/agenda-items/set-order", method = RequestMethod.POST)
	public @ResponseBody
	Boolean setAgendaItemsOrder(
			@PathVariable("agendaId") long agendaId,
			@RequestParam("agendaItemIds") List<String> agendaItemIds) {

		logger.debug("" + agendaItemIds.size());
		int order = 0;
		
		for(String agendaItemId:agendaItemIds){
			try{
				//AgendaItem agendaItem = agendaItemDao.findById(Long.parseLong(agendaItemId));
				//agendaItem.setOrder(order ++ );
				//agendaItemDao.saveOrUpdate(agendaItem);
				
				agendaItemDao.updateAgendaItemOrder(Long.parseLong(agendaItemId), order ++);
			}catch(Exception ex){
				ex.printStackTrace();
				logger.error("cannot reorder");
			}
		}
		
		return Boolean.TRUE;
	}
	
	@RequestMapping(value = "/ajax/agendas/{agendaId}/agenda-items/list", method = RequestMethod.GET)
	public @ResponseBody
	String listAgendaItemsByDate(@PathVariable("agendaId") long agendaId, @RequestParam(value="hideReported", required=false) Boolean hideReported) {
		boolean hideReportedOrNot = (hideReported != null)?hideReported:false;
		
		List agendaItems = agendaItemDao.listByAgendaIdPure(agendaId, hideReportedOrNot);

		String xmlResult = "<list>";
		
		XmlHandler xmlHander = null;
		try {
			xmlHander = XmlHandlerFactory.newXmlHandler();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			logger.info("can't create xmlHandler? in IRBAgendaItemAjaxController");
		}

		for (Object agendaItemRowObject: agendaItems) {
			Map agendaItemRow = (Map) agendaItemRowObject;
			
			ProtocolForm protocolForm = protocolFormDao.findById(Long.valueOf(agendaItemRow.get("protocolFormId").toString()));

			String formTypeMachineReadable = (protocolForm!=null)?protocolForm.getProtocolFormType()
					.getUrlEncoded():"";

			List agendaItemReviewers = agendaItemReviewerDao.listAgendaItemReviewersNameByAgendaItemId(Long.valueOf(agendaItemRow.get("agendaItemId").toString()));

			xmlResult += "<agenda-item id=\"" + agendaItemRow.get("agendaItemId").toString() + "\" category=\"" + agendaItemRow.get("agendaItemCategory").toString() + "\">"; 
			xmlResult += "<xml-data>";
				xmlResult += agendaItemRow.get("agendaItemXmlData").toString();
			xmlResult += "</xml-data>";
		
			xmlResult += "<reviewers>";
			
			if (agendaItemReviewers != null && agendaItemReviewers.size() > 0) {
				for (Object rowObject: agendaItemReviewers) {
					Map row = (Map) rowObject;
					
					xmlResult += "<reviewer>";
					xmlResult += "<name>" + row.get("fullName").toString() + "</name>";
					xmlResult += "</reviewer>";
				}
				/*
				for(AgendaItemReviewerWrapper agendaItemReviewerWrapper:agendaItemReviewers){
					Person rp = agendaItemReviewerWrapper.getIRBReviewer().getUser().getPerson();
					xmlResult += "<reviewer>";
						xmlResult += "<name>" + rp.getFirstname() + " " + rp.getLastname() + "</name>";
					xmlResult += "</reviewer>";
				}*/
			}
				
			xmlResult += "</reviewers>";
			if (protocolForm != null){

				xmlResult += "<protocol-form id=\""
						+ protocolForm.getId() 
						//+ "\" lastVersionId=\""
						//+ lastProtocolFormXmlData.getId()
						+ "\">";
				xmlResult += "<protocol-meta>";
				xmlResult += protocolForm.getObjectMetaData();
				xmlResult += "</protocol-meta>";
				xmlResult += "<protocol-form-meta><status>";
				xmlResult += xmlHander != null ?xmlHander.getSingleStringValueByXPath(protocolForm.getMetaXml(), "//status"):"";
				xmlResult += "</status></protocol-form-meta>";
				xmlResult += "<protocol-form-type id=\"" + formTypeMachineReadable
						+ "\">"
						+ protocolForm.getProtocolFormType().getDescription()
						+ "</protocol-form-type>";
				//xmlResult += "<status><description>"
				//		+ org.apache.commons.lang.StringEscapeUtils
				//				.escapeXml(pformStatus.getProtocolFormStatus()
				//						.getDescription())
				//		+ "</description><modified>" + pformStatus.getModified()
				//		+ "</modified></status>";
				xmlResult += protocolFormDetailContentService
						.getDetailContent(protocolForm);
				xmlResult += "</protocol-form>";
			} else {
				xmlResult += "<protocol-form></protocol-form>";
			}
			
			xmlResult += "</agenda-item>";
		}

		xmlResult += "</list>";

		return xmlResult;
	}
	


	@RequestMapping(value = "/ajax/agendas/{agendaId}/agenda-items/{agendaItemId}/agenda-reviewers/list", method = RequestMethod.GET)
	public @ResponseBody
	List<AgendaItemReviewer> listIRBReviewerOnAgendaItem(
			@PathVariable("agendaItemId") long agendaItemId) {

		List<AgendaItemReviewer> agendaItemReviewrs = null;
		try {
			agendaItemReviewrs = agendaItemReviewerDao
					.listByAgendaItemIdNoWrapper(agendaItemId);
		} catch (Exception e) {
			logger.warn("cannot list irbReviewers for agendaItem: {"
					+ agendaItemId + "}; because of: " + e.getMessage());
			e.printStackTrace();
		}

		return agendaItemReviewrs;

	}


	// @TODO missing audit
	@RequestMapping(value = "/ajax/agendas/{agendaId}/agenda-items/{agendaItemId}/agenda-reviewers/assign", method = RequestMethod.GET)
	public @ResponseBody
	String assignIRBReviewerToAgendaItem(
			@PathVariable("agendaItemId") long agendaItemId,
			@RequestParam("irbReviewerId") long irbReviewerId,
			@RequestParam("userId") long userId) {
		
		boolean ifExisting = agendaItemReviewerDao.checkIfExistByAgendaItemIdandIRBReviewerId(agendaItemId, irbReviewerId);
		
		if (!ifExisting) {
			try {
				AgendaItemReviewer agendaItemReviewer = new AgendaItemReviewer();
				AgendaItem agendaItem = new AgendaItem();
				agendaItem.setId(agendaItemId);

				IRBReviewer irbReviewer = new IRBReviewer();
				irbReviewer.setId(irbReviewerId);

				agendaItemReviewer.setAgendaItem(agendaItem);
				agendaItemReviewer.setIrbReviewer(irbReviewer);

				agendaItemReviewerDao.saveOrUpdate(agendaItemReviewer);
			} catch (Exception e) {
				return XMLResponseHelper.xmlResult(Boolean.FALSE); 
			}
		}

		return XMLResponseHelper.xmlResult(Boolean.TRUE);
	}

	// @TODO missing audit
	@RequestMapping(value = "/ajax/agendas/{agendaId}/agenda-items/{agendaItemId}/agenda-reviewers/{agendaItemReviewerId}/remove", method = RequestMethod.GET)
	public @ResponseBody
	Boolean removeIRBReviewerFromAgendaItem(
			@PathVariable("agendaItemReviewerId") long agendaItemReviewerId,
			@RequestParam("userId") long userId) {

		AgendaItemReviewer agendaItemReviewer = null;
		try {
			agendaItemReviewer = agendaItemReviewerDao
					.findById(agendaItemReviewerId);
			agendaItemReviewer.setRetired(Boolean.TRUE);
			
			agendaItemReviewer = agendaItemReviewerDao.saveOrUpdate(agendaItemReviewer);

		} catch (Exception e) {
			e.printStackTrace();

			return Boolean.FALSE;
		}

		if (agendaItemReviewer == null) {
			return Boolean.FALSE;
		}

		//agendaItemReviewerDao.remove(agendaItemReviewer);

		return Boolean.TRUE;
	}
	
	@RequestMapping(value = "/ajax/agendas/{agendaId}/agenda-items/assign", method = RequestMethod.GET)
	public @ResponseBody
	JsonResponse assignAgendaItemToAgenda(
			@PathVariable("agendaId") long agendaId,
			@RequestParam("protocolFormId") long protocolFormId,
			@RequestParam("agendaItemCategory") AgendaItemCategory agendaItemCategory,
			@RequestParam("userId") long userId) {

		AgendaItem agendaItem = new AgendaItem(); 
		try {
			
			Agenda agenda = agendaDao.findById(agendaId);
			ProtocolForm protocolForm = protocolFormDao.findById(protocolFormId);
			
			List<AgendaItemWrapper> agendaItemLst = agendaItemDao.listByAgendaId(agenda.getId());
			if (agendaItemLst != null){
				for (AgendaItemWrapper ai : agendaItemLst){
					if (ai.getProtocolFormId() == protocolForm.getId()){
						return new JsonResponse(true, "Agenda Item already exists in the agenda!", "", false, null);
					}
				}
			}
			
			User user = userDao.findById(userId);
			
			businessObjectStatusHelperContainer
			.getBusinessObjectStatusHelper("AGENDA_ITEM")
			.triggerAction(
					protocolForm,
					Committee.IRB_OFFICE,
					user,
					"ASSIGNED_TO_AGENDA",
					null, null);
			
			agendaItem.setAgenda(agenda);
			agendaItem.setProtocolForm(protocolForm);
			agendaItem.setAgendaItemStatus(AgendaItemStatus.NEW);
			agendaItem.setAgendaItemCategory(agendaItemCategory);
			agendaItem.setOrder(0);

			agendaItem = agendaItemDao.saveOrUpdate(agendaItem);
			
		} catch (Exception e) {
			e.printStackTrace();			
		}
		return new JsonResponse(false, agendaItem);
	}
	
	private List<AgendaStatusEnum> availableAgendaStatuses = new ArrayList<AgendaStatusEnum>(
			0);
	{
		availableAgendaStatuses.add(AgendaStatusEnum.AGENDA_INCOMPLETE);
		availableAgendaStatuses
				.add(AgendaStatusEnum.AGENDA_PENDING_CHAIR_APPROVAL);
		availableAgendaStatuses.add(AgendaStatusEnum.AGENDA_APPROVED);
	}
	
	/**
	 * @TODO missing audit
	 * @param agendaItemId
	 * @param userId
	 * @return
	 */
	@RequestMapping(value = "/ajax/agendas/{agendaId}/agenda-items/{agendaItemId}/remove", method = RequestMethod.GET)
	public @ResponseBody
	Boolean removeAgendaItem(@PathVariable("agendaItemId") long agendaItemId,
			@RequestParam("userId") long userId) {
		try {
			AgendaItem agendaItem = agendaItemDao.findById(agendaItemId);
			
			User user = userDao.findById(userId);
			
			businessObjectStatusHelperContainer
			.getBusinessObjectStatusHelper("AGENDA_ITEM")
			.triggerAction(
					agendaItem.getProtocolForm(),
					Committee.IRB_OFFICE,
					user,
					"REMOVED_FROM_AGENDA",
					null, null);
			
			agendaItem.setRetired(true);
			agendaItem.setAgendaItemStatus(AgendaItemStatus.REMOVED);
			
			agendaItem = agendaItemDao.saveOrUpdate(agendaItem);
			
			auditService.auditEvent("AGENDA_ITEM_REMOVED", "Agenda Item: ProtocolForm "+ agendaItem.getProtocolFormId() +" has been removed from agenda "+ agendaItem.getAgenda().getDate() +".");
			
		} catch (Exception e) {
			e.printStackTrace();
			return Boolean.FALSE;
		}
		return Boolean.TRUE;
	}

	@RequestMapping(value = "/ajax/agendas/{agendaId}/agenda-items/{agendaItemId}/create-letter", method = RequestMethod.POST)
	public @ResponseBody
	EmailTemplate loadEmailTemplateByMotion(@PathVariable("agendaId") long agendaId, @PathVariable("agendaItemId") long agendaItemId,
			@RequestParam("userId") long userId) {
		try {
			AgendaItem agendaItem = agendaItemDao.findById(agendaItemId);
			
			Agenda agenda = agendaItem.getAgenda();
			
			User user = userDao.findById(userId);
			
			String action = meetingService.generateActionByMotion(agendaItem.getProtocolForm().getProtocolFormType(), agenda.getMeetingXmlData(), agendaItemId);

			String emailIdentifier = businessObjectStatusHelperContainer
			.getBusinessObjectStatusHelper(agendaItem.getProtocolForm().getProtocolFormType().toString())
			.getEmailIdentifier(agendaItem.getProtocolForm(), Committee.IRB_REVIEWER, action);

			return  protocolEmailDataService.loadEmailTemplateInMeeting(emailIdentifier, agendaItem.getProtocolForm(), Committee.IRB_REVIEWER, null, user, "", agenda);
			
		} catch (Exception e) {
			e.printStackTrace();
			
			return null;
		}
	}
	
	@RequestMapping(value = "/ajax/agendas/{agendaId}/agenda-items/{agendaItemId}/send-letter", method = RequestMethod.POST)
	public @ResponseBody JsonResponse sendLetterByChair(@PathVariable("agendaId") long agendaId, @PathVariable("agendaItemId") long agendaItemId,
			@RequestParam("userId") long userId,
			@RequestParam("username") String username,
			@RequestParam("password") String password,
			@RequestParam("xmlData") String xmlData,
			ModelMap modelMap) throws Exception{
		boolean authenticated = userAuthenticationService.isAuthenticated(username, password);
		
		AgendaItem agendaItem = agendaItemDao.findById(agendaItemId);
		
		ProtocolForm protocolForm = agendaItem.getProtocolForm();
		
		User user = userDao.findById(userId);
		
		String action = meetingService.generateActionByMotion(protocolForm.getProtocolFormType(), agendaItem.getAgenda().getMeetingXmlData(), agendaItemId);
		
		if (authenticated) {
			protocolForm = meetingService.addLatestMotionToProtocolForm(protocolForm, agendaItem.getAgenda().getMeetingXmlData(), agendaItemId);
			
			businessObjectStatusHelperContainer
			.getBusinessObjectStatusHelper(protocolForm.getProtocolFormType().toString())
			.triggerAction(
					protocolForm,
					Committee.IRB_REVIEWER,
					user,
					action,
					null, xmlData);
			
			return new JsonResponse(false);
			
		} else {
			return new JsonResponse(true, "Your username or password is not correct!", "", false, null);
		}
		
	}
	
	@RequestMapping(value = "/ajax/agendas/agenda-items/search-by-protocol-id", method = RequestMethod.GET)
	public @ResponseBody
	List<AgendaItem> listAgendaItemsByIRB(
			@RequestParam("protocolId") long protocolId) {

		List<AgendaItem> agendaItems = null;
		try {
			agendaItems = agendaItemDao.listByProtocolId(protocolId);

		} catch (Exception e) {
			e.printStackTrace();

		}
		return agendaItems;
	}


	@Autowired(required = true)
	public void setAgendaItemDao(AgendaItemDao agendaItemDao) {
		this.agendaItemDao = agendaItemDao;
	}

	public AgendaItemDao getAgendaItemDao() {
		return agendaItemDao;
	}

	@Autowired(required = true)
	public void setAgendaItemReviewerDao(AgendaItemReviewerDao agendaItemReviewerDao) {
		this.agendaItemReviewerDao = agendaItemReviewerDao;
	}

	public AgendaItemReviewerDao getAgendaItemReviewerDao() {
		return agendaItemReviewerDao;
	}

	@Autowired(required = true)
	public void setProtocolFormDao(ProtocolFormDao protocolFormDao) {
		this.protocolFormDao = protocolFormDao;
	}
	
	public ProtocolFormDao getProtocolFormDao() {
		return protocolFormDao;
	}

	@Autowired(required = true)
	public void setProtocolFormXmlDataDao(ProtocolFormXmlDataDao protocolFormXmlDataDao) {
		this.protocolFormXmlDataDao = protocolFormXmlDataDao;
	}

	public ProtocolFormXmlDataDao getProtocolFormXmlDataDao() {
		return protocolFormXmlDataDao;
	}

	@Autowired(required = true)
	public void setProtocolFormStatusDao(ProtocolFormStatusDao protocolFormStatusDao) {
		this.protocolFormStatusDao = protocolFormStatusDao;
	}

	public ProtocolFormStatusDao getProtocolFormStatusDao() {
		return protocolFormStatusDao;
	}

	@Autowired(required = true)
	public void setBusinessObjectStatusHelperContainer(
			BusinessObjectStatusHelperContainer businessObjectStatusHelperContainer) {
		this.businessObjectStatusHelperContainer = businessObjectStatusHelperContainer;
	}

	public BusinessObjectStatusHelperContainer getBusinessObjectStatusHelperContainer() {
		return businessObjectStatusHelperContainer;
	}

	@Autowired(required = true)
	public void setAgendaDao(AgendaDao agendaDao) {
		this.agendaDao = agendaDao;
	}

	public AgendaDao getAgendaDao() {
		return agendaDao;
	}

	@Autowired(required = true)
	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}

	public UserDao getUserDao() {
		return userDao;
	}

	public MeetingService getMeetingService() {
		return meetingService;
	}
	
	@Autowired(required = true)
	public void setMeetingService(MeetingService meetingService) {
		this.meetingService = meetingService;
	}

	public ProtocolEmailDataService getProtocolEmailDataService() {
		return protocolEmailDataService;
	}
	
	@Autowired(required = true)
	public void setProtocolEmailDataService(ProtocolEmailDataService protocolEmailDataService) {
		this.protocolEmailDataService = protocolEmailDataService;
	}

	public UserAuthenticationService getUserAuthenticationService() {
		return userAuthenticationService;
	}
	
	@Autowired(required = true)
	public void setUserAuthenticationService(UserAuthenticationService userAuthenticationService) {
		this.userAuthenticationService = userAuthenticationService;
	}

	public ProtocolFormDetailContentService getProtocolFormDetailContentService() {
		return protocolFormDetailContentService;
	}
	
	@Autowired(required = true)
	public void setProtocolFormDetailContentService(
			ProtocolFormDetailContentService protocolFormDetailContentService) {
		this.protocolFormDetailContentService = protocolFormDetailContentService;
	}

	public AuditService getAuditService() {
		return auditService;
	}
	
	@Autowired(required = true)
	public void setAuditService(AuditService auditService) {
		this.auditService = auditService;
	}
	
}
