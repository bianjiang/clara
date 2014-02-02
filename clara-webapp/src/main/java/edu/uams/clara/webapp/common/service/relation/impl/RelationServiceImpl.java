package edu.uams.clara.webapp.common.service.relation.impl;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import edu.uams.clara.webapp.common.dao.relation.RelatedObjectDao;
import edu.uams.clara.webapp.common.domain.relation.RelatedObject;
import edu.uams.clara.webapp.common.service.relation.RelationService;

public class RelationServiceImpl implements RelationService {
	private RelatedObjectDao relatedObjectDao;

	@Override
	public RelatedObject addRelationByIdAndType(long objectId,
			long relatedObjectId, String objectType, String relatedObjectType) {
		RelatedObject relatedObject = new RelatedObject();
		relatedObject.setCreated(new Date());
		relatedObject.setObjectId(objectId);
		relatedObject.setObjectType(objectType);
		relatedObject.setRelatedObjectId(relatedObjectId);
		relatedObject.setRelatedObjectType(relatedObjectType);
		
		return relatedObjectDao.saveOrUpdate(relatedObject);
	}

	@Override
	public RelatedObject removeRelationByIdAndType(long objectId,
			long relatedObjectId, String objectType, String relatedObjectType) {
		try{
			RelatedObject relatedObject = relatedObjectDao.getRelationByObjectIdAndObjectType(objectId, relatedObjectId, objectType, relatedObjectType);

			relatedObject.setRetired(true);
			
			return relatedObjectDao.saveOrUpdate(relatedObject);
		} catch (Exception e){
			e.printStackTrace();
			
			return null;
		}
	}

	@Override
	public List<RelatedObject> getRelationsByIdAndType(long id, String type) {
		List<RelatedObject> relatedObjectLst = relatedObjectDao.listRelatedObjectByIdAndType(id, type);
		
		return (relatedObjectLst!=null)?relatedObjectLst:null;
	}
	
	@Override
	public void removeRelation(RelatedObject relatedObject) {
		if (relatedObject != null) {
			relatedObject.setRetired(true);
			
			relatedObject = relatedObjectDao.saveOrUpdate(relatedObject);
		}
		
	}

	public RelatedObjectDao getRelatedObjectDao() {
		return relatedObjectDao;
	}
	
	@Autowired(required = true)
	public void setRelatedObjectDao(RelatedObjectDao relatedObjectDao) {
		this.relatedObjectDao = relatedObjectDao;
	}
}
