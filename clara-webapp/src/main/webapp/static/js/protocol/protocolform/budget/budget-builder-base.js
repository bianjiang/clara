Ext.ns('Clara.BudgetBuilder');

Clara.BudgetBuilder.MessageBus = new Ext.util.Observable();

Clara.BudgetBuilder.MessageBus.addEvents('beforetabchange','aftertabchange','beforebudgetload','budgetloaded','budgetversionloaded','budgetinfoupdated','onbudgetloaderror','beforebudgetsave','afterbudgetsave','afterhistoryupdated','onbudgetsaveerror',
										 'expenseadded','expenseselected','expenseremoved','expenseupdated','epochadded','epochchanged','epochupdated','epochcontentupdated',
										 'armselected','armadded','armremoved','armupdated','cycleselected','cycleadded','cycleremoved','cycleupdated','cyclevisitsremoved',
										 'visitselected','visitadded','visitremoved','visitupdated','epochadded','historyreverted','faupdated','budgettypechanged',
										 'beforebudgetexport','afterbudgetexport','onbudgetexporterror','procedureadded','procedureremoved','procedurechanged',
										 'beforeloadexternalexpenses','afterloadexternalexpenses','onloadexternalexpenseserror','permissionsupdated','armcyclesremoved');

Clara.BudgetBuilder.SaveAction = "";
Clara.BudgetBuilder.TotalBy = "price";

Clara.BudgetBuilder.RequiredRoles = {
		LockBudget:["LOCK_BUDGET"],
		EditLockedBudget:["EDIT_LOCKED_BUDGET"],
		EditBudget:["EDIT_BUDGET"],
		ViewPrices:["EDIT_BUDGET"]
};

var canEditBudget = null;

Clara.BudgetBuilder.canEdit = function(){
	canEditBudget = (canEditBudget == null)?((budget.locked && claraInstance.HasAnyPermissions(Clara.BudgetBuilder.RequiredRoles.EditLockedBudget)) || (!budget.locked && claraInstance.HasAnyPermissions(Clara.BudgetBuilder.RequiredRoles.EditBudget))):canEditBudget;
	return canEditBudget;
};

Clara.BudgetBuilder.canLock = function(){
	return claraInstance.HasAnyPermissions(Clara.BudgetBuilder.RequiredRoles.LockBudget);
};

Clara.BudgetBuilder.InvalidPhaseCharacters = "/\*'?[]:";

Clara.BudgetBuilder.validatePhaseName = function(v){
	var invalidFound = false;
	for (var i=0, l=v.length;i<l;i++){
		if (Clara.BudgetBuilder.InvalidPhaseCharacters.indexOf(v[i]) > -1) invalidFound = true;
	}
	return !invalidFound;
};

