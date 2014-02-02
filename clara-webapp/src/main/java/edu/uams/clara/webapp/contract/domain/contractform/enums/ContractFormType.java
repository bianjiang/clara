package edu.uams.clara.webapp.contract.domain.contractform.enums;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;


public enum ContractFormType {
	NEW_CONTRACT("New Contract", "new-contract", ContractFormXmlDataType.CONTRACT, "contract"),
	AMENDMENT("Amendment", "amendment", ContractFormXmlDataType.AMENDMENT, "contract");
	
	private String description;
	
	private String urlEncoded;
	
	private String baseTag;
	
	private ContractFormXmlDataType defaultContractFormXmlDataType;
	
	private ContractFormType(String description, String urlEncoded, ContractFormXmlDataType defaultContractFormXmlDataType, String baseTag){
		this.description = description;
		this.urlEncoded = urlEncoded;
		this.defaultContractFormXmlDataType = defaultContractFormXmlDataType;
		this.baseTag = baseTag;
	}
	
	private static final Map<String, ContractFormType> lookupByUrlCode = new HashMap<String, ContractFormType>();
	
	static {
		for (ContractFormType cft:EnumSet.allOf(ContractFormType.class)){
			lookupByUrlCode.put(cft.getUrlEncoded(), cft);
		}
	}
	
	public static ContractFormType getContractFormTypeByUrlCode(String contractFormUrlName){
		return lookupByUrlCode.get(contractFormUrlName);
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}

	public void setDefaultContractFormXmlDataType(
			ContractFormXmlDataType defaultContractFormXmlDataType) {
		this.defaultContractFormXmlDataType = defaultContractFormXmlDataType;
	}

	public ContractFormXmlDataType getDefaultContractFormXmlDataType() {
		return defaultContractFormXmlDataType;
	}

	public void setUrlEncoded(String urlEncoded) {
		this.urlEncoded = urlEncoded;
	}

	public String getUrlEncoded() {
		return urlEncoded;
	}

	public void setBaseTag(String baseTag) {
		this.baseTag = baseTag;
	}

	public String getBaseTag() {
		return baseTag;
	}	
}
