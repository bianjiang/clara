Ext.ns('Clara.ProtocolDashboard','Clara','Clara.Application');

var protocolDocumentPanel;
var protocolDashboardPanel;
var protocolTabPanel;
var protocolDetailPanel;
var protocolHeaderPanel;
var protocolNotificaitonPanel;
var protocolFormPanel;

// backport for using new function names with old protocol dashboard
Clara.Application.FormController = Clara.ProtocolDashboard;
Clara.Application.QueueController = Clara.ProtocolDashboard;


Clara.ProtocolDashboard.ChooseReviewRole = function(){
	new Ext.Window({
		model:true,
		title:'Who do you want to review this form as?',
		layout:'fit',
		height:200,
		width:300,
		iconCls:'icn-user-silhouette',
		buttons:[{
			id:'btnReviewFormAsCommittee',
			disabled:true,
			text:'Review Form',handler:function(){
			var g = Ext.getCmp("gpFormReviewCommittees");
			var rec = g.getSelectionModel().getSelected();
			//clog(rec);
			// http://localhost:8080/clara-webapp/protocols/201496/protocol-forms/1537/review?committee=IRB_OFFICE
			location.href = appContext+"/protocols/"+claraInstance.id+"/protocol-forms/"+Clara.ProtocolDashboard.SelectedFormRecord.get("protocolFormId")+"/review?committee="+rec.get("name");
			
		}}],
		items:[{
			xtype:'grid',
			hideHeaders:true,
			id:'gpFormReviewCommittees',
				store: new Ext.data.XmlStore({
					proxy: new Ext.data.HttpProxy({
						url:appContext+'/ajax/protocols/'+claraInstance.id+"/protocol-forms/"+Clara.ProtocolDashboard.SelectedFormRecord.get("protocolFormId")+"/get-user-role-list?userId="+claraInstance.user.id,
						method:"GET",
						headers:{'Accept':'application/xml;charset=UTF-8'}
					}),
                    fields:[{name:'name',mapping:'@name'},{name:'desc',mapping:'@desc'}],
                    autoLoad:true,
                    record:"committee",
                    sortInfo:{
					    	field:'desc',
					    	direction:'ASC'
					    }
                 }),
			colModel: new Ext.grid.ColumnModel({
				columns:[{id:'col-rf-name',dataIndex:'desc'}]
			}),
			listeners:{
				rowclick:function(g,rowIndex,e){
        			Ext.getCmp("btnReviewFormAsCommittee").setDisabled(false);
        		}
			},
			viewConfig:{
				forceFit:true
			},
			sm: new Ext.grid.RowSelectionModel({singleSelect:true})
		}]
	}).show();
};


assignedReviewerXmlReader = new Ext.data.XmlReader(
		{
			record : '/assigned-reviewers/assigned-reviewer',
			idProperty:'@user-role-id',
			fields : [
					{
						name : 'reviewerName',
						mapping : '@user-fullname'
					},{
						name : 'reviewerId',
						mapping : '@user-id'
					},{
						name : 'reviewerRoleId',
						mapping : '@user-role-id'
					},{
						name : 'reviewerRoleName',
						mapping : '@user-role'
					},{
						name : 'assigningCommittee',
						mapping : '@assigning-committee'
					},{
						name : 'userRoleCommittee',
						mapping : '@user-role-committee'
					}]
		});

Clara.ProtocolDashboard.SelectedFormRecord = {};
Clara.ProtocolDashboard.SelectedLetterRecord = {};

Clara.ProtocolDashboard.CloseStudy = function(protocolObj){
	var pid = protocolObj.protocolId;
	var callback = function(){ location.reload(true) };
	var msg = "Are you sure you want to administratively close this study?";
	
	new Ext.Window({
		title:'Why are you closing this study?',
		iconCls:'icn-blue-document-smiley-sad',
		width:500,
		height:200,
		padding:6,
		autoHeight:true,
		autoScroll:true,
		layout:'fit',
		items:[{
			xtype:'textarea',
			id:'fldReason',
			hideLabel:true,
			emptyText:'Please tell us why you are closing this study.'
		}],
		buttons:[{text:'Close study',handler:function(){
			
			Ext.Msg.confirm('Cancel this study?', msg, function(btn){
			    if (btn == 'yes'){
			        // process text value and close...
			    	var url = appContext+"/ajax/protocols/"+pid+"/close-study";
			    	jQuery.ajax({url: url,
			    		type: "POST",
			    		async: false,
			    		data: { 
			    			userId:claraInstance.user.id,
			    			reason:Ext.getCmp('fldReason').getValue()
			    		},
			    		success: function(msg){
			    			clog(msg);
			    			callback();
			    		}
			    	});	
			    }
			});
		}}]
	}).show();
	
	
};


