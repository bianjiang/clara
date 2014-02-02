Ext.define('Clara.Common.model.User', {
	extend: 'Ext.data.Model',
	fields: [	{name:'id'},
	         	{name:'userid',mapping:'userId'},
	         	{name:'sap'},
	         	{name:'department'},
	         	{name:'username'},
	         	{name:'firstname'},
	         	{name:'lastname'},
	         	{name:'email'},
	         	{name:'workphone'}
	         	],
	         	
	         	proxy: {
	         		type: 'ajax',
	         		url: appContext + '/ajax/users/persons/search',
	         		reader: {
	         			type: 'json',
	         			root: 'persons',
	         			idProperty: 'username'
	         		}
	         	}
});