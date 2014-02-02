package edu.uams.clara.webapp.common.dao.email;

import javax.persistence.TypedQuery;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import edu.uams.clara.core.dao.AbstractDomainDao;
import edu.uams.clara.webapp.common.domain.email.EmailTemplate;

@Repository
public class EmailTemplateDao extends AbstractDomainDao<EmailTemplate> {

	private static final long serialVersionUID = -237429716739473762L;

	//private final static Logger logger = LoggerFactory
	//		.getLogger(EmailTemplateDao.class);
	
	@Transactional(readOnly=true)
	public EmailTemplate findByIdentifier(String identifier){
		String query = "SELECT et FROM EmailTemplate et WHERE et.retired = :retired AND et.identifier = :identifier";
		TypedQuery<EmailTemplate> q = getEntityManager().createQuery(query,
				EmailTemplate.class);
		q.setFirstResult(0);
		q.setMaxResults(1);
		q.setHint("org.hibernate.cacheable", true);
		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("identifier", identifier);
		
		return q.getSingleResult();
	}
}
