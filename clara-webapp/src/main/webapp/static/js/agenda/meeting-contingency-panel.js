Ext.ns('Clara.IRBMeeting');

Clara.IRBMeeting.HistoryPanel = Ext.extend(Ext.grid.GridPanel, {
	height:350,
	layout:'fit',

	constructor:function(config){		
		Clara.IRBMeeting.HistoryPanel.superclass.constructor.call(this, config);
	},	
	
	initComponent: function(){
		var t=this;
		var objType = "edu.uams.clara.webapp.protocol.domain.irb.AgendaItem";
		var config = {
				view: new Ext.grid.GroupingView({
			        forceFit: true,
					headersDisabled: true,
					enableGroupingMenu:false,
			        groupTextTpl: '{[values.rs[0].data["formType"]]} (Form ID# {[values.rs[0].data["formId"]]})'
			    }),
				listeners:{
					activate:function(p){
						p.getStore().load();
					}
				},
	        	border:false,
	        	loadMask:true,
				store: new Ext.data.GroupingStore({
					autoLoad:true,
					groupField: '',
					remoteGroup: false,
					sortInfo:{
						field:'timestamp',
						direction:'DESC'
					},
						proxy: new Ext.data.HttpProxy({
							url:appContext+"/ajax/history/history.xml?id="+Clara.IRBMeeting.CurrentAgendaItemRecord.get("id")+"&type="+objType,
							method:'GET',
							headers:{'Accept':'application/xml;charset=UTF-8'}
						}),
						reader: new Ext.data.XmlReader({
							record:'log',
							root: 'logs',
						fields: [
						    {name:'desc', mapping:''},
							{name:'actor', mapping:'@actor'},
							{name:'formId', mapping:'@form-id'},
							{name:'formType', mapping:'@form-type'},
							{name:'formTypeDesc', mapping:'@form-type-desc'},
							{name:'eventType', mapping:'@event-type'},
							{name:'timestamp', mapping:'@timestamp', type:'timestamp'},//'m/d/Y g:i:s'}
							{name:'datetime', mapping:'@date-time'}
						]})
					}),
					
					colModel: new Ext.grid.ColumnModel({
				        defaults: {
				            width: 120,
				            sortable: true
				        },
				        columns: [{
					    	header:'Form ID',
					    	dataIndex: 'formId',
					    	hidden: true,
					    	menuDisabled:true
					    },
						            {
						                header: 'Date', width: 135,fixed:true, dataIndex: 'datetime',
						                xtype: 'datecolumn', format: 'm/d/Y h:ia'
						            },
						            {header: 'Note', dataIndex: 'desc',renderer:function(v,p,r){
						            	var h = "<div class='history-note'>";
						            	h += "<div class='history-note-body'>"+v+"</div>";
						            	h += "<div class='history-note-meta'>";
						            	h += "<div class='history-meta-formevent'><span class='formType'>"+r.get("formType")+"</span>: <span class='eventType'>"+r.get("eventType")+"</span> by <span class='actor'>"+r.get("actor")+"</span></div>";
						            	return h + "</div></div>";
						            }}
				        ]
				    }),
				    viewConfig: {
				        forceFit: true
		        	}
		        
		};
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		Clara.IRBMeeting.HistoryPanel.superclass.initComponent.apply(this, arguments);
		
		t.getStore().filter("formId",Clara.IRBMeeting.CurrentAgendaItemRecord.get("id"));
	}
});
Ext.reg('clarairbmeetinghistorypanel', Clara.IRBMeeting.HistoryPanel);

Clara.IRBMeeting.AgendaItemLog = Ext.extend(Ext.Window, {
	id: 'clara-meeting-agendaitemlogwindow',
	title:'IRB comment history',
	layout:'fit',
	border:false,
	constructor:function(config){		
		Clara.IRBMeeting.AgendaItemLog.superclass.constructor.call(this, config);
	},
	initComponent: function(){
		var t = this;

		var config = {
				items:[{xtype:'clarairbmeetinghistorypanel'}],
				modal:true,
				width:720,
				height:450,
				buttons:[{
					text:'Close',
					handler:function(){
						t.close();
					}
				}]
		};
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		Clara.IRBMeeting.AgendaItemLog.superclass.initComponent.apply(this, arguments);
	}
});
Ext.reg('clarairbagendaitemlogwindow', Clara.IRBMeeting.AgendaItemLog);

