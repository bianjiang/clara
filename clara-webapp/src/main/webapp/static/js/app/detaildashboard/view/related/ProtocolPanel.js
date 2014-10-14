Ext.define('Clara.DetailDashboard.view.related.ProtocolPanel', {
	extend: 'Ext.grid.GridPanel',
	alias: 'widget.relatedprotocolpanel',
	layout:'fit',
	title:'Related Protocols',
	border:true,
	viewConfig: {
		stripeRows: true,
		trackOver:false
	},
	loadMask:true,
	store: 'Clara.DetailDashboard.store.RelatedProtocols',
	clickableRows:true,
	dockedItems: [{
		dock: 'top',
		border:false,
		xtype: 'toolbar',
		items: [{
			iconCls:'icn-application--plus',
			text:'Add protocol..',
			id:'btnAddRelatedProtocol'
		},{
			iconCls:'icn-application--minus',
			text:'Remove protocol..',
			disabled:true,
			id:'btnRemoveRelatedProtocol'
		}]
	}],
	
	
	
	initComponent: function() { 
		var me = this;
		
		me.listeners = {
				activate:function(p){
					Ext.getCmp("btnRemoveRelatedProtocol").setDisabled(true);
					p.getStore().loadRelatedProtocols();
				}
			};
		
		me.columns = [
{
	resizable:true,
	flex:1,
	header:'Protocol',
	sortable:false,
	dataIndex: 'identifier',
	renderer:function(v,p,r){

			var s = "<a target='_blank' href='"+appContext+"/protocols/"+r.get("id")+"/dashboard' style='font-weight:800;'>"+r.get("identifier")+"</a>";
			s += " "+r.get("title");
			return "<div class='wrap'>"+s+"</div>";

	}
},
        {header: 'Status', width:200, sortable: true, dataIndex: 'status', 
        	renderer:function(v,p,record){
        		var html = "<div class='wrap'><span class='protocol-form-row-field protocol-form-status'>"+v+"</span>";
        		return html+"</div>";
        	}
        }
		];

		me.callParent();

	}
});