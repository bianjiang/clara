package edu.uams.clara.webapp.protocol.web.protocolform.modification.ajax;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
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
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;








import edu.uams.clara.webapp.common.businesslogic.form.validator.ValidationResponse;
import edu.uams.clara.webapp.common.businesslogic.form.validator.ValidationRuleContainer;
import edu.uams.clara.webapp.common.businesslogic.form.validator.ValidationRuleHandler;
import edu.uams.clara.webapp.common.businesslogic.form.validator.constraint.Constraint;
import edu.uams.clara.webapp.common.businesslogic.form.validator.constraint.enums.ConstraintLevel;
import edu.uams.clara.webapp.common.businesslogic.form.validator.rule.Rule;
import edu.uams.clara.webapp.common.domain.usercontext.enums.Committee;
import edu.uams.clara.webapp.common.service.form.FormService;
import edu.uams.clara.webapp.protocol.dao.ProtocolDocumentDao;
import edu.uams.clara.webapp.protocol.dao.businesslogicobject.ProtocolFormCommitteeStatusDao;
import edu.uams.clara.webapp.protocol.dao.protocolform.ProtocolFormDao;
import edu.uams.clara.webapp.protocol.dao.protocolform.ProtocolFormXmlDataDao;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.ProtocolFormCommitteeStatus;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.enums.ProtocolFormCommitteeStatusEnum;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolForm;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolFormXmlData;
import edu.uams.clara.webapp.protocol.service.protocolform.ProtocolFormReviewLogicServiceContainer;
import edu.uams.clara.webapp.xml.processor.XmlProcessor;

@Controller
public class ModificationValidationAjaxController {
	
	private final static Logger logger = LoggerFactory
	.getLogger(ModificationValidationAjaxController.class);

	private ProtocolFormDao protocolFormDao;

	private ProtocolFormXmlDataDao protocolFormXmlDataDao;

	private ProtocolDocumentDao protocolDocumentDao;

	private XmlProcessor xmlProcessor;

	private ValidationRuleHandler validationRuleHandler;

	private ValidationRuleContainer validationRuleContainer;
	
	private ProtocolFormCommitteeStatusDao protocolFormCommitteeStatusDao;
	
	private ProtocolFormReviewLogicServiceContainer protocolFormReviewLogicServiceContainer;
	
	private FormService formService;
	
	private List<String> xmlDataXPathList = Lists.newArrayList();{
		xmlDataXPathList.add("/protocol/site-responsible");
		xmlDataXPathList.add("/protocol/study-type/investigator-initiated/investigator-description");
		xmlDataXPathList.add("/protocol/budget-created");
		xmlDataXPathList.add("/protocol/budget/potentially-billed");
		xmlDataXPathList.add("/protocol/responsible-department");
		xmlDataXPathList.add("/protocol/study-nature");
		xmlDataXPathList.add("/protocol/study-nature/hud-use/where");
	}
	
	private ValidationResponse pharmacyReviewValidation(long protocolFormId){
		Constraint pharmacyValidationConstraint = new Constraint();
		Map<String, Object> pharmacyValidationAdditionalData = new HashMap<String, Object>();
		
		ValidationResponse pharmacyValidationVP = null;
		
		try{
			ProtocolForm protocolForm = protocolFormDao.findById(protocolFormId);
			ProtocolFormCommitteeStatus pfcs = protocolFormCommitteeStatusDao.getLatestByCommitteeAndProtocolFormId(Committee.PHARMACY_REVIEW, protocolForm.getId());
			
			if (pfcs != null){
				if (pfcs.getProtocolFormCommitteeStatus().equals(ProtocolFormCommitteeStatusEnum.IN_REVIEW_REQUESTED) || pfcs.getProtocolFormCommitteeStatus().equals(ProtocolFormCommitteeStatusEnum.IN_WAIVER_REQUESTED)){
					pharmacyValidationConstraint.setConstraintLevel(ConstraintLevel.ERROR);
					pharmacyValidationConstraint.setErrorMessage("Need to wait for Pharmacy to complete Review!");
					
					pharmacyValidationAdditionalData.put("pagename", "Review");
					pharmacyValidationAdditionalData.put("pageref", "review");
				}
			}
	
			logger.debug("add: " + pharmacyValidationAdditionalData);
			if (pharmacyValidationConstraint != null && (pharmacyValidationAdditionalData != null && !pharmacyValidationAdditionalData.isEmpty())){
				pharmacyValidationVP = new ValidationResponse(pharmacyValidationConstraint, pharmacyValidationAdditionalData);
			}
			
		} catch (Exception e){
			e.printStackTrace();
		}

		return pharmacyValidationVP;
	}
	
