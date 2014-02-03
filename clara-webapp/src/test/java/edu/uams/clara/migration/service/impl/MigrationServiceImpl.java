package edu.uams.clara.migration.service.impl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.WordUtils;
import org.bouncycastle.util.encoders.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import edu.uams.clara.core.util.xml.DomUtils;
import edu.uams.clara.core.util.xml.DomUtils.Encoding;
import edu.uams.clara.core.util.xml.XmlHandler;
import edu.uams.clara.integration.incoming.crimson.dao.CrimsonContractDao;
import edu.uams.clara.integration.incoming.crimson.dao.CrimsonStudyDao;
import edu.uams.clara.migration.dao.MigrationDao;
import edu.uams.clara.migration.service.MigrationService;
import edu.uams.clara.webapp.common.dao.history.TrackDao;
import edu.uams.clara.webapp.common.dao.usercontext.PersonDao;
import edu.uams.clara.webapp.common.dao.usercontext.UserDao;
import edu.uams.clara.webapp.common.domain.history.Track;
import edu.uams.clara.webapp.common.domain.usercontext.Person;
import edu.uams.clara.webapp.common.domain.usercontext.User;
import edu.uams.clara.webapp.common.domain.usercontext.enums.Committee;
import edu.uams.clara.webapp.common.service.impl.UserServiceImpl;
import edu.uams.clara.webapp.common.util.DateFormatUtil;
import edu.uams.clara.webapp.fileserver.service.SFTPService;
import edu.uams.clara.webapp.protocol.dao.ProtocolDao;
import edu.uams.clara.webapp.protocol.dao.businesslogicobject.ProtocolFormStatusDao;
import edu.uams.clara.webapp.protocol.dao.businesslogicobject.ProtocolStatusDao;
import edu.uams.clara.webapp.protocol.dao.protocolform.ProtocolFormDao;
import edu.uams.clara.webapp.protocol.dao.protocolform.ProtocolFormXmlDataDao;
import edu.uams.clara.webapp.protocol.domain.Protocol;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.ProtocolFormStatus;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.ProtocolStatus;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.enums.ProtocolFormStatusEnum;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.enums.ProtocolStatusEnum;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolForm;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolFormXmlData;
import edu.uams.clara.webapp.protocol.domain.protocolform.enums.ProtocolFormType;
import edu.uams.clara.webapp.protocol.domain.protocolform.enums.ProtocolFormXmlDataType;
import edu.uams.clara.webapp.protocol.service.impl.ProtocolMetaDataXmlServiceImpl;
import edu.uams.clara.webapp.xml.processor.XmlProcessor;

public class MigrationServiceImpl implements MigrationService {

	private final static Logger logger = LoggerFactory
			.getLogger(MigrationServiceImpl.class);

	private XmlProcessor xmlProcessor;
	private XmlHandler xmlHandler;
	private ProtocolDao protocolDao;
	private ProtocolFormDao protocolFormDao;
	private ProtocolFormXmlDataDao protocolFormXmlDataDao;
	private ProtocolStatusDao protocolStatusDao;
	private ProtocolFormStatusDao protocolFormStatusDao;
	private UserDao userDao;
	private PersonDao personDao;
	private TrackDao trackDao;
	private CrimsonStudyDao crimsonStudyDao;
	private CrimsonContractDao crimsonContractDao;
	private MigrationDao migrationDao;

	private SFTPService sFTPService;
	private UserServiceImpl userServiceImpl;
	private ProtocolMetaDataXmlServiceImpl ProtocolMetaDataXmlService;

	private String fileServerHost;
	private String localDirectory;

	private MessageDigest messageDigest = null;

	private XPathFactory xpathFactory = XPathFactory.newInstance();

	@Override
	public XPath getXPathInstance() {
		return xpathFactory.newXPath();
	}

	private String parseDate(String dateString) {
		String convertedDate = "";
		if (dateString == null || dateString.isEmpty()) {
			convertedDate = "";
		} else {
			try {
				Date date = new SimpleDateFormat("yyyy-MM-dd")
						.parse(dateString);

				convertedDate = DateFormatUtil.formateDateToMDY(date);
			} catch (Exception e) {
				convertedDate = "";
			}
		}

		return convertedDate;
	}

