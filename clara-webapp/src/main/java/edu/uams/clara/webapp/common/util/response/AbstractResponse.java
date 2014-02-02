package edu.uams.clara.webapp.common.util.response;

public abstract class AbstractResponse {

	private boolean error;
	private String message;
	private String redirect;
	private Object data;
	private boolean shouldRedirect;
	
	public AbstractResponse(boolean error, String message, String redirect, boolean shouldRedirect, Object data){
		this.error = error;
		this.message = message;
		this.redirect = redirect;
		this.data = data;
		this.shouldRedirect = shouldRedirect;
	}
		
	
	public boolean isError() {
		return error;
	}
	public void setError(boolean error) {
		this.error = error;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getRedirect() {
		return redirect;
	}
	public void setRedirect(String redirect) {
		this.redirect = redirect;
	}
	public Object getData() {
		return data;
	}
	public void setData(Object data) {
		this.data = data;
	}
	public boolean isShouldRedirect() {
		return shouldRedirect;
	}
	public void setShouldRedirect(boolean shouldRedirect) {
		this.shouldRedirect = shouldRedirect;
	}
	
	//default to return the current object, json marshalling is done on the httpmessageconvert
	// the xml marshalling needs to be done maually. too much overhead and trouble to do jaxb2
	protected Object toResponse(){
		return this;
	}
	
}
