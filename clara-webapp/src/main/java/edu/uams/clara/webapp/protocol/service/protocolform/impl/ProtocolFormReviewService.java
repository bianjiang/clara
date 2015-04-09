package edu.uams.clara.webapp.protocol.service.protocolform.impl;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import com.google.common.collect.Sets;

import edu.uams.clara.core.util.xml.XmlHandler;
import edu.uams.clara.core.util.xml.XmlHandlerFactory;
import edu.uams.clara.webapp.common.domain.usercontext.User;
import edu.uams.clara.webapp.common.domain.usercontext.enums.Committee;
import edu.uams.clara.webapp.common.domain.usercontext.enums.Permission;
import edu.uams.clara.webapp.protocol.dao.businesslogicobject.ProtocolFormCommitteeStatusDao;
import edu.uams.clara.webapp.protocol.dao.businesslogicobject.ProtocolFormStatusDao;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.ProtocolFormCommitteeStatus;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.ProtocolFormStatus;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolForm;
import edu.uams.clara.webapp.xml.processor.XmlProcessor;

@Service
public class ProtocolFormReviewService {
	@Value("${add.note.permission.xml.url}")
	private String addNotePermissionXmlPath;
	
	private ProtocolFormStatusDao protocolFormStatusDao;
	
	private ProtocolFormCommitteeStatusDao protocolFormCommitteeStatusDao;
	
	private ResourceLoader resourceLoader;
	
	private XmlProcessor xmlProcessor;
	
	public Set<Permission> getObjectPermissions(ProtocolForm protocolForm, User user, Committee committee) {
		Set<Permission> objectPermissions = Sets.newHashSet();
		
		objectPermissions.addAll((Collection)user.getAuthorities());
		
		objectPermissions.remove(Permission.COMMENT_CAN_ADD);
		
		objectPermissions.remove(Permission.CONTINGENCY_CAN_ADD);
		
		try {
			Resource  addNotePermissionXmlResource = resourceLoader.getResource(addNotePermissionXmlPath);
			
			ProtocolFormStatus latestFormStatus = protocolFormStatusDao.getLatestProtocolFormStatusByFormId(protocolForm.getId());
			
			String latestFormStatusStr = latestFormStatus.getProtocolFormStatus().toString();
			
			ProtocolFormCommitteeStatus latestFormCommitteeStatus = protocolFormCommitteeStatusDao.getLatestByCommitteeAndProtocolFormId(committee, protocolForm.getFormId());
			
			String latestCommitteeStatusStr = "";
			
			if (latestFormCommitteeStatus != null) {
				latestCommitteeStatusStr = latestFormCommitteeStatus.getProtocolFormCommitteeStatus().toString();
			}
			
			XmlHandler xmlHandler = XmlHandlerFactory.newXmlHandler();
			
			String lookupPath = "/notes-permission/form[@type='"+ protocolForm.getProtocolFormType().toString() +"']/status[@form-status='"+ latestFormStatusStr +"' or @form-status='ANY']/commmittees/committee[@name='"+ committee.toString() +"'][@status='"+ latestCommitteeStatusStr +"' or @status='']/permissions-to-add/permission";
			
			String addNotePermissionXmlString = xmlProcessor.loadXmlFile(addNotePermissionXmlResource.getFile());
			
			List<String> toAddPermissionLst = xmlHandler.getStringValuesByXPath(addNotePermissionXmlString, lookupPath);
			
			if (toAddPermissionLst.size() > 0) {
				for (String permissionStr : toAddPermissionLst) {
					objectPermissions.add(Permission.valueOf(permissionStr));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return objectPermissions;
	}

	public String getAddNotePermissionXmlPath() {
		return addNotePermissionXmlPath;
	}

	public void setAddNotePermissionXmlPath(String addNotePermissionXmlPath) {
		this.addNotePermissionXmlPath = addNotePermissionXmlPath;
	}

	public ProtocolFormStatusDao getProtocolFormStatusDao() {
		return protocolFormStatusDao;
	}
	
	@Autowired(required = true)
	public void setProtocolFormStatusDao(ProtocolFormStatusDao protocolFormStatusDao) {
		this.protocolFormStatusDao = protocolFormStatusDao;
	}

	public ProtocolFormCommitteeStatusDao getProtocolFormCommitteeStatusDao() {
		return protocolFormCommitteeStatusDao;
	}
	
	@Autowired(required = true)
	public void setProtocolFormCommitteeStatusDao(
			ProtocolFormCommitteeStatusDao protocolFormCommitteeStatusDao) {
		this.protocolFormCommitteeStatusDao = protocolFormCommitteeStatusDao;
	}

	public ResourceLoader getResourceLoader() {
		return resourceLoader;
	}
	
	@Autowired(required = true)
	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}

	public XmlProcessor getXmlProcessor() {
		return xmlProcessor;
	}
	
	@Autowired(required = true)
	public void setXmlProcessor(XmlProcessor xmlProcessor) {
		this.xmlProcessor = xmlProcessor;
	}
}
