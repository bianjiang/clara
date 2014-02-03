package edu.uams.clara.migration.service.impl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.bouncycastle.util.encoders.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import edu.uams.clara.core.util.xml.DomUtils;
import edu.uams.clara.core.util.xml.DomUtils.Encoding;
import edu.uams.clara.integration.incoming.crimson.dao.CrimsonContractDao;
import edu.uams.clara.integration.incoming.crimson.dao.CrimsonStudyDao;
import edu.uams.clara.migration.service.ContractMigrationService;
import edu.uams.clara.webapp.common.dao.history.TrackDao;
import edu.uams.clara.webapp.common.dao.relation.RelatedObjectDao;
import edu.uams.clara.webapp.common.dao.usercontext.PersonDao;
import edu.uams.clara.webapp.common.dao.usercontext.UserDao;
import edu.uams.clara.webapp.common.domain.history.Track;
import edu.uams.clara.webapp.common.domain.relation.RelatedObject;
import edu.uams.clara.webapp.common.domain.usercontext.enums.Committee;
import edu.uams.clara.webapp.common.service.impl.UserServiceImpl;
import edu.uams.clara.webapp.contract.dao.ContractDao;
import edu.uams.clara.webapp.contract.dao.businesslogicobject.ContractFormCommitteeStatusDao;
import edu.uams.clara.webapp.contract.dao.businesslogicobject.ContractFormStatusDao;
import edu.uams.clara.webapp.contract.dao.businesslogicobject.ContractStatusDao;
import edu.uams.clara.webapp.contract.dao.contractform.ContractFormDao;
import edu.uams.clara.webapp.contract.dao.contractform.ContractFormXmlDataDao;
import edu.uams.clara.webapp.contract.dao.contractform.ContractFormXmlDataDocumentDao;
import edu.uams.clara.webapp.contract.domain.Contract;
import edu.uams.clara.webapp.contract.domain.businesslogicobject.ContractFormCommitteeStatus;
import edu.uams.clara.webapp.contract.domain.businesslogicobject.ContractFormStatus;
import edu.uams.clara.webapp.contract.domain.businesslogicobject.ContractStatus;
import edu.uams.clara.webapp.contract.domain.businesslogicobject.enums.ContractFormCommitteeStatusEnum;
import edu.uams.clara.webapp.contract.domain.businesslogicobject.enums.ContractFormStatusEnum;
import edu.uams.clara.webapp.contract.domain.businesslogicobject.enums.ContractStatusEnum;
import edu.uams.clara.webapp.contract.domain.contractform.ContractForm;
import edu.uams.clara.webapp.contract.domain.contractform.ContractFormXmlData;
import edu.uams.clara.webapp.contract.domain.contractform.ContractFormXmlDataDocument;
import edu.uams.clara.webapp.contract.domain.contractform.ContractFormXmlDataDocument.Status;
import edu.uams.clara.webapp.contract.domain.contractform.enums.ContractFormType;
import edu.uams.clara.webapp.contract.domain.contractform.enums.ContractFormXmlDataType;
import edu.uams.clara.webapp.fileserver.dao.UploadedFileDao;
import edu.uams.clara.webapp.fileserver.domain.UploadedFile;
import edu.uams.clara.webapp.fileserver.service.SFTPService;
import edu.uams.clara.webapp.protocol.dao.ProtocolDao;
import edu.uams.clara.webapp.xml.processor.XmlProcessor;

public class ContractMigrationServiceImpl implements ContractMigrationService {
	private final static Logger logger = LoggerFactory
			.getLogger(ContractMigrationServiceImpl.class);

	// CRIMSON data
	private CrimsonContractDao crimsonContractDao;
	private CrimsonStudyDao crimsonStudyDao;

	private UserDao userDao;
	private PersonDao personDao;
	private UserServiceImpl userServiceImpl;
	private ContractDao contractDao;
	private ContractStatusDao contractStatusDao;
	private ContractFormDao contractFormDao;
	private ContractFormStatusDao contractFormStatusDao;
	private ContractFormXmlDataDao contractFormXmlDataDao;
	private TrackDao trackDao;
	private RelatedObjectDao relatedObjectDao;

	private ContractFormXmlDataDocumentDao contractFormXmlDataDocumentDao;
	private UploadedFileDao uploadedFileDao;
	private SFTPService sFTPService;
	private ProtocolDao protocolDao;
	private ContractFormCommitteeStatusDao contractFormCommitteeStatusDao;

	private XmlProcessor xmlProcessor;

	private String localDirectory;

	private MessageDigest messageDigest = null;

	private Document processFundingInfoForContract(Document doc,
			Object[] crimosnContract) {
		NodeList contractList = doc.getElementsByTagName("contract");
		Element contractEle = (Element) contractList.item(0);
		BigInteger crimsonContractID = (BigInteger) crimosnContract[0];
		if (crimsonContractDao.findFundingInfoByContractID(crimsonContractID) != null) {
			Element fundingEle = doc.createElement("funding");
			contractEle.appendChild(fundingEle);
			List<Object[]> fundingList = crimsonContractDao
					.findFundingInfoByContractID(crimsonContractID);
			for (int i = 0; i < fundingList.size(); i++) {
				Element fundingSrcEle = doc.createElement("funding-source");
				fundingEle.appendChild(fundingSrcEle);
				Object[] funding = fundingList.get(i);

				fundingSrcEle.setAttribute("amount", "");
				if (funding[2] != null) {
					int partial = (Short) funding[2];
					if (partial == 1)
						fundingSrcEle.setAttribute("amount", "Partial");
					else
						fundingSrcEle.setAttribute("amount", "Full");
				}

				fundingSrcEle.setAttribute("department", "");
				fundingSrcEle.setAttribute("entityid", "");
				fundingSrcEle.setAttribute("entityname", "");
				fundingSrcEle.setAttribute("id", UUID.randomUUID().toString());
				fundingSrcEle.setAttribute("name", "");
				fundingSrcEle.setAttribute("projectid", "");
				fundingSrcEle.setAttribute("projectpi", "");
				fundingSrcEle.setAttribute("type", "");

				int sponsorID = -1;
				if (funding[1] != null) {
					String sponsorIDStr = (String) funding[1];
					sponsorID = Integer.valueOf(sponsorIDStr);
				}

				// check the type of funding
				if (funding[0] != null) {
					int fundingTypeID = (Short) funding[0];
					String fundingType = crimsonStudyDao
							.findFundingTypeByID(fundingTypeID);
					if (fundingType.equals("Funding Agency")) {
						fundingSrcEle.setAttribute("type", "External");

						if (sponsorID > 0)
							fundingSrcEle.setAttribute("entityname",
									crimsonStudyDao
											.findSponsorNameByID(sponsorID));

						fundingSrcEle.setAttribute("entitytype", "Agency");

					} else if (fundingType.equals("CRO")
							|| fundingType.equals("SMO")) {
						fundingSrcEle.setAttribute("type", fundingType);

						if (sponsorID > 0)
							fundingSrcEle.setAttribute("entityname",
									crimsonStudyDao
											.findCRONamebyID(sponsorID));

						fundingSrcEle.setAttribute("entitytype", "Agency");

					} else if (fundingType.equals("Internal")) {
						fundingSrcEle.setAttribute("type", "Internal");

						if (sponsorID > 0)
							fundingSrcEle.setAttribute("entityname",
									crimsonStudyDao
											.findInternalSrc(sponsorID));

						fundingSrcEle.setAttribute("entitytype", "Cost Center");

					} else if (fundingType.equals("ARIA Project")) {
						fundingSrcEle.setAttribute("type", "Project");

						if (sponsorID > 0) {
							fundingSrcEle.setAttribute("entityname",
									crimsonStudyDao
											.findSponsorNameByID(sponsorID));
							Object[] project = crimsonStudyDao
									.findProjectInfoByID(sponsorID + "");
							String projectTitle = (String) project[1];
							fundingSrcEle.setAttribute("name", projectTitle);

							fundingSrcEle.setAttribute("projectid", sponsorID
									+ "");
							int piserial = (Integer) project[0];
							Object[] pi = crimsonStudyDao
									.findAriaUserByUserID(piserial);

							fundingSrcEle.setAttribute("projectpi",
									(String) pi[2] + " " + (String) pi[1]);
						}

						fundingSrcEle.setAttribute("entitytype", "Agency");

					}
				}

			}
		}
		return doc;

	}

