package edu.uams.clara.webapp.common.service.impl;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import edu.uams.clara.webapp.common.domain.usercontext.User;
import edu.uams.clara.webapp.common.service.UserService;


/**
 * @author Jiang Bian
 */
public class UserDetailsServiceImpl implements UserDetailsService
{	
    private final static Logger logger = LoggerFactory.getLogger(UserDetailsServiceImpl.class);
    
    private UserService userService;

    public UserDetailsServiceImpl()
    {
  
    }

    /**
     * loaduser...from either ldap or database...
     */
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException
    {
        logger.debug("trying to find user: " + username);
       
        User u = userService.getAndUpdateUserByUsername(username, false);    
         
        if (u == null)
        {
            logger.debug("user not found in either the database nor the ldap");
            
            throw new UsernameNotFoundException("User not found!");
        }
       
        logger.debug("user name: " + u.getPerson().getFirstname() + " " + u.getPerson().getLastname() + "userType: " + u.getUserType().toString());
                
        logger.debug("returning user " + u.getUsername());
       
        return u;
    }

    @Autowired(required=true)
	public void setUserService(UserService userService) {
		this.userService = userService;
	}

	public UserService getUserService() {
		return userService;
	}

}
