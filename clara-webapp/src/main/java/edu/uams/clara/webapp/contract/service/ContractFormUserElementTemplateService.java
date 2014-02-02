package edu.uams.clara.webapp.contract.service;

import edu.uams.clara.webapp.contract.domain.contractform.ContractFormUserElementTemplate.TemplateType;

public interface ContractFormUserElementTemplateService {

	String updateTemplateXMLData(String xmlData, TemplateType templateType);
}
