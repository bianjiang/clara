Ext.ns('Clara.BudgetBuilder');

Clara.BudgetBuilder.MessageBus.on('beforebudgetload', function(){
	////cdebug("Budget gonna load, better clear the history (if available)..");
	Clara.BudgetBuilder.RemoveHistoryItems();
});

Clara.BudgetBuilder.MessageBus.on('afterbudgetsave', function(){
	////cdebug("afterbudgetsave: AddHistoryItem");
	Clara.BudgetBuilder.AddHistoryItem();
});

Clara.BudgetBuilder.MessageBus.on('budgetloaded', function(){
	Clara.BudgetBuilder.SaveAction = "Opened Budget";
	Clara.BudgetBuilder.AddHistoryItem();
});

Clara.BudgetBuilder.RemoveHistoryItems = function(numberToRemove){
	var keystoremove=[];
	for (var i=0;i<localStorage.length;i++){
		var key = localStorage.key(i);
		if (key.indexOf("ClaraBBHistory_") == 0){
			keystoremove.push(key);
		}
	}
	keystoremove.sort();	// to make sure oldest history first
	var removeCount = (numberToRemove != null && numberToRemove < keystoremove.length)?numberToRemove:keystoremove.length;
	for (var i=0;i<removeCount;i++){
		localStorage.removeItem(keystoremove[i]);
	}
};

Clara.BudgetBuilder.AddHistoryItem = function(){
	////cdebug("Updating history..");
	var bXML = budget.toXML();
	if (bXML.length > 4496000 ){
		cwarn("LARGE BUDGET XML. Clearing local history.");
		localStorage.clear();
	} 

	var id="ClaraBBHistory_"+pad(localStorage.length+1, 10);
	var historyItem = {
			id: id,
			timestamp: new Date(),
			action:Clara.BudgetBuilder.SaveAction,
			xml:budget.toXML()
	};
	try{
		localStorage.setItem(id, JSON.stringify(historyItem));
	} catch (e) {
		cwarn(e);
		//if (e == QUOTA_EXCEEDED_ERR){
			localStorage.clear();							// Just clear the whole thing and start with a new set of history items
			// Clara.BudgetBuilder.RemoveHistoryItems(1);		// Deletes history, one item at a time, until theres enough space to save.
			Clara.BudgetBuilder.AddHistoryItem();			// Try to save (RECURSION BAHHHHMMMM!!!!!)
		//}
	}



};

Clara.BudgetBuilder.RevertToHistoryItem = function(id){
	var xml = localStorage.getItem(id);
	budget.fromXML(xml);
	Clara.BudgetBuilder.MessageBus.fireEvent('historyreverted'); 
	budget.save();
	Clara.BudgetBuilder.SaveAction = "Revert";
	Clara.BudgetBuilder.AddHistoryItem();
	
	// remove all items "below" this item in history
	var keystoremove=[];
	for (var i=0;i<localStorage.length;i++){
		var key = localStorage.key(i);
		if (key.indexOf("ClaraBBHistory_") == 0 && key > id){
			keystoremove.push(key);
		}
	}
	for (var i=0;i<localStorage.length;i++){
		localStorage.removeItem(keystoremove[i]);
	}
	
};

Clara.BudgetBuilder.HistoryWindow = Ext.extend(Ext.Window, {
	id: 'winHistory',
	xtype: 'panel',
	selectedRevertId:'',
	modal:true,
	width:280,
	height:300,
    title: 'History',
    layout: 'fit',
	constructor:function(config){		
		Clara.BudgetBuilder.HistoryWindow.superclass.constructor.call(this, config);
	},
	initComponent: function() {
		var config = {
				buttons:[{
					text: 'Clear',
					iconCls:'icn-bin',
	                id: 'btnClearHistory',
	                handler:function(){
						Clara.BudgetBuilder.RemoveHistoryItems();
						Clara.BudgetBuilder.SaveAction = "Clear History";
						Clara.BudgetBuilder.AddHistoryItem();
						Ext.getCmp("winHistory").close();
					}
				},{
					text: 'Revert',
	                iconCls:'icn-arrow-circle',
	                id: 'btnRevertBudget',
	                disabled:true,
	                handler:function(){
						Ext.Msg.show({
						   title:'Revert budget?',
						   msg: 'The budget will be reverted to a previous state, which cannot be undone. Are you sure you want to do this?',
						   buttons: Ext.Msg.YESNOCANCEL,
						   fn: function(btn){
							 var rid = Ext.getCmp("winHistory").selectedRevertId;
							 if (btn == 'yes' && rid != ''){
								Clara.BudgetBuilder.RevertToHistoryItem(rid);
							 } 
							 Ext.getCmp("winHistory").close();
						   },
						   icon: Ext.MessageBox.WARNING
						});
						
					}
				},{
					text: 'Cancel',
	                id: 'btnCancelRevertBudget',
	                handler:function(){
						Ext.getCmp("winHistory").close();
					}
				}],
				items: [{
					xtype:'clarahistorypanel'
				}]
		};
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		Clara.BudgetBuilder.HistoryWindow.superclass.initComponent.apply(this, arguments);
	}
});
Ext.reg('clarahistorywindow', Clara.BudgetBuilder.HistoryWindow);

