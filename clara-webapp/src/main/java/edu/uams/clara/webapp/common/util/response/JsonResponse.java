package edu.uams.clara.webapp.common.util.response;


public class JsonResponse extends AbstractResponse {
	
			
	public JsonResponse(boolean error){
		this(error, "", "", false, null);
	}
	
	public JsonResponse(boolean error, Object data){
		this(error, "", "", false, data);
	}
	
	public JsonResponse(boolean error, String message, String redirect, boolean shouldRedirect){
		this(error, message, redirect, shouldRedirect, null);
	}
	
	public JsonResponse(boolean error, String message, String redirect, boolean shouldRedirect, Object data){
		super(error, message, redirect, shouldRedirect, data);		
	}
	
	
}
