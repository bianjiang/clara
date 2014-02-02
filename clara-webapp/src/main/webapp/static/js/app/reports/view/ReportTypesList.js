Ext.define('Clara.Reports.view.ReportTypesList', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.reporttypeslist',
    store: 'ReportTypes',
    border:false,
    hideHeaders: true,
    stripeRows:true,
    listeners:{
    	itemclick: function(gp,rec){
    		reportGlobals.selectedReportType = rec;
    		Ext.getCmp("btnChooseReportType").setDisabled(false);
    	}
    },
    initComponent: function() {
        this.columns = [{
            dataIndex: 'id',
            renderer:function(v,m,r){
            	return "<div class='report-type report-type-"+r.get("id")+" "+r.get("icnCls")+"'><h1><span class='report-type-category'>"+r.get("category")+"</span>: <span class='report-type-type'>"+r.get("type")+"</span></h1><div class='report-type-description'>"+r.get("description")+"</div></div>";
            },
            flex: 1
        }];
        this.callParent();
    }
});