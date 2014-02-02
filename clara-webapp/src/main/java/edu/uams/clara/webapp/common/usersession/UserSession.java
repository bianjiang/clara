package edu.uams.clara.webapp.common.usersession;

import java.io.Serializable;

/**
 * 
 * @author jbian
 *
 */
public interface UserSession extends Serializable {

	void setAttribute(String name, Object object);

	Object getAttribute(String name);
	
	void removeAttribute(String name);
	
}
