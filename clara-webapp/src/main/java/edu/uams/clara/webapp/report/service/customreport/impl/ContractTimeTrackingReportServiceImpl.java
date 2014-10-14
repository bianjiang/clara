package edu.uams.clara.webapp.report.service.customreport.impl;

import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import edu.uams.clara.webapp.common.domain.usercontext.enums.Committee;
import edu.uams.clara.webapp.common.util.DateFormatUtil;
import edu.uams.clara.webapp.contract.dao.businesslogicobject.ContractFormCommitteeStatusDao;
import edu.uams.clara.webapp.contract.dao.contractform.ContractFormDao;
import edu.uams.clara.webapp.contract.domain.Contract;
import edu.uams.clara.webapp.contract.domain.contractform.ContractForm;
import edu.uams.clara.webapp.contract.dao.ContractDao;
import edu.uams.clara.webapp.contract.domain.businesslogicobject.ContractFormCommitteeStatus;
import edu.uams.clara.webapp.contract.domain.businesslogicobject.enums.ContractFormCommitteeStatusEnum;
import edu.uams.clara.webapp.report.dao.ReportFieldDao;
import edu.uams.clara.webapp.report.domain.CommitteeActions;
import edu.uams.clara.webapp.report.domain.ReportCriteria;
import edu.uams.clara.webapp.report.domain.ReportField;
import edu.uams.clara.webapp.report.domain.ReportFieldTemplate;
import edu.uams.clara.webapp.report.domain.ReportResultFieldTemplate;
import edu.uams.clara.webapp.report.domain.ReportTemplate;
import edu.uams.clara.webapp.report.service.customreport.CustomReportService;

public class ContractTimeTrackingReportServiceImpl  extends CustomReportService{
	private final static Logger logger = LoggerFactory
			.getLogger(ContractTimeTrackingReportServiceImpl.class);
	private ContractDao contractDao;
	private ContractFormDao contractFormDao;
	private ContractFormCommitteeStatusDao contractFormCommitteeStatusDao;
	private ReportFieldDao reportFieldDao;
	private CommitteeActions committeeactions = new CommitteeActions();
	
	private List<Committee> committees = Lists.newArrayList();{
		committees.add(Committee.CONTRACT_MANAGER);
		committees.add(Committee.CONTRACT_ADMIN);
		committees.add(Committee.CONTRACT_LEGAL_REVIEW);
	}
	
	private String replaceValues(String value){
		
		for(Entry<String,String> values:this.getDefaultValuesMap().entrySet()){
			value = value.replace("{" + values.getKey() + "}",
					values.getValue());
		}
		
		return value;
	}
	
