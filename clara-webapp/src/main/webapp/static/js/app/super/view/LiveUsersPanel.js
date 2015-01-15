Ext.define('Clara.Super.view.LiveUsersPanel', {
	extend: 'Ext.grid.Panel',
	alias: 'widget.liveuserspanel',
	autoScroll: true,
    border: false,
    stripeRows: true,
    hideHeaders:false,
    store: 'LiveUsers',
	initComponent: function() { 
        this.columns = [
            { header:'User Id', sortable: true, dataIndex: 'id'},
            { header:'Username', sortable: true, dataIndex: 'username'},
            { header:'Session Id', width: 350, sortable: false, dataIndex: 'sessionId'},
            { header:'isExpired', sortable: false, dataIndex: 'isExpired'},
            { header:'Last Request', width: 250, sortable: false, dataIndex: 'lastRequest'},
            {
                xtype: 'actioncolumn',
                width: 50,
                items: [{
                    icon:appContext + '/static/images/icn/user-thief.png',
                    tooltip: 'Impersonate',
                    handler: function(grid, rowIndex, colIndex) {
                        var rec = grid.getStore().getAt(rowIndex);
                        
                        window.location = "/clara-webapp/super/j_spring_security_switch_user?j_username=" + rec.get("username");
                        
                    }
                },{
                	icon:appContext+'/static/images/icn/dashboard.png',
                	tooltip:'View user activity',
                	handler: function(grid,rowIndex,colIdx){
                		var rec = grid.getStore().getAt(rowIndex);
                		Ext.create("Clara.Admin.view.UserVisitHistoryWindow", { user:rec }).show();
                	}
                },
                    {
                        icon:appContext + '/static/images/icn/cross.png',
                        tooltip: 'Kill user session',
                        handler: function(grid, rowIndex, colIndex) {
                            var rec = grid.getStore().getAt(rowIndex);
                            var c = confirm("Really kill this user's session?");
                            if (c){
                            	jQuery.ajax({
                            		url:appContext + "/ajax/admin/super/live-users/"+rec.get("id")+"/kill",
                            		data:{
                            			//type:rec.get("oType"),
                            			//formId:rec.get("formid")
                            		},
                            		type: "POST",
                            		async: false,
                        			dataType:'json',
                            		success: function(data){
                            			clog("Killed user .. Reloading store.");
                            			Ext.Msg.alert('Serever response', data.message);
                            			grid.getStore().load();
                            		}
                            	});
                            }
                            
                        }
                    }
                    
                ]
            }
        ];
		this.callParent();
		
	}
});