package edu.uams.clara.integration.outgoing.ctms;

import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.sql.DataSource;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import edu.uams.clara.core.util.xml.XmlHandler;
import edu.uams.clara.core.util.xml.XmlHandlerFactory;
import edu.uams.clara.integration.incoming.crimson.dao.ARIAUserDao;
import edu.uams.clara.integration.incoming.crimson.domain.ARIAUser;
import edu.uams.clara.integration.outgoing.ctms.dao.ClaraFundingDao;
import edu.uams.clara.integration.outgoing.ctms.dao.ClaraProtocolDao;
import edu.uams.clara.integration.outgoing.ctms.dao.ClaraProtocolDiseaseDao;
import edu.uams.clara.integration.outgoing.ctms.dao.ClaraProtocolDrugDao;
import edu.uams.clara.integration.outgoing.ctms.dao.ClaraProtocolUserDao;
import edu.uams.clara.integration.outgoing.ctms.dao.ClaraUserDao;
import edu.uams.clara.integration.outgoing.ctms.domain.ClaraBudget;
import edu.uams.clara.integration.outgoing.ctms.domain.ClaraFunding;
import edu.uams.clara.integration.outgoing.ctms.domain.ClaraProtocol;
import edu.uams.clara.integration.outgoing.ctms.domain.ClaraProtocolDisease;
import edu.uams.clara.integration.outgoing.ctms.domain.ClaraProtocolDrug;
import edu.uams.clara.integration.outgoing.ctms.domain.ClaraProtocolUser;
import edu.uams.clara.integration.outgoing.ctms.domain.ClaraUser;
import edu.uams.clara.integration.outgoing.ctms.domain.ClaraUser.UserType;
import edu.uams.clara.webapp.common.dao.usercontext.UserDao;
import edu.uams.clara.webapp.common.domain.usercontext.Person;
import edu.uams.clara.webapp.common.domain.usercontext.User;
import edu.uams.clara.webapp.common.util.DateFormatUtil;
import edu.uams.clara.webapp.protocol.dao.ProtocolDao;
import edu.uams.clara.webapp.protocol.dao.businesslogicobject.ProtocolFormStatusDao;
import edu.uams.clara.webapp.protocol.dao.protocolform.ProtocolFormDao;
import edu.uams.clara.webapp.protocol.dao.protocolform.ProtocolFormXmlDataDao;
import edu.uams.clara.webapp.protocol.domain.Protocol;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.ProtocolFormStatus;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.enums.ProtocolFormStatusEnum;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolForm;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolFormXmlData;
import edu.uams.clara.webapp.protocol.domain.protocolform.enums.ProtocolFormType;
import edu.uams.clara.webapp.protocol.domain.protocolform.enums.ProtocolFormXmlDataType;
import edu.uams.clara.webapp.xml.processor.XmlProcessor;

@Service
public class ExportProtocolsToCTMSService {
	private final static Logger logger = LoggerFactory
			.getLogger(ExportProtocolsToCTMSService.class);

	private EntityManager entityManager;

	private UserDao userDao;

	private ClaraProtocolDao claraProtocolDao;
	private ClaraUserDao claraUserDao;
	private ClaraProtocolUserDao claraProtocolUserDao;
	private ClaraProtocolDrugDao claraProtocolDrugDao;

	private ProtocolDao protocolDao;

	private ProtocolFormDao protocolFormDao;

	private ProtocolFormXmlDataDao protocolFormXmlDataDao;

	private ProtocolFormStatusDao protocolFormStatusDao;

	private ARIAUserDao ariaUserDao;

	private XmlProcessor xmlProcessor;

	private ClaraFundingDao claraFundingDao;

	private ClaraProtocolDiseaseDao claraProtocolDiseaseDao;
	
	private DataSource dataSource;

	private static Set<String> protocolDataXPaths = Sets.newHashSet();
	{
		protocolDataXPaths.add("/protocol/title");
		protocolDataXPaths.add("/protocol/status");
		protocolDataXPaths.add("/protocol/accural-goal-local");
		protocolDataXPaths.add("/protocol/original-study/submit-date");
		protocolDataXPaths.add("/protocol/original-study/approval-date");
		protocolDataXPaths.add("/protocol/most-recent-study/approval-date");
		protocolDataXPaths.add("/protocol/most-recent-study/approval-end-date");
		protocolDataXPaths.add("/protocol/lay-summary");
		protocolDataXPaths.add("/protocol/inclusion-criteria");
		protocolDataXPaths.add("/protocol/exclusion-criteria");
		protocolDataXPaths
				.add("/protocol/summary/hospital-service-determinations/insurance-plan-code");
		protocolDataXPaths
				.add("/protocol/summary/budget-determination/approval-date");
		protocolDataXPaths
				.add("/protocol/extra/prmc-related-or-not");
	}
	
