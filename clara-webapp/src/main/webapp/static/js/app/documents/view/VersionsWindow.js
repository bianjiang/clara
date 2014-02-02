Ext.define('Clara.Documents.view.VersionsWindow', {
	extend: 'Ext.window.Window',
	requires:[],
	alias: 'widget.versionswindow',
	title: 'Document Versions',
	width:770,
	modal:true,
	height:500,

	layout: {
		type: 'fit'
	},

	initComponent: function() {
		var me = this;

		me.items = [{
			xtype:'grid',
			border:false,
			viewConfig: {
				stripeRows: true,
				trackOver:false
			},
			listeners: {
				afterrender: function(gp){
					clog("after render grid");
					gp.getStore().loadDocumentVersions(Clara.Application.DocumentController.selectedDocument.get("parentid"));
				}
			},
			store : 'Clara.Documents.store.DocumentVersions',
			columns: [
				{
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
		    	  	sortable:true,
		    	  	renderer:function(v){
		        		return "<div class='document-row-info document-row-name nowrap'><div class='document-row-info-title'>"+v+"</div></div>";
		      		},
		    	  	flex:2
				},
				{
			  	  	header:'Type',
				  	dataIndex:'category',
				  	renderer: function(v) { 
				  		if (typeof Clara.Application.DocumentController == "undefined"){
				  			return "<div class='wrap'>"+v+"</div>";
				  		} else {
				  			return "<div class='document-row-info'>"+Clara.Application.DocumentController.documentTypes[v.toLowerCase()]+"</div>"; 
				  		}
				  	},
				  	sortable:true,
				  	flex:1,
				  	hidden: false
			    },
			    {
		    	  	header:'Version',
		      	  	dataIndex:'parentid',
		      	    renderer: function(v,p,r) { 
		      	    	if (r.get("parentid") != r.get("id")){
		      	    		return "v."+(parseInt(r.get("versionId"))+1)+"</span>";
		      	    	}
		      	    },
		          	sortable:true,
		          	width:48
		        },
		      {
		      	  	header:'Created',
		      	  	dataIndex:'created',
		      	    renderer: function(v) { return "<div class='document-row-info'>"+Ext.util.Format.date(v,'m/d/Y g:iA')+"</div>"; },
		          	sortable:true,
		          	width:140
		        },
		      {
		      	  	header:'Action',
		      	  	xtype:'actioncolumn',
		      	  	dataIndex:'created',
		      	    items:[{
		      	    	icon:'../../static/images/icn/arrow-270.png',
		      	    	tooltip:'Download',
		      	    	getClass: function(v,m,rec){
                  	  		if (Clara.Application.DocumentController.hasDocumentPermission(rec.get("category"), "canRead") === true) {
                  	  			return 'x-grid-center-icon';
                  	  		}
                  	  		return 'x-hide-display';
                  	  	},
		      	    	handler: function(g,r,c){
		      	    		var rec = g.getStore().getAt(r);
		      	    		Clara.Application.DocumentController.downloadDocument(rec);
		      	    	}
		      	    },{
		      	    	icon:'../../static/images/icn/pencil-field.png',
		      	    	tooltip:'Rename',
		      	    	getClass: function(v,m,rec){
                  	  		if (Clara.Application.DocumentController.hasDocumentPermission(rec.get("category"), "canUpdate") === true) {
                  	  			return 'x-grid-center-icon';
                  	  		}
                  	  		return 'x-hide-display';
                  	  	},
		      	    	handler: function(gp,r,c){
		      	    		var rec = gp.getStore().getAt(r);
		      	    		Clara.Application.DocumentController.renameDocument(rec, function(){
		      	    			gp.getStore().loadDocumentVersions(Clara.Application.DocumentController.selectedDocument.get("parentid"));
		      	    		});
		      	    	}
		      	    },{
		      	    	icon:'../../static/images/icn/lightning.png',
		      	    	tooltip:'Change Status',
		      	    	getClass: function(v,m,rec){
                  	  		if (Clara.Application.DocumentController.getDocumentPanel().formView === true && claraInstance.HasAnyPermissions(['ROLE_IRB_OFFICE','ROLE_SYSTEM_ADMIN'])) {
                  	  			return 'x-grid-center-icon';
                  	  		}
                  	  		return 'x-hide-display';
                  	  	},
		      	    	handler: function(gp,r,c){
		      	    		var rec = gp.getStore().getAt(r);
		      	    		Clara.Application.DocumentController.changeDocumentStatus(rec, function(){
		      	    			gp.getStore().loadDocumentVersions(Clara.Application.DocumentController.selectedDocument.get("parentid"));
		      	    		});
		      	    	}
		      	    },{
		      	    	icon:'../../static/images/icn/minus-circle.png',
		      	    	tooltip:'Delete',
		      	    	getClass: function(v,m,rec){
                  	  		if (Clara.Application.DocumentController.hasDocumentPermission(rec.get("category"), "canWrite") === true && ( rec.get("status") != "RSC_APPROVED" && rec.get("status") != "APPROVED" )) {
                  	  			return 'x-grid-center-icon';
                  	  		}
                  	  		return 'x-hide-display';
                  	  	},
		      	    	handler: function(gp,r,c){
		      	    		var rec = gp.getStore().getAt(r);
		      	    		Clara.Application.DocumentController.deleteDocument(rec, function(){
		      	    			gp.getStore().loadDocumentVersions(Clara.Application.DocumentController.selectedDocument.get("parentid"));
		      	    		});
		      	    	}
		      	    }],
		          	sortable:false,
		          	width:80
		        }
		    ]
		}];
		me.buttons = [{text:'Close', handler:function(){me.close();}}];

	
		
		me.callParent();
	}
});