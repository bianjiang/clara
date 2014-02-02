package edu.uams.clara.webapp.contract.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import edu.uams.clara.core.dao.AbstractDomainDao;

import edu.uams.clara.webapp.common.domain.usercontext.User;
import edu.uams.clara.webapp.common.objectwrapper.PagedList;
import edu.uams.clara.webapp.contract.domain.Contract;
import edu.uams.clara.webapp.contract.domain.businesslogicobject.ContractStatus;
import edu.uams.clara.webapp.contract.domain.businesslogicobject.enums.ContractStatusEnum;
import edu.uams.clara.webapp.contract.domain.contractform.ContractForm;
import edu.uams.clara.webapp.contract.domain.contractform.ContractFormXmlData;
import edu.uams.clara.webapp.contract.domain.contractform.enums.ContractFormType;
import edu.uams.clara.webapp.contract.domain.contractform.enums.ContractFormXmlDataType;
import edu.uams.clara.webapp.contract.objectwrapper.ContractSearchCriteria;
import edu.uams.clara.webapp.protocol.domain.Protocol;

@Repository
public class ContractDao extends AbstractDomainDao<Contract> {

	private static final long serialVersionUID = 7492131024720753741L;

	private final static Logger logger = LoggerFactory
			.getLogger(ContractDao.class);

