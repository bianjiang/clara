package edu.uams.clara.webapp.contract.web.contractform.review.ajax;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import edu.uams.clara.core.util.xml.DomUtils;
import edu.uams.clara.webapp.common.domain.usercontext.User;
import edu.uams.clara.webapp.common.domain.usercontext.enums.Committee;
import edu.uams.clara.webapp.common.domain.usercontext.enums.Permission;
import edu.uams.clara.webapp.common.service.CommitteeGroupService;
import edu.uams.clara.webapp.common.service.audit.AuditService;
import edu.uams.clara.webapp.common.util.DateFormatUtil;
import edu.uams.clara.webapp.common.util.HTMLHelper;
import edu.uams.clara.webapp.common.util.XMLResponseHelper;
import edu.uams.clara.webapp.contract.dao.businesslogicobject.ContractFormCommitteeCommentDao;
import edu.uams.clara.webapp.contract.dao.businesslogicobject.ContractFormCommitteeStatusDao;
import edu.uams.clara.webapp.contract.domain.businesslogicobject.ContractFormCommitteeComment;
import edu.uams.clara.webapp.contract.domain.businesslogicobject.ContractFormCommitteeStatus;
import edu.uams.clara.webapp.contract.domain.businesslogicobject.enums.ContractFormCommitteeStatusEnum;
import edu.uams.clara.webapp.contract.domain.contractform.ContractForm;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.enums.CommentStatus;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.enums.CommentType;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.enums.ContingencyType;
import edu.uams.clara.webapp.xml.processor.XmlProcessor;

@Controller
public class ContractFormReviewLogicAjaxController {

	private final static Logger logger = LoggerFactory
			.getLogger(ContractFormReviewLogicAjaxController.class);

	private ContractFormCommitteeStatusDao contractFormCommitteeStatusDao;

	private ContractFormCommitteeCommentDao contractFormCommitteeCommentDao;

	private CommitteeGroupService committeeGroupService;

	private AuditService auditService;
	
	/*
	private enum Action {
		REVIEW, ASSIGN_REVIEWER;
	}

	private String getAction(Action action,
			ContractFormCommitteeStatus contractFormCommitteeStatus, User user) {
		String actionXml = "<action>";
		switch (action) {
		case REVIEW:
			actionXml += "<value>REVIEW</value><name>Review</name><url>javascript:gotoRelativeUrl('/contracts/"
					+ contractFormCommitteeStatus.getContractForm()
							.getContract().getId()
					+ "/contract-forms/"
					+ contractFormCommitteeStatus.getContractFormId()
					+ "/review?committee="
					+ contractFormCommitteeStatus.getCommittee().toString()
					+ "');</url>";
			break;
		case ASSIGN_REVIEWER:
			// String reviewUrl = "protocols/" +
			// protocolFormCommitteeStatus.getProtocolForm().getProtocol().getId()
			// + "/protocol-forms/" +
			// protocolFormCommitteeStatus.getProtocolForm().getId() +
			// "/review?committee=" +
			// protocolFormCommitteeStatus.getCommittee().toString();
			actionXml += "<value>ASSIGN_REVIEWER</value><name>Assign Reviewer(s)</name><url>javascript:Clara.Application.QueueController.Reassign({roleId:'"
					+ Permission.ROLE_CONTRACT_LEGAL_REVIEW
					+ "',formId:"
					+ contractFormCommitteeStatus.getContractForm().getId()
					+ ",committee:'"
					+ contractFormCommitteeStatus.getCommittee().toString()
					+ "'},{objectType:'Protocol'});</url>";
			break;
		default:
			break;
		}

		actionXml += "</action>";

		return actionXml;
	}

	private List<String> getContractFormActionsByUser(
			ContractFormCommitteeStatus contractFormCommitteeStatus, User user) {

		List<String> actions = new ArrayList<String>();

		switch (contractFormCommitteeStatus.getCommittee()) {
		case CONTRACT_LEGAL_REVIEW:
			if (user.getAuthorities()
					.contains(Permission.ROLE_CONTRACT_LEGAL_REVIEW)
					&& !contractFormCommitteeStatus
							.getContractFormCommitteeStatus().equals(
									ContractFormCommitteeStatusEnum.IN_REVIEW)) {
				actions.add(getAction(Action.ASSIGN_REVIEWER,
						contractFormCommitteeStatus, user));
			}
			break;

		default:
			if (user.getAuthorities().contains(
					contractFormCommitteeStatus.getCommittee()
							.getRolePermissionIdentifier())
					&& !contractFormCommitteeStatus
							.getContractFormCommitteeStatus().equals(
									ContractFormCommitteeStatusEnum.IN_REVIEW)) {
				actions.add(getAction(Action.REVIEW,
						contractFormCommitteeStatus, user));
			}
			break;

		}

		return actions;
	}
	*/
	
