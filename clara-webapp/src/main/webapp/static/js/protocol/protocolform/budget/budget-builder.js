Ext.ns('Clara', 'Clara.BudgetBuilder');
Ext.QuickTips.init();

// Set up HTML encoder/decoder for XML
Encoder.EncodeType = "numerical";

Clara.BudgetBuilder.SimpleBudgetVisitCount = 0;
Clara.BudgetBuilder.SimpleBudgetSubjectCount = 0;
Clara.BudgetBuilder.ShowNotes = true;
Clara.BudgetBuilder.AllowExternalCostEditing = false;

var exportExcelMask = new Ext.LoadMask(Ext.getBody(), {id:'maskExportExcel',msg:"Saving Excel document, please wait..."});
var loadExternalExpensesMask = new Ext.LoadMask(Ext.getBody(), {id:'maskLoadExternalExpenses',msg:"Checking for changes (expenses), please wait..."});

//Ext.Window({id:'maskCompareVersions',msg:"Comparing versions. This may take a few moments..."});
var globalAjaxMask = new Ext.LoadMask(Ext.getBody(), {id:'maskGlobal',msg:"Please wait..."});
var redrawMask = new Ext.LoadMask(Ext.getBody(), {id:'maskRedraw',msg:"Redrawing, please wait..."});

closeBudgetWindow = function(){
	closeCurrentForm(function(){window.close();});
};

Clara.BudgetBuilder.GetPharmacyStatus= function(){
	var status="pharmacy review not yet requested"; 
	url = appContext + "/ajax/protocols/" + claraInstance.id + "/protocol-forms/" + claraInstance.form.id +"/review/review-status";

	jQuery.ajax({
		  type: 'GET',
		  async:false,
		  url: url,
		  data: {
			  committee: "PHARMACY_REVIEW"
		  },
		  success: function(data){
			  clog("PHARMSTAT SUCCESS:",data,jQuery(data).find("result").text().toLowerCase());
			  status = jQuery(data).find("result").text().toLowerCase();
		  },
		  error: function(){
			  status = "errorLoadingPharmacyStatus";
		  },
		  dataType: 'xml'
	});
	
	return status;
};

Clara.BudgetBuilder.GetActiveEpoch= function(){
	return Ext.getCmp("budget-tabpanel").activeEpoch;
};

Clara.BudgetBuilder.GetSelectedEpoch= function(){
	if (typeof Ext.getCmp('winBudgetStructure') == 'undefined') return null;
	else return Ext.getCmp('winBudgetStructure').selectedEpoch;
};

Clara.BudgetBuilder.GetProtocolCoverSheet = function(showIds, printOnLoad){
	showIds = showIds || [];	
	printOnLoad = printOnLoad || false;
	var showIdsUrl = (showIds.length > 0)?('&showIds='+showIds.join(',')):'';
	var url = appContext+"/protocols/"+claraInstance.id+"/protocol-forms/"+claraInstance.form.id+"/budgets/budgetbuilder?coversheet"+showIdsUrl+"&printOnLoad="+printOnLoad;
	window.open(url,'','width=800,height=600,location=0,menubar=0,scrollbars=1,status=0,toolbar=0,resizable=1');
};

Clara.BudgetBuilder.SetFA = function(){
	
	var winFA = new Ext.Window({
		id:"winSetBudgetFA",
		width:280,
		height:130,
		modal:true,
		padding:6,
		title:'Set Budget F&A',
		layout:'form',
		items:[{
			xtype:'numberfield',
			id:'fldBudgetFAPercent',
			allowBlank:false,
			allowDecimal:true,
			allowNegative:false,
			fieldLabel:'Enter a new F&A (in percent)',
			value:budget.FA
		}],
		buttons:[{
			text:"Cancel",
			handler:function(){
				Ext.getCmp("winSetBudgetFA").close();
			}
		},{
			text:'OK',
			handler:function(){
				if (Ext.getCmp("fldBudgetFAPercent").validate()){
					var newFA = Ext.getCmp("fldBudgetFAPercent").getValue();
					Clara.BudgetBuilder.SaveAction = "Budget F&A";
		    		budget.updateFA(parseFloat(newFA));
		    		budget.save();
		    		Ext.getCmp("winSetBudgetFA").close();
				} else {
					alert("Invalid F&A. Check the value and try again.");
				}
			}
		}]
	});
	winFA.show();
	
};

