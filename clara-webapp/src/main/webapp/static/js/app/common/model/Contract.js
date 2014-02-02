Ext.define('Clara.Common.model.ContractSponsor', {
	extend: 'Ext.data.Model',
	fields: ['name','company','title','department','phone','fax','email','address']
});

Ext.define('Clara.Common.model.ContractAssignedCommittee', {
	extend: 'Ext.data.Model',
	fields: [
				{
					name : 'name',
					mapping : '@name'
				}],
				hasMany: [{
		        	 model: 'Clara.Common.model.ContractAssignedReviewer',
		        	 name: 'assignedReviewers',
		        	 associationKey: 'assigned-reviewers',
		        	 reader: {
		        		 type: 'xml',
		        		 record : 'assigned-reviewer',
						 idProperty:'@user-role-id'
		        	 }
		         }]
		         
});

Ext.define('Clara.Common.model.ContractAssignedReviewer', {
	extend: 'Ext.data.Model',
	fields: [
				{
					name : 'reviewerName',
					mapping : '@user-fullname'
				},{
					name : 'reviewerId',
					mapping : '@user-id'
				},{
					name : 'reviewerRoleId',
					mapping : '@user-role-id'
				},{
					name : 'reviewerRoleName',
					mapping : '@user-role'
				},{
					name : 'assigningCommittee',
					mapping : '@assigning-committee'
				},{
					name : 'userRoleCommittee',
					mapping : '@user-role-committee'
				}]
});

Ext.define('Clara.Common.model.Contract', {
	extend: 'Ext.data.Model',
	requires:['Clara.Common.model.StaffMember'],
	sorters: [{
        property: 'id',
        direction: 'DESC'
    }],
	fields: [
	         {name:'id', mapping:'@id'},
	         {name:'index', mapping:'@id'},
	         {name:'formIndex',mapping:'@index'},
	         {name:'groupedIndex', convert: function(v,r){ return ""+r.get("id")+"-"+Ext.String.leftPad(r.get("formIndex"),5,0);}},		// Provides sortable id for contracts and amendments, since the "id" field is not unique
	         {name:'identifier', mapping:'@identifier', type:'string'},
	         {name:'title', mapping:'title', type:'string'},
	         {name:'created', mapping:'@created',type:'date',dateFormat:'m/d/Y'},
	         {name:'timestamp', mapping:'@timestamp'},
	         {name:'status',mapping:'status'},
	         {name:'priority',mapping:'status@priority'},
	         {name:'contractType',mapping:'@type'},
	         {name:'studyType', mapping:'type'},
	         {name:'contractEntityType',mapping:'type'},
	         {name:'contractEntitySubtype',mapping:'type sub-type:first'},
	         {name:'contractEntityTypeDesc', convert:function(v,r){
	        	 if (!r.get("contractEntityType")) return "";
	        	 return r.get("contractEntityType").toLowerCase()+((r.get("contractEntitySubtype"))?(" ("+r.get("contractEntitySubtype").toLowerCase()+")"):"");
	         }},
	         {name:'studyIdentifier',mapping:'protocol'},
	         {name:'entity',mapping:'@entity'}
	        
	      
	         ],

	         hasMany: [{
	        	 model: 'Clara.Common.model.StaffMember',
	        	 name: 'staffMembers',
	        	 associationKey: 'staffs',
	        	 reader: {
	        		 type: 'xml',
	        		 record: 'staff',
	        		 root:'staffs'
	        	 }
	         },
	         {
	        	 model: 'Clara.Common.model.ContractSponsor',
	        	 name: 'sponsors',
	        	 associationKey: 'sponsors',
	        	 reader: {
	        		 type: 'xml',
	        		 record: 'sponsor',
	        		 root:'sponsors'
	        	 }
	         },
	         {
	        	 model: 'Clara.Common.model.ContractAssignedCommittee',
	        	 name: 'assignedCommittees',
	        	 associationKey: 'committee-review',
	        	 reader: {
	        		 type: 'xml',
	        		 record : 'committee',
					 idProperty:'@name'
	        	 }
	         }
	         ],

	         proxy: {
	        	 type: 'ajax',
	        	 url: appContext+"/ajax/contracts/list.xml", 
	        	 actionMethods: {
	        	        read: 'POST'
	        	 },
	        	 reader: {
	        		 type: 'xml',
	        		 totalProperty:'@total',
	        		 idProperty: 'groupedIndex',
	        		 record:'contract',
	        		 root:'list'
	        	 }
	         }
});

