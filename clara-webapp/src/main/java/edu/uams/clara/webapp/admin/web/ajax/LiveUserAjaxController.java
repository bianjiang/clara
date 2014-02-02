package edu.uams.clara.webapp.admin.web.ajax;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import edu.uams.clara.webapp.common.domain.usercontext.User;
import edu.uams.clara.webapp.common.util.JsonResponseHelper;
import edu.uams.clara.webapp.common.util.response.JsonResponse;


@Controller
public class LiveUserAjaxController {
	
	private final static Logger logger = LoggerFactory
			.getLogger(LiveUserAjaxController.class);
	
	private SessionRegistry sessionRegistry;
	
	private final static SimpleDateFormat df = new SimpleDateFormat("yyyy.MM.dd 'at' HH:mm:ss z");
	
	@RequestMapping(value = "/ajax/admin/super/live-users/list", method = RequestMethod.POST, produces="application/json")
	public @ResponseBody JsonResponse getLiveUsers(){
		List<User> liveUsers = new ArrayList<User>();
		for(Object o:sessionRegistry.getAllPrincipals()){
			List<SessionInformation> sessions = sessionRegistry.getAllSessions(o, false);
			User u = (User)o;
			List<Map<String, String>> ss = new ArrayList<Map<String, String>>();
			for(SessionInformation session:sessions){
				Map<String, String> s = new HashMap<String, String>();
				s.put("sessionId", session.getSessionId());
				s.put("lastRequest", df.format(session.getLastRequest()));
				s.put("isExpired", "" + session.isExpired());
				ss.add(s);
			}
			u.setData(ss);
			liveUsers.add((User)o);
		}
		
		return JsonResponseHelper.newDataResponseStub(liveUsers);
	}
	
	@RequestMapping(value = "/ajax/admin/super/live-users/{userId}/kill", method = RequestMethod.POST)
	public @ResponseBody JsonResponse killLiveUserSession(@PathVariable("userId") long userId){
		
		long id = 0;
		for(Object o:sessionRegistry.getAllPrincipals()){
			User c = (User)o;
			if(c.getId() == userId){
				List<SessionInformation> sessions = sessionRegistry.getAllSessions(o, false);
				for(SessionInformation session:sessions){
					
					//logger.info(session.getSessionId(), session.getLastRequest());
					session.expireNow();//kill the session
				}
				logger.debug("killed sessions for user: ", c.getUsername());
			}
		}
		JsonResponse jsonResponse = new JsonResponse(false, "finished...", null, false);
		return jsonResponse;

	}
	
	public SessionRegistry getSessionRegistry() {
		return sessionRegistry;
	}

	@Autowired(required=true)
	public void setSessionRegistry(SessionRegistry sessionRegistry) {
		this.sessionRegistry = sessionRegistry;
	}
}