	private Document processBasicInfoForContract(Document doc,
			Object[] crimosnContract) {
		// get object for contract information
		BigInteger crimsonContractID = (BigInteger) crimosnContract[0];
		Object[] contractInfo = crimsonContractDao
				.findContractInfoByContractId(crimsonContractID);

		// create root element
		Element contractEle = doc.createElement("contract");
		doc.appendChild(contractEle);
		String contractNum = (String) crimosnContract[1];
		// get contract id for clara from contract number
		String[] splitContractNum = contractNum.split("-");
		String[] splitContractNum2 = splitContractNum[0].split("C");
		contractEle.setAttribute("id", splitContractNum2[1]);
		contractEle.setAttribute("identifier", contractNum);
		Date createdDate = (Date) crimosnContract[3];
		SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
		String createdDateString = dateFormat.format(createdDate);
		contractEle.setAttribute("created", createdDateString);
		int parintID = (Integer) crimosnContract[6];
		if (parintID == 0)
			contractEle.setAttribute("type", "New Contract");
		else
			contractEle.setAttribute("type", "Amendment");

		// get status info
		doc = getStatusInfo(doc, crimosnContract, contractEle);

		// get basic-info
		Element basicInfoEle = doc.createElement("basic-info");
		contractEle.appendChild(basicInfoEle);
		Element beginDateEle = doc.createElement("contract-begin-date");
		basicInfoEle.appendChild(beginDateEle);
		if (contractInfo[6] != null) {
			Date startDate = (Date) contractInfo[6];
			String startDateString = dateFormat.format(startDate);
			beginDateEle.setTextContent(startDateString);
		}
		Element endDateEle = doc.createElement("contract-end-date");
		basicInfoEle.appendChild(endDateEle);
		if (contractInfo[7] != null) {
			Date endDate = (Date) contractInfo[7];
			String endDateString = dateFormat.format(endDate);
			endDateEle.setTextContent(endDateString);
		}
		Element execDateEle = doc.createElement("contract-execution-date");
		basicInfoEle.appendChild(execDateEle);
		if (contractInfo[13] != null) {
			Date execDate = (Date) contractInfo[13];
			String execDateString = dateFormat.format(execDate);
			execDateEle.setTextContent(execDateString);
		}

		// get basic information
		Element basicInformationEle = doc.createElement("basic-information");
		contractEle.appendChild(basicInformationEle);
		Element contractTitleEle = doc.createElement("nature");
		basicInformationEle.appendChild(contractTitleEle);
		if (contractInfo[2] != null) {
			String contractTitle = (String) contractInfo[2];
			contractTitleEle.setTextContent(contractTitle);
		}
		Element contracTtypeEle = doc.createElement("contract-type");
		basicInformationEle.appendChild(contracTtypeEle);
		// set study related info, default set as no, if protocol related
		// changed to y later
		Element studyRelatedEle = doc.createElement("is-study-related");
		basicInformationEle.appendChild(studyRelatedEle);
		studyRelatedEle.setTextContent("n");

		// get type information
		Element typeEle = doc.createElement("type");
		contractEle.appendChild(typeEle);
		// contractInfo[3] is the contract type id
		if (contractInfo[3] != null) {
			int contractID = (int) contractInfo[3];
			String contractType = crimsonContractDao
					.findContractTypebyID(contractID);
			if (!contractType.isEmpty()) {
				typeEle.setTextContent(contractTypeMapping(contractType));
				contracTtypeEle
						.setTextContent(contractTypeMapping(contractType));
			}

		}

		// get sponsors
		doc = getSponsorInfo(doc, crimsonContractID, contractEle);

		// get protocol info
		Element protocolEle = doc.createElement("protocol");
		contractEle.appendChild(protocolEle);
		if (contractInfo[4] != null) {
			Integer protocolID = (Integer) contractInfo[4];

			if (protocolID > 0) {
				// set study related tag
				studyRelatedEle.setTextContent("y");

				// set study type
				Element studyTypeEle = doc.createElement("study-type");
				contractEle.appendChild(studyTypeEle);

				if (crimsonStudyDao.findStudyTypeIDsbyIRBNum(protocolID
						.toString()) != null) {
					String studyTypeIDs = crimsonStudyDao
							.findStudyTypeIDsbyIRBNum(protocolID.toString());
					if (studyTypeIDs.contains("|")) {
						String splitStudyTypeIDs[] = studyTypeIDs.split("\\|");
						for (int j = 0; j < splitStudyTypeIDs.length; j++) {
							studyTypeEle.setTextContent(crimsonStudyDao
									.findStudyType(splitStudyTypeIDs[j]));
						}
					} else {
						studyTypeEle.setTextContent(crimsonStudyDao
								.findStudyType(studyTypeIDs));
					}

				} else {
					studyTypeEle.setTextContent("unknown");
				}

				// get protocol title info
				Element titleEle = doc.createElement("title");
				contractEle.appendChild(titleEle);
				titleEle.setTextContent(crimsonContractDao
						.findIRBTitleByID(protocolID));

				protocolEle.setTextContent(String.valueOf(protocolID));

				// set info in related table for protocol
				RelatedObject relatedObject = new RelatedObject();
				try {
					relatedObject = relatedObjectDao
							.findRelatedObjectByObjIDTypeandRelatedObjIDType(
									crimsonContractID.longValue(), "Contract",
									protocolID.longValue(), "Protocol");
				} catch (Exception e) {
				}
				relatedObject.setCreated(new Date());
				relatedObject.setObjectId(crimsonContractID.longValue());
				relatedObject.setObjectType("Contract");
				relatedObject.setRelatedObjectId(protocolID.longValue());
				relatedObject.setRelatedObjectType("Protocol");
				relatedObject.setRetired(false);

				relatedObjectDao.saveOrUpdate(relatedObject);
			}

		}

		// get staff info
		Element staffsEle = doc.createElement("staffs");
		contractEle.appendChild(staffsEle);
		// get staff ids in crimson
		List<Object[]> staffIDList = crimsonContractDao
				.findMultipleSelectByContractID(crimsonContractID);
		if (staffIDList != null) {
			doc = getUserInfo(doc, staffIDList, staffsEle);

		}

		// get reviewers info
		doc = getReviewerInfo(doc, crimsonContractID, contractEle);

		return doc;
	}

	private Document getReviewerInfo(Document doc,
			BigInteger crimsonContractID, Element contractEle) {

		List<Object[]> reviewerList = crimsonContractDao
				.findReviewerInfoByContractID(crimsonContractID);
		if (reviewerList != null) {
			Element committeeRevEle = doc.createElement("committee-review");
			contractEle.appendChild(committeeRevEle);
			Element committeeAdminEle = doc.createElement("committee");
			committeeRevEle.appendChild(committeeAdminEle);
			Element committeeLegalEle = doc.createElement("committee");
			committeeRevEle.appendChild(committeeLegalEle);
			committeeAdminEle.setAttribute("type", "CONTRACT_ADMIN");
			committeeLegalEle.setAttribute("type", "CONTRACT_LEGAL_REVIEW");

			Element assigRevsForAdmEle = doc
					.createElement("assigned-reviewers");
			committeeAdminEle.appendChild(assigRevsForAdmEle);
			Element assigRevsForLegalEle = doc
					.createElement("assigned-reviewers");
			committeeLegalEle.appendChild(assigRevsForLegalEle);

			for (int i = 0; i < reviewerList.size(); i++) {

				Object[] reviewer = reviewerList.get(i);
				int userID = (Integer) reviewer[0];
				int userGroupID = (Integer) reviewer[1];
				String userGroup = crimsonContractDao
						.findReviewerGroupByID(userGroupID);
				Object[] user = crimsonStudyDao.findAriaUserByUserID(userID);
				String fullname = (String) user[2] + ", " + (String) user[1];



				Element assiRevEle = doc.createElement("assigned-reviewer");
				assiRevEle.setAttribute("assigning-committee",
						"CONTRACT_MANAGER");
				assiRevEle.setTextContent(fullname);

				String sapIDStr = (String) user[0];

				// if sap is not null, search it in clara to get user id
				if (!sapIDStr.isEmpty()) {
					// convert sap in into clara format, remove 000 in
					// front
					int sapID = Integer.valueOf(sapIDStr);
					sapIDStr = String.valueOf(sapID);
					// if user existed, directly use the id
					if (!userDao.getUserBySAP(sapIDStr).isEmpty()) {
						assiRevEle.setAttribute("user-id", ""
								+ userDao.getUserBySAP(sapIDStr).get(0)
										.getId());
					}
					// else create the user
					else {
						try {
							userServiceImpl.getAndUpdateUserBySap(
									sapIDStr, true);
							assiRevEle.setAttribute("user-id", ""
									+ userDao.getUserBySAP(sapIDStr)
											.get(0).getId());
						} catch (Exception e) {
							// logger.debug("user not found in Ldap: "+sapIDStr);
						}
					}

				}

				if (userGroup.equals("Legal")) {
					assigRevsForAdmEle.appendChild(assiRevEle);
					assiRevEle.setAttribute("user-role",
							"ROLE_CONTRACT_LEGAL_REVIEW");
					assiRevEle.setAttribute("user-role-committee",
							"CONTRACT_LEGAL_REVIEW");
					assiRevEle.setAttribute("user-fullname", fullname);
				}

				else if (userGroup.equals("Contract Administrator")) {
					assigRevsForLegalEle.appendChild(assiRevEle);
					assiRevEle.setAttribute("user-role", "ROLE_CONTRACT_ADMIN");
					assiRevEle.setAttribute("user-role-committee",
							"CONTRACT_ADMIN");
					assiRevEle.setAttribute("user-fullname", fullname);
				}

			}

		}
		return doc;

	}

