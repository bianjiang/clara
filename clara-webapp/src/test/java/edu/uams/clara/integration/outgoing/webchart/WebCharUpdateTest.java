package edu.uams.clara.integration.outgoing.webchart;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import edu.uams.clara.integration.incoming.aria.dao.AriaProtocolUpdateDao;
import edu.uams.clara.webapp.common.dao.usercontext.UserDao;
import edu.uams.clara.webapp.common.domain.usercontext.User;
import edu.uams.clara.webapp.common.service.impl.UserServiceImpl;
import edu.uams.clara.webapp.protocol.dao.ProtocolDao;
import edu.uams.clara.webapp.protocol.dao.businesslogicobject.ProtocolStatusDao;
import edu.uams.clara.webapp.protocol.domain.Protocol;
import edu.uams.clara.webapp.xml.processor.XmlProcessor;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "file:src/test/java/edu/uams/clara/integration/outgoing/webchart/WebCharUpdateTest-context.xml" })
public class WebCharUpdateTest {
	private final static Logger logger = LoggerFactory
			.getLogger(WebCharUpdateTest.class);

	private ProtocolDao protocolDao;
	private AriaProtocolUpdateDao ariaProtocolUpdateDao;
	private XmlProcessor xmlProcessor;
	private ProtocolStatusDao protocolStatusDao;
	private UserDao userDao;
	private UserServiceImpl userServiceImpl;
	
	

	@Test
	public void updateARIAUserProtocol() throws IOException, XPathExpressionException {
		List<Object[]> ariaUserCodeList = ariaProtocolUpdateDao
				.findAllAriaUserIDinARIAUserProtocol();

		// updating exisiting user code first
		for (int i = 0; i < ariaUserCodeList.size(); i++) {
			Object[] ariaUserProtocol = ariaUserCodeList.get(i);
			if (ariaUserProtocol[0] != null) {
				String ariaUserID = (String) ariaUserProtocol[0];
				if(!ariaUserID.isEmpty()){
				int ariaUserIDInt = Integer.valueOf(ariaUserID);
				String userIDStr = "";
				if (!ariaUserID.isEmpty()) {
					
					String sapIDStr = ariaProtocolUpdateDao
							.findAriaUserSapByUserID(ariaUserIDInt);
					
					try{
						Integer.valueOf(sapIDStr);
					}catch(Exception e){
						sapIDStr="";
					}
					if (!sapIDStr.isEmpty()) {
						// convert sap in into clara format, remove 000 in
						// front
						
						int sapID = Integer.valueOf(sapIDStr);
						sapIDStr = String.valueOf(sapID);
						// if user existed, directly use the id
						if (!userDao.getUserBySAP(sapIDStr).isEmpty()) {
							userIDStr = ""
									+ userDao.getUserBySAP(sapIDStr).get(0)
											.getId();
						}
						// else create the user
						else {
							try {
								userServiceImpl.getAndUpdateUserBySap(sapIDStr,
										true);
								userIDStr = ""
										+ userDao.getUserBySAP(sapIDStr).get(0)
												.getId();
							} catch (Exception e) {
								 //logger.debug("user not found in Ldap: "+sapIDStr);
								 userIDStr = "";
							}
						}

					}
					
				}
				//user not in clara and cannot be created 
				if(userIDStr.isEmpty()){
					userIDStr = String.valueOf(ariaUserIDInt+1000000);
				}
				int ariaProtocolID = (int)ariaUserProtocol[2];
				Date date = new Date();
				ariaProtocolUpdateDao.updateAriaUserIDinARIAUserProtocol(ariaProtocolID, date, userIDStr);
				
			}
			}
		}//end for
		//add user and protocol pair in clara and not in ariauserProtocol
		List<Protocol> protocolList = protocolDao.findAll();
		for (int i = 0; i < protocolList.size(); i++) {
			//get userid in protocol
			Protocol protocol = protocolList.get(i);
			String protocolXml = protocol.getMetaDataXml();
			Document doc = null;
			try {
				doc = xmlProcessor.loadXmlStringToDOM(protocolXml);
			} catch (SAXException e) {
				e.printStackTrace();
			}
			XPathFactory xPathFactory = XPathFactory.newInstance();
			XPath xPath = xPathFactory.newXPath();
			XPathExpression xPathExpression = null;
			xPathExpression = xPath.compile("/protocol/staffs/staff/user");

			NodeList userList = (NodeList) xPathExpression.evaluate(doc,
					XPathConstants.NODESET);
			for(int j=0;j<userList.getLength();j++){
				Element user = (Element) userList.item(j);
				String userIDstr = user.getAttribute("id");
				//some aria user do not have id in clara
				if(!userIDstr.isEmpty()){
					int ariaProtocolID =ariaProtocolUpdateDao.findARIAUserProtocolID(userIDstr,protocol.getId()+"");
					if(ariaProtocolID==0){
						//insert it
						Date date = new Date();
						ariaProtocolUpdateDao.insertARIAUserProtocol(date, userIDstr,protocol.getId()+"");
					}
				}
			}
			
		}
	}

