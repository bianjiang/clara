Ext.ns('Clara.Queues');

Clara.Queues.AssignableTypeForItem = function(formTypeId){
	var types = [];
	var assignableType = {
			fullBoard: ['Full Board','FULL_BOARD'],
			expedited: ['Expedited','EXPEDITED'],
			exempt: ['Exempt','EXEMPT'],
			reported: ['Reported','REPORTED']
	};
	if (formTypeId == 'human-subject-research-determination') return types;
	else if (formTypeId == 'new-submission'){
		types.push(assignableType.fullBoard);
		types.push(assignableType.expedited);
		types.push(assignableType.exempt);
	} 
	else if (formTypeId == 'safety-report'){
		types.push(assignableType.reported);
	} 
	else if (formTypeId == 'reportable-new-information') {
		types.push(assignableType.fullBoard);
		types.push(assignableType.expedited);
	}
	else if (formTypeId == 'humanitarian-use-device'){
		types.push(assignableType.fullBoard);
		types.push(assignableType.expedited);
	}
	else if (formTypeId == 'humanitarian-use-device-renewal'){
		types.push(assignableType.fullBoard);
		types.push(assignableType.expedited);
	}
	else if (formTypeId == 'emergency-use'){
		types.push(assignableType.fullBoard);
	}
	else if (formTypeId == 'continuing-review'){
		types.push(assignableType.fullBoard);
		types.push(assignableType.expedited);
		types.push(assignableType.exempt);
	}
	else if (formTypeId == 'modification'){
		types.push(assignableType.fullBoard);
		types.push(assignableType.expedited);
	}
	else if (formTypeId == 'study-closure'){
		types.push(assignableType.expedited);
	}
	else if (formTypeId == 'audit'){
		types.push(assignableType.fullBoard);
	}
	else if (formTypeId == 'staff'){
		types.push(assignableType.fullBoard);
		types.push(assignableType.expedited);
	}
	else if (formTypeId == 'privacy-board'){
		types.push(assignableType.fullBoard);
		types.push(assignableType.expedited);
		types.push(assignableType.reported);
	}
	return types;
};

Clara.Queues.AssignItem = function(agendaId, queueitem, type){
	var aid;
	if (agendaId.getMonth){
		// agendaId was passed a date, find the corresponding id
		aid = Clara.Queues.AgendaStore.getAt(Clara.Queues.AgendaStore.find("adate",agendaId)).get("id");
		
	} else {
		aid = agendaId;
	}

	jQuery.ajax({
		url: appContext + "/ajax/agendas/"+aid+"/agenda-items/assign",
		type: "GET",
		async: false,
		data: {
			"protocolFormId": queueitem.formId,
			"agendaItemCategory": type,
			"userId": claraInstance.user.id
		},
		success: function(data){
			clog("selected queue:" + Clara.Queues.SelectedQueue);
			
			Clara.Queues.MessageBus.fireEvent('queueitemassigned', Clara.Queues.SelectedQueue);
		}
	});
};

Clara.Queues.ProcessItem = function(queueitem, type, reviewerUserRoleId){
	
	clog("queueitem: " + queueitem + "; type: " + type + "; reviewerUserRoleId: " + reviewerUserRoleId);
	
	jQuery.ajax({
		url: appContext + "/ajax/queues/committees/irb-office/process",
		type: "GET",
		async: false,
		data: {
			"protocolFormId": queueitem.formId,
			"itemCategory": type,
			"userId": claraInstance.user.id,
			"reviewerUserRoleId":reviewerUserRoleId
		},
		success: function(data){
			Ext.getCmp("clara-queues-agendaassignmentwindow").close();
			Clara.Queues.MessageBus.fireEvent('queueitemassigned', Clara.Queues.SelectedQueue);
		}
	});
};


Clara.Queues.AgendaStore = new Ext.data.JsonStore({
	scope:this,
	url: appContext + "/ajax/agendas/list-available",
	autoLoad:true,
	fields: [
	    {name:'id', mapping: 'id'},
	    {name:'agendaStatus', mapping: 'agendaStatus'},
	    {name:'adate', mapping: 'date', type:'date',dateFormat:'Y-m-d'},
	    {name:'irbRoster', mapping: 'irbRoster'}
	],
	sortInfo: {
	    field: 'adate',
	    direction: 'DESC'
	}
});

