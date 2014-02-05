Ext.ns('Clara.BudgetBuilder');

Clara.BudgetBuilder.VisitGridPanel = Ext.extend(Ext.grid.EditorGridPanel,{
	parentWindow:{},
	initComponent: function() {
		var t = this;
		var config = {
				margins: '0 5 0 0',
				width: 355,
					title: 'Visits',
					view: new Ext.grid.GridView({
						emptyText: '<h1>No visits defined for this cycle.</h1><h2>Click "Add" above to create a new visit.</h2>'
					}),
					id: 'gpVisits',
					store: new Ext.data.ArrayStore({fields:['id','cycleindex','name', 'unit','unitvalue', 'notes','subjectcount']}),
					selModel:new Ext.grid.RowSelectionModel({singleSelect:false}),
					listeners:{
						rowclick:function(g,rowIndex,e){
							clog("g,gsm",g,g.getSelectionModel(),g.getSelectionModel());
							var selectedVisitRecords = g.getSelectionModel().getSelections();
							
							if (selectedVisitRecords.length > 1){
								clog("visit recs",selectedVisitRecords);
								// Clara.BudgetBuilder.MessageBus.fireEvent('visitrecordssselected', selectedVisitRecords);
								Ext.getCmp('btnRemoveVisit').setDisabled(false);
	                			Ext.getCmp('btnCopyVisit').setDisabled(true);
							}
							if (selectedVisitRecords.length == 1){
								Ext.getCmp('btnRemoveVisit').setDisabled(false);
								Ext.getCmp('btnCopyVisit').setDisabled(false);
							}
						},
						afteredit:function(e){
							var visit = budget.getVisit(e.record.get("id"));
							
							var isSimple = (visit.parentEpoch.simple == true || budget.type == "basic");
							
							if(!(isSimple || visit.parentCycle.simple == true) &&  e.record.get("cycleindex") > visit.parentCycle.endday){
					    		//Ext.Msg.alert('Error', 'The day of a visit has to be within the cycle range. Fix the cycle\'s start and end days first!');
							alert('The day of a visit has to be within the cycle range. Fix the cycle\'s start and end days first!');
							e.record.reject();
							return false;
						}else if (!isSimple && e.record.get("cycleindex") < 1){
								alert("You cannot start visits on a negative or zero day on complex phases.");
								e.record.reject();
							} else {
								e.record.commit();
								var d = e.record.data;
								clog("About to update visit with data",d);
								budget.updateVisit(new Clara.BudgetBuilder.Visit({id:d.id, cycleindex:d.cycleindex, name:d.name, unit:'Day',unitvalue:d.cycleindex, notes:d.notes, subjectcount:d.subjectcount}));
								if (t.parentWindow && typeof t.parentWindow.onVisitUpdated == "function") t.parentWindow.onVisitUpdated();
							}
						}
					},
					columns: [
					          {
					        	  xtype: 'gridcolumn',
					        	  menuDisabled:true,
					        	  id:'name',
					        	  dataIndex:'name',
					        	  header: 'Description',
					        	  sortable: false,
					        	  width: 90,
					        	  editor: {
					        		  xtype: 'textfield',
					        		  allowBlank: false,
					        		  listeners:{
					        			  'change':function(fld,nv,ov){
					        				  t.editedVisitname = true;
					        			  }
					        		  }
					        	  }
					          }
					          ,{
					        	  xtype: 'numbercolumn',
					        	  menuDisabled:true,
					        	  header: 'Day',
					        	  id:'cycleindex',
					        	  dataIndex:'cycleindex',
					        	  sortable: false,
					        	  width: 60,
					        	  align:'center',
					        	  format: '0',
							  editor: {
								  xtype: 'numberfield',
								  allowBlank: false,
                                      allowNegative:true,
								  allowDecimals:false
							  }
					          }, {
					        	  xtype: 'numbercolumn',
					        	  id:'subjectcount',
					        	  dataIndex:'subjectcount',
					        	  header: '# Subjects',
					        	  menuDisabled:true,
					        	  sortable: false,
					        	  width: 90,
					        	  align:'center',
					        	  format: '0',
					        	  editor: {
					        		  xtype: 'numberfield',
					        		  allowBlank: false,
					        		  allowDecimals:false
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
					        	        	  itemId: 'btnAddVisitMenu',
					        	        	  id: 'btnAddVisits',

					        	        	  handler:function(){
					        	        		  var v = {};
					        	        		  var a = t.parentWindow.selectedArm;
					        	        		  var c = t.parentWindow.selectedCycle;
					        	        		  var startday = (c.getLastVisit() == null)?1:(c.getLastVisit().cycleindex + 1);
					        	        		  new Clara.BudgetBuilder.MultipleVisitWindow({parentWindow:t.parentWindow, nextavailableday:startday, parentArm:a, parentCycle:c, visit:new Clara.BudgetBuilder.Visit({})}).show();
					        	        	  }

					        	          },

					        	          {
					        	        	  xtype: 'button',
					        	        	  text: 'Remove',
					        	        	  iconCls:'icn-minus-button',
					        	        	  itemId: 'btnRemoveVisit',
					        	        	  disabled:true,
					        	        	  id: 'btnRemoveVisit',
					        	        	  handler:function(){
					        	        		  // Get ID of selected cycle
					        	        		  var selectedRecords = t.getSelectionModel().getSelections();
					        	        		  clog("selectedRecords",selectedRecords);
					        	        		  
					        	        		  Ext.Msg.show({
					        	        			  title:'Remove '+selectedRecords.length+' visits?',
					        	        			  msg: 'You are about to remove <strong>'+selectedRecords.length+' visits</strong>. Are you sure you want to do this?',
					        	        			  buttons: Ext.Msg.YESNOCANCEL,
					        	        			  fn: function(btn){
					        	        				  if (btn == 'yes'){
					        	        					  t.parentWindow.selectedCycle.removeVisits(selectedRecords);
					        	        					  //budget.removeVisit({id:selectedRecord.get('id')});
					        	        					  if (typeof t.parentWindow.onVisitsRemoved == "function") t.parentWindow.onVisitsRemoved();
					        	        				  }
					        	        			  },
					        	        			  animEl: 'elId',
					        	        			  icon: Ext.MessageBox.WARNING
					        	        		  });

					        	        	  }
					        	          },

					        	          {
					        	        	  xtype: 'button',
					        	        	  text: 'Copy',
					        	        	  iconCls:'icn-layers',
					        	        	  itemId: 'btnCopyVisit',
					        	        	  id: 'btnCopyVisit',
					        	        	  disabled:true,
					        	        	  handler:function(){
					        	        		  var selectedRecord = t.getSelectionModel().getSelected();
					        	        		  Clara.BudgetBuilder.selectedVisit = budget.getVisit(selectedRecord.get("id"));
					        	        		  budget.addVisit(t.parentWindow.selectedCycle, Clara.BudgetBuilder.selectedVisit.copy());
					        	        	  }
					        	          },'-',{
					        	        	  xtype: 'button',
					        	        	  text: 'Sort',
					        	        	  hidden:true,
					        	        	  iconCls:'icn-sort-quantity',
					        	        	  itemId: 'btnSortVisits',
					        	        	  id: 'btnSortVisits',
					        	        	  handler:function(){
					        	        		  Ext.getCmp("gpVisits").getStore().sort('cycleindex','ASC');
					        	        	  }
					        	          }
					        	          ]
					          }

			

		};
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		Clara.BudgetBuilder.VisitGridPanel.superclass.initComponent.apply(this, arguments);
	}
});
Ext.reg('claraBudgetVisitGridPanel', Clara.BudgetBuilder.VisitGridPanel);

