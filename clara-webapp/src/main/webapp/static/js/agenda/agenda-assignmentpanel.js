Ext.ns('Clara.Agenda');

Clara.Agenda.SelectedAgendaItemReviewer = {};

Clara.Agenda.ResetSelectedAgendaOrder = function(){
	var a = Clara.Agenda.SelectedAgenda;

	Ext.Msg.show({
		title:'Reset agenda item order?',
		msg:'This will reset the order of the agenda items back to normal. Continue?',
		buttons:{'yes':'Reset order','no':'Cancel'},
		fn:function(btn){
			if (btn == 'yes'){

				var st = Ext.getCmp('clara-protocol-agenda-panel').getStore();
				clog("SORT!!!!!",st);
				
			}
		},
	    icon: Ext.MessageBox.QUESTION
	});
};

Clara.Agenda.ApproveSelectedAgenda = function(){
	var a = Clara.Agenda.SelectedAgenda;
	Ext.Msg.show({
		title:'Approve agenda?',
		msg:'Once you approve this agenda, notifications will be sent out to the committee members. The IRB Office may add items during this period, and they will be responsible for any further notifications to the committee. Continue?',
		buttons:{'yes':'Approve and Send Notifications','no':'Cancel'},
		fn:function(btn){
			if (btn == 'yes'){
				var url = appContext + "/ajax/agendas/"+a.id+"/approve";
				jQuery.ajax({
					  type: 'POST',
					  async:false,
					  url: url,
					  data: {userId: claraInstance.user.id},
					  success: function(){
						  Clara.Agenda.MessageBus.fireEvent('agendainfoupdated', this);  
					  },
					  error: function(){
						  Clara.Agenda.MessageBus.fireEvent('error', this); 
					  }
				});
			}
		},
	    icon: Ext.MessageBox.QUESTION
	});
};

Clara.Agenda.StartAgendaMeeting = function(){
	var a = Clara.Agenda.SelectedAgenda;
	var url = appContext + "/agendas/"+a.id+"/meeting";
	location.href = url;
};
 
Clara.Agenda.OpenSelectedAgenda = function(){
	var a = Clara.Agenda.SelectedAgenda;
	var aiid = Ext.getCmp("clara-protocol-agenda-panel").getStore().getAt(0).get("id");
	var st = a.agendaStatus;
	var url = appContext + "/agendas/"+a.id+"/agenda-items/"+aiid+"/view";
	location.href = url;
};


Clara.Agenda.SendSelectedAgenda = function(){

	
	if (Clara.Agenda.SelectedAgenda.agendaStatus == null || Clara.Agenda.SelectedAgenda.agendaStatus == 'AGENDA_INCOMPLETE')
	{
		Ext.Msg.show({
			title:'Send Agenda to Chair',
			msg:'<h1>This agenda will be sent to the chair for approval.</h1>Once approved, notifications will automatically be sent to the agenda committee members. Do you want to send this agenda to the chair?',
			buttons:Ext.Msg.YESNOCANCEL,
			fn:function(btn){
				if (btn == 'yes'){
					var a = Clara.Agenda.SelectedAgenda;
					var url = appContext + "/ajax/agendas/"+a.id+"/send-for-approval";
					jQuery.ajax({
						  type: 'POST',
						  async:false,
						  url: url,
						  data: {userId: claraInstance.user.id},
						  success: function(){
							  Clara.Agenda.MessageBus.fireEvent('agendainfoupdated', this);  
						  },
						  error: function(){
							  Clara.Agenda.MessageBus.fireEvent('error', this); 
						  }
					});
				}
			},
		    icon: Ext.MessageBox.QUESTION
		});
	} else {
		Ext.Msg.show({
			title:'Send Agenda',
			msg:'<h1>This agenda has already been approved by the chair.</h1>Why are you resending this agenda to agenda comittee members?',
			prompt:true,
			buttons:Ext.Msg.YESNOCANCEL,
			fn:function(btn, reason){
				if (btn == 'yes'){
					if (btn == 'yes'){
						var a = Clara.Agenda.SelectedAgenda;
						var url = appContext + "/ajax/agendas/"+a.id+"/send-for-approval";
						jQuery.ajax({
							  type: 'POST',
							  async:false,
							  url: url,
							  data: {userId: claraInstance.user.id, reason:reason},
							  success: function(){
								  Clara.Agenda.MessageBus.fireEvent('agendainfoupdated', this);  
							  },
							  error: function(){
								  Clara.Agenda.MessageBus.fireEvent('error', this); 
							  }
						});
					}
				}
			},
			icon: Ext.MessageBox.QUESTION
		});
	}
};