	private XmlProcessor xmlProcessor;

	@RequestMapping(value = "/ajax/contracts/{contractId}/contract-forms/{contractFormId}/review/committee-statuses/list.xml", method = RequestMethod.GET)
	public @ResponseBody
	String listFormCommitteeStatus(
			@PathVariable("contractFormId") long contractFormId) {
		List<ContractFormCommitteeStatus> contractFormCommitteeStatuses = contractFormCommitteeStatusDao
				.listLatestByContractFormId(contractFormId);

		User u = (User) SecurityContextHolder.getContext().getAuthentication()
				.getPrincipal();

		String xmlResult = "<list>";
		for (ContractFormCommitteeStatus fcs : contractFormCommitteeStatuses) {
			xmlResult += "<contract-form-committee-status id=\"" + fcs.getId()
					+ "\" contractFormId=\"" + fcs.getContractForm().getId()
					+ "\">";
			xmlResult += "<committee>" + fcs.getCommittee().getDescription()
					+ "</committee>";
			xmlResult += "<committee_code>" + fcs.getCommittee().toString()
					+ "</committee_code>";
			xmlResult += "<parent_committee_code>"
					+ committeeGroupService.getParentCommittee(fcs
							.getCommittee()) + "</parent_committee_code>";
			xmlResult += "<status priority=\""
					+ fcs.getContractFormCommitteeStatus().getPriorityLevel()
					+ "\">"
					+ fcs.getContractFormCommitteeStatus().getDescription()
					+ "</status>";
			xmlResult += "<modified>"
					+ DateFormatUtil.formateDate(fcs.getModified())
					+ "</modified>";

			String extraXmlData = fcs.getContractForm().getMetaDataXml();

			String assignedReviewersXml = "";
			if (extraXmlData != null && !extraXmlData.isEmpty()) {
				try {
					Document extraXmlDataDoc = xmlProcessor
							.loadXmlStringToDOM(extraXmlData);

					XPath xPath = xmlProcessor.getXPathInstance();

					NodeList assignedReviewers = (NodeList) xPath.evaluate(
							"//assigned-reviewer[@assigning-committee='"
									+ fcs.getCommittee()
									+ "' or @user-role-committee='"
									+ fcs.getCommittee() + "']",
							extraXmlDataDoc, XPathConstants.NODESET);

					for (int j = 0; j < assignedReviewers.getLength(); j++) {

						Element assignedReviewerEl = (Element) assignedReviewers
								.item(j);

						assignedReviewersXml += DomUtils
								.elementToString(assignedReviewerEl);
						logger.debug("formId: " + fcs.getContractForm().getId()
								+ " assignedReviewer xml: "
								+ assignedReviewersXml);

					}
				} catch (Exception ex) {
					// return XMLResponseHelper.getXmlResponseStub(true,
					// "errors when grabing the assigned reviewers from form metadata",
					// null);
				}
			}

			xmlResult += "<assigned-reviewers>";
			if (!assignedReviewersXml.isEmpty()) {
				xmlResult += assignedReviewersXml;
			}
			xmlResult += "</assigned-reviewers>";
			
			/*
			xmlResult += "<actions>";
			
			List<String> actions = getContractFormActionsByUser(fcs, u);
			for (String action : actions) {
				xmlResult += action;
			}
			xmlResult += "</actions>";
			*/
			xmlResult += "</contract-form-committee-status>";
		}
		xmlResult += "</list>";
		return xmlResult;
	}

