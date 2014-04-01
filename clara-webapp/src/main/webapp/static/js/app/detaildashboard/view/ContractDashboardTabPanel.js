Ext.define('Clara.DetailDashboard.view.ContractDashboardTabPanel',{
	extend: 'Ext.tab.Panel',
	alias: 'widget.contractdashboardtabpanel',
	border:false,
	requires:['Clara.DetailDashboard.view.HistoryPanel','Clara.DetailDashboard.view.LetterPanel','Clara.DetailDashboard.view.FormDetailPanel','Clara.Documents.view.DocumentPanel'],
	initComponent: function(){
		this.activeTab = 0;
		this.items=[{
			xtype:'formdetailpanel', 
			title:'Forms',
			iconCls:'icn-application-form',
			type:'contract'
		},{
			xtype:'documentpanel', 
			title:'Documents',
			iconCls:'icn-folder-open-document-text'
		},{
			xtype:'historypanel',
			title:'History',
			iconCls:'icn-clock-history',
			type:'contract'
		},{
			xtype:'relatedcontractpanel',
			title:'Related Contracts',
			iconCls:'icn-document-tree'
		},{
			xtype:'relatedprotocolpanel',
			title:'Related Protocols',
			iconCls:'icn-document-tree'
		}];
		this.callParent();
	}

	
});