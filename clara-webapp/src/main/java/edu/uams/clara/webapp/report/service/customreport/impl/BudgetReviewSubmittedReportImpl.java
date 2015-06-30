package edu.uams.clara.webapp.report.service.customreport.impl;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;

import edu.uams.clara.webapp.common.util.DateFormatUtil;
import edu.uams.clara.webapp.protocol.dao.protocolform.ProtocolFormDao;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolForm;
import edu.uams.clara.webapp.report.domain.ReportCriteria;
import edu.uams.clara.webapp.report.domain.ReportFieldTemplate;
import edu.uams.clara.webapp.report.domain.ReportTemplate;
import edu.uams.clara.webapp.report.service.customreport.CustomReportService;

public class BudgetReviewSubmittedReportImpl extends CustomReportService {
	private final static Logger logger = LoggerFactory.getLogger(BudgetReviewSubmittedReportImpl.class);

	private EntityManager em;
	private ProtocolFormDao protocolFormDao;
	
	@Override
	public String generateReportResult(ReportTemplate reportTemplate) {
		List<ReportCriteria> criterias = reportTemplate.getReportCriterias();
		String finalResultXml = "<report-results>";
		
		ObjectMapper objectMapper = new ObjectMapper();
			
		Map<String, String> fieldsRealXPathMap = Maps.newHashMap();
		Map<String, String> queryCriteriasValueMap = Maps.newHashMap();
		String date1 = "1990-10-10";
		String date2 = "2020-10-10";
		
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
		finalResultXml = finalResultXml+generateSummaryCriteriaTable(reportTemplate,
				queryCriteriasValueMap);
		finalResultXml = processTitleLineInfo(finalResultXml, reportTemplate);
		finalResultXml = processReportData(date1,date2,finalResultXml);
		
		finalResultXml += "</report-items>";
		finalResultXml += "</report-result>";
		finalResultXml += "</report-results>";
		finalResultXml =finalResultXml.replace("<![CDATA[null]]>", "");
		finalResultXml =finalResultXml.replace("null&lt;br&gt;", "");
		finalResultXml =finalResultXml.replace("&gt;null", "&gt;");
		finalResultXml =finalResultXml.replace("null", "");
		if(finalResultXml.contains("&")){
			finalResultXml=finalResultXml.replaceAll("&", "&amp;");
			}
		logger.debug(finalResultXml);
		return finalResultXml;
		
	}
	
	private String processTitleLineInfo(String finalResultXml, ReportTemplate reportTemplate){
		finalResultXml += "<report-result id=\""+ reportTemplate.getTypeDescription() +"\"  created=\""+ DateFormatUtil.formateDateToMDY(new Date()) +"\">";
		finalResultXml += "<title>"+ reportTemplate.getDescription() +"</title>";
		finalResultXml += "<fields>";
		finalResultXml += "<field id=\"protocolid\" desc=\"IRB #\" hidden=\"false\" />";
		finalResultXml += "<field id=\"title\" desc=\"Title\" hidden=\"false\" />";
		finalResultXml += "<field id=\"piname\" desc=\"PI Name\" hidden=\"false\" />";
		finalResultXml += "<field id=\"department\" desc=\"Department\" hidden=\"false\" />";
		finalResultXml += "<field id=\"sponsor\" desc=\"Sponsor\" hidden=\"false\" />";
		finalResultXml += "<field id=\"claraencrystart\" desc=\"Date CLARA Entry Started\" hidden=\"false\" />";
		finalResultXml += "<field id=\"initialclarasub\" desc=\"Date Initial CLARA Submission\" hidden=\"false\" />";
		finalResultXml += "<field id=\"subtobudget\" desc=\"Date CLARA Submission for B/C Review\" hidden=\"false\" />";
		finalResultXml += "<field id=\"reviewerassigned\" desc=\"Date B/C Reviewers Assigned\" hidden=\"false\" />";
		finalResultXml += "<field id=\"budgetneo\" desc=\"Date Budget Sent for Negotiations\" hidden=\"false\" />";
		finalResultXml += "<field id=\"revisiondate\" desc=\"Dates Protocol Was Sent Back to PI for Revisions\" hidden=\"false\" />";
		finalResultXml += "<field id=\"converageapproved\" desc=\"Date Route to Hospital\" hidden=\"false\" />";	
		finalResultXml += "<field id=\"budgetapproved\" desc=\"Date Budget Approved\" hidden=\"false\" />";
		finalResultXml += "<field id=\"irbprereview\" desc=\"Date sent to Pre-IRB Review\" hidden=\"false\" />";
		finalResultXml += "<field id=\"irbapproval\" desc=\"Date IRB Approval\" hidden=\"false\" />";	
	
		finalResultXml += "</fields>";
		finalResultXml += "<report-items>";
		return finalResultXml;
		
	}
	
