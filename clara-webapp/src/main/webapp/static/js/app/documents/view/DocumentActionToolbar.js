Ext.define('Clara.Documents.view.DocumentActionToolbar',{
	extend: 'Ext.toolbar.Toolbar',
	alias: 'widget.tbdocumentaction',
	border:false,
	formView: false,
	
	resetActions: function() {
		Ext.getCmp("btnDocumentDownload").setDisabled(true);
		Ext.getCmp("btnDocumentChangeStatus").setDisabled(true);
		Ext.getCmp("btnDocumentViewVersions").setDisabled(true);
		Ext.getCmp("btnDocumentRevise").setDisabled(true);
		Ext.getCmp("btnDocumentRename").setDisabled(true);
		Ext.getCmp("btnDocumentChangeType").setDisabled(true);
		Ext.getCmp("btnDocumentDelete").setDisabled(true);
	},
	
	initComponent: function(){
		var me = this;
		

		me.items = [
			{
				text: '<span style="font-weight:800;font-size:12px;">Upload New Document</span>',
				disabled:!me.formView,
				iconCls:'icn-navigation-090-button',
				iconAlign:'left',
				handler: function(){
					// new Clara.Documents.UploadWindow({doc:{}}).show();
				}
			},'-',{
                xtype: 'button',
                text: '<span style="font-weight:800;font-size:12px;">Upload Revised Version</span>',
                disabled:true,
                id:'btnDocumentRevise',
                iconCls:'icn-document-tree'
            },'->',
			{
                xtype: 'button',
                text: 'Open / Download',
                disabled:true,
                id:'btnDocumentDownload',
                iconCls:'icn-arrow-270'
            },
            {
                xtype: 'button',
                text: 'Change Status',
                hidden:true,
                id:'btnDocumentChangeStatus',
                iconCls:'icn-lightning'
            },
            {
                xtype: 'button',
                text: 'Rename',
                iconCls:'icn-pencil-field',
                id:'btnDocumentChangeType',
                hidden:true
            },
            {
                xtype: 'button',
                text: 'Versions',
                disabled:true,
                iconCls:'icn-folder-clock',
                id:'btnDocumentViewVersions'
            },
            
            
            {
                xtype: 'button',
                text: 'Rename',
                iconCls:'icn-pencil-field',
                id:'btnDocumentRename',
                disabled:true
            },{
                xtype: 'button',
                text: 'Delete',
                iconCls:'icn-minus-circle',
                id:'btnDocumentDelete',
                disabled:true
            },'-',{
	    		xtype:'button',
	    		iconCls:'icn-category',
	    		pressed:false,
				enableToggle:true,
				id:'btnDocumentGroupByForm',
	    		text:'Group by form'
	    	},'-',{
	    		xtype:'button',
	    		id:'btnPrintDocumentList',
	    		tooltip:'Print list (opens new window)',
	    		tooltipType:'title',
	    		iconCls:'icn-printer'
	    	}
		];
		me.callParent();
	}

});

