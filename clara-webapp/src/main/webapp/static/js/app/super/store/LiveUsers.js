Ext.define('Clara.Super.store.LiveUsers', {
    extend: 'Ext.data.Store',
    requires: 'Clara.Super.model.LiveUsers',    
    model: 'Clara.Super.model.LiveUsers',
    autoLoad: true
});