package edu.uams.clara.webapp.common.util.response;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.transform.Source;

import edu.uams.clara.core.util.xml.DomUtils;


@XmlRootElement( name="result" )
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "result", propOrder={"error", "message", "redirect", "data", "query", "shouldRedirect"})
public class XmlResponse extends AbstractResponse {

	public XmlResponse(boolean error, String message, String redirect,
			boolean shouldRedirect, Object data) {
		super(error, message, redirect, shouldRedirect, data);
	}
	
	@XmlElement( name="error" )
	public boolean isError() {
		return super.isError();
	}

	@XmlElement( name="message" )
	public String getMessage() {
		return super.getMessage();
	}

	@XmlElement( name="redirect" )
	public String getRedirect() {
		if(super.getRedirect() == null){
			return "";
		}
		return super.getRedirect();
	}

	@XmlElement( name="data" )
	public String getData() {
		if(super.getData() == null){
			return "";
		}
		return super.getData().toString();
	}

	@XmlElement( name="shouldRedirect" )
	public boolean isShouldRedirect() {
		return super.isShouldRedirect();
	}

	// dooing this to avoid jaxb2 overhead, but the problem might be how do we maintain this...
	@Override
	public Source toResponse(){
		
		String response = "<result>";
		response += "<error>" + this.isError() + "</error>";
		response += "<message>" + this.getMessage() + "</message>";
		response += "<redirect>" + this.getRedirect() + "</redirect>";
		response += "<data>" + this.getData() + "</data>";
		response += "<shouldRedirect>" + this.isShouldRedirect() + "</shouldRedirect>";
		response += "</result>";
		
		return DomUtils.toSource(response);
	}

}
