Ext.define('Clara.Common.store.Contracts', {
    extend: 'Ext.data.Store',
    requires: 'Clara.Common.model.Contract',    
    model: 'Clara.Common.model.Contract',
    groupField: 'id',
    groupDir:'DESC',
    autoLoad: false,
    remoteSort: true,
    sorters:[{
        property: 'id',
        direction: 'DESC'
    }]
});