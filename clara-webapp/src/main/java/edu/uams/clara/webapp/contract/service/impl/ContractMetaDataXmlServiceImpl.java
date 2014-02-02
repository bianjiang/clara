package edu.uams.clara.webapp.contract.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.Assert;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import edu.uams.clara.core.util.xml.DomUtils;
import edu.uams.clara.webapp.contract.domain.contractform.ContractForm;
import edu.uams.clara.webapp.contract.domain.contractform.ContractFormXmlData;
import edu.uams.clara.webapp.contract.domain.contractform.enums.ContractFormXmlDataType;
import edu.uams.clara.webapp.contract.dao.ContractDao;
import edu.uams.clara.webapp.contract.dao.businesslogicobject.ContractFormStatusDao;
import edu.uams.clara.webapp.contract.dao.contractform.ContractFormDao;
import edu.uams.clara.webapp.contract.dao.contractform.ContractFormXmlDataDao;
import edu.uams.clara.webapp.contract.domain.Contract;
import edu.uams.clara.webapp.contract.domain.businesslogicobject.ContractFormStatus;
import edu.uams.clara.webapp.contract.domain.businesslogicobject.ContractStatus;
import edu.uams.clara.webapp.contract.service.ContractAndFormStatusService;
import edu.uams.clara.webapp.contract.service.ContractMetaDataXmlService;
import edu.uams.clara.webapp.xml.processor.XmlProcessor;

/**
 * @ToDo need to ensure the root of contract metadataxml is /contract/
 * @author bianjiang
 *
 */
public class ContractMetaDataXmlServiceImpl implements ContractMetaDataXmlService {

	private final static Logger logger = LoggerFactory
	.getLogger(ContractMetaDataXmlService.class);
	
	private ContractDao contractDao;
	
	private ContractFormDao procotolFormDao;
	
	private ContractFormXmlDataDao contractFormXmlDataDao;
	
	private ContractFormStatusDao contractFormStatusDao;
	
	private XmlProcessor xmlProcessor;
	
	private ResourceLoader resourceLoader;
	
	@Value("${validationXmlPath}")
	private String validationXmlPath;
	
	
	//define the fields of contract metaData...
	private Map<ContractFormXmlDataType, Map<String, String>> xPathPairMap = new EnumMap<ContractFormXmlDataType, Map<String, String>>(ContractFormXmlDataType.class);
	{
		Map<String, String> newContractXPathPairs = new HashMap<String, String>();
//		newSubmissionXPathPairs.put("/contract/submission-type", "/contract/submission-type");
		newContractXPathPairs.put("/contract/title", "/contract/title");
		newContractXPathPairs.put("/contract/type", "/contract/type");
		newContractXPathPairs.put("/contract/protocol", "/contract/protocol");
		newContractXPathPairs.put("/contract/sponsors", "/contract/sponsors");
		newContractXPathPairs.put("/contract/staffs", "/contract/staffs");
		newContractXPathPairs.put("/contract/committee-review", "/contract/committee-review");
		newContractXPathPairs.put("/contract/basic-info", "/contract/basic-info");
		newContractXPathPairs.put("/contract/study-type", "/contract/study-type");
		
		xPathPairMap.put(ContractFormXmlDataType.CONTRACT, newContractXPathPairs);
		
		Map<String, String> amendmentXPathPairs = new HashMap<String, String>();
//		newSubmissionXPathPairs.put("/contract/submission-type", "/contract/submission-type");
		amendmentXPathPairs.put("/contract/title", "/contract/title");
		amendmentXPathPairs.put("/contract/type", "/contract/type");
		amendmentXPathPairs.put("/contract/protocol", "/contract/protocol");
		amendmentXPathPairs.put("/contract/sponsors", "/contract/sponsors");
		amendmentXPathPairs.put("/contract/staffs", "/contract/staffs");
		amendmentXPathPairs.put("/contract/committee-review", "/contract/committee-review");
		amendmentXPathPairs.put("/contract/basic-info", "/contract/basic-info");
		amendmentXPathPairs.put("/contract/study-type", "/contract/study-type");
		
		xPathPairMap.put(ContractFormXmlDataType.AMENDMENT, amendmentXPathPairs);
	}
	
