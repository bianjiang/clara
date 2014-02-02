package edu.uams.clara.core.util.xml;

import java.security.CodeSource;
import java.text.MessageFormat;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.TransformerFactory;
import javax.xml.xpath.XPathFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public final class XmlUtils {

	private final static Logger logger = LoggerFactory
			.getLogger(XmlUtils.class);
	
	private static String getJaxpImplementationInfo(String componentName, Class componentClass) {
	    CodeSource source = componentClass.getProtectionDomain().getCodeSource();
	    return MessageFormat.format(
	            "{0} implementation: {1} loaded from: {2}",
	            componentName,
	            componentClass.getName(),
	            source == null ? "Java Runtime" : source.getLocation());
	}
	
	public static void OutputJaxpImplementationInfo(){
		 logger.debug(getJaxpImplementationInfo("DocumentBuilderFactory", DocumentBuilderFactory.newInstance().getClass()));
		 logger.debug(getJaxpImplementationInfo("XPathFactory", XPathFactory.newInstance().getClass()));
		 logger.debug(getJaxpImplementationInfo("TransformerFactory", TransformerFactory.newInstance().getClass()));
		 logger.debug(getJaxpImplementationInfo("SAXParserFactory", SAXParserFactory.newInstance().getClass()));

	}
}
