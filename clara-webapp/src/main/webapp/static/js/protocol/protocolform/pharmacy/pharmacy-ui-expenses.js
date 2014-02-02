Ext.ns('Clara.Pharmacy');


Clara.Pharmacy.ExpenseWindow = Ext.extend(Ext.Window, {
	id:'winPharmacyExpense',
    title: 'Add expense',
    initExpenseFeeStore: new Ext.data.JsonStore({
    	header :{
    	    'Accept': 'application/json'
    	    },
    	    proxy : new Ext.data.HttpProxy({url:appContext+"/static/xml/pharmacy-fees-simc.json", method:'GET'}),

    	idProperty: 'id',
    	autoLoad:true,
    	fields:[
    		    {name:'id'},
    		    {name:'exptype'},
    		    {name:'description'},
    		    {name:'cost'},
    		    {name:'editable'},
    		    {name:'validMin'},
    		    {name:'validMax'}
    		]
    }),
    annualExpenseFeeStore: new Ext.data.JsonStore({
    	header :{
    	    'Accept': 'application/json'
    	    },
    	proxy : new Ext.data.HttpProxy({url:appContext+"/static/xml/pharmacy-fees-annual.json", method:'GET'}),
    	idProperty: 'id',
    	autoLoad:true,
    	fields:[
    		    {name:'id'},
    		    {name:'exptype'},
    		    {name:'description'},
    		    {name:'cost'},
    		    {name:'editable'},
    		    {name:'validMin'},
    		    {name:'validMax'}
    		]
    }),
    exptype:'simc',
    expense: {},
    width: 500,
    height: 190,
    padding:6,
    layout: 'form',
    editing:false,
    border:false,
    initComponent: function() {		
		var t = this;
		var feestore = (t.exptype == "simc")?t.initExpenseFeeStore:t.annualExpenseFeeStore;
		var config = {

			buttons:[{
				text:'Save Expense',
				handler:function(){
					if (Ext.getCmp("fldExpense").getValue() == "") {
						alert("Choose an expense type first.");
					} else {
						var exp = new Clara.Pharmacy.Expense({id:pharmacy.newId(), cost:parseFloat(Ext.getCmp("fldCost").getValue()), notes:Ext.getCmp("fldNotes").getValue(), type:feestore.getById(Ext.getCmp("fldExpense").getValue()).get("exptype"),name:Ext.getCmp("fldExpense").getValue(), description:feestore.getById(Ext.getCmp("fldExpense").getValue()).get("description"), count:1});
						pharmacy.expenses.push(exp);
						pharmacy.updateTotal();
						pharmacy.save();
						Ext.getCmp('winPharmacyExpense').close();
					}
				}
			}],
			items:[{
				xtype:'combo',
				id:'fldExpense',
				fieldLabel: 'Expense Type',
				anchor:'100%',
                typeAhead: true,
                triggerAction: "all",
                store: feestore,
                lazyRender: true,
                displayField:'description',
                valueField:'id',
                
                selectOnFocus:true,
                listeners:{
					
            		change:function(f,v,ov){
            			// v is value
						// get id, show price and set expensetype
						Ext.getCmp("fldCost").setValue(feestore.getById(v).get("cost"));
            		}
            	}
			},{
                xtype: 'numberfield',
                fieldLabel: 'Cost',
                id: 'fldCost',
                anchor: '100%'
            },
            {
                xtype: 'textarea',
                anchor: '100%',
                id:'fldNotes',
                fieldLabel: 'Notes'
            }]
		};
		Clara.NewSubmission.ProtocolDrugStore.load();
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		Clara.Pharmacy.ExpenseWindow.superclass.initComponent.apply(this, arguments);
	
		
	}
});