	private Map<ContractFormXmlDataType, Map<String, String>> contractFormXPathPairMap = new EnumMap<ContractFormXmlDataType, Map<String, String>>(ContractFormXmlDataType.class);
	{
		Map<String, String> newContractXPathPairs = new HashMap<String, String>();
		newContractXPathPairs.put("/contract/basic-information/nature", "/contract/title");
		newContractXPathPairs.put("/contract/basic-information/contract-type", "/contract/type");
		newContractXPathPairs.put("/contract/protocol", "/contract/protocol");
		newContractXPathPairs.put("/contract/sponsors", "/contract/sponsors");
		newContractXPathPairs.put("/contract/staffs", "/contract/staffs");
		newContractXPathPairs.put("/contract/study-type", "/contract/study-type");
		//newContractXPathPairs.put("/contract/committee-review", "/contract/committee-review");
			
		contractFormXPathPairMap.put(ContractFormXmlDataType.CONTRACT, newContractXPathPairs);
		
		Map<String, String> amendmentXPathPairs = new HashMap<String, String>();
		amendmentXPathPairs.put("/contract/basic-information/nature", "/contract/title");
		amendmentXPathPairs.put("/contract/basic-information/contract-type", "/contract/type");
		amendmentXPathPairs.put("/contract/protocol", "/contract/protocol");
		amendmentXPathPairs.put("/contract/sponsors", "/contract/sponsors");
		amendmentXPathPairs.put("/contract/staffs", "/contract/staffs");
		//amendmentXPathPairs.put("/contract/committee-review", "/contract/committee-review");
		amendmentXPathPairs.put("/contract/study-type", "/contract/study-type");
			
		contractFormXPathPairMap.put(ContractFormXmlDataType.AMENDMENT, amendmentXPathPairs);
	}
	
