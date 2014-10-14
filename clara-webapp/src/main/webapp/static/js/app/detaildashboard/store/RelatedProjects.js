Ext.define('Clara.DetailDashboard.store.RelatedProjects', {
	extend: 'Ext.data.Store',
    requires: 'Clara.Common.model.Project',    
    model: 'Clara.Common.model.Project',
    autoLoad: false,
    groupField: 'prn',
    groupDir:'DESC',
    autoLoad: false,
    sorters:[{
        property: 'prn',
        direction: 'DESC'
    }],
    loadRelatedProjects: function(){
    	var me = this,
    	oldUrl = me.getProxy().url,
    	oldMethod = me.getProxy().actionMethods.read;
    	
    	me.getProxy().url = appContext+"/ajax/"+claraInstance.type+"s/related-grant/list?"+claraInstance.type+"Id="+claraInstance.id;
    	me.getProxy().actionMethods.read = "GET";
    	
    	me.load({callback: function(){
    		me.getProxy().url = oldUrl;
    		me.getProxy().actionMethods.read = oldMethod;
    	}});
    	
    	
    }
});