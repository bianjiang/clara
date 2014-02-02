Ext.ns('Clara.NewSubmission.SubjectPopulation');

Clara.NewSubmission.SubjectPopulation.CriteriaStore = new Ext.data.GroupingStore({
	id:'subjectPopulationCriteriaStore',
	proxy: new Ext.data.HttpProxy({
		url: appContext + "/ajax/protocols/" + claraInstance.id + "/protocol-forms/" + claraInstance.form.id + "/protocol-form-xml-datas/" + claraInstance.form.xmlDataId + "/xml-elements/list",
		method:"GET",
		headers:{'Accept':'application/xml;charset=UTF-8'}
	}),
	reader: new Ext.data.XmlReader({
	record: 'criteria', 
	fields: [
		{name:'id', mapping:'@id'},
		{name:'index', mapping:'@index'},
		{name:'type',mapping:'@type'},
		{name:'value', mapping:'value'}
	]}),
	groupField:'type'
});



Clara.NewSubmission.SubjectPopulation.CriteriaGridPanel = Ext
		.extend(
				Ext.grid.GridPanel,
				{
					id : 'protocol-subject-population-criteria-gridpanel',
					frame : false,
					border : true,
					selectedCriteria : {},
					population: {},
					editing:false,
					populationId:null,
					constructor : function(config) {
						Clara.NewSubmission.SubjectPopulation.CriteriaGridPanel.superclass.constructor
								.call(this, config);
					},

					getMaxIndex : function() {
						var st = this.getStore();
						var maxidx = 0;
						st.each(function(rec) {
							maxidx = (rec.get("index") && parseFloat(rec
									.get("index")) > maxidx) ? parseFloat(rec
									.get("index")) : maxidx;
						});
						return maxidx;
					},

					loadCriteria : function() {
						var t =this;
						var ecgp = Clara.NewSubmission.SubjectPopulation.CriteriaStore;
						
						ecgp.rejectChanges(); // to clear local record add?
						ecgp.removeAll();
						ecgp
								.load({
									params : {
										listPath : '/protocol/study-subject-populations/population[@id="' + t.populationId + '"]/criterias/criteria'
									}
								});
					},

					initComponent : function() {
						var t = this;
						var config = {
							border : true,
							trackMouseOver : false,
							store : Clara.NewSubmission.SubjectPopulation.CriteriaStore,
							view : new Ext.grid.GroupingView(
									{
										forceFit : true,
										showGroupName : false,
										groupTextTpl : '{text} ({[values.rs.length]} criteria)'
									}),

							sm : new Ext.grid.RowSelectionModel({
								singleSelect : true
							}),
							loadMask : new Ext.LoadMask(Ext.getBody(), {
								msg : "Please wait..."
							}),
							columns : [

							{
								header : 'Type',
								dataIndex : 'type',
								sortable : false,
								hidden : true,
								width : 100
							}, {
								header : 'Criteria',
								dataIndex : 'value',
								sortable : false
							} ],

							listeners : {
								rowclick : function(grid, rowI, event) {
									var criteria = grid.getStore().getAt(
											rowI);
									t.selectedCriteria = criteria;
									Ext.getCmp("btnRemoveSubjectCriteria").enable();
								},
								rowdblclick: function(grid,rowI,e){
									var pop = grid.getStore().getAt(rowI);
									clog("EDITING REC",pop);
									new Clara.NewSubmission.SubjectPopulation.AddPopulationWindow({model:true, editRec:pop});
								}
							}
						};
						
						if(t.population.length > 0){
							loadCriteria();
						}

						Ext.apply(this, Ext.apply(this.initialConfig, config));
						Clara.NewSubmission.SubjectPopulation.CriteriaGridPanel.superclass.initComponent
								.apply(this, arguments);

					}

				});
Ext.reg('clara-protocol-population-criteria-gridpanel',
		Clara.NewSubmission.SubjectPopulation.CriteriaGridPanel);

Clara.NewSubmission.SubjectPopulation.AddPopulationWindow = Ext
		.extend(
				Ext.Window,
				{
					id : 'winAddPopulation',
					title : 'Subject Population',
					width : 600,
					height : 240,
					modal : true,
					criteriaForEachSubject: false,
					selectedCriteria:{},
					editRec:null,
					layout : 'fit',
					population: {},
					populationId:null,
					editing:false,
					constructor : function(config) {
						Clara.NewSubmission.SubjectPopulation.AddPopulationWindow.superclass.constructor
								.call(this, config);
					},
					initComponent : function() {
						
					    var t = this;
					    clog("new window",t);
					    if (t.editing){
					    	t.populationId = t.population.get("id");
					    }
						var config = {
							buttons : [ {
								text : 'Close',
								disabled : false,
								handler : function() {
									t.close();
								}},{
									text : 'Save',
									disabled : false,
									handler : function() {
										
										var populationName = Ext.getCmp("fldPopulationName").getValue();  
										var populationDescription = Ext.getCmp("fldPopulationDesc").getValue(); 
										
										if (jQuery.trim(populationName).length < 1) {
											alert("Please provide a population name!");
										} else {
											var cp = Ext.getCmp("population-criteria-gridpanel");
											clog("gridpanel",cp);
											var criteriaxml=(cp && cp.store.reader.xmlData)?XMLObjectToString(cp.store.reader.xmlData).replace('<list>','').replace('</list>',''):'';
											var xml = '<population name="'+Encoder.htmlEncode(populationName)+'"><description>'+Encoder.htmlEncode(populationDescription)+"</description>"+criteriaxml+'</population>';
											clog(xml);
											if (t.editing) updateExistingXmlInProtocol("/protocol/study-subject-populations/population", t.populationId, xml);
											else addXmlToProtocol("/protocol/study-subject-populations/population",xml);
											popCriteriaPanel.loadCriteria();
											t.close();
										}
									}
							
							}],
							items : [
									{
										xtype : 'panel',
										frame : false,
										layout : 'fit',
										border : false,
										items : [ {
											region : 'north',
											xtype : 'form',
											border : false,
											labelWidth : 80,
											padding : 16,
											plain : true,
											collapsed : false,
											collapsible : false,
											
											items : [
															{
																xtype:'textarea',
																fieldLabel : '<span style="font-weight:800;">Population</span>',
																id : 'fldPopulationName',
																name : 'population-name',
																width:450,
																value:(t.editing?t.population.get("name"):"")
															},{
																xtype:'textarea',
																fieldLabel : '<span style="font-weight:800;">Description</span>',
																id : 'fldPopulationDesc',
																name : 'population-desc',
																width:450,
																value:(t.editing?t.population.get("description"):"")
															}]
										}]

									} ]
						};

						/*
						 * approvedSiteStore.load({ params : { common : true }
						 * });
						 */
						Ext.apply(this, Ext.apply(this.initialConfig, config));

						Clara.NewSubmission.SubjectPopulation.AddPopulationWindow.superclass.initComponent
								.apply(this, arguments);
						
						if (t.editRec != null) {
							// prefill fields
							clog("i'd prefill fields here");
						}

					}
				});
