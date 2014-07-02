Ext.ns('Clara.BudgetBuilder');

Clara.BudgetBuilder.CycleGridPanel = Ext.extend (Ext.grid.GridPanel,{
	parentWindow:{},
	initComponent: function() {
		var t = this;
		var config = {
				title: 'Cycles',
				view: new Ext.grid.GridView({
					emptyText: '<h1>No cycles defined for this arm.</h1><h2>Click "Add" above to create a new cycle.</h2>',
					getRowClass: function(row,index){
						var cls='';
						var cycleId = row.data.id;
						var epoch = budget.getEpoch(Ext.getCmp('winBudgetStructure').selectedEpoch.id);
						var arm = epoch.getArm(Ext.getCmp('winBudgetStructure').selectedArm.id);
						var cycle = arm.getCycle(row.data.id);


						if (cycle && cycle.validate(arm).length > 0){
							return 'error-incomplete';
						}
						return cls;
					}
				}),
				region:'center',
				width: 285,
				itemId: 'gpCycles',
				margins: '0 0 0 0',
				

				cycleErrorTooltipRowRenderer: function(value, metadata, rec){
					var error = false;
					var msg = "";

					var cycleId = rec.get('id');
					var epoch = budget.getEpoch(Ext.getCmp('winBudgetStructure').selectedEpoch.id);
					var arm = epoch.getArm(Ext.getCmp('winBudgetStructure').selectedArm.id);
					var cycle = arm.getCycle(cycleId);


					if (cycle && cycle.validate(arm).length > 0){
						msg = 'There are no visits assigned to this cycle. Add visits to this cycle in order to continue.';
					} else {
						msg = '';
					}


					metadata.attr = 'ext:hide="user" ext:qtip="' + msg + '"';
					return "<div class='wrap'>"+value+"</div>";
				},

				store: new Ext.data.ArrayStore({fields:[{name:'id', type:'integer'},{name:'index', type:'integer'},'name',{name:'startday', type:'integer'},{name:'endday',type:'integer'},{name:'repetitions',type:'integer'},'repeatforever']}),
				selModel:new Ext.grid.RowSelectionModel({
					singleSelect:false
				}),
				listeners:{
					rowclick:function(g,rowIndex,e){
						clog("g,gsm",g,g.getSelectionModel(),g.getSelectionModel());
						var selectedCycleRecords = g.getSelectionModel().getSelections();
						
						if (selectedCycleRecords.length > 1){
							clog("cycle recs",selectedCycleRecords);
							Ext.getCmp('gpVisits').getStore().removeAll();
							Ext.getCmp('gpVisits').disable();
							Ext.getCmp('btnRemoveCycle').setDisabled(false);
							Ext.getCmp('btnCopyCycle').setDisabled(true);
						}
						else if (selectedCycleRecords.length == 1){
							
							var d =  g.getSelectionModel().getSelected().data;
							var cycle = budget.getCycle(d.id);
							
							Clara.BudgetBuilder.MessageBus.fireEvent('cycleselected', cycle);
							Ext.getCmp('winBudgetStructure').selectedCycle = cycle;
							Ext.getCmp("btnCopyCycle").setDisabled(false);
							Ext.getCmp("btnRemoveCycle").setDisabled(false);
							Ext.getCmp('gpVisits').enable();
							Ext.getCmp('winBudgetStructure').reloadVisitPanel(cycle);
							
						}
					},
					rowdblclick:function(g,rowIndex,e){
						clog(g.getSelectionModel().getSelected());
						var d =  g.getSelectionModel().getSelected().data;
						var cycle = budget.getCycle(d.id);
						clog("editing cycle",cycle);
						var epoch = budget.getEpoch(Ext.getCmp('winBudgetStructure').selectedEpoch.id);
						var arm = epoch.getArm(Ext.getCmp('winBudgetStructure').selectedArm.id);
						Clara.BudgetBuilder.MessageBus.fireEvent('cycleselected', cycle);
						new Clara.BudgetBuilder.CycleWindow({parentArm:arm,cycle:cycle}).show();
					}
				},
				columns: [
				          {
				        	  xtype: 'gridcolumn',
				        	  menuDisabled:true,
				        	  header: 'Name',
				        	  id:'name',
				        	  dataIndex:'name',
				        	  sortable: false,
				        	  renderer:t.cycleErrorTooltipRowRenderer
				          },
				          {
				        	  xtype: 'numbercolumn',
				        	  menuDisabled:true,
				        	  id:'startday',
				        	  dataIndex:'startday',
				        	  header: 'Start',
				        	  sortable: false,
				        	  //hidden:true,
				        	  width: 50,
				        	  align:'center',
				        	  renderer:t.cycleErrorTooltipRowRenderer,
				        	  format: '0'
				          },
				          {
				        	  xtype: 'numbercolumn',
				        	  menuDisabled:true,
				        	  id:'endday',
				        	  dataIndex:'endday',
				        	  header: 'End',
				        	  sortable: false,
				        	  width: 50,
				        	  align:'center',
				        	  renderer:t.cycleErrorTooltipRowRenderer,
				        	  format: '0'
				          },
				          
				          {
				        	  xtype: 'gridcolumn',
				        	  menuDisabled:true,
				        	  id:'repetitions',
				        	  dataIndex:'repetitions',
				        	  header: 'Indefinite?',
				        	  sortable: false,
				        	  width: 70,
				        	  align:'center',
				        	  renderer:t.cycleErrorTooltipRowRenderer,
				        	  format: '0',
				        	  renderer: function(v,s,r){
				        		  var d = r.data;
				        		  if (d.repeatforever == true){
				        			  return "Yes";
				        		  } else {
				        			  return "No";
				        		  }
				        	  }
				          }
				          ],
				          tbar: {
				        	  xtype: 'toolbar',
				        	  items: [
				        	          {
				        	        	  xtype: 'button',
				        	        	  text: 'Add',
				        	        	  iconCls:'icn-plus-button',
				        	        	  itemId: 'btnAddCycle',
				        	        	  id: 'btnAddCycle',
				        	        	  handler:function(){
				        	        		  var epoch = budget.getEpoch(Ext.getCmp('winBudgetStructure').selectedEpoch.id);
				        	        		  var arm = epoch.getArm(Ext.getCmp('winBudgetStructure').selectedArm.id);
				        	        		  var nextavailableday = arm.cycleLastDay()+1;
				        	        		  new Clara.BudgetBuilder.CycleWindow({nextavailableday:nextavailableday, parentArm:arm, cycle:new Clara.BudgetBuilder.Cycle({startday:nextavailableday, endday:nextavailableday,duration:1,durationunit:'Day',simple:true})}).show();
				        	        	  }
				        	          },
				        	          {
				        	        	  xtype: 'tbseparator'
				        	          },
				        	          {
				        	        	  xtype: 'button',
				        	        	  text: 'Remove',
				        	        	  disabled:true,
				        	        	  iconCls:'icn-minus-button',
				        	        	  itemId: 'btnRemoveCycle',
				        	        	  id: 'btnRemoveCycle',
				        	        	  handler:function(){
				        	        		  // Get ID of selected cycle

				        	        		 
				        	        		  var selectedRecords = t.getSelectionModel().getSelections();
				        	        		  clog("selectedRecords",selectedRecords);
				        	        		  
				        	        		  Ext.Msg.show({
				        	        			  title:'Remove '+selectedRecords.length+' cycles?',
				        	        			  msg: 'You are about to remove <strong>'+selectedRecords.length+' cycles</strong>. Are you sure you want to do this?',
				        	        			  buttons: Ext.Msg.YESNOCANCEL,
				        	        			  fn: function(btn){
				        	        				  if (btn == 'yes'){
				        	        					  t.parentWindow.selectedArm.removeCycles(selectedRecords);
				        	        					  if (typeof t.parentWindow.onCyclesRemoved == "function") t.parentWindow.onCyclesRemoved();
				        	        				  }
				        	        			  },
				        	        			  animEl: 'elId',
				        	        			  icon: Ext.MessageBox.WARNING
				        	        		  });
				        	        	  }
				        	          },
				        	          {
				        	        	  xtype: 'tbseparator'
				        	          },
				        	          {
				        	        	  xtype: 'button',
				        	        	  text: 'Copy..',
				        	        	  disabled:true,
				        	        	  iconCls:'icn-layers',
				        	        	  id: 'btnCopyCycle',
				        	        	  handler:function(){
				        	        		  new Clara.BudgetBuilder.CopyCycleWindow({}).show();
				        	        		  //var selectedRecord = Ext.getCmp('gpCycles').getSelectionModel().getSelected();
				        	        		  //budget.addCycle(Ext.getCmp('winBudgetStructure').selectedArm, Ext.getCmp('winBudgetStructure').selectedCycle.copy((Ext.getCmp('winBudgetStructure').selectedCycle.endday + 1),true));
				        	        	  }
				        	          }
				        	          ]
				          }
		};
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		Clara.BudgetBuilder.CycleGridPanel.superclass.initComponent.apply(this, arguments);

	}


});
Ext.reg('claraBudgetCycleGridPanel', Clara.BudgetBuilder.CycleGridPanel);