Clara.BudgetBuilder.Budget = function(o){
		this.idGenerator=			(o.idGenerator || 1);										// Auto-generated ID's, used universally for all elements
		this.stamp=					(o.stamp || 0);												// ID / GUID assigned to budget when saved
		this.epochs=				(o.epochs || []);											// Epochs for this study
		this.expenses=				(o.expenses || []);
		this.FA=					(o.FA || 0);
		this.locked=				(o.locked || false);
		this.initialExpenseFA = 	(o.initialExpenseFA || 0);
		this.initialInvoicableFA = 	(o.initialInvoicableFA || 0);
		this.budgetType = 			(o.budgetType || "standard");								// standard (all options), simple (hide arm/epoch/cycle)

		this.newId= function(){
			this.idGenerator++;
			return this.idGenerator;
		};
		
		this.validate= function(){
			var err = [];
			for (var i=0;i<this.epochs.length;i++){
				err = this.epochs[i].validate();
			}
		};
		
		this.isNew= function(){
			return (this.stamp < 1 && this.expenses.length < 1 && this.epochs.length < 1);
		};
		
		this.isSimple= function(){
			return (this.budgetType === "simple" || this.budgetType === "basic" );
		};
		
		// EXPENSE FUNCTIONS
		this.getInitialInvoicableFA= function(){
			return this.initialInvoicableFA;
		};

		
		this.setExpenseFA= function(expenseType, fa){
			
			if (expenseType == 'Invoicable') this.initialInvoicableFA = fa;
			else this.initialExpenseFA = fa;
			
			for (var i=0; i<this.expenses.length;i++){
				if (this.expenses[i].type == expenseType && this.expenses[i].subtype != 'Other' && this.expenses[i].external != true && this.expenses[i].faenabled != false){
					this.expenses[i].fa = fa;
				}
			}
		};
		
		this.sortAllArms= function(){
			for (var i=0, l=this.epochs.length;i<l;i++){
				this.epochs[i].sortArms();
			}
		};
		
		this.getExpenseArray= function(etype, groupSubTypes){
			var a = [];
			var eout = [];
			etype = etype || '';
			var prevSubType = '';
			var subTypeAmount=0;
			var subTypeTotal=0;

			var filteredExpenses = jQuery.map(this.expenses, function(e){
				return (etype == '' || e.type == etype)?e:null;
			});
			
			
			if (groupSubTypes === true){
				filteredExpenses.sort(function(a, b){
					 var nameA, nameB;
					 if (a.subtype === "Other") nameA = "zzzzzzzzz"+a.subtype.toLowerCase()+a.description.toLowerCase();
					 	else nameA = a.subtype.toLowerCase()+a.description.toLowerCase();
					 if (b.subtype === "Other") nameB = "zzzzzzzzz"+b.subtype.toLowerCase()+b.description.toLowerCase();
					 	else nameB = b.subtype.toLowerCase()+b.description.toLowerCase();

					 if (nameA < nameB) //sort string ascending
					  return -1; 
					 if (nameA > nameB)
					  return 1;
					 return 0; //default return value (no sorting)
				});
				
				for (var j=0;j<filteredExpenses.length;j++){
					if (prevSubType == filteredExpenses[j].subtype){
						clog("existing.. adding to "+prevSubType);
						subTypeAmount += parseFloat(filteredExpenses[j].cost);
					} else {
						if (prevSubType != ''){
							var e = filteredExpenses[j-1];
							e.cost = subTypeAmount;
							eout.push(e);
						}
						subTypeAmount = parseFloat(filteredExpenses[j].cost);
						prevSubType = filteredExpenses[j].subtype;
					}
				}
				//get the last one
				var e = filteredExpenses[filteredExpenses.length-1];
				e.cost = subTypeAmount;
				eout.push(e);
				
			} else {
				eout = filteredExpenses;
			}
			
			clog("eout",eout);
			
			for (var i=0; i<eout.length;i++){

					a.push([eout[i].id, 
					        eout[i].diff,
					        eout[i].type, 
					        eout[i].subtype, 
					        eout[i].description, 
					        eout[i].notes, 
					        eout[i].fa, 
					        eout[i].faenabled, 
					        eout[i].external, 
					        eout[i].count, 
					        eout[i].cost]);
					
			}
					cdebug("ExpenseArray",a);
					
					a.sort(function(aa, b){
						 var nameA, nameB;
						 
						 if (aa[3] == "Other") nameA = "zzzzzzzzz"+aa[3].toLowerCase()+aa[4].toLowerCase();
						 else nameA = aa[3].toLowerCase();
						 if (b[3] == "Other") nameB = "zzzzzzzzz"+b[3].toLowerCase()+b[4].toLowerCase();
						 else nameB = b[3].toLowerCase();
				
						 if (nameA < nameB) //sort string ascending
						  return -1; 
						 if (nameA > nameB)
						  return 1;
						 return 0; //default return value (no sorting)
					});
					
					cdebug("ExpenseArray",a);
					
			return a;
		};
		
		this.updateFA= function(fa){
			if (typeof fa == 'undefined' || fa == ''){
				fa = 0;
			}
			this.FA = fa;
			Clara.BudgetBuilder.MessageBus.fireEvent('faupdated', fa);
		};
		
		this.addExpense= function(e){
			if (typeof e.id == 'undefined' || e.id == ''){
				e.id = this.newId();
			}
			this.expenses.push(e);
			Clara.BudgetBuilder.MessageBus.fireEvent('expenseadded', e);
		};
		
		this.updateExpense= function(a){
			var i = this.expenses.length;
			while (i--){

						if (this.expenses[i].id == a.id){
							this.expenses[i].description = a.description;
							this.expenses[i].external = a.external;
							this.expenses[i].cost = a.cost;
							this.expenses[i].fa = a.fa;
							this.expenses[i].faenabled = a.faenabled;
							this.expenses[i].count = a.count;
							this.expenses[i].notes = a.notes;
							Clara.BudgetBuilder.MessageBus.fireEvent('expenseupdated', a);
							return true;
						}

			}
			return false;
		};
		
		this.removeExpense= function(e){
			var i = this.expenses.length;
			while (i--){
						if (this.expenses[i].id == e.id){
							this.expenses.splice(i,1);
							Clara.BudgetBuilder.MessageBus.fireEvent('expenseremoved', e);
							return true;
						}
			}
			return false;
		};
				
		this.getExpense= function(id){
			var i = this.expenses.length;
			while (i--){
				if (this.expenses[i].id == id){
					return this.expenses[i];
				}
			}
			return null;
		};
		
		// END EXPENSE FUNCITONS
		
		
		// EPOCH FUNCTIONS
		
		this.addEpoch= function(e){
			if (typeof e.id == 'undefined' || e.id == ''){
				e.id = this.newId();
			}
			e.index = e.id;
			this.epochs.push(e);
			Clara.BudgetBuilder.MessageBus.fireEvent('epochadded', e);
		};
		
		this.removeEpoch= function(e){
			var i = this.epochs.length;
			while (i--){
						if (this.epochs[i].id == e.id){
							this.epochs.splice(i,1);
							Clara.BudgetBuilder.MessageBus.fireEvent('epochremoved', e);
							return true;
						}
			}
			return false;
		};
			
		this.getEpochByName=function(name){
			var i = this.epochs.length;
			while (i--){
				if (this.epochs[i].name.toLowerCase() == name.toLowerCase()){
					return this.epochs[i];
				}
			}
			return null;
		};
		
		this.getEpoch= function(id){
			var i = this.epochs.length;
			while (i--){
				if (this.epochs[i].id == id){
					return this.epochs[i];
				}
			}
			return null;
		};
		
		this.setEpochSubjectCount= function(epoch,count){
			var e = this.getEpoch(epoch.id);
			var j = e.arms.length;
			var k = 0;
			var l = 0;
			while (j--){
				k = e.arms[j].cycles.length;
				while(k--){
					l = e.arms[j].cycles[k].visits.length;
					while(l--){
						e.arms[j].cycles[k].visits[l].subjectcount = count;
					}
				}
			}
			Clara.BudgetBuilder.MessageBus.fireEvent('epochcontentupdated', e);
		};
		
		// END EPOCH FUNCITONS
		
		// ARM FUNCTIONS
		
		this.addArm=	function(epoch, arm){
			if (typeof arm.id == 'undefined' || arm.id == ''){
				arm.id = this.newId();
			}
			epoch.arms.push(arm);
			epoch.sortArms();
			Clara.BudgetBuilder.MessageBus.fireEvent('armadded', arm);
			
		};
		
		this.updateArm= function(a){
			var i = this.epochs.length;
			var j = 0;
			while(i--){
				j = this.epochs[i].arms.length;
				while(j--){
						if (this.epochs[i].arms[j].id == a.id){
							this.epochs[i].arms[j].index = a.index;
							this.epochs[i].arms[j].name = a.name;	
							this.epochs[i].arms[j].notes = a.notes;
							Clara.BudgetBuilder.MessageBus.fireEvent('armupdated', a);
							return true;
						}
				}
			}
			return false;
		};
		
		this.removeArm= function(a){
			var i = this.epochs.length;
			var j = 0;
			while(i--){
				j = this.epochs[i].arms.length;
				while(j--){
						if (this.epochs[i].arms[j].id == a.id){
							this.epochs[i].arms.splice(j,1);
							Clara.BudgetBuilder.MessageBus.fireEvent('armremoved', a);
							return true;
						}

				}
			}
			return false;
		};
		
		this.getArm= function(aid){
			var i = this.epochs.length;
			var j = 0;
			while(i--){
				j = this.epochs[i].arms.length;
				while(j--){
						if (this.epochs[i].arms[j].id == aid){
							return this.epochs[i].arms[j];
						}
				}
			}
			return null;
		};
		
		this.getAllArms= function(){
			var arms = [];
			var i = this.epochs.length;
			while(i--){
				arms = arms.concat(this.epochs[i].arms);
			}
			return arms;
		};
		
		this.getArmArrayForEpoch= function(e){
			var i = this.epochs.length;
			while(i--){
						if (this.epochs[i].id == e.id){
							return this.epochs[i].armArray();
						}

			}
			return null;
		};
		
		// end ARM FUNCTIONS

		// CYCLE FUNCTIONS

		this.getCycleArrayForArm= function(a){
			var i = this.epochs.length;
			var j = 0;
			while(i--){
				j = this.epochs[i].arms.length;
				while(j--){

						if (this.epochs[i].arms[j].id == a.id){
							return this.epochs[i].arms[j].cycleArray();
						}

				}
			}
			return null;
		};
		
		this.getCycle= function(cid){
			var i = this.epochs.length;
			var j = 0;
			var k = 0;
			while(i--){
				j = this.epochs[i].arms.length;
				while(j--){
					k = this.epochs[i].arms[j].cycles.length;
					while(k--){
						if (this.epochs[i].arms[j].cycles[k].id == cid){
							return this.epochs[i].arms[j].cycles[k];
						}
					}
				}
			}
			return null;
		};
		
		this.addCycle= function(arm, c, supressEvent){
			supressEvent = supressEvent || false;
			clog("adding cycle",c);
			if (typeof c.id == 'undefined' || c.id == ''){
				c.id = this.newId();
			}
			arm.cycles.push(c);
			if (!supressEvent) Clara.BudgetBuilder.MessageBus.fireEvent('cycleadded', c);
			return c;
		};
		
		this.removeCycle= function(c){
			var i = this.epochs.length;
			var j = 0;
			var k = 0;
			while(i--){
				j = this.epochs[i].arms.length;
				while(j--){
					k = this.epochs[i].arms[j].cycles.length;
					while(k--){
						if (this.epochs[i].arms[j].cycles[k].id == c.id){
							this.epochs[i].arms[j].cycles.splice(k,1);
							//this.epochs[i].arms[j].sortCycles();
							Clara.BudgetBuilder.MessageBus.fireEvent('cycleremoved', c);
							return true;
						}
					}
				}
			}
			return false;
		};
		
		this.updateCycle= function(c){
			var i = this.epochs.length;
			var j = 0;
			var k = 0;
			while(i--){
				j = this.epochs[i].arms.length;
				while(j--){
					k = this.epochs[i].arms[j].cycles.length;
					while(k--){
						if (this.epochs[i].arms[j].cycles[k].id == c.id){
							this.epochs[i].arms[j].cycles[k].index = parseInt(c.index);
							this.epochs[i].arms[j].cycles[k].simple = c.simple;
							this.epochs[i].arms[j].cycles[k].name = c.name;
							this.epochs[i].arms[j].cycles[k].notes = c.notes;
							this.epochs[i].arms[j].cycles[k].startday = c.startday;
							this.epochs[i].arms[j].cycles[k].endday = c.endday;
							this.epochs[i].arms[j].cycles[k].repetitions = c.repetitions;
							this.epochs[i].arms[j].cycles[k].repeatforever = c.repeatforever;
							this.epochs[i].arms[j].cycles[k].duration = c.duration;
							this.epochs[i].arms[j].cycles[k].durationunit = c.durationunit;
							//this.epochs[i].arms[j].sortCycles();
							Clara.BudgetBuilder.MessageBus.fireEvent('cycleupdated', c);
							return true;
						}
					}
				}
			}
			return false;
		};

		// end CYCLE FUNCTIONS
		
		// VISIT FUNCTIONS

		 
		this.getVisitProcedure= function(vid,procid){
			var i = this.epochs.length;
			var j = 0;
			var k = 0;
			var l = 0;
			while(i--){
				j = this.epochs[i].arms.length;
				while(j--){
					k = this.epochs[i].arms[j].cycles.length;
					while(k--){
						l = this.epochs[i].arms[j].cycles[k].visits.length;
						while(l--){
							if (this.epochs[i].arms[j].cycles[k].visits[l].id == vid){
								var v= this.epochs[i].arms[j].cycles[k].visits[l];
								//check existing visitprocedures.
								for (var m=0;m<v.visitprocedures.length;m++){
									if (v.visitprocedures[m].procedureid == procid){
										//get
										return v.visitprocedures[m];
									}
								}
							}
						}
					}
				}
			}
			return null;
		};
		
		this.removeVisitProcedure= function(vid,procid){
			var i = this.epochs.length;
			var j = 0,k=0,l=0,m=0;
			while(i--){
				j = this.epochs[i].arms.length;
				while(j--){
					k = this.epochs[i].arms[j].cycles.length;
					while(k--){
						l = this.epochs[i].arms[j].cycles[k].visits.length;
						while(l--){
							if (vid==0 || this.epochs[i].arms[j].cycles[k].visits[l].id == vid){
								var v= this.epochs[i].arms[j].cycles[k].visits[l];
								m = v.visitprocedures.length;
								while(m--){
									if (v.visitprocedures[m].procedureid == procid){
										v.visitprocedures.splice(m,1);
										
										// Also remove subprocedure visitprocedures
										////cdebug("finding proc: "+procid);
										var proc = this.epochs[i].getProcedureById(procid);
										if (proc){
											for (var sp=0;sp < proc.subprocedures.length;sp++){
												this.removeVisitProcedure(vid, proc.subprocedures[sp].id);
											}
										}
										
										return;
									}
								}
							}
						}
					}
				}
			}
			return;
		};
		
		this.removeVisitProceduresByEpochAndProcedure= function(epoch, procedure) {
			////cdebug("clean vps in budget before save...");
			////cdebug(epoch);
			////cdebug(procedure);
			if(epoch && procedure && procedure.id) {
				var j = 0,k=0,l=0,m=0;
					var i = epoch.arms.length;
					while(i--){
						j = epoch.arms[i].cycles.length;
						while(j--){
							k = epoch.arms[i].cycles[j].visits.length;
							while(k--){
							var v= epoch.arms[i].cycles[j].visits[k];
							l = v.visitprocedures.length;
							while(l--){
								if (v.visitprocedures[l].procedureid == procedure.id){
									clog("removing vp with procid "+procedure.id,v.visitprocedures[l]);
									v.visitprocedures.splice(l,1);
									l = v.visitprocedures.length;	// to reset length var after deleted element
								}
							}
						}
					}
				}
			}
		};
		
		this.addOrUpdateVisitProcedure= function(vp){
			var i = this.epochs.length;
			var j = 0,k=0,l=0,m=0;
			while(i--){
				j = this.epochs[i].arms.length;
				while(j--){
					k = this.epochs[i].arms[j].cycles.length;
					while(k--){
						l = this.epochs[i].arms[j].cycles[k].visits.length;
						while(l--){
							
							if (this.epochs[i].arms[j].cycles[k].visits[l].id == vp.visitid){
								var v= this.epochs[i].arms[j].cycles[k].visits[l];
								m=v.visitprocedures.length;
								while(m--){
									if (v.visitprocedures[m].procedureid == vp.procedureid){
																	
										v.visitprocedures[m].repetitions = vp.repetitions;
										v.visitprocedures[m].type = vp.type;
										
										return;
									}
								}
								//if no update, then add
								v.visitprocedures.push(vp);
								return;
							}
						}
					}
				}
			}
		};
		
		this.getVisitArrayForCycle= function(c){
			var i = this.epochs.length;
			var j = 0,k=0,l=0,m=0;
			while(i--){
				j = this.epochs[i].arms.length;
				while(j--){
					k = this.epochs[i].arms[j].cycles.length;
					while(k--){
						if (this.epochs[i].arms[j].cycles[k].id == c.id){
							return this.epochs[i].arms[j].cycles[k].visitArray();
						}
					}
				}
			}
			return null;
		};
		
		this.getVisit= function(vid){
			var i = this.epochs.length;
			var j = 0,k=0,l=0,m=0;
			while(i--){
				j = this.epochs[i].arms.length;
				while(j--){
					k = this.epochs[i].arms[j].cycles.length;
					while(k--){
						l = this.epochs[i].arms[j].cycles[k].visits.length;
						while(l--){
							if (this.epochs[i].arms[j].cycles[k].visits[l].id == vid){
								return this.epochs[i].arms[j].cycles[k].visits[l];
							}
						}
					}
				}
			}
			return null;
		};
		
		this.addVisit= function(cycle, v, supressevent){
			if (cycle) clog("budget.addVisit(): Old cycle duration: ",cycle.duration);
			if (typeof supressevent == 'undefined' || supressevent == null) supressevent = false;
			if (typeof v.id == 'undefined' || v.id == ''){
				v.id = this.newId();
			}
			if (cycle) {
				v.parentCycle = cycle;
				v.parentEpoch = cycle.getParentEpoch();
			}
			cycle.visits.push(v);
			if (cycle.simple == true) cycle.recalculateDateRanges();	// to correct start/end/duration attributes for simple cycle
			clog("budget.addVisit(): New cycle duration: ",cycle.duration);
			if (!supressevent) Clara.BudgetBuilder.MessageBus.fireEvent('visitadded', v);
		};
		
	
		this.removeVisit= function(v){
			var i = this.epochs.length;
			var j = 0,k=0,l=0,m=0;
			while(i--){
				j = this.epochs[i].arms.length;
				while(j--){
					k = this.epochs[i].arms[j].cycles.length;
					while(k--){
						l = this.epochs[i].arms[j].cycles[k].visits.length;
						while(l--){
							if (this.epochs[i].arms[j].cycles[k].visits[l].id == v.id){
								this.epochs[i].arms[j].cycles[k].visits.splice(l,1);
								this.epochs[i].arms[j].cycles[k].recalculateDateRanges();
								Clara.BudgetBuilder.MessageBus.fireEvent('visitremoved', v);
								return true;
							}
						}
					}
				}
			}
			return false;
		};
		
		this.updateVisit= function(v){
			var i = this.epochs.length;
			var j = 0,k=0,l=0,m=0;
			while(i--){
				j = this.epochs[i].arms.length;
				while(j--){
					k = this.epochs[i].arms[j].cycles.length;
					while(k--){
						l = this.epochs[i].arms[j].cycles[k].visits.length;
						while(l--){
							if (this.epochs[i].arms[j].cycles[k].visits[l].id == v.id){
								clog("FOUND VISIT. NEW CYCLEINDEX",v.cycleindex);
								this.epochs[i].arms[j].cycles[k].visits[l].cycleindex = v.cycleindex;
								this.epochs[i].arms[j].cycles[k].visits[l].name = v.name;
								this.epochs[i].arms[j].cycles[k].visits[l].unit = v.unit;
								this.epochs[i].arms[j].cycles[k].visits[l].unitvalue = v.unitvalue;
								this.epochs[i].arms[j].cycles[k].visits[l].subjectcount = v.subjectcount;
								this.epochs[i].arms[j].cycles[k].visits[l].notes = v.notes;
								this.epochs[i].arms[j].cycles[k].visits[l].parentCycle = this.epochs[i].arms[j].cycles[k];
								this.epochs[i].arms[j].cycles[k].recalculateDateRanges();
								Clara.BudgetBuilder.MessageBus.fireEvent('visitupdated', v);
								return true;
							}
						}
					}
				}
			}
			return false;
		};
		
		// end VISIT FUNCTIONS

		
		this.load= function(version){
			var t = this;
			var url = appContext+"/ajax/protocols/"+claraInstance.id+"/protocol-forms/"+claraInstance.form.id+"/budgets/get";
			if (version) {
				url = appContext+"/ajax/protocols/"+claraInstance.id+"/protocol-forms/"+claraInstance.form.id+"/budgets/compare-to/"+version;
			}
			Clara.BudgetBuilder.MessageBus.fireEvent('beforebudgetload', this);
			var b = this;
			jQuery.ajax({
				  type: 'GET',
				  async:false,
				  cache:false,
				  url: url,
				  success: function(data){
					  b.fromXML(data);
					  //cdebug("Budget loaded. Checking/pruning invalid data..");
					  b.cleanUp();
					  Clara.BudgetBuilder.MessageBus.fireEvent('budgetloaded', t);
					  if (version) Clara.BudgetBuilder.MessageBus.fireEvent('budgetversionloaded', t);
				  },
				  error: function(){
					  Clara.BudgetBuilder.MessageBus.fireEvent('onbudgetloaderror', t);  
				  },
				  dataType: 'xml'
			});

		};
		
		this.cleanUp= function(){
			var errorsFound = [];
			var t = this;
			for (var k=0;k<t.epochs.length;k++){
				
				
				var e = t.epochs[k];
				//cdebug("checking epoch",e);
				e.arms = jQuery.map(e.arms, function(arm,i){
					if (arm.cycles.length < 1){
						cwarn("budget.cleanUp: Found empty arm (ID "+arm.id+"), removing.");
						errorsFound.push("Found an empty arm.");
						return null;
					} else {
						if (arm.id == ""){
							cwarn("budget.cleanUp: Found arm with blank ID, generating a new one.");
							arm.id = t.newId();
							errorsFound.push("Found a arm with no ID.");
						}
						arm.cycles = jQuery.map(arm.cycles,function(cycle,j){
							//cdebug("checking cycle ",cycle);
							if (cycle.visits.length < 1){
								cwarn("budget.cleanUp: Found empty cycle (ID "+cycle.id+"), removing.");
								errorsFound.push("Found an empty cycle.");
								return null;
							} else {
								if (cycle.id == ""){
									cwarn("budget.cleanUp: Found cycle with blank ID, generating a new one.");
									cycle.id = t.newId();
									errorsFound.push("Found a cycle with no ID.");
								}
								cycle.visits = jQuery.map(cycle.visits,function(visit,l){
									//cdebug("checking visit ",visit);
									visit.visitprocedures = jQuery.map(visit.visitprocedures,function(vp,m){
										//cdebug("checking vp ",vp);
										var found = false;
										for (var f=0;f<e.procedures.length;f++){
											if (e.procedures[f].id == vp.procedureid){
												found = true;
											}
											if (found === false){
												for (var sf=0;sf<e.procedures[f].subprocedures.length;sf++){
													if (e.procedures[f].subprocedures[sf].id == vp.procedureid) {
														found = true;
													}
												}
											}
										}
										if (found === false){
											cwarn("budget.cleanUp: Found orphan visitprocedure, removing.", vp);
											errorsFound.push("Found a visitless procedure.");
											return null;
										} else {
											
											return vp;
										}
									});
									return visit;
								});
								return cycle;
							}
						});
						return (arm.cycles.length > 0)?arm:null;
					}
				});
			}
			//cdebug("budget.cleanUp end");
			if (errorsFound.length > 0){
				
				var errors = "<ul><li>* "+errorsFound.join("</li><li>* ")+"</li></ul>";
				
				if (Clara.BudgetBuilder.canEdit()){
					Ext.Msg.show({
						   title:'Issues found',
						   msg: '<strong>We found some issues with this budget and have been fixed:</strong><br/><br/> '+errors+' <br/><strong>The budget will now save.</strong>',
						   buttons: Ext.Msg.OK,
						   fn: function(btn){
							   t.save();
						   },
						   icon: Ext.MessageBox.INFO
						});
				} else {
					Ext.Msg.show({
						   title:'Issues found',
						   msg: '<strong>We found some issues with this budget, but the fixes will not be saved (you do not have the necessary permissions to edit the burdget at this time):</strong><br/><br/>'+errors,
						   buttons: Ext.Msg.OK,
						   fn: function(btn){
							   t.save();
						   },
						   icon: Ext.MessageBox.INFO
						});
				}
			}
		};
		
		this.saveExcelDocument = function(options){
			options = options || {};
			
			Clara.BudgetBuilder.MessageBus.fireEvent('beforebudgetexport', this);
			var url = appContext+"/ajax/protocols/"+claraInstance.id+"/protocol-forms/"+claraInstance.form.id+"/budgets/export-documents";

			jQuery.ajax({
				  type: 'POST',
				  async:false,
				  url: url,
				  data: {committee:claraInstance.user.committee},
				  success: function(){
					  Clara.BudgetBuilder.MessageBus.fireEvent('afterbudgetexport', this);  
				  },
				  error: function(){
					  Clara.BudgetBuilder.MessageBus.fireEvent('onbudgetexporterror', this);  
				  },
				  dataType: 'xml'
			});
		};
		
		
		this.save= function(xmlstring,reload){
			var me = this;
			if (Clara.BudgetBuilder.canEdit()){
				reload = reload || false;
				Clara.BudgetBuilder.MessageBus.fireEvent('beforebudgetsave', this);
				var url = appContext+"/ajax/protocols/"+claraInstance.id+"/protocol-forms/"+claraInstance.form.id+"/budgets/save";
				me.stamp = parseInt(me.stamp)+1;
				var xmldata = (xmlstring)?xmlstring:me.toXML();
				
				jQuery.ajax({
					  type: 'POST',
					  async:false,
					  cache:false,
					  url: url,
					  success: function(data){
						  if (reload == true) location.reload(true);
					      else Clara.BudgetBuilder.MessageBus.fireEvent('afterbudgetsave', this); 
					  },
					  error: function(){
						  Clara.BudgetBuilder.MessageBus.fireEvent('onbudgetsaveerror', this);   
					  },
					  data:{xmlData:xmldata, reason:(Clara.BudgetBuilder.SaveAction || '')}
				});
				
			} else {
				cwarn("budget.save(): User can't edit, save aborted.");
			}
		};
		
		this.toXML= function(){
			var me = this;
			
			//cdebug("budget.toXML start");
			var xml = "<budget stamp='"+(me.stamp)+"' type='"+me.budgetType+"' fa='"+me.FA+"' locked='"+me.locked+"' faInitial='"+me.initialExpenseFA+"' faInvoicable='"+me.initialInvoicableFA+"'>";
			xml = xml + "<expenses>";
			var i = me.expenses.length;
			while(i--){
				xml = xml + me.expenses[i].toXML();
			}
			xml = xml + "</expenses>";
			xml = xml + "<epochs>";
			i=me.epochs.length;
			while(i--){
				xml = xml + me.epochs[i].toXML();
			}
			xml = xml + "</epochs>";
			xml = xml + "</budget>";
			//cdebug("budget.toXML returning");
			return xml;
		};
		
		this.fromXML= function(xml){
			clog("Loading budget from XML",xml);
			var maxid = 0;
			var fa = this.FA;
			var iefa = this.initialExpenseFA;
			var iifa = this.initialInvoicableFA;
			var b = this;
			jQuery(xml).find("budget:first").each(function(){	
				b.stamp = parseFloat(jQuery(this).attr('stamp')) || 0;
				b.budgetType = jQuery(this).attr('type');
				b.locked=	(jQuery(this).attr('locked') == 'true')?true:false;
				fa = parseFloat(jQuery(this).attr('fa') || 0);
				iefa = parseFloat(jQuery(this).attr('faInitial'));
				iifa = parseFloat(jQuery(this).attr('faInvoicable'));
			});
			this.FA = fa;
			this.initialInvoicableFA = iifa;
			this.initialExpenseFA = iefa;

			var exps = [];		// expenses
			jQuery(xml).find("expenses").find("expense").each(function(){
				var exp = new Clara.BudgetBuilder.Expense({
					id:			parseFloat(jQuery(this).attr('id')),
					diff:		jQuery(this).attr('diff'),
					fa:			parseFloat(jQuery(this).attr('fa')),
					external:	(jQuery(this).attr('external') == 'true')?true:false,
					faenabled:	(jQuery(this).attr('faenabled') == 'true')?true:false,
					count:		parseFloat(jQuery(this).attr('count')),
					cost:		parseFloat(jQuery(this).attr('cost')),
					type:		Encoder.htmlDecode(jQuery(this).attr('type')),
					subtype:	Encoder.htmlDecode(jQuery(this).attr('subtype')),
					description:Encoder.htmlDecode(jQuery(this).attr('description')),
					notes:		Encoder.htmlDecode(jQuery(this).attr('notes'))
				});
				maxid = (maxid < exp.id)?exp.id:maxid;
				exps.push(exp);
			});
			
			var eps = [];		// epochs
			jQuery(xml).find("epochs").find("epoch").each(function(){
				var ep = new Clara.BudgetBuilder.Epoch({
					id:			parseFloat(jQuery(this).attr('id')),
					diff:		jQuery(this).attr('diff'),
					conditional:(jQuery(this).attr('conditional') == 'true')?true:false,
					simple:(jQuery(this).attr('simple') == 'true')?true:false,
					index:		parseFloat(jQuery(this).attr('index')),
					name:		Encoder.htmlDecode(jQuery(this).attr('name')),
					notes:		Encoder.htmlDecode(jQuery(this).find('notes:first').text())
				});
				
				maxid = (maxid < ep.id)?ep.id:maxid;
				
				// Get procedures for epoch
				var procs = [];
				jQuery(this).find('procedures').children().each(function(){
					
					var proc = new Clara.BudgetBuilder.Procedure({
						id:					parseFloat(jQuery(this).attr('id')),
						isSubprocedure:		false,
						diff:				jQuery(this).attr('diff'),
						type:				Encoder.htmlDecode(jQuery(this).attr('type')),
						description:		Encoder.htmlDecode(jQuery(this).attr('description')),
						expensecategory:	Encoder.htmlDecode(jQuery(this).attr('expensecategory')),
						category:			Encoder.htmlDecode(jQuery(this).attr('category')),
						conditional:		(jQuery(this).attr('conditional') == 'true')?true:false,
						alternative:		(jQuery(this).attr('alternative') == 'true')?true:false,
						cptCode:			jQuery(this).attr('cptcode'),
						cdmCode:			parseFloat(jQuery(this).attr('cdmcode')),
						location:			Encoder.htmlDecode(jQuery(this).attr('location')),
						phys: {
							id:				Encoder.htmlDecode(jQuery(this).find('phys').attr('id')),
							only:			(jQuery(this).find('phys').attr('only') == 'true')?true:false,
							cost:			parseFloat(jQuery(this).find('phys').attr('cost')),
							locationCode:	jQuery(this).find('phys').attr('locationcode'),
							locationDesc:	Encoder.htmlDecode(jQuery(this).find('phys').attr('locationdesc'))
						},
						hosp: {
							id:				Encoder.htmlDecode(jQuery(this).find('hosp').attr('id')),
							description:	Encoder.htmlDecode(jQuery(this).find('hosp').attr('description')),
							only:			(jQuery(this).find('hosp').attr('only') == 'true')?true:false,
							cost:			parseFloat(jQuery(this).find('hosp').attr('cost')),
							locationCode:	jQuery(this).find('hosp').attr('locationcode'),
							locationDesc:	Encoder.htmlDecode(jQuery(this).find('hosp').attr('locationdesc'))
						},
						notes:				Encoder.htmlDecode(jQuery(this).find('notes:first').text()),
						clinicalNotes:		Encoder.htmlDecode(jQuery(this).find('clinical-notes:first').text()),
						cost:				{
							misc:			(""+jQuery(this).find('cost:first').find('misc').text()).toString(),
							sponsor:		(""+jQuery(this).find('cost:first').find('sponsor').text()).toString(),
							price:			(""+jQuery(this).find('cost:first').find('price').text()).toString()
						}
					});
					
					// Get codes for procedure
					var codes = [];
					jQuery(this).find('codes:first').find('code').each(function(){
						var c = new Clara.BudgetBuilder.ProcedureCode({
							id:					parseFloat(jQuery(this).attr('id')),
							diff:		jQuery(this).attr('diff'),
							type:				jQuery(this).attr('type'),
							description:		Encoder.htmlDecode(jQuery(this).attr('description'))
						});
						
						maxid = (maxid < c.id)?c.id:maxid;
						
						codes.push(c);
					});
					proc.codes = codes;
					
					maxid = (maxid < proc.id)?proc.id:maxid;
					
					// get subprocs for proc
					var subprocs = [];
					jQuery(this).find('subprocedures').find('procedure').each(function(){
						var subproc = new Clara.BudgetBuilder.Procedure({
							id:					parseFloat(jQuery(this).attr('id')),
							isSubprocedure:		true,
							diff:		jQuery(this).attr('diff'),
							type:				Encoder.htmlDecode(jQuery(this).attr('type')),
							description:		Encoder.htmlDecode(jQuery(this).attr('description')),
							expensecategory:	Encoder.htmlDecode(jQuery(this).attr('expensecategory')),
							category:			Encoder.htmlDecode(jQuery(this).attr('category')),
							cptCode:			jQuery(this).attr('cptcode'),
							cdmCode:			parseFloat(jQuery(this).attr('cdmcode')),
							conditional:		(jQuery(this).attr('conditional') == 'true')?true:false,
							alternative:		(jQuery(this).attr('alternative') == 'true')?true:false,		
							location:			Encoder.htmlDecode(jQuery(this).attr('location')),
							phys: {
								id:				Encoder.htmlDecode(jQuery(this).find('phys').attr('id')),
								only:			(jQuery(this).find('phys').attr('only') == 'true')?true:false,
								cost:			parseFloat(jQuery(this).find('phys').attr('cost')),
								locationCode:	jQuery(this).find('phys').attr('locationcode'),
								locationDesc:	Encoder.htmlDecode(jQuery(this).find('phys').attr('locationdesc'))
							},
							hosp: {
								id:				Encoder.htmlDecode(jQuery(this).find('hosp').attr('id')),
								description:	Encoder.htmlDecode(jQuery(this).find('hosp').attr('description')),
								only:			(jQuery(this).find('hosp').attr('only') == 'true')?true:false,
								cost:			parseFloat(jQuery(this).find('hosp').attr('cost')),
								locationCode:	jQuery(this).find('hosp').attr('locationcode'),
								locationDesc:	Encoder.htmlDecode(jQuery(this).find('hosp').attr('locationdesc'))
							},
							notes:				Encoder.htmlDecode(jQuery(this).find('notes:first').text()),
							clinicalNotes:		Encoder.htmlDecode(jQuery(this).find('clinical-notes:first').text()),
							cost:				{
								misc:			(""+jQuery(this).find('cost').find('misc').text()).toString(),
								sponsor:		(""+jQuery(this).find('cost').find('sponsor').text()).toString(),
								price:			(""+jQuery(this).find('cost').find('price').text()).toString()
							}
						});
						
						// Get codes for subprocedure
						var scodes = [];
						jQuery(this).find('codes:first').find('code').each(function(){
							var sc = new Clara.BudgetBuilder.ProcedureCode({
								id:					parseFloat(jQuery(this).attr('id')),
								diff:				jQuery(this).attr('diff'),
								type:				jQuery(this).attr('type'),
								description:		Encoder.htmlDecode(jQuery(this).attr('description'))
							});
							
							maxid = (maxid < sc.id)?sc.id:maxid;
							
							scodes.push(sc);
						});
						subproc.codes = scodes;
						
						maxid = (maxid < subproc.id)?subproc.id:maxid;
						
						subprocs.push(subproc);
					});
					proc.subprocedures = subprocs;
					
					
					
					
					//soft assign epoch parent object (to calculate totals)
					procs.push(proc);
				});
	
				ep.procedures = procs;
								
				// Get arms for epoch
				var arms = [];
				jQuery(this).find('arms').find('arm').each(function(){
					var arm = new Clara.BudgetBuilder.Arm({
						id:			parseFloat(jQuery(this).attr('id')),
						diff:		jQuery(this).attr('diff'),
						name:		Encoder.htmlDecode(jQuery(this).attr('name')),
						index:		parseFloat(jQuery(this).attr('index')),
						hidden:			(jQuery(this).attr('hidden') == 'true')?true:false,
						notes:		Encoder.htmlDecode(jQuery(this).find('notes:first').text())
					}); 
					
					maxid = (maxid < arm.id)?arm.id:maxid;
					
					// Get cycles for arm
					var cycles = [];
					
					jQuery(this).find('cycles').find('cycle').each(function(){
						var cycle = new Clara.BudgetBuilder.Cycle({
							id:			parseFloat(jQuery(this).attr('id')),
							simple:	    (jQuery(this).attr('simple') == 'true')?true:false,
							diff:		jQuery(this).attr('diff'),
							name:		Encoder.htmlDecode(jQuery(this).attr('name')),
							index:		parseFloat(jQuery(this).attr('index')),
							repetitions:parseFloat(jQuery(this).attr('repetitions')),
							repeatforever:(jQuery(this).attr('repeatforever') == 'true')?true:false,
							duration:parseFloat(jQuery(this).attr('duration')),
							durationunit:Encoder.htmlDecode(jQuery(this).attr('durationunit')),
							startday:	parseFloat(jQuery(this).attr('startday')),
							endday:		parseFloat(jQuery(this).attr('endday')),
							notes:		Encoder.htmlDecode(jQuery(this).find('notes').text())
						}); 
						
						// 9/11/2013: Check for cycles marked "simple" 
						//            that have complex attributes, 
						//		 	  set simple to false if found.
						
						if (cycle.repeatforever || cycle.repetitions > 1 || cycle.durationunit != "Day") cycle.simple = false;
						
						maxid = (maxid < cycle.id)?cycle.id:maxid;
						
						// Get visits for cycle
						var visits = [];
						jQuery(this).find('visits').find('visit').each(function(){
							var visit = new Clara.BudgetBuilder.Visit({
								id:			parseInt(jQuery(this).attr('id'),10),
								diff:		jQuery(this).attr('diff'),
								name:		Encoder.htmlDecode(jQuery(this).attr('name')),
								cycleindex:	parseFloat(jQuery(this).attr('cycleindex')),
								unit:		Encoder.htmlDecode(jQuery(this).attr('unit')),
								unitvalue:	parseFloat(jQuery(this).attr('unitvalue')),
								subjectcount:parseFloat(jQuery(this).attr('subj')),
								notes:		Encoder.htmlDecode(jQuery(this).find('notes').text())
							}); 
							
							maxid = (maxid < visit.id)?visit.id:maxid;
							
							// Get visitprocedures for visit
							var vps = [];
							if (visit.id == null) cwarn("fromXML: visitid for vp is null",visit,cycle);
							jQuery(this).find('visitprocedures').find('vp').each(function(){
								var vp = new Clara.BudgetBuilder.VisitProcedure({
									visitid:			visit.id,
									diff:		jQuery(this).attr('diff'),
									procedureid:		(jQuery(this).attr('procid'))?parseFloat(jQuery(this).attr('procid')):parseFloat(jQuery(this).attr('pid')),
									type:				(jQuery(this).attr('type'))?jQuery(this).attr('type'):jQuery(this).attr('t'),
									repetitions:		(jQuery(this).attr('reps'))?parseFloat(jQuery(this).attr('reps')):parseFloat(jQuery(this).attr('r'))
								});
								vps.push(vp);
							});
							
							visit.visitprocedures = vps;
							
							//soft assign epoch parent object (to calculate totals)
							visit.parentEpoch = ep;
							visit.parentCycle = cycle;
							visit.parentArm = arm;
							
							visits.push(visit);
						});
						visits.sort(function(a,b){
							return a.cycleindex - b.cycleindex;
						});
						cycle.visits = visits;
						cycles.push(cycle);
					});
					
					cycles.sort(function(a,b){
						return a.startday - b.startday;
					});
					
					arm.cycles = cycles;
					
					//soft assign epoch parent object (to calculate totals)
					arm.parentEpoch = ep;
					
					arms.push(arm);
				});
				
				ep.arms = arms;
				eps.push(ep);
			});
			
			eps.sort(function(a,b){
				return a.index - b.index;
			});
			
			this.expenses = exps;
			this.epochs = eps;
			this.idGenerator = maxid+1;
			
		};
};


