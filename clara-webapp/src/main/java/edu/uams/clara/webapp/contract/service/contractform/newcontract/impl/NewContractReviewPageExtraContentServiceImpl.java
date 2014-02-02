package edu.uams.clara.webapp.contract.service.contractform.newcontract.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import edu.uams.clara.webapp.contract.dao.contractform.ContractFormDao;
import edu.uams.clara.webapp.contract.domain.Contract;
import edu.uams.clara.webapp.contract.domain.contractform.ContractForm;
import edu.uams.clara.webapp.contract.service.contractform.newcontract.NewContractReviewPageExtraContentService;
import edu.uams.clara.webapp.xml.processor.XmlProcessor;

public class NewContractReviewPageExtraContentServiceImpl implements
		NewContractReviewPageExtraContentService {
	
	private ContractFormDao contractFormDao;
	
	private XmlProcessor xmlProcessor;

	@Override
	public String getExtraContent(long contractFormId,
			String reviewFormIdentifier) {
		String resultXml = "";
		
		if (reviewFormIdentifier.equals("contract-admin-review")){
			resultXml = getContractAdminExtraContent(contractFormId);
		}
		
		return resultXml;
	}
	
	private String getExtraContentValue(long contractFormId, String path){
		ContractForm contractForm = contractFormDao.findById(contractFormId);
		Contract contract = contractForm.getContract();
		
		String contractMetaData = contract.getMetaDataXml();
		
		String value = "";
		try{
			List<String> values = xmlProcessor.listElementStringValuesByPath(path, contractMetaData);
			if (values != null && !values.isEmpty()){
				value = values.get(0);
			}
		} catch (Exception e){
			e.printStackTrace();
		}
		
		return value;
	}
	
	private String getContractAdminExtraContent(long contractFormId){
		String endDate = this.getExtraContentValue(contractFormId, "/contract/basic-info/contract-end-date");
		String beginDate = this.getExtraContentValue(contractFormId, "/contract/basic-info/contract-begin-date");
		String executionDate = this.getExtraContentValue(contractFormId, "/contract/basic-info/contract-execution-date");
		
		String resultXml = "<panels><panel xtype=\"clarareviewercontractadminpanel\" id=\"ContractAdminFinalReviewPanel\"><formdata>";
		resultXml += "<contract-end-date>" + endDate + "</contract-end-date>";
		resultXml += "<contract-begin-date>" + beginDate + "</contract-begin-date>";
		resultXml += "<contract-execution-date>" + executionDate + "</contract-execution-date>";
		resultXml += "</formdata></panel></panels>";
		return resultXml;
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

}
