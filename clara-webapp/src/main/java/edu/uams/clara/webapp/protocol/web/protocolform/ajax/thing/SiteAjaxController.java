package edu.uams.clara.webapp.protocol.web.protocolform.ajax.thing;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import edu.uams.clara.webapp.protocol.dao.thing.SiteDao;
import edu.uams.clara.webapp.protocol.domain.thing.Site;


@Controller
public class SiteAjaxController {

	@SuppressWarnings("unused")
	private final static Logger logger = LoggerFactory
	.getLogger(SiteAjaxController.class);
	
	private SiteDao siteDao;
		
	@RequestMapping(value = "/ajax/protocols/protocol-forms/sites/search", method = RequestMethod.GET)
	public @ResponseBody Map<String, List<Site>> searchByKeyword(@RequestParam(value="keyword", required=false) String keyword, @RequestParam(value="common", required=false) Boolean common){
		
		logger.debug("keyword: " + keyword + "; common: " + common);
		
		List<Site> siteList = siteDao.searchByKeywordAndCommon(keyword, common);
		
		logger.debug("find: " + siteList.size() + " site(s)");
				
		Map<String, List<Site>> sites = new HashMap<String, List<Site>>(0);
		
		sites.put("sites", siteList);
		return sites;
				
	}
	
	
	@RequestMapping(value = "/ajax/protocols/protocol-forms/sites/save", method = RequestMethod.POST)
	public @ResponseBody Site saveSite(@RequestBody Site site){
		
		site = siteDao.saveOrUpdate(site);
		
		return site;
				
	}

	@Autowired(required=true)
	public void setSiteDao(SiteDao siteDao) {
		this.siteDao = siteDao;
	}

	public SiteDao getSiteDao() {
		return siteDao;
	}
}