	@RequestMapping(value = "/ajax/contracts/{contractId}/contract-forms/{contractFormId}/review/committee-statuses/{committee}/list", method = RequestMethod.GET)
	public @ResponseBody
	List<ContractFormCommitteeStatus> listFormCommitteeStatusByCommittee(
			@PathVariable("contractFormId") long contractFormId,
			@PathVariable("committee") Committee committee) {

		List<ContractFormCommitteeStatus> contractFormCommitteeStatuses = contractFormCommitteeStatusDao
				.listAllByCommitteeAndContractFormId(committee, contractFormId);

		return contractFormCommitteeStatuses;
	}

	@RequestMapping(value = "/ajax/contracts/{contractId}/contract-forms/{contractFormId}/review/committee-comments/save", method = RequestMethod.POST)
	public @ResponseBody
	String saveContractFormCommitteeComment(
			@PathVariable("contractFormId") long contractFormId,
			@RequestParam("committee") Committee committee,
			@RequestParam("text") String text,
			@RequestParam("commentType") CommentType commentType,
			@RequestParam(value = "commentStatus", required = false) CommentStatus commentStatus,
			@RequestParam("userId") long userId,
			@RequestParam(value = "replyToId", required = false) Long replyToId,
			@RequestParam("inLetter") boolean inLetter,
			@RequestParam("isPrivate") boolean isPrivate) {

		ContractForm contractForm = new ContractForm();
		contractForm.setId(contractFormId);

		User user = new User();
		user.setId(userId);

		ContractFormCommitteeComment parent = null;

		ContractFormCommitteeComment contractFormCommitteeComment = new ContractFormCommitteeComment();
		contractFormCommitteeComment.setCommentType(commentType);
		contractFormCommitteeComment.setCommentStatus(commentStatus);
		contractFormCommitteeComment.setCommittee(committee);
		contractFormCommitteeComment.setUser(user);

		contractFormCommitteeComment
				.setText(HTMLHelper.convertLinebreaks(text));
		contractFormCommitteeComment.setContractForm(contractForm);
		contractFormCommitteeComment.setModified(new Date());
		contractFormCommitteeComment.setInLetter(inLetter);
		contractFormCommitteeComment.setPrivate(isPrivate);
		if (replyToId != null) {
			parent = new ContractFormCommitteeComment();
			parent.setId(replyToId);
		} else {
			parent = contractFormCommitteeComment;
		}

		contractFormCommitteeComment.setReplyTo(parent);
		contractFormCommitteeCommentDao
				.saveOrUpdate(contractFormCommitteeComment);

		return XMLResponseHelper.xmlResult(Boolean.TRUE);
	}

	@RequestMapping(value = "/ajax/contracts/{contractId}/contract-forms/{contractFormId}/review/committee-comments/{contractFormCommitteeCommentId}/remove", method = RequestMethod.GET)
	public @ResponseBody
	String removeContractFormCommitteeComment(
			@PathVariable("contractFormCommitteeCommentId") long contractFormCommitteeCommentId,
			@RequestParam("userId") long userId) {

		ContractFormCommitteeComment contractFormCommitteeComment = contractFormCommitteeCommentDao
				.findById(contractFormCommitteeCommentId);

		contractFormCommitteeComment.setRetired(Boolean.TRUE);

		contractFormCommitteeCommentDao
				.saveOrUpdate(contractFormCommitteeComment);

		return XMLResponseHelper.xmlResult(Boolean.TRUE);
	}

	@RequestMapping(value = "/ajax/contracts/{contractId}/contract-forms/{contractFormId}/review/committee-comments/{contractFormCommitteeCommentId}/update", method = RequestMethod.POST)
	public @ResponseBody
	String updateContractFormCommitteeComment(
			@PathVariable("contractFormCommitteeCommentId") long contractFormCommitteeCommentId,
			@RequestParam(value = "text", required = false) String text,
			@RequestParam(value = "commentType", required = false) CommentType commentType,
			@RequestParam(value = "contingencyType", required = false) ContingencyType contingencyType,
			@RequestParam(value = "contingencySeverity", required = false) Boolean contingencySeverity,
			@RequestParam(value = "commentStatus", required = false) CommentStatus commentStatus,
			@RequestParam("userId") long userId,
			@RequestParam(value = "inLetter", required = false) boolean inLetter,
			@RequestParam(value = "isPrivate", required = false) boolean isPrivate) {

		ContractFormCommitteeComment contractFormCommitteeComment = contractFormCommitteeCommentDao
				.findById(contractFormCommitteeCommentId);

		if (text != null) {
			contractFormCommitteeComment.setText(text);
		}

		if (commentType != null) {
			contractFormCommitteeComment.setCommentType(commentType);
		}

		
		//if (commentStatus != null) {
			contractFormCommitteeComment.setCommentStatus(commentStatus);
		//}

		contractFormCommitteeComment.setInLetter(false || inLetter);
		contractFormCommitteeComment.setPrivate(false || isPrivate);

		contractFormCommitteeCommentDao
				.saveOrUpdate(contractFormCommitteeComment);

		return XMLResponseHelper.xmlResult(Boolean.TRUE);
	}

