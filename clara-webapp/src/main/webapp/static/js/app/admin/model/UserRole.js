Ext.define('Clara.Admin.model.UserRole', {
	extend: 'Ext.data.Model',
	fields: [{name:'id'},
	         {name:'rolename', mapping:'role.name'},
	         {name:'roleid', mapping:'role.id'},
	         {name:'roledesc', mapping:'role.displayName'},
	         {name:'roledeptlevel', mapping:'role.departmentLevel'},
	         {name:'delegate', mapping:'delegate'},
	         {name:'collegeid', mapping:function(v){ return (v.college)?v.college.id:null;}},
	         {name:'collegename', mapping:function(v){ return (v.college)?v.college.name:null;}},
	         {name:'deptid', mapping:function(v){ return (v.department)?v.department.id:null;}},
	         {name:'deptname',mapping:function(v){ return (v.department)?v.department.name:null;}}, 
	         {name:'subdeptid', mapping:function(v){ return (v.subDepartment)?v.subDepartment.id:null;}},
	         {name:'subdeptname', mapping:function(v){ return (v.subDepartment)?v.subDepartment.name:null;}}
	         ],

	         sorters: [{property:'rolename'}],
	         
	         proxy: {
	        	 type: 'ajax',
	        	 url:appContext + '/ajax/users/roles/list', 		// if UserRole, then CHANGES DYNAMICALLY
	        	 actionMethods: {
	                 read: 'GET'
	             },
	        	 headers:{'Accept':'application/json;charset=UTF-8'},
	        	 reader:{
	        		 idProperty: 'id'
	        	 }
	         }
});