Clara.Agenda.CancelSelectedAgenda = function(){
	var a = Clara.Agenda.SelectedAgenda;
	var url = appContext + "/ajax/agendas/"+a.id+"/cancel";
	
	clog(Clara.Agenda.SelectedAgenda);
	
	if (Clara.Agenda.SelectedAgenda.agendaStatus == null || Clara.Agenda.SelectedAgenda.agendaStatus == 'AGENDA_INCOMPLETE')
	{
		Ext.Msg.show({
			title:'Cancel Agenda',
			prompt:true,
			width:350,
			msg:'<h1>Why are you cancelling this agenda?</h1>Provide a reason below (ex. "Inclement Weather") for cancelling the agenda. Cancelled agendas remain in the agenda list for auditing purposes.',
			buttons:Ext.Msg.OKCANCEL,
			fn:function(btn,reason){
				if (btn == 'ok'){
					jQuery.ajax({
						  type: 'POST',
						  async:false,
						  url: url,
						  data: {userId: claraInstance.user.id, reason:reason},
						  success: function(){
							  Clara.Agenda.MessageBus.fireEvent('agendaremoved', this);  
						  },
						  error: function(){
							  Clara.Agenda.MessageBus.fireEvent('error', this); 
						  }
					});
				}
			},
		    icon: Ext.MessageBox.QUESTION
		});
	} else {
		Ext.Msg.show({
			title:'Cannot Cancel Agenda',
			width:350,
			msg:'<h1>This agenda cannot be cancelled.</h1>',
			buttons:Ext.Msg.OK,
			fn:function(btn){
				// Dont care
			},
			icon: Ext.MessageBox.WARNING
		});
	}

};

Clara.Agenda.RemoveSelectedAgenda = function(){
	var a = Clara.Agenda.SelectedAgenda;
	var url = appContext + "/ajax/agendas/"+a.id+"/remove";
	
	//clog(a);
	
	if (Clara.Agenda.SelectedAgenda.agendaStatus == null)// && Clara.Agenda.SelectedAgenda.agendaStatus == '')
	{
		Ext.Msg.show({
			title:'Remove Agenda',
			width:350,
			msg:'<h1>Are you sure you want to remove this agenda?</h1>Items on the agenda will be moved back to the IRB office queue for reassignment.',
			buttons:Ext.Msg.YESNO,
			fn:function(btn,reason){
				if (btn == 'yes'){
					jQuery.ajax({
						  type: 'GET',
						  async:false,
						  url: url,
						  data: {userId: claraInstance.user.id},
						  success: function(){
							  Clara.Agenda.MessageBus.fireEvent('agendaremoved', this);  
						  },
						  error: function(){
							  Clara.Agenda.MessageBus.fireEvent('error', this); 
						  }
					});
				}
			},
		    icon: Ext.MessageBox.ERROR
		});
	} else {
		Ext.Msg.show({
			title:'Cannot Remove Agenda',
			msg:'<h1>This agenda cannot be removed.</h1>',
			buttons:Ext.Msg.OK,
			fn:function(btn){
				// Dont care
			},
			icon: Ext.MessageBox.WARNING
		});
	}


};

Clara.Agenda.RemoveSelectedAgendaItem = function(){
	var a = Clara.Agenda.SelectedAgenda;
	var ai = Clara.Agenda.SelectedAgendaItem;
	var url = appContext + "/ajax/agendas/"+a.id+"/agenda-items/"+ai.id+"/remove";
	jQuery.ajax({
		  type: 'GET',
		  async:false,
		  url: url,
		  data: {userId: claraInstance.user.id},
		  success: function(){
			  Clara.Agenda.MessageBus.fireEvent('itemremoved', this);  
		  },
		  error: function(){
			  Clara.Agenda.MessageBus.fireEvent('error', this); 
		  }
	});
};

Clara.Agenda.AssignReviewer = function(){
	var a = Clara.Agenda.SelectedAgenda;
	var ai = Clara.Agenda.SelectedAgendaItem;
	var ar = Clara.Agenda.SelectedAgendaReviewer;
	var url = appContext + "/ajax/agendas/"+a.id+"/agenda-items/"+ai.id+"/agenda-reviewers/assign";
	jQuery.ajax({
		  type: 'GET',
		  async:false,
		  url: url,
		  data: {userId: claraInstance.user.id, irbReviewerId:ar.id},
		  success: function(){
			  Clara.Agenda.MessageBus.fireEvent('afterassignreviewer', this);  
		  },
		  error: function(){
			  Clara.Agenda.MessageBus.fireEvent('errorassignreviewer', this); 
		  }
	});
};

Clara.Agenda.RemoveReviewer = function(){
	var a = Clara.Agenda.SelectedAgenda;
	var ai = Clara.Agenda.SelectedAgendaItem;
	var air = Clara.Agenda.SelectedAgendaItemReviewer;
	var url = appContext + "/ajax/agendas/"+a.id+"/agenda-items/"+ai.id+"/agenda-reviewers/"+air.id+"/remove";
	jQuery.ajax({
		  type: 'GET',
		  async:false,
		  url: url,
		  data: {userId: claraInstance.user.id},
		  success: function(){
			  Clara.Agenda.MessageBus.fireEvent('afterassignreviewer', this);  
		  },
		  error: function(){
			  Clara.Agenda.MessageBus.fireEvent('errorassignreviewer', this); 
		  }
	});
};

