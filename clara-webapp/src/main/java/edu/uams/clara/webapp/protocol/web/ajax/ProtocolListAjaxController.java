package edu.uams.clara.webapp.protocol.web.ajax;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.xpath.XPathExpressionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.google.common.collect.Lists;

import edu.uams.clara.webapp.common.dao.usercontext.UserDao;
import edu.uams.clara.webapp.common.domain.usercontext.User;
import edu.uams.clara.webapp.common.domain.usercontext.enums.Permission;
import edu.uams.clara.webapp.common.objectwrapper.PagedList;
import edu.uams.clara.webapp.common.util.response.JsonResponse;
import edu.uams.clara.webapp.contract.domain.Contract;
import edu.uams.clara.webapp.protocol.dao.ProtocolDao;
import edu.uams.clara.webapp.protocol.dao.search.ProtocolSearchBookmarkDao;
import edu.uams.clara.webapp.protocol.domain.Protocol;
import edu.uams.clara.webapp.protocol.domain.search.ProtocolSearchBookmark;
import edu.uams.clara.webapp.protocol.objectwrapper.ProtocolSearchCriteria;
import edu.uams.clara.webapp.xml.processor.XmlProcessor;

@Controller
public class ProtocolListAjaxController {

	private final static Logger logger = LoggerFactory
			.getLogger(ProtocolListAjaxController.class);

	private ProtocolDao protocolDao;

	private ProtocolSearchBookmarkDao protocolSearchBookmarkDao;
	
	private UserDao userDao;

	private XmlProcessor xmlProcessor;

	private static final int PAGE_SIZE = 20;
	
	private ObjectMapper objectMapper = new ObjectMapper();

	/**
	 * reading protocol meta data from metaDataXml in protocol object
	 * 
	 * @param start
	 * @param limit
	 * @param fields
	 * @param query
	 * @return
	 * @throws XPathExpressionException
	 * @throws SAXException
	 * @throws IOException
	 */
	//@TODO @Cacheable
	@RequestMapping(value = "/ajax/protocols/list.xml", method = RequestMethod.POST)
	public @ResponseBody
	String listProtocols(
			@RequestParam(value = "start", required = false) Integer start,
			@RequestParam(value = "limit", required = false) Integer limit,
			@RequestParam(value = "keyword", required = false) String quickSearchKeyword,
			@RequestParam(value = "searchCriterias", required = false) String searchCriteriasJsonString
			) //@RequestParam(value = "searchCriterias[]", required = false) List<ProtocolSearchCriteria> searchCriterias
			throws XPathExpressionException, SAXException, IOException { 

		int s = start == null ? 0 : start;
		int l = limit == null ? PAGE_SIZE : limit;
		User u = (User) SecurityContextHolder.getContext().getAuthentication()
				.getPrincipal();
		
		for (GrantedAuthority p:u.getAuthorities()){
			logger.debug("p: " + (Permission)p);
		}

		PagedList<Protocol> pagedProtocolMetaDatas = null;

		List<ProtocolSearchCriteria> searchCriterias = null;
		
		boolean quickSearch = false;
		
		if (quickSearchKeyword != null && (searchCriteriasJsonString == null || searchCriteriasJsonString.isEmpty())){
			quickSearchKeyword = quickSearchKeyword.trim();
			searchCriteriasJsonString = "[{\"searchField\":\"TITLE\",\"searchOperator\":\"CONTAINS\",\"keyword\":\""+quickSearchKeyword+"\"}, {\"searchField\":\"IDENTIFIER\",\"searchOperator\":\"CONTAINS\",\"keyword\":\""+quickSearchKeyword+"\"}]";

			quickSearch = true;
		}
		
		if(searchCriteriasJsonString != null && searchCriteriasJsonString.length() > 0){
			
			logger.debug("searchCriteriasJsonString :" + searchCriteriasJsonString);
			JavaType listOfProtocolSearchCriteria = TypeFactory.defaultInstance().constructCollectionType(ArrayList.class, ProtocolSearchCriteria.class);
			
			//ProtocolSearchCriteria protocolSearchCriteria = objectMapper.readValue(searchCriteriasJsonString, ProtocolSearchCriteria.class);
			//logger.debug("ProtocolSearchCriteria: " + protocolSearchCriteria.getSearchField());
			searchCriterias = objectMapper.readValue(searchCriteriasJsonString, listOfProtocolSearchCriteria);
		}

		pagedProtocolMetaDatas = protocolDao
				.listPagedProtocolMetaDatasByUserAndSearchCriteriaAndProtocolStatusFilter(
						u, s, l, searchCriterias, quickSearch);

		String finalResultXml = "<?xml version=\"1.0\" encoding=\"utf-8\"?><list total=\""
				+ pagedProtocolMetaDatas.getTotal() + "\">";

		if (pagedProtocolMetaDatas.getList() != null
				&& !pagedProtocolMetaDatas.getList().isEmpty()) {
			for (Protocol p : pagedProtocolMetaDatas.getList()) {
				logger.debug("" + p.getId());
				finalResultXml += (p.getMetaDataXml() != null ? p
						.getMetaDataXml() : "");
			}
		}
		finalResultXml += "</list>";
		
		/*
		String convertedString = 
			       Normalizer
			           .normalize(finalResultXml, Normalizer.Form.NFD)
			           .replaceAll("[^\\p{ASCII}]", "");*/

		//return new StringHelper().convertToAscii(finalResultXml,true);
		return finalResultXml;
		
	}

