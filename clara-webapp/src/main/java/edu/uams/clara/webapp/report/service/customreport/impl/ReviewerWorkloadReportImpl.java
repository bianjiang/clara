package edu.uams.clara.webapp.report.service.customreport.impl;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.commons.lang.StringEscapeUtils;
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

public class ReviewerWorkloadReportImpl extends CustomReportService{
	private final static Logger logger = LoggerFactory.getLogger(EnrollmentReportServiceImpl.class);

	private EntityManager em;
	private ProtocolFormCommitteeStatusDao protocolFormCommitteeStatusDao;
	
	private String replaceValues(String value){
		
		for(Entry<String,String> values:this.getDefaultValuesMap().entrySet()){
			value = value.replace("{" + values.getKey() + "}",
					values.getValue());
		}
		
		return value;
	}
	
	private List<Object[]> getPfcsWithUidAndTimeRange(String beginTime, String endTime, String committee, long userId){
		String queryStr = "select  p.id, p.meta_data_xml.value('(/protocol/title/text())[1]','varchar(max)'), pf.protocol_form_type, pfcs.protocol_form_committee_status, pfcs.modified from protocol_form_committee_status pfcs inner join protocol_form pf on pfcs.protocol_form_id = pf.id inner join protocol p on pf.protocol_id = p.id  "
				+ " where pfcs.retired = 0 and p.retired = 0 "
				+ " and pfcs.modified between '"+beginTime+ "' and '"+endTime+ "' ";
				
				if(!committee.isEmpty()){
					queryStr +=" and pf.retired = 0 and pfcs.committee = '"+committee+"' ";
				}
		
				if(userId>0){
					queryStr +=" and pfcs.caused_by_user_id = "+userId;
				}
				
				queryStr+= " order by pfcs.modified desc";
		Query query = em.createNativeQuery(queryStr);
		List<Object[]> pfcsidsBig = (List<Object[]>) query.getResultList();
		logger.debug("  "+queryStr);
		return pfcsidsBig;
	}
	
	private String processEachResult(Object[] resultObject){
		
		BigInteger pid = (BigInteger) resultObject[0];
		String title = (String) resultObject[1];
		title = StringEscapeUtils.escapeHtml(title);
		String protocol_form_type = (String) resultObject[2];
		
		String reviewaction = (String) resultObject[3];
		
		String reviewDate = DateFormatUtil.formateDateToMDY((Date) resultObject[4]);
		
		String resultXmlForEachRecord = "";
		
			resultXmlForEachRecord = "<report-item>";
			
			resultXmlForEachRecord += "<field id=\"reviewaction\">";
			resultXmlForEachRecord += reviewaction;
			resultXmlForEachRecord += "</field>";
			
			resultXmlForEachRecord += "<field id=\"reviewdate\">";
			resultXmlForEachRecord += reviewDate;
			resultXmlForEachRecord += "</field>";
			
			
			resultXmlForEachRecord += "<field id=\"protocolid\">";
			resultXmlForEachRecord += "<![CDATA[<a target=\"_blank\" href=\""+this.getAppHost()+"/clara-webapp/protocols/"+pid+"/dashboard\">"+pid+"</a>]]>";
			resultXmlForEachRecord += "</field>";
			

			resultXmlForEachRecord += "<field id=\"title\">";
			resultXmlForEachRecord += title;
			resultXmlForEachRecord += "</field>";
			
			resultXmlForEachRecord += "<field id=\"protocolform\">";
			resultXmlForEachRecord += protocol_form_type;
			resultXmlForEachRecord += "</field>";
			
			resultXmlForEachRecord += "</report-item>";
		
		return resultXmlForEachRecord;
	}
	
	
	private String processTitleLineInfo(String finalResultXml, ReportTemplate reportTemplate){
		finalResultXml += "<report-result id=\""+ reportTemplate.getTypeDescription() +"\"  created=\""+ DateFormatUtil.formateDateToMDY(new Date()) +"\">";
		finalResultXml += "<title>"+ reportTemplate.getDescription() +"</title>";
		finalResultXml += "<fields>";
		finalResultXml += "<field id=\"reviewaction\" desc=\"Review Action\" hidden=\"false\" />";
		finalResultXml += "<field id=\"reviewdate\" desc=\"Review Date\" hidden=\"false\" />";
		finalResultXml += "<field id=\"protocolid\" desc=\"IRB #\" hidden=\"false\" />";
		finalResultXml += "<field id=\"title\" desc=\"Title\" hidden=\"false\" />";
		finalResultXml += "<field id=\"protocolform\" desc=\"Protocol Form Type\" hidden=\"false\" />";
		finalResultXml += "</fields>";
		finalResultXml += "<report-items>";
		return finalResultXml;
		
	}
	
	@Override
	public String generateReportResult(ReportTemplate reportTemplate) {
		List<ReportCriteria> criterias = reportTemplate.getReportCriterias();
		
		String finalResultXml = "<report-results>";
		
		ObjectMapper objectMapper = new ObjectMapper();
			
		Map<String, String> fieldsRealXPathMap = Maps.newHashMap();
		Map<String, String> queryCriteriasValueMap = Maps.newHashMap();
		String date1 = "1990-10-10";
		String date2 = "2020-10-10";
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
		finalResultXml = processTitleLineInfo(finalResultXml,reportTemplate);
		List<Object[]> resultObjs  = getPfcsWithUidAndTimeRange(date1, date2,committee,userid);
		
		for(Object[] resultObj : resultObjs){
			try{
				finalResultXml +=processEachResult(resultObj);
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		
		finalResultXml += "</report-items>";
		finalResultXml += "</report-result>";
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
