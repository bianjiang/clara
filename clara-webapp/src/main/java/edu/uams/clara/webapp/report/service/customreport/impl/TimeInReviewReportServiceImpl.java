package edu.uams.clara.webapp.report.service.customreport.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import edu.emory.mathcs.backport.java.util.Collections;
import edu.uams.clara.core.util.xml.XmlHandler;
import edu.uams.clara.core.util.xml.XmlHandlerFactory;
import edu.uams.clara.webapp.common.domain.usercontext.enums.Committee;
import edu.uams.clara.webapp.common.util.DateFormatUtil;
import edu.uams.clara.webapp.protocol.dao.ProtocolDao;
import edu.uams.clara.webapp.protocol.dao.businesslogicobject.AgendaStatusDao;
import edu.uams.clara.webapp.protocol.dao.businesslogicobject.ProtocolFormCommitteeStatusDao;
import edu.uams.clara.webapp.protocol.dao.businesslogicobject.ProtocolFormStatusDao;
import edu.uams.clara.webapp.protocol.dao.irb.AgendaItemDao;
import edu.uams.clara.webapp.protocol.dao.protocolform.ProtocolFormDao;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.AgendaStatus;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.ProtocolFormCommitteeStatus;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.ProtocolFormStatus;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.enums.AgendaStatusEnum;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.enums.ProtocolFormCommitteeStatusEnum;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.enums.ProtocolFormStatusEnum;
import edu.uams.clara.webapp.protocol.domain.irb.AgendaItem.AgendaItemStatus;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolForm;
import edu.uams.clara.webapp.protocol.domain.protocolform.enums.ProtocolFormType;
import edu.uams.clara.webapp.report.dao.ReportFieldDao;
import edu.uams.clara.webapp.report.domain.CommitteeActions;
import edu.uams.clara.webapp.report.domain.ReportCriteria;
import edu.uams.clara.webapp.report.domain.ReportField;
import edu.uams.clara.webapp.report.domain.ReportFieldTemplate;
import edu.uams.clara.webapp.report.domain.ReportResultFieldTemplate;
import edu.uams.clara.webapp.report.domain.ReportTemplate;
import edu.uams.clara.webapp.report.service.customreport.CustomReportService;

public class TimeInReviewReportServiceImpl extends CustomReportService {
	private final static Logger logger = LoggerFactory
			.getLogger(TimeInReviewReportServiceImpl.class);

	private ProtocolDao protocolDao;
	private ProtocolFormDao protocolFormDao;
	private ProtocolFormStatusDao protocolFormStatusDao;
	private ProtocolFormCommitteeStatusDao protocolFormCommitteeStatusDao;
	private ReportFieldDao reportFieldDao;
	private AgendaStatusDao agendaStatusDao;
	private AgendaItemDao agendaItemDao;
	private CommitteeActions committeeactions = new CommitteeActions();

