package edu.uams.clara.webapp.common.util;

import edu.uams.clara.webapp.common.util.response.JsonResponse;

public class JsonResponseHelper {
	
	
	public static JsonResponse newErrorResponseStub(final String message){
		return JsonResponseHelper.newJsonResponseStub(true, message);
	}
	
	public static JsonResponse newSuccessResponseStube(final String message){
		return JsonResponseHelper.newJsonResponseStub(false, message);
	}
	
	
	public static JsonResponse newDataResponseStub(final Object data){
		return JsonResponseHelper.newJsonResponseStub(false, null, data);
	}
	
	public static JsonResponse newJsonResponseStub(boolean error, final String message){

		JsonResponse jsonResponse = new JsonResponse(error, message, null, false, null);
		
		return jsonResponse;
		 
	}
		
	public static JsonResponse newJsonResponseStub(boolean error, final String message, final Object data){

		JsonResponse jsonResponse = new JsonResponse(error, message, null, false, data);
		
		return jsonResponse;
		 
	}
	
	public static JsonResponse newJsonResponseStub(boolean error, final String message, String redirect, boolean shouldRedirect, final Object data){

		JsonResponse jsonResponse = new JsonResponse(error, message, null, false, data);
		
		return jsonResponse;
		 
	}
}
