Ext.define('Clara.Queue.controller.Queue', {
	extend: 'Ext.app.Controller',
	models:['Clara.Queue.model.Queue','Clara.Queue.model.QueueItem'],
	stores:['Clara.Queue.store.Queues','Clara.Queue.store.QueueItems'],
    refs: [
           { ref: 'queues', selector: 'queuepanel'},
           { ref: 'cards', selector: '#queueitemscardpanel'},
           { ref: 'showQueueItemLogButton',selector:'#btnShowQueueItemLog'},
           { ref: 'showHistoryButton', selector: '#btnTBShowHistory'},
           { ref: 'showOnlyMineButton', selector: '#btnTBShowMineOnly'},
           { ref: 'tbQueueReviewStatus', selector:'#fldTBQueueReviewStatus'},
           { ref: 'tbQueueReviewRole', selector:'#fldTBQueueReviewRole'},
           { ref: 'tbQueueFormType', selector:'#fldTBQueueFormType'},
           { ref: 'tbQueueTextFilterField', selector:'#fldTBQueueTextFilterField'},
           { ref:'tbQueueFilter', selector:'#tbQueueFilter'},
		   { ref:'printButton',selector:'#btnPrintItems'}
    ],

	init: function() {
		var me = this;

		// Start listening for controller events
	

		// Start listening for events on views
		me.control({
			'queuepanel':{
        		select:me.onQueueSelect,
        		viewready: me.afterQueueLoad
        	},
        	'#btnPrintItems':{
        		click: me.printQueueItems
        	},
        	'#btnTBShowHistory':{
        		toggle: me.toggleHistoryForSelectedQueue
        	},
        	'#btnTBShowMineOnly':{
        		toggle: function(b,v) { 
        				b.setIconCls(v?"icn-ui-check-box":"icn-ui-check-box-uncheck");
        				me.filterSelectedQueue("isMine", v?"true":""); 
        			}
        	},
        	'#fldTBQueueReviewStatus':{
        		select: function(cb,recs) { me.filterSelectedQueue("formCommitteeStatus", recs[0].get("value")); }
        	},
        	'#fldTBQueueReviewRole':{
        		select: function(cb,recs) { me.filterSelectedQueue("roleName", recs[0].get("value")); }
        	},
        	'#fldTBQueueFormType':{
        		select: function(cb,recs) { me.filterSelectedQueue("formType", recs[0].get("value")); }
        	},
        	'#btnClearQueueFilter':{
        		click: function(){
        			Ext.getCmp("fldTBQueueTextFilterField").setValue("");
        			me.getShowOnlyMineButton().toggle(false);
    				jQuery(".queueitemspanel .x-grid-row").show();
    				me.getTbQueueReviewStatus().select(me.getTbQueueReviewStatus().getStore().getAt(0));
    				me.getTbQueueReviewRole().select(me.getTbQueueReviewRole().getStore().getAt(0));
    				me.getTbQueueFormType().select(me.getTbQueueFormType().getStore().getAt(0));
        			me.filterSelectedQueue(null,null,true);
        		}
        	}
		});
	},

	loadingMask: new Ext.LoadMask(Ext.getBody(), {msg:"Please wait..."}),
	
	selectedQueue: null,
	selectedQueueItem: null,
	activeFilters:[],
	showHistory: false,
	
	afterQueueLoad: function(gp,opts){
		var fromQueue = Clara.Queue.app.fromQueue;
		if (fromQueue != null){
			gp.getStore().each(function(rec){
				if (rec.get("identifier") == fromQueue){
					gp.getSelectionModel().select(rec);
				}
			});

		} else {
			clog("No fromQueue string",fromQueue);
		}
	},
	
	printQueueItems: function(){
		var me = this,
		activeCard = me.getCards().getLayout().getActiveItem();
clog("activecard",activeCard);
		Ext.ux.grid.Printer.title = "Queue: "+me.selectedQueue.get("name");
		Ext.ux.grid.Printer.printAutomatically = false;
    	Ext.ux.grid.Printer.print(activeCard);
	},
	
	toggleHistoryForSelectedQueue: function(btn, pressed){
		btn.setIconCls(pressed?"icn-ui-check-box":"icn-ui-check-box-uncheck");
		this.showHistory = pressed;
		this.filterSelectedQueue(null,null,false);
		this.loadQueue(this.selectedQueue);
	},
	
	filterSelectedQueue: function(filterField, value, clear){
		var me = this, found=false;
		clear = clear || false;
		clog("filterSelectedQueue: "+filterField,value,clear);
		
		
		
		if (clear) {
			me.activeFilters = [];
		} else {
			
			for (var i=0,l=me.activeFilters.length;i<l;i++){
				if (me.activeFilters[i].property == filterField){
					me.activeFilters[i].value = (value == null || value.indexOf("All ") > -1)?"":value;
					found = true;
				}
			}
			if (!found){
				me.activeFilters.push({
					property: filterField,
					value: value
				});
			}
			clog("activeFilters",me.activeFilters);
		}
		var st = Ext.data.StoreManager.lookup('Clara.Queue.store.QueueItems');
		me.getTbQueueTextFilterField().setValue("");
		st.clearFilter();
		st.filterBy(function(rec,id){
			var found = true;
			for (var i=0, l=me.activeFilters.length;i<l;i++){
				if (me.activeFilters[i].value != "" ) found = found && (rec.get(me.activeFilters[i].property) == me.activeFilters[i].value);
			}
			return found;
		});
		
		
	},
	
	onQueueItemsLoaded: function(qitems){
		clog("onQueueItemsLoaded",qitems);
		// Set up filter toolbar combos
		var statuses = [["All Statuses"]],
		types =[["All Types"]],
		roles = [["All Roles"]];

		Ext.each(qitems.collect('formCommitteeStatus',true, true), function(item){
			statuses.push([item]);
		});
		
		Ext.each(qitems.collect('roleName',true, true), function(item){
			roles.push([item]);
		});
		
		Ext.each(qitems.collect('formType',true, true), function(item){
			types.push([item]);
		});
		
		var statusStore = Ext.data.StoreManager.lookup('QueueReviewStatusStore');
		var roleStore = Ext.data.StoreManager.lookup('QueueReviewRoleStore');
		var typeStore = Ext.data.StoreManager.lookup('QueueReviewFormTypeStore');
		
		statusStore.loadData(statuses);
		roleStore.loadData(roles);
		typeStore.loadData(types);
		
		this.getTbQueueReviewStatus().select(this.getTbQueueReviewStatus().getStore().getAt(0));
		this.getTbQueueReviewRole().select(this.getTbQueueReviewRole().getStore().getAt(0));
		this.getTbQueueFormType().select(this.getTbQueueFormType().getStore().getAt(0));

	},
	
	loadQueue: function(rec){
		clog("loadQueue",rec);
		var me = this;
		var type = rec.get("objectType");
		
		var st = Ext.data.StoreManager.lookup('Clara.Queue.store.QueueItems');
		st.removeAll();
		
		Ext.apply(st.proxy.extraParams, {
			queue : rec.get("identifier"),
			objectType : type,
			userId : claraInstance.user.id,
			showHistory: me.showHistory,
		});
		
		if (type.toLowerCase() == "protocol"){
			me.getCards().getLayout().setActiveItem(1);
		} else if (type.toLowerCase() == "contract"){
			me.getCards().getLayout().setActiveItem(2);
		} else {
			me.getCards().getLayout().setActiveItem(0);
			cwarn("loadQueue: Unsupported queue type",type);
		}
		
		st.load({
			callback: function() {
				me.onQueueItemsLoaded(st);
				clog("QueueItems loaded for "+rec.get("identifier"));
			}
		});
	},
	
	onQueueSelect: function(g,rec){
		var me = this;
		clog("onQueueSelect",rec.get("name"),rec.get("identifier"),rec.get("objectType"));
		
		if (rec.get("objectType") == "Contract") me.getShowQueueItemLogButton().show();
		else me.getShowQueueItemLogButton().hide();
		
		me.getTbQueueFilter().setDisabled(false);
		me.getPrintButton().setDisabled(false);
		
		this.selectedQueue = rec;
		this.loadQueue(rec);
	}
	
	
});
