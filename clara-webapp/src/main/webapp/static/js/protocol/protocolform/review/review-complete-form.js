Ext.ns('Clara.Reviewer');


Clara.Reviewer.ClearFormValues = function(){
	// try to clear all form elements on the extra content panel
	var p = Ext.getCmp("finalreviewpanel");
	p.items.each(function(i){
		i.getForm().reset();
	});
};

// Final Review Panel: Will contain certain xtypes, with each xtype
// pre-populated with data from xml

Clara.Reviewer.FinalReviewPanel = Ext.extend(Ext.Panel, {

	id : 'finalreviewpanel',
	reviewFormName : '',
	reviewFormType : claraInstance.type, // or 'contract'
	reviewPanelXtypes : [],
	constructor : function(config) {
		Clara.Reviewer.FinalReviewPanel.superclass.constructor.call(this, config);
	},

	validate : function() {
		var t = this;
		var valid = true;
		clog(t.items.items);
		for ( var i = 0; i < t.items.items.length; i++) {
			valid = valid && t.items.items[i].validate();
		}
		return valid;
	},

	getXML : function() {
		var t = this;
		//var xml = "<" + t.reviewFormType + "-" + t.reviewFormName + ">";
		var xml = "";
		clog(t.items.items);
		for ( var i = 0; i < t.items.items.length; i++) {
			xml += t.items.items[i].getXML();
		}
		//xml += "</" + t.reviewFormType + "-" + t.reviewFormName + ">"
		return xml;
	},

	initComponent : function() {
		var t = this;
		var config = {
			border : false
		};

		// apply config
		Ext.apply(this, Ext.apply(this.initialConfig, config));

		// call parent
		Clara.Reviewer.FinalReviewPanel.superclass.initComponent.apply(this,
				arguments);

		var xml;
		// AJAX call to get XML for this form, then pass the XML for each xtype
		// to each type item
		jQuery.ajax({
			// url:appContext+'/static/xml/samples/final-review-panel.xml', //
			// TODO: replace with real controller url
			url : appContext + "/ajax/"+claraInstance.type+"s/"
					+ claraInstance.id
					+ "/"+claraInstance.type+"-forms/"
					+ claraInstance.form.id
					+ "/review/committee-review-form",
			type : "GET",
			data : {
				reviewFormIdentifier : t.reviewFormName,
				reviewFormType : claraInstance.form.type
			},
			async : false,
			dataType : 'xml',
			success : function(data) {
				xml = data;
			}
		});

		// read xtypes, generate items

		jQuery(xml).find('panels').find('panel').each(function() {
			var p = this;
			var px = jQuery(p).attr('xtype');
			var pid = jQuery(p).attr('id');
			clog("PR ADDING XTYPE "+px);
			t.add({
				xtype : px,
				id : pid,
				reviewFormType : t.reviewFormType,
				reviewPanelXml : p,
				padding : 6
			});
		});

		t.doLayout();

	}

});
Ext.reg('clarareviewerfinalreviewpanel', Clara.Reviewer.FinalReviewPanel);

Clara.Reviewer.CommitteeActionPanel = Ext
		.extend(
				Ext.FormPanel,
				{
					id : 'CommitteeActionPanel',
					reviewPanelXml : {},
					title : 'Actions',
					reviewFormType : claraInstance.type, // or 'contract'
					constructor : function(config) {
						Clara.Reviewer.CommitteeActionPanel.superclass.constructor
								.call(this, config);
					},
					validate : function() {
						var t = this;
						var cbs = Ext.getCmp("rbactiongroup").getValue();
						if (cbs == null)
							return false;
						else
							return true;
					},
					getXML : function() {
						var t = this;
						//var xml = "<" + t.id + ">";
						//var xml = ""
						//xml += t.getFormXMLString();
						//xml += "</" + t.id + ">";
						return t.getFormXMLString();
					},
					getFormXMLString : function() {
						var t = this;
						var xml = "<actions>";
						var cbs = Ext.getCmp("rbactiongroup").getValue();
						clog(cbs);
						if (cbs != null)
							xml += "<action>" + cbs.value + "</action>";
						xml += "</actions>";
						return xml;
					},
					initComponent : function() {
						var t = this;
						var config = {};

						// apply config
						Ext.apply(this, Ext.apply(this.initialConfig, config));

						// call parent
						Clara.Reviewer.CommitteeActionPanel.superclass.initComponent
								.apply(this, arguments);

						if (t.reviewPanelXml) {
							var xml = t.reviewPanelXml;

							var rbItems = [];

							jQuery(xml)
									.find("actions")
									.find("action")
									.each(
											function() {
												var checked = (jQuery(this)
														.find("checked").text() == "true") ? true
														: false;
												var desc = jQuery(this).find(
														"desc").text();
												var name = jQuery(this).find(
														"name").text();

												// add to form
												rbItems.push({
													boxLabel : desc,
													name : 'action',
													value : name,
													checked : checked
												});

											});

							clog("rbItems", rbItems);

							t.add({
								xtype : 'radiogroup',
								hideLabel : true,
								id : 'rbactiongroup',
								items : rbItems
							});

							t.doLayout();

						} else {
							clog(
									"[ERROR] Clara.Reviewer.CommitteeActionPanel.initComponent(): reviewPanelXml not defined",
									t);
						}
					}

				});
Ext.reg('clarareviewercommitteeactionpanel',
		Clara.Reviewer.CommitteeActionPanel);

Clara.Reviewer.GatekeeperSendNow = function(c,desc){
	clog("will send to "+c);
	new Ext.Window({
		id:'winGatekeeperSendNowWindow',
		width:500,
		title:"Send protocol to "+desc,
		layout:"form",
		bodyPadding:6,
		labelAlign:'top',
		labelSeparator:'',
		modal:true,
		padding:6,
		buttons:[{
			text:'Cancel',
			handler:function(){
				Ext.getCmp("winGatekeeperSendNowWindow").close();
			}
		},{
			text:'Send',
			handler:function(){
				clog(Ext.getCmp("gkNote").getValue());
				var btn = this;
				btn.setDisabled(true);
				btn.setText("Sending, please wait..");
				jQuery.ajax({
					//url:appContext+'/static/xml/samples/final-review-panel.xml',
					url : appContext + "/ajax/"+claraInstance.type+"s/"
							+ claraInstance.id
							+ "/"+claraInstance.type+"-forms/"
							+ claraInstance.form.id
							+ "/review/"
							+ claraInstance.form.urlName
							+ "/committee-review/assign-committee"
							,
					type : "POST",
					data : {
						xmlData: "<committee-review><invovled-committees><committee>"+c+"</committee></invovled-committees></committee-review>",
						note: Ext.getCmp("gkNote").getValue(),
						userId: claraInstance.user.id,
						committee: claraInstance.user.committee,
						action:'ASSIGN_TO_INDIVIDUAL_COMMITTEE'
					},
					async : false,
					dataType : 'json',
					error: function(j,t,e){
						cwarn("ERROR AJAX",j,t,e);
						btn.setDisabled(false);
						btn.setText("Send");
					},
					success : function(data) {
						// need to disable the field on ui, then close
						var cb = Ext.getCmp("fldAssign_"+c);
						cb.wrap.child('.x-form-cb-label').update(desc+" (Sent for review)");
						cb.setDisabled(true);
						Ext.getCmp("winGatekeeperSendNowWindow").close();
					}
				});
				
				
			}
		}],
		items:[{
			xtype:'displayfield',
			hideLabel:true,
			value:'<span style="font-size:16px;">This will send to <strong>'+desc+'</strong> immediately, without affecting your review as Gatekeeper.</span>'
		},{
			xtype:'textarea',
			id:'gkNote',
			anchor:'100%',
			fieldLabel:'<span style="font-weight:800;">Note to committee</span><br/>This will appear in the history for this form.'
		}]
	}).show();
};

