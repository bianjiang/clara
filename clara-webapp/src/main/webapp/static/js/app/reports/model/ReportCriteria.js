Ext.define('Clara.Reports.model.ReportCriteria', {
    extend: 'Ext.data.Model',
    fields: [{name:'id', mapping:'fieldIdentifier'}, {name:'xtype', mapping:'fieldXType'}, {name:'fieldname', mapping:'fieldIdentifier'}, {name:'fieldlabel', mapping:'fieldDisplayName'},
             {
    			name:'operators',
    			mapping:'allowedOperators'
             }
],
    proxy: {
        type: 'ajax',
        url: appContext+'/static/js/app/reports/data/report-criteria.json',
        reader: {
            type: 'json',
            root: 'data',
            idProperty:'id'
        }
    }
});