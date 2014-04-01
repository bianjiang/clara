package edu.uams.clara.webapp.protocol.dao.etl;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.springframework.transaction.annotation.Transactional;

import edu.uams.clara.webapp.common.domain.history.Track;
import edu.uams.clara.webapp.common.domain.security.acl.SecurableObject;
import edu.uams.clara.webapp.common.domain.security.acl.SecurableObjectAcl;
import edu.uams.clara.webapp.protocol.domain.Protocol;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.ProtocolFormCommitteeStatus;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.ProtocolFormStatus;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.ProtocolStatus;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolForm;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolFormXmlData;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolFormXmlDataDocument;

//@Repository
public class MiagrationDao {

	private EntityManager em;
	
	@Transactional(readOnly = true)
	public Protocol findProtocolById(long protocolId){
		String qry = "SELECT p.* FROM [clara].[dbo].[protocol] as p"
				+ " WHERE  p.retired = :retired"
				+ " AND p.id = :protocolId";
		TypedQuery<Protocol> q = (TypedQuery<Protocol>) em
				.createNativeQuery(qry, Protocol.class);
		q.setHint("org.hibernate.cacheable", false);
		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("protocolId", protocolId);
		//Query query = em.createNativeQuery(qry);
		//query.setParameter("retired", Boolean.FALSE);
		//query.setParameter("protocolId", protocolId);
		
		return q.getSingleResult();
	}
	
	@Transactional(readOnly = true)
	public List<Protocol> findProtocolByIdList(List<Long> protocolIdLst){
		String qry = "SELECT p.* FROM [clara].[dbo].[protocol] as p"
				+ " WHERE  p.retired = :retired"
				+ " AND p.id IN :protocolIdLst";
		TypedQuery<Protocol> q = (TypedQuery<Protocol>) em
				.createNativeQuery(qry, Protocol.class);
		q.setHint("org.hibernate.cacheable", false);
		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("protocolIdLst", protocolIdLst);
		//Query query = em.createNativeQuery(qry);
		//query.setParameter("retired", Boolean.FALSE);
		//query.setParameter("protocolId", protocolId);
		
		return q.getResultList();
	}
	
	@Transactional(readOnly = true)
	public List<ProtocolForm> findProtocolFormByProtocolId(long protocolId){
		String qry = "SELECT pf.* FROM [clara].[dbo].[protocol_form] as pf"
				+ " WHERE  pf.retired = :retired"
				+ " AND pf.protocol_id = :protocolId";
		TypedQuery<ProtocolForm> q = (TypedQuery<ProtocolForm>) em
				.createNativeQuery(qry, ProtocolForm.class);
		q.setHint("org.hibernate.cacheable", false);
		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("protocolId", protocolId);
		//Query query = em.createNativeQuery(qry);
		//query.setParameter("retired", Boolean.FALSE);
		//query.setParameter("protocolId", protocolId);
		
		return q.getResultList();
	}
	
	@Transactional(readOnly = true)
	public List<ProtocolFormXmlData> findProtocolFormXmlDataByProtocolId(long protocolId){
		String qry = "SELECT pfxd.* FROM [clara].[dbo].[protocol_form_xml_data] as pfxd, [clara].[dbo].[protocol_form] as pf"
				+ " WHERE  pfxd.retired = :retired"
				+ " AND pf.protocol_id = :protocolId"
				+ " AND pfxd.protocol_form_id = pf.id";
		TypedQuery<ProtocolFormXmlData> q = (TypedQuery<ProtocolFormXmlData>) em
				.createNativeQuery(qry, ProtocolFormXmlData.class);
		q.setHint("org.hibernate.cacheable", false);
		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("protocolId", protocolId);
		//Query query = em.createNativeQuery(qry);
		//query.setParameter("retired", Boolean.FALSE);
		//query.setParameter("protocolId", protocolId);
		
		return q.getResultList();
	}
	
