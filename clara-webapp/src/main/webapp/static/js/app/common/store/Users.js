Ext.define('Clara.Common.store.Users', {
    extend: 'Ext.data.Store',
    requires: 'Clara.Common.model.User',    
    model: 'Clara.Common.model.User',
    autoLoad: false
});