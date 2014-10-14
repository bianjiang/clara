var agendaGlobals = {};

Ext.Loader.setPath('Clara.Common', appContext+'/static/js/app/common');
Ext.Loader.setPath('Clara.DetailDashboard', appContext+'/static/js/app/detaildashboard');
Ext.Loader.setPath('Ext.ux', appContext+'/static/js/ext4/ux');

Ext.application({
    name: 'Clara.Agenda', 
    appFolder:appContext + '/static/js/app/agenda',

    models:['IrbReviewer','Agenda','AgendaItem','Clara.Common.model.IrbRoster','Clara.Common.model.User','Clara.DetailDashboard.model.History'],
    stores:['IrbReviewers','AssignedIrbReviewers','Agendas','AgendaItems','Clara.Common.store.IrbRosters','Clara.Common.store.Users','Clara.Agenda.store.CommentHistory'],
    requires:['Clara.Agenda.view.AgendaItemReviewerWindow','Clara.Agenda.view.AgendaRosterWindow','Clara.Agenda.view.NewAgendaWindow','Clara.Agenda.view.AgendaGridPanel','Clara.Agenda.view.AgendaItemGridPanel','Clara.DetailDashboard.view.HistoryPanel','Clara.Agenda.view.CommentHistoryWindow'],
    controllers: ['Agenda','AgendaItem','AgendaRoster'],
    
    autoCreateViewport: true,
    enableQuickTips: true,
    launch: function() {
    	Clara.Agenda.app = this;
    	Ext.require('Ext.ux.grid.GridPrinter');
        // This is fired as soon as the page is ready
    	clog("Ext.app launch.");
    },
    listeners:{
    }
});