Clara.BudgetBuilder.PromptSimpleBudgetVisits = function(){
	Ext.Msg.show({
		title:'Number of visits',
		msg:'How many visits do you want to start with?',
		buttons:Ext.Msg.OK,
		icon: Ext.MessageBox.info,
		value:1,
		modal:true,
		closable:false,
		prompt:true,
		fn:function(btn, text){
			Clara.BudgetBuilder.SimpleBudgetVisitCount = parseInt(text);
	    	if (btn == 'ok' && Clara.BudgetBuilder.SimpleBudgetVisitCount > 0){
	    		Clara.BudgetBuilder.PromptSimpleBudgetSubjectCount();
	    	} else {
	    		Clara.BudgetBuilder.PromptSimpleBudgetVisits();
	    	}
		}
	});
};

Clara.BudgetBuilder.PromptSimpleBudgetSubjectCount = function(){
	Ext.Msg.show({
		title:'Number of subjects',
		msg:'How many subjects are you planning to enroll?',
		buttons:Ext.Msg.OK,
		closable:false,
		icon: Ext.MessageBox.info,
		value:1,
		prompt:true,
		fn:function(btn, text){
			Clara.BudgetBuilder.SimpleBudgetSubjectCount = parseInt(text);
	    	if (btn == 'ok' && Clara.BudgetBuilder.SimpleBudgetSubjectCount > 0){
	    		var e = new Clara.BudgetBuilder.Epoch({name:"Basic Study", id:budget.newId(), simple:true});
	    		var a = new Clara.BudgetBuilder.Arm({id:budget.newId()});
	    		var c = new Clara.BudgetBuilder.Cycle({id:budget.newId(),simple:true});
	    		for (var i=0;i<parseInt(Clara.BudgetBuilder.SimpleBudgetVisitCount); i++){
	    			c.visits.push(new Clara.BudgetBuilder.Visit({id:budget.newId(), subjectcount: Clara.BudgetBuilder.SimpleBudgetSubjectCount, unit:'Day', unitvalue:i+1, cycleindex:i+1, name:"Day "+(i+1)}));
	    		}
	    		c.recalculateDateRanges();
	    		a.cycles.push(c);
	    		e.arms.push(a);
	    		budget.addEpoch(e);
	    		budget.save();
	    		
	    		// activate the phase.
	    		Ext.getCmp('budget-tabpanel').setActiveTab(0);
	    		if(Ext.isChrome){
	                location.reload(true);
	    		}
	    	} else {
	    		Clara.BudgetBuilder.PromptSimpleBudgetSubjectCount();
	    	}
		}
	});
};

Clara.BudgetBuilder.EditActivePhaseNotes = function(){
	Ext.Msg.show({
		title:'Phase notes',
		msg:'Please enter the notes for this phase below.',
		buttons:Ext.Msg.OKCANCEL,
		multiline:true,
		width:500,
		height:400,
		icon: Ext.MessageBox.info,
		value:Ext.getCmp("budget-tabpanel").activeEpoch.notes,
		prompt:true,
		fn:function(btn, text){
	    	if (btn == 'ok'){
	    		Ext.getCmp("budget-tabpanel").activeEpoch.notes = jQuery.trim(text);
	    		Clara.BudgetBuilder.SaveAction = "Edit Phase Notes";
	    		Clara.BudgetBuilder.MessageBus.fireEvent('epochcontentupdated', Ext.getCmp("budget-tabpanel").activeEpoch);
	    		budget.save();
	    	}
		}
	});
};

Clara.BudgetBuilder.SetVisibleEpochSubjectCount = function(count){
	Ext.Msg.show({
		title:'Number of subjects',
		msg:(budget.budgetType == 'simple')?'How many subjects?':'How many subjects? (NOTE: This will change for ALL visits for this study phase)',
		buttons:Ext.Msg.OKCANCEL,
		icon: Ext.MessageBox.info,
		value:count,
		prompt:true,
		fn:function(btn, text){
	    	if (btn == 'ok'){
	    		Clara.BudgetBuilder.SaveAction = "Subject Count";
	    		//budget.updateFA(parseFloat(text));
	    		budget.setEpochSubjectCount(Ext.getCmp("budget-tabpanel").activeEpoch,(parseInt(text) || 0));
	    		budget.save();
	    	}
		}
	});
};

// Listeners
Clara.BudgetBuilder.MessageBus.addListener('budgetloaded', function(){
	// Check locked, set background image
	if (budget.locked){
		jQuery("body").css('background-image', 'url('+appContext+'/static/images/bg_budget_locked.gif)');
		jQuery(".clara-budget-protocol-info h1").append(" (<strong>This budget is LOCKED to changes.</strong>)");
	}
	globalAjaxMask.hide();
});


