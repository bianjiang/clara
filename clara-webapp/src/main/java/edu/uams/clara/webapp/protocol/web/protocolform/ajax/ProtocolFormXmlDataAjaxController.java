package edu.uams.clara.webapp.protocol.web.protocolform.ajax;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.xml.transform.Source;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
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
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import edu.uams.clara.core.util.xml.DomUtils;
import edu.uams.clara.core.util.xml.XmlHandler;
import edu.uams.clara.core.util.xml.XmlHandlerFactory;
import edu.uams.clara.webapp.common.businesslogic.BusinessObjectStatusHelperContainer;
import edu.uams.clara.webapp.common.domain.history.Track;
import edu.uams.clara.webapp.common.domain.relation.RelatedObject;
import edu.uams.clara.webapp.common.domain.usercontext.User;
import edu.uams.clara.webapp.common.domain.usercontext.enums.Committee;
import edu.uams.clara.webapp.common.domain.usercontext.enums.Permission;
import edu.uams.clara.webapp.common.security.ObjectAclService;
import edu.uams.clara.webapp.common.service.audit.AuditService;
import edu.uams.clara.webapp.common.service.relation.RelationService;
import edu.uams.clara.webapp.common.util.DateFormatUtil;
import edu.uams.clara.webapp.common.util.XMLResponseHelper;
import edu.uams.clara.webapp.protocol.dao.ProtocolDao;
import edu.uams.clara.webapp.protocol.dao.protocolform.ProtocolFormDao;
import edu.uams.clara.webapp.protocol.dao.protocolform.ProtocolFormXmlDataDao;
import edu.uams.clara.webapp.protocol.domain.Protocol;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolForm;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolFormXmlData;
import edu.uams.clara.webapp.protocol.domain.protocolform.enums.ProtocolFormType;
import edu.uams.clara.webapp.protocol.domain.protocolform.enums.ProtocolFormXmlDataType;
import edu.uams.clara.webapp.protocol.service.ProtocolFormService;
import edu.uams.clara.webapp.protocol.service.ProtocolMetaDataXmlService;
import edu.uams.clara.webapp.protocol.service.history.ProtocolTrackService;
import edu.uams.clara.webapp.xml.processor.XmlProcessor;

/**
 * this controller should return xml string and set the content type as
 * text/xml...
 */
@Controller
public class ProtocolFormXmlDataAjaxController {

	private final static Logger logger = LoggerFactory
			.getLogger(ProtocolFormXmlDataAjaxController.class);

	private ProtocolDao protocolDao;

	private ProtocolFormDao protocolFormDao;

	private ProtocolFormXmlDataDao protocolFormXmlDataDao;

	private ProtocolMetaDataXmlService protocolMetaDataXmlService;
	
	private ObjectAclService objectAclService;
	
	private RelationService relationService;
	
	private AuditService auditService;
	
	private ProtocolFormService protocolFormService;
	
	private ProtocolTrackService protocolTrackService;

	private XmlProcessor xmlProcessor;
	
	private BusinessObjectStatusHelperContainer businessObjectStatusHelperContainer;
	
	private List<ProtocolFormType> toUpdateProtocolMetaDataFormTypeLst = Lists.newArrayList();{
		toUpdateProtocolMetaDataFormTypeLst.add(ProtocolFormType.NEW_SUBMISSION);
		toUpdateProtocolMetaDataFormTypeLst.add(ProtocolFormType.EMERGENCY_USE);
		toUpdateProtocolMetaDataFormTypeLst.add(ProtocolFormType.HUMAN_SUBJECT_RESEARCH_DETERMINATION);
	}