Clara.BudgetBuilder.Expense = function(o){
	this.id=				(o.id || '');	
	this.diff=				(o.diff || '');
	this.fa=				(o.fa || 0);
	this.faenabled=			(o.faenabled || false);
	this.external=			(o.external || false);
	this.count=				(o.count || 0);
	this.cost=				(o.cost || 0);
	this.type=				(o.type || '');	
	this.subtype=			(o.subtype || '');	
	this.description=		(o.description || '');	
	this.notes=				(o.notes || '');	
	
	// Functions

	this.toXML= function(){
		return "<expense id='"+this.id+"' fa='"+this.fa+"' faenabled='"+this.faenabled+"' external='"+this.external+"' count='"+this.count+"' cost='"+this.cost+"' type='"+this.type+"' subtype='"+this.subtype+"' description='"+Encoder.htmlEncode(this.description)+"' notes='"+Encoder.htmlEncode(this.notes)+"'/>";
	};
};



Clara.BudgetBuilder.VisitProcedure = function(o){
		this.procedureid=		(o.procedureid || '');			//	Procedure ID
		this.diff=				(o.diff || '');					//  Used to view differences ('A' added 'R' removed 'M' modified)
		this.visitid=			(o.visitid || null);			//	NOT SAVED: Only used when using procedure window to attach visits to procedures.
		this.type= 				(o.type || '');					// 	Type of procedure ("R", "C", etc.)
		this.repetitions= 		(o.repetitions || 1);			//	Number of times this procedure is performed in this visit
		this.procedure=			(o.procedure || null);			//	NOT SAVED: For getProcedure
		this.parentVisit=		(o.parentVisit || null);		//	NOT SAVED: For getTotal

		// Functions
		this.getParentEpoch= function(){
			var vid=this.visitid;
			var i = budget.epochs.length, j=0, k=0, l=0;
			while(i--){
				j = budget.epochs[i].arms.length;
				while(j--){
					k = budget.epochs[i].arms[j].cycles.length;
					while(k--){
						l = budget.epochs[i].arms[j].cycles[k].visits.length;
						while(l--){
							if (budget.epochs[i].arms[j].cycles[k].visits[l].id == vid){
								this.parentEpoch = budget.epochs[i];	// retain for later calculations
								this.parentVisit = budget.epochs[i].arms[j].cycles[k].visits[l];	// retain for later calculations
								return this.parentEpoch;
							}
						}
					}
				}
			}
		};
		
		
		this.getProcedure= function(){
			
			// first find parent epoch, since copied epochs can have duplicate proc ids
			
			var procs = this.getParentEpoch().procedures;
			var id = this.procedureid;
			if (this.procedure && this.procedure != null) return this.procedure;
			var i = procs.length, j=0;
			while(i--){
				if (procs[i].id == id){
					this.procedure = procs[i];	// retain for later calculations
					return this.procedure;
				}
			}
			return null;
		};
		
		this.getTotal= function(){
			var p = this.getProcedure();
			if (p == null || this.diff == "D") return 0;
			var cost = parseFloat(p.getAmount(Clara.BudgetBuilder.TotalBy));
		
			return cost * this.repetitions;
		};
		
		this.toXML= function(){
			var xml = "<vp pid='"+this.procedureid+"' t='"+this.type+"' r='"+this.repetitions+"'/>";
			return xml;
		};
		
};

Clara.BudgetBuilder.ProcedureCode = function(o){
		this.id=				(o.id || '');					// 	Code Identifier ('82049', '3291.23120.2319', or whatever)
		this.diff=				(o.diff || '');
		this.type=				(o.type || '');				//	Type of code ('SOFT', 'SNOMED')
		this.description=		(o.description || '');					//	Long description of code
		//this.primary=			(o.primary || true);				//  Is this procedure code the primary code (for use to display on budget, conversion to PSC, etc.)
		
		// Functions
		this.copy= function(){
			return new Clara.BudgetBuilder.ProcedureCode({
				id:this.id,
				type:this.type,
				description:this.description
			});
		};
		
		this.toXML= function(){
			return "<code id='"+this.id+"' type='"+this.type+"' description='"+Encoder.htmlEncode(this.description)+"'/>";
		};
};


