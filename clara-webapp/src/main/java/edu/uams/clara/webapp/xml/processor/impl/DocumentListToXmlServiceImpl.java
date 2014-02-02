package edu.uams.clara.webapp.xml.processor.impl;

import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import edu.uams.clara.core.util.xml.DomUtils;
import edu.uams.clara.core.util.xml.XmlHandler;
import edu.uams.clara.core.util.xml.XmlHandlerFactory;
import edu.uams.clara.webapp.contract.dao.contractform.ContractFormXmlDataDocumentDao;
import edu.uams.clara.webapp.contract.domain.contractform.ContractFormXmlDataDocumentWrapper;
import edu.uams.clara.webapp.protocol.dao.protocolform.ProtocolFormXmlDataDocumentDao;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolFormXmlDataDocumentWrapper;
import edu.uams.clara.webapp.xml.processor.DocumentListToXmlService;

public class DocumentListToXmlServiceImpl implements DocumentListToXmlService {

	private final static Logger logger = LoggerFactory
			.getLogger(DocumentListToXmlServiceImpl.class);
	private ProtocolFormXmlDataDocumentDao protocolFormXmlDataDocumentDao;
	private ContractFormXmlDataDocumentDao contractFormXmlDataDocumentDao;
	
	

	// PLEASE LETS COMBINE PROTOCOLFORMS AND CONTRACTFORMS TO SOMETHING MANAGEABLE 7/1/2013
	@Override
	public String transformProtocolFormXmlDataDocumentListToXML(
			List<ProtocolFormXmlDataDocumentWrapper> documentList) {
		
		logger.debug("Got list of size "+documentList.size());

		XmlHandler xmlHandler = null;
		try {
			xmlHandler = XmlHandlerFactory.newXmlHandler();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "<documents></documents>";
		}
		
		Document doc = xmlHandler.newDocument();//docBuilder.newDocument();
		Element rootElement = doc.createElement("documents");
		doc.appendChild(rootElement);
		
		for(ProtocolFormXmlDataDocumentWrapper claraDoc:documentList){
			Element d = doc.createElement("document");
			rootElement.appendChild(d);
			
			d.setAttribute("id", ""+claraDoc.getId());
			d.setAttribute("title", claraDoc.getTitle());
			d.setAttribute("created", claraDoc.getCreatedDate());
			d.setAttribute("category", claraDoc.getCategory());
			d.setAttribute("committee", claraDoc.getCommittee().toString());
			d.setAttribute("status", ((claraDoc.getStatus() != null)?claraDoc.getStatus().toString():""));
			d.setAttribute("path", claraDoc.getUploadedFile().getPath());
			d.setAttribute("filename", claraDoc.getUploadedFile().getFilename());
			d.setAttribute("identifier", claraDoc.getUploadedFile().getIdentifier());
			d.setAttribute("extension", claraDoc.getUploadedFile().getExtension());
			d.setAttribute("contenttype", claraDoc.getUploadedFile().getContentType());
			d.setAttribute("parentFormXmlDataDocumentId", ""+claraDoc.getParentProtocolFormXmlDataDocumentId());
			d.setAttribute("parentFormXmlDataId", ""+claraDoc.getProtocolFormXmlDataId());
			d.setAttribute("parentFormId", ""+claraDoc.getParentProtocolFormId());
			
			
			List<ProtocolFormXmlDataDocumentWrapper> versions = protocolFormXmlDataDocumentDao.listProtocolFormXmlDataDocumentRevisionsByParentId(claraDoc.getParentProtocolFormXmlDataDocumentId());
			logger.debug("Version count: "+versions.size());
			if (versions.size() > 0){
				Element v = doc.createElement("versions");
				d.appendChild(v);
				for(ProtocolFormXmlDataDocumentWrapper claraDocVersion:versions){
					Element dv = doc.createElement("document");
					v.appendChild(dv);
					
					dv.setAttribute("id", ""+claraDocVersion.getId());
					dv.setAttribute("title", claraDocVersion.getTitle());
					dv.setAttribute("created", claraDocVersion.getCreatedDate());
					dv.setAttribute("category", claraDocVersion.getCategory());
					dv.setAttribute("committee", claraDocVersion.getCommittee().toString());
					dv.setAttribute("status", ((claraDocVersion.getStatus() != null)?claraDocVersion.getStatus().toString():""));
					dv.setAttribute("path", claraDocVersion.getUploadedFile().getPath());
					dv.setAttribute("filename", claraDocVersion.getUploadedFile().getFilename());
					dv.setAttribute("identifier", claraDocVersion.getUploadedFile().getIdentifier());
					dv.setAttribute("extension", claraDocVersion.getUploadedFile().getExtension());
					dv.setAttribute("contenttype", claraDocVersion.getUploadedFile().getContentType());
					dv.setAttribute("parentFormXmlDataDocumentId", ""+claraDocVersion.getParentProtocolFormXmlDataDocumentId());
					dv.setAttribute("parentFormXmlDataId", ""+claraDocVersion.getProtocolFormXmlDataId());
					dv.setAttribute("parentFormId", ""+claraDocVersion.getParentProtocolFormId());
				}
			}
		}
		
		return DomUtils.elementToString(doc);
	}
	
