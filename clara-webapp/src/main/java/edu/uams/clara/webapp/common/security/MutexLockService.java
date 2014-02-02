package edu.uams.clara.webapp.common.security;

import edu.uams.clara.webapp.common.domain.security.MutexLock;
import edu.uams.clara.webapp.common.domain.usercontext.User;

public interface MutexLockService {

	User whoIsEditing(Class<?> objectClass, long id);

	boolean isLockedByObjectClassAndIdForCurrentUser(Class<?> objectClass,
			long objectId, User currentUser);
	
	MutexLock getLockedByObjectClassAndId(Class<?> objectClass,
			long objectId);

	MutexLock lockObjectByClassAndIdAndUser(Class<?> objectClass,
			long objectId, User currentUser);

	void unlockAllMutexLockByUser(User currentUser);
	
	void unlockMutexLock(MutexLock mutexLock);
	
	void updateMutexLock(MutexLock mutexLock);
}
