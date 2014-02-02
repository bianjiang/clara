package edu.uams.clara.webapp.protocol.web.protocolform.budget.ajax;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.xml.transform.Source;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import edu.uams.clara.core.util.xml.DomUtils;
import edu.uams.clara.webapp.common.businesslogic.BusinessObjectStatusHelperContainer;
import edu.uams.clara.webapp.common.dao.usercontext.UserDao;
import edu.uams.clara.webapp.common.domain.history.Track;
import edu.uams.clara.webapp.common.domain.usercontext.User;
import edu.uams.clara.webapp.common.domain.usercontext.enums.Committee;
import edu.uams.clara.webapp.common.security.MutexLockService;
import edu.uams.clara.webapp.common.service.audit.AuditService;
import edu.uams.clara.webapp.common.util.DateFormatUtil;
import edu.uams.clara.webapp.common.util.JsonResponseHelper;
import edu.uams.clara.webapp.common.util.XMLResponseHelper;
import edu.uams.clara.webapp.common.util.response.JsonResponse;
import edu.uams.clara.webapp.fileserver.domain.UploadedFile;
import edu.uams.clara.webapp.fileserver.service.FileGenerateAndSaveService;
import edu.uams.clara.webapp.protocol.dao.protocolform.ProtocolFormDao;
import edu.uams.clara.webapp.protocol.dao.protocolform.ProtocolFormXmlDataDao;
import edu.uams.clara.webapp.protocol.dao.protocolform.ProtocolFormXmlDataDocumentDao;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolForm;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolFormXmlData;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolFormXmlDataDocument;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolFormXmlDataDocument.Status;
import edu.uams.clara.webapp.protocol.domain.protocolform.enums.ProtocolFormXmlDataType;
import edu.uams.clara.webapp.protocol.service.ProtocolFormService;
import edu.uams.clara.webapp.protocol.service.history.ProtocolTrackService;
import edu.uams.clara.webapp.xml.processor.BudgetXmlDifferService;
import edu.uams.clara.webapp.xml.processor.BudgetXmlExportService;
import edu.uams.clara.webapp.xml.processor.XmlProcessor;

@Controller
public class BudgetAjaxController {

	private final static Logger logger = LoggerFactory
			.getLogger(BudgetAjaxController.class);

	private ProtocolFormDao protocolFormDao;

	private ProtocolFormXmlDataDao protocolFormXmlDataDao;
	
	private UserDao userDao;

	private ProtocolFormService protocolFormService;

	private BudgetXmlDifferService budgetXmlDifferService;

	private BudgetXmlExportService budgetExportService;
	
	private XmlProcessor xmlProcessor;
	
	private BusinessObjectStatusHelperContainer businessObjectStatusHelperContainer;
	
	private ProtocolTrackService protocolTrackService;
	
	private AuditService auditService;
	
	private MutexLockService mutexLockService;

	/**
	 * @ToDo security, and handle result not found exception...
	 * @param formXmlDataId
	 * @return
	 */
	@RequestMapping(value = "/ajax/protocols/{protocolId}/protocol-forms/{protocolFormId}/budgets/get", method = RequestMethod.GET, produces="application/xml")
	public @ResponseBody
	Source getBudgetXmlData(@PathVariable("protocolFormId") long protocolFormId) {

		ProtocolForm protocolForm = protocolFormDao.findById(protocolFormId);
		String result = "";
		try{
			result = protocolForm.getTypedProtocolFormXmlDatas()
			.get(ProtocolFormXmlDataType.BUDGET).getXmlData();
			
			return DomUtils.toSource(XMLResponseHelper.xmlResult(result));
		} catch (Exception e){
			e.printStackTrace();
			
			return XMLResponseHelper.newErrorResponseStub("Failed to get Budget data!");
		}

	}
	
