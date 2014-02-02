Ext.ns('Clara.NewSubmission');

Clara.NewSubmission.ProtocolDrugSearchPanel = Ext.extend(Ext.Panel, {
	id: 'protocol-drug-search-panel',
	title:'Search',
	layout:'border',
	readOnly:false,
	border:false,
	searchKeyword:"",
	constructor:function(config){		
		Clara.NewSubmission.ProtocolDrugSearchPanel.superclass.constructor.call(this, config);
	},

	initComponent: function() {
		var t=this;
		var config = {
				items: [{
					xtype:'uxsearchfield',
					store:Clara.NewSubmission.DrugStore,
					allowBlank:false,
					region:'north',
					border:false,
					frame:false,
					disabled:t.readOnly,
					height:24,
					name:'new-drug-name',
			
					minLength:4,
					id:'new-drug-name',
					emptyText:'Enter the name of the drug you wish to add',
					paramName : 'name',
		        	beforeClear:function(){
		        		Clara.NewSubmission.DrugStore.setBaseParam('name','');
		        	},
		        	listeners:{
		        		change:function(f,v,ov){
		        			t.SearchKeyword = v;
		        		}
		        	}
				},{
					xtype:'grid',
			    	frame:false,
			    	border:false,
			    	trackMouseOver:false,
			    	region: 'center',
			        store: Clara.NewSubmission.DrugStore,
			        selModel: new Ext.grid.RowSelectionModel({
			        	singleSelect:true,
			        	listeners: {
			        		rowselect: function(grid,rowIndex,record){
			        					drec = record.data;
			        					
			        					var existingStudyDrug = Ext.getCmp("protocol-drug-window").drug;
			        					if (existingStudyDrug && Ext.isDefined(existingStudyDrug.name)){
			        						existingStudyDrug.name = drec.description;
			        						existingStudyDrug.identifier = drec.id;
			        						existingStudyDrug.approved = drec.approved;
			        					}
			        					else {
			        						Ext.getCmp("protocol-drug-window").drug = new Clara.NewSubmission.Drug({
				        						name:drec.description,
				        						identifier:drec.id,
				        						approved:drec.approved
				        					});;
			        					}
			        					jQuery('#new-drug-name').val(drec.description);
			        					if (jQuery.trim(jQuery("#new-drug-name").val()) == '') {
						   					Ext.getCmp('protocol-drug-detail-panel').setDisabled(true);
						   					Ext.getCmp('btn-save-drug').setDisabled(true);
						   				} else {
						   					if (!t.readOnly) Ext.getCmp('btn-save-drug').setDisabled(false);
						   					Ext.getCmp('protocol-drug-detail-panel').setDisabled(false);
						   				}
			        				}
			        	}
			        }),
			        loadMask: new Ext.LoadMask(Ext.getBody(), {msg:"Please wait..."}),
			        viewConfig: {
						forceFit:true,
			    		getRowClass: function(record, index){
			    			var approved = record.get('approved');
			    			if (approved) { return 'drug-search-approved'; }
			    			else { return 'drug-search-not-approved'; }
			    		}
			    	},
			    	
			        columns: [new Ext.grid.RowNumberer({width: 30}),
			                  {
			                	  	header:'Drug Name',
			                	  	dataIndex:'description',
			                	  	sortable:true
			                  }
			        ]
				},{
					   xtype:'panel',
					   region:'south',
					   padding:6,
					   border:false,
					   unstyled:true,
					   style:'border-top:1px solid #99bbe8',
					   items:[{
						   xtype:'checkbox',
						   disabled:t.readOnly,
						   boxLabel:"<span style='font-weight:800;font-size:14px;'>The drug wasn't found in the search results above.</span><br/>By checking this box, the name you entered above will be added as a new drug, pending IRB approval.",
						   id:'custom-drug-name',
						   name:'custom-drug-name',
						   listeners:{
							   check:function(cb,v){
								   if (v == true){
									   if (jQuery.trim(jQuery("#new-drug-name").val()) == '') {
						   					Ext.getCmp('protocol-drug-detail-panel').setDisabled(true);
						   					
						   					Ext.getCmp('btn-save-drug').setDisabled(true);
						   				} else {
						   					Ext.getCmp('btn-save-drug').setDisabled(false);
						   					Ext.getCmp('protocol-drug-detail-panel').setDisabled(false);
						   					
						   				}
			        					var drug = new Clara.NewSubmission.Drug({
			        						name:jQuery.trim(jQuery("#new-drug-name").val()),
			        						identifier:null,
			        						approved:false
			        					});
			        					Ext.getCmp("protocol-drug-window").drug = drug;
			        					jQuery('#new-drug-name').val(drug.name);
								   } else {
									   Ext.getCmp("protocol-drug-window").drug = null;
									   jQuery('#new-drug-name').val('');
								   }
							   }
						   }
					   }]
				}],		
			    listeners:{}
		};
		
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		Clara.NewSubmission.ProtocolDrugSearchPanel.superclass.initComponent.apply(this, arguments);
		Clara.NewSubmission.DrugStore.removeAll();
	}
	

});
Ext.reg('claraprotocoldrugsearchpanel', Clara.NewSubmission.ProtocolDrugSearchPanel);