	private static Set<ProtocolFormStatusEnum> approvedFormStatuses = Sets.newHashSet();
	{
		approvedFormStatuses.add(ProtocolFormStatusEnum.IRB_APPROVED);
		approvedFormStatuses.add(ProtocolFormStatusEnum.EXEMPT_APPROVED);
		approvedFormStatuses.add(ProtocolFormStatusEnum.EXPEDITED_APPROVED);
	}

	// not just the one in ClaraProtocol, all associated data in
	// ClaraProtocolDrug, and ClaraProtocolUser
	private void deleteClaraProtocol(ClaraProtocol claraProtocol) {

		// get rid of user on the protocol
		claraProtocolUserDao.deleteByRefId("clara_protocol_id",
				claraProtocol.getId());

		// get rid of drugs on the protocol
		claraProtocolDrugDao.deleteByRefId("clara_protocol_id",
				claraProtocol.getId());

		// get rid of disease on the protocol
		claraProtocolDiseaseDao.deleteByRefId("clara_protocol_id",
				claraProtocol.getId());

		claraProtocolDao.delete(claraProtocol);
	}

	// one study multipe phases...
	private String getPhases(Protocol p) {
		try {
			XmlHandler xmlHandler = XmlHandlerFactory.newXmlHandler();
			List<String> phases = xmlHandler.getStringValuesByXPath(
					p.getMetaDataXml(), "/protocol/phases/phase");
			Joiner joiner = Joiner.on(", ").skipNulls();

			return joiner.join(phases);
		} catch (Exception ex) {
			// don't care
			return "";
		}
	}

