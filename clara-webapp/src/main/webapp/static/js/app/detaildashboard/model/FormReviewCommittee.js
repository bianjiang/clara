Ext.define('Clara.DetailDashboard.model.FormReviewCommittee', {
	extend: 'Ext.data.Model',
	proxy: new Ext.data.HttpProxy({
		type: 'ajax',
	   	url:'',
	   	actionMethods: {
	        read: 'GET'
	    },
		headers:{'Accept':'application/xml;charset=UTF-8'},
		reader: {
			type:'xml',
   		 	record:'committee',
   		 	root:'committees'
		}
	}),
    fields:[{name:'name',mapping:'@name'},{name:'desc',mapping:'@desc'}],
    autoLoad:true,
    record:"committee",
    sorters: [{property:'desc', direction:'ASC'}]
});