	private ValidationResponse fundingSourceValidation(String xmlDataString, Map<String, List<String>> values){
		Constraint fsValidationConstraint = new Constraint();
		Map<String, Object> fsValidationAdditionalData = new HashMap<String, Object>();
		
		ValidationResponse fsValidationVP = null;
		
		try{
			String siteResponsible = formService.getSafeStringValueByKey(values, "/protocol/site-responsible", "");
			
			String instestigatorDesc = formService.getSafeStringValueByKey(values, "/protocol/study-type/investigator-initiated/investigator-description", "");
			
			String budgetCreated = formService.getSafeStringValueByKey(values, "/protocol/budget-created", "");
			
			String haveBudget = formService.getSafeStringValueByKey(values, "/protocol/budget/potentially-billed", "");
			
			String studyNature = formService.getSafeStringValueByKey(values, "/protocol/study-nature", "");
			
			String hudStudyLocation = formService.getSafeStringValueByKey(values, "/protocol/study-nature/hud-use/where", "");

			if (!siteResponsible.equals("ach-achri")){
				if (studyNature.equals("hud-use") && hudStudyLocation.equals("ach/achri")) {
					return fsValidationVP;
				}
				
				Document pfxdDoc = xmlProcessor.loadXmlStringToDOM(xmlDataString);
				
				XPath xpath = xmlProcessor.getXPathInstance();
				
				NodeList fundingLst = (NodeList) xpath.evaluate( "/protocol/funding/funding-source", pfxdDoc, XPathConstants.NODESET);
				
				if (fundingLst == null || fundingLst.getLength() < 1){
					fsValidationConstraint.setConstraintLevel(ConstraintLevel.ERROR);
					fsValidationConstraint.setErrorMessage("Funding Source is required!");
					
					fsValidationAdditionalData.put("pagename", "Funding Sources");
					fsValidationAdditionalData.put("pageref", "funding-sources");
				} else {
					NodeList partialFundingLst = (NodeList) xpath.evaluate( "/protocol/funding/funding-source[@amount=\"Partial\"]", pfxdDoc, XPathConstants.NODESET);
					
					Node fullFundingNd = (Node) xpath.evaluate( "/protocol/funding/funding-source[@amount=\"Full\"]", pfxdDoc, XPathConstants.NODE);
					
					Node noFundingNd = (Node) xpath.evaluate( "/protocol/funding/funding-source[@type=\"None\"]", pfxdDoc, XPathConstants.NODE);
					
					if (fullFundingNd == null && noFundingNd == null){
						if (partialFundingLst != null && partialFundingLst.getLength() < 2){
							fsValidationConstraint.setConstraintLevel(ConstraintLevel.ERROR);
							fsValidationConstraint.setErrorMessage("At least 2 partial funding sources are required!");
							
							fsValidationAdditionalData.put("pagename", "Funding Sources");
							fsValidationAdditionalData.put("pageref", "funding-sources");
						}
					} else if (noFundingNd != null){
						Element noFundingEl = (Element) noFundingNd; 
						
						if (instestigatorDesc.equals("student-fellow-resident-post-doc") && budgetCreated.equals("y") && haveBudget.equals("y") && (noFundingEl.getAttribute("entityname").isEmpty() || noFundingEl.getAttribute("department").isEmpty() || noFundingEl.getAttribute("name").isEmpty())){
							fsValidationConstraint.setConstraintLevel(ConstraintLevel.ERROR);
							fsValidationConstraint.setErrorMessage("Since this study has budget, funding source is required!");
							
							fsValidationAdditionalData.put("pagename", "Funding Sources");
							fsValidationAdditionalData.put("pageref", "funding-sources");
						}
					}
				}
			}
			
			if (fsValidationConstraint != null && (fsValidationAdditionalData != null && !fsValidationAdditionalData.isEmpty())){
				fsValidationVP = new ValidationResponse(fsValidationConstraint, fsValidationAdditionalData);
			}
		} catch (Exception e){
			e.printStackTrace();
		}
		
		return fsValidationVP;
	}
	
