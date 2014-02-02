package edu.uams.clara.webapp.report.web.ajax;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.xml.transform.Source;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import edu.uams.clara.webapp.common.dao.usercontext.UserDao;
import edu.uams.clara.webapp.common.domain.usercontext.User;
import edu.uams.clara.webapp.common.util.XMLResponseHelper;
import edu.uams.clara.webapp.common.util.response.JsonResponse;
import edu.uams.clara.webapp.report.dao.ReportCriteriaDao;
import edu.uams.clara.webapp.report.dao.ReportFieldDao;
import edu.uams.clara.webapp.report.dao.ReportTemplateDao;
import edu.uams.clara.webapp.report.domain.ReportCriteria;
import edu.uams.clara.webapp.report.domain.ReportField;
import edu.uams.clara.webapp.report.domain.ReportFieldTemplate;
import edu.uams.clara.webapp.report.domain.ReportFieldTemplate.Operator;
import edu.uams.clara.webapp.report.domain.ReportResultFieldTemplate;
import edu.uams.clara.webapp.report.domain.ReportTemplate;
import edu.uams.clara.webapp.report.domain.ReportTemplate.GlobalOperator;
import edu.uams.clara.webapp.report.domain.ReportTemplate.ScheduleType;
import edu.uams.clara.webapp.report.domain.ReportTemplate.Status;
import edu.uams.clara.webapp.report.service.ReportCriteriaService;
import edu.uams.clara.webapp.report.service.ReportTemplateService;
import edu.uams.clara.webapp.report.service.customreport.CustomReportServiceContainer;
import edu.uams.clara.webapp.report.task.ReportTaskExecutor;

@Controller
public class ReportAjaxController {

	private final static Logger logger = LoggerFactory
			.getLogger(ReportAjaxController.class);
	
	private ReportTemplateDao reportTemplateDao;
	
	private UserDao userDao;
	
	private ReportCriteriaDao reportCriteriaDao;
	
	private ReportFieldDao reportFieldDao;
	
	private ReportCriteriaService reportCriteriaService;
	
	private ReportTemplateService reportTemplateService;
	
	private CustomReportServiceContainer customReportServiceContainer;
	
	private ThreadPoolTaskExecutor  taskExecutor;
	
	//private ThreadPoolTaskScheduler taskScheduler;
	
	//private EmailService emailService;
	
	//@Value("${application.host}")
	//private String applicationHost;
	
	
	@RequestMapping(value = "/ajax/reports/list", method = RequestMethod.GET, produces="application/json")
	public @ResponseBody JsonResponse getReportList(@RequestParam("userId") long userId){
		List<ReportTemplate> reportTemplates = null;
		
		try{
			reportTemplates = reportTemplateDao.listReportTemplatesByUser(userId);
		} catch (Exception e){
			e.printStackTrace();
			
			return new JsonResponse(true, "Failed to load report templates!", "", false);
		}
		
		return new JsonResponse(false, reportTemplates);
	}
	
	@RequestMapping(value = "/ajax/reports/{report-template-id}/set-displayfield-order", method = RequestMethod.POST, produces="application/json")
	public @ResponseBody JsonResponse changeResultFieldOrder(
			@RequestParam("displayFieldIds") List<Long> displayFieldIds,
			@PathVariable("report-template-id") long reportTemplateId){
		for(int i=0;i<displayFieldIds.size();i++){
			
			ReportField reportField = reportFieldDao.findById(displayFieldIds.get(i));
			String field = reportField.getField();
			field = field.replaceAll("order\":.*?,\"alias", "order\":\""+i+"\",\"alias");
			reportField.setField(field);
			reportFieldDao.saveOrUpdate(reportField);
		}
		
		List<ReportField> fieldList = Lists.newArrayList();
		fieldList = reportFieldDao.listAllFieldsByReportTemplateId(reportTemplateId);
		return new JsonResponse(false, fieldList);
	}
	
