package edu.uams.clara.webapp.contract.service.impl;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.xpath.XPathExpressionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.xml.sax.SAXException;

import edu.uams.clara.webapp.common.businesslogic.BusinessObjectStatusHelperContainer;
import edu.uams.clara.webapp.common.domain.usercontext.User;
import edu.uams.clara.webapp.common.domain.usercontext.enums.Committee;
import edu.uams.clara.webapp.common.service.UserService;
import edu.uams.clara.webapp.common.service.form.FormService;
import edu.uams.clara.webapp.common.util.DateFormatUtil;
import edu.uams.clara.webapp.contract.dao.ContractDao;
import edu.uams.clara.webapp.contract.dao.contractform.ContractFormDao;
import edu.uams.clara.webapp.contract.dao.contractform.ContractFormXmlDataDao;
import edu.uams.clara.webapp.contract.domain.Contract;
import edu.uams.clara.webapp.contract.domain.contractform.ContractForm;
import edu.uams.clara.webapp.contract.domain.contractform.ContractFormXmlData;
import edu.uams.clara.webapp.contract.domain.contractform.enums.ContractFormType;
import edu.uams.clara.webapp.contract.domain.contractform.enums.ContractFormXmlDataType;
import edu.uams.clara.webapp.contract.service.ContractFormService;
import edu.uams.clara.webapp.xml.processor.XmlProcessor;

public class ContractFormServiceImpl implements ContractFormService {
	
	private final static Logger logger = LoggerFactory
	.getLogger(ContractFormService.class);
	
	private UserService userService;

	private XmlProcessor xmlProcessor;
	
	private ContractFormDao contractFormDao;
	
	private FormService formService;
	
	private Map<ContractFormType, Set<ContractFormXmlDataType>> copyLists = new HashMap<ContractFormType, Set<ContractFormXmlDataType>>();
	{
		Set<ContractFormXmlDataType> lists = new HashSet<ContractFormXmlDataType>();
		lists.add(ContractFormXmlDataType.CONTRACT);
		copyLists.put(ContractFormType.NEW_CONTRACT, lists);
		
		lists.add(ContractFormXmlDataType.AMENDMENT);
		copyLists.put(ContractFormType.AMENDMENT, lists);
		
	}
	
	private ContractDao contractDao;
	
	private ContractFormXmlDataDao contractFormXmlDataDao;
	
	private BusinessObjectStatusHelperContainer businessObjectStatusHelperContainer;
	
	
	@Override
	public ContractForm createRevision(ContractForm contractForm){
		
			Date today = new Date();
			ContractForm nv = new ContractForm();
			nv.setCreated(today);
			nv.setParent(contractForm.getParent());
			nv.setContract(contractForm.getContract());
			nv.setContractFormType(contractForm.getContractFormType());
			nv.setMetaDataXml(contractForm.getMetaDataXml());
			
			nv = contractFormDao.saveOrUpdate(nv);
			
			Map<ContractFormXmlDataType, ContractFormXmlData> contractFormXmlDatas = new HashMap<ContractFormXmlDataType, ContractFormXmlData>(
					0);			
			
			
			for(ContractFormXmlDataType pfxdt:copyLists.get(contractForm.getContractFormType())){
				ContractFormXmlData opfxd = contractForm.getTypedContractFormXmlDatas().get(pfxdt);
				
				if(opfxd == null) continue;
				
				ContractFormXmlData npfxd = new ContractFormXmlData();
				npfxd.setCreated(today);
				npfxd.setContractForm(nv);
				npfxd.setParent(opfxd);
				npfxd.setContractFormXmlDataType(opfxd.getContractFormXmlDataType());
				npfxd.setXmlData(opfxd.getXmlData());
				npfxd.setRetired(false);
				
				npfxd = contractFormXmlDataDao.saveOrUpdate(npfxd);
				
				contractFormXmlDatas.put(npfxd.getContractFormXmlDataType(), npfxd);
			}
			nv.setTypedContractFormXmlDatas(contractFormXmlDatas);
			return nv;

	}
	

