Ext.define('Clara.DetailDashboard.model.FormAction', {
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

Ext.define('Clara.DetailDashboard.model.Form', {
	extend: 'Ext.data.Model',
	fields: [
				{name:'formId', mapping: '@'+(claraInstance.type || 'protocol')+'FormId'},
				{name:'formIndex', mapping: '@index'},
				{name:'formtype', mapping:   (claraInstance.type || 'protocol')+'-form-type'},
				{name:'formTypeId', mapping:   (claraInstance.type || 'protocol')+'-form-type@id'},
				
				{name:'url',mapping:'url'},
				{name:'studynature', mapping:'details>study-nature'},
				{name:'editurl',mapping:'editurl'},
				{name:'locked',mapping:'status>lock@value'},
				{name:'lockUserId',mapping:'status>lock@userid'},
				{name:'lockModified',mapping:'status>lock@modified', type: 'date', dateFormat: 'Y-m-d H:i:s.u'},
				{name:'lockMessage',mapping:'status>lock@message'},
				{name:'status', mapping:'status>description'},
				{name:'agendaDate', mapping:'status>agenda>assigned-date', type: 'date', dateFormat: 'm/d/Y'},
				{name:'statusModified',mapping:'status>modified', type: 'date', dateFormat: 'Y-m-d H:i:s.u'}
				
			],
	hasMany: [{
	   	 model:'Clara.Queue.model.QueueItemAssignedReviewer',
		 name:'assignedReviewers',
		 associationKey:'assigned-reviewers',
		 reader: {
			 type:'xml',
			 record:'assigned-reviewer',
			 root:'assigned-reviewers'
		 }
	},{
	   	 model: 'Clara.Queue.model.QueueItemDetail',
		 name: 'itemDetails',
		 associationKey: 'details',
		 reader: {
			 type: 'xml',
			 record: 'value',
			 root:'details'
		 }
	 },{
        model: 'Clara.DetailDashboard.model.FormAction',
        name: 'itemActions',
        associationKey: 'actions',
        reader: {
            type: 'xml',
            record: 'action',
            root:'actions'
        }
    }],
	sorters: [{property:'formId', direction:'DESC'}],
	proxy: {
		type: 'ajax',
	   	url:'',	
	   	actionMethods: {
	        read: 'GET'
	    },
	   	headers:{'Accept':'application/xml;charset=UTF-8'},
		extraParams: {userId: claraInstance.user.id || 0},

		reader: {
			type:'xml',
   		 	record:claraInstance.type+'-form',
   		 	root:'list'
		}
	}
});