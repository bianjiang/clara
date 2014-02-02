package edu.uams.clara.webapp.protocol.businesslogic.irb.agenda;

import edu.uams.clara.webapp.common.domain.usercontext.User;
import edu.uams.clara.webapp.protocol.domain.irb.Agenda;

public interface AgendaStatusHelper {

	public enum AgendaStatusChangeAction{
		CREATE, FINALIZE, APPROVE, CANCEL, START_MEETING, STOP_MEETING, SEND_CHAIR_FOR_APPROVAL, APPROVED_BY_CHAIR, TRANSCRIBING_DONE;
	}
	
	void logStatus(Agenda agenda, User user, String note, AgendaStatusChangeAction action);
	void logStatusWithActor(Agenda agenda, User user, String note, String actor);
		
}
