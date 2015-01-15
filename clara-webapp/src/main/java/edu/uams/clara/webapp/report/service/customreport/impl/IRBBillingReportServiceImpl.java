package edu.uams.clara.webapp.report.service.customreport.impl;

import java.io.IOException;
import java.math.BigInteger;
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
import org.w3c.dom.Element;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import edu.uams.clara.core.util.xml.XmlHandler;
import edu.uams.clara.core.util.xml.XmlHandlerFactory;
import edu.uams.clara.webapp.common.util.DateFormatUtil;
import edu.uams.clara.webapp.protocol.dao.businesslogicobject.ProtocolFormStatusDao;
import edu.uams.clara.webapp.protocol.dao.irb.AgendaDao;
import edu.uams.clara.webapp.protocol.dao.protocolform.ProtocolFormDao;
import edu.uams.clara.webapp.protocol.dao.protocolform.ProtocolFormXmlDataDao;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.ProtocolFormStatus;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.enums.ProtocolFormStatusEnum;
import edu.uams.clara.webapp.protocol.domain.irb.AgendaItem.AgendaItemStatus;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolForm;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolFormXmlData;
import edu.uams.clara.webapp.protocol.domain.protocolform.enums.ProtocolFormType;
import edu.uams.clara.webapp.protocol.domain.protocolform.enums.ProtocolFormXmlDataType;
import edu.uams.clara.webapp.report.domain.ReportCriteria;
import edu.uams.clara.webapp.report.domain.ReportFieldTemplate;
import edu.uams.clara.webapp.report.domain.ReportTemplate;
import edu.uams.clara.webapp.report.service.customreport.CustomReportService;

public class IRBBillingReportServiceImpl extends CustomReportService{
	private final static Logger logger = LoggerFactory.getLogger(IRBBillingReportServiceImpl.class);
	private EntityManager em;
	private AgendaDao agendaDao;
	private ProtocolFormDao protocolFormDao;
	private ProtocolFormXmlDataDao protocolFormXmlDataDao;
	private ProtocolFormStatusDao protocolFormStatusDao;
	
	private List<ProtocolFormStatusEnum> approvedStatus = Lists.newArrayList();{
	approvedStatus.add(ProtocolFormStatusEnum.ACKNOWLEDGED);
	approvedStatus.add(ProtocolFormStatusEnum.APPROVED);
	approvedStatus.add(ProtocolFormStatusEnum.IRB_ACKNOWLEDGED);
	approvedStatus.add(ProtocolFormStatusEnum.IRB_APPROVED);
	}
	
	private String processTitleLineInfo(String finalResultXml, ReportTemplate reportTemplate){
		finalResultXml += "<report-result id=\""+ reportTemplate.getTypeDescription() +"\"  created=\""+ DateFormatUtil.formateDateToMDY(new Date()) +"\">";
		finalResultXml += "<title>"+ reportTemplate.getDescription() +"</title>";
		finalResultXml += "<fields>";
		finalResultXml += "<field id=\"protocolid\" desc=\"IRB #\" hidden=\"false\" />";
		finalResultXml += "<field id=\"piname\" desc=\"PI Name\" hidden=\"false\" />";
		finalResultXml += "<field id=\"title\" desc=\"Title\" hidden=\"false\" />";
		finalResultXml += "<field id=\"agendadate\" desc=\"Agenda Date\" hidden=\"false\" />";
		finalResultXml += "<field id=\"formtype\" desc=\"Form Type\" hidden=\"false\" />";
		finalResultXml += "<field id=\"reviewtype\" desc=\"Review Type\" hidden=\"false\" />";
		finalResultXml += "<field id=\"responsibleinstitution\" desc=\"Responsible Institution\" hidden=\"false\" />";
		finalResultXml += "<field id=\"responsibledepartment\" desc=\"Responsible Department\" hidden=\"false\" />";
		finalResultXml += "<field id=\"initiator\" desc=\"Who Initiated\" hidden=\"false\" />";
		finalResultXml += "<field id=\"investigator\" desc=\"Investigator Descriptior\" hidden=\"false\" />";
		finalResultXml += "<field id=\"irbfee\" desc=\"IRB Fee\" hidden=\"false\" />";
		finalResultXml += "<field id=\"supporttype\" desc=\"Support Types\" hidden=\"false\" />";
		finalResultXml += "<field id=\"fundingname\" desc=\"Funding Name\" hidden=\"false\" />";
		finalResultXml += "<field id=\"fundingamount\" desc=\"Funding Amount\" hidden=\"false\" />";
		finalResultXml += "<field id=\"fundingtype\" desc=\"Funding Type\" hidden=\"false\" />";
		finalResultXml += "<field id=\"fundingentity\" desc=\"Funding Entity Name\" hidden=\"false\" />";
		finalResultXml += "</fields>";
		finalResultXml += "<report-items>";
		return finalResultXml;
		
	}