	private List<String> getProtoclMetaData(Document rawDoc,
			List<String> existedUserList) throws XPathExpressionException,
			DOMException, IOException {
		XPath xPath = getXPathInstance();

		XPathExpression xPathExpression = null;

		String rawXml = DomUtils.elementToString(rawDoc, false, Encoding.UTF16);

		Document protocolMetaDataDoc = xmlProcessor.newDocument();
		List<String> result = new ArrayList<String>();

		Element protocolEle = protocolMetaDataDoc.createElement("protocol");
		protocolMetaDataDoc.appendChild(protocolEle);

		xPathExpression = xPath.compile("//protocol");
		NodeList nodes = (NodeList) xPathExpression.evaluate(rawDoc,
				XPathConstants.NODESET);
		Element protocolEl = (Element) nodes.item(0);

		String irbNum = protocolEl.getAttribute("id");
		String status = protocolEl.getAttribute("status");

		protocolEle.setAttribute("id", irbNum);
		protocolEle.setAttribute("identifier", irbNum);

		Element titleEle = protocolMetaDataDoc.createElement("title");
		xPathExpression = xPath.compile("//protocol/title/text()");
		nodes = (NodeList) xPathExpression.evaluate(rawDoc,
				XPathConstants.NODESET);
		if (nodes.getLength() > 0) {
			try {
				String titleStr = nodes.item(0).getNodeValue();
				titleStr = titleStr.replace("��", "\'");
				titleStr = titleStr.replace("��", "&alpha;");
				titleStr = titleStr.replace("�C", "-");

				titleEle.setTextContent(titleStr);
			} catch (Exception e) {

			}
		}
		protocolEle.appendChild(titleEle);

		Element laySummaryEle = protocolMetaDataDoc
				.createElement("lay-summary");
		protocolEle.appendChild(laySummaryEle);
		String laySummayStr = xmlHandler.getSingleStringValueByXPath(rawXml,
				"//protocol/review/LaySummary");
		laySummaryEle.setTextContent(laySummayStr);
		Element inclusionCriteriaEle = protocolMetaDataDoc
				.createElement("inclusion-criteria");
		protocolEle.appendChild(inclusionCriteriaEle);
		String inclusionCriteriaStr = xmlHandler.getSingleStringValueByXPath(
				rawXml, "//protocol/review/SubjectCriteria");
		inclusionCriteriaEle.setTextContent(inclusionCriteriaStr);

		Element extraEle = protocolMetaDataDoc.createElement("extra");
		protocolEle.appendChild(extraEle);

		Element statusEle = protocolMetaDataDoc.createElement("status");

		statusEle.setTextContent(status);
		protocolEle.appendChild(statusEle);

		// summary content
		Element summaryEle = protocolMetaDataDoc.createElement("summary");
		protocolEle.appendChild(summaryEle);
		Element irbDeterminationEle = protocolMetaDataDoc
				.createElement("irb-determination");
		summaryEle.appendChild(irbDeterminationEle);
		xPathExpression = xPath
				.compile("//protocol/review/PediatricRisk/text()");
		nodes = (NodeList) xPathExpression.evaluate(rawDoc,
				XPathConstants.NODESET);
		Element pediatricRiskEle = protocolMetaDataDoc
				.createElement("ped-risk");
		irbDeterminationEle.appendChild(pediatricRiskEle);

		int pedRisk = 0;

		try {
			pedRisk = Integer.valueOf(nodes.item(0).getNodeValue());

		} catch (Exception e) {

		}

		String pedRiskStr = "";
		if (pedRisk > 0) {
			pedRiskStr = "" + pedRisk;
		}

		pediatricRiskEle.setTextContent(pedRiskStr);

		xPathExpression = xPath.compile("//protocol/review/AdultRisk/text()");
		nodes = (NodeList) xPathExpression.evaluate(rawDoc,
				XPathConstants.NODESET);
		Element adultRiskEle = protocolMetaDataDoc.createElement("adult-risk");
		irbDeterminationEle.appendChild(adultRiskEle);
		try {
			adultRiskEle.setTextContent(nodes.item(0).getNodeValue());
		} catch (Exception e) {

		}

		/***
		 * Moved to protocol form metadata and protocolformxmldata.
		 */
		/*
		 * xPathExpression = xPath
		 * .compile("//protocol/review/ConsentWaived/text()"); nodes =
		 * (NodeList) xPathExpression .evaluate(doc, XPathConstants.NODESET);
		 * Element consentWaivedkEle = protoclMetaDataDoc
		 * .createElement("consent-type"); //consent-wavied
		 * irbDeterminationEle.appendChild(consentWaivedkEle); try {
		 * consentWaivedkEle.setTextContent(nodes.item(0).getNodeValue()); }
		 * catch (Exception e) {
		 *
		 * }
		 */
		xPathExpression = xPath.compile("//protocol/review/HIPAAWaiver/text()");
		nodes = (NodeList) xPathExpression.evaluate(rawDoc,
				XPathConstants.NODESET);
		Element HIPAANotApplicableEle = protocolMetaDataDoc
				.createElement("hipaa-applicable");
		irbDeterminationEle.appendChild(HIPAANotApplicableEle);
		try {
			// if (nodes.item(0).getNodeValue().toLowerCase().equals("no")) {
			// HIPAANotApplicableEle.setTextContent("yes");
			// } else if (nodes.item(0).getNodeValue().toLowerCase()
			// .endsWith("yes")) {
			// HIPAANotApplicableEle.setTextContent("no");
			// } else {
			HIPAANotApplicableEle.setTextContent(nodes.item(0).getNodeValue()
					.toLowerCase());
			// }
		} catch (Exception e) {

		}

		xPathExpression = xPath
				.compile("//protocol/review/IRBHIPAAWaived/text()");
		nodes = (NodeList) xPathExpression.evaluate(rawDoc,
				XPathConstants.NODESET);
		Element waivedEle = protocolMetaDataDoc.createElement("hipaa-waived");
		irbDeterminationEle.appendChild(waivedEle);
		try {
			waivedEle.setTextContent(nodes.item(0).getNodeValue());
		} catch (Exception e) {

		}

		// Original Approval Status and Original Approval Date
		Element originalStudyEle = protocolMetaDataDoc
				.createElement("original-study");
		protocolEle.appendChild(originalStudyEle);

		Element orgApprovalDateEle = protocolMetaDataDoc
				.createElement("approval-date");
		originalStudyEle.appendChild(orgApprovalDateEle);
		xPathExpression = xPath
				.compile("//protocol/review/IniApprovalDate/text()");
		nodes = (NodeList) xPathExpression.evaluate(rawDoc,
				XPathConstants.NODESET);
		if (nodes.getLength() > 0) {
			try {
				orgApprovalDateEle.setTextContent(this.parseDate(nodes.item(0)
						.getNodeValue()));
			} catch (Exception e) {

			}

		}

		Element reviewPeriodInSummary = protocolMetaDataDoc
				.createElement("review-period");
		irbDeterminationEle.appendChild(reviewPeriodInSummary);

		Element IRBReviewPeriodEle = protocolMetaDataDoc
				.createElement("IRBReviewPeriod");
		originalStudyEle.appendChild(IRBReviewPeriodEle);
		xPathExpression = xPath.compile("//protocol/review");
		NodeList reviewNodes = (NodeList) xPathExpression.evaluate(rawDoc,
				XPathConstants.NODESET);
		if (reviewNodes.getLength() > 0) {
			Element reviewEle = (Element) reviewNodes.item(0);
			if (reviewEle.getAttribute("IRBReviewPeriod").equals("Annual")) {
				IRBReviewPeriodEle.setTextContent("12");
				reviewPeriodInSummary.setTextContent("12");
			} else if (reviewEle.getAttribute("IRBReviewPeriod").equals(
					"Semi-Annual")) {
				IRBReviewPeriodEle.setTextContent("6");
				reviewPeriodInSummary.setTextContent("6");
			} else if (reviewEle.getAttribute("IRBReviewPeriod").equals(
					"Quarterly")) {
				IRBReviewPeriodEle.setTextContent("3");
				reviewPeriodInSummary.setTextContent("3");
			}
		}

		Element originalSubmissionDateEle = protocolMetaDataDoc
				.createElement("originalSubmissionDate");
		originalStudyEle.appendChild(originalSubmissionDateEle);
		Element originalSubmissionDateEleForClara = protocolMetaDataDoc
				.createElement("submit-date");
		originalStudyEle.appendChild(originalSubmissionDateEleForClara);
		xPathExpression = xPath
				.compile("//protocol/review/OriginalSubmissionDate/text()");
		NodeList oriSubDateList = (NodeList) xPathExpression.evaluate(rawDoc,
				XPathConstants.NODESET);
		if (oriSubDateList.getLength() > 0) {
			try {
				originalSubmissionDateEle.setTextContent(this
						.parseDate(oriSubDateList.item(0).getNodeValue()));
				originalSubmissionDateEleForClara.setTextContent(this
						.parseDate(oriSubDateList.item(0).getNodeValue()));
			} catch (Exception e) {

			}

		}

		Element originalSubmissionTimeEle = protocolMetaDataDoc
				.createElement("originalSubmissionTime");
		originalStudyEle.appendChild(originalSubmissionTimeEle);
		xPathExpression = xPath
				.compile("//protocol/review/OriginalSubmissionTime/text()");
		NodeList oriSubTimeList = (NodeList) xPathExpression.evaluate(rawDoc,
				XPathConstants.NODESET);
		if (oriSubTimeList.getLength() > 0) {
			try {
				originalSubmissionTimeEle.setTextContent(oriSubTimeList.item(0)
						.getNodeValue());
			} catch (Exception e) {

			}
		}

		Element originalReviewDateEle = protocolMetaDataDoc
				.createElement("originalReviewDate");
		originalStudyEle.appendChild(originalReviewDateEle);

		Element originalReviewDateEleForClara = protocolMetaDataDoc
				.createElement("review-date");
		originalStudyEle.appendChild(originalReviewDateEleForClara);

		xPathExpression = xPath
				.compile("//protocol/review/IniReviewDate/text()");
		NodeList oriReviDateList = (NodeList) xPathExpression.evaluate(rawDoc,
				XPathConstants.NODESET);
		if (oriSubTimeList.getLength() > 0) {
			try {
				originalReviewDateEle.setTextContent(this
						.parseDate(oriReviDateList.item(0).getNodeValue()));
				originalReviewDateEleForClara.setTextContent(this
						.parseDate(oriReviDateList.item(0).getNodeValue()));
			} catch (Exception e) {

			}
		}

		Element closeDateEle = protocolMetaDataDoc.createElement("closeDate");
		Element closeDateEleForClara = protocolMetaDataDoc
				.createElement("close-date");
		originalStudyEle.appendChild(closeDateEle);
		originalStudyEle.appendChild(closeDateEleForClara);
		xPathExpression = xPath.compile("//protocol/review/CloseDate/text()");
		NodeList closeDateList = (NodeList) xPathExpression.evaluate(rawDoc,
				XPathConstants.NODESET);
		if (closeDateList.getLength() > 0) {
			try {
				closeDateEle.setTextContent(this.parseDate(closeDateList
						.item(0).getNodeValue()));
				closeDateEleForClara.setTextContent(this
						.parseDate(closeDateList.item(0).getNodeValue()));
			} catch (Exception e) {

			}
		}

		Element terminateDateEle = protocolMetaDataDoc
				.createElement("terminatedDate");
		originalStudyEle.appendChild(terminateDateEle);
		Element terminateDateEleForClara = protocolMetaDataDoc
				.createElement("terminated-date");
		originalStudyEle.appendChild(terminateDateEleForClara);
		xPathExpression = xPath
				.compile("//protocol/review/TerminatedDate/text()");
		NodeList termiDateList = (NodeList) xPathExpression.evaluate(rawDoc,
				XPathConstants.NODESET);
		if (termiDateList.getLength() > 0) {
			try {
				terminateDateEle.setTextContent(this.parseDate(termiDateList
						.item(0).getNodeValue()));
				terminateDateEleForClara.setTextContent(this
						.parseDate(termiDateList.item(0).getNodeValue()));
			} catch (Exception e) {

			}
		}

		Element suspendedDateEle = protocolMetaDataDoc
				.createElement("suspendedDate");
		originalStudyEle.appendChild(suspendedDateEle);
		Element suspendedDateEleForClara = protocolMetaDataDoc
				.createElement("suspend-date");
		originalStudyEle.appendChild(suspendedDateEleForClara);
		xPathExpression = xPath
				.compile("//protocol/review/SuspendedDate/text()");
		NodeList suspendedDateList = (NodeList) xPathExpression.evaluate(
				rawDoc, XPathConstants.NODESET);
		if (suspendedDateList.getLength() > 0) {
			try {
				suspendedDateEle.setTextContent(this
						.parseDate(suspendedDateList.item(0).getNodeValue()));
			} catch (Exception e) {

			}
		}

		Element HIPPAWaiverDateEle = protocolMetaDataDoc
				.createElement("HIPAAWaiverDate");
		Element HIPPAWaiverDateEleFan = protocolMetaDataDoc
				.createElement("hipaa-waived-date");
		irbDeterminationEle.appendChild(HIPPAWaiverDateEleFan);
		originalStudyEle.appendChild(HIPPAWaiverDateEle);
		xPathExpression = xPath
				.compile("//protocol/review/HIPAAWaiverDate/text()");
		NodeList HIPPADateList = (NodeList) xPathExpression.evaluate(rawDoc,
				XPathConstants.NODESET);
		if (HIPPADateList.getLength() > 0) {
			try {
				HIPPAWaiverDateEle.setTextContent(this.parseDate(HIPPADateList
						.item(0).getNodeValue()));
				HIPPAWaiverDateEleFan.setTextContent(this
						.parseDate(HIPPADateList.item(0).getNodeValue()));
			} catch (Exception e) {

			}
		}

		Element localSubjectsEle = protocolMetaDataDoc
				.createElement("localSubjects");
		originalStudyEle.appendChild(localSubjectsEle);
		Element totalSubjectsEle = protocolMetaDataDoc
				.createElement("totalSubjects");
		originalStudyEle.appendChild(totalSubjectsEle);

		/*
		 * Element subjectsEle = protoclMetaDataDoc.createElement("subjects");
		 * protocolEle.appendChild(subjectsEle); Element accuralGoalLocalEle =
		 * protoclMetaDataDoc .createElement("accural-goal-local");
		 * subjectsEle.appendChild(accuralGoalLocalEle); Element accuralGoalEle
		 * = protoclMetaDataDoc .createElement("accural-goal");
		 * subjectsEle.appendChild(accuralGoalEle);
		 */
		Element accrualGoalEle = protocolMetaDataDoc
				.createElement("accrual-goal");
		protocolEle.appendChild(accrualGoalEle);
		Element accrualGoalLocalEle = protocolMetaDataDoc
				.createElement("accrual-goal-local");
		protocolEle.appendChild(accrualGoalLocalEle);

		xPathExpression = xPath
				.compile("//protocol/review/LocalSubjects/text()");
		NodeList localSubjectsList = (NodeList) xPathExpression.evaluate(
				rawDoc, XPathConstants.NODESET);
		localSubjectsEle.setAttribute("value", "");
		accrualGoalLocalEle.setAttribute("value", "");
		if (localSubjectsList.getLength() > 0) {
			String localSubjectsStr = localSubjectsList.item(0).getNodeValue();

			localSubjectsEle.setAttribute("value", localSubjectsStr);
			accrualGoalLocalEle.setAttribute("value", localSubjectsStr);

			try {

				float localSubjectsFloat = Float.valueOf(localSubjectsStr);
				int localSubjectsInt = (int) localSubjectsFloat;

				localSubjectsEle.setTextContent(String
						.valueOf(localSubjectsInt));
				accrualGoalLocalEle.setTextContent(String
						.valueOf(localSubjectsInt));
			} catch (Exception e) {
				// do nothing
			}
		}

		xPathExpression = xPath
				.compile("//protocol/review/TotalSubjects/text()");
		NodeList totalSubjectsList = (NodeList) xPathExpression.evaluate(
				rawDoc, XPathConstants.NODESET);
		totalSubjectsEle.setAttribute("value", "");
		accrualGoalEle.setAttribute("value", "");
		if (totalSubjectsList.getLength() > 0) {
			String totalSubjectsStr = totalSubjectsList.item(0).getNodeValue();

			totalSubjectsEle.setAttribute("value", totalSubjectsStr);
			accrualGoalEle.setAttribute("value", totalSubjectsStr);

			try {

				float totalSubjectsFloat = Float.valueOf(totalSubjectsStr);
				int totalSubjectsInt = (int) totalSubjectsFloat;

				totalSubjectsEle.setTextContent(String
						.valueOf(totalSubjectsInt));
				accrualGoalEle.setTextContent(String.valueOf(totalSubjectsInt));
			} catch (Exception e) {
				// do nothing
			}
		}

		Element beginDateEle = protocolMetaDataDoc
				.createElement("approval-begin-date");
		originalStudyEle.appendChild(beginDateEle);
		xPathExpression = xPath.compile("//protocol/review/pBeginDate/text()");
		NodeList beginDateList = (NodeList) xPathExpression.evaluate(rawDoc,
				XPathConstants.NODESET);
		if (beginDateList.getLength() > 0) {
			beginDateEle.setTextContent(this.parseDate(beginDateList.item(0)
					.getNodeValue()));
		}

		Element agendaDateEle = protocolMetaDataDoc
				.createElement("IRBAgendaDate");
		originalStudyEle.appendChild(agendaDateEle);
		Element agendaDateForClaraEle = protocolMetaDataDoc
				.createElement("agenda-date");
		irbDeterminationEle.appendChild(agendaDateForClaraEle);

		xPathExpression = xPath
				.compile("//protocol/review/IRBAgendaDate/text()");
		NodeList agendaDateList = (NodeList) xPathExpression.evaluate(rawDoc,
				XPathConstants.NODESET);
		if (agendaDateList.getLength() > 0) {
			try {
				agendaDateEle.setTextContent(this.parseDate(agendaDateList
						.item(0).getNodeValue()));
				agendaDateForClaraEle.setTextContent(this
						.parseDate(agendaDateList.item(0).getNodeValue()));
			} catch (Exception e) {

			}
		}

		Element mostRecentStudyEle = protocolMetaDataDoc
				.createElement("most-recent-study");
		protocolEle.appendChild(mostRecentStudyEle);
		Element approvalEndDateEle = protocolMetaDataDoc
				.createElement("approval-end-date");
		mostRecentStudyEle.appendChild(approvalEndDateEle);
		xPathExpression = xPath.compile("//protocol/review/pEndDate/text()");
		nodes = (NodeList) xPathExpression.evaluate(rawDoc,
				XPathConstants.NODESET);
		if (nodes.getLength() > 0) {
			approvalEndDateEle.setTextContent(this.parseDate(nodes.item(0)
					.getNodeValue()));
		}

		Element mostRapprovalDateEle = protocolMetaDataDoc
				.createElement("approval-date");
		mostRecentStudyEle.appendChild(mostRapprovalDateEle);
		xPathExpression = xPath.compile("//protocol/review/IRBLastCR/text()");
		nodes = (NodeList) xPathExpression.evaluate(rawDoc,
				XPathConstants.NODESET);

		if (nodes.getLength() > 0) {
			mostRapprovalDateEle.setTextContent(this.parseDate(nodes.item(0)
					.getNodeValue()));
		}

		Element orgApprovalStatusEle = protocolMetaDataDoc
				.createElement("approval-status");
		mostRecentStudyEle.appendChild(orgApprovalStatusEle);

		Element orgApprovalStatusForClaraEle = protocolMetaDataDoc
				.createElement("approval-status");
		originalStudyEle.appendChild(orgApprovalStatusForClaraEle);

		xPathExpression = xPath.compile("//protocol/review");
		nodes = (NodeList) xPathExpression.evaluate(rawDoc,
				XPathConstants.NODESET);
		if (nodes.getLength() > 0) {
			Element approvalstatus = (Element) nodes.item(0);
			if (approvalstatus.getAttribute("IRBReviewStatus").equals("Full")) {
				orgApprovalStatusEle.setTextContent("Full Board");
				// orgApprovalStatusForClaraEle.setTextContent("Full Board");
			} else {
				orgApprovalStatusEle.setTextContent(approvalstatus
						.getAttribute("IRBReviewStatus"));
			}
			/*
			 * else if (approvalstatus.getAttribute("IRBReviewStatus").equals(
			 * "Expedited")) { orgApprovalStatusEle.setTextContent("Expedited");
			 * // orgApprovalStatusForClaraEle.setTextContent("Expedited"); }
			 *
			 * else if (approvalstatus.getAttribute("IRBReviewStatus").equals(
			 * "Exempt")) { orgApprovalStatusEle.setTextContent("Exempt"); //
			 * orgApprovalStatusForClaraEle.setTextContent("Exempt"); }
			 */

		}

		// phases info should come form crimson
		Element phasesEle = protocolMetaDataDoc.createElement("phases");
		protocolEle.appendChild(phasesEle);

		Element studyTypeEle = protocolMetaDataDoc.createElement("studytype");
		protocolEle.appendChild(studyTypeEle);

		Element pTypeEle = protocolMetaDataDoc.createElement("pType");
		studyTypeEle.appendChild(pTypeEle);

		xPathExpression = xPath.compile("//protocol/studytype/pType/text()");
		NodeList pTypenodes = (NodeList) xPathExpression.evaluate(rawDoc,
				XPathConstants.NODESET);
		if (pTypenodes.getLength() > 0) {
			pTypeEle.setTextContent(pTypenodes.item(0).getNodeValue());
			Element studyNatureEle = protocolMetaDataDoc
					.createElement("study-nature");
			if (pTypenodes.item(0).getNodeValue().equals("Biomedical")) {

				studyNatureEle.setTextContent("biomedical-clinical");

			} else if (pTypenodes.item(0).getNodeValue().equals("Behavioral")) {
				studyNatureEle.setTextContent("social-behavioral-education");
			}
			protocolEle.appendChild(studyNatureEle);

		} else {
			pTypeEle.setTextContent("unknown");
		}
		Element InvestInitiatedEle = protocolMetaDataDoc
				.createElement("isInvestigatorInitiated");
		xPathExpression = xPath
				.compile("//protocol/studytype/isInvestigatorInitiated/text()");
		nodes = (NodeList) xPathExpression.evaluate(rawDoc,
				XPathConstants.NODESET);

		studyTypeEle.appendChild(InvestInitiatedEle);
		if (nodes.getLength() > 0)
			InvestInitiatedEle.setTextContent(nodes.item(0).getNodeValue());
		else
			InvestInitiatedEle.setTextContent("unknown");

		// update info according to crimson info
		// crimson obejct[] null
		if (crimsonStudyDao.findCTObjectbyIRBNum(irbNum) == null) {
			Element studyPTypeEle = protocolMetaDataDoc.createElement("pType");
			studyTypeEle.appendChild(studyPTypeEle);
			studyPTypeEle.setTextContent("unknown");
		} // crimson obejct[] is not null
		else {
			Object crimsonCT[] = crimsonStudyDao.findCTObjectbyIRBNum(irbNum);

			// title
			if (crimsonCT[16] != null) {
				String title = (String) crimsonCT[16];
				titleEle.setTextContent(title);
			}

			// phase
			/*
			 * Element studyPhasesEle = (Element) doc.getElementsByTagName(
			 * "phases").item(0);
			 */
			if (crimsonCT[8] != null) {
				String phases = (String) crimsonCT[8];
				// multiple phases
				if (phases.contains("|")) {
					String splitPhases[] = phases.split("\\|");
					for (int j = 0; j < splitPhases.length; j++) {
						Element phaseEle = protocolMetaDataDoc
								.createElement("phase");
						phasesEle.appendChild(phaseEle);
						String phaseID = splitPhases[j];
						if (crimsonStudyDao.findPhaseById(phaseID)
								.equals("N/A")) {
							phaseEle.setTextContent("na");
						} else {
							phaseEle.setTextContent(crimsonStudyDao
									.findPhaseById(phaseID).toLowerCase());
						}
					}

				} else {
					Element phaseEle = protocolMetaDataDoc
							.createElement("phase");
					phasesEle.appendChild(phaseEle);
					if (crimsonStudyDao.findPhaseById(phases).equals("N/A")) {
						phaseEle.setTextContent("na");
					} else {
						phaseEle.setTextContent(crimsonStudyDao.findPhaseById(
								phases).toLowerCase());
					}
				}

			}
		}

		// begig to get drug info
		protocolMetaDataDoc = migrationAriaDrugInfo(protocolMetaDataDoc,
				protocolEle, rawDoc);

		Element staffsEle = protocolMetaDataDoc.createElement("staffs");
		protocolEle.appendChild(staffsEle);

		// get staff info
		xPathExpression = xPath.compile("//protocol/staffs/staff/user");
		nodes = (NodeList) xPathExpression.evaluate(rawDoc,
				XPathConstants.NODESET);

		Map<String, Element> userMap = new HashMap<String, Element>();
		for (int i = 0; i < nodes.getLength(); i++) {
			Element tempEleForloop = (Element) nodes.item(i);
			String staffid = UUID.randomUUID().toString();
			Element staffEle = protocolMetaDataDoc.createElement("staff");
			staffEle.setAttribute("id", staffid);

			Element lastNameEle = protocolMetaDataDoc.createElement("lastname");
			Element firstnameEle = protocolMetaDataDoc
					.createElement("firstname");
			Element emailEle = protocolMetaDataDoc.createElement("email");
			Element rolesEle = protocolMetaDataDoc.createElement("roles");
			Element costsEle = protocolMetaDataDoc.createElement("costs");
			Element cofIntEle = protocolMetaDataDoc
					.createElement("conflict-of-interest");
			Element cofIntDesEle = protocolMetaDataDoc
					.createElement("conflict-of-interest-description");
			// info missed
			Element respbsEle = protocolMetaDataDoc
					.createElement("reponsibilities");

			Element userEle = protocolMetaDataDoc.createElement("user");
			if (userMap.containsKey(tempEleForloop.getAttribute("id"))) {
				String piserialID = tempEleForloop.getAttribute("id");

				userEle = userMap.get(piserialID);
				rolesEle = (Element) userEle.getElementsByTagName("roles")
						.item(0);
			} else {
				staffsEle.appendChild(staffEle);
				staffEle.appendChild(userEle);
				userEle.appendChild(lastNameEle);
				userEle.appendChild(firstnameEle);
				userEle.appendChild(emailEle);
				userEle.appendChild(rolesEle);
				userEle.appendChild(costsEle);
				userEle.appendChild(cofIntEle);
				userEle.appendChild(cofIntDesEle);
				userEle.appendChild(respbsEle);
				Element respbEle = protocolMetaDataDoc
						.createElement("responsibility");
				respbsEle.appendChild(respbEle);
			}
			String sapIDStr = tempEleForloop.getAttribute("sap");

			userEle.setAttribute("sap", "");

			// get user id info from clara
			userEle.setAttribute("pi_serial", tempEleForloop.getAttribute("id"));
			userMap.put(tempEleForloop.getAttribute("id"), userEle);
			// Person person = new Person();
			User user = null;
			if (!sapIDStr.isEmpty()) {
				// convert sap in into clara format, remove 000 in
				// front
				try {
					int sapID = Integer.valueOf(sapIDStr);
					sapIDStr = String.valueOf(sapID);
					userEle.setAttribute("sap", sapIDStr);
				} catch (Exception e) {
				}

				logger.debug("sap: " + sapIDStr);
				if (userDao.getUserBySAP(sapIDStr).isEmpty()) {
					user = userServiceImpl
							.getAndUpdateUserBySap(sapIDStr, true);
				} else {
					user = userDao.getUserBySAP(sapIDStr).get(0);
				}

				if (user != null) {
					userEle.setAttribute("id", "" + user.getId());

					Person person = user.getPerson();

					userEle.setAttribute("phone", person.getWorkphone());
					lastNameEle.setTextContent(person.getLastname());
					firstnameEle.setTextContent(person.getFirstname());
					emailEle.setTextContent(person.getEmail());
				}

				// if user existed, directly use the id
				/*
				 * if (!userDao.getUserBySAP(sapIDStr).isEmpty()) {
				 * userEle.setAttribute("id", "" +
				 * userDao.getUserBySAP(sapIDStr).get(0).getId()); } // else
				 * create the user else { try {
				 * userServiceImpl.getAndUpdateUserBySap(sapIDStr, true);
				 * userEle.setAttribute("id", "" +
				 * userDao.getUserBySAP(sapIDStr).get(0) .getId()); } catch
				 * (Exception e) { }
				 *
				 * }
				 */

				// person = personDao.getPersonBySAP(sapIDStr);
				// logger.debug(personDao.getPersonBySAP(sapIDStr)+"");
			}
			/*
			 * if (personDao.getPersonBySAP(sapIDStr) != null) { person =
			 * personDao.getPersonBySAP(sapIDStr); userEle.setAttribute("phone",
			 * person.getWorkphone());
			 * lastNameEle.setTextContent(person.getLastname());
			 * firstnameEle.setTextContent(person.getFirstname());
			 * emailEle.setTextContent(person.getEmail()); }
			 */if (sapIDStr.isEmpty() || user == null) {

				String missedUserInfo = "";
				String firstName = "";
				String lastName = "";
				String email = "";
				String username = "";
				String department = "";
				String phone = "";
				phone = tempEleForloop.getAttribute("phone");
				userEle.setAttribute("phone",
						tempEleForloop.getAttribute("phone"));

				xPathExpression = xPath.compile("//user[@id='"
						+ tempEleForloop.getAttribute("id")
						+ "']/firstname/text()");
				NodeList tempNodeList = (NodeList) xPathExpression.evaluate(
						rawDoc, XPathConstants.NODESET);

				if (tempNodeList.getLength() > 0) {
					firstnameEle.setTextContent(tempNodeList.item(0)
							.getNodeValue());
					firstName = tempNodeList.item(0).getNodeValue();
				}

				xPathExpression = xPath.compile("//user[@id='"
						+ tempEleForloop.getAttribute("id")
						+ "']/lastname/text()");
				tempNodeList = (NodeList) xPathExpression.evaluate(rawDoc,
						XPathConstants.NODESET);

				if (tempNodeList.getLength() > 0) {
					lastNameEle.setTextContent(tempNodeList.item(0)
							.getNodeValue());
					lastName = tempNodeList.item(0).getNodeValue();
				}

				xPathExpression = xPath
						.compile("//user[@id='"
								+ tempEleForloop.getAttribute("id")
								+ "']/email/text()");
				tempNodeList = (NodeList) xPathExpression.evaluate(rawDoc,
						XPathConstants.NODESET);
				if (tempNodeList.getLength() > 0) {
					emailEle.setTextContent(tempNodeList.item(0).getNodeValue());
					email = tempNodeList.item(0).getNodeValue();
				}

				xPathExpression = xPath.compile("//user[@id='"
						+ tempEleForloop.getAttribute("id")
						+ "']/location/dept/text()");
				tempNodeList = (NodeList) xPathExpression.evaluate(rawDoc,
						XPathConstants.NODESET);
				if (tempNodeList.getLength() > 0) {
					department = tempNodeList.item(0).getNodeValue();
				}

				User achUser = null;
				try {
					achUser = userDao.getUserByEmail(email);

				} catch (Exception e) {
					achUser = null;

				}

				if (achUser != null) {
					userEle.setAttribute("id", "" + achUser.getId());
				}
				username = lastName + firstName;
				missedUserInfo += username + "," + firstName + "," + lastName
						+ "," + phone + "," + email + "," + department;
				if (!existedUserList.contains(missedUserInfo)) {
					existedUserList.add(missedUserInfo);

					userEle.setAttribute("phone", phone);
					lastNameEle.setTextContent(lastName);
					firstnameEle.setTextContent(firstName);
					emailEle.setTextContent(email);
					// save users not in clara
					/*
					 * BufferedReader input = new BufferedReader(new FileReader(
					 * "C:\\Data\\MigrationMissedUsers.txt")); String existData
					 * = ""; String newData = ""; while ((existData =
					 * input.readLine()) != null) { newData += existData + "\n";
					 * } input.close(); newData = newData + missedUserInfo;
					 * BufferedWriter output = new BufferedWriter(new
					 * FileWriter( "C:\\Data\\MigrationMissedUsers.txt"));
					 * output.write(newData); output.close();
					 */
				}
			}

			xPathExpression = xPath.compile("//user[@id='"
					+ tempEleForloop.getAttribute("id")
					+ "']/PositionTile/text()");
			NodeList roleNodeList = (NodeList) xPathExpression.evaluate(rawDoc,
					XPathConstants.NODESET);

			// for duplicated roles
			NodeList roleList = rolesEle.getElementsByTagName("role");
			List<String> roleTypeStringList = new ArrayList<String>();
			for (int j = 0; j < roleList.getLength(); j++) {
				roleTypeStringList.add(roleList.item(j).getTextContent()
						.toLowerCase());
			}
			for (int j = 0; j < roleNodeList.getLength(); j++) {

				if (roleTypeStringList.contains(roleNodeList.item(j)
						.getNodeValue().toLowerCase())) {
					continue;
				}
				roleTypeStringList.add(roleNodeList.item(j).getNodeValue()
						.toLowerCase());
				Element roleEle = protocolMetaDataDoc.createElement("role");
				String roleStr = roleNodeList.item(j).getNodeValue();
				roleStr = WordUtils.capitalize(roleStr);
				/*
				 * if(roleStr.equals("principal investigator")){
				 * roleStr="Principal Investigator"; }
				 */
				roleEle.setTextContent(roleStr);

				rolesEle.appendChild(roleEle);
			}

			// test whether notify
			Element notifyEle = protocolMetaDataDoc.createElement("notify");

			if (roleNodeList.getLength() > 0) {

				if (roleNodeList.item(0).getNodeValue()
						.equals("Primary Contact")) {

					notifyEle.setTextContent("true");

				} else
					notifyEle.setTextContent("false");
			}

			staffEle.appendChild(notifyEle);

		}

		if (crimsonStudyDao.findCTObjectbyIRBNum(irbNum) != null) {
			Object crimsonCT[] = crimsonStudyDao.findCTObjectbyIRBNum(irbNum);

			//add plan number
			int ctID = (int) crimsonCT[14];
			Object[] planCodeInfo = crimsonStudyDao.findPlanCodeByCTNum(ctID+"");
			if(planCodeInfo!=null){
				Element hospitalDeterEle = protocolMetaDataDoc
						.createElement("hospital-service-determinations");
				summaryEle.appendChild(hospitalDeterEle);

				Element planCodeEle = protocolMetaDataDoc
						.createElement("insurance-plan-code");
				hospitalDeterEle.appendChild(planCodeEle);

				Element corporateCodeEle = protocolMetaDataDoc
						.createElement("corporate-gurantor-code");
				hospitalDeterEle.appendChild(corporateCodeEle);

				if(planCodeInfo[0]!=null){
					corporateCodeEle.setTextContent((String)planCodeInfo[0]);
				}
				if(planCodeInfo[1]!=null){
					planCodeEle.setTextContent((String)planCodeInfo[1]);
				}
			}

			// add budget items

			Element budDeterEle = protocolMetaDataDoc
					.createElement("budget-determination");
			summaryEle.appendChild(budDeterEle);
			Element budgetApprDateEle = protocolMetaDataDoc
					.createElement("approval-date");
			budDeterEle.appendChild(budgetApprDateEle);
			if (crimsonStudyDao.findBudgetApprovalDate(ctID) != null) {
				String approvalDate = DateFormat.getInstance().format(
						crimsonStudyDao.findBudgetApprovalDate(ctID));
				budDeterEle.setTextContent(approvalDate);
			}

			Object[] budgetObj = null;
			try {
				budgetObj = crimsonStudyDao.findBudgetByCTID(ctID);
			} catch (Exception e) {
				budgetObj = null;
			}
			if (budgetObj != null) {
				Element hospitalServiceDeterminationsEle = protocolMetaDataDoc
						.createElement("hospital-service-determinations");
				summaryEle.appendChild(hospitalServiceDeterminationsEle);
				Element coverageDeterminationEle = protocolMetaDataDoc
						.createElement("coverage-determination");
				summaryEle.appendChild(coverageDeterminationEle);
				if (budgetObj[2] != null) {
					String guarantor = (String) budgetObj[2];
					Element guarantorEle = protocolMetaDataDoc
							.createElement("corporate-gurantor-code");
					hospitalServiceDeterminationsEle.appendChild(guarantorEle);
					guarantorEle.setTextContent(guarantor);
				}
				if (budgetObj[3] != null) {
					String planCode = (String) budgetObj[3];
					Element planCodeEle = protocolMetaDataDoc
							.createElement("insurance-plan-code");
					hospitalServiceDeterminationsEle.appendChild(planCodeEle);
					planCodeEle.setTextContent(planCode);
				}

				if (budgetObj[4] != null) {
					int riskCode = (int) budgetObj[4];
					Element trialCategoryEle = protocolMetaDataDoc
							.createElement("trial-category");
					coverageDeterminationEle.appendChild(trialCategoryEle);
					if (riskCode == 0) {
						trialCategoryEle.setTextContent("");
						trialCategoryEle.setAttribute("id", "");
					}
					if (riskCode == 1) {
						trialCategoryEle
								.setTextContent("MQCT (Medicare Qualifying Clinical Trial)");
						trialCategoryEle.setAttribute("id",
								"medicare-qualifying-clinical-trial)");
					}
					if (riskCode == 2) {
						trialCategoryEle
								.setTextContent("CTNQ (Clinical Trial-Non-Qualifying)");
						trialCategoryEle.setAttribute("id",
								"clinical-trial-non-qualifying)");
					}
					if (riskCode == 4) {
						trialCategoryEle.setTextContent("N/A");
						trialCategoryEle.setAttribute("id", "na");
					}
				}

				if (budgetObj[1] != null) {
					int bmUserID = (int) budgetObj[1];
					if (bmUserID > 0) {
						int hasUserTag = 0;
						NodeList existingUserELe = protocolMetaDataDoc
								.getElementsByTagName("user");
						for (int userIndex = 0; userIndex < existingUserELe
								.getLength(); userIndex++) {
							Element existUserEle = (Element) existingUserELe
									.item(userIndex);
							if (existUserEle.getAttribute("pi_serial").equals(
									"" + bmUserID)) {
								hasUserTag = 1;
								NodeList existingRolesELeList = existUserEle
										.getElementsByTagName("roles");
								Element existRolesEle = (Element) existingRolesELeList
										.item(0);
								Element roleEle = protocolMetaDataDoc
										.createElement("role");
								existRolesEle.appendChild(roleEle);
								roleEle.setTextContent("Budget Manager");

								NodeList existingRespsELeList = existUserEle
										.getElementsByTagName("reponsibilities");
								Element existRespsEle = (Element) existingRespsELeList
										.item(0);
								Element respEle = protocolMetaDataDoc
										.createElement("responsibility");
								existRespsEle.appendChild(respEle);
								respEle.setTextContent("Budget Manager");
							}
						}
						if (hasUserTag == 0) {
							protocolMetaDataDoc = processBudgetUser(bmUserID,
									protocolMetaDataDoc, staffsEle, 1);

						}
					}
				}
				if (budgetObj[5] != null) {
					int baUserID = (int) budgetObj[5];
					if (baUserID > 0) {
						int hasUserTag = 0;
						NodeList existingUserELe = protocolMetaDataDoc
								.getElementsByTagName("user");
						for (int userIndex = 0; userIndex < existingUserELe
								.getLength(); userIndex++) {
							Element existUserEle = (Element) existingUserELe
									.item(userIndex);
							if (existUserEle.getAttribute("pi_serial").equals(
									"" + baUserID)) {
								hasUserTag = 1;
								NodeList existingRolesELeList = existUserEle
										.getElementsByTagName("roles");
								Element existRolesEle = (Element) existingRolesELeList
										.item(0);
								Element roleEle = protocolMetaDataDoc
										.createElement("role");
								existRolesEle.appendChild(roleEle);
								roleEle.setTextContent("Budget Administrator");

								NodeList existingRespsELeList = existUserEle
										.getElementsByTagName("reponsibilities");
								Element existRespsEle = (Element) existingRespsELeList
										.item(0);
								Element respEle = protocolMetaDataDoc
										.createElement("responsibility");
								existRespsEle.appendChild(respEle);
								respEle.setTextContent("Budget Administrator");
							}
						}
						if (hasUserTag == 0) {
							protocolMetaDataDoc = processBudgetUser(baUserID,
									protocolMetaDataDoc, staffsEle, 0);

						}

					}
				}

			}
		}

		String protoclMetaData = DomUtils.elementToString(protocolMetaDataDoc,
				false, Encoding.UTF16);
		result.add(protoclMetaData);

		// continue to process xmlmetadata for prtocolform
		xPathExpression = xPath
				.compile("//protocol/review/ConsentWaived/text()");
		nodes = (NodeList) xPathExpression.evaluate(rawDoc,
				XPathConstants.NODESET);
		Element consentWaivedkEle = protocolMetaDataDoc
				.createElement("consent-type"); // consent-wavied
		irbDeterminationEle.appendChild(consentWaivedkEle);
		try {
			consentWaivedkEle.setTextContent(nodes.item(0).getNodeValue());
		} catch (Exception e) {

		}
		statusEle.setTextContent("ARCHIVE");
		protocolEle.setAttribute("type", "ARCHIVE");

		Element sitesEle = protocolMetaDataDoc.createElement("study-sites");
		protocolEle.appendChild(sitesEle);
		// get site info
		xPathExpression = xPath.compile("//protocol/study-sites/site");
		nodes = (NodeList) xPathExpression.evaluate(rawDoc,
				XPathConstants.NODESET);

		// get whether multiple sites
		Element multipleSitesEle = protocolMetaDataDoc.createElement("sites");
		protocolEle.appendChild(multipleSitesEle);

		Element multipleorSingleSiteEle = protocolMetaDataDoc
				.createElement("single-or-multi");
		multipleSitesEle.appendChild(multipleorSingleSiteEle);

		xPathExpression = xPath.compile("//protocol/review/MultiSite/text()");
		NodeList mulSitesNodes = (NodeList) xPathExpression.evaluate(rawDoc,
				XPathConstants.NODESET);

		if (mulSitesNodes.getLength() > 0) {
			if (mulSitesNodes.item(0).getNodeValue().toLowerCase()
					.equals("yes")) {
				multipleorSingleSiteEle.setTextContent("multiple-sites");

				Element multiSiteEle = protocolMetaDataDoc
						.createElement("multiple-sites");
				multipleorSingleSiteEle.appendChild(multiSiteEle);
				Element localLeadEle = protocolMetaDataDoc
						.createElement("is-local-lead-entity");
				multiSiteEle.appendChild(localLeadEle);
				xPathExpression = xPath
						.compile("//protocol/review/LocalLead/text()");
				NodeList localLeadNodes = (NodeList) xPathExpression.evaluate(
						rawDoc, XPathConstants.NODESET);
				if (localLeadNodes.getLength() > 0) {
					try {
						localLeadEle.setTextContent(localLeadNodes.item(0)
								.getNodeValue());
					} catch (Exception e) {

					}
				}

			} else if (mulSitesNodes.item(0).getNodeValue().toLowerCase()
					.equals("no")) {
				multipleorSingleSiteEle.setTextContent("single-site");
			}
		}

		for (int i = 0; i < nodes.getLength(); i++) {
			Element tempEleForloop = (Element) nodes.item(i);
			Element siteEle = protocolMetaDataDoc.createElement("site");
			sitesEle.appendChild(siteEle);
			siteEle.setAttribute("approved", "true");
			siteEle.setAttribute("id", UUID.randomUUID().toString());
			siteEle.setAttribute("site-id",
					tempEleForloop.getAttribute("site-id"));

			Element siteNameEle = protocolMetaDataDoc
					.createElement("site-name");
			siteEle.appendChild(siteNameEle);
			xPathExpression = xPath
					.compile("//protocol/study-sites/site[@site-id='"
							+ siteEle.getAttribute("site-id")
							+ "']/site-name/text()");
			NodeList tempNodeList = (NodeList) xPathExpression.evaluate(rawDoc,
					XPathConstants.NODESET);
			if (tempNodeList.getLength() > 0)
				siteNameEle.setTextContent(tempNodeList.item(0).getNodeValue());

			Element addressEle = protocolMetaDataDoc.createElement("address");
			siteEle.appendChild(addressEle);
			xPathExpression = xPath
					.compile("//protocol/study-sites/site[@site-id='"
							+ siteEle.getAttribute("site-id")
							+ "']/address/text()");
			tempNodeList = (NodeList) xPathExpression.evaluate(rawDoc,
					XPathConstants.NODESET);
			if (tempNodeList.getLength() > 0)
				addressEle.setTextContent(tempNodeList.item(0).getNodeValue());

			Element cityEle = protocolMetaDataDoc.createElement("city");
			siteEle.appendChild(cityEle);
			xPathExpression = xPath
					.compile("//protocol/study-sites/site[@site-id='"
							+ siteEle.getAttribute("site-id")
							+ "']/city/text()");
			tempNodeList = (NodeList) xPathExpression.evaluate(rawDoc,
					XPathConstants.NODESET);
			if (tempNodeList.getLength() > 0)
				cityEle.setTextContent(tempNodeList.item(0).getNodeValue());

			Element stateEle = protocolMetaDataDoc.createElement("state");
			siteEle.appendChild(stateEle);
			xPathExpression = xPath
					.compile("//protocol/study-sites/site[@site-id='"
							+ siteEle.getAttribute("site-id")
							+ "']/state/text()");
			tempNodeList = (NodeList) xPathExpression.evaluate(rawDoc,
					XPathConstants.NODESET);
			if (tempNodeList.getLength() > 0)
				stateEle.setTextContent(tempNodeList.item(0).getNodeValue());

			Element zipEle = protocolMetaDataDoc.createElement("zip");
			siteEle.appendChild(zipEle);
			xPathExpression = xPath
					.compile("//protocol/study-sites/site[@site-id='"
							+ siteEle.getAttribute("site-id") + "']/zip/text()");
			tempNodeList = (NodeList) xPathExpression.evaluate(rawDoc,
					XPathConstants.NODESET);
			if (tempNodeList.getLength() > 0)
				zipEle.setTextContent(tempNodeList.item(0).getNodeValue());

			Element contactEle = protocolMetaDataDoc
					.createElement("site-contact");
			siteEle.appendChild(contactEle);
			xPathExpression = xPath
					.compile("//protocol/study-sites/site[@site-id='"
							+ siteEle.getAttribute("site-id")
							+ "']/site-contact/text()");
			tempNodeList = (NodeList) xPathExpression.evaluate(rawDoc,
					XPathConstants.NODESET);
			if (tempNodeList.getLength() > 0)
				contactEle.setTextContent(tempNodeList.item(0).getNodeValue());

		}
		String protoclFormMetaData = DomUtils.elementToString(
				protocolMetaDataDoc, false, Encoding.UTF16);

		result.add(protoclFormMetaData);
		// logger.debug(protoclFormMetaData);
		return result;

	}