	@Transactional(readOnly = true)
	public List<ProtocolFormXmlDataDocument> findProtocolFormXmlDataDocumentsByProtocolId(long protocolId){
		String qry = "SELECT pfxdd.* FROM [clara].[dbo].[protocol_form_xml_data_document] as pfxdd, [clara].[dbo].[protocol_form_xml_data] as pfxd, [clara].[dbo].[protocol_form] as pf"
				+ " WHERE  pfxdd.retired = :retired"
				+ " AND pf.protocol_id = :protocolId"
				+ " AND pfxd.protocol_form_id = pf.id"
				+ " AND pfxdd.protocol_form_xml_data_id = pfxd.id";
		TypedQuery<ProtocolFormXmlDataDocument> q = (TypedQuery<ProtocolFormXmlDataDocument>) em
				.createNativeQuery(qry, ProtocolFormXmlDataDocument.class);
		q.setHint("org.hibernate.cacheable", false);
		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("protocolId", protocolId);
		//Query query = em.createNativeQuery(qry);
		//query.setParameter("retired", Boolean.FALSE);
		//query.setParameter("protocolId", protocolId);
		
		return q.getResultList();
	}
	
	@Transactional(readOnly = true)
	public List<ProtocolStatus> findProtocolStatusByProtocolId(long protocolId){
		String qry = "SELECT ps.* FROM [clara].[dbo].[protocol_status] as ps"
				+ " WHERE  ps.retired = :retired"
				+ " AND ps.protocol_id = :protocolId";
		TypedQuery<ProtocolStatus> q = (TypedQuery<ProtocolStatus>) em
				.createNativeQuery(qry, ProtocolStatus.class);
		q.setHint("org.hibernate.cacheable", false);
		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("protocolId", protocolId);
		//Query query = em.createNativeQuery(qry);
		//query.setParameter("retired", Boolean.FALSE);
		//query.setParameter("protocolId", protocolId);
		
		return q.getResultList();
	}
	
	@Transactional(readOnly = true)
	public List<ProtocolFormStatus> findProtocolFormStatusByProtocolId(long protocolId){
		String qry = "SELECT ps.* FROM [clara].[dbo].[protocol_form_status] as ps, [clara].[dbo].[protocol_form] as pf"
				+ " WHERE  ps.retired = :retired"
				+ " AND ps.protocol_form_id = pf.id"
				+ " AND pf.protocol_id = :protocolId";
		TypedQuery<ProtocolFormStatus> q = (TypedQuery<ProtocolFormStatus>) em
				.createNativeQuery(qry, ProtocolFormStatus.class);
		q.setHint("org.hibernate.cacheable", false);
		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("protocolId", protocolId);
		//Query query = em.createNativeQuery(qry);
		//query.setParameter("retired", Boolean.FALSE);
		//query.setParameter("protocolId", protocolId);
		
		return q.getResultList();
	}
	
	@Transactional(readOnly = true)
	public List<ProtocolFormCommitteeStatus> findProtocolFormCommitteeStatusByProtocolId(long protocolId){
		String qry = "SELECT ps.* FROM [clara].[dbo].[protocol_form_committee_status] as ps, [clara].[dbo].[protocol_form] as pf"
				+ " WHERE  ps.retired = :retired"
				+ " AND ps.protocol_form_id = pf.id"
				+ " AND pf.protocol_id = :protocolId";
		TypedQuery<ProtocolFormCommitteeStatus> q = (TypedQuery<ProtocolFormCommitteeStatus>) em
				.createNativeQuery(qry, ProtocolFormCommitteeStatus.class);
		q.setHint("org.hibernate.cacheable", false);
		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("protocolId", protocolId);
		//Query query = em.createNativeQuery(qry);
		//query.setParameter("retired", Boolean.FALSE);
		//query.setParameter("protocolId", protocolId);
		
		return q.getResultList();
	}
	
