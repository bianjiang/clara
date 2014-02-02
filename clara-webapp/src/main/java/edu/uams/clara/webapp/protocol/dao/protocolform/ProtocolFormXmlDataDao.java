package edu.uams.clara.webapp.protocol.dao.protocolform;

import java.util.List;

import javax.persistence.TypedQuery;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import edu.uams.clara.core.dao.AbstractDomainDao;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolFormXmlData;
import edu.uams.clara.webapp.protocol.domain.protocolform.enums.ProtocolFormXmlDataType;

@Repository
public class ProtocolFormXmlDataDao extends
		AbstractDomainDao<ProtocolFormXmlData> {

	private static final long serialVersionUID = 4333242801399552997L;

	/**
	 * get the last version of the protocolFormXmlData by its type and which
	 * protocol form it links to
	 * 
	 * @param protocolFormId
	 * @param protocolFormXmlDataType
	 * @return
	 */
	@Transactional(readOnly = true)
	public ProtocolFormXmlData getLastProtocolFormXmlDataByProtocolFormIdAndType(
			long protocolFormId, ProtocolFormXmlDataType protocolFormXmlDataType) {
		String query = "SELECT pfxd FROM ProtocolFormXmlData pfxd "
				+ " WHERE pfxd.protocolForm.id = :protocolFormId AND pfxd.retired = :retired AND pfxd.protocolForm.retired = :retired AND pfxd.protocolForm.protocol.retired = :retired AND pfxd.protocolFormXmlDataType = :protocolFormXmlDataType"
				+ " ORDER BY pfxd.created DESC ";

		TypedQuery<ProtocolFormXmlData> q = getEntityManager().createQuery(
				query, ProtocolFormXmlData.class);

		q.setHint("org.hibernate.cacheable", true);

		q.setFirstResult(0);
		q.setMaxResults(1);
		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("protocolFormXmlDataType", protocolFormXmlDataType);
		q.setParameter("protocolFormId", protocolFormId);

		return q.getSingleResult();
		
	}

	@Transactional(readOnly = true)
	public ProtocolFormXmlData getLastProtocolFormXmlDataByProtocolFormXmlDataIdAndType(
			long protocolFormXmlDataId,
			ProtocolFormXmlDataType protocolFormXmlDataType) {
		String query = "SELECT pfxd FROM ProtocolFormXmlData pfxd "
				+ " WHERE pfxd.id = :protocolFormXmlDataId AND pfxd.retired = :retired AND pfxd.protocolForm.retired = :retired AND pfxd.protocolForm.protocol.retired = :retired AND pfxd.protocolFormXmlDataType = :protocolFormXmlDataType"
				+ " ORDER BY pfxd.created DESC ";

		TypedQuery<ProtocolFormXmlData> q = getEntityManager().createQuery(
				query, ProtocolFormXmlData.class);

		q.setHint("org.hibernate.cacheable", true);

		q.setFirstResult(0);
		q.setMaxResults(1);
		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("protocolFormXmlDataType", protocolFormXmlDataType);
		q.setParameter("protocolFormXmlDataId", protocolFormXmlDataId);

		return q.getSingleResult();
	}

	@Transactional(readOnly = true)
	public List<ProtocolFormXmlData> listProtocolformXmlDatasByFormIdAndType(
			long protocolFormId, ProtocolFormXmlDataType protocolFormXmlDataType) {

		String query = "SELECT pfxd FROM ProtocolFormXmlData pfxd "
				+ " WHERE pfxd.retired = :retired AND pfxd.protocolFormXmlDataType = :protocolFormXmlDataType AND pfxd.parent.id IN ("
				+ " SELECT pd.parent.id FROM ProtocolFormXmlData pd WHERE pd.protocolForm.id = :protocolFormId AND pd.retired = :retired) "
				+ " ORDER BY pfxd.created DESC";

		TypedQuery<ProtocolFormXmlData> q = getEntityManager().createQuery(
				query, ProtocolFormXmlData.class);

		q.setHint("org.hibernate.cacheable", true);

		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("protocolFormXmlDataType", protocolFormXmlDataType);
		q.setParameter("protocolFormId", protocolFormId);
		return q.getResultList();

	}
	
	@Transactional(readOnly = true)
	public List<ProtocolFormXmlData> listProtocolformXmlDatasByType(
			ProtocolFormXmlDataType protocolFormXmlDataType) {

		String query = "SELECT pfxd FROM ProtocolFormXmlData pfxd "
				+ " WHERE pfxd.retired = :retired AND pfxd.protocolFormXmlDataType = :protocolFormXmlDataType "
				+ " ORDER BY pfxd.created DESC";

		TypedQuery<ProtocolFormXmlData> q = getEntityManager().createQuery(
				query, ProtocolFormXmlData.class);

		q.setHint("org.hibernate.cacheable", true);

		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("protocolFormXmlDataType", protocolFormXmlDataType);
		return q.getResultList();

	}
	
	@Transactional(readOnly = true)
	public List<ProtocolFormXmlData> listProtocolformXmlDatasByFormId(
			long protocolFormId ) {

		String query = "SELECT pfxd FROM ProtocolFormXmlData pfxd "
				+ " WHERE pfxd.retired = :retired AND pfxd.parent.id IN ("
				+ " SELECT pd.parent.id FROM ProtocolFormXmlData pd WHERE pd.protocolForm.id = :protocolFormId AND pd.retired = :retired) "
				+ " ORDER BY pfxd.created DESC";

		TypedQuery<ProtocolFormXmlData> q = getEntityManager().createQuery(
				query, ProtocolFormXmlData.class);

		q.setHint("org.hibernate.cacheable", true);

		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("protocolFormId", protocolFormId);
		return q.getResultList();

	}
	
	@Transactional(readOnly = true)
	public ProtocolFormXmlData getLastProtocolFormXmlDataByProtocolIdAndType(
			long protocodId, ProtocolFormXmlDataType protocolFormXmlDataType) {
		String query = "SELECT pfxd FROM ProtocolFormXmlData pfxd "
				+ " WHERE pfxd.protocolForm.protocol.id = :protocolId AND pfxd.retired = :retired AND pfxd.protocolForm.retired = :retired AND pfxd.protocolForm.protocol.retired = :retired AND pfxd.protocolFormXmlDataType = :protocolFormXmlDataType"
				+ " ORDER BY pfxd.created DESC ";

		TypedQuery<ProtocolFormXmlData> q = getEntityManager().createQuery(
				query, ProtocolFormXmlData.class);

		q.setHint("org.hibernate.cacheable", true);

		q.setFirstResult(0);
		q.setMaxResults(1);
		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("protocolFormXmlDataType", protocolFormXmlDataType);
		q.setParameter("protocolId", protocodId);

		return q.getSingleResult();
	}
	
	@Transactional(readOnly = true)
	public List<ProtocolFormXmlData> getProtocolFormXmlDatasByType(
			ProtocolFormXmlDataType protocolFormXmlDataType) {
		String query = "SELECT pfxd FROM ProtocolFormXmlData pfxd "
				+ " WHERE pfxd.retired = :retired AND pfxd.protocolForm.retired = :retired AND pfxd.protocolForm.protocol.retired = :retired AND pfxd.protocolFormXmlDataType = :protocolFormXmlDataType"
				+ " ORDER BY pfxd.created DESC ";

		TypedQuery<ProtocolFormXmlData> q = getEntityManager().createQuery(
				query, ProtocolFormXmlData.class);

		q.setHint("org.hibernate.cacheable", true);

		//q.setFirstResult(0);
		//q.setMaxResults(1);
		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("protocolFormXmlDataType", protocolFormXmlDataType);
		//q.setParameter("protocolId", protocodId);

		return q.getResultList();
	}
	
	@Transactional(readOnly = true)
	public List<ProtocolFormXmlData> listProtocolFormXmlDatasHaveNormalProcedure(){
		String qry = "SELECT pfxd.* FROM protocol_form_xml_data as pfxd"
				+ " WHERE  pfxd.retired = :retired"
				+ " AND pfxd.protocol_form_xml_data_type = 'BUDGET'"
				+ " AND pfxd.xml_data.exist('/budget/epochs/epoch/procedures/procedure[@type=\"normal\"]')=1";
		TypedQuery<ProtocolFormXmlData> q = (TypedQuery<ProtocolFormXmlData>) 
				getEntityManager().createNativeQuery(qry, ProtocolFormXmlData.class);
		q.setHint("org.hibernate.cacheable", false);
		q.setParameter("retired", Boolean.FALSE);
		//Query query = em.createNativeQuery(qry);
		//query.setParameter("retired", Boolean.FALSE);
		//query.setParameter("protocolId", protocolId);
		
		return q.getResultList();
	}
}
