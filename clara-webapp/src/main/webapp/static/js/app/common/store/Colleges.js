Ext.define('Clara.Common.store.Colleges', {
    extend: 'Ext.data.Store',
    requires: 'Clara.Common.model.College',    
    model: 'Clara.Common.model.College',
    autoLoad: false
});