Clara.Reviewer.GatekeeperAssignCommitteePanel = Ext
		.extend(
				Ext.FormPanel,
				{

					id : 'GatekeeperAssignCommitteePanel',
					reviewPanelXml : {},
					title : 'Gatekeeper: Assign committees for this protocol',
					reviewFormType : 'protocol', // or 'contract'
					constructor : function(config) {
						Clara.Reviewer.GatekeeperAssignCommitteePanel.superclass.constructor
								.call(this, config);
					},
					validate : function() {
						return true;
					},
					getXML : function() {
						var t = this;
						//var xml = "<" + t.id + ">";
						//xml += t.getFormXMLString();
						//xml += "</" + t.id + ">";
						return t.getFormXMLString();
					},

					getFormXMLString : function() {
						var t = this;
						var xml = "<invovled-committees>";
						var cbs = Ext.getCmp("cbcommitteegroup").getValue();
						for ( var i = 0; i < cbs.length; i++) {
							if (!cbs[i].disabled) xml += "<committee>" + cbs[i].getName() + "</committee>";
						}
						xml += "</invovled-committees>";
						return xml;
					},

					initComponent : function() {
						var t = this;
						var config = {};

						if (t.reviewPanelXml) {
							var xml = t.reviewPanelXml;

							var cbItems = [];

							jQuery(xml)
									.find("committees")
									.find("committee")
									.each(
											function() {
												var checked = (jQuery(this)
														.find("checked").text() == "true") ? true
														: false;
												var recommended = (jQuery(this)
														.find("recommended")
														.text() == "true") ? true
														: false;
												var desc = jQuery(this).find(
														"desc").text();
												var name = jQuery(this).find(
														"name").text();

												var disabled = (jQuery(this)
														.find("assigned").text() == "true") ? true
																: false;
												if (disabled){
													var assignedStatus = jQuery(this).find("status").text();
													if (jQuery.trim(assignedStatus) != ""){
														desc += " ("+assignedStatus+")";
													}
												} else {
													if (jQuery(this).find("individual-assignment").text() == "true") desc += " - <a href='javascript:;' onClick='Clara.Reviewer.GatekeeperSendNow(\""+name+"\",\""+desc+"\");'>Send now</a>";
												}
												
												// add to form
												cbItems
														.push({
															boxLabel : desc,
															name : name,
															id : 'fldAssign_'
																	+ name,
															checked : checked,
															disabled:disabled,
															cls : (recommended) ? "cb-recommended"
																	: ""
														});
											});

							clog("cbItems", cbItems);

							t.items = {
								xtype : 'checkboxgroup',
								hideLabel : true,
								id : 'cbcommitteegroup',
								itemCls : 'x-check-group-alt',
								columns : 1,
								items : cbItems
							};

						} else {
							clog(
									"[ERROR] Clara.Reviewer.GatekeeperAssignCommitteePanel.initComponent(): reviewPanelXml not defined",
									t);
						}

						// apply config
						Ext.apply(this, Ext.apply(this.initialConfig, config));

						// call parent
						Clara.Reviewer.GatekeeperAssignCommitteePanel.superclass.initComponent
								.apply(this, arguments);
					}

				});
Ext.reg('clarareviewergatekeeperassigncommitteepanel',
		Clara.Reviewer.GatekeeperAssignCommitteePanel);

Clara.Reviewer.IRBPrereviewAssignCommitteePanel = Ext
.extend(
		Ext.FormPanel,
		{

			id : 'IRBPrereviewAssignCommitteePanel',
			reviewPanelXml : {},
			title : 'IRB Prereview: Assign committees for this protocol',
			reviewFormType : 'protocol', // or 'contract'
			constructor : function(config) {
				Clara.Reviewer.IRBPrereviewAssignCommitteePanel.superclass.constructor
						.call(this, config);
			},
			validate : function() {
				return true;
			},
			getXML : function() {
				var t = this;
				//var xml = "<" + t.id + ">";
				//xml += t.getFormXMLString();
				//xml += "</" + t.id + ">";
				return t.getFormXMLString();
			},

			getFormXMLString : function() {
				var t = this;
				var xml = "<invovled-committees>";
				var cbs = Ext.getCmp("cbcommitteegroup").getValue();
				for ( var i = 0; i < cbs.length; i++) {
					if (!cbs[i].disabled) xml += "<committee>" + cbs[i].getName() + "</committee>";
				}
				xml += "</invovled-committees>";
				return xml;
			},

			initComponent : function() {
				var t = this;
				var config = {};

				if (t.reviewPanelXml) {
					var xml = t.reviewPanelXml;

					var cbItems = [];

					jQuery(xml)
							.find("committees")
							.find("committee")
							.each(
									function() {
										var checked = (jQuery(this)
												.find("checked").text() == "true") ? true
												: false;
										var recommended = (jQuery(this)
												.find("recommended")
												.text() == "true") ? true
												: false;
										var desc = jQuery(this).find(
												"desc").text();
										var name = jQuery(this).find(
												"name").text();

										var disabled = (jQuery(this)
												.find("assigned").text() == "true") ? true
														: false;
										if (disabled){
											var assignedStatus = jQuery(this).find("status").text();
											if (jQuery.trim(assignedStatus) != ""){
												desc += " ("+assignedStatus+")";
											}
										} else {
											if (jQuery(this).find("individual-assignment").text() == "true") desc += " - <a href='javascript:;' onClick='Clara.Reviewer.GatekeeperSendNow(\""+name+"\",\""+desc+"\");'>Send now</a>";
										}
										
										// add to form
										cbItems
												.push({
													boxLabel : desc,
													name : name,
													id : 'fldAssign_'
															+ name,
													checked : checked,
													disabled:disabled,
													cls : (recommended) ? "cb-recommended"
															: ""
												});
									});

					clog("cbItems", cbItems);

					t.items = {
						xtype : 'checkboxgroup',
						hideLabel : true,
						id : 'cbcommitteegroup',
						itemCls : 'x-check-group-alt',
						columns : 1,
						items : cbItems
					};

				} else {
					clog(
							"[ERROR] Clara.Reviewer.IRBPrereviewAssignCommitteePanel.initComponent(): reviewPanelXml not defined",
							t);
				}

				// apply config
				Ext.apply(this, Ext.apply(this.initialConfig, config));

				// call parent
				Clara.Reviewer.IRBPrereviewAssignCommitteePanel.superclass.initComponent
						.apply(this, arguments);
			}

		});
Ext.reg('clarareviewerirbprereviewassigncommitteepanel',
Clara.Reviewer.IRBPrereviewAssignCommitteePanel);

Clara.Reviewer.BudgetManagerAssignCommitteePanel = Ext
.extend(
		Ext.FormPanel,
		{

			id : 'BudgetManagerAssignCommitteePanel',
			reviewPanelXml : {},
			title : 'Assign committees for this protocol',
			reviewFormType : 'protocol', // or 'contract'
			constructor : function(config) {
				Clara.Reviewer.BudgetManagerAssignCommitteePanel.superclass.constructor
						.call(this, config);
			},
			validate : function() {
				return true;
			},
			getXML : function() {
				var t = this;
				//var xml = "<" + t.id + ">";
				//xml += t.getFormXMLString();
				//xml += "</" + t.id + ">";
				return t.getFormXMLString();
			},

			getFormXMLString : function() {
				var t = this;
				var xml = "<invovled-committees>";
				var cbs = Ext.getCmp("cbcommitteegroup").getValue();
				for ( var i = 0; i < cbs.length; i++) {
					xml += "<committee>" + cbs[i].getName()
							+ "</committee>";
				}
				xml += "</invovled-committees>";
				return xml;
			},

			initComponent : function() {
				var t = this;
				var config = {};

				if (t.reviewPanelXml) {
					var xml = t.reviewPanelXml;

					var cbItems = [];

					jQuery(xml)
							.find("committees")
							.find("committee")
							.each(
									function() {
										var checked = (jQuery(this)
												.find("checked").text() == "true") ? true
												: false;
										var recommended = (jQuery(this)
												.find("recommended")
												.text() == "true") ? true
												: false;
										var desc = jQuery(this).find(
												"desc").text();
										var name = jQuery(this).find(
												"name").text();

										// add to form
										cbItems
												.push({
													boxLabel : desc,
													name : name,
													id : 'fldAssign_'
															+ name,
													checked : checked,
													cls : (recommended) ? "cb-recommended"
															: ""
												});
									});

					clog("cbItems", cbItems);

					t.items = {
						xtype : 'checkboxgroup',
						// fieldLabel:'Committees',
						hideLabel : true,
						id : 'cbcommitteegroup',
						itemCls : 'x-check-group-alt',
						columns : 1,
						items : cbItems
					};

					// t.doLayout();

				} else {
					clog(
							"[ERROR] Clara.Reviewer.BudgetManagerAssignCommitteePanel.initComponent(): reviewPanelXml not defined",
							t);
				}

				// apply config
				Ext.apply(this, Ext.apply(this.initialConfig, config));

				// call parent
				Clara.Reviewer.BudgetManagerAssignCommitteePanel.superclass.initComponent
						.apply(this, arguments);
			}

		});
