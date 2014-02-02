Ext.define('Clara.Reports.model.Contract', {
	extend: 'Ext.data.Model',
	fields: [{name:'title', mapping: 'title'},
	         {name:'status',mapping:'status'},
	         {name:'submissionType',mapping: '@type'},
	         {name:'contractId',mapping:'@id',type:'int'},
	         {name:'contractIdentifier',mapping:'@identifier'}],

	         sorters: [{property:'contractId', direction:'DESC'}],
	         
	         proxy: {
	        	 type: 'ajax',
	        	 url: appContext + "/ajax/contracts/list.xml", 		// this will be set dynamically as different bookmarks are chosen.
	        	 actionMethods: {
	                 read: 'POST'
	             },
	        	 headers:{'Accept':'application/xml;charset=UTF-8'},
	        	 reader:{
	        		 type:'xml',
	        		 record:'contract',
	        		 root:'list',
	        		 totalProperty:'@total',
	        		 idProperty:'@id'
	        	 }
	         }
});
