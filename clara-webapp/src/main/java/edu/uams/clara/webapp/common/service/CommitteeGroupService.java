package edu.uams.clara.webapp.common.service;

import java.util.List;

import edu.uams.clara.webapp.common.domain.usercontext.enums.Committee;

public interface CommitteeGroupService {
	public Committee getParentCommittee(Committee committee);
	public List<Committee> getChildCommittees(Committee committee);
}
