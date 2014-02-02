Ext.define('Clara.Reports.store.Contracts', {
    extend: 'Ext.data.Store',
    requires: 'Clara.Reports.model.Contract',    
    model: 'Clara.Reports.model.Contract',
    autoLoad: false
});