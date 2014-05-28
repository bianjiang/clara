package edu.uams.clara.webapp.protocol.service.protocolform.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.transform.Source;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.dom4j.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.google.common.collect.Lists;

import edu.emory.mathcs.backport.java.util.Arrays;
import edu.uams.clara.webapp.common.dao.usercontext.UserDao;
import edu.uams.clara.webapp.common.domain.usercontext.User;
import edu.uams.clara.webapp.common.domain.usercontext.enums.Committee;
import edu.uams.clara.webapp.common.domain.usercontext.enums.Permission;
import edu.uams.clara.webapp.common.security.ObjectAclService;
import edu.uams.clara.webapp.common.util.XMLResponseHelper;
import edu.uams.clara.webapp.protocol.dao.businesslogicobject.ProtocolFormStatusDao;
import edu.uams.clara.webapp.protocol.dao.businesslogicobject.ProtocolStatusDao;
import edu.uams.clara.webapp.protocol.dao.protocolform.ProtocolFormDao;
import edu.uams.clara.webapp.protocol.dao.protocolform.ProtocolFormXmlDataDocumentDao;
import edu.uams.clara.webapp.protocol.domain.Protocol;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.ProtocolFormStatus;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.ProtocolStatus;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.enums.ProtocolFormStatusEnum;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.enums.ProtocolStatusEnum;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolForm;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolFormXmlData;
import edu.uams.clara.webapp.protocol.domain.protocolform.enums.ProtocolFormType;
import edu.uams.clara.webapp.protocol.service.protocolform.ProtocolFormXmlDataDocumentService;
import edu.uams.clara.webapp.xml.processor.XmlProcessor;

