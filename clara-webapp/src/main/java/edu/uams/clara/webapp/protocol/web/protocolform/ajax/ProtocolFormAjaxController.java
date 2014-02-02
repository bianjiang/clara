package edu.uams.clara.webapp.protocol.web.protocolform.ajax;

import java.util.List;

import javax.xml.transform.Source;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import edu.uams.clara.webapp.common.util.XMLResponseHelper;
import edu.uams.clara.webapp.protocol.dao.businesslogicobject.ProtocolFormStatusDao;
import edu.uams.clara.webapp.protocol.dao.protocolform.ProtocolFormDao;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolForm;
import edu.uams.clara.webapp.xml.processor.XmlProcessor;

@Controller
public class ProtocolFormAjaxController {

	private final static Logger logger = LoggerFactory
			.getLogger(ProtocolFormAjaxController.class);

	private ProtocolFormDao protocolFormDao;
	private ProtocolFormStatusDao protocolFormStatusDao;

	private XmlProcessor xmlProcessor;

	@RequestMapping(value = "/ajax/protocols/{protocolId}/protocol-forms/{protocolFormId}/versions-for-comparison", method = RequestMethod.GET, produces = "application/xml")
	public @ResponseBody
	Source getComparisonVersionPage(
			@PathVariable("protocolId") long protocolId,
			@PathVariable("protocolFormId") long protocolFormId) {
		// long parentId=
		// protocolDao.getLastestProtocolXmlDataByProtocolId(protocolId).getParent().getId();
		List<ProtocolForm> protocolFormsForMod = protocolFormDao
				.listProtocolFormsByProtocolId(protocolId);

		// List<ProtocolFormXmlData> versions =
		// protocolFormXmlDataDao.listProtocolformXmlDatasByParentId(parentId);
		// remove the current version
		// Map<String, ProtocolFormType> results = new HashMap<String,
		// ProtocolFormType>();
		Document formInfoDoc = xmlProcessor.newDocument();
		Element formsEle = formInfoDoc.createElement("form-infos");
		formInfoDoc.appendChild(formsEle);
		for (int i=protocolFormsForMod.size()-1; i >-1; i--) {
			if (protocolFormsForMod.get(i).getId() > protocolFormId) {
				continue;
			}

			// make result String
			Element formEle = formInfoDoc.createElement("form-info");
			formsEle.appendChild(formEle);
			formEle.setAttribute("id", protocolFormsForMod.get(i).getFormId()
					+ "");
			formEle.setAttribute("submitted", protocolFormsForMod.get(i)
					.getCreated() + "");
			formEle.setAttribute("id", protocolFormsForMod.get(i).getFormId()
					+ "");
			formEle.setAttribute("parentId", protocolFormsForMod.get(i)
					.getParentFormId() + "");
			formEle.setAttribute(
					"status",
					protocolFormStatusDao.getLatestProtocolFormStatusByFormId(
							protocolFormsForMod.get(i).getFormId())
							.getProtocolFormStatus()
							+ "");
			formEle.setAttribute("formType", protocolFormsForMod.get(i).getFormType().toString());

			// resultXml = DomUtils.elementToString(formInfoDoc, true,
			// Encoding.UTF8);
		}

		return XMLResponseHelper.newDataResponseStub(formInfoDoc);

	}

	public ProtocolFormDao getProtocolFormDao() {
		return protocolFormDao;
	}

	@Autowired(required = true)
	public void setProtocolFormDao(ProtocolFormDao protocolFormDao) {
		this.protocolFormDao = protocolFormDao;
	}

	public ProtocolFormStatusDao getProtocolFromStatusDao() {
		return protocolFormStatusDao;
	}

	@Autowired(required = true)
	public void setProtocolFromStatusDao(
			ProtocolFormStatusDao protocolFromStatusDao) {
		this.protocolFormStatusDao = protocolFromStatusDao;
	}

	public XmlProcessor getXmlProcessor() {
		return xmlProcessor;
	}

	@Autowired(required = true)
	public void setXmlProcessor(XmlProcessor xmlProcessor) {
		this.xmlProcessor = xmlProcessor;
	}
}