Clara.BudgetBuilder.Procedure = function(o){
		this.id=				(o.id || ''); 					//	Unique ID for this procedure
		this.isSubprocedure=	(o.isSubprocedure || false);
		this.diff=				(o.diff || '');
		this.type=				(o.type || 'normal');				// can be normal, misc, officevisit or outside
		this.description=		(o.description || '');
		this.expensecategory=	(o.expensecategory || '');
		this.notes=				(o.notes || '');
		this.clinicalNotes=		(o.clinicalNotes || '');
		this.conditional=		(o.conditional || false);
		this.alternative=		(o.alternative || false);
		this.cptCode=			(o.cptCode || 0);
		this.cdmCode=			(o.cdmCode || 0);
		this.location=			(o.location || '');
		this.category=			(o.category || '');				//	Category of the procedure ("Radiology", etc.)
		this.subprocedures=		(o.subprocedures || []);		//  Sub-Procedures in this visit (if this is a grouped procedure)
		this.codes=				(o.codes || []);	
		this.phys= {
				id:				(o.phys.id || 0),
			only:				(o.phys.only || false),
			cost:				(o.phys.cost || 0),
			locationCode:		(o.phys.locationCode || ''),
			locationDesc:		(o.phys.locationDesc || '')
		};
		this.hosp= {
				id:				(o.hosp.id || 0),
			only:				(o.hosp.only || false),
			description:		(o.hosp.description || ''),
			cost:				(o.hosp.cost || 0),
			locationCode:		(o.hosp.locationCode || ''),
			locationDesc:		(o.hosp.locationDesc || '')
		};
		var h = this.hosp;
		var p = this.phys;
		
		this.cost=				{
			misc:			(o.cost.misc || 0),
			sponsor:		(o.cost.sponsor || 0),				//	Cost to sponsor
			price:			(o.cost.price || 0),				//	Price to charge
			getTotal:		function(){ 
				return (parseFloat(this.misc)+parseFloat(h.cost)+parseFloat(p.cost));
			},
			getResidual:	function(){
				return (parseFloat(this.price) - parseFloat(this.getTotal()));
			}

		};
		
		// Functions
		
		this.getAmount= function(amountType){
			var proc = this;
			var procCosts = {
					cost:(parseFloat(proc.cost.misc)>0)?parseFloat(proc.cost.misc):(parseFloat(proc.hosp.cost)+parseFloat(proc.phys.cost)),
					sponsor:(proc.cost.sponsor),
					price:(proc.cost.price),
					residual:(proc.cost.getResidual())
				};
			return procCosts[amountType];
		};
		
		this.getTotalsForArm= function(arm,totalby){
			var armTotals = {
				R:0,
				C:0,
				I:0,
				RNS:0,
				CNMS:0,
				CL:0
			};
			totalby = totalby || Clara.BudgetBuilder.TotalBy;
			var proc = this;
			
			if (proc.diff =="D") return armTotals; 
			
			var procCosts = {
				cost:(parseFloat(proc.cost.misc)>0)?parseFloat(proc.cost.misc):(parseFloat(proc.hosp.cost)+parseFloat(proc.phys.cost)),
				sponsor:(proc.cost.sponsor),
				price:(proc.cost.price),
				residual:(proc.cost.getResidual())
			};

			var v = arm.getAllVisits();
			var j = v.length;
			while (j--){
				var visitProcedure = v[j].getVisitProcedure(proc);
				if (visitProcedure && visitProcedure.diff != "D"){
					//
					reps = visitProcedure.repetitions;
					// check parent cycle for repeat
					if (v[j].parentCycle && !v[j].parentCycle.repeatforever){
						reps = reps * parseInt(v[j].parentCycle.repetitions);
					}
					//
					armTotals[visitProcedure.type.toUpperCase()] = armTotals[visitProcedure.type.toUpperCase()] + (procCosts[totalby] * reps);
					
				}
			}
		
			return armTotals;
		};
		
		this.copy= function(keepOriginalId){
			var b = this;
			return new Clara.BudgetBuilder.Procedure({
				id:(keepOriginalId)?b.id:budget.newId(),
				type:b.type,
				diff:b.diff,
				isSubprocedure:b.isSubprocedure,
				description:b.description,
				expensecategory:b.expensecategory,
				cptCode:b.cptCode,
				cdmCode:b.cdmCode,
				notes:b.notes,
				clinicalNotes:b.clinicalNotes,
				conditional:b.conditional,
				alternative:b.alternative,
				category:b.category,
				codes:b.copyCodes(),
				location:b.location,
				subprocedures:b.copySubprocedures(),
				hosp: {
					id:b.hosp.id,
					only:b.hosp.only,
					description:b.hosp.description,
					cost:b.hosp.cost,
					locationCode:b.hosp.locationCode,
					locationDsc:b.hosp.locationDesc
				},
				phys: {
					id:b.phys.id,
					only:b.phys.only,
					cost:b.phys.cost,
					locationCode:b.phys.locationCode,
					locationDsc:b.phys.locationDesc
				},
				cost: {
					misc:b.cost.misc,
					sponsor:b.cost.sponsor,
					price:b.cost.price
				}
			});
		};
		
		this.isOfficeVisit = function(){
			
			var officeVisitCodes = ['90804','90808','99056','99058','99201',
			                        '99202','99203','99204','99205','99211',
			                        '99212','99213','99214','99215','99241',
			                        '99242','99243','99244','99245','99354',
			                        '99355'];
	
			for(var i = 0; i < officeVisitCodes.length; i ++) {
				if(officeVisitCodes[i] == this.cptCode) return true;
			}
			
			return false;

		};
		
		this.getHighestCostSubProcedure= function(){
			var t=this;
			if (t.subprocedures.length == 0) return t;
			else {
				// Find highest-cost procedure or sub-procedure and return it
				var highestproc = t;
				var i = this.subprocedures.length;
				while(i--){
					if (this.subprocedures[i].cost.getTotal() > highestproc.cost.getTotal()) highestproc = this.subprocedures[i];
				}
				// Swap this subprocedure with the parent
				return highestproc;
			}
		};
		
		this.addSubprocedure= function(proc){
			this.subprocedures.push(proc);
		};
		
		this.updateSubprocedure= function(proc){
			var i = this.subprocedures.length;
			while(i--){
				if (this.subprocedures[i].id == proc.id){
					this.subprocedures.splice(i,1,proc);
					return true;
				}
			}
			return false;
		};
		
		this.removeSubprocedure= function(id){
			var i = this.subprocedures.length;
			while(i--){
				if (this.subprocedures[i].id == id) highestproc = this.subprocedures.splice(i,1);
			}
		};
		
		this.getSubprocedure= function(id){
			var i = this.subprocedures.length;
			while(i--){
				if (this.subprocedures[i].id == id) return this.subprocedures[i];
			}
			return null;
		};
		
		this.getSubprocedureArray= function(){
			var a = [];
			var sp = null;
			var i = this.subprocedures.length;
			while(i--){
				sp = this.subprocedures[i];
				a.push([sp.id, sp.cptCode, sp.getDescription(), sp.cost.getTotal(), sp.cost.sponsor,sp.cost.price,(sp.cost.price-sp.cost.getTotal()), sp.notes]);
			}
			return a;
		};
		
		this.swapWithHighestCostProcedure= function(){
			var t=this;
			if (t.subprocedures.length == 0) return;
			else {
				// Find highest-cost procedure or sub-procedure and return it
				var highestproc = t;
				var i = this.subprocedures.length;
				while(i--){
					if (t.subprocedures[i].cost.getTotal() > highestproc.cost.getTotal()) highestproc = this.subprocedures[i];
				}
				// Swap this subprocedure with the parent
				highestproc.isSubprocedure = false;
				t.swapProcedureInformation(highestproc);
			}
		};
		
		this.swapProcedureInformation= function(proc, retainId){
			
			var proc1 = this;
			var proc2 = proc;
			
			var tproc = proc2.copy(true);
			if (!retainId) proc2.id = proc1.id;
			proc2.type = proc1.type;
			proc2.diff = proc1.diff;
			proc2.description = proc1.description;
			proc2.expensecategory = proc1.expensecategory;
			proc2.cptCode = proc1.cptCode;
			proc2.cdmCode = proc1.cdmCode;
			proc2.notes = proc1.notes;
			proc2.clinicalNotes = proc1.clinicalNotes;
			proc2.conditional = proc1.conditional;
			proc2.alternative = proc1.alternative;

			proc2.category = proc1.category;
			proc2.codes = proc1.copyCodes();
			proc2.hosp.only = proc1.hosp.only;
			proc2.hosp.description = proc1.hosp.description;
			proc2.hosp.locationCode = proc1.hosp.locationCode;
			proc2.hosp.locationDesc = proc1.hosp.locationDesc;
			proc2.hosp.cost = proc1.hosp.cost;
			proc2.hosp.id = proc1.hosp.id;
			proc2.phys.only = proc1.phys.only;
			proc2.phys.locationCode = proc1.phys.locationCode;
			proc2.phys.locationDesc = proc1.phys.locationDesc;
			proc2.phys.cost = proc1.phys.cost;
			proc2.phys.id = proc1.phys.id;
			proc2.cost.misc = proc1.cost.misc;
			proc2.cost.sponsor = proc1.cost.sponsor;
			proc2.cost.price = proc1.cost.price;
			
			if (!retainId) proc1.id = tproc.id;
			proc1.type = tproc.type;
			proc1.diff = tproc.diff;
			proc1.description = tproc.description;
			proc1.expensecategory = tproc.expensecategory;
			proc1.cptCode = tproc.cptCode;
			proc1.cdmCode = tproc.cdmCode;
			//proc1.snomedCode = tproc.snomedCode;
			//proc1.softCode = tproc.softCode;
			proc1.conditional = proc2.conditional;
			proc1.alternative = proc2.alternative;
			proc1.notes = proc2.notes;
			proc1.clinicalNotes = proc2.clinicalNotes;
			proc1.category = tproc.category;
			proc1.codes = tproc.copyCodes();
			proc1.hosp.only = tproc.hosp.only;
			proc1.hosp.description = tproc.hosp.description;
			proc1.hosp.locationCode = tproc.hosp.locationCode;
			proc1.hosp.locationDesc = tproc.hosp.locationDesc;
			proc1.hosp.cost = tproc.hosp.cost;
			proc1.hosp.id = tproc.hosp.id;
			proc1.phys.only = tproc.phys.only;
			proc1.phys.locationCode = tproc.phys.locationCode;
			proc1.phys.locationDesc = tproc.phys.locationDesc;
			proc1.phys.cost = tproc.phys.cost;
			proc1.phys.id = tproc.phys.id;
			proc1.cost.misc = tproc.cost.misc;
			proc1.cost.sponsor = tproc.cost.sponsor;
			proc1.cost.price = tproc.cost.price;
			
		};
		
		this.copySubprocedures= function(){
			var s = [];
			var i = this.subprocedures.length;
			while(i--){
				s.push(this.subprocedures[i].copy());
			}
			return s;
		};
		
		this.copyCodes= function(){
			var c = [];
			var i = this.codes.length;
			while(i--){
				c.push(this.codes[i].copy());
			}
			return c;
		};
		
		this.addCode=	function(code){
			this.removeCodeById(code.id);
			this.codes.push(code);
		};
		
		this.removeCodeById=	function(id){
			var i = this.codes.length;
			while(i--){
				if (this.codes[i].id == id){
					this.codes.splice(i,1);
					break;
				}
			}
		};
		
		this.getCodes=	function(){
			return codes;
		};
		
		this.getCodeArray= function(){
			var c = this.codes;
			var ca = [];
			var i = this.codes.length;
			while(i--){
				ca.push([c[i].id, c[i].type, c[i].description]);
			}
			//cdebug("procedure.getCodeArray",this,ca);
			return ca;
		};
		
		this.getDescription= function(){
			
			//if (this.snomedCode != '') return (this.snomedCode+": "+this.description);
			//if (this.softCode != '')  return (this.softCode+": "+this.description);
			var cost = (parseFloat(this.hosp.cost) > 0)?(" (H: "+Ext.util.Format.usMoney(this.hosp.cost)+")"):"";
			return (this.cptCode > 0 && this.type == 'normal')?(this.cptCode+": "+this.description+cost):(this.description+cost);
		};
		

		
		this.toXML= function(){
			var xml = "<procedure id='"+this.id+"' alternative='"+this.alternative+"' conditional='"+this.conditional+"' type='"+this.type+"' expensecategory='"+this.expensecategory+"' description='"+Encoder.htmlEncode(this.description)+"' cptcode='"+this.cptCode+"' cdmcode='"+this.cdmCode+"' category='"+Encoder.htmlEncode(this.category)+"' location='"+Encoder.htmlEncode(this.location)+"'>";
			xml=xml+"<hosp id='"+this.hosp.id+"' cost='"+this.hosp.cost+"' only='"+this.hosp.only+"' locationcode='"+this.hosp.locationCode+"' locationdesc='"+Encoder.htmlEncode(this.hosp.locationDesc)+"' description='"+Encoder.htmlEncode(this.hosp.description)+"'/>";
			xml=xml+"<phys id='"+this.phys.id+"' cost='"+this.phys.cost+"' only='"+this.phys.only+"' locationcode='"+this.phys.locationCode+"' locationdesc='"+Encoder.htmlEncode(this.phys.locationDesc)+"'/>";
			xml=xml+"<notes>"+Encoder.htmlEncode(this.notes)+"</notes>";
			xml += "<clinical-notes>"+Encoder.htmlEncode(this.clinicalNotes)+"</clinical-notes>";
			xml=xml+"<cost><misc>"+this.cost.misc+"</misc><sponsor>"+this.cost.sponsor+"</sponsor><price>"+this.cost.price+"</price><residual>"+this.cost.getResidual()+"</residual></cost>";
			if (this.codes.length > 0){
				xml = xml + "<codes>";
				var i = this.codes.length;
				while(i--){
					xml = xml + this.codes[i].toXML();
				} 
				xml = xml + "</codes>";
			}
			if (this.subprocedures.length > 0){
				xml = xml + "<subprocedures>";
				var j = this.subprocedures.length;
				while(j--){
					xml = xml + this.subprocedures[j].toXML();
				}
				xml = xml + "</subprocedures>";
			}
			xml = xml + "</procedure>";
			return xml;
		};
		
};

