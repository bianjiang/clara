package edu.uams.clara.webapp.common.service.audit.impl;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;

import edu.uams.clara.core.domain.AbstractDomainEntity;
import edu.uams.clara.webapp.common.dao.audit.AuditDao;
import edu.uams.clara.webapp.common.domain.audit.Audit;
import edu.uams.clara.webapp.common.service.audit.AuditService;


public class AuditServiceImpl implements AuditService {
	
	private AuditDao auditDao;
	
	public AuditServiceImpl(){

	}

	@Autowired(required=true)
	public void setAuditDao(AuditDao auditDao) {
		this.auditDao = auditDao;
	}

	public AuditDao getAuditDao() {
		return auditDao;
	}

	
	
	@Override
	public Audit auditEvent(String eventType, String message, String extraData){
		return auditEvent(eventType, message, extraData, null);
	}

	@Override
	public Audit auditEvent(String eventType, String message) {
		return auditEvent(eventType, message, null, null);
	}

	@Override
	public Audit auditEvent(String eventType, String message,
			String extraData, AbstractDomainEntity refDomainEntity) {
		Audit ab = new Audit();
		ab.setEventType(eventType);
		ab.setMessage(message);
		ab.setExtraData(extraData);
		ab.setDatetime(new Date());
		if(refDomainEntity != null){
			ab.setRefObjectId(refDomainEntity.getId());
			ab.setRefObjectClass(refDomainEntity.getClass().getName());
		}
		ab = auditDao.saveOrUpdate(ab);	
		
		return ab;
		
	}

	@Override
	public Audit auditEvent(String eventType, String message,
			AbstractDomainEntity refDomainEntity) {
		return auditEvent(eventType, message, null, refDomainEntity);
	}
}
