package edu.uams.clara.migration.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.IOUtils;
import org.bouncycastle.util.encoders.Hex;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.google.common.collect.Lists;

import edu.uams.clara.core.util.xml.DomUtils;
import edu.uams.clara.core.util.xml.DomUtils.Encoding;
import edu.uams.clara.integration.incoming.crimson.dao.CrimsonStudyDao;
import edu.uams.clara.migration.service.impl.MigrationServiceImpl;
import edu.uams.clara.webapp.common.dao.security.acl.SecurableObjectAclDao;
import edu.uams.clara.webapp.common.dao.security.acl.SecurableObjectDao;
import edu.uams.clara.webapp.common.dao.usercontext.UserDao;
import edu.uams.clara.webapp.common.domain.security.acl.SecurableObject;
import edu.uams.clara.webapp.common.domain.security.acl.SecurableObjectAcl;
import edu.uams.clara.webapp.common.domain.security.acl.enums.Permission;
import edu.uams.clara.webapp.common.domain.usercontext.User;
import edu.uams.clara.webapp.common.security.impl.ObjectAclServiceImpl;
import edu.uams.clara.webapp.fileserver.service.SFTPService;
import edu.uams.clara.webapp.protocol.dao.ProtocolDao;
import edu.uams.clara.webapp.protocol.dao.businesslogicobject.ProtocolFormStatusDao;
import edu.uams.clara.webapp.protocol.dao.protocolform.ProtocolFormDao;
import edu.uams.clara.webapp.protocol.dao.protocolform.ProtocolFormXmlDataDao;
import edu.uams.clara.webapp.protocol.domain.Protocol;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.ProtocolFormStatus;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.enums.ProtocolFormStatusEnum;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolForm;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolFormXmlData;
import edu.uams.clara.webapp.protocol.domain.protocolform.enums.ProtocolFormType;
import edu.uams.clara.webapp.protocol.domain.protocolform.enums.ProtocolFormXmlDataType;
import edu.uams.clara.webapp.xml.processor.XmlProcessor;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "file:src/test/java/edu/uams/clara/migration/service/migrationServiceTest-context.xml" })
public class migrationServiceTest {
	private final static Logger logger = LoggerFactory
			.getLogger(migrationServiceTest.class);

	private CrimsonStudyDao crimsonStudyDao;
	private MigrationServiceImpl migrationServiceImpl;

	private ProtocolFormDao protocolFormDao;
	private ProtocolFormStatusDao protocolFormStatusDao;
	private ProtocolFormXmlDataDao protocolFormXmlDataDao;
	private ProtocolDao protocolDao;
	private SecurableObjectDao securableObjectDao;
	private SecurableObjectAclDao securableObjectAclDao;

	private UserDao userDao;
	private ObjectAclServiceImpl objectAclService;
	private XPathFactory xpathFactory = XPathFactory.newInstance();
	private XmlProcessor xmlProcessor;
	private SFTPService sFTPService;
	public XPath getXPathInstance() {
		return xpathFactory.newXPath();
	}
	
	private MessageDigest messageDigest = null;
	private String fileServerHost="clarafs.uams.edu";
	private String localDirectory="C:/Data/upload";

