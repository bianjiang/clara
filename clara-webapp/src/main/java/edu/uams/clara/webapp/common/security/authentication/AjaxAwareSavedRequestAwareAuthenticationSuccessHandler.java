package edu.uams.clara.webapp.common.security.authentication;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.util.StringUtils;

/**
 * make the AuthenticationSuccessHandler ajax aware... all ajax calls are started with /ajax...
 * so, if it's coming back because of a ajax call and timeout...send the user back to index 
 * @author jbian
 *
 */
public class AjaxAwareSavedRequestAwareAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler  {

    private RequestCache requestCache = new HttpSessionRequestCache();
 
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws ServletException, IOException {
        SavedRequest savedRequest = requestCache.getRequest(request, response);
        
        if (savedRequest == null) {
            super.onAuthenticationSuccess(request, response, authentication);
            return;
        }

        if (isAlwaysUseDefaultTargetUrl() || (request != null && getTargetUrlParameter() != null && StringUtils.hasText(request.getParameter(getTargetUrlParameter())))) {
        	
            requestCache.removeRequest(request, response);
            super.onAuthenticationSuccess(request, response, authentication);

            return;
        }
        
        
        clearAuthenticationAttributes(request);
        

        // Use the DefaultSavedRequest URL
        String targetUrl = savedRequest.getRedirectUrl();

        if (targetUrl.indexOf("/ajax") != -1) {
        	logger.debug("saved url is a ajax call, going to / instead of going to DefaultSavedRequest Url: " + targetUrl);
        	getRedirectStrategy().sendRedirect(request, response, "/index");
        }else{
        	//hack to get redirect correct, otherwise, it will redirect to "http://localhost:8080/clara-webapp/;jsessionid=B0A52A1F5D6E51F9412BFCBCBD3FFA75" which cause an exception in finding the mapped http url
        	if(targetUrl.indexOf("/clara-webapp/;jsessionid=") != -1) {
        		targetUrl = "/index";        		
        	}
        	//logger.warn(targetUrl);
        	logger.debug("Redirecting to DefaultSavedRequest Url: " + targetUrl);
        	getRedirectStrategy().sendRedirect(request, response, targetUrl);
        }
    }

    public void setRequestCache(RequestCache requestCache) {
    	super.setRequestCache(requestCache);
        this.requestCache = requestCache;
    }

}
