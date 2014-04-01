package edu.uams.clara.webapp.xml.processor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.dom4j.Node;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.xml.sax.SAXException;

import edu.uams.clara.test.Assert.XMLAssert;
import edu.uams.clara.webapp.common.domain.usercontext.enums.Committee;
import edu.uams.clara.webapp.contract.domain.businesslogicobject.enums.ContractStatusEnum;
import edu.uams.clara.webapp.protocol.dao.protocolform.ProtocolFormDao;
import edu.uams.clara.webapp.protocol.dao.protocolform.ProtocolFormXmlDataDao;
import edu.uams.clara.webapp.protocol.domain.protocolform.enums.ProtocolFormType;
import edu.uams.clara.webapp.protocol.service.ProtocolMetaDataXmlService;

/**
 * 
 * @author Jiang Bian
 * 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "file:src/test/java/edu/uams/clara/webapp/xml/processor/XmlProcessorTest-context.xml" })
public class XmlProcessorTest {

	private final static Logger logger = LoggerFactory
			.getLogger(XmlProcessorTest.class);

	private XmlProcessor xmlProcessor;
	
	private ProtocolMetaDataXmlService protocolMetaDataXmlService;
	
	private ProtocolFormDao protocolFormDao;
	
	private ProtocolFormXmlDataDao protocolFormXmlDataDao;
	
	private final DocumentBuilderFactory dbFactory = DocumentBuilderFactory
			.newInstance();
	
	
	//@Test
	public void testThreadSafety() throws ParserConfigurationException, InterruptedException{
		
		//final DocumentBuilder documentBuilder  = dbFactory.newDocumentBuilder();
		final String xmlData = "<drugs><drug id=\"1234\">2-8˚C<storage>Study drug should be stored at 2-8˚C. All investigational products®</storage></drug></drugs>";
		final String originalXml = "<protocol id=\"1\"><submission-type>new protocol</submission-type></protocol>";
		final String modifiedXml = "<protocol><misc><is-tri-involved>y</is-tri-involved></misc><multisite>No</multisite></protocol>";

		final String expectedXml = "<protocol id=\"1\"><misc><is-tri-involved>y</is-tri-involved></misc><multisite>No</multisite><submission-type>new protocol</submission-type></protocol>";
		
		
		List<Thread> threads = new ArrayList<Thread>();
		for (int i=0; i< 1000; i++){
			Thread t = new Thread(new Runnable(){

				@Override
				public void run() {
					
					try {
						//String xml = xmlProcessor.mergeElementsByXmlAndPath(path, originalXml, elementXml);
						//Assert.assertEquals(resultXml, xml);
						//InputSource documentInputSource = new InputSource(new StringReader(
								//xmlData));
						//documentBuilder.parse(documentInputSource);
						//System.out.println(xmlProcessor.escapeText(xmlData));
						
						//String finalXml = xmlProcessor.merge(originalXml, modifiedXml);
						//XMLAssert.assertXMLEquals(expectedXml, finalXml);
						String finalXml =  xmlProcessor.deleteElementByPath("/drugs/drug", xmlData).get("finalXml").toString();
						XMLAssert.assertXMLEquals(finalXml, "<drugs></drugs>");
						
						
						
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					
				}
				
			});
			
			threads.add(t);
			t.start();
		}
		
		for (Thread t:threads){
			t.join();
		}
	}

	// @Test
	public void testTextEscape() throws SAXException, IOException {

		logger.info("" + Node.ATTRIBUTE_NODE);
		logger.info("" + Node.DOCUMENT_NODE);
		logger.info("" + Node.TEXT_NODE);
		logger.info("" + Node.ELEMENT_NODE);
		logger.info("" + Node.COMMENT_NODE);
		logger.info("" + Node.CDATA_SECTION_NODE);
		// String xmlResult =
		// "<list><drug admin=\"\" approved=\"true\" brochure=\"n\" dosage=\"\" id=\"df4ec95e-19eb-45ca-b164-5740fadcdfe2\" identifier=\"102519\" ind=\"\" insert=\"y\" isprovided=\"n\" name=\"Easprin\" nsc=\"\" provider=\"\" status=\"\" type=\"investigational\">2-8˚C<storage>Study drug should be stored at 2-8˚C. All investigational products®</storage><prep/><toxicities/><pharmacies><other-pharmacy id=\"0\" identifier=\"0\" name=\"\"/><other-pharmacy id=\"0\" identifier=\"0\" name=\"\"/></pharmacies></drug></list>";
		String xmlResult = "<drug>2-8˚C<storage>Study drug should be stored at 2-8˚C. All investigational products®</storage></drug>";
		logger.info(xmlProcessor.escapeText(xmlResult));
	}

	// @Test
	public void testReplaceNodeValueByPath() throws XPathExpressionException,
			SAXException, IOException {
		String thisProtocolMetaDataXml = xmlProcessor
				.replaceOrAddNodeValueByPath(
						"/contract/status",
						"<contract created=\"11/06/2012\" id=\"13054\" identifier=\"C13054-2012\" timestamp=\"1352242558070\" type=\"New Contract\"><basic-info><contract-end-date/><contract-begin-date/><contract-execution-date/></basic-info><protocol/><title>test</title><staffs><staff id=\"c5302e81-17ff-4ad3-bd78-8540b25bff31\"><user id=\"68\" phone=\"(501) 686-5418\" sap=\"36993\"><lastname>Yu</lastname><firstname>Fan</firstname><email>FYu2@uams.edu</email><roles><role>Principal Investigator</role></roles><reponsibilities><responsibility>Obtaining informed consent</responsibility><responsibility>Performing non-invasive study activities</responsibility></reponsibilities><costs/><conflict-of-interest>false</conflict-of-interest><conflict-of-interest-description/></user><notify>false</notify></staff></staffs><type>clinical-trial-agreement<confidentiality-disclosure-agreement><sub-type/></confidentiality-disclosure-agreement><clinical-trial-agreement><sub-type>sponsored-cta</sub-type></clinical-trial-agreement><material-transfer-agreement><sub-type/></material-transfer-agreement><research-agreement><sub-type/></research-agreement><subcontracts><sub-type/></subcontracts><license><sub-type/></license></type></contract>",
						ContractStatusEnum.UNDER_CONTRACT_MANAGER_REVIEW
								.getDescription());

		logger.debug("$$$$$$$$$$$$$$$$ " + thisProtocolMetaDataXml);
	}

	//@Test
	public void testListElementStringValuesByPath()
			throws XPathExpressionException, SAXException, IOException, InterruptedException {
		 String xmlData = "	<message><to>GROUP_studyPI</to><cc></cc><subject>IRB Letter</subject><body>The following documents were received:<br/> <ul><li>Summary of Changes to Update 10 dated 04/18/2013 </li><li>Protocol Update 10 dated 04/18/2013 </li></ul><p>testsetset<br/></p></body></message>";
		 List<String> commentLst = xmlProcessor.listElementStringValuesByPath("//message/body", xmlData);
		 
		 logger.debug("emailComment: " + commentLst.get(0));
		/*
		List<Thread> threads = new ArrayList<Thread>();
		for (int i=0; i< 1000; i++){
			final int c = i;
			Thread t = new Thread(new Runnable(){

				@Override
				public void run() {
					
					try {
						String xmlData = "<reportable-new-info><is-reportable>" + c + "</is-reportable><is-reportable>" + (c + 1) +  "</is-reportable></reportable-new-info>";
						List<String> result = xmlProcessor.listElementStringValuesByPath(
								"/reportable-new-info/is-reportable", xmlData);
						
						Assert.assertEquals(c, Integer.parseInt(result.get(0)));
						Assert.assertEquals(c + 1, Integer.parseInt(result.get(1)));
	
						
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					
				}
				
			});
			
			threads.add(t);
			t.start();
		}
		
		for (Thread t:threads){
			t.join();
		}
		*/
		
		

	}

	/**
	 * This test will fail... the merge is not ordered...for now it's fine..
	 * but... - Jiang
	 * 
	 * @throws SAXException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws AssertionError
	 */
	// @Test
	public void testXmlMerge() throws SAXException, IOException,
			AssertionError, ParserConfigurationException {
		String originalXml = "<protocol id=\"1\"><misc><submission-type>new protocol</submission-type></protocol>";
		String modifiedXml = "<protocol><misc><is-tri-involved>y</is-tri-involved></misc><subjects><disease-ontology></disease-ontology><inclusion-exclusion-criteria-for-this-study></inclusion-exclusion-criteria-for-this-study></subjects><risks><radiation-safety><involve-use-of-strenuous-exercise><y><nature-of-study-exercise></nature-of-study-exercise></y></involve-use-of-strenuous-exercise></radiation-safety></risks></protocol>";

		String expectedXml = "<protocol id=\"1\"><multisite>No</multisite><submission-type>new protocol</submission-type></protocol>";
		String finalXml = xmlProcessor.merge(originalXml, modifiedXml);

		logger.debug(finalXml);
		logger.debug(expectedXml);
		// assertEquals("assertEquals", expectedXml, finalXml);
		XMLAssert.assertXMLEquals(expectedXml, finalXml);
	}

	/**
	 * doing merge by Map<String, String> xPathPairs
	 * 
	 * @throws SAXException
	 * @throws IOException
	 * @throws XPathExpressionException
	 * @throws InterruptedException 
	 */
	//@Test
	public void testXmlMergeByXPathsErorrs() throws SAXException, IOException,
			XPathExpressionException, InterruptedException {

		final String originalXml = "<protocol id=\"1\"><status>Draft</status></protocol>"; // xyz
																						// should
																						// be
																						// replaced
		final String modifiedXml = "<protocol id=\"1\"><study-type>Industry Sponsored</study-type><responsible-department collegedesc=\"INFO Information Technology\" collegeid=\"16\" deptdesc=\"INFO Administration\" deptid=\"182\"/><study-nature>biomedical-clinical<biomedical-clinical><study-involves><involve>investigational-drugs</involve><involve>investigational-devices</involve></study-involves></biomedical-clinical></study-nature><site-responsible>uams</site-responsible><department/><study-sites><site approved=\"true\" id=\"d7760779-80d7-4d82-b7d5-43c48e3decd1\" site-id=\"1\"><site-name>ACH - Arkansas Children's Hospital</site-name><address>1 Children's Way</address><city>Little Rock</city><state>AR</state><zip>72202</zip><site-contact>asd</site-contact></site><site approved=\"true\" id=\"51478cd0-858a-480b-9367-3b367311dabb\" site-id=\"4\"><site-name>CAVHS - Central Arkansas Veterans Healthcare System</site-name><address>4300 West 7th Street</address><city>Little Rock</city><state>AR</state><zip>72205-5484</zip><site-contact>asd</site-contact></site></study-sites></protocol>";

		final String expectedXml = "<protocol id=\"1\"><status>Draft</status><responsible-department collegedesc=\"INFO Information Technology\" collegeid=\"16\" deptdesc=\"INFO Administration\" deptid=\"182\"/><study-sites><site approved=\"true\" id=\"d7760779-80d7-4d82-b7d5-43c48e3decd1\" site-id=\"1\"><site-name>ACH - Arkansas Children's Hospital</site-name><address>1 Children's Way</address><city>Little Rock</city><state>AR</state><zip>72202</zip><site-contact>asd</site-contact></site><site approved=\"true\" id=\"51478cd0-858a-480b-9367-3b367311dabb\" site-id=\"4\"><site-name>CAVHS - Central Arkansas Veterans Healthcare System</site-name><address>4300 West 7th Street</address><city>Little Rock</city><state>AR</state><zip>72205-5484</zip><site-contact>asd</site-contact></site></study-sites></protocol>";
		// "<pharmacy id=\"NaN\" waived=\"false\" total=\"1250\"><expenses><expense id=\"1\" count=\"1\" cost=\"1000\" type=\"simc\" name=\"base-fee\" description=\"Base Fee\" notes=\"\"><drugs /><fees /></expense></expenses></pharmacy>";
		//final String expectedXml = "<protocol id=\"1\"><study-type>Industry Sponsored</study-type><submission-type>Full Protocol</submission-type><title>whatever</title><staffs><staff id=\"db94d624-d78b-4177-931c-4c163452244d\"><user id=\"1\"><lastname>Bian</lastname><firstname>Jiang</firstname><email>JBian@uams.edu</email></user><roles><role>Principal Investigator</role></roles><responsibilities><responsibility>Perform Disease Assessments</responsibility><responsibility>Dispense Investigational Product</responsibility></responsibilities><conflict-of-interest>n</conflict-of-interest><conflict-of-interest-description /></staff></staffs></protocol>";

		final Map<String, String> newSubmissionXPathPairs = new HashMap<String, String>();
		// newSubmissionXPathPairs.put("/pharmacy", "/protocol/pharmacy");
		newSubmissionXPathPairs.put("/protocol/responsible-department",
				"/protocol/responsible-department");
		newSubmissionXPathPairs.put("/protocol/study-sites",
				"/protocol/study-sites");
		newSubmissionXPathPairs.put("/protocol/study-type",
				"/protocol/study-type");
		newSubmissionXPathPairs.put("/protocol/study-sites",
		"/protocol/study-sites");
		newSubmissionXPathPairs.put("/protocol/title", "/protocol/title");
		newSubmissionXPathPairs.put("/protocol/staffs", "/protocol/staffs");
		newSubmissionXPathPairs.put("/protocol/subjects", "/protocol/subjects");
		newSubmissionXPathPairs.put("/protocol/budget", "/protocol/budget");

		// newSubmissionXPathPairs.put("/protocol/staffs/staff/user/[roles/role='Principal Investigator']",
		// "/protocol/staffs/staff/user");
		List<Thread> threads = new ArrayList<Thread>();
		for (int i=0; i< 1000; i++){
			Thread t = new Thread(new Runnable(){

				@Override
				public void run() {
					
					try {
						String finalXml = xmlProcessor
								.mergeByXPaths(originalXml, modifiedXml,
										XmlProcessor.Operation.UPDATE_IF_EXIST,
										newSubmissionXPathPairs);

						logger.debug("%%%%%%%%%%%%%%%%%%" + finalXml);
						logger.debug("%%%%%%%%%%%%%%%%%%" + expectedXml);
						// assertEquals("assertEquals", expectedXml, finalXml);
						XMLAssert.assertXMLEquals(expectedXml, finalXml);
						
						
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					
				}
				
			});
			
			threads.add(t);
			t.start();
		}
		
		for (Thread t:threads){
			t.join();
		}
		
		
		
	}

	/**
	 * doing merge by Map<String, String> xPathPairs
	 * 
	 * @throws SAXException
	 * @throws IOException
	 * @throws XPathExpressionException
	 */
	// @Test
	public void testXmlMergeByXPaths() throws SAXException, IOException,
			XPathExpressionException {
		String originalXml = "<protocol id=\"1\"><study-type>xyz</study-type></protocol>"; // xyz
																							// should
																							// be
																							// replaced
		String modifiedXml = "<protocol id=\"1\"><staffs><staff id=\"db94d624-d78b-4177-931c-4c163452244d\"><user id=\"1\"><lastname>Bian</lastname><firstname>Jiang</firstname><email>JBian@uams.edu</email></user><roles><role>Principal Investigator</role></roles><responsibilities><responsibility>Perform Disease Assessments</responsibility><responsibility>Dispense Investigational Product</responsibility></responsibilities><conflict-of-interest>n</conflict-of-interest><conflict-of-interest-description /></staff></staffs><submission-type>Full Protocol</submission-type><title>whatever</title><study-type>Industry Sponsored</study-type></protocol>";

		String expectedXml = "<protocol id=\"1\"><study-type>Industry Sponsored</study-type><submission-type>Full Protocol</submission-type><title>whatever</title><staffs><staff id=\"db94d624-d78b-4177-931c-4c163452244d\"><user id=\"1\"><lastname>Bian</lastname><firstname>Jiang</firstname><email>JBian@uams.edu</email></user><roles><role>Principal Investigator</role></roles><responsibilities><responsibility>Perform Disease Assessments</responsibility><responsibility>Dispense Investigational Product</responsibility></responsibilities><conflict-of-interest>n</conflict-of-interest><conflict-of-interest-description /></staff></staffs></protocol>";

		Map<String, String> xPathPairs = new HashMap<String, String>();
		xPathPairs
				.put("/protocol/submission-type", "/protocol/submission-type");
		xPathPairs.put("/protocol/title", "/protocol/title");
		xPathPairs.put("/protocol/study-type", "/protocol/study-type");
		xPathPairs.put(
				"/protocol/staffs/staff[roles/role='Principal Investigator']",
				"/protocol/staffs/staff");

		String finalXml = xmlProcessor.mergeByXPaths(originalXml, modifiedXml,
				XmlProcessor.Operation.REPLACE_BY_XPATH_LIST, xPathPairs);

		logger.debug(finalXml);
		// assertEquals("assertEquals", expectedXml, finalXml);
	}

	/**
	 * doing merge by Map<String, String> xPathPairs
	 * 
	 * @throws SAXException
	 * @throws IOException
	 * @throws XPathExpressionException
	 */
	// @Test
	public void testXmlMergeByXPathsForProtocolMetaDataXmlServiceWithHSRDForm()
			throws SAXException, IOException, XPathExpressionException {
		String originalXml = "<protocol id=\"1\"></protocol>"; // xyz should be
																// replaced
		String modifiedXml = "<hsrd protocol-id=\"5\"><staffs/><data-types/><data-living-individuals>n</data-living-individuals><data-individuals-interaction>y</data-individuals-interaction><data-individuals-private-description/><study-description>test study</study-description><is-systematic>n</is-systematic><study-contribution>whatever this is a test</study-contribution><test-articles>whatever this is a test</test-articles></hsrd>";

		String expectedXml = "<protocol id=\"1\"><title>test study</title></protocol>";

		Map<String, String> xPathPairs = new HashMap<String, String>();
		// xPathPairs.put("/protocol/submission-type",
		// "/protocol/submission-type");
		// xPathPairs.put("/protocol/title", "/protocol/title");
		// xPathPairs.put("/protocol/study-type", "/protocol/study-type");
		// xPathPairs.put("/protocol/staffs/staff[roles/role='Principal Investigator']",
		// "/protocol/staffs/staff");
		xPathPairs.put("/hsrd/study-description", "/protocol/title");

		String finalXml = xmlProcessor.mergeByXPaths(originalXml, modifiedXml,
				XmlProcessor.Operation.REPLACE_BY_XPATH_LIST, xPathPairs);

		logger.debug(finalXml);
		assertEquals("assertEquals", expectedXml, finalXml);
	}

	//@Test
	/**
	 * the id is undefined...
	 * @throws InterruptedException 
	 */
	public void testAddElementToList() throws SAXException, IOException,
			XPathExpressionException, InterruptedException {
		final String originalXml = "<protocol id=\"1\"><committee-review></committee-review></protocol>";
		final String elementXml = "<irb-prereview><fda>true</fda><expedited>false</expedited><exempt>true</exempt></irb-prereview>";
		final String path = "/protocol/committee-review/irb-prereview";

		final String expectedXml = "<protocol id=\"1\"><committee-review><irb-prereview><fda>true</fda><expedited>false</expedited><exempt>true</exempt></irb-prereview></committee-review></protocol>";

			
		List<Thread> threads = new ArrayList<Thread>();
		for (int i=0; i< 1000; i++){
			Thread t = new Thread(new Runnable(){

				@Override
				public void run() {
					
					try {
						Map<String, Object> resultMap = xmlProcessor.addElementByPath(path,
								originalXml, elementXml, false);

						assertNotNull(resultMap);

						assertNotNull(resultMap.get("finalXml"));
						assertNotNull(resultMap.get("elementXml"));
						assertNotNull(resultMap.get("elementId"));
						
						

						String finalXml = resultMap.get("finalXml").toString();

						//logger.debug("%%%%%%%%%%%%%%%%%%" + finalXml);
						//logger.debug("%%%%%%%%%%%%%%%%%%" + expectedXml);
						// assertEquals("assertEquals", expectedXml, finalXml);
						XMLAssert.assertXMLEquals(expectedXml, finalXml);
						
						
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					
				}
				
			});
			
			threads.add(t);
			t.start();
		}
		
		for (Thread t:threads){
			t.join();
		}
		
		
		
		/*
		Document currentProtocolFormMetaDoc = xmlProcessor
				.loadXmlStringToDOM(finalXml);
		XPath xpath = xmlProcessor.getXPathInstance();

		Element specificCommitteeReviewNode = (Element) (xpath.evaluate(path,
				currentProtocolFormMetaDoc, XPathConstants.NODE));

		specificCommitteeReviewNode.setAttribute("form-status",
				"UNDER_IRB_PREREVIEW");
		specificCommitteeReviewNode.setAttribute("form-committee-status",
				"IN_REVIEW");

		String finalfinal = DomUtils
				.elementToString(currentProtocolFormMetaDoc);

		logger.debug("@@@@@@@@@@@@@@@@@@@@@@" + finalfinal);
		*/
		// assertEquals("assertEquals", expectedXml, finalXml);
	}

	// @Test
	public void testDeleteElementFromList() throws SAXException, IOException,
			XPathExpressionException {
		String originalXml = "<protocol id=\"1\"><drugs><drug id=\"1\"><name>whateverdrug</name></drug><drug id=\"2\"><name>iamno</name></drug></drugs></protocol>";
		String elementId = "1";
		String path = "/protocol/drugs/drug";

		String expectedXml = "<protocol id=\"1\"><drugs><drug id=\"2\"><name>iamno</name></drug></drugs></protocol>";

		Map<String, Object> resultMap = xmlProcessor.deleteElementByPathById(
				path, originalXml, elementId);

		assertNotNull(resultMap);

		assertNotNull(resultMap.get("finalXml"));
		assertNotNull(resultMap.get("isDeleted"));

		String finalXml = resultMap.get("finalXml").toString();

		assertEquals("assertEquals", expectedXml, finalXml);
		assertTrue("isDeteled:", (Boolean) resultMap.get("isDeleted"));
	}

	// @Test
	public void testGetElementFromList() throws XPathExpressionException,
			SAXException, IOException {
		String originalXml = "<protocol id=\"1\"><drugs><drug id=\"1\"><name>whateverdrug</name></drug></drugs></protocol>";
		String elementId = "1";
		String path = "/protocol/drugs/drug";

		String expectedXml = "<drug id=\"1\"><name>whateverdrug</name></drug>";

		String finalXml = xmlProcessor.getElementByPathById(path, originalXml,
				elementId);

		assertEquals("assertEquals", expectedXml, finalXml);

	}

	// @Test
	public void testUpdateElementInList() throws XPathExpressionException,
			SAXException, IOException {
		String originalXml = "<protocol id=\"1\"><drugs><drug id=\"1\"><name>whateverdrug</name></drug></drugs></protocol>";
		String elementId = "1";
		String path = "/protocol/drugs/drug";
		String elementXml = "<drug><name>iamnewdrug</name></drug>";

		String expectedXml = "<protocol id=\"1\"><drugs><drug id=\"1\"><name>iamnewdrug</name></drug></drugs></protocol>";

		Map<String, Object> resultMap = xmlProcessor.updateElementByPathById(
				path, originalXml, elementId, elementXml);

		assertNotNull(resultMap);

		assertNotNull(resultMap.get("finalXml"));
		assertNotNull(resultMap.get("elementXml"));
		assertNotNull(resultMap.get("elementId"));

		String finalXml = resultMap.get("finalXml").toString();

		assertEquals("assertEquals", expectedXml, finalXml);
	}

	@Test
	public void testListElements() throws XPathExpressionException,
			SAXException, IOException {
		// String originalXml =
		// "<protocol id=\"209\"><staffs><staff id=\"d82edc2c-2c85-492d-8f58-7a8d2f87d6e4\"><user id=\"1\"><lastname>Bian</lastname><firstname>Jiang</firstname><roles><role>Principal Investigator</role></roles><reponsibilities><responsibility>Perform Disease Assessments</responsibility></reponsibilities></user><notify>true</notify></staff><staff id=\"d82edc2c-2c85-492d-8f58-7a8d34tdgde\"><user id=\"1\"><lastname>Yu</lastname><firstname>Fan</firstname><roles><role>Principal Investigator</role></roles><reponsibilities><responsibility>Perform Disease Assessments</responsibility></reponsibilities></user><notify>true</notify></staff></staffs></protocol>";
		// String path =
		// "/protocol/monitoring/is-monitored-externally[. = \"y\"]";
		// String path =
		// "/protocol/staffs/staff[user/lastname[. = \"Bian\"] and user/firstname[. = \"Jiang\"]]";

		String originalXml = "<protocol id=\"209\"><committee-review><committee type=\"BUDGET_REVIEW\"><assigned-reviewers><assigned-reviewer assigning-committee=\"BUDGET_MANAGER\" user-fullname=\"Baker, Matt\" user-id=\"3\" user-role=\"ROLE_BUDGET_REVIEWER\" user-role-committee=\"BUDGET_REVIEW\" user-role-id=\"152\">Baker, Matt</assigned-reviewer>";
		originalXml += "</assigned-reviewers></committee><committee type=\"COVERAGE_REVIEW\"><assigned-reviewers><assigned-reviewer assigning-committee=\"BUDGET_MANAGER\" user-fullname=\"Baker, Matt\" user-id=\"3\" user-role=\"ROLE_COVERAGE_REVIEWER\" user-role-committee=\"COVERAGE_REVIEW\" user-role-id=\"248\">Baker, Matt</assigned-reviewer>";
		originalXml += "</assigned-reviewers></committee><coverage-determination><enrolled-diagnosed>yes</enrolled-diagnosed><trial-category>medicare-qualifying</trial-category><theraputic-intent>yes</theraputic-intent><medicare-benefit>no</medicare-benefit></coverage-determination></committee-review></protocol>";

		//String path = "/protocol/committee-review/committee[assigned-reviewers/assigned-reviewer[@assigning-committee=\"BUDGET_MANAGER\" and @user-role=\"ROLE_BUDGET_REVIEWER\"]]";
		String path = "//committee-review/committee";
		// String expectedXml =
		// "<list><drug id=\"1\"><name>whateverdrug</name></drug><drug id=\"2\"><name>iamno2</name></drug></list>";

		logger.debug("++++++++++++++++++++");
		String finalXml = xmlProcessor.listElementsByPath(path, originalXml,
				true);

		logger.trace("finalXml: " + finalXml);
		// assertEquals("assertEquals", expectedXml, finalXml);

	}

	// @Test
	public void testListElementsValueByPathsAndXmlDataTypes()
			throws XPathExpressionException, SAXException, IOException {
		String originalXml = "<protocol id=\"1\"><title>WhateverTitle</title><phases><phase>I</phase><phase>II</phase></phases><drugs><drug id=\"1\"><name>Whateverdrug</name></drug><drug id=\"2\"><name>iamno2</name></drug></drugs></protocol>";
		Set<String> paths = new HashSet<String>(0);
		paths.add("/protocol/drugs/drug/name");
		paths.add("/protocol/phases/phase");
		paths.add("/protocol/title");

		/**
		 * need to find a better way to define the data type of each xml
		 * element... it's important to do this, because, during validation, the
		 * validator needs to know the type of the value, for example, it's
		 * invalid to put in "abc" as a number... if a path is not defined in
		 * this map, then by default its a String...
		 */
		Map<String, Class<?>> protocolXmlDataTypes = new HashMap<String, Class<?>>(
				0);
		protocolXmlDataTypes.put("/protocol/drugs/drug/name", List.class);
		protocolXmlDataTypes.put("/protocol/phases/phase", List.class);
		protocolXmlDataTypes.put("/protocol/title", String.class);

		Map<String, Object> results = xmlProcessor.listElementValuesByPaths(
				paths, protocolXmlDataTypes, originalXml);

		for (Entry<String, Object> entry : results.entrySet()) {
			logger.debug(entry.getKey() + ": ");
			if (entry.getValue() instanceof List) {
				for (Object v : (List) entry.getValue()) {
					logger.debug("[" + v.toString() + "]");
				}
			} else {
				logger.debug("{" + entry.getValue().toString() + "}");
			}

		}
		// assertEquals("assertEquals", expectedXml, finalXml);
	}

	// @Test
	public void testPullFromNewSubmission() throws XPathExpressionException,
			SAXException, IOException {
		String listPath = "/protocol/external-review-bodies";
		String originalXml = "<protocol><external-review-bodies><body enddate=\"10/14/2011\" id=\"7b9e49bf-c0ab-4afb-8f4b-448e3b78f2d1\" startdate=\"10/03/2011\"><body-name>asd</body-name></body></external-review-bodies></protocol>";

		logger.debug("$$$$$$$$$$$$$$$$$$$");
		String resultXml = xmlProcessor.listElementsByPath(listPath,
				originalXml, true);

		logger.trace("finalXml: " + resultXml);
	}

	// @Test
	public void testListElementsValueByPaths() throws XPathExpressionException,
			SAXException, IOException {
		String originalXml = "<protocol id=\"1\"><title>WhateverTitle</title><phases><phase>I</phase><phase>II</phase></phases><drugs><drug id=\"1\"><name>Whateverdrug</name></drug><drug id=\"2\"><name>iamno2</name></drug></drugs></protocol>";
		Set<String> paths = new HashSet<String>(0);
		paths.add("/protocol/drugs/drug/name");
		paths.add("/protocol/phases/phase");
		paths.add("/protocol/title");

		Map<String, List<String>> results = xmlProcessor
				.listElementStringValuesByPaths(paths, originalXml);

		for (Entry<String, List<String>> entry : results.entrySet()) {
			logger.debug(entry.getKey() + ": ");

			for (String v : entry.getValue()) {
				logger.debug("[" + v + "]");
			}

		}
		// assertEquals("assertEquals", expectedXml, finalXml);
	}

	private ApplicationContext applicationContext;

	// @Test
	public void testLoadChecklist() throws FileNotFoundException, IOException,
			XPathExpressionException, SAXException {

		// Resource checklistXmlFile =
		// applicationContext.getResource("/xml/checklist-sample.xml");
		Resource checklistXmlFile = new FileSystemResource(
				"src/main/webapp/static/xml/checklist-sample.xml");

		String checklistXmlString = xmlProcessor.loadXmlFile(checklistXmlFile
				.getFile());

		ProtocolFormType protocolFormType = ProtocolFormType.HUMAN_SUBJECT_RESEARCH_DETERMINATION;
		logger.debug(xmlProcessor.listElementsByPath(
				"/checklists/checklist-group[@committee='"
						+ Committee.IRB_PREREVIEW
						+ "' and @protocol-form-type='"
						+ protocolFormType.toString() + "']",
				checklistXmlString, false));

	}

	@Autowired(required = true)
	public void setXmlProcessor(XmlProcessor xmlProcessor) {
		this.xmlProcessor = xmlProcessor;
	}

	public XmlProcessor getXmlProcessor() {
		return xmlProcessor;
	}

	@Autowired(required = true)
	public void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	public ApplicationContext getApplicationContext() {
		return applicationContext;
	}

	public ProtocolMetaDataXmlService getProtocolMetaDataXmlService() {
		return protocolMetaDataXmlService;
	}
	
	@Autowired(required = true)
	public void setProtocolMetaDataXmlService(ProtocolMetaDataXmlService protocolMetaDataXmlService) {
		this.protocolMetaDataXmlService = protocolMetaDataXmlService;
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
	public void setProtocolFormXmlDataDao(ProtocolFormXmlDataDao protocolFormXmlDataDao) {
		this.protocolFormXmlDataDao = protocolFormXmlDataDao;
	}
}
