package edu.uams.clara.webapp.common.web.form.ajax;

import javax.xml.transform.Source;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import edu.uams.clara.core.util.xml.DomUtils;
import edu.uams.clara.webapp.common.domain.relation.RelatedObject;
import edu.uams.clara.webapp.common.domain.usercontext.enums.Committee;
import edu.uams.clara.webapp.common.service.relation.RelationService;
import edu.uams.clara.webapp.common.util.XMLResponseHelper;
import edu.uams.clara.webapp.common.util.response.JsonResponse;
import edu.uams.clara.webapp.contract.dao.contractform.ContractFormDao;
import edu.uams.clara.webapp.contract.domain.contractform.ContractForm;
import edu.uams.clara.webapp.protocol.dao.protocolform.ProtocolFormDao;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolForm;
import edu.uams.clara.webapp.xml.processor.XmlProcessor;

@Controller
public class GeneralFormInformationAjaxController {
	
	private final static Logger logger = LoggerFactory
			.getLogger(GeneralFormInformationAjaxController.class);

	private ProtocolFormDao protocolFormDao;
	
	private ContractFormDao contractFormDao;
	
	private XmlProcessor xmlProcessor;
	
	private RelationService relationService;
	
	
	@RequestMapping(value = "/ajax/{objectType}/{objectType}-forms/{formId}/list-reviewers", method = RequestMethod.GET, produces="application/xml")

	public @ResponseBody Source getAssignedReviewers(@PathVariable("formId") long formId, @PathVariable("objectType") String objectType, @RequestParam("committee") Committee assigningCommittee, @RequestParam("userId") long userId){

	
		String metaDataXml = null;
		if("protocol".equals(objectType)){
			ProtocolForm protocolForm = protocolFormDao.findById(formId);
			metaDataXml = protocolForm.getMetaDataXml();
		}else if ("contract".equals(objectType)){
			ContractForm contractForm =  contractFormDao.findById(formId);
			metaDataXml = contractForm.getMetaDataXml();
		}else{
			return XMLResponseHelper.newErrorResponseStub("no metaData associated with this form!");
		}

		String assignedReviewersXml = "";
		if (metaDataXml != null && !metaDataXml.isEmpty()){
			try {
				Document extraXmlDataDoc = xmlProcessor.loadXmlStringToDOM(metaDataXml);
				
				XPath xPath = xmlProcessor.getXPathInstance();

				//NodeList assignedReviewers = (NodeList) xPath.evaluate("//assigned-reviewer[@assigning-committee='"+ assigningCommittee + "']", extraXmlDataDoc, XPathConstants.NODESET);
				NodeList assignedReviewers = (NodeList) xPath.evaluate("//assigned-reviewer", extraXmlDataDoc, XPathConstants.NODESET);

				for (int j = 0; j < assignedReviewers.getLength(); j ++){
					
					Element assignedReviewerEl = (Element)assignedReviewers.item(j);
					
					assignedReviewersXml += DomUtils.elementToString(assignedReviewerEl);
					logger.debug("formId: " + formId + " assignedReviewer xml: " + assignedReviewersXml);
					//if (Long.parseLong(assignedReviewerEl.getAttribute("user-id")) == userId){
						//isMine = true;
						//break;
					//}								
				}
			} catch (Exception ex){
				logger.error("errors when grabing the assigned reviewers from form metadata", ex);
				return XMLResponseHelper.newErrorResponseStub("errors when grabing the assigned reviewers from form metadata");
			}			
		}else{
			return XMLResponseHelper.newSuccessResponseStube("no reviewers assigned!");
		}
		
		String xmlData = "<assigned-reviewers>";
		if (!assignedReviewersXml.isEmpty()){
			xmlData += assignedReviewersXml;
		}
		xmlData += "</assigned-reviewers>";
		
		
		return XMLResponseHelper.newDataResponseStub(xmlData);	
		
	}
	
	@RequestMapping(value = "/ajax/add-related-object", method = RequestMethod.POST)
	public @ResponseBody
	JsonResponse addRelatedObject(@RequestParam("objectId") long objectId,
			@RequestParam("objectType") String objectType,
			@RequestParam("relatedObjectId") long relatedObjectId,
			@RequestParam("relatedObjectType") String relatedObjectType,
			@RequestParam("userId") long userId) {
		//might need to restrict roles
		try{
			RelatedObject relatedObject = relationService.addRelationByIdAndType(objectId, relatedObjectId, objectType, relatedObjectType);	
			return new JsonResponse(false, "", null, false, relatedObject);
		} catch (Exception e){
			e.printStackTrace();
			return new JsonResponse(true, "Failed to add related contract!", "", false, null);
		}
		
	}
	
	@RequestMapping(value = "/ajax/delete-related-object", method = RequestMethod.POST)
	public @ResponseBody
	JsonResponse deleteRelatedObject(@RequestParam("objectId") long objectId,
			@RequestParam("objectType") String objectType,
			@RequestParam("relatedObjectId") long relatedObjectId,
			@RequestParam("relatedObjectType") String relatedObjectType,
			@RequestParam("userId") long userId) {
		//might need to restrict roles
		try{
			RelatedObject relatedObject = relationService.removeRelationByIdAndType(objectId, relatedObjectId, objectType, relatedObjectType);	
			return new JsonResponse(false, "", "", false, relatedObject);
		} catch (Exception e){
			e.printStackTrace();
			return new JsonResponse(true, "Failed to delete related contract!", "", false, null);
		}
		
	}

	public ProtocolFormDao getProtocolFormDao() {
		return protocolFormDao;
	}


	@Autowired(required = true)
	public void setProtocolFormDao(ProtocolFormDao protocolFormDao) {
		this.protocolFormDao = protocolFormDao;
	}


	public ContractFormDao getContractFormDao() {
		return contractFormDao;
	}


	@Autowired(required = true)
	public void setContractFormDao(ContractFormDao contractFormDao) {
		this.contractFormDao = contractFormDao;
	}


	public XmlProcessor getXmlProcessor() {
		return xmlProcessor;
	}

	@Autowired(required = true)
	public void setXmlProcessor(XmlProcessor xmlProcessor) {
		this.xmlProcessor = xmlProcessor;
	}
	
	public RelationService getRelationService() {
		return relationService;
	}
	
	@Autowired(required = true)
	public void setRelationService(RelationService relationService) {
		this.relationService = relationService;
	}
	
}