	@Override
	public ContractForm updateContractFormMetaDataXml(
			ContractFormXmlData contractFormXmlData, String extraDataXml) {
		Assert.notNull(contractFormXmlData);
		Assert.notNull(contractFormXmlData.getContractForm());
		Assert.notNull(contractFormXmlData.getContractForm().getContract());
		
		ContractFormXmlDataType contractFormXmlDataType = contractFormXmlData.getContractFormXmlDataType();
		ContractForm pf = contractFormXmlData.getContractForm();
		
		if(contractFormXPathPairMap.get(contractFormXmlDataType) == null) {
			logger.debug("no entry needs to be updated!");
			return pf; 
		}
		
		String contractFormMetaDataXml = pf.getMetaDataXml();
		
		try{
			ContractFormStatus contractFormStatus = contractFormStatusDao.getLatestContractFormStatusByFormId(pf.getId());
			
			logger.debug("contractFormStatus: " + contractFormStatus.getContractFormStatus().getDescription());
			
			contractFormMetaDataXml = xmlProcessor.replaceOrAddNodeValueByPath("/"+ pf.getContractFormType().getBaseTag() +"/status", contractFormMetaDataXml, contractFormStatus.getContractFormStatus().getDescription());
			
			pf.setMetaDataXml(contractFormMetaDataXml);
			pf = procotolFormDao.saveOrUpdate(pf);
			
			if (extraDataXml == null || extraDataXml.isEmpty()){
				logger.debug("before mergeByXPaths -> contractForm.metadataxml: " + contractFormMetaDataXml);
				contractFormMetaDataXml = xmlProcessor.mergeByXPaths(pf.getMetaDataXml(), contractFormXmlData.getXmlData(), XmlProcessor.Operation.UPDATE_IF_EXIST, contractFormXPathPairMap.get(contractFormXmlDataType));
				logger.debug("after mergeByXPaths -> contractForm.metadataxml: " + contractFormMetaDataXml);
			} else {
				contractFormMetaDataXml = parseExtraDataXml(pf, extraDataXml);
				
				Map<String, Object> resultMap = null;
				
				Document extraDataXmlDoc = xmlProcessor.loadXmlStringToDOM(extraDataXml);
				//Document currentContractMetaDataXmlDoc = xmlProcessor.loadXmlStringToDOM(contractFormMetaDataXml);
				XPath xpath = xmlProcessor.getXPathInstance();
				//***@TODO need to talk about this...this is mainly for assigned reviewer?
				/*
				Element committeeEl = (Element)xpath.evaluate("/committee-review/committee", extraDataXmlDoc,XPathConstants.NODE);
				
				if (committeeEl != null){
					logger.debug("committee-element: " + DomUtils.elementToString(committeeEl));
					resultMap = xmlProcessor.addSubElementToElementIdentifiedByXPath("/contract/committee-review", contractFormMetaDataXml, DomUtils.elementToString(committeeEl), false);
					
					contractFormMetaDataXml = resultMap.get("finalXml").toString();
				}
				
				Element committeeReviewEl = (Element) xpath.evaluate("/"+ pf.getContractFormType().getBaseTag() +"/committee-review",
						currentContractMetaDataXmlDoc, XPathConstants.NODE);
				
				Element committeeReviewInExtraDataEl = (Element) xpath.evaluate("/committee-review",
						extraDataXmlDoc, XPathConstants.NODE);
				
				Map<String, Object> orgResultMap = null;
				if (committeeReviewEl != null){
					Element committeeReviewFirstChildInExtraDataEl = null;
					if (committeeReviewInExtraDataEl != null && committeeReviewInExtraDataEl.hasChildNodes()){
						committeeReviewFirstChildInExtraDataEl = (Element) committeeReviewInExtraDataEl.getFirstChild();
						
						logger.debug("committee review first child: " + DomUtils.elementToString(committeeReviewFirstChildInExtraDataEl));
						orgResultMap = xmlProcessor.addSubElementToElementIdentifiedByXPath("/"+ pf.getContractFormType().getBaseTag() +"/committee-review", contractFormMetaDataXml, DomUtils.elementToString(committeeReviewFirstChildInExtraDataEl), false);
					}
				} else {
					orgResultMap = xmlProcessor.addElementByPath("/"+ pf.getContractFormType().getBaseTag() +"/committee-review", contractFormMetaDataXml, extraDataXml, false);
				}
				
				contractFormMetaDataXml = orgResultMap!=null?orgResultMap.get("finalXml").toString():contractFormMetaDataXml;*/
				Document contractFormMetaDataXmlDoc = xmlProcessor.loadXmlStringToDOM(contractFormMetaDataXml);	
				
				Element formStatusTrackEl = (Element) xpath.evaluate("//revisition-requested-status-track",
						extraDataXmlDoc, XPathConstants.NODE);
				
				if (formStatusTrackEl != null){
					logger.debug("form-status-track: " + DomUtils.elementToString(formStatusTrackEl));
								
					Element formStatusTrackElInMetaData = (Element) xpath.evaluate("/"+ pf.getContractFormType().getBaseTag() +"/committee-review/revisition-requested-status-track",
							contractFormMetaDataXmlDoc, XPathConstants.NODE);
					
					if(formStatusTrackElInMetaData != null){
						formStatusTrackElInMetaData.setAttribute("original-object-status",
								formStatusTrackEl.getAttribute("original-object-status"));
						formStatusTrackElInMetaData.setAttribute("original-form-status",
								formStatusTrackEl.getAttribute("original-form-status"));
						formStatusTrackElInMetaData.setAttribute(
								"original-form-committee-status",
								formStatusTrackEl.getAttribute("original-form-committee-status"));
						formStatusTrackElInMetaData.setAttribute("requested-committee",
								formStatusTrackEl.getAttribute("requested-committee"));
						contractFormMetaDataXml = DomUtils.elementToString(contractFormMetaDataXmlDoc);
					}else{
						resultMap = xmlProcessor.addSubElementToElementIdentifiedByXPath("/"+ pf.getContractFormType().getBaseTag() +"/committee-review", contractFormMetaDataXml, DomUtils.elementToString(formStatusTrackEl), false);
						contractFormMetaDataXml = resultMap.get("finalXml").toString();
					}
				}
			}
			
			String priorityLevel = contractFormStatus.getContractFormStatus().getPriorityLevel();
			
			Map<String, String> attributes = new HashMap<String, String>();
			attributes.put("priority", priorityLevel);
			
			contractFormMetaDataXml = xmlProcessor.addAttributesByPath("/contract/status", contractFormMetaDataXml, attributes);
			
			logger.debug("contractFormMetaDataXml->finaXml: " + contractFormMetaDataXml);
			
			pf.setMetaDataXml(contractFormMetaDataXml);
			pf = procotolFormDao.saveOrUpdate(pf);
			
			try{
				pf = addExtraContentToContractFormMetaData(pf);
			} catch (Exception e){
				e.printStackTrace();
			}
			
		} catch (Exception e){
			e.printStackTrace();
		}
		
		return pf;
	}
	
