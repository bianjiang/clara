Ext.define('Clara.DetailDashboard.store.FormReviewStatusDetails', {
    extend: 'Ext.data.Store',
    requires: 'Clara.DetailDashboard.model.FormReviewStatusDetail',    
    model: 'Clara.DetailDashboard.model.FormReviewStatusDetail',
    autoLoad:false,
    loadFormReviewStatusDetail: function(form, committee){
    	
    	var me = this,
    		url = appContext+"/ajax/"+claraInstance.type+"s/"+claraInstance.id+"/"+claraInstance.type+"-forms/"+form.get("formId")+"/review/committee-statuses/"+committee+"/list";
 	 
    	clog("loadFormReviewStatusDetail(): "+url);
    	
    	me.getProxy().url = url;
    	me.load();
    }
});