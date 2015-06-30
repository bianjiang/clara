package edu.uams.clara.webapp.protocol.dao.businesslogicobject;

import java.util.List;
import java.util.Set;

import javax.persistence.TypedQuery;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import edu.uams.clara.core.dao.AbstractDomainDao;
import edu.uams.clara.webapp.common.domain.usercontext.enums.Committee;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.ProtocolFormCommitteeStatus;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.enums.ProtocolFormCommitteeStatusEnum;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.enums.ProtocolFormStatusEnum;
import edu.uams.clara.webapp.protocol.domain.irb.IRBReviewer;
import edu.uams.clara.webapp.protocol.domain.protocolform.enums.ProtocolFormType;

@Repository
public class ProtocolFormCommitteeStatusDao extends
		AbstractDomainDao<ProtocolFormCommitteeStatus> {

	private static final long serialVersionUID = 2397010827587080822L;

	@Transactional(readOnly = true)
	public List<ProtocolFormCommitteeStatus> listLatestByProtocolFormId(
			long protocolFormId) {
		String query = "SELECT fcs FROM ProtocolFormCommitteeStatus fcs "
				+ " WHERE fcs.id IN ("
				+ " SELECT MAX(mfcs.id) FROM ProtocolFormCommitteeStatus mfcs, ProtocolForm pf "
				+ " WHERE pf.id = :protocolFormId AND mfcs.protocolForm.parent.id = pf.parent.id AND mfcs.retired = :retired "
				+ " GROUP BY mfcs.committee)";

		TypedQuery<ProtocolFormCommitteeStatus> q = getEntityManager()
				.createQuery(query, ProtocolFormCommitteeStatus.class);

		q.setHint("org.hibernate.cacheable", true);
		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("protocolFormId", protocolFormId);

		return q.getResultList();

	}

	@Transactional(readOnly = true)
	public List<ProtocolFormCommitteeStatus> listAllByCommitteeAndProtocolFormId(
			Committee committee, long protocolFormId) {
		String query = "SELECT fcs FROM ProtocolFormCommitteeStatus fcs, ProtocolForm pf "
				+ " WHERE fcs.protocolForm.parent.id = pf.parent.id "
				+ " AND pf.id = :protocolFormId AND fcs.committee = :committee"
				+ " AND fcs.retired = :retired AND pf.retired = :retired AND fcs.protocolForm.retired = :retired AND pf.parent.retired = :retired ORDER BY fcs.modified ASC";

		TypedQuery<ProtocolFormCommitteeStatus> q = getEntityManager()
				.createQuery(query, ProtocolFormCommitteeStatus.class);

		q.setHint("org.hibernate.cacheable", true);
		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("committee", committee);
		q.setParameter("protocolFormId", protocolFormId);

		return q.getResultList();

	}
	
	@Transactional(readOnly = true)
	public List<ProtocolFormCommitteeStatus> listAllByProtocolFormId(long protocolFormId) {
		String query = "SELECT fcs FROM ProtocolFormCommitteeStatus fcs, ProtocolForm pf "
				+ " WHERE fcs.protocolForm.parent.id = pf.parent.id "
				+ " AND pf.id = :protocolFormId "
				+ " AND fcs.retired = :retired AND pf.retired = :retired AND fcs.protocolForm.retired = :retired AND pf.parent.retired = :retired ORDER BY fcs.modified ASC";

		TypedQuery<ProtocolFormCommitteeStatus> q = getEntityManager()
				.createQuery(query, ProtocolFormCommitteeStatus.class);

		q.setHint("org.hibernate.cacheable", true);
		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("protocolFormId", protocolFormId);

		return q.getResultList();

	}
	
	@Transactional(readOnly = true)
	public List<ProtocolFormCommitteeStatus> listAllByCommitteeAndProtocolFormIdandStatus(
			Committee committee, long protocolFormId, ProtocolFormCommitteeStatusEnum protocolFormCommitteeStatusEnum) {
		String query = "SELECT fcs FROM ProtocolFormCommitteeStatus fcs, ProtocolForm pf "
				+ " WHERE fcs.protocolForm.parent.id = pf.parent.id "
				+ " AND pf.id = :protocolFormId AND fcs.committee = :committee"
				+ " AND fcs.retired = :retired AND pf.retired = :retired AND fcs.protocolFormCommitteeStatus = :protocolFormCommitteeStatusEnum AND fcs.protocolForm.retired = :retired AND pf.parent.retired = :retired ORDER BY fcs.modified ASC";

		TypedQuery<ProtocolFormCommitteeStatus> q = getEntityManager()
				.createQuery(query, ProtocolFormCommitteeStatus.class);

		q.setHint("org.hibernate.cacheable", true);
		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("committee", committee);
		q.setParameter("protocolFormId", protocolFormId);
		q.setParameter("protocolFormCommitteeStatusEnum", protocolFormCommitteeStatusEnum);
		
		return q.getResultList();

	}
	
	
	@Transactional(readOnly = true)
	public List<ProtocolFormCommitteeStatus> listAllOfCurrentFormByCommitteeAndProtocolFormId(
			Committee committee, long protocolFormId) {
		String query = "SELECT fcs FROM ProtocolFormCommitteeStatus fcs, ProtocolForm pf "
				+ " WHERE fcs.protocolForm.id = pf.id "
				+ " AND pf.id = :protocolFormId AND fcs.committee = :committee"
				+ " AND fcs.retired = :retired AND pf.retired = :retired AND fcs.protocolForm.retired = :retired AND pf.retired = :retired ORDER BY fcs.modified ASC";

		TypedQuery<ProtocolFormCommitteeStatus> q = getEntityManager()
				.createQuery(query, ProtocolFormCommitteeStatus.class);

		q.setHint("org.hibernate.cacheable", true);
		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("committee", committee);
		q.setParameter("protocolFormId", protocolFormId);

		return q.getResultList();

	}

	@Transactional(readOnly = true)
	public ProtocolFormCommitteeStatus getLatestByCommitteeAndProtocolFormId(
			Committee committee, long protocolFormId) {
		String query = "SELECT fcs FROM ProtocolFormCommitteeStatus fcs "
				+ " WHERE fcs.protocolForm.id IN (SELECT pf.id FROM ProtocolForm pf WHERE pf.parent.id IN (SELECT p.parent.id FROM ProtocolForm p WHERE p.id = :protocolFormId AND p.retired = :retired) AND pf.retired = :retired) AND fcs.retired = :retired "
				+ " AND fcs.committee = :committee ORDER BY fcs.id DESC";

		TypedQuery<ProtocolFormCommitteeStatus> q = getEntityManager()
				.createQuery(query, ProtocolFormCommitteeStatus.class);

		q.setHint("org.hibernate.cacheable", true);
		q.setFirstResult(0);
		q.setMaxResults(1);

		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("protocolFormId", protocolFormId);
		q.setParameter("committee", committee);

		ProtocolFormCommitteeStatus o = null;
		try {
			o = q.getSingleResult();
		} catch (Exception ex) {
		}
		return o;

	}

	/**
	 * the goal is to list all protocolforms that is In Review by a certain
	 * committee for example, list all latest protocolformcommitteestatus
	 * IRB_OFFICE, that is IN_REVIEW GROUP BY ppfcs.protocolForm.parent.id is
	 * because not only the ProtocolFormCommitteeStatus is versioned, the
	 * ProtocolForm is versioned as well..
	 * 
	 * @param committee
	 * @param protocolFormCommitteeStatus
	 * @return
	 */
	@Transactional(readOnly = true)
	public List<ProtocolFormCommitteeStatus> listByCommitteeAndStatus(
			Committee committee,
			ProtocolFormCommitteeStatusEnum protocolFormCommitteeStatus) {

		String query = "SELECT pfcs FROM ProtocolFormCommitteeStatus pfcs "
				+ " WHERE pfcs.protocolFormCommitteeStatus = :protocolFormCommitteeStatus AND pfcs.committee = :committee AND pfcs.id IN ("
				+ " SELECT MAX(ppfcs.id) FROM ProtocolFormCommitteeStatus ppfcs "
				+ " WHERE ppfcs.retired = :retired AND ppfcs.committee = :committee"
				+ " GROUP BY ppfcs.protocolForm.parent.id)";

		TypedQuery<ProtocolFormCommitteeStatus> q = getEntityManager()
				.createQuery(query, ProtocolFormCommitteeStatus.class);

		q.setHint("org.hibernate.cacheable", true);
		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("committee", committee);
		q.setParameter("protocolFormCommitteeStatus",
				protocolFormCommitteeStatus);

		return q.getResultList();
	}

	/**
	 * @TODO restrict to pfcs.protocolForm.protocolFormStatus =
	 *       :protocolFormStatus AND similar to listByCommitteeAndStatus,
	 *       however, the protocolFormStatus is restricted, and it's queriying
	 *       multiple protocolformcomitteestatus at a time
	 * 
	 * @param committee
	 * @param protocolFormStatus
	 * @param protocolFormCommitteeStatuses
	 * @param showHistory
	 * @return List<ProtocolFormCommitteeStatus>
	 */
	@Transactional(readOnly = true)
	public List<ProtocolFormCommitteeStatus> listByCommitteeAndStatuses(
			Committee committee,
			ProtocolFormStatusEnum protocolFormStatus,
			List<ProtocolFormCommitteeStatusEnum> protocolFormCommitteeStatuses,
			boolean showHistory) {

		String query = "SELECT pfcs FROM ProtocolFormCommitteeStatus pfcs, ProtocolFormStatus pfs "
				+ " WHERE pfcs.protocolForm.id = pfs.protocolForm.id "
				+ (protocolFormStatus.equals(ProtocolFormStatusEnum.ANY) ? ""
						: " AND pfs.protocolFormStatus = :protocolFormStatus")
				+ " AND pfs.id = (SELECT MAX(fs.id) FROM ProtocolFormStatus fs WHERE fs.protocolForm.id = pfcs.protocolForm.id AND fs.retired = :retired) "
				+ (showHistory ? ""
						: "AND pfcs.protocolFormCommitteeStatus IN :protocolFormCommitteeStatuses ")
				+ " AND pfcs.committee = :committee AND pfcs.id IN ("
				+ " SELECT MAX(ppfcs.id) FROM ProtocolFormCommitteeStatus ppfcs "
				+ " WHERE ppfcs.retired = :retired AND ppfcs.committee = :committee"
				+ " GROUP BY ppfcs.protocolForm.parent.id)";

		TypedQuery<ProtocolFormCommitteeStatus> q = getEntityManager()
				.createQuery(query, ProtocolFormCommitteeStatus.class);

		q.setHint("org.hibernate.cacheable", true);
		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("committee", committee);
		if (!protocolFormStatus.equals(ProtocolFormStatusEnum.ANY)) {
			q.setParameter("protocolFormStatus", protocolFormStatus);
		}
		if (!showHistory) {
			q.setParameter("protocolFormCommitteeStatuses",
					protocolFormCommitteeStatuses);
		}
		return q.getResultList();
	}
	
	@Transactional(readOnly = true)
	public List<ProtocolFormCommitteeStatus> listByCommitteeAndFormTypeAndStatuses(
			Committee committee,
			ProtocolFormStatusEnum protocolFormStatus,
			Set<ProtocolFormCommitteeStatusEnum> protocolFormCommitteeStatuses,
			ProtocolFormType protocolFormtype,
			boolean showHistory) {
		
		//default completed item period is one month
		String completeItemPeriod = "DATEADD(MONTH, -1, GETDATE())";
		
		if (committee.equals(Committee.BUDGET_REVIEW) || committee.equals(Committee.COVERAGE_REVIEW) || committee.equals(Committee.PRECOVERAGE_REVIEW)) {
			completeItemPeriod = "DATEADD(MONTH, -3, GETDATE())";
		}

		String query = "SELECT pfcs FROM ProtocolFormCommitteeStatus pfcs, ProtocolFormStatus pfs, ProtocolForm pf "
				+ " WHERE pfcs.protocolForm.parent.id = pfs.protocolForm.parent.id "
				+ " AND pfs.retired = :retired "
				+ " AND pf.retired = :retired "
				+ (protocolFormStatus.equals(ProtocolFormStatusEnum.ANY) ? " AND pfs.protocolFormStatus <> 'CANCELLED'"
						: " AND pfs.protocolFormStatus = :protocolFormStatus")
				+ " AND pfs.id = (SELECT MAX(fs.id) FROM ProtocolFormStatus fs WHERE fs.protocolForm.parent.id = pfcs.protocolForm.parent.id AND fs.retired = :retired) "
				+ (showHistory ? ""
						: "AND pfcs.protocolFormCommitteeStatus IN :protocolFormCommitteeStatuses ")
				+ " AND pfcs.committee = :committee AND pfcs.id IN ("
				+ " SELECT MAX(ppfcs.id) FROM ProtocolFormCommitteeStatus ppfcs "
				+ " WHERE ppfcs.retired = :retired AND ppfcs.committee = :committee "
				+ " GROUP BY ppfcs.protocolForm.parent.id)"
				+ " AND pfcs.protocolForm.id = pf.id "
				+ " AND pf.protocolFormType = :protocolFormtype "
				+ (showHistory ? "AND pfcs.modified > "+ completeItemPeriod +"" : "");

		TypedQuery<ProtocolFormCommitteeStatus> q = getEntityManager()
				.createQuery(query, ProtocolFormCommitteeStatus.class);

		q.setHint("org.hibernate.cacheable", true);
		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("committee", committee);
		q.setParameter("protocolFormtype", protocolFormtype);
		if (!protocolFormStatus.equals(ProtocolFormStatusEnum.ANY)) {
			q.setParameter("protocolFormStatus", protocolFormStatus);
		}
		if (!showHistory) {
			q.setParameter("protocolFormCommitteeStatuses",
					protocolFormCommitteeStatuses);
		}
		return q.getResultList();
	}

	/**
	 * by default showHistory is false
	 * 
	 * @param committee
	 * @param protocolFormStatus
	 * @param protocolFormCommitteeStatuses
	 * @return
	 */
	public List<ProtocolFormCommitteeStatus> listByCommitteeAndStatuses(
			Committee committee, ProtocolFormStatusEnum protocolFormStatus,
			List<ProtocolFormCommitteeStatusEnum> protocolFormCommitteeStatuses) {
		return listByCommitteeAndStatuses(committee, protocolFormStatus,
				protocolFormCommitteeStatuses, false);
	}


	/**
	 * the goal is to list all protocolforms that is In Review by the specified
	 * IRB Reviewer as assigned review (agenda_item_irb_reviewer)
	 * 
	 * @param committee
	 * @param protocolFormCommitteeStatus
	 * @return
	 */
	@Transactional(readOnly = true)
	public List<ProtocolFormCommitteeStatus> listByAgendaItemIRBReviewerAndProtocolFormCommitteeStatuses(
			IRBReviewer irbReviewer,
			List<ProtocolFormCommitteeStatusEnum> protocolFormCommitteeStatuses) {

		String query = "SELECT pfcs FROM ProtocolFormCommitteeStatus pfcs, AgendaItemReviewer air "
				+ " WHERE air.agendaItem.protocolForm.id = pfcs.protocolForm.id AND air.agendaItem.retired = :retired AND air.agendaItem.protocolForm.retired = :retired AND air.retired = :retired AND air.irbReviewer = :irbReviewer AND pfcs.protocolFormCommitteeStatus IN (:protocolFormCommitteeStatuses) AND pfcs.committee = :committee AND pfcs.id IN ("
				+ " SELECT MAX(ppfcs.id) FROM ProtocolFormCommitteeStatus ppfcs "
				+ " WHERE ppfcs.retired = :retired AND ppfcs.committee = :committee"
				+ " GROUP BY ppfcs.protocolForm.parent.id)";

		TypedQuery<ProtocolFormCommitteeStatus> q = getEntityManager()
				.createQuery(query, ProtocolFormCommitteeStatus.class);

		q.setHint("org.hibernate.cacheable", true);
		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("committee", Committee.IRB_REVIEWER);
		q.setParameter("irbReviewer", irbReviewer);
		q.setParameter("protocolFormCommitteeStatuses",
				protocolFormCommitteeStatuses);

		return q.getResultList();
	}

}