	private ContractForm addExtraContentToContractFormMetaData(ContractForm contractForm) throws Exception{
//		ContractFormStatus currentContractFormStatus = contractFormStatusDao.getLatestContractFormStatusByFormId(contractForm.getId());
//		logger.debug("current contractform status: " + currentContractFormStatus.getContractFormStatus());
//		
//		Map<String, Object> fResultMap = null;
//		String contractFormMetaDataXml = contractForm.getMetaDataXml();
//		
//		switch(currentContractFormStatus.getContractFormStatus()){
//		case EXPEDITED_APPROVED:
//			fResultMap = xmlProcessor.addElementByPath("/contract/expedited-or-not", contractFormMetaDataXml, "<expedited-or-not>yes</expedited-or-not>", false);
//			break;
//		case EXEMPT_APPROVED:
//			fResultMap = xmlProcessor.addElementByPath("/contract/exempt-or-not", contractFormMetaDataXml, "<exempt-or-not>yes</exempt-or-not>", false);
//			break;
//		}
//		
//		if (fResultMap !=null){
//			contractFormMetaDataXml = fResultMap.get("finalXml").toString();
//		}
//		
//		contractForm.setMetaDataXml(contractFormMetaDataXml);
//		contractForm = procotolFormDao.saveOrUpdate(contractForm);
		
		return contractForm;
		
	}
	
	private String parseExtraDataXml(ContractForm contractForm, String extraDataXml){
		Map<String, String> resultMap = new HashMap<String, String>();
		String contractFormMetaXml = contractForm.getMetaDataXml();
		
		List<String> values = null;
		try{
			values = xmlProcessor.listElementStringValuesByPath("/committee-review/committee/extra-content/contract-begin-date", extraDataXml);
			resultMap.put("/"+ contractForm.getContractFormType().getBaseTag() +"/basic-info/contract-begin-date", (values!=null&&values.size()>0)?values.get(0):"");
			
			values = xmlProcessor.listElementStringValuesByPath("/committee-review/committee/extra-content/contract-end-date", extraDataXml);
			resultMap.put("/"+ contractForm.getContractFormType().getBaseTag() +"/basic-info/contract-end-date", (values!=null&&values.size()>0)?values.get(0):"");
			
			values = xmlProcessor.listElementStringValuesByPath("/committee-review/committee/extra-content/contract-execution-date", extraDataXml);
			resultMap.put("/"+ contractForm.getContractFormType().getBaseTag() +"/basic-info/contract-execution-date", (values!=null&&values.size()>0)?values.get(0):"");
			
			String cancelReason = xmlProcessor.getAttributeValueByPathAndAttributeName("/committee-review/committee/cancel-reason", extraDataXml, "text");
			String cancelSubReason = xmlProcessor.getAttributeValueByPathAndAttributeName("/committee-review/committee/cancel-reason", extraDataXml, "subtext");
			
			resultMap.put("/"+ contractForm.getContractFormType().getBaseTag() +"/cancel-reason", (cancelReason!=null)?cancelReason:"");
			resultMap.put("/"+ contractForm.getContractFormType().getBaseTag() +"/cancel-sub-reason", (cancelSubReason!=null)?cancelSubReason:"");

			if (resultMap != null){
				for (Map.Entry<String, String> entry : resultMap.entrySet()){
					logger.debug("key: " + entry.getKey());
					logger.debug("value: " + entry.getValue());
					contractFormMetaXml = xmlProcessor.replaceOrAddNodeValueByPath(entry.getKey(), contractFormMetaXml, entry.getValue());
				}
			} 
			
		} catch (Exception e){
			e.printStackTrace();
		}
		
		return contractFormMetaXml;
	}
	