Clara.BudgetBuilder.Visit = function(o){
		this.id=				(o.id || '');				//	Unique ID for this visit
		this.diff=				(o.diff || '');
		this.name=				(o.name || '');				// 	Name of the visit ("Day 4", etc.)
		this.cycleindex= 		(o.cycleindex || 0);		//	1-based Position of visit in the cycle (1st, 2nd, etc.)
		this.unit=				(o.unit || 'Day');
		this.unitvalue=			(o.unitvalue || 0);
		this.subjectcount=      (o.subjectcount || 0);
		this.visitprocedures=	(o.visitprocedures || []);	//  Procedures in this visit
		this.notes=				(o.notes || '');

		// Functions
		
		this.getParent= function(parenttype){
			var t = this;
			var i = budget.epochs.length;
			var j = 0,k=0,l=0,m=0;
			while(i--){
				j = budget.epochs[i].arms.length;
				while(j--){
					k = budget.epochs[i].arms[j].cycles.length;
					while(k--){
						l = budget.epochs[i].arms[j].cycles[k].visits.length;
						while(l--){
							if (budget.epochs[i].arms[j].cycles[k].visits[l].id == t.id){
								if (parenttype == 'cycle'){
									return budget.epochs[i].arms[j].cycles[k];
								}else if (parenttype == 'arm'){
									return budget.epochs[i].arms[j];
								}else if (parenttype == 'epoch'){
									return budget.epochs[i];
								}else{
									return null;
								}
							}
						}
					}
				}
			}
			return null;
		};
		
		this.getTotals= function(){
			var typedTotals = {
				R:0,
				C:0,
				O:0,
				I:0,
				RNS:0,
				CNMS:0,
				CL:0
			};
			var x = this.visitprocedures.length;
			if (x <= 0 || this.diff == "D") return typedTotals;
			while(x--){
				if (this.visitprocedures[x].diff != "D") typedTotals[this.visitprocedures[x].type.toUpperCase()] += (this.visitprocedures[x].getTotal() * this.subjectcount);
			}
		
			return typedTotals;
		};
		
		
		this.getVisitProcedure= function(procedure){
			var x = this.visitprocedures.length;
			while(x--){
				if (this.visitprocedures[x].procedureid == procedure.id){
					return this.visitprocedures[x];
				}
			}
			return false;
		};
		
		this.hasSubProcedureMarked= function(subProc){
			var j = this.visitprocedures.length;
			while(j--){
				if(this.visitprocedures[j].procedureid == subProc.id) {
					return true;
				}
			}
			
			return false;
		};
		
		this.hasOfficeVisitItemSubprocedures = function(procedure){
			if(procedure.visitprocedures && procedure.subprocedures && procedure.subprocedures.length > 0) {
				var i = procedure.visitprocedures.length;
				while(i--){
					var j = this.visitprocedures.length;
					while(j--){
						if(procedure.subprocedures[i].type == 'officevisit' && this.visitprocedures[j].procedureid == procedure.subprocedures[i].id) {
							return true;
						}
					}
				}
			}
			return false;
		};
		
		this.copy= function(){
			var newId = budget.newId();
			var newVisit = new Clara.BudgetBuilder.Visit({
				id:newId,
				name:this.name,
				notes:this.notes,
				cycleindex:this.cycleindex,
				unit:this.unit,
				unitvalue:this.unitvalue,
				subjectcount:this.subjectcount,
				parentCycle:this.parentCycle || null
			});
			newVisit.visitprocedures = this.copyVisitProcedures(newId);
			
			return newVisit;
		};
		
		/**
		 * This will not update Outside procedure
		 */
		this.addAllProceduresToVisit= function(procs, vtype,reps, ignoreOutsideProcedure){
			var visit = this;			
			var vps = [];			
			var i = procs.length;
			while(i--){
				
				var vp = null;			
				
				//only update the count for outside procedures
				if(ignoreOutsideProcedure && procs[i].type == 'outside' && vtype != 'O'){
										
					////cdebug("outside");
					////cdebug(this.getVisitProcedure(procs[i]));
					if(this.getVisitProcedure(procs[i])) {
						
						vp = this.getVisitProcedure(procs[i]);
											
						if(vp.repetitions > 0 ){
							vp.repetitions = reps;
						}
						
						////cdebug("outside");
						////cdebug(vp);
					}
				}else{
					vp = new Clara.BudgetBuilder.VisitProcedure({
						procedureid:procs[i].id,
						type:vtype,
						visitid:visit.id,
						repetitions:reps
					});
				}
				
				if(vp != null){
					vps.push(
						vp
					);
				}				
				////cdebug(this.visitprocedures);
			}
			
			////cdebug(vps);
			
			this.visitprocedures = vps;
		};
		
		this.setAllVisitProcedures= function(vtype,reps){
			////cdebug("visit.setallvisitprocedures: vtype="+vtype+" reps="+reps);
			if (vtype && reps){
				////cdebug("setting?");
				var x = this.visitprocedures.length;
				while(x--){
					this.visitprocedures[x].type = vtype;
					this.visitprocedures[x].repetitions = reps;
				}
			} else {
				this.visitprocedures.splice(0,this.visitprocedures.length);
			}
			////cdebug(this.visitprocedures);
		};
		
		this.copyVisitProcedures= function(newVisitId){
			var vpcopy = [];

			var x = this.visitprocedures.length;
			while(x--){
				vpcopy.push(
						new Clara.BudgetBuilder.VisitProcedure({
							procedureid:this.visitprocedures[x].procedureid,
							type:this.visitprocedures[x].type,
							repetitions:this.visitprocedures[x].repetitions,
							visitid:newVisitId
						})
				);
			}
			return vpcopy;
		};
		
		this.calculateTotal= function(totalby){
			totalby = totalby || Clara.BudgetBuilder.TotalBy;
			this.parentEpoch = this.parentEpoch || this.getParent('epoch');
			this.parentCycle = this.parentCycle || this.getParent('cycle');
			if (this.parentEpoch){
				var ep = this.parentEpoch;
				var total = 0;
				
				var amount = 0;
				var reps = 1;
				
				var x = this.visitprocedures.length;
				while(x--){
					if (this.visitprocedures[x].type == 'R' && this.visitprocedures[x].diff != 'D'){
						// get procedure and its costs, add to total
						var proc = ep.getProcedureById(this.visitprocedures[x].procedureid);
						if (proc && (typeof proc.isSubprocedure == undefined || !proc.isSubprocedure)){
							 
								amount = (totalby == 'sponsor' || totalby == 'price')?proc.cost[totalby]:(proc.cost.getTotal());
								reps = this.visitprocedures[x].repetitions;
								total += (amount * reps);
							
						}
					}
				}
				return total;
			} else { cwarn("arm.calculateTotal: No parentEpoch defined",this);return 0; }
		};
		
		this.toXML= function(){
			var xml = "<visit id='"+this.id+"' name='"+Encoder.htmlEncode(this.name)+"' subj='"+this.subjectcount+"' cycleindex='"+this.cycleindex+"' unit='"+this.unit+"' unitvalue='"+this.unitvalue+"'>";
			xml = xml + "<visitprocedures>";
			var x = this.visitprocedures.length;
			while(x--){
				xml = xml + this.visitprocedures[x].toXML();
			}
			xml = xml + "</visitprocedures>";
			xml = xml + "<notes>" + Encoder.htmlEncode(this.notes) + "</notes>";
			
			// totals for excel export ONLY (not used for calculation)
			xml += "<totals>";
			var tp = this.calculateTotal('price');
			var tc = this.calculateTotal('cost');
			var ts = this.calculateTotal('sponsor');
			var arm = this.getParent('arm');
			xml += "<total by='price' psd='"+tp+"'/>";
			xml += "<total by='cost' psd='"+tc+"'/>"; 
			xml += "<total by='sponsor' psd='"+ts+"'/>"; 
			xml += "</totals>";
			
			xml = xml + "</visit>";
			return xml;
		};
		
};

Clara.BudgetBuilder.Cycle = function(o){
		this.id=				(o.id || '');				//	Unique ID for this cycle
		this.diff=				(o.diff || '');
		this.simple=			(typeof o.simple==undefined?true:o.simple);			// simple cycle by default
		this.name= 				(o.name || '');				// 	Name of the cycle ("Cycle 1", etc.)
		this.index=				(o.index || 0);				//	order index
		this.startday=			(o.startday || 0);				//	Start day
		this.endday=			(o.endday || 0);				//	End day
		this.visits=			(o.visits || []);				//  Visits in this cycle
		this.repetitions=		(o.repetitions || 1);
		this.repeatforever=		(o.repeatforever || false);
		this.duration=			(o.duration || 0);		
		this.durationunit=		(o.durationunit || 'Day');
		this.notes=				(o.notes || '');
		
		// Functions
		
		this.getTotals= function(){
			var cycleTotals = {
				R:0,
				C:0,
				O:0,
				I:0,
				RNS:0,
				CNMS:0,
				CL:0
			};
			
			if (this.diff == "D") return cycleTotals;
			
			var x = this.visits.length;
			while(x--){
				if (this.visits[x].diff != "D") {
					var reps = (this.repeatforever)?1:this.repetitions;
					var visitTotals = this.visits[x].getTotals();
					cycleTotals['R'] += visitTotals['R'] * reps;
					cycleTotals['C'] += visitTotals['C'] * reps;
					cycleTotals['O'] += visitTotals['O'] * reps;
					cycleTotals['I'] += visitTotals['I'] * reps;
					cycleTotals['RNS'] += visitTotals['RNS'] * reps;
					cycleTotals['CNMS'] += visitTotals['CNMS'] * reps;
					cycleTotals['CL'] += visitTotals['CL'] * reps;
				}
			}
			
			return cycleTotals;
		};
		
		this.copy= function(newstartday, preventCreateNewId){
			clog("copying cycle",this,newstartday,"PREVENT NEW ID?",preventCreateNewId);
			var preventCreateNewId = preventCreateNewId || false;
			var copyStartDay = (newstartday)?newstartday:this.startday;
			var copyEndDay = copyStartDay + (this.endday - this.startday);
			//var copyDuration = (copyEndDay - copyStartDay + 1);
			
			return new Clara.BudgetBuilder.Cycle({
				id:preventCreateNewId?'':budget.newId(),
				simple:this.simple,
				name:this.name,
				index:this.index+1,
				repetitions:this.repetitions,
				repeatforever:this.repeatforever,
				duration:this.duration,//copyDuration,//this.duration,
				durationunit:this.durationunit,//'Day',//this.durationunit,
				notes:this.notes,
				startday:copyStartDay,//this.startday,
				endday:copyEndDay,//this.endday,
				visits:this.copyVisits()
			});
		};
		
		this.getParentEpoch= function(){
			var tid = this.id;
			var i = budget.epochs.length,j=0,k=0;
			while(i--){
				var e = budget.epochs[i];
				j=e.arms.length;
				while(j--){
					var a = e.arms[j];
					k=a.cycles.length;
					while(k--){
						if (a.cycles[k].id == tid) return e;	
					}
				}
			}
			return null;
		};
		
		this.removeVisits= function(visitsToRemove){	//accepts array of ext records OR clara visits
			
			var cycle = this;
			var visitIdsToRemove = [];
			if (Ext.isArray(visitsToRemove) && visitsToRemove.length > 0){
				if (typeof visitsToRemove[0].get == "function") {
					// we got Ext records
					for (var i=0,l=visitsToRemove.length;i<l;i++){
						visitIdsToRemove.push(visitsToRemove[i].get("id"));
					}
				} else if (typeof visitsToRemove[0].cycleindex != "undefined"){
					// we got array of type Clara.BudgetBuilder.Visit
					for (var i=0,l=visitsToRemove.length;i<l;i++){
						visitIdsToRemove.push(visitsToRemove[i].id);
					}
				}
				
				clog("cycle.removeVisits",this,visitIdsToRemove);
				
				cycle.visits = jQuery.map(cycle.visits,function(visit,l){
					for (var i=0,l=visitIdsToRemove.length;i<l;i++){
						if (visitIdsToRemove[i] == visit.id) return null;
					}
					return visit;
				});
				this.recalculateDateRanges();
				Clara.BudgetBuilder.MessageBus.fireEvent('cyclevisitsremoved', null);
			}
		};

		this.getFirstVisit = function(){
			this.sortVisits();
			if (this.visits.length > 0) return this.visits[0];
			else return null;
		};
		
		this.getLastVisit = function(){
			clog("cycle.getLastVisit()",this.visits);
			this.sortVisits();
			clog("cycle.getLastVisit(), sorted",this.visits);
			if (this.visits.length > 0) return this.visits[this.visits.length-1];
			else return null;
		};
		
		this.recalculateDateRanges= function(){
			var t = this;
			t.startday = parseInt(t.startday);
			t.endday = parseInt(t.endday);
			clog("cycle.recalculateDateRanges()",t);
			clog("cycle.recalculateDateRanges: start range ("+t.startday+","+t.endday+") length "+t.duration);
			var isSimple = (budget.budgetType == 'simple' || (t.getParentEpoch() && t.getParentEpoch().simple == true) || t.simple == true);
			if (isSimple){
				// if it's a simple cycle in a complex phase
				if(budget.budgetType != 'simple' && (t.getParentEpoch() && t.getParentEpoch().simple != true) && t.simple == true){
					clog("cycle.recalculateDateRanges: this cycle is simple");
					var lastVisitInCycle = t.getLastVisit();
					t.startday = (typeof t.startday == 'undefined' || t.startday == null)?1:t.startday;
					t.endday = (lastVisitInCycle)?(t.startday + (lastVisitInCycle.cycleindex-1)):t.startday;
					t.duration = t.endday - t.startday + 1;
					if (t.duration < 1) cwarn("WARNING: cycle.recalculateDateRanges: duration zero or less",t);
				}else{ // if it's a simple phase or a simple budget
					if(this.visits.length > 0)  {
						this.sortVisits(); // sort first
						var lastVisitInCycle = this.visits[this.visits.length-1];
						var firstVisitInCycle = this.visits[0];					
						clog("cycle.recalculateDateRanges: the first visit is", firstVisitInCycle);
						clog("cycle.recalculateDateRanges: the last visit is", lastVisitInCycle);
						t.startday = firstVisitInCycle.cycleindex;
						t.endday = lastVisitInCycle.cycleindex;
						t.duration = t.endday - t.startday + 1;
					}
				}
			} else {
				clog("cycle.recalculateDateRanges: this cycle is NOT simple");
				// this cycle has set start day and length.
				var multiplier = (t.durationunit == "Year")?365:(t.durationunit == "Week")?7:(t.durationunit == "Month")?30:1;
				if (t.repeatforever == true) t.endday = parseFloat(t.startday) + (multiplier*parseFloat(t.duration)) - 1;
				else t.endday = parseFloat(t.startday) + (parseFloat(t.repetitions) * (multiplier*parseFloat(t.duration))) - 1;
			}
			clog("cycle.recalculateDateRanges: updated range ("+t.startday+","+t.endday+") length "+t.duration,t);
		};
		
		this.copyVisits = function(){
			var a = [];
			var l=this.visits.length;
			for(i=0;i<l;i++){
				a.push(this.visits[i].copy());
			}
			return a;
		};
		
		this.sortVisits= function(){
			this.visits.sort(function(a,b){
				var x = a.cycleindex - b.cycleindex;
				return x?x:(a.name > b.name);
			});
		};
		

		this.validate= function(parentarm){
			var err = [];
			if (this.visits.length == 0){
				//cdebug("Arm '"+parentarm.name+"' Cycle '"+this.name+"' contains no visits");
				err.push("Arm '"+parentarm.name+"' Cycle '"+this.name+"' contains no visits");
			}
			return err;
		};
		
		this.visitArray= function(){
			this.sortVisits();
			var a = [];
			var l=this.visits.length;
			for(i=0;i<l;i++){
				a.push([this.visits[i].id, this.visits[i].cycleindex, this.visits[i].name, this.visits[i].unit, this.visits[i].unitvalue, this.visits[i].notes, this.visits[i].subjectcount]);
			}
			clog("cycle.visitArray",a);
			
			return a;
		};
		
		this.toXML= function(){

			var xml = "<cycle id='"+this.id+"' index='"+this.index+"' repetitions='"+this.repetitions+"' simple='"+this.simple+"' repeatforever='"+this.repeatforever+"' duration='"+this.duration+"' durationunit='"+this.durationunit+"' name='"+Encoder.htmlEncode(this.name)+"' startday='"+this.startday+"' endday='"+this.endday+"'>";
			xml = xml + "<notes>" + Encoder.htmlEncode(this.notes) + "</notes>";
			xml = xml + "<visits>";
			var i=this.visits.length;
			while(i--){
				xml = xml + this.visits[i].toXML();
			}
			xml = xml + "</visits>";
			xml = xml + "</cycle>";
			return xml;
		};
		
};