Clara.BudgetBuilder.MessageBus.addListener('beforebudgetsave', function(){
	Ext.getBody().mask('Saving, please wait...', 'x-mask-loading');
});
Clara.BudgetBuilder.MessageBus.addListener('afterbudgetsave', function(){
	Ext.getBody().unmask();
});
Clara.BudgetBuilder.MessageBus.addListener('onbudgetsaveerror', function(){
	Ext.getBody().unmask();
});
Clara.BudgetBuilder.MessageBus.addListener('beforebudgetload', function(){
	Ext.getBody().mask('Loading, please wait...', 'x-mask-loading');
});
Clara.BudgetBuilder.MessageBus.addListener('budgetversionloaded', function(){
	Ext.getBody().unmask();
});
Clara.BudgetBuilder.MessageBus.addListener('budgetloaded', function(){
	Ext.getBody().unmask();
	budget.sortAllArms();
});



Clara.BudgetBuilder.MessageBus.addListener('procedurechanged',function(p){
	clog("About to highlight procedure", p);
	Ext.getCmp("budget-tabpanel").activeTab.highlightProcedure(p);
},null,{delay:500});

Clara.BudgetBuilder.MessageBus.addListener('budgettypechanged', function(budgetType){
	var isSimple = (budget.budgetType == "simple" || budget.budgetType == "basic");
	var isStandard = !isSimple;

	Ext.getCmp('btnAddEpoch').setVisible(isStandard);
	Ext.getCmp('btnSwitchToComplex').setVisible(isSimple);
	
	// This should only run once, since we only allow changing to "Simple" on empty, new budgets
	if (isSimple && budget.epochs.length == 0){
		//create structure
		
		
		Clara.BudgetBuilder.PromptSimpleBudgetVisits();

	} else if (isStandard  && budget.epochs.length == 0){
		// prompt for a new epoch
		// new Clara.BudgetBuilder.NewEpochWindow().show();
	}
	
});

Clara.BudgetBuilder.MessageBus.addListener('epochcontentupdated', function(epoch){
	if (typeof Ext.getCmp('winBudgetStructure') != 'undefined'){
		
		Ext.getCmp('winBudgetStructure').validateEpoch();
		budget.save();

		if (typeof Ext.getCmp('winBudgetStructure').refreshViews == 'function') Ext.getCmp('winBudgetStructure').refreshViews();

	}
});

Clara.BudgetBuilder.MessageBus.addListener('onbudgetsaveerror',function(budget){
	cwarn("onbudgetsaveerror.");
	var debugInfo = "";
	
	debugInfo += "protocol url: "+appContext+"/protocols/"+claraInstance.id;
	debugInfo += "\nbudget url: "+appContext+"/protocols/"+claraInstance.id+"/protocol-forms/"+claraInstance.form.id+"/budgets/budgetbuilder";
	debugInfo += "\nbudget xml url: "+appContext+"/ajax/protocols/"+claraInstance.id+"/protocol-forms/"+claraInstance.form.id+"/budgets/get";
	debugInfo += "\nlastaction: "+Clara.BudgetBuilder.SaveAction;
	debugInfo += "\nuser: "+claraInstance.user.id;
	
	Ext.Msg.show({
		   title:'Error saving budget',
		   msg: '<h1>There was en error saving the budget.</h1>Click "Ok" to refresh this page and try again.',
		   buttons: Ext.Msg.OK,
		   //multiline:true,
		   fn: function(){
			 location.reload();  
		   },
		   //value: debugInfo,
		   animEl: 'elId',
		   icon: Ext.MessageBox.ERROR
		});
});

Clara.BudgetBuilder.MessageBus.addListener('beforebudgetexport',function(budget){
	exportExcelMask.show();
});

Clara.BudgetBuilder.MessageBus.addListener('afterbudgetexport',function(budget){
	//cdebug("after export fired.");
	alert("Budget documents uploaded.");
	exportExcelMask.hide();
	location.reload();
});

Clara.BudgetBuilder.MessageBus.addListener('onbudgetexporterror',function(budget){
	//cdebug("error export fired.");
	alert("Budget documents uploaded.");
	exportExcelMask.hide();
	location.reload();
});

Clara.BudgetBuilder.MessageBus.addListener('beforeloadexternalexpenses',function(budget){
	loadExternalExpensesMask.show();
});

