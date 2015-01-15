package edu.uams.clara.integration.incoming.sap.service;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import edu.uams.clara.integration.incoming.sap.dao.SalaryDao;
import edu.uams.clara.webapp.common.dao.usercontext.PersonDao;
import edu.uams.clara.webapp.common.domain.usercontext.Person;

@Service
public class ImportSalaryService {

	private SalaryDao salaryDao;
	private PersonDao personDao;

	private final static Logger logger = LoggerFactory
			.getLogger(ImportSalaryService.class);

	public void addandUpateSalaryForPerson() {
		List<Person> persons = new ArrayList<Person>();
		persons = personDao.findAll();
		for (Person person : persons) {
			if (person.getSap() != null) {
				if (person.getSap().isEmpty())
					continue;

				person.setAnnualSalary(salaryDao.findSalaryBySap(person
						.getSap()));
				personDao.saveOrUpdate(person);

				/*
				 * if(salaryDao.findSalaryBySap(person.getSap()).equals(""))
				 * logger
				 * .debug(person.getFirstname()+" "+person.getLastname()+" "
				 * +person.getSap());
				 */

			}
		}
	}

	public SalaryDao getSalaryDao() {
		return salaryDao;
	}

	@Autowired(required = true)
	public void setSalaryDao(SalaryDao salaryDao) {
		this.salaryDao = salaryDao;
	}

	public PersonDao getPersonDao() {
		return personDao;
	}

	@Autowired(required = true)
	public void setPersonDao(PersonDao personDao) {
		this.personDao = personDao;
	}
}
