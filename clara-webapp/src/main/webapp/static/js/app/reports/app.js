var reportGlobals = {};

Ext.Loader.setPath('Clara.LetterBuilder', appContext+'/static/js/app/letterbuilder');

Ext.application({
    name: 'Clara.Reports', 
    appFolder:appContext + '/static/js/app/reports',
    
    models:['Clara.LetterBuilder.model.Recipient','DisplayField','UserReport','Clara.Common.model.User','Protocol','Contract','UserReportCriteria','ReportCriteria','ComboCriteria'],
    stores:['ReportRecipients','ReportDisplayFields','UserReportDisplayFields','UserReports','Clara.Common.store.Users','Protocols','Contracts','UserReportCriterias','ReportCriterias','ComboCriterias'],
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