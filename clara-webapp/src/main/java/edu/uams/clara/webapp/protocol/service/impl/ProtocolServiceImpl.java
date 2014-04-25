package edu.uams.clara.webapp.protocol.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.google.common.collect.Maps;

import edu.uams.clara.core.util.xml.XmlHandler;
import edu.uams.clara.core.util.xml.XmlHandlerFactory;
import edu.uams.clara.integration.outgoing.epic.StudyDefinitionWSClient;
import edu.uams.clara.webapp.common.domain.history.Track;
import edu.uams.clara.webapp.common.domain.usercontext.Person;
import edu.uams.clara.webapp.common.domain.usercontext.User;
import edu.uams.clara.webapp.common.domain.usercontext.enums.Committee;
import edu.uams.clara.webapp.common.security.ObjectAclService;
import edu.uams.clara.webapp.common.service.UserService;
import edu.uams.clara.webapp.common.service.form.FormService;
import edu.uams.clara.webapp.common.service.form.impl.FormServiceImpl.UserSearchField;
import edu.uams.clara.webapp.common.util.DateFormatUtil;
import edu.uams.clara.webapp.protocol.dao.ProtocolDao;
import edu.uams.clara.webapp.protocol.dao.businesslogicobject.ProtocolFormStatusDao;
import edu.uams.clara.webapp.protocol.dao.businesslogicobject.ProtocolStatusDao;
import edu.uams.clara.webapp.protocol.dao.protocolform.ProtocolFormDao;
import edu.uams.clara.webapp.protocol.dao.protocolform.ProtocolFormXmlDataDao;
import edu.uams.clara.webapp.protocol.domain.Protocol;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.ProtocolFormStatus;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.ProtocolStatus;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.enums.ProtocolFormStatusEnum;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.enums.ProtocolStatusEnum;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolForm;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolFormXmlData;
import edu.uams.clara.webapp.protocol.domain.protocolform.enums.ProtocolFormType;
import edu.uams.clara.webapp.protocol.domain.protocolform.enums.ProtocolFormXmlDataType;
import edu.uams.clara.webapp.protocol.service.ProtocolFormService;
import edu.uams.clara.webapp.protocol.service.ProtocolService;
import edu.uams.clara.webapp.protocol.service.history.ProtocolTrackService;
import edu.uams.clara.webapp.xml.processor.XmlProcessor;

public class ProtocolServiceImpl implements ProtocolService {
	
	private final static Logger logger = LoggerFactory
			.getLogger(ProtocolServiceImpl.class);
	
	private ProtocolDao protocolDao;
	private ProtocolFormDao protocolFormDao;
	private ProtocolFormXmlDataDao protocolFormXmlDataDao;
	
	private ProtocolStatusDao protocolStatusDao;
	private ProtocolFormStatusDao protocolFormStatusDao;
	
	private UserService userService;
	private ProtocolFormService protocolFormService;
	
	private StudyDefinitionWSClient studyDefinitionWSClient;
	
	private ProtocolTrackService protocolTrackService;
	
	private ObjectAclService objectAclService;
	
	private FormService formService;
	
	private XmlProcessor xmlProcessor;
	
	private long baseProtocolIdentifier = 0;


