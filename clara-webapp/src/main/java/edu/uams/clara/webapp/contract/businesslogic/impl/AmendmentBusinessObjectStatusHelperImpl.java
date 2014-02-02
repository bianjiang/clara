package edu.uams.clara.webapp.contract.businesslogic.impl;

import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import edu.uams.clara.webapp.common.domain.form.Form;
import edu.uams.clara.webapp.common.domain.usercontext.User;
import edu.uams.clara.webapp.common.domain.usercontext.enums.Committee;
import edu.uams.clara.webapp.contract.businesslogic.ContractBusinessObjectStatusHelper;

public class AmendmentBusinessObjectStatusHelperImpl extends
		ContractBusinessObjectStatusHelper {
	
	private final static Logger logger = LoggerFactory
			.getLogger(AmendmentBusinessObjectStatusHelperImpl.class);

	public AmendmentBusinessObjectStatusHelperImpl()
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