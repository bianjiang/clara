package edu.uams.clara.webapp.protocol.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
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
import org.xml.sax.SAXException;

import com.google.common.collect.Sets;

import edu.uams.clara.webapp.common.dao.usercontext.UserDao;
import edu.uams.clara.webapp.common.domain.usercontext.User;
import edu.uams.clara.webapp.protocol.dao.protocolform.ProtocolFormXmlDataDao;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolFormXmlData;
import edu.uams.clara.webapp.protocol.service.impl.ProtocolFormServiceImpl;
import edu.uams.clara.webapp.xml.processor.XmlProcessor;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "file:src/test/java/edu/uams/clara/webapp/protocol/service/ProtocolFormServiceTest-context.xml" })
public class ProtocolFormServiceTest {
	
	private final static Logger logger = LoggerFactory
			.getLogger(ProtocolFormServiceTest.class);

	private XmlProcessor xmlProcessor;
	
	private UserDao userDao;
	
	private ProtocolFormXmlDataDao protocolFormXmlDataDao;
	
	
	@Test
	public void testBudgetOnlyTriger(){
		String workflow= workFlowDetermination(protocolFormXmlDataDao.findById(18908));
		logger.debug(workflow);
	}
	
	
	private String workFlowDetermination(ProtocolFormXmlData protocolFormXmlData) {
		String workflow = "";
		
		String protocolFormXmlDataString = protocolFormXmlData.getXmlData();

		try{
			logger.debug("!!!!3!"+protocolFormXmlData.getProtocolFormXmlDataType());
			switch(protocolFormXmlData.getProtocolFormXmlDataType()){
			case PROTOCOL:
				String studyNaturePath = "/protocol/study-nature";
				
				List<String> studyNatureValues = xmlProcessor.listElementStringValuesByPath(studyNaturePath, protocolFormXmlDataString);
				
				String studyNatureValue = (studyNatureValues!=null && !studyNatureValues.isEmpty())?studyNatureValues.get(0):"";
				
				String siteId = xmlProcessor.getAttributeValueByPathAndAttributeName("/protocol/study-sites/site", protocolFormXmlDataString, "site-id");

				if (studyNatureValue.equals("hud-use")){
					workflow = "HUD";
				} else {
					List<String> primaryResValues = xmlProcessor.listElementStringValuesByPath("/protocol/site-responsible", protocolFormXmlData.getXmlData());
					
					String primaryResValue = (primaryResValues!=null && !primaryResValues.isEmpty())?primaryResValues.get(0):""; 
					
					if (primaryResValue.equals("ach-achri") || (siteId != null && !siteId.isEmpty() && (siteId.equals("2") || siteId.equals("1")))){
						workflow = "ACH";
					} else if (primaryResValue.equals("uams")){
						List<String> studyTypeValues = xmlProcessor.listElementStringValuesByPath("/protocol/study-type", protocolFormXmlData.getXmlData());
						
						String studyTypeValue = (studyTypeValues!=null && !studyTypeValues.isEmpty())?studyTypeValues.get(0):""; 
						
						if (studyTypeValue.equals("investigator-initiated")){
							workflow = "INVESTIGATOR";
						} else {
							workflow = "NOT_INVESTIGATOR";
						}
					} else if (primaryResValue.equals("other")){
						workflow = "OTHER";
					}
				}
				break;
			case EMERGENCY_USE:
				List<String> euValues = xmlProcessor.listElementStringValuesByPath("//ieu-or-eu", protocolFormXmlData.getXmlData());
				
				String euValue = (euValues!=null && !euValues.isEmpty())?euValues.get(0):""; 
				
				if (euValue.equals("intended-emergency-use")){
					workflow = "INTENDED";
				} else if (euValue.equals("emergency-use-follow-up-report")){
					workflow = "FOLLOW-UP";
				}
				break;
			case MODIFICATION:
				Set<String> pathList = Sets.newHashSet();
				pathList.add("/protocol/crimson/has-budget");
				pathList.add("/protocol/budget/potentially-billed");
				pathList.add("/protocol/budget/need-budget-in-clara");
				pathList.add("/protocol/modification/to-modify-section/is-audit");
				pathList.add("/protocol/modification/to-modify-section/involve-change-in/budget-modified");
				pathList.add("/protocol/modification/to-modify-section/involve-change-in/contract-modified");
				pathList.add("/protocol/modification/to-modify-section/involve-change-in/pi-modified");
				pathList.add("/protocol/modification/to-modify-section/involve-addition-deletion-of/procedure");
				pathList.add("/protocol/modification/to-modify-section/involve-addition-deletion-of/pharmacy");
				pathList.add("/protocol/modification/to-modify-section/involve-addition-deletion-of/subjects");
				pathList.add("/protocol/modification/to-modify-section/amendment-to-injury");
				pathList.add("/protocol/modification/to-modify-section/submit-to-medicare");
				pathList.add("/protocol/modification/to-modify-section/conduct-under-uams");
				pathList.add("/protocol/study-type");
				pathList.add("/protocol/site-responsible");
				pathList.add("/protocol/migrated");
				pathList.add("/protocol/modification/to-modify-section/complete-budget-migration");
				
				try {
					Map<String, List<String>> values = getXmlProcessor().listElementStringValuesByPaths(pathList, protocolFormXmlDataString);
					
					//String hasCrimsonBudget = (values.get("/protocol/crimson/has-budget") != null && !values.get("/protocol/crimson/has-budget").isEmpty())?values.get("/protocol/crimson/has-budget").get(0):"";
					//String potentiallyBilled = (values.get("/protocol/budget/potentially-billed") != null && !values.get("/protocol/budget/potentially-billed").isEmpty())?values.get("/protocol/budget/potentially-billed").get(0):"";
					//String needBudgetInClara = (values.get("/protocol/budget/need-budget-in-clara") != null && !values.get("/protocol/budget/need-budget-in-clara").isEmpty())?values.get("/protocol/budget/need-budget-in-clara").get(0):"";
					String isAudit = (values.get("/protocol/modification/to-modify-section/is-audit") != null && !values.get("/protocol/modification/to-modify-section/is-audit").isEmpty())?values.get("/protocol/modification/to-modify-section/is-audit").get(0):"";
					String budgetModified = (values.get("/protocol/modification/to-modify-section/involve-change-in/budget-modified") != null && !values.get("/protocol/modification/to-modify-section/involve-change-in/budget-modified").isEmpty())?values.get("/protocol/modification/to-modify-section/involve-change-in/budget-modified").get(0):"";
					String contractModified = (values.get("/protocol/modification/to-modify-section/involve-change-in/contract-modified") != null && !values.get("/protocol/modification/to-modify-section/involve-change-in/contract-modified").isEmpty())?values.get("/protocol/modification/to-modify-section/involve-change-in/contract-modified").get(0):"";
					String piModified = (values.get("/protocol/modification/to-modify-section/involve-change-in/pi-modified") != null && !values.get("/protocol/modification/to-modify-section/involve-change-in/pi-modified").isEmpty())?values.get("/protocol/modification/to-modify-section/involve-change-in/pi-modified").get(0):"";
					String procedureDeleted = (values.get("/protocol/modification/to-modify-section/involve-addition-deletion-of/procedure") != null && !values.get("/protocol/modification/to-modify-section/involve-addition-deletion-of/procedure").isEmpty())?values.get("/protocol/modification/to-modify-section/involve-addition-deletion-of/procedure").get(0):"";
					String pharmacyDeleted = (values.get("/protocol/modification/to-modify-section/involve-addition-deletion-of/pharmacy") != null && !values.get("/protocol/modification/to-modify-section/involve-addition-deletion-of/pharmacy").isEmpty())?values.get("/protocol/modification/to-modify-section/involve-addition-deletion-of/pharmacy").get(0):"";
					String subjectDeleted = (values.get("/protocol/modification/to-modify-section/involve-addition-deletion-of/subjects") != null && !values.get("/protocol/modification/to-modify-section/involve-addition-deletion-of/subjects").isEmpty())?values.get("/protocol/modification/to-modify-section/involve-addition-deletion-of/subjects").get(0):"";
					String amendToInjury = (values.get("/protocol/modification/to-modify-section/amendment-to-injury") != null && !values.get("/protocol/modification/to-modify-section/amendment-to-injury").isEmpty())?values.get("/protocol/modification/to-modify-section/amendment-to-injury").get(0):"";
					String submitToMedicare = (values.get("/protocol/modification/to-modify-section/submit-to-medicare") != null && !values.get("/protocol/modification/to-modify-section/submit-to-medicare").isEmpty())?values.get("/protocol/modification/to-modify-section/submit-to-medicare").get(0):"";
					String conductUnderUams = (values.get("/protocol/modification/to-modify-section/conduct-under-uams") != null && !values.get("/protocol/modification/to-modify-section/conduct-under-uams").isEmpty())?values.get("/protocol/modification/to-modify-section/conduct-under-uams").get(0):"";
					String studyType = (values.get("/protocol/study-type") != null && !values.get("/protocol/study-type").isEmpty())?values.get("/protocol/study-type").get(0):"";
					String respSite = (values.get("/protocol/site-responsible") != null && !values.get("/protocol/site-responsible").isEmpty())?values.get("/protocol/site-responsible").get(0):"";
					String migrated = (values.get("/protocol/migrated") != null && !values.get("/protocol/migrated").isEmpty())?values.get("/protocol/migrated").get(0):"";
					String budgetConvert = (values.get("/protocol/modification/to-modify-section/complete-budget-migration") != null && !values.get("/protocol/modification/to-modify-section/complete-budget-migration").isEmpty())?values.get("/protocol/modification/to-modify-section/complete-budget-migration").get(0):"";
					logger.debug("!!!!3!");
					if (isAudit.equals("y")) {
						workflow = "IRB";
					} else if (migrated.equals("y")) {
						if (respSite.equals("ach-achri")) {
							workflow = "IRB";
						} else if (budgetConvert.equals("y")) {
							workflow = "COMPLIANCE";
						} else {
							workflow = "IRB";
						}
					} else {
						if (budgetModified.equals("y") || contractModified.equals("y") || piModified.equals("y") || procedureDeleted.equals("y") || pharmacyDeleted.equals("y") || subjectDeleted.equals("y") || amendToInjury.equals("y") || submitToMedicare.equals("y")) {
							if (studyType.equals("investigator-initiated")) {
								if (conductUnderUams.equals("y")) {
									workflow = "GATEKEEPER";
								} else {
									workflow = "BUDGET_ONLY";
									logger.debug("!!!!!2");
								}
							} else if (studyType.equals("industry-sponsored") || studyType.equals("industry-sponsored")) {
								workflow = "BUDGET_ONLY";
								logger.debug("!!!!3!");
							}
						} else {
							workflow = "IRB";
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				/*
				Map<String, String> workFlowPair = new HashMap<String, String>();
				workFlowPair.put("budget", "BUDGET_ONLY");
				workFlowPair.put("irb", "IRB");
				workFlowPair.put("gatekeeper", "GATEKEEPER");
				workFlowPair.put("irb-mig", "CRIMSON");
				
				List<String> toModificationValues = xmlProcessor.listElementStringValuesByPath("//modification/require-review", protocolFormXmlData.getXmlData());
				
				String toModificationValue = (toModificationValues!=null && !toModificationValues.isEmpty())?toModificationValues.get(0):""; 
				
				workflow = workFlowPair.get(toModificationValue);
				*/
				break;
			default:
				break;
			}
			
		} catch (Exception e){
			e.printStackTrace();
		}
		
		logger.debug("workflow: " + workflow);
		return workflow;
	}
	
	//@Test
	public void testIsPIOrNot() throws XPathExpressionException, IOException, SAXException{
		ProtocolFormXmlData protocolFormXmlData = protocolFormXmlDataDao.findById(826l);
		User currentUser = userDao.findById(1l);
		
		String x = "/protocol/staffs/staff/user[@id='" + currentUser.getId()
				+ "']/roles/role[contains(.,'Principal Investigator')]";
		logger.debug("xPath: " + x);
		XPath xpath = xmlProcessor.getXPathInstance();

		String xmlData = null;
		switch (protocolFormXmlData.getProtocolFormXmlDataType()) {
		case PROTOCOL:
		case HUMAN_SUBJECT_RESEARCH_DETERMINATION:
		case MODIFICATION:
			xmlData = protocolFormXmlData.getXmlData();
			break;
		default:
			xmlData = protocolFormXmlData.getProtocolForm().getProtocol()
					.getMetaDataXml();
		}

		boolean isPI = false;
		try {
			isPI = (Boolean) xpath.evaluate(x,
					xmlProcessor.loadXmlStringToDOM(xmlData),
					XPathConstants.BOOLEAN);
		} catch (Exception e) {
			logger.error("error when checking whether userId: "
					+ currentUser.getId()
					+ "; is the PI or Not on protocolFormXmlDataId: "
					+ protocolFormXmlData.getId() + "; due to: "
					+ e.getMessage());
			
		}
		
		logger.debug("isPI: " + isPI);


		
	}

	public XmlProcessor getXmlProcessor() {
		return xmlProcessor;
	}

	@Autowired(required=true)
	public void setXmlProcessor(XmlProcessor xmlProcessor) {
		this.xmlProcessor = xmlProcessor;
	}

	public UserDao getUserDao() {
		return userDao;
	}

	@Autowired(required=true)
	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}

	public ProtocolFormXmlDataDao getProtocolFormXmlDataDao() {
		return protocolFormXmlDataDao;
	}

	@Autowired(required=true)
	public void setProtocolFormXmlDataDao(ProtocolFormXmlDataDao protocolFormXmlDataDao) {
		this.protocolFormXmlDataDao = protocolFormXmlDataDao;
	}

}
