Ext.define('Clara.Reports.controller.Report', {
    extend: 'Ext.app.Controller',
    models: ['UserReport'],
    refs: [{
        ref: 'userReportsList',
        selector: 'userreportslist'
    },{
        ref: 'reportTypesList',
        selector: 'reporttypeslist'
    },{
    	ref:'reportSchedulePanel',
    	selector:'reportschedulepanel'
    },{
        ref: 'reportForm',
        selector: 'newuserreportwindow > panel'
    },{
    	ref: 'userReportWindow',
    	selector:'userreportwindow'
    }],

    stores: ['UserReportDisplayFields','ComboCriterias','UserReportResults','UserReports','ReportTypes','Clara.Common.store.Users','Clara.Common.store.Colleges','Clara.Common.store.Departments','Clara.Common.store.Subdepartments','Protocols'],
    
    init: function() {
        // Start listening for events on views
        this.control({
        	'userreportslist': {
                selectionchange: this.onReportSelectChange,
                itemdblclick: this.onReportDoubleClick
            },
            'button[action=generate-report]':{
            	click: this.createNewReport
            },
            'button[action=refresh]':{
            	click: this.refreshReportPanel
            },
            'a':{
            	deletereport: this.deleteReport
            }
        });
    },
    
    onReportDoubleClick: function(gp,rec,item,idx,e,eopts){
    	clog("dbl",gp,rec,item);
        Clara.Reports.app.getController("UserReport").selectedUserReport = rec;
        if (piwik_enabled()){
			_paq.push(['trackEvent', 'REPORTS', 'Edit report: Window open for '+rec.get("description")]);
		}
    	Ext.create('Clara.Reports.view.UserReportWindow', {report:rec.data,title: "Editing '"+rec.get("description")+"'"}).show();
    },
    
    deleteReport: function(id){
    	var t = this;
    	Ext.Msg.show({
    	     title:'Delete report?',
    	     msg: 'Are you sure you want to delete this report?',
    	     buttons: Ext.Msg.YESNO,
    	     icon: Ext.Msg.WARNING,
    	     fn: function(btn){
    	    	 if (btn == 'yes'){
                     jQuery.ajax({
                 		url:appContext + "/ajax/reports/"+id+"/remove",
                 		data:{
                 			userId:claraInstance.user.id
                 		},
                 		type: "GET",
                 		async: false,
             			dataType:'xml',
             			success: function(data){
             				if (piwik_enabled()){
             					_paq.push(['trackEvent', 'REPORTS', 'Report deleted: '+id]);
             				}
                 			clog("Deleted.");
                 			t.refreshReportPanel();
                 		}, error: function(data){
                 			clog("Deleted (error callback)");
                 			t.refreshReportPanel();
                 		}
                 	});
    	    	 }
    	     }
    	});
    	
    },
    
    onLaunch: function() {
        // load user reports instead of autoload?
    },

    createNewReport: function(){
    	clog("createNewReport");
    	if (piwik_enabled()){
				_paq.push(['trackEvent', 'REPORTS', 'New Report window open']);
			}
    	Ext.create('Clara.Reports.view.ReportTypesWindow', {}).show();
    },
    
    refreshReportPanel:function(){
    	clog("refreshReportPanel");
    	var store = this.getUserReportsStore();
    	store.removeAll();
    	store.load();
    },
    
    onReportSelectChange: function(selModel, selection) {
        // Fire an application wide event
    	clog("onReportSelectChange",selModel,selection);
        Clara.Reports.app.getController("UserReport").selectedUserReport =  selection[0];
        this.application.fireEvent('userreportselected', selection[0]);
    }
});
