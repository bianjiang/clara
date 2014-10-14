package edu.uams.clara.webapp.protocol.service.protocolform.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;

import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.common.collect.Lists;
import edu.uams.clara.webapp.common.businesslogic.form.validator.ValidationResponse;
import edu.uams.clara.webapp.common.businesslogic.form.validator.constraint.Constraint;
import edu.uams.clara.webapp.common.businesslogic.form.validator.constraint.enums.ConstraintLevel;
import edu.uams.clara.webapp.common.domain.usercontext.enums.Committee;
import edu.uams.clara.webapp.common.service.form.FormService;
import edu.uams.clara.webapp.protocol.dao.businesslogicobject.ProtocolFormCommitteeStatusDao;
import edu.uams.clara.webapp.protocol.dao.protocolform.ProtocolFormDao;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.ProtocolFormCommitteeStatus;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.enums.ProtocolFormCommitteeStatusEnum;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolForm;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolFormXmlData;
import edu.uams.clara.webapp.protocol.domain.protocolform.enums.ProtocolFormXmlDataType;
import edu.uams.clara.webapp.protocol.service.protocolform.ProtocolFormReviewLogicServiceContainer;
import edu.uams.clara.webapp.protocol.service.protocolform.ProtocolFormValidationService;
import edu.uams.clara.webapp.protocol.service.protocolform.ProtocolFormXmlDataDocumentService;
import edu.uams.clara.webapp.xml.processor.XmlProcessor;

