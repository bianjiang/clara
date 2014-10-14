package edu.uams.clara.webapp.maintainence;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.beanutils.BeanUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import edu.emory.mathcs.backport.java.util.Collections;
import edu.uams.clara.core.util.xml.XmlHandler;
import edu.uams.clara.core.util.xml.XmlHandlerFactory;
import edu.uams.clara.integration.incoming.billingcodes.domain.HospitalChargeUpdate;
import edu.uams.clara.webapp.common.dao.audit.AuditDao;
import edu.uams.clara.webapp.common.dao.department.CollegeDao;
import edu.uams.clara.webapp.common.dao.department.DepartmentDao;
import edu.uams.clara.webapp.common.domain.department.College;
import edu.uams.clara.webapp.common.domain.department.Department;
import edu.uams.clara.webapp.common.domain.usercontext.enums.Committee;
import edu.uams.clara.webapp.common.util.DateFormatUtil;
import edu.uams.clara.webapp.contract.dao.businesslogicobject.ContractFormCommitteeStatusDao;
import edu.uams.clara.webapp.contract.dao.contractform.ContractFormDao;
import edu.uams.clara.webapp.contract.domain.businesslogicobject.ContractFormCommitteeStatus;
import edu.uams.clara.webapp.contract.domain.businesslogicobject.enums.ContractFormCommitteeStatusEnum;
import edu.uams.clara.webapp.contract.domain.contractform.enums.ContractFormType;
import edu.uams.clara.webapp.protocol.dao.ProtocolDao;
import edu.uams.clara.webapp.protocol.dao.irb.AgendaDao;
import edu.uams.clara.webapp.protocol.dao.protocolform.ProtocolFormDao;
import edu.uams.clara.webapp.protocol.dao.protocolform.ProtocolFormXmlDataDao;
import edu.uams.clara.webapp.protocol.domain.Protocol;
import edu.uams.clara.webapp.protocol.domain.budget.code.HospitalChargeProcedure;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.AgendaStatus;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.ProtocolFormCommitteeStatus;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.ProtocolFormStatus;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.ProtocolStatus;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.enums.AgendaStatusEnum;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.enums.ProtocolFormCommitteeStatusEnum;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.enums.ProtocolFormStatusEnum;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.enums.ProtocolStatusEnum;
import edu.uams.clara.webapp.protocol.domain.irb.AgendaItem.AgendaItemStatus;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolForm;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolFormXmlData;
import edu.uams.clara.webapp.protocol.domain.protocolform.enums.ProtocolFormType;
import edu.uams.clara.webapp.protocol.domain.protocolform.enums.ProtocolFormXmlDataType;
import edu.uams.clara.webapp.xml.processor.XmlProcessor;
import edu.uams.clara.webapp.protocol.dao.budget.code.HospitalChargeProcedureDao;
import edu.uams.clara.webapp.protocol.dao.businesslogicobject.AgendaStatusDao;
import edu.uams.clara.webapp.protocol.dao.businesslogicobject.ProtocolFormCommitteeStatusDao;
import edu.uams.clara.webapp.protocol.dao.businesslogicobject.ProtocolFormStatusDao;
import edu.uams.clara.webapp.protocol.dao.businesslogicobject.ProtocolStatusDao;
import edu.uams.clara.webapp.report.domain.CommitteeActions;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "file:src/test/java/edu/uams/clara/webapp/maintainence/ReportGenerationTest-context.xml" })
public class ReportGenerationTest {
	private final static Logger logger = LoggerFactory
			.getLogger(ReportGenerationTest.class);
	private ProtocolFormDao protocolFormDao;
	private ProtocolFormXmlDataDao protocolFormXmlDataDao;
	private ProtocolDao protocolDao;
	private ProtocolFormStatusDao protocolFormStatusDao;
	private ProtocolFormCommitteeStatusDao protocolFormCommitteeStatusDao;
	private ContractFormDao contractFormDao;
	private ContractFormCommitteeStatusDao contractFormCommitteeStatusDao;
	private AgendaDao agendaDao;
	private ProtocolStatusDao protocolStatusDao;
	private AuditDao auditDao;
	private HospitalChargeProcedureDao hospitalChargeProcedureDao;

	private XmlProcessor xmlProcessor;
	private XmlHandler xmlHandler;
	private EntityManager em;
	private DepartmentDao departmentDao;
	private AgendaStatusDao agendaStatusDao;
	private CollegeDao collegeDao;
	private CommitteeActions committeeactions = new CommitteeActions();

	private List<ProtocolFormStatusEnum> draftFormStatus = Lists.newArrayList();
	{
		draftFormStatus.add(ProtocolFormStatusEnum.DRAFT);
		draftFormStatus.add(ProtocolFormStatusEnum.PENDING_PI_ENDORSEMENT);
		draftFormStatus.add(ProtocolFormStatusEnum.PENDING_PL_ENDORSEMENT);
		draftFormStatus.add(ProtocolFormStatusEnum.PENDING_TP_ENDORSEMENT);
	}
	
	private List<ProtocolFormStatusEnum> completeFormStatus = Lists.newArrayList();
	{
		completeFormStatus.add(ProtocolFormStatusEnum.ACKNOWLEDGED);
		completeFormStatus.add(ProtocolFormStatusEnum.APPROVED);
		completeFormStatus.add(ProtocolFormStatusEnum.COMPLETED);
		completeFormStatus.add(ProtocolFormStatusEnum.EXEMPT_APPROVED);
		completeFormStatus.add(ProtocolFormStatusEnum.EXPEDITED_APPROVED);
		completeFormStatus.add(ProtocolFormStatusEnum.IRB_ACKNOWLEDGED);
		completeFormStatus.add(ProtocolFormStatusEnum.IRB_APPROVED);
		completeFormStatus.add(ProtocolFormStatusEnum.IRB_DECLINED);
		completeFormStatus.add(ProtocolFormStatusEnum.DETERMINED_HUMAN_SUBJECT_RESEARCH);
		completeFormStatus.add(ProtocolFormStatusEnum.DETERMINED_NOT_HUMAN_SUBJECT_RESEARCH);
		completeFormStatus.add(ProtocolFormStatusEnum.DETERMINED_NOT_REPORTABLE_NEW_INFORMATION);
	}
	
	private Map<Committee, List<ContractFormCommitteeStatusEnum>> contractStartCommitteeStatusMap = Maps
			.newHashMap();
	{
		// contractAdmin
		List<ContractFormCommitteeStatusEnum> contractAdminList = Lists
				.newArrayList();
		contractAdminList.add(ContractFormCommitteeStatusEnum.IN_REVIEW);
		contractStartCommitteeStatusMap.put(Committee.CONTRACT_ADMIN,
				contractAdminList);

		// contractLegal
		List<ContractFormCommitteeStatusEnum> contractLegalList = Lists
				.newArrayList();
		contractLegalList.add(ContractFormCommitteeStatusEnum.IN_REVIEW);
		contractStartCommitteeStatusMap.put(Committee.CONTRACT_LEGAL_REVIEW,
				contractLegalList);
	}

	private Map<Committee, List<ContractFormCommitteeStatusEnum>> contractPendingCommitteeStatusMap = Maps
			.newHashMap();
	{
		// contractAdmin
		List<ContractFormCommitteeStatusEnum> contractAdminList = Lists
				.newArrayList();
		contractAdminList.add(ContractFormCommitteeStatusEnum.OUTSIDE_IRB);
		contractAdminList.add(ContractFormCommitteeStatusEnum.PENDING_BUDGET);
		contractAdminList.add(ContractFormCommitteeStatusEnum.PENDING_COVERAGE);
		contractAdminList.add(ContractFormCommitteeStatusEnum.PENDING_IRB);
		contractAdminList.add(ContractFormCommitteeStatusEnum.PENDING_PI);
		contractAdminList
				.add(ContractFormCommitteeStatusEnum.PENDING_SIGNATURE);
		contractAdminList
				.add(ContractFormCommitteeStatusEnum.PENDING_SPONSOR_RESPONSE);
		contractPendingCommitteeStatusMap.put(Committee.CONTRACT_ADMIN,
				contractAdminList);

		// contractLegal
		List<ContractFormCommitteeStatusEnum> contractLegalList = Lists
				.newArrayList();
		contractLegalList.add(ContractFormCommitteeStatusEnum.OUTSIDE_IRB);
		contractLegalList.add(ContractFormCommitteeStatusEnum.PENDING_BUDGET);
		contractLegalList.add(ContractFormCommitteeStatusEnum.PENDING_COVERAGE);
		contractLegalList.add(ContractFormCommitteeStatusEnum.PENDING_IRB);
		contractLegalList.add(ContractFormCommitteeStatusEnum.PENDING_PI);
		contractLegalList
				.add(ContractFormCommitteeStatusEnum.PENDING_REVIEWER_ASSIGNMENT);
		contractLegalList
				.add(ContractFormCommitteeStatusEnum.PENDING_SPONSOR_RESPONSE);
		contractPendingCommitteeStatusMap.put(Committee.CONTRACT_LEGAL_REVIEW,
				contractLegalList);
	}

