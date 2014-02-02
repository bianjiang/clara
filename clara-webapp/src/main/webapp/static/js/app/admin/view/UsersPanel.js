Ext.define('Clara.Admin.view.UsersPanel', {
	extend: 'Ext.grid.Panel',
	requires: ['Ext.ux.form.SearchField'],
	alias: 'widget.userspanel',
	title:'User Accounts',
	iconCls:'icn-user',
	border:false,
	viewConfig:{
		trackOver:false
	},
	store:'Clara.Common.store.Users',
	listeners:{
		itemclick: function(gp,rec){
			adminGlobals.selectedUser = rec;
			// check enable/disable buttons
			var isClaraUser = (adminGlobals.selectedUser.get("id") > 0);
			Ext.getCmp("btnCreateUserAccount").setDisabled(isClaraUser);
			Ext.getCmp("btnRoles").setDisabled(!isClaraUser);
		},
		itemdblclick: function(gp,rec){
			var w = Ext.create("Clara.Admin.view.UserRolesWindow", { user:rec }).show();
		}
	},
	initComponent: function() {
		var t = this;
		
		this.dockedItems = [{
			dock: 'top',
			border:false,
			xtype: 'toolbar',
			items: [{
				xtype:'searchfield',
				store:Ext.data.StoreManager.lookup('Clara.Common.store.Users'),
				title:'Search for a Clara or UAMS user..',
				emptyText:'Search by name or email address',
				paramName : 'keyword',
				reloadAllAsClear:false,
				flex:1
			},'->', {
				xtype: 'button',
				id:'btnRoles',
				text: 'Edit Roles',
				disabled:true,
				iconCls:'icn-xfn-friend-met',
				handler: function(){
					Ext.create("Clara.Admin.view.UserRolesWindow", { user:adminGlobals.selectedUser }).show();
				}
			},{
				xtype: 'button',
				id:'btnCreateUserAccount',
				action:'create_clara_account',
				text: 'Create CLARA Account for User',
				disabled:true,
				iconCls:'icn-user--arrow'
			}, {
				xtype: 'button',
				id:'btnCreateOffCampusUser',
				action:'create_off_campus_account',
				text: 'Create Off-Campus User',
				disabled:false,
				iconCls:'icn-user--plus'
			}]
		}];
		
		t.columns = [
		{header: 'Username', width: 300, sortable: true, dataIndex: 'username', renderer:function(value, p, record){
    		var ruser = record.get("username");
    		var html = "<div class='"+((record.get('id') > 0)?"admin-staff-search-user-exists":"admin-staff-search-user-not-exists")+"'><span class='admin-user-row-field admin-username'>"+ruser+"</span>" + ((record.get('id') > 0)?(" - <a href='"+appContext+"/user/"+record.get("userid")+"/profile' target='_blank'>Profile</a>"):"");
    		if (claraInstance.HasAnyPermissions(['ROLE_SECRET_ADMIN']) && record.get('id') > 0){
    			html += " | <a href='"+appContext+"/super/j_spring_security_switch_user?j_username="+ruser+"'>Impersonate</a>";
    		}
    		return html+"</div>";
        }},
        {header: 'Last Name', width: 120, sortable: true, dataIndex: 'lastname'},
        {header: 'First Name', width: 120, sortable: true, dataIndex: 'firstname'},
        {header: 'Department', flex:1, sortable: true, dataIndex: 'department', id:'user-department'},
        {header: 'Email', width: 180, sortable: true, dataIndex: 'email'},
        {header: 'Phone', width: 120, sortable: true, dataIndex: 'workphone'}
		];
		t.callParent();

	}

});