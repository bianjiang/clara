Ext.ns('Clara.ReportableNewInformation');

Clara.ReportableNewInformation.ReportInformationPanel = Ext.extend(Ext.FormPanel, {
	id: 'reportable-new-information-report-information-panel',
	title:'Report Information',
	labelWidth: 125,
	border:false,
	/*
	defaults: {      // defaults applied to items

        layout: 'form',
        border: false,
        bodyStyle: 'padding:4px',
        
    },*/
	constructor:function(config){		
		Clara.ReportableNewInformation.ReportInformationPanel.superclass.constructor.call(this, config);
	},

	initComponent: function() {
		
		var config = {
				padding:6,
				items:[
				       {
				    	   xtype:'displayfield',
				    	   html:'<h1 style="font-size:18px;">Fields in <span class="extLblRequired">red are required.</span></h1>'
				       },
				   new Ext.form.FieldSet({
					   //title: 'Report Information',
					   autoHeight: true,
					   border:false,
					   labelStyle: 'font-weight:800',
					   items: [
							new Ext.form.DateField({	
							    fieldLabel: 'Date of Report',
							    width:150,
							    cls:'extLblRequired',
							    labelStyle:'font-weight:800;color:red;',
							    format:'Y-m-d',
							    allowBlank:false,
							    name: 'date-of-report',
							    id: 'date-of-report'
							}),
							new Ext.form.DateField({	
							    fieldLabel: 'Date Sponsor Notified',
							    width:150,
							    format:'Y-m-d',
							    allowBlank:false,
							    name: 'date-of-sponsor-notified',
							    id: 'date-of-sponsor-notified'
							}),
							new Ext.form.DateField({	
							    fieldLabel: 'Event Date',
							    width:150,
							    cls:'extLblRequired',
							    labelStyle:'font-weight:800;color:red;',
							    format:'Y-m-d',
							    allowBlank:false,
							    name: 'date-of-sae-onset',
							    id:'date-of-sae-onset'
							}),
							new Ext.form.RadioGroup({
                                fieldLabel: 'Report Type',
                                vertical: false,
                                id:'report-type',
                                items: [
                                        {boxLabel: 'Initial Report', name: 'report-type', inputValue: 'Initial Report'},
                                        {boxLabel: 'Follow-up Report', name: 'report-type', inputValue: 'Follow-up Report'}
                                ]
							}),
							new Ext.form.TextField({	
							    fieldLabel: 'Report #',
							    width:150,
							    name: 'report-number',
							    id: 'report-number'
							}),
							new Ext.form.TextField({	
							    fieldLabel: 'Participant Initials/ID',
							    width:150,
							    name: 'participant-id',
							    id: 'participant-id'
							})
							/*new Ext.form.DateField({	
							    fieldLabel: 'Initial Report Date',
							    width:150,
							    format:'Y-m-d',
							    allowBlank:false,
							    name: 'date-of-initial-report',
							    id: 'date-of-initial-report'
							}),*/
							/*new Ext.form.RadioGroup({
								id:'event-outcome',
                                fieldLabel: 'Event Outcome',
                                vertical: false,
                                items: [
                                        {boxLabel: 'Resolved', name: 'event-outcome', inputValue: 'Resolved'},
                                        {boxLabel: 'Stabilized', name: 'event-outcome', inputValue: 'Stabilized'},
                                        {boxLabel: 'Ongoing', name: 'event-outcome', inputValue: 'Ongoing'}
                                ]
							})*/
				   		]
					})
		    	],		
			    listeners:{}
		};
		
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		Clara.ReportableNewInformation.ReportInformationPanel.superclass.initComponent.apply(this, arguments);
		//Clara.ReportableNewInformation.DrugStore.removeAll();
	}
	

});
Ext.reg('clara-reportable-new-information-report-information-panel', Clara.ReportableNewInformation.ReportInformationPanel);

