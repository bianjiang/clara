Ext.define('Clara.Review.model.ReviewNoteReply', {
	extend: 'Ext.data.Model',
	fields: [
	         {name:'id', mapping:'id'},
	         {name:'committee', mapping:'committee', type:'string'},
	         {name:'committeeDescription'},
	         {name:'timestamp', mapping:'@timestamp', type:'timestamp'},
	         {name:'modified', mapping:'modifiedDate', type: 'date', dateFormat: 'm/d/Y H:i:s'},
	         {name:'userId', mapping:'userId'},
	         {name:'userFullname', mapping:'userFullname', type:'string'},
	         {name:'text', mapping:'text', type:'string'}
	         ]
});

Ext.define('Clara.Review.model.ReviewNote', {
	extend: 'Ext.data.Model',
	fields: [
	         {name:'id', mapping:'id'},
	         {name:'committee', mapping:'committee'},
	         {name:'committeeDescription'},
	         {name:'modified', mapping:'modifiedDate', type: 'date', dateFormat: 'm/d/Y H:i:s'},
	         {name:'userFullname', mapping:'userFullname'},
	         {name:'userId', mapping:'userId'},
	         {name:'text', mapping:'text'},
	         {name:'timestamp', mapping:'@timestamp', type:'timestamp'},
	         {name:'commentType', mapping:'commentType'},
	         {name:'inLetter', mapping:'inLetter'},
	         {name:'isPrivate'},
	         {name:'commentStatus', mapping:'commentStatus'}
	         ],

	         hasMany: [{
	    	   	 model:'Clara.Review.model.ReviewNoteReply',
	    		 name:'replies',
	    		 associationKey:'children',
	    		 reader: {
	    			 type:'json',
	    			 root:'children'
	    		 }
	    	}],

	         sorters: [{property:'id', direction:'DESC'}],
	         proxy: {
	        	 type: 'ajax',
	        	 url:'',	
	        	 actionMethods: {
	        		 read: 'GET'
	        	 },
	        	 headers:{'Accept': 'application/json'},
	        	 extraParams: {
					userId:claraInstance.user.id,
					committee:claraInstance.user.committee
	        	 },

	        	 reader: {
	        		 type:'json',
	        		 idProperty:'id'
	        	 }
	         }
});