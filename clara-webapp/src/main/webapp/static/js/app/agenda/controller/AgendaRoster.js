Ext.define('Clara.Agenda.controller.AgendaRoster', {
	extend: 'Ext.app.Controller',

	refs: [{
		ref: 'agendaRosterWindow', selector: 'agendarosterwindow'},{
		ref: 'reviewerGridPanel', selector:'#gpIrbReviewers'},{
		ref: 'memberGridPanel', selector:'#gpIrbMembers'},{
		ref: 'removeReviewerButton', selector: '#btnRemoveReviewer'},{
		ref: 'assignAlternateButton', selector: '#btnAssignAlternate'},{
		ref: 'addMemberButton', selector: '#btnAddMember'
	}],

	init: function() {
		var me = this;

		// Start listening for controller events
		me.on("agendaRosterUpdated", function(){
    		me.onAgendaRosterUpdate();
    	});

		// Start listening for events on views
		me.control({
			'#gpIrbReviewers':{
        		itemclick:function(g,rec){ me.onPersonSelected("irbReviewer",rec); }
        	},
        	'#gpIrbMembers':{
        		itemclick:function(g,rec){ me.onPersonSelected("irbMember",rec); }
        	},
        	'#btnRemoveReviewer':{
        		click: me.removeReviewer
        	},
        	'#btnAssignAlternate':{
        		click: me.assignAlternate
        	},
        	'#btnAddMember':{
        		click: me.addMember
        	}
		});
	},

	loadingMask: new Ext.LoadMask(Ext.getBody(), {msg:"Please wait..."}),
	
	selectedIrbReviewer: null,
	selectedIrbMember: null,

	
	addMember: function(){
		var me = this;
		var agendaController = Clara.Agenda.app.getController("Agenda");
		Ext.Msg.show({
			title:'Reason for assigning alternate?',
			   msg: 'You are about to add '+me.selectedIrbMember.get("user").person.firstname+' '+me.selectedIrbMember.get("user").person.lastname+' for this one agenda only. Please provide a reason for this change.',
			   buttons: Ext.Msg.OKCANCEL,
			   multiline:true,
			   
			   fn: function(btn, str){
				   
				   if (btn == 'ok'){
	    				me.loadingMask.show();
	    				Ext.Ajax.request({
	    					method:'POST',
	    					url: appContext + "/ajax/agendas/"+agendaController.selectedAgenda.get("id")+"/agenda-irb-reviewers/add-additional",
	    					params: {userId: claraInstance.user.id,reason: str,irbReviewerId: me.selectedIrbMember.get("id")},
	    					success: function(response){
	    						clog('addMember: Ext.Ajax success',response);
	    						me.loadingMask.hide();
	    						me.fireEvent("agendaRosterUpdated");
	    					},
	    					failure: function(error) {
	    						cwarn('addMember: Ext.Ajax failure',error);
	    						me.loadingMask.hide();
	    					}
	    				});
	    				
	    			}
			   },
			   animEl: 'elId',
			   icon: Ext.MessageBox.WARNING
			});
	},
	
	assignAlternate: function(){
		var me = this;
		var agendaController = Clara.Agenda.app.getController("Agenda");
		Ext.Msg.show({
			title:'Reason for assigning alternate?',
			   msg: 'You are about to replace '+me.selectedIrbReviewer.get("user").person.firstname+' '+me.selectedIrbReviewer.get("user").person.lastname+' with '+me.selectedIrbMember.get("user").person.firstname+' '+me.selectedIrbMember.get("user").person.lastname+' for this one agenda only. Please provide a reason for this change.',
			   buttons: Ext.Msg.OKCANCEL,
			   multiline:true,
			   
			   fn: function(btn, str){
				   
				   if (btn == 'ok'){
	    				me.loadingMask.show();
	    				Ext.Ajax.request({
	    					method:'POST',
	    					url: appContext + "/ajax/agendas/"+agendaController.selectedAgenda.get("id")+"/agenda-irb-reviewers/"+me.selectedIrbReviewer.get("id")+"/assign-alternate",
	    					params: {userId: claraInstance.user.id,reason: str,alternateIRBReviewerId: me.selectedIrbMember.get("id")},
	    					success: function(response){
	    						clog('assignAlternate: Ext.Ajax success',response);
	    						me.loadingMask.hide();
	    						me.fireEvent("agendaRosterUpdated");
	    					},
	    					failure: function(error) {
	    						cwarn('assignAlternate: Ext.Ajax failure',error);
	    						me.loadingMask.hide();
	    					}
	    				});
	    				
	    			}
			   },
			   animEl: 'elId',
			   icon: Ext.MessageBox.WARNING
			});
	},
	
	removeReviewer: function(){
		var me = this;
		var agendaController = Clara.Agenda.app.getController("Agenda");
		
		Ext.Msg.show({
			   title:'Reason for removing member?',
			   msg: 'You are about to remove '+me.selectedIrbReviewer.get("user").person.firstname+' '+me.selectedIrbReviewer.get("user").person.lastname+' to this one agenda only. Please provide a reason for this change. <h3>Note: This person will be removed as reviewer for any assigned items on the agenda, and will need to be reassigned manually.</h3>',
			   buttons: Ext.Msg.OKCANCEL,
			   multiline:true,
			   
			   fn: function(btn, str){
				   
				   if (btn == 'ok'){
	    				me.loadingMask.show();
	    				Ext.Ajax.request({
	    					method:'POST',
	    					url: appContext + "/ajax/agendas/"+agendaController.selectedAgenda.get("id")+"/agenda-irb-reviewers/"+me.selectedIrbReviewer.get("id")+"/remove",
	    					params: {userId: claraInstance.user.id,reason: str},
	    					success: function(response){
	    						clog('removeReviewer: Ext.Ajax success',response);
	    						me.loadingMask.hide();
	    						me.fireEvent("agendaRosterUpdated");
	    					},
	    					failure: function(error) {
	    						cwarn('removeReviewer: Ext.Ajax failure',error);
	    						me.loadingMask.hide();
	    					}
	    				});
	    				
	    			}
			   },
			   animEl: 'elId',
			   icon: Ext.MessageBox.WARNING
			});

	
	},

	addReviewer: function(rec){
		var me = this;
		var agendaController = Clara.Agenda.app.getController("Agenda");

		var recToAdd = (rec)?rec:me.selectedIrbMember;
		
		me.loadingMask.show();
		Ext.Ajax.request({
			method:'POST',
			url: appContext + "/ajax/agendas/"+agendaController.selectedAgenda.get("id")+"/agenda-irb-reviewers/add",
			params: {userId: claraInstance.user.id,agendaIRBReviewerId:recToAdd.get("id")},
			success: function(response){
				clog('addReviewer: Ext.Ajax success',response);
				me.loadingMask.hide();
				me.fireEvent("agendaRosterUpdated");
			},
			failure: function(error) {
				cwarn('addReviewer: Ext.Ajax failure',error);
				me.loadingMask.hide();
			}
		});

	},
	
	deleteReviewer: function(rec){
		var me = this;
		var agendaController = Clara.Agenda.app.getController("Agenda");
		
		me.loadingMask.show();
		Ext.Ajax.request({
			method:'POST',
			url: appContext + "/ajax/agendas/"+agendaController.selectedAgenda.get("id")+"/agenda-irb-reviewers/delete",
			params: {userId: claraInstance.user.id,agendaIRBReviewerId:rec.get("id")},
			success: function(response){
				clog('deleteReviewer: Ext.Ajax success',response);
				me.loadingMask.hide();
				me.fireEvent("agendaRosterUpdated");
			},
			failure: function(error) {
				cwarn('deleteReviewer: Ext.Ajax failure',error);
				me.loadingMask.hide();
			}
		});

	},
	
	removeAlternate: function(rec){
		var me = this;
		var agendaController = Clara.Agenda.app.getController("Agenda");
		
		me.loadingMask.show();
		Ext.Ajax.request({
			method:'POST',
			url: appContext + "/ajax/agendas/"+agendaController.selectedAgenda.get("id")+"/agenda-irb-reviewers/"+rec.get("id")+"/remove-alternate",
			params: {userId: claraInstance.user.id},
			success: function(response){
				clog('removeAlternate: Ext.Ajax success',response);
				me.loadingMask.hide();
				me.fireEvent("agendaRosterUpdated");
			},
			failure: function(error) {
				cwarn('removeAlternate: Ext.Ajax failure',error);
				me.loadingMask.hide();
			}
		});

	},
	
	resetReviewerStatus: function(id){
		var me = this;
		
		var st = Ext.data.StoreManager.lookup('IrbReviewers');
		var rec = st.getById(id);
		
		clog("resetReviewerStatus: rec",rec);
		
		var action = rec.get("status");
		if (action == "REPLACED"){
			me.removeAlternate(rec);
		} else if (action == "REMOVED"){
			me.addReviewer(rec);
		} else if (action == "ADDITIONAL"){
			me.deleteReviewer(rec);
		} else {
			cwarn("resetReviewerStatus: Unrecognised status '"+action+"'",rec);
		}
	},
	
	onAgendaRosterUpdate: function(){
		var me = this;
		me.getRemoveReviewerButton().setDisabled(true);
		me.getAssignAlternateButton().setDisabled(true);
		Ext.data.StoreManager.lookup('IrbReviewers').clearFilter();
		Ext.data.StoreManager.lookup('IrbReviewers').filter([
		                             						{filterFn: function(r){return r.get("irbRoster") !== "WEEK_5"}}
		                             						]);
		Ext.data.StoreManager.lookup('IrbReviewers').load();
	},
	
	onPersonSelected: function(personType, rec){
		clog("AgendaRoster.onPersonSelected",personType,rec);
		var me = this;
		
		if (personType == "irbMember"){
			me.selectedIrbMember = rec;
		} else if (personType == "irbReviewer"){
			me.selectedIrbReviewer = rec;
		}
		
		clog("me.selectedIrbReviewer",me.selectedIrbReviewer);
		clog("me.selectedIrbMember",me.selectedIrbMember);

		if (me.selectedIrbReviewer && Ext.isDefined(me.selectedIrbReviewer.get)){
			if (me.selectedIrbReviewer.get("status") == "REPLACED"){
				me.getRemoveReviewerButton().setDisabled(true);
				me.getAssignAlternateButton().setDisabled(true);
			} else if (me.selectedIrbReviewer.get("status") == "NORMAL"){
				me.getRemoveReviewerButton().setDisabled(false);
			}
		}

		if (me.selectedIrbMember && Ext.isDefined(me.selectedIrbMember.get)){
			if (personType == "irbMember"){
				me.getAddMemberButton().setDisabled(false);
			} else {
				me.getAddMemberButton().setDisabled(true);
			}
		}

		if (me.selectedIrbMember && me.selectedIrbReviewer && Ext.isDefined(me.selectedIrbMember.get) && Ext.isDefined(me.selectedIrbReviewer.get)){
			me.getAssignAlternateButton().setDisabled(false);
		} else {
			me.getAssignAlternateButton().setDisabled(true);
		}
	}


});
