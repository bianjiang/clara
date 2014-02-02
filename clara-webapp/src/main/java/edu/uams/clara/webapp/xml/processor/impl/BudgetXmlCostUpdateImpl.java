package edu.uams.clara.webapp.xml.processor.impl;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.uams.clara.core.util.xml.DomUtils;
import edu.uams.clara.core.util.xml.DomUtils.Encoding;
import edu.uams.clara.webapp.protocol.dao.budget.code.HospitalChargeProcedureDao;
import edu.uams.clara.webapp.protocol.dao.budget.code.PhysicianChargeProcedureDao;
import edu.uams.clara.webapp.protocol.domain.budget.code.HospitalChargeProcedure;
import edu.uams.clara.webapp.protocol.domain.budget.code.PhysicianChargeProcedure;
import edu.uams.clara.webapp.xml.processor.BudgetXmlCostUpdate;
import edu.uams.clara.webapp.xml.processor.XmlProcessor;

public class BudgetXmlCostUpdateImpl implements BudgetXmlCostUpdate {
	private final static Logger logger = LoggerFactory
			.getLogger(BudgetXmlCostUpdateImpl.class);
	private XmlProcessor xmlProcessor;
	private HospitalChargeProcedureDao hospitalChargeProcedureDao;
	private PhysicianChargeProcedureDao physicianChargeProcedureDao;

	public String updateCost(String BudgetXml) {
		Document Budget = null;

		try {
			Budget = xmlProcessor.loadXmlStringToDOM(BudgetXml);
		} catch (Exception e) {
			e.printStackTrace();
		}

		NodeList procedureNodeList = Budget.getElementsByTagName("procedure");

		for (int proIndex = 0; proIndex < procedureNodeList.getLength(); proIndex++) {
			Element prodedure = (Element) procedureNodeList.item(proIndex);
			NodeList hospNodeList = prodedure.getElementsByTagName("hosp");
			NodeList physNodeList = prodedure.getElementsByTagName("phys");
			NodeList costNodeList = prodedure.getElementsByTagName("cost");
			NodeList costChileList = costNodeList.item(0).getChildNodes();

			Element hosp = (Element) hospNodeList.item(0);
			Element phys = (Element) physNodeList.item(0);
			Node sponsor = costChileList.item(1);
			Node price = costChileList.item(2);

			HospitalChargeProcedure hospitalChargeProcedurehosp = new HospitalChargeProcedure();
			PhysicianChargeProcedure physicianChargeProcedure = new PhysicianChargeProcedure();

			if (hospitalChargeProcedureDao.findByCptCode(
					prodedure.getAttribute("cptcode")).size() == 0)
				continue;

			if (hosp.getAttribute("id").isEmpty()) {
				hospitalChargeProcedurehosp = hospitalChargeProcedureDao
						.findByCptCode(prodedure.getAttribute("cptcode"))
						.get(0);
			} else
				hospitalChargeProcedurehosp = hospitalChargeProcedureDao
						.findById(Integer.parseInt(hosp.getAttribute("id")));

			// update the hos.cost and phy.cost from the database
			if (hospitalChargeProcedurehosp != null)
				hosp.setAttribute("cost",
						"" + hospitalChargeProcedurehosp.getCost());

			if (physicianChargeProcedureDao.findByCptCode(
					prodedure.getAttribute("cptcode")).size() == 0)
				continue;

			if (phys.getAttribute("id").isEmpty()) {
				physicianChargeProcedure = physicianChargeProcedureDao
						.findByCptCode(prodedure.getAttribute("cptcode"))
						.get(0);
			} else
				physicianChargeProcedure = physicianChargeProcedureDao
						.findById(Integer.parseInt(phys.getAttribute("id")));

			if (physicianChargeProcedure != null)
				phys.setAttribute("cost",
						"" + physicianChargeProcedure.getCost());

			BigDecimal bdPrice = new BigDecimal(0);
			// set price=hosp.price+phy.price
			if (physicianChargeProcedure != null
					&& hospitalChargeProcedurehosp != null)
				bdPrice = hospitalChargeProcedurehosp.getCost().add(
						physicianChargeProcedure.getCost());
			else if (physicianChargeProcedure == null
					&& hospitalChargeProcedurehosp != null)
				bdPrice = hospitalChargeProcedurehosp.getCost();
			else if (physicianChargeProcedure != null
					&& hospitalChargeProcedurehosp == null)
				bdPrice = physicianChargeProcedure.getCost();

			sponsor.setTextContent("0");
			price.setTextContent("" + bdPrice);

		}

		BudgetXml = DomUtils.elementToString(Budget, false, Encoding.UTF16);
		return BudgetXml;

	}

	public XmlProcessor getXmlProcessor() {
		return xmlProcessor;
	}

	@Autowired(required = true)
	public void setXmlProcessor(XmlProcessor xmlProcessor) {
		this.xmlProcessor = xmlProcessor;
	}

	public HospitalChargeProcedureDao getHospitalChargeProcedureDao() {
		return hospitalChargeProcedureDao;
	}

	@Autowired(required = true)
	public void setHospitalChargeProcedureDao(
			HospitalChargeProcedureDao hospitalChargeProcedureDao) {
		this.hospitalChargeProcedureDao = hospitalChargeProcedureDao;
	}

	public PhysicianChargeProcedureDao getPhysicianChargeProcedureDao() {
		return physicianChargeProcedureDao;
	}

	@Autowired(required = true)
	public void setPhysicianChargeProcedureDao(
			PhysicianChargeProcedureDao physicianChargeProcedureDao) {
		this.physicianChargeProcedureDao = physicianChargeProcedureDao;
	}
}
