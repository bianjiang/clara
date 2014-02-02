Ext.define('Clara.DetailDashboard.model.FormReviewStatusDetail', {
	extend: 'Ext.data.Model',
	proxy: new Ext.data.HttpProxy({
		type: 'ajax',
	   	url:'',
	   	actionMethods: {
	        read: 'GET'
	    },
		headers:{'Accept': 'application/json'},
		reader: {
			type:'json',
			idProperty: 'id'
		}
		
	}),
    fields:[{name:'protocolFormCommitteeStatus', mapping:claraInstance.type+'FormCommitteeStatus'},
			{name:'note'},
			{name:'modifiedDateTime', type:'date', dateFormat:'m/d/Y H:i:s'}]
});