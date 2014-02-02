package edu.uams.clara.webapp.queue.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import edu.uams.clara.core.util.xml.DomUtils;
import edu.uams.clara.webapp.common.domain.form.Form;
import edu.uams.clara.webapp.common.domain.usercontext.User;
import edu.uams.clara.webapp.common.domain.usercontext.UserRole;
import edu.uams.clara.webapp.common.domain.usercontext.enums.Committee;
import edu.uams.clara.webapp.contract.dao.businesslogicobject.ContractFormCommitteeStatusDao;
import edu.uams.clara.webapp.contract.dao.businesslogicobject.ContractFormStatusDao;
import edu.uams.clara.webapp.contract.dao.contractform.ContractFormDao;
import edu.uams.clara.webapp.contract.dao.contractform.ContractFormXmlDataDao;
import edu.uams.clara.webapp.contract.domain.businesslogicobject.ContractFormCommitteeStatus;
import edu.uams.clara.webapp.contract.domain.businesslogicobject.ContractFormStatus;
import edu.uams.clara.webapp.contract.domain.businesslogicobject.enums.ContractFormCommitteeStatusEnum;
import edu.uams.clara.webapp.contract.domain.businesslogicobject.enums.ContractFormStatusEnum;
import edu.uams.clara.webapp.contract.domain.contractform.ContractForm;
import edu.uams.clara.webapp.contract.domain.contractform.ContractFormXmlData;
import edu.uams.clara.webapp.contract.domain.contractform.enums.ContractFormType;
import edu.uams.clara.webapp.contract.service.history.ContractTrackService;
import edu.uams.clara.webapp.queue.service.QueueService;
import edu.uams.clara.webapp.xml.processor.XmlProcessor;

public class ContractQueueServiceImpl extends QueueService {

	private final static Logger logger = LoggerFactory
			.getLogger(ContractQueueServiceImpl.class);

	private ContractFormCommitteeStatusDao contractFormCommitteeStatusDao;

	private ContractFormXmlDataDao contractFormXmlDataDao;

	private ContractFormStatusDao contractFormStatusDao;
	
	private ContractFormDao contractFormDao;
	
	private ContractTrackService contractTrackService;

	@Value("${queue.template.xml.uri}")
	private String queueTemplateXmlUri;

