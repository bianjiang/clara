package edu.uams.clara.webapp.protocol.web.irb.agenda;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.joda.time.DateTime;
import org.joda.time.Minutes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.google.common.collect.Lists;

import edu.uams.clara.core.util.xml.XmlHandler;
import edu.uams.clara.core.util.xml.XmlHandlerFactory;
import edu.uams.clara.webapp.common.domain.security.MutexLock;
import edu.uams.clara.webapp.common.domain.usercontext.User;
import edu.uams.clara.webapp.common.domain.usercontext.enums.Committee;
import edu.uams.clara.webapp.common.domain.usercontext.enums.Permission;
import edu.uams.clara.webapp.common.security.MutexLockService;
import edu.uams.clara.webapp.protocol.dao.businesslogicobject.AgendaStatusDao;
import edu.uams.clara.webapp.protocol.dao.businesslogicobject.ProtocolFormCommitteeCommentDao;
import edu.uams.clara.webapp.protocol.dao.irb.AgendaDao;
import edu.uams.clara.webapp.protocol.dao.irb.AgendaItemDao;
import edu.uams.clara.webapp.protocol.dao.irb.IRBReviewerDao;
import edu.uams.clara.webapp.protocol.dao.protocolform.ProtocolFormDao;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.AgendaStatus;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.ProtocolFormCommitteeComment;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.enums.AgendaStatusEnum;
import edu.uams.clara.webapp.protocol.domain.irb.Agenda;
import edu.uams.clara.webapp.protocol.domain.irb.AgendaItem;
import edu.uams.clara.webapp.protocol.domain.irb.AgendaItem.AgendaItemCategory;
import edu.uams.clara.webapp.protocol.domain.irb.AgendaItemWrapper;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolForm;
import edu.uams.clara.webapp.protocol.domain.protocolform.enums.ProtocolFormType;
import edu.uams.clara.webapp.protocol.service.protocolform.impl.ProtocolFormReviewService;

@Controller
public class IRBAgendaController {

	private final static Logger logger = LoggerFactory
			.getLogger(IRBAgendaController.class);

	private ProtocolFormDao protocolFormDao;
	private IRBReviewerDao irbReviewerDao;
	private AgendaItemDao agendaItemDao;
	private AgendaDao agendaDao;
	private AgendaStatusDao agendaStatusDao;
	private ProtocolFormCommitteeCommentDao protocolFormCommitteeCommentDao;
	
	private MutexLockService mutexLockService;
	
	private ProtocolFormReviewService protocolFormReviewService;
	
	private static final int timeOutPeriod = 45;

	@RequestMapping(value = "/agendas", method = RequestMethod.GET)
	public String getAgendaIndex4(ModelMap modelMap) {

		User user = (User) SecurityContextHolder.getContext()
				.getAuthentication().getPrincipal();
		// logger.warn(""+irbReviewerDao.findByUserId(user.getId()));
		try{
			modelMap.put("irbreviewer", irbReviewerDao.findByUserId(user.getId()));
		} catch (Exception e){
			e.printStackTrace();
		}

		modelMap.put("user", user);
		return "agendas/index";
	}
	

	// TODO: Remove  after EXT4 testing complete
	@RequestMapping(value = "/agendas/{agendaId}/meeting4", method = RequestMethod.GET)
	public String getMeeting4(@PathVariable("agendaId") long agendaId,
			ModelMap modelMap) {
		User currentUser = (User) SecurityContextHolder.getContext()
				.getAuthentication().getPrincipal();

		Agenda agenda = agendaDao.findById(agendaId);
		
		MutexLock mutexLock = mutexLockService.getLockedByObjectClassAndId(
				Agenda.class, agendaId);

		Date currentDate = new Date();
		Date lastModifiedDate = (mutexLock != null) ? mutexLock.getModified()
				: null;
		DateTime currentDateTime = new DateTime(currentDate);
		DateTime lastModifiedDateTime = (mutexLock != null) ? new DateTime(
				lastModifiedDate) : null;

		String isLocked = "true";
		String isLockedUserString = "";
		long isLockedUserId = 0;

		if (!mutexLockService.isLockedByObjectClassAndIdForCurrentUser(
				Agenda.class, agendaId, currentUser)) {
			isLocked = "false";
		} else {
			if (mutexLock != null) {

				logger.debug("time period after last access: "
						+ Minutes.minutesBetween(lastModifiedDateTime,
								currentDateTime).getMinutes());
				if (Minutes.minutesBetween(lastModifiedDateTime,
						currentDateTime).getMinutes() > timeOutPeriod) {
					isLocked = "false";
				} else {
					isLocked = "true";
					isLockedUserString = mutexLock.getUser().getPerson()
							.getFullname();
					isLockedUserId = mutexLock.getUser().getId();
				}
			}
		}

		modelMap.put("user", (User) SecurityContextHolder.getContext()
				.getAuthentication().getPrincipal());
		modelMap.put("isLocked", isLocked);
		modelMap.put("isLockedUserString", isLockedUserString);
		modelMap.put("isLockedUserId", isLockedUserId);
		modelMap.put("agenda", agenda);
		return "agendas/meeting4";
	}
	// TODO: Remove  after EXT4 testing complete
	
	
	
