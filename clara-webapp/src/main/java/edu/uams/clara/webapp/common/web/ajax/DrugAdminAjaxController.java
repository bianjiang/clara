package edu.uams.clara.webapp.common.web.ajax;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import edu.uams.clara.webapp.protocol.dao.thing.DrugDao;
import edu.uams.clara.webapp.protocol.domain.thing.Drug;

@Controller
public class DrugAdminAjaxController{
	
	private final static Logger logger = LoggerFactory
			.getLogger(DrugAdminAjaxController.class);
	
	private DrugDao drugDao;
	
	@RequestMapping(value = "/ajax/drugs/list", method = RequestMethod.GET)
	public @ResponseBody Map<String,List<Drug>> listUnapprovedDrugs(){
		Map<String, List<Drug>> drugs = new HashMap<String, List<Drug>>(0);
		List<Drug> unapprovedDrugList = drugDao.listUnapprovedThingsByType();
		drugs.put("drugs", unapprovedDrugList);
		return drugs;
	}
	
	@RequestMapping(value = "/ajax/drugs/update", method = RequestMethod.GET)
	
	public DrugDao getDrugDao() {
		return drugDao;
	}
	
	@Autowired(required = true)
	public void setDrugDao(DrugDao drugDao) {
		this.drugDao = drugDao;
	}	
}