Clara.BudgetBuilder.HistoryPanel = Ext.extend(Ext.Panel, {
	id: 'history-panel',
	xtype: 'panel',
	border:false,
    layout: 'border',
	constructor:function(config){		
		Clara.BudgetBuilder.HistoryPanel.superclass.constructor.call(this, config);
		Clara.BudgetBuilder.MessageBus.on('afterhistoryupdated', this.onBudgetSaved, this);
	},
	historyStore: new Ext.data.ArrayStore({
		sortInfo:{field:'timestamp',direction:'ASC'},
		fields:[{name:'index'},{name:'id'},{name:'timestamp', dateFormat: 'n/j h:ia'},{name:'action'},{name:'xml'}]
	}),
	getHistoryArray: function(){
		var historyArray = [];
		
		for (var i=0;i<localStorage.length;i++){
			var key = localStorage.key(i);
			if (key.indexOf("ClaraBBHistory_") == 0){
				var historyItem = JSON.parse(localStorage.getItem(key));
				historyArray.push([i,historyItem.id, historyItem.timestamp, historyItem.action, historyItem.xml]);
			}
		}
		return historyArray;
	},
	onBudgetSaved: function(){
		this.reloadHistory();
	},
	reloadHistory: function(){
		this.historyStore.removeAll();
		this.historyStore.loadData(this.getHistoryArray());
	},
	initComponent: function() {
		var config = {
				items: [{
					     xtype: 'grid',
					     deferRowRender:false,
					     viewConfig: {
					         forceFit: true,
					         emptyText: 'No local history found.',
					         headersDisabled:true,
					         getRowClass: function(record,index,rp,st){
								return (index == st.getCount()-1)?"history-row-current":"history-row";
							 }
					      },
					      listeners: {
					          afterrender: function(grid) {
					              grid.getView().el.select('.x-grid3-header').setStyle('display',    'none');
					              grid.getSelectionModel().selectLastRow();
					              grid.getView().focusRow(grid.getStore().getCount()-1);
					          },
					      	  rowclick: function(grid,index,e){
					        	  if (index != grid.getStore().getCount()-1){
					        		  Ext.getCmp("btnRevertBudget").setDisabled(false);
					        		  Ext.getCmp("winHistory").selectedRevertId = grid.getStore().getAt(index).data.id;
					        	  } else {
					        		  Ext.getCmp("btnRevertBudget").setDisabled(true);
					        		  Ext.getCmp("winHistory").selectedRevertId = "";
					        	  }
					          }
					      },
					     stripeRows:true,
					     loadMask:true,
					     region: 'center',
					     border: false,
					     store:this.historyStore,
					     id: 'gpHistory',
					     columns: [
								new Ext.grid.RowNumberer(),{
								    xtype: 'gridcolumn',
								    dataIndex: 'action',
								    header: 'Action',
								    sortable: false
								}
					     ]
					    }
					 ]
		};
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		Clara.BudgetBuilder.HistoryPanel.superclass.initComponent.apply(this, arguments);
		this.reloadHistory();
	}
});
Ext.reg('clarahistorypanel', Clara.BudgetBuilder.HistoryPanel);