	private Map<Committee, List<ContractFormCommitteeStatusEnum>> contractEndCommitteeStatusMap = Maps
			.newHashMap();
	{
		// contractAdmin
		List<ContractFormCommitteeStatusEnum> contractAdminList = Lists
				.newArrayList();
		contractAdminList.add(ContractFormCommitteeStatusEnum.APPROVED);
		contractAdminList.add(ContractFormCommitteeStatusEnum.COMPLETED);
		contractAdminList
				.add(ContractFormCommitteeStatusEnum.REVISION_REQUESTED);
		contractAdminList
				.add(ContractFormCommitteeStatusEnum.FINAL_LEGAL_APPROVAL);
		contractEndCommitteeStatusMap.put(Committee.CONTRACT_ADMIN,
				contractAdminList);

		// contractLegal
		List<ContractFormCommitteeStatusEnum> contractLegalList = Lists
				.newArrayList();
		contractLegalList.add(ContractFormCommitteeStatusEnum.APPROVED);
		contractLegalList.add(ContractFormCommitteeStatusEnum.COMPLETED);
		contractEndCommitteeStatusMap.put(Committee.CONTRACT_LEGAL_REVIEW,
				contractLegalList);
	}

	
	private Map<Committee, List<ProtocolFormCommitteeStatusEnum>> startCommitteeStatusMap = Maps
			.newHashMap();
	{
		// Expedited
		List<ProtocolFormCommitteeStatusEnum> expeditedList = Lists
				.newArrayList();
		expeditedList
				.add(ProtocolFormCommitteeStatusEnum.PENDING_EXPEDITED_IRB_REVIEW);
		startCommitteeStatusMap.put(Committee.IRB_EXPEDITED_REVIEWER,
				expeditedList);

		// Exempt
		List<ProtocolFormCommitteeStatusEnum> exemptList = Lists.newArrayList();
		exemptList
				.add(ProtocolFormCommitteeStatusEnum.PENDING_EXEMPT_IRB_REVIEW);
		startCommitteeStatusMap.put(Committee.IRB_EXEMPT_REVIEWER, exemptList);
		
		// irbReview
		List<ProtocolFormCommitteeStatusEnum> irbReviewList = Lists.newArrayList();
		irbReviewList
						.add(ProtocolFormCommitteeStatusEnum.IN_REVIEW);
				startCommitteeStatusMap.put(Committee.IRB_REVIEWER, irbReviewList);

		// irbOffice
		List<ProtocolFormCommitteeStatusEnum> irbOfficeList = Lists
				.newArrayList();
		irbOfficeList
				.add(ProtocolFormCommitteeStatusEnum.PENDING_IRB_REVIEW_ASSIGNMENT);
		irbOfficeList
				.add(ProtocolFormCommitteeStatusEnum.PENDING_IRB_REVIEW_RE_ASSIGNMENT);
		irbOfficeList
		.add(ProtocolFormCommitteeStatusEnum.IRB_AGENDA_ASSIGNED);
		irbOfficeList
		.add(ProtocolFormCommitteeStatusEnum.IN_REVIEW);
		startCommitteeStatusMap.put(Committee.IRB_OFFICE, irbOfficeList);

		// irbPrereview
		List<ProtocolFormCommitteeStatusEnum> irbPrereviewList = Lists
				.newArrayList();
		irbPrereviewList.add(ProtocolFormCommitteeStatusEnum.IN_REVIEW);
		startCommitteeStatusMap.put(Committee.IRB_PREREVIEW, irbPrereviewList);

		// irbConsentReview
		List<ProtocolFormCommitteeStatusEnum> irbConsentReviewList = Lists
				.newArrayList();
		irbConsentReviewList.add(ProtocolFormCommitteeStatusEnum.IN_REVIEW);
		startCommitteeStatusMap.put(Committee.IRB_CONSENT_REVIEWER,
				irbConsentReviewList);

		// irbAssigner
		List<ProtocolFormCommitteeStatusEnum> irbAssignerList = Lists
				.newArrayList();
		irbAssignerList
				.add(ProtocolFormCommitteeStatusEnum.PENDING_REVIEWER_ASSIGNMENT);
		startCommitteeStatusMap.put(Committee.IRB_ASSIGNER, irbAssignerList);

		// budgetReview
		List<ProtocolFormCommitteeStatusEnum> budgetReviewList = Lists
				.newArrayList();
		budgetReviewList.add(ProtocolFormCommitteeStatusEnum.IN_REVIEW);
		startCommitteeStatusMap.put(Committee.BUDGET_REVIEW, budgetReviewList);

		// budgetManager
		List<ProtocolFormCommitteeStatusEnum> budgetManagerList = Lists
				.newArrayList();
		budgetManagerList.add(ProtocolFormCommitteeStatusEnum.IN_REVIEW);
		budgetManagerList.add(ProtocolFormCommitteeStatusEnum.PENDING_REVIEWER_ASSIGNMENT);
		startCommitteeStatusMap
				.put(Committee.BUDGET_MANAGER, budgetManagerList);

		// coverageReview
		List<ProtocolFormCommitteeStatusEnum> coverageReviewList = Lists
				.newArrayList();
		coverageReviewList.add(ProtocolFormCommitteeStatusEnum.IN_REVIEW);
		startCommitteeStatusMap.put(Committee.COVERAGE_REVIEW,
				coverageReviewList);

		// pharmacyReview
		List<ProtocolFormCommitteeStatusEnum> pharmacyReviewList = Lists
				.newArrayList();
		pharmacyReviewList
				.add(ProtocolFormCommitteeStatusEnum.IN_REVIEW_REQUESTED);
		pharmacyReviewList
				.add(ProtocolFormCommitteeStatusEnum.IN_WAIVER_REQUESTED);
		startCommitteeStatusMap.put(Committee.PHARMACY_REVIEW,
				pharmacyReviewList);

		// hospitalReview
		List<ProtocolFormCommitteeStatusEnum> hospitalReviewList = Lists
				.newArrayList();
		hospitalReviewList.add(ProtocolFormCommitteeStatusEnum.IN_REVIEW);
		startCommitteeStatusMap.put(Committee.HOSPITAL_SERVICES,
				hospitalReviewList);

		// colleageDean
		List<ProtocolFormCommitteeStatusEnum> colleageDeanList = Lists
				.newArrayList();
		colleageDeanList.add(ProtocolFormCommitteeStatusEnum.IN_REVIEW);
		startCommitteeStatusMap.put(Committee.COLLEGE_DEAN, colleageDeanList);

		// departmentChair
		List<ProtocolFormCommitteeStatusEnum> departmentChairList = Lists
				.newArrayList();
		departmentChairList.add(ProtocolFormCommitteeStatusEnum.IN_REVIEW);
		startCommitteeStatusMap.put(Committee.DEPARTMENT_CHAIR,
				departmentChairList);

		// gatekeeper
		List<ProtocolFormCommitteeStatusEnum> gatekeeperList = Lists
				.newArrayList();
		gatekeeperList.add(ProtocolFormCommitteeStatusEnum.IN_REVIEW);
		startCommitteeStatusMap.put(Committee.GATEKEEPER, gatekeeperList);

		// COMPLIANCE_REVIEW
		List<ProtocolFormCommitteeStatusEnum> complianceList = Lists
				.newArrayList();
		complianceList
				.add(ProtocolFormCommitteeStatusEnum.IN_REVIEW_FOR_BUDGET);
		complianceList.add(ProtocolFormCommitteeStatusEnum.IN_REVIEW);
		startCommitteeStatusMap
				.put(Committee.COMPLIANCE_REVIEW, complianceList);
		
		// achri
		List<ProtocolFormCommitteeStatusEnum> achriList = Lists
								.newArrayList();
		achriList.add(ProtocolFormCommitteeStatusEnum.IN_REVIEW);
		startCommitteeStatusMap.put(Committee.ACHRI, achriList);

	}
	private Map<Committee, List<ProtocolFormCommitteeStatusEnum>> endCommitteeStatusMap = Maps
			.newHashMap();
	{
		List<ProtocolFormCommitteeStatusEnum> expeditedList = Lists
				.newArrayList();
		expeditedList.add(ProtocolFormCommitteeStatusEnum.APPROVED);
		expeditedList
				.add(ProtocolFormCommitteeStatusEnum.NOT_APPLICABLE_TO_EXPEDITED_IRB_REVIEW);
		expeditedList.add(ProtocolFormCommitteeStatusEnum.REVISION_REQUESTED);
		endCommitteeStatusMap.put(Committee.IRB_EXPEDITED_REVIEWER,
				expeditedList);

		// exempt
		List<ProtocolFormCommitteeStatusEnum> exemptList = Lists.newArrayList();
		exemptList.add(ProtocolFormCommitteeStatusEnum.APPROVED);
		exemptList
				.add(ProtocolFormCommitteeStatusEnum.NOT_APPLICABLE_TO_EXEMPT_IRB_REVIEW);
		exemptList.add(ProtocolFormCommitteeStatusEnum.REVISION_REQUESTED);
		exemptList.add(ProtocolFormCommitteeStatusEnum.COMPLETED);
		endCommitteeStatusMap.put(Committee.IRB_EXEMPT_REVIEWER, exemptList);

		// irbReview
				List<ProtocolFormCommitteeStatusEnum> irbReviewList = Lists.newArrayList();
				irbReviewList.add(ProtocolFormCommitteeStatusEnum.ACKNOWLEDGED);
				irbReviewList.add(ProtocolFormCommitteeStatusEnum.APPROVED);
				irbReviewList.add(ProtocolFormCommitteeStatusEnum.DEFERRED_WITH_MAJOR_CONTINGENCIES);
				irbReviewList.add(ProtocolFormCommitteeStatusEnum.DEFERRED_WITH_MINOR_CONTINGENCIES);
				irbReviewList.add(ProtocolFormCommitteeStatusEnum.REMOVED_FROM_IRB_AGENDA);
				endCommitteeStatusMap.put(Committee.IRB_REVIEWER, irbReviewList);
		
		// irbOffice
		List<ProtocolFormCommitteeStatusEnum> irbOfficeList = Lists
				.newArrayList();
		irbOfficeList.add(ProtocolFormCommitteeStatusEnum.IRB_AGENDA_ASSIGNED);
		irbOfficeList.add(ProtocolFormCommitteeStatusEnum.EXEMPT_IRB_REVIEW);
		irbOfficeList.add(ProtocolFormCommitteeStatusEnum.EXPEDITED_IRB_REVIEW);
		irbOfficeList.add(ProtocolFormCommitteeStatusEnum.APPROVED);
		irbOfficeList.add(ProtocolFormCommitteeStatusEnum.REVISION_REQUESTED);
		irbOfficeList.add(ProtocolFormCommitteeStatusEnum.ACKNOWLEDGED);
		irbOfficeList.add(ProtocolFormCommitteeStatusEnum.DEFERRED_WITH_MINOR_CONTINGENCIES);
		irbOfficeList.add(ProtocolFormCommitteeStatusEnum.DEFERRED_WITH_MAJOR_CONTINGENCIES);
		irbOfficeList.add(ProtocolFormCommitteeStatusEnum.COMPLETED);
		endCommitteeStatusMap.put(Committee.IRB_OFFICE, irbOfficeList);

		// irbPrereview
		List<ProtocolFormCommitteeStatusEnum> irbPrereviewList = Lists
				.newArrayList();
		irbPrereviewList.add(ProtocolFormCommitteeStatusEnum.APPROVED);
		irbPrereviewList
				.add(ProtocolFormCommitteeStatusEnum.REVISION_REQUESTED);
		irbPrereviewList.add(ProtocolFormCommitteeStatusEnum.ACKNOWLEDGED);
		irbPrereviewList.add(ProtocolFormCommitteeStatusEnum.COMPLETED);
		endCommitteeStatusMap.put(Committee.IRB_PREREVIEW, irbPrereviewList);

		// irbConsentReview
		List<ProtocolFormCommitteeStatusEnum> irbConsentReviewList = Lists
				.newArrayList();
		irbConsentReviewList.add(ProtocolFormCommitteeStatusEnum.APPROVED);
		endCommitteeStatusMap.put(Committee.IRB_CONSENT_REVIEWER,
				irbConsentReviewList);

		// irbAssigner
		List<ProtocolFormCommitteeStatusEnum> irbAssignerList = Lists
				.newArrayList();
		irbAssignerList.add(ProtocolFormCommitteeStatusEnum.REVIEWER_ASSIGNED);
		endCommitteeStatusMap.put(Committee.IRB_ASSIGNER, irbAssignerList);

		// budgetReview
		List<ProtocolFormCommitteeStatusEnum> budgetReviewList = Lists
				.newArrayList();
		budgetReviewList.add(ProtocolFormCommitteeStatusEnum.APPROVED);
		budgetReviewList.add(ProtocolFormCommitteeStatusEnum.COMPLETED);
		budgetReviewList
				.add(ProtocolFormCommitteeStatusEnum.REVISION_REQUESTED);
		endCommitteeStatusMap.put(Committee.BUDGET_REVIEW, budgetReviewList);

		// budgetManager
		List<ProtocolFormCommitteeStatusEnum> budgetManagerList = Lists
				.newArrayList();
		budgetManagerList
				.add(ProtocolFormCommitteeStatusEnum.REVIEWER_ASSIGNED);
		budgetManagerList
		.add(ProtocolFormCommitteeStatusEnum.REVISION_REQUESTED);
		endCommitteeStatusMap.put(Committee.BUDGET_MANAGER, budgetManagerList);

		// coverageReview
		List<ProtocolFormCommitteeStatusEnum> coverageReviewList = Lists
				.newArrayList();
		coverageReviewList.add(ProtocolFormCommitteeStatusEnum.APPROVED);
		coverageReviewList.add(ProtocolFormCommitteeStatusEnum.COMPLETED);
		coverageReviewList
				.add(ProtocolFormCommitteeStatusEnum.REVISION_REQUESTED);
		coverageReviewList
				.add(ProtocolFormCommitteeStatusEnum.PENDING_CONSENT_LEGAL_REVIEW);
		endCommitteeStatusMap
				.put(Committee.COVERAGE_REVIEW, coverageReviewList);

		// pharmacyReview
		List<ProtocolFormCommitteeStatusEnum> pharmacyReviewList = Lists
				.newArrayList();
		pharmacyReviewList.add(ProtocolFormCommitteeStatusEnum.APPROVED);
		pharmacyReviewList
				.add(ProtocolFormCommitteeStatusEnum.WAIVER_REQUEST_APPROVED);
		endCommitteeStatusMap
				.put(Committee.PHARMACY_REVIEW, pharmacyReviewList);

		// hospitalReview
		List<ProtocolFormCommitteeStatusEnum> hospitalReviewList = Lists
				.newArrayList();
		hospitalReviewList.add(ProtocolFormCommitteeStatusEnum.APPROVED);
		endCommitteeStatusMap.put(Committee.HOSPITAL_SERVICES,
				hospitalReviewList);

		// colleageDean
		List<ProtocolFormCommitteeStatusEnum> colleageDeanList = Lists
				.newArrayList();
		colleageDeanList.add(ProtocolFormCommitteeStatusEnum.APPROVED);
		endCommitteeStatusMap.put(Committee.COLLEGE_DEAN, colleageDeanList);

		// departmentChair
		List<ProtocolFormCommitteeStatusEnum> departmentChairList = Lists
				.newArrayList();
		departmentChairList.add(ProtocolFormCommitteeStatusEnum.APPROVED);
		endCommitteeStatusMap.put(Committee.DEPARTMENT_CHAIR,
				departmentChairList);

		// gatekeeper
		List<ProtocolFormCommitteeStatusEnum> gatekeeperList = Lists
				.newArrayList();
		gatekeeperList.add(ProtocolFormCommitteeStatusEnum.APPROVED);
		gatekeeperList.add(ProtocolFormCommitteeStatusEnum.REVISION_REQUESTED);
		gatekeeperList.add(ProtocolFormCommitteeStatusEnum.PENDING_PTL_DEVELOP);
		gatekeeperList.add(ProtocolFormCommitteeStatusEnum.REJECTED);
		endCommitteeStatusMap.put(Committee.GATEKEEPER, gatekeeperList);

		// COMPLIANCE_REVIEW
		List<ProtocolFormCommitteeStatusEnum> complianceList = Lists
				.newArrayList();
		complianceList
				.add(ProtocolFormCommitteeStatusEnum.COMMITTEE_REVIEW_NOT_APPLICABLE);
		complianceList.add(ProtocolFormCommitteeStatusEnum.REVISION_REQUESTED);
		complianceList.add(ProtocolFormCommitteeStatusEnum.APPROVED);
		endCommitteeStatusMap.put(Committee.COMPLIANCE_REVIEW, complianceList);
		
		// achri
		List<ProtocolFormCommitteeStatusEnum> achriList = Lists
						.newArrayList();
		achriList.add(ProtocolFormCommitteeStatusEnum.REVISION_REQUESTED);
		achriList.add(ProtocolFormCommitteeStatusEnum.APPROVED);
		endCommitteeStatusMap.put(Committee.ACHRI, achriList);
	}
	
	
	
	private Map<String, Long> getTimeInEachQueueByProtocolId(long protocolId,
			List<Committee> committees) {
		List<ProtocolForm> pfs = protocolFormDao
				.listProtocolFormsByProtocolIdAndProtocolFormType(protocolId,
						ProtocolFormType.NEW_SUBMISSION);
		Map<String, Long> resultMap = Maps.newTreeMap();
		long totaltimeForStudy = 0;
		long earliestTime=0;
		long finalActionTime=0;
		for (ProtocolForm pf : pfs) {
			if (pf.getId() != pf.getParent().getId()) {
				continue;
		}

			for (Committee committee : committees) {
				long totalTime=0;
				long startTime = 0;
				long endTime = 0;
				long preStartTime =0;

				List<ProtocolFormCommitteeStatus> pfcss = protocolFormCommitteeStatusDao
						.listAllByCommitteeAndProtocolFormId(committee,
								pf.getFormId());
				for (int i = 0; i < pfcss.size(); i++) {
					ProtocolFormCommitteeStatus pfcs = pfcss.get(i);
					List<ProtocolFormCommitteeStatusEnum> startActions = startCommitteeStatusMap
							.get(committee);
					List<ProtocolFormCommitteeStatusEnum> endActions = endCommitteeStatusMap
							.get(committee);

					if (startActions == null) {
						continue;
					}

					if (startActions.contains(pfcs
							.getProtocolFormCommitteeStatus())) {
						startTime = pfcs.getModified().getTime();
						if(startTime<earliestTime||earliestTime==0){
						earliestTime = startTime;
						}
					} else if (startTime > 0
							&& endActions.contains(pfcs
									.getProtocolFormCommitteeStatus())) {
						endTime = pfcs.getModified().getTime();
						if(endTime>finalActionTime){
							finalActionTime = endTime;
						}
					} 
					
					if (startTime > 0 && endTime == 0
							&& i == (pfcss.size() - 1)) {
						endTime = new Date().getTime();
						finalActionTime=endTime;
					}
					

					if (startTime > 0 && endTime > 0) {
						totalTime += endTime - startTime;
						if (endTime - startTime < 0) {
							logger.debug(pf.getFormId() + "#######"
									+ (endTime - startTime));
						}
						preStartTime=startTime;
						startTime = 0;
						endTime = 0;

					}
				}
				if (totalTime > 0) {
					//totalTime = 1 + totalTime / (24 * 60 * 60 * 1000);
					resultMap.put(committee.getDescription(), totalTime);
				}
			}
		}
		totaltimeForStudy= finalActionTime-earliestTime;
		resultMap.put("totalTime", totaltimeForStudy);
//				+ " Days" : totaltimeForStudy + " Day");
		try{logger.debug(protocolId+" "+resultMap.get(Committee.IRB_REVIEWER.getDescription())/ (24 * 60 * 60 * 1000));
		}catch(Exception e){
			
		}
		return resultMap;
	}
	
	private Map<String, Long> getCountForEachQueue(long protocolId,
			List<Committee> committees) {
		List<ProtocolForm> pfs = protocolFormDao
				.listProtocolFormsByProtocolIdAndProtocolFormType(protocolId,
						ProtocolFormType.NEW_SUBMISSION);
		Map<String, Long> resultMap = Maps.newTreeMap();
		long totaltimeForStudy = 0;
		long earliestTime=0;
		long finalActionTime=0;
		for (ProtocolForm pf : pfs) {
			if (pf.getId() != pf.getParent().getId()) {
				continue;
		}
			
			for (Committee committee : committees) {
				long count = 0;
				long totalTime=0;
				long startTime = 0;
				long endTime = 0;
				long preStartTime =0;

				List<ProtocolFormCommitteeStatus> pfcss = protocolFormCommitteeStatusDao
						.listAllByCommitteeAndProtocolFormId(committee,
								pf.getFormId());
				for (int i = 0; i < pfcss.size(); i++) {
					ProtocolFormCommitteeStatus pfcs = pfcss.get(i);
					List<ProtocolFormCommitteeStatusEnum> startActions = startCommitteeStatusMap
							.get(committee);
					List<ProtocolFormCommitteeStatusEnum> endActions = endCommitteeStatusMap
							.get(committee);

					if (startActions == null) {
						continue;
					}

					if (startActions.contains(pfcs
							.getProtocolFormCommitteeStatus())) {
						startTime = pfcs.getModified().getTime();
						if(startTime<earliestTime||earliestTime==0){
						earliestTime = startTime;
						}
					} else if (startTime > 0
							&& endActions.contains(pfcs
									.getProtocolFormCommitteeStatus())) {
						endTime = pfcs.getModified().getTime();
						if(endTime>finalActionTime){
							finalActionTime = endTime;
						}
					} 
					
					if (startTime > 0 && endTime == 0
							&& i == (pfcss.size() - 1)) {
						endTime = new Date().getTime();
						finalActionTime=endTime;
					}
					

					if (startTime > 0 && endTime > 0) {
						totalTime += endTime - startTime;
						count++;
						if (endTime - startTime < 0) {
							logger.debug(pf.getFormId() + "#######"
									+ (endTime - startTime));
						}
						preStartTime=startTime;
						startTime = 0;
						endTime = 0;

					}
				}
				if (totalTime > 0) {
					//totalTime = 1 + totalTime / (24 * 60 * 60 * 1000);
					resultMap.put(committee.getDescription(), count);
				}
			}
		}
		totaltimeForStudy= finalActionTime-earliestTime;
		resultMap.put("totalTime", totaltimeForStudy);
//				+ " Days" : totaltimeForStudy + " Day");
		try{
		//	logger.debug(protocolId+" "+resultMap.get(Committee.IRB_REVIEWER.getDescription())/ (24 * 60 * 60 * 1000));
		}catch(Exception e){
			
		}
		return resultMap;
	}
	
	//@Test
	public void TimeInEachQueue() throws IOException{

		FileInputStream fstream;
		fstream = new FileInputStream("C:\\Data\\id.txt");
		DataInputStream in = new DataInputStream(fstream);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String strLine;

		long pitimeTotal = 0;
		long othertotal= 0;
		List<Committee> committees = Arrays.asList(Committee
				.values());
		Map<String,Long> totalTimeFoeEachQueue =Maps.newHashMap();
		Map<String,Long> finalCountMap =Maps.newHashMap();
		int totalNumber=0;
		while ((strLine = br.readLine()) != null) {
			totalNumber++;
			long pfId = Long.valueOf(strLine);
			long tempTime = getTotalTimeForPI(pfId);
			pitimeTotal +=tempTime/(24 * 60 * 60 * 1000);
			
			Map<String,Long>timeSpentMap=getTimeInEachQueueByProtocolId(protocolFormDao.findById(pfId).getProtocol().getId(),committees);
			Map<String,Long> countMap = getCountForEachQueue(protocolFormDao.findById(pfId).getProtocol().getId(),committees);
			for (Entry<String, Long> values : timeSpentMap.entrySet()) {
				String key = values.getKey();
				if(totalTimeFoeEachQueue.containsKey(key)){
					long tempTimeForQueue = totalTimeFoeEachQueue.get(key)+(values.getValue())/(24 * 60 * 60 * 1000);
					totalTimeFoeEachQueue.remove(key);
					totalTimeFoeEachQueue.put(key,tempTimeForQueue );
					long count = finalCountMap.get(key)+countMap.get(key);
					finalCountMap.put(key, count);
				}
				else{
					totalTimeFoeEachQueue.put(key,values.getValue()/(24 * 60 * 60 * 1000));
					finalCountMap.put(key, countMap.get(key));
				}
			}
			othertotal += timeSpentMap.get("totalTime");
			
		}	
		for (Entry<String, Long> values : totalTimeFoeEachQueue.entrySet()) {
			System.out.println(values.getKey()+" "+(1+values.getValue()/(finalCountMap.get(values.getKey()))));
			//System.out.println(values.getKey()+" "+finalCountMap.get(values.getKey())+"\n");
		}
		System.out.println("PI"+(1+pitimeTotal / (totalNumber))+"\n");
		System.out.println("Total"+(1+(pitimeTotal+othertotal/(24 * 60 * 60 * 1000)) / (totalNumber)));
	}
	