	private List<String[]> getDetailTimeInPIByProtocolFormId(long protocolFormId) {
		List<ProtocolFormStatusEnum> startActions = committeeactions
				.getPiStartActions();

		List<String[]> results = Lists.newArrayList();
		List<String> mapKeys = Lists.newArrayList();
		List<ProtocolFormStatus> detailStartActions = Lists.newArrayList();
		Map<ProtocolFormStatus, ProtocolFormStatus> detailActions = new HashMap<ProtocolFormStatus, ProtocolFormStatus>();
		Map<String, String[]> preOrderresultMap = Maps.newHashMap();
		List<ProtocolFormStatus> pfss = protocolFormStatusDao
				.getAllProtocolFormStatusByParentFormId(protocolFormId);

		ProtocolFormStatus tempStartPfs = null;
		long startTime = 0;
		long endTime = 0;
		for (int i = 0; i < pfss.size(); i++) {

			ProtocolFormStatus pfs = pfss.get(i);

			if (startActions.contains(pfs.getProtocolFormStatus())
					&& startTime == 0) {
				startTime = pfs.getModified().getTime();
				tempStartPfs = pfs;
			} else if (startActions.contains(pfs.getProtocolFormStatus())
					&& startTime > 0 && endTime == 0) {
				endTime = pfs.getModified().getTime();
				detailStartActions.add(tempStartPfs);
				mapKeys.add("" + tempStartPfs.getModified().getTime()
						+ tempStartPfs.getProtocolFormStatus() + "PI");
				detailActions.put(tempStartPfs, pfs);
				i--;
			} else if (startTime > 0 && endTime == 0) {
				endTime = pfs.getModified().getTime();
				detailStartActions.add(tempStartPfs);
				mapKeys.add("" + tempStartPfs.getModified().getTime()
						+ tempStartPfs.getProtocolFormStatus() + "PI");
				detailActions.put(tempStartPfs, pfs);
			}

			if (startTime > 0 && endTime == 0 && i == pfss.size() - 1) {
				endTime = new Date().getTime();
				detailStartActions.add(tempStartPfs);
				mapKeys.add("" + tempStartPfs.getModified().getTime()
						+ tempStartPfs.getProtocolFormStatus() + "PI");
				detailActions.put(tempStartPfs, null);
			}

			if (startTime > 0 && endTime > 0) {
				startTime = 0;
				endTime = 0;
			}

		}

		for (ProtocolFormStatus start : detailStartActions) {
			String[] resultsArray = new String[7];
			long timespan = 0;
			resultsArray[0] = "PI";
			resultsArray[1] = start.getProtocolFormStatus().getDescription();
			resultsArray[2] = DateFormatUtil.formateDate(start.getModified());
			if (detailActions.get(start) != null) {
				resultsArray[3] = detailActions.get(start)
						.getProtocolFormStatus().getDescription();
				resultsArray[4] = DateFormatUtil.formateDate(detailActions.get(
						start).getModified());
				timespan = 1
						+ (detailActions.get(start).getModified().getTime() - start
								.getModified().getTime())
						/ (24 * 60 * 60 * 1000);
			} else {
				timespan = 1
						+ (new Date().getTime() - start.getModified().getTime())
						/ (24 * 60 * 60 * 1000);
				resultsArray[3] = "";

				resultsArray[4] = "";
			}
			resultsArray[5] = timespan > 1 ? (timespan + " Days")
					: (timespan + " Day");
			resultsArray[6] = "" + start.getModified().getTime()
					+ start.getProtocolFormStatus() + "PI";
			preOrderresultMap.put(
					"" + start.getModified().getTime()
							+ start.getProtocolFormStatus() + "PI",
					resultsArray);
		}
		// orderResult
		Collections.sort(mapKeys);

		for (String startTimeStr : mapKeys) {
			results.add(preOrderresultMap.get(startTimeStr));

		}

		return results;

	}

	private Map<String, Long> getTotalTimeForPI(long protocolFormId) {
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

	private Map<String, String> getTimeInEachQueueByProtocolId(long protocolId,
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
		Map<String, String> resultMap = Maps.newTreeMap();
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
								if(agendaItemDao.listbyProtocolFormId(pfcs.getProtocolFormId()).size()>1){
									agendaStatus = agendaStatusDao
											.getAgendaStatusByAgendaStatusAndProtocolFormIdAndAgendaItemStatus(
													AgendaStatusEnum.AGENDA_APPROVED,
													pfcs.getProtocolFormId(),AgendaItemStatus.REMOVED);
								}else{
									agendaStatus = agendaStatusDao
											.getAgendaStatusByAgendaStatusAndProtocolFormId(
													AgendaStatusEnum.AGENDA_APPROVED,
													pfcs.getProtocolFormId());
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
								if(agendaItemDao.listbyProtocolFormId(pfcs.getProtocolFormId()).size()>1){
									agendaStatus = agendaStatusDao
											.getAgendaStatusByAgendaStatusAndProtocolFormIdAndAgendaItemStatus(
													AgendaStatusEnum.AGENDA_APPROVED,
													pfcs.getProtocolFormId(),AgendaItemStatus.REMOVED);
								}else{
									agendaStatus = agendaStatusDao
											.getAgendaStatusByAgendaStatusAndProtocolFormId(
													AgendaStatusEnum.AGENDA_APPROVED,
													pfcs.getProtocolFormId());
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
					totalTime = 1 + totalTime / (24 * 60 * 60 * 1000);
					resultMap.put(committee.getDescription(), totalTime + "");
				}
			}
		}
		if (totalTimeForConsentLegalReview > 0) {
			totalTimeForConsentLegalReview = 1 + totalTimeForConsentLegalReview
					/ (24 * 60 * 60 * 1000);
			resultMap.put(Committee.PROTOCOL_LEGAL_REVIEW.getDescription(),
					totalTimeForConsentLegalReview + "");
		}

		/*
		 * totaltimeForStudy = 1 + (finalActionTime - earliestTime) / (24 * 60 *
		 * 60 * 1000);
		 */
		resultMap.put("finaActionTime", finalActionTime + "");
		return resultMap;
	}

