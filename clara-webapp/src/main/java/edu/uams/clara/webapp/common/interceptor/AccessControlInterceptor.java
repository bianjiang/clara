package edu.uams.clara.webapp.common.interceptor;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.joda.time.DateTime;
import org.joda.time.Minutes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.uams.clara.webapp.common.dao.usercontext.RoleDao;
import edu.uams.clara.webapp.common.domain.security.MutexLock;
import edu.uams.clara.webapp.common.domain.usercontext.Role;
import edu.uams.clara.webapp.common.domain.usercontext.User;
import edu.uams.clara.webapp.common.domain.usercontext.enums.Committee;
import edu.uams.clara.webapp.common.domain.usercontext.enums.Permission;
import edu.uams.clara.webapp.common.security.MutexLockService;
import edu.uams.clara.webapp.common.security.ObjectAclService;
import edu.uams.clara.webapp.common.util.response.JsonResponse;
import edu.uams.clara.webapp.contract.dao.businesslogicobject.ContractFormStatusDao;
import edu.uams.clara.webapp.contract.domain.Contract;
import edu.uams.clara.webapp.contract.domain.businesslogicobject.ContractFormStatus;
import edu.uams.clara.webapp.contract.domain.businesslogicobject.enums.ContractFormStatusEnum;
import edu.uams.clara.webapp.contract.domain.contractform.ContractForm;
import edu.uams.clara.webapp.protocol.dao.businesslogicobject.ProtocolFormStatusDao;
import edu.uams.clara.webapp.protocol.dao.protocolform.ProtocolFormDao;
import edu.uams.clara.webapp.protocol.domain.Protocol;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.ProtocolFormStatus;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.enums.ProtocolFormStatusEnum;
import edu.uams.clara.webapp.protocol.domain.irb.Agenda;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolForm;

public class AccessControlInterceptor extends HandlerInterceptorAdapter {
	private final static Logger logger = LoggerFactory
			.getLogger(AccessControlInterceptor.class);

	private MutexLockService mutexLockService;

	private ObjectAclService objectAclService;

	private RoleDao roleDao;
	
	private ProtocolFormDao protocolFormDao;
	
	private ProtocolFormStatusDao protocolFormStatusDao;
	
	private ContractFormStatusDao contractFormStatusDao;
	
	private List<ProtocolFormStatusEnum> protocolFormStatusLst = new ArrayList<ProtocolFormStatusEnum>();{
		protocolFormStatusLst.add(ProtocolFormStatusEnum.DRAFT);
		protocolFormStatusLst.add(ProtocolFormStatusEnum.REVISED);
		protocolFormStatusLst.add(ProtocolFormStatusEnum.UNDER_REVISION);
		protocolFormStatusLst.add(ProtocolFormStatusEnum.REVISION_PENDING_PI_ENDORSEMENT);
		protocolFormStatusLst.add(ProtocolFormStatusEnum.REVISION_REQUESTED);
		protocolFormStatusLst.add(ProtocolFormStatusEnum.RETURN_FOR_BUDGET_NEGOTIATIONS);
		protocolFormStatusLst.add(ProtocolFormStatusEnum.PENDING_BUDGET_NEGOTIATIONS);
		protocolFormStatusLst.add(ProtocolFormStatusEnum.PENDING_BUDGET_NEGOTIATIONS_PENDING_PI_ENDORSEMENT);
		protocolFormStatusLst.add(ProtocolFormStatusEnum.PENDING_TP_ENDORSEMENT);
		protocolFormStatusLst.add(ProtocolFormStatusEnum.PENDING_PI_ENDORSEMENT);
		//protocolFormStatusLst.add(ProtocolFormStatusEnum.PENDING_PL_ENDORSEMENT);
		protocolFormStatusLst.add(ProtocolFormStatusEnum.REVISION_WITH_MAJOR_PENDING_PI_ENDORSEMENT);
		protocolFormStatusLst.add(ProtocolFormStatusEnum.REVISION_WITH_MINOR_PENDING_PI_ENDORSEMENT);
		protocolFormStatusLst.add(ProtocolFormStatusEnum.UNDER_REVISION_MAJOR_CONTINGENCIES);
		protocolFormStatusLst.add(ProtocolFormStatusEnum.UNDER_REVISION_MINOR_CONTINGENCIES);
		protocolFormStatusLst.add(ProtocolFormStatusEnum.UNDER_REVISION_RESPONSE_TO_TABLED);
		protocolFormStatusLst.add(ProtocolFormStatusEnum.RESPONSE_TO_TABLED_PENDING_PI_ENDORSEMENT);
	}
	