Ext.reg('clarareviewerbudgetmanagerassigncommitteepanel',
Clara.Reviewer.BudgetManagerAssignCommitteePanel);

Clara.Reviewer.IRBOfficeAssignCommitteePanel = Ext
.extend(
		Ext.FormPanel,
		{

			id : 'IRBOfficeAssignCommitteePanel',
			reviewPanelXml : {},
			title : 'IRB Office: Assign committees for this protocol',
			reviewFormType : 'protocol', // or 'contract'
			constructor : function(config) {
				Clara.Reviewer.IRBOfficeAssignCommitteePanel.superclass.constructor
						.call(this, config);
			},
			validate : function() {
				return true;
			},
			getXML : function() {
				var t = this;
				return t.getFormXMLString();
			},

			getFormXMLString : function() {
				var t = this;
				var xml = "<invovled-committees>";
				var cbs = Ext.getCmp("cbcommitteegroup").getValue();
				for ( var i = 0; i < cbs.length; i++) {
					xml += "<committee>" + cbs[i].getName()
							+ "</committee>";
				}
				xml += "</invovled-committees>";
				return xml;
			},

			initComponent : function() {
				var t = this;
				var config = {};

				if (t.reviewPanelXml) {
					var xml = t.reviewPanelXml;

					var cbItems = [];

					jQuery(xml)
							.find("committees")
							.find("committee")
							.each(
									function() {
										var checked = (jQuery(this)
												.find("checked").text() == "true") ? true
												: false;
										var recommended = (jQuery(this)
												.find("recommended")
												.text() == "true") ? true
												: false;
										var desc = jQuery(this).find(
												"desc").text();
										var name = jQuery(this).find(
												"name").text();

										// add to form
										cbItems
												.push({
													boxLabel : desc,
													name : name,
													id : 'fldAssign_'
															+ name,
													checked : checked,
													cls : (recommended) ? "cb-recommended"
															: "",
												});
									});

					clog("cbItems", cbItems);

					t.items = {
						xtype : 'checkboxgroup',
						// fieldLabel:'Committees',
						hideLabel : true,
						id : 'cbcommitteegroup',
						itemCls : 'x-check-group-alt',
						columns : 1,
						items : cbItems
					};

					// t.doLayout();

				} else {
					clog(
							"[ERROR] Clara.Reviewer.IRBOfficeAssignCommitteePanel.initComponent(): reviewPanelXml not defined",
							t);
				}

				// apply config
				Ext.apply(this, Ext.apply(this.initialConfig, config));

				// call parent
				Clara.Reviewer.IRBOfficeAssignCommitteePanel.superclass.initComponent
						.apply(this, arguments);
			}

		});
Ext.reg('clarareviewerirbofficeassigncommitteepanel',
Clara.Reviewer.IRBOfficeAssignCommitteePanel);

Clara.Reviewer.ACHRIAssignCommitteePanel = Ext
.extend(
		Ext.FormPanel,
		{

			id : 'ACHRIAssignCommitteePanel',
			reviewPanelXml : {},
			title : 'ACHRI: Assign committees for this protocol',
			reviewFormType : 'protocol', // or 'contract'
			constructor : function(config) {
				Clara.Reviewer.ACHRIAssignCommitteePanel.superclass.constructor
						.call(this, config);
			},
			validate : function() {
				return true;
			},
			getXML : function() {
				var t = this;
				//var xml = "<" + t.id + ">";
				//xml += t.getFormXMLString();
				//xml += "</" + t.id + ">";
				return t.getFormXMLString();
			},

			getFormXMLString : function() {
				var t = this;
				var xml = "<invovled-committees>";
				var cbs = Ext.getCmp("cbcommitteegroup").getValue();
				for ( var i = 0; i < cbs.length; i++) {
					xml += "<committee>" + cbs[i].getName()
							+ "</committee>";
				}
				xml += "</invovled-committees>";
				return xml;
			},

			initComponent : function() {
				var t = this;
				var config = {};

				if (t.reviewPanelXml) {
					var xml = t.reviewPanelXml;

					var cbItems = [];

					jQuery(xml)
							.find("committees")
							.find("committee")
							.each(
									function() {
										var checked = (jQuery(this)
												.find("checked").text() == "true") ? true
												: false;
										var recommended = (jQuery(this)
												.find("recommended")
												.text() == "true") ? true
												: false;
										var desc = jQuery(this).find(
												"desc").text();
										var name = jQuery(this).find(
												"name").text();

										// add to form
										cbItems
												.push({
													boxLabel : desc,
													name : name,
													id : 'fldAssign_'
															+ name,
													checked : checked,
													cls : (recommended) ? "cb-recommended"
															: "",
												});
									});

					clog("cbItems", cbItems);

					t.items = {
						xtype : 'checkboxgroup',
						// fieldLabel:'Committees',
						hideLabel : true,
						id : 'cbcommitteegroup',
						itemCls : 'x-check-group-alt',
						columns : 1,
						items : cbItems
					};

					// t.doLayout();

				} else {
					clog(
							"[ERROR] Clara.Reviewer.ACHRIAssignCommitteePanel.initComponent(): reviewPanelXml not defined",
							t);
				}

				// apply config
				Ext.apply(this, Ext.apply(this.initialConfig, config));

				// call parent
				Clara.Reviewer.ACHRIAssignCommitteePanel.superclass.initComponent
						.apply(this, arguments);
			}

		});
Ext.reg('clarareviewerachriassigncommitteepanel',
Clara.Reviewer.ACHRIAssignCommitteePanel);


Clara.Reviewer.ClinicalTrialsReviewPanel = Ext
.extend(Ext.FormPanel,{

	id : 'ClinicalTrialsReviewPanel',
	reviewPanelXml : {},
	title : 'ClinicalTrials.gov: Please provide the following information',
	reviewFormType : 'protocol', // or 'contract'
	constructor : function(config) {
		Clara.Reviewer.ClinicalTrialsReviewPanel.superclass.constructor
				.call(this, config);
	},
	validate : function() {
		return true;
	},
	getXML : function() {
		var t = this;
		return t.getFormXMLString();
	},

	getFormXMLString : function() {
		var t = this;
		var xml = "";
		xml += "<nct-number>" + Ext.getCmp("fldNCT").getValue() + "</nct-number>";
		return xml;
	},

	initComponent : function() {
		var t = this;

		if (t.reviewPanelXml) {
			var xml = t.reviewPanelXml;

			t.items = [new Ext.form.TextField({
				id: 'fldNCT',
				fieldLabel : 'NCT Number'
			})

			];

		} else {
			clog(
					"[ERROR] Clara.Reviewer.ClinicalTrialsReviewPanel.initComponent(): reviewPanelXml not defined",
					t);
		}

		// call parent
		Clara.Reviewer.ClinicalTrialsReviewPanel.superclass.initComponent
				.apply(this, arguments);
	}

	
});
Ext.reg('clarareviewerclinicaltrialsreviewpanel', Clara.Reviewer.ClinicalTrialsReviewPanel);


