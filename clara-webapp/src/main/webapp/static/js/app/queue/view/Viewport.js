Ext.define('Clara.Queue.view.Viewport',{
	extend: 'Ext.container.Viewport',
	border:false,
	requires:['Clara.Queue.view.QueueItemReviewerWindow','Clara.Queue.view.QueuePanel','Clara.Queue.view.QueueItem.ProtocolPanel','Clara.Queue.view.QueueItem.ContractPanel'],
	layout:'border',
	defaults:{
		split:true,
		border:false,
		collapsible:false
	},
	initComponent: function(){
		this.items=[{
			dock:'top',
			xtype:'container',
			region:'north',
			split:false,
			contentEl:'clara-header',
			bodyCls:'background-normal',
			border:0
		},{
			xtype:'panel',
			region:'center',
			layout: 'card',
			split:false,
			border:false,
			id:'queueitemscardpanel',
			dockedItems: [{
				xtype:'toolbar',
				dock:'top',
				items:[{
					xtype:'button',
					id:'btnShowQueueItemLog',
					iconCls:'icn-sticky-note',
					text:'Show log',
					pressed:false,
					hidden:true,
					enableToggle:true,
					toggleHandler: function(btn,st){
						if (st) {
							jQuery(".queueitem-logs").show();
							btn.setText("Hide Log");
						} else {
							jQuery(".queueitem-logs").hide();
							btn.setText("Show Log");
						}
						
					}
				},'->',
				       {
					xtype : 'button',
					text : 'View study',
					id : 'btnViewStudy',
					iconCls : 'icn-book',
					disabled : true
		       },
		       {
					xtype : 'button',
					text : '<strong>Review form</strong>',
					id : 'btnReviewQueueItem',
					iconCls : 'icn-clipboard-search-result',
					disabled : true
		       },
		       {
		    	   xtype : 'button',
					text : 'Assign Reviewers',
					id : 'btnAssignItemToReviewer',
					iconCls : 'icn-user--arrow',
					disabled : true,
					hidden : false
		       },{
					xtype:'button',
					text:'Assign Agenda',
					id:'btnAssignToAgenda',
					iconCls:'icn-calendar-next',
					hidden:true
		       },'-',{
					xtype:'button',
					id:'btnPrintItems',
					iconCls:'icn-printer',
					disabled:true
				}
				]
			},{
		    	xtype:'tbqueuefilter',
		    	id:'tbQueueFilter',
		    	dock:'top',
		    	border:false,
		    	disabled:true
		    }],
			items:[{html:'No queue selected.'},{xtype:'protocolqueueitemspanel'},{xtype:'contractqueueitemspanel'}]
		},{
			xtype:'queuepanel',
			region:'west',
			split:true,
			width:160,
			id:'queuepanel'
		}];
		this.callParent();
	}


});