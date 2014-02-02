Ext.ns('Clara.Agenda');

Clara.Agenda.GetStatusText = function(enumStatus){
	var st = enumStatus || "Unknown";
	if (st != 'Unknown') st = enumStatus.charAt(0).toUpperCase() + enumStatus.slice(1).toLowerCase().replace(/_/g," ");
	if (st == 'Agenda incomplete') st = "New agenda";
	else if (st == 'Agenda approved') st = "Approved by chair";
	return st;
};

Clara.Agenda.GetCommitteeText = function(enumCommittee){
	return enumCommittee.replace("WEEK_","Committee ");
};

Clara.Agenda.AgendaPagePanel = Ext.extend(Ext.Panel, {
	id: 'clara-protocol-agenda-page-panel',
	frame:false,
	layout:'border',
	border:false,
	height:350,
	protocolInfoEl:"",
	constructor:function(config){		
		Clara.Agenda.AgendaPagePanel.superclass.constructor.call(this, config);
	},
	tbar: new Ext.Toolbar({
		items:[{
				xtype:'panel',
				html:'Agendas',
				padding:4,
				unstyled:true,
				bodyStyle:'font-size:24px;background:transparent;',
				border:false
			   }
           	]}),
	initComponent: function(){
		var config = {
				items:[
				       { xtype:'panel',region:'center',layout:'fit',border:false,
				    	   style:'border-left:1px solid #96baea;',
				    	   tbar:{
				    		 items:[{xtype:'panel',html:'<span id="agenda-item-details" class="disabled-row" style="font-size:14px;background:transparent;">No agenda selected.</span>',padding:4,
									unstyled:true,
									bodyStyle:'font-size:18px;background:transparent;',
									border:false},'->',{
		
										id:'btn-start-meeting',
										iconCls:'icn-projection-screen-presentation',
										text:'<strong>Show Meeting Page</strong>',
										hidden:true,
										handler:function(){
											Clara.Agenda.StartAgendaMeeting();
										}
									
									},{
		
										id:'btn-show-minutes',
										iconCls:'icn-report',
										text:'<span style="font-weight:800;margin-right:12px;">Show Minutes</span>',
										hidden:true,
										handler:function(){
											var url = appContext + "/agendas/"+Clara.Agenda.SelectedAgenda.id+"/minutes";
											window.open( url, '');
										}
									
									},{
										xtype:'button',
										id:'btn-agenda-menu',
										hidden:!(claraInstance.HasAnyPermissions(['ROLE_IRB_OFFICE','ROLE_SYSTEM_ADMIN'])),
										iconCls:'icn-calendar',
										text:'Agenda',
										menu:[
										      
{
	id:'btn-addminutes-agenda',
	iconCls:'icn-report--plus',
	text:'<strong>Add last meeting\'s minutes</strong>',
	hidden:!(claraInstance.HasAnyPermissions(['ROLE_IRB_OFFICE','ROLE_SYSTEM_ADMIN'])),
	disabled:true,
	handler:function(){
		if (Clara.Agenda.SelectedAgenda.agendaStatus != null && Clara.Agenda.SelectedAgenda.agendaStatus != 'MEETING_ADJOURNED') Clara.Agenda.AddMinutesToSelectedAgenda();
	}
},{
	id:'btn-send-agenda',
	iconCls:'icn-mail-send',
	text:'<strong>Send Agenda..</strong>',
	disabled:true,
	handler:function(){
		if (Clara.Agenda.SelectedAgenda.agendaStatus != null && Clara.Agenda.SelectedAgenda.agendaStatus != 'MEETING_ADJOURNED') Clara.Agenda.SendSelectedAgenda();
	}
},'-',
										      
										      
										      {
											id:'btn-sort-agenda',
											iconCls:'icn-sort-quantity',
											hidden:!(claraInstance.HasAnyPermissions(['ROLE_IRB_OFFICE','ROLE_SYSTEM_ADMIN'])),
											text:'Reset Item Order..',
											handler:function(){
												if (Clara.Agenda.SelectedAgenda.agendaStatus != null && Clara.Agenda.SelectedAgenda.agendaStatus != 'MEETING_ADJOURNED') Clara.Agenda.ResetSelectedAgendaOrder();
											}
										},{
							
											id:'btn-manage-agenda-roster',
											iconCls:'icn-users',
											text:'Manage Roster..',
											disabled:true,
											hidden:!(claraInstance.HasAnyPermissions(['ROLE_IRB_OFFICE','ROLE_SYSTEM_ADMIN'])),
											handler:function(){
												if (Clara.Agenda.SelectedAgenda.agendaStatus != null && Clara.Agenda.SelectedAgenda.agendaStatus != 'MEETING_ADJOURNED') new Clara.Agenda.ManageAgendaRosterWindow().show();
											}
										},{
							
											id:'btn-cancel-agenda',
											iconCls:'icn-calendar--minus',
											text:'Cancel..',
											disabled:true,
											handler:function(){
												Clara.Agenda.CancelSelectedAgenda();
											}
										},{
							
											id:'btn-remove-agenda',
											iconCls:'icn-minus-button',
											text:'Remove..',
											disabled:true,
											handler:function(){
												Clara.Agenda.RemoveSelectedAgenda();
											}
										}]
									},{
										xtype:'button',
										id:'btn-agendaitem-menu',
										hidden:!(claraInstance.HasAnyPermissions(['ROLE_IRB_OFFICE','ROLE_SYSTEM_ADMIN','ROLE_IRB_OFFICE_CHAIR'])),
										iconCls:'icn-calendar-day',
										disabled:true,
										text:'Agenda Item',
										menu:[{
												id:'btn-assign-reviewers',
												hidden:!(claraInstance.HasAnyPermissions(['ROLE_IRB_OFFICE','ROLE_SYSTEM_ADMIN','ROLE_IRB_OFFICE_CHAIR'])),
												iconCls:'icn-user--plus',
												text:'Assign reviewers',
												disabled:true,
												handler:function(){
													if (Clara.Agenda.SelectedAgenda.agendaStatus != null && Clara.Agenda.SelectedAgenda.agendaStatus != 'MEETING_ADJOURNED') new Clara.Agenda.ReviewAssignmentWindow().show();
												}
											},{
												id:'btn-remove-item',
												iconCls:'icn-calendar--minus',
												hidden:!(claraInstance.HasAnyPermissions(['ROLE_IRB_OFFICE','ROLE_SYSTEM_ADMIN'])),
												text:'Remove item from agenda..',
												disabled:true,
												handler:function(){
													var disabledReportableTypes = ["continuing-review","new-submission", "modification", "reportable-new-information"];
													if (!(disabledReportableTypes.hasValue(Clara.Agenda.SelectedAgendaItem.protocolFormTypeId) && Clara.Agenda.SelectedAgendaItem.catagory == "REPORTED") && Clara.Agenda.SelectedAgenda.agendaStatus != null && Clara.Agenda.SelectedAgenda.agendaStatus != 'MEETING_ADJOURNED') {
														Ext.Msg.show({
															   title:'Remove item from agenda',
															   msg: 'Removing this item will move it back to the IRB office queue (any existing comments or contingencies for this item will be saved). Are you sure you want to do this?',
															   buttons: Ext.Msg.YESNO,
															   fn: function(btn){
																	if (btn == 'yes'){
																		Clara.Agenda.RemoveSelectedAgendaItem();
																	}
															   },
															   animEl: 'elId',
															   icon: Ext.MessageBox.WARNING
														});
													} else {
														alert("You cannot remove this item.");
													}
												}
											}
											]
									},{
										xtype:'button',
										id:'btn-approve-agenda',
										iconCls:'icn-thumb-up',
										hidden:true,
										text:'<span style="font-weight:800;">Approve Agenda...</span>',
										handler:function(){
											if (Clara.Agenda.SelectedAgenda.agendaStatus != null && Clara.Agenda.SelectedAgenda.agendaStatus != 'MEETING_ADJOURNED') Clara.Agenda.ApproveSelectedAgenda();
										}
									},'-',{
							    		xtype:'button',
							    		tooltip:'Print list (opens new window)',
							    		tooltipType:'title',
							    		iconCls:'icn-printer',
										handler: function(){
											var gp = Ext.getCmp('clara-protocol-agenda-panel');
											Ext.ux.Printer.print(gp,{ keepWindowOpen:true, title:jQuery("#agenda-item-details").text() });
										}
							    	}],
				    		 
				    	   },
				    	   items:[{xtype:'claraagendapanel',
				    		   fbar:{
				    			   height:24,
				    			   buttonAlign:'center',
				    			   items:[{id:'agenda-item-count',xtype:'tbtext',text:''}]
				    		   }}]}
				     ,{xtype:'claraagendadatelistpanel',region:'west',border:false,width:220,split:true}
				       
				       ]
		};
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		Clara.Agenda.AgendaPagePanel.superclass.initComponent.apply(this, arguments);
	}
});
Ext.reg('claraagendapagepanel', Clara.Agenda.AgendaPagePanel);

