package edu.uams.clara.webapp.protocol.web.protocolform.ajax.thing;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import edu.uams.clara.core.dao.thing.ThingDao;
import edu.uams.clara.core.domain.thing.Thing;
import edu.uams.clara.webapp.common.util.response.JsonResponse;

@Controller
public class ICD9AjaxController {
	private final static Logger logger = LoggerFactory
			.getLogger(ICD9AjaxController.class);

	private ThingDao thingDao;
	
	private List<String> types = new ArrayList<String>();
	{
		types.add("ICD_9_PROC");
		types.add("ICD_9_DIAG");
	}
	@RequestMapping(value = "/ajax/protocols/protocol-forms/icd9/search", method = RequestMethod.GET)
	public @ResponseBody
	JsonResponse searchByKeyword(
			@RequestParam(value = "keyword", required = true) String keyword) {
		
		
		try {
			List<Thing> icd9Codes = thingDao.searchByKeywordsAndTypes(keyword, types);
			return new JsonResponse(false, icd9Codes);
		} catch (Exception e) {
			e.printStackTrace();

			return new JsonResponse(true, "Failed to find...", "", false,
					null);
		}

	}


	public ThingDao getThingDao() {
		return thingDao;
	}

	@Autowired(required = true)
	public void setThingDao(ThingDao thingDao) {
		this.thingDao = thingDao;
	}

}
