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
import org.w3c.dom.NodeList;

import com.google.common.collect.Lists;

import edu.uams.clara.webapp.report.domain.ReportFieldTemplate;
import edu.uams.clara.webapp.report.domain.ReportResultFieldTemplate;
import edu.uams.clara.webapp.report.domain.ReportTemplate;
import edu.uams.clara.webapp.report.service.ReportCriteriaService;
import edu.uams.clara.webapp.report.service.ReportTemplateService;
import edu.uams.clara.webapp.xml.processor.XmlProcessor;

public class ReportTemplateServiceImpl implements ReportTemplateService {
	private final static Logger logger = LoggerFactory.getLogger(ReportTemplateService.class);
	
	private ReportCriteriaService reportCriteriaService;
	
	private XmlProcessor xmlProcessor;
	
	@Value("${reportSearchableFieldsTemplateXml.url}")
	private String reportSearchableFieldsTemplateXml;

	@Override
	public List<ReportResultFieldTemplate> getDefaultReportResultFieldTemplateByFieldName(ReportTemplate reportTemplate){
		List<ReportResultFieldTemplate> reportResultFieldTemplates =Lists.newArrayList();
		
		String reportType = reportTemplate.getTypeDescription();
		try {
			Document doc = xmlProcessor.loadXmlFileToDOM(reportSearchableFieldsTemplateXml);
			
			XPath xPath = xmlProcessor.getXPathInstance();
			
			NodeList fieldNodeLst = (NodeList) xPath.evaluate("/reports/report[@type=\""+ reportType +"\"]/results/field", doc, XPathConstants.NODESET);
			for (int i = 0; i < fieldNodeLst.getLength(); i++) {
				Element currentFieldEl = (Element) fieldNodeLst.item(i);
				if(currentFieldEl.getAttribute("defaultDisplay").equals("false")){
					continue;
				}
				ReportResultFieldTemplate reportResultFieldTemplate =new ReportResultFieldTemplate();
				String fieldIdentifier = currentFieldEl.getAttribute("identifier");
				reportResultFieldTemplate.setDefaultDisplay(currentFieldEl.getAttribute("defaultDisplay"));
				reportResultFieldTemplate.setFieldDisplayName(currentFieldEl.getAttribute("desc"));
				reportResultFieldTemplate.setFieldIdentifier(fieldIdentifier);
				reportResultFieldTemplate.setOrder(currentFieldEl.getAttribute("order"));
				reportResultFieldTemplate.setValue(currentFieldEl.getAttribute("value"));
				reportResultFieldTemplate.setAlias(currentFieldEl.getAttribute("alias"));
				reportResultFieldTemplates.add(reportResultFieldTemplate);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return reportResultFieldTemplates;
		
	}
	
	@Override
	public ReportResultFieldTemplate getReportResultFieldTemplateByFieldName(String fieldName,ReportTemplate reportTemplate){
		ReportResultFieldTemplate reportResultFieldTemplate =new ReportResultFieldTemplate();
		String reportType = reportTemplate.getTypeDescription();
		try {
			Document doc = xmlProcessor.loadXmlFileToDOM(reportSearchableFieldsTemplateXml);
			
			XPath xPath = xmlProcessor.getXPathInstance();
			
			NodeList fieldNodeLst = (NodeList) xPath.evaluate("/reports/report[@type=\""+ reportType +"\"]/results/field", doc, XPathConstants.NODESET);
			
			for (int i = 0; i < fieldNodeLst.getLength(); i++) {
				Element currentFieldEl = (Element) fieldNodeLst.item(i);
				if(!fieldName.equals(currentFieldEl.getAttribute("identifier"))){
					continue;
				}
				String fieldIdentifier = currentFieldEl.getAttribute("identifier");
				reportResultFieldTemplate.setDefaultDisplay(currentFieldEl.getAttribute("defaultDisplay"));
				reportResultFieldTemplate.setFieldDisplayName(currentFieldEl.getAttribute("desc"));
				reportResultFieldTemplate.setFieldIdentifier(fieldIdentifier);
				reportResultFieldTemplate.setOrder(currentFieldEl.getAttribute("order"));
				reportResultFieldTemplate.setValue(currentFieldEl.getAttribute("value"));
				reportResultFieldTemplate.setAlias(currentFieldEl.getAttribute("alias"));
				break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return reportResultFieldTemplate;
		
	}
	
	@Override
	public List<ReportResultFieldTemplate> listAvailableFields(ReportTemplate reportTemplate){
		List<ReportResultFieldTemplate> reportFieldsLst = Lists.newArrayList();
		String reportType = reportTemplate.getTypeDescription();

		try {
			Document doc = xmlProcessor.loadXmlFileToDOM(reportSearchableFieldsTemplateXml);
			
			XPath xPath = xmlProcessor.getXPathInstance();
			
			NodeList fieldNodeLst = (NodeList) xPath.evaluate("/reports/report[@type=\""+ reportType +"\"]/results/field", doc, XPathConstants.NODESET);
			
			for (int i = 0; i < fieldNodeLst.getLength(); i++) {
				Element currentFieldEl = (Element) fieldNodeLst.item(i);
				
				String fieldIdentifier = currentFieldEl.getAttribute("identifier");

				ReportResultFieldTemplate reportResultFieldTemplate = new ReportResultFieldTemplate();
					
				reportResultFieldTemplate.setDefaultDisplay(currentFieldEl.getAttribute("defaultDisplay"));
				reportResultFieldTemplate.setFieldDisplayName(currentFieldEl.getAttribute("desc"));
				reportResultFieldTemplate.setFieldIdentifier(fieldIdentifier);
				reportResultFieldTemplate.setOrder(currentFieldEl.getAttribute("order"));
				reportResultFieldTemplate.setValue(currentFieldEl.getAttribute("value"));
				reportResultFieldTemplate.setAlias(currentFieldEl.getAttribute("alias"));
				
				reportFieldsLst.add(reportResultFieldTemplate);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return reportFieldsLst;
	}	
	
	@Override
	public List<ReportFieldTemplate> listAvailableCriterias(
			ReportTemplate reportTemplate) {
		List<ReportFieldTemplate> reportFieldsLst = Lists.newArrayList();
		String reportType = reportTemplate.getTypeDescription();

		try {
			Document doc = xmlProcessor.loadXmlFileToDOM(reportSearchableFieldsTemplateXml);
			
			XPath xPath = xmlProcessor.getXPathInstance();
			
			NodeList fieldNodeLst = (NodeList) xPath.evaluate("/reports/report[@type=\""+ reportType +"\"]/fields/field", doc, XPathConstants.NODESET);
			
			for (int i = 0; i < fieldNodeLst.getLength(); i++) {
				Element currentFieldEl = (Element) fieldNodeLst.item(i);
				
				String fieldIdentifier = currentFieldEl.getAttribute("identifier");

				ReportFieldTemplate reportField = reportCriteriaService.getReportFieldTemplate(fieldIdentifier, null, "", "");
				
				reportFieldsLst.add(reportField);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return reportFieldsLst;
	}

	public ReportCriteriaService getReportCriteriaService() {
		return reportCriteriaService;
	}
	
	@Autowired(required = true)
	public void setReportCriteriaService(ReportCriteriaService reportCriteriaService) {
		this.reportCriteriaService = reportCriteriaService;
	}

	public String getReportSearchableFieldsTemplateXml() {
		return reportSearchableFieldsTemplateXml;
	}

	public void setReportSearchableFieldsTemplateXml(
			String reportSearchableFieldsTemplateXml) {
		this.reportSearchableFieldsTemplateXml = reportSearchableFieldsTemplateXml;
	}

	public XmlProcessor getXmlProcessor() {
		return xmlProcessor;
	}
	
	@Autowired(required = true)
	public void setXmlProcessor(XmlProcessor xmlProcessor) {
		this.xmlProcessor = xmlProcessor;
	}

}
