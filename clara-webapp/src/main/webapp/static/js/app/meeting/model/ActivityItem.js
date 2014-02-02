Ext.define('Clara.Meeting.model.ActivityItem', {
    extend: 'Ext.data.Model',
    fields: [{name:'id', mapping: '@id'},
             {name:'timestamp',mapping:'@ts', type:'timestamp'},
             {name:'agendaItemId', mapping:'@agendaitemid'},
             {name:'protocolId', mapping:'@protocolid'},
             {name:'protocolFormId', mapping:'@protocolformid'},
     	     {name:'letterSent', mapping:'@lettersent', type:'boolean'},
     	     {name:'type'},
     	     {name:'notes'}
     	     ],
    	    
     	     hasMany:[{ model:'Clara.Meeting.model.Motion', 
	    			name:'motions',
	    			associationKey:'motions',
	    			reader: {
	        		 type: 'xml',
	        		 record: 'motion',
	        		 root:'motions'
	    			}}]
    	 
});