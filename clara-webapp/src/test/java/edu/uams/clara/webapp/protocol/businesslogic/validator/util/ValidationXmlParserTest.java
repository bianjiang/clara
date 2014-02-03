package edu.uams.clara.webapp.protocol.businesslogic.validator.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import edu.uams.clara.webapp.common.businesslogic.form.validator.ValidationResponse;
import edu.uams.clara.webapp.common.businesslogic.form.validator.ValidationRuleContainer;
import edu.uams.clara.webapp.common.businesslogic.form.validator.ValidationRuleHandler;
import edu.uams.clara.webapp.common.businesslogic.form.validator.rule.Rule;
import edu.uams.clara.webapp.common.businesslogic.form.validator.util.ValidationXmlParser;
import edu.uams.clara.webapp.protocol.dao.protocolform.ProtocolFormXmlDataDao;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolFormXmlData;
import edu.uams.clara.webapp.protocol.web.protocolform.newsubmission.ajax.NewSubmissionValidationAjaxController;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"file:src/test/java/edu/uams/clara/webapp/protocol/businesslogic/validator/util/ValidationXmlParserTest-context.xml"})
public class ValidationXmlParserTest {

	private final static Logger logger = LoggerFactory
	.getLogger(ValidationXmlParserTest.class);

	private ValidationXmlParser validationXmlParser;

	private ValidationRuleHandler validationRuleHandler;

	private ProtocolFormXmlDataDao protocolFormXmlDataDao;

	private ValidationRuleContainer validationRuleContainer;

	private NewSubmissionValidationAjaxController newSubmissionValidationAjaxController;


	@Test
	public void testValidateXmlParser(){

		//List<Rule> protocolValidationRules = getValidationRuleContainer().getValidationRules("protocolValidationRules");
		//Set<String> valueKeys = getValidationRuleContainer().getCachedValueKeys("protocolValidationRules");

		//ProtocolFormXmlData protocolXmlData = protocolFormXmlDataDao.findById(467l);
		//Class<?> stringClass = Class.forName("java.lang.String");
		//List<ValidationResponse> validationResponses = newSubmissionValidationAjaxController.validateProtocolNewSubmissionForm(467l);
	}

	public ValidationXmlParser getValidationXmlParser() {
		return validationXmlParser;
	}

	@Autowired(required=true)
	public void setValidationXmlParser(ValidationXmlParser validationXmlParser) {
		this.validationXmlParser = validationXmlParser;
	}

	public ValidationRuleHandler getValidationRuleHandler() {
		return validationRuleHandler;
	}

	@Autowired(required=true)
	public void setValidationRuleHandler(ValidationRuleHandler validationRuleHandler) {
		this.validationRuleHandler = validationRuleHandler;
	}

	public ProtocolFormXmlDataDao getProtocolFormXmlDataDao() {
		return protocolFormXmlDataDao;
	}

	@Autowired(required=true)
	public void setProtocolFormXmlDataDao(ProtocolFormXmlDataDao protocolFormXmlDataDao) {
		this.protocolFormXmlDataDao = protocolFormXmlDataDao;
	}

	public ValidationRuleContainer getValidationRuleContainer() {
		return validationRuleContainer;
	}

	@Autowired(required=true)
	public void setValidationRuleContainer(ValidationRuleContainer validationRuleContainer) {
		this.validationRuleContainer = validationRuleContainer;
	}

	public NewSubmissionValidationAjaxController getNewSubmissionValidationAjaxController() {
		return newSubmissionValidationAjaxController;
	}

	@Autowired(required=true)
	public void setNewSubmissionValidationAjaxController(
			NewSubmissionValidationAjaxController newSubmissionValidationAjaxController) {
		this.newSubmissionValidationAjaxController = newSubmissionValidationAjaxController;
	}
}
