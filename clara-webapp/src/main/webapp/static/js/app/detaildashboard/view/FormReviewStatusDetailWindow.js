Ext.define('Clara.DetailDashboard.view.FormReviewStatusDetailWindow', {
	extend: 'Ext.window.Window',
	alias: 'widget.formreviewstatusdetailwindow',
	title: 'History',
    width: 550,
    height: 250,
    layout: 'fit',
    style:'z-index:-1;',
    form: null,
    statusRecord: null,
    initComponent: function() {
		var me = this;
	
		me.items = [{
			xtype:'grid',
			border:false,
			loadMask:true,
			store:'Clara.DetailDashboard.store.FormReviewStatusDetails',
			columns:[{
                	header: 'Date', width: 135, dataIndex: 'modifiedDateTime',
                	xtype: 'datecolumn', format: 'm/d/Y h:i A'
	            },
	            {header: 'Status', flex:1, dataIndex: 'protocolFormCommitteeStatus'},
	            {header: 'Note', flex:2, dataIndex: 'note'}]
			}];
		
		me.buttons = [
		 		    {
				        text: 'Close',
				        handler: function(){
							me.close();
				        }
				    }
				];
		me.callParent();
		var st = Ext.data.StoreManager.lookup('Clara.DetailDashboard.store.FormReviewStatusDetails');
    	st.loadFormReviewStatusDetail(me.form,me.statusRecord.get("committee_code"));
	}
});