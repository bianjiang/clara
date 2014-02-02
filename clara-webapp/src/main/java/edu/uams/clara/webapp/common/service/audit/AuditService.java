package edu.uams.clara.webapp.common.service.audit;

import edu.uams.clara.core.domain.AbstractDomainEntity;
import edu.uams.clara.webapp.common.domain.audit.Audit;

public interface AuditService {
	
	public static enum AuditEvent {
		EPIC_WS_SENT, EPIC_WS_FAILED, EPIC_WS_SUCCEED, VIEW_BUDDGET, EDIT_BUDGET, SAVE_BUDGET, UPDATE_USER_PROFILE;
	}

	Audit auditEvent(String eventType, String message, String extraData, AbstractDomainEntity refDomainEntity);
	
	Audit auditEvent(String eventType, String message, AbstractDomainEntity refDomainEntity);
	
	Audit auditEvent(String eventType, String message);
	
	Audit auditEvent(String eventType, String message, String extraData);

}