	@Override
	public Contract updateContractMetaDataXml(
			ContractForm contractForm
			) {
		
		Assert.notNull(contractForm);
		Assert.notNull(contractForm.getContract());
		
		ContractFormXmlDataType contractFormXmlDataType = contractForm.getContractFormType().getDefaultContractFormXmlDataType();
		
		Contract p = contractForm.getContract();		

		if(xPathPairMap.get(contractFormXmlDataType) == null) {
			logger.debug("no entry needs to be updated!");
			return p; 
		}
		try {
			 String contractMetaDataXml = xmlProcessor.mergeByXPaths(p.getMetaDataXml(), contractForm.getMetaDataXml(), XmlProcessor.Operation.UPDATE_IF_EXIST, xPathPairMap.get(contractFormXmlDataType));
			logger.debug("after mergeByXPaths -> contract.metadataxml: " + contractMetaDataXml);
				
			ContractStatus contractStatus = contractDao.getLatestContractStatusByContractId(p.getId());
			logger.debug("contractStatus: " + contractStatus.getContractStatus().getDescription());
			contractMetaDataXml = xmlProcessor.replaceOrAddNodeValueByPath("/contract/status", contractMetaDataXml, org.apache.commons.lang.StringEscapeUtils.escapeXml(contractStatus.getContractStatus().getDescription()));
		
			String priorityLevel = contractStatus.getContractStatus().getPriorityLevel();
			
			Map<String, String> attributes = new HashMap<String, String>();
			attributes.put("priority", priorityLevel);
			
			contractMetaDataXml = xmlProcessor.addAttributesByPath("/contract/status", contractMetaDataXml, attributes);
			
			logger.debug("final contractMetaDataXml: " + contractMetaDataXml);

			p.setMetaDataXml(contractMetaDataXml);
			p = contractDao.saveOrUpdate(p);
		
		} catch (Exception ex) {
			logger.error(ex.getMessage());
			ex.printStackTrace();
		}
		
		return p;
	}
	
//	@Override
//	public Contract updateContractMetaDataXml(
//			ContractForm contractForm,
//			ContractFormXmlDataType contractFormXmlDataType) {
//		
//		Assert.notNull(contractForm);
//		
//		Assert.notNull(contractFormXmlDataType);
//		
//		ContractFormXmlData contractFormXmlData = contractForm.getTypedContractFormXmlDatas().get(contractFormXmlDataType);
//		
//		Contract p = updateContractMetaDataXml(contractFormXmlData);	
//		
//		
//		return p;
//		
//		
//	}
	
