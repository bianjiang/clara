Ext.define('Clara.Review.store.ReviewNotes', {
    extend: 'Ext.data.Store',
    requires: 'Clara.Review.model.ReviewNote',    
    model: 'Clara.Review.model.ReviewNote',
    autoLoad:false,
    loadReviewNotes: function(formId){
    	
    	var me = this,
    	    type = claraInstance.type,
    	    id = claraInstance.id,
    	    url= appContext+"/ajax/"+type+"s/" + id + "/"+type+"-forms/"+formId+"/review/committee-comments/list";
    	
    	clog("loadReviewNotes(): "+url);
    	
    	me.getProxy().url = url;
    	me.load();
    }
});