Clara.IRBMeeting.EditorWindow = Ext.extend(Ext.Window, {
	id: 'clara-meeting-editorwindow',
	title:'Edit',
	layout:'border',
	border:false,
	plain:true,
	editing:false,
	editrecord:{},
	constructor:function(config){		
		Clara.IRBMeeting.EditorWindow.superclass.constructor.call(this, config);
	},
	initComponent: function(){
		var t = this;
		t.editing = (typeof t.editrecord.data !='undefined');
		var editing = t.editing;

		var config = {
				items:[{id:'clara-meeting-editowwindow-userarea',xtype:'form',region:'north',bodyStyle:'background-color:transparent;',height:30,padding:6,labelWidth:150,border:false,items:[{
					xtype:'combo',
					fieldLabel: 'Who made this comment',
    	    	   	typeAhead:false,
		        	forceSelection:true,
		        	displayField:'username', 
		        	valueField:'userid',
		        	mode:'remote', 
		        	triggerAction:'all',
		        	editable:false,
		        	// hidden:t.editing,
		        	width:520,
		        	allowBlank:false,
		        	id:'fldReviewer',
		        	name:'fldReviewer',
		        	tpl: '<tpl for=".">'
		                + '<div class="x-combo-list-item"><div><b>{fname} {lname}</b></div></div>'
		                + '</tpl>',
		               listeners:{
		            	 afterrender:function(cb){
		            		 if (t.editing) {
		            			 clog("setting user to editrecord",t.editrecord);
		            			 cb.setValue(t.editrecord.data.userId);
		            			 cb.setRawValue(t.editrecord.data.userFullname);
		            		 }
		            		 
		            	 }  
		               },
					store:new Ext.data.JsonStore({
						xtype:'jsonstore',
			    		//autoLoad:false,
			    		proxy : new Ext.data.HttpProxy({
		                     method: 'GET',
		                     url: appContext + "/ajax/agendas/"+Clara.IRBMeeting.AgendaId+"/irb-reviewers/list"
		                }),
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
					})
				}]},
				       {id:'clara-meeting-editorwindow-textarea',xtype:'textarea', region:'center', style:'font-size:24px;line-height:28px;',border:false,value:(editing)?t.editrecord.data.text:''},
				       {id:'clara-meeting-editorwindow-formarea',xtype:'form', region:'south', labelWidth:70, height:128,bodyStyle:'background-color:transparent;',border:false,layout:'column',labelAlign:'top',
							items:[{
						        // Fieldset in Column 1
						        xtype:'panel',
						        columnWidth: 0.3,
						        title: 'Comment type',
						        frame:true,
						        collapsible: false,
						        items :[{
						            xtype: 'radiogroup',
						            id:'fldType',
						            columns:1,
						            columnWidth:.5,
						            hideLabel:true,
						            cls: 'x-check-group-alt',
						            items: [
										{boxLabel: '<span style="font-weight:800;">Major Contingency</span>', name: 'rb-type', inputValue:'CONTINGENCY_MAJOR', checked: (editing && this.editrecord.data && this.editrecord.data.commentType == 'CONTINGENCY_MAJOR')},
										{boxLabel: 'Minor Contingency', name: 'rb-type', inputValue:'CONTINGENCY_MINOR', checked: (editing && this.editrecord.data && this.editrecord.data.commentType == 'CONTINGENCY_MINOR')},
										{boxLabel: 'Comment', name: 'rb-type', inputValue:'NOTE_MINOR', checked: (editing && this.editrecord.data && this.editrecord.data.commentType == 'NOTE_MINOR')},
										{boxLabel: 'IRB Studywide Comment', name: 'rb-type', inputValue:'STUDYWIDE', checked: (editing && this.editrecord.data && this.editrecord.data.commentType == 'STUDYWIDE'),
													listeners:{check:function(t,c){
														Ext.getCmp("fldAddToLetter").setValue(false);
													}}}
						            ]
						        }
						        ]
						    },{

						        // Fieldset in Column 2
						        xtype:'panel',
						        columnWidth: 0.2,
						        title: 'Mark as',
						        frame:true,
						        collapsible: false,
						        items :[
										{
										    xtype: 'radiogroup',
										    id:'fldStatus',
										    columns:1,
										    fieldLabel: 'Status',
										    labelAlign:'left',
										    cls: 'x-check-group-alt',
										    items: [
										{boxLabel: 'Met', name: 'rb-status', inputValue:'MET', checked: (editing && this.editrecord.data && this.editrecord.data.commentStatus == 'MET')},
										{boxLabel: 'Not met', name: 'rb-status', inputValue:'NOT_MET', checked: (editing && this.editrecord.data.commentStatus == 'NOT_MET')}
										    ]
										}
						        ]
						    }
						    ,{

						        // Fieldset in Column 3
						        xtype:'panel',
						        columnWidth: 0.5,
						        title: 'Letter',
						        frame:true,
						        collapsible: false,
						        items :[
										{

										    xtype: 'checkboxgroup',
										    id:'cbgOptions',
										    columns:1,
										    labelAlign:'left',
										    cls: 'x-check-group-alt',
										    items: [
												{
												    xtype: 'checkbox',
												    id:'fldAddToLetter',
												    labelAlign:'left',
												    boxLabel: 'Add to letter',
												    checked: (editing && this.editrecord.data && this.editrecord.data.inLetter == true)
												    
												},
												{
												    xtype: 'checkbox',
												    id:'fldChangeCommittee',
												    labelAlign:'left',
												    boxLabel: 'Set committee to "IRB Reviewer"',
												    checked: (!editing || (editing && this.editrecord.data && this.editrecord.data.committee == "IRB_REVIEWER")),
												    hidden: !editing
												}
										    ]
										
										}
						        ]
						    }
						    
						   
						    
						    
						    
							]
				       }],
				modal:true,
				width:720,
				height:450,
				buttons:[{
					text:'Save',
					handler:function(){
						if (Ext.getCmp("fldType").getValue() == null) alert("Please choose a comment type."); else {
						var tp = Ext.getCmp("fldType").getValue().inputValue;
						var inLetter = Ext.getCmp("fldAddToLetter").getValue();
						var committee = (Ext.getCmp("fldChangeCommittee").getValue())?"IRB_REVIEWER":null;
						clog("CHECK COMMITTEE",Ext.getCmp("fldChangeCommittee").getValue(),committee);
						var st = (Ext.getCmp("fldStatus").getValue() != null)?Ext.getCmp("fldStatus").getValue().inputValue:"NOT_MET";
						
						var data = {
							text:jQuery("#clara-meeting-editorwindow-textarea").val(),
							commentStatus:st,
							commentType:tp,
							inLetter:inLetter,
							isPrivate:true,
							userId:(editing)?t.editrecord.data.userId:claraInstance.user.id,
							version:(meeting.status == "IN_PROGRESS" || meeting.status == "NEW")?false:true,
							agendaItemId:Clara.IRBMeeting.CurrentAgendaItemRecord.get("id")
						};
						
						if (committee != null) data.committee = committee;

						var url = appContext + "/ajax/protocols/"+Clara.IRBMeeting.CurrentAgendaItemRecord.data.protocolId+"/protocol-forms/"+Clara.IRBMeeting.CurrentAgendaItemRecord.data.protocolFormId+"/review/committee-comments/";
						if (editing) url += t.editrecord.data.id+"/update";
						else url += "save";
						
				
							if (Ext.getCmp("fldReviewer").getValue() != "") data.userId = Ext.getCmp("fldReviewer").getValue();
						// }
						
						jQuery.ajax({
							url: url,
							type: "POST",
							async: false,
							data: data,    								
							success: function(data){
								if(Clara.IRBMeeting.MessageBus){
									Clara.IRBMeeting.MessageBus.fireEvent('contingenciesupdated', this);
								}
								t.close();
							},
							error: function(){
								alert("Error saving. No changes were saved to this item.");
							}
						});
						
					}
					}
				}]
		};
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		Clara.IRBMeeting.EditorWindow.superclass.initComponent.apply(this, arguments);
	}
});
Ext.reg('clarairbeditorwindow', Clara.IRBMeeting.EditorWindow);

