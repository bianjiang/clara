Ext.define('Clara.Common.model.StudySite', {
    extend: 'Ext.data.Model',
    fields: [{name:'id', mapping: 'id'},
     	    {name:'siteName', mapping:'siteName'},
    	    {name:'siteFullname', mapping:'siteFullname'},
    	    {name:'address', mapping:'address'},
    	    {name:'approved', mapping:'approved'},
    	    {name:'fwaObtained'},
    	    {name:'fwaNumber'},
    	    {name:'city', mapping:'city'},
    	    {name:'state', mapping:'state'},
    	    {name:'zip', mapping:'zip'},
    	    {name:'common', mapping:'common'}],
    proxy: {
        type: 'ajax',
        url: appContext + '/ajax/sites/list',
        reader: {
            type: 'json',
			idProperty: 'id',
			root: 'sites'
        }
    }
});