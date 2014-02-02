Ext.define('Clara.Reports.view.ReportDisplayFieldGridPanel', {
	extend: 'Ext.grid.Panel',
	alias: 'widget.reportdisplayfieldgridpanel',
	store: 'UserReportDisplayFields',
    requires:['Ext.grid.*','Ext.data.*','Ext.dd.*'],
	border:false,
    multiSelect: false,
    autoScroll: true,
	hideHeaders: true,
	stripeRows:false,
	parentWindow:{},
	viewConfig:{
		trackOver:false
	},
	maskOnDisable:false,
	initComponent: function() {
		var me = this;
        me.viewConfig = {
            plugins: {
            	ptype: 'gridviewdragdrop',
                dragText: 'Drag and drop to reorder'
            },
            listeners: {
                drop: function(node, data, dropRec, dropPosition) {
                    me.fireEvent("itemOrderChanged",data);
                }
            },
            emptyText:'<div class="wrap" style="padding:8px;"><h1>You have not selected any fields yet.</h1>Click the "Add.." button to add a field to show on the report.</div>'
        };
		me.columns = [{xtype:'rownumberer',dataIndex:''},
		{   
			header:'Field',
			dataIndex: 'fieldlabel',
			flex: 40,
			renderer: function(v){
				return "<div class='wrap'>"+v+"</div>";
			}
		},
		{   
			xtype:'actioncolumn',
			header:'',
			items:[{
				iconCls:'icn-minus-circle',
				icon:appContext+'/static/images/icn/minus-circle.png',
				tooltip:'Remove',
				handler: function(grid,rowIndex){
					var rec = grid.getStore().getAt(rowIndex);
					clog("REMOVE",rec);
					// /ajax/reports/{report-template-id}/remove-criteria
					jQuery.ajax({
		    			  type: 'GET',
		    			  async:false,
		    			  url: appContext+'/ajax/reports/'+me.parentWindow.report.id+'/remove-displayfield',
		    			  data: { reportFieldId:rec.get("id") },
		    			  success: function(data){
		      		        if (!data.error){
		      		        	var userReportDisplayFieldStore = Ext.StoreMgr.get('UserReportDisplayFields');
		      		        	userReportDisplayFieldStore.removeAll();
		      		        	userReportDisplayFieldStore.load();
		      		        }
		    			  },
		    			  error: function(){
		    				  cwarn("Error removing display field.");
		    			  }
		    		});
				}
			}],
			flex: 2
		}];

		var userReportDFStore = Ext.StoreMgr.get('UserReportDisplayFields');
		userReportDFStore.getProxy().url = appContext+'/ajax/reports/'+me.parentWindow.report.id+'/list-displayfields';
		userReportDFStore.load();
		
		me.callParent();
	}
});