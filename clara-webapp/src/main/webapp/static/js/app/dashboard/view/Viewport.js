Ext.define('Clara.Dashboard.view.Viewport',{
	extend: 'Ext.container.Viewport',
	border:false,
	requires:['Clara.Dashboard.view.CreateSubmissionWindow','Clara.Dashboard.view.ProtocolGridPanel','Clara.Dashboard.view.ContractGridPanel','Clara.Dashboard.view.BookmarkPanel','Clara.Dashboard.view.BookmarkWindow'],
	layout:'border',
	defaults:{
		split:true,
		border:false,
		collapsible:false
	},
	initComponent: function(){
		this.items=[{
			dock:'top',
			xtype:'container',
			region:'north',
			split:false,
			contentEl:'clara-header',
			bodyCls:'background-normal',
			border:0
		},{
			xtype:claraInstance.type+'gridpanel',
			region:'center',
			split:false,
			id:'dashboardgridpanel'
		},{
			xtype:'bookmarkpanel',
			region:'west',
			split:true,
			width:200
		}];
		this.callParent();
	}

	
});