package edu.uams.clara.webapp.maintainence.dao;

import java.math.BigInteger;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.springframework.transaction.annotation.Transactional;

import edu.uams.clara.webapp.protocol.domain.Protocol;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.ProtocolFormCommitteeStatus;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolForm;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolFormXmlData;
import edu.uams.clara.webapp.protocol.domain.protocolform.enums.ProtocolFormXmlDataType;

public class MaintainenceDao {
	
	private EntityManager em;
	
	@Transactional(readOnly = true)
	public List<ProtocolFormXmlData> listProtocolFormXmlDatasHaveNormalProcedure(long protocolFormId){
		String qry = "SELECT pfxd.* FROM protocol_form_xml_data as pfxd"
				+ " WHERE  pfxd.retired = :retired"
				+ " AND pfxd.protocol_form_id = :protocolFormId"
				+ " AND pfxd.protocol_form_xml_data_type = 'BUDGET'"
				+ " AND pfxd.xml_data.exist('/budget/epochs/epoch/procedures/procedure[@type=\"normal\"]')=1";
		TypedQuery<ProtocolFormXmlData> q = (TypedQuery<ProtocolFormXmlData>) em
				.createNativeQuery(qry, ProtocolFormXmlData.class);
		q.setHint("org.hibernate.cacheable", false);
		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("protocolFormId", protocolFormId);
		//Query query = em.createNativeQuery(qry);
		//query.setParameter("retired", Boolean.FALSE);
		//query.setParameter("protocolId", protocolId);
		
		return q.getResultList();
	}
	
	@Transactional(readOnly = true)
	public List<BigInteger> listProtocolFormXmlDatasWithLatestApprovedBudget(){
		String qry = "SELECT max(pf.id) from protocol_form pf, protocol_form_status  pfs, protocol_form_xml_data  pfxd"
				+ " WHERE  pf.retired = :retired"
				+ " AND pf.id = pfs.protocol_form_id AND pfs.protocol_form_status IN ('IRB_ACKNOWLEDGED','IRB_APPROVED','EXPEDITED_APPROVED')"
				+ " AND pfs.retired = :retired"
				+ " AND pfxd.protocol_form_xml_data_type ='BUDGET'"
				+ " AND pfxd.retired =:retired AND pfxd.protocol_form_id = pf.id"
				+ " group by pf.protocol_id";
		/*TypedQuery<ProtocolFormXmlData> q = (TypedQuery<ProtocolFormXmlData>) em
				.createNativeQuery(qry, ProtocolFormXmlData.class);*/
		Query q = em.createNativeQuery(qry);
		
		q.setHint("org.hibernate.cacheable", false);
		q.setParameter("retired", Boolean.FALSE);
		//Query query = em.createNativeQuery(qry);
		//query.setParameter("retired", Boolean.FALSE);
		//query.setParameter("protocolId", protocolId);
		
		return q.getResultList();
	}
	
	
	@Transactional(readOnly = true)
	public List<Protocol> listProtocolWithoutStatusInMetaData(){
		String qry = "SELECT p.* FROM protocol p"
				+ " WHERE  p.retired = :retired"
				+ " AND p.meta_data_xml.exist('/protocol/status')=0";
		TypedQuery<Protocol> q = (TypedQuery<Protocol>) em
				.createNativeQuery(qry, Protocol.class);
		q.setHint("org.hibernate.cacheable", false);
		q.setParameter("retired", Boolean.FALSE);
		//Query query = em.createNativeQuery(qry);
		//query.setParameter("retired", Boolean.FALSE);
		//query.setParameter("protocolId", protocolId);
		
		return q.getResultList();
	}
	
	@Transactional(readOnly = true)
	public List<Protocol> listProtocolWithoutCorrectApprovalEndDate(String formStatus){
		String qry = "SELECT p.* from protocol p WHERE id IN ("
				+ " SELECT DISTINCT protocol_id FROM protocol_form WHERE id IN ("
				+ " SELECT DISTINCT protocol_form_id FROM protocol_form_status WHERE protocol_form_status = :formStatus AND retired = :retired)"
				+ " AND retired = :retired) AND retired = :retired";
		TypedQuery<Protocol> q = (TypedQuery<Protocol>) em
				.createNativeQuery(qry, Protocol.class);
		q.setHint("org.hibernate.cacheable", false);
		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("formStatus", formStatus);
		//Query query = em.createNativeQuery(qry);
		//query.setParameter("retired", Boolean.FALSE);
		//query.setParameter("protocolId", protocolId);
		
		return q.getResultList();
	}
	
