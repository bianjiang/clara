package edu.uams.clara.webapp.report.dao;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.jpa.HibernateEntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import edu.uams.clara.core.dao.AbstractDomainDao;
import edu.uams.clara.webapp.report.domain.ReportResult;
import edu.uams.clara.webapp.report.domain.result.AuditReport;

@Repository
public class ReportResultDao extends AbstractDomainDao<ReportResult> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1429091396549809557L;
	
	private final static Logger logger = LoggerFactory
			.getLogger(ReportResultDao.class);
	
	@Transactional(readOnly = true)
	public List<AuditReport> getAuditReport(long protocolId, long userId, String mostRecentApproval, String mostRecentNextApproval, String reviewType){
		String userIdQuerySt = "p.meta_data_xml.exist('/protocol/staffs/staff/user[roles/role=\"Principal Investigator\" and @id = \""+ userId +"\"]') = 1";
		String reviewTypeSt = "p.meta_data_xml.exist('/protocol/original-study/approval-status/text()[.=\""+ reviewType +"\"]')=1";
		
		String query = "SELECT p.id as protocolId, p.meta_data_xml.value('(/protocol/title/text())[1]', 'varchar(255)') as protocolTitle, p.meta_data_xml.value('(/protocol/staffs/staff/user[roles/role=\"Principal Investigator\"]/lastname/text())[1]', 'varchar(255)')+','+ p.meta_data_xml.value('(/protocol/staffs/staff/user[roles/role=\"Principal Investigator\"]/firstname/text())[1]', 'varchar(255)') as piName, ps.protocol_status as currentStatus FROM protocol p, protocol_status ps "
				+ " WHERE p.retired = :retired AND ps.retired = :retired "
				+ " AND p.id = ps.protocol_id "
				+ " AND ps.id = (SELECT MAX(id) FROM protocol_status WHERE protocol_id = p.id AND retired = :retired)"
				+ ((protocolId!=0)?" AND p.id = :protocolId":"")
				+ ((userId!=0)?" AND " + userIdQuerySt:"")
				+ ((!mostRecentApproval.isEmpty())?" AND " + mostRecentApproval:"")
				+ ((!mostRecentNextApproval.isEmpty())?" AND " + mostRecentNextApproval:"")
				+ ((!reviewType.isEmpty())?" AND " + reviewTypeSt:"")
				+ " ORDER BY p.id DESC";
		logger.debug("query: " + query);
		
		Query q =  getEntityManager()
				.createNativeQuery(query);
		
		//q.setHint("org.hibernate.cacheable", true);

		q.setParameter("retired", Boolean.FALSE);
		
		if (protocolId != 0){
			q.setParameter("protocolId", protocolId);
		}

		List<Object[]> rows = q.getResultList();
		List<AuditReport> auditReportLst = new ArrayList<AuditReport>();
		
		for (Object[] row : rows){
			AuditReport auditReport = new AuditReport((BigInteger) row[0], (String) row[1], (String) row[2], (String) row[3]);
			auditReportLst.add(auditReport);
		}
		
		return auditReportLst;
	}
	
	@Transactional(readOnly = true)
	public List<Map> generateResult(String query) {
		HibernateEntityManager hem = getEntityManager().unwrap(HibernateEntityManager.class);
		
		Session session = hem.getSession();
		
		org.hibernate.Query q = session.createSQLQuery(query);
		q.setResultTransformer(Criteria.ALIAS_TO_ENTITY_MAP);
		
		return q.list();
	}
	
	@Transactional(readOnly = true) 
	public List<ReportResult> listReportResultsByReportId(long reportId) {
		String query = "SELECT rr FROM ReportResult rr "
					+ " WHERE rr.retired = :retired AND rr.reportTemplate.id = :reportId";
		
		TypedQuery<ReportResult> q = getEntityManager()
				.createQuery(query, ReportResult.class);
		
		q.setHint("org.hibernate.cacheable", true);

		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("reportId", reportId);
		return q.getResultList();
	}

}
