package edu.uams.clara.webapp.protocol.businesslogic.irb.agenda.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import edu.uams.clara.webapp.common.businesslogic.BusinessObjectStatusHelperContainer;
import edu.uams.clara.webapp.common.dao.usercontext.UserRoleDao;
import edu.uams.clara.webapp.common.domain.usercontext.User;
import edu.uams.clara.webapp.common.domain.usercontext.UserRole;
import edu.uams.clara.webapp.common.domain.usercontext.enums.Committee;
import edu.uams.clara.webapp.protocol.businesslogic.irb.agenda.AgendaStatusHelper;
import edu.uams.clara.webapp.protocol.dao.businesslogicobject.AgendaStatusDao;
import edu.uams.clara.webapp.protocol.dao.businesslogicobject.ProtocolFormCommitteeStatusDao;
import edu.uams.clara.webapp.protocol.dao.businesslogicobject.ProtocolFormStatusDao;
import edu.uams.clara.webapp.protocol.dao.businesslogicobject.ProtocolStatusDao;
import edu.uams.clara.webapp.protocol.dao.irb.AgendaItemDao;
import edu.uams.clara.webapp.protocol.dao.irb.AgendaItemReviewerDao;
import edu.uams.clara.webapp.protocol.dao.protocolform.ProtocolFormDao;
import edu.uams.clara.webapp.protocol.domain.Protocol;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.AgendaStatus;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.ProtocolFormCommitteeStatus;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.ProtocolFormStatus;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.ProtocolStatus;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.enums.AgendaStatusEnum;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.enums.ProtocolFormCommitteeStatusEnum;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.enums.ProtocolFormStatusEnum;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.enums.ProtocolStatusEnum;
import edu.uams.clara.webapp.protocol.domain.irb.Agenda;
import edu.uams.clara.webapp.protocol.domain.irb.AgendaItemReviewerWrapper;
import edu.uams.clara.webapp.protocol.domain.irb.AgendaItemWrapper;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolForm;
import edu.uams.clara.webapp.protocol.domain.protocolform.enums.ProtocolFormType;
import edu.uams.clara.webapp.protocol.service.email.ProtocolEmailDataService;
import edu.uams.clara.webapp.queue.service.QueueServiceContainer;
import edu.uams.clara.webapp.xml.processor.XmlProcessor;

/**
 * 
 * this is service bean is used to control the status of the agenda
 * 1). IRB Office finalize the agenda, and send it to the chair for review
 * 2). Chair sign off 
 * @author bianjiang
 *
 */
public class AgendaStatusHelperImpl implements AgendaStatusHelper {
	
	private final static Logger logger = LoggerFactory
			.getLogger(AgendaStatusHelper.class);
			
	
	private AgendaStatusDao agendaStatusDao;
	
	private AgendaItemDao agendaItemDao;	
	
	private BusinessObjectStatusHelperContainer businessObjectStatusHelperContainer;
	
	private XmlProcessor xmlProcessor;
	
	private ProtocolFormDao protocolFormDao;
	
	private ProtocolFormStatusDao protocolFormStatusDao;
	
	private ProtocolStatusDao protocolStatusDao;
	
	private ProtocolFormCommitteeStatusDao protocolFormCommitteeStatusDao;
	
	private AgendaItemReviewerDao agendaItemReviewerDao;	

	private QueueServiceContainer queueServiceContainer;
	
	private UserRoleDao userRoleDao;
		
	
	private ProtocolEmailDataService protocolEmailDataService;
	
	private void updateAssignedIRBReviewerToFormMetaData(AgendaItemWrapper agendaItem, User currentUser){
		
		List<AgendaItemReviewerWrapper> agendaItemReviewrs = null;
		
		List<UserRole> reviewersUserRole = new ArrayList<UserRole>();
		
		try {
			agendaItemReviewrs = agendaItemReviewerDao
					.listByAgendaItemId(agendaItem.getId());
			
			for(AgendaItemReviewerWrapper agendaItemReviewrWrapper:agendaItemReviewrs){
				
				try{
					UserRole userRole = userRoleDao.getUserRolesByUserIdAndCommittee(agendaItemReviewrWrapper.getIRBReviewer().getUser().getId(), Committee.IRB_REVIEWER);
					reviewersUserRole.add(userRole);
				}catch(Exception ex){
					logger.warn(ex.getMessage());
				}				
			}
			
		}catch(Exception ex){
			ex.printStackTrace();
			logger.warn(ex.getMessage());
		}
		
		queueServiceContainer.getQueueService("Protocol").updateAssignedReviewersToMetaData(agendaItem.getProtocolForm(), Committee.IRB_OFFICE, currentUser, reviewersUserRole);			
		
	}
	
