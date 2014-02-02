Ext.define('Clara.DetailDashboard.store.History', {
    extend: 'Ext.data.Store',
    requires: 'Clara.DetailDashboard.model.History',    
    model: 'Clara.DetailDashboard.model.History',
    autoLoad:false,
    groupField: '',
    remoteGroup: false,
    sorters: [{property:'timestamp', direction:'DESC'}],
    sortOnLoad: true,
    remoteSort: false,
    loadHistory: function(objType){
    	var me = this;
    	me.getProxy().url = appContext+"/ajax/history/history.xml?id="+claraInstance.id+"&type="+objType;
    	me.load();
    }
});