package edu.uams.clara.migration.dao;

import java.util.Date;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import edu.uams.clara.webapp.protocol.dao.ProtocolDao;

public class MigrationDao {
	private final static Logger logger = LoggerFactory
			.getLogger(ProtocolDao.class);
	private EntityManager em;
	
	@Transactional
	public void insertProtocol(long id, int concurrent_version,
			Date created, String meta_data_xml, String protocol_identifier) {
		logger.debug("creating......");
		String query = "SET IDENTITY_INSERT [clara].[dbo].[protocol]  ON;"
				+ " insert into [clara].[dbo].[protocol] (id, concurrent_version, retired, created, locked, meta_data_xml, protocol_identifier) values (:id, :concurrent_version, :retired, :created, :locked, :meta_data_xml, :protocol_identifier);"
				+ "" + "SET IDENTITY_INSERT  [clara].[dbo].[protocol]  OFF;";

		Query qry = em.createNativeQuery(query);
		qry.setParameter("id", id);
		qry.setParameter("concurrent_version", concurrent_version);
		qry.setParameter("retired", false);
		qry.setParameter("locked", false);
		qry.setParameter("created", created);
		qry.setParameter("meta_data_xml", meta_data_xml);
		qry.setParameter("protocol_identifier", protocol_identifier);

		qry.executeUpdate();

		/*
		 * String query1 =
		 * "SELECT id FROM [clara].[dbo].[protocol] WHERE id = :id AND retired = :retired"
		 * ; Query qry1 = getEntityManager().createNativeQuery(query1);
		 * qry1.setParameter("id", id); qry1.setParameter("retired",
		 * Boolean.FALSE); String query1Result = ""; try {
		 * qry1.getSingleResult(); query1Result=""+qry1.getSingleResult(); }
		 * catch (Exception e) { query1Result ="null"; }
		 * 
		 * if (query1Result.equals("null")){ logger.debug("creating......");
		 * String query = "SET IDENTITY_INSERT [clara].[dbo].[protocol]  ON;" +
		 * " insert into [clara].[dbo].[protocol] (id, concurrent_version, retired, created, locked, meta_data_xml, protocol_identifier) values (:id, :concurrent_version, :retired, :created, :locked, :meta_data_xml, :protocol_identifier);"
		 * + "" + "SET IDENTITY_INSERT  [clara].[dbo].[protocol]  OFF;";
		 * 
		 * Query qry = getEntityManager().createNativeQuery(query);
		 * qry.setParameter("id", id); qry.setParameter("concurrent_version",
		 * concurrent_version); qry.setParameter("retired", false);
		 * qry.setParameter("locked", false); qry.setParameter("created",
		 * created); qry.setParameter("meta_data_xml", meta_data_xml);
		 * qry.setParameter("protocol_identifier", protocol_identifier);
		 * 
		 * qry.executeUpdate(); }
		 * 
		 * if (!query1Result.equals("null")) { // protocol existed String query2
		 * =
		 * "UPDATE [clara].[dbo].[protocol] set retired = :retired, locked =:locked, meta_data_xml = :meta_data_xml, created =:created,concurrent_version=:concurrent_version, protocol_identifier =:protocol_identifier"
		 * + "  where id =:id"; Query qry2 =
		 * getEntityManager().createNativeQuery(query2); qry2.setParameter("id",
		 * id); qry2.setParameter("concurrent_version", concurrent_version);
		 * qry2.setParameter("created", created);
		 * qry2.setParameter("meta_data_xml", meta_data_xml);
		 * qry2.setParameter("retired", false); qry2.setParameter("locked",
		 * false); qry2.setParameter("protocol_identifier",
		 * protocol_identifier); qry2.executeUpdate(); } else {
		 * logger.debug("creating......"); String query =
		 * "SET IDENTITY_INSERT [clara].[dbo].[protocol]  ON;" +
		 * " insert into [clara].[dbo].[protocol] (id, concurrent_version, retired, created, locked, meta_data_xml, protocol_identifier) values (:id, :concurrent_version, :retired, :created, :locked, :meta_data_xml, :protocol_identifier);"
		 * + "" + "SET IDENTITY_INSERT  [clara].[dbo].[protocol]  OFF;";
		 * 
		 * Query qry = getEntityManager().createNativeQuery(query);
		 * qry.setParameter("id", id); qry.setParameter("concurrent_version",
		 * concurrent_version); qry.setParameter("retired", false);
		 * qry.setParameter("locked", false); qry.setParameter("created",
		 * created); qry.setParameter("meta_data_xml", meta_data_xml);
		 * qry.setParameter("protocol_identifier", protocol_identifier);
		 * 
		 * qry.executeUpdate(); }
		 */

	}
	
	public EntityManager getEm() {
		return em;
	}

	@PersistenceContext(unitName = "defaultPersistenceUnit")
	public void setEm(EntityManager em) {
		this.em = em;
	}

}
