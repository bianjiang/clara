Ext.define('Clara.Queue.model.AssignedReviewer', {
    extend: 'Ext.data.Model',
    fields: [{
		name : 'userid',
		mapping : '@user-id'
	},{
		name : 'userroleid',
		mapping : '@user-role-id'
	},{
		name : 'userrole',
		mapping : '@user-role'
	}, {
		name : 'name',
		mapping : '@user-fullname'
	}, {
		name : 'committee',
		mapping : '@user-role-committee'
	}],
    proxy: {
        type: 'ajax',
        url: appContext, 	//set dynamically
        headers:{'Accept':'application/xml;charset=UTF-8'},
        reader: {
            type: 'xml',
			record: 'assigned-reviewer',
			root:'data'
        }
    }
});