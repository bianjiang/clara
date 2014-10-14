Ext.define('Clara.DetailDashboard.model.FormReviewCompletedChecklist', {
	extend: 'Ext.data.Model',
	fields: [{name:'id', mapping:'@id'},
				{name:'actor', mapping:'reviewer-name'},
				{name:'url',mapping: 'url'},
				{name:'modified', mapping:'modified', type: 'string'}
			],
	
	proxy: {
		type: 'ajax',
	   	url:'',
	   	actionMethods: {
	        read: 'GET'
	    },
	   	headers:{'Accept':'application/xml;charset=UTF-8'},

		reader: {
			type:'xml',
   		 	record:'checklist',
   		 	root:'list',
   		 	idProperty:'url'
		}
	}
});