package edu.uams.clara.webapp.report.service.customreport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.annotation.Resource;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;

import org.apache.commons.io.IOUtils;
import org.apache.velocity.app.VelocityEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.ui.velocity.VelocityEngineUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import edu.uams.clara.core.util.xml.XmlHandler;
import edu.uams.clara.core.util.xml.XmlHandlerFactory;
import edu.uams.clara.webapp.common.dao.email.EmailTemplateDao;
import edu.uams.clara.webapp.common.domain.email.EmailTemplate;
import edu.uams.clara.webapp.common.domain.usercontext.User;
import edu.uams.clara.webapp.common.service.EmailService;
import edu.uams.clara.webapp.common.service.form.FormService;
import edu.uams.clara.webapp.fileserver.domain.UploadedFile;
import edu.uams.clara.webapp.fileserver.service.FileGenerateAndSaveService;
import edu.uams.clara.webapp.report.dao.ReportResultDao;
import edu.uams.clara.webapp.report.domain.CommitteeActions;
import edu.uams.clara.webapp.report.domain.ReportField;
import edu.uams.clara.webapp.report.domain.ReportResult;
import edu.uams.clara.webapp.report.domain.ReportTemplate;

public abstract class CustomReportService {
	private ResourceLoader resourceLoader;
	
	private ReportResultDao reportResultDao;
	
	private FileGenerateAndSaveService fileGenerateAndSaveService;
	
	private EmailService emailService;

	private FormService formService;

	private EmailTemplateDao emailTemplateDao;

	private VelocityEngine velocityEngine;


	@Value("${application.host}")
	private String appHost;

	@Value("${reportFieldTemplateXml.url}")
	private String reportFieldTemplateXml;

	@Value("${reportSearchableFieldsTemplateXml.url}")
	private String reportXml;
	
	@Resource(name="systemConfigs") private Map<String,String> defaultValuesMap = Maps.newHashMap();
	
	private CommitteeActions committeeActions = new CommitteeActions();
	
	
	
