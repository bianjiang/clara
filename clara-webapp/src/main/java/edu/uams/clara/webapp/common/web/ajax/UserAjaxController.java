package edu.uams.clara.webapp.common.web.ajax;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.transform.Source;

import org.apache.commons.lang.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.encoding.Md5PasswordEncoder;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import edu.uams.clara.core.util.xml.DomUtils;
import edu.uams.clara.core.util.xml.XmlHandler;
import edu.uams.clara.core.util.xml.XmlHandlerFactory;
import edu.uams.clara.webapp.common.dao.department.CollegeDao;
import edu.uams.clara.webapp.common.dao.department.DepartmentDao;
import edu.uams.clara.webapp.common.dao.department.SubDepartmentDao;
import edu.uams.clara.webapp.common.dao.security.MutexLockDao;
import edu.uams.clara.webapp.common.dao.usercontext.CitiMemberDao;
import edu.uams.clara.webapp.common.dao.usercontext.PersonDao;
import edu.uams.clara.webapp.common.dao.usercontext.RoleDao;
import edu.uams.clara.webapp.common.dao.usercontext.UserCOIDao;
import edu.uams.clara.webapp.common.dao.usercontext.UserDao;
import edu.uams.clara.webapp.common.dao.usercontext.UserRoleDao;
import edu.uams.clara.webapp.common.domain.department.College;
import edu.uams.clara.webapp.common.domain.department.Department;
import edu.uams.clara.webapp.common.domain.department.SubDepartment;
import edu.uams.clara.webapp.common.domain.security.MutexLock;
import edu.uams.clara.webapp.common.domain.usercontext.CitiMember;
import edu.uams.clara.webapp.common.domain.usercontext.Person;
import edu.uams.clara.webapp.common.domain.usercontext.Role;
import edu.uams.clara.webapp.common.domain.usercontext.Role.DepartmentLevel;
import edu.uams.clara.webapp.common.domain.usercontext.User;
import edu.uams.clara.webapp.common.domain.usercontext.User.UserType;
import edu.uams.clara.webapp.common.domain.usercontext.UserRole;
import edu.uams.clara.webapp.common.domain.usercontext.enums.Permission;
import edu.uams.clara.webapp.common.security.MutexLockService;
import edu.uams.clara.webapp.common.service.EmailService;
import edu.uams.clara.webapp.common.service.UserService;
import edu.uams.clara.webapp.common.service.audit.AuditService;
import edu.uams.clara.webapp.common.util.DateFormatUtil;
import edu.uams.clara.webapp.common.util.JsonResponseHelper;
import edu.uams.clara.webapp.common.util.XMLResponseHelper;
import edu.uams.clara.webapp.common.util.response.JsonResponse;
import edu.uams.clara.webapp.contract.dao.contractform.ContractFormDao;
import edu.uams.clara.webapp.contract.domain.contractform.ContractForm;
import edu.uams.clara.webapp.fileserver.dao.UploadedFileDao;
import edu.uams.clara.webapp.fileserver.domain.UploadedFile;
import edu.uams.clara.webapp.fileserver.service.SFTPService;
import edu.uams.clara.webapp.protocol.dao.protocolform.ProtocolFormDao;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolForm;
import edu.uams.clara.webapp.xml.processor.XmlProcessor;

@Controller
public class UserAjaxController {

	private final static Logger logger = LoggerFactory
			.getLogger(UserAjaxController.class);

	private UserService userService;

	private UserDao userDao;

	private PersonDao personDao;

	private RoleDao roleDao;

	private UserRoleDao userRoleDao;

	private CollegeDao collegeDao;

	private DepartmentDao departmentDao;

	private SubDepartmentDao subDepartmentDao;

	private Md5PasswordEncoder md5PasswordEncoder;

	private UploadedFileDao uploadedFileDao;

	private CitiMemberDao citiMemberDao;

	private UserCOIDao userCOIDao;

	private MutexLockDao mutexLockDao;

	private ProtocolFormDao protocolFormDao;

	private ContractFormDao contractFormDao;

	private EmailService emailService;

	private MutexLockService mutexLockService;
	
	private XmlProcessor xmlProcessor;
	
	private AuditService auditService;

	@Value("${application.host}")
	private String applicationHost;

