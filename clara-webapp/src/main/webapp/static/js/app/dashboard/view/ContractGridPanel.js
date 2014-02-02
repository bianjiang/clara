Ext.define('Clara.Dashboard.view.ContractGridPanel', {
	extend: 'Clara.Common.view.ContractGridPanel',
	alias: 'widget.contractgridpanel',
	requires: ['Ext.ux.form.SearchField'],
	autoScroll: true,
	clickableRows:true,
	cls:'contractgridpanel',
	initComponent: function() { 
		var me = this;
	    me.dockedItems= [{
	        xtype: 'pagingtoolbar',
	        store: 'Clara.Common.store.Contracts',
	        dock: 'bottom',
	        displayInfo: true
	    },{
	    	xtype:'toolbar',
	    	dock:'top',
	    	items:[{
	    			xtype:'button',
	    			text:'<strong>Create a new contract..</strong>',
	    			iconCls:'icn-plus-button',
	    			id:'btnDashboardNewContract'
	    			},'->',
	    			{
	    				xtype:'searchfield',
	    				store:Ext.data.StoreManager.lookup('Clara.Common.store.Contracts'),
	    				emptyText:'Search Contract Number, Company/Entity Name, PI Name or IRB#',
	    				paramName : 'keyword',
			        	reloadAllAsClear:true,
			        	flex:1,
			        	beforeSearch: function(){
			        		var fld = this;
			        		Clara.Dashboard.app.getController("Dashboard").getBookmarkPanel().getView().select(0);
			        		var pstore = Ext.data.StoreManager.lookup('Clara.Common.store.Contracts');
			        		Ext.apply(pstore.proxy.extraParams, {
			        			searchCriterias : null,
			        			keyword: fld.getValue()
			        		});
			        		
			        		return true;
			        	},
			        	beforeClear:function(){
			        		Clara.Dashboard.app.getController("Dashboard").getBookmarkPanel().getView().select(0);
			        		var pstore = Ext.data.StoreManager.lookup('Clara.Common.store.Contracts');
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