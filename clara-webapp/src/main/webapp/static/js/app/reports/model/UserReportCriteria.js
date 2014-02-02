Ext.define('Clara.Reports.model.UserReportCriteria', {
    extend: 'Ext.data.Model',
    fields: ['id',
             {name:'xtype', mapping:'reportCriteria.fieldXType'},
             {name:'fieldname', mapping:'reportCriteria.fieldIdentifier'},
             {name:'fieldlabel', mapping:'reportCriteria.fieldDisplayName'},
             {name:'displayvalue', mapping:'reportCriteria.displayValue'},
             {name:'value', mapping:'reportCriteria.value'},
             {name:'operator', mapping:'reportCriteria.operator'}],
    proxy: {
        type: 'ajax',
        url: appContext+'/null',	// change dynamically based on selected report
        reader: {
            type: 'json',
            root: 'data',
            idProperty:'id'
        }
    }
});