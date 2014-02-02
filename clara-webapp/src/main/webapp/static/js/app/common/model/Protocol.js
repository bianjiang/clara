Ext.define('Clara.Common.model.ProtocolExtraDetail', {
	extend: 'Ext.data.Model',
	fields: [{name:'detailName', mapping:'@name'},{name:'detailValue',mapping:'/'}]
});

Ext.define('Clara.Common.model.Protocol', {
	extend: 'Ext.data.Model',
	requires:['Clara.Common.model.StaffMember'],
	sorters: [{
        property: 'id',
        direction: 'DESC'
    }],
	fields: [
	         {name:'id', mapping:'@id'},
	         {name:'identifier', mapping:'@identifier', type:'string'},
	         {name:'title', mapping:'title', type:'string'},
	         {name:'studyNature', mapping:'study'},
	         {name:'studyNatureDesc', mapping:'study-nature', convert:function(v,r){
	        	 var mappings= {
	         			'social-behavioral-education' : 'Social / Behavioral / Education',
	        			'biomedical-clinical' : 'Biomedical / Clinical',
	        			'hud-use' : 'HUD Use for Treatment/Diagnosis',
	        			'chart-review' : 'Chart Review Only'
	        		};
	        	 if (typeof mappings[v] != "undefined") return mappings[v]+"";
	        	 else return "";
	         }},
	         {name:'studyType', mapping:'study-type'},
	         {name:'studyTypeDesc', mapping:'study-type', convert:function(v,r){
	        	 var mappings= {
	        			 'industry-sponsored' : 'Industry Sponsored',
	         			'cooperative-group' : 'Cooperative Group',
	         			'investigator-initiated' : 'Investigator Initiated'
	        		};
	        	 if (typeof mappings[v] != "undefined") return mappings[v]+"";
	        	 else return "";
	         }},
	         {name:'priority',mapping:'status@priority'},
	         {name:'status',mapping:'status'},
	         
	         {name:'collegeId',mapping:'responsible-department@collegeid'},
	         {name:'collegeDesc',mapping:'responsible-department@collegedesc'},
	         {name:'deptId',mapping:'responsible-department@deptid'},
	         {name:'deptDesc',mapping:'responsible-department@deptdesc'},
	         {name:'subdeptId',mapping:'responsible-department@subdeptid'},
	         {name:'subdeptDesc',mapping:'responsible-department@subdesptdesc'},
	         
	         {name:'submissionType',mapping: '@type'},
	         {name:'formType',mapping: '@type'},

	         {name:'hasClaraBudget', mapping:'budget-created'},
	         {name:'hasCrimsonBudget', mapping:'has-budget'},

	         {name:'budget', mapping: 'protocol', convert: function(v,rec){
	        	 return (rec.get("hasClaraBudget") == 'y')?"CLARA":(rec.get("hasCrimsonBudget") == 'yes')?"CRIMSON":"";
	         }}

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
	        	 model: 'Clara.Common.model.ProtocolExtraDetail',
	        	 name: 'itemDetails',
	        	 associationKey: 'protocol-form>details',
	        	 reader: {
	        		 type: 'xml',
	        		 record: 'value',
	        		 root:'details'
	        	 }
	         }],

	         proxy: {
	        	 type: 'ajax',
	        	 url: appContext+"/ajax/protocols/list.xml", 
	        	 actionMethods: {
	        	        read: 'POST'
	        	 },
	        	 reader: {
	        		 type: 'xml',
	        		 totalProperty:'@total',
	        		 idProperty: 'id',
	        		 record:'protocol',
	        		 root:'list'
	        	 }
	         }
});

