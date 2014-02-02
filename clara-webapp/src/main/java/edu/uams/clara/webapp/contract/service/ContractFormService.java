package edu.uams.clara.webapp.contract.service;

import java.io.IOException;

import javax.xml.xpath.XPathExpressionException;

import org.xml.sax.SAXException;

import edu.uams.clara.webapp.common.domain.usercontext.User;
import edu.uams.clara.webapp.contract.domain.contractform.ContractForm;
import edu.uams.clara.webapp.contract.domain.contractform.ContractFormXmlData;
import edu.uams.clara.webapp.contract.domain.contractform.enums.ContractFormType;
import edu.uams.clara.webapp.protocol.domain.Protocol;

public interface ContractFormService {

	ContractForm createRevision(ContractForm contractForm);
	
	ContractFormXmlData createNewForm(ContractFormType contractFormType, long contractId) throws XPathExpressionException, IOException, SAXException;

	void triggerPIAction(String action, ContractForm contractForm,
			User currentUser, String message) throws XPathExpressionException,
			IOException, SAXException;

	void triggerPIAction(String action, String condition, String workflow,
			ContractForm contractForm, User currentUser, String message)
			throws XPathExpressionException, IOException, SAXException;

	boolean isCurrentUserPIOrNot(ContractFormXmlData contractFormXmlData,
			User currentUser);

}
