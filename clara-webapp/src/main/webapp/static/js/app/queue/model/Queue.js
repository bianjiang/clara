Ext.define('Clara.Queue.model.Queue', {
    extend: 'Ext.data.Model',
    fields: [{
		name : 'identifier',
		mapping : '@identifier'
	}, {
		name : 'name',
		mapping : '@name'
	}, {
		name : 'objectType',
		mapping : '@object-type'
	}],
    proxy: {
        type: 'ajax',
        url: appContext + '/ajax/queues/list-user-queues.xml',
        extraParams: {userId: claraInstance.user.id || 0},
        headers:{'Accept':'application/xml;charset=UTF-8'},
        reader: {
            type: 'xml',
			record: 'queue',
			root:'list'
        }
    }
});