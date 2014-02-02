Ext.define('Clara.Reports.view.ReportTypesWindow', {
    extend: 'Ext.window.Window',
    requires:['Clara.Reports.view.ReportTypesList'],
    alias: 'widget.reporttypeswindow',
    title: 'What type of report do you want to create?',
    width:600,
    height:350,
    modal:true,
    iconCls:'icn-report--plus',
    layout: {
        type: 'fit'
    },
    items:[{
    	xtype:'reporttypeslist'
    }],
    
    style : 'z-index: -1;', // IE8 fix (http://www.sencha.com/forum/archive/index.php/t-241500.html?s=15ad65f757fb7325aa20735e3226faab)
    
    initComponent: function() {
    	var t = this;
       
        t.buttons = [{
        	id:'btnChooseReportType',
        	disabled:true,
        	text:'Create and continue..',
        	handler:function(){
        		
        		// Create report, and if successful, open the criteria window
        		
        		jQuery.ajax({
					  type: 'POST',
					  async:false,
					  url: appContext+'/ajax/reports/create',
					  data: {
	        				description: reportGlobals.selectedReportType.get("category")+": "+reportGlobals.selectedReportType.get("type"),
	        				reportType: reportGlobals.selectedReportType.get("type"),
	        				globalOperator: "AND",
	        				cron:null
	        			},
					  success: function(data){
	        		        if (!data.error){
                                clog("Created report. Got back data:",data);

                                Clara.Reports.app.getController("UserReport").selectedUserReport = Ext.create('Clara.Reports.model.UserReport',{
                                    id: data.data.id,
                                    description: data.data.description,
                                    globalOperator: data.data.globalOperator,
                                    cron: data.data.cronExpression,
                                    typeDescription:data.data.typeDescription,
                                    created: data.data.created,
                                    status: data.data.status
                                });
                                t.close();
	                			var rwin = Ext.create('Clara.Reports.view.UserReportWindow', {report:data.data,title: reportGlobals.selectedReportType.get("category")+": "+reportGlobals.selectedReportType.get("type"),reportTypeRecord:reportGlobals.selectedReportType});
	                			rwin.show();
	                			Clara.Reports.app.getController("Report").refreshReportPanel();
	                			
	        		        }
					  },
					  error: function(){
						  cwarn("Error creating new report");
					  }
				});
        		
        		
        	}
        }];
        t.callParent();
    }
});