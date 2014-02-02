package edu.uams.clara.webapp.common.usersession.impl;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;

import edu.uams.clara.webapp.common.dao.usercontext.UserDao;
import edu.uams.clara.webapp.common.usersession.UserSession;

/**
 * 
 * @author jbian
 * 
 */
public class UserSessionImpl implements UserSession {

	private static final long serialVersionUID = 1302981247244576918L;

	private UserDao userDao;

	private HttpSession session;

	private HttpServletRequest request;

	

	@Autowired(required = true)
	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}

	public UserDao getUserDao() {
		return userDao;
	}

	@Override
	public void setAttribute(String name, Object object) {
		if (session != null) {
			Object o = session.getAttribute(name);

			if (o != null) {
				session.removeAttribute(name);
			}

			session.setAttribute(name, object);
		}

	}

	@Override
	public Object getAttribute(String name) {
		if (session != null) {
			return session.getAttribute(name);
		} else {
			return null;
		}
	}
	
	@Override
	public void removeAttribute(String name) {
		if (session != null) {
			session.removeAttribute(name);
		}		
	}

	@Autowired(required = false)
	public void setSession(HttpSession session) {
		this.session = session;
	}

	public HttpSession getSession() {
		return session;
	}

	@Autowired(required = false)
	public void setRequest(HttpServletRequest request) {
		this.request = request;
	}

	public HttpServletRequest getRequest() {
		return request;
	}

	

}