Clara.Agenda.RenderAgendaRosterRow = function(v,p,r){
	var row = "<div class='agenda-reviewer-row'>";
	var d = r.data;
	//clog(d);
	if (d.status == "NORMAL")
		row += "<h2>"+d.fname+" "+d.lname+"</h2><span>"+d.degree+" - "+d.type+" - "+d.phone+"</span>";
	else if (d.status == "REPLACED"){
		var reason = (d.reason != '')?d.reason:'No reason given';
		row += "<h2><span style='text-decoration: line-through;color:red;'>"+d.fname+" "+d.lname+"</span>&nbsp;&nbsp;&nbsp;"+d.altirbreviewerfname+" "+d.altirbreviewerlname+"</h2><span style='text-decoration: line-through;color:red;'>"+d.degree+" - "+d.type+" - "+d.phone+"</span>&nbsp;&nbsp;&nbsp;"+d.altirbreviewerphone;
		row += "<div class='agenda-reviewer-row-alternative-reason'>"+reason+"</div>";
	}else if (d.status == "REMOVED"){
		var reason = (d.reason != '')?d.reason:'No reason given';
		row += "<h2><span style='text-decoration: line-through;color:red;'>"+d.fname+" "+d.lname+"</span></h2><span style='text-decoration: line-through;color:red;'>"+d.degree+" - "+d.type+" - "+d.phone+"</span>";
		row += "<div class='agenda-reviewer-row-alternative-reason'>"+reason+" - <a href='javascript:;' onClick='Clara.Agenda.UndoRemoveAgendaPerson("+d.id+");'>Undo</a></div>";
	} else if (d.status == "ADDITIONAL"){
		var reason = (d.reason != '')?d.reason:'No reason given';
		row += "<h2><span style='color:blue;'>"+d.fname+" "+d.lname+"</span></h2><span style='color:blue;'>"+d.degree+" - "+d.type+" - "+d.phone+"</span>";
		row += "<div class='agenda-reviewer-row-alternative-reason'>"+reason+" - <a href='javascript:;' onClick='Clara.Agenda.UndoAddAdditionalAgendaPerson("+d.id+");'>Undo</a></div>";
	}
	return row + "</div>";
};

Clara.Agenda.UndoAddAdditionalAgendaPerson = function(id){
	var a = Clara.Agenda.SelectedAgenda;
	var url = appContext + "/ajax/agendas/"+a.id+"/agenda-irb-reviewers/delete";
	jQuery.ajax({
		  type: 'POST',
		  async:false,
		  url: url,
		  data: {userId: claraInstance.user.id, agendaIRBReviewerId:id},
		  success: function(){
			  Clara.Agenda.MessageBus.fireEvent('agendarosterupdated', this); 
		  },
		  error: function(){
			  Clara.Agenda.MessageBus.fireEvent('error', this); 
		  }
	});
};

Clara.Agenda.UndoRemoveAgendaPerson = function(id){
	var a = Clara.Agenda.SelectedAgenda;
	var url = appContext + "/ajax/agendas/"+a.id+"/agenda-irb-reviewers/add";
	jQuery.ajax({
		  type: 'POST',
		  async:false,
		  url: url,
		  data: {userId: claraInstance.user.id, agendaIRBReviewerId:id},
		  success: function(){
			  Clara.Agenda.MessageBus.fireEvent('agendarosterupdated', this); 
		  },
		  error: function(){
			  Clara.Agenda.MessageBus.fireEvent('error', this); 
		  }
	});
};

Clara.Agenda.RenderReviewerRow = function(v,p,r){
	var st = null;
	var row = "<div class='agenda-reviewer-row'>";
	var d = r.data;
	row += "<h2>"+d.fname+" "+d.lname+"</h2><span>"+d.degree+" - "+d.type+" - "+d.phone+"</span>";
	return row + "</div>";
};