	private Document getStatusInfo(Document doc, Object[] crimosnContract,
			Element contractEle) {
		Element statusEle = doc.createElement("status");
		contractEle.appendChild(statusEle);

		if (crimosnContract[5] != null) {
			int statusID = (Integer) crimosnContract[5];
			String status = crimsonContractDao.findStatusByID(statusID);
			if (!status.isEmpty()) {
				String statusForClara;
				switch (status) {
				case "Cancelled":
					statusForClara = "Cancelled";
					break;
				case "Deleted(Not Being Used)":
					statusForClara = "Deleted (Not Being Used)";
					break;
				case "New Contract":
					statusForClara = "Draft";
					break;
				case "Pending Assign Contract Reviewer":
					statusForClara = "Pending Reviewer Assignment";
					break;
				case "Pending Budget Review":
					statusForClara = "Pending Budget Review";
					break;
				case "Pending Coverage Review":
					statusForClara = "Pending Coverage";
					break;
				case "In Contract Review":
					statusForClara = "Under Contract Review";
					break;
				case "In Legal Review":
					statusForClara = "Under Legal Review";
					break;
				case "Pending PI Review":
					statusForClara = "Pending PI";
					break;
				case "Final Legal Approval":
					statusForClara = "Final Legal Approval";
					break;
				case "Pending Signatures":
					statusForClara = "Pending Signature";
					break;
				case "Executed Contract":
					statusForClara = "Contract Executed";
					break;
				default:
					statusForClara = "";
					break;
				}

				if (!statusForClara.isEmpty()) {
					statusEle.setTextContent(statusForClara);
					if (statusForClara.contains("Pending"))
						statusEle.setAttribute("priority", "WARN");
					else
						statusEle.setAttribute("priority", "INFO");
				}

			}
		}
		return doc;

	}

	private ContractStatusEnum getStatusOnly(Object[] contractInfo) {
		ContractStatusEnum statusForClara = null;
		if (contractInfo[5] != null) {
			int statusID = (Integer) contractInfo[5];
			String status = crimsonContractDao.findStatusByID(statusID);
			if (!status.isEmpty()) {

				switch (status) {
				case "Cancelled":
					statusForClara = ContractStatusEnum.CANCELLED;
					break;
				case "Deleted(Not Being Used)":
					statusForClara = null;
					break;
				case "New Contract":
					statusForClara = ContractStatusEnum.DRAFT;
					break;
				case "Pending Assign Contract Reviewer":
					statusForClara = ContractStatusEnum.PENDING_REVIEWER_ASSIGNMENT;
					break;
				case "Pending Budget Review":
					statusForClara = ContractStatusEnum.PENDING_BUDGET;
					break;
				case "Pending Coverage Review":
					statusForClara = ContractStatusEnum.PENDING_COVERAGE;
					break;
				case "In Contract Review":
					statusForClara = ContractStatusEnum.UNDER_CONTRACT_REVIEW;
					break;
				case "In Legal Review":
					statusForClara = ContractStatusEnum.UNDER_LEGAL_REVIEW;
					break;
				case "Pending PI Review":
					statusForClara = ContractStatusEnum.PENDING_PI;
					break;
				case "Final Legal Approval":
					statusForClara = ContractStatusEnum.FINAL_LEGAL_APPROVAL;
					break;
				case "Pending Signatures":
					statusForClara = ContractStatusEnum.PENDING_SIGNATURE;
					break;
				case "Executed Contract":
					statusForClara = ContractStatusEnum.CONTRACT_EXECUTED;
					break;
				default:
					statusForClara = null;
					break;
				}
			}
		}
		return statusForClara;
	}

	private ContractFormStatusEnum getFormStatusOnly(Object[] contractInfo) {
		ContractFormStatusEnum statusForClara = null;
		if (contractInfo[5] != null) {
			int statusID = (Integer) contractInfo[5];
			String status = crimsonContractDao.findStatusByID(statusID);
			if (!status.isEmpty()) {

				switch (status) {
				case "Cancelled":
					statusForClara = ContractFormStatusEnum.CANCELLED;
					break;
				case "Deleted(Not Being Used)":
					statusForClara = null;
					break;
				case "New Contract":
					statusForClara = ContractFormStatusEnum.DRAFT;
					break;
				case "Pending Assign Contract Reviewer":
					statusForClara = ContractFormStatusEnum.PENDING_REVIEWER_ASSIGNMENT;
					break;
				case "Pending Budget Review":
					statusForClara = ContractFormStatusEnum.PENDING_BUDGET;
					break;
				case "Pending Coverage Review":
					statusForClara = ContractFormStatusEnum.PENDING_COVERAGE;
					break;
				case "In Contract Review":
					statusForClara = ContractFormStatusEnum.UNDER_CONTRACT_REVIEW;
					break;
				case "In Legal Review":
					statusForClara = ContractFormStatusEnum.UNDER_LEGAL_REVIEW;
					break;
				case "Pending PI Review":
					statusForClara = ContractFormStatusEnum.PENDING_PI;
					break;
				case "Final Legal Approval":
					statusForClara = ContractFormStatusEnum.FINAL_LEGAL_APPROVAL;
					break;
				case "Pending Signatures":
					statusForClara = ContractFormStatusEnum.PENDING_SIGNATURE;
					break;
				case "Executed Contract":
					statusForClara = ContractFormStatusEnum.CONTRACT_EXECUTED;
					break;
				default:
					statusForClara = null;
					break;
				}
			}
		}
		return statusForClara;
	}

	private Document getSponsorInfo(Document doc, BigInteger crimsonContractID,
			Element contractEle) {
		Element sponsorsEle = doc.createElement("sponsors");
		contractEle.appendChild(sponsorsEle);

		List<Object[]> sponsorList = crimsonContractDao
				.findContactbyContractID(crimsonContractID);
		if (sponsorList != null) {
			for (int i = 0; i < sponsorList.size(); i++) {
				Object[] sponsor = sponsorList.get(i);

				Element sponsorEle = doc.createElement("sponsor");
				sponsorsEle.appendChild(sponsorEle);
				String sponsorID = UUID.randomUUID().toString();
				sponsorEle.setAttribute("id", sponsorID);

				Element sponsorNameEle = doc.createElement("name");
				sponsorEle.appendChild(sponsorNameEle);
				if (sponsor[1] != null) {
					String sponsorName = (String) sponsor[1];
					sponsorNameEle.setTextContent(sponsorName);
				}

				Element sponsorCompanyEle = doc.createElement("company");
				sponsorEle.appendChild(sponsorCompanyEle);
				if (sponsor[0] != null) {
					String sponsorCompany = (String) sponsor[0];
					sponsorCompanyEle.setTextContent(sponsorCompany);
				}

				// no title info in crimson sponsor, just create the tag here
				Element sponsorTitleEle = doc.createElement("title");
				sponsorEle.appendChild(sponsorTitleEle);

				Element sponsorPhoneEle = doc.createElement("phone");
				sponsorEle.appendChild(sponsorPhoneEle);
				if (sponsor[2] != null) {
					String sponsorPhone = (String) sponsor[2];
					sponsorPhoneEle.setTextContent(sponsorPhone);
				}

				Element sponsorFaxEle = doc.createElement("fax");
				sponsorEle.appendChild(sponsorFaxEle);
				if (sponsor[3] != null) {
					String sponsorFax = (String) sponsor[3];
					sponsorFaxEle.setTextContent(sponsorFax);
				}

				Element sponsorEmailEle = doc.createElement("email");
				sponsorEle.appendChild(sponsorEmailEle);
				if (sponsor[4] != null) {
					String sponsorEmail = (String) sponsor[4];
					sponsorEmailEle.setTextContent(sponsorEmail);
				}

				Element sponsorAddressEle = doc.createElement("address");
				sponsorEle.appendChild(sponsorAddressEle);
				if (sponsor[5] != null) {
					String sponsorAddress = (String) sponsor[5];
					sponsorAddressEle.setTextContent(sponsorAddress);
				}
			}
		}
		return doc;
	}

