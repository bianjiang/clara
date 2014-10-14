package edu.uams.clara.webapp.protocol.web.protocolform.continuingreview.ajax;

import java.io.IOException;
import java.util.ArrayList;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.xml.sax.SAXException;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import edu.uams.clara.webapp.common.businesslogic.form.validator.ValidationResponse;
import edu.uams.clara.webapp.common.businesslogic.form.validator.ValidationRuleContainer;
import edu.uams.clara.webapp.common.businesslogic.form.validator.ValidationRuleHandler;
import edu.uams.clara.webapp.common.businesslogic.form.validator.rule.Rule;
import edu.uams.clara.webapp.common.domain.usercontext.enums.Committee;
import edu.uams.clara.webapp.common.service.form.FormService;
import edu.uams.clara.webapp.protocol.dao.ProtocolDocumentDao;
import edu.uams.clara.webapp.protocol.dao.protocolform.ProtocolFormDao;
import edu.uams.clara.webapp.protocol.dao.protocolform.ProtocolFormXmlDataDao;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolFormXmlData;
import edu.uams.clara.webapp.protocol.service.protocolform.ProtocolFormValidationService;
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
	
	private ProtocolFormValidationService protocolFormValidationService;
	
	private Set<String> ignoreValidationQuestionSet = Sets.newHashSet();{
		ignoreValidationQuestionSet.add("/continuing-review/study-report/any-adverse-events/y/adverse-events-accur-at-frequency");
		ignoreValidationQuestionSet.add("/continuing-review/study-report/any-adverse-events/y/adverse-events-accur-at-frequency/explain");
		ignoreValidationQuestionSet.add("/continuing-review/study-report/any-adverse-events/y/adverse-events-change-risk");
		ignoreValidationQuestionSet.add("/continuing-review/study-report/any-adverse-events/y/adverse-events-change-risk/explain");
		ignoreValidationQuestionSet.add("/continuing-review/study-report/any-adverse-events/y/sponsor-provide-information");
		ignoreValidationQuestionSet.add("/continuing-review/study-report/any-adverse-events/y/sponsor-provide-information/explain");
		ignoreValidationQuestionSet.add("/continuing-review/study-report/any-deviations/y/deviations-occur-in-pattern");
		ignoreValidationQuestionSet.add("/continuing-review/study-report/any-deviations/y/deviations-occur-in-pattern/explain");
		ignoreValidationQuestionSet.add("/continuing-review/study-report/any-deviations/y/deviations-negatively-impact");
		ignoreValidationQuestionSet.add("/continuing-review/study-report/any-deviations/y/deviations-negatively-impact/explain");
		ignoreValidationQuestionSet.add("/continuing-review/study-report/any-deviations/y/deviations-how-many");
	}

	@RequestMapping(value = "/ajax/protocols/{protocolId}/protocol-forms/{protocolFormId}/continuing-review/protocol-form-xml-datas/{protocolFormXmlDataId}/validate", method = RequestMethod.GET)
	public @ResponseBody
	List<ValidationResponse> validateProtocolContinuingReviewForm(
			@PathVariable("protocolFormXmlDataId") long protocolFormXmlDataId,
			@RequestParam("committee") Committee committee) {
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
			
			//ignore some validations temprarily when in review, might need to add it back later ...
			if (!committee.equals(Committee.PI)) {
				List<Rule> needToBeRemovedRuleList = Lists.newArrayList();
				
				for (int i=0; i < continuingReviewValidationRules.size(); i++) {
					if (ignoreValidationQuestionSet.contains(continuingReviewValidationRules.get(i).getValueKey())) {
						needToBeRemovedRuleList.add(continuingReviewValidationRules.get(i));
						//protocolValidationRules.remove(i);
					}
				}
				
				continuingReviewValidationRules.removeAll(needToBeRemovedRuleList);
				
			}

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
			
			validationResponses = protocolFormValidationService.getExtraValidationResponses(protocolXmlData, validationResponses);

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

	public ProtocolFormValidationService getProtocolFormValidationService() {
		return protocolFormValidationService;
	}
	
	@Autowired(required=true)
	public void setProtocolFormValidationService(
			ProtocolFormValidationService protocolFormValidationService) {
		this.protocolFormValidationService = protocolFormValidationService;
	}
}
