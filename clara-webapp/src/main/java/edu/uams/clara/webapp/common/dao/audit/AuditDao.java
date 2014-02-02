package edu.uams.clara.webapp.common.dao.audit;

import org.springframework.stereotype.Repository;
import edu.uams.clara.core.dao.AbstractDomainDao;
import edu.uams.clara.webapp.common.domain.audit.Audit;

@Repository
public class AuditDao extends AbstractDomainDao<Audit> {

	private static final long serialVersionUID = 3940577669286896600L;


}