	private Document migrationAriaDrugInfo(Document protocolMetaDataDoc,
			Element protocolEle, Document rawDoc)
			throws XPathExpressionException {
		XPath xPath = getXPathInstance();
		XPathExpression xPathExpression = null;

		Element drugsEle = protocolMetaDataDoc.createElement("drugs");
		protocolEle.appendChild(drugsEle);

		xPathExpression = xPath.compile("//protocol/drugs/drug");
		NodeList drugNodes = (NodeList) xPathExpression.evaluate(rawDoc,
				XPathConstants.NODESET);
		String rawXml = DomUtils.elementToString(rawDoc, false, Encoding.UTF16);
		for (int i = 0; i < drugNodes.getLength(); i++) {
			Element drugEle = protocolMetaDataDoc.createElement("drug");
			drugsEle.appendChild(drugEle);
			Element ariaDrugEle = (Element) drugNodes.item(i);

			String drugID = UUID.randomUUID().toString();

			drugEle.setAttribute("id", drugID);
			drugEle.setAttribute("admin", ariaDrugEle.getAttribute("admin"));
			drugEle.setAttribute("brochure",
					ariaDrugEle.getAttribute("brochure"));
			drugEle.setAttribute("approved", "");
			drugEle.setAttribute("identifier", "");
			drugEle.setAttribute("ind", ariaDrugEle.getAttribute("ind"));
			drugEle.setAttribute("insert", ariaDrugEle.getAttribute("insert"));
			drugEle.setAttribute("isprovided",
					ariaDrugEle.getAttribute("isprovided"));
			drugEle.setAttribute("name", ariaDrugEle.getAttribute("name"));
			drugEle.setAttribute("nsc", ariaDrugEle.getAttribute("nsc"));
			drugEle.setAttribute("provider",
					ariaDrugEle.getAttribute("provider"));
			drugEle.setAttribute("status", "");
			drugEle.setAttribute("type", ariaDrugEle.getAttribute("type"));

			Element dosageEle = protocolMetaDataDoc
					.createElement("providerdosage");
			drugEle.appendChild(dosageEle);
			dosageEle.setTextContent(xmlHandler.getSingleStringValueByXPath(
					rawXml, "//protocol/drugs/drug/providerdosage"));

			Element storageEle = protocolMetaDataDoc.createElement("storage");
			drugEle.appendChild(storageEle);
			storageEle.setTextContent(xmlHandler.getSingleStringValueByXPath(
					rawXml, "//protocol/drugs/drug/storage"));

			Element prepEle = protocolMetaDataDoc.createElement("prep");
			drugEle.appendChild(prepEle);
			prepEle.setTextContent(xmlHandler.getSingleStringValueByXPath(
					rawXml, "//protocol/drugs/drug/prep"));

			Element toxicityEle = protocolMetaDataDoc
					.createElement("toxicities");
			drugEle.appendChild(toxicityEle);
			toxicityEle.setTextContent(xmlHandler.getSingleStringValueByXPath(
					rawXml, "//protocol/drugs/drug/toxicities"));

			Element otherpharmEle = protocolMetaDataDoc
					.createElement("otherpharmacies");
			drugEle.appendChild(otherpharmEle);
			Element pharmEle = protocolMetaDataDoc.createElement("pharmacies");
			drugEle.appendChild(pharmEle);

			String pharmStr = xmlHandler.getSingleStringValueByXPath(rawXml,
					"//protocol/drugs/drug/pharmacy");
			logger.debug(pharmStr);
			int pharmExistTag = 0;
			if (pharmStr.contains("UAMS Outpatient")) {
				Element localPharmEle = protocolMetaDataDoc
						.createElement("local-pharmacy");
				pharmEle.appendChild(localPharmEle);
				localPharmEle.setAttribute("id", "0");
				localPharmEle.setAttribute("identifier", "UAMS (Outpatient)");
				localPharmEle.setAttribute("name", "UAMS (Outpatient)");
				pharmExistTag = 1;
			}
			if (pharmStr.contains("ACH Outpatient")) {
				Element localPharmEle = protocolMetaDataDoc
						.createElement("local-pharmacy");
				pharmEle.appendChild(localPharmEle);
				localPharmEle.setAttribute("id", "1");
				localPharmEle.setAttribute("identifier", "ACH (Outpatient)");
				localPharmEle.setAttribute("name", "ACH (Outpatient)");
				pharmExistTag = 1;
			}
			if (pharmStr.contains("UAMS Inpatient")) {
				Element localPharmEle = protocolMetaDataDoc
						.createElement("local-pharmacy");
				pharmEle.appendChild(localPharmEle);
				localPharmEle.setAttribute("id", "2");
				localPharmEle.setAttribute("identifier", "UAMS (Inpatient)");
				localPharmEle.setAttribute("name", "ACH (Inpatient)");
				pharmExistTag = 1;
			}
			if (pharmStr.contains("ACH Inpatient")) {
				Element localPharmEle = protocolMetaDataDoc
						.createElement("local-pharmacy");
				pharmEle.appendChild(localPharmEle);
				localPharmEle.setAttribute("id", "3");
				localPharmEle.setAttribute("identifier", "ACH (Inpatient)");
				localPharmEle.setAttribute("name", "ACH (Inpatient)");
				pharmExistTag = 1;
			}
			if (pharmExistTag == 0) {
				otherpharmEle.setTextContent(pharmStr);
			}

		}

		return protocolMetaDataDoc;
	}

