Ext.ns('Clara.Documents');

Clara.Documents.Document = function(o){
	this.id=											(o.id || '');	
	this.hashid=										(o.hashid || '');
	this.title=											(o.title || '');
	this.category=										(o.category || '');
	this.status=										(o.status || '');
	this.filename=										(o.filename || '');
	this.createdDate=									(o.createdDate || '');
	this.extension=										(o.extension || '');
	this.path=											(o.path || '');
	this.parentFormXmlDataDocumentId=					(o.parentFormXmlDataDocumentId || 0);
};

Clara.Documents.MessageBus = new Ext.util.Observable();
Clara.Documents.MessageBus.addEvents('documenttypesloaded','documentsloaded','filterselected','fileselected','fileremoved','fileuploaded','filemetadatasaved','filerenamed');

Clara.Documents.disableFileActions = function(){
	Ext.getCmp("btn-clara-document-detail-version").setDisabled(true);
	Ext.getCmp("btn-clara-document-download").setDisabled(true);
	Ext.getCmp("btn-clara-document-remove").setDisabled(true);
	Ext.getCmp("btn-clara-document-revise").setDisabled(true);
	Ext.getCmp("btn-clara-document-rename").setDisabled(true);
};

Clara.Documents.DocumentTypes = [];
Clara.Documents.DocumentTypeStore = new Ext.data.XmlStore({
		autoLoad:false,
		proxy: new Ext.data.HttpProxy({
			url: appContext+"/",
			method:"GET",
			headers:{'Accept':'application/xml;charset=UTF-8'}
		}),
		
		hasMultiSort:true,
		multiSortInfo: {
				sorters:[{
					field: 'sortOrder',
					direction:'ASC'
				},{
					field: 'category',
					direction:'ASC'
				},{
					field: 'desc',
					direction:'ASC'
				}
			],
			direction:'ASC'
		},
		record:"document-type",
		fields:[{name:'doctype', mapping:'@value'},
		        {name:'category', mapping:'@category'},
		        {name:'desc', mapping:'@desc'},
		        {name:'descCls', mapping:'@descCls'},
		        {name:'canRead', mapping:'@read'},
		        {name:'canWrite', mapping:'@write'},
		        {name:'canUpdate', mapping:'@update'},
		        {name:'protocol', mapping:'@protocol'},
		        {name:'contract', mapping:'@contract'}, 
		        {name:'sortOrder', convert:function(v,node){
		        	var sort = (typeof jQuery(node).attr("default-sort") != "undefined")?parseInt(jQuery(node).attr("default-sort")):0;
		        	jQuery(node).find('filters').children().each(function(){
		        		var c = jQuery(this).attr("committee");
		        		var s = jQuery(this).attr("status");
		        		if ((c == "*" || c == claraInstance.user.committee) && (s == "*" || s == claraInstance.status)){
		        			sort = (typeof jQuery(this).attr("sort-value") != "undefined")?parseInt(jQuery(this).attr("sort-value")):sort;
		        		}
		        	});
		        	return sort;
		        }},
		    
		        {name:'filters',
		        	convert : function(v, node) {	
					// HACK to remove duplicate records caused by ext's XML reader
		        		var reader = new Ext.data.XmlReader(
		        				{
		        					record : 'filter',
		        					fields : [
		        							{
		        								name : 'committee',
		        								mapping : '@committee'
		        							},{
		        								name : 'status',
		        								mapping : '@status'
		        							},{
		        								name : 'visible',
		        								mapping : '@visible',
		        								type:'boolean'
		        							},{
		        								name : 'sortValue',
		        								mapping : '@sort-value'
		        							}]
		        				});
					return reader.readRecords(node).records;
					
				}}
		        ],
		listeners:{
			beforeload: function(st,opts){
				url = appContext+"/ajax/"+claraInstance.type+"s/"+claraInstance.id+"/"+claraInstance.type+"-forms/"+(claraInstance.form.id || 0)+"/list-doc-types";

				clog("beforeload: setting proxyurl to "+url);

				st.proxy.setUrl(url);
				
				
				opts.params = {
					userId:claraInstance.user.id,
					committee:claraInstance.user.committee || null,
					docAction:getUrlVars()["docAction"] || null
				};
			},
			load:function(st,recs){
				for (var i=0;i<recs.length;i++){
					// populate doctype array for lookup in UI
					Clara.Documents.DocumentTypes[recs[i].get("doctype")] = recs[i].get("desc");
				}
				st.filterBy(function(rec,id){
					
					return (rec.get("canWrite") == "true" || rec.get("canUpdate") == "true") &&
					((claraInstance.type == "protocol")?(rec.get("protocol") == "true"):(rec.get("contract") == "true"));
				});
				clog ("DOC TYPES LOADED",Clara.Documents.DocumentTypes,st);
				Clara.Documents.MessageBus.fireEvent('documenttypesloaded', st);
				
			}
		}
	});

Clara.Documents.SendMetadata= function(doc){ 

		var url = appContext + "/ajax/"+claraInstance.type+"s/"+claraInstance.id+"/"+claraInstance.type+"-forms/" + claraInstance.form.id + "/"+claraInstance.type+"-form-xml-datas/" + claraInstance.form.xmlDataId + "/documents/add";
		jQuery.ajax({
			url: url,
			type: "POST",
			async: false,
			data: {
				"id": claraInstance.id,
				"formId": claraInstance.form.id,
				"formXmlDataId": claraInstance.form.xmlDataId,
				"userId": claraInstance.user.id,
				"committee":claraInstance.user.committee,
				"title": doc.title,
				"uploadedFileId": doc.id,
				"category": doc.category,
				"parentFormXmlDataDocumentId": (doc.parentFormXmlDataDocumentId || 0)
			},
			success: function(data){
				Clara.Documents.MessageBus.fireEvent('filemetadatasaved', data);
			}
		});
	
};

Clara.Documents.RenameWindow = Ext.extend(Ext.Window, {
	width:800,
	modal:true,
	id:'clara-documents-editwindow',
	layout:'form',
	autoHeight:true,
	closable:true,
	closeAction:'close',
	title:'Edit document',
	iconCls:'icn-blue-document--pencil',
	doc:{},
	bodyStyle: 'padding: 10px;',
	constructor:function(config){		
		Clara.Documents.RenameWindow.superclass.constructor.call(this, config);
	},	
	initComponent: function(){
		var t = this;
		var config = {
				listeners:{},
				items:[{
					xtype:'textfield',
					width:655,
					id:'fldFilename',
					value:t.doc.title,
					fieldLabel:'New file name',
					allowBlank:false
				}],
				buttons: [

							{
								text:'Save',
								id:'btnSaveDocument',
								disabled:false,
								handler: function(){
									
									if (Ext.getCmp("fldFilename").validate()){
										var url = appContext+"/ajax/"+claraInstance.type+"s/"+claraInstance.id+"/"+claraInstance.type+"-forms/"+claraInstance.form.id+"/"+claraInstance.type+"-form-xml-datas/"+claraInstance.form.xmlDataId+"/documents/"+t.doc.id+"/rename";
										Ext.Ajax.request({
											   url: url,
											   method:'POST',
											   success: function(response,opts){
												   Clara.Documents.MessageBus.fireEvent('filerenamed');
												   t.close();
											   },
											   failure: function(){
												   cwarn("Error renaming file.",response, opts);
												   alert("Error renaming file. Please try again in a few moments.");
											   },
											   params: { userId: claraInstance.user.id, title: Ext.getCmp("fldFilename").getValue()}
											});
										
									} else {
										alert("Please enter a file name.");
									}
									
								}
							},
							{
								text:'Cancel',
								disabled:false,
								handler: function(){
									t.close();
								}
							}]
		};
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		Clara.Documents.RenameWindow.superclass.initComponent.apply(this, arguments);		
	}
});
Ext.reg('claradocumentrenamewindow', Clara.Documents.RenameWindow);




