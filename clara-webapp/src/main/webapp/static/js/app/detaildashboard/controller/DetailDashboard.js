Ext.define('Clara.DetailDashboard.controller.DetailDashboard', {
    extend: 'Ext.app.Controller',
    models: ['Clara.Common.model.Protocol','Clara.Common.model.Contract','Clara.DetailDashboard.model.History'],
    stores: ['Clara.Common.store.Protocols','Clara.Common.store.Contracts','Clara.DetailDashboard.store.History'],
    
    refs: [{ ref: 'historyPanel', selector: 'historypanel'},
           { ref:'addRelatedContractWindow', selector:'addrelatedcontractwindow'},
           { ref:'addRelatedProtocolWindow', selector:'addrelatedprotocolwindow'},
           { ref:'letterPanel', selector: 'letterpanel'}],
   
    loadingMask: new Ext.LoadMask(Ext.getBody(), {msg:"Please wait..."}),
    selectedLetter: null,
    selectedRelatedObject: null,
    
    init: function() {
    	var me = this;
    	
    	me.control({
    		'letterpanel':{
    			itemclick : me.onLetterSelect
    		},
    		'relatedcontractpanel':{
    			itemclick : me.onRelatedContractSelect
    		},
    		'relatedprotocolpanel':{
    			itemclick : me.onRelatedProtocolSelect
    		},
    		'addrelatedcontractwindow commoncontractgridpanel':{
    			itemclick : me.onAddContractSelect
    		},
    		'addrelatedprotocolwindow commonprotocolgridpanel':{
    			itemclick : me.onAddProtocolSelect
    		},
    		'#btnRemoveRelatedProtocol':{
    			click: function() { me.removeRelatedObject("protocol"); }
    		},
    		'#btnAddRelatedProtocol':{
    			click: function(){
    				Ext.create('Clara.DetailDashboard.view.related.AddProtocolWindow',{}).show();
    			}
    		},
    		'#btnRemoveRelatedContract':{
    			click: function() { me.removeRelatedObject("contract"); }
    		},
    		'#btnAddRelatedContract':{
    			click: function(){
    				Ext.create('Clara.DetailDashboard.view.related.AddContractWindow',{}).show();
    			}
    		},
    		'#btnAddSelectedProtocol':{
    			click: me.addSelectedObject
    		},
    		'#btnAddSelectedContract':{
    			click: me.addSelectedObject
    		},
    		'#btnToggleGroupHistory':{
        		toggle:me.onToggleGroupHistory
        	},
        	'#btnToggleGroupLetters':{
        		toggle:me.onToggleGroupLetters
        	},
        	'#btnNewIRBLetter':{
        		click:function() { me.createLetter("IRB_LETTER"); }
        	},
        	'#btnNewCorrectionLetter':{
        		click:function() { me.createLetter("IRB_CORRECTION_LETTER"); }
        	},
        	'#btnNewAuditReportLetter':{
        		click:function() { me.createLetter("RECEIPT_OF_AUDIT_REPORT_LETTER"); }
        	},
        	'#btnPrintHistory':{
        		click:me.onPrintHistory
        	}
    	});
    },
    
    addSelectedObject: function(){
    	var me = this;
    	Ext.Ajax
			.request({
				url : appContext+"/ajax/add-related-object",
				method : 'POST',
				success : function() {
				  if (me.selectedRelatedObject.relatedObjectType == "protocol") me.getAddRelatedProtocolWindow().close();
			      else me.getAddRelatedContractWindow().close();
				  var store = Ext.data.StoreManager.lookup((me.selectedRelatedObject.relatedObjectType == "protocol")?'Clara.DetailDashboard.store.RelatedProtocols':'Clara.DetailDashboard.store.RelatedContracts');
 				  if (me.selectedRelatedObject.relatedObjectType == "protocol") store.loadRelatedProtocols();
 				  else store.loadRelatedContracts();
 				  me.selectedRelatedObject = {};
				},
				params : {
					objectId: claraInstance.id,
					objectType:claraInstance.type,
					relatedObjectType:me.selectedRelatedObject.relatedObjectType,
					relatedObjectId: me.selectedRelatedObject.get("id"), 
					userId:claraInstance.user.id
					}
			});
    },
    
    onAddContractSelect: function(gp, rec, item){
    	var me = this;
    	clog("Contract selected",rec);
    	me.selectedRelatedObject = rec;
    	me.selectedRelatedObject.relatedObjectType = "contract";
    	Ext.getCmp("btnAddSelectedContract").setDisabled(false);
    },
    
    onAddProtocolSelect: function(gp, rec, item){
    	var me = this;
    	clog("Protocol selected",rec);
    	me.selectedRelatedObject = rec;
    	me.selectedRelatedObject.relatedObjectType = "protocol";
    	Ext.getCmp("btnAddSelectedProtocol").setDisabled(false);
    },

    removeRelatedObject: function(){
    	var me = this,
    		objectType = me.selectedRelatedObject.relatedObjectType,
    		objectTypeCap = (objectType == "protocol")?"Protocol":"Contract";
    	clog("About to remove related object. Rec:",me.selectedRelatedObject)
    	Ext.Msg.show({
 		   title:'Remove related '+objectType+'?',
 		   msg: 'Are you sure? (This will NOT delete the related '+objectType+' from Clara)',
 		   buttons: Ext.Msg.YESNOCANCEL,
 		   fn: function(buttonId){
 			   if (buttonId == 'yes'){
 				   Ext.Ajax
 					.request({
 						url : appContext+"/ajax/delete-related-object",
 						method : 'POST',
 						success : function() {
 							  Ext.getCmp('btnRemoveRelated'+objectTypeCap).setDisabled(true);
		    				  me.selectedRelatedObject = {};
		    				  var store = Ext.data.StoreManager.lookup('Clara.DetailDashboard.store.Related'+objectTypeCap+'s');
 		    				  if (objectType == "protocol") store.loadRelatedProtocols();
 		    				  else store.loadRelatedContracts();
 						},
 						params : {
 							objectId: claraInstance.id,
 							objectType:claraInstance.type,
 							relatedObjectType:objectType,
 							relatedObjectId: me.selectedRelatedObject.get("id"), 
 							userId:claraInstance.user.id
 							}
 					});
 			   }
 		   },
 		   animEl: 'elId',
 		   icon: Ext.MessageBox.QUESTION
 		});
    },
    
    onRelatedProtocolSelect: function(gp, rec, item){
    	var me = this;
    	clog("RelatedProtocol selected",rec);
    	me.selectedRelatedObject = rec;
    	me.selectedRelatedObject.relatedObjectType = "protocol";
    	Ext.getCmp("btnRemoveRelatedProtocol").setDisabled(false);
    },
    
    onRelatedContractSelect: function(gp, rec, item){
    	var me = this;
    	clog("RelatedContract selected",rec);
    	me.selectedRelatedObject = rec;
    	me.selectedRelatedObject.relatedObjectType = "contract";
    	Ext.getCmp("btnRemoveRelatedContract").setDisabled(false);
    },
    
    onLetterSelect: function(gp, rec, item){
    	var me = this;
    	clog("letter selected");
    	me.selectedLetter = rec;
    	Ext.getCmp("btnNewCorrectionLetter").setDisabled(!claraInstance.HasAnyPermissions(['ROLE_IRB_EXPEDITED_REVIEWER','ROLE_IRB_OFFICE','ROLE_IRB_PREREVIEW']));
    },
    
    createLetter: function(template){
    	clog("createLetter: "+template);
    	var me = this;
    	Ext.create("Clara.LetterBuilder.view.LetterBuilderWindow",{
    		templateId:template,
    		parentMessageId:(template == "IRB_CORRECTION_LETTER")?me.selectedLetter.get("id"):null,
    		onSuccess: function(){
    			me.reloadLetters();
    		}
    	}).show();
    },
    
    reloadLetters: function(){
    	var letterStore = Ext.data.StoreManager.lookup('Clara.DetailDashboard.store.Letters');
    	letterStore.load();
    	Ext.getCmp("btnNewCorrectionLetter").setDisabled(true);
    },
    
    onPrintHistory : function() {
		var me = this;
		Ext.ux.grid.Printer.title = "History";
		Ext.ux.grid.Printer.printAutomatically = false;
		Ext.ux.grid.Printer.print(me.getHistoryPanel());
	},
    
    onToggleGroupHistory: function(btn,pressed){
    	var historyStore = Ext.data.StoreManager.lookup('Clara.DetailDashboard.store.History');
    	if (pressed){
    		historyStore.group("parentFormId");
    		btn.setIconCls('icn-ui-check-box');
    	} else {
    		historyStore.clearGrouping();
    		btn.setIconCls('icn-ui-check-box-uncheck');
    	}
    },
    onToggleGroupLetters: function(btn,pressed){
    	var historyStore = Ext.data.StoreManager.lookup('Clara.DetailDashboard.store.Letters');
    	if (pressed){
    		historyStore.group("letterType");
    		btn.setIconCls('icn-ui-check-box');
    	} else {
    		historyStore.clearGrouping();
    		btn.setIconCls('icn-ui-check-box-uncheck');
    	}
    }
    
});