	@RequestMapping(value = "/agendas/{agendaId}/meeting", method = RequestMethod.GET)
	public String getMeeting(@PathVariable("agendaId") long agendaId,
			ModelMap modelMap) {
		User currentUser = (User) SecurityContextHolder.getContext()
				.getAuthentication().getPrincipal();

		Agenda agenda = agendaDao.findById(agendaId);
		
		MutexLock mutexLock = mutexLockService.getLockedByObjectClassAndId(
				Agenda.class, agendaId);

		Date currentDate = new Date();
		Date lastModifiedDate = (mutexLock != null) ? mutexLock.getModified()
				: null;
		DateTime currentDateTime = new DateTime(currentDate);
		DateTime lastModifiedDateTime = (mutexLock != null) ? new DateTime(
				lastModifiedDate) : null;

		String isLocked = "true";
		String isLockedUserString = "";
		long isLockedUserId = 0;

		if (!mutexLockService.isLockedByObjectClassAndIdForCurrentUser(
				Agenda.class, agendaId, currentUser)) {
			isLocked = "false";
		} else {
			if (mutexLock != null) {

				logger.debug("time period after last access: "
						+ Minutes.minutesBetween(lastModifiedDateTime,
								currentDateTime).getMinutes());
				if (Minutes.minutesBetween(lastModifiedDateTime,
						currentDateTime).getMinutes() > timeOutPeriod) {
					isLocked = "false";
				} else {
					isLocked = "true";
					isLockedUserString = mutexLock.getUser().getPerson()
							.getFullname();
					isLockedUserId = mutexLock.getUser().getId();
				}
			}
		}

		modelMap.put("user", (User) SecurityContextHolder.getContext()
				.getAuthentication().getPrincipal());
		modelMap.put("isLocked", isLocked);
		modelMap.put("isLockedUserString", isLockedUserString);
		modelMap.put("isLockedUserId", isLockedUserId);
		modelMap.put("agenda", agenda);
		return "agendas/meeting";
	}

	@RequestMapping(value = "/agendas/{agendaId}/minutes", method = RequestMethod.GET)
	public String getMinutes(@PathVariable("agendaId") long agendaId,
			ModelMap modelMap) {

		Agenda agenda = agendaDao.findById(agendaId);
		
		List<AgendaItemWrapper> agendaItemList = agendaItemDao.listByAgendaId(agendaId);
		
		List<ProtocolFormCommitteeComment> agendaComments = new ArrayList<ProtocolFormCommitteeComment>();
		
		try{
			for (AgendaItemWrapper agendaItemWrapper : agendaItemList){
				List<Committee> committees = new ArrayList<Committee>();
				committees.add(Committee.IRB_CONSENT_REVIEWER);
				committees.add(Committee.IRB_EXEMPT_REVIEWER);
				committees.add(Committee.IRB_EXPEDITED_REVIEWER);
				committees.add(Committee.IRB_MEETING_OPERATOR);
				committees.add(Committee.IRB_CHAIR);
				committees.add(Committee.IRB_OFFICE);
				committees.add(Committee.IRB_PREREVIEW);
				committees.add(Committee.IRB_PROTOCOL_REVIEWER);
				committees.add(Committee.IRB_REVIEWER);
				
				List<ProtocolFormCommitteeComment> protocolFormCommitteeComments = protocolFormCommitteeCommentDao.listAllCommentsByProtocolFormIdAndCommittees(agendaItemWrapper.getProtocolFormId(), committees);
				for (ProtocolFormCommitteeComment pc : protocolFormCommitteeComments){
					if (pc.getId() == pc.getReplyToId()) {
						agendaComments.add(pc);// ignore replies to comments, only add IRB comments
					}
				}
				
			}
		} catch (Exception e){
			e.printStackTrace();
		}
		
		
		modelMap.put("user", (User) SecurityContextHolder.getContext()
				.getAuthentication().getPrincipal());
		modelMap.put("agenda", agenda);
		modelMap.put("agendaItems",agendaItemList);
		modelMap.put("minutes", agenda.getMeetingXmlData());
		modelMap.put("agendaComments", agendaComments);
		modelMap.put("irbReviewerDao", irbReviewerDao);
		modelMap.put("protocolFormCommitteeCommentDao",
				protocolFormCommitteeCommentDao);
		modelMap.put("protocolFormDao", protocolFormDao);

		return "agendas/minutes";
	}