Clara.BudgetBuilder.CopyCycleWindow = Ext.extend(Ext.Window, {
	layout:'form',
	width:400,
	parentArm:null,
	modal:true,
	padding:6,
	title:'Copy cycle',
	initComponent: function() {
		var t = this;
		t.parentArm = Ext.getCmp('winBudgetStructure').selectedArm;
		
		this.listeners = {
				close:function(){
					if (typeof Ext.getCmp("winBudgetStructure") != "undefined") Ext.getCmp("winBudgetStructure").selectArmRow(t.parentArm);
				}
		};
		this.buttons = [
		                {
		                	id: 'btnAddCycleCopiesToBudget',
		                	text: 'Save',
		                	handler: function(){
		                		var cycle = Ext.getCmp('winBudgetStructure').selectedCycle;
		                		
		                		var count = Ext.getCmp("fldNumberOfCycleCopies").getValue();
		                		var keepStartDay = Ext.getCmp("fldKeepCycleStartDay").getValue();
		                		var nextStartDay = 0;
		                		
		                		for (var i=0; i<count; i++){
		                			nextStartDay = (keepStartDay)?cycle.startday:(cycle.endday+1);
		                			cycle = budget.addCycle(t.parentArm, cycle.copy(nextStartDay,true), true);	// last "true" to supress addcycle event
		                		}
		                		
		                		Clara.BudgetBuilder.MessageBus.fireEvent('epochcontentupdated',t.parentArm.parentEpoch);
		                		
	        	        		t.close();
		                		
		                	}
		                }
		                ];
		this.items = [
		              {
		            	  xtype: 'numberfield',
		            	  id:'fldNumberOfCycleCopies',
		            	  fieldLabel: 'Number of copies',
		            	  anchor: '100%',
		            	  value:1
		              },
		              {

	            		  xtype: 'checkbox',
	            		  boxLabel: 'Make copies start on same day as original',
	            		  checked:false,
	            		  id:'fldKeepCycleStartDay'
		              }
		              ];
		Clara.BudgetBuilder.CopyCycleWindow.superclass.initComponent.call(this);
	}
});



