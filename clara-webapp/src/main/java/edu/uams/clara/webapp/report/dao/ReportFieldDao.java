package edu.uams.clara.webapp.report.dao;

import java.util.List;

import javax.persistence.TypedQuery;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import edu.uams.clara.core.dao.AbstractDomainDao;
import edu.uams.clara.webapp.report.domain.ReportField;

@Repository
public class ReportFieldDao extends AbstractDomainDao<ReportField>{

	private static final long serialVersionUID = -658907323962888898L;

	@Transactional(readOnly = true)
	public List<ReportField> listAllFieldsByReportTemplateId(long reportTemplateId){
		String query = "SELECT rf FROM ReportField rf "
				+ " WHERE rf.reportTemplate.id = :reportTemplateId AND rf.retired = :retired";
		
		TypedQuery<ReportField> q = getEntityManager()
				.createQuery(query, ReportField.class);
		
		q.setHint("org.hibernate.cacheable", true);

		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("reportTemplateId", reportTemplateId);
		return q.getResultList();
	}
	
	@Transactional(readOnly = true)
	public ReportField getLatestFieldByReportTemplateId(long reportTemplateId){
		String query = "SELECT rf FROM ReportField rf "
				+ " WHERE rf.reportTemplate.id = :reportTemplateId AND rf.retired = :retired "
				+ " AND rf.id in (SELECT MAX(rf.id) from ReportField rf where retired =0 group by rf.reportTemplate.id)";
		
		TypedQuery<ReportField> q = getEntityManager()
				.createQuery(query, ReportField.class);
		
		q.setHint("org.hibernate.cacheable", true);

		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("reportTemplateId", reportTemplateId);
		return q.getSingleResult();
	}
}
