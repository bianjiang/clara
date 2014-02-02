Ext.define('Clara.DetailDashboard.store.Letters', {
    extend: 'Ext.data.Store',
    requires: 'Clara.DetailDashboard.model.History',    
    model: 'Clara.DetailDashboard.model.History',
    autoLoad:false,
    groupField: '',
    sorters: [{property:'timestamp', direction:'DESC'}],
    sortOnLoad: true,
    remoteSort: false,
    loadLetters: function(objType){
    	var me = this;
    	me.getProxy().url = appContext+"/ajax/history/history.xml?id="+claraInstance.id+"&type="+objType+"&filter=letter";
    	me.load();
    }
});