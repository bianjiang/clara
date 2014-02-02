package edu.uams.clara.webapp.common.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import edu.uams.clara.webapp.common.dao.usercontext.UserDao;
import edu.uams.clara.webapp.common.domain.usercontext.User;
import edu.uams.clara.webapp.common.domain.usercontext.enums.Permission;
import edu.uams.clara.webapp.common.service.UserAuthenticationService;

public class UserAuthenticationServiceImpl implements UserAuthenticationService {

	private final static Logger logger = LoggerFactory
			.getLogger(UserAuthenticationService.class);

	private AuthenticationManager authenticationManager;
	
	private UserDao userDao;

	@Override
	public boolean isAuthenticated(String username, String password) {
		boolean authenticated = false;
		
		User user = (User) ((SecurityContext) SecurityContextHolder.getContext())
				.getAuthentication().getPrincipal();
		
		try {
			User signUser = userDao.getUserByUsername(username);
			
			if (user.getId() != signUser.getId() && !signUser.getAuthorities().contains(Permission.ROLE_SECRET_ADMIN)){
				return false;
			}
		} catch (Exception e) {
			logger.warn("invalid username: " + username);
			return false;
		}
		

		try {
			Authentication request = new UsernamePasswordAuthenticationToken(
					username, password);
			Authentication result = authenticationManager.authenticate(request);

			authenticated = result.isAuthenticated();
		} catch (AuthenticationException ex) {
			logger.warn("unauthenticated user; username: " + username
					+ "; password: " + password);
			authenticated = false;
		}

		return authenticated;
	}

	@Autowired(required = true)
	public void setAuthenticationManager(
			AuthenticationManager authenticationManager) {
		this.authenticationManager = authenticationManager;
	}

	public AuthenticationManager getAuthenticationManager() {
		return authenticationManager;
	}

	public UserDao getUserDao() {
		return userDao;
	}
	
	@Autowired(required = true)
	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}

}
