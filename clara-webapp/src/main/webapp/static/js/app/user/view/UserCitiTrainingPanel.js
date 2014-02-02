Ext.define('Clara.User.view.UserCitiTrainingPanel', {
	extend: 'Ext.grid.Panel',
	alias: 'widget.usercititrainingpanel',
	autoScroll: true,
    border: false,
    stripeRows: true,
    viewConfig:{
    	getRowClass: function(r, index) {
            var c = r.get('dateCompletionExpires');
            var dt = new Date();
            if (c < dt) {
                return 'summary-bar-status-WARN';
            }
        }
    },
	initComponent: function() { 
        this.columns = [
{header: 'Name', sortable: true, dataIndex: 'nameOfCurriculum'},
{header: 'Group', sortable: true, dataIndex: 'group'},
{header: 'Desc', sortable: true, dataIndex: 'stageDescription'}  ,
{header: 'Date Earned', sortable: true, dataIndex: 'dateCompletionEarned', xtype: 'datecolumn', format: 'm/d/Y'},
{header: 'Score', sortable: true, dataIndex: 'learnerScore',renderer:function(v,p,r){ return r.get("learnerScore")+" ("+r.get("passingScore")+" to pass)"; }},
{header: 'Expires', sortable: true, dataIndex: 'dateCompletionExpires', xtype: 'datecolumn', format: 'm/d/Y'}
        ];
		this.store = new Ext.data.Store({
			autoLoad: true,
			header :{
		           'Accept': 'application/json'
		       },
			proxy: new Ext.data.HttpProxy({
				url: appContext + '/ajax/users/'+profile.id+'/citimember', //'+profile.id+'
				method:'GET',
				reader: {
					type:'json',
					root:'data',
					idProperty: 'id'
				}
			}),
			
			fields:[
			    {name:'id'},
				{name:'registrationDate', type: 'date'},
				{name:'nameOfCurriculum'},
				{name:'group',mapping:'group'},
				{name:'stageNumber'},
				{name:'stageDescription'},
				{name:'completionReportNumber'},
				{name:'dateCompletionEarned', type: 'date'},
				{name:'learnerScore'},
				{name:'passingScore'},
				{name:'dateCompletionExpires', type: 'date'}
			]
		});
		this.callParent();
	}
});