	@RequestMapping(value = "/ajax/protocols/{protocolId}/protocol-forms/{protocolFormId}/budgets/delete", method = RequestMethod.GET, produces="application/json")
	public @ResponseBody
	JsonResponse deleteBudget(@PathVariable("protocolId") long protocolId,
			@PathVariable("protocolFormId") long protocolFormId) {
		User u = (User) SecurityContextHolder.getContext().getAuthentication()
				.getPrincipal();
		
		ProtocolForm protocolForm = protocolFormDao.findById(protocolFormId);
		
		try{
			ProtocolFormXmlData budgetXmlData = protocolForm.getTypedProtocolFormXmlDatas().get(ProtocolFormXmlDataType.BUDGET);
			if (budgetXmlData != null){
				budgetXmlData.setRetired(true);
				protocolFormXmlDataDao.saveOrUpdate(budgetXmlData);
			}
			
			Track track = protocolTrackService.getOrCreateTrack("PROTOCOL",
					protocolId);

			Document logsDoc = protocolTrackService.getLogsDocument(track);

			Element logEl = logsDoc.createElement("log");
			
			Date now = new Date();
			
			String logId = UUID.randomUUID().toString();
			
			logEl.setAttribute("id", logId);
			logEl.setAttribute("parent-id", logId);
			logEl.setAttribute("action-user-id", String.valueOf(u.getId()));
			logEl.setAttribute("actor", u.getPerson().getFullname());
			logEl.setAttribute("date-time", DateFormatUtil.formateDate(now));
			logEl.setAttribute("event-type", "DELETE_BUDGET");
			logEl.setAttribute("form-id", String.valueOf(protocolFormId));
			logEl.setAttribute("parent-form-id", String.valueOf(protocolForm.getParent().getId()));
			logEl.setAttribute("form-type", protocolForm.getFormType());
			logEl.setAttribute("log-type", "ACTION");
			logEl.setAttribute("timestamp", String.valueOf(now.getTime()));

			String message = "Budget has been deleted by "
					+ u.getPerson().getFullname() + "";

			logEl.setTextContent(message);

			logsDoc.getDocumentElement().appendChild(logEl);

			track = protocolTrackService.updateTrack(track, logsDoc);
		} catch (Exception e){
			e.printStackTrace();
			
			return JsonResponseHelper.newErrorResponseStub("Failed to delete budget!");
		}
	
		return JsonResponseHelper.newSuccessResponseStube("Successfully deleted!");

	}

	@RequestMapping(value = "/ajax/protocols/{protocolId}/protocol-forms/{protocolFormId}/budgets/save", method = RequestMethod.POST, produces="application/xml")
	public @ResponseBody
	Source saveBudgetXmlData(HttpServletRequest request,
			@PathVariable("protocolId") long protocolId,
			@PathVariable("protocolFormId") long protocolFormId,
			@RequestParam("xmlData") String xmlData,
			@RequestParam(value = "reason", required = false) String reason) {
		User u = (User) SecurityContextHolder.getContext().getAuthentication()
				.getPrincipal();
		
		if (xmlData == null || xmlData.isEmpty()){
			logger.error("nothing posted from client for saving a budget??? protocolFormId: " + protocolFormId);
			return XMLResponseHelper.newErrorResponseStub("nothing posted from client???");
		}
		
		if (mutexLockService.isLockedByObjectClassAndIdForCurrentUser(
				ProtocolForm.class, protocolId, u)) {
			return XMLResponseHelper.newErrorResponseStub("Budget is being edited by "+ u.getPerson().getFullname() +"!"); 
		}
		
		try {
			ProtocolForm protocolForm = protocolFormDao
					.findById(protocolFormId);
			ProtocolFormXmlData budgetXmlData = protocolForm
					.getTypedProtocolFormXmlDatas().get(
							ProtocolFormXmlDataType.BUDGET);

			budgetXmlData.setXmlData(xmlData);

			if (budgetXmlData.getParent() == null) {
				budgetXmlData.setParent(budgetXmlData);
			}

			budgetXmlData = protocolFormXmlDataDao.saveOrUpdate(budgetXmlData);	
			
			String changeReason = (reason!=null)?reason:"";
			
			auditService.auditEvent(AuditService.AuditEvent.SAVE_BUDGET.toString(), "ProtocolId: " + protocolId + " UserID: " + u.getId() + " UserIP: " + request.getRemoteAddr() + " Reason: " + changeReason, xmlData);
			
			return XMLResponseHelper.newSuccessResponseStube(Boolean.TRUE.toString());
		} catch (Exception ex) {
			logger.error("error when saving the budget!!! for protocolformId: " + protocolFormId, ex);

			return XMLResponseHelper.newErrorResponseStub("error when saving the budget!");
		}

	}
	

