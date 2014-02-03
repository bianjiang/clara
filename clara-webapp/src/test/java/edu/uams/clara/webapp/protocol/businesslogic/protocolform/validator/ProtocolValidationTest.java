package edu.uams.clara.webapp.protocol.businesslogic.protocolform.validator;

import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import edu.uams.clara.webapp.common.businesslogic.form.validator.ValidationRuleContainer;
import edu.uams.clara.webapp.common.businesslogic.form.validator.ValidationRuleHandler;
import edu.uams.clara.webapp.protocol.dao.ProtocolDao;
import edu.uams.clara.webapp.protocol.dao.ProtocolDocumentDao;
import edu.uams.clara.webapp.protocol.dao.protocolform.ProtocolFormDao;
import edu.uams.clara.webapp.xml.processor.XmlProcessor;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"file:src/test/java/edu/uams/clara/webapp/protocol/businesslogic/protocolform/validator/ProtocolValidationTest-context.xml"})
public class ProtocolValidationTest {

	private final static Logger logger = LoggerFactory
	.getLogger(ProtocolValidationTest.class);

	private ValidationRuleContainer validaitonRuleContainer;

	private ValidationRuleHandler validationRuleHandler;
	private XmlProcessor xmlProcessor;

	private ProtocolDao protocolDao;

	private ProtocolDocumentDao protocolDocumentDao;

	private ProtocolFormDao protocolFormDao;

	//@Test
	/*
	public void testProtocolValidation(){


		List<ValidationRule> protocolValidationRules = validaitonRuleContainer.getValidationRules("protocolValidationRules");

		for(ValidationRule vr:protocolValidationRules){
			logger.trace("vr: " + vr.getValueKey());
		}
		//setup values
		Map<String, List<String>> values = new HashMap<String, List<String>>(0);
		values.put("/protocol/title", createValueList(""));
		values.put("/protocol/subjects/localaccrual", createValueList("100"));
		values.put("/protocol/subjects/totalaccrual", createValueList("100"));


		List<ValidationResponse> validationResponses = validationRuleHandler.validate(protocolValidationRules, values);

		for(ValidationResponse r:validationResponses){
			logger.debug("Response: " + r.getConstraint().getErrorMessage());
		}

	}

	@Test
	public void integrationTestProtocolValidaiton() throws XPathExpressionException, SAXException, IOException{
		List<ValidationRule> protocolValidationRules = validaitonRuleContainer.getValidationRules("protocolValidationRules");

		ProtocolForm newsubmissionForm = protocolFormDao.findById(1);

		logger.debug("protocol: " + newsubmissionForm.getProtocol().getId());

		for(Entry<ProtocolFormXmlDataType, ProtocolFormXmlData> entry:newsubmissionForm.getTypedProtocolFormXmlDatas().entrySet()){
			logger.debug(entry.getKey().toString() + ": " + entry.getValue().getXmlData());
		}

		ProtocolFormXmlData protocolXmlData = newsubmissionForm.getTypedProtocolFormXmlDatas().get(ProtocolFormXmlDataType.PROTOCOL);

		String xmlData = protocolXmlData.getXmlData();

		Assert.hasText(xmlData);

		Set<String> valueKeys = validaitonRuleContainer.getCachedValueKeys("protocolValidationRules");

		for(String v:valueKeys){
			logger.debug(v);
		}
		//setup values
		Map<String, List<String>> values = xmlProcessor.listElementStringValuesByPaths(valueKeys, xmlData);

		List<String> documentTypeValueList = protocolDocumentDao.listProtocolFormXmlDataDocumentCategories(protocolXmlData.getProtocolForm().getProtocol());
		values.put("/protocol/documents/document/document-type", documentTypeValueList);

		List<ValidationResponse> validationResponses = validationRuleHandler.validate(protocolValidationRules, values);

		for(ValidationResponse r:validationResponses){
			logger.debug("Response: " + r.getConstraint().getErrorMessage());
		}
	}


	@SuppressWarnings("unchecked")
	private List<String> createValueList(String... values){
		return Arrays.asList(values);
	}

	@Autowired(required=true)
	public void setValidationRuleHandler(ValidationRuleHandler validationRuleHandler) {
		this.validationRuleHandler = validationRuleHandler;
	}

	public ValidationRuleHandler getValidationRuleHandler() {
		return validationRuleHandler;
	}

	@Autowired(required=true)
	public void setXmlProcessor(XmlProcessor xmlProcessor) {
		this.xmlProcessor = xmlProcessor;
	}

	public XmlProcessor getXmlProcessor() {
		return xmlProcessor;
	}

	@Autowired(required=true)
	public void setProtocolDao(ProtocolDao protocolDao) {
		this.protocolDao = protocolDao;
	}

	public ProtocolDao getProtocolDao() {
		return protocolDao;
	}

	@Autowired(required=true)
	public void setValidaitonRuleContainer(ValidationRuleContainer validaitonRuleContainer) {
		this.validaitonRuleContainer = validaitonRuleContainer;
	}

	public ValidationRuleContainer getValidaitonRuleContainer() {
		return validaitonRuleContainer;
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
	*/
}