	//@Test
	public void summaryTest(){
		List<Long> submissionSummary = getSummaryTimeFromSubmissionToComplete();
		logger.debug(submissionSummary.get(0)+"");
		logger.debug(submissionSummary.get(submissionSummary.size()-3)+"");
		logger.debug(submissionSummary.get(submissionSummary.size()-2)+"");
		logger.debug(submissionSummary.get(submissionSummary.size()-1)+"");
		
	}
	
	private List<Long> getSummaryTimeFromSubmissionToComplete(){
		List<ProtocolForm> pfms = protocolFormDao.listParentProtocolFormsByProtocolFormType(ProtocolFormType.NEW_SUBMISSION);
		List<Long> tiemForSubmission =Lists.newArrayList();
		
		long totalTime = 0;
		int totalNumber = 0;
		long singleTime =0;
		for(ProtocolForm pf:pfms){
			List<ProtocolFormStatus> pfss = protocolFormStatusDao.getAllProtocolFormStatusByParentFormId(pf.getFormId());
			long startTime = 0;
			long endTime = 0;
			if(completeFormStatus.contains(pfss.get(pfss.size()-1).getProtocolFormStatus())){
				startTime = pfss.get(0).getModified().getTime();
				endTime =  pfss.get(pfss.size()-1).getModified().getTime();
				totalNumber++;
				singleTime =1+(endTime - startTime)/(24*60*60*1000);
				totalTime +=endTime - startTime;
				tiemForSubmission.add(singleTime);
			}
		}
		
		Collections.sort(tiemForSubmission);
		int medianIndex= tiemForSubmission.size()/2;
		tiemForSubmission.add(1+(totalTime/totalNumber)/(24*60*60*1000));
		tiemForSubmission.add(tiemForSubmission.get(medianIndex));
		return tiemForSubmission;
	}
	
	private List<Long> getSummaryTimeFromCreateToSubmission(){
		List<ProtocolForm> pfms = protocolFormDao.listParentProtocolFormsByProtocolFormType(ProtocolFormType.NEW_SUBMISSION);
		List<Long> tiemForSubmission =Lists.newArrayList();
		
		long totalTime = 0;
		int totalNumber = 0;
		long singleTime =0;
		for(ProtocolForm pf:pfms){
			List<ProtocolFormStatus> pfss = protocolFormStatusDao.getAllProtocolFormStatusByParentFormId(pf.getFormId());
			
			long startTime = 0;
			long endTime = 0;
			
			for(int i=0;i<pfss.size();i++){
				if(pfss.contains(ProtocolFormStatusEnum.CANCELLED)){
					break;
				}
				ProtocolFormStatus pfs = pfss.get(i);
				if (pfs.getProtocolFormStatus().equals(ProtocolFormStatusEnum.DRAFT)&&startTime==0) {
					startTime = pfs.getModified().getTime();
				}else if (!draftFormStatus.contains(pfs.getProtocolFormStatus())&&startTime > 0 && endTime==0) {
					endTime = pfs.getModified().getTime();
				} 
				
				if (startTime > 0 && endTime > 0) {
					totalNumber++;
					singleTime =1+(endTime - startTime)/(24*60*60*1000);
					totalTime +=endTime - startTime;
					tiemForSubmission.add(singleTime);
					if (totalTime < 0) {
						//logger.debug(protocolFormId + "#######" + totalTime);
					}
				}
				
				
			}
		}
		
		Collections.sort(tiemForSubmission);
		int medianIndex= tiemForSubmission.size()/2;
		//mean time
		tiemForSubmission.add(1+(totalTime/totalNumber)/(24*60*60*1000));
		//median time
		tiemForSubmission.add(tiemForSubmission.get(medianIndex));
		return tiemForSubmission;
	}
	
	private long getTotalTimeForPI(long protocolFormId){
		List<ProtocolFormStatusEnum> startActions = Lists.newArrayList();
		//startActions.add(ProtocolFormStatusEnum.DRAFT);
		startActions.add(ProtocolFormStatusEnum.UNDER_REVISION);
		startActions.add(ProtocolFormStatusEnum.REVISION_REQUESTED);
		startActions.add(ProtocolFormStatusEnum.REVISION_PENDING_PI_ENDORSEMENT);
		//startActions.add(ProtocolFormStatusEnum.PENDING_PI_ENDORSEMENT);
		startActions.add(ProtocolFormStatusEnum.PENDING_PI_SIGN_OFF);
		//startActions.add(ProtocolFormStatusEnum.PENDING_PL_ENDORSEMENT);
		//startActions.add(ProtocolFormStatusEnum.PENDING_TP_ENDORSEMENT);
		
			long totalTime = 0;
			long startTime = 0;
			long endTime = 0;
			List<ProtocolFormStatus> pfss = protocolFormStatusDao.getAllProtocolFormStatusByParentFormId(protocolFormId);
			
			//because the query order by modified desc, we use i-- for for loop
			for(int i=0;i<pfss.size();i++){
				ProtocolFormStatus pfs = pfss.get(i);
				if (startActions.contains(pfs.getProtocolFormStatus())&&startTime==0) {
					startTime = pfs.getModified().getTime();
				} else if (startActions.contains(pfs.getProtocolFormStatus())&&startTime>0&& endTime==0) {
					endTime = pfs.getModified().getTime();
					i--;
				}else if (startTime > 0 && endTime==0) {
					endTime = pfs.getModified().getTime();
				} 
				
				if (startTime > 0 && endTime==0&& i==pfss.size()-1) {
					endTime = new Date().getTime();
				}
				
				if (startTime > 0 && endTime > 0) {
					
					totalTime += endTime - startTime;
					startTime=0;
					endTime=0;
					if (totalTime < 0) {
						logger.debug(protocolFormId + "#######" + totalTime);
					}
				}
				
				
			}
			//totalTime = 1+totalTime/(24*60*60*1000);
			return totalTime;
	}
	
	private String[] getAverageTimeSpentByPI(ProtocolFormType pft){
		List<ProtocolFormStatusEnum> startActions = Lists.newArrayList();
		
		startActions.add(ProtocolFormStatusEnum.DRAFT);
		startActions.add(ProtocolFormStatusEnum.UNDER_REVISION);
		startActions.add(ProtocolFormStatusEnum.REVISION_REQUESTED);
		startActions.add(ProtocolFormStatusEnum.REVISION_PENDING_PI_ENDORSEMENT);
		startActions.add(ProtocolFormStatusEnum.PENDING_PI_ENDORSEMENT);
		startActions.add(ProtocolFormStatusEnum.PENDING_PI_SIGN_OFF);
		startActions.add(ProtocolFormStatusEnum.PENDING_PL_ENDORSEMENT);
		startActions.add(ProtocolFormStatusEnum.PENDING_TP_ENDORSEMENT);
		
		//get submitted form id
		String qry="select distinct protocol_form_id from protocol_form_status ps,protocol_form pf where ps.id in (select max(id) from protocol_form_status where retired =0 group by protocol_form_id) "
				+ "and ps.protocol_form_status not in ('DRAFT','PENDING_PL_ENDORSEMENT','PENDING_PI_ENDORSEMENT','PENDING_TP_ENDORSEMENT') "
				+ "and pf.retired =0 and pf.protocol_form_type ='"+pft+"' and pf.id = ps.protocol_form_id ";
		Query query = em.createNativeQuery(qry);
		List<BigInteger> pfIDs = (List<BigInteger>) query.getResultList();
		long maxTime = 0;
		long minTime = 100000;
		long maxTimePFID=0;
		int counter = 0;
		long totalTime = 0;
		
		for (BigInteger pfIDBig : pfIDs) {
			long pfID = pfIDBig.longValue();
			
			long startTime = 0;
			long endTime = 0;
			List<ProtocolFormStatus> pfss = protocolFormStatusDao.getAllProtocolFormStatusByFormId(pfID);

			//because the query order by modified desc, we use i-- for for loop
			for(int i=pfss.size()-1;i>-1;i--){
				ProtocolFormStatus pfs = pfss.get(i);
				if (pfs.getProtocolForm().getFormId() != pfID) {
					continue;
				}
				
				if (startActions.contains(pfs.getProtocolFormStatus())) {
					startTime = pfs.getModified().getTime();
				} else if (startTime > 0 && endTime==0) {
					endTime = pfs.getModified().getTime();
				} else if (startTime > 0 && endTime==0&& i==0) {
					endTime = new Date().getTime();
				}
				
				if (startTime > 0 && endTime > 0) {
					long timeSpent = endTime - startTime;
					long minuteTimes = endTime - startTime;
					
					timeSpent = timeSpent / (24 * 60 * 60 * 1000);
					minuteTimes = minuteTimes / (60 * 1000);
					totalTime += timeSpent;
					counter++;
					if (timeSpent > maxTime) {
						maxTime = timeSpent;
						maxTimePFID =pfID;
					}
					if (minuteTimes < minTime) {
						minTime = minuteTimes;
					}
					startTime=0;
					endTime=0;
					if (timeSpent < 0) {
						logger.debug(pfID + "#######" + timeSpent);
					}
				}
				
				
			}
			
		}
		double averageTime = (double) totalTime / (double) counter;
		if (minTime == 0) {
			minTime = 1;
		}
		logger.debug(maxTimePFID+"");
		String[] entry = { "PI" + "-" + pft.toString(),
				averageTime + "", " Days", maxTime + "", " Days",
				minTime + "", " Minutes" };
		return entry;
		
	}
	
	private String[] getAverageTimeByQueueAndContractFormType(Committee committee,
			ContractFormType cft) {
		List<ContractFormCommitteeStatusEnum> startStatus = contractStartCommitteeStatusMap
				.get(committee);
		List<ContractFormCommitteeStatusEnum> endStatus = contractPendingCommitteeStatusMap
				.get(committee);
		List<ContractFormCommitteeStatusEnum> pendingStatus = contractEndCommitteeStatusMap
				.get(committee);

		String committeeStatus = "";
		for (int i = 0; i < startStatus.size(); i++) {
			if (i == 0) {
				committeeStatus += "'" + startStatus.get(i) + "'";
			} else {
				committeeStatus += ",'" + startStatus.get(i) + "'";
			}
		}

		String qry = "select distinct contract_form_id from contract_form_committee_status where retired=0 and committee ='"
				+ committee
				+ "' and contract_form_committee_status in ("
				+ committeeStatus + ")";
		Query query = em.createNativeQuery(qry);
		List<BigInteger> cfIDs = (List<BigInteger>) query.getResultList();
		long maxTime = 0;
		long minTime = 100000;
		int counter = 0;
		long totalTime = 0;

		for (BigInteger cfIDBig : cfIDs) {
			long cfID = cfIDBig.longValue();
			if (!contractFormDao.findById(cfID).getContractFormType()
					.equals(cft)) {
				continue;
			}
			long startTime = 0;
			long endTime = 0;
			List<ContractFormCommitteeStatus> cfcss = contractFormCommitteeStatusDao
					.listAllByCommitteeAndContractFormId(committee, cfID);
			for (int i=0;i<cfcss.size();i++) {
				ContractFormCommitteeStatus cfcs = cfcss.get(i);
				if (cfcs.getContractFormId() != cfID) {
					continue;
				}
				
				if (startStatus.contains(cfcs.getContractFormCommitteeStatus())) {
					startTime = cfcs.getModified().getTime();
				} else if (startTime > 0
						&& endStatus.contains(cfcs
								.getContractFormCommitteeStatus())) {
					endTime = cfcs.getModified().getTime();
				} else if (startTime > 0
						&& pendingStatus.contains(cfcs
								.getContractFormCommitteeStatus())) {
					endTime = cfcs.getModified().getTime();
				}else if (startTime > 0 && endTime == 0&&i==(cfcss.size()-1)){
					Date date = new Date();
					endTime = date.getTime();
				}
				if (startTime > 0 && endTime > 0) {
					long timeSpent = endTime - startTime;
					long minuteTimes = endTime - startTime;
					
					timeSpent = timeSpent / (24 * 60 * 60 * 1000);
					minuteTimes = minuteTimes / (60 * 1000);
					totalTime += timeSpent;
					counter++;
					if (timeSpent > maxTime) {
						maxTime = timeSpent;
					}
					if (minuteTimes < minTime) {
						minTime = minuteTimes;
					}
					startTime=0;
					endTime=0;
					if (timeSpent < 0) {
						logger.debug(cfID + "#######" + timeSpent);
					}
				}
			}
		}
		double averageTime = (double) totalTime / (double) counter;
		if (minTime == 0) {
			minTime = 1;
		}
		String[] entry = { committee.toString() + "-" + cft.toString(),
				averageTime + "", " Days", maxTime + "", " Days",
				minTime + "", " Minutes" };
		return entry;
	}

	private String[] getAverageTimeByQueueAndFormType(Committee committee,
			ProtocolFormType pft) {
		List<ProtocolFormCommitteeStatusEnum> startStatus = startCommitteeStatusMap
				.get(committee);
		List<ProtocolFormCommitteeStatusEnum> endedStatus = endCommitteeStatusMap
				.get(committee);
		String committeeStatus = "";
		for (int i = 0; i < startStatus.size(); i++) {
			if (i == 0) {
				committeeStatus += "'" + startStatus.get(i) + "'";
			} else {
				committeeStatus += ",'" + startStatus.get(i) + "'";
			}
		}

		String qry = "select distinct protocol_form_id from protocol_form_committee_status where retired=0 and committee ='"
				+ committee
				+ "' and protocol_form_committee_status in ("
				+ committeeStatus + ")";
		Query query = em.createNativeQuery(qry);
		List<BigInteger> pfIDs = (List<BigInteger>) query.getResultList();
		long maxTime = 0;
		long minTime = 100000;
		int counter = 0;
		long totalTime = 0;

		for (BigInteger pfIDBig : pfIDs) {
			long pfID = pfIDBig.longValue();
			if (!protocolFormDao.findById(pfID).getProtocolFormType()
					.equals(pft)) {
				continue;
			}
			List<ProtocolFormCommitteeStatus> pfcss = protocolFormCommitteeStatusDao
					.listAllByCommitteeAndProtocolFormId(committee, pfID);
			List<ProtocolFormCommitteeStatus> startList = Lists.newArrayList();
			List<ProtocolFormCommitteeStatus> endList = Lists.newArrayList();
			for (ProtocolFormCommitteeStatus pfcs : pfcss) {
				if (pfcs.getProtocolFormId() != pfID) {
					continue;
				}
				if (startStatus.contains(pfcs.getProtocolFormCommitteeStatus())) {
					startList.add(pfcs);
				} else if (endedStatus.contains(pfcs
						.getProtocolFormCommitteeStatus())) {
					endList.add(pfcs);
				}
			}

			for (int i = 0; i < startList.size(); i++) {
				long timeSpent = new Date().getTime()
						- startList.get(i).getModified().getTime();
				try {
					//  review is finished
					endList.get(i);
					timeSpent = endList.get(i).getModified().getTime()
							- startList.get(i).getModified().getTime();
				} catch (Exception e) {
					
				}
				
				long minuteTimes = endList.get(i).getModified().getTime()
						- startList.get(i).getModified().getTime();
				timeSpent = timeSpent / (60 * 60 * 1000);
				logger.debug(timeSpent+"");
				minuteTimes = minuteTimes / (60 * 1000);
				counter++;
				totalTime += timeSpent;
				// logger.debug(pfID+"  "+endList.get(i).getProtocolFormCommitteeStatus().getDescription()+"  "+timeSpent+"   "+startList.get(i).getProtocolFormCommitteeStatus().getDescription());
				if (timeSpent < 0) {
					logger.debug(pfID + "#######" + timeSpent);
				}
				if (timeSpent > maxTime) {
					maxTime = timeSpent;
				}
				if (minuteTimes < minTime) {
					minTime = minuteTimes;
				}
			}

		}
		double averageTime = (double) totalTime / (double) counter;
		if (minTime == 0) {
			minTime = 1;
		}
		String[] entry = { committee.toString() + "-" + pft.toString(),
				averageTime + "", " Hours", maxTime + "", " Hours",
				minTime + "", " Minutes" };
		/*
		 * logger.debug(committee.toString()+"####"+pft.toString()+"\n");
		 * logger.debug("Average Time: "+averageTime+" Hours\n");
		 * logger.debug("Maximum Time: "+maxTime+" Hours\n");
		 * logger.debug("Minimum Time: "+minTime +" Minutes\n");
		 */
		logger.debug("Total: "+counter);
		return entry;

	}
	

