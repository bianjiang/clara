Ext.define('Clara.Documents.controller.Documents', {
	extend: 'Ext.app.Controller',

	selectedDocument: null,
	selectedDocuments:[],	// for downloading multiple docs.
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
	       { ref: 'changeTypeButton', selector: '#btnDocumentChangeType'},
	       { ref: 'deleteButton', selector: '#btnDocumentDelete'},
	       { ref: 'changeTypeButton', selector: '#btnDocumentChangeType'}
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
					//itemclick:function(g,rec){ me.onDocumentSelected(rec); },
					selectionchange: function(g,recs){
						if (recs.length == 1){
							me.onDocumentSelected(recs[0]);
						} else {
							me.onDocumentsSelected(recs);
						}
					},
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
				'#btnDocumentChangeType':{
					click: function(){
						var doc = me.selectedDocument;
	        			me.changeDocumentType(doc);
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
						//var doc = me.selectedDocument;
	        			me.downloadDocument();
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
		if (piwik_enabled()){
			_paq.push(['trackEvent', 'PRINT', 'Print window opened: Document List']);
		}
	},
	
	reviseDocument: function(doc, callbackFn){
		if (piwik_enabled()){
			_paq.push(['trackEvent', 'DOCUMENTS', 'Revise Document: Window Opened for '+doc.get("title")+' ('+doc.get("id")+')']);
		}
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
								if (piwik_enabled()){
									_paq.push(['trackEvent', 'DOCUMENTS', 'Renamed Document: '+text+' ('+doc.get("id")+')']);
								}
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
								if (piwik_enabled()){
									_paq.push(['trackEvent', 'DOCUMENTS', 'Deleted Document: '+doc.get("title")+' ('+doc.get("id")+')']);
								}
								me.onDocumentsUpdated();
								if (callbackFn) callbackFn();
							}
						});
					}
				},
			   icon: Ext.MessageBox.WARNING
			});
	},
	
	
	downloadDocument: function(rec){
		var me = this;
		var downloadMask = new Ext.LoadMask({
		    msg    : 'Please wait...',
		    target : Ext.getBody()
		});

		
		if (me.selectedDocuments.length > 0){
			var docIds = [];
			for (var i=0,l=me.selectedDocuments.length;i<l;i++){
				docIds.push(me.selectedDocuments[i].get("uploadedFileId"));
			}
			clog("DOWNLOAD MULTIPLE");
			downloadMask.show();
			var url = appContext + "/ajax/documents/download";
			Ext.Ajax.request({
				method:"POST",
				url:url,
				params:{
					docId: docIds
				},
				success: function(response,opts){
					
					var zipFile = Ext.decode(response.responseText);
					clog("SUCCESS resp",response,zipFile);
					location.href=zipFile.url;
					
					Ext.getBody().unmask();
				},
				failure: function(response,opts){
					alert("There was a problem downloading these files. Please try again later.");
					clog("FAIL resp",response);
					  Ext.getBody().unmask();
				},
				listeners:{
					requestcomplete: function(c,r,o,e){
						clog("RESPONSE: ",r);
						downloadMask.hide();
					},
					requestexception: function(c,r,o,e){
						clog("EXCEPTION: ",r);
						downloadMask.hide();
					}
				}
			});
		} else {
			var doc = (typeof rec == "undefined" || rec == null)?me.selectedDocument:rec;
			var url = fileserverURL + doc.get("path") +doc.get("hashid")+"."+doc.get("extension")+"?n="+encodeURIComponent(doc.get("documentname")).replace(/%20/g, "_");
			clog("Opening",url,doc);
			window.open( url, '');
		}
		
		
	},
	
	initDocumentPanel: function(p){
		var me = this;
		clog("initDocumentPanel");
		me.loadingMask.show();
		Ext.StoreMgr.lookup("Clara.Documents.store.DocumentTypes").loadDocumentTypes();
	},
	
	
	documentTypeExists: function(doctype){
		clog("Checking doc type exists.. ", doctype);

		var st = Ext.StoreMgr.lookup("Clara.Documents.store.Documents")
		clog("Store count: "+st.getCount());
		if (st.getCount() == 0) return false;
		else {
			var idx = st.findBy(function(rec){
				if (rec.get("category") === doctype) return true;
			});
			return (idx > -1)?true:false;
		}

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
	
	onDocumentsSelected: function(docs){
		// When multiple docs selected, only enable "download" button
		var me = this,
			canReadAllDocs = true;
		
		clog("Docs selected",docs);
		me.selectedDocuments = docs;
		
		for (var i=0, l=docs.length;i<l; i++){
			if (me.hasDocumentPermission(docs[i].get("category"), "canRead") == false) {
				canReadAllDocs = false;
			}
		}
		
		me.getDownloadDocumentButton().setDisabled(!canReadAllDocs);
		me.getViewVersionsButton().setDisabled(true);
		if (me.getDeleteButton()) me.getDeleteButton().setDisabled(true);
		if (me.getReviseButton()) me.getReviseButton().setDisabled(true);
		if (me.getRenameButton()) me.getRenameButton().setDisabled(true);
		if (me.getChangeStatusButton()) me.getChangeStatusButton().setDisabled(true);
	},
	
	onDocumentSelected: function(doc){
		var me = this;
		me.selectedDocument = doc;
		while (me.selectedDocuments.length) { me.selectedDocuments.pop(); }
		
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
