Ext.ns('Clara.BudgetBuilder');

Clara.BudgetBuilder.StructureWindow = Ext.extend(Ext.Window, {
	id:'winBudgetStructure',
    title: 'Edit Phase',
    closable:false,
    width: 950,
    height: 568,
    layout: 'border',
    defaults: {
        collapsible: false,
        split: true
    },
    validateEpoch: function(){
    	var t = this;
    	var errors = t.selectedEpoch.validate();
		t.errors = errors;
		if (errors.length > 0){
			Ext.getCmp('btnCloseWindow').disable();
			Ext.getCmp('winstatusbar').setStatus({
				text: 'Errors found. You must complete this study phase before closing.',
				iconCls: 'icon-review-not-met'
			});
		} else {
			Ext.getCmp('btnCloseWindow').enable();
			Ext.getCmp('winstatusbar').setStatus({
				text: 'Study phase is valid.',
				iconCls: 'icon-review-met'
			});
		}
		t.refreshViews();
    },
    reselectArmAndCycle: function(){
    	clog("Clara.BudgetBuilder.StructureWindow:reselectArmAndCycle",this.selectedArm,this.selectedCycle);
    	
    	if (this.selectedArm && this.selectedArm.id) this.selectArmRow(this.selectedArm);
    	if (this.selectedCycle && this.selectedCycle.id){ 
    		this.selectCycleRow(this.selectedCycle);
    		this.reloadVisitPanel(this.selectedCycle);
        	Ext.getCmp("btnRemoveVisit").setDisabled(true);
        	Ext.getCmp("btnCopyVisit").setDisabled(true);
    	} else {
    		// no cycle selected, clear cycle and visit panels
    		Ext.getCmp('gpCycles').disable();
        	Ext.getCmp('gpVisits').disable();
        	this.reloadArmPanel();
    	}
    	
    },
    onCyclesRemoved: function(){
    	clog("Clara.BudgetBuilder.StructureWindow:onCyclesRemoved");
    	// this.reselectArmAndCycle();
    },
    onVisitsRemoved: function(){
    	clog("Clara.BudgetBuilder.StructureWindow:onVisitsRemoved");
    	// this.reselectArmAndCycle();
    },
    onVisitUpdated: function(){
    	clog("Clara.BudgetBuilder.StructureWindow:onVisitUpdated");
    	// this.reloadCyclePanel();
    	//this.reselectArmAndCycle();
    },
    editedVisitname:false,
    selectedEpoch: {},
    selectedArm: {},
    selectedCycle: {},
    errors:[],
	selectArmRow: function(arm){
		clog("Clara.BudgetBuilder.StructureWindow:selectArmRow");
		clog("selectArmRow",arm,Ext.getCmp('gpArms').getStore().find("id",arm.id));
		Ext.getCmp('gpArms').getSelectionModel().selectRow(Ext.getCmp('gpArms').getStore().find("id",arm.id));
	},
	selectCycleRow: function(cycle){
		clog("Clara.BudgetBuilder.StructureWindow:selectCycleRow");
		clog("selectCycleRow",cycle,Ext.getCmp('gpCycles').getStore().find("id",cycle.id));
		Ext.getCmp('gpCycles').getSelectionModel().selectRow(Ext.getCmp('gpCycles').getStore().find("id",cycle.id));
	},
	reloadArmPanel: function(disableSort){
		clog("Clara.BudgetBuilder.StructureWindow:reloadArmPanel","selectedEpoch",this.selectedEpoch);
		disableSort = disableSort || false;
		var arms = budget.getArmArrayForEpoch(this.selectedEpoch);
		Ext.getCmp('gpArms').getStore().loadData(arms);
		if (!disableSort) Ext.getCmp('gpArms').getStore().sort("index","ASC");
		Ext.getCmp("btnCopyArm").setDisabled(true);
		Ext.getCmp("btnRemoveArm").setDisabled(true);
	},
	reloadCyclePanel: function(disableSort){
		clog("Clara.BudgetBuilder.StructureWindow:reloadCyclePanel","selectedArm",this.selectedArm);
		disableSort = disableSort || false;
		var cycles = budget.getCycleArrayForArm(this.selectedArm);
		Ext.getCmp('gpCycles').getStore().loadData(cycles);
		if (!disableSort) Ext.getCmp('gpCycles').getStore().sort("startday","ASC");
		Ext.getCmp("btnCopyCycle").setDisabled(true);
		Ext.getCmp("btnRemoveCycle").setDisabled(true);
	},

	reloadVisitPanel: function(disableSort){	
		clog("Clara.BudgetBuilder.StructureWindow:reloadVisitPanel","selectedCycle",this.selectedCycle);
		disableSort = disableSort || false;
		var visits = budget.getVisitArrayForCycle(this.selectedCycle);
		Ext.getCmp('gpVisits').getStore().loadData(visits);
		if (!disableSort) Ext.getCmp('gpVisits').getStore().sort([{field:'cycleindex',direction:'ASC'},{field:'name',direction:'ASC'}]);
		Ext.getCmp("btnRemoveVisit").setDisabled(true);
    	Ext.getCmp("btnCopyVisit").setDisabled(true);
	},
	refreshViews: function(){
		clog("Clara.BudgetBuilder.StructureWindow:refreshViews");
		Ext.getCmp('gpArms').getView().refresh();
		Ext.getCmp('gpCycles').getView().refresh();
		Ext.getCmp('gpVisits').getView().refresh();
		this.reselectArmAndCycle();
	},
    initComponent: function() {
		var t = this;
		
		t.listeners = {
			show: function(){
				
	        	Ext.getCmp('gpCycles').disable();
	        	Ext.getCmp('gpVisits').disable();
	        	t.reloadArmPanel();
			}
		};

		
		t.buttons = [{
			id:'btnCloseWindow',
			text:'Close',
			handler: function(){
				Ext.getCmp('winBudgetStructure').close();
			}
		}];
		
		t.bbar = new Ext.ux.StatusBar({
            id: 'winstatusbar',
            defaultText: 'Ready.',
            iconCls: 'icon-review-met'
        });
		
        t.items = [
            {
			    xtype: 'panel',
			    height: 40,
			    split:false,
			    layout: 'absolute',
			    region:'north',
			    unstyled: true,
			    border:false,
			    items: [
			        {
			            xtype: 'textfield',
			            x: 60,
			            y: 10,
			            width: 160,
			            name: 'fldEpochName',
			            id: 'fldEpochName',
			            allowBlank: false,
			            hidden:true,
			            value:this.selectedEpoch.name,
			            style:'font-size:14px;',
			            enableKeyEvents:true,
			            listeners: {
			        		'change': function(fld,newv,oldv){
			        			var v = jQuery("#fldEpochName").val();
			        			if (!Clara.BudgetBuilder.validatePhaseName(v)) alert("Phase name cannot contain the following: "+Clara.BudgetBuilder.InvalidPhaseCharacters);
			        			else {
			        				Ext.getCmp('winBudgetStructure').selectedEpoch.name = v;
			        				Clara.BudgetBuilder.MessageBus.fireEvent('epochupdated', Ext.getCmp('winBudgetStructure').selectedEpoch);
			        			}
			        		},
			        		'keyup':function(fld,e){
			        			var v = jQuery("#fldEpochName").val();
			        			if (!Clara.BudgetBuilder.validatePhaseName(v)) alert("Phase name cannot contain the following: "+Clara.BudgetBuilder.InvalidPhaseCharacters);
			        			else {
			        				Ext.getCmp('winBudgetStructure').selectedEpoch.name = v;
			        				Clara.BudgetBuilder.MessageBus.fireEvent('epochupdated', Ext.getCmp('winBudgetStructure').selectedEpoch);
			        			}
			        		},
			        		'blur':function(e){
			        			var v = jQuery("#fldEpochName").val();
			        			if (!Clara.BudgetBuilder.validatePhaseName(v)) alert("Phase name cannot contain the following: "+Clara.BudgetBuilder.InvalidPhaseCharacters);
			        			else {
			        				Ext.getCmp('winBudgetStructure').selectedEpoch.name = v;
			        				Clara.BudgetBuilder.MessageBus.fireEvent('epochcontentupdated', Ext.getCmp('winBudgetStructure').selectedEpoch);
			        			}
			        		}
			        	}
			        },
			        {
		                xtype: 'checkbox',
		                id:'fldEpochIsConditional',
		                boxLabel: 'Conditional phase (if checked, describe condition in "Phase Notes" below)',
		                checked:(this.selectedEpoch.conditional || false),
		                x: 10,
		                y: 10,
			            listeners: {
			        		'check': function(fld,checked){
			        			Ext.getCmp('winBudgetStructure').selectedEpoch.conditional = checked;
			        			Clara.BudgetBuilder.MessageBus.fireEvent('epochupdated', Ext.getCmp('winBudgetStructure').selectedEpoch);
			        			Clara.BudgetBuilder.MessageBus.fireEvent('epochcontentupdated', Ext.getCmp('winBudgetStructure').selectedEpoch);
			        		}
			        	}
		            }
			    ]
			},{
			    xtype: 'panel',
			    height: 90,
			    minSize:90,
			    split:true,
			    layout: 'fit',
			    margins:'0 5 5 5',
			    region:'south',
			    title:'Phase Notes',
			    items: [
			        {
			            xtype: 'textarea',
			            name: 'fldEpochNotes',
			            id: 'fldEpochNotes',
			            allowBlank: true,
			            value:this.selectedEpoch.notes,
			            style:'font-size:14px;',
			            enableKeyEvents:true,
			            listeners: {
			        		'change': function(fld,newv,oldv){
			        			Ext.getCmp('winBudgetStructure').selectedEpoch.notes = jQuery("#fldEpochNotes").val();
			        			Clara.BudgetBuilder.MessageBus.fireEvent('epochupdated', Ext.getCmp('winBudgetStructure').selectedEpoch);
			        		},
			        		'keyup':function(fld,e){
			        			Ext.getCmp('winBudgetStructure').selectedEpoch.notes = jQuery("#fldEpochNotes").val();
			        			Clara.BudgetBuilder.MessageBus.fireEvent('epochupdated', Ext.getCmp('winBudgetStructure').selectedEpoch);
			        		},
			        		'blur':function(e){
			        			Ext.getCmp('winBudgetStructure').selectedEpoch.notes = jQuery("#fldEpochNotes").val();
			        			Clara.BudgetBuilder.MessageBus.fireEvent('epochcontentupdated', Ext.getCmp('winBudgetStructure').selectedEpoch);
			        		}
			        	}
			        }
			    ]
			},
			{xtype:'claraBudgetArmGridPanel',id: 'gpArms', region:'west'},
			{xtype:'claraBudgetCycleGridPanel',id: 'gpCycles', region:'center', parentWindow:t},
			{xtype:'claraBudgetVisitGridPanel',id: 'gpVisits', region:'east', parentWindow:t}
        ];

        Clara.BudgetBuilder.StructureWindow.superclass.initComponent.call(this);

    }
});

