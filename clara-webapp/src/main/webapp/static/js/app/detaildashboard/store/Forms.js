Ext.define('Clara.DetailDashboard.store.Forms', {
    extend: 'Ext.data.Store',
    requires: 'Clara.DetailDashboard.model.Form',    
    model: 'Clara.DetailDashboard.model.Form',
    autoLoad:false,
    loadForms: function(){
    	
    	var me = this,
    	    type = claraInstance.type,
    	    id = claraInstance.id,
    	    url= appContext+"/ajax/"+type+"s/"+id+"/"+type+"-forms/list.xml";
    	
    	clog("loadForms(): "+url);
    	
    	me.getProxy().url = url;
    	me.load();
    }
});