	@Override
	public String transformContractFormXmlDataDocumentListToXML(
			List<ContractFormXmlDataDocumentWrapper> documentList) {
		
		logger.debug("Got list of size "+documentList.size());

		
		XmlHandler xmlHandler = null;
		try {
			xmlHandler = XmlHandlerFactory.newXmlHandler();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "<documents></documents>";
		}
		
		Document doc = xmlHandler.newDocument();//docBuilder.newDocument();
		Element rootElement = doc.createElement("documents");
		doc.appendChild(rootElement);
		
		for(ContractFormXmlDataDocumentWrapper claraDoc:documentList){
			Element d = doc.createElement("document");
			rootElement.appendChild(d);
			
			d.setAttribute("id", ""+claraDoc.getId());
			d.setAttribute("title", claraDoc.getTitle());
			d.setAttribute("created", claraDoc.getCreatedDate());
			d.setAttribute("category", claraDoc.getCategory());
			d.setAttribute("committee", claraDoc.getCommittee().toString());
			d.setAttribute("status", ((claraDoc.getStatus() != null)?claraDoc.getStatus().toString():""));
			d.setAttribute("path", claraDoc.getUploadedFile().getPath());
			d.setAttribute("filename", claraDoc.getUploadedFile().getFilename());
			d.setAttribute("identifier", claraDoc.getUploadedFile().getIdentifier());
			d.setAttribute("extension", claraDoc.getUploadedFile().getExtension());
			d.setAttribute("contenttype", claraDoc.getUploadedFile().getContentType());
			d.setAttribute("parentFormXmlDataDocumentId", ""+claraDoc.getParentContractFormXmlDataDocumentId());
			d.setAttribute("parentFormXmlDataId", ""+claraDoc.getContractFormXmlDataId());
			d.setAttribute("parentFormId", ""+claraDoc.getParentContractFormId());
			
			
			List<ContractFormXmlDataDocumentWrapper> versions = contractFormXmlDataDocumentDao.listContractFormXmlDataDocumentRevisionsByParentId(claraDoc.getParentContractFormXmlDataDocumentId());
			logger.debug("Version count: "+versions.size());
			if (versions.size() > 0){
				Element v = doc.createElement("versions");
				d.appendChild(v);
				for(ContractFormXmlDataDocumentWrapper claraDocVersion:versions){
					Element dv = doc.createElement("document");
					v.appendChild(dv);
					
					dv.setAttribute("id", ""+claraDocVersion.getId());
					dv.setAttribute("title", claraDocVersion.getTitle());
					dv.setAttribute("created", claraDocVersion.getCreatedDate());
					dv.setAttribute("category", claraDocVersion.getCategory());
					dv.setAttribute("committee", claraDocVersion.getCommittee().toString());
					dv.setAttribute("status", ((claraDocVersion.getStatus() != null)?claraDocVersion.getStatus().toString():""));
					dv.setAttribute("path", claraDocVersion.getUploadedFile().getPath());
					dv.setAttribute("filename", claraDocVersion.getUploadedFile().getFilename());
					dv.setAttribute("identifier", claraDocVersion.getUploadedFile().getIdentifier());
					dv.setAttribute("extension", claraDocVersion.getUploadedFile().getExtension());
					dv.setAttribute("contenttype", claraDocVersion.getUploadedFile().getContentType());
					dv.setAttribute("parentFormXmlDataDocumentId", ""+claraDocVersion.getParentContractFormXmlDataDocumentId());
					dv.setAttribute("parentFormXmlDataId", ""+claraDocVersion.getContractFormXmlDataId());
					dv.setAttribute("parentFormId", ""+claraDocVersion.getParentContractFormId());
				}
			}
		}
		
		return DomUtils.elementToString(doc);
		
	}

	public ContractFormXmlDataDocumentDao getContractFormXmlDataDocumentDao() {
		return contractFormXmlDataDocumentDao;
	}

	@Autowired(required=true)
	public void setContractFormXmlDataDocumentDao(
			ContractFormXmlDataDocumentDao contractFormXmlDataDocumentDao) {
		this.contractFormXmlDataDocumentDao = contractFormXmlDataDocumentDao;
	}

	public ProtocolFormXmlDataDocumentDao getProtocolFormXmlDataDocumentDao() {
		return protocolFormXmlDataDocumentDao;
	}

	@Autowired(required=true)
	public void setProtocolFormXmlDataDocumentDao(
			ProtocolFormXmlDataDocumentDao protocolFormXmlDataDocumentDao) {
		this.protocolFormXmlDataDocumentDao = protocolFormXmlDataDocumentDao;
	}

}
