package edu.uams.clara.webapp.common.security;

import org.springframework.security.core.userdetails.UserDetails;

/**
 * Responsible for encrypt password and verify password.
 * @author bianjiang
 *
 */
public interface UserPasswordHelper {

	String encryptPassword(String password, UserDetails userDetails);

	boolean isPasswordValid(String rawPass,
			UserDetails userDetails);
	 
}
