Ext.define('Clara.LetterBuilder.model.Recipient', {
	extend: 'Ext.data.Model',
	fields : [ 'type', 'desc', 'address' ],
	         	
	         	proxy: {
	         		type: 'ajax',
	         		url : appContext + '/ajax/email-templates/recipients/search',
	         		reader: {
	         			type: 'json',
	         			root: 'recipients',
	         			idProperty: 'address'
	         		}
	         	}
});