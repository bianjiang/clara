package edu.uams.clara.webapp.common.security.authentication;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.uams.clara.core.util.web.HttpServletUtils;
import edu.uams.clara.webapp.common.util.JsonResponseHelper;
import edu.uams.clara.webapp.common.util.response.JsonResponse;

/**
 * authenticationProcessingFilterEntryPoint to redirect the user to the login
 * page when trying to access a secured ressource this authentication entry
 * point is ajax aware... it checks the request header to check whether this is
 * a ajax call or not, if it is.. then
 * 
 * @author h0cked
 * 
 */
public class AjaxAwareAuthenticationEntryPoint extends
		LoginUrlAuthenticationEntryPoint {

	private String hostUrl;

	private ObjectMapper objectMapper = new ObjectMapper();

	private static final Logger logger = LoggerFactory
			.getLogger(AjaxAwareAuthenticationEntryPoint.class);

	public AjaxAwareAuthenticationEntryPoint(String loginFormUrl) {
		super(loginFormUrl);
	}

	@Override
	public void commence(HttpServletRequest request,
			HttpServletResponse response, AuthenticationException authException)
			throws IOException, ServletException {
		// if this is an ajax call...
		if (HttpServletUtils.isAjaxRequest(request)) {
			logger.debug("this is a ajax call..., but the user session expired!");
			
			//Map<String, Object> jsonObj = new HashMap<String, Object>();
			//jsonObj.put("error", true);
			//jsonObj.put("message", "Your session expired! Please login again!");

			String redirectUrl = (getHostUrl() != null ? getHostUrl() : "")
					+ request.getContextPath() + getLoginFormUrl();
			//jsonObj.put("redirect", redirectUrl);
			
			JsonResponse errorResponse= JsonResponseHelper.newJsonResponseStub(true, "Your session has expired. Please login again.", redirectUrl, true, null);

			response.setCharacterEncoding("UTF-8");
			response.setContentType("application/json;charset=utf-8");

			response.getOutputStream().print(
					objectMapper.writeValueAsString(errorResponse));

			// response.sendError(601, "");
		} else {
			super.commence(request, response, authException);
		}
	}

	public void setHostUrl(String hostUrl) {
		this.hostUrl = hostUrl;
	}

	public String getHostUrl() {
		return hostUrl;
	}
}
