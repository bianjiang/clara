package edu.uams.clara.webapp.report.service.customreport.impl;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;

import edu.uams.clara.core.util.xml.XmlHandler;
import edu.uams.clara.core.util.xml.XmlHandlerFactory;
import edu.uams.clara.webapp.common.util.DateFormatUtil;
import edu.uams.clara.webapp.protocol.dao.irb.AgendaDao;
import edu.uams.clara.webapp.protocol.dao.irb.AgendaItemDao;
import edu.uams.clara.webapp.protocol.domain.Protocol;
import edu.uams.clara.webapp.protocol.domain.irb.Agenda;
import edu.uams.clara.webapp.protocol.domain.irb.AgendaItem.AgendaItemCategory;
import edu.uams.clara.webapp.protocol.domain.irb.AgendaItemWrapper;
import edu.uams.clara.webapp.report.domain.ReportCriteria;
import edu.uams.clara.webapp.report.domain.ReportFieldTemplate;
import edu.uams.clara.webapp.report.domain.ReportTemplate;
import edu.uams.clara.webapp.report.service.customreport.CustomReportService;

public class AgendaReportServiceImpl extends CustomReportService{
	private final static Logger logger = LoggerFactory.getLogger(CancerRelatedReportServiceImpl.class);
	private AgendaItemDao agendaItemDao;
	private AgendaDao agendaDao;
	
