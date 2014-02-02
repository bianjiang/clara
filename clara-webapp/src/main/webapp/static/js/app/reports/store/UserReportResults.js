Ext.define('Clara.Reports.store.UserReportResults', {
    extend: 'Ext.data.Store',
    requires: 'Clara.Reports.model.UserReportResult',    
    model: 'Clara.Reports.model.UserReportResult',
    autoLoad: false
});