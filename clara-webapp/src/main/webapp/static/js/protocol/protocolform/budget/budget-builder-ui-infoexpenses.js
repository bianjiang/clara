Ext.ns('Clara.BudgetBuilder');

Clara.BudgetBuilder.SyncExternalExpenses = function(){
	var url = appContext+"/ajax/protocols/"+claraInstance.id+"/protocol-forms/"+claraInstance.form.id+"/budgets/get-external-expenses";
	Clara.BudgetBuilder.MessageBus.fireEvent('beforeloadexternalexpenses', null);
	jQuery.ajax({
		  type: 'GET',
		  async:false,
		  url: url,
		  success: function(data){
			  //cdebug(data);
			  var exps = [];		// expenses
				jQuery(data).find("expenses").find("expense").each(function(){
					var exp = new Clara.BudgetBuilder.Expense({
						id:			budget.newId(), 	//parseFloat(jQuery(this).attr('id')),
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
					exps.push(exp);
				});
			  
			  Clara.BudgetBuilder.MessageBus.fireEvent('afterloadexternalexpenses', null);
			  var budgetChanged = false;
			  var expenseFound = false;
			  if (budget){
				  
				  for (var i=0;i<exps.length;i++){
					  var ee = exps[i];
					  for (var j=0;j<budget.expenses.length;j++){
						  var be = budget.expenses[j];
						  if (be.external && be.type == ee.type && be.subtype == ee.subtype && be.description == ee.description) {
							  expenseFound = true;
							  budgetChanged = budgetChanged || (be.count != ee.count || be.cost != ee.cost || be.fa != ee.fa || be.faenabled != ee.faenabled);
							  budget.expenses.splice(j,1,ee);
						  }
					  }
					  if (expenseFound == false){
						  //cdebug("ADDED EXT EXPENSE");
						  budget.expenses.push(ee);
						  budgetChanged = true;
					  }
				  }
				  
				  if (budgetChanged){
					  
					  budget.save();

				  }
				  
			  }
			  
		  },
		  error: function(){
			  Clara.BudgetBuilder.MessageBus.fireEvent('onloadexternalexpenseserror', null);
			  //return null;
		  },
		  dataType: 'xml'
	});
};

Clara.BudgetBuilder.ExpenseStore = new Ext.data.GroupingStore({
	reader: new Ext.data.ArrayReader({},[{name:'id'},
	                                     {name:'diff'},
	                                     {name:'type'},
	                                     {name:'subtype'},
	                	              {name:'description'},
	                	              {name:'notes'},
	                	              {name:'fa'},
	                	              {name:'faenabled'},
	                	              {name:'external'},
	                	              {name:'count'},
	                	              {name:'cost'}]),
	autoLoad:false,
	sortInfo:{field:'subtype', direction:'ASC'},
	groupField:'subtype'
});

Clara.BudgetBuilder.InvoicableStore = new Ext.data.GroupingStore({
	reader: new Ext.data.ArrayReader({},[{name:'id'},
	                                     {name:'diff'},
	                                     {name:'type'},
	                                     {name:'subtype'},
	                	              {name:'description'},
	                	              {name:'notes'},
	                	              {name:'fa'},
	                	              {name:'faenabled'},
	                	              {name:'external'},
	                	              {name:'count'},
	                	              {name:'cost'},
	                                     ]),
	autoLoad:false,
	//sortInfo:{field:'subtype', direction:'ASC'},
	remoteSort:true,
	groupField:'subtype'
});


Clara.BudgetBuilder.InitialExpenseWindow = Ext.extend(Ext.Window, {
	id: 'winExpense',
    editing:false,
    readOnly:false,
    expenseType:'Administrative',
    expense:{},
    width: 625,
    height: 250,
    layout: 'form',
    padding:6,
	constructor:function(config){		
		Clara.BudgetBuilder.InitialExpenseWindow.superclass.constructor.call(this, config);
    },
	initComponent: function() {
		var t = this;
		this.title = (this.editing)?this.expense.subtype+" Expense":"Add "+this.expenseType+" Expense";
		var config = {
				buttons:[{
					id:'btnCloseExpWindow',
					text:'Save',
					disabled:(t.editing)?(t.readOnly || (t.expense.external && (Clara.BudgetBuilder.AllowExternalCostEditing == false))):false, 
					handler: function(){
					
						if (Ext.getCmp("fldExpense").validate() && Ext.getCmp("fldAmount").validate()){
							
							var e = new Clara.BudgetBuilder.Expense({
								fa:Ext.getCmp("fldFA").getValue(), //budget.initialExpenseFA,
								count:1,
								cost:Ext.getCmp("fldAmount").getValue(),
								type:'Initial Cost',
								subtype:(t.editing)?t.expense.subtype:t.expenseType,
								description:Ext.getCmp("fldExpense").getValue(),
								notes:Ext.getCmp("fldNotes").getValue()
							});
												
							if (!t.editing){
								Clara.BudgetBuilder.SaveAction = "Added expense";
								//cdebug("Saving expense");
								//cdebug(e);
								budget.addExpense(e);
							} else {
								e.id = t.expense.id;
								Clara.BudgetBuilder.SaveAction = "Edited expense";
								//cdebug("Updating expense");
								//cdebug(e);
								budget.updateExpense(e);
							}
							Clara.BudgetBuilder.MessageBus.fireEvent('budgetinfoupdated', e);
							budget.save();
							t.close();
						
						}
					}
				}],
				items: [{
	                xtype: 'textfield',
	                allowBlank: false,
	                fieldLabel: 'Expense',
				    anchor: '100%',
	                id:'fldExpense',
	                value:(this.editing)?this.expense.description:null
	            },
	            
	        
	            {
	                xtype: 'numberfield',
	                fieldLabel: 'Amount',
				    anchor: '100%',
	                allowBlank: false,
	                value:(this.editing)?this.expense.cost:null,
	                id:'fldAmount'
	            },
	            {
	                xtype: 'numberfield',
	                fieldLabel: 'F&A, in percent',
				    anchor: '100%',
	                allowBlank: false,
	                value:(this.editing)?this.expense.fa:budget.initialExpenseFA,
	                id:'fldFA'
	            },
	            
	            {
	                xtype: 'textarea',
	                id:'fldNotes',
	                fieldLabel: 'Notes',
				    anchor: '100%',
	                value:(this.editing)?this.expense.notes:null
	            }]
		};
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		Clara.BudgetBuilder.InitialExpenseWindow.superclass.initComponent.apply(this, arguments);
	}
});
Ext.reg('claraaddInitialExpenseWindow', Clara.BudgetBuilder.InitialExpenseWindow);


Clara.BudgetBuilder.InvoicableWindow = Ext.extend(Ext.Window, {
	id: 'winInvoicable',
    editing:false,
    readOnly:false,
    expenseType:'',
    expenseFA:true,
    faenabled:false,
    expense:{},
    width: 625,
    height: 430,
    layout: 'absolute',
	constructor:function(config){		
		Clara.BudgetBuilder.InvoicableWindow.superclass.constructor.call(this, config);
    },
	initComponent: function() {
		var t = this;
		this.title = (this.editing)?"Edit '"+this.expense.subtype+"' Invoicable":"Add '"+this.expenseType+"' Invoicable";
		var config = {
				buttons:[{
					id:'btnCloseInvWindow',
					text:'Save',
					disabled:(t.editing)?(t.readOnly 
							|| (t.expense.external && Clara.BudgetBuilder.AllowExternalCostEditing == false && t.expense.subtype != 'IRB Fee')
					):false,
					handler: function(){
					
						if (Ext.getCmp("fldInvoicable").validate() && Ext.getCmp("fldAmount").validate() && Ext.getCmp("fldCount").validate() && Ext.getCmp("fldFA").validate()){
							
							var e = new Clara.BudgetBuilder.Expense({
								fa: Ext.getCmp("fldFA").getValue(),
								faenabled:(Ext.getCmp('winInvoicable').editing)?Ext.getCmp('winInvoicable').expense.faenabled:Ext.getCmp('winInvoicable').faenabled,
								count:Ext.getCmp("fldCount").getValue(),
								cost:Ext.getCmp("fldAmount").getValue(),
								type:'Invoicable',
								subtype:(Ext.getCmp('winInvoicable').editing)?Ext.getCmp('winInvoicable').expense.subtype:Ext.getCmp('winInvoicable').expenseType,
								description:Ext.getCmp("fldInvoicable").getValue(),
								notes:Ext.getCmp("fldNotes").getValue()
							});
												
							if (!Ext.getCmp("winInvoicable").editing){
								Clara.BudgetBuilder.SaveAction = "Added invoicable";
								budget.addExpense(e);
							} else {
								e.id = Ext.getCmp('winInvoicable').expense.id;
								Clara.BudgetBuilder.SaveAction = "Edited invoicable";
								budget.updateExpense(e);
							}
							Clara.BudgetBuilder.MessageBus.fireEvent('budgetinfoupdated', e);
							budget.save();
							Ext.getCmp('winInvoicable').close();
						
						}
					}
				}],
				items: [{
	                xtype: 'textfield',
	                x: 70,
	                y: 10,
	                width: 530,
	                allowBlank: false,
	                readOnly: ((this.editing &&  this.expense.subtype == "IRB Fee"))?true:false,
	                id: 'fldInvoicable',
	                value:((this.editing && this.expense.subtype == 'Other') || this.expenseType == 'Other')?(this.expense.description || ''):(this.expense.subtype || this.expenseType)
	            },
	            {
	                xtype: 'label',
	                text: 'Type',
	                x: 10,
	                y: 10,
	                style: 'font-size:14px;text-align:right;',
	                width: 50
	            },
	            {
                xtype: 'label',
                text: '# occurrences',
                x: 160,
                y: 40,
                style: 'font-size:14px;'
            },
            {
                xtype: 'label',
                text: 'F&A:',
                x: 320,
                y: 40,
                style: 'font-size:14px;text-align:right;',
                width: 30,
                id: 'lblFA'
            },
            {
                value: (this.editing)?this.expense.fa:this.expenseFA,
                readOnly: (this.editing && this.expense.subtype == 'IRB Fee')?true:false,
                xtype: 'numberfield',
                allowBlank: false,
                x: 360,
                y: 40,
                width: 50,
                id: 'fldFA'
            },
            {
                xtype: 'label',
                text: 'Notes',
                x: 10,
                y: 70,
                style: 'font-size:14px;text-align:right;',
                width: 50
            },
            {
                xtype: 'label',
                text: 'Amount',
                x: 10,
                y: 40,
                style: 'font-size:14px;'
            },
            {
                xtype: 'numberfield',
                x: 70,
                y: 40,
                allowBlank: false,
                width: 70,
                readOnly: (this.editing && this.expense.subtype == 'IRB Fee')?true:false,
                id: 'fldAmount',
                value:(this.editing)?this.expense.cost:null
            },
            {
                xtype: 'numberfield',
                x: 260,
                y: 40,
                width: 40,
                allowBlank: false,
                id: 'fldCount',
                value:(this.editing)?this.expense.count:1
            },
            {
                xtype: 'textarea',
                x: 70,
                y: 70,
                width: 530,
                height:280,
                id: 'fldNotes',
                value:(this.editing)?this.expense.notes:null
            }
	            ]
		};
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		Clara.BudgetBuilder.InvoicableWindow.superclass.initComponent.apply(this, arguments);
	}
});
Ext.reg('clarainvoicablewindow', Clara.BudgetBuilder.InvoicableWindow);


Clara.BudgetBuilder.ExpensesWindow = Ext.extend(Ext.Window, {
	id: 'winInfo',
	width:750,
	height:500,
    title: 'Study Expenses',
    layout: 'fit',
	constructor:function(config){		
		Clara.BudgetBuilder.ExpensesWindow.superclass.constructor.call(this, config);
	},
	initComponent: function() {
		var config = {
				buttons:[{
					id:'btnCloseInfoWindow',
					text:'Close',
					handler: function(){
						Ext.getCmp('winInfo').close();
					}
				}],
				items: [{
					xtype:'tabpanel',
					border:false,
					forceLayout:true,
					activeTab:0,
					items:[{
						xtype:'clarainitcostspanel'
					},{
						xtype:'clarainvcostspanel'
					}],
					listeners:{
						beforetabchange: function(tp,newp,oldp){
							newp.refreshItems();
						}
					}
				}]
		};
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		Clara.BudgetBuilder.ExpensesWindow.superclass.initComponent.apply(this, arguments);
	}
});
Ext.reg('claraexpenseswindow', Clara.BudgetBuilder.ExpensesWindow);


Clara.BudgetBuilder.InitialCostsPanel = Ext.extend(Ext.Panel, {
	id: 'init-costs-panel',
	xtype: 'panel',
    title: 'Initiation Expenses (Non-refundable)',
    layout: 'border',
    selectedExpense:{},
	constructor:function(config){		
		Clara.BudgetBuilder.InitialCostsPanel.superclass.constructor.call(this, config);
		Clara.BudgetBuilder.MessageBus.on('budgetinfoupdated', this.onInfoUpdated, this);
		Clara.BudgetBuilder.MessageBus.on('expenseselected', this.onExpenseSelected, this);
	},
	onExpenseSelected: function(exp){
		//if (Ext.getCmp('init-costs-panel')){
			Ext.getCmp('init-costs-panel').selectedExpense = exp;
		//}
	},
	editExpenseById: function(id){
		var selectedExpense = budget.getExpense(id);
		Clara.BudgetBuilder.MessageBus.fireEvent('expenseselected', selectedExpense);
		//if (Clara.BudgetBuilder.canEdit()){
			var editInitialExpenseWindow = new Clara.BudgetBuilder.InitialExpenseWindow({readOnly:!Clara.BudgetBuilder.canEdit(),expense:selectedExpense, editing:true, modal:true, expenseType:'Administrative'});
			editInitialExpenseWindow.show();
		//}
	},
	onInfoUpdated: function() {
		var w = this;
		w.refreshItems();
	},
	refreshItems: function(){
		cdebug("refreshItems");
		Clara.BudgetBuilder.ExpenseStore.removeAll();
		Clara.BudgetBuilder.ExpenseStore.loadData(budget.getExpenseArray());
		Clara.BudgetBuilder.ExpenseStore.filter('type','Initial Cost',false,true,true);
		this.updateTotal();
	},
	updateTotal: function(){
		cdebug("updateTotal");
		if (jQuery("#total-init-value").length > 0){
			var gs = Ext.getCmp('gpInitCosts').getStore().data.items;
			//cdebug(gs);
			var itemTotal = 0;
			var total = 0;
			
			for (var i=0;i<gs.length;i++){
				var e = gs[i].data;
				if (e.diff != "D"){
					itemTotal += e.count;
					total += (e.count * (e.cost + (e.cost * (e.fa/100))));//:(e.count * e.cost);
				}
			}
			//cdebug(Ext.util.Format.usMoney(total));
			jQuery("#total-init-value").html(Ext.util.Format.usMoney(total));
		}
	},
	initComponent: function() {
		var icp=this;
		var config = {
				items: [{
					xtype:'panel',
					hidden:!Clara.BudgetBuilder.canEdit(),
					region:'west',
					width:200,
					id:'initAddPanel',
					layout:'vbox',
					align : 'stretch',
				    pack  : 'start',
				    title:'Add expense..',
					items:[{
						xtype:'button',
						width:184,
						scale:'large',
						text:'Administrative',
				        handler: function(){
				   		   var addInitialExpenseWindow = new Clara.BudgetBuilder.InitialExpenseWindow({modal:true, expenseType:'Administrative'});
				   		   addInitialExpenseWindow.show();
				   		}
						
					},{
						xtype:'button',
						width:184,
						scale:'large',
						style:'margin-top:8px;',
			    		text: 'Personnel',
					    	   handler: function(){
			   				var addInitialExpenseWindow = new Clara.BudgetBuilder.InitialExpenseWindow({modal:true, expenseType:'Personnel'});
			   				addInitialExpenseWindow.show();
			   			}
					}],
					padding:6,
					border:false,
					style:'border-right:2px solid #8ab1e5;',
					bodyStyle:{"background-color":"#dfe8f6"}
				},
				{
					xtype:'panel',
					region:'south',
					id:'initTotalPanel',
					html:'<div id="total-init"><div class="expense-total-label">Initiation Cost Total:</div><div class="expense-total-value" id="total-init-value"></div><div style="clear:both;"></div></div>',
					height:38,
					border:false,
					padding:6,
					unstyled:true,
					listeners: {afterrender:function(){Ext.getCmp("init-costs-panel").updateTotal();}},
					style:'border-top:2px solid #8ab1e5;font-size:16px;'
				},
		        {
			     xtype: 'grid',
			     tbar:{

			            xtype: 'toolbar',
			            id: 'tbInit',
			            items: [
						{
						    xtype: 'button',
						    text: 'Print Expenses',
						    iconCls:'icn-printer',
						    id: 'btnPrintExpenses',
						    handler:function(){
						    	Clara.BudgetBuilder.GetProtocolCoverSheet(['expenses'], true);
							}
						},
	                    '->',
			            {
			                xtype: 'tbtext',
			                text: 'Set Default F&A to:',
			                disabled:!Clara.BudgetBuilder.canEdit()
			            },{
		                    xtype: 'numberfield',
		                    allowBlank:false,
		                    disabled:!Clara.BudgetBuilder.canEdit(),
		                    value: budget.initialExpenseFA,
		                    id:'fldFAExpenses',
		                    width: 50
		                },
		                {
			                xtype: 'button',
			                text: 'Save',
			                disabled:!Clara.BudgetBuilder.canEdit(),
			                iconCls:'icn-calculator--pencil',
			                id: 'btnSetFAExpense',
			                handler:function(){
			            		if (Ext.getCmp("fldFAExpenses").validate()){
			            			var newFA = Ext.getCmp("fldFAExpenses").getValue();
				            		budget.setExpenseFA("Initial Cost",newFA);
				            		
				            		
				            		Clara.BudgetBuilder.SaveAction = "Expense F&A";
				            		budget.save();
				            		Clara.BudgetBuilder.MessageBus.fireEvent('budgetinfoupdated');
			            		}
			        		}
			            }
			            ]
			        
			     },
			     region:'center',
					      view: new Ext.grid.GroupingView({
					          //forceFit: true,
						         rowOverCls:'',
						         emptyText: 'No initiation costs.',
						         headersDisabled:true,
					          groupTextTpl: '{text} ({[values.rs.length]} {[values.rs.length > 1 ? "Expenses" : "Expense"]})',
					          showGroupName:false,
					          getRowClass: function(rec, idx, rowPrms, ds) {
					        	  
					        	  if (rec.get("diff") != ""){
					        		  return 'expense-diff-'+rec.get("diff");
					        	  } else {
					        		  return rec.data.external === true ? 'expense-row-external' : '';
					        	  }
					          }
					      }),
					     stripeRows:true,
					     loadMask:true,
					     border: false,
					     store:Clara.BudgetBuilder.ExpenseStore,
					     id: 'gpInitCosts',
					     autoExpandColumn:'exp-description',
			             listeners:{
			            	 rowclick:function(g,rowIndex,e){
				            		if (typeof g.getSelectionModel().getSelected() != 'undefined' ){
				            			var d = g.getSelectionModel().getSelected().data;
				            			Clara.BudgetBuilder.MessageBus.fireEvent('expenseselected', budget.getExpense(d.id));
				            		}
				            	},
				            	rowdblclick:function(g,rowIndex,e){
				            		if (typeof g.getSelectionModel().getSelected() != 'undefined' ){
				            			var d = g.getSelectionModel().getSelected().data;

				            			icp.editExpenseById(d.id);
				            			
				            		}
				            	}
			             },
					     columns: [
					         {
					             xtype: 'gridcolumn',
					             dataIndex: 'type',
					             header: 'Type',
					             sortable: false,
					             hidden:true
					             
					         },
					         {
					             xtype: 'gridcolumn',
					             dataIndex: 'subtype',
					             header: 'Cost type',
					             sortable: false,
					             hidden:true
					         },
					         {
					             xtype: 'gridcolumn',
					             dataIndex: 'description',
					             id:'exp-description',
					             header: 'Description',
					             sortable: false,
					             renderer:function(v,p,r){
					            	 var st = "";
					            	 if (r.get("subtype") != "IRB Fee" && r.get("subtype") != "Pharmacy Fee") st = "<a style='float:left;' href=\"javascript:Ext.getCmp('init-costs-panel').editExpenseById("+r.get("id")+");\">"+v+"</a>";	
					            	 else st = v;
					            	 
					            	 if (r.get("notes") && r.get("notes").length > 0){
					            		 st += "<img style='float:right;margin-left:8px;' src='"+appContext+"/static/images/icn/sticky-note.png' border='0'/>";
					            	 }
					            	 
					            	 return st;
					             }
					         },
					         {
					             xtype: 'gridcolumn',
					             dataIndex: 'cost',
					             header: 'Cost',
					             renderer:Ext.util.Format.usMoney,
					             align:'right',
					             width:60,
					             fixed:true,
					             sortable: false
					         },
					         {
					             xtype: 'gridcolumn',
					             dataIndex: 'fa',
					             header: 'F&A %',
					             //renderer:Ext.util.Format.usMoney,
					             align:'right',
					             width:55,
					             sortable: false
					         },
					         {
					             xtype: 'gridcolumn',
					             header: 'Total',
					             width:60,
					             align:'right',
					             sortable: false,
					             renderer: function(v,s,r){
					        	 	var e = r.data;
					         		return Ext.util.Format.usMoney(e.cost + e.cost * (e.fa/100));
					         	 }
					         },
					         {
					        	 xtype:'gridcolumn',
					        	 dataIndex:'id',
					        	 header:'Actions',
					        	 width:60,
					        	 renderer: function(v,s,r){
					        		 return ( r.get("external") == false && Clara.BudgetBuilder.canEdit() )?"<span class='row-actions'><a href='javascript:;' onclick='Clara.BudgetBuilder.RemoveExpense("+r.data.id+");'>Remove</a></span>":"";
					        	 }
					         }
					     ]
					    }
					 ]
		};
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		Clara.BudgetBuilder.InitialCostsPanel.superclass.initComponent.apply(this, arguments);
		
	}
});
Ext.reg('clarainitcostspanel', Clara.BudgetBuilder.InitialCostsPanel);


Clara.BudgetBuilder.RemoveExpense = function(id){
	var exp = budget.getExpense(id);
	if (typeof exp.id != 'undefined' && (exp.external == false || (exp.external && Clara.BudgetBuilder.AllowExternalCostEditing))){ 
		budget.removeExpense(exp);
		Clara.BudgetBuilder.SaveAction = "Remove expense";
		budget.save();
		Clara.BudgetBuilder.MessageBus.fireEvent('budgetinfoupdated');
	}
	
	if (typeof exp.id != 'undefined' && exp.external == true && !Clara.BudgetBuilder.AllowExternalCostEditing){
		alert("This expense cannot be deleted.");
	}
	
};

Clara.BudgetBuilder.InvoicablePanel = Ext.extend(Ext.Panel, {
	id: 'invoicable-panel',
	selectedExpense:{},
	xtype: 'panel',
    title: 'Invoicable Expenses',
    layout: 'border',
	constructor:function(config){		
		Clara.BudgetBuilder.InvoicablePanel.superclass.constructor.call(this, config);
		Clara.BudgetBuilder.MessageBus.on('budgetinfoupdated', this.onInfoUpdated, this);
		Clara.BudgetBuilder.MessageBus.on('expenseselected', this.onExpenseSelected, this);
	},
	onExpenseSelected: function(exp){
		//if (this.isVisible()){
			Ext.getCmp('invoicable-panel').selectedExpense = exp;
		//}
	},
	editInvoicableById: function(id){
		var selectedExpense = budget.getExpense(id);
		
		Clara.BudgetBuilder.MessageBus.fireEvent('expenseselected', selectedExpense);
		//if (Clara.BudgetBuilder.canEdit()){
			var editInvoicableExpenseWindow = new Clara.BudgetBuilder.InvoicableWindow({readOnly:!Clara.BudgetBuilder.canEdit(),expense:selectedExpense, editing:true, modal:true, expenseType:'Administrative'});
			editInvoicableExpenseWindow.show();
		//}
	},
	onInfoUpdated: function() {
		//if (this.isVisible()){
			this.refreshItems();
		//}
	},
	refreshItems: function(){
		var gs = Ext.getCmp('gpInvCosts').getStore();
		gs.removeAll();
		gs.loadData(budget.getExpenseArray());
		gs.filter('type','Invoicable',false,true,true);
		this.updateTotal();
	},
	updateTotal: function(){
		//cdebug("updateTotal");
		if (jQuery("#total-invoicables-value").length > 0){
			var gs = Ext.getCmp('gpInvCosts').getStore().data.items;
			//cdebug(gs);
			var itemTotal = 0;
			var total = 0;
			
			for (var i=0;i<gs.length;i++){
				var e = gs[i].data;
				if (e.diff != "D"){
					itemTotal += e.count;
					total += (e.faenabled)?(e.count * (e.cost + (e.cost * (e.fa/100)))):(e.count * e.cost);
				}
			}
			//cdebug(Ext.util.Format.usMoney(total));
			jQuery("#total-invoicables-value").html(Ext.util.Format.usMoney(total));
		} else {
			//cdebug('#total-invoicables-value doesnt exist');
		}
	},
	initComponent: function() {
		var invp = this;
		var config = {
             
				items: [{

					xtype:'panel',
					region:'west',
					width:200,
					hidden:!Clara.BudgetBuilder.canEdit(),
					id:'invAddPanel',
					layout:'vbox',
					align : 'stretch',
				    pack  : 'start',
				    title:'Add expense..',
					items:[{
						xtype:'button',
						width:184,
						margins:{top:0, right:0, bottom:2, left:0}, scale:'medium',
						text:'Protocol Amendments (staff time)',
				        handler: function(){
				        	var iw = new Clara.BudgetBuilder.InvoicableWindow({modal:true, expenseType:this.text, expenseFA:budget.initialInvoicableFA, faenabled:true});
					   		iw.show();   
				   		}
						
					},{
						xtype:'button',
						width:184,
						margins:{top:0, right:0, bottom:2, left:0}, scale:'medium',
						
			    		text: 'Monitoring Visits (staff time)',
					    	   handler: function(){
					    		   var iw = new Clara.BudgetBuilder.InvoicableWindow({modal:true, expenseType:this.text, expenseFA:budget.initialInvoicableFA, faenabled:true});
							   		iw.show(); 
			   			}
					},{
						xtype:'button',
						width:184,
						margins:{top:0, right:0, bottom:2, left:0}, scale:'medium',
			    		text: 'Screen Failures',
					    	   handler: function(){
					    		   var iw = new Clara.BudgetBuilder.InvoicableWindow({modal:true, expenseType:this.text, expenseFA:budget.initialInvoicableFA, faenabled:true});
							   		iw.show(); 
			   			}
					},{
						xtype:'button',
						width:184,
						margins:{top:0, right:0, bottom:2, left:0}, scale:'medium',
			    		text: 'Incomplete Subjects',
					    	   handler: function(){
					    		   var iw = new Clara.BudgetBuilder.InvoicableWindow({modal:true, expenseType:this.text, expenseFA:budget.initialInvoicableFA, faenabled:true});
							   		iw.show(); 
			   			}
					},{
						xtype:'button',
						width:184,
						margins:{top:0, right:0, bottom:2, left:0}, scale:'medium',
			    		text: 'Research Document Storage',
					    	   handler: function(){
					    		   var iw = new Clara.BudgetBuilder.InvoicableWindow({modal:true, expenseType:this.text, expenseFA:0, faenabled:false});
							   		iw.show(); 
			   			}
					},{
						xtype:'button',
						width:184,
						margins:{top:0, right:0, bottom:2, left:0}, scale:'medium',
			    		text: 'Study Close Out Fees',
					    	   handler: function(){
					    		   var iw = new Clara.BudgetBuilder.InvoicableWindow({modal:true, expenseType:this.text, expenseFA:0, faenabled:false});
							   		iw.show(); 
			   			}
					},{
						xtype:'button',
						width:184,
						margins:{top:0, right:0, bottom:2, left:0}, scale:'medium',
			    		text: 'Regulatory Processing (staff time)',
					    	   handler: function(){
					    		   var iw = new Clara.BudgetBuilder.InvoicableWindow({modal:true, expenseType:this.text, expenseFA:budget.initialInvoicableFA, faenabled:true});
							   		iw.show(); 
			   			}
					},{
						xtype:'button',
						width:184,
						margins:{top:0, right:0, bottom:2, left:0}, scale:'medium',
			    		text: 'Safety Reports-Simple (staff time)',
					    	   handler: function(){
					    		   var iw = new Clara.BudgetBuilder.InvoicableWindow({modal:true, expenseType:this.text, expenseFA:budget.initialInvoicableFA, faenabled:true});
							   		iw.show(); 
			   			}
					},{
						xtype:'button',
						width:184,
						margins:{top:0, right:0, bottom:2, left:0}, scale:'medium',
			    		text: 'Safety Reports-Complex (staff time)',
					    	   handler: function(){
					    		   var iw = new Clara.BudgetBuilder.InvoicableWindow({modal:true, expenseType:this.text, expenseFA:budget.initialInvoicableFA, faenabled:true});
							   		iw.show(); 
			   			}
					},{
						xtype:'button',
						width:184,
						margins:{top:0, right:0, bottom:2, left:0}, scale:'medium',
			    		text: 'Other',
					    	   handler: function(){
					    		   var iw = new Clara.BudgetBuilder.InvoicableWindow({modal:true, expenseType:this.text, expenseFA:budget.initialInvoicableFA, faenabled:true});
							   		iw.show(); 
			   			}
					}],
					padding:6,
					border:false,
					style:'border-right:2px solid #8ab1e5;',
					bodyStyle:{"background-color":"#dfe8f6"}
				
				},{
							xtype:'panel',
							region:'south',
							id:'invoicableTotalPanel',
							html:'<div id="total-invoicables"><div class="expense-total-label">Invoiced Total:</div><div class="expense-total-value" id="total-invoicables-value"></div><div style="clear:both;"></div></div>',
							height:38,
							border:false,
							padding:6,
							unstyled:true,
							listeners: {afterrender:function(){Ext.getCmp("invoicable-panel").updateTotal();}},
							style:'border-top:2px solid #8ab1e5;font-size:16px;'
						},
				        {
					     xtype: 'grid',
					     region:'center',
					      view: new Ext.grid.GridView({
					          // forceFit: true,
						      rowOverCls:'',
						      emptyText: 'No invoicable costs.',
						      headersDisabled:true,
						      getRowClass: function(rec, idx, rowPrms, ds) {
						    	  if (rec.get("diff") != ""){
						    		  return 'expense-diff-'+rec.get("diff");
						    	  }
						      }
					      }),
					      tbar:{

					            xtype: 'toolbar',
					            disabled:!Clara.BudgetBuilder.canEdit(),
					            id: 'tbInv',
					            items: [
								{
								    xtype: 'button',
								    text: 'Print Invoicables',
								    iconCls:'icn-printer',
								    id: 'btnPrintInvoicables',
								    handler:function(){
								    	Clara.BudgetBuilder.GetProtocolCoverSheet(['invoicables'], true);
									}
								},
					            '->',
				                {
					                xtype: 'tbtext',
					                text: 'Set Default F&A to:',
					                disabled:!Clara.BudgetBuilder.canEdit()
					            },{
				                    xtype: 'numberfield',
				                    allowBlank:false,
				                    disabled:!Clara.BudgetBuilder.canEdit(),
				                    value: budget.getInitialInvoicableFA(),
				                    id:'fldFAInvoicables',
				                    width: 50
				                },{
					                xtype: 'button',
					                text: 'Save',
					                iconCls:'icn-calculator--pencil',
					                id: 'btnSetFAInvoicable',
					                handler:function(){
					            		if (Ext.getCmp("fldFAInvoicables").validate()){
						            		budget.setExpenseFA("Invoicable", Ext.getCmp("fldFAInvoicables").getValue());
						            		Clara.BudgetBuilder.SaveAction = "Set invoicable F&A";
						            		budget.save();
						            		Clara.BudgetBuilder.MessageBus.fireEvent('budgetinfoupdated');
					            		}
					        		}
					            }
					            ]
					        
					      },
					     stripeRows:true,
					     loadMask:true,
					     border: false,
					     store:Clara.BudgetBuilder.InvoicableStore,
					     id: 'gpInvCosts',
					     autoExpandColumn:'col-inv-desc',
			             listeners:{
				            	rowclick:function(g,rowIndex,e){
				            		var d =  g.getSelectionModel().getSelected().data;
				            		Clara.BudgetBuilder.MessageBus.fireEvent('expenseselected', budget.getExpense(d.id));
				            	},
				            	rowdblclick:function(g,rowIndex,e){
				            		if (typeof g.getSelectionModel().getSelected() != 'undefined' ){
				            			var d = g.getSelectionModel().getSelected().data;
				            			invp.editInvoicableById(d.id);
				            			
				            			
				            		}
				            	}
				             },
						     columns: [
						         {
						             xtype: 'gridcolumn',
						             dataIndex: 'type',
						             header: 'Type',
						             sortable: false,
						             hidden:true
						         },
						         {
						             xtype: 'gridcolumn',
						             dataIndex: 'subtype',
						             header: 'Cost type',
						             sortable: false,
						             hidden:true
						         },
						         {
						             xtype: 'gridcolumn',
						             dataIndex: 'description',
						             header: 'Description',
						             id:'col-inv-desc',
						             sortable: false,
						             renderer: function(v,s,r){

						         		var html = (r.data.subtype == 'Other')?("<span class='invoicable-other'><strong>Other:</strong> "+v+"</span>"):v;
						         		html = (r.get("subtype") != "IRB Fee" && r.get("subtype") != "Pharmacy Fee")?"<a style='float:left;' href=\"javascript:Ext.getCmp('invoicable-panel').editInvoicableById("+r.get("id")+");\">" + html + "</a>":html;
						            	 
						            	 if (r.get("notes") && r.get("notes").length > 0){
						            		 html += "<img style='float:right;margin-left:8px;' src='"+appContext+"/static/images/icn/sticky-note.png' border='0'/>";
						            	 }
						            	 
						            	 return html;
						         	 }
						         },
						         {
						             xtype: 'gridcolumn',
						             dataIndex: 'cost',
						             header: 'Cost',
						             width:60,
						             renderer:Ext.util.Format.usMoney,
						             align:'right',
						             fixed:true,
						             sortable: false
						         },
						         {
						             xtype: 'gridcolumn',
						             dataIndex: 'count',
						             header: '#',
						             width:32,
						             fixed:true,
						             sortable: false
						         },
						         {
						             xtype: 'gridcolumn',
						             dataIndex: 'fa',
						             header: 'F&A %',
						             width:42,
						             fixed:true,
						             sortable: false
						         },
						         {
						             xtype: 'gridcolumn',
						             header: 'Total',
						             id:'col-init-total',
						             width:60,
						             align:'right',
						             sortable: false,
						             renderer: function(v,s,r){
						        	 	var e = r.data;
						         		return Ext.util.Format.usMoney((e.faenabled)?((e.cost + e.cost * (e.fa/100)) * e.count):(e.cost * e.count));
						         	 }
						         },
						         {
						        	 xtype:'gridcolumn',
						        	 dataIndex:'id',
						        	 header:'Actions',
						        	 width:60,
						        	 renderer: function(v,s,r){
						        		 return (r.get("subtype") != "IRB Fee" && r.get("subtype") != "Pharmacy Fee" && Clara.BudgetBuilder.canEdit())?"<span class='row-actions'><a href='javascript:;' onclick='Clara.BudgetBuilder.RemoveExpense("+r.data.id+");'>Remove</a></span>":"";
						        	 }
						         }
						     ]
					    }
					 ]
		};
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		Clara.BudgetBuilder.InvoicablePanel.superclass.initComponent.apply(this, arguments);
	}
});
Ext.reg('clarainvcostspanel', Clara.BudgetBuilder.InvoicablePanel);




