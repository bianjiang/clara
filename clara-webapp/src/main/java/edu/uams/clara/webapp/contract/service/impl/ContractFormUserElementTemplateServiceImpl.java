package edu.uams.clara.webapp.contract.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import edu.uams.clara.webapp.contract.domain.contractform.ContractFormUserElementTemplate.TemplateType;
import edu.uams.clara.webapp.contract.service.ContractFormUserElementTemplateService;
import edu.uams.clara.webapp.xml.processor.XmlProcessor;

public class ContractFormUserElementTemplateServiceImpl implements
		ContractFormUserElementTemplateService {
	private final static Logger logger = LoggerFactory
			.getLogger(ContractFormUserElementTemplateService.class);

	private XmlProcessor xmlProcessor;

	@Override
	public String updateTemplateXMLData(String xmlData,
			TemplateType templateType) {
		String updatedXmlData = "";

		switch (templateType) {
		case BUDGET: // update budget costs
			// updatedXmlData = budgetXmlCostUpdate.updateCost(xmlData);
			break;
		case STAFF:// for staff, the id needs to updated before send it to the
					// client
			try {

				// xmlData = "<staffs>" + xmlData + "</staffs>";

				updatedXmlData = xmlProcessor.newElementIdByPath(
						"/staffs/staff", xmlData);

				// updatedXmlData = updatedXmlData.replace("<staffs>", "");
				// updatedXmlData = updatedXmlData.replace("</staffs>", "");
				Assert.hasText(updatedXmlData);
			} catch (Exception ex) {
				logger.error(
						"failed update the uuid on staff when loading from template",
						ex);
			}
			break;
		default:
			break;
		}
		return updatedXmlData;
	}

	public XmlProcessor getXmlProcessor() {
		return xmlProcessor;
	}

	@Autowired(required = true)
	public void setXmlProcessor(XmlProcessor xmlProcessor) {
		this.xmlProcessor = xmlProcessor;
	}
}
