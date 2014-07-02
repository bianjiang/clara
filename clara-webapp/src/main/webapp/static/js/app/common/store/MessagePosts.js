Ext.define('Clara.Common.store.MessagePosts', {
    extend: 'Ext.data.Store',
	requires: 'Clara.Common.model.MessagePost',    
    model: 'Clara.Common.model.MessagePost',
    autoLoad: true,
	sorters : [ {
		property : 'messageLevel',
		direction : 'DESC'	// "SEVERE", "MODERATE", "INFO"
	},{
		property : 'id',
		direction : 'DESC'
	} ]
});