	/*
	@Override
	public Contract updateContractStatus(Contract contract) {
		
		//get current contractstatus and put it into the contract.metaDataXml
		try{

			ContractStatus contractStatus = contractDao.getLatestContractStatusByContractId(contract.getId());
						
			logger.debug("contractStatus: " + contractStatus.getContractStatus().getDescription());
			String thisContractMetaDataXml = xmlProcessor.replaceOrAddNodeValueByPath("/contract/status", contract.getMetaDataXml(), org.apache.commons.lang.StringEscapeUtils.escapeXml(contractStatus.getContractStatus().getDescription())); 
		
			logger.debug("metaDataXml: " + thisContractMetaDataXml);
			
			contract.setMetaDataXml(thisContractMetaDataXml);
			
			contract = contractDao.saveOrUpdate(contract);		
			
		}catch(Exception ex)
		{
			ex.printStackTrace();
			logger.error("missing status for contract: " + contract.getId());
		}
		
		return contract;
		
	}
	*/
//	@Override
//	public ContractForm updateContractFormStatus(ContractForm contractForm) {
//			
//		//get current contractformstatus and put it into the contractform.metaDataXml
//		try{
//			ContractFormStatus contractFormStatus = procotolFormDao.getLatestContractFormStatusByContractFormId(contractForm.getId());
//						
//			logger.debug("contractFormStatus: " + contractFormStatus.getContractFormStatus().getDescription());
//			String thisContractFormMetaDataXml = xmlProcessor.replaceOrAddNodeValueByPath("/contract/status", contractForm.getMetaDataXml(), org.apache.commons.lang.StringEscapeUtils.escapeXml(contractFormStatus.getContractFormStatus().getDescription())); 
//		
//			logger.debug("metaDataXml: " + thisContractFormMetaDataXml);
//			contractForm.setMetaDataXml(thisContractFormMetaDataXml);
//			
//			contractForm = procotolFormDao.saveOrUpdate(contractForm);	
//			
//		}catch(Exception ex)
//		{
//			logger.error("missing status for contractForm: " + contractForm.getId());
//		}
//		
//		return contractForm;
//		
//	}
	
	@Override
	public ContractFormXmlData consolidateContractFormXmlData(
			ContractFormXmlData contractFormXmlData, ContractFormXmlDataType contractFormXmlDataType) throws IOException, SAXException,
			XPathExpressionException {
		String lookupPath = validationXmlPath + "/newContractValidation.xml";
		
		//String testPath = "file:src/test/java/edu/uams/clara/webapp/contract/service/newSubmissionValidation.xml";
		
		XPath xPath = xmlProcessor.getXPathInstance();

		//Document validationXmlDocument = xmlProcessor
				//.loadXmlFileToDOM(testPath);
		Document validationXmlDocument = xmlProcessor.loadXmlFileToDOM(resourceLoader.getResource(lookupPath).getFile());
		
		String prerequisiteRuleXpath = "/rules/rule/prerequisites/rule";
		
		NodeList prerequisiteRulesLst = (NodeList) xPath.evaluate(prerequisiteRuleXpath,
				validationXmlDocument, XPathConstants.NODESET);
		
		logger.debug("nodelist length: " + prerequisiteRulesLst.getLength());
		
		for (int i=0; i<prerequisiteRulesLst.getLength(); i++){
			//Element prerequisiteRuleEl = (Element) xPath.evaluate(prerequisiteRuleXpath,
					//validationXmlDocument, XPathConstants.NODE);
			if (prerequisiteRulesLst.item(i).getNodeType() == Node.ELEMENT_NODE){
				Element prerequisiteRuleEl = (Element) prerequisiteRulesLst.item(i);
				
				List<String> lst = new ArrayList<String>(0);
				lst = xmlProcessor.listElementStringValuesByPath(prerequisiteRuleEl.getAttribute("path"), contractFormXmlData.getXmlData());

				Element prerequisiteRuleConstraintEl = (Element) prerequisiteRuleEl.getElementsByTagName("constraint").item(0);
				
				Map<String, Object> resultMap = new HashMap<String, Object>();
				
				Element el = (Element) prerequisiteRuleEl.getParentNode().getParentNode();

				if (needRemoved(lst, prerequisiteRuleConstraintEl.getAttribute("data"))){
					Element ruleEl = (Element) prerequisiteRuleConstraintEl.getParentNode().getParentNode().getParentNode();
					
					resultMap = xmlProcessor.deleteElementByPath(ruleEl.getAttribute("path"), contractFormXmlData.getXmlData());
					
					logger.debug("path: " + ruleEl.getAttribute("path") + " removed!");
					
					contractFormXmlData.setXmlData(resultMap.get("finalXml").toString());
					contractFormXmlData = contractFormXmlDataDao.saveOrUpdate(contractFormXmlData);
				}
			}
			
		}
		
		Document currentContractXmlDataDoc = xmlProcessor.loadXmlStringToDOM(contractFormXmlData.getXmlData());
		NodeList list = currentContractXmlDataDoc.getElementsByTagName("*");
		
		logger.debug("totoal node list length: " + list.getLength());

		for (int i=0; i<list.getLength(); i++){
			Node nd = list.item(i);
			
			if (!nd.hasChildNodes() && !nd.hasAttributes() && nd.getTextContent().isEmpty()){
				Node parentNode = nd.getParentNode();
				parentNode.removeChild(nd);
			}
			
			for (int j=0; j<nd.getChildNodes().getLength(); j++){
				if (!nd.getChildNodes().item(j).hasChildNodes() && !nd.getChildNodes().item(j).hasAttributes() && nd.getChildNodes().item(j).getTextContent().isEmpty()){
					Node pnd = nd.getChildNodes().item(j).getParentNode();
					pnd.removeChild(nd.getChildNodes().item(j));
				}
			}
		}
		
		String finalXml = DomUtils.elementToString(currentContractXmlDataDoc);
		contractFormXmlData.setXmlData(finalXml);
		contractFormXmlData = contractFormXmlDataDao.saveOrUpdate(contractFormXmlData);
		
		return contractFormXmlData;
	}
	
