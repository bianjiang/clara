Ext.define('Clara.User.store.LockedForms', {
    extend: 'Ext.data.Store',
    requires: 'Clara.User.model.LockedForm',    
    model: 'Clara.User.model.LockedForm',
    autoLoad: true
});