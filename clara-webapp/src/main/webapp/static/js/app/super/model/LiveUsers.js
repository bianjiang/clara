Ext.define('Clara.Super.model.LiveUsers', {
	extend : 'Ext.data.Model',
	fields : [ {
		name : 'id'
	}, {
		name : 'username'
	},{
		name: 'data'
	},{
        name: 'sessionId',
        convert: function(value, record) {
        	
            return record.get('data')[0]['sessionId'];
        }
    },{
        name: 'isExpired',
        convert: function(value, record) {
            return record.get('data')[0]['isExpired'];
        }
    } 
	,{
        name: 'lastRequest',
        convert: function(value, record) {
            return record.get('data')[0]['lastRequest'];
        }
    } ],

	sorters : [ {
		property : 'id',
		direction : 'DESC'
	} ],

	proxy : {
		type : 'ajax',
		url : appContext + '/ajax/admin/super/live-users/list',
		actionMethods : {
			read : 'POST'
		},
		headers : {
			'Accept' : 'application/json;charset=UTF-8'
		},
		reader : {
			type : 'json',
			root : 'data',
			idProperty : 'id',
			messageProperty:'message'
		}
	}
});
