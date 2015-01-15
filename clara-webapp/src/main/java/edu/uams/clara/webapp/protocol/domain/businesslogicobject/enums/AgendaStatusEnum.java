package edu.uams.clara.webapp.protocol.domain.businesslogicobject.enums;

public enum AgendaStatusEnum {
	AGENDA_INCOMPLETE("Agenda Incomplete", false, false),
	AGENDA_PENDING_CHAIR_APPROVAL("Agenda Pending Chair Approval", false, false),
	AGENDA_APPROVED("Agenda Approved By Chair", true, false),
	CANCELLED("Cancelled", false, false),
	MEETING_IN_PROGRESS("Meeting in Progress", true, false),	
	MEETING_ADJOURNED("Meeting Adjourned", true, true),
	MEETING_ADJOURNED_PENDING_CHAIR_APPROVAL("Meeting Pending Chair Approval", true, true),
	MEETING_ADJOURNED_PENDING_IRB_OFFICE_PROCESS("Pending Transcription", true, true),
	MEETING_CLOSED("Meeting has been finalized", true, true);
	

	private String description;
	private boolean agendaApproved;
	private boolean meetingCompleted;
	
	private AgendaStatusEnum(String description, Boolean agendaApproved, Boolean meetingCompleted){
		this.description = description;
		this.agendaApproved = agendaApproved;
		this.meetingCompleted = meetingCompleted;
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

	public boolean isMeetingCompleted() {
		return meetingCompleted;
	}

	public void setMeetingCompleted(boolean meetingCompleted) {
		this.meetingCompleted = meetingCompleted;
	}
}
