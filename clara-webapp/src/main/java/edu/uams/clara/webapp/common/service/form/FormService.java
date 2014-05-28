package edu.uams.clara.webapp.common.service.form;

import java.util.List;
import java.util.Map;

import edu.uams.clara.webapp.common.domain.form.Form;
import edu.uams.clara.webapp.common.domain.usercontext.User;
import edu.uams.clara.webapp.common.service.form.impl.FormServiceImpl.UserSearchField;

public interface FormService {
	String pullFromOtherForm(String listPath, String originalXml);
	boolean isCurrentUserSpecificRoleOrNot(Form form,
			User currentUser, String roleName);
	String getAssignedReviewers(Form form);
	List<User> getUsersByKeywordAndSearchField(String keyWord, String xmlData, UserSearchField userSearchField);
	
	List<String> getNoClaraUsers(String metaData);
	
	Map<String, List<String>> getValuesFromXmlString(String xmlString, List<String> xPathList);
	
	String getSafeStringValueByKey(Map<String, List<String>> values, String key, String exceptedReturnValueForNull);
	
	String addExtraStaffInformation(String formBaseTag, String formXmlData);
}
