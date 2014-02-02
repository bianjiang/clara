package edu.uams.clara.webapp.common.service.impl;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.ldap.userdetails.UserDetailsContextMapper;

import edu.uams.clara.webapp.common.domain.usercontext.User;
import edu.uams.clara.webapp.common.service.UserService;

public class UserDetailsContextMapperImpl implements UserDetailsContextMapper {

	private final static Logger logger = LoggerFactory
			.getLogger(UserDetailsContextMapper.class);

	private UserService userService;

	@Override
	public void mapUserToContext(UserDetails user, DirContextAdapter ctx) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}	

	@Override
	public UserDetails mapUserFromContext(DirContextOperations ctx,
			String username, Collection<? extends GrantedAuthority> authorities) {
		if (username == null || username.length() == 0) {
			throw new UsernameNotFoundException("username must not be null");
		}

		User u = userService.getUserByUsername(username);

		logger.debug("username: " + username + "; userId: " + u.getId());
		return u;
	}
	
	@Autowired(required = true)
	public void setUserService(UserService userService) {
		this.userService = userService;
	}

	public UserService getUserService() {
		return userService;
	}

}
