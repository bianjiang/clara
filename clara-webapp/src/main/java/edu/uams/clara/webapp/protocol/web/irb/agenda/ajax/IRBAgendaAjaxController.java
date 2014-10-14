package edu.uams.clara.webapp.protocol.web.irb.agenda.ajax;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.common.collect.Lists;

import edu.uams.clara.webapp.common.businesslogic.BusinessObjectStatusHelperContainer;
import edu.uams.clara.webapp.common.dao.usercontext.UserDao;
import edu.uams.clara.webapp.common.domain.email.EmailTemplate;
import edu.uams.clara.webapp.common.domain.history.Track;
import edu.uams.clara.webapp.common.domain.security.MutexLock;
import edu.uams.clara.webapp.common.domain.usercontext.User;
import edu.uams.clara.webapp.common.domain.usercontext.enums.Committee;
import edu.uams.clara.webapp.common.domain.usercontext.enums.Permission;
import edu.uams.clara.webapp.common.security.MutexLockService;
import edu.uams.clara.webapp.common.service.UserService;
import edu.uams.clara.webapp.common.service.audit.AuditService;
import edu.uams.clara.webapp.common.util.DateFormatUtil;
import edu.uams.clara.webapp.common.util.JsonResponseHelper;
import edu.uams.clara.webapp.common.util.response.JsonResponse;
import edu.uams.clara.webapp.protocol.businesslogic.irb.agenda.AgendaStatusHelper;
import edu.uams.clara.webapp.protocol.businesslogic.irb.agenda.AgendaStatusHelper.AgendaStatusChangeAction;
import edu.uams.clara.webapp.protocol.dao.businesslogicobject.AgendaStatusDao;
import edu.uams.clara.webapp.protocol.dao.irb.AgendaDao;
import edu.uams.clara.webapp.protocol.dao.irb.AgendaIRBReviewerDao;
import edu.uams.clara.webapp.protocol.dao.irb.AgendaItemDao;
import edu.uams.clara.webapp.protocol.dao.irb.IRBReviewerDao;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.enums.AgendaStatusEnum;
import edu.uams.clara.webapp.protocol.domain.irb.Agenda;
import edu.uams.clara.webapp.protocol.domain.irb.AgendaIRBReviewer;
import edu.uams.clara.webapp.protocol.domain.irb.AgendaIRBReviewer.AgendaIRBReviewerStatus;
import edu.uams.clara.webapp.protocol.domain.irb.AgendaItem.AgendaItemCategory;
import edu.uams.clara.webapp.protocol.domain.irb.AgendaItem.AgendaItemStatus;
import edu.uams.clara.webapp.protocol.domain.irb.AgendaItem;
import edu.uams.clara.webapp.protocol.domain.irb.AgendaItemWrapper;
import edu.uams.clara.webapp.protocol.domain.irb.IRBReviewer;
import edu.uams.clara.webapp.protocol.domain.irb.enums.IRBRoster;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolForm;
import edu.uams.clara.webapp.protocol.service.email.ProtocolEmailDataService;
import edu.uams.clara.webapp.protocol.service.email.ProtocolEmailService;
import edu.uams.clara.webapp.protocol.service.history.ProtocolTrackService;
import edu.uams.clara.webapp.xml.processor.XmlProcessor;

@Controller
public class IRBAgendaAjaxController {

	private final static Logger logger = LoggerFactory
			.getLogger(IRBAgendaAjaxController.class);

	private AgendaDao agendaDao;
	
	private AgendaItemDao agendaItemDao;

	private UserDao userDao;

	private IRBReviewerDao irbReviewerDao;

	private AgendaIRBReviewerDao agendaIRBReviewerDao;
	
	private AgendaStatusDao agendaStatusDao;
	
	private BusinessObjectStatusHelperContainer businessObjectStatusHelperContainer;
	
	private ProtocolEmailDataService protocolEmailDataService;
	
	private ProtocolEmailService protocolEmailService;
	
	private AuditService auditService;
	
	private UserService userService;
	
	private MutexLockService mutexLockService;
	
	private ProtocolTrackService protocolTrackService;
	
	private XmlProcessor xmlProcessor;

	private List<AgendaStatusEnum> availableAgendaStatuses = new ArrayList<AgendaStatusEnum>(
			0);
	{
		availableAgendaStatuses.add(AgendaStatusEnum.AGENDA_INCOMPLETE);
		availableAgendaStatuses.add(AgendaStatusEnum.AGENDA_PENDING_CHAIR_APPROVAL);
		availableAgendaStatuses.add(AgendaStatusEnum.AGENDA_APPROVED);
	}
	
	private List<AgendaStatusEnum> approvedAgendaStatuses = Lists.newArrayList();{
		approvedAgendaStatuses.add(AgendaStatusEnum.AGENDA_APPROVED);
		approvedAgendaStatuses.add(AgendaStatusEnum.MEETING_ADJOURNED);
		approvedAgendaStatuses.add(AgendaStatusEnum.MEETING_ADJOURNED_PENDING_CHAIR_APPROVAL);
		approvedAgendaStatuses.add(AgendaStatusEnum.MEETING_ADJOURNED_PENDING_IRB_OFFICE_PROCESS);
		approvedAgendaStatuses.add(AgendaStatusEnum.MEETING_CLOSED);
		approvedAgendaStatuses.add(AgendaStatusEnum.MEETING_IN_PROGRESS);
	}