	@Override
	public ProtocolFormXmlData creatNewProtocol(ProtocolFormType protocolFormType) throws XPathExpressionException, IOException,
	SAXException{
		Protocol p = new Protocol();
		Date created = new Date();
		p.setCreated(created);
		p.setLocked(false);
		
		p = protocolDao.saveOrUpdate(p);

		p.setProtocolIdentifier(Long.toString(baseProtocolIdentifier+p.getId()));
		
		//protocol.metadataxml always start with /protocol/
		//String protocolMetaDataXmlString = "<protocol id=\"" + p.getId() + "\" identifier=\"" + p.getProtocolIdentifier() + "\" type=\""+ protocolFormType.getDescription() +"\"></protocol>";
		String protocolMetaDataXmlString = (protocolFormType.equals(ProtocolFormType.NEW_SUBMISSION))?"<protocol id=\"" + p.getId() + "\" identifier=\"" + p.getProtocolIdentifier() + "\"></protocol>":"<protocol id=\"" + p.getId() + "\" identifier=\"" + p.getProtocolIdentifier() + "\" type=\""+ protocolFormType.getDescription() +"\"></protocol>";;
		
		p.setMetaDataXml(protocolMetaDataXmlString);
		p = protocolDao.saveOrUpdate(p);
		
		String protocolFormXmlString = "<"+ protocolFormType.getBaseTag() +" id=\"" + p.getId() + "\" identifier=\"" + p.getProtocolIdentifier() + "\" type=\""+ protocolFormType.getDescription() +"\"></"+ protocolFormType.getBaseTag() +">";
					
		ProtocolForm f = new ProtocolForm();
		f.setProtocolFormType(protocolFormType);
		f.setProtocol(p);
		f.setCreated(created);
		f.setMetaDataXml(protocolFormXmlString);
		f.setParent(f);
		f.setLocked(false);

		f = protocolFormDao.saveOrUpdate(f);		
		
		ProtocolFormXmlData fxd = new ProtocolFormXmlData();
		fxd.setProtocolForm(f);
		fxd.setXmlData(protocolFormXmlString);
		fxd.setProtocolFormXmlDataType(protocolFormType.getDefaultProtocolFormXmlDataType());
		fxd.setParent(fxd);
		fxd.setCreated(created);

		fxd = protocolFormXmlDataDao.saveOrUpdate(fxd);

		Map<ProtocolFormXmlDataType, ProtocolFormXmlData> protocolFormXmlDatas = new HashMap<ProtocolFormXmlDataType, ProtocolFormXmlData>(
				0);
		protocolFormXmlDatas.put(protocolFormType.getDefaultProtocolFormXmlDataType(), fxd);
		f.setTypedProtocolFormXmlDatas(protocolFormXmlDatas);

		User currentUser = userService.getCurrentUser();

		protocolFormService.triggerPIAction("CREATE", f, currentUser, null);

		objectAclService.updateObjectAclByUser(Protocol.class, p.getId(), currentUser);
		return fxd;
	}
	
	@Override
	public String populateEpicDesc(String xmlData) {
		List<User> piUserLst = formService.getUsersByKeywordAndSearchField("Principal Investigator", xmlData, UserSearchField.ROLE);
		List<User> emrStudyContactUserLst = formService.getUsersByKeywordAndSearchField("EMR Study Contact", xmlData, UserSearchField.RESPONSIBILITY);
		
		String epicDescription = "";
		
		if (piUserLst.size() > 0){
			epicDescription += String.format("Principal Investigator: ");
			
			for (User user : piUserLst){
				Person person = user.getPerson();
				epicDescription += String.format("Name: " + person.getFullname() + " Department: " + person.getDepartment() + " Email: " + person.getEmail() + " Phone: " + person.getWorkphone() + "%n");
			}
		}
		
		if (emrStudyContactUserLst.size() > 0){
			epicDescription += String.format(" EMR Study Contact: ");
			
			for (User user : emrStudyContactUserLst){
				Person person = user.getPerson();
				epicDescription += String.format("Name: " + person.getFullname() + " Department: " + person.getDepartment() + " Email: " + person.getEmail() + " Phone: " + person.getWorkphone() + "%n");
			}
		}
		
		return epicDescription;
	}
	