	@RequestMapping(value = "/ajax/reports/create", method = RequestMethod.POST, produces="application/json")
	public @ResponseBody JsonResponse addReportTemplate(
			@RequestParam("reportType") String reportType,
			@RequestParam("description") String description,
			@RequestParam("globalOperator") GlobalOperator globalOperator,
			@RequestParam(value="scheduleType", required=false) ScheduleType scheduleType){
		ReportTemplate reportTemplate = new ReportTemplate();
		ObjectMapper objectMapper = new ObjectMapper();
		
		User currentUser = (User) SecurityContextHolder.getContext().getAuthentication()
				.getPrincipal();
		
		try{
			reportTemplate.setCreated(new Date());
			reportTemplate.setDescription(description);
			reportTemplate.setParameters(null);
			reportTemplate.setRetired(false);
			reportTemplate.setStatus(Status.NOT_READY);
			reportTemplate.setTypeDescription(reportType);
			reportTemplate.setUser(currentUser);
			reportTemplate.setGlobalOperator(globalOperator);
			//reportTemplate.setCronExpression(cronExpression);
			reportTemplate.setScheduleType(scheduleType);
			
			reportTemplate = reportTemplateDao.saveOrUpdate(reportTemplate);
			List<ReportResultFieldTemplate> reportResultFieldTemplates = reportTemplateService.getDefaultReportResultFieldTemplateByFieldName(reportTemplate);
			for(ReportResultFieldTemplate reportResultFieldTemplate :reportResultFieldTemplates){
			ReportField reportField = new ReportField();
			reportField.setRetired(false);
			reportField.setReportTemplate(reportTemplate);
			
			String defaultReportField = objectMapper.writeValueAsString(reportResultFieldTemplate);
			reportField.setField(defaultReportField);
			reportFieldDao.saveOrUpdate(reportField);
			}
		} catch (Exception e){
			e.printStackTrace();
			
			return new JsonResponse(true, "Failed to create report template!", "", false);
		}
		
		return new JsonResponse(false, reportTemplate);
	}
	
	@RequestMapping(value = "/ajax/reports/{report-template-id}/remove", method = RequestMethod.GET)
	public @ResponseBody JsonResponse deleteReportTemplate(
			@PathVariable("report-template-id") long reportTemplateId){
		ReportTemplate reportTemplate = reportTemplateDao.findById(reportTemplateId);
		
		User u = (User) SecurityContextHolder.getContext().getAuthentication()
				.getPrincipal();
		
		if (reportTemplate.getUser().getId() != u.getId()){
			return new JsonResponse(true, "You cannot remove this report template!", "", false);
			//return XMLResponseHelper.getXmlResponseStub(true, "You cannot update this report template!", null);
		}
		
		try{
			reportTemplate.setRetired(Boolean.TRUE);
			
			reportTemplate = reportTemplateDao.saveOrUpdate(reportTemplate);
		} catch (Exception e){
			e.printStackTrace();
			
			return new JsonResponse(true, "Failed to delete this report template!", "", false);
			//return XMLResponseHelper.getXmlResponseStub(true, "Failed to delete this report template!", null);
		}
		
		return new JsonResponse(false);
	}
	
	@RequestMapping(value = "/ajax/reports/{report-template-id}/update", method = RequestMethod.POST, produces="application/json")
	public @ResponseBody JsonResponse updateReportTemplate(
			@PathVariable("report-template-id") long reportTemplateId,
			@RequestParam("reportType") String reportType,
			@RequestParam("description") String description,
			@RequestParam("parameters") String parameters,
			@RequestParam("globalOperator") GlobalOperator globalOperator,
			@RequestParam(value="scheduleType", required=false) ScheduleType scheduleType){
		ReportTemplate reportTemplate = reportTemplateDao.findById(reportTemplateId);
		
		User u = (User) SecurityContextHolder.getContext().getAuthentication()
				.getPrincipal();
		if (reportTemplate.getUser().getId() != u.getId()){
			return new JsonResponse(true, "You cannot update this report template!", "", false);
		}
		
		try{
			Date currentDate = new Date();
			reportTemplate.setCreated(currentDate);
			reportTemplate.setDescription(description);
			reportTemplate.setTypeDescription(reportType);
			reportTemplate.setGlobalOperator(globalOperator);
			//reportTemplate.setCronExpression(cronExpression);
			reportTemplate.setScheduleType(scheduleType);
			reportTemplate.setParameters(parameters);
			reportTemplate = reportTemplateDao.saveOrUpdate(reportTemplate);
			//String dateString = currentDate.toString();
			
			taskExecutor.execute(new ReportTaskExecutor(reportTemplate, customReportServiceContainer, reportTemplateDao));
			/*
			if (cronExpression != null && !cronExpression.isEmpty()) {
				taskScheduler.schedule(new ReportTaskExecutor(reportTemplate,customReportServiceContainer,reportTemplateDao,emailService,email,applicationHost,dateString), new CronTrigger(cronExpression));
			}
			*/
		} catch (Exception e){
			e.printStackTrace();
			
			return new JsonResponse(true, "Failed to update report template!", "", false);
		}
		
		return new JsonResponse(false, reportTemplate);
	}
	
