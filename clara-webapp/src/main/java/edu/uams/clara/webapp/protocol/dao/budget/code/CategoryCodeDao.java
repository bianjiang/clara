package edu.uams.clara.webapp.protocol.dao.budget.code;

import javax.persistence.TypedQuery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import edu.uams.clara.core.dao.AbstractDomainDao;
import edu.uams.clara.webapp.protocol.domain.budget.code.CategoryCode;

@Repository
public class CategoryCodeDao extends AbstractDomainDao<CategoryCode> {

	private static final long serialVersionUID = -35078597471728977L;
	private final static Logger logger = LoggerFactory
			.getLogger(CategoryCodeDao.class);

	@Transactional(readOnly = true)
	public CategoryCode findByCode(String code) {

		String query = "SELECT c FROM CategoryCode c "
				+ " WHERE c.retired = :retired AND c.code LIKE :code";

		TypedQuery<CategoryCode> q = getEntityManager().createQuery(query,
				CategoryCode.class);
		q.setHint("org.hibernate.cacheable", true);
		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("code", code);

		CategoryCode categoryCode = null;
		try {
			categoryCode = q.getSingleResult();
		} catch (Exception ex) {
			logger.warn(ex.getMessage());
		}

		return categoryCode;

	}

}
