package edu.uams.clara.integration.incoming.crimson.dao;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

@Repository
public class CrimsonStudyDao {

	private EntityManager em;

	@Transactional(readOnly = true)
	public List<Integer> findfundingIDs(String irbNum) {

		String qry = "SELECT fud.num_funding_ID FROM [HospSQL1_ClinicalResearch].[dbo].[funding] as fud"
				+ " WHERE  fud.num_ct_ID = (SELECT MAX(ct.num_ct_ID) FROM"
				+ " [HospSQL1_ClinicalResearch].[dbo].[ct] as ct"
				+ " where ct.num_irb_ID= :irbNum)";
		Query query = em.createNativeQuery(qry);
		query.setParameter("irbNum", irbNum);
		List<Integer> result = null;
		try {
			result = query.getResultList();

			return result;
		} catch (Exception e) {
			return result;
		}

	}

	@Transactional(readOnly = true)
	public String findNofundingInfo(String irbNum) {

		String qry = "SELECT ct.num_ct_no_funding FROM [HospSQL1_ClinicalResearch].[dbo].[ct] as ct"
				+ " WHERE ct.num_irb_ID= :irbNum";

		Query query = em.createNativeQuery(qry);
		// query.setHint("org.hibernate.cacheable", true);
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
	public String findNofundingCost(String irbNum) {

		String qry = "SELECT ct.txt_ct_home_fund FROM [HospSQL1_ClinicalResearch].[dbo].[ct] as ct"
				+ " WHERE ct.num_irb_ID= :irbNum";

		Query query = em.createNativeQuery(qry);
		// query.setHint("org.hibernate.cacheable", true);
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
	public String findNofundingFund(String irbNum) {

		String qry = "SELECT ct.txt_ct_home_cost FROM [HospSQL1_ClinicalResearch].[dbo].[ct] as ct"
				+ " WHERE ct.num_irb_ID= :irbNum";

		Query query = em.createNativeQuery(qry);
		// query.setHint("org.hibernate.cacheable", true);
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
	public String findStudyType(String typeID) {

		String qry = "SELECT txt_type FROM [HospSQL1_ClinicalResearch].[dbo].[study_type] "
				+ " WHERE  num_study_type_ID = :typeID";

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
	public String findDeptByIRBNum(String irbNum) {

		String qry = "SELECT dept.txt_dept_name FROM [HospSQL1_ClinicalResearch].[dbo].[dept] as dept"
				+ " WHERE  dept.num_dept_ID = (SELECT ct.num_dept_ID "
				+ "FROM [HospSQL1_ClinicalResearch].[dbo].[ct] as ct"
				+ " WHERE ct.num_ct_ID = (SELECT MAX(ct.num_ct_ID) FROM  [HospSQL1_ClinicalResearch].[dbo].[ct]"
				+ " WHERE ct.num_irb_ID= :irbNum))";

		Query query = em.createNativeQuery(qry);
		// query.setHint("org.hibernate.cacheable", true);
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
	public List<String> findfundingTitle(String irbNum) {

		String qry = "SELECT CAST(fud.txt_project_title AS Varchar(8000)) FROM [HospSQL1_ClinicalResearch].[dbo].[funding] as fud"
				+ " WHERE  fud.num_ct_ID = (SELECT MAX(ct.num_ct_ID) FROM"
				+ " [HospSQL1_ClinicalResearch].[dbo].[ct] as ct"
				+ " where ct.num_irb_ID= :irbNum)";
		Query query = em.createNativeQuery(qry);
		query.setParameter("irbNum", irbNum);
		List<String> result = null;
		try {
			result = query.getResultList();

			return result;
		} catch (Exception e) {
			return result;
		}

	}

	@Transactional(readOnly = true)
	public List<String> findfundingType(String irbNum) {

		String qry = "SELECT fdt.txt_funding_type FROM [HospSQL1_ClinicalResearch].[dbo].[funding_type] as fdt "
				+ "inner join [HospSQL1_ClinicalResearch].[dbo].[funding] as fud"
				+ " on fdt.num_funding_type_ID = fud.num_funding_type_ID "
				+ "AND fud.num_ct_ID = (SELECT MAX(ct.num_ct_ID) FROM"
				+ " [HospSQL1_ClinicalResearch].[dbo].[ct] as ct  where ct.num_irb_ID= :irbNum)";

		Query query = em.createNativeQuery(qry);
		query.setParameter("irbNum", irbNum);

		List<String> result = null;
		try {
			result = query.getResultList();
			return result;
		} catch (Exception e) {
			return result;
		}

	}

	@Transactional(readOnly = true)
	public List<String> findfundingConnectionId(String irbNum) {

		String qry = "SELECT fud.num_connection_ID FROM [HospSQL1_ClinicalResearch].[dbo].[funding] as fud"
				+ " WHERE  fud.num_ct_ID = (SELECT MAX(ct.num_ct_ID) FROM"
				+ " [HospSQL1_ClinicalResearch].[dbo].[ct] as ct"
				+ " where ct.num_irb_ID= :irbNum)";

		Query query = em.createNativeQuery(qry);
		query.setParameter("irbNum", irbNum);

		List<String> result = null;
		try {
			result = query.getResultList();
			return result;
		} catch (Exception e) {
			return result;
		}

	}

	@Transactional(readOnly = true)
	public List<String> findfundingPartialInfo(String irbNum) {

		String qry = "SELECT fud.num_partial FROM [HospSQL1_ClinicalResearch].[dbo].[funding] as fud"
				+ " WHERE  fud.num_ct_ID = (SELECT MAX(ct.num_ct_ID) FROM"
				+ " [HospSQL1_ClinicalResearch].[dbo].[ct] as ct"
				+ " where ct.num_irb_ID= :irbNum)";

		Query query = em.createNativeQuery(qry);
		query.setParameter("irbNum", irbNum);

		List<String> result = null;
		try {
			result = query.getResultList();
			return result;
		} catch (Exception e) {
			return result;
		}

	}

	@Transactional(readOnly = true)
	public String findSponsorNamebyID(String sponsorID) {

		String qry = "SELECT txt_sponsor_name FROM [HospSQL1_ClinicalResearch].[dbo].[sponsor] as spr "
				+ "WHERE spr.num_sponsor_ID like :sponsorID";

		Query query = em.createNativeQuery(qry);
		// query.setHint("org.hibernate.cacheable", true);
		query.setParameter("sponsorID", sponsorID);

		String result = "";
		try {
			result = (String) query.getSingleResult();
			return result;
		} catch (Exception e) {
			return result;
		}

	}

	/*
	 * @Transactional(readOnly = true) public String findSponsorTypebyID(String
	 * sponsorID) {
	 * 
	 * String qry = "SELECT txt_sponsor_funding_type FROM " +
	 * "[HospSQL1_ClinicalResearch].[dbo].[funding] as fud " +
	 * "inner join [HospSQL1_ClinicalResearch].[dbo].[sponsor] as spr " +
	 * "on fud.num_connection_ID = spr.num_sponsor_ID AND fud.txt_project_title like :sponsorID"
	 * ;
	 * 
	 * Query query = em .createNativeQuery(qry);
	 * //query.setHint("org.hibernate.cacheable", true);
	 * query.setParameter("sponsorID",sponsorID);
	 * 
	 * String result=""; try{ result =(String)query.getSingleResult(); return
	 * result; } catch(Exception e){ return result; }
	 * 
	 * 
	 * }
	 */

	@Transactional(readOnly = true)
	public String findCRONamebyID(String CroID) {

		String qry = "SELECT txt_cro_name FROM "
				+ " [HospSQL1_ClinicalResearch].[dbo].[cro] as c "
				+ "Where c.num_cro_ID like :CroID";

		Query query = em.createNativeQuery(qry);
		// query.setHint("org.hibernate.cacheable", true);
		query.setParameter("CroID", CroID);

		String result = "";
		try {
			result = (String) query.getSingleResult();
			return result;
		} catch (Exception e) {
			return result;
		}

	}

	@Transactional(readOnly = true)
	public List<String> findfundingContactsAddress(long fundingID) {

		String qry = "SELECT CAST(cot.txt_contact_address as Varchar(8000)) "
				+ " FROM [HospSQL1_ClinicalResearch].[dbo].[contact] as cot"
				+ " WHERE cot.num_funding_ID= :fundingID";
		Query query = em.createNativeQuery(qry);
		query.setParameter("fundingID", fundingID);
		List<String> result = null;
		try {
			result = query.getResultList();

			return result;
		} catch (Exception e) {
			return result;
		}

	}

	@Transactional(readOnly = true)
	public List<String> findfundingContactsPhone(long fundingID) {

		String qry = "SELECT CAST(cot.txt_contact_phone as Varchar(8000)) "
				+ " FROM [HospSQL1_ClinicalResearch].[dbo].[contact] as cot"
				+ " WHERE cot.num_funding_ID= :fundingID";
		Query query = em.createNativeQuery(qry);
		query.setParameter("fundingID", fundingID);
		List<String> result = null;
		try {
			result = query.getResultList();

			return result;
		} catch (Exception e) {
			return result;
		}

	}

	@Transactional(readOnly = true)
	public List<String> findfundingContactsFax(long fundingID) {

		String qry = "SELECT CAST(cot.txt_contact_fax as Varchar(8000)) "
				+ " FROM [HospSQL1_ClinicalResearch].[dbo].[contact] as cot"
				+ " WHERE cot.num_funding_ID= :fundingID";
		Query query = em.createNativeQuery(qry);
		query.setParameter("fundingID", fundingID);
		List<String> result = null;
		try {
			result = query.getResultList();

			return result;
		} catch (Exception e) {
			return result;
		}

	}

	@Transactional(readOnly = true)
	public List<String> findfundingContactsEmail(long fundingID) {

		String qry = "SELECT CAST(cot.txt_contact_email as Varchar(8000)) "
				+ " FROM [HospSQL1_ClinicalResearch].[dbo].[contact] as cot"
				+ " WHERE cot.num_funding_ID= :fundingID";
		Query query = em.createNativeQuery(qry);
		query.setParameter("fundingID", fundingID);
		List<String> result = null;
		try {
			result = query.getResultList();

			return result;
		} catch (Exception e) {
			return result;
		}

	}

	@Transactional(readOnly = true)
	public List<String> findfundingContactsTitle(long fundingID) {

		String qry = "SELECT CAST(cot.txt_contact_title as Varchar(8000)) "
				+ " FROM [HospSQL1_ClinicalResearch].[dbo].[contact] as cot"
				+ " WHERE cot.num_funding_ID= :fundingID";
		Query query = em.createNativeQuery(qry);
		query.setParameter("fundingID", fundingID);
		List<String> result = null;
		try {
			result = query.getResultList();

			return result;
		} catch (Exception e) {
			return result;
		}

	}

	@Transactional(readOnly = true)
	public List<String> findfundingContactsPerson(long fundingID) {

		String qry = "SELECT CAST(cot.txt_contact_person as Varchar(8000)) "
				+ " FROM [HospSQL1_ClinicalResearch].[dbo].[contact] as cot"
				+ " WHERE cot.num_funding_ID= :fundingID";
		Query query = em.createNativeQuery(qry);
		query.setParameter("fundingID", fundingID);
		List<String> result = null;
		try {
			result = query.getResultList();

			return result;
		} catch (Exception e) {
			return result;
		}

	}

	@Transactional(readOnly = true)
	public String findInternalSrc(String internalID) {

		String qry = "SELECT it.txt_internal_source "
				+ " FROM [HospSQL1_ClinicalResearch].[dbo].[internal] as it"
				+ " WHERE it.num_internal_ID= :internalID";
		Query query = em.createNativeQuery(qry);
		query.setParameter("internalID", internalID);
		String result = "";
		try {
			result = (String) query.getSingleResult();

			return result;
		} catch (Exception e) {
			return result;
		}

	}

	@Transactional(readOnly = true)
	public String findProjectSponsor(String projectId) {

		String qry = "SELECT spo.txt_sponsor_name "
				+ " FROM [HospSQL1_ClinicalResearch].[dbo].[sponsor] as spo inner join"
				+ "	[HospSQL1_ClinicalResearch].[dbo].[project] pro"
				+ " on pro.num_project_ID= :projectId AND pro.num_sponsor_ID = spo.num_sponsor_ID";
		Query query = em.createNativeQuery(qry);
		query.setParameter("projectId", projectId);
		String result = "";
		try {
			result = (String) query.getSingleResult();

			return result;
		} catch (Exception e) {
			return result;
		}

	}

	@Transactional(readOnly = true)
	public String findProjectPiName(String projectId) {

		String qry = "SELECT au.first "
				+ " FROM [HospSQL1_ClinicalResearch].[dbo].[aria_users] as au inner join [HospSQL1_ClinicalResearch].[dbo].[project] pro"
				+ " on pro.num_project_ID= :projectId AND pro.pi_serial = au.pi_serial";
		Query query = em.createNativeQuery(qry);
		query.setParameter("projectId", projectId);

		String qry2 = "SELECT au.lname "
				+ " FROM [HospSQL1_ClinicalResearch].[dbo].[aria_users] as au inner join [HospSQL1_ClinicalResearch].[dbo].[project] pro"
				+ " on pro.num_project_ID= :projectId AND pro.pi_serial = au.pi_serial";
		Query query2 = em.createNativeQuery(qry2);
		query2.setParameter("projectId", projectId);

		String result = "";
		try {
			result = query.getSingleResult() + " " + query2.getSingleResult();

			return result;
		} catch (Exception e) {
			return result;
		}

	}
	
	@Transactional(readOnly = true)
	public int findPiSerialByfirstandLastName(String firstName, String lastName){
		String qry = "SELECT pi_serial"
				+ " FROM [HospSQL1_ClinicalResearch].[dbo].[aria_users] " +
				" WHERE [lname] = :lastName AND [first] = :firstName";
		Query query = em.createNativeQuery(qry);
		query.setParameter("firstName", firstName);
		query.setParameter("lastName", lastName);
		
		int result = 0;
		try {
			result = (int) query.getSingleResult();
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			return result;
		}
	}

	@Transactional(readOnly = true)
	public String findLocationCodeById(String locationID) {
		String qry = "SELECT txt_location_code FROM [HospSQL1_ClinicalResearch].[dbo].[location]"
				+ " WHERE  num_location_ID = :locationID";

		Query query = em.createNativeQuery(qry);
		query.setParameter("locationID", locationID);
		String result = "";
		try {
			result = (String) query.getSingleResult();

			return result;
		} catch (Exception e) {
			return result;
		}

	}

	@Transactional(readOnly = true)
	public String findLocationNameById(String locationID) {
		String qry = "SELECT txt_location_name FROM [HospSQL1_ClinicalResearch].[dbo].[location]"
				+ " WHERE  num_location_ID = :locationID";

		Query query = em.createNativeQuery(qry);
		query.setParameter("locationID", locationID);
		String result = "";
		try {
			result = (String) query.getSingleResult();

			return result;
		} catch (Exception e) {
			return result;
		}

	}

	@Transactional(readOnly = true)
	public String findLocationIDsbyIRBNum(String IRBNum) {

		String qry = "SELECT txt_locationIDs FROM [HospSQL1_ClinicalResearch].[dbo].[ct]"
				+ " WHERE ct.num_ct_ID = (SELECT MAX(ct.num_ct_ID) FROM  [HospSQL1_ClinicalResearch].[dbo].[ct]"
				+ " WHERE ct.num_irb_ID= :IRBNum)";
		Query query = em.createNativeQuery(qry);
		query.setParameter("IRBNum", IRBNum);
		String result = "";
		try {
			result = (String) query.getSingleResult();

			return result;
		} catch (Exception e) {
			return result;
		}
	}

	@Transactional(readOnly = true)
	public String findStudyTypeIDsbyIRBNum(String IRBNum) {

		String qry = "SELECT txt_studytypeIDs FROM [HospSQL1_ClinicalResearch].[dbo].[ct]"
				+ " WHERE ct.num_ct_ID = (SELECT MAX(ct.num_ct_ID) FROM  [HospSQL1_ClinicalResearch].[dbo].[ct]"
				+ " WHERE ct.num_irb_ID= :IRBNum)";
		Query query = em.createNativeQuery(qry);
		query.setParameter("IRBNum", IRBNum);
		String result = "";
		try {
			result = (String) query.getSingleResult();

			return result;
		} catch (Exception e) {
			return result;
		}
	}

	@Transactional(readOnly = true)
	public String findDivisionNameByID(int divisionID) {
		String qry = "SELECT txt_division_name FROM [HospSQL1_ClinicalResearch].[dbo].[division] "
				+ "WHERE num_division_ID = :divisionID";
		Query query = em.createNativeQuery(qry);
		query.setParameter("divisionID", divisionID);
		String result = "";
		try {
			result = (String) query.getSingleResult();
			return result;
		} catch (Exception e) {
			return result;
		}
	}

	@Transactional(readOnly = true)
	public String findDeptNameByID(int deptID) {
		String qry = "SELECT txt_dept_name FROM [HospSQL1_ClinicalResearch].[dbo].[dept] "
				+ "WHERE num_dept_ID = :deptID";
		Query query = em.createNativeQuery(qry);
		query.setParameter("deptID", deptID);
		String result = "";
		try {
			result = (String) query.getSingleResult();

			return result;
		} catch (Exception e) {
			return result;
		}
	}

	@Transactional(readOnly = true)
	public String findsubDeptNameByID(int subdeptID) {
		String qry = "SELECT txt_subdept_name FROM [HospSQL1_ClinicalResearch].[dbo].[subdept] "
				+ "WHERE num_subdept_ID = :subdeptID";
		Query query = em.createNativeQuery(qry);
		query.setParameter("subdeptID", subdeptID);
		String result = "";
		try {
			result = (String) query.getSingleResult();

			return result;
		} catch (Exception e) {
			return result;
		}
	}

	@Transactional(readOnly = true)
	public String findCtTypeNameById(int ctTypeID) {
		String qry = "SELECT txt_ct_type FROM [HospSQL1_ClinicalResearch].[dbo].[ct_type] "
				+ "WHERE num_ct_type_ID = :ctTypeID";
		Query query = em.createNativeQuery(qry);
		query.setParameter("ctTypeID", ctTypeID);
		String result = "";
		try {
			result = (String) query.getSingleResult();

			return result;
		} catch (Exception e) {
			return result;
		}
	}

	@Transactional(readOnly = true)
	public String findPhaseById(String phaseID) {
		String qry = "SELECT txt_phase FROM [HospSQL1_ClinicalResearch].[dbo].[phase] "
				+ "WHERE num_phase_ID = :phaseID";
		Query query = em.createNativeQuery(qry);
		query.setParameter("phaseID", phaseID);
		String result = "";
		try {
			result = (String) query.getSingleResult();

			return result;
		} catch (Exception e) {
			return result;
		}
	}

	@Transactional(readOnly = true)
	public String findNotifyMethodById(int methodID) {
		String qry = "SELECT txt_notify_method FROM [HospSQL1_ClinicalResearch].[dbo].[notify_method] "
				+ "WHERE num_notify_method_ID = :methodID";
		Query query = em.createNativeQuery(qry);
		query.setParameter("methodID", methodID);
		String result = "";
		try {
			result = (String) query.getSingleResult();

			return result;
		} catch (Exception e) {
			return result;
		}
	}

	@Transactional(readOnly = true)
	public String findNotifyModeById(int modeID) {
		String qry = "SELECT txt_notify_mode FROM [HospSQL1_ClinicalResearch].[dbo].[notify_mode] "
				+ "WHERE num_notify_mode_ID = :modeID";
		Query query = em.createNativeQuery(qry);
		query.setParameter("modeID", modeID);
		String result = "";
		try {
			result = (String) query.getSingleResult();

			return result;
		} catch (Exception e) {
			return result;
		}
	}

	@Transactional(readOnly = true)
	public String findArticleById(int articleID) {
		String qry = "SELECT txt_article FROM [HospSQL1_ClinicalResearch].[dbo].[article] "
				+ "WHERE num_article_ID = :articleID";
		Query query = em.createNativeQuery(qry);
		query.setParameter("articleID", articleID);
		String result = "";
		try {
			result = (String) query.getSingleResult();

			return result;
		} catch (Exception e) {
			return result;
		}
	}

	@Transactional(readOnly = true)
	public String findManufactorByID(int manuID) {
		String qry = "SELECT txt_ct_man FROM [HospSQL1_ClinicalResearch].[dbo].[ct_man] "
				+ "WHERE num_ct_man_ID = :manuID";
		Query query = em.createNativeQuery(qry);
		query.setParameter("manuID", manuID);
		String result = "";
		try {
			result = (String) query.getSingleResult();

			return result;
		} catch (Exception e) {
			return result;
		}
	}

	@Transactional(readOnly = true)
	public Object[] findRegInfobyCtID(int ctID) {
		String qry = "SELECT *  FROM [HospSQL1_ClinicalResearch].[dbo].[ct_reg] WHERE num_ct_ID =:ctID";
		Query query = em.createNativeQuery(qry);
		query.setParameter("ctID", ctID);
		try {
			Object result[] = (Object[]) query.getSingleResult();
			return result;
		} catch (Exception e) {
			return null;
		}
	}
	
	@Transactional(readOnly = true)
	public Object[] findPlanCodeByCTNum(String CTNum) {
		String qry = "SELECT [txt_corporate_guarantor],[txt_plan_code] FROM [HospSQL1_ClinicalResearch].[dbo].[budget] where [num_ct_ID] ="+CTNum;
		Query query = em.createNativeQuery(qry);
		try {
			Object result[] = (Object[]) query.getSingleResult();
			return result;
		} catch (Exception e) {
			return null;
		}
	}

	@Transactional(readOnly = true)
	public Object[] findCTObjectbyIRBNum(String IRBNum) {
		// 0 brief title;1 division id; 2 dept id;3 subdept id;4, cttype id; 5
		// summary; 6 comment; 7 num_compassionate_use;
		// 8 txt_phase;9 notify_method_ID;10 notify_mode_ID;11 txt_test_article;
		// 12 num_prmc_required;
		// 13 num_ct_man_ID;14 num_ct_id; 15 budget required;16 txt_title, 17
		// num_ct_status, 18 txt_ct_site
		String qry = "SELECT CAST(txt_brief_title as varchar(8000)) as briftitle ,num_division_ID, num_dept_ID, num_subdept_ID, num_ct_type_ID, "
				+ "CAST(txt_summary as varchar(8000)) as summary, CAST(txt_comment as varchar(8000)) as cet, num_compassionate_use, "
				+ "txt_phase, num_notify_method_ID, num_notify_mode_ID, txt_test_article, num_prmc_required, num_ct_man_ID, "
				+ "num_ct_ID, num_budget_req,  CAST(txt_title as varchar(8000)) as title, num_ct_status,txt_ct_site"
				+ " FROM [HospSQL1_ClinicalResearch].[dbo].[ct]"
				+ " WHERE ct.num_ct_ID = (SELECT MAX(ct.num_ct_ID) FROM  [HospSQL1_ClinicalResearch].[dbo].[ct]"
				+ " WHERE ct.num_irb_ID= :IRBNum)";
		Query query = em.createNativeQuery(qry);
		query.setParameter("IRBNum", IRBNum);
		try {
			Object result[] = (Object[]) query.getSingleResult();
			return result;
		} catch (Exception e) {
			return null;
		}
	}
	
	@Transactional(readOnly = true)
	public List<Object[]> findAllCTObjectsByIRBNum(String IRBNum) {
		String qry ="SELECT num_ct_id,num_parent FROM [HospSQL1_ClinicalResearch].[dbo].[ct] WHERE num_irb_ID= :IRBNum";
		Query query = em.createNativeQuery(qry);
		query.setParameter("IRBNum", IRBNum);
		try {
			List<Object[]> result = (List<Object[]>) query.getResultList();
			return result;
		} catch (Exception e) {
			return null;
		}
		
	}
	
	@Transactional(readOnly = true)
	public List<Object[]> findAllDocumentsByCTID(int ctID){
		//0 num_docs_ID,1 txt_docs_title,2 num_docs_revision,3 num_docs_revision_of,4 date_docs_uploaded,5 num_doc_type_ID, 6 txt_docs_ext
		String qry ="SELECT num_docs_ID,txt_docs_title,num_docs_revision,num_docs_revision_of,date_docs_uploaded,num_doc_type_ID, txt_docs_ext" +
				" FROM [HospSQL1_ClinicalResearch].[dbo].[docs] " +
				" WHERE num_ct_ID = :ctID";
		Query query = em.createNativeQuery(qry);
		query.setParameter("ctID", ctID);
		
		try {
			List<Object[]> result = (List<Object[]>) query.getResultList();
			return result;
		} catch (Exception e) {
			return null;
		}
	}
	
	@Transactional(readOnly = true)
	public List<Object[]> findAllPacketDocumentsByIRBNum(String IRBNum){
		//0 num_postdoc_ID,1 txt_docs_title,2 num_docs_revision,3 num_docs_revision_of,4 date_uploaded,5 num_postdoc_type_ID, 6 txt_docs_ext, 7is_postweb
		String qry ="SELECT num_postdoc_ID,txt_docs_title,num_docs_revision,num_docs_revision_of,date_uploaded,num_postdoc_type_ID, txt_docs_ext,num_is_postweb" +
				" FROM [HospSQL1_ClinicalResearch].[dbo].[postdoc] " +
				" WHERE txt_irb_ID = :IRBNum";
		Query query = em.createNativeQuery(qry);
		query.setParameter("IRBNum", IRBNum);
		
		try {
			List<Object[]> result = (List<Object[]>) query.getResultList();
			return result;
		} catch (Exception e) {
			return null;
		}
	}
	
	@Transactional(readOnly = true)
	public String findDocumentTypeByID(short typeID){
		String qry ="SELECT [txt_doc_type] FROM [HospSQL1_ClinicalResearch].[dbo].[doc_type] WHERE num_doc_type_ID=:typeID";
		Query query = em.createNativeQuery(qry);
		query.setParameter("typeID", typeID);
		try {
			String result = (String) query.getSingleResult();
			return result;
		} catch (Exception e) {
			return "";
		}
	}
	
	@Transactional(readOnly = true)
	public String findPacketDocumentTypeByID(short typeID){
		String qry ="SELECT [txt_doc_type] FROM [HospSQL1_ClinicalResearch].[dbo].[postdoc_type] WHERE num_postdoc_type_ID=:typeID";
		Query query = em.createNativeQuery(qry);
		query.setParameter("typeID", typeID);
		try {
			String result = (String) query.getSingleResult();
			return result;
		} catch (Exception e) {
			return "";
		}
	}
	

	@Transactional(readOnly = true)
	public List<String> findDrugNameByIRBNum(String IRBNum) {
		String qry = "SELECT txt_drugname "
				+ " FROM [HospSQL1_ClinicalResearch].[dbo].[drug] "
				+ " WHERE num_irb_ID = :IRBNum";
		Query query = em.createNativeQuery(qry);
		query.setParameter("IRBNum", IRBNum);
		try {
			return (List<String>) query.getResultList();
		} catch (Exception e) {
			return null;
		}
	}

	@Transactional(readOnly = true)
	public String findCrimosnStatusByID(int statusID) {
		String qry = "SELECT txt_status FROM [HospSQL1_ClinicalResearch].[dbo].ct_status "
				+ " WHERE num_ct_status_ID = :statusID";
		Query query = em.createNativeQuery(qry);
		query.setParameter("statusID", statusID);
		try {
			return (String) query.getSingleResult();
		} catch (Exception e) {
			return "";
		}

	}

	@Transactional(readOnly = true)
	public String findCrimosnStatusByCrimsonID(int crimsonID) {
		String qry = " SELECT txt_status FROM [HospSQL1_ClinicalResearch].[dbo].ct_status inner join [HospSQL1_ClinicalResearch].[dbo].ct "
				+ "on num_ct_id = :crimsonID AND num_ct_status = num_ct_status_ID";
		Query query = em.createNativeQuery(qry);
		query.setParameter("crimsonID", crimsonID);
		try {
			return (String) query.getSingleResult();
		} catch (Exception e) {
			return "";
		}
	}
	
	@Transactional(readOnly = true)
	public String findDiseaseSitebyID(int siteID){
		String qry = "SELECT txt_site FROM [HospSQL1_ClinicalResearch].[dbo].[site] " +
				" WHERE [num_site_ID] = :siteID";
		Query query = em.createNativeQuery(qry);
		query.setParameter("siteID", siteID);
		try {
			return (String) query.getSingleResult();
		} catch (Exception e) {
			return "";
		}
	}
	
	@Transactional(readOnly = true)
	public String findSubDiseaseSitebyID(int subSiteID){
		String qry = "SELECT txt_sub_site FROM [HospSQL1_ClinicalResearch].[dbo].[sub_site] " +
				" WHERE [num_sub_site_ID] = :subSiteID";
		Query query = em.createNativeQuery(qry);
		query.setParameter("subSiteID", subSiteID);
		try {
			return (String) query.getSingleResult();
		} catch (Exception e) {
			return "";
		}
	}
	
	@Transactional(readOnly = true)
	public Object[] findBudgetByCTID(int crimsonID){
		//0 [num_budget_ID],1 [num_bm],2 [txt_corporate_guarantor],3 [txt_plan_code],
		//4 [num_risk_code_ID],5 [num_ba]
		String qry = "SELECT [num_budget_ID],[num_bm],[txt_corporate_guarantor],[txt_plan_code],[num_risk_code_ID],[num_ba] " +
				" FROM [HospSQL1_ClinicalResearch].[dbo].[budget] " +
				"WHERE num_ct_ID = :crimsonID";
		Query query = em.createNativeQuery(qry);
		query.setParameter("crimsonID", crimsonID);
		try {
			Object[] result = (Object[]) query.getSingleResult();
			return result;
		} catch (Exception e) {
			return null;
		}
		
	}
	
	@Transactional(readOnly = true)
	public Object[] findAriaUserByUserID(int userID) {
		// 0 sapID, 1 lname, 2 first, 3 prim_email, 4 prim_phone, 5 pi_serial, 6
		// dept_name
		String qryString = "SELECT sapID, lname, first, prim_email, prim_phone, pi_serial, dept_name "
				+ " FROM [HospSQL1_ClinicalResearch].[dbo].[aria_users] "
				+ " WHERE pi_serial = :userID";
		Query query = em.createNativeQuery(qryString);
		query.setParameter("userID", userID);

		try {
			Object[] result = (Object[]) query.getSingleResult();
			return result;
		} catch (Exception e) {
			return null;
		}
	}
	
	@Transactional(readOnly = true)
	public List<Object[]> findAllProject() {
//0 [num_project_ID], 1 txt_project_title, 2  pi_serial,3  [num_sponsor_ID],4  [txt_project_status]
		String qry = "SELECT [num_project_ID], txt_project_title,  pi_serial, [num_sponsor_ID], [txt_project_status] FROM [HospSQL1_ClinicalResearch].[dbo].[project]";
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
	public String findFundingTypeByID(int typeID) {
		String qryString = "SELECT txt_funding_type FROM [HospSQL1_ClinicalResearch].[dbo].[funding_type] "
				+ " WHERE num_funding_type_ID = :typeID";
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
	public String findSponsorNameByID(int sponsor_ID) {
		String qryString = "SELECT txt_sponsor_name FROM [HospSQL1_ClinicalResearch].[dbo].[sponsor] "
				+ " WHERE num_sponsor_ID = :sponsor_ID";
		Query query = em.createNativeQuery(qryString);
		query.setParameter("sponsor_ID", sponsor_ID);
		try {
			String result = (String) query.getSingleResult();
			return result;
		} catch (Exception e) {
			return "";
		}

	}

	@Transactional(readOnly = true)
	public String findCRONamebyID(int CroID) {

		String qry = "SELECT txt_cro_name FROM "
				+ " [HospSQL1_ClinicalResearch].[dbo].[cro] as c "
				+ "Where c.num_cro_ID like :CroID";

		Query query = em.createNativeQuery(qry);
		// query.setHint("org.hibernate.cacheable", true);
		query.setParameter("CroID", CroID);

		String result = "";
		try {
			result = (String) query.getSingleResult();
			return result;
		} catch (Exception e) {
			return result;
		}

	}

	@Transactional(readOnly = true)
	public String findInternalSrc(int internalID) {

		String qry = "SELECT it.txt_internal_source "
				+ " FROM [HospSQL1_ClinicalResearch].[dbo].[internal] as it"
				+ " WHERE it.num_internal_ID= :internalID";
		Query query = em.createNativeQuery(qry);
		query.setParameter("internalID", internalID);
		String result = "";
		try {
			result = (String) query.getSingleResult();

			return result;
		} catch (Exception e) {
			return result;
		}

	}

	@Transactional(readOnly = true)
	public Object[] findProjectInfoByID(String projectID) {

		String qry = "SELECT  pi_serial, txt_project_title FROM [HospSQL1_ClinicalResearch].[dbo].[project] "
				+ " WHERE [num_project_ID] LIKE :projectID";
		Query query = em.createNativeQuery(qry);
		query.setParameter("projectID", projectID);

		try {
			Object[] result = (Object[]) query.getSingleResult();
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	@Transactional(readOnly = true)
	public Date findSubmittedTimeByctID(int ctID){
		String qry = "SELECT date_submitted FROM [HospSQL1_ClinicalResearch].[dbo].[ct_report_info] WHERE num_ct_ID = :ctID";
		Query query = em.createNativeQuery(qry);
		query.setParameter("ctID", ctID);
		
		try {
			Date result = (Date) query.getSingleResult();
			return result;
		} catch (Exception e) {
			//e.printStackTrace();
			return null;
		}
	}
	
	@Transactional(readOnly = true)
	public List<Object[]> findUserEvaByctID(int ctID){
		String qry = "SELECT [num_rev_entity],[date_starttime],[date_stoptime] " +
				" FROM [HospSQL1_ClinicalResearch].[dbo].[user_evaluation] " +
				" WHERE [num_ct_ID] = :ctID order by [num_rev_entity],[date_starttime]";
		Query query = em.createNativeQuery(qry);
		query.setParameter("ctID", ctID);
		
		try {
			List<Object[]> result = (List<Object[]>) query.getResultList();
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	@Transactional(readOnly = true)
	public List<String> findTextRev(List<Integer> entities){
		String qry ="SELECT txt_rev FROM [HospSQL1_ClinicalResearch].[dbo].[revlook]";
		
		if(entities != null){
			Joiner joiner = Joiner.on(", ").skipNulls();
			qry += " WHERE num_revlook_ID IN (" + joiner.join(entities) + ")";
		}
		Query query = em.createNativeQuery(qry);
		try {
			List<String> result = (List<String>) query.getResultList();
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	@Transactional(readOnly = true)
	public Date findBudgetApprovalDate(int ctID){
		String qry ="SELECT [date_completed] FROM [HospSQL1_ClinicalResearch].[dbo].[ct_report_info] where num_ct_ID = :ctID";
		Query query = em.createNativeQuery(qry);
		query.setParameter("ctID", ctID);
		
		try {
			Date result = (Date) query.getSingleResult();
			return result;
		} catch (Exception e) {
			//e.printStackTrace();
			return null;
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
