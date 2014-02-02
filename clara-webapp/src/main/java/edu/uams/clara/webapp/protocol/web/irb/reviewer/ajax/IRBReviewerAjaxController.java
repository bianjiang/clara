package edu.uams.clara.webapp.protocol.web.irb.reviewer.ajax;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import edu.uams.clara.webapp.protocol.dao.irb.IRBReviewerDao;
import edu.uams.clara.webapp.protocol.domain.irb.IRBReviewer;

@Controller
public class IRBReviewerAjaxController {

	private IRBReviewerDao irbReviewerDao;
	
	@RequestMapping(value = "/ajax/irb-reviewers/list-expedited", method = RequestMethod.GET)
	public @ResponseBody
	List<IRBReviewer> listExpeditedIRBReviewers() {
		
		List<IRBReviewer> irbReviewers = irbReviewerDao.listExpeditedIRBReviewers();
		
		return irbReviewers;
	}

	@Autowired(required=true)
	public void setIrbReviewerDao(IRBReviewerDao irbReviewerDao) {
		this.irbReviewerDao = irbReviewerDao;
	}

	public IRBReviewerDao getIrbReviewerDao() {
		return irbReviewerDao;
	}
}
