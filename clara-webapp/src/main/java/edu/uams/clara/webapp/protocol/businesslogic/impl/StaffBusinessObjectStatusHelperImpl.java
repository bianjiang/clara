package edu.uams.clara.webapp.protocol.businesslogic.impl;

import javax.xml.parsers.ParserConfigurationException;

import edu.uams.clara.webapp.common.domain.form.Form;
import edu.uams.clara.webapp.common.domain.usercontext.User;
import edu.uams.clara.webapp.common.domain.usercontext.enums.Committee;
import edu.uams.clara.webapp.protocol.businesslogic.ProtocolBusinessObjectStatusHelper;

public class StaffBusinessObjectStatusHelperImpl extends ProtocolBusinessObjectStatusHelper {

	public StaffBusinessObjectStatusHelperImpl()
			throws ParserConfigurationException {
		super();
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public String checkCondition(Form form, Committee committee, User user,
			String action, String extraDataXml) {
		return null;
	}

}
