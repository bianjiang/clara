Ext.define('Clara.Admin.store.LockedForms', {
    extend: 'Ext.data.Store',
    requires: 'Clara.Admin.model.LockedForm',    
    model: 'Clara.Admin.model.LockedForm',
    autoLoad: false
});