Ext.define('Clara.Admin.controller.UserVisitHistory', {
    extend: 'Ext.app.Controller',
    refs : [ {
		ref : 'piwikUserVisitPanel',
		selector : '#piwikuservisitpanel'
	},
	{
		ref : 'piwikUserVisitDetailPanel',
		selector : '#piwikuservisitdetailpanel'
	}],
    
    init: function() {
        // Start listening for events on views
        this.control({
        	
        	'#piwikuservisitpanel':{
        		itemclick:this.onClickPiwikUserVisit
        	}
        	
        });
    },
    
    onClickPiwikUserVisit: function(gp,rec){
    	clog("PU",rec, rec.actionDetails());
    	this.getPiwikUserVisitDetailPanel().reconfigure(rec.actionDetails());
    },
    
   
    onLaunch: function() {
  
    }
});
