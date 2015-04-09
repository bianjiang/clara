package edu.uams.clara.webapp.contract.web.contractform.ajax;

import java.util.Date;
import java.util.List;

import javax.xml.transform.Source;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.collect.Lists;

import edu.uams.clara.webapp.common.dao.usercontext.UserDao;
import edu.uams.clara.webapp.common.domain.usercontext.User;
import edu.uams.clara.webapp.common.domain.usercontext.enums.Committee;
import edu.uams.clara.webapp.common.domain.usercontext.enums.Permission;
import edu.uams.clara.webapp.common.security.ObjectAclService;
import edu.uams.clara.webapp.common.service.audit.AuditService;
import edu.uams.clara.webapp.common.util.JsonResponseHelper;
import edu.uams.clara.webapp.common.util.response.JsonResponse;
import edu.uams.clara.webapp.contract.dao.ContractDao;
import edu.uams.clara.webapp.contract.dao.businesslogicobject.ContractFormStatusDao;
import edu.uams.clara.webapp.contract.dao.contractform.ContractFormXmlDataDao;
import edu.uams.clara.webapp.contract.dao.contractform.ContractFormXmlDataDocumentDao;
import edu.uams.clara.webapp.contract.domain.Contract;
import edu.uams.clara.webapp.contract.domain.businesslogicobject.ContractFormStatus;
import edu.uams.clara.webapp.contract.domain.businesslogicobject.enums.ContractFormStatusEnum;
import edu.uams.clara.webapp.contract.domain.contractform.ContractFormXmlData;
import edu.uams.clara.webapp.contract.domain.contractform.ContractFormXmlDataDocument;
import edu.uams.clara.webapp.contract.domain.contractform.ContractFormXmlDataDocumentWrapper;
import edu.uams.clara.webapp.contract.service.contractform.ContractFormXmlDataDocumentService;
import edu.uams.clara.webapp.fileserver.dao.UploadedFileDao;
import edu.uams.clara.webapp.fileserver.domain.UploadedFile;
import edu.uams.clara.webapp.fileserver.service.SFTPService;
import edu.uams.clara.webapp.xml.processor.XmlProcessor;

@Controller
public class ContractFormXmlDataDocumentAjaxController {

	private final static Logger logger = LoggerFactory
	.getLogger(ContractFormXmlDataDocumentAjaxController.class);	
	
	private UploadedFileDao uploadedFileDao;

	private ContractFormXmlDataDocumentDao contractFormXmlDataDocumentDao;
	
	private ContractFormStatusDao contractFormStatusDao;
	
	private ContractDao contractDao;
	
	private UserDao userDao;
	
	private ObjectAclService objectAclService;
	
	private XmlProcessor xmlProcessor;
	
	private SFTPService sftpService;
	
	private AuditService auditService;
	
	private ContractFormXmlDataDocumentService contractFormXmlDataDocumentService;
	
	@Value("${documentTypesXml.url}")
	private String documentTypesDirPath;
	
	
	//private ContractFormXmlDataDocumentCacheCleaner contractFormXmlDataDocumentCacheCleaner;
	
	private ContractFormXmlDataDao contractFormXmlDataDao;
	
