Ext.define('Clara.Queue.store.ExpeditedExemptReviewers', {
    extend: 'Ext.data.Store',
    requires: 'Clara.Queue.model.ExpeditedExemptReviewer',    
    model: 'Clara.Queue.model.ExpeditedExemptReviewer',
    autoLoad: true,
    sorters:[{
        property: 'lastname',
        direction: 'ASC'
      }]
});