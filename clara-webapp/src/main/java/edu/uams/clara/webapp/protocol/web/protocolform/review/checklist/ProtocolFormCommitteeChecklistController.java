package edu.uams.clara.webapp.protocol.web.protocolform.review.checklist;

import java.io.FileNotFoundException;
import java.io.IOException;

import javax.xml.xpath.XPathExpressionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.xml.sax.SAXException;

import edu.uams.clara.webapp.common.dao.usercontext.UserDao;
import edu.uams.clara.webapp.common.domain.usercontext.User;
import edu.uams.clara.webapp.common.domain.usercontext.enums.Committee;
import edu.uams.clara.webapp.protocol.dao.businesslogicobject.ProtocolFormCommitteeStatusDao;
import edu.uams.clara.webapp.protocol.dao.protocolform.review.ProtocolFormChecklistAnswerDao;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.ProtocolFormCommitteeStatus;
import edu.uams.clara.webapp.protocol.domain.protocolform.enums.ProtocolFormType;
import edu.uams.clara.webapp.protocol.domain.protocolform.review.ProtocolFormChecklistAnswer;
import edu.uams.clara.webapp.xml.processor.XmlProcessor;

@Controller
public class ProtocolFormCommitteeChecklistController {
	private final static Logger logger = LoggerFactory
			.getLogger(ProtocolFormCommitteeChecklistController.class);

	private XmlProcessor xmlProcessor;

	private ResourceLoader resourceLoader;

	private ProtocolFormCommitteeStatusDao protocolFormCommitteeStatusDao;
	
	private ProtocolFormChecklistAnswerDao protocolFormChecklistAnswerDao;
	
	private UserDao userDao;

	@Value("${checklistXmlTemplate.url}")
	private String checklistXmlTemplatePath;

	@RequestMapping(value = "/protocols/{protocolId}/protocol-forms/{protocolFormId}/review/checklists/committee-checklist.xml", method = RequestMethod.GET)
	public String getFormCommitteeChecklist(
			@PathVariable("protocolFormId") long protocolFormId,
			@RequestParam("committee") Committee committee,
			@RequestParam("formType") ProtocolFormType protocolFormType,
			@RequestParam("userId") long userId,
			@RequestParam(value = "readOnly", required = false) String readOnly,
			ModelMap modelMap) throws FileNotFoundException, IOException,
			XPathExpressionException, SAXException {
		User user = userDao.findById(userId);
		
		String checkListXmlString = "";

		try {
			ProtocolFormCommitteeStatus protocolFormCommitteeStatus = protocolFormCommitteeStatusDao
					.getLatestByCommitteeAndProtocolFormId(committee,
							protocolFormId);

			Resource checklistXmlFileResource = resourceLoader
					.getResource(checklistXmlTemplatePath);

			// Document checkListDoc =
			// xmlProcessor.loadXmlFileToDOM(checklistXmlFileResource.getFile());

			String checkListXml = xmlProcessor
					.loadXmlFile(checklistXmlFileResource.getFile());

			// XPath xPath = xmlProcessor.getXPathInstance();

			String path = "/checklists/checklist-group[conditions/condition[@protocol-form-type=\""
					+ protocolFormType.toString()
					+ "\" and @committee-name=\""
					+ committee.toString()
					+ "\" and @protocol-form-committee-status=\""
					+ protocolFormCommitteeStatus
							.getProtocolFormCommitteeStatus().toString()
					+ "\"]]";
			
			if (readOnly.equals("true")) {
				path = "/checklists/checklist-group[conditions/condition[@protocol-form-type=\""
						+ protocolFormType.toString()
						+ "\" and @committee-name=\""
						+ committee.toString()
						+ "\"]]";
			}

			// Element checkListGroupEl = (Element) xPath.evaluate(path,
			// checkListDoc, XPathConstants.NODE);

			checkListXmlString = xmlProcessor.listElementsByPath(path,
					checkListXml, false);

		} catch (Exception ex) {
			ex.printStackTrace();

			checkListXmlString = "<checklist-group></checklist-group>";
		}
		
		String checklistAnswerXmlString = "";
		
		try {
			ProtocolFormChecklistAnswer pfca = protocolFormChecklistAnswerDao.getLatestAnswerByUserAndCommitteeAndProtocolFormId(user.getId(), committee, protocolFormId);
			
			checklistAnswerXmlString = pfca.getXmlData();
		} catch (Exception e) {
			//e.printStackTrace();
			
			checklistAnswerXmlString = "<answers />";
		}
		
		modelMap.put("committee", committee.toString());
		modelMap.put("protocolFormId", protocolFormId);
		modelMap.put("checkListXml", checkListXmlString);
		modelMap.put("checkListAnswerXml", checklistAnswerXmlString);
		modelMap.put("readOnly", readOnly);
		//logger.error(checkListXmlString);

		return "protocol/review/checklist";

	}

	public ResourceLoader getResourceLoader() {
		return resourceLoader;
	}

	@Autowired(required = true)
	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}

	public String getChecklistXmlTemplatePath() {
		return checklistXmlTemplatePath;
	}

	public void setChecklistXmlTemplatePath(String checklistXmlTemplatePath) {
		this.checklistXmlTemplatePath = checklistXmlTemplatePath;
	}

	public XmlProcessor getXmlProcessor() {
		return xmlProcessor;
	}

	@Autowired(required = true)
	public void setXmlProcessor(XmlProcessor xmlProcessor) {
		this.xmlProcessor = xmlProcessor;
	}

	public ProtocolFormCommitteeStatusDao getProtocolFormCommitteeStatusDao() {
		return protocolFormCommitteeStatusDao;
	}

	@Autowired(required = true)
	public void setProtocolFormCommitteeStatusDao(
			ProtocolFormCommitteeStatusDao protocolFormCommitteeStatusDao) {
		this.protocolFormCommitteeStatusDao = protocolFormCommitteeStatusDao;
	}

	public ProtocolFormChecklistAnswerDao getProtocolFormChecklistAnswerDao() {
		return protocolFormChecklistAnswerDao;
	}
	
	@Autowired(required = true)
	public void setProtocolFormChecklistAnswerDao(
			ProtocolFormChecklistAnswerDao protocolFormChecklistAnswerDao) {
		this.protocolFormChecklistAnswerDao = protocolFormChecklistAnswerDao;
	}

	public UserDao getUserDao() {
		return userDao;
	}
	
	@Autowired(required = true)
	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}
}
