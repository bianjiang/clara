package edu.uams.clara.webapp.report.service.customreport.impl;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import edu.emory.mathcs.backport.java.util.Collections;
import edu.uams.clara.webapp.common.dao.department.DepartmentDao;
import edu.uams.clara.webapp.common.domain.department.Department;
import edu.uams.clara.webapp.common.domain.usercontext.enums.Committee;
import edu.uams.clara.webapp.common.util.DateFormatUtil;
import edu.uams.clara.webapp.protocol.dao.ProtocolDao;
import edu.uams.clara.webapp.protocol.dao.businesslogicobject.AgendaStatusDao;
import edu.uams.clara.webapp.protocol.dao.businesslogicobject.ProtocolFormCommitteeStatusDao;
import edu.uams.clara.webapp.protocol.dao.businesslogicobject.ProtocolFormStatusDao;
import edu.uams.clara.webapp.protocol.dao.protocolform.ProtocolFormDao;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.ProtocolFormCommitteeStatus;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.ProtocolFormStatus;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.enums.ProtocolFormStatusEnum;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolForm;
import edu.uams.clara.webapp.protocol.domain.protocolform.enums.ProtocolFormType;
import edu.uams.clara.webapp.report.domain.CommitteeActions;
import edu.uams.clara.webapp.report.domain.ReportCriteria;
import edu.uams.clara.webapp.report.domain.ReportFieldTemplate;
import edu.uams.clara.webapp.report.domain.ReportTemplate;
import edu.uams.clara.webapp.report.service.customreport.CustomReportService;

public class HSRReportRestultServiceImpl extends CustomReportService {
	private final static Logger logger = LoggerFactory
			.getLogger(HSRReportRestultServiceImpl.class);

	private EntityManager em;
	private DepartmentDao departmentDao;
	private ProtocolFormStatusDao protocolFormStatusDao;
	private ProtocolFormDao protocolFormDao;
	private ProtocolDao protocolDao;
	private ProtocolFormCommitteeStatusDao protocolFormCommitteeStatusDao;
	private AgendaStatusDao agendaStatusDao;
	
	private CommitteeActions committeeActions = new CommitteeActions();

	private  Map<Long,String> collegeIds = Maps.newLinkedHashMap();
	{
		collegeIds.put(3l,"CHP");
		collegeIds.put(5l,"COM");
		collegeIds.put(6l,"CON");
		collegeIds.put(9l,"COP");
		collegeIds.put(14l,"COPH");
		collegeIds.put(10l,"Regional Programs");
		collegeIds.put(100000l,"All Colleges");
	}

	private  List<String> initators = Lists.newArrayList();
	{
		initators.add("INDUSTRY-SPONSORED");
		initators.add("INVESTIGATOR-INITIATED");
		initators.add("COOPERATIVE-GROUP");
		initators.add("STUDENT-FELLOW-RESIDENT-POST-DOC");
		initators.add("OTHER");
	}
	
	private Map<String,String> fundingSources = Maps.newLinkedHashMap();{
		fundingSources.put("Federal Agency","('federal-grant-directly-to-uams-achri','federal-sub-contract-from-another-institution')");
		fundingSources.put("Industry","('industry-support-full-funding','industry-support-partial-funding','industry-support-drug-device-only')");
		fundingSources.put("Non-Federal","('non-federal-grant')");
		fundingSources.put("UAMS","('internal-support')");
		fundingSources.put("Student Research","");
		fundingSources.put("Other","('other')");
		fundingSources.put("None","('no-designated-support')");
	}

	private  List<String> reviewTypes = Lists.newArrayList();
	{
		reviewTypes.add("FULL BOARD");
		reviewTypes.add("EXPEDITED");
		reviewTypes.add("EXEMPT");
	}
	
	private  List<String> closeReasons = Lists.newArrayList();
	{
		closeReasons.add("completed-as-planned");
		closeReasons.add("terminated-early-due-to-safety-concerns");
		closeReasons.add("terminated-early-due-to-slow-accrual");
		closeReasons.add("due-to-sponsor-withdrawal");
	}

	private String fillSummaryFileds(Map<String, String> fieldsMap) {
		String summaryFields = "";
		for (Map.Entry<String, String> entry : fieldsMap.entrySet()) {
			summaryFields += "<field class=\"field-summary\" id=\""
					+ entry.getKey() + "\">";
			summaryFields += entry.getValue();
			summaryFields += "</field>";
		}
		return summaryFields;

	}

	private String fillTitleRow(Map<String, String> fieldsMap) {
		String titleFields = "<fields>";
		for (Map.Entry<String, String> entry : fieldsMap.entrySet()) {
			titleFields += "<field id=\"" + entry.getKey() + "\" desc=\""
					+ entry.getValue() + "\" hidden=\"" + "false" + "\" />";
		}
		titleFields += "</fields>";
		return titleFields;
	}
	
	private String reviewtypeQuery(String reviewType, String qry){
		if(reviewType.equals("FULL BOARD")){
			qry="(id in (select distinct p1.protocol_id from protocol_form p1,protocol_form p2 where p1.parent_id =p2.parent_id and p1.id in (select protocol_form_id from protocol_form_status  where protocol_form_status ='EXPEDITED_APPROVED'  and retired= 0) and p2.id in (select protocol_form_id from protocol_form_status  where protocol_form_status ='IRB_DEFERRED_WITH_MINOR_CONTINGENCIES'  and retired= 0) and p1.protocol_form_type ='NEW_SUBMISSION' and p2.protocol_form_type ='NEW_SUBMISSION') or "+qry+")";
		}else if(reviewType.equals("EXPEDITED")){
			qry="(id not in (select distinct p1.protocol_id from protocol_form p1,protocol_form p2 where p1.parent_id =p2.parent_id and p1.id in (select protocol_form_id from protocol_form_status  where protocol_form_status ='EXPEDITED_APPROVED'  and retired= 0) and p2.id in (select protocol_form_id from protocol_form_status  where protocol_form_status ='IRB_DEFERRED_WITH_MINOR_CONTINGENCIES'  and retired= 0) and p1.protocol_form_type ='NEW_SUBMISSION' and p2.protocol_form_type ='NEW_SUBMISSION')) and "+qry;
		}
		
		return qry;
	}