	/**
	 * TODO: index the xml...
	 * 
	 * @param fields
	 * @param query
	 * @return
	 */
	@Transactional(readOnly = true)
	private List<String> contractSearchCriteriaResolver(
			List<ContractSearchCriteria> searchCriteria) {
		List<String> xPathCriteria = new ArrayList<String>();

		for (ContractSearchCriteria p : searchCriteria) {
			switch (p.getSearchField()) {
			case IDENTIFIER: {
				logger.debug("it thinks theres an IDENTIFIER..");
				switch (p.getSearchOperator()) {
				case CONTAINS:
					xPathCriteria
							.add("meta_data_xml.exist('/contract[fn:contains(@identifier,\""
									+ p.getKeyword().toUpperCase()
									+ "\")]') = 1");
					// xPathCriteria.add("meta_data_xml.exist('/protocol[@identifier[fn:contains(fn:upper-case(.), \""
					// + p.getKeyword().toUpperCase() + "\")]]') = 1");
					break;
				case EQUALS:
					xPathCriteria
							.add("meta_data_xml.exist('/contract[@identifier[. = \""
									+ p.getKeyword() + "\"]]') = 1");
					break;
				case DOES_NOT_CONTAIN:
					xPathCriteria
							.add("meta_data_xml.exist('/contract[@identifier[fn:contains(fn:upper-case(.), \""
									+ p.getKeyword().toUpperCase()
									+ "\")]]') = 0");
				default:
					break;
				}
			}
				break;
			case TITLE: {
				switch (p.getSearchOperator()) {
				case CONTAINS:
					xPathCriteria
							.add("meta_data_xml.exist('/contract/title/text()[fn:contains(fn:upper-case(.), \""
									+ p.getKeyword().toUpperCase()
									+ "\")]') = 1");
					break;
				case EQUALS:
					xPathCriteria
							.add("meta_data_xml.exist('/contract/title[. = \""
									+ p.getKeyword() + "\"]') = 1");
					break;
				case DOES_NOT_CONTAIN:
					xPathCriteria
							.add("meta_data_xml.exist('/contract/title/text()[fn:contains(fn:upper-case(.), \""
									+ p.getKeyword().toUpperCase()
									+ "\")]') = 0");
				default:
					break;
				}
			}
				break;
			case PI_NAME: {
				switch (p.getSearchOperator()) {
				case CONTAINS:
					xPathCriteria
							.add("meta_data_xml.exist('/contract/staffs/staff/user[roles/role=\"Principal Investigator\" and (lastname[fn:contains(fn:upper-case(.), \""
									+ p.getKeyword().toUpperCase()
									+ "\")] or firstname[fn:contains(fn:upper-case(.), \""
									+ p.getKeyword().toUpperCase()
									+ "\")])]') = 1");
					break;
				case EQUALS:
					xPathCriteria
							.add("meta_data_xml.exist('/contract/staffs/staff/user[roles/role=\"Principal Investigator\" and (lastname[fn:contains(fn:upper-case(.), \""
									+ p.getKeyword().toUpperCase()
									+ "\")] or firstname[fn:contains(fn:upper-case(.), \""
									+ p.getKeyword().toUpperCase()
									+ "\")])]') = 1");
					break;
				case DOES_NOT_CONTAIN:
					xPathCriteria
							.add("meta_data_xml.exist('/contract/staffs/staff/user[roles/role=\"Principal Investigator\" and (lastname[fn:contains(fn:upper-case(.), \""
									+ p.getKeyword().toUpperCase()
									+ "\")] or firstname[fn:contains(fn:upper-case(.), \""
									+ p.getKeyword().toUpperCase()
									+ "\")])]') = 0");
				default:
					break;
				}
			}
				break;
			case CONTRACT_STATUS: {
				switch (p.getSearchOperator()) {
				case CONTAINS:
					xPathCriteria
							.add("c.id IN (SELECT contract_id FROM contract_status WHERE retired = :retired AND id IN (SELECT MAX(id) FROM CONTRACT_STATUS WHERE retired = :retired GROUP BY contract_id) AND contract_status = '"+ p.getKeyword() +"')");
					break;
				case EQUALS:
					xPathCriteria
					.add("c.id IN (SELECT contract_id FROM contract_status WHERE retired = :retired AND id IN (SELECT MAX(id) FROM CONTRACT_STATUS WHERE retired = :retired GROUP BY contract_id) AND contract_status = '"+ p.getKeyword() +"')");
					break;
				case DOES_NOT_CONTAIN:
					xPathCriteria
					.add("c.id NOT IN (SELECT contract_id FROM contract_status WHERE retired = :retired AND id IN (SELECT MAX(id) FROM CONTRACT_STATUS WHERE retired = :retired GROUP BY contract_id) AND contract_status = '"+ p.getKeyword() +"')");
				default:
					break;
				}
			}
				break;
			case STAFF_NAME: {
				switch (p.getSearchOperator()) {
				case CONTAINS:
					xPathCriteria
							.add("meta_data_xml.exist('/contract/staffs/staff/user/lastname[fn:contains(fn:upper-case(.), \""
									+ p.getKeyword().toUpperCase()
									+ "\") or /contract/staffs/staff/user/firstname[fn:contains(fn:upper-case(.), \""
									+ p.getKeyword().toUpperCase()
									+ "\")]]') = 1");
					break;
				case EQUALS:
					xPathCriteria
							.add("meta_data_xml.exist('/contract/staffs/staff/user/lastname[fn:contains(fn:upper-case(.), \""
									+ p.getKeyword().toUpperCase()
									+ "\") or /contract/staffs/staff/user/firstname[fn:contains(fn:upper-case(.), \""
									+ p.getKeyword().toUpperCase()
									+ "\")]]') = 1");
					break;
				case DOES_NOT_CONTAIN:
					xPathCriteria
							.add("meta_data_xml.exist('/contract/staffs/staff/user/lastname[fn:contains(fn:upper-case(.), \""
									+ p.getKeyword().toUpperCase()
									+ "\") or /contract/staffs/staff/user/firstname[fn:contains(fn:upper-case(.), \""
									+ p.getKeyword().toUpperCase()
									+ "\")]]') = 0");
				default:
					break;
				}
			}
				break;
			case CONTRACT_TYPE: {
				switch (p.getSearchOperator()) {
				case CONTAINS:
					xPathCriteria
							.add("meta_data_xml.exist('/contract/basic-information/contract-type/text()[fn:contains(fn:upper-case(.), \""
									+ p.getKeyword().toUpperCase()
									+ "\")]') = 1");
					break;
				case EQUALS:
					xPathCriteria
							.add("meta_data_xml.exist('/contract/basic-information/contract-type[. = \""
									+ p.getKeyword() + "\"]') = 1");
					break;
				case DOES_NOT_CONTAIN:
					xPathCriteria
							.add("meta_data_xml.exist('/contract/basic-information/contract-type/text()[fn:contains(fn:upper-case(.), \""
									+ p.getKeyword().toUpperCase()
									+ "\")]') = 0");
				default:
					break;
				}
			}
				break;
			case ENTITY_NAME: {
				switch (p.getSearchOperator()) {
				case CONTAINS:
					xPathCriteria
							.add("meta_data_xml.exist('/contract/sponsors/sponsor/name[fn:contains(fn:upper-case(.), \""
									+ p.getKeyword().toUpperCase()
									+ "\") or /contract/sponsors/sponsor/company[fn:contains(fn:upper-case(.), \""
									+ p.getKeyword().toUpperCase()
									+ "\")]]') = 1");
					break;
				case EQUALS:
					xPathCriteria
							.add("meta_data_xml.exist('/contract/sponsors/sponsor/name[fn:contains(fn:upper-case(.), \""
									+ p.getKeyword().toUpperCase()
									+ "\") or /contract/sponsors/sponsor/company[fn:contains(fn:upper-case(.), \""
									+ p.getKeyword().toUpperCase()
									+ "\")]]') = 1");
					break;
				case DOES_NOT_CONTAIN:
					xPathCriteria
							.add("meta_data_xml.exist('/contract/sponsors/sponsor/name[fn:contains(fn:upper-case(.), \""
									+ p.getKeyword().toUpperCase()
									+ "\") or /contract/sponsors/sponsor/company[fn:contains(fn:upper-case(.), \""
									+ p.getKeyword().toUpperCase()
									+ "\")]]') = 0");
				default:
					break;
				}
			}
				break;
			case PROTOCOL_ID: {
				switch (p.getSearchOperator()) {
				case CONTAINS:
					xPathCriteria
							.add("meta_data_xml.exist('/contract/protocol/text()[fn:contains(fn:upper-case(.), \""
									+ p.getKeyword().toUpperCase()
									+ "\")]') = 1");
					break;
				case EQUALS:
					xPathCriteria
							.add("meta_data_xml.exist('/contract/protocol[. = \""
									+ p.getKeyword() + "\"]') = 1");
					break;
				case DOES_NOT_CONTAIN:
					xPathCriteria
							.add("meta_data_xml.exist('/contract/protocol/text()[fn:contains(fn:upper-case(.), \""
									+ p.getKeyword().toUpperCase()
									+ "\")]') = 0");
				default:
					break;
				}
			}
				break;
			default:
				break;
			}
		}

		/*
		 * for (ContractSearchField field : fields) { switch (field) { case
		 * TITLE:
		 * xPathCriteria.add("meta_data.exist('/contract/title[fn:contains(., \""
		 * + keyword + "\")]') = 1"); break; case PI_NAME: xPathCriteria.add(
		 * "meta_data.exist('/contract/staffs/staff/user/roles/role[.=\"Principal Investigator\" and (../../user/lastname[fn:contains(fn:upper-case(.), \""
		 * + keyword.toUpperCase() +
		 * "\")] or ../../user/firstname[fn:contains(fn:upper-case(.), \"" +
		 * keyword.toUpperCase() + "\")])]') = 1"); break; case PROTOCOL_STATUS:
		 * break; case STAFF_NAME: xPathCriteria.add(
		 * "meta_data.exist('/contract/staffs/staff/user/lastname[fn:contains(fn:upper-case(.), \""
		 * + keyword.toUpperCase() + "\")]') = 1"); xPathCriteria.add(
		 * "meta_data.exist('/contract/staffs/staff/user/firstname[fn:contains(fn:upper-case(.), \""
		 * + keyword.toUpperCase() + "\")]') = 1"); break; default: break; } }
		 */

		return xPathCriteria;
	}

