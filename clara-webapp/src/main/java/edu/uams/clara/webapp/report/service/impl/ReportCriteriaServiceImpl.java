package edu.uams.clara.webapp.report.service.impl;

import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.common.collect.Lists;

import edu.uams.clara.webapp.report.domain.ReportFieldTemplate;
import edu.uams.clara.webapp.report.domain.ReportFieldTemplate.Operator;
import edu.uams.clara.webapp.report.service.ReportCriteriaService;
import edu.uams.clara.webapp.xml.processor.XmlProcessor;

public class ReportCriteriaServiceImpl implements ReportCriteriaService {
	private final static Logger logger = LoggerFactory.getLogger(ReportCriteriaService.class);
	
	private XmlProcessor xmlProcessor;
	
	@Value("${reportFieldTemplateXml.url}")
	private String reportFieldTemplateXml;
	
	private List<String> subStudyTypeList = Lists.newArrayList();{
		subStudyTypeList.add("local-faculty");
		subStudyTypeList.add("non-local-faculty");
		subStudyTypeList.add("student-fellow-resident-post-doc");
		subStudyTypeList.add("other");
	}
	
	private String replaceSearchPathForStudyTypeSubTagQuery(String fieldIdentifier){
		fieldIdentifier = "substudytype";
		return fieldIdentifier;
	}
	
	@Override
	public ReportFieldTemplate getReportFieldTemplate(String fieldIdentifier,
			Operator operator, String value, String displayValue){
		ReportFieldTemplate reportFieldTemplate = new ReportFieldTemplate();
		
		try {
			Document doc = xmlProcessor.loadXmlFileToDOM(reportFieldTemplateXml);
			
			XPath xPath = xmlProcessor.getXPathInstance();
			
			if(subStudyTypeList.contains(value)){
				fieldIdentifier=replaceSearchPathForStudyTypeSubTagQuery(fieldIdentifier);
			}
			
			Element fieldEl = (Element) xPath.evaluate("/fields/field[@identifier=\""+ fieldIdentifier +"\"]", doc, XPathConstants.NODE);
			Object dataSourceObject = Class.forName(fieldEl.getAttribute("data-source"));
			
			String nodeXpath = fieldEl.getAttribute("search-xpath");
			
			String reportableXpath = fieldEl.getAttribute("report-xpath");
			
			String xType = fieldEl.getAttribute("xtype");
			
			String fieldDisplayName = fieldEl.getAttribute("display-name");
			
			String fieldAllowedOperators = fieldEl.getAttribute("allowed-operators");
			
			reportFieldTemplate.setDataSourceObject(dataSourceObject);
			reportFieldTemplate.setAllowedOperators(fieldAllowedOperators);
			reportFieldTemplate.setDisplayValue(displayValue);
			reportFieldTemplate.setFieldDisplayName(fieldDisplayName);
			reportFieldTemplate.setFieldIdentifier(fieldIdentifier);
			reportFieldTemplate.setFieldXType(xType);
			reportFieldTemplate.setNodeXPath(nodeXpath);
			reportFieldTemplate.setReportableXPath(reportableXpath);
			reportFieldTemplate.setOperator(operator);
			reportFieldTemplate.setValue(value);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return reportFieldTemplate;
	}

	public XmlProcessor getXmlProcessor() {
		return xmlProcessor;
	}
	
	@Autowired(required = true)
	public void setXmlProcessor(XmlProcessor xmlProcessor) {
		this.xmlProcessor = xmlProcessor;
	}

	public String getReportFieldTemplateXml() {
		return reportFieldTemplateXml;
	}

	public void setReportFieldTemplateXml(String reportFieldTemplateXml) {
		this.reportFieldTemplateXml = reportFieldTemplateXml;
	}

}
