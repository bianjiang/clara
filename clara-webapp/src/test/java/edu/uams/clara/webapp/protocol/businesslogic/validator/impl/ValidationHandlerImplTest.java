package edu.uams.clara.webapp.protocol.businesslogic.validator.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.xml.xpath.XPathExpressionException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.xml.sax.SAXException;

import edu.uams.clara.webapp.common.businesslogic.form.validator.ValidationResponse;
import edu.uams.clara.webapp.common.businesslogic.form.validator.ValidationRuleHandler;
import edu.uams.clara.webapp.common.businesslogic.form.validator.constraint.Constraint;
import edu.uams.clara.webapp.common.businesslogic.form.validator.constraint.enums.ConstraintType;
import edu.uams.clara.webapp.common.businesslogic.form.validator.rule.Rule;
import edu.uams.clara.webapp.common.businesslogic.form.validator.rule.ValidationRule;
import edu.uams.clara.webapp.protocol.dao.ProtocolDao;
import edu.uams.clara.webapp.protocol.dao.protocolform.ProtocolFormDao;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolForm;
import edu.uams.clara.webapp.xml.processor.XmlProcessor;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"file:src/test/java/edu/uams/clara/webapp/protocol/businesslogic/validator/impl/ValidationHandlerImplTest-context.xml"})
public class ValidationHandlerImplTest {
//
//	private final static Logger logger = LoggerFactory
//	.getLogger(ValidationHandlerImplTest.class);
//
//	private ValidationRuleHandler validationRuleHandler;
//
//	private XmlProcessor xmlProcessor;
//
//	@Test
//	public void testValidate(){
//		logger.debug("testValidate");
//		Constraint requiredConstraint = new Constraint();
//		requiredConstraint.setConstraintType(ConstraintType.REQUIRED);
//
//		//setup values
//		Map<String, List<String>> values = new HashMap<String, List<String>>(0);
//		List<String> titleValueList = new ArrayList<String>(0);
//		titleValueList.add("");
//		values.put("/protocol/title", titleValueList);
//
//		/*
//		List<String> localAccuralValueList = new ArrayList<String>(0);
//		localAccuralValueList.add("0");
//		values.put("/protocol/local-accural", localAccuralValueList);
//		*/
//		//setup validationRules list
//
//		ValidationRule titleRequiredValidationRule = new ValidationRule();
//		titleRequiredValidationRule.addConstraint(requiredConstraint);
//		titleRequiredValidationRule.setValueKey("/protocol/title");
//		titleRequiredValidationRule.setValueType(String.class);
//		titleRequiredValidationRule.addAdditionalData("description", "Title");
//		titleRequiredValidationRule.addAdditionalData("page-ref", "basic-details");
//
//		ValidationRule localAccuralRequiredValidationRule = new ValidationRule();
//		localAccuralRequiredValidationRule.addConstraint(requiredConstraint);
//		localAccuralRequiredValidationRule.setValueKey("/protocol/local-accural");
//		localAccuralRequiredValidationRule.setValueType(Integer.class);
//		localAccuralRequiredValidationRule.addAdditionalData("description", "Local Accural");
//		localAccuralRequiredValidationRule.addAdditionalData("page-ref", "subjects");
//
//
//		//setup prerequisiteRules list
//		Rule requireTotalAccural = new Rule();
//		requireTotalAccural.addConstraint(requiredConstraint);
//		requireTotalAccural.setValueKey("/protocol/total-accural");
//		requireTotalAccural.setValueType(Integer.class);
//
//		List<Rule> localAccuralRequiredPrerequisiteRules = new ArrayList<Rule>(0);
//		localAccuralRequiredPrerequisiteRules.add(requireTotalAccural);
//
//		localAccuralRequiredValidationRule.setPrerequisiteRules(localAccuralRequiredPrerequisiteRules);
//
//		List<ValidationRule> validationRules = new ArrayList<ValidationRule>(0);
//		validationRules.add(titleRequiredValidationRule);
//		validationRules.add(localAccuralRequiredValidationRule);
//
//
//
//
//
//		List<ValidationResponse> validationResponses = validationRuleHandler.validate(validationRules, values);
//
//		for(ValidationResponse r:validationResponses){
//			logger.debug("Response: " + r.getConstraint().getConstraintType());
//		}
//
//	}
//
//	/**
//	 * in this test the values are produced by xmlProcessor, which get the values from the xml,
//	 * the validator doesn't care where the values are coming from as long as its a Map<String, List<String>>,
//	 * ideally we should be able to do Map<String, Object> to make the validator more generic. However, this will require a extra Map passed into the xmlProcessor's listElementsValue to make it generate a type safe Map
//	 * if we do that, then the validator doesn't have to know the type of the value its getting, which we save the valueType property on each Rule
//	 * but think it from a different perspective, local-accural is a number field, so naturally we will want a number, so in a type-safe listElementsValue function, we will want to convert it into an Integer.class
//	 * however, if the user enters "abc" instead of "123", an exception will be thrown by the type-safe listElementsValue function, which is not a ideal result
//	 * And in common sense, we will want the validator to give a error message if the user enters characters instead of a number for local-accural field.
//	 * @throws XPathExpressionException
//	 * @throws SAXException
//	 * @throws IOException
//	 */
//	@Test
//	public void integrationTestValidator() throws XPathExpressionException,
//			SAXException, IOException {
//		logger.debug("integrationTestValidator");
//		String originalXml = "<protocol id=\"1\"><title></title><local-accural>0</local-accural><phases><phase>I</phase><phase>II</phase></phases><drugs><drug id=\"1\"><name>Whateverdrug</name></drug><drug id=\"2\"><name>iamno2</name></drug></drugs></protocol>";
//		Set<String> listPaths = new HashSet<String>(0);
//		listPaths.add("/protocol/drugs/drug/name");
//		listPaths.add("/protocol/phases/phase");
//		listPaths.add("/protocol/title");
//		listPaths.add("/protocol/local-accural");
//
//		Map<String, List<String>> values = xmlProcessor.listElementStringValuesByPaths(listPaths, originalXml);
//
//		for(Entry<String, List<String>> entry:values.entrySet()){
//			logger.debug(entry.getKey() + ": [");
//			for(String v:entry.getValue()){
//				logger.debug("    {" + v + "}");
//			}
//			logger.debug("]");
//		}
//
//		Constraint requiredConstraint = new Constraint();
//		requiredConstraint.setConstraintType(ConstraintType.REQUIRED);
//
//
//		ValidationRule titleRequiredValidationRule = new ValidationRule();
//		titleRequiredValidationRule.addConstraint(requiredConstraint);
//		titleRequiredValidationRule.setValueKey("/protocol/title");
//		titleRequiredValidationRule.addAdditionalData("description", "Title");
//		titleRequiredValidationRule.addAdditionalData("page-ref", "basic-details");
//		titleRequiredValidationRule.setValueType(String.class);
//
//
//		ValidationRule localAccuralRequiredValidationRule = new ValidationRule();
//		localAccuralRequiredValidationRule.addConstraint(requiredConstraint);
//		localAccuralRequiredValidationRule.setValueKey("/protocol/local-accural");
//		localAccuralRequiredValidationRule.setValueType(Integer.class);
//		localAccuralRequiredValidationRule.addAdditionalData("description", "Local Accural");
//		localAccuralRequiredValidationRule.addAdditionalData("page-ref", "subjects");
//
//		//setup validationRules list
//		List<ValidationRule> validationRules = new ArrayList<ValidationRule>(0);
//		validationRules.add(titleRequiredValidationRule);
//		validationRules.add(localAccuralRequiredValidationRule);
//
//		List<ValidationResponse> validationResponses = validationRuleHandler.validate(validationRules, values);
//
//		for(ValidationResponse r:validationResponses){
//			logger.debug("Response: " + r.getConstraint().getConstraintType());
//		}
//
//	}
//
//	/**
//	 * the old validate function in ValidationHandler makes assumptions that the values have the correct class type when passed in,
//	 * which requires a protocolXmlDataTypes hashmap passed into XmlProcessor to get type safe values...
//	 * @deprecated
//	 * @throws XPathExpressionException
//	 * @throws SAXException
//	 * @throws IOException
//	 */
//	//@Test
//	public void integrationTestValidatorWithXmlDataTypes() throws XPathExpressionException,
//			SAXException, IOException {
//		String originalXml = "<protocol id=\"1\"><title></title><phases><phase>I</phase><phase>II</phase></phases><drugs><drug id=\"1\"><name>Whateverdrug</name></drug><drug id=\"2\"><name>iamno2</name></drug></drugs></protocol>";
//		Set<String> listPaths = new HashSet<String>(0);
//		listPaths.add("/protocol/drugs/drug/name");
//		listPaths.add("/protocol/phases/phase");
//		listPaths.add("/protocol/title");
//
//		/**
//		 * need to find a better way to define the data type of each xml element...
//		 * it's important to do this, because, during validation, the validator needs to know the type of the value, for example, it's invalid to put in "abc" as a number...
//		 * if a path is not defined in this map, then by default its a String...
//		 */
//		Map<String, Class<?>> protocolXmlDataTypes = new HashMap<String, Class<?>>(0);
//		protocolXmlDataTypes.put("/protocol/drugs/drug/name", List.class);
//		protocolXmlDataTypes.put("/protocol/phases/phase", List.class);
//		protocolXmlDataTypes.put("/protocol/title", String.class);
//
//		Map<String, Object> values = xmlProcessor.listElementValuesByPaths(listPaths, protocolXmlDataTypes, originalXml);
//
//		Constraint requiredConstraint = new Constraint();
//		requiredConstraint.setConstraintType(ConstraintType.REQUIRED);
//
//
//		ValidationRule validationRule = new ValidationRule();
//		validationRule.addConstraint(requiredConstraint);
//		validationRule.setValueKey("/protocol/title");
//		validationRule.addAdditionalData("description", "Title");
//		validationRule.addAdditionalData("page-ref", "basic-details");
//
//		//setup validationRules list
//		List<ValidationRule> validationRules = new ArrayList<ValidationRule>(0);
//		validationRules.add(validationRule);
//
//		/*
//		List<ValidationResponse> validationResponses = validationRuleHandler.validate(validationRules, values);
//
//		for(ValidationResponse r:validationResponses){
//			logger.debug("Response: " + r.getMessage());
//		}
//		*/
//
//	}
//
//
//
//	@Test
//	public void testDocumentRequirements(){
//		Constraint requiredConstraint = new Constraint();
//		requiredConstraint.setConstraintType(ConstraintType.CONTAINS);
//		requiredConstraint.addParam(ConstraintType.Contains.ParamKeys.VALUE.toString(), "Protocol");
//
//		ValidationRule protocolDocumentRequiredValidationRule = new ValidationRule();
//		protocolDocumentRequiredValidationRule.addConstraint(requiredConstraint);
//		protocolDocumentRequiredValidationRule.setValueKey("/protocol/documents/document/document-type");
//		//protocolDocumentRequiredValidationRule.setValueType(List.class);
//		protocolDocumentRequiredValidationRule.addAdditionalData("description", "Document");
//		protocolDocumentRequiredValidationRule.addAdditionalData("page-ref", "documents");
//
//
//		List<ValidationRule> validationRules = new ArrayList<ValidationRule>(0);
//		validationRules.add(protocolDocumentRequiredValidationRule);
//
//		//setup values
//		Map<String, List<String>> values = new HashMap<String, List<String>>(0);
//		List<String> documentTypeValueList = new ArrayList<String>(0);
//		documentTypeValueList.add("WhateverAsLongAsItsNotProtocol");
//		documentTypeValueList.add("Protocol");
//		values.put("/protocol/documents/document/document-type", documentTypeValueList);
//
//		List<ValidationResponse> validationResponses = validationRuleHandler.validate(validationRules, values);
//
//		for(ValidationResponse r:validationResponses){
//			logger.debug("Response: " + r.getConstraint());
//		}
//	}
//
//	private ProtocolDao protocolDao;
//
//	private ProtocolFormDao protocolFormDao;
//
//
//	@Test
//	public void integrationTestDocumentRequirements(){
//		Constraint requiredConstraint = new Constraint();
//		requiredConstraint.setConstraintType(ConstraintType.CONTAINS);
//		requiredConstraint.addParam(ConstraintType.Contains.ParamKeys.VALUE.toString(), "Protocol");
//
//		ValidationRule protocolDocumentRequiredValidationRule = new ValidationRule();
//		protocolDocumentRequiredValidationRule.addConstraint(requiredConstraint);
//		protocolDocumentRequiredValidationRule.setValueKey("/protocol/documents/document/document-type");
//		//protocolDocumentRequiredValidationRule.setValueType(List.class);
//		protocolDocumentRequiredValidationRule.addAdditionalData("description", "Document");
//		protocolDocumentRequiredValidationRule.addAdditionalData("page-ref", "documents");
//
//
//		List<ValidationRule> validationRules = new ArrayList<ValidationRule>(0);
//		validationRules.add(protocolDocumentRequiredValidationRule);
//
//		//setup values
//		Map<String, List<String>> values = new HashMap<String, List<String>>(0);
//
//		ProtocolForm protocolForm = protocolFormDao.findById(1);
//		logger.debug("protocolForm: " + protocolForm.getProtocol().getId());
//		
//		//List<String> documentTypeValueList = protocolFileDao.listProtocolFileCategories(protocol);
//		values.put("/protocol/documents/document/document-type", null);
//
//
//		List<ValidationResponse> validationResponses = validationRuleHandler.validate(validationRules, values);
//
//		for(ValidationResponse r:validationResponses){
//			logger.debug("Response: " + r.getConstraint());
//		}
//	}
//	*/
//
//	@Autowired(required = true)
//	public void setXmlProcessor(XmlProcessor xmlProcessor) {
//		this.xmlProcessor = xmlProcessor;
//	}
//
//	public XmlProcessor getXmlProcessor() {
//		return xmlProcessor;
//	}
//
//	@Autowired(required=true)
//	public void setValidationRuleHandler(ValidationRuleHandler validationRuleHandler) {
//		this.validationRuleHandler = validationRuleHandler;
//	}
//
//	public ValidationRuleHandler getValidationRuleHandler() {
//		return validationRuleHandler;
//	}
//
//	@Autowired(required=true)
//	public void setProtocolDao(ProtocolDao protocolDao) {
//		this.protocolDao = protocolDao;
//	}
//
//	public ProtocolDao getProtocolDao() {
//		return protocolDao;
//	}
//
//	@Autowired(required=true)
//	public void setProtocolFormDao(ProtocolFormDao protocolFormDao) {
//		this.protocolFormDao = protocolFormDao;
//	}
//
//	public ProtocolFormDao getProtocolFormDao() {
//		return protocolFormDao;
//	}
}
