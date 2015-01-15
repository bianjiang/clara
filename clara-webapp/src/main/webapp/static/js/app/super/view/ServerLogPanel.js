Ext.define('Clara.Super.view.ServerLogPanel', {
	extend: 'Ext.grid.Panel',
	alias: 'widget.serverlogpanel',
	autoScroll: true,
    border: false,
    stripeRows: true,
    hideHeaders:false,
    emptyText:'TODO: SEVERE log entries here.',
	viewConfig: { deferEmptyText:false},
    // store: 'ServerLogs',
	initComponent: function() { 
		this.dockedItems= [{
			dock: 'top',
			border:false,
			xtype: 'toolbar',
			items: ['->','-',{
		        xtype: 'textfield',
		        name: 'ipaddress',
		        id:'fldpiwikipaddress',
		        fieldLabel:'<strong>Piwik Search</strong>',
		        emptyText:'IP Address',
		        allowBlank: false  // requires a non-empty value
		    },{
				id:'btnSearchPiwik',
				iconCls:'icn-arrow',
				handler:function(){
					var ipaddr = Ext.getCmp("fldpiwikipaddress").getValue();
					if (ipaddr && jQuery.trim(ipaddr) != ""){
						Ext.create("Clara.Admin.view.UserVisitHistoryWindow", { ipAddress:ipaddr }).show();
					}
				}
			}
			]
		}];
		
        this.columns = [
           
            { header:'Time', sortable: true,width:150, dataIndex: 'logtime',   xtype: 'datecolumn',   format:'m/d/Y'},
            { header:'IP', sortable: true,width:100,  dataIndex: 'logip',   xtype: 'datecolumn',   format:'m/d/Y'},
            { header:'Level', width:80,  sortable: false, dataIndex: 'loglevel'},
            { header:'Message', sortable: false,flex:1, dataIndex: 'logmessage'},
            {
                xtype: 'actioncolumn',
                width: 50,
                items: [
                    {
                        icon:appContext + '/static/images/icn/cross.png',
                        tooltip: 'View PIWIK Activity',
                        handler: function(grid, rowIndex, colIndex) {
                            var rec = grid.getStore().getAt(rowIndex);
                            clog("Show piwik for",rec);
                        }
                    }
                    
                ]
            }
        ];
		this.callParent();
		
	}
});