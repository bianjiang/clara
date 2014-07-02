package edu.uams.clara.webapp.protocol.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.xml.xpath.XPathExpressionException;

import org.xml.sax.SAXException;

import edu.uams.clara.webapp.common.domain.usercontext.User;
import edu.uams.clara.webapp.common.domain.usercontext.enums.Committee;
import edu.uams.clara.webapp.protocol.domain.Protocol;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.enums.ProtocolStatusEnum;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolForm;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolFormXmlData;
import edu.uams.clara.webapp.protocol.domain.protocolform.enums.ProtocolFormType;

public interface ProtocolService {
	public ProtocolFormXmlData creatNewProtocol(ProtocolFormType protocolFormType) throws XPathExpressionException, IOException,
	SAXException;
	
	String populateEpicDesc(String xmlData);
	
	void setProtocolStatus(Protocol protocol, ProtocolStatusEnum protocolStatusEnum, User user, Committee committee, String note);
	
	boolean allowForm(Protocol protocol, ProtocolFormType protocolFormType);
	
	Map<String, Boolean> checkStudyCharacteristic(String protocolMetaData);
	
	//boolean isPushedToEpic(String protocolMetaData);
	
	boolean isPushedToPSC(String protocolMetaData);
	
	void pushToEpic(Protocol protocol);
	
	Protocol consolidateProtocol(Protocol protocol, List<String> xPathList);

}
