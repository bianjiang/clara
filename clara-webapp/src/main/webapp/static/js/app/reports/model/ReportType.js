Ext.define('Clara.Reports.model.ReportType', {
    extend: 'Ext.data.Model',
    fields: ['id', 'type', 'category','environments', 'formid','icnCls','description', 'url','forceQueue'],
    proxy: {
        type: 'ajax',
        url: appContext+'/static/js/app/reports/data/report-types.json',
        reader: {
            type: 'json',
            root: 'results',
            idProperty:'id'
        }
    }
});