	@RequestMapping(value = "/ajax/contracts/{id}/contract-forms/{formId}/contract-form-xml-datas/{formXmlDataId}/documents/add", method = RequestMethod.POST)
	public @ResponseBody
	JsonResponse addContractFormXmlDataDocument(@PathVariable("formXmlDataId") long contractFormXmlDataId,
			@PathVariable("id") long contractId,
			@PathVariable("formId") long contractFormId,
			@RequestParam(value="parentFormXmlDataDocumentId", required=false) long parentContractFormXmlDataDocumentId,
			@RequestParam("userId") long userId,
			@RequestParam("uploadedFileId") long uploadedFileId,
			@RequestParam("category") String category,
			@RequestParam("categoryDescription") String categoryDesc,
			@RequestParam("committee") Committee committee,
			@RequestParam(value="title", required=false) String title) throws Exception {
		Contract contract = contractDao.findById(contractId);
		
		//logger.debug("formXmldataId: " + contractFormXmlDataId);
		//ContractFormXmlData contractFormXmlData = contractFormXmlDataDao.findById(contractFormXmlDataId);
		ContractFormXmlData contractFormXmlData = null;
		
		if (contractFormXmlDataId != 0) {
			contractFormXmlData = contractFormXmlDataDao.findById(contractFormXmlDataId);
		}
		
		ContractFormXmlDataDocument contractDocument = new ContractFormXmlDataDocument();		
		
		if(parentContractFormXmlDataDocumentId > 0){
			logger.debug("add a new version...");
			//remove cache
			//contractFormXmlDataDocumentCacheCleaner.removeContractFormXmlDataDocumentRevisionsCache(contractFormXmlDataId, parentContractFormXmlDataDocumentId);
			
			ContractFormXmlDataDocument parentContractDocument = new ContractFormXmlDataDocument();
			
			//parentContractDocument.setId(parentContractFormXmlDataDocumentId);
			
			parentContractDocument = contractFormXmlDataDocumentDao.findById(parentContractFormXmlDataDocumentId);
			
			if (contractFormXmlData == null) {
				contractFormXmlData = parentContractDocument.getContractFormXmlData();
			}
			
			contractDocument.setParent(parentContractDocument);
			
			long lastestVersionId = contractFormXmlDataDocumentDao.countDocumentRevisionsByParentId(parentContractFormXmlDataDocumentId);

			contractDocument.setVersionId(lastestVersionId + 1);
		}else{
			
			contractDocument.setParent(contractDocument);
			
			contractDocument.setVersionId(0);
		}
		

		UploadedFile up = uploadedFileDao.findById(uploadedFileId);
		
		contractDocument.setUploadedFile(up);
		if (title == null || title.isEmpty()) {
			title = up.getFilename();
		}
		contractDocument.setTitle(title);
		contractDocument.setCategory(category);
		contractDocument.setCategoryDesc(categoryDesc);
		contractDocument.setCommittee(committee);
		contractDocument.setCreated(new Date());
		
		contractDocument.setContractFormXmlData(contractFormXmlData);
		
		User u = new User();
		u.setId(userId);
		contractDocument.setUser(u);
		
		try {
			sftpService.uploadLocalUploadedFileToRemote(contract, up);
			
			contractDocument = contractFormXmlDataDocumentDao.saveOrUpdate(contractDocument);

			return new JsonResponse(false, contractDocument);
		} catch (Exception e) {
			e.printStackTrace();
			
			return new JsonResponse(true, "Failed to upload document!", "", false);
		}
	}
	
	@RequestMapping(value = "/ajax/contracts/{contractId}/contract-forms/{contractFormId}/contract-form-xml-datas/{contractFormXmlDataId}/documents/{contractFormXmlDataDocumentId}/rename", method = RequestMethod.POST, produces="application/json")
	public @ResponseBody
	JsonResponse renameContractFormXmlDataDocument(@PathVariable("contractId") long contractId,
			@PathVariable("contractFormId") long contractFormId,
			@PathVariable("contractFormXmlDataId") long contractFormXmlDataId,
			@PathVariable("contractFormXmlDataDocumentId") long contractFormXmlDataDocumentId,
			@RequestParam("userId") long userId,
			@RequestParam("title") String title) throws Exception {
				
				
		try{
			ContractFormXmlDataDocument contractDocument = contractFormXmlDataDocumentDao.findById(contractFormXmlDataDocumentId);
			contractDocument.setTitle(title);
			contractDocument = contractFormXmlDataDocumentDao.saveOrUpdate(contractDocument);
			
			return JsonResponseHelper.newDataResponseStub(contractDocument);
		}catch(Exception ex){
			logger.error("failed rename contractDocument: " + contractFormXmlDataDocumentId,  ex);
			return JsonResponseHelper.newErrorResponseStub("failed to rename the document, do you have the permission to do so?");
		}
	}
	