	private Document getUserInfo(Document doc, List<Object[]> staffIDList,
			Element staffsEle) {
		List<String> userNameList = new ArrayList<String>();
		for (int i = 0; i < staffIDList.size(); i++) {
			Object[] Crimsonstaff = staffIDList.get(i);
			if (Crimsonstaff[1] != null) {

				String userID = (String) Crimsonstaff[1];
				Object[] user = crimsonStudyDao.findAriaUserByUserID(Integer
						.valueOf(userID));
				// test if user is already exist

				String userName = "";
				if (user[1] != null)
					userName += (String) user[1];

				if (user[2] != null)
					userName += (String) user[2];

				int staffIndex = -1;

				if (userNameList.contains(userName))
					staffIndex = userNameList.indexOf(userName);
				else
					userNameList.add(userName);

				// staff existed
				if (staffIndex > -1) {
					Element userEle = (Element) doc
							.getElementsByTagName("user").item(staffIndex);
					Element rolesEle = (Element) userEle.getElementsByTagName(
							"roles").item(0);
					Element respsEle = (Element) userEle.getElementsByTagName(
							"reponsibilities").item(0);

					if (Crimsonstaff[0] != null) {
						int roleTypeID = (Integer) Crimsonstaff[0];
						String roleStr = crimsonContractDao
								.findStaffRoleByType(roleTypeID).trim();
						if (roleStr.endsWith("Budget Manager")) {
							Element respEle = doc
									.createElement("responsibility");
							respsEle.appendChild(respEle);
							respEle.setTextContent("Budget Manager");
						} else {
							Element roleEle = doc.createElement("role");
							rolesEle.appendChild(roleEle);
							if (roleStr.endsWith("Study Contact"))
								roleEle.setTextContent("Study Coordinator");
							else if (roleStr.endsWith("Primary Investigator"))
								roleEle.setTextContent("Principal Investigator");
							else {
								roleEle.setTextContent(roleStr.trim());
							}
						}

					}

				} else {
					Element staffEle = doc.createElement("staff");
					staffsEle.appendChild(staffEle);
					Element notifyEle = doc.createElement("notify");
					staffEle.appendChild(notifyEle);
					notifyEle.setTextContent("true");

					String staffID = UUID.randomUUID().toString();
					staffEle.setAttribute("id", staffID);

					Element userEle = doc.createElement("user");
					staffEle.appendChild(userEle);

					// keep aria_user id as attribute in user
					if (user[5] == null)
						userEle.setAttribute("pi_serial", "0");
					else {
						int piSerial = (Integer) user[5];
						userEle.setAttribute("pi_serial",
								String.valueOf(piSerial));
					}

					userEle.setAttribute("phone", "");
					if (user[4] != null) {
						userEle.setAttribute("phone", (String) user[4]);
					}

					if (user[0] != null) {
						userEle.setAttribute("sap", "");
						userEle.setAttribute("id", "");
						String sapIDStr = (String) user[0];

						// if sap is not null, search it in clara to get user id
						if (!sapIDStr.isEmpty()) {
							// convert sap in into clara format, remove 000 in
							// front
							int sapID = Integer.valueOf(sapIDStr);
							sapIDStr = String.valueOf(sapID);
							userEle.setAttribute("sap", sapIDStr);

							// if user existed, directly use the id
							if (!userDao.getUserBySAP(sapIDStr).isEmpty()) {
								userEle.setAttribute("id", ""
										+ userDao.getUserBySAP(sapIDStr).get(0)
												.getId());
							}
							// else create the user
							else {
								try {
									userServiceImpl.getAndUpdateUserBySap(
											sapIDStr, true);
									userEle.setAttribute("id", ""
											+ userDao.getUserBySAP(sapIDStr)
													.get(0).getId());
								} catch (Exception e) {
									// logger.debug("user not found in Ldap: "+sapIDStr);
								}
							}

						}

					}

					// get sub element info for the user
					Element lastNameEle = doc.createElement("lastname");
					userEle.appendChild(lastNameEle);
					if (user[1] != null)
						lastNameEle.setTextContent((String) user[1]);

					Element firstNameEle = doc.createElement("firstname");
					userEle.appendChild(firstNameEle);
					if (user[2] != null)
						firstNameEle.setTextContent((String) user[2]);

					Element emailEle = doc.createElement("email");
					userEle.appendChild(emailEle);
					if (user[3] != null)
						emailEle.setTextContent((String) user[3]);

					Element rolesEle = doc.createElement("roles");
					userEle.appendChild(rolesEle);
					Element roleEle = doc.createElement("role");
					rolesEle.appendChild(roleEle);
					Element respsEle = doc.createElement("reponsibilities");
					userEle.appendChild(respsEle);
					Element respEle = doc.createElement("responsibility");
					respsEle.appendChild(respEle);
					if (Crimsonstaff[0] != null) {
						int roleTypeID = (Integer) Crimsonstaff[0];
						String roleStr = crimsonContractDao
								.findStaffRoleByType(roleTypeID).trim();
						if (roleStr.endsWith("Study Contact"))
							roleEle.setTextContent("Study Coordinator");
						else if (roleStr.endsWith("Primary Investigator"))
							roleEle.setTextContent("Principal Investigator");
						else
							roleEle.setTextContent(roleStr.trim());

						if (roleStr.endsWith("Budget Manager"))
							respsEle.setTextContent("Budget Manager");
					}

					Element costsEle = doc.createElement("costs");
					userEle.appendChild(costsEle);
					Element conflictInterestEle = doc
							.createElement("conflict-of-interest");
					userEle.appendChild(conflictInterestEle);
					conflictInterestEle.setTextContent("false");
				}
			}
		}
		return doc;
	}

	// private void createUserAccount(Object[] ariaUser, String sapIDStr) {
	// Person person = new Person();
	// person.setRetired(false);
	//
	// person.setSap(sapIDStr);
	// if (ariaUser[6] != null)
	// person.setDepartment((String) ariaUser[6]);
	//
	// if (ariaUser[1] != null)
	// person.setLastname((String) ariaUser[1]);
	//
	// if (ariaUser[2] != null)
	// person.setFirstname((String) ariaUser[2]);
	//
	// if (ariaUser[3] != null)
	// person.setEmail((String) ariaUser[3]);
	//
	// if (ariaUser[4] != null)
	// person.setWorkphone((String) ariaUser[4]);
	//
	// person.setUsername(((String) ariaUser[1] + (String) ariaUser[2])
	// .toLowerCase());
	//
	// //personDao.saveOrUpdate(person);
	//
	// User user = new User();
	// user.setRetired(false);
	// user.setAccountNonExpired(true);
	// user.setAccountNonLocked(true);
	// user.setCredentialsNonExpired(true);
	// user.setEnabled(true);
	// user.setUserType(UserType.LDAP_USER);
	// user.setUsername(((String) ariaUser[1] + (String) ariaUser[2])
	// .toLowerCase());
	// user.setPerson(person);
	// user.setTrained(false);
	//
	// userDao.saveOrUpdate(user);
	// }

	public void processRelatedContracts() {
		List<Object[]> RelatedContractsPairList = crimsonContractDao
				.findAllRelatedContracts();
		for (int i = 0; i < RelatedContractsPairList.size(); i++) {
			Object[] RelatedContractPair = RelatedContractsPairList.get(i);
			int objectID = (int) RelatedContractPair[0];
			int relatedObjectID = (int) RelatedContractPair[1];

			String ObjectcontractNum = crimsonContractDao
					.findtxtContractNumByContractID(objectID);
			// get contract id for clara from contract number
			String[] splitContractNum = ObjectcontractNum.split("-");
			String[] splitContractNum2 = splitContractNum[0].split("C");
			long objectcontractID = Long.valueOf(splitContractNum2[1]);

			String RelatedObjectcontractNum = crimsonContractDao
					.findtxtContractNumByContractID(relatedObjectID);
			String[] RelatedsplitContractNum = RelatedObjectcontractNum
					.split("-");
			String[] RelatedsplitContractNum2 = RelatedsplitContractNum[0]
					.split("C");
			long RelatedobjectcontractID = Long
					.valueOf(RelatedsplitContractNum2[1]);

			RelatedObject relatedObject = new RelatedObject();
			try {
				relatedObject = relatedObjectDao
						.findRelatedObjectByObjIDTypeandRelatedObjIDType(
								objectcontractID, "Contract",
								RelatedobjectcontractID, "Contract");
			} catch (Exception e) {

			}
			relatedObject.setCreated(new Date());
			relatedObject.setObjectId(objectcontractID);
			relatedObject.setObjectType("Contract");
			relatedObject.setRelatedObjectId(RelatedobjectcontractID);
			relatedObject.setRelatedObjectType("Contract");
			relatedObject.setRetired(false);

			relatedObjectDao.saveOrUpdate(relatedObject);

		}

	}

