package edu.uams.clara.webapp.protocol.dao.thing;

import java.util.List;

import javax.persistence.TypedQuery;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import edu.uams.clara.core.dao.AbstractDomainDao;
import edu.uams.clara.webapp.protocol.domain.thing.Site;

@Repository
public class SiteDao extends AbstractDomainDao<Site> {

	private static final long serialVersionUID = -6981936144855117514L;

	@Transactional(readOnly = true)
	public List<Site> searchByKeywordAndCommon(String keyword, Boolean common){
	
		
		TypedQuery<Site> query = getEntityManager()
		.createQuery(
				"SELECT s FROM Site s"
			+ " WHERE s.retired = :retired"
			+ ((keyword != null)?" AND ("
				+ " s.nciIdentifier LIKE :keyword"
				+ " OR s.siteName LIKE :keyword" 
				+ " OR s.address LIKE :keyword"
				+ " OR s.city LIKE :keyword"
				+ " OR s.zip LIKE :keyword"
			+ ") ":"")
			+ (common == null?"":" AND s.common = :common")
			+ " ORDER BY s.siteName ASC",
				Site.class);
		
		query.setHint("org.hibernate.cacheable", true);
		
		query.setParameter("retired", Boolean.FALSE);
		if(common != null){
			query.setParameter("common", common);
		}
		if(keyword != null){
			query.setParameter("keyword", "%" + keyword + "%");
		}
		return query.getResultList();
	}
	
	@Transactional(readOnly = true)
	public List<Site> listUnapprovedSites(){
		
		TypedQuery<Site> query = getEntityManager()
		.createQuery(
				"SELECT s FROM Site s"
			+ " WHERE s.retired = :retired"
			+ " AND s.approved = 0"
			+ " ORDER BY s.siteName ASC",
				Site.class);
		
		query.setHint("org.hibernate.cacheable", true);		
		query.setParameter("retired", Boolean.FALSE);
		return query.getResultList();
	}
}
