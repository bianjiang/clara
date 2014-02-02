package edu.uams.clara.webapp.protocol.web.protocolform.hsrd.ajax;

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
public class HSRDValidationAjaxController {
	
	private final static Logger logger = LoggerFactory
	.getLogger(HSRDValidationAjaxController.class);

	private ProtocolFormDao protocolFormDao;

	private ProtocolFormXmlDataDao protocolFormXmlDataDao;

	private ProtocolDocumentDao protocolDocumentDao;

	private XmlProcessor xmlProcessor;

	private ValidationRuleHandler validationRuleHandler;

	private ValidationRuleContainer validationRuleContainer;

	@RequestMapping(value = "/ajax/protocols/{protocolId}/protocol-forms/{protocolFormId}/human-subject-research-determination/protocol-form-xml-datas/{protocolFormXmlDataId}/validate", method = RequestMethod.GET)
	public @ResponseBody
	List<ValidationResponse> validateHumanSubjectResearchDeterminationForm(
			@PathVariable("protocolFormXmlDataId") long protocolFormXmlDataId) {

		ProtocolFormXmlData protocolFormXmlData = protocolFormXmlDataDao.findById(protocolFormXmlDataId);

		String xmldata = protocolFormXmlData.getXmlData();

		List<ValidationResponse> validationResponses = new ArrayList<ValidationResponse>();
		if(StringUtils.hasText(xmldata)){



			List<Rule> protocolValidationRules = getValidationRuleContainer().getValidationRules("hsrdValidationRules");

			//Assert.notNull(protocolValidationRules);

			Set<String> valueKeys = getValidationRuleContainer().getCachedValueKeys("hsrdValidationRules");

			//Assert.notNull(valueKeys);

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

			/*... documents not finished **/
			//List<String> documentTypeValueList = protocolDocumentDao.listProtocolFormXmlDataDocumentCategories(protocolXmlData.getProtocolForm().getProtocol());
			//values.put("/protocol/documents/document/document-type", documentTypeValueList);
			
			validationResponses = validationRuleHandler.validate(protocolValidationRules, values);
			
//			List<String> valueLst = new ArrayList<String>();
//			try{
//				valueLst = xmlProcessor.listElementStringValuesByPath("/hsrd/is-hsrd-qualified", xmldata);
//			} catch(Exception e){
//				e.printStackTrace();
//			}
//			
//			String ishsrdQualified = "";
//			if (valueLst.size() > 0){
//				ishsrdQualified = valueLst.get(0);
//			}
//			
//			Constraint hsrdQualfiedConstraint = new Constraint();
//			Map<String, Object> hsrdQualifiedAdditionalData = new HashMap<String, Object>();
//			
//			if (ishsrdQualified.equals("y")){
//				hsrdQualfiedConstraint.setConstraintLevel(ConstraintLevel.WARNING);
//				hsrdQualfiedConstraint.setErrorMessage("Based upon the information you’ve provided, your study qualifies as human subject research. The IRB Director or Designee will verify this determination and provide you with a Determination Letter.");
//				
//				hsrdQualifiedAdditionalData.put("pagename", "Review");
//				hsrdQualifiedAdditionalData.put("pageref", "review");
//			} else {
//				hsrdQualfiedConstraint.setConstraintLevel(ConstraintLevel.WARNING);
//				hsrdQualfiedConstraint.setErrorMessage("Based upon the information you’ve provided, your study does NOT qualify as human subject research. The IRB Director or Designee will verify this determination and provide with a Determination Letter. ");
//				
//				hsrdQualifiedAdditionalData.put("pagename", "Review");
//				hsrdQualifiedAdditionalData.put("pageref", "review");
//			}
//			
//			ValidationResponse hsrdQualifiedVP = new ValidationResponse(hsrdQualfiedConstraint, hsrdQualifiedAdditionalData);
//
//			validationResponses.add(hsrdQualifiedVP);

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