/*Clara.ReportableNewInformation.ParticipantInformationPanel = Ext.extend(Ext.FormPanel, {
	id: 'reportable-new-information-participant-information-panel',
	title:'Participant Information',
	labelWidth: 125,
	border:false,
	/*
	defaults: {      // defaults applied to items

        layout: 'form',
        border: false,
        bodyStyle: 'padding:4px',
        
    },
	constructor:function(config){		
		Clara.ReportableNewInformation.ParticipantInformationPanel.superclass.constructor.call(this, config);
	},

	initComponent: function() {
		
		var config = {
				items:[
				   new Ext.form.FieldSet({
					   //title: 'Report Information',
					   autoHeight: true,
					   border:false,
					   labelStyle: 'font-weight:800',
					   items: [							
							new Ext.form.TextField({	
							    fieldLabel: 'Participant Initials/ID',
							    width:150,
							    name: 'participant-id',
							    id: 'participant-id'
							}),
							new Ext.form.TextField({	
							    fieldLabel: 'Age/Date of Birth',
							    width:150,
							    name: 'dob',
							    id:'dob'
							}),
							new Ext.form.RadioGroup({
                                fieldLabel: 'Gender',
                                vertical: false,
                                id:'gender',
                                items: [
                                        {boxLabel: 'Male', name: 'gender', inputValue: 'Male'},
                                        {boxLabel: 'Female', name: 'gender', inputValue: 'Female'}
                                ]
							})
				   		]
					})
		    	],		
			    listeners:{}
		};
		
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		Clara.ReportableNewInformation.ParticipantInformationPanel.superclass.initComponent.apply(this, arguments);
		//Clara.ReportableNewInformation.DrugStore.removeAll();
	}
	

});
Ext.reg('clara-reportable-new-information-participant-information-panel', Clara.ReportableNewInformation.ParticipantInformationPanel);*/

/*Clara.ReportableNewInformation.ResolutionPanel = Ext.extend(Ext.FormPanel, {
	id: 'reportable-new-information-resolution-panel',
	title:'Resolution',
	labelWidth: 125,
	border:false,
	/*
	defaults: {      // defaults applied to items

        layout: 'form',
        border: false,
        bodyStyle: 'padding:4px',
        
    },
	constructor:function(config){		
		Clara.ReportableNewInformation.ResolutionPanel.superclass.constructor.call(this, config);
	},

	initComponent: function() {
		
		var config = {
				items:[
				   new Ext.form.FieldSet({
					   
					   autoHeight: true,
					   border:false,
					   labelStyle: 'font-weight:800',
					   items: [	
							new Ext.form.TextArea({	
							    fieldLabel: 'Action Taken',
							    width:300,
							    allowBlank:false,
							    name: 'action-taken',
							    id:'action-taken'
							})
				   		]
					})
		    	],		
			    listeners:{}
		};
		
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		Clara.ReportableNewInformation.ResolutionPanel.superclass.initComponent.apply(this, arguments);
		//Clara.ReportableNewInformation.DrugStore.removeAll();
	}
	

});
Ext.reg('clara-reportable-new-information-resolution-panel', Clara.ReportableNewInformation.ResolutionPanel);*/

