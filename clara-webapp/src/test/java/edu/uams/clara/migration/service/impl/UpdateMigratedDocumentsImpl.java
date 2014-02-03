package edu.uams.clara.migration.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Set;

import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.io.IOUtils;
import org.bouncycastle.util.encoders.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import edu.uams.clara.core.util.xml.DomUtils;
import edu.uams.clara.core.util.xml.XmlHandler;
import edu.uams.clara.core.util.xml.DomUtils.Encoding;
import edu.uams.clara.migration.service.UpdateMigratedDocumentsService;
import edu.uams.clara.webapp.fileserver.service.SFTPService;
import edu.uams.clara.webapp.protocol.dao.ProtocolDao;
import edu.uams.clara.webapp.protocol.dao.protocolform.ProtocolFormDao;
import edu.uams.clara.webapp.protocol.dao.protocolform.ProtocolFormXmlDataDao;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolFormXmlData;
import edu.uams.clara.webapp.protocol.domain.protocolform.enums.ProtocolFormType;
import edu.uams.clara.webapp.protocol.domain.protocolform.enums.ProtocolFormXmlDataType;
import edu.uams.clara.webapp.xml.processor.XmlProcessor;

public class UpdateMigratedDocumentsImpl implements
		UpdateMigratedDocumentsService {

	private SFTPService sFTPService;

	private String fileServerHost;
	private String localDirectory;
	private XmlProcessor xmlProcessor;
	private MessageDigest messageDigest = null;
	private XmlHandler xmlHandler;

	private ProtocolDao protocolDao;
	private ProtocolFormDao protocolFormDao;
	private ProtocolFormXmlDataDao protocolFormXmlDataDao;

	private final static Logger logger = LoggerFactory
			.getLogger(UpdateMigratedDocumentsImpl.class);

	@Override
	public void updateMigratedDocumentsService(String ariaXmlData) {
		String protocolIdentifier = xmlHandler.getSingleStringValueByXPath(ariaXmlData, "//protocol/@id");
		long protocolId = Long.valueOf(protocolIdentifier);
		try {

			Set<String> paths = Sets.newHashSet();
			paths.add("//protocol/documents/document");

			List<Element> documentAriaEles = xmlProcessor
					.listDomElementsByPaths(paths, ariaXmlData);

			long protocolFormId = protocolFormDao.getProtocolFormByProtocolIdAndProtocolFormType(protocolId, ProtocolFormType.ARCHIVE).getFormId();

			ProtocolFormXmlData pfdx= protocolFormXmlDataDao.getLastProtocolFormXmlDataByProtocolFormIdAndType(protocolFormId, ProtocolFormXmlDataType.ARCHIVE);

			String xmlData = pfdx.getXmlData();
			Document doc = null;
			try {
				doc = xmlProcessor
						.loadXmlStringToDOM(xmlData);
			}catch(Exception e){

			}
			for (Element araiDocEle : documentAriaEles) {
				try {

					// upload documents to file server
					String oldUrl = xmlHandler.getSingleStringValueByXPath(
							ariaXmlData, "//protocol/documents/document[@id="
									+ araiDocEle.getAttribute("id")
									+ "]/ariapath");
					String hashFileName = uploadDocumenttoFileServer(
							protocolIdentifier, oldUrl);
					if(!hashFileName.isEmpty()){
						doc =setProtocolFormXmlData(doc, protocolIdentifier,  araiDocEle, hashFileName,oldUrl);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			xmlData = DomUtils.elementToString(doc, false);
			pfdx.setXmlData(xmlData);
			protocolFormXmlDataDao.saveOrUpdate(pfdx);

		} catch (Exception e) {
			logger.debug("Study is not in Clara: " + protocolId);
		}
	}

	private Document setProtocolFormXmlData(Document doc,String protocolIdentifier, Element araiDocEle,String hashFileName,String oldUrl) {


		//get existing docs path
		List<String> existingFilePaths =Lists.newArrayList();
		String xmlData = DomUtils.elementToString(doc,
				false, Encoding.UTF16);
		existingFilePaths=xmlHandler.getStringValuesByXPath(xmlData, "//protocol/documents/document/ariapath");

		String filepath = "https://" + fileServerHost+ "/files/protocol/" + protocolIdentifier + "/"+hashFileName;

		if(!existingFilePaths.contains(filepath)){
			try {
				Set<String> paths = Sets.newHashSet();
				paths.add("//protocol/documents");
				List<Element> documensEles=  xmlProcessor
					.listDomElementsByPaths(paths, xmlData);
				Element documentsEle;
				Element protocolEle =(Element) doc.getElementsByTagName("protocol").item(0);

				if(documensEles.size()>0){
				documentsEle = (Element)protocolEle.getElementsByTagName("documents").item(0);
				}else{
					documentsEle = doc.createElement("documents");
					protocolEle.appendChild(documentsEle);
				}

				Element newDocumentEle = doc
						.createElement("document");
				documentsEle.appendChild(newDocumentEle);
				Element newAriapathEle = doc
						.createElement("ariapath");
				newDocumentEle.appendChild(newAriapathEle);
				String[] splitUrl = oldUrl.split(
						"/" + protocolIdentifier + "/");
				newDocumentEle.setAttribute("oldName",
						splitUrl[splitUrl.length - 1]);
				newDocumentEle.setAttribute("date",
						araiDocEle.getAttribute("date"));
				newDocumentEle.setAttribute("id", araiDocEle.getAttribute("id"));
				newDocumentEle.setAttribute("status",
						araiDocEle.getAttribute("status"));
				newDocumentEle.setAttribute("title",
						araiDocEle.getAttribute("title"));
				newDocumentEle.setAttribute("type",
						araiDocEle.getAttribute("type"));
				newDocumentEle.setAttribute("version",
						araiDocEle.getAttribute("version"));
				newAriapathEle.setTextContent(filepath);

			} catch (Exception e) {
				e.printStackTrace();
			}


		}
		return doc;

	}

	private String uploadDocumenttoFileServer(String protocolIdentifier, String oldUrl)
			throws XPathExpressionException, DOMException,
			NoSuchAlgorithmException, IOException {

		File fileDir = new File(localDirectory);
		if (!fileDir.exists()) {
			fileDir.mkdir();
		}
		String hashFileNameAndExt = "";
		// copy the file from aria server to local
		String oldAriaDocUrl[] = oldUrl.split("/" + protocolIdentifier + "/");
		String fileName = "Z://" + protocolIdentifier + "//" + oldAriaDocUrl[1];
		// get extention
		String extention[] = oldAriaDocUrl[1].split("\\.");
		String ext = "." + extention[extention.length - 1];

		messageDigest = MessageDigest.getInstance("SHA-256",
				new org.bouncycastle.jce.provider.BouncyCastleProvider());
		try {
			InputStream fileContent = new FileInputStream(fileName);

			byte[] bytes = IOUtils.toByteArray(fileContent);

			messageDigest.update(bytes);

			// get the hash of file content
			String hashFileName = new String(Hex.encode(messageDigest.digest()));
			hashFileNameAndExt =hashFileName + ext;
			// hashFileNames.add(hashFileName + ext);
			// write the file to local

			String uploadfilename = localDirectory + "/" + hashFileName + ext;
			FileOutputStream fout = new FileOutputStream(uploadfilename);
			fout.write(bytes);
			fout.flush();
			fout.close();

			// upload file to the server
			int trySFTP = 1;
			while (trySFTP > 0) {
				try {

					sFTPService.uploadLocalFileToRemote("protocol/"
							+ protocolIdentifier + "/" + hashFileName + ext);

					trySFTP = 0;
				} catch (Exception e) {
					if (trySFTP < 5) {
						trySFTP++;
					}
					if (trySFTP == 5) {
						trySFTP = 0;
					}

				}
			}
			// delete the file after uploading...
			File uploadedFile = new File(uploadfilename);
			uploadedFile.delete();

			// upload success begin to addXmldata in clara
		} catch (Exception e) {
			return "";
		}

		// remove template dir
		fileDir.delete();
		return hashFileNameAndExt;

	}

	public SFTPService getsFTPService() {
		return sFTPService;
	}

	@Autowired(required = true)
	public void setsFTPService(SFTPService sFTPService) {
		this.sFTPService = sFTPService;
	}

	public String getLocalDirectory() {
		return localDirectory;
	}

	public void setLocalDirectory(String localDirectory) {
		this.localDirectory = localDirectory;
	}

	public String getFileServerHost() {
		return fileServerHost;
	}

	public void setFileServerHost(String fileServerHost) {
		this.fileServerHost = fileServerHost;
	}

	public XmlProcessor getXmlProcessor() {
		return xmlProcessor;
	}

	@Autowired(required = true)
	public void setXmlProcessor(XmlProcessor xmlProcessor) {
		this.xmlProcessor = xmlProcessor;
	}

	public XmlHandler getXmlHandler() {
		return xmlHandler;
	}

	@Autowired(required = true)
	public void setXmlHandler(XmlHandler xmlHandler) {
		this.xmlHandler = xmlHandler;
	}

	public ProtocolFormXmlDataDao getProtocolFormXmlDataDao() {
		return protocolFormXmlDataDao;
	}

	@Autowired(required = true)
	public void setProtocolFormXmlDataDao(
			ProtocolFormXmlDataDao protocolFormXmlDataDao) {
		this.protocolFormXmlDataDao = protocolFormXmlDataDao;
	}

	public ProtocolFormDao getProtocolFormDao() {
		return protocolFormDao;
	}

	@Autowired(required = true)
	public void setProtocolFormDao(ProtocolFormDao protocolFormDao) {
		this.protocolFormDao = protocolFormDao;
	}

	public ProtocolDao getProtocolDao() {
		return protocolDao;
	}

	@Autowired(required = true)
	public void setProtocolDao(ProtocolDao protocolDao) {
		this.protocolDao = protocolDao;
	}
}