Clara.Documents.StatusWindow = Ext.extend(Ext.Window, {
	width:400,
	modal:true,
	id:'clara-documents-statuswindow',
	autoHeight:true,
	closable:true,
	closeAction:'close',
	title:'Change status',
	iconCls:'icn-lightning',
	doc:{},
	constructor:function(config){		
		Clara.Documents.StatusWindow.superclass.constructor.call(this, config);
	},	
	initComponent: function(){
		var t = this;
		clog("CHANGE STATUS WINDOW: DOC",t.doc.status);
		var config = {

				items:[{
					xtype:'form',
					id:'clara-documents-statuswindow-form',
					fileUpload: true,
					labelWidth: 100,
					border:false,
					frame: false,
			    	autoHeight:true,
			    	bodyStyle: 'padding: 10px;',
			    	items:[
			    	      {
					        	xtype:'combo',
				    	   		width:255,
				    	      	fieldLabel:"Status",
				    	      	typeAhead:false,
				    	      	store: new Ext.data.SimpleStore({
				                       fields:['statustext','id'],
				                       data: [['Draft','DRAFT'],/*['ACKNOWLEDGED','Acknowledged'],['DECLINED','Declined'],['DETERMINED','Determined'],*/
				                              ['RSC Approved','RSC_APPROVED'],['IRB Approved','APPROVED'],['Retired','RETIRED'], ['HC Approved','HC_APPROVED']]
				                    }),
					    		value:(typeof t.doc.id != 'undefined')?(t.doc.status):"",
					    	   	forceSelection:true,
					    	   	displayField:'statustext', 
					    	   	valueField:'id', 
					    	   	mode:'local', 
					    	   	triggerAction:'all',
					    	   	editable:false,
					    	   	allowBlank:false,
					    	   	lazyRender: true,
					    	    selectOnFocus:true,
					    	   	id:'fldDocumentStatus'
				    	   }

			    	]    
				}],
				buttons: [

							{
								scope:this,
					            text: 'Save',
					            id:'btn-clara-documents-statuswindow-save',
					            handler: function(){
					            	if (Ext.getCmp("fldDocumentStatus").validate()){
										var url = appContext+"/ajax/"+claraInstance.type+"s/"+claraInstance.id+"/"+claraInstance.type+"-forms/"+claraInstance.form.id+"/"+claraInstance.type+"-form-xml-datas/"+claraInstance.form.xmlDataId+"/documents/"+t.doc.id+"/update-status";
										Ext.Ajax.request({
											   url: url,
											   method:'POST',
											   success: function(response,opts){
												   Clara.Documents.MessageBus.fireEvent('filerenamed');
												   t.close();
											   },
											   failure: function(){
												   cwarn("Error changing status.",response, opts);
												   alert("Error changing status. Please try again in a few moments.");
											   },
											   params: { userId: claraInstance.user.id, status: Ext.getCmp("fldDocumentStatus").getValue()}
											});
										
									} else {
										alert("Please choose a status.");
									}
					            }
					        },							{
								text:'Cancel',
								disabled:false,
								handler: function(){
									t.close();
								}
							}]
		};
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		Clara.Documents.StatusWindow.superclass.initComponent.apply(this, arguments);		
	}
});
Ext.reg('claradocumentstatuswindow', Clara.Documents.StatusWindow);


Clara.Documents.UploadWindow = Ext.extend(Ext.Window, {
	width:800,
	modal:true,
	id:'clara-documents-uploadwindow',
	autoHeight:true,
	closable:true,
	closeAction:'close',
	title:'Upload document',
	iconCls:'icn-drive-upload',
	doc:{},
	constructor:function(config){		
		Clara.Documents.UploadWindow.superclass.constructor.call(this, config);
	},	
	initComponent: function(){
		var t = this;
		var config = {
				listeners:{
					show:function(w){
						if (!w.doc.id){
							clog("resetting file upload form");
							Ext.getCmp("clara-documents-uploadwindow-form").getForm().reset();
						}
					}
				},
				items:[{
					xtype:'form',
					id:'clara-documents-uploadwindow-form',
					fileUpload: true,
					labelWidth: 100,
					border:false,
					frame: false,
			    	autoHeight:true,
			    	bodyStyle: 'padding: 10px;',
			    	items:[
							{
					            xtype: 'fileuploadfield',
					            id: 'clara-documents-uploadwindow-file',
					            emptyText: 'No file selected.',
					            width:655,
					            fieldLabel: 'File',
					            name: 'file',
					            buttonText: 'Choose file..',

					            listeners: {
					                'fileselected': function(fb, v){
										if (typeof Ext.getCmp('clara-documents-uploadwindow').doc.id != 'undefined') {
											Ext.getCmp('btn-clara-documents-uploadwindow-save').setDisabled(false);
										} else {
											Ext.getCmp("clara-documents-uploadwindow-details-type").setDisabled(false);
											Ext.getCmp('btn-clara-documents-uploadwindow-save').setDisabled(true);
										}
					                }
					            }
					        },
			    	      {
					        	xtype:'combo',
				    	   		width:655,
				    	      	fieldLabel:"Document type",
				    	      	disabled:true,
				    	      	typeAhead:false,
					    	   	store:Clara.Documents.DocumentTypeStore, 
					    		value:(typeof t.doc.id != 'undefined')?(t.doc.category):"",
					    	   	forceSelection:true,
					    	   	displayField:'desc', 
					    	   	tpl:new Ext.XTemplate(
	                            		  '<tpl for="."><div class="x-combo-list-item"><span class="documentcategory">{category}:</span> <span class="{descCls}">{desc}</span></div></tpl>'
	                                    ),
					    	   	valueField:'doctype', 
					    	   	mode:'remote', 
					    	   	triggerAction:'all',
					    	   	editable:false,
					    	   	allowBlank:false,
					    	   	id:'clara-documents-uploadwindow-details-type',
					    	   	listeners:{
					    	   		'select':function(c, record, index){
					    	   				if (index<0) {
					    	   					Ext.getCmp('btn-clara-documents-uploadwindow-save').setDisabled(true);
					    	   				} else {
					    	   					Ext.getCmp('btn-clara-documents-uploadwindow-save').setDisabled(false);
					    	   				}
					    	   			
					       			}
					       		}
				    	   },
							{	xtype:'textfield',
							    fieldLabel: 'Document <span style="font-weight:800;">name</span>',
							    id: 'clara-documents-uploadwindow-details-title',
							    value:(typeof t.doc.id != 'undefined')?(t.doc.title):"",
							    width:655
							},
							{
								xtype:'label',
								html:'<div style="font-weight:100;color:red;padding-left:104px;"><h1 style="font-size:12px;color:#666;">The Document Name must include the title, version number, and date (listed on the document).</h1>This is the information that will be included in IRB letters when referencing this document.</div>'
							},
							{	xtype:'hidden',
							    id: 'clara-documents-uploadwindow-details-parentid',
							    value:(typeof t.doc.parentFormXmlDataDocumentId != 'undefined')?(t.doc.parentFormXmlDataDocumentId):0,
							    height:0
							}
			    	       
			    	]    
				}],
				buttons: [

							{
								scope:this,
					            text: 'Upload',
					            id:'btn-clara-documents-uploadwindow-save',
					            handler: function(){
									var fp = Ext.getCmp("clara-documents-uploadwindow-form");
					                if(fp.getForm().isValid()){
					                	fp.getForm().submit({
					                		headers : {
					                			"Accept":"text/html"
					                		},
						                    url: appContext + '/fileserver/fileUpload',
						                    waitMsg: 'Uploading your document...',
						                    success: function(request, response){
					                			clog("success:");
					                			clog(response);
					                		// For some reason, the response shows up as a failure...
					                	   },
					                	   // the failure method will handle the json response from MyFormResult controller
					                	   failure: function(request, response){
					                			clog("failure:");
					                			clog(response.result);
					                			   var fileObj = response.result;
					                			   if (typeof fileObj == 'undefined' || fileObj == null || fileObj == false){
					                				   Ext.Msg.alert('Error', 'There was a problem uploading the file. Please try again later.');
					                				   cwarn('There was a problem uploading the file. Please try again later.',response);
					                			   } else {
					                				    var d = Ext.getCmp('clara-documents-uploadwindow').doc;
					                				    d.id = fileObj.id;
					                				    d.hashid = fileObj.identifier;
					                				    d.title = jQuery("#clara-documents-uploadwindow-details-title").val();
					                				    // d.category = jQuery("#clara-documents-uploadwindow-details-type").val(); // to get machine-readable type, use Ext.getCmp("clara-documents-uploadwindow-details-type").getValue();
					                				    d.category = Ext.getCmp("clara-documents-uploadwindow-details-type").getValue();
					                				    d.parentFormXmlDataDocumentId = jQuery("#clara-documents-uploadwindow-details-parentid").val();
					                				    clog("SENDING METATDATA FOR DOC:");
					                				    clog(d);
					                				    clog(Ext.getCmp('clara-documents-gridpanel').formXmlData);
					                				    Clara.Documents.SendMetadata(d);
					           							t.close();
					                			   }
					                		   
					                	   }
						                });
					                }
					            }
					        },							{
								text:'Cancel',
								disabled:false,
								handler: function(){
									t.close();
								}
							}]
		};
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		Clara.Documents.UploadWindow.superclass.initComponent.apply(this, arguments);		
	}
});
Ext.reg('claradocumentuploadwindow', Clara.Documents.UploadWindow);

