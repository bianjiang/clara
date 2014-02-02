package edu.uams.clara.webapp.protocol.web.protocolform.newsubmission.ajax;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import edu.uams.clara.webapp.protocol.dao.thing.ResearchOrganizationDao;
import edu.uams.clara.webapp.protocol.domain.thing.ResearchOrganization;

@Controller
public class ResearchOrganizationAjaxController {
	
	@SuppressWarnings("unused")
	private final static Logger logger = LoggerFactory
			.getLogger(ResearchOrganizationAjaxController.class);
	
	
	private ResearchOrganizationDao researchOrganizationDao;

	
	@RequestMapping(value = "/ajax/protocols/protocol-forms/new-submission/research-orgnizations/list", method = RequestMethod.GET)
	public @ResponseBody
	Map<String, List<ResearchOrganization>> list() {
		List<ResearchOrganization> researchOrganizationList = researchOrganizationDao.listThings();

		Map<String, List<ResearchOrganization>> researchOrganizations = new HashMap<String, List<ResearchOrganization>>(0);
		researchOrganizations.put("research-organization", researchOrganizationList);
		return researchOrganizations;
	}

	@RequestMapping(value = "/ajax/protocols/protocol-forms/new-submission/research-orgnizations/search", method = RequestMethod.GET)
	public @ResponseBody
	Map<String, List<ResearchOrganization>> searchByName(@RequestParam("name") String name) {

		List<ResearchOrganization> researchOrganizationList = researchOrganizationDao.searchByName(name);

		Map<String, List<ResearchOrganization>> researchOrganizations = new HashMap<String, List<ResearchOrganization>>(0);
		researchOrganizations.put("research-organization", researchOrganizationList);
		return researchOrganizations;
	}

	@Autowired(required=true)
	public void setResearchOrganizationDao(ResearchOrganizationDao researchOrganizationDao) {
		this.researchOrganizationDao = researchOrganizationDao;
	}

	public ResearchOrganizationDao getResearchOrganizationDao() {
		return researchOrganizationDao;
	}

	
}