Clara.ReportableNewInformation.RecommendationsPanel = Ext.extend(Ext.FormPanel, {
	id: 'reportable-new-information-recommendations-panel',
	title:'Additional Information',
	border:false,
	/*
	defaults: {      // defaults applied to items

        layout: 'form',
        border: false,
        bodyStyle: 'padding:4px',
        
    },*/
	constructor:function(config){		
		Clara.ReportableNewInformation.RecommendationsPanel.superclass.constructor.call(this, config);
	},

	initComponent: function() {
		
		var config = {
				items:[
				   new Ext.form.FieldSet({					   
					   autoHeight: true,
					   border:false,
					   items: [	
							/*new Ext.form.Label({								
								style: "font-weight:400;font-size:larger",
							    text: '1. Do you recommend a change to the protocol?'
							}),
							new Ext.form.RadioGroup({
                                vertical: false,
                                id:'change-to-protocol',
                                items: [
                                        {boxLabel: 'Yes', name: 'change-to-protocol', inputValue: 'yes'},
                                        {boxLabel: 'No', name: 'change-to-protocol', inputValue: 'no'}
                                ]
							}),
							new Ext.form.Label({								
								style: "font-weight:400;font-size:larger",
							    text: '2. Do you recommend a change to the site’s consent form? '
							}),
							new Ext.form.RadioGroup({
                                vertical: false,
                                id:'change-to-local-consent',
                                items: [
                                        {boxLabel: 'Yes', name: 'change-to-local-consent', inputValue: 'yes'},
                                        {boxLabel: 'No', name: 'change-to-local-consent', inputValue: 'no'}
                                ]
							}),
							new Ext.form.Label({								
								style: "font-weight:400;font-size:larger",
							    text: '3. Do you recommend a change to the study-wide consent form?'
							}),
							new Ext.form.RadioGroup({
                                vertical: false,
                                id:'change-to-global-consent',
                                items: [
                                        {boxLabel: 'Yes', name: 'change-to-global-consent', inputValue: 'yes'},
                                        {boxLabel: 'No', name: 'change-to-global-consent', inputValue: 'no'}
                                ]
							}),*/
							new Ext.form.TextArea({	
							    fieldLabel: 'Additional Information',
							    width:300,
							    cls:'extLblRequired',
							    labelStyle:'font-weight:800;color:red;',
							    height: 150,
							    allowBlank:false,
							    name: 'additional-information',
							    id:'additional-information'
							})
				   		]
					})
		    	],		
			    listeners:{}
		};
		
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		Clara.ReportableNewInformation.RecommendationsPanel.superclass.initComponent.apply(this, arguments);
		//Clara.ReportableNewInformation.DrugStore.removeAll();
	}
	

});
Ext.reg('clara-reportable-new-information-recommendations-panel', Clara.ReportableNewInformation.RecommendationsPanel);



