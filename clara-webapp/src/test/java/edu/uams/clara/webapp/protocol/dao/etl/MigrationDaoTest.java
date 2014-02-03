package edu.uams.clara.webapp.protocol.dao.etl;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import edu.uams.clara.core.util.xml.DomUtils;
import edu.uams.clara.core.util.xml.XmlHandler;
import edu.uams.clara.webapp.common.dao.history.TrackDao;
import edu.uams.clara.webapp.common.dao.security.acl.SecurableObjectAclDao;
import edu.uams.clara.webapp.common.dao.security.acl.SecurableObjectDao;
import edu.uams.clara.webapp.common.dao.usercontext.PersonDao;
import edu.uams.clara.webapp.common.dao.usercontext.UserDao;
import edu.uams.clara.webapp.common.domain.history.Track;
import edu.uams.clara.webapp.common.domain.security.acl.SecurableObject;
import edu.uams.clara.webapp.common.domain.security.acl.SecurableObjectAcl;
import edu.uams.clara.webapp.common.domain.security.acl.enums.Permission;
import edu.uams.clara.webapp.common.domain.usercontext.Person;
import edu.uams.clara.webapp.common.domain.usercontext.User;
import edu.uams.clara.webapp.common.domain.usercontext.enums.Committee;
import edu.uams.clara.webapp.common.security.ObjectAclService;
import edu.uams.clara.webapp.common.util.DateFormatUtil;
import edu.uams.clara.webapp.contract.dao.ContractDao;
import edu.uams.clara.webapp.contract.dao.contractform.ContractFormDao;
import edu.uams.clara.webapp.contract.dao.contractform.ContractFormXmlDataDao;
import edu.uams.clara.webapp.contract.domain.Contract;
import edu.uams.clara.webapp.contract.domain.contractform.ContractForm;
import edu.uams.clara.webapp.contract.domain.contractform.ContractFormXmlData;
import edu.uams.clara.webapp.protocol.dao.ProtocolDao;
import edu.uams.clara.webapp.protocol.dao.businesslogicobject.ProtocolFormCommitteeStatusDao;
import edu.uams.clara.webapp.protocol.dao.businesslogicobject.ProtocolFormStatusDao;
import edu.uams.clara.webapp.protocol.dao.businesslogicobject.ProtocolStatusDao;
import edu.uams.clara.webapp.protocol.dao.protocolform.ProtocolFormDao;
import edu.uams.clara.webapp.protocol.dao.protocolform.ProtocolFormXmlDataDao;
import edu.uams.clara.webapp.protocol.dao.protocolform.ProtocolFormXmlDataDocumentDao;
import edu.uams.clara.webapp.protocol.domain.Protocol;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.ProtocolFormCommitteeStatus;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.ProtocolFormStatus;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.ProtocolStatus;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.enums.ProtocolFormStatusEnum;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolForm;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolFormXmlData;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolFormXmlDataDocument;
import edu.uams.clara.webapp.protocol.domain.protocolform.enums.ProtocolFormType;
import edu.uams.clara.webapp.protocol.domain.protocolform.enums.ProtocolFormXmlDataType;
import edu.uams.clara.webapp.xml.processor.XmlProcessor;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "file:src/test/java/edu/uams/clara/webapp/protocol/dao/etl/MigrationDaoTest-context.xml" })
public class MigrationDaoTest {
	private final static Logger logger = LoggerFactory
			.getLogger(MigrationDaoTest.class);

	private MiagrationDao miagrationDao;

	private ProtocolDao protocolDao;

	private ProtocolFormDao protocolFormDao;

	private ProtocolFormXmlDataDao protocolFormXmlDataDao;

	private ProtocolFormXmlDataDocumentDao protocolFormXmlDataDocumentDao;

	private ProtocolFormCommitteeStatusDao protocolFormCommitteeStatusDao;

	private ProtocolStatusDao protocolStatusDao;

	private ProtocolFormStatusDao protocolFormStatusDao;

	private SecurableObjectDao securableObjectDao;

	private SecurableObjectAclDao securableObjectAclDao;

	private TrackDao trackDao;

	private UserDao userDao;

	private PersonDao personDao;

	private XmlProcessor xmlProcessor;

	private XmlHandler xmlHandler;

	private ObjectAclService objectAclService;

	private ContractDao contractDao;

	private ContractFormDao contractFormDao;

	private ContractFormXmlDataDao contractFormXmlDataDao;

