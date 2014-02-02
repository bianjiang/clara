Ext.define('Clara.Reports.view.Viewport',{
	extend: 'Ext.container.Viewport',
	requires:['Clara.Reports.view.AddDisplayFieldWindow','Clara.Reports.view.ReportResultsWindow','Clara.Reports.view.UserReportsList','Clara.Reports.view.ReportTypesWindow','Clara.Reports.view.UserReportWindow','Clara.Reports.view.AddCriteriaWindow'],
	layout:'fit',
	defaults:{
		split:true,
		border:false,
		collapsible:false
	},
	initComponent: function(){
		this.items={
				layout:'border',
				items:[{
						dock:'top',
						xtype:'panel',
						contentEl:'clara-header',
						region:'north',
						bodyCls:'background-normal',
						border:0
					},{
						xtype:'userreportslist',
						id:'userReportsListPanel',
						region:'center'
					}]
				
		};
		this.callParent();
	}

});