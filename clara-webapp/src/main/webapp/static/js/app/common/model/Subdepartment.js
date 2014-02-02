Ext.define('Clara.Common.model.Subdepartment', {
    extend: 'Ext.data.Model',
    fields: [{name:'id'},
             {name:'departmentId'},
			    {name:'sapCode'},
			    {name:'name'}],
    proxy: {
        type: 'ajax',
        url: appContext + '/ajax/colleges/list',
        reader: {
            type: 'json',
			idProperty: 'id'
        }
    }
});