Clara.Documents.VersionWindow = Ext.extend(Ext.Window, {
	id: 'clara-documents-version-window',
	parentdoc:{},
	parentpanelid:'',
	parentdocid:0,
	selecteddoc:{},
	title:'Document Versions',
	modal:true,
    closable:true,
    width:770,
    height:400,
    layout: 'fit',
	constructor:function(config){		
		Clara.Documents.VersionWindow.superclass.constructor.call(this, config);
	},	
	showRenameWindowForSelectedVersion:function(){
		new Clara.Documents.RenameWindow({doc:Ext.getCmp("clara-documents-version-window").selecteddoc}).show();
	},
	showStatusWindowForSelectedVersion:function(){
		new Clara.Documents.StatusWindow({doc:Ext.getCmp("clara-documents-version-window").selecteddoc}).show();
	},
	loadDocuments: function(){
		Ext.getCmp("clara-documents-version-gridpanel").getStore().removeAll();
		Ext.getCmp("clara-documents-version-gridpanel").getStore().load();
	},
	initComponent: function(){	
		var t = this;

		if (t.parentdocid > 0){
			t.parentdoc = {parentFormXmlDataDocumentId:t.parentdocid};
		}
		
		var versionUrl = appContext + "/ajax/"+claraInstance.type+"s/"+claraInstance.id+"/"+claraInstance.type+"-forms/"+claraInstance.form.id+"/"+claraInstance.type+"-form-xml-datas/"+claraInstance.form.xmlDataId + "/documents/"+Ext.getCmp("clara-documents-version-window").parentdoc.parentFormXmlDataDocumentId+"/list-versions";
		var parentMappingName = (claraInstance.type == "protocol")?"parentProtocolFileId":"parentContractFileId";
		
		Clara.Documents.MessageBus.addListener('fileremoved', function(data){
			clog("Clara.Documents.VersionWindow: fileremoved: REFRESHING GRIDPANEL");
			t.loadDocuments();
		});
		
		Clara.Documents.MessageBus.addListener('filerenamed', function(data){
			clog("Clara.Documents.VersionWindow: fileremoved: REFRESHING GRIDPANEL");
			t.loadDocuments();
		});
		
		var config = {
				buttons: [
				  		{
				  			text:'Download version',
				  			id:'btn-clara-documents-version-download',
				  			disabled:true,
				  			handler: function(){
				  				var doc = Ext.getCmp("clara-documents-version-window").selecteddoc;
				  				window.open( fileserverURL + doc.path +doc.hashid+"."+doc.extension+"?n="+encodeURI(doc.filename).replace(/%20/g, "_"), '');
				  			}
				  		},{
				  			text:'Close',
				  			disabled:false,
				  			handler: function(){
				  				Ext.getCmp("clara-documents-version-window").close();
				  			}
				  		}],
				items:[{
					xtype:'grid',
					id:'clara-documents-version-gridpanel',
			    	frame:false,
			    	border:false,
			    	trackMouseOver:false,
			        store: new Ext.data.JsonStore({
			    		proxy: new Ext.data.HttpProxy({
			    			url: versionUrl,
			    			method:"GET",
			    			headers:{'Accept':'application/json;charset=UTF-8'}
			    		}),
			    		autoLoad:true,
			    		idProperty: 'id',
			    		fields: [
			    		         {name:'id', mapping:'id'},
			    		         {name:'hashid', mapping:'uploadedFile.identifier'},
			    		         {name:'extension'},
			    		         {name:'path',mapping:'uploadedFile.path'},
			    		         {name:'documentname', mapping:'uploadedFile.filename'},
			    		         {name:'version', mapping:'versionId'},
			    		         {name:'category'},
			    		         {name:'status'},
			    		         {name:'title'},
			    		         {name:'parentid', mapping:parentMappingName},
			    		         {name:'created', mapping:'createdDate', type: 'date', dateFormat: 'm/d/Y H:i:s'},
			    		         {name:'extension', mapping:'uploadedFile.extension'}
			    		         ]
			    	}),
			        sm: new Ext.grid.RowSelectionModel({singleSelect: true}),
			        loadMask: new Ext.LoadMask(Ext.getBody(), {msg:"Please wait..."}),
			        height:250,
			    	viewConfig: {
			    		forceFit:true,
			    		getRowClass: function(record, index){
			    			if (index <= 0) { return 'doc-history-newest-version'; }
			    			else { return 'doc-history-older-version'; }
			    		}
			    	},
				    listeners:{
						scope:this,
					    rowclick: function(grid, rowI, event)   {
							var docdata = grid.getStore().getAt(rowI).data;

							var doc = new Clara.Documents.Document({
								id:docdata.id,
								hashid:docdata.hashid,
								path:docdata.path,
								title:docdata.title,
								status:docdata.status,
								category:docdata.category,
								filename:docdata.documentname,
								createdDate:docdata.createdDate,
								extension:docdata.extension,
								parentFormXmlDataDocumentId:docdata.parentid
							});
							
							t.selecteddoc = doc;
							Ext.getCmp("btn-clara-documents-version-download").enable();
							
					    }
				    },
			        columns: [{
			            	  	header:'Status',
			              	  	dataIndex:'status',
			              	  renderer: function(v,p,r) { 
				              		if (r.get("status") == "DRAFT"){
			              	    		return "<div style='float:left;'><div class='icn-pencil-small' style='color:#999;background-repeat:no-repeat;background-position:left right;width:32px;height:24px;padding-left:15px;font-weight:100;'>Draft</div></div>";
			              	    	}
				              		else if (r.get("status") == "RETIRED"){
			              	    		return "<div style='float:left;'><div class='icn-box-small' style='color:#999;background-repeat:no-repeat;background-position:left right;width:32px;height:24px;padding-left:15px;font-weight:100;'>Retired</div></div>";
			              	    	}
				              		else if (r.get("status") == "APPROVED"){
			              	    		return "<div style='float:left;'><div class='icn-tick' style='color:green;background-repeat:no-repeat;background-position:left right;width:32px;height:24px;padding-left:15px;'>IRB</div></div>";
			              	    	}else if (r.get("status") == "RSC_APPROVED") {
			              	    		return "<div style='float:left;'><div class='icn-tick' style='color:green;background-repeat:no-repeat;background-position:left right;width:32px;height:24px;padding-left:15px;'>RSC</div></div>";
			              	    	}else if (r.get("status") == "HC_APPROVED") {
			              	    		return "<div style='float:left;'><div class='icn-tick' style='color:green;background-repeat:no-repeat;background-position:left right;width:32px;height:24px;padding-left:15px;'>HC</div></div>";
			              	    	}else {
			              	    		return "<div style='float:left;'><div class='icn-ui-check-box-uncheck-disabled' style='color:#999;background-repeat:no-repeat;background-position:left right;width:32px;height:24px;padding-left:15px;'></div></div>";
			              	    	}
			              	    },
			                  	sortable:true,
			                  	width:70
			                },
			                
		                  {
		                	  	header:'Document name',
		                	  	dataIndex:'title',
		                	  	sortable:false,
		                	  	width:220,
		                	  	renderer: function(v,p,r){
		                	  		return "<div class='document-row-info doc-history-row doc-history-row-filename'>"+v+"</div>";
		                	  	}
		                  },
			                  {
			                	  	header:'Category',
			                	  	dataIndex:'category',
			                	  	sortable:false,
			                	  	width:200,
			                	  	renderer: function(v,p,r){
			                	  		clog("rendering doc category: Clara.Documents.DocumentTypes["+v.toLowerCase()+"]");
			                	  		return "<div class='document-row-info doc-history-row doc-history-row-category wrap'>"+Clara.Documents.DocumentTypes[v.toLowerCase()]+"</div>";
			                	  	}
			                    },
				                {
			                	  	header:'Version',
			                	  	dataIndex:'version',
			                	  	sortable:false,
			                	  	width:70,
			                	  	renderer: function(v,p,r){
			                	  		return ""+(parseInt(v)+1);
			                	  	}
			                  },
			                    {
			                  	  	header:'Created',
			                  	  	dataIndex:'created',
			                  	    renderer: function(dt) { return dt.format("m/d/y g:iA"); },
				                  	sortable:false,
				                  	width:120
				                },

			                    {
			                  	  	header:'Action',
			                  	  	dataIndex:'id',
			                  	    renderer: function(v,p,r) { 
			                  	    	var actions = "<div class='wrap'>";
			                  	    	if (Clara.Documents.HasPermission(r.get("category"), "canUpdate") == true) {
			                  	    		actions += "<a href='javascript:Ext.getCmp(\"clara-documents-version-window\").showRenameWindowForSelectedVersion();' style='margin-right:6px;'><strong>Rename</strong></a> ";
			                  	    	}
			                  	    	
			                  	    	if (Ext.getCmp("clara-documents-gridpanel").readOnly == false && claraInstance.HasAnyPermissions(['ROLE_IRB_OFFICE','ROLE_SYSTEM_ADMIN'])) {
			                  	    		actions += "<a href='javascript:Ext.getCmp(\"clara-documents-version-window\").showStatusWindowForSelectedVersion();'>Status</a> ";
			                  	    	}
			                  	    	
			                  	    	if (Clara.Documents.HasPermission(r.get("category"), "canWrite") == true 
			                  	    			&& ( r.get("status") != "RSC_APPROVED" && r.get("status") != "APPROVED" ) ) {
			                  	    		actions += "<a href='javascript:Clara.Documents.RemoveDocument(\""+v+"\");'>Delete</a>";
			                  	    	}
			                  	    	actions += "</div>";
			                  	    	return actions;
			                  	    },
				                  	sortable:false,
				                  	width:180
				                }
				        ]
				    }]
		};
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		Clara.Documents.VersionWindow.superclass.initComponent.apply(this, arguments);
	}
});
Ext.reg('claradocumentversionwindow', Clara.Documents.VersionWindow);

