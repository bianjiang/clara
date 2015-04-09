package edu.uams.clara.integration.incoming.sap.dao;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class SalaryDao {
	private EntityManager em;

	private final static Logger logger = LoggerFactory
			.getLogger(SalaryDao.class);

	@Transactional(readOnly = true)
	public String findSalaryBySap(String sap) {

		String qry = "select e.AnnualSalary from "
				+ "[UAMSSQL08].[ITDevDataRepository].[dbo].[Clara_Employees] as e"
				+ " where e.SAPID like :sap";
		Query query = em.createNativeQuery(qry).setParameter("sap", sap);
		String result = "";
		try {
			result = result + query.getSingleResult();
			return result;
		} catch (Exception e) {
			return result;
		}

	}

	public EntityManager getEm() {
		return em;
	}

	@PersistenceContext(unitName = "defaultPersistenceUnit")
	public void setEm(EntityManager em) {
		this.em = em;
	}
}