	@Override
	public void setProtocolStatus(Protocol protocol,
			ProtocolStatusEnum protocolStatusEnum, User user,
			Committee committee, String note) {
		try {
			ProtocolStatus protocolStatus = new ProtocolStatus();
			protocolStatus.setCauseByUser(user);
			protocolStatus.setCausedByCommittee(committee);
			protocolStatus.setModified(new Date());
			protocolStatus.setNote(note);
			protocolStatus.setProtocol(protocol);
			protocolStatus.setProtocolStatus(protocolStatusEnum);
			protocolStatus.setRetired(Boolean.FALSE);
			
			protocolStatusDao.saveOrUpdate(protocolStatus);
			
			String protocolMetaDataXml = protocol.getMetaDataXml();
			
			protocolMetaDataXml = xmlProcessor.replaceOrAddNodeValueByPath(
					"/protocol/status", protocolMetaDataXml,
					org.apache.commons.lang.StringEscapeUtils
							.escapeXml(protocolStatusEnum
									.getDescription()));
			
			Map<String, String> attributes = Maps.newHashMap();
			attributes.put("priority", protocolStatusEnum.getPriorityLevel());
			
			protocolMetaDataXml = xmlProcessor.addAttributesByPath(
					"/protocol/status", protocolMetaDataXml, attributes);

			protocol.setMetaDataXml(protocolMetaDataXml);
			protocolDao.saveOrUpdate(protocol);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	private List<ProtocolFormStatusEnum> protocolFormStatusLst = new ArrayList<ProtocolFormStatusEnum>();{
		protocolFormStatusLst.add(ProtocolFormStatusEnum.IRB_ACKNOWLEDGED);
		protocolFormStatusLst.add(ProtocolFormStatusEnum.IRB_APPROVED);
		protocolFormStatusLst.add(ProtocolFormStatusEnum.EXPEDITED_APPROVED);
		protocolFormStatusLst.add(ProtocolFormStatusEnum.EXEMPT_APPROVED);
		protocolFormStatusLst.add(ProtocolFormStatusEnum.IRB_DECLINED);
		protocolFormStatusLst.add(ProtocolFormStatusEnum.IRB_TABLED);
		protocolFormStatusLst.add(ProtocolFormStatusEnum.COMPLIANCE_APPROVED);
		protocolFormStatusLst.add(ProtocolFormStatusEnum.CANCELLED);
		protocolFormStatusLst.add(ProtocolFormStatusEnum.IRB_REVIEW_NOT_NEEDED);
		protocolFormStatusLst.add(ProtocolFormStatusEnum.BUDGET_NOT_REVIEWED);
	}
	
	@Override
	public boolean allowForm(Protocol protocol,
			ProtocolFormType protocolFormType) {
		boolean allowForm = false;
		
		try {
			ProtocolForm protocolForm = protocolFormDao.getLatestProtocolFormByProtocolIdAndProtocolFormType(protocol.getId(), protocolFormType);
			
			ProtocolFormStatus latestModProtocolFormStatus = protocolFormStatusDao.getLatestProtocolFormStatusByFormId(protocolForm.getId());

			if (protocolFormStatusLst.contains(latestModProtocolFormStatus.getProtocolFormStatus())){
				allowForm = true;
			}
		} catch (Exception e) {
			allowForm = true;
			
		}
		
		return allowForm;
	}
	
	@Override
	public Map<String, Boolean> checkStudyCharacteristic(String protocolMetaData) {
		Map<String, Boolean> studyCharacteristicMap = Maps.newHashMap();
		
		String isAchStudyXpath = "boolean(count(/protocol/site-responsible[text()='ach-achri'])>0 or count(/protocol/study-sites/site[@site-id=\"2\" or @site-id=\"1\"])>0)";
		
		String isAchStudyWithDrugXpath = "boolean((count(/protocol/site-responsible[text()='ach-achri'])>0 or count(/protocol/study-sites/site[@site-id=\"2\" or @site-id=\"1\"])>0) and count(/protocol/drugs/drug)>0)";
		
		String isInvestigatorStudyPath = "boolean(count(/protocol/study-type[text()='investigator-initiated'])>0)";
		
		String isIndustryOrCoorpStudyPath = "boolean(count(/protocol/study-type[text()='cooperative-group'])>0 or count(/protocol/study-type[text()='industry-sponsored'])>0)";
		
		String isUamsStudyPath = "boolean(count(/protocol/site-responsible[text()='uams'])>0)";
		
		try {
			boolean isAchStudy = false;
			boolean isAchStudyWithDrug = false;
			
			boolean isInvestigatorStudy = false;
			boolean isIndustryOrCoorpStudy = false;
			
			boolean isUAMSStudy = false;
			
			Document assertXml = xmlProcessor.loadXmlStringToDOM(protocolMetaData);
			
			XPath xpathInstance = xmlProcessor.getXPathInstance();
			
			isAchStudy = (Boolean) (xpathInstance
					.evaluate(
							isAchStudyXpath,
							assertXml, XPathConstants.BOOLEAN));
			
			studyCharacteristicMap.put("isAchStudy", isAchStudy);
			
			xpathInstance.reset();
			
			isAchStudyWithDrug = (Boolean) (xpathInstance
					.evaluate(
							isAchStudyWithDrugXpath,
							assertXml, XPathConstants.BOOLEAN));
			
			studyCharacteristicMap.put("isAchStudyWithDrug", isAchStudyWithDrug);
			
			xpathInstance.reset();
			
			isInvestigatorStudy = (Boolean) (xpathInstance
					.evaluate(
							isInvestigatorStudyPath,
							assertXml, XPathConstants.BOOLEAN));
			
			studyCharacteristicMap.put("isInvestigatorStudy", isInvestigatorStudy);
			
			xpathInstance.reset();
			
			isIndustryOrCoorpStudy = (Boolean) (xpathInstance
					.evaluate(
							isIndustryOrCoorpStudyPath,
							assertXml, XPathConstants.BOOLEAN));
			
			studyCharacteristicMap.put("isIndustryOrCoorpStudy", isIndustryOrCoorpStudy);
			
			xpathInstance.reset();
			
			isUAMSStudy = (Boolean) (xpathInstance
					.evaluate(
							isUamsStudyPath,
							assertXml, XPathConstants.BOOLEAN));
			
			studyCharacteristicMap.put("isUAMSStudy", isUAMSStudy);
			
		} catch (Exception e) {
			//don't care
		}
		
		return studyCharacteristicMap;
	}
	
	@Override
	public boolean isPushedToPSC(String protocolMetaData) {
		boolean isPushedToPSC = false;
		try {
			Document assertXml = xmlProcessor.loadXmlStringToDOM(protocolMetaData);
			
			XPath xpathInstance = xmlProcessor.getXPathInstance();
			
			isPushedToPSC = (Boolean) (xpathInstance
					.evaluate(
							"boolean(count(/protocol/pushed-to-psc[text()='y'])>0)",
							assertXml, XPathConstants.BOOLEAN));
			
		} catch (Exception e) {
			//don't care
		}
		
		return isPushedToPSC;
	}
	
	/*
	private boolean isPushedToEpic(String protocolMetaData) {
		boolean isPushedToEpic = false;
		
		try {
			Document assertXml = xmlProcessor.loadXmlStringToDOM(protocolMetaData);
			
			XPath xpathInstance = xmlProcessor.getXPathInstance();
			
			isPushedToEpic = (Boolean) (xpathInstance
					.evaluate(
							"boolean(count(/protocol/pushed-to-epic[text()='y'])>0",
							assertXml, XPathConstants.BOOLEAN));
			
		} catch (Exception e) {
			//don't care
		}
		
		return isPushedToEpic;
	}
	*/
	
	private boolean canPushToEpic(String protocolMetaData) {
		boolean canPushToEpic = false;
		
		//String protocolMetaData = protocol.getMetaDataXml();
		
		boolean isUAMSStudy = checkStudyCharacteristic(protocolMetaData).get("isUAMSStudy");
		
		boolean isPushedToEpic = false;
		
		try {
			Document assertXml = xmlProcessor.loadXmlStringToDOM(protocolMetaData);
			
			XPath xpathInstance = xmlProcessor.getXPathInstance();
			
			isPushedToEpic = (Boolean) (xpathInstance
					.evaluate(
							"boolean(count(/protocol/pushed-to-epic[text()='y'])>0)",
							assertXml, XPathConstants.BOOLEAN));			
		} catch (Exception e) {
			//e.printStackTrace();
		}
		
		if (isUAMSStudy && !isPushedToEpic) {
			canPushToEpic = true;
		}

		return canPushToEpic;
	}
	
	@Override
	public void pushToEpic(Protocol protocol) {
		String protocolMetaData = protocol.getMetaDataXml();
		
		if (canPushToEpic(protocolMetaData)) {
			try {
				XmlHandler xmlHandler = XmlHandlerFactory.newXmlHandler();
				String epicTitle = xmlHandler.getSingleStringValueByXPath(protocolMetaData, "/protocol/epic/epic-title");
				String epicSummary = xmlHandler.getSingleStringValueByXPath(protocolMetaData, "/protocol/epic/epic-desc");
				
				if (epicTitle.isEmpty()) {
					epicTitle = xmlHandler.getSingleStringValueByXPath(protocolMetaData, "/protocol/title");
				}
				
				if (epicSummary.isEmpty()) {
					epicSummary = populateEpicDesc(protocolMetaData);
				}
				
				studyDefinitionWSClient.retrieveProtocolDefResponse("" + protocol.getId(), epicTitle, epicSummary, protocolMetaData);
				
				//add flag to meta data
				protocolMetaData = xmlProcessor.replaceOrAddNodeValueByPath("/protocol/pushed-to-epic", protocolMetaData, "y");
				
				protocolMetaData = xmlProcessor.replaceOrAddNodeValueByPath("/protocol/pushed-to-epic-date", protocolMetaData, DateFormatUtil.formateDateToMDY(new Date()));
				
				protocol.setMetaDataXml(protocolMetaData);

				protocol = protocolDao.saveOrUpdate(protocol);
				
				//add log
				Track track = protocolTrackService.getOrCreateTrack("PROTOCOL",
						protocol.getId());

				Document logsDoc = protocolTrackService.getLogsDocument(track);

				Element logEl = logsDoc.createElement("log");
				
				String logId = UUID.randomUUID().toString();
				
				Date now = new Date();
				
				logEl.setAttribute("id", logId);
				logEl.setAttribute("parent-id", logId);
				logEl.setAttribute("action-user-id", "0");
				logEl.setAttribute("actor", "System");
				logEl.setAttribute("date-time", DateFormatUtil.formateDate(now));
				logEl.setAttribute("event-type", "PUSH_TO_EPIC");
				logEl.setAttribute("form-id", "0");
				logEl.setAttribute("parent-form-id", "0");
				logEl.setAttribute("form-type", "PROTOCOL");
				logEl.setAttribute("log-type", "ACTION");
				logEl.setAttribute("timestamp", String.valueOf(now.getTime()));

				String message = "Protocol has been pushed to EPIC by system.";

				logEl.setTextContent(message);

				logsDoc.getDocumentElement().appendChild(logEl);

				track = protocolTrackService.updateTrack(track, logsDoc);
				
			} catch (Exception e) {

				logger.error("failed: ", e);
			}
		}
		
	}

	
	@Override
	public Protocol consolidateProtocol(Protocol protocol,
			List<String> xPathList) {
		String protocolMetaData = protocol.getMetaDataXml();
		
		Map<String, Object> resultMap = Maps.newHashMap();
		
		for (String xPath : xPathList) {
			try {
				resultMap = xmlProcessor.deleteElementByPath(xPath, protocolMetaData);
			} catch (Exception e) {
				
			}
			
			if (resultMap != null && !resultMap.isEmpty())
				protocolMetaData = resultMap.get("finalXml").toString();
		}
		
		protocol.setMetaDataXml(protocolMetaData);
		
		protocol = protocolDao.saveOrUpdate(protocol);
		
		return protocol;
	}
	
	public ProtocolDao getProtocolDao() {
		return protocolDao;
	}
	
	@Autowired(required=true)
	public void setProtocolDao(ProtocolDao protocolDao) {
		this.protocolDao = protocolDao;
	}

	public ProtocolFormDao getProtocolFormDao() {
		return protocolFormDao;
	}
	
	@Autowired(required=true)
	public void setProtocolFormDao(ProtocolFormDao protocolFormDao) {
		this.protocolFormDao = protocolFormDao;
	}

	public ProtocolFormXmlDataDao getProtocolFormXmlDataDao() {
		return protocolFormXmlDataDao;
	}
	
	@Autowired(required=true)
	public void setProtocolFormXmlDataDao(ProtocolFormXmlDataDao protocolFormXmlDataDao) {
		this.protocolFormXmlDataDao = protocolFormXmlDataDao;
	}

	public UserService getUserService() {
		return userService;
	}
	
	@Autowired(required=true)
	public void setUserService(UserService userService) {
		this.userService = userService;
	}

	public ProtocolFormService getProtocolFormService() {
		return protocolFormService;
	}
	
	@Autowired(required=true)
	public void setProtocolFormService(ProtocolFormService protocolFormService) {
		this.protocolFormService = protocolFormService;
	}

	public long getBaseProtocolIdentifier() {
		return baseProtocolIdentifier;
	}

	public void setBaseProtocolIdentifier(long baseProtocolIdentifier) {
		this.baseProtocolIdentifier = baseProtocolIdentifier;
	}

	public ObjectAclService getObjectAclService() {
		return objectAclService;
	}

	@Autowired(required=true)
	public void setObjectAclService(ObjectAclService objectAclService) {
		this.objectAclService = objectAclService;
	}

	public FormService getFormService() {
		return formService;
	}
	
	@Autowired(required=true)
	public void setFormService(FormService formService) {
		this.formService = formService;
	}

	public ProtocolStatusDao getProtocolStatusDao() {
		return protocolStatusDao;
	}
	
	@Autowired(required=true)
	public void setProtocolStatusDao(ProtocolStatusDao protocolStatusDao) {
		this.protocolStatusDao = protocolStatusDao;
	}

	public XmlProcessor getXmlProcessor() {
		return xmlProcessor;
	}
	
	@Autowired(required=true)
	public void setXmlProcessor(XmlProcessor xmlProcessor) {
		this.xmlProcessor = xmlProcessor;
	}

	public ProtocolFormStatusDao getProtocolFormStatusDao() {
		return protocolFormStatusDao;
	}
	
	@Autowired(required=true)
	public void setProtocolFormStatusDao(ProtocolFormStatusDao protocolFormStatusDao) {
		this.protocolFormStatusDao = protocolFormStatusDao;
	}

	public StudyDefinitionWSClient getStudyDefinitionWSClient() {
		return studyDefinitionWSClient;
	}
	
	@Autowired(required=true)
	public void setStudyDefinitionWSClient(StudyDefinitionWSClient studyDefinitionWSClient) {
		this.studyDefinitionWSClient = studyDefinitionWSClient;
	}

	public ProtocolTrackService getProtocolTrackService() {
		return protocolTrackService;
	}
	
	@Autowired(required=true)
	public void setProtocolTrackService(ProtocolTrackService protocolTrackService) {
		this.protocolTrackService = protocolTrackService;
	}
}
