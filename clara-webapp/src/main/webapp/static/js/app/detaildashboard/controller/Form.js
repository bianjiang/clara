Ext.define('Clara.DetailDashboard.controller.Form', {
    extend: 'Ext.app.Controller',
    stores:['Clara.DetailDashboard.store.FormReviewCommittees'],
    
    refs: [{ ref: 'formGridPanel', selector: 'formgridpanel'},
           { ref: 'actionContainer', selector: 'formactioncontainer'},
           { ref: 'detailTabPanel', selector: '#formDetailTabPanel'},
           { ref: 'formReviewStatusPanel', selector:'formreviewstatuspanel'}],
   
    loadingMask: new Ext.LoadMask(Ext.getBody(), {msg:"Please wait..."}),
    selectedForm: null,

    init: function() {
    	var me = this;
    	
    	me.control({
    		'formgridpanel':{
    			itemclick : me.onFormSelect
    		},
    		'#btnNewForm':{
    			click: function(){
    				Ext.create("Clara.DetailDashboard.view.CreateFormWindow",{}).show();
    			}
    		}
    		
    	});
    },
    
    // Form action functions
    
    ChooseUploadDocumentRole: function(){
    	var me = this,
    		selectedRole= null,
    	    udStore = new Ext.data.Store({
    	    	id:'ChooseUploadDocumentRoleStore',
				autoLoad:true,
				
				proxy: {
					type: 'ajax',
					url:appContext+'/ajax/'+claraInstance.type+'s/'+claraInstance.id+'/'+claraInstance.type+'-forms/'+me.selectedForm.get("formId")+'/get-upload-documentuser-role-list',
				   	actionMethods: {
				        read: 'GET'
				    },
				   	headers:{'Accept':'application/xml;charset=UTF-8'},
					extraParams: {userId: claraInstance.user.id || 0},
					reader: {
						type:'xml',
						record:'committee',
			   		 	root:'committees'
					}
				},
                fields:[{name:'name',mapping:'@name'},{name:'desc',mapping:'@desc'}]
             });
    	
    	Ext.create("Ext.Window",{
    		modal:true,
    		title:'Who do you want to upload document as?',
    		layout:'fit',
    		height:200,
    		width:300,
    		border:false,
    		iconCls:'icn-user-silhouette',
    		items:[{
    			xtype:'grid',
    			hideHeaders:true,
    			id:'gpFormReviewCommittees',
    			store: udStore,
    			columns:[{dataIndex:'desc',flex:1}],
    			listeners:{
    				select:function(gp,record,idx){
    					selectedRole = record;
            			Ext.getCmp("btnReviewFormAsCommittee").setDisabled(false);
            		}
    			},
    			viewConfig:{
    				forceFit:true
    			}
    		}],
    		buttons:[{
    			id:'btnReviewFormAsCommittee',
    			disabled:true,
    			text:'Review Form',handler:function(){
    			location.href = appContext+"/"+claraInstance.type+"s/"+claraInstance.id+"/"+claraInstance.type+"-forms/"+me.selectedForm.get("formId")+"/review?committee="+selectedRole.get("name");
    		}}],
    	}).show();
    	
    	/*
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
    	 */
    },
    
    ExecuteForm: function(url){
    	var me = this;
        url= (url)?url:(appContext+"/ajax/contracts/"+claraInstance.id+"/contract-forms/"+me.selectedForm.get("formId")+"/contract-executed");
    	Ext.Msg.prompt('Execute?', '<strong>You are about to skip over the recommended review process.</strong><br/>If you wish to continue, enter your final review notes below and press "Ok".', function(btn, note){
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
    },
    
    ForwardSelectedContract: function(){
    	var me = this;

    	var contractFormId =  me.selectedForm.get("formId"),
    	formtypename =  me.selectedForm.get("formTypeId");
    	
    	
    	clog("FORWARD!!", me.selectedForm);
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
    					claraInstance.action = "FORWARD_"+cType;
    					
    					letterController = Clara.Application.getController('Clara.LetterBuilder.controller.LetterBuilder');
    					letterController.templateURL = appContext+"/ajax/contracts/"+claraInstance.id+"/contract-forms/"+contractFormId+"/email-templates/"+claraInstance.user.committee+"/"+cType;
    					letterController.sendURL = appContext+"/ajax/contracts/"+claraInstance.id+"/contract-forms/"+contractFormId+"/review/"+formtypename+"/sign";
    					
    					Ext.create("Clara.LetterBuilder.view.LetterBuilderWindow",{
    			    		templateId:cType,
    			    		parentMessageId:null,
    			    		requireSignature:false,
    			    		onSuccess: function(){
    			    			Ext.getCmp(id).close();
    			    		}
    			    	}).show();
    					
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
    },
    
    CloseStudy: function(protocolObj){
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
    			id:'fldCloseStudyReason',
    			hideLabel:true,
    			emptyText:'Please tell us why you are closing this study.'
    		}],
    		buttons:[{text:'Close study',handler:function(){
    			
    			Ext.Msg.confirm('Close this study?', msg, function(btn){
    			    if (btn == 'yes'){
    			        // process text value and close...
    			    	var url = appContext+"/ajax/protocols/"+pid+"/close-study";
    			    	jQuery.ajax({url: url,
    			    		type: "POST",
    			    		async: false,
    			    		data: { 
    			    			userId:claraInstance.user.id,
    			    			reason:Ext.getCmp('fldCloseStudyReason').getValue()
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
    },
    
    ExecuteContract: function(){
    	var me = this;
    	var url=appContext+"/ajax/contracts/"+claraInstance.id+"/contract-forms/"+me.selectedForm.get("formId")+"/contract-executed";
    	Ext.MessageBox.prompt('Execute this contract?', '<strong>You are about to skip over the recommended review process for this contract.</strong><br/>If you wish to continue, enter your final review notes below and press "Ok".', function(btn, note){
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
    	    			clog(msg);
    	    			if (msg.indexOf("<result><error>true</error>") == -1){
    	    				callback = function(){ location.reload(true); };
    	    			}	// UGLY UGLY UGLY
    	    		}
    	    	});	
    	    	
    	    }
    	},this,true);
    },
    
    CancelForm: function(){
    	var me = this,
    		cancelReasonRecord = {},
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
		     }),
    		callback = function(){ location.href=appContext; },
    		msg = "Are you sure you want to cancel?";
    	
    	new Ext.Window({
    		title:'Cancel?',
    		iconCls:'icn-blue-document-smiley-sad',
    		
    		width:500,
    		height:200,
    		padding:6,
    		autoHeight:true,
    		autoScroll:true,
    		layout:'form',
    		items:[{
    			hidden:(claraInstance.type != 'contract'),
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
	        			var s = Ext.getCmp('fldCancelReasonText');		
	        			s.setReadOnly(false);
	        			s.enable();
	        		},
	        		'select': function(cmb,rec,idx){
	        			var s = Ext.getCmp('fldCancelReasonText');
	        			s.setReadOnly(false);
	        			s.enable();
	        			cancelReasonRecord = rec;
	        			jQuery('#fldCancelReasonText').removeClass('x-item-disabled');
	        		}
	        	}
	        
    		},{
    			xtype:'textarea',
    			id:'fldCancelReasonText',
    			hideLabel:true,
    			disabled:(claraInstance.type == 'contract'),
    			emptyText:'Explain why you wish to cancel this item.'
    		}],
    		buttons:[{text:'Continue',handler:function(){
    			
    			var xml ="";
    			
    			if (claraInstance.type == "protocol"){
    				xml = "<cancel-reason type='PROTOCOL'>"+Encoder.cdataWrap(Ext.getCmp('fldCancelReasonText').getValue())+"</cancel-reason>";
    			} else {
    				if (typeof cancelReasonRecord.get == "function"){
    					xml ="<cancel-reason type='CONTRACT' id='"+cancelReasonRecord.get("id")+"' text='"+Encoder.htmlEncode(cancelReasonRecord.get("name"))+"' subid='' subtext='"+Encoder.htmlEncode(Ext.getCmp('fldCancelReasonText').getValue())+"' />";
    				}
    				else {
    					xml ="<cancel-reason type='CONTRACT' id='404' text='"+Encoder.htmlEncode(Ext.getCmp('fldReason').getRawValue())+"' subid='' subtext='"+Encoder.htmlEncode(Ext.getCmp('fldCancelReasonText').getValue())+"' />";
    				}	
    			}
    			
    			Ext.Msg.confirm('Cancel?', msg, function(btn){
    			    if (btn == 'yes'){
    			        // process text value and close...
    			    	var url = appContext+"/ajax/"+claraInstance.type+"s/"+claraInstance.id+"/"+claraInstance.type+"-forms/"+me.selectedForm.get("formId")+"/cancel";
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
    },
    
    ChooseReviewRole: function(){
    	var me = this;
    	var st = Ext.data.StoreManager.lookup('Clara.DetailDashboard.store.FormReviewCommittees');
    	st.loadFormReviewCommittees(me.selectedForm);
    	
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
    			var rec = g.getSelectionModel().getSelection()[0];

    			location.href = appContext+"/"+claraInstance.type+"s/"+claraInstance.id+"/"+claraInstance.type+"-forms/"+me.selectedForm.get("formId")+"/review?committee="+rec.get("name");
    			
    		}}],
    		items:[{
    			xtype:'grid',
    			hideHeaders:true,
    			border:false,
    			id:'gpFormReviewCommittees',
    			store: 'Clara.DetailDashboard.store.FormReviewCommittees',
    			columns:[{id:'col-rf-name',dataIndex:'desc',flex:1}],
    			listeners:{
    				itemclick:function(gp, record, item){
            			Ext.getCmp("btnReviewFormAsCommittee").setDisabled(false);
            		}
    			},
    			sm: new Ext.selection.Model({mode:'SINGLE'})
    		}]
    	}).show();

    },
    
    RemoveBudget: function(){
    	var me = this;
    	var callback = function(){ alert("Budget deleted.") };
    	var msg = "Are you sure you want to remove the budget?";

    	Ext.Msg.confirm('Delete this budget?', msg, function(btn){
    	    if (btn == 'yes'){
    	        // process text value and close...
    	    	var url = appContext+"/ajax/protocols/"+claraInstance.id+"/protocol-forms/"+me.selectedForm.get("formId")+"/budgets/delete";
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
    },
    
    RemoveForm: function(){
    	var me = this,
    	    callback = {},
    	    msg = "Are you sure you want to remove this form?";
    	
    	if (me.selectedForm.get("formtype") == "New Submission"){
    		callback = function(){ location.href=appContext; };
    		msg += "<br/><span style='font-weight:800;'>WARNING: Since you're removing the New Submission form, this will remove the protocol too.</span>";
    	} else {
    		callback = function(){ location.reload(true); };
    	}
    	msg += "<br/><span style='font-weight:800;color:red;text-decoration:underline;'>If this form contains a budget, it will be deleted as well.</span>";
    	
    	Ext.Msg.confirm('Delete this form?', msg, function(btn){
    	    if (btn == 'yes'){
    	        // process text value and close...
    	    	var url = appContext+"/ajax/protocols/"+claraInstance.id+"/protocol-forms/"+me.selectedForm.get("formId")+"/delete";
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
    },
    
    
    // end Form action functions
    
    onFormSelect: function(gp, record, item){
    	clog("form selected",record);
    	
    	var me = this,
    		detailHtml = '<div class="formActions"><h1>'+record.get('formtype')+'</h1>',
    		actions = record.itemActions();
    	
    	me.getFormGridPanel().fireEvent("formselected",record);
    	
    	me.selectedForm = record;
    	if (typeof Clara.Application.ReviewNoteController !== "undefined") Clara.Application.ReviewNoteController.selectedFormId = record.get("formId");
    	me.getDetailTabPanel().setDisabled(false);
    	
    	
    	actions.each(function(rec){
    		var style =rec.get("class");

			if(rec.data.urlType == 'javascript'){
				detailHtml += '<button class="form-action-button button '+style+'" onClick="' + rec.get("url") + '">' + rec.get("name")  + '</button>';
			}else if(rec.get("urlType") == 'ajax'){
				detailHtml += '<button class="form-action-button button '+style+'" onClick="javascript:ajax_link_clicked(\'' + appContext + rec.get("url") + '\', \'' + (rec.get("response")) + '\');">' + rec.get("name")  + '</button>';
			}else if(rec.get("urlType") == 'label'){
				detailHtml += '<span class="'+style+'">' + rec.get("name")  + '</span><br/>';
				
			}else{
				var code = (rec.get("target"))?('window.open(\''+appContext + rec.get("url")+'\');'):('location.href=\''+appContext + rec.get("url")+'\';');
				detailHtml += '<button class="form-action-button button '+style+'" onClick="' + code + '">' + rec.get("name")  + '</button>';
			}
		});
    	
    	clog(detailHtml);
    	me.getActionContainer().update(detailHtml+"</div>");
    	if (me.getFormReviewStatusPanel().rendered) me.getFormReviewStatusPanel().getStore().loadFormReviewStatus(record);
    	if (Clara.Application.ReviewNoteController.getReviewNotePanel().rendered) Clara.Application.ReviewNoteController.reloadNotes(record.get("formId"));
    },
    
    reloadForms: function(){
    	var st = Ext.data.StoreManager.lookup('Clara.DetailDashboard.store.Forms');
    	st.load();
    }
    
});