	@RequestMapping(value = "/ajax/reports/{report-template-id}/add-displayfield", method = RequestMethod.POST, produces="application/json")
	public @ResponseBody JsonResponse addFieldToReportTemplate(
			@PathVariable("report-template-id") long reportTemplateId,
			@RequestParam("fieldname") String fieldName){
		ReportTemplate reportTemplate = reportTemplateDao.findById(reportTemplateId);
		
		ReportResultFieldTemplate reportResultFieldTemplate = reportTemplateService.getReportResultFieldTemplateByFieldName(fieldName,reportTemplate);
		ReportField  reportField = new ReportField();
		
		ObjectMapper objectMapper = new ObjectMapper();
		
		try {
			String field = objectMapper.writeValueAsString(reportResultFieldTemplate);
			
			reportField.setField(field);
			reportField.setReportTemplate(reportTemplate);
			reportField.setRetired(Boolean.FALSE);
			
			reportField = reportFieldDao.saveOrUpdate(reportField);
		} catch (Exception e) {
			e.printStackTrace();
			
			return new JsonResponse(true, "Failed to add new criteria!", "", false);
		}
		
		return new JsonResponse(false, reportField);
	}
	
	
	@RequestMapping(value = "/ajax/reports/{report-template-id}/add-criteria", method = RequestMethod.POST, produces="application/json")
	public @ResponseBody JsonResponse addCriteriaToReportTemplate(
			@PathVariable("report-template-id") long reportTemplateId,
			@RequestParam("fieldname") String fieldName,
			@RequestParam("operator") Operator operator,
			@RequestParam("fieldvalue") String fieldValue,
			@RequestParam("displayvalue") String displayValue){
		ReportTemplate reportTemplate = reportTemplateDao.findById(reportTemplateId);
		
		ReportFieldTemplate reportFieldTemplate = reportCriteriaService.getReportFieldTemplate(fieldName, operator, fieldValue, displayValue);
		
		ReportCriteria reportCriteria = new ReportCriteria();
		
		ObjectMapper objectMapper = new ObjectMapper();
		
		try {
			String criteria = objectMapper.writeValueAsString(reportFieldTemplate);
			
			reportCriteria.setCriteria(criteria);
			reportCriteria.setReportTemplate(reportTemplate);
			reportCriteria.setRetired(Boolean.FALSE);
			
			reportCriteria = reportCriteriaDao.saveOrUpdate(reportCriteria);
		} catch (Exception e) {
			e.printStackTrace();
			
			return new JsonResponse(true, "Failed to add new criteria!", "", false);
		}
		
		return new JsonResponse(false, reportCriteria);
	}
	
	@RequestMapping(value = "/ajax/reports/{report-template-id}/list-criteria", method = RequestMethod.GET, produces="application/json")
	public @ResponseBody JsonResponse listCriteriaOfReportTemplate(
			@PathVariable("report-template-id") long reportTemplateId){
		List<ReportCriteria> criteriaLst = Lists.newArrayList();
		criteriaLst = reportCriteriaDao.listAllCriteriasByReportTemplateId(reportTemplateId);
		return new JsonResponse(false, criteriaLst);
	}
	
	@RequestMapping(value = "/ajax/reports/{report-template-id}/list-displayfields", method = RequestMethod.GET, produces="application/json")
	public @ResponseBody JsonResponse listdisplayfieldsOfReportTemplate(
			@PathVariable("report-template-id") long reportTemplateId){
		List<ReportField> fieldList = Lists.newArrayList();
		List<ReportField> sortedFieldList = Lists.newArrayList();
		fieldList = reportFieldDao.listAllFieldsByReportTemplateId(reportTemplateId);
			for(int i =0;i<fieldList.size();i++){
				for(ReportField field: fieldList){
				if(field.getField().contains("\""+i+"\"")){
					sortedFieldList.add(field);
					if(i==0){
						continue;
					}
					break;
				}
				
			}
		}
		
		return new JsonResponse(false, sortedFieldList);
	}
			
	
	
	@RequestMapping(value = "/ajax/reports/{report-template-id}/remove-displayfield", method = RequestMethod.GET)
	public @ResponseBody Source removeDisplayfieldOfReportTemplate(
			@PathVariable("report-template-id") long reportTemplateId,
			@RequestParam("reportFieldId") long reportFieldId){	
		
		try {
			ReportField reportField = reportFieldDao.findById(reportFieldId);
			reportField.setRetired(Boolean.TRUE);
			
			reportFieldDao.saveOrUpdate(reportField);
		} catch (Exception e){
			e.printStackTrace();
			
			return XMLResponseHelper.newErrorResponseStub("Failed to remove criteria!");
		}
		
		return XMLResponseHelper.newSuccessResponseStube(Boolean.TRUE.toString());
	}
	
