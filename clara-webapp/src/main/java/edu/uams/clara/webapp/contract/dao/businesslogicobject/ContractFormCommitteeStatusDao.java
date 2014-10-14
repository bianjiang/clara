package edu.uams.clara.webapp.contract.dao.businesslogicobject;

import java.util.List;
import java.util.Map;

import javax.persistence.TypedQuery;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.jpa.HibernateEntityManager;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;

import edu.uams.clara.core.dao.AbstractDomainDao;
import edu.uams.clara.webapp.common.domain.usercontext.enums.Committee;
import edu.uams.clara.webapp.contract.domain.businesslogicobject.ContractFormCommitteeStatus;
import edu.uams.clara.webapp.contract.domain.businesslogicobject.enums.ContractFormCommitteeStatusEnum;
import edu.uams.clara.webapp.contract.domain.businesslogicobject.enums.ContractFormStatusEnum;
import edu.uams.clara.webapp.contract.domain.contractform.enums.ContractFormType;

@Repository
public class ContractFormCommitteeStatusDao extends AbstractDomainDao<ContractFormCommitteeStatus> {

	private static final long serialVersionUID = 2397010827587080822L;
	
	@Transactional(readOnly = true)
	public List<ContractFormCommitteeStatus> listLatestByContractFormId(long contractFormId){
		String query = "SELECT fcs FROM ContractFormCommitteeStatus fcs "
			+ " WHERE fcs.id IN ("
			+ " SELECT MAX(mfcs.id) FROM ContractFormCommitteeStatus mfcs, ContractForm pf "
			+ " WHERE pf.id = :contractFormId AND mfcs.contractForm.parent.id = pf.parent.id AND mfcs.retired = :retired "
			+ " GROUP BY mfcs.committee)";


		TypedQuery<ContractFormCommitteeStatus> q = getEntityManager().createQuery(query,
				ContractFormCommitteeStatus.class);

		q.setHint("org.hibernate.cacheable", true);
		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("contractFormId", contractFormId);
	
		return q.getResultList();
		
	}
	
	@Transactional(readOnly = true)
	public List<ContractFormCommitteeStatus> listAllByCommitteeAndContractFormId(Committee committee, long contractFormId){
		String query = "SELECT fcs FROM ContractFormCommitteeStatus fcs, ContractForm pf "
			+ " WHERE fcs.contractForm.parent.id = pf.parent.id "
			+ " AND pf.id = :contractFormId AND fcs.committee = :committee"
			+ " AND fcs.retired = :retired AND pf.retired = :retired AND fcs.contractForm.retired = :retired AND pf.parent.retired = :retired order by fcs.modified ASC";

		TypedQuery<ContractFormCommitteeStatus> q = getEntityManager().createQuery(query,
				ContractFormCommitteeStatus.class);

		q.setHint("org.hibernate.cacheable", true);
		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("committee", committee);
		q.setParameter("contractFormId", contractFormId);
	
		return q.getResultList();
		
	}
	
	@Transactional(readOnly = true)
	public ContractFormCommitteeStatus getLatestByCommitteeAndContractFormId(Committee committee, long contractFormId){
		String query = "SELECT fcs FROM ContractFormCommitteeStatus fcs "
			+ " WHERE fcs.contractForm.id = :contractFormId AND fcs.retired = :retired "
			+ " AND fcs.committee = :committee ORDER BY fcs.id DESC";


		TypedQuery<ContractFormCommitteeStatus> q = getEntityManager().createQuery(query,
				ContractFormCommitteeStatus.class);

		q.setHint("org.hibernate.cacheable", true);
		q.setFirstResult(0);
		q.setMaxResults(1);
		
		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("contractFormId", contractFormId);
		q.setParameter("committee", committee);
		
		ContractFormCommitteeStatus o = null;
		try {
			o = q.getSingleResult();
		} catch (Exception ex) {
		}
		return o;
		
	}
	
	/**
	 * the goal is to list all contractforms that is In Review by a certain committee
	 * for example, list all latest contractformcommitteestatus IRB_OFFICE, that is IN_REVIEW
	 * GROUP BY ppfcs.contractForm.parent.id is because not only the ContractFormCommitteeStatus is versioned, the ContractForm is versioned as well..
	 * @param committee
	 * @param contractFormCommitteeStatus
	 * @return
	 */
	@Transactional(readOnly = true)
	public List<ContractFormCommitteeStatus> listByCommitteeAndStatus(Committee committee, ContractFormCommitteeStatusEnum contractFormCommitteeStatus){ 
		
		String query = "SELECT pfcs FROM ContractFormCommitteeStatus pfcs "
			+ " WHERE pfcs.contractFormCommitteeStatus = :contractFormCommitteeStatus AND pfcs.committee = :committee AND pfcs.id IN ("
			+ " SELECT MAX(ppfcs.id) FROM ContractFormCommitteeStatus ppfcs "
			+ " WHERE ppfcs.retired = :retired AND ppfcs.committee = :committee"
			+ " GROUP BY ppfcs.contractForm.parent.id)";
		
		TypedQuery<ContractFormCommitteeStatus> q = getEntityManager().createQuery(query,
				ContractFormCommitteeStatus.class);

		
		q.setHint("org.hibernate.cacheable", true);
		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("committee", committee);
		q.setParameter("contractFormCommitteeStatus", contractFormCommitteeStatus);
	
		return q.getResultList();
	}
	
