Ext.define('Clara.Reports.store.UserReportDisplayFields', {
    extend: 'Ext.data.Store',
    requires: 'Clara.Reports.model.UserReportDisplayField',
    model: 'Clara.Reports.model.UserReportDisplayField',
    autoLoad: false,
    proxy: {
        type: 'ajax',
        url: appContext+'/null',	// change dynamically based on selected report
        reader: {
            type: 'json',
            root: 'data',
            idProperty:'id'
        }
    }
});