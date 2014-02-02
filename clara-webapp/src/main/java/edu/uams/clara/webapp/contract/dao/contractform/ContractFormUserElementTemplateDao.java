package edu.uams.clara.webapp.contract.dao.contractform;

import java.util.List;

import javax.persistence.TypedQuery;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import edu.uams.clara.core.dao.AbstractDomainDao;
import edu.uams.clara.webapp.contract.domain.contractform.ContractFormUserElementTemplate;
import edu.uams.clara.webapp.contract.domain.contractform.ContractFormUserElementTemplate.TemplateType;

@Repository
public class ContractFormUserElementTemplateDao extends
		AbstractDomainDao<ContractFormUserElementTemplate> {

	private static final long serialVersionUID = 5430512720096443789L;

	@Transactional(readOnly = true)
	public List<ContractFormUserElementTemplate> listContractFormUserElementTemplateByTemplateTypeAndUserId(
			TemplateType templateType, long userId) {

		String query = "SELECT pfuet FROM ContractFormUserElementTemplate pfuet "
				+ " WHERE pfuet.retired = :retired AND pfuet.user.id = :userId AND pfuet.templateType = :templateType "
				+ " ORDER BY pfuet.templateName ASC";

		TypedQuery<ContractFormUserElementTemplate> q = getEntityManager()
				.createQuery(query, ContractFormUserElementTemplate.class);

		q.setHint("org.hibernate.cacheable", true);

		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("userId", userId);
		q.setParameter("templateType", templateType);
		return q.getResultList();

	}

}
