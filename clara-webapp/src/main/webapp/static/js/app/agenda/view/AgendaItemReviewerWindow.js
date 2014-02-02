Ext.define('Clara.Agenda.view.AgendaItemReviewerWindow', {
	extend: 'Ext.window.Window',
	alias: 'widget.agendaitemreviewerwindow',
	layout: {
	    type: 'border'
	},
	title: 'Assign reviewers',
	modal:true,
	width:750,
	height:500,
	initComponent: function() {
		var me = this;
		var agendacontroller = Clara.Agenda.app.getController("Agenda");
		var agendaitemcontroller = Clara.Agenda.app.getController("AgendaItem");
		me.buttons = [{
			text:'Done',
			handler:function(){me.close();}
		}];
		me.items = [{
			region:'center',
			split:true,
			xtype:'grid',
			id:'gpAgendaItemIrbReviewers',
			hideHeaders: true,
			store:'IrbReviewers',
			title:'IRB Reviewers for this committee',
			listeners: {
			       	added: function(g){
			       		g.getStore().getProxy().url = appContext + "/ajax/agendas/"+agendacontroller.selectedAgenda.get("id")+"/agenda-irb-reviewers/list";
			       		g.getStore().load();
			    	}	
		   	    },
			columns: [{dataIndex: 'id',renderer:agendacontroller.renderAgendaRosterRow, flex:1}],
			dockedItems: [{
				dock: 'bottom',
				border:false,
				xtype: 'toolbar',
				items: ['->',{
		    		id:'btnAssignReviewerToAgendaItem',
		    		disabled:true,
		    		text:'Assign',
		    		iconAlign:'right',
		    		iconCls:'icn-arrow'
		    	}]
			}]
		},{
			region:'east',
			width:350,
			split:true,
			xtype:'grid',
			id:'gpAgendaItemAssignedIrbReviewers',
			hideHeaders: true,
			store:'AssignedIrbReviewers',
			title:'Reviewers assigned to this item',
			columns: [{dataIndex: 'id',renderer:agendacontroller.renderAgendaRosterRow, flex:1}],
			listeners: {
		       	added: function(g){
		       		g.getStore().getProxy().url = appContext + "/ajax/agendas/"+agendacontroller.selectedAgenda.get("id")+"/agenda-items/"+agendacontroller.selectedAgendaItem.get("id")+"/agenda-reviewers/list";
		       		g.getStore().load();
		    	}	
	   	    },
			dockedItems: [{
				dock: 'bottom',
				border:false,
				xtype: 'toolbar',
				items: ['->',{
					id:'btnRemoveReviewerFromAgendaItem',
					disabled:true,
					iconAlign:'left',
					iconCls:'icn-minus',
					text:'Remove',
		    	}]
			}]
		}];
		
		me.callParent();
		
	}
});