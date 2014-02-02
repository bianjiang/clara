package edu.uams.clara.webapp.report.service.impl;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import edu.uams.clara.core.util.xml.XmlHandler;
import edu.uams.clara.core.util.xml.XmlHandlerFactory;
import edu.uams.clara.webapp.common.util.DateFormatUtil;
import edu.uams.clara.webapp.report.dao.ReportFieldDao;
import edu.uams.clara.webapp.report.domain.CommitteeActions;
import edu.uams.clara.webapp.report.domain.ReportCriteria;
import edu.uams.clara.webapp.report.domain.ReportField;
import edu.uams.clara.webapp.report.domain.ReportFieldTemplate;
import edu.uams.clara.webapp.report.domain.ReportResultFieldTemplate;
import edu.uams.clara.webapp.report.domain.ReportTemplate;
import edu.uams.clara.webapp.report.service.customreport.CustomReportService;

public class ReportResultServiceImpl extends CustomReportService {
	private final static Logger logger = LoggerFactory.getLogger(ReportResultServiceImpl.class);
	
	private ReportFieldDao reportFieldDao;
	
	private String replaceValues(String value){
		
		for(Entry<String,String> values:this.getDefaultValuesMap().entrySet()){
			//*************should replace later*****/
			value = value.replace("{" + values.getKey() + "}",
					values.getValue());
		}
		
		return value;
	}
	
	private  List<String> lowercaseIdentifiers = Lists.newArrayList();
	{
		lowercaseIdentifiers.add("sites");
	}
	