	@Transactional(readOnly = true)
	public SecurableObject findSecurableObjectByProtocolId(long protocolId){
		String qry = "SELECT so.* FROM [clara].[dbo].[securable_object] as so"
				+ " WHERE  so.retired = :retired"
				+ " AND so.object_id = :protocolId"
				+ " AND so.object_identification_expression is NULL";
		TypedQuery<SecurableObject> q = (TypedQuery<SecurableObject>) em
				.createNativeQuery(qry, SecurableObject.class);
		q.setHint("org.hibernate.cacheable", false);
		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("protocolId", protocolId);
		//Query query = em.createNativeQuery(qry);
		//query.setParameter("retired", Boolean.FALSE);
		//query.setParameter("protocolId", protocolId);
		
		return q.getSingleResult();
	}
	
	@Transactional(readOnly = true)
	public List<SecurableObjectAcl> findSecurableObjectAclByProtocolId(long protocolId){
		String qry = "SELECT soa.* FROM [clara].[dbo].[securable_object] as so, [clara].[dbo].[securable_object_acl] as soa"
				+ " WHERE  soa.retired = :retired"
				+ " AND so.object_id = :protocolId"
				+ " AND soa.securable_object_id = so.id"
				+ " AND so.object_identification_expression is NULL";
		TypedQuery<SecurableObjectAcl> q = (TypedQuery<SecurableObjectAcl>) em
				.createNativeQuery(qry, SecurableObjectAcl.class);
		q.setHint("org.hibernate.cacheable", false);
		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("protocolId", protocolId);
		//Query query = em.createNativeQuery(qry);
		//query.setParameter("retired", Boolean.FALSE);
		//query.setParameter("protocolId", protocolId);
		
		return q.getResultList();
	}
	
