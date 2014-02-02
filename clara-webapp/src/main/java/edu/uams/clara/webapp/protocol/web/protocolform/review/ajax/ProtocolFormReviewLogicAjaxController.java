package edu.uams.clara.webapp.protocol.web.protocolform.review.ajax;

import java.util.Date;
import java.util.List;
import java.util.UUID;

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
import edu.uams.clara.webapp.common.dao.usercontext.UserDao;
import edu.uams.clara.webapp.common.domain.history.Track;
import edu.uams.clara.webapp.common.domain.usercontext.User;
import edu.uams.clara.webapp.common.domain.usercontext.enums.Committee;
import edu.uams.clara.webapp.common.service.CommitteeGroupService;
import edu.uams.clara.webapp.common.service.audit.AuditService;
import edu.uams.clara.webapp.common.service.history.AgendaItemTrackService;
import edu.uams.clara.webapp.common.util.DateFormatUtil;
import edu.uams.clara.webapp.common.util.HTMLHelper;
import edu.uams.clara.webapp.common.util.XMLResponseHelper;
import edu.uams.clara.webapp.protocol.dao.businesslogicobject.ProtocolFormCommitteeCommentDao;
import edu.uams.clara.webapp.protocol.dao.businesslogicobject.ProtocolFormCommitteeStatusDao;
import edu.uams.clara.webapp.protocol.dao.irb.AgendaItemDao;
import edu.uams.clara.webapp.protocol.dao.protocolform.ProtocolFormDao;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.ProtocolFormCommitteeComment;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.ProtocolFormCommitteeStatus;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.enums.CommentStatus;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.enums.CommentType;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.enums.ContingencyType;
import edu.uams.clara.webapp.protocol.domain.irb.AgendaItem;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolForm;
import edu.uams.clara.webapp.xml.processor.XmlProcessor;

@Controller
public class ProtocolFormReviewLogicAjaxController {

	private final static Logger logger = LoggerFactory
			.getLogger(ProtocolFormReviewLogicAjaxController.class);

	private ProtocolFormCommitteeStatusDao protocolFormCommitteeStatusDao;

	private ProtocolFormCommitteeCommentDao protocolFormCommitteeCommentDao;

	private ProtocolFormDao protocolFormDao;

	private CommitteeGroupService committeeGroupService;

	private AuditService auditService;
	
	private AgendaItemTrackService agendaItemTrackService;
	
	private AgendaItemDao agendaItemDao;
	
	private UserDao userDao;

