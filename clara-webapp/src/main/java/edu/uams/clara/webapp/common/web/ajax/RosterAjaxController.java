package edu.uams.clara.webapp.common.web.ajax;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;

import edu.uams.clara.webapp.common.dao.usercontext.UserDao;
import edu.uams.clara.webapp.common.domain.usercontext.User;
import edu.uams.clara.webapp.protocol.dao.irb.IRBReviewerDao;
import edu.uams.clara.webapp.protocol.domain.irb.IRBReviewer;
import edu.uams.clara.webapp.protocol.domain.irb.IRBReviewer.IRBReviewerType;
import edu.uams.clara.webapp.protocol.domain.irb.enums.IRBRoster;

@Controller
public class RosterAjaxController {

	private final static Logger logger = LoggerFactory
			.getLogger(RosterAjaxController.class);

	private IRBReviewerDao irbReviewerDao;
	
	
	public UserDao getUserDao() {
		return userDao;
	}

	@Autowired(required=true)
	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}

	public IRBReviewerDao getIrbReviewerDao() {
		return irbReviewerDao;
	}

	@Autowired(required=true)
	public void setIrbReviewerDao(IRBReviewerDao irbReviewerDao) {
		this.irbReviewerDao = irbReviewerDao;
	}

	private UserDao userDao;

	@RequestMapping(value = "/ajax/rosters/list", method = RequestMethod.GET)
	public @ResponseBody
	List<IRBReviewer> getIRBReviewers() {
		List<IRBReviewer> irbReviewers = irbReviewerDao.listAllIRBReviewers();
//		logger.info("#" + irbReviewers.size());
//		ObjectMapper objectMapper = new ObjectMapper();
//		try {
//			logger.debug(objectMapper.writeValueAsString(irbReviewers));
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			logger.info(e.getMessage());
//		}
		//return irbReviewerDao.listAllIRBReviewers();
		return irbReviewers;
	}

	/**
	 * not really deleting, but rather set the retired to true
	 * @return
	 */
	@RequestMapping(value = "/ajax/rosters/reviewers/delete", method = RequestMethod.GET)
	public @ResponseBody IRBReviewer deleteUserRole(@RequestParam("reviewerId") long reviewerId) {
		IRBReviewer reviewer = null;
		try {
			reviewer = irbReviewerDao.findById(reviewerId);
			reviewer.setRetired(Boolean.TRUE);
			
			reviewer = irbReviewerDao.saveOrUpdate(reviewer);
		} catch (Exception ex) {
			logger.warn("IRB reviewer cannot be removed; reviewerId: " + reviewerId);
			
		}
		return reviewer;
	}

	@RequestMapping(value = "/ajax/rosters/reviewers/update", method = RequestMethod.POST)
	public @ResponseBody IRBReviewer updateIRBReviewer(@RequestBody IRBReviewer reviewer) {
		logger.debug("reviewer: "+reviewer.toString());
		IRBReviewer newIRBReviewer = irbReviewerDao.findById(reviewer.getId());
		
		logger.debug(newIRBReviewer.toString()+" : Week - "+newIRBReviewer.getIrbRoster());
		
		newIRBReviewer.setIrbRoster(reviewer.getIrbRoster());
		newIRBReviewer.setType(reviewer.getType());
		newIRBReviewer.setRetired(reviewer.isRetired());

		newIRBReviewer.setAffiliated(reviewer.isAffiliated());
		newIRBReviewer.setAlternativeMember(reviewer.isAlternativeMember());
		newIRBReviewer.setComment(reviewer.getComment());
		newIRBReviewer.setDegree(reviewer.getDegree());
		
		newIRBReviewer = irbReviewerDao.saveOrUpdate(newIRBReviewer);
		return newIRBReviewer;
				
	}
	
	
	@RequestMapping(value = "/ajax/rosters/reviewers/create", method = RequestMethod.POST)
	public @ResponseBody
	IRBReviewer createReviewer(@RequestParam("userId") long userId,
			@RequestParam("week") IRBRoster roster,
			@RequestParam("type") IRBReviewerType reviewertype,
			@RequestParam(value="specialty", required = false) String specialty,
			@RequestParam(value="degree", required = false) String degree,
			@RequestParam(value="alternativeMember", required = false) Boolean alternativeMember,
			@RequestParam(value="affiliated", required = false) Boolean affiliated,
			@RequestParam(value="comment", required = false) String comment) {

		
		IRBReviewer reviewer = new IRBReviewer();
		User user = userDao.findById(userId);
		
		reviewer.setUser(user);
		reviewer.setIrbRoster(roster);
		reviewer.setType(reviewertype);
		
		if (specialty != null){
			reviewer.setSpecialty(specialty);
		}
		if (degree != null){
			reviewer.setDegree(degree);
		}
		if (alternativeMember != null){
			reviewer.setAlternativeMember(alternativeMember);
		}
		if (affiliated != null){
			reviewer.setAffiliated(affiliated);
		}
		if (comment != null){
			reviewer.setComment(comment);
		}

		return irbReviewerDao.saveOrUpdate(reviewer);

	}

}
