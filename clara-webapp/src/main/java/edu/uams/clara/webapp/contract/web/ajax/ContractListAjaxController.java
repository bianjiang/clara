package edu.uams.clara.webapp.contract.web.ajax;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.xpath.XPathExpressionException;

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
import org.xml.sax.SAXException;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;

import edu.uams.clara.webapp.common.dao.usercontext.UserDao;
import edu.uams.clara.webapp.common.domain.usercontext.User;
import edu.uams.clara.webapp.common.objectwrapper.PagedList;
import edu.uams.clara.webapp.common.util.StringHelper;
import edu.uams.clara.webapp.common.util.response.JsonResponse;
import edu.uams.clara.webapp.contract.dao.ContractDao;
import edu.uams.clara.webapp.contract.dao.contractform.ContractFormDao;
import edu.uams.clara.webapp.contract.dao.search.ContractSearchBookmarkDao;
import edu.uams.clara.webapp.contract.domain.Contract;
import edu.uams.clara.webapp.contract.domain.businesslogicobject.enums.ContractFormStatusEnum;
import edu.uams.clara.webapp.contract.domain.contractform.ContractForm;
import edu.uams.clara.webapp.contract.domain.search.ContractSearchBookmark;
import edu.uams.clara.webapp.contract.objectwrapper.ContractSearchCriteria;
import edu.uams.clara.webapp.protocol.domain.Protocol;
import edu.uams.clara.webapp.protocol.objectwrapper.ProtocolSearchCriteria;
import edu.uams.clara.webapp.xml.processor.XmlProcessor;

@Controller
public class ContractListAjaxController {

	private final static Logger logger = LoggerFactory
			.getLogger(ContractListAjaxController.class);

	private ContractDao contractDao;
	
	private ContractFormDao contractFormDao;

	private ContractSearchBookmarkDao contractSearchBookmarkDao;
	
	private UserDao userDao;

	private XmlProcessor xmlProcessor;

	private static final int PAGE_SIZE = 20;
	
	private ObjectMapper objectMapper = new ObjectMapper();
	/**
	 * reading contract meta data from metaDataXml in contract object
	 * 
	 * @param filter
	 * @param start
	 * @param limit
	 * @param fields
	 * @param query
	 * @return
	 * @throws XPathExpressionException
	 * @throws SAXException
	 * @throws IOException
	 */
	/*
	 * @Cacheable(cacheName = "contractListXmlCache", keyGenerator =
	 * @KeyGenerator(name = "HashCodeCacheKeyGenerator", properties = {
	 * 
	 * @Property(name = "includeMethod", value = "false") }))
	 */

