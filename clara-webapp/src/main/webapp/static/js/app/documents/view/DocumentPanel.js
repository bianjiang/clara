Ext.define('Clara.Documents.view.DocumentPanel', {
	extend: 'Ext.grid.GridPanel',
	alias: 'widget.documentpanel',
	requires:['Clara.Documents.view.DocumentFilterToolbar','Clara.Documents.view.DocumentActionToolbar','Clara.Documents.view.VersionsWindow','Clara.Documents.view.UploadWindow'],
	title:'Documents',
	border:false,
	viewConfig: {
		stripeRows: true,
		trackOver:false,
		emptyText:'No documents found.'
	},
	loadMask:true,
	store: 'Clara.Documents.store.Documents',
	features: [{
		ftype:'grouping',
		enableGroupingMenu:false,
		
        groupHeaderTpl: ['{[values.rows[0].data.formTypeDesc]}']
	}],
	cls:'documentpanel',
	
	selModel: {
		mode:'MULTI'
	},
	
	// is this a form view, or dashboard? only formview can upload new files.
	formView: false,
	
	initComponent: function() { 
		var me = this;
		
		me.savedToFormRenderer = function(v,m,r) { 
  	    	var displayCurrentForm = (Ext.getCmp("btnTBDocumentsShowThisFormOnly").hidden == false)?" (this one)":"";
  	    	var relativeFormId = (typeof Clara.Application.DocumentController.parentFormIds[r.get("formType")] == 'undefined')?1:(1+Clara.Application.DocumentController.parentFormIds[r.get("formType")].indexOf(r.get("parentFormId")));
  	    	var displayFormId = (relativeFormId > 1)?" <span class='doc-file-formid'>#"+relativeFormId+"</span>":"";
  	    	return "<div class='document-row-info wrap'>"+((claraInstance.form && claraInstance.form.id && r.get('formId') == claraInstance.form.id)?(r.get("formTypeDesc")+displayFormId+displayCurrentForm):(r.get("formTypeDesc")+displayFormId)+"</div>");
  	    };
		
		me.dockedItems = [{
			dock: 'top',
			border:false,
			xtype: 'tbdocumentfilter',
			formView: me.formView
		},{
			dock:'top',
			border:false,
			xtype:'tbdocumentaction',
			formView: me.formView
		}];
		
		me.columns = [{
      	  	header:'Saved to form',
      	  	dataIndex:'formId',
      	  	id:'columnSavedToForm1',
      	    renderer: me.savedToFormRenderer,
          	sortable:false,
          	hidden:true,
          	width:110
        },{
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
      	    	} 
      	    	else if (r.get("status") == "ACKNOWLEDGED"){
      	    		return "<div style='float:left;'><div class='icn-ui-check-box-mix' style='color:#999;background-repeat:no-repeat;background-position:left right;width:32px;height:24px;padding-left:15px;'>Acknowledged</div></div>";
      	    	}
      	    	else if (r.get("status") == "HC_APPROVED") {
      	    		return "<div style='float:left;'><div class='icn-tick' style='color:green;background-repeat:no-repeat;background-position:left right;width:32px;height:24px;padding-left:15px;'>HC</div></div>";
      	    	} else if (r.get("status") == "PACKET_DOCUMENT") {
      	    		return "<div style='float:left;'><div class='icn-box-small' style='color:orange;background-repeat:no-repeat;background-position:left right;width:32px;height:24px;padding-left:15px;'>Packet</div></div>";
      	    	} else if (r.get("status") == "EPIC_DOCUMENT") {
      	    		return "<div style='float:left;'><div class='icn-box-small' style='color:red;background-repeat:no-repeat;background-position:left right;width:32px;height:24px;padding-left:15px;'>Epic</div></div>";
      	    	} else if (r.get("status") == "FINAL_LEGAL_APPROVED") {
      	    		return "<div style='float:left;'><div class='icn-box-small' style='color:red;background-repeat:no-repeat;background-position:left right;width:32px;height:24px;padding-left:15px;'>LEGAL</div></div>";
      	    	} else if (r.get("status") == "NAR") {
      	    		return "<div style='float:left;'><div class='icn-box-small' style='color:red;background-repeat:no-repeat;background-position:left right;width:32px;height:24px;padding-left:15px;'>NAR</div></div>";
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
    	  	renderer:function(v){
        		return "<div class='document-row-info document-row-name nowrap'><div class='document-row-info-title'>"+v+"</div></div>";
      		},
    	  	flex:1
      },{
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
	  	width:280,
	  	hidden: false
    },{
      	  	header:'Saved to form',
      	  	dataIndex:'formId',
      	  	id:'columnSavedToForm2',
      	    renderer: me.savedToFormRenderer,
          	sortable:true,
          	width:110
        },{
      	  	dataIndex:'parentFormId',
      	    hidden:true
        },{
    	  	header:'Version',
      	  	dataIndex:'parentid',
      	    renderer: function(v,p,r) { 
      	    	if (r.get("parentid") != r.get("id")){
      	    		return "<span class='document-row-info-version'>v."+(parseInt(r.get("versionId"))+1)+"</span>";
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
        }
];

		me.callParent();

	}
});