Clara.Agenda.AddMinutesToSelectedAgenda = function(){
	// TODO: Add check for existing minutes, etc.
	jQuery.ajax({
		  type: 'POST',
		  async:false,
		  url: appContext + "/ajax/agendas/"+Clara.Agenda.SelectedAgenda.id+"/add-minutes",
		  data: {
			userId:claraInstance.user.id  
		  },
		  success: function(){
			  Clara.Agenda.MessageBus.fireEvent('itemadded', this);  
		  },
		  error: function(){
			  Clara.Agenda.MessageBus.fireEvent('error', this);  
		  }
	});
};

Clara.Agenda.ReorderAgendaItems = function(store){
	var agendaItemIds = [];
	var index = 0;
	store.each(function(r){
		agendaItemIds.push(r.data.id);
	});
	
	clog(store);
	
	Ext.Ajax.request({
		method:'POST',
		url: appContext + "/ajax/agendas/"+Clara.Agenda.SelectedAgenda.id+"/agenda-items/set-order",
		params: {agendaItemIds:agendaItemIds},
		success: function(response){
			clog('reorderAgendaItems: Ext.Ajax success',response);
			Clara.Agenda.MessageBus.fireEvent('afteragendaitemordersaved', this);  
		},
		failure: function(error) {
			Clara.Agenda.MessageBus.fireEvent('onagendaitemordersaveerror', this);  
		}
	});
	
};