Clara.ProtocolDashboard.CancelForm = function(){
	// removes selected protocol form (if allowed)
	var callback = function(){ location.href=appContext; };
	var msg = "Are you sure you want to cancel this form?";
	
	new Ext.Window({
		id:id,
		title:'Why are you cancelling this form?',
		iconCls:'icn-blue-document-smiley-sad',
		width:500,
		height:200,
		padding:6,
		autoHeight:true,
		autoScroll:true,
		layout:'fit',
		items:[{
			xtype:'textarea',
			id:'fldReason',
			hideLabel:true,
			emptyText:'Please tell us why you are cancelling this form.'
		}],
		buttons:[{text:'Continue',handler:function(){
			
			var xml ="<cancel-reason type='PROTOCOL'>"+Encoder.cdataWrap(Ext.getCmp('fldReason').getValue())+"</cancel-reason>";
			
			Ext.Msg.confirm('Cancel this study?', msg, function(btn){
			    if (btn == 'yes'){
			        // process text value and close...
			    	var url = appContext+"/ajax/protocols/"+claraInstance.id+"/protocol-forms/"+Clara.ProtocolDashboard.SelectedFormRecord.get("protocolFormId")+"/cancel";
			    	jQuery.ajax({url: url,
			    		type: "POST",
			    		async: false,
			    		data: {
			    			xml:xml
			    		},
			    		success: function(msg){
			    			clog(msg);
			    			callback();
			    		}
			    	});	
			    }
			});
		}}]
	}).show();
};

Clara.ProtocolDashboard.RemoveBudget = function(){
	// removes selected protocol form (if allowed)
	var callback = function(){ alert("Budget deleted.") };
	var msg = "Are you sure you want to remove the budget?";

	Ext.Msg.confirm('Delete this budget?', msg, function(btn){
	    if (btn == 'yes'){
	        // process text value and close...
	    	var url = appContext+"/ajax/protocols/"+claraInstance.id+"/protocol-forms/"+Clara.ProtocolDashboard.SelectedFormRecord.get("protocolFormId")+"/budgets/delete";
	    	jQuery.ajax({url: url,
	    		type: "GET",
	    		async: false,
	    		data: {},
	    		success: function(msg){
	    			clog(msg);
	    			callback();
	    		}
	    	});	
	    }
	});
};

Clara.ProtocolDashboard.RemoveForm = function(){
	// removes selected protocol form (if allowed)
	var callback = {};
	var msg = "Are you sure you want to remove this form?";
	
	if (Clara.ProtocolDashboard.SelectedFormRecord.get("formtype") == "New Submission"){
		callback = function(){ location.href=appContext; };
		msg += "<br/><span style='font-weight:800;'>WARNING: Since you're removing the New Submission form, this will remove the protocol too.</span>";
	} else {
		callback = function(){ location.reload(true); };
	}
	msg += "<br/><span style='font-weight:800;color:red;text-decoration:underline;'>If this form contains a budget, it will be deleted as well.</span>";
	
	Ext.Msg.confirm('Delete this form?', msg, function(btn){
	    if (btn == 'yes'){
	        // process text value and close...
	    	var url = appContext+"/ajax/protocols/"+claraInstance.id+"/protocol-forms/"+Clara.ProtocolDashboard.SelectedFormRecord.get("protocolFormId")+"/delete";
	    	jQuery.ajax({url: url,
	    		type: "GET",
	    		async: false,
	    		data: {},
	    		success: function(res){
	    			clog(res);
	    			callback();
	    		}
	    	});	
	    }
	});
};