	private String contractTypeMapping(String crimsonType) {
		String claraType = crimsonType;
		switch (claraType) {
		case "Grant Agreement":
			claraType = "grant-agreement";
			break;
		case "Grant Sub-Award":
			claraType = "grant-sub-award";
			break;
		case "Foundation":
			claraType = "foundation";
			break;
		case "Material Transfer Agreement (MTA)":
			claraType = "material-transfer-agreement";
			break;
		case "Confidential Disclosure Agreement (CDA)":
			claraType = "confidential-disclosure-agreement";
			break;
		case "Data Use Agreement (HIPAA)":
			claraType = "hipaa-data-use-agreement";
			break;
		case "Research Agreement":
			claraType = "research-agreement";
			break;
		case "Clinical Trial Agreement (CTA)":
			claraType = "clinical-trial-agreement";
			break;
		case "Consortium Master":
			claraType = "consortium-master";
			break;
		case "Master Agreement with Sponsor":
			claraType = "master-agreement-with-sponsor";
			break;
		case "CTA - Sub-contract":
			claraType = "cta-sub-contract";
			break;
		case "Other":
			claraType = "other";
			break;
		case "Master CDA (MCDA)":
			claraType = "master-cda";
			break;
		case "Master CTA (MCTA)":
			claraType = "master-cta";
			break;
		case "Cooperative Group Master":
			claraType = "cooperative-group";
			break;
		case "Cooperative Group CDA":
			claraType = "cooperative-group-cda";
			break;
		case "Cooperative Group CTA":
			claraType = "cooperative-group-cta";
			break;
		case "Consortium CDA":
			claraType = "consortium-cda";
			break;
		case "Consortium CTA":
			claraType = "consortium-cta";
			break;
		case "Licensing Agreement - UAMS Licensee":
			claraType = "uams-as-licensee";
			break;
		case "Licensing Agreement - UAMS Licensor":
			claraType = "uams-as-licensor";
			break;
		case "CTA - PI Initiated, Industry Supported":
			claraType = "pi-initiated-cta";
			break;
		case "CTA - Emergency Use":
			claraType = "emergency-use-cta";
			break;
		case "CTA - Compassionate Use":
			claraType = "compassionate-use-cta";
			break;
		default:
			claraType = "";
			break;
		}
		return claraType;

	}

	private String processLogInfo(BigInteger CrimsonContractID, long contractID) {
		List<Object[]> logsList = crimsonContractDao
				.findLogsByContractID(CrimsonContractID);
		Document loginfo = null;
		loginfo = xmlProcessor.newDocument();
		Element logsEle = loginfo.createElement("logs");
		loginfo.appendChild(logsEle);
		logsEle.setAttribute("object-id", String.valueOf(contractID));
		logsEle.setAttribute("object-type", "Contract");
		for (int i = 0; i < logsList.size(); i++) {
			Element logEle = loginfo.createElement("log");
			logEle.setAttribute("date-time", "");
			Object[] log = logsList.get(i);
			if (log[4] != null) {
				Date logDate = (Date) log[4];
				logEle.setAttribute("date-time", "" + logDate);

			}
			logEle.setAttribute("event-type", "");
			if (log[2] != null) {
				String logType = (String) log[2];
				logEle.setAttribute("event-type", logType);
			}
			if (log[3] != null) {
				String logMsg = (String) log[3];
				logEle.setTextContent(logMsg);
			}
			logEle.setAttribute("action-user-id", "");

			if (log[5] != null) {
				int userID = (int) log[5];
				Object[] user = crimsonStudyDao.findAriaUserByUserID(userID);
				if (user != null) {
					String userName = "";
					if (user[1] != null) {
						userName += (String) user[1] + " ";
					}
					if (user[2] != null) {
						userName += (String) user[2];
					}
					logEle.setAttribute("actor", userName);
					if (user[0] != null) {
						String sapIDStr = (String) user[0];

						// if sap is not null, search it in clara to get user id
						if (!sapIDStr.isEmpty()) {
							// convert sap in into clara format, remove 000 in
							// front
							int sapID = Integer.valueOf(sapIDStr);
							sapIDStr = String.valueOf(sapID);

							// if user existed, directly use the id
							if (!userDao.getUserBySAP(sapIDStr).isEmpty()) {
								logEle.setAttribute("action-user-id", ""
										+ userDao.getUserBySAP(sapIDStr).get(0)
												.getId());
							}
							// else create the user
							else {
								try {
									userServiceImpl.getAndUpdateUserBySap(
											sapIDStr, true);
									logEle.setAttribute("action-user-id", ""
											+ userDao.getUserBySAP(sapIDStr)
													.get(0).getId());
								} catch (Exception e) {
									// logger.debug("user not found in Ldap: "+sapIDStr);
								}
							}

						}

					}
				}
			}

			logsEle.appendChild(logEle);

		}

		String logXml = DomUtils
				.elementToString(loginfo, false, Encoding.UTF16);

		return logXml;
	}