Clara.Queues.ExpeditedExemptReviewerStore = new Ext.data.JsonStore({
	scope:this,
	header :{
        'Accept': 'application/json'
    },
    proxy: new Ext.data.HttpProxy({
    	url: appContext + "/ajax/users/list-user-role-by-roles",
		method:'GET'
	}),
	baseParams: {roles:['ROLE_IRB_EXPEDITED_REVIEWER','ROLE_IRB_EXEMPT_REVIEWER']},
	autoLoad:true,
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
Clara.Queues.ExpeditedExemptReviewerStoreSelectedRoleFilter = "ROLE_IRB_EXPEDITED_REVIEWER";

Clara.Queues.ExpeditedExemptReviewerStoreFilterByRole = function(rec){
	return (rec.get("roleIdentifier") == Clara.Queues.ExpeditedExemptReviewerStoreSelectedRoleFilter);
};


Clara.Queues.AgendaAssignmentWindow = Ext.extend(Ext.Window, {
	id:'clara-queues-agendaassignmentwindow',
    title: 'Assign to IRB Agenda / Reviewer',
    width: 500,
    height: 430,
    layout: 'border',
    bodyCssClass:'queues-assign-window-body',
	queueitem:{},
	modal:true,
	constructor:function(config){		
		Clara.Queues.AgendaAssignmentWindow.superclass.constructor.call(this, config);
	},	
	initComponent: function(){
		var t = this;
		var irbPrereviewDetermination = "";

		if (t.queueitem.irbSuggestedType != "") irbPrereviewDetermination = t.queueitem.irbSuggestedType;

		irbPrereviewDetermination = (irbPrereviewDetermination == "")?"<span style='font-weight:100;color:#999;'>None</span>":irbPrereviewDetermination;
		
		this.buttons = [{
			scope:this,
			text:'Cancel',
			handler:function(){ this.close(); }
		},{
			scope:this,
			text:'Assign',
			handler:function(){
				var fldAssignAgendaItemDate = Ext.getCmp("fldAssignAgendaItemDate");
				var fldAssignItemReviewer = Ext.getCmp("fldAssignItemReviewer");
				var fldAssignAgendaItemType = Ext.getCmp("fldAssignAgendaItemType");
				

				
				if (fldAssignAgendaItemType.validate()){
					
					if (fldAssignAgendaItemType.getValue() == "EXEMPT" || fldAssignAgendaItemType.getValue() == "EXPEDITED"){
						if (!fldAssignItemReviewer.validate()){
							alert("Please choose a reviewer.");
						}
						else {
							Clara.Queues.ProcessItem(t.queueitem, fldAssignAgendaItemType.getValue(), fldAssignItemReviewer.getValue());
							t.close();
						}
					} else {
						
						if (fldAssignAgendaItemDate.getValue().getMonth) {
							Clara.Queues.AssignItem(fldAssignAgendaItemDate.getValue(), t.queueitem, fldAssignAgendaItemType.getValue());
							t.close();
						} else {
							alert("Please choose an agenda date.");
						}
						
					}
				}else{
					alert("Please complete the assignment form.");
				}

			}
		}];
        this.items = [
                      
                      {
                    	  xtype:'panel',
                    	  region:'north',
                    	  layout:'absolute',
                    	  height:85,
                    	  items:[{
                        	  scope:this,
                              xtype: 'label',
                              //text: 'IRB #',
                              x: 10,
                              y: 10,
                              width: 90,
                              style: 'font-size:14px;text-align:right;',
                              text: 'IRB #: ' + this.queueitem.identifier
                          },
                          {
                              xtype: 'label',
                              text: 'IRB TITLE',
                              x: 145,
                              y: 10,
                              width: 300,
                              cls:'sidebar-item-label',
                              style: 'color:#000;font-size:12px;',
                              text:this.queueitem.title
                          },
                          {
                              xtype: 'label',
                              text: 'IRB FORMTYPE',
                              x: 145,
                              y: 30,
                              width: 290,
                              style: 'font-size:11px;',
                              text:this.queueitem.formType
                              },
                          {
                              xtype: 'container',
                              id:'lblPrereviewDetermination',
                              html: 'Prereview Determination: <span style="font-weight:800;">'+irbPrereviewDetermination+"</span>",
                              x: 145,
                              y: 60,
                              width: 300,
                              style: 'font-size:12px;color:#000;'
                          }]
                   
                      },
                      {
   
                    	  
                    	  xtype:'form',
                    	  padding:6,
                    	  region:'center',
                    	  items:[{
                              xtype: 'combo',
                              fieldLabel:'Item Type',
                              typeAhead: false,
                              triggerAction: 'all',
                              store: new Ext.data.SimpleStore({
                              	fields:['type', 'enumtype'],
                              	data: Clara.Queues.AssignableTypeForItem(t.queueitem.formTypeId) //[['Expedited','EXPEDITED'],['Exempt','EXEMPT'],['Full Board','FULL_BOARD'],['Reported','REPORTED']]
                              }),
                              allowBlank:false,
                              displayField:'type',
                              valueField:'enumtype',
                              mode:'local',
                              triggerAction:'all',
                              name: 'fldAssignAgendaItemType',
                              id: 'fldAssignAgendaItemType',
                              listeners:{
                        	  	select:function(cb,r,idx){
                        	  		// Clear all other fields first
    		                    	  Ext.getCmp("fldAssignAgendaItemDate").clearSelectedDates();
    		                    	  Ext.getCmp("fldAssignItemReviewer").clearValue();
    		                    	  Ext.getCmp("fldAssignItemReviewer").setDisabled(false);
    		                    	  Ext.getCmp("fldAssignAgendaItemDate").setDisabled(true);
    		                    	  
    		                    	  
    		                    	  
                        	  		var t = r.data.type;
                        	  		clog("change item type:",t);
                        	  		if (t == 'Expedited'){
                        	  			clog('its expedited..');
                        	  			Clara.Queues.ExpeditedExemptReviewerStoreSelectedRoleFilter = "ROLE_IRB_EXPEDITED_REVIEWER";
                        	  			Ext.getCmp("fldAssignItemReviewer").getStore().clearFilter();
                           	  			Ext.getCmp("fldAssignItemReviewer").getStore().filterBy(function(r,id){ clog(r.get("roleIdentifier")); return (r.get("roleIdentifier") == Clara.Queues.ExpeditedExemptReviewerStoreSelectedRoleFilter); });

                        	  		} else if (t == 'Exempt') {
                        	  			Clara.Queues.ExpeditedExemptReviewerStoreSelectedRoleFilter = "ROLE_IRB_EXEMPT_REVIEWER";
                        	  			Ext.getCmp("fldAssignItemReviewer").getStore().clearFilter();
                        	  			Ext.getCmp("fldAssignItemReviewer").getStore().filterBy(function(r,id){ return (r.get("roleIdentifier") == Clara.Queues.ExpeditedExemptReviewerStoreSelectedRoleFilter); });
                        	  			
                        	  		} else {
                        	  			Ext.getCmp("fldAssignAgendaItemDate").setDisabled(false);
                        	  			Ext.getCmp("fldAssignItemReviewer").setDisabled(true);
                        	  		}
                          		}
                          	  }
                          },{
                              xtype: 'datepickerplus',
                              usePickerPlus: true,
                      		  noOfMonth: 2,
                      		noOfMonthPerRow : 2,
			    			showWeekNumber: false,
			    			useQuickTips:false,
			    			multiSelection:false,
                      		//minDate: new Date(),
                      		maxDate: new Date(2050,12,31),
                              fieldLabel:'Agenda Date',
                              typeAhead: true,
                              disabled:true,
                              
                              listeners:{
                            	"render":function(t){
                            		// populate selectable days here.
                            		clog("RENDER CAL:",t);
                            		
                            		var edates = [];
                            		var adates = [];
                            		
                            		Clara.Queues.AgendaStore.each(function(rec){
                            			clog("date",rec.get("adate"),"formatted",Ext.util.Format.date(rec.get("adate"), 'Y-m-d H:i:s'));
                            			adates.push(Ext.util.Format.date(rec.get("adate"), 'Y-m-d H:i:s'));
                            			edates.push({
                            				date:rec.get("adate"),
                            				text:rec.get("irbRoster")+", Status: "+rec.get("agendaStatus"),
                            				cls: "x-datepickerplus-eventdates"
                            			});
                            		});
                            		clog(adates,edates);
                            		t.setAllowedDates(adates);
                            		t.setEventDates(edates);
                            	}  
                              },

                              allowBlank:false,
 
                              name: 'fldAssignAgendaItemDate',
                              id: 'fldAssignAgendaItemDate',
                              itemSelector: 'div.agenda-date'
                          },
                          {
                              xtype: 'combo',
                              fieldLabel:'Expedited/Exempt Reviewer',
                              typeAhead: false,
                              width:367,
                              forceSelection:true,
                              lazyInit:false,
                              lazyRender:false,
                              store: Clara.Queues.ExpeditedExemptReviewerStore,
                              allowBlank:false,
                              displayField:'username',
                              tpl:new Ext.XTemplate(
                            		  '<tpl for="."><div class="x-combo-list-item">{lastname}, {firstname} ({email})</div></tpl>'
                                    ),
                              valueField:'id',
                              mode:'local',
                              disabled:true,
                              name: 'fldAssignItemReviewer',
                              id: 'fldAssignItemReviewer',
                              listeners:{
                            	  beforequery:function(e){
                            		  e.combo.onLoad();return false;
                            	  }
                              }
                          }
                         ]
                      }
                      
                      
                      
                  ];

        
        
		Clara.Queues.AgendaAssignmentWindow.superclass.initComponent.apply(this, arguments);
		clog(this.queueitem);
		
    }
});
Ext.reg('claraqueueagendaassignmentwindow', Clara.Queues.AgendaAssignmentWindow);