Clara.NewSubmission.ProtocolDrugDetailPanel = Ext.extend(Ext.FormPanel, {
	id: 'protocol-drug-detail-panel',
	title:'Details',
	border:false,
	disabled:true,
	padding:6,
	readOnly:false,
	labelWidth: 200,
	collapsed:false,
	collapsible:false,
	
	constructor:function(config){		
		Clara.NewSubmission.ProtocolDrugDetailPanel.superclass.constructor.call(this, config);
	},

	initComponent: function() {
		var t=this;
		var config = {
				items:[ 
{
	xtype:'radiogroup',
	id:'drug-has-insertbrochure',
    fieldLabel: 'Does this drug have any of the following',
    disabled:t.readOnly,
    items:[
           {boxLabel:'Package Insert',inputValue:'drug-has-insert',name: 'drug-has-insertbrochure'},
           {boxLabel:'Investigators Brochure',inputValue:'drug-has-investigators-brochure',name: 'drug-has-insertbrochure'},
           {boxLabel:'Neither',inputValue:'neither',name: 'drug-has-insertbrochure'}
           ]
},
				      
						{
							xtype:'combo',
							width:240,
		    	    	   	fieldLabel:"What is the drug's current status?",
		    	    	   	typeAhead:false,
				        	store:Clara.NewSubmission.DrugStatusStore, 
				        	forceSelection:true,
				        	displayField:'desc', 
				        	valueField:'value',
				        	mode:'remote', 
				        	triggerAction:'all',
				        	editable:false,
				        	allowBlank:false,
				        	name:'drug-status',
				        	id:'drug-status'
						},
						{
							xtype:'combo',
		    	       		width:240,
		    	    	   	fieldLabel:"How will this drug be administered?",
		    	    	   	typeAhead:false,
				        	store:Clara.NewSubmission.DrugAdminStore, 
				        	disabled:t.readOnly,
				        	forceSelection:false,
				        	displayField:'desc', 
				        	valueField:'value',
				        	mode:'remote', 
				        	triggerAction:'all',
				        	editable:true,
				        	allowBlank:false,
				        	name:'drug-administration',
				        	id:'drug-administration'
				        },
				        {	
				        	xtype:'textarea',
						    fieldLabel: 'Location of Treatment Center (e.g., Chemo infusion center, CRC, etc.)',
						    width:250,
						    disabled:t.readOnly,
						    id: 'drug-treatment-center',
						    name: 'drug-treatment-center'
						},
				        {
				        	xtype:'radiogroup',
				        	fieldLabel: 'Is the drug provided for this study?',
						    id:'drug-provided',
						    columns:[50,50],
						    items:[
						           {boxLabel:'Yes',inputValue:'y',name: 'drug-provided',width:50},
						           {boxLabel:'No',inputValue:'n',name: 'drug-provided',width:50}
						           ]
				        },
				        {
				        	xtype:'textfield',
				        	fieldLabel: 'What is the <span style="font-weight:800;">provider\'s name</span>?',
						    id: 'drug-provider',
						    name: 'drug-provider',
						    width:400
				        },
						{	xtype:'textfield',

							disabled:t.readOnly,
						    fieldLabel: 'What is the <span style="font-weight:800;">dosage form</span> that will be provided?',
						    id: 'drug-provider-dosage',
						    name: 'drug-provider-dosage',
						    width:400
						},
                    {
                        xtype:'textfield',
                        fieldLabel: 'What is the <span style="font-weight:800;">IND number</span>?',
                        id: 'drug-ind-number',
                        name: 'drug-ind-number'
                    },
                    {
                        xtype:'textfield',
                        fieldLabel: 'What is the <span style="font-weight:800;">NSC number</span>?',
                        id: 'drug-nsc-number',
                        hidden:true,
                        name: 'drug-nsc-number'
                    }
		    	       
		    	]
		};
		
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		Clara.NewSubmission.ProtocolDrugDetailPanel.superclass.initComponent.apply(this, arguments);
		
	}
	

});
Ext.reg('claraprotocoldrugdetailpanel', Clara.NewSubmission.ProtocolDrugDetailPanel);



