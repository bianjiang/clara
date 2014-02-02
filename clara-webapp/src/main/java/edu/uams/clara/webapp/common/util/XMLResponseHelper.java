package edu.uams.clara.webapp.common.util;

import javax.xml.transform.Source;

import org.w3c.dom.Node;

import edu.uams.clara.core.util.xml.DomUtils;
import edu.uams.clara.webapp.common.util.response.XmlResponse;

public class XMLResponseHelper {
	/*
	public static String xmlResult(Object o){
		return "<result>" + o.toString() + "</result>";
	}
	public static Source xmlSourceResult(Object o){
		return DomUtils.toSource(xmlResult(o));
	}
	
	*/
	/***These need to go away slowly ***/
	public static String xmlResult(Object o){
		return "<result>" + o.toString() + "</result>";
	}
	
	public static Source newErrorResponseStub(final String message){
		return XMLResponseHelper.newXmlResponseStub(true, message);
	}
	
	public static Source newSuccessResponseStube(final String message){
		return XMLResponseHelper.newXmlResponseStub(false, message);
	}
	
	public static Source newDataResponseStub(final String data){
		return XMLResponseHelper.newXmlResponseStub(false, null, data);
	}
	
	public static Source newDataResponseStub(final Node node){
		return XMLResponseHelper.newXmlResponseStub(false, null, node);
	}
	
	public static Source newXmlResponseStub(boolean error, final String message){
		XmlResponse xmlResponse = new XmlResponse(error, message, null, false, null);
		
		return xmlResponse.toResponse();
	}
	
	public static Source newXmlResponseStub(boolean error, final String message, final String data){
		XmlResponse xmlResponse = new XmlResponse(error, message, null, false, data);
		
		return xmlResponse.toResponse();
	}
	
	
	public static Source newXmlResponseStub(boolean error, final String message, final Node node){

		XmlResponse xmlResponse = new XmlResponse(error, message, null, false, DomUtils.elementToString(node));
		
		return xmlResponse.toResponse();
		 
	}
}
