Ext.define('Clara.Common.model.UserStudyRole', {
	extend: 'Ext.data.Model',
	fields: [{name:'name', mapping:'/'}]
});

Ext.define('Clara.Common.model.UserStudyCost', {
	extend: 'Ext.data.Model',
	fields: [{name:'name', mapping:'/'}]
});

Ext.define('Clara.Common.model.UserStudyResponsibility', {
	extend: 'Ext.data.Model',
	fields: [{name:'name', mapping:'/'}]
});

Ext.define('Clara.Common.model.StaffMember', {
	extend: 'Ext.data.Model',
	isPI: function(){
		var me = this;
		var isPI = false;
		var roles = me.roles();
		roles.each(function(r){
			if (r.get("name") == "Principal Investigator") isPI = true;
		});
		return isPI;
	},
	fields: [	
	         {name:'id'},
	         {name:'userId',mapping:'user@id'},
	         {name:'userSapId',mapping:'user@sap'},
	         {name:'userPhone',mapping:'user@phone'},
	         {name:'userFirstName',mapping:'user>firstname'},
	         {name:'userLastName',mapping:'user>lastname'},
	         {name:'userEmail',mapping:'user>email'},
	         {name:'conflictOfInterest',mapping:'user>conflict-of-interest'},
	         {name:'conflictOfInterestDescription',mapping:'user>conflict-of-interest-description'},
	         {name:'receiveNotifications',mapping:'notify'},
	         {name:'username'},
	         {name:'firstname'},
	         {name:'lastname'},
	         {name:'email'},
	         {name:'workphone'}
	         ],

	         hasMany: [{
	        	 model: 'Clara.Common.model.UserStudyCost',
	        	 name: 'costs',
	        	 associationKey: 'user>costs',
	        	 reader: {
	        		 type: 'xml',
	        		 record: 'cost',
	        		 root:'costs'
	        	 }
	         },{
	        	 model: 'Clara.Common.model.UserStudyRole',
	        	 name: 'roles',
	        	 associationKey: 'user>roles',
	        	 reader: {
	        		 type: 'xml',
	        		 record: 'role',
	        		 root:'roles'
	        	 }
	         },
	         {
	        	 model: 'Clara.Common.model.UserStudyResponsibility',
	        	 name: 'responsibilities',
	        	 associationKey: 'user>responsibilities',
	        	 reader: {
	        		 type: 'xml',
	        		 record: 'responsibility',
	        		 root:'responsibilities'
	        	 }
	         }],


});