	@RequestMapping(value = "/ajax/protocols/{protocolId}/protocol-forms/{protocolFormId}/budgets/get-external-expenses", method = RequestMethod.GET)
	public @ResponseBody
	String getExternalExpenses(
			@PathVariable("protocolFormId") long protocolFormId) {
		String finalXml = null;

		try {
			finalXml = protocolFormService
					.generateExternalExpenses(protocolFormId);
			
			return XMLResponseHelper.xmlResult(finalXml);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Failed to get external expenses!", e);
			//return XMLResponseHelper.getXmlResponseStub(true, "Failed to get external expenses!", null);
			//return XMLResponseHelper.getXmlResponseStub(true, "Failed to get external expenses!", null);
			return "<expenses></expenses>";
		}

		//return finalXml;

	}

	@RequestMapping(value = "/ajax/protocols/{protocolId}/protocol-forms/{protocolFormId}/budgets/list-versions", method = RequestMethod.GET, produces="application/xml")
	public @ResponseBody
	Source listBudgetVersions(
			@PathVariable("protocolFormId") long protocolFormId) {
		try{
			String finalXml = "<list>";

			List<ProtocolFormXmlData> budgetXmlDataLst = protocolFormXmlDataDao
					.listProtocolformXmlDatasByFormIdAndType(protocolFormId,
							ProtocolFormXmlDataType.BUDGET);

			for (ProtocolFormXmlData pfxd : budgetXmlDataLst) {
				finalXml += "<budget id='"
						+ pfxd.getId()
						+ "' created='"
						+ pfxd.getCreated()
						+ "' type='"
						+ pfxd.getProtocolForm().getProtocolFormType()
								.getDescription() + "'/>";
			}
			
			return DomUtils.toSource(XMLResponseHelper.xmlResult(finalXml + "</list>"));
		} catch (Exception e){
			e.printStackTrace();
			
			return XMLResponseHelper.newErrorResponseStub("Failed to list versions!");
		}
		

		//return finalXml + "</list>";

	}

	@RequestMapping(value = "/ajax/protocols/{protocolId}/protocol-forms/{protocolFormId}/budgets/compare-to/{olderVersionId}", method = RequestMethod.GET, produces="application/xml")
	public @ResponseBody
	Source compareBudgetXml(
			@PathVariable("protocolFormId") long protocolFormId,
			@PathVariable("olderVersionId") long olderVersionId) {
		String finalXml = "";

		ProtocolFormXmlData currentBudgetXmlData = protocolFormXmlDataDao
				.getLastProtocolFormXmlDataByProtocolFormIdAndType(
						protocolFormId, ProtocolFormXmlDataType.BUDGET);

		ProtocolFormXmlData compareToBudgetXmlData = protocolFormXmlDataDao
				.getLastProtocolFormXmlDataByProtocolFormXmlDataIdAndType(
						olderVersionId, ProtocolFormXmlDataType.BUDGET);

		String currentBudgetXml = currentBudgetXmlData.getXmlData();
		String compareToBudgetXml = compareToBudgetXmlData.getXmlData();

		try {
			finalXml = budgetXmlDifferService.differBudgetXml(
					compareToBudgetXml, currentBudgetXml);
			
			return DomUtils.toSource(XMLResponseHelper.xmlResult(finalXml));
		} catch (Exception e) {
			e.printStackTrace();
			
			return XMLResponseHelper.newErrorResponseStub("Failed to compare versions!");
		}

		//return finalXml;

	}

	private ProtocolFormXmlDataDocumentDao protocolFormXmlDataDocumentDao;

	private FileGenerateAndSaveService fileGenerateAndSaveService;
	