	@RequestMapping(value = "/agendas/{agendaId}/summary", method = RequestMethod.GET)
	public String listAgendaItemsByDate(
			@PathVariable("agendaId") long agendaId, ModelMap modelMap) {
		Agenda agenda = agendaDao.findById(agendaId);
		List<AgendaItem> agendaItems = agendaItemDao.listByAgendaIdNoWrapper(agendaId);
		
		List<AgendaItem> auditList = Lists.newArrayList();
		List<AgendaItem> rniList = Lists.newArrayList();
		List<AgendaItem> hudList = Lists.newArrayList();
		List<AgendaItem> newSubAndFullBoardList = Lists.newArrayList();
		List<AgendaItem> contiRevAndFullBoardList = Lists.newArrayList();
		List<AgendaItem> modiAndFullBoardList = Lists.newArrayList();
		List<AgendaItem> pbList = Lists.newArrayList();
		List<AgendaItem> oaList = Lists.newArrayList();
		List<AgendaItem> respMajorList = Lists.newArrayList();
		//List<AgendaItem> newSubAndExpedList = new ArrayList<AgendaItem>();
		//List<AgendaItem> contiRevAndExpedList = new ArrayList<AgendaItem>();
		
		//List<AgendaItem> respMinorList = new ArrayList<AgendaItem>();
		//List<AgendaItem> studyCloList = new ArrayList<AgendaItem>();
		List<AgendaItem> reportedItemList = Lists.newArrayList();
		/*List<AgendaItem> ModiAndReportedList = Lists.newArrayList();
		List<AgendaItem> rniAndReportedList = Lists.newArrayList();
		List<AgendaItem> newSubAndReportedList = Lists.newArrayList();
		List<AgendaItem> contiRevAndReportedList = Lists.newArrayList();*/
		List<AgendaItem> minutsList = Lists.newArrayList();
		
		String xmlData = "";
		
		String metaData = "";
		
		XmlHandler xmlHanlder = null;
		
		try {
			xmlHanlder = XmlHandlerFactory.newXmlHandler();
		} catch (Exception e) {
			e.printStackTrace();
		}

		for (int i = 0; i < agendaItems.size(); i++) {

			ProtocolForm protocolForm = protocolFormDao.findById(agendaItems.get(i).getProtocolFormId());
			
			if (protocolForm != null){
				xmlData = protocolForm.getMetaDataXml();
				metaData = protocolForm.getObjectMetaData();
			}
			
			String reponse = "";
			
			try {
				reponse = (!metaData.isEmpty())?xmlHanlder.getSingleStringValueByXPath(metaData, "/protocol/summary/irb-determination/recent-motion"):"";
			} catch (Exception e) {
				
			}

			if (reponse.equals("Defer with major contingencies")) {
				respMajorList.add(agendaItems.get(i));
			}
			else if (agendaItems.get(i).getAgendaItemCategory().compareTo(AgendaItemCategory.MINUTES) == 0){
				minutsList.add(agendaItems.get(i));
			}
			else if (agendaItems.get(i).getAgendaItemCategory()
					.compareTo(AgendaItemCategory.FULL_BOARD) == 0) {
				if (agendaItems.get(i).getProtocolForm()
						.getProtocolFormType()
						.compareTo(ProtocolFormType.NEW_SUBMISSION) == 0) {
					try {
						String hud = (!xmlData.isEmpty())?xmlHanlder.getSingleStringValueByXPath(xmlData, "/protocol/study-nature"):"";
						
						if (hud.equals("hud-use")) {
							hudList.add(agendaItems.get(i));
						} else {
							newSubAndFullBoardList.add(agendaItems.get(i));
						}
					} catch (Exception e) {
						
					}
				}
				else if (agendaItems.get(i).getProtocolForm()
						.getProtocolFormType()
						.compareTo(ProtocolFormType.CONTINUING_REVIEW) == 0) {
					contiRevAndFullBoardList.add(agendaItems.get(i));
				}
				else if (agendaItems.get(i).getProtocolForm()
						.getProtocolFormType()
						.compareTo(ProtocolFormType.MODIFICATION) == 0) {
					try {
						String audit = (!xmlData.isEmpty())?xmlHanlder.getSingleStringValueByXPath(xmlData, "/protocol/modification/to-modify-section/is-audit"):"";
						
						if (audit.equals("y")) {
							auditList.add(agendaItems.get(i));
						} else {
							modiAndFullBoardList.add(agendaItems.get(i));
						}
					} catch (Exception e) {
						
					}
				}
				else if (agendaItems.get(i).getProtocolForm().getProtocolFormType()
						.compareTo(ProtocolFormType.REPORTABLE_NEW_INFORMATION) == 0) {
					rniList.add(agendaItems.get(i));
				}
				else if (agendaItems.get(i).getProtocolForm().getProtocolFormType()
						.compareTo(ProtocolFormType.PRIVACY_BOARD) == 0) {
					pbList.add(agendaItems.get(i));
				}
				else if (agendaItems.get(i).getProtocolForm().getProtocolFormType()
						.compareTo(ProtocolFormType.OFFICE_ACTION) == 0) {
					oaList.add(agendaItems.get(i));
				}
			}

			else if (agendaItems.get(i).getAgendaItemCategory()
					.compareTo(AgendaItemCategory.REPORTED) == 0) {
				reportedItemList.add(agendaItems.get(i));
				/*if (agendaItems.get(i).getProtocolForm()
						.getProtocolFormType()
						.compareTo(ProtocolFormType.NEW_SUBMISSION) == 0) {
					newSubAndReportedList.add(agendaItems.get(i));
				}
				else if (agendaItems.get(i).getProtocolForm()
						.getProtocolFormType()
						.compareTo(ProtocolFormType.CONTINUING_REVIEW) == 0) {
					contiRevAndReportedList.add(agendaItems.get(i));
				}
				else if (agendaItems.get(i).getProtocolForm()
						.getProtocolFormType()
						.compareTo(ProtocolFormType.MODIFICATION) == 0) {
					ModiAndReportedList.add(agendaItems.get(i));
				}
				else if (agendaItems.get(i).getProtocolForm().getProtocolFormType()
						.compareTo(ProtocolFormType.REPORTABLE_NEW_INFORMATION) == 0) {
					rniAndReportedList.add(agendaItems.get(i));
				}*/
			}
		}

		@SuppressWarnings("rawtypes")
		List<List> allList = new ArrayList<List>();
		List<String> headList = new ArrayList<String>();
		headList.add("leave for index");
		headList.add("Audits");
		headList.add("Reportable New Information");
		headList.add("New Submissions");
		headList.add("Humanitarian User Device");
		headList.add("Continuing Reviews");
		headList.add("Modifications");
		headList.add("Privacy Board");
		headList.add("Office Action");
		headList.add("Responses to Major Contingencies");
		headList.add("Minutes");
		headList.add("Reported Items");
		/*headList.add("Reportable New Information and Reported");
		headList.add("New Submissions and Reported");
		headList.add("Continuing Reviews and Reported");
		headList.add("Modifications and Reported");*/
		
		allList.add(auditList);
		allList.add(rniList);
		allList.add(newSubAndFullBoardList);
		allList.add(hudList);
		allList.add(contiRevAndFullBoardList);
		allList.add(modiAndFullBoardList);
		allList.add(pbList);
		allList.add(oaList);
		allList.add(respMajorList);
		allList.add(minutsList);
		allList.add(reportedItemList);
		/*allList.add(rniAndReportedList);
		allList.add(newSubAndReportedList);
		allList.add(contiRevAndReportedList);
		allList.add(ModiAndReportedList);*/

		modelMap.put("agenda", agenda);
		modelMap.put("headList", headList);
		modelMap.put("allList", allList);
		modelMap.put("irbReviewerDao", irbReviewerDao);

		return "agendas/summary";

	}
	