Clara.BudgetBuilder.Arm = function(o){
		this.id=			(o.id || budget.newId());				//	Unique ID for this arm
		this.diff=			(o.diff || '');
		this.name= 			(o.name || '');				// 	Name of the arm ("Drug A", etc.)
		this.index= 		(o.index || 0);				//	Position of arm (1st, 2nd, etc.)
		this.cycles= 		(o.cycles || []);			//	Cycles of type Clara.BudgetBuilder.Cycle
		this.hidden=		(o.hidden || false);		//	set to true to collapse all arm's columns to one on the grdipanel
		this.notes=			(o.notes || '');
		this.parentEpoch=	(o.parentEpoch || null);
		
		// Functions
		
		this.getTotals= function(){
			var armTotals = {
				R:0,
				C:0,
				O:0,
				I:0,
				RNS:0,
				CNMS:0,
				CL:0
			};
			var x=this.cycles.length;
			if (x <= 0 || this.diff == "D") { return armTotals; }
			while(x--){
				if (this.cycles[x].diff != "D") {
					var cycleTotals = this.cycles[x].getTotals();
					armTotals['R'] += cycleTotals['R'];
					armTotals['C'] += cycleTotals['C'];
					armTotals['O'] += cycleTotals['O'];
					armTotals['I'] += cycleTotals['I'];
					armTotals['RNS'] += cycleTotals['RNS'];
					armTotals['CNMS'] += cycleTotals['CNMS'];
					armTotals['CL'] += cycleTotals['CL'];
				}
			}
			
			return armTotals;
		};
		
		this.setIndex= function(idx){
			this.index = parseInt(""+idx);
		};
		
		this.copy= function(){
			return new Clara.BudgetBuilder.Arm({
				id:budget.newId(),
				name:this.name,
				notes:this.notes,
				index:this.index,
				hidden:this.hidden,
				parentEpoch:this.parentEpoch,
				cycles:this.copyCycles()
			});
		};
		
		this.copyCycles= function(){
			var c = [];
			var x=this.cycles.length;
			while(x--){
				c.push(this.cycles[x].copy());
			}
			return c;
		};
		
		this.getParentEpoch= function(){
			var tid = this.id, i=budget.epochs.length, j=0;
			while(i--){
				var e = budget.epochs[i];
				j = e.arms.length;
				while(j--){
					if (e.arms[j].id == tid) return e;
				}
			}
			return null;
		};
		
		this.sortCycles= function(){
			//cdebug("sorting cycles",this.cycles);
			this.cycles.sort(function(a,b){
				if (a.startday - b.startday == 0){
					if (a.name == b.name){
						return a.id - b.id;
					} else {
						if ( a.name < b.name ) return -1;
						if ( a.name > b.name ) return 1;
					}
				} else return a.startday - b.startday;
			});
			//cdebug("sorting cycles done",this.cycles);
		};
		
		this.getCycle= function(id){
			var i=this.cycles.length;
			while(i--){
				if(this.cycles[i].id == id){
					return this.cycles[i];
				}
			}
			return null;
		};
		
		this.validate= function(norecurse){
			norecurse = norecurse || false;
			var err = [];
			if (this.cycles.length == 0){
				//cdebug("Arm '"+this.name+"' contains no cycles");
				err.push("Arm '"+this.name+"' contains no cycles");
			} else {
				if (!norecurse){
					var i=this.cycles.length;
					while(i--){
						err = err.concat(this.cycles[i].validate(this));
					}
				}
			}
			return err;
		};
		
		this.cycleLastDay = function(){
			var ld = null;
			var i=this.cycles.length;
			while(i--){
				if (ld == null) ld = this.cycles[i].endday;
				else if (ld < this.cycles[i].endday) ld = this.cycles[i].endday;
			}
			return ld;
		};
		
		this.cycleArray= function(){
			var a = [];
			this.sortCycles();
			var l = this.cycles.length;
			for (var i=0; i<l;i++){
				a.push([this.cycles[i].id, this.cycles[i].index, this.cycles[i].name, this.cycles[i].startday, this.cycles[i].endday, this.cycles[i].repetitions, this.cycles[i].repeatforever]);
			}
			return a;
		};
				
		this.getAllVisits= function(){
			var visits = [];
			var l = this.cycles.length;
			for (var i=0; i<l;i++){
				this.cycles[i].sortVisits();
				visits = visits.concat(this.cycles[i].visits);
			}
			return visits;
		};

		this.removeCycles= function(cyclesToRemove){	//accepts array of ext records OR clara visits
			
			var arm = this;
			var cycleIdsToRemove = [];
			if (Ext.isArray(cyclesToRemove) && cyclesToRemove.length > 0){
				if (typeof cyclesToRemove[0].get == "function") {
					// we got Ext records
					for (var i=0,l=cyclesToRemove.length;i<l;i++){
						cycleIdsToRemove.push(cyclesToRemove[i].get("id"));
					}
				} else if (typeof cyclesToRemove[0].toXML != "undefined"){
					// we got array of type Clara.BudgetBuilder.Arm
					for (var i=0,l=cyclesToRemove.length;i<l;i++){
						cycleIdsToRemove.push(cyclesToRemove[i].id);
					}
				}
				
				clog("arm.removeCycles",this,cycleIdsToRemove);
				
				arm.cycles = jQuery.map(arm.cycles,function(cycle,l){
					for (var i=0,l=cycleIdsToRemove.length;i<l;i++){
						if (cycleIdsToRemove[i] == cycle.id) return null;
					}
					return cycle;
				});
				Clara.BudgetBuilder.MessageBus.fireEvent('armcyclesremoved', null);
			}
		};
		
		this.calculateTotal= function(totalby){
			var arm = this;
			totalby = totalby || Clara.BudgetBuilder.TotalBy;
			var armTotals = {
				R:0,
				C:0,
				I:0,
				RNS:0,
				CNMS:0,
				CL:0
			};
			if (arm.diff == "D") return armTotals;
			
			    this.parentEpoch = this.parentEpoch || this.getParentEpoch();
				var procs = this.parentEpoch.procedures;
				var reps = 1;
				var i=procs.length;
				while(i--){
					var at = procs[i].getTotalsForArm(arm,totalby);
					armTotals.R += at.R;
					armTotals.C += at.C;
					armTotals.I += at.I;
					armTotals.RNS += at.RNS;
					armTotals.CNMS += at.CNMS;
					armTotals.CL += at.CL;
				}
		
		

			return armTotals;
		};
		
		
		
		this.toXML= function(){

			var xml = "<arm id='"+this.id+"' name='"+Encoder.htmlEncode(this.name)+"' index='"+this.index+"' hidden='"+this.hidden+"'>";
			xml = xml + "<notes>" + Encoder.htmlEncode(this.notes) + "</notes>";
			xml = xml + "<cycles>";
			for (var i=0; i<this.cycles.length;i++){
				xml = xml + this.cycles[i].toXML();
			}
			xml = xml + "</cycles>";
			
			// totals for excel export ONLY
			xml += "<proctotals>";
			// getTotalsForArm
			this.parentEpoch = this.parentEpoch || this.getParentEpoch();
			var procs = this.parentEpoch.procedures;
			var i=procs.length;
			while(i--){
				var ptC = procs[i].getTotalsForArm(this,'cost');
				var ptP = procs[i].getTotalsForArm(this,'price');
				var ptS = procs[i].getTotalsForArm(this,'sponsor');
				
				xml+="<pt pid='"+procs[i].id+"'>";
				xml += "<t by='cost' r='"+ptC.R+"' c='"+ptC.C+"' i='"+ptC.I+"' rns='"+ptC.RNS+"' cnms='"+ptC.CNMS+"' cl='"+ptC.CL+"'/>";
				xml += "<t by='price' r='"+ptP.R+"' c='"+ptP.C+"' i='"+ptP.I+"' rns='"+ptP.RNS+"' cnms='"+ptP.CNMS+"' cl='"+ptP.CL+"'/>";
				xml += "<t by='sponsor' r='"+ptS.R+"' c='"+ptS.C+"' i='"+ptS.I+"' rns='"+ptS.RNS+"' cnms='"+ptS.CNMS+"' cl='"+ptS.CL+"'/>";
				xml += "</pt>";
			}
			
			xml += "</proctotals>";
			xml += "<armtotals>";
			
			var atC = this.calculateTotal('cost');
			var atP = this.calculateTotal('price');
			var atS = this.calculateTotal('sponsor');
			xml += "<t by='cost' r='"+atC.R+"' c='"+atC.C+"' i='"+atC.I+"' rns='"+atC.RNS+"' cnms='"+atC.CNMS+"' cl='"+atC.CL+"'/>";
			xml += "<t by='price' r='"+atP.R+"' c='"+atP.C+"' i='"+atP.I+"' rns='"+atP.RNS+"' cnms='"+atP.CNMS+"' cl='"+atP.CL+"'/>";
			xml += "<t by='sponsor' r='"+atS.R+"' c='"+atS.C+"' i='"+atS.I+"' rns='"+atS.RNS+"' cnms='"+atS.CNMS+"' cl='"+atS.CL+"'/>";
			
			xml += "</armtotals>";
			
			xml = xml + "</arm>";
			return xml;
		};
		
};

