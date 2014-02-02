package edu.uams.clara.webapp.fileserver.dao;

import javax.persistence.TypedQuery;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import edu.uams.clara.core.dao.AbstractDomainDao;
import edu.uams.clara.webapp.fileserver.domain.UploadedFile;

@Repository
public class UploadedFileDao extends AbstractDomainDao<UploadedFile> {

	private static final long serialVersionUID = -2287521062060865678L;
	
	@Transactional(readOnly = true)
	public UploadedFile getUploadedFile(String identifier, String path){
		String query ="SELECT upf FROM UploadedFile upf " +
				" WHERE upf.retired = :retired " +
				" AND upf.identifier =:identifier " +
				" AND upf.path =:path ";
		TypedQuery<UploadedFile> q = getEntityManager().createQuery(
				query, UploadedFile.class);
		
		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("path",
				path);
		q.setParameter("identifier", identifier);
		UploadedFile uploadedFile= null;
		try{
			uploadedFile= q.getSingleResult();
		}
		catch(Exception e){
		}
		return uploadedFile;
	}
	
	@Transactional(readOnly = true)
	public UploadedFile getUploadedFileByIdentifier(String identifier){
		String query ="SELECT upf FROM UploadedFile upf " +
				" WHERE upf.retired = :retired " +
				" AND upf.identifier =:identifier";
		TypedQuery<UploadedFile> q = getEntityManager().createQuery(
				query, UploadedFile.class);
		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("identifier", identifier);
		UploadedFile uploadedFile= null;
		try{
			uploadedFile= q.getResultList().get(0);
		}
		catch(Exception e){
		}
		return uploadedFile;
	}
	
	
}

