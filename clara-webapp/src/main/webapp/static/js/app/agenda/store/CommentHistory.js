Ext.define('Clara.Agenda.store.CommentHistory', {
    extend: 'Ext.data.Store',
    requires: 'Clara.DetailDashboard.model.History',    
    model: 'Clara.DetailDashboard.model.History',
    autoLoad:false,
    groupField: '',
    remoteGroup: false,
    sorters: [{property:'timestamp', direction:'DESC'}],
    sortOnLoad: true,
    remoteSort: false,
    loadHistory: function(agendaId){
    	clog("loadHistory called for "+agendaId);
    	var me = this;
    	me.getProxy().url = appContext+"/ajax/history/history.xml?id="+agendaId+"&type=edu.uams.clara.webapp.protocol.domain.irb.AgendaItem";
    	me.load();
    }
});