	@RequestMapping(value = "/ajax/contracts/{contractId}/contract-forms/{contractFormId}/review/committee-comments/{contractFormCommitteeCommentId}/statuses/changeseverity", method = RequestMethod.POST)
	public @ResponseBody
	String changeContractFormCommitteeCommentStatus(
			@PathVariable("contractFormCommitteeCommentId") long contractFormCommitteeCommentId,
			@RequestParam(value = "contingencySeverity", required = false) Boolean contingencySeverity) {

		ContractFormCommitteeComment contractFormCommitteeComment = contractFormCommitteeCommentDao
				.findById(contractFormCommitteeCommentId);

		// contractFormCommitteeComment.setContingencySeverity(contingencySeverity);

		contractFormCommitteeCommentDao
				.saveOrUpdate(contractFormCommitteeComment);

		return XMLResponseHelper.xmlResult(Boolean.TRUE);
	}

	@RequestMapping(value = "/ajax/contracts/{contractId}/contract-forms/{contractFormId}/review/committee-comments/{contractFormCommitteeCommentId}/statuses/change", method = RequestMethod.POST)
	public @ResponseBody
	String changeContractFormCommitteeCommentStatus(
			@PathVariable("contractFormCommitteeCommentId") long contractFormCommitteeCommentId,
			@RequestParam(value = "commentStatus", required = false) CommentStatus commentStatus) {

		ContractFormCommitteeComment contractFormCommitteeComment = contractFormCommitteeCommentDao
				.findById(contractFormCommitteeCommentId);

		contractFormCommitteeComment.setCommentStatus(commentStatus);

		contractFormCommitteeCommentDao
				.saveOrUpdate(contractFormCommitteeComment);

		return XMLResponseHelper.xmlResult(Boolean.TRUE);
	}

	@RequestMapping(value = "/ajax/contracts/{contractId}/contract-forms/{contractFormId}/review/committee-comments/list", method = RequestMethod.GET)
	public @ResponseBody
	List<ContractFormCommitteeComment> listContractFormCommitteeComment(
			@RequestParam("userId") long userId,
			@PathVariable("contractFormId") long contractFormId) {

		List<ContractFormCommitteeComment> contractFormCommitteeComments = contractFormCommitteeCommentDao
				.listAllParentsByContractFormIdExcludingByUserId(
						contractFormId, userId);

		return contractFormCommitteeComments;
	}