	private ValidationResponse departmentValidation(String xmlDataString){
		Constraint departmentValidationConstraint = new Constraint();
		Map<String, Object> departmentValidationAdditionalData = new HashMap<String, Object>();
		
		ValidationResponse departmentValidationVP = null;
		
		try{
			String collegeDesc = xmlProcessor.getAttributeValueByPathAndAttributeName("/protocol/responsible-department", xmlDataString, "collegedesc");
			
			if (collegeDesc != null && !collegeDesc.isEmpty()){
				String deptDesc = xmlProcessor.getAttributeValueByPathAndAttributeName("/protocol/responsible-department", xmlDataString, "deptdesc");
				
				if (!collegeDesc.equals("Not applicable")) {
					if (deptDesc == null || deptDesc.isEmpty()){
						departmentValidationConstraint.setConstraintLevel(ConstraintLevel.ERROR);
						departmentValidationConstraint.setErrorMessage("Department is required if you select college!");
						
						departmentValidationAdditionalData.put("pagename", "First Page");
						departmentValidationAdditionalData.put("pageref", "first-page");
					}
				}
				
			} else {
				departmentValidationConstraint.setConstraintLevel(ConstraintLevel.ERROR);
				departmentValidationConstraint.setErrorMessage("College/Department informtion is required!");
				
				departmentValidationAdditionalData.put("pagename", "First Page");
				departmentValidationAdditionalData.put("pageref", "first-page");
			}
			
			if (departmentValidationConstraint != null && (departmentValidationAdditionalData != null && !departmentValidationAdditionalData.isEmpty())){
				departmentValidationVP = new ValidationResponse(departmentValidationConstraint, departmentValidationAdditionalData);
			}
			
		} catch (Exception e){
			e.printStackTrace();
		}
		
		return departmentValidationVP;
	}
	
	private Set<String> ignoreValidationQuestionSet = Sets.newHashSet();{
		ignoreValidationQuestionSet.add("/protocol/budget/involves/uams-inpatient-units");
		ignoreValidationQuestionSet.add("/protocol/budget/involves/uams-clinics");
		ignoreValidationQuestionSet.add("/protocol/budget/involves/uams-ss-ou");
		//firstBudgetQuestionSubSet.add("/protocol/budget/involves/uams-infusion");
		ignoreValidationQuestionSet.add("/protocol/budget/involves/uams-clinicallab");
		ignoreValidationQuestionSet.add("/protocol/budget/involves/uams-radiology");
		ignoreValidationQuestionSet.add("/protocol/budget/involves/uams-pharmacy");
		ignoreValidationQuestionSet.add("/protocol/budget/involves/uams-other");
		ignoreValidationQuestionSet.add("/protocol/budget/involves/uams-supplies");
		ignoreValidationQuestionSet.add("/protocol/budget/involves/fgp-fees");
		ignoreValidationQuestionSet.add("/protocol/budget/involves/industry-support");
		ignoreValidationQuestionSet.add("/protocol/epic/involve-chemotherapy");
		ignoreValidationQuestionSet.add("/protocol/study-type/investigator-initiate/support-type-describe");
	}
	
