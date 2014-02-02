Ext.define('Clara.Queue.model.Reviewer', {
	extend: 'Ext.data.Model',
	fields: [{name:'id'},
			    {name:'userId', mapping:'user.id'},
			    {name:'roleName', mapping:'role.name'},
			    {name:'roleIdentifier', mapping:'role.rolePermissionIdentifier'},
			    {name:'username', mapping:'user.username'},
			    {name:'email', mapping:'user.person.email'},
			    {name:'firstname', mapping:'user.person.firstname'},
			    {name:'lastname', mapping:'user.person.lastname'},
			    {name:'middlename', mapping:'user.person.middlename'}
	         ],
	         proxy: {
	        	 type: 'ajax',
	        	 url: appContext + "/ajax/users/list-user-role-by-roles",	
	        	 actionMethods: {
	                 read: 'GET'
	             },
	        	 headers:{'Accept':'application/json;charset=UTF-8'},
	        	 reader:{
	        		 idProperty: 'id'
	        	 }
	         }
});