	@RequestMapping(value = "/ajax/contracts/list.xml", method = RequestMethod.POST)
	public @ResponseBody
	String listContracts(
			@RequestParam(value = "filter", required = false) ContractFormStatusEnum filter,
			@RequestParam(value = "start", required = false) Integer start,
			@RequestParam(value = "limit", required = false) Integer limit,
			@RequestParam(value = "keyword", required = false) String quickSearchKeyword,
			@RequestParam(value = "searchCriterias", required = false) String searchCriteriasJsonString
			) //@RequestParam(value = "searchCriterias[]", required = false) List<ContractSearchCriteria> searchCriterias
			throws XPathExpressionException, SAXException, IOException { 

		int s = start == null ? 0 : start;
		int l = limit == null ? PAGE_SIZE : limit;
		User u = (User) SecurityContextHolder.getContext().getAuthentication()
				.getPrincipal();

		PagedList<ContractForm> pagedContractMetaDatas = null;

		logger.debug("filter: " + filter);
		List<ContractSearchCriteria> searchCriterias = null;
		
		if (quickSearchKeyword != null && !quickSearchKeyword.isEmpty() && (searchCriteriasJsonString == null || searchCriteriasJsonString.isEmpty())){
			
			quickSearchKeyword = quickSearchKeyword.trim();
			//searchCriteriasJsonString = "[{\"searchField\":\"TITLE\",\"searchOperator\":\"CONTAINS\",\"keyword\":\""+quickSearchKeyword+"\"}, {\"searchField\":\"IDENTIFIER\",\"searchOperator\":\"CONTAINS\",\"keyword\":\""+quickSearchKeyword+"\"}, {\"searchField\":\"ENTITY_NAME\",\"searchOperator\":\"CONTAINS\",\"keyword\":\""+quickSearchKeyword+"\"}, {\"searchField\":\"PROTOCOL_ID\",\"searchOperator\":\"EQUALS\",\"keyword\":\""+quickSearchKeyword+"\"}, {\"searchField\":\"PI_NAME\",\"searchOperator\":\"CONTAINS\",\"keyword\":\""+quickSearchKeyword+"\"}]";
			//use Full Text instead
			searchCriteriasJsonString = "[{\"searchField\":\"IDENTIFIER\",\"searchOperator\":\"CONTAINS\",\"keyword\":\""+quickSearchKeyword+"\"}, {\"searchField\":\"ENTITY_NAME\",\"searchOperator\":\"CONTAINS\",\"keyword\":\""+quickSearchKeyword+"\"}, {\"searchField\":\"PROTOCOL_ID\",\"searchOperator\":\"EQUALS\",\"keyword\":\""+quickSearchKeyword+"\"}]";
		}
		
		if(searchCriteriasJsonString != null && searchCriteriasJsonString.length() > 0){
			
			logger.debug("searchCriteriasJsonString :" + searchCriteriasJsonString);
			JavaType listOfContractSearchCriteria = TypeFactory.defaultInstance().constructCollectionType(ArrayList.class, ContractSearchCriteria.class);
			
			//ContractSearchCriteria contractSearchCriteria = objectMapper.readValue(searchCriteriasJsonString, ContractSearchCriteria.class);
			//logger.debug("ContractSearchCriteria: " + contractSearchCriteria.getSearchField());
			searchCriterias = objectMapper.readValue(searchCriteriasJsonString, listOfContractSearchCriteria);
			
		}
		logger.debug("searchCriterias: "+searchCriterias);
		/*pagedContractMetaDatas = contractDao
				.listPagedContractMetaDatasByUserAndSearchCriteriaAndContractStatusFilter(
						u, s, l, searchCriterias, filter);*/
		pagedContractMetaDatas = contractFormDao
				.listPagedContractMetaDatasByUserAndSearchCriteriaAndContractStatusFilter(
						u, s, l, searchCriterias, filter, quickSearchKeyword);

		String finalResultXml = "<list total=\""
				+ pagedContractMetaDatas.getTotal() + "\">";

		if (pagedContractMetaDatas.getList() != null
				&& !pagedContractMetaDatas.getList().isEmpty()) {
			for (ContractForm p : pagedContractMetaDatas.getList()) {
				logger.debug("" + p.getId());
				finalResultXml += (p.getMetaDataXml() != null ? p
						.getMetaDataXml() : "");
			}
		}
		finalResultXml += "</list>";

		
		//return new StringHelper().convertToAscii(finalResultXml,true);
		/*String convertedString = 
			       Normalizer
			           .normalize(finalResultXml, Normalizer.Form.NFD)
			           .replaceAll("[^\\p{ASCII}]", "");*/

		//return new StringHelper().convertToAscii(finalResultXml,true);
		return finalResultXml;
	}
	
//	@RequestMapping(value = "/ajax/contracts/search-by-protocol/list.xml", method = RequestMethod.GET)
//	public @ResponseBody
//	String listContractsByProtocolId(
//			@RequestParam(value = "protocolId", required = true) long protocolId,
//			@RequestParam(value = "start", required = false) Integer start,
//			@RequestParam(value = "limit", required = false) Integer limit
//			)
//			throws XPathExpressionException, SAXException, IOException { 
//
//		int s = start == null ? 0 : start;
//		int l = limit == null ? PAGE_SIZE : limit;
//		User u = (User) SecurityContextHolder.getContext().getAuthentication()
//				.getPrincipal();
//
//		PagedList<Contract> pagedContractMetaDatas = contractDao.listPagedContractMetaDatasByUserAndProtocolId(u, s, l, protocolId);
//		
//		String finalResultXml = "<?xml version=\"1.0\" encoding=\"utf-8\"?><list total=\""
//				+ pagedContractMetaDatas.getTotal() + "\">";
//
//		if (pagedContractMetaDatas.getList() != null
//				&& !pagedContractMetaDatas.getList().isEmpty()) {
//			for (Contract c : pagedContractMetaDatas.getList()) {
//				logger.debug("" + c.getId());
//				finalResultXml += (c.getMetaDataXml() != null ? c
//						.getMetaDataXml() : "");
//			}
//		}
//		finalResultXml += "</list>";
//
//		return finalResultXml;
//	}
	
	@RequestMapping(value = "/ajax/contracts/related-contract/list.xml", method = RequestMethod.GET)
	public @ResponseBody
	String listContractsByContractId(
			@RequestParam(value = "contractId", required = true) long contractId,
			@RequestParam(value = "start", required = false) Integer start,
			@RequestParam(value = "limit", required = false) Integer limit
			)
			throws XPathExpressionException, SAXException, IOException { 

		int s = start == null ? 0 : start;
		int l = limit == null ? PAGE_SIZE : limit;
		User u = (User) SecurityContextHolder.getContext().getAuthentication()
				.getPrincipal();

		PagedList<Contract> pagedContractMetaDatas = contractDao.listPagedContractMetaDatasByUserAndContractId(u, s, l, contractId);
		
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

		return new StringHelper().convertToAscii(finalResultXml,true);
		//return finalResultXml;
	}
	
