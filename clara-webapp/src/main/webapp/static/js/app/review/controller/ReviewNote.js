Ext.define('Clara.Review.controller.ReviewNote', {
    extend: 'Ext.app.Controller',
    models: ['Clara.Review.model.ReviewNote'],
    stores: ['Clara.Review.store.ReviewNotes'],
    
    refs: [{ ref: 'reviewNotePanel', selector: 'reviewnotepanel'}],
   
    loadingMask: new Ext.LoadMask(Ext.getBody(), {msg:"Please wait..."}),
    
    selectedFormId: null,
    selectedNote: null,

    init: function() {
    	var me = this;
    	
    	me.control({
    		'reviewnotepanel':{
    			itemdblclick : me.onNoteSelect
    		},
        	'#btnToggleGroupNotesByCommittee':{
        		toggle:me.onToggleGroupNotes
        	},
        	'#btnToggleOnlyShowIRBNotes':{
        		toggle:me.onToggleOnlyIRBNotes
        	},
        	'#btnPrintCommitteeNotes':{
        		click:me.onPrintCommitteeNotes
        	}
    	});
    },
    
    onNoteSelect: function(gp, rec, item){
    	var me = this;
    	clog("note selected",rec);
    	me.selectedNote = rec;
    },
    
    reloadNotes: function(formId){
    	clog("ReviewNote: reloadNotes()",formId);
    	var st = Ext.data.StoreManager.lookup('Clara.Review.store.ReviewNotes');
    	st.loadReviewNotes(formId);
    },
    
    onPrintCommitteeNotes : function() {
		var me = this;
		Ext.ux.grid.Printer.title = "Notes";
		Ext.ux.grid.Printer.printAutomatically = false;
		Ext.ux.grid.Printer.print(me.getReviewNotePanel());
	},
    
	onToggleOnlyIRBNotes: function(btn,pressed){
    	var st = Ext.data.StoreManager.lookup('Clara.Review.store.ReviewNotes');
    	if (pressed){
    		st.filterBy(function(rec){
    			return (rec.get("committee").indexOf("IRB_") > -1);
    		});
    		
    		btn.setIconCls('icn-ui-check-box');
    	} else {
    		st.clearFilter();
    		btn.setIconCls('icn-ui-check-box-uncheck');
    	}
    },
	
	onToggleGroupNotes: function(btn,pressed){
    	var st = Ext.data.StoreManager.lookup('Clara.Review.store.ReviewNotes');
    	if (pressed){
    		st.group("committee");
    		btn.setIconCls('icn-ui-check-box');
    	} else {
    		st.clearGrouping();
    		btn.setIconCls('icn-ui-check-box-uncheck');
    	}
    }
    
});