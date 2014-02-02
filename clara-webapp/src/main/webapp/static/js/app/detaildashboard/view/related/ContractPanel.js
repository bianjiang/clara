Ext.define('Clara.DetailDashboard.view.related.ContractPanel', {
	extend: 'Ext.grid.GridPanel',
	alias: 'widget.relatedcontractpanel',
	layout:'fit',
	title:'Related Contracts',
	border:true,
	viewConfig: {
		stripeRows: true,
		trackOver:false
	},
	loadMask:true,
	store: 'Clara.DetailDashboard.store.RelatedContracts',
	clickableRows:true,
	dockedItems: [{
		dock: 'top',
		border:false,
		xtype: 'toolbar',
		items: [{
			iconCls:'icn-application--plus',
			text:'Add contract..',
			id:'btnAddRelatedContract'
		},{
			iconCls:'icn-application--minus',
			text:'Remove contract..',
			disabled:true,
			id:'btnRemoveRelatedContract'
		}]
	}],
	
	
	
	initComponent: function() { 
		var me = this;
		
		me.listeners = {
				activate:function(p){
					Ext.getCmp("btnRemoveRelatedContract").setDisabled();
					p.getStore().loadRelatedContracts();
				}
			};
		
		me.columns = [
{
	resizable:true,
	flex:1,
	header:'Contract',
	sortable:false,
	dataIndex: 'identifier',
	renderer:function(v,p,r){

			var s = "<a target='_blank' href='"+appContext+"/contracts/"+r.get("id")+"/dashboard' style='font-weight:800;'>"+r.get("identifier")+"</a>";
			s += " "+Clara_HumanReadableType(r.get("contractEntityTypeDesc").replace(/-/gi," "));
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