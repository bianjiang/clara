Ext.define('Clara.Common.store.Departments', {
    extend: 'Ext.data.Store',
    requires: 'Clara.Common.model.Department',    
    model: 'Clara.Common.model.Department',
    autoLoad: false
});