package edu.uams.clara.integration.incoming.crimson.dao;

import java.math.BigInteger;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.springframework.transaction.annotation.Transactional;

public class CrimsonContractDao {
	private EntityManager em;

	@Transactional(readOnly = true)
	public List<Object[]> findAllContract() {
		// 0, num_contract_ID, 1 txt_contract_number, 2 num_user_ID, 3
		// date_created, 4 num_status,
		// 5 num_approvalstatus, 6 num_parent, 7 num_cancel_reason, 8
		// txt_cancel_reason, 9 num_prestatus
		String qryString = "SELECT num_contract_ID, txt_contract_number, num_user_ID, date_created, num_status, "
				+ " num_approvalstatus, num_parent, num_cancel_reason, txt_cancel_reason, num_prestatus "
				+ " FROM [HospSQL1_ClinicalResearch].[dbo].[contract] "
				+ " WHERE num_status = :status AND num_contract_ID in (SELECT MAX(num_contract_ID) FROM [HospSQL1_ClinicalResearch].[dbo].contract "
				+ " GROUP BY txt_contract_number)";
		Query query = em.createNativeQuery(qryString);
		query.setParameter("status", 1);
		try {
			List<Object[]> result = (List<Object[]>) query.getResultList();
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@Transactional(readOnly = true)
	public Object[] findContractByID(int contractID) {
		// 0, num_contract_ID, 1 txt_contract_number, 2 num_user_ID, 3
		// date_created, 4 num_status,
		// 5 num_approvalstatus, 6 num_parent, 7 num_cancel_reason, 8
		// txt_cancel_reason, 9 num_prestatus
		String qryString = "SELECT num_contract_ID, txt_contract_number, num_user_ID, date_created, num_status, "
				+ " num_approvalstatus, num_parent, num_cancel_reason, txt_cancel_reason, num_prestatus "
				+ " FROM [HospSQL1_ClinicalResearch].[dbo].[contract] "
				+ " WHERE num_status = :status AND num_contract_ID in (SELECT MAX(num_contract_ID) FROM [HospSQL1_ClinicalResearch].[dbo].contract "
				+ " GROUP BY txt_contract_number) AND num_contract_ID = :contractID ";
		Query query = em.createNativeQuery(qryString);
		query.setParameter("status", 1);
		query.setParameter("contractID", contractID);
		try {
			Object[] result = (Object[]) query.getSingleResult();
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@Transactional(readOnly = true)
	public Object[] findContractInfoByContractId(BigInteger contractID) {
		/*
		 * 0 num_contract_info_ID, 1 num_contract_ID, 2
		 * txt_contract_description, 3 num_contract_type_ID, 4 num_irb_ID, 5
		 * num_prn_number, 6 date_contract_start, 7 date_contract_stop, 8
		 * num_study_type_ID, 9 num_has_funding, 10 txt_no_funding_dept, 11
		 * txt_no_funding_fund, 12 txt_no_funding_cost_center, 13
		 * date_contract_exec, num_coop_ID
		 */
		String qryString = "SELECT num_contract_info_ID, num_contract_ID, CAST(txt_contract_description AS varchar(8000)) as txt_contract_description, num_contract_type_ID, num_irb_ID, num_prn_number, "
				+ " date_contract_start, date_contract_stop, num_study_type_ID, num_has_funding, txt_no_funding_dept, txt_no_funding_fund, "
				+ " txt_no_funding_cost_center, date_contract_exec, num_coop_ID"
				+ " FROM [HospSQL1_ClinicalResearch].[dbo].[contract_info] "
				+ " WHERE num_contract_ID = :contractID";
		Query query = em.createNativeQuery(qryString);
		query.setParameter("contractID", contractID);
		try {
			Object[] result = (Object[]) query.getSingleResult();
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@Transactional(readOnly = true)
	public String findContractTypebyID(int typeID) {
		String qryString = "SELECT ct.txt_contract_type FROM [HospSQL1_ClinicalResearch].[dbo].[contract_type] as ct"
				+ " WHERE ct.num_contract_type_ID =:typeID";
		Query query = em.createNativeQuery(qryString);
		query.setParameter("typeID", typeID);

		try {
			String result = (String) query.getSingleResult();
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}

	@Transactional(readOnly = true)
	public List<Object[]> findContactbyContractID(BigInteger contractID) {
		/*
		 * 0 txt_contact_party, 1 txt_institution, 2 txt_phone, 3 txt_fax, 4
		 * txt_email, 5 txt_address
		 */
		String qryString = "SELECT txt_contact_party, txt_institution, txt_phone, txt_fax, txt_email, txt_address "
				+ " FROM [HospSQL1_ClinicalResearch].[dbo].[contract_contact] "
				+ " WHERE num_contract_ID =:contractID";
		Query query = em.createNativeQuery(qryString);
		query.setParameter("contractID", contractID);

		try {
			List<Object[]> result = (List<Object[]>) query.getResultList();
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	// this function used for find staff id for CLARA
	@Transactional(readOnly = true)
	public List<Object[]> findMultipleSelectByContractID(BigInteger contractID) {
		/* 0 num_type, 1 txt_value--aria_userID */
		String qryString = "SELECT num_type, txt_value "
				+ " FROM [HospSQL1_ClinicalResearch].[dbo].[multipleselect]"
				+ " WHERE num_connection_ID =:contractID";
		Query query = em.createNativeQuery(qryString);
		query.setParameter("contractID", contractID);

		try {
			List<Object[]> result = (List<Object[]>) query.getResultList();
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	// this function used for find staff role for CLARA
	@Transactional(readOnly = true)
	public String findStaffRoleByType(int type) {
		String qryString = "SELECT txt_description "
				+ " FROM [HospSQL1_ClinicalResearch].[dbo].[multipleselect_lookup]"
				+ " WHERE num_type = :type";

		Query query = em.createNativeQuery(qryString);
		query.setParameter("type", type);

		try {
			String result = (String) query.getSingleResult();
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}

	

	@Transactional(readOnly = true)
	public String findStatusByID(int statusID) {
		String qryString = "SELECT txt_approvalstatus "
				+ " FROM [HospSQL1_ClinicalResearch].[dbo].[contract_approvalstatus] "
				+ " WHERE num_approvalstatus = :statusID";

		Query query = em.createNativeQuery(qryString);
		query.setParameter("statusID", statusID);

		try {
			String result = (String) query.getSingleResult();
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}

	@Transactional(readOnly = true)
	public List<Object[]> findReviewerInfoByContractID(BigInteger contractID) {
		// 0 num_user_ID, 1 num_userGroup
		String qryString = "SELECT num_user_ID, num_userGroup "
				+ " FROM [HospSQL1_ClinicalResearch].[dbo].[contract_reviewer_person] "
				+ " WHERE num_contract_ID = :contractID";

		Query query = em.createNativeQuery(qryString);
		query.setParameter("contractID", contractID);

		try {
			List<Object[]> result = (List<Object[]>) query.getResultList();
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@Transactional(readOnly = true)
	public String findReviewerGroupByID(int groupID) {
		String qryString = "SELECT txt_groups_name "
				+ " FROM [HospSQL1_ClinicalResearch].[dbo].[groups]"
				+ " WHERE num_groups_ID = :groupID";

		Query query = em.createNativeQuery(qryString);
		query.setParameter("groupID", groupID);

		try {
			String result = (String) query.getSingleResult();
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}

	@Transactional(readOnly = true)
	public List<Object[]> findFundingInfoByContractID(BigInteger contractID) {
		// 0 num_funding_type_ID, 1 txt_connection_ID, 2 num_partial, 3
		// txt_val1, 4 txt_val2
		String qryString = "SELECT num_funding_type_ID,txt_connection_ID,num_partial,txt_val1,txt_val2 "
				+ " FROM [HospSQL1_ClinicalResearch].[dbo].[contract_funding] "
				+ " WHERE num_contract_ID = :contractID";
		Query query = em.createNativeQuery(qryString);
		query.setParameter("contractID", contractID);
		try {
			List<Object[]> result = (List<Object[]>) query.getResultList();
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}	

	@Transactional(readOnly = true)
	public List<Integer> findRelatedContractByID(BigInteger contractID) {
		String qry = "SELECT num_relatedContract_ID FROM [HospSQL1_ClinicalResearch].[dbo].[contract_related] "
				+ " WHERE num_contract_ID = :contractID";
		Query query = em.createNativeQuery(qry);
		query.setParameter("contractID", contractID);

		try {
			List<Integer> result = (List<Integer>) query.getResultList();
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@Transactional(readOnly = true)
	public List<Object[]> findAllRelatedContracts() {
		String qry = "SELECT [num_contract_ID], [num_relatedContract_ID] FROM [HospSQL1_ClinicalResearch].[dbo].[contract_related]";
		Query query = em.createNativeQuery(qry);
		try {
			List<Object[]> result = (List<Object[]>) query.getResultList();
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@Transactional(readOnly = true)
	public String findtxtContractNumByContractID(int contractID) {
		String qry = "SELECT [txt_contract_number] FROM [HospSQL1_ClinicalResearch].[dbo].[contract] "
				+ " WHERE num_contract_ID = :contractID ";
		Query query = em.createNativeQuery(qry);
		query.setParameter("contractID", contractID);
		String result = "";
		try {
			result = (String) query.getSingleResult();
			return result;
		} catch (Exception e) {
			return result;
		}

	}

	@Transactional(readOnly = true)
	public String findIRBTitleByID(int irbNum) {
		String qry = "SELECT CAST(txt_irb_title AS varchar(8000)) FROM "
				+ " [HospSQL1_ClinicalResearch].[dbo].[irb] "
				+ " WHERE num_irb_ID =:irbNum";
		Query query = em.createNativeQuery(qry);
		query.setParameter("irbNum", irbNum);
		String result = "";
		try {
			result = (String) query.getSingleResult();
			return result;
		} catch (Exception e) {
			return result;
		}

	}

	@Transactional(readOnly = true)
	public List<Object[]> findLogsByContractID(BigInteger contractID) {
		//0 [num_contract_log_ID] , 1 [num_contract_ID],2 [txt_type],3 [txt_message],4 [date_logged],5 [num_user_ID],6 [num_status]
		String qry = "SELECT num_contract_log_ID ,num_contract_ID,txt_type,CAST(txt_message AS varchar(8000)),date_logged,num_user_ID,num_status" +
				" FROM [HospSQL1_ClinicalResearch].[dbo].[contract_log] " +
				" WHERE num_contract_ID =:contractID";
		Query query = em.createNativeQuery(qry);
		query.setParameter("contractID", contractID);
		try {
			List<Object[]> result = (List<Object[]>) query.getResultList();
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	@Transactional(readOnly = true)
	public List<Object[]> findDocsByContractID(BigInteger contractID) {
	/*	0, [num_contract_doc_ID],1 [num_contract_doc_type_ID] ,2 [txt_docs_title],3 [txt_docs_name],4 [txt_docs_ext], 
		 5 [date_uploaded],6 [num_docs_revised],7 [num_docs_revision] ,8 num_docs_revision_of ,9 num_user_ID*/
		String qry ="SELECT [num_contract_doc_ID],[num_contract_doc_type_ID] ,[txt_docs_title],[txt_docs_name],[txt_docs_ext], " +
				" [date_uploaded],[num_docs_revised],[num_docs_revision] ,num_docs_revision_of ,num_user_ID " +
				" From [HospSQL1_ClinicalResearch].[dbo].[contract_doc] " +
				" WHERE num_contract_ID =:contractID and num_status = 1";
		Query query = em.createNativeQuery(qry);
		query.setParameter("contractID", contractID);
		try {
			List<Object[]> result = (List<Object[]>) query.getResultList();
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	@Transactional(readOnly = true)
	public String getContractDocTypeByID(short typeID){
		String qry ="SELECT [txt_contract_doc_type] " +
				" FROM [HospSQL1_ClinicalResearch].[dbo].[contract_doc_type] " +
				" WHERE num_contract_doc_type_ID = :typeID";
		Query query = em.createNativeQuery(qry);
		query.setParameter("typeID", typeID);
		String result = "";
		try {
			result = (String) query.getSingleResult();
			return result;
		} catch (Exception e) {
			return result;
		}
		
	}
	
	@Transactional(readOnly = true)
	public List<Object[]> getCommitteeStatusByContractID(BigInteger contractID){
		/*0 [num_contract_report_ID],1 [num_group],2 [txt_start_decision],3 [date_start],
		" 4 [num_user_start],5 [txt_stop_decision],6 [date_stop],7 [num_contract_ID],8 [num_user_stop]
*/		String qry="SELECT [num_contract_report_ID],[num_group],[txt_start_decision],[date_start]," +
				" [num_user_start],[txt_stop_decision],[date_stop],[num_contract_ID],[num_user_stop] " +
				" FROM [HospSQL1_ClinicalResearch].[dbo].[contract_report]" +
				" WHERE num_contract_ID = :contractID";
		Query query = em.createNativeQuery(qry);
		query.setParameter("contractID", contractID);
		try {
			List<Object[]> result = (List<Object[]>) query.getResultList();
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	@Transactional(readOnly = true)
	public String getGroupNameByID(int groupID){
		String qry ="SELECT [txt_groups_name] " +
				" FROM [HospSQL1_ClinicalResearch].[dbo].[groups] " +
				" WHERE num_groups_ID = :groupID";
		Query query = em.createNativeQuery(qry);
		query.setParameter("groupID", groupID);
		String result = "";
		try {
			result = (String) query.getSingleResult();
			return result;
		} catch (Exception e) {
			return result;
		}
	}
	

	public EntityManager getEm() {
		return em;
	}

	@PersistenceContext(unitName = "defaultPersistenceUnit")
	public void setEm(EntityManager em) {
		this.em = em;
	}

}