Clara.Reviewer.AuditIRBPrereviewFinalReviewPanel = Ext
.extend(
		Ext.FormPanel,
		{
			id : 'AuditIRBPrereviewFinalReviewPanel',
			reviewPanelXml : {},
			title : 'IRB Office: Please provide the following information',
			reviewFormType : 'protocol', // or 'contract'
			constructor : function(config) {
				Clara.Reviewer.AuditIRBPrereviewFinalReviewPanel.superclass.constructor
						.call(this, config);
			},
			validate : function() {
				return true;
			},
			getXML : function() {
				var t = this;
				return t.getFormXMLString();
			},

			getFormXMLString : function() {
				var t = this;
				var xml = "";
				xml += "<finding>" + Ext.getCmp("fldFinding").getValue().inputValue
				+ "</finding><finding-other>" + Ext.getCmp("fldFindingOther").getValue().inputValue
						+ "</finding-other><reportable>"+ Ext.getCmp("fldReportable").getValue().inputValue +"</reportable><irb>"+ Ext.getCmp("fldIRB").getValue().inputValue +"</irb><hipaa>"+ Ext.getCmp("fldHIPAA").getValue().inputValue +"</hipaa>";
				return xml;
			},

			initComponent : function() {
				var t = this;

				if (t.reviewPanelXml) {
					var xml = t.reviewPanelXml;

					t.items = [new Ext.form.RadioGroup({
						id: 'fldFinding',
						fieldLabel : 'IRB Office Suggested Findings Assessment',
						columns: 1,
						items:[
								{boxLabel: 'No Evidence of Non-Compliance', name: 'rb-finding', inputValue: 'No Evidence of Non-Compliance'},
								{boxLabel: 'Non-Compliance', name: 'rb-finding', inputValue: 'Non-Compliance'},
								{boxLabel: 'Continuing Non-Compliance', name: 'rb-finding', inputValue: 'Continuing Non-Compliance'},
								{boxLabel: 'Serious Non-Compliance', name: 'rb-finding', inputValue: 'Serious Non-Compliance'},
								{boxLabel: 'Serious and Continuing Non-Compliance', name: 'rb-finding', inputValue: 'Serious and Continuing Non-Compliance'},
								{boxLabel: 'Other (see below)', name: 'rb-finding', inputValue: 'Other'}
						       ]
					}),new Ext.form.TextArea({
						id: 'fldFindingOther',
						fieldLabel : 'If you chose "Other" above, please specify'
					}),
					new Ext.form.RadioGroup({
						id: 'fldReportable',
						fieldLabel : 'Findings Reportable to OHRP/Other Regulatory Agency?',
						columns: 1,
						items:[
								{boxLabel: 'Yes', name: 'rb-reportable', inputValue: 'yes'},
								{boxLabel: 'No', name: 'rb-reportable', inputValue: 'no'}
						       ]
					}),
					new Ext.form.RadioGroup({
						id: 'fldHIPAA',
						fieldLabel : 'HIPAA Finding?',
						columns: 1,
						items:[
								{boxLabel: 'Yes', name: 'rb-h', inputValue: 'yes'},
								{boxLabel: 'No', name: 'rb-h', inputValue: 'no'}
						       ]
					}),
					new Ext.form.RadioGroup({
						id: 'fldIRB',
						fieldLabel : 'IRB Finding?',
						columns: 1,
						items:[
								{boxLabel: 'Yes', name: 'rb-i', inputValue: 'yes'},
								{boxLabel: 'No', name: 'rb-i', inputValue: 'no'}
						       ]
					})

					];

				} else {
					clog(
							"[ERROR] Clara.Reviewer.AuditIRBPrereviewFinalReviewPanel.initComponent(): reviewPanelXml not defined",
							t);
				}

				// call parent
				Clara.Reviewer.AuditIRBPrereviewFinalReviewPanel.superclass.initComponent
						.apply(this, arguments);
			}

		});
Ext.reg('clarareviewerauditirbprereviewpanel',
Clara.Reviewer.AuditIRBPrereviewFinalReviewPanel);


Clara.Reviewer.ModIRBPrereviewFinalReviewPanel = Ext
.extend(
		Ext.FormPanel,
		{
			id : 'IRBPrereviewFinalReviewPanel',
			reviewPanelXml : {},
			title : 'IRB Prereview: Please provide the following information',
			reviewFormType : 'protocol', // or 'contract'
			constructor : function(config) {
				Clara.Reviewer.ModIRBPrereviewFinalReviewPanel.superclass.constructor
						.call(this, config);
			},
			validate : function() {
				return (Ext.getCmp("fldSuggestedType").getValue() != null);
			},
			getXML : function() {
				var t = this;
				return t.getFormXMLString();
			},

			getFormXMLString : function() {
				var t = this;
				var xml = "";
				xml += "<suggested-type>"+ Ext.getCmp("fldSuggestedType").getValue().inputValue +"</suggested-type>";
				return xml;
			},

			initComponent : function() {
				var t = this;

				if (t.reviewPanelXml) {
					var xml = t.reviewPanelXml;

					t.items = [new Ext.form.RadioGroup({
						id: 'fldSuggestedType',
						fieldLabel : 'Suggested Review Type',
						columns: 1,
						items:[
								{boxLabel: 'Expedited', name: 'rb-suggested', inputValue: 'EXPEDITED'},
								//{boxLabel: 'Exempt', name: 'rb-suggested', inputValue: 'exempt'},
								{boxLabel: 'Full Board', name: 'rb-suggested', inputValue: 'FULL_BOARD'},
								{boxLabel: 'N/A', name: 'rb-suggested', inputValue: 'na'}
						       ]
					})

					];

				} else {
					clog(
							"[ERROR] Clara.Reviewer.ModIRBPrereviewFinalReviewPanel.initComponent(): reviewPanelXml not defined",
							t);
				}

				// call parent
				Clara.Reviewer.ModIRBPrereviewFinalReviewPanel.superclass.initComponent
						.apply(this, arguments);
			}

		});
Ext.reg('clarareviewermodirbprereviewpanel',
		Clara.Reviewer.ModIRBPrereviewFinalReviewPanel);
Ext.reg('clarareviewerirbprereviewpanel',
		Clara.Reviewer.ModIRBPrereviewFinalReviewPanel);

Clara.Reviewer.IRBPrereviewAuditResponse = Ext
.extend(
		Ext.FormPanel,
		{
			id : 'IRBPrereviewAuditResponse',
			reviewPanelXml : {},
			title : 'IRB Prereview: Please provide the following information',
			reviewFormType : 'protocol', // or 'contract'
			constructor : function(config) {
				Clara.Reviewer.IRBPrereviewAuditResponse.superclass.constructor
						.call(this, config);
			},
			validate : function() {
				var t = this;
				var valid = true;
				clog(t.items.items);
				for ( var i = 0; i < t.items.items.length; i++) {
					valid = valid && t.items.items[i].validate();
				}
				return valid;
			},
			getXML : function() {
				var t = this;
				return t.getFormXMLString();
			},

			getFormXMLString : function() {
				var t = this;
				var xml = "";
				xml += "<audit-type>" + Ext.getCmp("fldAuditType").getValue().inputValue
				+ "<audit-other><![CDATA[" + Ext.getCmp("fldAuditOther").getValue() + "]]></audit-other>"
				+ "</audit-type><hipaa-finding>"+ Ext.getCmp("fldHipaaFinding").getValue().inputValue +"</hipaa-finding>"
				+ "<irb-finding>"+ Ext.getCmp("fldIRBFinding").getValue().inputValue +"</irb-finding>";
				return xml;
			},

			initComponent : function() {
				var t = this;

				if (t.reviewPanelXml) {
					var xml = t.reviewPanelXml;

					t.items = [new Ext.form.RadioGroup({
						id: 'fldAuditType',
						fieldLabel : 'Audit Type',
						allowBlank:false,
						columns: 1,
						items:[
								{boxLabel: 'Random', name: 'rb-suggested', inputValue: 'random'},
								{boxLabel: 'Requested', name: 'rb-suggested', inputValue: 'requested'},
								{boxLabel: 'For Cause', name: 'rb-suggested', inputValue: 'for-cause'},
								{boxLabel: 'Other', name: 'rb-suggested', inputValue: 'other'}
						       ]
					}),
					new Ext.form.TextArea({
						id: 'fldAuditOther',
						width:400,
						fieldLabel : 'If you chose "Other" above, please specify'
					}),
					new Ext.form.RadioGroup({
						id: 'fldHipaaFinding',
						allowBlank:false,
						fieldLabel : 'HIPAA finding?',
						columns: 1,
						items:[
								{boxLabel: 'Yes', name: 'rb-hf', inputValue: 'yes'},
								{boxLabel: 'No', name: 'rb-hf', inputValue: 'no'}
						       ]
					}),
					new Ext.form.RadioGroup({
						id: 'fldIRBFinding',
						allowBlank:false,
						fieldLabel : 'IRB finding?',
						columns: 1,
						items:[
								{boxLabel: 'Yes', name: 'rb-if', inputValue: 'yes'},
								{boxLabel: 'No', name: 'rb-if', inputValue: 'no'}
						       ]
					})

					];

				} else {
					clog(
							"[ERROR] Clara.Reviewer.IRBPrereviewAuditResponse.initComponent(): reviewPanelXml not defined",
							t);
				}

				// call parent
				Clara.Reviewer.IRBPrereviewAuditResponse.superclass.initComponent
						.apply(this, arguments);
			}

		});
Ext.reg('clarareviewerirbprereviewauditresponse',
Clara.Reviewer.IRBPrereviewAuditResponse);