	@RequestMapping(value = "/ajax/protocols/{protocolId}/protocol-forms/{protocolFormId}/modification/protocol-form-xml-datas/{protocolFormXmlDataId}/validate", method = RequestMethod.GET)
	public @ResponseBody
	List<ValidationResponse> validateProtocolModificationForm(@PathVariable("protocolFormId") long protocolFormId,
			@PathVariable("protocolFormXmlDataId") long protocolFormXmlDataId,
			@RequestParam("committee") Committee committee) {

		ProtocolFormXmlData protocolXmlData = protocolFormXmlDataDao.findById(protocolFormXmlDataId);

		String xmldata = protocolXmlData.getXmlData();

		List<ValidationResponse> validationResponses = new ArrayList<ValidationResponse>();
		
		if (!committee.equals(Committee.PHARMACY_REVIEW)){
			if(StringUtils.hasText(xmldata)){

				List<Rule> protocolValidationRules = getValidationRuleContainer().getValidationRules("modificationValidationRules");
				
				//ignore some validations temprarily when in review, might need to add it back later ...
				if (!committee.equals(Committee.PI)) {
					List<Rule> needToBeRemovedRuleList = Lists.newArrayList();
					
					for (int i=0; i < protocolValidationRules.size(); i++) {
						if (ignoreValidationQuestionSet.contains(protocolValidationRules.get(i).getValueKey())) {
							needToBeRemovedRuleList.add(protocolValidationRules.get(i));
							//protocolValidationRules.remove(i);
						}
					}
					
					protocolValidationRules.removeAll(needToBeRemovedRuleList);
					
				}
				
				Assert.notNull(protocolValidationRules);

				Set<String> valueKeys = getValidationRuleContainer().getCachedValueKeys("modificationValidationRules");

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

				validationResponses = validationRuleHandler.validate(protocolValidationRules, values);

			}
			
			String xmlDataString = protocolXmlData.getXmlData();
			
			Map<String, List<String>> xmlDataValues = formService.getValuesFromXmlString(xmlDataString, xmlDataXPathList);
			
			ValidationResponse pharmacyValidationVP = pharmacyReviewValidation(protocolFormId);
			if (pharmacyValidationVP != null){
				validationResponses.add(pharmacyValidationVP);
			}
			
			ValidationResponse fundingSourceValidationVp = fundingSourceValidation(xmlDataString, xmlDataValues);
			if (fundingSourceValidationVp != null){
				validationResponses.add(fundingSourceValidationVp);
			}
			
			ValidationResponse departmentValidationVp = departmentValidation(xmlDataString);
			if (departmentValidationVp != null){
				validationResponses.add(departmentValidationVp);
			}
			
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

	public ProtocolFormCommitteeStatusDao getProtocolFormCommitteeStatusDao() {
		return protocolFormCommitteeStatusDao;
	}
	
	@Autowired(required=true)
	public void setProtocolFormCommitteeStatusDao(
			ProtocolFormCommitteeStatusDao protocolFormCommitteeStatusDao) {
		this.protocolFormCommitteeStatusDao = protocolFormCommitteeStatusDao;
	}

	public ProtocolFormReviewLogicServiceContainer getProtocolFormReviewLogicServiceContainer() {
		return protocolFormReviewLogicServiceContainer;
	}
	
	@Autowired(required=true)
	public void setProtocolFormReviewLogicServiceContainer(
			ProtocolFormReviewLogicServiceContainer protocolFormReviewLogicServiceContainer) {
		this.protocolFormReviewLogicServiceContainer = protocolFormReviewLogicServiceContainer;
	}

	public FormService getFormService() {
		return formService;
	}
	
	@Autowired(required=true)
	public void setFormService(FormService formService) {
		this.formService = formService;
	}
}
