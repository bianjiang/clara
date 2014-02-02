package edu.uams.clara.lucene.common.indexrules;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * A generic POJO to represent the source data contained in indexing rules
 *
 */
@XmlRootElement( name="source-field" )
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "source-field", propOrder={"identifier", "sourceObject", "field", "sourceFieldDataType", "query", "dataType"})
public class SourceField {
	
	@XmlRootElement(name="source-field-data-type")
	public enum SourceFieldDataType {
		XML, VALUE;
	};

	public SourceField(){
		
	}
	
	/**
	 * 
	 * @param sourceObject
	 * @param field
	 * @param fieldDataType
	 * @param query
	 */
	public SourceField(String identifier, Class<?> sourceObject, String field, SourceFieldDataType sourceFieldDataType, String query, Class<?> dataType){
		this.identifier = identifier;
		this.sourceObject = sourceObject;
		this.field = field;
		this.sourceFieldDataType = sourceFieldDataType;
		this.query = query;
		this.dataType = dataType;
	}
	
	/**
	 * The identifier to identify this sourcefield
	 */
	@XmlAttribute(name="id")
	private String identifier;
	
	/**
	 * The source domain object that we can get data from
	 */
	@XmlAttribute(name="source-object")
	private Class<?> sourceObject;

	/**
	 * Specific field of the source in database
	 */
	@XmlAttribute(name="field")
	private String field;
	
	/**
	 * The field could contain xml string or just a value field in the database
	 */
	@XmlAttribute(name="source-field-data-type")
	private SourceFieldDataType sourceFieldDataType;
	
	/**
	 * For xml, we need xpath to identify the field
	 */
	@XmlAttribute(name="query")
	private String query;
	
	/**
	 * the type of the data (String.classs, Integer.class, etc.)
	 */
	@XmlAttribute(name="data-type")
	private Class<?> dataType;

	public Class<?> getSourceObject() {
		return sourceObject;
	}

	public void setSourceObject(Class<?> sourceObject) {
		this.sourceObject = sourceObject;
	}

	public String getField() {
		return field;
	}

	public void setField(String field) {
		this.field = field;
	}


	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public SourceFieldDataType getSourceFieldDataType() {
		return sourceFieldDataType;
	}

	public void setSourceFieldDataType(SourceFieldDataType sourceFieldDataType) {
		this.sourceFieldDataType = sourceFieldDataType;
	}

	public Class<?> getDataType() {
		return dataType;
	}

	public void setDataType(Class<?> dataType) {
		this.dataType = dataType;
	}

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
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
	    final SourceField other = (SourceField)obj;
	    
	    if(!this.getIdentifier().equals(other.getIdentifier())){
	    	return false;
	    }
	    return true;
	}
}