Clara.BudgetBuilder.VisitValidator = function(t){
	var multiplier = 1;
	if (t.visitType == "Week") { multiplier = 7; }
	else if (t.visitType == "Month") { multiplier = 30; }
	else if (t.visitType == "2 Months") { multiplier = 60; }
	else if (t.visitType == "3 Months") { multiplier = 90; }
	else if (t.visitType == "4 Months") { multiplier = 120; }
	else if (t.visitType == "6 Months") { multiplier = 180; }
	else if (t.visitType == "Year") { multiplier = 365; }
	
	var numberOfVisitsToAdd     = t.repetitions;
	var visitRangeStartDay      = t.startday;
	var visitRangeEndDay        = visitRangeStartDay + ((numberOfVisitsToAdd-1) * multiplier);
	
	t.parentCycle.recalculateDateRanges();
	
	var cycleStartDay = parseInt(t.parentCycle.startday);
	var cycleEndDay = parseInt(t.parentCycle.endday);
	var cycleLength = (cycleEndDay-cycleStartDay) + 1;
	
	var isSimple = (t.parentArm.getParentEpoch().simple == true || budget.type == "basic" || t.parentCycle.simple == true);
	
	if (!isSimple && visitRangeStartDay < 1) return -1;	// complex phases cannot have negative-day or zero-day visits
	
	clog("Clara.BudgetBuilder.VisitValidator",{numberOfVisitsToAdd:numberOfVisitsToAdd,visitRangeStartDay:visitRangeStartDay,visitRangeEndDay:visitRangeEndDay,cycleStartDay:cycleStartDay,cycleEndDay:cycleEndDay,cycleLength:cycleLength,answer:(cycleLength - visitRangeEndDay)});
	return (cycleLength - visitRangeEndDay);
};


