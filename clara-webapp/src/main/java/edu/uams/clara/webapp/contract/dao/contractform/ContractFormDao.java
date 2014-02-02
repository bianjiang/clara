package edu.uams.clara.webapp.contract.dao.contractform;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import edu.uams.clara.core.dao.AbstractDomainDao;
import edu.uams.clara.webapp.common.domain.usercontext.User;
import edu.uams.clara.webapp.common.domain.usercontext.enums.Permission;
import edu.uams.clara.webapp.common.objectwrapper.PagedList;
import edu.uams.clara.webapp.contract.domain.Contract;
import edu.uams.clara.webapp.contract.domain.businesslogicobject.ContractFormStatus;
import edu.uams.clara.webapp.contract.domain.businesslogicobject.enums.ContractFormStatusEnum;
import edu.uams.clara.webapp.contract.domain.contractform.ContractForm;
import edu.uams.clara.webapp.contract.domain.contractform.enums.ContractFormType;
import edu.uams.clara.webapp.contract.objectwrapper.ContractSearchCriteria;

@Repository
public class ContractFormDao extends AbstractDomainDao<ContractForm> {
	private final static Logger logger = LoggerFactory
			.getLogger(ContractFormDao.class);

	private static final long serialVersionUID = 7492131024720753963L;

	@Transactional(readOnly = true)
	public ContractFormStatus getLatestContractFormStatusByContractFormId(long contractFormId){
		String query = "SELECT pfs FROM ContractFormStatus pfs "
			+ " WHERE pfs.contract.id = :contractFormId AND pfs.retired = :retired ORDER BY pfs.id DESC";

		TypedQuery<ContractFormStatus> q = getEntityManager().createQuery(query,
				ContractFormStatus.class);
		q.setFirstResult(0);
		q.setMaxResults(1);
		q.setHint("org.hibernate.cacheable", true);
		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("contractFormId", contractFormId);
	
		return q.getSingleResult();
	}
	
	@Transactional(readOnly = true)
	public List<ContractForm> listContractFormsByContractIdAndContractFormType(
			long contractId, ContractFormType contractFormType) {

		String query = "SELECT cf FROM ContractForm cf "
			+ " WHERE cf.contract.id = :contractId AND cf.contractFormType = :contractFormType AND cf.retired = :retired";

		TypedQuery<ContractForm> q = getEntityManager()
				.createQuery(query, ContractForm.class);

		q.setHint("org.hibernate.cacheable", true);

		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("contractId", contractId);
		q.setParameter("contractFormType", contractFormType);
		return q.getResultList();
	}
	
	@Transactional(readOnly = true)
	public List<ContractForm> listContractFormsByContractId(
			long contractId) {

		String query = "SELECT cf FROM ContractForm cf "
			+ " WHERE cf.contract.id = :contractId AND cf.retired = :retired";

		TypedQuery<ContractForm> q = getEntityManager()
				.createQuery(query, ContractForm.class);

		q.setHint("org.hibernate.cacheable", true);

		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("contractId", contractId);
		return q.getResultList();
	}
	
	@Transactional(readOnly = true)
	public ContractForm getContractFormByContractIdAndContractFormType(long contractId, ContractFormType contractFormType){
		String query = "SELECT cf FROM ContractForm cf "
			+ " WHERE cf.contract.id = :contractId AND cf.contractFormType = :contractFormType AND cf.retired = :retired";

		TypedQuery<ContractForm> q = getEntityManager().createQuery(query,
				ContractForm.class);
		q.setFirstResult(0);
		q.setMaxResults(1);
		q.setHint("org.hibernate.cacheable", true);
		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("contractId", contractId);
		q.setParameter("contractFormType", contractFormType);
	
		return q.getSingleResult();
	}
	
	@Transactional(readOnly = true)
	public ContractForm getLatestContractFormByContractIdAndContractFormType(long contractId, ContractFormType contractFormType){
		String query = "SELECT cf FROM ContractForm cf "
			+ " WHERE cf.contract.id = :contractId AND cf.contractFormType = :contractFormType AND cf.retired = :retired "
			+ " ORDER BY cf.id DESC ";

		TypedQuery<ContractForm> q = getEntityManager().createQuery(query,
				ContractForm.class);
		q.setFirstResult(0);
		q.setMaxResults(1);
		q.setHint("org.hibernate.cacheable", true);
		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("contractId", contractId);
		q.setParameter("contractFormType", contractFormType);
	
		return q.getSingleResult();
	}
	
