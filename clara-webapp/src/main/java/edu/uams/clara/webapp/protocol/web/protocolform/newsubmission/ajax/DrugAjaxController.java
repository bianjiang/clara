package edu.uams.clara.webapp.protocol.web.protocolform.newsubmission.ajax;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import edu.uams.clara.webapp.protocol.dao.thing.DrugDao;
import edu.uams.clara.webapp.protocol.domain.thing.Drug;

@Controller
public class DrugAjaxController {
	private static final int PAGE_SIZE = 20;
	
	@SuppressWarnings("unused")
	private final static Logger logger = LoggerFactory
			.getLogger(DrugAjaxController.class);
	
	private DrugDao drugDao;
	/**
	 * return json
	 * @return
	 */
	@RequestMapping(value = "/ajax/protocols/protocol-forms/new-submission/drugs/list", method = RequestMethod.GET)
	public @ResponseBody
	Map<String, List<Drug>> getDrugs() {

		List<Drug> drugList = drugDao.listThings();

		Map<String, List<Drug>> drugs = new HashMap<String, List<Drug>>(0);
		drugs.put("drugs", drugList);
		return drugs;
	}

	@RequestMapping(value = "/ajax/protocols/protocol-forms/new-submission/drugs/search", method = RequestMethod.GET)
	@Transactional(readOnly = true)
	public @ResponseBody
	Map<String, List<Drug>> searchByName(@RequestParam("name") String name,
			@RequestParam("start") Integer start,
			@RequestParam(value="limit", required=false) Integer limit) {
		int l = limit == null ? PAGE_SIZE : limit;
		
		List<Drug> drugList = drugDao.searchByName(name);

		Map<String, List<Drug>> drugs = new HashMap<String, List<Drug>>(0);
		drugs.put("drugs", drugList);
		return drugs;
	}

	@Autowired(required=true)
	public void setDrugDao(DrugDao drugDao) {
		this.drugDao = drugDao;
	}

	public DrugDao getDrugDao() {
		return drugDao;
	}

	
}
