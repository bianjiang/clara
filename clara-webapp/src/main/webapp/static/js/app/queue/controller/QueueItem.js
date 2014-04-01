Ext.define('Clara.Queue.controller.QueueItem', {
	extend: 'Ext.app.Controller',

	refs: [{
			ref: 'protocolQueueItemsPanel', selector: 'protocolqueueitemspanel'},{
			ref: 'contractQueueItemsPanel', selector:'contractqueueitemspanel'},{
			ref: 'viewStudyButton', selector:'#btnViewStudy'
			},{
			ref: 'reviewButton', selector:'#btnReviewQueueItem'
			},{
				ref:'assignItemButton', selector:'#btnAssignItemToReviewer'
			},
			{
				ref:'assignAgendaButton', selector:'#btnAssignToAgenda'
			}
			
			],

	init: function() {
		var me = this;
		
		// Start listening for controller events
	

		// Start listening for events on views
		me.control({
			'queuepanel':{
        		itemclick:me.onQueueSelect
        	},
			'protocolqueueitemspanel':{
        		itemclick:function(g,rec){ me.onQueueItemSelected("protocol",rec); }
        	},
        	'contractqueueitemspanel':{
        		itemclick:function(g,rec){ me.onQueueItemSelected("contract",rec); }
        	},
        	'#btnViewStudy':{
        		click:function(){
        			var queueController = Clara.Application.getController("Queue");
        			var url = appContext+"/"+queueController.selectedQueue.get("objectType").toLowerCase()+"s/"+me.selectedQueueItem.get("claraIdentifier")+"/dashboard?formId="+me.selectedQueueItem.get("formId");
    				window.open( url+"&fromQueue="+queueController.selectedQueue.get("identifier"));
        		}
        	},
        	'#btnAssignItemToReviewer':{
        		click: me.onAssignItem
        	}
        	
		});
	},

	loadingMask: new Ext.LoadMask(Ext.getBody(), {msg:"Please wait..."}),
	
	selectedQueueItem: null,

	onAssignItem: function(){
		
		var me = this;
		var queueController = Clara.Application.getController("Queue");
		var queueAssignController = Clara.Application.getController("QueueAssign");
		queueAssignController.queueItem = me.selectedQueueItem;
		queueAssignController.queue = queueController.selectedQueue;
		clog("QueueItem:onAssignItem",queueAssignController.queueItem);
		Ext.create('Clara.Queue.view.QueueItemReviewerWindow').show();
	},
	
	onQueueSelect: function(){
		var me = this;
		me.selectedQueueItem = null;
		me.getViewStudyButton().setDisabled(true);
		me.getReviewButton().setDisabled(true);
		me.getAssignItemButton().setDisabled(true);
		me.getAssignAgendaButton().setVisible(false);
	},
	
	onQueueItemSelected: function(queueType, rec){
		clog("QueueItem.onQueueItemSelected",queueType,rec);
		var me = this;
		me.selectedQueueItem = rec;
		me.getViewStudyButton().setDisabled(false);
		me.getAssignItemButton().setDisabled(true);
		me.getAssignAgendaButton().setVisible(false);
		
		var reviewAction = null;
		
		rec.actions().each(function(r){
			if (r.get("name") == "REVIEW") reviewAction = r;
			else if (r.get("name") == "ASSIGN_REVIEWER") me.getAssignItemButton().setDisabled(false);
			else if (r.get("name") == "ASSIGN_AGENDA") me.getAssignAgendaButton().setVisible(true);
		});
		
		me.getReviewButton().el.clearListeners();
		if (reviewAction) {
			me.getReviewButton().el.on('click', function() {		
				var queueController = Clara.Application.getController("Queue");
				location.href =  appContext + reviewAction.get("url")+"&fromQueue="+queueController.selectedQueue.get("identifier");
			});
			me.getReviewButton().setDisabled(false);
		} else {
			me.getReviewButton().setDisabled(true);
		}
		
	}

	

});
