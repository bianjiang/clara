Ext.define('Clara.DetailDashboard.store.RelatedProtocols', {
    extend: 'Clara.Common.store.Protocols',
    groupField: 'id',
    groupDir:'DESC',
    autoLoad: false,
    sorters:[{
        property: 'id',
        direction: 'DESC'
    }],
    loadRelatedProtocols: function(){
    	var me = this,
    	oldUrl = me.getProxy().url,
    	oldMethod = me.getProxy().actionMethods.read;
    	
    	me.getProxy().url = appContext+"/ajax/"+claraInstance.type+"s/related-protocol/list.xml?"+claraInstance.type+"Id="+claraInstance.id;
    	me.getProxy().actionMethods.read = "GET";
    	
    	me.load({callback: function(){
    		me.getProxy().url = oldUrl;
    		me.getProxy().actionMethods.read = oldMethod;
    	}});
    	
    	
    }
});