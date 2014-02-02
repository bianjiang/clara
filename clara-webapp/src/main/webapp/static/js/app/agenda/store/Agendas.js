Ext.define('Clara.Agenda.store.Agendas', {
    extend: 'Ext.data.Store',
    requires: 'Clara.Agenda.model.Agenda',    
    model: 'Clara.Agenda.model.Agenda',
    autoLoad: false,
    sorters:[{
        property: 'date',
        direction: 'DESC'
      }]
});