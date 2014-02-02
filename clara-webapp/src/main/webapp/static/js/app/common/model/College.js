Ext.define('Clara.Common.model.College', {
    extend: 'Ext.data.Model',
    fields: [{name:'id'},
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