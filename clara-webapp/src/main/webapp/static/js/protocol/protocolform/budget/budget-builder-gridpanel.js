Ext.ns('Clara.BudgetBuilder');

Clara.BudgetBuilder.EpochGridPanel = Ext.extend(Ext.grid.GridPanel, {
	border: false,
	stripeRows: true,
	epoch:{},
	scrollToRow : function(row) {
		clog("scrollToRow", row);
        this.getView().focusCell(row, 0, true);
    },
	highlightProcedure: function(proc){
		clog("highlightProcedure START");
		if (proc){
			var t = this;
			var st = t.getStore();
			var rowidx = st.findExact('procid',proc.id);
			if (rowidx > -1){
				t.getSelectionModel().unlock();
				t.getView().focusRow(rowidx);
				t.getSelectionModel().selectRow(rowidx);
				t.getSelectionModel().lock();
				
				// now scroll to it
				t.scrollToRow(rowidx);
				
			}
		}
	},
	
	constructor:function(config){		
		Clara.BudgetBuilder.EpochGridPanel.superclass.constructor.call(this, config);
	},

	reloadEpoch:	function(e){
		this.epoch = e;
		this.refresh();
	},
	
	refresh: function(){
		//cdebug("refreshing epoch gridpanel. epoch:",this.epoch,"epoch.getStore()",this.epoch.getStore(), "getColModel",this.epoch.getColumnModel(),"getColGrouping",this.epoch.getColumnGrouping());
		this.reconfigure(this.epoch.getStore(), this.epoch.getColumnModel(), this.epoch.getColumnGrouping());		
		this.getStore().loadData(this.epoch.getArray());
	},
	
	reconfigure: function(store, colModel, plugins) {
		var rendered = this.rendered;
        if(rendered){
            if(this.loadMask){
                this.loadMask.destroy();
                this.loadMask = new Ext.LoadMask(this.bwrap,
                        Ext.apply({}, {store:store}, this.initialConfig.loadMask));
            }
        }
        if(this.view){
            this.view.initData(store, colModel);
        }
        this.store = store;
        this.colModel = colModel;
        
         this.plugins = plugins;
        this.plugins.init(this);
        
        if(rendered){
            this.view.refresh(true);
        }
        this.fireEvent('reconfigure', this, store, colModel);
	},	
	
	initComponent: function() {
		var isSimpleEpoch = (this.epoch && (this.epoch.simple == true));
		var isSimple = (budget.isSimple() || isSimpleEpoch);
		var t = this;
		
		//cdebug("initing epoch gridpanel. epoch:",this.epoch,"epoch.getStore()",this.epoch.getStore(), "getColModel",this.epoch.getColumnModel(),"getColGrouping",this.epoch.getColumnGrouping());

		var config = {
				enableColumnMove:false,
				enableColumnHide:false,
				enableColumnResize:true,
				enableDragDrop:false,
				view: new Ext.grid.GroupingView({
			        groupTextTpl: '{text}',
			        showGroupName:false
			    }),
				viewConfig: {
					getRowClass: function(record, index){
						return "procedure-row";		
					}
				},
				tbar:[{
					//id: 'btnSimpleEditVisits',
					disabled:!Clara.BudgetBuilder.canEdit(),
					iconCls:'icn-pencil',
					hidden: !isSimple,
					text: 'Edit Visits...',
					handler: function(){
						var structureWindow = new Clara.BudgetBuilder.SimpleVisitWindow({modal:true});
						structureWindow.show();
					}
				},{
					//id: 'btnEditStructure',
					disabled:!Clara.BudgetBuilder.canEdit(),
					width:100,
					iconCls:'icn-pencil',
					hidden: isSimple,
					text: 'Edit Phase..',
					handler: function(){
						var structureWindow = new Clara.BudgetBuilder.StructureWindow({modal:true, selectedEpoch:Ext.getCmp('budget-tabpanel').activeEpoch});
						structureWindow.show();
					}
				},{
					//id: 'btnEditStructure',
					disabled:!Clara.BudgetBuilder.canEdit(),
					width:100,
					iconCls:'icn-exclamation',
					hidden: (!isSimpleEpoch || budget.isSimple()),
					text: 'Convert to complex phase..',
					handler: function(){
						var title = 'Convert To Complex Phase';
						var msg = 'Are you sure you want to convert this phase to complex? <br/><br/> WARNING: This process is not reversible.';
						
						Ext.Msg.show({
							title:title,
							msg:msg,
							buttons:Ext.Msg.OKCANCEL,
							icon: Ext.MessageBox.alert,
							fn:function(btn, text){
						    	if (btn == 'ok'){
						    		if (Ext.getCmp('budget-tabpanel').activeEpoch.canMakeComplex()) {
						    			Ext.getCmp('budget-tabpanel').activeEpoch.makeComplex();
						    			Clara.BudgetBuilder.MessageBus.fireEvent('epochupdated', Ext.getCmp('budget-tabpanel').activeEpoch);
					        			Clara.BudgetBuilder.MessageBus.fireEvent('epochcontentupdated', Ext.getCmp('budget-tabpanel').activeEpoch);
					        			var myMask = new Ext.LoadMask(Ext.getBody(), {msg:"Converting phase, please wait..."});
							    		myMask.show();
							    		budget.save(budget.toXML(),true);
						    		} else {
						    			Ext.Msg.alert('Cannot convert phase', 'This phase cannot be converted at this time. Check for and fix any visits with negative days.');
						    		}
						    		
						    		
						    	}
							}
						});
					
					}
				},'-',{
					//id: 'btnSimpleRename',
					disabled:!Clara.BudgetBuilder.canEdit(),
					iconCls:'icn-ui-tab--pencil',
					//hidden: !isSimple,
					text: 'Rename phase...',
					handler: function(){
						Ext.Msg.prompt("Study Phase Name", "Enter phase name:", function(btn,text){
							var name = jQuery.trim(text);
							name = name.substr(0,31);
							var epochNameExists = (budget.getEpochByName(name) != null);
							var epochNameIsBlank = (name == "");
							if (btn == 'ok'){
								if (!epochNameExists && !epochNameIsBlank &&  Clara.BudgetBuilder.validatePhaseName(name)){
									Ext.getCmp('budget-tabpanel').activeEpoch.name = name;
									Clara.BudgetBuilder.MessageBus.fireEvent('epochupdated', Ext.getCmp('budget-tabpanel').activeEpoch);
				        			Clara.BudgetBuilder.MessageBus.fireEvent('epochcontentupdated', Ext.getCmp('budget-tabpanel').activeEpoch);
									budget.save();
								} else {
									if (epochNameExists) alert("A phase already exists with that name. Choose another one and try again.");
									if (epochNameIsBlank) alert("Phase name cannot be blank.");
									if (!Clara.BudgetBuilder.validatePhaseName(name)) alert("Phase name cannot contain the following: "+Clara.BudgetBuilder.InvalidPhaseCharacters);
								}
								
							}
						},this,false,Ext.getCmp('budget-tabpanel').activeEpoch.name);
					}
				},{
					//id: 'btnCopyEpoch',
					disabled:!Clara.BudgetBuilder.canEdit(),				
					iconCls:'icn-layers',
					text: 'Copy Phase',
					hidden: budget.isSimple(),
					handler: function(){
						Ext.Msg.prompt("Copy Study Phase", "Enter new phase name:", function(btn,text){
							if (btn == 'ok' && jQuery.trim(text) != ""){
								var e = Ext.getCmp('budget-tabpanel').activeEpoch.copy(text);
								budget.addEpoch(e);
								budget.save();
							}
						});
					}
				},{
					//id: 'btnDeleteEpoch',
					disabled:!Clara.BudgetBuilder.canEdit(),
					iconCls:'icn-minus-button',
					text: 'Remove Phase..',
					hidden: budget.isSimple(),
					handler: function(){
						var tp = Ext.getCmp("budget-tabpanel");
						Clara.BudgetBuilder.ConfirmRemoveEpoch(tp, tp.getActiveTab());
					}
				},'->',{
						//id: 'btnAddProcedure',
						disabled:!Clara.BudgetBuilder.canEdit(),
						iconCls:'icn-table-insert-row',
						text: 'Add Procedure',
						menu: [
						       
						       {
						    	   text:'UAMS Procedure',
						    	   iconCls:'icn-zone-select',
						    	   handler: function(){
						   				var addProcedureWindow = new Clara.BudgetBuilder.ProcedureWindow({modal:true});
						   				addProcedureWindow.show();
						   			}
						       },
						       {
						    		//id: 'btnAddMiscProcedure',
						    		disabled:false,
						    		iconCls:'icn-zone',
						    		text: 'Misc Procedure',
						    		handler: function(){
						    			var addProcedureWindow = new Clara.BudgetBuilder.ProcedureWindow({modal:true, proceduretype:'misc'});
						    			addProcedureWindow.show();
						    		}
						       },
						       {
						    		//id: 'btnAddOutsideProcedure',
						    		disabled:false,
						    		iconCls:'icn-zone--arrow',
						    		text: 'Outside Procedure',
						    		handler: function(){

						    			var addProcedureWindow = new Clara.BudgetBuilder.ProcedureWindow({modal:true, proceduretype:'outside'});
						    			addProcedureWindow.show();
						    			
						    		}
						    	},
						       {
						    		disabled:false,
						    		iconCls:'icn-book-question',
						    		text: 'Request a CPT code update..',
						    		handler: function(){

						    			var w = new Clara.BudgetBuilder.RequestCodeWindow({iconCls:'icn-book-question'});
						    			w.show();
						    			
						    		}
						    	}
						       
						       ]
						
					},{
						disabled:!Clara.BudgetBuilder.canEdit(),
						iconCls:'icn-pill--plus',
						text: 'Drugs',
						menu: [
						       {
						    	   text:'Drug',
						    	   iconCls:'icn-pill',
						    	   handler: function(){
						    		   clog("pharmacyStatus",pharmacyStatus);
						    		   if (pharmacyStatus == "waiver_request_approved" || pharmacyStatus == "approved"){
						    		    var dwin = new Clara.BudgetBuilder.ProcedureWindow({modal:true, proceduretype:'drug'});
						    		    dwin.show();
						    		   } else if(pharmacyStatus.length == 0) { // not requested
						    				 alert("Pharmacy Review is not requested. Please request for pharmacy review before adding drugs and drug dispensing fee into the budget!");
						    			
						    		   } else {
						    			   alert("Pharmacy Review Not Complete (current review status is '"+pharmacyStatus+"').. Enter charges in the budget after the review has been completed.");
						    		   }
						   				//var addDrugWindow= new Clara.BudgetBuilder.DrugWindow({modal:true});
						   				//addDrugWindow.show();
						   			}
						       },
						       {
						    		iconCls:'icn-pill--arrow',
						    		text: 'Pharmacy dispensing fee',
						    		handler: function(){
						    			
						    			if (pharmacyStatus == "waiver_request_approved" || pharmacyStatus == "approved"){
						    				var dwin = new Clara.BudgetBuilder.ProcedureWindow({modal:true, proceduretype:'drugdispensing'});
						    				dwin.show();
						    			 } else if(pharmacyStatus.length == 0) { // not requested
						    				 alert("Pharmacy Review is not requested. Please request for pharmacy review before adding drugs and drug dispensing fee into the budget!")
								    			
						    			 }
						    			 else {
							    			   alert("Pharmacy Review Not Complete (current review status is '"+pharmacyStatus+"').. Enter charges in the budget after the review has been completed.");
							    		   }
						    			//var addDrugWindow = new Clara.BudgetBuilder.DrugWindow({modal:true, drugtype:'dispensing'});
					 					//addDrugWindow.show();
						    		}
						       }
						       ]
						}],
				loadMask:true,
				store: this.epoch.getStore(),
    			colModel: this.epoch.getColumnModel(),
    			plugins: this.epoch.getColumnGrouping(),
				listeners: {
					cellclick: function(gp,row,col,e){
						var record = gp.getStore().getAt(row);  // Get the Record
					    var fieldName = gp.getColumnModel().getDataIndex(col); // Get field name
					    var data = record.get(fieldName);
					    
					    if (Clara.BudgetBuilder.canEdit() && data.indexOf("' id='vp-") > 0) {
					    	clog(row,record,col,data);
					    	var dataHTML = jQuery.parseHTML(data);
					    	clog(dataHTML[0].id);
					    	
					    	new Clara.BudgetBuilder.EditVisitProcedurePopup({vid:dataHTML[0].id.split("-")[1], pid:dataHTML[0].id.split("-")[2], cid:dataHTML[0].id}).show();
					    	
					    }
					},
					activate: function(){
						var t = this;
						t.getTopToolbar().setDisabled(!Clara.BudgetBuilder.canEdit());
						t.refresh();
					},
					deactivate: function(){
						this.getStore().removeAll();
					}
				}
		};
		
		
		
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		Clara.BudgetBuilder.EpochGridPanel.superclass.initComponent.apply(this, arguments);
		//cdebug("[Clara.BudgetBuilder.EpochGridPanel] ABOUT TO CALL this.getStore().loadData(this.epoch.getArray())");
		this.getStore().loadData(this.epoch.getArray());
	}});
Ext.reg('clarabudgetepochgridpanel', Clara.BudgetBuilder.EpochGridPanel);

