package edu.uams.clara.integration.outgoing.webchart;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import edu.uams.clara.core.util.xml.XmlHandler;
import edu.uams.clara.core.util.xml.XmlHandlerFactory;
import edu.uams.clara.integration.outgoing.ctms.dao.AriaUserDao;
import edu.uams.clara.integration.outgoing.ctms.domain.AriaUser;
import edu.uams.clara.integration.outgoing.webchart.dao.WebChartARIAProtocolDao;
import edu.uams.clara.integration.outgoing.webchart.dao.WebChartARIAUserDao;
import edu.uams.clara.integration.outgoing.webchart.dao.WebChartARIAUserProtocolDao;
import edu.uams.clara.integration.outgoing.webchart.dao.WebChartClinicalTrialPatientContactDao;
import edu.uams.clara.integration.outgoing.webchart.domain.WebChartARIAProtocol;
import edu.uams.clara.integration.outgoing.webchart.domain.WebChartARIAUser;
import edu.uams.clara.integration.outgoing.webchart.domain.WebChartARIAUserProtocol;
import edu.uams.clara.integration.outgoing.webchart.domain.WebChartClinicalTrialPatientContact;
import edu.uams.clara.webapp.common.dao.usercontext.UserDao;
import edu.uams.clara.webapp.common.domain.usercontext.Person;
import edu.uams.clara.webapp.common.domain.usercontext.User;
import edu.uams.clara.webapp.protocol.dao.ProtocolDao;
import edu.uams.clara.webapp.protocol.dao.protocolform.ProtocolFormDao;
import edu.uams.clara.webapp.protocol.domain.Protocol;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.enums.ProtocolStatusEnum;

@Service
public class ExportProtocolsToWebChartService {
	private final static Logger logger = LoggerFactory
			.getLogger(ExportProtocolsToWebChartService.class);

	private WebChartARIAUserDao webChartARIAUserDao;
	private WebChartARIAProtocolDao webChartARIAProtocolDao;
	private ProtocolDao protocolDao;
	private ProtocolFormDao protocolFormDao;
	private UserDao userDao;
	private AriaUserDao ariaUserDao;
	private WebChartARIAUserProtocolDao webChartARIAUserProtocolDao;
	private WebChartClinicalTrialPatientContactDao webChartClinicalTrialPatientContactDao;

	// relative to /protocol/staffs/staff/user
	private Set<String> userInfoXPaths = Sets.newHashSet();
	{
		userInfoXPaths.add("@id");
		userInfoXPaths.add("@pi_serial");
		userInfoXPaths.add("./email");
		userInfoXPaths.add("./firstname");
		userInfoXPaths.add("./lastname");

	}