	@Transactional(readOnly = true)
	public List<ContractForm> listLatestContractFormsByContractId(
			long contractId) {
		String query = "SELECT cf FROM ContractForm cf WHERE cf.retired = :retired AND cf.contract.id = :contractId"
				+ " AND cf.id in (SELECT MAX(c.id) FROM ContractForm c where c.contract.id = :contractId GROUP BY c.parent.id, c.contractFormType)";

		TypedQuery<ContractForm> q = getEntityManager().createQuery(query,
				ContractForm.class);
		q.setHint("org.hibernate.cacheable", true);
		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("contractId", contractId);

		return q.getResultList();
	}

	@Transactional(readOnly = true)
	public ContractForm getLatestContractFormByContractIdAndContractFormType(
			long contractId, ContractFormType contractFormType) {
		String query = "SELECT pf FROM ContractForm pf WHERE pf.retired = :retired AND pf.contractFormType = :contractFormType AND pf.id IN ("
				+ " SELECT MAX(ppf.id) FROM ContractForm ppf "
				+ " WHERE ppf.retired = :retired AND ppf.contract.id = :contractId "
				+ " GROUP BY ppf.parent.id ) ";

		TypedQuery<ContractForm> q = getEntityManager().createQuery(query,
				ContractForm.class);
		q.setFirstResult(0);
		q.setMaxResults(1);
		q.setHint("org.hibernate.cacheable", true);
		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("contractId", contractId);
		q.setParameter("contractFormType", contractFormType);

		return q.getSingleResult();
	}