	private List<AgendaStatusEnum> beforeMeetingStartStatusLst = Lists.newArrayList();{
		beforeMeetingStartStatusLst.add(AgendaStatusEnum.AGENDA_APPROVED);
		beforeMeetingStartStatusLst.add(AgendaStatusEnum.AGENDA_INCOMPLETE);
		beforeMeetingStartStatusLst.add(AgendaStatusEnum.AGENDA_PENDING_CHAIR_APPROVAL);
	}

	@RequestMapping(value = "/agendas/{agendaId}/agenda-items/{agendaItemId}/view", method = RequestMethod.GET)
	public String getAgendaItem(@PathVariable("agendaId") long agendaId,
			@PathVariable("agendaItemId") long agendaItemId, ModelMap modelMap) {
		User currentUser = (User) SecurityContextHolder.getContext()
				.getAuthentication().getPrincipal();
		
		Agenda agenda = agendaDao.findById(agendaId);
		
		AgendaStatus latestAgendaStatus = agendaStatusDao.getAgendaStatusByAgendaId(agendaId);
		
		boolean readOnly = false;
		
		if (currentUser.getAuthorities().contains(Permission.VIEW_AGENDA_ONLY)){
			readOnly = true;
		} else {
			if (beforeMeetingStartStatusLst.contains(latestAgendaStatus.getAgendaStatus())){
				readOnly = false;
			} else {
				readOnly = true;
			}
		}

		AgendaItem agendaItem = agendaItemDao.findById(agendaItemId);
		
		Set<Permission> objectPermissions = protocolFormReviewService.getObjectPermissions(agendaItem.getProtocolForm(), currentUser, Committee.IRB_REVIEWER);
		
		if (!currentUser.getAuthorities().contains(Permission.ROLE_IRB_CHAIR)) {
			if (latestAgendaStatus.getAgendaStatus().isAgendaApproved()) {
				Boolean isThisWeekIRBRoster = irbReviewerDao.checkIfOnIRBRosterByIRBRosterAndUserId(agenda.getIrbRoster(), currentUser.getId());

				if (!isThisWeekIRBRoster) {
					objectPermissions.remove(Permission.COMMENT_CAN_ADD);
					objectPermissions.remove(Permission.CONTINGENCY_CAN_ADD);
				} else {
					Boolean isAssignedReviewer = irbReviewerDao.checkIfIRBRosterIsAssignedToAgendaItem(agendaItemId, currentUser.getId());

					if (isAssignedReviewer) {
						objectPermissions.remove(Permission.COMMENT_CAN_ADD);
						objectPermissions.remove(Permission.CONTINGENCY_CAN_ADD);
					} else {
						objectPermissions.remove(Permission.CONTINGENCY_CAN_ADD);
					}
				}
			} else {
				objectPermissions.remove(Permission.COMMENT_CAN_ADD);
				objectPermissions.remove(Permission.CONTINGENCY_CAN_ADD);
			}
		}

		modelMap.put("user", currentUser);
		modelMap.put("agendaItem", agendaItem);
		modelMap.put("readOnly", readOnly);
		modelMap.put("objectPermissions", objectPermissions);

		return "agendas/agenda-item";
	}

