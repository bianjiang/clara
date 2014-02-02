package edu.uams.clara.webapp.protocol.domain.businesslogicobject.enums;

public enum ChecklistQuestionAnswer {
	YES("Yes"), NO("No"), NOT_CERTAIN("Not Certain"), NOT_APPLICABLE("Not Applicable");
	
	private String description;
	
	private ChecklistQuestionAnswer(String description){
		this.description = description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}
	
}