	@RequestMapping(value = "/ajax/reports/{report-template-id}/remove-criteria", method = RequestMethod.GET)
	public @ResponseBody Source removeCriteriaOfReportTemplate(
			@PathVariable("report-template-id") long reportTemplateId,
			@RequestParam("criteriaId") long criteriaId){	
		
		try {
			ReportCriteria criteria = reportCriteriaDao.findById(criteriaId);
			criteria.setRetired(Boolean.TRUE);
			
			reportCriteriaDao.saveOrUpdate(criteria);
		} catch (Exception e){
			e.printStackTrace();
			
			return XMLResponseHelper.newErrorResponseStub("Failed to remove criteria!");
		}
		
		return XMLResponseHelper.newSuccessResponseStube(Boolean.TRUE.toString());
	}
	
	@RequestMapping(value = "/ajax/reports/{report-template-id}/list-available-criteria", method = RequestMethod.GET, produces="application/json")
	public @ResponseBody JsonResponse listAvailableCriteria(
			@PathVariable("report-template-id") long reportTemplateId){		
		ReportTemplate reportTemplate = reportTemplateDao.findById(reportTemplateId);
		
		List<ReportFieldTemplate> availableCiterias = reportTemplateService.listAvailableCriterias(reportTemplate);
		
		return new JsonResponse(false, availableCiterias);
	}
	
	@RequestMapping(value = "/ajax/reports/{report-template-id}/list-available-displayfields", method = RequestMethod.GET, produces="application/json")
	public @ResponseBody JsonResponse listAvailableFields(
			@PathVariable("report-template-id") long reportTemplateId){		
		ReportTemplate reportTemplate = reportTemplateDao.findById(reportTemplateId);
		List<ReportResultFieldTemplate> availableFields = reportTemplateService.listAvailableFields(reportTemplate);
		
		return new JsonResponse(false, availableFields);
	}

	public ReportTemplateDao getReportTemplateDao() {
		return reportTemplateDao;
	}
	
	@Autowired(required=true)
	public void setReportTemplateDao(ReportTemplateDao reportTemplateDao) {
		this.reportTemplateDao = reportTemplateDao;
	}

	public UserDao getUserDao() {
		return userDao;
	}
	
	@Autowired(required=true)
	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}

	public ReportCriteriaService getReportCriteriaService() {
		return reportCriteriaService;
	}
	
	@Autowired(required=true)
	public void setReportCriteriaService(ReportCriteriaService reportCriteriaService) {
		this.reportCriteriaService = reportCriteriaService;
	}

	public ReportCriteriaDao getReportCriteriaDao() {
		return reportCriteriaDao;
	}
	
	@Autowired(required=true)
	public void setReportCriteriaDao(ReportCriteriaDao reportCriteriaDao) {
		this.reportCriteriaDao = reportCriteriaDao;
	}

	public ReportTemplateService getReportTemplateService() {
		return reportTemplateService;
	}
	
	@Autowired(required=true)
	public void setReportTemplateService(ReportTemplateService reportTemplateService) {
		this.reportTemplateService = reportTemplateService;
	}

	public CustomReportServiceContainer getCustomReportServiceContainer() {
		return customReportServiceContainer;
	}
	
	@Autowired(required=true)
	public void setCustomReportServiceContainer(
			CustomReportServiceContainer customReportServiceContainer) {
		this.customReportServiceContainer = customReportServiceContainer;
	}

	public ReportFieldDao getReportFieldDao() {
		return reportFieldDao;
	}

	@Autowired(required=true)
	public void setReportFieldDao(ReportFieldDao reportFieldDao) {
		this.reportFieldDao = reportFieldDao;
	}

	public ThreadPoolTaskExecutor getTaskExecutor() {
		return taskExecutor;
	}

	@Autowired(required=true)
	public void setTaskExecutor(ThreadPoolTaskExecutor taskExecutor) {
		this.taskExecutor = taskExecutor;
	}
	
	/*
	public ThreadPoolTaskScheduler getTaskScheduler() {
		return taskScheduler;
	}

	@Autowired(required=true)
	public void setTaskScheduler(ThreadPoolTaskScheduler taskScheduler) {
		this.taskScheduler = taskScheduler;
	}
	*/
	 /*
	public EmailService getEmailService() {
		return emailService;
	}

	@Autowired(required=true)
	public void setEmailService(EmailService emailService) {
		this.emailService = emailService;
	}
	*/
}