	@Transactional(readOnly = true)
	public List<ProtocolFormXmlData> listProtocolFormXmlDataWithPharmacyFee(){
		String qry = "SELECT pfxd.* from protocol_form_xml_data pfxd WHERE retired = :retired"
				+ " AND pfxd.protocol_form_xml_data_type = 'BUDGET'" 
				+ " AND pfxd.xml_data.exist('/budget/expenses/expense[@type=\"Invoicable\" and @subtype=\"Pharmacy Fee\"]')=1";
		TypedQuery<ProtocolFormXmlData> q = (TypedQuery<ProtocolFormXmlData>) em
				.createNativeQuery(qry, ProtocolFormXmlData.class);
		q.setHint("org.hibernate.cacheable", false);
		q.setParameter("retired", Boolean.FALSE);
		//Query query = em.createNativeQuery(qry);
		//query.setParameter("retired", Boolean.FALSE);
		//query.setParameter("protocolId", protocolId);
		
		return q.getResultList();
	}
	
	@Transactional(readOnly = true)
	public String getEpicCdmByCptCode(String cptCode) {
		String qry = "SELECT epic_cdm_code FROM epic_cdm WHERE default_cpt_code = :cptCode";
		
		Query q = em.createNativeQuery(qry);
		
		q.setHint("org.hibernate.cacheable", false);
		q.setParameter("cptCode", cptCode);
		
		q.setFirstResult(0);
		q.setMaxResults(1);

		
		return q.getSingleResult().toString();
	}
	
	@Transactional(readOnly = true)
	public List<Protocol> listProtocolFromTempList(){
		String qry = "SELECT p.* from protocol p, temp_ccto_protocol tp WHERE p.id = tp.protocol_id AND p.retired = :retired";
		TypedQuery<Protocol> q = (TypedQuery<Protocol>) em
				.createNativeQuery(qry, Protocol.class);
		q.setHint("org.hibernate.cacheable", false);
		q.setParameter("retired", Boolean.FALSE);
		
		return q.getResultList();
	}
	
	@Transactional(readOnly = true)
	public List<ProtocolForm> listRevisionRequestedProtocolFormFrom(){
		String qry = "select pf.* from protocol_form pf where id in ("
				+ " select protocol_form_id from protocol_form_committee_status where protocol_form_committee_status = 'REVISION_REQUESTED' and retired = :retired)";
		TypedQuery<ProtocolForm> q = (TypedQuery<ProtocolForm>) em
				.createNativeQuery(qry, ProtocolForm.class);
		q.setHint("org.hibernate.cacheable", false);
		q.setParameter("retired", Boolean.FALSE);
		
		return q.getResultList();
	}
	
	@Transactional(readOnly = true)
	public ProtocolFormXmlData getOldestProtocolFormXmlData(long protocolFormParentId, ProtocolFormXmlDataType protocolFormXmlDataType){
		String qry = "select pfxd.* from protocol_form_xml_data pfxd where pfxd.protocol_form_id IN (select id from protocol_form where parent_id = :protocolFormParentId and retired = :retired) and pfxd.retired = :retired and pfxd.protocol_form_xml_data_type = :protocolFormXmlDataType order by pfxd.id";
		TypedQuery<ProtocolFormXmlData> q = (TypedQuery<ProtocolFormXmlData>) em
				.createNativeQuery(qry, ProtocolFormXmlData.class);
		q.setFirstResult(0);
		q.setMaxResults(1);
		
		q.setHint("org.hibernate.cacheable", false);
		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("protocolFormParentId", protocolFormParentId);
		q.setParameter("protocolFormXmlDataType", protocolFormXmlDataType.toString());
		
		return q.getSingleResult();
	}
	
	@Transactional(readOnly = true)
	public List<ProtocolFormXmlData> listProtocolFormXmlDataVersions(long protocolFormParentId, ProtocolFormXmlDataType protocolFormXmlDataType){
		String qry = "select * from protocol_form_xml_data where protocol_form_id IN (select id from protocol_form where parent_id = :protocolFormParentId and retired = :retired) and retired = :retired and protocol_form_xml_data_type = :protocolFormXmlDataType order by id";
		TypedQuery<ProtocolFormXmlData> q = (TypedQuery<ProtocolFormXmlData>) em
				.createNativeQuery(qry, ProtocolFormXmlData.class);
		
		q.setHint("org.hibernate.cacheable", false);
		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("protocolFormParentId", protocolFormParentId);
		q.setParameter("protocolFormXmlDataType", protocolFormXmlDataType.toString());
		
		return q.getResultList();
	}
	
