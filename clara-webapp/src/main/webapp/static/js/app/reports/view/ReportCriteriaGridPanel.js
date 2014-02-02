Ext.define('Clara.Reports.view.ReportCriteriaGridPanel', {
	extend: 'Ext.grid.Panel',
	alias: 'widget.reportcriteriagridpanel',
	store: 'UserReportCriterias',
	border:false,
	hideHeaders: false,
	stripeRows:false,
	parentWindow:{},
	viewConfig:{
		trackOver:false
	},
	disableSelection:true,
	listeners:{
		itemclick: function(gp,rec){
			reportGlobals.selectedReportCriteria = rec;
		}
	},
	initComponent: function() {
		var t = this;
		t.columns = [{
			header:'Search Field',
			dataIndex: 'fieldlabel',
			flex: 10
		},
		{
			header:'Operator',
			dataIndex: 'operator',
			flex: 10
		},
		{   
			header:'Value',
			dataIndex: 'displayvalue',
			flex: 20,
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
				handler: function(grid,rowIndex,colIndex){
					var rec = grid.getStore().getAt(rowIndex);
					clog("REMOVE",rec);
					// /ajax/reports/{report-template-id}/remove-criteria
					jQuery.ajax({
		    			  type: 'GET',
		    			  async:false,
		    			  url: appContext+'/ajax/reports/'+t.parentWindow.report.id+'/remove-criteria',
		    			  data: { criteriaId:rec.get("id") },
		    			  success: function(data){
		      		        if (!data.error){
		      		        	var userReportCriteriasStore = Ext.StoreMgr.get('UserReportCriterias');
		      		        	userReportCriteriasStore.removeAll();
		      		        	userReportCriteriasStore.load();
		      		        }
		    			  },
		    			  error: function(){
		    				  cwarn("Error removing criteria.");
		    			  }
		    		});
				}
			}],
			flex: 2
		}];
		t.callParent();

		var reportCriteriasStore = Ext.StoreMgr.get('ReportCriterias');
		reportCriteriasStore.getProxy().url = appContext+'/ajax/reports/'+t.parentWindow.report.id+'/list-available-criteria';
		

		
		t.store.getProxy().url = appContext+'/ajax/reports/'+t.parentWindow.report.id+'/list-criteria';
		t.store.load();
	}
});