	/*Move review and re-assign links to form actions
	 * private enum Action {
		REVIEW, ASSIGN_REVIEWER;
	}

	private String getAction(Action action,
			ProtocolFormCommitteeStatus protocolFormCommitteeStatus, User user) {
		String actionXml = "<action>";
		switch (action) {
		case REVIEW:
			actionXml += "<value>REVIEW</value><name>Review</name><url>javascript:gotoRelativeUrl('/protocols/"
					+ protocolFormCommitteeStatus.getProtocolForm()
							.getProtocol().getId()
					+ "/protocol-forms/"
					+ protocolFormCommitteeStatus.getProtocolFormId()
					+ "/review?committee="
					+ protocolFormCommitteeStatus.getCommittee().toString()
					+ "');</url>";
			break;
		case ASSIGN_REVIEWER:
			// String reviewUrl = "protocols/" +
			// protocolFormCommitteeStatus.getProtocolForm().getProtocol().getId()
			// + "/protocol-forms/" +
			// protocolFormCommitteeStatus.getProtocolForm().getId() +
			// "/review?committee=" +
			// protocolFormCommitteeStatus.getCommittee().toString();
			actionXml += "<value>ASSIGN_REVIEWER</value><name>Assign Reviewer(s)</name><url>javascript:Clara.Queues.Reassign({roleId:'"
					+ Permission.ROLE_BUDGET_MANAGER
					+ "',formId:"
					+ protocolFormCommitteeStatus.getProtocolForm().getId()
					+ ",committee:'"
					+ protocolFormCommitteeStatus.getCommittee().toString()
					+ "'},{objectType:'Protocol'});</url>";
			break;
		default:
			break;
		}

		actionXml += "</action>";

		return actionXml;
	}

	private List<String> getProtocolFormActionsByUser(
			ProtocolFormCommitteeStatus protocolFormCommitteeStatus, User user) {
		List<String> actions = new ArrayList<String>();

		switch (protocolFormCommitteeStatus.getCommittee()) {
		case BUDGET_MANAGER:
			if (user.getAuthorities().contains(Permission.ROLE_BUDGET_MANAGER) && !protocolFormCommitteeStatus.getProtocolFormCommitteeStatus().equals(ProtocolFormCommitteeStatusEnum.IN_REVIEW)) {
				actions.add(getAction(Action.ASSIGN_REVIEWER,
						protocolFormCommitteeStatus, user));
			}
			break;
		case REGULATORY_MANAGER:
			if (user.getAuthorities().contains(Permission.ROLE_REGULATORY_MANAGER)) {
				actions.add(getAction(Action.ASSIGN_REVIEWER,
						protocolFormCommitteeStatus, user));
			}
			break;
		default:
			if (user.getAuthorities().contains(
					protocolFormCommitteeStatus.getCommittee()
							.getRolePermissionIdentifier()) && !protocolFormCommitteeStatus.getProtocolFormCommitteeStatus().equals(ProtocolFormCommitteeStatusEnum.IN_REVIEW) && !protocolFormCommitteeStatus.getProtocolFormCommitteeStatus().equals(ProtocolFormCommitteeStatusEnum.NOTIFIED)) {
				actions.add(getAction(Action.REVIEW,
						protocolFormCommitteeStatus, user));
			}
			break;
		}

		return actions;
	}
	*/

	private XmlProcessor xmlProcessor;

	@RequestMapping(value = "/ajax/protocols/{protocolId}/protocol-forms/{protocolFormId}/review/committee-statuses/list.xml", method = RequestMethod.GET)
	public @ResponseBody
	String listFormCommitteeStatus(
			@PathVariable("protocolFormId") long protocolFormId) {
		List<ProtocolFormCommitteeStatus> protocolFormCommitteeStatuses = protocolFormCommitteeStatusDao
				.listLatestByProtocolFormId(protocolFormId);

		User u = (User) SecurityContextHolder.getContext().getAuthentication()
				.getPrincipal();

		String xmlResult = "<list>";
		for (ProtocolFormCommitteeStatus fcs : protocolFormCommitteeStatuses) {
			xmlResult += "<protocol-form-committee-status id=\"" + fcs.getId()
					+ "\" protocolFormId=\"" + fcs.getProtocolForm().getId()
					+ "\">";
			xmlResult += "<committee>" + fcs.getCommittee().getDescription()
					+ "</committee>";
			xmlResult += "<committee_code>" + fcs.getCommittee().toString()
					+ "</committee_code>";
			xmlResult += "<parent_committee_code>"
					+ committeeGroupService.getParentCommittee(fcs
							.getCommittee()) + "</parent_committee_code>";
			xmlResult += "<status priority=\""
					+ fcs.getProtocolFormCommitteeStatus().getPriorityLevel()
					+ "\">"
					+ fcs.getProtocolFormCommitteeStatus().getDescription()
					+ "</status>";
			xmlResult += "<modified>"
					+ DateFormatUtil.formateDate(fcs.getModified())
					+ "</modified>";
			
			/*
			String extraXmlData = fcs.getProtocolForm().getMetaDataXml();

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
						logger.debug("formId: " + fcs.getProtocolForm().getId()
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
			 * xmlResult += "<actions>";
			 */
			/*
			List<String> actions = getProtocolFormActionsByUser(fcs, u);
			for (String action : actions) {
				xmlResult += action;
			}
			xmlResult += "</actions>";
			*/
			xmlResult += "</protocol-form-committee-status>";
		}
		xmlResult += "</list>";
		return xmlResult;
	}