	/*********
	 * Report Data
	 * ******/
	private String subType1Report(String queryCriterias) {
		String submittedStudyQueryStr = " id not in (select distinct protocol_id from protocol_status where retired = 0 and id in (select max(id) from protocol_status where retired = 0  group by protocol_id) and protocol_status  in ('CANCELLED','DRAFT','PENDING_PL_ENDORSEMENT','PENDING_PI_ENDORSEMENT','PENDING_TP_ENDORSEMENT'))";
		String underINDCondition = " and meta_data_xml.value('(/protocol/summary/drugs-and-devices/ind)[1]','varchar(255)') not in  ('','N/A') ";
		String underIDECondition = " and meta_data_xml.value('(/protocol/summary/drugs-and-devices/ide)[1]','varchar(255)') not in  ('','N/A') ";

		String reportResultXml = "<report-result id=\"" + "" + "\"  created=\""
				+ DateFormatUtil.formateDateToMDY(new Date()) + "\">";

		reportResultXml += "<title>"
				+ "Report Data"
				+ "</title>";
		Map<String, String> titleRowFileds = Maps.newLinkedHashMap();
		titleRowFileds.put("college", "College");
		titleRowFileds.put("fundings", "Initiator");
		titleRowFileds.put("fullreviewind", "");
		titleRowFileds.put("fullreviewide", "Full Board Review");
		titleRowFileds.put("fullreviewother", "");
		titleRowFileds.put("fullreviewtotal", "");
		titleRowFileds.put("expedited", "Expedited");
		titleRowFileds.put("exempt", "Exempt");
		titleRowFileds.put("total", "Total");
		reportResultXml += fillTitleRow(titleRowFileds);

		Map<String, String> summaryFileds = Maps.newLinkedHashMap();
		summaryFileds.put("college", "");
		summaryFileds.put("fundings", "");
		summaryFileds.put("fullreviewind", "Under IND");
		summaryFileds.put("fullreviewide", "Under IDE");
		summaryFileds.put("fullreviewother", "Other");
		summaryFileds.put("fullreviewtotal", "Total");
		summaryFileds.put("expedited", "");
		summaryFileds.put("exempt", "");
		summaryFileds.put("total", "");

		reportResultXml += "<report-items>";
		reportResultXml += "<report-item>";
		reportResultXml += fillSummaryFileds(summaryFileds);

		reportResultXml += "</report-item>";
		Query query = null;
		boolean shaded = false;
		for (Long collegeId : this.getCollegeIds().keySet()) {
			boolean collegeRow = true;
			int allCount[] =  new int[7];
			for (String iniator : this.getInitators()) {
				if(shaded){
					reportResultXml += "<report-item class=\"shaded-row\">";
				}else{
				reportResultXml += "<report-item>";
				}
				reportResultXml += "<field id=\"college\">";
				if(collegeRow){
					
					reportResultXml +=this.getCollegeIds().get(collegeId);
					collegeRow=false;
				}
				reportResultXml +="</field>";
				reportResultXml += "<field id=\"fundings\">";
				reportResultXml +=iniator;
				reportResultXml +="</field>";
				int initiatorTotal =0;
				for (String reviewType : this.getReviewTypes()) {
					
					String reviewTypeCondition =  " meta_data_xml.exist('/protocol/most-recent-study/approval-status/text()[fn:contains(fn:upper-case(.),\""+ reviewType+ "\")]')=1 ";
					reviewTypeCondition= reviewtypeQuery(reviewType, reviewTypeCondition);
					String queryStr = " select count(distinct id) from protocol where  retired = 0 "
							+ " and " +reviewTypeCondition
							+ " and meta_data_xml.exist('/protocol/study-type/text()[fn:contains(fn:upper-case(.),\""
							+ iniator
							+ "\")]')=1 "
							+ queryCriterias+" and "+submittedStudyQueryStr;
					
					if(collegeId!=100000l){
						queryStr+=" and meta_data_xml.exist('/protocol/responsible-department[@collegeid = \""
								+ collegeId + "\"]')=1";
					}else{
						queryStr+=" and meta_data_xml.value('(/protocol/responsible-department/@collegeid)[1]','varchar(255)') in ('3','5','6','9','14','10')";
					}
					
					if (iniator.equals("INVESTIGATOR-INITIATED")) {
						queryStr += " and "+ " meta_data_xml.exist('/protocol/study-type/investigator-initiated/investigator-description/text()[fn:contains(fn:upper-case(.),\"STUDENT-FELLOW-RESIDENT-POST-DOC\")]')=0"; 
					}

					if (iniator.equals("STUDENT-FELLOW-RESIDENT-POST-DOC")) {
						queryStr = queryStr
								.replace("/protocol/study-type/text()",
										"/protocol/study-type/investigator-initiated/investigator-description/text()");
					}
					query = em.createNativeQuery(queryStr);
					int typeTotal  = (int) query.getSingleResult();
					if (reviewType.equals("FULL BOARD")) {
						int fullboradOther  = typeTotal;
						// need to consider ide and ind
						String queryIND = queryStr+underINDCondition;
						String queryIDE = queryStr+underIDECondition;
						query = em.createNativeQuery(queryIND);
						reportResultXml += "<field id=\"fullreviewind\">";
						int queryresult = (int) query.getSingleResult();
						reportResultXml+=""+queryresult;
						reportResultXml+="</field>";
						fullboradOther = fullboradOther -queryresult;
						allCount[0] = allCount[0]+queryresult;
						
						query = em.createNativeQuery(queryIDE);
						reportResultXml += "<field id=\"fullreviewide\">";
						queryresult = (int) query.getSingleResult();
						reportResultXml+=""+queryresult;
						reportResultXml+="</field>";
						fullboradOther = fullboradOther -queryresult;
						allCount[1] = allCount[1]+queryresult;
						reportResultXml += "<field id=\"fullreviewother\">";
						reportResultXml+=""+fullboradOther;
						reportResultXml+="</field>";
						allCount[2] = allCount[2]+fullboradOther;
						allCount[3] = allCount[3]+typeTotal;
					}else if(reviewType.equals("EXPEDITED")) {
						allCount[4] = allCount[4]+typeTotal;
					}else if(reviewType.equals("EXEMPT")) {
						allCount[5] = allCount[5]+typeTotal;
					}
					
					reportResultXml += "<field id=\""
							+ reviewType.toLowerCase() + "\">";
					reportResultXml+=""+typeTotal;
					reportResultXml+="</field>";
					
					initiatorTotal +=typeTotal;

					
				}
				reportResultXml += "<field id=\""
						+ "total" + "\">";
				reportResultXml+=""+initiatorTotal;
				reportResultXml+="</field>";
				reportResultXml += "</report-item>";
				allCount[6] = allCount[6]+initiatorTotal;
			}
			if(shaded){
				reportResultXml += "<report-item class=\"shaded-row\">";
			}else{
				reportResultXml += "<report-item>";
			}
				reportResultXml += "<field>";
				reportResultXml+="</field>";
				reportResultXml += "<field>";
				reportResultXml+="ALL";
				reportResultXml+="</field>";
				for(int i=0;i<allCount.length;i++){
					reportResultXml += "<field>";
					reportResultXml+=""+allCount[i];
					reportResultXml+="</field>";
				}
				reportResultXml += "</report-item>";
				shaded = !shaded;
		}
		reportResultXml += "</report-items>";

		reportResultXml += "</report-result>";
		
		return reportResultXml;
	}
	
	
	/*********
	 * Summary of protocols by type of review and sources of funding
	 * ******/
private String subType2Report(String queryCriterias) {
		String submittedStudyQueryStr = " id not in (select distinct protocol_id from protocol_status where retired = 0 and id in (select max(id) from protocol_status where retired = 0  group by protocol_id) and protocol_status  in ('CANCELLED','DRAFT','PENDING_PL_ENDORSEMENT','PENDING_PI_ENDORSEMENT','PENDING_TP_ENDORSEMENT'))";

		String underINDCondition = " and meta_data_xml.value('(/protocol/summary/drugs-and-devices/ind)[1]','varchar(255)') not in  ('','N/A') ";
		String underIDECondition = " and meta_data_xml.value('(/protocol/summary/drugs-and-devices/ide)[1]','varchar(255)') not in  ('','N/A') ";

		String reportResultXml = "<report-result id=\"" + "" + "\"  created=\""
				+ DateFormatUtil.formateDateToMDY(new Date()) + "\">";

		reportResultXml += "<title>"
				+ "Report Data"
				+ "</title>";
		Map<String, String> titleRowFileds = Maps.newLinkedHashMap();
		titleRowFileds.put("college", "College");
		titleRowFileds.put("fundings", "Source of Funding");
		titleRowFileds.put("fullreviewind", "");
		titleRowFileds.put("fullreviewide", "Full Board Review");
		titleRowFileds.put("fullreviewother", "");
		titleRowFileds.put("fullreviewtotal", "");
		titleRowFileds.put("expedited", "Expedited");
		titleRowFileds.put("exempt", "Exempt");
		titleRowFileds.put("total", "Total");
		reportResultXml += fillTitleRow(titleRowFileds);

		Map<String, String> summaryFileds = Maps.newLinkedHashMap();
		summaryFileds.put("college", "");
		summaryFileds.put("fundings", "");
		summaryFileds.put("fullreviewind", "Under IND");
		summaryFileds.put("fullreviewide", "Under IDE");
		summaryFileds.put("fullreviewother", "Other");
		summaryFileds.put("fullreviewtotal", "Total");
		summaryFileds.put("expedited", "");
		summaryFileds.put("exempt", "");
		summaryFileds.put("total", "");

		reportResultXml += "<report-items>";
		reportResultXml += "<report-item>";
		reportResultXml += fillSummaryFileds(summaryFileds);

		reportResultXml += "</report-item>";
		Set<BigInteger> countedPids = Sets.newHashSet(); 
		Query query =null;
		boolean shaded = false;
		for (Long collegeId : this.getCollegeIds().keySet()) {
			boolean collegeRow = true;
			int allCount[] =  new int[7];
			for (String fundingSource : this.getFundingSources().keySet()) {
				
				if(shaded){
					reportResultXml += "<report-item class=\"shaded-row\">";
				}else{
				reportResultXml += "<report-item>";
				}
				reportResultXml += "<field id=\"college\">";
				if(collegeRow){
					reportResultXml +=this.getCollegeIds().get(collegeId);
					collegeRow=false;
					if(collegeId==100000l){
						countedPids.clear();
					}
				}
				reportResultXml +="</field>";
				reportResultXml += "<field id=\"fundings\">";
				reportResultXml +=fundingSource;
				String fundingSourceQueryStr = "meta_data_xml.value('(/protocol/study-type/investigator-initiated/sub-type/text())[1]','varchar(255)') in"+this.getFundingSources().get(fundingSource);
				if(fundingSource.equals("Federal Agency")){
					fundingSourceQueryStr ="(("+fundingSourceQueryStr+") or (meta_data_xml.exist('/protocol/study-type/text()[fn:contains(fn:upper-case(.),\"COOPERATIVE-GROUP\")]')=1))";
				}else if(fundingSource.equals("Industry")){
					fundingSourceQueryStr ="(("+fundingSourceQueryStr+") or (meta_data_xml.exist('/protocol/study-type/text()[fn:contains(fn:upper-case(.),\"INDUSTRY-SPONSORED\")]')=1))";
				}else if(fundingSource.equals("Student Research")){
					fundingSourceQueryStr ="(meta_data_xml.exist('/protocol/study-type/investigator-initiated/investigator-description/text()[fn:contains(fn:upper-case(.),\"STUDENT-FELLOW-RESIDENT-POST-DOC\")]')=1)";
				}else if(fundingSource.equals("Other")){
					fundingSourceQueryStr ="(("+fundingSourceQueryStr+") or (meta_data_xml.exist('/protocol/study-type/investigator-initiated/sub-type')=0))";
				}
				
				reportResultXml +="</field>";
				int initiatorTotal =0;
				for (String reviewType : this.getReviewTypes()) {
					String reviewTypeCondition =  " meta_data_xml.exist('/protocol/most-recent-study/approval-status/text()[fn:contains(fn:upper-case(.),\""+ reviewType+ "\")]')=1 ";
					reviewTypeCondition= reviewtypeQuery(reviewType, reviewTypeCondition);
					String queryStr = " select id from protocol where retired = 0 "
							+ " and " +reviewTypeCondition
							+ " and "
							+ fundingSourceQueryStr
							+ queryCriterias + " and "+submittedStudyQueryStr;
					
					if(collegeId!=100000l){
						queryStr+=" and meta_data_xml.exist('/protocol/responsible-department[@collegeid = \""
								+ collegeId + "\"]')=1";
					}else{
						queryStr+=" and meta_data_xml.value('(/protocol/responsible-department/@collegeid)[1]','varchar(255)') in ('3','5','6','9','14','10')";
					}

					query = em.createNativeQuery(queryStr);
					List<BigInteger> pids = query.getResultList();
					List<BigInteger> noCountPids = Lists.newArrayList();
					
					for(BigInteger pid : pids){
						if(!countedPids.contains(pid)){
							noCountPids.add(pid);
						}
					}
					
					int typeTotal  = noCountPids.size();
					if (reviewType.equals("FULL BOARD")) {
						int fullboradOther  = typeTotal;
						// need to consider ide and ind
						String queryIND = queryStr+underINDCondition;
						String queryIDE = queryStr+underIDECondition;
						query = em.createNativeQuery(queryIND);
						reportResultXml += "<field id=\"fullreviewind\">";
						pids = query.getResultList();
						List<BigInteger> noCountPidsForFullBoard = Lists.newArrayList();
						for(BigInteger pid : pids){
							if(noCountPids.contains(pid)){
								noCountPidsForFullBoard.add(pid);
							}
						}
						int indStudyNum =noCountPidsForFullBoard.size();
						
						reportResultXml+=""+indStudyNum;
						reportResultXml+="</field>";
						fullboradOther = fullboradOther -indStudyNum;
						allCount[0] = allCount[0]+indStudyNum;
						
						query = em.createNativeQuery(queryIDE);
						reportResultXml += "<field id=\"fullreviewide\">";
						
						pids = query.getResultList();
						noCountPidsForFullBoard.clear();
						for(BigInteger pid : pids){
							if(noCountPids.contains(pid)){
								noCountPidsForFullBoard.add(pid);
							}
						}
						int ideStudyNum = noCountPidsForFullBoard.size();
						
						reportResultXml+=""+ideStudyNum;
						reportResultXml+="</field>";
						fullboradOther = fullboradOther -ideStudyNum;
						allCount[1] = allCount[1]+ideStudyNum;
						reportResultXml += "<field id=\"fullreviewother\">";
						reportResultXml+=""+fullboradOther;
						reportResultXml+="</field>";
						allCount[2] = allCount[2]+fullboradOther;
						allCount[3] = allCount[3]+typeTotal;
					}else if(reviewType.equals("EXPEDITED")) {
						allCount[4] = allCount[4]+typeTotal;
					}else if(reviewType.equals("EXEMPT")) {
						allCount[5] = allCount[5]+typeTotal;
					}
					countedPids.addAll(noCountPids);
					
					reportResultXml += "<field id=\""
							+ reviewType.toLowerCase() + "\">";
					reportResultXml+=""+typeTotal;
					reportResultXml+="</field>";
					
					initiatorTotal +=typeTotal;

					
				}
				reportResultXml += "<field id=\""
						+ "total" + "\">";
				reportResultXml+=""+initiatorTotal;
				reportResultXml+="</field>";
				reportResultXml += "</report-item>";
				allCount[6] = allCount[6]+initiatorTotal;
			}
			if(shaded){
				reportResultXml += "<report-item class=\"shaded-row\">";
			}else{
			reportResultXml += "<report-item>";
			}
				reportResultXml += "<field>";
				reportResultXml+="</field>";
				reportResultXml += "<field>";
				reportResultXml+="ALL";
				reportResultXml+="</field>";
				for(int i=0;i<allCount.length;i++){
					reportResultXml += "<field>";
					reportResultXml+=""+allCount[i];
					reportResultXml+="</field>";
				}
				reportResultXml += "</report-item>";
			shaded = !shaded;
		}
		reportResultXml += "</report-items>";

		reportResultXml += "</report-result>";
		
		return reportResultXml;
	}
	
/*********
 * Summary of protocols by type of review and sources of funding
 * ******/
private String subType3Report(String queryCriterias) {
	
	String underINDCondition = " and meta_data_xml.value('(/protocol/summary/drugs-and-devices/ind)[1]','varchar(255)') not in  ('','N/A') ";
	String underIDECondition = " and meta_data_xml.value('(/protocol/summary/drugs-and-devices/ide)[1]','varchar(255)') not in  ('','N/A') ";

	String reportResultXml = "<report-result id=\"" + "" + "\"  created=\""
			+ DateFormatUtil.formateDateToMDY(new Date()) + "\">";

	reportResultXml += "<title>"
			+ "Report Data"
			+ "</title>";
	Map<String, String> titleRowFileds = Maps.newLinkedHashMap();
	titleRowFileds.put("college", "College");
	titleRowFileds.put("department", "Department");
	titleRowFileds.put("fullreviewind", "");
	titleRowFileds.put("fullreviewide", "Full Board Review");
	titleRowFileds.put("fullreviewother", "");
	titleRowFileds.put("fullreviewtotal", "");
	titleRowFileds.put("expedited", "Expedited");
	titleRowFileds.put("exempt", "Exempt");
	titleRowFileds.put("total", "Total");
	reportResultXml += fillTitleRow(titleRowFileds);

	Map<String, String> summaryFileds = Maps.newLinkedHashMap();
	summaryFileds.put("college", "");
	summaryFileds.put("department", "");
	summaryFileds.put("fullreviewind", "Under IND");
	summaryFileds.put("fullreviewide", "Under IDE");
	summaryFileds.put("fullreviewother", "Other");
	summaryFileds.put("fullreviewtotal", "Total");
	summaryFileds.put("expedited", "");
	summaryFileds.put("exempt", "");
	summaryFileds.put("total", "");

	reportResultXml += "<report-items>";
	reportResultXml += "<report-item>";
	reportResultXml += fillSummaryFileds(summaryFileds);

	reportResultXml += "</report-item>";
	Query query = null;
	boolean shaded = false;
	for (Long collegeId : this.getCollegeIds().keySet()) {
		if(collegeId==100000l){
			continue;
		}
		boolean collegeRow = true;
		int allCount[] =  new int[7];
		
		List<Department> departments = departmentDao.findDeptsByCollegeId(collegeId);
		for (Department department : departments) {
			
			if(shaded){
				reportResultXml += "<report-item class=\"shaded-row\">";
			}else{
			reportResultXml += "<report-item>";
			}
			reportResultXml += "<field id=\"college\">";
			if(collegeRow){
				reportResultXml +=this.getCollegeIds().get(collegeId);
				collegeRow=false;
			}
			reportResultXml +="</field>";
			reportResultXml += "<field id=\"department\">";
			reportResultXml +=department.getName().replace("&", "&amp;");
			reportResultXml +="</field>";
			int initiatorTotal =0;
			for (String reviewType : this.getReviewTypes()) {
				String reviewTypeCondition =  " meta_data_xml.exist('/protocol/most-recent-study/approval-status/text()[fn:contains(fn:upper-case(.),\""+ reviewType+ "\")]')=1 ";
				reviewTypeCondition= reviewtypeQuery(reviewType, reviewTypeCondition);
				String queryStr = " select count(distinct id) from protocol where retired = 0 "
						+" and " +reviewTypeCondition
						+" and meta_data_xml.exist('/protocol/responsible-department[@deptid = \""
						+ department.getId() + "\"]')=1"
						+ queryCriterias;
				
				if(collegeId!=100000l){
					queryStr+=" and meta_data_xml.exist('/protocol/responsible-department[@collegeid = \""
							+ collegeId + "\"]')=1";
				}else{
					queryStr+=" and meta_data_xml.value('(/protocol/responsible-department/@collegeid)[1]','varchar(255)') in ('3','5','6','9','14','10')";
				}

				query = em.createNativeQuery(queryStr);
				int typeTotal  = (int) query.getSingleResult();
				if (reviewType.equals("FULL BOARD")) {
					int fullboradOther  = typeTotal;
					// need to consider ide and ind
					String queryIND = queryStr+underINDCondition;
				    String queryIDE = queryStr+underIDECondition;
					query = em.createNativeQuery(queryIND);
					reportResultXml += "<field id=\"fullreviewind\">";
					int queryresult = (int) query.getSingleResult();
					reportResultXml+=""+queryresult;
					reportResultXml+="</field>";
					fullboradOther = fullboradOther -queryresult;
					allCount[0] = allCount[0]+queryresult;
					
					query = em.createNativeQuery(queryIDE);
					reportResultXml += "<field id=\"fullreviewide\">";
					queryresult = (int) query.getSingleResult();
					reportResultXml+=""+queryresult;
					reportResultXml+="</field>";
					fullboradOther = fullboradOther -queryresult;
					allCount[1] = allCount[1]+queryresult;
					reportResultXml += "<field id=\"fullreviewother\">";
					reportResultXml+=""+fullboradOther;
					reportResultXml+="</field>";
					allCount[2] = allCount[2]+fullboradOther;
					allCount[3] = allCount[3]+typeTotal;
				}else if(reviewType.equals("EXPEDITED")) {
					allCount[4] = allCount[4]+typeTotal;
				}else if(reviewType.equals("EXEMPT")) {
					allCount[5] = allCount[5]+typeTotal;
				}
				
				reportResultXml += "<field id=\""
						+ reviewType.toLowerCase() + "\">";
				reportResultXml+=""+typeTotal;
				reportResultXml+="</field>";
				
				initiatorTotal +=typeTotal;

				
			}
			reportResultXml += "<field id=\""
					+ "total" + "\">";
			reportResultXml+=""+initiatorTotal;
			reportResultXml+="</field>";
			reportResultXml += "</report-item>";
			allCount[6] = allCount[6]+initiatorTotal;
		}
		if(shaded){
			reportResultXml += "<report-item class=\"shaded-row\">";
		}else{
			reportResultXml += "<report-item>";
		}
			reportResultXml += "<field>";
			reportResultXml+="</field>";
			reportResultXml += "<field>";
			reportResultXml+="ALL";
			reportResultXml+="</field>";
			for(int i=0;i<allCount.length;i++){
				reportResultXml += "<field>";
				reportResultXml+=""+allCount[i];
				reportResultXml+="</field>";
			}
			reportResultXml += "</report-item>";
		shaded = !shaded;
	}
	reportResultXml += "</report-items>";

	reportResultXml += "</report-result>";
	
	return reportResultXml;
}


/*********
 * Summary of Protocols Open during (date1) to (date2) – includes those that are enrolling patients and those for which enrollment is completed and follow-up is ongoing
 * ******/
private String subType4Report(String queryCriterias,String date1,String date2) {
	//open action happen before date2 and no close action before date1
	String timeRangeQuery = "id in (select distinct protocol_id from protocol_status where protocol_status ='OPEN' and retired =0 and Datediff(day, '"+date2+"',modified)<0)";
	timeRangeQuery += "and id not in (select distinct protocol_id from protocol_status where protocol_status ='CLOSED' and retired =0 and Datediff(day, '"+date1+"',modified)<0)";
	
	String underINDCondition = " and meta_data_xml.value('(/protocol/summary/drugs-and-devices/ind)[1]','varchar(255)') not in  ('','N/A') ";
	String underIDECondition = " and meta_data_xml.value('(/protocol/summary/drugs-and-devices/ide)[1]','varchar(255)') not in  ('','N/A') ";

	String reportResultXml = "<report-result id=\"" + "" + "\"  created=\""
			+ DateFormatUtil.formateDateToMDY(new Date()) + "\">";

	reportResultXml += "<title>"
			+ "Report Data"
			+ "</title>";
	Map<String, String> titleRowFileds = Maps.newLinkedHashMap();
	titleRowFileds.put("college", "College");
	titleRowFileds.put("fundings", "Initiator");
	titleRowFileds.put("fullreviewind", "");
	titleRowFileds.put("fullreviewide", "Full Board Review");
	titleRowFileds.put("fullreviewother", "");
	titleRowFileds.put("fullreviewtotal", "");
	titleRowFileds.put("expedited", "Expedited");
	titleRowFileds.put("exempt", "Exempt");
	titleRowFileds.put("total", "Total");
	reportResultXml += fillTitleRow(titleRowFileds);

	Map<String, String> summaryFileds = Maps.newLinkedHashMap();
	summaryFileds.put("college", "");
	summaryFileds.put("fundings", "");
	summaryFileds.put("fullreviewind", "Under IND");
	summaryFileds.put("fullreviewide", "Under IDE");
	summaryFileds.put("fullreviewother", "Other");
	summaryFileds.put("fullreviewtotal", "Total");
	summaryFileds.put("expedited", "");
	summaryFileds.put("exempt", "");
	summaryFileds.put("total", "");

	reportResultXml += "<report-items>";
	reportResultXml += "<report-item>";
	reportResultXml += fillSummaryFileds(summaryFileds);

	reportResultXml += "</report-item>";
	Query query = null;
	boolean shaded =  false;
	for (Long collegeId : this.getCollegeIds().keySet()) {
		boolean collegeRow = true;
		int allCount[] =  new int[7];
		for (String iniator : this.getInitators()) {
			
			if(shaded){
				reportResultXml += "<report-item class=\"shaded-row\">";
			}else{
			reportResultXml += "<report-item>";
			}
			reportResultXml += "<field id=\"college\">";
			if(collegeRow){
				
				reportResultXml +=this.getCollegeIds().get(collegeId);
				collegeRow=false;
			}
			reportResultXml +="</field>";
			reportResultXml += "<field id=\"fundings\">";
			reportResultXml +=iniator;
			reportResultXml +="</field>";
			int initiatorTotal =0;
			for (String reviewType : this.getReviewTypes()) {
				String reviewTypeCondition =  " meta_data_xml.exist('/protocol/most-recent-study/approval-status/text()[fn:contains(fn:upper-case(.),\""+ reviewType+ "\")]')=1 ";
				reviewTypeCondition= reviewtypeQuery(reviewType, reviewTypeCondition);
				String queryStr = " select count(distinct id) from protocol where retired = 0 "
						+ " and " +reviewTypeCondition
						+ " and meta_data_xml.exist('/protocol/study-type/text()[fn:contains(fn:upper-case(.),\""
						+ iniator
						+ "\")]')=1 "
						+ " and " +timeRangeQuery+queryCriterias;
				
				if(collegeId!=100000l){
					queryStr+=" and meta_data_xml.exist('/protocol/responsible-department[@collegeid = \""
							+ collegeId + "\"]')=1";
				}else{
					queryStr+=" and meta_data_xml.value('(/protocol/responsible-department/@collegeid)[1]','varchar(255)') in ('3','5','6','9','14','10')";
				}
				
				if (iniator.equals("INVESTIGATOR-INITIATED")) {
					queryStr += " and "+ " meta_data_xml.exist('/protocol/study-type/investigator-initiated/investigator-description/text()[fn:contains(fn:upper-case(.),\"STUDENT-FELLOW-RESIDENT-POST-DOC\")]')=0"; 
				}

				if (iniator.equals("STUDENT-FELLOW-RESIDENT-POST-DOC")) {
					queryStr = queryStr
							.replace("/protocol/study-type/text()",
									"/protocol/study-type/investigator-initiated/investigator-description/text()");
				}
				query = em.createNativeQuery(queryStr);
				int typeTotal  = (int) query.getSingleResult();
				if (reviewType.equals("FULL BOARD")) {
					int fullboradOther  = typeTotal;
					// need to consider ide and ind
					String queryIND = queryStr+underINDCondition;
				    String queryIDE = queryStr+underIDECondition;
					query = em.createNativeQuery(queryIND);
					reportResultXml += "<field id=\"fullreviewind\">";
					int queryresult = (int) query.getSingleResult();
					reportResultXml+=""+queryresult;
					reportResultXml+="</field>";
					fullboradOther = fullboradOther -queryresult;
					allCount[0] = allCount[0]+queryresult;
					
					query = em.createNativeQuery(queryIDE);
					reportResultXml += "<field id=\"fullreviewide\">";
					queryresult = (int) query.getSingleResult();
					reportResultXml+=""+queryresult;
					reportResultXml+="</field>";
					fullboradOther = fullboradOther -queryresult;
					allCount[1] = allCount[1]+queryresult;
					reportResultXml += "<field id=\"fullreviewother\">";
					reportResultXml+=""+fullboradOther;
					reportResultXml+="</field>";
					allCount[2] = allCount[2]+fullboradOther;
					allCount[3] = allCount[3]+typeTotal;
				}else if(reviewType.equals("EXPEDITED")) {
					allCount[4] = allCount[4]+typeTotal;
				}else if(reviewType.equals("EXEMPT")) {
					allCount[5] = allCount[5]+typeTotal;
				}
				
				reportResultXml += "<field id=\""
						+ reviewType.toLowerCase() + "\">";
				reportResultXml+=""+typeTotal;
				reportResultXml+="</field>";
				
				initiatorTotal +=typeTotal;

				
			}
			reportResultXml += "<field id=\""
					+ "total" + "\">";
			reportResultXml+=""+initiatorTotal;
			reportResultXml+="</field>";
			reportResultXml += "</report-item>";
			allCount[6] = allCount[6]+initiatorTotal;
		}
		if(shaded){
			reportResultXml += "<report-item class=\"shaded-row\">";
		}else{
			reportResultXml += "<report-item>";
		}
			reportResultXml += "<field>";
			reportResultXml+="</field>";
			reportResultXml += "<field>";
			reportResultXml+="ALL";
			reportResultXml+="</field>";
			for(int i=0;i<allCount.length;i++){
				reportResultXml += "<field>";
				reportResultXml+=""+allCount[i];
				reportResultXml+="</field>";
			}
			reportResultXml += "</report-item>";
		shaded = !shaded;
	}
	reportResultXml += "</report-items>";

	reportResultXml += "</report-result>";
	
	return reportResultXml;
}


/******
 * Studies Submitted for Continuing Review between (date1) and (date2)
 * ******/
private String subType5Report(String queryCriterias,String date1,String date2) {
	String timeRangeQuery = "select distinct protocol_id from protocol_form where protocol_form_type ='CONTINUING_REVIEW' "
			+ " and retired =0 and "
			+ " id in(select distinct protocol_form_id from protocol_form_status where Datediff(day, '"+date1+"',modified)>0 and Datediff(day, '"+date2+"',modified)<0 "
			+ " and id in (select min(id) from protocol_form_status where retired =0 and protocol_form_status not in ('DRAFT', 'PENDING_PI_ENDORSEMENT', 'CANCELLED')  group by protocol_form_id))";
	List<BigInteger> pids = Lists.newArrayList();
	Query query = em.createNativeQuery(timeRangeQuery);
	try{
		pids = query.getResultList();
		if(pids.size()>0){
		queryCriterias +=" and id in (";
		for(int i=0; i<pids.size();i++){
			if(i<pids.size()-1){
				queryCriterias+="'"+pids.get(i)+"',";
			}else{
				queryCriterias+="'"+pids.get(i)+"')";
			}
		}
		}
	}catch(Exception e){
	}
	String underINDCondition = " and meta_data_xml.value('(/protocol/summary/drugs-and-devices/ind)[1]','varchar(255)') not in  ('','N/A') ";
	String underIDECondition = " and meta_data_xml.value('(/protocol/summary/drugs-and-devices/ide)[1]','varchar(255)') not in  ('','N/A') ";

	String reportResultXml = "<report-result id=\"" + "" + "\"  created=\""
			+ DateFormatUtil.formateDateToMDY(new Date()) + "\">";

	reportResultXml += "<title>"
			+ "Report Data"
			+ "</title>";
	Map<String, String> titleRowFileds = Maps.newLinkedHashMap();
	titleRowFileds.put("college", "College");
	titleRowFileds.put("fundings", "Initiator");
	titleRowFileds.put("fullreviewind", "");
	titleRowFileds.put("fullreviewide", "Full Board Review");
	titleRowFileds.put("fullreviewother", "");
	titleRowFileds.put("fullreviewtotal", "");
	titleRowFileds.put("expedited", "Expedited");
	titleRowFileds.put("exempt", "Exempt");
	titleRowFileds.put("total", "Total");
	reportResultXml += fillTitleRow(titleRowFileds);

	Map<String, String> summaryFileds = Maps.newLinkedHashMap();
	summaryFileds.put("college", "");
	summaryFileds.put("fundings", "");
	summaryFileds.put("fullreviewind", "Under IND");
	summaryFileds.put("fullreviewide", "Under IDE");
	summaryFileds.put("fullreviewother", "Other");
	summaryFileds.put("fullreviewtotal", "Total");
	summaryFileds.put("expedited", "");
	summaryFileds.put("exempt", "");
	summaryFileds.put("total", "");

	reportResultXml += "<report-items>";
	reportResultXml += "<report-item>";
	reportResultXml += fillSummaryFileds(summaryFileds);

	reportResultXml += "</report-item>";
	boolean shaded = false;
	for (Long collegeId : this.getCollegeIds().keySet()) {
		boolean collegeRow = true;
		int allCount[] =  new int[7];
		for (String iniator : this.getInitators()) {
			
			if(shaded){
				reportResultXml += "<report-item class=\"shaded-row\">";
			}else{
			reportResultXml += "<report-item>";
			}
			reportResultXml += "<field id=\"college\">";
			if(collegeRow){
				
				reportResultXml +=this.getCollegeIds().get(collegeId);
				collegeRow=false;
			}
			reportResultXml +="</field>";
			reportResultXml += "<field id=\"fundings\">";
			reportResultXml +=iniator;
			reportResultXml +="</field>";
			int initiatorTotal =0;
			for (String reviewType : this.getReviewTypes()) {
				String reviewTypeCondition =  " meta_data_xml.exist('/protocol/most-recent-study/approval-status/text()[fn:contains(fn:upper-case(.),\""+ reviewType+ "\")]')=1 ";
				reviewTypeCondition= reviewtypeQuery(reviewType, reviewTypeCondition);
				String queryStr = " select count(distinct id) from protocol where retired = 0 "
						+ " and " +reviewTypeCondition
						+ " and meta_data_xml.exist('/protocol/study-type/text()[fn:contains(fn:upper-case(.),\""
						+ iniator
						+ "\")]')=1 "
						+ queryCriterias;
				
				if(collegeId!=100000l){
					queryStr+=" and meta_data_xml.exist('/protocol/responsible-department[@collegeid = \""
							+ collegeId + "\"]')=1";
				}else{
					queryStr+=" and meta_data_xml.value('(/protocol/responsible-department/@collegeid)[1]','varchar(255)') in ('3','5','6','9','14','10')";
				}
				
				
				if (iniator.equals("INVESTIGATOR-INITIATED")) {
					queryStr += " and "+ " meta_data_xml.exist('/protocol/study-type/investigator-initiated/investigator-description/text()[fn:contains(fn:upper-case(.),\"STUDENT-FELLOW-RESIDENT-POST-DOC\")]')=0"; 
				}

				if (iniator.equals("STUDENT-FELLOW-RESIDENT-POST-DOC")) {
					queryStr = queryStr
							.replace("/protocol/study-type/text()",
									"/protocol/study-type/investigator-initiated/investigator-description/text()");
				}
				query = em.createNativeQuery(queryStr);
				int typeTotal  = (int) query.getSingleResult();
				if (reviewType.equals("FULL BOARD")) {
					int fullboradOther  = typeTotal;
					// need to consider ide and ind
					String queryIND = queryStr+underINDCondition;
				    String queryIDE = queryStr+underIDECondition;
					query = em.createNativeQuery(queryIND);
					reportResultXml += "<field id=\"fullreviewind\">";
					int queryresult = (int) query.getSingleResult();
					reportResultXml+=""+queryresult;
					reportResultXml+="</field>";
					fullboradOther = fullboradOther -queryresult;
					allCount[0] = allCount[0]+queryresult;
					
					query = em.createNativeQuery(queryIDE);
					reportResultXml += "<field id=\"fullreviewide\">";
					queryresult = (int) query.getSingleResult();
					reportResultXml+=""+queryresult;
					reportResultXml+="</field>";
					fullboradOther = fullboradOther -queryresult;
					allCount[1] = allCount[1]+queryresult;
					reportResultXml += "<field id=\"fullreviewother\">";
					reportResultXml+=""+fullboradOther;
					reportResultXml+="</field>";
					allCount[2] = allCount[2]+fullboradOther;
					allCount[3] = allCount[3]+typeTotal;
				}else if(reviewType.equals("EXPEDITED")) {
					allCount[4] = allCount[4]+typeTotal;
				}else if(reviewType.equals("EXEMPT")) {
					allCount[5] = allCount[5]+typeTotal;
				}
				
				reportResultXml += "<field id=\""
						+ reviewType.toLowerCase() + "\">";
				reportResultXml+=""+typeTotal;
				reportResultXml+="</field>";
				
				initiatorTotal +=typeTotal;

				
			}
			reportResultXml += "<field id=\""
					+ "total" + "\">";
			reportResultXml+=""+initiatorTotal;
			reportResultXml+="</field>";
			reportResultXml += "</report-item>";
			allCount[6] = allCount[6]+initiatorTotal;
		}
		if(shaded){
			reportResultXml += "<report-item class=\"shaded-row\">";
		}else{
			reportResultXml += "<report-item>";
		}
			reportResultXml += "<field>";
			reportResultXml+="</field>";
			reportResultXml += "<field>";
			reportResultXml+="ALL";
			reportResultXml+="</field>";
			for(int i=0;i<allCount.length;i++){
				reportResultXml += "<field>";
				reportResultXml+=""+allCount[i];
				reportResultXml+="</field>";
			}
			reportResultXml += "</report-item>";
		shaded = !shaded;
	}
	reportResultXml += "</report-items>";

	reportResultXml += "</report-result>";
	
	return reportResultXml;
}

	
/*********
 * Patient Enrollment on Studies Submitted for Continuing Review between (date1) and (date2)
 * ******/
private String subType6Report(String queryCriterias,String date1,String date2) {
		String timeRangeQuery = "select distinct protocol_id from protocol_form where protocol_form_type ='CONTINUING_REVIEW' "
				+ " and retired =0 and "
				+ " id in(select distinct protocol_form_id from protocol_form_status where Datediff(day, '"+date1+"',modified)>0 and Datediff(day, '"+date2+"',modified)<0 "
				+ " and id in (select min(id) from protocol_form_status where retired =0 and protocol_form_status not in ('DRAFT', 'PENDING_PI_ENDORSEMENT', 'CANCELLED')  group by protocol_form_id))";
		List<BigInteger> pids = Lists.newArrayList();
		Query query = em.createNativeQuery(timeRangeQuery);
		try{
			pids = query.getResultList();
			queryCriterias +=" and id in (";
			for(int i=0; i<pids.size();i++){
				if(i<pids.size()-1){
					queryCriterias+="'"+pids.get(i)+"',";
				}else{
					queryCriterias+="'"+pids.get(i)+"')";
				}
			}
		}catch(Exception e){
		}
		
	String underINDCondition = " and meta_data_xml.value('(/protocol/summary/drugs-and-devices/ind)[1]','varchar(255)') not in  ('','N/A') ";
	String underIDECondition = " and meta_data_xml.value('(/protocol/summary/drugs-and-devices/ide)[1]','varchar(255)') not in  ('','N/A') ";

	String reportResultXml = "<report-result id=\"" + "" + "\"  created=\""
			+ DateFormatUtil.formateDateToMDY(new Date()) + "\">";

	reportResultXml += "<title>"
			+ "Report Data"
			+ "</title>";
	Map<String, String> titleRowFileds = Maps.newLinkedHashMap();
	titleRowFileds.put("college", "College");
	titleRowFileds.put("initiators", "Initiator");
	titleRowFileds.put("fullreviewind", "");
	titleRowFileds.put("fullreviewide", "Full Board Review");
	titleRowFileds.put("fullreviewother", "");
	titleRowFileds.put("fullreviewtotal", "");
	titleRowFileds.put("expedited", "Expedited");
	titleRowFileds.put("exempt", "Exempt");
	titleRowFileds.put("total", "Total");
	reportResultXml += fillTitleRow(titleRowFileds);

	Map<String, String> summaryFileds = Maps.newLinkedHashMap();
	summaryFileds.put("college", "");
	summaryFileds.put("initiators", "");
	summaryFileds.put("fullreviewind", "Under IND");
	summaryFileds.put("fullreviewide", "Under IDE");
	summaryFileds.put("fullreviewother", "Other");
	summaryFileds.put("fullreviewtotal", "Total");
	summaryFileds.put("expedited", "");
	summaryFileds.put("exempt", "");
	summaryFileds.put("total", "");

	reportResultXml += "<report-items>";
	reportResultXml += "<report-item>";
	reportResultXml += fillSummaryFileds(summaryFileds);

	reportResultXml += "</report-item>";
	boolean shaded = false;
	for (Long collegeId : this.getCollegeIds().keySet()) {
		boolean collegeRow = true;
		long allCount[] =  new long[7];
		for (String iniator : this.getInitators()) {
			
			if(shaded){
				reportResultXml += "<report-item class=\"shaded-row\">";
			}else{
			reportResultXml += "<report-item>";
			}
			reportResultXml += "<field id=\"college\">";
			if(collegeRow){
				
				reportResultXml +=this.getCollegeIds().get(collegeId);
				collegeRow=false;
			}
			reportResultXml +="</field>";
			reportResultXml += "<field id=\"initiators\">";
			reportResultXml +=iniator;
			reportResultXml +="</field>";
			int initiatorTotal =0;
			for (String reviewType : this.getReviewTypes()) {
				String reviewTypeCondition =  " meta_data_xml.exist('/protocol/most-recent-study/approval-status/text()[fn:contains(fn:upper-case(.),\""+ reviewType+ "\")]')=1 ";
				reviewTypeCondition= reviewtypeQuery(reviewType, reviewTypeCondition);
				String queryStr = " select sum(meta_data_xml.value('(/protocol/summary/irb-determination/subject-accrual/enrollment/local/since-approval/text())[1]','bigint')) "
						+ " from protocol where  meta_data_xml.value('(/protocol/summary/irb-determination/subject-accrual/enrollment/local/since-approval/text())[1]','bigint') is not null "
						+ " and retired = 0 "
						+ " and " +reviewTypeCondition
						+ " and meta_data_xml.exist('/protocol/study-type/text()[fn:contains(fn:upper-case(.),\""
						+ iniator
						+ "\")]')=1 "
						+ queryCriterias;
				
				if(collegeId!=100000l){
					queryStr+=" and meta_data_xml.exist('/protocol/responsible-department[@collegeid = \""
							+ collegeId + "\"]')=1";
				}else{
					queryStr+=" and meta_data_xml.value('(/protocol/responsible-department/@collegeid)[1]','varchar(255)') in ('3','5','6','9','14','10')";
				}
				
				if (iniator.equals("INVESTIGATOR-INITIATED")) {
					queryStr += " and "+ " meta_data_xml.exist('/protocol/study-type/investigator-initiated/investigator-description/text()[fn:contains(fn:upper-case(.),\"STUDENT-FELLOW-RESIDENT-POST-DOC\")]')=0"; 
				}

				if (iniator.equals("STUDENT-FELLOW-RESIDENT-POST-DOC")) {
					queryStr = queryStr
							.replace("/protocol/study-type/text()",
									"/protocol/study-type/investigator-initiated/investigator-description/text()");
				}
				query = em.createNativeQuery(queryStr);
				long typeTotal  = 0;
				try{
					typeTotal= ((BigInteger) query.getSingleResult()).longValue();
				}catch(Exception e){
					
				}
				if (reviewType.equals("FULL BOARD")) {
					long fullboradOther  = typeTotal;
					// need to consider ide and ind
					String queryIND = queryStr+underINDCondition;
				    String queryIDE = queryStr+underIDECondition;
					query = em.createNativeQuery(queryIND);
					reportResultXml += "<field id=\"fullreviewind\">";
					long queryresult =0 ;
					try{
						queryresult= ((BigInteger) query.getSingleResult()).longValue();
					}catch(Exception e){
						
					}
					reportResultXml+=""+queryresult;
					reportResultXml+="</field>";
					fullboradOther = fullboradOther -queryresult;
					allCount[0] = allCount[0]+queryresult;
					
					query = em.createNativeQuery(queryIDE);
					reportResultXml += "<field id=\"fullreviewide\">";
					queryresult =0 ;
					try{
						queryresult= ((BigInteger) query.getSingleResult()).longValue();
					}catch(Exception e){
						
					}
					reportResultXml+=""+queryresult;
					reportResultXml+="</field>";
					fullboradOther = fullboradOther -queryresult;
					allCount[1] = allCount[1]+queryresult;
					reportResultXml += "<field id=\"fullreviewother\">";
					reportResultXml+=""+fullboradOther;
					reportResultXml+="</field>";
					allCount[2] = allCount[2]+fullboradOther;
					allCount[3] = allCount[3]+typeTotal;
				}else if(reviewType.equals("EXPEDITED")) {
					allCount[4] = allCount[4]+typeTotal;
				}else if(reviewType.equals("EXEMPT")) {
					allCount[5] = allCount[5]+typeTotal;
				}
				
				reportResultXml += "<field id=\""
						+ reviewType.toLowerCase() + "\">";
				reportResultXml+=""+typeTotal;
				reportResultXml+="</field>";
				
				initiatorTotal +=typeTotal;

				
			}
			reportResultXml += "<field id=\""
					+ "total" + "\">";
			reportResultXml+=""+initiatorTotal;
			reportResultXml+="</field>";
			reportResultXml += "</report-item>";
			allCount[6] = allCount[6]+initiatorTotal;
		}
		if(shaded){
			reportResultXml += "<report-item class=\"shaded-row\">";
		}else{
			reportResultXml += "<report-item>";
		}
			reportResultXml += "<field>";
			reportResultXml+="</field>";
			reportResultXml += "<field>";
			reportResultXml+="ALL";
			reportResultXml+="</field>";
			for(int i=0;i<allCount.length;i++){
				reportResultXml += "<field>";
				reportResultXml+=""+allCount[i];
				reportResultXml+="</field>";
			}
			reportResultXml += "</report-item>";
		shaded = !shaded;
	}
	reportResultXml += "</report-items>";

	reportResultXml += "</report-result>";
	
	return reportResultXml;
}



/*********
 * Protocols closed to enrollment and follow-up between (date1) and (date2)
 * ******/
private String subType7Report(String queryCriterias,String date1,String date2) {
	String timeRangeQuery = "(id in (select distinct protocol_id from protocol_status where protocol_status ='CLOSED' and retired =0 and Datediff(day, '"+date1+"',modified)>0 and Datediff(day, '"+date2+"',modified)<0 "
			+ ") or id in (select distinct protocol_id from protocol_form where retired = 0 and id in(select protocol_form_id from protocol_form_xml_data where xml_data.value('(/continuing-review/general-study-info/study-status/statuses/status/text())[1]','varchar(100)') in ('research-permanently-closed-limited-to-data','research-permanently-closed-private-identifiable-information') and retired = 0) and id in (select protocol_form_id from protocol_form_status where protocol_form_status in ('EXEMPT_APPROVED','EXPEDITED_APPROVED','IRB_APPROVED') ) and protocol_form_type ='CONTINUING_REVIEW'))";

	String reportResultXml = "<report-result id=\"" + "" + "\"  created=\""
			+ DateFormatUtil.formateDateToMDY(new Date()) + "\">";

	reportResultXml += "<title>"
			+ "Report Data"
			+ "</title>";
	Map<String, String> titleRowFileds = Maps.newLinkedHashMap();
	titleRowFileds.put("college", "College");
	titleRowFileds.put("initiators", "Initiator");
	titleRowFileds.put("fullreview", "Full Board Review");
	titleRowFileds.put("expedited", "Expedited");
	titleRowFileds.put("exempt", "Exempt");
	titleRowFileds.put("total", "Total");
	reportResultXml += fillTitleRow(titleRowFileds);


	reportResultXml += "<report-items>";
	boolean shaded = false;
	for (Long collegeId : this.getCollegeIds().keySet()) {
		boolean collegeRow = true;
		long allCount[] =  new long[4];
		for (String iniator : this.getInitators()) {
			
			if(shaded){
				reportResultXml += "<report-item class=\"shaded-row\">";
			}else{
			reportResultXml += "<report-item>";
			}
			reportResultXml += "<field id=\"college\">";
			if(collegeRow){
				
				reportResultXml +=this.getCollegeIds().get(collegeId);
				collegeRow=false;
			}
			reportResultXml +="</field>";
			reportResultXml += "<field id=\"initiators\">";
			reportResultXml +=iniator;
			reportResultXml +="</field>";
			int initiatorTotal =0;
			Query query = null;
			for (String reviewType : this.getReviewTypes()) {
				String reviewTypeCondition =  "meta_data_xml.exist('/protocol/most-recent-study/approval-status/text()[fn:contains(fn:upper-case(.),\""+ reviewType+ "\")]')=1 ";
				reviewTypeCondition= reviewtypeQuery(reviewType, reviewTypeCondition);
				String queryStr = " select  count(distinct id) "
						+ " from protocol where retired = 0 "
						+ " and " +reviewTypeCondition
						+ " and meta_data_xml.exist('/protocol/study-type/text()[fn:contains(fn:upper-case(.),\""
						+ iniator
						+ "\")]')=1 "
						+ " and " + timeRangeQuery +queryCriterias;
				
				
				if(collegeId!=100000l){
					queryStr+=" and meta_data_xml.exist('/protocol/responsible-department[@collegeid = \""
							+ collegeId + "\"]')=1";
				}else{
					queryStr+=" and meta_data_xml.value('(/protocol/responsible-department/@collegeid)[1]','varchar(255)') in ('3','5','6','9','14','10')";
				}
				
				if (iniator.equals("INVESTIGATOR-INITIATED")) {
					queryStr += " and "+ " meta_data_xml.exist('/protocol/study-type/investigator-initiated/investigator-description/text()[fn:contains(fn:upper-case(.),\"STUDENT-FELLOW-RESIDENT-POST-DOC\")]')=0"; 
				}

				if (iniator.equals("STUDENT-FELLOW-RESIDENT-POST-DOC")) {
					queryStr = queryStr
							.replace("/protocol/study-type/text()",
									"/protocol/study-type/investigator-initiated/investigator-description/text()");
				}
				query = em.createNativeQuery(queryStr);
				
				int typeTotal  = 0;
				try{
					typeTotal= (Integer) query.getSingleResult();
				}catch(Exception e){
					e.printStackTrace();
				}
				
				if (reviewType.equals("FULL BOARD")) {
					allCount[0] = allCount[0]+typeTotal;
				}else if(reviewType.equals("EXPEDITED")) {
					allCount[1] = allCount[1]+typeTotal;
				}else if(reviewType.equals("EXEMPT")) {
					allCount[2] = allCount[2]+typeTotal;
				}
				
				reportResultXml += "<field id=\""
						+ reviewType.toLowerCase() + "\">";
				reportResultXml+=""+typeTotal;
				reportResultXml+="</field>";
				
				initiatorTotal +=typeTotal;

				
			}
			reportResultXml += "<field id=\""
					+ "total" + "\">";
			reportResultXml+=""+initiatorTotal;
			reportResultXml+="</field>";
			reportResultXml += "</report-item>";
			allCount[3] = allCount[3]+initiatorTotal;
		}
		if(shaded){
			reportResultXml += "<report-item class=\"shaded-row\">";
		}else{
			reportResultXml += "<report-item>";
		}
			reportResultXml += "<field>";
			reportResultXml+="</field>";
			reportResultXml += "<field>";
			reportResultXml+="ALL";
			reportResultXml+="</field>";
			for(int i=0;i<allCount.length;i++){
				reportResultXml += "<field>";
				reportResultXml+=""+allCount[i];
				reportResultXml+="</field>";
			}
			reportResultXml += "</report-item>";
			shaded = !shaded;
	}
	reportResultXml += "</report-items>";

	reportResultXml += "</report-result>";
	
	return reportResultXml;
}


/*********
 * Full Board Review Studies Closed between (date1) and (date2) – Reasons for Closure
 * ******/
private String subType8Report(String queryCriterias,String date1,String date2) {
		String timeRangeQuery = "select distinct protocol_id from protocol_status where protocol_status ='CLOSED' and retired =0 and Datediff(day, '"+date1+"',modified)>0 and Datediff(day, '"+date2+"',modified)<0 "
				+ " and protocol_id in (select distinct protocol_id from protocol_form where retired = 0 and protocol_form_type ='STUDY_CLOSURE')";
		List<BigInteger> pids = Lists.newArrayList();
		Query query = em.createNativeQuery(timeRangeQuery);
		try{
			pids = query.getResultList();
			if(!pids.isEmpty()){
			queryCriterias +=" and id in (";
			for(int i=0; i<pids.size();i++){
				if(i<pids.size()-1){
					queryCriterias+="'"+pids.get(i)+"',";
				}else{
					queryCriterias+="'"+pids.get(i)+"')";
				}
			}}
		}catch(Exception e){
		}

	String reportResultXml = "<report-result id=\"" + "" + "\"  created=\""
			+ DateFormatUtil.formateDateToMDY(new Date()) + "\">";

	reportResultXml += "<title>"
			+ "Report Data"
			+ "</title>";
	Map<String, String> titleRowFileds = Maps.newLinkedHashMap();
	titleRowFileds.put("college", "College");
	titleRowFileds.put("initiators", "Initiator");
	titleRowFileds.put("completed", "Study completed as planned");
	titleRowFileds.put("terminatedsafetyconcerns", "Study terminated early due to safety concerns");
	titleRowFileds.put("terminatedslowaccrual", "Study terminated due to slow accrual");
	titleRowFileds.put("sponsorwithdrawal", "Sponsor withdrawal");
	titleRowFileds.put("total", "Total");
	reportResultXml += fillTitleRow(titleRowFileds);
	
	reportResultXml += "<report-items>";
	boolean shaded = false;
	for (Long collegeId : this.getCollegeIds().keySet()) {
		boolean collegeRow = true;
		long allCount[] =  new long[5];
		for (String iniator : this.getInitators()) {
			
			if(shaded){
				reportResultXml += "<report-item class=\"shaded-row\">";
			}else{
			reportResultXml += "<report-item>";
			}
			reportResultXml += "<field id=\"college\">";
			if(collegeRow){
				
				reportResultXml +=this.getCollegeIds().get(collegeId);
				collegeRow=false;
			}
			reportResultXml +="</field>";
			reportResultXml += "<field id=\"initiators\">";
			reportResultXml +=iniator;
			reportResultXml +="</field>";
			int initiatorTotal =0;
			
			for (int i =0;i<closeReasons.size();i++) {
				String closeReason = closeReasons.get(i);
				String queryStr = " select count(distinct id)"
						+ " from protocol where retired = 0 "
						+ " and meta_data_xml.exist('/protocol/most-recent-study/approval-status/text()[fn:contains(fn:upper-case(.),\"FULL BOARD\")]')=1 "
						+ " and meta_data_xml.exist('/protocol/study-type/text()[fn:contains(fn:upper-case(.),\""
						+ iniator
						+ "\")]')=1 "
						+ " and id in (select distinct protocol_id from protocol_form where retired =0 and id in (select distinct protocol_form_id from protocol_form_xml_data where retired=0 and   xml_data.exist('/study-closure/study-status/permanently-closed-to-enrollment/reason/text()[fn:contains(.,\""+closeReason+"\")]')=1))"
						+ queryCriterias;
				if(collegeId!=100000l){
					queryStr+=" and meta_data_xml.exist('/protocol/responsible-department[@collegeid = \""
							+ collegeId + "\"]')=1";
				}else{
					queryStr+=" and meta_data_xml.value('(/protocol/responsible-department/@collegeid)[1]','varchar(255)') in ('3','5','6','9','14','10')";
				}
				
				if (iniator.equals("INVESTIGATOR-INITIATED")) {
					queryStr += " and "+ " meta_data_xml.exist('/protocol/study-type/investigator-initiated/investigator-description/text()[fn:contains(fn:upper-case(.),\"STUDENT-FELLOW-RESIDENT-POST-DOC\")]')=0"; 
				}

				if (iniator.equals("STUDENT-FELLOW-RESIDENT-POST-DOC")) {
					queryStr = queryStr
							.replace("/protocol/study-type/text()",
									"/protocol/study-type/investigator-initiated/investigator-description/text()");
				}
				query = em.createNativeQuery(queryStr);
				long typeTotal  = 0;
				try{
					typeTotal= (int) query.getSingleResult();
				}catch(Exception e){
					e.printStackTrace();
				}
				
				reportResultXml += "<field id=\""
						+  "\">";
				reportResultXml+=""+typeTotal;
				reportResultXml+="</field>";
				 
				initiatorTotal +=typeTotal;
				allCount[i] = allCount[i]+typeTotal;
				
			}
			reportResultXml += "<field id=\""
					+ "total" + "\">";
			reportResultXml+=""+initiatorTotal;
			reportResultXml+="</field>";
			reportResultXml += "</report-item>";
			allCount[4] = allCount[4]+initiatorTotal;
		}
		if(shaded){
			reportResultXml += "<report-item class=\"shaded-row\">";
		}else{
			reportResultXml += "<report-item>";
		}
			reportResultXml += "<field>";
			reportResultXml+="</field>";
			reportResultXml += "<field>";
			reportResultXml+="ALL";
			reportResultXml+="</field>";
			for(int i=0;i<allCount.length;i++){
				reportResultXml += "<field>";
				reportResultXml+=""+allCount[i];
				reportResultXml+="</field>";
			}
			reportResultXml += "</report-item>";
			shaded = !shaded;
	}
	reportResultXml += "</report-items>";

	reportResultXml += "</report-result>";
	
	return reportResultXml;
}


/*********
 * Full Board Review Studies Closed between (date1) and (date2) – Cumulative Patients Enrolled
 * ******/
private String subType9Report(String queryCriterias,String date1,String date2) {
		String timeRangeQuery = "select distinct protocol_id from protocol_status where protocol_status ='CLOSED' and retired =0 and Datediff(day, '"+date1+"',modified)>0 and Datediff(day, '"+date2+"',modified)<0 "
				+ " and protocol_id in (select distinct protocol_id from protocol_form where retired = 0 and protocol_form_type ='STUDY_CLOSURE')";
		List<BigInteger> pids = Lists.newArrayList();
		Query query = em.createNativeQuery(timeRangeQuery);
		try{
			pids = query.getResultList();
			if(!pids.isEmpty()){
			queryCriterias +=" and id in (";
			for(int i=0; i<pids.size();i++){
				if(i<pids.size()-1){
					queryCriterias+="'"+pids.get(i)+"',";
				}else{
					queryCriterias+="'"+pids.get(i)+"')";
				}
			}}
		}catch(Exception e){
		}

	String reportResultXml = "<report-result id=\"" + "" + "\"  created=\""
			+ DateFormatUtil.formateDateToMDY(new Date()) + "\">";

	reportResultXml += "<title>"
			+ "Report Data"
			+ "</title>";
	Map<String, String> titleRowFileds = Maps.newLinkedHashMap();
	titleRowFileds.put("college", "College");
	titleRowFileds.put("initiators", "Initiator");
	titleRowFileds.put("patient", "No.  patients enrolled");
	titleRowFileds.put("metgoal", "No. protocols that met UAMS accrual goal");
	titleRowFileds.put("notmetgoal", "No. protocols that did not meet UAMS accrual goal");
	reportResultXml += fillTitleRow(titleRowFileds);
	
	reportResultXml += "<report-items>";
	boolean shaded = false;
	for (Long collegeId : this.getCollegeIds().keySet()) {
		boolean collegeRow = true;
		long allCount[] =  new long[3];
		for (String iniator : this.getInitators()) {
			
			if(shaded){
				reportResultXml += "<report-item class=\"shaded-row\">";
			}else{
			reportResultXml += "<report-item>";
			}
			reportResultXml += "<field id=\"college\">";
			if(collegeRow){
				
				reportResultXml +=this.getCollegeIds().get(collegeId);
				collegeRow=false;
			}
			reportResultXml +="</field>";
			reportResultXml += "<field id=\"initiators\">";
			reportResultXml +=iniator;
			reportResultXml +="</field>";
			
			for (int i =0;i<3;i++) {
				
				String queryStr = " from protocol where retired = 0 "
						+ " and meta_data_xml.exist('/protocol/most-recent-study/approval-status/text()[fn:contains(fn:upper-case(.),\"FULL BOARD\")]')=1 "
						+ " and meta_data_xml.exist('/protocol/study-type/text()[fn:contains(fn:upper-case(.),\""
						+ iniator
						+ "\")]')=1 "
						+ queryCriterias;
				if(i==0){
					queryStr ="select sum(meta_data_xml.value('(/protocol/summary/irb-determination/subject-accrual/enrollment/local/since-approval/text())[1]','bigint')) " +queryStr +"and meta_data_xml.value('(/protocol/summary/irb-determination/subject-accrual/enrollment/local/since-approval/text())[1]','bigint') is not null ";
				}else if(i==1){
					queryStr = "select count(id) "+queryStr+""+ "and id in (select distinct protocol_id from protocol_form where retired =0 and id in (select distinct protocol_form_id from protocol_form_xml_data where retired=0 and   xml_data.exist('/study-closure/subject-accrual/reach-uams-enrollment-goal/text()[fn:contains(.,\"y\")]')=1))";
				}else if(i==2){
					queryStr = "select count(id) "+queryStr+""+ "and id in (select distinct protocol_id from protocol_form where retired =0 and id in (select distinct protocol_form_id from protocol_form_xml_data where retired=0 and   xml_data.exist('/study-closure/subject-accrual/reach-uams-enrollment-goal/text()[fn:contains(.,\"n\")]')=1))";
				}
				
				if(collegeId!=100000l){
					queryStr+=" and meta_data_xml.exist('/protocol/responsible-department[@collegeid = \""
							+ collegeId + "\"]')=1";
				}else{
					queryStr+=" and meta_data_xml.value('(/protocol/responsible-department/@collegeid)[1]','varchar(255)') in ('3','5','6','9','14','10')";
				}
				
				if (iniator.equals("INVESTIGATOR-INITIATED")) {
					queryStr += " and "+ " meta_data_xml.exist('/protocol/study-type/investigator-initiated/investigator-description/text()[fn:contains(fn:upper-case(.),\"STUDENT-FELLOW-RESIDENT-POST-DOC\")]')=0"; 
				}

				if (iniator.equals("STUDENT-FELLOW-RESIDENT-POST-DOC")) {
					queryStr = queryStr
							.replace("/protocol/study-type/text()",
									"/protocol/study-type/investigator-initiated/investigator-description/text()");
				}
				query = em.createNativeQuery(queryStr);
				long typeTotal  = 0;
				try{
					typeTotal= (int) query.getSingleResult();
				}catch(Exception e){
					//e.printStackTrace();
				}
				
				reportResultXml += "<field id=\""
						+  "\">";
				reportResultXml+=""+typeTotal;
				reportResultXml+="</field>";
				 
				allCount[i] = allCount[i]+typeTotal;
			}
			reportResultXml += "</report-item>";
		}
		if(shaded){
			reportResultXml += "<report-item class=\"shaded-row\">";
		}else{
			reportResultXml += "<report-item>";
		}
			reportResultXml += "<field>";
			reportResultXml+="</field>";
			reportResultXml += "<field>";
			reportResultXml+="ALL";
			reportResultXml+="</field>";
			for(int i=0;i<allCount.length;i++){
				reportResultXml += "<field>";
				reportResultXml+=""+allCount[i];
				reportResultXml+="</field>";
			}
			reportResultXml += "</report-item>";
		shaded = !shaded;
	}
	reportResultXml += "</report-items>";

	reportResultXml += "</report-result>";
	
	return reportResultXml;
}