	private String processReportData(String startDate, String endDate, String xmlResult){
		String queryStrForCommitteeStatus = "select protocol_form_id, modified from protocol_form_committee_status  where retired = 0 and id in (select min(id) from protocol_form_committee_status where id in (select id from protocol_form_committee_status  where retired = 0 and protocol_form_committee_status = 'IN_REVIEW' and committee = 'BUDGET_MANAGER' and protocol_form_id in (select  distinct id  from protocol_form where retired =0 and protocol_form_type = 'NEW_SUBMISSION') "
				+ " and modified > '"+startDate+"' and modified < '"+endDate+"')  group by protocol_form_id)";
		logger.debug(queryStrForCommitteeStatus);
		Query query = em.createNativeQuery(queryStrForCommitteeStatus);
		List<Object[]> committeeQueryResults = (List<Object[]>) query.getResultList();
		
		for(Object[] result:committeeQueryResults){
			try{
			xmlResult += "<report-item>";
			BigInteger pfIdBig = (BigInteger)result[0];
			long pfId = pfIdBig.longValue();
			
			Date submittedToBudgetDate  = (Date) result[1];
			String submittedToBudgetDateStr = DateFormatUtil.formateDateToMDY(submittedToBudgetDate);
			
			ProtocolForm pf= protocolFormDao.findById(pfId);
			long pid = pf.getProtocol().getId();
			
			xmlResult += "<field id=\"protocolid\">";
			xmlResult +=  "<![CDATA[<a target=\"_blank\" href=\""+this.getAppHost()+"/clara-webapp/protocols/"+pid+"/dashboard\">"+pid+"</a>]]>";

			xmlResult += "</field>";
			
			
			String getResultinfoQuery = "SELECT distinct meta_data_xml.value('(/protocol/title/text())[1]','varchar(max)') as protocalTitle , meta_data_xml.value('(/protocol/staffs/staff/user[roles/role[fn:contains(fn:upper-case(.),\"PRINCIPAL INVESTIGATOR\")]]/lastname/text())[1]','varchar(50)')+ ',' +meta_data_xml.value('(/protocol/staffs/staff/user[roles/role[fn:contains(fn:upper-case(.),\"PRINCIPAL INVESTIGATOR\")]]/firstname/text())[1]','varchar(50)') as piName , meta_data_xml.value('(/protocol/responsible-department/@deptdesc)[1]','varchar(max)') as department, created as createddate, meta_data_xml.value('(/protocol/original-study/submit-date/text())[1]','varchar(max)') as initialsubmittedDate, meta_data_xml.value('(/protocol/original-study/approval-date/text())[1]','varchar(100)') as originalApprovaldate,meta_data_xml.value('(/protocol/summary/budget-determination/approval-date/text())[1]','varchar(100)') as budgetapproveddate, T2.Loc.value('./@amount','varchar(max)')+','+T2.Loc.value('./@type','varchar(max)')+','+T2.Loc.value('./@entityname','varchar(max)') as funding FROM protocol  OUTER APPLY meta_data_xml.nodes('//protocol/funding/funding-source') as T2(Loc)  WHERE"
					+ " id =" +pid;
			
			query = em.createNativeQuery(getResultinfoQuery);
			List<Object[]> infoResults = (List<Object[]>) query.getResultList();
			
			String title = "";
			String piname = "";
			String department = "";
			String protocolCreatedDate = "";
			String initialSubmitedDate = "";
			String originalApprovaldate = "";
			String budgetApprovaldate = "";
			String sponsors = "";
			String budgetReviewAssignedDate = "";
			String senttoIRBPreDate = "";
			String coverageApprovedDate = "";
			String budgetNeoDate = "";
			String revisionDate = "";
			
			
			for(int i =0;i<infoResults.size();i++){
				Object[] infoResult = infoResults.get(i);
				sponsors = "<list>";
				
				if(i==0){
					title = (String)infoResult[0];
					piname = (String)infoResult[1];
					department = (String)infoResult[2];
					protocolCreatedDate= DateFormatUtil.formateDateToMDY((Date)infoResult[3]);
					initialSubmitedDate = (String)infoResult[4];
					originalApprovaldate = (String)infoResult[5];
					budgetApprovaldate = (String)infoResult[6];
				}
				
				sponsors += "<item>"+ (String)infoResult[7] +"</item>";
				
				if(i==infoResults.size()-1){
					sponsors += "</list>";
				}
			}
			
			String budgetReviewAssignedDateQry = "select modified from protocol_form_committee_status where retired = 0 and id in (select min(id) from protocol_form_committee_status where id in "
					+ "(select id from protocol_form_committee_status where retired = 0 and protocol_form_committee_status = 'REVIEWER_ASSIGNED' and committee = 'BUDGET_MANAGER' "
					+ "and protocol_form_id in (select id from protocol_form where retired = 0 and parent_id = "+pf.getParent().getId()+")))";
 
			query = em.createNativeQuery(budgetReviewAssignedDateQry);
			
			try{
				 budgetReviewAssignedDate = DateFormatUtil.formateDateToMDY((Date)query.getSingleResult());
			}catch(Exception e){
				
			}
			
			String senttoIRBPreDateQry = "select modified from protocol_form_committee_status where retired = 0 and id in (select min(id) from protocol_form_committee_status where id in "
					+ "(select id from protocol_form_committee_status where retired = 0 and protocol_form_committee_status = 'IN_REVIEW' and committee = 'IRB_PREREVIEW' "
					+ "and protocol_form_id in (select id from protocol_form where retired = 0 and parent_id = "+pf.getParent().getId()+")))";
			
			query = em.createNativeQuery(senttoIRBPreDateQry);
			try{
				senttoIRBPreDate = DateFormatUtil.formateDateToMDY((Date)query.getSingleResult());
			}catch(Exception e){
				
			}
			
			
			String coverageApprovedDateQry = "select modified from protocol_form_committee_status where retired = 0 and id in (select min(id) from protocol_form_committee_status where id in "
					+ "(select id from protocol_form_committee_status where retired = 0 and protocol_form_committee_status = 'APPROVED' and committee = 'COVERAGE_REVIEW' "
					+ "and protocol_form_id in (select id from protocol_form where retired = 0 and parent_id = "+pf.getParent().getId()+")))";
 
			query = em.createNativeQuery(coverageApprovedDateQry);
			
			try{
				coverageApprovedDate=DateFormatUtil.formateDateToMDY((Date)query.getSingleResult());
			}catch(Exception e){
				
			}
			
			
			String budgetNeoDateQry = "select modified from protocol_form_committee_status where retired = 0 and id in (select min(id) from protocol_form_committee_status where id in "
					+ "(select id from protocol_form_committee_status where retired = 0 and protocol_form_committee_status = 'RETURN_FOR_BUDGET_NEGOTIATIONS' and committee = 'BUDGET_REVIEW' "
					+ "and protocol_form_id in (select id from protocol_form where retired = 0 and parent_id = "+pf.getParent().getId()+")))";
 
			query = em.createNativeQuery(budgetNeoDateQry);
			try{
				budgetNeoDate = DateFormatUtil.formateDateToMDY((Date)query.getSingleResult());
			}catch(Exception e){
				
			}
			
			
			String budgetrevisionRequestedQuery = "select modified from protocol_form_committee_status where retired = 0 and protocol_form_committee_status = 'REVISION_REQUESTED' and committee = 'BUDGET_REVIEW' and  protocol_form_id in (select id from protocol_form where retired = 0 and parent_id = "+pf.getParent().getId()+") order by modified";
 
			query = em.createNativeQuery(budgetrevisionRequestedQuery);
			List<Date> revisionDates = query.getResultList();
			
			revisionDate = "<list>";
			for(Date revision:revisionDates){
				
				revisionDate += "<item>"+ DateFormatUtil.formateDateToMDY(revision) +"</item>";
			}
			revisionDate += "</list>";
			
			
			xmlResult += "<field id=\"title\">";
			xmlResult += title;
			xmlResult += "</field>";
			xmlResult += "<field id=\"piname\">";
			xmlResult += piname;
			xmlResult += "</field>";
			xmlResult += "<field id=\"department\">";
			xmlResult += department;
			xmlResult += "</field>";
			xmlResult += "<field id=\"sponsor\">";
			xmlResult += sponsors;
			xmlResult += "</field>";
			xmlResult += "<field id=\"claraencrystart\">";
			xmlResult += protocolCreatedDate;
			xmlResult += "</field>";
			xmlResult += "<field id=\"initialclarasub\">";
			xmlResult += initialSubmitedDate;
			xmlResult += "</field>";
			xmlResult += "<field id=\"subtobudget\">";
			xmlResult += submittedToBudgetDateStr;
			xmlResult += "</field>";
			xmlResult += "<field id=\"reviewerassigned\">";
			xmlResult += budgetReviewAssignedDate;
			xmlResult += "</field>";
			xmlResult += "<field id=\"budgetneo\">";
			xmlResult += budgetNeoDate;
			xmlResult += "</field>";
			xmlResult += "<field id=\"revisiondate\">";
			xmlResult += revisionDate;
			xmlResult += "</field>";
			xmlResult += "<field id=\"converageapproved\">";
			xmlResult += coverageApprovedDate;
			xmlResult += "</field>";
			xmlResult += "<field id=\"budgetapproved\">";
			xmlResult += budgetApprovaldate;
			xmlResult += "</field>";
			xmlResult += "<field id=\"irbprereview\">";
			xmlResult += senttoIRBPreDate;
			xmlResult += "</field>";
			xmlResult += "<field id=\"irbapproval\">";
			xmlResult += originalApprovaldate;
			xmlResult += "</field>";
			
			xmlResult += "</report-item>";
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		return xmlResult;
	}
	
	public EntityManager getEm() {
		return em;
	}

	@PersistenceContext(unitName = "defaultPersistenceUnit")
	public void setEm(EntityManager em) {
		this.em = em;
	}
	
	public ProtocolFormDao getProtocolFormDao() {
		return protocolFormDao;
	}

	@Autowired(required=true)
	public void setProtocolFormDao(ProtocolFormDao protocolFormDao) {
		this.protocolFormDao = protocolFormDao;
	}

}
