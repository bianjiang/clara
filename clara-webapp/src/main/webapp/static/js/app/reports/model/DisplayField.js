Ext.define('Clara.Reports.model.DisplayField', {
    extend: 'Ext.data.Model',
    fields: ['id',
             {name:'fieldname', mapping:'fieldIdentifier'},
             {name:'fieldlabel', mapping:'fieldDisplayName'},
             {name:'order', mapping:'order'}]
});