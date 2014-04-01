Ext.define('Clara.Reports.model.UserReport', {
    extend: 'Ext.data.Model',
    fields: ['id', 'description', 'typeId','typeDescription','globalOperator','status',
             
             {name:'created', type: 'date',convert: function(v,rec){
            	 return new Date(v);
			 }},
			 {name:'cron',mapping:'cronExpression'},
			 {name:'scheduleType'}
			 
			 
			 ],
             
             hasMany: [{
	        	 model: 'Clara.Reports.model.UserReportCriteria',
	        	 name: 'userReportCriteria',
	        	 associationKey: 'reportCriterias',
	        	 reader: {
	        		 root:'reportCriterias'
	        	 }
	         },{
	        	 model:'Clara.LetterBuilder.model.Recipient',
	        	 name:'recipients',
	        	 accociationKey:'recipients',
	        	 reader: {
	        		 root:'recipients'
	        	 }
	         }],
    proxy: {
        type: 'ajax',
        url: appContext+'/ajax/reports/list?userId='+claraInstance.user.id,
        reader: {
            type: 'json',
            root: 'data'
        }
    }
});