Ext.define('Clara.Documents.model.DocumentTypeFilter', {
	extend: 'Ext.data.Model',
	fields: [{name:'committee', mapping:'@committee'},{name:'status', mapping:'@status'},{name:'visible', mapping:'@visible'},{name:'sort',mapping:'@sort-value'}]
});

Ext.define('Clara.Documents.model.DocumentType', {
	extend: 'Ext.data.Model',
	fields: [
				{name:'doctype', mapping:'@value'},
				{name:'category', mapping:'@category'},
				{name:'desc', mapping:'@desc'},
				{name:'descCls', mapping:'@descCls'},
				{name:'canRead', mapping:'@read'},
				{name:'canWrite', mapping:'@write'},
				{name:'canUpdate', mapping:'@update'},
				{name:'protocol', mapping:'@protocol'},
				{name:'contract', mapping:'@contract'}, 
				{name:'sortOrder', mapping:'@default-sort'}

	         ],
	         hasMany: [{
	        	 model: 'Clara.Documents.model.DocumentTypeFilter',
	        	 name: 'filters',
	        	 associationKey: 'filters',
	        	 reader: {
	        		 type: 'xml',
	        		 record: 'filter',
	        		 root:'filters'
	        	 }
	         }],

	         proxy: {
	        	 type: 'ajax',
	        	 url: '',
	        	 reader: {
	        		 type: 'xml',
	        		 record:'document-type',
	        		 root:'document-types'
	        	 }
	         }
});