Clara.ReportableNewInformation.DetailOfNewInformationWindow = Ext.extend(Ext.Window, {
	id: 'reportable-new-information-detail-of-new-information-window',
	height:400,
	width:500,
	newinformation:{},
	editing:false,
	modal:true,
	layout:'border',
	border:false,
	constructor:function(config){		
		Clara.ReportableNewInformation.DetailOfNewInformationWindow.superclass.constructor.call(this, config);
	},
	initComponent: function() {		
		var config = {
				items: [{
					xtype:'tabpanel',
					id:'tpDrugWizard',
					region:'center',
					deferredRender:false,
					activeTab:0,
					items:[
					       {xtype:'clara-reportable-new-information-report-information-panel'},
					       //{xtype:'clara-reportable-new-information-participant-information-panel'},
					       //{xtype:'clara-reportable-new-information-resolution-panel'},
					       {xtype:'clara-reportable-new-information-recommendations-panel'}
					       ]
				}],
				buttons: [
							{
								text:'Close',
								disabled:false,
								handler: function(){
									Ext.getCmp('reportable-new-information-detail-of-new-information-window').close();
								}
							},
							{
								text:'Save New Information',
								id:'btn-save-detail-of-new-information',
								disabled:false,
								handler: function(){
									var newinformation = Ext.getCmp('reportable-new-information-detail-of-new-information-window').newinformation;
									
									var n = new Clara.ReportableNewInformation.Event({
										id:newinformation.id,
										type:newinformation.type,
										dateofreport:(Ext.getCmp("date-of-report").getValue()?Ext.getCmp("date-of-report").getValue().format('Y-m-d'):''),
										dateofsponsornotified:(Ext.getCmp("date-of-sponsor-notified").getValue()?Ext.getCmp("date-of-sponsor-notified").getValue().format('Y-m-d'):''),
										dateofsaeonset:(Ext.getCmp("date-of-sae-onset").getValue()?Ext.getCmp("date-of-sae-onset").getValue().format('Y-m-d'):''),
										reporttype:(Ext.getCmp("report-type").getValue()?Ext.getCmp("report-type").getValue().getGroupValue():''),
										reportnumber:Ext.getCmp("report-number").getValue(),
										//dateofinitialreport:(Ext.getCmp("date-of-initial-report").getValue()?Ext.getCmp("date-of-initial-report").getValue().format('Y-m-d'):''),
										//eventoutcome:(Ext.getCmp("event-outcome").getValue()?Ext.getCmp("event-outcome").getValue().getGroupValue():''),
										participantid:Ext.getCmp("participant-id").getValue(),
										//dob:Ext.getCmp("dob").getValue(),
										//gender:(Ext.getCmp("gender").getValue()?Ext.getCmp("gender").getValue().getGroupValue():''),
										//actiontaken:Ext.getCmp("action-taken").getValue(),
										changetoprotocol:(Ext.getCmp("change-to-protocol").getValue()?Ext.getCmp("change-to-protocol").getValue().getGroupValue():''),
										changetolocalconsent:(Ext.getCmp("change-to-local-consent").getValue()?Ext.getCmp("change-to-local-consent").getValue().getGroupValue():''),
										chagnetoglobalconsent:(Ext.getCmp("change-to-global-consent").getValue()?Ext.getCmp("change-to-global-consent").getValue().getGroupValue():''),
										additionalinformation:Ext.getCmp("additional-information").getValue()
									});									
									
									if (n.dateofreport && n.dateofsaeonset && n.additionalinformation){
										
										clog(n.toXML());
										if (Ext.getCmp('reportable-new-information-detail-of-new-information-window').editing){
											var newinformationID = updateExistingXmlInProtocol('/reportable-new-info/detail-of-new-information/events/event', n.id, n.toXML());
										} else {
											var newinformationID = addXmlToProtocol( '/reportable-new-info/detail-of-new-information/events/event', n.toXML());
										}
										Clara.ReportableNewInformation.ReloadNewInformation ();
										Ext.getCmp('reportable-new-information-detail-of-new-information-window').close();
										
									} else {
										Ext.Msg.show({
											title: "Warning...",
											msg:'Please at least enter the <b>Date of Report</b>, <b>Event Date</b>, and provide <b>Additional Information</b> of this event!',
											buttons: Ext.Msg.OK
											});
									}
									
								}
				}],		
			    listeners:{
					afterrender:function(){
						if (this.editing){
							var newinformation = this.newinformation;							
							
							Ext.getCmp("date-of-report").setValue(newinformation.dateofreport);
							Ext.getCmp("date-of-sponsor-notified").setValue(newinformation.dateofsponsornotified);
							Ext.getCmp("report-type").setValue(newinformation.reporttype);
							Ext.getCmp("date-of-sae-onset").setValue(newinformation.dateofsaeonset);
							Ext.getCmp("report-number").setValue(newinformation.reportnumber);
							//Ext.getCmp("date-of-initial-report").setValue(newinformation.dateofinitialreport);
							//Ext.getCmp("event-outcome").setValue(newinformation.eventoutcome);
							Ext.getCmp("participant-id").setValue(newinformation.participantid);
							//Ext.getCmp("dob").setValue(newinformation.dob);
							//Ext.getCmp("gender").setValue(newinformation.gender);
							//Ext.getCmp("action-taken").setValue(newinformation.actiontaken);
							Ext.getCmp("change-to-protocol").setValue(newinformation.changetoprotocol);
							Ext.getCmp("change-to-local-consent").setValue(newinformation.changetolocalconsent);
							Ext.getCmp("change-to-global-consent").setValue(newinformation.chagnetoglobalconsent);
							Ext.getCmp("additional-information").setValue(newinformation.additionalinformation);
							
						}
					}
				}
		};
		
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		Clara.ReportableNewInformation.DetailOfNewInformationWindow.superclass.initComponent.apply(this, arguments);
		

		
	}
	

});
Ext.reg('clara-reportable-new-information-detail-of-new-information-window', Clara.ReportableNewInformation.DetailOfNewInformationWindow);