	@RequestMapping(value = "/ajax/users/{userId}/coi/list", method = RequestMethod.GET)
	public @ResponseBody
	JsonResponse listCOI(@PathVariable("userId") Long userId) {
		User u = userDao.findById(userId);
		String sapId = u.getPerson().getSap();
		try {
			return new JsonResponse(false, userCOIDao.getUserCOIBySAP(sapId));
		} catch (Exception ex) {
			return new JsonResponse(true, ex.getMessage(), "", false);
		}
	}

	@RequestMapping(value = "/ajax/users/persons/search", method = RequestMethod.GET)
	public @ResponseBody
	Map<String, List<Person>> search(@RequestParam("keyword") String keyword) {
		List<Person> personList = userService.searchForPersons(keyword);

		Map<String, List<Person>> persons = new HashMap<String, List<Person>>(0);
		persons.put("persons", personList);
		return persons;
	}

	@RequestMapping(value = "/ajax/users/createoffcampususer", method = RequestMethod.POST)
	public @ResponseBody
	JsonResponse createOffCampusUser(@RequestParam("username") String username,
			@RequestParam("firstname") String firstname,
			@RequestParam("lastname") String lastname,
			@RequestParam("department") String department,
			@RequestParam("middlename") String middlename,
			@RequestParam("phone") String phone,
			@RequestParam("email") String email) {
		String password = RandomStringUtils.random(8, true, true);

		try {
			if (personDao.getPersonByUsername(username) != null) {
				return new JsonResponse(
						true,
						"The username already exists!  Please change to other uasername!",
						"", false, null);
			}

			if (personDao.getPersonByName(firstname, lastname, middlename) != null) {
				return new JsonResponse(true, "The user already exists!", "",
						false, null);
			}
			
			if (personDao.getPersonByEmail(email) != null) {
				return new JsonResponse(true, "The user with this email already exists!", "",
						false, null);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}

		Person person = new Person();

		person.setDepartment(department);
		person.setEmail(email);
		person.setFirstname(firstname);
		person.setJobTitle("");
		person.setLastname(lastname);
		person.setMiddlename(middlename);
		person.setSap("");
		person.setUsername(username);
		person.setWorkphone(phone);

		personDao.saveOrUpdate(person);

		User u = new User();
		Person p = personDao.getPersonByUsername(username);

		u.setPerson(p);
		u.setUsername(p.getUsername());
		u.setUserType(UserType.DATABASE_USER);
		u.setAccountNonExpired(true);
		u.setAccountNonLocked(true);
		u.setCredentialsNonExpired(true);
		u.setPassword(md5PasswordEncoder.encodePassword(password, username));
		u.setEnabled(true);
		u.setTrained(false);

		u = userDao.saveOrUpdate(u);

		String emailText = "<html><head><link href=\"/clara-webapp/static/styles/letters.css\" media=\"screen\" type=\"text/css\" rel=\"stylesheet\"/></head><body>";
		emailText += "<div class=\"email-template\">";
		emailText += "<br/>Your account has been created in CLARA by System Admin.<br/><br/>Username: "
				+ username
				+ "<br/>Temporary password: "
				+ password
				+ "<br/><br/>You can click the following link to access CLARA and reset password:<br/><a href=\""
				+ applicationHost + "\">CLARA</a>";
		emailText += "</div></body></html>";

		List<String> mailTo = new ArrayList<String>();
		mailTo.add(email);

		String subject = "New CLARA Account";

		emailService.sendEmail(emailText, mailTo, null, subject, null);

		return new JsonResponse(false, u);
	}

	@RequestMapping(value = "/ajax/users/editoffcampususer", method = RequestMethod.POST)
	public @ResponseBody
	JsonResponse editOffCampusUser(
			@RequestParam("id") long userId,
			@RequestParam(value = "firstname", required = false) String firstname,
			@RequestParam(value = "lastname", required = false) String lastname,
			@RequestParam(value = "middlename", required = false) String middlename,
			@RequestParam(value = "phone", required = false) String phone,
			@RequestParam(value = "email", required = false) String email,
			@RequestParam(value = "department", required = false) String department,
			@RequestParam(value = "jobtitle", required = false) String jobtitle) {
		User user = null;

		try {
			user = userDao.findById(userId);
			if (user == null) {
				return new JsonResponse(true, "The user does not exist!", "",
						false, null);
			} else {
				Person person = user.getPerson();

				person.setFirstname(firstname);
				person.setMiddlename(middlename);
				person.setLastname(lastname);
				person.setWorkphone(phone);
				person.setEmail(email);
				person.setDepartment(department);
				person.setJobTitle(jobtitle);

				personDao.saveOrUpdate(person);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return new JsonResponse(true, "Failed to edit user information!",
					"", false, null);
		}

		return new JsonResponse(false, user);
	}

	@RequestMapping(value = "/ajax/users/resetpassword", method = RequestMethod.POST)
	public @ResponseBody
	JsonResponse resetPassword(@RequestParam("username") String username) {
		User user = null;

		try {
			user = userDao.getUserByUsername(username);
			if (user == null) {
				return new JsonResponse(true, "The user does not exist!", "",
						false, null);
			} else {
				if (!user.getUserType().equals(UserType.DATABASE_USER)) {
					return new JsonResponse(
							true,
							"Since you are UAMS employee, you cannot reset the password!",
							"", false, null);
				}

				String email = user.getPerson().getEmail();

				String password = RandomStringUtils.random(8, true, true);

				user.setPassword(md5PasswordEncoder.encodePassword(password,
						user.getUsername()));

				user = userDao.saveOrUpdate(user);

				String emailText = "<html><head><link href=\"/clara-webapp/static/styles/letters.css\" media=\"screen\" type=\"text/css\" rel=\"stylesheet\"/></head><body>";
				emailText += "<div class=\"email-template\">";
				emailText += "<br/>Your password has been reset.<br/><br/>Username: "
						+ username
						+ "<br/>Temporary password: "
						+ password
						+ "<br/><br/>You can click the following link to access CLARA:<br/><a href=\"https://clara.uams.edu\">CLARA</a>";
				emailText += "</div></body></html>";

				List<String> mailTo = new ArrayList<String>();
				mailTo.add(email);

				String subject = "Reset Password";

				emailService.sendEmail(emailText, mailTo, null, subject, null);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return new JsonResponse(true, "Failed to edit user information!",
					"", false, null);
		}

		return new JsonResponse(false, user);
	}

	@RequestMapping(value = "/ajax/users/{userId}/profile", method = RequestMethod.GET, produces = "application/xml")
	public @ResponseBody
	Source getUserProfile(@PathVariable("userId") long userId) {
		try {
			User u = userDao.findById(userId);

			return XMLResponseHelper
					.newDataResponseStub((u.getProfile() != null) ? u
							.getProfile() : "");
		} catch (Exception ex) {
			ex.printStackTrace();
			logger.error("failed to load user profile", ex);

			return XMLResponseHelper
					.newErrorResponseStub("Failed to load the user's CLARA profile (maybe because the user is not a CLARA user). Please click on \"Create Account\" to active this user's CLARA account");
		}
	}

	@RequestMapping(value = "/ajax/users/{userId}/saveuserprofile", method = RequestMethod.POST)
	public @ResponseBody
	JsonResponse saveUserProfile(@PathVariable("userId") long userId,
			@RequestParam("profile") String profile) {
		User currentUser = (User) SecurityContextHolder.getContext().getAuthentication()
				.getPrincipal();
		
		User u = new User();
		try {
			u = userDao.findById(userId);
			
			if (u.getProfile() != null && !u.getProfile().isEmpty()) {
				XmlHandler xmlHandler = XmlHandlerFactory.newXmlHandler();
				
				String isEpicNotified = xmlHandler.getSingleStringValueByXPath(u.getProfile(), "/metadata/is-epic-notified");
				
				if (!isEpicNotified.isEmpty()) {
					profile = xmlProcessor.replaceOrAddNodeValueByPath("/metadata/is-epic-notified", profile, isEpicNotified);
				}
			}
			
			u.setProfile(profile);
			
			auditService.auditEvent(AuditService.AuditEvent.UPDATE_USER_PROFILE.toString(), "UserId: " + userId + "; Actor User Id: " + currentUser.getId() + "", profile);

			return new JsonResponse(false, "", "", false,
					userDao.saveOrUpdate(u));
		} catch (Exception ex) {
			ex.printStackTrace();

			return new JsonResponse(true, "Failed to save profile!", "", false,
					null);
		}
	}

	@RequestMapping(value = "/ajax/users/{userId}/citimember", method = RequestMethod.GET)
	public @ResponseBody
	JsonResponse getUserCitiTraining(@PathVariable("userId") long userId) {

		// String userName = u.getUsername();

		try {
			User u = userDao.findById(userId);
			List<CitiMember> citiMemberLst = citiMemberDao
					.listCitiMemberByUser(u);

			return JsonResponseHelper.newDataResponseStub(citiMemberLst);
		} catch (Exception ex) {
			// ex.printStackTrace();

			return JsonResponseHelper
					.newErrorResponseStub("Failed to get Citi Training information!");
		}
	}

	@RequestMapping(value = "/ajax/users/validate-training", method = RequestMethod.POST, produces = "application/json")
	public @ResponseBody
	JsonResponse getUserCitiTraining(
			@RequestParam("userList[]") List<String> userList) {
		Date now = new Date();

		DateFormat df = new SimpleDateFormat("MM/dd/yyyy");

		Map<String, Boolean> citiMap = new HashMap<String, Boolean>();
		logger.debug("/ajax/users/get-metadata: userList.size() = "
				+ userList.size());
		XmlHandler xmlHanlder = null;
		
		try {
			xmlHanlder = XmlHandlerFactory.newXmlHandler();
		} catch (Exception e) {
			
		}
		
		for (String userId : userList) {
			try {
				User u = userDao.findById(Long.parseLong(userId));
				// String userName = u.getUsername();

				boolean valid = false;
				
				String citiCompleted = "";
				
				if (u.getProfile() != null && !u.getProfile().isEmpty()) {
					citiCompleted = xmlHanlder.getSingleStringValueByXPath(u.getProfile(), "/metadata/citi-training-complete");
				}
				
				if (citiCompleted.equals("true")) {
					valid = true;
				} else {
					List<CitiMember> citiMembers = citiMemberDao
							.listCitiMemberByUser(u);
					for (CitiMember citiMember : citiMembers) {

						try {
							Date expireDate = df.parse(citiMember
									.getDateCompletionExpires());
							if (!expireDate.before(now)) {
								valid = true;
							}
						} catch (ParseException e) {
							// TODO Auto-generated catch block
							//e.printStackTrace();
						}

					}
				}
				
				citiMap.put(userId, valid);
			} catch (Exception ex) {
				// userId can be 0...

			}
		}

		return JsonResponseHelper.newDataResponseStub(citiMap);
	}

	@RequestMapping(value = "/ajax/users/changepassword", method = RequestMethod.POST, produces="application/json")
	public @ResponseBody
	JsonResponse changePassword(@RequestParam("username") String username,
			@RequestParam("password") String password) {
		User u = null;
		try {
			u = userDao.getUserByUsername(username);

			if (!u.getUserType().equals(UserType.DATABASE_USER)) {
				return JsonResponseHelper.newErrorResponseStub("Since you are UAMS employee, you cannot change the password!");
			}

			u.setPassword(md5PasswordEncoder.encodePassword(password, username));
			userDao.saveOrUpdate(u);
		} catch (Exception ex) {
			//ex.printStackTrace();

			return JsonResponseHelper.newErrorResponseStub("Failed to change the password!");
		}
		return JsonResponseHelper.newDataResponseStub(u);
	}

	@RequestMapping(value = "/ajax/users/changepasswordfromuser", method = RequestMethod.POST, produces="application/json")
	public @ResponseBody
	JsonResponse changePasswordFromUser(
			@RequestParam("username") String username,
			@RequestParam("oldPassword") String oldPassword,
			@RequestParam("newPassword") String newPassword) {

		User u = userDao.getUserByUsername(username);

		if (!u.getUserType().equals(UserType.DATABASE_USER)) {
			return JsonResponseHelper.newErrorResponseStub("Since you are UAMS employee, you cannot change the password!");
		}

		if (md5PasswordEncoder.encodePassword(oldPassword, username).equals(
				u.getPassword())) {
			try {
				u.setPassword(md5PasswordEncoder.encodePassword(newPassword,
						username));
				userDao.saveOrUpdate(u);
			} catch (Exception ex) {
				//ex.printStackTrace();

				return JsonResponseHelper.newErrorResponseStub("Failed to change the password!");
			}
		} else {
			return JsonResponseHelper.newErrorResponseStub("Old password is not correct!");

		}

		return JsonResponseHelper.newDataResponseStub(u);
	}

	/**
	 * There is another way to return json, please refere to addProtocolFile
	 * method in <code>DocumentController</code> The pros: of doing it using
	 * 
	 * @ResponseBody, which uses MappingJacksonHttpMessageConverter to serialize
	 *                domain into json, is I can configure it differently to
	 *                make it return xml using ContentNegotiatingViewResolver +
	 *                XML Marshalling View The conds: I can't use a
	 *                MixInAnnotations interface to filter out the properties I
	 *                don't need...I have to hard code the jackson annotation in
	 *                the domain entity. However, this problem is purely a
	 *                spring issue, which they are trying to address in 3.1, so
	 *                we can use a ConversionService for Jackson
	 *                HttpMessageConverter,
	 *                <url>https://jira.springsource.org/browse/SPR-7054</url>
	 *                Therefore, doing @ResponseBody is perferred...
	 * 
	 * @param username
	 * @return
	 */
	@RequestMapping(value = "/ajax/users/createuseraccount", method = RequestMethod.GET)
	public @ResponseBody
	User createUserAccount(@RequestParam("username") String username) {
		User user = userService.getAndUpdateUserByUsername(username, true);
		return user;
	}

	/**
	 * 
	 * @param userId
	 * @return
	 */
	@RequestMapping(value = "/ajax/users/{userId}/user-roles/list", method = RequestMethod.GET)
	public @ResponseBody
	List<UserRole> getUserRoles(@PathVariable("userId") long userId) {

		// User user = userDao.findById(userId);
		List<UserRole> userRoles = null;
		try {
			userRoles = userRoleDao.getUserRolesByUserId(userId);
		} catch (Exception ex) {
			logger.warn("Cannot list user roles for userId: " + userId);
			ex.printStackTrace();
		}
		return userRoles;
	}

	/**
	 * 
	 * @param userId
	 * @return
	 */
	@RequestMapping(value = "/ajax/users/roles/list", method = RequestMethod.GET)
	public @ResponseBody
	List<Role> getRoles() {
		return roleDao.listAllOrderByName();
	}

	@RequestMapping(value = "/ajax/users/list-user-by-role", method = RequestMethod.GET)
	public @ResponseBody
	List<User> listUserByRole(
			@RequestParam("role") Permission rolePermissionIdentifier) {
		return userDao.getUsersByUserRole(rolePermissionIdentifier);
	}

	@RequestMapping(value = "/ajax/users/list-user-role-by-roles", method = RequestMethod.GET)
	public @ResponseBody
	List<UserRole> listUserByRole(
			@RequestParam("roles") List<Permission> rolePermissionIdentifiers) {
		return userRoleDao.getUserRolesByUserRole(rolePermissionIdentifiers);
	}

	/**
	 * not really deleting, but rather set the retired to true
	 * 
	 * @param userId
	 * @param userRoleId
	 * @return
	 */
	@RequestMapping(value = "/ajax/users/{userId}/user-roles/{userRoleId}/delete", method = RequestMethod.GET)
	public @ResponseBody
	UserRole deleteUserRole(@PathVariable("userId") long userId,
			@PathVariable("userRoleId") long userRoleId) {
		UserRole userRole = null;
		try {
			userRole = userRoleDao.findById(userRoleId);
			userRole.setRetired(Boolean.TRUE);

			userRole = userRoleDao.saveOrUpdate(userRole);
		} catch (Exception ex) {
			logger.warn("user role cannot be removed; userId: " + userId
					+ "; userRoleId: " + userRoleId);

		}
		return userRole;
	}
	
	private SFTPService sftpService;

	@RequestMapping(value = "/ajax/user/savefilemetadata", method = RequestMethod.POST, produces="application/json")
	public @ResponseBody
	JsonResponse saveCV(@RequestParam("userId") long userId,
			@RequestParam("title") String title,
			@RequestParam("uploadedFileId") long uploadedFileId,
			@RequestParam("category") String category) {
		

		try {
			User user = userDao.findById(userId);
			UploadedFile up = uploadedFileDao.findById(uploadedFileId);
			user.setUploadedFile(up);
			user = userDao.saveOrUpdate(user);
			
			sftpService.uploadLocalUploadedFileToRemote(user, up);

			return JsonResponseHelper.newDataResponseStub(user);
		} catch (Exception e) {
			e.printStackTrace();

			return JsonResponseHelper.newErrorResponseStub("Failed to save user CV!");
		}
	}

	@RequestMapping(value = "/ajax/users/{userId}/user-roles/create", method = RequestMethod.GET)
	public @ResponseBody
	UserRole createUserRole(
			@PathVariable("userId") long userId,
			@RequestParam("roleId") long roleId,
			@RequestParam(value = "dId", required = false) Long dId,
			@RequestParam(value = "isDelegate", required = false) Boolean isDelegate,
			@RequestParam(value = "isBusinessAdmin", required = false) Boolean isBusinessAdmin) {

		User currentUser = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		//check if the user has the system admin role to add roles
		boolean noPermission = true;
		for(UserRole userRole :currentUser.getUserRoles()) {
			if(userRole.getRole().getDisplayName().trim().equals("System Admin")){
				noPermission = false;
				break;
			}
		}
		// do not have system admin role, go back to index page.
		if(noPermission){
			return null;
		}
		
		UserRole userRole = null;
		try {
			userRole = userRoleDao.getUserRoleByUserIdAndRoleId(userId, roleId);
		} catch (Exception ex) {
			logger.info("user doesn't have this role yet!");
		}

		if (userRole != null) {
			// Allow mutiple college dean, depart chair and subdepartment chair
			// roles
			if (roleId != 4 && roleId != 5 && roleId != 6) {
				return null;
			}

		}

		User user = new User();
		user.setId(userId);

		Role role = roleDao.findById(roleId);

		userRole = new UserRole();

		// set default permission from the role
		// logger.debug("number of default roles: " +
		// role.getDefaultPermissions().size());
		// userRole.setUserRolePermissions(new
		// HashSet<Permission>(role.getDefaultPermissions()));

		userRole.setUser(user);
		userRole.setRole(role);

		if (isDelegate != null) {
			userRole.setDelegate(isDelegate);
		} else {
			userRole.setDelegate(Boolean.FALSE);
		}
		
		if (isBusinessAdmin != null) {
			userRole.setBusinessAdmin(isBusinessAdmin);
		} else {
			userRole.setBusinessAdmin(Boolean.FALSE);
		}

		DepartmentLevel departmentLevel = role.getDepartmentLevel();

		logger.debug("role: " + role.getId() + "; departmentLevel: "
				+ role.getDepartmentLevel() + "; dId: " + dId);

		if (dId != null) {

			logger.debug("getting departmentlevel and did");
			switch (departmentLevel) {
			case SUB_DEPARTMENT:
				SubDepartment subDepartment = subDepartmentDao.findById(dId);
				userRole.setSubDepartment(subDepartment);
				// logger.debug("sub-department: " + subDepartment.getName());
				userRole.setDepartment(subDepartment.getDepartment());
				// logger.debug("department: " +
				// subDepartment.getDepartment().getName());
				userRole.setCollege(subDepartment.getDepartment().getCollege());
				break;
			case DEPARTMENT:
				Department department = departmentDao.findById(dId);
				userRole.setDepartment(department);
				userRole.setCollege(department.getCollege());
				break;
			case COLLEGE:
				College college = new College();
				college.setId(dId);
				userRole.setCollege(college);
				break;
			}
		}

		try {
			userRole = userRoleDao.saveOrUpdate(userRole);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return userRole;

	}

	@RequestMapping(value = "/ajax/users/get-open-forms.xml", method = RequestMethod.GET, produces = "application/xml")
	public @ResponseBody
	Source getOpenForms() {
		String finalXml = "";

		try {
			List<MutexLock> mutexLocks = mutexLockDao.findAllLocked();
		

			if (mutexLocks != null && !mutexLocks.isEmpty()) {
				for (MutexLock mutexLock : mutexLocks) {
					if (mutexLock.getObjectClass().equals(ProtocolForm.class)) {

						ProtocolForm protocolForm = protocolFormDao
								.findById(mutexLock.getObjectId());

						finalXml += "<form object-type=\"protocol\" user-id=\""
								+ mutexLock.getUser().getId()
								+ "\" username=\""
								+ mutexLock.getUser().getUsername()
								+ "\" object-id=\""
								+ protocolForm.getProtocol().getId()
								+ "\" form-type=\""
								+ protocolForm.getFormType() + "\" form-id=\""
								+ protocolForm.getId() + "\" date-time=\"" + DateFormatUtil.formateDate(mutexLock.getModified()) + "\"/>";
					}

					if (mutexLock.getObjectClass().equals(ContractForm.class)) {
						logger.debug("" + mutexLock.getId());
						ContractForm contractForm = contractFormDao
								.findById(mutexLock.getObjectId());

						logger.debug("contractForm: "
								+ contractForm.getFormId());
						finalXml += "<form object-type=\"contract\" user-id=\""
								+ mutexLock.getUser().getId()
								+ "\" username=\""
								+ mutexLock.getUser().getUsername()
								+ "\" object-id=\""
								+ contractForm.getContract().getId()
								+ "\" form-type=\""
								+ contractForm.getFormType() + "\" form-id=\""
								+ contractForm.getId() + "\" date-time=\"" + DateFormatUtil.formateDate(mutexLock.getModified()) + "\"/>";
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();

			return XMLResponseHelper
					.newErrorResponseStub("Failed to load open forms!");
		}

		return DomUtils.toSource(XMLResponseHelper.xmlResult(finalXml));
	}

	@RequestMapping(value = "/ajax/users/{userId}/get-open-forms.xml", method = RequestMethod.POST, produces = "application/xml")
	public @ResponseBody
	Source getOpenForms(@PathVariable("userId") long userId) {
		String finalXml = "";

		try {
			List<MutexLock> mutexLocks = mutexLockDao
					.findAllLockedByUserId(userId);

			if (mutexLocks != null && !mutexLocks.isEmpty()) {
				for (MutexLock mutexLock : mutexLocks) {
					if (mutexLock.getObjectClass().equals(ProtocolForm.class)) {

						ProtocolForm protocolForm = protocolFormDao
								.findById(mutexLock.getObjectId());
						
						if (protocolForm != null) {
							finalXml += "<form object-type=\"protocol\" object-id=\""
									+ protocolForm.getProtocol().getId()
									+ "\" form-type=\""
									+ protocolForm.getFormType()
									+ "\" form-id=\""
									+ protocolForm.getId() + "\" />";
						}
						
					}

					if (mutexLock.getObjectClass().equals(ContractForm.class)) {

						ContractForm contractForm = contractFormDao
								.findById(mutexLock.getObjectId());
						
						if (contractForm != null) {
							finalXml += "<form object-type=\"contract\" object-id=\""
									+ contractForm.getContract().getId()
									+ "\" form-type=\""
									+ contractForm.getFormType()
									+ "\" form-id=\""
									+ contractForm.getId() + "\" />";
						}
						
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();

			return XMLResponseHelper
					.newErrorResponseStub("Failed to load open forms!");
		}

		return DomUtils.toSource(XMLResponseHelper.xmlResult(finalXml));
	}

	@RequestMapping(value = "/ajax/users/{userId}/close-open-form", method = RequestMethod.POST, produces = "application/xml")
	public @ResponseBody
	Source closeForm(@PathVariable("userId") long userId,
			@RequestParam("type") String type,
			@RequestParam("formId") long formId) {

		try {
			MutexLock mutexLock = null;

			if (type.equals("protocol")) {
				mutexLock = mutexLockDao
						.getMutexLockByObjectClassAndIdAndUserId(
								ProtocolForm.class, formId, userId);
			}

			if (type.equals("contract")) {
				mutexLock = mutexLockDao
						.getMutexLockByObjectClassAndIdAndUserId(
								ContractForm.class, formId, userId);
			}

			if (mutexLock != null) {
				mutexLockService.unlockMutexLock(mutexLock);
			} else {
				return XMLResponseHelper
						.newErrorResponseStub("This form has already been unlocked!");
			}
		} catch (Exception e) {
			e.printStackTrace();

			return XMLResponseHelper
					.newErrorResponseStub("Failed to unlock form!");
		}

		return DomUtils.toSource(XMLResponseHelper.xmlResult(Boolean.TRUE));
	}

	@Autowired(required = true)
	public void setUserService(UserService userService) {
		this.userService = userService;
	}

	public UserService getUserService() {
		return userService;
	}

	@Autowired(required = true)
	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}

	public UserDao getUserDao() {
		return userDao;
	}

	@Autowired(required = true)
	public void setRoleDao(RoleDao roleDao) {
		this.roleDao = roleDao;
	}

	public RoleDao getRoleDao() {
		return roleDao;
	}

	@Autowired(required = true)
	public void setCollegeDao(CollegeDao collegeDao) {
		this.collegeDao = collegeDao;
	}

	public CollegeDao getCollegeDao() {
		return collegeDao;
	}

	@Autowired(required = true)
	public void setDepartmentDao(DepartmentDao departmentDao) {
		this.departmentDao = departmentDao;
	}

	public DepartmentDao getDepartmentDao() {
		return departmentDao;
	}

	@Autowired(required = true)
	public void setSubDepartmentDao(SubDepartmentDao subDepartmentDao) {
		this.subDepartmentDao = subDepartmentDao;
	}

	public SubDepartmentDao getSubDepartmentDao() {
		return subDepartmentDao;
	}

	@Autowired(required = true)
	public void setUserRoleDao(UserRoleDao userRoleDao) {
		this.userRoleDao = userRoleDao;
	}

	public UserRoleDao getUserRoleDao() {
		return userRoleDao;
	}

	public Md5PasswordEncoder getMd5PasswordEncoder() {
		return md5PasswordEncoder;
	}

	@Autowired(required = true)
	public void setMd5PasswordEncoder(Md5PasswordEncoder md5PasswordEncoder) {
		this.md5PasswordEncoder = md5PasswordEncoder;
	}

	public PersonDao getPersonDao() {
		return personDao;
	}

	@Autowired(required = true)
	public void setPersonDao(PersonDao personDao) {
		this.personDao = personDao;
	}

	public UploadedFileDao getUploadedFileDao() {
		return uploadedFileDao;
	}

	@Autowired(required = true)
	public void setUploadedFileDao(UploadedFileDao uploadedFileDao) {
		this.uploadedFileDao = uploadedFileDao;
	}

	public CitiMemberDao getCitiMemberDao() {
		return citiMemberDao;
	}

	@Autowired(required = true)
	public void setCitiMemberDao(CitiMemberDao citiMemberDao) {
		this.citiMemberDao = citiMemberDao;
	}

	public UserCOIDao getUserCOIDao() {
		return userCOIDao;
	}

	@Autowired(required = true)
	public void setUserCOIDao(UserCOIDao userCOIDao) {
		this.userCOIDao = userCOIDao;
	}

	public EmailService getEmailService() {
		return emailService;
	}

	@Autowired(required = true)
	public void setEmailService(EmailService emailService) {
		this.emailService = emailService;
	}

	public MutexLockService getMutexLockService() {
		return mutexLockService;
	}

	@Autowired(required = true)
	public void setMutexLockService(MutexLockService mutexLockService) {
		this.mutexLockService = mutexLockService;
	}

	public MutexLockDao getMutexLockDao() {
		return mutexLockDao;
	}

	@Autowired(required = true)
	public void setMutexLockDao(MutexLockDao mutexLockDao) {
		this.mutexLockDao = mutexLockDao;
	}

	public ProtocolFormDao getProtocolFormDao() {
		return protocolFormDao;
	}

	@Autowired(required = true)
	public void setProtocolFormDao(ProtocolFormDao protocolFormDao) {
		this.protocolFormDao = protocolFormDao;
	}

	public ContractFormDao getContractFormDao() {
		return contractFormDao;
	}

	@Autowired(required = true)
	public void setContractFormDao(ContractFormDao contractFormDao) {
		this.contractFormDao = contractFormDao;
	}

	public SFTPService getSftpService() {
		return sftpService;
	}

	@Autowired(required = true)
	public void setSftpService(SFTPService sftpService) {
		this.sftpService = sftpService;
	}

	public XmlProcessor getXmlProcessor() {
		return xmlProcessor;
	}
	
	@Autowired(required = true)
	public void setXmlProcessor(XmlProcessor xmlProcessor) {
		this.xmlProcessor = xmlProcessor;
	}

	public AuditService getAuditService() {
		return auditService;
	}
	
	@Autowired(required = true)
	public void setAuditService(AuditService auditService) {
		this.auditService = auditService;
	}
}
