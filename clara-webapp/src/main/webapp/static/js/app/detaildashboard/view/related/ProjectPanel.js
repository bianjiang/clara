Ext.define('Clara.DetailDashboard.view.related.ProjectPanel', {
	extend: 'Ext.grid.GridPanel',
	alias: 'widget.relatedprojectpanel',
	layout:'fit',
	title:'Related Projects',
	border:true,
	viewConfig: {
		stripeRows: true,
		trackOver:false
	},
	loadMask:true,
	store: 'Clara.DetailDashboard.store.RelatedProjects',
	clickableRows:true,
	dockedItems: [{
		dock: 'top',
		border:false,
		xtype: 'toolbar',
		items: [{
			iconCls:'icn-application--plus',
			text:'Add project..',
			id:'btnAddRelatedProject'
		},{
			iconCls:'icn-application--minus',
			text:'Remove project..',
			disabled:true,
			id:'btnRemoveRelatedProject'
		}]
	}],
	
	
	
	initComponent: function() { 
		var me = this;
		
		me.listeners = {
				activate:function(p){
					Ext.getCmp("btnRemoveRelatedProject").setDisabled(true);
					p.getStore().loadRelatedProjects();
				}
			};
		
		me.columns = [
{
	resizable:true,
	flex:1,
	header:'Project',
	sortable:false,
	dataIndex: 'prn',
	renderer:function(v,p,r){

			var s = "<strong>"+r.get("prn")+"</strong> "+r.get("title")+"<div><strong>PI:</strong> "+r.get("piName")+"</div>";
			return "<div class='wrap'>"+s+"</div>";

	}
},
        {header: 'Status', width:200, sortable: true, dataIndex: 'status', 
        	renderer:function(v,p,record){
        		var html = "<div class='wrap'><span class='project-form-row-field project-form-status'>"+v+"</span>";
        		return html+"</div>";
        	}
        }
		];

		me.callParent();

	}
});