	//@Test
	public void getAverageTimeByQueueAndFormTypeTest() throws IOException {
		CSVWriter writer = new CSVWriter(new FileWriter(
				"C:\\Data\\"+ DateFormatUtil.formateDateToMDY(new Date()).replace("/", "-")+"Average Time In Queue Report.csv"));
		String[] title = { "Type", "Average Time", "", "Maximum Time", "",
				"Minimum Time" };
		writer.writeNext(title);
		
		String[] entry = getAverageTimeSpentByPI(ProtocolFormType.NEW_SUBMISSION);
		logger.debug(entry[1] + entry[2] + entry[3] + entry[4]
				+ entry[5] + entry[6]);
		//this part for protocol
		/*List<ProtocolFormType> pfts = Lists.newArrayList();
		pfts.add(ProtocolFormType.NEW_SUBMISSION);
		pfts.add(ProtocolFormType.MODIFICATION);
		pfts.add(ProtocolFormType.CONTINUING_REVIEW);
		pfts.add(ProtocolFormType.HUMAN_SUBJECT_RESEARCH_DETERMINATION);
		pfts.add(ProtocolFormType.STUDY_CLOSURE);
		pfts.add(ProtocolFormType.STAFF);
		for (Committee committee : startCommitteeStatusMap.keySet()) {
			for (ProtocolFormType pft : pfts) {
				String[] entry = getAverageTimeByQueueAndFormType(Committee.COLLEGE_DEAN,
						pft);
				if (entry[1].equals("NaN")) {
					continue;
				}
				logger.debug(Committee.COLLEGE_DEAN + "       " + pft);
				logger.debug(entry[1] + entry[2] + entry[3] + entry[4]
						+ entry[5] + entry[6]);
				writer.writeNext(entry);
			}
		}
*/		
		//This part for contract
		/*List<ContractFormType> cfts = Lists.newArrayList();
		cfts.add(ContractFormType.NEW_CONTRACT);
		for (Committee committee : contractStartCommitteeStatusMap.keySet()) {
			for (ContractFormType cft : cfts) {
				String[] entry = getAverageTimeByQueueAndContractFormType(committee,
						cft);
				if (entry[1].equals("NaN")) {
					continue;
				}
				logger.debug(committee + "       " + cft);
				logger.debug(entry[1] + entry[2] + entry[3] + entry[4]
						+ entry[5] + entry[6]);
				writer.writeNext(entry);
			}
		}*/

		writer.flush();
		writer.close();
	}
	
	//@Test 
	public void voidJeneffier10032013() throws IOException, XPathExpressionException, SAXException{
		//Could she get a report that lists all the open studies in the Myeloma Institute?  Protocol number, title, Sponsor and if there is a CR in the system the number of subjects enrolled
		CSVWriter writer = new CSVWriter(new FileWriter("C:\\Data\\Report for MYEL Myeloma Institute .csv"));
		String[] Titleentry={"IRB#","Title","Funding Name", "Funding Amount", "Funding Type",
				"Funding Entity Name","Local Subject Since Activation","Local Subject Since Last Approval","All Sites Subject Since Activation","All Sites Subject Since Last Approval"};
		writer.writeNext(Titleentry);
		
		String qry = "select distinct protocol_id from protocol_status "
				+ "where protocol_id in (select id from protocol where meta_data_xml.exist('/protocol/responsible-department[@collegedesc[fn:contains(.,\"MYEL Myeloma Institute\")]]')=1 "
				+ "and retired =0) and retired =0 and id in (select max(id) from protocol_status  where retired=0 group by protocol_id) and protocol_status='OPEN'";
		Query query = em.createNativeQuery(qry);
		List<BigInteger> pIDs = (List<BigInteger>) query.getResultList();
		pIDs.add(new BigInteger("138049"));
		pIDs.add(new BigInteger("7417"));
		pIDs.add(new BigInteger("105455"));
		pIDs.add(new BigInteger("1332"));
		




		for (BigInteger pIDBig : pIDs) {
			long pid = pIDBig.longValue();
			Protocol p = protocolDao.findById(pid);
			ProtocolFormXmlData crfxd = null;
			try{
				crfxd=protocolFormXmlDataDao.getLastProtocolFormXmlDataByProtocolIdAndType(pid, ProtocolFormXmlDataType.CONTINUING_REVIEW);
			}catch(Exception e){
				
			}
			String enrolledSubjectLocalActivition = "";
			String enrolledSubjectLocalLastApprove = "";
			String enrolledSubjectTotalActivition = "";
			String enrolledSubjectTotalLastApprove = "";
			if(crfxd!=null){
				enrolledSubjectLocalActivition = xmlHandler.getSingleStringValueByXPath(crfxd.getXmlData(), "//subject-accrual/enrollment/local/since-activation");
				enrolledSubjectLocalLastApprove = xmlHandler.getSingleStringValueByXPath(crfxd.getXmlData(), "//subject-accrual/enrollment/local/since-approval");
				enrolledSubjectTotalActivition = xmlHandler.getSingleStringValueByXPath(crfxd.getXmlData(), "//subject-accrual/enrollment/local/multi-site-since-activation");
				enrolledSubjectTotalLastApprove = xmlHandler.getSingleStringValueByXPath(crfxd.getXmlData(), "//subject-accrual/enrollment/local/multi-site-since-approval");
			}
			
			String xml = p.getMetaDataXml();
			String title =  xmlHandler.getSingleStringValueByXPath(xml, "/protocol/title");
			
			Set<String> fundingPath = Sets.newHashSet();
			fundingPath.add("/protocol/funding/funding-source");
			List<Element> fundings = xmlProcessor.listDomElementsByPaths(
					fundingPath, xml);
			
			boolean firstFunding = true;
			for (Element funding : fundings) {
				String fundingName = funding.getAttribute("name");
				String fundingAmount = funding.getAttribute("amount");
				String fundingEntityName = funding.getAttribute("entityname");
				String type = funding.getAttribute("type");
				if (firstFunding) {
					String[] entry = { pid + "",
							title,  fundingName, fundingAmount, type,
							fundingEntityName,enrolledSubjectLocalActivition,enrolledSubjectLocalLastApprove,enrolledSubjectTotalActivition,enrolledSubjectTotalLastApprove };
					writer.writeNext(entry);
					firstFunding = false;
				} else {
					String[] entry = { "", "", fundingName,
							fundingAmount, type, fundingEntityName };
					writer.writeNext(entry);
				}
			}

			if (fundings.size() == 0) {
				String[] entry = {pid + "", title,"","","","",enrolledSubjectLocalActivition,enrolledSubjectLocalLastApprove,enrolledSubjectTotalActivition,enrolledSubjectTotalLastApprove };
				writer.writeNext(entry);
			}

		}
		writer.flush();
		writer.close();
	}

	//@Test
	public void generateWeeklyBillingProtocolInMeeting()
			throws XPathExpressionException, SAXException, IOException {
		String beginTime = "'2014-07-11'";
		String endTime = "'2014-07-17'";
		CSVWriter writer = new CSVWriter(new FileWriter("C:\\Data\\"
				+ beginTime + "-To-" + endTime + "-IRB-Billing-Report.csv"));
		String[] Titleentry = { "IRB Number","PI Name","Title","Agenda Date", "Form Type", "Review Type",
				"Responsible Institution", "Responsible Department",
				"Who Initiated", "Investigator Descriptior", "Support Types",
				"IRB Fee", "Funding Name", "Funding Amount", "Funding Type",
				"Funding Entity Name" };
		writer.writeNext(Titleentry);
		String qry = "select protocol_form_id from protocol_form_status "
				+ "where protocol_form_status in ('IRB_APPROVED','EXPEDITED_APPROVED') "
				+ "and id in (select max(id) from protocol_form_status where retired = 0 group by protocol_form_id) and modified>"
				+ beginTime
				+ " and modified <"
				+ endTime
				+ " and protocol_form_id in (select id from protocol_form where protocol_form_type in ('CONTINUING_REVIEW','NEW_SUBMISSION') and retired =0)";

		Query query = em.createNativeQuery(qry);
		List<BigInteger> pfIDs = (List<BigInteger>) query.getResultList();

		List<ProtocolFormStatusEnum> approvedStatus = Lists.newArrayList();
		approvedStatus.add(ProtocolFormStatusEnum.ACKNOWLEDGED);
		approvedStatus.add(ProtocolFormStatusEnum.APPROVED);
		approvedStatus.add(ProtocolFormStatusEnum.IRB_ACKNOWLEDGED);
		approvedStatus.add(ProtocolFormStatusEnum.IRB_APPROVED);

		for (BigInteger pfIDBig : pfIDs) {
			long pfID = pfIDBig.longValue();

			ProtocolForm pf = protocolFormDao.findById(pfID);
			ProtocolFormStatus pfstatus = protocolFormStatusDao
					.getLatestProtocolFormStatusByFormId(pf.getFormId());
			/*
			 * if(pf.getProtocolFormType()!=ProtocolFormType.NEW_SUBMISSION&&pf.
			 * getProtocolFormType()!=ProtocolFormType.CONTINUING_REVIEW){
			 * continue; } if(!approvedStatus.contains(pfstatus)){ continue; }
			 */
			String protocolxmlData = pf.getProtocol().getMetaDataXml();
			String protocolID = pf.getProtocol().getId() + "";
			String reviewType = "";
			if (pfstatus.getProtocolFormStatus().equals(
					ProtocolFormStatusEnum.IRB_APPROVED)) {
				reviewType = "Full Board";
			} else if (pfstatus.getProtocolFormStatus().equals(
					ProtocolFormStatusEnum.EXPEDITED_APPROVED)) {
				reviewType = "Expedited";
			}
			
			String piname = xmlHandler.getSingleStringValueByXPath(protocolxmlData, "/protocol/staffs/staff/user[roles/role/text()=\"Principal Investigator\"]/lastname/text()")+","+
					xmlHandler.getSingleStringValueByXPath(protocolxmlData, "/protocol/staffs/staff/user[roles/role/text()=\"Principal Investigator\"]/firstname/text()");
			String title = xmlHandler.getSingleStringValueByXPath(protocolxmlData, "/protocol/title/text()");
			String agendaDate= DateFormatUtil.formateDateToMDY(agendaDao.getAgendaByProtocolFormIdAndAgendaItemStatus(pfID,AgendaItemStatus.NEW).getDate());	

			String responsibleInstitution = xmlHandler
					.getSingleStringValueByXPath(protocolxmlData,
							"/protocol/site-responsible");
			String college = xmlHandler.getSingleStringValueByXPath(
					protocolxmlData,
					"/protocol/responsible-department/@collegedesc");
			String department = xmlHandler.getSingleStringValueByXPath(
					protocolxmlData,
					"/protocol/responsible-department/@subdeptdesc");
			
			if(!(responsibleInstitution.contains("uams")||responsibleInstitution.contains("ach-achri"))){
				continue;
			}
			
			
			
			if (!college.isEmpty() && !department.isEmpty()) {
				department = college + "-" + department;
			} else {
				department = college + department;
			}

			String whoInitiated = "";
			try {
				whoInitiated = xmlProcessor.listElementStringValuesByPath(
						"/protocol/study-type", protocolxmlData).get(0);
				if(!whoInitiated.contains("industry-sponsored")){
					continue;
				}
			} catch (Exception e) {

			}
			
			

			String investigatorDesc = xmlHandler
					.getSingleStringValueByXPath(protocolxmlData,
							"/protocol/study-type/investigator-initiated/investigator-description");

			Set<String> supportTypePath = Sets.newHashSet();
			supportTypePath
					.add("/protocol/study-type/investigator-initiated/sub-type");
			List<Element> subtypes = xmlProcessor.listDomElementsByPaths(
					supportTypePath, protocolxmlData);
			int subtypesSzie = subtypes.size();
			int subtypeIndex = 0;

			String irbFee = "";

			if (pf.getFormType().equals(
					ProtocolFormType.NEW_SUBMISSION.toString())) {
				ProtocolFormXmlData pfxd = protocolFormXmlDataDao
						.getLastProtocolFormXmlDataByProtocolFormIdAndType(
								pfID, ProtocolFormXmlDataType.PROTOCOL);
				String pfxdXml = pfxd.getXmlData();
				irbFee = xmlHandler
						.getSingleStringValueByXPath(
								pfxdXml,
								"/protocol/irb-fees/category[name/text()[contains(., \"(New Submission)\")]]/fee");
			} else if (pf.getFormType().equals(
					ProtocolFormType.CONTINUING_REVIEW.toString())) {
				try {
					List<ProtocolForm> modificationLists = protocolFormDao
							.listProtocolFormsByProtocolIdAndProtocolFormType(
									pf.getProtocol().getId(),
									ProtocolFormType.MODIFICATION);
					ProtocolForm latestModificationBeforeContinuing = null;
					for (ProtocolForm mf : modificationLists) {
						if (mf.getFormId() < pfID) {
							latestModificationBeforeContinuing = mf;
						} else {
							break;
						}
					}
					if (latestModificationBeforeContinuing != null) {
						ProtocolFormXmlData pfxd = protocolFormXmlDataDao
								.getLastProtocolFormXmlDataByProtocolFormIdAndType(
										latestModificationBeforeContinuing
												.getFormId(),
										ProtocolFormXmlDataType.MODIFICATION);
						String pfxdXml = pfxd.getXmlData();
						irbFee = xmlHandler
								.getSingleStringValueByXPath(
										pfxdXml,
										"/protocol/irb-fees/category[name/text()[contains(., \"(Continuing Review)\")]]/fee");

					}
				} catch (Exception e) {
					ProtocolFormXmlData pfxd = protocolFormXmlDataDao
							.getLastProtocolFormXmlDataByProtocolFormIdAndType(
									pfID, ProtocolFormXmlDataType.PROTOCOL);
					String pfxdXml = pfxd.getXmlData();
					irbFee = xmlHandler
							.getSingleStringValueByXPath(
									pfxdXml,
									"/protocol/irb-fees/category[name/text()[contains(., \"(Continuing Review)\")]]/fee");
				}
			}

			Set<String> fundingPath = Sets.newHashSet();
			fundingPath.add("/protocol/funding/funding-source");
			List<Element> fundings = xmlProcessor.listDomElementsByPaths(
					fundingPath, protocolxmlData);

			boolean firstFunding = true;
			for (Element funding : fundings) {
				String fundingName = funding.getAttribute("name");
				String fundingAmount = funding.getAttribute("amount");
				String fundingEntityName = funding.getAttribute("entityname");
				String type = funding.getAttribute("type");
				if(type.equals("Internal")||type.equals("None")){
					if(!fundingName.isEmpty()){
						fundingName = "Fund: "+fundingName;
					}
					if(!fundingEntityName.isEmpty()){
						fundingEntityName = "Cost Center: "+fundingEntityName;
					}
					fundingEntityName = fundingName +" "+fundingEntityName;
					fundingName ="";
				}
				
				if (firstFunding) {
					String subtypeStr = "";
					if (subtypeIndex < subtypesSzie) {
						subtypeStr = subtypes.get(subtypeIndex)
								.getTextContent();
						subtypeIndex++;
					}
					String[] entry = { protocolID,piname,title,agendaDate, pf.getFormType() + "",
							reviewType, responsibleInstitution, department,
							whoInitiated, investigatorDesc, subtypeStr, irbFee,
							fundingName, fundingAmount, type, fundingEntityName };
					writer.writeNext(entry);
					firstFunding = false;
				} else {
					String subtypeStr = "";
					if (subtypeIndex < subtypesSzie) {
						subtypeStr = subtypes.get(subtypeIndex)
								.getTextContent();
						subtypeIndex++;
					}
					String[] entry = { "", "","", "", "", "","", "", "", "", subtypeStr,
							"", fundingName, fundingAmount, type,
							fundingEntityName };
					writer.writeNext(entry);
				}
			}

			if (fundings.size() == 0) {
				String subtypeStr = "";
				if (subtypeIndex < subtypesSzie) {
					subtypeStr = subtypes.get(subtypeIndex).getTextContent();
					subtypeIndex++;
				}
				String[] entry = { protocolID,piname,title,agendaDate, pf.getFormType() + "",
						reviewType, responsibleInstitution, department,
						whoInitiated, investigatorDesc, subtypeStr, irbFee };
				writer.writeNext(entry);
			}
			while (subtypeIndex < subtypesSzie) {
				String subtypeStr = "";
				subtypeStr = subtypes.get(subtypeIndex).getTextContent();
				subtypeIndex++;
				String[] entry = { "", "", "", "", "", "", "", "", "", "", subtypeStr };
				writer.writeNext(entry);
			}

		}
		writer.flush();
		writer.close();

	}

