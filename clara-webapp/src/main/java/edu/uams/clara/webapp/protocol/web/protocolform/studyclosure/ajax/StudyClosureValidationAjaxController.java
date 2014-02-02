package edu.uams.clara.webapp.protocol.web.protocolform.studyclosure.ajax;

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
public class StudyClosureValidationAjaxController {
	
	private final static Logger logger = LoggerFactory
	.getLogger(StudyClosureValidationAjaxController.class);

	private ProtocolFormDao protocolFormDao;

	private ProtocolFormXmlDataDao protocolFormXmlDataDao;

	private ProtocolDocumentDao protocolDocumentDao;

	private XmlProcessor xmlProcessor;

	private ValidationRuleHandler validationRuleHandler;

	private ValidationRuleContainer validationRuleContainer;

	@RequestMapping(value = "/ajax/protocols/{protocolId}/protocol-forms/{protocolFormId}/study-closure/protocol-form-xml-datas/{protocolFormXmlDataId}/validate", method = RequestMethod.GET)
	public @ResponseBody
	List<ValidationResponse> validateProtocolReportableNewInformationForm(
			@PathVariable("protocolFormXmlDataId") long protocolFormXmlDataId) {

		ProtocolFormXmlData protocolXmlData = protocolFormXmlDataDao.findById(protocolFormXmlDataId);

		String xmldata = protocolXmlData.getXmlData();

		List<ValidationResponse> validationResponses = new ArrayList<ValidationResponse>();
		if(StringUtils.hasText(xmldata)){

			List<Rule> reportableNewInformationValidationRules = getValidationRuleContainer().getValidationRules("studyClosureValidationRules");

			Assert.notNull(reportableNewInformationValidationRules);
			
			Set<String> valueKeys = getValidationRuleContainer().getCachedValueKeys("studyClosureValidationRules");

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
			
			List<String> statusValues = new ArrayList<String>();
			try{
				statusValues = xmlProcessor.listElementStringValuesByPath("/study-closure/study-status", xmldata);
			} catch (Exception e){
				e.printStackTrace();
			}
			
			String status = "";
			if (statusValues.size() > 0){
				status = statusValues.get(0);
			}
			
			if (status.equals("other")){
				Constraint scQualfiedConstraint = new Constraint();
				Map<String, Object> scQualifiedAdditionalData = new HashMap<String, Object>();
				
				scQualfiedConstraint.setConstraintLevel(ConstraintLevel.ERROR);
				scQualfiedConstraint.setErrorMessage("This study does not meet the criteria for Study Closure at this time.  Please follow usual procedures for Continuing Review");
				
				scQualifiedAdditionalData.put("pagename", "Review");
				scQualifiedAdditionalData.put("pageref", "review");
				
				ValidationResponse scQualifiedVP = new ValidationResponse(scQualfiedConstraint, scQualifiedAdditionalData);

				validationResponses.add(scQualifiedVP);
			}
			
			

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