	private void processAgendaItems(Agenda agenda, User currentUser){
		
		List<AgendaItemWrapper> agendaItems = agendaItemDao.listByAgendaId(agenda.getId());
		
		for(AgendaItemWrapper agendaItemWrapper:agendaItems){
			
			ProtocolForm protocolForm = agendaItemWrapper.getProtocolForm();
			try {
				updateAssignedIRBReviewerToFormMetaData(agendaItemWrapper, currentUser);
				businessObjectStatusHelperContainer.getBusinessObjectStatusHelper(protocolForm.getProtocolFormType().toString()).triggerAction(protocolForm, Committee.IRB_REVIEWER, currentUser, "ASSIGN_REVIEWER", null, null);
			} catch (Exception ex) {
				// TODO Auto-generated catch block
				ex.printStackTrace();
			} 
			
		}
	}
	
	@Override
	public void logStatus(Agenda agenda, User user, String note, AgendaStatusChangeAction action) {
		
		Assert.notNull(agenda);
		
		AgendaStatus agendaStatus = new AgendaStatus();
		agendaStatus.setAgenda(agenda);
		agendaStatus.setModified(new Date());
		agendaStatus.setUser(user);
		agendaStatus.setNote(note);
		
		List<AgendaItemWrapper> agendaItemLst = agendaItemDao.listByAgendaId(agenda.getId());
		
		switch (action){
		case CREATE:
			agendaStatus.setAgendaStatus(AgendaStatusEnum.AGENDA_INCOMPLETE);
			break;
		case FINALIZE:			
			agendaStatus.setAgendaStatus(AgendaStatusEnum.AGENDA_PENDING_CHAIR_APPROVAL);	
			break;
		case APPROVE:
			agendaStatus.setAgendaStatus(AgendaStatusEnum.AGENDA_APPROVED);
			//processAgendaItems(agenda,user);
			break;
		case CANCEL:
			agendaStatus.setAgendaStatus(AgendaStatusEnum.CANCELLED);
			for (AgendaItemWrapper agendaItemWrapper : agendaItemLst){
				
				ProtocolForm protocolForm = agendaItemWrapper.getProtocolForm();
				
				if (protocolForm.getFormType().equals(ProtocolFormType.NEW_SUBMISSION)){
					Protocol protocol = protocolForm.getProtocol();
					
					ProtocolStatus protocolStatus = new ProtocolStatus();
					protocolStatus.setProtocol(protocol);
					protocolStatus.setCauseByUser(user);
					protocolStatus.setModified(new Date());
					protocolStatus.setCausedByCommittee(Committee.IRB_OFFICE);
					protocolStatus.setProtocolStatus(ProtocolStatusEnum.PENDING_IRB_REVIEW_RE_ASSIGNMENT);
					
					protocolStatusDao.saveOrUpdate(protocolStatus);
				}
				
				ProtocolFormStatus protocolFormStatus = new ProtocolFormStatus();
				protocolFormStatus.setProtocolForm(protocolForm);
				protocolFormStatus.setModified(new Date());
				protocolFormStatus.setCausedByCommittee(Committee.IRB_OFFICE);
				protocolFormStatus.setCauseByUser(user);
				protocolFormStatus.setProtocolFormStatus(ProtocolFormStatusEnum.PENDING_IRB_REVIEW_RE_ASSIGNMENT);
				
				protocolFormStatusDao.saveOrUpdate(protocolFormStatus);
				
				ProtocolFormCommitteeStatus protocolFormCommitteeStatus = new ProtocolFormCommitteeStatus();
				protocolFormCommitteeStatus.setCauseByUser(user);
				protocolFormCommitteeStatus.setCausedByCommittee(Committee.IRB_OFFICE);
				protocolFormCommitteeStatus.setCommittee(Committee.IRB_OFFICE);
				protocolFormCommitteeStatus.setModified(new Date());
				protocolFormCommitteeStatus.setProtocolForm(protocolForm);
				protocolFormCommitteeStatus.setProtocolFormCommitteeStatus(ProtocolFormCommitteeStatusEnum.PENDING_IRB_REVIEW_RE_ASSIGNMENT);
				
				protocolFormCommitteeStatusDao.saveOrUpdate(protocolFormCommitteeStatus);
			}
			break;
		case START_MEETING:
			agendaStatus.setAgendaStatus(AgendaStatusEnum.MEETING_IN_PROGRESS);
			break;
		case STOP_MEETING:
			agendaStatus.setAgendaStatus(AgendaStatusEnum.MEETING_ADJOURNED);
			break;
		}
		
		agendaStatus = agendaStatusDao.saveOrUpdate(agendaStatus);
	}  
	