	// @Test
	public void generateReport9202013Kate() throws IOException,
			XPathExpressionException, SAXException {
		/***
		 * select distinct protocol_id from protocol_form pf where id in (
		 * select protocol_form_id from protocol_form_status pfs where
		 * pfs.retired =0 and pfs.protocol_form_status LIKE '%IRB%' ) and
		 * pf.retired = 0 AND protocol_id > 200000 and protocol_id in (select
		 * distinct p.id from protocol p, protocol_status ps where p.retired =0
		 * and ps.retired=0 and p.id =ps.protocol_id and ps.protocol_status not
		 * in (
		 * 'DRAFT','PENDING_PL_ENDORSEMENT','PENDING_PI_ENDORSEMENT','PENDING_TP_ENDORSEMENT
		 * ' ) )
		 ***/
		FileInputStream fstream;
		fstream = new FileInputStream("C:\\Data\\id.txt");
		DataInputStream in = new DataInputStream(fstream);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String strLine;
		CSVWriter writer = new CSVWriter(new FileWriter(
				"C:\\Data\\New-Submission-IRB-Report.csv"));
		String[] Titleentry = { "IRB Number", "Primarily Responsible",
				"Primarily Responsible Department", "Who Initiated",
				"Support Types", "Funding Name", "Funding Amount",
				"Funding Type", "Funding Entity Name" };
		writer.writeNext(Titleentry);

		while ((strLine = br.readLine()) != null) {
			String protocolIDStr = strLine;
			Protocol p = protocolDao.findById(Long.valueOf(protocolIDStr));
			String xmlData = p.getMetaDataXml();

			String responsibleInstitution = "";
			try {
				responsibleInstitution = xmlProcessor
						.listElementStringValuesByPath(
								"/protocol/site-responsible", xmlData).get(0);
			} catch (Exception e) {

			}
			String college = xmlHandler.getSingleStringValueByXPath(xmlData,
					"/protocol/responsible-department/@collegedesc");
			String department = xmlHandler.getSingleStringValueByXPath(xmlData,
					"/protocol/responsible-department/@subdeptdesc");
			department = college + "-" + department;

			String whoInitiated = "";
			try {
				whoInitiated = xmlProcessor.listElementStringValuesByPath(
						"/protocol/study-type", xmlData).get(0);
			} catch (Exception e) {

			}

			Set<String> supportTypePath = Sets.newHashSet();
			supportTypePath
					.add("/protocol/study-type/investigator-initiated/sub-type");
			List<Element> subtypes = xmlProcessor.listDomElementsByPaths(
					supportTypePath, xmlData);
			int subtypesSzie = subtypes.size();
			int subtypeIndex = 0;

			Set<String> fundingPath = Sets.newHashSet();
			fundingPath.add("/protocol/funding/funding-source");
			List<Element> fundings = xmlProcessor.listDomElementsByPaths(
					fundingPath, xmlData);

			boolean firstFunding = true;
			for (Element funding : fundings) {
				String fundingName = funding.getAttribute("name");
				String fundingAmount = funding.getAttribute("amount");
				String fundingEntityName = funding.getAttribute("entityname");
				String type = funding.getAttribute("type");
				if (firstFunding) {
					String subtypeStr = "";
					if (subtypeIndex < subtypesSzie) {
						subtypeStr = subtypes.get(subtypeIndex)
								.getTextContent();
						subtypeIndex++;
					}
					String[] entry = { protocolIDStr, responsibleInstitution,
							department, whoInitiated, subtypeStr, fundingName,
							fundingAmount, type, fundingEntityName };
					writer.writeNext(entry);
					firstFunding = false;
				} else {
					String subtypeStr = "";
					if (subtypeIndex < subtypesSzie) {
						subtypeStr = subtypes.get(subtypeIndex)
								.getTextContent();
						subtypeIndex++;
					}
					String[] entry = { "", "", "", "", subtypeStr, fundingName,
							fundingAmount, type, fundingEntityName };
					writer.writeNext(entry);
				}
			}

			if (fundings.size() == 0) {
				String subtypeStr = "";
				if (subtypeIndex < subtypesSzie) {
					subtypeStr = subtypes.get(subtypeIndex).getTextContent();
					subtypeIndex++;
				}
				String[] entry = { protocolIDStr, responsibleInstitution,
						department, whoInitiated, subtypeStr };
				writer.writeNext(entry);
			}
			while (subtypeIndex < subtypesSzie) {
				String subtypeStr = "";
				subtypeStr = subtypes.get(subtypeIndex).getTextContent();
				subtypeIndex++;
				String[] entry = { "", "", "", "", subtypeStr };
				writer.writeNext(entry);
			}

		}

		writer.flush();
		writer.close();
	}

	// @Test
	public void generateReport9192013Jeniffer() throws IOException,
			XPathExpressionException, SAXException {
/***
		 * SELECT max(pf.id) FROM protocol_form pf
		WHERE  pf.retired = 0 
		AND protocol_form_type ='new_submission' and pf.created > '2013/07/01' and pf.created<'2013-08-31'

		and pf.id  in (select distinct  protocol_form_id from protocol_form_status where id in (select max(id) from protocol_form_status where retired =0 group by protocol_form_id)and  
		protocol_form_status not in ('DRAFT','PENDING_PL_ENDORSEMENT','PENDING_PI_ENDORSEMENT','PENDING_TP_ENDORSEMENT'))group by protocol_id
		 * ***/

		FileInputStream fstream;
		fstream = new FileInputStream("C:\\Data\\id.txt");
		DataInputStream in = new DataInputStream(fstream);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String strLine;
		/*
		 * CSVWriter writer =new CSVWriter(new
		 * FileWriter("C:\\Data\\NewSubmission-Report.csv"));
		 */
		CSVWriter writer = new CSVWriter(new FileWriter(
				"C:\\Data\\Continuing Review-Report.csv"));
		String[] Titleentry = { "IRB Number", "Review Type",
				"Responsible Institution", "Student Involve", "irbFee",
				"Funding Name", "Funding Amount", "Funding Type",
				"Funding Entity Name" };
		writer.writeNext(Titleentry);
		while ((strLine = br.readLine()) != null) {
			long pfID = Long.valueOf(strLine);
			ProtocolForm pf = protocolFormDao.findById(pfID);
			String xmlData = pf.getMetaDataXml();

			// for continuing review
			Protocol p = pf.getProtocol();
			String protocolxmlData = p.getMetaDataXml();

			String reviewType = xmlHandler.getSingleStringValueByXPath(xmlData,
					"//summary/irb-determination/suggested-type");
			String responsibleInstitution = xmlHandler
					.getSingleStringValueByXPath(protocolxmlData,
							"/protocol/site-responsible");
			String studentInvolve = xmlHandler
					.getSingleStringValueByXPath(protocolxmlData,
							"/protocol/study-type/investigator-initiated/investigator-description");
			if (studentInvolve.contains("student-fellow-resident-post-doc")) {
				studentInvolve = "Yes";
			} else {
				studentInvolve = "No";
			}

			// this part for new submission

			/*
			 * ProtocolFormXmlData pfxd = protocolFormXmlDataDao.
			 * getLastProtocolFormXmlDataByProtocolFormIdAndType(pfID,
			 * ProtocolFormXmlDataType.PROTOCOL); String pfxdXml =
			 * pfxd.getXmlData(); String irbFee =
			 * xmlHandler.getSingleStringValueByXPath(pfxdXml,
			 * "/protocol/irb-fees/category[name/text()[contains(., \"(New Submission)\")]]/fee"
			 * );
			 */

			// this part for continuing review irb fee
			List<ProtocolForm> modificationLists = protocolFormDao
					.listProtocolFormsByProtocolIdAndProtocolFormType(pf
							.getProtocol().getId(),
							ProtocolFormType.MODIFICATION);
			ProtocolForm latestModificationBeforeContinuing = null;
			for (ProtocolForm mf : modificationLists) {
				if (mf.getFormId() < pfID) {
					latestModificationBeforeContinuing = mf;
				} else {
					break;
				}
			}
			String irbFee = "";
			if (latestModificationBeforeContinuing != null) {
				/*
				 * logger.debug(latestModificationBeforeContinuing.getFormId() +
				 * "");
				 */
				ProtocolFormXmlData pfxd = protocolFormXmlDataDao
						.getLastProtocolFormXmlDataByProtocolFormIdAndType(
								latestModificationBeforeContinuing.getFormId(),
								ProtocolFormXmlDataType.MODIFICATION);
				String pfxdXml = pfxd.getXmlData();
				irbFee = xmlHandler
						.getSingleStringValueByXPath(
								pfxdXml,
								"/protocol/irb-fees/category[name/text()[contains(., \"(Continuing Review)\")]]/fee");

			}

			Set<String> fundingPath = Sets.newHashSet();
			fundingPath.add("/protocol/funding/funding-source");
			List<Element> fundings = xmlProcessor.listDomElementsByPaths(
					fundingPath, protocolxmlData);
			boolean firstFunding = true;
			for (Element funding : fundings) {
				String fundingName = funding.getAttribute("name");
				String fundingAmount = funding.getAttribute("amount");
				String fundingEntityName = funding.getAttribute("entityname");
				String type = funding.getAttribute("type");
				if (firstFunding) {
					String[] entry = { pf.getProtocol().getId() + "",
							reviewType, responsibleInstitution, studentInvolve,
							irbFee, fundingName, fundingAmount, type,
							fundingEntityName };
					writer.writeNext(entry);
					firstFunding = false;
				} else {
					String[] entry = { "", "", "", "", "", fundingName,
							fundingAmount, type, fundingEntityName };
					writer.writeNext(entry);
				}
			}

			if (fundings.size() == 0) {
				String[] entry = { pf.getProtocol().getId() + "", reviewType,
						responsibleInstitution, studentInvolve, irbFee };
				writer.writeNext(entry);
			}

		}
		writer.flush();
		writer.close();
	}
	
	// @Test
	public void processCrimsonContractInfo() throws IOException,
			XPathExpressionException, SAXException {
		/***
		 * This code is used to merge records queried from crimson contract
		 ***/

		String qry = "select contract.[txt_contract_number], users.first+' '+users.lname, ctype.[txt_contract_type], contractinfo.[num_irb_ID],contact.[txt_contact_party], cstatus.[txt_approvalstatus], contract.[date_created] from [HOSP_SQL1].[ClinicalResearch].[dbo].[multipleselect] mts, [HOSP_SQL1].[ClinicalResearch].[dbo].[aria_users] users, [HOSP_SQL1].[ClinicalResearch].[dbo].[contract_info] contractinfo, [HOSP_SQL1].[ClinicalResearch].[dbo].[contract_contact] contact, [HOSP_SQL1].[ClinicalResearch].[dbo].[contract] contract, [HOSP_SQL1].[ClinicalResearch].[dbo].[contract_type] ctype, [HOSP_SQL1].[ClinicalResearch].[dbo].[contract_approvalstatus] cstatus where mts.[num_connection_ID] = contact.[num_contract_ID]  and contact.[num_contract_ID] =contractinfo.[num_contract_ID] and contract.[num_contract_ID] = mts.[num_connection_ID] and cstatus.[num_approvalstatus] = contract.[num_approvalstatus] and mts.[txt_value] = users.[pi_serial] and mts.[num_type] =1 and ctype.[num_contract_type_ID] = contractinfo.[num_contract_type_ID] and cstatus.[num_approvalstatus] <> -30";
		Query query = em.createNativeQuery(qry);
		List<Object[]> results = (List<Object[]>) query.getResultList();
		
		CSVWriter writer = new CSVWriter(new FileWriter(
				"C:\\Data\\crimsonContractInfo2.csv"));
		String[] Titleentry = { "Contract ID", "PI", "Type", "IRB#", "Entity",
				"Status", "Created Date" };
		writer.writeNext(Titleentry);
		Map<String,Object[]> resultsMap =Maps.newHashMap();
		Map<String,String> piMap =Maps.newHashMap();
		Map<String,String> entityMap =Maps.newHashMap();
		for(Object[] result : results){
			String contractid = (String)result[0];
			String pi = (String)result[1];
			String entity = (String)result[4];
			if(!resultsMap.containsKey(contractid)){
				resultsMap.put(contractid, result);
			}
			
			if(piMap.containsKey(contractid)){
				if(!piMap.get(contractid).contains(pi)){
				piMap.put(contractid, piMap.get(contractid)+"; "+pi);
				}
			}else{
				piMap.put(contractid, pi);
			}
			
			if(entityMap.containsKey(contractid)){
				if(!entityMap.get(contractid).contains(entity)){
				entityMap.put(contractid, entityMap.get(contractid)+"; "+entity);
				}
			}else{
				entityMap.put(contractid, entity);
			}
		}
		
		for(String contractid : resultsMap.keySet()){
			Object[] result  = resultsMap.get(contractid);
			String pi = (String)result[1];
			String entity = (String)result[4];
			
			if(piMap.containsKey(contractid)){
				pi=piMap.get(contractid);
			}
			
			if(entityMap.containsKey(contractid)){
				entity=entityMap.get(contractid);
			}
			
			String[] entry = {contractid, pi, (String)result[2], (Integer)result[3]+"", entity,
					(String)result[5], (Date) result[6]+"" };
			writer.writeNext(entry);
		}
		writer.flush();
		writer.close();
		
	 }
	
	
	//@Test
	public void findresearchstaffnamereport() throws IOException{
		List<String> budgetAdmins = Lists.newArrayList();
		List<String> budgetManagers = Lists.newArrayList();
		List<String> studyCoordinators = Lists.newArrayList();
		List<String> researchStaffs = Lists.newArrayList();
		
		CSVWriter writer = new CSVWriter(new FileWriter("C:\\Data\\NameList.csv"));
		String[] Titleentry = { "Budget Administrator","Budget Manager","Study Coordinator","Research Staff" };
		writer.writeNext(Titleentry);
		
		String qry1 = "select distinct meta_data_xml.value('(/protocol/staffs/staff/user[roles/role[fn:contains(fn:upper-case(.),\"BUDGET ADMINISTRATOR\")]]/lastname/text())[1]','varchar(50)')+ ' ' +meta_data_xml.value('(/protocol/staffs/staff/user[roles/role[fn:contains(fn:upper-case(.),\"BUDGET ADMINISTRATOR\")]]/firstname/text())[1]','varchar(50)') from protocol where retired = 0 and  meta_data_xml.exist('/protocol/staffs/staff/user/roles/role[text()=\"Budget Administrator\"]')=1";
		String qry2 = "select distinct meta_data_xml.value('(/protocol/staffs/staff/user[roles/role[fn:contains(fn:upper-case(.),\"BUDGET MANAGER\")]]/lastname/text())[1]','varchar(50)')+ ' ' +meta_data_xml.value('(/protocol/staffs/staff/user[roles/role[fn:contains(fn:upper-case(.),\"BUDGET MANAGER\")]]/firstname/text())[1]','varchar(50)') from protocol where retired = 0 and  meta_data_xml.exist('/protocol/staffs/staff/user/roles/role[text()=\"Budget Manager\"]')=1";
		String qry3 = "select distinct meta_data_xml.value('(/protocol/staffs/staff/user[roles/role[fn:contains(fn:upper-case(.),\"STUDY COORDINATOR\")]]/lastname/text())[1]','varchar(50)')+ ' ' +meta_data_xml.value('(/protocol/staffs/staff/user[roles/role[fn:contains(fn:upper-case(.),\"STUDY COORDINATOR\")]]/firstname/text())[1]','varchar(50)') from protocol where retired = 0 and  meta_data_xml.exist('/protocol/staffs/staff/user/roles/role[text()=\"Study Coordinator\"]')=1";
		String qry4 = "select distinct meta_data_xml.value('(/protocol/staffs/staff/user[roles/role[fn:contains(fn:upper-case(.),\"SUPPORT STAFF\")]]/lastname/text())[1]','varchar(50)')+ ' ' +meta_data_xml.value('(/protocol/staffs/staff/user[roles/role[fn:contains(fn:upper-case(.),\"SUPPORT STAFF\")]]/firstname/text())[1]','varchar(50)') from protocol where retired = 0 and  meta_data_xml.exist('/protocol/staffs/staff/user/roles/role[text()=\"Support Staff\"]')=1";
		
		int maxSize = 0;
		Query query = em.createNativeQuery(qry1);
		budgetAdmins = (List<String>) query.getResultList();
		if(maxSize<budgetAdmins.size()){
			maxSize=budgetAdmins.size();
		}
		query = em.createNativeQuery(qry2);
		budgetManagers = (List<String>) query.getResultList();
		if(maxSize<budgetManagers.size()){
			maxSize=budgetManagers.size();
		}
		query = em.createNativeQuery(qry3);
		studyCoordinators = (List<String>) query.getResultList();
		if(maxSize<studyCoordinators.size()){
			maxSize=studyCoordinators.size();
		}
		query = em.createNativeQuery(qry4);
		researchStaffs = (List<String>) query.getResultList();
		if(maxSize<researchStaffs.size()){
			maxSize=researchStaffs.size();
		}
		
		for(int i=0;i<maxSize;i++){
			String budgetAdmin = "";
			String budgetManager="";
			String studyCoordinator = "";
			String researchStaff = "";
			
			try{
				budgetAdmin = budgetAdmins.get(i);
			}catch(Exception e){
			}
			
			try{
				budgetManager = budgetManagers.get(i);
			}catch(Exception e){
			}
			
			try{
				studyCoordinator = studyCoordinators.get(i);
			}catch(Exception e){
			}
			
			try{
				researchStaff = researchStaffs.get(i);
				if(budgetAdmins.contains(researchStaff)||budgetManagers.contains(researchStaff)||studyCoordinators.contains(researchStaff)){
					researchStaff = "";
				}
			}catch(Exception e){
			}
			
			String[] entry = {budgetAdmin,budgetManager,studyCoordinator,researchStaff};
			writer.writeNext(entry);
		}
		writer.flush();
		writer.close();
	}
	
