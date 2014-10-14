package edu.uams.clara.webapp.protocol.domain.businesslogicobject.enums;

public enum AgendaStatusEnum {
	AGENDA_INCOMPLETE("Agenda Incomplete", false),
	AGENDA_PENDING_CHAIR_APPROVAL("Agenda Pending Chair Approval", false),
	AGENDA_APPROVED("Agenda Approved By Chair", true),
	CANCELLED("Cancelled", false),
	MEETING_IN_PROGRESS("Meeting in Progress", true),	
	MEETING_ADJOURNED("Meeting Adjourned", true),
	MEETING_ADJOURNED_PENDING_CHAIR_APPROVAL("Meeting Pending Chair Approval", true),
	MEETING_ADJOURNED_PENDING_IRB_OFFICE_PROCESS("Pending Transcription", true),
	MEETING_CLOSED("Meeting has been finalized", true);
	

	private String description;
	private boolean agendaApproved;
	
	private AgendaStatusEnum(String description, Boolean agendaApproved){
		this.description = description;
		this.agendaApproved = agendaApproved;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}

	public boolean isAgendaApproved() {
		return agendaApproved;
	}

	public void setAgendaApproved(boolean agendaApproved) {
		this.agendaApproved = agendaApproved;
	}
}