Clara.BudgetBuilder.MessageBus.addListener('afterloadexternalexpenses',function(budget){
	//cdebug("after loading external expenses fired.");
	loadExternalExpensesMask.hide();
});

Clara.BudgetBuilder.MessageBus.addListener('onloadexternalexpenseserror',function(budget){
	//cdebug("error loading external expenses");
	loadExternalExpensesMask.hide();
});

Clara.BudgetBuilder.MessageBus.addListener('armadded', function(arm){
	cdebug("EVENT arm added. arm:");
	////cdebug(arm);
	Clara.BudgetBuilder.SaveAction = "Add arm";
	Clara.BudgetBuilder.MessageBus.fireEvent('epochcontentupdated', Clara.BudgetBuilder.GetSelectedEpoch());
	////cdebug("Clara.BudgetBuilder.StructureWindow: add: REFRESHING VISIT GRIDPANEL");
	Ext.getCmp('winBudgetStructure').selectedArm = arm;
	Ext.getCmp('gpVisits').getStore().removeAll();
	Ext.getCmp('gpCycles').getStore().removeAll();
	Ext.getCmp('winBudgetStructure').reloadArmPanel();
});

Clara.BudgetBuilder.MessageBus.addListener('armupdated', function(arm){
	cdebug("EVENT arm up");
	Clara.BudgetBuilder.SaveAction = "Edit arm";
	Clara.BudgetBuilder.MessageBus.fireEvent('epochcontentupdated', Clara.BudgetBuilder.GetSelectedEpoch());
	////cdebug("Clara.BudgetBuilder.StructureWindow: update: REFRESHING TAB");
});

Clara.BudgetBuilder.MessageBus.addListener('armremoved', function(arm){
	cdebug("EVENT arm re");
	Clara.BudgetBuilder.SaveAction = "Remove arm";
	Ext.getCmp('winBudgetStructure').selectedArm = {};
	////cdebug("Clara.BudgetBuilder.StructureWindow: remove: REFRESHING ARM GRIDPANEL");
	Ext.getCmp('gpVisits').getStore().removeAll();
	Ext.getCmp('gpCycles').getStore().removeAll();
	Ext.getCmp('winBudgetStructure').reloadArmPanel();
	Clara.BudgetBuilder.MessageBus.fireEvent('epochcontentupdated', Clara.BudgetBuilder.GetSelectedEpoch());

	Ext.getCmp('gpCycles').disable();
	Ext.getCmp('gpVisits').disable();
});

Clara.BudgetBuilder.MessageBus.addListener('armselected', function(arm){
	cdebug("EVENT arm select");
	////cdebug("Clara.BudgetBuilder.StructureWindow: select: LOADING CYCLES");
	Ext.getCmp('winBudgetStructure').selectedArm = arm;
	Ext.getCmp('gpVisits').getStore().removeAll();
	Ext.getCmp('gpVisits').disable();
	Ext.getCmp('gpCycles').enable();
	Ext.getCmp("btnCopyArm").setDisabled(false);
	Ext.getCmp("btnRemoveArm").setDisabled(false);
	Ext.getCmp('winBudgetStructure').reloadCyclePanel(arm);
});

Clara.BudgetBuilder.MessageBus.addListener('cycleselected', function(cycle){
	////cdebug("EVENT cycleselect");
	////cdebug("Clara.BudgetBuilder.StructureWindow: select: LOADING VISITS");
	
});

Clara.BudgetBuilder.MessageBus.addListener('visitadded', function(visit){
	cdebug("EVENT visit added");
	Clara.BudgetBuilder.SaveAction = "Add visit";
	Clara.BudgetBuilder.MessageBus.fireEvent('epochcontentupdated', Clara.BudgetBuilder.GetSelectedEpoch());
});

Clara.BudgetBuilder.MessageBus.addListener('visitupdated', function(visit){
	clog("EVENT visit updated",visit);
	Clara.BudgetBuilder.SaveAction = "Edit visit";
	Clara.BudgetBuilder.MessageBus.fireEvent('epochcontentupdated', Clara.BudgetBuilder.GetSelectedEpoch());
	////cdebug("Clara.BudgetBuilder.StructureWindow: update: REFRESHING TAB");
});

Clara.BudgetBuilder.MessageBus.addListener('visitselected', function(visit){
	clog("visit selcted",visit);
	Clara.BudgetBuilder.selectedVisit = visit;
});

