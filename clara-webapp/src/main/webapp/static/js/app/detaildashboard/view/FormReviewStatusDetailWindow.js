Ext.define('Clara.DetailDashboard.view.FormReviewStatusDetailWindow', {
	extend: 'Ext.window.Window',
	alias: 'widget.formreviewstatusdetailwindow',
	title: 'Review Status Details',
    width: 550,
    height: 250,
    layout: 'fit',
    style:'z-index:-1;',
    form: null,
    statusRecord: null,
    initComponent: function() {
		var me = this;
	
		me.items = {
				xtype:'tabpanel',
				border:false,
				items:[{
					xtype:'grid',
					title:'History',
					border:false,
					loadMask:true,
					store:'Clara.DetailDashboard.store.FormReviewStatusDetails',
					columns:[{
		                	header: 'Date', width: 135, dataIndex: 'modifiedDateTime',
		                	xtype: 'datecolumn', format: 'm/d/Y h:i A'
			            },
			            {header: 'Status', flex:1, dataIndex: 'protocolFormCommitteeStatus'},
			            {header: 'Note', flex:2, dataIndex: 'note'}]
					},{
						xtype:'grid',
						title:'Completed Checklists',
						hidden:!(claraInstance.HasAnyPermissions(['ROLE_IRB_OFFICE','ROLE_SYSTEM_ADMIN'])),
						border:false,
						loadMask:true,
						store:'Clara.DetailDashboard.store.FormReviewCompletedChecklists',
						columns:[{
			                	header: 'Date', width: 135, dataIndex: 'modified',
			                	xtype: 'datecolumn', format: 'm/d/Y h:i A'
				            },
				            {header: 'User', flex:1, dataIndex: 'actor'},
				            {header: '', flex:2, dataIndex: 'url', renderer: function(v){
				            	return "<a target='_blank' href='"+appContext+v+"'>View</a>";
				            }}]
					}]
		};
		
		me.buttons = [
		 		    {
				        text: 'Close',
				        handler: function(){
							me.close();
				        }
				    }
				];
		me.callParent();
		var st = Ext.data.StoreManager.lookup('Clara.DetailDashboard.store.FormReviewStatusDetails'),
			clst = Ext.data.StoreManager.lookup('Clara.DetailDashboard.store.FormReviewCompletedChecklists');
		
    	st.loadFormReviewStatusDetail(me.form,me.statusRecord.get("committee_code"));
    	clst.loadChecklists(me.form,me.statusRecord.get("committee_code"));
	}
});