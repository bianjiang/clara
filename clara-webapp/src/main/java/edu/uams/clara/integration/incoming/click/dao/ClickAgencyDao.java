package edu.uams.clara.integration.incoming.click.dao;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import edu.uams.clara.integration.incoming.user.usercontext.dao.ClickUserCOIDao;

@Repository
public class ClickAgencyDao {
	private EntityManager em;

	private final static Logger logger = LoggerFactory
			.getLogger(ClickUserCOIDao.class);

	@Transactional(readOnly = true)
	public List<String> listAllClickAgencies() {
		String qry = "SELECT CONVERT(varchar(max),[name]) as name FROM [CLICK].[Grants].[dbo].[vUAMS_Click_Agencies]";
		Query query = em.createNativeQuery(qry);
			List<String> results = (List<String>) query.getResultList();
		return results;
	}

	public EntityManager getEm() {
		return em;
	}

	@PersistenceContext(unitName = "defaultPersistenceUnit")
	public void setEm(EntityManager em) {
		this.em = em;
	}
}