	@Transactional(readOnly = true)
	private List<String> contractSearchCriteriaResolver(List<ContractSearchCriteria> searchCriteria){
		List<String> xPathCriteria = new ArrayList<String>();
		
		for (ContractSearchCriteria p:searchCriteria){
			switch (p.getSearchField()){
			case IDENTIFIER:{
				switch (p.getSearchOperator()){
				case CONTAINS:
					xPathCriteria.add("meta_data_xml.exist('/contract[fn:contains(@identifier,\""+p.getKeyword().toUpperCase()+"\")]') = 1");
					//xPathCriteria.add("meta_data_xml.exist('/protocol[@identifier[fn:contains(fn:upper-case(.), \"" + p.getKeyword().toUpperCase() + "\")]]') = 1");
					break;				
				case EQUALS:
					xPathCriteria.add("meta_data_xml.exist('/contract[@identifier[. = \"" + p.getKeyword() + "\"]]') = 1");
					break;
				case DOES_NOT_CONTAIN:
					xPathCriteria.add("meta_data_xml.exist('/contract[@identifier[fn:contains(fn:upper-case(.), \"" + p.getKeyword().toUpperCase() + "\")]]') = 0");
				default:
					break;
				}
			}
			break;
			case TITLE:{
				switch (p.getSearchOperator()){
				case CONTAINS:
					xPathCriteria.add("CONTAINS(meta_data_xml, '"+ p.getKeyword() +"') AND meta_data_xml.exist('/contract/title/text()[fn:contains(fn:upper-case(.), \"" + p.getKeyword().toUpperCase() + "\")]') = 1");
					break;				
				case EQUALS:
					xPathCriteria.add("CONTAINS(meta_data_xml, '"+ p.getKeyword() +"') AND meta_data_xml.exist('/contract/title[. = \"" + p.getKeyword() + "\"]') = 1");
					break;
				case DOES_NOT_CONTAIN:
					xPathCriteria.add("meta_data_xml.exist('/contract/title/text()[fn:contains(fn:upper-case(.), \"" + p.getKeyword().toUpperCase() + "\")]') = 0");
				default:
					break;
				}
			}
			break;
			case PI_USERID:{
				switch (p.getSearchOperator()){
				case CONTAINS:
					xPathCriteria
					.add("meta_data_xml.exist('/contract/staffs/staff/user[roles/role=\"Principal Investigator\" and @id = \""
							+ p.getKeyword() + "\"]') = 1");
					break;				
				case EQUALS:
					xPathCriteria
					.add("meta_data_xml.exist('/contract/staffs/staff/user[roles/role=\"Principal Investigator\" and @id = \""
							+ p.getKeyword() + "\"]') = 1");
					break;
				case DOES_NOT_CONTAIN:
					xPathCriteria
					.add("meta_data_xml.exist('/contract/staffs/staff/user[roles/role=\"Principal Investigator\" and @id = \""
							+ p.getKeyword() + "\"]') = 0");
				default:
					break;
				}
			}
			break;
			case ASSIGNED_REVIEWER_USERID:{
				switch (p.getSearchOperator()){
				case CONTAINS:
					xPathCriteria
					.add("meta_data_xml.exist('/contract/committee-review/committee/assigned-reviewers/assigned-reviewer[@user-id = \""
							+ p.getKeyword() + "\"]') = 1");
					break;				
				case EQUALS:
					xPathCriteria
					.add("meta_data_xml.exist('/contract/committee-review/committee/assigned-reviewers/assigned-reviewer[@user-id = \""
							+ p.getKeyword() + "\"]') = 1");
					break;
				case DOES_NOT_CONTAIN:
					xPathCriteria
					.add("meta_data_xml.exist('/contract/committee-review/committee/assigned-reviewers/assigned-reviewer[@user-id = \""
							+ p.getKeyword() + "\"]') = 0");
				default:
					break;
				}
			}
			break;
			case PI_NAME:{
				switch (p.getSearchOperator()){
				case CONTAINS:
					xPathCriteria.add("meta_data_xml.exist('/contract/staffs/staff/user[roles/role=\"Principal Investigator\" and (lastname[fn:contains(fn:upper-case(.), \"" + p.getKeyword().toUpperCase() + "\")] or firstname[fn:contains(fn:upper-case(.), \"" + p.getKeyword().toUpperCase() + "\")])]') = 1");
					break;				
				case EQUALS:
					xPathCriteria.add("meta_data_xml.exist('/contract/staffs/staff/user[roles/role=\"Principal Investigator\" and (lastname[fn:contains(fn:upper-case(.), \"" + p.getKeyword().toUpperCase() + "\")] or firstname[fn:contains(fn:upper-case(.), \"" + p.getKeyword().toUpperCase() + "\")])]') = 1");
					break;
				case DOES_NOT_CONTAIN:
					xPathCriteria.add("meta_data_xml.exist('/contract/staffs/staff/user[roles/role=\"Principal Investigator\" and (lastname[fn:contains(fn:upper-case(.), \"" + p.getKeyword().toUpperCase() + "\")] or firstname[fn:contains(fn:upper-case(.), \"" + p.getKeyword().toUpperCase() + "\")])]') = 0");
				default:
					break;
				}
			}
			break;
			case CONTRACT_STATUS:{
				String status = ContractFormStatusEnum.valueOf(p.getKeyword()).getDescription();
				switch (p.getSearchOperator()){
				case CONTAINS:
					xPathCriteria
					.add("meta_data_xml.exist('/contract/status[. = \""+ status +"\"]')=1");
					break;				
				case EQUALS:
					xPathCriteria
					.add("meta_data_xml.exist('/contract/status[. = \""+ status +"\"]')=1");
					break;
				case DOES_NOT_CONTAIN:
					xPathCriteria
					.add("meta_data_xml.exist('/contract/status[. = \""+ status +"\"]') = 0");
				default:
					break;
				}
			}
			break;
			case STAFF_USERID:{
				switch (p.getSearchOperator()){
				case CONTAINS:
					xPathCriteria.add("meta_data_xml.exist('/contract/staffs/staff/user[@id = \""+ p.getKeyword() +"\"]') = 1");
					break;				
				case EQUALS:
					xPathCriteria.add("meta_data_xml.exist('/contract/staffs/staff/user[@id = \""+ p.getKeyword() +"\"]') = 1");
					break;
				case DOES_NOT_CONTAIN:
					xPathCriteria.add("meta_data_xml.exist('/contract/staffs/staff/user[@id = \""+ p.getKeyword() +"\"]') = 0");
				default:
					break;
				}
			}
			break;	
			case CONTRACT_TYPE:{
				switch (p.getSearchOperator()){
				case CONTAINS:
					xPathCriteria.add("CONTAINS(meta_data_xml, '"+ p.getKeyword() +"') AND meta_data_xml.exist('/contract/type/text()[fn:contains(fn:upper-case(.), \"" + p.getKeyword().toUpperCase() + "\")]') = 1");
					break;				
				case EQUALS:
					xPathCriteria.add("CONTAINS(meta_data_xml, '"+ p.getKeyword() +"') AND meta_data_xml.exist('/contract/type/text()[. = \"" + p.getKeyword() + "\"]') = 1");
					break;
				case DOES_NOT_CONTAIN:
					xPathCriteria.add("meta_data_xml.exist('/contract/type/text()[fn:contains(fn:upper-case(.), \"" + p.getKeyword().toUpperCase() + "\")]') = 0");
				default:
					break;
				}
			}
			break;
			case ENTITY_NAME:{
				switch (p.getSearchOperator()){
				case CONTAINS:
					xPathCriteria.add("CONTAINS(meta_data_xml, '"+ p.getKeyword() +"') AND meta_data_xml.exist('/contract/sponsors/sponsor/name[fn:contains(fn:upper-case(.), \"" + p.getKeyword().toUpperCase() + "\") or /contract/sponsors/sponsor/company[fn:contains(fn:upper-case(.), \"" + p.getKeyword().toUpperCase() + "\")]]') = 1");
					break;				
				case EQUALS:
					xPathCriteria.add("CONTAINS(meta_data_xml, '"+ p.getKeyword() +"') AND meta_data_xml.exist('/contract/sponsors/sponsor/name[fn:contains(fn:upper-case(.), \"" + p.getKeyword().toUpperCase() + "\") or /contract/sponsors/sponsor/company[fn:contains(fn:upper-case(.), \"" + p.getKeyword().toUpperCase() + "\")]]') = 1");
					break;
				case DOES_NOT_CONTAIN:
					xPathCriteria.add("meta_data_xml.exist('/contract/sponsors/sponsor/name[fn:contains(fn:upper-case(.), \"" + p.getKeyword().toUpperCase() + "\") or /contract/sponsors/sponsor/company[fn:contains(fn:upper-case(.), \"" + p.getKeyword().toUpperCase() + "\")]]') = 0");
				default:
					break;
				}
			}
			break;
			case PROTOCOL_ID:{
				switch (p.getSearchOperator()){
				/*
				case CONTAINS:
					xPathCriteria.add("meta_data_xml.exist('/contract/protocol/text()[fn:contains(fn:upper-case(.), \"" + p.getKeyword().toUpperCase() + "\")]') = 1");
					break;				
				case EQUALS:
					xPathCriteria.add("meta_data_xml.exist('/contract/protocol[. = \"" + p.getKeyword() + "\"]') = 1");
					break;
				case DOES_NOT_CONTAIN:
					xPathCriteria.add("meta_data_xml.exist('/contract/protocol/text()[fn:contains(fn:upper-case(.), \"" + p.getKeyword().toUpperCase() + "\")]') = 0");
				*/
				case CONTAINS:
					xPathCriteria.add("contract_id IN (SELECT related_object_id FROM related_object WHERE related_object_type = 'contract' AND object_id = \""+ p.getKeyword() +"\" AND retired = 0)");
					break;
				case EQUALS:
					xPathCriteria.add("contract_id IN (SELECT related_object_id FROM related_object WHERE related_object_type = 'contract' AND object_id = \""+ p.getKeyword() +"\" AND retired = 0)");
					break;
				case DOES_NOT_CONTAIN:
					xPathCriteria.add("contract_id IN (SELECT related_object_id FROM related_object WHERE related_object_type = 'contract' AND object_id = \""+ p.getKeyword() +"\" AND retired = 0)");
					break;
				default:
					break;
				}
			}
			break;
			default:
				break;
			}
		}

		/*for (ContractSearchField field : fields) {
			switch (field) {
			case TITLE:
				xPathCriteria.add("meta_data.exist('/contract/title[fn:contains(., \"" + keyword + "\")]') = 1");
				break;
			case PI_NAME:
				xPathCriteria.add("meta_data.exist('/contract/staffs/staff/user/roles/role[.=\"Principal Investigator\" and (../../user/lastname[fn:contains(fn:upper-case(.), \"" + keyword.toUpperCase() + "\")] or ../../user/firstname[fn:contains(fn:upper-case(.), \"" + keyword.toUpperCase() + "\")])]') = 1");
				break;
			case PROTOCOL_STATUS:				
				break;
			case STAFF_NAME:
				xPathCriteria.add("meta_data.exist('/contract/staffs/staff/user/lastname[fn:contains(fn:upper-case(.), \"" + keyword.toUpperCase() + "\")]') = 1");
				xPathCriteria.add("meta_data.exist('/contract/staffs/staff/user/firstname[fn:contains(fn:upper-case(.), \"" + keyword.toUpperCase() + "\")]') = 1");
				break;
			default:
				break;
			}
		}*/

		return xPathCriteria;		
	}
	
