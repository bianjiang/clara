Ext.Loader.setPath('Clara.Common', appContext+'/static/js/app/common');
Ext.Loader.setPath('Ext.ux', appContext+'/static/js/ext4/ux');

Ext.application({
    name: 'Clara.Queue', 
    appFolder:appContext + '/static/js/app/queue',

    models:['Clara.Queue.model.ExpeditedExemptReviewer','Clara.Queue.model.AssignedReviewer','Clara.Queue.model.Reviewer','Clara.Queue.model.QueueItem','Clara.Agenda.model.IrbReviewer','Clara.Agenda.model.Agenda','Clara.Common.model.IrbRoster','Clara.Common.model.User'],
    stores:['Clara.Queue.store.ExpeditedExemptReviewers','Clara.Queue.store.AssignedReviewers','Clara.Queue.store.Reviewers','Clara.Queue.store.QueueItems','Clara.Agenda.store.IrbReviewers','Clara.Agenda.store.AssignedIrbReviewers','Clara.Agenda.store.Agendas','Clara.Common.store.IrbRosters','Clara.Common.store.Users'],
    requires:['Clara.Queue.view.QueueFilterToolbar'],
    controllers: ['Queue','QueueItem','QueueAssign','QueueIRBAssign'],
    
    autoCreateViewport: true,
    enableQuickTips: true,
    launch: function() {
    	Clara.Application = this;
    	Clara.Application.QueueAssignController = this.getController("QueueAssign");
    	
    	Clara.Application.fromQueue = (typeof getUrlVars()["fromQueue"] == "undefined")?null:getUrlVars()["fromQueue"];
    	Ext.require('Ext.ux.grid.GridPrinter');
    	Ext.tip.QuickTipManager.init();
    	clog("Ext.app launch.");
    }
});