Ext.define('Clara.Reports.model.UserReportDisplayField', {
    extend: 'Ext.data.Model',
    fields: ['id',
        {name:'fieldname', mapping:'reportField.fieldIdentifier'},
        {name:'fieldlabel', mapping:'reportField.fieldDisplayName'},
        {name:'order', mapping:'reportField.order'}]
});