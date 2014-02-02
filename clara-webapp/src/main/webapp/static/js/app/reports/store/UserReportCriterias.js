Ext.define('Clara.Reports.store.UserReportCriterias', {
    extend: 'Ext.data.Store',
    requires: 'Clara.Reports.model.UserReportCriteria',    
    model: 'Clara.Reports.model.UserReportCriteria',
    autoLoad: false
});