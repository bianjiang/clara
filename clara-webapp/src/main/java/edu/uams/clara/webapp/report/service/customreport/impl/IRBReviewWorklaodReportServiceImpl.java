package edu.uams.clara.webapp.report.service.customreport.impl;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
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
import edu.uams.clara.webapp.protocol.dao.businesslogicobject.ProtocolFormCommitteeStatusDao;
import edu.uams.clara.webapp.report.domain.ReportCriteria;
import edu.uams.clara.webapp.report.domain.ReportFieldTemplate;
import edu.uams.clara.webapp.report.domain.ReportTemplate;
import edu.uams.clara.webapp.report.service.customreport.CustomReportService;

public class IRBReviewWorklaodReportServiceImpl  extends CustomReportService{

private final static Logger logger = LoggerFactory.getLogger(IRBReviewWorklaodReportServiceImpl.class);

private EntityManager em;
private ProtocolFormCommitteeStatusDao protocolFormCommitteeStatusDao;

private List<Object[]> getPfcsWithUidAndTimeRange(String beginTime, String endTime, String committee, long userId){
	String queryStr = "select  pf.id, pf.meta_data_xml.value('(//summary/irb-determination/recent-motion)[1]','varchar(max)'), pf.protocol_form_type, pfcs.committee, pfcs.modified from protocol_form_committee_status pfcs inner join protocol_form pf on pfcs.protocol_form_id = pf.id inner join protocol p on pf.protocol_id = p.id  "
			+ " where pfcs.retired = 0 and p.retired = 0 "
			+ " and pfcs.modified between '"+beginTime+ "' and '"+endTime+ "' "
			+ " and pfcs.action in ('APPROVE','ACKNOWLEDGED','ACKNOWLEDGE','APPROVED','DECLINE','DECLINED','IS_HSR','IS_NOT_HSR','NOT_RNI','CHECK_CONSENT_FORMS','ASSIGN_REVIEWER','DEFER_WITH_MAJOR','TABLE','DEFER_WITH_MINOR') "
			+ " and pfcs.committee = pfcs.cause_by_committee ";
			if(!committee.isEmpty()){
				queryStr +=" and pf.retired = 0 and pfcs.committee = '"+committee+"' ";
			}
	
			if(userId>0){
				queryStr +=" and pfcs.caused_by_user_id = "+userId;
			}
			
			queryStr+= " order by pfcs.modified desc";
	Query query = em.createNativeQuery(queryStr);
	List<Object[]> pfcsidsBig = (List<Object[]>) query.getResultList();
	//logger.debug("  "+queryStr);
	return pfcsidsBig;
}

private String processResult(List<Object[]> resultObjs){
	String xmlResult = "";
	Map<String,Integer> resultMap = Maps.newHashMap();
	
	for(Object[] resultObject : resultObjs){
		String minorCondition = (String) resultObject[1];
		
		String protocol_form_type = (String) resultObject[2];
		
		String committee = (String) resultObject[3];
		
		String reviewType = protocol_form_type+"-"+committee;
		
		if(minorCondition!=null&&minorCondition.trim().equals("Defer with minor contingencies")){
			reviewType = protocol_form_type+"-Minor Met";
		}
		
		if(resultMap.keySet().contains(reviewType)){
			resultMap.put(reviewType, resultMap.get(reviewType)+1);
		}else{
			resultMap.put(reviewType, 1);
		}
	}
	
	for(String key: resultMap.keySet()){
		xmlResult += "<report-item>";
		
		xmlResult += "<field id=\"reviewtype\">";
		xmlResult += key;
		xmlResult += "</field>";
		
		xmlResult += "<field id=\"reviewnumber\">";
		xmlResult += resultMap.get(key);
		xmlResult += "</field>";
		
		
		
		xmlResult += "</report-item>";
	}
	
	return xmlResult;
	
}

private String processTitleLineInfo(String finalResultXml, ReportTemplate reportTemplate, String startTime, String endtime){
	String xmlTitle = "";
	xmlTitle += "<report-result>";
	xmlTitle += "<title>"+ startTime+"~"+endtime +"</title>";
	xmlTitle += "<fields>";
	xmlTitle += "<field id=\"reviewtype\" desc=\"Review Type\" hidden=\"false\" />";
	xmlTitle += "<field id=\"reviewnumber\" desc=\"Number\" hidden=\"false\" />";
	xmlTitle += "</fields>";
	xmlTitle += "<report-items>";
	return xmlTitle;
	
}

@Override
public String generateReportResult(ReportTemplate reportTemplate) {
	List<ReportCriteria> criterias = reportTemplate.getReportCriterias();
	
	String finalResultXml = "<report-results>";
	
	ObjectMapper objectMapper = new ObjectMapper();
		
	Map<String, String> fieldsRealXPathMap = Maps.newHashMap();
	Map<String, String> queryCriteriasValueMap = Maps.newHashMap();
	String date1 = "4/21/2013";
	SimpleDateFormat sdf = new SimpleDateFormat("M/d/yyyy");
	Date today = new Date();
	String date2 = sdf.format(today);
	long userid = 0;
	String committee = "";
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
							"Approved Time Range",
							"BEFORE: " + value);
					realXpath = reportCriteriaField.getNodeXPath().replace("{value}", "'"+ value.toUpperCase() +"'");
				} else if(reportCriteriaField.getOperator().toString()
						.equals("AFTER")){
					date1 = value;
					queryCriteriasValueMap.put(
							"Approved Time Range",
							"AFTER: " + value);
					realXpath = reportCriteriaField.getNodeXPath().replace("{value}", "'"+ value.toUpperCase() +"'");
				} else if (reportCriteriaField.getOperator().toString()
				
						.equals("BETWEEN")) {
					 date1 = value.toUpperCase().substring(0,value.toUpperCase().indexOf(","));
					 date2 = value.toUpperCase().substring(value.toUpperCase().indexOf(",") + 1,value.length());
					 queryCriteriasValueMap.put(
								"Approved Time Range",
								"BETWEEN: " + date1+"~"+date2);
					 
					 realXpath =reportCriteriaField.getNodeXPath().replace("{value}", "'"+ value.toUpperCase().substring(0,value.toUpperCase().indexOf(",")) +"'");
					 realXpath = realXpath.replace("{operator}", ">");
					 realXpath = realXpath +" AND "+reportCriteriaField.getNodeXPath().replace("{value}", "'"+ value.toUpperCase().substring(value.toUpperCase().indexOf(",")+1,value.length()) +"'");;
					 realXpath = realXpath.replace("{operator}", "<");
				} else {
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
							};
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
				
				//getuserid
				if(fieldIdentifier.equals("reviewername")){
					userid = Integer.valueOf(value);
				}
				
				//getuserid
				if(fieldIdentifier.equals("reviewmmittee")){
					committee = value;
				}
				

				//for scheduele only, create a time range from a date to the running date
				if(fieldIdentifier.equals("reviewinlastxdays")||fieldIdentifier.equals("reviewinlastxmonths")){
					Date currentDate =  new Date();
					
					Calendar c = Calendar.getInstance(); 
					c.setTime(currentDate); 
					if(fieldIdentifier.equals("reviewinlastxmonths")){
						c.add(Calendar.MONTH, -(Integer.valueOf(value)));
					}else{
						c.add(Calendar.DATE, -(Integer.valueOf(value)));
					}
					Date date = c.getTime();
					date1 = DateFormatUtil.formateDateToMDY(date);
					
					c.setTime(currentDate); 
					c.add(Calendar.DATE, -1);
					date = c.getTime();
					date2 = DateFormatUtil.formateDateToMDY(date);

					queryCriteriasValueMap.put(
							"Approved Time Range",
							"BETWEEN: " + date1+"~"+date2);
					
					//real query value, in between query, we need include today, if we do not add 23:59:59:999
					date2 = DateFormatUtil.formateDateToMDY(currentDate);
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
	
	
	//split result by week
	Calendar cs = Calendar.getInstance(); 
	
	Calendar ce = Calendar.getInstance(); 
	
	Calendar cEndDisplay = Calendar.getInstance(); 
	
	Calendar stopDate = Calendar.getInstance(); 
	
	try {
		ce.setTime(sdf.parse(date2));
		cs.add(Calendar.DATE, 1);
		cs.setTime(sdf.parse(date2));
		cs.add(Calendar.DATE, -6);
		cEndDisplay.setTime(sdf.parse(date2));
		stopDate.setTime(sdf.parse(date1));
		
		if(cs.before(stopDate)){
			cs.setTime(sdf.parse(date1));
		}
	} catch (ParseException e) {
		e.printStackTrace();
	}
	
	
	while(!cs.before(stopDate)){
		String startDateStr = DateFormatUtil.formateDateToMDY(cs.getTime());
		String endDateStr = DateFormatUtil.formateDateToMDY(ce.getTime());
		String endDateDisplayStr = DateFormatUtil.formateDateToMDY(cEndDisplay.getTime());
		
		finalResultXml += processTitleLineInfo(finalResultXml,reportTemplate,startDateStr, endDateDisplayStr);
		List<Object[]> resultObjs  = getPfcsWithUidAndTimeRange(startDateStr, endDateStr,committee,userid);
		finalResultXml +=processResult(resultObjs);
		
		finalResultXml += "</report-items>";
		finalResultXml += "</report-result>";
		
		if(!cs.after(stopDate)){
			break;
		}
		
		cs.add(Calendar.DATE, -7);
		ce.add(Calendar.DATE, -7);
		cEndDisplay.add(Calendar.DATE, -7);
		
		if(cs.before(stopDate)){
			try {
				cs.setTime(sdf.parse(date1));
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
	}
	
	
	
	finalResultXml += "</report-results>";
	finalResultXml =finalResultXml.replace("<![CDATA[null]]>", "");
	finalResultXml =finalResultXml.replace("null&lt;br&gt;", "");
	finalResultXml =finalResultXml.replace("&gt;null", "&gt;");
	if(finalResultXml.contains("&")){
		finalResultXml=finalResultXml.replaceAll("&", "&amp;");
		}
	//logger.debug(finalResultXml);
	return finalResultXml;
}

public EntityManager getEm() {
	return em;
}

@PersistenceContext(unitName = "defaultPersistenceUnit")
public void setEm(EntityManager em) {
	this.em = em;
}

public ProtocolFormCommitteeStatusDao getProtocolFormCommitteeStatusDao() {
	return protocolFormCommitteeStatusDao;
}

@Autowired(required = true)
public void setProtocolFormCommitteeStatusDao(
		ProtocolFormCommitteeStatusDao protocolFormCommitteeStatusDao) {
	this.protocolFormCommitteeStatusDao = protocolFormCommitteeStatusDao;
}


}
