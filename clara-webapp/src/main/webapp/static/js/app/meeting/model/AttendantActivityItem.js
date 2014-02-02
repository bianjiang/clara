Ext.define('Clara.Meeting.model.AttendantActivityItem', {
    extend: 'Ext.data.Model',
    fields: [{name:'firstName', mapping: '@fname'},
             {name:'lastName', mapping:'@lname'},
             {name:'meetingStatus', mapping:'@meetingstatus'},
             {name:'userId', mapping:'@uid'}
     	     ],
    	    
     	     hasMany:[{ model:'Clara.Meeting.model.MemberStatus', 
	    			name:'statuses',
	    			associationKey:'statuses',
	    			reader: {
	        		 type: 'xml',
	        		 record: 'status',
	        		 root:'statuses'
	    			}}]
    	 
});