Clara.Reviewer.CRIRBPrereviewFinalReviewPanel = Ext
.extend(
		Ext.FormPanel,
		{
			id : 'IRBPrereviewFinalReviewPanel',
			reviewPanelXml : {},
			title : 'IRB Prereview: Please provide the following information',
			reviewFormType : 'protocol', // or 'contract'
			constructor : function(config) {
				Clara.Reviewer.CRIRBPrereviewFinalReviewPanel.superclass.constructor
						.call(this, config);
			},
			validate : function() {
				return (Ext.getCmp("fldSuggestedType").getValue() != null);
			},
			getXML : function() {
				var t = this;
				return t.getFormXMLString();
			},

			getFormXMLString : function() {
				var t = this;
				var xml = "";
				xml += "<suggested-type>"+ Ext.getCmp("fldSuggestedType").getValue().inputValue +"</suggested-type>";
				return xml;
			},

			initComponent : function() {
				var t = this;

				if (t.reviewPanelXml) {
					var xml = t.reviewPanelXml;

					t.items = [new Ext.form.RadioGroup({
						id: 'fldSuggestedType',
						fieldLabel : 'Suggested Review Type',
						columns: 1,
						items:[
								{boxLabel: 'Expedited', name: 'rb-suggested', inputValue: 'EXPEDITED'},
								{boxLabel: 'Exempt', name: 'rb-suggested', inputValue: 'EXEMPT'},
								{boxLabel: 'Full Board', name: 'rb-suggested', inputValue: 'FULL_BOARD'},
								{boxLabel: 'N/A', name: 'rb-suggested', inputValue: 'na'}
						       ]
					})

					];

				} else {
					clog(
							"[ERROR] Clara.Reviewer.CRIRBPrereviewFinalReviewPanel.initComponent(): reviewPanelXml not defined",
							t);
				}

				// call parent
				Clara.Reviewer.CRIRBPrereviewFinalReviewPanel.superclass.initComponent
						.apply(this, arguments);
			}

		});
Ext.reg('clarareviewercrirbprereviewpanel',
Clara.Reviewer.CRIRBPrereviewFinalReviewPanel);

Clara.Reviewer.NewSubIRBPrereviewFinalReviewPanel = Ext
		.extend(
				Ext.FormPanel,
				{
					id : 'IRBPrereviewFinalReviewPanel',
					reviewPanelXml : {},
					title : 'IRB Prereview: Please provide the following information',
					reviewFormType : 'protocol', // or 'contract'
					constructor : function(config) {
						Clara.Reviewer.NewSubIRBPrereviewFinalReviewPanel.superclass.constructor
								.call(this, config);
					},
					validate : function() {
						return (Ext.getCmp("fldFda").getValue() != null && Ext.getCmp("fldSuggestedType").getValue() != null);
					},
					getXML : function() {
						var t = this;
						return t.getFormXMLString();
					},

					getFormXMLString : function() {
						var t = this;
						var xml = "";
						xml += "<fda>" + Ext.getCmp("fldFda").getValue().inputValue
								+ "</fda><suggested-type>"+ Ext.getCmp("fldSuggestedType").getValue().inputValue +"</suggested-type>";
						return xml;
					},

					initComponent : function() {
						var t = this;

						if (t.reviewPanelXml) {
							var xml = t.reviewPanelXml;

							t.items = [new Ext.form.RadioGroup({
								id: 'fldSuggestedType',
								fieldLabel : 'Suggested Review Type',
								columns: 1,
								items:[
										{boxLabel: 'Expedited', name: 'rb-suggested', inputValue: 'EXPEDITED'},
										{boxLabel: 'Exempt', name: 'rb-suggested', inputValue: 'EXEMPT'},
										{boxLabel: 'Full Board', name: 'rb-suggested', inputValue: 'FULL_BOARD'},
										{boxLabel: 'N/A', name: 'rb-suggested', inputValue: 'na'}
								       ]
							}),
							new Ext.form.RadioGroup({
								id: 'fldFda',
								fieldLabel : 'Is this protocol subject to FDA oversight?',
								columns: 1,
								items:[
										{boxLabel: 'Yes', name: 'rb-auto', inputValue: 'yes'},
										{boxLabel: 'No', name: 'rb-auto', inputValue: 'no'}
								       ]
							})

							];

						} else {
							clog(
									"[ERROR] Clara.Reviewer.NewSubIRBPrereviewFinalReviewPanel.initComponent(): reviewPanelXml not defined",
									t);
						}

						// call parent
						Clara.Reviewer.NewSubIRBPrereviewFinalReviewPanel.superclass.initComponent
								.apply(this, arguments);
					}

				});
Ext.reg('clarareviewernewsubirbprereviewpanel',
		Clara.Reviewer.NewSubIRBPrereviewFinalReviewPanel);



Clara.Reviewer.CoverageFinalReviewPanel = Ext
.extend(
		Ext.FormPanel,
		{
			id : 'CoverageFinalReviewPanel',
			reviewFormIdentifier:'coverage-review',
			reviewPanelXml : {},
			labelWidth:250,
			labelSeparator:'',
			title : 'Coverage: Please provide the following information',
			reviewFormType : 'protocol', // or 'contract'
			constructor : function(config) {
				Clara.Reviewer.CoverageFinalReviewPanel.superclass.constructor
						.call(this, config);
			},
			validate : function() {
				return true;
			},
			getXML : function() {
				var t = this;
				return t.getFormXMLString();
			},
			
			getFormXMLString : function() {
				var t = this;
				var xml = "";
				xml += "<medicare-benefit>" + Ext.getCmp("fldQ1").getValue().inputValue
				+ "</medicare-benefit><theraputic-intent>"+ Ext.getCmp("fldQ2").getValue().inputValue +"</theraputic-intent>"
				+ "<enrolled-diagnosed>"+ Ext.getCmp("fldQ3").getValue().inputValue +"</enrolled-diagnosed>"
				+ "<trial-category>"+ Ext.getCmp("fldQ4").getValue().inputValue +"</trial-category>";

				return xml;
			},

			initComponent : function() {
				var t = this;

				if (t.reviewPanelXml) {
					var xml = t.reviewPanelXml;

					t.items = [new Ext.form.RadioGroup({
						id: 'fldQ1',
						fieldLabel : 'Does the principal purpose of the trial evaluate an item or service that falls within a Medicare benefit category?',
						labelStyle:'font-weight:800;',
						columns: 1,
						items:[
								{boxLabel: 'Yes', name: 'rb-1', inputValue: 'yes'},
								{boxLabel: 'No', name: 'rb-1', inputValue: 'no'},
								{boxLabel: 'N/A', name: 'rb-1', inputValue: 'na'}
						       ]
					}),
					new Ext.form.RadioGroup({
						id: 'fldQ2',
						fieldLabel : 'Does the trial have therapeutic intent?<br/><span class="finalReviewChoiceDesc">The trial must not be designed to exclusively test toxicity or disease pathosphysiology</span>',
						labelStyle:'font-weight:800;',
						columns: 1,
						items:[
								{boxLabel: 'Yes', name: 'rb-2', inputValue: 'yes'},
								{boxLabel: 'No', name: 'rb-2', inputValue: 'no'},
								{boxLabel: 'N/A', name: 'rb-2', inputValue: 'na'}
						       ]
					}),
					new Ext.form.RadioGroup({
						id: 'fldQ3',
						fieldLabel : 'Does the trial enroll patients with diagnosed disease?<br/><span class="finalReviewChoiceDesc">Trials of diagnostic interventions may enroll healthy patients in order to have a proper control group</span>',
						labelStyle:'font-weight:800;',
						columns: 1,
						items:[
								{boxLabel: 'Yes', name: 'rb-3', inputValue: 'yes'},
								{boxLabel: 'No', name: 'rb-3', inputValue: 'no'},
								{boxLabel: 'N/A', name: 'rb-3', inputValue: 'na'}
						       ]
					}),
					new Ext.form.RadioGroup({
						id: 'fldQ4',
						fieldLabel : 'Choose a category for this trial.<br/><span class="finalReviewChoiceDesc">If the trial doesn\'t fall under one of these categories, choose "N/A".</span>',
						labelStyle:'font-weight:800;',
						columns: 1,
						items:[
								{boxLabel: 'Compassionate Use', name: 'rb-4', inputValue: 'compassionate-use'},
								{boxLabel: 'Device', name: 'rb-4', inputValue: 'device'},
								{boxLabel: 'HUD', name: 'rb-4', inputValue: 'hud'},
								{boxLabel: 'Medicare Non-Qualifying', name: 'rb-4', inputValue: 'medicare-non-qualifying'},
								{boxLabel: 'Medicare Qualifying', name: 'rb-4', inputValue: 'medicare-qualifying'},
								{boxLabel: 'Phase 1 Non-Qualifying / Phase 2 Qualifying', name: 'rb-4', inputValue: 'phase-1-nq-phase2-q'},
								{boxLabel: 'Single Patient IND', name: 'rb-4', inputValue: 'single-patient-ind'},
								{boxLabel: 'N/A', name: 'rb-4', inputValue: 'na'}
						       ]
					})
					];

				} else {
					clog(
							"[ERROR] Clara.Reviewer.CoverageFinalReviewPanel.initComponent(): reviewPanelXml not defined",
							t);
				}

				// call parent
				Clara.Reviewer.CoverageFinalReviewPanel.superclass.initComponent
						.apply(this, arguments);
				
				var p = this.reviewPanelXml;

				Ext.getCmp("CoverageFinalReviewPanel").getForm().findField("fldQ1").setValue(jQuery(p).find("formdata").find("medicare-benefit").text());
				Ext.getCmp("CoverageFinalReviewPanel").getForm().findField("fldQ2").setValue(jQuery(p).find("formdata").find("theraputic-intent").text());
				Ext.getCmp("CoverageFinalReviewPanel").getForm().findField("fldQ3").setValue(jQuery(p).find("formdata").find("enrolled-diagnosed").text());
				Ext.getCmp("CoverageFinalReviewPanel").getForm().findField("fldQ4").setValue(jQuery(p).find("formdata").find("trial-category").text());

			}

		});
