Ext.define('Clara.Admin.model.Thing', {
	extend: 'Ext.data.Model',
	fields: [{name:'identifier'},
	         {name:'id'},
			    {name:'value'},
			    {name:'description'},
			    {name:'type'},
			    {name:'approved'}
	         ],

	         sorters: [{property:'identifier'}],
	         
	         proxy: {
	        	 type: 'ajax',
	        	 url:appContext + '/ajax/admin/things/search', 
	        	 actionMethods: {
	                 read: 'GET',
	                 update:'POST',
	                 create:'POST',
	                 destroy:'POST'
	             },
	        	 headers:{'Accept':'application/json;charset=UTF-8'},
	        	 reader:{
	        		 idProperty: 'id',
	        		 root:'data'
	        	 },
	        	 writer:{
	        		 writeAllFields:true,
	        		 allowSingle:true
	        	 },
	        	 api:{
	        		 'read':appContext + '/ajax/admin/things/search', 
	        		 'update':appContext + '/ajax/admin/things/update', 
	        		 'create':appContext + '/ajax/admin/things/create', 
	        		 'destroy':appContext + '/ajax/admin/things/delete', 
	        	 }
	         }
});
