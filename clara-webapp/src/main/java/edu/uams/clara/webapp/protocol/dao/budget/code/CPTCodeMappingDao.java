package edu.uams.clara.webapp.protocol.dao.budget.code;

import java.util.List;

import javax.persistence.TypedQuery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import edu.uams.clara.core.dao.AbstractDomainDao;
import edu.uams.clara.webapp.protocol.domain.budget.code.CPTCodeMapping;

@Repository
public class CPTCodeMappingDao extends AbstractDomainDao<CPTCodeMapping> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4022913915692087326L;
	private final static Logger logger = LoggerFactory
			.getLogger(CPTCodeMappingDao.class);
	
	@Transactional(readOnly = true)
	public List<CPTCodeMapping> findByCPTCode(String cptCode) {

		String query = "SELECT c FROM CPTCodeMapping c "
				+ " WHERE c.retired = :retired AND (c.cptCode LIKE :cptCode OR c.otherCode LIKE :cptCode)";

		TypedQuery<CPTCodeMapping> q = getEntityManager().createQuery(query,
				CPTCodeMapping.class);
		q.setHint("org.hibernate.cacheable", true);
		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("cptCode", cptCode);

		return q.getResultList();

	}

}
