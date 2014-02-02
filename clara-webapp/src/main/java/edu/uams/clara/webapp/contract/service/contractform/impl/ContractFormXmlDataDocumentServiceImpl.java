package edu.uams.clara.webapp.contract.service.contractform.impl;

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
import edu.uams.clara.webapp.contract.dao.businesslogicobject.ContractFormStatusDao;
import edu.uams.clara.webapp.contract.dao.businesslogicobject.ContractStatusDao;
import edu.uams.clara.webapp.contract.dao.contractform.ContractFormDao;
import edu.uams.clara.webapp.contract.dao.contractform.ContractFormXmlDataDocumentDao;
import edu.uams.clara.webapp.contract.domain.Contract;
import edu.uams.clara.webapp.contract.domain.businesslogicobject.ContractFormStatus;
import edu.uams.clara.webapp.contract.domain.businesslogicobject.enums.ContractFormStatusEnum;
import edu.uams.clara.webapp.contract.domain.contractform.ContractFormXmlData;
import edu.uams.clara.webapp.contract.service.contractform.ContractFormXmlDataDocumentService;
import edu.uams.clara.webapp.xml.processor.XmlProcessor;

public class ContractFormXmlDataDocumentServiceImpl implements
		ContractFormXmlDataDocumentService {
	private final static Logger logger = LoggerFactory
			.getLogger(ContractFormXmlDataDocumentService.class);

	private ContractFormXmlDataDocumentDao contractFormXmlDataDocumentDao;

	private ContractFormStatusDao contractFormStatusDao;

	private ContractStatusDao contractStatusDao;

	private ContractFormDao contractFormDao;

	private UserDao userDao;

	private ObjectAclService objectAclService;

	private XmlProcessor xmlProcessor;

	private ResourceLoader resourceLoader;

	@Value("${contractRequiredDocumentsXml.url}")
	private String contractRequiredDocumentsXml;

	@Value("${documentTypesXml.url}")
	private String documentTypesXml;

	@Override
	public Map<String, Boolean> checkRequiredDocuments(
			ContractFormXmlData contractFormXmlData) throws IOException,
			SAXException, XPathExpressionException {
		Map<String, Boolean> resultMap = new HashMap<String, Boolean>();

		List<String> documentTypeList = contractFormXmlDataDocumentDao
				.listContractFormXmlDataDocumentCategories(contractFormXmlData
						.getContractForm().getId());

		// Resource requiredDocResource =
		// resourceLoader.getResource(contractRequiredDocumentsXml);
		XPath xPath = xmlProcessor.getXPathInstance();

		Document requiredDocDom = xmlProcessor
				.loadXmlFileToDOM(contractRequiredDocumentsXml);

		Document documentTypesDocDom = xmlProcessor
				.loadXmlFileToDOM(documentTypesXml);

		Element formEl = (Element) xPath.evaluate("/forms/form[@type=\""
				+ contractFormXmlData.getContractForm().getContractFormType()
						.toString() + "\"]", requiredDocDom,
				XPathConstants.NODE);

		String formRequiredDoc = formEl.getAttribute("required");

		if (formRequiredDoc != null && !formRequiredDoc.isEmpty()) {
			xPath.reset();
			NodeList contractValidationDocGroupLst = (NodeList) xPath.evaluate(
					"/document-types/document-type[@contract-validation-value=\""
							+ formRequiredDoc + "\"]", documentTypesDocDom,
					XPathConstants.NODESET);

			for (int j = 0; j < contractValidationDocGroupLst.getLength(); j++) {
				Element contractRequiredDocEl = (Element) contractValidationDocGroupLst
						.item(j);

				if (resultMap.containsKey(formRequiredDoc)) {
					if (!resultMap.get(formRequiredDoc)) {
						resultMap.put(formRequiredDoc, documentTypeList
								.contains(contractRequiredDocEl
										.getAttribute("value")));
					}
				} else {
					resultMap.put(formRequiredDoc, documentTypeList
							.contains(contractRequiredDocEl
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

					List<String> contractTypeLst = xmlProcessor
							.listElementStringValuesByPath(
									otherRequiredEl.getAttribute("path"),
									contractFormXmlData.getXmlData());

					boolean needCheckRequired = false;

					if (contractTypeLst != null && contractTypeLst.size() > 0) {

						if (otherRequiredEl.getAttribute("value").contains(",")) {
							List<String> valueLst = Arrays
									.asList(otherRequiredEl.getAttribute(
											"value").split(","));

							if (contractTypeLst.containsAll(valueLst)) {
								needCheckRequired = true;
							}
						} else {
							if (contractTypeLst.get(0).equals(
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
								NodeList contractValidationDocumentGroupLst = (NodeList) xPath
										.evaluate(
												"/document-types/document-type[@contract-validation-value=\""
														+ required + "\"]",
												documentTypesDocDom,
												XPathConstants.NODESET);

								for (int j = 0; j < contractValidationDocumentGroupLst
										.getLength(); j++) {
									Element contractRequiredDocEl = (Element) contractValidationDocumentGroupLst
											.item(j);

									if (resultMap.containsKey(required)) {
										if (!resultMap.get(required)) {
											resultMap
													.put(required,
															documentTypeList
																	.contains(contractRequiredDocEl
																			.getAttribute("value")));
										}
									} else {
										resultMap
												.put(required,
														documentTypeList
																.contains(contractRequiredDocEl
																		.getAttribute("value")));
									}
								}
							}
						} else {
							xPath.reset();
							NodeList contractValidationDocGroupLst = (NodeList) xPath
									.evaluate(
											"/document-types/document-type[@contract-validation-value=\""
													+ otherRequiredEl
															.getAttribute("required")
													+ "\"]",
											documentTypesDocDom,
											XPathConstants.NODESET);

							for (int j = 0; j < contractValidationDocGroupLst
									.getLength(); j++) {
								Element contractRequiredDocEl = (Element) contractValidationDocGroupLst
										.item(j);

								if (resultMap.containsKey(otherRequiredEl
										.getAttribute("required"))) {
									if (!resultMap.get(otherRequiredEl
											.getAttribute("required"))) {
										resultMap
												.put(otherRequiredEl
														.getAttribute("required"),
														documentTypeList
																.contains(contractRequiredDocEl
																		.getAttribute("value")));
									}
								} else {
									resultMap
											.put(otherRequiredEl
													.getAttribute("required"),
													documentTypeList
															.contains(contractRequiredDocEl
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
		//
		//
		// try{
		// List<String> contractTypeLst =
		// xmlProcessor.listElementStringValuesByPath("/contract/basic-information/contract-type",
		// contractFormXmlData.getXmlData());
		//
		// contractType = (contractTypeLst!=null && contractTypeLst.size() >
		// 0)?contractTypeLst.get(0):"";
		// } catch (Exception e){
		// e.printStackTrace();
		// }
		//
		//
		//
		// if (contractType.equals("clinical-trial-agreement")){
		//
		// resultMap.put("Budget Exhibit",
		// documentTypeList.contains("Budget Exhibit"));
		//
		// }
		//
		// resultMap.put("Contract", documentTypeList.contains("Contract"));
		logger.debug("result map: " + resultMap);
		return resultMap;
	}

	private List<ContractFormStatusEnum> canEditDocStatusList = Lists
			.newArrayList();
	{
		canEditDocStatusList.add(ContractFormStatusEnum.DRAFT);
		canEditDocStatusList
				.add(ContractFormStatusEnum.UNDER_CONTRACT_MANAGER_REVIEW);
	}

	@Override
	public Source listDocumentTypes(long contractId, long contractFormId,
			long userId, Committee committee) {
		User currentUser = userDao.findById(userId);
		
		if (contractFormId == 0) {
			try {
				Document documentTypesDoc = xmlProcessor
						.loadXmlFileToDOM(getDocumentTypesXml());
				
				//Redmine ticket#2843 Let Contract Admin, Manager, and Legal Review be able to upload, revise, rename documents through the document tab on the dashboard
				if (currentUser.getAuthorities().contains(Permission.CAN_UPLOAD_CONTRACT_DOCUMENT_AT_ANYTIME)) {
					Element rootEl = (Element) documentTypesDoc.getFirstChild();
					
					NodeList childLst = rootEl.getChildNodes();
					
					for (int i = 0; i < childLst.getLength(); i++) {
						if (childLst.item(i).getNodeType() == Node.ELEMENT_NODE) {
							Element currentEl = (Element) childLst.item(i);

							if (currentEl.getAttribute("only-for-display").equals(
									"true")) {
								continue;
							}

							// packet only used in protocol archive
							if (currentEl.getAttribute("category").equals("Packet")) {
								currentEl.setAttribute("write", "false");
								currentEl.setAttribute("read", "true");
								currentEl.setAttribute("update", "false");
							} else {
								currentEl.setAttribute("write", "true");
								currentEl.setAttribute("read", "true");
								currentEl.setAttribute("update", "true");
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

		
		ContractFormStatus latestContracFormtStatus = contractFormStatusDao
				.getLatestContractFormStatusByFormId(contractFormId);
		// String contractXml = contractFormDao.
		try {
			Document documentTypesDoc = xmlProcessor
					.loadXmlFileToDOM(getDocumentTypesXml());

			Element rootEl = (Element) documentTypesDoc.getFirstChild();

			NodeList childLst = rootEl.getChildNodes();

			boolean canEdit = false;

			canEdit = objectAclService.hasEditObjectAccess(Contract.class,
					contractId, currentUser);

			for (int i = 0; i < childLst.getLength(); i++) {
				if (childLst.item(i).getNodeType() == Node.ELEMENT_NODE) {
					Element currentEl = (Element) childLst.item(i);

					if (currentEl.getAttribute("only-for-display").equals(
							"true")) {
						continue;
					}

					// packet only used in protocol archive
					if (currentEl.getAttribute("category").equals("Packet")) {
						currentEl.setAttribute("write", "false");
						currentEl.setAttribute("read", "true");
						currentEl.setAttribute("update", "false");
					} else {
						if (latestContracFormtStatus.getContractFormStatus()
								.equals(ContractFormStatusEnum.DRAFT)
								&& canEdit) {
							currentEl.setAttribute("write", "true");
							currentEl.setAttribute("read", "true");
							currentEl.setAttribute("update", "true");
						}

						if (canEditDocStatusList
								.contains(latestContracFormtStatus
										.getContractFormStatus())
								&& currentUser.getAuthorities().contains(
										Permission.ROLE_CONTRACT_MANAGER)) {
							currentEl.setAttribute("write", "true");
							currentEl.setAttribute("read", "true");
							currentEl.setAttribute("update", "true");
						}

						if (!canEditDocStatusList
								.contains(latestContracFormtStatus
										.getContractFormStatus())
								&& currentUser.getAuthorities().contains(
										Permission.ROLE_CONTRACT_MANAGER)) {
							currentEl.setAttribute("write", "false");
							currentEl.setAttribute("read", "true");
							currentEl.setAttribute("update", "true");
						}

						if (!latestContracFormtStatus.getContractFormStatus()
								.equals(ContractFormStatusEnum.DRAFT)
								&& (currentUser.getAuthorities().contains(
										Permission.ROLE_COVERAGE_REVIEWER)
										|| currentUser
												.getAuthorities()
												.contains(
														Permission.ROLE_BUDGET_REVIEWER) || currentUser
										.getAuthorities().contains(
												Permission.ROLE_BUDGET_MANAGER))) {
							currentEl.setAttribute("write", "false");
							currentEl.setAttribute("read", "true");
							currentEl.setAttribute("update", "false");
						}

						if (!canEditDocStatusList
								.contains(latestContracFormtStatus
										.getContractFormStatus())
								&& (currentUser.getAuthorities().contains(
										Permission.ROLE_CONTRACT_ADMIN) || currentUser
										.getAuthorities()
										.contains(
												Permission.ROLE_CONTRACT_LEGAL_REVIEW))) {
							currentEl.setAttribute("write", "true");
							currentEl.setAttribute("read", "true");
							currentEl.setAttribute("update", "true");
						}
					}
				}
			}

			return XMLResponseHelper.newDataResponseStub(documentTypesDoc);
		} catch (Exception e) {
			e.printStackTrace();

			return XMLResponseHelper
					.newErrorResponseStub("Failed to load document types!");
		}
	}

	public ContractFormXmlDataDocumentDao getContractFormXmlDataDocumentDao() {
		return contractFormXmlDataDocumentDao;
	}

	@Autowired(required = true)
	public void setContractFormXmlDataDocumentDao(
			ContractFormXmlDataDocumentDao contractFormXmlDataDocumentDao) {
		this.contractFormXmlDataDocumentDao = contractFormXmlDataDocumentDao;
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

	public String getContractRequiredDocumentsXml() {
		return contractRequiredDocumentsXml;
	}

	public void setContractRequiredDocumentsXml(
			String contractRequiredDocumentsXml) {
		this.contractRequiredDocumentsXml = contractRequiredDocumentsXml;
	}

	public String getDocumentTypesXml() {
		return documentTypesXml;
	}

	public void setDocumentTypesXml(String documentTypesXml) {
		this.documentTypesXml = documentTypesXml;
	}

	public ContractFormStatusDao getContractFormStatusDao() {
		return contractFormStatusDao;
	}

	@Autowired(required = true)
	public void setContractFormStatusDao(
			ContractFormStatusDao contractFormStatusDao) {
		this.contractFormStatusDao = contractFormStatusDao;
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
	public void etContractFormDao(ContractFormDao contractFormDao) {
		this.contractFormDao = contractFormDao;
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
