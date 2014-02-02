Ext.ns('Clara');

var requiredRoles = {
		contingency : ['CONTINGENCY_CAN_ADD'],
		copyComments: ['COMMENT_CAN_COPY'],
		moveComments: ['COMMENT_CAN_MOVE'],
		addComments:  ['ROLE_IRB_REVIEWER','COMMENT_CAN_ADD'],
		changeStatus: ['CAN_EDIT_IRB_COMMENT_STATUS']
	};

Clara.IsAgendaChair = function(week){
	
	for (var i=0;i<claraInstance.IrbReviewer.length;i++){
		if (claraInstance.IrbReviewer[i].irbroster == week && claraInstance.IrbReviewer[i].chair == true) return true;
	}
	
	return false;

};

Clara.IsUser = function(userId){
	return (claraInstance.user.id == userId);
};

Clara.HasAnyPermissions = function(setToCheck, logMsg){
	clog("claraInstance.HasAnyPermissions() DEPRECATED: Use claraInstance.HasAnyPermissions() instead");
	return (typeof claraInstance != 'undefined')?claraInstance.HasAnyPermissions(setToCheck, logMsg):false;
};

Clara.HasAllPermissions = function(setToCheck, logMsg){
	clog("Clara.HasAllPermissions() DEPRECATED: Use claraInstance.HasAllPermissions() instead");
	return (typeof claraInstance != 'undefined')?claraInstance.HasAnyPermissions(setToCheck, logMsg):false;
};

