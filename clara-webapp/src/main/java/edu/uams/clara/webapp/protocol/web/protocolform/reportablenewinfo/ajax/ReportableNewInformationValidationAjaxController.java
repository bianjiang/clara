package edu.uams.clara.webapp.protocol.web.protocolform.reportablenewinfo.ajax;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.xpath.XPathExpressionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.xml.sax.SAXException;


import edu.uams.clara.webapp.common.businesslogic.form.validator.ValidationResponse;
import edu.uams.clara.webapp.common.businesslogic.form.validator.ValidationRuleContainer;
import edu.uams.clara.webapp.common.businesslogic.form.validator.ValidationRuleHandler;
import edu.uams.clara.webapp.common.businesslogic.form.validator.constraint.Constraint;
import edu.uams.clara.webapp.common.businesslogic.form.validator.constraint.enums.ConstraintLevel;
import edu.uams.clara.webapp.common.businesslogic.form.validator.rule.Rule;
import edu.uams.clara.webapp.common.businesslogic.form.validator.rule.ValidationRule;
import edu.uams.clara.webapp.protocol.dao.ProtocolDocumentDao;
import edu.uams.clara.webapp.protocol.dao.protocolform.ProtocolFormDao;
import edu.uams.clara.webapp.protocol.dao.protocolform.ProtocolFormXmlDataDao;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolFormXmlData;
import edu.uams.clara.webapp.xml.processor.XmlProcessor;

@Controller
public class ReportableNewInformationValidationAjaxController {
	
	private final static Logger logger = LoggerFactory
	.getLogger(ReportableNewInformationValidationAjaxController.class);

	private ProtocolFormDao protocolFormDao;

	private ProtocolFormXmlDataDao protocolFormXmlDataDao;

	private ProtocolDocumentDao protocolDocumentDao;

	private XmlProcessor xmlProcessor;

	private ValidationRuleHandler validationRuleHandler;

	private ValidationRuleContainer validationRuleContainer;
	
	private boolean isReportableOrNot(ProtocolFormXmlData protocolXmlData){
		boolean isReportableOrNot = false;
		
		try{
			List<String> values = xmlProcessor.listElementStringValuesByPath("/reportable-new-information/is-reportable", protocolXmlData.getXmlData());
			
			if (values.size() > 0){
				if (values.get(0).equals("y")){
					isReportableOrNot = true;
				} else {
					isReportableOrNot = false;
				}
			}
		} catch (Exception e){
			e.printStackTrace();
			isReportableOrNot = false;
		}
		
		return isReportableOrNot;
	}

	@RequestMapping(value = "/ajax/protocols/{protocolId}/protocol-forms/{protocolFormId}/reportable-new-information/protocol-form-xml-datas/{protocolFormXmlDataId}/validate", method = RequestMethod.GET)
	public @ResponseBody
	List<ValidationResponse> validateProtocolReportableNewInformationForm(
			@PathVariable("protocolFormXmlDataId") long protocolFormXmlDataId) {

		ProtocolFormXmlData protocolXmlData = protocolFormXmlDataDao.findById(protocolFormXmlDataId);

		String xmldata = protocolXmlData.getXmlData();

		List<ValidationResponse> validationResponses = new ArrayList<ValidationResponse>();
		if(StringUtils.hasText(xmldata)){

			List<Rule> reportableNewInformationValidationRules = getValidationRuleContainer().getValidationRules("reportableNewInformationValidationRules");

			Assert.notNull(reportableNewInformationValidationRules);
			
			Set<String> valueKeys = getValidationRuleContainer().getCachedValueKeys("reportableNewInformationValidationRules");

			Assert.notNull(valueKeys);

			Map<String, List<String>> values = null;


			//setup values
			try {
				values = xmlProcessor.listElementStringValuesByPaths(valueKeys, xmldata);
			} catch (XPathExpressionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SAXException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			validationResponses = validationRuleHandler.validate(reportableNewInformationValidationRules, values);

		}
		
		if (!isReportableOrNot(protocolXmlData)){
			Constraint notReportableConstraint = new Constraint();
			Map<String, Object> notReportableAdditionalData = new HashMap<String, Object>();
			
			notReportableConstraint.setConstraintLevel(ConstraintLevel.ERROR);
			notReportableConstraint.setErrorMessage("This event/information does not meet the criteria for an Unanticipated Problem Involving Risk " +
					"to Subjects or Others (UPIRTSO) and does not require immediate reporting to the IRB." + "<br/>" + "Please submit the information in summary format at Continuing Review." + 
					"<br/>" + "A suggested summary format is available at:<a href=\"http://www.uams.edu/irb/Reporting%20Events%20and%20Deviations.asp\" target=\"_blank\">http://www.uams.edu/irb/Reporting%20Events%20and%20Deviations.asp</a>" + 
					"<br/>" + "The IRB recommends that events be compiled throughout the year." + 
					"<br/>" + "If you need to update documents now, please do so using a Modification.");
			
			notReportableAdditionalData.put("pagename", "Review");
			notReportableAdditionalData.put("pageref", "review");
			
			ValidationResponse budgetVP = new ValidationResponse(notReportableConstraint, notReportableAdditionalData);
			
			validationResponses.add(budgetVP);
		}
		
		return validationResponses;
	}

	@Autowired(required=true)
	public void setXmlProcessor(XmlProcessor xmlProcessor) {
		this.xmlProcessor = xmlProcessor;
	}

	public XmlProcessor getXmlProcessor() {
		return xmlProcessor;
	}

	@Autowired(required=true)
	public void setValidationRuleContainer(ValidationRuleContainer validationRuleContainer) {
		this.validationRuleContainer = validationRuleContainer;
	}

	public ValidationRuleContainer getValidationRuleContainer() {
		return validationRuleContainer;
	}

	@Autowired(required=true)
	public void setValidationRuleHandler(ValidationRuleHandler validationRuleHandler) {
		this.validationRuleHandler = validationRuleHandler;
	}

	public ValidationRuleHandler getValidationRuleHandler() {
		return validationRuleHandler;
	}

	@Autowired(required=true)
	public void setProtocolDocumentDao(ProtocolDocumentDao protocolDocumentDao) {
		this.protocolDocumentDao = protocolDocumentDao;
	}

	public ProtocolDocumentDao getProtocolDocumentDao() {
		return protocolDocumentDao;
	}

	@Autowired(required=true)
	public void setProtocolFormDao(ProtocolFormDao protocolFormDao) {
		this.protocolFormDao = protocolFormDao;
	}

	public ProtocolFormDao getProtocolFormDao() {
		return protocolFormDao;
	}
	
	@Autowired(required=true)
	public void setProtocolFormXmlDataDao(ProtocolFormXmlDataDao protocolFormXmlDataDao) {
		this.protocolFormXmlDataDao = protocolFormXmlDataDao;
	}

	public ProtocolFormXmlDataDao getProtocolFormXmlDataDao() {
		return protocolFormXmlDataDao;
	}
}