	@Transactional(readOnly = true)
	public List<Contract> listContractsByUser(User user) {
		String query = "SELECT p FROM Contract p, SecurableObjectAcl soa "
				+ " WHERE p.retired = :retired AND soa.retired = :retired AND soa.ownerClass = :ownerClass AND soa.ownerId = :ownerId "
				+ " AND soa.securableObject.objectClass = :objectClass AND soa.securableObject.objectId = p.id ";

		TypedQuery<Contract> q = getEntityManager().createQuery(query,
				Contract.class);

		q.setHint("org.hibernate.cacheable", true);
		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("ownerClass", User.class);
		q.setParameter("ownerId", user.getId());
		q.setParameter("objectClass", Contract.class);

		return q.getResultList();
	}

	@Transactional(readOnly = true)
	public PagedList<Contract> listPagedContractMetaDatasByUserAndSearchCriteriaAndContractStatusFilter(
			User user, int start, int limit,
			List<ContractSearchCriteria> searchCriterias,
			ContractStatusEnum filter) {
		PagedList<Contract> pagedList = new PagedList<Contract>();
		pagedList.setStart(start);
		pagedList.setLimit(limit);

		String xpathWhereClause = "";

		if (searchCriterias != null) {
			List<String> xPathCriterias = contractSearchCriteriaResolver(searchCriterias);

			int c = xPathCriterias.size();

			int i = 0;

			for (String xc : xPathCriterias) {
				xpathWhereClause += xc;

				if (i != c - 1) {
					xpathWhereClause += " OR ";
				}

				i++;
			}
		}

		logger.debug("xpathWhereClause: " + xpathWhereClause);

		String queryTotal = " SELECT COUNT(*) FROM Contract c "
				+ (filter != null ? ", ContractStatus cs" : "")
				+ " WHERE c.retired = :retired "
				+ (filter != null ? " AND cs.id = (SELECT MAX(cs.id) FROM ContractStatus cs WHERE cs.contract.id = c.id AND cs.contractStatus = :contractStatus) "
						: "")
				+ ((searchCriterias != null) ? " AND  (" + xpathWhereClause
						+ ") " : "");

		Query tq = getEntityManager().createQuery(queryTotal);
		tq.setHint("org.hibernate.cacheable", true);
		tq.setParameter("retired", Boolean.FALSE);
		if (filter != null) {
			tq.setParameter("contractStatus", filter);
		}

		long total = (Long) tq.getSingleResult();

		pagedList.setTotal(total);

		String query = " SELECT c FROM Contract c "
				+ (filter != null ? ", ContractStatus cs" : "")
				+ " WHERE c.retired = :retired "
				+ (filter != null ? " AND cs.id = (SELECT MAX(cs.id) FROM ContractStatus cs WHERE cs.contract.id = c.id AND cs.contractStatus = :contractStatus) "
						: "")
				+ ((searchCriterias != null) ? " AND  (" + xpathWhereClause
						+ ") " : "") + " ORDER BY c.id DESC";

		TypedQuery<Contract> q = getEntityManager().createQuery(query,
				Contract.class);
		q.setFirstResult(start);
		q.setMaxResults(limit);
		q.setHint("org.hibernate.cacheable", true);
		q.setParameter("retired", Boolean.FALSE);
		if (filter != null) {
			q.setParameter("contractStatus", filter);
		}

		pagedList.setList(q.getResultList());
		return pagedList;
	}

