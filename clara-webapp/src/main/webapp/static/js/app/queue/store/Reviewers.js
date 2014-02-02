Ext.define('Clara.Queue.store.Reviewers', {
    extend: 'Ext.data.Store',
    requires: 'Clara.Queue.model.Reviewer',    
    model: 'Clara.Queue.model.Reviewer',
    autoLoad: false,
    sorters:[{
        property: 'lastname',
        direction: 'ASC'
      }]
});