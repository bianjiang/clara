Ext.define('Clara.Reports.store.ComboCriterias', {
    extend: 'Ext.data.Store',
    requires: 'Clara.Reports.model.ComboCriteria',    
    model: 'Clara.Reports.model.ComboCriteria',
    autoLoad: true
});