	@Transactional(readOnly = true)
	public PagedList<Contract> listPagedContractMetaDatasByUserAndProtocolId(
			User user, int start, int limit, long protocolId) {
		PagedList<Contract> pagedList = new PagedList<Contract>();
		pagedList.setStart(start);
		pagedList.setLimit(limit);

		String queryTotal = " SELECT COUNT(*) FROM Contract c "
				+ " WHERE c.retired = :retired "
				+ " AND c.protocol.id = :protocolId ";

		Query tq = getEntityManager().createQuery(queryTotal);
		tq.setHint("org.hibernate.cacheable", true);
		tq.setParameter("retired", Boolean.FALSE);
		tq.setParameter("protocolId", protocolId);

		long total = (Long) tq.getSingleResult();

		pagedList.setTotal(total);

		String query = " SELECT c FROM Contract c "
				+ " WHERE c.retired = :retired "
				+ " AND c.protocol.id = :protocolId " + " ORDER BY c.id DESC";

		TypedQuery<Contract> q = getEntityManager().createQuery(query,
				Contract.class);
		q.setFirstResult(start);
		q.setMaxResults(limit);
		q.setHint("org.hibernate.cacheable", true);
		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("protocolId", protocolId);

		pagedList.setList(q.getResultList());
		return pagedList;
	}

	@Transactional(readOnly = true)
	public PagedList<Contract> listPagedContractMetaDatasByUserAndContractId(
			User user, int start, int limit, long contractId) {
		PagedList<Contract> pagedList = new PagedList<Contract>();
		pagedList.setStart(start);
		pagedList.setLimit(limit);

		String queryTotal = " SELECT COUNT(*) FROM contract c "
				+ " WHERE c.retired = :retired "
				+ " AND c.id IN (SELECT ro.related_object_id FROM related_object ro WHERE ro.object_id = :contractId AND ro.object_type = 'Contract' AND ro.related_object_type = 'Contract' AND ro.retired = :retired GROUP BY ro.related_object_id UNION "
				+ " SELECT rlc.object_id FROM related_object rlc WHERE rlc.related_object_id = :contractId AND rlc.object_type = 'Contract' AND rlc.related_object_type = 'Contract' AND rlc.retired = :retired GROUP BY rlc.object_id) ";

		Query tq = getEntityManager().createNativeQuery(queryTotal);
		// tq.setHint("org.hibernate.cacheable", true);
		tq.setParameter("retired", Boolean.FALSE);
		tq.setParameter("contractId", contractId);

		long total = Long.valueOf(tq.getSingleResult().toString());

		pagedList.setTotal(total);

		String query = " SELECT c.* FROM contract c "
				+ " WHERE c.retired = :retired "
				+ " AND c.id IN (SELECT ro.related_object_id FROM related_object ro WHERE ro.object_id = :contractId AND ro.object_type = 'Contract' AND ro.related_object_type = 'Contract' AND ro.retired = :retired GROUP BY ro.related_object_id UNION "
				+ " SELECT rlc.object_id FROM related_object rlc WHERE rlc.related_object_id = :contractId AND rlc.object_type = 'Contract' AND rlc.related_object_type = 'Contract' AND rlc.retired = :retired GROUP BY rlc.object_id) ";

		TypedQuery<Contract> q = (TypedQuery<Contract>) getEntityManager()
				.createNativeQuery(query, Contract.class);
		q.setFirstResult(start);
		q.setMaxResults(limit);
		// q.setHint("org.hibernate.cacheable", true);
		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("contractId", contractId);

		pagedList.setList(q.getResultList());
		return pagedList;
	}

