Ext.define('Clara.Common.model.Department', {
    extend: 'Ext.data.Model',
    fields: [{name:'id'},
             {name:'collegeId'},
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