	@Test
	public void updatePacketDocument() throws IOException,
			XPathExpressionException, SAXException {
		List<ProtocolFormXmlData> pfxds = Lists.newArrayList();
		pfxds=protocolFormXmlDataDao.listProtocolformXmlDatasByType(ProtocolFormXmlDataType.ARCHIVE);
		//pfxds.add(protocolFormXmlDataDao.findById(8879));
		for (ProtocolFormXmlData pfxd : pfxds) {
			String xml = pfxd.getXmlData();
			Map<String, Object> resultMap = new HashMap<String, Object>(
					0);
	try {
				resultMap = xmlProcessor.deleteElementByPath(
						"/protocol/crimson/documents[@form=\"packets\"]", xml);
				xml = resultMap.get("finalXml").toString();
			} catch (Exception e) {
				logger.info("element does not exist.");
			}
	
			try{
			Document doc = xmlProcessor.loadXmlStringToDOM(xml);

			NodeList protocolList = doc.getElementsByTagName("protocol");
			Element protocolEle = (Element) protocolList.item(0);
			String irbNum = protocolEle.getAttribute("id");
			// doc obejct[] null, no packet for this study
			if (crimsonStudyDao.findAllPacketDocumentsByIRBNum(irbNum) == null) {

			} else {
				Element crimsonRootEle = doc.createElement("crimson");
				if ((Element) doc.getElementsByTagName("crimson").item(0) != null) {
					crimsonRootEle = (Element) doc.getElementsByTagName(
							"crimson").item(0);
				}
				protocolEle.appendChild(crimsonRootEle);
				logger.debug(irbNum);
				List<Object[]> ctObjList = crimsonStudyDao
						.findAllPacketDocumentsByIRBNum(irbNum);
				Element packetEle = doc.createElement("documents");
				packetEle.setAttribute("form", "packets");
				crimsonRootEle.appendChild(packetEle);
				// sort document list by parent first, then by version
				for (int j = 0; j < ctObjList.size(); j++) {
					for (int k = j + 1; k < ctObjList.size(); k++) {
						if ((Integer) ctObjList.get(j)[3] > (Integer) ctObjList
								.get(k)[3]) {
							Object[] tempObj = ctObjList.get(j);
							ctObjList.set(j, ctObjList.get(k));
							ctObjList.set(k, tempObj);
						}
					}
				}

				// second round sort
				for (int j = 0; j < ctObjList.size(); j++) {
					for (int k = j + 1; k < ctObjList.size(); k++) {
						int parent1 = (Integer) ctObjList.get(j)[3];
						int parent2 = (Integer) ctObjList.get(k)[3];
						if (parent1 == parent2) {
							if ((short) ctObjList.get(j)[2] < (short) ctObjList
									.get(k)[2]) {

								Object[] tempObj = ctObjList.get(j);
								ctObjList.set(j, ctObjList.get(k));
								ctObjList.set(k, tempObj);
							}
						}

					}
				}

				Map<Integer, Element> crimDocumentMap = new HashMap<Integer, Element>();
				for (int j = 0; j < ctObjList.size(); j++) {
					Element packetDocEle = doc.createElement("document");
					Object[] docObj = ctObjList.get(j);

					if (j == 0) {
						packetEle.appendChild(packetDocEle);
						crimDocumentMap.put((Integer) docObj[3], packetDocEle);
					}
					if (j > 0) {
						Object[] docObjPre = ctObjList.get(j - 1);
						short revisionID = 0;
						if (docObjPre[2] != null) {
							revisionID = (short) docObjPre[2];
						}
						if (revisionID == 0) {
							packetEle.appendChild(packetDocEle);
							crimDocumentMap.put((Integer) docObj[3],
									packetDocEle);
						} else {
							try {
								crimDocumentMap.get((Integer) docObj[3])
										.appendChild(packetDocEle);
							} catch (Exception e) {
								packetEle.appendChild(packetDocEle);
								crimDocumentMap.put((Integer) docObj[3],
										packetDocEle);

							}
						}

					}

					packetDocEle.setAttribute("id", "");
					if (docObj[0] != null) {
						packetDocEle.setAttribute("id", ""
								+ (Integer) docObj[0]);
					}
					packetDocEle.setAttribute("title", "");
					if (docObj[1] != null) {
						packetDocEle.setAttribute("title", (String) docObj[1]);
					}
					packetDocEle.setAttribute("version", "");
					if (docObj[2] != null) {
						packetDocEle.setAttribute("version", ""
								+ (short) docObj[2]);
					}
					packetDocEle.setAttribute("parent", "");
					if (docObj[3] != null) {
						packetDocEle.setAttribute("parent", ""
								+ (Integer) docObj[3]);
					}
					packetDocEle.setAttribute("date", "");
					if (docObj[4] != null) {
						packetDocEle.setAttribute("date",
								((Date) docObj[4]).toString());
					}
					packetDocEle.setAttribute("type", "");
					if (docObj[5] != null) {
						short typeID = (short) docObj[5];
						packetDocEle.setAttribute("type", crimsonStudyDao
								.findPacketDocumentTypeByID(typeID));
					}
					packetDocEle.setAttribute("is_postweb", "");
					if (docObj[7] != null) {
						short isPostweb = (short) docObj[7];
						if (isPostweb == 1) {
							packetDocEle.setAttribute("is_postweb", "y");
						} else if (isPostweb == 0) {
							packetDocEle.setAttribute("is_postweb", "n");
						}

					}

					String existingFormData = "";
					try {
						existingFormData = protocolFormXmlDataDao
								.getLastProtocolFormXmlDataByProtocolIdAndType(
										Long.valueOf(irbNum),
										ProtocolFormXmlDataType.ARCHIVE)
								.getXmlData();
					} catch (Exception e) {
						existingFormData = "";
					}

					Document existtingFormXmlDataDoc = null;

					if (existingFormData.isEmpty()) {
						existtingFormXmlDataDoc = doc;
					} else {
						try {
							existtingFormXmlDataDoc = xmlProcessor
									.loadXmlStringToDOM(existingFormData);
						} catch (SAXException e) {
						}
					}

					String hashFileName = "";
					if (existtingFormXmlDataDoc != null) {
						XPath xPath = getXPathInstance();

						XPathExpression xPathExpression = null;

						xPathExpression = xPath
								.compile("//protocol/crimson/documents[@form='"
										+ packetEle.getAttribute("form")
										+ "']/document[@id='"
										+ (Integer) docObj[0]
										+ "']/ariapath/text()");
						NodeList existingDocuemnts = (NodeList) xPathExpression
								.evaluate(existtingFormXmlDataDoc,
										XPathConstants.NODESET);

						if (existingDocuemnts.getLength() > 0) {
							hashFileName = existingDocuemnts.item(0)
									.getTextContent();
						} else {
							hashFileName = uploadCrimsonDocumenttoFileServer(
									"postdocs", (Integer) docObj[0],
									(String) docObj[6], irbNum);
						}
						Element ariapathEle = doc.createElement("ariapath");
						packetDocEle.appendChild(ariapathEle);
						ariapathEle.setTextContent(hashFileName);
					} else {
						hashFileName = uploadCrimsonDocumenttoFileServer(
								"postdocs", (Integer) docObj[0],
								(String) docObj[6], irbNum);
						Element ariapathEle = doc.createElement("ariapath");
						packetDocEle.appendChild(ariapathEle);
						ariapathEle.setTextContent(hashFileName);
					}
				}
			}
			
			xml = DomUtils.elementToString(doc, false, Encoding.UTF16);
			pfxd.setXmlData(xml);
			protocolFormXmlDataDao.saveOrUpdate(pfxd);
			}catch(Exception e){
				logger.debug("@@@@@@@@"+pfxd.getId());
			}
		}

	}
	