Clara.ProtocolDashboard.LetterPanel = Ext.extend(Ext.grid.GridPanel, {
	id: 'clara-protocol-db-letterpanel',
	height:350,
	title:'Letters',
	iconCls:'icn-mail',
	layout:'fit',
	
	constructor:function(config){		
		Clara.ProtocolDashboard.LetterPanel.superclass.constructor.call(this, config);
	},	
	
	initComponent: function(){
		var t = this;
		var config = {
				tbar: {
					xtype:'toolbar',
					items:[
					       {iconCls:'icn-mail--plus',text:'New IRB Letter..',hidden:!claraInstance.HasAnyPermissions(['CAN_SEND_IRB_LETTER']),
					    	handler:function(){
								var templateUrl = appContext + '/ajax/'+claraInstance.type+'s/'+claraInstance.id+'/email-templates/IRB_LETTER';

								var messageWin = new Clara.Mail.MessageWindow({
									templateUrl:templateUrl,
									messageBody:'',
									delayedSend:false,
									metadata:claraInstance,
									sendFunction:Clara.Mail.SignAndSubmit,
									requireSignature:true,
									title:'Send IRB Letter',
									sendUrl:appContext+'/ajax/'+claraInstance.type+'s/'+claraInstance.id+'/email-templates/IRB_LETTER/send-letter',
									modal:true,
									iconCls:'icn-mail--pencil',
									onSuccess:function(){ Ext.getCmp("clara-protocol-db-letterpanel").getStore().load();}
								});
								messageWin.show();	

								
					    	}   
					       },
					       {iconCls:'icn-mail--pencil',text:'New Correction Letter..',disabled:true,id:'btnNewCorrectionLetter',hidden:!claraInstance.HasAnyPermissions(['CAN_SEND_IRB_LETTER']),
						    	handler:function(){
									var templateUrl = appContext + '/ajax/'+claraInstance.type+'s/'+claraInstance.id+'/email-templates/IRB_CORRECTION_LETTER';

									var messageWin = new Clara.Mail.MessageWindow({
										templateUrl:templateUrl,
										messageBody:'',
										parentMessageId:Clara.ProtocolDashboard.SelectedLetterRecord.get("parentid"),
										delayedSend:false,
										metadata:claraInstance,
										sendFunction:Clara.Mail.SignAndSubmit,
										requireSignature:true,
										title:'Send Correction Letter',
										sendUrl:appContext+'/ajax/'+claraInstance.type+'s/'+claraInstance.id+'/email-templates/IRB_CORRECTION_LETTER/send-letter',
										modal:true,
										iconCls:'icn-mail--pencil',
										onSuccess:function(){ Ext.getCmp("clara-protocol-db-letterpanel").getStore().load();}
									});
									messageWin.show();	

									
						    	}   
						       },{iconCls:'icn-mail--exclamation',text:'New Audit Report Letter..',id:'btnNewAuditReportLetter',hidden:!claraInstance.HasAnyPermissions(['CAN_SEND_IRB_LETTER']),
							    	handler:function(){
										var templateUrl = appContext + '/ajax/'+claraInstance.type+'s/'+claraInstance.id+'/email-templates/RECEIPT_OF_AUDIT_REPORT_LETTER';

										var messageWin = new Clara.Mail.MessageWindow({
											templateUrl:templateUrl,
											messageBody:'',
											delayedSend:false,
											metadata:claraInstance,
											sendFunction:Clara.Mail.SignAndSubmit,
											requireSignature:true,
											title:'Send Audit Report Letter',
											sendUrl:appContext+'/ajax/'+claraInstance.type+'s/'+claraInstance.id+'/email-templates/RECEIPT_OF_AUDIT_REPORT_LETTER/send-letter',
											modal:true,
											iconCls:'icn-mail--exclamation',
											onSuccess:function(){ Ext.getCmp("clara-protocol-db-letterpanel").getStore().load();}
										});
										messageWin.show();	

										
							    	}   
							       },'->',{
									enableToggle: true,
									iconCls:'icn-ui-check-box-uncheck',
									text:'Group by Letter',
									pressed: false,
							            toggleHandler: function(item, pressed){
							            	var b = this;
					    	        		if (pressed){
					    	        			t.store.groupBy("parentid");
					    	        			b.setIconClass('icn-ui-check-box');
					    	        		} else {
					    	        			t.store.clearGrouping();
					    	        			b.setIconClass('icn-ui-check-box-uncheck');
					    	        		}
						 	    		}	
								}
			           	]},
					view: new Ext.grid.GroupingView({
				        forceFit: true,
						headersDisabled: true,
						enableGroupingMenu:false,
				        groupTextTpl: '{[values.rs[0].data["letterType"]]}'
				    }),
		        	border:false,
		        	loadMask:true,
					store: new Ext.data.GroupingStore({
						autoLoad:false,
						groupField: '',
						remoteGroup: false,
						proxy: new Ext.data.HttpProxy({
							url:appContext+"/ajax/history/history.xml?id="+claraInstance.id+"&type=edu.uams.clara.webapp.protocol.domain.Protocol&filter=letter",
							method:'GET',
							headers:{'Accept':'application/xml;charset=UTF-8'}
						}),
						sortInfo: {
							field:'timestamp',
							direction: 'DESC'
						},
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
							{name:'letterType',mapping: '@email-template-identifier'},
							{name:'id',mapping:'@id'},
							{name:'parentid',mapping:'@parent-id'},
							{name:'timestamp', mapping:'@timestamp', type:'timestamp'},//'m/d/Y g:i:s'}
							{name:'datetime', mapping:'@date-time'}
						]})
					}),
					colModel: new Ext.grid.ColumnModel({
				        defaults: {
				            width: 120,
				            sortable: true
				        },
				        columns: [
				                    {header: 'Letter ID', dataIndex: 'parentid',hidden:true,menuDisabled:true},
						            {
						                header: 'Date', width: 135,fixed:true, dataIndex: 'datetime',sortable:true,
						                xtype: 'datecolumn', format: 'm/d/Y h:i:sa'
						            },
						            {header: 'Who?', width:150,fixed:true,dataIndex: 'actor'},
						            {header: 'Letter Type', width:150,fixed:true,dataIndex: 'letterType'},
						            {header: 'Message', dataIndex: 'desc', renderer: function(v){return "<div class='wrap'>"+v+"</div>";}}
				        ]
				    }),
				    listeners:{
						activate:function(p){
							p.getStore().load();
						},
				    	rowclick: function(grid, rowI, event)   {
							var r = grid.getStore().getAt(rowI);
							Clara.ProtocolDashboard.SelectedLetterRecord = r;
							clog(r.get("id"),r.get("parentid"),r.data);
							if (claraInstance.HasAnyPermissions(['ROLE_IRB_EXPEDITED_REVIEWER','ROLE_IRB_OFFICE','ROLE_IRB_PREREVIEW'])) Ext.getCmp("btnNewCorrectionLetter").setDisabled(false);
				    	}
				    }
		        
		};
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		Clara.ProtocolDashboard.LetterPanel.superclass.initComponent.apply(this, arguments);
	}
});
Ext.reg('claraprotocolletterpanel', Clara.ProtocolDashboard.LetterPanel);



