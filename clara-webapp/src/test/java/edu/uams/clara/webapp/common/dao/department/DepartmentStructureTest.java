package edu.uams.clara.webapp.common.dao.department;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.hibernate.Hibernate;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.uams.clara.webapp.common.domain.department.College;
import edu.uams.clara.webapp.common.domain.department.Department;
import edu.uams.clara.webapp.common.domain.department.SubDepartment;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration( { "file:src/test/java/edu/uams/clara/webapp/common/dao/department/DepartmentStructureTest-context.xml" })
public class DepartmentStructureTest {

	private final static Logger logger = LoggerFactory
	.getLogger(DepartmentStructureTest.class);
	
	private ResourceLoader resourceLoader;
	
	private SubDepartmentDao subDepartmentDao;
	
	private DepartmentDao departmentDao;
	
	private CollegeDao collegeDao;
	
	private Map<String, String> sapdepts = new HashMap<String, String>();
	
	private Map<String, Set<String>> orgnots = new HashMap<String, Set<String>>();
	
	//@Test
	public void generateCollege() throws JsonGenerationException, JsonMappingException, IOException{
		List<College> colleges = collegeDao.findAll();
		Set<Department> departments = null;
		Set<SubDepartment> subDepartments = null;
		
		for(College c:colleges){
			departments = c.getDepartments();
			
			logger.debug("College: " + c.getName() + " has" + departments.size());
			for(Department d:departments){
				subDepartments = d.getSubDepartments();
				logger.debug("-----Department: " + d.getName() + " has" + subDepartments.size());
			}			
			
		}
		
		ObjectMapper objectMapper = new ObjectMapper();
		
		FileOutputStream fos = new FileOutputStream("src/main/webapp/static/json/departments.json"); 
		OutputStreamWriter out = new OutputStreamWriter(fos, "UTF-8");
		
		String departmentsJson = objectMapper.writeValueAsString(colleges);
		
		try {
		      out.write(departmentsJson);
		    }
		    finally {
		      out.close();
		    }
		//for(College college:)
	}
	
	//@Test
	public void testCollege(){
		College college = collegeDao.findBySapCode("50000278");
		
		logger.debug("college: " + college.getName());
		
		for(Department department:college.getDepartments()){
			logger.debug("department: " + department.getName());
			for(SubDepartment subDepartment:department.getSubDepartments()){
				logger.debug("subDepartment: " + subDepartment.getName());
				
			}
		}
	}
	