Clara.Agenda.ReviewAssignmentWindow = Ext.extend(Ext.Window, {
	id: 'clara-protocol-agenda-reviewassignment-window',
    title: 'Assign reviewers',
    modal:true,
    width: 800,
    height: 400,
    layout: 'border',
    agenda:{},
    agendaitem:{},
	constructor:function(config){		
		Clara.Agenda.ReviewAssignmentWindow.superclass.constructor.call(this, config);
		Clara.Agenda.MessageBus.on('agendareviewerselected', this.onAgendaReviewerSelected, this);
		Clara.Agenda.MessageBus.on('afterassignreviewer', this.onAgendaReviewerAssigned, this);
		Clara.Agenda.MessageBus.on('agendaitemreviewerselected', this.onAgendaItemReviewerSelected, this);
	},
	onAgendaReviewerAssigned: function(r){
		// reload all..
		Ext.getCmp("clara-protocol-agenda-reviewassignment-availablegridpanel").getStore().removeAll();
		Ext.getCmp("clara-protocol-agenda-reviewassignment-availablegridpanel").getStore().load();
		Ext.getCmp("clara-protocol-agenda-reviewassignment-assignedgridpanel").getStore().removeAll();
		Ext.getCmp("clara-protocol-agenda-reviewassignment-assignedgridpanel").getStore().load();
		Ext.getCmp("btnAssignReviewer").setDisabled(true);
		Ext.getCmp("btnRemoveReviewer").setDisabled(true);
	},
	onAgendaReviewerSelected: function(r){
		//clog("HEARD AGENDA REVEIWER SELECT:");
		Ext.getCmp("clara-protocol-agenda-reviewassignment-assignedgridpanel").getSelectionModel().clearSelections();
		//clog(r);
		Clara.Agenda.SelectedAgendaReviewer = r;
		Ext.getCmp("btnAssignReviewer").setDisabled(false);
		Ext.getCmp("btnRemoveReviewer").setDisabled(true);
	},
	onAgendaItemReviewerSelected: function(r){
		//clog("HEARD AGENDAITEM REVIEWER SELECT:");
		Ext.getCmp("clara-protocol-agenda-reviewassignment-availablegridpanel").getSelectionModel().clearSelections();
		//clog(r);
		Clara.Agenda.SelectedAgendaItemReviewer = r;
		Ext.getCmp("btnAssignReviewer").setDisabled(true);
		Ext.getCmp("btnRemoveReviewer").setDisabled(false);
	},
	buttons:[{
		id:'btnAssignAlternate',
		text:'Done',
		handler: function(){Ext.getCmp('clara-protocol-agenda-reviewassignment-window').close();}
	}],
	initComponent: function(){
		var t = this;
		var config = {
				items:[{
					id:'clara-protocol-agenda-reviewassignment-availablegridpanel',
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
							Clara.Agenda.MessageBus.fireEvent('agendareviewerselected', record.data);  
					    }
					},
					view: new Ext.grid.GridView({
			    		forceFit:true,
			    		emptyText:''
			    	}),
					store:{
						xtype:'jsonstore',
						url: appContext + "/ajax/agendas/"+Clara.Agenda.SelectedAgenda.id+"/irb-reviewers/list",
			    		autoLoad:true,
			    		fields: [
			    					{name:'id'},
			    					{name:'userid',mapping:'user.id'},
			    					{name:'username', mapping:'user.username'},
			    					{name:'fname', mapping:'user.person.firstname'},
			    					{name:'lname', mapping:'user.person.lastname'},
			    					{name:'phone', mapping:'user.person.workphone'},
			    					{name:'alternativeMember'},
			    					{name:'affiliated'},
			    					{name:'degree'},
			    					{name:'irbRoster'},
			    					{name:'comment'},
			    					{name:'type'}
			    				]
					},
			        columns: [
					            {header: 'Committee Reviewers', sortable: true, dataIndex: 'id',renderer:Clara.Agenda.RenderReviewerRow}
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
					    	   		Clara.Agenda.AssignReviewer();
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
					    	   		Clara.Agenda.RemoveReviewer();
					       	   }
					       }
					],
					region:'center',
					border:false, 
					bodyStyle:'border-left:1px solid #96baea; border-right:1px solid #96baea;background-color:#dee8f7;'
				},{

					id:'clara-protocol-agenda-reviewassignment-assignedgridpanel',
					xtype:'grid',
					border:false,
					width:350,
					region:'east',
				    listeners:{
					    rowclick: function(grid, rowI, event)   {
							var record = grid.getStore().getAt(rowI);
							Clara.Agenda.MessageBus.fireEvent('agendaitemreviewerselected', record.data);  
					    }
					},
					view: new Ext.grid.GridView({
			    		forceFit:true,
			    		emptyText:'There are no reviewers assigned to this agenda item. Choose a reviewer from the list on the right and click "Assign".'
			    	}),
					store:{
						xtype:'jsonstore',
						url: appContext + "/ajax/agendas/"+Clara.Agenda.SelectedAgenda.id+"/agenda-items/"+Clara.Agenda.SelectedAgendaItem.id+"/agenda-reviewers/list",
			    		autoLoad:true,
			    		fields: [
			    					{name:'id'},
			    					{name:'userid',mapping:'irbReviewer.user.id'},
			    					{name:'username', mapping:'irbReviewer.user.username'},
			    					{name:'fname', mapping:'irbReviewer.user.person.firstname'},
			    					{name:'lname', mapping:'irbReviewer.user.person.lastname'},
			    					{name:'phone', mapping:'irbReviewer.user.person.workphone'},
			    					{name:'alternativeMember',mapping:'irbReviewer.alternativeMember'},
			    					{name:'affiliated',mapping:'irbReviewer.affiliated'},
			    					{name:'degree',mapping:'irbReviewer.degree'},
			    					{name:'irbRoster',mapping:'irbReviewer.irbRoster'},
			    					{name:'comment',mapping:'irbReviewer.comment'},
			    					{name:'type',mapping:'irbReviewer.type'}
			    				]
					},
			        columns: [
					            {header: 'Reviewers assigned to this agenda item', sortable: true, dataIndex: 'id',renderer:Clara.Agenda.RenderReviewerRow}
					        ]
				
				}]
		};
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		Clara.Agenda.ReviewAssignmentWindow.superclass.initComponent.apply(this, arguments);
	}
});
Ext.reg('claraagendareviewassignmentwindow', Clara.Agenda.ReviewAssignmentWindow);


