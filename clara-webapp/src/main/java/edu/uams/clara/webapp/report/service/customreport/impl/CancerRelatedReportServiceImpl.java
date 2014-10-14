package edu.uams.clara.webapp.report.service.customreport.impl;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import edu.uams.clara.core.util.xml.XmlHandler;
import edu.uams.clara.core.util.xml.XmlHandlerFactory;
import edu.uams.clara.webapp.common.util.DateFormatUtil;
import edu.uams.clara.webapp.report.dao.ReportFieldDao;
import edu.uams.clara.webapp.report.domain.ReportCriteria;
import edu.uams.clara.webapp.report.domain.ReportField;
import edu.uams.clara.webapp.report.domain.ReportFieldTemplate;
import edu.uams.clara.webapp.report.domain.ReportResultFieldTemplate;
import edu.uams.clara.webapp.report.domain.ReportTemplate;
import edu.uams.clara.webapp.report.service.customreport.CustomReportService;

public class CancerRelatedReportServiceImpl extends CustomReportService {
	private final static Logger logger = LoggerFactory.getLogger(CancerRelatedReportServiceImpl.class);

private ReportFieldDao reportFieldDao;
private  List<String> reviewTypes = Lists.newArrayList();
{
	reviewTypes.add("FULL BOARD");
	reviewTypes.add("EXPEDITED");
	reviewTypes.add("EXEMPT");
}
	
	private String replaceValues(String value){
		
		for(Entry<String,String> values:this.getDefaultValuesMap().entrySet()){
			//*************should replace later*****/
			value = value.replace("{" + values.getKey() + "}",
					values.getValue());
		}
		
		return value;
	}

	@Override
	public String generateReportResult(ReportTemplate reportTemplate) {
		String finalResultXml = "<report-results>";
		
		
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
			for(String reviewType: this.getReviewTypes()){
			String reviewTypeQryStr = " AND meta_data_xml.exist('/protocol/most-recent-study/approval-status/text()[fn:contains(fn:upper-case(.),\""+reviewType+"\")]')=1";
			
			finalResultXml += "<report-result id=\""+ reportTemplate.getTypeDescription() +"\"  created=\""+ DateFormatUtil.formateDateToMDY(new Date()) +"\">";
			finalResultXml += "<title>"+ reportTemplate.getDescription()+"-"+reviewType +"</title>";
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
			
			String rawQuery = generateRawQeury(reportTemplate, fieldsRealXPathMap);
			
			String reportStatement = generateReportStatement(reportTemplate,resultsFiledsForDisplay);
			
			rawQuery = rawQuery.replace("{reportstatment}", reportStatement);
			if(reportStatement.contains("FROM protocol")){
				rawQuery=rawQuery.replace("FROM protocol WHERE", "WHERE");
			}
			String realQuery = fillMessage(rawQuery, fieldsRealXPathMap);
			
			logger.debug("real query: " + realQuery);
			realQuery +=reviewTypeQryStr;
			
			List<Map> resultObjectLst = getReportResultDao().generateResult(realQuery);
			
			
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
			finalResultXml += "</report-items>";
			
			finalResultXml += "</report-result>";
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		finalResultXml += "</report-results>";
		
		finalResultXml =finalResultXml.replace("<![CDATA[null]]>", "");
		finalResultXml =finalResultXml.replace("null&lt;br&gt;", "");
		if(finalResultXml.contains("&")){
			finalResultXml=finalResultXml.replaceAll("&", "&amp;");
		}
		
		return finalResultXml;
	}

	public ReportFieldDao getReportFieldDao() {
		return reportFieldDao;
	}

	@Autowired(required=true)
	public void setReportFieldDao(ReportFieldDao reportFieldDao) {
		this.reportFieldDao = reportFieldDao;
	}
	public List<String> getReviewTypes() {
		return reviewTypes;
	}

}