	@Test
	public void loadDepartments(){
		List<Object[]> deptMapList = departmentDao.findALLDeptCodeMap();
		List<Object[]> deptDescList = departmentDao.getAllDeptDescription();
		for(int i=0;i<deptDescList.size();i++){
			Object[] deptDescObj = deptDescList.get(i);
			if(deptDescObj[0]!=null&&deptDescObj[1]!=null){
				String code = (String)deptDescObj[0];
				String desc = (String)deptDescObj[1];
				sapdepts.put(code.trim(), desc.trim());
			}
		}
		/**
		 * Load data from txt files into memory
		 */
		/*Resource orgnot = resourceLoader.getResource("import data/orgnot.txt");
		Resource sapdept = resourceLoader.getResource("import data/sapdept.txt");
		
		InputStream in = sapdept.getInputStream();
		
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		
		String strLine = null;
		String[] a = null;
		while((strLine = br.readLine()) != null){
			a = strLine.split("\\t");
			//logger.debug("sapCode: " + a[0]);
			sapdepts.put(a[0].trim(), a[1].trim());
		}
			
		in.close();
		
		in = orgnot.getInputStream();
		
		br = new BufferedReader(new InputStreamReader(in));*/
		
		//set for retired judge
		List<String> allCodeList = new ArrayList<String>();
		
		/*while((strLine = br.readLine()) != null){
			a = strLine.split("\\t");
			
			Set<String> p = orgnots.get(a[0].trim());
			allCodeList.add(a[1].trim());
			if(p == null){
				p = new TreeSet<String>();
				
				orgnots.put(a[0].trim(), p);

			}
			
			p.add(a[1].trim());
		}
		
		in.close();*/
		
		for(int i=0;i<deptMapList.size();i++){
			Object[] deptMapObj = deptMapList.get(i);
			if(deptMapObj[0]!=null&&deptMapObj[1]!=null){
				String cCode = (String)deptMapObj[0];
				String dCode = (String)deptMapObj[1];
				Set<String> p = orgnots.get(cCode.trim());
				allCodeList.add(dCode.trim());
				if(p == null){
					p = new TreeSet<String>();
					
					orgnots.put(cCode.trim(), p);

				}
				p.add(dCode.trim());
			}
			
		}
		
		/**
		 * every college is a child of 50000001 UAMS
		 */
		
		String parent = "50000001";
		String name = null;

		College college = null;
		Department department = null;
		SubDepartment subDepartment = null;
		
		for(String cCode:orgnots.get(parent)){
			name = sapdepts.get(cCode);
			logger.debug("college: " + cCode + "; name: " + name);
			college = collegeDao.findBySapCode(cCode);
			
			if(college == null){
				college = new College();				
			}
			
			//doesn't matter... do a update... we are assuming that the code will never change...
			//but there are cases where the department name is changed, or a new code is added...
			college.setName(name);
			college.setSapCode(cCode);
			college.setRetired(Boolean.FALSE);
			if(name==null){
				college.setRetired(Boolean.TRUE);
			}
			college = collegeDao.saveOrUpdate(college);
			
			//find all departments under this college...
			if(orgnots.get(cCode) == null){
				continue;
			}
			
			for(String dCode:orgnots.get(cCode)){
				name = sapdepts.get(dCode);
				logger.debug("-----department: " + dCode + "; name: " + name);
				department = departmentDao.findBySapCode(dCode);
				
				if(department == null){
					department = new Department();				
				}
				
				department.setCollege(college);
				department.setName(name);
				department.setSapCode(dCode);
				department.setRetired(Boolean.FALSE);
				if(name==null){
					department.setRetired(Boolean.TRUE);
				}
				department = departmentDao.saveOrUpdate(department);
				
				//find all departments under this college...
				if(orgnots.get(dCode) == null){
					continue;
				}
				for(String sCode:orgnots.get(dCode)){
					name = sapdepts.get(sCode);
					logger.debug("----------subDepartment: " + sCode + "; name: " + name);
					subDepartment = subDepartmentDao.findBySapCode(sCode);
					
					if(subDepartment == null){
						subDepartment = new SubDepartment();				
					}
					
					subDepartment.setDepartment(department);
					subDepartment.setName(name);
					subDepartment.setSapCode(sCode);
					subDepartment.setRetired(Boolean.FALSE);
					if(name==null){
						subDepartment.setRetired(Boolean.TRUE);
					}
					subDepartment = subDepartmentDao.saveOrUpdate(subDepartment);
				}
			}
		}
		
		//if the data in table is not available in text document, retire it
		List<College> collegeList = collegeDao.findAll();
		List<Department> departmentList = departmentDao.findAll();
		List<SubDepartment> subDepartmentList = subDepartmentDao.findAll();
		
		for(int collegeLen=0; collegeLen<collegeList.size();collegeLen++){
			if(allCodeList.contains(collegeList.get(collegeLen).getSapCode()))
				continue;
			
			collegeList.get(collegeLen).setRetired(true);
			collegeDao.saveOrUpdate(collegeList.get(collegeLen));
			logger.debug("retired sap is: "+collegeList.get(collegeLen).getSapCode());
		}
		
		for(int departmentlen=0; departmentlen<departmentList.size();departmentlen++){
			if(allCodeList.contains(departmentList.get(departmentlen).getSapCode()))
				continue;
			
			departmentList.get(departmentlen).setRetired(true);
			departmentDao.saveOrUpdate(departmentList.get(departmentlen));
			logger.debug("retired sap is: "+departmentList.get(departmentlen).getSapCode());
		}
		
		for(int subDepartmentLen=0; subDepartmentLen<subDepartmentList.size();subDepartmentLen++){
			if(allCodeList.contains(subDepartmentList.get(subDepartmentLen).getSapCode()))
				continue;
			
			subDepartmentList.get(subDepartmentLen).setRetired(true);
			subDepartmentDao.saveOrUpdate(subDepartmentList.get(subDepartmentLen));
			logger.debug("retired sap is: "+subDepartmentList.get(subDepartmentLen).getSapCode());
		}
		
		
	}

	@Autowired(required=true)
	public void setSubDepartmentDao(SubDepartmentDao subDepartmentDao) {
		this.subDepartmentDao = subDepartmentDao;
	}

	public SubDepartmentDao getSubDepartmentDao() {
		return subDepartmentDao;
	}

	@Autowired(required=true)
	public void setDepartmentDao(DepartmentDao departmentDao) {
		this.departmentDao = departmentDao;
	}

	public DepartmentDao getDepartmentDao() {
		return departmentDao;
	}

	@Autowired(required=true)
	public void setCollegeDao(CollegeDao collegeDao) {
		this.collegeDao = collegeDao;
	}

	public CollegeDao getCollegeDao() {
		return collegeDao;
	}
	
	@Autowired(required=true)
	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}

	public ResourceLoader getResourceLoader() {
		return resourceLoader;
	}
	
	
}
