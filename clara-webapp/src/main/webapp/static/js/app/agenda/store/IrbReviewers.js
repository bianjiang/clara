Ext.define('Clara.Agenda.store.IrbReviewers', {
    extend: 'Ext.data.Store',
    requires: 'Clara.Agenda.model.IrbReviewer',    
    model: 'Clara.Agenda.model.IrbReviewer',
    autoLoad: false
});