Clara.Documents.HasPermission = function(category, permission){
	clog("Clara.Documents.HasPermission: ",category,permission);
	permission = permission || "canWrite";
	
	var record = Clara.Documents.DocumentTypeStore.query("doctype",category).first();
	clog("Clara.Documents.HasPermission: Record ",record);
	if (typeof record == 'undefined' || !record) return false;
	
	
	if (record) {
		return (record.get(permission) == "true");
	} else {
		return false;
	}
};

Clara.Documents.DetailBar = Ext.extend(Ext.Toolbar, {
	readOnly:false,
	constructor:function(config){		
		Clara.Documents.DetailBar.superclass.constructor.call(this, config);
	},
	initComponent: function(){
		var t = this;
		Clara.Documents.MessageBus.addListener('fileselected', function(doc){
clog("DOC OBJ",doc);
			if (Clara.Documents.HasPermission(doc.category, "canRead")) {
				Ext.getCmp("btn-clara-document-download").setDisabled(false);
				if (doc.id == doc.parentFormXmlDataDocumentId || doc.parentFormXmlDataDocumentId == 0){
					clog("disable version");
					Ext.getCmp("btn-clara-document-detail-version").setDisabled(true);
				} else {
					clog("enable version");
					Ext.getCmp("btn-clara-document-detail-version").setDisabled(false);
				}
			}
			else {
				Ext.getCmp("btn-clara-document-download").setDisabled(true);
				Ext.getCmp("btn-clara-document-detail-version").setDisabled(true);
			}
			
			if ((Ext.getCmp("clara-documents-gridpanel").readOnly == false || claraInstance.type == 'contract') // REdmine #2843: Allow contract users to delete anytime.
					&& Clara.Documents.HasPermission(doc.category, "canWrite")
				    && ( doc.status != "RSC_APPROVED" && doc.status != "APPROVED" )
			) {
				Ext.getCmp("btn-clara-document-remove").setDisabled(false);			
			} else {
				Ext.getCmp("btn-clara-document-remove").setDisabled(true);
			}
			
			if (Clara.Documents.HasPermission(doc.category, "canUpdate")){
				Ext.getCmp("btn-clara-document-revise").setDisabled(false);
				Ext.getCmp("btn-clara-document-rename").setDisabled(false);
			}
			else{
				Ext.getCmp("btn-clara-document-revise").setDisabled(true);
				Ext.getCmp("btn-clara-document-rename").setDisabled(true);
			}
			
			
			Ext.getCmp("btn-clara-document-changestatus").setVisible(Ext.getCmp("clara-documents-gridpanel").readOnly == false && claraInstance.HasAnyPermissions(['ROLE_IRB_OFFICE','ROLE_SYSTEM_ADMIN']));
			

		});
		Clara.Documents.MessageBus.addListener('filterselected', function(doc){
			Clara.Documents.disableFileActions();
		});
		Clara.Documents.MessageBus.addListener('filerenamed', function(doc){
			Clara.Documents.disableFileActions();
		});
		
		var config = {
				items:[{
			    	text: '<span style="font-weight:800;font-size:12px;">Upload New Document</span>',
			    	disabled:t.readOnly,
			    	iconCls:'icn-navigation-090-button',iconAlign:'right',
			    	handler: function(){
			    		cdebug("new doc");
						new Clara.Documents.UploadWindow({doc:{}}).show();
			    	}
		    	},'->',	
			                        {
			                            xtype: 'button',
			                            text: 'Open / Download',
			                            disabled:true,
			                            id:'btn-clara-document-download',
			                            iconCls:'icn-drive-download',
			                            handler: function(){
		                        			var doc = Ext.getCmp("clara-documents-gridpanel").selectedDocument;
		                        			var url = fileserverURL + doc.path +doc.hashid+"."+doc.extension+"?n="+encodeURIComponent(doc.filename).replace(/%20/g, "_");
		                        			clog("Opening",url,doc);
		                        			window.open( url, '');
		                        		}
			                        },
			                        {
			                            xtype: 'button',
			                            text: 'Change Status',
			                            hidden:true,
			                            id:'btn-clara-document-changestatus',
			                            iconCls:'icn-lightning',
			                            handler: function(){
			                            	var doc = Ext.getCmp("clara-documents-gridpanel").selectedDocument;
		                        			new Clara.Documents.StatusWindow({doc:doc}).show();
		                        		}
			                        },
			                        {
			                            xtype: 'button',
			                            text: 'Versions',
			                            disabled:true,
			                            iconCls:'icn-folder-clock',
			                            id:'btn-clara-document-detail-version',
			                            handler: function(){
		                        			var doc = Ext.getCmp("clara-documents-gridpanel").selectedDocument;
		                        			new Clara.Documents.VersionWindow({parentdoc:doc}).show();
		                        		}
			                        },
			                        {
			                            xtype: 'button',
			                            text: 'Revise',
			                            disabled:true,
			                            id:'btn-clara-document-revise',
			                            iconCls:'icn-document-import',
			                            handler: function(){
		                        			var doc = Ext.getCmp("clara-documents-gridpanel").selectedDocument;
		                        			new Clara.Documents.UploadWindow({doc:doc, title:"Updating '"+doc.title+"'..."}).show();
		                        		}
			                        },
			                        
			                        {
			                            xtype: 'button',
			                            text: 'Rename',
			                            iconCls:'icn-ui-text-field-select',
			                            id:'btn-clara-document-rename',
			                            disabled:true,
			                            handler: function(){
			                            	var buttn = this;
		                        			var doc = Ext.getCmp("clara-documents-gridpanel").selectedDocument;
		                        			var theDocID = doc.id;
		                        			new Clara.Documents.RenameWindow({doc:doc, title:"Renaming '"+doc.title+"'..."}).show();
		                        		}
			                        },{
			                            xtype: 'button',
			                            text: 'Delete',
			                            iconCls:'icn-minus-circle',
			                            id:'btn-clara-document-remove',
			                            disabled:true,
			                            handler: function(){
			                            	var buttn = this;
		                        			var doc = Ext.getCmp("clara-documents-gridpanel").selectedDocument;
		                        			var theDocID = doc.id;
		                        			Clara.Documents.RemoveDocument(theDocID,doc.title);
		                        		}
			                        },'-',{
			        		    		xtype:'button',
			        		    		iconCls:'icn-category-group',
			        		    		pressed:false,
			        					enableToggle:true,
			        					toggleHandler: function(btn,st){
			        						var gp = Ext.getCmp("clara-documents-gridpanel");
			        						if (st){
			        							btn.setIconClass("icn-category-group-select");
			        							gp.getStore().groupBy("parentFormId");
			        			    			gp.getView().expandAllGroups();
			        						} else {
			        							btn.setIconClass("icn-category-group");
			        							gp.getStore().clearGrouping();
			        						}
			        					},
			        		    		text:'Group by form'
			        		    	},'-',{
			        		    		xtype:'button',
			        		    		tooltip:'Print list (opens new window)',
			        		    		tooltipType:'title',
			        		    		iconCls:'icn-printer', 	
			        					handler: function(){
			        						var gp = Ext.getCmp("clara-documents-gridpanel");
			        						Ext.ux.Printer.print(gp,{title:'Documents'});
			        					}
			        		    	}
		                    
		                ]
		};
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		Clara.Documents.DetailBar.superclass.initComponent.apply(this, arguments);
		
		
		
	}
});
Ext.reg('claradocumentdetailbar', Clara.Documents.DetailBar);