Clara.Pharmacy.InvestigationalDrugComboWindow = Ext.extend(Ext.Window, {
	id:'winPharmacyInvDrug',
    title: 'Dispensing Combination',
    width: 600,
    height: 620,
    layout: 'border',
    editing:false,
    border:false,
    drugs:[],
    fee1:0,
    fee2:0,
    fee3:0,
    getTotal: function(){
		Ext.getCmp("fldCost").setValue(parseFloat(this.fee1)+parseFloat(this.fee2)+parseFloat(this.fee3));
	},
    initComponent: function() {
		var t = this;
		var config = {
				buttons:[
							{
								text:'Close',
								disabled:false,
								handler: function(){
									Ext.getCmp('winPharmacyInvDrug').close();
								}
							},
							{
								text:'Save Combination',
								id:'btn-save-combo',
								disabled:true,
								handler: function(){
									var device = Ext.getCmp('winPharmacyInvDrug').drugs;
									
									if (t.drugs.length > 0){
										var cost = Ext.getCmp("fldCost").getValue();
										var note = Ext.getCmp("fldNotes").getValue();
										var type = "drug";
										var desc = Ext.getCmp("fldDesc").getValue();
										
										var exp = new Clara.Pharmacy.Expense({id:pharmacy.newId(), cost:cost, notes:note, type:type, description:desc, name:desc, count:1});
										
										jQuery.each(t.drugs, function(i,d){
											exp.addDrug(d.id, d.name);
										});
										
										var fee1radio = Ext.getCmp("fldAdditionalFee").getValue();
										var fee2radio = Ext.getCmp("fldAdditionalFeeDoses").getValue();
										var fee3radio = Ext.getCmp("fldAdditionalFeeMultiple").getValue();
										
										if (fee1radio != null) {
											exp.addFee(fee1radio.getGroupValue().split('- Add $')[1],fee1radio.getGroupValue());
										}
										
										if (fee2radio != null) {
											exp.addFee(fee2radio.getGroupValue().split('- Add $')[1],fee2radio.getGroupValue());
										}
										
										if (fee3radio != null) {
											exp.addFee(fee3radio.getGroupValue().split('- Add $')[1],fee3radio.getGroupValue());
										}
										
										pharmacy.expenses.push(exp);
										pharmacy.save();
										Ext.getCmp('winPharmacyInvDrug').close();
									} else {
										alert('Choose one or more drugs first.');
									}
								}
				}],
				items:[{
					xtype:'grid',
					region:'center',
					id:'gpDrugs',
					loadMask: new Ext.LoadMask(Ext.getBody(), {msg:"Please wait..."}),
					listeners:{
						rowclick:function(){
							Ext.getCmp("btn-save-combo").setDisabled(false);
							t.drugs.splice(0, t.drugs.length);
							var records = Ext.getCmp("gpDrugs").getSelectionModel().getSelections();
							jQuery.each(records, function(i,r){
								var d = new Clara.NewSubmission.Drug({});
								d.fromRecord(r);
								t.drugs.push(d);
							});
						}
					},
			        columns: [
			                  {
			                	  	header:'Drug Name',
			                	  	dataIndex:'lastname',
			                	  	sortable:true,
			                	  	renderer:function(value, p, record){
					                      return String.format('<b>{1}</b><br/>{2}',value, record.data.name, record.data.status);
					                },
			                	  	width:250
			                  },
			                  {
			                	  	header:'How to administer',
			                	  	dataIndex:'administration',
			                	  	sortable:false,
			                	  	width:100
			                  }
			        ],
			        store:Clara.NewSubmission.ProtocolDrugStore,
					viewConfig: {
						forceFit:true
					}
				},{
					xtype:'form',
					height:360,
					region:'south',
					padding:6,
					items:[{
		                xtype: 'textfield',
		                allowBlank:false,
		                fieldLabel: 'Description',
		                id:'fldDesc',
		                name:'fldDesc',
		                anchor: '100%'
		            },{
						xtype:'radiogroup',
						fieldLabel:'Additional fees',
						name:'fldAdditionalFee',
						id:'fldAdditionalFee',
						style:'padding-bottom:4px;border-bottom:1px solid #aaa;',
						columns:1,
						listeners:{
							change:function(rg,r){
								t.fee1=parseInt(r.getGroupValue().split('- Add $')[1]);
								t.getTotal();
							}
						},
						items:[{boxLabel: 'Oral or topical dose - Add $10', name: 'fldAdditionalFee', inputValue: 'Oral or topical dose - Add $10'},
						       {boxLabel: 'Oral/topical drug requiring some preparation (admixture, packaging, etc.) - Add $12', name: 'fldAdditionalFee', inputValue: 'Oral/topical drug requiring some preparation (admixture, packaging, etc.) - Add $12'},
						       {boxLabel: 'Parenteral (IM, SQ, etc.) drug - Add $20', name: 'fldAdditionalFee', inputValue: 'Parenteral (IM, SQ, etc.) drug - Add $20'},
						       {boxLabel: 'Parenteral (IV) drug requiring simple admixture - Add $25', name: 'fldAdditionalFee', inputValue: 'Parenteral (IV) drug requiring simple admixture - Add $25'},
						       {boxLabel: 'Parenteral drug with complex or time consuming admixture - Add $50', name: 'fldAdditionalFee', inputValue: 'Parenteral drug with complex or time consuming admixture - Add $50'}
						       ]
					},{
						xtype:'radiogroup',
						fieldLabel:'Multiple doses',
						style:'padding-bottom:4px;border-bottom:1px solid #aaa;',
						name:'fldAdditionalFeeDoses',
						id:'fldAdditionalFeeDoses',
						columns:1,
						listeners:{
							change:function(rg,r){
								t.fee2=parseInt(r.getGroupValue().split('- Add $')[1]);
								t.getTotal();
							}
						},
						items:[{boxLabel: 'Multiple doses (inpatient; oral or intravenous drugs) - Add $50', name: 'fldAdditionalFeeDoses', inputValue: 'Multiple doses (inpatient; oral or intravenous drugs) - Add $50'},
						       {boxLabel: 'Multiple doses (outpatient; 2 - 4 prescriptions per subject) - Add $30', name: 'fldAdditionalFeeDoses', inputValue: 'Multiple doses (outpatient; 2 - 4 prescriptions per subject) - Add $30'},
						       {boxLabel: 'Multiple doses (outpatient; > 4 prescriptions per subject) - Add $50', name: 'fldAdditionalFeeDoses', inputValue: 'Multiple doses (outpatient; > 4 prescriptions per subject) - Add $50'}
						       ]
					},{
						xtype:'radiogroup',
						fieldLabel:'Multiple drugs',
						name:'fldAdditionalFeeMultiple',
						id:'fldAdditionalFeeMultiple',
						columns:1,
						listeners:{
							change:function(rg,r){
								t.fee3=parseInt(r.getGroupValue().split('- Add $')[1]);
								t.getTotal();
							}
						},
						items:[{boxLabel: 'Multiple investigational drugs to dispense - Add $15', name: 'fldAdditionalFeeMultiple', inputValue: 'Multiple investigational drugs to dispense - Add $15'}]
					},{
		                xtype: 'numberfield',
		                fieldLabel: 'Cost',
		                id:'fldCost',
		                name:'fldCost',
		                anchor: '100%'
		            },
		            {
		                xtype: 'textarea',
		                anchor: '100%',
		                height:50,
		                id:'fldNotes',
		                name:'fldNotes',
		                fieldLabel: 'Notes'
		            }]
				}]
		};
		Clara.NewSubmission.ProtocolDrugStore.load();
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		Clara.Pharmacy.InvestigationalDrugComboWindow.superclass.initComponent.apply(this, arguments);
	}
});