	@Override
	public String generateReportResult(ReportTemplate reportTemplate) {
		String finalResultXml = "<report-results>";
		finalResultXml += "<report-result id=\""+ reportTemplate.getTypeDescription() +"\"  created=\""+ DateFormatUtil.formateDateToMDY(new Date()) +"\">";
		finalResultXml += "<title>"+ reportTemplate.getDescription() +"</title>";
		
		List<ReportCriteria> criterias = reportTemplate.getReportCriterias();
		
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
								if(!this.getLowercaseIdentifiers().contains(fieldIdentifier)){
									values[i] = values[i].toUpperCase();
								}
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
									realXpath += reportCriteriaField.getNodeXPath().replace("{value}", values[1].toUpperCase());
								}
								else{
									realXpath += reportCriteriaField.getNodeXPath().replace("{value}", "'"+ values[i] +"'");
								}
							}
							realXpath += ")";
						}else{
							if(!this.getLowercaseIdentifiers().contains(fieldIdentifier)){
								value = value.toUpperCase();
							}
						if (reportCriteriaField.getNodeXPath().contains(".exist") || reportCriteriaField.getNodeXPath().contains(".value")) {
							if(value.equals("=1")||value.equals("=0")){
								realXpath = reportCriteriaField.getNodeXPath().replace("{value}", value);
							}else{
						realXpath = reportCriteriaField.getNodeXPath().replace("{value}", "\""+ value +"\"");
							}
					} else if(value.contains("'")){
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
		
		try {
			List<ReportField> reportFields =reportFieldDao.listAllFieldsByReportTemplateId(reportTemplate.getId());
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
			finalResultXml += "<fields>";
			ReportResultFieldTemplate reportResultFieldTemplate = null;
			List<String> resultsFiledsForDisplay = Lists.newArrayList();
			for (ReportField reportField:sortedReportFields) {
				reportResultFieldTemplate = objectMapper.readValue(reportField.getField(), ReportResultFieldTemplate.class);
				String desc = reportResultFieldTemplate.getFieldDisplayName();
				
				String identifier = reportResultFieldTemplate.getFieldIdentifier();
				resultsFiledsForDisplay.add(identifier);
				//should be able to edit
				String hidden = "false";
				
				finalResultXml += "<field id=\""+ identifier +"\" desc=\""+ desc +"\" hidden=\""+ hidden +"\" />";
			}
			
			finalResultXml += "</fields>";
			
			finalResultXml += "<report-items>";
			
			String rawQeury = generateRawQeury(reportTemplate, fieldsRealXPathMap);
			
			String reportStatement = generateReportStatement(reportTemplate,resultsFiledsForDisplay);
			
			rawQeury = rawQeury.replace("{reportstatment}", reportStatement);
			if(reportStatement.contains("FROM protocol")){
				rawQeury=rawQeury.replace("FROM protocol WHERE", "WHERE");
			}
			String realQeury = fillMessage(rawQeury, fieldsRealXPathMap);
			logger.debug("real query: " + realQeury);
			
			
			List<Map> resultObjectLst = getReportResultDao().generateResult(realQeury);
			
			
			Map<String,String> resultXmls = Maps.newHashMap();
			Set<String> pids= resultXmls.keySet();
			for (Map rowObject: resultObjectLst) {
				String singleResultXml = "";
				singleResultXml += "<report-item>";
				String protocolId = (String) rowObject.get("protocolId");
				pids= resultXmls.keySet();
				for (ReportField reportField:sortedReportFields) {
					reportResultFieldTemplate = objectMapper.readValue(reportField.getField(), ReportResultFieldTemplate.class);
					
					String identifier = reportResultFieldTemplate.getFieldIdentifier();
					String alias = reportResultFieldTemplate.getAlias();
					String value = reportResultFieldTemplate.getValue();
					
					
					if(value == null||value.isEmpty()){
						value = "<![CDATA[" + (String) rowObject.get(alias)  + "]]>";
					}else{
						value = replaceValues(value);
						try{
							value=	"<![CDATA[" + value.replace("{" + alias  + "}", (String) rowObject.get(alias))  + "]]>";
					
						}catch(Exception e){
							//do nothing
						}
					}
					
					singleResultXml += "<field id=\""+ identifier +"\">";
					
					singleResultXml += value;
					
					singleResultXml += "</field>";
				}
				singleResultXml += "</report-item>";
				
				if(pids.contains(protocolId)){
					XmlHandler xmlHandler = XmlHandlerFactory.newXmlHandler();
					String existingXml =  resultXmls.get(protocolId);
					for (ReportField reportField:sortedReportFields){
						reportResultFieldTemplate = objectMapper.readValue(reportField.getField(), ReportResultFieldTemplate.class);
						String identifier = reportResultFieldTemplate.getFieldIdentifier();
						
						String alias = reportResultFieldTemplate.getAlias();
						String value = (String) rowObject.get(alias);
						String valueExist = xmlHandler.getSingleStringValueByXPath(existingXml, "//report-item/field[@id=\""+identifier+"\"]/text()");
						if(valueExist==null){
							valueExist="";
						}
						if(value==null){
							value="";
						}
						if(!value.equals(valueExist)&&!alias.equals("protocolId")){
							existingXml=xmlHandler.replaceOrAddNodeValueByPath("//report-item/field[@id=\""+identifier+"\"]", existingXml, valueExist+"<br>"+value);
						}
					}
					resultXmls.put(protocolId,existingXml);
				}else{
				resultXmls.put(protocolId,singleResultXml);
				}
			}
			
			for(Entry<String,String> singleResultXml:resultXmls.entrySet()){
				finalResultXml+=singleResultXml.getValue();
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		finalResultXml += "</report-items>";
		
		finalResultXml += "</report-result>";
		
		finalResultXml += "</report-results>";
		
		finalResultXml =finalResultXml.replace("<![CDATA[null]]>", "");
		finalResultXml =finalResultXml.replace("null&lt;br&gt;", "");
		
		
		return finalResultXml;
	}

	public ReportFieldDao getReportFieldDao() {
		return reportFieldDao;
	}

	@Autowired(required=true)
	public void setReportFieldDao(ReportFieldDao reportFieldDao) {
		this.reportFieldDao = reportFieldDao;
	}
	

	public List<String> getLowercaseIdentifiers() {
		return lowercaseIdentifiers;
	}
}