Clara.Agenda.DateListPanel = Ext.extend(Ext.grid.GridPanel, {
	id: 'clara-protocol-agenda-datelist-panel',
	frame:false,
	border:false,
	height:350,
	protocolFormXmlData:{},
	selectedAgenda:{},
	constructor:function(config){		
		Clara.Agenda.DateListPanel.superclass.constructor.call(this, config);
		Clara.Agenda.MessageBus.on('afteragendasave', this.onAgendasChanged, this);
		Clara.Agenda.MessageBus.on('agendaremoved', this.onAgendasChanged, this);
		Clara.Agenda.MessageBus.on('agendainfoupdated', function(){ location.reload(); }, this);
	},
	trackMouseOver:false,
	onAgendasChanged: function(){
		//Ext.getCmp("btn-meeting-menu").hide();
		Ext.getCmp("btn-start-meeting").hide();
		Ext.getCmp("btn-show-minutes").hide();
		Ext.getCmp("btn-manage-agenda-roster").setDisabled(true);
		Ext.getCmp("btn-agendaitem-menu").setDisabled(true);
		//Ext.getCmp("btn-open-agenda").setDisabled(true);
		Ext.getCmp("btn-send-agenda").setDisabled(true);
		Ext.getCmp("btn-addminutes-agenda").setDisabled(true);
		Ext.getCmp("btn-cancel-agenda").setDisabled(true);
		Ext.getCmp("btn-remove-agenda").setDisabled(true);
		Ext.getCmp("btn-assign-reviewers").setDisabled(true);
		Ext.getCmp("btn-remove-item").setDisabled(true);
		jQuery("#agenda-item-details").addClass("disabled-row").html("No agenda selected.");
		this.store.removeAll();
		this.store.load();
	},
	bodyStyle:'border-right:1px solid #8DB2E3;border-bottom:1px solid #8DB2E3;',
	initComponent: function(){
		var config = {
				store: new Ext.data.JsonStore({
					scope:this,
					url: appContext + "/ajax/agendas/list",
					autoLoad:true,
					sortInfo: {
					    field: 'date',
					    direction: 'DESC'
					},
					root:'data',
					fields: [
					    {name:'id', mapping: 'id'},
					    {name:'agendaStatus', mapping: 'agendaStatus'},
					    {name:'agendaStatusDesc', mapping: 'agendaStatusDesc'},
					    {name:'date', mapping: 'date', type:'date', dateFormat:'Y-m-d'},
					    {name:'irbRoster', mapping: 'irbRoster'}
					]
				}),
tbar:{
	items:[
	{
		text:'New Agenda..',
		iconCls:'icn-plus-button',
		hidden:!(claraInstance.HasAnyPermissions(['ROLE_IRB_OFFICE','ROLE_SYSTEM_ADMIN'])),
		handler:function(){
			new Clara.Agenda.NewWindow().show();
		}
	}
	]
},
		        viewConfig: {
		    		forceFit:true,
		    		getRowClass: function(record, index){
		    			return 'agenda-row-'+((record.get('agendaStatus') == null)?'AGENDA_INCOMPLETE':record.get('agendaStatus'));
		    		}
		    	},
		    	columns: [
				            {
				            	header: 'Date', sortable: true, dataIndex: 'date', renderer:function(value, p, record){
				        			var r=record.data;
				        			return "<div class='agenda-list-row'><div class='agenda-list-row-date' style='float:left;'><h1>"+Ext.util.Format.date(r.date, 'm/d/Y')+"</h1><h2>"+Clara.Agenda.GetCommitteeText(r.irbRoster)+"</h2></div><div class='agenda-list-row-details' style='float:right;'><div class='agenda-list-row-status'>"+Clara.Agenda.GetStatusText(r.agendaStatusDesc)+"</div></div><div style='clear:both;'></div>";
				            	}
				            }
				        ],
						listeners: {
						    rowclick: function(grid, rowI, event)   {
								var record = grid.getStore().getAt(rowI);
								Clara.Agenda.MessageBus.fireEvent('agendaselected', record.data);  
						    }
						}
		};
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		Clara.Agenda.DateListPanel.superclass.initComponent.apply(this, arguments);
	}
});
Ext.reg('claraagendadatelistpanel', Clara.Agenda.DateListPanel);


