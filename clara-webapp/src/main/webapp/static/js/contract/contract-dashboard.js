Ext.ns('Clara.ContractDashboard','Clara.ProtocolForms');
Ext.QuickTips.init();

var contractDocumentPanel;
var contractDashboardPanel;
var contractTabPanel;
var contractDetailPanel;
var contractHeaderPanel;
var contractNotificaitonPanel;
var contractFormPanel;
var selectedContractRecord;


Clara.ContractDashboard.SelectedFormRecord = {};

Clara.ContractDashboard.ChooseReviewRole = function(){
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
			location.href = appContext+"/contracts/"+claraInstance.id+"/contract-forms/"+Clara.ContractDashboard.SelectedFormRecord.get("contractFormId")+"/review?committee="+rec.get("name");
			
		}}],
		items:[{
			xtype:'grid',
			hideHeaders:true,
			id:'gpFormReviewCommittees',
				store: new Ext.data.XmlStore({
					autoLoad:true,
					proxy: new Ext.data.HttpProxy({
						url:appContext+'/ajax/contracts/'+claraInstance.id+"/contract-forms/"+Clara.ContractDashboard.SelectedFormRecord.get("contractFormId")+"/get-user-role-list?userId="+claraInstance.user.id,
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

Clara.ContractDashboard.ChooseUploadDocumentRole = function(){
	new Ext.Window({
		model:true,
		title:'Who do you want to upload document as?',
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
			location.href = appContext+"/contracts/"+claraInstance.id+"/contract-forms/"+Clara.ContractDashboard.SelectedFormRecord.get("contractFormId")+"/review?committee="+rec.get("name");
			
		}}],
		items:[{
			xtype:'grid',
			hideHeaders:true,
			id:'gpFormReviewCommittees',
				store: new Ext.data.XmlStore({
					autoLoad:true,
					proxy: new Ext.data.HttpProxy({
						url:appContext+'/ajax/contracts/'+claraInstance.id+"/contract-forms/"+Clara.ContractDashboard.SelectedFormRecord.get("contractFormId")+"/get-upload-documentuser-role-list?userId="+claraInstance.user.id,
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

Clara.ContractDashboard.CancelForm = function(){
	// removes selected protocol form (if allowed)
	var callback = {};
	var msg = "Are you sure you want to cancel this contract?";
	
	if (Clara.ContractDashboard.SelectedFormRecord.get("formtype") == "New Contract"){
		callback = function(){ location.href=appContext+"/contracts/index"; };
		msg += "<br/><span style='font-weight:800;'>WARNING: Since you're cancelling the New Contract form, this will cancel the entire contract too.</span>";
	} else {
		callback = function(){ location.reload(true); };
	}
	
	var reason = {};
	var subreason = {};
	
	reasonstore = new Ext.data.ArrayStore({
        fields:['id','name'],
        autoLoad:true,
        data: [
        [17,'Unable to agree on contract language'],
        [10,'Unable to agree to budget terms'],
        [18,'UAMS does not have an appropriate facility'],
        [19,'UAMS does not have appropriate equipment'],
        [20,'Principal Investigator left'],
        [21,'Cannot meet sponsor\'s timeline for approval/initiation of study'],
        [22,'Other'] 
        ]
     });
	
subreasondata = [
	[17,1,'AAHRPP Language'],
	[17,2,'Data Ownership'],
	[17,3,'Governing Law'],
	[17,4,'Indemnification'],
	[17,5,'Insurance (UAMS has none)'],
	[17,6,'Intellectual Property'],
	[17,7,'Publication Rights'],
	[17,8,'Subject Injury Language'],
	[17,9,'Other'],
	[10,11,'Disagreement on coverage assessment'],
	[10,12,'Indirect cost rate is too high'],
	[10,13,'Laboratory costs are excessive'],
	[10,14,'Other procedure costs are excessive'],
	[10,15,'Staff costs are excessive'],
	[10,16,'Other']
];
	
	subreasonstore = new Ext.data.ArrayStore({
        fields:['parentid','id','name'],
        data: subreasondata,
        autoLoad:false
     });
	
	new Ext.Window({
		id:id,
		title:'Cancel Contract',
		width:500,
		padding:6,
		autoHeight:true,
		autoScroll:true,
		layout:'form',
		items:[{
            xtype: 'combo',
            fieldLabel:'Reason',
            width:360,
            store:reasonstore,
            id: 'fldReason',
            typeAhead:false,
            forceSelection:false,
            displayField:'name', 
            valueField:'id',
            editable:true,
        	allowBlank:false,
            mode:'local', 
        	triggerAction:'all',
        	listeners:{
        		'change':function(){
        			var s = Ext.getCmp('fldSubReason');		
        			s.setReadOnly(false);
        			s.enable();
        			s.getStore().removeAll();   
        			s.clearValue();
        		},
        		'select': function(cmb,rec,idx){
        			var c = Ext.getCmp('fldReason');
        			var s = Ext.getCmp('fldSubReason');
        			s.setReadOnly(false);
        			s.enable();
        			s.clearValue();
        			reason = rec;
        			s.getStore().removeAll();       			
        			s.getStore().loadData(subreasondata);
        			s.getStore().filter('parentid',rec.get("id"));
        			clog(rec,s.getStore().data.length);
        			jQuery('#fldSubReason').removeClass('x-item-disabled');
        		}
        	}
        
		},{
            xtype: 'combo',
            fieldLabel:'Detail',
            width:360,
            store:subreasonstore,
            id: 'fldSubReason',
            readOnly:true,
            disabled:true,
            typeAhead:false,
            forceSelection:false,
            displayField:'name', 
            valueField:'id',
            editable:true,
        	// allowBlank:false,
            mode:'local', 
        	triggerAction:'all',
        	listeners:{
        		'select': function(cmb,rec,idx){
        			subreason = rec;
        		}
        	}
        
		}],
		buttons:[{text:'Continue',handler:function(){
			
			var xml = "";
			if (typeof reason.get == "function"){
				if (typeof subreason.get == "function") xml ="<cancel-reason id='"+reason.get("id")+"' text='"+Encoder.htmlEncode(reason.get("name"))+"' subid='' subtext='"+Encoder.htmlEncode(subreason.get("name"))+"' />";
				else {
					xml ="<cancel-reason type='CONTRACT' id='"+reason.get("id")+"' text='"+Encoder.htmlEncode(reason.get("name"))+"' subid='' subtext='"+Encoder.htmlEncode(Ext.getCmp('fldSubReason').getRawValue())+"' />";
				}
			}
			else {
				xml ="<cancel-reason type='CONTRACT' id='404' text='"+Encoder.htmlEncode(Ext.getCmp('fldReason').getRawValue())+"' subid='' subtext='"+Encoder.htmlEncode(Ext.getCmp('fldSubReason').getRawValue())+"' />";
			}
			
			Ext.Msg.confirm('Cancel this form?', msg, function(btn){
			    if (btn == 'yes'){
			        // process text value and close...
			    	var url = appContext+"/ajax/contracts/"+claraInstance.id+"/contract-forms/"+Clara.ContractDashboard.SelectedFormRecord.get("contractFormId")+"/cancel";
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

Clara.ContractDashboard.ExecuteForm = function(){
	var url=appContext+"/ajax/contracts/"+claraInstance.id+"/contract-forms/"+Clara.ContractDashboard.SelectedFormRecord.get("contractFormId")+"/contract-executed";
	Ext.Msg.prompt('Execute this contract?', '<strong>You are about to skip over the recommended review process for this contract.</strong><br/>If you wish to continue, enter your final review notes below and press "Ok".', function(btn, note){
	    if (btn == 'ok'){
	        // process text value and close...
	    	jQuery.ajax({url: url,
	    		type: "POST",
	    		async: false,
	    		dataType:'text',
	    		data: {
	    			userId: claraInstance.user.id,
	    			note:note
	    		},
	    		success: function(msg){
	    			location.reload(true);
	    		}
	    	});	
	    	
	    }
	},this,true);
}

Clara.ContractDashboard.RemoveForm = function(){
	// removes selected protocol form (if allowed)
	var callback = {};
	var msg = "Are you sure you want to remove this form?";
	
	if (Clara.ContractDashboard.SelectedFormRecord.get("formtype") == "New Contract"){
		callback = function(){ location.href=appContext+"/contracts/index"; };
		msg += "<br/><span style='font-weight:800;'>WARNING: Since you're removing the New Contract form, this will also remove the contract if there is no Amendment.</span>";
	} else {
		callback = function(){ location.reload(true); };
	}
	
	Ext.Msg.confirm('Delete this form?', msg, function(btn){
	    if (btn == 'yes'){
	        // process text value and close...
	    	var url = appContext+"/ajax/contracts/"+claraInstance.id+"/contract-forms/"+Clara.ContractDashboard.SelectedFormRecord.get("contractFormId")+"/delete";
	    	jQuery.ajax({url: url,
	    		type: "GET",
	    		async: false,
	    		dataType:'text',
	    		data: {},
	    		success: function(msg){
	    			clog(msg);
	    			if (msg.indexOf("<result><error>true</error>") == -1){callback();}	// UGLY UGLY UGLY
	    		}
	    	});	
	    	
	    }
	});
};


Clara.ContractDashboard.forwardSelectedContract = function(contractFormId,formtypename){
	
	if (typeof selectedContractRecord != 'undefined' && typeof selectedContractRecord.data != 'undefined'){
		contractFormId = selectedContractRecord.get("contractFormId");
		formtypename = selectedContractRecord.get("formtypename");
	}
	
	clog("FORWARD!!",selectedContractRecord);
	var id = Ext.id();
	
	new Ext.Window({
		id:id,
		title:'Forward Contract',
		width:500,
		padding:6,
		autoHeight:true,
		autoScroll:true,
		layout:'form',
		buttons:[{
			text:'Continue',
			handler: function(){
				var cType = Ext.getCmp("fldForwardContractType").getValue();
				if (cType && cType != ""){
					var templateURL=appContext+"/ajax/contracts/"+claraInstance.id+"/contract-forms/"+contractFormId+"/email-templates/"+claraInstance.user.committee+"/"+cType;
					clog(templateURL);
					claraInstance.action = "FORWARD_"+cType;
					var sendURL = appContext+"/ajax/contracts/"+claraInstance.id+"/contract-forms/"+contractFormId+"/review/"+formtypename+"/sign";
					var message = new Clara.Mail.MessageWindow({
						getMessageXml: function(message){
							return "<committee-review><committee type='"+claraInstance.user.committee+"'><actor>"+claraInstance.user.committee+"</actor><action>"+claraInstance.action+"</action><letter><message><to>"+message.to+"</to><cc>"+message.cc+"</cc><subject>"+Encoder.cdataWrap(message.subject)+"</subject><body>"+Encoder.cdataWrap(message.body)+"</body></message></letter></committee></committee-review>";
						},
						sendFunction:Clara.Mail.SignAndSubmit,
						onSuccess:function(){
							Ext.getCmp(id).close();
						},
						templateUrl:templateURL,
						sendUrl:sendURL,
						delayedSend:false,
						requireSignature:true,
						metadata:claraInstance,
						title:'Send Letter',
						modal:true,
						iconCls:'icn-mail--pencil'
					});
					message.show();	
				} else {
					alert("Choose a contract template type.");
				}
			}
		}],
		items:[{
			width:330,
			xtype:'combo',
		    typeAhead: true,
		    id:'fldForwardContractType',
		    fieldLabel:'Select a template',
		    triggerAction: 'all',
		    lazyRender:true,
		    mode: 'local',
		    store: new Ext.data.ArrayStore({
		        fields: [
		            'name',
		            'desc'
		        ],
		        data: [['EMAIL_NOTICE', 'Email Notice'], ['FORWARD_CONTRACT', 'Forward this Contract'], 
		               ['BUDGET_EXHIBIT_MODIFIED', 'Budget Exhibit Modified'], ['BUDGET_EXHIBIT_APPROVED', 'Budget Exhibit Approved'],
		               ['FORWARD_WRITTEN_CONSENT_FOR_REVIEW', 'ICF Review Notice'], ['FORWARD_WRITTEN_CONSENT_WITH_FINAL_LEGAL_APPROVAL', 'ICF Approval Notice']]
		    }),
		    valueField: 'name',
		    displayField: 'desc'
		}]
	}).show();
};

Clara.ContractDashboard.LetterPanel = Ext.extend(Ext.grid.GridPanel, {
	id: 'clara-contract-db-letterpanel',
	height:350,
	title:'Letters',
	iconCls:'icn-mail',
	layout:'fit',
	tbar: {
		xtype:'toolbar',
		items:[
		       {iconCls:'icn-mail--plus',text:'New Letter',iconAlign:'top'}
           	]},
	constructor:function(config){		
		Clara.ContractDashboard.LetterPanel.superclass.constructor.call(this, config);
	},	
	initComponent: function(){

		var config = {
		        	border:false,
		        	loadMask:true,
					store: new Ext.data.Store({
						autoLoad:true,

						proxy: new Ext.data.HttpProxy({
							url:appContext+"/ajax/history/history.xml?id="+claraInstance.id+"&type=edu.uams.clara.webapp.contract.domain.Contract&filter=letter",
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
							{name:'timestamp', mapping:'@timestamp', type:'date', dateFormat:'YmdHis'},//'m/d/Y g:i:s'}
							{name:'datetime', mapping:'@date-time'}
						]})
					}),
					colModel: new Ext.grid.ColumnModel({
				        defaults: {
				            width: 120,
				            sortable: true
				        },
				        columns: [
						            {
						                header: 'Date', width: 135,fixed:true, dataIndex: 'datetime',
						                xtype: 'datecolumn', format: 'm/d/Y h:i:sa'
						            },
						            {header: 'Who?', width:150,fixed:true,dataIndex: 'actor'},
						            {header: 'Form', width:150,fixed:true,dataIndex: 'formType'},
						            {header: 'Message', dataIndex: 'desc'}
				        ]
				    }),
				    viewConfig: {
				        forceFit: true
		        	}
		        
		};
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		Clara.ContractDashboard.LetterPanel.superclass.initComponent.apply(this, arguments);
	}
});
Ext.reg('claracontractletterpanel', Clara.ContractDashboard.LetterPanel);


Clara.ContractDashboard.FormPanel = Ext.extend(Ext.Panel, {
	id: 'clara-contract-db-formpanel',
	height:350,
	title:'Forms',
	iconCls:'icn-application-form',
	layout:'border',
	constructor:function(config){		
		Clara.ContractDashboard.FormPanel.superclass.constructor.call(this, config);
	},	
	
	initComponent: function(){

		var config = {
				scope:this,
				tbar: new Ext.Toolbar({
		    		style:'border-left: 1px solid #8DB2E3;',
		    		items:[
{
   	id: 'btnAddForm',         	    	
	 	iconCls:'icn-plus-button',
	 	text: 'New form..',
	 	handler:function(){
	 		var w = new Clara.NewFormListWindow();
	 		w.show();
	 	}
	 	
	 }
			           	]}),
			           	items:[{xtype:'claracontractformgridpanel',style:'border-right:1px solid #96baea;',  region:'center',border:false,split:true},
						       {
			           				xtype:'tabpanel',id:'tpFormDetails',style:'border-left:1px solid #96baea;',region:'east',width:500,border:false,split:true,activeItem:0,disabled:true,
			           				items:[{title:'Actions',iconCls:'icn-hand-point',xtype:'panel',border:false,style:'border-bottom:1px solid #96baea;width:100%;background-color:#dee8f7;',padding:6,html:'<div id="protocolform-rowdetail"></div><div id="protocolform-norowselected">Choose a form to see more details.</div>'},
			           				    {title:'Review Status',iconCls:'icn-dashboard',id:'contractformstatuspanel', xtype:'claraformstatusgridpanel',border:false}]
						            
						       }
						       ]
		};
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		Clara.ContractDashboard.FormPanel.superclass.initComponent.apply(this, arguments);
	}
});
Ext.reg('claracontractformpanel', Clara.ContractDashboard.FormPanel);




Clara.ContractDashboard.FormGridPanel = Ext.extend(Ext.grid.GridPanel, {
	id: 'clara-contract-db-formgridpanel',
	height:350,
	bodyStyle:'border-bottom: 1px solid #8DB2E3;',
	constructor:function(config){		
		Clara.ContractDashboard.FormGridPanel.superclass.constructor.call(this, config);
	},	
	
	initComponent: function(){
		var config = {
				store: new Ext.data.XmlStore({
					scope:this,
					
					proxy: new Ext.data.HttpProxy({
						scope:this,
						url: appContext + "/ajax/contracts/"+claraInstance.id+"/contract-forms/list.xml?userId="+claraInstance.user.id, //
						method:"GET",
						headers:{'Accept':'application/xml;charset=UTF-8'}
					}),
					record: 'contract-form', 
					autoLoad:true,
					root:'list',
					fields: [
					    {name:'contractFormId', mapping: '@contractFormId'},
					    {name:'formtype', mapping:'contract-form-type'},
					    {name:'formIndex', mapping:'@index'},
						{name:'formtypename', mapping:'contract-form-type@id'},
						{name:'url',mapping:'url'},
						
						{name:'locked',mapping:'status>lock@value'},
						{name:'lockUserId',mapping:'status>lock@userid'},
						{name:'lockModified',mapping:'status>lock@modified', type: 'date', dateFormat: 'Y-m-d H:i:s.u'},
						{name:'lockMessage',mapping:'status>lock@message'},
						{name:'editurl',mapping:'editurl'},
						{name:'status', mapping:'status>description'},
						{name:'statusModified',mapping:'status>modified', type: 'date', dateFormat: 'Y-m-d H:i:s.u'},
						{name:'actions',convert:function(v,node){ return new Ext.data.XmlReader({record: 'action',fields: [{name:'type'},{name:'class', mapping:'@cls'},{name:'urlType',mapping:'url@type'},{name:'name'},{name:'target',mapping:'url@target'},{name:'url'}]}).readRecords(node).records; }},
						{
							name : 'assignedReviewers',
							mapping:'assigned-reviewers',
							convert : function(v, node) {
								var recs = new Ext.data.XmlReader(
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
										}).readRecords(node).records;
							    return recs;
							}
						}
						
					]
				}),
		        viewConfig: {
		    		forceFit:true,
		    		loadMask:true	// TODO: This isnt displaying, probably because we're deferring store loading until the tab is opened. FIX.
		    	},
		        columns: [
		            {header: 'Form Type', width: 240, sortable: true, dataIndex: 'formtype', renderer:function(value, p, record){
		            	var locked = record.get("locked");
		        		var url=record.data.url;
		        		var formType = record.data.formtype;
		        		var str = (locked === "true")?"<div class='form-row form-row-locked'>":"<div class='form-row'>";
		        		if (record.get("formIndex") > 0) str += "<span class='contract-form-row-field contract-form-type'>"+formType+" #" +record.get("formIndex")+"</span>"
		        		else str += "<span class='contract-form-row-field contract-form-type'>"+formType+"</span>";
		        		str += (locked === "true")?"<div style='font-weight:800;border:1px solid red; padding:4px;margin-top:2px;'>"+record.get("lockMessage")+"</div>":"";
		        		return str+"</div>";
		            }},
		            {header: 'Last Modified', width: 95, sortable: true, renderer: function(value) { return "<span class='contract-form-row-field'>"+Ext.util.Format.date(value,'m/d/Y')+"</span>";}, dataIndex: 'statusModified'},
		            {header: 'Status', id:'contract-forms-status-column', width: 200, sortable: true, dataIndex: 'status', renderer:function(v,p,record){return "<span class='contract-form-row-field contract-form-status'>"+record.data.status+"</span>";}},
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
		    		
				    rowclick: function(grid, rowI, event)   {
				    	var record = grid.getStore().getAt(rowI);
				    	selectedContractRecord=record;
						clog(record);
						Clara.ContractDashboard.SelectedFormRecord = record;
						claraInstance.form.id = record.get("contractFormId");
						claraInstance.form.type = record.get("formtype");
						
						var detailHtml = "<div class='protocolform-info'>";
						detailHtml += "<dl><dt class='protocolform-type'>Title</dt><dd class='protocolform-type'>"+record.get("formtype")+"</dd>";
						detailHtml += "<dl><dt>Status</dt><dd>"+record.get("status")+"</dd>";
						detailHtml += "<dt>Modified</dt><dd>"+record.get("statusModified")+"</dd></dl>";
						detailHtml += "<div class='protocolform-actions'>";
							
							for (var i=0; i<record.data.actions.length; i++) {
								var style =record.data.actions[i].get("class");
								//check for "forward" action first
								if(record.data.actions[i].get("name").toLowerCase() == 'forward'){
									detailHtml += '<button class="form-action-button button '+style+'" onClick="javascript:Clara.ContractDashboard.forwardSelectedContract();">' + record.data.actions[i].data.name  + '</button><br/>';
								}else {
									if(record.data.actions[i].data.urlType == 'javascript'){
										detailHtml += '<button class="form-action-button button '+style+'" onClick="' + record.data.actions[i].data.url + '">' + record.data.actions[i].data.name  + '</button><br/>';
									}else{
										var code = (record.data.actions[i].data.target)?('window.open(\''+appContext + record.data.actions[i].data.url+'\');'):('location.href=\''+appContext + record.data.actions[i].data.url+'\';');
										detailHtml += '<button class="form-action-button button '+style+'" onClick="' + code + '">' + record.data.actions[i].data.name  + '</button><br/>';
									}
								}
							}
						
						detailHtml += "</div></div>";
						jQuery("#protocolform-rowdetail").html(detailHtml);
						jQuery("#protocolform-norowselected").hide();
Ext.getCmp("tpFormDetails").setDisabled(false);
						Ext.getCmp("contractformstatuspanel").enable();
						Ext.getCmp("contractformstatuspanel").formId = record.data.contractFormId;
						Ext.getCmp("contractformstatuspanel").resetUrl();
						Ext.getCmp("contractformstatuspanel").getStore().load();
				    }
				}
		};
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		Clara.ContractDashboard.FormGridPanel.superclass.initComponent.apply(this, arguments);
	}
});
Ext.reg('claracontractformgridpanel', Clara.ContractDashboard.FormGridPanel);




Clara.ContractDashboard.DashboardTabPanel = Ext.extend(Ext.Panel, {
	id: 'clara-contract-dashboardpanel',
	frame:false,
	layout:'border',
	border:false,
	height:350,
	agendaItem:{},
	agenda:{},
	headerHTML:"<div id='clara-contract-db-header'></div>",
	contractInfoEl:"",
	constructor:function(config){		
		Clara.ContractDashboard.DashboardTabPanel.superclass.constructor.call(this, config);
	},

	initComponent: function(){
		var t = this;
		var config = {
				items:[
				       { xtype:'panel',id:'dashboard-header-panel',autoHeight:true,html:this.headerHTML,region:'north', border:false,unstyled:true,bodyStyle:'background-color:#dfe1e3;',  
				    	 listeners:{
				   				afterrender: function(hp){
				    	   			var p = Ext.getCmp("clara-contract-dashboardpanel");
				   					if (p.contractInfoEl != "clara-contract-db-header") { clog("GOING TO ATTACH "+p.contractInfoEl+" TO header...");jQuery("#clara-contract-db-header").append(jQuery("#"+p.contractInfoEl)); }
				   					clog("doing layout");
				   					hp.doLayout();
				   				}
				   			}
				       },
				       { xtype:'tabpanel',id:'clara-contract-dashboard-tabpanel',region:'center',activeTab:0,border:false,baseCls:'contract-tabpanel',
				    	 items:[{xtype:'claracontractformpanel'},
				    	          {xtype:'claradocumentpanel',title:'Documents',iconCls:'icn-folder-open-document-text',border:false,readOnly:true,allDocs:true, hideFormOnlyOption:true},
				    	          {xtype:'clarahistorypanel',type:'contract'},
							      {xtype:'clararelatedobjectlistpanel',relatedObject:{type:'contract'}},
							      {xtype:'clararelatedobjectlistpanel',relatedObject:{type:'protocol'}}]}
				       ]
		};
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		Clara.ContractDashboard.DashboardTabPanel.superclass.initComponent.apply(this, arguments);
	}
});
Ext.reg('claracontractdbtabpanel', Clara.ContractDashboard.DashboardTabPanel);




function renderContractDashboardPage(contractInfoElement){

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

				{	xtype:'claracontractdbtabpanel',
					contractInfoEl: contractInfoElement,
					region:'center'
				}
		       ]
	});
}