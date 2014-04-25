package edu.uams.clara.webapp.protocol.web.protocolform.ajax;

import java.util.Date;
import java.util.List;

import javax.xml.transform.Source;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import edu.uams.clara.webapp.common.dao.usercontext.UserDao;
import edu.uams.clara.webapp.common.domain.usercontext.User;
import edu.uams.clara.webapp.common.domain.usercontext.enums.Committee;
import edu.uams.clara.webapp.common.domain.usercontext.enums.Permission;
import edu.uams.clara.webapp.common.security.ObjectAclService;
import edu.uams.clara.webapp.common.service.audit.AuditService;
import edu.uams.clara.webapp.common.util.JsonResponseHelper;
import edu.uams.clara.webapp.common.util.response.JsonResponse;
import edu.uams.clara.webapp.fileserver.dao.UploadedFileDao;
import edu.uams.clara.webapp.fileserver.domain.UploadedFile;
import edu.uams.clara.webapp.fileserver.service.SFTPService;
import edu.uams.clara.webapp.protocol.dao.businesslogicobject.ProtocolFormStatusDao;
import edu.uams.clara.webapp.protocol.dao.businesslogicobject.ProtocolStatusDao;
import edu.uams.clara.webapp.protocol.dao.protocolform.ProtocolFormDao;
import edu.uams.clara.webapp.protocol.dao.protocolform.ProtocolFormXmlDataDao;
import edu.uams.clara.webapp.protocol.dao.protocolform.ProtocolFormXmlDataDocumentDao;
import edu.uams.clara.webapp.protocol.domain.Protocol;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.ProtocolFormStatus;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.enums.ProtocolFormStatusEnum;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolFormXmlData;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolFormXmlDataDocument;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolFormXmlDataDocument.Status;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolFormXmlDataDocumentWrapper;
import edu.uams.clara.webapp.protocol.service.protocolform.ProtocolFormXmlDataDocumentService;
import edu.uams.clara.webapp.xml.processor.XmlProcessor;

@Controller
public class ProtocolFormXmlDataDocumentAjaxController {

	private final static Logger logger = LoggerFactory
	.getLogger(ProtocolFormXmlDataDocumentAjaxController.class);	
	
	private UploadedFileDao uploadedFileDao;

	private ProtocolFormXmlDataDocumentDao protocolFormXmlDataDocumentDao;
	
	//private ProtocolFormXmlDataDocumentCacheCleaner protocolFormXmlDataDocumentCacheCleaner;
	
	private ProtocolFormXmlDataDao protocolFormXmlDataDao;
	
	private ProtocolFormStatusDao protocolFormStatusDao;
	
	private ProtocolStatusDao protocolStatusDao;
	
	private ProtocolFormDao protocolFormDao;
	
	private UserDao userDao;
	
	private ObjectAclService objectAclService;
	
	private AuditService auditService;
	
	private ProtocolFormXmlDataDocumentService protocolFormXmlDataDocumentService;
	
	private XmlProcessor xmlProcessor;
	
