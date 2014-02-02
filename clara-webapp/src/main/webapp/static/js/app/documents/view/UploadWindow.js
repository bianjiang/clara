Ext.define('Clara.Documents.view.UploadWindow', {
	extend: 'Ext.window.Window',
	iconCls:'icn-drive-upload',
	requires:[],
	alias: 'widget.uploadwindow',
	title: 'Upload',
	width:800,
	modal:true,
	callbackFn: null,
	doc:null,

	layout: 'fit',

	initComponent: function() {
		var me = this;

		me.items =[{
			xtype:'form',
			id:'uploadDocumentForm',
			bodyPadding:10,
			border:false,
			items:[{
				xtype:'filefield',
				name:'file',
				fieldLabel:'File',
				msgTarget:'side',
				allowBlank:false,
				anchor:'100%',
				buttonText:'Choose a file..',
				listeners: {
	                'change': function(fb, v){
						if (me.doc !== null) {
							Ext.getCmp('btnUploadSelectedDocument').setDisabled(false);
						} else {
							Ext.getCmp("fldSelectedDocumentType").setDisabled(false);
							Ext.getCmp('btnUploadSelectedDocument').setDisabled(true);
						}
	                }
	            }
			},{

	        	xtype:'combo',
	        	anchor:'100%',
		      	fieldLabel:"Document type",
		      	disabled:true,
		      	typeAhead:false,
	    	   	store:'Clara.Documents.store.DocumentTypes', 
	    		value:(me.doc !== null)?(me.doc.get("category")):"",
	    	   	forceSelection:true,
	    	   	displayField:'desc', 
	    	   	listConfig:{
	    			 loadingText: 'Please wait..',
	    			 emptyText: 'No matching document types found.',
	    			 getInnerTpl: function() {
	    	             return '<span class="documentcategory">{category}:</span> <span class="{descCls}">{desc}</span>';
	    	         }
	    		 },
	    	  
	    	   	valueField:'doctype', 
	    	   	mode:'remote', 
	    	   	triggerAction:'all',
	    	   	editable:false,
	    	   	allowBlank:false,
	    	   	id:'fldSelectedDocumentType',
	    	   	listeners:{
	    	   		'select':function(c, record, index){
	    	   				if (index<0) {
	    	   					Ext.getCmp('btnUploadSelectedDocument').setDisabled(true);
	    	   				} else {
	    	   					Ext.getCmp('btnUploadSelectedDocument').setDisabled(false);
	    	   				}
	    	   			
	       			}
	       		}
		   
			},{
				xtype:'textfield',
			    fieldLabel: 'Document <span style="font-weight:800;">name</span>',
			    id: 'fldSelectedDocumentName',
			    allowBlank:false,
			    value:(me.doc !== null)?(me.doc.get("title")):"",
			    width:655
			},
			{
				xtype:'label',
				html:'<div style="font-weight:100;color:red;padding-left:104px;"><h1 style="font-size:12px;color:#666;">The Document Name must include the title, version number, and date (listed on the document).</h1>This is the information that will be included in IRB letters when referencing this document.</div>'
			},
			{	xtype:'hidden',
			    id: 'fldSelectedDocumentParentId',
			    value:(me.doc !== null)?(me.doc.get("parentFormXmlDataDocumentId")):0,
			    height:0
			}]
		}];
		
		me.buttons = [{
			text:'Upload',
			disabled:true,
			id:'btnUploadSelectedDocument',
			handler: function(){
				var fp = Ext.getCmp("uploadDocumentForm");
				/*
			    if(form.isValid()){
			        form.submit({
			            url: 'photo-upload.php',
			            waitMsg: 'Uploading your photo...',
			            success: function(fp, o) {
			                Ext.Msg.alert('Success', 'Your photo "' + o.result.file + '" has been uploaded.');
			            }
			        });
			    }
			    */
				
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
                				    var d =  me.doc || Ext.create('Clara.Documents.model.Document',{});
                				    d.set("id", fileObj.id);
                				    d.set("hashid", fileObj.identifier);
                				    d.set("title", Ext.getCmp("fldSelectedDocumentName").getValue());
                				    d.set("category", Ext.getCmp("fldSelectedDocumentType").getValue());
                				    d.set("parentFormXmlDataDocumentId", Ext.getCmp("fldSelectedDocumentParentId").getValue());
                				 
                				    var url = appContext + "/ajax/"+claraInstance.type+"s/"+claraInstance.id+"/"+claraInstance.type+"-forms/" + (claraInstance.form.id || 0)+ "/"+claraInstance.type+"-form-xml-datas/" + (claraInstance.form.xmlDataId || 0)+ "/documents/add";
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
                							"title": d.get("title"),
                							"uploadedFileId": d.get("id"),
                							"category": d.get("category"),
                							"parentFormXmlDataDocumentId": (d.get("parentid") || 0)
                						},
                						success: function(data){
                							Clara.Application.DocumentController.onDocumentsUpdated();
                							if (me.callbackFn) me.callbackFn();
                						}
                					});
                				    
                				    
           							me.close();
                			   }
                		   
                	   }
	                });
                }
            
				
			}
		},{text:'Cancel', handler:function(){me.close();}}];

	
		
		me.callParent();
	}
});