	@RequestMapping(value = "/ajax/contracts/related-protocol/list.xml", method = RequestMethod.GET)
	public @ResponseBody
	String listProtocolsByContractId(
			@RequestParam(value = "contractId", required = true) long contractId,
			@RequestParam(value = "start", required = false) Integer start,
			@RequestParam(value = "limit", required = false) Integer limit
			)
			throws XPathExpressionException, SAXException, IOException { 

		int s = start == null ? 0 : start;
		int l = limit == null ? PAGE_SIZE : limit;
		User u = (User) SecurityContextHolder.getContext().getAuthentication()
				.getPrincipal();

		PagedList<Protocol> pagedContractMetaDatas = contractDao.listPagedProtocolMetaDatasByUserAndContractId(u, s, l, contractId);
		
		String finalResultXml = "<?xml version=\"1.0\" encoding=\"utf-8\"?><list total=\""
				+ pagedContractMetaDatas.getTotal() + "\">";

		if (pagedContractMetaDatas.getList() != null
				&& !pagedContractMetaDatas.getList().isEmpty()) {
			for (Protocol c : pagedContractMetaDatas.getList()) {
				logger.debug("" + c.getId());
				finalResultXml += (c.getMetaDataXml() != null ? c
						.getMetaDataXml() : "");
			}
		}
		finalResultXml += "</list>";

		return finalResultXml;
	}

