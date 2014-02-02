Ext.define('Clara.Admin.view.UserRolesWindow', {
	extend: 'Ext.window.Window',
	alias: 'widget.userroleswindow',
	layout: 'fit',
	width:450,
	height:400,
	padding: 6,
    modal: true,
    user:{},
    initComponent: function() {
    	var t = this;
    	var roleStore = Ext.data.StoreManager.lookup('UserRoles');
    	t.title = "Roles for "+t.user.get("username");
		this.dockedItems = [{
			dock: 'top',
			xtype: 'toolbar',
	    		items:['->',
	    		       {
	           	    	id: 'btnAddUserRole',
		           	 	iconCls:'icn-plus-circle',
		           	 	text: 'Add Role..',
		           	 	disabled:false,
		           	 	handler: function(){
	    					var wRole = Ext.create("Clara.Admin.view.AddRoleWindow",{user:t.user}).show().alignTo(t.getId(), "tl");
	    				}
		           	 }
		           	]
		}];
    	
		this.buttons = [
		    {
		        text: 'Close',
		        handler: function(){
		        	t.close();
		        }
		    }
		];
        this.items = [{
        	xtype:'grid',
        	border:false,
        	store:roleStore,
        	columns: [
			            {header: 'Role', sortable: true, dataIndex: 'roledesc', id:'user-role',flex:1,renderer:function(value, p, record){
			            	clog(record);
			        		var rdesc = record.data.roledesc + ((record.data.roledeptlevel != null&&record.data.delegate)?'&nbsp;(Delegate)':'');
			        		var html = "<h1 class='admin-user-role-row user-role-desc'>"+rdesc + "</h1>";
			        		html = html + "<h3 class='admin-user-role-row user-role-depts'>"+((record.data.collegename != null)?record.data.collegename:"");
			        		html = html + ((record.data.deptname != null)?(" - "+record.data.deptname):"");
			        		html = html +((record.data.subdeptname != null)?(" - "+record.data.subdeptname):"")+"</h3>";
			        		return html;
			            }},{
			            	   
			    			xtype:'actioncolumn',
			    			header:'',
			    			items:[{
			    				iconCls:'icn-minus-circle',
			    				icon:appContext+'/static/images/icn/minus-circle.png',
			    				tooltip:'Remove this role',
			    				handler: function(grid,rowIndex,colIndex){
			    					var rec = grid.getStore().getAt(rowIndex);
			    					jQuery.ajax({
			    						url: appContext + "/ajax/users/"+t.user.get("userid")+"/user-roles/"+rec.get("id")+"/delete",
			    						type: "GET",
			    						async: false,
			    						success: function(data){
			    							t.fireEvent('userroleremoved', t.user);
			    						}
			    					});
			    				}
			    			}],
			    			width:50
			    		
			            }
			        ]
        }];
        
        roleStore.loadUserRoles(t.user.get("userid"));
        
        this.callParent();
    }
});