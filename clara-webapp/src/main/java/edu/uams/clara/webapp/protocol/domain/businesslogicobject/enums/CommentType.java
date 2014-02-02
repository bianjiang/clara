package edu.uams.clara.webapp.protocol.domain.businesslogicobject.enums;

public enum CommentType {
	CONTINGENCY("Contingency"), NOTE("Note"),	// REMOVE THIS ROW WHEN UI IS FINISHED
	CONTINGENCY_MAJOR("Major Contingency"), CONTINGENCY_MINOR("Minor Contingency"), 
	NOTE_MAJOR("Major Note"),NOTE_MINOR("Minor Note"),
	PERSONAL_NOTE("Personal Note"), REPLY("Reply"),
	COMMITTEE_PRIVATE_NOTE("Committee Private Note"),
	STUDYWIDE("Study Wide Note");
	
	
	private String description;
	
	private CommentType(String description){
		this.description = description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}
}
