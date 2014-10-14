Ext.ns('Clara.Pharmacy');


Clara.Pharmacy.ConfirmRemoveExpense = function(eid){
	Ext.Msg.show({
		title:"WARNING: About to delete expense",
		msg:"Are you sure you want to delete this expense?", 
		buttons:Ext.Msg.YESNOCANCEL,
		icon:Ext.MessageBox.WARNING,
		fn: function(btn){
			if (btn == 'yes'){
				pharmacy.removeExpenseById(eid);
				pharmacy.save();
			}
		}
		
	});
	return false;
};

Clara.Pharmacy.FeeGridPanel = Ext.extend(Ext.grid.GridPanel, {
	selectedExpenseId:null,
	initComponent:function(){
		var t = this;
		var config = {
			tbar: {
				xtype:'toolbar',
				items:[{xtype:'button',disabled:!Clara.Pharmacy.canEdit(),text:'Initiation Expense', iconCls:'icn-plus-button', handler: function(){new Clara.Pharmacy.ExpenseWindow({exptype:'simc'}).show();}},
				       {xtype:'button',disabled:!Clara.Pharmacy.canEdit(),text:'Other / Annual Expense', iconCls:'icn-plus-button', handler: function(){new Clara.Pharmacy.ExpenseWindow({exptype:'annual'}).show();}},
					 {xtype:'button',disabled:!Clara.Pharmacy.canEdit(),text:'Drug Dispensing Fee', iconCls:'icn-pill--plus', handler: function(){new Clara.Pharmacy.InvestigationalDrugComboWindow({}).show();}},
					 '-',
				       {id:'btnRemoveExpense',disabled:true,xtype:'button', text:'Remove Expense', iconCls:'icn-minus-button', handler:function(){
							clog("removing "+t.selectedExpenseId);
							Clara.Pharmacy.ConfirmRemoveExpense(t.selectedExpenseId);
				       }},'->', {xtype:'button', text:'Show Coversheet', iconCls:'icn-document', handler:function(){
				    	   var url = appContext+"/protocols/"+claraInstance.id+"/protocol-forms/"+claraInstance.form.id+"/pharmacy/pharmacybuilder?coversheet";
				    		window.open(url,'','width=800,height=600,location=0,menubar=0,scrollbars=1,status=0,toolbar=0,resizable=1');
				       }},
				       {xtype:'cycle', disabled:!Clara.Pharmacy.canEdit(), showText:true, prependText:'<span style="font-weight:800;">INITIAL Fees</span> are ', id:'btnToggleFee', items: [{id:'btnFeeNotWaived',text:'not waived',iconCls:'icn-money'}, {id:'btnFeeWaived',text:'<span style="font-weight:800;color:red;">WAIVED</span>',iconCls:'icn-money--minus'}],
				    	   changeHandler:function(btn,item){
				    	   		pharmacy.setInitialWaived(item.text !== "not waived");
				       		}},
				
						      
						       {xtype:'button', text:'Close', iconCls:'icn-disk', handler:function(){
						    	   closeCurrentForm(function(){window.close();}, false);
						       }}
				       ]
			},
			bbar: {
				xtype:'toolbar',
				items:['->',{xtype:'panel', padding:8, border:false, plain:true,unstyled:true, html:'<div id="pharmacy-total">Total (initiation costs only): '+pharmacy.getDisplayTotal()+'</div>'}]
			},
			store: pharmacy.getStore(),
			colModel: new Ext.grid.ColumnModel({
				columns:[
						{id:'type',header:'Type',dataIndex:'type',width:100, hidden:true},
						{id:'description',header:'Description',dataIndex:'description',width:300, renderer:function(v,p,r){
							var html = "<div class='wrap'>";
							if (r.data.type == "Drug Dispensing Fee"){
								var id = r.get("id");
								html += "<span style='font-weight:800;'>"+v+"</span>";
								var exp = pharmacy.getExpense(id);
								
								
								
								html += "<ul class='pharmacy-expense-drugs'>";
								for (var i=0;i<exp.drugs.length;i++){
									html += "<li>"+exp.drugs[i].name+"</li>";
								}
								html += "</ul>";
									
								html += "<ul class='pharmacy-expense-fees'>";
								for (var i=0;i<exp.fees.length;i++){
									html += "<li>"+exp.fees[i].description+"</li>";
								}
								html += "</ul>";
								if (exp.notes && exp.notes.length > 0){
									html += "<div class='row-pharmacy-notes'>"+exp.notes+"</div>"
								}
							}
							else {
								var id = r.get("id");
								html += v;
								var exp = pharmacy.getExpense(id);
								if (exp.notes && exp.notes.length > 0){
									html += "<div class='row-pharmacy-notes'>"+exp.notes+"</div>"
								}
							}
							return html+"</div>";
						}},
						{id:'cost',header:'Cost',dataIndex:'cost',width:30, renderer:Ext.util.Format.usMoney},
						{header:'Waived?',dataIndex:'id', width:50, renderer: function(v,p,r){
							if (r.get("type") == "Study Initiation, Management and Closeout"){
								return (r.get("waived") === false)?"No":"<span style='font-weight:800;color:red'>Yes</span>";
							} else {
								if (r.get("waived") === false) return "No <a href='javascript:pharmacy.setExpenseWaived("+v+", true);'>Waive</a>";
								else return "<span style='font-weight:800;color:red'>Yes</span> <a href='javascript:pharmacy.setExpenseWaived("+v+", false);'>Add back</a>";
							}
						}}
				         ]
			}),
			listeners: {
				rowclick: function(t, i){
					var rec = t.getStore().getAt(i);
					t.selectedExpenseId = rec.get("id");
					clog("NAME: ");clog(rec.data);
					if (rec.get("description") != "Base Fee") Ext.getCmp("btnRemoveExpense").setDisabled(!Clara.Pharmacy.canEdit());
					else Ext.getCmp("btnRemoveExpense").setDisabled(true);
				},
				rowdblclick: function(t,i){
					var rec = t.getStore().getAt(i);
					if (rec.get("description") == "Base Fee" && Clara.Pharmacy.canEdit()){
						Ext.Msg.prompt('Editing Base Fee', 'Enter a new base fee:', function(btn, text){
						    if (btn == 'ok'){
						    	var newValue = -1;
						        if (Ext.num(text, -1) > -1 ){
						        	newValue = text;
						        } else if (text == ''){
						        	newValue = 0;
						        } else {
						        	Ext.Msg.alert('Invalid value', 'Please enter a valid amount.');
						        }
						        clog("NewValue is "+newValue);
						        if (newValue > -1){
						        	// SAVE
						        	pharmacy.updateExpenseCost(rec.get("id"),newValue);
						        	pharmacy.save();
						        }
						    }
						},this,false,rec.get("cost"));
					}
				}
			},
			view: new Ext.grid.GroupingView({
		        forceFit: true,
		        showGroupName : false,
		        // custom grouping text template to display the number of items per group
		        groupTextTpl: '{text} - <span style="font-weight:100;">{[values.rs.length]} {[values.rs.length > 1 ? "Expenses" : "Expense"]}</span>'
		    })
		};
		// apply config
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		  
		// call parent
		Clara.Pharmacy.FeeGridPanel.superclass.initComponent.apply(this, arguments);
		this.getStore().loadData(pharmacy.getArray());

	},
	
	
	renderFeeRow:function(value, p, r){
		rHtml = "<div class='fee-row'>";
		rHtml = rHtml + "<div class='fee-row-desc'><input type='checkbox' onclick=\"toggleFeeValue('fee-field-enable-"+r.data.id+"');\" class='cb-fee-field' name='fee-field-enable-"+r.data.id+"' id='fee-field-enable-"+r.data.id+"' value='"+r.data.id+"'/><label for='fee-field-enable-"+r.data.id+"'>" + r.data.description + "</label>";
		if (r.data.editable == 'false'){
			rHtml = rHtml + "<div class='fee-row-cost'>ADD: <span class='fee-row-cost-value'>$"+parseFloat(r.data.cost).toFixed(2)+"</span></div></div>";
			rHtml = rHtml + "<div class='fee-row-value' id='fee-row-value-" +r.data.id+"'><input class='fld-input-value' name='fld-" +r.data.id+"' id='fld-" +r.data.id+"' value='" + r.data.cost + "' readonly='readonly'/>";
			if (r.data.canaddmultiple == 'true'){
				rHtml = rHtml + "<br/><input type='checkbox' onclick=\"toggleMultipleFeeValue('fee-field-multiple-enable-"+r.data.id+"');\" class='cb-fee-multiple-field' name='fee-field-multiple-enable-"+r.data.id+"' id='fee-field-multiple-enable-"+r.data.id+"' value='"+r.data.id+"'/><label class='cb-fee-multiple-field-label' for='fee-field-multiple-enable-"+r.data.id+"'>Multiple investigational drugs<br/>(ADD $15.00)</label>";
			}
			rHtml = rHtml + "</div>";
		} else {
			rHtml = rHtml + "<div class='fee-row-cost'>ADD: <span class='fee-row-cost-value'>Enter value at the right</span></div></div>";
			rHtml = rHtml + "<div class='fee-row-value' id='fee-row-value-" +r.data.id+"'><input class='fld-input-value' onkeydown='updateTotals();' onkeyup='updateTotals();' name='fld-" +r.data.id+"' value='" + r.data.cost + "'/></div>";
		}
		rHtml = rHtml + "<div style='clear:both;'></div></div>";
		return rHtml;
	},
	
	onRender:function(){
		clog("Render: Pharmacy",pharmacy);
		if (pharmacy.initialWaived){
			clog("ITS INITAL WAIVED!");
			 Ext.getCmp("btnToggleFee").setActiveItem(Ext.getCmp("btnFeeWaived"),true);
		}
		
		Clara.Pharmacy.FeeGridPanel.superclass.onRender.apply(this, arguments);
	}
	
});

Ext.reg('pharmacyfeepanel',Clara.Pharmacy.FeeGridPanel);