	private Document processBudgetUser(int userID, Document protoclMetaDataDoc,
			Element staffsEle, int manager) {
		Object[] ariaUserObje = crimsonStudyDao.findAriaUserByUserID(userID);
		String sapIDStr = "";
		if (ariaUserObje[0] != null) {
			sapIDStr = (String) ariaUserObje[0];
			if (!sapIDStr.isEmpty()) {
				int sapID = Integer.valueOf(sapIDStr);
				sapIDStr = String.valueOf(sapID);
			}
		}
		Element staffEle = protoclMetaDataDoc.createElement("staff");
		staffsEle.appendChild(staffEle);
		String staffid = UUID.randomUUID().toString();
		staffEle.setAttribute("id", staffid);

		Element lastNameEle = protoclMetaDataDoc.createElement("lastname");
		Element firstnameEle = protoclMetaDataDoc.createElement("firstname");
		Element emailEle = protoclMetaDataDoc.createElement("email");
		Element rolesEle = protoclMetaDataDoc.createElement("roles");
		Element roleEle = protoclMetaDataDoc.createElement("role");

		Element costsEle = protoclMetaDataDoc.createElement("costs");
		Element cofIntEle = protoclMetaDataDoc
				.createElement("conflict-of-interest");
		Element cofIntDesEle = protoclMetaDataDoc
				.createElement("conflict-of-interest-description");
		// info missed
		Element respbsEle = protoclMetaDataDoc.createElement("reponsibilities");
		Element notifyEle = protoclMetaDataDoc.createElement("notify");

		Element userEle = protoclMetaDataDoc.createElement("user");
		staffsEle.appendChild(staffEle);
		staffEle.appendChild(userEle);
		staffEle.appendChild(notifyEle);
		userEle.appendChild(lastNameEle);
		userEle.appendChild(firstnameEle);
		userEle.appendChild(emailEle);
		userEle.appendChild(rolesEle);
		rolesEle.appendChild(roleEle);
		userEle.appendChild(costsEle);
		userEle.appendChild(cofIntEle);
		userEle.appendChild(cofIntDesEle);
		userEle.appendChild(respbsEle);
		Element respbEle = protoclMetaDataDoc.createElement("responsibility");
		respbsEle.appendChild(respbEle);

		notifyEle.setTextContent("false");
		if (manager == 1) {
			roleEle.setTextContent("Budget Manager");
			respbEle.setTextContent("Budget Manager");
		} else {
			roleEle.setTextContent("Budget Administrator");
			respbEle.setTextContent("Budget Administrator");
		}
		userEle.setAttribute("sap", "");

		// get user id info from clara
		userEle.setAttribute("pi_serial", "" + userID);
		Person person = new Person();
		if (!sapIDStr.isEmpty()) {
			// convert sap in into clara format, remove 000 in
			// front
			try {
				userEle.setAttribute("sap", sapIDStr);
			} catch (Exception e) {
			}

			// if user existed, directly use the id
			if (!userDao.getUserBySAP(sapIDStr).isEmpty()) {
				userEle.setAttribute("id", ""
						+ userDao.getUserBySAP(sapIDStr).get(0).getId());
			}
			// else create the user
			else {
				try {
					userServiceImpl.getAndUpdateUserBySap(sapIDStr, true);
					userEle.setAttribute("id",
							"" + userDao.getUserBySAP(sapIDStr).get(0).getId());
				} catch (Exception e) {
				}

			}

			// person = personDao.getPersonBySAP(sapIDStr);
			// logger.debug(personDao.getPersonBySAP(sapIDStr)+"");
		}

		if (personDao.getPersonBySAP(sapIDStr) != null) {
			person = personDao.getPersonBySAP(sapIDStr);
			userEle.setAttribute("phone", person.getWorkphone());
			lastNameEle.setTextContent(person.getLastname());
			firstnameEle.setTextContent(person.getFirstname());
			emailEle.setTextContent(person.getEmail());
		} else {

			String firstName = "";
			String lastName = "";
			String email = "";
			String phone = "";
			if (ariaUserObje[4] != null) {
				phone = (String) ariaUserObje[4];
			}

			userEle.setAttribute("phone", phone);
			if (ariaUserObje[1] != null) {
				lastName = (String) ariaUserObje[1];
			}
			if (ariaUserObje[2] != null) {
				firstName = (String) ariaUserObje[2];
			}
			if (ariaUserObje[3] != null) {
				email = (String) ariaUserObje[3];
			}
			firstnameEle.setTextContent(firstName);
			lastNameEle.setTextContent(lastName);
			emailEle.setTextContent(email);

			User achUser = null;
			try {
				achUser = userDao.getUserByEmail(email);

			} catch (Exception e) {
				achUser = null;

			}

			if (achUser != null) {
				userEle.setAttribute("id", "" + achUser.getId());
			}
		}

		return protoclMetaDataDoc;
	}

	private List<String> uploadDocumenttoFileServer(Document doc,
			String irbNum, List<String> hashFileNames,
			List<Integer> notExistedFileList) throws XPathExpressionException,
			DOMException, NoSuchAlgorithmException, IOException {
		/*
		 * String existingFormData = ""; try { existingFormData =
		 * protocolFormXmlDataDao
		 * .getLastProtocolFormXmlDataByProtocolIdAndType( Long.valueOf(irbNum),
		 * ProtocolFormXmlDataType.ARCHIVE) .getXmlData(); } catch (Exception e)
		 * { existingFormData = ""; }
		 *
		 * Document existtingFormXmlData = null;
		 *
		 * if (existingFormData.isEmpty()){ existtingFormXmlData = doc; } else {
		 * try { existtingFormXmlData = xmlProcessor
		 * .loadXmlStringToDOM(existingFormData); } catch (SAXException e) {
		 * existtingFormXmlData = null; } }
		 */

		XPath xPath = getXPathInstance();

		XPathExpression xPathExpression = null;

		// get the documents url lists for the protocol...
		xPathExpression = xPath
				.compile("//protocol/documents/document/ariapath/text()");
		NodeList documentUrls = (NodeList) xPathExpression.evaluate(doc,
				XPathConstants.NODESET);

		xPathExpression = xPath.compile("//protocol/documents/document");
		NodeList documentsInAria = (NodeList) xPathExpression.evaluate(doc,
				XPathConstants.NODESET);
		File fileDir = new File(localDirectory);
		if (!fileDir.exists()) {
			fileDir.mkdir();
		}

		for (int i = 0; i < documentUrls.getLength(); i++) {
			// whether file is already on server
			Element airaDocument = (Element) documentsInAria.item(i);
			/*
			 * if (existtingFormXmlData != null) { xPathExpression = xPath
			 * .compile("//protocol/documents/document[@id='" +
			 * airaDocument.getAttribute("id") + "']/ariapath/text()"); NodeList
			 * existingDocuemnts = (NodeList) xPathExpression
			 * .evaluate(existtingFormXmlData, XPathConstants.NODESET);
			 *
			 * if (existingDocuemnts.getLength() > 0) { String existPath =
			 * existingDocuemnts.item(0) .getTextContent(); String[]
			 * splitExistHashName = existPath.split("/"); String existHashName =
			 * splitExistHashName[splitExistHashName.length - 1];
			 * hashFileNames.add(existHashName); //continue; }
			 */
			// }
			// copy the file from aria server to local
			String oldAriaDocUrl[] = documentUrls.item(i).getNodeValue()
					.split("/" + irbNum + "/");
			String fileName = "Z://" + irbNum + "//" + oldAriaDocUrl[1];
			// get extention

			String extention[] = oldAriaDocUrl[1].split("\\.");
			String ext = "." + extention[extention.length - 1];

			messageDigest = MessageDigest.getInstance("SHA-256",
					new org.bouncycastle.jce.provider.BouncyCastleProvider());
			try {
				//logger.debug(fileName);
				InputStream fileContent = new FileInputStream(fileName);

				byte[] bytes = IOUtils.toByteArray(fileContent);

				messageDigest.update(bytes);

				// get the hash of file content
				String hashFileName = new String(Hex.encode(messageDigest
						.digest()));

				hashFileNames.add(hashFileName + ext);
				// write the file to local

				String uploadfilename = localDirectory + "/" + hashFileName
						+ ext;
				//logger.debug("uploadfilename: " + uploadfilename);
				FileOutputStream fout = new FileOutputStream(uploadfilename);
				fout.write(bytes);
				fout.flush();
				fout.close();

				// upload file to the server
				int trySFTP = 1;
				while (trySFTP > 0) {
					try {

						sFTPService.uploadLocalFileToRemote("protocol/"
								+ irbNum + "/" + hashFileName + ext);

						trySFTP = 0;
					} catch (Exception e) {
						if (trySFTP < 5) {
							trySFTP++;
						}
						if (trySFTP == 5) {
							BufferedReader input = new BufferedReader(
									new FileReader(
											"C:\\Data\\SFTPMissedList.txt"));
							String existData = "";
							String newData = "";
							while ((existData = input.readLine()) != null) {
								newData += existData + "\n";
							}
							input.close();
							newData += fileName;
							BufferedWriter output = new BufferedWriter(
									new FileWriter(
											"C:\\Data\\SFTPMissedList.txt"));
							output.write(newData);
							output.close();
						}

					}
				}
				// delete the file after uploading...
				File uploadedFile = new File(uploadfilename);
				uploadedFile.delete();
			} catch (Exception e) {
				// logger.debug("id is " +i);
				notExistedFileList.add(i);
				e.printStackTrace();
				continue;
			}

		}
		// remove template dir
		fileDir.delete();

		return hashFileNames;
	}