	private SFTPService sftpService;
	
	
	@RequestMapping(value = "/ajax/protocols/{id}/protocol-forms/{formId}/protocol-form-xml-datas/{formXmlDataId}/documents/add", method = RequestMethod.POST)
	public @ResponseBody
	JsonResponse addProtocolFormXmlDataDocument(@PathVariable("formXmlDataId") long protocolFormXmlDataId,
			@PathVariable("id") long protocolId,
			@PathVariable("formId") long protocolFormId,
			@RequestParam(value="parentFormXmlDataDocumentId", required=false) long parentProtocolFormXmlDataDocumentId,
			@RequestParam("userId") long userId,
			@RequestParam("uploadedFileId") long uploadedFileId,
			@RequestParam("category") String category,
			@RequestParam("categoryDescription") String categoryDesc,
			@RequestParam("committee") Committee committee,
			@RequestParam(value="title", required=false) String title) throws Exception {
		
		logger.debug("formXmldataId: " + protocolFormXmlDataId);
		ProtocolFormXmlData protocolFormXmlData = protocolFormXmlDataDao.findById(protocolFormXmlDataId);
		
		ProtocolFormXmlDataDocument protocolDocument = new ProtocolFormXmlDataDocument();		
		
		if(parentProtocolFormXmlDataDocumentId > 0){
			logger.debug("add a new version...");
			//remove cache
			//protocolFormXmlDataDocumentCacheCleaner.removeProtocolFormXmlDataDocumentRevisionsCache(protocolFormXmlDataId, parentProtocolFormXmlDataDocumentId);
			
			ProtocolFormXmlDataDocument parentProtocolDocument = new ProtocolFormXmlDataDocument();
			
			parentProtocolDocument.setId(parentProtocolFormXmlDataDocumentId);
			
			protocolDocument.setParent(parentProtocolDocument);
			
			long lastestVersionId = protocolFormXmlDataDocumentDao.countDocumentRevisionsByParentId(parentProtocolFormXmlDataDocumentId);
			
			//ProtocolFormXmlDataDocument latestRevision = protocolFormXmlDataDocumentLst.get(protocolFormXmlDataDocumentLst.size()-1);
			
			protocolDocument.setVersionId(lastestVersionId + 1);
			
		}else{
			
			protocolDocument.setParent(protocolDocument);
			protocolDocument.setVersionId(0);
		}
		

		UploadedFile up = uploadedFileDao.findById(uploadedFileId);
		
		
		protocolDocument.setUploadedFile(up);
		if (title == null || title.isEmpty()) {
			title = up.getFilename();
		}

		protocolDocument.setTitle(title);
		protocolDocument.setCategory(category);
		protocolDocument.setCategoryDesc(categoryDesc);
		protocolDocument.setCommittee(committee);
		protocolDocument.setCreated(new Date());
		if (category.contains("packet")) {
			protocolDocument.setStatus(Status.PACKET_DOCUMENT);
		} else if (category.contains("epic")) {
			protocolDocument.setStatus(Status.EPIC_DOCUMENT);
		} else {
			if (committee.equals(Committee.CONTRACT_LEGAL_REVIEW)) {
				protocolDocument.setStatus(Status.FINAL_LEGAL_APPROVED);
			} else {
				protocolDocument.setStatus(Status.DRAFT);
			}
		}
		
		protocolDocument.setProtocolFormXmlData(protocolFormXmlData);
		
		User u = new User();
		u.setId(userId);
		protocolDocument.setUser(u);
		
		try {
			sftpService.uploadLocalUploadedFileToRemote(protocolFormXmlData.getProtocolForm().getProtocol(), up);
			
			protocolDocument = protocolFormXmlDataDocumentDao.saveOrUpdate(protocolDocument);
			
			auditService.auditEvent("DOCUMENT_CREATED",
					"User: " + userId + " has created document - documentId:"+ protocolDocument.getId());

			return new JsonResponse(false, protocolDocument);
		} catch (Exception e) {
			return new JsonResponse(true, "Failed to upload document!", "", false);
		}
	}
	
	/**
	 * 
	 * @param protocolFormXmlDataId
	 * @param parentProtocolFormXmlDataDocumentId
	 * @return
	 */
	@RequestMapping(value = "/ajax/protocols/{protocolId}/protocol-forms/{protocolFormId}/protocol-form-xml-datas/{protocolFormXmlDataId}/documents/{protocolFormXmlDataDocumentId}/list-versions")
	/*
	@Cacheable(cacheName = "protocolFormXmlDataDocumentRevisionsCache", keyGenerator = @KeyGenerator(name = "HashCodeCacheKeyGenerator", properties = {
			@Property(name = "includeMethod", value = "false") }))*/
	public @ResponseBody
	List<ProtocolFormXmlDataDocumentWrapper> listProtocolFormXmlDataDocumentVersions(
			@PathVariable("protocolFormXmlDataDocumentId") long protocolFormXmlDataDocumentId) {

		List<ProtocolFormXmlDataDocumentWrapper> protocolFormXmlDataDocumentVersions = protocolFormXmlDataDocumentDao.listProtocolFormXmlDataDocumentRevisionsByParentId(protocolFormXmlDataDocumentId);

		return protocolFormXmlDataDocumentVersions;
	}
	
