Ext.ns('Clara.BudgetBuilder');

Clara.BudgetBuilder.TabPanel = Ext.extend(Ext.ux.panel.DDTabPanel, {
	id: 'budget-tabpanel',
	enableTabScroll: true,
	border: false,
	budget:{},
	activeEpoch:{},
	constructor:function(config){		
		Clara.BudgetBuilder.TabPanel.superclass.constructor.call(this, config);
	},
	
	loadBudget:function(){
		if (this.budget && this.budget.epochs){
			this.removeAll(true);	// first remove existing tabs (in case of reload)
			for (var i=0;i<this.budget.epochs.length;i++){
				this.addEpochTab(this.budget.epochs[i]);
			}
		}
	},
	
	selectEpoch: function(epoch){
		var t = this;
		var tabs = t.find('epoch', epoch);
		t.setActiveTab(tabs[0]);
		t.activeEpoch = epoch;
	},
	
	addEpochTab: function(epoch){
		var iconClass = (epoch.simple)?"icn-layout-3":"icn-table";
		var diffClass = (epoch.diff !='')?"epoch-diff-"+epoch.diff:'';
		this.add({
			xtype:'clarabudgetepochgridpanel',
			epoch:epoch,
			allowDrag: Clara.BudgetBuilder.canEdit(),
			title:"<span class='"+diffClass+"'>"+((epoch.conditional)?(epoch.name+" (conditional)"):epoch.name)+"</span>",
			iconCls:iconClass
		});
	},
	
	initComponent: function() {
		
		Clara.BudgetBuilder.MessageBus.addListener('epochcontentupdated', function(epoch){
			var e = budget.getEpoch(epoch.id);
			var ev = e.validate();
			if (ev.length < 1) {
				Ext.getCmp('budget-tabpanel').getActiveTab().refresh();
				Ext.getCmp('budget-tabpanel').getActiveTab().show();
			} else {
				// Nothing TODO: Maybe style invalid grid, but its not necessary.
				Ext.getCmp('budget-tabpanel').getActiveTab().hide();
			}
			//Ext.getCmp('budget-tabpanel').getActiveTab().title = e.name;
		});
		
		Clara.BudgetBuilder.MessageBus.addListener('epochupdated', function(epoch){
			Ext.getCmp('budget-tabpanel').getActiveTab().setTitle((epoch.conditional)?(epoch.name+" (conditional)"):epoch.name);//epoch.name;
		});
		
		Clara.BudgetBuilder.MessageBus.addListener('epochadded', function(epoch){
			Ext.getCmp('budget-tabpanel').addEpochTab(epoch);
		});
		
		Clara.BudgetBuilder.MessageBus.addListener('historyreverted', function(){
			Ext.getCmp('budget-tabpanel').loadBudget();
			Ext.getCmp('budget-tabpanel').setActiveTab(0);
		});
		
		Clara.BudgetBuilder.MessageBus.addListener('budgetversionloaded', function(){
			Ext.getCmp('budget-tabpanel').loadBudget();
			Ext.getCmp('budget-tabpanel').setActiveTab(0);
			

			// close any ajax loading screens..
			if (typeof Ext.getCmp("winBudgetVersions") != 'undefined') Ext.getCmp("winBudgetVersions").close();
			Ext.MessageBox.hide();
			
		});
		
		Clara.BudgetBuilder.MessageBus.addListener('faupdated', function(){
			Ext.getCmp('budget-tabpanel').loadBudget();
			Ext.getCmp('budget-tabpanel').setActiveTab(0);
		});
		
		var config = {
				
			    activeTab: 0,
			    listeners:{
			    	afterrender: function(){
			    		Ext.getCmp("budget-viewport").doLayout();	// force layout (large budgets in Chrome wont display until window resize)
			    	},
					beforerender: function(){
						this.loadBudget(this.budget);
					},
					add: function(){
						this.doLayout();
					},
					// beforeremove: Clara.BudgetBuilder.ConfirmRemoveEpoch,
					beforetabchange: function(tp, newTab, oldTab){
						
						if (typeof newTab != 'undefined'){
							
							Clara.BudgetBuilder.MessageBus.fireEvent('beforetabchange');
							if (newTab.xtype == 'clarabudgetepochgridpanel'){
								this.activeEpoch = newTab.epoch;
							} else {
								this.activeEpoch = {};
							}
							Clara.BudgetBuilder.MessageBus.fireEvent('epochchanged', this.activeEpoch);
							Clara.BudgetBuilder.MessageBus.fireEvent('aftertabchange');
						}
					},
					drop:function(){
						// reorder epochs
						if (Clara.BudgetBuilder.canEdit){
							for (var i=0;i<this.items.items.length;i++){
								this.items.items[i].epoch.index = i;
							}
							Clara.BudgetBuilder.SaveAction = "Reorder study phase";
							budget.save();
						}
					}
				}
		};
		
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		Clara.BudgetBuilder.TabPanel.superclass.initComponent.apply(this, arguments);
		
	}
	

});
Ext.reg('clarabudgettabpanel', Clara.BudgetBuilder.TabPanel);
