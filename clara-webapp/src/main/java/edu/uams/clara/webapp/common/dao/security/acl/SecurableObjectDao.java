package edu.uams.clara.webapp.common.dao.security.acl;

import javax.persistence.TypedQuery;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import edu.uams.clara.core.dao.AbstractDomainDao;
import edu.uams.clara.webapp.common.domain.security.acl.SecurableObject;

@Repository
public class SecurableObjectDao extends AbstractDomainDao<SecurableObject> {

	private static final long serialVersionUID = 3407617647670705917L;

	@Transactional(readOnly=true)
	public SecurableObject getSecurableObjectByClassAndId(Class<?> objectClass,
			long objectId) {
		TypedQuery<SecurableObject> query = getEntityManager()
				.createQuery(
						"SELECT so FROM SecurableObject so WHERE so.retired = :retired AND so.objectClass = :objectClass AND so.objectId = :objectId",
						SecurableObject.class)
				.setParameter("retired", Boolean.FALSE)
				.setParameter("objectClass", objectClass)
				.setParameter("objectId", objectId);

		query.setHint("org.hibernate.cacheable", true);
		
		SecurableObject securableObject = null;
		try{
			securableObject = query.getSingleResult();
		}catch(EmptyResultDataAccessException ex){
			//ex.printStackTrace();
		}
		 
		return securableObject;
	}
	
	@Transactional(readOnly=true)
	public SecurableObject getSecurableObjectByClassAndIdAndObjectIdExpression(Class<?> objectClass,
			long objectId, String objectIdExpression) {
		TypedQuery<SecurableObject> query = getEntityManager()
				.createQuery(
						"SELECT so FROM SecurableObject so WHERE so.retired = :retired AND so.objectClass = :objectClass AND so.objectId = :objectId AND so.objectIdExpression = :objectIdExpression",
						SecurableObject.class)
				.setParameter("retired", Boolean.FALSE)
				.setParameter("objectClass", objectClass)
				.setParameter("objectId", objectId)
				.setParameter("objectIdExpression", objectIdExpression);

		query.setHint("org.hibernate.cacheable", true);
		
		SecurableObject securableObject = null;
		try{
			securableObject = query.getSingleResult();
		}catch(EmptyResultDataAccessException ex){
			//ex.printStackTrace();
		}
		 
		return securableObject;
	}
	
	
}
