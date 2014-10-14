Ext.define('Clara.Queue.controller.QueueIRBAssign', {
	extend: 'Ext.app.Controller',

	refs: [{
			ref: 'irbAssignmentWindow', selector: 'irbassignmentwindow'},{
			ref: 'assignAgendaItemDate', selector:'#fldAssignAgendaItemDate'},{
			ref: 'assignItemReviewer', selector:'#fldAssignItemReviewer'},{
			ref: 'assignAgendaItemType', selector:'#fldAssignAgendaItemType'},{
			ref:'assignIRBButton', selector:'#btnIRBAssign'
			}],

	init: function() {
		var me = this;
		
		// Start listening for controller events
	

		// Start listening for events on views
		me.control({
			'irbassignmentwindow':{
        		show:me.onWindowShow
        	},
        	'#fldAssignAgendaItemType':{
        		select:me.onTypeSelected
        	},
        	'#btnAssignToAgenda':{
        		click:me.onAssignToAgenda
        	},
        	'#btnSaveAgendaAssignment':{
        		click:me.onSaveAgendaAssignment
        	}
		});
	},

	loadingMask: new Ext.LoadMask(Ext.getBody(), {msg:"Please wait..."}),

	processAgendaItem: function(agendaItem, agendaItemType, agendaItemReviewerId){
		var me = this;
		me.loadingMask.show();
		Ext.Ajax.request({
			method:'GET',
			url: appContext + "/ajax/queues/committees/irb-office/process",
			params:{
				"protocolFormId": agendaItem.get("formId"),
				"itemCategory": agendaItemType,
				"userId": claraInstance.user.id,
				"reviewerUserRoleId":agendaItemReviewerId
			},
			success: function(response){
				if (piwik_enabled()){
					_paq.push(['trackEvent', 'QUEUE_IRB', 'Processed agenda item ('+agendaItem.get("formId")+')']);
				}
				me.loadingMask.hide();
				me.getIrbAssignmentWindow().close();
				Clara.Application.getController("Queue").onQueueSelect(null,Clara.Application.getController("Queue").selectedQueue);	// reload the queue list by reselecting it
			},
			failure: function(error) {
				cwarn('processAgendaItem: Ext.Ajax failure',error);
				if (piwik_enabled()){
					_paq.push(['trackEvent', 'ERROR', 'QUEUE_IRB: Failure processing agenda item ('+agendaItem.get("formId")+')']);
				}
				me.loadingMask.hide();
			}
		});
		
	},
	
	assignItemToAgenda: function(agendaId,queueItem,itemType){
		var me = this;
		me.loadingMask.show();
		Ext.Ajax.request({
			method:'GET',
			url: appContext + "/ajax/agendas/"+agendaId+"/agenda-items/assign",
			params:{
				"protocolFormId": queueItem.get("formId"),
				"agendaItemCategory": itemType,
				"userId": claraInstance.user.id
			},
			success: function(response){
				if (piwik_enabled()){
					_paq.push(['trackEvent', 'QUEUE_IRB', 'Assigned item ('+queueItem.get("formId")+') to agenda '+agendaId]);
				}
				me.loadingMask.hide();
				me.getIrbAssignmentWindow().close();
				Clara.Application.getController("Queue").onQueueSelect(null,Clara.Application.getController("Queue").selectedQueue);	// reload the queue list by reselecting it
			},
			failure: function(error) {
				cwarn('assignItemToAgenda: Ext.Ajax failure',error);
				if (piwik_enabled()){
					_paq.push(['trackEvent', 'ERROR', 'QUEUE_IRB: Failure assigning item ('+queueItem.get("formId")+') to agenda '+agendaId]);
				}
				me.loadingMask.hide();
			}
		});

	},
	
	onSaveAgendaAssignment: function(){
		var me = this,
		    fldAssignAgendaItemDate = me.getAssignAgendaItemDate(),
		    fldAssignItemReviewer =  me.getAssignItemReviewer(),
		    fldAssignAgendaItemType =  me.getAssignAgendaItemType(),
		    queueItem = Clara.Application.getController("QueueItem").selectedQueueItem;

		if (fldAssignAgendaItemType.validate()){
			
			if (fldAssignAgendaItemType.getValue() == "EXEMPT" || fldAssignAgendaItemType.getValue() == "EXPEDITED"){
				if (!fldAssignItemReviewer.validate()){
					alert("Please choose a reviewer.");
				}
				else {
					me.processAgendaItem(queueItem, fldAssignAgendaItemType.getValue(), fldAssignItemReviewer.getValue());
				}
			} else {
				
				if (fldAssignAgendaItemDate.validate()) {
					me.assignItemToAgenda(fldAssignAgendaItemDate.getValue(), queueItem, fldAssignAgendaItemType.getValue());
				} else {
					alert("Please choose an agenda date.");
				}
				
			}
		}else{
			alert("Please complete the assignment form.");
		}

	
	},
	
	onAssignToAgenda: function(){
		Ext.create("Clara.Queue.view.IRBAssignmentWindow").show();
	},
	
	onWindowShow: function(w){
		// set info on IRB window
	},

	onTypeSelected: function(cb,recs,idx){
		var me = this,
		r = recs[0],
		aiReviewer = me.getAssignItemReviewer(),
		aiDate = me.getAssignAgendaItemDate();

		aiDate.clearValue();
		aiDate.setDisabled(true);
		aiReviewer.clearValue();
		aiReviewer.setDisabled(false);

		var t = r.get("type");
		clog("change item type:",t);
		if (t == 'Expedited'){
			clog('its expedited..');
			aiReviewer.getStore().load();
			aiReviewer.getStore().clearFilter();
			aiReviewer.getStore().filter("roleIdentifier","ROLE_IRB_EXPEDITED_REVIEWER");
		} else if (t == 'Exempt') {
			aiReviewer.getStore().load();
			aiReviewer.getStore().clearFilter();
			aiReviewer.getStore().filter("roleIdentifier","ROLE_IRB_EXEMPT_REVIEWER");
		} else {
			aiDate.getStore().getProxy().url = appContext+"/ajax/agendas/list-available";
			aiDate.getStore().load();
			aiDate.setDisabled(false);
			aiReviewer.setDisabled(true);
		}

	}



});