	//@Test
	public void udpateWebChartAriaUser(){
		List<Object[]> ariaUserList = ariaProtocolUpdateDao.findARIAUser();
		for(int i=0;i<ariaUserList.size();i++){
			if(ariaUserList.get(i)[1]!=null){
			String ariaUserID = (String)ariaUserList.get(i)[1];
			int ariaUserIDInt = Integer.valueOf(ariaUserID);
			String userIDStr = "";
			if (!ariaUserID.isEmpty()) {
				
				String sapIDStr = ariaProtocolUpdateDao
						.findAriaUserSapByUserID(ariaUserIDInt);
				
				try{
					Integer.valueOf(sapIDStr);
				}catch(Exception e){
					sapIDStr="";
				}
				if (!sapIDStr.isEmpty()) {
					// convert sap in into clara format, remove 000 in
					// front
					
					int sapID = Integer.valueOf(sapIDStr);
					sapIDStr = String.valueOf(sapID);
					// if user existed, directly use the id
					if (!userDao.getUserBySAP(sapIDStr).isEmpty()) {
						userIDStr = ""
								+ userDao.getUserBySAP(sapIDStr).get(0)
										.getId();
					}
					// else create the user
					else {
						try {
							userServiceImpl.getAndUpdateUserBySap(sapIDStr,
									true);
							userIDStr = ""
									+ userDao.getUserBySAP(sapIDStr).get(0)
											.getId();
						} catch (Exception e) {
							 //logger.debug("user not found in Ldap: "+sapIDStr);
							 userIDStr = "";
						}
					}

				}
				
				
				
			}
			//user not in clara and cannot be created 
			if(userIDStr.isEmpty()){
				userIDStr = String.valueOf(ariaUserIDInt+1000000);
				int ARIAUserID = (int)ariaUserList.get(i)[0];
				Date date = new Date();
				ariaProtocolUpdateDao.updateAriaUserUserCodeOnly(ARIAUserID, date, userIDStr);
				}
			else{
				int ARIAUserID = (int)ariaUserList.get(i)[0];
				Date date = new Date();
				User user = userDao.findById(Long.valueOf(userIDStr));
				String name = user.getPerson().getFullname();
				String phone = user.getPerson().getWorkphone();
				String email =  user.getPerson().getEmail();
				ariaProtocolUpdateDao.updateAriaUser(ARIAUserID, date, userIDStr,name,phone,email);
				
			}
			
			
		}
		}
	}
	
	
	//@Test
	public void updateAriaProtocols() throws XPathExpressionException,
			IOException {
		List<Protocol> protocolList = protocolDao.findAll();
		for (int i = 0; i < protocolList.size(); i++) {
			Protocol protocol = protocolList.get(i);
			int ariaProtocolID = 0;
			try {
				ariaProtocolID = ariaProtocolUpdateDao
						.getAriaProtocolByProtocolID(protocol
								.getProtocolIdentifier());
			} catch (Exception e) {
				ariaProtocolID = 0;
			}

			Date date = new Date();
			String protocolXml = protocol.getMetaDataXml();
			Document doc = null;
			try {
				doc = xmlProcessor.loadXmlStringToDOM(protocolXml);
			} catch (SAXException e) {
				e.printStackTrace();
			}
			XPathFactory xPathFactory = XPathFactory.newInstance();
			XPath xPath = xPathFactory.newXPath();
			XPathExpression xPathExpression = null;

			xPathExpression = xPath.compile("/protocol/title/text()");

			NodeList titlelist = (NodeList) xPathExpression.evaluate(doc,
					XPathConstants.NODESET);
			String title = "";
			if (titlelist.getLength() > 0) {
				title = titlelist.item(0).getNodeValue();
			}

			String status = protocolStatusDao
					.findProtocolStatusByProtocolId(protocol.getId())
					.getProtocolStatus().toString();
			xPathExpression = xPath
					.compile("/protocol/staffs/staff/user[roles/role[text()='"
							+ "Principal Investigator" + "']]/lastname/text()");

			NodeList piLastNamelist = (NodeList) xPathExpression.evaluate(doc,
					XPathConstants.NODESET);
			String piLastname = "";
			if (piLastNamelist.getLength() > 0) {
				piLastname = piLastNamelist.item(0).getNodeValue();
			}
			xPathExpression = xPath
					.compile("/protocol/staffs/staff/user[roles/role[text()='"
							+ "Principal Investigator" + "']]/firstname/text()");

			NodeList piFirstNamelist = (NodeList) xPathExpression.evaluate(doc,
					XPathConstants.NODESET);
			String piFirsttname = "";
			if (piFirstNamelist.getLength() > 0) {
				piFirsttname = piFirstNamelist.item(0).getNodeValue();
			}
			String piname = piFirsttname + " " + piLastname;
			// the protocl is not in airaProtocols
			if (ariaProtocolID == 0) {
				logger.debug("inserting...");
				ariaProtocolUpdateDao
						.insertAriaProtocols(date,
								protocol.getProtocolIdentifier(), title,
								piname, status);
			}
			// if the protocol exist update it
			else {
				logger.debug("updating...");
				ariaProtocolUpdateDao
						.updateAriaProtocols(ariaProtocolID, date,
								protocol.getProtocolIdentifier(), title,
								piname, status);
			}

		}

		List<Object[]> ariaProtocols = ariaProtocolUpdateDao
				.getAllAriaProtocols();

		for (int i = 0; i < ariaProtocols.size(); i++) {
			Object[] ariaProtocol = ariaProtocols.get(i);
			if (ariaProtocol[1] == null) {
				continue;
			}

			String protocolIDStr = (String) ariaProtocol[1];
			long protocolID = Long.valueOf(protocolIDStr);
			Protocol protocol = null;
			try {
				protocol = protocolDao.findById(protocolID);
			} catch (Exception e) {
				protocol = null;
			}
			if (protocol == null) {
				continue;
			}

		}
	}