	/**
	 * @TODO restrict to pfcs.contractForm.contractFormStatus = :contractFormStatus AND
	 * similar to listByCommitteeAndStatus, however, the contractFormStatus is restricted, and it's queriying multiple contractformcomitteestatus at a time
	 * 
	 * @param committee
	 * @param contractFormStatus
	 * @param contractFormCommitteeStatuses
	 * @return List<ContractFormCommitteeStatus>
	 */
	@Transactional(readOnly = true)
	public List<ContractFormCommitteeStatus> listByCommitteeAndStatuses(Committee committee, ContractFormStatusEnum contractFormStatus, List<ContractFormCommitteeStatusEnum> contractFormCommitteeStatuses){ 
		
		String query = "SELECT pfcs FROM ContractFormCommitteeStatus pfcs, ContractFormStatus pfs "
			+ " WHERE pfcs.contractForm.id = pfs.contractForm.id "
			+ (contractFormStatus.equals(ContractFormStatusEnum.ANY) ?"":" AND pfs.contractFormStatus = :contractFormStatus")
			+ " AND pfs.id = (SELECT MAX(fs.id) FROM ContractFormStatus fs WHERE fs.contractForm.id = pfcs.contractForm.id AND fs.retired = :retired) AND pfcs.contractFormCommitteeStatus IN :contractFormCommitteeStatuses AND pfcs.committee = :committee AND pfcs.id IN ("
			+ " SELECT MAX(ppfcs.id) FROM ContractFormCommitteeStatus ppfcs "
			+ " WHERE ppfcs.retired = :retired AND ppfcs.committee = :committee"
			+ " GROUP BY ppfcs.contractForm.parent.id)";
		
		TypedQuery<ContractFormCommitteeStatus> q = getEntityManager().createQuery(query,
				ContractFormCommitteeStatus.class);

		
		q.setHint("org.hibernate.cacheable", true);
		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("committee", committee);
		if (!contractFormStatus.equals(ContractFormStatusEnum.ANY)){
		q.setParameter("contractFormStatus", contractFormStatus);
		}
		q.setParameter("contractFormCommitteeStatuses", contractFormCommitteeStatuses);
	
		return q.getResultList();
	}
	
	@Transactional(readOnly = true)
	public List<ContractFormCommitteeStatus> listByCommitteeAndFormTypeAndStatuses(
			Committee committee,
			ContractFormStatusEnum contractFormStatus,
			List<ContractFormCommitteeStatusEnum> contractFormCommitteeStatuses,
			ContractFormType contractFormtype,
			boolean showHistory) {
		
		String query = "SELECT pfcs FROM ContractFormCommitteeStatus pfcs, ContractFormStatus pfs, ContractForm pf "
				+ " WHERE pfcs.contractForm.id = pfs.contractForm.id "
				+ " AND pf.retired = :retired "
				+ " AND pfcs.retired = :retired "
				+ (contractFormStatus.equals(ContractFormStatusEnum.ANY) ? ""
						: " AND pfs.contractFormStatus = :contractFormStatus")
				+ " AND pfs.id = (SELECT MAX(fs.id) FROM ContractFormStatus fs WHERE fs.contractForm.id = pfcs.contractForm.id AND fs.retired = :retired) "
				+ (showHistory ? ""
						: "AND pfcs.contractFormCommitteeStatus IN :contractFormCommitteeStatuses ")
				+ " AND pfcs.committee = :committee AND pfcs.id IN ("
				+ " SELECT MAX(ppfcs.id) FROM ContractFormCommitteeStatus ppfcs "
				+ " WHERE ppfcs.retired = :retired AND ppfcs.committee = :committee"
				+ " GROUP BY ppfcs.contractForm.parent.id)"
				+ " AND pfcs.contractForm.id = pf.id "
				+ " AND pf.contractFormType = :contractFormtype "
				+ (showHistory ? "AND pfcs.modified > DATEADD(MONTH, -1, GETDATE())" : "");

		TypedQuery<ContractFormCommitteeStatus> q = getEntityManager()
				.createQuery(query, ContractFormCommitteeStatus.class);

		q.setHint("org.hibernate.cacheable", true);
		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("committee", committee);
		q.setParameter("contractFormtype", contractFormtype);
		if (!contractFormStatus.equals(ContractFormStatusEnum.ANY)) {
			q.setParameter("contractFormStatus", contractFormStatus);
		}
		if (!showHistory) {
			q.setParameter("contractFormCommitteeStatuses",
					contractFormCommitteeStatuses);
		}
		return q.getResultList();
	}
	
	@Transactional(readOnly = true)
	public List<String> getCommitteeNotesByContractFormId(long contractFormId) {
		String query = "SELECT DISTINCT('(' + CONVERT(VARCHAR(20), modified, 100) + ')' + ' Notes: ' + note) AS committeeNote FROM contract_form_committee_status "
					+ " WHERE contract_form_id = :contractFormId AND retired = :retired AND note IS NOT NULL AND note <> ''";
		
		HibernateEntityManager hem = getEntityManager().unwrap(HibernateEntityManager.class);
		
		Session session = hem.getSession();
		
		org.hibernate.Query q = session.createSQLQuery(query);
		q.setResultTransformer(Criteria.ALIAS_TO_ENTITY_MAP);
		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("contractFormId", contractFormId);
		
		List resultObjectLst = q.list();
		
		List<String> committeeNotesLst = Lists.newArrayList();
		
		if (resultObjectLst != null && !resultObjectLst.isEmpty()) {
			for (Object resultObject: resultObjectLst) {
				Map row = (Map) resultObject;
				
				committeeNotesLst.add(row.get("committeeNote").toString());
			}
		}
		
		return committeeNotesLst;
	}

}
