package edu.uams.clara.webapp.protocol.web.protocolform.continuingreview.ajax;

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
import edu.uams.clara.webapp.common.service.form.FormService;
import edu.uams.clara.webapp.protocol.dao.ProtocolDocumentDao;
import edu.uams.clara.webapp.protocol.dao.protocolform.ProtocolFormDao;
import edu.uams.clara.webapp.protocol.dao.protocolform.ProtocolFormXmlDataDao;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolFormXmlData;
import edu.uams.clara.webapp.xml.processor.XmlProcessor;

@Controller
public class ContinuingReviewValidationAjaxController {
	
	private final static Logger logger = LoggerFactory
	.getLogger(ContinuingReviewValidationAjaxController.class);

	private ProtocolFormDao protocolFormDao;

	private ProtocolFormXmlDataDao protocolFormXmlDataDao;

	private ProtocolDocumentDao protocolDocumentDao;

	private XmlProcessor xmlProcessor;

	private ValidationRuleHandler validationRuleHandler;

	private ValidationRuleContainer validationRuleContainer;
	
	private FormService formService;
	
	private boolean needCR(ProtocolFormXmlData protocolXmlData){
		boolean needCR = false;
		
		try{
			List<String> values = xmlProcessor.listElementStringValuesByPath("/continuing-review/need-cr", protocolXmlData.getXmlData());
			
			if (values.size() > 0){
				if (values.get(0).equals("y")){
					needCR = true;
				} else {
					needCR = false;
				}
			}
		} catch (Exception e){
			e.printStackTrace();
			needCR = false;
		}
		
		return needCR;
	}

	@RequestMapping(value = "/ajax/protocols/{protocolId}/protocol-forms/{protocolFormId}/continuing-review/protocol-form-xml-datas/{protocolFormXmlDataId}/validate", method = RequestMethod.GET)
	public @ResponseBody
	List<ValidationResponse> validateProtocolContinuingReviewForm(
			@PathVariable("protocolFormXmlDataId") long protocolFormXmlDataId) {
		ProtocolFormXmlData protocolXmlData = null;
		try{
		 protocolXmlData = protocolFormXmlDataDao.findById(protocolFormXmlDataId);
		}catch(Exception e){
			logger.debug("protocolFormXmlDataId: "+protocolFormXmlDataId);
			e.printStackTrace();
			throw e;
			
		}
		String xmldata = protocolXmlData.getXmlData();

		List<ValidationResponse> validationResponses = new ArrayList<ValidationResponse>();
		if(StringUtils.hasText(xmldata)){

			List<Rule> continuingReviewValidationRules = getValidationRuleContainer().getValidationRules("continuingReviewValidationRules");

			Assert.notNull(continuingReviewValidationRules);
			
			Set<String> valueKeys = getValidationRuleContainer().getCachedValueKeys("continuingReviewValidationRules");
			
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

			/*... documents not finished **/
			//List<String> documentTypeValueList = protocolDocumentDao.listProtocolFormXmlDataDocumentCategories(protocolXmlData.getProtocolForm().getProtocol());
			//values.put("/protocol/documents/document/document-type", documentTypeValueList);

			validationResponses = validationRuleHandler.validate(continuingReviewValidationRules, values);

		}
		
		if (!needCR(protocolXmlData)){
			Constraint notNeedCRConstraint = new Constraint();
			Map<String, Object> notNeedCRAdditionalData = new HashMap<String, Object>();
			
			notNeedCRConstraint.setConstraintLevel(ConstraintLevel.ERROR);
			notNeedCRConstraint.setErrorMessage("To close this study, please exit this Continuing Review and submit a Study Closure form instead.");
			
			notNeedCRAdditionalData.put("pagename", "Review");
			notNeedCRAdditionalData.put("pageref", "review");
			
			ValidationResponse needCRVP = new ValidationResponse(notNeedCRConstraint, notNeedCRAdditionalData);
			
			validationResponses.add(needCRVP);
		}
		
		try {
			List<String> noClaraUserList = formService.getNoClaraUsers(xmldata);
			
			if (noClaraUserList != null && !noClaraUserList.isEmpty()) {
				for (String noClaraUser : noClaraUserList) {
					Constraint noClaraUserConstraint = new Constraint();
					Map<String, Object> noClaraUserAdditionalData = new HashMap<String, Object>();
					
					noClaraUserConstraint.setConstraintLevel(ConstraintLevel.ERROR);
					noClaraUserConstraint.setErrorMessage("Staff "+ noClaraUser + " does not have CLARA account, please remove this staff and create account!");
					
					noClaraUserAdditionalData.put("pagename", "Staff");
					noClaraUserAdditionalData.put("pageref", "staff");
					
					ValidationResponse noClaraUserVP = new ValidationResponse(noClaraUserConstraint, noClaraUserAdditionalData);
					
					validationResponses.add(noClaraUserVP);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
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

	public FormService getFormService() {
		return formService;
	}
	
	@Autowired(required=true)
	public void setFormService(FormService formService) {
		this.formService = formService;
	}
}