	protected Document getReportDoc() {
		Document reportDoc = null;
		//String testPath =
				 //"file:src/test/java/edu/uams/clara/webapp/report/reports.xml";
		
		try {
			XmlHandler xmlHandler = XmlHandlerFactory.newXmlHandler();
			
			reportDoc = xmlHandler.loadXmlFileToDOM(resourceLoader.getResource(reportXml).getFile());
			
			//reportDoc = xmlHandler.loadXmlFileToDOM(resourceLoader.getResource(testPath).getFile());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return reportDoc;
	}
	
	protected Document getReportFieldDoc(ReportField rfd) {
		Document reportDoc = null;
		//String testPath =
				 //"file:src/test/java/edu/uams/clara/webapp/report/reports.xml";
		
		try {
			XmlHandler xmlHandler = XmlHandlerFactory.newXmlHandler();
			
			reportDoc = xmlHandler.parse(rfd.getField());
			
			//reportDoc = xmlHandler.loadXmlFileToDOM(resourceLoader.getResource(testPath).getFile());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return reportDoc;
	}
	
	protected Map<String, String> getFieldReportXpathMap() {
		Map<String, String> fieldReportXpathMap = Maps.newHashMap();
	
		//String testPath =
				 //"file:src/test/java/edu/uams/clara/webapp/report/fields.xml";
		
		try {
			XmlHandler xmlHandler = XmlHandlerFactory.newXmlHandler();
			
			Document fieldDoc = xmlHandler.loadXmlFileToDOM(resourceLoader.getResource(reportFieldTemplateXml).getFile());
			
			//Document fieldDoc = xmlHandler.loadXmlFileToDOM(resourceLoader.getResource(testPath).getFile());
			
			XPath xpathInstance = xmlHandler.newXPathInstance();
			
			NodeList fieldLst = (NodeList) xpathInstance
					.evaluate(
							"/fields/field",
							fieldDoc, XPathConstants.NODESET);
			
			for (int i = 0; i < fieldLst.getLength(); i++) {
				Element currentEl = (Element) fieldLst.item(i);
				
				fieldReportXpathMap.put(currentEl.getAttribute("identifier"), currentEl.getAttribute("report-xpath"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return fieldReportXpathMap;
	}
	
	protected String fillMessage(String message, Map<String, String> fieldsRealXPathMap){		
		for (Entry<String, String> fieldXpah : fieldsRealXPathMap.entrySet()) {
			message = message.replaceAll(fieldXpah.getKey().replace("{", "\\{").replace("}", "\\}"), fieldXpah.getValue());
		}
		
		return message;		
	}
	
	private List<String> parameterList = Lists.newArrayList();{
		parameterList.add("/metadata/email");
	}

	private EmailTemplate loadReportCompleteEmailTemplate(
			String identifier,
			Map<String, Object> model
			){

		EmailTemplate emailTemplate = emailTemplateDao
				.findByIdentifier(identifier);

		final String templateContent = VelocityEngineUtils
				.mergeTemplateIntoString(velocityEngine,
						emailTemplate.getVmTemplate(), model);

		emailTemplate.setTemplateContent(templateContent);

		return emailTemplate;
	}

	private void sendEmail(String desc, List<String> emails,long resultId, User currentUser) {
		Map<String,Object> emailModel = Maps.newHashMap();
		emailModel.put("reportType", desc);
		emailModel.put("reportGeneratedTime", (new Date()).toString());
		emailModel.put("reportlink", "\""+appHost+"/clara-webapp/reports/results/"+resultId+"/view\"");

		EmailTemplate emailTemplate = loadReportCompleteEmailTemplate("REPORT_COMPLETED_NOTIFICATION",emailModel);
		List<String> mailTo = Lists.newArrayList();
		List<String> ccLst = Lists.newArrayList();
		if(emails.size()>0){
		mailTo.addAll(emails);
		}
		if(!mailTo.contains(currentUser.getPerson().getEmail())){
			mailTo.add(currentUser.getPerson().getEmail());
		}

		emailService.sendEmail(emailTemplate.getTemplateContent(), mailTo, ccLst, emailTemplate.getSubject(), null);
	}

	public void uploadResultToFileServer(ReportTemplate reportTemplate) throws IOException {
		String report = this.generateReportResult(reportTemplate);
		int tryUploadTime =3;
		
		
		UploadedFile uploadedFile = null;
		while(uploadedFile == null&&tryUploadTime>0){
		uploadedFile=fileGenerateAndSaveService
				.processFileGenerateAndSave(reportTemplate,
						"result",
						IOUtils.toInputStream(report),
						"xml",
						"text/xml");
		tryUploadTime--;
		}
		ReportResult reportResult = new ReportResult();
		reportResult.setCreated(new Date());
		reportResult.setReportTemplate(reportTemplate);
		reportResult.setUploadedFile(uploadedFile);

		getReportResultDao().saveOrUpdate(reportResult);

		try {
			XmlHandler xmlHandler = XmlHandlerFactory.newXmlHandler();
			Set<String> emailPath = Sets.newHashSet();
			emailPath.add("/metadata/emails/email");
			Map<String, List<String>> emailsMap= xmlHandler.getStringValuesByXPaths(reportTemplate.getParameters(), emailPath);
			List<String> emails = emailsMap.get("/metadata/emails/email");
			this.sendEmail(reportTemplate.getDescription(), emails,reportResult.getId(),reportTemplate.getUser());
		} catch (Exception e) {

		}


	}

	public String generateReportStatement(ReportTemplate reportTemplate,List<String> resultsFiledsForDisplay) {
		String reportStatement = "";
		
		try {
			XmlHandler xmlHandler = XmlHandlerFactory.newXmlHandler();
			
			//Document reportDoc = xmlHandler.loadXmlFileToDOM(resourceLoader.getResource(reportXml).getFile());
			
			Document reportDoc = getReportDoc();
			
			String reportIdentifier = reportTemplate.getTypeDescription();
			
			XPath xpathInstance = xmlHandler.newXPathInstance();
			
			NodeList reportFieldLst = (NodeList) xpathInstance
					.evaluate(
							"/reports/report[@type='"+ reportIdentifier +"']/results/field",
							reportDoc, XPathConstants.NODESET); 
			
			int c = reportFieldLst.getLength();
			Set<String> crossApplySet = Sets.newHashSet();
			
			for (int i = 0; i < reportFieldLst.getLength(); i++) {
				
				Element currentEl = (Element) reportFieldLst.item(i);
				String fieldIdentifier = currentEl.getAttribute("identifier");
				if(!resultsFiledsForDisplay.contains(fieldIdentifier)){
					continue;
				}
				
				
				String alias = currentEl.getAttribute("alias");
				

				String xpath = this.getFieldReportXpathMap().get(fieldIdentifier);
				if(i != 0){
					reportStatement += " , "+xpath + " as " + alias;;
				}else{
				reportStatement += xpath + " as " + alias;
				}
				if(!currentEl.getAttribute("queryCrossCondition").isEmpty()){
					crossApplySet.add(currentEl.getAttribute("queryCrossCondition"));
				}
			}
			if(crossApplySet.size()>0){
				reportStatement +=" FROM protocol ";
				for(String crossApplyStatment :crossApplySet){
					reportStatement+=" "+crossApplyStatment;
				}
			}
		} catch (Exception e) {
			
		}
		
		return reportStatement;
	}
	
	public String generateRawQeury(ReportTemplate reportTemplate, Map<String, String> fieldsRealXPathMap) {
		String rawQeury = "";
		
		try {
			XmlHandler xmlHandler = XmlHandlerFactory.newXmlHandler();
			
			//Document reportDoc = xmlHandler.loadXmlFileToDOM(resourceLoader.getResource(reportXml).getFile());
			
			Document reportDoc = getReportDoc();
			
			String reportIdentifier = reportTemplate.getTypeDescription();
			
			XPath xpathInstance = xmlHandler.newXPathInstance();
			
			Set<String> mapKeyList = fieldsRealXPathMap.keySet();
			
			
			NodeList reportConditions = (NodeList) xpathInstance
					.evaluate(
							"/reports/report[@type='"+ reportIdentifier +"']/queries/conditions/condition",
							reportDoc, XPathConstants.NODESET);
			
			List<String> conditionLst = Lists.newArrayList();
			
			for (int i = 0; i < reportConditions.getLength(); i++) {
				Element currentEl = (Element) reportConditions.item(i);

				if (mapKeyList.contains(currentEl.getAttribute("involve"))) {
					conditionLst.add(currentEl.getTextContent());
				}
			}
			
			String conditions = "";
			
			int c = conditionLst.size();
			
			int i = 0;
		
			for(String condition : conditionLst){
				conditions += condition;
				
				if(i != c - 1){
					conditions += " " + reportTemplate.getGlobalOperator().toString() + " ";
				}
				
				i ++;
			}
			
			xpathInstance.reset();
			
			Element reportMainQuery = (Element) xpathInstance
					.evaluate(
							"/reports/report[@type='"+ reportIdentifier +"']/queries/query[@type=\"main\"]",
							reportDoc, XPathConstants.NODE);
			
			rawQeury = reportMainQuery.getTextContent().replace("{conditions}", conditions);			
			if(conditions.isEmpty()){
				rawQeury =rawQeury.replace("retired = 0 AND", "retired =0");
			}
			String testStudyListString ="";
			List<Long> testStudyList =committeeActions.getTestStudyOnProduction();
			for(int k=0;k<testStudyList.size();k++){
				if(k==0){
					testStudyListString +="('"+testStudyList.get(k)+"'";
				}else if(k==testStudyList.size()-1){
					testStudyListString +=",'"+testStudyList.get(k)+"')";
				}else{
					testStudyListString +=",'"+testStudyList.get(k)+"'";
				}
			}
			rawQeury += " AND id NOT IN "+testStudyListString;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return rawQeury;
		
	}
	
	public abstract String generateReportResult(ReportTemplate reportTemplate);

	public ReportResultDao getReportResultDao() {
		return reportResultDao;
	}
	
	@Autowired(required = true)
	public void setReportResultDao(ReportResultDao reportResultDao) {
		this.reportResultDao = reportResultDao;
	}

	public FileGenerateAndSaveService getFileGenerateAndSaveService() {
		return fileGenerateAndSaveService;
	}
	
	@Autowired(required = true)
	public void setFileGenerateAndSaveService(FileGenerateAndSaveService fileGenerateAndSaveService) {
		this.fileGenerateAndSaveService = fileGenerateAndSaveService;
	}
	
	public String getReportFieldTemplateXml() {
		return reportFieldTemplateXml;
	}

	public void setReportFieldTemplateXml(String reportFieldTemplateXml) {
		this.reportFieldTemplateXml = reportFieldTemplateXml;
	}

	public String getReportXml() {
		return reportXml;
	}

	public void setReportXml(String reportXml) {
		this.reportXml = reportXml;
	}

	public ResourceLoader getResourceLoader() {
		return resourceLoader;
	}
	
	@Autowired(required = true)
	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}

	public Map<String,String> getDefaultValuesMap() {
		return defaultValuesMap;
	}

	public void setDefaultValuesMap(Map<String,String> defaultValuesMap) {
		this.defaultValuesMap = defaultValuesMap;
	}

	public EmailService getEmailService() {
		return emailService;
	}
	
	@Autowired(required = true)
	public void setEmailService(EmailService emailService) {
		this.emailService = emailService;
	}

	public FormService getFormService() {
		return formService;
	}
	
	@Autowired(required = true)
	public void setFormService(FormService formService) {
		this.formService = formService;
	}

	public EmailTemplateDao getEmailTemplateDao() {
		return emailTemplateDao;
	}

	@Autowired(required=true)
	public void setEmailTemplateDao(EmailTemplateDao emailTemplateDao) {
		this.emailTemplateDao = emailTemplateDao;
	}

	public VelocityEngine getVelocityEngine() {
		return velocityEngine;
	}

	@Autowired(required=true)
	public void setVelocityEngine(VelocityEngine velocityEngine) {
		this.velocityEngine = velocityEngine;
	}

	public String getAppHost() {
		return appHost;
	}

	public void setAppHost(String appHost) {
		this.appHost = appHost;
	}

}
