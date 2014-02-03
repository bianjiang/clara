package edu.uams.clara.xml;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.google.common.collect.Sets;

import edu.uams.clara.core.util.xml.XmlHandler;
import edu.uams.clara.core.util.xml.XmlHandlerFactory;

/**
 *
 * @author Jiang Bian
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "file:src/test/java/edu/uams/clara/xml/XmlHandlerTest-context.xml" })
public class XmlHandlerTest {

	private final static Logger logger = LoggerFactory
			.getLogger(XmlHandlerTest.class);

	// relative to /protocol/staffs/staff/user
	private Set<String> userInfoXPaths = Sets.newHashSet();
	{
		userInfoXPaths.add("@id");
		userInfoXPaths.add("@pi_serial");
		userInfoXPaths.add("./roles/role");// only consider the first role
	}

	private final String testXml = "<protocol id=\"00003\" identifier=\"00003\">\r\n  <title>VA Cooperative Study of Coronary Artery Surgery I Stable Angina  CSP  4)  The con rev approval for 1 16 90 was contingent      </title>\r\n  <extra />\r\n  <status>Closed</status>\r\n  <summary>\r\n    <irb-determination>\r\n      <ped-risk>0.0</ped-risk>\r\n      <adult-risk />\r\n      <consent-waived />\r\n      <hipaa-not-applicable />\r\n      <hipaa-waived />\r\n      <review-period>12</review-period>\r\n      <hipaa-waived-date />\r\n      <agenda-date />\r\n    </irb-determination>\r\n  </summary>\r\n  <original-study>\r\n    <approval-date>1983-01-18</approval-date>\r\n    <IRBReviewPeriod>12</IRBReviewPeriod>\r\n    <originalSubmissionDate />\r\n    <submit-date />\r\n    <originalSubmissionTime />\r\n    <originalReviewDate />\r\n    <review-date />\r\n    <closeDate>1990-03-20</closeDate>\r\n    <close-date>1990-03-20</close-date>\r\n    <terminatedDate />\r\n    <terminated-date />\r\n    <suspendedDate />\r\n    <suspend-date />\r\n    <HIPAAWaiverDate />\r\n    <localSubjects value=\"0\">0.0</localSubjects>\r\n    <totalSubjects value=\"0\">0.0</totalSubjects>\r\n    <approval-begin-date>1983-01-18</approval-begin-date>\r\n    <IRBAgendaDate />\r\n    <approval-status />\r\n  </original-study>\r\n  <subjects>\r\n    <accural-goal-local value=\"0\">0.0</accural-goal-local>\r\n    <accural-goal value=\"0\">0.0</accural-goal>\r\n  </subjects>\r\n  <most-recent-study>\r\n    <approval-end-date>1984-01-18</approval-end-date>\r\n    <approval-date>1983-01-18</approval-date>\r\n    <approval-status>Full Board</approval-status>\r\n  </most-recent-study>\r\n  <phases />\r\n  <studytype>\r\n    <pType>Biomedical</pType>\r\n    <isInvestigatorInitiated>unknown</isInvestigatorInitiated>\r\n    <pType>unknown</pType>\r\n  </studytype>\r\n  <study-nature>biomedical-clinical</study-nature>\r\n  <staffs>\r\n    <staff id=\"ea8b1673-6d4b-4451-b458-8e10f73dbd63\">\r\n      <user id=\"165\" phone=\"\" pi_serial=\"1300\" sap=\"\">\r\n        <lastname>Murphy</lastname>\r\n        <firstname>M.</firstname>\r\n        <email />\r\n        <roles>\r\n          <role>Principal Investigator</role>\r\n        </roles>\r\n        <costs />\r\n        <conflict-of-interest />\r\n        <conflict-of-interest-description />\r\n        <reponsibilities>\r\n          <responsibility />\r\n        </reponsibilities>\r\n      </user>\r\n      <notify>false</notify>\r\n    </staff>\r\n  </staffs>\r\n</protocol>";

	@Test
	public void testXmlHandler() throws ParserConfigurationException {
		XmlHandler xmlHandler = XmlHandlerFactory.newXmlHandler();
		List<Map<String, String>> results = xmlHandler
				.getListOfMappedElementValues(testXml,
						"/protocol/staffs/staff/user", userInfoXPaths);

		for(Map<String, String> result:results){
			logger.debug(result.get("./roles/role"));
			for(Entry<String, String> attr:result.entrySet()){
				logger.debug(attr.getKey() + ":" + attr.getValue());
			}
		}
	}
}
