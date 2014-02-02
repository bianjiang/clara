package edu.uams.clara.webapp.protocol.domain.businesslogicobject.enums;

public enum ContingencyType {
	FORMATTING("Formatting Issues"), OTHER("Other");
	
	private String description;
	
	private ContingencyType(String description){
		this.description = description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}
	
}
