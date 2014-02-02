package edu.uams.clara.webapp.protocol.dao.budget.code;

import java.util.List;

import javax.persistence.TypedQuery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import edu.uams.clara.core.dao.AbstractDomainDao;
import edu.uams.clara.webapp.protocol.domain.budget.code.CPTCode;

@Repository
public class CPTCodeDao extends AbstractDomainDao<CPTCode> {

	private final static Logger logger = LoggerFactory
	.getLogger(CPTCodeDao.class);
	
	private static final long serialVersionUID = 1633250832299701904L;
	@Transactional(readOnly=true)
	public List<CPTCode> findByKeyword(String keyword){
				 
		String query = "SELECT c FROM CPTCode c "
			+ " WHERE c.retired = :retired AND (c.code LIKE :keyword OR UPPER(c.shortDescription) LIKE UPPER(:keyword)) ";
				
		TypedQuery<CPTCode> q = getEntityManager().createQuery(query,
				CPTCode.class);
		q.setHint("org.hibernate.cacheable", true);
		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("keyword", "%" + keyword + "%");
		
		return q.getResultList();	
	}
	
	@Transactional(readOnly=true)
	public CPTCode findByCode(String code){
		TypedQuery<CPTCode> query = getEntityManager()
		.createQuery(
				"SELECT c FROM CPTCode c WHERE c.retired = :retired AND c.code LIKE :code", CPTCode.class)
		.setParameter("retired", Boolean.FALSE).setParameter(
				"code", code);
		query.setHint("org.hibernate.cacheable", true);
		query.setFirstResult(0);
		query.setMaxResults(1);
		
		CPTCode cptCode = null;
		try{
			cptCode = query.getSingleResult();
		}catch(Exception ex){
			logger.warn(ex.getMessage());
		}
		
		return cptCode;
	}

}