Clara.BudgetBuilder.CycleWindow = Ext.extend(Ext.Window, {
	title: 'Add Cycle',
	width: 500,
	height: 500,
	layout: 'form',
	padding: 6,
	itemId: 'winCycle',
	modal: true,
	id: 'winCycle',
	cycle:{},
	newCycle:{},
	parentArm:{},
	cycleType:'Day',
	nextavailableday:0,
	getStatusText:function(){
		// this.cycle.recalculateDateRanges();
		var btn = Ext.getCmp('btnAddCycleToBudget');
		
		if(btn) btn.setDisabled(false);
		
		var newCycle = this.newCycle;
		
		var cycle = this.cycle;
		
		var multiplier = (newCycle.durationunit == "Year")?365:(newCycle.durationunit == "Week")?7:(newCycle.durationunit == "Month")?30:1;
		if (newCycle.repeatforever == true) newCycle.endday = parseFloat(newCycle.startday) + (multiplier*parseFloat(newCycle.duration)) - 1;
		else newCycle.endday = parseFloat(newCycle.startday) + (parseFloat(newCycle.repetitions) * (multiplier*parseFloat(newCycle.duration))) - 1;
	
		
		var txt = "This cycle ";
		if (newCycle.startday != ""){
			txt += "starts on day "+newCycle.startday+((newCycle.simple == true)?".":" and ends on day "+newCycle.endday+" ("+(parseFloat(newCycle.endday)-parseFloat(newCycle.startday)+1)+" days long).");
		} else {
			txt += "is incomplete because it has no start date.";
		}
		if (newCycle.simple == false && newCycle.repeatforever == true){
			txt += " This cycle REPEATS INDEFINITELY.";
		} else{			
			
			if(jQuery("#fldCycleLength")) {

				// bakermatt 8/14/2013
				// get cycle days, to get the SMALLEST VALID END DATE
				// then compare with user's entered date.
				
				if (newCycle && newCycle.getLastVisit() != null){
					
					var lastVisitDay = newCycle.getLastVisit().cycleindex; 	
					
					clog("Cycle len: lastVisit",lastVisitDay,"Start",newCycle.startday,"End",newCycle.endday,"Duration",(newCycle.endday - newCycle.startday + 1));
					
					//  if((newCycle.endday - newCycle.startday + 1) < (cycle.endday - cycle.startday + 1)){
	
					if((newCycle.endday - newCycle.startday + 1) < (lastVisitDay)){
					
						txt += "<br/><span style='color:red;font-weight:800;'>" +
								"CYCLE LENGTH TOO SHORT. THERE IS AT LEAST ONE VISIT ON DAY "+(newCycle.startday+(lastVisitDay-1))+".<br/>" +
								"THAT IS PREVENTING YOU FROM HAVING A CYCLE OF LENGTH "+(parseFloat(newCycle.endday)-parseFloat(newCycle.startday)+1)+".<br/>REMOVE THE EXTRA VISITS BEFORE CHANING THE CYCLE LENGTH.</br></span>";
						if (btn) btn.setDisabled(true);
					}
				}
			}
		}		
		
		
		return txt;
	},
	updateStatusText:function(){
		Ext.getCmp("fldCycleDescription").setValue(this.getStatusText());
	},
	initComponent: function() {
		var t = this;
		var cycle = t.cycle;
		var newCycle = t.newCycle = cycle.copy(null, true); // prevent new id creation
		
		clog("Clara.BudgetBuilder.CycleWindow:initComponent -> cycle:",cycle,"newcycle",t.newCycle);
		
		cycle.startday = (cycle && cycle.startday)?cycle.startday:t.nextavailableday;
		this.listeners = {
				close:function(){
					if (typeof Ext.getCmp("winBudgetStructure") != "undefined") Ext.getCmp("winBudgetStructure").selectArmRow(t.parentArm);
				}
		};
		this.buttons = [
		                {
		                	id: 'btnAddCycleToBudget',
		                	text: 'Save',
		                	handler: function(){
		                		
		                		if(cycle.id == '') { // when we launch there is no id field on the newCycle...
		                			cycle = newCycle;
		                		}else{
		                			newCycle.id = cycle.id;
		                			cycle = newCycle;
		                		}
		                		
		                		var name = jQuery("#fldCycleName").val();
		                		var notes = jQuery("#fldNotes").val();

		                		cycle.repetitions = 1;	// just remove repetitions on save
		                		
		                		if (cycle.simple == true){
		                			cycle.repetitions = 1;
		                			cycle.durationunit = "Day";
		                			cycle.duration = 1;
		                			cycle.repeatforever = false;
		                			cycle.endday = cycle.startday;
		                		}

		                		cycle.name = name;
		                		cycle.notes = notes;
		                		cycle.recalculateDateRanges();
		                		if (cycle.id == "") { // New cycle 
		                			cycle.id = budget.newId();
		                			
		                			var savedCycle = budget.addCycle(t.parentArm, cycle);
		                			// Add visit to new cycle on startday
		                			budget.addVisit(savedCycle, new Clara.BudgetBuilder.Visit({
		                				id: budget.newId(),
		                				unit:'Day',
		                				unitvalue:1,
		                				name:("Day 1"),
		                				notes:'',
		                				subjectcount:1,
		                				cycleindex:1
		                			}), true);
		                			
		                			Clara.BudgetBuilder.MessageBus.fireEvent('epochcontentupdated',t.parentArm.parentEpoch);

		                		}else{
		                			clog("Updating cycle",cycle);
		                			budget.updateCycle(cycle);
		                		}
		                		Ext.getCmp('winCycle').close();


		                	}
		                }
		                ];
		this.items = [
		              {
		            	  xtype: 'textfield',
		            	  id:'fldCycleName',
		            	  fieldLabel: 'Name',
		            	  anchor: '100%',
		            	  value:(cycle && cycle.name)?cycle.name:""
		              },
		              {
		            	  xtype: 'numberfield',
		            	  id:'fldCycleStartDay',
		            	  fieldLabel: 'Start day',
		            	  anchor: '100%',
		            	  value:(cycle && cycle.startday)?cycle.startday:t.nextavailableday,
		            			  listeners: {
		            				  change:function(){
		            					  newCycle.startday = jQuery("#fldCycleStartDay").val();
		            					  t.updateStatusText();
		            				  }
		            			  }
		              },
		              {
		            	  xtype: 'textarea',
		            	  anchor: '100%',
		            	  id:'fldNotes',
		            	  value:(cycle && cycle.notes)?cycle.notes:"",
		            			  fieldLabel: 'Notes'
		              },
		              {
		            	  xtype: 'fieldset',
		            	  title: 'Advanced',
		            	  collapsed:(cycle && cycle.simple),
		            	  id:'fldCycleIsAdvanced',
		            	  checkboxToggle: true,
		            	  items: [{
		            		  xtype: 'combo',
		            		  fieldLabel: 'Cycle type',
		            		  anchor: '100%',
		            		  typeAhead: true,
		            		  triggerAction: 'all',
		            		  store: new Ext.data.SimpleStore({
		            			  fields:['type'],
		            			  data: [['Day'],['Week'],['Month'],['Year']]
		            		  }),
		            		  lazyRender: true,
		            		  id:'fldVisitType',
		            		  displayField:'type',
		            		  mode:'local',
		            		  selectOnFocus:true,
		            		  value:cycle.durationunit,
		            		  listeners:{
		            			  scope:this,
		            			  collapse:function(cb){
		            				  var v = cb.getValue();
		            				  clog("collapse",v);
		            				  t.cycleType = v;
		            				  //cycle.durationunit = v;
		            				  newCycle.durationunit = v;
		            				  t.updateStatusText();
		            			  },

		            			  change:function(f,v,ov){
		            				  t.cycleType = v;
		            				  clog("change",v);
		            				  //cycle.durationunit = v;
		            				  newCycle.durationunit = v;
		            				  t.updateStatusText();
		            			  }
		            		  },
		            		  listClass: 'x-combo-list-small'
		            	  },
		            	  {
		            		  xtype: 'numberfield',
		            		  id:'fldCycleLength',
		            		  fieldLabel: 'Cycle length',
		            		  anchor: '100%',
		            		  value:(cycle && cycle.duration)?cycle.duration:1,
		            				  listeners: {
		            					  change:function(){
		            						  //cycle.duration = jQuery("#fldCycleLength").val();
		            						  newCycle.duration = jQuery("#fldCycleLength").val();
		            						  t.updateStatusText();
		            					  }
		            				  }
		            	  },
		            	  {
		            		  xtype: 'checkbox',
		            		  boxLabel: 'Cycle repeats indefinitely',
		            		  checked:(cycle)?cycle.repeatforever:false,
		            				  id:'fldCycleRepeatForever',
		            				  checked:(cycle)?cycle.repeatforever:false,
		            						  anchor: '100%',
		            						  listeners: {
		            							  check:function(cb,v){
		            								  //cycle.repeatforever = v;
		            								  newCycle.repeatforever = v;
		            								  t.updateStatusText();
		            							  }
		            						  }

		            	  }],
		            	  listeners: {
		            		  collapse:function(){
		            			  cycle.simple = true;
		            			  newCycle.simple = true;
		            			  t.updateStatusText();
		            		  },
		            		  expand:function(){
		            			  cycle.simple = false;
		            			  newCycle.simple = false;
		            			  t.updateStatusText();
		            		  }
		            	  }
		              },

		              {
		            	  xtype: 'displayfield',
		            	  id:'fldCycleDescription',
		            	  value: t.getStatusText(),
		            	  fieldLabel: 'Label',
		            	  anchor: '100%',
		            	  hideLabel: true,
		            	  style: 'font-size:11px;'
		              }
		              ];
		Clara.BudgetBuilder.CycleWindow.superclass.initComponent.call(this);
	}
});