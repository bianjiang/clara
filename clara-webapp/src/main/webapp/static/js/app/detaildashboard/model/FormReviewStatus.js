Ext.define('Clara.DetailDashboard.model.FormReviewAction', {
    extend: 'Ext.data.Model',
    fields: [
				{name:'type'},
				{name:'class', mapping:'@cls'},
				{name:'name'},
				{name:'urlType',mapping:'url@type'},
				{name:'response', mapping:'url@response'},//the type of response
				{name:'target',mapping:'url@target'},
				{name:'url'}
             ]
});

Ext.define('Clara.DetailDashboard.model.FormReviewStatus', {
	extend: 'Ext.data.Model',
	proxy: new Ext.data.HttpProxy({
		type: 'ajax',
	   	url:'',
	   	actionMethods: {
	        read: 'GET'
	    },
		headers:{'Accept':'application/xml;charset=UTF-8'},
		reader: {
			type:'xml',
			record:claraInstance.type+'-form-committee-status',
   		 	root:'list'
		}
		
	}),
    fields:[{name:'formCommitteeStatusId', mapping: '@id'},
            {name:'formId', mapping: claraInstance.type+"FormId"},
            {name:'committee', mapping: 'committee'},
            {name:'parentCommittee', mapping: 'parent_committee_code'},
            {name:'committee_code', mapping: 'committee_code'},
            {name:'status', mapping: 'status'},
            {name:'priority', mapping: 'status@priority'},
            {name:'modified', mapping:'modified'}],
     hasMany: [{
               model: 'Clara.DetailDashboard.model.FormReviewAction',
               name: 'itemActions',
               associationKey: 'actions',
               reader: {
                   type: 'xml',
                   record: 'action',
                   root:'actions'
               }
           }],
});