	@RequestMapping(value = "/ajax/protocols/{protocolId}/protocol-forms/{protocolFormId}/review/committee-statuses/{committee}/list", method = RequestMethod.GET)
	public @ResponseBody
	List<ProtocolFormCommitteeStatus> listFormCommitteeStatusByCommittee(
			@PathVariable("protocolFormId") long protocolFormId,
			@PathVariable("committee") Committee committee) {

		List<ProtocolFormCommitteeStatus> protocolFormCommitteeStatuses = protocolFormCommitteeStatusDao
				.listAllByCommitteeAndProtocolFormId(committee, protocolFormId);

		return protocolFormCommitteeStatuses;
	}
	
	private void updateAgendaItemlog(User user, long agendaItemId, String eventType, String logText){
		Date now = new Date();
		try{
			AgendaItem agendaItem = agendaItemDao.findById(agendaItemId);
			
			Track track = agendaItemTrackService.getOrCreateTrack("AGENDAITEM", agendaItemId);
			
			Document logsDoc = agendaItemTrackService.getLogsDocument(track);
			
			Element logEl = logsDoc.createElement("log");
			
			String logId = UUID.randomUUID().toString();
			
			logEl.setAttribute("id", logId);
			logEl.setAttribute("parent-id", logId);
			logEl.setAttribute("action-user-id", String.valueOf(user.getId()));
			logEl.setAttribute("actor", user.getPerson().getFullname());
			logEl.setAttribute("date-time", DateFormatUtil.formateDate(now));
			logEl.setAttribute("event-type", eventType);
			logEl.setAttribute("form-id", String.valueOf(agendaItem.getProtocolFormId()));
			logEl.setAttribute("parent-form-id", String.valueOf(agendaItem.getProtocolForm().getParentFormId()));
			logEl.setAttribute("type", agendaItem.getProtocolForm().getFormType());
			logEl.setAttribute("log-type", "ACTION");
			logEl.setAttribute("timestamp", String.valueOf(now.getTime()));

			logEl.setTextContent(logText);

			logsDoc.getDocumentElement().appendChild(logEl);
			
			track = agendaItemTrackService.updateTrack(track, logsDoc);
		} catch (Exception e){
			e.printStackTrace();
		}
	}

	@RequestMapping(value = "/ajax/protocols/{protocolId}/protocol-forms/{protocolFormId}/review/committee-comments/save", method = RequestMethod.POST)
	public @ResponseBody
	String saveProtocolFormCommitteeComment(
			@PathVariable("protocolFormId") long protocolFormId,
			@RequestParam("committee") Committee committee,
			@RequestParam("text") String text,
			@RequestParam("commentType") CommentType commentType,
			@RequestParam(value = "commentStatus", required = false) CommentStatus commentStatus,
			@RequestParam("userId") long userId,
			@RequestParam(value = "replyToId", required = false) Long replyToId,
			@RequestParam("inLetter") boolean inLetter,
			@RequestParam("isPrivate") boolean isPrivate,
			@RequestParam(value = "version", required = false) boolean version,
			@RequestParam(value = "agendaItemId", required = false) Long agendaItemId) {
		User user = userDao.findById(userId);
		try{
			if (version && agendaItemId != null){
				String message = "New Comment: \""+ text +"\" has been added by "
						+ user.getPerson().getFullname() +"";
				updateAgendaItemlog(user, agendaItemId, "ADD_COMMENT", message);
			}
		} catch (Exception e){
			e.printStackTrace();
		}
		
		
		try{
			ProtocolForm protocolForm = new ProtocolForm();
			protocolForm.setId(protocolFormId);

			ProtocolFormCommitteeComment parent = null;

			ProtocolFormCommitteeComment protocolFormCommitteeComment = new ProtocolFormCommitteeComment();
			protocolFormCommitteeComment.setCommentType(commentType);
			protocolFormCommitteeComment.setCommentStatus(commentStatus);
			protocolFormCommitteeComment.setCommittee(committee);
			protocolFormCommitteeComment.setUser(user);

			protocolFormCommitteeComment
					.setText(HTMLHelper.convertLinebreaks(text));
			protocolFormCommitteeComment.setProtocolForm(protocolForm);
			protocolFormCommitteeComment.setModified(new Date());
			protocolFormCommitteeComment.setInLetter(inLetter);
			protocolFormCommitteeComment.setPrivate(isPrivate);
			if (replyToId != null) {
				parent = new ProtocolFormCommitteeComment();
				parent.setId(replyToId);
			} else {
				parent = protocolFormCommitteeComment;
			}

			protocolFormCommitteeComment.setReplyTo(parent);
			protocolFormCommitteeCommentDao
					.saveOrUpdate(protocolFormCommitteeComment);
		} catch (Exception e){
			e.printStackTrace();
		}

		return XMLResponseHelper.xmlResult(Boolean.TRUE);
	}

