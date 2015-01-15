package edu.uams.clara.webapp.protocol.web.protocolform.ajax;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.xml.transform.Source;
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
import org.xml.sax.SAXException;

import com.google.common.collect.Maps;

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
import edu.uams.clara.webapp.protocol.dao.irb.AgendaItemDao;
import edu.uams.clara.webapp.protocol.dao.protocolform.ProtocolFormDao;
import edu.uams.clara.webapp.protocol.dao.protocolform.ProtocolFormXmlDataDao;
import edu.uams.clara.webapp.protocol.domain.Protocol;
import edu.uams.clara.webapp.protocol.domain.irb.AgendaItem;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolForm;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolFormXmlData;
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
	
	private AgendaItemDao agendaItemDao;

	private ProtocolMetaDataXmlService protocolMetaDataXmlService;
	
	private ObjectAclService objectAclService;
	
	private RelationService relationService;
	
	private AuditService auditService;
	
	private ProtocolFormService protocolFormService;
	
	private ProtocolTrackService protocolTrackService;

	private XmlProcessor xmlProcessor;
	
	private BusinessObjectStatusHelperContainer businessObjectStatusHelperContainer;
	
	/*
	private List<ProtocolFormType> toUpdateProtocolMetaDataFormTypeLst = Lists.newArrayList();{
		toUpdateProtocolMetaDataFormTypeLst.add(ProtocolFormType.NEW_SUBMISSION);
		toUpdateProtocolMetaDataFormTypeLst.add(ProtocolFormType.EMERGENCY_USE);
		toUpdateProtocolMetaDataFormTypeLst.add(ProtocolFormType.HUMAN_SUBJECT_RESEARCH_DETERMINATION);
		toUpdateProtocolMetaDataFormTypeLst.add(ProtocolFormType.PRIVACY_BOARD);
	}
	*/

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

			if (protocolForm.getProtocolFormType().getCanUpdateMetaData()){
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
			
			if (protocolForm.getProtocolFormType().getCanUpdateMetaData()){
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
			try {
				AgendaItem agendaItem = agendaItemDao.getLatestByProtocolFormId(protocolFormId);
				
				if (agendaItem != null) {
					return XMLResponseHelper.newErrorResponseStub("This form is on "+ DateFormatUtil.formateDateToMDY(agendaItem.getAgenda().getDate()) + " agenda.  Please remove it from agenda first!");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			try{				
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
	
	/*
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
	*/
	
	private void processXmlData(ProtocolFormXmlData protocolFormXmlData, String xmlData) {
		String protocolFormXmlDataString = protocolFormXmlData.getXmlData();
		
		if (protocolFormXmlData.getProtocolFormXmlDataType().equals(ProtocolFormXmlDataType.PROTOCOL) || protocolFormXmlData.getProtocolFormXmlDataType().equals(ProtocolFormXmlDataType.MODIFICATION)){
			String value = "";
			
			if (xmlData.contains("<responsible-department")){
				if (xmlData.contains("REP Regional Programs")){
					value = "n";
				} else {
					value = "y";
				}
				
				try{
					protocolFormXmlDataString = xmlProcessor.replaceOrAddNodeValueByPath("/protocol/need-budget-by-department", protocolFormXmlDataString, value);
					
				} catch (Exception e){
					e.printStackTrace();
				}
			}
			
			try {
				Map<String, Boolean> budgetRelatedDeterminationMap = protocolFormService.budgetRelatedDetermination(protocolFormXmlData);
				
				boolean budgetSectionEnabled = budgetRelatedDeterminationMap.get("budgetSectionEnabled");
				
				if (budgetSectionEnabled) {
					protocolFormXmlDataString = xmlProcessor.replaceOrAddNodeValueByPath("/protocol/budget-question-required", protocolFormXmlDataString, "y");
				} else {
					protocolFormXmlDataString = xmlProcessor.replaceOrAddNodeValueByPath("/protocol/budget-question-required", protocolFormXmlDataString, "n");
				}
				
				boolean budgetRequired = budgetRelatedDeterminationMap.get("budgetRequired");
				
				if (budgetSectionEnabled && budgetRequired) {
					protocolFormXmlDataString = xmlProcessor.replaceOrAddNodeValueByPath("/protocol/need-budget", protocolFormXmlDataString, "y");
				} else {
					protocolFormXmlDataString = xmlProcessor.replaceOrAddNodeValueByPath("/protocol/need-budget", protocolFormXmlDataString, "n");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		if (protocolFormXmlData.getProtocolFormXmlDataType().equals(ProtocolFormXmlDataType.CONTINUING_REVIEW)) {
			
			try {
				List<String> sponsorTypes = xmlProcessor.getAttributeValuesByPathAndAttributeName("/continuing-review/funding/funding-source", protocolFormXmlDataString, "type");
				
				if (sponsorTypes.size() > 0 && sponsorTypes.contains("External")) {
					protocolFormXmlDataString = xmlProcessor.replaceOrAddNodeValueByPath("/continuing-review/has-external-sponsor", protocolFormXmlDataString, "y");
				} else {
					protocolFormXmlDataString = xmlProcessor.replaceOrAddNodeValueByPath("/continuing-review/has-external-sponsor", protocolFormXmlDataString, "n");
				}
			} catch (Exception e) {
				//e.printStackTrace();
				
				try {
					protocolFormXmlDataString = xmlProcessor.replaceOrAddNodeValueByPath("/continuing-review/has-external-sponsor", protocolFormXmlDataString, "n");
				} catch (XPathExpressionException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (SAXException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
			
			if (xmlData.contains("<any-adverse-events>")) {
				try {
					XmlHandler xmlHandler = XmlHandlerFactory.newXmlHandler();
					
					protocolFormXmlDataString = xmlHandler.replaceOrAddNodeValueByPath("/continuing-review/study-report/any-adverse-events/extra-condition", protocolFormXmlDataString, "n");
					
					String anyInternalOrLocal = xmlHandler.getSingleStringValueByXPath(protocolFormXmlDataString, "/continuing-review/study-report/any-adverse-events");
					String accurAtFrequency = xmlHandler.getSingleStringValueByXPath(protocolFormXmlDataString, "/continuing-review/study-report/any-adverse-events/y/adverse-events-accur-at-frequency");
					String changeRisk = xmlHandler.getSingleStringValueByXPath(protocolFormXmlDataString, "/continuing-review/study-report/any-adverse-events/y/adverse-events-change-risk");
					String sponsorProvideInfo = xmlHandler.getSingleStringValueByXPath(protocolFormXmlDataString, "/continuing-review/study-report/any-adverse-events/y/sponsor-provide-information");
					
					if (anyInternalOrLocal.equals("y")) {
						if ((accurAtFrequency.equals("y") && changeRisk.equals("y")) || sponsorProvideInfo.equals("y")) {
							protocolFormXmlDataString = xmlHandler.replaceOrAddNodeValueByPath("/continuing-review/study-report/any-adverse-events/extra-condition", protocolFormXmlDataString, "y");
						}
					}
				} catch (Exception e) {
					
				}
			}
			
			if (xmlData.contains("<any-deviations>")) {
				try {
					XmlHandler xmlHandler = XmlHandlerFactory.newXmlHandler();
					
					protocolFormXmlDataString = xmlHandler.replaceOrAddNodeValueByPath("/continuing-review/study-report/any-deviations/extra-condition", protocolFormXmlDataString, "n");
					
					String anyDeviations = xmlHandler.getSingleStringValueByXPath(protocolFormXmlDataString, "/continuing-review/study-report/any-deviations");
					String accurInPattern = xmlHandler.getSingleStringValueByXPath(protocolFormXmlDataString, "/continuing-review/study-report/any-deviations/y/deviations-occur-in-pattern");
					String negativelyImpact = xmlHandler.getSingleStringValueByXPath(protocolFormXmlDataString, "/continuing-review/study-report/any-deviations/y/deviations-negatively-impact");

					if (anyDeviations.equals("y")) {
						if (accurInPattern.equals("y") || negativelyImpact.equals("y")) {
							protocolFormXmlDataString = xmlHandler.replaceOrAddNodeValueByPath("/continuing-review/study-report/any-deviations/any-deviations-extra-condition", protocolFormXmlDataString, "y");
						}
					}
				} catch (Exception e) {
					
				}
			}
			
		}
		
		protocolFormXmlData.setXmlData(protocolFormXmlDataString);
		protocolFormXmlDataDao.saveOrUpdate(protocolFormXmlData);
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
					protocolFormService.updateIRBExpensesInBudget(protocolForm, xmldata);
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

			//logger.debug(protocolFormXmlData.getProtocolFormXmlDataType()
					//.toString());

			protocolForm = protocolMetaDataXmlService.updateProtocolFormMetaDataXml(protocolFormXmlData, null);

			if (protocolForm.getProtocolFormType().getCanUpdateMetaData()){
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
				protocolFormService.updateIRBExpensesInBudget(protocolForm, protocolFormXmlData.getXmlData());
			}
			
			processXmlData(protocolFormXmlData, xmldata);
			
			logger.debug("elementXml: " + xmldata);
			if(xmldata.contains("</staffs>")){
				logger.debug("update user acl based on staff xml");
				objectAclService.updateObjectAclByStaffXml(Protocol.class, protocolForm.getProtocol().getId(), xmldata, true);
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

			if (protocolForm.getProtocolFormType().getCanUpdateMetaData()){
				protocolMetaDataXmlService
				.updateProtocolMetaDataXml(protocolForm);
			}

			if(elementXml.contains("</staff>")){
				logger.debug("update user acl based on staff xml");
				objectAclService.updateObjectAclByStaffXml(Protocol.class, protocolForm.getProtocol().getId(), elementXml, true);
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
			
			if (protocolForm.getProtocolFormType().getCanUpdateMetaData()){
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

		if (protocolForm.getProtocolFormType().getCanUpdateMetaData()){
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

		if (protocolForm.getProtocolFormType().getCanUpdateMetaData()){
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
			if (protocolForm.getProtocolFormType().getCanUpdateMetaData()){
				protocolMetaDataXmlService
				.updateProtocolMetaDataXml(protocolForm);
			}
			
			if(elementXml.contains("</staff>")){
				logger.debug("update user acl based on staff xml");
				objectAclService.updateObjectAclByStaffXml(Protocol.class, protocolForm.getProtocol().getId(), elementXml, true);
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

	public AgendaItemDao getAgendaItemDao() {
		return agendaItemDao;
	}
	
	@Autowired(required=true)
	public void setAgendaItemDao(AgendaItemDao agendaItemDao) {
		this.agendaItemDao = agendaItemDao;
	}
}
