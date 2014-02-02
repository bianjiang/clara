package edu.uams.clara.webapp.common.service.relation;

import java.util.List;

import edu.uams.clara.webapp.common.domain.relation.RelatedObject;

public interface RelationService {
	RelatedObject addRelationByIdAndType(long objectId, long relatedObjectId, String objectType, String relatedObjectType);
	RelatedObject removeRelationByIdAndType(long objectId, long relatedObjectId, String objectType, String relatedObjectType);
	List<RelatedObject> getRelationsByIdAndType(long id, String type);
	void removeRelation(RelatedObject relatedObject);
}