Clara.Agenda.ManageAgendaRosterWindow = Ext.extend(Ext.Window, {
	id: 'clara-agenda-manage-roster-window',
    title: 'Manage Agenda Roster',
    modal:true,
    width: 800,
    height: 400,
    layout: 'border',
    agenda:{},
    agendaitem:{},
    alternateFor:{},
    alternative:{},
    selectedAgendaPerson:{},
    selectedRosterPerson:{},
	constructor:function(config){		
		Clara.Agenda.ManageAgendaRosterWindow.superclass.constructor.call(this, config);
		Clara.Agenda.MessageBus.on('agendarosteritemselected', this.onAgendaPersonSelected, this);
		Clara.Agenda.MessageBus.on('rosteritemselected', this.onRosterPersonSelected, this);
		Clara.Agenda.MessageBus.on('agendarosterupdated', this.onAgendaRosterUpdated, this);
	},
	onAgendaRosterUpdated: function(){
		Ext.getCmp("clara-protocol-agenda-alternative-availablegridpanel").getStore().removeAll();
		Ext.getCmp("clara-protocol-agenda-alternative-availablegridpanel").getStore().load();
		Ext.getCmp('btnRemoveAssignment').setDisabled(true);
		Ext.getCmp('btnRemoveMember').setDisabled(true);
		Ext.getCmp('btnAssignAlternate').setDisabled(true);
		Ext.getCmp('btnAddMember').setDisabled(true);
	},
	onPersonSelected: function(column,rec){
		var t = this;
		clog(column,rec);
		if (Ext.isDefined(rec)){
			if (Ext.isDefined(t.selectedAgendaPerson) && Ext.isDefined(t.selectedAgendaPerson.data)){
				if (t.selectedAgendaPerson.get("status") == "REPLACED"){
					Ext.getCmp('btnRemoveAssignment').setDisabled(false);
					Ext.getCmp('btnRemoveMember').setDisabled(true);
					Ext.getCmp('btnAssignAlternate').setDisabled(true);
				} else if (t.selectedAgendaPerson.get("status") == "NORMAL"){
					Ext.getCmp('btnRemoveAssignment').setDisabled(true);
					Ext.getCmp('btnRemoveMember').setDisabled(false);
				}
			}
			
			if (Ext.isDefined(t.selectedRosterPerson.data)){
				if (column == "roster"){
					Ext.getCmp('btnAddMember').setDisabled(false);
				} else {
					Ext.getCmp('btnAddMember').setDisabled(true);
				}
			}
			
			if (Ext.isDefined(t.selectedRosterPerson.data) && Ext.isDefined(t.selectedAgendaPerson.data)){
				
				clog("iff huh",t.selectedRosterPerson,t.selectedAgendaPerson,(Ext.isDefined(t.selectedRosterPerson.data) && Ext.isDefined(t.selectedAgendaPerson.data)));
				Ext.getCmp('btnAssignAlternate').setDisabled(false);
			} else {
				Ext.getCmp('btnAssignAlternate').setDisabled(true);
			}
		}
		
	},
	onRosterPersonSelected: function(rec){
		this.selectedRosterPerson = rec;
		this.onPersonSelected("roster",rec);
	},
	onAgendaPersonSelected: function(rec){
		this.selectedAgendaPerson = rec;
		this.onPersonSelected("agenda",rec);
	},

	initComponent: function(){
		var t = this;
		var config = {
				buttons:[{
					id:'btnCancelAssignment',
					text:'Close',
					handler: function(){
						t.close();
					}}],
				listeners:{
					beforeclose:function(w){
						Clara.Agenda.MessageBus.un('agendarosteritemselected', this.onAgendaPersonSelected, this);
						Clara.Agenda.MessageBus.un('rosteritemselected', this.onRosterPersonSelected, this);
						Clara.Agenda.MessageBus.un('agendarosterupdated', this.onAgendaRosterUpdated, this);
					}
				},
				items:[{
					id:'clara-protocol-agenda-alternative-availablegridpanel',
					xtype:'grid',
					width:350,
					border:false,
					loadMask:true,
					split:true,
					region:'west',
					selModel: new Ext.grid.RowSelectionModel({
				    	singleSelect:true
				    }),
				    listeners:{
					    rowclick: function(grid, rowI, event)   {
							var record = grid.getStore().getAt(rowI);
							Clara.Agenda.MessageBus.fireEvent('agendarosteritemselected', record);
					    }
					},
					view: new Ext.grid.GridView({
						forceFit:true
			    	}),
			    	bbar:[{
			    		id:'btnRemoveMember',
			    		disabled:true,
			    		text:'Remove..',
			    		iconCls:'icn-user--minus',
			    		handler: function(){
			    			var alt = (Ext.getCmp('clara-agenda-manage-roster-window').alternateFor.data);
			    			var rec = t.selectedAgendaPerson;
			    			Ext.Msg.show({
			    				   title:'Reason for removing member?',
			    				   msg: 'You are about to remove '+rec.get("fname")+' '+rec.get("lname")+' to this one agenda only. Please provide a reason for this change. <h3>Note: This person will be removed as reviewer for any assigned items on the agenda, and will need to be reassigned manually.</h3>',
			    				   buttons: Ext.Msg.OKCANCEL,
			    				   multiline:true,
			    				   
			    				   fn: function(btn, str){
			    						if (btn == 'ok'){
			    							//clog(btn);
			    							//clog(str);
			    							var a = Clara.Agenda.SelectedAgenda;
			    							var url = appContext + "/ajax/agendas/"+a.id+"/agenda-irb-reviewers/"+rec.get("id")+"/remove";
			    							jQuery.ajax({
			    								  type: 'POST',
			    								  async:false,
			    								  url: url,
			    								  data: {userId: claraInstance.user.id,reason: str},
			    								  success: function(){
			    									  Clara.Agenda.MessageBus.fireEvent('agendarosterupdated', this); 
			    								  },
			    								  error: function(){
			    									  Clara.Agenda.MessageBus.fireEvent('error', this); 
			    								  }
			    							});
			    						}
			    				   },
			    				   animEl: 'elId',
			    				   icon: Ext.MessageBox.WARNING
			    				});

			    		}
			    	},{
			    		id:'btnAssignAlternate',
			    		text:'Assign Alternate..',
			    		iconCls:'icn-xfn-friend-met',
			    		disabled:true,
			    		handler: function(){
			    			var alt = t.selectedRosterPerson.data;//(Ext.getCmp('clara-agenda-manage-roster-window').alternative.data);
			    			var altfor = t.selectedAgendaPerson.data;// (Ext.getCmp('clara-agenda-manage-roster-window').alternateFor.data);
			    			
			    			Ext.Msg.show({
			    				   title:'Reason for assigning alternate?',
			    				   msg: 'You are about to replace '+altfor.fname+' '+altfor.lname+' with '+alt.fname+' '+alt.lname+' for this one agenda only. Please provide a reason for this change.',
			    				   buttons: Ext.Msg.OKCANCEL,
			    				   multiline:true,
			    				   
			    				   fn: function(btn, str){
			    						if (btn == 'ok'){
			    							//clog(btn);
			    							//clog(str);
			    							var a = Clara.Agenda.SelectedAgenda;
			    							var url = appContext + "/ajax/agendas/"+a.id+"/agenda-irb-reviewers/"+altfor.id+"/assign-alternate";
			    							jQuery.ajax({
			    								  type: 'POST',
			    								  async:false,
			    								  url: url,
			    								  data: {userId: claraInstance.user.id,alternateIRBReviewerId: alt.id,reason: str},
			    								  success: function(){
			    									  Clara.Agenda.MessageBus.fireEvent('agendarosterupdated', this); 
			    								  },
			    								  error: function(){
			    									  Clara.Agenda.MessageBus.fireEvent('error', this); 
			    								  }
			    							});
			    							// /ajax/agendas/{agendaId}/agenda-irb-reviewers/{agendaIRBReviewerId}/
			    						}
			    				   },
			    				   animEl: 'elId',
			    				   icon: Ext.MessageBox.WARNING
			    				});
			    			
			    			//Ext.getCmp('clara-agenda-manage-roster-window').close();
			    		}

			    	},{
		id:'btnRemoveAssignment',
		text:'Remove Alternate..',
		iconCls:'icn-xfn',
		disabled:true,
		handler: function(){
			var alt = t.selectedRosterPerson.data;//(Ext.getCmp('clara-agenda-manage-roster-window').alternative.data);
			var altfor = t.selectedAgendaPerson.data;// (Ext.getCmp('clara-agenda-manage-roster-window').alternateFor.data);
			
			Ext.Msg.show({
				   title:'Undo alternate?',
				   msg: 'You are about to remove the alternate for '+altfor.fname+' '+altfor.lname+'. Are you sure?',
				   buttons: Ext.Msg.OKCANCEL,
				   
				   fn: function(btn, str){
						if (btn == 'ok'){
							//clog(btn);
							//clog(str);
							var a = Clara.Agenda.SelectedAgenda;
							var url = appContext + "/ajax/agendas/"+a.id+"/agenda-irb-reviewers/"+altfor.id+"/remove-alternate";
							jQuery.ajax({
								  type: 'POST',
								  async:false,
								  url: url,
								  data: {userId: claraInstance.user.id},
								  success: function(){
									  Clara.Agenda.MessageBus.fireEvent('agendarosterupdated', this); 
								  },
								  error: function(){
									  Clara.Agenda.MessageBus.fireEvent('error', this); 
								  }
							});
						}
						// /ajax/agendas/{agendaId}/agenda-irb-reviewers/{agendaIRBReviewerId}/
						
				   },
				   animEl: 'elId',
				   icon: Ext.MessageBox.WARNING
				});
			
			//Ext.getCmp('clara-agenda-manage-roster-window').close();
		}
}],
					store:{
						xtype:'jsonstore',
						url: appContext + "/ajax/agendas/"+Clara.Agenda.SelectedAgenda.id+"/agenda-irb-reviewers/list",
			    		autoLoad:true,
			    		listeners:{
			    			beforeload:function(){
			    				clog("about to load from "+appContext + "/ajax/agendas/"+Clara.Agenda.SelectedAgenda.id+"/agenda-irb-reviewers/list");
			    			},
			    			load: function(){
			    				clog("loading..");
			    				Ext.getCmp("clara-protocol-agenda-alternate-assignedgridpanel").getStore().load();
			    			}
			    		},
			    		fields: [
			    					{name:'id'},
			    					{name:'status'},
			    					{name:'reason', mapping:'reason'},
			    					{name:'irbreviewerid', mapping:'irbReviewer.id'},
			    					{name:'userid',mapping:'irbReviewer.user.id'},
			    					{name:'username', mapping:'irbReviewer.user.username'},
			    					{name:'fname', mapping:'irbReviewer.user.person.firstname'},
			    					{name:'lname', mapping:'irbReviewer.user.person.lastname'},
			    					{name:'phone', mapping:'irbReviewer.user.person.workphone'},
			    					{name:'alternativeMember', mapping:'irbReviewer.alternativeMember'},
			    					{name:'affiliated', mapping:'irbReviewer.affiliated'},
			    					{name:'degree', mapping:'irbReviewer.degree'},
			    					{name:'irbRoster',mapping:'irbReviewer.irbRoster'},
			    					{name:'comment', mapping:'irbReviewer.comment'},
			    					{name:'type', mapping:'irbReviewer.type'},
			    					
			    					{name:'altirbreviewerid',convert:function(v,node){
			    						if (node.alternateIRBReviewer){
			    							return node.alternateIRBReviewer.id;
			    						} else return null;
			    					}},
			    					{name:'altirbreviewerfname',convert:function(v,node){ 
			    						if (node.alternateIRBReviewer){
			    							return node.alternateIRBReviewer.user.person.firstname;
			    						} else return null;
			    					}},
			    					{name:'altirbreviewerlname',convert:function(v,node){ 
			    						if (node.alternateIRBReviewer){
			    							return node.alternateIRBReviewer.user.person.lastname;
			    						} else return null;
			    					}},
			    					{name:'altirbreviewerphone',convert:function(v,node){ 
			    						if (node.alternateIRBReviewer){
			    							return node.alternateIRBReviewer.user.person.workphone;
			    						} else return null;
			    					}}
			    				]
					},
			        columns: [
					            {header: 'Agenda Roster for '+Ext.util.Format.date(Clara.Agenda.SelectedAgenda.date), sortable: true, dataIndex: 'id',renderer:Clara.Agenda.RenderAgendaRosterRow}
					        ]
				},{

					id:'clara-protocol-agenda-alternate-assignedgridpanel',
					xtype:'grid',
					border:false,
					width:350,
					region:'center',
					loadMask:true,
				    listeners:{
					    rowclick: function(grid, rowI, event)   {
							var record = grid.getStore().getAt(rowI);
							// Ext.getCmp('clara-agenda-manage-roster-window').alternative = record;
							Clara.Agenda.MessageBus.fireEvent('rosteritemselected', record);
					    }
					},
					view: new Ext.grid.GridView({
						forceFit:true,
			    		getRowClass: function(record, index){
			    			var ast = Ext.getCmp("clara-protocol-agenda-alternative-availablegridpanel").getStore();
			    			var personInCurrentAgenda = (ast.find("userid",record.get("userid")) > -1);
			    			return (personInCurrentAgenda)?'roster-row-exists':'roster-row';
			    		},
			    		emptyText:'<h1>Wow, that\'s weird.</h1>There doesn\'t seem to be any people on the IRB roster. This shouldn\'t happen in real life.'
			    	}),
			    	bbar:[{
						id:'btnAddMember',
						disabled:true,
						iconCls:'icn-user--plus',
						text:'Add to Agenda Roster..',
						handler: function(){
							var alt = t.selectedRosterPerson.data;//(Ext.getCmp('clara-agenda-manage-roster-window').alternative.data);
							
							Ext.Msg.show({
								   title:'Reason for adding member?',
								   msg: 'You are about to add '+alt.fname+' '+alt.lname+' to this one agenda only. Please provide a reason for this change.',
								   buttons: Ext.Msg.OKCANCEL,
								   multiline:true,
								   
								   fn: function(btn, str){
										if (btn == 'ok'){
											//clog(btn);
											//clog(str);
											var a = Clara.Agenda.SelectedAgenda;
											var url = appContext + "/ajax/agendas/"+a.id+"/agenda-irb-reviewers/add-additional";
											jQuery.ajax({
												  type: 'POST',
												  async:false,
												  url: url,
												  data: {userId: claraInstance.user.id,irbReviewerId: alt.id,reason: str},
												  success: function(){
													  Clara.Agenda.MessageBus.fireEvent('agendarosterupdated', this); 
												  },
												  error: function(){
													  Clara.Agenda.MessageBus.fireEvent('error', this); 
												  }
											});
										}
								   },
								   animEl: 'elId',
								   icon: Ext.MessageBox.WARNING
								});

						}
				}],
					store:{
						xtype:'jsonstore',
						url: appContext + "/ajax/rosters/list",
			    		autoLoad:false,
			    		fields: [
			    					{name:'id'},
			    					{name:'userid',mapping:'user.id'},
			    					{name:'username', mapping:'user.username'},
			    					{name:'fname', mapping:'user.person.firstname'},
			    					{name:'lname', mapping:'user.person.lastname'},
			    					{name:'phone', mapping:'user.person.workphone'},
			    					{name:'alternativeMember',mapping:'alternativeMember'},
			    					{name:'affiliated',mapping:'affiliated'},
			    					{name:'degree',mapping:'degree'},
			    					{name:'irbRoster',mapping:'irbRoster'},
			    					{name:'comment',mapping:'comment'},
			    					{name:'type',mapping:'type'}
			    				]
					},
			        columns: [
					            {header: 'Available Roster', sortable: true, dataIndex: 'id',renderer:Clara.Agenda.RenderReviewerRow}
					        ]
				
				}]
		};
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		Clara.Agenda.ManageAgendaRosterWindow.superclass.initComponent.apply(this, arguments);
	}
});
Ext.reg('claramanageagendarosterwindow', Clara.Agenda.ManageAgendaRosterWindow);


