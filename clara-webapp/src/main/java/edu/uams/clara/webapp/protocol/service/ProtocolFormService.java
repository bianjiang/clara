package edu.uams.clara.webapp.protocol.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.xml.xpath.XPathExpressionException;

import org.xml.sax.SAXException;

import edu.uams.clara.webapp.common.domain.usercontext.User;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolForm;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolFormXmlData;
import edu.uams.clara.webapp.protocol.domain.protocolform.enums.ProtocolFormType;

public interface ProtocolFormService {

	ProtocolForm createRevision(ProtocolForm protocolForm);
	
	ProtocolFormXmlData createNewForm(ProtocolFormType protocolFormType, long protocolId) throws XPathExpressionException, IOException, SAXException;

	void triggerPIAction(String action, ProtocolForm protocolForm,
			User currentUser, String message) throws XPathExpressionException,
			IOException, SAXException;

	void triggerPIAction(String action, String condition, String workflow, 
			ProtocolForm protocolForm, User currentUser, String message)
			throws XPathExpressionException, IOException, SAXException;
	
	//String isIndustryOrInvestigatorInitiated(ProtocolFormXmlData protocolFormXmlData);
	
	//String isUAMSOrACH(ProtocolFormXmlData protocolFormXmlData);
	
	String workFlowDetermination(ProtocolFormXmlData protocolFormXmlData);

	//String isNotificationOrFollowUp(ProtocolFormXmlData protocolFormXmlData);
	
	//String isBudgetModificationOrNot(ProtocolFormXmlData protocolFormXmlData);

	void generateIRBFees(ProtocolFormXmlData protocolFormXmlData) throws IOException, SAXException,
			XPathExpressionException;

	String generateExternalExpenses(long protocolFormId);
	
	void updateIRBExpensesInBudget(ProtocolForm protocolForm, String xmlData);
	
	String finalSignOffDetermination(ProtocolForm protocolForm, User user);
	
	ProtocolForm consolidateProtocolForm(ProtocolForm protocolForm, List<String> xPathList);
	
	Map<String, Boolean> budgetRelatedDetermination(ProtocolFormXmlData protocolFormXmlData);
	
	Map<String, Boolean> nctNumberValidation(ProtocolForm protocolForm);
}
