Ext.define('Clara.Common.store.IrbRosters', {
    extend: 'Ext.data.Store',
    requires: 'Clara.Common.model.IrbRoster',    
    model: 'Clara.Common.model.IrbRoster',
    groupField:'irbRoster',
    autoLoad: false
});