	@RequestMapping(value = "/ajax/protocols/{protocolId}/protocol-forms/{protocolFormId}/protocol-form-xml-datas/{protocolFormXmlDataId}/documents/{protocolFormXmlDataDocumentId}/rename", method = RequestMethod.POST, produces="application/json")
	public @ResponseBody
	JsonResponse renameProtocolFormXmlDataDocument(@PathVariable("protocolId") long protocolId,
			@PathVariable("protocolFormId") long protocolFormId,
			@PathVariable("protocolFormXmlDataDocumentId") long protocolFormXmlDataDocumentId,
			@RequestParam("userId") long userId,
			@RequestParam("title") String title) throws Exception {
				
				
		try{
			ProtocolFormXmlDataDocument document = protocolFormXmlDataDocumentDao.findById(protocolFormXmlDataDocumentId);
			document.setTitle(title);
			document = protocolFormXmlDataDocumentDao.saveOrUpdate(document);
			
			auditService.auditEvent("DOCUMENT_RENAME",
					"User: " + userId + " has renamed document: "+ document.getId());
			
			return JsonResponseHelper.newDataResponseStub(document);
		}catch(Exception ex){
			logger.error("failed rename protocolDocument: " + protocolFormXmlDataDocumentId,  ex);
			return JsonResponseHelper.newErrorResponseStub("failed to rename the document, do you have the permission to do so?");
		}
	}
	
	@RequestMapping(value = "/ajax/protocols/{protocolId}/protocol-forms/{protocolFormId}/protocol-form-xml-datas/{protocolFormXmlDataId}/documents/{protocolFormXmlDataDocumentId}/update-status", method = RequestMethod.POST, produces="application/json")
	public @ResponseBody
	JsonResponse updateProtocolFormXmlDataDocumentStatus(@PathVariable("protocolId") long protocolId,
			@PathVariable("protocolFormId") long protocolFormId,
			@PathVariable("protocolFormXmlDataDocumentId") long protocolFormXmlDataDocumentId,
			@RequestParam("userId") long userId,
			@RequestParam("status") Status status) throws Exception {
				
				
		try{
			ProtocolFormXmlDataDocument document = protocolFormXmlDataDocumentDao.findById(protocolFormXmlDataDocumentId);
			document.setStatus(status);
			document = protocolFormXmlDataDocumentDao.saveOrUpdate(document);
			
			auditService.auditEvent("DOCUMENT_STATUS_UPDATE",
					"User: " + userId + " has updated document status: "+ document.getId());
			
			return JsonResponseHelper.newDataResponseStub(document);
		}catch(Exception ex){
			logger.error("failed rename protocolDocument: " + protocolFormXmlDataDocumentId,  ex);
			return JsonResponseHelper.newErrorResponseStub("failed to change the document's status, do you have the permission to do so?");
		}
	}

	@RequestMapping(value = "/ajax/protocols/{protocolId}/protocol-forms/{protocolFormId}/protocol-form-xml-datas/{protocolFormXmlDataId}/documents/{protocolFormXmlDataDocumentId}/delete", method = RequestMethod.POST)
	public @ResponseBody 
	JsonResponse deleteProtocolFormXmlDataDocument(@PathVariable("protocolId") long protocolId,
			@PathVariable("protocolFormId") long protocolFormId,
			@PathVariable("protocolFormXmlDataDocumentId") long protocolFormXmlDataDocumentId,
			@RequestParam("userId") long userId,
			@RequestParam("committee") Committee committee){
		ProtocolFormXmlDataDocument pfdd = protocolFormXmlDataDocumentDao.findById(protocolFormXmlDataDocumentId);
		ProtocolFormStatus protocolFormStatus = protocolFormStatusDao.getLatestProtocolFormStatusByFormId(protocolFormId);
		
		User currentUser = userDao.findById(userId);
		
		boolean deletable = false;
		
		if (protocolFormStatus.getProtocolFormStatus().equals(ProtocolFormStatusEnum.DRAFT)||protocolFormStatus.getProtocolFormStatus().equals(ProtocolFormStatusEnum.UNDER_REVISION)){
			if (objectAclService.isObjectAccessible(Protocol.class, protocolId, currentUser)){
				deletable = true;
			}
		} else {
			if (currentUser.getAuthorities().contains(Permission.DELETE_DOCUMENT)){
				deletable = true;
			}
		}
		
		try{
			if (deletable){
				pfdd.setRetired(true);
				pfdd.setCreated(new Date());
				pfdd.setUser(currentUser);
				pfdd.setCommittee(committee);
				
				pfdd = protocolFormXmlDataDocumentDao.saveOrUpdate(pfdd);
				
				auditService.auditEvent("DOCUMENT_DELETE",
						"User: " + userId + " has deleted document: "+ pfdd.getId());
			} else {
				return new JsonResponse(true, "You do not have right to delete this document!", "", false, null);
			}
		} catch (Exception e){
			e.printStackTrace();
			return new JsonResponse(true, "Failed to delete this document!", "", false, null);
		}
		 
		return new JsonResponse(false);
	}
	