	@RequestMapping(value = "/ajax/protocols/{protocolId}/protocol-forms/{protocolFormId}/review/committee-comments/{protocolFormCommitteeCommentId}/remove", method = RequestMethod.GET)
	public @ResponseBody
	String removeProtocolFormCommitteeComment(
			@PathVariable("protocolFormCommitteeCommentId") long protocolFormCommitteeCommentId,
			@RequestParam("userId") long userId,
			@RequestParam(value = "version", required = false) boolean version,
			@RequestParam(value = "agendaItemId", required = false) Long agendaItemId) {

		ProtocolFormCommitteeComment protocolFormCommitteeComment = protocolFormCommitteeCommentDao
				.findById(protocolFormCommitteeCommentId);
		
		User user = userDao.findById(userId);
		
		if (version){
			String message = "Comment: \""+ protocolFormCommitteeComment.getText() +"\" has been deleted by "
					+ user.getPerson().getFullname() +"";
			updateAgendaItemlog(user, agendaItemId, "DELETE_COMMENT", message);
		}

		logger.debug("DELETING?"+protocolFormCommitteeComment.getText());
		protocolFormCommitteeComment.setRetired(Boolean.TRUE);

		protocolFormCommitteeCommentDao
				.saveOrUpdate(protocolFormCommitteeComment);

		return XMLResponseHelper.xmlResult(Boolean.TRUE);
	}

