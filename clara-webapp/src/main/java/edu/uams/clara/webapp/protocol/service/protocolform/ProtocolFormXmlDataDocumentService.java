package edu.uams.clara.webapp.protocol.service.protocolform;

import java.io.IOException;
import java.util.Map;

import javax.xml.transform.Source;
import javax.xml.xpath.XPathExpressionException;

import org.xml.sax.SAXException;

import edu.uams.clara.webapp.common.domain.usercontext.enums.Committee;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolFormXmlData;

public interface ProtocolFormXmlDataDocumentService {
	Map<String, Boolean> checkRequiredDocuments(ProtocolFormXmlData protocolFormXmlData) throws IOException, SAXException,
	XPathExpressionException;
	
	Source listDocumentTypes(long protocolId, long protocolFormId, long userId, Committee committee);
}
