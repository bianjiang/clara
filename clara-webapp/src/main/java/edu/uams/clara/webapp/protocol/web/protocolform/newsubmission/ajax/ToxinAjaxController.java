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
import edu.uams.clara.webapp.protocol.dao.thing.ToxinDao;
import edu.uams.clara.webapp.protocol.domain.thing.Toxin;


@Controller
public class ToxinAjaxController {
	
	@SuppressWarnings("unused")
	private final static Logger logger = LoggerFactory
			.getLogger(ToxinAjaxController.class);
	
	private ToxinDao toxinDao;
	
	@RequestMapping(value = "/ajax/protocols/protocol-forms/new-submission/risks/toxins/list", method = RequestMethod.GET)
	public @ResponseBody
	Map<String, List<Toxin>> getToxin() {
		List<Toxin> toxinList = toxinDao.listThings();

		Map<String, List<Toxin>> toxins = new HashMap<String, List<Toxin>>(0);
		toxins.put("toxins", toxinList);
		return toxins;
}
	@RequestMapping(value = "/ajax/protocols/protocol-forms/new-submission/risks/toxins/search", method = RequestMethod.GET)
	public @ResponseBody
	Map<String, List<Toxin>> searchByName(@RequestParam("keyword") String name) {

		List<Toxin> toxinList = toxinDao.searchByName(name);

		Map<String, List<Toxin>> toxins = new HashMap<String, List<Toxin>>(0);
		toxins.put("toxins", toxinList);
		return toxins;
	}

	@Autowired(required=true)
	public void setToxinDao(ToxinDao toxinDao) {
		this.toxinDao = toxinDao;
	}

	public ToxinDao getToxinDao() {
		return toxinDao;
	}
}