	private ClaraProtocol saveOrUpdateClaraProtocol(Protocol protocol) {

		ClaraProtocol claraProtocol = null;

		try {
			try {
				claraProtocol = claraProtocolDao.findByIRBNumber(Long
						.toString(protocol.getId()));
				logger.debug("found claraProtocolId: " + claraProtocol.getId());
			} catch (Exception ex) {
				// don't care create new
			}

			// delete
			if (claraProtocol != null && protocol.isRetired()) {
				logger.debug("retire irbNumber: "
						+ claraProtocol.getIrbNumber());
				deleteClaraProtocol(claraProtocol);
				return null;
			}

			// not adding reitred ones in...
			if (protocol.isRetired()) {
				logger.debug("doesn't exist in ClaraProtocol, so just go on...");
				return null;
			}

			XmlHandler xmlHandler = XmlHandlerFactory.newXmlHandler();

			Map<String, String> protocolData = xmlHandler
					.getFirstStringValuesByXPaths(protocol.getMetaDataXml(),
							protocolDataXPaths);
			protocolData.put("irbNumber", "" + protocol.getId());

			if (claraProtocol == null) {
				claraProtocol = new ClaraProtocol.Builder()
						.values(protocolData).build();
			} else {
				claraProtocol = new ClaraProtocol.Builder(claraProtocol.getId())
						.values(protocolData).build();
			}

			ProtocolForm newSubmissionForm = null;

			try {
				newSubmissionForm = protocolFormDao
						.getLatestProtocolFormByProtocolIdAndProtocolFormType(
								protocol.getId(),
								ProtocolFormType.NEW_SUBMISSION);
			} catch (Exception e) {
				// don't care
			}

			boolean budgetReviewAssigned = false;

			if (newSubmissionForm != null) {
				try {
					ProtocolFormStatus protocolFormStatus = protocolFormStatusDao
							.getProtocolFormStatusByFormIdAndProtocolFormStatus(
									newSubmissionForm.getId(),
									ProtocolFormStatusEnum.UNDER_BUDGET_REVIEW);

					if (protocolFormStatus != null) {
						budgetReviewAssigned = true;
					}

				} catch (Exception e) {
					logger.warn("No budget review yet!");
					budgetReviewAssigned = false;
				}
			}
			
			/*
			ProtocolFormXmlData protocolformXmlData = null;
			try {
				protocolformXmlData = protocolFormXmlDataDao
						.getLastProtocolFormXmlDataByProtocolIdAndType(
								protocol.getId(),
								ProtocolFormXmlDataType.MODIFICATION);
			} catch (Exception e) {
				try {
					protocolformXmlData = protocolFormXmlDataDao
							.getLastProtocolFormXmlDataByProtocolIdAndType(
									protocol.getId(),
									ProtocolFormXmlDataType.PROTOCOL);
				} catch (Exception ex) {
					protocolformXmlData = null;
				}
			}

			if (protocolformXmlData != null) {
				String cancerRelated = "";
				String pfxdXmlData = protocolformXmlData.getXmlData();
				try {
					cancerRelated = xmlProcessor.listElementStringValuesByPath(
							"/protocol/misc/is-cancer-study", pfxdXmlData).get(
							0);
				} catch (Exception e) {
				}
				if (cancerRelated.equals("y")) {
					cancerRelated = "yes";
				} else if (cancerRelated.equals("n")) {
					cancerRelated = "no";
				}
				claraProtocol.setCancerRelated(cancerRelated);
			}
			*/

			String studyType = "";
			try {
				studyType = xmlProcessor.listElementStringValuesByPath(
						"/protocol/study-type", protocol.getMetaDataXml()).get(
						0);
			} catch (Exception e) {

			}

			String cancerRelated = "";
			
			cancerRelated = protocolData
					.get("/protocol/extra/prmc-related-or-not");
			
			if (cancerRelated.equals("y")) {
				cancerRelated = "yes";
			} else if (cancerRelated.equals("n")) {
				cancerRelated = "no";
			}
			
			claraProtocol.setClaraCreatedDate(DateFormatUtil
					.formateDateToMDY(protocol.getCreated()));
			// claraProtocol.setMetaDataXml(protocol.getMetaDataXml());
			claraProtocol.setIrbSubmittedDate(protocolData
					.get("/protocol/original-study/submit-date"));
			claraProtocol.setIrbApprovalDate(protocolData
					.get("/protocol/original-study/approval-date"));
			claraProtocol.setLastCRApprovalDate(protocolData
					.get("/protocol/most-recent-study/approval-date"));
			claraProtocol.setNextCRApprovalDate(protocolData
					.get("/protocol/most-recent-study/approval-end-date"));
			claraProtocol.setIrbStatus(protocolData.get("/protocol/status"));
			claraProtocol.setLocalAccuralGaol(protocolData
					.get("/protocol/accural-goal-local"));
			claraProtocol.setTitle(protocolData.get("/protocol/title"));
			claraProtocol.setLaySummary(protocolData
					.get("/protocol/lay-summary"));
			claraProtocol.setInclusionCriteria(protocolData
					.get("/protocol/inclusion-criteria"));
			claraProtocol.setExclusionCriteria(protocolData
					.get("/protocol/exclusion-criteria"));
			claraProtocol
					.setBudgetApprovalDate(protocolData
							.get("/protocol/summary/budget-determination/approval-date"));
			claraProtocol.setBudgetReviewAssigned(budgetReviewAssigned);
			claraProtocol.setStudyType(studyType);
			claraProtocol.setCancerRelated(cancerRelated);

			/*try{
				ProtocolFormXmlData pfxd = protocolFormXmlDataDao.getLastProtocolFormXmlDataByProtocolIdAndType(protocol.getId(), ProtocolFormXmlDataType.ARCHIVE);
				claraProtocol.setAriaXml(pfxd.getXmlData());
			}catch(Exception e){
				//skip
			}*/
			
			String phases = getPhases(protocol);

			if (!phases.isEmpty()) {
				logger.debug("phases: " + phases);
			}
			claraProtocol.setPhases(phases);

			claraProtocol = claraProtocolDao.saveOrUpdate(claraProtocol);

		} catch (ParserConfigurationException | InstantiationException
				| IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return claraProtocol;
	}

	/**
	 * Check whether the user is a clara user or an old aria users we didn't
	 * create the account Find whether we have this user in remote database If
	 * so, update, if not create; Batch Job, nightly, we don't care how long
	 * it's not gonna take as long as it's forever
	 * 
	 * @param userInfo
	 * @return
	 */
	private ClaraUser findOrCreateClaraUser(Map<String, String> userInfo) {
		long piSerial = 0;
		try {
			piSerial = Long.parseLong(userInfo.get("@pi_serial"));
		} catch (Exception ex) {
			// ignore; default to 0;
		}
		long id = 0;
		try {
			id = Long.parseLong(userInfo.get("@id"));
		} catch (Exception ex) {
			// ignore; default to 0;
		}

		ClaraUser claraUser = new ClaraUser();

		if (id > 0) {
			claraUser.setUserType(UserType.CLARA);
			claraUser.setUserId(id);
		} else if (id == 0 && piSerial > 0) {
			claraUser.setUserType(UserType.ARIA);
			claraUser.setUserId(piSerial);
		} else {
			return null;
		}

		try {
			// if it exists in db, don't need to deal with this
			// ClaraUser
			claraUser = claraUserDao.findClaraUserByUserIdAndType(
					claraUser.getUserId(), claraUser.getUserType());
		} catch (Exception ex) {
			// not found
			// logger.debug("cannot find this user in db: " +
			// claraUser.getUserId());
			ex.printStackTrace();
		}

		String email = "";
		String firstName = "";
		String lastName = "";
		String middleName = "";
		String phone = "";
		String userName = "";
		String sapid = "";

		if (claraUser.getUserType().equals(UserType.CLARA)) {
			try {
				User user = userDao.findById(claraUser.getUserId());

				Person person = user.getPerson();

				email = person.getEmail();
				firstName = person.getFirstname();
				lastName = person.getLastname();
				middleName = person.getMiddlename();
				phone = person.getWorkphone();
				userName = user.getUsername();
				sapid = person.getSap();
			} catch (Exception ex) {
				logger.error(
						"CANNOT FIND CLARA User by userId:"
								+ claraUser.getUserId(), ex);
				return null;
			}
		} else if (claraUser.getUserType().equals(UserType.ARIA)) {

			try {
				ARIAUser ariaUser = ariaUserDao
						.findARIAUserByPiSerial(claraUser.getUserId());

				email = ariaUser.getPrimaryEmail();
				firstName = ariaUser.getFirstname();
				lastName = ariaUser.getLastname();
				middleName = ariaUser.getMiddleInitial();
				phone = ariaUser.getPrimaryPhone();
				userName = ariaUser.getUsername();
				sapid = ariaUser.getSapid();
			} catch (Exception ex) {
				logger.error(
						"CANNOT FIND ARIA User by pi_serial:"
								+ claraUser.getUserId(), ex);
				return null;
			}
		}

		claraUser.setEmail(email);
		claraUser.setFirstName(firstName);
		claraUser.setLastName(lastName);
		claraUser.setMiddleName(middleName);
		claraUser.setPhone(phone);
		claraUser.setUserName(userName);
		
		if(sapid!=null && !sapid.isEmpty()){
			sapid = ""+Integer.valueOf(sapid);
		}
		claraUser.setSapid(sapid);

		if (claraUser.getId() > 0) {

			claraUser = claraUserDao.update(claraUser);
			// logger.debug("updated ClaraUser: " + claraUser.getUserType() +
			// "; "
			// + claraUser.getUserId());
		} else {
			claraUser = claraUserDao.insert(claraUser);
			// logger.debug("created ClaraUser: " + claraUser.getUserType() +
			// "; "
			// + claraUser.getUserId());
		}

		return claraUser;

	}

	// relative to /protocol/staffs/staff/user
	private Set<String> userInfoXPaths = Sets.newHashSet();
	{
		userInfoXPaths.add("@id");
		userInfoXPaths.add("@pi_serial");
		// userInfoXPaths.add("./roles/role");// only consider the first role
		userInfoXPaths.add("./email");
	}

	private ClaraProtocolUser saveOrUpdateClaraProtocolUser(
			ClaraProtocolUser claraProtocolUser) {
		
		/* we decide to delete all existing clara protocol users and add with current staffs in CLARA
		ClaraProtocolUser fromDB = null;
		try {
			fromDB = claraProtocolUserDao.findByClaraProtocolIdAndClaraUserId(
					claraProtocolUser.getClaraProtocolId(),
					claraProtocolUser.getClaraUserId(),
					claraProtocolUser.getClaraProtocolUserRole());
		} catch (Exception ex) {
			// don't care...
		}

		// update db.
		if (fromDB != null) {
			claraProtocolUser.setId(fromDB.getId());
		}
		*/
		
		claraProtocolUser = claraProtocolUserDao
				.saveOrUpdate(claraProtocolUser);
		// logger.debug("add " + claraProtocolUser.getClaraUserId()
		// + " to protocol: [" + claraProtocolUser.getClaraProtocolId()
		// + "] with [" + claraProtocolUser.getClaraProtocolUserRole()
		// + "] role");
		return claraProtocolUser;

	}

	// updates the ClaraUser table ..
	private void updateClaraUsersOnProtocol(Protocol p,
			ClaraProtocol claraProtocol) {

		// create a list of ClaraUsers (also update the ClaraUser table).
		//List<ClaraProtocolUser> claraProtocolUsers = Lists.newArrayList();
		try {
			XmlHandler xmlHandler = XmlHandlerFactory.newXmlHandler();
			// get all user elements
			List<String> starffIDs = new ArrayList<String>();

			try {
				starffIDs = xmlProcessor
						.getAttributeValuesByPathAndAttributeName(
								"/protocol/staffs/staff", p.getMetaDataXml(),
								"id");
			} catch (XPathExpressionException | SAXException | IOException e1) {
				e1.printStackTrace();
			}
			
			List<ClaraProtocolUser> existingClaraProtocolUsers = claraProtocolUserDao
					.listByClaraProtocolId(claraProtocol.getId());
			
			if (existingClaraProtocolUsers != null && existingClaraProtocolUsers.size() > 0) {
				for (ClaraProtocolUser existingUser : existingClaraProtocolUsers) {
					claraProtocolUserDao.delete(existingUser);
				}
			}

			for (String staffID : starffIDs) {
				// for user with multiple roles
				List<String> roles = new ArrayList<String>();
				try {
					roles = xmlProcessor
							.listElementStringValuesByPath(
									"/protocol/staffs/staff[@id=\"" + staffID
											+ "\"]/user/roles/role",
									p.getMetaDataXml());
				} catch (XPathExpressionException | SAXException | IOException e) {
					e.printStackTrace();
				}

				String userID = xmlProcessor
						.getAttributeValueByPathAndAttributeName(
								"/protocol/staffs/staff[@id=\"" + staffID
										+ "\"]/user", p.getMetaDataXml(), "id");
				String userPiSerial = xmlProcessor
						.getAttributeValueByPathAndAttributeName(
								"/protocol/staffs/staff[@id=\"" + staffID
										+ "\"]/user", p.getMetaDataXml(),
								"pi_serial");
				String userEmail = xmlHandler.getSingleStringValueByXPath(
						p.getMetaDataXml(), "/protocol/staffs/staff[@id=\""
								+ staffID + "\"]/user/email");

				if (roles.size() > 0) {
					for (String role : roles) {
						Map<String, String> userInfo = new HashMap<String, String>();
						userInfo.put("./roles/role", role);
						userInfo.put("@id", userID);
						userInfo.put("@pi_serial", userPiSerial);
						userInfo.put("./email", userEmail);

						ClaraUser claraUser = findOrCreateClaraUser(userInfo);
						if (claraUser == null) {
							// logger.error("NEITHER PI SERIAL NOR ID... THIS SHOULDN'T HAPPEN, CHECK XML FOR PROTOCOID: "
							// + p.getId());
							this.corruptedUserList.add(userInfo.get("./email")
									+ " on protocol.id: " + p.getId());
						} else {
							ClaraProtocolUser claraProtocolUser = new ClaraProtocolUser();
							claraProtocolUser.setClaraProtocolId(claraProtocol
									.getId());
							claraProtocolUser.setClaraUserId(claraUser.getId());
							claraProtocolUser.setClaraProtocolUserRole(userInfo
									.get("./roles/role"));

							claraProtocolUser = saveOrUpdateClaraProtocolUser(claraProtocolUser);
							//claraProtocolUsers.add(claraProtocolUser);

						}
					}
				}

			}

			/*
			 * 
			 * List<Map<String, String>> results = xmlHandler
			 * .getListOfMappedElementValues(p.getMetaDataXml(),
			 * "/protocol/staffs/staff/user", userInfoXPaths);
			 * 
			 * for (Map<String, String> userInfo : results) {
			 * 
			 * ClaraUser claraUser = findOrCreateClaraUser(userInfo); if
			 * (claraUser == null) { // logger.error(
			 * "NEITHER PI SERIAL NOR ID... THIS SHOULDN'T HAPPEN, CHECK XML FOR PROTOCOID: "
			 * // + p.getId());
			 * this.corruptedUserList.add(userInfo.get("./email") +
			 * " on protocol.id: " + p.getId()); } else { ClaraProtocolUser
			 * claraProtocolUser = new ClaraProtocolUser();
			 * claraProtocolUser.setClaraProtocolId(claraProtocol.getId());
			 * claraProtocolUser.setClaraUserId(claraUser.getId());
			 * claraProtocolUser.setClaraProtocolUserRole(userInfo
			 * .get("./roles/role"));
			 * 
			 * claraProtocolUser =
			 * saveOrUpdateClaraProtocolUser(claraProtocolUser);
			 * claraProtocolUsers.add(claraProtocolUser);
			 * 
			 * } }
			 */
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		/*
		 * if (claraProtocolUsers.size() == 0) {
		 * logger.error("no staff on this protocol?" + p.getId() + "; xml: " +
		 * p.getMetaDataXml()); }
		 */
		//deteleRetiredClaraProtocolUsers(claraProtocol, claraProtocolUsers);
	}

	// remove if it's not listed as a staff anymore
	// @TODO we are only picking up the first role, so ClaraUserId is unique...
	// otherwise, we are in trouble...
	public void deteleRetiredClaraProtocolUsers(ClaraProtocol claraProtocol,
			List<ClaraProtocolUser> claraProtocolUsers) {
		List<ClaraProtocolUser> existingClaraProtocolUsers = claraProtocolUserDao
				.listByClaraProtocolId(claraProtocol.getId());

		Map<Long, ClaraProtocolUser> keyedExistingClaraProtocolUsers = Maps
				.uniqueIndex(existingClaraProtocolUsers,
						new Function<ClaraProtocolUser, Long>() {

							@Override
							public Long apply(
									ClaraProtocolUser claraProtocolUser) {
								return claraProtocolUser.getClaraUserId();
							}
						});

		Set<Long> existing = keyedExistingClaraProtocolUsers.keySet();

		// Map<Long, ClaraProtocolUser> keyedIncomingClaraUsers = Maps
		// .uniqueIndex(claraProtocolUsers,
		// new Function<ClaraProtocolUser, Long>() {
		//
		// @Override
		// public Long apply(
		// ClaraProtocolUser claraProtocolUser) {
		// return claraProtocolUser.getClaraUserId();
		// }
		// });
		/***
		 * Duplicate key may exists as staff can be added to the same study
		 * multiple times..
		 */
		Map<Long, ClaraProtocolUser> keyedIncomingClaraUsers = Maps
				.newHashMap();
		for (ClaraProtocolUser claraProtocolUser : claraProtocolUsers) {
			if (keyedIncomingClaraUsers.containsKey(claraProtocolUser
					.getClaraUserId())) {
				continue;
			}
			keyedIncomingClaraUsers.put(claraProtocolUser.getClaraUserId(),
					claraProtocolUser);
		}

		Set<Long> incoming = keyedIncomingClaraUsers.keySet();

		Set<Long> toDels = Sets.difference(existing, incoming);

		// logger.debug("existing: " + existing);
		// logger.debug("incoming: " + incoming);
		// logger.debug("toDels: " + toDels);
		// delete first...
		for (Long toDel : toDels) {
			claraProtocolUserDao.delete(keyedExistingClaraProtocolUsers
					.get(toDel));
		}
	}

	// relative to /protocol/drugs/drug
	private Set<String> drugInfoXPaths = Sets.newHashSet();
	{
		drugInfoXPaths.add("@id");
		drugInfoXPaths.add("@name");
	}

	// update the claraprotocolDisease

	private void updateClaraDiseasesOnProtocol(Protocol p,
			ClaraProtocol claraProtocol) {

		claraProtocolDiseaseDao.deleteByRefId("clara_protocol_id",
				claraProtocol.getId());
		try {
			Set<String> diseasePath = Sets.newHashSet();
			diseasePath.add("/protocol/diseases/disease");

			List<Element> diseases = xmlProcessor.listDomElementsByPaths(
					diseasePath, p.getMetaDataXml());
			logger.debug("found diseases: " + diseases.size());

			for (Element disease : diseases) {
				ClaraProtocolDisease claraProtocolDisease = new ClaraProtocolDisease();
				claraProtocolDisease.setClaraProtocolId(claraProtocol.getId());
				claraProtocolDisease
						.setDoID(disease.getAttribute("externalid"));
				claraProtocolDisease.setDescription(disease
						.getAttribute("text"));

				claraProtocolDisease = claraProtocolDiseaseDao
						.saveOrUpdate(claraProtocolDisease);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// updates the ClaraUser table ..
	private void updateClaraDrugsOnProtocol(Protocol p,
			ClaraProtocol claraProtocol) {

		claraProtocolDrugDao.deleteByRefId("clara_protocol_id",
				claraProtocol.getId());

		try {
			XmlHandler xmlHandler = XmlHandlerFactory.newXmlHandler();
			List<Map<String, String>> results = xmlHandler
					.getListOfMappedElementValues(p.getMetaDataXml(),
							"/protocol/drugs/drug", drugInfoXPaths);
			logger.debug("found drugs: " + results.size());

			for (Map<String, String> drugInfo : results) {

				ClaraProtocolDrug claraProtocolDrug = new ClaraProtocolDrug();
				claraProtocolDrug.setClaraProtocolId(claraProtocol.getId());
				claraProtocolDrug.setDrugId(drugInfo.get("@id"));
				claraProtocolDrug.setDrugName(drugInfo.get("@name"));

				claraProtocolDrug = claraProtocolDrugDao
						.saveOrUpdate(claraProtocolDrug);

			}
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private Set<String> corruptedUserList = Sets.newHashSet();

	public void updateCTMSIntegration(String type) throws Exception {
		
		List<Protocol> protocols = null;
		
		switch (type) {
		case "NOT_CLOSED":
			protocols = protocolDao.listNotClosedProtocol();
			break;
		case "CLOSED":
			protocols = protocolDao.listClosedProtocol();
			break;
		}
		// List<Protocol> protocols = protocolDao.listProtocolsByIdRange(0,
		// 1000);
		for (Protocol p : protocols) {

			try {
				ClaraProtocol claraProtocol = saveOrUpdateClaraProtocol(p);
				if (claraProtocol == null) { // retired protocol
					continue;
				}
				logger.debug("claraProtocolId: " + claraProtocol.getId());
				updateClaraUsersOnProtocol(p, claraProtocol);
				updateClaraDrugsOnProtocol(p, claraProtocol);
				updateClaraDiseasesOnProtocol(p, claraProtocol);
				updateCTMSFundingInfo(p);
				updateCMTSClaraBudgetInfo(p,claraProtocol);
			} catch (Exception ex) {
				ex.printStackTrace();
			}

		}

		Joiner joiner = Joiner.on("\r\n").skipNulls();

		logger.error("users missing both pi_serial and id: \r\n"
				+ joiner.join(corruptedUserList));
	}
	
	private void insertBudget(ClaraBudget claraBudget) {
		
		try {
			 NamedParameterJdbcTemplate jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
			 Map<String,String> namedParameters = Maps.newHashMap();
			 namedParameters.put("claraProtocolId", claraBudget.getClaraProtocolId()+"");
			 namedParameters.put("createdDate", claraBudget.getBudgetCreatedDate());
			 namedParameters.put("approvedDate", claraBudget.getBudgetApprovalDate());
			 namedParameters.put("xml", claraBudget.getXmlData());
			 
			 String sql = "INSERT INTO [ctms_integration].[dbo].[clara_budget] ([clara_protocol_id],[budget_created_date],[budget_approval_date],[xml_data]) VALUES (:claraProtocolId ,:createdDate,:approvedDate,:xml)";
			 jdbcTemplate.update(sql, namedParameters);	 
			 
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void updateCMTSClaraBudgetInfo(Protocol p,ClaraProtocol claraProtocol){
		List<ProtocolFormXmlData> pfxds = protocolFormXmlDataDao.listProtocolformXmlDatasByProtocolIdAndType(p.getId(), ProtocolFormXmlDataType.BUDGET);
		
		if(pfxds.size()>0){
			for(ProtocolFormXmlData pfxd:pfxds){
				try{
					ProtocolFormStatus pfs = null;
					for(ProtocolFormStatusEnum pfse :approvedFormStatuses){
						try{
						pfs = protocolFormStatusDao.getProtocolFormStatusByFormIdAndProtocolFormStatus(pfxd.getProtocolForm().getFormId(), pfse);
						}catch(Exception e){
							
						}
						if(pfs!=null){
							break;
						}
					}
					
					//budget is approved
					if(pfs!=null){
						String xml = pfs.getProtocolForm().getMetaDataXml();
						if(xml ==null ||xml.isEmpty()){
							continue;
						}
						String approvedDate ="";
						String createdDate = DateFormatUtil.formateDateToMDY(pfxd.getCreated());
						XmlHandler xmlHandler = XmlHandlerFactory.newXmlHandler();
						approvedDate=xmlHandler.getSingleStringValueByXPath(xml, "/protocol/summary/budget-determination/approval-date");
						if(p.getId()>200000&&approvedDate.isEmpty()){
							//for non-migrated study, we only dump pi-sign-off studies
							continue;
						}
						ClaraBudget claraBudget = null;
						try {
							 NamedParameterJdbcTemplate jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
							 Map<String,String> namedParameters = Maps.newHashMap();
							 namedParameters.put("claraProtocolId", p.getId()+"");
							 namedParameters.put("createdDate", createdDate);
							 namedParameters.put("approvedDate", approvedDate);
							 
							 String sql = "SELECT * FROM [ctms_integration].[dbo].[clara_budget] WHERE clara_protocol_id = :claraProtocolId AND budget_approval_date = :approvedDate AND budget_created_date =:createdDate";
							 List<Map<String, Object>> results = jdbcTemplate.queryForList(sql, namedParameters);
							 
							 //data exist
							 if(results.size()>0){
								 continue;
							 }else{
								 claraBudget = new ClaraBudget();
							 }
							 
						} catch (Exception e) {
							e.printStackTrace();
							claraBudget = new ClaraBudget();
						}
						claraBudget.setClaraProtocolId(claraProtocol.getId());
						claraBudget.setXmlData(pfxd.getXmlData().replaceAll("'s", "&#39;s"));
						claraBudget.setBudgetCreatedDate(createdDate);
						claraBudget.setBudgetApprovalDate(approvedDate);
						insertBudget(claraBudget);
					}
				}catch(Exception e){
					e.printStackTrace();
				}
				
									
				}
			}
	}

	private void updateCTMSFundingInfo(Protocol p)
			throws XPathExpressionException, SAXException, IOException {
		String xmlData = p.getMetaDataXml();
		try {
			if (!xmlData.isEmpty() && xmlData.contains("funding-source")) {
				Set<String> pathSet = Sets.newHashSet();
				pathSet.add("/protocol/funding/funding-source");
				List<Element> fundingElements = xmlProcessor
						.listDomElementsByPaths(pathSet, p.getMetaDataXml());
				for (Element fundingEle : fundingElements) {
					ClaraFunding claraFunding = new ClaraFunding();
					try {
						claraFunding = claraFundingDao
								.findFundingbyProtocolIDandTypeandSponsor(
										p.getId(),
										fundingEle.getAttribute("type"),
										fundingEle.getAttribute("entityname"));
					} catch (Exception e) {
						claraFunding = new ClaraFunding();
					}
					claraFunding.setIrbnumber(p.getId());
					claraFunding.setSponsorName(fundingEle
							.getAttribute("entityname"));
					claraFunding.setType(fundingEle.getAttribute("type"));
					claraFunding.setExternalID(fundingEle
							.getAttribute("projectid"));
					claraFundingDao.saveOrUpdate(claraFunding);

				}
			}
		} catch (Exception e) {

		}

	}

	public EntityManager getEntityManager() {
		return entityManager;
	}

	@PersistenceContext(unitName = "defaultPersistenceUnit")
	public void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	public UserDao getUserDao() {
		return userDao;
	}

	@Autowired(required = true)
	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}

	public ProtocolDao getProtocolDao() {
		return protocolDao;
	}

	@Autowired(required = true)
	public void setProtocolDao(ProtocolDao protocolDao) {
		this.protocolDao = protocolDao;
	}

	public ARIAUserDao getAriaUserDao() {
		return ariaUserDao;
	}

	@Autowired(required = true)
	public void setAriaUserDao(ARIAUserDao ariaUserDao) {
		this.ariaUserDao = ariaUserDao;
	}

	public ClaraUserDao getClaraUserDao() {
		return claraUserDao;
	}

	@Autowired(required = true)
	public void setClaraUserDao(ClaraUserDao claraUserDao) {
		this.claraUserDao = claraUserDao;
	}

	public ClaraProtocolDao getClaraProtocolDao() {
		return claraProtocolDao;
	}

	@Autowired(required = true)
	public void setClaraProtocolDao(ClaraProtocolDao claraProtocolDao) {
		this.claraProtocolDao = claraProtocolDao;
	}

	public ClaraProtocolUserDao getClaraProtocolUserDao() {
		return claraProtocolUserDao;
	}

	@Autowired(required = true)
	public void setClaraProtocolUserDao(
			ClaraProtocolUserDao claraProtocolUserDao) {
		this.claraProtocolUserDao = claraProtocolUserDao;
	}

	public ClaraProtocolDrugDao getClaraProtocolDrugDao() {
		return claraProtocolDrugDao;
	}

	@Autowired(required = true)
	public void setClaraProtocolDrugDao(
			ClaraProtocolDrugDao claraProtocolDrugDao) {
		this.claraProtocolDrugDao = claraProtocolDrugDao;
	}

	public ProtocolFormDao getProtocolFormDao() {
		return protocolFormDao;
	}

	@Autowired(required = true)
	public void setProtocolFormDao(ProtocolFormDao protocolFormDao) {
		this.protocolFormDao = protocolFormDao;
	}

	public ProtocolFormStatusDao getProtocolFormStatusDao() {
		return protocolFormStatusDao;
	}

	@Autowired(required = true)
	public void setProtocolFormStatusDao(
			ProtocolFormStatusDao protocolFormStatusDao) {
		this.protocolFormStatusDao = protocolFormStatusDao;
	}

	public XmlProcessor getXmlProcessor() {
		return xmlProcessor;
	}

	@Autowired(required = true)
	public void setXmlProcessor(XmlProcessor xmlProcessor) {
		this.xmlProcessor = xmlProcessor;
	}

	public ClaraFundingDao getClaraFundingDao() {
		return claraFundingDao;
	}

	@Autowired(required = true)
	public void setClaraFundingDao(ClaraFundingDao claraFundingDao) {
		this.claraFundingDao = claraFundingDao;
	}

	public ClaraProtocolDiseaseDao getClaraProtocolDiseaseDao() {
		return claraProtocolDiseaseDao;
	}

	@Autowired(required = true)
	public void setClaraProtocolDiseaseDao(
			ClaraProtocolDiseaseDao claraProtocolDiseaseDao) {
		this.claraProtocolDiseaseDao = claraProtocolDiseaseDao;
	}

	public ProtocolFormXmlDataDao getProtocolFormXmlDataDao() {
		return protocolFormXmlDataDao;
	}

	@Autowired(required = true)
	public void setProtocolFormXmlDataDao(
			ProtocolFormXmlDataDao protocolFormXmlDataDao) {
		this.protocolFormXmlDataDao = protocolFormXmlDataDao;
	}

	public DataSource getDataSource() {
		return dataSource;
	}

	@Autowired(required = true)
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

}
