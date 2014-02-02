package edu.uams.clara.webapp.report.dao;

import java.util.List;

import javax.persistence.TypedQuery;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import edu.uams.clara.core.dao.AbstractDomainDao;
import edu.uams.clara.webapp.report.domain.ReportTemplate;
import edu.uams.clara.webapp.report.domain.ReportTemplate.ScheduleType;

@Repository
public class ReportTemplateDao extends AbstractDomainDao<ReportTemplate> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6922469879172382026L;
	
	@Transactional(readOnly = true)
	public List<ReportTemplate> listReportTemplatesByUser(long userId){
		String query = "SELECT rt FROM ReportTemplate rt "
				+ " WHERE rt.user.id = :userId AND rt.retired = :retired";
		
		TypedQuery<ReportTemplate> q = getEntityManager()
				.createQuery(query, ReportTemplate.class);
		
		q.setHint("org.hibernate.cacheable", true);

		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("userId", userId);
		return q.getResultList();
	}
	
	@Transactional(readOnly = true)
	public List<ReportTemplate> listReportTemplatesByScheduleType(ScheduleType scheduleType){
		String query = "SELECT rt FROM ReportTemplate rt "
				+ " WHERE rt.scheduleType = :scheduleType AND rt.retired = :retired";
		
		TypedQuery<ReportTemplate> q = getEntityManager()
				.createQuery(query, ReportTemplate.class);
		
		q.setHint("org.hibernate.cacheable", true);

		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("scheduleType", scheduleType);
		return q.getResultList();
	}
}