	@RequestMapping(value = "/ajax/protocols/{protocolId}/protocol-forms/{protocolFormId}/{protocolFormXmlDataType}/update", method = RequestMethod.POST, produces="application/xml")
	public @ResponseBody
	Source updateProtocolFormXmlDataByProtocolFormXmlDataType(
			@PathVariable("protocolFormId") long protocolFormId,
			@PathVariable("protocolFormXmlDataType") ProtocolFormXmlDataType protocolFormXmlDataType,
			@RequestParam("pagefragment") String xmldata) {

		ProtocolForm protocolForm = protocolFormDao.findById(protocolFormId);

		ProtocolFormXmlData protocolFormXmlData = protocolForm
				.getTypedProtocolFormXmlDatas().get(protocolFormXmlDataType);

		String mergedXmlString = protocolFormXmlData.getXmlData();

		if (StringUtils.hasText(xmldata)) {

			try {
				mergedXmlString = xmlProcessor.merge(mergedXmlString, xmldata);
			} catch (SAXException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			protocolFormXmlData.setXmlData(mergedXmlString);

			protocolFormXmlData = protocolFormXmlDataDao
					.saveOrUpdate(protocolFormXmlData);

			protocolForm = protocolMetaDataXmlService.updateProtocolFormMetaDataXml(protocolFormXmlData, null);

			if (toUpdateProtocolMetaDataFormTypeLst.contains(protocolForm.getProtocolFormType())){
				protocolMetaDataXmlService
				.updateProtocolMetaDataXml(protocolForm);
			}

		}

		return XMLResponseHelper.newSuccessResponseStube(Boolean.TRUE.toString());

	}

	@RequestMapping(value = "/ajax/protocols/{protocolId}/protocol-forms/{protocolFormId}/delete", method = RequestMethod.GET, produces="application/xml")
	public @ResponseBody
	Source deleteProtocolForm(@PathVariable("protocolId") long protocolId,
			@PathVariable("protocolFormId") long protocolFormId) {
		User u = (User) SecurityContextHolder.getContext().getAuthentication()
				.getPrincipal();
		
		ProtocolForm protocolForm = protocolFormDao.findById(protocolFormId);
		Protocol protocol = protocolForm.getProtocol();
		
		try{
			ProtocolFormXmlData protocolFormXmlData = protocolForm.getTypedProtocolFormXmlDatas().get(protocolForm.getProtocolFormType().getDefaultProtocolFormXmlDataType());
			if (protocolFormXmlData != null){
				protocolFormXmlData.setRetired(true);
				protocolFormXmlDataDao.saveOrUpdate(protocolFormXmlData);
			}
				
			ProtocolFormXmlData budgetXmlData = protocolForm.getTypedProtocolFormXmlDatas().get(ProtocolFormXmlDataType.BUDGET);
			if (budgetXmlData != null){
				budgetXmlData.setRetired(true);
				protocolFormXmlDataDao.saveOrUpdate(budgetXmlData);
			}
			
			ProtocolFormXmlData pharmacyXmlData = protocolForm.getTypedProtocolFormXmlDatas().get(ProtocolFormXmlDataType.PHARMACY);
			if (pharmacyXmlData != null){
				pharmacyXmlData.setRetired(true);
				protocolFormXmlDataDao.saveOrUpdate(pharmacyXmlData);
			}
			
			protocolForm.setRetired(true);
			protocolFormDao.saveOrUpdate(protocolForm);
			
			if (protocolForm.getProtocolFormType().equals(ProtocolFormType.NEW_SUBMISSION) || protocolForm.getProtocolFormType().equals(ProtocolFormType.HUMAN_SUBJECT_RESEARCH_DETERMINATION) || protocolForm.getProtocolFormType().equals(ProtocolFormType.EMERGENCY_USE)){
				/*
				 * Need to delete all relations, eg. contracts if protocol is deleted
				 * */
				try {
					List<RelatedObject> relatedObjects = relationService.getRelationsByIdAndType(protocolId, "protocol");
					
					if (relatedObjects.size() > 0) {
						for (RelatedObject ro : relatedObjects) {
							relationService.removeRelation(ro);
						}
					}
				} catch (Exception e) {
					//don't care ...
				}
				
				protocol.setRetired(true);
				protocolDao.saveOrUpdate(protocol);
				
				auditService.auditEvent("PROTOCOL_DELETED",
						u.getPerson().getFullname() + " has deleted protocol - protocolId:"+ protocolId);
			}
			
			Track track = protocolTrackService.getOrCreateTrack("PROTOCOL",
					protocolId);

			Document logsDoc = protocolTrackService.getLogsDocument(track);

			Element logEl = logsDoc.createElement("log");
			
			Date now = new Date();
			
			String logId = UUID.randomUUID().toString();
			
			logEl.setAttribute("id", logId);
			logEl.setAttribute("parent-id", logId);
			logEl.setAttribute("action-user-id", String.valueOf(u.getId()));
			logEl.setAttribute("actor", u.getPerson().getFullname());
			logEl.setAttribute("date-time", DateFormatUtil.formateDate(now));
			logEl.setAttribute("event-type", "PROTOCOL_FORM_DELETE");
			logEl.setAttribute("form-id", String.valueOf(protocolFormId));
			logEl.setAttribute("parent-form-id", String.valueOf(protocolForm.getParent().getId()));
			logEl.setAttribute("form-type", protocolForm.getFormType());
			logEl.setAttribute("log-type", "ACTION");
			logEl.setAttribute("timestamp", String.valueOf(now.getTime()));

			String message = ""+ protocolForm.getProtocolFormType().getDescription() +" Form has been deleted by "
					+ u.getPerson().getFullname() + "";

			logEl.setTextContent(message);

			logsDoc.getDocumentElement().appendChild(logEl);

			track = protocolTrackService.updateTrack(track, logsDoc);
		} catch (Exception e){
			e.printStackTrace();
			return XMLResponseHelper.newErrorResponseStub("Failed to delete form!");
		}
	
		return XMLResponseHelper.newSuccessResponseStube("Successful");

	}
	
	@RequestMapping(value = "/ajax/protocols/{protocolId}/protocol-forms/{protocolFormId}/cancel", method = RequestMethod.POST, produces="application/xml")
	public @ResponseBody
	Source cancelProtocolForm(
			@PathVariable("protocolId") long protocolId,
			@PathVariable("protocolFormId") long protocolFormId,
			@RequestParam("xml") String xml) {
		ProtocolForm protocolForm = protocolFormDao.findById(protocolFormId);
		
		User u = (User) SecurityContextHolder.getContext().getAuthentication()
				.getPrincipal();
		
		if (objectAclService.hasEditObjectAccess(Protocol.class,
				protocolForm.getProtocol().getId(), u) || u.getAuthorities().contains(Permission.CANCEL_PROTOCOL_FORM)){
			try{
				XmlHandler xmlHandler = XmlHandlerFactory.newXmlHandler();
				
				//String cancelReason = xmlHandler.getSingleStringValueByXPath(xml, "/cancel-reason");
				
				if (objectAclService.hasEditObjectAccess(Protocol.class,
				protocolForm.getProtocol().getId(), u)) {
					xml = "<committee-review><committee type=\"PI\">" + xml + "</committee></committee-review>";
					
					businessObjectStatusHelperContainer.getBusinessObjectStatusHelper(protocolForm.getFormType().toString()).triggerAction(protocolForm, Committee.PI, u, "CANCEL_PROTOCOL_FORM", "", xml);
				} else if (u.getAuthorities().contains(Permission.CANCEL_PROTOCOL_FORM)) {
					xml = "<committee-review><committee type=\"IRB_OFFICE\">" + xml + "</committee></committee-review>";
					
					businessObjectStatusHelperContainer.getBusinessObjectStatusHelper(protocolForm.getFormType().toString()).triggerAction(protocolForm, Committee.IRB_OFFICE, u, "CANCEL_PROTOCOL_FORM", "", xml);
				}
				
				
			} catch (Exception e){
				e.printStackTrace();
				return XMLResponseHelper.newErrorResponseStub("Failed to cancel protocol form!");
			}
		} else {
			return XMLResponseHelper.newErrorResponseStub("You don't have the right to cacnel protocol form!");
		}
		
		return XMLResponseHelper.newSuccessResponseStube("Succed...");
	}
	
	private void updateBudgetExpenses(ProtocolForm protocolForm, String irbFeeXml){
		logger.debug("Updating budget expenses ...");
		try{
			ProtocolFormXmlData budgetXmlData = protocolForm.getTypedProtocolFormXmlDatas().get(ProtocolFormXmlDataType.BUDGET);
			
			if (budgetXmlData != null && budgetXmlData.getXmlData() != null && !budgetXmlData.getXmlData().isEmpty()){
				Document irbFeesDom = xmlProcessor.loadXmlStringToDOM(irbFeeXml);
				XPath xPath = xmlProcessor.getXPathInstance();
				NodeList categoriesList = (NodeList) (xPath.evaluate("/protocol/irb-fees/category",
						irbFeesDom, XPathConstants.NODESET));
				
				Document budgetXmlDom = xmlProcessor.loadXmlStringToDOM(budgetXmlData.getXmlData());
				xPath.reset();
				
				for (int i=0; i<categoriesList.getLength(); i++){
					Element currentCategoryElement = (Element) categoriesList
							.item(i);
					String nameValue = currentCategoryElement.getFirstChild().getTextContent();
					String feeValue = currentCategoryElement.getLastChild().getTextContent();
					
					String externalOrNot = "true";
					if (feeValue.isEmpty() || feeValue.equals("0")){
						externalOrNot = "false";
					}
					
					String expenseType = "Initial Cost";
					if (!nameValue.contains("New Submission")){
						expenseType = "Invoicable";
					}

					Element expensesEl = (Element) (xPath.evaluate("/budget/expenses", budgetXmlDom, XPathConstants.NODE));
					
					Element expenseEl = (Element) (xPath.evaluate("/budget/expenses/expense[@type=\""+ expenseType +"\" and @subtype=\"IRB Fee\"]",
							budgetXmlDom, XPathConstants.NODE));

					if (expenseEl != null){
						expenseEl.setAttribute("cost", feeValue);
						expenseEl.setAttribute("external", externalOrNot);
						//expenseEl.setAttribute("notes", nameValue);
					} else {
						Element newExpenseNode = budgetXmlDom.createElement("expense");
						newExpenseNode.setAttribute("type", expenseType);
						newExpenseNode.setAttribute("subtype", "IRB Fee");
						newExpenseNode.setAttribute("cost", feeValue);
						newExpenseNode.setAttribute("fa", "0");
						newExpenseNode.setAttribute("faenabled", "false");
						newExpenseNode.setAttribute("external", externalOrNot);
						newExpenseNode.setAttribute("count", "1");
						newExpenseNode.setAttribute("description", "IRB Fee");
						newExpenseNode.setAttribute("notes", nameValue);
						
						expensesEl.appendChild(newExpenseNode);
					}
				}
				
				budgetXmlData.setXmlData(DomUtils.elementToString(budgetXmlDom));
				protocolFormXmlDataDao.saveOrUpdate(budgetXmlData);
				
			}
		} catch (Exception e){
			e.printStackTrace();
		}
	}
	
	private void updateBudgetInfoByDepartment(ProtocolFormXmlData protocolFormXmlData, String xmlData){
		String protocolFormXmlDataString = protocolFormXmlData.getXmlData();
		
		String value = "";
		
		if (xmlData.contains("<responsible-department")){
			if (xmlData.contains("REP Regional Programs")){
				value = "n";
			} else {
				value = "y";
			}
			
			try{
				protocolFormXmlDataString = xmlProcessor.replaceOrAddNodeValueByPath("/protocol/need-budget-by-department", protocolFormXmlDataString, value);
				
				protocolFormXmlData.setXmlData(protocolFormXmlDataString);
				protocolFormXmlDataDao.saveOrUpdate(protocolFormXmlData);
			} catch (Exception e){
				e.printStackTrace();
			}
		}

	}
	
	private String updateIRBFee(String protocolFormXmlDataString, String xmlData) {
		try {
			String type = xmlProcessor.getAttributeValueByPathAndAttributeName("/protocol/irb-fees", protocolFormXmlDataString, "type");
			
			Map<String, Object> resultMap = xmlProcessor.deleteElementByPath("/protocol/irb-fees", protocolFormXmlDataString);
			
			String xmlDataAfterDeletion = resultMap.get("finalXml").toString();
			
			resultMap = xmlProcessor.addElementByPath("/protocol/irb-fees", xmlDataAfterDeletion, xmlData.replace("<protocol>", "").replace("</protocol>", ""), false);
			
			protocolFormXmlDataString = resultMap.get("finalXml").toString();
			
			Map<String, String> attributes = Maps.newHashMap();
			attributes.put("type", type);
			
			protocolFormXmlDataString = xmlProcessor.addAttributesByPath("/protocol/irb-fees", protocolFormXmlDataString, attributes);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return protocolFormXmlDataString;
	}

	@RequestMapping(value = "/ajax/protocols/{protocolId}/protocol-forms/{protocolFormId}/protocol-form-xml-datas/{protocolFormXmlDataId}/update", method = RequestMethod.POST, produces="application/xml")
	public @ResponseBody
	Source updateProtocolFormXmlDataById(@PathVariable("protocolFormId") long protocolFormId,
			@PathVariable("protocolFormXmlDataId") long protocolFormXmlDataId,
			@RequestParam("pagefragment") String xmldata) {

		logger.debug("pagefragment xmldata: " + xmldata);
		ProtocolForm protocolForm = protocolFormDao.findById(protocolFormId);
		ProtocolFormXmlData protocolFormXmlData = protocolFormXmlDataDao
				.findById(protocolFormXmlDataId);

		String protocolFormXmlDataString = protocolFormXmlData.getXmlData();

		if (StringUtils.hasText(xmldata)) {

			try {
				if (!xmldata.contains("<irb-fees>")) {				
					protocolFormXmlDataString = xmlProcessor.merge(protocolFormXmlDataString, xmldata);
				} else {
					protocolFormXmlDataString = updateIRBFee(protocolFormXmlDataString, xmldata);
					updateBudgetExpenses(protocolForm, xmldata);
				}
				
			} catch (SAXException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			protocolFormXmlData.setXmlData(protocolFormXmlDataString);

			protocolFormXmlData = protocolFormXmlDataDao
					.saveOrUpdate(protocolFormXmlData);

			// logger.debug(mergedXmlString);

			logger.debug(protocolFormXmlData.getProtocolFormXmlDataType()
					.toString());

			protocolForm = protocolMetaDataXmlService.updateProtocolFormMetaDataXml(protocolFormXmlData, null);

			if (toUpdateProtocolMetaDataFormTypeLst.contains(protocolForm.getProtocolFormType())){
				protocolMetaDataXmlService
				.updateProtocolMetaDataXml(protocolForm);
			}
			
			if (xmldata.contains("<study-type>") || xmldata.contains("<site-responsible>")){
				logger.debug("update IRB fees");
				try{
					protocolFormService.generateIRBFees(protocolFormXmlData);
				} catch (Exception e){
					e.printStackTrace();
				}
				updateBudgetExpenses(protocolForm, protocolFormXmlData.getXmlData());
			}
			
			updateBudgetInfoByDepartment(protocolFormXmlData, xmldata);
			
			logger.debug("elementXml: " + xmldata);
			if(xmldata.contains("</staffs>")){
				logger.debug("update user acl based on staff xml");
				objectAclService.updateObjectAclByStaffXml(Protocol.class, protocolForm.getProtocol().getId(), xmldata);
			}
			
			/* staff will never be updated... in ACL...
			if(xmldata.contains("<staffs>")){
				logger.debug("update user acl based on staff xml");
				objectAclService.updateObjectAclByStaffXml(Protocol.class, protocolForm.getProtocol().getId(), protocolFormXmlData.getXmlData());
			}
			*/

		}

		return XMLResponseHelper.newSuccessResponseStube(Boolean.TRUE.toString());

	}

	/**
	 * @ToDo need to add protocolMetaDataXmlService.updateProtocolMetaDataXml,
	 *       since PI is part of the protocol.metaData, and adding PI is done
	 *       here.. There will be a problem of removing PI... the system is not
	 *       smart enough yet to remove PI from metadata xml if the PI is
	 *       removed from protocol ...
	 * @param protocolFormXmlDataId
	 * @param listPath
	 * @param elementXml
	 * @return
	 */
	@RequestMapping(value = "/ajax/protocols/{protocolId}/protocol-forms/{protocolFormId}/protocol-form-xml-datas/{protocolFormXmlDataId}/xml-elements/add")
	public @ResponseBody
	String addXmlElementToProtocolXmlData(
			@PathVariable("protocolFormXmlDataId") long protocolFormXmlDataId,
			@RequestParam("listPath") String listPath,
			@RequestParam("elementXml") String elementXml) {

		ProtocolFormXmlData protocolFormXmlData = protocolFormXmlDataDao
				.findById(protocolFormXmlDataId);
		
		ProtocolForm protocolForm = protocolFormXmlData.getProtocolForm();

		String originalXml = protocolFormXmlData.getXmlData();

		Map<String, Object> resultMap = null;
		
		String processedElementXml = "";

		try {
			//String excapedXml = xmlProcessor.escapeText(elementXml);
			resultMap = xmlProcessor.addElementByPath(listPath, originalXml,
					elementXml, true);

			Assert.notEmpty(resultMap);

			Assert.notNull(resultMap.get("finalXml"));
			Assert.notNull(resultMap.get("elementXml"));
			Assert.notNull(resultMap.get("elementId"));
			
			protocolFormXmlData.setXmlData(resultMap.get("finalXml").toString());
			

			protocolFormXmlData = protocolFormXmlDataDao
					.saveOrUpdate(protocolFormXmlData);
			
			protocolForm = protocolMetaDataXmlService.updateProtocolFormMetaDataXml(protocolFormXmlData, null);

			if (toUpdateProtocolMetaDataFormTypeLst.contains(protocolForm.getProtocolFormType())){
				protocolMetaDataXmlService
				.updateProtocolMetaDataXml(protocolForm);
			}

			if(elementXml.contains("</staff>")){
				logger.debug("update user acl based on staff xml");
				objectAclService.updateObjectAclByStaffXml(Protocol.class, protocolForm.getProtocol().getId(), elementXml);
			}
			
			processedElementXml = resultMap.get("elementXml").toString();
			//finalXml = xmlProcessor.escapeText(protocolFormXmlData.getXmlData());
		} catch (Exception ex) {
			logger.error("failed to add element to protocolFormXmlDataId: " + protocolFormXmlDataId + "; xmlData: " + elementXml, ex);
		}
		
		return processedElementXml;
	}

	/**
	 * This funciton is used to add sub-elements to a specified (e.g. a element
	 * with id) element the function first use the xpath to find the parent
	 * element, and then add the elementXml, return the id of the newly added
	 * element
	 * 
	 * @TODO
	 * @param protocolFormXmlDataId
	 * @param listPath
	 * @param elementXml
	 * @return
	 */
	@RequestMapping(value = "/ajax/protocols/{protocolId}/protocol-forms/{protocolFormId}/protocol-form-xml-datas/{protocolFormXmlDataId}/xml-elements/add-by-xpath")
	public @ResponseBody
	String addXmlElementToProtocolXmlDataByXpath(
			@PathVariable("protocolFormXmlDataId") long protocolFormXmlDataId,
			@RequestParam("parentElementXPath") String parentElementXPath,
			@RequestParam("elementXml") String elementXml) {

		ProtocolFormXmlData protocolFormXmlData = protocolFormXmlDataDao
				.findById(protocolFormXmlDataId);
		
		ProtocolForm protocolForm = protocolFormXmlData.getProtocolForm();

		String originalXml = protocolFormXmlData.getXmlData();

		Map<String, Object> resultMap = null;
		
		String processedElementXml = "";
		
		logger.debug("parentElementXPath: " + parentElementXPath);

		try {
			//String excapedXml = xmlProcessor.escapeText(elementXml);
			resultMap = xmlProcessor.addSubElementToElementIdentifiedByXPath(
					parentElementXPath, originalXml, elementXml, true);

			Assert.notEmpty(resultMap);

			Assert.notNull(resultMap.get("finalXml"));
			Assert.notNull(resultMap.get("elementXml"));
			Assert.notNull(resultMap.get("elementId"));
			
			protocolFormXmlData.setXmlData(resultMap.get("finalXml").toString());
			
			processedElementXml = resultMap.get("elementXml").toString();
			logger.debug(processedElementXml);
			protocolFormXmlData = protocolFormXmlDataDao
					.saveOrUpdate(protocolFormXmlData);

			protocolForm = protocolMetaDataXmlService.updateProtocolFormMetaDataXml(protocolFormXmlData, null);
			
			if (toUpdateProtocolMetaDataFormTypeLst.contains(protocolForm.getProtocolFormType())){
				protocolMetaDataXmlService
				.updateProtocolMetaDataXml(protocolForm);
			}
			
			//finalXml = xmlProcessor.escapeText(protocolFormXmlData.getXmlData());
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

		return processedElementXml;
	}

	@RequestMapping(value = "/ajax/protocols/{protocolId}/protocol-forms/{protcoolFormId}/protocol-form-xml-datas/{protocolFormXmlDataId}/xml-elements/delete")
	public @ResponseBody
	String deleteItemFromProtocolXmlData(
			@PathVariable("protocolFormXmlDataId") long protocolFormXmlDataId,
			@RequestParam("listPath") String listPath,
			@RequestParam("elementId") String elementId)
			throws XPathExpressionException {
		
		ProtocolFormXmlData protocolFormXmlData = protocolFormXmlDataDao
				.findById(protocolFormXmlDataId);
		
		ProtocolForm protocolForm = protocolFormXmlData.getProtocolForm();

		String originalXml = protocolFormXmlData.getXmlData();

		Map<String, Object> resultMap = null;

		try {
			
			resultMap = xmlProcessor.deleteElementByPathById(listPath,
					originalXml, elementId);
			
			if(listPath.contains("/staffs/staff")){
				objectAclService.deleteObjectAclByXPathAndElementIdAndXml(Protocol.class, protocolForm.getProtocol().getId(), listPath, elementId, originalXml);
			}
			Assert.notEmpty(resultMap);

			Assert.notNull(resultMap.get("finalXml"));
			Assert.notNull(resultMap.get("isDeleted"));
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		protocolFormXmlData.setXmlData(resultMap.get("finalXml").toString());

		logger.debug("finalXml: " + resultMap.get("finalXml").toString());
		protocolFormXmlData = protocolFormXmlDataDao
				.saveOrUpdate(protocolFormXmlData);

		protocolForm = protocolMetaDataXmlService.updateProtocolFormMetaDataXml(protocolFormXmlData, null);

		if (toUpdateProtocolMetaDataFormTypeLst.contains(protocolForm.getProtocolFormType())){
			protocolMetaDataXmlService
			.updateProtocolMetaDataXml(protocolForm);
		}
		
		
		return XMLResponseHelper.xmlResult(resultMap.get("isDeleted"));
	}
	
	@RequestMapping(value = "/ajax/protocols/{protocolId}/protocol-forms/{protcoolFormId}/protocol-form-xml-datas/{protocolFormXmlDataId}/xml-elements/delete-all")
	public @ResponseBody
	String deleteItemFromProtocolXmlData(
			@PathVariable("protocolFormXmlDataId") long protocolFormXmlDataId,
			@RequestParam("listPath") String listPath)
			throws XPathExpressionException {
		
		ProtocolFormXmlData protocolFormXmlData = protocolFormXmlDataDao
				.findById(protocolFormXmlDataId);
		
		ProtocolForm protocolForm = protocolFormXmlData.getProtocolForm();

		String originalXml = protocolFormXmlData.getXmlData();

		Map<String, Object> resultMap = null;

		try {
			
			resultMap = xmlProcessor.deleteElementByPath(listPath, originalXml);
			
			Assert.notEmpty(resultMap);

			Assert.notNull(resultMap.get("finalXml"));
			Assert.notNull(resultMap.get("isDeleted"));
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		protocolFormXmlData.setXmlData(resultMap.get("finalXml").toString());

		logger.debug("finalXml: " + resultMap.get("finalXml").toString());
		protocolFormXmlData = protocolFormXmlDataDao
				.saveOrUpdate(protocolFormXmlData);

		protocolForm = protocolMetaDataXmlService.updateProtocolFormMetaDataXml(protocolFormXmlData, null);

		if (toUpdateProtocolMetaDataFormTypeLst.contains(protocolForm.getProtocolFormType())){
			protocolMetaDataXmlService
			.updateProtocolMetaDataXml(protocolForm);
		}	
		
		return XMLResponseHelper.xmlResult(resultMap.get("isDeleted"));
	}

	@RequestMapping(value = "/ajax/protocols/{protocolId}/protocol-forms/{protcoolFormId}/protocol-form-xml-datas/{protocolFormXmlDataId}/xml-elements/get")
	public @ResponseBody
	String getItemFromProtocolXmlData(
			@PathVariable("protocolFormXmlDataId") long protocolFormXmlDataId,
			@RequestParam("listPath") String listPath,
			@RequestParam("elementId") String elementId) {

		ProtocolFormXmlData protocolFormXmlData = protocolFormXmlDataDao
				.findById(protocolFormXmlDataId);

		String originalXml = protocolFormXmlData.getXmlData();

		String elementXml = null;

		try {
			elementXml = xmlProcessor.getElementByPathById(listPath,
					originalXml, elementId);
			Assert.hasText(elementXml);

		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return elementXml;
	}

	@RequestMapping(value = "/ajax/protocols/{protocolId}/protocol-forms/{protcoolFormId}/protocol-form-xml-datas/{protocolFormXmlDataId}/xml-elements/update")
	public @ResponseBody
	String updateItemInProtocolXmlData(
			@PathVariable("protocolFormXmlDataId") long protocolFormXmlDataId,
			@RequestParam("listPath") String listPath,
			@RequestParam("elementId") String elementId,
			@RequestParam("elementXml") String elementXml) {

		ProtocolFormXmlData protocolFormXmlData = protocolFormXmlDataDao
				.findById(protocolFormXmlDataId);

		String originalXml = protocolFormXmlData.getXmlData();

		ProtocolForm protocolForm = protocolFormXmlData.getProtocolForm();
		
		String finalXml = "";

		Map<String, Object> resultMap = null;

		try {
			//String excapedXml = xmlProcessor.escapeText(elementXml);
			resultMap = xmlProcessor.updateElementByPathById(listPath,
					originalXml, elementId, elementXml);
			Assert.notEmpty(resultMap);

			Assert.notNull(resultMap.get("finalXml"));
			Assert.notNull(resultMap.get("elementXml"));
			Assert.notNull(resultMap.get("elementId"));
			
			protocolFormXmlData.setXmlData(resultMap.get("finalXml").toString());

			protocolFormXmlData = protocolFormXmlDataDao
					.saveOrUpdate(protocolFormXmlData);

			//protocolForm.setMetaDataXml(protocolFormXmlData.toString());
			//protocolForm = protocolFormDao.saveOrUpdate(protocolForm);
			protocolForm = protocolMetaDataXmlService.updateProtocolFormMetaDataXml(protocolFormXmlData, null);
			if (toUpdateProtocolMetaDataFormTypeLst.contains(protocolForm.getProtocolFormType())){
				protocolMetaDataXmlService
				.updateProtocolMetaDataXml(protocolForm);
			}
			
			if(elementXml.contains("</staff>")){
				logger.debug("update user acl based on staff xml");
				objectAclService.updateObjectAclByStaffXml(Protocol.class, protocolForm.getProtocol().getId(), elementXml);
			}
			
			//finalXml = xmlProcessor.escapeText(protocolFormXmlData.getXmlData());
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return finalXml;

	}

	@RequestMapping(value = "/ajax/protocols/{protocolId}/protocol-forms/{protcoolFormId}/protocol-form-xml-datas/{protocolFormXmlDataId}/xml-elements/list")
	public @ResponseBody
	String listElementsInProtocolXmlData(
			@PathVariable("protocolFormXmlDataId") long protocolFormXmlDataId,
			@RequestParam("listPath") String listPath) {

		ProtocolFormXmlData protocolFormXmlData = protocolFormXmlDataDao
				.findById(protocolFormXmlDataId);

		String originalXml = protocolFormXmlData.getXmlData();

		String resultXml = null;

		try {
			resultXml = xmlProcessor.listElementsByPath(listPath, originalXml,
					true);
			Assert.hasText(resultXml);
			
			//logger.info(resultXml);
			//resultXml = xmlProcessor.escapeText(resultXml);
			//logger.info(resultXml);
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return resultXml;
	}
	
	@RequestMapping(value = "/ajax/protocols/{protocolId}/protocol-forms/{protcolFormId}/protocol-form-xml-datas/{protocolFormXmlDataId}/xml-elements/listValues")
	public @ResponseBody
	Map<String, List<String>> listElementsValuesInProtocolXmlData(
			@PathVariable("protocolFormXmlDataId") long protocolFormXmlDataId,
			@RequestParam("listPaths[]") Set<String> listPaths) {

		ProtocolFormXmlData protocolFormXmlData = protocolFormXmlDataDao
				.findById(protocolFormXmlDataId);

		String originalXml = protocolFormXmlData.getXmlData();

		Map<String, List<String>> results = null;
		
		try {
			results = xmlProcessor.listElementStringValuesByPaths(listPaths,
					originalXml);

		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return results;

	}

	@Autowired(required = true)
	public void setXmlProcessor(XmlProcessor xmlProcessor) {
		this.xmlProcessor = xmlProcessor;
	}

	public XmlProcessor getXmlProcessor() {
		return xmlProcessor;
	}

	@Autowired(required = true)
	public void setProtocolFormXmlDataDao(
			ProtocolFormXmlDataDao protocolFormXmlDataDao) {
		this.protocolFormXmlDataDao = protocolFormXmlDataDao;
	}

	public ProtocolFormXmlDataDao getProtocolFormXmlDataDao() {
		return protocolFormXmlDataDao;
	}

	@Autowired(required = true)
	public void setProtocolFormDao(ProtocolFormDao protocolFormDao) {
		this.protocolFormDao = protocolFormDao;
	}

	public ProtocolFormDao getProtocolFormDao() {
		return protocolFormDao;
	}

	@Autowired(required = true)
	public void setProtocolDao(ProtocolDao protocolDao) {
		this.protocolDao = protocolDao;
	}

	public ProtocolDao getProtocolDao() {
		return protocolDao;
	}

	@Autowired(required = true)
	public void setProtocolMetaDataXmlService(
			ProtocolMetaDataXmlService protocolMetaDataXmlService) {
		this.protocolMetaDataXmlService = protocolMetaDataXmlService;
	}

	public ProtocolMetaDataXmlService getProtocolMetaDataXmlService() {
		return protocolMetaDataXmlService;
	}
	

	public ObjectAclService getObjectAclService() {
		return objectAclService;
	}

	@Autowired(required = true)
	public void setObjectAclService(ObjectAclService objectAclService) {
		this.objectAclService = objectAclService;
	}

	public ProtocolFormService getProtocolFormService() {
		return protocolFormService;
	}

	@Autowired(required=true)
	public void setProtocolFormService(ProtocolFormService protocolFormService) {
		this.protocolFormService = protocolFormService;
	}

	public ProtocolTrackService getProtocolTrackService() {
		return protocolTrackService;
	}
	
	@Autowired(required=true)
	public void setProtocolTrackService(ProtocolTrackService protocolTrackService) {
		this.protocolTrackService = protocolTrackService;
	}

	public AuditService getAuditService() {
		return auditService;
	}
	
	@Autowired(required=true)
	public void setAuditService(AuditService auditService) {
		this.auditService = auditService;
	}

	public BusinessObjectStatusHelperContainer getBusinessObjectStatusHelperContainer() {
		return businessObjectStatusHelperContainer;
	}
	
	@Autowired(required=true)
	public void setBusinessObjectStatusHelperContainer(
			BusinessObjectStatusHelperContainer businessObjectStatusHelperContainer) {
		this.businessObjectStatusHelperContainer = businessObjectStatusHelperContainer;
	}

	public RelationService getRelationService() {
		return relationService;
	}
	
	@Autowired(required=true)
	public void setRelationService(RelationService relationService) {
		this.relationService = relationService;
	}
}