	private Document processDocumentInfo(Document doc,
			Document docWotdocumentsInfo) throws XPathExpressionException {
		XPath xPath = getXPathInstance();

		XPathExpression xPathExpression = null;

		xPathExpression = xPath.compile("//protocol");
		NodeList nodes = (NodeList) xPathExpression.evaluate(doc,
				XPathConstants.NODESET);
		Element tempEle = (Element) nodes.item(0);
		String irbNum = tempEle.getAttribute("id");

		List<String> hashFileNames = new ArrayList<String>();
		List<Integer> notExistedFileList = new ArrayList<Integer>();
		// uoload file to the file server
		try {
			hashFileNames = uploadDocumenttoFileServer(doc, irbNum,
					hashFileNames, notExistedFileList);
		} catch (DOMException | NoSuchAlgorithmException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// get the documents url lists for the protocol...

		NodeList documentUrls = doc.getElementsByTagName("ariapath");
		NodeList documents = doc.getElementsByTagName("document");

		NodeList newProtocolList = docWotdocumentsInfo
				.getElementsByTagName("protocol");
		Element newProtocolEle = (Element) newProtocolList.item(0);
		Element newDocumentsEle = docWotdocumentsInfo
				.createElement("documents");
		newProtocolEle.appendChild(newDocumentsEle);
		// add old doc name as an attribute for each document and change old url
		// to new url

		// j is set for the noexisted file
		for (int i = 0, j = 0; i < documents.getLength(); i++) {
			if (notExistedFileList.contains(i)) {
				j++;
				continue;

			}

			Element newDocumentEle = docWotdocumentsInfo
					.createElement("document");
			newDocumentsEle.appendChild(newDocumentEle);
			Element newAriapathEle = docWotdocumentsInfo
					.createElement("ariapath");
			newDocumentEle.appendChild(newAriapathEle);

			Element documentEle = (Element) documents.item(i);
			Element docUrlEle = (Element) documentUrls.item(i);
			String[] splitUrl = docUrlEle.getTextContent().split(
					"/" + irbNum + "/");
			newDocumentEle.setAttribute("oldName",
					splitUrl[splitUrl.length - 1]);
			newDocumentEle.setAttribute("date",
					documentEle.getAttribute("date"));
			newDocumentEle.setAttribute("id", documentEle.getAttribute("id"));
			newDocumentEle.setAttribute("status",
					documentEle.getAttribute("status"));
			newDocumentEle.setAttribute("title",
					documentEle.getAttribute("title"));
			newDocumentEle.setAttribute("type",
					documentEle.getAttribute("type"));
			newDocumentEle.setAttribute("version",
					documentEle.getAttribute("version"));

			// this url is for dev

			/*
			 * String newFileUrl = "https://" + fileServerHost +
			 * "/files/dev/protocol/" + irbNum + "/" + hashFileNames.get(i - j);
			 */

			// this url is for production

			String newFileUrl = "https://" + fileServerHost
					+ "/files/protocol/" + irbNum + "/"
					+ hashFileNames.get(i - j);

			newAriapathEle.setTextContent(newFileUrl);
		}

		return docWotdocumentsInfo;

	}

	private Document procsessCrimensonInfo(Document doc) {
		NodeList protocolList = doc.getElementsByTagName("protocol");
		Element protocolEle = (Element) protocolList.item(0);
		String irbNum = protocolEle.getAttribute("id");
		// crimson obejct[] null
		if (crimsonStudyDao.findCTObjectbyIRBNum(irbNum) == null) {
			Element studyTypesEle = (Element) doc.getElementsByTagName(
					"studytype").item(0);
			Element studyTypeEle = doc.createElement("pType");
			studyTypesEle.appendChild(studyTypeEle);
			studyTypeEle.setTextContent("unknown");
		} // crimson obejct[] is not null
		else {
			Object crimsonCT[] = crimsonStudyDao.findCTObjectbyIRBNum(irbNum);
			// create element for crimson
			Element crimsonEle = doc.createElement("crimson");
			protocolEle.appendChild(crimsonEle);

			// get crimson id
			if (crimsonCT[14] != null) {
				Element ctIDEle = doc.createElement("crimsonId");
				crimsonEle.appendChild(ctIDEle);
				int ctID = (int) crimsonCT[14];
				ctIDEle.setTextContent("" + ctID);
			}

			// get drug info for protocol
			List<String> drugNameList = crimsonStudyDao
					.findDrugNameByIRBNum(irbNum);

			if (drugNameList != null) {
				Element drugsEle = doc.createElement("drugs");
				crimsonEle.appendChild(drugsEle);
				for (int j = 0; j < drugNameList.size(); j++) {
					Element drugEle = doc.createElement("drug");
					drugsEle.appendChild(drugEle);
					drugEle.setAttribute("name", drugNameList.get(j));
					drugEle.setAttribute("id", UUID.randomUUID().toString());
					drugEle.setAttribute("admin", "");
					drugEle.setAttribute("approved", "");
					drugEle.setAttribute("brochure", "");
					drugEle.setAttribute("dosage", "");
					drugEle.setAttribute("identifier", "");
					drugEle.setAttribute("ind", "");
					drugEle.setAttribute("insert", "");
					drugEle.setAttribute("isprovided", "");
					drugEle.setAttribute("nsc", "");
					drugEle.setAttribute("provider", "");
					drugEle.setAttribute("status", "");
					Element drugStorageEle = doc.createElement("storage");
					Element drugPrepeEle = doc.createElement("prep");
					Element drugToxEle = doc.createElement("toxicities");
					Element drugPharmEle = doc.createElement("pharmacies");
					drugEle.appendChild(drugPharmEle);
					drugEle.appendChild(drugToxEle);
					drugEle.appendChild(drugPrepeEle);
					drugEle.appendChild(drugStorageEle);
				}
			}

			// get crimson status
			if (crimsonCT[17] != null) {
				int statusID = (int) crimsonCT[17];
				String crimsonStatus = crimsonStudyDao
						.findCrimosnStatusByID(statusID);
				Element crimsonStatusEle = doc.createElement("crimson-status");
				crimsonEle.appendChild(crimsonStatusEle);
				crimsonStatusEle.setTextContent(crimsonStatus);
			}

			// get location ids
			if (crimsonStudyDao.findLocationIDsbyIRBNum(irbNum) != null) {
				String locationIDs = crimsonStudyDao
						.findLocationIDsbyIRBNum(irbNum);
				Element LocationsEle = doc.createElement("locations");
				crimsonEle.appendChild(LocationsEle);
				// if multiple lcations
				if (locationIDs.contains("|")) {
					String splitLocationIDs[] = locationIDs.split("\\|");
					for (int i = 0; i < splitLocationIDs.length; i++) {
						Element LocationEle = doc.createElement("location");
						LocationsEle.appendChild(LocationEle);
						LocationEle.setAttribute("code", crimsonStudyDao
								.findLocationCodeById(splitLocationIDs[i]));
						LocationEle.setAttribute("name", crimsonStudyDao
								.findLocationNameById(splitLocationIDs[i]));
					}
				}// only one location
				else {
					Element LocationEle = doc.createElement("location");
					LocationsEle.appendChild(LocationEle);
					LocationEle.setAttribute("code",
							crimsonStudyDao.findLocationCodeById(locationIDs));
					LocationEle.setAttribute("name",
							crimsonStudyDao.findLocationNameById(locationIDs));
				}
			}

			// study type
			Element crimsonStudyTypesEle = doc
					.createElement("crimson-studytype");
			if (crimsonStudyDao.findStudyTypeIDsbyIRBNum(irbNum) != null) {
				String studyTypeIDs = crimsonStudyDao
						.findStudyTypeIDsbyIRBNum(irbNum);
				if (studyTypeIDs.contains("|")) {
					String splitStudyTypeIDs[] = studyTypeIDs.split("\\|");
					for (int j = 0; j < splitStudyTypeIDs.length; j++) {
						Element studyTypeEle = doc.createElement("type");
						crimsonStudyTypesEle.appendChild(studyTypeEle);
						studyTypeEle.setTextContent(crimsonStudyDao
								.findStudyType(splitStudyTypeIDs[j]));
					}
				} else {
					Element studyTypeEle = doc.createElement("type");
					crimsonStudyTypesEle.appendChild(studyTypeEle);
					studyTypeEle.setTextContent(crimsonStudyDao
							.findStudyType(studyTypeIDs));
				}

			} else {
				Element studyTypeEle = doc.createElement("type");
				crimsonStudyTypesEle.appendChild(studyTypeEle);
				studyTypeEle.setTextContent("unknown");
			}

			// brief title
			Element briefTitleEle = doc.createElement("brief-title");
			crimsonEle.appendChild(briefTitleEle);

			if (crimsonCT[0] != null) {
				String briefTitle = (String) crimsonCT[0];
				briefTitleEle.setTextContent(briefTitle);
			}
			// set college and depoartment info for fan
			Element respDeptEle = doc.createElement("responsible-department");
			protocolEle.appendChild(respDeptEle);

			// division info
			Element divisionEle = doc
					.createElement("study-college-or-division");
			crimsonEle.appendChild(divisionEle);
			if (crimsonCT[1] != null) {
				int divisionId = (short) crimsonCT[1];
				String divisionName = crimsonStudyDao
						.findDivisionNameByID(divisionId);
				divisionEle.setTextContent(divisionName);
				respDeptEle.setAttribute("collegedesc", divisionName);
			}

			// dept info

			Element deptEle = doc.createElement("department");
			crimsonEle.appendChild(deptEle);
			if (crimsonCT[2] != null) {
				int deptId = (short) crimsonCT[2];
				String deptName = crimsonStudyDao.findDeptNameByID(deptId);
				deptEle.setTextContent(deptName);
				respDeptEle.setAttribute("deptdesc", deptName);
			}
			// sub-dept info
			Element subDeptEle = doc.createElement("sub-department");
			crimsonEle.appendChild(subDeptEle);
			if (crimsonCT[3] != null) {
				int subDeptId = (short) crimsonCT[3];
				String subDeptName = crimsonStudyDao
						.findsubDeptNameByID(subDeptId);
				subDeptEle.setTextContent(subDeptName);
				respDeptEle.setAttribute("subdeptdesc", subDeptName);

			}

			// set study-type for fan display
			Element studyTypeEle = doc.createElement("study-type");
			protocolEle.appendChild(studyTypeEle);

			// study category
			Element studyCategoryEle = doc.createElement("study-category");
			crimsonEle.appendChild(studyCategoryEle);

			Element categoryEle = doc.createElement("category");
			studyCategoryEle.appendChild(categoryEle);
			if (crimsonCT[4] != null) {
				int ctTypeID = (short) crimsonCT[4];
				String ctTypeName = crimsonStudyDao
						.findCtTypeNameById(ctTypeID);
				categoryEle.setTextContent(ctTypeName);
				if (ctTypeName.equals("Industry Sponsored"))
					studyTypeEle.setTextContent("industry-sponsored");
				else if (ctTypeName.equals("Investigator Initiated"))
					studyTypeEle.setTextContent("investigator-initiated");
				else if (ctTypeName.equals("Cooperative Group"))
					studyTypeEle.setTextContent("cooperative-group");
			}

			Element compassUseEle = doc.createElement("compassionate-use");
			studyCategoryEle.appendChild(compassUseEle);
			if (crimsonCT[7] != null) {
				int compassUse = (short) crimsonCT[7];
				if (compassUse == 1)
					compassUseEle.setTextContent("yes");
				else if (compassUse == 0)
					compassUseEle.setTextContent("no");
			}

			Element notifyMethodEle = doc.createElement("notify-method");
			studyCategoryEle.appendChild(notifyMethodEle);
			if (crimsonCT[9] != null) {
				int notifyMethodId = (short) crimsonCT[9];
				if (notifyMethodId > 0)
					notifyMethodEle.setTextContent(crimsonStudyDao
							.findNotifyMethodById(notifyMethodId));
			}

			Element notifyModeEle = doc.createElement("notify-mode");
			studyCategoryEle.appendChild(notifyModeEle);
			if (crimsonCT[10] != null) {
				int notifyModeId = (short) crimsonCT[10];
				// logger.debug("" + notifyModeId);
				if (notifyModeId > 0)
					notifyModeEle.setTextContent(crimsonStudyDao
							.findNotifyModeById(notifyModeId));
			}

			// Regulatory Information
			Element reguInfoEle = doc.createElement("regulatory-information");
			crimsonEle.appendChild(reguInfoEle);

			Element articlesEle = doc.createElement("test-article");
			reguInfoEle.appendChild(articlesEle);
			if (crimsonCT[11] != null) {
				String atricleIDs = (String) crimsonCT[11];
				if (atricleIDs.contains("|") || atricleIDs.contains("-")) {
					if (atricleIDs.contains("|")) {
						String splitArticleIDs[] = atricleIDs.split("\\|");
						for (int i = 0; i < splitArticleIDs.length; i++) {
							Element articleEle = doc.createElement("article");
							articlesEle.appendChild(articleEle);
							String articleIdString = splitArticleIDs[i];
							int articleId = Integer.parseInt(articleIdString);
							articleEle.setTextContent(crimsonStudyDao
									.findArticleById(articleId));
						}
					}
				} else {
					Element articleEle = doc.createElement("article");
					articlesEle.appendChild(articleEle);
					int articleId = Integer.parseInt(atricleIDs);
					articleEle.setTextContent(crimsonStudyDao
							.findArticleById(articleId));
				}
			}

			Element prmcEle = doc.createElement("prmc-required");
			reguInfoEle.appendChild(prmcEle);
			if (crimsonCT[12] != null) {
				int prmcRequired = (short) crimsonCT[12];
				if (prmcRequired == 1)
					prmcEle.setTextContent("yes");
				else
					prmcEle.setTextContent("no");
			}

			Element manufacturingEle = doc.createElement("manufacturing");
			reguInfoEle.appendChild(manufacturingEle);
			if (crimsonCT[13] != null) {
				int macuId = (short) crimsonCT[13];
				if (macuId > 0)
					manufacturingEle.setTextContent(crimsonStudyDao
							.findManufactorByID(macuId));
			}

			Element registedWithGovEle = doc
					.createElement("registration-with-clinicalTrials-gov");
			reguInfoEle.appendChild(registedWithGovEle);
			if (crimsonCT[14] != null) {
				int ctID = (int) crimsonCT[14];
				if (crimsonStudyDao.findRegInfobyCtID(ctID) != null) {
					Object regInfo[] = crimsonStudyDao.findRegInfobyCtID(ctID);
					Element requiredRegEle = doc
							.createElement("require-registration");
					registedWithGovEle.appendChild(requiredRegEle);
					if (regInfo[5] != null) {
						int required = (short) regInfo[5];
						if (required == 2) {
							requiredRegEle.setTextContent("no");
						} else if (required == 1) {
							requiredRegEle.setTextContent("yes");
							Element haveBeenRegEle = doc
									.createElement("been-registered");
							registedWithGovEle.appendChild(haveBeenRegEle);
							if (regInfo[2] != null) {
								int reguested = (short) regInfo[2];
								if (reguested == 2) {
									haveBeenRegEle.setTextContent("no");
								} else if (reguested == 1) {
									haveBeenRegEle.setTextContent("yes");
									Element regDateEle = doc
											.createElement("registration-date");
									registedWithGovEle.appendChild(regDateEle);
									Element regNumEle = doc
											.createElement("registration-number");
									registedWithGovEle.appendChild(regNumEle);
									if (regInfo[3] != null) {
										String regDate = (String) regInfo[3];
										regDateEle.setTextContent(regDate);
									}
									if (regInfo[4] != null) {
										String regNum = (String) regInfo[4];
										regNumEle.setTextContent(regNum);
									}

								}
							}

						}
					}

				}

			}

			// summary
			Element summaryEle = doc.createElement("summary");
			crimsonEle.appendChild(summaryEle);
			if (crimsonCT[5] != null) {
				String summary = (String) crimsonCT[5];
				summaryEle.setTextContent(summary);
			}
			// comment
			Element commentEle = doc.createElement("comment");
			crimsonEle.appendChild(commentEle);
			if (crimsonCT[6] != null) {
				String comment = (String) crimsonCT[6];
				commentEle.setTextContent(comment);
			}

			// have budget or not
			if (crimsonCT[15] != null) {
				Element budgetReqEle = doc.createElement("has-budget");
				crimsonEle.appendChild(budgetReqEle);
				int budgetReq = (short) crimsonCT[15];
				if (budgetReq == 1)
					budgetReqEle.setTextContent("no");
				else if (budgetReq == 2)
					budgetReqEle.setTextContent("yes");
			}

			Element budDeterEle = doc.createElement("budget-determination");
			summaryEle.appendChild(budDeterEle);
			Element budgetApprDateEle = doc.createElement("approval-date");
			budDeterEle.appendChild(budgetApprDateEle);
			if (crimsonStudyDao.findBudgetApprovalDate((int) crimsonCT[14]) != null) {
				String approvalDate = DateFormat.getInstance().format(
						crimsonStudyDao
								.findBudgetApprovalDate((int) crimsonCT[14]));
				budDeterEle.setTextContent(approvalDate);
			}

			// have disease site or not
			if (crimsonCT[18] != null) {
				String diseaseSiteID = (String) crimsonCT[18];
				processDiseaseSiteInfo(doc, diseaseSiteID, crimsonEle);
			}

		}
		return doc;
	}

	private Document processDiseaseSiteInfo(Document doc, String diseaseSiteID,
			Element crimsonEle) {
		Element diseaseSitesEle = doc.createElement("disease-sites");
		crimsonEle.appendChild(diseaseSitesEle);
		String[] splitSites = diseaseSiteID.split("\\|");
		for (int i = 1; i < splitSites.length; i++) {
			if (splitSites[i].equals("-9"))
				continue;
			if (splitSites[i].contains("_")) {
				String[] splitSitesID = splitSites[i].split("\\_");
				int siteID = Integer.valueOf(splitSitesID[0]);
				int subSiteID = Integer.valueOf(splitSitesID[1]);
				// logger.debug(splitSitesID[1]);
				Element diseaseSiteEle = doc.createElement("disease-site");
				diseaseSitesEle.appendChild(diseaseSiteEle);

				diseaseSiteEle.setAttribute("id", splitSitesID[0]);
				diseaseSiteEle.setAttribute("description",
						crimsonStudyDao.findDiseaseSitebyID(siteID));

				Element subDiseaseSiteEle = doc.createElement("subsite");
				diseaseSiteEle.appendChild(subDiseaseSiteEle);
				subDiseaseSiteEle.setAttribute("id", splitSitesID[1]);
				subDiseaseSiteEle.setAttribute("description",
						crimsonStudyDao.findSubDiseaseSitebyID(subSiteID));

			} else {
				Element diseaseSiteEle = doc.createElement("disease-site");
				diseaseSitesEle.appendChild(diseaseSiteEle);
				diseaseSiteEle.setAttribute("id", splitSites[i]);
				int siteID = Integer.valueOf(splitSites[i]);
				diseaseSiteEle.setTextContent(crimsonStudyDao
						.findDiseaseSitebyID(siteID));

			}

		}

		return doc;

	}

	private Document processFundingInfo(Document doc) {
		NodeList protocolList = doc.getElementsByTagName("protocol");
		Element protocolEle = (Element) protocolList.item(0);
		String irbNum = protocolEle.getAttribute("id");
		if (crimsonStudyDao.findfundingTitle(irbNum) == null)
			return doc;

		List<Integer> fundingIDs = crimsonStudyDao.findfundingIDs(irbNum);
		List<String> fundingTitles = crimsonStudyDao.findfundingTitle(irbNum);
		List<String> fundingSposors = crimsonStudyDao
				.findfundingConnectionId(irbNum);
		List<String> fundingTypes = crimsonStudyDao.findfundingType(irbNum);
		List<String> fundingPartialInfos = crimsonStudyDao
				.findfundingPartialInfo(irbNum);

		Element fundingEle = doc.createElement("funding");
		if (fundingTitles.size() > 0)
			protocolEle.appendChild(fundingEle);

		for (int i = 0; i < fundingTitles.size(); i++) {
			Element fundingSrcEle = doc.createElement("funding-source");
			fundingEle.appendChild(fundingSrcEle);

			// get dept info
			fundingSrcEle.setAttribute("department",
					crimsonStudyDao.findDeptByIRBNum(irbNum));

			fundingSrcEle.setAttribute("entityid", "");

			fundingSrcEle.setAttribute("id", UUID.randomUUID().toString());

			// if funding not obtained
			if (crimsonStudyDao.findNofundingInfo(irbNum) != null) {

				fundingSrcEle.setAttribute("entitytype", "Cost Center");
				fundingSrcEle.setAttribute("projectid", "");
				fundingSrcEle.setAttribute("projectpi", "");
				fundingSrcEle.setAttribute("type", "None");
				fundingSrcEle.setAttribute("entityid",
						crimsonStudyDao.findNofundingCost(irbNum));
				fundingSrcEle.setAttribute("type",
						crimsonStudyDao.findNofundingFund(irbNum));
			}
			// funding existed
			else {

				// get title info
				try {
					fundingSrcEle.setAttribute("name", fundingTitles.get(i));
				} catch (Exception e) {
					e.printStackTrace();
				}

				if (fundingPartialInfos.get(i) == "0")
					fundingSrcEle.setAttribute("amount", "Partial");
				else if (fundingPartialInfos.get(i) == "1")
					fundingSrcEle.setAttribute("amount", "Full");
				else
					fundingSrcEle.setAttribute("amount", "");

				// add contacts
				Element fundingContactsEle = doc.createElement("contacts");
				fundingSrcEle.appendChild(fundingContactsEle);
				if (crimsonStudyDao
						.findfundingContactsPerson(fundingIDs.get(i)) != null) {
					for (int j = 0; j < crimsonStudyDao
							.findfundingContactsPerson(fundingIDs.get(i))
							.size(); j++) {
						Element fundingContactEle = doc
								.createElement("contact");
						fundingContactsEle.appendChild(fundingContactEle);
						fundingContactEle.setAttribute("name", crimsonStudyDao
								.findfundingContactsPerson(fundingIDs.get(i))
								.get(j));
						fundingContactEle.setAttribute("cell", crimsonStudyDao
								.findfundingContactsPhone(fundingIDs.get(i))
								.get(j));
						fundingContactEle.setAttribute("email", crimsonStudyDao
								.findfundingContactsEmail(fundingIDs.get(i))
								.get(j));
						fundingContactEle.setAttribute(
								"address",
								crimsonStudyDao.findfundingContactsAddress(
										fundingIDs.get(i)).get(j));
						fundingContactEle.setAttribute(
								"fax",
								crimsonStudyDao.findfundingContactsFax(
										fundingIDs.get(i)).get(j));
						fundingContactEle.setAttribute("title", crimsonStudyDao
								.findfundingContactsTitle(fundingIDs.get(i))
								.get(j));
						fundingContactEle.setAttribute("id", "" + j);
						fundingContactEle.setAttribute("role", "");
					}
				}
				// check the type of funding
				if (fundingTypes.get(i).equals("Funding Agency")) {
					fundingSrcEle.setAttribute("type", "External");

					fundingSrcEle.setAttribute("entityname", crimsonStudyDao
							.findSponsorNamebyID(fundingSposors.get(i)));

					fundingSrcEle.setAttribute("entitytype", "Agency");

				} else if (fundingTypes.get(i).equals("CRO")
						|| fundingTypes.get(i).equals("SMO")) {
					fundingSrcEle.setAttribute("type", fundingTypes.get(i));

					fundingSrcEle.setAttribute("entityname", crimsonStudyDao
							.findCRONamebyID(fundingSposors.get(i)));

					fundingSrcEle.setAttribute("entitytype", "Agency");

				} else if (fundingTypes.get(i).equals("Internal")) {
					fundingSrcEle.setAttribute("type", "Internal");

					fundingSrcEle.setAttribute("entityname", crimsonStudyDao
							.findInternalSrc(fundingSposors.get(i)));

					fundingSrcEle.setAttribute("entitytype", "Cost Center");

				} else if (fundingTypes.get(i).equals("ARIA Project")) {
					fundingSrcEle.setAttribute("type", "Project");

					fundingSrcEle.setAttribute("entityname", crimsonStudyDao
							.findProjectSponsor(fundingSposors.get(i)));

					fundingSrcEle.setAttribute("projectid",
							fundingSposors.get(i));

					fundingSrcEle.setAttribute("projectpi", crimsonStudyDao
							.findProjectPiName(fundingSposors.get(i)));

					fundingSrcEle.setAttribute("entitytype", "Agency");

				}

			}

		}

		return doc;
	}

	private Document processCrimsonDocument(Document doc)
			throws XPathExpressionException, IOException {
		NodeList protocolList = doc.getElementsByTagName("protocol");
		Element protocolEle = (Element) protocolList.item(0);
		String irbNum = protocolEle.getAttribute("id");

		List<Object[]> ctObjList = Lists.newArrayList();

		try {
			ctObjList = crimsonStudyDao.findAllCTObjectsByIRBNum(irbNum);
		} catch (Exception e) {

		}

		// crimson obejct[] null, no crimson for this study
		if (ctObjList.size() == 0) {
			return doc;
		}
		// add docuemnt to it
		else {
			Element crimsonRootEle = (Element) doc.getElementsByTagName(
					"crimson").item(0);

			// generate documents list for original and amendment
			Map<Integer, Element> documentsMap = new HashMap<Integer, Element>();
			for (int i = 0, j = 1; i < ctObjList.size(); i++) {
				if (ctObjList.get(i)[1] == null) {
					Element crimDocsEle = doc.createElement("documents");
					crimDocsEle.setAttribute("form", "original");
					crimsonRootEle.appendChild(crimDocsEle);
					documentsMap.put(0, crimDocsEle);
				} else {
					int parent = (Integer) ctObjList.get(i)[1];

					if (parent == 0) {
						Element crimDocsEle = doc.createElement("documents");
						crimDocsEle.setAttribute("form", "original");
						crimsonRootEle.appendChild(crimDocsEle);
						documentsMap.put(0, crimDocsEle);
					}
					if (parent > 0) {
						Element crimDocsEle = doc.createElement("documents");
						crimDocsEle.setAttribute("form", "amendment-" + j);
						crimsonRootEle.appendChild(crimDocsEle);
						documentsMap.put(j, crimDocsEle);
						j++;
					}
				}
			}

			for (int i = 0; i < ctObjList.size(); i++) {
				Element crimDocsEle = documentsMap.get(i);

				int ctID = (int) ctObjList.get(i)[0];
				List<Object[]> crimDocumentList = crimsonStudyDao
						.findAllDocumentsByCTID(ctID);

				// sort document list by parent first, then by version
				for (int j = 0; j < crimDocumentList.size(); j++) {
					for (int k = j + 1; k < crimDocumentList.size(); k++) {
						if ((Integer) crimDocumentList.get(j)[3] > (Integer) crimDocumentList
								.get(k)[3]) {
							Object[] tempObj = crimDocumentList.get(j);
							crimDocumentList.set(j, crimDocumentList.get(k));
							crimDocumentList.set(k, tempObj);
						}
					}
				}

				// second round sort
				for (int j = 0; j < crimDocumentList.size(); j++) {
					for (int k = j + 1; k < crimDocumentList.size(); k++) {
						int parent1 = (Integer) crimDocumentList.get(j)[3];
						int parent2 = (Integer) crimDocumentList.get(k)[3];
						if (parent1 == parent2) {
							if ((short) crimDocumentList.get(j)[2] < (short) crimDocumentList
									.get(k)[2]) {

								Object[] tempObj = crimDocumentList.get(j);
								crimDocumentList
										.set(j, crimDocumentList.get(k));
								crimDocumentList.set(k, tempObj);
							}
						}

					}
				}
				Map<Integer, Element> crimDocumentMap = new HashMap<Integer, Element>();

				for (int j = 0; j < crimDocumentList.size(); j++) {
					Object[] docObj = crimDocumentList.get(j);
					Element crimDocEle = doc.createElement("document");

					if (j == 0) {
						crimDocsEle.appendChild(crimDocEle);
						crimDocumentMap.put((Integer) docObj[3], crimDocEle);
					}
					if (j > 0) {
						Object[] docObjPre = crimDocumentList.get(j - 1);
						if ((short) docObjPre[2] == 0) {
							crimDocsEle.appendChild(crimDocEle);
							crimDocumentMap
									.put((Integer) docObj[3], crimDocEle);
						} else {
							try {
								crimDocumentMap.get((Integer) docObj[3])
										.appendChild(crimDocEle);
							} catch (Exception e) {
								crimDocsEle.appendChild(crimDocEle);
								crimDocumentMap.put((Integer) docObj[3],
										crimDocEle);

							}
						}

					}

					crimDocEle.setAttribute("id", "");
					if (docObj[0] != null) {
						crimDocEle.setAttribute("id", "" + (Integer) docObj[0]);
					}
					crimDocEle.setAttribute("title", "");
					if (docObj[1] != null) {
						crimDocEle.setAttribute("title", (String) docObj[1]);
					}
					crimDocEle.setAttribute("version", "");
					if (docObj[2] != null) {
						crimDocEle.setAttribute("version", ""
								+ (short) docObj[2]);
					}
					crimDocEle.setAttribute("parent", "");
					if (docObj[3] != null) {
						crimDocEle.setAttribute("parent", ""
								+ (Integer) docObj[3]);
					}
					crimDocEle.setAttribute("date", "");
					if (docObj[4] != null) {
						crimDocEle.setAttribute("date",
								((Date) docObj[4]).toString());
					}
					crimDocEle.setAttribute("type", "");
					if (docObj[5] != null) {
						short typeID = (short) docObj[5];
						crimDocEle.setAttribute("type",
								crimsonStudyDao.findDocumentTypeByID(typeID));
					}

					String existingFormData = "";
					try {
						existingFormData = protocolFormXmlDataDao
								.getLastProtocolFormXmlDataByProtocolIdAndType(
										Long.valueOf(irbNum),
										ProtocolFormXmlDataType.ARCHIVE)
								.getXmlData();
					} catch (Exception e) {
						existingFormData = "";
					}

					Document existtingFormXmlDataDoc = null;

					if (existingFormData.isEmpty()) {
						existtingFormXmlDataDoc = doc;
					} else {
						try {
							existtingFormXmlDataDoc = xmlProcessor
									.loadXmlStringToDOM(existingFormData);
						} catch (SAXException e) {
							existtingFormXmlDataDoc = null;
						}
					}

					String hashFileName = "";
					if (existtingFormXmlDataDoc != null) {
						XPath xPath = getXPathInstance();

						XPathExpression xPathExpression = null;

						xPathExpression = xPath
								.compile("//protocol/crimson/documents[@form='"
										+ crimDocsEle.getAttribute("form")
										+ "']/document[@id='"
										+ (Integer) docObj[0]
										+ "']/ariapath/text()");
						NodeList existingDocuemnts = (NodeList) xPathExpression
								.evaluate(existtingFormXmlDataDoc,
										XPathConstants.NODESET);

						if (existingDocuemnts.getLength() > 0) {
							hashFileName = existingDocuemnts.item(0)
									.getTextContent();
						} else {
							hashFileName = uploadCrimsonDocumenttoFileServer(
									"docs", (Integer) docObj[0],
									(String) docObj[6], irbNum);
						}
						Element ariapathEle = doc.createElement("ariapath");
						crimDocEle.appendChild(ariapathEle);
						ariapathEle.setTextContent(hashFileName);
					} else {
						hashFileName = uploadCrimsonDocumenttoFileServer(
								"docs", (Integer) docObj[0],
								(String) docObj[6], irbNum);
						Element ariapathEle = doc.createElement("ariapath");
						crimDocEle.appendChild(ariapathEle);
						ariapathEle.setTextContent(hashFileName);
					}
				}

			}
			return doc;
		}

	}

	private Document processPacketDocument(Document doc) throws IOException,
			XPathExpressionException {
		NodeList protocolList = doc.getElementsByTagName("protocol");
		Element protocolEle = (Element) protocolList.item(0);
		String irbNum = protocolEle.getAttribute("id");
		// doc obejct[] null, no packet for this study
		if (crimsonStudyDao.findAllPacketDocumentsByIRBNum(irbNum) == null) {
			return doc;
		} else {
			Element crimsonRootEle = doc.createElement("crimson");
			if ((Element) doc.getElementsByTagName("crimson").item(0) != null) {
				crimsonRootEle = (Element) doc.getElementsByTagName("crimson")
						.item(0);
			}
			protocolEle.appendChild(crimsonRootEle);

			logger.debug(irbNum);
			List<Object[]> ctObjList = crimsonStudyDao
					.findAllPacketDocumentsByIRBNum(irbNum);
			Element packetEle = doc.createElement("documents");
			packetEle.setAttribute("form", "packets");
			crimsonRootEle.appendChild(packetEle);
			// sort document list by parent first, then by version
			for (int j = 0; j < ctObjList.size(); j++) {
				for (int k = j + 1; k < ctObjList.size(); k++) {
					if ((Integer) ctObjList.get(j)[3] > (Integer) ctObjList
							.get(k)[3]) {
						Object[] tempObj = ctObjList.get(j);
						ctObjList.set(j, ctObjList.get(k));
						ctObjList.set(k, tempObj);
					}
				}
			}

			// second round sort
			for (int j = 0; j < ctObjList.size(); j++) {
				for (int k = j + 1; k < ctObjList.size(); k++) {
					int parent1 = (Integer) ctObjList.get(j)[3];
					int parent2 = (Integer) ctObjList.get(k)[3];
					if (parent1 == parent2) {
						if ((short) ctObjList.get(j)[2] < (short) ctObjList
								.get(k)[2]) {

							Object[] tempObj = ctObjList.get(j);
							ctObjList.set(j, ctObjList.get(k));
							ctObjList.set(k, tempObj);
						}
					}

				}
			}

			Map<Integer, Element> crimDocumentMap = new HashMap<Integer, Element>();
			for (int j = 0; j < ctObjList.size(); j++) {
				Element packetDocEle = doc.createElement("document");
				Object[] docObj = ctObjList.get(j);

				if (j == 0) {
					packetEle.appendChild(packetDocEle);
					crimDocumentMap.put((Integer) docObj[3], packetDocEle);
				}
				if (j > 0) {
					Object[] docObjPre = ctObjList.get(j - 1);
					short revisionID = 0;
					if (docObjPre[2] != null) {
						revisionID = (short) docObjPre[2];
					}
					if (revisionID == 0) {
						packetEle.appendChild(packetDocEle);
						crimDocumentMap.put((Integer) docObj[3], packetDocEle);
					} else {
						try {
							crimDocumentMap.get((Integer) docObj[3])
									.appendChild(packetDocEle);
						} catch (Exception e) {
							packetEle.appendChild(packetDocEle);
							crimDocumentMap.put((Integer) docObj[3],
									packetDocEle);

						}
					}

				}

				packetDocEle.setAttribute("id", "");
				if (docObj[0] != null) {
					packetDocEle.setAttribute("id", "" + (Integer) docObj[0]);
				}
				packetDocEle.setAttribute("title", "");
				if (docObj[1] != null) {
					packetDocEle.setAttribute("title", (String) docObj[1]);
				}
				packetDocEle.setAttribute("version", "");
				if (docObj[2] != null) {
					packetDocEle
							.setAttribute("version", "" + (short) docObj[2]);
				}
				packetDocEle.setAttribute("parent", "");
				if (docObj[3] != null) {
					packetDocEle.setAttribute("parent", ""
							+ (Integer) docObj[3]);
				}
				packetDocEle.setAttribute("date", "");
				if (docObj[4] != null) {
					packetDocEle.setAttribute("date",
							((Date) docObj[4]).toString());
				}
				packetDocEle.setAttribute("type", "");
				if (docObj[5] != null) {
					short typeID = (short) docObj[5];
					packetDocEle.setAttribute("type",
							crimsonStudyDao.findPacketDocumentTypeByID(typeID));
				}
				packetDocEle.setAttribute("is_postweb", "");
				if (docObj[7] != null) {
					short isPostweb = (short) docObj[7];
					if (isPostweb == 1) {
						packetDocEle.setAttribute("is_postweb", "y");
					} else if (isPostweb == 0) {
						packetDocEle.setAttribute("is_postweb", "n");
					}

				}

				String existingFormData = "";
				try {
					existingFormData = protocolFormXmlDataDao
							.getLastProtocolFormXmlDataByProtocolIdAndType(
									Long.valueOf(irbNum),
									ProtocolFormXmlDataType.ARCHIVE)
							.getXmlData();
				} catch (Exception e) {
					existingFormData = "";
				}

				Document existtingFormXmlDataDoc = null;

				if (existingFormData.isEmpty()) {
					existtingFormXmlDataDoc = doc;
				} else {
					try {
						existtingFormXmlDataDoc = xmlProcessor
								.loadXmlStringToDOM(existingFormData);
					} catch (SAXException e) {
					}
				}

				String hashFileName = "";
				if (existtingFormXmlDataDoc != null) {
					XPath xPath = getXPathInstance();

					XPathExpression xPathExpression = null;

					xPathExpression = xPath
							.compile("//protocol/crimson/documents[@form='"
									+ packetEle.getAttribute("form")
									+ "']/document[@id='" + (Integer) docObj[0]
									+ "']/ariapath/text()");
					NodeList existingDocuemnts = (NodeList) xPathExpression
							.evaluate(existtingFormXmlDataDoc,
									XPathConstants.NODESET);

					if (existingDocuemnts.getLength() > 0) {
						hashFileName = existingDocuemnts.item(0)
								.getTextContent();
					} else {
						hashFileName = uploadCrimsonDocumenttoFileServer(
								"postdocs", (Integer) docObj[0],
								(String) docObj[6], irbNum);
					}
					Element ariapathEle = doc.createElement("ariapath");
					packetDocEle.appendChild(ariapathEle);
					ariapathEle.setTextContent(hashFileName);
				} else {
					hashFileName = uploadCrimsonDocumenttoFileServer(
							"postdocs", (Integer) docObj[0],
							(String) docObj[6], irbNum);
					Element ariapathEle = doc.createElement("ariapath");
					packetDocEle.appendChild(ariapathEle);
					ariapathEle.setTextContent(hashFileName);
				}
			}
			return doc;
		}

	}

	private String uploadCrimsonDocumenttoFileServer(String path, int docID,
			String ext, String irbNum) {

		File fileDir = new File(localDirectory);
		if (!fileDir.exists())
			fileDir.mkdir();

		// copy the file from aria server to local
		String fileName = "X://" + path + "/" + docID + "." + ext;
		String hashFileName = "";
		String uploadfilename = "";
		try {
			messageDigest = MessageDigest.getInstance("SHA-256",
					new org.bouncycastle.jce.provider.BouncyCastleProvider());

			InputStream fileContent = new FileInputStream(fileName);

			byte[] bytes = IOUtils.toByteArray(fileContent);

			messageDigest.update(bytes);

			// get the hash of file content
			hashFileName = new String(Hex.encode(messageDigest.digest()));

			// write the file to local

			uploadfilename = localDirectory + "/" + hashFileName + "." + ext;

			FileOutputStream fout = new FileOutputStream(uploadfilename);
			fout.write(bytes);
			fout.flush();
			fout.close();

			// upload file to the server
			int trySFTP = 1;
			while (trySFTP > 0) {
				try {

					sFTPService.uploadLocalFileToRemote("protocol/" + irbNum
							+ "/" + hashFileName + "." + ext);

					trySFTP = 0;
				} catch (Exception e) {
					if (trySFTP < 5) {
						trySFTP++;
					}
					if (trySFTP == 5) {
						BufferedReader input = new BufferedReader(
								new FileReader("C:\\Data\\SFTPMissedList.txt"));
						String existData = "";
						String newData = "";
						while ((existData = input.readLine()) != null) {
							newData += existData + "\n";
						}
						input.close();
						newData += fileName;
						BufferedWriter output = new BufferedWriter(
								new FileWriter("C:\\Data\\SFTPMissedList.txt"));
						output.write(newData);
						output.close();
					}

				}
			}
			// delete the file after uploading...
			File uploadedFile = new File(uploadfilename);
			uploadedFile.delete();
		} catch (Exception e) {
		}

		// remove template dir
		fileDir.delete();

		// dev

		/*
		 * return "https://" + fileServerHost + "/files/dev/protocol/" + irbNum
		 * + "/" + hashFileName + "." + ext;
		 */

		// production

		return "https://" + fileServerHost + "/files/protocol/" + irbNum + "/"
				+ hashFileName + "." + ext;

	}

	// append letterback to it
	private Document appendExistingLetters(Document newDoc, Document existingDoc)
			throws XPathExpressionException {
		XPath xPath = getXPathInstance();

		XPathExpression xPathExpression = null;
		xPathExpression = xPath.compile("//protocol/letters");
		NodeList lettersnodes = (NodeList) xPathExpression.evaluate(
				existingDoc, XPathConstants.NODESET);
		if (lettersnodes.getLength() > 0) {
			Element protocolEleInNew = (Element) newDoc.getElementsByTagName(
					"protocol").item(0);
			Element lettersNodesNew = newDoc.createElement("letters");
			protocolEleInNew.appendChild(lettersNodesNew);
			NodeList letterList = ((Element) lettersnodes.item(0))
					.getElementsByTagName("letter");
			for (int i = 0; i < letterList.getLength(); i++) {
				Element oldLetterNode = (Element) letterList.item(i);
				Element letterNode = newDoc.createElement("letter");
				lettersNodesNew.appendChild(letterNode);

				letterNode.setAttribute("date",
						oldLetterNode.getAttribute("date"));
				letterNode.setAttribute("from",
						oldLetterNode.getAttribute("from"));
				letterNode.setAttribute("path",
						oldLetterNode.getAttribute("path"));
				letterNode.setAttribute("to", oldLetterNode.getAttribute("to"));
				letterNode.setAttribute("type",
						oldLetterNode.getAttribute("type"));

			}
		}
		return newDoc;

	}

	private List<ProtocolFormStatusEnum> protocolFormStatusLst = new ArrayList<ProtocolFormStatusEnum>();
	{
		protocolFormStatusLst.add(ProtocolFormStatusEnum.IRB_ACKNOWLEDGED);
		protocolFormStatusLst.add(ProtocolFormStatusEnum.IRB_APPROVED);
		protocolFormStatusLst.add(ProtocolFormStatusEnum.EXPEDITED_APPROVED);
	}

	private Set<String> metaDataPath = Sets.newHashSet();
	{
		metaDataPath.add("/protocol/original-study/submit-date");
		metaDataPath.add("/protocol/original-study/close-date");
		metaDataPath.add("/protocol/original-study/suspend-date");
		metaDataPath
				.add("/protocol/original-study/defer-with-minor-approval-date");
		metaDataPath.add("/protocol/original-study/approval-date");
		metaDataPath.add("/protocol/original-study/originalSubmissionDate");
		metaDataPath.add("/protocol/original-study/originalReviewDate");
		metaDataPath.add("/protocol/original-study/review-date");
		metaDataPath.add("/protocol/original-study/closeDate");
		metaDataPath.add("/protocol/original-study/HIPAAWaiverDate");
		metaDataPath.add("/protocol/original-study/approval-begin-date");
		metaDataPath.add("/protocol/original-study/IRBAgendaDate");
		metaDataPath.add("/protocol/most-recent-study/approval-end-date");
		metaDataPath.add("/protocol/most-recent-study/approval-date");
		metaDataPath.add("/protocol/most-recent-study/IRBReviewPeriod");
		metaDataPath.add("/protocol/most-recent-study/terminatedDate");
		metaDataPath.add("/protocol/most-recent-study/terminated-date");
		metaDataPath.add("/protocol/most-recent-study/suspendedDate");
		metaDataPath.add("/protocol/most-recent-study/suspend-date");
		metaDataPath.add("/protocol/most-recent-study/localSubjects");
		metaDataPath.add("/protocol/most-recent-study/totalSubjects");
		metaDataPath.add("/protocol/most-recent-study/approval-status");
		metaDataPath.add("/protocol/summary/irb-determination/review-period");
		metaDataPath.add("/protocol/summary/irb-determination/fda");
		metaDataPath.add("/protocol/summary/irb-determination/adult-risk");
		metaDataPath.add("/protocol/summary/irb-determination/ped-risk");
		metaDataPath.add("/protocol/summary/irb-determination/consent-waived");
		metaDataPath
				.add("/protocol/summary/irb-determination/consent-document-waived");
		metaDataPath
				.add("/protocol/summary/irb-determination/hipaa-not-applicable");
		metaDataPath.add("/protocol/summary/irb-determination/hipaa-waived");
		metaDataPath
				.add("/protocol/summary/irb-determination/suggested-next-review-type");
		metaDataPath.add("/protocol/summary/irb-determination/suggested-type");
		metaDataPath.add("/protocol/summary/irb-determination/finding");
		metaDataPath.add("/protocol/summary/irb-determination/finding-other");
		metaDataPath.add("/protocol/summary/irb-determination/reportable");
		metaDataPath.add("/protocol/summary/irb-determination/irb");
		metaDataPath.add("/protocol/summary/irb-determination/hipaa");
		metaDataPath.add("/protocol/summary/irb-determination/audit-type");
		metaDataPath.add("/protocol/summary/irb-determination/hipaa-finding");
		metaDataPath.add("/protocol/summary/irb-determination/irb-finding");
		metaDataPath.add("/protocol/summary/irb-determination/agenda-date");
		metaDataPath.add("/protocol/summary/irb-determination/recent-motion");
		metaDataPath
				.add("/protocol/summary/irb-determination/non-compliance-assessment");
		metaDataPath
				.add("/protocol/summary/irb-determination/reportable-to-ohrp");
		metaDataPath
				.add("/protocol/summary/irb-determination/hipaa-waived-date");
		metaDataPath.add("/protocol/lay-summary");
		metaDataPath.add("/protocol/inclusion-criteria");
		metaDataPath
				.add("/protocol/summary/budget-determination/approval-date");
	}

	public List<String> saveMegrateDataAsProtocol(String megrateXml,
			List<String> existedUsers) throws ParserConfigurationException,
			SAXException, IOException, XPathExpressionException {
		// get xpath
		XPath xPath = getXPathInstance();

		XPathExpression xPathExpression = null;

		// get doc for metadata string
		Document rawDoc = null;
		try {
			rawDoc = xmlProcessor.loadXmlStringToDOM(megrateXml);
		} catch (SAXException e) {
			e.printStackTrace();
		}
		// get protocol_identifier
		xPathExpression = xPath.compile("//protocol");
		NodeList protocolNodeLst = (NodeList) xPathExpression.evaluate(rawDoc,
				XPathConstants.NODESET);
		Element protocolEl = (Element) protocolNodeLst.item(0);

		int protocolId = Integer.valueOf(protocolEl.getAttribute("id"));
		long protcolIdLong = Long.valueOf(protocolEl.getAttribute("id"));
		// remove this part after rerun for crimson protocols

		/*
		 * only for crimson with budget Object[] crimsonCT = null; try {
		 * crimsonCT = crimsonStudyDao.findCTObjectbyIRBNum("" + protocolId); }
		 * catch (Exception e) { crimsonCT = null; } if (crimsonCT != null) {
		 * Object[] budgetObj = null; int ctID = (int) crimsonCT[14]; try {
		 * budgetObj = crimsonStudyDao.findBudgetByCTID(ctID); } catch
		 * (Exception e) { budgetObj = null; } if (budgetObj != null) {
		 * logger.debug("crimson study with budget");
		 */// /////////

		// only for crimson has packets
		/*
		 * if (crimsonStudyDao.findAllPacketDocumentsByIRBNum("" + protocolId)
		 * == null) { logger.debug("Do not having packets...."); return
		 * existedUsers; } else if
		 * (crimsonStudyDao.findAllPacketDocumentsByIRBNum( "" +
		 * protocolId).size() == 0) {
		 * logger.debug("Do not having packets 2...."); return existedUsers; }
		 */

		xPathExpression = xPath
				.compile("//protocol/review/ModificationDate/text()");
		NodeList modificationDateNodeLst = (NodeList) xPathExpression.evaluate(
				rawDoc, XPathConstants.NODESET);
		String dateString = "";
		if (modificationDateNodeLst.getLength() > 0) {
			dateString = modificationDateNodeLst.item(0).getNodeValue();
		}
		// get List of protooclMetaDataXml and protocolFormMetaDataXml
		List<String> metaXml = getProtoclMetaData(rawDoc, existedUsers);

		// process document part based on the generated strings
		Document docWotdocumentsInfo = null;
		try {

			docWotdocumentsInfo = xmlProcessor.loadXmlStringToDOM(metaXml
					.get(1));
		} catch (SAXException ex) {
			ex.printStackTrace();
		}
		// xPathExpression = xPath.compile("//protocol");
		// NodeList protocolNodes = (NodeList) xPathExpression.evaluate(rawDoc,
		// XPathConstants.NODESET);
		// Element protocolEle = (Element) protocolNodes.item(0);

		// add document info to the doc
		rawDoc = processDocumentInfo(rawDoc, docWotdocumentsInfo);
		rawDoc = procsessCrimensonInfo(rawDoc);
		rawDoc = processCrimsonDocument(rawDoc);
		rawDoc = processPacketDocument(rawDoc);
		rawDoc = processFundingInfo(rawDoc);
		// append existing letters
		String exisitingProtocolFormXmlDataString = "";

		try {
			exisitingProtocolFormXmlDataString = protocolFormXmlDataDao
					.getLastProtocolFormXmlDataByProtocolIdAndType(
							protcolIdLong, ProtocolFormXmlDataType.ARCHIVE)
					.getXmlData();
		} catch (Exception e) {
			exisitingProtocolFormXmlDataString = "";
		}

		Document existingDoc = null;

		if (exisitingProtocolFormXmlDataString.isEmpty()) {
			existingDoc = rawDoc;
		} else {
			try {
				existingDoc = xmlProcessor
						.loadXmlStringToDOM(exisitingProtocolFormXmlDataString);
			} catch (SAXException e) {
				e.printStackTrace();
			}
		}

		rawDoc = appendExistingLetters(rawDoc, existingDoc);

		String xmlForprotocolFormXmlData = DomUtils.elementToString(rawDoc,
				false, Encoding.UTF16);

		// save protocol

		Date createdDate = new Date();

		try {
			SimpleDateFormat dateFormat = new SimpleDateFormat("yy-MM-dd");
			createdDate = dateFormat.parse(dateString);
		} catch (ParseException ex) { // TODO Auto-generated catch block
			// ex.printStackTrace();
			DateFormat.getInstance().format(createdDate);
		}

		String protocolMetaData = metaXml.get(0);

		String protocolFormMetaData = metaXml.get(1);

		// check whether the protocol is moditifed
		Protocol protocol = null;
		try {
			protocol = protocolDao.findById(protcolIdLong);

			// This part is only for update protocol that has already been
			// migrated to CLRAR, only merge aproved forms
			String existingProtocolMetaData = protocol.getMetaDataXml();

			List<ProtocolFormType> formTypeList = new ArrayList<ProtocolFormType>();
			formTypeList.add(ProtocolFormType.MODIFICATION);
			formTypeList.add(ProtocolFormType.CONTINUING_REVIEW);
			formTypeList.add(ProtocolFormType.REPORTABLE_NEW_INFORMATION);
			formTypeList.add(ProtocolFormType.STUDY_CLOSURE);

			List<ProtocolFormStatusEnum> approvedFormStatusList = new ArrayList<ProtocolFormStatusEnum>();
			approvedFormStatusList.add(ProtocolFormStatusEnum.IRB_ACKNOWLEDGED);
			approvedFormStatusList.add(ProtocolFormStatusEnum.IRB_APPROVED);
			approvedFormStatusList.add(ProtocolFormStatusEnum.EXEMPT_APPROVED);
			approvedFormStatusList
					.add(ProtocolFormStatusEnum.EXPEDITED_APPROVED);

			String ariaStringForProtocol = metaXml.get(0);

			// get the latest form of all types using treeMap to sort it by
			// formid
			// create a tag to check whether there is approved continuing review
			int approvedCTN = 0;
			Map<Long, ProtocolForm> existingFormMap = new TreeMap<Long, ProtocolForm>();
			for (int i = 0; i < formTypeList.size(); i++) {
				ProtocolFormType protocolFormType = formTypeList.get(i);
				try {
					ProtocolForm protocolForm = protocolFormDao
							.getLatestProtocolFormByProtocolIdAndProtocolFormType(
									protocolId, protocolFormType);
					// get status
					long prfID = protocolForm.getFormId();
					ProtocolFormStatus prfStafus = protocolFormStatusDao
							.getLatestProtocolFormStatusByFormId(prfID);
					if (approvedFormStatusList.contains(prfStafus
							.getProtocolFormStatus())) {
						existingFormMap.put(prfID, protocolForm);
						if (protocolFormType
								.equals(ProtocolFormType.CONTINUING_REVIEW)) {
							approvedCTN = 1;
						}
					}
				} catch (Exception e) {
				}
			}

			if (existingFormMap != null && existingFormMap.size() > 0) {
				// sort the existingFormListByFormId
				for (Long formID : existingFormMap.keySet()) {
					ProtocolForm protocolForm = existingFormMap.get(formID);
					Map<String, String> ProtocolFromToProtocolMetaDataMapping = ProtocolMetaDataXmlService
							.getProtocolFromToProtocolMetaDataMapping(protocolForm
									.getProtocolFormType()
									.getDefaultProtocolFormXmlDataType());
					String formXmlData = protocolForm.getMetaDataXml();
					// logger.debug("$$$ origin:" +ariaStringForProtocol);
					// logger.debug("%%% modify: " + formXmlData);
					ariaStringForProtocol = xmlProcessor.mergeByXPaths(
							ariaStringForProtocol, formXmlData,
							XmlProcessor.Operation.UPDATE_IF_EXIST,
							ProtocolFromToProtocolMetaDataMapping);
				}
			}

			// if there are approved continue reviewing, copy dates info in
			// existing protocol meta data
			if (approvedCTN == 1) {
				Map<String, List<String>> resultMap = xmlProcessor
						.listElementStringValuesByPaths(this.metaDataPath,
								existingProtocolMetaData);

				for (Entry<String, List<String>> entry : resultMap.entrySet()) {
					if (entry.getValue() != null && !entry.getValue().isEmpty()) {
						logger.debug("path: " + entry.getKey() + " value: "
								+ entry.getValue().get(0));
						ariaStringForProtocol = xmlProcessor
								.replaceOrAddNodeValueByPath(entry.getKey(),
										ariaStringForProtocol, entry.getValue()
												.get(0));
					}
				}
			}

			protocol.setMetaDataXml(ariaStringForProtocol);

			protocolDao.saveOrUpdate(protocol);

		} catch (Exception e) {
			e.printStackTrace();
		}

		if (protocol == null) {
			migrationDao.insertProtocol(protocolId, 0, createdDate,
					protocolMetaData, protocolEl.getAttribute("id"));
		}

		// save protocolStatus
		// if (modifiedTag == 0) {

		// }
		protocol = protocolDao.findById(protcolIdLong);

		String status = protocolEl.getAttribute("status");
		ProtocolStatus protocolStatus = null;

		try {
			if (status.equals("Open")) {
				protocolStatus = protocolStatusDao
						.findProtocolStatusByStatusAndProtocolId(protocolId,
								ProtocolStatusEnum.OPEN);

			} else if (status.equals("Closed")) {
				protocolStatus = protocolStatusDao
						.findProtocolStatusByStatusAndProtocolId(protocolId,
								ProtocolStatusEnum.CLOSED);

			} else if (status.equals("Terminated")) {
				protocolStatus = protocolStatusDao
						.findProtocolStatusByStatusAndProtocolId(protocolId,
								ProtocolStatusEnum.TERMINATED);

			} else if (status.equals("Declined")) {
				protocolStatus = protocolStatusDao
						.findProtocolStatusByStatusAndProtocolId(protocolId,
								ProtocolStatusEnum.DECLINED);
			}

		} catch (Exception ex) {
			// protocolStatus = new ProtocolStatus();
		}

		ProtocolStatus protocolStatus2 = null;
		try {
			protocolStatus2 = protocolStatusDao
					.findProtocolStatusByStatusAndProtocolId(protocolId,
							ProtocolStatusEnum.ARCHIVED);
		} catch (Exception ex) {
			// protocolStatus2 = new ProtocolStatus();
		}

		if (protocolStatus == null && protocolStatus2 == null) {
			protocolStatus = new ProtocolStatus();
		} else if (protocolStatus == null && protocolStatus2 != null) {
			protocolStatus = protocolStatus2;
		}

		protocolStatus.setModified(createdDate);
		if (status.equals("Open")) {
			protocolStatus.setProtocolStatus(ProtocolStatusEnum.OPEN);
		} else if (status.equals("Closed")) {
			protocolStatus.setProtocolStatus(ProtocolStatusEnum.CLOSED);
		} else if (status.equals("Terminated")) {
			protocolStatus.setProtocolStatus(ProtocolStatusEnum.TERMINATED);
		} else if (status.equals("Declined")) {
			protocolStatus.setProtocolStatus(ProtocolStatusEnum.DECLINED);
		}
		protocolStatus.setProtocol(protocol);
		protocolStatus.setCausedByCommittee(Committee.PI);
		protocolStatus.setCauseByUser(userDao.findById(73));

		protocolStatusDao.saveOrUpdate(protocolStatus);

		try {
			String updatedStatusMetaData = xmlProcessor
					.replaceOrAddNodeValueByPath("", protocol.getMetaDataXml(),
							org.apache.commons.lang.StringEscapeUtils
									.escapeXml(protocolStatus
											.getProtocolStatus()
											.getDescription()));

			Map<String, String> attributes = new HashMap<String, String>();
			attributes.put("priority", protocolStatus.getProtocolStatus()
					.getPriorityLevel());

			updatedStatusMetaData = xmlProcessor.addAttributesByPath(
					"/protocol/status", updatedStatusMetaData, attributes);

			protocol.setMetaDataXml(updatedStatusMetaData);
			protocolDao.saveOrUpdate(protocol);
		} catch (Exception e) {
			// don't care
		}
		// save protocolForm
		ProtocolForm protocolForm = null;
		try {
			protocolForm = protocolFormDao
					.getLatestProtocolFormByProtocolIdAndProtocolFormType(
							protocolId, ProtocolFormType.ARCHIVE);
		} catch (Exception ex) {
			logger.warn("No existing ProtocolForm, create one ...");
			protocolForm = new ProtocolForm();
		}

		protocolForm.setCreated(createdDate);
		protocolForm.setLocked(false);
		protocolForm.setRetired(false);
		protocolForm.setProtocol(protocol);
		protocolForm.setParent(protocolForm);
		protocolForm.setMetaDataXml(protocolFormMetaData);
		protocolForm.setProtocolFormType(ProtocolFormType.ARCHIVE);

		protocolFormDao.saveOrUpdate(protocolForm);

		// save protocolFormStatus

		ProtocolFormStatus protocolFormStatus = null;
		try {
			protocolFormStatus = protocolFormStatusDao
					.getProtocolFormStatusByFormIdAndProtocolFormStatus(
							protocolForm.getFormId(),
							ProtocolFormStatusEnum.ARCHIVED);
		} catch (Exception ex) {
			logger.warn("No existing ProtocolFormStatus, create one ...");
			protocolFormStatus = new ProtocolFormStatus();
		}

		protocolFormStatus.setModified(createdDate);
		protocolFormStatus
				.setProtocolFormStatus(ProtocolFormStatusEnum.ARCHIVED);
		protocolFormStatus.setProtocolForm(protocolForm);
		protocolFormStatus.setCausedByCommittee(Committee.PI);
		protocolFormStatus.setCauseByUser(userDao.findById(73));
		protocolFormStatusDao.saveOrUpdate(protocolFormStatus);

		// save protocolFormXmlData
		ProtocolFormXmlData protocolFormXmlData = null;
		try {
			protocolFormXmlData = protocolFormXmlDataDao
					.getLastProtocolFormXmlDataByProtocolFormIdAndType(
							protocolForm.getFormId(),
							ProtocolFormXmlDataType.ARCHIVE);
		} catch (Exception ex) {
			logger.warn("No existing ProtocolFormXmlData, create one ...");
			protocolFormXmlData = new ProtocolFormXmlData();
		}

		protocolFormXmlData.setCreated(createdDate);
		protocolFormXmlData.setParent(protocolFormXmlData);
		protocolFormXmlData.setProtocolForm(protocolForm);
		protocolFormXmlData
				.setProtocolFormXmlDataType(ProtocolFormXmlDataType.ARCHIVE);
		protocolFormXmlData.setRetired(false);
		xmlForprotocolFormXmlData = xmlForprotocolFormXmlData.replace("&#", "");
		protocolFormXmlData.setXmlData(xmlForprotocolFormXmlData);
		protocolFormXmlDataDao.saveOrUpdate(protocolFormXmlData);

		// save track history

		Track track = new Track();
		try {
			track = trackDao.getTrackByTypeAndRefObjectID("PROTOCOL",
					protocolId);
		} catch (Exception e) {
			// e.printStackTrace();
			track = null;
		}
		if (track == null) {
			track = new Track();
			// }
			Date logDate = new Date();
			track.setModified(logDate);
			track.setRetired(false);
			track.setType("PROTOCOL");
			track.setRefObjectClass(Protocol.class);
			track.setRefObjectId(protocolId);

			Document loginfo = null;
			loginfo = xmlProcessor.getDocumentBuilder().newDocument();
			Element logsEle = loginfo.createElement("logs");
			logsEle.setAttribute("object-id", protocolEl.getAttribute("id"));
			logsEle.setAttribute("object-type", "Protocol");
			Element logEle = loginfo.createElement("log");
			logEle.setAttribute("action-user-id", "73");
			logEle.setAttribute("actor", "Yuan, Jiawei");

			logEle.setAttribute("date-time", DateFormat.getInstance().format(logDate));
			logEle.setAttribute("event-type", "" + "ARCHIVE");
			logEle.setTextContent("Archive protocol #"
					+ protocolEl.getAttribute("id") + " is imported by "
					+ userDao.findById(73).getUsername());
			logsEle.appendChild(logEle);
			loginfo.appendChild(logsEle);
			String logXml = DomUtils.elementToString(loginfo, false,
					Encoding.UTF16);
			track.setXmlData(logXml);

			trackDao.saveOrUpdate(track);
		}
		// logger.debug(protocolId+"");
		/*
		 * } catch (Exception e) { e.printStackTrace(); }
		 */

		return existedUsers;

	}

	public XmlProcessor getXmlProcessor() {
		return xmlProcessor;
	}

	@Autowired(required = true)
	public void setXmlProcessor(XmlProcessor xmlProcessor) {
		this.xmlProcessor = xmlProcessor;
	}

	public ProtocolDao getProtocolDao() {
		return protocolDao;
	}

	@Autowired(required = true)
	public void setProtocolDao(ProtocolDao protocolDao) {
		this.protocolDao = protocolDao;
	}

	public ProtocolFormDao getProtocolFormDao() {
		return protocolFormDao;
	}

	@Autowired(required = true)
	public void setProtocolFormDao(ProtocolFormDao protocolFormDao) {
		this.protocolFormDao = protocolFormDao;
	}

	public ProtocolFormXmlDataDao getProtocolFormXmlDataDao() {
		return protocolFormXmlDataDao;
	}

	@Autowired(required = true)
	public void setProtocolFormXmlDataDao(
			ProtocolFormXmlDataDao protocolFormXmlDataDao) {
		this.protocolFormXmlDataDao = protocolFormXmlDataDao;
	}

	public ProtocolStatusDao getProtocolStatusDao() {
		return protocolStatusDao;
	}

	@Autowired(required = true)
	public void setProtocolStatusDao(ProtocolStatusDao protocolStatusDao) {
		this.protocolStatusDao = protocolStatusDao;
	}

	public ProtocolFormStatusDao getProtocolFormStatusDao() {
		return protocolFormStatusDao;
	}

	@Autowired(required = true)
	public void setProtocolFormStatusDao(
			ProtocolFormStatusDao protocolFormStatusDao) {
		this.protocolFormStatusDao = protocolFormStatusDao;
	}

	public UserDao getUserDao() {
		return userDao;
	}

	@Autowired(required = true)
	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}

