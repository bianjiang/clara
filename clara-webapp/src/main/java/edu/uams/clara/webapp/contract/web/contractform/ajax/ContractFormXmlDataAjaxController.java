package edu.uams.clara.webapp.contract.web.contractform.ajax;

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

import edu.uams.clara.webapp.common.businesslogic.BusinessObjectStatusHelperContainer;
import edu.uams.clara.webapp.common.dao.usercontext.UserDao;
import edu.uams.clara.webapp.common.domain.history.Track;
import edu.uams.clara.webapp.common.domain.relation.RelatedObject;
import edu.uams.clara.webapp.common.domain.usercontext.User;
import edu.uams.clara.webapp.common.domain.usercontext.enums.Committee;
import edu.uams.clara.webapp.common.domain.usercontext.enums.Permission;
import edu.uams.clara.webapp.common.security.ObjectAclService;
import edu.uams.clara.webapp.common.service.audit.AuditService;
import edu.uams.clara.webapp.common.service.form.FormService;
import edu.uams.clara.webapp.common.service.relation.RelationService;
import edu.uams.clara.webapp.common.util.DateFormatUtil;
import edu.uams.clara.webapp.common.util.XMLResponseHelper;
import edu.uams.clara.webapp.common.util.response.JsonResponse;
import edu.uams.clara.webapp.contract.dao.ContractDao;
import edu.uams.clara.webapp.contract.dao.contractform.ContractFormDao;
import edu.uams.clara.webapp.contract.dao.contractform.ContractFormXmlDataDao;
import edu.uams.clara.webapp.contract.domain.Contract;
import edu.uams.clara.webapp.contract.domain.contractform.ContractForm;
import edu.uams.clara.webapp.contract.domain.contractform.ContractFormXmlData;
import edu.uams.clara.webapp.contract.domain.contractform.enums.ContractFormType;
import edu.uams.clara.webapp.contract.domain.contractform.enums.ContractFormXmlDataType;
import edu.uams.clara.webapp.contract.service.ContractMetaDataXmlService;
import edu.uams.clara.webapp.contract.service.ContractService;
import edu.uams.clara.webapp.contract.service.history.ContractTrackService;
import edu.uams.clara.webapp.protocol.dao.ProtocolDao;
import edu.uams.clara.webapp.protocol.domain.Protocol;
import edu.uams.clara.webapp.xml.processor.XmlProcessor;

/**
 * this controller should return xml string and set the content type as
 * text/xml...
 */
@Controller
public class ContractFormXmlDataAjaxController {

	@SuppressWarnings("unused")
	private final static Logger logger = LoggerFactory
			.getLogger(ContractFormXmlDataAjaxController.class);

	private ContractDao contractDao;

	private ContractFormDao contractFormDao;

	private ContractFormXmlDataDao contractFormXmlDataDao;

	private ContractMetaDataXmlService contractMetaDataXmlService;
	
	private FormService formService;
	
	private ObjectAclService objectAclService;
	
	private AuditService auditService;
	
	private ProtocolDao protocolDao;
	
	private UserDao userDao;

	private XmlProcessor xmlProcessor;
	
	private RelationService relationService;
	
	private ContractService contractService;
	
	private ContractTrackService contractTrackService;
	
	private BusinessObjectStatusHelperContainer businessObjectStatusHelperContainer;

	@RequestMapping(value = "/ajax/contracts/{contractId}/contract-forms/{contractFormId}/{contractFormXmlDataType}/update", method = RequestMethod.POST)
	public @ResponseBody
	String updateContractFormXmlDataByContractFormXmlDataType(
			@PathVariable("contractFormId") long contractFormId,
			@PathVariable("contractFormXmlDataType") ContractFormXmlDataType contractFormXmlDataType,
			@RequestParam("pagefragment") String xmldata) {

		ContractForm contractForm = contractFormDao.findById(contractFormId);

		ContractFormXmlData contractFormXmlData = contractForm
				.getTypedContractFormXmlDatas().get(contractFormXmlDataType);

		String mergedXmlString = contractFormXmlData.getXmlData();

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

			contractFormXmlData.setXmlData(mergedXmlString);

			contractFormXmlData = contractFormXmlDataDao
					.saveOrUpdate(contractFormXmlData);

			contractForm = contractMetaDataXmlService.updateContractFormMetaDataXml(contractFormXmlData, null);
			contractMetaDataXmlService
					.updateContractMetaDataXml(contractForm);
		}