	private String uploadContractDocumenttoFileServer(String path,
			BigInteger docID, String ext, long contractID,
			UploadedFile uploadedFile) {

		File fileDir = new File(localDirectory);
		if (!fileDir.exists())
			fileDir.mkdir();

		// copy the file from aria server to local
		String fileName = "X://" + path + docID + "." + ext;
		String hashFileName = "";
		String uploadfilename = "";
		try {
			messageDigest = MessageDigest.getInstance("SHA-256",
					new org.bouncycastle.jce.provider.BouncyCastleProvider());

			InputStream fileContent = new FileInputStream(fileName);
			byte[] bytes = IOUtils.toByteArray(fileContent);
			uploadedFile.setSize(bytes.length);
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
					sFTPService.uploadLocalFileToRemote("contract/"
							+ contractID + "/" + hashFileName + "." + ext);
					trySFTP = 0;
				} catch (Exception e) {
					if (trySFTP < 4) {
						trySFTP++;
					}
					if (trySFTP == 4) {
						e.printStackTrace();
						BufferedReader input = new BufferedReader(
								new FileReader(
										"C:\\Data\\ContractSFTPMissedList.txt"));
						String existData = "";
						String newData = "";
						while ((existData = input.readLine()) != null) {
							newData += existData + "\n";
						}
						input.close();
						newData += fileName;
						BufferedWriter output = new BufferedWriter(
								new FileWriter(
										"C:\\Data\\ContractSFTPMissedList.txt"));
						output.write(newData);
						output.close();
					}

				}
			}
			// delete the file after uploading...
			File fileForUploadedFile = new File(uploadfilename);
			fileForUploadedFile.delete();
		} catch (Exception e) {

		}

		// remove template dir
		fileDir.delete();

		// training
		return hashFileName;

		// production

		/*
		 * return "https://" + fileServerHost + "/files/contract/" + irbNum +
		 * "/" + hashFileName + "." + ext;
		 */

	}

	private void processDocument(BigInteger CrimsonContractID, long contractID,
			ContractFormXmlData contractFormXmlData) {
		List<Object[]> docsList = crimsonContractDao
				.findDocsByContractID(CrimsonContractID);
		Map<Integer, ContractFormXmlDataDocument> parentDocMap = new HashMap<Integer, ContractFormXmlDataDocument>();
		for (int i = 0; i < docsList.size(); i++) {
			Object[] docObj = docsList.get(i);
			UploadedFile uploadedFile = new UploadedFile();

			String path = "contract//contract_doc//";
			BigInteger docID = (BigInteger) docObj[0];
			String identifier = uploadContractDocumenttoFileServer(path, docID,
					(String) docObj[4], contractID, uploadedFile);

			if (uploadedFileDao.getUploadedFile(identifier, "/contract/"
					+ contractID + "/") != null) {
				uploadedFile = uploadedFileDao.getUploadedFile(identifier,
						"/contract/" + contractID + "/");
			}

			uploadedFile.setRetired(false);
			uploadedFile.setIdentifier(identifier);
			if (docObj[5] != null) {
				Date createdDate = (Date) docObj[5];
				uploadedFile.setCreated(createdDate);
			}

			if (docObj[3] != null) {
				String docName = (String) docObj[3];
				uploadedFile.setFilename(docName);
			}
			if (docObj[4] != null) {
				String ext = (String) docObj[4];
				uploadedFile.setExtension(ext);
			}
			uploadedFile.setPath("/contract/" + contractID + "/");
			uploadedFileDao.saveOrUpdate(uploadedFile);

			ContractFormXmlDataDocument contractFormXmlDataDocument = new ContractFormXmlDataDocument();
			if (contractFormXmlDataDocumentDao
					.getContractFormXmlDataDocumentByUploadedFileID(uploadedFile
							.getId()) != null) {
				contractFormXmlDataDocument = contractFormXmlDataDocumentDao
						.getContractFormXmlDataDocumentByUploadedFileID(uploadedFile
								.getId());
			}
			contractFormXmlDataDocument.setRetired(false);
			contractFormXmlDataDocument.setUploadedFile(uploadedFile);
			contractFormXmlDataDocument.setParent(contractFormXmlDataDocument);
			if (docObj[5] != null) {
				Date createdDate = (Date) docObj[5];
				contractFormXmlDataDocument.setCreated(createdDate);
			}
			if (docObj[2] != null) {
				String docTitle = (String) docObj[2];
				contractFormXmlDataDocument.setTitle(docTitle);
			}
			contractFormXmlDataDocument
					.setContractFormXmlData(contractFormXmlData);
			if (docObj[1] != null) {
				short typeID = (short) docObj[1];
				String docType = crimsonContractDao
						.getContractDocTypeByID(typeID);
				docType = docType.toLowerCase().trim();
				docType = docType.replace(" ", "-");
				contractFormXmlDataDocument.setCategory(docType);
			}

			contractFormXmlDataDocument.setStatus(Status.DRAFT);

			if (docObj[9] != null) {
				int userID = (int) docObj[9];
				Object[] user = crimsonStudyDao.findAriaUserByUserID(userID);
				if (user != null) {
					if (user[0] != null) {
						String sapIDStr = (String) user[0];

						// if sap is not null, search it in clara to get user id
						if (!sapIDStr.isEmpty()) {
							// convert sap in into clara format, remove 000 in
							// front
							int sapID = Integer.valueOf(sapIDStr);
							sapIDStr = String.valueOf(sapID);

							// if user existed, directly use the id
							if (!userDao.getUserBySAP(sapIDStr).isEmpty()) {
								contractFormXmlDataDocument.setUser(userDao
										.getUserBySAP(sapIDStr).get(0));
							}
							// else create the user
							else {
								try {
									userServiceImpl.getAndUpdateUserBySap(
											sapIDStr, true);
									contractFormXmlDataDocument.setUser(userDao
											.getUserBySAP(sapIDStr).get(0));
								} catch (Exception e) {
									// logger.debug("user not found in Ldap: "+sapIDStr);
								}
							}

						}

					}
				}
			}
			if (docObj[7] != null) {
				short version = (short) docObj[7];
				contractFormXmlDataDocument.setVersionId(version);
				if (version > 0) {
					contractFormXmlDataDocument.setParent(parentDocMap
							.get((Integer) docObj[8]));
				}
				if (version == 0) {
					parentDocMap.put((Integer) docObj[8],
							contractFormXmlDataDocument);
					// parentDoc = contractFormXmlDataDocument;
				}

			}

			contractFormXmlDataDocumentDao
					.saveOrUpdate(contractFormXmlDataDocument);

		}

	}

	private ContractFormCommitteeStatusEnum getCommitteeStatusForClara(String action, String role){
		String combinedAction=action+" "+role;
		ContractFormCommitteeStatusEnum contractFormCommitteeStatusEnum=null;
		switch (combinedAction) {
		/*case "PENDING_LEGAL_REVIEW"+" "+"Contract Administrator":
			contractFormCommitteeStatusEnum = ContractFormCommitteeStatusEnum.APPROVED;
			break;*/
		case "PENDING_LEGAL_REVIEW"+" "+"Legal":
			contractFormCommitteeStatusEnum = ContractFormCommitteeStatusEnum.IN_REVIEW;
			break;
		case "PENDING_CONTRACT_ADMIN_FINAL_REVIEW"+" "+"Contract Administrator":
			contractFormCommitteeStatusEnum = ContractFormCommitteeStatusEnum.FINAL_LEGAL_APPROVAL;
			break;
		/*case "PENDING_CONTRACT_ADMIN_FINAL_REVIEW"+" "+"Legal":
			contractFormCommitteeStatusEnum = ContractFormCommitteeStatusEnum.APPROVED;
			break;*/
		case "PENDING_ASSIGN_REVIEWERS"+" "+"Contract Manager":
			contractFormCommitteeStatusEnum = ContractFormCommitteeStatusEnum.PENDING_REVIEWER_ASSIGNMENT;
			break;
		case "PENDING_SIGNATURES"+" "+"Contract Administrator":
			contractFormCommitteeStatusEnum = ContractFormCommitteeStatusEnum.PENDING_SIGNATURE;
			break;
		case "PENDING_CONTRACT_ADMIN_REVIEW"+" "+"Contract Administrator":
			contractFormCommitteeStatusEnum = ContractFormCommitteeStatusEnum.IN_REVIEW;
			break;
		case "RESTRICTED_CONTRACT_EXECUTED"+" "+"Contract Administrator":
			contractFormCommitteeStatusEnum = ContractFormCommitteeStatusEnum.CONTRACT_EXECUTED_PENDING_DOCUMENTS;
			break;
	/*	case "REVIEWER_ASSIGNED"+" "+"Contract Administrator":
			contractFormCommitteeStatusEnum = ContractFormCommitteeStatusEnum.IN_REVIEW;
			break;*/
		case "REVIEWER_ASSIGNED"+" "+"Contract Manager":
			contractFormCommitteeStatusEnum = ContractFormCommitteeStatusEnum.REVIEWER_ASSIGNED;
			break;
		/*case "ROUTED_TO_LEGAL"+" "+"Contract Administrator":
			contractFormCommitteeStatusEnum = ContractFormCommitteeStatusEnum.APPROVED;
			break;*/
		case "ROUTED_TO_LEGAL"+" "+"Legal":
			contractFormCommitteeStatusEnum =ContractFormCommitteeStatusEnum.IN_REVIEW;
			break;
		case "CONTRACT_EXECUTED"+" "+"Contract Administrator":
			contractFormCommitteeStatusEnum =ContractFormCommitteeStatusEnum.COMPLETED;
			break;
		/*case "LEGAL_FINAL_APPROVAL_AND_ROUTE_TO_CONTRACT_ADMIN"+" "+"Contract Administrator":
			contractFormCommitteeStatusEnum =ContractFormCommitteeStatusEnum.FINAL_LEGAL_APPROVAL;
			break;*/
		case "LEGAL_FINAL_APPROVAL_AND_ROUTE_TO_CONTRACT_ADMIN"+" "+"Legal":
			contractFormCommitteeStatusEnum =ContractFormCommitteeStatusEnum.APPROVED;
			break;
		/*case "READY_FOR_SIGNATURES"+" "+"Contract Administrator":
			contractFormCommitteeStatusEnum =ContractFormCommitteeStatusEnum.PENDING_SIGNATURE;
			break;
		case "ROUTED_TO_CONTRACT_ADMIN"+" "+"Contract Administrator":
			contractFormCommitteeStatusEnum =ContractFormCommitteeStatusEnum.IN_REVIEW;
			break;*/
		case "ROUTED_TO_CONTRACT_ADMIN"+" "+"Legal":
			contractFormCommitteeStatusEnum =ContractFormCommitteeStatusEnum.COMPLETED;
			break;
		case "REVISED_AND_ROUTED_TO_LEGAL"+" "+"Contract Administrator":
			contractFormCommitteeStatusEnum =ContractFormCommitteeStatusEnum.COMPLETED;
			break;
		/*case "REVISED_AND_ROUTED_TO_LEGAL"+" "+"Legal":
			contractFormCommitteeStatusEnum =ContractFormCommitteeStatusEnum.IN_REVIEW;
			break;*/
		case "CONTRACT_SUBMITTED"+" "+"Contract Manager":
			contractFormCommitteeStatusEnum =ContractFormCommitteeStatusEnum.IN_REVIEW;
			break;
		case "REVISED_AND_ROUTED_TO_CONTRACT_ADMIN"+" "+"Legal":
			contractFormCommitteeStatusEnum =ContractFormCommitteeStatusEnum.COMPLETED;
			break;
		case "APPROVAL_WITHDRAWN"+" "+"Contract Administrator":
			contractFormCommitteeStatusEnum =ContractFormCommitteeStatusEnum.COMPLETED;
			break;
		default:
			contractFormCommitteeStatusEnum = null;
			break;
		}
		return contractFormCommitteeStatusEnum;
	}

	private Committee getCausedByCommitteeForClara(String committeeStr){
		Committee committee =null;
		switch (committeeStr) {
		case "Contract Administrator":
			committee =Committee.CONTRACT_ADMIN;
			break;
		case "Legal":
			committee =Committee.CONTRACT_LEGAL_REVIEW;
			break;
		case "Principal Investigator":
			committee =Committee.PI;
			break;
		case "Contract Manager":
			committee =Committee.CONTRACT_MANAGER;
			break;
		default:
			committee = null;
			break;
		}
		return committee;
	}

	private void processCommitteeStatus(BigInteger crimsonContractID,
			long contractID, ContractForm contractForm) {
		List<Object[]> contractReportList = crimsonContractDao
				.getCommitteeStatusByContractID(crimsonContractID);
		Map<String,Committee> causedCommitteeMap = new HashMap<String,Committee>();
		causedCommitteeMap.put("PENDING_LEGAL_REVIEW", Committee.CONTRACT_ADMIN);
		causedCommitteeMap.put("PENDING_CONTRACT_ADMIN_FINAL_REVIEW", Committee.CONTRACT_LEGAL_REVIEW);
		causedCommitteeMap.put("PENDING_ASSIGN_REVIEWERS", Committee.PI);
		causedCommitteeMap.put("PENDING_SIGNATURES", Committee.CONTRACT_ADMIN);
		causedCommitteeMap.put("PENDING_CONTRACT_ADMIN_REVIEW", Committee.CONTRACT_LEGAL_REVIEW);
		causedCommitteeMap.put("RESTRICTED_CONTRACT_EXECUTED", Committee.CONTRACT_ADMIN);
		causedCommitteeMap.put("REVIEWER_ASSIGNED", Committee.CONTRACT_MANAGER);
		causedCommitteeMap.put("ROUTED_TO_LEGAL", Committee.CONTRACT_ADMIN);
		causedCommitteeMap.put("CONTRACT_EXECUTED", Committee.CONTRACT_ADMIN);
		causedCommitteeMap.put("LEGAL_FINAL_APPROVAL_AND_ROUTE_TO_CONTRACT_ADMIN", Committee.CONTRACT_LEGAL_REVIEW);
		causedCommitteeMap.put("READY_FOR_SIGNATURES", Committee.CONTRACT_ADMIN);
		causedCommitteeMap.put("ROUTED_TO_CONTRACT_ADMIN", Committee.CONTRACT_LEGAL_REVIEW);
		causedCommitteeMap.put("REVISED_AND_ROUTED_TO_LEGAL", Committee.CONTRACT_ADMIN);
		causedCommitteeMap.put("CONTRACT_SUBMITTED", Committee.PI);
		causedCommitteeMap.put("REVISED_AND_ROUTED_TO_CONTRACT_ADMIN", Committee.CONTRACT_LEGAL_REVIEW);
		causedCommitteeMap.put("APPROVAL_WITHDRAWN", Committee.CONTRACT_LEGAL_REVIEW);

		for (int i = 0; i < contractReportList.size(); i++) {
			Object[] contractReportObje = contractReportList.get(i);
			// for start action
			Committee committee=null;
			ContractFormCommitteeStatusEnum contractFormCommitteeStatusEnum = null;
			String role = "";
			if(contractReportObje[1]!=null){
				int groupID  = (int)contractReportObje[1];
				role = crimsonContractDao.getGroupNameByID(groupID).trim();

				if(role.equals("Contract Manager")||role.equals("Legal")||role.equals("Contract Administrator")){
					committee = getCausedByCommitteeForClara(role);

			ContractFormCommitteeStatus contractFormCommitteeStatus = new ContractFormCommitteeStatus();

			if(contractReportObje[2]!=null){
				contractFormCommitteeStatusEnum = getCommitteeStatusForClara((String)contractReportObje[2],  role);
				contractFormCommitteeStatus.setContractFormCommitteeStatus(contractFormCommitteeStatusEnum);
			}
			if(contractFormCommitteeStatusEnum!=null){
			contractFormCommitteeStatus.setRetired(false);
			contractFormCommitteeStatus.setContractForm(contractForm);
			contractFormCommitteeStatus.setCommittee(committee);
			contractFormCommitteeStatus.setCausedByCommittee(causedCommitteeMap.get((String)contractReportObje[2]));


			if (contractReportObje[3] != null) {
				contractFormCommitteeStatus
						.setModified((Date) contractReportObje[3]);
			}
			if (contractReportObje[4] != null) {
				int userID = (Integer) contractReportObje[4];
				Object[] user = crimsonStudyDao.findAriaUserByUserID(userID);
				if (user != null) {
					if (user[0] != null) {
						String sapIDStr = (String) user[0];

						// if sap is not null, search it in clara to get user id
						if (!sapIDStr.isEmpty()) {
							// convert sap in into clara format, remove 000 in
							// front
							int sapID = Integer.valueOf(sapIDStr);
							sapIDStr = String.valueOf(sapID);

							// if user existed, directly use the id
							if (!userDao.getUserBySAP(sapIDStr).isEmpty()) {
								contractFormCommitteeStatus
										.setCauseByUser(userDao.getUserBySAP(
												sapIDStr).get(0));
							}
							// else create the user
							else {
								try {
									userServiceImpl.getAndUpdateUserBySap(
											sapIDStr, true);
									contractFormCommitteeStatus
											.setCauseByUser(userDao
													.getUserBySAP(sapIDStr)
													.get(0));
								} catch (Exception e) {
									// logger.debug("user not found in Ldap: "+sapIDStr);
								}
							}

						}

					}
				}
			}

			contractFormCommitteeStatusDao.saveOrUpdate(contractFormCommitteeStatus);

				}
				}
			}
			// for stop action
			ContractFormCommitteeStatus contractFormCommitteeStatusForStop = new ContractFormCommitteeStatus();
			contractFormCommitteeStatusForStop.setRetired(false);
			contractFormCommitteeStatusForStop.setContractForm(contractForm);

			contractFormCommitteeStatusForStop.setCommittee(committee);
			contractFormCommitteeStatusEnum=null;
			if(contractReportObje[5]!=null){
				contractFormCommitteeStatusEnum = getCommitteeStatusForClara((String)contractReportObje[5], role);
				contractFormCommitteeStatusForStop.setContractFormCommitteeStatus(contractFormCommitteeStatusEnum);
				contractFormCommitteeStatusForStop.setCausedByCommittee(causedCommitteeMap.get((String)contractReportObje[5]));
			}
			if(contractFormCommitteeStatusEnum!=null){
			if (contractReportObje[6] != null) {
				contractFormCommitteeStatusForStop
						.setModified((Date) contractReportObje[6]);
			}
			if (contractReportObje[8] != null) {
				int userID = (Integer) contractReportObje[8];
				Object[] user = crimsonStudyDao.findAriaUserByUserID(userID);
				if (user != null) {
					if (user[0] != null) {
						String sapIDStr = (String) user[0];

						// if sap is not null, search it in clara to get user id
						if (!sapIDStr.isEmpty()) {
							// convert sap in into clara format, remove 000 in
							// front
							int sapID = Integer.valueOf(sapIDStr);
							sapIDStr = String.valueOf(sapID);

							// if user existed, directly use the id
							if (!userDao.getUserBySAP(sapIDStr).isEmpty()) {
								contractFormCommitteeStatusForStop
										.setCauseByUser(userDao.getUserBySAP(
												sapIDStr).get(0));
							}
							// else create the user
							else {
								try {
									userServiceImpl.getAndUpdateUserBySap(
											sapIDStr, true);
									contractFormCommitteeStatusForStop
											.setCauseByUser(userDao
													.getUserBySAP(sapIDStr)
													.get(0));
								} catch (Exception e) {
									// logger.debug("user not found in Ldap: "+sapIDStr);
								}
							}

						}

					}
				}
			}
			contractFormCommitteeStatusDao.saveOrUpdate(contractFormCommitteeStatusForStop);
			}
		}

	}

	@Override
	public void migrateContract() {
		// get all contracts from crimson
		List<Object[]> crimsonContracts = crimsonContractDao.findAllContract();
		// process each contract
/*
		for (int i = crimsonContracts.size() - 1; i > -1; i--) {
			Object[] crimsonObject = crimsonContracts.get(i);
*/
			// this two lines for test


			  for (int i = 0; i < 1; i++) { Object[] crimsonObject =
			 crimsonContractDao.findContractByID(1812);


			BigInteger crimsonContractID = (BigInteger) crimsonObject[0];
			logger.debug("Processing... " + crimsonContractID);
			try {
				Object[] contractInfo = crimsonContractDao
						.findContractInfoByContractId(crimsonContractID);
				Document doc = xmlProcessor.getDocumentBuilder().newDocument();
				processBasicInfoForContract(doc, crimsonObject);
				// processFundingInfoForContract(doc, crimsonContracts.get(i));
				int parentID = (Integer) crimsonObject[6];
				// parent id =0, it is a contract

				String contractNum = (String) crimsonObject[1];
				// get contract id for clara from contract number
				String[] splitContractNum = contractNum.split("-");
				String[] splitContractNum2 = splitContractNum[0].split("C");
				long contractID = Long.valueOf(splitContractNum2[1]);

				Date createdDate = (Date) crimsonObject[3];

				Document documentForContract = doc;

				Element statusEle = (Element) documentForContract
						.getElementsByTagName("status").item(0);
				String statusSTR = statusEle.getTextContent();
				statusEle.setTextContent("");
				String contractOnlyMetaData = DomUtils.elementToString(
						documentForContract, false, Encoding.UTF16);
				statusEle.setTextContent(statusSTR);
				// logger.debug(contractMetaData);

				int protocolID = 0;
				if (contractInfo[4] != null) {
					protocolID = (int) contractInfo[4];
				}
				// protocl may not existed in clara
				try {
					protocolDao.findById(protocolID);
					if (protocolDao.findById(protocolID) == null) {
						protocolID = 0;
					}
				} catch (Exception e) {
					protocolID = 0;
				}
				// save contract status
				Contract contract = new Contract();
				contract.setId(contractID);
				if (parentID == 0) {
					contractDao.disableIdentyInsert(contractID, 0, createdDate,
							contractOnlyMetaData, protocolID, contractNum);

					ContractStatus contractStatus = new ContractStatus();
					try {
						contractStatus = contractStatusDao
								.findContractStatusByContractId(contractID);
					} catch (Exception e) {

					}
					contractStatus.setRetired(false);
					contractStatus.setContract(contract);
					contractStatus.setContractStatus(null);
					contractStatus
							.setContractStatus(getStatusOnly(crimsonObject));
					contractStatus.setModified(createdDate);
					contractStatus.setCauseByUser(userDao.findById(73));
					contractStatus.setCausedByCommittee(Committee.PI);

					contractStatusDao.saveOrUpdate(contractStatus);

				}

				// add funding info to the doc info
				Document contractFormDoc = processFundingInfoForContract(doc,
						crimsonObject);
				String contractFormXmldata = DomUtils.elementToString(
						contractFormDoc, false, Encoding.UTF16);
				// save contractform
				ContractForm contractForm = new ContractForm();
				try {
					if (parentID == 0)
						contractForm = contractFormDao
								.getContractFormByContractIdAndContractFormType(
										contractID,
										ContractFormType.NEW_CONTRACT);
					else
						contractForm = contractFormDao
								.getContractFormByContractIdAndContractFormType(
										contractID, ContractFormType.AMENDMENT);
				} catch (Exception e) {

				}
				contractForm.setRetired(false);
				contractForm.setContract(contract);
				contractForm.setCreated(createdDate);
				contractForm.setLocked(false);
				contractForm.setMetaDataXml(contractFormXmldata);
				contractForm.setParent(contractForm);
				if (parentID == 0)
					contractForm
							.setContractFormType(ContractFormType.NEW_CONTRACT);
				else
					contractForm
							.setContractFormType(ContractFormType.AMENDMENT);

				contractFormDao.saveOrUpdate(contractForm);

				// process contractFormStatus
				ContractFormStatus contractFormStatus = new ContractFormStatus();
				try {
					contractFormStatus = contractFormStatusDao
							.getContractFormStatusByFormId(contractForm
									.getFormId());
				} catch (Exception e) {
				}
				// e.printStackTrace();
				contractFormStatus.setCauseByUser(userDao.findById(73));
				contractFormStatus.setCausedByCommittee(Committee.PI);
				contractFormStatus.setContractForm(contractForm);
				contractFormStatus.setModified(createdDate);
				contractFormStatus.setRetired(false);
				contractFormStatus
						.setContractFormStatus(getFormStatusOnly(crimsonObject));

				contractFormStatusDao.saveOrUpdate(contractFormStatus);

				// process contract_form_xml_data
				ContractFormXmlData contractFormXmlData = new ContractFormXmlData();
				try {
					if (parentID == 0)
						contractFormXmlData = contractFormXmlDataDao
								.getLastContractFormXmlDataByContractFormIdAndType(
										contractForm.getFormId(),
										ContractFormXmlDataType.CONTRACT);
					else
						contractFormXmlData = contractFormXmlDataDao
								.getLastContractFormXmlDataByContractFormIdAndType(
										contractForm.getFormId(),
										ContractFormXmlDataType.AMENDMENT);
				} catch (Exception e) {
				}
				contractFormXmlData.setContractForm(contractForm);
				if (parentID == 0)
					contractFormXmlData
							.setContractFormXmlDataType(ContractFormXmlDataType.CONTRACT);
				else
					contractFormXmlData
							.setContractFormXmlDataType(ContractFormXmlDataType.AMENDMENT);
				contractFormXmlData.setCreated(createdDate);
				contractFormXmlData.setParent(contractFormXmlData);
				contractFormXmlData.setRetired(false);
				contractFormXmlData.setXmlData(contractFormXmldata);

				contractFormXmlDataDao.saveOrUpdate(contractFormXmlData);

				// process Document
				processDocument(crimsonContractID, contractID,
						contractFormXmlData);

				// process commitee status
				 processCommitteeStatus (crimsonContractID,
						 contractID, contractForm);
				// save track history
				Track track = null;
				try {
					track = trackDao.getTrackByTypeAndRefObjectID("CONTRACT",
							contractID);
				} catch (Exception e) {
					// e.printStackTrace();
					track = null;
				}
				if (track == null) {
					track = new Track();
				}
				Date logDate = new Date();
				track.setModified(logDate);
				track.setRetired(false);
				track.setType("CONTRACT");
				track.setRefObjectClass(Contract.class);
				track.setRefObjectId(contractID);
				String logXml = processLogInfo(crimsonContractID, contractID);

				track.setXmlData(logXml);

				trackDao.saveOrUpdate(track);
			} catch (Exception e) {
				logger.debug("error: " + crimsonContractID);
				e.printStackTrace();
			}
		}

		// process related contract info
		processRelatedContracts();

	}

	public XmlProcessor getXmlProcessor() {
		return xmlProcessor;
	}

	@Autowired(required = true)
	public void setXmlProcessor(XmlProcessor xmlProcessor) {
		this.xmlProcessor = xmlProcessor;
	}

	public CrimsonContractDao getCrimsonContractDao() {
		return crimsonContractDao;
	}

	@Autowired(required = true)
	public void setCrimsonContractDao(CrimsonContractDao crimsonContractDao) {
		this.crimsonContractDao = crimsonContractDao;
	}

	public UserDao getUserDao() {
		return userDao;
	}

	@Autowired(required = true)
	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
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

	public ContractDao getContractDao() {
		return contractDao;
	}

	@Autowired(required = true)
	public void setContractDao(ContractDao contractDao) {
		this.contractDao = contractDao;
	}

	public ContractStatusDao getContractStatusDao() {
		return contractStatusDao;
	}

	@Autowired(required = true)
	public void setContractStatusDao(ContractStatusDao contractStatusDao) {
		this.contractStatusDao = contractStatusDao;
	}

	public ContractFormDao getContractFormDao() {
		return contractFormDao;
	}

	@Autowired(required = true)
	public void setContractFormDao(ContractFormDao contractFormDao) {
		this.contractFormDao = contractFormDao;
	}

	public ContractFormStatusDao getContractFormStatusDao() {
		return contractFormStatusDao;
	}

	@Autowired(required = true)
	public void setContractFormStatusDao(
			ContractFormStatusDao contractFormStatusDao) {
		this.contractFormStatusDao = contractFormStatusDao;
	}

	public TrackDao getTrackDao() {
		return trackDao;
	}

	@Autowired(required = true)
	public void setTrackDao(TrackDao trackDao) {
		this.trackDao = trackDao;
	}

	public ContractFormXmlDataDao getContractFormXmlDataDao() {
		return contractFormXmlDataDao;
	}

	@Autowired(required = true)
	public void setContractFormXmlDataDao(
			ContractFormXmlDataDao contractFormXmlDataDao) {
		this.contractFormXmlDataDao = contractFormXmlDataDao;
	}

	public RelatedObjectDao getRelatedObjectDao() {
		return relatedObjectDao;
	}

	@Autowired(required = true)
	public void setRelatedObjectDao(RelatedObjectDao relatedObjectDao) {
		this.relatedObjectDao = relatedObjectDao;
	}

	public CrimsonStudyDao getcrimsonStudyDao() {
		return crimsonStudyDao;
	}

	@Autowired(required = true)
	public void setcrimsonStudyDao(
			CrimsonStudyDao crimsonStudyDao) {
		this.crimsonStudyDao = crimsonStudyDao;
	}

	public UploadedFileDao getUploadedFileDao() {
		return uploadedFileDao;
	}

	@Autowired(required = true)
	public void setUploadedFileDao(UploadedFileDao uploadedFileDao) {
		this.uploadedFileDao = uploadedFileDao;
	}

	public ContractFormXmlDataDocumentDao getContractFormXmlDataDocumentDao() {
		return contractFormXmlDataDocumentDao;
	}

	@Autowired(required = true)
	public void setContractFormXmlDataDocumentDao(
			ContractFormXmlDataDocumentDao contractFormXmlDataDocumentDao) {
		this.contractFormXmlDataDocumentDao = contractFormXmlDataDocumentDao;
	}

	public String getLocalDirectory() {
		return localDirectory;
	}

	public void setLocalDirectory(String localDirectory) {
		this.localDirectory = localDirectory;
	}

	public SFTPService getsFTPService() {
		return sFTPService;
	}

	@Autowired(required = true)
	public void setsFTPService(SFTPService sFTPService) {
		this.sFTPService = sFTPService;
	}

	public ProtocolDao getProtocolDao() {
		return protocolDao;
	}

	@Autowired(required = true)
	public void setProtocolDao(ProtocolDao protocolDao) {
		this.protocolDao = protocolDao;
	}

	public ContractFormCommitteeStatusDao getContractFormCommitteeStatusDao() {
		return contractFormCommitteeStatusDao;
	}

	@Autowired(required = true)
	public void setContractFormCommitteeStatusDao(
			ContractFormCommitteeStatusDao contractFormCommitteeStatusDao) {
		this.contractFormCommitteeStatusDao = contractFormCommitteeStatusDao;
	}

}
