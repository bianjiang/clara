Ext.define('Clara.Common.store.Subdepartments', {
    extend: 'Ext.data.Store',
    requires: 'Clara.Common.model.Subdepartment',    
    model: 'Clara.Common.model.Subdepartment',
    autoLoad: false
});