	//@Test
	public void testFindProtocolById() {
		try {
			User user = userDao.findById(68l);
			long protocolId = 131753l;
			Protocol p = miagrationDao.findProtocolById(protocolId);

			Protocol protocol = new Protocol();

			protocol.setCreated(p.getCreated());
			protocol.setLocked(p.isLocked());
			protocol.setMetaDataXml(p.getMetaDataXml());
			protocol.setProtocolIdentifier(p.getProtocolIdentifier());
			protocol.setRetired(p.isRetired());

			protocol = protocolDao.saveOrUpdate(protocol);

			String protocolMeta = protocol.getMetaDataXml();
			Document protocolMetaDoc = xmlProcessor
					.loadXmlStringToDOM(protocolMeta);

			Element protocolEl = protocolMetaDoc.getDocumentElement();

			protocolEl.setAttribute("id", String.valueOf(protocol.getId()));

			protocolEl.setAttribute("identifier",
					String.valueOf(protocol.getId()));

			protocolMeta = DomUtils.elementToString(protocolEl);

			protocol.setMetaDataXml(protocolMeta);
			protocol = protocolDao.saveOrUpdate(protocol);

			logger.info("id: " + protocol.getId());

			List<ProtocolStatus> psLst = miagrationDao
					.findProtocolStatusByProtocolId(protocolId);
			logger.debug("p status size: " + psLst.size());
			for (ProtocolStatus ps : psLst) {
				ProtocolStatus protocolStatus = new ProtocolStatus();
				protocolStatus.setCauseByUser(ps.getCauseByUser());
				protocolStatus.setCausedByCommittee(ps.getCausedByCommittee());
				protocolStatus.setModified(ps.getModified());
				protocolStatus.setNote(ps.getNote());
				protocolStatus.setProtocol(protocol);
				protocolStatus.setProtocolStatus(ps.getProtocolStatus());
				protocolStatus.setRetired(ps.isRetired());

				protocolStatusDao.saveOrUpdate(protocolStatus);
			}
			/*
			try {
				SecurableObject so = miagrationDao
						.findSecurableObjectByProtocolId(protocolId);
				SecurableObject securableObject = new SecurableObject();
				securableObject.setObjectClass(so.getClass());
				securableObject.setObjectId(protocol.getId());
				securableObject.setObjectIdExpression(so
						.getObjectIdExpression());
				securableObject.setRetired(so.isRetired());

				securableObject = securableObjectDao
						.saveOrUpdate(securableObject);

				List<SecurableObjectAcl> soaLst = miagrationDao
						.findSecurableObjectAclByProtocolId(protocolId);
				for (SecurableObjectAcl soa : soaLst) {
					SecurableObjectAcl securableObjectAcl = new SecurableObjectAcl();
					securableObjectAcl.setOwnerClass(soa.getOwnerClass());
					securableObjectAcl.setOwnerId(soa.getOwnerId());
					securableObjectAcl.setPermission(soa.getPermission());
					securableObjectAcl.setRetired(soa.isRetired());
					securableObjectAcl.setSecurableObject(securableObject);

					securableObjectAclDao.saveOrUpdate(securableObjectAcl);
				}
			} catch (Exception e) {

			}
			*/
			try {
				List<ProtocolForm> pfLst = miagrationDao
						.findProtocolFormByProtocolId(protocolId);
				logger.debug("pf size: " + pfLst.size());
				for (ProtocolForm pf : pfLst) {
					ProtocolForm protocolForm = new ProtocolForm();
					protocolForm.setCreated(pf.getCreated());
					protocolForm.setLocked(pf.isLocked());
					protocolForm.setMetaDataXml(pf.getMetaDataXml());
					protocolForm.setParent(protocolForm);
					protocolForm.setProtocol(protocol);
					protocolForm.setProtocolFormType(pf.getProtocolFormType());
					protocolForm.setRetired(pf.isRetired());

					protocolForm = protocolFormDao.saveOrUpdate(protocolForm);

					List<ProtocolFormStatus> pfsLst = miagrationDao
							.findProtocolFormStatusByProtocolId(protocolId);
					logger.debug("pf status size: " + pfsLst.size());
					for (ProtocolFormStatus pfs : pfsLst) {
						ProtocolFormStatus protocolFormStatus = new ProtocolFormStatus();
						protocolFormStatus.setCauseByUser(user);
						protocolFormStatus.setCausedByCommittee(pfs
								.getCausedByCommittee());
						protocolFormStatus.setModified(pfs.getModified());
						protocolFormStatus.setNote(pfs.getNote());
						protocolFormStatus.setProtocolForm(protocolForm);
						protocolFormStatus.setProtocolFormStatus(pfs
								.getProtocolFormStatus());
						protocolFormStatus.setRetired(pfs.isRetired());

						protocolFormStatus = protocolFormStatusDao.saveOrUpdate(protocolFormStatus);
					}

					List<ProtocolFormCommitteeStatus> pfcsList = miagrationDao
							.findProtocolFormCommitteeStatusByProtocolId(protocolId);
					logger.debug("pf committee status size: " + pfcsList.size());
					if (pfcsList != null && !pfcsList.isEmpty()) {
						for (ProtocolFormCommitteeStatus pfcs : pfcsList) {
							ProtocolFormCommitteeStatus protocolFormCommitteeStatus = new ProtocolFormCommitteeStatus();
							protocolFormCommitteeStatus.setAction(pfcs.getAction());
							protocolFormCommitteeStatus.setCauseByUser(user);
							protocolFormCommitteeStatus.setCausedByCommittee(pfcs
									.getCausedByCommittee());
							protocolFormCommitteeStatus.setCommittee(pfcs
									.getCommittee());
							protocolFormCommitteeStatus.setModified(pfcs
									.getModified());
							protocolFormCommitteeStatus.setNote(pfcs.getNote());
							protocolFormCommitteeStatus
									.setProtocolForm(protocolForm);
							protocolFormCommitteeStatus
									.setProtocolFormCommitteeStatus(pfcs
											.getProtocolFormCommitteeStatus());
							protocolFormCommitteeStatus
									.setRetired(pfcs.isRetired());
							protocolFormCommitteeStatus.setXmlData(pfcs
									.getXmlData());

							protocolFormCommitteeStatus = protocolFormCommitteeStatusDao
									.saveOrUpdate(protocolFormCommitteeStatus);
						}
					}

					List<ProtocolFormXmlData> pfxdLst = miagrationDao
							.findProtocolFormXmlDataByProtocolId(protocolId);
					logger.debug("pf xml datas size: " + pfxdLst.size());
					for (ProtocolFormXmlData pfxd : pfxdLst) {
						ProtocolFormXmlData protocolFormXmlData = new ProtocolFormXmlData();
						protocolFormXmlData.setCreated(pfxd.getCreated());
						protocolFormXmlData.setParent(protocolFormXmlData);
						protocolFormXmlData.setProtocolForm(protocolForm);
						protocolFormXmlData.setProtocolFormXmlDataType(pfxd
								.getProtocolFormXmlDataType());
						protocolFormXmlData.setRetired(pfxd.isRetired());
						protocolFormXmlData.setXmlData(pfxd.getXmlData());

						protocolFormXmlData = protocolFormXmlDataDao
								.saveOrUpdate(protocolFormXmlData);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		// logger.debug("meta data: " + p.getMetaDataXml());
	}

	// @Test
	public void updateProtocolFormStatus() {
		ProtocolForm protocolForm = protocolFormDao.findById(13252l);

		ProtocolFormStatus protocolFormStatus = new ProtocolFormStatus();
		protocolFormStatus.setCauseByUser(userDao.findById(68l));
		protocolFormStatus.setCausedByCommittee(Committee.IRB_REVIEWER);
		protocolFormStatus.setModified(new Date());
		protocolFormStatus.setNote("Updated by System Admin for Achived Study");
		protocolFormStatus.setProtocolForm(protocolForm);
		protocolFormStatus
				.setProtocolFormStatus(ProtocolFormStatusEnum.IRB_APPROVED);
		protocolFormStatus.setRetired(false);

		protocolFormStatusDao.saveOrUpdate(protocolFormStatus);
	}

	// @Test
	public void testFindProtocolFormByProtocolId() {
		List<ProtocolForm> pfLst = miagrationDao
				.findProtocolFormByProtocolId(201524l);

		// logger.debug("meta data: " + p.getProtocol().getId());
	}

	// @Test
	public void testfindProtocolFormXmlDataByProtocolId() {
		List<ProtocolFormXmlData> pfxdLst = miagrationDao
				.findProtocolFormXmlDataByProtocolId(201524l);

		for (ProtocolFormXmlData pfxd : pfxdLst) {
			logger.debug("type: " + pfxd.getProtocolFormXmlDataType());
		}

		// logger.debug("meta data: " + p.getProtocol().getId());
	}

	// @Test
	public void testfindProtocolFormXmlDataDocumentsByProtocolId() {
		List<ProtocolFormXmlDataDocument> pfxddLst = miagrationDao
				.findProtocolFormXmlDataDocumentsByProtocolId(201524l);

		for (ProtocolFormXmlDataDocument pfxdd : pfxddLst) {
			logger.debug("title: " + pfxdd.getTitle());
		}

		// logger.debug("meta data: " + p.getProtocol().getId());
	}

	// @Test
	public void testListAllStudiesByUserIdAndRoleName() {
		Set<Permission> permissionSet = new HashSet<Permission>();
		// permissionSet.add(Permission.WRITE);
		permissionSet.add(Permission.READ);

		// User user = userDao.findById(2446l);

		// List<Protocol> pfxddLst =
		// miagrationDao.findAllStudiesByUserIdAndRoleName(41l, "");

		// List<Protocol> pfxddLst =
		// miagrationDao.findAllStudiesByUserIdAndRoleName(0,
		// "STUDY COORDINATOR");

		// List<Protocol> pfxddLst =
		// miagrationDao.findAllStudiesByUserPISerialAndRoleName("20000",
		// "STUDY COORDINATOR");

		// List<ProtocolFormXmlData> pfxddLst =
		// protocolFormXmlDataDao.listProtocolformXmlDatasByFormId(8515l);

		// List<Protocol> pfxdLst =
		// miagrationDao.listAllProtocolWithTypeIssue();

		List<Long> protocolIds = Lists.newArrayList();
		// protocolIds.add(110528l);

		protocolIds.add(109924l);
		protocolIds.add(105581l);
		protocolIds.add(63573l);
		protocolIds.add(31115l);
		protocolIds.add(138823l);
		protocolIds.add(133414l);
		protocolIds.add(130873l);
		protocolIds.add(110808l);

		// List<Protocol> pfxdLst =
		// miagrationDao.findAllStudiesByPISerial(15114l);

		// logger.debug("Size: " + pfxddLst.size());
		try {
			/*
			 * for (Long protocolId : protocolIds){
			 *
			 * List<ProtocolForm> pfLst =
			 * protocolFormDao.listProtocolFormsByProtocolIdAndProtocolFormType
			 * (protocolId, ProtocolFormType.ARCHIVE);
			 *
			 * for (ProtocolForm pf : pfLst){ ProtocolFormXmlData pfxd =
			 * pf.getTypedProtocolFormXmlDatas
			 * ().get(pf.getProtocolFormType().getDefaultProtocolFormXmlDataType
			 * ());
			 *
			 * String xmlData = pfxd.getXmlData();
			 *
			 * String id = UUID.randomUUID().toString();
			 *
			 * String staffXml = "<staff id=\""+ id +
			 * "\"><user id=\"761\" phone=\"+1 (501) 526-8439\" pi_serial=\"14341\" sap=\"6720\">"
			 * + "<lastname>Waldrip</lastname>" + "<firstname>Jan</firstname>" +
			 * "<email>WaldripJanC@uams.edu</email>" + "<roles>" +
			 * "<role>Research Administrator</role>" + "</roles>" + "<costs />"
			 * + "<conflict-of-interest />" +
			 * "<conflict-of-interest-description />" + "<reponsibilities>" +
			 * "<responsibility>Managing CLARA submission</responsibility>" +
			 * "</reponsibilities>" + "</user>" + "<notify>false</notify>" +
			 * "</staff>";
			 *
			 * Map<String,Object> resultMap =
			 * xmlProcessor.addElementByPath("/protocol/staffs/staff", xmlData,
			 * staffXml, false);
			 *
			 * pfxd.setXmlData(resultMap.get("finalXml").toString());
			 * protocolFormXmlDataDao.saveOrUpdate(pfxd); }
			 *
			 * objectAclService.updateObjectAclByUserAndPermissions(Protocol.class
			 * , protocolId, user, permissionSet); }
			 */

			// for (Protocol pfxd : pfxddLst){
			Protocol pfxd = protocolDao.findById(138456l);

			String email = "sophiehollenberg@yahoo.com";
			// User user = userDao.getUserByEmail(email);
			// String id = String.valueOf(user.getId());
			String piSerial = "19630";

			// logger.debug("userId: " + id);

			String xmlData = pfxd.getMetaDataXml();

			xmlData = xmlProcessor.replaceAttributeValueByPathAndAttributeName(
					"/protocol/staffs/staff/user[email=\"" + email + "\"]",
					"pi_serial", xmlData, piSerial);
			// xmlData =
			// xmlProcessor.replaceAttributeValueByPathAndAttributeName("/protocol/staffs/staff/user[@pi_serial=\"18085\"]",
			// "id", xmlData, id);
			// xmlData =
			// xmlProcessor.replaceAttributeValueByPathAndAttributeName("/protocol/staffs/staff/user[@pi_serial=\"20000\"]",
			// "id", xmlData, id);

			pfxd.setMetaDataXml(xmlData);
			protocolDao.saveOrUpdate(pfxd);

			List<ProtocolForm> protocolForms = protocolFormDao
					.listProtocolFormsByProtocolId(pfxd.getId());

			for (ProtocolForm pf : protocolForms) {
				String formMeta = pf.getMetaDataXml();

				// formMeta =
				// xmlProcessor.replaceOrAddNodeValueByPath("/protocol/staffs/staff/user[@id=\"41\"]/lastname",
				// formMeta, "Kinder");
				formMeta = xmlProcessor
						.replaceAttributeValueByPathAndAttributeName(
								"/protocol/staffs/staff/user[email=\"" + email
										+ "\"]", "pi_serial", xmlData, piSerial);

				pf.setMetaDataXml(formMeta);
				protocolFormDao.saveOrUpdate(pf);

				List<ProtocolFormXmlData> protocolFormXmlDatas = protocolFormXmlDataDao
						.listProtocolformXmlDatasByFormId(pf.getId());

				for (ProtocolFormXmlData pfx : protocolFormXmlDatas) {
					String formXmlData = pfx.getXmlData();

					// formXmlData =
					// xmlProcessor.replaceOrAddNodeValueByPath("/protocol/staffs/staff/user[@id=\"41\"]/lastname",
					// formXmlData, "Kinder");
					formXmlData = xmlProcessor
							.replaceAttributeValueByPathAndAttributeName(
									"/protocol/staffs/staff/user[email=\""
											+ email + "\"]", "pi_serial",
									xmlData, piSerial);

					pfx.setXmlData(formXmlData);
					protocolFormXmlDataDao.saveOrUpdate(pfx);
				}
			}

			// xmlData =
			// xmlProcessor.replaceAttributeValueByPathAndAttributeName("/protocol/staffs/staff/user[@id=\"1831\"]",
			// "id", xmlData, "2355");

			// pfxd.setMetaDataXml(xmlData);
			// protocolDao.saveOrUpdate(pfxd);

			// xmlData =
			// xmlProcessor.replaceOrAddNodeValueByPath("/protocol/staffs/staff/user[@id=\"158\"]/roles/role",
			// xmlData, "Study Coordinator");
			// xmlData =
			// xmlProcessor.replaceOrAddNodeValueByPath("/protocol/staffs/staff/user[@id=\"158\"]/reponsibilities/responsibility",
			// xmlData, "Managing CLARA submission");

			// pfxd.setXmlData(xmlData);
			// protocolFormXmlDataDao.saveOrUpdate(pfxd);

			// objectAclService.updateObjectAclByUserAndPermissions(Protocol.class,
			// pfxd.getId(), user, permissionSet);
			// }
		} catch (Exception e) {
			e.printStackTrace();
		}
		/*
		 * for (Protocol p : pfxddLst){ String meta = p.getMetaDataXml(); meta =
		 * meta.replace("<role>study coordinator</role>",
		 * "<role>Study Coordinator</role>");
		 *
		 * p.setMetaDataXml(meta); protocolDao.saveOrUpdate(p); }
		 */

	}

	// @Test
	public void testTypoFind() {
		List<Track> tracks = miagrationDao.findAllTypoTrack();
		logger.debug("typo size: " + tracks.size());

		for (Track t : tracks) {
			String trackXml = t.getXmlData();
			trackXml = trackXml.replace("improted", "imported");

			t.setXmlData(trackXml);
			trackDao.saveOrUpdate(t);
		}

	}

	// @Test
	public void fixMissingMigratedField() throws Exception {
		List<ProtocolFormXmlData> protocolFormXmlDataLst = miagrationDao
				.listAllMissingMigratedField();
		logger.debug("size: " + protocolFormXmlDataLst.size());

		for (ProtocolFormXmlData pfxd : protocolFormXmlDataLst) {
			String xmlData = pfxd.getXmlData();

			xmlData = xmlProcessor.replaceOrAddNodeValueByPath(
					"/protocol/migrated", xmlData, "y");

			pfxd.setXmlData(xmlData);
			protocolFormXmlDataDao.saveOrUpdate(pfxd);
		}
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
		metaDataPath.add("/protocol/most-recent-study/terminatedDate");
		metaDataPath.add("/protocol/most-recent-study/terminated-date");
		metaDataPath.add("/protocol/most-recent-study/suspendedDate");
		metaDataPath.add("/protocol/most-recent-study/suspend-date");
	}

	// @Test
	public void convertDate() throws Exception {
		List<Protocol> protocolLst = protocolDao.findAll();

		for (Protocol protocol : protocolLst) {
			if (protocol.getId() != 136210)
				continue;
			logger.debug("protocolId: " + protocol.getId());

			String protocolMetaData = protocol.getMetaDataXml();

			Map<String, List<String>> resultMap = Maps.newHashMap();

			try {
				resultMap = xmlProcessor.listElementStringValuesByPaths(
						metaDataPath, protocolMetaData);
			} catch (Exception e) {

			}

			if (resultMap.size() > 0) {
				for (String s : metaDataPath) {
					logger.debug("path: " + s);
					if (resultMap.get(s) == null || resultMap.get(s).isEmpty()) {
						continue;
					}

					try {
						Date date = new SimpleDateFormat("yyyy-MM-dd")
								.parse(resultMap.get(s).get(0));

						protocolMetaData = xmlProcessor
								.replaceOrAddNodeValueByPath(s,
										protocolMetaData,
										DateFormatUtil.formateDateToMDY(date));
					} catch (Exception e) {

					}
				}

				protocol.setMetaDataXml(protocolMetaData);
				protocolDao.saveOrUpdate(protocol);
			}

		}

		/*
		 * List<Contract> contractLst = contractDao.findAll();
		 *
		 * for (Contract contract : contractLst){ if (contract.getId() ==
		 * 20034){ continue; } String contractMetaData =
		 * contract.getMetaDataXml(); try { String createdStr =
		 * xmlProcessor.getAttributeValueByPathAndAttributeName("/contract",
		 * contractMetaData, "created");
		 *
		 * try { Date date = new
		 * SimpleDateFormat("yyyy-MM-dd").parse(createdStr); contractMetaData =
		 * xmlProcessor.replaceAttributeValueByPathAndAttributeName("/contract",
		 * "created", contractMetaData, DateFormatUtil.formateDateToMDY(date));
		 *
		 * contract.setMetaDataXml(contractMetaData);
		 * contractDao.saveOrUpdate(contract); } catch (IllegalArgumentException
		 * e){
		 *
		 * }
		 *
		 * } catch (Exception e){ e.printStackTrace(); }
		 *
		 * List<ContractForm> contractFormLst =
		 * contractFormDao.listContractFormsByContractId(contract.getId());
		 *
		 * for (ContractForm contractForm : contractFormLst){ String
		 * contractFormMetaData = contractForm.getMetaDataXml(); try { String
		 * createdStr =
		 * xmlProcessor.getAttributeValueByPathAndAttributeName("/contract",
		 * contractFormMetaData, "created");
		 *
		 * try { Date date = new
		 * SimpleDateFormat("yyyy-MM-dd").parse(createdStr);
		 * contractFormMetaData =
		 * xmlProcessor.replaceAttributeValueByPathAndAttributeName("/contract",
		 * "created", contractFormMetaData,
		 * DateFormatUtil.formateDateToMDY(date));
		 *
		 * contractForm.setMetaDataXml(contractFormMetaData);
		 * contractFormDao.saveOrUpdate(contractForm); } catch
		 * (IllegalArgumentException e){
		 *
		 * }
		 *
		 * } catch (Exception e){ e.printStackTrace(); }
		 *
		 * ContractFormXmlData cfxd =
		 * contractForm.getTypedContractFormXmlDatas()
		 * .get(contractForm.getContractFormType
		 * ().getDefaultContractFormXmlDataType()); String contractFormXmlData =
		 * cfxd.getXmlData(); try { String createdStr =
		 * xmlProcessor.getAttributeValueByPathAndAttributeName("/contract",
		 * contractFormXmlData, "created");
		 *
		 * try { Date date = new
		 * SimpleDateFormat("yyyy-MM-dd").parse(createdStr); contractFormXmlData
		 * =
		 * xmlProcessor.replaceAttributeValueByPathAndAttributeName("/contract",
		 * "created", contractFormXmlData,
		 * DateFormatUtil.formateDateToMDY(date));
		 *
		 * cfxd.setXmlData(contractFormXmlData);
		 * contractFormXmlDataDao.saveOrUpdate(cfxd); } catch
		 * (IllegalArgumentException e){
		 *
		 * }
		 *
		 * } catch (Exception e){ e.printStackTrace(); } } }
		 */
	}

	private Set<String> irbDeterminSet = Sets.newHashSet();
	{
		irbDeterminSet.add("/protocol/summary/irb-determination/review-period");
		irbDeterminSet.add("/protocol/summary/irb-determination/fda");
		irbDeterminSet.add("/protocol/summary/irb-determination/adult-risk");
		irbDeterminSet.add("/protocol/summary/irb-determination/ped-risk");
		irbDeterminSet
				.add("/protocol/summary/irb-determination/consent-waived");
		irbDeterminSet
				.add("/protocol/summary/irb-determination/consent-document-waived");
		irbDeterminSet
				.add("/protocol/summary/irb-determination/hipaa-not-applicable");
		irbDeterminSet.add("/protocol/summary/irb-determination/hipaa-waived");
		irbDeterminSet
				.add("/protocol/summary/irb-determination/suggested-next-review-type");
		irbDeterminSet
				.add("/protocol/summary/irb-determination/suggested-type");
		irbDeterminSet.add("/protocol/summary/irb-determination/finding");
		irbDeterminSet.add("/protocol/summary/irb-determination/finding-other");
		irbDeterminSet.add("/protocol/summary/irb-determination/reportable");
		irbDeterminSet.add("/protocol/summary/irb-determination/irb");
		irbDeterminSet.add("/protocol/summary/irb-determination/hipaa");
		irbDeterminSet.add("/protocol/summary/irb-determination/audit-type");
		irbDeterminSet.add("/protocol/summary/irb-determination/hipaa-finding");
		irbDeterminSet.add("/protocol/summary/irb-determination/irb-finding");
		irbDeterminSet.add("/protocol/summary/irb-determination/agenda-date");
		irbDeterminSet.add("/protocol/summary/irb-determination/recent-motion");
		irbDeterminSet
				.add("/protocol/summary/irb-determination/non-compliance-assessment");
		irbDeterminSet
				.add("/protocol/summary/irb-determination/reportable-to-ohrp");
		irbDeterminSet
				.add("/protocol/summary/irb-determination/hipaa-waived-date");
	}

	// @Test
	public void migrationDataFix() {
		// fix wrong path of accural-goal and accural-goal-local
		/*
		 * List<Protocol> protocolLst = protocolDao.listProtocolsByIdRange(0,
		 * 200000);
		 *
		 * for (Protocol p : protocolLst){ if (p.getId() == 72426) continue;
		 * logger.debug("protocol Id: " + p.getId()); String protocolMetaData =
		 * p.getMetaDataXml(); try { String accuralGoal =
		 * xmlHandler.getSingleStringValueByXPath(protocolMetaData,
		 * "/protocol/subjects/accural-goal"); String accuralGaolInt = "";
		 *
		 * if (accuralGoal.isEmpty()) { accuralGaolInt = ""; } else {
		 * accuralGaolInt =
		 * (!accuralGoal.equals("0.0"))?String.valueOf(Float.valueOf
		 * (accuralGoal).intValue()):"0"; }
		 *
		 * protocolMetaData =
		 * xmlProcessor.replaceOrAddNodeValueByPath("/protocol/accural-goal",
		 * protocolMetaData, accuralGaolInt);
		 *
		 * Map<String, String> attriutes = Maps.newHashMap();
		 * attriutes.put("value", accuralGoal); protocolMetaData =
		 * xmlProcessor.addAttributesByPath("/protocol/accural-goal",
		 * protocolMetaData, attriutes);
		 *
		 * } catch (Exception e){ e.printStackTrace(); }
		 *
		 * try { String accuralGoalLocal =
		 * xmlHandler.getSingleStringValueByXPath(protocolMetaData,
		 * "/protocol/subjects/accural-goal-local"); String accuralGaolLocalInt
		 * = "";
		 *
		 * if (accuralGoalLocal.isEmpty()) { accuralGaolLocalInt = ""; } else {
		 * accuralGaolLocalInt =
		 * (!accuralGoalLocal.equals("0.0"))?String.valueOf
		 * (Float.valueOf(accuralGoalLocal).intValue()):"0"; }
		 *
		 * protocolMetaData =
		 * xmlProcessor.replaceOrAddNodeValueByPath("/protocol/accural-goal-local"
		 * , protocolMetaData, accuralGaolLocalInt);
		 *
		 * Map<String, String> attriutes = Maps.newHashMap();
		 * attriutes.put("value", accuralGoalLocal); protocolMetaData =
		 * xmlProcessor.addAttributesByPath("/protocol/accural-goal-local",
		 * protocolMetaData, attriutes);
		 *
		 * } catch (Exception e){ e.printStackTrace(); }
		 *
		 * p.setMetaDataXml(protocolMetaData); protocolDao.saveOrUpdate(p); }
		 */

		/*
		 * List<Protocol> protocolLst =
		 * miagrationDao.listAllMigratedProtocolAcknowledged();
		 * logger.debug("size: " + protocolLst.size()); for (Protocol p :
		 * protocolLst){ if (p.getId() == 132662) continue;
		 *
		 * logger.debug("protocol id : " + p.getId());
		 *
		 * ProtocolForm pf =
		 * protocolFormDao.getLatestProtocolFormByProtocolIdAndProtocolFormType
		 * (p.getId(), ProtocolFormType.ARCHIVE);
		 *
		 * String protocolMetaData =p.getMetaDataXml(); String protocolFormMeta
		 * = pf.getMetaDataXml();
		 *
		 * try { Map<String, List<String>> pfMetaResultMap =
		 * xmlProcessor.listElementStringValuesByPaths(irbDeterminSet,
		 * protocolFormMeta);
		 *
		 * for (Entry<String, List<String>> entry : pfMetaResultMap.entrySet()){
		 * if (entry.getValue() != null && !entry.getValue().isEmpty()){ String
		 * pfValue = entry.getValue().get(0);
		 *
		 * String valueInProtocol =
		 * xmlHandler.getSingleStringValueByXPath(protocolMetaData,
		 * entry.getKey());
		 *
		 * if (valueInProtocol.isEmpty()){ protocolMetaData =
		 * xmlProcessor.replaceOrAddNodeValueByPath(entry.getKey(),
		 * protocolMetaData, pfValue); } } } } catch (Exception e) {
		 * e.printStackTrace(); }
		 *
		 * p.setMetaDataXml(protocolMetaData); protocolDao.saveOrUpdate(p); }
		 */

		List<Protocol> protocolLst = protocolDao.listProtocolsByIdRange(0,
				200000);

		for (Protocol p : protocolLst) {
			if (p.getId() == 111327)
				continue;
			logger.debug("protocol Id: " + p.getId());
			String protocolMeta = p.getMetaDataXml();

			try {
				String hippaValue = xmlHandler
						.getSingleStringValueByXPath(protocolMeta,
								"/protocol/summary/irb-determination/hipaa-not-applicable");
				String hippaChangeValue = "";

				if (hippaValue.equals("no")) {
					hippaChangeValue = "yes";
				} else if (hippaValue.equals("yes")) {
					hippaChangeValue = "no";
				}

				protocolMeta = xmlProcessor.replaceOrAddNodeValueByPath(
						"/protocol/summary/irb-determination/hipaa-applicable",
						protocolMeta, hippaChangeValue);
				Map<String, Object> resultMap = xmlProcessor
						.deleteElementByPath(
								"/protocol/summary/irb-determination/hipaa-not-applicable",
								protocolMeta);

				protocolMeta = resultMap.get("finalXml").toString();

				p.setMetaDataXml(protocolMeta);
				protocolDao.saveOrUpdate(p);
			} catch (Exception e) {

			}
		}
	}

	//@Test
	public void updateProtocolMetaData() {
		List<Protocol> protocolLst = miagrationDao.findFormStaffOnProtocol();

		logger.debug("size: " + protocolLst.size());

		for (Protocol p : protocolLst) {
			logger.debug(p.getId()+"");
			ProtocolForm pf = null;

			try {

				List<String> userIDList = xmlProcessor
						.getAttributeValuesByPathAndAttributeName(
								"/protocol/staffs/staff/user",
								p.getMetaDataXml(), "id");
				// String userIdStr =
				// xmlProcessor.getAttributeValueByPathAndAttributeName("/protocol/staffs/staff/user[roles/role/text()[fn:contains(fn:upper-case(.),\"FORMER\")]]",
				// p.getMetaDataXml(), "id");
				for (String userIdStr : userIDList) {
					List<String> rolesList = xmlProcessor
							.listElementStringValuesByPath(
									"/protocol/staffs/staff/user[@id=\""
											+ userIdStr + "\"]/roles/role",
									p.getMetaDataXml());
					int existFormerStaff = 0;
					for (String role : rolesList) {
						logger.debug(role);
						if (role.toLowerCase().contains("former")) {
							existFormerStaff = 1;
						}
					}

					if (existFormerStaff == 0) {
						continue;
					}

					logger.debug("userIdStr: " + userIdStr);
					if (userIdStr != null && !userIdStr.isEmpty()) {
						long userId = Long.valueOf(userIdStr);
						User u = userDao.findById(userId);

						Boolean include = false;

						try {
							pf = protocolFormDao
									.getLatestProtocolFormByProtocolIdAndProtocolFormType(
											p.getId(),
											ProtocolFormType.MODIFICATION);

							Document assertXml = xmlProcessor
									.loadXmlStringToDOM(pf.getMetaDataXml());

							XPath xpathInstance = xmlProcessor
									.getXPathInstance();
							String xPath = "boolean(count(/protocol/staffs/staff/user[@id="
									+ userIdStr + "])>0";
							include = (Boolean) (xpathInstance.evaluate(xPath,
									assertXml, XPathConstants.BOOLEAN));

						} catch (Exception e) {

						}
						logger.debug("include: " + include);
						if (pf == null) {
							if (objectAclService.isObjectAccessible(
									Protocol.class, p.getId(), u))
								objectAclService.deleteObjectAclByUserId(
										Protocol.class, p.getId(), userId);
						} else {
							if (!include) {
								if (objectAclService.isObjectAccessible(
										Protocol.class, p.getId(), u))
									objectAclService.deleteObjectAclByUserId(
											Protocol.class, p.getId(), userId);
							}
						}
					}
				}
			} catch (Exception e) {

			}

		}
	}

	private Map<ProtocolFormXmlDataType, Map<String, String>> xPathPairMap = new EnumMap<ProtocolFormXmlDataType, Map<String, String>>(
			ProtocolFormXmlDataType.class);
	{
		Map<String, String> newSubmissionXPathPairs = new HashMap<String, String>();
		newSubmissionXPathPairs.put("/protocol/extra/prmc-related-or-not",
				"/protocol/extra/prmc-related-or-not");

		xPathPairMap.put(ProtocolFormXmlDataType.PROTOCOL,
				newSubmissionXPathPairs);

		Map<String, String> modificationXPathPairs = new HashMap<String, String>();
		modificationXPathPairs.put("/protocol/extra/prmc-related-or-not",
				"/protocol/extra/prmc-related-or-not");

		xPathPairMap.put(ProtocolFormXmlDataType.MODIFICATION,
				modificationXPathPairs);
	}

	@Test
	public void updateProtocolMetaDataForCancerStudy(){

		List<ProtocolForm> pfList = this.miagrationDao.listCancerStudyProotcolForms();
		logger.debug("@@@@@@@@@@@@@@@@ size: " + pfList.size());

		for (ProtocolForm pf : pfList) {

			Protocol p = pf.getProtocol();

			if (p.getId() < 202782) continue;
			logger.debug("####### protocolFormId:" + pf.getId());
			String protocolMetaDataXml = p.getMetaDataXml();
			/*
			try {
				Map<String, List<String>> values = xmlProcessor.listElementStringValuesByPaths(paths, protocolMetaDataXml);

				if (values.size() > 0) continue;
			} catch (Exception e) {

			}
			*/

			try {
				//logger.debug("before mergeByXPaths -> protocol.metadataxml: "
						//+ protocolMetaDataXml);
				protocolMetaDataXml = xmlProcessor.mergeByXPaths(
						protocolMetaDataXml, pf.getMetaDataXml(),
						XmlProcessor.Operation.UPDATE_IF_EXIST,
						xPathPairMap.get(pf
								.getProtocolFormType().getDefaultProtocolFormXmlDataType()));
				//logger.debug("after mergeByXPaths -> protocol.metadataxml: "
						//+ protocolMetaDataXml);
				p.setMetaDataXml(protocolMetaDataXml);
				p = protocolDao.saveOrUpdate(p);
			} catch (XPathExpressionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SAXException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	public MiagrationDao getMiagrationDao() {
		return miagrationDao;
	}

	@Autowired(required = true)
	public void setMiagrationDao(MiagrationDao miagrationDao) {
		this.miagrationDao = miagrationDao;
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

	public ProtocolFormXmlDataDocumentDao getProtocolFormXmlDataDocumentDao() {
		return protocolFormXmlDataDocumentDao;
	}

	@Autowired(required = true)
	public void setProtocolFormXmlDataDocumentDao(
			ProtocolFormXmlDataDocumentDao protocolFormXmlDataDocumentDao) {
		this.protocolFormXmlDataDocumentDao = protocolFormXmlDataDocumentDao;
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

	public SecurableObjectDao getSecurableObjectDao() {
		return securableObjectDao;
	}

	@Autowired(required = true)
	public void setSecurableObjectDao(SecurableObjectDao securableObjectDao) {
		this.securableObjectDao = securableObjectDao;
	}

	public SecurableObjectAclDao getSecurableObjectAclDao() {
		return securableObjectAclDao;
	}

	@Autowired(required = true)
	public void setSecurableObjectAclDao(
			SecurableObjectAclDao securableObjectAclDao) {
		this.securableObjectAclDao = securableObjectAclDao;
	}

	public XmlProcessor getXmlProcessor() {
		return xmlProcessor;
	}

	@Autowired(required = true)
	public void setXmlProcessor(XmlProcessor xmlProcessor) {
		this.xmlProcessor = xmlProcessor;
	}

	public UserDao getUserDao() {
		return userDao;
	}

	@Autowired(required = true)
	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}

	public ObjectAclService getObjectAclService() {
		return objectAclService;
	}

	@Autowired(required = true)
	public void setObjectAclService(ObjectAclService objectAclService) {
		this.objectAclService = objectAclService;
	}

	public ProtocolFormCommitteeStatusDao getProtocolFormCommitteeStatusDao() {
		return protocolFormCommitteeStatusDao;
	}

	@Autowired(required = true)
	public void setProtocolFormCommitteeStatusDao(
			ProtocolFormCommitteeStatusDao protocolFormCommitteeStatusDao) {
		this.protocolFormCommitteeStatusDao = protocolFormCommitteeStatusDao;
	}

	public TrackDao getTrackDao() {
		return trackDao;
	}

	@Autowired(required = true)
	public void setTrackDao(TrackDao trackDao) {
		this.trackDao = trackDao;
	}

	public PersonDao getPersonDao() {
		return personDao;
	}

	@Autowired(required = true)
	public void setPersonDao(PersonDao personDao) {
		this.personDao = personDao;
	}

	public XmlHandler getXmlHandler() {
		return xmlHandler;
	}

	@Autowired(required = true)
	public void setXmlHandler(XmlHandler xmlHandler) {
		this.xmlHandler = xmlHandler;
	}

	public ContractDao getContractDao() {
		return contractDao;
	}

	@Autowired(required = true)
	public void setContractDao(ContractDao contractDao) {
		this.contractDao = contractDao;
	}

	public ContractFormDao getContractFormDao() {
		return contractFormDao;
	}

	@Autowired(required = true)
	public void setContractFormDao(ContractFormDao contractFormDao) {
		this.contractFormDao = contractFormDao;
	}

	public ContractFormXmlDataDao getContractFormXmlDataDao() {
		return contractFormXmlDataDao;
	}

	@Autowired(required = true)
	public void setContractFormXmlDataDao(
			ContractFormXmlDataDao contractFormXmlDataDao) {
		this.contractFormXmlDataDao = contractFormXmlDataDao;
	}

}
