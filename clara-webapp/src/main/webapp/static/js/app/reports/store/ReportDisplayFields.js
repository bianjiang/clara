Ext.define('Clara.Reports.store.ReportDisplayFields', {
    extend: 'Ext.data.Store',
    requires: 'Clara.Reports.model.DisplayField',    
    model: 'Clara.Reports.model.DisplayField',
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