	@Override
	public String getFormsInQueueByUser(String queueIdentifier, User user, boolean showHistory) {
		Set<String> res = new HashSet<String>();
		
		logger.debug("showHistory: " + showHistory);
		
		XmlProcessor xmlProcessor = this.getXmlProcessor();
		Set<String> lookupPaths = new HashSet<String>();

		for (UserRole ur : user.getUserRoles()) {
			if (!ur.isRetired() && ur.getRole().getCommitee() != null) {
				logger.debug("ur: " + ur.getRole().getName());
				lookupPaths.add("/queues/queue[@identifier='" + queueIdentifier
						+ "']/roles/role[@identifier='"
						+ ur.getRole().getRolePermissionIdentifier() + "']/.");
			}
		}

		String queueTemplateXml;
		try {
			queueTemplateXml = xmlProcessor.loadXmlFile(queueTemplateXmlUri);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "<list></list>";
		}

		List<Element> roles;
		try {
			roles = xmlProcessor.listDomElementsByPaths(lookupPaths,
					queueTemplateXml);
		} catch (Exception ex) {
			ex.printStackTrace();
			return "<list></list>";
		} 

		//List<ContractFormCommitteeStatus> contractFormsInReview = new ArrayList<ContractFormCommitteeStatus>();
		
		String xmlResult = "<list>";
		
		for (Element role : roles) {

			// logger.debug("x: " + DomUtils.elementToString(role));
			
			Committee committee = Committee.valueOf(role
					.getAttribute("committee"));

			String roleName = role.getAttribute("name");
			String roleId = role.getAttribute("identifier");

			NodeList formNodes = role.getElementsByTagName("form");

			for (int i = 0; i < formNodes.getLength(); i++) {
				Element formElement = (Element) formNodes.item(i);
				ContractFormType formType = ContractFormType
						.valueOf(formElement.getAttribute("type"));
				ContractFormStatusEnum formStatus = ContractFormStatusEnum
						.valueOf(formElement.getAttribute("status"));

				List<ContractFormCommitteeStatusEnum> formCommitteeStatuses = new ArrayList<ContractFormCommitteeStatusEnum>();

				NodeList formCommitteeStatusNodes = formElement
						.getElementsByTagName("form-committee-status");

				for (int j = 0; j < formCommitteeStatusNodes.getLength(); j++) {
					Element formCommitteeStatusElement = (Element) formCommitteeStatusNodes
							.item(j);

					formCommitteeStatuses.add(ContractFormCommitteeStatusEnum
							.valueOf(formCommitteeStatusElement
									.getTextContent()));
				}

				if(showHistory){
					formStatus = ContractFormStatusEnum.ANY;
				}
				
				logger.debug("committee: " + committee + "; formType:"
						+ formType + "; formStatus: " + formStatus);
				
				for (ContractFormCommitteeStatus contractFormCommitteeStatus : contractFormCommitteeStatusDao
						.listByCommitteeAndFormTypeAndStatuses(committee, formStatus,
								formCommitteeStatuses, formType, showHistory)) {

					ContractForm contractForm = contractFormCommitteeStatus
							.getContractForm();
					
					List<String> latestLogLst = contractTrackService.getLatestLogs("CONTRACT", contractForm.getContract().getId(), 5);

					//logger.debug("contractFormId: " + contractForm.getId() + "; contractFormCommitteeStatus.xmlData: " + contractFormCommitteeStatus.getXmlData());
					
					boolean isMine = false;
					String extraXmlData = contractForm.getMetaDataXml();
					
					String assignedReviewersXml = "";
					if (extraXmlData != null && !extraXmlData.isEmpty()){
						try {
							Document extraXmlDataDoc = xmlProcessor.loadXmlStringToDOM(extraXmlData);
							
							XPath xPath = xmlProcessor.getXPathInstance();

							//NodeList assignedReviewers = (NodeList) xPath.evaluate("//assigned-reviewer[@assigning-committee='"+ committee +"' or @user-role-committee='" + committee + "']", extraXmlDataDoc, XPathConstants.NODESET);

							NodeList assignedReviewers = (NodeList) xPath.evaluate("//assigned-reviewer", extraXmlDataDoc, XPathConstants.NODESET);
							
							for (int j = 0; j < assignedReviewers.getLength(); j ++){
								
								Element assignedReviewerEl = (Element)assignedReviewers.item(j);
								
								assignedReviewersXml += DomUtils.elementToString(assignedReviewerEl);
								logger.debug("contractId: " + contractForm.getContract().getId() + " assignedReviewer xml: " + assignedReviewersXml);
								if (Long.parseLong(assignedReviewerEl.getAttribute("user-id")) == user.getId()){
									isMine = true;
									//break;
								}								
							}
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (SAXException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (XPathExpressionException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
					}
					
					String formTypeMachineReadable = contractForm.getContractFormType()
							.getUrlEncoded();
					ContractFormStatus pformStatus = contractFormStatusDao
							.getLatestContractFormStatusByFormId(contractForm.getId());

					ContractFormXmlData lastContractFormXmlData = contractFormXmlDataDao
							.getLastContractFormXmlDataByContractFormIdAndType(
									contractForm.getId(), contractForm
											.getContractFormType()
											.getDefaultContractFormXmlDataType());
					
					
					String formXml = "";
					formXml += "<form committee-name=\"" + committee.getDescription() + "\" committee=\"" + committee + "\" role-name=\"" + roleName + "\" role-id=\""+roleId+"\" form-id=\""
							+ contractForm.getId() + "\" last-version-id=\""
							+ lastContractFormXmlData.getId() + "\""
							+ " is-mine=\"" + isMine + "\">";
					
					formXml += "<assigned-reviewers>";
					if (!assignedReviewersXml.isEmpty()){
						formXml += assignedReviewersXml;
					}
					formXml += "</assigned-reviewers>";
					
					try {
						formXml += xmlProcessor.replaceRootTagWith(contractForm.getMetaDataXml(), "meta");
					} catch (SAXException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					formXml += "<form-type id=\"" + formTypeMachineReadable
							+ "\">"
							+ contractForm.getContractFormType().getDescription()
							+ "</form-type>";
					formXml += "<form-status><description>"
							+ org.apache.commons.lang.StringEscapeUtils
									.escapeXml(pformStatus.getContractFormStatus()
											.getDescription())
							+ "</description><modified-at>" + pformStatus.getModified()
							+ "</modified-at></form-status>";
					formXml += "<form-committee-status><description>"
							+ org.apache.commons.lang.StringEscapeUtils
									.escapeXml(contractFormCommitteeStatus.getContractFormCommitteeStatus()
											.getDescription())
							+ "</description><modified-at>" + contractFormCommitteeStatus.getModified()
							+ "</modified-at><xml-data>" + contractFormCommitteeStatus.getXmlData() + "</xml-data></form-committee-status>";
					formXml += "<latest-logs>";
					if (latestLogLst != null && !latestLogLst.isEmpty()) {
						for (String log : latestLogLst) {
							formXml += log;
						}
						
					}
					formXml += "</latest-logs>";
					
					formXml += "<actions>";
					
					//@TODO need to change to switch or something...
					//if (ContractFormCommitteeStatusEnum.PENDING_IRB_AGENDA_ASSIGNMENT.equals(contractFormCommitteeStatus
					//		.getContractFormCommitteeStatus())){
					//	formXml += "<action><name>ASSIGN_AGENDA</name><url></url></action>";
					//}else 
					if(ContractFormCommitteeStatusEnum.PENDING_REVIEWER_ASSIGNMENT.equals(contractFormCommitteeStatus
							.getContractFormCommitteeStatus())) {
						//logger.debug("c:" + contractFormCommitteeStatus.getCommittee());
						String assignToRole = "";
						switch(contractFormCommitteeStatus.getCommittee()){
						case BUDGET_MANAGER:
							assignToRole = "ROLE_BUDGET_REVIEWER";
							break;
						case COVERAGE_MANAGER:
							assignToRole = "ROLE_COVERAGE_REVIEWER";
							break;
						}
						formXml += "<action><name>ASSIGN_REVIEWER</name><assign-to-role>" +assignToRole + "</assign-to-role><url></url></action>";
					}else{
						formXml += "<action><name>REVIEW</name><url>/contracts/"
								+ contractForm.getContract().getId() + "/contract-forms/"
								+ contractForm.getId() + "/review?committee="
								+ committee + "</url></action>";
					}
					

					formXml += "</actions>";
					formXml += "</form>";
					
					res.add(formXml);
				}
			}

		}
		
		for (String formS:res){
			xmlResult += formS;
		}

		xmlResult += "</list>";

		return xmlResult;
	}
	
	protected synchronized void saveOrUpdateFormMetaDataXml(Form form, String metaDataXml){
		ContractForm contractForm = (ContractForm) form;
		contractForm.setMetaDataXml(metaDataXml);
		contractFormDao.saveOrUpdate(contractForm);
	}
	
	@Override
	public Form getForm(long formId){
		return contractFormDao.findById(formId);
	}

	@Autowired(required = true)
	public void setContractFormStatusDao(
			ContractFormStatusDao contractFormStatusDao) {
		this.contractFormStatusDao = contractFormStatusDao;
	}

	public ContractFormStatusDao getContractFormStatusDao() {
		return contractFormStatusDao;
	}

	@Autowired(required = true)
	public void setContractFormXmlDataDao(
			ContractFormXmlDataDao contractFormXmlDataDao) {
		this.contractFormXmlDataDao = contractFormXmlDataDao;
	}

	public ContractFormXmlDataDao getContractFormXmlDataDao() {
		return contractFormXmlDataDao;
	}

	@Autowired(required = true)
	public void setContractFormCommitteeStatusDao(
			ContractFormCommitteeStatusDao contractFormCommitteeStatusDao) {
		this.contractFormCommitteeStatusDao = contractFormCommitteeStatusDao;
	}

	public ContractFormCommitteeStatusDao getContractFormCommitteeStatusDao() {
		return contractFormCommitteeStatusDao;
	}

	public String getQueueTemplateXmlUri() {
		return queueTemplateXmlUri;
	}

	public void setQueueTemplateXmlUri(String queueTemplateXmlUri) {
		this.queueTemplateXmlUri = queueTemplateXmlUri;
	}

	public ContractFormDao getContractFormDao() {
		return contractFormDao;
	}

	@Autowired(required = true)
	public void setContractFormDao(ContractFormDao contractFormDao) {
		this.contractFormDao = contractFormDao;
	}

	public ContractTrackService getContractTrackService() {
		return contractTrackService;
	}
	
	@Autowired(required = true)
	public void setContractTrackService(ContractTrackService contractTrackService) {
		this.contractTrackService = contractTrackService;
	}

	
}