	@RequestMapping(value = "/ajax/contracts/{contractId}/contract-forms/{contractFormId}/review/review-status", method = RequestMethod.GET)
	public @ResponseBody
	String getReviewStatus(@PathVariable("contractFormId") long contractFormId,
			@RequestParam("committee") Committee committee) {

		try {
			ContractFormCommitteeStatus contractFormCommitteeStatus = contractFormCommitteeStatusDao
					.getLatestByCommitteeAndContractFormId(committee,
							contractFormId);
			if (contractFormCommitteeStatus != null) {
				// return
				// XMLResponseHelper.xmlResult(contractFormCommitteeStatus.getContractFormCommitteeStatus());
				return XMLResponseHelper.xmlResult("True");
			} else {
				return XMLResponseHelper.xmlResult("False");
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			return XMLResponseHelper.xmlResult("False");
		}
	}

	@RequestMapping(value = "/ajax/contracts/{contractId}/contract-forms/{contractFormId}/review/committee-comments/{contractFormCommitteeCommentId}/move", method = RequestMethod.POST)
	public @ResponseBody
	String moveContractFormCommitteeComment(
			@PathVariable("contractFormCommitteeCommentId") long contractFormCommitteeCommentId,
			@RequestParam("committee") Committee committee,
			@RequestParam("userId") long userId,
			@RequestParam(value = "makeCopy", required = false) boolean makeCopy) {
		ContractFormCommitteeComment contractFormCommitteeComment = contractFormCommitteeCommentDao
				.findById(contractFormCommitteeCommentId);

		User user = new User();
		user.setId(userId);

		if (makeCopy) {
			try {
				ContractFormCommitteeComment newContractFormCommitteeComment = new ContractFormCommitteeComment();
				newContractFormCommitteeComment
						.setCommentType(contractFormCommitteeComment
								.getCommentType());
				newContractFormCommitteeComment.setCommittee(committee);
				// newContractFormCommitteeComment.setContingencyType(contractFormCommitteeComment.getContingencyType());
				newContractFormCommitteeComment.setModified(new Date());
				newContractFormCommitteeComment
						.setText(contractFormCommitteeComment.getText());
				newContractFormCommitteeComment
						.setContractForm(contractFormCommitteeComment
								.getContractForm());
				newContractFormCommitteeComment.setUser(user);
				newContractFormCommitteeComment
						.setCommentStatus(contractFormCommitteeComment
								.getCommentStatus());
				// newContractFormCommitteeComment.setContingencySeverity(contractFormCommitteeComment.getContingencySeverity());
				newContractFormCommitteeComment
						.setInLetter(contractFormCommitteeComment.isInLetter());

				contractFormCommitteeCommentDao
						.saveOrUpdate(newContractFormCommitteeComment);

				newContractFormCommitteeComment
						.setReplyTo(newContractFormCommitteeComment);
				contractFormCommitteeCommentDao
						.saveOrUpdate(newContractFormCommitteeComment);

				auditService.auditEvent("COMMENT_COPIED",
						committee.getDescription()
								+ " has made a copy of comment "
								+ contractFormCommitteeComment.getId(),
						newContractFormCommitteeComment);

				return XMLResponseHelper.xmlResult(Boolean.TRUE);
			} catch (Exception e) {
				e.printStackTrace();
				return XMLResponseHelper.xmlResult(Boolean.FALSE);
			}

		} else {
			try {
				contractFormCommitteeComment.setCommittee(committee);
				contractFormCommitteeComment.setUser(user);
				contractFormCommitteeComment.setModified(new Date());

				contractFormCommitteeCommentDao
						.saveOrUpdate(contractFormCommitteeComment);
				auditService.auditEvent(
						"COMMENT_MOVED",
						"Comment " + contractFormCommitteeComment.getId()
								+ " has been moved to committee "
								+ committee.getDescription(),
						contractFormCommitteeComment);

				return XMLResponseHelper.xmlResult(Boolean.TRUE);
			} catch (Exception e) {
				e.printStackTrace();
				return XMLResponseHelper.xmlResult(Boolean.FALSE);
			}

		}

	}

	@Autowired(required = true)
	public void setContractFormCommitteeStatusDao(
			ContractFormCommitteeStatusDao contractFormCommitteeStatusDao) {
		this.contractFormCommitteeStatusDao = contractFormCommitteeStatusDao;
	}

	public ContractFormCommitteeStatusDao getContractFormCommitteeStatusDao() {
		return contractFormCommitteeStatusDao;
	}

	@Autowired(required = true)
	public void setContractFormCommitteeCommentDao(
			ContractFormCommitteeCommentDao contractFormCommitteeCommentDao) {
		this.contractFormCommitteeCommentDao = contractFormCommitteeCommentDao;
	}

	public ContractFormCommitteeCommentDao getContractFormCommitteeCommentDao() {
		return contractFormCommitteeCommentDao;
	}

	public XmlProcessor getXmlProcessor() {
		return xmlProcessor;
	}

	@Autowired(required = true)
	public void setXmlProcessor(XmlProcessor xmlProcessor) {
		this.xmlProcessor = xmlProcessor;
	}

	public CommitteeGroupService getCommitteeGroupService() {
		return committeeGroupService;
	}

	@Autowired(required = true)
	public void setCommitteeGroupService(
			CommitteeGroupService committeeGroupService) {
		this.committeeGroupService = committeeGroupService;
	}

	public AuditService getAuditService() {
		return auditService;
	}

	@Autowired(required = true)
	public void setAuditService(AuditService auditService) {
		this.auditService = auditService;
	}
}