	private List<String[]> getDetailTimeInEachQueueByProtocoFormlId(
			long protocolFormId, List<Committee> committees) {
		Map<ProtocolFormCommitteeStatus, ProtocolFormCommitteeStatus> detailActions = new HashMap<ProtocolFormCommitteeStatus, ProtocolFormCommitteeStatus>();
		Map<ProtocolFormCommitteeStatus, Long> detailActionEndTime = new HashMap<ProtocolFormCommitteeStatus, Long>();
		Map<ProtocolFormCommitteeStatus, String> detailActionStringEndTime = new HashMap<ProtocolFormCommitteeStatus, String>();
		List<ProtocolFormCommitteeStatus> detailStartActions = Lists
				.newArrayList();

		List<String[]> results = getDetailTimeInPIByProtocolFormId(protocolFormId);
		Map<String, String[]> preOrderresultMap = Maps.newHashMap();
		List<String> mapKeys = Lists.newArrayList();
		// Get PI time info
		for (String[] key : results) {
			mapKeys.add(key[6]);
			preOrderresultMap.put(key[6], key);
		}
		results.clear();

		ProtocolForm pf = protocolFormDao.findById(protocolFormId);
		for (Committee committee : committees) {
			long startTime = 0;
			long endTime = 0;
			String endTimeStr = "";

			List<ProtocolFormCommitteeStatus> pfcss = protocolFormCommitteeStatusDao
					.listAllByCommitteeAndProtocolFormId(committee,
							pf.getFormId());
			ProtocolFormCommitteeStatus tempStartPfcs = null;
			for (int i = 0; i < pfcss.size(); i++) {
				ProtocolFormCommitteeStatus pfcs = pfcss.get(i);
				List<ProtocolFormCommitteeStatusEnum> startActions = committeeactions
						.getStartCommitteeStatusMap().get(committee);
				List<ProtocolFormCommitteeStatusEnum> endActions = committeeactions
						.getEndCommitteeStatusMap().get(committee);

				if (startActions == null) {
					continue;
				}
				if (startActions
						.contains(pfcs.getProtocolFormCommitteeStatus())) {
					startTime = pfcs.getModified().getTime();
					// chage the start time to agenda approved
					if (committee.equals(Committee.IRB_REVIEWER)) {
						try {
							AgendaStatus agendaStatus = null;
							if(agendaItemDao.listbyProtocolFormId(pfcs.getProtocolFormId()).size()>1){
								agendaStatus = agendaStatusDao
										.getAgendaStatusByAgendaStatusAndProtocolFormIdAndAgendaItemStatus(
												AgendaStatusEnum.AGENDA_APPROVED,
												pfcs.getProtocolFormId(),AgendaItemStatus.REMOVED);
							}else{
								agendaStatus = agendaStatusDao
										.getAgendaStatusByAgendaStatusAndProtocolFormId(
												AgendaStatusEnum.AGENDA_APPROVED,
												pfcs.getProtocolFormId());
							}
							startTime = agendaStatus.getModified().getTime();
							tempStartPfcs = pfcs;
						} catch (Exception e) {
							startTime = 0;
						}
					} else {
						tempStartPfcs = pfcs;
					}

				} else if (startTime > 0
						&& endActions.contains(pfcs
								.getProtocolFormCommitteeStatus())) {
					endTime = pfcs.getModified().getTime();
					endTimeStr = pfcs.getModifiedDateTime();

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
							endTimeStr = DateFormatUtil
									.formateDate(agendaStatus.getModified());
							pfcs.setProtocolFormCommitteeStatus(ProtocolFormCommitteeStatusEnum.IRB_AGENDA_APPROVED);
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
							endTimeStr = DateFormatUtil.formateDate(new Date());
						}
					}

					if (committee.equals(Committee.COVERAGE_REVIEW)
							&& pfcs.getProtocolFormCommitteeStatus()
									.equals(ProtocolFormCommitteeStatusEnum.PENDING_CONSENT_LEGAL_REVIEW)) {

						long startTimeForConsentLegal = pfcs.getModified()
								.getTime();
						long endTimeForConsentLegal = new Date().getTime();
						ProtocolFormCommitteeStatus pfcsForConsentLegalReviewEnd = null;

						if (i < pfcss.size() - 1) {
							endTimeForConsentLegal = pfcss.get(i + 1)
									.getModified().getTime();
							pfcsForConsentLegalReviewEnd = pfcss.get(i + 1);
							pfcsForConsentLegalReviewEnd
									.setCommittee(Committee.PROTOCOL_LEGAL_REVIEW);
							pfcsForConsentLegalReviewEnd
									.setProtocolFormCommitteeStatus(pfcss.get(
											i + 1)
											.getProtocolFormCommitteeStatus());
						}

						String endTimeStrForConsentLegal = DateFormatUtil
								.formateDate(new Date());
						mapKeys.add(""
								+ startTimeForConsentLegal
								+ ProtocolFormCommitteeStatusEnum.PENDING_CONSENT_LEGAL_REVIEW
								+ Committee.PROTOCOL_LEGAL_REVIEW);
						ProtocolFormCommitteeStatus pfcsForConsentLegalReview = pfcs;

						pfcsForConsentLegalReview
								.setCommittee(Committee.PROTOCOL_LEGAL_REVIEW);
						pfcsForConsentLegalReview
								.setProtocolFormCommitteeStatus(ProtocolFormCommitteeStatusEnum.PENDING_CONSENT_LEGAL_REVIEW);

						detailStartActions.add(pfcsForConsentLegalReview);
						detailActions.put(pfcsForConsentLegalReview,
								pfcsForConsentLegalReviewEnd);
						detailActionEndTime.put(pfcsForConsentLegalReview,
								endTimeForConsentLegal);
						detailActionStringEndTime.put(
								pfcsForConsentLegalReview,
								endTimeStrForConsentLegal);
					}
					detailStartActions.add(tempStartPfcs);
					logger.debug(tempStartPfcs + "");
					mapKeys.add("" + tempStartPfcs.getModified().getTime()
							+ tempStartPfcs.getProtocolFormCommitteeStatus()
							+ tempStartPfcs.getCommittee());
					detailActions.put(tempStartPfcs, pfcs);
					detailActionEndTime.put(tempStartPfcs, endTime);
					detailActionStringEndTime.put(tempStartPfcs, endTimeStr);
				}

				if (startTime > 0 && endTime == 0 && i == (pfcss.size() - 1)) {
					endTime = new Date().getTime();
					detailStartActions.add(tempStartPfcs);
					mapKeys.add("" + tempStartPfcs.getModified().getTime()
							+ tempStartPfcs.getProtocolFormCommitteeStatus()
							+ tempStartPfcs.getCommittee());
					detailActions.put(tempStartPfcs, null);
				}

				if (startTime > 0 && endTime > 0) {
					if (endTime - startTime < 0) {
						logger.debug(pf.getFormId() + "#######"
								+ (endTime - startTime));
					}
					startTime = 0;
					endTime = 0;

				}
			}
		}

		for (ProtocolFormCommitteeStatus start : detailStartActions) {
			String[] resultsArray = new String[6];
			long timespan = 0;
			resultsArray[0] = start.getCommittee().getDescription();
			resultsArray[1] = start.getProtocolFormCommitteeStatus()
					.getDescription();
			resultsArray[2] = start.getModifiedDateTime();
			if (detailActions.get(start) != null) {
				resultsArray[3] = detailActions.get(start)
						.getProtocolFormCommitteeStatus().getDescription();
				resultsArray[4] = detailActionStringEndTime.get(start);

				timespan = 1
						+ (detailActionEndTime.get(start) - start.getModified()
								.getTime()) / (24 * 60 * 60 * 1000);

			} else {
				timespan = 1
						+ (new Date().getTime() - start.getModified().getTime())
						/ (24 * 60 * 60 * 1000);
				resultsArray[3] = "";

				resultsArray[4] = "";
			}
			resultsArray[5] = timespan > 1 ? (timespan + " Days")
					: (timespan + " Day");
			preOrderresultMap.put(
					"" + start.getModified().getTime()
							+ start.getProtocolFormCommitteeStatus()
							+ start.getCommittee(), resultsArray);
		}
		// orderResult
		Collections.sort(mapKeys);

		for (String startTime : mapKeys) {
			results.add(preOrderresultMap.get(startTime));
		}
		return results;
	}

	

	private String replaceValues(String value) {

		for (Entry<String, String> values : this.getDefaultValuesMap()
				.entrySet()) {
			// *************should replace later*****/
			value = value.replace("{" + values.getKey() + "}",
					values.getValue());
		}

		return value;
	}

	public String generateDetailReport(long protocolFormId) {
		String finalResultXml = "<report-results>";
		finalResultXml += "<report-result id =\"Detail Actions Report\">";
		finalResultXml += "<title>";
		finalResultXml += "<![CDATA[Protocol: Detail Actions Report]]>";
		finalResultXml += "</title>";
		finalResultXml += "<fields>";
		finalResultXml += "<field id =\"reviewEntity\" desc=\"Review Entity\" hidden=\"false\"/>";
		finalResultXml += "<field id =\"startAction\" desc=\"Start Action\" hidden=\"false\"/>";
		finalResultXml += "<field id =\"startTime\" desc=\"Start Time\" hidden=\"false\"/>";
		finalResultXml += "<field id =\"stopAction\" desc=\"Stop Action\" hidden=\"false\"/>";
		finalResultXml += "<field id =\"stopTime\" desc=\"Stop Time\" hidden=\"false\"/>";
		finalResultXml += "<field id =\"timeSpan\" desc=\"Time Span\" hidden=\"false\"/>";
		finalResultXml += "</fields>";
		finalResultXml += "<report-items>";
		List<Committee> committees = Arrays.asList(Committee.values());
		List<String[]> detailReports = getDetailTimeInEachQueueByProtocoFormlId(
				protocolFormId, committees);
		for (String[] reportItem : detailReports) {
			finalResultXml += "<report-item>";
			finalResultXml += "<field id=\"" + "reviewEntity" + "\">";
			finalResultXml += "<![CDATA[" + reportItem[0] + "]]>";
			finalResultXml += "</field>";
			finalResultXml += "<field id=\"" + "startAction" + "\">";
			finalResultXml += "<![CDATA[" + reportItem[1] + "]]>";
			finalResultXml += "</field>";
			finalResultXml += "<field id=\"" + "startTime" + "\">";
			finalResultXml += "<![CDATA[" + reportItem[2] + "]]>";
			finalResultXml += "</field>";
			finalResultXml += "<field id=\"" + "stopAction" + "\">";
			finalResultXml += "<![CDATA[" + reportItem[3] + "]]>";
			finalResultXml += "</field>";
			finalResultXml += "<field id=\"" + "stopTime" + "\">";
			finalResultXml += "<![CDATA[" + reportItem[4] + "]]>";
			finalResultXml += "</field>";
			finalResultXml += "<field id=\"" + "timeSpan" + "\">";
			finalResultXml += "<![CDATA[" + reportItem[5] + "]]>";
			finalResultXml += "</field>";
			finalResultXml += "</report-item>";
		}
		finalResultXml += "</report-items>";
		finalResultXml += "</report-result>";
		finalResultXml += "</report-results>";
		if(finalResultXml.contains("&")){
			finalResultXml=finalResultXml.replaceAll("&", "&amp;");
		}
		return finalResultXml;

	}

	@Override
	public String generateReportResult(ReportTemplate reportTemplate) {
		String finalResultXml = "<report-results>";
		

		List<ReportCriteria> criterias = reportTemplate.getReportCriterias();

		ObjectMapper objectMapper = new ObjectMapper();

		Map<String, String> fieldsRealXPathMap = Maps.newHashMap();
		Map<String, String> queryCriteriasValueMap = Maps.newHashMap();

		for (ReportCriteria rc : criterias) {
			ReportFieldTemplate reportField = new ReportFieldTemplate();

			try {
				reportField = objectMapper.readValue(rc.getCriteria(),
						ReportFieldTemplate.class);

				String fieldIdentifier = reportField.getFieldIdentifier();

				String value = reportField.getValue();
				if(reportField.getOperator().toString().equals("AFTER")){
					queryCriteriasValueMap.put(reportField.getFieldDisplayName(), "AFTER: "+reportField.getDisplayValue());
				}else if(reportField.getOperator().toString().equals("BEFORE")){
					queryCriteriasValueMap.put(reportField.getFieldDisplayName(), "BEFORE: "+reportField.getDisplayValue());
				}else{
					queryCriteriasValueMap.put(reportField.getFieldDisplayName(), reportField.getDisplayValue());
				}
				if (value != null && !value.isEmpty()) {
					String realXpath = "";

					if (reportField.getOperator().toString().equals("AFTER")
							|| reportField.getOperator().toString()
									.equals("BEFORE")) {
						realXpath = reportField.getNodeXPath().replace(
								"{value}", "'" + value.toUpperCase() + "'");
					} else if (reportField.getOperator().toString()
							.equals("BETWEEN")) {
						realXpath = reportField.getNodeXPath().replace(
								"{value}",
								"'"
										+ value.toUpperCase().substring(
												0,
												value.toUpperCase()
														.indexOf(",")) + "'");
						realXpath = realXpath.replace("{operator}", ">");
						realXpath = realXpath
								+ " AND "
								+ reportField
										.getNodeXPath()
										.replace(
												"{value}",
												"'"
														+ value.toUpperCase()
																.substring(
																		value.toUpperCase()
																				.indexOf(
																						",") + 1,
																		value.length())
														+ "'");
						realXpath = realXpath.replace("{operator}", "<");

					} else {
						if (value.contains("|")) {
							String[] values = value.split("\\|");
							realXpath += "(";
							for (int i = 0; i < values.length; i++) {
								if (i > 0) {
									realXpath += " OR ";
								}
								if (reportField.getNodeXPath().contains(
										".exist")
										|| reportField.getNodeXPath().contains(
												".value")) {
									realXpath += reportField
											.getNodeXPath()
											.replace(
													"{value}",
													"\""
															+ values[i]
																	.toUpperCase()
															+ "\"");
								} else if (values[i].contains("'")) {
									if (value.equals("=1")
											|| value.equals("=0")) {
										realXpath += reportField
												.getNodeXPath()
												.replace("{value}",
														values[i].toUpperCase());
									} else {
										realXpath += reportField
												.getNodeXPath()
												.replace(
														"{value}",
														"\""
																+ values[i]
																		.toUpperCase()
																+ "\"");
									}
								} else {
									realXpath += reportField
											.getNodeXPath()
											.replace(
													"{value}",
													"'"
															+ values[i]
																	.toUpperCase()
															+ "'");
								}
							}
							realXpath += ")";
						} else {
							if (reportField.getNodeXPath().contains(".exist")
									|| reportField.getNodeXPath().contains(
											".value")) {
								if (value.equals("=1") || value.equals("=0")) {
									realXpath = reportField.getNodeXPath()
											.replace("{value}",
													value.toUpperCase());
								} else {
									realXpath = reportField.getNodeXPath()
											.replace(
													"{value}",
													"\"" + value.toUpperCase()
															+ "\"");
								}
							} else if (value.contains("'")) {
								realXpath = reportField.getNodeXPath().replace(
										"{value}", value.toUpperCase());
							} else if(value.toUpperCase().equals("IN")||value.toUpperCase().equals("NOT IN")){
								realXpath = reportField.getNodeXPath().replace("{value}", value);
							}else {
								realXpath = reportField.getNodeXPath().replace(
										"{value}",
										"'" + value.toUpperCase() + "'");
							}
						}
					}

					if (!reportField.getOperator().toString().equals("BETWEEN")) {
						realXpath = realXpath.replace("{operator}", reportField
								.getOperator().getRealOperator());
					}
					fieldsRealXPathMap.put("{" + fieldIdentifier
							+ ".search-xpath}", realXpath);
					// fieldsRealXPathMap.put("{" + fieldIdentifier +
					// ".report-xpath}", reportField.getReportableXPath());
				}
			} catch (JsonParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JsonMappingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		finalResultXml = finalResultXml+generateSummaryCriteriaTable(reportTemplate,
				queryCriteriasValueMap);
		finalResultXml += "<report-result id=\""
				+ reportTemplate.getTypeDescription() + "\"  created=\""
				+ DateFormatUtil.formateDateToMDY(new Date()) + "\">";
		finalResultXml += "<title>" + reportTemplate.getDescription()
				+ "</title>";
		String rawQeury = generateRawQeury(reportTemplate,
				fieldsRealXPathMap);
		List<String> rawQueryResultSearchFields  =Lists.newArrayList();
		rawQueryResultSearchFields.add("protocolid");
		rawQeury = rawQeury.replace("{reportstatment}",
				generateReportStatement(reportTemplate, rawQueryResultSearchFields));
		String realQeury = fillMessage(rawQeury, fieldsRealXPathMap);

		logger.debug("real query: " + realQeury);

		List<Map> resultObjectLst = getReportResultDao().generateResult(
				realQeury);

		try {
			List<ReportField> reportFields = reportFieldDao
					.listAllFieldsByReportTemplateId(reportTemplate.getId());
			List<ReportField> sortedReportFields = Lists.newArrayList();
			for(int i =0;i<reportFields.size();i++){
				for(ReportField field: reportFields){
				if(field.getField().contains("\""+i+"\"")){
					sortedReportFields.add(field);
					if(i==0){
						continue;
					}
					break;
				}
				
			}
		}
			List<String> resultsDisplayList = Lists.newArrayList();
			ReportResultFieldTemplate reportResultFieldTemplate = null;
			for (ReportField reportField : sortedReportFields) {
				reportResultFieldTemplate = objectMapper
						.readValue(reportField.getField(),
								ReportResultFieldTemplate.class);
				resultsDisplayList.add(reportResultFieldTemplate
						.getFieldIdentifier());
			}
			finalResultXml += "<fields>";
			for (ReportField reportField : sortedReportFields) {
				reportResultFieldTemplate = objectMapper
						.readValue(reportField.getField(),
								ReportResultFieldTemplate.class);
				String desc = reportResultFieldTemplate.getFieldDisplayName();

				String identifier = reportResultFieldTemplate
						.getFieldIdentifier();

				// should be able to edit
				String hidden = "false";

				finalResultXml += "<field id=\"" + identifier + "\" desc=\""
						+ desc + "\" hidden=\"" + hidden + "\" />";
			}
			finalResultXml += "</fields>";

			finalResultXml += "<report-items>";
			for (Map rowObject : resultObjectLst) {
				Map<String, String> timeSpentMap = Maps.newHashMap();
				String tempfinalResultXml = "";
				try {
					for (ReportField reportField : sortedReportFields) {
						reportResultFieldTemplate = objectMapper.readValue(
								reportField.getField(),
								ReportResultFieldTemplate.class);

						String identifier = reportResultFieldTemplate
								.getFieldIdentifier();
						String alias = reportResultFieldTemplate.getAlias();
						String value = reportResultFieldTemplate.getValue();
						if (!identifier.equals("protocolid")) {
							continue;
						}
						tempfinalResultXml = "<report-item>";

						long protocolId = Long.valueOf((String) rowObject
								.get(alias));

						List<Committee> committees = Arrays.asList(Committee
								.values());
						timeSpentMap = getTimeInEachQueueByProtocolId(
								protocolId, committees);
						
						if (value == null || value.isEmpty()) {
							value = "<![CDATA[" + (String) rowObject.get(alias);
						} else {
							value = replaceValues(value);
							try {
								value = "<![CDATA["
										+ value.replace("{" + alias + "}",
												(String) rowObject.get(alias));

							} catch (Exception e) {
								// do nothing
							}
						}

						tempfinalResultXml += "<field class=\"field-summary\" id=\""
								+ identifier + "\">";

						tempfinalResultXml += value;
						tempfinalResultXml += "]]></field><field class=\"field-summary\" id=\""
								+ identifier + "-1\">";

						ProtocolForm pf = null;
						try {
							pf = protocolFormDao
									.getLatestProtocolFormByProtocolIdAndProtocolFormType(
											protocolId,
											ProtocolFormType.NEW_SUBMISSION);
						} catch (Exception e) {
							pf = protocolFormDao
									.getLatestProtocolFormByProtocolIdAndProtocolFormType(
											protocolId,
											ProtocolFormType.HUMAN_SUBJECT_RESEARCH_DETERMINATION);
						}

						ProtocolFormStatus pfs = protocolFormStatusDao
								.getLatestProtocolFormStatusByFormId(pf
										.getFormId());
						long pfParentId = pf.getParent().getFormId();

						long totalTime = 0;
						if (Long.valueOf(timeSpentMap.get("finaActionTime")
								.split(" ")[0])
								- getTotalTimeForPI(pfParentId)
										.get("finalTime") > 0) {
							totalTime = (Long.valueOf(timeSpentMap.get(
									"finaActionTime").split(" ")[0]) - getTotalTimeForPI(
									pfParentId).get("startTime"));
						} else {
							totalTime = getTotalTimeForPI(pfParentId).get(
									"finalTime")
									- getTotalTimeForPI(pfParentId).get(
											"startTime");
						}
						totalTime = 1 + totalTime / (24 * 60 * 60 * 1000);
						String dayUnit = "Days";
						if (totalTime == 1) {
							dayUnit = "Day";
						}

						tempfinalResultXml += "<![CDATA[<strong>Current Status</strong>: "
								+ pfs.getProtocolFormStatus().getDescription()
								+ "]]></field><field class=\"field-summary\" id=\""
								+ identifier
								+ "-2\"><![CDATA[<strong>Total (in days):</strong> "
								+ totalTime
								+ " "
								+ dayUnit
								+ "  <div class=\"field-detail pull-right\"><a target=\"_blank\" href=\"/clara-webapp/reports/results/detail/timeinreview/"
								+ pfParentId + "\">Details</a></div>]]>";
						tempfinalResultXml += "</field>";
						tempfinalResultXml += "</report-item>";

						tempfinalResultXml += "<report-item>";
						if (resultsDisplayList.contains("protocolid")) {
							tempfinalResultXml += "<field id=\"" + "protocolid"
									+ "\">";
							tempfinalResultXml += "</field>";
						}
						if (resultsDisplayList.contains("reviewentity")) {
							tempfinalResultXml += "<field id=\""
									+ "reviewentity" + "\">";
							value = "<![CDATA[" + "PI" + "]]>";
							tempfinalResultXml += value;
							tempfinalResultXml += "</field>";
						}
						if (resultsDisplayList.contains("timespent")) {
							tempfinalResultXml += "<field id=\"" + "timespent"
									+ "\">";
							value = "<![CDATA["
									+ getTotalTimeForPI(pfParentId).get(
											"pitotalTime") + "]]>";
							tempfinalResultXml += value;
							tempfinalResultXml += "</field>";
						}
						tempfinalResultXml += "</report-item>";

					}
					finalResultXml += tempfinalResultXml;

				} catch (Exception e) {
					e.printStackTrace();
					continue;
				}

				for (Entry<String, String> values : timeSpentMap.entrySet()) {
					if (values.getKey().equals("finaActionTime")) {
						continue;
					}
					finalResultXml += "<report-item>";
					if (resultsDisplayList.contains("protocolid")) {
						finalResultXml += "<field id=\"" + "protocolid" + "\">";
						finalResultXml += "</field>";
					}
					if (resultsDisplayList.contains("reviewentity")) {
						finalResultXml += "<field id=\"" + "reviewentity"
								+ "\">";
						String value = "<![CDATA[" + values.getKey() + "]]>";
						finalResultXml += value;
						finalResultXml += "</field>";
					}
					if (resultsDisplayList.contains("timespent")) {
						finalResultXml += "<field id=\"" + "timespent" + "\">";
						String value = "<![CDATA[" + values.getValue() + "]]>";
						finalResultXml += value;
						finalResultXml += "</field>";
					}
					finalResultXml += "</report-item>";
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		finalResultXml += "</report-items>";

		finalResultXml += "</report-result>";

		finalResultXml += "</report-results>";

		finalResultXml = finalResultXml.replace("<![CDATA[null]]>", "");
		finalResultXml = finalResultXml.replace("null&lt;br&gt;", "");
		finalResultXml =finalResultXml.replace("&gt;null", "&gt;");
		if(finalResultXml.contains("&")){
			finalResultXml=finalResultXml.replaceAll("&", "&amp;");
		}
		return finalResultXml;
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

	public ProtocolFormCommitteeStatusDao getProtocolFormCommitteeStatusDao() {
		return protocolFormCommitteeStatusDao;
	}

	@Autowired(required = true)
	public void setProtocolFormCommitteeStatusDao(
			ProtocolFormCommitteeStatusDao protocolFormCommitteeStatusDao) {
		this.protocolFormCommitteeStatusDao = protocolFormCommitteeStatusDao;
	}

	public ProtocolFormDao getProtocolFormDao() {
		return protocolFormDao;
	}

	@Autowired(required = true)
	public void setProtocolFormDao(ProtocolFormDao protocolFormDao) {
		this.protocolFormDao = protocolFormDao;
	}

	public ReportFieldDao getReportFieldDao() {
		return reportFieldDao;
	}

	@Autowired(required = true)
	public void setReportFieldDao(ReportFieldDao reportFieldDao) {
		this.reportFieldDao = reportFieldDao;
	}

	public AgendaStatusDao getAgendaStatusDao() {
		return agendaStatusDao;
	}

	@Autowired(required = true)
	public void setAgendaStatusDao(AgendaStatusDao agendaStatusDao) {
		this.agendaStatusDao = agendaStatusDao;
	}

	public AgendaItemDao getAgendaItemDao() {
		return agendaItemDao;
	}

	@Autowired(required = true)
	public void setAgendaItemDao(AgendaItemDao agendaItemDao) {
		this.agendaItemDao = agendaItemDao;
	}

}