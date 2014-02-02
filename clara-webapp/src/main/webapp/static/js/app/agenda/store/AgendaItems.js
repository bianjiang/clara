Ext.define('Clara.Agenda.store.AgendaItems', {
    extend: 'Ext.data.Store',
    requires: 'Clara.Agenda.model.AgendaItem',    
    model: 'Clara.Agenda.model.AgendaItem',
    autoLoad: false
});