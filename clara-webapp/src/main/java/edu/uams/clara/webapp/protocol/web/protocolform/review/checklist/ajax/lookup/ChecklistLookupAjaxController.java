package edu.uams.clara.webapp.protocol.web.protocolform.review.checklist.ajax.lookup;

import java.io.FileNotFoundException;
import java.io.IOException;

import javax.xml.xpath.XPathExpressionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.xml.sax.SAXException;

import edu.uams.clara.webapp.common.domain.usercontext.enums.Committee;
import edu.uams.clara.webapp.protocol.domain.protocolform.enums.ProtocolFormType;
import edu.uams.clara.webapp.xml.processor.XmlProcessor;

/**
 * TODO: add caching
 */
@Controller
public class ChecklistLookupAjaxController {

	private final static Logger logger = LoggerFactory
	.getLogger(ChecklistLookupAjaxController.class);
		
	private ResourceLoader resourceLoader;
	
	private XmlProcessor xmlProcessor;
	
	/*
	@Cacheable(cacheName = "checkListCache", keyGenerator = @KeyGenerator(name = "HashCodeCacheKeyGenerator", properties = {
			@Property(name = "includeMethod", value = "false") }))*/
	@RequestMapping(value = "/ajax/protocols/protocolForms/review/checklists/listByCommitteeAndFormType.xml", method = RequestMethod.GET)
	public @ResponseBody String getChecklistByCommitteeAndFormType(@RequestParam("committee") Committee committee, @RequestParam("protocolFormType") ProtocolFormType protocolFormType) throws FileNotFoundException, IOException, XPathExpressionException, SAXException{
		
		Resource  checklistXmlFileResource = null;
		
		switch(committee){
		case IRB_OFFICE:
			switch(protocolFormType){
			case HUMAN_SUBJECT_RESEARCH_DETERMINATION:
				checklistXmlFileResource = resourceLoader.getResource("/static/xml/checklists/hsrd-checklist.xml");
				break;
			case EMERGENCY_USE:
				checklistXmlFileResource = resourceLoader.getResource("/static/xml/checklists/eu-checklist.xml");
				break;
			default:
				break;
			}
			break;
		default:
			break;
		}
		
		String checklistXmlString = xmlProcessor.loadXmlFile(checklistXmlFileResource.getFile());
		
		return xmlProcessor.listElementsByPath("/checklists/checklistgroup[@form-type='" + protocolFormType.toString() + "']/committees/committee/[@name='" + committee.toString() + "' and protocol-form-committee-status='']/parent()/parent()/", checklistXmlString, false);
	}

	@Autowired(required=true)
	public void setXmlProcessor(XmlProcessor xmlProcessor) {
		this.xmlProcessor = xmlProcessor;
	}

	public XmlProcessor getXmlProcessor() {
		return xmlProcessor;
	}

	@Autowired(required=true)
	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}

	public ResourceLoader getResourceLoader() {
		return resourceLoader;
	}
}