Clara.Documents.RemoveDocument = function(did,dtitle){
	dtitle = dtitle || '';
	Ext.Msg.show({
		   title:'Delete Document',
		   msg: (dtitle != '')?'Are you sure you want to delete "'+dtitle+'"?':'Are you sure you want to delete this file?',
		   buttons: Ext.Msg.YESNO,
		   fn: function(btn){
				if (btn == 'yes'){
					clog("Call AJAX REMOVE and RELOAD DOCUMENT LIST");
					
					jQuery.ajax({
						url: appContext+"/ajax/"+claraInstance.type+"s/"+claraInstance.id+"/"+claraInstance.type+"-forms/"+claraInstance.form.id+"/"+claraInstance.type+"-form-xml-datas/"+claraInstance.form.xmlDataId+"/documents/"+did+"/delete",
						type: "POST",
						async: false,
						data: {
							"userId": claraInstance.user.id,
							"committee":claraInstance.user.committee
						},
						success: function(data){
							clog(data);
							Clara.Documents.MessageBus.fireEvent('fileremoved', data);
						}
					});
				}else{
					// just go away
				}
			},
		   icon: Ext.MessageBox.WARNING
		});
};


Clara.Documents.Panel = Ext.extend(Ext.Panel, {
	id: 'clara-documents-panel',
	allDocs:false,
	hideFormOnlyOption:false,
	readOnly:false,
	frame:false,
	height:350,
	layout:'border',
	border:true,
	helpHtml:null,
	constructor:function(config){		
		Clara.Documents.Panel.superclass.constructor.call(this, config);
	},	
	
	initComponent: function(){
		var t = this;
		
		clog("normalized doc t.formXmlData",t.formXmlData);
		var config = (t.helpHtml)?{
				items:[{xtype:'claradocumentfilterpanel',hideFormOnlyOption:t.hideFormOnlyOption},{
					xtype:'container',region:'north',style:'padding:6px;',html:t.helpHtml,autoHeight:true
				},{xtype:'claradocumentgridpanel', id:'clara-documents-gridpanel',readOnly:this.readOnly, allDocs:this.allDocs}]
		}:{
			items:[{xtype:'claradocumentfilterpanel',hideFormOnlyOption:t.hideFormOnlyOption},{xtype:'claradocumentgridpanel', id:'clara-documents-gridpanel', readOnly:this.readOnly, allDocs:this.allDocs}]
		};
		if (!claraInstance.id) cwarn("WARNING: Clara.Documents.Panel: No claraInstance.id defined.",claraInstance);
		
		config.listeners = {
				show: function(p){
					clog("SHOW");
					var g = Ext.getCmp("clara-documents-gridpanel");
					if (g.getStore().getCount() == 0){
						g.loadDocuments(function(){
							Ext.getCmp("clara-documents-gridpanel").getStore().clearGrouping();	// hack to force clientside form numbering.
						});
						
					}
				}
		};

		Ext.apply(this, Ext.apply(this.initialConfig, config));
		Clara.Documents.Panel.superclass.initComponent.apply(this, arguments);

		// NOW load doc types (doc will load after 'documenttypesloaded' message is fired).
		Clara.Documents.DocumentTypeStore.load();
	}
});
Ext.reg('claradocumentpanel', Clara.Documents.Panel);



