Ext.define('Clara.Admin.view.LockedFormsPanel', {
	extend: 'Ext.grid.Panel',
	alias: 'widget.adminlockedformspanel',
	autoScroll: true,
    border: false,
    stripeRows: true,
    hideHeaders:false,
    store: 'LockedForms',
	initComponent: function() { 
        this.columns = [
            { header:'User', sortable: true, dataIndex: 'username'},
            { header:'Type', sortable: true, dataIndex: 'oType'},
            { header:'ID', sortable: true, dataIndex: 'objectid'},
            { header:'Form', sortable: true, dataIndex: 'formType',flex:1},
            { header:'DateTime', sortable:true, dataIndex: 'modifedTime',width:150},
            {
                xtype: 'actioncolumn',
                width: 50,
                items: [
                    {
                        icon:appContext+'/static/images/icn/lock-unlock.png',
                        tooltip: 'Unlock (close) form',
                        handler: function(grid, rowIndex, colIndex) {
                            var rec = grid.getStore().getAt(rowIndex);
                            jQuery.ajax({
                        		url:appContext + "/ajax/users/"+rec.get("userid")+"/close-open-form",
                        		data:{
                        			type:rec.get("oType"),
                        			formId:rec.get("formid")
                        		},
                        		type: "POST",
                        		async: false,
                    			dataType:'xml',
                        		success: function(data){
                        			clog("Unlocked form. Reloading store.");
                        			grid.getStore().load();
                        		}
                        	});
                        }
                    }
                ]
            }
        ];
		this.callParent();
		
	}
});