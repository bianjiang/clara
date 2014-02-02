Ext.define('Clara.Super.view.NewsPanel', {
	extend: 'Ext.grid.Panel',
	alias: 'widget.newspanel',
	autoScroll: true,
    border: false,
    stripeRows: true,
    hideHeaders:false,
    store: 'MessagePosts',
	initComponent: function() { 
		this.dockedItems= [{
			dock: 'top',
			border:false,
			xtype: 'toolbar',
			items: [{
				text:'New Post..',
				id:'btnNewPost',
				iconCls:'icn-plus-button',
				handler:function(){
					Ext.create('Clara.Super.view.PostDetailWindow',{}).show();
				}
			}
			]
		}];
		
        this.columns = [
           
            { header:'Created', sortable: true,width:100, dataIndex: 'created',   xtype: 'datecolumn',   format:'m/d/Y'},
            { header:'Expires', sortable: true,width:100,  dataIndex: 'expireDate',   xtype: 'datecolumn',   format:'m/d/Y'},
            { header:'Message Level', width:200,  sortable: false, dataIndex: 'messageLevel'},
            { header:'Title', sortable: false,flex:1, dataIndex: 'title'},
            {
                xtype: 'actioncolumn',
                width: 50,
                items: [
                    {
                        icon:appContext + '/static/images/icn/cross.png',
                        tooltip: 'Delete post',
                        handler: function(grid, rowIndex, colIndex) {
                            var rec = grid.getStore().getAt(rowIndex);
                            var c = confirm("Really delete this post?");
                            if (c){
                            	jQuery.ajax({
                            		url:appContext +"/ajax/admin/super/posts/"+rec.get("id")+"/remove",
                            		type: "GET",
                            		async: false,
                        			dataType:'json',
                            		success: function(data){
                            			clog("Removed post .. Reloading store.");
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