package edu.uams.clara.webapp.contract.service;

import java.io.IOException;

import javax.xml.xpath.XPathExpressionException;

import org.xml.sax.SAXException;

import edu.uams.clara.webapp.contract.domain.Contract;
import edu.uams.clara.webapp.contract.domain.contractform.ContractFormXmlData;
import edu.uams.clara.webapp.contract.domain.contractform.enums.ContractFormType;
import edu.uams.clara.webapp.protocol.domain.Protocol;

public interface ContractService {
	ContractFormXmlData creatNewContract(ContractFormType contractFormType) throws XPathExpressionException, IOException,
	SAXException;
	
	Contract createNewFormFromProtocol(Protocol p) throws XPathExpressionException, IOException, SAXException;
	
	Contract pullFromProotcol(ContractFormXmlData contractFormXmlData, Protocol p) throws XPathExpressionException, IOException, SAXException;
}
