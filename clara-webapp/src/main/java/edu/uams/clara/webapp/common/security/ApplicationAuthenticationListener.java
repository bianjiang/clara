package edu.uams.clara.webapp.common.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AbstractAuthenticationEvent;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.InteractiveAuthenticationSuccessEvent;
import org.springframework.transaction.annotation.Transactional;

import edu.uams.clara.webapp.common.service.audit.AuditService;

public class ApplicationAuthenticationListener implements
		ApplicationListener<AbstractAuthenticationEvent> {

	private final static Logger logger = LoggerFactory
			.getLogger(ApplicationAuthenticationListener.class);

	public ApplicationAuthenticationListener(AuditService auditService){
		this.auditService = auditService;
	}
	
	public ApplicationAuthenticationListener(){
		
	}

	private AuditService auditService;
			
	@Override
	@Transactional
	public void onApplicationEvent(AbstractAuthenticationEvent event) {

		if ( event instanceof AbstractAuthenticationFailureEvent )
		{
			//AbstractAuthenticationFailureEvent authenticationFailedEvent = ( AbstractAuthenticationFailureEvent ) event;
			logger.debug( "authFailed:" + event.toString() );
			((AbstractAuthenticationFailureEvent) event).getException().printStackTrace();
			auditService.auditEvent("USER_LOGIN_FAIL", "authentication failed for " + event.getAuthentication().getName(), event.toString());
			
		}
		else if ( event instanceof InteractiveAuthenticationSuccessEvent )
		{
			//AuthenticationSuccessEvent authenticationSuccessEvent = ( AuthenticationSuccessEvent ) event;
			logger.debug ( "authSuccess:" + event.toString() );
			auditService.auditEvent("USER_LOGIN_SUCCESS", "authentication success for " + event.getAuthentication().getName(), event.toString());
		}
		else
		{
			logger.debug ( "undefined: " + event.getClass ().getName () );
		}		
	}

	@Autowired(required=true)
	public void setAuditService(AuditService auditService) {
		this.auditService = auditService;
	}

	public AuditService getAuditService() {
		return auditService;
	}


}
