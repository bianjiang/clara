package edu.uams.clara.core.util.xml;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Properties;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public final class DomUtils extends org.springframework.util.xml.DomUtils {
	private final static Logger logger = LoggerFactory
			.getLogger(DomUtils.class);
	public static enum Encoding {
		UTF8 ("UTF-8"), UTF16("UTF-16"), ISO8859_1("iso-8859-1");
		
		private Encoding(String characterset){
			this.characterset = characterset;
		}
		
		public String getCharacterset() {
			return characterset;
		}

		public void setCharacterset(String characterset) {
			this.characterset = characterset;
		}

		private String characterset;
		
	}

	private static final TransformerFactory factory = TransformerFactory
			.newInstance();
	
	/*
	private static Transformer transformer = null;
	
	
	static {
		try {
			transformer = factory.newTransformer();
			Properties properties = new Properties();
			properties.setProperty(OutputKeys.ENCODING, Encoding.UTF16.getCharacterset());
			// properties.setProperty(OutputKeys.STANDALONE, "no");
			properties.setProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			transformer.setOutputProperties(properties);

		} catch (TransformerConfigurationException e) {

			e.printStackTrace();
			
		}
	}
	*/
	
	public static String elementToPrettyString(final Node node) {
		Properties properties = new Properties();

		properties.setProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");

		properties.setProperty(OutputKeys.ENCODING, Encoding.UTF8.getCharacterset());
		
		properties.setProperty(OutputKeys.INDENT, "yes");
		properties.setProperty(OutputKeys.METHOD, "xml");
		properties.setProperty("{http://xml.apache.org/xslt}indent-amount", "5");

		
		return elementToString(node, properties);
	}


	public static String elementToString(final Node node) {

		return elementToString(node, false, Encoding.UTF16);
	}
	
	public static String elementToString(final Node node, boolean declear) {

		return elementToString(node, declear, Encoding.UTF16);
	}


	public static String elementToString(final Node node, boolean declear,
			Encoding encoding) {		

		Properties properties = new Properties();

		if (declear == true) {
			properties.setProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
		} else {
			properties.setProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		}

		properties.setProperty(OutputKeys.ENCODING, encoding.getCharacterset());	
		

		return elementToString(node, properties);
	}
	
	private static String elementToString(final Node node, Properties properties){
		if (node == null) {

			return null;
		}

		Source source = new DOMSource(node);
		return toString(source, properties);		
	}
	
	private static String toString(final Source source, Properties properties){
		StringWriter stringWriter = new StringWriter();
		Result result = new StreamResult(stringWriter);
		String resultString = null;
		
		try {
			Transformer transformer = null;
			synchronized(factory){
				transformer = factory.newTransformer();
			}
			transformer.setOutputProperties(properties);					
			transformer.transform(source, result);
			
			resultString = stringWriter.getBuffer().toString();

		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			
			logger.error("DomUtils.TransformerException", e);
		}

		return resultString;
	}
	
	public static String toString(final Source source){
		
		Properties properties = new Properties();

		properties.setProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");

		properties.setProperty(OutputKeys.ENCODING, Encoding.UTF8.getCharacterset());
		
		return toString(source, properties);
	}
	
	
	public static Source toSource(String inXml){
		return new StreamSource(new StringReader(inXml));
	}
	
	public static Source toSource(Document document){
		return new DOMSource(document);
	}
	
	
}
