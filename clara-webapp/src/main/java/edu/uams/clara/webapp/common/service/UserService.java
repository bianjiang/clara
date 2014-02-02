package edu.uams.clara.webapp.common.service;

import java.util.List;

import edu.uams.clara.webapp.common.domain.usercontext.Person;
import edu.uams.clara.webapp.common.domain.usercontext.User;

public interface UserService {

	/**
	 * get user from the database first, if it doesn't exist, return null
	 * @param username
	 * @return User
	 */
	User getUserByUsername(String username);
	
	/**
	 * get current user from securecontext
	 * @return User
	 */
	User getCurrentUser();
	
	/**
	 * get user from the database first, if it doesn't exist:
		 * if @param create is true, then it queries the ldap server, and assign the user the default role "ROLE_USER", if still can't find the user, throw  UsernameNotFoundException
		 * if @param create is false, then it returns null
	 * if it does exist in database, this will still query the ldap to get latest user information and update the local database
	 * @param username
	 * @param create
	 * @return User
	 */
	User getAndUpdateUserByUsername(String username, boolean create);
	
	/**
	 * search for users by keyword
	 * it searches the database first, if it doesn't exist, query the ldap server. 
	 * users from database will have id assigned, while user from ldap won't have id assigned.
	 * 
	 * @param keyword
	 * @return
	 */
	List<Person> searchForPersons(String keyword);

	User getAndUpdateUserBySap(String sap, boolean create);	
	
	void createExternalUser(String username, String firstName, String lastName, String phone, String email,String department);
	
	User getUserByUserId(long userId);
	
	User getUserByEmail(String email);

}
