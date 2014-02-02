package edu.uams.clara.webapp.protocol.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import edu.uams.clara.webapp.protocol.dao.ProtocolDao;
import edu.uams.clara.webapp.protocol.dao.businesslogicobject.ProtocolFormStatusDao;
import edu.uams.clara.webapp.protocol.dao.businesslogicobject.ProtocolStatusDao;
import edu.uams.clara.webapp.protocol.domain.Protocol;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.ProtocolFormStatus;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.ProtocolStatus;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.enums.ProtocolFormStatusEnum;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.enums.ProtocolStatusEnum;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolForm;
import edu.uams.clara.webapp.protocol.service.ProtocolAndFormStatusService;

public class ProtocolAndFormStatusServiceImpl implements
		ProtocolAndFormStatusService {
	private final static Logger logger = LoggerFactory
			.getLogger(ProtocolAndFormStatusServiceImpl.class);
	
	private ProtocolStatusDao protocolStatusDao;
	private ProtocolFormStatusDao protocolFormStatusDao;
	private ProtocolDao protocolDao;
	
	private List<String> normalList = new ArrayList<String>();{		
		normalList.add(ProtocolStatusEnum.DRAFT.getDescription());
	}
	
	private List<String> infoList = new ArrayList<String>();{	
		for(ProtocolStatusEnum pse : ProtocolStatusEnum.values()){
			if (pse.toString().startsWith("UNDER")){
				infoList.add(pse.getDescription());
			}
		}
	}
	
	private List<String> warnList = new ArrayList<String>();{	
		for(ProtocolStatusEnum pse : ProtocolStatusEnum.values()){
			if (pse.toString().contains("PENDING") || pse.toString().contains("PENDING_PI_ENDORSEMENT")){
				warnList.add(pse.getDescription());
			}
		}
	}
	
	private List<String> errorList = new ArrayList<String>();{	
		errorList.add(ProtocolStatusEnum.POTENTIAL_NON_COMPLIANCE.getDescription());
		errorList.add(ProtocolStatusEnum.REVISION_REQUESTED.getDescription());
		
		for(ProtocolStatusEnum pse : ProtocolStatusEnum.values()){
			if (pse.toString().contains("DECLINE")){
				errorList.add(pse.getDescription());
			}
		}
	}

	@Override
	public String getProtocolPriorityLevel(Protocol protocol) {
		String priorityLevel = "";
		
		ProtocolStatus protocolStatus = protocolDao.getLatestProtocolStatusByProtocolId(protocol.getId());
		
		ProtocolStatusEnum pse = protocolStatus.getProtocolStatus();
		
		if (normalList.contains(pse.getDescription())) priorityLevel = "NORMAL";
		if (infoList.contains(pse.getDescription())) priorityLevel = "INFO";
		if (warnList.contains(pse.getDescription())) priorityLevel = "WARN";
		if (errorList.contains(pse.getDescription())) priorityLevel = "ERROR";
		
		return priorityLevel;
	}
	
	//need to re-do this one later, cause it's using protocol status
	@Override
	public String getProtocolFormPriorityLevel(ProtocolForm protocolForm) {
		String priorityLevel = "";
		
		ProtocolFormStatus protocolFormStatus = protocolFormStatusDao.getLatestProtocolFormStatusByFormId(protocolForm.getId());
		
		ProtocolFormStatusEnum pfse = protocolFormStatus.getProtocolFormStatus();
		
		if (normalList.contains(pfse.getDescription())) priorityLevel = "NORMAL";
		if (infoList.contains(pfse.getDescription())) priorityLevel = "INFO";
		if (warnList.contains(pfse.getDescription())) priorityLevel = "WARN";
		if (errorList.contains(pfse.getDescription())) priorityLevel = "ERROR";
		
		return priorityLevel;
	}

	public ProtocolStatusDao getProtocolStatusDao() {
		return protocolStatusDao;
	}
	
	@Autowired(required = true)
	public void setProtocolStatusDao(ProtocolStatusDao protocolStatusDao) {
		this.protocolStatusDao = protocolStatusDao;
	}

	public ProtocolFormStatusDao getProtocolFormStatusDao() {
		return protocolFormStatusDao;
	}
	
	@Autowired(required = true)
	public void setProtocolFormStatusDao(ProtocolFormStatusDao protocolFormStatusDao) {
		this.protocolFormStatusDao = protocolFormStatusDao;
	}

	public ProtocolDao getProtocolDao() {
		return protocolDao;
	}
	
	@Autowired(required = true)
	public void setProtocolDao(ProtocolDao protocolDao) {
		this.protocolDao = protocolDao;
	}

}
