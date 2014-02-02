Ext.define('Clara.Queue.model.ContractSponsor', {
	extend: 'Ext.data.Model',
	fields: ['name','company','title','department','phone','fax','email','address']
});

Ext.define('Clara.Queue.model.QueueItemReviewer', {
	extend: 'Ext.data.Model',
	fields: [{name:'name', mapping:'name'}]
});

Ext.define('Clara.Queue.model.QueueItemAction', {
    extend: 'Ext.data.Model',
    fields: [{name:'name', mapping:'name'},
        {name:'url', mapping:'url'},
        {name:'assignToRole', mapping:'assign-to-role'}]
});

Ext.define('Clara.Queue.model.QueueItemWarning', {
    extend: 'Ext.data.Model',
    fields: [{name:'warning', mapping:'/'},
        {name:'category', mapping:'@category'}]
});

Ext.define('Clara.Queue.model.QueueItemCommitteeReview', {
	extend: 'Ext.data.Model',
	fields: [{name:'committee', mapping:'@type'},
	         {name:'action', mapping:'action'},
	         {name:'actor', mapping:'actor'}]
});

Ext.define('Clara.Queue.model.QueueItemAssignedReviewer', {
	extend: 'Ext.data.Model',
	idProperty:'@user-role-id',
	fields: [{
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

Ext.define('Clara.Queue.model.QueueItemDetail', {
	extend: 'Ext.data.Model',
	fields: [{name:'detailName', mapping:'@name'},{name:'detailValue',mapping:'/'}]
});

Ext.define('Clara.Queue.model.QueueItemLog', {
	extend: 'Ext.data.Model',
	fields: [{name:'log', mapping:'/'},{name:'time',mapping:'@date-time'},{name:'actor',mapping:'@actor'}]
});

Ext.define('Clara.Queue.model.QueueItem', {
	extend: 'Ext.data.Model',
	requires:['Clara.Common.model.StaffMember'],
	fields: [
	         {   name: 'formId', mapping:'@form-id' },
	         {   name: 'studyNature',mapping:'protocol-form>details>study-nature' },
	         {	 name: 'isMine', mapping : '@is-mine' },
	         {   name: 'irbSuggestedType', mapping:'meta>summary>irb-determination>suggested-type' },
	         {	 name: 'metaType', mapping:'meta>type' },
	         {   name: 'roleId', mapping: '@role-id' },
	         {   name: 'roleName', mapping : '@role-name' },
	         {   name: 'studyIdentifier',mapping:'protocol'},
	         {   name: 'studyType', mapping : 'meta>study-type' },
	         {   name: 'committee', mapping : '@committee' },
	         {   name: 'committeeName', mapping : '@committee-name' },
	         {   name: 'formCommitteeStatus', mapping:'form-committee-status>description' },
	         {   name: 'claraIdentifier', mapping : 'meta@id' },
	         {   name: 'identifier', mapping : 'meta@identifier' },
	         {   name: 'title', mapping: 'meta>title' },
	         {   name: 'contractEntitySubtype', mapping:'type sub-type:first'},
	         
	         {name:'studyNatureDesc', convert:function(v,r){
	        	 var mappings= {
	         			'social-behavioral-education' : 'Social / Behavioral / Education',
	        			'biomedical-clinical' : 'Biomedical / Clinical',
	        			'hud-use' : 'HUD Use for Treatment/Diagnosis',
	        			'chart-review' : 'Chart Review Only'
	        		};
	        	 if (typeof mappings[r.get("studyNature")] != "undefined") return mappings[r.get("studyNature")]+"";
	        	 else return "";
	         }},
	         {name:'studyTypeDesc',convert:function(v,r){
	        	 var mappings= {
	        			'industry-sponsored' : 'Industry Sponsored',
	         			'cooperative-group' : 'Cooperative Group',
	         			'investigator-initiated' : 'Investigator Initiated'
	        		};
	        	 if (typeof mappings[r.get("studyType")] != "undefined") return mappings[r.get("studyType")]+"";
	        	 else return "";
	         }},
 
	         {
	        	 name : 'formType',
	        	 mapping : 'form-type',
	        	 sortType: function(value){
	        		 if (value == "Emergency Use"){
	        			 return 1;
	        		 } else {
	        			 return 10;
	        		 }
	        	 }
	         },
	         {   name : 'formTypeId', mapping : 'form-type@id' },
	         {   name : 'url',  mapping : 'url' },
	         {   name : 'editurl', mapping : 'editurl' },
	         {   name : 'formStatus', mapping : 'form-status>description' },
	         {   name : 'priority', mapping : 'meta>status@priority' },
	         {
	        	 name : 'formStatusModified',
	        	 mapping : 'form-status>modified-at',
	        	 type : 'date',
	        	 dateFormat : 'Y-m-d H:i:s.u'
	         },
	         {
	        	 name : 'formCommitteeStatus',
	        	 mapping : 'form-committee-status>description',
	        	 sortType: function(value){
	        		 if (value == "Potential Non-compliance In Review"){
	        			 return 1;
	        		 } else {
	        			 return 10;
	        		 }
	        	 }
	         },
	         {
	        	 name : 'formCommitteeStatusModified',
	        	 mapping : 'form-committee-status>modified-at',
	        	 type : 'date',
	        	 dateFormat : 'Y-m-d H:i:s.u'
	         }
	         ],
	         hasMany: [{
	        	 model:'Clara.Queue.model.QueueItemCommitteeReview',
	        	 name:'committeeReviews',
	        	 associationKey:'committee-review',
	        	 reader: {
	        		 type:'xml',
	        		 record:'committee',
	        		 root:'committee-review'
	        	 }
	         },{
	        	 model:'Clara.Queue.model.QueueItemAssignedReviewer',
	        	 name:'assignedReviewers',
	        	 associationKey:'assigned-reviewers',
	        	 reader: {
	        		 type:'xml',
	        		 record:'assigned-reviewer',
	        		 root:'assigned-reviewers'
	        	 }
	         },{
                 model: 'Clara.Queue.model.QueueItemAction',
                 name: 'actions',
                 associationKey: 'actions',
                 reader: {
                     type: 'xml',
                     record: 'action',
                     root:'actions'
                 }
             },{
                 model: 'Clara.Queue.model.QueueItemWarning',
                 name: 'itemWarnings',
                 associationKey: 'warnings',
                 reader: {
                     type: 'xml',
                     record: 'warning',
                     root:'warnings'
                 }
             },
	         {
	        	 model: 'Clara.Queue.model.QueueItemReviewer',
	        	 name: 'reviewers',
	        	 associationKey: 'reviewers',
	        	 reader: {
	        		 type: 'xml',
	        		 record: 'reviewer',
	        		 root:'reviewers'
	        	 }
	         },
	         {
	        	 model: 'Clara.Queue.model.QueueItemDetail',
	        	 name: 'itemDetails',
	        	 associationKey: 'details',
	        	 reader: {
	        		 type: 'xml',
	        		 record: 'value',
	        		 root:'details'
	        	 }
	         },
	         {
	        	 model: 'Clara.Queue.model.ContractSponsor',
	        	 name: 'sponsors',
	        	 associationKey: 'sponsors',
	        	 reader: {
	        		 type: 'xml',
	        		 record: 'sponsor',
	        		 root:'sponsors'
	        	 }
	         },
	         {
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
	        	 model: 'Clara.Queue.model.QueueItemLog',
	        	 name: 'logs',
	        	 associationKey: 'latest-logs',
	        	 reader: {
	        		 type: 'xml',
	        		 record: 'log',
	        		 root:'latest-logs'
	        	 }
	         }],

	         proxy: {
	        	 type: 'ajax',
	        	 url: appContext + "/ajax/queues/forms/list.xml",
	        	 reader: {
	        		 type: 'xml',
	        		 record:'form',
	        		 root:'list'
	        	 }
	         }
});