	@Override
	public void logStatusWithActor(Agenda agenda, User user, String note, String actor) {
		Assert.notNull(agenda);
		
		AgendaStatus agendaStatus = new AgendaStatus();
		agendaStatus.setAgenda(agenda);
		agendaStatus.setModified(new Date());
		agendaStatus.setUser(user);
		agendaStatus.setNote(note);
		
		AgendaStatus previouseAgendaStatus = agendaStatusDao.getAgendaStatusByAgendaId(agenda.getId());
		
		if (actor.equals("IRB_OFFICE")){
			if (previouseAgendaStatus.getAgendaStatus().equals(AgendaStatusEnum.MEETING_ADJOURNED_PENDING_IRB_OFFICE_PROCESS)){
				agendaStatus.setAgendaStatus(AgendaStatusEnum.MEETING_CLOSED);
			} else {
				agendaStatus.setAgendaStatus(AgendaStatusEnum.MEETING_ADJOURNED_PENDING_CHAIR_APPROVAL);
			}
			//TODO: send email
		}
		
		if (actor.equals("IRB_CHAIR")){
			agendaStatus.setAgendaStatus(AgendaStatusEnum.MEETING_ADJOURNED_PENDING_IRB_OFFICE_PROCESS);
			//TODO: send email
		}
		
		/*
		if (actor.equals("TRANSCRIBER")){
			agendaStatus.setAgendaStatus(AgendaStatusEnum.MEETING_CLOSED);
			//TODO: send email
		}*/
		
		agendaStatus = agendaStatusDao.saveOrUpdate(agendaStatus);
		
	}

	@Autowired(required=true)
	public void setAgendaStatusDao(AgendaStatusDao agendaStatusDao) {
		this.agendaStatusDao = agendaStatusDao;
	}

	public AgendaStatusDao getAgendaStatusDao() {
		return agendaStatusDao;
	}

	public ProtocolEmailDataService getProtocolEmailDataService() {
		return protocolEmailDataService;
	}
	
	@Autowired(required=true)
	public void setProtocolEmailDataService(ProtocolEmailDataService protocolEmailDataService) {
		this.protocolEmailDataService = protocolEmailDataService;
	}

	public BusinessObjectStatusHelperContainer getBusinessObjectStatusHelperContainer() {
		return businessObjectStatusHelperContainer;
	}

	@Autowired(required=true)
	public void setBusinessObjectStatusHelperContainer(
			BusinessObjectStatusHelperContainer businessObjectStatusHelperContainer) {
		this.businessObjectStatusHelperContainer = businessObjectStatusHelperContainer;
	}

	public AgendaItemDao getAgendaItemDao() {
		return agendaItemDao;
	}

	@Autowired(required=true)
	public void setAgendaItemDao(AgendaItemDao agendaItemDao) {
		this.agendaItemDao = agendaItemDao;
	}

	public XmlProcessor getXmlProcessor() {
		return xmlProcessor;
	}

	@Autowired(required=true)
	public void setXmlProcessor(XmlProcessor xmlProcessor) {
		this.xmlProcessor = xmlProcessor;
	}

	public AgendaItemReviewerDao getAgendaItemReviewerDao() {
		return agendaItemReviewerDao;
	}

	@Autowired(required=true)
	public void setAgendaItemReviewerDao(AgendaItemReviewerDao agendaItemReviewerDao) {
		this.agendaItemReviewerDao = agendaItemReviewerDao;
	}

	public ProtocolFormDao getProtocolFormDao() {
		return protocolFormDao;
	}

	@Autowired(required=true)
	public void setProtocolFormDao(ProtocolFormDao protocolFormDao) {
		this.protocolFormDao = protocolFormDao;
	}

	public QueueServiceContainer getQueueServiceContainer() {
		return queueServiceContainer;
	}

	@Autowired(required = true)
	public void setQueueServiceContainer(QueueServiceContainer queueServiceContainer) {
		this.queueServiceContainer = queueServiceContainer;
	}

	public UserRoleDao getUserRoleDao() {
		return userRoleDao;
	}

	@Autowired(required = true)
	public void setUserRoleDao(UserRoleDao userRoleDao) {
		this.userRoleDao = userRoleDao;
	}

	public ProtocolFormStatusDao getProtocolFormStatusDao() {
		return protocolFormStatusDao;
	}
	
	@Autowired(required = true)
	public void setProtocolFormStatusDao(ProtocolFormStatusDao protocolFormStatusDao) {
		this.protocolFormStatusDao = protocolFormStatusDao;
	}

	public ProtocolStatusDao getProtocolStatusDao() {
		return protocolStatusDao;
	}
	
	@Autowired(required = true)
	public void setProtocolStatusDao(ProtocolStatusDao protocolStatusDao) {
		this.protocolStatusDao = protocolStatusDao;
	}

	public ProtocolFormCommitteeStatusDao getProtocolFormCommitteeStatusDao() {
		return protocolFormCommitteeStatusDao;
	}
	
	@Autowired(required = true)
	public void setProtocolFormCommitteeStatusDao(
			ProtocolFormCommitteeStatusDao protocolFormCommitteeStatusDao) {
		this.protocolFormCommitteeStatusDao = protocolFormCommitteeStatusDao;
	}

}
