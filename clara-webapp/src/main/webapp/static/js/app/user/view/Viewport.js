Ext.define('Clara.User.view.Viewport',{
	extend: 'Ext.container.Viewport',
	requires:['Clara.User.view.UserDetailPanel','Clara.User.view.UserLockedFormsPanel','Clara.User.view.UserRolesPanel','Clara.User.view.UserCitiTrainingPanel','Clara.User.view.UserCoiPanel','Clara.User.view.UserPasswordWindow'],
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
				xtype:'panel',
				title:'Profile',
				border:false,
				iconCls:'icn-user',
				layout:'hbox',
				items:[{xtype:'userdetailpanel',width:400,height:'100%'},{
					xtype:'tabpanel',height:'100%',flex:1,border:false,style: 'border-left: 1px solid #8DB2E3;',
					items:[{title:'Assigned Roles',xtype:'userrolespanel'},{id:'ctpnl',title:'Citi Training',xtype:'usercititrainingpanel'},{id:'coipnl',title:'UAMS COI',xtype:'usercoipanel'}]
				}],
				listeners:{
					render:function(t){
						t.setTitle("Profile for "+profile.firstname+" "+profile.lastname);
					}
				}
			},{
				xtype:'userlockedformspanel',
				title:'Locked Items',
				id:'userlockedformspanel',
				iconCls:'icn-lock'
			}]
		}];
		this.callParent();
	}

});