Ext.reg('clara-newsubmission-subjectpopulation-add-population-window',
		Clara.NewSubmission.SubjectPopulation.AddPopulationWindow);

var selectedSubjectPopulation = {};
Clara.NewSubmission.SubjectPopulation.GridPanel = Ext
		.extend(
				Ext.grid.GridPanel,
				{
					criteriaForEachSubject : false,
					selectedSubjectPopulation: {},
					store : {},
					width : 800,
					height : 400,
					setCriteriaHidden: function(hide){
						var t = this;
						t.criteriaForEachSubject = !hide;
						t.getColumnModel().setHidden(1,hide);
					},
					loadCriteria:function(){
						var ecgp = this.store;
						ecgp.rejectChanges();	// to clear local record add?
						ecgp.removeAll();
						ecgp.load({params:{listPath:'/protocol/study-subject-populations/population'}});
					},
					constructor : function(config) {
						Clara.NewSubmission.SubjectGridCriteriaPanel.superclass.constructor
								.call(this, config);
					},
					initComponent : function() {
						var t = this;
						var config = {
							stripeRows : true,
							itemId : 'gridSubjectPopulation',
							id : 'gridSubjectPopulations',
							view : new Ext.grid.GridView({

							}),
							listeners : {
								rowclick : function(grid,
										rowIndex, e) {
									var record = grid.store.getAt(rowIndex);
									selectedSubjectPopulation = record;
									Ext.getCmp("btn-remove-population")
											.enable();
								},
								rowdblclick: function(grid,idx,e){
									
									t.selectedSubjectPopulation = grid.store.getAt(idx);
									new Clara.NewSubmission.SubjectPopulation.AddPopulationWindow({editing:true, population:selectedSubjectPopulation,criteriaForEachSubject:t.criteriaForEachSubject}).show();
								}

							},
							selModel : new Ext.grid.RowSelectionModel(
									{
										singleSelect : true
									}),
							columns : [
									{
										xtype : 'gridcolumn',
										header : 'Population',
										dataIndex : 'name',
										sortable : false,
										width : 200
									},{
										xtype : 'gridcolumn',
										header : 'Description',
										dataIndex : 'description',
										sortable : false,
										width : 400
									} ],
							tbar : {
								xtype : 'toolbar',
								items : [

										{
											xtype : 'button',
											text : 'New Subject Population',
											iconCls : 'icn-user--plus',
											handler : function() {
												addPopulationWindow = Ext
														.getCmp("winAddPopulation");
												if (!addPopulationWindow) {
													addPopulationWindow = new Clara.NewSubmission.SubjectPopulation.AddPopulationWindow({model:true});
												}
												addPopulationWindow.show();
											}
										},
										{
											xtype : 'tbseparator'
										},
										{
											xtype : 'button',
											disabled : true,
											text : 'Remove Subject Population',
											id : 'btn-remove-population',
											iconCls : 'icn-user--minus',
											handler : function() {
												
												Ext.MessageBox
														.confirm(
																'Remove Subject Population',
																'Are you sure you want to do remove this subject population?',
																function(a) {
																	if (a == "yes") {
																		removeXmlFromProtocol(
																				'/'
																						+ claraInstance.form.xmlBaseTag
																						+ '/study-subject-populations/population',
																				selectedSubjectPopulation.data.id);
																				t.loadCriteria();
																		Ext
																				.getCmp(
																						"btn-remove-population")
																				.disable();
																	}

																});

											}
										} ]
							}

						};

						Ext.apply(this, Ext.apply(this.initialConfig, config));
						Clara.NewSubmission.SubjectGridCriteriaPanel.superclass.initComponent
								.apply(this, arguments);
						
							this.loadCriteria();
						//this.store.load({params:{listPath:'/protocol/recruiting-criteria/criteria'}});
					}
				});
Ext.reg('claraprotocolcriteriagridpanel',
		Clara.NewSubmission.SubjectGridCriteriaPanel);