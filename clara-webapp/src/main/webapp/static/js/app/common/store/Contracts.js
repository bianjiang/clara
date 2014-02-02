Ext.define('Clara.Common.store.Contracts', {
    extend: 'Ext.data.Store',
    requires: 'Clara.Common.model.Contract',    
    model: 'Clara.Common.model.Contract',
    groupField: 'id',
    groupDir:'DESC',
    autoLoad: false,
    sorters:[{
        property: 'id',
        direction: 'DESC'
    },{
    	property: 'groupedIndex',
        direction: 'ASC'
    }]
});