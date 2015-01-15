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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;

import edu.uams.clara.core.util.xml.XmlHandler;
import edu.uams.clara.core.util.xml.XmlHandlerFactory;
import edu.uams.clara.webapp.common.dao.usercontext.UserDao;
import edu.uams.clara.webapp.common.util.DateFormatUtil;
import edu.uams.clara.webapp.common.util.RawvalueLookupService;
import edu.uams.clara.webapp.protocol.dao.businesslogicobject.ProtocolFormStatusDao;
import edu.uams.clara.webapp.protocol.dao.protocolform.ProtocolFormDao;
import edu.uams.clara.webapp.protocol.dao.protocolform.ProtocolFormXmlDataDao;
import edu.uams.clara.webapp.protocol.domain.Protocol;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolFormXmlData;
import edu.uams.clara.webapp.protocol.domain.protocolform.enums.ProtocolFormXmlDataType;
import edu.uams.clara.webapp.report.domain.ReportCriteria;
import edu.uams.clara.webapp.report.domain.ReportFieldTemplate;
import edu.uams.clara.webapp.report.domain.ReportTemplate;
import edu.uams.clara.webapp.report.service.customreport.CustomReportService;

public class EnrollmentReportServiceImpl extends CustomReportService{
	private final static Logger logger = LoggerFactory.getLogger(EnrollmentReportServiceImpl.class);
	private EntityManager em;
	private ProtocolFormDao protocolFormDao;
	private ProtocolFormXmlDataDao protocolFormXmlDataDao;
	private ProtocolFormStatusDao protocolFormStatusDao;
	private UserDao userDao;
	private RawvalueLookupService rawvalueLookupService;
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

