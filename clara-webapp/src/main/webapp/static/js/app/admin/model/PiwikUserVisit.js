Ext.define('Clara.Admin.model.PiwikUserVisitDetail', {
	extend: 'Ext.data.Model',
	fields: [{name:'type', mapping:'type'},
	         {name:'pageId',mapping:'pageId'},
	         {name:'url', mapping:'url'},
	         {name:'pageTitle', mapping:'pageTitle'},
	         {name:'timePretty', mapping:'serverTimePretty'},
	         {name:'timeSpentPretty', mapping:'timeSpentPretty'},
	         {name:'type', mapping:'type'},
	         {name:'eventCategory'},
	         {name:'eventAction'}
	         
	         ]
});

Ext.define('Clara.Admin.model.PiwikUserVisit', {
	extend: 'Ext.data.Model',
	fields: [{name:'id', mapping:'idVisit'},
	         {name:'ip', mapping:'visitIp'},
	         {name:'username', mapping:'customVariables["1"].customVariableValue1'},
	         {name:'piwikUserId', mapping:'visitorId'},
	         {name:'visitTimestamp',mapping:'firstActionTimestamp'},
	         {name:'serverDate', mapping:'serverDate'},
	         {name:'visitDateTime', mapping : 'serverTimePrettyFirstAction',
	     		convert : function(v, r) {
	     			return r.get("serverDate")+" "+v;
	     		}
	     	 },
	         {name:'visitDuration', mapping:'visitDuration'},
	         {name:'visitDurationPretty', mapping:'visitDurationPretty'},
	         {name:'osName', mapping:'operatingSystemShortName'},
	         {name:'browser', mapping:'browserName'},
	         {name:'provider', mapping:'provider'}
	         
	         
	         ],
	         
	         hasMany: [
	         {
	        	 model: 'Clara.Admin.model.PiwikUserVisitDetail',
	        	 name: 'actionDetails',
	        	 associationKey: 'actionDetails',
	        	 reader: {
	        		 type: 'json'
	        	 }
	         }],

     sorters: [{property:'visitTimestamp', direction:'DESC'}],
     
     proxy: {
    	 type: 'jsonp',
    	 url:'', 
    	 callbackKey:'jsoncallback',
    	 actionMethods: {
             read: 'GET'
         },
    	 headers:{'Accept':'application/json;charset=UTF-8'},
    	 reader:{
    		 idProperty: 'idVisit'
	        	 }
	         }
});
