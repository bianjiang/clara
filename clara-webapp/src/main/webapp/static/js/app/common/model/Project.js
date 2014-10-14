Ext.define('Clara.Common.model.Project', {
    extend: 'Ext.data.Model',
    fields: [{name:'id'},
             {name:'prn'},
             {name:'identifier',mapping:'prn'},
			    {name:'fundingAgency'},
			    {name:'title',mapping:'grantTitle'},
			    {name:'piName'},
			    {name:'status'}],
    proxy: {
        type: 'ajax',
        url: appContext + '',	//Dynamic, there is no "list" for projects
        reader: {
            type: 'json',
			idProperty: 'id',
			root:'data'
        }
    }
});