Ext.define('Clara.Meeting.model.Motion', {
    extend: 'Ext.data.Model',
    fields: [{name:'id', mapping: '@id'},
             {name:'timestamp',mapping:'@ts', type:'timestamp'},
             {name:'value', mapping:'@value'},
             {name:'adultRisk', mapping:'@adultrisk'},
             {name:'consentDocumentationWaived', mapping:'@consentdocumentationwaived'},
             {name:'consentWaived', mapping:'@consentwaived'},
             {name:'hipaa', mapping:'@hipaa'},
             {name:'hipaaWaived', mapping:'@hipaawaived'},
             {name:'madeByUserId', mapping:'@mid'},
             {name:'madeByUserName', mapping:'@mname'},
             {name:'ncDetermination', mapping:'@ncdetermination'},
             {name:'ncReportable', mapping:'@ncreportable'},
             {name:'pedRisk', mapping:'@pedrisk'},
             {name:'reviewPeriod', mapping:'@reviewperiod'},
             {name:'reviewType', mapping:'@reviewtype'},
             {name:'secondById', mapping:'@sid'},
             {name:'secondByUserName', mapping:'@sname'}
     	     ],
    	    
     	     hasMany:[{ model:'Clara.Meeting.model.Vote', 
	    			name:'votes',
	    			associationKey:'votes',
	    			reader: {
	        		 type: 'xml',
	        		 record: 'vote',
	        		 root:'votes'
	    			}}]
    	 
});