	private List<ContractFormStatusEnum> contractFormStatusLst = new ArrayList<ContractFormStatusEnum>();{
		contractFormStatusLst.add(ContractFormStatusEnum.DRAFT);
		contractFormStatusLst.add(ContractFormStatusEnum.REVISED);
		contractFormStatusLst.add(ContractFormStatusEnum.UNDER_REVISION);
	}
	
	private Pattern formXmlDataPattern = Pattern
			.compile("(?:ajax)?\\/(protocols|contracts)\\/(\\d+)\\/((protocol|contract)\\-forms)\\/(\\d+)(?:\\/.*?)?\\/((protocol|contract)-form-xml-datas)\\/(\\d+)\\/");
	
	private Pattern meetingPattern = Pattern
			.compile("agendas\\/(\\d+)\\/meeting");

	private Pattern listOrGetPattern = Pattern
			.compile("(?:ajax)?\\/(protocols|contracts)\\/(\\d+)/((protocol|contract)\\-forms)\\/(\\d+)(?:\\/.*?)?\\/((protocol|contract)-form-xml-datas)\\/(\\d+)\\/(.*?)\\/([list|get].*?)");
	
	//TODO need to find a way to merge this pattern with listOrGetPattern since they are all read only pattern
	private Pattern validatePattern = Pattern
			.compile("(?:ajax)?\\/(protocols|contracts)\\/(\\d+)\\/((protocol|contract)\\-forms)\\/(\\d+)(?:\\/.*?)?\\/((protocol|contract)-form-xml-datas)\\/(\\d+)\\/validate");
	
	private static final int timeOutPeriod = 45;
	
