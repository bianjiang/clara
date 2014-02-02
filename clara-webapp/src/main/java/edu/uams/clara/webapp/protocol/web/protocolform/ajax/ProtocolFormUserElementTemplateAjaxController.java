package edu.uams.clara.webapp.protocol.web.protocolform.ajax;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import edu.uams.clara.webapp.common.dao.usercontext.UserDao;
import edu.uams.clara.webapp.common.domain.usercontext.User;
import edu.uams.clara.webapp.common.domain.usercontext.enums.Committee;
import edu.uams.clara.webapp.common.service.CommitteeGroupService;
import edu.uams.clara.webapp.common.util.UserContextHelper;
import edu.uams.clara.webapp.common.util.XMLResponseHelper;
import edu.uams.clara.webapp.protocol.dao.protocolform.ProtocolFormUserElementTemplateDao;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolFormUserElementTemplate;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolFormUserElementTemplate.TemplateType;
import edu.uams.clara.webapp.protocol.service.ProtocolFormUserElementTemplateService;
import edu.uams.clara.webapp.xml.processor.XmlProcessor;

@Controller
public class ProtocolFormUserElementTemplateAjaxController {

	private final static Logger logger = LoggerFactory
			.getLogger(ProtocolFormUserElementTemplateAjaxController.class);

	private ProtocolFormUserElementTemplateDao protocolFormUserElementTemplateDao;

	private UserDao userDao;

	private ProtocolFormUserElementTemplateService protocolFormUserElementTemplateService;

	private CommitteeGroupService committeeGroupService;

	private XmlProcessor xmlProcessor;

