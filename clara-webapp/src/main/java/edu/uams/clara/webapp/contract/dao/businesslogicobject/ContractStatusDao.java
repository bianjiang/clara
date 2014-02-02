package edu.uams.clara.webapp.contract.dao.businesslogicobject;

import java.util.List;

import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;

import edu.uams.clara.core.dao.AbstractDomainDao;
import edu.uams.clara.webapp.contract.domain.Contract;
import edu.uams.clara.webapp.contract.domain.businesslogicobject.ContractStatus;
import edu.uams.clara.webapp.contract.domain.businesslogicobject.enums.ContractStatusEnum;

/**
 * @author yufan
 *
 */
@Repository
public class ContractStatusDao extends AbstractDomainDao<ContractStatus> {

	private static final long serialVersionUID = 3693534301704910264L;
	
	private final static Logger logger = LoggerFactory
			.getLogger(ContractStatusDao.class);
	
	@Transactional(readOnly = true)
	public ContractStatus findContractStatusByContractId(long contractID) {
		String query = "SELECT cs FROM ContractStatus cs "
				+ " WHERE cs.contract.id = :contractID AND cs.retired = :retired ORDER BY cs.id DESC";
		TypedQuery<ContractStatus> q = getEntityManager().createQuery(query,
				ContractStatus.class);
		q.setParameter("contractID", contractID);
		q.setParameter("retired", Boolean.FALSE);
		
		return q.getSingleResult();
	}
	
	@Transactional(readOnly = true)
	public List<ContractStatus> findAllContractStatusByContractId(long contractID) {
		String query = "SELECT cs FROM ContractStatus cs "
				+ " WHERE cs.contract.id = :contractID AND cs.retired = :retired ORDER BY cs.id DESC";
		TypedQuery<ContractStatus> q = getEntityManager().createQuery(query,
				ContractStatus.class);
		q.setParameter("contractID", contractID);
		q.setParameter("retired", Boolean.FALSE);
		
		return q.getResultList();
	}
	
	/**
	 * @param contractID
	 * @return
	 * 
	 * Used to check if a contract has been submitted or not, Under Budget Manager Review is the second step in normal workflow,
	 * so if the status list include UNDER_BUDGET_MANAGER_REVIEW, it means the contract has been submitted.
	 * But for some contracts that are administratively executed at the very beginning when contract is created, we need to check if 
	 * CONTRACT_EXECUTED status is included to determine if contract is submitted.
	 */
	@Transactional(readOnly = true)
	public boolean checkIfContractIsSubmitted(long contractID) {
		String query = "SELECT COUNT(*) FROM contract_status cs "
				+ " WHERE cs.contract_id = :contractID AND cs.retired = :retired AND cs.contract_status IN ('UNDER_CONTRACT_MANAGER_REVIEW', 'CONTRACT_EXECUTED')";
		Query q = getEntityManager().createNativeQuery(query);
		q.setParameter("contractID", contractID);
		q.setParameter("retired", Boolean.FALSE);
		//q.setParameter("contractStatuses", contractStatuses);

		try {
			long total = Long.valueOf(q.getSingleResult().toString());
			logger.debug("^^^^^^^^^ " + total);
			if (total > 0) {
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			return false;
		}
	}

}
