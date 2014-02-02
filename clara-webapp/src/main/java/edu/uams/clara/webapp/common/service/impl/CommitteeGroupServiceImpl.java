package edu.uams.clara.webapp.common.service.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import edu.uams.clara.webapp.common.domain.usercontext.enums.Committee;
import edu.uams.clara.webapp.common.service.CommitteeGroupService;
import edu.uams.clara.webapp.xml.processor.XmlProcessor;

public class CommitteeGroupServiceImpl implements CommitteeGroupService {
	private final static Logger logger = LoggerFactory
			.getLogger(CommitteeGroupServiceImpl.class);
	
	private final XmlProcessor xmlProcessor;
	
	private final File committeesGroupXmlFile;
	
	private final ResourceLoader resourceLoader;
	//ImmutableMap are thread-safe once constructed... 
	private final ImmutableMap<Committee, Committee> parentCommitteeMapping;
		
	private final ImmutableMap<Committee, List<Committee>> childCommitteesMapping;
		
	@Autowired(required=true)
	public CommitteeGroupServiceImpl(final ResourceLoader resourceLoader, final @Value("${committees.grouping.xml.uri}") String committeesGroupingXmlFilePath, final XmlProcessor xmlProcessor) throws XPathExpressionException, IOException, SAXException{
		this.resourceLoader = resourceLoader;
		this.xmlProcessor = xmlProcessor;
		this.committeesGroupXmlFile = resourceLoader.getResource(committeesGroupingXmlFilePath).getFile();
		logger.debug("xmlFile: " + committeesGroupXmlFile.getAbsolutePath());
		this.parentCommitteeMapping = ImmutableMap.copyOf(loadParentCommitteesMapping());
		this.childCommitteesMapping = ImmutableMap.copyOf(loadChildCommitteesMapping());
	}	
	
	private Map<Committee, Committee> loadParentCommitteesMapping() throws IOException, SAXException, XPathExpressionException{
		Document committeesGroupDoc = xmlProcessor.loadXmlFileToDOM(committeesGroupXmlFile);
		XPath xpath = xmlProcessor.getXPathInstance();
		
		NodeList committeesNL = (NodeList)xpath.evaluate("/committees/committee", committeesGroupDoc, XPathConstants.NODESET);
		
		Map<Committee, Committee> loaded = Maps.newHashMap();
		for (int i = 0; i < committeesNL.getLength(); i ++){
			Element committeeEl = (Element)committeesNL.item(i);
			
			loaded.put(Committee.valueOf(committeeEl.getAttribute("name")), Committee.valueOf(committeeEl.getAttribute("parent")));
		}
		logger.debug("whatever: " + loaded.size());
		return loaded;
		
	}
	
	private Map<Committee, List<Committee>> loadChildCommitteesMapping() throws IOException, SAXException, XPathExpressionException{
		Document committeesGroupDoc = xmlProcessor.loadXmlFileToDOM(committeesGroupXmlFile);
		XPath xpath = xmlProcessor.getXPathInstance();
		
		Map<Committee, List<Committee>> loaded = Maps.newHashMap(); 
		// just load everything first
		for(Committee committee:Committee.values()){
			NodeList committeesNL = (NodeList)xpath.evaluate("/committees/committee[@parent='"+ committee +"']", committeesGroupDoc, XPathConstants.NODESET);
			
			List<Committee> childCommitteesList = new ArrayList<Committee>();
			
			for (int i = 0; i < committeesNL.getLength(); i ++){
				Element committeeEl = (Element)committeesNL.item(i);
				
				childCommitteesList.add(Committee.valueOf(committeeEl.getAttribute("name")));
			}
			
			loaded.put(committee, childCommitteesList);
		}
		
		return loaded;
	}

	@Override
	public Committee getParentCommittee(Committee committee) {		
		if (parentCommitteeMapping.size() == 0 || parentCommitteeMapping.get(committee) == null){
			return committee;
		}
		return parentCommitteeMapping.get(committee);
	}

	@Override
	public List<Committee> getChildCommittees(Committee committee) {
		
		List<Committee> childCommitteesOfCommittee = childCommitteesMapping.get(committee);
		
		if(childCommitteesOfCommittee == null){
			childCommitteesOfCommittee = Lists.newArrayList();
		}
		return childCommitteesOfCommittee;
	}	

	public ImmutableMap<Committee, Committee> getParentCommitteeMapping() {
		return parentCommitteeMapping;
	}

	public ImmutableMap<Committee, List<Committee>> getChildCommitteesMapping() {
		return childCommitteesMapping;
	}

	public ResourceLoader getResourceLoader() {
		return resourceLoader;
	}

	public File getCommitteesGroupXmlFile() {
		return committeesGroupXmlFile;
	}

}