Clara.Agenda.AgendaPanel = Ext.extend(Ext.grid.GridPanel, {
	id: 'clara-protocol-agenda-panel',
	plugins: [new Ext.ux.dd.GridDragDropRowOrder(
		    {
		        copy: false, // false by default
		        scrollable: true, // enable scrolling support (default is false)
		        ddGroup:'agenda-dd',
		        targetCfg: { 
				    notifyDrop:function(dd,e,data){
		    			var grid = Ext.getCmp('clara-protocol-agenda-panel');
		    			var ds = grid.store;
		    			var sm = grid.getSelectionModel();
		                var rows = sm.getSelections();
		                if(dd.getDragData(e)) {
		                    var cindex=dd.getDragData(e).rowIndex;
		                    if(typeof(cindex) != "undefined") {
		                        for(var i = 0; i <  rows.length; i++) {
		                        ds.remove(ds.getById(rows[i].id));
		                        }
		                        ds.insert(cindex,data.selections);
		                        sm.clearSelections();
		                     }
		                    grid.getView().refresh(false);
		                    Clara.Agenda.ReorderAgendaItems(grid.store);
		                 }
		    		}
		    	} // any properties to apply to the actual DropTarget
		    })],
	frame:false,
	border:false,
	trackMouseOver:false,
	enableDragDrop:true,
	ddGroup:'agenda-dd',
	ddText:'Reorder this agenda item.',
	height:350,
	protocolFormXmlData:{},
	headerHTML:"<div id='clara-protocol-db-header'></div>",
	protocolInfoEl:"",
	constructor:function(config){		
		Clara.Agenda.AgendaPanel.superclass.constructor.call(this, config);
		Clara.Agenda.MessageBus.on('itemremoved', this.onAgendaChanged, this);
		Clara.Agenda.MessageBus.on('itemadded', this.onAgendaChanged, this);
		Clara.Agenda.MessageBus.on('agendaselected', this.onAgendaSelected, this);
		Clara.Agenda.MessageBus.on('agendaitemselected', this.onAgendaItemSelected, this);
		Clara.Agenda.MessageBus.on('afterassignreviewer', this.onAssignReviewer, this);
	},
	onAgendaChanged: function(){
		this.store.removeAll();
		jQuery("#agenda-item-details").addClass("disabled-row").html("No agenda selected.");
		Ext.getCmp("agenda-item-count").setText("");
		this.store.proxy.setUrl(appContext + "/ajax/agendas/"+Clara.Agenda.SelectedAgenda.id+"/agenda-items/list");
		this.store.load();
	},
	onAssignReviewer: function(){
		this.onAgendaChanged();
	},
	onAgendaSelected: function(agenda){
		clog("HEARD AGENDA SELECT");

		jQuery("#agenda-item-details").removeClass("disabled-row").html(Ext.util.Format.date(agenda.date, 'm/d/Y')+": "+Clara.Agenda.GetCommitteeText(agenda.irbRoster)+" ("+Clara.Agenda.GetStatusText(agenda.agendaStatus)+")");
		Clara.Agenda.SelectedAgenda = agenda;
		this.store.removeAll();
		this.store.proxy.setUrl(appContext + "/ajax/agendas/"+agenda.id+"/agenda-items/list");
		this.store.load();
		Ext.getCmp("btn-manage-agenda-roster").setDisabled(false);
		Ext.getCmp("btn-agendaitem-menu").setDisabled(true);
		Ext.getCmp("btn-send-agenda").setDisabled(false);
		Ext.getCmp("btn-addminutes-agenda").setDisabled(false);
		Ext.getCmp("btn-cancel-agenda").setDisabled(false);
		Ext.getCmp("btn-remove-agenda").setDisabled(false);
		Ext.getCmp("btn-assign-reviewers").setDisabled(true);
		Ext.getCmp("btn-remove-item").setDisabled(true);

		
		var agendaItemEditReadOnlyStatuses = ["MEETING_IN_PROGRESS","CANCELLED","MEETING_ADJOURNED_PENDING_CHAIR_APPROVAL","MEETING_ADJOURNED","MEETING_ADJOURNED_PENDING_IRB_OFFICE_PROCESS","MEETING_CLOSED"];

		if (agendaItemEditReadOnlyStatuses.hasValue(Clara.Agenda.SelectedAgenda.agendaStatus)){
			Ext.getCmp("btn-manage-agenda-roster").hide();
			Ext.getCmp("btn-sort-agenda").hide();
			Ext.getCmp("btn-agendaitem-menu").hide();
			Ext.getCmp("btn-agenda-menu").hide();
			Ext.getCmp("btn-send-agenda").hide();
			Ext.getCmp("btn-addminutes-agenda").hide();
			Ext.getCmp("btn-cancel-agenda").hide();
			Ext.getCmp("btn-remove-agenda").hide();
			Ext.getCmp("btn-assign-reviewers").hide();
			Ext.getCmp("btn-remove-item").hide();
			Ext.getCmp("btn-approve-agenda").hide();
			Ext.getCmp("btn-show-minutes").show();
		} else {
			if (claraInstance.HasAnyPermissions(['EDIT_AGENDA'])){
				Ext.getCmp("btn-manage-agenda-roster").show();
				Ext.getCmp("btn-sort-agenda").show();
				Ext.getCmp("btn-agendaitem-menu").show();
				Ext.getCmp("btn-agenda-menu").show();
				Ext.getCmp("btn-send-agenda").show();
				Ext.getCmp("btn-addminutes-agenda").show();
				Ext.getCmp("btn-cancel-agenda").show();
				Ext.getCmp("btn-remove-agenda").show();
				Ext.getCmp("btn-assign-reviewers").show();
				Ext.getCmp("btn-remove-item").show();
				Ext.getCmp("btn-approve-agenda").show();
			}
			Ext.getCmp("btn-show-minutes").hide();
			
		}
		if (Clara.IsAgendaChair(agenda.irbRoster) && agenda.agendaStatus =='AGENDA_PENDING_CHAIR_APPROVAL') Ext.getCmp("btn-approve-agenda").show();
		else Ext.getCmp("btn-approve-agenda").hide();
		if (claraInstance.HasAnyPermissions(['ROLE_IRB_MEETING_OPERATOR','ROLE_IRB_CHAIR'])) {
			Ext.getCmp("btn-start-meeting").show();
		}
		else {
			Ext.getCmp("btn-start-meeting").hide();
		}
		
	},
	onAgendaItemSelected: function(agendaitem){
		clog("Clara.Agenda.AgendaPanel.onAgendaItemSelected",agendaitem);
		Ext.getCmp("btn-agendaitem-menu").setDisabled(false);
		Clara.Agenda.SelectedAgendaItem = agendaitem;
		if (agendaitem.category == 'FULL_BOARD') {
			Ext.getCmp("btn-assign-reviewers").setDisabled(false);
		} else {
			Ext.getCmp("btn-assign-reviewers").setDisabled(true);
		}
		Ext.getCmp("btn-remove-item").setDisabled(false);
	},
	
	initComponent: function(){
		var config = {
				loadMask:true,
				autoExpandColumn:'col-agenda-item-row-title',
				store: new Ext.data.Store({
		    		proxy: new Ext.data.HttpProxy({
		    			url: appContext, //changes dynamically
		    			method:"GET",
		    			headers:{'Accept':'application/xml;charset=UTF-8'}
		    		}),
					autoLoad:false,
					//groupField: 'category',
					listeners:{
						load: function(t,recs){
							Ext.getCmp("agenda-item-count").setText(Ext.util.Format.plural(recs.length,"agenda item"));
						}
					},
					reader: new Ext.data.XmlReader({
						record:'agenda-item',
						root: 'list',
						fields: [
							{name:'id', mapping:'@id'},
							{name:'category', mapping:'@category'},
							{name:'xmlTitle', mapping:'xml-data>item>title'},
							{name:'xmlUrl', mapping:'xml-data>item>url'},
							{name:'protocolFormType', mapping:'protocol-form>protocol-form-type'},
							{name:'protocolFormTypeId', mapping:'protocol-form>protocol-form-type>@id'},
							{name:'protocolFormId', mapping:'protocol-form>@id'},
							{name:'protocolId', mapping:'protocol-form>protocol-meta>protocol>@id'},
							{name:'protocolTitle', mapping:'protocol-form>protocol-meta>protocol>title'},
							{name:'studyNature',mapping:'protocol-form>details>study-nature'},
							{name:'details',mapping:'protocol-form>details',convert:function(v,node){ return new Ext.data.XmlReader({record: 'value',fields: [{name:'detailName', mapping:'@name'},{name:'detailValue',mapping:''}]}).readRecords(node).records; }},
							{name:'protocolFormStatus', mapping:'protocol-form>protocol-form-meta>status'},
							{name:'reviewers', mapping:'reviewers', convert:function(v,node){
								return new Ext.data.XmlReader({
									record: 'reviewer',
									fields: [{name:'name', mapping:'name'}]
								}).readRecords(node).records; 
							}}
							
						]
					})
				}),
				view: new Ext.grid.GridView({
		    		getRowClass: function(record, index){
		    			return (record.get('reviewers').length == 0 && record.get('category') == 'FULL_BOARD')?'agenda-item-row-noreviewers':'';
		    		},
		    		emptyText:'<h1>There are no agenda items assigned to this date.</h1>Items are assigned by the IRB office once they have been approved by the appropirate committees.'
		    	}),
		    	columns: [
new Ext.grid.RowNumberer({width:48}),
		    	          {header: 'Category', renderer: function(v){ return "<div class='agenda-list-row agenda-category'>"+Clara.Agenda.GetStatusText(v)+"</div>"; }, sortable: true, dataIndex: 'category',id:'col-agenda-item-row-category'},
					        
		    	        {header: 'Type', width:245, sortable: true, dataIndex: 'protocolFormType',renderer:function(v,p,r){
		    	        	var str = "<div class='agenda-list-row'><h1>"+v+"</h1><h2>"+(Clara.Protocols.NameMappings.studyNature[r.get("studyNature")] || "")+"</h2>";
				        	if (r.get("details").length > 0){
			        			str += "<dl class='protocol-form-row-details'>";
			        			var a = r.get("details");
			        			for (var i=0; i<a.length; i++) {
			        				str += "<dt>" + a[i].get("detailName") + "</dt>";
			        				str += "<dd>" + a[i].get("detailValue") + "</dd>";
			        			}
			        			str += "</dl>";
			        		}
				        	return str+"</div>";
		    	        }},
				        {header: 'IRB #', sortable: true, width:60, dataIndex: 'protocolId',renderer:function(v,p,r){return "<div class='agenda-list-row'>"+v+"</div>";}},
				        {header: 'Protocol Name', sortable: true, dataIndex: 'protocolTitle',id:'col-agenda-item-row-title',renderer:function(v,p,r){
				        	if (r.get("xmlTitle") && r.get("xmlTitle") != "") return "<div class='agenda-list-row'><h1>"+r.get("xmlTitle")+"</h2></div>";
				        	else return (r.data.protocolFormStatus != 'Assigned to an IRB Agenda')?("<div class='agenda-list-row'><h1>"+v+"</h1><h2>Status: "+r.data.protocolFormStatus+"</h2></div>"):("<div class='agenda-list-row'><h1>"+v+"</h1></div>");
				        	}
				        },
			            {header: 'Reviewers', hidden:!(claraInstance.HasAnyPermissions(['ROLE_IRB_REVIEWER','ROLE_IRB_OFFICE','ROLE_SYSTEM_ADMIN','ROLE_IRB_COMMITTEE_CHAIR'])),width:130, sortable: true, dataIndex: 'reviewers', renderer:function(value, p, record){
				        		var r = record.get("reviewers");
				        		clog(record);
				        		clog(value);
				        		var html = "<div class='agenda-list-row agenda-row-reviewers'>";
			        			if (r.length == 0){
			        				if (record.get('category') == 'FULL_BOARD') html += "<span style='font-weight:800;color:red;'>No reviewers assigned.</span>";
			        				html += "</div>";
			        			} else {
			        				for (var i=0;i<r.length;i++){
			        					if (i > 0) html+="<br/>";
			        					html += "<span class='agenda-row-reviewer'>"+r[i].data.name+"</span>";
			        				}
			        			}
		        				return html + "</div>";
			            	}
			            },{
			            	header:'',
			            	dataIndex:'id',
			            	renderer: function(v,p,r){
			            		if (claraInstance.HasAnyPermissions(['VIEW_AGENDA_ONLY','ROLE_SYSTEM_ADMIN','ROLE_IRB_COMMITTEE_CHAIR','VIEW_AGENDA_ITEM'])){
			            			url = appContext+"/agendas/"+Clara.Agenda.SelectedAgenda.id+"/agenda-items/"+v+"/view";
			            			if (r.get("xmlUrl") && r.get("xmlUrl") != "") url = appContext+r.get("xmlUrl");
			            			return "<div class='icn-arrow' style='background-repeat:no-repeat;padding-left:20px;font-size:12px;'><a href='"+url+"'>View</a></div>";
			            		}
			            	}
			            }
				        ],
				listeners: {
						    rowclick: function(grid, rowI, event)   {
								var record = grid.getStore().getAt(rowI);
								Clara.Agenda.MessageBus.fireEvent('agendaitemselected', record.data);  
						    }
						}
		};
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		Clara.Agenda.AgendaPanel.superclass.initComponent.apply(this, arguments);
	}
});
Ext.reg('claraagendapanel', Clara.Agenda.AgendaPanel);