	// @Test
	public void insertTest() {
		Date date = new Date();
		ariaProtocolUpdateDao.insertAriaProtocols(date, "test", "test", "test",
				"test");
		// ariaProtocolUpdateDao.getAriaProtocolByProtocolID("00003");
	}

	public ProtocolDao getProtocolDao() {
		return protocolDao;
	}

	@Autowired(required = true)
	public void setProtocolDao(ProtocolDao protocolDao) {
		this.protocolDao = protocolDao;
	}

	public AriaProtocolUpdateDao getAriaProtocolUpdateDao() {
		return ariaProtocolUpdateDao;
	}

	@Autowired(required = true)
	public void setAriaProtocolUpdateDao(
			AriaProtocolUpdateDao ariaProtocolUpdateDao) {
		this.ariaProtocolUpdateDao = ariaProtocolUpdateDao;
	}

	public XmlProcessor getXmlProcessor() {
		return xmlProcessor;
	}

	@Autowired(required = true)
	public void setXmlProcessor(XmlProcessor xmlProcessor) {
		this.xmlProcessor = xmlProcessor;
	}

	public ProtocolStatusDao getProtocolStatusDao() {
		return protocolStatusDao;
	}

	@Autowired(required = true)
	public void setProtocolStatusDao(ProtocolStatusDao protocolStatusDao) {
		this.protocolStatusDao = protocolStatusDao;
	}

	public UserDao getUserDao() {
		return userDao;
	}

	@Autowired(required = true)
	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}

	public UserServiceImpl getUserServiceImpl() {
		return userServiceImpl;
	}

	@Autowired(required = true)
	public void setUserServiceImpl(UserServiceImpl userServiceImpl) {
		this.userServiceImpl = userServiceImpl;
	}

}
