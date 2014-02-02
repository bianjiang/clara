Ext.define('Clara.DetailDashboard.store.FormReviewStatus', {
    extend: 'Ext.data.Store',
    requires: 'Clara.DetailDashboard.model.FormReviewStatus',    
    model: 'Clara.DetailDashboard.model.FormReviewStatus',
    autoLoad:false,
    loadFormReviewStatus: function(form){
    	
    	var me = this,
    	    url = appContext+"/ajax/"+claraInstance.type+"s/"+claraInstance.id+"/"+claraInstance.type+"-forms/"+form.get("formId")+"/review/committee-statuses/list.xml";
    	 
    	clog("loadFormReviewStatus(): "+url);
    	
    	me.getProxy().url = url;
    	me.load();
    }
});