Clara.ProtocolDashboard.FormPanel = Ext.extend(Ext.Panel, {
	id: 'clara-protocol-db-formpanel',
	height:350,
	title:'Forms',
	iconCls:'icn-application-form',
	layout:'border',
	selectedFormId:null,
	constructor:function(config){		
		Clara.ProtocolDashboard.FormPanel.superclass.constructor.call(this, config);
	},	
	
	initComponent: function(){
		var t =this;
		var config = {
				scope:this,
				
				items:[{
						xtype:'claraprotocolformgridpanel',
						style:'border-right:1px solid #96baea;', 
						region:'center',
						border:false,
						split:true,
						selectedFormId:t.selectedFormId,
						tbar: new Ext.Toolbar({
				    		items:[
				    		       {
				           	    	id: 'btnAddForm',         	    	
					           	 	iconCls:'icn-plus-button',
					           	 	hidden:(Ext.getCmp("clara-protocol-dashboardpanel").agenda.id)?true:false,
					           	 	text: 'New form..',
					           	 	handler:function(){
					           	 		var w = new Clara.NewFormListWindow();
					           	 		w.show();
					           	 	}
					           	 	
					           	 }
					           	]})
					},
				       {
       				xtype:'tabpanel',id:'tpFormDetails',style:'border-left:1px solid #96baea;',region:'east',width:500,border:false,split:true,activeItem:0,disabled:true,
       				items:[{title:'Actions',iconCls:'icn-hand-point',xtype:'panel',autoScroll:true,border:false,style:'border-bottom:1px solid #96baea;width:100%;background-color:#dee8f7;',split:true,padding:6,html:'<div id="protocolform-rowdetail"></div><div id="protocolform-norowselected">Choose a form to see more details.</div>'},
		                   {title:'Review Status',iconCls:'icn-dashboard',id:'contractformstatuspanel', xtype:'claraformstatusgridpanel',split:true,border:false },
          				    {title:'Notes by Committee',iconCls:'icn-sticky-notes',id:'contractformnotespanel',autoLoadComments:false,xtype:'reviewer-contingencygrid-panel',readOnly:true,border:false}]
		            
		       }
				       ]
		};
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		Clara.ProtocolDashboard.FormPanel.superclass.initComponent.apply(this, arguments);
		
		if (t.selectedFormId && t.selectedFormId > 0){
			clog("SELECTING FORM "+t.selectedFormId);
		}
		
	}
});
Ext.reg('claraprotocolformpanel', Clara.ProtocolDashboard.FormPanel);


