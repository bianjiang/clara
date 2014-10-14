Ext.define('Clara.DetailDashboard.view.ProtocolDashboardTabPanel',{
	extend: 'Ext.tab.Panel',
	alias: 'widget.protocoldashboardtabpanel',
	border:false,
	requires:['Clara.DetailDashboard.view.related.ProjectPanel','Clara.DetailDashboard.view.HistoryPanel','Clara.DetailDashboard.view.LetterPanel','Clara.DetailDashboard.view.FormDetailPanel','Clara.Documents.view.DocumentPanel'],
	initComponent: function(){
		this.activeTab = 2;
		this.items=[{
			xtype:'panel',
			title:'Overview',
			iconCls:'icn-book',
			border:false,
			html:'<iframe style="overflow:auto;width:100%;height:100%;" frameborder="0" id="protocolOverview" src="'
				+ appContext
				+ '/protocols/'
				+ claraInstance.id
				+ '/summary?noheader=true"></iframe>'
		},{
			xtype:'documentpanel', 
			title:'Documents',
			iconCls:'icn-folder-open-document-text'
		},{
			xtype:'formdetailpanel', 
			title:'Forms',
			iconCls:'icn-application-form'
		},{
			xtype:'letterpanel', 
			title:'Letters',
			iconCls:'icn-mails'
		},{
			xtype:'historypanel',
			title:'History',
			iconCls:'icn-clock-history'
		},{
			xtype:'relatedcontractpanel',
			title:'Related Contracts',
			iconCls:'icn-document-tree'
		},{
			xtype:'relatedprojectpanel',
			title:'Related Projects',
			iconCls:'icn-document-tree'
		}];
		this.callParent();
	}

	
});