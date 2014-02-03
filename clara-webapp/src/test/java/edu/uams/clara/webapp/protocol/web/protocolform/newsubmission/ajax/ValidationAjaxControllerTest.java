package edu.uams.clara.webapp.protocol.web.protocolform.newsubmission.ajax;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.xpath.XPathExpressionException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.uams.clara.webapp.common.businesslogic.form.validator.ValidationResponse;
import edu.uams.clara.webapp.common.businesslogic.form.validator.ValidationRuleContainer;
import edu.uams.clara.webapp.common.businesslogic.form.validator.ValidationRuleHandler;
import edu.uams.clara.webapp.common.businesslogic.form.validator.rule.Rule;
import edu.uams.clara.webapp.common.domain.usercontext.enums.Committee;
import edu.uams.clara.webapp.protocol.dao.etl.MiagrationDao;
import edu.uams.clara.webapp.protocol.dao.protocolform.ProtocolFormXmlDataDao;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolFormXmlData;
import edu.uams.clara.webapp.protocol.web.protocolform.newsubmission.ajax.NewSubmissionValidationAjaxController;
import edu.uams.clara.webapp.xml.processor.XmlProcessor;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"file:src/test/java/edu/uams/clara/webapp/protocol/web/protocolform/newsubmission/ajax/ValidationAjaxControllerTest-context.xml"})
public class ValidationAjaxControllerTest {

	private final static Logger logger = LoggerFactory
	.getLogger(ValidationAjaxControllerTest.class);

	//private NewSubmissionValidationAjaxController validationAjaxController;

	private ValidationRuleContainer validaitonRuleContainer;

	private ValidationRuleHandler validationRuleHandler;

	private MiagrationDao miagrationDao;

	private XmlProcessor xmlProcessor;

	private ProtocolFormXmlDataDao protocolFormXmlDataDao;

	@Test
	public void testValidateProtocol() throws JsonGenerationException, JsonMappingException, IOException{

		//List<ProtocolFormXmlData> protocolFormXmlDataLst = miagrationDao.findAllMod();

		List<Long> lst = new ArrayList<Long>();

		//for (ProtocolFormXmlData pfxd : protocolFormXmlDataLst){
			//if (pfxd.getProtocolForm().getProtocol().getId() == 13449){
				ProtocolFormXmlData pfxd = protocolFormXmlDataDao.findById(14146l);

				String xmldata = pfxd.getXmlData();

				List<ValidationResponse> validationResponses = new ArrayList<ValidationResponse>();

				if(StringUtils.hasText(xmldata)){

					List<Rule> protocolValidationRules = validaitonRuleContainer.getValidationRules("modificationValidationRules");

					Assert.notNull(protocolValidationRules);

					Set<String> valueKeys = validaitonRuleContainer.getCachedValueKeys("modificationValidationRules");

					Assert.notNull(valueKeys);


					Map<String, List<String>> values = null;


					//setup values
					try {
						values = xmlProcessor.listElementStringValuesByPaths(valueKeys, xmldata);
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

					validationResponses = validationRuleHandler.validate(protocolValidationRules, values);
				}

				if (validationResponses != null){
					lst.add(pfxd.getProtocolForm().getProtocol().getId());
					logger.debug(" response: " + validationResponses.size());
					//logger.debug(" protocolFormXmlDataId: " + pfxd.getId() + " protocolId: " + pfxd.getProtocolForm().getProtocol().getId());
				}
			//}

		}

	//@Test
	public void performanceTestValidateProtocol() throws JsonGenerationException, JsonMappingException, IOException{

		long startTime = System.currentTimeMillis();
		int n = 10;
		for(int i = 0; i < n; i ++){
			//List<ValidationResponse> validationResponses = validationAjaxController.validateProtocolNewSubmissionForm(1);
			ObjectMapper objectMapper = new ObjectMapper();
			//String w = objectMapper.writeValueAsString(validationResponses);

			//logger.debug(w);
		}

		long stopTime = System.currentTimeMillis();
		double timeSpan = (stopTime - startTime)/1000;

		logger.debug("----------------------------------------");
		logger.debug("Timespan: " +  timeSpan + "s for " + n + " times");
		logger.debug(timeSpan/n + " per run");

	}

	@Autowired(required=true)
	public void setValidaitonRuleContainer(ValidationRuleContainer validaitonRuleContainer) {
		this.validaitonRuleContainer = validaitonRuleContainer;
	}

	public ValidationRuleContainer getValidaitonRuleContainer() {
		return validaitonRuleContainer;
	}

	public MiagrationDao getMiagrationDao() {
		return miagrationDao;
	}

	@Autowired(required=true)
	public void setMiagrationDao(MiagrationDao miagrationDao) {
		this.miagrationDao = miagrationDao;
	}

	public XmlProcessor getXmlProcessor() {
		return xmlProcessor;
	}

	@Autowired(required=true)
	public void setXmlProcessor(XmlProcessor xmlProcessor) {
		this.xmlProcessor = xmlProcessor;
	}

	public ValidationRuleHandler getValidationRuleHandler() {
		return validationRuleHandler;
	}

	@Autowired(required=true)
	public void setValidationRuleHandler(ValidationRuleHandler validationRuleHandler) {
		this.validationRuleHandler = validationRuleHandler;
	}

	public ProtocolFormXmlDataDao getProtocolFormXmlDataDao() {
		return protocolFormXmlDataDao;
	}

	@Autowired(required=true)
	public void setProtocolFormXmlDataDao(ProtocolFormXmlDataDao protocolFormXmlDataDao) {
		this.protocolFormXmlDataDao = protocolFormXmlDataDao;
	}



}
