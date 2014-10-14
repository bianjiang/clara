package edu.uams.clara.webapp.contract.objectwrapper;

import edu.uams.clara.webapp.common.objectwrapper.AbstractSearchCriteria;

public class ContractSearchCriteria extends AbstractSearchCriteria {

	public static enum ProtooclSearchOperator{
		EQUALS, CONTAINS, DOES_NOT_CONTAIN;
	}
	
	public static enum ContractSearchField{
		IDENTIFIER, TITLE, PI_NAME, CONTRACT_STATUS, STAFF_NAME, CONTRACT_TYPE, ENTITY_NAME, PROTOCOL_ID, STAFF_USERID, PI_USERID, ASSIGNED_REVIEWER_USERID;
	}
	
	private ContractSearchField searchField;
	
	private String searchFieldDescription;
	
	private ProtooclSearchOperator searchOperator;
	
	private String searchOperatorDescription;
	
	private String keyword;
	
	private String searchKeywordDescription;

	public ContractSearchField getSearchField() {
		return searchField;
	}

	public void setSearchField(ContractSearchField searchField) {
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

	public String getSearchFieldDescription() {
		return searchFieldDescription;
	}

	public void setSearchFieldDescription(String searchFieldDescription) {
		this.searchFieldDescription = searchFieldDescription;
	}

	public String getSearchOperatorDescription() {
		return searchOperatorDescription;
	}

	public void setSearchOperatorDescription(String searchOperatorDescription) {
		this.searchOperatorDescription = searchOperatorDescription;
	}

	public String getSearchKeywordDescription() {
		return searchKeywordDescription;
	}

	public void setSearchKeywordDescription(String searchKeywordDescription) {
		this.searchKeywordDescription = searchKeywordDescription;
	}
}
