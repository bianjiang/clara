package edu.uams.clara.webapp.contract;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


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

import edu.uams.clara.core.util.xml.DomUtils;
import edu.uams.clara.core.util.xml.DomUtils.Encoding;
import edu.uams.clara.webapp.common.util.DateFormatUtil;
import edu.uams.clara.webapp.contract.dao.contractform.ContractFormDao;
import edu.uams.clara.webapp.contract.domain.contractform.ContractForm;
import edu.uams.clara.webapp.xml.processor.XmlProcessor;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "file:src/test/java/edu/uams/clara/webapp/contract/ContractDataXmlUpdateTest-context.xml" })
public class ContractDataXmlUpdateTest {

	private final static Logger logger = LoggerFactory
			.getLogger(ContractDataXmlUpdateTest.class);

	private ContractFormDao contractFormDao;
	private XmlProcessor xmlProcessor;
	private DateFormatUtil dateFormatUtil;

	@Test
	public void UpdateDataXml() {

		List<ContractForm> ContractFormList = new ArrayList<ContractForm>();
		ContractFormList = contractFormDao.findAll();
		String xmlData = null;
		String type = null;
		Date createdDate = new Date();

		ContractForm contractForm = new ContractForm();

		Document Budget = null;

		for (int ContractNUm = 0; ContractNUm < ContractFormList.size(); ContractNUm++) {
			contractForm = ContractFormList.get(ContractNUm);
			xmlData = contractForm.getMetaDataXml();
			type = contractForm.getContractFormType().getDescription();
			createdDate = contractForm.getCreated();

			try {
				Budget = xmlProcessor.loadXmlStringToDOM(xmlData);
			} catch (Exception e) {
				e.printStackTrace();
			}
			NodeList contractNodeList = Budget.getElementsByTagName("contract");

			if (contractNodeList.getLength() == 0)
				continue;

			Element contract = (Element) contractNodeList.item(0);

			contract.setAttribute("type", type);
			contract.setAttribute("timestamp", "" + System.currentTimeMillis());
			contract.setAttribute("created",
					dateFormatUtil.formateDateToMDY(createdDate));

			xmlData = DomUtils.elementToString(Budget, false, Encoding.UTF16);

			contractForm.setMetaDataXml(xmlData);

			contractFormDao.saveOrUpdate(contractForm);

		}

	}

	public ContractFormDao getContractFormDao() {
		return contractFormDao;
	}

	@Autowired(required = true)
	public void setContractFormDao(ContractFormDao contractFormDao) {
		this.contractFormDao = contractFormDao;
	}

	public XmlProcessor getXmlProcessor() {
		return xmlProcessor;
	}

	@Autowired(required = true)
	public void setXmlProcessor(XmlProcessor xmlProcessor) {
		this.xmlProcessor = xmlProcessor;
	}

	public DateFormatUtil getDateFormatUtil() {
		return dateFormatUtil;
	}

	@Autowired(required = true)
	public void setDateFormatUtil(DateFormatUtil dateFormatUtil) {
		this.dateFormatUtil = dateFormatUtil;
	}

}
