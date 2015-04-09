package edu.uams.clara.migration.bugfix;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.transaction.Transactional;
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
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import au.com.bytecode.opencsv.CSVWriter;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import edu.uams.clara.core.util.xml.DomUtils;
import edu.uams.clara.core.util.xml.XmlHandler;
import edu.uams.clara.integration.incoming.crimson.dao.CrimsonStudyDao;
import edu.uams.clara.webapp.common.dao.department.CollegeDao;
import edu.uams.clara.webapp.common.dao.department.DepartmentDao;
import edu.uams.clara.webapp.common.dao.history.TrackDao;
import edu.uams.clara.webapp.common.dao.usercontext.UserDao;
import edu.uams.clara.webapp.common.domain.department.College;
import edu.uams.clara.webapp.common.domain.department.Department;
import edu.uams.clara.webapp.common.domain.history.Track;
import edu.uams.clara.webapp.common.domain.usercontext.User;
import edu.uams.clara.webapp.common.domain.usercontext.enums.Committee;
import edu.uams.clara.webapp.common.service.UserService;
import edu.uams.clara.webapp.contract.dao.contractform.ContractFormDao;
import edu.uams.clara.webapp.protocol.dao.ProtocolDao;
import edu.uams.clara.webapp.protocol.dao.businesslogicobject.ProtocolFormCommitteeStatusDao;
import edu.uams.clara.webapp.protocol.dao.businesslogicobject.ProtocolFormStatusDao;
import edu.uams.clara.webapp.protocol.dao.etl.MiagrationDao;
import edu.uams.clara.webapp.protocol.dao.protocolform.ProtocolFormDao;
import edu.uams.clara.webapp.protocol.dao.protocolform.ProtocolFormUserElementTemplateDao;
import edu.uams.clara.webapp.protocol.dao.protocolform.ProtocolFormXmlDataDao;
import edu.uams.clara.webapp.protocol.domain.Protocol;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.ProtocolFormCommitteeStatus;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.ProtocolFormStatus;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.enums.ProtocolFormCommitteeStatusEnum;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.enums.ProtocolFormStatusEnum;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolForm;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolFormUserElementTemplate;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolFormXmlData;
import edu.uams.clara.webapp.protocol.domain.protocolform.enums.ProtocolFormType;
import edu.uams.clara.webapp.protocol.domain.protocolform.enums.ProtocolFormXmlDataType;
import edu.uams.clara.webapp.xml.processor.XmlProcessor;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "file:src/test/java/edu/uams/clara/migration/bugfix/BugFixTest-context.xml" })
public class BugFixTest {
	private final static Logger logger = LoggerFactory
			.getLogger(BugFixTest.class);

	private ProtocolFormUserElementTemplateDao protocolFormUserElementTemplateDao;

	private XmlProcessor xmlProcessor;
	private XmlHandler xmlHandler;
	private UserDao userDao;
	private ProtocolDao protocolDao;
	private ProtocolFormDao protocolFormDao;
	private ProtocolFormXmlDataDao protocolFormXmlDataDao;
	private TrackDao trackDao;
	private ContractFormDao contractFormDao;
	private MiagrationDao miagrationDao;
	private UserService userService;
	private ProtocolFormStatusDao protocolFormStatusDao;
	private ProtocolFormCommitteeStatusDao protocolFormCommitteeStatusDao;
	private CrimsonStudyDao crimsonStudyDao;
	private CollegeDao collegeDao;
	private DepartmentDao departmentDao;

	private EntityManager em;
	
    public String stripHtmlRegex(String source) {
	// Replace all tag characters with an empty string.
	return source.replaceAll("<.*?>", "");
    }

    public String stripTagsCharArray(String source) {
	// Create char array to store our result.
	char[] array = new char[source.length()];
	int arrayIndex = 0;
	boolean inside = false;

	// Loop over characters and append when not inside a tag.
	for (int i = 0; i < source.length(); i++) {
	    char let = source.charAt(i);
	    if (let == '<') {
		inside = true;
		continue;
	    }
	    if (let == '>') {
		inside = false;
		continue;
	    }
	    if (!inside) {
		array[arrayIndex] = let;
		arrayIndex++;
	    }
	}
	// ... Return written data.
	return new String(array, 0, arrayIndex);
    }
	
	@Test
	public void auditReportWithHistory() throws IOException, XPathExpressionException, SAXException{
		String queryStr = "SELECT distinct CAST(id AS varchar(100)) as protocolId ,meta_data_xml.value('(/protocol/study-type[text() = \"investigator-initiated\"]/investigator-initiated/investigator-description/text())[1]','varchar(max)') as subtype , meta_data_xml.value('(/protocol/staffs/staff/user[roles/role[fn:contains(fn:upper-case(.),\"PRINCIPAL INVESTIGATOR\")]]/lastname/text())[1]','varchar(50)')+ ',' +meta_data_xml.value('(/protocol/staffs/staff/user[roles/role[fn:contains(fn:upper-case(.),\"PRINCIPAL INVESTIGATOR\")]]/firstname/text())[1]','varchar(50)') as piName ,meta_data_xml.value('(/protocol/title/text())[1]','varchar(max)') as protocalTitle, meta_data_xml.value('(/protocol/responsible-department/@collegedesc)[1]','varchar(50)') as college,  meta_data_xml.value('(/protocol/responsible-department/@deptdesc)[1]','varchar(50)') as department, meta_data_xml.value('(/protocol/status/text())[1]','varchar(50)') as protocolStatus  "
				+ " FROM protocol WHERE retired = 0 AND meta_data_xml.exist('/protocol/study-type/text()[fn:upper-case(.)=\"INVESTIGATOR-INITIATED\"]')=1 AND Datediff(day, '05/31/2014',created)>0 and id not in (select [protocol_id] from test_studies)";
		Query query = em.createNativeQuery(queryStr);
		List<Object[]> results = (List<Object[]>) query.getResultList();
		logger.debug(results.size()+"");
		CSVWriter writer = new CSVWriter(new FileWriter(
				"C:\\Data\\ClaraDataOutput.csv"));
		String[] title = { "IRB", "PI Name","PI Status","Title","Date to Gatekeeper","College","Department","History"};
		writer.writeNext(title);
		for(Object[] result:results){
			String pidString = (String)result[0];
			String subtype = (String)result[1];
			String piname = (String)result[2];
			String protocolTitle = (String)result[3];
			String college = (String)result[4];
			String department = (String)result[5];
			subtype.replaceAll("-", " ");
			long pid = Long.valueOf(pidString);
			Track track = trackDao.getTrackByTypeAndRefObjectID("PROTOCOL", pid);
			String historyXml = track.getXmlData();
			Set<String> logpath = Sets.newHashSet();
			logpath.add("logs/log");
			List<Element> logs = xmlProcessor.listDomElementsByPaths(logpath, historyXml);
			String history ="";
			
			for(Element log : logs){
				String actor = log.getAttribute("actor");
				String eventType = log.getAttribute("event-type");
				String formType = log.getAttribute("form-type");
				String dateTime = log.getAttribute("date-time");
				String message = log.getTextContent();
				message = stripHtmlRegex(message);
				message = stripTagsCharArray(message);
				history += "Time: "+ dateTime+" Actor: "+actor+" Event: "+eventType+" Form Type: "+formType+" Log: "+message+"\n\n";
			}
			
			//get time to gatekeeper
			long newsubFormId = protocolFormDao.listProtocolFormsByProtocolId(pid).get(0).getParentFormId();
			
			
			List<ProtocolFormCommitteeStatus> gatekeeperPfcss = protocolFormCommitteeStatusDao
					.listAllByCommitteeAndProtocolFormId(Committee.GATEKEEPER,newsubFormId);
			
			String GatekeeperStartTime = "";
			for(ProtocolFormCommitteeStatus pfcs: gatekeeperPfcss){
				if(pfcs.getProtocolFormCommitteeStatus().equals(ProtocolFormCommitteeStatusEnum.IN_REVIEW)){
					GatekeeperStartTime =pfcs.getModifiedDateTime(); 
					break;
				}
			}
			
			String[] empty = {pidString,piname,subtype,protocolTitle,GatekeeperStartTime,college,department,history};
			writer.writeNext(empty);
		}
		writer.flush();
		writer.close();
	}
	
	//@Test
	public void countActiveStudiesByDepartment(){
		List<Department> depts = departmentDao.findDeptsByCollegeId(5);
		List<College> colleges = collegeDao.findAll();
		Map<String,String> collegeResults = Maps.newHashMap();
		Map<String,String> deptResults = Maps.newHashMap();
		/*for(Department dept: depts){
			String queryStr = "select count(id) from protocol where retired = 0 and id not in (select distinct protocol_id from protocol_form where retired =0 and protocol_form_type = 'HUMAN_SUBJECT_RESEARCH_DETERMINATION') and id in (select distinct protocol_id from protocol_status where retired = 0 and protocol_status ='open' and id in (select max(id) from protocol_status where retired = 0 group by protocol_id)) and id  not in (select protocol_id from test_studies) and meta_data_xml.exist('/protocol/extra/prmc-related-or-not/text()[fn:contains(fn:upper-case(.),\"Y\")]')=1 and meta_data_xml.exist('/protocol/responsible-department[@collegeid = \"5\" and @deptid = \""+dept.getId()+"\"]')=1";

			
			logger.debug(queryStr);
			Query query = em.createNativeQuery(queryStr);
			deptResults.put(dept.getName(), ""+query.getSingleResult());
		}*/
		
		for(College college: colleges){
			String queryStr = "select count(id) from protocol where retired = 0 and id not in (select distinct protocol_id from protocol_form where retired =0 and protocol_form_type = 'HUMAN_SUBJECT_RESEARCH_DETERMINATION') and id in (select distinct protocol_id from protocol_status where retired = 0 and protocol_status ='open' and id in (select max(id) from protocol_status where retired = 0 group by protocol_id)) and id  not in (select protocol_id from test_studies) and meta_data_xml.exist('/protocol/extra/prmc-related-or-not/text()[fn:contains(fn:upper-case(.),\"Y\")]')=1 and meta_data_xml.exist('/protocol/responsible-department[@collegeid = \""+college.getId()+"\"]')=1";
			logger.debug(queryStr);
			Query query = em.createNativeQuery(queryStr);
			collegeResults.put(college.getName(), ""+query.getSingleResult());
		}
			
		for(String key : collegeResults.keySet()){
			System.out.println(key);
		}
		
		for(String key : collegeResults.keySet()){
			System.out.println(collegeResults.get(key));
		}
		
	}
	
	/*@Transactional
	@Test
	public void fixcptcode() throws NumberFormatException, IOException{
		//String qry = "select cpt_code from hospital_charge_procedure where retired =0 and len(cpt_code)<5 and cpt_code in ('100','102','103','104','1112','1120','1130','1140','1150','1160','1170','1173','1180','1190','120','1200','1202','1210','1212','1214','1215','1220','1230','1232','1234','124','1250','126','1260','1270','1272','1274','1320','1340','1360','1380','1382','1390','1392','140','1400','1402','1404','142','1420','1430','1432','144','1440','1442','1444','145','1462','1464','147','1470','1472','1474','148','1480','1482','1484','1486','1490','1500','1502','1520','1522','160','1610','162','1620','1622','1630','1634','1636','1638','164','1650','1652','1654','1656','1670','1680','1682','170','1710','1712','1714','1716','172','1730','1732','174','1740','1742','1744','1756','1758','176','1760','1770','1772','1780','1782','1810','1820','1829','1830','1832','1840','1842','1844','1850','1852','1860','190','1916','192','1920','1922','1924','1925','1926','1930','1931','1932','1933','1935','1936','1951','1952','1953','1958','1960','1961','1962','1963','1965','1966','1967','1968','1969','1990','1991','1992','1996','1999','210','211','212','214','215','216','218','220','222','300','320','322','326','350','352','400','402','404','406','410','450','452','454','470','472','474','500','520','522','524','528','529','530','532','534','537','539','540','541','542','546','548','550','560','561','562','563','566','567','580','600','604','620','622','625','626','630','632','634','635','640','670','700','702','730','740','750','752','754','756','770','790','792','794','796','797','800','802','810','820','830','832','834','836','840','842','844','846','848','851','860','862','864','865','866','868','870','872','873','880','882','902','904','906','908','910','912','914','916','918','920','921','922','924','926','928','930','932','934','936','938','940','942','944','948','950','952')";
		//String qry = "select code from cpt_code where retired =0 and len(code)<5";
		String qry = "select cpt_code from physician_charge_procedure where retired =0 and len(cpt_code)<5";
		
		Query query = em.createNativeQuery(qry);
		List<String> codes = query.getResultList();
		for(String code:codes){
			try{
				Integer.valueOf(code);
			}catch(Exception e){
				continue;
			}
			String tempcode = code;
			int length = code.length();
			for(int i =0;i<5-length;i++){
				code = "0"+code;
			}
			qry = "update physician_charge_procedure set cpt_code = '"+code+"' where cpt_code = '"+tempcode+"'";
			//qry = "update hospital_charge_procedure set cpt_code = '"+code+"' where cpt_code = '"+tempcode+"'";
			//qry = "update cpt_code set code =  '"+code+"' where code = '"+tempcode+"'";
			logger.debug(qry);
			query = em.createNativeQuery(qry);
			query.executeUpdate();
			System.out.println(qry);
		}
	}*/
	
