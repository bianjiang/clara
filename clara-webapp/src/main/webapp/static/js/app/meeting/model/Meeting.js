Ext.define('Clara.Meeting.model.Meeting', {
    extend: 'Ext.data.Model',
    fields: [{name:'id', mapping: '@id'},
     	    {name:'status', mapping:'@status'},
     	    {name:'notes'},
    	    {name:'startTime', mapping: '@starttime', type:'date', dateFormat:'Y-m-d'},
    	    {name:'endTime', mapping: '@endtime', type:'date', dateFormat:'Y-m-d'}],
    	    
    	    hasMany:[{ 	model:'Clara.Meeting.model.AttendantActivityItem', 
    	    			name:'attendantActivities',
    	    			associationKey:'attendance',
    	    			reader: {
    	        		 type: 'xml',
    	        		 record: 'member',
    	        		 root:'attendance'
    	    			}
    	    		 },
    	             { 
    	    			model:'Clara.Meeting.model.ActivityItem',
    	    			name:'activityItems',
    	    			associationKey:'activity',
    	    			reader: {
    	        		 type: 'xml',
    	        		 record: 'item',
    	        		 root:'activity'
    	    			}
    	             }],
    	             
    	    proxy: {
	        	 type: 'ajax',
	        	 url: appContext+"/ajax/agendas/{agendaId}/load-meeting-xml-data", // changes dynamically
	        	 reader: {
	        		 type: 'xml',
	        		 idProperty: 'id',
	        		 record:'meeting'
	        	 }
	         }
});