Clara.BudgetBuilder.SimpleVisitWindow = Ext.extend(Ext.Window, {
	id:'winBudgetStructure',
    title: 'Edit Visits',
    width: 768,
    height: 518,
    layout: 'fit',
    closable:false,
    border:false,
    selectedEpoch: {},
    selectedArm: {},
    selectedCycle: {},
    errors:[],
    validateEpoch: function() {},
	onVisitUpdated: function(){
    	clog("Clara.BudgetBuilder.SimpleVisitWindow:onVisitUpdated");
    },
    onVisitsRemoved: function(){
    	clog("Clara.BudgetBuilder.SimpleVisitWindow:onVisitsRemoved");
    	this.reloadVisitPanel();
    },
	reloadVisitPanel: function(disableSort){	
		disableSort = disableSort || false;
		var visits = budget.getVisitArrayForCycle(this.selectedCycle);
		Ext.getCmp('gpVisits').getStore().loadData(visits);
		if (!disableSort) Ext.getCmp('gpVisits').getStore().sort([{field:'cycleindex',direction:'ASC'},{field:'name',direction:'ASC'}]);
		Ext.getCmp("btnRemoveVisit").setDisabled(true);
    	Ext.getCmp("btnCopyVisit").setDisabled(true);
	},
	refreshViews: function(){
		Ext.getCmp('gpVisits').getView().refresh();
	},
    initComponent: function() {
		var t = this;
		this.selectedEpoch = Clara.BudgetBuilder.GetActiveEpoch(); 
		this.selectedArm = this.selectedEpoch.arms[0];
		this.selectedCycle = this.selectedArm.cycles[0];
		this.listeners = {
			activate: function(){
				t.reloadVisitPanel();
			}
		};
		
		this.buttons = [{
			id:'btnCloseWindow',
			text:'Close',
			handler: function(){
				t.close();
			}
		}];
		
		this.bbar = new Ext.ux.StatusBar({
            id: 'winstatusbar',
            defaultText: 'Ready.',
            iconCls: 'icon-review-met'
        });
		
        this.items = [
            {xtype:'claraBudgetVisitGridPanel',id: 'gpVisits', parentWindow:t}
        ];

        Clara.BudgetBuilder.SimpleVisitWindow.superclass.initComponent.call(this);

    }
});