	//@Test
	public void fixPlanCode() throws NumberFormatException, IOException{
		List<Protocol> protocols = Lists.newArrayList();
		protocols=protocolDao.listProtocolsByIdRange(0, 200000);
		
		//protocols.add(protocolDao.findById(104727));

		for(Protocol p : protocols){
			if(crimsonStudyDao.findCTObjectbyIRBNum(p.getId()+"")==null){
				continue;
			}
			Object[] ctObj = crimsonStudyDao.findCTObjectbyIRBNum(p.getId()+"");
			int ctNumber = (int) ctObj[14];
			String qry = "SELECT [txt_corporate_guarantor],[txt_plan_code] FROM [HOSP_SQL1].[ClinicalResearch].[dbo].[budget] where [num_ct_ID] ="+ctNumber;
			Query query = em.createNativeQuery(qry);
			try{
			Object[] codeInfo = (Object[]) query.getSingleResult();
			String planCode = (String) codeInfo[1];
			String guarantorCpde = (String) codeInfo[0];
			String xml =  p.getMetaDataXml();
			String existPlanCode = "";
			String existGuarantorCpde = "";
			existGuarantorCpde=xmlHandler.getSingleStringValueByXPath(xml, "/protocol/summary/hospital-service-determinations/corporate-gurantor-code");
			existPlanCode=xmlHandler.getSingleStringValueByXPath(xml, "/protocol/summary/hospital-service-determinations/insurance-plan-code");
			if(existPlanCode.isEmpty()&&existGuarantorCpde.isEmpty()){
				xml =(String) xmlProcessor.addSubElementToElementIdentifiedByXPath("/protocol/summary", xml, "<hospital-service-determinations></hospital-service-determinations>", false).get("finalXml");
			}
			if(existPlanCode.isEmpty()){
				xml =(String) xmlProcessor.addSubElementToElementIdentifiedByXPath("/protocol/summary/hospital-service-determinations", xml, "<insurance-plan-code></insurance-plan-code>", false).get("finalXml");
				xml=xmlProcessor.replaceOrAddNodeValueByPath("/protocol/summary/hospital-service-determinations/insurance-plan-code", xml, planCode);	
			}
			if(existGuarantorCpde.isEmpty()){
				xml =(String) xmlProcessor.addSubElementToElementIdentifiedByXPath("/protocol/summary/hospital-service-determinations", xml, "<corporate-gurantor-code></corporate-gurantor-code>", false).get("finalXml");
				xml=xmlProcessor.replaceOrAddNodeValueByPath("/protocol/summary/hospital-service-determinations/corporate-gurantor-code", xml, guarantorCpde);	
			}
			p.setMetaDataXml(xml);
			protocolDao.saveOrUpdate(p);
			logger.debug(codeInfo[0]+" "+codeInfo[1]+" "+ctNumber+" "+p.getId());
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}
	
	//@Test
	public void fixMigrationQuestionInNonInitialModForm(){
// /protocol/modification/to-modify-section/complete-migration
		List<ProtocolFormXmlData> pfxds= protocolFormXmlDataDao.listProtocolformXmlDatasByType(ProtocolFormXmlDataType.MODIFICATION);
		//List<ProtocolFormXmlData> pfxds = Lists.newArrayList();
		//pfxds.add(protocolFormXmlDataDao.findById(15480));
		
		logger.debug("working");
		for(ProtocolFormXmlData pfxd :pfxds){
			String xml = pfxd.getXmlData();
			ProtocolForm pf = pfxd.getProtocolForm();
			
			String pfXml  = pf.getMetaDataXml();
			String initialMod = xmlHandler.getSingleStringValueByXPath(pfXml, "/protocol/initial-mod");
			if(initialMod.equals("y")){
				continue;
			}
			initialMod = xmlHandler.getSingleStringValueByXPath(xml, "/protocol/initial-mod");
			if(initialMod.equals("y")){
				continue;
			}
			try{
			if(!protocolFormStatusDao.getLatestProtocolFormStatusByFormId(pf.getFormId()).getProtocolFormStatus().equals(ProtocolFormStatusEnum.DRAFT)){
				//logger.debug(pf.getFormId()+""+protocolFormStatusDao.getLatestProtocolFormStatusByFormId(pf.getFormId()).getProtocolFormStatus());
				continue;
			}}
			catch(Exception e){
					continue;
			}
			String errorTag = "";
			errorTag = xmlHandler.getSingleStringValueByXPath(xml, "/protocol/modification/to-modify-section/complete-migration");
			if(!errorTag.isEmpty()){
				Map<String, Object> resultMap = new HashMap<String, Object>(0);
				try {
					resultMap = xmlProcessor.deleteElementByPath(
							"/protocol/modification/to-modify-section/complete-migration", xml);
					xml = resultMap.get("finalXml").toString();
				} catch (Exception e) {
					logger.info("element does not exist.");
				}
				pfxd.setXmlData(xml);
				protocolFormXmlDataDao.saveOrUpdate(pfxd);
				
				resultMap= new HashMap<String, Object>(0);
				try {
					resultMap = xmlProcessor.deleteElementByPath(
							"/protocol/modification/to-modify-section/complete-migration", pfXml);
					pfXml = resultMap.get("finalXml").toString();
				} catch (Exception e) {
					logger.info("element does not exist.");
				}
				
				pf.setMetaDataXml(pfXml);
				protocolFormDao.saveOrUpdate(pf);
				logger.debug(pf.getFormId()+"");
			}
		}
	}
	
	//@Test
	public void fixMissingIRBReviewCommitteeStatus(){

		Map<ProtocolFormCommitteeStatusEnum, ProtocolFormCommitteeStatusEnum> commiteeStatusMap = Maps.newHashMap();
		commiteeStatusMap.put(ProtocolFormCommitteeStatusEnum.APPROVED, ProtocolFormCommitteeStatusEnum.APPROVED);
		commiteeStatusMap.put(ProtocolFormCommitteeStatusEnum.DECLINED, ProtocolFormCommitteeStatusEnum.DECLINED);
		commiteeStatusMap.put(ProtocolFormCommitteeStatusEnum.DEFERRED_WITH_MAJOR_CONTINGENCIES, ProtocolFormCommitteeStatusEnum.DEFERRED_WITH_MAJOR_CONTINGENCIES);
		commiteeStatusMap.put(ProtocolFormCommitteeStatusEnum.DEFERRED_WITH_MINOR_CONTINGENCIES, ProtocolFormCommitteeStatusEnum.DEFERRED_WITH_MINOR_CONTINGENCIES);
		//commiteeStatusMap.put(ProtocolFormCommitteeStatusEnum.PENDING_IRB_REVIEW_RE_ASSIGNMENT, ProtocolFormCommitteeStatusEnum.REMOVED_FROM_IRB_AGENDA);
		commiteeStatusMap.put(ProtocolFormCommitteeStatusEnum.TABLED, ProtocolFormCommitteeStatusEnum.TABLED);
		commiteeStatusMap.put(ProtocolFormCommitteeStatusEnum.ACKNOWLEDGED, ProtocolFormCommitteeStatusEnum.ACKNOWLEDGED);
		
		
		List<ProtocolForm> pfs =Lists.newArrayList();
		pfs = protocolFormDao.findAll();
		//pfs.add(protocolFormDao.findById(13919));
		for(ProtocolForm pf :pfs){
			long pfId = pf.getFormId();
		List<ProtocolFormCommitteeStatus> pfcss = protocolFormCommitteeStatusDao.listAllOfCurrentFormByCommitteeAndProtocolFormId(Committee.IRB_REVIEWER, pfId);
		List<ProtocolFormCommitteeStatus> pfcssForIRBOFFICE = protocolFormCommitteeStatusDao.listAllOfCurrentFormByCommitteeAndProtocolFormId(Committee.IRB_OFFICE, pfId);
		List<ProtocolFormCommitteeStatusEnum> pfcssForIRBOFFICEnums  = Lists.newArrayList();
		List<ProtocolFormCommitteeStatusEnum> pfcssForIRBReviewEnums  = Lists.newArrayList();
		for(ProtocolFormCommitteeStatus pfcs: pfcssForIRBOFFICE){
			pfcssForIRBOFFICEnums.add(pfcs.getProtocolFormCommitteeStatus());
		}
		for(ProtocolFormCommitteeStatus pfcs: pfcss){
			pfcssForIRBReviewEnums.add(pfcs.getProtocolFormCommitteeStatus());
		}
		
		for(Map.Entry<ProtocolFormCommitteeStatusEnum,ProtocolFormCommitteeStatusEnum> entry : commiteeStatusMap.entrySet() ){
			if(pfcssForIRBOFFICEnums.contains(entry.getKey())&&!pfcssForIRBReviewEnums.contains(entry.getValue())&&pfcss.size()>0){
				ProtocolFormCommitteeStatus pfcs = new ProtocolFormCommitteeStatus();
				int officeStatusIndex = pfcssForIRBOFFICEnums.indexOf(entry.getKey());
				ProtocolFormCommitteeStatus pfcsOffice = pfcssForIRBOFFICE.get(officeStatusIndex);
				pfcs.setRetired(false);
				pfcs.setAction(pfcsOffice.getAction());
				pfcs.setCausedByUserId(pfcsOffice.getCausedByUserId());
				pfcs.setCausedByCommittee(Committee.IRB_REVIEWER);
				pfcs.setCommittee(Committee.IRB_REVIEWER);
				pfcs.setModified(pfcsOffice.getModified());
				pfcs.setProtocolForm(pfcsOffice.getProtocolForm());
				pfcs.setProtocolFormCommitteeStatus(pfcsOffice.getProtocolFormCommitteeStatus());
				protocolFormCommitteeStatusDao.saveOrUpdate(pfcs);
				System.out.println(pfId+"");
			}
		}
		}
	}
	
	
	//@Test
	public void insertRecord(){
		long pfid = 16767;
		ProtocolForm pf = protocolFormDao.findById(pfid);
		List<ProtocolFormStatus> pfss=protocolFormStatusDao.getAllProtocolFormStatusByFormId(pfid);
		 	
		for(ProtocolFormStatus pfs:pfss){
			pfs.setRetired(true);
			logger.debug(pfs.getId()+"");
			protocolFormStatusDao.saveOrUpdate(pfs);
		}
		List<ProtocolFormCommitteeStatus> pfcss = Lists.newArrayList();
		List<ProtocolFormCommitteeStatus> pfcstemp1 = protocolFormCommitteeStatusDao.listAllByCommitteeAndProtocolFormId(Committee.IRB_ASSIGNER, pfid);
		List<ProtocolFormCommitteeStatus> pfcstemp2 = protocolFormCommitteeStatusDao.listAllByCommitteeAndProtocolFormId(Committee.IRB_PREREVIEW, pfid);
		pfcss.addAll(pfcstemp1);
		pfcss.addAll(pfcstemp2);
		
		for(ProtocolFormCommitteeStatus pfcs : pfcss){
			pfcs.setRetired(true);
			protocolFormCommitteeStatusDao.saveOrUpdate(pfcs);
			logger.debug(pfcs.getId()+"");
		}
		
		ProtocolFormStatus newPfs =new ProtocolFormStatus();
		newPfs.setModified(new Date());
		newPfs.setCauseByUser(userDao.findById(158));
		newPfs.setProtocolForm(pf);
		newPfs.setProtocolFormStatus(ProtocolFormStatusEnum.UNDER_COMPLIANCE_REVIEW);
		protocolFormStatusDao.saveOrUpdate(newPfs);
		
		ProtocolFormCommitteeStatus newPfc = new ProtocolFormCommitteeStatus();
		newPfc.setCausedByUserId(158);
		newPfc.setCausedByCommittee(Committee.PI);
		newPfc.setAction("SIGN_SUBMIT");
		newPfc.setCommittee(Committee.COMPLIANCE_REVIEW);
		newPfc.setModified(new Date());
		newPfc.setProtocolForm(pf);
		newPfc.setProtocolFormCommitteeStatus(ProtocolFormCommitteeStatusEnum.IN_REVIEW);
		newPfc.setXmlData("");
		protocolFormCommitteeStatusDao.saveOrUpdate(newPfc);
	}
	//@Test
	public void fixDuplicatedStudiesInBudget() throws IOException, XPathExpressionException, SAXException{
		FileInputStream fstream;
		fstream = new FileInputStream("C:\\Data\\id.txt");
		DataInputStream in = new DataInputStream(fstream);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String strLine;
		List<String> pxfdIds = Lists.newArrayList();
		while ((strLine = br.readLine()) != null) {
			pxfdIds.add(strLine);
		}
		
		//pxfdIds.add("10652");
		
		for(String pfxdidStr :pxfdIds){
			logger.debug(pfxdidStr);
			long pfxdId = Long.valueOf(pfxdidStr);
			ProtocolFormXmlData pfxd = protocolFormXmlDataDao.findById(pfxdId);
			String xml = pfxd.getXmlData();
			
			String answer = xmlProcessor.listElementStringValuesByPath("/protocol/budget/not-duplicate-existing-studies", xml).get(0);
			String explain = xmlHandler.getSingleStringValueByXPath(xml, "/protocol/budget/not-duplicate-existing-studies/explain");
				
			Map<String, Object> resultMap = new HashMap<String, Object>(
							0);
			try {
						resultMap = xmlProcessor.deleteElementByPath(
								"/protocol/budget/not-duplicate-existing-studies", xml);
						xml = resultMap.get("finalXml").toString();
					} catch (Exception e) {
						logger.info("element does not exist.");
					}
			
			
			logger.debug(answer);
			if(answer.equals("y")){
				xml =(String) xmlProcessor.addSubElementToElementIdentifiedByXPath("/protocol/budget", xml, "<duplicate-existing-studies></duplicate-existing-studies>", false).get("finalXml");
				xml=xmlProcessor.replaceOrAddNodeValueByPath("/protocol/budget/duplicate-existing-studies", xml, "n");	
			}else if(answer.equals("n")){
				xml =(String) xmlProcessor.addSubElementToElementIdentifiedByXPath("/protocol/budget", xml, "<duplicate-existing-studies></duplicate-existing-studies>", false).get("finalXml");
				xml=xmlProcessor.replaceOrAddNodeValueByPath("/protocol/budget/duplicate-existing-studies", xml, "y");	
				xml =(String) xmlProcessor.addSubElementToElementIdentifiedByXPath("/protocol/budget/duplicate-existing-studies", xml, "<explain></explain>", false).get("finalXml");
				xml=xmlProcessor.replaceOrAddNodeValueByPath("/protocol/budget/duplicate-existing-studies/explain", xml, explain);	
			}
			
			pfxd.setXmlData(xml);
			protocolFormXmlDataDao.saveOrUpdate(pfxd);
			
			
		}
	}
	
	//@Test
	public void fixMissingDepartmentInfoInProtocol() throws IOException, XPathExpressionException, SAXException{
		FileInputStream fstream;
		fstream = new FileInputStream("C:\\Data\\id.txt");
		DataInputStream in = new DataInputStream(fstream);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String strLine;
		List<String> protocolIds = Lists.newArrayList();
		while ((strLine = br.readLine()) != null) {
			protocolIds.add(strLine);
		}
		
		for(String pidStr :protocolIds){
			long pid = Long.valueOf(pidStr);
			Protocol  p = protocolDao.findById(pid);
			String protocolXml =  p.getMetaDataXml();
			/*if(pid!=103668){
				continue;
			}*/
			
			
			
			try{
			ProtocolFormXmlData pfxd = protocolFormXmlDataDao.getLastProtocolFormXmlDataByProtocolIdAndType(pid, ProtocolFormXmlDataType.MODIFICATION);
			String pfxdXml =  pfxd.getXmlData();
			
			Set<String> paths = Sets.newHashSet();
			paths.add("/protocol/responsible-department");
			//if the protocol does not has this element, add it first
			List<Element> departmentElesForProtocol = xmlProcessor.listDomElementsByPaths(paths, protocolXml);
			try{
			Element departmentEleForProtocol = departmentElesForProtocol.get(0);
			}catch(Exception e){
				logger.debug("no tags!!! Adding");
				protocolXml =(String) xmlProcessor.addSubElementToElementIdentifiedByXPath("/protocol", protocolXml, "<responsible-department></responsible-department>", false).get("finalXml");
			}
			
			List<Element> departmentEles = xmlProcessor.listDomElementsByPaths(paths, pfxdXml);
			Element departmentEle = departmentEles.get(0);
			if(!departmentEle.getAttribute("collegedesc").isEmpty()){
				protocolXml =xmlProcessor.replaceAttributeValueByPathAndAttributeName("/protocol/responsible-department", "collegedesc", protocolXml, departmentEle.getAttribute("collegedesc"));
			}
			if(!departmentEle.getAttribute("collegeid").isEmpty()){
				protocolXml =xmlProcessor.replaceAttributeValueByPathAndAttributeName("/protocol/responsible-department", "collegeid", protocolXml, departmentEle.getAttribute("collegeid"));
			}
			if(!departmentEle.getAttribute("deptdesc").isEmpty()){
				protocolXml =xmlProcessor.replaceAttributeValueByPathAndAttributeName("/protocol/responsible-department", "deptdesc", protocolXml, departmentEle.getAttribute("deptdesc"));
			}
			if(!departmentEle.getAttribute("deptid").isEmpty()){
				protocolXml =xmlProcessor.replaceAttributeValueByPathAndAttributeName("/protocol/responsible-department", "deptid", protocolXml, departmentEle.getAttribute("deptid"));
			}
			if(!departmentEle.getAttribute("subdeptdesc").isEmpty()){
				protocolXml =xmlProcessor.replaceAttributeValueByPathAndAttributeName("/protocol/responsible-department", "subdeptdesc", protocolXml, departmentEle.getAttribute("subdeptdesc"));
			}
			if(!departmentEle.getAttribute("subdeptid").isEmpty()){
				protocolXml =xmlProcessor.replaceAttributeValueByPathAndAttributeName("/protocol/responsible-department", "subdeptid", protocolXml, departmentEle.getAttribute("subdeptid"));
			}
			p.setMetaDataXml(protocolXml);
			protocolDao.saveOrUpdate(p);
			
			}catch(Exception e){
				logger.debug(pidStr);
			}
		}
	}
	
	
	//@Test
	public void getNotMigratedStudies() throws IOException{
		FileInputStream fstream;
		fstream = new FileInputStream("C:\\Data\\id.txt");
		DataInputStream in = new DataInputStream(fstream);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String strLine;
		List<Protocol> protocols = protocolDao.findAll();
		List<Long> pids = Lists.newArrayList();
		for(Protocol p: protocols){
			pids.add(p.getId());
		}
		while ((strLine = br.readLine()) != null) {
			long pid = Long.valueOf(strLine);
			if(pids.contains(pid)){
				continue;
			}
			System.out.println(pid);
		}
	}
	
	//@Test
	public void fixInitialModTagMissing() throws XPathExpressionException, SAXException, IOException{
		String qry ="select id from protocol_form  where id in (select min(id) from protocol_form where retired =0 and protocol_id<200000 and protocol_form_type ='MODIFICATION' and parent_id =id   group by protocol_id) and meta_data_xml.exist('/protocol/initial-mod')=0";
		Query query = em.createNativeQuery(qry);
		List<BigInteger> pfIDs = (List<BigInteger>) query.getResultList();
		logger.debug(pfIDs.size()+"");
		
		for(BigInteger pfIDBig: pfIDs){
			long pfID = pfIDBig.longValue();
			/*if(pfID!=16149){
				continue;
			}*/
			logger.debug(pfID+"");
			ProtocolForm pf = protocolFormDao.findById(pfID);
			String xmlData = pf.getMetaDataXml();
			xmlData = xmlProcessor.replaceOrAddNodeValueByPath("/protocol/initial-mod", xmlData, "y");
			pf.setMetaDataXml(xmlData);
			protocolFormDao.saveOrUpdate(pf);
		}
	}

	//copy existing accural goal local from form_xml_data to form and protocol
	//@Test
	public void copyAccuralGoal() throws IOException, XPathExpressionException, SAXException{
		/*select distinct  ps.protocol_form_id from protocol_form_status ps, protocol_form pf where ps.id in (select max(id) from protocol_form_status where retired =0 group by protocol_form_id)and  
		ps.protocol_form_status in ('EXEMPT_APPROVED','IRB_APPROVED','IRB_ACKNOWLEDGED','EXPEDITED_APPROVED') 
		and ps.protocol_form_id =pf.id and pf.protocol_form_type ='CONTINUING_REVIEW' and pf.retired =0*/
		FileInputStream fstream;
		fstream = new FileInputStream("C:\\Data\\id.txt");
		DataInputStream in = new DataInputStream(fstream);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String strLine;
		while ((strLine = br.readLine()) != null) {
			long pfID = Long.valueOf(strLine);
			ProtocolForm pf = protocolFormDao.findById(pfID);
			ProtocolFormXmlData pfdx = protocolFormXmlDataDao.getLastProtocolFormXmlDataByProtocolFormIdAndType(pfID, ProtocolFormXmlDataType.CONTINUING_REVIEW);
			String accrulLocal= xmlHandler.getSingleStringValueByXPath(pfdx.getXmlData(), "/continuing-review/subject-accrual/enrollment/local/since-approval");
			if(accrulLocal ==null||accrulLocal.isEmpty()){
				continue;
			}
			
			String pfXml = pf.getMetaDataXml();
			pfXml= xmlProcessor.replaceOrAddNodeValueByPath("/continuing-review/summary/irb-determination/subject-accrual/enrollment/local/since-approval", pfXml, accrulLocal);
			pf.setMetaDataXml(pfXml);
			protocolFormDao.saveOrUpdate(pf);
			
			Protocol p = pf.getProtocol();
			String pXml = p.getMetaDataXml();
			pXml = xmlProcessor.replaceOrAddNodeValueByPath("/protocol/summary/irb-determination/subject-accrual/enrollment/local/since-approval", pXml, accrulLocal);
			p.setMetaDataXml(pXml);
			protocolDao.saveOrUpdate(p);
		}
	}
	
	
	//@Test
	public void addPharmsyCreatedAgg() throws IOException, XPathExpressionException, SAXException{
		FileInputStream fstream;
		fstream = new FileInputStream("C:\\Data\\id.txt");
		DataInputStream in = new DataInputStream(fstream);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String strLine;
		List<String> Ids = Lists.newArrayList();
		while ((strLine = br.readLine()) != null) {
			long pfxdId = Long.valueOf(strLine);
			ProtocolFormXmlData pfxd = protocolFormXmlDataDao.findById(pfxdId);
			String xmlData = pfxd.getXmlData();
			xmlData=xmlProcessor.replaceOrAddNodeValueByPath("/protocol/pharmacy-created", xmlData, "y");
			pfxd.setXmlData(xmlData);
			protocolFormXmlDataDao.saveOrUpdate(pfxd);
		}
			
	}
	
	//@Test
	public void addPharmacyRequest()throws IOException, XPathExpressionException, SAXException{
		FileInputStream fstream;
		fstream = new FileInputStream("C:\\Data\\latestformList.txt");
		DataInputStream in = new DataInputStream(fstream);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String strLine;
		List<String> formIds = Lists.newArrayList();
		while ((strLine = br.readLine()) != null) {
			formIds.add(strLine);
		}
		
		for(String formIdStr : formIds){
			long formId = Long.valueOf(formIdStr);
			logger.debug(formIdStr);
			ProtocolForm pf = protocolFormDao.findById(formId);
			String xmlData = pf.getProtocol().getMetaDataXml();
			
			String piID = xmlHandler
					.getSingleStringValueByXPath(
							xmlData,
							"/protocol/staffs/staff/user[roles/role/text()=\"Principal Investigator\"]/@id");
			
			ProtocolFormCommitteeStatus protocolFormCommitteeStatus = new ProtocolFormCommitteeStatus();
			protocolFormCommitteeStatus.setAction("REQUEST_REVIEW");
			protocolFormCommitteeStatus.setRetired(false);
			protocolFormCommitteeStatus.setCommittee(Committee.PHARMACY_REVIEW);
			protocolFormCommitteeStatus.setModified(new Date());
			protocolFormCommitteeStatus.setProtocolFormCommitteeStatus(ProtocolFormCommitteeStatusEnum.IN_REVIEW_REQUESTED);
			protocolFormCommitteeStatus.setProtocolForm(pf);
			protocolFormCommitteeStatus.setCausedByCommittee(Committee.PI);
			protocolFormCommitteeStatus.setCausedByUserId(Long.valueOf(piID));
			
			protocolFormCommitteeStatusDao.saveOrUpdate(protocolFormCommitteeStatus);
		}
	}
	
	//@Test
	public void addRolesAndResponsibilityToUsers()throws IOException, XPathExpressionException, SAXException{
		FileInputStream fstream;
		fstream = new FileInputStream("C:\\Data\\protocol.txt");
		DataInputStream in = new DataInputStream(fstream);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String strLine;
		List<String> protocolIds = Lists.newArrayList();
		while ((strLine = br.readLine()) != null) {
			protocolIds.add(strLine);
		}
		
		fstream = new FileInputStream("C:\\Data\\protocolForm.txt");
		 in = new DataInputStream(fstream);
		 br = new BufferedReader(new InputStreamReader(in));
		String strLine2;
		List<String> formList = Lists.newArrayList();
		while ((strLine2 = br.readLine()) != null) {
			formList.add(strLine2);
		}
		
		fstream = new FileInputStream("C:\\Data\\protocolFormMetaData.txt");
		 in = new DataInputStream(fstream);
		 br = new BufferedReader(new InputStreamReader(in));
		String strLine3;
		List<String> metaDataList = Lists.newArrayList();
		while ((strLine3 = br.readLine()) != null) {
			metaDataList.add(strLine3);
		}
		
		Set<String> rolePaths = Sets.newHashSet();
		rolePaths.add("//staffs/staff/user[@id=\"763\"]/roles/role");
		Set<String> respPaths = Sets.newHashSet();
		respPaths.add("//staffs/staff/user[@id=\"763\"]/reponsibilities/responsibility");
		String staffid = "";
		for(String pidStr: protocolIds){
			long pid = Long.valueOf(pidStr);
			Protocol p = protocolDao.findById(pid);
			String xmlData = p.getMetaDataXml();
			logger.debug(pidStr);
			//check if the user is one staffList
			String lastname="";
			lastname = xmlHandler.getSingleStringValueByXPath(xmlData,"//staffs/staff/user[@id=\"763\"]/lastname");
			if(lastname.isEmpty()){
				staffid = UUID.randomUUID().toString();
				String elementStr  ="<staff id=\""+staffid+"\"> <user id=\"763\" phone=\"\" pi_serial=\"13937\" sap=\"7169\"><lastname>Myrick</lastname><firstname>Rebecca</firstname><email>MyrickRebeccaS@uams.edu</email><roles><role>Budget Manager</role></roles><reponsibilities><responsibility>Budget Manager</responsibility><responsibility>Managing CLARA submission</responsibility></reponsibilities><conflict-of-interest>false</conflict-of-interest></user><notify>true</notify></staff>";
				xmlData =(String) xmlProcessor.addSubElementToElementIdentifiedByXPath("//staffs", xmlData, elementStr, false).get("finalXml");
			}else{
				
			xmlData = xmlProcessor.replaceOrAddNodeValueByPath("//staffs/staff[user[@id=\"763\"]]/notify", xmlData, "true");
			
			List<Element> roleEles = xmlProcessor.listDomElementsByPaths(rolePaths, xmlData);
			List<Element> respEles = xmlProcessor.listDomElementsByPaths(respPaths, xmlData);
			boolean roleExist = false;
			boolean resp1Exisit = false;
			boolean resp2Exisit = false;
			for(Element roleEle : roleEles){
				if(roleEle.getTextContent().toLowerCase().contains("budget manager")){
					roleExist= true;
					break;
				}
			}
			for(Element respEle :respEles){
				if(respEle.getTextContent().toLowerCase().contains("budget manager")){
					resp1Exisit= true;
				}
				if(respEle.getTextContent().toLowerCase().contains("managing clara submission")){
					resp2Exisit= true;
				}
			}
			if(!roleExist){
				xmlData =(String) xmlProcessor.addSubElementToElementIdentifiedByXPath("//staffs/staff/user[@id=\"763\"]/roles", xmlData, "<role>Budget Manager</role>", false).get("finalXml");
				
			}
			if(!resp1Exisit){
				try{
					xmlData =(String) xmlProcessor.addSubElementToElementIdentifiedByXPath("//staffs/staff/user[@id=\"763\"]/reponsibilities", xmlData, "<responsibility>Budget Manager</responsibility>", false).get("finalXml");
				}catch(Exception e){
					xmlData =(String) xmlProcessor.addSubElementToElementIdentifiedByXPath("//staffs/staff/user[@id=\"763\"]", xmlData, "<reponsibilities><responsibility>Budget Manager</responsibility></reponsibilities>", false).get("finalXml");

				}
			}
			if(!resp2Exisit){
				xmlData =(String) xmlProcessor.addSubElementToElementIdentifiedByXPath("//staffs/staff/user[@id=\"763\"]/reponsibilities", xmlData, "<responsibility>Managing CLARA submission</responsibility>", false).get("finalXml");
			}
			}
			p.setMetaDataXml(xmlData);
			protocolDao.saveOrUpdate(p);
		}
		
		//protocolForm
	/*	for(String pfidStr: formList){
			long pfid = Long.valueOf(pfidStr);
			ProtocolForm pf = protocolFormDao.findById(pfid);
			String xmlData = pf.getMetaDataXml();
			
			//check if the user is one staffList
			String lastname="";
			lastname = xmlHandler.getSingleStringValueByXPath(xmlData,"//staffs/staff/user[@id=\"763\"]/lastname");
			if(lastname.isEmpty()){
				String elementStr  ="<staff id=\""+staffid+"\"> <user id=\"763\" phone=\"\" pi_serial=\"13937\" sap=\"7169\"><lastname>Myrick</lastname><firstname>Rebecca</firstname><email>MyrickRebeccaS@uams.edu</email><roles><role>Budget Manager</role></roles><reponsibilities><responsibility>Budget Manager</responsibility><responsibility>Managing CLARA submission</responsibility></reponsibilities><conflict-of-interest>false</conflict-of-interest></user><notify>true</notify></staff>";
				xmlData =(String) xmlProcessor.addSubElementToElementIdentifiedByXPath("//staffs", xmlData, elementStr, false).get("finalXml");
			}else{
			xmlData = xmlProcessor.replaceOrAddNodeValueByPath("//staffs/staff[user[@id=\"763\"]]/notify", xmlData, "true");
			List<Element> roleEles = xmlProcessor.listDomElementsByPaths(rolePaths, xmlData);
			List<Element> respEles = xmlProcessor.listDomElementsByPaths(respPaths, xmlData);
			boolean roleExist = false;
			boolean resp1Exisit = false;
			boolean resp2Exisit = false;
			for(Element roleEle : roleEles){
				if(roleEle.getTextContent().toLowerCase().contains("budget manager")){
					roleExist= true;
					break;
				}
			}
			for(Element respEle :respEles){
				if(respEle.getTextContent().toLowerCase().contains("budget manager")){
					resp1Exisit= true;
				}
				if(respEle.getTextContent().toLowerCase().contains("managing clara submission")){
					resp2Exisit= true;
				}
			}
			if(!roleExist){
				xmlData =(String) xmlProcessor.addSubElementToElementIdentifiedByXPath("//staffs/staff/user[@id=\"763\"]/roles", xmlData, "<role>Budget Manager</role>", false).get("finalXml");
				
			}
			if(!resp1Exisit){
				try{
					xmlData =(String) xmlProcessor.addSubElementToElementIdentifiedByXPath("//staffs/staff/user[@id=\"763\"]/reponsibilities", xmlData, "<responsibility>Budget Manager</responsibility>", false).get("finalXml");
				}catch(Exception e){
					xmlData =(String) xmlProcessor.addSubElementToElementIdentifiedByXPath("//staffs/staff/user[@id=\"763\"]", xmlData, "<reponsibilities><responsibility>Budget Manager</responsibility></reponsibilities>", false).get("finalXml");

				}			}
			if(!resp2Exisit){
				xmlData =(String) xmlProcessor.addSubElementToElementIdentifiedByXPath("//staffs/staff/user[@id=\"763\"]/reponsibilities", xmlData, "<responsibility>Managing CLARA submission</responsibility>", false).get("finalXml");
			}}
			pf.setMetaDataXml(xmlData);
			protocolFormDao.saveOrUpdate(pf);
		}
		
		//protocolFormXmlData
		for(String pfxdidStr: metaDataList){
			long pfxdid = Long.valueOf(pfxdidStr);
			ProtocolFormXmlData pfxd = protocolFormXmlDataDao.findById(pfxdid);
			String xmlData = pfxd.getXmlData();
			//check if the user is one staffList
			String lastname="";
			lastname = xmlHandler.getSingleStringValueByXPath(xmlData,"//staffs/staff/user[@id=\"763\"]/lastname");
			if(lastname.isEmpty()){
				String elementStr  ="<staff id=\""+staffid+"\"> <user id=\"763\" phone=\"\" pi_serial=\"13937\" sap=\"7169\"><lastname>Myrick</lastname><firstname>Rebecca</firstname><email>MyrickRebeccaS@uams.edu</email><roles><role>Budget Manager</role></roles><reponsibilities><responsibility>Budget Manager</responsibility><responsibility>Managing CLARA submission</responsibility></reponsibilities><conflict-of-interest>false</conflict-of-interest></user><notify>true</notify></staff>";
				xmlData =(String) xmlProcessor.addSubElementToElementIdentifiedByXPath("//staffs", xmlData, elementStr, false).get("finalXml");
			}else{
			xmlData = xmlProcessor.replaceOrAddNodeValueByPath("//staffs/staff[user[@id=\"763\"]]/notify", xmlData, "true");
			List<Element> roleEles = xmlProcessor.listDomElementsByPaths(rolePaths, xmlData);
			List<Element> respEles = xmlProcessor.listDomElementsByPaths(respPaths, xmlData);
			boolean roleExist = false;
			boolean resp1Exisit = false;
			boolean resp2Exisit = false;
			for(Element roleEle : roleEles){
				if(roleEle.getTextContent().toLowerCase().contains("budget manager")){
					roleExist= true;
					break;
				}
			}
			for(Element respEle :respEles){
				if(respEle.getTextContent().toLowerCase().contains("budget manager")){
					resp1Exisit= true;
				}
				if(respEle.getTextContent().toLowerCase().contains("managing clara submission")){
					resp2Exisit= true;
				}
			}
			if(!roleExist){
				xmlData =(String) xmlProcessor.addSubElementToElementIdentifiedByXPath("//staffs/staff/user[@id=\"763\"]/roles", xmlData, "<role>Budget Manager</role>", false).get("finalXml");
				
			}
			if(!resp1Exisit){
				try{
					xmlData =(String) xmlProcessor.addSubElementToElementIdentifiedByXPath("//staffs/staff/user[@id=\"763\"]/reponsibilities", xmlData, "<responsibility>Budget Manager</responsibility>", false).get("finalXml");
				}catch(Exception e){
					xmlData =(String) xmlProcessor.addSubElementToElementIdentifiedByXPath("//staffs/staff/user[@id=\"763\"]", xmlData, "<reponsibilities><responsibility>Budget Manager</responsibility></reponsibilities>", false).get("finalXml");

				}			}
			if(!resp2Exisit){
				xmlData =(String) xmlProcessor.addSubElementToElementIdentifiedByXPath("//staffs/staff/user[@id=\"763\"]/reponsibilities", xmlData, "<responsibility>Managing CLARA submission</responsibility>", false).get("finalXml");
			}}
			pfxd.setXmlData(xmlData);
			protocolFormXmlDataDao.saveOrUpdate(pfxd);
		}*/
	}
	
	//@Test
	public void hasDrugorDevices() throws IOException{
		FileInputStream fstream;
		fstream = new FileInputStream("C:\\Data\\drugs.txt");
		DataInputStream in = new DataInputStream(fstream);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String strLine;
		List<String> drugsList = Lists.newArrayList();
		while ((strLine = br.readLine()) != null) {
			drugsList.add(strLine);
		}
		
		fstream = new FileInputStream("C:\\Data\\devices.txt");
		 in = new DataInputStream(fstream);
		 br = new BufferedReader(new InputStreamReader(in));
		String strLine2;
		List<String> devicesList = Lists.newArrayList();
		while ((strLine2 = br.readLine()) != null) {
			devicesList.add(strLine2);
		}
		
		fstream = new FileInputStream("C:\\Data\\whole.txt");
		 in = new DataInputStream(fstream);
		 br = new BufferedReader(new InputStreamReader(in));
		String strLine3;
		List<String> testList = Lists.newArrayList();
		while ((strLine3 = br.readLine()) != null) {
			testList.add(strLine3);
		}
		/*for(String id :devicesList){
			System.out.println(id);
		}*/
		for(String id :testList){
			if(devicesList.contains(id))
				System.out.println("yes");
			else{
				System.out.println("no");
			}
		}
	}
	
	//@Test
	public void fixSummaryQuestionAnswerCasesensitive() {
		List<String> pathsList = Lists.newArrayList();
		pathsList.add("//summary/irb-determination/fda");
		pathsList.add("//summary/irb-determination/consent-waived");
		pathsList
				.add("//summary/irb-determination/consent-document-waived");
		pathsList.add("//summary/irb-determination/hipaa-applicable");
		pathsList.add("//summary/irb-determination/hipaa-waived");
		pathsList.add("//summary/irb-determination/reportable-to-ohrp");
		pathsList.add("//summary/irb-determination/non-compliance-assessment");
		pathsList
				.add("//summary/irb-determination/suggested-next-review-type");

		/*List<Protocol> protocolList = Lists.newArrayList();
		protocolList= protocolDao.findAll();*/
		List<ProtocolForm> formList = Lists.newArrayList();
		formList = protocolFormDao.findAll();
		//protocolList.add(protocolDao.findById(202034));
		for (ProtocolForm p : formList) {
			String xml = p.getMetaDataXml();

			for (String path : pathsList) {
				try {
					String value = xmlHandler.getSingleStringValueByXPath(xml,
							path);
					
					if(path.equals("//summary/irb-determination/suggested-next-review-type") ){
						if (value.toLowerCase().equals("expedited")) {
							xml=xmlProcessor.replaceOrAddNodeValueByPath(path, xml,
									"expedited".toUpperCase());
						}else if(value.toLowerCase().equals("exempt")) {
							xml=xmlProcessor.replaceOrAddNodeValueByPath(path, xml,
									"exempt".toUpperCase());
						}else if(value.toLowerCase().equals("full-board")) {
							xml=xmlProcessor.replaceOrAddNodeValueByPath(path, xml,
									"FULL_BOARD");
						}
					}
					else{
					
					if (value.toLowerCase().equals("yes")) {
						xml=xmlProcessor.replaceOrAddNodeValueByPath(path, xml,
								"yes");
					}else if(value.toLowerCase().equals("no")) {
						xml=xmlProcessor.replaceOrAddNodeValueByPath(path, xml,
								"no");
					}else if(value.toLowerCase().equals("na")) {
						xml=xmlProcessor.replaceOrAddNodeValueByPath(path, xml,
								"na");
					}
					}
					
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			p.setMetaDataXml(xml);
			protocolFormDao.saveOrUpdate(p);

		}

	}

	// @Test
	public void fixAuditRisAndPedRisk() {
		Map<Integer, String> pedRiskMap = Maps.newHashMap();
		pedRiskMap.put(0, "RISK_PED_NA");
		pedRiskMap.put(1, "RISK_PED_1");
		pedRiskMap.put(2, "RISK_PED_2");
		pedRiskMap.put(3, "RISK_PED_3");
		pedRiskMap.put(4, "RISK_PED_4");

		Map<String, String> auditRiskMap = Maps.newHashMap();
		auditRiskMap.put("Minimal".toLowerCase(), "RISK_ADULT_1");
		auditRiskMap.put("Greater than minimal".toLowerCase(), "RISK_ADULT_2");
		auditRiskMap.put("N/A", "RISK_ADULT_NA");

		List<Protocol> protocolLst = Lists.newArrayList();
		// protocolLst.add(protocolDao.findById(113385));
		protocolLst = protocolDao.findAll();

		for (Protocol p : protocolLst) {
			try {
				String protocolMeta = p.getMetaDataXml();
				String pedRisk = xmlHandler.getSingleStringValueByXPath(
						protocolMeta,
						"/protocol/summary/irb-determination/ped-risk");
				String auditRisk = xmlHandler.getSingleStringValueByXPath(
						protocolMeta,
						"/protocol/summary/irb-determination/adult-risk");
				try {
					float pedRiskValue = Float.valueOf(pedRisk);
					int pedKey = (int) pedRiskValue;
					if (pedRiskMap.containsKey(pedKey)) {
						protocolMeta = xmlProcessor
								.replaceOrAddNodeValueByPath(
										"/protocol/summary/irb-determination/ped-risk",
										protocolMeta, pedRiskMap.get(pedKey));
					}
				} catch (Exception e) {
					// e.printStackTrace();
					// donothing
				}

				try {
					if (auditRiskMap.containsKey(auditRisk.toLowerCase())) {
						protocolMeta = xmlProcessor
								.replaceOrAddNodeValueByPath(
										"/protocol/summary/irb-determination/adult-risk",
										protocolMeta, auditRiskMap
												.get(auditRisk.toLowerCase()));
					}
				} catch (Exception e) {
					e.printStackTrace();
					// donothing
				}
				p.setMetaDataXml(protocolMeta);
				protocolDao.saveOrUpdate(p);
			} catch (Exception e) {
				logger.debug("Error Protocol: " + p.getId());
				e.printStackTrace();
			}
		}
	}

	// @Test
	public void removeCreatedBudgetTag() {
		List<Protocol> protocolLst = Lists.newArrayList();
		// protocolLst.add(protocolDao.findById(201868));
		protocolLst = protocolDao.findAll();
		for (Protocol p : protocolLst) {
			try {
				String protocolMeta = p.getMetaDataXml();
				String hasbudgetValue = xmlHandler.getSingleStringValueByXPath(
						protocolMeta, "/protocol/extra/has-budget-or-not");
				String siteValue = xmlHandler.getSingleStringValueByXPath(
						protocolMeta, "/protocol/site-responsible");

				if (!hasbudgetValue.equals("y") || !siteValue.equals("uams")) {
					// remove tag
					Map<String, Object> resultMap = new HashMap<String, Object>(
							0);
					try {
						resultMap = xmlProcessor.deleteElementByPath(
								"/protocol/budget-created", protocolMeta);
						protocolMeta = resultMap.get("finalXml").toString();
					} catch (Exception e) {
						logger.info("element does not exist.");
					}

					p.setMetaDataXml(protocolMeta);
					protocolDao.saveOrUpdate(p);
				}
			} catch (Exception e) {
				logger.debug("protocol: " + p.getId());
			}
		}

		List<ProtocolForm> formList = Lists.newArrayList();

		// formList.add(protocolFormDao.findById(11494));
		formList = protocolFormDao.findAll();
		for (ProtocolForm pf : formList) {
			try {
				String pfMeta = pf.getMetaDataXml();
				String hasbudgetValue = "";
				hasbudgetValue = xmlHandler.getSingleStringValueByXPath(pfMeta,
						"/protocol/extra/has-budget-or-not");
				String siteValue = "";
				siteValue = xmlHandler.getSingleStringValueByXPath(pfMeta,
						"/protocol/site-responsible");

				if (!hasbudgetValue.equals("y") || !siteValue.equals("uams")) {
					// remove tag
					Map<String, Object> resultMap = new HashMap<String, Object>(
							0);
					try {
						resultMap = xmlProcessor.deleteElementByPath(
								"/protocol/budget-created", pfMeta);
						pfMeta = resultMap.get("finalXml").toString();
					} catch (Exception e) {
						logger.info("element does not exist.");
					}

					pf.setMetaDataXml(pfMeta);
					protocolFormDao.saveOrUpdate(pf);
				}
			} catch (Exception e) {
				logger.debug("protocolform: " + pf.getId());
			}
		}
	}

	// @Test
	public void hippaFixTest() {
		List<Protocol> protocolLst = protocolDao.listProtocolsByIdRange(0,
				200000);
		// List<Protocol> protocolLst = Lists.newArrayList();
		// protocolLst.add(protocolDao.findById(61004));

		for (Protocol p : protocolLst) {
			logger.debug("protocol Id: " + p.getId());
			String protocolMeta = p.getMetaDataXml();
			// see if modification existed
			List<ProtocolForm> forms = Lists.newArrayList();
			boolean modificationAcknowledged = false;
			try {
				forms = protocolFormDao
						.listProtocolFormsByProtocolIdAndProtocolFormType(
								p.getId(), ProtocolFormType.MODIFICATION);
				for (ProtocolForm form : forms) {
					/*
					 * List<ProtocolFormStatus> statusList =
					 * protocolFormStatusDao
					 * .getAllProtocolFormStatusByFormId(form.getFormId());
					 * logger.debug(""+statusList.size());
					 */
					ProtocolFormStatus formStatus = protocolFormStatusDao
							.getLatestProtocolFormStatusByFormId(form
									.getFormId());
					if (formStatus.getProtocolFormStatus().equals(
							ProtocolFormStatusEnum.IRB_ACKNOWLEDGED)) {
						modificationAcknowledged = true;
						break;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (modificationAcknowledged == true) {
				// initial modification acknowledged
				try {
					Map<String, Object> resultMap = new HashMap<String, Object>(
							0);
					try {
						resultMap = xmlProcessor
								.deleteElementByPath(
										"/protocol/summary/irb-determination/hipaa-not-applicable",
										protocolMeta);
						protocolMeta = resultMap.get("finalXml").toString();
					} catch (Exception e) {
						// logger.info("hipaa-not-applicable element does not exist.");
					}

					String hippavalue = "no";
					// get hippavalue from question
					Set<String> paths = Sets.newHashSet();
					paths.add("/protocol/hipaa/is-phi-obtained");
					paths.add("/protocol/hipaa/access-existing-phi");
					String phiObtained = "";
					String phiExisting = "";
					Map<String, List<String>> hipaaQuestionValues = xmlProcessor
							.listElementStringValuesByPaths(paths, protocolMeta);
					try {
						phiObtained = hipaaQuestionValues.get(
								"/protocol/hipaa/is-phi-obtained").get(0);
					} catch (Exception e) {

					}
					try {
						phiExisting = hipaaQuestionValues.get(
								"/protocol/hipaa/access-existing-phi").get(0);
					} catch (Exception e) {

					}

					if (phiObtained.equals("y") || phiExisting.equals("y")) {
						hippavalue = "yes";
					}

					protocolMeta = xmlProcessor
							.replaceOrAddNodeValueByPath(
									"/protocol/summary/irb-determination/hipaa-applicable",
									protocolMeta, hippavalue);

					p.setMetaDataXml(protocolMeta);
					protocolDao.saveOrUpdate(p);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				// no modification has been made
				try {
					// remove hippa-not-applicable tag
					Map<String, Object> resultMap = new HashMap<String, Object>(
							0);
					try {
						resultMap = xmlProcessor
								.deleteElementByPath(
										"/protocol/summary/irb-determination/hipaa-not-applicable",
										protocolMeta);
						protocolMeta = resultMap.get("finalXml").toString();
					} catch (Exception e) {
						// logger.info("hipaa-not-applicable element does not exist.");
					}

					String hippavalue = xmlHandler.getSingleStringValueByXPath(
							protocolMeta,
							"/protocol/summary/irb-determination/hipaa-waived");

					protocolMeta = xmlProcessor
							.replaceOrAddNodeValueByPath(
									"/protocol/summary/irb-determination/hipaa-applicable",
									protocolMeta, hippavalue);

					p.setMetaDataXml(protocolMeta);
					protocolDao.saveOrUpdate(p);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	// @Test
	public void addSapIdBack() {
		List<Protocol> protocols = Lists.newArrayList();
		// protocols.add(protocolDao.findById(131390));
		protocols = protocolDao.findAll();
		for (Protocol p : protocols) {
			try {
				String xmlData = p.getMetaDataXml();
				Set<String> paths = Sets.newHashSet();
				paths.add("/protocol/staffs/staff/user");
				List<Element> userEles = xmlProcessor.listDomElementsByPaths(
						paths, xmlData);
				for (Element userElement : userEles) {
					User user = new User();
					String sap = "";
					String userIdStr = userElement.getAttribute("id");
					if (userIdStr.isEmpty() || userIdStr == null
							|| userIdStr.equals("0")) {
						try {
							Node emailNode = userElement.getElementsByTagName(
									"Email").item(0);
							String email = emailNode.getTextContent();
							user = userService.getUserByEmail(email);
							sap = user.getPerson().getSap();
							// if no sap, ignore this user
							if (sap == null || sap.trim().isEmpty()
									|| sap.equals("null")) {
								continue;
							}
							xmlData = xmlProcessor
									.replaceAttributeValueByPathAndAttributeName(
											"/protocol/staffs/staff/user[email/text()=\""
													+ email + "\"]", "sap",
											xmlData, sap);
						} catch (Exception e) {
							// do nothing
						}
					} else {
						long userId = Long.valueOf(userIdStr);
						user = userService.getUserByUserId(userId);
						sap = user.getPerson().getSap();
						// if no sap, ignore this user
						if (sap == null || sap.trim().isEmpty()
								|| sap.equals("null")) {
							continue;
						}
						xmlData = xmlProcessor
								.replaceAttributeValueByPathAndAttributeName(
										"/protocol/staffs/staff/user[@id=\""
												+ userId + "\"]", "sap",
										xmlData, sap);

					}

				}
				p.setMetaDataXml(xmlData);
				protocolDao.saveOrUpdate(p);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	// @Test
	public void addTagInProtocolXml() throws IOException {
		FileInputStream fstream;
		fstream = new FileInputStream("C:\\Data\\pushed.txt");
		DataInputStream in = new DataInputStream(fstream);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String strLine;
		while ((strLine = br.readLine()) != null) {
			try {
				Protocol p = protocolDao.findById(Long.valueOf(strLine));
				String xmlData = p.getMetaDataXml();
				// xmlData =
				// xmlProcessor.replaceOrAddNodeValueByPath("/protocol/pushed-to-epic-date",
				// xmlData, "07/12/2013");
				xmlData = xmlProcessor.replaceOrAddNodeValueByPath(
						"/protocol/pushed-to-epic-date", xmlData, "07/30/2013");
				p.setMetaDataXml(xmlData);
				protocolDao.saveOrUpdate(p);
				// logger.debug("success: "+strLine);
			} catch (Exception e) {
				logger.debug("error: " + strLine);
			}
		}

	}

	// remove {} from logs
	// @Test
	public void removeErrorTagInLogs() {
		String qry = "SELECT id from track  where xml_data.exist('/logs/log/text()[contains(., \"{\") and contains(., \"}\")]')=1 and ref_object_id in(select id from protocol where retired =0)";
		Query query = em.createNativeQuery(qry);
		List<BigInteger> trackIds = Lists.newArrayList();
		trackIds = (List<BigInteger>) query.getResultList();
		// trackIds.add(new BigInteger("13443"));
		for (BigInteger trackId : trackIds) {
			logger.debug(trackId.toString());
			Track track = trackDao.findById(trackId.longValue());
			String logXml = track.getXmlData();
			String regex = "\\{.*?\\}";
			logXml = logXml.replaceAll(regex, "");
			track.setXmlData(logXml);
			trackDao.saveOrUpdate(track);
		}

	}

	// @Test
	public void testDuplicatedUserId() {
		List<Protocol> protocols = protocolDao.findAll();
		for (Protocol p : protocols) {
			try {

				String xml = p.getMetaDataXml();

				List<String> userIdList = Lists.newArrayList();
				Set<String> paths = Sets.newHashSet();
				paths.add("/protocol/staffs/staff/user");
				List<Element> userEles = xmlProcessor.listDomElementsByPaths(
						paths, xml);
				for (Element user : userEles) {
					String id = "";
					id = user.getAttribute("id");
					if (id.isEmpty() || id.equals("0")) {
						continue;
					}
					if (userIdList.contains(id)) {
						logger.debug(p.getId() + " " + id);
					}
					userIdList.add(id);

				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	// replace errorcoding in logs
	// @Test
	public void test() {
		List<Track> protocolFormTracks = trackDao.ListTracksByType("PROTOCOL");
		for (Track track : protocolFormTracks) {
			String xml = track.getXmlData();

			if (xml.contains("PI (&amp;lt;")) {
				xml = xml.replace("amp;lt;", "lt;");
				xml = xml.replace("amp;gt;", "gt;");
			}
			track.setXmlData(xml);
			trackDao.saveOrUpdate(track);
		}

	}

	// @Test
	public void fixMissingPIInfoInLog() throws XPathExpressionException,
			SAXException, IOException {
		List<String> eventTypes = new ArrayList<String>();
		eventTypes.add("NEW_SUBMISSION_SUBMITTED_TO_ACH_GATEKEEPER");
		eventTypes.add("STUDY_SUBMITTED_TO_STAFFS");
		eventTypes.add("NEW_SUBMISSION_SUBMITTED_TO_GATEKEEPER");
		eventTypes.add("NEW_SUBMISSION_SUBMITTED_TO_BUDGET_MANAGER");
		eventTypes.add("NEW_PROTOCOL_SUBMITTED_TO_IRB");
		eventTypes.add("EMERGENCY_USE_SUBMITTED_TO_IRB_OFFICE");

		List<Track> protocolFormTracks = trackDao.ListTracksByType("PROTOCOL");

		for (Track track : protocolFormTracks) {
			String logXml = track.getXmlData();
			Set<String> pathSet = new HashSet<String>();
			pathSet.add("/logs/log");
			List<Element> logElements = xmlProcessor.listDomElementsByPaths(
					pathSet, logXml);
			for (Element logEle : logElements) {
				String eventType = logEle.getAttribute("event-type");
				String logIDStr = logEle.getAttribute("id");
				// if belong the listed event types
				if (eventTypes.contains(eventType)) {
					String logString = logEle.getTextContent();
					if (!logString.contains("PI (")) {
						// add Pi info
						String formIDStr = logEle.getAttribute("form-id");
						if (!formIDStr.isEmpty() && !formIDStr.equals("0")) {
							long formID = Long.valueOf(formIDStr);

							String porocolFormMetaData = protocolFormDao
									.findById(formID).getMetaDataXml();
							String piEmail = xmlHandler
									.getSingleStringValueByXPath(
											porocolFormMetaData,
											"/protocol/staffs/staff/user[roles/role/text()=\"Principal Investigator\"]/email");
							if (piEmail.isEmpty()) {
								piEmail = xmlHandler
										.getSingleStringValueByXPath(
												porocolFormMetaData,
												"/protocol/staffs/staff/user[roles/role/text()=\"principal investigator\"]/email");
							}
							String pifn = xmlHandler
									.getSingleStringValueByXPath(
											porocolFormMetaData,
											"/protocol/staffs/staff/user[roles/role/text()=\"Principal Investigator\"]/firstname");
							if (pifn.isEmpty()) {
								pifn = xmlHandler
										.getSingleStringValueByXPath(
												porocolFormMetaData,
												"/protocol/staffs/staff/user[roles/role/text()=\"principal investigator\"]/firstname");
							}
							String piln = xmlHandler
									.getSingleStringValueByXPath(
											porocolFormMetaData,
											"/protocol/staffs/staff/user[roles/role/text()=\"Principal Investigator\"]/lastname");
							if (piln.isEmpty()) {
								piln = xmlHandler
										.getSingleStringValueByXPath(
												porocolFormMetaData,
												"/protocol/staffs/staff/user[roles/role/text()=\"principal investigator\"]/lastname");
							}

							if (logString.contains("PI")) {
								String piInfo = "PI (&lt;a href=\"mailto:"
										+ piEmail + "\"&gt;" + piln + ", "
										+ pifn + "&lt;/a&gt;)";
								logString = logString.replace("PI", piInfo);
							} else if (!logString.contains("PI")
									&& logString
											.contains("Study has been submitted")) {
								String piInfo = "PI (&lt;a href=\"mailto:"
										+ piEmail + "\"&gt;" + piln + ", "
										+ pifn + "&lt;/a&gt;)";
								logString = logString
										.replace("Study has been submitted",
												"Study has been submitted by "
														+ piInfo);
							} else if (!logString.contains("PI")
									&& logString
											.contains("Emergency Use has been submitted to IRB Office for review")) {
								String piInfo = "PI (&lt;a href=\"mailto:"
										+ piEmail + "\"&gt;" + piln + ", "
										+ pifn + "&lt;/a&gt;)";
								logString = logString
										.replace(
												"Emergency Use has been submitted to IRB Office for review",
												"Emergency Use has been submitted to IRB Office for review by "
														+ piInfo);
							}

						}
					}
					// replace the study with exact formtype
					if (logString.toLowerCase().contains("study")) {
						String formIDStr = logEle.getAttribute("form-id");
						if (!formIDStr.isEmpty() && !formIDStr.equals("0")) {
							Long formID = Long.valueOf(formIDStr);
							ProtocolForm prf = protocolFormDao.findById(formID);
							ProtocolFormType prfStatus = prf
									.getProtocolFormType();
							String ptypeDesc = prfStatus.getDescription();
							if (!prfStatus
									.equals(ProtocolFormType.NEW_SUBMISSION)) {
								logString = logString.replace("study",
										ptypeDesc);
							}
						}
					}

					logXml = xmlProcessor.replaceOrAddNodeValueByPath(
							"/logs/log[@id=\"" + logIDStr + "\"]", logXml,
							logString);

				}
			}
			track.setXmlData(logXml);
			trackDao.saveOrUpdate(track);
		}

	}

	// @Test
	public void generateListForJiang() {

		FileInputStream fstream;
		try {
			fstream = new FileInputStream("C:\\Data\\Permission.txt");
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			logger.debug("********");
			while ((strLine = br.readLine()) != null) {

				String xml = protocolDao.findById(Long.valueOf(strLine))
						.getMetaDataXml();
				String protocolID = xmlProcessor
						.getAttributeValueByPathAndAttributeName("/protocol",
								xml, "id");
				// logger.debug(protocolID);
				String piEmail = xmlHandler
						.getSingleStringValueByXPath(
								xml,
								"/protocol/staffs/staff/user[roles/role/text()=\"Principal Investigator\"]/email");
				if (piEmail.isEmpty()) {
					piEmail = xmlHandler
							.getSingleStringValueByXPath(
									xml,
									"/protocol/staffs/staff/user[roles/role/text()=\"principal investigator\"]/email");
				}
				String pifn = xmlHandler
						.getSingleStringValueByXPath(
								xml,
								"/protocol/staffs/staff/user[roles/role/text()=\"Principal Investigator\"]/firstname");
				if (pifn.isEmpty()) {
					pifn = xmlHandler
							.getSingleStringValueByXPath(
									xml,
									"/protocol/staffs/staff/user[roles/role/text()=\"principal investigator\"]/firstname");
				}
				String piln = xmlHandler
						.getSingleStringValueByXPath(
								xml,
								"/protocol/staffs/staff/user[roles/role/text()=\"Principal Investigator\"]/lastname");
				if (piln.isEmpty()) {
					piln = xmlHandler
							.getSingleStringValueByXPath(
									xml,
									"/protocol/staffs/staff/user[roles/role/text()=\"principal investigator\"]/lastname");
				}
				String pcEmail = xmlHandler
						.getSingleStringValueByXPath(
								xml,
								"/protocol/staffs/staff/user[roles/role/text()=\"Primary Contact\" or \"primary contact\"]/email");
				if (pcEmail.isEmpty()) {
					pcEmail = xmlHandler
							.getSingleStringValueByXPath(xml,
									"/protocol/staffs/staff/user[roles/role/text()=\"primary contact\"]/email");
				}
				String pcfn = xmlHandler
						.getSingleStringValueByXPath(
								xml,
								"/protocol/staffs/staff/user[roles/role/text()=\"Primary Contact\" or \"primary contact\"]/firstname");
				if (pcfn.isEmpty()) {
					pcfn = xmlHandler
							.getSingleStringValueByXPath(xml,
									"/protocol/staffs/staff/user[roles/role/text()=\"primary contact\"]/firstname");
				}
				String pcln = xmlHandler
						.getSingleStringValueByXPath(
								xml,
								"/protocol/staffs/staff/user[roles/role/text()=\"Primary Contact\" or \"primary contact\"]/lastname");
				if (pcln.isEmpty()) {
					pcln = xmlHandler
							.getSingleStringValueByXPath(xml,
									"/protocol/staffs/staff/user[roles/role/text()=\"primary contact\"]/lastname");
				}
				System.out.println(protocolID + " pi: " + pifn + " " + piln
						+ " " + piEmail + " Primart-Contact: " + pcfn + " "
						+ pcln + " " + pcEmail);
			}
		} catch (Exception e) {

		}

	}

	// @Test
	public void findFormerStaffChangeRolesInModification() {
		List<Protocol> protocolLst = miagrationDao.findFormStaffOnProtocol();
		Map<Long, List<String>> formerStaffUserMap = new HashMap<Long, List<String>>();
		List<Long> protocolIDListForMap = new ArrayList<Long>();
		// get the map for all former staff users in protocol
		for (Protocol p : protocolLst) {
			List<String> formerStaffUsers = new ArrayList<String>();
			ProtocolForm pf = null;
			try {

				List<String> userIDList = xmlProcessor
						.getAttributeValuesByPathAndAttributeName(
								"/protocol/staffs/staff/user",
								p.getMetaDataXml(), "id");
				for (String userIdStr : userIDList) {
					List<String> rolesList = xmlProcessor
							.listElementStringValuesByPath(
									"/protocol/staffs/staff/user[@id=\""
											+ userIdStr + "\"]/roles/role",
									p.getMetaDataXml());
					int existFormerStaff = 0;
					for (String role : rolesList) {
						if (role.toLowerCase().contains("former")) {
							existFormerStaff = 1;
						}
					}

					if (existFormerStaff == 0) {
						continue;
					}
					formerStaffUsers.add(userIdStr);
				}
			} catch (Exception e) {
			}
			try {
				pf = protocolFormDao
						.getLatestProtocolFormByProtocolIdAndProtocolFormType(
								p.getId(), ProtocolFormType.MODIFICATION);
			} catch (Exception e) {
				continue;
			}
			// if former staff user is not in modification, remove it from list
			List<String> latestFormerUserList = new ArrayList<String>();

			for (String userID : formerStaffUsers) {
				if (pf.getMetaDataXml().contains("user id=\"" + userID + "\"")) {
					latestFormerUserList.add(userID);
				}
			}

			if (latestFormerUserList.size() > 0) {
				formerStaffUserMap.put(p.getId(), latestFormerUserList);
				protocolIDListForMap.add(p.getId());
			}

		}

		for (Long protocolID : protocolIDListForMap) {
			List<String> tempUserList = formerStaffUserMap.get(protocolID);
			for (String userID : tempUserList) {
				logger.debug(protocolID + " " + userID);
			}
		}

	}

	// @Test
	public void addIdForTrack() throws XPathExpressionException, SAXException,
			IOException {
		List<Track> tackList = trackDao.findAll();
		for (Track track : tackList) {
			logger.debug("track id: " + track.getId());
			String logXml = track.getXmlData();
			Document doc = null;
			try {
				doc = xmlProcessor.loadXmlStringToDOM(logXml);
			} catch (SAXException e) {
				e.printStackTrace();
			}

			NodeList logList = doc.getElementsByTagName("log");
			for (int i = 0; i < logList.getLength(); i++) {
				Element log = (Element) logList.item(i);
				String idStr = UUID.randomUUID().toString();
				if (log.hasAttribute("id")) {
					continue;
				} else {
					log.setAttribute("id", idStr);
					log.setAttribute("parent-id", idStr);
				}
			}
			logXml = DomUtils.elementToString(doc);
			track.setXmlData(logXml);
			trackDao.saveOrUpdate(track);
		}
	}

	// @Test
	public void updateParentIDinTrack() throws XPathExpressionException,
			SAXException, IOException {
		List<Track> tackList = trackDao.findAll();
		for (Track track : tackList) {
			logger.debug("track id: " + track.getId());

			String logXml = track.getXmlData();
			String trackType = track.getType();
			if (trackType.equals("PROTOCOL") || trackType.equals("AGENDAITEM")) {
				Set<String> pathSet = new HashSet<String>();
				pathSet.add("/logs/log");
				List<Element> logElements = xmlProcessor
						.listDomElementsByPaths(pathSet, logXml);
				for (Element logEle : logElements) {
					if (!logEle.hasAttribute("form-id")) {
						continue;
					}
					if (logEle.getAttribute("form-id").isEmpty()) {
						continue;
					}
					long formId = Long.valueOf(logEle.getAttribute("form-id"));
					String logIDStr = logEle.getAttribute("id");

					long parentID = 0;
					if (formId != 0) {
						parentID = protocolFormDao.findById(formId).getParent()
								.getId();
					}

					Map<String, String> parentIDMap = new HashMap<String, String>();

					parentIDMap.put("parent-form-id", parentID + "");

					logXml = xmlProcessor.addAttributesByPath(
							"/logs/log[@id=\"" + logIDStr + "\"]", logXml,
							parentIDMap);

				}
			} else if (trackType.equals("CONTRACT")) {
				Set<String> pathSet = new HashSet<String>();
				pathSet.add("/logs/log");
				List<Element> logElements = xmlProcessor
						.listDomElementsByPaths(pathSet, logXml);
				for (Element logEle : logElements) {
					if (!logEle.hasAttribute("form-id")) {
						continue;
					}
					if (logEle.getAttribute("form-id").isEmpty()) {
						continue;
					}
					long formId = Long.valueOf(logEle.getAttribute("form-id"));
					String logIDStr = logEle.getAttribute("id");

					long parentID = 0;
					if (formId != 0) {
						parentID = contractFormDao.findById(formId).getParent()
								.getId();
					}

					Map<String, String> parentIDMap = new HashMap<String, String>();

					parentIDMap.put("parent-form-id", parentID + "");

					logXml = xmlProcessor.addAttributesByPath(
							"/logs/log[@id=\"" + logIDStr + "\"]", logXml,
							parentIDMap);

				}
			}
			track.setXmlData(logXml);
			trackDao.saveOrUpdate(track);

		}
	}

	// @Test
	public void mergeXmlData() throws XPathExpressionException, SAXException,
			IOException {
		String formXml = protocolFormDao.findById(14857).getMetaDataXml();
		ProtocolForm protocolForm = protocolFormDao.findById(13777);
		formXml = xmlProcessor.replaceOrAddNodeValueByPath(
				"/protocol/modification/to-modify-section/complete-migration",
				formXml, "y");
		protocolForm.setMetaDataXml(formXml);
		protocolFormDao.saveOrUpdate(protocolForm);

		String formDataXml = protocolFormXmlDataDao.findById(15658)
				.getXmlData();
		ProtocolFormXmlData protocolFormXmlData = protocolFormXmlDataDao
				.findById(14508);
		formDataXml = xmlProcessor.replaceOrAddNodeValueByPath(
				"/protocol/modification/to-modify-section/complete-migration",
				formDataXml, "y");
		protocolFormXmlData.setXmlData(formDataXml);
		protocolFormXmlDataDao.saveOrUpdate(protocolFormXmlData);
	}

	// @Test
	public void getAllDateInfoFromProtocol() throws IOException {

		String qry = "select protocol_id,lastCR_date,end_date from [clara_dev].[dbo].[temp_date_comparision]";
		Query query = em.createNativeQuery(qry);
		List<Object[]> airaDataList = (List<Object[]>) query.getResultList();
		for (Object[] ariaData : airaDataList) {
			if (ariaData != null) {
				String protocolID = (String) ariaData[0];
				Protocol protocol = protocolDao.findById(Long
						.valueOf(protocolID));

				if (protocol == null) {
					continue;
				}
				try {
					String xmlData = protocol.getMetaDataXml();
					String approvalDate = xmlHandler
							.getSingleStringValueByXPath(xmlData,
									"/protocol/most-recent-study/approval-date");
					String endDate = xmlHandler.getSingleStringValueByXPath(
							xmlData,
							"/protocol/most-recent-study/approval-end-date");
					// compare lastCR date
					String ariaLastCRDateStr = "";
					String ariaEndDateStr = "";
					Date ariaLastCRDate = new Date();
					Date ariaEndDate = ariaLastCRDate;
					Date LastCRDate = ariaLastCRDate;
					Date EndDate = ariaLastCRDate;
					if (ariaData[1] != null) {
						ariaLastCRDateStr = (String) ariaData[1];
						if (!ariaLastCRDateStr.isEmpty()
								&& !ariaLastCRDateStr.equals("null")) {
							ariaLastCRDate = new SimpleDateFormat("MM/DD/YYYY")
									.parse(ariaLastCRDateStr);
						}
					}
					if (ariaData[2] != null) {
						ariaEndDateStr = (String) ariaData[2];
						if (!ariaEndDateStr.isEmpty()
								&& !ariaEndDateStr.equals("null")) {
							ariaEndDate = new SimpleDateFormat("MM/DD/YYYY")
									.parse(ariaEndDateStr);
						}
					}

					if (!approvalDate.isEmpty()) {
						try {
							LastCRDate = new SimpleDateFormat("MM/DD/YYYY")
									.parse(approvalDate);
						} catch (Exception e) {
							LastCRDate = new SimpleDateFormat("YYYY-MM-DD")
									.parse(approvalDate);
						}
					}
					if (!endDate.isEmpty()) {
						try {
							EndDate = new SimpleDateFormat("MM/DD/YYYY")
									.parse(endDate);
						} catch (Exception e) {
							EndDate = new SimpleDateFormat("YYYY-MM-DD")
									.parse(endDate);
						}
					}
					if (ariaLastCRDate.equals(LastCRDate)
							&& ariaEndDate.equals(EndDate)) {
						continue;
					}
					System.out.println(protocol.getId() + ", ariaLastCRDate: "
							+ ariaLastCRDateStr + ", ariaEndDate: "
							+ ariaEndDateStr + ", claraapprovalDate: "
							+ approvalDate + ", claraendDate: " + endDate);
				} catch (Exception e) {
					// e.printStackTrace();
				}

			}
		}
		/*
		 * FileInputStream fstream;
		 * 
		 * fstream = new FileInputStream("C:\\Data\\irblist.txt");
		 * DataInputStream in = new DataInputStream(fstream); BufferedReader br
		 * = new BufferedReader(new InputStreamReader(in)); String strLine;
		 * while ((strLine = br.readLine()) != null) {
		 * 
		 * Protocol protocol = protocolDao.findById(Long.valueOf(strLine)); try{
		 * String xmlData = protocol.getMetaDataXml(); String approvalDate =
		 * xmlHandler.getSingleStringValueByXPath(xmlData,
		 * "/protocol/original-study/approval-date"); String endDate =
		 * xmlHandler.getSingleStringValueByXPath(xmlData,
		 * "/protocol/most-recent-study/approval-end-date");
		 * System.out.println(protocol.getId()+" "+approvalDate+" "+endDate); }
		 * catch(Exception e){ System.out.println(" "); } }
		 */
	}

	// @Test
	public void fixUserPiserial() throws XPathExpressionException,
			SAXException, IOException {
		// List<Protocol> protocols = protocolDao.findAll();

		List<Protocol> protocols = Lists.newArrayList();

		FileInputStream fstream;
		fstream = new FileInputStream("C:\\Data\\id.txt");
		DataInputStream in = new DataInputStream(fstream);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String strLine;
		while ((strLine = br.readLine()) != null) {
			try {
				Protocol p = protocolDao.findById(Long.valueOf(strLine));
				protocols.add(p);
			} catch (Exception e) {
				logger.debug("error: " + strLine);
			}
		}

		Map<String, String> manuallyPIseiralMap = Maps.newHashMap();
		manuallyPIseiralMap.put("CLDoan@uams.edu", "16040");
		manuallyPIseiralMap.put("CDGolden@uams.edu", "15020");
		manuallyPIseiralMap.put("SchexnayderSM@uams.edu", "1872");
		manuallyPIseiralMap.put("BuchmannJulissa@uams.edu", "14692");
		manuallyPIseiralMap.put("TilfordMickJ@uams.edu", "2119");
		manuallyPIseiralMap.put("RailwilkersonSueE@uams.edu", "14299");
		manuallyPIseiralMap.put("McgowenNicoleE@uams.edu", "16968");
		manuallyPIseiralMap.put("AAOsiecki@uams.edu", "17363");
		manuallyPIseiralMap.put("SimontonatchleyStep@uams.edu", "177");
		manuallyPIseiralMap.put("ransomchase@uams.edu", "19430");
		manuallyPIseiralMap.put("ShahHemendraR@uams.edu", "1587");
		for (Protocol protocol : protocols) {
			// logger.debug(protocol.getId()+"");
			String xmlData = protocol.getMetaDataXml();
			List<String> emailList = xmlProcessor
					.listElementStringValuesByPath(
							"/protocol/staffs/staff/user/email", xmlData);
			// logger.debug("id: "+protocol.getId());

			for (String email : emailList) {
				try {
					String existPiserial = xmlHandler
							.getSingleStringValueByXPath(xmlData,
									"/protocol/staffs/staff/user[email=\""
											+ email + "\"]/@pi_serial");
					// logger.debug(existPiserial);
					if (!existPiserial.isEmpty()) {
						continue;
					}

					if (manuallyPIseiralMap.containsKey(email)) {
						String piSerial = manuallyPIseiralMap.get(email);
						Map<String, String> attributeMap = new HashMap<String, String>();
						attributeMap.put("pi_serial", piSerial);
						xmlData = xmlProcessor.addAttributesByPath(
								"/protocol/staffs/staff/user[email=\"" + email
										+ "\"]", xmlData, attributeMap);
					} else {
						String lastname = xmlHandler
								.getSingleStringValueByXPath(xmlData,
										"/protocol/staffs/staff/user[email=\""
												+ email + "\"]/lastname");
						String firstname = xmlHandler
								.getSingleStringValueByXPath(xmlData,
										"/protocol/staffs/staff/user[email=\""
												+ email + "\"]/firstname");

						String ariaUserqry = "SELECT [pi_serial]"
								+ " FROM [HOSP_SQL1].[ClinicalResearch].[dbo].[aria_users] "
								+ " WHERE prim_email = '" + email + "'";
						Query query = em.createNativeQuery(ariaUserqry);
						List<Integer> piSerialList = Lists.newArrayList();
						piSerialList = (List<Integer>) query.getResultList();

						if (piSerialList.size() > 1) {
							ariaUserqry = "SELECT [pi_serial]"
									+ " FROM [HOSP_SQL1].[ClinicalResearch].[dbo].[aria_users] "
									+ " WHERE prim_email = '" + email
									+ "' and first = '" + firstname + "'";
							Query query2 = em.createNativeQuery(ariaUserqry);
							List<Integer> tempList = Lists.newArrayList();
							tempList = (List<Integer>) query2.getResultList();
							if (tempList.size() > 0) {
								piSerialList = (List<Integer>) query
										.getResultList();
							}
						}

						if (piSerialList.size() == 0) {
							// logger.debug(email);
							ariaUserqry = "SELECT [pi_serial]"
									+ " FROM [HOSP_SQL1].[ClinicalResearch].[dbo].[aria_users] "
									+ " WHERE first = '" + firstname
									+ "' and lname ='" + lastname + "'";
							Query query3 = em.createNativeQuery(ariaUserqry);
							piSerialList = (List<Integer>) query3
									.getResultList();
						}

						int piSerial = piSerialList.get(0);

						Map<String, String> attributeMap = new HashMap<String, String>();
						attributeMap.put("pi_serial", piSerial + "");
						xmlData = xmlProcessor.addAttributesByPath(
								"/protocol/staffs/staff/user[email=\"" + email
										+ "\"]", xmlData, attributeMap);

					}
					// logger.debug(email);
				} catch (Exception e) {
					logger.debug(email + ": " + protocol.getId());
					/*
					 * Map<String, String> attributeMap = new HashMap<String,
					 * String>(); attributeMap.put("pi_serial", ""); xmlData =
					 * xmlProcessor.addAttributesByPath(
					 * "/protocol/staffs/staff/user[email=\"" + email + "\"]",
					 * xmlData, attributeMap);
					 */
				}
				// set user id
				/*
				 * try { logger.debug(email); long userId =
				 * userDao.getUserByEmail(email).getId(); Map<String, String>
				 * attributeMap = new HashMap<String, String>();
				 * attributeMap.put("id", userId + ""); xmlData =
				 * xmlProcessor.addAttributesByPath(
				 * "/protocol/staffs/staff/user[email=\"" + email + "\"]",
				 * xmlData, attributeMap);
				 * 
				 * } catch (Exception e) { Map<String, String> attributeMap =
				 * new HashMap<String, String>(); attributeMap.put("id", "");
				 * xmlData = xmlProcessor.addAttributesByPath(
				 * "/protocol/staffs/staff/user[email=\"" + email + "\"]",
				 * xmlData, attributeMap); }
				 */
			}
			try {
				protocol.setMetaDataXml(xmlData);
				protocolDao.saveOrUpdate(protocol);
			} catch (Exception e) {
				logger.debug(protocol.getId() + "");
			}
		}
	}

	// @Test
	public void fixUserPiserialinForm() throws XPathExpressionException,
			SAXException, IOException {
		String qry = "select id from protocol_form  where meta_data_xml.exist('//staffs/staff/user[(@id=\"0\" or @id=\"\")]')=1 and retired =0";
		Query query = em.createNativeQuery(qry);
		// List<BigInteger> idList = (List<BigInteger>) query.getResultList();
		List<BigInteger> idList = Lists.newArrayList();
		List<ProtocolForm> protocolForms = Lists.newArrayList();
		// protocolForms.add(protocolFormDao.findById(14244));

		List<Protocol> protocols = Lists.newArrayList();
		protocols.add(protocolDao.findById(106548));
		protocols.add(protocolDao.findById(5446));
		protocols.add(protocolDao.findById(53054));
		protocols.add(protocolDao.findById(107377));
		protocols.add(protocolDao.findById(111481));
		protocols.add(protocolDao.findById(114344));
		protocols.add(protocolDao.findById(130757));
		protocols.add(protocolDao.findById(134140));
		protocols.add(protocolDao.findById(881));

		for (Protocol p : protocols) {
			protocolForms.addAll(protocolFormDao
					.listProtocolFormsByProtocolId(p.getId()));
		}
		// for (BigInteger id : idList) {
		for (ProtocolForm protocolForm : protocolForms) {
			/*
			 * ProtocolForm protocolForm = protocolFormDao
			 * .findById(id.longValue());
			 */
			String xmlData = protocolForm.getMetaDataXml();
			List<String> emailList = xmlProcessor
					.listElementStringValuesByPath(
							"/protocol/staffs/staff/user/email", xmlData);

			for (String email : emailList) {
				try {
					// logger.debug(email);
					String ariaUserqry = "SELECT [pi_serial]"
							+ " FROM [HOSP_SQL1].[ClinicalResearch].[dbo].[aria_users] "
							+ " WHERE prim_email = '" + email + "'";
					Query query2 = em.createNativeQuery(ariaUserqry);
					int piSerial = (int) query2.getSingleResult();
					Map<String, String> attributeMap = new HashMap<String, String>();
					attributeMap.put("pi_serial", piSerial + "");
					xmlData = xmlProcessor.addAttributesByPath(
							"/protocol/staffs/staff/user[email=\"" + email
									+ "\"]", xmlData, attributeMap);

				} catch (Exception e) {
					Map<String, String> attributeMap = new HashMap<String, String>();
					attributeMap.put("pi_serial", "");
					xmlData = xmlProcessor.addAttributesByPath(
							"/protocol/staffs/staff/user[email=\"" + email
									+ "\"]", xmlData, attributeMap);
				}

				// set user id
				try {
					logger.debug(email);
					long userId = userDao.getUserByEmail(email).getId();
					Map<String, String> attributeMap = new HashMap<String, String>();
					attributeMap.put("id", userId + "");
					xmlData = xmlProcessor.addAttributesByPath(
							"/protocol/staffs/staff/user[email=\"" + email
									+ "\"]", xmlData, attributeMap);

				} catch (Exception e) {
					Map<String, String> attributeMap = new HashMap<String, String>();
					attributeMap.put("id", "");
					xmlData = xmlProcessor.addAttributesByPath(
							"/protocol/staffs/staff/user[email=\"" + email
									+ "\"]", xmlData, attributeMap);
				}
			}
			protocolForm.setMetaDataXml(xmlData);
			protocolFormDao.saveOrUpdate(protocolForm);
		}
	}

	// @Test
	public void updateUserIDisZeroInProtocol() throws XPathExpressionException,
			SAXException, IOException {
		String qry = "select id from protocol where meta_data_xml.exist('//staffs/staff/user[(@id=\"0\" or @id=\"\")]')=1 and retired =0";
		Query query = em.createNativeQuery(qry);
		// List<BigInteger> idList = (List<BigInteger>) query.getResultList();
		List<BigInteger> idList = Lists.newArrayList();
		idList.add(new BigInteger("4689"));
		for (BigInteger id : idList) {
			Protocol protocol = protocolDao.findById(id.longValue());
			String xmlData = protocol.getMetaDataXml();
			List<String> emailList = xmlProcessor
					.listElementStringValuesByPath(
							"//staffs/staff/user[(@id=\"0\" or @id=\"\")]/email",
							xmlData);

			for (String email : emailList) {
				try {

					try {
						String userid = userDao.getUserByEmail(email).getId()
								+ "";

						Map<String, String> attributeMap = new HashMap<String, String>();
						attributeMap.put("id", userid);
						xmlData = xmlProcessor.addAttributesByPath(
								"//staffs/staff/user[email=\"" + email + "\"]",
								xmlData, attributeMap);
					} catch (Exception e) {
						String ariaUserqry = "SELECT [pi_serial]"
								+ " FROM [HOSP_SQL1].[ClinicalResearch].[dbo].[aria_users] "
								+ " WHERE prim_email = '" + email + "'";
						Query query2 = em.createNativeQuery(ariaUserqry);
						int piSerial = (int) query2.getSingleResult();
						Map<String, String> attributeMap = new HashMap<String, String>();
						attributeMap.put("pi_serial", piSerial + "");
						xmlData = xmlProcessor.addAttributesByPath(
								"//staffs/staff/user[email=\"" + email + "\"]",
								xmlData, attributeMap);

						logger.debug(id + " piserial " + piSerial);
					}
				} catch (Exception e) {
				}
			}
			protocol.setMetaDataXml(xmlData);
			protocolDao.saveOrUpdate(protocol);
		}
	}

	// @Test
	public void fixUserIDisZeroInProtocolForm()
			throws XPathExpressionException, SAXException, IOException {
		String qry = "select id from protocol_form  where meta_data_xml.exist('//staffs/staff/user[(@id=\"0\" or @id=\"\")]')=1 and retired =0";
		Query query = em.createNativeQuery(qry);
		List<BigInteger> idList = (List<BigInteger>) query.getResultList();
		for (BigInteger id : idList) {
			ProtocolForm protocolForm = protocolFormDao
					.findById(id.longValue());
			String xmlData = protocolForm.getMetaDataXml();
			List<String> emailList = xmlProcessor
					.listElementStringValuesByPath(
							"//staffs/staff/user[(@id=\"0\" or @id=\"\")]/email",
							xmlData);

			for (String email : emailList) {
				try {
					logger.debug(id + " ");
					try {
						String userid = userDao.getUserByEmail(email).getId()
								+ "";

						Map<String, String> attributeMap = new HashMap<String, String>();
						attributeMap.put("id", userid);
						xmlData = xmlProcessor.addAttributesByPath(
								"//staffs/staff/user[email=\"" + email + "\"]",
								xmlData, attributeMap);
					} catch (Exception e) {
						String ariaUserqry = "SELECT [pi_serial]"
								+ " FROM [HOSP_SQL1].[ClinicalResearch].[dbo].[aria_users] "
								+ " WHERE prim_email = '" + email + "'";
						Query query2 = em.createNativeQuery(ariaUserqry);
						int piSerial = (int) query2.getSingleResult();
						Map<String, String> attributeMap = new HashMap<String, String>();
						attributeMap.put("pi_serial", piSerial + "");
						xmlData = xmlProcessor.addAttributesByPath(
								"//staffs/staff/user[email=\"" + email + "\"]",
								xmlData, attributeMap);
					}
				} catch (Exception e) {
				}
			}
			protocolForm.setMetaDataXml(xmlData);
			protocolFormDao.saveOrUpdate(protocolForm);
		}
	}

	// @Test
	public void fixUserIDisZeroInProtocolFormXmlData()
			throws XPathExpressionException, SAXException, IOException {
		String qry = "select id from protocol_form_xml_data where xml_data.exist('//staffs/staff/user[(@id=\"0\" or @id=\"\")]')=1 and retired =0";
		Query query = em.createNativeQuery(qry);
		List<BigInteger> idList = (List<BigInteger>) query.getResultList();
		for (BigInteger id : idList) {
			ProtocolFormXmlData protocolFormXmlData = protocolFormXmlDataDao
					.findById(id.longValue());
			String xmlData = protocolFormXmlData.getXmlData();
			List<String> emailList = xmlProcessor
					.listElementStringValuesByPath(
							"//staffs/staff/user[(@id=\"0\" or @id=\"\")]/email",
							xmlData);

			for (String email : emailList) {
				try {
					try {
						String userid = userDao.getUserByEmail(email).getId()
								+ "";

						Map<String, String> attributeMap = new HashMap<String, String>();
						attributeMap.put("id", userid);
						xmlData = xmlProcessor.addAttributesByPath(
								"//staffs/staff/user[email=\"" + email + "\"]",
								xmlData, attributeMap);
					} catch (Exception e) {
						String ariaUserqry = "SELECT [pi_serial]"
								+ " FROM [HOSP_SQL1].[ClinicalResearch].[dbo].[aria_users] "
								+ " WHERE prim_email = '" + email + "'";
						Query query2 = em.createNativeQuery(ariaUserqry);
						int piSerial = (int) query2.getSingleResult();
						Map<String, String> attributeMap = new HashMap<String, String>();
						attributeMap.put("pi_serial", piSerial + "");
						xmlData = xmlProcessor.addAttributesByPath(
								"//staffs/staff/user[email=\"" + email + "\"]",
								xmlData, attributeMap);
					}
				} catch (Exception e) {
				}
			}
			protocolFormXmlData.setXmlData(xmlData);
			protocolFormXmlDataDao.saveOrUpdate(protocolFormXmlData);
		}
	}

	// @Test
	public void fixUserIDisZero() throws XPathExpressionException,
			SAXException, IOException {
		int idList[] = { 56, 57, 58, 81, 85, 127 };
		for (int j = 0; j < idList.length; j++) {
			int id = idList[j];

			ProtocolFormUserElementTemplate protocolFormUserElementTemplate = new ProtocolFormUserElementTemplate();
			protocolFormUserElementTemplate = protocolFormUserElementTemplateDao
					.findById(id);
			String xmlData = protocolFormUserElementTemplate.getXmlData();

			List<String> emailList = xmlProcessor
					.listElementStringValuesByPath(
							"//staff/user[@id=\"0\" or @id=\"\"]/email",
							xmlData);
			logger.debug(emailList.size() + "");
			for (int i = 0; i < emailList.size(); i++) {
				User user = null;
				user = userDao.getUserByEmail(emailList.get(i));
				if (user != null) {
					String userID = user.getId() + "";
					String newXmlData = xmlProcessor
							.replaceAttributeValueByPathAndAttributeName(
									"/staffs/staff/user[email=\""
											+ emailList.get(i) + "\"]", "id",
									xmlData, userID);
					protocolFormUserElementTemplate.setXmlData(newXmlData);
					protocolFormUserElementTemplateDao
							.saveOrUpdate(protocolFormUserElementTemplate);
				}
			}

		}

	}

	public ProtocolFormUserElementTemplateDao getProtocolFormUserElementTemplateDao() {
		return protocolFormUserElementTemplateDao;
	}

	@Autowired(required = true)
	public void setProtocolFormUserElementTemplateDao(
			ProtocolFormUserElementTemplateDao protocolFormUserElementTemplateDao) {
		this.protocolFormUserElementTemplateDao = protocolFormUserElementTemplateDao;
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

	public EntityManager getEm() {
		return em;
	}

	@PersistenceContext(unitName = "defaultPersistenceUnit")
	public void setEm(EntityManager em) {
		this.em = em;
	}

	public TrackDao getTrackDao() {
		return trackDao;
	}

	@Autowired(required = true)
	public void setTrackDao(TrackDao trackDao) {
		this.trackDao = trackDao;
	}

	public ContractFormDao getContractFormDao() {
		return contractFormDao;
	}

	@Autowired(required = true)
	public void setContractFormDao(ContractFormDao contractFormDao) {
		this.contractFormDao = contractFormDao;
	}

	public MiagrationDao getMiagrationDao() {
		return miagrationDao;
	}

	@Autowired(required = true)
	public void setMiagrationDao(MiagrationDao miagrationDao) {
		this.miagrationDao = miagrationDao;
	}

	public UserService getUserService() {
		return userService;
	}

	@Autowired(required = true)
	public void setUserService(UserService userService) {
		this.userService = userService;
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

	public CrimsonStudyDao getCrimsonStudyDao() {
		return crimsonStudyDao;
	}

	@Autowired(required = true)
	public void setCrimsonStudyDao(CrimsonStudyDao crimsonStudyDao) {
		this.crimsonStudyDao = crimsonStudyDao;
	}

	public CollegeDao getCollegeDao() {
		return collegeDao;
	}

	@Autowired(required = true)
	public void setCollegeDao(CollegeDao collegeDao) {
		this.collegeDao = collegeDao;
	}

	public DepartmentDao getDepartmentDao() {
		return departmentDao;
	}

	@Autowired(required = true)
	public void setDepartmentDao(DepartmentDao departmentDao) {
		this.departmentDao = departmentDao;
	}

}
