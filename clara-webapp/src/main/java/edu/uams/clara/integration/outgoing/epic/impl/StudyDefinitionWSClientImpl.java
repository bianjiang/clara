package edu.uams.clara.integration.outgoing.epic.impl;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.client.core.WebServiceMessageCallback;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.soap.SoapHeader;
import org.springframework.ws.soap.SoapHeaderElement;
import org.springframework.ws.soap.SoapMessage;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import edu.uams.clara.core.util.xml.DomUtils;
import edu.uams.clara.core.util.xml.XMLTemplateField;
import edu.uams.clara.core.util.xml.XMLTemplateFieldFactory;
import edu.uams.clara.core.util.xml.XmlHandler;
import edu.uams.clara.core.util.xml.XmlHandlerFactory;
import edu.uams.clara.integration.outgoing.epic.StudyDefinitionWSClient;
import edu.uams.clara.webapp.common.domain.usercontext.User;
import edu.uams.clara.webapp.common.service.UserService;
import edu.uams.clara.webapp.common.service.audit.AuditService;

public class StudyDefinitionWSClientImpl implements StudyDefinitionWSClient {

	private final static Logger logger = LoggerFactory
			.getLogger(StudyDefinitionWSClientImpl.class);

	private ResourceLoader resourceLoader;

	private String retrieveProtocolDefResponseRequestXmlTemplateUri;

	private String retrieveProtocolPIXmlTemplateUri;

	private String retrieveProtocolNoPIUserXmlTemplateUri;
	
	private String retrieveProtocolNctNumberTemplateUri;

	private String uri;

	private WebServiceTemplate webServiceTemplate;

	private String hl7RootId;

	private AuditService auditService;
	
	private UserService userService;
	
	private boolean shouldRun = false;


	private EntityManager em;

	private final List<XMLTemplateField> studyDefXMLTemplateFields = Lists
			.newArrayList();
	{
		studyDefXMLTemplateFields.add(XMLTemplateFieldFactory
				.newAttirbuteTemplate("irbNumber",
						"/RetrieveProtocolDefResponse/query", "root"));
		studyDefXMLTemplateFields
				.add(XMLTemplateFieldFactory
						.newAttirbuteTemplate(
								"hl7RootId",
								"/RetrieveProtocolDefResponse/protocolDef/plannedStudy/id",
								"root"));
		studyDefXMLTemplateFields
				.add(XMLTemplateFieldFactory
						.newAttirbuteTemplate(
								"irbNumber",
								"/RetrieveProtocolDefResponse/protocolDef/plannedStudy/id",
								"extension"));
		studyDefXMLTemplateFields.add(XMLTemplateFieldFactory.newNodeTemplate(
				"epicTitle",
				"/RetrieveProtocolDefResponse/protocolDef/plannedStudy/title"));
		studyDefXMLTemplateFields.add(XMLTemplateFieldFactory.newNodeTemplate(
				"epicSummary",
				"/RetrieveProtocolDefResponse/protocolDef/plannedStudy/text"));

		// appending user info element to plannedStudy, the last parameter
		// defines whether we do replace or append
		studyDefXMLTemplateFields.add(XMLTemplateFieldFactory.newNodeTemplate(
				"epicUserInfo",
				"/RetrieveProtocolDefResponse/protocolDef/plannedStudy", true));
	}

	// the only difference between pi and non-PI is just the template, don't
	// need to define this xmltemplatefields for each.
	private final List<XMLTemplateField> studyDefXMLUserInfoTemplateFields = Lists
			.newArrayList();
	{
		studyDefXMLUserInfoTemplateFields.add(XMLTemplateFieldFactory
				.newAttirbuteTemplate("role",
						"/subjectOf/studyCharacteristic/code", "code"));

		studyDefXMLUserInfoTemplateFields.add(XMLTemplateFieldFactory
				.newAttirbuteTemplate("providerCode",
						"/subjectOf/studyCharacteristic/value", "code"));
	}
	
	private final List<XMLTemplateField> studyDefXMLNCTNumberInfoTemplateFields = Lists
			.newArrayList();
	{
		studyDefXMLNCTNumberInfoTemplateFields.add(XMLTemplateFieldFactory
				.newAttirbuteTemplate("nctNumber",
						"/subjectOf/studyCharacteristic/value", "value"));

	}