	@Override
	public boolean preHandle(HttpServletRequest request,
			HttpServletResponse response, Object handler) throws Exception {
		
		if (!request.getRequestURI().contains("/documents/") && !request.getRequestURI().contains("/signed")) {
			//logger.debug("preHandle mutexLockCheck!!!!!");
			StringBuffer url = request.getRequestURL();
			
			Date currentDate = new Date();
			
			//logger.debug("url: " + url);
			// "(protocols|contracts)/.*-forms/\d+/"
			// Pattern patternva = Pattern.compile("(forms/)(.+?)(/)");
			Matcher m = formXmlDataPattern.matcher(url);
			Matcher listOrGetMatcher = listOrGetPattern.matcher(url);
			Matcher meetingReadOnlyMatcher = meetingPattern.matcher(url);
			Matcher validateMatcher = validatePattern.matcher(url);
			
			boolean listOrGet = listOrGetMatcher.find();
			boolean validate = validateMatcher.find();

			boolean readOnly = (listOrGet || validate)?true:false;
			boolean meetingReadOnly = meetingReadOnlyMatcher.find();

			if (m.find()) {
				//String formTypeUrlEncoded = m.group(6);
				
				long objectId = Long.valueOf(m.group(2));
				long formId = Long.valueOf(m.group(5));
				String object = m.group(1);
				
				//logger.debug("formId: " + formId + ", object: " + object);
				
				Class<?> objectClass = null;
				Class<?> objectFormClass = null;

				if ("protocols".equals(object)) {
					objectClass = Protocol.class;
					objectFormClass = ProtocolForm.class;
				} else if ("contracts".equals(object)) {
					objectClass = Contract.class;
					objectFormClass = ContractForm.class;
				} else {
					return super.preHandle(request, response, handler);
				}
				
				User currentUser = (User) SecurityContextHolder.getContext()
						.getAuthentication().getPrincipal();

				MutexLock mutexLock = mutexLockService.getLockedByObjectClassAndId(
						objectFormClass, formId);

				if (mutexLock != null) {
					Date lastModifiedDate = mutexLock.getModified();

					DateTime currentDateTime = new DateTime(currentDate);
					DateTime lastModifiedDateTime = new DateTime(lastModifiedDate);

					//logger.debug("time diff: "
							//+ Minutes.minutesBetween(lastModifiedDateTime,
									//currentDateTime).getMinutes());

					if (Minutes.minutesBetween(lastModifiedDateTime,
							currentDateTime).getMinutes() > timeOutPeriod && !readOnly) {
						if (objectAclService.hasEditObjectAccess(objectClass, objectId, currentUser) && !currentUser.getAuthorities().contains(Permission.ROLE_REVIEWER)){
							
							if ("protocols".equals(object)){
								ProtocolFormStatus latestProtocolFormStatus = protocolFormStatusDao.getLatestProtocolFormStatusByFormId(formId);

								if (!protocolFormStatusLst.contains(latestProtocolFormStatus.getProtocolFormStatus())){		
									response.sendRedirect("/clara-webapp/protocols/" + objectId + "/dashboard");
									
									return false;
								}
							}
							
							if ("contracts".equals(object)){
								ContractFormStatus latestContractFormStatus = contractFormStatusDao.getLatestContractFormStatusByFormId(formId);
								
								if (!contractFormStatusLst.contains(latestContractFormStatus.getContractFormStatus())){			
									response.sendRedirect("/clara-webapp/contracts/" + objectId + "/dashboard");
									
									return false;
								}
							}
						}
						
						mutexLockService.unlockMutexLock(mutexLock);
						mutexLockService.lockObjectByClassAndIdAndUser(objectFormClass,
								formId, currentUser);
					} else {
						if (mutexLockService
								.isLockedByObjectClassAndIdForCurrentUser(
										objectFormClass, formId, currentUser)) {
							//logger.warn("another user is editing");

							if (request.getRequestURL().indexOf("/ajax") != -1) {
								
								boolean shouldRedirect = true;
									
								if(readOnly){
									shouldRedirect = false;
								}

								if (shouldRedirect){
									ObjectMapper mapper = new ObjectMapper();

									JsonResponse jsonResponse = new JsonResponse(true,
											"Another user is editing this form...", "/", shouldRedirect);
									response.getOutputStream().print(
											mapper.writeValueAsString(jsonResponse));
								}
					
							} else {
								response.sendRedirect("/clara-webapp");
								return false;
							}
						} else {
							if (objectAclService.hasEditObjectAccess(objectClass, objectId, currentUser) && !currentUser.getAuthorities().contains(Permission.ROLE_REVIEWER)){
								
								if ("protocols".equals(object)){
									ProtocolFormStatus latestProtocolFormStatus = protocolFormStatusDao.getLatestProtocolFormStatusByFormId(formId);

									if (!protocolFormStatusLst.contains(latestProtocolFormStatus.getProtocolFormStatus())){		
										response.sendRedirect("/clara-webapp/protocols/" + objectId + "/dashboard");
										
										return false;
									}
								}
								
								if ("contracts".equals(object)){
									ContractFormStatus latestContractFormStatus = contractFormStatusDao.getLatestContractFormStatusByFormId(formId);
									
									if (!contractFormStatusLst.contains(latestContractFormStatus.getContractFormStatus())){			
										response.sendRedirect("/clara-webapp/contracts/" + objectId + "/dashboard");
										
										return false;
									}
								}
							}
							
							if (mutexLock.getUser().getId() != currentUser.getId() && !readOnly)
								mutexLockService.lockObjectByClassAndIdAndUser(
										objectFormClass, formId, currentUser);
						}
					}
				} else {
					if (!readOnly){
						mutexLockService.lockObjectByClassAndIdAndUser(objectFormClass,
								formId, currentUser);
					}
					
				}
			}
			
			if (meetingReadOnly){
				User currentUser = (User) SecurityContextHolder.getContext()
						.getAuthentication().getPrincipal();
				
				long agendaId = Long.valueOf(meetingReadOnlyMatcher.group(1));
				
				MutexLock mutexLock = mutexLockService.getLockedByObjectClassAndId(
						Agenda.class, agendaId);
				
				if (mutexLock != null) {
					Date lastModifiedDate = mutexLock.getModified();

					DateTime currentDateTime = new DateTime(currentDate);
					DateTime lastModifiedDateTime = new DateTime(lastModifiedDate);

					if (Minutes.minutesBetween(lastModifiedDateTime,
							currentDateTime).getMinutes() > timeOutPeriod) {
						mutexLockService.unlockMutexLock(mutexLock);
						mutexLockService.lockObjectByClassAndIdAndUser(Agenda.class,
								agendaId, currentUser);
					} else {
						mutexLockService.updateMutexLock(mutexLock);
					}
				} else {
					mutexLockService.lockObjectByClassAndIdAndUser(Agenda.class,
							agendaId, currentUser);
					
				}
			}
		}

		return super.preHandle(request, response, handler);
	}

