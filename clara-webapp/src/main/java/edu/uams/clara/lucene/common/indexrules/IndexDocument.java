package edu.uams.clara.lucene.common.indexrules;

import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * IndexDocument is a collection of index rules, and settings for the index
 *
 */
@XmlRootElement( name="index-document" )
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "index-document", propOrder={"indexLocation", "indexRules"})
public class IndexDocument {
	
	public IndexDocument(){
		
	}
	
	public IndexDocument(String indexLocation, Set<IndexRule> indexRules){
		this.indexLocation = indexLocation;
		this.indexRules = indexRules;
	}
	
	@XmlAttribute(name="id")
	private String identifier;

	@XmlAttribute(name="location")
	private String indexLocation;
	
	@XmlElementWrapper(name="index-rules")
	@XmlElement(name="index-rule")
	private Set<IndexRule> indexRules;

	public String getIndexLocation() {
		return indexLocation;
	}

	public void setIndexLocation(String indexLocation) {
		this.indexLocation = indexLocation;
	}

	public Set<IndexRule> getIndexRules() {
		return indexRules;
	}

	public void setIndexRules(Set<IndexRule> indexRules) {
		this.indexRules = indexRules;
	}

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}
	
	
}
