Ext.define('Clara.Agenda.model.AgendaItemReviewer', {
	extend: 'Ext.data.Model',
	fields: [{name:'name', mapping:'name'}]
});

Ext.define('Clara.Agenda.model.AgendaItemDetail', {
	extend: 'Ext.data.Model',
	fields: [{name:'detailName', mapping:'@name'},{name:'detailValue',mapping:'/'}]
});

Ext.define('Clara.Agenda.model.AgendaItem', {
	extend: 'Ext.data.Model',
	fields: [
	         {name:'id', mapping:'@id'},
	         {name:'category', mapping:'@category'},
	         {name:'xmlTitle', mapping:'xml-data>item>title'},
	         {name:'categoryLongStatusDesc', mapping: '@category', convert: function(enumStatus){
	        	 var st = enumStatus || "Unknown";
	        	 if (st != 'Unknown') st = enumStatus.charAt(0).toUpperCase() + enumStatus.slice(1).toLowerCase().replace(/_/g," ");
	        	 if (st == 'Agenda incomplete') st = "New agenda";
	        	 else if (st == 'Agenda approved') st = "Approved by chair";
	        	 return st;
	         }},
	         {name:'xmlUrl', mapping:'xml-data>item>url'},
	         {name:'protocolFormType', mapping:'protocol-form>protocol-form-type'},
	         {name:'protocolFormTypeId', mapping:'protocol-form>protocol-form-type>@id'},
	         {name:'protocolFormId', mapping:'protocol-form>@id'},
	         {name:'protocolId', mapping:'protocol-form>protocol-meta>protocol>@id'},
	         {name:'protocolTitle', mapping:'protocol-form>protocol-meta>protocol>title'},
	         {name:'studyNature',mapping:'protocol-form>details>study-nature'},
	         {name:'protocolFormStatus', mapping:'protocol-form>protocol-form-meta>status'}

	         ],
	         hasMany: [{
	        	 model: 'Clara.Agenda.model.AgendaItemReviewer',
	        	 name: 'assignedReviewers',
	        	 associationKey: 'reviewers',
	        	 reader: {
	        		 type: 'xml',
	        		 record: 'reviewer',
	        		 root:'reviewers'
	        	 }
	         },
	         {
	        	 model: 'Clara.Agenda.model.AgendaItemDetail',
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
	        	 url: appContext, //changes dynamically
	        	 reader: {
	        		 type: 'xml',
	        		 idProperty: 'id',
	        		 record:'agenda-item',
	        		 root:'list'
	        	 }
	         }
});