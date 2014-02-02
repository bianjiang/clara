package edu.uams.clara.webapp.report.service;

import java.util.List;

import edu.uams.clara.webapp.report.domain.ReportFieldTemplate;
import edu.uams.clara.webapp.report.domain.ReportResultFieldTemplate;
import edu.uams.clara.webapp.report.domain.ReportTemplate;

public interface ReportTemplateService {
	List<ReportFieldTemplate> listAvailableCriterias(ReportTemplate reportTemplate);
	
	List<ReportResultFieldTemplate> listAvailableFields(ReportTemplate reportTemplate);
	
	ReportResultFieldTemplate getReportResultFieldTemplateByFieldName(String fieldName,ReportTemplate reportTemplate);
	
	List<ReportResultFieldTemplate> getDefaultReportResultFieldTemplateByFieldName(ReportTemplate reportTemplate);
}
