package edu.uams.clara.webapp.common.security.impl;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import edu.uams.clara.webapp.common.dao.security.MutexLockDao;
import edu.uams.clara.webapp.common.domain.security.MutexLock;
import edu.uams.clara.webapp.common.domain.usercontext.User;
import edu.uams.clara.webapp.common.security.MutexLockService;

public class MutexLockServiceImpl implements MutexLockService {
	
	private final static Logger logger = LoggerFactory
			.getLogger(MutexLockService.class);
	
	private MutexLockDao mutexLockDao;
	
	@Override
	public User whoIsEditing(Class<?> objectClass, long id) {
		User user = null;
		try{
			user = mutexLockDao.getUserByObjectClassAndIdAndLockedStatus(objectClass, id, true);
		}catch(Exception ex){
			//
		}
		return user;
	}

	//if the locked user is not current user, return true as the study has been locked by other user
	@Override
	public boolean isLockedByObjectClassAndIdForCurrentUser(Class<?> objectClass,
			long objectId, User currentUser) {
		List<MutexLock> mutexLocks = mutexLockDao.listMutexLockByObjectClassAndIdAndLockedStatus(objectClass, objectId, true);

		if(mutexLocks == null){
			return false;
		}else{
			for(MutexLock mutexLock:mutexLocks){
				logger.debug("formId: " + mutexLock.getObjectId());
				if(mutexLock.getUser().getId() == currentUser.getId()){
					return false;
				}else{
					return true;
				}
			}
		}
		return false;
	}
	
	@Override
	public MutexLock getLockedByObjectClassAndId(Class<?> objectClass,
			long objectId) {
		List<MutexLock> mutexLocks = mutexLockDao.listMutexLockByObjectClassAndIdAndLockedStatus(objectClass, objectId, true);

		return (mutexLocks!=null && !mutexLocks.isEmpty())?mutexLocks.get(0):null;
	}
	
	@Override
	public MutexLock lockObjectByClassAndIdAndUser(Class<?> objectClass,
			long objectId, User currentUser){
		MutexLock mutexLock = mutexLockDao.getMutexLockByObjectClassAndIdAndUserId(objectClass, objectId, currentUser.getId());

		if (mutexLock == null) {
			mutexLock = new MutexLock();
			mutexLock.setObjectClass(objectClass);
			mutexLock.setObjectId(objectId);
			mutexLock.setUser(currentUser);
			
		}
		
		mutexLock.setLocked(true);
		mutexLock.setRetired(false);
		mutexLock.setModified(new Date());

		return mutexLockDao.saveOrUpdate(mutexLock);
	}
	
	@Override
	public void unlockAllMutexLockByUser(User currentUser){
		
		List<MutexLock> mutexLocks = mutexLockDao.findAllByUserId(currentUser.getId());
		
		for(MutexLock mutexLock:mutexLocks){
			mutexLock.setLocked(false);
			mutexLockDao.saveOrUpdate(mutexLock);
		}
	}
	
	@Override
	public void unlockMutexLock(MutexLock mutexLock) {
		mutexLock.setLocked(false);
		
		mutexLockDao.saveOrUpdate(mutexLock);
	}
	
	@Override
	public void updateMutexLock(MutexLock mutexLock) {
		mutexLock.setModified(new Date());
		
		mutexLockDao.saveOrUpdate(mutexLock);
	}

	public MutexLockDao getMutexLockDao() {
		return mutexLockDao;
	}

	@Autowired(required=true)
	public void setMutexLockDao(MutexLockDao mutexLockDao) {
		this.mutexLockDao = mutexLockDao;
	}
}
