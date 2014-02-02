Ext.define('Clara.Reports.model.Protocol', {
	extend: 'Ext.data.Model',
	fields: [{name:'title', mapping: 'title'},
	         {name:'status',mapping:'status'},
	         {name:'priority',mapping:'status@priority'},
	         {name:'studyType', mapping:'study-type'},
	         {name:'submissionType',mapping: '@type'},
	         {name:'formType',mapping: '@type'},
	         {name:'protocolId',mapping:'@id',type:'int'},
	         {name:'protocolIdentifier',mapping:'@identifier'}],

	         sorters: [{property:'protocolId', direction:'DESC'}],
	         
	         proxy: {
	        	 type: 'ajax',
	        	 url: appContext + "/ajax/protocols/list.xml", 		// this will be set dynamically as different bookmarks are chosen.
	        	 actionMethods: {
	                 read: 'POST'
	             },
	        	 headers:{'Accept':'application/xml;charset=UTF-8'},
	        	 reader:{
	        		 type:'xml',
	        		 record:'protocol',
	        		 root:'list',
	        		 totalProperty:'@total',
	        		 idProperty:'@id'
	        	 }
	         }
});