	@RequestMapping(value = "/ajax/protocols/protocol-forms/user-templates/list-by-type-and-user")
	public @ResponseBody
	List<ProtocolFormUserElementTemplate> listProtocolFormUserElementTemplateByTemplateTypeAndUserId(
			@RequestParam("templateType") ProtocolFormUserElementTemplate.TemplateType templateType,
			@RequestParam("userId") long userId) {
		User u = userDao.findById(userId);

		List<ProtocolFormUserElementTemplate> protocolFormUserElementTemplates = Lists
				.newArrayList();
		
		boolean showSharedTemplates = false;
		// sharing budget template among RSC Budget/Coverage Reviewers
		Set<Committee> childCommitteeLst = ImmutableSet
				.copyOf(Iterables.concat(committeeGroupService
						.getChildCommittees(Committee.COVERAGE_REVIEW),
						committeeGroupService
								.getChildCommittees(Committee.COVERAGE_REVIEW)));

		// only budget templates are shared among budget reviewers
		if (UserContextHelper.isMemberOfCommittees(u, childCommitteeLst)
				&& ProtocolFormUserElementTemplate.TemplateType.BUDGET
						.equals(templateType)) {
			logger.debug("budget reviewer and in budget page...");
			try {
				protocolFormUserElementTemplates.addAll(protocolFormUserElementTemplateDao
						.listSharedTemplatesByTypeAndCommittees(templateType,
								childCommitteeLst));
				showSharedTemplates = true;
			} catch (Exception ex) {
				logger.warn("no shared budget templates found!", ex);
			}
		}

		// all templates can be shared between CCTO members
		Set<Committee> sharingCommittees = ImmutableSet.of(Committee.CCTO);
		if (UserContextHelper.isMemberOfCommittees(u, sharingCommittees)) {
			try {
				protocolFormUserElementTemplates.addAll(protocolFormUserElementTemplateDao
						.listSharedTemplatesByTypeAndCommittees(templateType,
								sharingCommittees));
				showSharedTemplates = true;
			} catch (Exception ex) {
				logger.warn("no shared budget templates found!", ex);
			}
		}

		if (!showSharedTemplates) {
			try {
				protocolFormUserElementTemplates = protocolFormUserElementTemplateDao
						.listProtocolFormUserElementTemplateByTemplateTypeAndUserId(
								templateType, userId);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		return protocolFormUserElementTemplates;
	}

	@RequestMapping(value = "/ajax/protocols/protocol-forms/user-templates/{protocolFormUserElementTemplateId}/get-xml-data")
	public @ResponseBody
	String getProtocolFormUserElementTemplateXmlData(
			@PathVariable("protocolFormUserElementTemplateId") long protocolFormUserElementTemplateId) {
		ProtocolFormUserElementTemplate protocolFormUserElementTemplate = protocolFormUserElementTemplateDao
				.findById(protocolFormUserElementTemplateId);

		String updatedXmlData = protocolFormUserElementTemplateService
				.updateTemplateXMLData(
						protocolFormUserElementTemplate.getXmlData(),
						protocolFormUserElementTemplate.getTemplateType());

		return updatedXmlData;
	}

	private String preProcessTemplate(TemplateType templateType, String xmlData) {
		if (templateType.equals(TemplateType.BUDGET)) {
			try {
				Map<String, Object> resultMap = xmlProcessor
						.deleteElementByPath(
								"/budget/expenses/expense[@external='true']",
								xmlData);
				xmlData = resultMap.get("finalXml").toString();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return xmlData;
	}

	@RequestMapping(value = "/ajax/protocols/protocol-forms/user-templates/add")
	public @ResponseBody
	ProtocolFormUserElementTemplate addProtocolFormUserElementTemplate(
			@RequestParam("templateType") ProtocolFormUserElementTemplate.TemplateType templateType,
			@RequestParam("userId") long userId,
			@RequestParam("xmlData") String xmlData,
			@RequestParam("templateName") String templateName) {

		User user = userDao.findById(userId);

		xmlData = preProcessTemplate(templateType, xmlData);

		ProtocolFormUserElementTemplate protocolFormUserElementTemplate = new ProtocolFormUserElementTemplate();
		protocolFormUserElementTemplate.setUser(user);
		protocolFormUserElementTemplate.setCreated(new Date());
		protocolFormUserElementTemplate.setXmlData(xmlData);
		protocolFormUserElementTemplate.setTemplateType(templateType);
		protocolFormUserElementTemplate.setTemplateName(templateName);

		return protocolFormUserElementTemplateDao
				.saveOrUpdate(protocolFormUserElementTemplate);

	}

	@RequestMapping(value = "/ajax/protocols/protocol-forms/user-templates/{protocolFormUserElementTemplateId}/update")
	public @ResponseBody
	Boolean updateProtocolFormUserElementTemplate(
			@PathVariable("protocolFormUserElementTemplateId") long protocolFormUserElementTemplateId,
			@RequestParam("xmlData") String xmlData,
			@RequestParam("name") String templateName){
		
		ProtocolFormUserElementTemplate protocolFormUserElementTemplate = protocolFormUserElementTemplateDao.findById(protocolFormUserElementTemplateId);
		protocolFormUserElementTemplate.setXmlData(xmlData);
		protocolFormUserElementTemplate.setTemplateName(templateName);
		try{
			protocolFormUserElementTemplate = protocolFormUserElementTemplateDao.saveOrUpdate(protocolFormUserElementTemplate);
			return Boolean.TRUE;
		}catch(Exception ex){
			ex.printStackTrace();
			return Boolean.FALSE;
		}
	}
	
	@RequestMapping(value = "/ajax/protocols/protocol-forms/user-templates/{protocolFormUserElementTemplateId}/remove")
	public @ResponseBody
	String removeProtocolFormUserElementTemplate(
			@PathVariable("protocolFormUserElementTemplateId") long protocolFormUserElementTemplateId) {

		ProtocolFormUserElementTemplate protocolFormUserElementTemplate = protocolFormUserElementTemplateDao
				.findById(protocolFormUserElementTemplateId);
		protocolFormUserElementTemplate.setRetired(true);
		try {

			protocolFormUserElementTemplate = protocolFormUserElementTemplateDao
					.saveOrUpdate(protocolFormUserElementTemplate);
			return XMLResponseHelper.xmlResult(Boolean.TRUE);
		} catch (Exception ex) {
			ex.printStackTrace();
			return XMLResponseHelper.xmlResult(Boolean.FALSE);
		}
	}

	@Autowired(required = true)
	public void setProtocolFormUserElementTemplateDao(
			ProtocolFormUserElementTemplateDao protocolFormUserElementTemplateDao) {
		this.protocolFormUserElementTemplateDao = protocolFormUserElementTemplateDao;
	}

	public ProtocolFormUserElementTemplateDao getProtocolFormUserElementTemplateDao() {
		return protocolFormUserElementTemplateDao;
	}

	@Autowired(required = true)
	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}

	public UserDao getUserDao() {
		return userDao;
	}

	public ProtocolFormUserElementTemplateService getProtocolFormUserElementTemplateService() {
		return protocolFormUserElementTemplateService;
	}

	@Autowired(required = true)
	public void setProtocolFormUserElementTemplateService(
			ProtocolFormUserElementTemplateService protocolFormUserElementTemplateService) {
		this.protocolFormUserElementTemplateService = protocolFormUserElementTemplateService;
	}

	public CommitteeGroupService getCommitteeGroupService() {
		return committeeGroupService;
	}

	@Autowired(required = true)
	public void setCommitteeGroupService(
			CommitteeGroupService committeeGroupService) {
		this.committeeGroupService = committeeGroupService;
	}

	public XmlProcessor getXmlProcessor() {
		return xmlProcessor;
	}

	@Autowired(required = true)
	public void setXmlProcessor(XmlProcessor xmlProcessor) {
		this.xmlProcessor = xmlProcessor;
	}

}