Ext.reg('clarareviewercoveragepanel',
Clara.Reviewer.CoverageFinalReviewPanel);




Ext.ns('Clara.Reviewer.NewSubmission','Clara.Reviewer.Modification','Clara.Reviewer.ContinuingReview');


Clara.Reviewer.NewSubmission.IRBExpeditedFinalReviewPanel = Ext
		.extend(
				Ext.FormPanel,
				{
					id : 'NewSubmissionIRBExpeditedFinalReviewPanel',
					reviewPanelXml : {},
					title : 'IRB Expedited Review: Please provide the following information',
					reviewFormType : 'protocol', // or 'contract'
					constructor : function(config) {
						Clara.Reviewer.NewSubmission.IRBExpeditedFinalReviewPanel.superclass.constructor
								.call(this, config);
					},
					validate : function() {
						var t = this;
						var valid = true;
						clog(t.items.items);
						for ( var i = 0; i < t.items.items.length; i++) {
							valid = valid && t.items.items[i].validate();
						}
						return valid;
					},
					getXML : function() {
						var t = this;
						
						return t.getFormXMLString();
					},

					getFormXMLString : function() {
						var t = this;
						var xml = "";
						xml += "<expedited><category>";
								//+ Ext.getCmp("fldExpeditedCategory").getValue()
								//+ "</category>";
						var categories = Ext.getCmp("fldExpeditedCategory").getValue();
						for ( var i = 0; i < categories.length; i++) {
							if (!categories[i].disabled) xml += "<value>" + categories[i].getName() + "</value>";
						}
						xml += "</category>";
						xml += "<consent-waived>"+Ext.getCmp("fldQ1").getValue().inputValue+"</consent-waived>";
						xml += "<consent-documentation-waived>"+Ext.getCmp("fldQ2").getValue().inputValue+"</consent-documentation-waived>";
						xml += "<hipaa-waived>"+Ext.getCmp("fldQ3").getValue().inputValue+"</hipaa-waived>";
						xml += "</expedited>";
						return xml;
					},

					initComponent : function() {
						var t = this;

						if (t.reviewPanelXml) {
							var xml = t.reviewPanelXml;
							
							/*var expeditedCategories = [
							                 ['1'],
							                 ['2'],
							                 ['3'],
							                 ['4'],
							                 ['5'],
							                 ['6'],
							                 ['7'],
							                 ['N/A'],
							                 ['Not yet determined']
							             ];*/

							t.items = [ /*new Ext.form.ComboBox({
								id: 'fldExpeditedCategory',
								labelStyle:'font-weight:800;',
								fieldLabel : 'Expedited Category',
								hiddenName : 'fldExpeditedCategory',
								store : new Ext.data.SimpleStore({
									fields : [ 'category' ],
									data : expeditedCategories
								}),
								displayField : 'category',
								allowBlank:false,
								typeAhead : true,
								mode : 'local',
								triggerAction : 'all',
								emptyText : 'Choose a category...',
								selectOnFocus : true*/
					            new Ext.form.CheckboxGroup({
									   id:'fldExpeditedCategory',
									   hideLabel:false,
									   labelStyle:'font-weight:800;',
									   fieldLabel:'Choose a category:',
									   xtype:'checkboxgroup',
									   name:'categorylist',
									   itemCls: 'x-check-group-alt',
									   columns:3,
									   vertical:true,
									   items:[
				    	    	        new Ext.form.Checkbox({hideLabel:true, boxLabel:'1', inputValue:'1', name:'1'}),
				    	    	        new Ext.form.Checkbox({hideLabel:true, boxLabel:'2', inputValue:'2', name:'2'}),
				    	    	        new Ext.form.Checkbox({hideLabel:true, boxLabel:'3', inputValue:'3', name:'3'}),
				    	    	        new Ext.form.Checkbox({hideLabel:true, boxLabel:'4', inputValue:'4', name:'4'}),
				    	    	        new Ext.form.Checkbox({hideLabel:true, boxLabel:'5', inputValue:'5', name:'5'}),
				    	    	        new Ext.form.Checkbox({hideLabel:true, boxLabel:'6', inputValue:'6', name:'6'}),
				    	    	        new Ext.form.Checkbox({hideLabel:true, boxLabel:'7', inputValue:'7', name:'7'}),
				    	    	        new Ext.form.Checkbox({hideLabel:true, boxLabel:'N/A', inputValue:'N/A', name:'N/A'}),
				    	    	        new Ext.form.Checkbox({hideLabel:true, boxLabel:'Not yet determined', inputValue:'Not yet determined', name:'Not yet determined'})
				    	    	        ]
							}),
							new Ext.form.RadioGroup({
								id: 'fldQ1',
								fieldLabel : 'Consent waived?',
								allowBlank:false,
								labelStyle:'font-weight:800;',
								columns: 1,
								items:[
										{boxLabel: 'Yes', name: 'rb-1', inputValue: 'yes'},
										{boxLabel: 'No', name: 'rb-1', inputValue: 'no'},
										{boxLabel: 'N/A', name: 'rb-1', inputValue: 'na'},
										{boxLabel: 'Not yet determined', name: 'rb-1', inputValue: 'Not yet determined'}
								       ]
							}),
							new Ext.form.RadioGroup({
								id: 'fldQ2',
								fieldLabel : 'Consent Documentation Waived?',
								allowBlank:false,
								labelStyle:'font-weight:800;',
								columns: 1,
								items:[
										{boxLabel: 'Yes', name: 'rb-2', inputValue: 'yes'},
										{boxLabel: 'No', name: 'rb-2', inputValue: 'no'},
										{boxLabel: 'N/A', name: 'rb-2', inputValue: 'na'},
										{boxLabel: 'Not yet determined', name: 'rb-2', inputValue: 'Not yet determined'}
								       ]
							}),
							new Ext.form.RadioGroup({
								id: 'fldQ3',
								fieldLabel : 'HIPAA waived?',
								allowBlank:false,
								labelStyle:'font-weight:800;',
								columns: 1,
								items:[
										{boxLabel: 'Yes', name: 'rb-3', inputValue: 'yes'},
										{boxLabel: 'No', name: 'rb-3', inputValue: 'no'},
										{boxLabel: 'N/A', name: 'rb-3', inputValue: 'na'},
										{boxLabel: 'Not yet determined', name: 'rb-3', inputValue: 'Not yet determined'}
								       ]
							})
							
							];

						} else {
							clog(
									"[ERROR] Clara.Reviewer.NewSubmission.IRBExpeditedFinalReviewPanel.initComponent(): reviewPanelXml not defined",
									t);
						}

						// call parent
						Clara.Reviewer.NewSubmission.IRBExpeditedFinalReviewPanel.superclass.initComponent
								.apply(this, arguments);
					}

				});
Ext.reg('clara.reviewer.newsubmission.irb.expedited.review.panel',
		Clara.Reviewer.NewSubmission.IRBExpeditedFinalReviewPanel);

