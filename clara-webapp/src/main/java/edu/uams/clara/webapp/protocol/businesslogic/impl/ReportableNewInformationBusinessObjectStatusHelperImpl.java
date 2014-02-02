package edu.uams.clara.webapp.protocol.businesslogic.impl;

import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.uams.clara.webapp.common.domain.form.Form;
import edu.uams.clara.webapp.common.domain.usercontext.User;
import edu.uams.clara.webapp.common.domain.usercontext.enums.Committee;
import edu.uams.clara.webapp.protocol.businesslogic.ProtocolBusinessObjectStatusHelper;

public class ReportableNewInformationBusinessObjectStatusHelperImpl  extends
ProtocolBusinessObjectStatusHelper {
	private final static Logger logger = LoggerFactory
			.getLogger(ReportableNewInformationBusinessObjectStatusHelperImpl.class);
	
	public ReportableNewInformationBusinessObjectStatusHelperImpl()
			throws ParserConfigurationException {
		super();
	}
	
	@Override
	public String checkCondition(Form form, Committee committee, User user,
			String action, String extraDataXml) {
		return null;
	}
}
