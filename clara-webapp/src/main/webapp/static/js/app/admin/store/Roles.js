Ext.define('Clara.Admin.store.Roles', {
    extend: 'Ext.data.Store',
    requires: 'Clara.Admin.model.Role',    
    model: 'Clara.Admin.model.Role',
    autoLoad: true
});