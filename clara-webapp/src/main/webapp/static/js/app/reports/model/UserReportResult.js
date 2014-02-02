Ext.define('Clara.Reports.model.UserReportResult', {
    extend: 'Ext.data.Model',
    fields: ['id', 
             
             {name:'created', mapping:'uploadedFile.created',type: 'date',convert: function(v,rec){
            	 return new Date(v);
			 }}],
    proxy: {
        type: 'ajax',
        //url: appContext+'/static/js/app/reports/data/TEST-userreportresult-list.json',
        url: appContext+'/ajax/reports/{report-template-id}/list-results',	// will be set dynamically
        reader: {
            type: 'json',
            root: 'data'
        }
    }
});