	private WebChartARIAProtocol saveOrUpdateWebChartARIAProtocol(
			Protocol protocol) {
		WebChartARIAProtocol webChartARIAProtocol = null;
		try {
			try {
				webChartARIAProtocol = webChartARIAProtocolDao
						.findByClaraProtocolCode(protocol
								.getProtocolIdentifier());
				logger.debug("found webChartARIAProtocol: "
						+ webChartARIAProtocol.getId());
			} catch (Exception ex) {
				// don't care create new
			}
			// delete
			if (webChartARIAProtocol != null && (protocol.isRetired()||protocolDao.getLatestProtocolStatusByProtocolId(protocol.getId()).getProtocolStatus().equals(ProtocolStatusEnum.DRAFT))) {
				logger.debug("retire irbNumber: "
						+ webChartARIAProtocol.getProtocolCode());
				deleteWebChartARIAProtocol(webChartARIAProtocol);
				return null;
			}

			// not adding reitred ones in...
			if (protocol.isRetired()) {
				logger.debug("Retired protocol, so just go on...");
				return null;
			}

			XmlHandler xmlHandler = XmlHandlerFactory.newXmlHandler();

			String title = xmlHandler.getSingleStringValueByXPath(
					protocol.getMetaDataXml(), "/protocol/title");
			String status = xmlHandler.getSingleStringValueByXPath(
					protocol.getMetaDataXml(), "/protocol/status");
			String firstName = "";
			String lastName = "";
			firstName = xmlHandler
					.getSingleStringValueByXPath(
							protocol.getMetaDataXml(),
							"/protocol/staffs/staff/user[roles/role/text()=\"Principal Investigator\"]/firstname");
			lastName = xmlHandler
					.getSingleStringValueByXPath(
							protocol.getMetaDataXml(),
							"/protocol/staffs/staff/user[roles/role/text()=\"Principal Investigator\"]/lastname");

			if (firstName.isEmpty() && lastName.isEmpty()) {
				firstName = xmlHandler
						.getSingleStringValueByXPath(
								protocol.getMetaDataXml(),
								"/protocol/staffs/staff/user[roles/role/text()=\"principal investigator\"]/firstname");
				lastName = xmlHandler
						.getSingleStringValueByXPath(
								protocol.getMetaDataXml(),
								"/protocol/staffs/staff/user[roles/role/text()=\"principal investigator\"]/lastname");
			}

			if (webChartARIAProtocol == null) {
				webChartARIAProtocol = new WebChartARIAProtocol();
			}
			webChartARIAProtocol.setPiName(firstName + " " + lastName);
			webChartARIAProtocol.setProtocolCode(protocol
					.getProtocolIdentifier());
			webChartARIAProtocol.setProtocolDescription(title);
			webChartARIAProtocol.setProtocolStatus(status.toUpperCase());

			String currentDateStr = DateFormat.getInstance().format(new Date());
			webChartARIAProtocol.setRecordDate(currentDateStr);
			webChartARIAProtocolDao.saveOrUpdate(webChartARIAProtocol);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return webChartARIAProtocol;

	}

	private void updateNewAddedPatientContact() {
		List<WebChartClinicalTrialPatientContact> webChartClinicalTrialPatientContacts = webChartClinicalTrialPatientContactDao
				.findAll();

		for (WebChartClinicalTrialPatientContact webChartClinicalTrialPatientContact : webChartClinicalTrialPatientContacts) {
			String cidStr = webChartClinicalTrialPatientContact.getCid();
			if (cidStr != null && !cidStr.isEmpty()) {
				String firstCha = cidStr.substring(0, 1);
				if (firstCha.equals("A")) {
					String tempCid = cidStr.replace("A", "");
					long cid = Long.valueOf(tempCid);
					if (cid > 20000000) {
						continue;
					}
					String wrapId = "A" + (20000000 + cid);
					webChartClinicalTrialPatientContact.setCid(wrapId);
					String currentDateStr = DateFormat.getInstance().format(
							new Date());
					webChartClinicalTrialPatientContact
							.setRecordDate(currentDateStr);
					webChartClinicalTrialPatientContactDao
							.saveOrUpdate(webChartClinicalTrialPatientContact);
				}
			}
		}
	}

	private void addPreFixtoAriaUserID() {
		// When run the code first time, add prefix to all ariauser id

		List<WebChartARIAUser> webChartARIAUsers = webChartARIAUserDao
				.findAll();
		for (WebChartARIAUser webChartARIAUser : webChartARIAUsers) {
			String userCode = webChartARIAUser.getAriaUserCode();
			if (userCode != null && !userCode.isEmpty()) {
				long userCodeLong = Long.valueOf(userCode);
				String wrapId = (20000000 + userCodeLong) + "";
				webChartARIAUser.setAriaUserCode(wrapId);
				String currentDateStr = DateFormat.getInstance().format(
						new Date());
				webChartARIAUser.setRecordDAte(currentDateStr);
				webChartARIAUserDao.saveOrUpdate(webChartARIAUser);
			}
		}

		List<WebChartARIAUserProtocol> webChartARIAUserProtocols = webChartARIAUserProtocolDao
				.findAll();
		for (WebChartARIAUserProtocol webChartARIAUserProtocol : webChartARIAUserProtocols) {
			String userCode = webChartARIAUserProtocol.getAriaUserCode();
			if (userCode != null && !userCode.isEmpty()) {
				long userCodeLong = Long.valueOf(userCode);
				String wrapId = (20000000 + userCodeLong) + "";
				webChartARIAUserProtocol.setAriaUserCode(wrapId);
				String currentDateStr = DateFormat.getInstance().format(
						new Date());
				webChartARIAUserProtocol.setRecordDate(currentDateStr);
				webChartARIAUserProtocolDao
						.saveOrUpdate(webChartARIAUserProtocol);
			}
		}

		/*List<WebChartClinicalTrialPatientContact> webChartClinicalTrialPatientContacts = webChartClinicalTrialPatientContactDao
				.findAll();

		for (WebChartClinicalTrialPatientContact webChartClinicalTrialPatientContact : webChartClinicalTrialPatientContacts) {
			String cidStr = webChartClinicalTrialPatientContact.getCid();
			if (cidStr != null && !cidStr.isEmpty()) {
				String firstCha = cidStr.substring(0, 1);
				logger.debug(cidStr);
				logger.debug(firstCha);
				if (firstCha.equals("A")) {
					String tempCid = cidStr.replace("A", "");
					long cid = Long.valueOf(tempCid);
					String wrapId = "A" + (20000000 + cid);
					webChartClinicalTrialPatientContact.setCid(wrapId);
					String currentDateStr = DateFormat.getInstance().format(
							new Date());
					logger.debug(wrapId);
					webChartClinicalTrialPatientContact
							.setRecordDate(currentDateStr);
					webChartClinicalTrialPatientContactDao
							.saveOrUpdate(webChartClinicalTrialPatientContact);
				}
			}
		}*/
	}

	private WebChartARIAUser findOrCreateWebChartARIAUserANDWebChartARIAUserProtocol(
			Map<String, String> userInfo, Protocol p) {
		// code for first time running only, will warp the existing user id in
		// webchart
		long piSerial = 0;
		long id = 0;
		try {
			piSerial = Long.parseLong(userInfo.get("@pi_serial"));
		} catch (Exception ex) {
			// ignore; default to 0;
		}
		try {
			id = Long.parseLong(userInfo.get("@id"));
		} catch (Exception ex) {
			// ignore; default to 0;
		}
		// get existing record
		// have user id in clara
		String wrapId = "";
		/**********************/
		/* only used at first time, if not, replace later findWebChartARIAUserByAriaUserCode(lookUpID),findWebChartARIAUserProtocolByAriaUserCodeAndProtocolCode(
		lookUpID, p.getProtocolIdentifier()) .listWebChartClinicalTrialPatientContactsByCID("A"
							+ lookUpID);with wrap id*/
		/*String lookUpID = "";
		if (piSerial > 0) {
			lookUpID = (20000000 + piSerial) + "";
		}*/
		/**********************/
		long wrapIdLong = 0;
		if (id > 0) {
			wrapId = id + "";
			wrapIdLong = id;
		} else if (id == 0 && piSerial > 0) {
			wrapId = (20000000 + piSerial) + "";
			wrapIdLong = 20000000 + piSerial;
		} else {
			return null;
		}
		WebChartARIAUser webChartARIAUser = new WebChartARIAUser();
		WebChartARIAUserProtocol webChartARIAUserProtocol = new WebChartARIAUserProtocol();
		// if there exists webChartARIAUserin db
		try {
			webChartARIAUser = webChartARIAUserDao
					.findWebChartARIAUserByAriaUserCode(wrapId);

			webChartARIAUser.setAriaUserCode(wrapId);
		} catch (Exception e) {
			// do nothing
		}
		// if there exists webChartARIAUserProtocol in db
		try {
			webChartARIAUserProtocol = webChartARIAUserProtocolDao
					.findWebChartARIAUserProtocolByAriaUserCodeAndProtocolCode(
							wrapId, p.getProtocolIdentifier());
			webChartARIAUserProtocol.setAriaUserCode(wrapId);

		} catch (Exception e) {
			// do nothing
		}

		String email = "";
		String firstName = "";
		String lastName = "";
		String phone = "";

		if (wrapIdLong < 20000000) {
			try {
				User user = userDao.findById(wrapIdLong);

				Person person = user.getPerson();

				email = person.getEmail();
				firstName = person.getFirstname();
				lastName = person.getLastname();
				phone = person.getWorkphone();
			} catch (Exception ex) {
				logger.error("CANNOT FIND CLARA User by userId:" + wrapIdLong,
						ex);
				return null;
			}
		} else if (wrapIdLong > 20000000) {

			try {
				AriaUser ariaUser = ariaUserDao
						.findARIAUserByPiSerial(wrapIdLong - 20000000);

				email = ariaUser.getPrimemail();
				firstName = ariaUser.getFirst();
				lastName = ariaUser.getLname();
				phone = ariaUser.getPrimphone();
			} catch (Exception ex) {
				logger.error(
						"CANNOT FIND ARIA User by pi_serial:" + wrapIdLong, ex);
				return null;
			}
		}

		webChartARIAUser.setEmailAddress(email);
		webChartARIAUser.setPhoneNumber(phone);
		String currentDateStr = DateFormat.getInstance().format(new Date());
		webChartARIAUser.setRecordDAte(currentDateStr);
		webChartARIAUser.setName(firstName + " " + lastName);

		if (webChartARIAUser.getAriaUserCode() != null) {

			webChartARIAUser = webChartARIAUserDao.update(webChartARIAUser);
		} else {
			webChartARIAUser.setAriaUserCode(wrapId);
			webChartARIAUser = webChartARIAUserDao.insert(webChartARIAUser);
		}

		webChartARIAUserProtocol.setAriaProtocolCode(p.getProtocolIdentifier());
		webChartARIAUserProtocol.setRecordDate(currentDateStr);

		if (webChartARIAUserProtocol.getAriaUserCode() != null) {

			webChartARIAUserProtocol = webChartARIAUserProtocolDao
					.update(webChartARIAUserProtocol);
		} else {
			webChartARIAUserProtocol.setAriaUserCode(wrapId);
			webChartARIAUserProtocol = webChartARIAUserProtocolDao
					.insert(webChartARIAUserProtocol);
		}

		// if there exists WebChartClinicalTrialPatientContact in db
		/*try {
			List<WebChartClinicalTrialPatientContact> webChartClinicalTrialPatientContacts = webChartClinicalTrialPatientContactDao
					.listWebChartClinicalTrialPatientContactsByCID("A"
							+ wrapId);
			for (WebChartClinicalTrialPatientContact webChartClinicalTrialPatientContact : webChartClinicalTrialPatientContacts) {
				webChartClinicalTrialPatientContact.setCid("A" + wrapId);

				webChartClinicalTrialPatientContactDao
						.saveOrUpdate(webChartClinicalTrialPatientContact);
			}
		} catch (Exception e) {
			// do nothing
		}*/

		return webChartARIAUser;
	}

	private void updateWebChartARIAUserAndWebChartARIAUserProtocol(Protocol p) {
		// create list for webchartARIAuser
		// List<WebChartARIAUser> webChartARIAUserList = Lists.newArrayList();

		try {
			XmlHandler xmlHandler = XmlHandlerFactory.newXmlHandler();
			List<Map<String, String>> results = xmlHandler
					.getListOfMappedElementValues(p.getMetaDataXml(),
							"/protocol/staffs/staff/user", userInfoXPaths);
			
			for (Map<String, String> userInfo : results) {
				findOrCreateWebChartARIAUserANDWebChartARIAUserProtocol(
						userInfo, p);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// not just the one in webChartARIAProtocol, all associated data
	private void deleteWebChartARIAProtocol(
			WebChartARIAProtocol webChartARIAProtocol) {

		webChartARIAProtocolDao.delete(webChartARIAProtocol);
		List<WebChartARIAUserProtocol> webChartARIAUserProtocols = Lists
				.newArrayList();
		webChartARIAUserProtocols = webChartARIAUserProtocolDao
				.ListUserProtocolByProtocolNumber(webChartARIAProtocol
						.getProtocolCode());
		if (webChartARIAUserProtocols != null) {
			for (WebChartARIAUserProtocol webChartARIAUserProtocol : webChartARIAUserProtocols) {
				webChartARIAUserProtocolDao.delete(webChartARIAUserProtocol);
			}
		}
	}

	public void updateWebChartIntegration() {
		List<Protocol> protocols = protocolDao.listAllProtocols();
		List<Protocol> draftprotocols = protocolDao.listProtocolswithLatestStatus(ProtocolStatusEnum.DRAFT);
		protocols.removeAll(draftprotocols);
		/*
		 * List<Protocol> protocols = Lists.newArrayList();
		 * protocols.add(protocolDao.findById(49925));
		 */
		
		// this function only runs on first time, if not first time, also
		// needs to change lookupcode in
		// findOrCreateWebChartARIAUserANDWebChartARIAUserProtocol
		//addPreFixtoAriaUserID();

		//this function used if it is not first time run
		//do not run this... it shoud from aria
		//updateNewAddedPatientContact();
		
		for (Protocol p : protocols) {
			WebChartARIAProtocol webChartARIAProtocol = saveOrUpdateWebChartARIAProtocol(p);
			if (webChartARIAProtocol == null) {
				// retired protocol
				continue;
			}
			updateWebChartARIAUserAndWebChartARIAUserProtocol(p);

		}
	}

	public WebChartARIAUserDao getWebChartARIAUserDao() {
		return webChartARIAUserDao;
	}

	@Autowired(required = true)
	public void setWebChartARIAUserDao(WebChartARIAUserDao webChartARIAUserDao) {
		this.webChartARIAUserDao = webChartARIAUserDao;
	}

	public ProtocolFormDao getProtocolFormDao() {
		return protocolFormDao;
	}

	@Autowired(required = true)
	public void setProtocolFormDao(ProtocolFormDao protocolFormDao) {
		this.protocolFormDao = protocolFormDao;
	}

	public ProtocolDao getProtocolDao() {
		return protocolDao;
	}

	@Autowired(required = true)
	public void setProtocolDao(ProtocolDao protocolDao) {
		this.protocolDao = protocolDao;
	}

	public WebChartARIAProtocolDao getWebChartARIAProtocolDao() {
		return webChartARIAProtocolDao;
	}

	@Autowired(required = true)
	public void setWebChartARIAProtocolDao(
			WebChartARIAProtocolDao webChartARIAProtocolDao) {
		this.webChartARIAProtocolDao = webChartARIAProtocolDao;
	}

	public UserDao getUserDao() {
		return userDao;
	}

	@Autowired(required = true)
	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}

	public AriaUserDao getAriaUserDao() {
		return ariaUserDao;
	}

	@Autowired(required = true)
	public void setAriaUserDao(AriaUserDao ariaUserDao) {
		this.ariaUserDao = ariaUserDao;
	}

	public WebChartARIAUserProtocolDao getWebChartARIAUserProtocolDao() {
		return webChartARIAUserProtocolDao;
	}

	@Autowired(required = true)
	public void setWebChartARIAUserProtocolDao(
			WebChartARIAUserProtocolDao webChartARIAUserProtocolDao) {
		this.webChartARIAUserProtocolDao = webChartARIAUserProtocolDao;
	}

	public WebChartClinicalTrialPatientContactDao getWebChartClinicalTrialPatientContactDao() {
		return webChartClinicalTrialPatientContactDao;
	}

	@Autowired(required = true)
	public void setWebChartClinicalTrialPatientContactDao(
			WebChartClinicalTrialPatientContactDao webChartClinicalTrialPatientContactDao) {
		this.webChartClinicalTrialPatientContactDao = webChartClinicalTrialPatientContactDao;
	}

}