	private String uploadCrimsonDocumenttoFileServer(String path, int docID,
			String ext, String irbNum) {

		File fileDir = new File(localDirectory);
		if (!fileDir.exists())
			fileDir.mkdir();

		// copy the file from aria server to local
		String fileName = "X://" + path + "/" + docID + "." + ext;
		String hashFileName = "";
		String uploadfilename = "";
		try {
			messageDigest = MessageDigest.getInstance("SHA-256",
					new org.bouncycastle.jce.provider.BouncyCastleProvider());

			InputStream fileContent = new FileInputStream(fileName);

			byte[] bytes = IOUtils.toByteArray(fileContent);

			messageDigest.update(bytes);

			// get the hash of file content
			hashFileName = new String(Hex.encode(messageDigest.digest()));

			// write the file to local

			uploadfilename = localDirectory + "/" + hashFileName + "." + ext;

			FileOutputStream fout = new FileOutputStream(uploadfilename);
			fout.write(bytes);
			fout.flush();
			fout.close();

			// upload file to the server
			int trySFTP = 1;
			while (trySFTP > 0) {
				try {

					sFTPService.uploadLocalFileToRemote("protocol/" + irbNum
							+ "/" + hashFileName + "." + ext);

					trySFTP = 0;
				} catch (Exception e) {
					if (trySFTP < 5) {
						trySFTP++;
					}
					if (trySFTP == 5) {
						BufferedReader input = new BufferedReader(
								new FileReader("C:\\Data\\SFTPMissedList.txt"));
						String existData = "";
						String newData = "";
						while ((existData = input.readLine()) != null) {
							newData += existData + "\n";
						}
						input.close();
						newData += fileName;
						BufferedWriter output = new BufferedWriter(
								new FileWriter("C:\\Data\\SFTPMissedList.txt"));
						output.write(newData);
						output.close();
					}

				}
			}
			// delete the file after uploading...
			File uploadedFile = new File(uploadfilename);
			uploadedFile.delete();
		} catch (Exception e) {
		}

		// remove template dir
		fileDir.delete();

		// dev

		/*
		 * return "https://" + fileServerHost + "/files/dev/protocol/" + irbNum
		 * + "/" + hashFileName + "." + ext;
		 */

		// production

		return "https://" + fileServerHost + "/files/protocol/" + irbNum + "/"
				+ hashFileName + "." + ext;

	}

	// append letterback to it
	private Document appendExistingLetters(Document newDoc, Document existingDoc)
			throws XPathExpressionException {
		XPath xPath = getXPathInstance();

		XPathExpression xPathExpression = null;
		xPathExpression = xPath.compile("//protocol/letters");
		NodeList lettersnodes = (NodeList) xPathExpression.evaluate(
				existingDoc, XPathConstants.NODESET);
		if (lettersnodes.getLength() > 0) {
			Element protocolEleInNew = (Element) newDoc.getElementsByTagName(
					"protocol").item(0);
			Element lettersNodesNew = newDoc.createElement("letters");
			protocolEleInNew.appendChild(lettersNodesNew);
			NodeList letterList = ((Element) lettersnodes.item(0))
					.getElementsByTagName("letter");
			for (int i = 0; i < letterList.getLength(); i++) {
				Element oldLetterNode = (Element) letterList.item(i);
				Element letterNode = newDoc.createElement("letter");
				lettersNodesNew.appendChild(letterNode);

				letterNode.setAttribute("date",
						oldLetterNode.getAttribute("date"));
				letterNode.setAttribute("from",
						oldLetterNode.getAttribute("from"));
				letterNode.setAttribute("path",
						oldLetterNode.getAttribute("path"));
				letterNode.setAttribute("to", oldLetterNode.getAttribute("to"));
				letterNode.setAttribute("type",
						oldLetterNode.getAttribute("type"));

			}
		}
		return newDoc;

	}