Clara.Reviewer.ContinuingReview.IRBExpeditedFinalReviewPanel = Ext
.extend(
		Ext.FormPanel,
		{
			id : 'ContinuingReviewIRBExpeditedFinalReviewPanel',
			reviewPanelXml : {},
			title : 'IRB Expedited Review: Please provide the following information',
			reviewFormType : 'protocol', // or 'contract'
			constructor : function(config) {
				Clara.Reviewer.ContinuingReview.IRBExpeditedFinalReviewPanel.superclass.constructor
						.call(this, config);
			},
			validate : function() {
				var t = this;
				var valid = true;
				clog(t.items.items);
				for ( var i = 0; i < t.items.items.length; i++) {
					valid = valid && t.items.items[i].validate();
				}
				return valid;
			},
			getXML : function() {
				var t = this;
				
				return t.getFormXMLString();
			},

			getFormXMLString : function() {
				var t = this;
				var xml = "";
				xml += "<expedited><category>";
						//+ Ext.getCmp("fldExpeditedCategory").getValue()
						//+ "</category>";
				var categories = Ext.getCmp("fldExpeditedCategory").getValue();
				for ( var i = 0; i < categories.length; i++) {
					if (!categories[i].disabled) xml += "<value>" + categories[i].getName() + "</value>";
				}
				xml += "</category>";
				xml += "</expedited>";
				return xml;
			},

			initComponent : function() {
				var t = this;

				if (t.reviewPanelXml) {
					var xml = t.reviewPanelXml;

					t.items = [ /*new Ext.form.ComboBox({
						id: 'fldExpeditedCategory',
						labelStyle:'font-weight:800;',
						fieldLabel : 'Expedited Category',
						hiddenName : 'fldExpeditedCategory',
						store : new Ext.data.SimpleStore({
							fields : [ 'category' ],
							data : expeditedCategories
						}),
						displayField : 'category',
						allowBlank:false,
						typeAhead : true,
						mode : 'local',
						triggerAction : 'all',
						emptyText : 'Choose a category...',
						selectOnFocus : true*/
			            new Ext.form.CheckboxGroup({
							   id:'fldExpeditedCategory',
							   hideLabel:false,
							   labelStyle:'font-weight:800;',
							   fieldLabel:'Choose a category:',
							   xtype:'checkboxgroup',
							   name:'categorylist',
							   itemCls: 'x-check-group-alt',
							   columns:3,
							   vertical:true,
							   items:[
		    	    	        new Ext.form.Checkbox({hideLabel:true, boxLabel:'8', inputValue:'8', name:'8'}),
		    	    	        new Ext.form.Checkbox({hideLabel:true, boxLabel:'8b', inputValue:'8b', name:'8b'}),
		    	    	        new Ext.form.Checkbox({hideLabel:true, boxLabel:'8c', inputValue:'8c', name:'8c'}),
		    	    	        new Ext.form.Checkbox({hideLabel:true, boxLabel:'9', inputValue:'9', name:'9'}),
		    	    	        new Ext.form.Checkbox({hideLabel:true, boxLabel:'N/A', inputValue:'N/A', name:'N/A'}),
		    	    	        new Ext.form.Checkbox({hideLabel:true, boxLabel:'Not yet determined', inputValue:'Not yet determined', name:'Not yet determined'})
		    	    	        ]
					})
					
					];

				} else {
					clog(
							"[ERROR] Clara.Reviewer.ContinuingReview.IRBExpeditedFinalReviewPanel.initComponent(): reviewPanelXml not defined",
							t);
				}

				// call parent
				Clara.Reviewer.ContinuingReview.IRBExpeditedFinalReviewPanel.superclass.initComponent
						.apply(this, arguments);
			}

		});
Ext.reg('clara.reviewer.continuingreview.irb.expedited.review.panel',
Clara.Reviewer.ContinuingReview.IRBExpeditedFinalReviewPanel);

Clara.Reviewer.NewSubmission.IRBExemptFinalReviewPanel = Ext
.extend(
		Ext.FormPanel,
		{
			id : 'NewSubmissionIRBExemptFinalReviewPanel',
			reviewPanelXml : {},
			title : 'IRB Exempt Review: Please provide the following information',
			reviewFormType : 'protocol', // or 'contract'
			constructor : function(config) {
				Clara.Reviewer.NewSubmission.IRBExemptFinalReviewPanel.superclass.constructor
						.call(this, config);
			},
			validate : function() {
				var t = this;
				var valid = true;
				clog(t.items.items);
				for ( var i = 0; i < t.items.items.length; i++) {
					valid = valid && t.items.items[i].validate();
				}
				return valid;
			},
			getXML : function() {
				var t = this;
				//var xml = "<" + t.id + ">";
				//xml += t.getFormXMLString();
				//xml += "</" + t.id + ">";
				return t.getFormXMLString();
			},

			getFormXMLString : function() {
				var t = this;
				var xml = "";
				xml += "<exempt><category>";
						//+ Ext.getCmp("fldExemptCategory").getValue()
						//+ "</category>";
				var exemptcategories = Ext.getCmp("fldExemptCategory").getValue();
				for ( var i = 0; i < exemptcategories.length; i++) {
					if (!exemptcategories[i].disabled) xml += "<value>" + exemptcategories[i].getName() + "</value>";
				}
				xml += "</category>";
				xml += "<hipaa-waived>"+Ext.getCmp("fldQ3").getValue().inputValue+"</hipaa-waived>";
				xml += "</exempt>";
				return xml;
			},

			initComponent : function() {
				var t = this;

				if (t.reviewPanelXml) {
					var xml = t.reviewPanelXml;
					
					/*var exemptCategories = [
					                 ['1'],
					                 ['2'],
					                 ['3'],
					                 ['4'],
					                 ['5'],
					                 ['6'],
					                 ['N/A']
					             ];*/

					t.items = [ /*new Ext.form.ComboBox({
						id : 'fldExemptCategory',
						fieldLabel : 'Exempt Category',
						hiddenName : 'fldExemptCategory',
						store : new Ext.data.SimpleStore({
							fields : [ 'category' ],
							data : exemptCategories
						}),
						displayField : 'category',
						allowBlank:false,
						typeAhead : true,
						mode : 'local',
						triggerAction : 'all',
						emptyText : 'Choose a category...',
						selectOnFocus : true,
						labelStyle:'font-weight:800;'*/
			            new Ext.form.CheckboxGroup({
							   id:'fldExemptCategory',
							   hideLabel:false,
							   labelStyle:'font-weight:800;',
							   fieldLabel:'Choose a category:',
							   xtype:'checkboxgroup',
							   name:'exemptcategorylist',
							   itemCls: 'x-check-group-alt',
							   columns:2,
							   vertical:true,
							   items:[
		    	    	        new Ext.form.Checkbox({hideLabel:true, boxLabel:'1', inputValue:'1', name:'1'}),
		    	    	        new Ext.form.Checkbox({hideLabel:true, boxLabel:'2', inputValue:'2', name:'2'}),
		    	    	        new Ext.form.Checkbox({hideLabel:true, boxLabel:'3', inputValue:'3', name:'3'}),
		    	    	        new Ext.form.Checkbox({hideLabel:true, boxLabel:'4', inputValue:'4', name:'4'}),
		    	    	        new Ext.form.Checkbox({hideLabel:true, boxLabel:'5', inputValue:'5', name:'5'}),
		    	    	        new Ext.form.Checkbox({hideLabel:true, boxLabel:'6', inputValue:'6', name:'6'}),
		    	    	        new Ext.form.Checkbox({hideLabel:true, boxLabel:'N/A', inputValue:'N/A', name:'N/A'})
		    	    	        ]
					}),
					new Ext.form.RadioGroup({
						id: 'fldQ3',
						allowBlank:false,
						fieldLabel : 'HIPAA waived?',
						labelStyle:'font-weight:800;',
						columns: 1,
						items:[
								{boxLabel: 'Yes', name: 'rb-1', inputValue: 'yes'},
								{boxLabel: 'No', name: 'rb-1', inputValue: 'no'},
								{boxLabel: 'N/A', name: 'rb-1', inputValue: 'na'}
						       ]
					})
					];

				} else {
					clog(
							"[ERROR] Clara.Reviewer.NewSubmission.IRBExemptFinalReviewPanel.initComponent(): reviewPanelXml not defined",
							t);
				}

				// call parent
				Clara.Reviewer.NewSubmission.IRBExemptFinalReviewPanel.superclass.initComponent
						.apply(this, arguments);
			}

		});
Ext.reg('clara.reviewer.newsubmission.irb.exempt.review.panel',
Clara.Reviewer.NewSubmission.IRBExemptFinalReviewPanel);

