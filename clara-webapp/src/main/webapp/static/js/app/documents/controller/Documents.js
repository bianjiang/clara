Ext.define('Clara.Documents.controller.Documents', {
	extend: 'Ext.app.Controller',

	selectedDocument: null,
	parentFormIds: [],
	documentTypes: [],
	
	refs: [
	       { ref: 'documentPanel', selector: 'documentpanel'},
	       { ref: 'filterToolbar', selector: 'tbdocumentfilter'},
	       { ref: 'actionToolbar', selector: 'tbdocumentaction'},
	       { ref: 'downloadDocumentButton', selector: '#btnDocumentDownload'},
	       { ref: 'viewVersionsButton', selector:'#btnDocumentViewVersions'},
	       { ref: 'changeStatusButton', selector: '#btnDocumentChangeStatus'},
	       { ref: 'reviseButton', selector: '#btnDocumentRevise'},
	       { ref: 'printButton', selector: '#btnPrintDocumentList'},
	       { ref: 'renameButton', selector: '#btnDocumentRename'},
	       { ref: 'deleteButton', selector: '#btnDocumentDelete'},
	       { ref: 'changeStatusButton', selector: '#btnDocumentChangeStatus'}
	],
	
	init: function() {
		var me = this;

		// Start listening for controller events
		me.on("documentTypesLoaded", function(recs) {
			for (var i=0;i<recs.length;i++){
				// populate doctype array for lookup in UI
				me.documentTypes[recs[i].get("doctype")] = recs[i].get("desc");
			}
			
			me.loadingMask.setLoading("Loading documents..");
			Ext.StoreMgr.lookup("Clara.Documents.store.Documents").loadDocuments();
		});
		
		me.on("documentsLoaded", function(recs) {
			
			var filterStore = Ext.StoreMgr.lookup("FilteredDocumentTypeStore"),
				previousType = "",
				filterArray = [['','All Types']];
			
			// Setup parent id array for client-side version numbering (ugh)
    		Ext.Array.each(recs, function(rec){
    			
				if (me.parentFormIds[rec.get("formType")]){
					if (!me.parentFormIds[rec.get("formType")].hasValue(rec.get("parentFormId"))){
						me.parentFormIds[rec.get("formType")].push(rec.get("parentFormId"));
						me.parentFormIds[rec.get("formType")].sort();
					}
				} else {
					me.parentFormIds[rec.get("formType")] = [rec.get("parentFormId")];
				}
				
				if (previousType !== me.documentTypes[rec.get("category")]){
    				filterArray.push([
    				                  rec.get("category"),
    				                  me.documentTypes[rec.get("category")]
    				                  ]);
    				previousType = me.documentTypes[rec.get("category")];
    			}
      	    });
    		filterArray.sort();
    		filterStore.loadData(filterArray);
    		
    		me.getDocumentPanel().getView().refresh();
    		me.getActionToolbar().resetActions();
			me.loadingMask.hide();
		});
		
		// Start listening for events on views

		me.control({
				'documentpanel':{
					itemclick:function(g,rec){ me.onDocumentSelected(rec); },
					afterrender:function(p){
						me.initDocumentPanel(p);
					}
				},
				'#fldTBDocumentType':{
					select: function(cb, recs){
						var docStore = Ext.StoreMgr.lookup("Clara.Documents.store.Documents");
						docStore.clearFilter();
						docStore.filter("category", recs[0].get("id"));
					}
				},
				'#btnDocumentDelete':{
					click: function(){
						var doc = me.selectedDocument;
	        			me.deleteDocument(doc);
					}
				},
				'#btnDocumentRevise':{
					click: function(){
						var doc = me.selectedDocument;
	        			me.reviseDocument(doc);
					}
				},
				'#btnDocumentRename':{
					click: function(){
						var doc = me.selectedDocument;
	        			me.renameDocument(doc);
					}
				},
				'#btnDocumentChangeStatus':{
					click: function(){
						var doc = me.selectedDocument;
	        			me.changeDocumentStatus(doc);
					}
				},
				'#btnDocumentDownload':{
					click: function(){
						var doc = me.selectedDocument;
	        			me.downloadDocument(doc);
					}
				},
				'#btnDocumentViewVersions':{
					click: function(){
	        			Ext.create('Clara.Documents.view.VersionsWindow',{}).show();
					}
				},
				'#btnDocumentGroupByForm':{
					toggle: function(btn,pressed){
						var st = Ext.data.StoreManager.lookup('Clara.Documents.store.Documents');
						Ext.getCmp("columnSavedToForm1").setVisible(pressed);
						Ext.getCmp("columnSavedToForm2").setVisible(!pressed);
				    	if (pressed){
				    		btn.setIconCls('icn-category-group');
				    		st.group("parentFormId");
				    	} else {
				    		btn.setIconCls('icn-category');
				    		st.clearGrouping();
				    	}
					}
				},
				'#btnPrintDocumentList' : {
					click : function() {
						me.printDocumentList();
					}
				}
		});
	},

	loadingMask: new Ext.LoadMask(Ext.getBody(), {msg:"Loading document types..."}),
	
	onDocumentsUpdated: function(){
		clog("onDocumentsUpdated: Refreshing");
		Ext.StoreMgr.lookup("Clara.Documents.store.Documents").loadDocuments();
	},
	
	printDocumentList: function(){
		var me = this;
		Ext.ux.grid.Printer.title = "Document List";
		Ext.ux.grid.Printer.printAutomatically = false;
		Ext.ux.grid.Printer.print(me.getDocumentPanel());
	},
	
	reviseDocument: function(doc, callbackFn){
		Ext.create("Clara.Documents.view.UploadWindow", {doc:doc, callbackFn: callbackFn, title:'Revising "'+doc.get("title")+'"..'}).show();
	},
	
	renameDocument: function(doc, callbackFn){
		var me = this;
		clog("renameDocument: "+doc.get('title'),doc);
		Ext.Msg.prompt('Rename Document', 'New file name:', function(btn, text){
					if (btn == 'ok'){
						jQuery.ajax({
							url: appContext+"/ajax/"+claraInstance.type+"s/"+claraInstance.id+"/"+claraInstance.type+"-forms/"+(claraInstance.form.id ||0)+"/"+claraInstance.type+"-form-xml-datas/"+(claraInstance.form.xmlDataId ||0)+"/documents/"+doc.get("id")+"/rename",
							type: "POST",
							async: false,
							data: {
								"userId": claraInstance.user.id,
								"title":text
							},
							success: function(data){
								me.onDocumentsUpdated();
								if (callbackFn) callbackFn();
							}
						});
					}
				},this,false,doc.get('title')
			);
	},
	
	deleteDocument: function(doc, callbackFn){
		var me = this;
		clog("deleteDocument: "+doc.get('title'),doc);
		Ext.Msg.show({
			   title:'Delete Document',
			   msg: 'Are you sure you want to delete "'+doc.get('title')+'"?',
			   buttons: Ext.Msg.YESNO,
			   fn: function(btn){
					if (btn == 'yes'){
						jQuery.ajax({
							url: appContext+"/ajax/"+claraInstance.type+"s/"+claraInstance.id+"/"+claraInstance.type+"-forms/"+claraInstance.form.id+"/"+claraInstance.type+"-form-xml-datas/"+claraInstance.form.xmlDataId+"/documents/"+doc.get("id")+"/delete",
							type: "POST",
							async: false,
							data: {
								"userId": claraInstance.user.id,
								"committee":claraInstance.user.committee
							},
							success: function(data){
								me.onDocumentsUpdated();
								if (callbackFn) callbackFn();
							}
						});
					}
				},
			   icon: Ext.MessageBox.WARNING
			});
	},
	
	
	downloadDocument: function(doc){
		var url = fileserverURL + doc.get("path") +doc.get("hashid")+"."+doc.get("extension")+"?n="+encodeURIComponent(doc.get("documentname")).replace(/%20/g, "_");
		clog("Opening",url,doc);
		window.open( url, '');
	},
	
	initDocumentPanel: function(p){
		var me = this;
		clog("initDocumentPanel");
		me.loadingMask.show();
		Ext.StoreMgr.lookup("Clara.Documents.store.DocumentTypes").loadDocumentTypes();
	},
	
	hasDocumentPermission: function(category, permission){
		var me = this,
			logString = "hasDocumentPermission: Check if "+permission+" "+category;

		permission = permission || "canWrite";
		
		var record = Ext.StoreMgr.lookup("Clara.Documents.store.DocumentTypes").query("doctype",category).first();
		
		if (typeof record == 'undefined' || !record) {
			cdebug("hasDocumentPermission: No record found.");
			clog(logString+": FALSE");
			return false;
		}

		if (record) {
			clog(logString+": "+((record.get(permission) == "true")?"TRUE":"FALSE"));
			return (record.get(permission) == "true");
		} else {
			clog(logString+": FALSE");
			return false;
		}
	},
	
	onDocumentSelected: function(doc){
		var me = this;
		me.selectedDocument = doc;
		clog("Document selected:",doc);
		
		// Enable appropriate buttons
		
		if (me.hasDocumentPermission(doc.get("category"), "canRead")) {
			me.getDownloadDocumentButton().setDisabled(false);
			if (doc.get("id") == doc.get("parentid") || doc.get("parentid") == 0){
				me.getViewVersionsButton().setDisabled(true);
			} else {
				me.getViewVersionsButton().setDisabled(false);
			}
		}
		else {
			me.getDownloadDocumentButton().setDisabled(true);
			me.getViewVersionsButton().setDisabled(true);
		}
		
		if ((me.getDocumentPanel().formView == true || claraInstance.type == 'contract') // Redmine #2843: Allow contract users to delete anytime.
				&& me.hasDocumentPermission(doc.get("category"), "canWrite")
			    && ( doc.get("status") != "RSC_APPROVED" && doc.get("status") != "APPROVED" )
		) {
			if (me.getDeleteButton()) me.getDeleteButton().setDisabled(false);			
		} else {
			if (me.getDeleteButton()) me.getDeleteButton().setDisabled(true);
		}
		
		var canUpdateDoc = me.hasDocumentPermission(doc.get("category"), "canUpdate");
		if (me.getReviseButton()) me.getReviseButton().setDisabled(!canUpdateDoc);
		if (me.getRenameButton()) me.getRenameButton().setDisabled(!canUpdateDoc);
		
		if (me.getChangeStatusButton()) me.getChangeStatusButton().setVisible(me.getDocumentPanel().formView == true && claraInstance.HasAnyPermissions(['ROLE_IRB_OFFICE','ROLE_SYSTEM_ADMIN','ROLE_BUDGET_REVIEWER']));
		
	}
	
});