Clara.BudgetBuilder.MultipleVisitWindow = Ext.extend(Ext.Window,{
	title:'Visits: Add',
	modal:true,
	width:485,
	height:420,
	layout:'form',
	labelWidth:200,
	padding:6,
	id:'winMultipleVisit',
	parentWindow:{},
	labelSeparator:'',
	visitType:'Day',
	visitName:'',
	nextavailableday:1,
	startday:1,
	repetitions:1,
	subjectcount:0,
	simplevisit:false,
	getStatusText:function(){
		var t = this;
		
		var isSimple = (this.parentArm.getParentEpoch().simple == true || budget.type == "basic" || this.parentCycle.simple == true);
		
		var txt = (!isSimple)?('This cycle starts on Day ' + this.parentCycle.startday + ' and ends on ' + this.parentCycle.endday) + '. And the length of the cycle is ' + (this.parentCycle.endday - this.parentCycle.startday + 1) + ' days <br/>':'';
		
		txt += "About to add "+t.repetitions+" '"+t.visitType+"' visits, ";
		var varray = [t.startday];
		
		var multiplier = 1;
		if (t.visitType == "Week") { multiplier = 7; }
		else if (t.visitType == "Month") { multiplier = 30; }
		else if (t.visitType == "2 Months") { multiplier = 60; }
		else if (t.visitType == "3 Months") { multiplier = 90; }
		else if (t.visitType == "4 Months") { multiplier = 120; }
		else if (t.visitType == "6 Months") { multiplier = 180; }
		else if (t.visitType == "Year") { multiplier = 365; }
		
		// push last day to array

		varray.push(t.startday + ((t.repetitions-1) * multiplier));

		txt += " with the first on Day "+varray[0] + ' of the cycle, ';
		txt += " and the last visit on Day "+varray[1] + ' of the cycle.';

		var btn = Ext.getCmp("btnAddVisitsToBudget");

		//clog(this.parentArm.getParentEpoch().simple);
		//clog(this.parentCycle.simple);
		//clog(budget.type != "basic");
		
		// you were checking for this.parentArm.getParentEpoch().simple == false && this.parentCycle.simple == false && budget.type != "basic"
		// don't think this is right... suppose to be ||? rather than &&
		
		if (!isSimple && this.validateVisitRange() < 0){
			clog("here");
			txt += "<br/><span style='color:red;font-weight:800;'>EXCEEDS CYCLE LENGTH. FIX BEFORE SAVING.";
			if (btn) btn.setDisabled(true);
			
		} else if (isSimple) {
			this.validateVisitRange(false);	// fix cycle length
			if (btn) btn.setDisabled(false);
		} else {
			txt += "<br/><span style='color:green;font-weight:100;'>Valid.";
			if (btn) btn.setDisabled(false);			
		}		

		return txt;
	},
	validateVisitRange: function(){
		return Clara.BudgetBuilder.VisitValidator(this);
	},
	constructor:function(config){		
		Clara.BudgetBuilder.MultipleVisitWindow.superclass.constructor.call(this, config);
	},
	initComponent: function() {
		var t = this;
		t.startday = t.nextavailableday;

		t.buttons = [{text:'Cancel', handler:function(){Ext.getCmp('winMultipleVisit').close();}},{
			id: 'btnAddVisitsToBudget',
			text: 'Save',
			handler: function(){
				var isSimple = (t.parentArm.getParentEpoch().simple == true || budget.type == "basic" || t.parentCycle.simple == true);
				
				if (!isSimple && t.validateVisitRange() < 0){
					alert("You cannot add visits that are outside the range of the cycle. Please check visit day and try again.");
				} else if (!isSimple && t.startday < 1){
					alert("You cannot start visits on a negative or zero day on complex phases. Please check start day and try again.");
				} else {				
					clog("ABOUT TO SAVE VISIT, REPS=",t.repetitions);
					var cidx = t.startday; 
					var multiplier = 1;
					if (t.visitType == "Week") { multiplier = 7; }
					else if (t.visitType == "Month") { multiplier = 30; }
					else if (t.visitType == "2 Months") { multiplier = 60; }
					else if (t.visitType == "3 Months") { multiplier = 90; }
					else if (t.visitType == "4 Months") { multiplier = 120; }
					else if (t.visitType == "6 Months") { multiplier = 180; }
					else if (t.visitType == "Year") { multiplier = 365; }
	
					if (t.repetitions > 0){	
	
						for (var i=0;i<t.repetitions;i++){
							v = new Clara.BudgetBuilder.Visit({});
							v.unit = t.visitType;
							var day = (cidx + (i * multiplier));
							var addition = day % multiplier == 0 ? 0: 1;	    				
							v.unitvalue = Math.floor(day / multiplier) + ((multiplier>1)?addition:0);
							v.name = (t.visitName == "")?(t.visitType + " " + v.unitvalue):t.visitName;	
							v.cycleindex = cidx + (i * multiplier);
							v.notes = '';
							v.subjectcount = t.subjectcount;
							v.id = budget.newId(); 	
							budget.addVisit(t.parentCycle, v, true);
							v = null;
						}
						t.parentCycle.recalculateDateRanges(); // to get the cycle recalculated right...
						Clara.BudgetBuilder.MessageBus.fireEvent('visitadded', v);
						t.parentWindow.onVisitUpdated();
						t.close();					
					} else{
						alert("Please select a number of visits larger than zero.");
					}
				
				}
			}
		}];
		t.items = [{
			xtype:'displayfield',
			hidden: ( budget.isSimple() || t.parentArm.getParentEpoch().simple),
			value:'<strong>Note:</strong> All visit days are relative to the cycle.'
		},{
			fieldLabel:'Set visit name(s): (leave blank for default name(s))',
			xtype:'textfield',
			id:'fldVisitNames',
			value:"",
			anchor: '100%',
			listeners: {
				change:function(f,nv,ov){
					if (nv != "") t.visitName = nv;
				}
			}
		},{
			xtype: 'combo',
			fieldLabel: 'Visit Type',
			anchor: '100%',
			typeAhead: true,
			triggerAction: 'all',
			store: new Ext.data.SimpleStore({
				fields:['type','desc'],
				data: [['Day','Day'],['Week', 'Week (7 days)'],['Month', 'Month (30 days)'],['2 Months', '2 Months (60 days)'],['3 Months', '3 Months (90 days)'],['4 Months', '4 Months (120 days)'],['6 Months', '6 Months (180 days)'],['Year', 'Year (365 days)']]
			}),
			lazyRender: true,
			id:'fldVisitType',
			displayField:'desc',
			valueField:'type',
			mode:'local',
			selectOnFocus:true,
			value:t.visitType,
			listeners:{
				scope:this,
				change:function(f,v,ov){
					t.visitType = v;
					Ext.getCmp("lblSimpleVisitDesc").setValue(t.getStatusText());
				}
			},
			listClass: 'x-combo-list-small'
		},{
			fieldLabel:'How many visits do you want to add?',
			xtype:'numberfield',
			id:'fldNumVisits',
			allowDecimals:false,
			value:1,
			anchor: '100%',
			listeners: {
				change:function(){
					t.repetitions = parseInt(jQuery("#fldNumVisits").val());
					Ext.getCmp("lblSimpleVisitDesc").setValue(t.getStatusText());
				}
			}
		},{
			fieldLabel:'Add visits starting on what day?',
			xtype:'numberfield',
			id:'fldStartDay',
			allowDecimals:false,
            allowNegative:false,
			value:t.nextavailableday,
			anchor: '100%',
			listeners: {
				change:function(){
					t.startday = parseInt(jQuery("#fldStartDay").val());
					Ext.getCmp("lblSimpleVisitDesc").setValue(t.getStatusText());
				}
			}
		},{
			fieldLabel:'How many subjects for each visit?',
			xtype:'numberfield',
			id:'fldNumSubjects',
			allowDecimals:false,
			value:0,
			anchor: '100%',
			listeners: {
				change:function(){
					t.subjectcount = parseInt(jQuery("#fldNumSubjects").val());
				}
			}
		},{
			xtype:'displayfield',
			id:'lblSimpleVisitDesc',
			text:'This visit will begin on day '+t.startday
		}];
		Clara.BudgetBuilder.MultipleVisitWindow.superclass.initComponent.call(this);
	}
});