	public TrackDao getTrackDao() {
		return trackDao;
	}

	@Autowired(required = true)
	public void setTrackDao(TrackDao trackDao) {
		this.trackDao = trackDao;
	}

	public CrimsonStudyDao getcrimsonStudyDao() {
		return crimsonStudyDao;
	}

	@Autowired(required = true)
	public void setcrimsonStudyDao(CrimsonStudyDao crimsonStudyDao) {
		this.crimsonStudyDao = crimsonStudyDao;
	}

	public SFTPService getsFTPService() {
		return sFTPService;
	}

	@Autowired(required = true)
	public void setsFTPService(SFTPService sFTPService) {
		this.sFTPService = sFTPService;
	}

	public String getLocalDirectory() {
		return localDirectory;
	}

	public void setLocalDirectory(String localDirectory) {
		this.localDirectory = localDirectory;
	}

	public String getFileServerHost() {
		return fileServerHost;
	}

	public void setFileServerHost(String fileServerHost) {
		this.fileServerHost = fileServerHost;
	}

	@Override
	public XPathFactory getXpathFactory() {
		return xpathFactory;
	}

	public void setXpathFactory(XPathFactory xpathFactory) {
		this.xpathFactory = xpathFactory;
	}

	public PersonDao getPersonDao() {
		return personDao;
	}

