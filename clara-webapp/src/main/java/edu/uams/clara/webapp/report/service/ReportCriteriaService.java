package edu.uams.clara.webapp.report.service;

import edu.uams.clara.webapp.report.domain.ReportFieldTemplate;
import edu.uams.clara.webapp.report.domain.ReportFieldTemplate.Operator;

public interface ReportCriteriaService {
	ReportFieldTemplate getReportFieldTemplate(String fieldIdentifier, Operator operator, String value, String displayValue);
}
