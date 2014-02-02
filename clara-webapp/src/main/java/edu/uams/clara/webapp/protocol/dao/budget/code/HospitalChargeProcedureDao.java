package edu.uams.clara.webapp.protocol.dao.budget.code;

import java.util.List;

import javax.persistence.TypedQuery;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import edu.uams.clara.core.dao.AbstractDomainDao;
import edu.uams.clara.webapp.protocol.domain.budget.code.HospitalChargeProcedure;

@Repository
public class HospitalChargeProcedureDao extends AbstractDomainDao<HospitalChargeProcedure>  {

	private static final long serialVersionUID = 7751825728770762459L;

	@Transactional(readOnly=true)
	public List<HospitalChargeProcedure> findByCptCode(String cptCode){
		 
		String query = "SELECT hcp FROM HospitalChargeProcedure hcp "
			+ " WHERE hcp.retired = :retired AND " +
			" (hcp.cptCode LIKE :cptCode OR hcp.cptCode IN" +
				" (SELECT ccm.otherCode FROM CPTCodeMapping ccm WHERE ccm.retired = :retired AND ccm.cptCode = :cptCode))";
		
		TypedQuery<HospitalChargeProcedure> q = getEntityManager().createQuery(query,
				HospitalChargeProcedure.class);
		q.setHint("org.hibernate.cacheable", true);
		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("cptCode", cptCode);
		
		return q.getResultList();	
	}
	
	@Transactional(readOnly=true)
	public HospitalChargeProcedure findFirstByCptCode(String cptCode){
		 
		String query = "SELECT hcp FROM HospitalChargeProcedure hcp "
			+ " WHERE hcp.retired = :retired AND " +
			" (hcp.cptCode LIKE :cptCode OR hcp.cptCode IN" +
				" (SELECT ccm.otherCode FROM CPTCodeMapping ccm WHERE ccm.retired = :retired AND ccm.cptCode = :cptCode))";
		
		TypedQuery<HospitalChargeProcedure> q = getEntityManager().createQuery(query,
				HospitalChargeProcedure.class);
		
		q.setFirstResult(0);
		q.setMaxResults(1);
		
		q.setHint("org.hibernate.cacheable", true);
		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("cptCode", cptCode);
		
		return q.getSingleResult();	
	}
	
	@Transactional(readOnly=true)
	public HospitalChargeProcedure findByCptCodeOnly(String cptCode){
		 
		String query = "SELECT hcp FROM HospitalChargeProcedure hcp "
			+ " WHERE hcp.retired = :retired AND hcp.cptCode LIKE :cptCode";
		
		TypedQuery<HospitalChargeProcedure> q = getEntityManager().createQuery(query,
				HospitalChargeProcedure.class);
		
		q.setFirstResult(0);
		q.setMaxResults(1);
		
		q.setHint("org.hibernate.cacheable", true);
		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("cptCode", cptCode);
		
		return q.getSingleResult();	
	}
	/*
	@Transactional(readOnly=true)
	public HospitalChargeProcedure findById(String id){
		String query = "SELECT hcp FROM HospitalChargeProcedure hcp "
				+ " WHERE hcp.retired = :retired AND hcp.id LIKE :id ";
			
			TypedQuery<HospitalChargeProcedure> q = getEntityManager().createQuery(query,
					HospitalChargeProcedure.class);
			q.setHint("org.hibernate.cacheable", true);
			q.setParameter("retired", Boolean.FALSE);
			q.setParameter("id", id);
			
			return q.getSingleResult();	
		
	}
	*/
}
