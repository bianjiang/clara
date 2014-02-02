package edu.uams.clara.webapp.common.dao.relation;

import java.util.List;

import javax.persistence.TypedQuery;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import edu.uams.clara.core.dao.AbstractDomainDao;
import edu.uams.clara.webapp.common.domain.relation.RelatedObject;

@Repository
public class RelatedObjectDao extends AbstractDomainDao<RelatedObject> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 9043504963277475605L;

	@Transactional(readOnly = true)
	public RelatedObject getRelationByObjectIdAndObjectType(long objectId,
			long relatedObjectId, String objectType, String relatedObjectType) {
		String query = "SELECT ro FROM RelatedObject ro WHERE ro.objectId IN (:objectId, :relatedObjectId) AND ro.objectType IN (:objectType, :relatedObjectType) AND ro.relatedObjectId IN (:objectId, :relatedObjectId) AND ro.relatedObjectType IN (:objectType, :relatedObjectType) AND ro.retired = :retired";

		TypedQuery<RelatedObject> q = getEntityManager().createQuery(query,
				RelatedObject.class);
		q.setFirstResult(0);
		q.setMaxResults(1);
		q.setHint("org.hibernate.cacheable", true);
		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("objectId", objectId);
		q.setParameter("relatedObjectId", relatedObjectId);
		q.setParameter("objectType", objectType);
		q.setParameter("relatedObjectType", relatedObjectType);

		return q.getSingleResult();
	}

	@Transactional(readOnly = true)
	public List<RelatedObject> listRelatedObjectByIdAndType(long id, String type) {
		String query = "SELECT ro FROM RelatedObject ro WHERE ro.retired = :retired AND ((ro.objectId = :id AND ro.objectType = :type) OR (ro.relatedObjectId = :id AND ro.relatedObjectType = :type))";

		TypedQuery<RelatedObject> q = getEntityManager().createQuery(query,
				RelatedObject.class);
		q.setHint("org.hibernate.cacheable", true);
		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("id", id);
		q.setParameter("type", type);

		return q.getResultList();
	}

	@Transactional(readOnly = true)
	public RelatedObject findRelatedObjectByObjIDTypeandRelatedObjIDType(
			long ojectID, String objectType, long relatedObjectID,
			String relatedObjectType) {
		String query = "SELECT ro FROM RelatedObject ro WHERE ro.retired = :retired AND ro.objectId = :ojectID AND ro.objectType = :objectType AND ro.relatedObjectId = :relatedObjectID AND ro.relatedObjectType = :relatedObjectType";
		TypedQuery<RelatedObject> q = getEntityManager().createQuery(query,
				RelatedObject.class);
		q.setHint("org.hibernate.cacheable", true);
		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("ojectID", ojectID);
		q.setParameter("objectType", objectType);
		q.setParameter("relatedObjectID", relatedObjectID);
		q.setParameter("relatedObjectType", relatedObjectType);

		RelatedObject relatedObject = null;
		relatedObject = q.getSingleResult();

		return relatedObject;

	}
}
