package edu.uams.clara.webapp.common.security.filter.ajax;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.uams.clara.core.util.web.HttpServletUtils;
import edu.uams.clara.webapp.common.util.response.JsonResponse;

public class AjaxSecurityFilter extends OncePerRequestFilter {

	private final static Logger logger = LoggerFactory
			.getLogger(AjaxSecurityFilter.class);
	
	private ObjectMapper objectMapper = new ObjectMapper();

	protected void doFilterInternal(HttpServletRequest request,
			HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		if (!HttpServletUtils.isAjaxRequest(request)) {
			filterChain.doFilter(request, response);
			return;
		}

		logger.debug("AjaxSecurityFilter: Processing an AJAX call : "
				+ request.getRequestURL());

		RedirectResponseWrapper redirectResponseWrapper = new RedirectResponseWrapper(
				response);

		filterChain.doFilter(request, redirectResponseWrapper);

		if (redirectResponseWrapper.getRedirect() != null) {
			request.setCharacterEncoding("UTF-8");
			response.setContentType("application/json;charset=utf-8");

			String redirectURL = redirectResponseWrapper.getRedirect();

			//Map<String, Object> jsonObj = new HashMap<String, Object>();
			JsonResponse jsonResponse = null;
			
			//HttpSession httpSession = request.getSession();

			if (redirectURL.indexOf("login") != -1) {
				// populate your reply in this case the json object
				// with what ever information needed to pop up your login window
				
				jsonResponse = new JsonResponse(true,
						"Your session has expired. Please login again.", "/", true);
				/*
				jsonObj.put("error", true);
				jsonObj.put("message",
						"Your session expired! Please login again!");
				*/
				if (redirectURL.indexOf("login_error=1") != -1) {
					// populate the json object with the failure message like
					// Bad credentials , etc
				}
			}// / your auth is successful the call is successful
			else {
				// you can return the user name and password in the reply so it
				// can be displayed for example in you app

				//SecurityContext ctx = SecurityContextHolder.getContext();
				//if (ctx != null) {
					//Authentication auth = ctx.getAuthentication();
					// here you can retreive all your user credential and send
					// them back in the json object
				//}
			}			

			try {
				response.getOutputStream().print(
						objectMapper.writeValueAsString(jsonResponse));
			} catch (Exception e) {
				
			}
		}
	}

	

}