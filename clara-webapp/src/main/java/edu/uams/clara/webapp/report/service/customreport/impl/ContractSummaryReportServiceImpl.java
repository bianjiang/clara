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

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;

import edu.uams.clara.webapp.common.util.DateFormatUtil;
import edu.uams.clara.webapp.report.domain.ReportCriteria;
import edu.uams.clara.webapp.report.domain.ReportFieldTemplate;
import edu.uams.clara.webapp.report.domain.ReportTemplate;
import edu.uams.clara.webapp.report.service.customreport.CustomReportService;

public class ContractSummaryReportServiceImpl extends CustomReportService {

	private final static Logger logger = LoggerFactory.getLogger(ContractSummaryReportServiceImpl.class);
	private EntityManager em;
	
	@Override
	public String generateReportResult(ReportTemplate reportTemplate) {
		String finalResultXml = "<report-results>";
		List<ReportCriteria> criterias = reportTemplate.getReportCriterias();
		ObjectMapper objectMapper = new ObjectMapper();
		
		Map<String, String> fieldsRealXPathMap = Maps.newHashMap();
		Map<String, String> queryCriteriasValueMap = Maps.newHashMap();
		String date1 = "4/21/2013";
		SimpleDateFormat sdf = new SimpleDateFormat("M/d/yyyy");
		Date today = new Date();
		String date2 = sdf.format(today);
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
								"Submission Time Range",
								"BEFORE: " + value);
						realXpath = reportCriteriaField.getNodeXPath().replace("{value}", "'"+ value.toUpperCase() +"'");
					} else if(reportCriteriaField.getOperator().toString()
							.equals("AFTER")){
						date1 = value;
						queryCriteriasValueMap.put(
								"Submission Time Range",
								"AFTER: " + value);
						realXpath = reportCriteriaField.getNodeXPath().replace("{value}", "'"+ value.toUpperCase() +"'");
					} else if (reportCriteriaField.getOperator().toString()
					
							.equals("BETWEEN")) {
						 date1 = value.toUpperCase().substring(0,value.toUpperCase().indexOf(","));
						 date2 = value.toUpperCase().substring(value.toUpperCase().indexOf(",") + 1,value.length());
						 queryCriteriasValueMap.put(
									"Submission Time Range",
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
								"Submission Time Range",
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
		
		
		//get query end date
		Calendar ce = Calendar.getInstance(); 
		
		try {
			ce.setTime(sdf.parse(date2));
			ce.add(Calendar.DATE, 1);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		String startDateStr = date1;
		String endDateDisplayStr = date2;
		String endDateQuery = DateFormatUtil.formateDateToMDY(ce.getTime());
		
		finalResultXml += processTitleLineInfo(finalResultXml,reportTemplate,startDateStr, endDateDisplayStr);
		
		finalResultXml += "<report-items>";
		finalResultXml += "<report-item>";
		finalResultXml += "<field id=\"time-period\">";
		finalResultXml += startDateStr+" - "+endDateDisplayStr;
		finalResultXml += "</field>";
		
		
		
		finalResultXml +=processResult(startDateStr,endDateQuery);
		finalResultXml += "</report-item>";
		finalResultXml += "</report-items>";
		finalResultXml += "</report-result>";
		finalResultXml += "</report-results>";
		
		finalResultXml =finalResultXml.replace("<![CDATA[null]]>", "");
		finalResultXml =finalResultXml.replace("&gt;null", "&gt;");
		finalResultXml =finalResultXml.replace("null&lt;br&gt;", "");
		
		logger.debug(finalResultXml);
		
		return finalResultXml;
	}
	
	private String processResult(String startDate, String endDate){
		String resultXml = "";
		String averageTimequeryStr = "select avg(DATEDIFF(day, startT.startDate, endT.endDate)) "
				+ " from (select contract_form_id as startFormId, modified as startDate from contract_form_status where retired = 0 "
				+ " and contract_form_status = 'UNDER_CONTRACT_MANAGER_REVIEW' ) as startT, (select contract_form_id as endFormId, modified as endDate from contract_form_status where retired = 0 and contract_form_status = 'CONTRACT_EXECUTED' ) as endT  "
				+ " where startT.startFormId = endT.endFormId and startT.startDate between '"+startDate+"' and '"+endDate+"'";
		
		Query query = em.createNativeQuery(averageTimequeryStr);
		String averageTime = query.getSingleResult().toString();
		
		String contractNumQueryStr ="select count(distinct contract_id) from contract_form cf, (select startT.startFormId as formId,  DATEDIFF(day, startT.startDate, endT.endDate) as dayDiff from (select contract_form_id as startFormId, modified as startDate from contract_form_status where retired = 0 "
				+ " and contract_form_status = 'UNDER_CONTRACT_MANAGER_REVIEW' ) as startT, (select contract_form_id as endFormId, modified as endDate from contract_form_status where retired = 0 and contract_form_status = 'CONTRACT_EXECUTED' ) as endT where startT.startFormId = endT.endFormId "
				+ "and startT.startDate between '"+startDate+"' and '"+endDate+"') as temp where cf.id = temp.formId and cf.retired = 0";
		
		query = em.createNativeQuery(contractNumQueryStr);
		
		String contractNum =  ""+ query.getSingleResult();
		
		resultXml += "<field id=\"contractnum\">";
		resultXml += contractNum;
		resultXml += "</field>";
		
		resultXml += "<field id=\"avgreviewtime\">";
		resultXml += averageTime;
		resultXml += "</field>";
		
		return resultXml;
		
	}
	
	private String processTitleLineInfo(String finalResultXml, ReportTemplate reportTemplate, String startTime, String endtime){
		String xmlTitle = "";
		xmlTitle += "<report-result>";
		xmlTitle += "<title>"+ reportTemplate.getDescription() +"</title>";
		xmlTitle += "<fields>";
		xmlTitle += "<field id=\"time-period\" desc=\"Time Period (submitted during this time period)\" hidden=\"false\" />";
		xmlTitle += "<field id=\"contractnum\" desc=\"Number of Contracts\" hidden=\"false\" />";
		xmlTitle += "<field id=\"avgreviewtime\" desc=\"Average time of review (days)\" hidden=\"false\" />";
		xmlTitle += "</fields>";
		return xmlTitle;
		
	}
	
	
	public EntityManager getEm() {
		return em;
	}

	@PersistenceContext(unitName = "defaultPersistenceUnit")
	public void setEm(EntityManager em) {
		this.em = em;
	}

}
