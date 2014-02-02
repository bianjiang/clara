Ext.define('Clara.Agenda.controller.AgendaItem', {
	extend: 'Ext.app.Controller',

	refs: [{
			ref: 'agendaItemReviewerWindow', selector: 'agendaitemreviewerwindow'},{
			ref: 'agendaItemIrbReviewerGridPanel', selector:'#gpAgendaItemIrbReviewers'},{
			ref: 'agendaItemAssignedIrbReviewerGridPanel', selector:'#gpAgendaItemAssignedIrbReviewers'},{
			ref: 'removeReviewerButton', selector: '#btnRemoveReviewerFromAgendaItem'},{
			ref: 'assignReviewerButton', selector: '#btnAssignReviewerToAgendaItem'},{
			ref: 'reportedItemsCheckbox', selector: '#cbShowReportedItems'}
			],

	init: function() {
		var me = this;

		// Start listening for controller events
		me.on("assignedReviewerUpdated", function(){
    		me.onAssignedReviewerUpdate();
    	});

		// Start listening for events on views
		me.control({
			'agendaitemreviewerwindow':{
				close: function(w){
					Clara.Agenda.app.getController("Agenda").fireEvent("agendaItemsUpdated");
				}
			},
			'#gpAgendaItemIrbReviewers':{
        		itemclick:function(g,rec){ me.onPersonSelected("irbReviewer",rec); }
        	},
        	'#gpAgendaItemAssignedIrbReviewers':{
        		itemclick:function(g,rec){ me.onPersonSelected("irbAssignedReviewer",rec); }
        	},
        	'#btnRemoveReviewerFromAgendaItem':{
        		click: me.removeReviewer
        	},
        	'#btnAssignReviewerToAgendaItem':{
        		click: me.assignReviewer
        	},
        	'#cbShowReportedItems':{
        		change: me.onToggleReportedItems
        	}
		});
	},

	loadingMask: new Ext.LoadMask(Ext.getBody(), {msg:"Please wait..."}),
	
	selectedIrbReviewer: null,
	selectedAssignedIrbReviewer: null,
	
	onToggleReportedItems: function(cb,showReported){
		var st = Ext.data.StoreManager.lookup('AgendaItems');
		
		Ext.apply(st.proxy.extraParams, {
			hideReported : !showReported
		});
		
		st.load();
	},
	
	assignReviewer: function(){
		var me = this;
		var agendaController = Clara.Agenda.app.getController("Agenda");
		
		me.loadingMask.show();
		Ext.Ajax.request({
			method:'GET',
			url: appContext + "/ajax/agendas/"+agendaController.selectedAgenda.get("id")+"/agenda-items/"+agendaController.selectedAgendaItem.get("id")+"/agenda-reviewers/assign",
			params: {userId: claraInstance.user.id, irbReviewerId:me.selectedIrbReviewer.get("irbreviewerid")},
			success: function(response){
				clog('removeReviewer: Ext.Ajax success',response);
				me.loadingMask.hide();
				me.fireEvent("assignedReviewerUpdated");
			},
			failure: function(error) {
				cwarn('removeReviewer: Ext.Ajax failure',error);
				me.loadingMask.hide();
			}
		});
	},
	
	removeReviewer: function(){
		var me = this;
		var agendaController = Clara.Agenda.app.getController("Agenda");
		
		me.loadingMask.show();
		Ext.Ajax.request({
			method:'GET',
			url: appContext + "/ajax/agendas/"+agendaController.selectedAgenda.get("id")+"/agenda-items/"+agendaController.selectedAgendaItem.get("id")+"/agenda-reviewers/"+me.selectedAssignedIrbReviewer.get("id")+"/remove",
			params: {userId: claraInstance.user.id},
			success: function(response){
				clog('removeReviewer: Ext.Ajax success',response);
				me.loadingMask.hide();
				me.fireEvent("assignedReviewerUpdated");
			},
			failure: function(error) {
				cwarn('removeReviewer: Ext.Ajax failure',error);
				me.loadingMask.hide();
			}
		});
	},
	
	onAssignedReviewerUpdate: function(){
		var me = this;
		me.getAssignReviewerButton().setDisabled(true);
		me.getRemoveReviewerButton().setDisabled(true);
		Ext.data.StoreManager.lookup('AssignedIrbReviewers').load();
	},
	
	onPersonSelected: function(personType, rec){
		clog("AgendaItem.onPersonSelected",personType,rec);
		var me = this;
		
		if (personType == "irbAssignedReviewer"){
			me.selectedAssignedIrbReviewer = rec;
			clog("me.selectedAssignedIrbReviewer",me.selectedAssignedIrbReviewer);
			me.getRemoveReviewerButton().setDisabled(false);
		} else if (personType == "irbReviewer"){
			me.selectedIrbReviewer = rec;
			clog("me.selectedIrbReviewer",me.selectedIrbReviewer);
			me.getAssignReviewerButton().setDisabled(false);
		}

	}

	

});
