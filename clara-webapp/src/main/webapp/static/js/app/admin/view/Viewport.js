Ext.define('Clara.Admin.view.Viewport',{
	extend: 'Ext.container.Viewport',
	border:false,
	requires:['Clara.Admin.view.NewIRBRosterMemberWindow','Clara.Admin.view.NewLookupItemWindow','Clara.Admin.view.LookupEditorPanel','Clara.Admin.view.IrbRosterPanel','Clara.Admin.view.LockedFormsPanel','Clara.Admin.view.UsersPanel','Clara.Admin.view.UserRolesWindow','Clara.Admin.view.AddRoleWindow','Clara.Admin.view.NewOffCampusUserWindow','Clara.Admin.view.StudySitesPanel','Clara.Admin.view.StudySiteWindow'],
	layout:'fit',
	defaults:{
		split:true,
		border:false,
		collapsible:false
	},
	initComponent: function(){
		this.items={
				layout:'border',
				items:[{
						dock:'top',
						xtype:'panel',
						contentEl:'clara-header',
						region:'north',
						bodyCls:'background-normal',
						border:0
					},{
						xtype:'tabpanel',
						id:'adminTabPanel',
						border:false,
						region:'center',
						items:[{
							xtype:'userspanel'
						},{
							title:'Locked Forms',
							iconCls:'icn-lock',
							xtype:'adminlockedformspanel'
						},{
							title:'Study Sites',
							iconCls:'icn-building',
							xtype:'studysitespanel'
						},{
							xtype:'irbrosterpanel'
						},{
							xtype:'lookupeditorpanel'
						}]
					}]
				
		};
		this.callParent();
	}

});