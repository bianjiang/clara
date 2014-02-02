package edu.uams.clara.core.springmvc.extension;

import java.lang.annotation.Annotation;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.MethodParameter;
import org.springframework.web.HttpSessionRequiredException;
import org.springframework.web.bind.support.WebArgumentResolver;
import org.springframework.web.context.request.NativeWebRequest;

public class SessionParamArgumentResolver implements WebArgumentResolver {

	private final static Logger logger = LoggerFactory
			.getLogger(SessionParamArgumentResolver.class);

	public Object resolveArgument(MethodParameter methodParam,
			NativeWebRequest webRequest) throws Exception {

		Annotation[] paramAnns = methodParam.getParameterAnnotations();
		Class<?> paramType = methodParam.getParameterType();

		for (Annotation paramAnn : paramAnns) {
			if (SessionParam.class.isInstance(paramAnn)) {
				SessionParam sessionParam = (SessionParam) paramAnn;
				
				logger.debug("paramType: " + paramType.toString());

				String paramName = sessionParam.value();

				boolean required = sessionParam.required();
				
				logger.debug("required:" + required);
				
				HttpServletRequest servletRequest = webRequest.getNativeRequest(HttpServletRequest.class);

				HttpSession session = servletRequest.getSession(false);
				
				//logger.debug("getting object from protocol..." + session.getId());
				Object result = null;
				if (session != null) {
					result = session.getAttribute(paramName);
				
					logger.debug("getting [" + paramName + "]; sessionId: " + session.getId());
				}
				
				if (result == null && required && session == null)
					raiseSessionRequiredException(paramName, paramType);
				if (result == null && required)
					raiseMissingParameterException(paramName, paramType);
				
				if(result == null && isNumericType(paramType)){
					logger.debug("result is null, convert to 0");
					result = 0;
				}
				return result;
			}
		}

		return WebArgumentResolver.UNRESOLVED;

	}

	/**
     * Indicates if a given class type is a primitive numeric one type
     * (one of byte, short, int, long, float, or double).
     * @param type the type to check
     * @return true if it is a primitive numeric type, false otherwise
     */
	private boolean isNumericType(Class<?> paramType){
		 return ( paramType == byte.class   || paramType == short.class ||
				 paramType == int.class    || paramType == long.class  || 
				 paramType == double.class || paramType == float.class );
	}

	protected void raiseMissingParameterException(String paramName,
			Class<?> paramType) throws Exception {
		throw new IllegalStateException("Missing parameter '" + paramName
				+ "' of type [" + paramType.getName() + "]");
	}

	protected void raiseSessionRequiredException(String paramName,
			Class<?> paramType) throws Exception {
		throw new HttpSessionRequiredException(
				"No HttpSession found for resolving parameter '" + paramName
						+ "' of type [" + paramType.getName() + "]");
	}

}
