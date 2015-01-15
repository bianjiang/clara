package edu.uams.clara.webapp.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import edu.uams.clara.core.util.xml.XmlHandler;
import edu.uams.clara.core.util.xml.XmlHandlerFactory;
import edu.uams.clara.webapp.xml.processor.XmlProcessor;

@Service
public class RawvalueLookupService {
	private final static Logger logger = LoggerFactory.getLogger(RawvalueLookupService.class);
	
	private XmlProcessor xmlProcessor;
	
	@Value("${rawvalueLookupXml.url}")
	private String lookupXml;
	
	public String rawvalueLookUp(String rawValue){
		
		String humanReadableValue = rawValue;
		try{
		String xml = xmlProcessor.loadXmlFile(lookupXml);
		XmlHandler xmlHandler = XmlHandlerFactory.newXmlHandler();
		
		humanReadableValue = xmlHandler.getSingleStringValueByXPath(xml,"//option[value/text()=\""+rawValue+"\"]/desc/text()");
		}catch(Exception e){
			e.printStackTrace();
		}
		return humanReadableValue;

	}

	public XmlProcessor getXmlProcessor() {
		return xmlProcessor;
	}
	
	@Autowired(required = true)
	public void setXmlProcessor(XmlProcessor xmlProcessor) {
		this.xmlProcessor = xmlProcessor;
	}

	public String getLookupXml() {
		return lookupXml;
	}

	public void setLookupXml(String lookupXml) {
		this.lookupXml = lookupXml;
	}

}
