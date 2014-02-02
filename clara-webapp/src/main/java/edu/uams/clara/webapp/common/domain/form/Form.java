package edu.uams.clara.webapp.common.domain.form;

public interface Form {
	String getFormType();
	long getFormId();
	long getParentFormId();
	String getMetaXml();
	String getIdentifier();
	//get Contract or Protocol meta data
	String getObjectMetaData();
}
