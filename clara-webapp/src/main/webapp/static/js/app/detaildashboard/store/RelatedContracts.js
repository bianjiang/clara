Ext.define('Clara.DetailDashboard.store.RelatedContracts', {
    extend: 'Clara.Common.store.Contracts',
    groupField: 'id',
    groupDir:'DESC',
    autoLoad: false,
    sorters:[{
        property: 'id',
        direction: 'DESC'
    },{
    	property: 'groupedIndex',
        direction: 'ASC'
    }],
    loadRelatedContracts: function(){
    	var me = this,
    	oldUrl = me.getProxy().url,
    	oldMethod = me.getProxy().actionMethods.read;
    	
    	me.getProxy().url = appContext+"/ajax/"+claraInstance.type+"s/related-contract/list.xml?"+claraInstance.type+"Id="+claraInstance.id;
    	me.getProxy().actionMethods.read = "GET";
    	
    	me.load({callback: function(){
    		me.getProxy().url = oldUrl;
    		me.getProxy().actionMethods.read = oldMethod;
    	}});
    	
    	
    }
});