	/**
	 * 
	 * @param contractFormXmlDataId
	 * @param parentContractFormXmlDataDocumentId
	 * @return
	 */
	@RequestMapping(value = "/ajax/contracts/{contractId}/contract-forms/{contractFormId}/contract-form-xml-datas/{contractFormXmlDataId}/documents/{contractFormXmlDataDocumentId}/list-versions")
	/*
	@Cacheable(cacheName = "contractFormXmlDataDocumentRevisionsCache", keyGenerator = @KeyGenerator(name = "HashCodeCacheKeyGenerator", properties = {
			@Property(name = "includeMethod", value = "false") }))*/
	public @ResponseBody
	List<ContractFormXmlDataDocumentWrapper> listContractFormXmlDataDocumentVersions(
			@PathVariable("contractFormXmlDataDocumentId") long contractFormXmlDataDocumentId) {

		List<ContractFormXmlDataDocumentWrapper> contractFormXmlDataDocumentVersions = contractFormXmlDataDocumentDao.listContractFormXmlDataDocumentRevisionsByParentId(contractFormXmlDataDocumentId);

		return contractFormXmlDataDocumentVersions;
	}

	
	@RequestMapping(value = "/ajax/contracts/{contractId}/contract-forms/{contractFormId}/contract-form-xml-datas/{contractFormXmlDataId}/documents/{contractFormXmlDataDocumentId}/change-type", method = RequestMethod.POST, produces="application/json")
	public @ResponseBody
	JsonResponse changeContractFormXmlDataDocumentType(@PathVariable("contractId") long contractId,
			@PathVariable("contractFormId") long contractFormId,
			@PathVariable("contractFormXmlDataDocumentId") long contractFormXmlDataDocumentId,
			@RequestParam("userId") long userId,
			@RequestParam("category") String category,
			@RequestParam("categoryDescription") String categoryDesc) throws Exception{
		List<ContractFormXmlDataDocument> finalDocumentLst = Lists.newArrayList();
				
		try{
			List<ContractFormXmlDataDocument> documentLst = contractFormXmlDataDocumentDao.listDocumentRevisionsByContractFormXmlDataDocumentId(contractFormXmlDataDocumentId);
			
			for (ContractFormXmlDataDocument cfxdd : documentLst) {
				cfxdd.setCategory(category);
				cfxdd.setCategoryDesc(categoryDesc);
				
				cfxdd = contractFormXmlDataDocumentDao.saveOrUpdate(cfxdd);
				
				finalDocumentLst.add(cfxdd);
			}
			
			auditService.auditEvent("DOCUMENT_TYPE_UPDATE",
					"User: " + userId + " has changed document: "+ contractFormXmlDataDocumentId +" type to: "+ categoryDesc);
			
			return JsonResponseHelper.newDataResponseStub(finalDocumentLst);
		}catch(Exception ex){
			logger.error("failed change contractDocument type: " + contractFormXmlDataDocumentId,  ex);
			return JsonResponseHelper.newErrorResponseStub("failed to change the document's type, do you have the permission to do so?");
		}
	}
	
	@RequestMapping(value = "/ajax/contracts/{contractId}/contract-forms/{contractFormId}/contract-form-xml-datas/{contractFormXmlDataId}/documents/{contractFormXmlDataDocumentId}/delete", method = RequestMethod.POST)
	public @ResponseBody 
	JsonResponse deleteContractFormXmlDataDocument(@PathVariable("contractId") long contractId,
			@PathVariable("contractFormId") long contractFormId,
			@PathVariable("contractFormXmlDataDocumentId") long contractFormXmlDataDocumentId,
			@RequestParam("userId") long userId,
			@RequestParam("committee") Committee committee){
		ContractFormXmlDataDocument pfdd = contractFormXmlDataDocumentDao.findById(contractFormXmlDataDocumentId);
		ContractFormStatus contractFormStatus = contractFormStatusDao.getLatestContractFormStatusByFormId(contractFormId);
		
		User currentUser = userDao.findById(userId);
		
		boolean deletable = false;
		logger.debug("permission: " + currentUser.getAuthorities());
		if (currentUser.getAuthorities().contains(Permission.DELETE_CONTRACT_DOCUMENT)){
			deletable = true;
		} else {
			if (contractFormStatus.getContractFormStatus().equals(ContractFormStatusEnum.DRAFT)){
				if (objectAclService.hasEditObjectAccess(Contract.class, contractId, currentUser)){
					deletable = true;
				}
			}
		}
		
		/*
		if (contractFormStatus.equals(ContractFormStatusEnum.DRAFT)){
			if (objectAclService.isObjectAccessible(Contract.class, contractId, currentUser)){
				deletable = true;
			}
		} else {
			if (currentUser.getAuthorities().contains(Permission.DELETE_DOCUMENT) || currentUser.getAuthorities().contains(Permission.DELETE_CONTRACT_DOCUMENT)){
				deletable = true;
			}
		}
		*/
		logger.debug("detetable: " + deletable);
		try{
			if (deletable){
				pfdd.setRetired(true);
				pfdd.setCreated(new Date());
				pfdd.setUser(currentUser);
				pfdd.setCommittee(committee);
				
				contractFormXmlDataDocumentDao.saveOrUpdate(pfdd);
			} else {
				return new JsonResponse(true, "You do not have right to delete this document!", "", false, null);
			}
		} catch (Exception e){
			e.printStackTrace();
			return new JsonResponse(true, "Failed to delete this document!", "", false, null);
		}
		 
		return new JsonResponse(false, "", "", false, null);
	}
	
