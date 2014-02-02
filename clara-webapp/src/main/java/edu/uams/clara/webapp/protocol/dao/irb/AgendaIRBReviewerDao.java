package edu.uams.clara.webapp.protocol.dao.irb;

import java.util.List;

import javax.persistence.TypedQuery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import edu.uams.clara.core.dao.AbstractDomainDao;
import edu.uams.clara.webapp.protocol.domain.irb.AgendaIRBReviewer;

@Repository
public class AgendaIRBReviewerDao extends AbstractDomainDao<AgendaIRBReviewer> {

	private static final long serialVersionUID = 7043776873775104092L;

	private final static Logger logger = LoggerFactory
	.getLogger(AgendaIRBReviewerDao.class);
	
	@Transactional(readOnly = true)
	public List<AgendaIRBReviewer> listAgendaIRBReviewersByAgendaId(long agendaId) {
		
		TypedQuery<AgendaIRBReviewer> query = getEntityManager()
				.createQuery(
						"SELECT a FROM AgendaIRBReviewer a WHERE a.retired = :retired AND a.agenda.id = :agendaId",
						AgendaIRBReviewer.class)
				.setParameter("retired", Boolean.FALSE)
				.setParameter("agendaId", agendaId);
		query.setHint("org.hibernate.cacheable", true);		

		return query.getResultList();
	}
	
}