	//@Test
	public void budgetandcancerStudiesInCrimsonAndClaraReportProcess() throws IOException{
		 CSVReader reader = new CSVReader(new FileReader("C:\\Users\\yuanjiawei\\Desktop\\Submitted Studies3.csv"));
		 CSVWriter writer = new CSVWriter(new FileWriter(
					"C:\\Data\\Submitted Studies3.csv"));
			String[] title = { "IRB", "PI", "Title", "Submitted Date","Approved Date" };
			writer.writeNext(title);
		    String [] nextLine;
		    boolean firstline = true;
		    List<String> existingIRB = Lists.newArrayList();
		    while ((nextLine = reader.readNext()) != null) {
		    	if(firstline){
		    		firstline = false;
		    		continue;
		    	}
		    if(existingIRB.contains(nextLine[0])||nextLine[1].contains("Jiang,Bian")||nextLine[1].contains("Gail,Douglas")||nextLine[1].contains("Xiaoran,Wang")){
		    	continue;
		    }
		    existingIRB.add(nextLine[0]);
		    writer.writeNext(nextLine);
		    
		    }
		    
		    reader.close();
		    writer.flush();
			writer.close();
	}
	
	
	//@Test
	public void generateCrimsonSubmittedStudyReport() throws IOException{
		
		Map<String,String> timeranges = Maps.newHashMap();
		timeranges.put("2007-07-01", "2009-10-31");
		timeranges.put("2009-11-01", "2012-12-31");
		timeranges.put("2013-01-01", "2018-10-31");
		
		CSVWriter writer = new CSVWriter(new FileWriter("C:\\Data\\Submitted Studies With Time Range"+".csv"));
	    String[] title = { "IRB", "PI", "Title", "Submitted Date","IRB Approved","Study Creation to Study Submission(Days)","Time With PI (Days)","Time With RSC (days)" };

	    
		for(String startDate:timeranges.keySet()){
			String endDate = timeranges.get(startDate);
			
		String queryStr = "select ct.[num_ct_ID], ct.[num_irb_ID],auser.first+','+auser.[lname] as PI, Replace(cast(ct.txt_title as varchar(max)),CHAR(13)+CHAR(10),' '), review.[txt_date], ct.date_created from [HOSP_SQL1].[ClinicalResearch].[dbo].[ct_class] as class,[HOSP_SQL1].[ClinicalResearch].[dbo].[review] as review,[HOSP_SQL1].[ClinicalResearch].[dbo].[ct] as ct, [HOSP_SQL1].[ClinicalResearch].[dbo].[aria_users] as auser where review.num_review_ID in (select min(num_review_ID) from [HOSP_SQL1].[ClinicalResearch].[dbo].[review] where [num_rev_entity] = 14 and [num_rev_value] = 8 group by num_ct_ID) and review.num_ct_ID = ct.num_ct_ID and ct.num_pi = auser.[pi_serial] and review.num_ct_ID in (select distinct [num_ct_ID] from [HOSP_SQL1].[ClinicalResearch].[dbo].[budget]) and class.num_ct_ID = ct.num_ct_ID and class.[num_class_ID] in (1,2) and ct.[num_ct_status] <> 10 and DATEDIFF(DAY,txt_date,'"+startDate+"')<0 and DATEDIFF(DAY,txt_date,'"+endDate+"')>0 and ct.num_ct_ID in (select min(num_ct_ID)  from [HOSP_SQL1].[ClinicalResearch].[dbo].ct group by [num_irb_ID])order by [num_review_ID] asc";
		String irbQueryStrInti = "SELECT [date_irb_approval] FROM [HOSP_SQL1].[ClinicalResearch].[dbo].[irb] where [num_irb_ID] =";
		String timeRangeByEntityQueryInti = "SELECT [num_rev_entity] ,[num_rev_value] ,[txt_date] FROM [HOSP_SQL1].[ClinicalResearch].[dbo].[review] where [num_ct_ID]=";

			    writer.writeNext(title);
	    
	    List<String> existingIRB = Lists.newArrayList();
		Query query = em.createNativeQuery(queryStr);
		List<Object[]> results = query.getResultList();
		
		long piaverage =0;
		long rscaverage =0;
		for(Object[] resultRow : results){
			String[] entry = new String[title.length];
			
			int ctID = (Integer) resultRow[0];
			entry[0] = ""+(Integer) resultRow[1];
			entry[1] = (String) resultRow[2];
			entry[2] = (String) resultRow[3];
			entry[3] = DateFormat.getInstance().format((Date) resultRow[4]);
			
			String irbQueryStr = irbQueryStrInti+entry[0];
			Query query2 = em.createNativeQuery(irbQueryStr);
			entry[4] = "No";
			try{
			Date irbApprovals = (Date) query2.getSingleResult();
			if(irbApprovals != null){
				entry[4] = "Yes";
			}
			}catch(Exception e){
				
			}
			
			String timeRangeQuery = timeRangeByEntityQueryInti+ctID;
			
			query2 = em.createNativeQuery(timeRangeQuery);
			List<Object[]> actions = query2.getResultList();
			
			entry[5] = ""+roundUp(((Date) resultRow[4]).getTime()-((Date) resultRow[5]).getTime(),(24 * 60 * 60 * 1000));
			//long piTime= ((Date) resultRow[4]).getTime()-((Date) resultRow[5]).getTime();
			long piTime= 0;
			long budgetTime= 0;
			long coverTime= 0;
			if(actions.size()>0){
				long pistart = 0;
				long piend = 0;
				long budgetstart = 0;
				long budgetend = 0;
				long coverstart = 0;
				long coverend = 0;
				
				for(int i =0; i<actions.size();i++){
					Object[] action = actions.get(i);
					
					//process pi
					if((short)action[0] ==14){
						//pi in modification
						if((short)action[1] ==14){
							pistart = ((Date)action[2]).getTime();
						}else if((short)action[1] ==8 &&pistart>0 ){
							piend = ((Date)action[2]).getTime();
						}
						
						//pi crimson complete
						if((short)action[1] ==11){
							piend = ((Date)action[2]).getTime();
							Object[] preAction = actions.get(i-1);
							pistart = ((Date)preAction[2]).getTime();
						}
						
						if(pistart >0 && piend >0){
							piTime += piend-pistart;
							piend = 0;
							pistart = 0;
						}
					}
					
					
					//process budget
					if((short)action[0] ==2){
						//in review
						if((short)action[1] ==3){
							budgetstart = ((Date)action[2]).getTime();
						}else if(((short)action[1] ==1 ||(short)action[1] ==2||(short)action[1] ==4) &&budgetstart>0 ){
							budgetend = ((Date)action[2]).getTime();
							coverstart = ((Date)action[2]).getTime();
						}
						
						if(budgetstart >0 && budgetend >0){
							budgetTime += budgetend-budgetstart;
							budgetend = 0;
							budgetstart = 0;
						}
					}
					
					//process coverage
					if((short)action[0] ==3){
						//in review
						/*if((short)action[1] ==3){
							coverstart = ((Date)action[2]).getTime();
						}else */
							
						if(((short)action[1] ==1 ||(short)action[1] ==2) &&coverstart>0 ){
							coverend = ((Date)action[2]).getTime();
						}
						
						if(coverstart >0 && coverend >0){
							coverTime += coverend-coverstart;
							coverend = 0;
							coverstart = 0;
						}
					}
					
					
				}
				
				if(pistart>0 &&piend ==0){
					piend =  new Date().getTime();
				}
				
				if(budgetstart>0 &&budgetend ==0){
					budgetend =  new Date().getTime();
				}
				
				if(coverstart>0 &&coverend ==0){
					coverend =  new Date().getTime();
				}
			}
			piaverage+= piTime;
			piTime = roundUp(piTime,(24 * 60 * 60 * 1000));
			
			entry[6] = ""+piTime;
			long rscTime = budgetTime + coverTime;
			rscaverage +=rscTime;
			rscTime = roundUp(rscTime,(24 * 60 * 60 * 1000));
			entry[7] = ""+rscTime;
			
			//logger.debug((Integer) resultRow[1]+"  "+piTime+ "   "+ rscTime+ " "+actions.size());
			
			if(existingIRB.contains(entry[0])||entry[1].contains("Jiang,Bian")||entry[1].contains("Gail,Douglas")||entry[1].contains("Xiaoran,Wang")){
		    	continue;
		    }
			existingIRB.add(entry[1]);
			writer.writeNext(entry);
		}
		String[] blank = { "", "", "", "","","","" };
		logger.debug(roundUp(piaverage/existingIRB.size(),(24 * 60 * 60 * 1000))+"  "+roundUp(rscaverage/existingIRB.size(),(24 * 60 * 60 * 1000)));
		writer.writeNext(blank);
		
		}
		writer.flush();
		writer.close();
	}
	
	//@Test
	public void generateCrimsonApprovedStudyReport() throws IOException{
		Map<String,String> timeranges = Maps.newHashMap();
		timeranges.put("2007-07-01", "2009-10-31");
		timeranges.put("2009-11-01", "2012-12-31");
		timeranges.put("2013-01-01", "2018-10-31");
		CSVWriter writer = new CSVWriter(new FileWriter("C:\\Data\\Approved Studies With Time Range.csv"));
	    String[] title = { "IRB", "PI", "Title", "Submitted Date","Approved Date","IRB Approved","Study Creation to Study Submission(Days)","Time With PI (Days)","Time With RSC (days)" };
	    writer.writeNext(title);
		for(String startDate:timeranges.keySet()){
			String endDate = timeranges.get(startDate);
		
		String queryStr = "select ct.[num_ct_ID], ct.[num_irb_ID],auser.first+','+auser.[lname] as PI, Replace(cast(ct.txt_title as varchar(max)),CHAR(13)+CHAR(10),' '), review1.txt_date as date1, review2.[txt_date] as date2,ct.date_created from [HOSP_SQL1].[ClinicalResearch].[dbo].[ct_class] as class,[HOSP_SQL1].[ClinicalResearch].[dbo].[review] as review1,[HOSP_SQL1].[ClinicalResearch].[dbo].[review] as review2,[HOSP_SQL1].[ClinicalResearch].[dbo].[ct] as ct, [HOSP_SQL1].[ClinicalResearch].[dbo].[aria_users] as auser where  review2.num_review_ID in (select min(review3.num_review_ID) from [HOSP_SQL1].[ClinicalResearch].[dbo].[review] as review3 where   [num_rev_value] = 11 group by num_ct_ID) and review1.num_review_ID in (select min(review4.num_review_ID) from [HOSP_SQL1].[ClinicalResearch].[dbo].[review] as review4 where [num_rev_entity] = 14 and [num_rev_value] = 8 group by num_ct_ID) and review2.num_ct_ID = ct.num_ct_ID and ct.num_pi = auser.[pi_serial] and review1.num_ct_ID = ct.num_ct_ID and review2.num_ct_ID in (select distinct [num_ct_ID] from [HOSP_SQL1].[ClinicalResearch].[dbo].[budget]) and ct.[num_ct_status] <> 10 and ct.num_ct_ID in (select min(num_ct_ID)  from [HOSP_SQL1].[ClinicalResearch].[dbo].ct group by [num_irb_ID]) and class.num_ct_ID = ct.num_ct_ID and class.[num_class_ID] in (1,2) and DATEDIFF(DAY,review2.txt_date,'"+startDate+"')<0 and DATEDIFF(DAY,review2.txt_date,'"+endDate+"')>0 and ct.num_ct_ID in (select min(num_ct_ID)  from [HOSP_SQL1].[ClinicalResearch].[dbo].ct group by [num_irb_ID])";
				
		String irbQueryStrInti = "SELECT [date_irb_approval] FROM [HOSP_SQL1].[ClinicalResearch].[dbo].[irb] where [num_irb_ID] =";
		String timeRangeByEntityQueryInti = "SELECT [num_rev_entity] ,[num_rev_value] ,[txt_date] FROM [HOSP_SQL1].[ClinicalResearch].[dbo].[review] where [num_ct_ID]=";

		
	    
	    List<String> existingIRB = Lists.newArrayList();
		Query query = em.createNativeQuery(queryStr);
		List<Object[]> results = query.getResultList();
		long piaverage =0;
		long rscaverage =0;
		for(Object[] resultRow : results){
			String[] entry = new String[title.length];
			
			int ctID = (Integer) resultRow[0];
			entry[0] = ""+(Integer) resultRow[1];
			entry[1] = (String) resultRow[2];
			entry[2] = (String) resultRow[3];
			entry[3] = DateFormat.getInstance().format((Date) resultRow[4]);
			entry[4] = DateFormat.getInstance().format((Date) resultRow[5]);
			
			String irbQueryStr = irbQueryStrInti+entry[0];
			Query query2 = em.createNativeQuery(irbQueryStr);
			entry[5] = "No";
			try{
			Date irbApprovals = (Date) query2.getSingleResult();
			if(irbApprovals != null){
				entry[5] = "Yes";
			}
			}catch(Exception e){
				
			}
			
			String timeRangeQuery = timeRangeByEntityQueryInti+ctID;
			
			query2 = em.createNativeQuery(timeRangeQuery);
			List<Object[]> actions = query2.getResultList();
			
			//long piTime= ((Date) resultRow[4]).getTime()-((Date) resultRow[6]).getTime();
			entry[6] = ""+roundUp(((Date) resultRow[4]).getTime()-((Date) resultRow[6]).getTime(),(24 * 60 * 60 * 1000));
			long piTime= 0;
			long budgetTime= 0;
			long coverTime= 0;
			if(actions.size()>0){
				long pistart = 0;
				long piend = 0;
				long budgetstart = 0;
				long budgetend = 0;
				long coverstart = 0;
				long coverend = 0;
				
				for(int i =0; i<actions.size();i++){
					Object[] action = actions.get(i);
					
					//process pi
					if((short)action[0] ==14){
						//pi in modification
						if((short)action[1] ==14){
							pistart = ((Date)action[2]).getTime();
						}else if((short)action[1] ==8 &&pistart>0 ){
							piend = ((Date)action[2]).getTime();
						}
						
						//pi crimson complete
						if((short)action[1] ==11){
							piend = ((Date)action[2]).getTime();
							Object[] preAction = actions.get(i-1);
							pistart = ((Date)preAction[2]).getTime();
						}
						
						if(pistart >0 && piend >0){
							piTime += piend-pistart;
							piend = 0;
							pistart = 0;
						}
					}
					
					
					//process budget
					if((short)action[0] ==2){
						//in review
						if((short)action[1] ==3){
							budgetstart = ((Date)action[2]).getTime();
						}else if(((short)action[1] ==1 ||(short)action[1] ==2||(short)action[1] ==4) &&budgetstart>0 ){
							budgetend = ((Date)action[2]).getTime();
							coverstart = ((Date)action[2]).getTime();
						}
						
						if(budgetstart >0 && budgetend >0){
							budgetTime += budgetend-budgetstart;
							budgetend = 0;
							budgetstart = 0;
						}
					}
					
					//process coverage
					if((short)action[0] ==3){
						//in review
						/*if((short)action[1] ==3){
							coverstart = ((Date)action[2]).getTime();
						}else */
							
						if(((short)action[1] ==1 ||(short)action[1] ==2) &&coverstart>0 ){
							coverend = ((Date)action[2]).getTime();
						}
						
						if(coverstart >0 && coverend >0){
							coverTime += coverend-coverstart;
							coverend = 0;
							coverstart = 0;
						}
					}
					
					
				}
				
				if(pistart>0 &&piend ==0){
					piend =  new Date().getTime();
				}
				
				if(budgetstart>0 &&budgetend ==0){
					budgetend =  new Date().getTime();
				}
				
				if(coverstart>0 &&coverend ==0){
					coverend =  new Date().getTime();
				}
			}
			piaverage+= piTime;
			piTime = roundUp(piTime,(24 * 60 * 60 * 1000));
			entry[7] = ""+piTime;
			long rscTime = budgetTime + coverTime;
			rscaverage +=rscTime;
			rscTime = roundUp(rscTime,(24 * 60 * 60 * 1000));
			entry[8] = ""+rscTime;
			
			//logger.debug((Integer) resultRow[1]+"  "+piTime+ "   "+ rscTime+ " "+actions.size());
			
			if(existingIRB.contains(entry[0])||entry[1].contains("Jiang,Bian")||entry[1].contains("Gail,Douglas")||entry[1].contains("Xiaoran,Wang")){
		    	continue;
		    }
			existingIRB.add(entry[1]);
			writer.writeNext(entry);
		}
		String[] blank = { "", "", "", "","","","" };
		logger.debug(roundUp(piaverage/existingIRB.size(),(24 * 60 * 60 * 1000))+"  "+roundUp(rscaverage/existingIRB.size(),(24 * 60 * 60 * 1000)));
		writer.writeNext(blank);
		
		}
		 writer.flush();
		writer.close();
	}
	
