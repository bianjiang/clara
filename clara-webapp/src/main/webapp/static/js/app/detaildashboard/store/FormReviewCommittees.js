Ext.define('Clara.DetailDashboard.store.FormReviewCommittees', {
    extend: 'Ext.data.Store',
    requires: 'Clara.DetailDashboard.model.FormReviewCommittee',    
    model: 'Clara.DetailDashboard.model.FormReviewCommittee',
    autoLoad:false,
    loadFormReviewCommittees: function(form){
    	
    	var me = this,
    	    url = appContext+'/ajax/'+claraInstance.type+'s/'+claraInstance.id+"/"+claraInstance.type+"-forms/"+form.get("formId")+"/get-user-role-list?userId="+claraInstance.user.id;
    	
    	clog("loadFormReviewCommittees(): "+url);
    	
    	me.getProxy().url = url;
    	me.load();
    }
});