public class ProtocolFormXmlDataDocumentServiceImpl implements
		ProtocolFormXmlDataDocumentService {
	private final static Logger logger = LoggerFactory
			.getLogger(ProtocolFormXmlDataDocumentService.class);

	private ProtocolFormXmlDataDocumentDao protocolFormXmlDataDocumentDao;

	private ProtocolFormStatusDao protocolFormStatusDao;

	private ProtocolStatusDao protocolStatusDao;

	private ProtocolFormDao protocolFormDao;

	private UserDao userDao;

	private ObjectAclService objectAclService;

	private XmlProcessor xmlProcessor;

	private ResourceLoader resourceLoader;

	@Value("${protocolRequiredDocumentsXml.url}")
	private String protocolRequiredDocumentsXml;

	@Value("${documentTypesXml.url}")
	private String documentTypesXml;

	@Override
	public Map<String, Boolean> checkRequiredDocuments(
			ProtocolFormXmlData protocolFormXmlData) throws IOException,
			SAXException, XPathExpressionException {
		Map<String, Boolean> resultMap = new HashMap<String, Boolean>();

		List<String> documentTypeList = protocolFormXmlDataDocumentDao
				.listProtocolFormXmlDataDocumentCategories(protocolFormXmlData
						.getProtocolForm().getId());

		XPath xPath = xmlProcessor.getXPathInstance();

		Document requiredDocDom = xmlProcessor
				.loadXmlFileToDOM(protocolRequiredDocumentsXml);

		Document documentTypesDocDom = xmlProcessor
				.loadXmlFileToDOM(documentTypesXml);

		Element formEl = (Element) xPath.evaluate("/forms/form[@type=\""
				+ protocolFormXmlData.getProtocolForm().getProtocolFormType()
						.toString() + "\"]", requiredDocDom,
				XPathConstants.NODE);

		String formRequiredDoc = formEl.getAttribute("required");

		if (formRequiredDoc != null && !formRequiredDoc.isEmpty()) {
			xPath.reset();
			NodeList protocolValidationDocGroupLst = (NodeList) xPath.evaluate(
					"/document-types/document-type[@protocol-validation-value=\""
							+ formRequiredDoc + "\"]", documentTypesDocDom,
					XPathConstants.NODESET);

			for (int j = 0; j < protocolValidationDocGroupLst.getLength(); j++) {
				Element protocolRequiredDocEl = (Element) protocolValidationDocGroupLst
						.item(j);

				if (resultMap.containsKey(formRequiredDoc)) {
					if (!resultMap.get(formRequiredDoc)) {
						resultMap.put(formRequiredDoc, documentTypeList
								.contains(protocolRequiredDocEl
										.getAttribute("value")));
					}
				} else {
					resultMap.put(formRequiredDoc, documentTypeList
							.contains(protocolRequiredDocEl
									.getAttribute("value")));
				}

			}
		}

		NodeList otherRequiredNodeLst = formEl.getChildNodes();

		if (otherRequiredNodeLst != null
				&& otherRequiredNodeLst.getLength() > 0) {
			for (int i = 0; i < otherRequiredNodeLst.getLength(); i++) {
				if (otherRequiredNodeLst.item(i).getNodeType() == Node.ELEMENT_NODE) {
					Element otherRequiredEl = (Element) otherRequiredNodeLst
							.item(i);

					List<String> protocolTypeLst = xmlProcessor
							.listElementStringValuesByPath(
									otherRequiredEl.getAttribute("path"),
									protocolFormXmlData.getXmlData());

					boolean needCheckRequired = false;

					if (protocolTypeLst != null && protocolTypeLst.size() > 0) {

						if (otherRequiredEl.getAttribute("value").contains(",")) {
							List<String> valueLst = Arrays
									.asList(otherRequiredEl.getAttribute(
											"value").split(","));

							if (protocolTypeLst.containsAll(valueLst)) {
								needCheckRequired = true;
							}
						} else {
							if (protocolTypeLst.get(0).equals(
									otherRequiredEl.getAttribute("value"))) {
								needCheckRequired = true;
							}
						}
					}

					if (needCheckRequired) {
						String requiredValue = otherRequiredEl
								.getAttribute("required");

						if (requiredValue.contains(",")) {
							List<String> requiredValueLst = Arrays
									.asList(otherRequiredEl.getAttribute(
											"required").split(","));

							for (String required : requiredValueLst) {
								xPath.reset();
								NodeList protocolValidationDocumentGroupLst = (NodeList) xPath
										.evaluate(
												"/document-types/document-type[@contract-validation-value=\""
														+ required + "\"]",
												documentTypesDocDom,
												XPathConstants.NODESET);

								for (int j = 0; j < protocolValidationDocumentGroupLst
										.getLength(); j++) {
									Element protocolRequiredDocEl = (Element) protocolValidationDocumentGroupLst
											.item(j);

									if (resultMap.containsKey(required)) {
										if (!resultMap.get(required)) {
											resultMap
													.put(required,
															documentTypeList
																	.contains(protocolRequiredDocEl
																			.getAttribute("value")));
										}
									} else {
										resultMap
												.put(required,
														documentTypeList
																.contains(protocolRequiredDocEl
																		.getAttribute("value")));
									}
								}
							}
						} else {
							xPath.reset();
							NodeList protocolValidationDocGroupLst = (NodeList) xPath
									.evaluate(
											"/document-types/document-type[@protocol-validation-value=\""
													+ otherRequiredEl
															.getAttribute("required")
													+ "\"]",
											documentTypesDocDom,
											XPathConstants.NODESET);

							for (int j = 0; j < protocolValidationDocGroupLst
									.getLength(); j++) {
								Element protocolRequiredDocEl = (Element) protocolValidationDocGroupLst
										.item(j);

								if (resultMap.containsKey(otherRequiredEl
										.getAttribute("required"))) {
									if (!resultMap.get(otherRequiredEl
											.getAttribute("required"))) {
										resultMap
												.put(otherRequiredEl
														.getAttribute("required"),
														documentTypeList
																.contains(protocolRequiredDocEl
																		.getAttribute("value")));
									}
								} else {
									resultMap
											.put(otherRequiredEl
													.getAttribute("required"),
													documentTypeList
															.contains(protocolRequiredDocEl
																	.getAttribute("value")));
								}

							}
							// resultMap.put(otherRequiredEl.getAttribute("required"),
							// documentTypeList.contains(otherRequiredEl.getAttribute("required")));
						}
					}
				}

			}
		}

		logger.debug("result map: " + resultMap);
		return resultMap;
	}

	private List<ProtocolFormStatusEnum> canEditDocStatusLst = Lists
			.newArrayList();
	{
		canEditDocStatusLst.add(ProtocolFormStatusEnum.DRAFT);
		canEditDocStatusLst.add(ProtocolFormStatusEnum.PENDING_PI_ENDORSEMENT);
		canEditDocStatusLst.add(ProtocolFormStatusEnum.PENDING_PI_SIGN_OFF);
		canEditDocStatusLst.add(ProtocolFormStatusEnum.PENDING_TP_ENDORSEMENT);
		canEditDocStatusLst.add(ProtocolFormStatusEnum.UNDER_REVISION);
		// canEditDocStatusLst.add(ProtocolFormStatusEnum.PENDING_PL_ENDORSEMENT);
	}

	@Override
	public Source listDocumentTypes(long protocolId, long protocolFormId,
			long userId, Committee committee) {
		
		User currentUser = userDao.findById(userId);
		boolean canEdit = false;
		logger.debug("pId: " + protocolId + " currentUser: "
				+ currentUser.getId());
		canEdit = objectAclService.hasEditObjectAccess(Protocol.class,
				protocolId, currentUser);
		
		if (protocolFormId == 0) {
			try {
				Document documentTypesDoc = xmlProcessor
						.loadXmlFileToDOM(getDocumentTypesXml());
				
				Element rootEl = (Element) documentTypesDoc.getFirstChild();

				NodeList childLst = rootEl.getChildNodes();

				
				for (int i = 0; i < childLst.getLength(); i++) {
					if (childLst.item(i).getNodeType() == Node.ELEMENT_NODE) {
						Element currentEl = (Element) childLst.item(i);

						String category = currentEl.getAttribute("category");
						//logger.info(category);
						if (category.equals("Budget Document")) {
							// if
							// (currentEl.getAttribute("only-for-display").equals("true")){
							//
							// if(currentEl.getAttribute("category").equals("Budget Document")){
							//
							// }
							//
							// //continue;
							// }
							//
							// only budget reviewers and pi can read
//							if(!canEdit && currentUser.getAuthorities().contains(Permission.ROLE_IRB_REVIEWER)) {
//								currentEl.setAttribute("read", "false");
//								logger.debug("canEdit: " + canEdit);
//							}
							//logger.debug("canEdit: " + canEdit);
							//@ToDo: this check can be moved to the xml level, e.g., can-read-permissions="VIEW_BUDGET_DOCUMENTS"...
							if(canEdit || currentUser.getAuthorities().contains(Permission.VIEW_BUDGET_DOCUMENTS)) {
								currentEl.setAttribute("read", "true");
							}
							
						}
					}
				}

				return XMLResponseHelper.newDataResponseStub(documentTypesDoc);
			} catch (Exception ex) {
				return XMLResponseHelper
						.newErrorResponseStub("Failed to load document types!");
			}
		}

		

		ProtocolForm protocolForm = protocolFormDao.findById(protocolFormId);

		ProtocolStatus latestProtocolStatus = protocolStatusDao
				.findProtocolStatusByProtocolId(protocolId);

		ProtocolFormStatus lastestProtocolFormStatus = protocolFormStatusDao
				.getLatestProtocolFormStatusByFormId(protocolFormId);

		try {
			Document documentTypesDoc = xmlProcessor
					.loadXmlFileToDOM(getDocumentTypesXml());

			Element rootEl = (Element) documentTypesDoc.getFirstChild();

			NodeList childLst = rootEl.getChildNodes();
			//List<ProtocolFormStatusEnum> existingFormStatus =Lists.newArrayList();
			//for(ProtocolFormStatus pfs :protocolFormStatusDao.getAllProtocolFormStatusByParentFormId(protocolForm.getParent().getFormId())){
			//	existingFormStatus.add(pfs.getProtocolFormStatus());
			//}
			


			for (int i = 0; i < childLst.getLength(); i++) {
				if (childLst.item(i).getNodeType() == Node.ELEMENT_NODE) {
					Element currentEl = (Element) childLst.item(i);
			
					String category = currentEl.getAttribute("category");
					//logger.info(category);
					if (category.equals("Budget Document")) {
						// if
						// (currentEl.getAttribute("only-for-display").equals("true")){
						//
						// if(currentEl.getAttribute("category").equals("Budget Document")){
						//
						// }
						//
						// //continue;
						// }
						//
						// only budget reviewers and pi can read
//						if(!canEdit && currentUser.getAuthorities().contains(Permission.ROLE_IRB_REVIEWER)) {
//							currentEl.setAttribute("read", "false");
//							logger.debug("canEdit: " + canEdit);
//						}
						//logger.debug("canEdit: " + canEdit);
						if(canEdit || currentUser.getAuthorities().contains(Permission.VIEW_BUDGET_DOCUMENTS)) {
							currentEl.setAttribute("read", "true");
						}
						
					} else if (currentEl.getAttribute("category").equals(
							"Packet")) {
						if (latestProtocolStatus.getProtocolStatus().equals(
								ProtocolStatusEnum.OPEN)
								&& canEdit
								&& (protocolForm.getProtocolFormType().equals(
										ProtocolFormType.NEW_SUBMISSION) || protocolForm
										.getProtocolFormType().equals(
												ProtocolFormType.ARCHIVE))) {
							currentEl.setAttribute("read", "true");
							currentEl.setAttribute("write", "true");
							currentEl.setAttribute("update", "true");
						} else {
							currentEl.setAttribute("read", "true");
							currentEl.setAttribute("write", "false");
							currentEl.setAttribute("update", "false");
						}
					} else {
						if (latestProtocolStatus.getProtocolStatus().equals(
								ProtocolStatusEnum.OPEN)
								&& canEdit
								&& (protocolForm.getProtocolFormType().equals(
										ProtocolFormType.NEW_SUBMISSION) || protocolForm
										.getProtocolFormType().equals(
												ProtocolFormType.ARCHIVE))) {
							if (currentEl.getAttribute("category").equals(
							"Epic") && currentUser.getAuthorities().contains(
									Permission.ROLE_PHARMACY_REVIEW)) {
								currentEl.setAttribute("read", "true");
								currentEl.setAttribute("write", "true");
								currentEl.setAttribute("update", "true");
							} else {
								currentEl.setAttribute("read", "true");
								currentEl.setAttribute("write", "false");
								currentEl.setAttribute("update", "false");
							}
							
						} else {
							if (canEditDocStatusLst
									.contains(lastestProtocolFormStatus
											.getProtocolFormStatus())
									&& canEdit) {
								/*
								if(existingFormStatus.contains(ProtocolFormStatusEnum.UNDER_BUDGET_MANAGER_REVIEW)&&!existingFormStatus.contains(ProtocolFormStatusEnum.UNDER_IRB_PREREVIEW)){
									currentEl.setAttribute("read", "true");
									currentEl.setAttribute("write", "false");
									currentEl.setAttribute("update", "false");
								}else{
								currentEl.setAttribute("read", "true");
								currentEl.setAttribute("write", "true");
								currentEl.setAttribute("update", "true");
								}
								*/
								currentEl.setAttribute("read", "true");
								currentEl.setAttribute("write", "true");
								currentEl.setAttribute("update", "true");
							} else {
								if (currentUser.getAuthorities().contains(
										Permission.DELETE_DOCUMENT)) {
									currentEl.setAttribute("read", "true");
									currentEl.setAttribute("write", "true");
									currentEl.setAttribute("update", "true");
								} else {
									currentEl.setAttribute("read", "true");
									currentEl.setAttribute("write", "false");
									currentEl.setAttribute("update", "true");
								}
							}
						}
					}
				}

			}
			// what? why convert it to string then back to source?
			//String docString = DomUtils.elementToString(documentTypesDoc);

			return XMLResponseHelper.newDataResponseStub(documentTypesDoc);
		} catch (Exception e) {
			// e.printStackTrace();

			return XMLResponseHelper
					.newErrorResponseStub("Failed to load document types!");
		}
	}

	public ProtocolFormXmlDataDocumentDao getProtocolFormXmlDataDocumentDao() {
		return protocolFormXmlDataDocumentDao;
	}

	@Autowired(required = true)
	public void setProtocolFormXmlDataDocumentDao(
			ProtocolFormXmlDataDocumentDao protocolFormXmlDataDocumentDao) {
		this.protocolFormXmlDataDocumentDao = protocolFormXmlDataDocumentDao;
	}

	public XmlProcessor getXmlProcessor() {
		return xmlProcessor;
	}

	@Autowired(required = true)
	public void setXmlProcessor(XmlProcessor xmlProcessor) {
		this.xmlProcessor = xmlProcessor;
	}

	public ResourceLoader getResourceLoader() {
		return resourceLoader;
	}

	@Autowired(required = true)
	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}

	public String getDocumentTypesXml() {
		return documentTypesXml;
	}

	public void setDocumentTypesXml(String documentTypesXml) {
		this.documentTypesXml = documentTypesXml;
	}

	public String getProtocolRequiredDocumentsXml() {
		return protocolRequiredDocumentsXml;
	}

	public void setProtocolRequiredDocumentsXml(
			String protocolRequiredDocumentsXml) {
		this.protocolRequiredDocumentsXml = protocolRequiredDocumentsXml;
	}

	public ProtocolFormStatusDao getProtocolFormStatusDao() {
		return protocolFormStatusDao;
	}

	@Autowired(required = true)
	public void setProtocolFormStatusDao(
			ProtocolFormStatusDao protocolFormStatusDao) {
		this.protocolFormStatusDao = protocolFormStatusDao;
	}

	public ProtocolStatusDao getProtocolStatusDao() {
		return protocolStatusDao;
	}

	@Autowired(required = true)
	public void setProtocolStatusDao(ProtocolStatusDao protocolStatusDao) {
		this.protocolStatusDao = protocolStatusDao;
	}

	public ProtocolFormDao getProtocolFormDao() {
		return protocolFormDao;
	}

	@Autowired(required = true)
	public void setProtocolFormDao(ProtocolFormDao protocolFormDao) {
		this.protocolFormDao = protocolFormDao;
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

}
