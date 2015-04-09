package edu.uams.clara.webapp.contract.dao.contractform;

import java.util.Date;
import java.util.List;

import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import edu.uams.clara.core.dao.AbstractDomainDao;
import edu.uams.clara.webapp.common.domain.usercontext.enums.Committee;
import edu.uams.clara.webapp.contract.domain.contractform.ContractFormXmlDataDocument;
import edu.uams.clara.webapp.contract.domain.contractform.ContractFormXmlDataDocumentWrapper;

@Repository
public class ContractFormXmlDataDocumentDao extends
		AbstractDomainDao<ContractFormXmlDataDocument> {

	private static final long serialVersionUID = 3841006897986675159L;
	
	
	@Transactional(readOnly = true)
	public ContractFormXmlDataDocument getContractFormXmlDataDocumentByUploadedFileID(long uploadedFileID){
		String query = "SELECT cfd FROM ContractFormXmlDataDocument cfd "
				+ " WHERE cfd.uploadedFile.id = :uploadedFileID AND cfd.retired = :retired ";
		TypedQuery<ContractFormXmlDataDocument> q = getEntityManager().createQuery(query, ContractFormXmlDataDocument.class);
		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("uploadedFileID", uploadedFileID);
		ContractFormXmlDataDocument contractFormXmlDataDocument= null;
		try{
			contractFormXmlDataDocument= q.getSingleResult();
		}
		catch(Exception e){
		}
		return contractFormXmlDataDocument;
		
	}
	
	@Transactional(readOnly = true)
	public long countDocumentRevisionsByParentId(
			long parentId) {

		/*String nativeQuery = "SELECT contract_form_xml_data_document.version_id FROM contract_form_xml_data_document "
				+ " WHERE contract_form_xml_data_document.parent_id = :parentId AND contract_form_xml_data_document.retired = :retired "
				+ " ORDER BY contract_form_xml_data_document.id DESC";*/
		
		String query = "SELECT cfdm.versionId FROM ContractFormXmlDataDocument cfdm "
				+ " WHERE cfdm.parent.id = :parentId AND cfdm.retired = :retired "
				+ " ORDER BY cfdm.id DESC";

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
	public List<ContractFormXmlDataDocumentWrapper> listContractFormXmlDataDocumentRevisionsByParentId(
			long parentId) {
		
		String nativeQuery = "SELECT contract_form_xml_data_document.*, contract_form.id AS contract_form_id, contract_form.contract_form_type AS contract_form_type, contract_form.parent_id AS parent_contract_form_id FROM contract_form_xml_data_document "
				+ " INNER JOIN contract_form_xml_data ON contract_form_xml_data.id = contract_form_xml_data_document.contract_form_xml_data_id "
				+ " INNER JOIN contract_form ON contract_form.id = contract_form_xml_data.contract_form_id "
				+ " WHERE contract_form_xml_data_document.parent_id = :parentId AND contract_form_xml_data_document.retired = :retired ORDER BY contract_form_xml_data_document.id DESC ";


		TypedQuery<ContractFormXmlDataDocumentWrapper> q = (TypedQuery<ContractFormXmlDataDocumentWrapper>) getEntityManager()
				.createNativeQuery(nativeQuery, ContractFormXmlDataDocumentWrapper.class);
		q.setHint("org.hibernate.cacheable", false);
		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("parentId", parentId);

		return q.getResultList();
	}

	/*
	 * contract is not VERSION-ED, but ContractForm and ContractFormXmlData are
	 * both version-ed. So, this function list all contract form documents link
	 * to this contractForm and all documents link to this contractForm's
	 * previous versions
	 */
	@Transactional(readOnly = true)
	public List<ContractFormXmlDataDocumentWrapper> listDocumentsByContractFormId(
			long contractFormId) {

		/*String query = "SELECT pfd FROM ContractFormXmlDataDocument pfd "
				+ " WHERE pfd.id IN ("
				+ " SELECT MAX(pfdd.id) FROM ContractFormXmlDataDocument pfdd, ContractForm pf "
				+ " WHERE pfdd.retired = :retired"
				+ " AND pf.id = :contractFormId AND ("
				+ " pfdd.contractFormXmlData.contractForm.parent.id = pf.parent.id "
				+ " AND pfdd.contractFormXmlData.contractForm.created <= pf.created) "
				+ " GROUP BY pfdd.parent.id )";*/
		
		String nativeQuery = "SELECT contract_form_xml_data_document.*, contract_form.id AS contract_form_id, contract_form.contract_form_type AS contract_form_type, contract_form.parent_id AS parent_contract_form_id FROM contract_form_xml_data_document "
				+ " INNER JOIN contract_form_xml_data ON contract_form_xml_data.id = contract_form_xml_data_document.contract_form_xml_data_id "
				+ " INNER JOIN contract_form ON contract_form.id = contract_form_xml_data.contract_form_id "
				+ " WHERE contract_form_xml_data_document.retired = :retired AND contract_form_xml_data_document.id IN ("
				+ " SELECT MAX(pfdd.id) FROM contract_form_xml_data_document pfdd, contract_form pf"
				+ " WHERE pfdd.retired = :retired "
				+ " AND pf.retired = :retired AND pf.id = :contractFormId AND pf.parent_id = ("
				+ " SELECT p.parent_id FROM contract_form_xml_data pfxd, contract_form p "
				+ " WHERE pfxd.id = pfdd.contract_form_xml_data_id AND pfxd.contract_form_id = p.id "
				+ " AND p.created <= pf.created) "
				+ " GROUP BY pfdd.parent_id)";

		TypedQuery<ContractFormXmlDataDocumentWrapper> q = (TypedQuery<ContractFormXmlDataDocumentWrapper>) getEntityManager()
				.createNativeQuery(nativeQuery, ContractFormXmlDataDocumentWrapper.class);
		q.setHint("org.hibernate.cacheable", false);
		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("contractFormId", contractFormId);

		return q.getResultList();
	}
	
	@Transactional(readOnly = true)
	public List<ContractFormXmlDataDocumentWrapper> listDocumentsByContractFormIdAndCategory(
			long contractFormId, String category) {

		/*String query = "SELECT pfd FROM ContractFormXmlDataDocument pfd "
			+ " WHERE pfd.id IN ("
			+ " SELECT MAX(pfdd.id) FROM ContractFormXmlDataDocument pfdd, ContractForm pf "
			+ " WHERE pfdd.retired = :retired"
			+ " AND pf.id = :contractFormId AND pfdd.category = :category AND ("
			+ " pfdd.contractFormXmlData.contractForm.parent.id = pf.parent.id "
			+ " AND pfdd.contractFormXmlData.contractForm.created <= pf.created) "
			+ " GROUP BY pfdd.parent.id )";*/
		
		String nativeQuery = "SELECT contract_form_xml_data_document.*, contract_form.id AS contract_form_id, contract_form.contract_form_type AS contract_form_type, contract_form.parent_id AS parent_contract_form_id FROM contract_form_xml_data_document "
				+ " INNER JOIN contract_form_xml_data ON contract_form_xml_data.id = contract_form_xml_data_document.contract_form_xml_data_id "
				+ " INNER JOIN contract_form ON contract_form.id = contract_form_xml_data.contract_form_id "
				+ " WHERE contract_form_xml_data_document.retired = :retired AND contract_form_xml_data_document.id IN ("
				+ " SELECT MAX(pfdd.id) FROM contract_form_xml_data_document pfdd, contract_form pf"
				+ " WHERE pfdd.retired = :retired "
				+ " AND pf.retired = :retired AND pf.id = :contractFormId AND pf.parent_id = ("
				+ " SELECT p.parent_id FROM contract_form_xml_data pfxd, contract_form p "
				+ " WHERE pfxd.id = pfdd.contract_form_xml_data_id AND pfxd.contract_form_id = p.id "
				+ " AND p.created <= pf.created) AND pfdd.category = :category "
				+ " GROUP BY pfdd.parent_id)";

		TypedQuery<ContractFormXmlDataDocumentWrapper> q = (TypedQuery<ContractFormXmlDataDocumentWrapper>) getEntityManager()
				.createNativeQuery(nativeQuery, ContractFormXmlDataDocumentWrapper.class);
		q.setHint("org.hibernate.cacheable", false);
		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("contractFormId", contractFormId);
		q.setParameter("category", category);

		return q.getResultList();
	}
	
	@Transactional(readOnly = true)
	public List<ContractFormXmlDataDocumentWrapper> listDocumentsByContractFormIdAndCommittee(
			long contractFormId, Committee committee) {

		/*String query = "SELECT pfd FROM ContractFormXmlDataDocument pfd "
			+ " WHERE pfd.id IN ("
			+ " SELECT MAX(pfdd.id) FROM ContractFormXmlDataDocument pfdd, ContractForm pf "
			+ " WHERE pfdd.retired = :retired"
			+ " AND pf.id = :contractFormId AND pfdd.committee = :committee AND ("
			+ " pfdd.contractFormXmlData.contractForm.parent.id = pf.parent.id "
			+ " AND pfdd.contractFormXmlData.contractForm.created <= pf.created) "
			+ " GROUP BY pfdd.parent.id )";*/
		
		String nativeQuery = "SELECT contract_form_xml_data_document.*, contract_form.id AS contract_form_id, contract_form.contract_form_type AS contract_form_type, contract_form.parent_id AS parent_contract_form_id FROM contract_form_xml_data_document "
				+ " INNER JOIN contract_form_xml_data ON contract_form_xml_data.id = contract_form_xml_data_document.contract_form_xml_data_id "
				+ " INNER JOIN contract_form ON contract_form.id = contract_form_xml_data.contract_form_id "
				+ " WHERE contract_form_xml_data_document.retired = :retired AND contract_form_xml_data_document.id IN ("
				+ " SELECT MAX(pfdd.id) FROM contract_form_xml_data_document pfdd, contract_form pf"
				+ " WHERE pfdd.retired = :retired "
				+ " AND pf.retired = :retired AND pf.id = :contractFormId AND pf.parent_id = ("
				+ " SELECT p.parent_id FROM contract_form_xml_data pfxd, contract_form p "
				+ " WHERE pfxd.id = pfdd.contract_form_xml_data_id AND pfxd.contract_form_id = p.id "
				+ " AND p.created <= pf.created) AND pfdd.committee = :committee "
				+ " GROUP BY pfdd.parent_id)";

		TypedQuery<ContractFormXmlDataDocumentWrapper> q = (TypedQuery<ContractFormXmlDataDocumentWrapper>) getEntityManager()
				.createNativeQuery(nativeQuery, ContractFormXmlDataDocumentWrapper.class);
		q.setHint("org.hibernate.cacheable", false);
		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("contractFormId", contractFormId);
		q.setParameter("committee", committee);

		return q.getResultList();
	}
	
	@Transactional(readOnly = true)
	public List<ContractFormXmlDataDocumentWrapper> listDocumentsByContractFormIdAndLaterThanDate(
			long contractFormId, Date created) {

		/*String query = "SELECT pfd FROM ContractFormXmlDataDocument pfd "
			+ " WHERE pfd.created >= :created AND pfd.id IN ("
			+ " SELECT MAX(pfdd.id) FROM ContractFormXmlDataDocument pfdd, ContractForm pf "
			+ " WHERE pfdd.retired = :retired"
			+ " AND pf.id = :contractFormId AND ("
			+ " pfdd.contractFormXmlData.contractForm.parent.id = pf.parent.id "
			+ " AND pfdd.contractFormXmlData.contractForm.created <= pf.created) "
			+ " GROUP BY pfdd.parent.id )";*/
		
		String nativeQuery = "SELECT contract_form_xml_data_document.*, contract_form.id AS contract_form_id, contract_form.contract_form_type AS contract_form_type, contract_form.parent_id AS parent_contract_form_id FROM contract_form_xml_data_document "
				+ " INNER JOIN contract_form_xml_data ON contract_form_xml_data.id = contract_form_xml_data_document.contract_form_xml_data_id "
				+ " INNER JOIN contract_form ON contract_form.id = contract_form_xml_data.contract_form_id "
				+ " WHERE contract_form_xml_data_document.retired = :retired "
				+ " AND contract_form_xml_data_document.created >= :created "
				+ " AND contract_form_xml_data_document.id IN ("
				+ " SELECT MAX(pfdd.id) FROM contract_form_xml_data_document pfdd, contract_form pf"
				+ " WHERE pfdd.retired = :retired "
				+ " AND pf.retired = :retired AND pf.id = :contractFormId AND pf.parent_id = ("
				+ " SELECT p.parent_id FROM contract_form_xml_data pfxd, contract_form p "
				+ " WHERE pfxd.id = pfdd.contract_form_xml_data_id AND pfxd.contract_form_id = p.id "
				+ " AND p.created <= pf.created) AND pfdd.committee = :committee "
				+ " GROUP BY pfdd.parent_id)";

		TypedQuery<ContractFormXmlDataDocumentWrapper> q = (TypedQuery<ContractFormXmlDataDocumentWrapper>) getEntityManager()
				.createNativeQuery(nativeQuery, ContractFormXmlDataDocumentWrapper.class);
		q.setHint("org.hibernate.cacheable", false);
		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("contractFormId", contractFormId);
		q.setParameter("created", created);

		return q.getResultList();
	}
	
	
	@Transactional(readOnly = true)
	public List<ContractFormXmlDataDocument> listDocumentRevisionsByContractFormXmlDataDocumentId(
			long contractFormXmlDataId) {
		
		
		String nativeQuery = "SELECT cfd.* FROM contract_form_xml_data_document cfd"
						+ " WHERE cfd.parent_id IN (SELECT cfxd.parent_id FROM contract_form_xml_data_document cfxd WHERE cfxd.id = :contractFormXmlDataId AND cfxd.retired = :retired) "
						+ " AND cfd.retired = :retired";

		TypedQuery<ContractFormXmlDataDocument> q = (TypedQuery<ContractFormXmlDataDocument>) getEntityManager()
				.createNativeQuery(nativeQuery, ContractFormXmlDataDocument.class);

		q.setHint("org.hibernate.cacheable", false);
		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("contractFormXmlDataId", contractFormXmlDataId);

		return q.getResultList();
	}
	

	/**
	 * this functions differs from the previous function is that it list
	 * documents link to the right contractForm and related contractForms let's
	 * say we have docId 1, 2 links to contractFormId 1 (PF 1); docId 3 links to
	 * contractFormId 2; and docId 4, 5 links to protcolFormId 3, where PF 2 is
	 * a revision of PF 1, and PF 3 is a revision of PF 2, so that PF3.created >
	 * PF2.created > PF1.created if we run
	 * listAllDocumentsByContractFormId(PF2), it will return
	 * docIDs {1,2,3,4,5} while in previous function
	 * listDocumentsByContractFormId(PF2) should return {1, 2, 3}
	 * 
	 * and remember, listDocumentsByContractFormId(PF3) == listAllDocumentsByContractFormId(PF3) 
	 * == listAllDocumentsByContractFormId(PF2) == listAllDocumentsByContractFormId(PF1)
	 * 
	 * @param contractForm
	 * @return
	 */
	/*
	@Transactional(readOnly = true)
	public List<ContractFormXmlDataDocument> listAllDocumentsByContractFormId(
			long contractFormId) {

		String query = "SELECT pfd FROM ContractFormXmlDataDocument pfd "
				+ " WHERE pfd.id IN ("
				+ " SELECT MAX(pfdd.id) FROM ContractFormXmlDataDocument pfdd, ContractForm pf "
				+ " WHERE pfdd.retired = :retired"
				+ " AND pf.id = :contractFormId "
				+ " AND pfdd.contractFormXmlData.contractForm.parent = pf.parent "
				+ " GROUP BY pfdd.parent.id )";

		TypedQuery<ContractFormXmlDataDocument> q = getEntityManager()
				.createQuery(query, ContractFormXmlDataDocument.class);
		q.setHint("org.hibernate.cacheable", false);
		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("contractFormId", contractFormId);

		return q.getResultList();
	}*/
	
	public List<String> listContractFormXmlDataDocumentCategories(long contractFormId) {
//		String query = "SELECT DISTINCT pfd.category FROM ContractFormXmlDataDocument pfd "
//				+ " WHERE pfd.retired = :retired"
//				+ " AND pfd.contractFormXmlData.id = :contractFormXmlDataId";
		String query = "SELECT pfd.category FROM ContractFormXmlDataDocument pfd "
				+ " WHERE pfd.id IN ("
				+ " SELECT MAX(pfdd.id) FROM ContractFormXmlDataDocument pfdd, ContractForm pf "
				+ " WHERE pfdd.retired = :retired"
				+ " AND pf.id = :contractFormId "
				+ " AND pfdd.contractFormXmlData.contractForm.parent = pf.parent "
				+ " GROUP BY pfdd.parent.id )";

		TypedQuery<String> q = getEntityManager().createQuery(query,
				String.class);
		q.setHint("org.hibernate.cacheable", true);
		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("contractFormId", contractFormId);

		return q.getResultList();
	}
	
	public ContractFormXmlDataDocument getContractFormXmlDataDocumentParentByParentID(int parentID){
		String query = "SELECT pfd FROM ContractFormXmlDataDocument pfd "
				+ " WHERE pfd.parent.id =:parentID AND pfdd.retired = :retired";
		TypedQuery<ContractFormXmlDataDocument> q = getEntityManager().createQuery(query,
				ContractFormXmlDataDocument.class);
		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("contractFormId", parentID);
		ContractFormXmlDataDocument contractFormXmlDataDocument = null;
		try{
			contractFormXmlDataDocument = q.getSingleResult();
			return contractFormXmlDataDocument;
		}
		catch(Exception e){
			return null;
		}
	}
	
	

}