public class ProtocolFormValidationServiceImpl implements
		ProtocolFormValidationService {
	private FormService formService;
	
	private ProtocolFormReviewLogicServiceContainer protocolFormReviewLogicServiceContainer;
	
	private ProtocolFormXmlDataDocumentService protocolFormXmlDataDocumentService;
	
	private XmlProcessor xmlProcessor;
	
	private ProtocolFormDao protocolFormDao;
	
	private ProtocolFormCommitteeStatusDao protocolFormCommitteeStatusDao;
	
	private List<String> xmlDataXPathList = Lists.newArrayList();{
		xmlDataXPathList.add("/protocol/site-responsible");
		xmlDataXPathList.add("/protocol/study-type/investigator-initiated/investigator-description");
		//xmlDataXPathList.add("/protocol/budget-created");
		//xmlDataXPathList.add("/protocol/budget/potentially-billed");
		xmlDataXPathList.add("/protocol/need-budget");
		xmlDataXPathList.add("/protocol/responsible-department");
		xmlDataXPathList.add("/protocol/study-nature");
		xmlDataXPathList.add("/protocol/study-nature/hud-use/where");
		xmlDataXPathList.add("/protocol/site-responsible/enroll-subject-in-uams");
		xmlDataXPathList.add("/continuing-review/need-cr");
		xmlDataXPathList.add("/reportable-new-information/is-reportable");
	}
	
	private ValidationResponse pharmacyReviewValidation(ProtocolForm protocolForm, String xmlDataString, Map<String, List<String>> values){
		Constraint pharmacyValidationConstraint = new Constraint();
		Map<String, Object> pharmacyValidationAdditionalData = new HashMap<String, Object>();
		
		ValidationResponse pharmacyValidationVP = null;
		
		try{
			
			String siteResponsible = formService.getSafeStringValueByKey(values, "/protocol/site-responsible", "");
			
			List<String> sitesId = xmlProcessor.getAttributeValuesByPathAndAttributeName("/protocol/study-sites/site", protocolForm.getMetaDataXml(), "site-id");
			
			if (siteResponsible.equals("uams") || (siteResponsible.equals("ach-achri") && sitesId.contains("7"))){

				ProtocolFormCommitteeStatus pfcs = protocolFormCommitteeStatusDao.getLatestByCommitteeAndProtocolFormId(Committee.PHARMACY_REVIEW, protocolForm.getId());
				
				if (pfcs == null){
					
					if (protocolFormReviewLogicServiceContainer.getProtocolFormReviewLogicService("NEW_SUBMISSION").isInvolvedByType(protocolForm, "Drug")){
						pharmacyValidationConstraint.setConstraintLevel(ConstraintLevel.ERROR);
						pharmacyValidationConstraint.setErrorMessage("Need to request Pharmacy Review before submit the form!");
						
						pharmacyValidationAdditionalData.put("pagename", "Drug and Device");
						pharmacyValidationAdditionalData.put("pageref", "drugs-devices");
					}
				} else if (pfcs.getProtocolFormCommitteeStatus().equals(ProtocolFormCommitteeStatusEnum.IN_REVIEW_REQUESTED) || pfcs.getProtocolFormCommitteeStatus().equals(ProtocolFormCommitteeStatusEnum.IN_WAIVER_REQUESTED)){
					pharmacyValidationConstraint.setConstraintLevel(ConstraintLevel.ERROR);
					pharmacyValidationConstraint.setErrorMessage("Need to wait for Pharmacy to complete Review!");
					
					pharmacyValidationAdditionalData.put("pagename", "Review");
					pharmacyValidationAdditionalData.put("pageref", "review");
				} else if (pfcs.getProtocolFormCommitteeStatus().equals(ProtocolFormCommitteeStatusEnum.REJECTED)){
					pharmacyValidationConstraint.setConstraintLevel(ConstraintLevel.ERROR);
					pharmacyValidationConstraint.setErrorMessage("Since Pharmacy has rejected, you cannot submit the protocol at this time!");
					
					pharmacyValidationAdditionalData.put("pagename", "Review");
					pharmacyValidationAdditionalData.put("pageref", "review");
				}
				
				//logger.debug("add: " + pharmacyValidationAdditionalData);
			}
			
			if (pharmacyValidationConstraint != null && (pharmacyValidationAdditionalData != null && !pharmacyValidationAdditionalData.isEmpty())){
				pharmacyValidationVP = new ValidationResponse(pharmacyValidationConstraint, pharmacyValidationAdditionalData);
			}
			
		} catch (Exception e){
			e.printStackTrace();
		}

		return pharmacyValidationVP;
	}
	
	private ValidationResponse pharmacyReviewValidationForMod(ProtocolForm protocolForm){
		Constraint pharmacyValidationConstraint = new Constraint();
		Map<String, Object> pharmacyValidationAdditionalData = new HashMap<String, Object>();
		
		ValidationResponse pharmacyValidationVP = null;
		
		try{
			ProtocolFormCommitteeStatus pfcs = protocolFormCommitteeStatusDao.getLatestByCommitteeAndProtocolFormId(Committee.PHARMACY_REVIEW, protocolForm.getId());
			
			if (pfcs != null){
				if (pfcs.getProtocolFormCommitteeStatus().equals(ProtocolFormCommitteeStatusEnum.IN_REVIEW_REQUESTED) || pfcs.getProtocolFormCommitteeStatus().equals(ProtocolFormCommitteeStatusEnum.IN_WAIVER_REQUESTED)){
					pharmacyValidationConstraint.setConstraintLevel(ConstraintLevel.ERROR);
					pharmacyValidationConstraint.setErrorMessage("Need to wait for Pharmacy to complete Review!");
					
					pharmacyValidationAdditionalData.put("pagename", "Review");
					pharmacyValidationAdditionalData.put("pageref", "review");
				}
			}
	
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
			
			//String budgetCreated = formService.getSafeStringValueByKey(values, "/protocol/budget-created", "");
			
			//String haveBudget = formService.getSafeStringValueByKey(values, "/protocol/budget/potentially-billed", "");
			
			String enrollSubjectInUams = formService.getSafeStringValueByKey(values, "/protocol/site-responsible/enroll-subject-in-uams", "");
			
			String needBudget = formService.getSafeStringValueByKey(values, "/protocol/need-budget", "");
			
			String studyNature = formService.getSafeStringValueByKey(values, "/protocol/study-nature", "");
			
			String hudStudyLocation = formService.getSafeStringValueByKey(values, "/protocol/study-nature/hud-use/where", "");

			if (studyNature.equals("hud-use") && hudStudyLocation.equals("ach/achri")) {
				return fsValidationVP;
			}
			
			if (siteResponsible.equals("uams") || enrollSubjectInUams.equals("y")) {
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

						if (instestigatorDesc.equals("student-fellow-resident-post-doc") && needBudget.equals("y") && (noFundingEl.getAttribute("entityname").isEmpty() || noFundingEl.getAttribute("department").isEmpty() || noFundingEl.getAttribute("name").isEmpty())){
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
	
	private List<ValidationResponse> userValidation(String xmlData, List<ValidationResponse> validationResponses) {
		try {
			List<String> noClaraUserList = formService.getNoClaraUsers(xmlData);
			
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
	
	private List<ValidationResponse> requiredDocumentValidation(ProtocolFormXmlData protocolXmlData, List<ValidationResponse> validationResponses) {
		try{
			Map<String, Boolean> documentMap = protocolFormXmlDataDocumentService.checkRequiredDocuments(protocolXmlData);
			
			for(Map.Entry<String, Boolean> entry : documentMap.entrySet()){
				
				if (!entry.getValue()){
					Constraint documentConstraint = new Constraint();
					Map<String, Object> documentAdditionalData = new HashMap<String, Object>();
					
					documentConstraint.setConstraintLevel(ConstraintLevel.ERROR);
					documentConstraint.setErrorMessage(entry.getKey() + " is required!");
					
					documentAdditionalData.put("pagename", "Documents");
					documentAdditionalData.put("pageref", "documents");
					
					ValidationResponse documentVP = new ValidationResponse(documentConstraint, documentAdditionalData);
					
					validationResponses.add(documentVP);
				}
			}
		} catch (Exception e){
			e.printStackTrace();
		}
		
		return validationResponses;
	}
	
	private ValidationResponse needCRValidation(Map<String, List<String>> values) {
		ValidationResponse needCRValidationVP = null;
		
		String needCR = formService.getSafeStringValueByKey(values, "/continuing-review/need-cr", "");
		
		if (!needCR.equals("y")) {
			Constraint notNeedCRConstraint = new Constraint();
			Map<String, Object> notNeedCRAdditionalData = new HashMap<String, Object>();
			
			notNeedCRConstraint.setConstraintLevel(ConstraintLevel.ERROR);
			notNeedCRConstraint.setErrorMessage("To close this study, please exit this Continuing Review and submit a Study Closure form instead.");
			
			notNeedCRAdditionalData.put("pagename", "Review");
			notNeedCRAdditionalData.put("pageref", "review");
			
			needCRValidationVP = new ValidationResponse(notNeedCRConstraint, notNeedCRAdditionalData);
		}
		
		return needCRValidationVP;
	}
	
	private ValidationResponse isReportableValidation(Map<String, List<String>> values) {
		ValidationResponse isReportableValidationVP = null;
		
		String isReportable = formService.getSafeStringValueByKey(values, "/reportable-new-information/is-reportable", "");
		
		if (!isReportable.equals("y")) {
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
			
			isReportableValidationVP = new ValidationResponse(notReportableConstraint, notReportableAdditionalData);
		}
		
		return isReportableValidationVP;
	}

	@Override
	public List<ValidationResponse> getExtraValidationResponses(
			ProtocolFormXmlData protocolFormXmlData, List<ValidationResponse> validationResponses) {
		ProtocolFormXmlDataType pfxdType = protocolFormXmlData.getProtocolFormXmlDataType();
		
		String xmlData = protocolFormXmlData.getXmlData();
		
		Map<String, List<String>> xmlDataValues = formService.getValuesFromXmlString(xmlData, xmlDataXPathList);
		
		switch(pfxdType) {
		case PROTOCOL:
		case MODIFICATION:
			if (pfxdType.equals(ProtocolFormXmlDataType.MODIFICATION)) {
				ValidationResponse pharmacyValidationForMod = this.pharmacyReviewValidationForMod(protocolFormXmlData.getProtocolForm());
				
				if (pharmacyValidationForMod != null) {
					validationResponses.add(pharmacyValidationForMod);
				}
			} else {
				ValidationResponse pharmacyValidation = this.pharmacyReviewValidation(protocolFormXmlData.getProtocolForm(), xmlData, xmlDataValues);
				
				if (pharmacyValidation != null) {
					validationResponses.add(pharmacyValidation);
				}
			}
			
			ValidationResponse fundingSourceValidation = this.fundingSourceValidation(xmlData, xmlDataValues);
			ValidationResponse departmentValidation = this.departmentValidation(xmlData);
			
			if (fundingSourceValidation != null) {
				validationResponses.add(fundingSourceValidation);
			}
			
			if (departmentValidation != null) {
				validationResponses.add(departmentValidation);
			}
			
			validationResponses = this.userValidation(xmlData, validationResponses);
			
			validationResponses = this.requiredDocumentValidation(protocolFormXmlData, validationResponses);
			
			break;
		case CONTINUING_REVIEW:
			ValidationResponse needCRValidation = this.needCRValidation(xmlDataValues);
			
			if (needCRValidation != null) {
				validationResponses.add(needCRValidation);
			}
			
			validationResponses = this.userValidation(xmlData, validationResponses);
			
			break;
		case REPORTABLE_NEW_INFORMATION:
			ValidationResponse isReportableValidation = this.isReportableValidation(xmlDataValues);
			
			if (isReportableValidation != null) {
				validationResponses.add(isReportableValidation);
			}
			
			break;
		case STAFF:
			validationResponses = this.userValidation(xmlData, validationResponses);
			
			break;
		}
		return validationResponses;
	}

	public FormService getFormService() {
		return formService;
	}
	
	@Autowired(required = true)
	public void setFormService(FormService formService) {
		this.formService = formService;
	}

	public ProtocolFormDao getProtocolFormDao() {
		return protocolFormDao;
	}
	
	@Autowired(required = true)
	public void setProtocolFormDao(ProtocolFormDao protocolFormDao) {
		this.protocolFormDao = protocolFormDao;
	}

	public ProtocolFormCommitteeStatusDao getProtocolFormCommitteeStatusDao() {
		return protocolFormCommitteeStatusDao;
	}
	
	@Autowired(required = true)
	public void setProtocolFormCommitteeStatusDao(
			ProtocolFormCommitteeStatusDao protocolFormCommitteeStatusDao) {
		this.protocolFormCommitteeStatusDao = protocolFormCommitteeStatusDao;
	}

	public ProtocolFormReviewLogicServiceContainer getProtocolFormReviewLogicServiceContainer() {
		return protocolFormReviewLogicServiceContainer;
	}
	
	@Autowired(required = true)
	public void setProtocolFormReviewLogicServiceContainer(
			ProtocolFormReviewLogicServiceContainer protocolFormReviewLogicServiceContainer) {
		this.protocolFormReviewLogicServiceContainer = protocolFormReviewLogicServiceContainer;
	}

	public XmlProcessor getXmlProcessor() {
		return xmlProcessor;
	}
	
	@Autowired(required = true)
	public void setXmlProcessor(XmlProcessor xmlProcessor) {
		this.xmlProcessor = xmlProcessor;
	}

	public ProtocolFormXmlDataDocumentService getProtocolFormXmlDataDocumentService() {
		return protocolFormXmlDataDocumentService;
	}
	
	@Autowired(required = true)
	public void setProtocolFormXmlDataDocumentService(
			ProtocolFormXmlDataDocumentService protocolFormXmlDataDocumentService) {
		this.protocolFormXmlDataDocumentService = protocolFormXmlDataDocumentService;
	}

}
