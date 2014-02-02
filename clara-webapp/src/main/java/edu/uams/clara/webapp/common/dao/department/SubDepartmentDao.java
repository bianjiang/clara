package edu.uams.clara.webapp.common.dao.department;

import javax.persistence.TypedQuery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import edu.uams.clara.core.dao.AbstractDomainDao;
import edu.uams.clara.webapp.common.domain.department.SubDepartment;

@Repository
public class SubDepartmentDao extends AbstractDomainDao<SubDepartment> {

	private static final long serialVersionUID = -5581916909691765938L;
	
	private final static Logger logger = LoggerFactory
	.getLogger(SubDepartmentDao.class);

	@Transactional(readOnly=true)
	public SubDepartment findBySapCode(String sapCode){
		TypedQuery<SubDepartment> query = getEntityManager()
		.createQuery(
				"SELECT s FROM SubDepartment s WHERE s.retired = :retired AND s.sapCode LIKE :sapCode", SubDepartment.class)
		.setParameter("retired", Boolean.FALSE).setParameter(
				"sapCode", sapCode);
		query.setHint("org.hibernate.cacheable", true);
		query.setFirstResult(0);
		query.setMaxResults(1);
		
		SubDepartment subDepartment = null;
		try{
			subDepartment = query.getSingleResult();
		}catch(Exception ex){
			logger.warn(ex.getMessage());
		}
		
		return subDepartment;
	}
}
