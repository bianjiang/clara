Ext.define('Clara.User.model.LockedForm', {
	extend: 'Ext.data.Model',
	fields: [{name:'objectid',mapping:'@object-id'},
	         {name:'formid',mapping:'@form-id',type:'int'},
	         {name:'oType', mapping:'@object-type'},
	         {name:'formType', mapping:'@form-type'},
	         {name:'modifedTime', mapping:'@date-time'}],

	         sorters: [{property:'id', direction:'DESC'}],
	         
	         proxy: {
	        	 type: 'ajax',
	        	 url:appContext + '/ajax/users/'+profile.id+'/get-open-forms.xml', 		
	        	 actionMethods: {
	                 read: 'POST'
	             },
	        	 headers:{'Accept':'application/xml;charset=UTF-8'},
	        	 reader:{
	        		 type:'xml',
	        		 record:'form',
	        		 root:'result',
	        		 idProperty:'@form-id'
	        	 }
	         }
});
