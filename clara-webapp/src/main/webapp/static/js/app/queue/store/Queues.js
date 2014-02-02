Ext.define('Clara.Queue.store.Queues', {
    extend: 'Ext.data.Store',
    requires: 'Clara.Queue.model.Queue',    
    model: 'Clara.Queue.model.Queue',
    autoLoad: false,
    sorters:[{
        property: 'name',
        direction: 'ASC'
      }]
});