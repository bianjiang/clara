package edu.uams.clara.webapp.contract.web.contractform.ajax;

import java.util.List;

import javax.xml.transform.Source;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import edu.uams.clara.webapp.common.util.XMLResponseHelper;
import edu.uams.clara.webapp.contract.dao.businesslogicobject.ContractFormStatusDao;
import edu.uams.clara.webapp.contract.dao.contractform.ContractFormDao;
import edu.uams.clara.webapp.contract.domain.contractform.ContractForm;
import edu.uams.clara.webapp.contract.web.contractform.ajax.ContractFormAjaxController;
import edu.uams.clara.webapp.xml.processor.XmlProcessor;

public class ContractFormAjaxController {
	private final static Logger logger = LoggerFactory
			.getLogger(ContractFormAjaxController.class);

	private ContractFormDao contractFormDao;
	private ContractFormStatusDao contractFormStatusDao;

	private XmlProcessor xmlProcessor;

	@RequestMapping(value = "/ajax/contracts/{contractId}/contract-forms/{contractFormId}/versions-for-comparison", method = RequestMethod.GET, produces = "application/xml")
	public @ResponseBody
	Source getComparisonVersionPage(
			@PathVariable("contractId") long contractId,
			@PathVariable("contractFormId") long contractFormId) {
		// long parentId=
		// contractDao.getLastestContractXmlDataByContractId(contractId).getParent().getId();
		List<ContractForm> contractFormsForMod = contractFormDao
				.listContractFormsByContractId(contractId);

		// List<ContractFormXmlData> versions =
		// contractFormXmlDataDao.listContractformXmlDatasByParentId(parentId);
		// remove the current version
		// Map<String, ContractFormType> results = new HashMap<String,
		// ContractFormType>();
		Document formInfoDoc = xmlProcessor.newDocument();
		Element formsEle = formInfoDoc.createElement("form-infos");
		formInfoDoc.appendChild(formsEle);

		for (int i=contractFormsForMod.size()-1; i >-1; i--) {
			if (contractFormsForMod.get(i).getId() > contractFormId) {
				continue;
			}

			// make result String
			Element formEle = formInfoDoc.createElement("form-info");
			formsEle.appendChild(formEle);
			formEle.setAttribute("id", contractFormsForMod.get(i).getFormId()
					+ "");
			formEle.setAttribute("submitted", contractFormsForMod.get(i)
					.getCreated() + "");
			formEle.setAttribute("id", contractFormsForMod.get(i).getFormId()
					+ "");
			formEle.setAttribute("parentId", contractFormsForMod.get(i)
					.getParentFormId() + "");
			formEle.setAttribute(
					"status",
					contractFormStatusDao.getLatestContractFormStatusByFormId(
							contractFormsForMod.get(i).getFormId())
							.getContractFormStatus()
							+ "");
			formEle.setAttribute("formType", contractFormsForMod.get(i).getFormType().toString());

			// resultXml = DomUtils.elementToString(formInfoDoc, true,
			// Encoding.UTF8);
		}

		return XMLResponseHelper.newDataResponseStub(formInfoDoc);

	}
}
