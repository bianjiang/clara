Ext.define('Clara.Queue.view.QueuePanel', {
	extend: 'Ext.grid.GridPanel',
	alias: 'widget.queuepanel',
	autoScroll: true,
	cls:'sidebar',
	unstyled:true,
	style:'border:1px solid #96baea;border-left:0px;',
	rowLines:false,
	hideHeaders:true,
	requires:[],
	title:'Available Queues',
	store: 'Clara.Queue.store.Queues',
	initComponent: function() { 
		var me = this;
		me.border=false;
		me.viewConfig = {
			trackOver:false,
			stripeRows: false,
			selectedItemCls: 'selected-bookmark',
			getRowClass: function() {
	            return "bookmark-row";
	        }
		};

		me.columns = [{
			header : '',
			dataIndex : 'name',
			flex:1
		}];
		me.listeners = {
				added: function(){
					me.getStore().load();
				}	
		};
		me.callParent();

	}
});