	// @Test
	public void copyForm() {
		long protocolID = 201658;
		long protocolNewID = 110396;
		// long newProtocol
		List<ProtocolForm> formList = protocolFormDao
				.listProtocolFormsByProtocolId(protocolID);
		Protocol protocol = new Protocol();
		protocol = protocolDao.findById(protocolNewID);
		for (int i = 0; i < formList.size(); i++) {

			ProtocolForm protocolForm = formList.get(i);

			ProtocolForm protocolFormCopy = new ProtocolForm();
			protocolFormCopy.setCreated(protocolForm.getCreated());
			protocolFormCopy.setLocked(false);
			protocolFormCopy.setMetaDataXml(protocolForm.getMetaDataXml());
			protocolFormCopy.setParent(protocolFormCopy);
			protocolFormCopy.setProtocol(protocol);
			protocolFormCopy.setProtocolFormType(ProtocolFormType.MODIFICATION);
			protocolFormCopy.setRetired(false);
			protocolFormDao.saveOrUpdate(protocolFormCopy);

			ProtocolFormStatus protocolFormStatus = protocolFormStatusDao
					.getLatestProtocolFormStatusByFormId(protocolForm
							.getFormId());
			ProtocolFormStatus protocolFormStatusCopy = new ProtocolFormStatus();
			protocolFormStatusCopy.setCauseByUser(protocolFormStatus
					.getCauseByUser());
			protocolFormStatusCopy.setCausedByCommittee(protocolFormStatus
					.getCausedByCommittee());
			protocolFormStatusCopy
					.setModified(protocolFormStatus.getModified());
			protocolFormStatusCopy.setProtocolForm(protocolFormCopy);
			protocolFormStatusCopy
					.setProtocolFormStatus(ProtocolFormStatusEnum.DRAFT);
			protocolFormStatusCopy.setRetired(false);
			protocolFormStatusDao.saveOrUpdate(protocolFormStatusCopy);

			List<ProtocolFormXmlData> formXmlDataList = protocolFormXmlDataDao
					.listProtocolformXmlDatasByFormId(protocolForm.getFormId());
			for (int j = 0; j < formXmlDataList.size(); j++) {
				ProtocolFormXmlData protocolFormXmlData = formXmlDataList
						.get(j);
				ProtocolFormXmlData protocolFormXmlDataCopy = new ProtocolFormXmlData();

				protocolFormXmlDataCopy.setCreated(protocolFormXmlData
						.getCreated());
				protocolFormXmlDataCopy.setParent(protocolFormXmlDataCopy);
				protocolFormXmlDataCopy.setProtocolForm(protocolFormCopy);
				if (protocolFormXmlData.getProtocolFormXmlDataType().equals(
						ProtocolFormXmlDataType.PROTOCOL))
					protocolFormXmlDataCopy
							.setProtocolFormXmlDataType(ProtocolFormXmlDataType.MODIFICATION);
				else
					protocolFormXmlDataCopy
							.setProtocolFormXmlDataType(protocolFormXmlData
									.getProtocolFormXmlDataType());
				protocolFormXmlDataCopy.setRetired(false);
				protocolFormXmlDataCopy.setXmlData(protocolFormXmlData
						.getXmlData());
				protocolFormXmlDataDao.saveOrUpdate(protocolFormXmlDataCopy);

			}

		}

		// access add
		SecurableObject securableObject = securableObjectDao
				.getSecurableObjectByClassAndId(Protocol.class, protocolID);

		SecurableObject securableObjectCopy = new SecurableObject();
		securableObjectCopy.setObjectClass(Protocol.class);
		securableObjectCopy.setObjectId(protocolNewID);
		securableObjectCopy.setObjectIdExpression("NULL");
		securableObjectCopy.setRetired(false);
		securableObjectCopy.setUseObjectIdExpression(false);
		securableObjectDao.saveOrUpdate(securableObjectCopy);

		List<SecurableObjectAcl> secureACLList = securableObjectAclDao
				.getSecurableObjectaclBysecurableObejctID(securableObject
						.getId());
		for (int i = 0; i < secureACLList.size(); i++) {
			SecurableObjectAcl securableObjectAcl = secureACLList.get(i);
			SecurableObjectAcl securableObjectAclCopy = new SecurableObjectAcl();
			securableObjectAclCopy.setOwnerClass(User.class);
			securableObjectAclCopy.setOwnerId(securableObjectAcl.getOwnerId());
			securableObjectAclCopy.setPermission(securableObjectAcl
					.getPermission());
			securableObjectAclCopy.setRetired(false);
			securableObjectAclCopy.setSecurableObject(securableObjectCopy);
			securableObjectAclDao.saveOrUpdate(securableObjectAclCopy);
		}
	}

	// before running ,make sure file path for database,protocoldao, and
	// test-content
	// @Test
	public void singleMigrationtest() throws XPathExpressionException,
			ParserConfigurationException, SAXException, IOException {

		List<String> existedUserList = new ArrayList<String>();
		DocumentBuilderFactory domFactory = DocumentBuilderFactory
				.newInstance();
		domFactory.setNamespaceAware(true);
		// never forget this!
		DocumentBuilder builder = domFactory.newDocumentBuilder();
		Document doc = builder.parse("C:\\Data\\test.xml");

		String megrateXml = DomUtils.elementToString(doc, false);

		// logger.debug(megrateXml);
		migrationServiceImpl.saveMegrateDataAsProtocol(megrateXml,
				existedUserList);

	}