Clara.Reviewer.ContinuingReview.IRBExemptFinalReviewPanel = Ext
.extend(
		Ext.FormPanel,
		{
			id : 'ContinuingReviewIRBExemptFinalReviewPanel',
			reviewPanelXml : {},
			title : 'IRB Exempt Review: Please provide the following information',
			reviewFormType : 'protocol', // or 'contract'
			constructor : function(config) {
				Clara.Reviewer.ContinuingReview.IRBExemptFinalReviewPanel.superclass.constructor
						.call(this, config);
			},
			validate : function() {
				var t = this;
				var valid = true;
				clog(t.items.items);
				for ( var i = 0; i < t.items.items.length; i++) {
					valid = valid && t.items.items[i].validate();
				}
				return valid;
			},
			getXML : function() {
				var t = this;
				//var xml = "<" + t.id + ">";
				//xml += t.getFormXMLString();
				//xml += "</" + t.id + ">";
				return t.getFormXMLString();
			},

			getFormXMLString : function() {
				var t = this;
				var xml = "";
				xml += "<exempt><category>";
						//+ Ext.getCmp("fldExemptCategory").getValue()
						//+ "</category>";
				var exemptcategories = Ext.getCmp("fldExemptCategory").getValue();
				for ( var i = 0; i < exemptcategories.length; i++) {
					if (!exemptcategories[i].disabled) xml += "<value>" + exemptcategories[i].getName() + "</value>";
				}
				xml += "</category>";
				xml += "</exempt>";
				return xml;
			},

			initComponent : function() {
				var t = this;

				if (t.reviewPanelXml) {
					var xml = t.reviewPanelXml;

					t.items = [ /*new Ext.form.ComboBox({
						id : 'fldExemptCategory',
						fieldLabel : 'Exempt Category',
						hiddenName : 'fldExemptCategory',
						store : new Ext.data.SimpleStore({
							fields : [ 'category' ],
							data : exemptCategories
						}),
						displayField : 'category',
						allowBlank:false,
						typeAhead : true,
						mode : 'local',
						triggerAction : 'all',
						emptyText : 'Choose a category...',
						selectOnFocus : true,
						labelStyle:'font-weight:800;'*/
			            new Ext.form.CheckboxGroup({
							   id:'fldExemptCategory',
							   hideLabel:false,
							   labelStyle:'font-weight:800;',
							   fieldLabel:'Choose a category:',
							   xtype:'checkboxgroup',
							   name:'exemptcategorylist',
							   itemCls: 'x-check-group-alt',
							   columns:2,
							   vertical:true,
							   items:[
		    	    	        new Ext.form.Checkbox({hideLabel:true, boxLabel:'1', inputValue:'1', name:'1'}),
		    	    	        new Ext.form.Checkbox({hideLabel:true, boxLabel:'2', inputValue:'2', name:'2'}),
		    	    	        new Ext.form.Checkbox({hideLabel:true, boxLabel:'3', inputValue:'3', name:'3'}),
		    	    	        new Ext.form.Checkbox({hideLabel:true, boxLabel:'4', inputValue:'4', name:'4'}),
		    	    	        new Ext.form.Checkbox({hideLabel:true, boxLabel:'5', inputValue:'5', name:'5'}),
		    	    	        new Ext.form.Checkbox({hideLabel:true, boxLabel:'6', inputValue:'6', name:'6'}),
		    	    	        new Ext.form.Checkbox({hideLabel:true, boxLabel:'N/A', inputValue:'N/A', name:'N/A'})
		    	    	        ]
					})
					];

				} else {
					clog(
							"[ERROR] Clara.Reviewer.ContinuingReview.IRBExemptFinalReviewPanel.initComponent(): reviewPanelXml not defined",
							t);
				}

				// call parent
				Clara.Reviewer.ContinuingReview.IRBExemptFinalReviewPanel.superclass.initComponent
						.apply(this, arguments);
			}

		});
Ext.reg('clara.reviewer.continuingreview.irb.exempt.review.panel',
Clara.Reviewer.ContinuingReview.IRBExemptFinalReviewPanel);

Clara.Reviewer.NewSubmission.RagulatoryFinalReviewPanel = Ext
.extend(
		Ext.FormPanel,
		{
			id : 'NewSubmissionRagulatoryFinalReviewPanel',
			reviewPanelXml : {},
			title : 'Ragulatory Review: Please provide the following information',
			reviewFormType : 'protocol', // or 'contract'
			constructor : function(config) {
				Clara.Reviewer.NewSubmission.RagulatoryFinalReviewPanel.superclass.constructor
						.call(this, config);
			},
			validate : function() {
				return true;
			},
			getXML : function() {
				var t = this;
				return t.getFormXMLString();
			},

			getFormXMLString : function() {
				var t = this;
				var xml = "";
				xml += "<ind>"
						+ Ext.getCmp("fldIndNumber").getValue()
						+ "</ind><ide>"
						+ Ext.getCmp("fldIdeNumber").getValue()
						+ "</ide>";
				return xml;
			},

			initComponent : function() {
				var t = this;

				if (t.reviewPanelXml) {
					var xml = t.reviewPanelXml;
			
					t.items = [ new Ext.form.TextField({
						id: 'fldIndNumber',
						fieldLabel : '<b>IND Number</b>',
						emptyText : 'Enter IND Number...',
						selectOnFocus : true
					}),
					new Ext.form.TextField({
						id: 'fldIdeNumber',
						fieldLabel : '<b>IDE Number</b>',
						emptyText : 'Enter IDE Number...',
						selectOnFocus : true
					})
					];

				} else {
					clog(
							"[ERROR] Clara.Reviewer.NewSubmission.RagulatoryFinalReviewPanel.initComponent(): reviewPanelXml not defined",
							t);
				}

				// call parent
				Clara.Reviewer.NewSubmission.RagulatoryFinalReviewPanel.superclass.initComponent
						.apply(this, arguments);
			}

		});
Ext.reg('clara.reviewer.newsubmission.ragulatory.review.panel',
Clara.Reviewer.NewSubmission.RagulatoryFinalReviewPanel);

function redirectAfterReviewSignoff(){
	if (claraInstance.user.committee != "PI"){
		location.href = appContext + "/queues?fromQueue="+fromQueue;
	}
	else location.href = appContext + "/" + claraInstance.type + "s/" +claraInstance.id+"/dashboard";
}

Clara.Reviewer.ContractAdminFinalReviewPanel = Ext
.extend(
		Ext.FormPanel,
		{
			id : 'NewContractContractAdminFinalReviewPanel',
			reviewPanelXml : {},
			title : 'Contract Admin: Please provide the following information',
			reviewFormType : 'contract', // or 'contract'
			constructor : function(config) {
				Clara.Reviewer.ContractAdminFinalReviewPanel.superclass.constructor
						.call(this, config);
			},
			validate : function() {
				return true;
			},
			getXML : function() {
				var t = this;
				return t.getFormXMLString();
			},

			getFormXMLString : function() {
				var t = this;
				var xml = "";
				xml += "<contract-begin-date>"
						+ Ext.getCmp("fldBeginDate").getRawValue()
						+ "</contract-begin-date><contract-end-date>"
						+ Ext.getCmp("fldEndDate").getRawValue()
						+ "</contract-end-date><contract-execution-date>"
						+ Ext.getCmp("fldExecutionDate").getRawValue()
						+ "</contract-execution-date>";
				return xml;
			},

			initComponent : function() {
				var t = this;

				if (t.reviewPanelXml) {
					var xml = t.reviewPanelXml;
			
					t.items = [ new Ext.form.DateField({
						id: 'fldBeginDate',
						fieldLabel : '<b>Contract term begin date</b>',
						format: 'm/d/Y',
						altFormats: 'm/d/Y', 
						emptyText : 'Begin date...'
					}),
					new Ext.form.DateField({
						id: 'fldEndDate',
						fieldLabel : '<b>Contract term end date</b>',
						format: 'm/d/Y',
						altFormats: 'm/d/Y', 
						emptyText : 'End date...'
					}),
					new Ext.form.DateField({
						id: 'fldExecutionDate',
						fieldLabel : '<b>Contract execution date</b>',
						format: 'm/d/Y',
						altFormats: 'm/d/Y', 
						emptyText : 'Execution date...'
					})
					];

				} else {
					clog(
							"[ERROR] Clara.Reviewer.NewContract.ContractAdminFinalReviewPanel.initComponent(): reviewPanelXml not defined",
							t);
				}

				// call parent
				Clara.Reviewer.ContractAdminFinalReviewPanel.superclass.initComponent
						.apply(this, arguments);
			}

		});
Ext.reg('clarareviewercontractadminpanel',
Clara.Reviewer.ContractAdminFinalReviewPanel);