	private Set<Permission> getUserObjectSpecificPermissions(
			Class<?> objectClass, long objectId, User user) {
		Set<Permission> permissions = new HashSet<Permission>(0);

		if (objectClass == null) {
			return permissions;
		}
		//logger.debug("getUserObjectSpecificPermissions("
				//+ objectClass.toString() + ", " + objectId + ", "
				//+ user.getId() + ")");

		if (objectAclService.isObjectAccessible(objectClass, objectId, user)) {
			Role studyStaffRole = roleDao.getRoleByCommittee(Committee.PI);

			permissions.addAll(studyStaffRole.getDefaultPermissions());
			
		}

		return permissions;
	}

	private Pattern formSpecificPattern = Pattern
			.compile("(protocols|contracts)\\/(\\d+)\\/((protocol|contract)\\-forms)\\/(\\d+)\\/(.*?)\\/");

	private Set<Permission> editStudyPermissions = new HashSet<Permission>();
	{
		editStudyPermissions.add(Permission.EDIT_BUDGET);
		editStudyPermissions.add(Permission.EDIT_STUDY);
		editStudyPermissions.add(Permission.EDIT_LOCKED_BUDGET);
		editStudyPermissions.add(Permission.EDIT_PHARMACY);
	}

	@Override
	public void postHandle(HttpServletRequest request,
			HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {
		
		Date currentDate = new Date();
		
		String claraBuildNumber = String.valueOf(currentDate.getTime());

		//logger.debug("postHandle: url: " + request.getRequestURL());
		Matcher m = formSpecificPattern.matcher(request.getRequestURL());

		/* only push the objectPermissions if it's not an ajax call */
		if (m.find() && !(request.getRequestURL().indexOf("/ajax") != -1)) {
			User currentUser = (User) SecurityContextHolder.getContext()
					.getAuthentication().getPrincipal();
			
			long objectID = Long.valueOf(m.group(2));
			long formId = Long.valueOf(m.group(5));
			String formTypeUrlEncoded = m.group(6);

			String object = m.group(1);
			
			Class<?> objectClass = null;
			
			if ("protocols".equals(object)) {
				objectClass = Protocol.class;
				
				
			} else if ("contracts".equals(object)) {
				objectClass = Contract.class;
			}

			Set<Permission> objectPermissions = (modelAndView.getModelMap().get("objectPermissions") != null)?(Set<Permission>)modelAndView.getModelMap().get("objectPermissions"):getUserObjectSpecificPermissions(
					objectClass, objectID, currentUser);

			boolean isLocked = false;
			
			if (Protocol.class.equals(objectClass) && "budgets".equals(formTypeUrlEncoded)){
				//logger.debug("formTypeUrlEncoded: " + formTypeUrlEncoded + "; formId: " + formId + "; currentUser: " + currentUser.getId());
				if (request.getParameter("readOnly") == null || request.getParameter("readOnly").isEmpty()){
					MutexLock mutexLock = mutexLockService.getLockedByObjectClassAndId(
							ProtocolForm.class, formId);
					
					isLocked = mutexLockService.isLockedByObjectClassAndIdForCurrentUser(
							ProtocolForm.class, formId, currentUser);
					if (mutexLock != null){
						Date lastModifiedDate = mutexLock.getModified();

						DateTime currentDateTime = new DateTime(currentDate);
						DateTime lastModifiedDateTime = new DateTime(lastModifiedDate);

						//logger.debug("time diff: "
								//+ Minutes.minutesBetween(lastModifiedDateTime,
										//currentDateTime).getMinutes());

						if (Minutes.minutesBetween(lastModifiedDateTime,
								currentDateTime).getMinutes() > timeOutPeriod) {
							mutexLockService.unlockMutexLock(mutexLock);
							mutexLockService.lockObjectByClassAndIdAndUser(ProtocolForm.class,
									formId, currentUser);
						} else {
							if (isLocked){
								//logger.warn("another user is editing");
								
								objectPermissions.removeAll(editStudyPermissions);
							} else {
								if (mutexLock.getUser().getId() != currentUser.getId()){
									mutexLockService.lockObjectByClassAndIdAndUser(
											ProtocolForm.class, formId, currentUser);
								}
							}
						}
					} else {
						mutexLockService.lockObjectByClassAndIdAndUser(
								ProtocolForm.class, formId, currentUser);
					}
				}
				
				
				/*
				if (isLocked) {
					logger.debug("here");
					objectPermissions.removeAll(editStudyPermissions);
					for(Permission p:objectPermissions){
						logger.debug("p: " + p);
					}
				}*/
			}
			logger.debug("formTypeUrlEncoded: " + formTypeUrlEncoded);
			if (Protocol.class.equals(objectClass) && "pharmacy".equals(formTypeUrlEncoded)){
				if (currentUser.getAuthorities().contains(Permission.EDIT_PHARMACY)){
					MutexLock mutexLock = mutexLockService.getLockedByObjectClassAndId(
							ProtocolForm.class, formId);
					
					isLocked = mutexLockService.isLockedByObjectClassAndIdForCurrentUser(
							ProtocolForm.class, formId, currentUser);
					if (mutexLock != null){
						Date lastModifiedDate = mutexLock.getModified();

						DateTime currentDateTime = new DateTime(currentDate);
						DateTime lastModifiedDateTime = new DateTime(lastModifiedDate);

						//logger.debug("time diff: "
								//+ Minutes.minutesBetween(lastModifiedDateTime,
										//currentDateTime).getMinutes());

						if (Minutes.minutesBetween(lastModifiedDateTime,
								currentDateTime).getMinutes() > timeOutPeriod) {
							mutexLockService.unlockMutexLock(mutexLock);
							mutexLockService.lockObjectByClassAndIdAndUser(ProtocolForm.class,
									formId, currentUser);
						} else {
							if (isLocked){
								//logger.warn("another user is editing");
								
								objectPermissions.removeAll(editStudyPermissions);
							} else {
								if (mutexLock.getUser().getId() != currentUser.getId()){
									mutexLockService.lockObjectByClassAndIdAndUser(
											ProtocolForm.class, formId, currentUser);
								}
							}
						}
					} else {
						mutexLockService.lockObjectByClassAndIdAndUser(
								ProtocolForm.class, formId, currentUser);
					}
				}
			}

			modelAndView.getModelMap().put("objectPermissions",
					objectPermissions);
			
			modelAndView.getModelMap().put("mutexLocked",
					isLocked);
			modelAndView.getModelMap().put("claraBuildNumber", claraBuildNumber);
			//logger.debug("postHandle add object specific permissions!!!!!");
		}
		// modelAndView.getModelMap().put(key, value)
		super.postHandle(request, response, handler, modelAndView);
	}

	public MutexLockService getMutexLockService() {
		return mutexLockService;
	}

	@Autowired(required = true)
	public void setMutexLockService(MutexLockService mutexLockService) {
		this.mutexLockService = mutexLockService;
	}

	public RoleDao getRoleDao() {
		return roleDao;
	}

	@Autowired(required = true)
	public void setRoleDao(RoleDao roleDao) {
		this.roleDao = roleDao;
	}

	public ObjectAclService getObjectAclService() {
		return objectAclService;
	}

	@Autowired(required = true)
	public void setObjectAclService(ObjectAclService objectAclService) {
		this.objectAclService = objectAclService;
	}

	public ProtocolFormDao getProtocolFormDao() {
		return protocolFormDao;
	}

	@Autowired(required = true)
	public void setProtocolFormDao(ProtocolFormDao protocolFormDao) {
		this.protocolFormDao = protocolFormDao;
	}

	public ProtocolFormStatusDao getProtocolFormStatusDao() {
		return protocolFormStatusDao;
	}
	
	@Autowired(required = true)
	public void setProtocolFormStatusDao(ProtocolFormStatusDao protocolFormStatusDao) {
		this.protocolFormStatusDao = protocolFormStatusDao;
	}
	
	public ContractFormStatusDao getContractFormStatusDao() {
		return contractFormStatusDao;
	}
	
	@Autowired(required = true)
	public void setContractFormStatusDao(ContractFormStatusDao contractFormStatusDao) {
		this.contractFormStatusDao = contractFormStatusDao;
	}
}