	@RequestMapping(value = "/ajax/protocols/{protocolId}/protocol-forms/{protocolFormId}/list-doc-types", method = RequestMethod.GET, produces="application/xml")
	public @ResponseBody 
	Source listProtocolDocumentTypes(@PathVariable("protocolId") long protocolId,
			@PathVariable("protocolFormId") long protocolFormId,
			@RequestParam("userId") long userId,
			@RequestParam(value="committee", required=false) Committee committee,
			@RequestParam(value="docAction", required=false) String docAction){
		
		return protocolFormXmlDataDocumentService.listDocumentTypes(protocolId, protocolFormId, userId, committee);
		
	}

	@Autowired(required=true)
	public void setUploadedFileDao(UploadedFileDao uploadedFileDao) {
		this.uploadedFileDao = uploadedFileDao;
	}

	public UploadedFileDao getUploadedFileDao() {
		return uploadedFileDao;
	}

	@Autowired(required=true)
	public void setProtocolFormXmlDataDocumentDao(ProtocolFormXmlDataDocumentDao protocolFormXmlDataDocumentDao) {
		this.protocolFormXmlDataDocumentDao = protocolFormXmlDataDocumentDao;
	}


	public ProtocolFormXmlDataDocumentDao getProtocolFormXmlDataDocumentDao() {
		return protocolFormXmlDataDocumentDao;
	}

	@Autowired(required=true)
	public void setProtocolFormXmlDataDao(ProtocolFormXmlDataDao protocolFormXmlDataDao) {
		this.protocolFormXmlDataDao = protocolFormXmlDataDao;
	}


	public ProtocolFormXmlDataDao getProtocolFormXmlDataDao() {
		return protocolFormXmlDataDao;
	}

	public ProtocolFormStatusDao getProtocolFormStatusDao() {
		return protocolFormStatusDao;
	}
	
	@Autowired(required=true)
	public void setProtocolFormStatusDao(ProtocolFormStatusDao protocolFormStatusDao) {
		this.protocolFormStatusDao = protocolFormStatusDao;
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

	public XmlProcessor getXmlProcessor() {
		return xmlProcessor;
	}
	
	@Autowired(required=true)
	public void setXmlProcessor(XmlProcessor xmlProcessor) {
		this.xmlProcessor = xmlProcessor;
	}

	public ProtocolStatusDao getProtocolStatusDao() {
		return protocolStatusDao;
	}
	
	@Autowired(required=true)
	public void setProtocolStatusDao(ProtocolStatusDao protocolStatusDao) {
		this.protocolStatusDao = protocolStatusDao;
	}

	public ProtocolFormDao getProtocolFormDao() {
		return protocolFormDao;
	}
	
	@Autowired(required=true)
	public void setProtocolFormDao(ProtocolFormDao protocolFormDao) {
		this.protocolFormDao = protocolFormDao;
	}

	public ProtocolFormXmlDataDocumentService getProtocolFormXmlDataDocumentService() {
		return protocolFormXmlDataDocumentService;
	}
	
	@Autowired(required=true)
	public void setProtocolFormXmlDataDocumentService(
			ProtocolFormXmlDataDocumentService protocolFormXmlDataDocumentService) {
		this.protocolFormXmlDataDocumentService = protocolFormXmlDataDocumentService;
	}

	public AuditService getAuditService() {
		return auditService;
	}
	
	@Autowired(required=true)
	public void setAuditService(AuditService auditService) {
		this.auditService = auditService;
	}
}
