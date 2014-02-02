package edu.uams.clara.webapp.protocol.scheduler;

import java.io.IOException;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.google.common.collect.Sets;

import edu.uams.clara.core.util.xml.XmlHandler;
import edu.uams.clara.core.util.xml.XmlHandlerFactory;
import edu.uams.clara.webapp.common.dao.usercontext.UserDao;
import edu.uams.clara.webapp.common.domain.usercontext.User;
import edu.uams.clara.webapp.common.service.EmailService;
import edu.uams.clara.webapp.protocol.dao.ProtocolDao;
import edu.uams.clara.webapp.protocol.domain.Protocol;
import edu.uams.clara.webapp.protocol.service.email.ProtocolEmailService;
import edu.uams.clara.webapp.xml.processor.XmlProcessor;

@Service
public class SendEpicSecurityTeamNotificationService {
	private ProtocolDao protocolDao;

	private final static Logger logger = LoggerFactory
			.getLogger(SendEpicSecurityTeamNotificationService.class);

	private ProtocolEmailService protocolEmailService;

	private EmailService emailService;

	private XmlProcessor xmlProcessor;

	private UserDao userDao;

	@Value("${application.host}")
	private String applicationHost;
	
	private boolean shouldRun = false;

	private EntityManager em;

	public void run() throws XPathExpressionException, SAXException,
			IOException, ParserConfigurationException {
		// get ids of submitted protocols
		String qry = "select distinct protocol_id from protocol_status where protocol_status not in ('DRAFT','PENDING_PL_ENDORSEMENT','PENDING_PI_ENDORSEMENT','PENDING_TP_ENDORSEMENT') and retired =0";
		Query query = em.createNativeQuery(qry);
		List<BigInteger> pids = (List<BigInteger>) query.getResultList();
		/*List<Long> pids = Lists.newArrayList();
		pids.add(201868l);*/
		String profileTemplate = "<metadata></metadata>";
		
		XmlHandler xmlHandler = XmlHandlerFactory.newXmlHandler();
		
		for (BigInteger pid : pids) {
			try {
				boolean needNotify = false;
				Protocol p = protocolDao.findById(pid.longValue());
				String xmlData = p.getMetaDataXml();
				Set<String> xpathSet = Sets.newHashSet();
				xpathSet.add("/protocol/staffs/staff/user");
				String mailContent = "";
				if (xmlData.contains("<has-budget>yes</has-budget>")) {
					needNotify = true;
				} else if (xmlData
						.contains("<budget-created>y</budget-created>")) {
					needNotify = true;
				}
				// submitted staff has budget
				if (needNotify == true) {
					// get user list of protocol
					List<Element> users = xmlProcessor.listDomElementsByPaths(
							xpathSet, xmlData);
					for (Element userEle : users) {
						String userIdStr = "";
						try {
							userIdStr = userEle.getAttribute("id");
						} catch (Exception e) {
							// do nothing
						}
						// user id does not exist
						if (userIdStr.isEmpty()) {
							continue;
						}

						long userId = Long.valueOf(userIdStr);
						User user = userDao.findById(userId);
						String userProfile = "";
						String notified = "";
						logger.debug(user.getProfile());
						if (user.getProfile() == null) {
							userProfile += profileTemplate;
						} else if (user.getProfile().isEmpty()) {
							userProfile += profileTemplate;
						} else {
							userProfile = user.getProfile();
						}

						if (xmlProcessor.listElementStringValuesByPath(

						"/metadata/is-epic-notified", userProfile).size() > 0) {
							notified = xmlProcessor
									.listElementStringValuesByPath(
											"/metadata/is-epic-notified",
											userProfile).get(0);
						}
						if (notified.equals("y")) {
							// already notified
							continue;
						} else if (notified.isEmpty()) {
							userProfile = xmlProcessor
									.replaceOrAddNodeValueByPath(
											"/metadata/is-epic-notified",
											userProfile, "y");
							user.setProfile(userProfile);
							userDao.saveOrUpdate(user);
							// begin to send
							String userName = xmlHandler
									.getSingleStringValueByXPath(xmlData,
											"/protocol/staffs/staff/user[@id="
													+ userIdStr + "]/firstname")
									+ " , "
									+ xmlHandler.getSingleStringValueByXPath(
											xmlData,
											"/protocol/staffs/staff/user[@id="
													+ userIdStr + "]/lastname");
							List<String> roles = xmlProcessor
									.listElementStringValuesByPath(
											"/protocol/staffs/staff/user[@id="
													+ userIdStr
													+ "]/roles/role", xmlData);
							List<String> responsibilities = xmlProcessor
									.listElementStringValuesByPath(
											"/protocol/staffs/staff/user[@id="
													+ userIdStr
													+ "]/reponsibilities/responsibility",
											xmlData);
							String email = xmlHandler
									.getSingleStringValueByXPath(xmlData,
											"/protocol/staffs/staff/user[@id="
													+ userIdStr + "]/email");

							mailContent += "<b>User Name:</b> " + userName
									+ "<br/>";
							mailContent += "<b>Email:</b> " + email + "<br/>";
							mailContent += "<b>Roles:</b> ";
							int commerCount = 0;
							for (String role : roles) {
								if (commerCount > 0) {
									mailContent += " , " + role;
								} else {
									mailContent += role;
								}
								commerCount++;
							}
							commerCount = 0;
							mailContent += "<br/>"
									+ "<b>Responsibilities:</b> ";
							for (String responsibility : responsibilities) {
								if (commerCount > 0) {
									mailContent += " , " + responsibility;
								} else {
									mailContent += responsibility;
								}
								commerCount++;
							}
							mailContent += "<br/><br/>";

						}

					}
				}
				// if have content to send
				if (!mailContent.isEmpty()) {
					sendSecurityTeamNotification(p, mailContent);
				}
			} catch (Exception e) {
				logger.debug("error protocol: "+pid.longValue());
			}
		}
	}

