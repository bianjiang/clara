Ext.define('Clara.DetailDashboard.model.History', {
	extend: 'Ext.data.Model',
	fields: [{name:'id', mapping:'@id'},
			    {name:'desc', mapping:'/'},
				{name:'actor', mapping:'@actor'},
				{name:'formId', mapping:'@form-id'},
				{name:'formType', mapping:'@form-type'},
				{name:'formTypeDesc', mapping:'@form-type', convert:function(v){return stringToHumanReadable(v);}},
				{name:'parentFormId', mapping:'@parent-form-id'},
				{name:'eventType', mapping:'@event-type'},
				{name:'letterType',mapping: '@email-template-identifier'},
				{name:'timestamp', mapping:'@timestamp'},
				{name:'datetime', mapping:'@date-time'}
			],
	
	proxy: {
		type: 'ajax',
	   	url:'', // appContext+"/ajax/history/history.xml?id="+claraInstance.id+"&type="+objType,	
	   	actionMethods: {
	        read: 'GET'
	    },
	   	headers:{'Accept':'application/xml;charset=UTF-8'},
		extraParams: {userId: claraInstance.user.id || 0},

		reader: {
			type:'xml',
   		 	record:'log',
   		 	root:'logs',
   		 	idProperty:'@id'
		}
	}
});