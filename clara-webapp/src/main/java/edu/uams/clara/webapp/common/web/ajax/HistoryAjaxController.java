package edu.uams.clara.webapp.common.web.ajax;

import java.io.IOException;

import javax.xml.transform.Source;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.slf4j.Logger;
import edu.uams.clara.webapp.common.dao.history.TrackDao;
import edu.uams.clara.webapp.common.domain.history.Track;
import edu.uams.clara.webapp.common.util.XMLResponseHelper;
import edu.uams.clara.webapp.xml.processor.XmlProcessor;

@Controller
public class HistoryAjaxController {
	private final static Logger logger = LoggerFactory
			.getLogger(HistoryAjaxController.class);
	
	private TrackDao trackDao;
	
	private XmlProcessor xmlProcessor;
	
	@RequestMapping(value = "/ajax/history/history.xml", method = RequestMethod.GET, produces = "application/xml")
	public @ResponseBody Source getHistory(@RequestParam("type") String type, @RequestParam("id") long id, @RequestParam(value="filter", required=false) String filter) throws ClassNotFoundException, IOException, SAXException, XPathExpressionException{
		
		Class<?> objectClass = Class.forName(type);
		//logger.debug("type: " + objectClass.getSimpleName() + " class: " + objectClass + " id: " + id);
		
		Track track = null;
		
		try {
			track = trackDao.getTrackByTypeAndRefObjectClassAndId(objectClass.getSimpleName(), objectClass, id);
		} catch (Exception e) {
			e.printStackTrace();
			
			Document newLogsDoc = xmlProcessor.newDocument();
			Element logsRoot = newLogsDoc.createElement("logs");
			newLogsDoc.appendChild(logsRoot);
			return XMLResponseHelper.newDataResponseStub(newLogsDoc);
		}
		
		if (track ==  null) {
			Document newLogsDoc = xmlProcessor.newDocument();
			Element logsRoot = newLogsDoc.createElement("logs");
			newLogsDoc.appendChild(logsRoot);
			return XMLResponseHelper.newDataResponseStub(newLogsDoc);
		}

		if(filter != null){
			filter = filter.toUpperCase();
			
			Document logsDoc = xmlProcessor.loadXmlStringToDOM(track.getXmlData());
			
			Document newLogsDoc = xmlProcessor.newDocument();
			
			Element logsRoot = newLogsDoc.createElement("logs");
			
			logsRoot.setAttribute("object-type",
					objectClass.getSimpleName());
			logsRoot.setAttribute("object-id", "" + id);
			
			XPath xpath = xmlProcessor.getXPathInstance();
			
			NodeList letterLogs = (NodeList)xpath.evaluate("/logs/log[@log-type = '" + filter + "']", logsDoc, XPathConstants.NODESET);
			
			for (int i = 0; i < letterLogs.getLength(); i ++){
				logsRoot.appendChild(newLogsDoc.importNode(letterLogs.item(i), true));
			}
			
			newLogsDoc.appendChild(logsRoot);
			
			logger.debug("filter done!");
			//return DomUtils.elementToString(newLogsDoc);
			
			return XMLResponseHelper.newDataResponseStub(newLogsDoc);
		}
		
		//logger.debug("track load done!");
		//return track.getXmlData();
		return XMLResponseHelper.newDataResponseStub(track.getXmlData());
	}

	public TrackDao getTrackDao() {
		return trackDao;
	}
	
	@Autowired(required = true)
	public void setTrackDao(TrackDao trackDao) {
		this.trackDao = trackDao;
	}

	public XmlProcessor getXmlProcessor() {
		return xmlProcessor;
	}

	@Autowired(required = true)
	public void setXmlProcessor(XmlProcessor xmlProcessor) {
		this.xmlProcessor = xmlProcessor;
	}
}
