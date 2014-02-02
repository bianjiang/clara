package edu.uams.clara.webapp.contract.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import edu.uams.clara.core.util.xml.DomUtils;
import edu.uams.clara.webapp.common.domain.usercontext.User;
import edu.uams.clara.webapp.common.service.relation.RelationService;
import edu.uams.clara.webapp.common.security.ObjectAclService;
import edu.uams.clara.webapp.common.service.UserService;
import edu.uams.clara.webapp.common.service.form.FormService;
import edu.uams.clara.webapp.common.util.DateFormatUtil;
import edu.uams.clara.webapp.contract.dao.ContractDao;
import edu.uams.clara.webapp.contract.dao.contractform.ContractFormDao;
import edu.uams.clara.webapp.contract.dao.contractform.ContractFormXmlDataDao;
import edu.uams.clara.webapp.contract.domain.Contract;
import edu.uams.clara.webapp.contract.domain.contractform.ContractFormXmlData;
import edu.uams.clara.webapp.contract.domain.contractform.enums.ContractFormType;
import edu.uams.clara.webapp.contract.service.ContractFormService;
import edu.uams.clara.webapp.contract.service.ContractService;
import edu.uams.clara.webapp.protocol.domain.Protocol;
import edu.uams.clara.webapp.xml.processor.XmlProcessor;

public class ContractServiceImpl implements ContractService {
	
	private final static Logger logger = LoggerFactory
			.getLogger(ContractServiceImpl.class);
	
	private ContractDao contractDao;
	private ContractFormDao contractFormDao;
	private ContractFormXmlDataDao contractFormXmlDataDao;
	
	private UserService userService;
	private ContractFormService contractFormService;
	
	private ObjectAclService objectAclService;
	
	private FormService formService;
	
	private XmlProcessor xmlProcessor;
	
	private long baseContractIdentifier = 0;
	
	private RelationService relationService;


	@Override
	public ContractFormXmlData creatNewContract(ContractFormType contractFormType) throws XPathExpressionException, IOException,
	SAXException{
		Contract p = new Contract();
		Date created = new Date();
		p.setCreated(created);
		p.setLocked(false);
		
		p = contractDao.saveOrUpdate(p);
		
		String contractIdentifier = "C" + Long.toString(baseContractIdentifier+p.getId()) + "-" + Integer.toString(Calendar.getInstance().get(Calendar.YEAR));

		p.setContractIdentifier(contractIdentifier);

		//contract.metadataxml always start with /contract/
		//String contractMetaDataXmlString = "<contract id=\"" + p.getId() + "\" identifier=\"" + p.getContractIdentifier() + "\" type=\""+ contractFormType.getDescription() +"\" created=\""+ DateFormatUtil.formateDateToMDY(created) +"\"></contract>";
		String contractMetaDataXmlString = "<contract id=\"" + p.getId() + "\" identifier=\"" + p.getContractIdentifier() + "\" created=\""+ DateFormatUtil.formateDateToMDY(created) +"\"></contract>";
		
		p.setMetaDataXml(contractMetaDataXmlString);
		p = contractDao.saveOrUpdate(p);
		
		ContractFormXmlData cfxd = contractFormService.createNewForm(ContractFormType.NEW_CONTRACT, p.getId());

		User currentUser = userService.getCurrentUser();

		objectAclService.updateObjectAclByUser(Contract.class, p.getId(), currentUser);
		return cfxd;
	}
	
	@Override
	public Contract createNewFormFromProtocol(Protocol p)
			throws XPathExpressionException, IOException, SAXException {
		ContractFormXmlData contractFormXmlData = this.creatNewContract(ContractFormType.NEW_CONTRACT);
		
		Contract contract = pullFromProotcol(contractFormXmlData, p);
		
		/*
		String contractFormXml = xmlProcessor.replaceOrAddNodeValueByPath("/contract/basic-information/is-study-related", contractFormXmlData.getXmlData(), "y");
		
		contractFormXml = xmlProcessor.replaceOrAddNodeValueByPath("/contract/protocol", contractFormXml, String.valueOf(p.getId()));

		contractFormXmlData.setXmlData(contractFormXml);
		contractFormXmlData = contractFormXmlDataDao.saveOrUpdate(contractFormXmlData);
		
		Contract contract = contractFormXmlData.getContractForm().getContract();
		contract.setProtocol(p);
		contract = contractDao.saveOrUpdate(contract);
		
		relationService.addRelationByIdAndType(p.getId(), contract.getId(), "Protocol", "Contract");
		*/
		
		return contract;
	}
	
	private static List<String> protocolXPaths = new ArrayList<String>();{
		protocolXPaths.add("/protocol/staffs");
		protocolXPaths.add("/protocol/study-type");
		protocolXPaths.add("/protocol/committee-review/committee[assigned-reviewers/assigned-reviewer[@assigning-committee=\"BUDGET_MANAGER\"]]");
		protocolXPaths.add("/protocol/funding");
	}
	