	/***
	 * <RetrieveProtocolDefResponse xmlns="urn:ihe:qrph:rpe:2009"> <query
	 * extension="1.2.5.2.3.4" root="99075"/> <protocolDef> <plannedStudy
	 * classCode="CLNTRL" moodCode="DEF" ITSVersion="XML_1.0"
	 * xmlns="urn:hl7-org:v3"> <id root="542951" extension="99075"/> <title>A
	 * Preliminary Study Utilizing a Flexible Endoscope for Pelvic
	 * Culdoscopy</title> <text>PI: Alexander Burnett; Description: A
	 * Preliminary Study Utilizing a Flexible Endoscope for Pelvic
	 * Culdoscopy.</text> </plannedStudy> </protocolDef>
	 * </RetrieveProtocolDefResponse>
	 * 
	 * @param irbNumber
	 * @param epicTitle
	 * @param epicSummary
	 * @return
	 * @throws ParserConfigurationException
	 * @throws IOException
	 * @throws SAXException
	 * @throws XPathExpressionException
	 */
	private Source createRetrieveProtocolDefResponseRequestPayload(
			String irbNumber, String epicTitle, String epicSummary,
			String epicUserInfo) throws ParserConfigurationException,
			IOException, SAXException, XPathExpressionException {
		Resource xmlFileResource = resourceLoader
				.getResource(retrieveProtocolDefResponseRequestXmlTemplateUri);
		XmlHandler xmlHandler = XmlHandlerFactory.newXmlHandler();

		Map<String, String> values = Maps.newHashMap();
		values.put("hl7RootId", hl7RootId);
		values.put("irbNumber", irbNumber);
		values.put("epicTitle", irbNumber + " " + epicTitle);
		values.put("epicSummary", epicSummary);
		values.put("epicUserInfo", epicUserInfo);

		return xmlHandler.replaceTemplateFields(xmlFileResource.getFile(),
				XMLTemplateFieldFactory.setValues(studyDefXMLTemplateFields,
						values));

	}