	@RequestMapping(value = "/ajax/contracts/search-bookmarks/save", method = RequestMethod.POST)
	public @ResponseBody
	JsonResponse saveContractSearchBookmarks(
			@RequestParam(value = "userId") long userId,
			@RequestParam(value = "name") String name,
			@RequestParam(value = "searchCriterias") String searchCriterias) {

		// User u =
		// (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		try {
			User u = userDao.findById(userId);
			ContractSearchBookmark contractSearchBookmark = new ContractSearchBookmark();

			contractSearchBookmark.setName(name);
			contractSearchBookmark.setSearchCriterias(searchCriterias);
			contractSearchBookmark.setUser(u);
			
			contractSearchBookmark = contractSearchBookmarkDao
					.saveOrUpdate(contractSearchBookmark);

			return new JsonResponse(false, "", "", false, null);
		} catch (Exception ex) {
			ex.printStackTrace();
			return new JsonResponse(true, "Saving search bookmarks is encounting an error!", "", false, null);
		}

	}
	
	@RequestMapping(value = "/ajax/contracts/search-bookmarks/export", method = RequestMethod.POST)
	public @ResponseBody
	String exportBookmakrSearch(
			@RequestParam(value = "searchCriterias", required = false) String searchCriteriasJsonString
			) //@RequestParam(value = "searchCriterias[]", required = false) List<ProtocolSearchCriteria> searchCriterias
			throws XPathExpressionException, SAXException, IOException { 

		User u = (User) SecurityContextHolder.getContext().getAuthentication()
				.getPrincipal();

		PagedList<ContractForm> pagedContractMetaDatas = null;

		List<ContractSearchCriteria> searchCriterias = null;
		
		if(searchCriteriasJsonString != null && searchCriteriasJsonString.length() > 0){
			
			logger.debug("searchCriteriasJsonString :" + searchCriteriasJsonString);
			JavaType listOfContractSearchCriteria = TypeFactory.defaultInstance().constructCollectionType(ArrayList.class, ContractSearchCriteria.class);
			
			//ContractSearchCriteria contractSearchCriteria = objectMapper.readValue(searchCriteriasJsonString, ContractSearchCriteria.class);
			//logger.debug("ContractSearchCriteria: " + contractSearchCriteria.getSearchField());
			searchCriterias = objectMapper.readValue(searchCriteriasJsonString, listOfContractSearchCriteria);
			
		}
		logger.debug("searchCriterias: "+searchCriterias);
		/*pagedContractMetaDatas = contractDao
				.listPagedContractMetaDatasByUserAndSearchCriteriaAndContractStatusFilter(
						u, s, l, searchCriterias, filter);*/
		pagedContractMetaDatas = contractFormDao
				.listPagedContractMetaDatasByUserAndSearchCriteriaAndContractStatusFilter(
						u, 0, 10000000, searchCriterias, null, null);

		String finalResultXml = "<list total=\""
				+ pagedContractMetaDatas.getTotal() + "\">";

		if (pagedContractMetaDatas.getList() != null
				&& !pagedContractMetaDatas.getList().isEmpty()) {
			for (ContractForm cf: pagedContractMetaDatas.getList()) {
				//logger.debug("" + p.getId());
				finalResultXml += (cf.getMetaDataXml() != null ? cf
						.getMetaDataXml() : "");
			}
		}
		finalResultXml += "</list>";
		//logger.debug(finalResultXml);
		String fileUrl = contractFormDao.exportBookmarkSearchResultFile(finalResultXml,u);
		
		return fileUrl;
	}
	
	@RequestMapping(value = "/ajax/contracts/search-bookmarks/{searchBookmarkId}/update", method = RequestMethod.POST)
	public @ResponseBody
	JsonResponse updateProtocolSearchBookmarks(
			@PathVariable(value = "searchBookmarkId") long searchBookmarkId,
			@RequestParam(value = "userId") long userId,
			@RequestParam(value = "name") String name,
			@RequestParam(value = "searchCriterias") String searchCriterias) {

		try {
			ContractSearchBookmark contractSearchBookmark = contractSearchBookmarkDao.findById(searchBookmarkId);

			contractSearchBookmark.setName(name);
			contractSearchBookmark.setSearchCriterias(searchCriterias);
			
			contractSearchBookmark = contractSearchBookmarkDao
					.saveOrUpdate(contractSearchBookmark);

			return new JsonResponse(false);
		} catch (Exception ex) {
			ex.printStackTrace();
			logger.info("bookmark", ex);
			return new JsonResponse(true, "Failed to update bookmark!", "", false, null);
		}

	}
	
	@RequestMapping(value = "/ajax/contracts/search-bookmarks/{id}/remove", method = RequestMethod.POST)
	public @ResponseBody
	JsonResponse removeContractSearchBookmarks(
			@PathVariable("id") long id,
			@RequestParam(value = "userId") long userId) {

		// User u =
		// (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		try {
			//User u = userDao.findById(userId);//for audit...
			ContractSearchBookmark contractSearchBookmark = contractSearchBookmarkDao.findById(id);
			contractSearchBookmark.setRetired(true);			
			
			contractSearchBookmark = contractSearchBookmarkDao
					.saveOrUpdate(contractSearchBookmark);

			return new JsonResponse(false, "", "", false, null);
		} catch (Exception ex) {
			ex.printStackTrace();
			return new JsonResponse(true, "Removing search bookmarks is encounting an error!", "", false, null);
		}

	}
	
	@RequestMapping(value = "/ajax/contracts/search-bookmarks/list", method = RequestMethod.GET)
	public @ResponseBody
	Map<String, List<ContractSearchBookmark>> listContractSearchBookmarks(
			@RequestParam(value = "userId") long userId) {

		Map<String, List<ContractSearchBookmark>> contractSearchBookmarks = new HashMap<String, List<ContractSearchBookmark>>(0);
		
		try {
			
			List<ContractSearchBookmark> contractSearchBookmarkList = new ArrayList<ContractSearchBookmark>();
	
			ContractSearchBookmark allContracts = new ContractSearchBookmark();
			allContracts.setName("All contracts");
			allContracts.setId(0);
			contractSearchBookmarkList.add(allContracts);
			
			/*
			ContractSearchBookmark myContracts = new ContractSearchBookmark();
			myContracts.setName("My contracts");
			myContracts.setId(1);
			contractSearchBookmarkList.add(myContracts);
			*/
			
			contractSearchBookmarkList.addAll(contractSearchBookmarkDao.listSearchBookmarksByUserId(userId));

			contractSearchBookmarks.put("bookmarks", contractSearchBookmarkList);
			
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		return contractSearchBookmarks;
		

	}

	@Autowired(required = true)
	public void setContractDao(ContractDao contractDao) {
		this.contractDao = contractDao;
	}

	public ContractDao getContractDao() {
		return contractDao;
	}

	@Autowired(required = true)
	public void setXmlProcessor(XmlProcessor xmlProcessor) {
		this.xmlProcessor = xmlProcessor;
	}

	public XmlProcessor getXmlProcessor() {
		return xmlProcessor;
	}

	
	public ContractSearchBookmarkDao getContractSearchBookmarkDao() {
		return contractSearchBookmarkDao;
	}

	@Autowired(required = true)
	public void setContractSearchBookmarkDao(
			ContractSearchBookmarkDao contractSearchBookmarkDao) {
		this.contractSearchBookmarkDao = contractSearchBookmarkDao;
	}
	
	public UserDao getUserDao() {
		return userDao;
	}

	@Autowired(required = true)
	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}

	public ContractFormDao getContractFormDao() {
		return contractFormDao;
	}
	
	@Autowired(required = true)
	public void setContractFormDao(ContractFormDao contractFormDao) {
		this.contractFormDao = contractFormDao;
	}

}
