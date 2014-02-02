package edu.uams.clara.webapp.contract.service.contractform;

import java.io.IOException;
import java.util.Map;

import javax.xml.transform.Source;
import javax.xml.xpath.XPathExpressionException;

import org.xml.sax.SAXException;

import edu.uams.clara.webapp.common.domain.usercontext.enums.Committee;
import edu.uams.clara.webapp.contract.domain.contractform.ContractFormXmlData;

public interface ContractFormXmlDataDocumentService {
	Map<String, Boolean> checkRequiredDocuments(ContractFormXmlData contractFormXmlData) throws IOException, SAXException,
	XPathExpressionException;
	
	Source listDocumentTypes(long contractId, long contractFormId, long userId, Committee committee);
}
