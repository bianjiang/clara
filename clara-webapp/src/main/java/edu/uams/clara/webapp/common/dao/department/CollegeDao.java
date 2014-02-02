package edu.uams.clara.webapp.common.dao.department;

import java.util.List;

import javax.persistence.TypedQuery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import edu.uams.clara.core.dao.AbstractDomainDao;
import edu.uams.clara.webapp.common.domain.department.College;

@Repository
public class CollegeDao extends AbstractDomainDao<College> {

	private static final long serialVersionUID = 1236837339738494276L;
	
	private final static Logger logger = LoggerFactory
	.getLogger(CollegeDao.class);
	
	@Transactional(readOnly=true)
	public List<College> listAllOrderByName(){
		TypedQuery<College> query = getEntityManager()
		.createQuery(
				"SELECT c FROM College c WHERE c.retired = :retired ORDER BY c.name ASC", College.class)
		.setParameter("retired", Boolean.FALSE);
		query.setHint("org.hibernate.cacheable", true);
		
		return query.getResultList();
	}
	

	@Transactional(readOnly=true)
	public College findBySapCode(String sapCode){
		TypedQuery<College> query = getEntityManager()
		.createQuery(
				"SELECT c FROM College c WHERE c.retired = :retired AND c.sapCode LIKE :sapCode", College.class)
		.setParameter("retired", Boolean.FALSE).setParameter(
				"sapCode", sapCode);
		query.setHint("org.hibernate.cacheable", true);
		query.setFirstResult(0);
		query.setMaxResults(1);
		
		College college = null;
		try{
			college = query.getSingleResult();
		}catch(Exception ex){
			logger.warn(ex.getMessage());
		}
		
		return college;
	}
}
