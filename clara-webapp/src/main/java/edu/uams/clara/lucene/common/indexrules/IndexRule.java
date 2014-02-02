package edu.uams.clara.lucene.common.indexrules;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.lucene.index.IndexableField;

import edu.emory.mathcs.backport.java.util.Arrays;

/**
 * An IndexRule represents a rule how an index (field) is done, where it ingests a set of datasources and spits out a set of indexed data.
 * It also helps to construct lucene queries 
 *
 */
@XmlRootElement( name="index-rule" )
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "index-rule", propOrder={"identifier", "luceneFieldType", "sourceFields", "format"})
public class IndexRule {

	/**
	 * Data sources, where the identifier (normally its an xpath in CLARA, but it really can be anything that's unique)
	 * e.g., /protocol/title, 
	 * 
	 * one could have multiple source fields mapped to one identifier, e.g., 
	 *  /protocol/pi -> pi.userId Long
	 *  			 -> pi.firstname String
	 *  			 -> pi.lastname String
	 *  		     -> pi.email String
	 *  So that the user can search for a PI by multiple different fields
	 *  Note: really, you should NEVER mix data types into one indexed fields. In the above example,
	 *  One can split the index into two in one document
	 *  	1) /protocol/pi.userId -> userId: org.apache.lucene.document.LongField
	 *  	2) /protocol/pi.name -> "{firstname} {lastname} {email}" org.apache.lucene.document.TextField
	 *  
	 */
	
	public IndexRule() {
		
	}
	
	public IndexRule(String identifier, Class<? extends IndexableField> luceneFieldType, String format, SourceField... sourceFields){
		this.identifier = identifier;
		this.luceneFieldType = luceneFieldType;
		this.sourceFields = new HashSet<SourceField>((List<SourceField>)Arrays.asList(sourceFields));
		this.format = format;
	}
	
	public IndexRule(String identifier, Class<? extends IndexableField> luceneFieldType, SourceField... sourceFields){
		this(identifier, luceneFieldType, null, sourceFields);
	}
	
	@XmlAttribute(name="id")
	private String identifier;
	
	/**
	 * The field that needs to be stored as
	 */
	@XmlAttribute(name="lucene-field-type")
	private Class<? extends IndexableField> luceneFieldType;
	
	@XmlElementWrapper(name="source-fields")
	@XmlElement(name="source-field")
	private Set<SourceField> sourceFields;
	
	/***
	 * the format string, it tells how value are constructued from the values of the dataSources
	 * "{firstname} {lastname} {email}", where dataSources should return 3 string values with these identifies
	 */
	@XmlAttribute(name="format")
	private String format;

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public Class<? extends IndexableField> getLuceneFieldType() {
		return luceneFieldType;
	}

	public void setLuceneFieldType(Class<? extends IndexableField> luceneFieldType) {
		this.luceneFieldType = luceneFieldType;
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public Set<SourceField> getSourceFields() {
		return sourceFields;
	}

	public void setSourceFields(Set<SourceField> sourceFields) {
		this.sourceFields = sourceFields;
	}
	
	@Override
	public int hashCode()
    {
		int hash = 17;
		hash = (int) (31 * hash + this.getIdentifier().hashCode());
		hash = 31 * hash + this.getClass().getSimpleName().hashCode();
		return hash;
    }
	
	@Override
	public boolean equals(Object obj) {
	    if (obj == null) {
	        return false;
	    }
	    if (getClass() != obj.getClass()) {
	        return false;
	    }
	    final IndexRule other = (IndexRule)obj;
	    
	    if(!this.getIdentifier().equals(other.getIdentifier())){
	    	return false;
	    }
	    return true;
	}
	
	
	
}