	@Override
	public ContractFormXmlData createNewForm(ContractFormType contractFormType,
			long contractId) throws XPathExpressionException, IOException,
			SAXException {
		Date created = new Date();
		Contract p = contractDao.findById(contractId);
		
		int index = 0;
		
		ContractForm originalContractForm = null;
		ContractFormXmlData originalContractFormXmlData = null;
		
		try{
			List<ContractForm> contractFormLst = contractFormDao.listContractFormsByContractIdAndContractFormType(contractId, ContractFormType.AMENDMENT);
			
			if (contractFormLst == null || contractFormLst.size() == 0){
				originalContractForm = contractFormDao.getContractFormByContractIdAndContractFormType(contractId, ContractFormType.NEW_CONTRACT);
				originalContractFormXmlData = originalContractForm.getTypedContractFormXmlDatas().get(ContractFormXmlDataType.CONTRACT);
				
				index = 1;
			} else {
				originalContractForm = contractFormDao.getLatestContractFormByContractIdAndContractFormType(contractId, ContractFormType.AMENDMENT);
				originalContractFormXmlData = originalContractForm.getTypedContractFormXmlDatas().get(ContractFormXmlDataType.AMENDMENT);
				
				index = contractFormLst.size() + 1;
			}
		} catch (Exception e){
			e.printStackTrace();
		}

		String contractFormXmlStringStart = "<"+ contractFormType.getBaseTag() +" id=\"" + p.getId()
				+ "\" identifier=\"" + p.getContractIdentifier() + "\" type=\""+ contractFormType.getDescription() +"\" created=\""+ DateFormatUtil.formateDateToMDY(created) +"\" timestamp=\""+ created.getTime() +"\" index=\""+ index +"\">";
		String contractFormXmlStringEnd = "</"+ contractFormType.getBaseTag() +">";
		String contractFormXmlString = "";
		String finalXmlString = null;
		
		if (contractFormType.equals(ContractFormType.AMENDMENT)){
			contractFormXmlString = formService.pullFromOtherForm("//committee-review", originalContractForm.getMetaDataXml());
		}
		
		finalXmlString = contractFormXmlStringStart + contractFormXmlString + contractFormXmlStringEnd;
		
		
		
		ContractForm f = new ContractForm();
		f.setContractFormType(contractFormType);
		f.setContract(p);
		/*if (contractFormType.equals(ContractFormType.AMENDMENT)){
			//f.setParent(originalContractForm);
			f.setMetaDataXml(originalContractForm.getMetaDataXml());
		} else {		
			
			f.setMetaDataXml(finalXmlString);
		}*/
		f.setMetaDataXml(finalXmlString);
		f.setParent(f);
		f.setCreated(created);	
		f.setLocked(false);

		f = contractFormDao.saveOrUpdate(f);
		
		ContractFormXmlData fxd = new ContractFormXmlData();
		fxd.setContractForm(f);
		if (contractFormType.equals(ContractFormType.AMENDMENT)){
			fxd.setXmlData(originalContractFormXmlData.getXmlData());
			fxd.setParent(originalContractFormXmlData);
		} else {
			fxd.setXmlData(finalXmlString);
			fxd.setParent(fxd);
		}
		
		fxd.setContractFormXmlDataType(f.getContractFormType()
				.getDefaultContractFormXmlDataType());
		fxd.setCreated(created);
		fxd = contractFormXmlDataDao.saveOrUpdate(fxd);

		Map<ContractFormXmlDataType, ContractFormXmlData> contractFormXmlDatas = new HashMap<ContractFormXmlDataType, ContractFormXmlData>(
				0);
		contractFormXmlDatas.put(fxd.getContractFormXmlDataType(), fxd);

		f.setTypedContractFormXmlDatas(contractFormXmlDatas);

		User currentUser = userService.getCurrentUser();

		triggerPIAction("CREATE", f, currentUser, null);

		return fxd;
	}

	@Override
	public void triggerPIAction(String action, ContractForm contractForm,
			User currentUser, String message) throws XPathExpressionException,
			IOException, SAXException {
		businessObjectStatusHelperContainer.getBusinessObjectStatusHelper(
				contractForm.getContractFormType().toString()).triggerAction(
				contractForm, Committee.PI, currentUser, action, message, null);
		
	}

	@Override
	public void triggerPIAction(String action, String condition, String workflow, 
			ContractForm contractForm, User currentUser, String message)
			throws XPathExpressionException, IOException, SAXException {
		businessObjectStatusHelperContainer.getBusinessObjectStatusHelper(
				contractForm.getContractFormType().toString()).triggerAction(
				contractForm, Committee.PI, currentUser, action, condition, workflow, 
				message, null);
		
	}

	@Override
	public boolean isCurrentUserPIOrNot(
			ContractFormXmlData contractFormXmlData, User currentUser) {
		return true;
	}

	@Autowired(required = true)
	public void setContractFormDao(ContractFormDao contractFormDao) {
		this.contractFormDao = contractFormDao;
	}

	public ContractFormDao getContractFormDao() {
		return contractFormDao;
	}

	@Autowired(required = true)
	public void setContractFormXmlDataDao(ContractFormXmlDataDao contractFormXmlDataDao) {
		this.contractFormXmlDataDao = contractFormXmlDataDao;
	}

	public ContractFormXmlDataDao getContractFormXmlDataDao() {
		return contractFormXmlDataDao;
	}	
	
	@Autowired(required=true)
	public void setBusinessObjectStatusHelperContainer(
			BusinessObjectStatusHelperContainer businessObjectStatusHelperContainer) {
		this.businessObjectStatusHelperContainer = businessObjectStatusHelperContainer;
	}

	public BusinessObjectStatusHelperContainer getBusinessObjectStatusHelperContainer() {
		return businessObjectStatusHelperContainer;
	}


	public UserService getUserService() {
		return userService;
	}

	@Autowired(required=true)
	public void setUserService(UserService userService) {
		this.userService = userService;
	}


	public XmlProcessor getXmlProcessor() {
		return xmlProcessor;
	}

	@Autowired(required=true)
	public void setXmlProcessor(XmlProcessor xmlProcessor) {
		this.xmlProcessor = xmlProcessor;
	}


	public ContractDao getContractDao() {
		return contractDao;
	}

	@Autowired(required=true)
	public void setContractDao(ContractDao contractDao) {
		this.contractDao = contractDao;
	}


	public FormService getFormService() {
		return formService;
	}

	@Autowired(required=true)
	public void setFormService(FormService formService) {
		this.formService = formService;
	}
}