		return XMLResponseHelper.xmlResult(Boolean.TRUE);

	}
	
	@RequestMapping(value = "/ajax/contracts/{contractId}/contract-forms/{contractFormId}/delete", method = RequestMethod.GET, produces="application/xml")
	public @ResponseBody
	Source deleteContractForm(@PathVariable("contractId") long contractId,
			@PathVariable("contractFormId") long contractFormId) {
		User u = (User) SecurityContextHolder.getContext().getAuthentication()
				.getPrincipal();

		ContractForm contractForm = contractFormDao.findById(contractFormId);
		Contract contract = contractForm.getContract();
		
		try{			
			ContractFormXmlData contractFormXmlData = contractForm.getTypedContractFormXmlDatas().get(contractForm.getContractFormType().getDefaultContractFormXmlDataType());
			
			if (contractForm.getContractFormType().equals(ContractFormType.AMENDMENT)){
				if (contractFormXmlData != null){
					contractFormXmlData.setRetired(true);
					contractFormXmlDataDao.saveOrUpdate(contractFormXmlData);
				}
				
				contractForm.setRetired(true);
				contractFormDao.saveOrUpdate(contractForm);
				
				auditService.auditEvent("CONTRACT_FORM_DELETED",
						u.getPerson().getFullname() + " has deleted contract form - formId:" + contractFormId + " formType:" + contractForm.getContractFormType().getDescription());
			} else {
				List<ContractForm> amendmentFormLst = contractFormDao.listContractFormsByContractIdAndContractFormType(contractId, ContractFormType.AMENDMENT);
				
				if (amendmentFormLst != null && !amendmentFormLst.isEmpty()){
					return XMLResponseHelper.newErrorResponseStub("Cannot delete the New Contract form it has one or more Amendments!");
				} else {
					if (contractFormXmlData != null){
						contractFormXmlData.setRetired(true);
						contractFormXmlDataDao.saveOrUpdate(contractFormXmlData);
					}
					
					contractForm.setRetired(true);
					contractFormDao.saveOrUpdate(contractForm);
					
					/*
					 * Need to delete all relations, eg. protocols if contract is deleted
					 * */
					try {
						List<RelatedObject> relatedObjects = relationService.getRelationsByIdAndType(contractId, "contract");
						
						if (relatedObjects.size() > 0) {
							for (RelatedObject ro : relatedObjects) {
								relationService.removeRelation(ro);
							}
						}
					} catch (Exception e) {
						//don't care ...
					}
					
					contract.setRetired(true);
					contractDao.saveOrUpdate(contract);
					
					auditService.auditEvent("CONTRACT_DELETED",
							u.getPerson().getFullname() + " has deleted contract - contractId:"+ contractId);
				}
			}
			
			Track track = contractTrackService.getOrCreateTrack("CONTRACT",
					contractId);

			Document logsDoc = contractTrackService.getLogsDocument(track);

			Element logEl = logsDoc.createElement("log");
			
			Date now = new Date();
			
			String logId = UUID.randomUUID().toString();
			
			logEl.setAttribute("id", logId);
			logEl.setAttribute("parent-id", logId);
			logEl.setAttribute("action-user-id", String.valueOf(u.getId()));
			logEl.setAttribute("actor", u.getPerson().getFullname());
			logEl.setAttribute("date-time", DateFormatUtil.formateDate(now));
			logEl.setAttribute("event-type", "CONTRACT_FORM_DELETE");
			logEl.setAttribute("form-id", String.valueOf(contractFormId));
			logEl.setAttribute("parent-form-id", String.valueOf(contractForm.getParent().getId()));
			logEl.setAttribute("form-type", contractForm.getFormType());
			logEl.setAttribute("log-type", "ACTION");
			logEl.setAttribute("timestamp", String.valueOf(now.getTime()));

			String message = ""+ contractForm.getContractFormType().getDescription() +" Form has been deleted by "
					+ u.getPerson().getFullname() + "";

			logEl.setTextContent(message);

			logsDoc.getDocumentElement().appendChild(logEl);

			track = contractTrackService.updateTrack(track, logsDoc);
			
		} catch (Exception e){
			e.printStackTrace();
			return XMLResponseHelper.newErrorResponseStub("Failed to delete form!");
		}
	
		return XMLResponseHelper.newSuccessResponseStube("");

	}
	
	@RequestMapping(value = "/ajax/contracts/{contractId}/contract-forms/{contractFormId}/cancel", method = RequestMethod.POST, produces="application/xml")
	public @ResponseBody
	Source cancelContractForm(
			@PathVariable("contractFormId") long contractFormId,
			@RequestParam("xml") String xml) {
		ContractForm contractForm = contractFormDao.findById(contractFormId);
		
		User u = (User) SecurityContextHolder.getContext().getAuthentication()
				.getPrincipal();
		
		if (u.getAuthorities().contains(Permission.ROLE_CONTRACT_LEGAL_REVIEW)){
			try{
				xml = "<committee-review><committee type=\"CONTRACT_LEGAL_REVIEW\">" + xml + "</committee></committee-review>";
				businessObjectStatusHelperContainer.getBusinessObjectStatusHelper(contractForm.getFormType().toString()).triggerAction(contractForm, Committee.CONTRACT_LEGAL_REVIEW, u, "CANCEL_CONTRACT_FORM", "", xml);
			} catch (Exception e){
				e.printStackTrace();
				return XMLResponseHelper.newErrorResponseStub("Failed to cancel contract form!");
			}
		} else {
			return XMLResponseHelper.newErrorResponseStub("You don't have the right to cacnel contract form!");
		}
		
		return XMLResponseHelper.newSuccessResponseStube("Succed...");
	}
	
	@RequestMapping(value = "/ajax/contracts/{contractId}/contract-forms/{contractFormId}/contract-form-xml-datas/{contractFormXmlDataId}/get-protocol-metadata", method = RequestMethod.GET)
	public @ResponseBody String getProtocolMeta(@PathVariable("contractFormId") long contractFormId,
			@PathVariable("contractFormXmlDataId") long contractFormXmlDataId,
			@RequestParam("protocolId") long protocolId){
		Protocol protocol =  protocolDao.findById(protocolId);
		String protocolMetaDataXml = protocolDao.findById(protocolId).getMetaDataXml();
		
		ContractFormXmlData contractFormXmlData = contractFormXmlDataDao.findById(contractFormXmlDataId);
		
		try{
			contractService.pullFromProotcol(contractFormXmlData, protocol);
		} catch (Exception e){
			e.printStackTrace();
		}
		
		return protocolMetaDataXml;
	}

	@RequestMapping(value = "/ajax/contracts/{contractId}/contract-forms/{contractFormId}/contract-form-xml-datas/{contractFormXmlDataId}/update", method = RequestMethod.POST)
	public @ResponseBody
	String updateContractFormXmlDataById(@PathVariable("contractFormId") long contractFormId,
			@PathVariable("contractFormXmlDataId") long contractFormXmlDataId,
			@RequestParam("pagefragment") String xmldata) {

		logger.debug("pagefragment xmldata: " + xmldata);
		ContractForm contractForm = contractFormDao.findById(contractFormId);
		ContractFormXmlData contractFormXmlData = contractFormXmlDataDao
				.findById(contractFormXmlDataId);

		String mergedXmlString = contractFormXmlData.getXmlData();
		
		/*
		if(xmldata.contains("</protocol>")){
			logger.debug("related");
			
			//remove original
			try {
				List<String> protocolIds = xmlProcessor.listElementStringValuesByPath("/contract/protocol", mergedXmlString);
				long protocolId = (protocolIds.size() > 0)?Long.parseLong(protocolIds.get(0)):0;
				
				if(protocolId > 0){
					relationService.removeRelationByIdAndType(protocolId, contractForm.getContract().getId(), "Protocol", "Contract");
					
				}
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
			
			try {
				List<String> protocolIds = xmlProcessor.listElementStringValuesByPath("/contract/protocol", xmldata);
				long protocolId = (protocolIds.size() > 0)?Long.parseLong(protocolIds.get(0)):0;
				
				if(protocolId > 0){
					relationService.addRelationByIdAndType(protocolId, contractForm.getContract().getId(), "Protocol", "Contract");
					
				}
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
			
					
			//objectAclService.updateObjectAclByStaffXml(Protocol.class, protocolForm.getProtocol().getId(), xmldata);
		}*/

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

			contractFormXmlData.setXmlData(mergedXmlString);

			contractFormXmlData = contractFormXmlDataDao
					.saveOrUpdate(contractFormXmlData);

			// logger.debug(mergedXmlString);

			try{
				logger.debug("start trimming ...");
				contractFormXmlData = contractMetaDataXmlService.consolidateContractFormXmlData(contractFormXmlData, contractFormXmlData.getContractFormXmlDataType());
			} catch (Exception e) {
				//don't care
			}

			contractForm = contractMetaDataXmlService.updateContractFormMetaDataXml(contractFormXmlData, null);
			contractMetaDataXmlService
					.updateContractMetaDataXml(contractForm);
		}

		return XMLResponseHelper.xmlResult(Boolean.TRUE);

	}

	/**
	 * @ToDo need to add contractMetaDataXmlService.updateContractMetaDataXml,
	 *       since PI is part of the contract.metaData, and adding PI is done
	 *       here.. There will be a problem of removing PI... the system is not
	 *       smart enough yet to remove PI from metadata xml if the PI is
	 *       removed from contract ...
	 * @param contractFormXmlDataId
	 * @param listPath
	 * @param elementXml
	 * @return
	 */
	@RequestMapping(value = "/ajax/contracts/{contractId}/contract-forms/{contractFormId}/contract-form-xml-datas/{contractFormXmlDataId}/xml-elements/add")
	public @ResponseBody
	String addXmlElementToContractXmlData(
			@PathVariable("contractFormXmlDataId") long contractFormXmlDataId,
			@RequestParam("listPath") String listPath,
			@RequestParam("elementXml") String elementXml) {

		ContractFormXmlData contractFormXmlData = contractFormXmlDataDao
				.findById(contractFormXmlDataId);
		
		ContractForm contractForm = contractFormXmlData.getContractForm();

		String originalXml = contractFormXmlData.getXmlData();

		Map<String, Object> resultMap = null;

		try {
			resultMap = xmlProcessor.addElementByPath(listPath, originalXml,
					elementXml, true);
			Assert.notEmpty(resultMap);

			Assert.notNull(resultMap.get("finalXml"));
			Assert.notNull(resultMap.get("elementXml"));
			Assert.notNull(resultMap.get("elementId"));
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		contractFormXmlData.setXmlData(resultMap.get("finalXml").toString());

		contractFormXmlData = contractFormXmlDataDao
				.saveOrUpdate(contractFormXmlData);
		
		contractForm = contractMetaDataXmlService.updateContractFormMetaDataXml(contractFormXmlData, null);
		contractMetaDataXmlService
				.updateContractMetaDataXml(contractForm);

		if(elementXml.contains("</staff>")){
			logger.debug("update user acl based on staff xml");
			objectAclService.updateObjectAclByStaffXml(Contract.class, contractForm.getContract().getId(), elementXml);
		}

		return resultMap.get("elementXml").toString();
	}

	/**
	 * This funciton is used to add sub-elements to a specified (e.g. a element
	 * with id) element the function first use the xpath to find the parent
	 * element, and then add the elementXml, return the id of the newly added
	 * element
	 * 
	 * @TODO
	 * @param contractFormXmlDataId
	 * @param listPath
	 * @param elementXml
	 * @return
	 */
	@RequestMapping(value = "/ajax/contracts/{contractId}/contract-forms/{contractFormId}/contract-form-xml-datas/{contractFormXmlDataId}/xml-elements/add-by-xpath")
	public @ResponseBody
	String addXmlElementToContractXmlDataByXpath(
			@PathVariable("contractFormXmlDataId") long contractFormXmlDataId,
			@RequestParam("parentElementXPath") String parentElementXPath,
			@RequestParam("elementXml") String elementXml) {

		ContractFormXmlData contractFormXmlData = contractFormXmlDataDao
				.findById(contractFormXmlDataId);
		
		ContractForm contractForm = contractFormXmlData.getContractForm();

		String originalXml = contractFormXmlData.getXmlData();

		Map<String, Object> resultMap = null;
		
		logger.debug("parentElementXPath: " + parentElementXPath);

		try {

			resultMap = xmlProcessor.addSubElementToElementIdentifiedByXPath(
					parentElementXPath, originalXml, elementXml, true);

			Assert.notEmpty(resultMap);

			Assert.notNull(resultMap.get("finalXml"));
			Assert.notNull(resultMap.get("elementXml"));
			Assert.notNull(resultMap.get("elementId"));
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

		contractFormXmlData.setXmlData(resultMap.get("finalXml").toString());

		contractFormXmlData = contractFormXmlDataDao
				.saveOrUpdate(contractFormXmlData);

		contractForm = contractMetaDataXmlService.updateContractFormMetaDataXml(contractFormXmlData, null);
		contractMetaDataXmlService
				.updateContractMetaDataXml(contractForm);

		return resultMap.get("elementXml").toString();
	}

	@RequestMapping(value = "/ajax/contracts/{contractId}/contract-forms/{protcoolFormId}/contract-form-xml-datas/{contractFormXmlDataId}/xml-elements/delete")
	public @ResponseBody
	String deleteItemFromContractXmlData(
			@PathVariable("contractFormXmlDataId") long contractFormXmlDataId,
			@RequestParam("listPath") String listPath,
			@RequestParam("elementId") String elementId)
			throws XPathExpressionException {
		
		ContractFormXmlData contractFormXmlData = contractFormXmlDataDao
				.findById(contractFormXmlDataId);
		
		ContractForm contractForm = contractFormXmlData.getContractForm();

		String originalXml = contractFormXmlData.getXmlData();

		Map<String, Object> resultMap = null;

		try {
			objectAclService.deleteObjectAclByXPathAndElementIdAndXml(Contract.class, contractForm.getContract().getId(), listPath, elementId, originalXml);

			resultMap = xmlProcessor.deleteElementByPathById(listPath,
					originalXml, elementId);
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

		contractFormXmlData.setXmlData(resultMap.get("finalXml").toString());

		logger.debug("finalXml: " + resultMap.get("finalXml").toString());
		contractFormXmlData = contractFormXmlDataDao
				.saveOrUpdate(contractFormXmlData);

		contractForm = contractMetaDataXmlService.updateContractFormMetaDataXml(contractFormXmlData, null);
		contractMetaDataXmlService
				.updateContractMetaDataXml(contractForm);

		return XMLResponseHelper.xmlResult(resultMap.get("isDeleted"));
	}

	@RequestMapping(value = "/ajax/contracts/{contractId}/contract-forms/{protcoolFormId}/contract-form-xml-datas/{contractFormXmlDataId}/xml-elements/get")
	public @ResponseBody
	String getItemFromContractXmlData(
			@PathVariable("contractFormXmlDataId") long contractFormXmlDataId,
			@RequestParam("listPath") String listPath,
			@RequestParam("elementId") String elementId) {

		ContractFormXmlData contractFormXmlData = contractFormXmlDataDao
				.findById(contractFormXmlDataId);

		String originalXml = contractFormXmlData.getXmlData();

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

	@RequestMapping(value = "/ajax/contracts/{contractId}/contract-forms/{protcoolFormId}/contract-form-xml-datas/{contractFormXmlDataId}/xml-elements/update")
	public @ResponseBody
	String updateItemInContractXmlData(
			@PathVariable("contractFormXmlDataId") long contractFormXmlDataId,
			@RequestParam("listPath") String listPath,
			@RequestParam("elementId") String elementId,
			@RequestParam("elementXml") String elementXml) {

		ContractFormXmlData contractFormXmlData = contractFormXmlDataDao
				.findById(contractFormXmlDataId);

		String originalXml = contractFormXmlData.getXmlData();

		ContractForm contractForm = contractFormXmlData.getContractForm();

		Map<String, Object> resultMap = null;

		try {
			resultMap = xmlProcessor.updateElementByPathById(listPath,
					originalXml, elementId, elementXml);
			Assert.notEmpty(resultMap);

			Assert.notNull(resultMap.get("finalXml"));
			Assert.notNull(resultMap.get("elementXml"));
			Assert.notNull(resultMap.get("elementId"));
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		contractFormXmlData.setXmlData(resultMap.get("finalXml").toString());

		contractFormXmlData = contractFormXmlDataDao
				.saveOrUpdate(contractFormXmlData);

		//contractForm.setMetaDataXml(contractFormXmlData.toString());
		//contractForm = contractFormDao.saveOrUpdate(contractForm);
		contractForm = contractMetaDataXmlService.updateContractFormMetaDataXml(contractFormXmlData, null);
		contractMetaDataXmlService
				.updateContractMetaDataXml(contractForm);

		return resultMap.get("elementXml").toString();

	}

	@RequestMapping(value = "/ajax/contracts/{contractId}/contract-forms/{protcoolFormId}/contract-form-xml-datas/{contractFormXmlDataId}/xml-elements/list")
	public @ResponseBody
	String listElementsInContractXmlData(
			@PathVariable("contractFormXmlDataId") long contractFormXmlDataId,
			@RequestParam("listPath") String listPath) {

		ContractFormXmlData contractFormXmlData = contractFormXmlDataDao
				.findById(contractFormXmlDataId);

		String originalXml = contractFormXmlData.getXmlData();

		String resultXml = null;

		try {
			resultXml = xmlProcessor.listElementsByPath(listPath, originalXml,
					true);
			Assert.hasText(resultXml);

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

	@RequestMapping(value = "/ajax/contracts/{contractId}/contract-forms/{protcolFormId}/contract-form-xml-datas/{contractFormXmlDataId}/xml-elements/listValues")
	public @ResponseBody
	Map<String, List<String>> listElementsValuesInContractXmlData(
			@PathVariable("contractFormXmlDataId") long contractFormXmlDataId,
			@RequestParam("listPaths[]") Set<String> listPaths) {

		ContractFormXmlData contractFormXmlData = contractFormXmlDataDao
				.findById(contractFormXmlDataId);

		String originalXml = contractFormXmlData.getXmlData();

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
	
	@RequestMapping(value = "/ajax/contracts/{contractId}/contract-forms/{contractFormId}/update-summary", method = RequestMethod.POST)
	public @ResponseBody JsonResponse updateContractSummary(@PathVariable("contractId") long contractId,
			@PathVariable("contractFormId") long contractFormId,
			@RequestParam("path") String path,
			@RequestParam("value") String value,
			@RequestParam("userId") long userId){
		ContractForm contractForm = contractFormDao.findById(contractFormId);
		String contractFormMetaXml = contractForm.getMetaDataXml();
		
		User currentUser = userDao.findById(userId);
		
		Date now = new Date();
		
		try{
			contractFormMetaXml = xmlProcessor.replaceOrAddNodeValueByPath(path, contractFormMetaXml, value);
			
			contractForm.setMetaDataXml(contractFormMetaXml);
			contractFormDao.saveOrUpdate(contractForm);
			
			Track track = contractTrackService.getOrCreateTrack("CONTRACT",
					contractId);

			Document logsDoc = contractTrackService.getLogsDocument(track);

			Element logEl = logsDoc.createElement("log");
			
			String logId = UUID.randomUUID().toString();
			
			logEl.setAttribute("id", logId);
			logEl.setAttribute("parent-id", logId);
			logEl.setAttribute("action-user-id", String.valueOf(userId));
			logEl.setAttribute("actor", currentUser.getPerson().getFullname());
			logEl.setAttribute("date-time", DateFormatUtil.formateDate(now));
			logEl.setAttribute("event-type", "CONTRACT_FORM_SUMMARY_UPDATE");
			logEl.setAttribute("form-id", "0");
			logEl.setAttribute("parent-form-id", "0");
			logEl.setAttribute("form-type", "CONTRACT");
			logEl.setAttribute("log-type", "ACTION");
			logEl.setAttribute("timestamp", String.valueOf(now.getTime()));

			String message = "Contract form Summary has been updated by "
					+ currentUser.getPerson().getFullname() + "";

			logEl.setTextContent(message);

			logsDoc.getDocumentElement().appendChild(logEl);

			track = contractTrackService.updateTrack(track, logsDoc);
			
			auditService.auditEvent("CONTRACT_FORM_SUMMARY_UPDATED",
					currentUser.getPerson().getFullname() + " has updated answer of " + path + " to " + value);
			
			
			return new JsonResponse(false, "", "", false, null);
		} catch (Exception e){
			e.printStackTrace();
			return new JsonResponse(true, "Failed to update contract summary!", "", false, null);
		}

	}

	@Autowired(required = true)
	public void setXmlProcessor(XmlProcessor xmlProcessor) {
		this.xmlProcessor = xmlProcessor;
	}

	public XmlProcessor getXmlProcessor() {
		return xmlProcessor;
	}

	@Autowired(required = true)
	public void setContractFormXmlDataDao(
			ContractFormXmlDataDao contractFormXmlDataDao) {
		this.contractFormXmlDataDao = contractFormXmlDataDao;
	}

	public ContractFormXmlDataDao getContractFormXmlDataDao() {
		return contractFormXmlDataDao;
	}

	@Autowired(required = true)
	public void setContractFormDao(ContractFormDao contractFormDao) {
		this.contractFormDao = contractFormDao;
	}

	public ContractFormDao getContractFormDao() {
		return contractFormDao;
	}

	@Autowired(required = true)
	public void setContractDao(ContractDao contractDao) {
		this.contractDao = contractDao;
	}

	public ContractDao getContractDao() {
		return contractDao;
	}

	@Autowired(required = true)
	public void setContractMetaDataXmlService(
			ContractMetaDataXmlService contractMetaDataXmlService) {
		this.contractMetaDataXmlService = contractMetaDataXmlService;
	}

	public ContractMetaDataXmlService getContractMetaDataXmlService() {
		return contractMetaDataXmlService;
	}

	public ProtocolDao getProtocolDao() {
		return protocolDao;
	}
	
	@Autowired(required = true)
	public void setProtocolDao(ProtocolDao protocolDao) {
		this.protocolDao = protocolDao;
	}

	public FormService getFormService() {
		return formService;
	}
	
	@Autowired(required = true)
	public void setFormService(FormService formService) {
		this.formService = formService;
	}

	public ObjectAclService getObjectAclService() {
		return objectAclService;
	}

	@Autowired(required = true)
	public void setObjectAclService(ObjectAclService objectAclService) {
		this.objectAclService = objectAclService;
	}

	public RelationService getRelationService() {
		return relationService;
	}

	@Autowired(required = true)
	public void setRelationService(RelationService relationService) {
		this.relationService = relationService;
	}

	public BusinessObjectStatusHelperContainer getBusinessObjectStatusHelperContainer() {
		return businessObjectStatusHelperContainer;
	}
	
	@Autowired(required = true)
	public void setBusinessObjectStatusHelperContainer(
			BusinessObjectStatusHelperContainer businessObjectStatusHelperContainer) {
		this.businessObjectStatusHelperContainer = businessObjectStatusHelperContainer;
	}

	public AuditService getAuditService() {
		return auditService;
	}
	
	@Autowired(required = true)
	public void setAuditService(AuditService auditService) {
		this.auditService = auditService;
	}

	public ContractService getContractService() {
		return contractService;
	}
	
	@Autowired(required = true)
	public void setContractService(ContractService contractService) {
		this.contractService = contractService;
	}

	public UserDao getUserDao() {
		return userDao;
	}
	
	@Autowired(required = true)
	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}

	public ContractTrackService getContractTrackService() {
		return contractTrackService;
	}
	
	@Autowired(required = true)
	public void setContractTrackService(ContractTrackService contractTrackService) {
		this.contractTrackService = contractTrackService;
	}
}