					//for scheduele only, create a time range from a date to the running date
					if(fieldIdentifier.equals("crapprovedinlastxdays")||fieldIdentifier.equals("crapprovedinlastxmonths")){
						Date currentDate =  new Date();
						
						Calendar c = Calendar.getInstance(); 
						c.setTime(currentDate); 
						if(fieldIdentifier.equals("crapprovedinlastxmonths")){
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
		List<BigInteger> pfidsBig = getCRApprovedBetweenTimeRange(date1, date2);
		
		for(BigInteger pfidBig : pfidsBig){
			try{
				long pfId = pfidBig.longValue();
				finalResultXml +=processEachCR(pfId);
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		
		finalResultXml += "</report-items>";
		finalResultXml += "</report-result>";
		finalResultXml += "</report-results>";
		finalResultXml =finalResultXml.replace("<![CDATA[null]]>", "");
		finalResultXml =finalResultXml.replace("null&lt;br&gt;", "");
		if(finalResultXml.contains("&")){
			finalResultXml=finalResultXml.replaceAll("&", "&amp;");
			}
		logger.debug(finalResultXml);
		return finalResultXml;
	}
	
	private List<BigInteger> getCRApprovedBetweenTimeRange(String beginTime, String endTime){
		String queryStr = "select id from protocol_form where retired = 0 and protocol_form_type = 'CONTINUING_REVIEW' and id in (select distinct protocol_form_id from protocol_form_status where retired = 0 and id in (select max(id) from protocol_form_status where retired = 0 group by protocol_form_id)  and protocol_form_status in ('IRB_APPROVED','EXPEDITED_APPROVED') and id not in (select distinct protocol_form_id from protocol_form_xml_data where retired = 0 and protocol_form_xml_data_type = 'CONTINUING_REVIEW' and xml_data.value('(/continuing-review/subject-accrual/chart-review-study-only/text())[1]','varchar(50)') = 'y')  and modified>"
				+ "'"+beginTime+ "'"
				+ " and modified <"
				+ "'"+ endTime+ "')";
		logger.debug(queryStr);
		Query query = em.createNativeQuery(queryStr);
		List<BigInteger> pfidsBig = query.getResultList();
		
		return pfidsBig;
	}
	
	private String processEachCR(long pfId){
		String resultXmlForCR = "";
		ProtocolFormXmlData pfxd = protocolFormXmlDataDao.getLastProtocolFormXmlDataByProtocolFormIdAndType(pfId, ProtocolFormXmlDataType.CONTINUING_REVIEW);
		Protocol p = protocolFormDao.findById(pfId).getProtocol();
		
		String protocolXml = p.getMetaDataXml();
		String pfxdXml = pfxd.getXmlData();
		
		try{
			XmlHandler xmlHandler = XmlHandlerFactory.newXmlHandler();
			String studyType = xmlHandler.getSingleStringValueByXPath(protocolXml, "/protocol/study-type/text()");
			String piname = xmlHandler.getSingleStringValueByXPath(protocolXml, "/protocol/staffs/staff/user[roles/role/text()=\"Principal Investigator\"]/lastname/text()")+","+
					xmlHandler.getSingleStringValueByXPath(protocolXml, "/protocol/staffs/staff/user[roles/role/text()=\"Principal Investigator\"]/firstname/text()");
			String piuserId = xmlHandler.getSingleStringValueByXPath(protocolXml, "/protocol/staffs/staff/user[roles/role/text()=\"Principal Investigator\"]/@id");
			String title = xmlHandler.getSingleStringValueByXPath(protocolXml, "/protocol/title/text()");
			String primaryLocation = xmlHandler.getSingleStringValueByXPath(protocolXml, "/protocol/site-responsible/text()");
			String currentApprovalStatus =  xmlHandler.getSingleStringValueByXPath(protocolXml, "/protocol/most-recent-study/approval-status/text()");
			String originalApprovedDate = xmlHandler.getSingleStringValueByXPath(protocolXml, "/protocol/original-study/approval-date/text()");
			String generalstudyinfo = xmlHandler.getSingleStringValueByXPath(pfxdXml, "//general-study-info/keep-study-open");
			if(generalstudyinfo.equals("n")){
				generalstudyinfo = "Study Does Not Kepp Open";
			}else if(generalstudyinfo.equals("y")){
				generalstudyinfo = xmlHandler.getSingleStringValueByXPath(pfxdXml, "//general-study-info/study-status/statuses/status/text()");
				generalstudyinfo = rawvalueLookupService.rawvalueLookUp(generalstudyinfo);
			}
			
			String totalSubUAMSOversight = xmlHandler.getSingleStringValueByXPath(protocolXml, "/protocol/accural-goal-local/text()");
			String subjectsSinceActivation = xmlHandler.getSingleStringValueByXPath(pfxdXml, "//subject-accrual/enrollment/local/since-activation/text()");
			String subjectsSinceApproval = xmlHandler.getSingleStringValueByXPath(pfxdXml, "//subject-accrual/enrollment/local/since-approval/text()");
		
			String piCollegeAndDept = "";
			if(!piuserId.isEmpty()){
				try{
					piCollegeAndDept = userDao.findById(Long.valueOf(piuserId)).getPerson().getDepartment();
				}catch(Exception e){
					piCollegeAndDept = "";
				}
			}
			
			resultXmlForCR = "<report-item>";
			resultXmlForCR += "<field id=\"protocolid\">";
			String pidvalue = "<![CDATA[<a target=\"_blank\" href=\"{application.host}/clara-webapp/protocols/"+p.getId()+"/dashboard\">"+p.getId()+"</a>]]>";
			
			pidvalue = replaceValues(pidvalue);
			resultXmlForCR += p.getId();
			resultXmlForCR += "</field>";
			
			resultXmlForCR += "<field id=\"studytype\">";
			resultXmlForCR += studyType;
			resultXmlForCR += "</field>";

			resultXmlForCR += "<field id=\"title\">";
			resultXmlForCR += title;
			resultXmlForCR += "</field>";
			
			resultXmlForCR += "<field id=\"piname\">";
			resultXmlForCR += piname;
			resultXmlForCR += "</field>";
			
			resultXmlForCR += "<field id=\"picollege\">";
			resultXmlForCR += piCollegeAndDept;
			resultXmlForCR += "</field>";
			
			resultXmlForCR += "<field id=\"primarylocation\">";
			resultXmlForCR += primaryLocation;
			resultXmlForCR += "</field>";
			
			resultXmlForCR += "<field id=\"currentapprovalstatus\">";
			resultXmlForCR += currentApprovalStatus;
			resultXmlForCR += "</field>";
			
			resultXmlForCR += "<field id=\"originalapproveddate\">";
			resultXmlForCR += originalApprovedDate;
			resultXmlForCR += "</field>";
			
			resultXmlForCR += "<field id=\"generalstudyinfo\">";
			resultXmlForCR += generalstudyinfo;
			resultXmlForCR += "</field>";
			
			resultXmlForCR += "<field id=\"totalsubUAMSoversight\">";
			resultXmlForCR += totalSubUAMSOversight;
			resultXmlForCR += "</field>";
			
			resultXmlForCR += "<field id=\"subjectssinceactivation\">";
			resultXmlForCR += subjectsSinceActivation;
			resultXmlForCR += "</field>";
			
			resultXmlForCR += "<field id=\"subjectssinceapproval\">";
			resultXmlForCR += subjectsSinceApproval;
			resultXmlForCR += "</field>";
			resultXmlForCR += "</report-item>";
		}catch(Exception e){
			e.printStackTrace();
		}
		
		
		return resultXmlForCR;
	}
	
	private String processTitleLineInfo(String finalResultXml, ReportTemplate reportTemplate){
		finalResultXml += "<report-result id=\""+ reportTemplate.getTypeDescription() +"\"  created=\""+ DateFormatUtil.formateDateToMDY(new Date()) +"\">";
		finalResultXml += "<title>"+ reportTemplate.getDescription() +"</title>";
		finalResultXml += "<fields>";
		finalResultXml += "<field id=\"protocolid\" desc=\"IRB #\" hidden=\"false\" />";
		finalResultXml += "<field id=\"studytype\" desc=\"Study Type\" hidden=\"false\" />";
		finalResultXml += "<field id=\"title\" desc=\"Title\" hidden=\"false\" />";
		finalResultXml += "<field id=\"piname\" desc=\"PI Name\" hidden=\"false\" />";
		finalResultXml += "<field id=\"picollege\" desc=\"PI's College & Department\" hidden=\"false\" />";
		finalResultXml += "<field id=\"primaryLocation\" desc=\"Primay Location\" hidden=\"false\" />";
		finalResultXml += "<field id=\"currentapprovalstatus\" desc=\"Current Approval Status \" hidden=\"false\" />";
		finalResultXml += "<field id=\"originalApprovedDate\" desc=\"Original Approval Date \" hidden=\"false\" />";
		finalResultXml += "<field id=\"generalstudyinfo\" desc=\"General Study Information \" hidden=\"false\" />";
		finalResultXml += "<field id=\"totalSubUAMSOversight\" desc=\"Total Subjects under UAMS Oversight\" hidden=\"false\" />";
		finalResultXml += "<field id=\"subjectsSinceActivation\" desc=\"Number of Subjects Enrolled Since Activation\" hidden=\"false\" />";
		finalResultXml += "<field id=\"subjectsSinceApproval\" desc=\"Number of Subjects Enrolled Since Last Approval\" hidden=\"false\" />";
		finalResultXml += "</fields>";
		finalResultXml += "<report-items>";
		return finalResultXml;
		
	}
	
	private String replaceValues(String value){
		
		for(Entry<String,String> values:this.getDefaultValuesMap().entrySet()){
			value = value.replace("{" + values.getKey() + "}",
					values.getValue());
		}
		
		return value;
	}
	
	public EntityManager getEm() {
		return em;
	}

	@PersistenceContext(unitName = "defaultPersistenceUnit")
	public void setEm(EntityManager em) {
		this.em = em;
	}
	
	public ProtocolFormStatusDao getProtocolFormStatusDao() {
		return protocolFormStatusDao;
	}

	@Autowired(required = true)
	public void setProtocolFormStatusDao(
			ProtocolFormStatusDao protocolFormStatusDao) {
		this.protocolFormStatusDao = protocolFormStatusDao;
	}
	
	public ProtocolFormDao getProtocolFormDao() {
		return protocolFormDao;
	}

	@Autowired(required = true)
	public void setProtocolFormDao(ProtocolFormDao protocolFormDao) {
		this.protocolFormDao = protocolFormDao;
	}

	public ProtocolFormXmlDataDao getProtocolFormXmlDataDao() {
		return protocolFormXmlDataDao;
	}

	@Autowired(required = true)
	public void setProtocolFormXmlDataDao(
			ProtocolFormXmlDataDao protocolFormXmlDataDao) {
		this.protocolFormXmlDataDao = protocolFormXmlDataDao;
	}

	public UserDao getUserDao() {
		return userDao;
	}

	@Autowired(required = true)
	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}

	public RawvalueLookupService getRawvalueLookupService() {
		return rawvalueLookupService;
	}

	@Autowired(required = true)
	public void setRawvalueLookupService(RawvalueLookupService rawvalueLookupService) {
		this.rawvalueLookupService = rawvalueLookupService;
	}

}
