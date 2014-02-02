Ext.define('Clara.Super.model.MessagePost', {
	extend : 'Ext.data.Model',
	fields : [{
		name:'id'
	},{
		name:'title'
	},{name:'created', mapping: 'created', type: 'date', dateFormat: 'timestamp', convert:function(v){
		return new Date(v);
	}},
	{name:'expireDate', mapping: 'expireDate', type: 'date', dateFormat: 'timestamp', convert:function(v){
		return new Date(v);
	}},{
		name:'message'
	},{
		name:'messageLevel'
	},],

	sorters : [ {
		property : 'id',
		direction : 'DESC'
	} ],

	proxy : {
		type : 'ajax',
		url : appContext + '/ajax/admin/super/posts/list',
		actionMethods : {
			read : 'GET'
		},
		headers : {
			'Accept' : 'application/json;charset=UTF-8'
		},
		reader : {
			type : 'json',
			root : 'data',
			idProperty : 'id',
			messageProperty:'message'
		},
		actionMethods: {
            read: 'GET',
            update:'POST',
            create:'POST',
            destroy:'POST'
        },
	   	 writer:{
	   		 writeAllFields:true,
	   		 allowSingle:true
	   	 },
	   	 api:{
	   		 'read':appContext + '/ajax/admin/super/posts/list', 
	   		 'create':appContext + '/ajax/admin/super/posts/create', 
	   		 'destroy':appContext + '/ajax/admin/things/delete'
	   	 }
	}
});
