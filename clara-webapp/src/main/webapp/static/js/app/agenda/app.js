var agendaGlobals = {};

Ext.Loader.setPath('Clara.Common', appContext+'/static/js/app/common');
Ext.Loader.setPath('Ext.ux', appContext+'/static/js/ext4/ux');

Ext.application({
    name: 'Clara.Agenda', 
    appFolder:appContext + '/static/js/app/agenda',

    models:['IrbReviewer','Agenda','AgendaItem','Clara.Common.model.IrbRoster','Clara.Common.model.User'],
    stores:['IrbReviewers','AssignedIrbReviewers','Agendas','AgendaItems','Clara.Common.store.IrbRosters','Clara.Common.store.Users'],
    requires:['Clara.Agenda.view.AgendaItemReviewerWindow','Clara.Agenda.view.AgendaRosterWindow','Clara.Agenda.view.NewAgendaWindow','Clara.Agenda.view.AgendaGridPanel','Clara.Agenda.view.AgendaItemGridPanel'],
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