package edu.uams.clara.webapp.common.dao.security.acl;

import java.util.List;

import javax.persistence.TypedQuery;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import edu.uams.clara.core.dao.AbstractDomainDao;
import edu.uams.clara.webapp.common.domain.security.acl.SecurableObject;
import edu.uams.clara.webapp.common.domain.security.acl.SecurableObjectAcl;
import edu.uams.clara.webapp.common.domain.security.acl.enums.Permission;

@Repository
public class SecurableObjectAclDao extends AbstractDomainDao<SecurableObjectAcl> {

	private static final long serialVersionUID = -7238938351076281918L;
	
	public List<Permission> getUserPermissionsOnSecurableObjectByOwnerAndObject(Class<?> ownerClass, long ownerId, Class<?> objectClass, long objectId){
		TypedQuery<Permission> query = getEntityManager()
				.createQuery(
						" SELECT soa.permission FROM SecurableObjectAcl soa, SecurableObject so " +
						" WHERE soa.securableObject.id = so.id AND soa.ownerClass = :ownerClass " +
						" AND soa.ownerId = :ownerId " +
						" AND so.objectId = :objectId " +
						" AND so.objectClass = :objectClass AND so.retired = :retired AND soa.retired = :retired ",
						Permission.class)
				.setParameter("ownerClass", ownerClass)
				.setParameter("ownerId", ownerId)
				.setParameter("objectClass", objectClass)
				.setParameter("objectId", objectId)
				.setParameter("retired", Boolean.FALSE)
				;
		query.setHint("org.hibernate.cacheable", true);
		
		return query.getResultList();
		
		
	}
	
	@Transactional(readOnly=true)
	public List<SecurableObjectAcl> getSecurableObjectaclBysecurableObejctID(long seObjId){
		TypedQuery<SecurableObjectAcl> query = getEntityManager()
				.createQuery(
						" SELECT soa FROM SecurableObjectAcl soa " +
						" WHERE soa.securableObject.id = :seObjId " ,
						//" AND soa.retired = :retired",
						SecurableObjectAcl.class)
				//.setParameter("retired", Boolean.FALSE)
				.setParameter("seObjId", seObjId)
				;
			query.setHint("org.hibernate.cacheable", true);
		
		return query.getResultList();
		
	}
	

	/*since we need to reuse the same acl object, so we don't care whether it's retired or not*/
	@Transactional(readOnly=true)
	public SecurableObjectAcl getSecurableObjectAclByOwnerAndSecurableObjectAndPermission(Class<?> ownerClass,
			long ownerId, SecurableObject securableObject, Permission permission) {
		TypedQuery<SecurableObjectAcl> query = getEntityManager()
				.createQuery(
						" SELECT soa FROM SecurableObjectAcl soa " +
						" WHERE soa.ownerClass = :ownerClass " +
						" AND soa.ownerId = :ownerId " +
						" AND soa.securableObject = :securableObject " +
						" AND soa.permission = :permission ",
						//" AND soa.retired = :retired",
						SecurableObjectAcl.class)
				//.setParameter("retired", Boolean.FALSE)
				.setParameter("ownerClass", ownerClass)
				.setParameter("ownerId", ownerId)
				.setParameter("securableObject", securableObject)
				.setParameter("permission", permission)
				;

		query.setHint("org.hibernate.cacheable", true);
		query.setFirstResult(0);
		query.setMaxResults(1);
		
		SecurableObjectAcl securableObjectAcl = null;
		try{
			securableObjectAcl = query.getSingleResult();
		}catch(EmptyResultDataAccessException ex){
			//ex.printStackTrace();
		}
		 
		return securableObjectAcl;
	}
	
}