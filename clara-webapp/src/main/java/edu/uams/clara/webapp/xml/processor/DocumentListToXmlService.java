package edu.uams.clara.webapp.xml.processor;

import java.util.List;


import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolFormXmlDataDocumentWrapper;
import edu.uams.clara.webapp.contract.domain.contractform.ContractFormXmlDataDocumentWrapper;

public interface DocumentListToXmlService {
	String transformProtocolFormXmlDataDocumentListToXML(List<ProtocolFormXmlDataDocumentWrapper> documentList);

	String transformContractFormXmlDataDocumentListToXML(List<ContractFormXmlDataDocumentWrapper> documentList);
}