	private String getProtocolUserInfoXml(String xmlData)
			throws ParserConfigurationException, XPathExpressionException,
			SAXException, IOException {
		Resource piUserTemplateFile = resourceLoader
				.getResource(retrieveProtocolPIXmlTemplateUri);
		Resource noPIUserTemplateFile = resourceLoader
				.getResource(retrieveProtocolNoPIUserXmlTemplateUri);
		Resource nctNumberTemplateFile = resourceLoader
				.getResource(retrieveProtocolNctNumberTemplateUri);

		XmlHandler xmlHandler = XmlHandlerFactory.newXmlHandler();

		List<Element> userElements = xmlHandler.listElementsByXPath(xmlData,
				"/protocol/staffs/staff/user");

		Document doc = xmlHandler.newDocument();
		
		Element root = doc.createElement("user-info-temp");

		XPath xPathInstance = xmlHandler.newXPathInstance();

		for (Element userElement : userElements) {
			xPathInstance.reset(); // have to do this

			String role = "";
			boolean isPI = false;

			List<String> roleList = xmlHandler.getStringValuesByXPath(
					DomUtils.elementToString(userElement), "/user/roles/role");

			if (roleList.contains("Principal Investigator")
					|| roleList.contains("principal investigator")) {
				isPI = true;
			}
			
			
			
			User user = new User();
			String sap = "";
			String userIdStr = userElement.getAttribute("id");
			if(userIdStr==null || userIdStr.isEmpty() || userIdStr.equals("0")){
				try{
					Node emailNode = userElement.getElementsByTagName("Email").item(0);
					user=userService.getUserByEmail(emailNode.getTextContent());
					sap = user.getPerson().getSap();
				}
				catch(Exception e){
					//do nothing
				}
			} else {
				long userId = Long.valueOf(userIdStr);
				user = userService.getUserByUserId(userId);
				sap = user.getPerson().getSap();
			}

			// if no sap, ignore this user
			if (sap == null || sap.trim().isEmpty() || sap.equals("null")) {
				continue;
			}

			if (isPI) {
				role = "Principal Investigator";
				// query from se table
				/*
				String qry = "SELECT [provider_id] FROM [clara].[dbo].[epic_pi_id] where sap_id =:sap";
				Query query = em.createNativeQuery(qry);
				query.setParameter("sap", sap);
				try {
					providerCode = (String) query.getSingleResult();
				} catch (Exception e) {
					// no mapped provider found
					continue;
				}*/

				Map<String, String> values = Maps.newHashMap();
				sap = String.format("%08d", Integer.parseInt(sap));
				values.put("providerCode", sap);
				values.put("role", role);
				// values.put("codeSystem", codeSystem);
				Source source = xmlHandler.replaceTemplateFields(
						piUserTemplateFile.getFile(), XMLTemplateFieldFactory
								.setValues(studyDefXMLUserInfoTemplateFields,
										values));

				// find the reference
				Document userDoc = xmlHandler.parse(DomUtils.toString(source));
				
				root.appendChild(doc.importNode(userDoc.getDocumentElement(),
						true));
				role = "Study Coordinator";
				values.clear();
				values.put("providerCode", sap);
				values.put("role", role);
				source = xmlHandler.replaceTemplateFields(
						noPIUserTemplateFile.getFile(), XMLTemplateFieldFactory
								.setValues(studyDefXMLUserInfoTemplateFields,
										values));

				userDoc = xmlHandler
						.parse(DomUtils.toString(source));


				root.appendChild(doc.importNode(userDoc.getDocumentElement(),
						true));
			} else {
				role = "Study Coordinator";
				// query from ME table
				/*
				String qry = "SELECT [sap_id] FROM [clara].[dbo].[epic_pi_id] where sap_id =:sap";
				Query query = em.createNativeQuery(qry);
				query.setParameter("sap", sap);
				try {
					providerCode = (String) query.getSingleResult();
				} catch (Exception e) {
					// no mapped provider found
					continue;
				}*/
				Map<String, String> values = Maps.newHashMap();
				values.put("providerCode", sap);
				values.put("role", role);
				Source source = xmlHandler.replaceTemplateFields(
						noPIUserTemplateFile.getFile(), XMLTemplateFieldFactory
								.setValues(studyDefXMLUserInfoTemplateFields,
										values));

				Document userDoc = xmlHandler
						.parse(DomUtils.toString(source));


				root.appendChild(doc.importNode(userDoc.getDocumentElement(),
						true));
			}

		}
		//add NCT NUMBER
				String nctNumber = xmlHandler.getSingleStringValueByXPath(xmlData, "/protocol/summary/clinical-trials-determinations/nct-number");
				if(!nctNumber.isEmpty()){
				Map<String, String> values = Maps.newHashMap();
				//remove "NCT" prefix and only send the digit part
				values.put("nctNumber", nctNumber.replaceAll("\\D+",""));
				Source source = xmlHandler.replaceTemplateFields(
						nctNumberTemplateFile.getFile(), XMLTemplateFieldFactory
								.setValues(studyDefXMLNCTNumberInfoTemplateFields,
										values));
				Document userDoc = xmlHandler
						.parse(DomUtils.toString(source));
				root.appendChild(doc.importNode(userDoc.getDocumentElement(),
						true));
				}
		return DomUtils.elementToString(root);

	}

