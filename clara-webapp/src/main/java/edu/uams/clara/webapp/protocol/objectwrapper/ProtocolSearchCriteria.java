package edu.uams.clara.webapp.protocol.objectwrapper;

import edu.uams.clara.webapp.common.objectwrapper.AbstractSearchCriteria;

public class ProtocolSearchCriteria extends AbstractSearchCriteria {

	public static enum ProtooclSearchOperator{
		EQUALS, CONTAINS, DOES_NOT_CONTAIN, IS;
	}
	
	public static enum ProtocolSearchField{
		IDENTIFIER, TITLE, PI_NAME, PROTOCOL_STATUS, PROTOCOL_FORM_STATUS, STAFF_NAME, STUDY_TYPE, COLLEGE, DEPARTMENT, DIVISION, FORM_TYPE, ASSIGNED_REVIEWER, PRIMARY_SITE, DRUG_NAME, LOCATION, MY_PROTOCOLS, STAFF_USERID, PI_USERID, ASSIGNED_REVIEWER_USERID, FORMER_STAFF_USERID, PENDING_PI_ACTION, FUNDING_SOURCE;
	}
	
	private ProtocolSearchField searchField;
	
	private ProtooclSearchOperator searchOperator;
	
	private String keyword;

	public ProtocolSearchField getSearchField() {
		return searchField;
	}

	public void setSearchField(ProtocolSearchField searchField) {
		this.searchField = searchField;
	}

	public ProtooclSearchOperator getSearchOperator() {
		return searchOperator;
	}

	public void setSearchOperator(ProtooclSearchOperator searchOperator) {
		this.searchOperator = searchOperator;
	}

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}
}