	@Transactional(readOnly = true)
	public PagedList<Protocol> listPagedProtocolMetaDatasByUserAndContractId(
			User user, int start, int limit, long contractId) {
		PagedList<Protocol> pagedList = new PagedList<Protocol>();
		pagedList.setStart(start);
		pagedList.setLimit(limit);

		String queryTotal = " SELECT COUNT(*) FROM protocol p "
				+ " WHERE p.retired = :retired "
				+ " AND p.id IN (SELECT ro.related_object_id FROM related_object ro WHERE ro.object_id = :contractId AND ro.object_type = 'Contract' AND ro.related_object_type = 'Protocol' AND ro.retired = :retired GROUP BY ro.related_object_id UNION "
				+ " SELECT rlc.object_id FROM related_object rlc WHERE rlc.related_object_id = :contractId AND rlc.related_object_type = 'Contract' AND rlc.object_type = 'Protocol' AND rlc.retired = :retired GROUP BY rlc.object_id) ";

		Query tq = getEntityManager().createNativeQuery(queryTotal);
		// tq.setHint("org.hibernate.cacheable", true);
		tq.setParameter("retired", Boolean.FALSE);
		tq.setParameter("contractId", contractId);

		long total = Long.valueOf(tq.getSingleResult().toString());

		pagedList.setTotal(total);

		String query = " SELECT p.* FROM protocol p "
				+ " WHERE p.retired = :retired "
				+ " AND p.id IN (SELECT ro.related_object_id FROM related_object ro WHERE ro.object_id = :contractId AND ro.object_type = 'Contract' AND ro.related_object_type = 'Protocol' AND ro.retired = :retired GROUP BY ro.related_object_id UNION "
				+ " SELECT rlc.object_id FROM related_object rlc WHERE rlc.related_object_id = :contractId AND rlc.related_object_type = 'Contract' AND rlc.object_type = 'Protocol' AND rlc.retired = :retired GROUP BY rlc.object_id) ";

		TypedQuery<Protocol> q = (TypedQuery<Protocol>) getEntityManager()
				.createNativeQuery(query, Protocol.class);
		q.setFirstResult(start);
		q.setMaxResults(limit);
		// q.setHint("org.hibernate.cacheable", true);
		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("contractId", contractId);

		pagedList.setList(q.getResultList());
		return pagedList;
	}

	@Transactional(readOnly = true)
	public List<ContractFormXmlData> listLastestContractXmlDatas() {

		String query = "SELECT pfxd FROM ContractFormXmlData pfxd WHERE pfxd.id IN ("
				+ " SELECT MAX(pfxdd.id) FROM ContractFormXmlData pfxdd "
				+ " WHERE pfxdd.retired = :retired"
				+ " AND pfxdd.contractForm.retired = :retired"
				+ " AND pfxdd.contractForm.contract.retired = :retired "
				+ " AND pfxdd.contractFormXmlDataType = :contractFormXmlDataType "
				+ " GROUP BY pfxdd.contractForm.contract.id, pfxdd.parent) ";

		TypedQuery<ContractFormXmlData> q = getEntityManager().createQuery(
				query, ContractFormXmlData.class);
		q.setHint("org.hibernate.cacheable", true);
		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("contractFormXmlDataType",
				ContractFormXmlDataType.CONTRACT);

		return q.getResultList();
	}

	/**
	 * @@TODO this needs to be reviewed
	 * @param contractId
	 * @return
	 */
	@Transactional(readOnly = true)
	public ContractFormXmlData getLastestContractXmlDataByContractId(
			long contractId) {

		String query = "SELECT pfxd FROM ContractFormXmlData pfxd "
				+ " WHERE pfxd.retired = :retired"
				+ " AND pfxd.contractForm.retired = :retired"
				+ " AND pfxd.contractForm.contract.retired = :retired "
				+ " AND pfxd.contractFormXmlDataType = :contractFormXmlDataType "
				+ " AND pfxd.contractForm.contract.id = :contractId"
				+ " ORDER BY pfxd.id DESC";

		TypedQuery<ContractFormXmlData> q = getEntityManager().createQuery(
				query, ContractFormXmlData.class);

		q.setFirstResult(0);
		q.setMaxResults(1);
		q.setHint("org.hibernate.cacheable", true);
		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("contractFormXmlDataType",
				ContractFormXmlDataType.CONTRACT);
		q.setParameter("contractId", contractId);

		return q.getSingleResult();
	}