Clara.BudgetBuilder.MessageBus.addListener('cyclevisitsremoved', function(){
	////cdebug("EVENT visit removed");
	Clara.BudgetBuilder.SaveAction = "Removed multiple visits";
	Ext.getCmp('winBudgetStructure').selectedVisit = {};
	Clara.BudgetBuilder.MessageBus.fireEvent('epochcontentupdated', Clara.BudgetBuilder.GetSelectedEpoch());
});

Clara.BudgetBuilder.MessageBus.addListener('armcyclesremoved', function(){
	////cdebug("EVENT visit removed");
	Clara.BudgetBuilder.SaveAction = "Removed multiple cycles";
	Ext.getCmp('winBudgetStructure').selectedCycle = {};
	Ext.getCmp('winBudgetStructure').selectedVisit = {};
	Clara.BudgetBuilder.MessageBus.fireEvent('epochcontentupdated', Clara.BudgetBuilder.GetSelectedEpoch());
});

Clara.BudgetBuilder.MessageBus.addListener('visitremoved', function(visit){
	////cdebug("EVENT visit removed");
	Clara.BudgetBuilder.SaveAction = "Remove visit";
	Clara.BudgetBuilder.MessageBus.fireEvent('epochcontentupdated', Clara.BudgetBuilder.GetSelectedEpoch());
});

Clara.BudgetBuilder.MessageBus.addListener('cycleadded', function(cycle){
	////cdebug("EVENT add cyc");
	Clara.BudgetBuilder.SaveAction = "Add cycle";
	Clara.BudgetBuilder.MessageBus.fireEvent('epochcontentupdated', Clara.BudgetBuilder.GetSelectedEpoch());
	////cdebug("Clara.BudgetBuilder.StructureWindow: add: REFRESHING CYCLE GRIDPANEL");
	Ext.getCmp('winBudgetStructure').reloadCyclePanel();
});

Clara.BudgetBuilder.MessageBus.addListener('cycleupdated', function(visit){
	////cdebug("EVENT ed cyc");
	Clara.BudgetBuilder.SaveAction = "Edit cycle";
	Clara.BudgetBuilder.MessageBus.fireEvent('epochcontentupdated', Clara.BudgetBuilder.GetSelectedEpoch());
	////cdebug("Clara.BudgetBuilder.StructureWindow: update: REFRESHING TAB");
});

Clara.BudgetBuilder.MessageBus.addListener('cycleremoved', function(cycle){
	////cdebug("EVENT rem cyc");
	Clara.BudgetBuilder.SaveAction = "Remove cycle";
	Ext.getCmp('winBudgetStructure').selectedCycle = {};
	Clara.BudgetBuilder.MessageBus.fireEvent('epochcontentupdated', Clara.BudgetBuilder.GetSelectedEpoch());
	////cdebug("Clara.BudgetBuilder.StructureWindow: remove: REFRESHING CYCLE GRIDPANEL");
	Ext.getCmp('gpVisits').getStore().removeAll();
	Ext.getCmp('winBudgetStructure').reloadCyclePanel();
});

// end listeners


Clara.BudgetBuilder.UpdateTotals = function(type){
	var epoch = Ext.getCmp("budget-tabpanel").activeEpoch;
	Clara.BudgetBuilder.TotalBy = type;
	Clara.BudgetBuilder.MessageBus.fireEvent('epochcontentupdated', epoch);
};

Clara.BudgetBuilder.EditVisitColumn = function(visitid){
	var v = budget.getVisit(visitid);
	//cdebug(v);
	new Clara.BudgetBuilder.EditVisitProcedurePopup({editType:"v",cid:"x-grid3-hd-v"+visitid, vid:visitid}).show();
};


Clara.BudgetBuilder.EditActiveEpochProcedure = function(procid){
	var epoch = Ext.getCmp("budget-tabpanel").activeEpoch;
	var proc = epoch.getProcedureById(procid);
	if (proc.type.indexOf("drug_") == -1) {
		var editWindow = new Clara.BudgetBuilder.ProcedureWindow({modal:true, procedure:proc, proceduretype:proc.type});
	} else {
		// This procedure is REALLY a drug
		var darray = proc.type.split("_");
		var editWindow = new Clara.BudgetBuilder.DrugWindow({modal:true, drug:proc, drugtype:darray[1]});
	}
	editWindow.show();
};

