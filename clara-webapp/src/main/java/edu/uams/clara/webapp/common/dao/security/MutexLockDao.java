package edu.uams.clara.webapp.common.dao.security;

import java.util.List;

import javax.persistence.TypedQuery;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import edu.uams.clara.core.dao.AbstractDomainDao;
import edu.uams.clara.webapp.common.domain.security.MutexLock;
import edu.uams.clara.webapp.common.domain.usercontext.User;

@Repository
public class MutexLockDao extends AbstractDomainDao<MutexLock> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8808598089502260725L;
	
	@Transactional(readOnly = true)
	public User getUserByObjectClassAndIdAndLockedStatus( Class<?> objectClass, long objectId, boolean locked){
		TypedQuery<User> query = getEntityManager()
				.createQuery(
						"SELECT ml.user FROM MutexLock ml WHERE ml.retired = :retired AND ml.objectId = :objectId AND ml.objectClass = :objectClass AND ml.locked = :locked",
						User.class)
				.setParameter("retired", Boolean.FALSE)
				.setParameter("objectClass", objectClass)
				.setParameter("objectId", objectId)
				.setParameter("locked", locked);

		query.setHint("org.hibernate.cacheable", true);
		query.setFirstResult(0);
		query.setMaxResults(1);		
		 
		try{
			return query.getSingleResult();
		}catch(Exception ex){
			return null;
		}
	}
	
	@Transactional(readOnly = true)
	public List<MutexLock> listMutexLockByObjectClassAndIdAndLockedStatus(Class<?> objectClass, long objectId, boolean locked){
		TypedQuery<MutexLock> query = getEntityManager()
				.createQuery(
						"SELECT ml FROM MutexLock ml WHERE ml.retired = :retired AND ml.objectId = :objectId AND ml.objectClass = :objectClass AND ml.locked = :locked",
						MutexLock.class)
				.setParameter("retired", Boolean.FALSE)
				.setParameter("objectClass", objectClass)
				.setParameter("objectId", objectId)
				.setParameter("locked", locked);

		query.setHint("org.hibernate.cacheable", true);		
		
		try{		 
			return query.getResultList();
		}catch(Exception ex){
			return null;
		}
	}
	
	@Transactional(readOnly = true)
	public MutexLock getMutexLockByObjectClassAndIdAndUserId(Class<?> objectClass, long objectId, long userId){
		TypedQuery<MutexLock> query = getEntityManager()
				.createQuery(
						"SELECT ml FROM MutexLock ml WHERE ml.retired = :retired AND ml.objectId = :objectId AND ml.objectClass = :objectClass AND ml.user.id = :userId",
						MutexLock.class)
				.setParameter("retired", Boolean.FALSE)
				.setParameter("objectClass", objectClass)
				.setParameter("objectId", objectId)
				.setParameter("userId", userId);

		query.setHint("org.hibernate.cacheable", true);		
		query.setFirstResult(0);
		query.setMaxResults(1);		
		 
		try{
			return query.getSingleResult();
		}catch(Exception ex){
			return null;
		}
	}
	
	@Transactional(readOnly = true)
	public List<MutexLock> findAllByUserId(long userId){
		TypedQuery<MutexLock> query = getEntityManager()
				.createQuery(
						"SELECT ml FROM MutexLock ml WHERE ml.retired = :retired AND ml.user.id = :userId",
						MutexLock.class)
				.setParameter("retired", Boolean.FALSE)
				.setParameter("userId", userId);

		query.setHint("org.hibernate.cacheable", true);		

		 
		try{
			return query.getResultList();
		}catch(Exception ex){
			return null;
		}
	}
	
	@Transactional(readOnly = true)
	public List<MutexLock> findAllLockedByUserId(long userId){
		TypedQuery<MutexLock> query = getEntityManager()
				.createQuery(
						"SELECT ml FROM MutexLock ml WHERE ml.retired = :retired AND ml.locked = :locked AND ml.user.id = :userId",
						MutexLock.class)
				.setParameter("retired", Boolean.FALSE)
				.setParameter("userId", userId)
				.setParameter("locked", Boolean.TRUE);;

		query.setHint("org.hibernate.cacheable", true);		

		 
		try{
			return query.getResultList();
		}catch(Exception ex){
			return null;
		}
	}
	
	@Transactional(readOnly = true)
	public List<MutexLock> findAllLocked(){
		TypedQuery<MutexLock> query = getEntityManager()
				.createQuery(
						"SELECT ml FROM MutexLock ml WHERE ml.retired = :retired AND ml.locked = :locked",
						MutexLock.class)
				.setParameter("retired", Boolean.FALSE)
				.setParameter("locked", Boolean.TRUE);;

		query.setHint("org.hibernate.cacheable", true);		

		 
		try{
			return query.getResultList();
		}catch(Exception ex){
			return null;
		}
	}
	

}