	@RequestMapping(value = "/ajax/protocols/{protocolId}/protocol-forms/{protocolFormId}/review/committee-comments/{protocolFormCommitteeCommentId}/update", method = RequestMethod.POST)
	public @ResponseBody
	String updateProtocolFormCommitteeComment(
			@PathVariable("protocolFormCommitteeCommentId") long protocolFormCommitteeCommentId,
			@RequestParam(value = "text", required = false) String text,
			@RequestParam(value = "commentType", required = false) CommentType commentType,
			@RequestParam(value = "contingencyType", required = false) ContingencyType contingencyType,
			@RequestParam(value = "contingencySeverity", required = false) Boolean contingencySeverity,
			@RequestParam(value = "commentStatus", required = false) CommentStatus commentStatus,
			@RequestParam("userId") long userId,
			@RequestParam(value = "inLetter", required = false) boolean inLetter,
			@RequestParam(value = "isPrivate", required = false) boolean isPrivate,
			@RequestParam(value = "version", required = false) boolean version,
			@RequestParam(value = "agendaItemId", required = false) Long agendaItemId,
			@RequestParam(value = "committee", required = false) Committee committee) {

		ProtocolFormCommitteeComment protocolFormCommitteeComment = protocolFormCommitteeCommentDao
				.findById(protocolFormCommitteeCommentId);
		
		logger.info("here");
		
		
		logger.info("here");
		try{
			User user = userDao.findById(userId);
			protocolFormCommitteeComment.setUser(user);
			protocolFormCommitteeComment.setModified(new Date());
			
			if (version){
				String message = "Comment: \""+ protocolFormCommitteeComment.getText() +"\" has been updated to \""+ text +"\" by "
						+ user.getPerson().getFullname() + "";
				updateAgendaItemlog(user, agendaItemId, "UPDATE_COMMENT", message);

				/*
				ProtocolFormCommitteeComment childProtocolFormCommitteeComment = new ProtocolFormCommitteeComment();
				childProtocolFormCommitteeComment.setCommentStatus(commentStatus);
				childProtocolFormCommitteeComment.setCommentType(commentType);
				childProtocolFormCommitteeComment.setCommittee(protocolFormCommitteeComment.getCommittee());
				childProtocolFormCommitteeComment.setInLetter(inLetter);
				childProtocolFormCommitteeComment.setModified(new Date());
				childProtocolFormCommitteeComment.setProtocolForm(protocolFormCommitteeComment.getProtocolForm());
				childProtocolFormCommitteeComment.setReplyTo(protocolFormCommitteeComment);
				childProtocolFormCommitteeComment.setRetired(false);
				childProtocolFormCommitteeComment.setText(text);
				childProtocolFormCommitteeComment.setUser(user);
				
				protocolFormCommitteeCommentDao.saveOrUpdate(childProtocolFormCommitteeComment);
				*/
			} 

			if (text != null) {
				protocolFormCommitteeComment.setText(HTMLHelper.convertLinebreaks(text));
			}

			if (commentType != null) {
				protocolFormCommitteeComment.setCommentType(commentType);
			}
			
			if (committee != null) {
				protocolFormCommitteeComment.setCommittee(committee);
			}

			// if(contingencyType != null){
			// protocolFormCommitteeComment.setContingencyType(contingencyType);
			// }

			//if (commentStatus != null) {
				protocolFormCommitteeComment.setCommentStatus(commentStatus);
			//}

			// if(contingencySeverity != null){
			// protocolFormCommitteeComment.setContingencySeverity(contingencySeverity);
			// }
			
			protocolFormCommitteeComment.setInLetter(false || inLetter);
			
			protocolFormCommitteeComment.setPrivate(false || isPrivate);

			protocolFormCommitteeCommentDao
					.saveOrUpdate(protocolFormCommitteeComment);
			
		} catch (Exception e){
			e.printStackTrace();
			
			XMLResponseHelper.xmlResult(Boolean.FALSE);
		}
		

		return XMLResponseHelper.xmlResult(Boolean.TRUE);
	}

	@RequestMapping(value = "/ajax/protocols/{protocolId}/protocol-forms/{protocolFormId}/review/committee-comments/{protocolFormCommitteeCommentId}/statuses/changeseverity", method = RequestMethod.POST)
	public @ResponseBody
	String changeProtocolFormCommitteeCommentStatus(
			@PathVariable("protocolFormCommitteeCommentId") long protocolFormCommitteeCommentId,
			@RequestParam(value = "contingencySeverity", required = false) Boolean contingencySeverity) {

		ProtocolFormCommitteeComment protocolFormCommitteeComment = protocolFormCommitteeCommentDao
				.findById(protocolFormCommitteeCommentId);

		// protocolFormCommitteeComment.setContingencySeverity(contingencySeverity);

		protocolFormCommitteeCommentDao
				.saveOrUpdate(protocolFormCommitteeComment);

		return XMLResponseHelper.xmlResult(Boolean.TRUE);
	}

	@RequestMapping(value = "/ajax/protocols/{protocolId}/protocol-forms/{protocolFormId}/review/committee-comments/{protocolFormCommitteeCommentId}/statuses/change", method = RequestMethod.POST)
	public @ResponseBody
	String changeProtocolFormCommitteeCommentStatus(
			@PathVariable("protocolFormCommitteeCommentId") long protocolFormCommitteeCommentId,
			@RequestParam(value = "commentStatus", required = false) CommentStatus commentStatus) {

		ProtocolFormCommitteeComment protocolFormCommitteeComment = protocolFormCommitteeCommentDao
				.findById(protocolFormCommitteeCommentId);

		protocolFormCommitteeComment.setCommentStatus(commentStatus);

		protocolFormCommitteeCommentDao
				.saveOrUpdate(protocolFormCommitteeComment);

		return XMLResponseHelper.xmlResult(Boolean.TRUE);
	}