	private String processIRBBillingInfo(String finalResultXml,String beginTime, String endTime){
		String qry = "select protocol_form_id from protocol_form_status "
				+ "where protocol_form_status in ('IRB_APPROVED','EXPEDITED_APPROVED') "
				+ "and id in (select max(id) from protocol_form_status where retired = 0 group by protocol_form_id) and modified>"
				+ "'"+beginTime+ "'"
				+ " and modified <"
				+ "'"+ endTime+ "'"
				+ " and protocol_form_id in (select id from protocol_form where protocol_form_type in ('CONTINUING_REVIEW','NEW_SUBMISSION')  and  retired =0)";
		Query query = em.createNativeQuery(qry);
		List<BigInteger> pfIDs = (List<BigInteger>) query.getResultList();
		for (BigInteger pfIDBig : pfIDs) {
			
			try{
				
			long pfID = pfIDBig.longValue();
			XmlHandler xmlHandler = XmlHandlerFactory.newXmlHandler();
			ProtocolForm pf = protocolFormDao.findById(pfID);
			ProtocolFormStatus pfstatus = protocolFormStatusDao
					.getLatestProtocolFormStatusByFormId(pf.getFormId());
			/*
			 * if(pf.getProtocolFormType()!=ProtocolFormType.NEW_SUBMISSION&&pf.
			 * getProtocolFormType()!=ProtocolFormType.CONTINUING_REVIEW){
			 * continue; } if(!approvedStatus.contains(pfstatus)){ continue; }
			 */
			String protocolxmlData = pf.getProtocol().getMetaDataXml();
			String protocolID = pf.getProtocol().getId() + "";
			String reviewType = "";
			if (pfstatus.getProtocolFormStatus().equals(
					ProtocolFormStatusEnum.IRB_APPROVED)) {
				reviewType = "Full Board";
			} else if (pfstatus.getProtocolFormStatus().equals(
					ProtocolFormStatusEnum.EXPEDITED_APPROVED)) {
				reviewType = "Expedited";
			}
			
			
			
			String piname = xmlHandler.getSingleStringValueByXPath(protocolxmlData, "/protocol/staffs/staff/user[roles/role/text()=\"Principal Investigator\"]/lastname/text()")+","+
					xmlHandler.getSingleStringValueByXPath(protocolxmlData, "/protocol/staffs/staff/user[roles/role/text()=\"Principal Investigator\"]/firstname/text()");
			String title = xmlHandler.getSingleStringValueByXPath(protocolxmlData, "/protocol/title/text()");
			String agendaDate= "";
			try{
				agendaDate=DateFormatUtil.formateDateToMDY(agendaDao.getAgendaByProtocolFormIdAndAgendaItemStatus(pfID,AgendaItemStatus.NEW).getDate());	
			}catch(Exception e){
				//donothing
			}

			String responsibleInstitution = xmlHandler
					.getSingleStringValueByXPath(protocolxmlData,
							"/protocol/site-responsible");
			String college = xmlHandler.getSingleStringValueByXPath(
					protocolxmlData,
					"/protocol/responsible-department/@collegedesc");
			String department = xmlHandler.getSingleStringValueByXPath(
					protocolxmlData,
					"/protocol/responsible-department/@subdeptdesc");
			if (!college.isEmpty() && !department.isEmpty()) {
				department = college + "-" + department;
			} else {
				department = college + department;
			}
			
			if(!(responsibleInstitution.contains("uams")||responsibleInstitution.contains("ach-achri"))){
				continue;
			}

			String whoInitiated = "";
			try {
				whoInitiated = xmlHandler.getSingleStringValueByXPath(protocolxmlData, "/protocol/study-type/text()");
				if(!whoInitiated.contains("industry-sponsored")){
					continue;
				}
			} catch (Exception e) {
				
			}

			String investigatorDesc = xmlHandler
					.getSingleStringValueByXPath(protocolxmlData,
							"/protocol/study-type/investigator-initiated/investigator-description");

			List<Element> subtypes = xmlHandler.listElementsByXPath(protocolxmlData, "/protocol/study-type/investigator-initiated/sub-type");
			int subtypesSzie = subtypes.size();
			int subtypeIndex = 0;

			String irbFee = "";

			if (pf.getFormType().equals(
					ProtocolFormType.NEW_SUBMISSION.toString())) {
				ProtocolFormXmlData pfxd = protocolFormXmlDataDao
						.getLastProtocolFormXmlDataByProtocolFormIdAndType(
								pfID, ProtocolFormXmlDataType.PROTOCOL);
				String pfxdXml = pfxd.getXmlData();
				irbFee = xmlHandler
						.getSingleStringValueByXPath(
								pfxdXml,
								"/protocol/irb-fees/category[name/text()[contains(., \"(New Submission)\")]]/fee");
			} else if (pf.getFormType().equals(
					ProtocolFormType.CONTINUING_REVIEW.toString())) {
				try {
					List<ProtocolForm> modificationLists = protocolFormDao
							.listProtocolFormsByProtocolIdAndProtocolFormType(
									pf.getProtocol().getId(),
									ProtocolFormType.MODIFICATION);
					ProtocolForm latestModificationBeforeContinuing = null;
					for (ProtocolForm mf : modificationLists) {
						if (mf.getFormId() < pfID) {
							latestModificationBeforeContinuing = mf;
						} else {
							break;
						}
					}
					if (latestModificationBeforeContinuing != null) {
						ProtocolFormXmlData pfxd = protocolFormXmlDataDao
								.getLastProtocolFormXmlDataByProtocolFormIdAndType(
										latestModificationBeforeContinuing
												.getFormId(),
										ProtocolFormXmlDataType.MODIFICATION);
						String pfxdXml = pfxd.getXmlData();
						irbFee = xmlHandler
								.getSingleStringValueByXPath(
										pfxdXml,
										"/protocol/irb-fees/category[name/text()[contains(., \"(Continuing Review)\")]]/fee");

					}
				} catch (Exception e) {
					ProtocolFormXmlData pfxd = protocolFormXmlDataDao
							.getLastProtocolFormXmlDataByProtocolFormIdAndType(
									pfID, ProtocolFormXmlDataType.PROTOCOL);
					String pfxdXml = pfxd.getXmlData();
					irbFee = xmlHandler
							.getSingleStringValueByXPath(
									pfxdXml,
									"/protocol/irb-fees/category[name/text()[contains(., \"(Continuing Review)\")]]/fee");
				}
			}
			
			if(department.contains("&")){
				department=department.replaceAll("&", "&amp;");
				}
			if(title.contains("&")){
				title=title.replaceAll("&", "&amp;");
				}
			if(title.contains("<")){
				title=title.replaceAll("<", "&lt;");
				}
			if(title.contains(">")){
				title=title.replaceAll(">", "&gt;");
				}
			
			finalResultXml += "<report-item>";
			finalResultXml += "<field id=\"protocolid\">";
			finalResultXml += protocolID;
			finalResultXml += "</field>";
			finalResultXml += "<field id=\"piname\">";
			finalResultXml += piname;
			finalResultXml += "</field>";
			finalResultXml += "<field id=\"title\">";
			finalResultXml += title;
			finalResultXml += "</field>";
			finalResultXml += "<field id=\"agendadate\">";
			finalResultXml += agendaDate;
			finalResultXml += "</field>";
			finalResultXml += "<field id=\"formtype\">";
			finalResultXml += pf.getFormType() + "";
			finalResultXml += "</field>";
			finalResultXml += "<field id=\"reviewtype\">";
			finalResultXml += reviewType;
			finalResultXml += "</field>";
			finalResultXml += "<field id=\"responsibleinstitution\">";
			finalResultXml += responsibleInstitution;
			finalResultXml += "</field>";
			finalResultXml += "<field id=\"responsibledepartment\">";
			finalResultXml += department;
			finalResultXml += "</field>";
			finalResultXml += "<field id=\"initiator\">";
			finalResultXml += whoInitiated;
			finalResultXml += "</field>";
			finalResultXml += "<field id=\"investigator\">";
			finalResultXml += investigatorDesc;
			finalResultXml += "</field>";
			finalResultXml += "<field id=\"irbfee\">";
			finalResultXml += irbFee;
			finalResultXml += "</field>";
			
			
			
			
			
			String tempFundingName ="<field id=\"fundingname\"><list>";
			String tempFundingAmount ="<field id=\"fundingamount\"><list>";
			String tempFundingType ="<field id=\"fundingtype\"><list>";
			String tempFundingEntity ="<field id=\"fundingentity\"><list>";
			String tempSubtypes ="<field id=\"supporttype\"><list>";
			
			while (subtypeIndex < subtypesSzie) {
				tempSubtypes +="<item>"+ subtypes.get(subtypeIndex).getTextContent() +  "</item>";
				subtypeIndex++;
			}
			tempSubtypes += "</list></field>";
			finalResultXml +=tempSubtypes;
			List<Element> fundings = xmlHandler.listElementsByXPath(protocolxmlData, "/protocol/funding/funding-source");

			for (Element funding : fundings) {
				String fundingName = funding.getAttribute("name");
				String fundingAmount = funding.getAttribute("amount");
				String fundingEntityName = funding.getAttribute("entityname");
				String type = funding.getAttribute("type");
				if(type.equals("Internal")||type.equals("None")){
					if(!fundingName.isEmpty()){
						fundingName = "Fund: "+fundingName;
					}
					if(!fundingEntityName.isEmpty()){
						fundingEntityName = "Cost Center: "+fundingEntityName;
					}
					fundingEntityName = fundingName +" "+fundingEntityName;
					fundingName ="";
				}
				
					tempFundingName +="<item>"+ fundingName +  "</item>";
					tempFundingAmount +="<item>"+ fundingAmount +  "</item>";
					tempFundingType +="<item>"+ type +  "</item>";
					tempFundingEntity +="<item>"+ fundingEntityName +  "</item>";
				
								
			}
			tempFundingName += "</list></field>";
			tempFundingAmount += "</list></field>";
			tempFundingType += "</list></field>";
			tempFundingEntity += "</list></field>";
			finalResultXml+=tempFundingName+tempFundingAmount+tempFundingType+tempFundingEntity;
			if(finalResultXml.contains("<list></list>")){
				finalResultXml = finalResultXml.replaceAll("<list></list>", "");
			}
			finalResultXml += "</report-item>";
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

					//for scheduel only, create a time range from a date to the running date
					if(fieldIdentifier.equals("approvedinlastxdays")){
						Date currentDate =  new Date();
						
						date2 = DateFormatUtil.formateDateToMDY(currentDate);
						Calendar c = Calendar.getInstance(); 
						c.setTime(currentDate); 
						c.add(Calendar.DATE, -(Integer.valueOf(value)));
						currentDate = c.getTime();
						date1 = DateFormatUtil.formateDateToMDY(currentDate);
						
						queryCriteriasValueMap.put(
								"Time Span",
								"BETWEEN: " + date1+"~"+date2);
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
		finalResultXml = processIRBBillingInfo(finalResultXml,date1,date2);
		finalResultXml =finalResultXml.replace("<![CDATA[null]]>", "");
		finalResultXml =finalResultXml.replace("&gt;null", "&gt;");
		finalResultXml =finalResultXml.replace("null&lt;br&gt;", "");
		logger.debug(finalResultXml);
		return finalResultXml;
	}

	public List<ProtocolFormStatusEnum> getApprovedStatus() {
		return approvedStatus;
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

	public AgendaDao getAgendaDao() {
		return agendaDao;
	}

	@Autowired(required = true)
	public void setAgendaDao(AgendaDao agendaDao) {
		this.agendaDao = agendaDao;
	}

}