Clara.BudgetBuilder.VersionsWindow = Ext.extend(Ext.Window, {
	id: 'winBudgetVersions',
	xtype: 'panel',
	modal:true,
	width:380,
	height:300,
    title: 'Compare to another version..',
    iconCls:'icn-edit-diff',
    layout: 'fit',
	constructor:function(config){		
		Clara.BudgetBuilder.VersionsWindow.superclass.constructor.call(this, config);
	},
	selectedVersionId:-1,
	initComponent: function() {
		var t = this;
		var config = {
				buttons:[{
					text: 'Compare',
					id:'btnCompare',
	                iconCls:'icn-arrow-circle',
	                disabled:true,
	                handler:function(){
						if (t.selectedVersionId > 0) {
							Ext.MessageBox.show({
								msg: 'Comparing..',
								progressText:'Comparing versions. This may take a few moments...',
								width:300,
								wait:true
							});
							// make read only
							Clara.BudgetBuilder.canEdit = function() { return false; }; // for most of the UI
							Clara.BudgetBuilder.AllowExternalCostEditing = false;		// for expenses
							Ext.getCmp("btnAddEpoch").setDisabled(true);
							Ext.getCmp("btnUploadToDocs").setDisabled(true);
							Ext.getCmp("btnTools").setDisabled(true);
							
							//ui update (header)
							jQuery("body").css('background-image', 'url('+appContext+'/static/images/bg_budget_locked.gif)');
							jQuery(".clara-budget-protocol-info h1").append(" (<span style='font-weight:100;'>Comparing budget changes in read-only mode.</span>) &nbsp;<a href='javascript:location.reload();'><span style='color:white;font-weight:800;'>Go back to current version.</span></a>");
							
							// load the new budget w/diffs
							budget.load(t.selectedVersionId);
						}
					}
				},{
					text: 'Cancel',
	                handler:function(){
						t.close();
					}
				}],
				items: [{
					xtype:'clarabudgetversionspanel'
				}]
		};
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		Clara.BudgetBuilder.VersionsWindow.superclass.initComponent.apply(this, arguments);
	}
});
Ext.reg('clarabudgetversionswindow', Clara.BudgetBuilder.VersionsWindow);

Clara.BudgetBuilder.BudgetVersionsPanel = Ext.extend(Ext.Panel, {
	id: 'versions-panel',
	xtype: 'panel',
	border:false,
    layout: 'border',
	constructor:function(config){		
		Clara.BudgetBuilder.BudgetVersionsPanel.superclass.constructor.call(this, config);
	},
	versionStore: new Ext.data.XmlStore({
		proxy: new Ext.data.HttpProxy({
			url: appContext+"/ajax/protocols/"+claraInstance.id+"/protocol-forms/"+claraInstance.form.id+"/budgets/list-versions",
			method:"GET",
			headers:{'Accept':'application/xml;charset=UTF-8'}
		}),
		sortInfo: {
		    field: 'created',
		    direction: 'ASC'
		},
		record:"budget",
		root:"list",
		fields:[{name:'id', mapping:'@id'},{name:'created', mapping:'@created', type: 'date', dateFormat: 'Y-m-d G:i:s.u'},{name:'formType', mapping:'@type'}]
	}),
	reloadVersions: function(){
		this.versionStore.removeAll();
		this.versionStore.load();
	},
	initComponent: function() {
		var config = {
				items: [{
					     xtype: 'grid',
					     deferRowRender:false,
					     viewConfig: {
					         forceFit: true,
					         emptyText: 'No other versions found.',
					         headersDisabled:true,
					         getRowClass: function(record,index,rp,st){
								return (index == st.getCount()-1)?"history-row-current":"history-row";
							 }
					      },
					      listeners: {
					          afterrender: function(grid) {
					              grid.getView().el.select('.x-grid3-header').setStyle('display',    'none');
					              grid.getSelectionModel().selectLastRow();
					              grid.getView().focusRow(grid.getStore().getCount()-1);
					          },
					      	  rowclick: function(grid,index,e){
					        	  if (index != grid.getStore().getCount()-1){	
					        		  Ext.getCmp("winBudgetVersions").selectedVersionId = grid.getStore().getAt(index).get("id");
					        		  Ext.getCmp("btnCompare").setDisabled(false);
					        	  } else { // disable comparing with latest version (itself)
					        		  Ext.getCmp("btnCompare").setDisabled(true);
					        	  }
					          }
					      },
					     stripeRows:true,
					     loadMask:true,
					     region: 'center',
					     border: false,
					     store:this.versionStore,
					     id: 'gpVersions',
					     columns: [
								{
								    xtype: 'gridcolumn',
								    dataIndex: 'created',
								    header: 'Time',
								    renderer: Ext.util.Format.dateRenderer('m/d/Y g:i A'),
								    sortable: true,
								    width:120
								},{
								    xtype: 'gridcolumn',
								    dataIndex: 'formType',
								    header: 'Attached to form',
								    sortable: false
								}
					     ]
					    }
					 ]
		};
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		Clara.BudgetBuilder.BudgetVersionsPanel.superclass.initComponent.apply(this, arguments);
		this.reloadVersions();
	}
});
Ext.reg('clarabudgetversionspanel', Clara.BudgetBuilder.BudgetVersionsPanel);
