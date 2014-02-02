Ext.define('Clara.Documents.view.DocumentFilterToolbar',{
	extend: 'Ext.toolbar.Toolbar',
	alias: 'widget.tbdocumentfilter',
	border:false,
	formView: false,
	
	initComponent: function(){
		var me = this;
		me.items = [{
			xtype:'tbtext',
			text:'Show:',
			hidden:!me.formView
		},{
			xtype:'button',
			text:'All Documents',
			iconCls:'icn-blue-documents-stack',
			id:'btnTBDocumentsShowAll',
			enableToggle:true,
			hidden:!me.formView
		},{
			xtype:'button',
			iconCls:'icn-blue-document-invoice',
			text:'This form only',
			id:'btnTBDocumentsShowThisFormOnly',
			enableToggle:true,
			hidden:!me.formView
		},{
			xtype:'button',
			iconCls:'icn-blue-document-snippet',
			text:'This revision only',
			id:'btnTBDocumentsShowThisRevisionOnly',
			enableToggle:true,
			hidden:!me.formView
		},'->',{
			xtype:'textfield',
			fieldLabel:'<strong>Search</strong>',
			labelWidth:40,
			width:250,
			labelAlign:'right',
			id:'fldTBDocumentTextFilterField',
			enableKeyEvents:true,
			listeners:{
				keyup:function(f){
				    var v = f.getValue();
					if (jQuery.trim(v) == "") {
						clog("nothing to search by");
						jQuery(".documentpanel .x-grid-row").show();
					}
				
					else {
						clog("filtering by "+v);
						jQuery(".documentpanel .x-grid-row").each(function(){
							var row = this;
							if (jQuery(row).text().toLowerCase().indexOf(jQuery.trim(v).toLowerCase()) == -1) jQuery(row).hide();
							else jQuery(row).show();
						});
					}
				}
			}
		},{
			xtype:'combo',
			width:250,
			cls: 'cbqueuelist',
			id:'fldTBDocumentType',
			fieldLabel:'Filter by type',
			labelAlign:'right',
			displayField:'value',
			valueField:'id',
			queryMode:'local',
			value:'All Types',
			forceSelection:true,
			store: new Ext.data.ArrayStore({
				storeId:'FilteredDocumentTypeStore',
				fields: ['id','value'],
				data:[['','All Types']]
			})
		},{
			xtype:'combo',
			width:200,
			hidden:true,
			cls: 'cbqueuelist',
			id:'fldTBDocumentCreator',
			labelAlign:'right',
			displayField:'value',
			valueField:'id',
			queryMode:'local',
			value:'All Committees',
			forceSelection:true,
			store: new Ext.data.ArrayStore({
				storeId:'FilteredDocumentCreatorStore',
				fields: ['id','value'],
				data:[['','All Committees']]
			})
		},
		{
			xtype:'button',
			id:'btnClearQueueFilter',
			iconCls:'icn-cross',
			handler: function(){
				var docStore = Ext.StoreMgr.lookup("Clara.Documents.store.Documents");
				docStore.clearFilter();
				jQuery(".documentpanel .x-grid-row").show();
				
				Ext.getCmp("fldTBDocumentTextFilterField").setValue("");
				Ext.getCmp("fldTBDocumentType").setValue("");
			}
		}
		];
		me.callParent();
	}

});