	@RequestMapping(value = "/ajax/protocols/{protocolId}/protocol-forms/{protocolFormId}/review/committee-comments/list", method = RequestMethod.GET)
	public @ResponseBody
	List<ProtocolFormCommitteeComment> listProtocolFormCommitteeComment(
			//@RequestParam("committee") Committee committee,
			@RequestParam("userId") long userId,
			@PathVariable("protocolFormId") long protocolFormId) {
		User currentUser = userDao.findById(userId);
		
		List<ProtocolFormCommitteeComment> protocolFormCommitteeComments = protocolFormCommitteeCommentDao
				.listAllParentsByProtocolFormIdExcludingByUserId(
						protocolFormId, currentUser);
		/*
		boolean showIRBComments = false;
		
		if (currentUser.getAuthorities().contains(Permission.VIEW_IRB_COMMENTS)){
			showIRBComments = true;
		} else {
			showIRBComments = false;
		}
		
		if (protocolFormCommitteeComments.size() > 0){
			for (Iterator<ProtocolFormCommitteeComment> iter = protocolFormCommitteeComments.iterator(); iter.hasNext();){
				ProtocolFormCommitteeComment pfcc = iter.next();
				if (!showIRBComments){
					if (irbRelatedCommitteeLst.contains(pfcc.getCommittee()) && !pfcc.isInLetter()){
						iter.remove();
					}
				}
			}
		}*/
		
		
		return protocolFormCommitteeComments;
	}

