Ext.define('Clara.Common.model.FundingSource', {
	extend: 'Ext.data.Model',
	fields: [	{name:'id'},
	    		{name:'name',mapping:'value'}
	         	],
	         	
	         	proxy: {
	         		type: 'ajax',
	         		url: appContext + '/ajax/protocols/protocol-forms/new-submission/sponsors/search',
	         		reader: {
	         			type: 'json',
	         			root: 'sponsors',
	         			idProperty: 'id'
	         		}
	         	}
});