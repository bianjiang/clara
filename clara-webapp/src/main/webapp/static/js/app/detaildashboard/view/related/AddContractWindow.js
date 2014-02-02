Ext.define('Clara.DetailDashboard.view.related.AddContractWindow', {
	extend: 'Ext.window.Window',
	requires:['Ext.ux.form.SearchField','Clara.Common.view.ContractGridPanel'],
	alias: 'widget.addrelatedcontractwindow',
	title: 'Choose a Contract',
	width:750,
	modal:true,
	height:600,

	layout: {
		type: 'fit'
	},

	initComponent: function() {
		var me = this;

		me.dockedItems = [{
	        xtype: 'pagingtoolbar',
	        store: 'Clara.Common.store.Contracts',
	        dock: 'bottom',
	        displayInfo: true
	    },{
	    	xtype:'toolbar',
	    	dock:'top',
	    	items:[
	    			{
	    				xtype:'searchfield',
	    				store:Ext.data.StoreManager.lookup('Clara.Common.store.Contracts'),
	    				emptyText:'Search Contract Number, Company/Entity Name, PI Name or IRB#',
	    				paramName : 'keyword',
			        	reloadAllAsClear:true,
			        	flex:1,
			        	beforeSearch: function(){
			        		var fld = this;
			        		var pstore = Ext.data.StoreManager.lookup('Clara.Common.store.Contracts');
			        		Ext.apply(pstore.proxy.extraParams, {
			        			searchCriterias : null,
			        			keyword: fld.getValue()
			        		});
			        		return true;
			        	},
			        	beforeClear:function(){
			        		var pstore = Ext.data.StoreManager.lookup('Clara.Common.store.Contracts');
			        		Ext.apply(pstore.proxy.extraParams, {
			        			searchCriterias : null,
			        			keyword: ''
			        		});
			        	}
	    			}]
	    }];
		
		me.items = [{
			xtype:'commoncontractgridpanel',
			store: 'Clara.Common.store.Contracts',
			border:false
		}];
		me.buttons = [{text:'Close', handler:function(){me.close();}},{
			id:'btnAddSelectedContract',
			disabled:true,
			text:'Add Contract'
		}];

		me.callParent();
	}
});