	//@Test
	public void generateClaraSubmittedStudyReport() throws NumberFormatException, IOException{
		List<Committee> committees = Lists.newArrayList();
		committees.add(Committee.BUDGET_MANAGER);
		committees.add(Committee.BUDGET_REVIEW);
		committees.add(Committee.COVERAGE_REVIEW);
		committees.add(Committee.COVERAGE_MANAGER);
		
		FileInputStream fstream;
		fstream = new FileInputStream("C:\\Data\\id.txt");
		DataInputStream in = new DataInputStream(fstream);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String strLine;
		
		while ((strLine = br.readLine()) != null) {
			Map<String, Long> results = getTimeInEachQueueByProtocolIdLatestVersion(Long.valueOf(strLine),committees);
			
			long totalTime = 0;
			for(String key:results.keySet()){
				totalTime += results.get(key);
			}
			totalTime = roundUp(totalTime,(24 * 60 * 60 * 1000));
			//logger.debug(strLine+"   "+totalTime);
			System.out.println(totalTime);
			
			List<ProtocolForm> pfs = protocolFormDao
			.listProtocolFormsByProtocolIdAndProtocolFormType(
					Long.valueOf(strLine), ProtocolFormType.NEW_SUBMISSION);
			for (ProtocolForm pf : pfs) {
				if (pf.getId() != pf.getParent().getId()) {
					continue;
				}
				
				System.out.println(getTotalTimeForPILatest(pf.getId()).get("pitotalTime"));
			}
			
		}
		
	}
	
	private Map<String, Long> getTimeInEachQueueByProtocolIdLatestVersion(long protocolId,
			List<Committee> committees) {
		List<ProtocolForm> pfs = Lists.newArrayList();
		try {
			pfs = protocolFormDao
					.listProtocolFormsByProtocolIdAndProtocolFormType(
							protocolId, ProtocolFormType.NEW_SUBMISSION);
		} catch (Exception e) {
			pfs = protocolFormDao
					.listProtocolFormsByProtocolIdAndProtocolFormType(
							protocolId,
							ProtocolFormType.HUMAN_SUBJECT_RESEARCH_DETERMINATION);
		}
		Map<String, Long> resultMap = Maps.newTreeMap();
		// long totaltimeForStudy = 0;
		// long earliestTime = 0;
		long finalActionTime = 0;
		long totalTimeForConsentLegalReview = 0;
		for (ProtocolForm pf : pfs) {
			if (pf.getId() != pf.getParent().getId()) {
				continue;
			}

			for (Committee committee : committees) {
				long totalTime = 0;
				long startTime = 0;
				long endTime = 0;

				List<ProtocolFormCommitteeStatus> pfcss = protocolFormCommitteeStatusDao
						.listAllByCommitteeAndProtocolFormId(committee,
								pf.getFormId());
				for (int i = 0; i < pfcss.size(); i++) {
					ProtocolFormCommitteeStatus pfcs = pfcss.get(i);
					List<ProtocolFormCommitteeStatusEnum> startActions = committeeactions
							.getStartCommitteeStatusMap().get(committee);
					List<ProtocolFormCommitteeStatusEnum> endActions = committeeactions
							.getEndCommitteeStatusMap().get(committee);

					if (startActions == null) {
						continue;
					}

					if (startActions.contains(pfcs
							.getProtocolFormCommitteeStatus())) {
						startTime = pfcs.getModified().getTime();
						/*
						 * if(!committee.equals(Committee.PHARMACY_REVIEW)){ if
						 * (startTime < earliestTime || earliestTime == 0) {
						 * earliestTime = startTime; } }
						 */
						// chage the start time to agenda approved
						if (committee.equals(Committee.IRB_REVIEWER)) {
							try {
								AgendaStatus agendaStatus = null;
								try{
									agendaStatus = agendaStatusDao
											.getAgendaStatusByAgendaStatusAndProtocolFormId(
													AgendaStatusEnum.AGENDA_APPROVED,
													pfcs.getProtocolFormId());
								}catch(Exception ex){
									agendaStatus = agendaStatusDao
											.getAgendaStatusByAgendaStatusAndProtocolFormIdAndAgendaItemStatus(
													AgendaStatusEnum.AGENDA_APPROVED,
													pfcs.getProtocolFormId(),AgendaItemStatus.REMOVED);

								}
								startTime = agendaStatus.getModified()
										.getTime();
							} catch (Exception e) {
								startTime = 0;
							}
						}

					} else if (startTime > 0
							&& endActions.contains(pfcs
									.getProtocolFormCommitteeStatus())) {
						endTime = pfcs.getModified().getTime();
						if (committee.equals(Committee.IRB_OFFICE)
								&& pfcs.getProtocolFormCommitteeStatus()
										.equals(ProtocolFormCommitteeStatusEnum.IRB_AGENDA_ASSIGNED)) {
							try {
								AgendaStatus agendaStatus = null;
								try{
									agendaStatus = agendaStatusDao
											.getAgendaStatusByAgendaStatusAndProtocolFormId(
													AgendaStatusEnum.AGENDA_APPROVED,
													pfcs.getProtocolFormId());
								}catch(Exception ex){
									agendaStatus = agendaStatusDao
											.getAgendaStatusByAgendaStatusAndProtocolFormIdAndAgendaItemStatus(
													AgendaStatusEnum.AGENDA_APPROVED,
													pfcs.getProtocolFormId(),AgendaItemStatus.REMOVED);

								}
								endTime = agendaStatus.getModified().getTime();
							} catch (Exception e) {
								if (i != (pfcss.size() - 1)) {
									if (pfcss
											.get(i + 1)
											.getProtocolFormCommitteeStatus()
											.equals(ProtocolFormCommitteeStatusEnum.PENDING_IRB_REVIEW_RE_ASSIGNMENT)) {
										endTime = 0;
										continue;
									}
								}
								endTime = new Date().getTime();
							}
						}
						if (committee.equals(Committee.COVERAGE_REVIEW)
								&& pfcs.getProtocolFormCommitteeStatus()
										.equals(ProtocolFormCommitteeStatusEnum.PENDING_CONSENT_LEGAL_REVIEW)) {

							long startTimeForConsentLegal = pfcs.getModified()
									.getTime();
							long endTimeForConsentLegal = new Date().getTime();
							if (i < pfcss.size() - 1) {
								endTimeForConsentLegal = pfcss.get(i + 1)
										.getModified().getTime();
							}

							totalTimeForConsentLegalReview += endTimeForConsentLegal
									- startTimeForConsentLegal;

						}

						if (!committee.equals(Committee.PHARMACY_REVIEW)) {
							if (endTime > finalActionTime) {
								finalActionTime = endTime;
							}
						}
					}

					if (startTime > 0 && endTime == 0
							&& i == (pfcss.size() - 1)) {
						endTime = new Date().getTime();

						finalActionTime = endTime;

					}

					if (startTime > 0 && endTime > 0) {
						totalTime += endTime - startTime;
						if (endTime - startTime < 0) {
							logger.debug(pf.getFormId() + "#######"
									+ (endTime - startTime));
						}
						startTime = 0;
						endTime = 0;

					}
				}
				if (totalTime > 0) {
					//totalTime = 1 + totalTime / (24 * 60 * 60 * 1000);
					resultMap.put(committee.getDescription(), totalTime);
				}
			}
		}
		if (totalTimeForConsentLegalReview > 0) {
			totalTimeForConsentLegalReview = 1 + totalTimeForConsentLegalReview
					/ (24 * 60 * 60 * 1000);
			resultMap.put(Committee.PROTOCOL_LEGAL_REVIEW.getDescription(),
					totalTimeForConsentLegalReview );
		}

		/*
		 * totaltimeForStudy = 1 + (finalActionTime - earliestTime) / (24 * 60 *
		 * 60 * 1000);
		 */
		//resultMap.put("finaActionTime", finalActionTime + "");
		return resultMap;
	}
	
	private Map<String, Long> getTotalTimeForPILatest(long protocolFormId) {
		List<ProtocolFormStatusEnum> startActions = committeeactions
				.getPiStartActions();
		Map<String, Long> results = Maps.newHashMap();
		long creationTime = 0;
		long totalTime = 0;
		long startTime = 0;
		long endTime = 0;
		List<ProtocolFormStatus> pfss = protocolFormStatusDao
				.getAllProtocolFormStatusByParentFormId(protocolFormId);

		long finalTime = pfss.get(pfss.size() - 1).getModified().getTime();
		// because the query order by modified desc, we use i-- for for loop
		for (int i = 0; i < pfss.size(); i++) {
			ProtocolFormStatus pfs = pfss.get(i);
			if (startActions.contains(pfs.getProtocolFormStatus())
					&& startTime == 0) {
				startTime = pfs.getModified().getTime();
				if (pfs.getProtocolFormStatus().equals(
						ProtocolFormStatusEnum.DRAFT)) {
					creationTime = startTime;
				}
			} else if (startActions.contains(pfs.getProtocolFormStatus())
					&& startTime > 0 && endTime == 0) {
				endTime = pfs.getModified().getTime();
				i--;
			} else if (startTime > 0 && endTime == 0) {
				endTime = pfs.getModified().getTime();
			}

			if (startTime > 0 && endTime == 0 && i == pfss.size() - 1) {
				endTime = new Date().getTime();
				finalTime = endTime;
			}

			if (startTime > 0 && endTime > 0) {

				totalTime += endTime - startTime;
				startTime = 0;
				endTime = 0;
				if (totalTime < 0) {
					logger.debug(protocolFormId + "#######" + totalTime);
				}
			}

		}

		totalTime = 1 + totalTime / (24 * 60 * 60 * 1000);
		results.put("pitotalTime", totalTime);
		results.put("startTime", creationTime);
		results.put("finalTime", finalTime);
		return results;
	}
	
	
	//@Test
	public void claraData() throws IOException, ParserConfigurationException{
		String queryStr = "select id from protocol as p where p.retired = 0 and p.id in (select distinct protocol_id from protocol_form where id in (select distinct protocol_form_id from protocol_form_xml_data where retired = 0 and protocol_form_xml_data_type = 'budget')) and p.meta_data_xml.exist('/protocol/extra/prmc-related-or-not/text()[fn:contains(fn:upper-case(.),\"N\")]')=1 and p.id >200000";
		Query query = em.createNativeQuery(queryStr);
		List<BigInteger> pids = query.getResultList();
		
		List<Committee> committees = Lists.newArrayList();
		committees.add(Committee.BUDGET_MANAGER);
		committees.add(Committee.BUDGET_REVIEW);
		committees.add(Committee.COVERAGE_REVIEW);
		committees.add(Committee.COVERAGE_MANAGER);
		
		List<ProtocolFormStatusEnum> pfsDrafts = Lists.newArrayList();
		pfsDrafts.add(ProtocolFormStatusEnum.DRAFT);
		pfsDrafts.add(ProtocolFormStatusEnum.PENDING_PI_ENDORSEMENT);
		pfsDrafts.add(ProtocolFormStatusEnum.PENDING_PL_ENDORSEMENT);
		pfsDrafts.add(ProtocolFormStatusEnum.PENDING_TP_ENDORSEMENT);
		pfsDrafts.add(ProtocolFormStatusEnum.CANCELLED);
		
		List<ProtocolFormStatusEnum> approvedStatus = Lists.newArrayList();
		approvedStatus.add(ProtocolFormStatusEnum.EXPEDITED_APPROVED);
		approvedStatus.add(ProtocolFormStatusEnum.EXEMPT_APPROVED);
		approvedStatus.add(ProtocolFormStatusEnum.IRB_APPROVED);
		
		CSVWriter writer = new CSVWriter(new FileWriter("C:\\Data\\Clara Submitted Studies With Time Range with creation"+".csv"));
	    String[] title = { "IRB", "PI", "Title", "Submitted Date","IRB Approved","Approved Date","Study Creation to Study Submission(Days)", "Time With PI (Days)","Time With RSC (days)" };
	    writer.writeNext(title);
		
		for(BigInteger pidBig :pids){
			long pid = pidBig.longValue();
			String[] entry = new String[title.length];
			Protocol p = protocolDao.findById(pid);
			entry[0] = pid+"";
			String xml = p.getMetaDataXml();
			XmlHandler xmlHandler =  XmlHandlerFactory.newXmlHandler();
			entry[1] = xmlHandler.getSingleStringValueByXPath(xml, "/protocol/staffs/staff/user[roles/role/text()=\"Principal Investigator\"]/lastname/text()")+","+
					xmlHandler.getSingleStringValueByXPath(xml, "/protocol/staffs/staff/user[roles/role/text()=\"Principal Investigator\"]/firstname/text()");
			entry[2] = xmlHandler.getSingleStringValueByXPath(xml, "/protocol/title/text()");
			
			//logger.debug(pid+"");
			List<ProtocolForm> prfs =  protocolFormDao.listProtocolFormsByProtocolIdAndProtocolFormType(pid, ProtocolFormType.NEW_SUBMISSION);
			ProtocolForm pf = prfs.get(0);
			List<ProtocolFormStatus> pfss = protocolFormStatusDao.getAllProtocolFormStatusByParentFormId(pf.getParentFormId());
			
			
			//get submitted time
			Date creatededDate = pfss.get(0).getModified();
			Date submittedDate = new Date();
			for(ProtocolFormStatus pfs:pfss){
				if(!pfsDrafts.contains(pfs.getProtocolFormStatus())){
					submittedDate = pfs.getModified();
					entry[3] = DateFormat.getInstance().format(submittedDate);
					break;
				}
			}
			
			//check if approved
			entry[4] = "No";
			Date approvedDate = new Date();
			for(ProtocolFormStatus pfs:pfss){
				if(approvedStatus.contains(pfs.getProtocolFormStatus())){
					approvedDate = pfs.getModified();
					entry[5] = DateFormat.getInstance().format(approvedDate);
					entry[4] = "Yes";
					break;
				}
			}
			
			Map<String, Long> results = getTimeInEachQueueByProtocolIdLatestVersion(pid,committees);
			
			long totalTime = 0;
			for(String key:results.keySet()){
				totalTime += results.get(key);
			}
			totalTime = roundUp(totalTime,(24 * 60 * 60 * 1000));
			entry[8] = totalTime+"";

			//we need creation time
			long creationToSub = roundUp(submittedDate.getTime()-creatededDate.getTime(),(24 * 60 * 60 * 1000));
			entry[6] = creationToSub+"";
			entry[7] =(getTotalTimeForPILatest(pf.getId()).get("pitotalTime"))+"";
			
			
			
			if(entry[3]!=null){
				writer.writeNext(entry);
			}
			
		}
		writer.flush();
		writer.close();
	
	}
	
	//@Test
	public void countStudiesByDepartment(){
		List<Department> depts = departmentDao.findDeptsByCollegeId(5);
		List<College> colleges = collegeDao.findAll();
		Map<String,String> collegeResults = Maps.newHashMap();
		Map<String,String> deptResults = Maps.newHashMap();
		for(Department dept: depts){
			String queryStr = "select count(id) from protocol where retired = 0 and id not in (select distinct protocol_id from protocol_form where retired =0 and protocol_form_type = 'HUMAN_SUBJECT_RESEARCH_DETERMINATION') and id in (select distinct protocol_id from protocol_status where retired = 0 and protocol_status ='open' and id in (select max(id) from protocol_status where retired = 0 group by protocol_id)) and id  not in (select protocol_id from test_studies) and meta_data_xml.exist('/protocol/extra/prmc-related-or-not/text()[fn:contains(fn:upper-case(.),\"Y\")]')=0 and meta_data_xml.exist('/protocol/responsible-department[@collegeid = \"5\" and @deptid = \""+dept.getId()+"\"]')=1";

			
			logger.debug(queryStr);
			Query query = em.createNativeQuery(queryStr);
			deptResults.put(dept.getName(), ""+query.getSingleResult());
		}
		
		/*for(College college: colleges){
			String queryStr = "select count(id) from protocol where retired = 0 and id not in (select distinct protocol_id from protocol_form where retired =0 and protocol_form_type = 'HUMAN_SUBJECT_RESEARCH_DETERMINATION') and id in (select distinct protocol_id from protocol_status where retired = 0 and protocol_status ='open' and id in (select max(id) from protocol_status where retired = 0 group by protocol_id)) and id  not in (select protocol_id from test_studies) and meta_data_xml.exist('/protocol/extra/prmc-related-or-not/text()[fn:contains(fn:upper-case(.),\"Y\")]')=0 and meta_data_xml.exist('/protocol/responsible-department[@collegeid = \""+college.getId()+"\"]')=1";
			logger.debug(queryStr);
			Query query = em.createNativeQuery(queryStr);
			collegeResults.put(college.getName(), ""+query.getSingleResult());
		}*/
			
		for(String key : deptResults.keySet()){
			System.out.println(key);
		}
		
		for(String key : deptResults.keySet()){
			System.out.println(deptResults.get(key));
		}
		
	}
	
