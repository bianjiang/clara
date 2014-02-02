Ext.define('Clara.Queue.store.AssignedReviewers', {
    extend: 'Ext.data.Store',
    requires: 'Clara.Queue.model.AssignedReviewer',    
    model: 'Clara.Queue.model.AssignedReviewer',
    autoLoad: false,
    sorters:[{
        property: 'lastname',
        direction: 'ASC'
      }]
});