	@RequestMapping(value = "/ajax/contracts/{contractId}/contract-forms/{contractFormId}/list-doc-types", method = RequestMethod.GET, produces="application/xml")
	public @ResponseBody 
	Source listContractDocumentTypes(@PathVariable("contractId") long contractId,
			@PathVariable("contractFormId") long contractFormId,
			@RequestParam("userId") long userId,
			@RequestParam(value="committee", required=false) Committee committee,
			@RequestParam(value="docAction", required=false) String docAction){
		return contractFormXmlDataDocumentService.listDocumentTypes(contractId, contractFormId, userId, committee);
	}

	@Autowired(required=true)
	public void setUploadedFileDao(UploadedFileDao uploadedFileDao) {
		this.uploadedFileDao = uploadedFileDao;
	}

	public UploadedFileDao getUploadedFileDao() {
		return uploadedFileDao;
	}

	@Autowired(required=true)
	public void setContractFormXmlDataDocumentDao(ContractFormXmlDataDocumentDao contractFormXmlDataDocumentDao) {
		this.contractFormXmlDataDocumentDao = contractFormXmlDataDocumentDao;
	}


	public ContractFormXmlDataDocumentDao getContractFormXmlDataDocumentDao() {
		return contractFormXmlDataDocumentDao;
	}

	@Autowired(required=true)
	public void setContractFormXmlDataDao(ContractFormXmlDataDao contractFormXmlDataDao) {
		this.contractFormXmlDataDao = contractFormXmlDataDao;
	}


	public ContractFormXmlDataDao getContractFormXmlDataDao() {
		return contractFormXmlDataDao;
	}

	public ContractFormStatusDao getContractFormStatusDao() {
		return contractFormStatusDao;
	}
	
	@Autowired(required=true)
	public void setContractFormStatusDao(ContractFormStatusDao contractFormStatusDao) {
		this.contractFormStatusDao = contractFormStatusDao;
	}

	public UserDao getUserDao() {
		return userDao;
	}
	
	@Autowired(required=true)
	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}

	public ObjectAclService getObjectAclService() {
		return objectAclService;
	}
	
	@Autowired(required=true)
	public void setObjectAclService(ObjectAclService objectAclService) {
		this.objectAclService = objectAclService;
	}

	public SFTPService getSftpService() {
		return sftpService;
	}

	@Autowired(required=true)
	public void setSftpService(SFTPService sftpService) {
		this.sftpService = sftpService;
	}

	public String getDocumentTypesDirPath() {
		return documentTypesDirPath;
	}

	public void setDocumentTypesDirPath(String documentTypesDirPath) {
		this.documentTypesDirPath = documentTypesDirPath;
	}

	public XmlProcessor getXmlProcessor() {
		return xmlProcessor;
	}
	
	@Autowired(required=true)
	public void setXmlProcessor(XmlProcessor xmlProcessor) {
		this.xmlProcessor = xmlProcessor;
	}

	public ContractFormXmlDataDocumentService getContractFormXmlDataDocumentService() {
		return contractFormXmlDataDocumentService;
	}
	
	@Autowired(required=true)
	public void setContractFormXmlDataDocumentService(
			ContractFormXmlDataDocumentService contractFormXmlDataDocumentService) {
		this.contractFormXmlDataDocumentService = contractFormXmlDataDocumentService;
	}

	public ContractDao getContractDao() {
		return contractDao;
	}
	
	@Autowired(required=true)
	public void setContractDao(ContractDao contractDao) {
		this.contractDao = contractDao;
	}
	
	public AuditService getAuditService() {
		return auditService;
	}
	
	@Autowired(required=true)
	public void setAuditService(AuditService auditService) {
		this.auditService = auditService;
	}
	
}