	@RequestMapping(value = "/ajax/protocols/search-bookmarks/save", method = RequestMethod.POST)
	public @ResponseBody
	JsonResponse saveProtocolSearchBookmarks(
			@RequestParam(value = "userId") long userId,
			@RequestParam(value = "name") String name,
			@RequestParam(value = "searchCriterias") String searchCriterias) {

		// User u =
		// (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		try {
			User u = userDao.findById(userId);
			ProtocolSearchBookmark protocolSearchBookmark = new ProtocolSearchBookmark();

			protocolSearchBookmark.setName(name);
			protocolSearchBookmark.setSearchCriterias(searchCriterias);
			protocolSearchBookmark.setUser(u);
			
			protocolSearchBookmark = protocolSearchBookmarkDao
					.saveOrUpdate(protocolSearchBookmark);

			return new JsonResponse(false);
		} catch (Exception ex) {
			ex.printStackTrace();
			logger.info("bookmark", ex);
			return new JsonResponse(true, "Saving search bookmarks is encounting an error!", "", false, null);
		}

	}
	
	@RequestMapping(value = "/ajax/protocols/search-bookmarks/{id}/remove", method = RequestMethod.POST)
	public @ResponseBody
	JsonResponse removeProtocolSearchBookmarks(
			@PathVariable("id") long id,
			@RequestParam(value = "userId") long userId) {

		// User u =
		// (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		try {
			//User u = userDao.findById(userId);//for audit...
			ProtocolSearchBookmark protocolSearchBookmark = protocolSearchBookmarkDao.findById(id);
			protocolSearchBookmark.setRetired(true);			
			
			protocolSearchBookmark = protocolSearchBookmarkDao
					.saveOrUpdate(protocolSearchBookmark);
			
			logger.debug("id: " + id);
			return new JsonResponse(false);
		} catch (Exception ex) {
			ex.printStackTrace();
			return new JsonResponse(true, "Removing search bookmarks is encounting an error!", "", false, null);
		}

	}
	
	private ProtocolSearchBookmark newCommonBookmark(long id, String name, String searchCriterias){
		
		ProtocolSearchBookmark bookmark = new ProtocolSearchBookmark();
		bookmark.setId(id);// common bookmark all have id <= 0 to avoid conflict with bookmarks from db.
		bookmark.setName(name);
		bookmark.setSearchCriterias(searchCriterias);
		
		return bookmark;
	}
	
	@RequestMapping(value = "/ajax/protocols/search-bookmarks/list", method = RequestMethod.GET)
	public @ResponseBody
	Map<String, List<ProtocolSearchBookmark>> listProtocolSearchBookmarks(
			@RequestParam(value = "userId") long userId) {

		Map<String, List<ProtocolSearchBookmark>> protocolSearchBookmarks = new HashMap<String, List<ProtocolSearchBookmark>>(0);
		
		try {
			
			List<ProtocolSearchBookmark> protocolSearchBookmarkList = Lists.newArrayList();
	
			protocolSearchBookmarkList.add(newCommonBookmark(-2, "All protocols", null));
			protocolSearchBookmarkList.add(newCommonBookmark(-1, "My protocols", "[{\"searchField\":\"MY_PROTOCOLS\",\"searchOperator\":\"IS\",\"keyword\":\"true\"}]"));
			protocolSearchBookmarkList.add(newCommonBookmark(0, "My protocols - PI Action Needed", "[{\"searchField\":\"PENDING_PI_ACTION\",\"searchOperator\":\"IS\",\"keyword\":\"true\"}]"));
			
			protocolSearchBookmarkList.addAll(protocolSearchBookmarkDao.listSearchBookmarksByUserId(userId));

			protocolSearchBookmarks.put("bookmarks", protocolSearchBookmarkList);
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		return protocolSearchBookmarks;
		

	}
	
	@RequestMapping(value = "/ajax/protocols/related-contract/list.xml", method = RequestMethod.GET)
	public @ResponseBody
	String listRelatedContract(
			@RequestParam(value = "protocolId", required = true) long protocolId,
			@RequestParam(value = "start", required = false) Integer start,
			@RequestParam(value = "limit", required = false) Integer limit
			)
			throws XPathExpressionException, SAXException, IOException { 

		int s = start == null ? 0 : start;
		int l = limit == null ? PAGE_SIZE : limit;
		User u = (User) SecurityContextHolder.getContext().getAuthentication()
				.getPrincipal();

		PagedList<Contract> pagedContractMetaDatas = protocolDao.listPagedContractMetaDatasByUserAndProtocolId(u, s, l, protocolId);
		
		String finalResultXml = "<?xml version=\"1.0\" encoding=\"utf-8\"?><list total=\""
				+ pagedContractMetaDatas.getTotal() + "\">";

		if (pagedContractMetaDatas.getList() != null
				&& !pagedContractMetaDatas.getList().isEmpty()) {
			for (Contract c : pagedContractMetaDatas.getList()) {
				logger.debug("" + c.getId());
				finalResultXml += (c.getMetaDataXml() != null ? c
						.getMetaDataXml() : "");
			}
		}
		finalResultXml += "</list>";

		return finalResultXml;
	}

	@Autowired(required = true)
	public void setProtocolDao(ProtocolDao protocolDao) {
		this.protocolDao = protocolDao;
	}

	public ProtocolDao getProtocolDao() {
		return protocolDao;
	}

	@Autowired(required = true)
	public void setXmlProcessor(XmlProcessor xmlProcessor) {
		this.xmlProcessor = xmlProcessor;
	}

	public XmlProcessor getXmlProcessor() {
		return xmlProcessor;
	}

	
	public ProtocolSearchBookmarkDao getProtocolSearchBookmarkDao() {
		return protocolSearchBookmarkDao;
	}

	@Autowired(required = true)
	public void setProtocolSearchBookmarkDao(
			ProtocolSearchBookmarkDao protocolSearchBookmarkDao) {
		this.protocolSearchBookmarkDao = protocolSearchBookmarkDao;
	}
	
	public UserDao getUserDao() {
		return userDao;
	}

	@Autowired(required = true)
	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}

}