	@RequestMapping(value = "/ajax/protocols/{protocolId}/protocol-forms/{protocolFormId}/review/review-status", method = RequestMethod.GET)
	public @ResponseBody
	String getReviewStatus(@PathVariable("protocolFormId") long protocolFormId,
			@RequestParam("committee") Committee committee) {

		try {
			if (committee.equals(Committee.PHARMACY_REVIEW)){
				ProtocolForm protocolForm = protocolFormDao.findById(protocolFormId);
				List<String> values = xmlProcessor.listElementStringValuesByPath("/protocol/site-responsible", protocolForm.getMetaDataXml());
				
				String responsibleSite = (values!=null && !values.isEmpty())?values.get(0):"";
				
				if (responsibleSite.equals("ach-achri")){
					return XMLResponseHelper.xmlResult("NO_PHARMACY_REVIEW");
				}
			}
			
			ProtocolFormCommitteeStatus protocolFormCommitteeStatus = protocolFormCommitteeStatusDao
					.getLatestByCommitteeAndProtocolFormId(committee,
							protocolFormId);
			if (protocolFormCommitteeStatus != null) {
				// return
				// XMLResponseHelper.xmlResult(protocolFormCommitteeStatus.getProtocolFormCommitteeStatus());
				return XMLResponseHelper.xmlResult(protocolFormCommitteeStatus.getProtocolFormCommitteeStatus());
			} else {
				return XMLResponseHelper.xmlResult("");
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			return XMLResponseHelper.xmlResult("");
		}
	}

	@RequestMapping(value = "/ajax/protocols/{protocolId}/protocol-forms/{protocolFormId}/review/committee-comments/{protocolFormCommitteeCommentId}/move", method = RequestMethod.POST)
	public @ResponseBody
	String moveProtocolFormCommitteeComment(
			@PathVariable("protocolFormCommitteeCommentId") long protocolFormCommitteeCommentId,
			@RequestParam("committee") Committee committee,
			@RequestParam("userId") long userId,
			@RequestParam(value = "makeCopy", required = false) boolean makeCopy) {
		ProtocolFormCommitteeComment protocolFormCommitteeComment = protocolFormCommitteeCommentDao
				.findById(protocolFormCommitteeCommentId);

		User user = new User();
		user.setId(userId);

		if (makeCopy) {
			try {
				ProtocolFormCommitteeComment newProtocolFormCommitteeComment = new ProtocolFormCommitteeComment();
				newProtocolFormCommitteeComment
						.setCommentType(protocolFormCommitteeComment
								.getCommentType());
				newProtocolFormCommitteeComment.setCommittee(committee);
				// newProtocolFormCommitteeComment.setContingencyType(protocolFormCommitteeComment.getContingencyType());
				newProtocolFormCommitteeComment.setModified(new Date());
				newProtocolFormCommitteeComment
						.setText(protocolFormCommitteeComment.getText());
				newProtocolFormCommitteeComment
						.setProtocolForm(protocolFormCommitteeComment
								.getProtocolForm());
				newProtocolFormCommitteeComment.setUser(user);
				newProtocolFormCommitteeComment
						.setCommentStatus(protocolFormCommitteeComment
								.getCommentStatus());
				// newProtocolFormCommitteeComment.setContingencySeverity(protocolFormCommitteeComment.getContingencySeverity());
				newProtocolFormCommitteeComment
						.setInLetter(protocolFormCommitteeComment.isInLetter());

				protocolFormCommitteeCommentDao
						.saveOrUpdate(newProtocolFormCommitteeComment);

				newProtocolFormCommitteeComment
						.setReplyTo(newProtocolFormCommitteeComment);
				protocolFormCommitteeCommentDao
						.saveOrUpdate(newProtocolFormCommitteeComment);

				auditService.auditEvent("COMMENT_COPIED",
						committee.getDescription()
								+ " has made a copy of comment "
								+ protocolFormCommitteeComment.getId(),
						newProtocolFormCommitteeComment);

				return XMLResponseHelper.xmlResult(Boolean.TRUE);
			} catch (Exception e) {
				e.printStackTrace();
				return XMLResponseHelper.xmlResult(Boolean.FALSE);
			}

		} else {
			try {
				protocolFormCommitteeComment.setCommittee(committee);
				protocolFormCommitteeComment.setUser(user);
				protocolFormCommitteeComment.setModified(new Date());

				protocolFormCommitteeCommentDao
						.saveOrUpdate(protocolFormCommitteeComment);
				auditService.auditEvent(
						"COMMENT_MOVED",
						"Comment " + protocolFormCommitteeComment.getId()
								+ " has been moved to committee "
								+ committee.getDescription(),
						protocolFormCommitteeComment);

				return XMLResponseHelper.xmlResult(Boolean.TRUE);
			} catch (Exception e) {
				e.printStackTrace();
				return XMLResponseHelper.xmlResult(Boolean.FALSE);
			}

		}

	}

	@Autowired(required = true)
	public void setProtocolFormCommitteeStatusDao(
			ProtocolFormCommitteeStatusDao protocolFormCommitteeStatusDao) {
		this.protocolFormCommitteeStatusDao = protocolFormCommitteeStatusDao;
	}

	public ProtocolFormCommitteeStatusDao getProtocolFormCommitteeStatusDao() {
		return protocolFormCommitteeStatusDao;
	}

	@Autowired(required = true)
	public void setProtocolFormCommitteeCommentDao(
			ProtocolFormCommitteeCommentDao protocolFormCommitteeCommentDao) {
		this.protocolFormCommitteeCommentDao = protocolFormCommitteeCommentDao;
	}

	public ProtocolFormCommitteeCommentDao getProtocolFormCommitteeCommentDao() {
		return protocolFormCommitteeCommentDao;
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

	public ProtocolFormDao getProtocolFormDao() {
		return protocolFormDao;
	}
	
	@Autowired(required = true)
	public void setProtocolFormDao(ProtocolFormDao protocolFormDao) {
		this.protocolFormDao = protocolFormDao;
	}

	public AgendaItemDao getAgendaItemDao() {
		return agendaItemDao;
	}
	
	@Autowired(required = true)
	public void setAgendaItemDao(AgendaItemDao agendaItemDao) {
		this.agendaItemDao = agendaItemDao;
	}

	public UserDao getUserDao() {
		return userDao;
	}
	
	@Autowired(required=true)
	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}

	public AgendaItemTrackService getAgendaItemTrackService() {
		return agendaItemTrackService;
	}
	
	@Autowired(required=true)
	public void setAgendaItemTrackService(AgendaItemTrackService agendaItemTrackService) {
		this.agendaItemTrackService = agendaItemTrackService;
	}
}
