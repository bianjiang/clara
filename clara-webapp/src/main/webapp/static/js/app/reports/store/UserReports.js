Ext.define('Clara.Reports.store.UserReports', {
    extend: 'Ext.data.Store',
    requires: 'Clara.Reports.model.UserReport',    
    model: 'Clara.Reports.model.UserReport',
    autoLoad: true
});