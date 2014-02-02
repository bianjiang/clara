package edu.uams.clara.webapp.protocol.domain.businesslogicobject.enums;

public enum AgendaStatusEnum {
	AGENDA_INCOMPLETE("Agenda Incomplete"),
	AGENDA_PENDING_CHAIR_APPROVAL("Agenda Pending Chair Approval"),
	AGENDA_APPROVED("Agenda Approved By Chair"),
	CANCELLED("Cancelled"),
	MEETING_IN_PROGRESS("Meeting in Progress"),	
	MEETING_ADJOURNED("Meeting Adjourned"),
	MEETING_ADJOURNED_PENDING_CHAIR_APPROVAL("Meeting Pending Chair Approval"),
	MEETING_ADJOURNED_PENDING_IRB_OFFICE_PROCESS("Pending Transcription"),
	MEETING_CLOSED("Meeting has been finalized");
	

	private String description;
	
	private AgendaStatusEnum(String description){
		this.description = description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}
}
