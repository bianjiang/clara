Ext.define('Clara.Dashboard.view.ProtocolGridPanel', {
	extend: 'Clara.Common.view.ProtocolGridPanel',
	alias: 'widget.protocolgridpanel',
	requires: ['Ext.ux.form.SearchField'],
	pageSize:25,
	clickableRows:true,	
	initComponent: function() { 
		var me = this;
	    me.dockedItems= [{
	        xtype: 'pagingtoolbar',
	        store: 'Clara.Common.store.Protocols',
	        dock: 'bottom',
	        displayInfo: true
	    },{
	    	xtype:'toolbar',
	    	dock:'top',
	    	items:[{
	    			xtype:'button',
	    			text:'<strong>Create a new submission..</strong>',
	    			iconCls:'icn-plus-button',
	    			id:'btnDashboardNewSubmission'
	    			},'->',
	    			{
	    				xtype:'searchfield',
	    				store:Ext.data.StoreManager.lookup('Clara.Common.store.Protocols'),
	    				emptyText:'Search title or IRB number..',
	    				paramName : 'keyword',
			        	reloadAllAsClear:true,
			        	flex:1,
			        	beforeSearch: function(){
			        		var fld = this;
			        		Clara.Dashboard.app.getController("Dashboard").getBookmarkPanel().getView().select(0);
			        		var pstore = Ext.data.StoreManager.lookup('Clara.Common.store.Protocols');
			        		Ext.apply(pstore.proxy.extraParams, {
			        			searchCriterias : null,
			        			keyword: fld.getValue()
			        		});
			        		
			        		return true;
			        	},
			        	beforeClear:function(){
			        		Clara.Dashboard.app.getController("Dashboard").getBookmarkPanel().getView().select(0);
			        		var pstore = Ext.data.StoreManager.lookup('Clara.Common.store.Protocols');
			        		Ext.apply(pstore.proxy.extraParams, {
			        			searchCriterias : null,
			        			keyword: ''
			        		});
			        	}
	    			}]
	    }];
		
		me.callParent();

	}
});