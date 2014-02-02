Ext.define('Clara.Reports.store.ReportTypes', {
    extend: 'Ext.data.Store',
    requires: 'Clara.Reports.model.ReportType',    
    model: 'Clara.Reports.model.ReportType',
    autoLoad: true,
    filters:[{
    	filterFn: function(item){
    		// only show report types that are available in the app's environment (development, training, production)
    		return (item.get("environments").indexOf(appEnvironment) > -1);
    	}
    }]
});