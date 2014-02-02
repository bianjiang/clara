package edu.uams.clara.webapp.contract.dao;

import java.util.List;

import javax.persistence.TypedQuery;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import edu.uams.clara.core.dao.AbstractDomainDao;
import edu.uams.clara.webapp.contract.dao.contractform.ContractFormXmlDataDocumentDao;
import edu.uams.clara.webapp.contract.domain.contractform.ContractFormXmlDataDocument;
import edu.uams.clara.webapp.contract.domain.contractform.ContractFormXmlDataDocumentWrapper;

@Repository
public class ContractDocumentDao extends AbstractDomainDao<ContractFormXmlDataDocument> {

	private static final long serialVersionUID = -53397841844865753L;

	private ContractFormXmlDataDocumentDao contractFormXmlDataDocumentDao;
	
	/**
	 * List all the latest files for the entire contract
	 */
	@Transactional(readOnly = true)
	public List<ContractFormXmlDataDocumentWrapper> listContractFormXmlDataDocuments(long contractId) {

		/*String query = "SELECT pfd FROM ContractFormXmlDataDocument pfd " + " WHERE pfd.id IN ("
				+ " SELECT MAX(pfdd.id) FROM ContractFormXmlDataDocument pfdd "
				+ " WHERE pfdd.retired = :retired"
				+ " AND pfdd.contractFormXmlData.contractForm.contract = :contract "
				//+ " AND pfdd.contractFormXmlData.contractFormXmlDataType = :contractFormXmlDataType "
				+ " GROUP BY pfdd.parent.id )";*/
		
		String nativeQuery = "SELECT contract_form_xml_data_document.*, contract_form.id AS contract_form_id, contract_form.contract_form_type AS contract_form_type, contract_form.parent_id AS parent_contract_form_id  FROM contract_form_xml_data_document" +
				" INNER JOIN contract_form_xml_data ON contract_form_xml_data.id = contract_form_xml_data_document.contract_form_xml_data_id" +
				" INNER JOIN contract_form ON contract_form.id = contract_form_xml_data.contract_form_id" +
				" WHERE contract_form_xml_data_document.retired = :retired AND contract_form_xml_data_document.id IN (" +
				" SELECT MAX(contract_form_xml_data_document.id) FROM contract_form_xml_data_document" +
				" INNER JOIN contract_form_xml_data ON contract_form_xml_data.id = contract_form_xml_data_document.contract_form_xml_data_id" +
				" INNER JOIN contract_form ON contract_form.id = contract_form_xml_data.contract_form_id" +
				" INNER JOIN contract ON contract.id = contract_form.contract_id" +
				" WHERE contract_form_xml_data_document.retired = :retired AND contract_form_xml_data.retired = :retired AND contract_form.retired = :retired AND contract.retired = :retired AND  contract.id = :contractId" +
				" GROUP BY contract_form_xml_data_document.parent_id)";

		TypedQuery<ContractFormXmlDataDocumentWrapper> q = (TypedQuery<ContractFormXmlDataDocumentWrapper>) getEntityManager().createNativeQuery(nativeQuery,
				ContractFormXmlDataDocumentWrapper.class);
		q.setHint("org.hibernate.cacheable", false);
		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("contractId", contractId);
		//q.setParameter("contractFormXmlDataType", ContractFormXmlDataType.PROTOCOL);

		return q.getResultList();

	}

	/**
	 * List all revisions for the specified FormXmlDataDocument,
	 * relay to the formXmlDataDocumentDao to do the work...
	 */
	@Transactional(readOnly = true)
	public List<ContractFormXmlDataDocumentWrapper> listContractFormXmlDataDocumentRevisionsByParentId(
			long parentId) {
		return contractFormXmlDataDocumentDao.listContractFormXmlDataDocumentRevisionsByParentId(parentId);
	}

	@Autowired(required=true)
	public void setContractFormXmlDataDocumentDao(
			ContractFormXmlDataDocumentDao contractFormXmlDataDocumentDao) {
		this.contractFormXmlDataDocumentDao = contractFormXmlDataDocumentDao;
	}

	public ContractFormXmlDataDocumentDao getContractFormXmlDataDocumentDao() {
		return contractFormXmlDataDocumentDao;
	}
}
