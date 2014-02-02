package edu.uams.clara.webapp.report.dao;

import java.util.List;

import javax.persistence.TypedQuery;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import edu.uams.clara.core.dao.AbstractDomainDao;
import edu.uams.clara.webapp.report.domain.ReportCriteria;

@Repository
public class ReportCriteriaDao extends AbstractDomainDao<ReportCriteria> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3722147707866261645L;
	
	@Transactional(readOnly = true)
	public List<ReportCriteria> listAllCriteriasByReportTemplateId(long reportTemplateId){
		String query = "SELECT rc FROM ReportCriteria rc "
				+ " WHERE rc.reportTemplate.id = :reportTemplateId AND rc.retired = :retired";
		
		TypedQuery<ReportCriteria> q = getEntityManager()
				.createQuery(query, ReportCriteria.class);
		
		q.setHint("org.hibernate.cacheable", true);

		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("reportTemplateId", reportTemplateId);
		return q.getResultList();
	}

}