	public String subType10Report(String queryCriterias) {
		String reportResultXml = "";
		for (String reviewType : this.getReviewTypes()) {
			String reviewTypeCondition = " meta_data_xml.exist('/protocol/most-recent-study/approval-status/text()[fn:contains(fn:upper-case(.),\""
					+ reviewType + "\")]')=1 ";
			reviewTypeCondition = reviewtypeQuery(reviewType,
					reviewTypeCondition);
			String queryStr = " select distinct id from protocol where retired = 0 "
					+ " and "
					+ reviewTypeCondition
					+ " and id>200000"
					+ queryCriterias;

			Query query = em.createNativeQuery(queryStr);
			List<BigInteger> protocolIds = query.getResultList();
			Set<ProtocolForm> pfms = Sets.newHashSet();
			for (BigInteger protocolIdBigInt : protocolIds) {
				try {
					pfms.addAll(protocolFormDao
							.listProtocolFormsByProtocolIdAndProtocolFormType(
									protocolIdBigInt.longValue(),
									ProtocolFormType.NEW_SUBMISSION));
				} catch (Exception e) {

				}
			}
			
			

			reportResultXml += "<report-result id=\"" + "" + "\"  created=\""
					+ DateFormatUtil.formateDateToMDY(new Date()) + "\">";

			reportResultXml += "<title>" + "Process Measures-" + reviewType
					+ "</title>";
			Map<String, String> titleRowFileds = Maps.newLinkedHashMap();
			titleRowFileds.put("title", "");
			titleRowFileds.put("protocolNumber", "No. protocols");
			titleRowFileds.put("25th", "25th percentile");
			titleRowFileds.put("median", "Median");
			titleRowFileds.put("75th", "75th percentile");
			titleRowFileds.put("range", "Range");
			reportResultXml += fillTitleRow(titleRowFileds);
			reportResultXml += "<report-items>";

			List<Integer> initationToClose = initiationToCloseCount(
					queryCriterias, reviewType);
			Map<String, List<Integer>> processes = Maps.newLinkedHashMap();
			/*
			 * List<Committee> irbCommittees = Lists.newArrayList();
			 * for(Committee committee :Arrays.asList(Committee.values())){
			 * if(committee.getDescription().contains("IRB")){
			 * irbCommittees.add(committee);
			 * logger.debug(committee.getDescription()); } }
			 * irbCommittees.add(Committee.DEPARTMENT_CHAIR);
			 * irbCommittees.add(Committee.COLLEGE_DEAN);
			 */
			
			
			processes.put("Total Study Time (Duration from CLARA initiation to study completion) (days) - closed protocols only",
							initationToClose);
			processes.put("Submission Creation Time (Duration from CLARA initiation to CLARA submission) (days)",
							getSummaryTimeFromCreateToSubmission(pfms));
		
			Map<String, List<Integer>> SummaryTimeFromNewSubmissionToIRBSubmission = getSummaryTimeFromClaraSubmissionToIRBSubmission(pfms);
			
			processes.put("Institutional Review Time (Duration from CLARA submission to IRB submission) (days)",SummaryTimeFromNewSubmissionToIRBSubmission.get("TotalTime"));
			processes.put("<![CDATA[<div style='padding-left:32px;'>]]>Institutional Review Time with Reviewers (days)<![CDATA[</div>]]>",SummaryTimeFromNewSubmissionToIRBSubmission.get("ReviewTime"));
			processes.put("<![CDATA[<div style='padding-left:32px;'>]]>Institutional Review Time with PI (days)<![CDATA[</div>]]>",SummaryTimeFromNewSubmissionToIRBSubmission.get("PITime"));

			Map<String, List<Integer>> durationFromOfBudgetLegalCoverageForNSF = getDurationFromOfBudgetLegalCoverageForNSF(pfms);
			processes.put("Budget Review Time (Duration from budget review submission to completion of coverage review, including legal review, if applicable) (days)",durationFromOfBudgetLegalCoverageForNSF.get("TotalTime"));
			processes.put("<![CDATA[<div style='padding-left:32px;'>]]>Budget Review Time with Reviewers (days)<![CDATA[</div>]]>",durationFromOfBudgetLegalCoverageForNSF.get("ReviewTime"));
			processes.put("<![CDATA[<div style='padding-left:32px;'>]]>Budget Review Time with PI (days)<![CDATA[</div>]]>",durationFromOfBudgetLegalCoverageForNSF.get("PITime"));
			
			Map<String, List<Integer>> SummaryTimeFromIRBSubmissionToIRBApproveTime = getSummaryTimeFromIRBSubmissionToIRBApprove(pfms);
			processes.put("IRB Review Time (Duration from IRB submission to IRB approval) (days)",SummaryTimeFromIRBSubmissionToIRBApproveTime.get("TotalTime"));
			processes.put("<![CDATA[<div style='padding-left:32px;'>]]>IRB Review Time with Reviewers (days)<![CDATA[</div>]]>",SummaryTimeFromIRBSubmissionToIRBApproveTime.get("ReviewTime"));
			processes.put("<![CDATA[<div style='padding-left:32px;'>]]>IRB Review Time with PI (days)<![CDATA[</div>]]>",SummaryTimeFromIRBSubmissionToIRBApproveTime.get("PITime"));
			
			String subtitleString = "placeholder";
			for (String process : processes.keySet()) {
				reportResultXml += "<report-item>";
				String mapkey = process;
				if(process.contains(subtitleString)){
					process = process.replace(subtitleString, "");
				}
				reportResultXml += "<field>";
				reportResultXml += process;
				reportResultXml += "</field>";
				List<Integer> reportItemResult = processes.get(mapkey);
				if (reportItemResult != null) {
					if(reportItemResult.size()==1){
						for (int i = 0; i < titleRowFileds.keySet().size() - 1; i++) {
							reportResultXml += "<field>";
							reportResultXml += " ";
							reportResultXml += "</field>";
						}
						subtitleString =process;
					}
					else{
					for (int i = 0; i < titleRowFileds.keySet().size() - 1; i++) {
						if (i == titleRowFileds.keySet().size() - 2) {
							reportResultXml += "<field>";
							if(reportItemResult.get(i)==0&&reportItemResult.get(i+1)==0){
								reportResultXml += "N/A";
							}else{
							reportResultXml += reportItemResult.get(i) + " ~ "
									+ reportItemResult.get(i + 1);
							}
							reportResultXml += "</field>";
						} else {
							reportResultXml += "<field>";
							if(reportItemResult.get(i)==0){
								reportResultXml += "N/A";
							}else{
							reportResultXml += "" + reportItemResult.get(i);
							}
							reportResultXml += "</field>";
						}

					}

				}
				}
				reportResultXml += "</report-item>";
			}

			reportResultXml += "</report-items>";

			reportResultXml += "</report-result>";
		}
		return reportResultXml;
	}

private List<Integer> initiationToCloseCount(String queryCriterias,String reviewType){
	String queryStr = "select Datediff(day, ps1.modified,ps2.modified) from protocol_status as ps1,protocol_status as ps2 where ps1.id in(select min(id) from protocol_status where retired =0 and protocol_id>200000  "
			+ " and protocol_id in(select protocol_id from protocol_status where protocol_status ='CLOSED' and retired =0) "
			+ " and protocol_id in (select distinct  protocol_id from protocol_form where protocol_form_type ='NEW_SUBMISSION' and retired = 0) group by protocol_id ) "
			+ " and ps2.retired =0 "
			+ " and ps1.retired =0 and ps2.protocol_id =ps1.protocol_id "
			+ " and ps2.protocol_status ='CLOSED'"
			+ " and ps1.protocol_id in (select id from protocol where retired = 0 "
				+ " and meta_data_xml.exist('/protocol/most-recent-study/approval-status/text()[fn:contains(fn:upper-case(.),\""
				+ reviewType
				+ "\")]')=1 "
				+ " and id>200000"
				+ queryCriterias+")";
	
	
	Query query = em.createNativeQuery(queryStr);
	List<Integer> results = query.getResultList();
	try{
	return processMeasureComputation(results);
	}catch(Exception e){
		e.printStackTrace();
		return null;
	}
}
private List<Integer> processMeasureComputation(List<Integer> initialList){
	Collections.sort(initialList);
	int medianIndex= initialList.size()/2;
	int quterIndex= initialList.size()/4;
	int quter3Index= initialList.size()*3/4;
	
	List<Integer> results =Lists.newArrayList();
	if(initialList.size()>0){
		int medianValue = 0;
		//Calculate Median Value
		if(initialList.size()%2!=0){
			medianIndex= (initialList.size()-1)/2;
			medianValue = initialList.get(medianIndex);
		}else{
			medianValue = roundUpInt(initialList.get(medianIndex)+initialList.get(medianIndex-1),2);
		}		
		results.add(initialList.size());
		results.add(initialList.get(quterIndex)>0?initialList.get(quterIndex):1);
		results.add(medianValue>0? medianValue:1);
		results.add(initialList.get(quter3Index)>0?initialList.get(quter3Index):1);
		results.add(initialList.get(0)>0?initialList.get(0):1);
		results.add(initialList.get(initialList.size()-1)>0?initialList.get(initialList.size()-1):1);
	}else{
		results.add(0);
		results.add(0);
		results.add(0);
		results.add(0);
		results.add(0);
		results.add(0);
	}
	return results;
	
}

private List<Integer> getSummaryTimeFromCreateToSubmission(Set<ProtocolForm> pfms ){
	List<Integer> tiemForSubmission =Lists.newArrayList();
	List<Integer> results = Lists.newArrayList();
	
	long totalTime = 0;
	int singleTime =0;
	for(ProtocolForm pf:pfms){
		if(pf.getId()!=pf.getParent().getId()){
			continue;
		}
		List<ProtocolFormStatus> pfss = protocolFormStatusDao.getAllProtocolFormStatusByParentFormId(pf.getFormId());
		long startTime = 0;
		long endTime = 0;
		
		List<ProtocolFormStatusEnum> pfssEms = Lists.newArrayList();
		for(int i=0;i<pfss.size();i++){
			pfssEms.add(pfss.get(i).getProtocolFormStatus());
		}
		for(int i=0;i<pfss.size();i++){
			if(pfssEms.contains(ProtocolFormStatusEnum.CANCELLED)&&committeeActions.getDraftFormStatus().contains(pfss.get(pfss.size()-2).getProtocolFormStatus())){
				break;
			}
			ProtocolFormStatus pfs = pfss.get(i);
			
			if (pfs.getProtocolFormStatus().equals(ProtocolFormStatusEnum.DRAFT)&&startTime==0) {
				startTime = pfs.getModified().getTime();
			}else if (!committeeActions.getDraftFormStatus().contains(pfs.getProtocolFormStatus())&&startTime > 0 && endTime==0) {
				endTime = pfs.getModified().getTime();
			} 
			
			//this is the code for time from initiation to irbsubmission
			/*if (pfs.getProtocolFormStatus().equals(ProtocolFormStatusEnum.DRAFT)&&startTime==0) {
				startTime = pfs.getModified().getTime();
			}else if (pfs.getProtocolFormStatus().equals(ProtocolFormStatusEnum.UNDER_IRB_PREREVIEW)&&startTime > 0 && endTime==0) {
				endTime = pfs.getModified().getTime();
			} */
			
			if (startTime > 0 && endTime > 0) {
				singleTime =(int) (1+(endTime - startTime)/(24*60*60*1000));
				totalTime+=singleTime;
				tiemForSubmission.add(singleTime);
				if (totalTime < 0) {
					//logger.debug(protocolFormId + "#######" + totalTime);
				}
				break;
			}
			
			
		}
	}
	
	results = processMeasureComputation(tiemForSubmission);
	return results;
	}

private Map<String,List<Integer>> getSummaryTimeFromIRBSubmissionToIRBApprove(Set<ProtocolForm> pfms){
	List<Integer> tiemForSubmissionofTotal =Lists.newArrayList();
	List<Integer> tiemForSubmissionofReviewer =Lists.newArrayList();
	List<Integer> tiemForSubmissionofPI =Lists.newArrayList();
	Map<String,List<Integer>> results = Maps.newHashMap();
	for(ProtocolForm pf:pfms){
		if(pf.getId()!=pf.getParent().getId()){
			continue;
		}
		
		try{
		List<ProtocolFormStatus> pfss = protocolFormStatusDao.getAllProtocolFormStatusByParentFormId(pf.getFormId());
		
		long reviewTotaltime = 0;
		long SubmissionstartTime = 0;
		boolean firstIRBPrereview =false;
		List<Long> startTimes = Lists.newArrayList();
		List<Long> endTimes = Lists.newArrayList();
		
		//For irb queues only, we should remove revision time of PI
		for(int i=0;i<pfss.size();i++){
			ProtocolFormStatus pfs = pfss.get(i);
			if(pfs.getProtocolFormStatus().equals(ProtocolFormStatusEnum.UNDER_IRB_PREREVIEW)){
				firstIRBPrereview =true;
			}
			
			if(firstIRBPrereview&&(startTimes.size()==endTimes.size())&&committeeActions.getIrbSubmissionStatus().contains(pfs.getProtocolFormStatus())){
				startTimes.add(pfs.getModified().getTime());
				//Start time should from irb assiner 
				if(SubmissionstartTime==0){
					ProtocolFormCommitteeStatus irbStartCommitteeStatus = protocolFormCommitteeStatusDao.listAllByCommitteeAndProtocolFormId(Committee.IRB_ASSIGNER, pf.getFormId()).get(0);
					
					SubmissionstartTime = irbStartCommitteeStatus.getModified().getTime();
				}
				continue;
			}
			if(startTimes.size()>endTimes.size()&&committeeActions.getRevisionRequestedStatus().contains(pfs.getProtocolFormStatus())){
				endTimes.add(pfs.getModified().getTime());
				continue;
			}
			if(startTimes.size()>endTimes.size()&&committeeActions.getIrbApprovalNSFFormStatus().contains(pfs.getProtocolFormStatus())){
				endTimes.add(pfs.getModified().getTime());
				break;
			}
		}
		
		Collections.sort(startTimes);
		Collections.sort(endTimes);
		
		for(int i=0;i<startTimes.size();i++){
			reviewTotaltime += endTimes.get(i)-startTimes.get(i);
		}
		
		if(reviewTotaltime>0){
			tiemForSubmissionofReviewer.add((int) (1+reviewTotaltime/(24*60*60*1000)));
			tiemForSubmissionofPI.add((int) (1+(endTimes.get(endTimes.size()-1)-SubmissionstartTime-reviewTotaltime)/(24*60*60*1000)));
			tiemForSubmissionofTotal.add((int) (1+(endTimes.get(endTimes.size()-1)-SubmissionstartTime)/(24*60*60*1000)));
		}
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	results.put("TotalTime", processMeasureComputation(tiemForSubmissionofTotal));
	results.put("ReviewTime", processMeasureComputation(tiemForSubmissionofReviewer));
	results.put("PITime", processMeasureComputation(tiemForSubmissionofPI));
	
	return results;
}

private Map<String,List<Integer>> getSummaryTimeFromClaraSubmissionToIRBSubmission(Set<ProtocolForm> pfms){
	List<Integer> totalTimes =Lists.newArrayList();
	List<Integer> reviewerTimes =Lists.newArrayList();
	List<Integer> piTimes =Lists.newArrayList();
	Map<String,List<Integer>> results = Maps.newHashMap();
	
	List<ProtocolFormStatusEnum> reviewerStartAction = committeeActions.getReviewTypeBeforeIRB();
	
	for(ProtocolForm pf:pfms){
		if(pf.getId()!=pf.getParent().getId()){
			continue;
		}
		try{
		List<ProtocolFormStatus> pfss = protocolFormStatusDao.getAllProtocolFormStatusByParentFormId(pf.getFormId());

		long totaltime = 0;
		long piStartTime = 0;
		long irbsubmissionTime = 0;
		List<Long> startTimes = Lists.newArrayList();
		List<Long> endTimes = Lists.newArrayList();
		
		//For irb queues only, we should remove revision time of PI
		for(int i=0;i<pfss.size();i++){
			ProtocolFormStatus pfs = pfss.get(i);
			
			if((startTimes.size()==endTimes.size())&&reviewerStartAction.contains(pfs.getProtocolFormStatus())){
				startTimes.add(pfs.getModified().getTime());
				if(piStartTime==0){
					piStartTime = pfs.getModified().getTime();
				}
				continue;
			}
			if(startTimes.size()>endTimes.size()&&committeeActions.getRevisionRequestedStatus().contains(pfs.getProtocolFormStatus())){
				endTimes.add(pfs.getModified().getTime());
				continue;
			}
			if(startTimes.size()>endTimes.size()&&pfs.getProtocolFormStatus().equals(ProtocolFormStatusEnum.PENDING_PI_SIGN_OFF)){
				endTimes.add(pfs.getModified().getTime());
				irbsubmissionTime = pfs.getModified().getTime();
				break;
			}else if(startTimes.size()>endTimes.size()&&pfs.getProtocolFormStatus().equals(ProtocolFormStatusEnum.UNDER_IRB_PREREVIEW)){
				ProtocolFormCommitteeStatus irbStartCommitteeStatus = protocolFormCommitteeStatusDao.listAllByCommitteeAndProtocolFormId(Committee.IRB_ASSIGNER, pf.getFormId()).get(0);
					
				endTimes.add(irbStartCommitteeStatus.getModified().getTime());
				irbsubmissionTime = pfs.getModified().getTime();
				break;
			}
		}
		
		Collections.sort(startTimes);
		Collections.sort(endTimes);
		for(int i=0;i<startTimes.size();i++){
			totaltime += endTimes.get(i)-startTimes.get(i);
		}
		
		if(totaltime>0){
			totalTimes.add((int) (1+(irbsubmissionTime-piStartTime)/(24*60*60*1000)));
			reviewerTimes.add((int) (1+totaltime/(24*60*60*1000)));
			piTimes.add((int) (1+(irbsubmissionTime-piStartTime-totaltime)/(24*60*60*1000)));
		}
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
	results.put("TotalTime", processMeasureComputation(totalTimes));
	results.put("ReviewTime", processMeasureComputation(reviewerTimes));
	results.put("PITime", processMeasureComputation(piTimes));
	return results;
}


private  Map<String,List<Integer>> getSummaryTimeFromSubmissionToIRBApprove(Set<ProtocolForm> pfms){
	List<Integer> tiemForSubmissionofReviewer =Lists.newArrayList();
	List<Integer> tiemForSubmissionofPI =Lists.newArrayList();
	Map<String,List<Integer>> results = Maps.newHashMap();
	List<ProtocolFormStatusEnum> reviewerStartAction = committeeActions.getIrbSubmissionStatus();
	reviewerStartAction.add(ProtocolFormStatusEnum.UNDER_PREREVIEW);
	for(ProtocolForm pf:pfms){
		if(pf.getId()!=pf.getParent().getId()){
			continue;
		}
		try{
		List<ProtocolFormStatus> pfss = protocolFormStatusDao.getAllProtocolFormStatusByParentFormId(pf.getFormId());

		long totaltime = 0;
		long piStartTime = 0;
		List<Long> startTimes = Lists.newArrayList();
		List<Long> endTimes = Lists.newArrayList();
		
		//For irb queues only, we should remove revision time of PI
		for(int i=0;i<pfss.size();i++){
			ProtocolFormStatus pfs = pfss.get(i);
			
			if((startTimes.size()==endTimes.size())&&reviewerStartAction.contains(pfs.getProtocolFormStatus())){
				startTimes.add(pfs.getModified().getTime());
				if(piStartTime==0){
					piStartTime = pfs.getModified().getTime();
				}
				continue;
			}
			if(startTimes.size()>endTimes.size()&&committeeActions.getRevisionRequestedStatus().contains(pfs.getProtocolFormStatus())){
				endTimes.add(pfs.getModified().getTime());
				continue;
			}
			if(startTimes.size()>endTimes.size()&&committeeActions.getIrbApprovalNSFFormStatus().contains(pfs.getProtocolFormStatus())){
				endTimes.add(pfs.getModified().getTime());
				break;
			}
		}
		
		Collections.sort(startTimes);
		Collections.sort(endTimes);
		
		for(int i=0;i<startTimes.size();i++){
			totaltime += endTimes.get(i)-startTimes.get(i);
		}
		
		if(totaltime>0){
			tiemForSubmissionofReviewer.add((int) (1+totaltime/(24*60*60*1000)));
			tiemForSubmissionofPI.add((int) (1+(endTimes.get(endTimes.size()-1)-piStartTime-totaltime)/(24*60*60*1000)));
		}
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
	results.put("ReviewTime", processMeasureComputation(tiemForSubmissionofReviewer));
	results.put("PITime", processMeasureComputation(tiemForSubmissionofPI));
	
	return results;
}

	private Map<String,List<Integer>> getDurationFromOfBudgetLegalCoverageForNSF(
			Set<ProtocolForm> pfms) {
		List<Integer> tiemForSubmissionofReviewer = Lists.newArrayList();
		List<Integer> tiemForSubmissionofPI = Lists.newArrayList();
		List<Integer> tiemForSubmissionofTotal = Lists.newArrayList();
		Map<String,List<Integer>> results = Maps.newHashMap();	
		
		for (ProtocolForm pf : pfms) {
			
			if (pf.getId() != pf.getParent().getId()) {
				continue;
			}
			try {
				List<ProtocolFormStatus> pfss = protocolFormStatusDao
						.getAllProtocolFormStatusByParentFormId(pf.getFormId());
				long piTime = 0;
				long startTime = 0;
				long endTime = 0;
				long piStartTime = 0;
				long piEndTime = 0;
				ProtocolFormStatusEnum piRevisionCuasedActionStatus =null;
				boolean revisionRequested =false;
				for (int i = 0; i < pfss.size(); i++) {
					ProtocolFormStatus pfs = pfss.get(i);
					if (pfs.getProtocolFormStatus().equals(
							ProtocolFormStatusEnum.PENDING_REVIEWER_ASSIGNMENT)
							&& startTime == 0) {
						startTime = pfs.getModified().getTime();
					}
					if (pfs.getProtocolFormStatus()
							.equals(ProtocolFormStatusEnum.UNDER_HOSPITAL_SERVICES_REVIEW)
							&& startTime > 0) {
						endTime = pfs.getModified().getTime();
						int singleTime = (int) (1 + (endTime - startTime-piTime)
								/ (24 * 60 * 60 * 1000));
						tiemForSubmissionofReviewer.add(singleTime);
						tiemForSubmissionofPI.add((int)(1 + (piTime)
								/ (24 * 60 * 60 * 1000)));
						tiemForSubmissionofTotal.add((int) (1 + (endTime - startTime)
								/ (24 * 60 * 60 * 1000)));
						
					}
					
					//calculating pi revision time
					if (pfs.getProtocolFormStatus().equals(
							ProtocolFormStatusEnum.REVISION_REQUESTED)) {
						piStartTime = pfs.getModified().getTime();
						piRevisionCuasedActionStatus = pfss.get(i-1).getProtocolFormStatus();
						revisionRequested = true;
					}
					
					if(revisionRequested&&pfs.getProtocolFormStatus().equals(piRevisionCuasedActionStatus)){
						piEndTime = pfs.getModified().getTime();
						piTime += piEndTime- piStartTime;
						piStartTime=0;
						piEndTime=0;
					}
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		results.put("TotalTime", processMeasureComputation(tiemForSubmissionofTotal));
		results.put("ReviewTime", processMeasureComputation(tiemForSubmissionofReviewer));
		results.put("PITime", processMeasureComputation(tiemForSubmissionofPI));

		return results;
	}

	/*private List<Integer> getTimeInEachQueueByProtocolId(
			Set<ProtocolForm> pfms, List<Committee> committees) {

		List<Integer> results = Lists.newArrayList();
		for (ProtocolForm pf : pfms) {
			if (pf.getId() != pf.getParent().getId()) {
				continue;
			}
			long totalTime = 0;
			try {
				for (Committee committee : committees) {
					long startTime = 0;
					long endTime = 0;
					List<ProtocolFormCommitteeStatus> pfcss = protocolFormCommitteeStatusDao
							.listAllByCommitteeAndProtocolFormId(committee,
									pf.getFormId());
					for (int i = 0; i < pfcss.size(); i++) {
						ProtocolFormCommitteeStatus pfcs = pfcss.get(i);
						List<ProtocolFormCommitteeStatusEnum> startActions = committeeActions
								.getStartCommitteeStatusMapForHSR().get(
										committee);
						List<ProtocolFormCommitteeStatusEnum> endActions = committeeActions
								.getEndCommitteeStatusMapForHSR()
								.get(committee);
						if (startActions == null) {
							continue;
						}
						if (startActions.contains(pfcs
								.getProtocolFormCommitteeStatus())) {
							startTime = pfcs.getModified().getTime();

							// change the start time to agenda approved
							if (committee.equals(Committee.IRB_REVIEWER)) {
								try{
								AgendaStatus agendaStatus = null;
								try{
									agendaStatus = agendaStatusDao
											.getAgendaStatusByAgendaStatusAndProtocolFormId(
													AgendaStatusEnum.AGENDA_APPROVED,
													pfcs.getProtocolFormId());
								}catch(Exception ex){
									agendaStatus = agendaStatusDao
											.getAgendaStatusByAgendaStatusAndProtocolFormIdAndAgendaItemStatus(
													AgendaStatusEnum.AGENDA_APPROVED,
													pfcs.getProtocolFormId(),AgendaItemStatus.REMOVED,true);}
									startTime = agendaStatus.getModified()
											.getTime();
								} catch (Exception e) {
									startTime = 0;
								}
							}

						} else if (startTime > 0
								&& endActions.contains(pfcs
										.getProtocolFormCommitteeStatus())) {
							endTime = pfcs.getModified().getTime();
							if (committee.equals(Committee.IRB_OFFICE)
									&& pfcs.getProtocolFormCommitteeStatus()
											.equals(ProtocolFormCommitteeStatusEnum.IRB_AGENDA_ASSIGNED)) {
								try{
								AgendaStatus agendaStatus = null;
								try{
									agendaStatus = agendaStatusDao
											.getAgendaStatusByAgendaStatusAndProtocolFormId(
													AgendaStatusEnum.AGENDA_APPROVED,
													pfcs.getProtocolFormId());
								}catch(Exception ex){
									agendaStatus = agendaStatusDao
											.getAgendaStatusByAgendaStatusAndProtocolFormIdAndAgendaItemStatus(
													AgendaStatusEnum.AGENDA_APPROVED,
													pfcs.getProtocolFormId(),AgendaItemStatus.REMOVED,true);}
									endTime = agendaStatus.getModified()
											.getTime();
								} catch (Exception e) {
									if (i != (pfcss.size() - 1)) {
										if (pfcss
												.get(i + 1)
												.getProtocolFormCommitteeStatus()
												.equals(ProtocolFormCommitteeStatusEnum.PENDING_IRB_REVIEW_RE_ASSIGNMENT)) {
											endTime = 0;
											continue;
										}
									}
									endTime = new Date().getTime();
								}
							}

						}

						if (startTime > 0 && endTime == 0
								&& i == (pfcss.size() - 1)) {
							endTime = new Date().getTime();
						}

						if (startTime > 0 && endTime > 0) {
							totalTime += endTime - startTime;
							startTime = 0;
							endTime = 0;

						}
					}

				}
				if (totalTime > 0) {
					totalTime = 1 + totalTime / (24 * 60 * 60 * 1000);
					if(totalTime==37){
						logger.debug(""+pf.getId()+pf.getProtocol().getId());
					}
					results.add((int) totalTime);
				}

			} catch (Exception e) {
			}

		}

		results = processMeasureComputation(results);

		return results;
	}*/

	
	private int roundUpInt(int a,int b) {
		return(((double)a/(double)b)>(a/b)?a/b+1:a/b);
	}


	@Override
	public String generateReportResult(ReportTemplate reportTemplate) {
		
		List<ReportCriteria> criterias = reportTemplate.getReportCriterias();

		ObjectMapper objectMapper = new ObjectMapper();

		Map<String, String> fieldsRealXPathMap = Maps.newHashMap();
		Map<String, String> queryCriteriasValueMap = Maps.newHashMap();

		String date1 = "1940-10-10";
		String date2 = "2060-10-10";
		for (ReportCriteria rc : criterias) {
			ReportFieldTemplate reportCriteriaField = new ReportFieldTemplate();

			try {
				reportCriteriaField = objectMapper.readValue(rc.getCriteria(),
						ReportFieldTemplate.class);

				String fieldIdentifier = reportCriteriaField
						.getFieldIdentifier();

				String value = reportCriteriaField.getValue();
				
				if (value != null && !value.isEmpty()) {
					String realXpath = "";

					if (reportCriteriaField.getOperator().toString()
							.equals("BEFORE")
							) {
						date2 = value;
						queryCriteriasValueMap.put(
								"Time Span",
								"BEFORE: " + value);
						realXpath = reportCriteriaField.getNodeXPath().replace("{value}", "'"+ value.toUpperCase() +"'");
					} else if(reportCriteriaField.getOperator().toString()
							.equals("AFTER")){
						date1 = value;
						queryCriteriasValueMap.put(
								"Time Span",
								"AFTER: " + value);
						realXpath = reportCriteriaField.getNodeXPath().replace("{value}", "'"+ value.toUpperCase() +"'");
					} else if (reportCriteriaField.getOperator().toString()
					
							.equals("BETWEEN")) {
						 date1 = value.toUpperCase().substring(0,value.toUpperCase().indexOf(","));
						 date2 = value.toUpperCase().substring(value.toUpperCase().indexOf(",") + 1,value.length());
						 queryCriteriasValueMap.put(
									"Time Span",
									"BETWEEN: " + date1+"~"+date2);
						 
						 realXpath =reportCriteriaField.getNodeXPath().replace("{value}", "'"+ value.toUpperCase().substring(0,value.toUpperCase().indexOf(",")) +"'");
						 realXpath = realXpath.replace("{operator}", ">");
						 realXpath = realXpath +" AND "+reportCriteriaField.getNodeXPath().replace("{value}", "'"+ value.toUpperCase().substring(value.toUpperCase().indexOf(",")+1,value.length()) +"'");;
						 realXpath = realXpath.replace("{operator}", "<");
					} else {
						date1 ="";
						queryCriteriasValueMap.put(
								reportCriteriaField.getFieldDisplayName(),
								reportCriteriaField.getDisplayValue());
						if (value.contains("|")) {
							String[] values = value.split("\\|");
							realXpath += "(";
							for (int i = 0; i < values.length; i++) {
								if (i > 0) {
									realXpath += " OR ";
								}
								if (reportCriteriaField.getNodeXPath()
										.contains(".exist")
										|| reportCriteriaField.getNodeXPath()
												.contains(".value")) {
									if (value.equals("=1")
											|| value.equals("=0")) {
										realXpath += reportCriteriaField
												.getNodeXPath()
												.replace("{value}",
														values[i].toUpperCase());
									} else {
										realXpath += reportCriteriaField
												.getNodeXPath()
												.replace(
														"{value}",
														"\""
																+ values[i]
																		.toUpperCase()
																+ "\"");
									}
								} else if (values[i].contains("'")) {
									realXpath += reportCriteriaField
											.getNodeXPath().replace("{value}",
													values[i].toUpperCase());
								} else {
									realXpath += reportCriteriaField
											.getNodeXPath()
											.replace(
													"{value}",
													"'"
															+ values[i]
																	.toUpperCase()
															+ "'");
								}
							}
							realXpath += ")";
						} else {
							if (reportCriteriaField.getNodeXPath().contains(
									".exist")
									|| reportCriteriaField.getNodeXPath()
											.contains(".value")) {
								if (value.equals("=1") || value.equals("=0")) {
									realXpath = reportCriteriaField
											.getNodeXPath().replace("{value}",
													value.toUpperCase());
								} else {
									realXpath = reportCriteriaField
											.getNodeXPath().replace(
													"{value}",
													"\"" + value.toUpperCase()
															+ "\"");
								}
								;
							} else if (value.contains("'")) {
								realXpath = reportCriteriaField
										.getNodeXPath()
										.replace("{value}", value.toUpperCase());
							} else if(value.toUpperCase().equals("IN")||value.toUpperCase().equals("NOT IN")){
								realXpath = reportCriteriaField.getNodeXPath().replace("{value}", value);
							}else {
								realXpath = reportCriteriaField
										.getNodeXPath()
										.replace("{value}",
												"'" + value.toUpperCase() + "'");
							}
						}

					}

					//date range donot need to add query conditions here
					if(!date1.isEmpty()&&!fieldIdentifier.equals("submissiontimespan")){
						continue;
					}
					if(!reportCriteriaField.getOperator().toString().equals("BETWEEN")){
						realXpath = realXpath.replace("{operator}", reportCriteriaField.getOperator().getRealOperator());
					}
					fieldsRealXPathMap.put("{" + fieldIdentifier
							+ ".search-xpath}", realXpath);
					// fieldsRealXPathMap.put("{" + fieldIdentifier +
					// ".report-xpath}", reportField.getReportableXPath());
				}
			} catch (JsonParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JsonMappingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		String rawQeury = generateRawQeury(reportTemplate, fieldsRealXPathMap);
		String queryCriterias = fillMessage(rawQeury, fieldsRealXPathMap);
		if(fieldsRealXPathMap.isEmpty()){
			queryCriterias= queryCriterias.replace("AND", "");
		} else{
			queryCriterias = " and "+queryCriterias;
		}
		String finalResultXml = "<report-results>";
		finalResultXml = finalResultXml+generateSummaryCriteriaTable(reportTemplate,
				queryCriteriasValueMap);
		
		logger.debug(queryCriterias);
			switch(reportTemplate.getTypeDescription()){
			case "Human Subject Research Dashboard-Summary of protocols by type of review and initiator":finalResultXml+=subType1Report(queryCriterias);
			break;
			case "Human Subject Research Dashboard-Summary of protocols by type of review and sources of funding":finalResultXml+=subType2Report(queryCriterias);
			break;
			case "Human Subject Research Dashboard-Summary of Principal Investigators":finalResultXml+=subType3Report(queryCriterias);
			break;
			case "Human Subject Research Dashboard-Summary of Protocols Open":finalResultXml+=subType4Report(queryCriterias,date1,date2);
			break;
			case "Human Subject Research Dashboard-Studies Submitted for Continuing Review":finalResultXml+=subType5Report(queryCriterias,date1,date2);
			break;
			case "Human Subject Research Dashboard-Patient Enrollment on Studies Submitted for Continuing Review":finalResultXml+=subType6Report(queryCriterias,date1,date2);
			break;
			case "Human Subject Research Dashboard-Protocols closed to enrollment and follow-up":finalResultXml+=subType7Report(queryCriterias,date1,date2);
			break;
			case "Human Subject Research Dashboard-Full Board Review Studies Closed-Reasons for Closure":finalResultXml+=subType8Report(queryCriterias,date1,date2);
			break;
			case "Human Subject Research Dashboard-Full Board Review Studies Closed-Cumulative Patients Enrolled":finalResultXml+=subType9Report(queryCriterias,date1,date2);
			break;
			case "Human Subject Research Dashboard-Process Measures":finalResultXml+=subType10Report(queryCriterias);
			break;
			}
		finalResultXml += "</report-results>";
		//finalResultXml += subType8Report(queryCriterias,"2finalResultXml+=subType1Report(queryCriterias);alResultXml += "</report-results>";
		logger.debug(finalResultXml);
		return finalResultXml;
	}

	public EntityManager getEm() {
		return em;
	}

	@PersistenceContext(unitName = "defaultPersistenceUnit")
	public void setEm(EntityManager em) {
		this.em = em;
	}

	public Map<Long,String> getCollegeIds() {
		return collegeIds;
	}


	public List<String> getInitators() {
		return initators;
	}


	public List<String> getReviewTypes() {
		return reviewTypes;
	}

	public Map<String,String> getFundingSources() {
		return fundingSources;
	}
	
	public List<String> getCloseReasons() {
		return closeReasons;
	}

	public DepartmentDao getDepartmentDao() {
		return departmentDao;
	}

	@Autowired(required=true)
	public void setDepartmentDao(DepartmentDao departmentDao) {
		this.departmentDao = departmentDao;
	}

	public ProtocolFormStatusDao getProtocolFormStatusDao() {
		return protocolFormStatusDao;
	}

	@Autowired(required=true)
	public void setProtocolFormStatusDao(ProtocolFormStatusDao protocolFormStatusDao) {
		this.protocolFormStatusDao = protocolFormStatusDao;
	}

	public ProtocolFormDao getProtocolFormDao() {
		return protocolFormDao;
	}

	@Autowired(required=true)
	public void setProtocolFormDao(ProtocolFormDao protocolFormDao) {
		this.protocolFormDao = protocolFormDao;
	}

	public ProtocolDao getProtocolDao() {
		return protocolDao;
	}
	
	@Autowired(required=true)
	public void setProtocolDao(ProtocolDao protocolDao) {
		this.protocolDao = protocolDao;
	}

	public ProtocolFormCommitteeStatusDao getProtocolFormCommitteeStatusDao() {
		return protocolFormCommitteeStatusDao;
	}

	@Autowired(required=true)
	public void setProtocolFormCommitteeStatusDao(
			ProtocolFormCommitteeStatusDao protocolFormCommitteeStatusDao) {
		this.protocolFormCommitteeStatusDao = protocolFormCommitteeStatusDao;
	}

	public AgendaStatusDao getAgendaStatusDao() {
		return agendaStatusDao;
	}

	@Autowired(required=true)
	public void setAgendaStatusDao(AgendaStatusDao agendaStatusDao) {
		this.agendaStatusDao = agendaStatusDao;
	}

	

}
