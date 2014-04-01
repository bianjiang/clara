package edu.uams.clara.webapp.protocol.web.protocolform.review;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.google.common.collect.Lists;

import edu.uams.clara.webapp.common.domain.usercontext.User;
import edu.uams.clara.webapp.common.domain.usercontext.enums.Committee;
import edu.uams.clara.webapp.common.domain.usercontext.enums.Permission;
import edu.uams.clara.webapp.protocol.dao.ProtocolDao;
import edu.uams.clara.webapp.protocol.dao.businesslogicobject.ProtocolFormStatusDao;
import edu.uams.clara.webapp.protocol.dao.protocolform.ProtocolFormDao;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.ProtocolFormStatus;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.enums.ProtocolFormStatusEnum;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolForm;

@Controller
public class ProtocolFormReviewController {

	private ProtocolFormDao protocolFormDao;

	private ProtocolDao protocolDao;

	private ProtocolFormStatusDao protocolFormStatusDao;
	
	private List<ProtocolFormStatusEnum> cannotEditProtocolFormStatusList = Lists.newArrayList();{
		cannotEditProtocolFormStatusList.add(ProtocolFormStatusEnum.PENDING_PI_SIGN_OFF);
		cannotEditProtocolFormStatusList.add(ProtocolFormStatusEnum.IRB_APPROVED);
		cannotEditProtocolFormStatusList.add(ProtocolFormStatusEnum.IRB_ACKNOWLEDGED);
		cannotEditProtocolFormStatusList.add(ProtocolFormStatusEnum.EXPEDITED_APPROVED);
		cannotEditProtocolFormStatusList.add(ProtocolFormStatusEnum.EXEMPT_APPROVED);
	}

	@RequestMapping(value = "/protocols/{protocolId}/protocol-forms/{protocolFormId}/review", method = RequestMethod.GET)
	public String getProtocolFormReviewPage(
			@PathVariable("protocolId") long protocolId,
			@PathVariable("protocolFormId") long protocolFormId,
			@RequestParam(value="fromQueue", required=false) String fromQueue,
			@RequestParam("committee") Committee committee, ModelMap modelMap) {
		User currentUser = (User) SecurityContextHolder.getContext()
				.getAuthentication().getPrincipal();

		ProtocolForm protocolForm = protocolFormDao.findById(protocolFormId);
		
		ProtocolFormStatus latestProtocolFormStatus = protocolFormStatusDao.getLatestProtocolFormStatusByFormId(protocolFormId);
		
		boolean readOnly = false;
		boolean canAddComments = false;
		boolean canAddContingencies = false;
		
		if (cannotEditProtocolFormStatusList.contains(latestProtocolFormStatus.getProtocolFormStatus())){
			readOnly = true;
			canAddComments = false;
			canAddContingencies = false;
		} else {
			if (currentUser.getAuthorities().contains(Permission.COMMENT_CAN_ADD)) {
				canAddComments = true;
			}
			
			if (currentUser.getAuthorities().contains(Permission.CONTINGENCY_CAN_ADD)) {
				canAddContingencies = true;
			}
		}

		modelMap.put("protocolForm", protocolForm);
		modelMap.put("formId", protocolFormId);
		modelMap.put("id", protocolId);
		modelMap.put("fromQueue",fromQueue);
		modelMap.put("committee", committee);
		modelMap.put("readOnly", readOnly);
		modelMap.put("user", currentUser);
		modelMap.put("canAddComments", canAddComments);
		modelMap.put("canAddContingencies", canAddContingencies);

		return "protocol/protocolform/review";
	}

	@RequestMapping(value = "/protocols/{protocolId}/protocol-forms/{protocolFormId}/review/complete", method = RequestMethod.GET)
	public String getCompleteReviewPage(
			@PathVariable("protocolId") long protocolId,
			@PathVariable("protocolFormId") long protocolFormId,
			@RequestParam(value="fromQueue", required=false) String fromQueue,
			@RequestParam("committee") Committee committee,ModelMap modelMap) {

		ProtocolForm protocolForm = protocolFormDao.findById(protocolFormId);

		String url = "redirect:/protocols/" + protocolId + "/protocol-forms/"
				+ protocolFormId + "/review/";
		
		url += protocolForm.getProtocolFormType().getUrlEncoded() + "/committee-review";

		modelMap.put("fromQueue", fromQueue);
		modelMap.put("committee", committee);
		// modelMap.put("user",
		// (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal());

		return url;

	}

	@Autowired(required = true)
	public void setProtocolDao(ProtocolDao protocolDao) {
		this.protocolDao = protocolDao;
	}

	public ProtocolDao getProtocolDao() {
		return protocolDao;
	}

	@Autowired(required = true)
	public void setProtocolFormDao(ProtocolFormDao protocolFormDao) {
		this.protocolFormDao = protocolFormDao;
	}

	public ProtocolFormDao getProtocolFormDao() {
		return protocolFormDao;
	}

	@Autowired(required = true)
	public void setProtocolFormStatusDao(
			ProtocolFormStatusDao protocolFormStatusDao) {
		this.protocolFormStatusDao = protocolFormStatusDao;
	}

	public ProtocolFormStatusDao getProtocolFormStatusDao() {
		return protocolFormStatusDao;
	}

}
