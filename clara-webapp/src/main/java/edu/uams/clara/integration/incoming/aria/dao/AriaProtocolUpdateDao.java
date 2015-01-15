package edu.uams.clara.integration.incoming.aria.dao;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.springframework.transaction.annotation.Transactional;

import edu.uams.clara.integration.outgoing.ctms.domain.AriaUser;


public class AriaProtocolUpdateDao {

	private EntityManager em;

	@Transactional(readOnly = true)
	public List<Object[]> getAllAriaProtocols() {
		String qry = "SELECT [AriaProtocolsID],[ProtocolCode] FROM [clara_dev].[dbo].[ARIAProtocols] ";
		Query query = em.createNativeQuery(qry);
		try {
			List<Object[]> result = (List<Object[]>) query.getResultList();
			return result;
		} catch (Exception e) {
			return null;
		}
	}
	
	@Transactional(readOnly = true)
	public int getAriaProtocolByProtocolID(String protocolCode){
		String qry = "SELECT [AriaProtocolsID] FROM [clara_dev].[dbo].[ARIAProtocols] " +
				" WHERE ProtocolCode = :protocolCode";
		Query query = em.createNativeQuery(qry);
		query.setParameter("protocolCode", protocolCode);
		int result =0;
		try {
			 result = (Integer) query.getSingleResult();
			return result;
		} catch (Exception e) {
			return result;
		}
	}

	
	@Transactional
	public void insertAriaProtocols(Date recordDate,
			String protocolCode, String protocolDescription, String PIName,
			String protocolStatus) {
		String qry = "insert into [clara_dev].[dbo].[ARIAProtocols] (RecordDate, ProtocolCode, ProtocolDescription, PIName, ProtocolStatus) values " +
				"(:recordDate, :protocolCode, :protocolDescription, :PIName, :protocolStatus)";
		
		Query query = em.createNativeQuery(qry);
		query.setParameter("recordDate", recordDate);
		query.setParameter("protocolCode", protocolCode);
		query.setParameter("protocolDescription", protocolDescription);
		query.setParameter("PIName", PIName);
		query.setParameter("protocolStatus", protocolStatus);

		query.executeUpdate();
	}
	
	@Transactional
	public void updateAriaProtocols(int AriaProtocolsID, Date recordDate,
			String protocolCode, String protocolDescription, String PIName,
			String protocolStatus) {
		String qry = "update [clara_dev].[dbo].[ARIAProtocols] set RecordDate=:recordDate, ProtocolCode=:protocolCode," +
				" ProtocolDescription=:protocolDescription, PIName=:PIName, ProtocolStatus=:protocolStatus " +
				" WHERE AriaProtocolsID =:AriaProtocolsID";
		
		Query query = em.createNativeQuery(qry);
		query.setParameter("recordDate", recordDate);
		query.setParameter("protocolCode", protocolCode);
		query.setParameter("protocolDescription", protocolDescription);
		query.setParameter("PIName", PIName);
		query.setParameter("protocolStatus", protocolStatus);
		query.setParameter("AriaProtocolsID", AriaProtocolsID);

		query.executeUpdate();
	}
	
	@Transactional(readOnly = true)
	public List<Object[]> findAllAriaUserIDinARIAUserProtocol(){
		String qry = "SELECT [ARIAUserCode], [ARIAProtocolCode], [ARIAUserProtocolID] FROM [clara_dev].[dbo].[ARIAUserProtocol] ";
		Query query = em.createNativeQuery(qry);
		try {
			List<Object[]> result = (List<Object[]>) query.getResultList();
			return result;
		} catch (Exception e) {
			return null;
		}
	}
	
	@Transactional(readOnly = true)
	public int findARIAUserProtocolID(String ARIAUserCode, String ARIAProtocolCode){
		String qry = "SELECT [ARIAUserProtocolID] FROM [clara_dev].[dbo].[ARIAUserProtocol] " +
				"WHERE ARIAProtocolCode=:ARIAProtocolCode AND ARIAUserCode=:ARIAUserCode";
		Query query = em.createNativeQuery(qry);
		query.setParameter("ARIAProtocolCode", ARIAProtocolCode);
		query.setParameter("ARIAUserCode", ARIAUserCode);
		
		int result =0;
		try {
			 result = (Integer) query.getSingleResult();
			return result;
		} catch (Exception e) {
			return result;
		}
	}
	