	private boolean needRemoved(List<String> lst, String dataValue){
		boolean needRemoved = false;
		
		String value = null;
		List<String> valueLst = null;
		
		if (lst.size()>0){
			if (dataValue.contains(",")){
				valueLst = Arrays.asList(dataValue.split(","));
				
				//Collections.disjoint Returns true if the two specified collections have no elements in common
				if (Collections.disjoint(valueLst, lst)){
					needRemoved = true;
				} else {
					needRemoved = false;
				}
			} else {
				value = dataValue;
				if (lst.get(0).equals(value)){
					needRemoved = false;
				} else {
					needRemoved = true;
				}
			}
		}

		return needRemoved;
	}

	@Autowired(required = true)
	public void setContractDao(ContractDao contractDao) {
		this.contractDao = contractDao;
	}

	public ContractDao getContractDao() {
		return contractDao;
	}
	
	@Autowired(required=true)
	public void setContractFormXmlDataDao(ContractFormXmlDataDao contractFormXmlDataDao) {
		this.contractFormXmlDataDao = contractFormXmlDataDao;
	}

	public ContractFormXmlDataDao getContractFormXmlDataDao() {
		return contractFormXmlDataDao;
	}

	@Autowired(required = true)
	public void setXmlProcessor(XmlProcessor xmlProcessor) {
		this.xmlProcessor = xmlProcessor;
	}

	public XmlProcessor getXmlProcessor() {
		return xmlProcessor;
	}

	public ContractFormDao getProcotolFormDao() {
		return procotolFormDao;
	}

	@Autowired(required = true)
	public void setProcotolFormDao(ContractFormDao procotolFormDao) {
		this.procotolFormDao = procotolFormDao;
	}

	public String getValidationXmlPath() {
		return validationXmlPath;
	}

	public void setValidationXmlPath(String validationXmlPath) {
		this.validationXmlPath = validationXmlPath;
	}

	public ResourceLoader getResourceLoader() {
		return resourceLoader;
	}
	
	@Autowired(required = true)
	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}

	public ContractFormStatusDao getContractFormStatusDao() {
		return contractFormStatusDao;
	}

	@Autowired(required = true)
	public void setContractFormStatusDao(ContractFormStatusDao contractFormStatusDao) {
		this.contractFormStatusDao = contractFormStatusDao;
	}
}