Clara.Documents.FilterPanel = Ext.extend(Ext.tree.TreePanel, {
	id: 'clara-documents-filterpanel',
	bodyStyle:'background-color:#dee8f7;border-right:1px solid #96baea;padding-top:8px;',
	cls:'clara-document-treepanel',
	useArrows:true,
	animate:false,
	enableDD:false,
	hideFormOnlyOption:false,
	containerScroll:true,
	region:'west',
	border:false,
	width:175,
	
	constructor:function(config){		
		Clara.Documents.FilterPanel.superclass.constructor.call(this, config);
	},	
	
	onDocumentsLoaded: function(s){
		var t = this;
		// Get the distinct document categories and committees
		var categories = [];
		var committees = [];
		clog(s);
		s.each(function(r){
			if (jQuery.inArray(r.get("category"), categories) == -1) {
				categories.push(r.get("category"));
			}
			if (jQuery.inArray(r.get("committee"), committees) == -1) {
				committees.push(r.get("committee"));
			}
		},this);
		
		categories.sort();
		committees.sort();
		
		if (this.rendered){
			var tp = this;
			var treeType = tp.getNodeById('tree-document-filter-bytype');
			var treeCommittee = tp.getNodeById('tree-document-filter-bycreator');
			
			treeType.removeAll();
			treeCommittee.removeAll();
			
			jQuery.each(categories, function(ind,value){
				clog("tree: value",value,"lookup",Clara.Documents.DocumentTypes[value]);
				treeType.appendChild({cls:'tree-document-filter-row',text: Clara.Documents.DocumentTypes[value],iconCls:'icn-blue-documents-stack',leaf: true,
					listeners:{
	        		click: function(){
	        			var s = Ext.getCmp("clara-documents-gridpanel").getStore();
	        			Clara.Documents.MessageBus.fireEvent('filterselected');
	        			s.filterBy(function(r,id){
	        				var compareCat = r.get("category");
	        				return ( value == compareCat);
	        			});
	        		}
	        	}});
			});
			
			jQuery.each(committees, function(ind,value){
				treeCommittee.appendChild({text: value,iconCls:'icn-user',leaf: true,
					listeners:{
	        		click: function(){
	        			var s = Ext.getCmp("clara-documents-gridpanel").getStore();     
	        			Clara.Documents.MessageBus.fireEvent('filterselected');
	        			s.filterBy(function(r,id){
	        				var compareComm = r.get("committee");
	        				return (value == compareComm);
	        			});
	        		}
	        	}});
			});
			
			tp.expandAll();
		}
		
	},
	
	initComponent: function(){
		var t = this;
		
		var children = [];
		
		children.push({
            text: 'All Documents',
            iconCls:'icn-application-documents',
            leaf: true,
            listeners:{
        		click: function(){
        			Clara.Documents.MessageBus.fireEvent('filterselected');
        			var s = Ext.getCmp("clara-documents-gridpanel").getStore();
        			s.clearFilter();
        			s.removeAll();
        			s.proxy.setUrl(appContext+"/ajax/"+claraInstance.type+"s/"+claraInstance.id+"/documents/list");
        			s.load();
        		}
        	}
    	});

		if (t.hideFormOnlyOption == false){
			children.push({
				text: '<span class="document-tree-root">Location</span>',
				id:'tree-document-filter-byform',
				iconCls:'no-icon',
				expanded: true,
				draggable:false,
				leaf: false,
				children: [{
					text: 'This form only',
					iconCls:'icn-blue-document-invoice',

					leaf: true,
					listeners:{
						click: function(){
							Clara.Documents.MessageBus.fireEvent('filterselected');
							var s = Ext.getCmp("clara-documents-gridpanel").getStore();
							s.clearFilter();
							s.removeAll();
							s.proxy.setUrl(appContext+"/ajax/"+claraInstance.type+"s/"+claraInstance.id+"/"+claraInstance.type+"-forms/" + claraInstance.form.id + "/documents/list-all");
							s.load();
						}
					}
				},{
					text: 'This revision only',
					iconCls:'icn-blue-document-snippet',

					leaf: true,
					listeners:{
						click: function(){
							Clara.Documents.MessageBus.fireEvent('filterselected');
							var s = Ext.getCmp("clara-documents-gridpanel").getStore();
							s.clearFilter();
							s.removeAll();
							s.proxy.setUrl(appContext+"/ajax/"+claraInstance.type+"s/"+claraInstance.id+"/"+claraInstance.type+"-forms/" + claraInstance.form.id + "/documents/list-all");
							s.load({
								callback:function(){
									var st = this;
									clog("FILTER: THIS REVSION ONLY");
									st.filterBy(function(rec){
										clog("filter rec",rec,rec.get("formId"),rec.get("parentFormId"));
										return (rec.get("formId") == claraInstance.form.id);
									});
								}
							});
						}
					}
				}
				]
			}); 

		}

		
		children.push({
	        text: '<span class="document-tree-root">Document Types</span>',
	        id:'tree-document-filter-bytype',
	        iconCls:'no-icon',
	        expanded: true,
	        draggable:false,
	        leaf: false,
	        children: []
	    }); 
		children.push({
	        text: '<span class="document-tree-root">Creators</span>',
	        iconCls:'no-icon',
	        id:'tree-document-filter-bycreator',
	        expanded: true,
	        hidden:true,
	        draggable:false,
	        leaf: false,
	        children: []
	    });
		children.push({
	        text: '<span class="document-tree-root">Search for</span>',
	        iconCls:'no-icon',
	        expanded: true,
	        hidden:true,
	        draggable:false,
	        leaf: false,
	        children: [{
	            text: 'Today',
	            iconCls:'icn-clock-select',
	            leaf: true,
	            listeners:{
	        		click: function(){
	        			var s = Ext.getCmp("clara-documents-gridpanel").getStore();
	        			var dt = new Date() - (1000 * 60 * 60 * 24 * 1);
	        			Clara.Documents.MessageBus.fireEvent('filterselected');
	        			s.filterBy(function(r,id){
	        				var compareDt = r.get("created");
	        				return (dt <= compareDt);
	        			});
	        		}
	        	}
	        }, {
	            text: 'This week',
	            iconCls:'icn-clock-select',
	            leaf: true,
	            listeners:{
	        		click: function(){
	        			var s = Ext.getCmp("clara-documents-gridpanel").getStore();
	        			var dt = new Date() - (1000 * 60 * 60 * 24 * 7);
	        			Clara.Documents.MessageBus.fireEvent('filterselected');
	        			s.filterBy(function(r,id){
	        				var compareDt = r.get("created");
	        				return (dt <= compareDt);
	        			});
	        		}
	        	}
	        }, {
	            text: 'Last two months',
	            iconCls:'icn-clock-select',
	            leaf: true,
	            listeners:{
	        		click: function(){
	        			var s = Ext.getCmp("clara-documents-gridpanel").getStore();
	        			var dt = new Date() - (1000 * 60 * 60 * 24 * 60);
	        			Clara.Documents.MessageBus.fireEvent('filterselected');
	        			s.filterBy(function(r,id){
	        				var compareDt = r.get("created");
	        				return (dt <= compareDt);
	        			});
	        		}
	        	}
	        }]
		});
		
		var config = {
				listeners:{
					"afterrender":function(t){t.onDocumentsLoaded(Ext.getCmp("clara-documents-gridpanel").getStore());}
				},
				loader: new Ext.tree.TreeLoader(),
				root: new Ext.tree.AsyncTreeNode({
					id:'tree-document-filter-root',
				    expanded: true,
				    children: children
				}),
				rootVisible: false
		};
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		Clara.Documents.FilterPanel.superclass.initComponent.apply(this, arguments);
		if(Clara.Documents.MessageBus){
			Clara.Documents.MessageBus.on('documentsloaded', this.onDocumentsLoaded, this);
		}
	}
});
Ext.reg('claradocumentfilterpanel', Clara.Documents.FilterPanel);



