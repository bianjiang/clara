package edu.uams.clara.webapp.protocol.dao.protocolform;

import java.util.Date;
import java.util.List;

import javax.persistence.TypedQuery;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import edu.uams.clara.core.dao.AbstractDomainDao;
import edu.uams.clara.webapp.common.domain.usercontext.enums.Committee;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolFormXmlDataDocument;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolFormXmlDataDocument.Status;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolFormXmlDataDocumentWrapper;

@Repository
public class ProtocolFormXmlDataDocumentDao extends
		AbstractDomainDao<ProtocolFormXmlDataDocument> {

	private static final long serialVersionUID = 3841006897986675188L;
	
	@Transactional(readOnly = true)
	public long countDocumentRevisionsByParentId(
			long parentId) {

		/*String nativeQuery = "SELECT COUNT(*) FROM protocol_form_xml_data_document "
				+ " WHERE protocol_form_xml_data_document.parent_id = :parentId AND protocol_form_xml_data_document.retired = :retired "
				+ " ORDER BY protocol_form_xml_data_document.id DESC";*/
		
		String query = "SELECT pfdm.versionId FROM ProtocolFormXmlDataDocument pfdm "
				+ " WHERE pfdm.parent.id = :parentId AND pfdm.retired = :retired "
				+ " ORDER BY pfdm.id DESC";

		/*Query q = getEntityManager()
				.createNativeQuery(nativeQuery, Integer.class);*/
		TypedQuery<Long> q = getEntityManager().createQuery(query, Long.class);
		
		q.setFirstResult(0);
		q.setMaxResults(1);
		q.setHint("org.hibernate.cacheable", true);
		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("parentId", parentId);

		return q.getSingleResult();
	}

	/**
	 * List all revisions for the specified FormXmlDataDocument,
	 */
	@Transactional(readOnly = true)
	public List<ProtocolFormXmlDataDocumentWrapper> listProtocolFormXmlDataDocumentRevisionsByParentId(
			long parentId) {

		String nativeQuery = "SELECT protocol_form_xml_data_document.*, protocol_form_xml_data.protocol_form_id, protocol_form.protocol_form_type, protocol_form.parent_id AS parent_protocol_form_id FROM protocol_form_xml_data_document "+
				" INNER JOIN protocol_form_xml_data ON protocol_form_xml_data.id = protocol_form_xml_data_document.protocol_form_xml_data_id" +
				" INNER JOIN protocol_form ON protocol_form.id = protocol_form_xml_data.protocol_form_id" +
				" WHERE protocol_form_xml_data_document.parent_id = :parentId AND protocol_form_xml_data_document.retired = :retired ORDER BY protocol_form_xml_data_document.id DESC ";

		TypedQuery<ProtocolFormXmlDataDocumentWrapper> q = (TypedQuery<ProtocolFormXmlDataDocumentWrapper>) getEntityManager()
				.createNativeQuery(nativeQuery, ProtocolFormXmlDataDocumentWrapper.class);
		q.setHint("org.hibernate.cacheable", false);
		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("parentId", parentId);

		return q.getResultList();
	}

	/*
	 * protocol is not VERSION-ED, but ProtocolForm and ProtocolFormXmlData are
	 * both version-ed. So, this function list all protocol form documents link
	 * to this protocolForm and all documents link to this protocolForm's
	 * previous versions
	 */
	@Transactional(readOnly = true)
	public List<ProtocolFormXmlDataDocumentWrapper> listDocumentsByProtocolFormId(
			long protocolFormId) {

		/*String query = "SELECT protocol_form_xml_data_document.* FROM protocol_form_xml_data_document "
				+ " WHERE protocol_form_xml_data_document.id IN ("
				+ " SELECT MAX(pfdd.id) FROM protocol_form_xml_data_document pfdd, protocol_form pf "
				+ " WHERE pfdd.retired = :retired"
				+ " AND pf.id = :protocolFormId AND "
				+ " pfdd.protocol_form_xml_data_id IN (SELECT pfxd.id FROM protocol_form_xml_data pfxd, protocol_form pfm WHERE pfxd.protocol_form_id = pfm.id AND pfm.parent_id = pf.parent_id "
				+ " AND pfm.created <= pf.created) "
				+ " GROUP BY pfdd.parent_id )";*/
		
		String nativeQuery = "SELECT protocol_form_xml_data_document.*, protocol_form.id AS protocol_form_id, protocol_form.protocol_form_type AS protocol_form_type, protocol_form.parent_id AS parent_protocol_form_id FROM protocol_form_xml_data_document "
						+ " INNER JOIN protocol_form_xml_data ON protocol_form_xml_data.id = protocol_form_xml_data_document.protocol_form_xml_data_id "
						+ " INNER JOIN protocol_form ON protocol_form.id = protocol_form_xml_data.protocol_form_id "
						+ " WHERE protocol_form_xml_data_document.retired = :retired AND protocol_form_xml_data_document.id IN ("
						+ " SELECT MAX(pfdd.id) FROM protocol_form_xml_data_document pfdd, protocol_form pf"
						+ " WHERE pfdd.retired = :retired "
						+ " AND pf.retired = :retired AND pf.id = :protocolFormId AND pf.parent_id = ("
						+ " SELECT p.parent_id FROM protocol_form_xml_data pfxd, protocol_form p "
						+ " WHERE pfxd.id = pfdd.protocol_form_xml_data_id AND pfxd.protocol_form_id = p.id "
						+ " AND p.created <= pf.created) "
						+ " GROUP BY pfdd.parent_id)";

		TypedQuery<ProtocolFormXmlDataDocumentWrapper> q = (TypedQuery<ProtocolFormXmlDataDocumentWrapper>) getEntityManager()
				.createNativeQuery(nativeQuery, ProtocolFormXmlDataDocumentWrapper.class);
		q.setHint("org.hibernate.cacheable", false);
		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("protocolFormId", protocolFormId);

		return q.getResultList();
	}
	
	@Transactional(readOnly = true)
	public List<ProtocolFormXmlDataDocumentWrapper> listDocumentsByProtocolFormIdAndStatus(
			long protocolFormId, Status documentStatus) {

		/*String query = "SELECT protocol_form_xml_data_document.* FROM protocol_form_xml_data_document "
				+ " WHERE protocol_form_xml_data_document.id IN ("
				+ " SELECT MAX(pfdd.id) FROM protocol_form_xml_data_document pfdd, protocol_form pf "
				+ " WHERE pfdd.retired = :retired"
				+ " AND pf.id = :protocolFormId AND "
				+ " pfdd.protocol_form_xml_data_id IN (SELECT pfxd.id FROM protocol_form_xml_data pfxd, protocol_form pfm WHERE pfxd.protocol_form_id = pfm.id AND pfm.parent_id = pf.parent_id "
				+ " AND pfm.created <= pf.created) "
				+ " GROUP BY pfdd.parent_id )";*/
		
		String nativeQuery = "SELECT protocol_form_xml_data_document.*, protocol_form.id AS protocol_form_id, protocol_form.protocol_form_type AS protocol_form_type, protocol_form.parent_id AS parent_protocol_form_id FROM protocol_form_xml_data_document "
						+ " INNER JOIN protocol_form_xml_data ON protocol_form_xml_data.id = protocol_form_xml_data_document.protocol_form_xml_data_id "
						+ " INNER JOIN protocol_form ON protocol_form.id = protocol_form_xml_data.protocol_form_id "
						+ " WHERE protocol_form_xml_data_document.retired = :retired AND protocol_form_xml_data_document.id IN ("
						+ " SELECT MAX(pfdd.id) FROM protocol_form_xml_data_document pfdd, protocol_form pf"
						+ " WHERE pfdd.retired = :retired "
						+ " AND pfdd.status = :documentStatus "
						+ " AND pf.retired = :retired AND pf.id = :protocolFormId AND pf.parent_id = ("
						+ " SELECT p.parent_id FROM protocol_form_xml_data pfxd, protocol_form p "
						+ " WHERE pfxd.id = pfdd.protocol_form_xml_data_id AND pfxd.protocol_form_id = p.id "
						+ " AND p.created <= pf.created) "
						+ " GROUP BY pfdd.parent_id)";

		TypedQuery<ProtocolFormXmlDataDocumentWrapper> q = (TypedQuery<ProtocolFormXmlDataDocumentWrapper>) getEntityManager()
				.createNativeQuery(nativeQuery, ProtocolFormXmlDataDocumentWrapper.class);
		q.setHint("org.hibernate.cacheable", false);
		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("protocolFormId", protocolFormId);
		q.setParameter("documentStatus", documentStatus.toString());
		
		return q.getResultList();
	}
	
	@Transactional(readOnly = true)
	public List<ProtocolFormXmlDataDocumentWrapper> listDocumentsByProtocolFormIdAndCategory(
			long protocolFormId, String category) {

		/*String query = "SELECT pfd FROM ProtocolFormXmlDataDocument pfd "
			+ " WHERE pfd.id IN ("
			+ " SELECT MAX(pfdd.id) FROM ProtocolFormXmlDataDocument pfdd, ProtocolForm pf "
			+ " WHERE pfdd.retired = :retired"
			+ " AND pf.id = :protocolFormId AND pfdd.category = :category AND ("
			+ " pfdd.protocolFormXmlData.protocolForm.parent.id = pf.parent.id "
			+ " AND pfdd.protocolFormXmlData.protocolForm.created <= pf.created) "
			+ " GROUP BY pfdd.parent.id )";*/
		
		String nativeQuery = "SELECT protocol_form_xml_data_document.*, protocol_form.id AS protocol_form_id, protocol_form.protocol_form_type AS protocol_form_type, protocol_form.parent_id AS parent_protocol_form_id FROM protocol_form_xml_data_document "
				+ " INNER JOIN protocol_form_xml_data ON protocol_form_xml_data.id = protocol_form_xml_data_document.protocol_form_xml_data_id "
				+ " INNER JOIN protocol_form ON protocol_form.id = protocol_form_xml_data.protocol_form_id "
				+ " WHERE protocol_form_xml_data_document.retired = :retired AND protocol_form_xml_data_document.id IN ("
				+ " SELECT MAX(pfdd.id) FROM protocol_form_xml_data_document pfdd, protocol_form pf"
				+ " WHERE pfdd.retired = :retired "
				+ " AND pf.retired = :retired AND pf.id = :protocolFormId AND pf.parent_id = ("
				+ " SELECT p.parent_id FROM protocol_form_xml_data pfxd, protocol_form p "
				+ " WHERE pfxd.id = pfdd.protocol_form_xml_data_id AND pfxd.protocol_form_id = p.id "
				+ " AND p.created <= pf.created) AND pfdd.category = :category "
				+ " GROUP BY pfdd.parent_id)";

		TypedQuery<ProtocolFormXmlDataDocumentWrapper> q = (TypedQuery<ProtocolFormXmlDataDocumentWrapper>) getEntityManager()
				.createNativeQuery(nativeQuery, ProtocolFormXmlDataDocumentWrapper.class);
		q.setHint("org.hibernate.cacheable", false);
		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("protocolFormId", protocolFormId);
		q.setParameter("category", category);

		return q.getResultList();
	}
	
	@Transactional(readOnly = true)
	public ProtocolFormXmlDataDocument getLatestDocumentByProtocolFormIdAndCategory(
			long protocolFormId, String category) {

		String query = "SELECT pfd FROM ProtocolFormXmlDataDocument pfd "
			+ " WHERE pfd.id IN ("
			+ " SELECT MAX(pfdd.id) FROM ProtocolFormXmlDataDocument pfdd, ProtocolForm pf "
			+ " WHERE pfdd.retired = :retired"
			+ " AND pf.id = :protocolFormId AND pfdd.category = :category AND ("
			+ " pfdd.protocolFormXmlData.protocolForm.parent.id = pf.parent.id "
			+ " AND pfdd.protocolFormXmlData.protocolForm.created <= pf.created) "
			+ " GROUP BY pfdd.parent.id ) ORDER BY pfd.id DESC";

		TypedQuery<ProtocolFormXmlDataDocument> q =  getEntityManager()
				.createQuery(query, ProtocolFormXmlDataDocument.class);
		q.setFirstResult(0);
		q.setMaxResults(1);
		q.setHint("org.hibernate.cacheable", false);
		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("protocolFormId", protocolFormId);
		q.setParameter("category", category);

		return q.getSingleResult();
	}
	
	@Transactional(readOnly = true)
	public List<ProtocolFormXmlDataDocument> getLatestDocumentByProtocolFormId(
			long protocolFormId) {
		
		/*
		String query = "SELECT pfd FROM ProtocolFormXmlDataDocument pfd "
			+ " WHERE pfd.id IN ("
			+ " SELECT MAX(pfdd.id) FROM ProtocolFormXmlDataDocument pfdd, ProtocolForm pf "
			+ " WHERE pfdd.retired = :retired"
			+ " AND pf.id = :protocolFormId AND ("
			+ " pfdd.protocolFormXmlData.protocolForm.parent.id = pf.parent.id "
			+ " AND pfdd.protocolFormXmlData.protocolForm.created <= pf.created) "
			+ " GROUP BY pfdd.parent.id ) ORDER BY pfd.id DESC";
		*/
		
		String nativeQuery = "SELECT pfd.* FROM protocol_form_xml_data_document pfd"
						+ " WHERE pfd.id IN ("
						+ " SELECT MAX(pfdd.id) FROM protocol_form_xml_data_document pfdd, protocol_form pf"
						+ " WHERE pfdd.retired = 0"
						+ " AND pf.id = :protocolFormId AND pfdd.protocol_form_xml_data_id in (SELECT pfdx.id FROM protocol_form_xml_data pfdx, protocol_form pff"
						+ " WHERE pfdx.protocol_form_id = pff.id"
						+ " AND pfdx.retired = :retired AND pff.retired = :retired"
						+ " AND pff.parent_id = pf.parent_id"
						+ " AND pff.created <= pf.created)"
						+ " GROUP BY pfdd.parent_id ) ORDER BY pfd.id DESC";

		TypedQuery<ProtocolFormXmlDataDocument> q = (TypedQuery<ProtocolFormXmlDataDocument>) getEntityManager()
				.createNativeQuery(nativeQuery, ProtocolFormXmlDataDocument.class);

		q.setHint("org.hibernate.cacheable", false);
		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("protocolFormId", protocolFormId);

		return q.getResultList();
	}
	
	@Transactional(readOnly = true)
	public List<ProtocolFormXmlDataDocument> getLatestDocumentExcludeCertainTypesByProtocolFormId(
			long protocolFormId, List<String> excludedDocTypes) {
		
		/*
		String query = "SELECT pfd FROM ProtocolFormXmlDataDocument pfd "
			+ " WHERE pfd.id IN ("
			+ " SELECT MAX(pfdd.id) FROM ProtocolFormXmlDataDocument pfdd, ProtocolForm pf "
			+ " WHERE pfdd.retired = :retired"
			+ " AND pf.id = :protocolFormId AND ("
			+ " pfdd.protocolFormXmlData.protocolForm.parent.id = pf.parent.id "
			+ " AND pfdd.protocolFormXmlData.protocolForm.created <= pf.created) "
			+ " GROUP BY pfdd.parent.id ) ORDER BY pfd.id DESC";
		*/
		
		String nativeQuery = "SELECT pfd.* FROM protocol_form_xml_data_document pfd"
						+ " WHERE pfd.id IN ("
						+ " SELECT MAX(pfdd.id) FROM protocol_form_xml_data_document pfdd, protocol_form pf"
						+ " WHERE pfdd.retired = 0"
						+ " AND pf.id = :protocolFormId AND pfdd.protocol_form_xml_data_id in (SELECT pfdx.id FROM protocol_form_xml_data pfdx, protocol_form pff"
						+ " WHERE pfdx.protocol_form_id = pff.id"
						+ " AND pfdx.retired = :retired AND pff.retired = :retired"
						+ " AND pff.parent_id = pf.parent_id"
						+ " AND pff.created <= pf.created)"
						+ " AND pfd.category NOT IN :excludedDocTypes"
						+ " GROUP BY pfdd.parent_id ) ORDER BY pfd.id DESC";

		TypedQuery<ProtocolFormXmlDataDocument> q = (TypedQuery<ProtocolFormXmlDataDocument>) getEntityManager()
				.createNativeQuery(nativeQuery, ProtocolFormXmlDataDocument.class);

		q.setHint("org.hibernate.cacheable", false);
		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("protocolFormId", protocolFormId);
		q.setParameter("excludedDocTypes", excludedDocTypes);

		return q.getResultList();
	}
	
	@Transactional(readOnly = true)
	public List<ProtocolFormXmlDataDocument> getLatestDocumentsByProtocolFormIdAndCategory(
			long protocolFormId, String category) {
		
		/*
		String query = "SELECT pfd FROM ProtocolFormXmlDataDocument pfd "
			+ " WHERE pfd.id IN ("
			+ " SELECT MAX(pfdd.id) FROM ProtocolFormXmlDataDocument pfdd, ProtocolForm pf "
			+ " WHERE pfdd.retired = :retired"
			+ " AND pf.id = :protocolFormId AND ("
			+ " pfdd.protocolFormXmlData.protocolForm.parent.id = pf.parent.id "
			+ " AND pfdd.protocolFormXmlData.protocolForm.created <= pf.created) "
			+ " GROUP BY pfdd.parent.id ) ORDER BY pfd.id DESC";
		*/
		
		String nativeQuery = "SELECT pfd.* FROM protocol_form_xml_data_document pfd"
						+ " WHERE pfd.id IN ("
						+ " SELECT MAX(pfdd.id) FROM protocol_form_xml_data_document pfdd, protocol_form pf"
						+ " WHERE pfdd.retired = 0"
						+ " AND pf.id = :protocolFormId AND pfdd.protocol_form_xml_data_id in (SELECT pfdx.id FROM protocol_form_xml_data pfdx, protocol_form pff"
						+ " WHERE pfdx.protocol_form_id = pff.id"
						+ " AND pfdx.retired = :retired AND pff.retired = :retired"
						+ " AND pff.parent_id = pf.parent_id"
						+ " AND pff.created <= pf.created)"
						+ " GROUP BY pfdd.parent_id ) AND pfd.category = :category ORDER BY pfd.id DESC";

		TypedQuery<ProtocolFormXmlDataDocument> q = (TypedQuery<ProtocolFormXmlDataDocument>) getEntityManager()
				.createNativeQuery(nativeQuery, ProtocolFormXmlDataDocument.class);

		q.setHint("org.hibernate.cacheable", false);
		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("protocolFormId", protocolFormId);
		q.setParameter("category", category);

		return q.getResultList();
	}
	
	@Transactional(readOnly = true)
	public List<ProtocolFormXmlDataDocumentWrapper> listDocumentsByProtocolFormIdAndCommittee(
			long protocolFormId, Committee committee) {

		/*String query = "SELECT pfd FROM ProtocolFormXmlDataDocument pfd "
			+ " WHERE pfd.id IN ("
			+ " SELECT MAX(pfdd.id) FROM ProtocolFormXmlDataDocument pfdd, ProtocolForm pf "
			+ " WHERE pfdd.retired = :retired"
			+ " AND pf.id = :protocolFormId AND pfdd.committee = :committee AND ("
			+ " pfdd.protocolFormXmlData.protocolForm.parent.id = pf.parent.id "
			+ " AND pfdd.protocolFormXmlData.protocolForm.created <= pf.created) "
			+ " GROUP BY pfdd.parent.id )";*/
		
		String nativeQuery = "SELECT protocol_form_xml_data_document.*, protocol_form.id AS protocol_form_id, protocol_form.protocol_form_type AS protocol_form_type, protocol_form.parent_id AS parent_protocol_form_id FROM protocol_form_xml_data_document "
				+ " INNER JOIN protocol_form_xml_data ON protocol_form_xml_data.id = protocol_form_xml_data_document.protocol_form_xml_data_id "
				+ " INNER JOIN protocol_form ON protocol_form.id = protocol_form_xml_data.protocol_form_id "
				+ " WHERE protocol_form_xml_data_document.retired = :retired AND protocol_form_xml_data_document.id IN ("
				+ " SELECT MAX(pfdd.id) FROM protocol_form_xml_data_document pfdd, protocol_form pf"
				+ " WHERE pfdd.retired = :retired "
				+ " AND pf.retired = :retired AND pf.id = :protocolFormId AND pf.parent_id = ("
				+ " SELECT p.parent_id FROM protocol_form_xml_data pfxd, protocol_form p "
				+ " WHERE pfxd.id = pfdd.protocol_form_xml_data_id AND pfxd.protocol_form_id = p.id "
				+ " AND p.created <= pf.created) AND pfdd.committee = :committee "
				+ " GROUP BY pfdd.parent_id)";

		TypedQuery<ProtocolFormXmlDataDocumentWrapper> q = (TypedQuery<ProtocolFormXmlDataDocumentWrapper>) getEntityManager()
				.createNativeQuery(nativeQuery, ProtocolFormXmlDataDocumentWrapper.class);
		q.setHint("org.hibernate.cacheable", false);
		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("protocolFormId", protocolFormId);
		q.setParameter("committee", committee);

		return q.getResultList();
	}
	
	@Transactional(readOnly = true)
	public List<ProtocolFormXmlDataDocumentWrapper> listDocumentsByProtocolFormIdAndLaterThanDate(
			long protocolFormId, Date created) {

		/*String query = "SELECT pfd FROM ProtocolFormXmlDataDocument pfd "
			+ " WHERE pfd.created >= :created AND pfd.id IN ("
			+ " SELECT MAX(pfdd.id) FROM ProtocolFormXmlDataDocument pfdd, ProtocolForm pf "
			+ " WHERE pfdd.retired = :retired"
			+ " AND pf.id = :protocolFormId AND ("
			+ " pfdd.protocolFormXmlData.protocolForm.parent.id = pf.parent.id "
			+ " AND pfdd.protocolFormXmlData.protocolForm.created <= pf.created) "
			+ " GROUP BY pfdd.parent.id )";*/
		
		String nativeQuery = "SELECT protocol_form_xml_data_document.*, protocol_form.id AS protocol_form_id, protocol_form.protocol_form_type AS protocol_form_type, protocol_form.parent_id AS parent_protocol_form_id FROM protocol_form_xml_data_document "
				+ " INNER JOIN protocol_form_xml_data ON protocol_form_xml_data.id = protocol_form_xml_data_document.protocol_form_xml_data_id "
				+ " INNER JOIN protocol_form ON protocol_form.id = protocol_form_xml_data.protocol_form_id "
				+ " WHERE protocol_form_xml_data_document.retired = :retired "
				+ " AND protocol_form_xml_data_document.created >= :created "
				+ " AND protocol_form_xml_data_document.id IN ("
				+ " SELECT MAX(pfdd.id) FROM protocol_form_xml_data_document pfdd, protocol_form pf"
				+ " WHERE pfdd.retired = :retired "
				+ " AND pf.retired = :retired AND pf.id = :protocolFormId AND pf.parent_id = ("
				+ " SELECT p.parent_id FROM protocol_form_xml_data pfxd, protocol_form p "
				+ " WHERE pfxd.id = pfdd.protocol_form_xml_data_id AND pfxd.protocol_form_id = p.id "
				+ " AND p.created <= pf.created) AND pfdd.committee = :committee "
				+ " GROUP BY pfdd.parent_id)";

		TypedQuery<ProtocolFormXmlDataDocumentWrapper> q = (TypedQuery<ProtocolFormXmlDataDocumentWrapper>) getEntityManager()
				.createNativeQuery(nativeQuery, ProtocolFormXmlDataDocumentWrapper.class);
		q.setHint("org.hibernate.cacheable", false);
		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("protocolFormId", protocolFormId);
		q.setParameter("created", created);

		return q.getResultList();
	}
	
	public List<String> listProtocolFormXmlDataDocumentCategories(long protocolFormId) {

		String query = "SELECT pfd.category FROM ProtocolFormXmlDataDocument pfd "
				+ " WHERE pfd.id IN ("
				+ " SELECT MAX(pfdd.id) FROM ProtocolFormXmlDataDocument pfdd, ProtocolForm pf "
				+ " WHERE pfdd.retired = :retired"
				+ " AND pf.id = :protocolFormId "
				+ " AND pfdd.protocolFormXmlData.protocolForm.parent = pf.parent "
				+ " GROUP BY pfdd.parent.id )";

		TypedQuery<String> q = getEntityManager().createQuery(query,
				String.class);
		q.setHint("org.hibernate.cacheable", true);
		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("protocolFormId", protocolFormId);

		return q.getResultList();
	}
	
	

	/**
	 * this functions differs from the previous function is that it list
	 * documents link to the right protocolForm and related protocolForms let's
	 * say we have docId 1, 2 links to protocolFormId 1 (PF 1); docId 3 links to
	 * protocolFormId 2; and docId 4, 5 links to protcolFormId 3, where PF 2 is
	 * a revision of PF 1, and PF 3 is a revision of PF 2, so that PF3.created >
	 * PF2.created > PF1.created if we run
	 * listAllDocumentsByProtocolFormId(PF2), it will return
	 * docIDs {1,2,3,4,5} while in previous function
	 * listDocumentsByProtocolFormId(PF2) should return {1, 2, 3}
	 * 
	 * and remember, listDocumentsByProtocolFormId(PF3) == listAllDocumentsByProtocolFormId(PF3) 
	 * == listAllDocumentsByProtocolFormId(PF2) == listAllDocumentsByProtocolFormId(PF1)
	 * 
	 * @param protocolForm
	 * @return
	 */
	/*
	@Transactional(readOnly = true)
	public List<ProtocolFormXmlDataDocument> listAllDocumentsByProtocolFormId(
			long protocolFormId) {

		String query = "SELECT pfd FROM ProtocolFormXmlDataDocument pfd "
				+ " WHERE pfd.id IN ("
				+ " SELECT MAX(pfdd.id) FROM ProtocolFormXmlDataDocument pfdd, ProtocolForm pf "
				+ " WHERE pfdd.retired = :retired"
				+ " AND pf.id = :protocolFormId "
				+ " AND pfdd.protocolFormXmlData.protocolForm.parent = pf.parent "
				+ " GROUP BY pfdd.parent.id )";

		TypedQuery<ProtocolFormXmlDataDocument> q = getEntityManager()
				.createQuery(query, ProtocolFormXmlDataDocument.class);
		q.setHint("org.hibernate.cacheable", true);
		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("protocolFormId", protocolFormId);

		return q.getResultList();
	}
	*/
	

}
