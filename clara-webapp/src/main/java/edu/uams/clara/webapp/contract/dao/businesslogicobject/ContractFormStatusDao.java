package edu.uams.clara.webapp.contract.dao.businesslogicobject;

import javax.persistence.TypedQuery;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import edu.uams.clara.core.dao.AbstractDomainDao;
import edu.uams.clara.webapp.contract.domain.businesslogicobject.ContractFormStatus;
import edu.uams.clara.webapp.contract.domain.businesslogicobject.ContractFormStatus;


@Repository
public class ContractFormStatusDao extends AbstractDomainDao<ContractFormStatus> {

	private static final long serialVersionUID = -2366324292063053946L;

	public ContractFormStatus getContractFormStatusByFormId(long contractFormId){
		String query = "SELECT fs FROM ContractFormStatus fs "
			+ " WHERE fs.contractForm.id = :contractFormId AND fs.retired = :retired ORDER BY fs.id DESC";

		TypedQuery<ContractFormStatus> q = getEntityManager().createQuery(query,
				ContractFormStatus.class);
		q.setFirstResult(0);
		q.setMaxResults(1);
		q.setHint("org.hibernate.cacheable", true);
		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("contractFormId", contractFormId);
	
		return q.getSingleResult();
	}
	
	@Transactional(readOnly = true)
	public ContractFormStatus getLatestContractFormStatusByFormId(long contractFormId){
		String query = "SELECT pfs FROM ContractFormStatus pfs WHERE pfs.retired = :retired AND pfs.id="
			+ "(SELECT MAX(fs.id) FROM ContractFormStatus fs "
			+ " WHERE fs.retired = :retired "
			+ " AND fs.contractForm.id IN (SELECT pf.id FROM ContractForm pf WHERE pf.retired = :retired AND pf.parent.id = (SELECT ppf.parent.id FROM ContractForm ppf WHERE ppf.retired = :retired AND ppf.id = :contractFormId)))";

		TypedQuery<ContractFormStatus> q = getEntityManager().createQuery(query,
				ContractFormStatus.class);
		q.setFirstResult(0);
		q.setMaxResults(1);
		q.setHint("org.hibernate.cacheable", true);
		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("contractFormId", contractFormId);

		return q.getSingleResult();
	}
}


