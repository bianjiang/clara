package edu.uams.clara.webapp.protocol.domain.protocolform.enums;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;


public enum ProtocolFormType {
	NEW_SUBMISSION("New Submission", "new-submission", ProtocolFormXmlDataType.PROTOCOL, "protocol"),
	HUMAN_SUBJECT_RESEARCH_DETERMINATION("Human Subject Research Determination", "human-subject-research-determination" , ProtocolFormXmlDataType.HUMAN_SUBJECT_RESEARCH_DETERMINATION, "hsrd"),
	RESPONSE("Response to Contingencies", "response" , ProtocolFormXmlDataType.RESPONSE, "response"),
	MODIFICATION("Modification", "modification", ProtocolFormXmlDataType.MODIFICATION, "protocol"),
	CONTINUING_REVIEW("Continuing Review", "continuing-review", ProtocolFormXmlDataType.CONTINUING_REVIEW, "continuing-review"),
	REPORTABLE_NEW_INFORMATION("Reportable New Information", "reportable-new-information", ProtocolFormXmlDataType.REPORTABLE_NEW_INFORMATION, "reportable-new-information"),
	DEATH_REPORT("Death Report", "death-report", ProtocolFormXmlDataType.DEATH_REPORT, "death-report"),
	STUDY_CLOSURE("Study Closure Form", "study-closure", ProtocolFormXmlDataType.STUDY_CLOSURE, "study-closure"),
	HUMANITARIAN_USE_DEVICE("Humanitarian Use Device Initial Application", "humanitarian-use-device", ProtocolFormXmlDataType.HUMANITARIAN_USE_DEVICE, "hud"),
	EMERGENCY_USE("Emergency Use", "emergency-use", ProtocolFormXmlDataType.EMERGENCY_USE, "emergency-use"),
	HUMANITARIAN_USE_DEVICE_RENEWAL("Humanitarian Use Device Renewal Application", "humanitarian-use-device-renewal", ProtocolFormXmlDataType.HUMANITARIAN_USE_DEVICE_RENEWAL, "hud-renewal"),
	AUDIT("Audit", "audit", ProtocolFormXmlDataType.AUDIT, "audit"),
	ARCHIVE("Archive", "archive", ProtocolFormXmlDataType.ARCHIVE, "archive"),
	STAFF("Staff Only Modification", "staff", ProtocolFormXmlDataType.STAFF, "staff");
	
	private String description;
	
	private String urlEncoded;
	
	private String baseTag;
	
	private ProtocolFormXmlDataType defaultProtocolFormXmlDataType;
	
	private ProtocolFormType(String description, String urlEncoded, ProtocolFormXmlDataType defaultProtocolFormXmlDataType, String baseTag){
		this.description = description;
		this.urlEncoded = urlEncoded;
		this.defaultProtocolFormXmlDataType = defaultProtocolFormXmlDataType;
		this.baseTag = baseTag;
	}
	
	private static final Map<String, ProtocolFormType> lookupByUrlCode = new HashMap<String, ProtocolFormType>();
	
	static {
		for (ProtocolFormType pft:EnumSet.allOf(ProtocolFormType.class)){
			lookupByUrlCode.put(pft.getUrlEncoded(), pft);
		}
	}
	
	public static ProtocolFormType getProtocolFormTypeByUrlCode(String protocolFormUrlName){
		return lookupByUrlCode.get(protocolFormUrlName);
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}

	public void setDefaultProtocolFormXmlDataType(
			ProtocolFormXmlDataType defaultProtocolFormXmlDataType) {
		this.defaultProtocolFormXmlDataType = defaultProtocolFormXmlDataType;
	}

	public ProtocolFormXmlDataType getDefaultProtocolFormXmlDataType() {
		return defaultProtocolFormXmlDataType;
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
