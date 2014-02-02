Ext.define('Clara.DetailDashboard.view.FormDetailPanel',{
	extend: 'Ext.Panel',
	alias: 'widget.formdetailpanel',
	border:false,
	layout:'border',
	type:null,
	requires:['Clara.DetailDashboard.view.FormGridPanel','Clara.DetailDashboard.view.FormActionContainer','Clara.DetailDashboard.view.FormReviewStatusPanel','Clara.Review.view.ReviewNotePanel'],
	initComponent: function(){
		var me = this;
		me.listeners = {
			afterrender:function(p){
				var st = Ext.data.StoreManager.lookup('Clara.DetailDashboard.store.Forms');
				st.loadForms();
			}
		};
		
		me.items=[{
			xtype:'formgridpanel',
			region:'center',
			split:true
		},{
			xtype:'tabpanel',
			id:'formDetailTabPanel',
			region:'east',
			width:350,
			split:true,
			disabled:true,
			items:[{
				xtype:'formactioncontainer',
				title:'Actions',
				iconCls:'icn-ui-buttons'
			},{
				xtype:'formreviewstatuspanel',
				title:'Review Status',
				iconCls:'icn-dashboard'
			},{
				xtype:'reviewnotepanel',
				title:'Review Notes',
				iconCls:'icn-sticky-notes-pin',
				hidden:(me.type === 'contract')
			}]
		}];
		me.callParent();
	}

	
});