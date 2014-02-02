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

import edu.uams.clara.webapp.protocol.dao.thing.SponsorDao;
import edu.uams.clara.webapp.protocol.domain.thing.Sponsor;

@Controller
public class SponsorAjaxController {
	
	@SuppressWarnings("unused")
	private final static Logger logger = LoggerFactory
			.getLogger(SponsorAjaxController.class);
	
	
	private SponsorDao sponsorDao;

	
	@RequestMapping(value = "/ajax/protocols/protocol-forms/new-submission/sponsors/list", method = RequestMethod.GET)
	public @ResponseBody
	Map<String, List<Sponsor>> list() {
		List<Sponsor> sponsorList = sponsorDao.listThings();

		Map<String, List<Sponsor>> sponsors = new HashMap<String, List<Sponsor>>(0);
		sponsors.put("sponsors", sponsorList);
		return sponsors;
	}

	@RequestMapping(value = "/ajax/protocols/protocol-forms/new-submission/sponsors/search", method = RequestMethod.GET)
	public @ResponseBody
	Map<String, List<Sponsor>> searchByName(@RequestParam("query") String name) {

		List<Sponsor> sponsorList = sponsorDao.searchByName(name);

		Map<String, List<Sponsor>> sponsors = new HashMap<String, List<Sponsor>>(0);
		sponsors.put("sponsors", sponsorList);
		return sponsors;
	}

	@Autowired(required=true)
	public void setSponsorDao(SponsorDao sponsorDao) {
		this.sponsorDao = sponsorDao;
	}

	public SponsorDao getSponsorDao() {
		return sponsorDao;
	}

	
}