	@Autowired(required = true)
	public void setProtocolFormDao(ProtocolFormDao protocolFormDao) {
		this.protocolFormDao = protocolFormDao;
	}

	public ProtocolFormDao getProtocolFormDao() {
		return protocolFormDao;
	}

	@Autowired(required = true)
	public void setAgendaItemDao(AgendaItemDao agendaItemDao) {
		this.agendaItemDao = agendaItemDao;
	}

	public AgendaDao getAgendaDao() {
		return agendaDao;
	}

	@Autowired(required = true)
	public void setAgendaDao(AgendaDao agendaDao) {
		this.agendaDao = agendaDao;
	}

	public AgendaItemDao getAgendaItemDao() {
		return agendaItemDao;
	}

	@Autowired(required = true)
	public void setIrbReviewerDao(IRBReviewerDao irbReviewerDao) {
		this.irbReviewerDao = irbReviewerDao;
	}

	public IRBReviewerDao getIrbReviewerDao() {
		return irbReviewerDao;
	}

	public ProtocolFormCommitteeCommentDao getProtocolFormCommitteeCommentDao() {
		return protocolFormCommitteeCommentDao;
	}

	@Autowired(required = true)
	public void setProtocolFormCommitteeCommentDao(
			ProtocolFormCommitteeCommentDao protocolFormCommitteeCommentDao) {
		this.protocolFormCommitteeCommentDao = protocolFormCommitteeCommentDao;
	}

	public MutexLockService getMutexLockService() {
		return mutexLockService;
	}
	
	@Autowired(required = true)
	public void setMutexLockService(MutexLockService mutexLockService) {
		this.mutexLockService = mutexLockService;
	}

	public AgendaStatusDao getAgendaStatusDao() {
		return agendaStatusDao;
	}
	
	@Autowired(required = true)
	public void setAgendaStatusDao(AgendaStatusDao agendaStatusDao) {
		this.agendaStatusDao = agendaStatusDao;
	}


	public ProtocolFormReviewService getProtocolFormReviewService() {
		return protocolFormReviewService;
	}

	@Autowired(required = true)
	public void setProtocolFormReviewService(ProtocolFormReviewService protocolFormReviewService) {
		this.protocolFormReviewService = protocolFormReviewService;
	}

}
