Ext.define('Clara.Common.model.MessagePost', {
	extend : 'Ext.data.Model',
	fields : [{
		name:'id'
	},{
		name:'title'
	},{name:'created', mapping: 'date', type:'date', dateFormat:'Y-m-d'},
	{name:'expireDate', mapping: 'date', type:'date', dateFormat:'Y-m-d'},{
		name:'message'
	},{
		name:'messageLevel'
	}],

	sorters : [ {
		property : 'id',
		direction : 'DESC'
	} ],
	

	proxy : {
		type : 'ajax',
		url : appContext + '/ajax/posts/list-current',
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
	   	 api:{
	   		 'read':appContext + '/ajax/posts/list-current'
	   	 }
	}
	

	
	
	
});
