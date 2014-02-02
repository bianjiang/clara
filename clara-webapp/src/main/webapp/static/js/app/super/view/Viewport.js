Ext.define('Clara.Super.view.Viewport',{
	extend: 'Ext.container.Viewport',
	requires:['Clara.Super.view.LiveUsersPanel', 'Clara.Super.view.ImpersonateUserPanel', 'Clara.Super.view.NewsPanel'],
	layout:'border',
	border:false,
	defaults:{
		split:false,
		border:false,
		collapsible:false
	},
	initComponent: function(){
		this.items=[{
			dock:'top',
			xtype:'panel',
			contentEl:'clara-header',
			region:'north',
			bodyCls:'background-normal',
			border:0
		},{
			xtype:'tabpanel',
			region:'center',
			items:[{
				xtype:'liveuserspanel',
				title:'Live Users',
				id:'liveuserspanel',
				iconCls:'icn-users'
			},{
				xtype:'impersonateUserPanel',
				title:'Impersonate',
				id:'impersonateUserPanel',
				iconCls:'icn-users'
			},{
				xtype:'newspanel',
				title:'Site News',
				id:'siteNewsPanel',
				iconCls:'icn-newspaper'
			}]
		}];
		this.callParent();
	}

});