Clara.BudgetBuilder.PromptNewBudget = function(callback){
	Ext.Msg.show({
		title:"New budget",
		closable:false,
		msg:"<h1>How would you like to enter the budget for this study?</h1><p><strong>Basic Budget</strong> is for visit-only budgets.</p><p><strong>Complex Budget</strong> enables all options, including adding arms, phases, cycles and visits.</p>", 
		buttons:{
			yes: "Basic Budget",
			no: "Complex Budget",
			cancel: "Load from saved template..."
		},
		fn: function(btn){
			if (btn != 'cancel'){
				if (btn == "no"){
					budget.budgetType = "complex";
				} else {
					budget.budgetType = "basic";
				}
				clog("budget.budgetType set to "+budget.budgetType);
				if (callback) callback();
				Clara.BudgetBuilder.MessageBus.fireEvent("budgettypechanged", budget.budgetType);
			}
			else{
				new Clara.TemplateLoadWindow({
	    			templateStore:Clara.BudgetBuilder.BudgetTemplateStore,
	    			cancelCallback: function(){Clara.BudgetBuilder.PromptNewBudget();},
	    			loadTemplateCallback: function(xml){
	    				budget.fromXML(xml);
	    				if (callback) callback();	    				
	    			}
	    		}).show();
			}
			
		}
		
	});
	return false;
};

Clara.BudgetBuilder.ConfirmRemoveEpoch = function(tp,t){
	Ext.Msg.show({
		title:"WARNING: About to delete study phase",
		msg:"Are you sure you want to delete this study phase?", 
		buttons:Ext.Msg.YESNOCANCEL,
		icon:Ext.MessageBox.WARNING,
		fn: function(btn){
			if (btn == 'yes'){
				tp.remove(t);
				budget.removeEpoch(t.epoch);
				Clara.BudgetBuilder.SaveAction = "Remove phase";
				budget.save();
			}
		}
		
	});
	return false;
};

Clara.BudgetBuilder.ShowProcedureNotes = function(procid){
	var epoch = Ext.getCmp("budget-tabpanel").activeEpoch,
		proc = epoch.getProcedureById(procid),
		notes = '';
	
	if (proc.notes !== ''){
		notes +="<div class='proc-notes proc-notes-billing'><h1>Billing Notes</h1>"+proc.notes+"</div>";
	}
	if (proc.clinicalNotes !== ''){
		notes +="<div class='proc-notes proc-notes-clinical'><h1>Clinical Notes</h1>"+proc.clinicalNotes+"</div>";
	}
	
	Ext.Msg.show({
		   title:proc.getDescription(),
		   msg: notes,
		   minWidth:400,
		   maxWidth:600,
		   buttons: Ext.Msg.OK,
		   animEl:'procnotelink-'+procid,
		   iconCls:'icn-sticky-note'
		});
	
	return false;
};

Clara.BudgetBuilder.ConfirmRemoveProcedure = function(procid){
	
	Ext.Msg.show({
		title:"WARNING: About to delete procedure",
		msg:"Are you sure you want to delete this procedure?", 
		buttons:Ext.Msg.YESNOCANCEL,
		icon:Ext.MessageBox.WARNING,
		fn: function(btn){
			if (btn == 'yes'){
				var epoch = Ext.getCmp("budget-tabpanel").activeEpoch,
					previousProcedure = null,
					currentProcId = null,
					previousProcId = null;
				
				// First traverse the gridpanel via jquery and get the previous procedure (so we can highlight it)
				jQuery(".procrow-desc").each(function(){
					previousProcId = currentProcId;
					currentProcId = jQuery(this).attr("id");
					if (currentProcId === "procrow-desc-"+procid){
						clog("FOUND "+currentProcId+" TO DELETE. Previous was:",previousProcId);
						if (previousProcId !== null) {
							previousProcedure = epoch.getProcedureById(previousProcId.split("-")[2]);
						}
					}
				});
				
				
				epoch.removeProcedureById(procid);
				//cdebug(epoch);
				Clara.BudgetBuilder.SaveAction = "Remove procedure";
				Clara.BudgetBuilder.MessageBus.fireEvent('epochcontentupdated', epoch);
				clog("Will highlight previous procedure?",previousProcedure);
				if (previousProcedure !== null) Clara.BudgetBuilder.MessageBus.fireEvent('procedurechanged', previousProcedure);
				budget.save();
			}
		}
		
	});
	return false;
};

