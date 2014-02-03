Ext.define('Clara.Queue.controller.QueueAssign', {
	extend: 'Ext.app.Controller',
	queueItem: null,
	queue: null,
	selectedReviewerType: null,
	selectedAvailableReviewer: null,
	selectedAssignedReviewer:null,
	refs: [{
			ref: 'queueItemReviewerWindow', selector: 'queueitemreviewerwindow'},{
			ref: 'availableReviewersPanel', selector:'#gpQueueItemAvailableReviewers'},{
			ref: 'assignedReviewersPanel', selector:'#gpQueueItemAssignedReviewers'
			},{
			ref: 'removeReviewerFromQueueItemButton', selector:'#btnRemoveReviewerFromQueueItem'
			},{
				ref:'assignReviewerToQueueItemButton', selector:'#btnAssignReviewerToQueueItem'
			}, {
				ref:'reviewerTypeField',selector:'#fldReviewerType'
			},
			{
				ref:'saveAssignmentButton', selector:'#btnSaveAssignment'
			}
			
			],

	init: function() {
		var me = this;
		
		// Start listening for controller events
	

		// Start listening for events on views
		me.control({
			'queueitemreviewerwindow':{
        		show:me.onWindowShow
        	},
        	'#fldReviewerType':{
        		select:me.onReviewerTypeSelect
        	},
			'#gpQueueItemAvailableReviewers':{
        		itemclick:function(g,rec){ me.onAvailableReviewerSelected(rec); }
        	},
        	'#gpQueueItemAssignedReviewers':{
        		itemclick:function(g,rec){ me.onAssignedReviewerSelected(rec); }
        	},
        	'#btnAssignReviewerToQueueItem':{
        		click:function(){
        			clog("btnAssignReviewerToQueueItem",me.selectedAvailableReviewer);
        			me.addAssignedReviewer(me.reloadAssignedReviewers);
        		}
        	},
        	'#btnRemoveReviewerFromQueueItem':{
        		click:function(){
        			clog("btnRemoveReviewerFromQueueItem",me.selectedAssignedReviewer);
        			me.removeAssignedReviewer(me.reloadAssignedReviewers);
        		}
        	},
        	'#btnSaveAssignment':{
        		click:function(){
        			clog("btnSaveAssignment",me.queueItem);
        			var win = me.getQueueItemReviewerWindow();
        			me.completeAssignment(function(){
        				win.close();
        				if (me.queue && me.queueItem){	// only do this is on queue page (this also is on the protocol/contract dashboards)
        					clog("queueController",Clara.Application.getController("Queue"));
        					Clara.Application.getController("Queue").onQueueSelect(null,Clara.Application.getController("Queue").selectedQueue);	// reload the queue list by reselecting it
        				}
        			});
        		}
        	}
        	
		});
	},

	loadingMask: new Ext.LoadMask(Ext.getBody(), {msg:"Please wait..."}),
	

	
	onWindowShow: function(w){
		this.getAvailableReviewersPanel().getStore().removeAll();
		this.reloadAssignedReviewers();
	},
	
	completeAssignment: function(callbackFn){
		var me = this;
		var queueType, committee, roleId, formId;
		me.loadingMask.show();
		
		if (me.queue && me.queueItem){
			queueType = me.queue.get("objectType");
			formId = me.queueItem.get("formId");
			committee = me.queueItem.get("committee");
		} else {
			var win = me.getQueueItemReviewerWindow();
			queueType = Ext.util.Format.capitalize(claraInstance.type);
			formId = win.formQueueItem.formId;
			committee = win.formQueueItem.committee;
		}
		
		Ext.Ajax.request({
			method:'GET',
			url: appContext + "/ajax/queues/complete-assign-reviewer",
			params:{
				"objectType": queueType,
				"formId": formId,
				"committee": committee,
				"userId": claraInstance.user.id,
				"action":"ASSIGN_REVIEWER"
			},
			success: function(response){
				me.loadingMask.hide();
				if (callbackFn) callbackFn();
			},
			failure: function(error) {
				cwarn('removeAssignedReviewer: Ext.Ajax failure',error);
				me.loadingMask.hide();
			}
		});
	},
	
	removeAssignedReviewer: function(callbackFn){
		var me = this;
		var queueType, committee, roleId, formId;
		var reviewer = me.selectedAssignedReviewer;
		clog("Reveiwer to remove",reviewer);
		me.loadingMask.show();
		
		if (me.queue && me.queueItem){
			queueType = me.queue.get("objectType");
			formId = me.queueItem.get("formId");
			roleId = me.queueItem.get("roleId");
			committee = me.queueItem.get("committee");
		} else {
			var win = me.getQueueItemReviewerWindow();
			queueType = Ext.util.Format.capitalize(claraInstance.type);
			roleId = win.formQueueItem.roleId;
			formId = win.formQueueItem.formId;
			committee = win.formQueueItem.committee;
		}
		
		Ext.Ajax.request({
			method:'GET',
			url: appContext + "/ajax/queues/remove-reviewer",
			params:{
				"objectType": queueType,
				"formId": formId,
				"committee": committee,
				"userId": claraInstance.user.id,
				"reviewerUserRoleId":reviewer.get("userroleid")
			},
			success: function(response){
				me.loadingMask.hide();
				if (callbackFn) callbackFn();
			},
			failure: function(error) {
				cwarn('removeAssignedReviewer: Ext.Ajax failure',error);
				me.loadingMask.hide();
			}
		});
	},
	
	addAssignedReviewer: function(callbackFn){
		var me = this;
		var queueType, committee, roleId, formId;
		var reviewer = me.selectedAvailableReviewer;
		
		if (me.queue && me.queueItem){
			queueType = me.queue.get("objectType");
			formId = me.queueItem.get("formId");
			roleId = me.queueItem.get("roleId");
			committee = me.queueItem.get("committee");
		} else {
			var win = me.getQueueItemReviewerWindow();
			queueType = Ext.util.Format.capitalize(claraInstance.type);
			roleId = win.formQueueItem.roleId;
			formId = win.formQueueItem.formId;
			committee = win.formQueueItem.committee;
		}
		
		me.loadingMask.show();
		Ext.Ajax.request({
			method:'GET',
			url: appContext + "/ajax/queues/assign-reviewer",
			params:{
				"objectType": queueType,
				"formId": formId,
				"committee": committee,
				"userId": claraInstance.user.id,
				"reviewerUserRoleId":reviewer.get("id")
			},
			success: function(response){
				me.loadingMask.hide();
				if (callbackFn) callbackFn();
			},
			failure: function(error) {
				cwarn('addAssignedReviewer: Ext.Ajax failure',error);
				me.loadingMask.hide();
			}
		});
	},
	
	reloadAssignedReviewers: function(){
		var queueType, committee, roleId, formId;
		
		var me = Clara.Application.getController("QueueAssign");	// callback changes "this" to window instead of controller, so we must call controller explicitly here
		clog("reloadAssignedReviewers START",this,me,me.queue,me.queueItem);
		var assignedStore = me.getAssignedReviewersPanel().getStore();
		assignedStore.removeAll();
		
		if (me.queue && me.queueItem){
			queueType = me.queue.get("objectType").toLowerCase();
			formId = me.queueItem.get("formId");
			roleId = me.queueItem.get("roleId");
			committee = me.queueItem.get("committee");
		} else {
			var win = me.getQueueItemReviewerWindow();
			queueType = claraInstance.type;
			roleId = win.formQueueItem.roleId;
			formId = win.formQueueItem.formId;
			committee = win.formQueueItem.committee;
		}
		
		clog("reloadAssignedReviewers",appContext + "/ajax/"+queueType+"/"+queueType+"-forms/"+formId+"/list-reviewers");
		assignedStore.getProxy().url = appContext + "/ajax/"+queueType+"/"+queueType+"-forms/"+formId+"/list-reviewers";
		Ext.apply(assignedStore.proxy.extraParams, {
			userId: claraInstance.user.id,
			committee: committee,
			roleId: roleId
		});
		assignedStore.load();
	},
	
	onReviewerTypeSelect: function(cb,recs,idx){
		var me = this;
		clog("onReviewerTypeSelect: cb,recs,idx",cb,recs,idx);
		me.getRemoveReviewerFromQueueItemButton().setDisabled(true);
		me.getAssignReviewerToQueueItemButton().setDisabled(true);
		var reviewersStore = Ext.data.StoreManager.lookup("Clara.Queue.store.Reviewers");
		me.selectedReviewerType = recs[0].get("roleid");
		reviewersStore.removeAll();
		clog("About to load role "+me.selectedReviewerType);
		reviewersStore.load({ params:{
			roles:[me.selectedReviewerType]
		}});
	},
	
	onAvailableReviewerSelected: function(rec){
		clog("onAvailableReviewerSelected",rec);
		var me = this;
		me.selectedAvailableReviewer = rec;
		me.getAssignReviewerToQueueItemButton().setDisabled(false);
		me.getRemoveReviewerFromQueueItemButton().setDisabled(true);
	},
	
	onAssignedReviewerSelected: function(rec){
		clog("onAssignedReviewerSelected",rec);
		var me = this;
		me.selectedAssignedReviewer = rec;
		me.getAssignReviewerToQueueItemButton().setDisabled(true);
		me.getRemoveReviewerFromQueueItemButton().setDisabled(false);
	},
	
	Reassign: function(qitem,q, callbackFn, triggerWorkflow){
		triggerWorkflow = (typeof triggerWorkflow == "undefined")?true:triggerWorkflow;
		clog("QueueAssign Controller: Reassign",qitem,q,callbackFn,triggerWorkflow);
				
		Ext.create('Clara.Queue.view.QueueItemReviewerWindow',{ 
			formQueueItem:qitem, 
			triggerWorkflow:triggerWorkflow, 
			formQueueType:q.objectType, 
			callback:function(){
				if (typeof callbackFn == "undefined"){
					clog("Clara.Queues.Reassign: callback undefined, using standard");
					var pnl = (q.objectType == "Protocol")?Ext.getCmp("clara-protocol-db-formgridpanel"):Ext.getCmp("clara-contract-db-formgridpanel");
					if (typeof pnl != "undefined"){
						clog("pnl",pnl);
						pnl.getStore().reload();
					}else {
						clog("pnl undefined",qitem,q);
					}
				} else {
					clog("Clara.Queues.Reassign: callback defined, calling..");
					callbackFn();
				}
			}}).show();

		
		// {roleId:'ROLEHERE',formId:111,committee:'COMITTEEHERE'}, {objectType:'Protocol'}
	},
	
	getActorStore: function(formQueueItem, formQueueType, triggerWorkflow){
		clog("getActorStore: this",this," formQueueItem",formQueueItem," this.queueItem",this.queueItem,Clara.Application.QueueAssignController.queueItem);
		var me = Clara.Application.QueueAssignController,	// WHY doesnt 'this' work here??
		    actor = (formQueueItem && formQueueItem != null)?formQueueItem.roleId:me.queueItem.get("roleId"),
		    formTypeId= (formQueueItem && formQueueItem != null)?formQueueItem.formTypeId:me.queueItem.get("formTypeId");
		
		clog("getActorStore",actor,formTypeId);
		var assignmentRules = [{
			actor:"ROLE_BUDGET_MANAGER",
			forms:['new-submission','modification'],
			canAssignTo:[["Budget Reviewer", "ROLE_BUDGET_REVIEWER"],["Coverage Reviewer","ROLE_COVERAGE_REVIEWER"]]
		},
		{
			actor:"ROLE_CONTRACT_MANAGER",
			forms:['new-contract','amendment'],
			canAssignTo:[["Contract Admin", "ROLE_CONTRACT_ADMIN"],["Contract Legal Reviewer","ROLE_CONTRACT_LEGAL_REVIEW"]]
		},
		{
			actor:"ROLE_CONTRACT_LEGAL_REVIEW",
			forms:['new-contract','amendment'],
			canAssignTo:[["Contract Admin", "ROLE_CONTRACT_ADMIN"],["Contract Legal Reviewer","ROLE_CONTRACT_LEGAL_REVIEW"]]
		},
		{
			actor:"ROLE_CONTRACT_LEGAL_REVIEW",
			forms:['new-contract-studyinfo'],
			canAssignTo:[["Budget Reviewer", "ROLE_BUDGET_REVIEWER"],["Coverage Reviewer","ROLE_COVERAGE_REVIEWER"]]
		},
		{
			actor:"ROLE_IRB_ASSIGNER",
			forms:['new-submission','modification','continuing-review'],
			canAssignTo:[["Consent Reviewer", "ROLE_IRB_CONSENT_REVIEWER"],["IRB Prereview","ROLE_IRB_PREREVIEW"],["IRB Office","ROLE_IRB_OFFICE"]]
		},
		{
			actor:"ROLE_IRB_ASSIGNER",
			forms:['emergency-use','human-subject-research-determination','study-closure'],
			canAssignTo:[["IRB Office","ROLE_IRB_OFFICE"]]
		},
		{
			actor:"ROLE_IRB_ASSIGNER",
			forms:['audit'],
			canAssignTo:[["IRB Prereview","ROLE_IRB_PREREVIEW"]]
		},
		{
			actor:"ROLE_IRB_ASSIGNER",
			forms:['reportable-new-information','humanitarian-use-device-renewal','staff'],
			canAssignTo:[["IRB Prereview","ROLE_IRB_PREREVIEW"],["IRB Office","ROLE_IRB_OFFICE"]]
		},
		{
			actor:"ROLE_REGULATORY_MANAGER",
			forms:['new-submission','modification'],
			canAssignTo:[["Regulatory Reviewer", "ROLE_MONITORING_REGULATORY_QA_REVIEWER"]]
		}];
		for (var i=0, l=assignmentRules.length;i<l;i++){
			if (assignmentRules[i].actor == actor && assignmentRules[i].forms.hasValue(formTypeId)){
				var st = new Ext.data.ArrayStore({
					autoDestroy:true,
					fields:['roledisplayname','roleid'],
					data: assignmentRules[i].canAssignTo
				});
				clog("getActorStore: returning",assignmentRules[i].canAssignTo,st);
				return st;
			}
		}
		cwarn("getActorStore: returning empty array");
		return new Ext.data.ArrayStore({
			autoDestroy:true,
			fields:['roledisplayname','roleid'],
			data: []
		});
	}

	

});
