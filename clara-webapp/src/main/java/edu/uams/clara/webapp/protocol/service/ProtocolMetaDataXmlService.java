package edu.uams.clara.webapp.protocol.service;

import java.io.IOException;

import javax.xml.xpath.XPathExpressionException;

import org.xml.sax.SAXException;

import edu.uams.clara.webapp.protocol.domain.Protocol;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolForm;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolFormXmlData;
import edu.uams.clara.webapp.protocol.domain.protocolform.enums.ProtocolFormXmlDataType;

public interface ProtocolMetaDataXmlService {
	ProtocolForm updateProtocolFormMetaDataXml(ProtocolFormXmlData protocolFormXmlData, String extraDataXml);
	
	Protocol updateProtocolMetaDataXml(ProtocolForm protocolForm);
	
	//Protocol updateProtocolMetaDataXml(ProtocolForm protocolForm, ProtocolFormXmlDataType protocolFormXmlDataType);

	//Protocol updateProtocolStatus(Protocol protocol);

	//ProtocolForm updateProtocolFormStatus(ProtocolForm protocolForm);
	
	ProtocolFormXmlData consolidateProtocolFormXmlData(ProtocolFormXmlData protocolFormXmlData, ProtocolFormXmlDataType protocolFormXmlDataType) throws IOException, SAXException,
	XPathExpressionException;
}
