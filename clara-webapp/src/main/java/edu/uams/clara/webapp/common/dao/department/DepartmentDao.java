package edu.uams.clara.webapp.common.dao.department;

import java.util.List;

import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import edu.uams.clara.core.dao.AbstractDomainDao;
import edu.uams.clara.webapp.common.domain.department.Department;

@Repository
public class DepartmentDao extends AbstractDomainDao<Department> {

	private static final long serialVersionUID = -1700717057622632798L;
	
	private final static Logger logger = LoggerFactory
	.getLogger(DepartmentDao.class);

	@Transactional(readOnly=true)
	public Department findBySapCode(String sapCode){
		TypedQuery<Department> query = getEntityManager()
		.createQuery(
				"SELECT d FROM Department d WHERE d.retired = :retired AND d.sapCode LIKE :sapCode", Department.class)
		.setParameter("retired", Boolean.FALSE).setParameter(
				"sapCode", sapCode);
		query.setHint("org.hibernate.cacheable", true);
		query.setFirstResult(0);
		query.setMaxResults(1);
		
		Department department = null;
		try{
			department = query.getSingleResult();
		}catch(Exception ex){
			logger.warn(ex.getMessage());
		}
		
		return department;
	}
	
	@Transactional(readOnly=true)
	public List<Object[]> findALLDeptCodeMap(){
		//0, father dept, 1,child dept
		String qry="SELECT [OBJID],[SOBID] FROM [clara_dev].[dbo].[ZHRITORG]";
		Query query = getEntityManager().createNativeQuery(qry);
		List<Object[]> result = null;
		try{
			result = (List<Object[]>)query.getResultList();
			if(result.size()==0){
				result = null;
			}
		}
		catch(Exception e){
		}
		return result;
	}
	
	@Transactional(readOnly=true)
	public List<Department> findDeptsByCollegeId(long collegeId){
		
		TypedQuery<Department> query = getEntityManager()
		.createQuery(
				"SELECT d FROM Department d WHERE d.retired = :retired AND d.college.id  = :collegeId", Department.class)
		.setParameter("retired", Boolean.FALSE).setParameter(
				"collegeId", collegeId);
		query.setHint("org.hibernate.cacheable", true);
		
		List<Department> departments = null;
		try{
			departments = query.getResultList();
		}catch(Exception ex){
			logger.warn(ex.getMessage());
		}
		
		return departments;
	}
	
	@Transactional(readOnly=true)
	public List<Object[]> getAllDeptDescription(){
		String qry ="SELECT [OBJID],[STEXT] FROM [clara_dev].[dbo].[ZHRITDEPTXT]";
		Query query = getEntityManager().createNativeQuery(qry);
		
		List<Object[]> result =null;
		try{
			result = (List<Object[]>)query.getResultList();
		}
		catch(Exception e){
		}
		return result;
	}
	
	
}