	@Transactional(readOnly = true)
	public ContractStatus getLatestContractStatusByContractId(long contractId) {
		String query = "SELECT ps FROM ContractStatus ps "
				+ " WHERE ps.contract.id = :contractId AND ps.retired = :retired ORDER BY ps.id DESC";

		TypedQuery<ContractStatus> q = getEntityManager().createQuery(query,
				ContractStatus.class);
		q.setFirstResult(0);
		q.setMaxResults(1);
		q.setHint("org.hibernate.cacheable", true);
		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("contractId", contractId);

		return q.getSingleResult();
	}

	@Transactional(readOnly = true)
	public Contract getContractByProtocolId(long protocolId) {
		String query = "SELECT c FROM Contract c "
				+ " WHERE c.protocol.id = :protocolId AND c.retired = :retired";

		TypedQuery<Contract> q = getEntityManager().createQuery(query,
				Contract.class);
		q.setFirstResult(0);
		q.setMaxResults(1);
		q.setHint("org.hibernate.cacheable", true);
		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("protocolId", protocolId);

		return q.getSingleResult();
	}

	@Transactional
	public void disableIdentyInsert(long id, int concurrent_version,
			Date created, String meta_data_xml, int protocol_id,
			String contract_identifier) {
		String query1 = "SELECT contract_identifier FROM [clara_training].[dbo].[contract] WHERE id = :id";
		Query qry1 = getEntityManager().createNativeQuery(query1);
		qry1.setParameter("id", id);

		String query1Result = "";
		try {
			query1Result = (String) qry1.getSingleResult();
		} catch (Exception e) {
			query1Result = "";
		}

		// contract existed
		if (!query1Result.equals("")) {
			logger.debug("update contract...");
			String query2 = "UPDATE [clara_training].[dbo].[contract] set meta_data_xml = :meta_data_xml, created =:created,concurrent_version=:concurrent_version"
					+ "  where id =:id";
			Query qry2 = getEntityManager().createNativeQuery(query2);
			qry2.setParameter("id", id);
			qry2.setParameter("concurrent_version", concurrent_version);
			qry2.setParameter("created", created);
			qry2.setParameter("meta_data_xml", meta_data_xml);
			qry2.executeUpdate();
		} else {
			logger.debug("creating contract...");
			String query ="";
			if(protocol_id==0){
				 query = "SET IDENTITY_INSERT [clara_training].[dbo].[contract]  ON; "
						+ " insert into [clara_training].[dbo].[contract] (id, concurrent_version, retired, created, locked, meta_data_xml, contract_identifier) values (:id, :concurrent_version, :retired, :created, :locked, :meta_data_xml, :contract_identifier); "
						+ " SET IDENTITY_INSERT  [clara_training].[dbo].[contract]  OFF;";
			}
			else{
				 query = "SET IDENTITY_INSERT [clara_training].[dbo].[contract]  ON; "
						+ " insert into [clara_training].[dbo].[contract] (id, concurrent_version, retired, created, locked, meta_data_xml, protocol_id, contract_identifier) values (:id, :concurrent_version, :retired, :created, :locked, :meta_data_xml, :protocol_id, :contract_identifier); "
						+ " SET IDENTITY_INSERT  [clara_training].[dbo].[contract]  OFF;";
			}
			
			
			Query qry = getEntityManager().createNativeQuery(query);
			qry.setParameter("id", id);
			qry.setParameter("concurrent_version", concurrent_version);
			qry.setParameter("retired", false);
			qry.setParameter("locked", false);
			qry.setParameter("created", created);
			qry.setParameter("meta_data_xml", meta_data_xml);
			if(protocol_id!=0){
			qry.setParameter("protocol_id", protocol_id);
			}
			qry.setParameter("contract_identifier", contract_identifier);

			qry.executeUpdate();
		}

	}
}