	private void sendSecurityTeamNotification(Protocol p, String mailContent) {
		// If run on test, comment out this part.
		
		if(!this.isShouldRun()) return;

		try {
			Map<String, Object> userInfoMap = new HashMap<String, Object>();
			userInfoMap.put("itSecurityUserinfo", mailContent);
			protocolEmailService.sendProtocolNotification(p, null, userInfoMap,
					null, "CLARA_IT_SECURITY_NOTIFICATION", "", null, null,
					null, "", null);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public ProtocolDao getProtocolDao() {
		return protocolDao;
	}

	@Autowired(required = true)
	public void setProtocolDao(ProtocolDao protocolDao) {
		this.protocolDao = protocolDao;
	}

	public ProtocolEmailService getProtocolEmailService() {
		return protocolEmailService;
	}

	@Autowired(required = true)
	public void setProtocolEmailService(
			ProtocolEmailService protocolEmailService) {
		this.protocolEmailService = protocolEmailService;
	}

	public XmlProcessor getXmlProcessor() {
		return xmlProcessor;
	}

	@Autowired(required = true)
	public void setXmlProcessor(XmlProcessor xmlProcessor) {
		this.xmlProcessor = xmlProcessor;
	}

	public EmailService getEmailService() {
		return emailService;
	}

	@Autowired(required = true)
	public void setEmailService(EmailService emailService) {
		this.emailService = emailService;
	}

	public EntityManager getEm() {
		return em;
	}

	@PersistenceContext(unitName = "defaultPersistenceUnit")
	public void setEm(EntityManager em) {
		this.em = em;
	}

	public String getApplicationHost() {
		return applicationHost;
	}

	public void setApplicationHost(String applicationHost) {
		this.applicationHost = applicationHost;
	}

	public UserDao getUserDao() {
		return userDao;
	}

	@Autowired(required = true)
	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}
	
	public boolean isShouldRun() {
		return shouldRun;
	}

	@Value("${scheduler.task.outgoing.should.run}")
	public void setShouldRun(boolean shouldRun) {
		this.shouldRun = shouldRun;
	}
}
