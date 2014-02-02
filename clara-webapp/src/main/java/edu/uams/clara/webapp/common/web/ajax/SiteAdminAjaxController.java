package edu.uams.clara.webapp.common.web.ajax;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import edu.uams.clara.webapp.common.util.XMLResponseHelper;
import edu.uams.clara.webapp.common.util.response.JsonResponse;
import edu.uams.clara.webapp.protocol.dao.thing.SiteDao;
import edu.uams.clara.webapp.protocol.domain.thing.Drug;
import edu.uams.clara.webapp.protocol.domain.thing.Site;

@Controller
public class SiteAdminAjaxController{
	
	private final static Logger logger = LoggerFactory
			.getLogger(SiteAdminAjaxController.class);

	private SiteDao siteDao;
	
	@RequestMapping(value = "/ajax/sites/list", method = RequestMethod.GET)
	public @ResponseBody Map<String, List<Site>> listUnapprovedSites(){
		Map<String, List<Site>> sites = new HashMap<String, List<Site>>(0);
		List<Site> unapprovedSiteList = siteDao.listUnapprovedSites();
		sites.put("sites", unapprovedSiteList);
		return sites;
	}
	
	@RequestMapping(value = "/ajax/sites/approve", method = RequestMethod.POST)
	public @ResponseBody String approveSites(@RequestParam(value = "siteid", required = true) long siteId){
		Assert.notNull(siteId);
		
		try{
			Site s = siteDao.findById(siteId);
			s.setApproved(true);
			siteDao.saveOrUpdate(s);
			
			return XMLResponseHelper.xmlResult(Boolean.TRUE);
		} catch (Exception ex) {
			ex.printStackTrace();
			return XMLResponseHelper.xmlResult(Boolean.FALSE);
		}
		
	}

	public SiteDao getSiteDao() {
		return siteDao;
	}
	
	@Autowired(required=true)
	public void setSiteDao(SiteDao siteDao) {
		this.siteDao = siteDao;
	}
}