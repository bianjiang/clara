package edu.uams.clara.webapp.contract.dao.businesslogicobject;

import javax.persistence.TypedQuery;

import org.springframework.stereotype.Repository;

import edu.uams.clara.core.dao.AbstractDomainDao;
import edu.uams.clara.webapp.common.domain.usercontext.enums.Committee;
import edu.uams.clara.webapp.contract.domain.businesslogicobject.ContractFormCommitteeChecklistXmlData;

@Repository
public class ContractFormCommitteeChecklistXmlDataDao extends AbstractDomainDao<ContractFormCommitteeChecklistXmlData> {



	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 *  
	 * @param formId
	 * @param committee
	 * @return 
	 */
	public ContractFormCommitteeChecklistXmlData findLatestByContractFormIdAndCommittee(long contractFormId, Committee committee){
		String query = "SELECT fccxd FROM ContractFormCommitteeChecklistXmlData fccxd "
				+ " WHERE fccxd.contractForm.id = :contractFormId AND fccxd.retired = :retired "
				+ " AND fccxd.committee = :committee "
				+ " ORDER BY fccxd.created DESC";

		TypedQuery<ContractFormCommitteeChecklistXmlData> q = getEntityManager().createQuery(query,
				ContractFormCommitteeChecklistXmlData.class);

		q.setHint("org.hibernate.cacheable", true);
		q.setFirstResult(0);
		q.setMaxResults(1);
		
		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("contractFormId", contractFormId);
		q.setParameter("committee", committee);	
			
		return q.getSingleResult();

	}

}
