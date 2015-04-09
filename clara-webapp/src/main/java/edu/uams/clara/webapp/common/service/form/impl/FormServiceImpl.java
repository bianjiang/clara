package edu.uams.clara.webapp.common.service.form.impl;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.google.common.collect.Lists;
import edu.uams.clara.core.util.xml.DomUtils;
import edu.uams.clara.webapp.common.dao.usercontext.CitiMemberDao;
import edu.uams.clara.webapp.common.dao.usercontext.UserDao;
import edu.uams.clara.webapp.common.domain.form.Form;
import edu.uams.clara.webapp.common.domain.usercontext.CitiMember;
import edu.uams.clara.webapp.common.domain.usercontext.User;
import edu.uams.clara.webapp.common.service.UserService;
import edu.uams.clara.webapp.common.service.form.FormService;
import edu.uams.clara.webapp.xml.processor.XmlProcessor;

public class FormServiceImpl implements FormService {
	private final static Logger logger = LoggerFactory
			.getLogger(FormServiceImpl.class);
	
	private XmlProcessor xmlProcessor;
	
	private UserDao userDao;
	
	private CitiMemberDao citiMemberDao;
	
	private UserService userService;
	
	public enum UserSearchField{
		ROLE, RESPONSIBILITY;
	}

	@Override
	public String pullFromOtherForm(String listPath, String originalXml) {
		String resultXml = null;
		try {
			resultXml = xmlProcessor.listElementsByPath(listPath, originalXml,
					true);
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		resultXml = resultXml.replace("<list>", "");
		resultXml = resultXml.replace("</list>", "");
		resultXml = resultXml.replace("<list/>", "");

		return resultXml;
	}
	
	@Override
	public boolean isCurrentUserSpecificRoleOrNot(
			Form form, User currentUser, String roleName) {
		String x = "//staffs/staff/user[@id='" + currentUser.getId()
				+ "']/roles/role[contains(.,\""+ roleName +"\")]";
		logger.debug("xPath: " + x);
		XPath xpath = xmlProcessor.getXPathInstance();
		
		String xmlData = form.getMetaXml();

		boolean isPI = false;
		try {
			isPI = (Boolean) xpath.evaluate(x,
					xmlProcessor.loadXmlStringToDOM(xmlData),
					XPathConstants.BOOLEAN);
		} catch (Exception e) {
			logger.error("error when checking whether userId: "
					+ currentUser.getId()
					+ "; is the PI or Not on form: "
					+ form.getFormId() + "; due to: "
					+ e.getMessage());
		}
		logger.debug("isPI: " + isPI);
		return isPI;
	}
	
	@Override
	public String getAssignedReviewers(Form form) {
		String extraXmlData = form.getMetaXml();
		
		String assignedReviewersXml = "<assigned-reviewers>";;
		if (extraXmlData != null && !extraXmlData.isEmpty()){
			try {
				Document extraXmlDataDoc = xmlProcessor.loadXmlStringToDOM(extraXmlData);
				
				XPath xPath = xmlProcessor.getXPathInstance();

				NodeList assignedReviewers = (NodeList) xPath.evaluate("//assigned-reviewer", extraXmlDataDoc, XPathConstants.NODESET);
				
				for (int j = 0; j < assignedReviewers.getLength(); j ++){
					
					Element assignedReviewerEl = (Element)assignedReviewers.item(j);
					
					assignedReviewersXml += DomUtils.elementToString(assignedReviewerEl);							
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SAXException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (XPathExpressionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		assignedReviewersXml += "</assigned-reviewers>";

		return assignedReviewersXml;
	}
	
	@Override
	public List<User> getUsersByKeywordAndSearchField(String keyWord, String xmlData, UserSearchField userSearchField) {
		String lookupPath = "";

		switch (userSearchField){
		case ROLE:
			lookupPath = "//staffs/staff/user[roles/role[contains(.,\""+ keyWord +"\")]]";
			break;
		case RESPONSIBILITY:
			lookupPath = "//staffs/staff/user[reponsibilities/responsibility[contains(.,\""+ keyWord +"\")]]";
			break;
		default:
			break;
		}
		
		List<User> userLst = Lists.newArrayList();
		try {
			Document xmlDataDoc = xmlProcessor.loadXmlStringToDOM(xmlData);
			
			XPath xPath = xmlProcessor.getXPathInstance();

			NodeList userNodeLst = (NodeList) xPath.evaluate(lookupPath, xmlDataDoc, XPathConstants.NODESET);

			if (userNodeLst.getLength() > 0){
				for (int i = 0; i < userNodeLst.getLength(); i++){
					Element currentEl = (Element) userNodeLst.item(i);
					
					String userIdStr = currentEl.getAttribute("id");
					
					if (userIdStr != null && !userIdStr.isEmpty()){
						User user = userDao.findById(Long.valueOf(userIdStr));
						
						userLst.add(user);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return userLst;
	}
	
	@Override
	public List<String> getNoClaraUsers(String metaData) {
		List<String> noClaraUsersList = Lists.newArrayList();
		
		String lookupPath = "//staffs/staff/user";
		
		try {
			Document doc = xmlProcessor.loadXmlStringToDOM(metaData);
			
			XPath xPath = xmlProcessor.getXPathInstance();
			
			NodeList userNodeLst = (NodeList) xPath.evaluate(lookupPath, doc, XPathConstants.NODESET);
			
			if (userNodeLst != null && userNodeLst.getLength() > 0)  {
				for (int i = 0; i < userNodeLst.getLength(); i++) {
					Element currentUserEl = (Element) userNodeLst.item(i);
					
					if (currentUserEl.getAttribute("id") == null || currentUserEl.getAttribute("id").isEmpty()) {
						String lastName = currentUserEl.getElementsByTagName("lastname").item(0).getTextContent();
						
						String firstName = currentUserEl.getElementsByTagName("firstname").item(0).getTextContent();
						
						noClaraUsersList.add(lastName + "," + firstName);
					}
				}
			}
		} catch (Exception e) {
			
		}
		
		return noClaraUsersList;
	}
	
	@Override
	public Map<String, List<String>> getValuesFromXmlString(String xmlString,
			List<String> xPathList) {
		Map<String, List<String>> values = null;
		
		if (StringUtils.hasText(xmlString)) {
			Set<String> valueKeys = new HashSet<String>(xPathList);

			try {
				values = xmlProcessor.listElementStringValuesByPaths(valueKeys,
						xmlString);
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
		
		return values;
	}
	
	@Override
	public String getSafeStringValueByKey(Map<String, List<String>> values,
			String key, String exceptedReturnValueForNull) {
		if (values == null) return exceptedReturnValueForNull;
		return values.get(key) != null && values.get(key).size() > 0?values.get(key).get(0):exceptedReturnValueForNull;
	}
	
	private String updateProfile(User user) {
		Date now = new Date();

		DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
		
		String extraInfoXml = "<extra>";
		String citiInfo = "<citi-training-complete>n</citi-training-complete>";
		String notes = "";
		
		String profile = user.getProfile();
		
		String citiTrainingExpireDate = "";
		
		if (profile != null && !profile.isEmpty()) {
			Map<String, String> profileValues = userService.getUserProfileInfo(profile);
			
			citiTrainingExpireDate = profileValues.get("citiTrainingExpiredate");
			notes = "<notes>" + profileValues.get("notes") + "</notes>";
		}
		
		if (!citiTrainingExpireDate.isEmpty()) {
			try {
				Date citiExpireDate = df.parse(citiTrainingExpireDate);
				
				if (!citiExpireDate.before(now)) {
					citiInfo = "<citi-training-complete>y</citi-training-complete>";
				}
			} catch (ParseException pe) {
				
			}
			
			//citiInfo = "<citi-training-complete>y</citi-training-complete>";
		} else {
			try {
				List<CitiMember> citiMembers = citiMemberDao
						.listCitiMemberByUser(user);
				for (CitiMember citiMember : citiMembers) {

					try {
						Date expireDate = df.parse(citiMember
								.getDateCompletionExpires());
						if (!expireDate.before(now)) {
							citiInfo = "<citi-training-complete>y</citi-training-complete>";
						}
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						//e.printStackTrace();
					}

				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
		
		extraInfoXml += citiInfo + notes;
		
		extraInfoXml += "</extra>";
		
		return extraInfoXml;
	}
	
	@Override
	public String addExtraStaffInformation(String formBaseTag, String formXmlData) {
		String finalMergedString = formXmlData;
		
		try {
			List<String> userIds = xmlProcessor.getAttributeValuesByPathAndAttributeName("/"+ formBaseTag +"/staffs/staff/user", formXmlData, "id");

			if (userIds != null && !userIds.isEmpty()) {
				Document xmlDataDoc = xmlProcessor.loadXmlStringToDOM(finalMergedString);
				
				NodeList staffNodeLst = xmlDataDoc.getElementsByTagName("user");
				
				if (staffNodeLst != null && staffNodeLst.getLength() > 0) {
					for (int i = 0; i < staffNodeLst.getLength(); i++) {
						Element currentEl = (Element) staffNodeLst.item(i);
						
						String userIdStr = currentEl.getAttribute("id");
						
						long userId = Long.valueOf(userIdStr);
						
						User user = userDao.findById(userId);
												
						String extraInfoXml = updateProfile(user);
						
						try {
							Document newDom = xmlProcessor.parse(extraInfoXml);
							
							Element newElementRoot = (Element) newDom.getFirstChild();
							
							currentEl.appendChild(xmlDataDoc.importNode(newElementRoot, true));
						} catch (Exception e) {
							e.printStackTrace();
						}
						
					}
				}
				
				finalMergedString = DomUtils.elementToString(xmlDataDoc);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			
			finalMergedString = formXmlData;
		}
		
		return finalMergedString;
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

	public CitiMemberDao getCitiMemberDao() {
		return citiMemberDao;
	}
	
	@Autowired(required=true)
	public void setCitiMemberDao(CitiMemberDao citiMemberDao) {
		this.citiMemberDao = citiMemberDao;
	}

	public UserService getUserService() {
		return userService;
	}
	
	@Autowired(required=true)
	public void setUserService(UserService userService) {
		this.userService = userService;
	}

}
