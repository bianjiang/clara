package edu.uams.clara.webapp.common.security;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;

import edu.uams.clara.webapp.common.dao.usercontext.UserDao;
import edu.uams.clara.webapp.common.domain.usercontext.User;
import edu.uams.clara.webapp.common.service.audit.AuditService;

public class AuditableSecurityContextLogoutHandler extends SecurityContextLogoutHandler{

	private AuditService auditService;
	
	private MutexLockService mutexLockService;
	
	private UserDao userDao;
	
	public AuditableSecurityContextLogoutHandler(AuditService auditService){
		this.auditService = auditService;
	}
	
	@Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        if (authentication != null) {
        	auditService.auditEvent("USER_LOGOUT", "user: " + authentication.getName() + " logout");

        	User user = userDao.getUserByUsername(authentication.getName());
        	
        	mutexLockService.unlockAllMutexLockByUser(user);
        	
        }
        super.logout(request, response, authentication);
    }

	public void setAuditService(AuditService auditService) {
		this.auditService = auditService;
	}

	public AuditService getAuditService() {
		return auditService;
	}	

	public UserDao getUserDao() {
		return userDao;
	}
	
	@Autowired(required = true)
	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}

	public MutexLockService getMutexLockService() {
		return mutexLockService;
	}

	@Autowired(required = true)
	public void setMutexLockService(MutexLockService mutexLockService) {
		this.mutexLockService = mutexLockService;
	}
}
