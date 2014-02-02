Ext.ns('Clara.Queues');

Clara.Queues.GetActorStore = function(actor, form){
	for (var i=0;i<Clara.Queues.AssignmentRules.length;i++){
		if (Clara.Queues.AssignmentRules[i].actor == actor && Clara.Queues.AssignmentRules[i].forms.hasValue(form)){
			return new Ext.data.ArrayStore({
				autoDestroy:true,
				fields:['roledisplayname','roleid'],
				data: Clara.Queues.AssignmentRules[i].canAssignTo
			});
		}
	}
};

Clara.Queues.AssignmentRules = [{
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

Clara.Queues.Reassign = function(qitem,q, callbackFn, triggerWorkflow){
	triggerWorkflow = (typeof triggerWorkflow == "undefined")?true:triggerWorkflow;
	clog("Clara.Queues.Reassign",qitem,q,callbackFn,triggerWorkflow);
	new Clara.Queues.ReviewAssignmentWindow({
		queueitem : qitem,
		editing: true,
		triggerWorkflow:triggerWorkflow,
		queuetype:q.objectType,
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
		}
	}).show();
	
	// {roleId:'ROLEHERE',formId:111,committee:'COMITTEEHERE'}, {objectType:'Protocol'}
};

Clara.Queues.Assign = function(qitem,q, callbackFn, triggerWorkflow){
	triggerWorkflow = (typeof triggerWorkflow == "undefined")?true:triggerWorkflow;
	clog("Clara.Queues.Assign",qitem,q,callbackFn,triggerWorkflow);
	new Clara.Queues.ReviewAssignmentWindow({
		queueitem : qitem,
		editing: false,
		triggerWorkflow:triggerWorkflow,
		queuetype:q.objectType,
		callback:function(){
			if (typeof callbackFn == "undefined"){
				clog("Clara.Queues.Assign: callback undefined, using standard");
				var pnl = (q.objectType == "Protocol")?Ext.getCmp("clara-protocol-db-formgridpanel"):Ext.getCmp("clara-contract-db-formgridpanel");
				if (typeof pnl != "undefined"){
					clog("pnl",pnl);
					pnl.getStore().reload();
				}else {
					clog("pnl undefined",qitem,q);
				}
			} else {
				clog("Clara.Queues.Assign: callback defined, calling..");
				callbackFn();
			}
		}
	}).show();
	
	// {roleId:'ROLEHERE',formId:111,committee:'COMITTEEHERE'}, {objectType:'Protocol'}
};

Clara.Queues.ReloadAssignmentPanel = function(gp,qitem){
	if (typeof gp != 'undefined'){
		var st = gp.getStore();
		var url = appContext+"/ajax/"+claraInstance.type+"/"+claraInstance.type+"-forms/"+(claraInstance.form.id?claraInstance.form.id:qitem.formId)+"/list-reviewers";
		st.removeAll();
		st.proxy.setUrl(url);
		st.load({params:{
			committee: qitem.committee, userId:claraInstance.user.id, roleId:qitem.roleId
		}});
	}
};

Clara.Queues.GetQueueItemAssignmentArrayStore = function(qitem){

	clog("getting assignments for qitem",qitem);
	var windowAssignmentGridPanel = Ext.getCmp("clara-protocol-queues-reviewassignment-assignedgridpanel");
	var contractProtocolInfoAssignmentGridPanel = Ext.getCmp("clara-contract-protocol-assignedgridpanel");
	Clara.Queues.ReloadAssignmentPanel(windowAssignmentGridPanel,qitem);
	Clara.Queues.ReloadAssignmentPanel(contractProtocolInfoAssignmentGridPanel,qitem);
};

Clara.Queues.GetQueueItemReviewerStore = function(role){
	
	// var role = Clara.Queues.GetAssignToRole(qitem);	// CHANGE TO: var role = qitem.roleId;
	// var role = qitem.roleId;
	clog("assignToRole:",role);
	if(role == null) return null;
	var store = new Ext.data.JsonStore({
		header :{
	        'Accept': 'application/json'
	    },
	    proxy: new Ext.data.HttpProxy({
	    	url: appContext + "/ajax/users/list-user-role-by-roles",
			method:'GET'
		}),
		baseParams: {roles:[role]},
		autoLoad:false,
		fields: [
		    {name:'id'},
		    {name:'userId', mapping:'user.id'},
		    {name:'roleName', mapping:'role.name'},
		    {name:'roleIdentifier', mapping:'role.rolePermissionIdentifier'},
		    {name:'username', mapping:'user.username'},
		    {name:'email', mapping:'user.person.email'},
		    {name:'firstname', mapping:'user.person.firstname'},
		    {name:'lastname', mapping:'user.person.lastname'},
		    {name:'middlename', mapping:'user.person.middlename'}
		]
	});
	
	return store;
};

Clara.Queues.AssignReviewerToFormItem = function(queueitem, reviewerUserRoleId, callback){
	
	jQuery.ajax({
		url: appContext + "/ajax/queues/assign-reviewer",
		type: "GET",
		async: false,
		data: {
			"objectType": Clara.Queues.SelectedQueue.objectType || Ext.util.Format.capitalize(claraInstance.type),
			"formId": queueitem.formId,
			"committee": queueitem.committee,
			"userId": claraInstance.user.id,
			"reviewerUserRoleId":reviewerUserRoleId
		},
		success: function(data){
			if (callback) callback();
		}
	});
};

Clara.Queues.RemoveReviewerFromFormItem = function(queueitem, reviewerUserRoleId, callback){
	
	jQuery.ajax({
		url: appContext + "/ajax/queues/remove-reviewer",
		type: "GET",
		async: false,
		data: {
			"objectType": Clara.Queues.SelectedQueue.objectType || Ext.util.Format.capitalize(claraInstance.type),
			"formId": queueitem.formId,
			"committee": queueitem.committee,
			"userId": claraInstance.user.id,
			"reviewerUserRoleId":reviewerUserRoleId
		},
		success: function(data){
			if (callback) callback();
		}
	});
};

Clara.Queues.CompleteAssignment = function(queueitem, action, callback){
	jQuery.ajax({
		url: appContext + "/ajax/queues/complete-assign-reviewer",
		type: "GET",
		async: false,
		data: {
			"objectType": Clara.Queues.SelectedQueue.objectType || Ext.util.Format.capitalize(claraInstance.type),
			"formId": queueitem.formId,
			"committee": queueitem.committee,
			"userId": claraInstance.user.id,
			"action":action
		},
		success: function(data){
			if (callback) callback();
		}
	});
};


Clara.Queues.RenderReviewerRow = function(v,p,r){
	var row = "";
	row = "<div class='agenda-reviewer-row'>";
	var d = r.data;
	row += "<h2>"+d.firstname+" "+d.lastname+"</h2><span>"+d.email+"</span>";
	return row + "</div>";
};

Clara.Queues.RenderAssignedReviewerRow = function(v,p,r){
	var row = "";
	clog("RenderAssignedReviewerRow",r);
	row = "<div class='agenda-reviewer-row'>";
	var d = r.data;
	row += "<h2>"+d.reviewerName+"<span style='font-weight:100;'> &gt; "+Clara.HumanReadableRoleName(r.get("reviewerRoleName"))+"</span></h2>";
	return row + "</div>";
};


Clara.Queues.ReviewAssignmentWindow = Ext.extend(Ext.Window, {
	id: 'clara-protocol-queues-reviewassignment-window',
    title: 'Assign reviewers',
    modal:true,
    width: 800,
    height: 400,
    closable:true,
    layout: 'border',
    triggerWorkflow:true,
    editing:false,
    queue:{},
    callback:function(){},
    queueitem:{},
    selectedRole:{},
    onClose:null,
	constructor:function(config){		
		Clara.Queues.ReviewAssignmentWindow.superclass.constructor.call(this, config);
	},
	onReviewerAssigned: function(r){
		// reload all..
		var t = Ext.getCmp("clara-protocol-queues-reviewassignment-window");
		Ext.getCmp("clara-protocol-queues-reviewassignment-availablegridpanel").getStore().removeAll();
		Ext.getCmp("clara-protocol-queues-reviewassignment-availablegridpanel").getStore().load();
		
		if (typeof t.queueitem == "undefined") cwarn("queueitem is undefined.",t);
		Clara.Queues.GetQueueItemAssignmentArrayStore(t.queueitem);
		//Ext.getCmp("clara-protocol-queues-reviewassignment-assignedgridpanel").getStore().removeAll();
		//Ext.getCmp("clara-protocol-queues-reviewassignment-assignedgridpanel").getStore().loadData(Clara.Queues.GetQueueItemAssignmentArrayStore(t.queueitem));
		
		Ext.getCmp("btnAssignReviewer").setDisabled(true);
		Ext.getCmp("btnRemoveReviewer").setDisabled(true);
	},
	onReviewerRemoved: function(r){
		// reload all..
		var t = Ext.getCmp("clara-protocol-queues-reviewassignment-window");

		if (typeof t.queueitem == "undefined") cwarn("queueitem is undefined.",t);
		Clara.Queues.GetQueueItemAssignmentArrayStore(t.queueitem);
		//Ext.getCmp("clara-protocol-queues-reviewassignment-assignedgridpanel").getStore().removeAll();
		//Ext.getCmp("clara-protocol-queues-reviewassignment-assignedgridpanel").getStore().loadData(Clara.Queues.GetQueueItemAssignmentArrayStore(t.queueitem));
		
		Ext.getCmp("btnAssignReviewer").setDisabled(true);
		Ext.getCmp("btnRemoveReviewer").setDisabled(true);
	},
	onReviewerSelected: function(r){
		Ext.getCmp("clara-protocol-queues-reviewassignment-assignedgridpanel").getSelectionModel().clearSelections();
		Clara.Queues.SelectedReviewer = r;
		Ext.getCmp("btnAssignReviewer").setDisabled(false);
		Ext.getCmp("btnRemoveReviewer").setDisabled(true);
	},
	onItemReviewerSelected: function(r){
		Ext.getCmp("clara-protocol-queues-reviewassignment-availablegridpanel").getSelectionModel().clearSelections();
		Clara.Queues.SelectedItemReviewer = r;
		Ext.getCmp("btnAssignReviewer").setDisabled(true);
		Ext.getCmp("btnRemoveReviewer").setDisabled(false);
	},

	queueUpdated: function(queue){
		if (typeof Ext.getCmp("clara-protocol-queues-reviewassignment-assignedgridpanel") != 'undefined'){
			clog("queue loaded, refreshing assigned reviewer list..",queue);
			Clara.Queues.GetQueueItemAssignmentArrayStore(t.queueitem);
			//Ext.getCmp("clara-protocol-queues-reviewassignment-assignedgridpanel").getStore().loadData(Clara.Queues.GetQueueItemAssignmentArrayStore(t.queueitem));
		}
	},
	initComponent: function(){
		var t = this;
		var actor = t.queueitem.roleId;
		var formTypeId= t.queueitem.formTypeId;
		var queueItemReviewerStore = Clara.Queues.GetQueueItemReviewerStore(t.queueitem.roleId);
		
		clog("Available store for combo",Clara.Queues.GetActorStore(actor,formTypeId),actor,formTypeId);
		
		Clara.Queues.MessageBus.addListener(
				'queueloaded', t.queueUpdated);
		var config = {
				buttons:[{
					id:'btnSaveAssignment',
					text:'Save Assignment',
					handler: function(){
						if (t.triggerWorkflow) {
							Clara.Queues.CompleteAssignment(t.queueitem, ((t.editing)?"UPDATE_REVIEWER":"ASSIGN_REVIEWER"), function(){if(typeof t.callback != 'undefined')t.callback();t.close();});
						}
						else {
							if(typeof t.callback != 'undefined')t.callback();t.close();
						}
					}
				}],
				listeners:{
					close: function(t){
						clog("Closing window",t,Clara.Queues.MessageBus);
						if (typeof Clara.Queues.MessageBus != "undefined") Clara.Queues.MessageBus.fireEvent('queueitemassigned', Clara.Queues.SelectedQueue);
						if (t.onClose) t.onClose();
						if (t.callback())t.callback();
					}
				},
				items:[{xtype:'form',
					border:false,
					labelWidth:140,
					padding:6,
					region:'north',
					height:34,
					bodyStyle:'border-bottom:1px solid #96baea;',
					items:[{
						xtype:'combo',
						fieldLabel:'What kind of reviewer',
                        typeAhead:false,
                        forceSelection:true,
                        itemId: 'fldReviewerType',
                        store: Clara.Queues.GetActorStore(actor,formTypeId),
                        displayField:'roledisplayname', 
                        valueField:'roleid',
                        editable:false,
			        	allowBlank:false,
                        mode:'local', 
                        id: 'fldReviewerType',
                        triggerAction:'all',
                        listeners:{
                        	select:function(cb,rec,idx){
                           		clog("fldReviewerType: select",cb,rec,idx);
                           		t.selectedRole = rec.get("roleid");
                        		queueItemReviewerStore.removeAll();
                        		queueItemReviewerStore = Clara.Queues.GetQueueItemReviewerStore(t.selectedRole);
                        		queueItemReviewerStore.load();
                        		Ext.getCmp("clara-protocol-queues-reviewassignment-availablegridpanel").reconfigure(queueItemReviewerStore, Ext.getCmp("clara-protocol-queues-reviewassignment-availablegridpanel").colModel);
                        		
                        	},
                        	change:function(cb,v,ov){
                         	}
                        }
					}]},{
					id:'clara-protocol-queues-reviewassignment-availablegridpanel',
					xtype:'grid',
					width:350,
					border:false,
					region:'west',
					selModel: new Ext.grid.RowSelectionModel({
				    	singleSelect:true
				    }),
				    listeners:{
					    rowclick: function(grid, rowI, event)   {
							var record = grid.getStore().getAt(rowI);
							t.onReviewerSelected(record);
					    }
					},
					view: new Ext.grid.GridView({
			    		forceFit:true,
			    		emptyText:''
			    	}),
					store:queueItemReviewerStore,
			        columns: [
					            {header: 'Available Reviewers', sortable: true, dataIndex: 'id',renderer:Clara.Queues.RenderReviewerRow}
					        ]
				},{
					xtype:'panel',
					layout:'absolute',
					items:[
					       {
					    	   xtype:'button',
					    	   id:'btnAssignReviewer',
					    	   disabled:true,
					    	   text:'Assign', 
					    	   iconCls:'icn-arrow',
					    	   iconAlign:'right',
					    	   x:12,
					    	   y:150,
					    	   handler:function(){
					    		   clog("trying to assign to Clara.Queues.SelectedReviewer",Clara.Queues.SelectedReviewer);
					    		   Clara.Queues.AssignReviewerToFormItem(t.queueitem, Clara.Queues.SelectedReviewer.get("id"),t.onReviewerAssigned);
					       	   }
					       },
					       {
					    	   xtype:'button',
					    	   id:'btnRemoveReviewer',
					    	   disabled:true,
					    	   text:'Remove', 
					    	   iconCls:'icn-arrow-180',
					    	   
					    	   x:12,
					    	   y:180,
					    	   handler:function(){
					    		   clog("trying to remove from Clara.Queues.SelectedItemReviewer",Clara.Queues.SelectedItemReviewer);
					    		   Clara.Queues.RemoveReviewerFromFormItem(t.queueitem, Clara.Queues.SelectedItemReviewer.get("reviewerRoleId"),t.onReviewerRemoved);
					       	   }
					       }
					],
					region:'center',
					border:false, 
					bodyStyle:'border-left:1px solid #96baea; border-right:1px solid #96baea;background-color:#dee8f7;'
				},{

					id:'clara-protocol-queues-reviewassignment-assignedgridpanel',
					xtype:'grid',
					border:false,
					width:350,
					region:'east',
				    listeners:{
				    	afterrender:function(gp){
				    		Clara.Queues.GetQueueItemAssignmentArrayStore(t.queueitem);
				    	},
					    rowclick: function(grid, rowI, event)   {
							var record = grid.getStore().getAt(rowI);
							t.onItemReviewerSelected(record);
					    }
					},
					view: new Ext.grid.GridView({
			    		forceFit:true,
			    		emptyText:'There are no reviewers assigned to this item. Choose a reviewer from the list on the right and click "Assign".'
			    	}),
					store:{
						// xtype:'arraystore',
						// fields:['reviewerid','reviewerName','reviewerRoleId','reviewerRoleName','assigningCommittee'],
						xtype:'xmlstore',
						proxy : new Ext.data.HttpProxy({
							url : appContext + "/",	// changes dynamically
							method : "GET",
							data : {
								userId : claraInstance.user.id
							},
							headers : {
								'Accept' : 'application/xml;charset="utf-8"'
							}
						}),
						autoLoad : false,
						record : 'assigned-reviewer',
						root : 'assigned-reviewers',
						sortInfo:{
							field:'reviewerName',direction:'ASC'
						},
						fields : [ {
							name : 'reviewerName',
							mapping : '@user-fullname'
						}, {
							name : 'reviewerid',
							mapping : '@user-id'
						}, {
							name : 'reviewerRoleId',
							mapping : '@user-role-id'
						}, {
							name : 'reviewerRoleName',
							mapping : '@user-role'
						}, {
							name : 'assigningCommittee',
							mapping : '@assigning-committee'
						} ]
					
					},
			        columns: [
					            {header: 'Reviewers assigned to this item', sortable: true, dataIndex: 'id',renderer:Clara.Queues.RenderAssignedReviewerRow}
					        ]
				
				}]
		};
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		Clara.Queues.ReviewAssignmentWindow.superclass.initComponent.apply(this, arguments);
	}
});
Ext.reg('clarareviewreviewassignmentwindow', Clara.Queues.ReviewAssignmentWindow);