Clara.NewSubmission.ProtocolDrugWindow = Ext.extend(Ext.Window, {
	id: 'protocol-drug-window',
	height:500,
	width:750,
	drug:{},
	drugType:'',
	editing:false,
	readOnly:false,
	modal:true,
	layout:'fit',
	border:false,
	constructor:function(config){		
		Clara.NewSubmission.ProtocolDrugWindow.superclass.constructor.call(this, config);
	},

	initComponent: function() {
		
		var t = this;
		t.setTitle((this.editing)?"'"+t.drug.name+"'":"Add "+t.drugType+" drug.."); 
		clog("new window.. readonly?",t.readOnly);
		var config = {
				items: [{
					xtype:'tabpanel',
					id:'tpDrugWizard',
					//region:'center',
					deferredRender:false,
					activeTab:0,
					items:[{xtype:'claraprotocoldrugsearchpanel',readOnly:t.readOnly},{xtype:'claraprotocoldrugdetailpanel',readOnly:t.readOnly}]
				}],
				buttons: [
							{
								text:'Close',
								disabled:false,
								handler: function(){
									Ext.getCmp('protocol-drug-window').close();
								}
							},
							{
								text:'Save Drug',
								id:'btn-save-drug',
								disabled:true,
								handler: function(){
									var drug = Ext.getCmp('protocol-drug-window').drug;
									if (!t.editing) t.drug.type = t.drugType;
									if (typeof drug.name != undefined && drug.name !=''){
										var d = new Clara.NewSubmission.Drug({
											id:drug.id,
											identifier:drug.identifier,
											approved:drug.approved || false,
											name:drug.name,
											type:drug.type,
											status:jQuery("#drug-status").val(),
											administration:jQuery("#drug-administration").val(),
											isprovided:jQuery("input:radio[name=drug-provided]:checked").val(),
											insert:(jQuery("input:radio[name=drug-has-insertbrochure]:checked").val() == "drug-has-insert")?"y":"n",
											brochure:(jQuery("input:radio[name=drug-has-insertbrochure]:checked").val() == "drug-has-investigators-brochure")?"y":"n",
                                            provider:jQuery("#drug-provider").val(),
                                            ind:jQuery("#drug-ind-number").val(),
                                            nsc:jQuery("#drug-nsc-number").val(),
											providerdosage:jQuery("#drug-provider-dosage").val(),
											treatmentcenterlocation:jQuery("#drug-treatment-center").val()
										});
										
										
										
										clog(d.toXML());
										if (Ext.getCmp('protocol-drug-window').editing){
											var drugID = updateExistingXmlInProtocol("/protocol/drugs/drug", d.id, d.toXML());
										} else {
											var drugID = addXmlToProtocol( "/protocol/drugs/drug", d.toXML(), "drug");
										}
										Clara.NewSubmission.ReloadDrugs();
										Ext.getCmp('protocol-drug-window').close();
									} else {
										alert('Choose a drug first.');
									}
								}
				}],		
			    listeners:{
					afterrender:function(){
						if (this.editing){
							clog("FILLING OUT DRUG");
							clog(this.drug);

							Ext.getCmp("new-drug-name").setValue(this.drug.name);
							Ext.getCmp("drug-status").setValue(this.drug.status);
							Ext.getCmp("drug-administration").setValue(this.drug.administration);
							
							Ext.getCmp('drug-provided').setValue((this.drug.isprovided == true)?"y":"n");
							Ext.getCmp('custom-drug-name').setValue((this.drug.approved == true)?"y":"n");
							Ext.getCmp('drug-has-insertbrochure').setValue((this.drug.insert == true)?"drug-has-insert":(this.drug.brochure == true)?"drug-has-investigators-brochure":"neither");

                            Ext.getCmp("drug-provider").setValue(this.drug.provider);
                            Ext.getCmp("drug-ind-number").setValue(this.drug.ind);
                            Ext.getCmp("drug-nsc-number").setValue(this.drug.nsc);
							Ext.getCmp("drug-provider-dosage").setValue(this.drug.providerdosage);

							Ext.getCmp("drug-treatment-center").setValue(this.drug.treatmentcenterlocation);
							
							
							// Redmine #2674: Allow editing of saved drug
							Ext.getCmp("protocol-drug-search-panel").enable();
							
							Ext.getCmp("protocol-drug-detail-panel").enable();
							if (!this.readOnly) {
								Ext.getCmp("btn-save-drug").enable();
							} else {

								Ext.getCmp("new-drug-name").setDisabled(true);
								Ext.getCmp("drug-status").setDisabled(true);
								Ext.getCmp("drug-provider").setDisabled(true);
								Ext.getCmp("drug-administration").setDisabled(true);
								Ext.getCmp('drug-has-insertbrochure').setDisabled(true);
								Ext.getCmp('custom-drug-name').setDisabled(true);
                                Ext.getCmp("drug-provided").setDisabled(true);
                                Ext.getCmp("drug-ind-number").setDisabled(true);
                                Ext.getCmp("drug-nsc-number").setDisabled(true);
								Ext.getCmp("drug-provider-dosage").setDisabled(true);

								Ext.getCmp("drug-treatment-center").setDisabled(true);
								
							}
							Ext.getCmp("tpDrugWizard").setActiveTab(1);
						}
					}
				}
		};
		clog("about to apply config");
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		Clara.NewSubmission.ProtocolDrugWindow.superclass.initComponent.apply(this, arguments);
		

		
	}
	

});
Ext.reg('claraprotocoldrugwindow', Clara.NewSubmission.ProtocolDrugWindow);



