Ext.define('Clara.Reports.store.ReportCriterias', {
    extend: 'Ext.data.Store',
    requires: 'Clara.Reports.model.ReportCriteria',    
    model: 'Clara.Reports.model.ReportCriteria',
    autoLoad: false
});