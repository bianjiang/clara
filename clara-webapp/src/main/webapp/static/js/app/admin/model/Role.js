Ext.define('Clara.Admin.model.Role', {
	extend: 'Ext.data.Model',
	fields: [{name:'id'},
			    {name:'name'},
			    {name:'rolePermissionIdentifier'},
			    {name:'commitee'},
			    {name:'departmentLevel'},
			    {name:'displayName'}
	         ],

	         sorters: [{property:'displayName'}],
	         
	         proxy: {
	        	 type: 'ajax',
	        	 url:appContext + '/ajax/users/roles/list', 		
	        	 actionMethods: {
	                 read: 'GET'
	             },
	        	 headers:{'Accept':'application/json;charset=UTF-8'},
	        	 reader:{
	        		 idProperty: 'id'
	        	 }
	         }
});