Clara.IRBMeeting.ContingencyGridPanel = Ext.extend(Ext.grid.GridPanel, {
	id: 'contingencyGridPanel',
    autoScroll: true,
    border: false,
    currentAgendaItem: {},
    readOnly:false,
    ddGroup:'reviewnote-dd',
	ddText:'Reorder this item.',
    bodyCssClass:'gridpanel-contingencies',
	stripeRows: true,
	constructor:function(config){		
		Clara.IRBMeeting.ContingencyGridPanel.superclass.constructor.call(this, config);
		if(Clara.IRBMeeting.MessageBus){
			Clara.IRBMeeting.MessageBus.on('agendaitemchosen', this.onAgendaItemChosen, this);
			Clara.IRBMeeting.MessageBus.on('contingencychosen', this.onContingencyChosen, this);
			Clara.IRBMeeting.MessageBus.on('contingenciesupdated', this.onContigenciesUpdated, this);
		}
	},
	onContingencyChosen: function(){
		if ((isChair && meeting.status == "SENT_TO_CHAIR") || (canEditMeeting  && !(isIrbOffice && meeting.status == "SENT_TO_CHAIR") && !(isIrbOffice && meeting.status == "SENT_TO_TRANSCRIBER"))) Ext.getCmp("btnRemoveComment").setDisabled(false);
	},
	onAgendaItemChosen: function(){
		var t = this;
		var ai = Clara.IRBMeeting.CurrentAgendaItemRecord;
	
		if (ai.get("category") !== "MINUTES"){
			
			t.currentAgendaItem = {
				committee:'IRB_REVIEWER',
				userId:claraInstance.user.id,
				protocolId:ai.data.protocolId,
				protocolFormId:ai.data.protocolFormId
			};
			t.store.removeAll();
			t.store.proxy.setUrl(appContext + "/ajax/protocols/" + t.currentAgendaItem.protocolId + "/protocol-forms/" + t.currentAgendaItem.protocolFormId + "/review/committee-comments/list");
			t.store.reload({params:{
				userId:this.currentAgendaItem.userId,
				committee:this.currentAgendaItem.committee
			}});
		}
		
		if (ai.get("category") == "REPORTED"){
			t.store.removeAll();
			Ext.getCmp("btnAddComment").setDisabled(true);
			Ext.getCmp("btnRemoveComment").setDisabled(true);
			Ext.getCmp("btnShowEditLog").setDisabled(true);
		} else {

			if (canEditMeeting  && !(isIrbOffice && meeting.status == "SENT_TO_CHAIR") && !(isIrbOffice && meeting.status == "SENT_TO_TRANSCRIBER")) Ext.getCmp("btnAddComment").setDisabled(false);
			
			Ext.getCmp("btnRemoveComment").setDisabled(true);
			Ext.getCmp("btnShowEditLog").setDisabled(false);
			
			if (isChair && meeting.status == "SENT_TO_CHAIR") {
				 Ext.getCmp("btnAddComment").setDisabled(false);
			}
			if (ai.get("category") === "MINUTES"){
				Ext.getCmp("btnAddComment").setDisabled(true);
			}
		}
		
		
	},

	initComponent: function() {
		var t = this;
		var config = {
				plugins: [new Ext.ux.dd.GridDragDropRowOrder(
					    {
					    	dragDropEnabled:(claraInstance.HasAnyPermissions(['CAN_REORDER_COMMENTS']) && meeting.status != "SENT_TO_TRANSCRIBER"),
					        copy: false, // false by default
					        scrollable: true, // enable scrolling support (default is false)
					        ddGroup:'reviewnote-dd',
					        targetCfg: { 
							    notifyDrop:function(dd,e,data){
					    			var grid = t;
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
					                    // SORT, THEN UPDATE EPOCH HERE.
										var protocolFormCommitteeCommentIds = [];
										ds.each(function(rec){
											protocolFormCommitteeCommentIds.push(rec.get("id")); 
										});
										
										// AJAX CALL HERE
									
										var protocolId;
										var protocolFormId;
										
										Ext.Ajax.request({
											method : 'POST',
											url : appContext + "/ajax/protocols/"+t.currentAgendaItem.protocolId+"/protocol-forms/"+t.currentAgendaItem.protocolFormId +"/review/committee-comments/set-order",
											params : {
												protocolFormCommitteeCommentIds : protocolFormCommitteeCommentIds
											},
											success : function(response) {
												clog('reorder Review notes: Ext.Ajax success',
														response);
											},
											failure : function(error) {
												cwarn('reorder Review notes: Ext.Ajax failure',
														error);
											}
										});
										
										 sm.selectRecords(rows);  
					                 }
					    		}
					    	} // any properties to apply to the actual DropTarget
					    })],
			    view: new Ext.grid.GroupingView({
			    	forceFit: true,
					rowOverCls:'',
					headersDisabled: true,
					//hideGroupedColumn : true,
					showGroupName : false,
					enableGroupingMenu:false,
					groupTextTpl: '{text} ({[values.rs.length]} {[values.rs.length > 1 ? "Items" : "Item"]})',
			    }),
				store: new Ext.data.GroupingStore({
					groupField: '',
					proxy: new Ext.data.HttpProxy({
						url: appContext + "/ajax/protocols/filler/null/whatever",
						method:"GET",
						headers:{'Accept':'application/json;charset=UTF-8'}
					}),
					multiSortInfo:{ 
	                       sorters: [{field: 'displayOrder', direction: "ASC"}
	                                    ,{field: 'id', direction: "DESC"}], 
	                       direction: 'ASC'},

					baseParams: {
						userId:this.currentAgendaItem.userId,
						committee:this.currentAgendaItem.committee
					},
					listeners:{
						load:function(s){
							s.filter("committee","IRB");
						}
					},
					autoLoad:false,
					reader: new Ext.data.JsonReader({
						idProperty: 'id',
						fields: [
						         {name:'id', mapping:'id'},
						         {name:'displayOrder', mapping:'displayOrder'},
						         {name:'committee', mapping:'committee'},
						         {name:'committeeDescription'},
						         {name:'modified', mapping:'modifiedDate', type: 'date', dateFormat: 'm/d/Y H:i:s'},
						         {name:'userFullname', mapping:'userFullname'},
						         {name:'userId',mapping:'userId'},
						         {name:'text', mapping:'text'},
						         {name:'commentType', mapping:'commentType'},
						         {name:'commentStatus', mapping:'commentStatus', convert:function(v){
						        	 return (v)?v:"No status";
						         }},
						         {name:'contingencyType', mapping:'contingencyType'},
						         {name:'inLetter', mapping:'inLetter'},
						         {name:'replies',mapping:'children',convert:function(v,node){ 
						        	 var replyReader = new Ext.data.JsonReader({
						        			root: 'children',
						        			fields: [
                                                        {name:'id', mapping:'id'},
                                                        {name:'committee', mapping:'committee', type:'string'},
                                                        {name:'committeeDescription'},
                                                        {name:'timestamp', mapping:'@timestamp', type:'timestamp'},//'m/d/Y g:i:s'}
                                                        {name:'modified', mapping:'modifiedDate', type: 'date', dateFormat: 'm/d/Y H:i:s'},
                                                        {name:'userId', mapping:'userId'},
                                                        {name:'userFullname', mapping:'userFullname', type:'string'},
                                                        {name:'text', mapping:'text', type:'string'}
						        			         ]
						        		});
						        	 return replyReader.readRecords(node).records; 
						         }}
						]
					})
				}),

				listeners: {
					rowdblclick: function(grid, rowI, event)   {
							var record = grid.getStore().getAt(rowI);
                            Clara.IRBMeeting.CurrentCommentRecord = record;
                            Clara.IRBMeeting.MessageBus.fireEvent("contingencychosen",record);
							if ((isChair && meeting.status == "SENT_TO_CHAIR") || (canEditMeeting && !(isIrbOffice && meeting.status == "SENT_TO_TRANSCRIBER"))) new Clara.IRBMeeting.EditorWindow({editrecord:record}).show();
				    },
					rowclick: function(grid, rowI, event)   {
						var record = grid.getStore().getAt(rowI);
						clog(record);
			
						Clara.IRBMeeting.CurrentCommentRecord = record;
						Clara.IRBMeeting.MessageBus.fireEvent("contingencychosen",record);
				    }	
				},
				
				tbar:new Ext.Toolbar({
		    		scope:this,
	    	    	id:'comment-statusbar',
	    	    	items:[{
	    	        	xtype:'button',
	    	        	text:'Add..',
	    	        	id:'btnAddComment',
	    	        	iconCls:'icn-plus-button',
	    	        	disabled:true,
	    	        	handler:function(){
	    	    			new Clara.IRBMeeting.EditorWindow({editing:false}).show();
	    	    		}
	    	        },{
	    	        	xtype:'button',
	    	        	text:'Remove..',
	    	        	id:'btnRemoveComment',
	    	        	iconCls:'icn-minus-button',
	    	        	disabled:true,
	    	        	handler:function(){
	    	        		var url = appContext+"/ajax/protocols/"+Clara.IRBMeeting.CurrentAgendaItemRecord.data.protocolId+"/protocol-forms/"+Clara.IRBMeeting.CurrentAgendaItemRecord.data.protocolFormId+"/review/committee-comments/"+Clara.IRBMeeting.CurrentCommentRecord.data.id+"/remove";
		    	        	Ext.Msg.show({
		    	    			title:'Remove',
		    	    			width:350,
		    	    			msg:'<h1>Are you sure you want to remove the selected item?</h1>',
		    	    			buttons:Ext.Msg.YESNOCANCEL,
		    	    			fn:function(btn,reason){
		    	    				if (btn == 'yes'){
		    	    					jQuery.ajax({
		    	    						  type: 'GET',
		    	    						  async:false,
		    	    						  url: url,
		    	    						  data: {
		    	    							  userId: claraInstance.user.id,
		    	    							  version:(meeting.status == "IN_PROGRESS" || meeting.status == "NEW")?false:true,
		    	    							  agendaItemId:Clara.IRBMeeting.CurrentAgendaItemRecord.get("id")
		    	    						  },
		    	    						  success: function(){
		    	    							  Clara.IRBMeeting.MessageBus.fireEvent('contingenciesupdated', this);  
		    	    						  },
		    	    						  error: function(){
		    	    							  Clara.IRBMeeting.MessageBus.fireEvent('error', this); 
		    	    						  }
		    	    					});
		    	    				}
		    	    			},
		    	    		    icon: Ext.MessageBox.QUESTION
		    	    		});
	    	    		}
	    	        },'->',{xtype:'tbtext',text:'Filter:'},{
                        xtype:'textfield',
                        labelWidth:40,
                        width:140,
                        labelAlign:'right',
                        id:'fldCommentTextFilterField',
                        enableKeyEvents:true,
                        listeners:{
                            keyup:function(f){
                                var v = f.getValue();
                                if (jQuery.trim(v) == "") {
                                    clog("nothing to search by");
                                    jQuery(".gridpanel-contingencies .x-grid3-row").show();
                                }

                                else {
                                    clog("filtering by "+v);
                                    jQuery(".gridpanel-contingencies .x-grid3-row").each(function(){
                                        var row = this;
                                        if (jQuery(row).text().toLowerCase().indexOf(jQuery.trim(v).toLowerCase()) == -1) jQuery(row).hide();
                                        else jQuery(row).show();
                                    });
                                }
                            }
                        }
                    },'-',{
   		           	 	iconCls:'icn-balloons-white',
   		           	 	text: 'Group by Type',
   			           	enableToggle: true,
   			            pressed: false,
   			            toggleHandler: function(item, pressed){
   	    	        		if (pressed){
   	    	        			t.store.groupBy("commentType");
   	    	        			this.setIconClass('icn-balloons');
   	    	        		} else {
   	    	        			t.store.clearGrouping();
   	    	        			t.getColumnModel().setHidden(1, true);
   	    	        			this.setIconClass('icn-balloons-white');
   	    	        		}
   		 	    		}

   		           	 },{
	   		           	 	iconCls:'icn-users',
	   		           	 	text: 'Group by Status',
	   		           	 	//hidden:t.isMyList(),
	   			           	enableToggle: true,
	   			            pressed: false,
	   			            toggleHandler: function(item, pressed){
	   			            	clog("Groupby status",t.store);
	   			            	
	   	    	        		if (pressed){
	   	    	        			t.store.groupBy("commentStatus");
	   	    	        		} else {
	   	    	        			t.store.clearGrouping();
	   	    	        		}
	   		 	    		}	
	   		           	 	
	   		           	 },'-',{
   		           		 xtype:'button',
   		           		 text:'Show Edit History',
   		           		 id:'btnShowEditLog',
   		           		 iconCls:'icn-clock-history',
   		           		 disabled:true,
   		           		 handler:function(){
   		           			 new Clara.IRBMeeting.AgendaItemLog({}).show();
   		           		 }
   		           	 }]
	    	    }),
				
				columns: [new Ext.grid.RowNumberer({width:42}),{
				    	id: 'committee',
				    	header:'Committee',
				    	dataIndex: 'committee',
				    	hidden: true
				    },{
				    	id:'commentStatus',
				    	header:'Status',
				    	hidden:true,
				    	dataIndex:'commentStatus'
				    },{
						id:'commentType',
						dataIndex:'commentType',
						sortable:false,
						hidden:true,
				    	menuDisabled:true,
				    	renderer:function(v){
				    		var h = "";
							if (v == "CONTINGENCY_MAJOR") {
								h = "Major Contingency";
							} else if (v == "CONTINGENCY_MINOR") {
								h = "Minor Contingency";
							} else if (v == "NOTE_MAJOR") {
								h = "Major Comment";
							} else if (v == "NOTE_MINOR") {
								h = "Minor Comment";
							} else if (v == "STUDYWIDE") {
								h = "IRB Study Note";
							}
							return h;
				    	}
				    },{
						dataIndex:'commentType',
						sortable:false,
						header:'',
						width:110,
						renderer: function(v,p,r) { return Clara.Reviewer.CommentRenderer(v,p,r,{ gpid: t.id, meetingView:true}); },
				    	menuDisabled:true
				    }]

		};
		
		// apply config
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		  
		// call parent
		Clara.IRBMeeting.ContingencyGridPanel.superclass.initComponent.apply(this, arguments);
	},
	onRender:function(){
		Clara.IRBMeeting.ContingencyGridPanel.superclass.onRender.apply(this, arguments);
	},
	onContigenciesUpdated: function(source){
		var me = this;
		clog("CONTINGENCIES UPDATED..");
		Ext.getCmp("btnRemoveComment").setDisabled(true);
		
		me.store.removeAll();
		me.store.proxy.setUrl(appContext + "/ajax/protocols/" + this.currentAgendaItem.protocolId + "/protocol-forms/" + this.currentAgendaItem.protocolFormId + "/review/committee-comments/list");
		me.store.reload({params:{
			userId:this.currentAgendaItem.userId,
			committee:this.currentAgendaItem.committee
		}, callback: function(){
			if (Clara.IRBMeeting.CurrentCommentRecord && Clara.IRBMeeting.CurrentCommentRecord.get){
				// scroll to and select the last-edited record
				var st = me.getStore();
				var rowidx = st.findExact('id',Clara.IRBMeeting.CurrentCommentRecord.get("id"));
				if (rowidx > 0){
					me.getSelectionModel().unlock();
					me.getView().focusRow(rowidx);
					me.getSelectionModel().selectRow(rowidx);
					me.getSelectionModel().lock();
					
					// now scroll to it (doesnt work YET)
					me.getView().ensureVisible(rowidx,0,false);
				
				}
				
			}
			Clara.IRBMeeting.CurrentCommentRecord = null;
		}});
		
		
	}
});

//register xtype
Ext.reg('meeting-contingencygrid-panel', Clara.IRBMeeting.ContingencyGridPanel);