	// @Test
	public void addPermission() throws IOException, XPathExpressionException {

		FileInputStream fstream;
		try {
			fstream = new FileInputStream("C:\\Data\\Permission.txt");
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			while ((strLine = br.readLine()) != null) {
				long protocolID = Long.valueOf(strLine);
				logger.debug("" + protocolID);
				String xml = protocolDao.findById(protocolID).getMetaDataXml();
				XPath xPath = getXPathInstance();
				XPathExpression xPathExpression = null;
				xPathExpression = xPath.compile("//protocol/staffs/staff/user");

				try {

					Document doc = xmlProcessor.loadXmlStringToDOM(xml);

					NodeList nodes = (NodeList) xPathExpression.evaluate(doc,
							XPathConstants.NODESET);
					for (int i = 0; i < nodes.getLength(); i++) {
						Element userEle = (Element) nodes.item(i);
						if (!userEle.getAttribute("id").isEmpty()) {
							xPathExpression = xPath
									.compile("/protocol/staffs/staff/user[@id='"
											+ userEle.getAttribute("id")
											+ "']/roles/role/text()");

							/*
							 * String writePermissionRoleCheck =
							 * "boolean(count(/protocol/staffs/staff/user[@id='"
							 * + userEle.getAttribute("id") +
							 * "']/roles[role[contains(fn:upper-case(.),\"PRINCIPAL INVESTIGATOR\")] or role[contains(fn:upper-case(.),\"PRIMARY CONTACT\")] or role[contains(fn:upper-case(.),\"STUDY COORDINATOR\")]])>0)"
							 * ;
							 * 
							 * String formerRoleCheck =
							 * "boolean(count(/protocol/staffs/staff/user[@id='"
							 * + userEle.getAttribute("id") +
							 * "']/roles[role[contains(fn:upper-case(.),\"FORMER\")]])>0)"
							 * ;
							 * 
							 * Boolean wirtePermissionRolesInvovled = (Boolean)
							 * (xPath .evaluate( writePermissionRoleCheck, doc,
							 * XPathConstants.BOOLEAN));
							 * 
							 * xPath.reset();
							 * 
							 * Boolean formerStaffRolesInvovled = (Boolean)
							 * (xPath .evaluate( formerRoleCheck, doc,
							 * XPathConstants.BOOLEAN));
							 * 
							 * if (!formerStaffRolesInvovled){ if
							 * (wirtePermissionRolesInvovled){
							 * permissions.add(Permission.WRITE); }
							 * 
							 * permissions.add(Permission.READ);
							 * objectAclService
							 * .updateObjectAclByUserAndPermissions(
							 * Protocol.class, protocolID,
							 * userDao.findById(Long.valueOf(userEle
							 * .getAttribute("id"))), permissions); }
							 */

							Set<Permission> permissions = new HashSet<Permission>();
							NodeList roleList = (NodeList) xPathExpression
									.evaluate(doc, XPathConstants.NODESET);
							int formerStaffTag = 0;
							for (int j = 0; j < roleList.getLength(); j++) {
								String role = roleList.item(j).getNodeValue()
										.trim();
								if (role.toLowerCase().equals(
										"principal investigator")
										|| role.toLowerCase().equals(
												"primary contact")
										|| role.toLowerCase().equals(
												"responsible staff")
										|| role.toLowerCase().equals(
												"budget manager")
										|| role.toLowerCase().equals(
												"study coordinator")) {
									permissions.add(Permission.WRITE);
								}
								if (role.toLowerCase().contains("former")) {
									formerStaffTag = 1;

								}
							}

							if (formerStaffTag == 1) {
								continue;
							}
							permissions.add(Permission.READ);
							objectAclService
									.updateObjectAclByUserAndPermissions(
											Protocol.class,
											protocolID,
											userDao.findById(Long.valueOf(userEle
													.getAttribute("id"))),
											permissions);

						}
					}

				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
			in.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

	}

	// @Test
	public void addPermissionToStudiesByProtocolIdAndUserId()
			throws IOException, XPathExpressionException {

		FileInputStream fstream;
		try {
			fstream = new FileInputStream("C:\\Data\\Permission.txt");
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;

			Set<Permission> permissions = new HashSet<Permission>();
			permissions.add(Permission.WRITE);
			permissions.add(Permission.READ);

			User user = userDao.findById(2248l);

			while ((strLine = br.readLine()) != null) {
				long protocolID = Long.valueOf(strLine);
				logger.debug("" + protocolID);

				objectAclService.updateObjectAclByUserAndPermissions(
						Protocol.class, protocolID, user, permissions);
			}
			in.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

	}

	// @Test
	public void errorListMigrationTest() throws ParserConfigurationException,
			IOException {
		FileInputStream fstream;
		DocumentBuilderFactory domFactory = DocumentBuilderFactory
				.newInstance();
		domFactory.setNamespaceAware(true);
		DocumentBuilder builder = domFactory.newDocumentBuilder();
		List<String> existedUserList = new ArrayList<String>();

		fstream = new FileInputStream("C:\\Data\\4-19errorList.txt");
		DataInputStream in = new DataInputStream(fstream);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String strLine = "";
		while ((strLine = br.readLine()) != null) {

			try {

				logger.debug("Process protocol: " + strLine);

				Document doc = builder.parse("C:\\Data\\AriaXmls-4-19\\"
						+ strLine + ".xml");
				String megrateXml = DomUtils.elementToString(doc, false,
						Encoding.UTF8);
				existedUserList = migrationServiceImpl
						.saveMegrateDataAsProtocol(megrateXml, existedUserList);
			} catch (Exception e) {
				e.printStackTrace();
				BufferedReader input = new BufferedReader(new FileReader(
						"C:\\Data\\error-study.txt"));
				String existData = "";
				String newData = "";
				while ((existData = input.readLine()) != null) {
					newData += existData + "\n";
				}
				input.close();
				BufferedWriter output = new BufferedWriter(new FileWriter(
						"C:\\Data\\error-study.txt"));
				newData += "" + strLine;
				output.write(newData);
				output.close();
				continue;
			}
		}
	}

	// @Test
	public void multipleMigrationTest() throws XPathExpressionException,
			ParserConfigurationException, SAXException, IOException {

		DocumentBuilderFactory domFactory = DocumentBuilderFactory
				.newInstance();
		domFactory.setNamespaceAware(true);
		DocumentBuilder builder = domFactory.newDocumentBuilder();
		List<String> existedUserList = new ArrayList<String>();

		FileInputStream fstream;
		try {
			fstream = new FileInputStream("C:\\Data\\migrationList.txt");
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			while ((strLine = br.readLine()) != null) {
				logger.debug("processing: " + strLine);
				try {
					Document doc = builder.parse("C:\\Data\\migrate\\"
							+ strLine + ".xml");
					String megrateXml = DomUtils.elementToString(doc, false,
							Encoding.UTF8);
					existedUserList = migrationServiceImpl
							.saveMegrateDataAsProtocol(megrateXml,
									existedUserList);

				} catch (Exception e) {
					e.printStackTrace();
					BufferedReader input = new BufferedReader(new FileReader(
							"C:\\Data\\error-study.txt"));
					String existData = "";
					String newData = "";
					while ((existData = input.readLine()) != null) {
						newData += existData + "\n";
					}
					input.close();
					BufferedWriter output = new BufferedWriter(new FileWriter(
							"C:\\Data\\error-study.txt"));
					newData += "" + strLine;
					output.write(newData);
					output.close();
					continue;
				}
			}
		} catch (Exception e) {

		}
	}

	// @Test
	public void replaceInConsistentID() throws IOException,
			XPathExpressionException {
		List<Protocol> protocolList = protocolDao.findAll();

		for (int i = 0; i < protocolList.size(); i++) {
			Protocol protocol = protocolList.get(i);
			// Protocol protocol= protocolDao.findById(protocolIDlist.get(i));
			String protocoXxmlData = protocol.getMetaDataXml();
			Document protocolDoc = null;
			try {
				protocolDoc = xmlProcessor.loadXmlStringToDOM(protocoXxmlData);
			} catch (SAXException e) {
				e.printStackTrace();
			}
			XPath xPath = getXPathInstance();
			XPathExpression xPathExpression = null;
			xPathExpression = xPath.compile("/protocol");

			NodeList protocolEleList = (NodeList) xPathExpression.evaluate(
					protocolDoc, XPathConstants.NODESET);
			if (protocolEleList.getLength() == 0) {
				continue;
			}
			Element protocolEle = (Element) protocolEleList.item(0);
			String protocolID = protocolEle.getAttribute("id");
			long protocolIDLong = Long.valueOf(protocolID);
			// if(protocolIDLong!=protocol.getId()){
			protocolEle.setAttribute("id", "" + protocol.getId());
			protocolEle.setAttribute("identifier", "" + protocol.getId());
			// }
			String newProtocoXML = DomUtils.elementToString(protocolDoc, false,
					Encoding.UTF16);
			protocol.setMetaDataXml(newProtocoXML);
			protocolDao.saveOrUpdate(protocol);

			// loop to get all form
			List<ProtocolForm> formList = protocolFormDao
					.listProtocolFormsByProtocolId(protocol.getId());
			for (int j = 0; j < formList.size(); j++) {
				ProtocolForm protocolForm = formList.get(j);
				String protocoFromXxmlData = protocolForm.getMetaDataXml();
				Document protocolFormDoc = null;
				try {
					protocolFormDoc = xmlProcessor
							.loadXmlStringToDOM(protocoFromXxmlData);
				} catch (SAXException e) {
					e.printStackTrace();
				}
				xPath = getXPathInstance();
				xPathExpression = xPath.compile("/protocol");

				NodeList protocolFormEleList = (NodeList) xPathExpression
						.evaluate(protocolFormDoc, XPathConstants.NODESET);

				if (protocolFormEleList.getLength() == 0) {
					continue;
				}
				Element protocolFormEle = (Element) protocolFormEleList.item(0);
				String protocolIDinForm = protocolFormEle.getAttribute("id");
				// String protocolIdentifier =
				// protocolEle.getAttribute("identifier");
				long protocolIDinFormLong = Long.valueOf(protocolIDinForm);
				// if(protocolIDinFormLong!=protocol.getId()){
				protocolFormEle.setAttribute("id", "" + protocol.getId());
				protocolFormEle.setAttribute("identifier",
						"" + protocol.getId());
				// }
				String newProtocoFormXML = DomUtils.elementToString(
						protocolFormDoc, false, Encoding.UTF16);
				protocolForm.setMetaDataXml(newProtocoFormXML);
				protocolFormDao.saveOrUpdate(protocolForm);

				List<ProtocolFormXmlData> formXMLDataList = protocolFormXmlDataDao
						.listProtocolformXmlDatasByFormId(protocolForm
								.getFormId());

				for (int k = 0; k < formXMLDataList.size(); k++) {
					ProtocolFormXmlData protocolFormXmlData = formXMLDataList
							.get(k);
					String protocoFromxmlDataMetaData = protocolFormXmlData
							.getXmlData();
					// xmlStringList.add(protocoXxmlData);
					Document protocolFormXMLDataDoc = null;
					try {
						protocolFormXMLDataDoc = xmlProcessor
								.loadXmlStringToDOM(protocoFromxmlDataMetaData);
					} catch (SAXException e) {
						e.printStackTrace();
					}
					xPath = getXPathInstance();
					xPathExpression = xPath.compile("/protocol");

					NodeList protocolFormXMLDATAEleList = (NodeList) xPathExpression
							.evaluate(protocolFormXMLDataDoc,
									XPathConstants.NODESET);
					if (protocolFormXMLDATAEleList.getLength() == 0) {
						continue;
					}
					Element protocolFormXMLDataEle = (Element) protocolFormXMLDATAEleList
							.item(0);
					String protocolIDinFormXMLData = protocolFormXMLDataEle
							.getAttribute("id");
					// String protocolIdentifier =
					// protocolEle.getAttribute("identifier");
					long protocolIDinFormXMLDataLong = Long
							.valueOf(protocolIDinFormXMLData);
					// if(protocolIDinFormXMLDataLong!=protocol.getId()){
					protocolFormXMLDataEle.setAttribute("id",
							"" + protocol.getId());
					protocolFormXMLDataEle.setAttribute("identifier", ""
							+ protocol.getId());
					// }
					String newProtocoFormXMLDataMeta = DomUtils
							.elementToString(protocolFormXMLDataDoc, false,
									Encoding.UTF16);
					protocolFormXmlData.setXmlData(newProtocoFormXMLDataMeta);
					protocolFormXmlDataDao.saveOrUpdate(protocolFormXmlData);
				}// end for 3

			}// end for 2
		}// end for 1

	}

	// @Test
	public void addUserIDForMissedUsers() throws NumberFormatException,
			DOMException, IOException, XPathExpressionException {
		String rawdata = null;
		BufferedReader reader = new BufferedReader(new FileReader(
				"C:\\DOCUME~1\\yuanjiawei\\Desktop\\newUser.txt"));
		String piserial = "16064";
		while ((rawdata = reader.readLine()) != null) {

			long IRBNum = Integer.valueOf(rawdata);
			logger.debug(IRBNum + "");
			ProtocolFormXmlData protocolFormXmlData = null;
			ProtocolForm protocolForm = null;
			Protocol protocol = null;

			try {
				protocolFormXmlData = protocolFormXmlDataDao
						.getLastProtocolFormXmlDataByProtocolIdAndType(IRBNum,
								ProtocolFormXmlDataType.ARCHIVE);
				protocolForm = protocolFormDao
						.getLatestProtocolFormByProtocolIdAndProtocolFormType(
								IRBNum, ProtocolFormType.ARCHIVE);
				protocol = protocolDao.findById(IRBNum);
			} catch (Exception e) {

			}
			if (protocolFormXmlData == null) {
				continue;
			}

			List<String> xmlStrings = new ArrayList<String>();
			xmlStrings.add(protocolFormXmlData.getXmlData());
			xmlStrings.add(protocolForm.getMetaDataXml());
			xmlStrings.add(protocol.getMetaDataXml());

			// if Letters ele does exist, create one

			for (int j = 0; j < xmlStrings.size(); j++) {
				Document doc = null;
				try {
					doc = xmlProcessor.loadXmlStringToDOM(xmlStrings.get(j));
				} catch (SAXException e) {
					e.printStackTrace();
				}
				XPath xPath = getXPathInstance();
				XPathExpression xPathExpression = null;

				xPathExpression = xPath
						.compile("/protocol/staffs/staff/user[@pi_serial='"
								+ piserial + "']/email/text()");

				NodeList emailist = (NodeList) xPathExpression.evaluate(doc,
						XPathConstants.NODESET);
				String email = emailist.item(0).getNodeValue();

				xPathExpression = xPath
						.compile("/protocol/staffs/staff/user[@pi_serial='"
								+ piserial + "']");

				NodeList userList = (NodeList) xPathExpression.evaluate(doc,
						XPathConstants.NODESET);

				for (int i = 0; i < userList.getLength(); i++) {
					Element userNode = (Element) userList.item(i);
					userNode.setAttribute("id",
							"" + userDao.getUserByEmail(email).getId());
				}

				String newContractFormXmldata = DomUtils.elementToString(doc,
						false, Encoding.UTF16);
				if (j == 0) {
					protocolFormXmlData.setXmlData(newContractFormXmldata);
					protocolFormXmlDataDao.saveOrUpdate(protocolFormXmlData);
				} else if (j == 1) {
					protocolForm.setMetaDataXml(newContractFormXmldata);
					protocolFormDao.saveOrUpdate(protocolForm);
				}
				if (j == 2) {
					protocol.setMetaDataXml(newContractFormXmldata);
					protocolDao.saveOrUpdate(protocol);
				}

			}
		}
	}

	// @Test
	public void getPiSerialofMissedUsers() throws IOException {
		String firstName = "";
		String lastName = "";
		FileInputStream fstream;

		fstream = new FileInputStream("C:\\Data\\erroeUserList.txt");
		DataInputStream in = new DataInputStream(fstream);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String strLine = "";
		while ((strLine = br.readLine()) != null) {
			String[] splitedData = strLine.split(",");
			lastName = splitedData[1];
			firstName = splitedData[2];
			int pi_serial = crimsonStudyDao.findPiSerialByfirstandLastName(
					firstName, lastName);
			BufferedReader input = new BufferedReader(new FileReader(
					"C:\\Data\\erroeUserList2.txt"));
			String existData = "";
			String newData = "";
			while ((existData = input.readLine()) != null) {
				newData += existData + "\n";
			}
			input.close();
			BufferedWriter output = new BufferedWriter(new FileWriter(
					"C:\\Data\\erroeUserList2.txt"));
			newData += pi_serial + "," + strLine;
			output.write(newData);
			output.close();
		}

	}

	public MigrationServiceImpl getMigrationServiceImpl() {
		return migrationServiceImpl;
	}

	@Autowired(required = true)
	public void setMigrationServiceImpl(
			MigrationServiceImpl migrationServiceImpl) {
		this.migrationServiceImpl = migrationServiceImpl;
	}

	public ProtocolFormDao getProtocolFormDao() {
		return protocolFormDao;
	}

	@Autowired(required = true)
	public void setProtocolFormDao(ProtocolFormDao protocolFormDao) {
		this.protocolFormDao = protocolFormDao;
	}

	public ProtocolFormStatusDao getProtocolFormStatusDao() {
		return protocolFormStatusDao;
	}

	@Autowired(required = true)
	public void setProtocolFormStatusDao(
			ProtocolFormStatusDao protocolFormStatusDao) {
		this.protocolFormStatusDao = protocolFormStatusDao;
	}

	public ProtocolFormXmlDataDao getProtocolFormXmlDataDao() {
		return protocolFormXmlDataDao;
	}

	@Autowired(required = true)
	public void setProtocolFormXmlDataDao(
			ProtocolFormXmlDataDao protocolFormXmlDataDao) {
		this.protocolFormXmlDataDao = protocolFormXmlDataDao;
	}

	public ProtocolDao getProtocolDao() {
		return protocolDao;
	}

	@Autowired(required = true)
	public void setProtocolDao(ProtocolDao protocolDao) {
		this.protocolDao = protocolDao;
	}

	public SecurableObjectDao getSecurableObjectDao() {
		return securableObjectDao;
	}

	@Autowired(required = true)
	public void setSecurableObjectDao(SecurableObjectDao securableObjectDao) {
		this.securableObjectDao = securableObjectDao;
	}

	public SecurableObjectAclDao getSecurableObjectAclDao() {
		return securableObjectAclDao;
	}

	@Autowired(required = true)
	public void setSecurableObjectAclDao(
			SecurableObjectAclDao securableObjectAclDao) {
		this.securableObjectAclDao = securableObjectAclDao;
	}

	public CrimsonStudyDao getCrimsonStudyDao() {
		return crimsonStudyDao;
	}

	@Autowired(required = true)
	public void setCrimsonStudyDao(CrimsonStudyDao crimsonStudyDao) {
		this.crimsonStudyDao = crimsonStudyDao;
	}

	public UserDao getUserDao() {
		return userDao;
	}

	@Autowired(required = true)
	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}

	public ObjectAclServiceImpl getObjectAclService() {
		return objectAclService;
	}

	@Autowired(required = true)
	public void setObjectAclService(ObjectAclServiceImpl objectAclService) {
		this.objectAclService = objectAclService;
	}

	public XPathFactory getXpathFactory() {
		return xpathFactory;
	}

	public void setXpathFactory(XPathFactory xpathFactory) {
		this.xpathFactory = xpathFactory;
	}

	public XmlProcessor getXmlProcessor() {
		return xmlProcessor;
	}

	@Autowired(required = true)
	public void setXmlProcessor(XmlProcessor xmlProcessor) {
		this.xmlProcessor = xmlProcessor;
	}

	public SFTPService getsFTPService() {
		return sFTPService;
	}
	@Autowired(required = true)
	public void setsFTPService(SFTPService sFTPService) {
		this.sFTPService = sFTPService;
	}

}