	@Override
	public void retrieveProtocolDefResponse(String irbNumber, String epicTitle,
<<<<<<< HEAD
			String epicSummary, String protocolMetaData) throws Exception {
		
		if (!shouldRun) return;

		final String messageUUID = UUID.randomUUID().toString();
		final String uri = this.uri;
		epicTitle = epicTitle.trim();
		epicSummary = epicSummary.trim();

		try {
			Source requestPayload = createRetrieveProtocolDefResponseRequestPayload(
					irbNumber, epicTitle, epicSummary, this.getProtocolUserInfoXml(protocolMetaData));
			
			Writer outWriter = new StringWriter();
			StreamResult result = new StreamResult(outWriter);
			
			webServiceTemplate.sendSourceAndReceiveToResult(requestPayload,
					new WebServiceMessageCallback() {

						/***
						 * <soap:Header
						 * xmlns:wsa="http://www.w3.org/2005/08/addressing">
						 * <wsa:Action>urn:ihe:qrph:rpe:2009:
						 * RetrieveProtocolDefResponse</wsa:Action>
						 * <wsa:MessageID
						 * >uuid:008fb54c-5ade-440d-ba64-e8289cc584cd
						 * </wsa:MessageID>
						 * <wsa:To>http://144.30.0.133/Interconnect
						 * -POC-EDI/wcf/Epic
						 * .EDI.IHEWcf.Services/ProtocolExecutor.svc</wsa:To>
						 * </soap:Header>
						 */
						@Override
						public void doWithMessage(WebServiceMessage message)
								throws IOException, TransformerException {
							SoapMessage soapMessage = ((SoapMessage) message);
							// SoapEnvelope envelope =
							// soapMessage.getEnvelope();

							// envelope.addNamespaceDeclaration("xsi",
							// "http://www.w3.org/2001/XMLSchema-instance");
							// envelope.addNamespaceDeclaration("xsd",
							// "http://www.w3.org/2001/XMLSchema");

							SoapHeader header = soapMessage.getSoapHeader();

							header.addNamespaceDeclaration("wsa",
									"http://www.w3.org/2005/08/addressing");

							SoapHeaderElement action = header
									.addHeaderElement(new QName(
											"http://www.w3.org/2005/08/addressing",
											"Action", "wsa"));
							action.setText("urn:ihe:qrph:rpe:2009:RetrieveProtocolDefResponse");

							SoapHeaderElement messageID = header
									.addHeaderElement(new QName(
											"http://www.w3.org/2005/08/addressing",
											"MessageID", "wsa"));
							messageID.setText(messageUUID);

							SoapHeaderElement to = header
									.addHeaderElement(new QName(
											"http://www.w3.org/2005/08/addressing",
											"To", "wsa"));
							to.setText(uri);

							auditService.auditEvent(
									AuditService.AuditEvent.EPIC_WS_SENT
											.toString(),
									"sent message with MessageID: "
											+ messageUUID, DomUtils
											.elementToString(soapMessage
													.getDocument()));

							logger.debug("sent message with MessageID: "
									+ messageUUID
									+ "; "
									+ DomUtils.elementToString(soapMessage
											.getDocument()));
						}

					}, result);

			auditService.auditEvent(
					AuditService.AuditEvent.EPIC_WS_SUCCEED.toString(),
					"EPIC_WS_SUCCEED: MessageID: " + messageUUID
							+ "; irbNumber: " + irbNumber + "; epicTitle: "
							+ epicTitle + "; epicSummary: " + epicSummary,
					outWriter.toString());

			logger.debug("EPIC_WS_SUCCEED: MessageID: " + messageUUID
					+ "; irbNumber: " + irbNumber + "; epicTitle: " + epicTitle
					+ "; epicSummary: " + epicSummary + "; "
					+ outWriter.toString());

		} catch (Exception ex) {
			auditService.auditEvent(
					AuditService.AuditEvent.EPIC_WS_FAILED.toString(),
					"failed to send message to epic with MessageID: "
							+ messageUUID + "; irbNumber: " + irbNumber
							+ "; epicTitle: " + epicTitle + "; epicSummary: "
							+ epicSummary);
			logger.error("failed to send message to epic with MessageID: "
					+ messageUUID + "; irbNumber: " + irbNumber
					+ "; epicTitle: " + epicTitle + "; epicSummary: "
					+ epicSummary, ex);
			throw ex;

		}
	}

	public WebServiceTemplate getWebServiceTemplate() {
		return webServiceTemplate;
	}

	public void setWebServiceTemplate(WebServiceTemplate webServiceTemplate) {
		this.webServiceTemplate = webServiceTemplate;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public AuditService getAuditService() {
		return auditService;
	}

	@Autowired(required = true)
	public void setAuditService(AuditService auditService) {
		this.auditService = auditService;
	}

	public ResourceLoader getResourceLoader() {
		return resourceLoader;
	}

	@Autowired(required = true)
	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}

	public String getRetrieveProtocolDefResponseRequestXmlTemplateUri() {
		return retrieveProtocolDefResponseRequestXmlTemplateUri;
	}

	public void setRetrieveProtocolDefResponseRequestXmlTemplateUri(
			String retrieveProtocolDefResponseRequestXmlTemplateUri) {
		this.retrieveProtocolDefResponseRequestXmlTemplateUri = retrieveProtocolDefResponseRequestXmlTemplateUri;
	}

	public String getHl7RootId() {
		return hl7RootId;
	}

	public void setHl7RootId(String hl7RootId) {
		this.hl7RootId = hl7RootId;
	}

	public String getRetrieveProtocolPIXmlTemplateUri() {
		return retrieveProtocolPIXmlTemplateUri;
	}

	public void setRetrieveProtocolPIXmlTemplateUri(
			String retrieveProtocolPIXmlTemplateUri) {
		this.retrieveProtocolPIXmlTemplateUri = retrieveProtocolPIXmlTemplateUri;
	}