	@Override
	public String generateReportResult(ReportTemplate reportTemplate) {
		
	List<ReportCriteria> criterias = reportTemplate.getReportCriterias();
	
	String finalResultXml = "<report-results>";
	
	ObjectMapper objectMapper = new ObjectMapper();
		
	Map<String, String> fieldsRealXPathMap = Maps.newHashMap();
		
		for (ReportCriteria rc : criterias) {
			ReportFieldTemplate reportCriteriaField = new ReportFieldTemplate();
			
			try {
				reportCriteriaField = objectMapper.readValue(rc.getCriteria(), ReportFieldTemplate.class);
				
				String fieldIdentifier = reportCriteriaField.getFieldIdentifier();
				
				String value = reportCriteriaField.getValue();
				
				if (value != null && !value.isEmpty()) {
					String realXpath = "";
					
					if(reportCriteriaField.getOperator().toString().equals("AFTER")||reportCriteriaField.getOperator().toString().equals("BEFORE")){
							realXpath = reportCriteriaField.getNodeXPath().replace("{value}", "'"+ value.toUpperCase() +"'");
					}else if(reportCriteriaField.getOperator().toString().equals("BETWEEN")){
						realXpath =reportCriteriaField.getNodeXPath().replace("{value}", "'"+ value.toUpperCase().substring(0,value.toUpperCase().indexOf(",")) +"'");
						realXpath = realXpath.replace("{operator}", ">");
						realXpath = realXpath +" AND "+reportCriteriaField.getNodeXPath().replace("{value}", "'"+ value.toUpperCase().substring(value.toUpperCase().indexOf(",")+1,value.length()) +"'");;
						realXpath = realXpath.replace("{operator}", "<");
						
					}
					else{
						if(value.contains("|")){
							String[] values = value.split("\\|");
							realXpath += "(";
							for(int i=0;i<values.length;i++){
									values[i] = values[i].toUpperCase();
								if(i>0){
									realXpath+=" OR ";
								}
								if (reportCriteriaField.getNodeXPath().contains(".exist") || reportCriteriaField.getNodeXPath().contains(".value")) {
									if(value.equals("=1")||value.equals("=0")){
										realXpath += reportCriteriaField.getNodeXPath().replace("{value}", values[i]);
									}else{
									realXpath += reportCriteriaField.getNodeXPath().replace("{value}", "\""+ values[i] +"\"");
									}
								} else if(values[i].contains("'")){
									realXpath += reportCriteriaField.getNodeXPath().replace("{value}", values[i].toUpperCase());
								}
								else{
									realXpath += reportCriteriaField.getNodeXPath().replace("{value}", "'"+ values[i] +"'");
								}
							}
							realXpath += ")";
						}else{
								value = value.toUpperCase();
						if (reportCriteriaField.getNodeXPath().contains(".exist") || reportCriteriaField.getNodeXPath().contains(".value")) {
							if(value.equals("=1")||value.equals("=0")){
								realXpath = reportCriteriaField.getNodeXPath().replace("{value}", value);
							}else{
						realXpath = reportCriteriaField.getNodeXPath().replace("{value}", "\""+ value +"\"");
							}
					} else if(value.contains("'")){
						realXpath = reportCriteriaField.getNodeXPath().replace("{value}", value);
					}else if(value.toUpperCase().equals("IN")||value.toUpperCase().equals("NOT IN")){
						realXpath = reportCriteriaField.getNodeXPath().replace("{value}", value);
					}
					else{
						realXpath = reportCriteriaField.getNodeXPath().replace("{value}", "'"+ value +"'");
					}}
						
						
					}
					
					if(!reportCriteriaField.getOperator().toString().equals("BETWEEN")){
						realXpath = realXpath.replace("{operator}", reportCriteriaField.getOperator().getRealOperator());
					}
					fieldsRealXPathMap.put("{" + fieldIdentifier + ".search-xpath}" , realXpath);
					//fieldsRealXPathMap.put("{" + fieldIdentifier + ".report-xpath}", reportField.getReportableXPath());
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
		
		finalResultXml += "<report-result id=\""+ reportTemplate.getTypeDescription() +"\"  created=\""+ DateFormatUtil.formateDateToMDY(new Date()) +"\">";
		finalResultXml += "<title>"+ reportTemplate.getDescription() +"</title>";
		finalResultXml += "<fields>";
		finalResultXml += "<field id=\"agendadate\" desc=\"Agenda Date\" hidden=\"false\" />";
		finalResultXml += "<field id=\"protocolid\" desc=\"IRB #\" hidden=\"false\" />";
		finalResultXml += "<field id=\"agendaitemtype\" desc=\"Agenda Item Type\" hidden=\"false\" />";
		finalResultXml += "<field id=\"protocolformtype\" desc=\"Protocol Form Type Type\" hidden=\"false\" />";
		finalResultXml += "<field id=\"piname\" desc=\"PI Name\" hidden=\"false\" />";
		finalResultXml += "<field id=\"college\" desc=\"College\" hidden=\"false\" />";
		finalResultXml += "<field id=\"department\" desc=\"Department\" hidden=\"false\" />";
		finalResultXml += "</fields>";
		finalResultXml += "<report-items>";
		String rawQuery = generateRawQeury(reportTemplate, fieldsRealXPathMap);
		
		String realQuery = fillMessage(rawQuery, fieldsRealXPathMap);
		
		logger.debug("real query: " + realQuery);
		List<Map> resultObjectLst = getReportResultDao().generateResult(realQuery);
		try {
			XmlHandler xmlHandler = XmlHandlerFactory.newXmlHandler();
			for (Map rowObject: resultObjectLst) {
				
				long agendaId = ((BigInteger) rowObject.get("id")).longValue();
				Agenda agenda = agendaDao.findById(agendaId);
				
				
				List<AgendaItemWrapper> agendaItems = agendaItemDao.listByAgendaId(agendaId);
				String tempXmlForPid = "";
				String tempXmlForPI = "";
				String tempXmlForCollege = "";
				String tempXmlForDept = "";
				String tempXmlForAgendaItemType="";
				String tempXmlForFormType ="";
				for(AgendaItemWrapper agendaItem :agendaItems){
					try{
					if(agendaItem.getAgendaItemCategory().equals(AgendaItemCategory.MINUTES)){
						continue;
					}
					Protocol p = agendaItem.getProtocolForm().getProtocol();
					
					if(!tempXmlForPid.isEmpty()){
						tempXmlForPid+="&lt;br&gt;";
					}else{
						tempXmlForPid += "<field id=\"protocolid\">";
					}
					tempXmlForPid +="&lt;a target=&quot;_blank&quot; href=&quot;"+this.getAppHost()+"/clara-webapp/protocols/"+p.getId()+"/dashboard&quot;&gt;"+p.getId()+"&lt;/a&gt;";
					
					if(!tempXmlForAgendaItemType.isEmpty()){
						tempXmlForAgendaItemType+="&lt;br&gt;";
					}else{
						tempXmlForAgendaItemType += "<field id=\"agendaitemtype\">";
					}
					tempXmlForAgendaItemType+=agendaItem.getAgendaItemCategory();
					
					if(!tempXmlForFormType.isEmpty()){
						tempXmlForFormType+="&lt;br&gt;";
					}else{
						tempXmlForFormType += "<field id=\"protocolformtype\">";
					}
					tempXmlForFormType+=agendaItem.getProtocolForm().getFormType();
							
					String protocolXml = p.getMetaDataXml();
					String piName =  xmlHandler.getSingleStringValueByXPath(protocolXml, "/protocol/staffs/staff/user[roles/role/text()=\"Principal Investigator\"]/lastname/text()") 
							+" "+xmlHandler.getSingleStringValueByXPath(protocolXml, "/protocol/staffs/staff/user[roles/role/text()=\"Principal Investigator\"]/firstname/text()");
					
					if(!tempXmlForPI.isEmpty()){
						tempXmlForPI+="&lt;br&gt;";
					}else{
						tempXmlForPI += "<field id=\"piname\">";
					}
					tempXmlForPI +=piName;
					
					String college =  xmlHandler.getSingleStringValueByXPath(protocolXml, "/protocol/responsible-department/@collegedesc");
					if(college.contains("&")){
					college=college.replaceAll("&", "&amp;");
					}
					if(!tempXmlForCollege.isEmpty()){
						tempXmlForCollege+="&lt;br&gt;";
					}else{
						tempXmlForCollege += "<field id=\"college\">";
					}
					tempXmlForCollege +=college;
					
					String department =  xmlHandler.getSingleStringValueByXPath(protocolXml, "/protocol/responsible-department/@deptdesc");
					if(department.contains("&")){
						department=department.replaceAll("&", "&amp;");
					}
					if(!tempXmlForDept.isEmpty()){
						tempXmlForDept+="&lt;br&gt;";
					}else{
						tempXmlForDept += "<field id=\"department\">";
					}
					tempXmlForDept +=department;
					}catch(Exception e){
						//do nothing, incase some protocol has issue
					}
				}
				
				if(!tempXmlForPid.isEmpty()){
					tempXmlForPid += "</field>";
				}
				if(!tempXmlForFormType.isEmpty()){
					tempXmlForFormType += "</field>";
				}
				if(!tempXmlForPI.isEmpty()){
					tempXmlForPI += "</field>";
				}
				if(!tempXmlForCollege.isEmpty()){
					tempXmlForCollege += "</field>";
				}
				if(!tempXmlForDept.isEmpty()){
					tempXmlForDept += "</field>";
				}
				if(!tempXmlForAgendaItemType.isEmpty()){
					tempXmlForAgendaItemType += "</field>";
				}
				
				finalResultXml += "<report-item>";
				finalResultXml += "<field id=\"agendadate\">";
				finalResultXml += agenda.getDate();
				finalResultXml += "</field>";
				finalResultXml = finalResultXml+tempXmlForPid+tempXmlForAgendaItemType+tempXmlForFormType+tempXmlForPI+tempXmlForCollege+tempXmlForDept;
				finalResultXml += "</report-item>";
			} 
		}catch (Exception e) {
			e.printStackTrace();
		}
		
		finalResultXml += "</report-items>";
		finalResultXml += "</report-result>";
		finalResultXml += "</report-results>";
		finalResultXml =finalResultXml.replace("<![CDATA[null]]>", "");
		finalResultXml =finalResultXml.replace("null&lt;br&gt;", "");
		finalResultXml =finalResultXml.replace("&gt;null", "&gt;");
		//logger.debug(finalResultXml);
		
		return finalResultXml;
	}

	public AgendaItemDao getAgendaItemDao() {
		return agendaItemDao;
	}

	@Autowired(required = true)
	public void setAgendaItemDao(AgendaItemDao agendaItemDao) {
		this.agendaItemDao = agendaItemDao;
	}

	public AgendaDao getAgendaDao() {
		return agendaDao;
	}

	@Autowired(required = true)
	public void setAgendaDao(AgendaDao agendaDao) {
		this.agendaDao = agendaDao;
	}
	

}
