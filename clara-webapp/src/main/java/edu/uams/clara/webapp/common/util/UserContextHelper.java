package edu.uams.clara.webapp.common.util;


import java.util.Set;

import edu.uams.clara.webapp.common.domain.usercontext.User;
import edu.uams.clara.webapp.common.domain.usercontext.UserRole;
import edu.uams.clara.webapp.common.domain.usercontext.enums.Committee;

public class UserContextHelper {

	//check whether the user is a member of one of the committtess
	public static boolean isMemberOfCommittees(final User user, final Set<Committee> committees){
		Set<UserRole> currentUserRoles = user.getUserRoles();

		boolean isMemberOfCommittees = false;

		for (UserRole ur : currentUserRoles) {
			Committee cmt = ur.getRole().getCommitee();

			if (committees.contains(cmt)) {
				return true;
			}
		}
		
		return isMemberOfCommittees;
	}
}