	public String getRetrieveProtocolNoPIUserXmlTemplateUri() {
		return retrieveProtocolNoPIUserXmlTemplateUri;
	}

	public void setRetrieveProtocolNoPIUserXmlTemplateUri(
			String retrieveProtocolNoPIUserXmlTemplateUri) {
		this.retrieveProtocolNoPIUserXmlTemplateUri = retrieveProtocolNoPIUserXmlTemplateUri;
	}

	public EntityManager getEm() {
		return em;
	}

	@PersistenceContext(unitName = "defaultPersistenceUnit")
	public void setEm(EntityManager em) {
		this.em = em;
	}

	public UserService getUserService() {
		return userService;
	}
	
	@Autowired(required = true)
	public void setUserService(UserService userService) {
		this.userService = userService;
	}

	public String getRetrieveProtocolNctNumberTemplateUri() {
		return retrieveProtocolNctNumberTemplateUri;
	}

	public void setRetrieveProtocolNctNumberTemplateUri(
			String retrieveProtocolNctNumberTemplateUri) {
		this.retrieveProtocolNctNumberTemplateUri = retrieveProtocolNctNumberTemplateUri;
	}
	
	public boolean isShouldRun() {
		return shouldRun;
	}

	@Value("${scheduler.task.outgoing.should.run}")
	public void setShouldRun(boolean shouldRun) {
		this.shouldRun = shouldRun;
	}
}
=======
			String epicSummary, String protocolMetaData) throws Exception {
		
		if (!shouldRun) return;

		final String messageUUID = UUID.randomUUID().toString();
		final String uri = this.uri;
		epicTitle = epicTitle.trim();
		epicSummary = epicSummary.trim();

		try {
			Source requestPayload = createRetrieveProtocolDefResponseRequestPayload(
					irbNumber, epicTitle, epicSummary, this.getProtocolUserInfoXml(protocolMetaData));

			Writer outWriter = new StringWriter();
			StreamResult result = new StreamResult(outWriter);
			
			webServiceTemplate.sendSourceAndReceiveToResult(requestPayload,
					new WebServiceMessageCallback() {

						/***
						 * <soap:Header
						 * xmlns:wsa="http://www.w3.org/2005/08/addressing">
						 * <wsa:Action>urn:ihe:qrph:rpe:2009:
						 * RetrieveProtocolDefResponse</wsa:Action>
						 * <wsa:MessageID
						 * >uuid:008fb54c-5ade-440d-ba64-e8289cc584cd
						 * </wsa:MessageID>
						 * <wsa:To>http://144.30.0.133/Interconnect
						 * -POC-EDI/wcf/Epic
						 * .EDI.IHEWcf.Services/ProtocolExecutor.svc</wsa:To>
						 * </soap:Header>
						 */
						@Override
						public void doWithMessage(WebServiceMessage message)
								throws IOException, TransformerException {
							SoapMessage soapMessage = ((SoapMessage) message);
							// SoapEnvelope envelope =
							// soapMessage.getEnvelope();

							// envelope.addNamespaceDeclaration("xsi",
							// "http://www.w3.org/2001/XMLSchema-instance");
							// envelope.addNamespaceDeclaration("xsd",
							// "http://www.w3.org/2001/XMLSchema");

							SoapHeader header = soapMessage.getSoapHeader();

							header.addNamespaceDeclaration("wsa",
									"http://www.w3.org/2005/08/addressing");

							SoapHeaderElement action = header
									.addHeaderElement(new QName(
											"http://www.w3.org/2005/08/addressing",
											"Action", "wsa"));
							action.setText("urn:ihe:qrph:rpe:2009:RetrieveProtocolDefResponse");

							SoapHeaderElement messageID = header
									.addHeaderElement(new QName(
											"http://www.w3.org/2005/08/addressing",
											"MessageID", "wsa"));
							messageID.setText(messageUUID);

							SoapHeaderElement to = header
									.addHeaderElement(new QName(
											"http://www.w3.org/2005/08/addressing",
											"To", "wsa"));
							to.setText(uri);

							auditService.auditEvent(
									AuditService.AuditEvent.EPIC_WS_SENT
											.toString(),
									"sent message with MessageID: "
											+ messageUUID, DomUtils
											.elementToString(soapMessage
													.getDocument()));

							logger.debug("sent message with MessageID: "
									+ messageUUID
									+ "; "
									+ DomUtils.elementToString(soapMessage
											.getDocument()));
						}

					}, result);

			auditService.auditEvent(
					AuditService.AuditEvent.EPIC_WS_SUCCEED.toString(),
					"EPIC_WS_SUCCEED: MessageID: " + messageUUID
							+ "; irbNumber: " + irbNumber + "; epicTitle: "
							+ epicTitle + "; epicSummary: " + epicSummary,
					outWriter.toString());

			logger.debug("EPIC_WS_SUCCEED: MessageID: " + messageUUID
					+ "; irbNumber: " + irbNumber + "; epicTitle: " + epicTitle
					+ "; epicSummary: " + epicSummary + "; "
					+ outWriter.toString());

		} catch (Exception ex) {
			auditService.auditEvent(
					AuditService.AuditEvent.EPIC_WS_FAILED.toString(),
					"failed to send message to epic with MessageID: "
							+ messageUUID + "; irbNumber: " + irbNumber
							+ "; epicTitle: " + epicTitle + "; epicSummary: "
							+ epicSummary);
			logger.error("failed to send message to epic with MessageID: "
					+ messageUUID + "; irbNumber: " + irbNumber
					+ "; epicTitle: " + epicTitle + "; epicSummary: "
					+ epicSummary, ex);
			throw ex;

		}
	}

	public WebServiceTemplate getWebServiceTemplate() {
		return webServiceTemplate;
	}

	public void setWebServiceTemplate(WebServiceTemplate webServiceTemplate) {
		this.webServiceTemplate = webServiceTemplate;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public AuditService getAuditService() {
		return auditService;
	}

	@Autowired(required = true)
	public void setAuditService(AuditService auditService) {
		this.auditService = auditService;
	}

	public ResourceLoader getResourceLoader() {
		return resourceLoader;
	}

	@Autowired(required = true)
	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}

	public String getRetrieveProtocolDefResponseRequestXmlTemplateUri() {
		return retrieveProtocolDefResponseRequestXmlTemplateUri;
	}

	public void setRetrieveProtocolDefResponseRequestXmlTemplateUri(
			String retrieveProtocolDefResponseRequestXmlTemplateUri) {
		this.retrieveProtocolDefResponseRequestXmlTemplateUri = retrieveProtocolDefResponseRequestXmlTemplateUri;
	}

	public String getHl7RootId() {
		return hl7RootId;
	}

	public void setHl7RootId(String hl7RootId) {
		this.hl7RootId = hl7RootId;
	}

	public String getRetrieveProtocolPIXmlTemplateUri() {
		return retrieveProtocolPIXmlTemplateUri;
	}

	public void setRetrieveProtocolPIXmlTemplateUri(
			String retrieveProtocolPIXmlTemplateUri) {
		this.retrieveProtocolPIXmlTemplateUri = retrieveProtocolPIXmlTemplateUri;
	}

	public String getRetrieveProtocolNoPIUserXmlTemplateUri() {
		return retrieveProtocolNoPIUserXmlTemplateUri;
	}

	public void setRetrieveProtocolNoPIUserXmlTemplateUri(
			String retrieveProtocolNoPIUserXmlTemplateUri) {
		this.retrieveProtocolNoPIUserXmlTemplateUri = retrieveProtocolNoPIUserXmlTemplateUri;
	}

	public EntityManager getEm() {
		return em;
	}

	@PersistenceContext(unitName = "defaultPersistenceUnit")
	public void setEm(EntityManager em) {
		this.em = em;
	}

	public UserService getUserService() {
		return userService;
	}
	
	@Autowired(required = true)
	public void setUserService(UserService userService) {
		this.userService = userService;
	}

	public String getRetrieveProtocolNctNumberTemplateUri() {
		return retrieveProtocolNctNumberTemplateUri;
	}

	public void setRetrieveProtocolNctNumberTemplateUri(
			String retrieveProtocolNctNumberTemplateUri) {
		this.retrieveProtocolNctNumberTemplateUri = retrieveProtocolNctNumberTemplateUri;
	}
	
	public boolean isShouldRun() {
		return shouldRun;
	}

	@Value("${scheduler.task.outgoing.should.run}")
	public void setShouldRun(boolean shouldRun) {
		this.shouldRun = shouldRun;
	}
}
>>>>>>> claraoriginal/master