	private static boolean isInteger(String s) {
	    try { 
	        Integer.parseInt(s); 
	    } catch(NumberFormatException e) { 
	        return false; 
	    }
	    // only got here if we didn't return false
	    return true;
	}
	
	@Transactional(readOnly = true)
	public PagedList<ContractForm> listPagedContractMetaDatasByUserAndSearchCriteriaAndContractStatusFilter(User user, int start, int limit, List<ContractSearchCriteria> searchCriterias, ContractFormStatusEnum filter, String quickSearchKeyword) {
		PagedList<ContractForm> pagedList = new PagedList<ContractForm>();
		pagedList.setStart(start);
		pagedList.setLimit(limit);
		
		String xpathWhereClause = "";
		
		if (quickSearchKeyword != null && !quickSearchKeyword.isEmpty()) {
			xpathWhereClause = "CONTAINS(meta_data_xml, '"+ quickSearchKeyword +"')";
			
			//@tickt #2869 
			//if quick search by protocol id, should search by both meta data and related object table
			if (isInteger(quickSearchKeyword)) {
				xpathWhereClause += " OR contract_id IN (SELECT related_object_id FROM related_object WHERE related_object_type = 'contract' AND object_id = "+ quickSearchKeyword +" AND retired = 0)";
			}
		} else {
			if(searchCriterias != null){
				List<String> xPathCriterias = contractSearchCriteriaResolver(searchCriterias);
				
				
				int c = xPathCriterias.size();
				
				int i = 0;
			
				for(String xc:xPathCriterias){
					xpathWhereClause += xc;
					
					if(i != c - 1){
						xpathWhereClause += " AND ";
					}
					
					i ++;
				}
			}
		}
		
		boolean viewAllContract = false;
		
		if (user.getAuthorities().contains(Permission.VIEW_ALL_CONTRACT)){
			viewAllContract = true;
		}
		
		String queryTotal = "";
		String query = "";
		
		if (viewAllContract){
			/*
			queryTotal = " SELECT COUNT(*) FROM contract c "
					+ (filter !=  null?", contract_status cs":"")
					+ " WHERE c.retired = :retired "
					+ (filter !=  null?" AND cs.id = (SELECT MAX(cs.id) FROM contract_status cs WHERE cs.contract_id = c.id AND cs.contract_status = :contractStatus) ":"")
					+ ((searchCriterias != null)?" AND  (" + xpathWhereClause + ") ": "");
			*/
			
			queryTotal = " SELECT COUNT(DISTINCT cf.contract_id) FROM contract_form cf WHERE cf.id IN (SELECT MAX(c.id) FROM contract_form c "
					+ (filter !=  null?", contract_form_status cs":"")
					+ " WHERE c.retired = :retired "
					+ (filter !=  null?" AND cs.id = (SELECT MAX(cs.id) FROM contract_form_status cs WHERE cs.contract_form_id = c.id AND cs.contract_form_status = :contractFormStatus) ":"")
					+ ((searchCriterias != null)?" AND  (" + xpathWhereClause + ") ": "")
					+ " GROUP BY c.parent_id)";
			
			query = " SELECT cf.* FROM contract_form cf WHERE cf.id IN (SELECT MAX(c.id) FROM contract_form c "
					+ (filter !=  null?", contract_form_status cs":"")
					+ " WHERE c.retired = :retired "
					+ (filter !=  null?" AND cs.id = (SELECT MAX(cs.id) FROM contract_form_status cs WHERE cs.contract_form_id = c.id AND cs.contract_form_status = :contractFormStatus) ":"")
					+ ((searchCriterias != null)?" AND  (" + xpathWhereClause + ") ": "")
					+ " GROUP BY c.parent_id) "
					+ " AND cf.contract_form_type = 'NEW_CONTRACT' ORDER BY cf.id DESC ";
		} else {
			/*
			queryTotal = " SELECT COUNT(*) FROM contract cl WHERE cl.id IN ("
					+ " SELECT DISTINCT c.id FROM contract c, securable_object_acl soa, securable_object so "
					+ (filter != null ? ", contract_status cs" : "")
					+ " WHERE soa.retired = :retired "
					+ " AND so.retired = :retired "
					+ " AND c.retired = :retired "
					+ " AND so.id = soa.securable_object_id "
					+ " AND soa.owner_class = :ownerClass AND soa.owner_id = :ownerId "
					// need to modify it if more permissions added in the future
					+ " AND (soa.permission = 'READ' OR soa.permission = 'ACCESS')"
					+ " AND so.object_class = :objectClass AND so.object_id = c.id "
					+ (filter != null ? " AND cs.id = (SELECT MAX(cs.id) FROM contract_status cs WHERE cs.contract_id = c.id AND cs.contract_status = :contractStatus) "
							: "")
					+ ((searchCriterias != null) ? " AND  (" + xpathWhereClause
							+ ") " : "")
					+ ")";
			*/
			
			queryTotal = " SELECT COUNT(DISTINCT cf.contract_id) FROM contract_form cf WHERE cf.id IN ("
					+ " SELECT DISTINCT c.id FROM contract_form c, securable_object_acl soa, securable_object so "
					+ (filter != null ? ", contract_form_status cfs" : "")
					+ " WHERE soa.retired = :retired "
					+ " AND so.retired = :retired "
					+ " AND c.retired = :retired "
					+ " AND so.id = soa.securable_object_id "
					+ " AND soa.owner_class = :ownerClass AND soa.owner_id = :ownerId "
					// need to modify it if more permissions added in the future
					+ " AND (soa.permission = 'READ' OR soa.permission = 'ACCESS') "
					+ " AND so.object_class = :objectClass AND so.object_id = c.contract_id "
					+ (filter != null ? " AND cfs.id = (SELECT MAX(cfs.id) FROM contract_form_status cfs WHERE cfs.contract_form_id = c.id AND cfs.contract_status = :contractStatus) "
							: "")
					+ ((searchCriterias != null) ? " AND  (" + xpathWhereClause
							+ ") " : "") + ")";
			
			query = " SELECT cf.* FROM contract_form cf WHERE cf.id IN ("
					+ " SELECT DISTINCT c.id FROM contract_form c, securable_object_acl soa, securable_object so "
					+ (filter != null ? ", contract_form_status cfs" : "")
					+ " WHERE soa.retired = :retired "
					+ " AND so.retired = :retired "
					+ " AND c.retired = :retired "
					+ " AND so.id = soa.securable_object_id "
					+ " AND soa.owner_class = :ownerClass AND soa.owner_id = :ownerId "
					// need to modify it if more permissions added in the future
					+ " AND (soa.permission = 'READ' OR soa.permission = 'ACCESS') "
					+ " AND so.object_class = :objectClass AND so.object_id = c.contract_id "
					+ (filter != null ? " AND cfs.id = (SELECT MAX(cfs.id) FROM contract_form_status cfs WHERE cfs.contract_form_id = c.id AND cfs.contract_status = :contractStatus) "
							: "")
					+ ((searchCriterias != null) ? " AND  (" + xpathWhereClause
							+ ") " : "") + ") AND cf.contract_form_type = 'NEW_CONTRACT' ORDER BY cf.id DESC";
		}

		Query tq = getEntityManager().createNativeQuery(queryTotal);
		//tq.setHint("org.hibernate.cacheable", true);
		tq.setParameter("retired", Boolean.FALSE);
		if (!viewAllContract) {
			tq.setParameter("ownerClass", User.class);
			tq.setParameter("ownerId", user.getId());
			tq.setParameter("objectClass", Contract.class);
		}
		if (filter != null) {
			tq.setParameter("contractStatus", filter);
		}

		long total = Long.valueOf(tq.getSingleResult().toString());
		pagedList.setTotal(total);

		TypedQuery<ContractForm> q = (TypedQuery<ContractForm>) getEntityManager().createNativeQuery(query,
				ContractForm.class);
		q.setFirstResult(start).setMaxResults(limit);

		//q.setHint("org.hibernate.cacheable", true);
		q.setParameter("retired", Boolean.FALSE);
		if (!viewAllContract) {
			q.setParameter("ownerClass", User.class);
			q.setParameter("ownerId", user.getId());
			q.setParameter("objectClass", Contract.class);
		}
		if (filter != null) {
			q.setParameter("contractStatus", filter);
		}
		
		//#2599 make sure Amendments are showing up correctly in the list, might need to find a better way later ...
		List<ContractForm> resultList = q.getResultList();
		Set<Contract> contractList = Sets.newHashSet();
		List<ContractForm> amendmentList = Lists.newArrayList();
		
		for (ContractForm cf : resultList) {
			contractList.add(cf.getContract());
		}
		
		for (Contract c : contractList) {
			List<ContractForm> aList = this.listContractFormsByContractIdAndContractFormType(c.getId(), ContractFormType.AMENDMENT);
			
			if (aList != null && !aList.isEmpty()) {
				amendmentList.addAll(aList);
			}
		}
		
		if (amendmentList != null && !amendmentList.isEmpty()) {
			resultList.addAll(amendmentList);
		}

		pagedList.setList(resultList);
		return pagedList;
	}
	
}
