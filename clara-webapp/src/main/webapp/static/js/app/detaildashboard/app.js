Ext.Loader.setPath('Clara.Common', appContext+'/static/js/app/common');
Ext.Loader.setPath('Clara.Dashboard', appContext+'/static/js/app/dashboard');
Ext.Loader.setPath('Clara.Documents', appContext+'/static/js/app/documents');
Ext.Loader.setPath('Clara.LetterBuilder', appContext+'/static/js/app/letterbuilder');
Ext.Loader.setPath('Clara.Queue', appContext+'/static/js/app/queue');
Ext.Loader.setPath('Clara.Review', appContext+'/static/js/app/review');
Ext.Loader.setPath('Ext.ux', appContext+'/static/js/ext4/ux');

Ext.application({
    name: 'Clara.DetailDashboard', 
    appFolder:appContext + '/static/js/app/detaildashboard',
    
    models:['Clara.Documents.model.DocumentType','Clara.Documents.model.Document','Clara.DetailDashboard.model.FormReviewStatusDetail','Clara.DetailDashboard.model.FormReviewStatus','Clara.Queue.model.QueueItem','Clara.DetailDashboard.model.Form','Clara.Common.model.User','Clara.DetailDashboard.model.History'],
    stores:['Clara.DetailDashboard.store.RelatedProtocols',
            'Clara.Common.store.Contracts',
            'Clara.Common.store.Protocols',
            'Clara.DetailDashboard.store.RelatedContracts',
            'Clara.Documents.store.DocumentVersions',
            'Clara.Documents.store.DocumentTypes',
            'Clara.Documents.store.Documents',
            'Clara.DetailDashboard.store.FormReviewStatusDetails',
            'Clara.DetailDashboard.store.FormReviewStatus',
            'Clara.Queue.store.AssignedReviewers',
            'Clara.Queue.store.Reviewers',
            'Clara.DetailDashboard.store.Forms',
            'Clara.Common.store.Users',
            'Clara.DetailDashboard.store.History',
            'Clara.DetailDashboard.store.Letters'],
    controllers: ['Clara.Documents.controller.Documents','Clara.Review.controller.ReviewNote','Clara.DetailDashboard.controller.Form','Clara.DetailDashboard.controller.DetailDashboard','Clara.LetterBuilder.controller.LetterBuilder','Clara.Queue.controller.QueueAssign'],
    
    autoCreateViewport: true,
    enableQuickTips: true,
    
    init: function(){
    	clog("Ext.app init.");
    	Clara.DetailDashboard.app = this;
    	Clara.Application = this;
        // This is fired as soon as the page is ready
    	
    	Clara.DetailDashboard.selectedFormId = (typeof getUrlVars()["selectedFormId"] == "undefined")?null:getUrlVars()["selectedFormId"];
    	Ext.require('Ext.ux.grid.GridPrinter');
    	
    },
    
    launch: function() {
    	clog("Ext.app launch.");
    	Clara.Application.DocumentController = this.getController("Clara.Documents.controller.Documents");
    	Clara.Application.FormController = this.getController("Clara.DetailDashboard.controller.Form");
    	Clara.Application.QueueController = this.getController("Clara.Queue.controller.QueueAssign");
    	Clara.Application.ReviewNoteController = this.getController("Clara.Review.controller.ReviewNote");
    	Ext.tip.QuickTipManager.init();
    }
});