	@Override
	public Contract pullFromProotcol(ContractFormXmlData contractFormXmlData, Protocol p)
			throws XPathExpressionException, IOException, SAXException {
		Contract contract = null;
		
		String contractFormXmlString = contractFormXmlData.getXmlData();
		
		List<String> values = xmlProcessor.listElementStringValuesByPath("/contract/protocol", contractFormXmlString);
		
		long oldRelatedProtocolId = (values != null && !values.isEmpty())?Long.valueOf(values.get(0)):0;
		
		try{
			for (String path : protocolXPaths){
				String pulledXmlString = formService.pullFromOtherForm(path, p.getMetaDataXml());
				
				Map<String, Object> resultMap = xmlProcessor.deleteElementByPath(path.replace("protocol", "contract"), contractFormXmlString);
				
				contractFormXmlString = resultMap.get("finalXml").toString();
				
				if (pulledXmlString != null && !pulledXmlString.isEmpty()){
					
					if (pulledXmlString.contains("assigned-reviewers")){
						Document assignedReviewerCommitteeDoc = xmlProcessor.loadXmlStringToDOM("<committee-review>" + pulledXmlString + "</committee-review>");
						
						XPath xPath = xmlProcessor.getXPathInstance();
						
						NodeList committeeLst = (NodeList) xPath.evaluate("/committee-review/committee", assignedReviewerCommitteeDoc, XPathConstants.NODESET);
						
						for (int i=0; i<committeeLst.getLength(); i++){
							String needToAddStr = DomUtils.elementToString(committeeLst.item(i));
							
							contractFormXmlString = resultMap.get("finalXml").toString();
							
							resultMap = xmlProcessor.addElementByPath("/contract/committee-review/committee", contractFormXmlString, needToAddStr, false);
						}
					} else {
						resultMap = xmlProcessor.addElementByPath(path.replace("protocol", "contract"), contractFormXmlString, pulledXmlString, false);
					}
				}
				
				contractFormXmlString = resultMap.get("finalXml").toString();
				
				
				if (path.equals("/protocol/staffs")){
					objectAclService.updateObjectAclByStaffXml(Contract.class, contractFormXmlData.getContractForm().getContract().getId(), pulledXmlString);
				}
			}
			
			contractFormXmlString = xmlProcessor.replaceOrAddNodeValueByPath("/contract/basic-information/is-study-related", contractFormXmlString, "y");
			
			contractFormXmlString = xmlProcessor.replaceOrAddNodeValueByPath("/contract/protocol", contractFormXmlString, String.valueOf(p.getId()));
			
			contractFormXmlData.setXmlData(contractFormXmlString);
			contractFormXmlData = contractFormXmlDataDao.saveOrUpdate(contractFormXmlData);		
			
			contract = contractFormXmlData.getContractForm().getContract();
			
			contract.setProtocol(p);
			contract = contractDao.saveOrUpdate(contract);
			
			if (oldRelatedProtocolId != p.getId()){
				try {
					relationService.removeRelationByIdAndType(oldRelatedProtocolId, contract.getId(), "protocol", "contract");
				} catch (Exception e) {
					//don't care
				}
				
				try {
					relationService.addRelationByIdAndType(p.getId(), contract.getId(), "protocol", "contract");
				} catch (Exception e) {
					//don't care
				}
				
			}
		} catch (Exception e){
			e.printStackTrace();
		}
		
		return contract;
	}

	public ContractDao getContractDao() {
		return contractDao;
	}
	
	@Autowired(required=true)
	public void setContractDao(ContractDao contractDao) {
		this.contractDao = contractDao;
	}

	public ContractFormDao getContractFormDao() {
		return contractFormDao;
	}
	
	@Autowired(required=true)
	public void setContractFormDao(ContractFormDao contractFormDao) {
		this.contractFormDao = contractFormDao;
	}

	public ContractFormXmlDataDao getContractFormXmlDataDao() {
		return contractFormXmlDataDao;
	}
	
	@Autowired(required=true)
	public void setContractFormXmlDataDao(ContractFormXmlDataDao contractFormXmlDataDao) {
		this.contractFormXmlDataDao = contractFormXmlDataDao;
	}

	public UserService getUserService() {
		return userService;
	}
	
	@Autowired(required=true)
	public void setUserService(UserService userService) {
		this.userService = userService;
	}

	public ContractFormService getContractFormService() {
		return contractFormService;
	}
	
	@Autowired(required=true)
	public void setContractFormService(ContractFormService contractFormService) {
		this.contractFormService = contractFormService;
	}

	public long getBaseContractIdentifier() {
		return baseContractIdentifier;
	}

	public void setBaseContractIdentifier(long baseContractIdentifier) {
		this.baseContractIdentifier = baseContractIdentifier;
	}

	public XmlProcessor getXmlProcessor() {
		return xmlProcessor;
	}
	
	@Autowired(required=true)
	public void setXmlProcessor(XmlProcessor xmlProcessor) {
		this.xmlProcessor = xmlProcessor;
	}

	public FormService getFormService() {
		return formService;
	}
	
	@Autowired(required=true)
	public void setFormService(FormService formService) {
		this.formService = formService;
	}

	public ObjectAclService getObjectAclService() {
		return objectAclService;
	}

	@Autowired(required=true)
	public void setObjectAclService(ObjectAclService objectAclService) {
		this.objectAclService = objectAclService;
	}

	public RelationService getRelationService() {
		return relationService;
	}
	
	@Autowired(required=true)
	public void setRelationService(RelationService relationService) {
		this.relationService = relationService;
	}

}
