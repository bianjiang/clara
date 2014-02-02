Ext.define('Clara.Reports.model.ComboCriteria', {
    extend: 'Ext.data.Model',
    fields: ['comboId', 'value', 'name'],
    proxy: {
        type: 'ajax',
        url: appContext+'/static/js/app/reports/data/combo-criteria.json',
        reader: {
            type: 'json',
            root: 'results',
            idProperty:'id'
        }
    }
});