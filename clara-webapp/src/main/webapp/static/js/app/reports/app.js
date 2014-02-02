var reportGlobals = {};




Ext.application({
    name: 'Clara.Reports', 
    appFolder:appContext + '/static/js/app/reports',
    
    models:['DisplayField','UserReport','Clara.Common.model.User','Protocol','Contract','UserReportCriteria','ReportCriteria','ComboCriteria'],
    stores:['ReportDisplayFields','UserReportDisplayFields','UserReports','Clara.Common.store.Users','Protocols','Contracts','UserReportCriterias','ReportCriterias','ComboCriterias'],
    controllers: ['Report','UserReport'],
    
    autoCreateViewport: true,
    enableQuickTips: true,
    launch: function() {
    	Clara.Reports.app = this;
        // This is fired as soon as the page is ready
    	clog("Ext.app launch.");
    },
    listeners:{
    	'userreportupdated':function(){
    		clog("APP EVENT:userreportupdated");
    		this.getController('Report').refreshReportPanel();
    	},
    	'userreportselected':function(r){
    		clog("APP EVENT:userreportselected",r);
    	}
    }
});