	@Override
	public String generateReportResult(ReportTemplate reportTemplate) {
		/*List<Contract> contracts = contractDao.findAll();
		contracts.clear();
		contracts.add(contractDao.findById(1));*/
		String finalResultXml = "<report-results>";
		List<ReportCriteria> criterias = reportTemplate.getReportCriterias();

		ObjectMapper objectMapper = new ObjectMapper();

		Map<String, String> fieldsRealXPathMap = Maps.newHashMap();
		Map<String, String> queryCriteriasValueMap = Maps.newHashMap();

		for (ReportCriteria rc : criterias) {
			ReportFieldTemplate reportField = new ReportFieldTemplate();

			try {
				reportField = objectMapper.readValue(rc.getCriteria(),
						ReportFieldTemplate.class);

				String fieldIdentifier = reportField.getFieldIdentifier();

				String value = reportField.getValue();
				if(reportField.getOperator().toString().equals("AFTER")){
					queryCriteriasValueMap.put(reportField.getFieldDisplayName(), "AFTER: "+reportField.getDisplayValue());
				}else if(reportField.getOperator().toString().equals("BEFORE")){
					queryCriteriasValueMap.put(reportField.getFieldDisplayName(), "BEFORE: "+reportField.getDisplayValue());
				}else{
					queryCriteriasValueMap.put(reportField.getFieldDisplayName(), reportField.getDisplayValue());
				}
				if (value != null && !value.isEmpty()) {
					String realXpath = "";

					if (reportField.getOperator().toString().equals("AFTER")
							|| reportField.getOperator().toString()
									.equals("BEFORE")) {
						realXpath = reportField.getNodeXPath().replace(
								"{value}", "'" + value.toUpperCase() + "'");
					} else if (reportField.getOperator().toString()
							.equals("BETWEEN")) {
						realXpath = reportField.getNodeXPath().replace(
								"{value}",
								"'"
										+ value.toUpperCase().substring(
												0,
												value.toUpperCase()
														.indexOf(",")) + "'");
						realXpath = realXpath.replace("{operator}", ">");
						realXpath = realXpath
								+ " AND "
								+ reportField
										.getNodeXPath()
										.replace(
												"{value}",
												"'"
														+ value.toUpperCase()
																.substring(
																		value.toUpperCase()
																				.indexOf(
																						",") + 1,
																		value.length())
														+ "'");
						realXpath = realXpath.replace("{operator}", "<");

					} else {
						if (value.contains("|")) {
							String[] values = value.split("\\|");
							realXpath += "(";
							for (int i = 0; i < values.length; i++) {
								if (i > 0) {
									realXpath += " OR ";
								}
								if (reportField.getNodeXPath().contains(
										".exist")
										|| reportField.getNodeXPath().contains(
												".value")) {
									realXpath += reportField
											.getNodeXPath()
											.replace(
													"{value}",
													"\""
															+ values[i]
																	.toUpperCase()
															+ "\"");
								} else if (values[i].contains("'")) {
									if (value.equals("=1")
											|| value.equals("=0")) {
										realXpath += reportField
												.getNodeXPath()
												.replace("{value}",
														values[i].toUpperCase());
									} else {
										realXpath += reportField
												.getNodeXPath()
												.replace(
														"{value}",
														"\""
																+ values[i]
																		.toUpperCase()
																+ "\"");
									}
								} else {
									realXpath += reportField
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
							if (reportField.getNodeXPath().contains(".exist")
									|| reportField.getNodeXPath().contains(
											".value")) {
								if (value.equals("=1") || value.equals("=0")) {
									realXpath = reportField.getNodeXPath()
											.replace("{value}",
													value.toUpperCase());
								} else {
									realXpath = reportField.getNodeXPath()
											.replace(
													"{value}",
													"\"" + value.toUpperCase()
															+ "\"");
								}
							} else if (value.contains("'")) {
								realXpath = reportField.getNodeXPath().replace(
										"{value}", value.toUpperCase());
							} else if(value.toUpperCase().equals("IN")||value.toUpperCase().equals("NOT IN")){
								realXpath = reportField.getNodeXPath().replace("{value}", value);
							}else {
								realXpath = reportField.getNodeXPath().replace(
										"{value}",
										"'" + value.toUpperCase() + "'");
							}
						}
					}

					if (!reportField.getOperator().toString().equals("BETWEEN")) {
						realXpath = realXpath.replace("{operator}", reportField
								.getOperator().getRealOperator());
					}
					fieldsRealXPathMap.put("{" + fieldIdentifier
							+ ".search-xpath}", realXpath);
					// fieldsRealXPathMap.put("{" + fieldIdentifier +
					// ".report-xpath}", reportField.getReportableXPath());
				}
			} catch (Exception e){
				e.printStackTrace();
			}
		}
		finalResultXml = finalResultXml+generateSummaryCriteriaTable(reportTemplate,
				queryCriteriasValueMap);
		
		finalResultXml += "<report-result id=\""
				+ reportTemplate.getTypeDescription() + "\"  created=\""
				+ DateFormatUtil.formateDateToMDY(new Date()) + "\">";
		finalResultXml += "<title>" + reportTemplate.getDescription()
				+ "</title>";
		
		String rawQeury = generateRawQeury(reportTemplate,
				fieldsRealXPathMap);
		List<String> rawQueryResultSearchFields  =Lists.newArrayList();
		rawQueryResultSearchFields.add("contractid");
		rawQeury = rawQeury.replace("{reportstatment}",
				generateReportStatement(reportTemplate, rawQueryResultSearchFields));
		String realQeury = fillMessage(rawQeury, fieldsRealXPathMap);

		logger.debug("real query: " + realQeury);
		List<Map> resultObjectLst = getReportResultDao().generateResult(
				realQeury);
		List<ReportField> reportFields = reportFieldDao
					.listAllFieldsByReportTemplateId(reportTemplate.getId());
			List<ReportField> sortedReportFields = Lists.newArrayList();
			for(int i =0;i<reportFields.size();i++){
				for(ReportField field: reportFields){
				if(field.getField().contains("\""+i+"\"")){
					sortedReportFields.add(field);
					if(i==0){
						continue;
					}
					break;
				}
				
			}
		}

		ReportResultFieldTemplate reportResultFieldTemplate = null;
		try {
			finalResultXml += "<fields>";

			for (ReportField reportField : reportFields) {
				reportResultFieldTemplate = objectMapper
						.readValue(reportField.getField(),
								ReportResultFieldTemplate.class);
				String desc = reportResultFieldTemplate.getFieldDisplayName();

				String identifier = reportResultFieldTemplate
						.getFieldIdentifier();

				// should be able to edit
				String hidden = "false";

				finalResultXml += "<field id=\"" + identifier + "\" desc=\""
						+ desc + "\" hidden=\"" + hidden + "\" />";
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		finalResultXml += "</fields>";
		finalResultXml += "<report-items>";
		for(Map rowObject : resultObjectLst){
			BigInteger contractIdBig = (BigInteger) rowObject.get("contractId");
			Contract contract = contractDao.findById(contractIdBig.longValue());
			finalResultXml += "<report-item>";
			try{
				
				finalResultXml += "<field id=\"" + "contractid" + "\">";
				
				String value = "<![CDATA[<a target=\"_blank\" href=\"{application.host}/clara-webapp/contracts/"+contract.getId()+"/dashboard\">"+contract.getId()+"</a>]]>";
				
				value = replaceValues(value);
				finalResultXml +=value;
				finalResultXml += "</field>";
				
				Map<String, String> timeForCommittees = getTimeInEachQueueByContractId(contract.getId());
				String reviewEntities = "";
				String timeSpans = "";
						
				for(String reviewEntity:timeForCommittees.keySet()){
					reviewEntities += "<item>"+reviewEntity+"</item>";
					timeSpans += "<item>"+timeForCommittees.get(reviewEntity)+"</item>";
				}
			
				finalResultXml += "<field id=\"" + "reviewentity" + "\"><list>";
				finalResultXml +=reviewEntities;
				finalResultXml += "</list></field>";
				
				finalResultXml += "<field id=\"" + "timespent" + "\"><list>";
				finalResultXml +=timeSpans;
				finalResultXml += "</list></field>";
				
				
				
			}catch(Exception e){
				e.printStackTrace();
			}
			finalResultXml += "</report-item>";
		}
		
		finalResultXml += "</report-items>";

		finalResultXml += "</report-result>";

		finalResultXml += "</report-results>";
		finalResultXml = finalResultXml.replaceAll("null", "");
		finalResultXml = finalResultXml.replaceAll("&", "&amp;");
		logger.debug(finalResultXml);
		return finalResultXml;
	}
	
	private Map<String, String> getTimeInEachQueueByContractId(long contractId) {
		List<ContractForm> cts = contractFormDao.listContractFormsByContractId(contractId);
		
		Map<String, String> resultMap = Maps.newTreeMap();
		
		for (ContractForm ct : cts) {
			if (ct.getId() != ct.getParent().getId()) {
				continue;
			}

			for (Committee committee : committees) {
				long totalTime = 0;
				long startTime = 0;
				long endTime = 0;

				List<ContractFormCommitteeStatus> cfcss = contractFormCommitteeStatusDao
						.listAllByCommitteeAndContractFormId(committee,
								ct.getFormId());
				for (int i = 0; i < cfcss.size(); i++) {
					ContractFormCommitteeStatus cfcs = cfcss.get(i);
					List<ContractFormCommitteeStatusEnum> startActions = committeeactions.getStartContractCommitteeStatusMap().get(committee);
					List<ContractFormCommitteeStatusEnum> endActions = committeeactions.getEndContractCommitteeStatusMap().get(committee);
					
					if (startActions == null) {
						continue;
					}

					if (startActions.contains(cfcs
							.getContractFormCommitteeStatus())) {
						startTime = cfcs.getModified().getTime();

					} else if (startTime > 0
							&& endActions.contains(cfcs
									.getContractFormCommitteeStatus())) {
						endTime = cfcs.getModified().getTime();
					}

					if (startTime > 0 && endTime == 0
							&& i == (cfcss.size() - 1)) {
						endTime = new Date().getTime();
					}

					if (startTime > 0 && endTime > 0) {
						totalTime += endTime - startTime;
						if (endTime - startTime < 0) {
							logger.debug(ct.getFormId() + "#######"
									+ (endTime - startTime));
						}
						startTime = 0;
						endTime = 0;

					}
				}
				if (totalTime > 0) {
					totalTime = 1 + totalTime / (24 * 60 * 60 * 1000);
					resultMap.put(committee.getDescription(), totalTime + "");
				}
			}
		}
		
		return resultMap;
	}

	public ContractFormDao getContractFormDao() {
		return contractFormDao;
	}

	@Autowired(required = true)
	public void setContractFormDao(ContractFormDao contractFormDao) {
		this.contractFormDao = contractFormDao;
	}

	public ContractFormCommitteeStatusDao getContractFormCommitteeStatusDao() {
		return contractFormCommitteeStatusDao;
	}

	@Autowired(required = true)
	public void setContractFormCommitteeStatusDao(
			ContractFormCommitteeStatusDao contractFormCommitteeStatusDao) {
		this.contractFormCommitteeStatusDao = contractFormCommitteeStatusDao;
	}

	public ContractDao getContractDao() {
		return contractDao;
	}

	@Autowired(required = true)
	public void setContractDao(ContractDao contractDao) {
		this.contractDao = contractDao;
	}

	public ReportFieldDao getReportFieldDao() {
		return reportFieldDao;
	}

	@Autowired(required = true)
	public void setReportFieldDao(ReportFieldDao reportFieldDao) {
		this.reportFieldDao = reportFieldDao;
	}

}