Clara.ProtocolDashboard.FormGridPanel = Ext.extend(Ext.grid.GridPanel, {
	id: 'clara-protocol-db-formgridpanel',
	height:350,
	bodyStyle:'border-bottom: 1px solid #8DB2E3;',
	constructor:function(config){		
		Clara.ProtocolDashboard.FormGridPanel.superclass.constructor.call(this, config);
	},	
	scrollToRow : function(row) {
        this.getView().focusCell(row, 0, true);
    },
    selectedFormId:null,
	highlightFormById: function(id){
		
		var t = this;
		var st = t.getStore();
		cdebug("highlightFormById",id,st);
		var rowidx = st.findExact('protocolFormId',""+id);
		if (rowidx > -1){
			t.getSelectionModel().unlock();
			t.getView().focusRow(rowidx);
			t.getSelectionModel().selectRow(rowidx);
			// t.getSelectionModel().lock();
		
			
			// now scroll to it
			t.scrollToRow(rowidx);
		}
	},
	initComponent: function(){
		var t=this;
		var config = {
				store: new Ext.data.XmlStore({
					scope:this,
					listeners:{
                        load: function(st,recs,opts){
                            clog("LOAD RECS",recs);
                            if (claraInstance.id < 190000){
                                if (recs.length < 2) jQuery("#summary-bar-migrated-study").show(0,function(){
                                    Ext.getCmp("clara-protocol-dashboardpanel").doLayout();

                                });
                                else jQuery("#summary-bar-migrated-study").hide(0,function(){
                                    Ext.getCmp("clara-protocol-dashboardpanel").doLayout();
                                });
                            }
                        }
                    },
					proxy: new Ext.data.HttpProxy({
						scope:this,
						url: appContext + "/ajax/protocols/"+claraInstance.id+"/protocol-forms/list.xml", //
						method:"GET",
						headers:{'Accept':'application/xml;charset=UTF-8'}
					}),
					record: 'protocol-form', 
					autoLoad:false,
					root:'list',
					fields: [
					    {name:'protocolFormId', mapping: '@protocolFormId'},
						{name:'formtype', mapping:'protocol-form-type'},
						{name:'url',mapping:'url'},
						{name:'studynature', mapping:'details>study-nature'},
						{name:'editurl',mapping:'editurl'},
						{name:'locked',mapping:'status>lock@value'},
						{name:'lockUserId',mapping:'status>lock@userid'},
						{name:'lockModified',mapping:'status>lock@modified', type: 'date', dateFormat: 'Y-m-d H:i:s.u'},
						{name:'lockMessage',mapping:'status>lock@message'},
						{name:'status', mapping:'status>description'},
						{name:'agendaDate', mapping:'status>agenda>assigned-date', type: 'date', dateFormat: 'm/d/Y'},
						{name:'statusModified',mapping:'status>modified', type: 'date', dateFormat: 'Y-m-d H:i:s.u'},
						{name:'details',convert:function(v,node){ return new Ext.data.XmlReader({record: 'value',fields: [{name:'detailName', mapping:'@name'},{name:'detailValue',mapping:''}]}).readRecords(node).records; }},
						{
							name:'actions',
							convert:function(v,node){
								return new Ext.data.XmlReader({
									record: 'action',
									fields: [
									         {name:'type'},
									         {name:'class', mapping:'@cls'},
									         {name:'name'},
									         {name:'urlType',mapping:'url@type'},
									         {name:'response', mapping:'url@response'},//the type of response
									         {name:'target',mapping:'url@target'},
									         {name:'url'}
									         ]
								}).readRecords(node).records; 
							}
						},
						{
							name : 'assignedReviewers',
							mapping:'assigned-reviewers',
							convert : function(v, node) {
								var recs = assignedReviewerXmlReader.readRecords(node).records;
							    return recs;
							}
						}
						
					]
				}),
		        viewConfig: {
		    		forceFit:true,
		    		loadMask:true,
		    		getRowClass: function(record){
		    			return (t.selectedFormId)?(record.get('protocolFormId') == ''+t.selectedFormId ? 'selected-form-row' : ''):'';
		    		}
		    	},
		        columns: [

		            {header: 'Form', width: 270, sortable: true, dataIndex: 'formtype', renderer:function(value, p, record){
		            	var locked = record.get("locked");
		        		var url=record.data.url;
		        		var formType = record.data.formtype;
		        		var str = (locked === "true")?"<div class='form-row form-row-locked'>":"<div class='form-row'>";
		        		str += (record.get("studynature") != "")?("<span class='protocol-form-row-field protocol-form-type'>"+formType+"</span><div class='studynature'>"+Clara.Protocols.NameMappings.studyNature[record.get("studynature")]+"</div>"):("<span class='protocol-form-row-field protocol-form-type'>"+formType+"</span>");
		        		str += (locked === "true")?"<div style='font-weight:800;border:1px solid red; padding:4px;margin-top:2px;'>"+record.get("lockMessage")+"</div>":"";
		        		if (record.get("details").length > 0){
		        			str += "<dl class='protocol-form-row-details'>";
		        			var a = record.get("details");
		        			clog("actions:",a);
		        			for (var i=0; i<a.length; i++) {
		        				str += "<dt>" + a[i].get("detailName") + "</dt>";
		        				str += "<dd>" + a[i].get("detailValue") + "</dd>";
		        			}
		        			str += "</dl>";
		        		}
		        		return str+"</div>";
		            }},
		            {header: 'Last Modified', width: 95, sortable: true, renderer: function(value) { return "<span class='protocol-form-row-field'>"+Ext.util.Format.date(value,'m/d/Y')+"</span>";}, dataIndex: 'statusModified'},
		            {header: 'Status', id:'protocol-forms-status-column', width: 215, sortable: true, dataIndex: 'status', 
		            	renderer:function(v,p,record){
		            		var html = "<div class='wrap'><span class='protocol-form-row-field protocol-form-status'>"+v+"</span>";
		            		if (record.get("agendaDate")) {
		            			var dateClass = "label-info";
		            			if (moment(record.get("agendaDate")).add('days',1) < moment(new Date()).add('days',0)) dateClass = "label-disabled";
		            			html += "<div class='protocol-form-row-field label "+dateClass+"'>On IRB agenda: "+moment(record.get("agendaDate")).format('L');+"</div>";
		            		}
		            		return html+"</div>";
		            	}
		            },
					{
						header : 'Assigned To',
						dataIndex : 'assignedReviewers',
						sortable : true,
						width : 230,
						renderer : function(v) {
							var h = "<ul class='form-list-row form-assigned-reviewers'>";
								for(var i=0;i<v.length;i++) {
									clog(v[i]);
									h +="<li class='form-assigned-reviewer'>"+Clara.HumanReadableRoleName(v[i].data.reviewerRoleName)+": "+v[i].data.reviewerName + "</li>";
								}
								h += "</ul>";
									
							return h;
						}
					}
		        ],
				listeners: {
		    		beforerender: function(){
		    		clog("load store before rendering...");
		    			if (this.store.getTotalCount() < 1) this.store.load({
		    				callback: function(recs){
		    					clog("STORE LOAD CALLBK",recs,t.selectedFormId);
		    					if (t.selectedFormId != null){
		    						t.highlightFormById(t.selectedFormId);
		    					}
		    				}
		    			});
		    		},
				    rowclick: function(grid, rowI, event)   {
						var record = grid.getStore().getAt(rowI);
						Clara.ProtocolDashboard.SelectedFormRecord = record;
						
						
						
						selectedItemAssignedReviewers = record.get("assignedReviewers");

						var detailHtml = "<div class='protocolform-info'>";
						detailHtml += "<dl><dt class='protocolform-type'>Title</dt><dd class='protocolform-type'>"+record.get("formtype")+"</dd>";
						detailHtml += "<dl><dt>Status</dt><dd>"+record.get("status")+"</dd>";
						detailHtml += "<dt>Modified</dt><dd>"+record.get("statusModified")+"</dd></dl>";
						detailHtml += "<div class='protocolform-actions'>";
							
						
						
						for (var i=0; i<record.data.actions.length; i++) {
							var style =record.data.actions[i].get("class");

								if(record.data.actions[i].data.urlType == 'javascript'){
									detailHtml += '<button class="form-action-button button '+style+'" onClick="' + record.data.actions[i].data.url + '">' + record.data.actions[i].data.name  + '</button>';
								}else if(record.data.actions[i].data.urlType == 'ajax'){
									detailHtml += '<button class="form-action-button button '+style+'" onClick="javascript:ajax_link_clicked(\'' + appContext + record.data.actions[i].data.url + '\', \'' + (record.data.actions[i].data.response) + '\');">' + record.data.actions[i].data.name  + '</button>';
								}else if(record.data.actions[i].data.urlType == 'label'){
									detailHtml += '<span class="'+style+'">' + record.data.actions[i].data.name  + '</span><br/>';
									
								}else{
									var code = (record.data.actions[i].data.target)?('window.open(\''+appContext + record.data.actions[i].data.url+'\');'):('location.href=\''+appContext + record.data.actions[i].data.url+'\';');
									detailHtml += '<button class="form-action-button button '+style+'" onClick="' + code + '">' + record.data.actions[i].data.name  + '</button>';
								}
						}
						
						detailHtml += "</div></div>";
						jQuery("#protocolform-rowdetail").html(detailHtml);
						jQuery("#protocolform-norowselected").hide();
						Ext.getCmp("tpFormDetails").setDisabled(false);
						Ext.getCmp("contractformstatuspanel").enable();
						Ext.getCmp("contractformstatuspanel").formId = record.data.protocolFormId;
						Ext.getCmp("contractformstatuspanel").resetUrl();
						Ext.getCmp("contractformstatuspanel").getStore().load();
						
						Ext.getCmp("contractformnotespanel").enable();
						Ext.getCmp("contractformnotespanel").formId = record.data.protocolFormId;
						Ext.getCmp("contractformnotespanel").resetUrl();
						Ext.getCmp("contractformnotespanel").getStore().load();
				    }
				}
		};
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		Clara.ProtocolDashboard.FormGridPanel.superclass.initComponent.apply(this, arguments);

		
	}
});
Ext.reg('claraprotocolformgridpanel', Clara.ProtocolDashboard.FormGridPanel);




