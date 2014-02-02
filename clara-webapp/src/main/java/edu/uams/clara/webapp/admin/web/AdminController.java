package edu.uams.clara.webapp.admin.web;

import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.annotation.AnnotationMethodHandlerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import edu.uams.clara.webapp.common.domain.usercontext.User;

@Controller
public class AdminController {
	
	private final static Logger logger = LoggerFactory
	.getLogger(AdminController.class);
	
	@Autowired(required=true)
	private ApplicationContext applicationContext;
	
	private SessionRegistry sessionRegistry;
	
	@RequestMapping(value="/admin")
	public String getAdminDashboard(ModelMap modelMap){
		modelMap.put("user", (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal());
		return "admin/index";
	}
	
	
	@RequestMapping(value="/super")
	public String getSecretAdmin(ModelMap modelMap){
		
		logger.debug("beans:" + applicationContext.getBeanDefinitionCount());
		for(Entry<String, RequestMappingHandlerAdapter> entry:applicationContext.getBeansOfType(RequestMappingHandlerAdapter.class).entrySet()){
			logger.debug(entry.getKey() + ":" + entry.getValue().getOrder() + "; " + entry.getValue().getMessageConverters().size());
			for(HttpMessageConverter converter: entry.getValue().getMessageConverters()){
				logger.debug(converter.getClass().getSimpleName() + ":" + converter.getSupportedMediaTypes().size());
				for(Object m:converter.getSupportedMediaTypes()){
					MediaType mm = (MediaType)m;
					logger.debug("mm: " + mm.getType());
				}
			}
		}
			/*
		Collection<? extends GrantedAuthority> authorities = SecurityContextHolder.getContext().getAuthentication().getAuthorities();
		for(GrantedAuthority a:authorities){
			if (a instanceof SwitchUserGrantedAuthority) {
				
			}
		}
		*/
		User user = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		
		modelMap.put("user", user);
		return "admin/secret";
	}

	public ApplicationContext getApplicationContext() {
		return applicationContext;
	}

	public void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	public SessionRegistry getSessionRegistry() {
		return sessionRegistry;
	}

	public void setSessionRegistry(SessionRegistry sessionRegistry) {
		this.sessionRegistry = sessionRegistry;
	}

	
	
	

}
