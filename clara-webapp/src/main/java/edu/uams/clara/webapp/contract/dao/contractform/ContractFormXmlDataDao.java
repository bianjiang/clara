package edu.uams.clara.webapp.contract.dao.contractform;

import javax.persistence.TypedQuery;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import edu.uams.clara.core.dao.AbstractDomainDao;
import edu.uams.clara.webapp.contract.domain.contractform.ContractFormXmlData;
import edu.uams.clara.webapp.contract.domain.contractform.enums.ContractFormXmlDataType;

@Repository
public class ContractFormXmlDataDao extends
		AbstractDomainDao<ContractFormXmlData> {

	private static final long serialVersionUID = 4333242801399552123L;

	/**
	 * get the last version of the contractFormXmlData by its type and which
	 * contract form it links to
	 * 
	 * @param contractFormId
	 * @param contractFormXmlDataType
	 * @return
	 */
	@Transactional(readOnly = true)
	public ContractFormXmlData getLastContractFormXmlDataByContractFormIdAndType(
			long contractFormId, ContractFormXmlDataType contractFormXmlDataType) {
		String query = "SELECT pfxd FROM ContractFormXmlData pfxd "
				+ " WHERE pfxd.contractForm.id = :contractFormId AND pfxd.retired = :retired AND pfxd.contractFormXmlDataType = :contractFormXmlDataType"
				+ " ORDER BY pfxd.created DESC ";

		TypedQuery<ContractFormXmlData> q = getEntityManager().createQuery(
				query, ContractFormXmlData.class);

		q.setHint("org.hibernate.cacheable", true);

		q.setFirstResult(0);
		q.setMaxResults(1);
		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("contractFormXmlDataType", contractFormXmlDataType);
		q.setParameter("contractFormId", contractFormId);

		return q.getSingleResult();
	}
	
}
