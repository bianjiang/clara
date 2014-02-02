Ext.define('Clara.Admin.store.Things', {
    extend: 'Ext.data.Store',
    requires: 'Clara.Admin.model.Thing',    
    model: 'Clara.Admin.model.Thing',
    autoLoad: false
});