	@Autowired(required = true)
	public void setPersonDao(PersonDao personDao) {
		this.personDao = personDao;
	}

	public UserServiceImpl getUserServiceImpl() {
		return userServiceImpl;
	}

	@Autowired(required = true)
	public void setUserServiceImpl(UserServiceImpl userServiceImpl) {
		this.userServiceImpl = userServiceImpl;
	}

	public CrimsonContractDao getCrimsonContractDao() {
		return crimsonContractDao;
	}

	@Autowired(required = true)
	public void setCrimsonContractDao(CrimsonContractDao crimsonContractDao) {
		this.crimsonContractDao = crimsonContractDao;
	}

	public ProtocolMetaDataXmlServiceImpl getProtocolMetaDataXmlService() {
		return ProtocolMetaDataXmlService;
	}

	@Autowired(required = true)
	public void setProtocolMetaDataXmlService(
			ProtocolMetaDataXmlServiceImpl protocolMetaDataXmlService) {
		ProtocolMetaDataXmlService = protocolMetaDataXmlService;
	}

	public MigrationDao getMigrationDao() {
		return migrationDao;
	}

	@Autowired(required = true)
	public void setMigrationDao(MigrationDao migrationDao) {
		this.migrationDao = migrationDao;
	}

	public XmlHandler getXmlHandler() {
		return xmlHandler;
	}

	@Autowired(required = true)
	public void setXmlHandler(XmlHandler xmlHandler) {
		this.xmlHandler = xmlHandler;
	}

}
