Ext.define('Clara.DetailDashboard.view.related.AddProtocolWindow', {
	extend: 'Ext.window.Window',
	requires:['Ext.ux.form.SearchField','Clara.Dashboard.view.ProtocolGridPanel'],
	alias: 'widget.addrelatedprotocolwindow',
	title: 'Choose a Protocol',
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
	        store: 'Clara.Common.store.Protocols',
	        dock: 'bottom',
	        displayInfo: true
	    },{
	    	xtype:'toolbar',
	    	dock:'top',
	    	items:[
	    			{
	    				xtype:'searchfield',
	    				store:Ext.data.StoreManager.lookup('Clara.Common.store.Protocols'),
	    				emptyText:'Search Title or IRB#',
	    				paramName : 'keyword',
			        	reloadAllAsClear:true,
			        	flex:1,
			        	beforeSearch: function(){
			        		var fld = this;
			        		var pstore = Ext.data.StoreManager.lookup('Clara.Common.store.Protocols');
			        		Ext.apply(pstore.proxy.extraParams, {
			        			searchCriterias : null,
			        			keyword: fld.getValue()
			        		});
			        		return true;
			        	},
			        	beforeClear:function(){
			        		var pstore = Ext.data.StoreManager.lookup('Clara.Common.store.Protocols');
			        		Ext.apply(pstore.proxy.extraParams, {
			        			searchCriterias : null,
			        			keyword: ''
			        		});
			        	}
	    			}]
	    }];
		
		me.items = [{
			xtype:'commonprotocolgridpanel',
			store: 'Clara.Common.store.Protocols',
			border:false
		}];
		me.buttons = [{text:'Close', handler:function(){me.close();}},{
			id:'btnAddSelectedProtocol',
			disabled:true,
			text:'Add Protocol'
		}];

		me.callParent();
	}
});