Clara.Agenda.NewWindow = Ext.extend(Ext.Window, {
	id: 'clara-protocol-agenda-new-window',
    title: 'New Agenda Date',
    modal:true,
    width: 423,
    height: 121,
    layout: 'absolute',
	constructor:function(config){		
		Clara.Agenda.NewWindow.superclass.constructor.call(this, config);
	},
	buttons:[{
		id:'btnSaveNewAgenda',
		text:'Save',
		handler: function(){
			// SAVE
			var fldADate = Ext.getCmp('fldNewAgendaDate');
			var fldAComm = Ext.getCmp('fldAgendaCommittee');
			if (fldADate.validate() && fldAComm.validate()){
				var dt = (fldADate.getValue().getMonth()+1) + "/" + fldADate.getValue().getDate() + "/" + fldADate.getValue().getFullYear();
				new Clara.Agenda.AgendaDate({agendaDate:dt, irbRoster:fldAComm.getValue()}).save();
				Ext.getCmp('clara-protocol-agenda-new-window').close();
			} else {
				alert("Choose a valid date and committee");
			}
		}
	}],
	initComponent: function(){
		var config = {
				items:[
			            {
			                xtype: 'datefieldplus',
			                id:'fldNewAgendaDate',
			                allowBlank:false,
			    			noOfMonth : 2,
			    			noOfMonthPerRow : 2,
			    			showWeekNumber: false,
			    			useQuickTips:false,
			    			multiSelection:false,
			                x: 50,
			                y: 20,
			                width: 120
			            },
			            {
			                xtype: 'label',
			                text: 'Date',
			                x: 10,
			                y: 20,
			                style: 'font-size:14px;'
			            },
			            {
			                xtype: 'label',
			                text: 'Committee',
			                x: 190,
			                y: 20,
			                style: 'font-size:14px;'
			            },
			            {
			                xtype: 'combo',
			                id:'fldAgendaCommittee',
			                typeAhead: true,
			                allowBlank:false,
                            triggerAction: 'all',
                            store: new Ext.data.SimpleStore({
                               fields:['committee','id'],
                               data: [['Committee 1','WEEK_1'],['Committee 2','WEEK_2'],['Committee 3','WEEK_3'],['Committee 4','WEEK_4']]
                            }),
                            lazyRender: true,
                            displayField:'committee',
                            valueField:'id',
                            mode:'local',
                            selectOnFocus:true,
			                width: 130,
			                x: 270,
			                y: 20
			            }
			        ]
		};
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		Clara.Agenda.NewWindow.superclass.initComponent.apply(this, arguments);
	}
});
Ext.reg('claraagendanewwindow', Clara.Agenda.NewWindow);