	@Transactional(readOnly = true)
	public void updateAriaUserIDinARIAUserProtocol(int ARIAUserProtocolID, Date recordDate,
			String ARIAUserCode) {
		String qry = "update [clara_dev].[dbo].[ARIAUserProtocol] set RecordDate=:recordDate, ARIAUserCode=:ARIAUserCode "+
				" WHERE ARIAUserProtocolID =:ARIAUserProtocolID";
		Query query = em.createNativeQuery(qry);
		query.setParameter("recordDate", recordDate);
		query.setParameter("ARIAUserProtocolID", ARIAUserProtocolID);
		query.setParameter("ARIAUserCode", ARIAUserCode);

		query.executeUpdate();
		
	}
	
	@Transactional(readOnly = true)
	public void updateAriaUser(int ARIAUserID, Date recordDate,String ARIAUserCode,String Name,String PhoneNumber,
			String EmailAddress) {
		String qry = "update [clara_dev].[dbo].[ARIAUser] set RecordDate=:recordDate, ARIAUserCode=:ARIAUserCode , EmailAddress=:EmailAddress , Name=:Name , PhoneNumber=:PhoneNumber"+
				" WHERE ARIAUserID =:ARIAUserID";
		Query query = em.createNativeQuery(qry);
		query.setParameter("recordDate", recordDate);
		query.setParameter("ARIAUserID", ARIAUserID);
		query.setParameter("ARIAUserCode", ARIAUserCode);
		query.setParameter("Name", Name);
		query.setParameter("PhoneNumber", PhoneNumber);
		query.setParameter("EmailAddress", EmailAddress);
		query.executeUpdate();
		
	}
	
	@Transactional(readOnly = true)
	public void updateAriaUserUserCodeOnly(int ARIAUserID, Date recordDate,String ARIAUserCode) {
		String qry = "update [clara_dev].[dbo].[ARIAUser] set RecordDate=:recordDate, ARIAUserCode=:ARIAUserCode"+
				" WHERE ARIAUserID =:ARIAUserID";
		Query query = em.createNativeQuery(qry);
		query.setParameter("recordDate", recordDate);
		query.setParameter("ARIAUserID", ARIAUserID);
		query.setParameter("ARIAUserCode", ARIAUserCode);

		query.executeUpdate();
		
	}
	
	@Transactional(readOnly = true)
	public void insertARIAUserProtocol(Date recordDate,
			String ARIAUserCode,String ARIAProtocolCode) {
		String qry = "insert into [clara_dev].[dbo].[ARIAUserProtocol] (RecordDate, ARIAUserCode, ARIAProtocolCode) values " +
				"(:recordDate, :ARIAUserCode, :ARIAProtocolCode)";
		Query query = em.createNativeQuery(qry);
		query.setParameter("recordDate", recordDate);
		query.setParameter("ARIAProtocolCode", ARIAProtocolCode);
		query.setParameter("ARIAUserCode", ARIAUserCode);

		query.executeUpdate();
	}
	
	@Transactional(readOnly = true)
	public String findAriaUserSapByUserID(long userID) {
		
		TypedQuery<AriaUser> query = getEntityManager()
				.createQuery(
						"SELECT a FROM AriaUser a WHERE a.retired = :retired AND a.piSerial = :userID", AriaUser.class);
		
		// q.setHint("org.hibernate.cacheable", true);
		query.setParameter("userID", userID);
		query.setParameter("retired", Boolean.FALSE);
		
		query.setParameter("userID", userID);
		String result="";
		try {
			result = query.getSingleResult().getSapID();
			return result;
		} catch (Exception e) {
			return result;
		}
	}
	
	@Transactional(readOnly = true)
	public List<Object[]> findARIAUser(){
		String qry = "SELECT [ARIAUserID], [ARIAUserCode] FROM [clara_dev].[dbo].[ARIAUser] ";
		Query query = em.createNativeQuery(qry);
		
		try {
			List<Object[]> result = (List<Object[]>) query.getResultList();
			return result;
		} catch (Exception e) {
			return null;
		}
	}
	

	public EntityManager getEntityManager() {
		return em;
	}

	@PersistenceContext(unitName = "defaultPersistenceUnit")
	public void setEm(EntityManager em) {
		this.em = em;
	}

}