	@Transactional(readOnly = true)
	public List<Protocol> findAllStudiesByUserIdAndRoleName(long userId, String roleName){
		String querySt = "";
		if (roleName.isEmpty()){
			querySt = "pfxd.meta_data_xml.exist('/protocol/staffs/staff/user[@id=\""+ userId +"\"]')=1";
		} else if (userId == 0){
			querySt = "pfxd.meta_data_xml.exist('/protocol/staffs/staff/user/roles/role/text()[fn:upper-case(.)=\""+ roleName +"\"]')=1";
		} else {
			querySt = "pfxd.meta_data_xml.exist('/protocol/staffs/staff/user[@id=\""+ userId +"\"]/roles/role/text()[fn:upper-case(.)=\""+ roleName +"\"]')=1";
		}
		
		String qry = "SELECT pfxd.* FROM protocol pfxd"
				+ " WHERE pfxd.retired = :retired"
				+ " AND "+ querySt +"";
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
	public List<Protocol> findAllStudiesByUserPISerialAndRoleName(String piSerial, String roleName){
		String querySt = "";
		if (roleName.isEmpty()){
			querySt = "pfxd.meta_data_xml.exist('/protocol/staffs/staff/user[@pi_serial="+ piSerial +"]')=1";
		} else if (piSerial.isEmpty()){
			querySt = "pfxd.meta_data_xml.exist('/protocol/staffs/staff/user/roles/role/text()[fn:upper-case(.)=\""+ roleName +"\"]')=1";
		} else {
			querySt = "pfxd.meta_data_xml.exist('/protocol/staffs/staff/user[@pi_serial="+ piSerial +"]/roles/role/text()[fn:upper-case(.)=\""+ roleName +"\"]')=1";
		}
		
		String qry = "SELECT pfxd.* FROM protocol pfxd"
				+ " WHERE pfxd.retired = :retired"
				+ " AND "+ querySt +"";
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
	public List<Protocol> findAllStudiesByPISerial(long piSerial){
		String querySt = "pfxd.meta_data_xml.exist('/protocol/staffs/staff/user[@pi_serial=\""+ piSerial +"\"]')=1";
		
		String qry = "SELECT pfxd.* FROM protocol pfxd"
				+ " WHERE pfxd.retired = :retired"
				+ " AND "+ querySt +"";
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
	public List<Protocol> findFormStaffOnProtocol(){
		String querySt = "pfxd.meta_data_xml.exist('/protocol/staffs/staff/user/roles/role/text()[fn:contains(fn:upper-case(.),\"FORMER\")]')=1";
		
		String qry = "SELECT pfxd.* FROM protocol pfxd"
				+ " WHERE pfxd.retired = :retired"
				+ " AND "+ querySt +"";
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
	public List<Protocol> listAllProtocolWithTypeIssue(){
		
		String qry = "SELECT pfxd.* FROM protocol pfxd"
				+ " WHERE pfxd.retired = :retired"
				+ " AND pfxd.meta_data_xml.exist('/protocol[@type]')=1";
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
	public List<Protocol> listAllMigratedProtocolAcknowledged(){
		
		String qry = "SELECT * FROM protocol WHERE retired = 0"
				+ " AND id < 200000 "
				+ " AND id IN ( "
				+ " SELECT DISTINCT protocol_id FROM protocol_form "
				+ " WHERE retired = :retired "
				+ " AND protocol_form_type = 'MODIFICATION' "
				+ " AND id IN (SELECT DISTINCT protocol_form_id FROM protocol_form_status WHERE retired = :retired and protocol_form_status = 'IRB_ACKNOWLEDGED'))";
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
	public List<ProtocolFormXmlData> findAllMod(){
		
		String qry = "SELECT pfxd.* from protocol_form_xml_data pfxd, protocol_form pf, protocol p, protocol_status ps"
				+ " WHERE pfxd.retired = :retired AND pf.retired = :retired AND p.retired = :retired AND ps.retired = :retired"
				+ " AND pfxd.protocol_form_id = pf.id"
				+ " AND pf.protocol_id = p.id"
				+ " AND pfxd.protocol_form_xml_data_type = 'MODIFICATION'"
				+ " AND p.id = ps.protocol_id"
				+ " AND ps.protocol_status = 'OPEN'";
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
	public List<Track> findAllTypoTrack(){
		
		String qry = "SELECT t.* from track t"
				+ " WHERE t.retired = :retired "
				+ " AND t.xml_data.exist('/logs/log/text()[fn:contains(.,\"improted\")]')=1";
		TypedQuery<Track> q = (TypedQuery<Track>) em
				.createNativeQuery(qry, Track.class);
		q.setHint("org.hibernate.cacheable", false);
		q.setParameter("retired", Boolean.FALSE);
		//Query query = em.createNativeQuery(qry);
		//query.setParameter("retired", Boolean.FALSE);
		//query.setParameter("protocolId", protocolId);
		
		return q.getResultList();
	}
	
	@Transactional(readOnly = true)
	public List<ProtocolFormXmlData> listAllMissingMigratedField(){
		String qry = "SELECT * FROM protocol_form_xml_data WHERE protocol_form_id IN ("
				+ " SELECT id FROM protocol_form where retired = :retired and protocol_id in (select id from protocol where retired = :retired and id < 200000))"
				+ " and retired = :retired and xml_data.exist('/protocol/migrated')=0"
				+ " and protocol_form_xml_data_type = 'MODIFICATION'";
		
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
	public List<ProtocolForm> listCancerStudyProotcolForms(){
		String qry = "SELECT * FROM protocol_form WHERE retired =:retired AND protocol_form_type = 'NEW_SUBMISSION' AND meta_data_xml.exist('/protocol/extra/prmc-related-or-not[text()=\"y\"]')=1";
		
		TypedQuery<ProtocolForm> q = (TypedQuery<ProtocolForm>) em
				.createNativeQuery(qry, ProtocolForm.class);
		q.setHint("org.hibernate.cacheable", false);
		q.setParameter("retired", Boolean.FALSE);
		//Query query = em.createNativeQuery(qry);
		//query.setParameter("retired", Boolean.FALSE);
		//query.setParameter("protocolId", protocolId);
		
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
