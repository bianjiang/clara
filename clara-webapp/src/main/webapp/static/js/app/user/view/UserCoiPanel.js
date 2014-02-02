Ext.define('Clara.User.view.UserCoiPanel', {
	extend: 'Ext.grid.Panel',
	alias: 'widget.usercoipanel',
	autoScroll: true,
    border: false,
    stripeRows: true,
    viewConfig:{
		emptyText:'No COI information found in ClickCommerce for this user.'
	},
	initComponent: function() { 
        this.columns = [
            {header: 'Desc', sortable: true, dataIndex: 'disclosureName'},
			{header: 'Submitted', sortable: true, dataIndex: 'discDateLastSubmitted', xtype: 'datecolumn', format: 'm/d/Y'},
			{header: 'Value', sortable: true, dataIndex: 'disclosureStates',width:190},
			{header: 'Expires', sortable: true, dataIndex: 'expirationDate', xtype: 'datecolumn', format: 'm/d/Y'}
        ];
		
		this.store = new Ext.data.Store({
			autoLoad: true,
			header :{
		           'Accept': 'application/json'
		       },
			proxy: new Ext.data.HttpProxy({
				url: appContext + '/ajax/users/'+profile.id+'/coi/list',
				method:'GET',
				reader: {
					type:'json',
					root:'data',
					idProperty: 'id'
				}
			}),
			
			fields:[
					{name:'id'},
					{name:'discDateLastSubmitted', type: 'date',convert: function(v,rec){
						return new Date(v);
					}},
					{name:'expirationDate', type: 'date',convert: function(v,rec){
						return new Date(v);
					}},
					{name:'nameOfCurriculum'},
					{name:'disclosureStates'},
					{name:'disclosureName'},
					{name:'sapId'},
					{name:'firstName'},
					{name:'lastName'}
			        ]
			
		});
        
     
		this.callParent();
	}
});