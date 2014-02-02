Ext.define('Clara.Meeting.controller.Meeting', {
    extend: 'Ext.app.Controller',
    stores: ['MeetingStore'],
    refs: [
           {ref: 'agendaItemPanel', selector: 'agendaitemgridpanel'},
           
           ],
    
    
    init: function() {
    	var me = this;
    	
    	// Start listening for controller events
    	// me.on("agendaItemsUpdated", function(){
    	// 	me.onAgendaItemUpdate();
    	// });

        // Start listening for events on views
        me.control({});
    },
    
    loadingMask: new Ext.LoadMask(Ext.getBody(), {msg:"Please wait..."}),
    selectedAgendaItem: null
    
    
});