Clara.ProtocolDashboard.OverviewPanel = Ext.extend(Ext.Panel, {
	id: 'clara-protocol-overviewpanel',
	frame:false,
	layout:'fit',
	border:false,
	height:350,
	agendaItem:{},
	viewAsAgendaItem:false,
	agenda:{},

	protocolInfoEl:"",
	constructor:function(config){		
		Clara.ProtocolDashboard.OverviewPanel.superclass.constructor.call(this, config);
	},

	initComponent: function(){
		var t = this;
		var iframeHtml = '<iframe style="overflow:auto;width:100%;height:100%;" frameborder="0" id="protocolOverview" src="'
			+ appContext
			+ '/protocols/'
			+ claraInstance.id
			+ '/summary?noheader=true"></iframe>';
		var config = {
				html:iframeHtml,
				iconCls:'icn-book',
				title:'Overview',
				border:false
		};
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		Clara.ProtocolDashboard.OverviewPanel.superclass.initComponent.apply(this, arguments);
	}
});
Ext.reg('claraprotocoloverviewpanel', Clara.ProtocolDashboard.OverviewPanel);


Clara.ProtocolDashboard.DashboardTabPanel = Ext.extend(Ext.Panel, {
	id: 'clara-protocol-dashboardpanel',
	frame:false,
	layout:'border',
	border:false,
	agendaItem:{},
	viewAsAgendaItem:false,
	agenda:{},
	selectedFormId:null,
	headerHTML:"<div id='clara-protocol-db-header'></div>",
	protocolInfoEl:"",
	constructor:function(config){		
		Clara.ProtocolDashboard.DashboardTabPanel.superclass.constructor.call(this, config);
	},

	initComponent: function(){
		var t = this;
		var config = {
				items:[
				       { xtype:'panel',id:'dashboard-header-panel',autoHeight:true,html:this.headerHTML,region:'north', border:false,unstyled:true,bodyStyle:'background-color:#dfe1e3;',  
				    	 listeners:{
				   				afterrender: function(hp){
				    	   			var p = Ext.getCmp("clara-protocol-dashboardpanel");
				   					if (p.protocolInfoEl != "clara-protocol-db-header") { clog("GOING TO ATTACH "+p.protocolInfoEl+" TO header...");jQuery("#clara-protocol-db-header").append(jQuery("#"+p.protocolInfoEl)); }
				   					clog("doing layout");
				   					hp.doLayout();
				   				}
				   			}
				       },
				       { xtype:'tabpanel',id:'clara-protocol-dashboard-tabpanel',region:'center',activeTab:2,border:false,baseCls:'protocol-tabpanel',
				    	 items:[{xtype:'claraprotocoloverviewpanel'},
				    	        {xtype:'claradocumentpanel',title:'Documents',iconCls:'icn-folder-open-document-text',border:false,readOnly:true,hideFormOnlyOption:true,allDocs:(t.agenda.id)?false:true,helpHtml:'<h1>Documents for this protocol</h1><p>All documents for this protocol are displayed below. You can filter by file creator, document type, and upload time. If you need to edit or add documents to this protocol, go to the "Forms" tab and edit the documents on the appropriate form.</p>'},
				    	        {xtype:'claraprotocolformpanel', selectedFormId:t.selectedFormId},
				    	        {xtype:'claraprotocolletterpanel'},
				    	        {xtype:'clarahistorypanel',type:'protocol'}
				    	        ]}
				       ]
		};
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		Clara.ProtocolDashboard.DashboardTabPanel.superclass.initComponent.apply(this, arguments);
		
		if (t.viewAsAgendaItem == false){
			Ext.getCmp("clara-protocol-dashboard-tabpanel").add(
				      {xtype:'clararelatedobjectlistpanel',relatedObject:{type:'contract'}}
			);
		}
		
		if (t.selectedFormId != null){
			clog("selectedFormId NOT NULL",t.selectedFormId);
			Ext.getCmp("clara-protocol-dashboard-tabpanel").setActiveTab(2);
			
		}
		
	}
});
Ext.reg('claraprotocoldbtabpanel', Clara.ProtocolDashboard.DashboardTabPanel);





function renderProtocolDashboardPage(protocolInfoElement){

	new Ext.Viewport({
		layout:'border',
		border:false,
		items:[	{
				    region: 'north',
				    contentEl:'clara-header',
				    bodyStyle:{ backgroundColor:'transparent' },
				    height:48,
				    border: false,
				    margins: '0 0 0 0'
				}, 

				{	xtype:'claraprotocoldbtabpanel',
					protocolInfoEl: protocolInfoElement,
					region:'center'
				}
		       ]
	});
}