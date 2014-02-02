package edu.uams.clara.webapp.user.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import edu.uams.clara.webapp.common.dao.usercontext.UserDao;
import edu.uams.clara.webapp.common.domain.usercontext.User;

@Controller
public class UserProfileController {
	
	private final static Logger logger = LoggerFactory
	.getLogger(UserProfileController.class);
	
	
	private UserDao userDao;
	
	@RequestMapping(value = "/user/{userId}/profile")
	public String getUserProfile(@PathVariable("userId") long userId, ModelMap modelMap){
		
		User u = null;
		try{
			u = userDao.findById(userId);
		} catch (Exception ex){
			ex.printStackTrace();
		}
		
		modelMap.put("user", (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal());
		modelMap.put("profileuser",u);
		return "user/profile";
	}
	
	@RequestMapping(value = "/user/profile")
	public String getUserProfile(ModelMap modelMap){
		
		User u = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		
		modelMap.put("user", u);
		modelMap.put("profileuser",u);
		return "user/profile";
	}

	public UserDao getUserDao() {
		return userDao;
	}

	@Autowired(required=true)
	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}
	
	

}