function renderBudgetBuilder(){

	
	
	budget.load();
	
	if (budget.isNew()){
		cwarn("NEW BUDGET. Setting FA?");
		if (claraInstance.budget.defaultFA) {
			budget.FA = claraInstance.budget.defaultFA;
			budget.initialExpenseFA = claraInstance.budget.defaultFA;
			budget.initialInvoicableFA = claraInstance.budget.defaultFA;
		}
		//cdebug("New budget, set default FA to ",budget.FA);
		var callback = function(){ 
			Clara.BudgetBuilder.SyncExternalExpenses();
			budget.stamp = parseInt(""+budget.stamp)+1;
			if (!budget.isSimple()) budget.save(budget.toXML(),true);	// reload for all browsers (chrome is MEAN)
			else budget.save();	// reload for all browsers (chrome is MEAN)
		};
		Clara.BudgetBuilder.PromptNewBudget(callback);
		
	}
	
	pharmacyStatus = Clara.BudgetBuilder.GetPharmacyStatus();
		
	var vpBottomBar = new Ext.Toolbar({
		items: [{
			id: 'btnBudgetExpenses',
			disabled:false,
			iconCls:'icn-calculator',
			// disabled:!Clara.BudgetBuilder.canEdit(),
			text: 'Expenses',
			handler: function(){
				var w = new Clara.BudgetBuilder.ExpensesWindow({modal:true});
				w.show();
			}},
			{
				
					id: 'btnAddEpoch',
					iconCls:'icn-ui-tab--plus',
					text: 'Add Phase..',
					hidden: (budget.budgetType == "simple" || budget.budgetType == "basic"),
					disabled:!Clara.BudgetBuilder.canEdit(),
					handler: function(){
						new Clara.BudgetBuilder.NewEpochWindow().show();
					}
				
			},{
				iconCls:'icn-balloon',
				text: 'Show notes',
				pressed:true,
				enableToggle:true,
				toggleHandler: function(btn,st){
					Clara.BudgetBuilder.ShowNotes = st;
					Clara.BudgetBuilder.MessageBus.fireEvent('epochcontentupdated', Ext.getCmp('budget-tabpanel').activeEpoch);
				}
			},'->',{
				text:'Show Cover Page',
				//disabled:!Clara.BudgetBuilder.canEdit(),
				iconCls:'icn-blog',
				handler: function(){
					Clara.BudgetBuilder.GetProtocolCoverSheet();
				}
			},{
				text:'Upload to Document Section',
				id:'btnUploadToDocs',
				iconCls:'icn-table-share',
				disabled:!Clara.BudgetBuilder.canEdit(),
				handler: function(){
					exportExcelMask.show();									
					budget.saveExcelDocument();
					exportExcelMask.hide();
				}
			},{
				text: 'Documents',
				iconCls:'icn-document',
				handler: function(){
					// 
					
					var winDocs = new Ext.Window({
						width:750,
						iconCls:'icn-document',
						height:500,
						modal:true,
						title:'Protocol Documents',
						layout:'fit',
						items:[new Clara.Documents.Panel({readOnly:true, border:false})]
					});
					winDocs.show();
				}
			},{
						id: 'btnTools',
						// disabled:!Clara.BudgetBuilder.canEdit(),
						text:'Setup & Settings',
						iconCls:'icn-wrench',
						menu:[{
							text:(budget.locked)?'Unlock Budget':'Lock Budget',
							disabled:!Clara.BudgetBuilder.canEdit() || !Clara.BudgetBuilder.canLock(),
							iconCls:(budget.locked)?'icn-unlock':'icn-lock',
							handler: function(){
								var title = (budget.locked)?'Unlock Budget':'Lock Budget';
								var msg = (budget.locked)?'This will allow anyone with budget editing privileges to make changes to the budget.':'This will prevent anyone with budget editing privileges from make changes to the budget.';
								
								Ext.Msg.show({
									title:title,
									msg:msg,
									buttons:Ext.Msg.OKCANCEL,
									icon: Ext.MessageBox.alert,
									fn:function(btn, text){
								    	if (btn == 'ok'){
								    		budget.locked = !budget.locked;	
								    		budget.save(budget.toXML(), true);						    		
								    	}
									}
								});
							}
						},{
							text:'Check pharmacy review status',
							iconCls:'icn-auction-hammer--exclamation',
							handler: function(){
								var myMask = new Ext.LoadMask(Ext.getBody(), {msg:"Checking pharmacy status, please wait..."});
								myMask.show();
								var status = Clara.BudgetBuilder.GetPharmacyStatus();
								myMask.hide();
								if (status && status != "") alert("Pharmacy review status: "+status);
								else if (status == "") alert("PI has not yet requested a pharmacy review.");
								else alert("Could not determine pharmacy review status.");
							}
						},{
							text:'Set Budget F&A',
							disabled:!Clara.BudgetBuilder.canEdit(),
							iconCls:'icn-user--pencil',
							handler: function(){
								Clara.BudgetBuilder.SetFA();
							}
						}, {
							text:'Switch To Complex Mode',
							disabled:!Clara.BudgetBuilder.canEdit(),
							id:'btnSwitchToComplex',
							iconCls:'icn-exclamation',
							hidden: (!budget.isSimple()),
							handler: function(){
								
								var title = 'Switch To Complex Mode';
								var msg = 'Are you sure you want to switch to complex mode? <br/><br/> WARNING: This process is not reversible.';
								
								Ext.Msg.show({
									title:title,
									msg:msg,
									buttons:Ext.Msg.OKCANCEL,
									icon: Ext.MessageBox.alert,
									fn:function(btn, text){
								    	if (btn == 'ok'){
								    		
								    		var myMask = new Ext.LoadMask(Ext.getBody(), {msg:"Converting to complex budget, please wait..."});
								    		myMask.show();
								    		
								    		budget.budgetType = 'complex';	
								    		budget.save(budget.toXML(),true);
								    		
								    	}
									}
								});
							}
						},
						{
							text:'Load from template..',
							disabled:!Clara.BudgetBuilder.canEdit(),
							iconCls:'icn-script--arrow',
							handler: function(){

					    		new Clara.TemplateLoadWindow({
					    			templateStore:Clara.BudgetBuilder.BudgetTemplateStore,
					    			loadTemplateCallback: function(xml){
					    				budget.fromXML(xml);
					    				Clara.BudgetBuilder.SyncExternalExpenses();
										budget.save(budget.toXML(),true);	// reload for all browsers (chrome is MEAN)			
					    			}
					    		}).show();
							}
						},{
							text:'Save as template..',
							disabled:!Clara.BudgetBuilder.canEdit(),
							iconCls:'icn-script--plus',
							handler: function(){
								new Clara.TemplateSaveWindow({templateStore:Clara.BudgetBuilder.BudgetTemplateStore, xml:budget.toXML()}).show();
							}
						},{
							text:'Compare to older version..',
							//disabled:!Clara.BudgetBuilder.canEdit(),
							iconCls:'icn-edit-diff',
							handler: function(){
								new Clara.BudgetBuilder.VersionsWindow().show();
							}
						}]
						},{
							iconCls:'icn-question-white',
							handler:function(){
								OpenHelpPage();
							}
						}]
	});
	
	if (supports_html5_storage()){
		Ext.getCmp('btnTools').menu.addItem({
							text:'Local Save History',
							disabled:!Clara.BudgetBuilder.canEdit(),
							iconCls:'icn-arrow-circle',
							handler: function(){
								var w = new Clara.BudgetBuilder.HistoryWindow();
								w.show();
							}
						});
	}
	
	Clara.BudgetBuilder.BudgetViewport = Ext.extend(Ext.Viewport, {
		id:'budget-viewport',
		constructor:function(config){		
			Clara.BudgetBuilder.BudgetViewport.superclass.constructor.call(this, config);
		},
		initComponent:function(){
			var config = {
				layout:'border',
				items: [	{
					    region: 'north',
					    id:'budget-builder-headerpanel',
					    contentEl:'clara-header',
					    bodyStyle:{ backgroundColor:'transparent' },
					    height:70,
					    border: false,
					    margins: '0 0 0 0',
					    bbar: vpBottomBar
					}, 
					{
						region:'center',
						border: true,
						margins:'1 1 1 1',
				        cmargins:'1 1 1 1',
				        xtype:'clarabudgettabpanel',
				        budget:budget
				        
						}
				]
			};
			// apply config
			Ext.apply(this, Ext.apply(this.initialConfig, config));
			 
			jQuery(".clara-header-links").css({"padding-top":"4px"});
			jQuery(".clara-header-links a").addClass("button medium red");
			
			// call parent
			Clara.BudgetBuilder.BudgetViewport.superclass.initComponent.apply(this, arguments);
		}

	});
		
	var vp = new Clara.BudgetBuilder.BudgetViewport();	
		
	
	
}

function supports_html5_storage() {
	if (/MSIE (\d+\.\d+);/.test(navigator.userAgent)) return false;
	  try {
	    return 'localStorage' in window && window['localStorage'] !== null;
	  } catch (e) {
	    return false;
	  }
	}
