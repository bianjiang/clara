Ext.define('Clara.Agenda.view.AgendaRosterWindow', {
	extend: 'Ext.window.Window',
	alias: 'widget.agendarosterwindow',
	layout: {
	    type: 'border'
	},
	title: 'Manage Roster',
	modal:true,
	width:750,
	height:500,
	initComponent: function() {
		var me = this;
		var controller = Clara.Agenda.app.getController("Agenda");
		
		me.buttons = [];
		me.items = [{
			region:'center',
			split:true,
			xtype:'grid',
			id:'gpIrbReviewers',
			hideHeaders: true,
			store:'IrbReviewers',
			title:'IRB Roster for this committee',
			listeners: {
			       	added: function(g){
			       		g.getStore().getProxy().url = appContext + "/ajax/agendas/"+controller.selectedAgenda.get("id")+"/agenda-irb-reviewers/list";
			       		g.getStore().load();
			    	}	
		   	    },
			columns: [{dataIndex: 'id',renderer:controller.renderAgendaRosterRow, flex:1}],
			dockedItems: [{
				dock: 'bottom',
				border:false,
				xtype: 'toolbar',
				items: [{
		    		id:'btnRemoveReviewer',
		    		disabled:true,
		    		text:'Remove..',
		    		iconCls:'icn-user--minus'
		    	},{
		    		id:'btnAssignAlternate',
		    		text:'Assign Alternate..',
		    		iconCls:'icn-xfn-friend-met',
		    		disabled:true

		    	}]
			}]
		},{
			region:'east',
			width:350,
			split:true,
			xtype:'grid',
			id:'gpIrbMembers',
			hideHeaders: true,
			store:'Clara.Common.store.IrbRosters',
			title:'All IRB members',
			columns: [{dataIndex: 'id',renderer:controller.renderAgendaRosterRow, flex:1}],
			dockedItems: [{
				dock: 'bottom',
				border:false,
				xtype: 'toolbar',
				items: [{
					id:'btnAddMember',
					disabled:true,
					hidden:true,	// Redmine #3067
					iconCls:'icn-user--plus',
					text:'Add to Agenda Roster..',
		    	}]
			}]
		}];
		
		me.callParent();
		
	}
});