	private AgendaStatusHelper agendaStatusHelper;
	
	private void unlockMeeting(long agendaId, User currentUser) {
		try {
			MutexLock mutexLock = mutexLockService.getLockedByObjectClassAndId(
					Agenda.class, agendaId);

			if (mutexLock != null) {
				if (mutexLock.getUser().getId() == currentUser.getId()) {
					mutexLockService.unlockMutexLock(mutexLock);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	@RequestMapping(value = "/ajax/agendas/list", method = RequestMethod.GET)
	public @ResponseBody
	JsonResponse listAgendas() {
		User user = (User) SecurityContextHolder.getContext()
				.getAuthentication().getPrincipal();
		
		try {
			if (user.getAuthorities().contains(Permission.EDIT_AGENDA) || user.getAuthorities().contains(Permission.VIEW_AGENDA_ONLY)){
				List<Agenda> agendas = Lists.newArrayList();
				
				if (user.getAuthorities().contains(Permission.ROLE_IRB_REVIEWER) && !user.getAuthorities().contains(Permission.ROLE_IRB_CHAIR)) {
					agendas = agendaDao.listAgendasByStatuses(approvedAgendaStatuses);
				} else {
					agendas = agendaDao.listAllAgendas();
				}
				
				return new JsonResponse(false, agendas);
			} else {
				return new JsonResponse(true, "You don't have the right to edit agenda!", "", false, null);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return new JsonResponse(true, "Failed to load agenda list!", "", false, null);
		}
	}

	@RequestMapping(value = "/ajax/agendas/list-available", method = RequestMethod.GET)
	public @ResponseBody
	List<Agenda> listAvailableAgendas() {

		List<Agenda> agendas = agendaDao
				.listAgendasByStatuses(availableAgendaStatuses);

		return agendas;
	}
	
	@RequestMapping(value = "/ajax/agendas/{agendaId}/unlock", method = RequestMethod.GET)
	public String unlockMeeting(
			@PathVariable("agendaId") long agendaId) {
		User currentUser = userService.getCurrentUser();

		unlockMeeting(agendaId, currentUser);

		return "redirect:/agendas";
	}

	@RequestMapping(value = "/ajax/agendas/{agendaId}/irb-reviewers/list", method = RequestMethod.GET)
	public @ResponseBody
	List<IRBReviewer> listAssignableAgendaIRBReviewers(
			@PathVariable("agendaId") long agendaId) {

		List<AgendaIRBReviewer> agendaIRBReviewers = agendaIRBReviewerDao
				.listAgendaIRBReviewersByAgendaId(agendaId);
		List<IRBReviewer> irbReviewers = new ArrayList<IRBReviewer>();

		for (AgendaIRBReviewer agendaIRBReviewer : agendaIRBReviewers) {
			switch (agendaIRBReviewer.getStatus()) {
			case NORMAL:
				irbReviewers.add(agendaIRBReviewer.getIrbReviewer());
				break;
			case ADDITIONAL:
				irbReviewers.add(agendaIRBReviewer.getIrbReviewer());
				break;
			case REPLACED:
				irbReviewers.add(agendaIRBReviewer.getAlternateIRBReviewer());
				break;
			default:
				break;
			}
		}
		return irbReviewers;
	}
	
	@RequestMapping(value="/ajax/agendas/{agendaId}/agenda-irb-reviewers/{agendaIRBReviewerId}/assign-alternate")
	public @ResponseBody
	AgendaIRBReviewer assignAlternateIRBReviewer(
			@PathVariable("agendaId") long agendaId,
			@PathVariable("agendaIRBReviewerId") long agendaIRBReviewerId,
			@RequestParam("alternateIRBReviewerId") long alternateIRBReviewerId,
			@RequestParam("reason") String reason,
			@RequestParam("userId") long userId){
		
		AgendaIRBReviewer agendaIRBReviewer = agendaIRBReviewerDao.findById(agendaIRBReviewerId);
		IRBReviewer alternateIRBReviewer = irbReviewerDao.findById(alternateIRBReviewerId);
				
		agendaIRBReviewer.setAlternateIRBReviewer(alternateIRBReviewer);
		agendaIRBReviewer.setReason(reason);
		agendaIRBReviewer.setStatus(AgendaIRBReviewerStatus.REPLACED);
		
		agendaIRBReviewer = agendaIRBReviewerDao.saveOrUpdate(agendaIRBReviewer);
		
		return agendaIRBReviewer;
		
	}
	
	@RequestMapping(value="/ajax/agendas/{agendaId}/agenda-irb-reviewers/{agendaIRBReviewerId}/remove-alternate")
	public @ResponseBody
	AgendaIRBReviewer removeAlternateIRBReviewer(
			@PathVariable("agendaId") long agendaId,
			@PathVariable("agendaIRBReviewerId") long agendaIRBReviewerId,
			@RequestParam("userId") long userId){
		
		AgendaIRBReviewer agendaIRBReviewer = agendaIRBReviewerDao.findById(agendaIRBReviewerId);
		
		agendaIRBReviewer.setAlternateIRBReviewer(null);
		agendaIRBReviewer.setStatus(AgendaIRBReviewerStatus.NORMAL);
		
		agendaIRBReviewer = agendaIRBReviewerDao.saveOrUpdate(agendaIRBReviewer);
		
		return agendaIRBReviewer;
		
	}
	
	@RequestMapping(value="/ajax/agendas/{agendaId}/agenda-irb-reviewers/{agendaIRBReviewerId}/remove")
	public @ResponseBody
	AgendaIRBReviewer removeIRBReviewer(
			@PathVariable("agendaId") long agendaId,
			@PathVariable("agendaIRBReviewerId") long agendaIRBReviewerId,
			@RequestParam("reason") String reason,
			@RequestParam("userId") long userId){
		
		AgendaIRBReviewer agendaIRBReviewer = agendaIRBReviewerDao.findById(agendaIRBReviewerId);
		
		agendaIRBReviewer.setReason(reason);
		agendaIRBReviewer.setStatus(AgendaIRBReviewerStatus.REMOVED);
		
		agendaIRBReviewer = agendaIRBReviewerDao.saveOrUpdate(agendaIRBReviewer);
		
		return agendaIRBReviewer;
		
	}
	
	@RequestMapping(value="/ajax/agendas/{agendaId}/agenda-irb-reviewers/add-additional")
	public @ResponseBody
	AgendaIRBReviewer addAdditionalIRBReviewer(
			@PathVariable("agendaId") long agendaId,
			@RequestParam("irbReviewerId") long irbReviewerId,
			@RequestParam("reason") String reason,
			@RequestParam("userId") long userId){
		
		AgendaIRBReviewer agendaIRBReviewer = new AgendaIRBReviewer();
		Agenda agenda = agendaDao.findById(agendaId);
		IRBReviewer irbReviewer = irbReviewerDao.findById(irbReviewerId);
		
		
		agendaIRBReviewer.setAgenda(agenda);
		agendaIRBReviewer.setIrbReviewer(irbReviewer);
		agendaIRBReviewer.setReason(reason);
		agendaIRBReviewer.setStatus(AgendaIRBReviewerStatus.ADDITIONAL);
		
		agendaIRBReviewer = agendaIRBReviewerDao.saveOrUpdate(agendaIRBReviewer);
		
		return agendaIRBReviewer;
		
	}
	
	@RequestMapping(value="/ajax/agendas/{agendaId}/agenda-irb-reviewers/add")
	public @ResponseBody
	AgendaIRBReviewer addIRBReviewer(
			@PathVariable("agendaId") long agendaId,
			@RequestParam("agendaIRBReviewerId") long agendaIRBReviewerId,
			@RequestParam("userId") long userId){
		
		AgendaIRBReviewer agendaIRBReviewer = agendaIRBReviewerDao.findById(agendaIRBReviewerId);
		agendaIRBReviewer.setStatus(AgendaIRBReviewerStatus.NORMAL);
		agendaIRBReviewer = agendaIRBReviewerDao.saveOrUpdate(agendaIRBReviewer);
		return agendaIRBReviewer;
		
	}
	
	@RequestMapping(value="/ajax/agendas/{agendaId}/agenda-irb-reviewers/delete")
	public @ResponseBody
	AgendaIRBReviewer deleteIRBReviewer(
			@PathVariable("agendaId") long agendaId,
			@RequestParam("agendaIRBReviewerId") long agendaIRBReviewerId,
			@RequestParam("userId") long userId){
		
		AgendaIRBReviewer agendaIRBReviewer = agendaIRBReviewerDao.findById(agendaIRBReviewerId);
		// agendaIRBReviewer.setStatus(AgendaIRBReviewerStatus.NORMAL);
		agendaIRBReviewer.setRetired(true);
		agendaIRBReviewer = agendaIRBReviewerDao.saveOrUpdate(agendaIRBReviewer);
		return agendaIRBReviewer;
		
	}
	
	
	
	@RequestMapping(value = "/ajax/agendas/{agendaId}/agenda-irb-reviewers/list", method = RequestMethod.GET)
	public @ResponseBody
	List<AgendaIRBReviewer> listAgendaIRBReviewers(
			@PathVariable("agendaId") long agendaId) {

		return agendaIRBReviewerDao.listAgendaIRBReviewersByAgendaId(agendaId);
	}

	@RequestMapping(value = "/ajax/agendas/{agendaId}/irb-rosters/assign", method = RequestMethod.GET)
	public @ResponseBody
	Agenda updateAgendaIRBRoster(
			@PathVariable("agendaId") long agendaId,
			@RequestParam(value = "irbRoster", required = false) IRBRoster irbRoster) {
		Agenda agenda = null;

		try {
			agenda = agendaDao.findById(agendaId);
			agenda.setIrbRoster(irbRoster);

			agendaDao.saveOrUpdate(agenda);

		} catch (Exception e) {
			e.printStackTrace();
		}

		return agenda;
	}

	@RequestMapping(value = "/ajax/agendas/create", method = RequestMethod.GET)
	public @ResponseBody
	JsonResponse createAgenda(
			@RequestParam(value = "agendaDate") Date agendaDate,
			@RequestParam(value = "irbRoster", required = false) IRBRoster irbRoster,
			@RequestParam(value = "userId") long userId) {
		Agenda existingAgenda = null;
		
		try{
			existingAgenda = agendaDao.findByDate(agendaDate);
		} catch (Exception e){
			//e.printStackTrace();
			logger.warn("No existing agenda found!");
		} 
		
		if (existingAgenda != null){
			return new JsonResponse(true, "This agenda has already been created!  Please select another date!", null, false);
		}

		Agenda agenda = new Agenda();
		agenda.setDate(agendaDate);
		agenda.setIrbRoster(irbRoster);
		agenda = agendaDao.saveOrUpdate(agenda);

		User user = userDao.findById(userId);

		agendaStatusHelper.logStatus(agenda, user, null,
				AgendaStatusChangeAction.CREATE);

		List<IRBReviewer> irbReviewers = irbReviewerDao
				.listIRBReviewersByIRBRoster(agenda.getIrbRoster());

		AgendaIRBReviewer agendaIRBReviewer = null;

		for (IRBReviewer irbReviewer : irbReviewers) {
			agendaIRBReviewer = new AgendaIRBReviewer();
			agendaIRBReviewer.setAgenda(agenda);
			agendaIRBReviewer.setIrbReviewer(irbReviewer);
			agendaIRBReviewer.setStatus(AgendaIRBReviewerStatus.NORMAL);

			agendaIRBReviewerDao.saveOrUpdate(agendaIRBReviewer);
		}

		return new JsonResponse(false, agenda);
	}

	/**
	 * need to do replace element rather than simply set XmlData
	 * 
	 * @param agendaId
	 * @param reason
	 * @param userId
	 * @return
	 */
	@RequestMapping(value = "/ajax/agendas/{agendaId}/cancel", method = RequestMethod.POST)
	public @ResponseBody
	Boolean cancelAgenda(@PathVariable("agendaId") long agendaId,
			@RequestParam("reason") String reason,
			@RequestParam("userId") long userId) {

		try {
			Agenda agenda = agendaDao.findById(agendaId);
			User user = userDao.findById(userId);

			agendaStatusHelper.logStatus(agenda, user, reason,
					AgendaStatusChangeAction.CANCEL);
			
			List<AgendaItemWrapper> agendaItems = agendaItemDao.listByAgendaId(agendaId);
			
			if (agendaItems != null && !agendaItems.isEmpty()){
				for (AgendaItemWrapper agendaItemWrapper : agendaItems){
					businessObjectStatusHelperContainer
					.getBusinessObjectStatusHelper("AGENDA_ITEM")
					.triggerAction(
							agendaItemWrapper.getProtocolForm(),
							Committee.IRB_OFFICE,
							user,
							"REMOVED_FROM_AGENDA",
							null, null);
					
					AgendaItem agendaItem = agendaItemDao.findById(agendaItemWrapper.getId());
					agendaItem.setAgendaItemStatus(AgendaItemStatus.REMOVED);
					
					agendaItem = agendaItemDao.saveOrUpdate(agendaItem);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			return Boolean.FALSE;
		}
		return Boolean.TRUE;
	}
	
	@RequestMapping(value = "/ajax/agendas/{agendaId}/meeting-start", method = RequestMethod.GET)
	public @ResponseBody
	Boolean startMeeting(@PathVariable("agendaId") long agendaId,
			@RequestParam("userId") long userId) {

		try {
			Agenda agenda = agendaDao.findById(agendaId);
			User user = userDao.findById(userId);

			agendaStatusHelper.logStatus(agenda, user, null,
					AgendaStatusChangeAction.START_MEETING);
			return Boolean.TRUE;

		} catch (Exception e) {
			e.printStackTrace();
			return Boolean.FALSE;
		}
	}
	
	@RequestMapping(value = "/ajax/agendas/{agendaId}/meeting-stop", method = RequestMethod.GET)
	public @ResponseBody
	Boolean stopMeeting(@PathVariable("agendaId") long agendaId,
			@RequestParam("userId") long userId) {

		try {
			Agenda agenda = agendaDao.findById(agendaId);
			User user = userDao.findById(userId);

			agendaStatusHelper.logStatus(agenda, user, null,
					AgendaStatusChangeAction.STOP_MEETING);
			return Boolean.TRUE;

		} catch (Exception e) {
			e.printStackTrace();
			return Boolean.FALSE;
		}
	}

	/**
	 * need to do replace element rather than simply set XmlData
	 * 
	 * @param agendaId
	 * @param reason
	 * @param userId
	 * @return
	 */
	@RequestMapping(value = "/ajax/agendas/{agendaId}/send-for-approval", method = RequestMethod.POST)
	public @ResponseBody
	Boolean sendForApprovalAgenda(@PathVariable("agendaId") long agendaId,
			@RequestParam("userId") long userId,
			@RequestParam(value = "reason", required = false) String reason) {
		User currentUser = userDao.findById(userId);

		try {
			Agenda agenda = agendaDao.findById(agendaId);

			User user = userDao.findById(userId);
			
			boolean iSIRBChair = irbReviewerDao.checkIfIRBChairByIRBRosterAndUserId(agenda.getIrbRoster(), userId);
			
			if (iSIRBChair) {
				agendaStatusHelper.logStatus(agenda, currentUser, null,
						AgendaStatusChangeAction.APPROVE);
				
				List<AgendaItem> agendaItems = agendaItemDao.listByAgendaIdAndCategory(agendaId, AgendaItemCategory.FULL_BOARD);
				
				for (AgendaItem agendaItem : agendaItems) {
					ProtocolForm protocolForm = agendaItem.getProtocolForm();
					
					Track track = protocolTrackService.getOrCreateTrack("PROTOCOL",
							protocolForm.getProtocol().getId());

					Document logsDoc = protocolTrackService.getLogsDocument(track);

					Element logEl = logsDoc.createElement("log");
		      
					String logId = UUID.randomUUID().toString();
		      
					Date now = new Date();
		            logEl.setAttribute("id", logId);
		            logEl.setAttribute("parent-id", logId);
		            logEl.setAttribute("action-user-id", String.valueOf(currentUser.getId()));
		            logEl.setAttribute("actor", currentUser.getUsername());
		            logEl.setAttribute("date-time", DateFormatUtil.formateDate(now));
		            logEl.setAttribute("event-type", "AGENDA_APPROVAL");
		            logEl.setAttribute("form-id", String.valueOf(protocolForm.getId()));
		            logEl.setAttribute("parent-form-id", String.valueOf(protocolForm.getParentFormId()));
		            logEl.setAttribute("form-type", protocolForm.getProtocolFormType().toString());
		            logEl.setAttribute("log-type", "ACTION");
		            logEl.setAttribute("timestamp", String.valueOf(now.getTime()));
		            
		            String message = "" + DateFormatUtil.formateDateToMDY(agenda.getDate()) + " agenda has been approved by Chair.";
		            logEl.setTextContent(message);

		            logsDoc.getDocumentElement().appendChild(logEl);

		            track = protocolTrackService.updateTrack(track, logsDoc);
				}
				
				logger.debug("Send agenda approval letter to reviewers ...");
				 EmailTemplate emailTemplate = protocolEmailDataService.loadObjectEmailTemplate("AGENDA_APPROVED_LETTER", null, agenda, null, null, currentUser, "");
				 emailTemplate = protocolEmailService.sendAgendaLetter(agenda, null, null, currentUser, emailTemplate.getIdentifier(), "", "Agenda Approved Letter", "Letter", emailTemplate.getTo(), emailTemplate.getCc(), emailTemplate.getRealSubject());
			} else {
				agendaStatusHelper.logStatus(agenda, user, null,
						AgendaStatusChangeAction.FINALIZE);
				
				logger.debug("Send pending agenda approval letter to chair ...");
				Map<String, Object> attributeRawValues = new HashMap<String, Object>();
				attributeRawValues.put("reason", reason);
				
				EmailTemplate emailTemplate = protocolEmailDataService.loadObjectEmailTemplate("SEND_AGENDA_FOR_APPROVAL_LETTER", null, agenda, null, null, currentUser, "");
				emailTemplate = protocolEmailService.sendAgendaLetter(agenda, null, attributeRawValues, currentUser, emailTemplate.getIdentifier(), "", "Send Agenda for Approval Letter", "Letter", emailTemplate.getTo(), emailTemplate.getCc(), emailTemplate.getRealSubject());
			}
			
			return Boolean.TRUE;

		} catch (Exception e) {
			e.printStackTrace();
			return Boolean.FALSE;
		}
	}

	/**
	 * need to do replace element rather than simply set XmlData
	 * 
	 * @param agendaId
	 * @param reason
	 * @param userId
	 * @return
	 */
	@RequestMapping(value = "/ajax/agendas/{agendaId}/approve", method = RequestMethod.POST)
	public @ResponseBody
	Boolean approveAgenda(@PathVariable("agendaId") long agendaId,
			@RequestParam("userId") long userId) {
		User currentUser = userDao.findById(userId);

		try {
			Agenda agenda = agendaDao.findById(agendaId);
			//User user = userDao.findById(userId);
			agendaStatusHelper.logStatus(agenda, currentUser, null,
					AgendaStatusChangeAction.APPROVE);
			
			List<AgendaItem> agendaItems = agendaItemDao.listByAgendaIdAndCategory(agendaId, AgendaItemCategory.FULL_BOARD);
			
			for (AgendaItem agendaItem : agendaItems) {
				ProtocolForm protocolForm = agendaItem.getProtocolForm();
				
				Track track = protocolTrackService.getOrCreateTrack("PROTOCOL",
						protocolForm.getProtocol().getId());

				Document logsDoc = protocolTrackService.getLogsDocument(track);

				Element logEl = logsDoc.createElement("log");
	      
				String logId = UUID.randomUUID().toString();
	      
				Date now = new Date();
	            logEl.setAttribute("id", logId);
	            logEl.setAttribute("parent-id", logId);
	            logEl.setAttribute("action-user-id", String.valueOf(currentUser.getId()));
	            logEl.setAttribute("actor", currentUser.getUsername());
	            logEl.setAttribute("date-time", DateFormatUtil.formateDate(now));
	            logEl.setAttribute("event-type", "AGENDA_APPROVAL");
	            logEl.setAttribute("form-id", String.valueOf(protocolForm.getId()));
	            logEl.setAttribute("parent-form-id", String.valueOf(protocolForm.getParentFormId()));
	            logEl.setAttribute("form-type", protocolForm.getProtocolFormType().toString());
	            logEl.setAttribute("log-type", "ACTION");
	            logEl.setAttribute("timestamp", String.valueOf(now.getTime()));
	            
	            String message = "" + DateFormatUtil.formateDateToMDY(agenda.getDate()) + " agenda has been approved by Chair.";
	            logEl.setTextContent(message);

	            logsDoc.getDocumentElement().appendChild(logEl);

	            track = protocolTrackService.updateTrack(track, logsDoc);
			}
			
			logger.debug("Send agenda approval letter to chair ...");
			 EmailTemplate emailTemplate = protocolEmailDataService.loadObjectEmailTemplate("AGENDA_APPROVED_LETTER", null, agenda, null, null, currentUser, "");
			 emailTemplate = protocolEmailService.sendAgendaLetter(agenda, null, null, currentUser, emailTemplate.getIdentifier(), "", "Agenda Approved Letter", "Letter", emailTemplate.getTo(), emailTemplate.getCc(), emailTemplate.getRealSubject());
			
			return Boolean.TRUE;
		} catch (Exception e) {
			e.printStackTrace();
			return Boolean.FALSE;
		}
	}
	
	@RequestMapping(value = "/ajax/agendas/{agendaId}/add-minutes", method = RequestMethod.POST)
	public @ResponseBody
	Boolean addMinutes(@PathVariable("agendaId") long agendaId,
			@RequestParam("userId") long userId) {
		try{
			Agenda currentAgenda = agendaDao.findById(agendaId);
			IRBRoster irbRoster = currentAgenda.getIrbRoster();
			
			Agenda lastAgenda = agendaDao.getLastAgendaForSameCommittee(irbRoster, agendaId, currentAgenda.getDate());
			
			AgendaItem agendaItem = new AgendaItem();
			agendaItem.setAgenda(currentAgenda);
			agendaItem.setAgendaItemStatus(AgendaItemStatus.NEW);
			agendaItem.setAgendaItemCategory(AgendaItemCategory.MINUTES);
			agendaItem.setOrder(0);
			//agendaItem.setProtocolForm(null);
			
			agendaItem = agendaItemDao.saveOrUpdate(agendaItem);
			String agendaItemXml = "<item id=\""+ agendaItem.getId() +"\" category=\"MINUTES\"><title>Minutes for "+ lastAgenda.getDate() +"</title><url>/agendas/"+ lastAgenda.getId() +"/minutes</url></item>";
			
			agendaItem.setXmlData(agendaItemXml);
			agendaItem = agendaItemDao.saveOrUpdate(agendaItem);
			
			return Boolean.TRUE;
		} catch (Exception e) {
			e.printStackTrace();
			return Boolean.FALSE;
		}
	}
	
	@RequestMapping(value = "/ajax/agendas/{agendaId}/meeting-complete", method = RequestMethod.GET)
	public @ResponseBody
	Boolean sendLetterToChair(@PathVariable("agendaId") long agendaId,
			@RequestParam("actor") String actor,
			@RequestParam(value = "userId") long userId) {
		User currentUser = userDao.findById(userId);
		
		 try {
			 Agenda agenda = agendaDao.findById(agendaId);
			 User user = userDao.findById(userId);
			 
			 agendaStatusHelper.logStatusWithActor(agenda, user, null, actor);
			 
			 Document assertXml = xmlProcessor.loadXmlStringToDOM(agenda.getMeetingXmlData());
				
			 XPath xpathInstance = xmlProcessor.getXPathInstance();
				
			 List<AgendaItemWrapper> agendaItemLst = agendaItemDao.listByAgendaId(agendaId);
			 
			 for (AgendaItemWrapper agendaItemWrapper : agendaItemLst){
				 if (agendaItemWrapper.getAgendaItemCategory().equals(AgendaItemCategory.FULL_BOARD)){
					 String agendaItemId = String.valueOf(agendaItemWrapper.getId());
					 
					 String xpath = "boolean(count(/meeting/activity/item[@agendaitemid[.=\""+ agendaItemId +"\"]]/motions/motion)>0)";
					 
					 Boolean hasMotion = (Boolean) (xpathInstance
								.evaluate(
										xpath,
										assertXml, XPathConstants.BOOLEAN));
					 AgendaItem agendaItem = agendaItemDao.findById(Long.valueOf(agendaItemId));
					 
					 if (!hasMotion){
						 agendaItem.setAgendaItemStatus(AgendaItemStatus.REMOVED);
						 agendaItemDao.saveOrUpdate(agendaItem);
						 
						 businessObjectStatusHelperContainer
							.getBusinessObjectStatusHelper("AGENDA_ITEM")
							.triggerAction(
									agendaItem.getProtocolForm(),
									Committee.IRB_OFFICE,
									currentUser,
									"REMOVED_FROM_AGENDA",
									null, null);
					 }
				 }
				 
			 }
			 
			 if (actor.equals("IRB_OFFICE")) {
				 logger.debug("Send letter to chair ...");
				 EmailTemplate emailTemplate = protocolEmailDataService.loadObjectEmailTemplate("NOTIFY_CHAIR_LETTER", null, agenda, null, null, currentUser, "");
				 emailTemplate = protocolEmailService.sendAgendaLetter(agenda, null, null, currentUser, emailTemplate.getIdentifier(), "", "Notify Chair Letter", "Letter", emailTemplate.getTo(), emailTemplate.getCc(), emailTemplate.getRealSubject());
			 }
			 
			 return Boolean.TRUE;
		 
		 }catch (Exception e) { 
			 e.printStackTrace(); 
			 return Boolean.FALSE; 
		}
	}
	
	@RequestMapping(value = "/ajax/agendas/{agendaId}/meeting-close", method = RequestMethod.GET)
	public @ResponseBody
	Boolean closeMeeting(@PathVariable("agendaId") long agendaId,
			@RequestParam("actor") String actor,
			@RequestParam(value = "userId") long userId) {
		Agenda agenda = agendaDao.findById(agendaId);
		User currentUser = userDao.findById(userId);
		
		 try {
			 agendaStatusHelper.logStatusWithActor(agenda, currentUser, null, actor);
			 
			 logger.debug("Send meeting close letter ...");
			 EmailTemplate emailTemplate = protocolEmailDataService.loadObjectEmailTemplate("MEETING_CLOSE_LETTER", null, agenda, null, null, currentUser, "");
			 emailTemplate = protocolEmailService.sendAgendaLetter(agenda, null, null, currentUser, emailTemplate.getIdentifier(), "", "Meeting Close Letter", "Letter", emailTemplate.getTo(), emailTemplate.getCc(), emailTemplate.getRealSubject());
			 
			 auditService.auditEvent("MEETING_COMPLETED", "Meeting on "+ DateFormatUtil.formateDateToMDY(agenda.getDate()) +" has completed.");
			 
			 return Boolean.TRUE;
		 
		 }catch (Exception e) { 
			 e.printStackTrace(); 
			 return Boolean.FALSE; 
		}
	}
	
	// @TODO missing audits
	@RequestMapping(value = "/ajax/agendas/{agendaId}/remove", method = RequestMethod.GET)
	public @ResponseBody
	JsonResponse removeAgenda(@PathVariable("agendaId") long agendaId,
			@RequestParam("userId") long userId) {
		User currentUser = userDao.findById(userId);

		try {
			Agenda agenda = agendaDao.findById(agendaId);
			
			List<AgendaItemWrapper> agendaItemLstt = agendaItemDao.listByAgendaId(agendaId);
			
			int reportedItemCount = 0;
			
			for (AgendaItemWrapper agendaItemWrapper : agendaItemLstt){
				if (agendaItemWrapper.getAgendaItemCategory().equals(AgendaItemCategory.REPORTED)) {
					reportedItemCount += 1;
				}
			}
			
			Agenda nextAvailabeAgenda = null;
			
			if (reportedItemCount > 0) {
				try {
					nextAvailabeAgenda = agendaDao
							.getNextAvailableAgendaByAgendaId(availableAgendaStatuses, agendaId);
					
					if (nextAvailabeAgenda.getId() == agenda.getId()) {
						return JsonResponseHelper.newErrorResponseStub("There is no next available agenda!  Please create a new agenda!");
					}
				} catch (Exception e) {
					e.printStackTrace();
					return JsonResponseHelper.newErrorResponseStub("There is no next available agenda!  Please create a new agenda!");
				}
			}
			
			for (AgendaItemWrapper agendaItemWrapper : agendaItemLstt){
				AgendaItem agendaItem = agendaItemDao.findById(agendaItemWrapper.getId());
				 if (agendaItem.getAgendaItemCategory().equals(AgendaItemCategory.FULL_BOARD)){
					 businessObjectStatusHelperContainer
						.getBusinessObjectStatusHelper("AGENDA_ITEM")
						.triggerAction(
								agendaItem.getProtocolForm(),
								Committee.IRB_OFFICE,
								currentUser,
								"REMOVED_FROM_AGENDA",
								null, null);
					 
					 agendaItem.setRetired(true);
					 agendaItem.setAgendaItemStatus(AgendaItemStatus.REMOVED);
						
					 agendaItem = agendaItemDao.saveOrUpdate(agendaItem);
				 } else {
					 logger.debug("agenda date: " + nextAvailabeAgenda.getDate()
								+ " is available!");

					 agendaItem.setAgenda(nextAvailabeAgenda);
					 agendaItem.setProtocolForm(agendaItem.getProtocolForm());
					 agendaItem.setAgendaItemStatus(AgendaItemStatus.NEW);
					 agendaItem.setAgendaItemCategory(agendaItem.getAgendaItemCategory());
					 agendaItem.setOrder(0);

					 agendaItemDao.saveOrUpdate(agendaItem);
					 
					 auditService.auditEvent("AGENDA_ITEM_PUSHED_TO_NEXT_AGENDA", "Agenda Item: ProtocolForm "+ agendaItem.getProtocolFormId() +" has been moved to next available agenda.");
				 }
				 
			 }

			agenda.setRetired(true);
			agendaDao.saveOrUpdate(agenda);
			
			auditService.auditEvent("AGENDA_REMOVED", currentUser
					.getPerson().getFullname()
					+ " has removed the agenda of "+ DateFormatUtil.formateDateToMDY(agenda.getDate()) +"");

		} catch (Exception e) {
			e.printStackTrace();
			return JsonResponseHelper.newErrorResponseStub("Failed to remove agenda!");
		}
		return JsonResponseHelper.newSuccessResponseStube("Successful");
	}
	
	@RequestMapping(value="/ajax/agendas/search", method = RequestMethod.GET, produces="application/json")
	public @ResponseBody
	JsonResponse listAgendasBySearchCriteria(
			@RequestParam("protocolId") long protocolId){
		
		try {
			List<Agenda> agendas = agendaDao.findAgendaByProtocolId(protocolId);
			
			return JsonResponseHelper.newDataResponseStub(agendas);
		} catch (Exception e) {
			return JsonResponseHelper.newErrorResponseStub("No agenda is related to this study!");
		}
		
	}

	@Autowired(required = true)
	public void setAgendaDao(AgendaDao agendaDao) {
		this.agendaDao = agendaDao;
	}

	public AgendaDao getAgendaDao() {
		return agendaDao;
	}

	@Autowired(required = true)
	public void setIrbReviewerDao(IRBReviewerDao irbReviewerDao) {
		this.irbReviewerDao = irbReviewerDao;
	}

	public IRBReviewerDao getIrbReviewerDao() {
		return irbReviewerDao;
	}

	@Autowired(required = true)
	public void setAgendaStatusHelper(AgendaStatusHelper agendaStatusHelper) {
		this.agendaStatusHelper = agendaStatusHelper;
	}

	public AgendaStatusHelper getAgendaStatusHelper() {
		return agendaStatusHelper;
	}

	@Autowired(required = true)
	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}

	public UserDao getUserDao() {
		return userDao;
	}

	@Autowired(required = true)
	public void setAgendaIRBReviewerDao(
			AgendaIRBReviewerDao agendaIRBReviewerDao) {
		this.agendaIRBReviewerDao = agendaIRBReviewerDao;
	}

	public AgendaIRBReviewerDao getAgendaIRBReviewerDao() {
		return agendaIRBReviewerDao;
	}

	public AgendaStatusDao getAgendaStatusDao() {
		return agendaStatusDao;
	}
	
	@Autowired(required = true)
	public void setAgendaStatusDao(AgendaStatusDao agendaStatusDao) {
		this.agendaStatusDao = agendaStatusDao;
	}

	public AgendaItemDao getAgendaItemDao() {
		return agendaItemDao;
	}
	
	@Autowired(required = true)
	public void setAgendaItemDao(AgendaItemDao agendaItemDao) {
		this.agendaItemDao = agendaItemDao;
	}

	public BusinessObjectStatusHelperContainer getBusinessObjectStatusHelperContainer() {
		return businessObjectStatusHelperContainer;
	}
	
	@Autowired(required = true)
	public void setBusinessObjectStatusHelperContainer(
			BusinessObjectStatusHelperContainer businessObjectStatusHelperContainer) {
		this.businessObjectStatusHelperContainer = businessObjectStatusHelperContainer;
	}

	public ProtocolEmailDataService getProtocolEmailDataService() {
		return protocolEmailDataService;
	}
	
	@Autowired(required = true)
	public void setProtocolEmailDataService(ProtocolEmailDataService protocolEmailDataService) {
		this.protocolEmailDataService = protocolEmailDataService;
	}

	public ProtocolEmailService getProtocolEmailService() {
		return protocolEmailService;
	}
	
	@Autowired(required = true)
	public void setProtocolEmailService(ProtocolEmailService protocolEmailService) {
		this.protocolEmailService = protocolEmailService;
	}

	public XmlProcessor getXmlProcessor() {
		return xmlProcessor;
	}
	
	@Autowired(required = true)
	public void setXmlProcessor(XmlProcessor xmlProcessor) {
		this.xmlProcessor = xmlProcessor;
	}

	public AuditService getAuditService() {
		return auditService;
	}
	
	@Autowired(required = true)
	public void setAuditService(AuditService auditService) {
		this.auditService = auditService;
	}

	public UserService getUserService() {
		return userService;
	}
	
	@Autowired(required = true)
	public void setUserService(UserService userService) {
		this.userService = userService;
	}

	public MutexLockService getMutexLockService() {
		return mutexLockService;
	}
	
	@Autowired(required = true)
	public void setMutexLockService(MutexLockService mutexLockService) {
		this.mutexLockService = mutexLockService;
	}

	public ProtocolTrackService getProtocolTrackService() {
		return protocolTrackService;
	}
	
	@Autowired(required = true)
	public void setProtocolTrackService(ProtocolTrackService protocolTrackService) {
		this.protocolTrackService = protocolTrackService;
	}
}