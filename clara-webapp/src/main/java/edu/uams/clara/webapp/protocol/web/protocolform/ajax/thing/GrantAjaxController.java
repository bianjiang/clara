package edu.uams.clara.webapp.protocol.web.protocolform.ajax.thing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import edu.uams.clara.webapp.common.util.JsonResponseHelper;
import edu.uams.clara.webapp.common.util.response.JsonResponse;
import edu.uams.clara.webapp.protocol.dao.thing.GrantDao;
import edu.uams.clara.webapp.protocol.domain.thing.Grant;

@Controller
public class GrantAjaxController {
	private final static Logger logger = LoggerFactory
	.getLogger(GrantAjaxController.class);
	
	private GrantDao grantDao;
	
	@RequestMapping(value = "/ajax/protocols/protocol-forms/grant/{prn}", method = RequestMethod.GET, produces="application/json")
	public @ResponseBody JsonResponse findAwardedGrantByPrn(@PathVariable("prn") String prn){
		
		Grant grant = null;
		try{
			grant = grantDao.findAwardedGrantByPRN(prn);
			return JsonResponseHelper.newDataResponseStub(grant);
		} catch (Exception e){
			
			return JsonResponseHelper.newErrorResponseStub("No Awarded Grant Found!");
		}
		
	}

	public GrantDao getGrantDao() {
		return grantDao;
	}

	@Autowired(required = true)
	public void setGrantDao(GrantDao grantDao) {
		this.grantDao = grantDao;
	}
	

}
