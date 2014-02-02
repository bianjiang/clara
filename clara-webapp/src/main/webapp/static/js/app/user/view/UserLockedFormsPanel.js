Ext.define('Clara.User.view.UserLockedFormsPanel', {
	extend: 'Ext.grid.Panel',
	alias: 'widget.userlockedformspanel',
	autoScroll: true,
    border: false,
    stripeRows: true,
    hideHeaders:true,
    store: 'LockedForms',
	initComponent: function() { 
        this.columns = [
            { header:'Type', sortable: true, dataIndex: 'oType'},
            { header:'ID', sortable: true, dataIndex: 'objectid'},
            { header:'Form', sortable: true, dataIndex: 'formType',width:400},
            { header:'DateTime', sortable:true, dataIndex: 'modifedTime'},
            {
                xtype: 'actioncolumn',
                width: 50,
                items: [
                    {
                        icon:'../../static/images/icn/lock-unlock.png',
                        tooltip: 'Unlock (close) form',
                        handler: function(grid, rowIndex, colIndex) {
                            var rec = grid.getStore().getAt(rowIndex);
                            jQuery.ajax({
                        		url:appContext + "/ajax/users/"+profile.id+"/close-open-form",
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