	//@Test
	public void addExpiredDatePathToProtocolMetaData() throws ParserConfigurationException, XPathExpressionException, SAXException, IOException{
		List<Protocol> protocols = protocolDao.listProtocolswithLatestStatus(ProtocolStatusEnum.EXPIRED);
		XmlHandler xmlHandler = XmlHandlerFactory.newXmlHandler();
		
		for(Protocol protocol : protocols){
			logger.debug(protocol.getId()+"");
			ProtocolStatus ps = protocolStatusDao.findProtocolStatusByProtocolId(protocol.getId());
			
			String updatedXml = xmlHandler.replaceOrAddNodeValueByPath("/protocol/original-study/expired-date", protocol.getMetaDataXml(),DateFormatUtil.formateDateToMDY(ps.getModified()));
			protocol.setMetaDataXml(updatedXml);
			protocolDao.saveOrUpdate(protocol);
		}
		
		
	}
	
	//@Test
	public void listIndividualsonActiveStudy() throws ParserConfigurationException{
		Map<String, Set<String>> result = Maps.newHashMap();
		Set<String> userId = Sets.newHashSet();
		//get active studies
		String queryStr = "SELECT distinct id FROM protocol WHERE retired = 0 AND id IN (SELECT protocol_id FROM protocol_status WHERE retired = 0 AND protocol_status = 'OPEN' and id in (SELECT MAX(id) FROM protocol_status where retired =0 group by protocol_id )) AND id  not in (select protocol_id from test_studies)";
		
		logger.debug(queryStr);
		XmlHandler xmlHandler = XmlHandlerFactory.newXmlHandler();
		Query query = em.createNativeQuery(queryStr);
		List<BigInteger> pIdBigs  = query.getResultList();
		
		for(BigInteger pIdBig :pIdBigs){
			long pId = pIdBig.longValue();
			Protocol p = protocolDao.findById(pId);
			String xml = p.getMetaDataXml();
			
			List<Element> users = xmlHandler.listElementsByXPath(xml, "/protocol/staffs/staff/user");
			for(Element user:users){
				String uid = user.getAttribute("id");
				List<Element> roles = xmlHandler.listElementsByXPath(xml, "/protocol/staffs/staff/user[@id="+uid+"]/roles/role");
				
				boolean checkResponsible = true;
				for(Element role:roles){
					if(role.getTextContent().equals("Principal Investigator")){
						if(result.get("Principal Investigator")==null){
							result.put("Principal Investigator",new HashSet<String>());
							
						}
						result.get("Principal Investigator").add(uid);
						//user.getElementsByTagName("firstname").item(0).getTextContent()+user.getElementsByTagName("lastname").item(0).getTextContent()
						
						checkResponsible = false;
						break;
					}else if(role.getTextContent().equals("Study Coordinator")){
						if(result.get("Study Coordinator")==null){
							result.put("Study Coordinator",new HashSet<String>());
						}

						result.get("Study Coordinator").add(uid);
						checkResponsible = false;
						break;
					}else if(role.getTextContent().equals("Treating Physician")){
						if(result.get("Treating Physician")==null){
							result.put("Treating Physician",new HashSet<String>());
						}

						result.get("Treating Physician").add(uid);
						checkResponsible = false;
						break;
					}
				}
				
				if(checkResponsible){
					List<Element> responsibilities = xmlHandler.listElementsByXPath(xml, "/protocol/staffs/staff/user[@id="+uid+"]/reponsibilities/responsibility");
					
					for(Element responsibility:responsibilities){
						if(responsibility.getTextContent().equals("Managing CLARA submission")){
							if(result.get(roles.get(0).getTextContent())==null){
								result.put(roles.get(0).getTextContent(),new HashSet<String>());
							}
								result.get(roles.get(0).getTextContent()).add(uid);
							
							
							break;
						}
					}
				}
			}
			
		}
		
		for(String key :  result.keySet()){
			logger.debug(key+"   "+result.get(key).size());
		}

	}
	
	//@Test
	public void fximissingSubPro() {
		try{
		XmlHandler xmlhandler = XmlHandlerFactory.newXmlHandler();
		Set<Long> results = Sets.newHashSet();
		List<ProtocolFormXmlData> pfxds = protocolFormXmlDataDao.listProtocolformXmlDatasByType(ProtocolFormXmlDataType.BUDGET);
		for(ProtocolFormXmlData pfxd:pfxds){
			try{
				String  xml = pfxd.getXmlData();
				List<Element> subPros = xmlHandler.listElementsByXPath(xml, "//subprocedures/procedure");
				
				for(Element subPro:subPros){
					String id = subPro.getAttribute("id");
					List<Element> vts =  xmlHandler.listElementsByXPath(xml, "//visits/visit/visitprocedures/vp[@pid=\""+id+"\"]");
					
					if(vts.size()==0){
						logger.debug(pfxd.getId()+"  @@@@@@@@@ "+pfxd.getProtocolForm().getProtocol().getId()+"          "+id);
						//results.add(pfxd.getId());
					}
				}
				
			}catch(Exception e){
				
			}
		}
		for(long data:results){
			System.out.println(data);
		}
		}catch(Exception e){
			
		}
		
		
		
	}
	
	//@Test
	public void gateKeepertoIRBReport() throws ParserConfigurationException{
		
		
		//List<ProtocolFormCommitteeStatus> approvedByGate = protocolFormCommitteeStatusDao.listByCommitteeAndStatus(Committee.GATEKEEPER, ProtocolFormCommitteeStatusEnum.APPROVED);
		String queryStr = "select distinct parent_id from protocol_form where protocol_form_type ='NEW_SUBMISSION' and retired = 0 and id in(select protocol_form_id from protocol_form_committee_status where retired = 0 and committee = 'GATEKEEPER' and protocol_form_committee_status = 'APPROVED'  and DATEDIFF(DAY,modified,'2013-09-20')<0)";		
		
		XmlHandler xmlHandler = XmlHandlerFactory.newXmlHandler();
		Query query = em.createNativeQuery(queryStr);
		List<BigInteger> pfParentIds  = query.getResultList();
		List<Long> toIRB = Lists.newArrayList();
		List<Long> toOther = Lists.newArrayList();
		List<Long> toNothing = Lists.newArrayList();
		
		for(BigInteger pfIdBig:pfParentIds){
			long pfid = pfIdBig.longValue();
			List<ProtocolFormCommitteeStatus> pfcss = protocolFormCommitteeStatusDao.listAllByProtocolFormId(pfid);
			
			if(pfcss.get(pfcss.size()-1).getCommittee().equals(Committee.GATEKEEPER)&&pfcss.get(pfcss.size()-1).getProtocolFormCommitteeStatus().equals(ProtocolFormCommitteeStatusEnum.APPROVED)){
				toNothing.add(pfcss.get(pfcss.size()-1).getProtocolForm().getProtocol().getId());
				continue;
			}
			
			for(int i = 0;i<pfcss.size()-1;i++){
				ProtocolFormCommitteeStatus pfcs = pfcss.get(i);
				logger.debug(pfcs.getCommitteeDescription());
				if(pfcs.getCommittee().equals(Committee.GATEKEEPER)&&pfcs.getProtocolFormCommitteeStatus().equals(ProtocolFormCommitteeStatusEnum.APPROVED)){
					Committee committeeAfterApproved = pfcss.get(i+1).getCommittee();
					if(committeeAfterApproved.getDescription().contains("IRB")){
						toIRB.add(pfcss.get(i+1).getProtocolForm().getProtocol().getId());
					}else{
						//may have multiple committee happened at the same time
						if(i+2<pfcss.size()){
							ProtocolFormCommitteeStatus pfcsj1 = pfcss.get(i+1);
							for(int j = i+2;j<pfcss.size()-1;j++){
								ProtocolFormCommitteeStatus pfcsj2 = pfcss.get(j);
								if(pfcsj1.getModified().getTime()-pfcsj2.getModified().getTime()==0){
									if(pfcsj2.getCommittee().getDescription().contains("IRB")){
										toIRB.add(pfcsj2.getProtocolForm().getProtocol().getId());
										break;
									}
								}else if(pfcsj1.getModified().getTime()-pfcsj2.getModified().getTime()<0){
									toOther.add(pfcss.get(i+1).getProtocolForm().getProtocol().getId());
									logger.debug(pfcsj2.getCommitteeDescription()+"**"+pfcss.get(i+1).getProtocolForm().getProtocol().getId());
									break;
								}
								
								
							}
						}else{
							toOther.add(pfcss.get(i+1).getProtocolForm().getProtocol().getId());
							logger.debug("***"+pfcss.get(i+1).getProtocolForm().getProtocol().getId());
						}
						
						
						
					}
					break;
					
				}
			}
			
		}
		
		logger.debug(toIRB.size()+"");
		logger.debug(toOther.size()+"");
		logger.debug(toNothing.size()+"");
	}
	
	//@Test
	public void getProtocolListByCollege() throws ParserConfigurationException, IOException{
		List<College> cs = collegeDao.findAll();
		CSVWriter writer = new CSVWriter(new FileWriter(
				"C:\\Data\\Protocols By College.csv"));
		String[] title = { "IRB", "College"};
		writer.writeNext(title);
		for(College c :cs){
			String queryStr = "select id from protocol where retired = 0 and id in (select id from protocol where retired =0 and meta_data_xml.exist('/protocol/responsible-department["
					+ "@collegeid = \"" + c.getId() + "\"" + "]') = 1) and id in (select distinct protocol_id from protocol_status where id in (select min(id) from protocol_status where retired =0 and protocol_status not in ( 'CANCELLED','DRAFT','PENDING_PL_ENDORSEMENT','PENDING_PI_ENDORSEMENT','PENDING_TP_ENDORSEMENT') group by protocol_id))";
		
			XmlHandler xmlHandler = XmlHandlerFactory.newXmlHandler();
			Query query = em.createNativeQuery(queryStr);
			List<BigInteger> pids  = query.getResultList();
			
			for(BigInteger pid:pids){
				String[] entry = {pid+"",c.getName()};
				writer.writeNext(entry);
			}
			
			String[] empty = {" "," "};
			writer.writeNext(empty);
		}
		
		writer.flush();
		writer.close();
	}
	
	private final static String[] mappings = new String[] { "cptCode", "cost" };
	
	@Test
	public void findHospitalChargeDifference() throws IOException{
		Map<String,BigDecimal> newChargeMap = Maps.newHashMap();
		CSVWriter writer = new CSVWriter(new FileWriter(
				"C:\\Data\\hospital Charger Difference.csv"));
		try {
			CSVReader csvReader = new CSVReader(new FileReader(
					"C:\\Data\\new hospital cost.csv"));

			int numberOfColumns = mappings.length;
			csvReader.readNext(); // skip header
			String[] nextLine;
			Map<String,String> importedCptMap = Maps.newHashMap();

			while ((nextLine = csvReader.readNext()) != null) {
				HospitalChargeUpdate hospitalChargeUpdate = new HospitalChargeUpdate();
				Map<String, Object> properties;
					
					if(importedCptMap.containsKey(nextLine[0].trim())&&!importedCptMap.get(nextLine[0].trim()).isEmpty()){
						continue;
					}
					for (int i = 0; i < numberOfColumns; i++) {
						if (i == 1) {
							String cost = nextLine[i].trim();
							importedCptMap.put(nextLine[0].trim(), cost);
							if (cost.isEmpty()) {
								cost = "0";
							}
							Number number = NumberFormat.getInstance().parse(
									cost);
							Float price = (float) (number.floatValue() * 1.05);
							BigDecimal bdPrice = new BigDecimal((double) price);
							bdPrice = bdPrice.setScale(2, 4);
							newChargeMap.put(nextLine[0], bdPrice);

						} else {
							newChargeMap.put(nextLine[0], BigDecimal.ZERO);
						}
					}
			}
			csvReader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		List<HospitalChargeProcedure> hsps = hospitalChargeProcedureDao.findAll();
		System.out.print("************");
		logger.debug(hsps.size()+"  "+newChargeMap.size());
		/*for(HospitalChargeProcedure hsp :hsps){
			if(newChargeMap.containsKey(hsp.getCptCode())){
				if(newChargeMap.get(hsp.getCptCode()).subtract(hsp.getCost()).intValue()!=0){
					String[] entry = {hsp.getCptCode(),newChargeMap.get(hsp.getCptCode())+"",hsp.getCost()+"",newChargeMap.get(hsp.getCptCode()).subtract(hsp.getCost())+""};
					writer.writeNext(entry);
				}
			}else{
				System.out.print(hsp.getCptCode()+"\n");
			}
		}*/
		
		for(String key :newChargeMap.keySet()){
			try{
				if(hospitalChargeProcedureDao.findByCptCodeOnly(key) == null){
					String[] entry ={key};
					writer.writeNext(entry);
				}
			}catch(Exception e){
				String[] entry ={key};
				writer.writeNext(entry);
			}
		}
		
		writer.flush();
		writer.close();
		
	}
	
	private long roundUp(long a,long b) {
		return(((double)a/(double)b)>(a/b)?a/b+1:a/b);
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

	public XmlProcessor getXmlProcessor() {
		return xmlProcessor;
	}

	@Autowired(required = true)
	public void setXmlProcessor(XmlProcessor xmlProcessor) {
		this.xmlProcessor = xmlProcessor;
	}

	public XmlHandler getXmlHandler() {
		return xmlHandler;
	}

	@Autowired(required = true)
	public void setXmlHandler(XmlHandler xmlHandler) {
		this.xmlHandler = xmlHandler;
	}

	public ProtocolDao getProtocolDao() {
		return protocolDao;
	}

	@Autowired(required = true)
	public void setProtocolDao(ProtocolDao protocolDao) {
		this.protocolDao = protocolDao;
	}

	public ProtocolFormStatusDao getProtocolFormStatusDao() {
		return protocolFormStatusDao;
	}

	@Autowired(required = true)
	public void setProtocolFormStatusDao(
			ProtocolFormStatusDao protocolFormStatusDao) {
		this.protocolFormStatusDao = protocolFormStatusDao;
	}

	public EntityManager getEm() {
		return em;
	}

	@PersistenceContext(unitName = "defaultPersistenceUnit")
	public void setEm(EntityManager em) {
		this.em = em;
	}

	public ProtocolFormCommitteeStatusDao getProtocolFormCommitteeStatusDao() {
		return protocolFormCommitteeStatusDao;
	}

	@Autowired(required = true)
	public void setProtocolFormCommitteeStatusDao(
			ProtocolFormCommitteeStatusDao protocolFormCommitteeStatusDao) {
		this.protocolFormCommitteeStatusDao = protocolFormCommitteeStatusDao;
	}

	public ContractFormDao getContractFormDao() {
		return contractFormDao;
	}

	@Autowired(required = true)
	public void setContractFormDao(ContractFormDao contractFormDao) {
		this.contractFormDao = contractFormDao;
	}

	public ContractFormCommitteeStatusDao getContractFormCommitteeStatusDao() {
		return contractFormCommitteeStatusDao;
	}

	@Autowired(required = true)
	public void setContractFormCommitteeStatusDao(
			ContractFormCommitteeStatusDao contractFormCommitteeStatusDao) {
		this.contractFormCommitteeStatusDao = contractFormCommitteeStatusDao;
	}

	public AgendaDao getAgendaDao() {
		return agendaDao;
	}

	@Autowired(required = true)
	public void setAgendaDao(AgendaDao agendaDao) {
		this.agendaDao = agendaDao;
	}

	public AgendaStatusDao getAgendaStatusDao() {
		return agendaStatusDao;
	}

	@Autowired(required = true)
	public void setAgendaStatusDao(AgendaStatusDao agendaStatusDao) {
		this.agendaStatusDao = agendaStatusDao;
	}

	public DepartmentDao getDepartmentDao() {
		return departmentDao;
	}

	@Autowired(required = true)
	public void setDepartmentDao(DepartmentDao departmentDao) {
		this.departmentDao = departmentDao;
	}

	public ProtocolStatusDao getProtocolStatusDao() {
		return protocolStatusDao;
	}

	@Autowired(required = true)
	public void setProtocolStatusDao(ProtocolStatusDao protocolStatusDao) {
		this.protocolStatusDao = protocolStatusDao;
	}

	public CollegeDao getCollegeDao() {
		return collegeDao;
	}

	@Autowired(required = true)
	public void setCollegeDao(CollegeDao collegeDao) {
		this.collegeDao = collegeDao;
	}

	public AuditDao getAuditDao() {
		return auditDao;
	}

	@Autowired(required = true)
	public void setAuditDao(AuditDao auditDao) {
		this.auditDao = auditDao;
	}

	public HospitalChargeProcedureDao getHospitalChargeProcedureDao() {
		return hospitalChargeProcedureDao;
	}

	@Autowired(required = true)
	public void setHospitalChargeProcedureDao(HospitalChargeProcedureDao hospitalChargeProcedureDao) {
		this.hospitalChargeProcedureDao = hospitalChargeProcedureDao;
	}

}