Clara.BudgetBuilder.Epoch = function(o){
		this.id=				(o.id || '');				//	Unique ID for this epoch
		this.diff=				(o.diff || '');
		this.name= 				(o.name || '');				// 	Name of the epoch ("Screening", etc.)
		this.conditional=		(o.conditional || false);
		this.simple=			(o.simple || false);
		this.notes= 			(o.notes || '');				
		this.index= 			(o.index || 0);				//	Position of epoch (1st, 2nd, etc.)
		this.arms= 				(o.arms || []);				//	Arms of type Clara.BudgetBuilder.Arm
		this.procedures=		(o.procedures || []);		//	Procedures of type Clara.BudgetBuilder.Procedure
		
		// Functions
		this.copy= function(name){
			return new Clara.BudgetBuilder.Epoch({
				id:budget.newId(),
				name:(name || this.name),
				conditional:this.conditional,
				simple:this.simple,
				notes:this.notes,
				index:this.index,
				arms:this.copyArms(),
				procedures:this.copyProcedures(true)		// "true" to keep original id's, since proc id's can
															// be treated as unique to each epoch.
			});
		};
		
		this.canMakeComplex= function(){
			var t = this;
			if (!t.simple) return false;	// it's already complex
			else {
				// Check for negative-day visit
				for (var i=0,l=t.arms.length;i<l;i++){
					for (var j=0,m=t.arms[i].cycles.length;j<m;j++){
						for (var k=0,n=t.arms[j].cycles[k].visits.length;k<n;k++){
							var v = t.arms[i].cycles[j].visits[k];
							if (v.unitvalue < 0) return false;
						}
					}
				}
				return true;
			}
		};
		
		this.makeComplex= function(){
			var t = this;
			t.simple = false;
			// Give all Arms/Cycles names
			for (var i=0,l=t.arms.length;i<l;i++){
				t.arms[i].name = "Arm "+(i+1);
				for (var j=0,m=t.arms[i].cycles.length;j<m;j++){
					t.arms[i].cycles[j].name = "Cycle "+(j+1);
				}
			}
		};
		
		this.rename= function(){

		};
		
		this.copyArms= function(){
			var a = [];
			var il = this.arms.length;
			for (var i=0; i<il; i++){
				a.push(this.arms[i].copy());
			}
			return a;
		};
		
		this.copyProcedures= function(keepOriginalId){
			keepOriginalId = (keepOriginalId || false);
			var p = [];
			var pl = this.procedures.length;
			for (var i=0; i<pl; i++){
				p.push(this.procedures[i].copy(keepOriginalId));
			}
			return p;
		};
		
		this.removeProcedureById= function(id){

			for(i=0,l=this.procedures.length;i<l;i++){
				if (this.procedures[i].id == id){
					// first find & remove all visits' visitprocedures with the same procedureid as id
					budget.removeVisitProceduresByEpochAndProcedure(this,this.procedures[i]);
					this.procedures.splice(i,1);
					break;
				}
			}
			
		};
		
		this.getProcedureById= function(id){
			var i = this.procedures.length;
			while(i--){
				if (this.procedures[i].id == id){
					return this.procedures[i];
				} else if (this.procedures[i].getSubprocedure(id) != null) {
					return this.procedures[i].getSubprocedure(id);
				}
			}
		};
		
		this.getVisitById = function(id){
			var j=this.arms.length,k=0,l=0;
			
			while(j--){
				k = this.arms[j].cycles.length;
				while(k--){
					l = this.arms[j].cycles[k].visits.length;
					while(l--){
						if (this.arms[j].cycles[k].visits[l].id == id){
							return this.arms[j].cycles[k].visits[l];
						}
					}
				}
			}
			
			return null;

		};
		
		this.updateProcedure= function(proc){
			var i=this.procedures.length;
			while(i--){
				if (this.procedures[i].id == proc.id){
					this.procedures.splice(i,1,proc);
					return true;
				}
			}
			return false;
		};
		
		this.getArm= function(id){
			var i=this.arms.length;
			while(i--){
				if(this.arms[i].id == id){
					return this.arms[i];
				}
			}
			return null;
		};
		
		this.sortArms= function(){
			this.arms.sort(function(a,b){ return a.index - b.index; });
		};
		
		this.validate= function(){
			var errors = [];
			// check for name
			if (jQuery.trim(this.name) == ''){
				errors.push("Epoch contains no name.");
			}
			// Check for arms
			if (this.arms.length < 1){

				errors.push("Epoch '"+this.name+"' contains no arms.");
			}else{
				var i=this.arms.length;
				while(i--){
					errors = errors.concat(this.arms[i].validate());
				}
			}
			return errors;
		};
		
		this.armArray= function(){
			this.sortArms();
			var a = [];
			var al = this.arms.length;
			for (var i=0; i<al;i++){
				a.push([this.arms[i].id, this.arms[i].index, this.arms[i].name, 0, this.arms[i].notes /*this.arms[i].subjectcount*/]);
			}
			return a;
		};
		
		this.getColumnGrouping=		function(showcosts, plaintext, removecolumns){
			var thisepoch = this;
			showcosts = (typeof showcosts == 'undefined')?true:showcosts;
			removecolumns = (typeof removecolumns == 'undefined')?[]:removecolumns;
			plaintext = (plaintext || false);
			//cdebug("getColumnGrouping(showcosts, plaintext,removecolumns): ",showcosts, plaintext,removecolumns);
			var armGroupRow = [];
			var cycleGroupRow = [];
	
			var procColspan = 1, /* TODO: fix the hidden 'category' column for grouping */ priceColspan=4, totalColspan=6, armColSpan=0, cycleColSpan=0;

			if (jQuery.inArray(2,removecolumns)!= -1) priceColspan -= 1;
			if (jQuery.inArray(3,removecolumns)!= -1) priceColspan -= 1;
			if (jQuery.inArray(4,removecolumns)!= -1) priceColspan -= 1;
			if (jQuery.inArray(5,removecolumns)!= -1) priceColspan -= 1;
			
			//priceColspan = priceColspan - (removecolumns.length + 1);
			
			var allcycles = this.getAllCycles();
			// I'm assuming we need to add the procedure columns and total columns to ALL groups..
			
			if (showcosts){
				procPriceColspan = procColspan + priceColspan;
			} else {
				procPriceColSpan = 1;
			}
			var phasenotes = "";
			if (Clara.BudgetBuilder.ShowNotes === true){
				if (jQuery.trim(this.notes) != ""){
					phasenotes = (plaintext)?jQuery.trim(this.notes):"<div class='budget-grid-phase-notes' onclick='if (Clara.BudgetBuilder.canEdit()) Clara.BudgetBuilder.EditActivePhaseNotes();'>"+jQuery.trim(this.notes)+"</div>";
				} else {
					phasenotes = (plaintext)?jQuery.trim(this.notes):"<div class='budget-grid-phase-notes empty-notes' onclick='if (Clara.BudgetBuilder.canEdit()) Clara.BudgetBuilder.EditActivePhaseNotes();'>Click to add notes..</div>";
				}
			}

			armGroupRow.push({header:phasenotes,colspan:procPriceColspan+1, align:'center'});

			var al = this.arms.length;
			for (var i=0; i<al;i++){
				var arm = this.arms[i];
				if (arm.hidden){
					armGroupRow.push({header:((plaintext)?'':'<div><a style="font-weight:100;" href="javascript:;" onclick="togglearm('+arm.id+');">Show</a></div>')+arm.name,colspan:1, align:'center'});
				}else{
					arm.name = (arm.name=="null")?"":arm.name;
					armColSpan = arm.getAllVisits().length;
					var armToggleText = (budget.budgetType == "basic" || thisepoch.simple)?"":"Hide";
					if (armColSpan == 0) armColSpan++;
					var armDiffClass = (arm.diff != '')?"arm-diff-"+arm.diff:"",
						armDesc = arm.name;
					armColSpan = (showcosts)?(armColSpan+totalColspan):armColSpan;
					if (Clara.BudgetBuilder.ShowNotes) armDesc += "<div class='arm-notes' id='arm-note-"+arm.id+"' onclick='Ext.Msg.alert(\"Arm Note\", jQuery(\"#arm-note-"+arm.id+"\").text());'>"+arm.notes+"</div>";
				    armGroupRow.push({header:((plaintext)?'':'<div><a class="'+armDiffClass+'" style="font-weight:100;" href="javascript:;" onclick="togglearm('+arm.id+');"><span class="'+armDiffClass+'">'+armToggleText+'</span></a></div>')+armDesc,colspan:armColSpan, align:'center'});
					
				}
			}
			
			cycleGroupRow.push({header:'',colspan:2, align:'center'});
			if (showcosts){
				cycleGroupRow.push({header:'Cost / Procedure',colspan:priceColspan, align:'center'});
			}
			

			for (var k=0; k<al;k++){
				var armk = this.arms[k];
				if (armk.hidden){
					cycleGroupRow.push({header:"",colspan:1, align:'center'});
				}else{
					armcycles = this.arms[k].cycles;
					for (var x=0; x<armcycles.length;x++){
						cycleColSpan = armcycles[x].visits.length;
						var cycleDiffClass = (armcycles[x].diff != '')?"cycle-diff-"+armk.diff:"";
						var header = "",
                            htext = "",
                            qtipText="";

						if (budget.budgetType == 'complex' && !thisepoch.simple){
							htext = (armcycles[x].name != "" && armcycles[x].name != "null")?(armcycles[x].name+": "):"";
							htext += (armcycles[x].duration)+" "+armcycles[x].durationunit+"s "+((armcycles[x].repetitions > 1)?("x "+armcycles[x].repetitions):"");
							if (plaintext && Clara.BudgetBuilder.ShowNotes){
								htext += (armcycles[x].notes || '');
							} else {
								if (Clara.BudgetBuilder.ShowNotes) htext += "<div class='cycle-notes' id='cycle-note-"+armcycles[x].id+"' onclick='Ext.Msg.alert(\"Cycle Note\", jQuery(\"#cycle-note-"+armcycles[x].id+"\").text());'>"+armcycles[x].notes+"</div>";
							}
						}

                        qtipText = "Cycle range: Day "+armcycles[x].startday+" to "+armcycles[x].endday;

						header = (plaintext)?htext:"<div ext:qtip='"+qtipText+"' class='header-cycle "+cycleDiffClass+"'>"+htext+"</div>";
						
						cycleGroupRow.push({header:header,colspan:cycleColSpan, align:'center'});
					}
					if (showcosts){
						cycleGroupRow.push({header:'Total (by charge type)',colspan:totalColspan, align:'center'});
					}
				}
			}

			//cdebug({rows: [armGroupRow, cycleGroupRow]});
			return new Ext.ux.grid.ColumnHeaderGroup({rows: [armGroupRow, cycleGroupRow]});
			
		};
		
		this.getColumnModel=	function(showcosts, plaintext,removecolumns){
			showcosts = (typeof showcosts == 'undefined')?true:showcosts;
			removecolumns = (typeof removecolumns == 'undefined')?[]:removecolumns;
			plaintext = (plaintext || false);		
			//cdebug("getColumnModel(showcosts, plaintext,removecolumns): ",showcosts, plaintext,removecolumns);
			var cols = [];
			if (jQuery.inArray(0, removecolumns) == -1) {	
				cols.push({id:'procedure', header:'Procedure', dataIndex:'procedure', width:300, resizable: true, menuDisabled: true});
			}
			if (jQuery.inArray(1, removecolumns) == -1) {	
				cols.push({id:'category', dataIndex:'category', hidden: true,groupRenderer:function(v,u,r,rowIndex,colIndex,js){
		            return (v == "zzzTotals")?"Totals":v;	// hack to force 'Totals' to bottom. Renames 'zzzTotals' to 'Totals' if 'zzzTotals' is found
		        }});
			}
			
			if (showcosts){
				var costClass = "header-"+Clara.BudgetBuilder.TotalBy;
				if (jQuery.inArray(2, removecolumns) == -1) {	
					var styledheader = (Clara.BudgetBuilder.TotalBy == 'cost')?'<a href="javascript:;" onclick="Clara.BudgetBuilder.UpdateTotals(\'cost\');"><span style="font-weight:800;">Cost</span></a>':'<a href="javascript:;" onclick="Clara.BudgetBuilder.UpdateTotals(\'cost\');"><span style="font-weight:100;">Cost</span></a>';
					cols.push({id:'maxCost', header:((plaintext)?'Cost':styledheader), dataIndex:'maxCost', width:60, menuDisabled: true});
				}
				
				if (jQuery.inArray(3, removecolumns) == -1) {	
					styledheader = (Clara.BudgetBuilder.TotalBy == 'sponsor')?'<a href="javascript:;" onclick="Clara.BudgetBuilder.UpdateTotals(\'sponsor\');"><span style="font-weight:800;">Sponsor</span></a>':'<a href="javascript:;" onclick="Clara.BudgetBuilder.UpdateTotals(\'sponsor\');"><span style="font-weight:100;">Sponsor</span></a>';
					cols.push({id:'sponsorCost', header:((plaintext)?'Sponsor':styledheader), dataIndex:'sponsorCost', width:60, menuDisabled: true});
				}
				
				if (jQuery.inArray(4, removecolumns) == -1) {	
					styledheader = (Clara.BudgetBuilder.TotalBy == 'price')?'<a href="javascript:;" onclick="Clara.BudgetBuilder.UpdateTotals(\'price\');"><span style="font-weight:800;">Price</span></a>':'<a href="javascript:;" onclick="Clara.BudgetBuilder.UpdateTotals(\'price\');"><span style="font-weight:100;">Price</span></a>';
					cols.push({id:'priceToCharge', header:((plaintext)?'Price':styledheader), dataIndex:'priceToCharge', width:60, menuDisabled: true});
				}
				
				if (jQuery.inArray(5, removecolumns) == -1) {	
					cols.push({id:'residual', header:'Residual', dataIndex:'residual', width:60, menuDisabled: true});
				}
	
			}
			var al = this.arms.length;
			for (var i=0; i<al;i++){
				
				var arm = this.arms[i];
				var cssColor = getCssForColumn(i,0);
				if (arm.hidden) {
					cols.push({id:'ah'+arm.id,dataIndex:'ah'+arm.id,header:'', width:100, menuDisabled: true,css:cssColor});
				}
				else {
					var armvisits = this.arms[i].getAllVisits();
					if (armvisits.length == 0){
						cols.push({id:'vnull',dataIndex:'vnull',header:"", width:80, menuDisabled: true,css:getCssForColumn(i,j, false)});
					}else {
						var avl = armvisits.length;
						for (var j=0; j<avl;j++){
							var visitDiffClass = (armvisits[j].diff != '')?"visit-diff-"+armvisits[j].diff:"";
							cols.push({id:'v'+armvisits[j].id,dataIndex:'v'+armvisits[j].id,header:((armvisits[j].name && armvisits[j].name != "")?((plaintext)?armvisits[j].name:((Clara.BudgetBuilder.canEdit()?"<a class='"+visitDiffClass+"' href='javascript:;' ext:qtip='"+Encoder.htmlEncode(armvisits[j].name)+"' onclick='Clara.BudgetBuilder.EditVisitColumn("+armvisits[j].id+");'>"+armvisits[j].name+"</a>":"<span class='"+visitDiffClass+"'>"+armvisits[j].name+"</span>"))):"<span class='"+visitDiffClass+"'>"+armvisits[j].cycleindex+"</span>" ), width:80, menuDisabled: true,css:getCssForColumn(i,j, false)});
						}
					}
					if (showcosts){
						cols.push({id:'totalR-'+arm.id, header:'R', dataIndex:'totalR-'+arm.id, width:80, menuDisabled: true,css:getCssForColumn(i,0, true)});
						cols.push({id:'totalC-'+arm.id, header:'C', dataIndex:'totalC-'+arm.id, width:80, menuDisabled: true,css:getCssForColumn(i,1, true)});
						cols.push({id:'totalI-'+arm.id, header:'I', dataIndex:'totalI-'+arm.id, width:80, menuDisabled: true,css:getCssForColumn(i,2, true)});
						cols.push({id:'totalRNS-'+arm.id, header:'RNS', dataIndex:'totalRNS-'+arm.id, width:80, menuDisabled: true,css:getCssForColumn(i,3, true)});
						cols.push({id:'totalCNMS-'+arm.id, header:'CNMS', dataIndex:'totalCNMS-'+arm.id, width:80, menuDisabled: true,css:getCssForColumn(i,4, true)});
						cols.push({id:'totalCL-'+arm.id, header:'CL', dataIndex:'totalCL-'+arm.id, width:80, menuDisabled: true,css:getCssForColumn(i,5, true)});
					}
				}
			}
			//cdebug(cols);
			return new Ext.grid.ColumnModel(cols);
		};
		
		this.getProcedureVisitArray= function(procedure, copyValues){		// for the "add procedure" window: visits
			// hideEmptyVisits: true to hide visits in the epoch that have no saved value
			var a=[], al=this.arms.length,acl=0,cvl=0,vpl=0;
			var el=[];

			copyValues = (copyValues || false);
			
			for (var i=0; i<al;i++){
				
				var arm = this.arms[i];
				acl = arm.cycles.length;
				arm.sortCycles();
				
				for (var j=0;j<acl; j++){
					var cycle = arm.cycles[j];
					cvl = arm.cycles[j].visits.length;
					for (var k=0;k<cvl;k++){
						var visit = cycle.visits[k];
						var procvisitfound = false;
						var pvType="";
						var pvReps="";
						//var pvSubjCnt=0;					
						
						if (procedure && visit.visitprocedures) {
							vpl = visit.visitprocedures.length;
							for (var v=0;v<vpl;v++)
							{
								if (visit.visitprocedures[v].procedureid == procedure.id){
									procvisitfound = true;
									pvType = visit.visitprocedures[v].type;
									pvReps = visit.visitprocedures[v].repetitions;
									//pvSubjCnt=visit.subjectcount;
								}
								
							}
						}

						el.push(arm.id);
						el.push(cycle.id);
						el.push(visit.id);
						el.push(arm.name);
						el.push(cycle.name+": (Day "+visit.cycleindex+") "+visit.name);
						el.push((!procvisitfound || !copyValues)?"":pvType);
						el.push((!procvisitfound || !copyValues)?"":pvReps);
						//el.push(pvSubjCnt);
						a.push(el);
						el = [];
						
					}
				}
				
			}

			return a;
		};
		
		this.getStore= function(showcosts){								// Returns Ext.data.ArrayStore for use on the GridPanel
			showcosts = (typeof showcosts == 'undefined' || showcosts == null)?true:showcosts;
			var fields = [{name:'procedure'},{name:'procid',type:'int'},{name:'category'}];
			if (showcosts){
				fields.push({name:'maxCost'});
				fields.push({name:'sponsorCost'});
				fields.push({name:'priceToCharge'});
				fields.push({name:'residual'});
			}
			var al = this.arms.length, avl=0;
			for (var i=0; i<al;i++){
				var emptyVisitCount = 0;
				var arm = this.arms[i];
				arm.sortCycles();
				if (arm.hidden){
					fields.push({name:'ah'+arm.id});
				}else{
					var armvisits = this.arms[i].getAllVisits();
					avl = armvisits.length;
					for (var j=0; j<avl;j++){
						fields.push({name:'v'+armvisits[j].id});
					}
					if (showcosts){
						fields.push({name:'totalR-'+this.arms[i].id});
						fields.push({name:'totalC-'+this.arms[i].id});
						fields.push({name:'totalI-'+this.arms[i].id});
						fields.push({name:'totalRNS-'+this.arms[i].id});
						fields.push({name:'totalCNMS-'+this.arms[i].id});
						fields.push({name:'totalCL-'+this.arms[i].id});
					}
				}
			}
			
			
			return new Ext.data.GroupingStore({
				reader: new Ext.data.ArrayReader({},fields),
				autoLoad:false,
				sortInfo:{field:'procid', direction:'ASC'},
				groupField:'category'
			});
			
			//return new Ext.data.ArrayStore({fields:fields, autoLoad:false});
		};
		
		this.getArray= function(showcosts, plaintext, includeMetadata){
			showcosts = (typeof showcosts == 'undefined' || showcosts == null)?true:showcosts;
			plaintext = (plaintext || false);
			includeMetadata = (includeMetadata || false);
			
			var celltotalprefix = (plaintext)?"":"<div class='cell-right'>";
			var celltotalsuffix = (plaintext)?"":"</div>";
			
			var a = [], pl = this.procedures.length;
			for (var i=0; i<pl;i++){
				var el = [];
				var hproc = this.procedures[i].getHighestCostSubProcedure();
				var proc = this.procedures[i];

				var procCosts = {
					cost:(proc.cost.misc > 0)?proc.cost.misc:proc.cost.getTotal(),//(parseFloat(proc.hosp.cost)+parseFloat(proc.phys.cost)),
					sponsor:(proc.cost.sponsor),
					price:(proc.cost.price),
					residual:(proc.type == 'outside')?0:(proc.cost.getResidual())
				};
				
				var notes = (plaintext || (jQuery.trim(proc.notes) == "" && jQuery.trim(proc.clinicalNotes) == "") )?"":"<a id='procnotelink-"+proc.id+"' href='javascript:;' onclick='Clara.BudgetBuilder.ShowProcedureNotes("+proc.id+");'><img style='float:left;margin-right:8px;' src='"+appContext+"/static/images/icn/sticky-note.png' border='0'/></a>";
				var descClass = (proc.subprocedures.length > 0 )?"row-has-subprocedures":"";
				
				var subproc = "";
				var subprocIcon = "";

				var procDiffClass = (proc.diff !='')?"vp-diff-"+proc.diff:"";

				
				if(!plaintext && proc.subprocedures.length > 0){
					if(proc.isOfficeVisit()){
						subprocIcon = "report_link.png";
					}else{
						subprocIcon = "link.png";
					}
					
					subproc = "<a href='javascript:;' onclick='Clara.BudgetBuilder.ShowSubprocedures("+proc.id+");'><img style='float:left;margin-right:8px;' src='"+appContext+"/static/images/icons/" + subprocIcon + "' border='0'/></a>";
				}
				
				var condProc = "",
					altProc = "";
				
				if(!plaintext && proc.conditional == true){
					condProc = "<a href='javascript:;' onclick='alert(\"This is a conditional procedure.\");'><img style='float:left;margin-right:8px;' src='"+appContext+"/static/images/icn/asterisk.png' border='0'/></a>";
				}
				
				if(!plaintext && proc.alternative == true){
					altProc = "<a href='javascript:;' onclick='alert(\"This is an alternative procedure.\");'><img style='float:left;margin-right:8px;' src='"+appContext+"/static/images/icn/node-select-next.png' border='0'/></a>";
				}
				
				var procDiffClass = (proc.diff != '')?"proc-diff-"+proc.diff:"";
				var desc = "<div class='procrow-desc' id='procrow-desc-"+proc.id+"'>";
				desc += Clara.BudgetBuilder.canEdit()?("<a href='javascript:;' onclick='Clara.BudgetBuilder.ConfirmRemoveProcedure("+proc.id+");'><img style='float:left;margin-right:8px;' src='"+appContext+"/static/images/icn/minus-circle.png' border='0'/></a>"+notes+subproc+condProc+altProc+"<span class='"+descClass+" "+procDiffClass+"'><a href='javascript:;' class='"+procDiffClass+"' style='line-height:17px;' onclick='Clara.BudgetBuilder.EditActiveEpochProcedure("+proc.id+");'>"+proc.getDescription()+"</a></span>"):(notes+subproc+condProc+altProc+"<span class='"+descClass+" "+procDiffClass+"'>"+proc.getDescription()+"</span>");
				desc += "</div>";
				var pdesc = (plaintext)?proc.getDescription():desc;
				
				if (includeMetadata == true){
					el.push({cat:proc.category, proc:proc.id, arm:k, type:'proc', value:pdesc});
					el.push({cat:proc.category, proc:proc.id, arm:k, type:'category', value:proc.category});
				} else {
		
					el.push(pdesc);
					el.push(proc.id);
					el.push(proc.category);
				
				}
				

				
				if (showcosts){
					
					if (includeMetadata == true){
						el.push({cat:proc.category, proc:proc.id, arm:k, type:'cost', value:Ext.util.Format.usMoney(procCosts.cost)});
						el.push({cat:proc.category, proc:proc.id, arm:k, type:'cost', value:Ext.util.Format.usMoney(procCosts.sponsor)});
						el.push({cat:proc.category, proc:proc.id, arm:k, type:'cost', value:Ext.util.Format.usMoney(procCosts.price)});
						el.push({cat:proc.category, proc:proc.id, arm:k, type:'cost', value:((proc.type == 'outside')?'-':Ext.util.Format.usMoney(procCosts.residual))});
					} else {
						el.push("<span class='"+procDiffClass+"'>"+Ext.util.Format.usMoney(procCosts.cost)+"</span>");
						el.push("<span class='"+procDiffClass+"'>"+Ext.util.Format.usMoney(procCosts.sponsor)+"</span>");
						el.push("<span class='"+procDiffClass+"'>"+Ext.util.Format.usMoney(procCosts.price)+"</span>");
						el.push((proc.type == 'outside')?'-':("<span class='"+procDiffClass+"'>"+Ext.util.Format.usMoney(procCosts.residual))+"</span>");
					}
				}
				var al = this.arms.length;
				for (var k=0; k<al;k++){
					var arm = this.arms[k];
					
					var armTotals = {
							R:0,
							C:0,
							I:0,
							RNS:0,
							CNMS:0,
							CL:0
						};
					
					if (arm.hidden){
						if (includeMetadata){
							el.push({cat:proc.category, proc:proc.id, arm:k, type:'arm', value:''});
						} else el.push("");
					}else{
						var v = this.arms[k].getAllVisits();
						var reps = 1, vl = v.length;
						for (var j=0; j<vl;j++){
							
							var vtext = "-";
							var vpDiffClass = "";
							var visitProcedure = v[j].getVisitProcedure(proc);
							if (visitProcedure){
								reps = visitProcedure.repetitions;
								vtext = visitProcedure.type+((reps > 1)?" ("+reps+")":"");
								// check parent cycle for repeat
								if (v[j].parentCycle && !v[j].parentCycle.repeatforever){
									reps = reps * parseInt(v[j].parentCycle.repetitions);
								}
								if (proc.diff !=''){
									vpDiffClass = "vp-diff-"+proc.diff;
								}
								else if (v[j].diff !=''){
									vpDiffClass = "vp-diff-"+v[j].diff;
								} else if (visitProcedure.diff !=''){
									vpDiffClass = "vp-diff-"+visitProcedure.diff;
								}
								
								if (visitProcedure.diff != "D") armTotals[visitProcedure.type.toUpperCase()] = armTotals[visitProcedure.type.toUpperCase()] + (procCosts[Clara.BudgetBuilder.TotalBy] * reps);
							}
							if (includeMetadata){
								el.push({cat:proc.category, proc:proc.id, arm:k, type:'cell', value:vtext});
							}
							else el.push((plaintext)?vtext:("<div ext:qtip='"+proc.getDescription()+"' class='cell-center " +vpDiffClass+ (v[j].hasOfficeVisitItemSubprocedures(proc)?' has-office-visit-item':'') +  "' id='vp-"+v[j].id+"-"+proc.id+"'>"+vtext+"</div>"));
						}
						if (showcosts){
							//TODO: Calculate totals.

							if (includeMetadata){
								el.push({cat:proc.category, proc:proc.id, arm:k, type:'cell-total', value:(armTotals.R!=0?celltotalprefix+Ext.util.Format.usMoney(armTotals.R)+celltotalsuffix:"")});
								el.push({cat:proc.category, proc:proc.id, arm:k, type:'cell-total', value:(armTotals.C!=0?celltotalprefix+Ext.util.Format.usMoney(armTotals.C)+celltotalsuffix:"")});
								el.push({cat:proc.category, proc:proc.id, arm:k, type:'cell-total', value:(armTotals.I!=0?celltotalprefix+Ext.util.Format.usMoney(armTotals.I)+celltotalsuffix:"")});
								el.push({cat:proc.category, proc:proc.id, arm:k, type:'cell-total', value:(armTotals.RNS!=0?celltotalprefix+Ext.util.Format.usMoney(armTotals.RNS)+celltotalsuffix:"")});
								el.push({cat:proc.category, proc:proc.id, arm:k, type:'cell-total', value:(armTotals.CNMS!=0?celltotalprefix+Ext.util.Format.usMoney(armTotals.CNMS)+celltotalsuffix:"")});
								el.push({cat:proc.category, proc:proc.id, arm:k, type:'cell-total', value:(armTotals.CL!=0?celltotalprefix+Ext.util.Format.usMoney(armTotals.CL)+celltotalsuffix:"")});
								}
							else {
								el.push(armTotals.R!=0?celltotalprefix+"<span class='"+procDiffClass+"'>"+Ext.util.Format.usMoney(armTotals.R)+"</span>"+celltotalsuffix:"");
								el.push(armTotals.C!=0?celltotalprefix+"<span class='"+procDiffClass+"'>"+Ext.util.Format.usMoney(armTotals.C)+"</span>"+celltotalsuffix:"");
								el.push(armTotals.I!=0?celltotalprefix+"<span class='"+procDiffClass+"'>"+Ext.util.Format.usMoney(armTotals.I)+"</span>"+celltotalsuffix:"");
								el.push(armTotals.RNS!=0?celltotalprefix+"<span class='"+procDiffClass+"'>"+Ext.util.Format.usMoney(armTotals.RNS)+"</span>"+celltotalsuffix:"");
								el.push(armTotals.CNMS!=0?celltotalprefix+"<span class='"+procDiffClass+"'>"+Ext.util.Format.usMoney(armTotals.CNMS)+"</span>"+celltotalsuffix:"");
								el.push(armTotals.CL!=0?celltotalprefix+"<span class='"+procDiffClass+"'>"+Ext.util.Format.usMoney(armTotals.CL)+"</span>"+celltotalsuffix:"");
							}
						}
					}
				}

				a.push(el);
			}
			
			// end of procedure list, now push total rows
			// ROW 1: Per Subject Direct
			
			if (showcosts){
				var el=[];
				var el2=[];
				var el3=[];
				var el4=[];
				var el5=[];
				
				el.push((plaintext)?"Per Subject Direct":"<div id='total-001' class='cell-right'>Per Subject Direct</div>");
				el2.push((plaintext)?"Facilities and Admin (FA): "+budget.FA+"%":"<div id='total-002' class='cell-right' onclick='if (Clara.BudgetBuilder.canEdit()) Clara.BudgetBuilder.SetFA();'>Facilities and Admin (F&A): "+budget.FA+"%</div>");
				el3.push((plaintext)?"Per Subject Total":"<div id='total-003' class='cell-right'>Per Subject Total</div>");
				el4.push((plaintext)?"# Subjects":"<div id='total-004' class='cell-right' onclick='if (Clara.BudgetBuilder.canEdit()) Clara.BudgetBuilder.SetVisibleEpochSubjectCount();'># Subjects</div>");
				el5.push((plaintext)?"Final Total":"<div id='total-005' class='cell-right'>Final Total</div>");
				el.push(0);el2.push(0);el3.push(0);el4.push(0);el5.push(0);	// dummy "procedureid" for totals
				el.push("zzzTotals");el2.push("zzzTotals");el3.push("zzzTotals");el4.push("zzzTotals");el5.push("zzzTotals");
				el.push("");el.push("");el.push("");el.push("");
				el2.push("");el2.push("");el2.push("");el2.push("");
				el3.push("");el3.push("");el3.push("");el3.push("");
				el4.push("");el4.push("");el4.push("");el4.push("");
				el5.push("");el5.push("");el5.push("");el5.push("");
				
				for (var k=0; k<this.arms.length;k++){
					var arm = this.arms[k];
					
					if (arm.hidden){
						el.push("");el2.push("");el3.push("");el4.push("");el5.push("");
					}else{
						var armTotals = arm.calculateTotal();
						
						var v = this.arms[k].getAllVisits();
						var vl = v.length;
						for (var j=0; j<vl;j++){
							var psdtotal = v[j].calculateTotal();
							el.push(celltotalprefix+Ext.util.Format.usMoney(psdtotal)+celltotalsuffix);
							el2.push(celltotalprefix+Ext.util.Format.usMoney(psdtotal * (budget.FA / 100))+celltotalsuffix);
							el3.push(celltotalprefix+Ext.util.Format.usMoney(psdtotal + (psdtotal * (budget.FA / 100)))+celltotalsuffix);
							el4.push(celltotalprefix+v[j].subjectcount+celltotalsuffix);
							el5.push(celltotalprefix+Ext.util.Format.usMoney(v[j].subjectcount * (psdtotal + (psdtotal * (budget.FA / 100))))+celltotalsuffix);
						}
						
						
						
						
						// Calculate "Arm Total" Totals
						
						el.push(celltotalprefix+((armTotals.R==0)?"":Ext.util.Format.usMoney(armTotals.R))+celltotalsuffix);
						el.push(celltotalprefix+((armTotals.C==0)?"":Ext.util.Format.usMoney(armTotals.C))+celltotalsuffix);
						el.push(celltotalprefix+((armTotals.I==0)?"":Ext.util.Format.usMoney(armTotals.I))+celltotalsuffix);
						el.push(celltotalprefix+((armTotals.RNS==0)?"":Ext.util.Format.usMoney(armTotals.RNS))+celltotalsuffix);
						el.push(celltotalprefix+((armTotals.CNMS==0)?"":Ext.util.Format.usMoney(armTotals.CNMS))+celltotalsuffix);
						el.push(celltotalprefix+((armTotals.CL==0)?"":Ext.util.Format.usMoney(armTotals.CL))+celltotalsuffix);

						el2.push(celltotalprefix+((armTotals.R==0)?"":Ext.util.Format.usMoney(armTotals.R * (budget.FA / 100)))+celltotalsuffix);
						el2.push(celltotalprefix+((armTotals.C==0)?"":Ext.util.Format.usMoney(0))+celltotalsuffix);
						el2.push(celltotalprefix+((armTotals.I==0)?"":Ext.util.Format.usMoney(armTotals.I * (budget.FA / 100)))+celltotalsuffix);
						el2.push(celltotalprefix+((armTotals.RNS==0)?"":Ext.util.Format.usMoney(0))+celltotalsuffix);
						el2.push(celltotalprefix+((armTotals.CNMS==0)?"":Ext.util.Format.usMoney(0))+celltotalsuffix);
						el2.push(celltotalprefix+((armTotals.CL==0)?"":Ext.util.Format.usMoney(0))+celltotalsuffix);

						el3.push(celltotalprefix+((armTotals.R==0)?"":Ext.util.Format.usMoney(armTotals.R+armTotals.R * (budget.FA / 100)))+celltotalsuffix);
						el3.push(celltotalprefix+((armTotals.C==0)?"":Ext.util.Format.usMoney(armTotals.C))+celltotalsuffix);
						el3.push(celltotalprefix+((armTotals.I==0)?"":Ext.util.Format.usMoney(armTotals.I+armTotals.I * (budget.FA / 100)))+celltotalsuffix);
						el3.push(celltotalprefix+((armTotals.RNS==0)?"":Ext.util.Format.usMoney(armTotals.RNS))+celltotalsuffix);
						el3.push(celltotalprefix+((armTotals.CNMS==0)?"":Ext.util.Format.usMoney(armTotals.CNMS))+celltotalsuffix);
						el3.push(celltotalprefix+((armTotals.CL==0)?"":Ext.util.Format.usMoney(armTotals.CL))+celltotalsuffix);
						
						el4.push("");el4.push("");el4.push("");el4.push("");el4.push("");el4.push("");
					
						
						var armTotalObject = arm.getTotals();
						clog("armTotalObject",armTotalObject);
						
						el5.push(celltotalprefix+((armTotalObject.R==0)?"":Ext.util.Format.usMoney((armTotalObject.R+(armTotalObject.R * (budget.FA / 100)))))+celltotalsuffix);
						el5.push(celltotalprefix+((armTotalObject.C==0)?"":Ext.util.Format.usMoney((armTotalObject.C)))+celltotalsuffix);
						el5.push(celltotalprefix+((armTotalObject.I==0)?"":Ext.util.Format.usMoney((armTotalObject.I+(armTotalObject.I * (budget.FA / 100)))))+celltotalsuffix);
						el5.push(celltotalprefix+((armTotalObject.RNS==0)?"":Ext.util.Format.usMoney((armTotalObject.RNS)))+celltotalsuffix);
						el5.push(celltotalprefix+((armTotalObject.CNMS==0)?"":Ext.util.Format.usMoney((armTotalObject.CNMS)))+celltotalsuffix);
						el5.push(celltotalprefix+((armTotalObject.CL==0)?"":Ext.util.Format.usMoney((armTotalObject.CL)))+celltotalsuffix);
						
						
					}
				}

					a.push(el);a.push(el2);a.push(el3);a.push(el4);a.push(el5);
				
			}
			//cdebug("[getarray]getarray array to return:",a);
			return a;
		};
		
		this.getTotalRow= function(total, key){
			var el = [];
			el.push("<div style='width:100%;text-align:right;'>"+key+"</div>");
			el.push("Totals");
			el.push("");
			el.push("");
			el.push("");
			el.push("");
		};
		
		this.getAllCycles= function(){
			var cycles = [];
			for (var i=0; i<this.arms.length;i++){
				cycles = cycles.concat(this.arms[i].cycles);
			}
			return cycles;
		};
		
		this.getAllVisits= function(){
			var visits = [];
			for (var i=0; i<this.arms.length;i++){
				visits = visits.concat(this.arms[i].getAllVisits());
			}
			return visits;
		};
		
		this.toXML= function(){
			var xml = "<epoch id='"+this.id+"' simple='"+this.simple+"' conditional='"+this.conditional+"' name='"+Encoder.htmlEncode(this.name)+"' index='"+this.index+"'>";
			xml = xml + "<notes>" + Encoder.htmlEncode(this.notes) + "</notes>";
			xml = xml + "<arms>";
			
			this.sortArms();

			var al = this.arms.length;
			for (var i=0; i<al;i++){
				this.arms[i].index=i;
				xml = xml + this.arms[i].toXML();
			}
			xml = xml + "</arms>";
			xml = xml + "<procedures>";
			var pl = this.procedures.length;
			for (var i=0; i<pl;i++){
				xml = xml + this.procedures[i].toXML();
			}
			xml = xml + "</procedures>";
			xml = xml + "</epoch>";
			return xml;
		};
		
		this.getArms= function(){
			return this.arms;
		};
		       
		this.addArm= function(arm){
			this.arms.push(arm);
		};
		
		this.updateArm= function(arm){
			var al = this.arms.length;
			for (var i=0; i<al;i++){
				if (this.arms[i].id == arm.id){
					this.arms.splice(i,1,arm);
				}
			}
		};

};