	@Transactional(readOnly = true)
	public List<ProtocolFormCommitteeStatus> listPharmacyApprovedCommitteeStatus(){
		String qry = "SELECT * FROM protocol_form_committee_status WHERE committee = 'PHARMACY_REVIEW' AND protocol_form_committee_status = 'APPROVED' AND retired = :retired ORDER BY modified";
		TypedQuery<ProtocolFormCommitteeStatus> q = (TypedQuery<ProtocolFormCommitteeStatus>) em
				.createNativeQuery(qry, ProtocolFormCommitteeStatus.class);
		
		q.setHint("org.hibernate.cacheable", false);
		q.setParameter("retired", Boolean.FALSE);
		
		return q.getResultList();
	}
	
	@Transactional(readOnly = true)
	public List<Protocol> listProtocolMissingDepartmentList(){
		String qry = "SELECT * FROM protocol WHERE retired = :retired AND id IN (SELECT distinct protocol_id FROM protocol_status WHERE retired = :retired AND protocol_status ='open' AND id IN (SELECT max(id) FROM protocol_status WHERE retired = :retired GROUP BY protocol_id)"
					+ ")"
					+ " AND id NOT IN (SELECT protocol_id FROM test_studies)"
					+ " AND meta_data_xml.exist('/protocol/responsible-department')=0";
		TypedQuery<Protocol> q = (TypedQuery<Protocol>) em
				.createNativeQuery(qry, Protocol.class);
		q.setHint("org.hibernate.cacheable", false);
		q.setParameter("retired", Boolean.FALSE);
		
		return q.getResultList();
	}
	
	@Transactional(readOnly = true)
	public List<ProtocolFormXmlData> listPharmacyXmlData(){
		String qry = "SELECT * FROM protocol_form_xml_data WHERE protocol_form_xml_data_type = 'PHARMACY' AND retired = :retired"
				+ " AND xml_data.exist('/pharmacy/@waived')=1";
				//+ " AND xml_data.exist('/pharmacy/expenses/expense/@waived')=0";
		TypedQuery<ProtocolFormXmlData> q = (TypedQuery<ProtocolFormXmlData>) em
				.createNativeQuery(qry, ProtocolFormXmlData.class);
		q.setHint("org.hibernate.cacheable", false);
		q.setParameter("retired", Boolean.FALSE);
		
		return q.getResultList();
	}
	
	@Transactional(readOnly = true)
	public List<ProtocolForm> listPendingPIActionForms(){
		String qry = "SELECT pform.* FROM protocol_form pform WHERE pform.id in (SELECT MAX(pf.id) FROM protocol_form pf where retired = :retired group by pf.parent_id)"
				+ " and pform.id in (SELECT pfstatus.protocol_form_id FROM protocol_form_status pfstatus WHERE pfstatus.id  IN (SELECT MAX(pfs.id) FROM protocol_form pf, protocol_form_status pfs"
				+ " WHERE pf.retired = :retired AND pfs.retired = :retired AND pf.id = pfs.protocol_form_id GROUP BY pfs.protocol_form_id)"
				+ " AND pfstatus.protocol_form_status IN ('PENDING_PI_ENDORSEMENT','REVISION_PENDING_PI_ENDORSEMENT','PENDING_PI_SIGN_OFF','IRB_DEFERRED_WITH_MAJOR_CONTINGENCIES','IRB_DEFERRED_WITH_MINOR_CONTINGENCIES',"
				+ " 'REVISION_WITH_MAJOR_PENDING_PI_ENDORSEMENT','REVISION_WITH_MINOR_PENDING_PI_ENDORSEMENT','PENDING_TP_ENDORSEMENT','RESPONSE_TO_TABLED_PENDING_PI_ENDORSEMENT'))";
		TypedQuery<ProtocolForm> q = (TypedQuery<ProtocolForm>) em
				.createNativeQuery(qry, ProtocolForm.class);
		q.setHint("org.hibernate.cacheable", false);
		q.setParameter("retired", Boolean.FALSE);
		
		return q.getResultList();
	}
	
	@Transactional(readOnly = true)
	public List<ProtocolFormXmlData> listBudgetXmlDataByCptCode(String cptCode) {
		String query = "select * from protocol_form_xml_data where protocol_form_xml_data_type = 'BUDGET' and retired = :retired "
				+ " and xml_data.exist('//procedure[@cptcode = \""+ cptCode +"\"]')=1";
		
		TypedQuery<ProtocolFormXmlData> q = (TypedQuery<ProtocolFormXmlData>) em
				.createNativeQuery(query, ProtocolFormXmlData.class);
		q.setHint("org.hibernate.cacheable", false);
		q.setParameter("retired", Boolean.FALSE);
		//q.setParameter("cptCode", cptCode);
		
		return q.getResultList();
	}
	
	public EntityManager getEm() {
		return em;
	}
	
	@PersistenceContext(unitName = "defaultPersistenceUnit")
	public void setEm(EntityManager em) {
		this.em = em;
	}

}
