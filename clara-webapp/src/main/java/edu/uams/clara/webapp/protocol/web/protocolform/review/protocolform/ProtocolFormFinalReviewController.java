package edu.uams.clara.webapp.protocol.web.protocolform.review.protocolform;


import java.io.IOException;

import javax.xml.xpath.XPathExpressionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.xml.sax.SAXException;

import edu.uams.clara.webapp.common.businesslogic.BusinessObjectStatusHelperContainer;
import edu.uams.clara.webapp.common.domain.usercontext.User;
import edu.uams.clara.webapp.common.domain.usercontext.enums.Committee;
import edu.uams.clara.webapp.protocol.dao.ProtocolDao;
import edu.uams.clara.webapp.protocol.dao.businesslogicobject.ProtocolFormCommitteeStatusDao;
import edu.uams.clara.webapp.protocol.dao.protocolform.ProtocolFormDao;
import edu.uams.clara.webapp.protocol.dao.protocolform.ProtocolFormXmlDataDao;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.ProtocolFormCommitteeStatus;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolForm;
import edu.uams.clara.webapp.xml.processor.XmlProcessor;

@Controller
public class ProtocolFormFinalReviewController {

	private final static Logger logger = LoggerFactory
			.getLogger(ProtocolFormFinalReviewController.class);
	private ProtocolFormDao protocolFormDao;
	
	private ProtocolFormXmlDataDao protocolFormXmlDataDao;
	
	private ProtocolDao protocolDao;
	
	private XmlProcessor xmlProcessor; 
	
	private BusinessObjectStatusHelperContainer businessObjectStatusHelperContainer;
	
	private ProtocolFormCommitteeStatusDao protocolFormCommitteeStatusDao;
	
	
	@RequestMapping(value = "/protocols/{protocolId}/protocol-forms/{protocolFormId}/review/{protocolFormUrlName}/{committeeReviewPage}", method = RequestMethod.GET)
	public String getProtocolFormReviewPage(
			@PathVariable("protocolId") long protocolId,
			@PathVariable("protocolFormId") long protocolFormId,
			@RequestParam("committee") Committee committee,
			@RequestParam(value="fromQueue", required=false) String fromQueue,
			@PathVariable("committeeReviewPage") String committeeReviewPage,
			@PathVariable("protocolFormUrlName") String protocolFormUrlName,
			ModelMap modelMap) throws XPathExpressionException, IOException, SAXException{

		ProtocolForm protocolForm = protocolFormDao.findById(protocolFormId);
		
		ProtocolFormCommitteeStatus pfcs = protocolFormCommitteeStatusDao.getLatestByCommitteeAndProtocolFormId(committee, protocolFormId);
		
		//ProtocolFormXmlData xmlData = protocolForm.getTypedProtocolFormXmlDatas().get(ProtocolFormXmlDataType.PROTOCOL);
				
		modelMap.put("protocolForm", protocolForm);
		
		//protocolForm.getTypedProtocolFormXmlDatas().get(protocolForm.getProtocolFormType().getDefaultProtocolFormXmlDataType());
		//modelMap.put("protocolFormXmlData", xmlData);		
		modelMap.put("committee", committee);
		modelMap.put("committeeReviewPage", committeeReviewPage);
		modelMap.put("protocolFormUrlName", protocolFormUrlName);
		
		String reviewPageXml = businessObjectStatusHelperContainer.getBusinessObjectStatusHelper(protocolForm.getFormType()).getReviewPageXml(protocolForm, committee, committeeReviewPage);
		logger.debug("reviewPageXml: " + reviewPageXml);
		modelMap.put("reviewPageXml", reviewPageXml);
		modelMap.put("fromQueue",fromQueue);
		modelMap.put("commentNote", (pfcs!=null && pfcs.getNote()!=null && !pfcs.getNote().isEmpty())?pfcs.getNote():"");
		
		modelMap.put("action", (pfcs!=null && pfcs.getAction()!=null && !pfcs.getAction().isEmpty())?pfcs.getAction():"");
		
		modelMap.put("user", (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal());
		
		return "protocol/protocolform/committee-review";
	}
	

	@Autowired(required=true)
	public void setProtocolDao(ProtocolDao protocolDao) {
		this.protocolDao = protocolDao;
	}

	public ProtocolDao getProtocolDao() {
		return protocolDao;
	}

	@Autowired(required=true)
	public void setProtocolFormDao(ProtocolFormDao protocolFormDao) {
		this.protocolFormDao = protocolFormDao;
	}

	public ProtocolFormDao getProtocolFormDao() {
		return protocolFormDao;
	}

	@Autowired(required=true)
	public void setProtocolFormXmlDataDao(ProtocolFormXmlDataDao protocolFormXmlDataDao) {
		this.protocolFormXmlDataDao = protocolFormXmlDataDao;
	}

	public ProtocolFormXmlDataDao getProtocolFormXmlDataDao() {
		return protocolFormXmlDataDao;
	}


	public XmlProcessor getXmlProcessor() {
		return xmlProcessor;
	}

	@Autowired(required=true)
	public void setXmlProcessor(XmlProcessor xmlProcessor) {
		this.xmlProcessor = xmlProcessor;
	}

	public BusinessObjectStatusHelperContainer getBusinessObjectStatusHelperContainer() {
		return businessObjectStatusHelperContainer;
	}

	@Autowired(required=true)
	public void setBusinessObjectStatusHelperContainer(
			BusinessObjectStatusHelperContainer businessObjectStatusHelperContainer) {
		this.businessObjectStatusHelperContainer = businessObjectStatusHelperContainer;
	}


	public ProtocolFormCommitteeStatusDao getProtocolFormCommitteeStatusDao() {
		return protocolFormCommitteeStatusDao;
	}

	@Autowired(required=true)
	public void setProtocolFormCommitteeStatusDao(
			ProtocolFormCommitteeStatusDao protocolFormCommitteeStatusDao) {
		this.protocolFormCommitteeStatusDao = protocolFormCommitteeStatusDao;
	}
}
