package edu.uams.clara.webapp.contract.service;

import java.io.IOException;

import javax.xml.xpath.XPathExpressionException;

import org.xml.sax.SAXException;

import edu.uams.clara.webapp.contract.domain.Contract;
import edu.uams.clara.webapp.contract.domain.contractform.ContractForm;
import edu.uams.clara.webapp.contract.domain.contractform.ContractFormXmlData;
import edu.uams.clara.webapp.contract.domain.contractform.enums.ContractFormXmlDataType;

public interface ContractMetaDataXmlService {
	ContractForm updateContractFormMetaDataXml(ContractFormXmlData contractFormXmlData, String extraDataXml);
	
	Contract updateContractMetaDataXml(ContractForm contractForm);
	
	//Contract updateContractMetaDataXml(ContractForm contractForm, ContractFormXmlDataType contractFormXmlDataType);

	//Contract updateContractStatus(Contract contract);

	//ContractForm updateContractFormStatus(ContractForm contractForm);
	
	ContractFormXmlData consolidateContractFormXmlData(ContractFormXmlData contractFormXmlData, ContractFormXmlDataType contractFormXmlDataType) throws IOException, SAXException,
	XPathExpressionException;
}
