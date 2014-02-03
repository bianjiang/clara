package edu.uams.clara.webapp.protocol.businesslogic.irb.agenda;

import java.math.BigInteger;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import edu.uams.clara.integration.outgoing.webchart.WebCharUpdateTest;
import edu.uams.clara.webapp.common.dao.department.CollegeDao;
import edu.uams.clara.webapp.common.dao.department.DepartmentDao;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "file:src/test/java/edu/uams/clara/webapp/protocol/businesslogic/irb/agenda/GenerateReportForCompltedAgendaTest.xml" })
public class GenerateReportForCompltedAgendaTest {
	private final static Logger logger = LoggerFactory
			.getLogger(WebCharUpdateTest.class);
	private DepartmentDao departmentDao;
	private CollegeDao collegeDao;
	private EntityManager em;

	@Test
	public void generateReportForCompltedAgenda() {
		String qry = "select pf.protocol_id as protocolId, "
				+ "agenda_item_category, pf.protocol_form_type, pf.meta_data_xml.value('(//staffs/staff/user[roles/role[fn:contains(fn:upper-case(.),\"PRINCIPAL INVESTIGATOR\")]]/lastname)[1]','varchar(20)') + ', ' + pf.meta_data_xml.value('(//staffs/staff/user[roles/role[fn:contains(fn:upper-case(.),\"PRINCIPAL INVESTIGATOR\")]]/firstname)[1]','varchar(20)') as fullname"
				+ ", p.department,ag.date" +
				" from agenda_item ai, protocol_form pf, user_account ua, person p, agenda as ag where ai.retired = 0 "
				+ " and pf.retired = 0"
				+ " and ai.protocol_form_id = pf.id"
				+ " and ai.agenda_item_status <> 'REMOVED'"
				+ " and ai.agenda_id in ("
				+ " select id from agenda where retired = 0"
				+ " and id in (select agenda_id from agenda_status where retired = 0 and agenda_status in ('MEETING_ADJOURNED_PENDING_IRB_OFFICE_PROCESS', 'MEETING_CLOSED')))"
				+ " and ua.id=pf.meta_data_xml.value('(//staffs/staff/user[roles/role[fn:contains(fn:upper-case(.),\"PRINCIPAL INVESTIGATOR\")]]/@id)[1]','bigint') "
				+ " and ua.retired = 0" + " and p.id=ua.person_id " +
						" and ag.retired =0 " +
						" and ag.id =ai.agenda_id"
				+ " order by ag.date ";

		Query query = em.createNativeQuery(qry);

		List<Object[]> results = (List<Object[]>) query
				.getResultList();

		// get department id and college
		for (Object[] result : results) {
			String deptName = "";
			String collegeName = "";
			BigInteger collegeId = null;
			if (result[4] != null) {
				deptName = (String) result[4];
			}
			if (deptName != null || !deptName.isEmpty()) {
				qry = "select college_id from department where name =:deptName";
				Query deptQuery = em.createNativeQuery(qry);
				deptQuery.setParameter("deptName", deptName);

				try {
					collegeId = (BigInteger) deptQuery.getSingleResult();
				} catch (Exception e) {
					// e.printStackTrace();
				}
				if (collegeId != null) {
					// department existing

					collegeName = collegeDao.findById(collegeId.longValue())
							.getName();
				}
			}
			// output to file
			System.out.println((BigInteger) result[0] + " * "
					+ (String) result[1] + " * "
					+ (String) result[2] + " * "
					+ (String) result[3] + " * " + deptName + " * "
					+ collegeName.toString()+ " * "
					);
		}
	}

	public DepartmentDao getDepartmentDao() {
		return departmentDao;
	}

	@Autowired(required = true)
	public void setDepartmentDao(DepartmentDao departmentDao) {
		this.departmentDao = departmentDao;
	}

	public CollegeDao getCollegeDao() {
		return collegeDao;
	}

	@Autowired(required = true)
	public void setCollegeDao(CollegeDao collegeDao) {
		this.collegeDao = collegeDao;
	}

	public EntityManager getEm() {
		return em;
	}

	@PersistenceContext(unitName = "defaultPersistenceUnit")
	public void setEm(EntityManager em) {
		this.em = em;
	}
}
