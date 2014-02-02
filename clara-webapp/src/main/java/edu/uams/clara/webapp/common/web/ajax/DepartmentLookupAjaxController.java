package edu.uams.clara.webapp.common.web.ajax;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import edu.uams.clara.webapp.common.dao.department.CollegeDao;
import edu.uams.clara.webapp.common.dao.department.DepartmentDao;
import edu.uams.clara.webapp.common.dao.department.SubDepartmentDao;
import edu.uams.clara.webapp.common.domain.department.College;
import edu.uams.clara.webapp.common.domain.department.Department;
import edu.uams.clara.webapp.common.domain.department.SubDepartment;

@Controller
public class DepartmentLookupAjaxController {

	private final static Logger logger = LoggerFactory
	.getLogger(DepartmentLookupAjaxController.class);
	
	private CollegeDao collegeDao;
	
	private DepartmentDao departmentDao;
	
	private SubDepartmentDao subDepartmentDao;
	
	@RequestMapping(value = "/ajax/colleges/list", method = RequestMethod.GET)
	public @ResponseBody List<College> getColleges(){
				
		return collegeDao.listAllOrderByName();
		
	}
	
	@RequestMapping(value = "/ajax/colleges/{collegeId}/departments/list", method = RequestMethod.GET)
	public @ResponseBody List<Department> getDepartmentsByCollegeId(@PathVariable("collegeId") long collegeId){
				
		College college = collegeDao.findById(collegeId);
		
		List<Department> departments = new ArrayList<Department>();
		if(college == null){
			return departments;
		}
		
		for(Department d:college.getDepartments()){
			departments.add(d);
		}
		
		Collections.sort(departments, new Comparator<Department>(){
			@Override public int compare(Department d1, Department d2){
				return d1.getName().compareTo(d2.getName());
			}
		});
		
		return departments;
		
	}
	
	@RequestMapping(value = "/ajax/colleges/{collegeId}/departments/{departmentId}/sub-departments/list", method = RequestMethod.GET)
	public @ResponseBody List<SubDepartment> getDepartmentsByCollegeId(@PathVariable("collegeId") long collegeId, @PathVariable("departmentId") long departmentId){
		
		Department department = departmentDao.findById(departmentId);
		
		List<SubDepartment> subDepartments = new ArrayList<SubDepartment>();
		if(department == null){
			return subDepartments;
		}
		
		for(SubDepartment sd:department.getSubDepartments()){
			subDepartments.add(sd);
		}
		
		Collections.sort(subDepartments, new Comparator<SubDepartment>(){
			@Override public int compare(SubDepartment d1, SubDepartment d2){
				return d1.getName().compareTo(d2.getName());
			}
		});
		
		return subDepartments;
		
	}

	@Autowired(required=true)
	public void setCollegeDao(CollegeDao collegeDao) {
		this.collegeDao = collegeDao;
	}

	public CollegeDao getCollegeDao() {
		return collegeDao;
	}

	@Autowired(required=true)
	public void setDepartmentDao(DepartmentDao departmentDao) {
		this.departmentDao = departmentDao;
	}

	public DepartmentDao getDepartmentDao() {
		return departmentDao;
	}

	@Autowired(required=true)
	public void setSubDepartmentDao(SubDepartmentDao subDepartmentDao) {
		this.subDepartmentDao = subDepartmentDao;
	}

	public SubDepartmentDao getSubDepartmentDao() {
		return subDepartmentDao;
	}
}