Clara.Documents.GridPanel = Ext.extend(Ext.grid.GridPanel, {
	id: 'clara-documents-gridpanel',
	frame:false,
	readOnly:false,
	allDocs:false,
	trackMouseOver:false,
	border:false,
	region:'center',
	selectedDocument:{},
	parentFormIds:[],
	constructor:function(config){		
		Clara.Documents.GridPanel.superclass.constructor.call(this, config);
	},	
	
	loadDocuments:function(callbackFn){
		clog("loadDocuments()");
		this.getStore().removeAll();
		if (callbackFn) this.getStore().load({ callback:callbackFn });
		else this.getStore().load();
	},

	initComponent: function() {
		var t = this;
		
		
		
		clog("All docs? "+this.allDocs);
		Clara.Documents.MessageBus.addListener('fileselected', function(doc){
			t.selectedDocument = doc;
		});
		
		Clara.Documents.MessageBus.addListener('documenttypesloaded', function(data){
			clog("Clara.Documents.GridPanel: documenttypesloaded: REFRESHING GRIDPANEL");
			if (t.isVisible()) {
				clog("Clara.Documents.GridPanel: IS VISIBLE, reloading");
				t.loadDocuments();
			}
		});
		
		Clara.Documents.MessageBus.addListener('filemetadatasaved', function(data){
			clog("Clara.Documents.GridPanel: filemetadatasaved: REFRESHING GRIDPANEL");
			t.loadDocuments();
		});
		
		Clara.Documents.MessageBus.addListener('filerenamed', function(data){
			clog("Clara.Documents.GridPanel: filemetadatasaved: REFRESHING GRIDPANEL");
			t.loadDocuments();
		});
		
		Clara.Documents.MessageBus.addListener('fileremoved', function(data){
			clog("Clara.Documents.GridPanel: fileremoved: REFRESHING GRIDPANEL");
			t.loadDocuments();
		});
				
		var config = {
				store:new Ext.data.GroupingStore({
					proxy: new Ext.data.HttpProxy({
						url: this.allDocs?(appContext+"/ajax/"+claraInstance.type+"s/"+claraInstance.id+"/documents/list"):(appContext+"/ajax/"+claraInstance.type+"s/"+claraInstance.id+"/"+claraInstance.type+"-forms/" + claraInstance.form.id + "/documents/list-all"),
						method:"GET",
						headers:{'Accept':'application/json;charset=UTF-8'}
					}),
					reader: new Ext.data.JsonReader({
						id: 'id',
						fields: [
						         {name:'id', mapping:'id'},
						         {name:'hashid', mapping:'uploadedFile.identifier'},
						         {name:'documentname', mapping:'uploadedFile.filename'},
						         {name:'category'},
						         {name:'title'},
						         {name:'versionId'},
						         {name:'status'},
						         {name:'path', mapping:'uploadedFile.path'},
						         {name:'parentid', mapping:(claraInstance.type == 'protocol')?'parentProtocolFormXmlDataDocumentId':'parentContractFormXmlDataDocumentId'},
						         {name:'parentFormId', mapping:(claraInstance.type == 'protocol')?'parentProtocolFormId':'parentContractFormId'},
						         {name:'committee', mapping:'committee'},
						         {name:'formId', mapping:(claraInstance.type == 'protocol')?'protocolFormId':'contractFormId'},
						         {name:'formType', mapping:(claraInstance.type == 'protocol')?'protocolFormType':'contractFormType'},
						         {name:'formTypeDesc', mapping:(claraInstance.type == 'protocol')?'protocolFormTypeDesc':'contractFormTypeDesc'},
						         {name:'formXmlDataId', mapping:(claraInstance.type == 'protocol')?'protocolFormXmlDataId':'contractFormXmlDataId'},
						         {name:'created', mapping:'createdDate', type: 'date', dateFormat: 'm/d/Y H:i:s'},
						         {name:'extension', mapping:'uploadedFile.extension'}
						         ]
					}),
					autoLoad:false,
					sortInfo: {field:'created', direction:'DESC'},
					listeners:{
						load:function(st,recs){
							// create 2d array of formtype/ids for relative id display
							
							st.each(function(rec){
							
							if (t.parentFormIds[rec.get("formType")]){
								if (!t.parentFormIds[rec.get("formType")].hasValue(rec.get("parentFormId"))){
									t.parentFormIds[rec.get("formType")].push(rec.get("parentFormId"));
									t.parentFormIds[rec.get("formType")].sort();
								}
							} else {
								t.parentFormIds[rec.get("formType")] = [rec.get("parentFormId")];
							}
							
							
							
                  	    	});

							
							clog("store load. t.parentFormIds",t.parentFormIds);
							clog("loaded. firing event..");
							Clara.Documents.MessageBus.fireEvent('documentsloaded', t.getStore());
						}
					}
				}),
				view: new Ext.grid.GroupingView({
		    		forceFit:true,
		    		emptyText:'There are no documents',
		    		startCollapsed : true,
		    		groupTextTpl:'{[ values.rs[0].data["formTypeDesc"] ]} ({[values.rs.length]} {[values.rs.length > 1 ? "documents" : "document"]})',
		    		getRowClass: function(record, index){
		    			var rscApprovedStatus = (record.get('category') == "protocol" || record.get('category').indexOf("consent") > -1)?"document-row-highlight-status-"+record.get("status"):"document-row-status-"+record.get("status");
		    			var thisFormCls = (claraInstance.form && claraInstance.form.id && record.get('formId') == claraInstance.form.id)?" document-row-thisform":"";
		    			return 'document-row document-row-'+record.get('category').replace(" ","-")+thisFormCls+" "+rscApprovedStatus;
		    		}

		    	}),
				tbar: {xtype:'claradocumentdetailbar', readOnly:this.readOnly},
				sm: new Ext.grid.RowSelectionModel({singleSelect: true}),
		        loadMask: new Ext.LoadMask(Ext.getBody(), {msg:"Please wait..."}),

		        columns: [{
			            	  	header:'Status',
			              	  	dataIndex:'status',
			              	  renderer: function(v,p,r) { 
				              		if (r.get("status") == "DRAFT"){
			              	    		return "<div style='float:left;'><div class='icn-pencil-small' style='color:#999;background-repeat:no-repeat;background-position:left right;width:32px;height:24px;padding-left:15px;font-weight:100;'>Draft</div></div>";
			              	    	}
				              		else if (r.get("status") == "RETIRED"){
			              	    		return "<div style='float:left;'><div class='icn-box-small' style='color:#999;background-repeat:no-repeat;background-position:left right;width:32px;height:24px;padding-left:15px;font-weight:100;'>Retired</div></div>";
			              	    	}
				              		else if (r.get("status") == "APPROVED"){
			              	    		return "<div style='float:left;'><div class='icn-tick' style='color:green;background-repeat:no-repeat;background-position:left right;width:32px;height:24px;padding-left:15px;'>IRB</div></div>";
			              	    	} else if (r.get("status") == "RSC_APPROVED") {
			              	    		return "<div style='float:left;'><div class='icn-tick' style='color:green;background-repeat:no-repeat;background-position:left right;width:32px;height:24px;padding-left:15px;'>RSC</div></div>";
			              	    	} else if (r.get("status") == "HC_APPROVED") {
			              	    		return "<div style='float:left;'><div class='icn-tick' style='color:green;background-repeat:no-repeat;background-position:left right;width:32px;height:24px;padding-left:15px;'>HC</div></div>";
			              	    	} else {
			              	    		return "<div style='float:left;'><div class='icn-ui-check-box-uncheck-disabled' style='color:#999;background-repeat:no-repeat;background-position:left right;width:32px;height:24px;padding-left:15px;'></div></div>";
			              	    	}
			              	    },
			                  	sortable:true,
			                  	width:60
			                },
		                  {
		                	  	header:'Name',
		                	  	dataIndex:'title',
		                	  	id:'colDocumentName',
		                	  	sortable:true,
		                	  	renderer:function(value, p, record){
		                    		return "<div class='document-row-info document-row-name nowrap'><div class='document-row-info-title'>"+record.data.title+"</div></div>";
		                  		},
		                	  	width:200
		                  },{
		                  	  	header:'Saved to form',
		                  	  	dataIndex:'formId',
		                  	    renderer: function(v,m,r) { 
		                  	    	var displayCurrentForm = (Ext.getCmp("clara-documents-filterpanel").hideFormOnlyOption == false)?" (this one)":"";
		                  	    	var relativeFormId = (typeof t.parentFormIds[r.get("formType")] == 'undefined')?1:(parseInt(t.parentFormIds[r.get("formType")].indexOf(r.get("parentFormId")) )+1);
		                  	    	var displayFormId = (relativeFormId >1)?" <span class='doc-file-formid'>#"+relativeFormId+"</span>":"";
		                  	    	return "<div class='document-row-info wrap'>"+((claraInstance.form && claraInstance.form.id && r.get('formId') == claraInstance.form.id)?(r.get("formTypeDesc")+displayFormId+displayCurrentForm):(r.get("formTypeDesc")+displayFormId)+"</div>");
		                  	    },
			                  	sortable:true,
			                  	width:100
			                },{
		                  	  	dataIndex:'parentFormId',
		                  	    hidden:true
			                },{
		                	  	header:'Type',
		                	  	dataIndex:'category',
		                	  	
		                	  	renderer: function(v) { 
		                	  		clog("rendering doc category: Clara.Documents.DocumentTypes["+v.toLowerCase()+"]");
		                	  		return "<div class='document-row-info'>"+Clara.Documents.DocumentTypes[v.toLowerCase()]+"</div>"; },
		                	  	sortable:true,
		                	  	width:280,
		                	  	hidden: false
		                    },{
		                	  	header:'Version',
		                  	  	dataIndex:'parentid',
		                  	    renderer: function(v,p,r) { 
		                  	    	if (r.get("parentid") != r.get("id")){
		                  	    		return "<a href='javascript:;' onClick='new Clara.Documents.VersionWindow({parentdocid:"+r.get("parentid")+", parentpanelid:\"clara-documents-gridpanel\"}).show();'><span class='document-row-info-version'>v."+(parseInt(r.data.versionId)+1)+"</span></a>";
		                  	    	}
		                  	    },
		                      	sortable:true,
		                      	width:34
		                    },
		                  {
		                  	  	header:'Created',
		                  	  	dataIndex:'created',
		                  	    renderer: function(date) { return "<div class='document-row-info'>"+date.format("m/d/y g:iA")+"</div>"; },
			                  	sortable:true,
			                  	width:70
			                }
		        ],
			    listeners:{
					
				    rowclick: function(grid, rowI, event)   {
						var docdata = grid.getStore().getAt(rowI).data;
						clog(docdata);
						var doc = new Clara.Documents.Document({
							id:docdata.id,
							hashid:docdata.hashid,
							title:docdata.title,
							category:docdata.category,
							status:docdata.status,
							filename:docdata.documentname,
							createdDate:docdata.createdDate,
							extension:docdata.extension,
							path:docdata.path,
							parentFormXmlDataDocumentId:docdata.parentid
						});
						
						Clara.Documents.MessageBus.fireEvent('fileselected', doc);
						
						
				    }
			    }
		};
		
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		Clara.Documents.GridPanel.superclass.initComponent.apply(this, arguments);
	}
	

});
Ext.reg('claradocumentgridpanel', Clara.Documents.GridPanel);