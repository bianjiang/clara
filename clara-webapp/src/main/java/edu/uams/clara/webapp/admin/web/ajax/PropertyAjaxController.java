package edu.uams.clara.webapp.admin.web.ajax;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import edu.uams.clara.core.dao.thing.ThingDao;
import edu.uams.clara.core.domain.thing.Thing;
import edu.uams.clara.webapp.common.util.JsonResponseHelper;
import edu.uams.clara.webapp.common.util.response.JsonResponse;
import edu.uams.clara.webapp.protocol.domain.thing.Site;


@Controller
public class PropertyAjaxController {
	private ThingDao thingDao;
	private final static Logger logger = LoggerFactory
			.getLogger(PropertyAjaxController.class);

	@RequestMapping(value = "/ajax/admin/things/search", method = RequestMethod.GET, produces="application/json")
	public @ResponseBody
	JsonResponse searchThingsByKeywordAndType(
			@RequestParam(value = "keyword", required = true) String keyword,
			@RequestParam(value = "type", required = true) String type) {
	
		try {
			List<Thing> things = thingDao.searchByKeywordsAndType(keyword, type);
			return new JsonResponse(false, things);
		} catch (Exception e) {
			e.printStackTrace();

			return new JsonResponse(true, "Failed to find...", "", false,
					null);
		}
		
	}
	
	@RequestMapping(value = "/ajax/admin/things/update", method = RequestMethod.POST)
	public @ResponseBody Thing updateThing(@RequestBody Thing thing){
		Thing newThing = thingDao.findById(thing.getId());
		
		newThing.setApproved(thing.isApproved());
		newThing.setDescription(thing.getDescription());
		newThing.setRetired(thing.isRetired());
		newThing.setType(thing.getType());
		newThing.setValue(thing.getValue());
		
		newThing = thingDao.saveOrUpdate(newThing);
		return newThing;
				
	}
	
	@RequestMapping(value = "/ajax/admin/things/create", method = RequestMethod.POST)
	public @ResponseBody Thing createThing(@RequestBody Thing thing){
		Thing newThing = new Thing();
		
		logger.debug("newthing.. type is "+thing.getType());
		
		newThing.setApproved(thing.isApproved());
		newThing.setDescription(thing.getDescription());
		newThing.setRetired(Boolean.FALSE);
		newThing.setType(thing.getType());
		newThing.setValue(thing.getValue());
		
		newThing = thingDao.saveOrUpdate(newThing);
		
		return newThing;
				
	}
	
	@RequestMapping(value = "/ajax/admin/things/delete", method = RequestMethod.POST)
	public @ResponseBody JsonResponse deleteThing(@RequestBody Thing thing){
		Thing toDeleteThing = thingDao.findById(thing.getId());
		
		try {
			toDeleteThing.setRetired(Boolean.TRUE);
			
			toDeleteThing = thingDao.saveOrUpdate(toDeleteThing);
		} catch (Exception e) {
			return JsonResponseHelper.newErrorResponseStub("Failed to delete!");
		}

		return JsonResponseHelper.newSuccessResponseStube("Successfully deleted!");
	}

	
	public ThingDao getThingDao() {
		return thingDao;
	}

	@Autowired(required = true)
	public void setThingDao(ThingDao thingDao) {
		this.thingDao = thingDao;
	}
}
