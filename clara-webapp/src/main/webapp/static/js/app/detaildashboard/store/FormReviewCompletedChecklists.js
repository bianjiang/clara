Ext.define('Clara.DetailDashboard.store.FormReviewCompletedChecklists', {
    extend: 'Ext.data.Store',
    requires: 'Clara.DetailDashboard.model.FormReviewCompletedChecklist',    
    model: 'Clara.DetailDashboard.model.FormReviewCompletedChecklist',
    autoLoad:false,
    groupField: '',
    remoteGroup: false,
    sorters: [{property:'modified', direction:'DESC'}],
    sortOnLoad: true,
    remoteSort: false,
    loadChecklists: function(form, committee){
    	
    	var me = this,
    		url = appContext+"/ajax/"+claraInstance.type+"s/"+claraInstance.id+"/"+claraInstance.type+"-forms/"+form.get("formId")+"/review/checklists/get?committee="+committee;
 	 
    	me.getProxy().url = url;
    	me.load();
    }
});