function togglearm(id){
	var arm = budget.getArm(id);
	arm.hidden = !arm.hidden;
	Clara.BudgetBuilder.MessageBus.fireEvent('epochcontentupdated', Ext.getCmp('budget-tabpanel').activeEpoch);
}

function getCssForColumn(arm, visit, isTotalCol){
	var amod = (arm % 3);
	var oddEven = (visit%2);
	var r,g,b;
	if (amod == 0){
		b = 255-(10*oddEven);
		g = (isTotalCol?245:255)-(50*(isTotalCol?0:1))-(10*oddEven);
		r = (isTotalCol?245:255)-(50*(isTotalCol?0:1))-(10*oddEven);
	} else if (amod == 1){
		r = (isTotalCol?245:255)-(50*(isTotalCol?0:amod))-(10*oddEven);
		g = 255-(10*oddEven);
		b = (isTotalCol?245:255)-(50*(isTotalCol?0:amod))-(10*oddEven);
	} else {
		r = 255-(10*oddEven);
		g = (isTotalCol?245:255)-(50*(isTotalCol?0:amod))-(10*oddEven);
		b = (isTotalCol?245:255)-(50*(isTotalCol?0:amod))-(10*oddEven);
	}
	
	var decColor = r + 256 * g + 65536 * b;
    htmlcolor = decColor.toString(16);
    var css = "background:#"+htmlcolor+"";
    if (isTotalCol){
    	if (visit == 0) css+= " url('../../../../../static/images/budget_totalcell.png') repeat-y left top";
    	if (visit == 5) css+= " url('../../../../../static/images/budget_totalcell_end.png') repeat-y right top";
    }
	return css+";";
}