	@RequestMapping(value = "/ajax/protocols/{protocolId}/protocol-forms/{protocolFormId}/budgets/export-documents", method = RequestMethod.POST)
	public @ResponseBody
	JsonResponse generateAndSaveBudgetDocuments(
			@PathVariable("protocolId") long protocolId,
			@PathVariable("protocolFormId") long protocolFormId,
			@RequestParam("committee") Committee committee)
			//@PathVariable("outputType") String outputType,
			//@RequestParam(value = "filename", required = true) String filename,
			//@RequestParam(value = "category", required = true) String category)
			throws IOException {

		ProtocolForm protocolForm = protocolFormDao.findById(protocolFormId);
		ProtocolFormXmlData budgetXmlData = protocolForm
				.getTypedProtocolFormXmlDatas().get(
						ProtocolFormXmlDataType.BUDGET);

		String budgetXml = budgetXmlData.getXmlData();
		
		ByteArrayOutputStream outputStream = null;
		
		List<ProtocolFormXmlDataDocument> budgetDocuments = new ArrayList<ProtocolFormXmlDataDocument>();
		
		for(BudgetXmlExportService.BudgetDocumentType budgetDocumentType:BudgetXmlExportService.BudgetDocumentType.values()){
			
			if(!budgetDocumentType.isActive()){
				continue;
			}
			outputStream = budgetExportService.generateBudgetExcelDocument(budgetXml, budgetDocumentType, protocolFormId);
			
			// modelMap.put("xmlData", xmlData);
			UploadedFile uploadedFile = fileGenerateAndSaveService
					.processFileGenerateAndSave(protocolForm.getProtocol(), budgetDocumentType.getFileName(), new ByteArrayInputStream(outputStream.toByteArray()), "xls",
							"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

			ProtocolFormXmlDataDocument budgetDocument = new ProtocolFormXmlDataDocument();

			ProtocolFormXmlDataDocument parentBudgetDocument = null;

			//List<ProtocolFormXmlDataDocumentWrapper> protocolDocumentLst = protocolFormXmlDataDocumentDao
			//		.listDocumentsByProtocolFormIdAndCategory(protocolFormId,
			//				budgetDocumentType.getCategory());
			
			ProtocolFormXmlDataDocument latestBudgetDocument = null;
			
			try{
				latestBudgetDocument = protocolFormXmlDataDocumentDao.getLatestDocumentByProtocolFormIdAndCategory(protocolFormId, budgetDocumentType.getCategory());
			} catch (Exception e){
				logger.debug("no previously generated budget document, don't care...");
				//e.printStackTrace();
			}
			
			
			long versionId = 0;
			//String desc = " Original";
			//logger.debug("protocol budget list: " + protocolDocumentLst);
			if (latestBudgetDocument != null){
				parentBudgetDocument = latestBudgetDocument;
				versionId = latestBudgetDocument.getVersionId() + 1;
			} else {
				parentBudgetDocument = budgetDocument;
			}
			
			/*if (protocolDocumentLst.size() == 0) {
				parentBudgetDocument = budgetDocument;
			} else {
				parentBudgetDocument = protocolFormXmlDataDocumentDao.findById(protocolDocumentLst.get(0).);
				versionId = protocolDocumentLst.get(0).getVersionId() + 1;
				//desc = " Revision " + String.valueOf(versionId);
			}*/

			budgetDocument.setUploadedFile(uploadedFile);
			budgetDocument.setTitle(uploadedFile.getFilename());
			budgetDocument.setCategory(budgetDocumentType.getCategory());
			budgetDocument.setCommittee(committee); // @TODO to be updated, need to
													// find out which committee will
													// do this...
			budgetDocument.setStatus(Status.DRAFT);
			budgetDocument.setCreated(new Date());
			budgetDocument.setParent((parentBudgetDocument == budgetDocument)?parentBudgetDocument:parentBudgetDocument.getParent());

			budgetDocument.setProtocolFormXmlData(budgetXmlData);
			logger.debug("versionId: " + versionId);
			budgetDocument.setVersionId(versionId);

			User u = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			//u.setId(1l); // need to pass userID from the client.
			budgetDocument.setUser(u);

			budgetDocument = protocolFormXmlDataDocumentDao
					.saveOrUpdate(budgetDocument);
			budgetDocuments.add(budgetDocument);
		}

		return new JsonResponse(false, "successfull!", null, false, budgetDocuments);
	}
	
	@RequestMapping(value = "/ajax/budget-request-code", method = RequestMethod.POST)
	public @ResponseBody String requestCode(@RequestParam("cpt") String cpt,
			//@RequestParam("cdm") String cdm,
			@RequestParam("description") String description,
			//@RequestParam("cost") String cost,
			//@RequestParam("offer") String offer,
			//@RequestParam("price") String price,
			@RequestParam("note") String note,
			@RequestParam("userId") long userId,
			@RequestParam("protocolId") long protocolId,
			@RequestParam("formId") long protocolFormId,
			@RequestParam("budgetId") long budgetId,
			@RequestParam("committee") Committee committee){
		try{
			ProtocolForm protocolForm = protocolFormDao.findById(protocolFormId);
			User user = userDao.findById(userId);
			
			String xml = "<committee-review><committee type=\""+ committee.toString() +"\">";
			//xml += "<request-code cpt=\""+ cpt +"\" cdm=\""+ cdm +"\" description=\""+ description +"\" cost=\""+ cost +"\" offer=\""+ offer +"\" price=\""+ price +"\" note=\""+ note +"\"></request-code>";
			xml += "<request-code cpt=\""+ cpt +"\" description=\""+ description +"\" note=\""+ note +"\"></request-code>";
			xml += "</committee></committee-review>";
			
			businessObjectStatusHelperContainer
			.getBusinessObjectStatusHelper(protocolForm.getProtocolFormType().toString())
			.triggerAction(
					protocolForm,
					committee,
					user,
					"REQUEST_CPT_CODE",
					"", xml);
			
		} catch (Exception e){
			e.printStackTrace();
			
			return XMLResponseHelper.xmlResult(Boolean.FALSE); 
		}
		
		return XMLResponseHelper.xmlResult(Boolean.TRUE);
	}


	@Autowired(required = true)
	public void setProtocolFormDao(ProtocolFormDao protocolFormDao) {
		this.protocolFormDao = protocolFormDao;
	}

	public ProtocolFormDao getProtocolFormDao() {
		return protocolFormDao;
	}

	@Autowired(required = true)
	public void setProtocolFormXmlDataDao(
			ProtocolFormXmlDataDao protocolFormXmlDataDao) {
		this.protocolFormXmlDataDao = protocolFormXmlDataDao;
	}

	public ProtocolFormXmlDataDao getProtocolFormXmlDataDao() {
		return protocolFormXmlDataDao;
	}

	public FileGenerateAndSaveService getFileGenerateAndSaveService() {
		return fileGenerateAndSaveService;
	}

	@Autowired(required = true)
	public void setFileGenerateAndSaveService(
			FileGenerateAndSaveService fileGenerateAndSaveService) {
		this.fileGenerateAndSaveService = fileGenerateAndSaveService;
	}

	public ProtocolFormXmlDataDocumentDao getProtocolFormXmlDataDocumentDao() {
		return protocolFormXmlDataDocumentDao;
	}

	@Autowired(required = true)
	public void setProtocolFormXmlDataDocumentDao(
			ProtocolFormXmlDataDocumentDao protocolFormXmlDataDocumentDao) {
		this.protocolFormXmlDataDocumentDao = protocolFormXmlDataDocumentDao;
	}

	public BudgetXmlDifferService getBudgetXmlDifferService() {
		return budgetXmlDifferService;
	}

	@Autowired(required = true)
	public void setBudgetXmlDifferService(
			BudgetXmlDifferService budgetXmlDifferService) {
		this.budgetXmlDifferService = budgetXmlDifferService;
	}

	public BudgetXmlExportService getBudgetExportService() {
		return budgetExportService;
	}

	@Autowired(required = true)
	public void setBudgetExportService(BudgetXmlExportService budgetExportService) {
		this.budgetExportService = budgetExportService;
	}


	public ProtocolFormService getProtocolFormService() {
		return protocolFormService;
	}

	@Autowired(required = true)
	public void setProtocolFormService(ProtocolFormService protocolFormService) {
		this.protocolFormService = protocolFormService;
	}

	public XmlProcessor getXmlProcessor() {
		return xmlProcessor;
	}
	
	@Autowired(required = true)
	public void setXmlProcessor(XmlProcessor xmlProcessor) {
		this.xmlProcessor = xmlProcessor;
	}

	public BusinessObjectStatusHelperContainer getBusinessObjectStatusHelperContainer() {
		return businessObjectStatusHelperContainer;
	}
	
	@Autowired(required = true)
	public void setBusinessObjectStatusHelperContainer(
			BusinessObjectStatusHelperContainer businessObjectStatusHelperContainer) {
		this.businessObjectStatusHelperContainer = businessObjectStatusHelperContainer;
	}

	public UserDao getUserDao() {
		return userDao;
	}
	
	@Autowired(required = true)
	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}

	public ProtocolTrackService getProtocolTrackService() {
		return protocolTrackService;
	}
	
	@Autowired(required = true)
	public void setProtocolTrackService(ProtocolTrackService protocolTrackService) {
		this.protocolTrackService = protocolTrackService;
	}

	public AuditService getAuditService() {
		return auditService;
	}
	
	@Autowired(required = true)
	public void setAuditService(AuditService auditService) {
		this.auditService = auditService;
	}

	public MutexLockService getMutexLockService() {
		return mutexLockService;
	}
	
	@Autowired(required = true)
	public void setMutexLockService(MutexLockService mutexLockService) {
		this.mutexLockService = mutexLockService;
	}
}
