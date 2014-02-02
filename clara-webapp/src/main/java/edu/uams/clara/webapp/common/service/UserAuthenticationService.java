package edu.uams.clara.webapp.common.service;

public interface UserAuthenticationService {
	boolean isAuthenticated(String username, String password);
}
