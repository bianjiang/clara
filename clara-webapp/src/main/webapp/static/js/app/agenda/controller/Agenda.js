Ext
		.define(
				'Clara.Agenda.controller.Agenda',
				{
					extend : 'Ext.app.Controller',
					stores : [ 'Agendas' ],
					refs : [ {
						ref : 'agendas',
						selector : 'agendagridpanel'
					}, {
						ref : 'agendaItems',
						selector : 'agendaitemgridpanel'
					}, {
						ref : 'searchAgendasByIRBField',
						selector : '#fldSearchAgendasByIRB'
					}, {
						ref : 'searchAgendasButton',
						selector : '#btnSearchAgendas'
					}, {
						ref : 'clearSearchAgendasButton',
						selector : '#btnClearSearchAgendas'
					}, {
						ref : 'startMeetingButton',
						selector : '#btnStartMeeting'
					}, {
						ref : 'showMinutesButton',
						selector : '#btnShowMinutes'
					}, {
						ref : 'showSummaryButton',
						selector : '#btnShowSummary'
					}, {
						ref : 'agendaMenu',
						selector : '#btnAgendaMenu'
					}, {
						ref : 'addMinutesAgendaButton',
						selector : '#btnAddMinutesAgenda'
					}, {
						ref : 'sendAgendaButton',
						selector : '#btnSendAgenda'
					}, {
						ref : 'manageAgendaRosterButton',
						selector : '#btnManageAgendaRoster'
					}, {
						ref : 'cancelAgendaButton',
						selector : '#btnCancelAgenda'
					}, {
						ref : 'removeAgendaButton',
						selector : '#btnRemoveAgenda'
					}, {
						ref : 'agendaItemMenu',
						selector : '#btnAgendaItemMenu'
					}, {
						ref : 'assignReviewersButton',
						selector : '#btnAssignReviewers'
					}, {
						ref : 'removeAgendaItemButton',
						selector : '#btnRemoveItem'
					}, {
						ref : 'approveAgendaButton',
						selector : '#btnApproveAgenda'
					}, {
						ref : 'printAgendaButton',
						selector : '#btnPrintAgenda'
					} ],

					init : function() {
						var me = this;

						// Start listening for controller events
						me.on("agendaItemsUpdated", function() {
							me.onAgendaItemUpdate();
						});
						me.on("agendasUpdated", function() {
							me.onAgendaUpdate();
							me.getAgendasStore().load();
						});

						// Start listening for events on views
						me.control({
							'agendagridpanel' : {
								itemclick : me.onAgendaSelect
							},
							'agendaitemgridpanel' : {
								itemclick : me.onAgendaItemSelect,
								itemOrderChanged : me.reorderAgendaItems
							},
							'button[action=create_agenda]' : {
								click : function() {
									me.showCreateAgendaWindow();
								}
							},
							'#btnSearchAgendas' : {
								click : function() {
									var irbValue = me.getSearchAgendasByIRBField().getValue();
									if (irbValue && irbValue != "" && irbValue > 0){
										clog("Searching for "+irbValue);
										me.searchAgendasByIRB(irbValue);
									}else{
										alert("Nothing to search for.");
									}
								}
							},
							'#btnClearSearchAgendas' : {
								click : function() {
									me.getSearchAgendasByIRBField().reset();
									me.getClearSearchAgendasButton().setDisabled(true);
									me.getAgendasStore().getProxy().url = appContext+"/ajax/agendas/list";
									me.fireEvent("agendasUpdated");
								}
							},
							'#fldSearchAgendasByIRB' : {
								change : function(fld) {
									if (!fld.getValue() || fld.getValue() == 0 || fld.getValue() == "") {
										me.getClearSearchAgendasButton().setDisabled(true);
										me.getSearchAgendasButton().setDisabled(true);
									} else {
										me.getClearSearchAgendasButton().setDisabled(false);
										me.getSearchAgendasButton().setDisabled(false);
									}
								}
							},
							'#btnNewAgenda' : {
								click : function() {
									me.createAgenda();
								}
							},
							'#btnStartMeeting' : {
								click : function() {
									me.showMeetingPage();
								}
							},
							'#btnShowSummary' : {
								click : function() {
									me.showSummaryPage();
								}
							},
							'#btnShowMinutes' : {
								click : function() {
									me.showMinutesPage();
								}
							},
							'#btnAddMinutesAgenda' : {
								click : function() {
									me.addMinutesToAgenda();
								}
							},
							'#btnCancelAgenda' : {
								click : function() {
									me.cancelAgenda();
								}
							},
							'#btnRemoveAgenda' : {
								click : function() {
									me.removeAgenda();
								}
							},
							'#btnSendAgenda' : {
								click : function() {
									me.sendAgenda();
								}
							},
							'#btnApproveAgenda' : {
								click : function() {
									me.approveAgenda();
								}
							},
							'#btnManageAgendaRoster' : {
								click : function() {
									me.manageAgendaRoster();
								}
							},
							'#btnAssignReviewers' : {
								click : function() {
									me.assignReviewers();
								}
							},
							'#btnRemoveItem' : {
								click : function() {
									me.removeAgendaItem();
								}
							},
							'#btnPrintAgenda' : {
								click : function() {
									me.printAgenda();
								}
							}
						});

						Ext.data.StoreManager.lookup(
								'Clara.Common.store.IrbRosters').load();
					},

					loadingMask : new Ext.LoadMask(Ext.getBody(), {
						msg : "Please wait..."
					}),

					selectedAgenda : null,
					selectedAgendaItem : null,
					agendaItemEditReadOnlyStatuses : [ "MEETING_IN_PROGRESS",
							"CANCELLED",
							"MEETING_ADJOURNED_PENDING_CHAIR_APPROVAL",
							"MEETING_ADJOURNED",
							"MEETING_ADJOURNED_PENDING_IRB_OFFICE_PROCESS",
							"MEETING_CLOSED" ],

					showMeetingPage : function() {
						window.open(appContext + "/agendas/"
								+ this.selectedAgenda.get("id") + "/meeting");
					},

					showSummaryPage : function() {
						window.open(appContext + "/agendas/"
								+ this.selectedAgenda.get("id") + "/summary");
					},

					showMinutesPage : function() {
						window.open(appContext + "/agendas/"
								+ this.selectedAgenda.get("id") + "/minutes");
					},

					searchAgendasByIRB: function(irb){
						var me = this;
						me.onAgendaUpdate();
						me.getAgendasStore().getProxy().url = appContext+"/ajax/agendas/search";
						me.getAgendasStore().load({params:{'protocolId':irb}});
					},
					
					assignReviewers : function() {
						Ext.create(
								'Clara.Agenda.view.AgendaItemReviewerWindow',
								{}).show();
					},

					reorderAgendaItems : function() {
						var me = this;
						var agendaItemIds = [];

						me.getAgendaItems().getStore().each(function(rec) {
							agendaItemIds.push(rec.get("id"));
						});
						clog("Order array", agendaItemIds);
						me.loadingMask.show();
						Ext.Ajax.request({
							method : 'POST',
							url : appContext + "/ajax/agendas/"
									+ me.selectedAgenda.get("id")
									+ "/agenda-items/set-order",
							params : {
								agendaItemIds : agendaItemIds
							},
							success : function(response) {
								clog('reorderAgendaItems: Ext.Ajax success',
										response);
								me.loadingMask.hide();
								me.fireEvent("agendaItemsUpdated");
							},
							failure : function(error) {
								cwarn('reorderAgendaItems: Ext.Ajax failure',
										error);
								me.loadingMask.hide();
							}
						});

					},

					removeAgendaItem : function() {
						var me = this;
						var disabledReportableTypes = [ "continuing-review",
								"new-submission", "modification",
								"reportable-new-information" ];
						if (!(Ext.Array
								.contains(disabledReportableTypes,
										me.selectedAgendaItem
												.get("protocolFormTypeId")) && me.selectedAgendaItem
								.get("catagory") == "REPORTED")
								&& me.selectedAgenda.get("agendaStatus") != null
								&& me.selectedAgenda.get("agendaStatus") != 'MEETING_ADJOURNED') {
							Ext.Msg
									.show({
										title : 'Remove item from agenda',
										msg : 'Removing this item will move it back to the IRB office queue (any existing comments or contingencies for this item will be saved). Are you sure you want to do this?',
										buttons : Ext.Msg.YESNO,
										fn : function(btn) {
											if (btn == 'yes') {
												me.loadingMask.show();
												Ext.Ajax
														.request({
															method : 'GET',
															url : appContext
																	+ "/ajax/agendas/"
																	+ me.selectedAgenda
																			.get("id")
																	+ "/agenda-items/"
																	+ me.selectedAgendaItem
																			.get("id")
																	+ "/remove",
															params : {
																userId : claraInstance.user.id
															},
															success : function(
																	response) {
																clog(
																		'removeAgendaItem: Ext.Ajax success',
																		response);
																me.loadingMask
																		.hide();
																me
																		.fireEvent("agendaItemsUpdated");
															},
															failure : function(
																	error) {
																cwarn(
																		'removeAgendaItem: Ext.Ajax failure',
																		error);
																me.loadingMask
																		.hide();
															}
														});
											}
										},
										animEl : 'elId',
										icon : Ext.MessageBox.WARNING
									});
						} else {
							alert("You cannot remove this item.");
						}
					},

					printAgenda : function() {
						var me = this;
						Ext.ux.grid.Printer.title = "Agenda items for "
								+ me.selectedAgenda.get("date");
						Ext.ux.grid.Printer.printAutomatically = false;
						Ext.ux.grid.Printer.print(me.getAgendaItems());
					},

					createAgenda : function() {
						Ext.create('Clara.Agenda.view.NewAgendaWindow', {})
								.show();
					},

					approveAgenda : function() {
						var me = this;

						Ext.Msg
								.show({
									title : 'Approve agenda?',
									msg : 'Once you approve this agenda, notifications will be sent out to the committee members. The IRB Office may add items during this period, and they will be responsible for any further notifications to the committee. Continue?',
									buttonText : {
										'yes' : 'Approve and Send Notifications',
										'no' : 'Cancel'
									},
									fn : function(btn) {
										if (btn == 'yes') {
											me.loadingMask.show();
											Ext.Ajax
													.request({
														timeout : 60000,
														method : 'POST',
														url : appContext
																+ "/ajax/agendas/"
																+ me.selectedAgenda
																		.get("id")
																+ "/approve",
														params : {
															userId : claraInstance.user.id
														},
														success : function(
																response) {
															clog(
																	'approveAgenda: Ext.Ajax success',
																	response);
															me.loadingMask
																	.hide();
															me
																	.fireEvent("agendasUpdated");
														},
														failure : function(
																error) {
															cwarn(
																	'approveAgenda: Ext.Ajax failure',
																	error);
															me.loadingMask
																	.hide();
														}
													});

										}
									},
									icon : Ext.MessageBox.QUESTION
								});
					},

					removeAgenda : function() {
						var me = this;

						Ext.Msg
								.show({
									title : 'Remove Agenda',
									width : 350,
									msg : '<h1>Are you sure you want to remove this agenda?</h1>Items on the agenda will be moved back to the IRB office queue for reassignment.',
									buttons : Ext.Msg.OKCANCEL,
									fn : function(btn) {
										if (btn == 'ok') {

											me.loadingMask.show();
											Ext.Ajax
													.request({
														method : 'GET',
														url : appContext
																+ "/ajax/agendas/"
																+ me.selectedAgenda
																		.get("id")
																+ "/remove",
														params : {
															userId : claraInstance.user.id
														},
														success : function(
																response) {
															clog(
																	'removeAgenda: Ext.Ajax success',
																	response);
															me.loadingMask
																	.hide();
															me
																	.fireEvent("agendasUpdated");
														},
														failure : function(
																error) {
															cwarn(
																	'removeAgenda: Ext.Ajax failure',
																	error);
															me.loadingMask
																	.hide();
														}
													});

										}
									},
									icon : Ext.MessageBox.WARNING
								});
					},

					cancelAgenda : function() {
						var me = this;

						Ext.Msg
								.show({
									title : 'Cancel Agenda',
									prompt : true,
									width : 350,
									msg : '<h1>Why are you cancelling this agenda?</h1>Provide a reason below (ex. "Inclement Weather") for cancelling the agenda. Cancelled agendas remain in the agenda list for auditing purposes.',
									buttons : Ext.Msg.OKCANCEL,
									fn : function(btn, reason) {
										if (btn == 'ok') {

											me.loadingMask.show();
											Ext.Ajax
													.request({
														method : 'POST',
														url : appContext
																+ "/ajax/agendas/"
																+ me.selectedAgenda
																		.get("id")
																+ "/cancel",
														params : {
															userId : claraInstance.user.id,
															reason : reason
														},
														success : function(
																response) {
															clog(
																	'cancelAgenda: Ext.Ajax success',
																	response);
															me.loadingMask
																	.hide();
															me
																	.fireEvent("agendasUpdated");
														},
														failure : function(
																error) {
															cwarn(
																	'cancelAgenda: Ext.Ajax failure',
																	error);
															me.loadingMask
																	.hide();
														}
													});

										}
									},
									icon : Ext.MessageBox.WARNING
								});
					},

					sendAgenda : function() {
						var me = this;

						var firstTime = (me.selectedAgenda.get("agendaStatus") == null || me.selectedAgenda
								.get("agendaStatus") == 'AGENDA_INCOMPLETE');
						var title = firstTime ? "Send Agenda to Chair"
								: "Resend agenda";
						var msg = firstTime ? "<h1>This agenda will be sent to the chair for approval.</h1>Once approved, notifications will automatically be sent to the agenda committee members. Do you want to send this agenda to the chair?"
								: "<h1>This agenda has already been approved by the chair.</h1>Why are you resending this agenda to agenda comittee members?";

						Ext.Msg
								.show({
									title : title,
									prompt : !firstTime,
									width : 350,
									msg : msg,
									buttons : Ext.Msg.OKCANCEL,
									fn : function(btn, reason) {
										if (btn == 'ok') {
											var params = {
												userId : claraInstance.user.id
											};
											if (reason)
												params.reason = reason;
											me.loadingMask.show();
											Ext.Ajax
													.request({
														method : 'POST',
														url : appContext
																+ "/ajax/agendas/"
																+ me.selectedAgenda
																		.get("id")
																+ "/send-for-approval",
														params : params,
														success : function(
																response) {
															clog(
																	'sendAgenda: Ext.Ajax success',
																	response);
															me.loadingMask
																	.hide();
															me
																	.fireEvent("agendasUpdated");
														},
														failure : function(
																error) {
															cwarn(
																	'sendAgenda: Ext.Ajax failure',
																	error);
															me.loadingMask
																	.hide();
														}
													});

										}
									},
									icon : Ext.MessageBox.WARNING
								});
					},

					addMinutesToAgenda : function() {
						var me = this;
						if (me.selectedAgenda.get("agendaStatus") != null
								&& me.selectedAgenda.get("agendaStatus") != 'MEETING_ADJOURNED') {
							me.loadingMask.show();
							Ext.Ajax
									.request({
										method : 'POST',
										url : appContext + "/ajax/agendas/"
												+ me.selectedAgenda.get("id")
												+ "/add-minutes",
										params : {
											userId : claraInstance.user.id
										},
										success : function(response) {
											clog(
													'addMinutesToAgenda: Ext.Ajax success',
													response);
											me.loadingMask.hide();
											me.fireEvent("agendaItemsUpdated");
										},
										failure : function(error) {
											cwarn(
													'addMinutesToAgenda: Ext.Ajax failure',
													error);
											me.loadingMask.hide();
										}
									});

						}
					},

					manageAgendaRoster : function() {
						Ext.create('Clara.Agenda.view.AgendaRosterWindow', {})
								.show();
					},

					renderAgendaRosterRow : function(v, p, r) {
						var row = "<div class='agenda-reviewer-row'>";
						var reason = (r.get("reason") != '') ? r.get("reason")
								: 'No reason given';

						if (r.get("status") == "NORMAL" || !r.get("status"))
							row += "<h2>" + r.get("user").person.firstname
									+ " " + r.get("user").person.lastname
									+ "</h2><span>" + r.get("degree") + " - "
									+ r.get("type") + " - "
									+ r.get("user").person.workphone
									+ "</span>";
						else if (r.get("status") == "REPLACED") {
							row += "<h2><span style='text-decoration: line-through;color:red;'>"
									+ r.get("user").person.firstname
									+ " "
									+ r.get("user").person.lastname
									+ "</span>&nbsp;&nbsp;&nbsp;"
									+ r.get("altuser").person.firstname
									+ " "
									+ r.get("altuser").person.lastname
									+ "</h2><span style='text-decoration: line-through;color:red;'>"
									+ r.get("degree")
									+ " - "
									+ r.get("type")
									+ " - "
									+ r.get("user").person.workphone
									+ "</span>&nbsp;&nbsp;&nbsp;"
									+ r.get("altuser").person.workphone;
							row += "<div class='agenda-reviewer-row-alternative-reason'>"
									+ reason
									+ " - <a href='javascript:;' onClick='Clara.Agenda.app.getController(\"AgendaRoster\").resetReviewerStatus("
									+ r.get("id") + ");'>Undo</a></div>";
						} else if (r.get("status") == "REMOVED") {
							row += "<h2><span style='text-decoration: line-through;color:red;'>"
									+ r.get("user").person.firstname
									+ " "
									+ r.get("user").person.lastname
									+ "</span></h2><span style='text-decoration: line-through;color:red;'>"
									+ r.get("degree")
									+ " - "
									+ r.get("type")
									+ " - "
									+ r.get("user").person.workphone
									+ "</span>";
							row += "<div class='agenda-reviewer-row-alternative-reason'>"
									+ reason
									+ " - <a href='javascript:;' onClick='Clara.Agenda.app.getController(\"AgendaRoster\").resetReviewerStatus("
									+ r.get("id") + ");'>Undo</a></div>";
						} else if (r.get("status") == "ADDITIONAL") {
							row += "<h2><span style='color:blue;'>"
									+ r.get("user").person.firstname + " "
									+ r.get("user").person.lastname
									+ "</span></h2><span style='color:blue;'>"
									+ r.get("degree") + " - " + r.get("type")
									+ " - " + r.get("user").person.workphone
									+ "</span>";
							row += "<div class='agenda-reviewer-row-alternative-reason'>"
									+ reason
									+ " - <a href='javascript:;' onClick='Clara.Agenda.app.getController(\"AgendaRoster\").resetReviewerStatus("
									+ r.get("id") + ");'>Undo</a></div>";
						}
						return row + "</div>";
					},

					onAgendaUpdate : function() {
						var me = this;
						clog("onAgendaUpdate");
						me.selectedAgenda = null;
						me.selectedAgendaItem = null;
						

						// Update UI / Button states
						Ext.getDom("agenda-item-details").innerHTML = "<span style='font-weight:100;'>No agenda selected.</span>";
						Ext.getDom("agenda-item-count").innerHTML = "";

						me.getShowSummaryButton().hide();
						me.getShowMinutesButton().hide();
						me.getAgendaMenu().hide();
						me.getAgendaItemMenu().hide();
						me.getApproveAgendaButton().hide();
						me.getPrintAgendaButton().hide();
						me.getStartMeetingButton().hide();
					},

					onAgendaItemUpdate : function() {
						var me = this;
						clog("onAgendaItemUpdate");
						me.selectedAgendaItem = null;
						Ext.data.StoreManager.lookup('AgendaItems').load();

						// Update UI / Button states
						me.getAgendaItemMenu().setDisabled(true);
					},

					onAgendaSelect : function(gp, rec, item) {
						clog("Agenda Controller: onAgendaSelect", rec);
						var me = this;
						me.selectedAgenda = rec;

						// Load Agenda Items
						var agendaItemsStore = Ext.data.StoreManager
								.lookup('AgendaItems');
						agendaItemsStore.getProxy().url = appContext
								+ "/ajax/agendas/"
								+ me.selectedAgenda.get("id")
								+ "/agenda-items/list";

						Ext.apply(agendaItemsStore.proxy.extraParams, {
							hideReported : true
						});

						Ext.getCmp("cbShowReportedItems").setValue(false);

						agendaItemsStore
								.load({
									callback : function(recs) {
										Ext.getDom("agenda-item-count").innerHTML = recs.length
												+ " items - ";
									}
								});

						// Update UI / Button states
						Ext.getDom("agenda-item-details").innerHTML = "<span style='font-weight:800;'>"
								+ (Ext.util.Format.date(me.selectedAgenda
										.get("date"), 'm/d/Y')
										+ ":</span> "
										+ me.selectedAgenda
												.get("agendaLongStatusDesc")
										+ " (Committee "
										+ me.selectedAgenda.get("irbRoster")
												.replace("WEEK_", "") + ")");

						me.getShowSummaryButton().show();
						me.getShowMinutesButton().show();
						me.getAgendaItemMenu().setDisabled(true);

						me.getPrintAgendaButton().show();
						me.getPrintAgendaButton().setDisabled(false);

						// Disable / hide buttons if meeting has already started
						if (Ext.Array.contains(
								me.agendaItemEditReadOnlyStatuses,
								me.selectedAgenda.get("agendaStatus"))) {
							clog("Read-only agenda. Hiding menus",
									me.selectedAgenda.get("agendaStatus"));
							me.getAgendaMenu().hide();
							me.getAgendaItemMenu().hide();
						} else {

							var canEditAgenda = Clara
									.HasAnyPermissions([ 'EDIT_AGENDA' ]);
							if (canEditAgenda) {

								me.getAgendaMenu().show();
								me.getAgendaItemMenu().show();

								me.getAgendaMenu().setDisabled(false);
								me.getAgendaItemMenu().setDisabled(true);

								// Agenda menu
								me.getManageAgendaRosterButton().setDisabled(
										false);
								me.getSendAgendaButton().setDisabled(false);
								me.getCancelAgendaButton().setDisabled(false);
								me.getRemoveAgendaButton().setDisabled(false);
								me.getAddMinutesAgendaButton().setDisabled(
										false);

								// Agenda item menu
								me.getAssignReviewersButton().setDisabled(true);
								me.getRemoveAgendaItemButton()
										.setDisabled(true);
							}
						}

						if (Clara.IsAgendaChair(me.selectedAgenda
								.get("irbRoster"))
								&& me.selectedAgenda.get("agendaStatus") == 'AGENDA_PENDING_CHAIR_APPROVAL')
							me.getApproveAgendaButton().show();
						else
							me.getApproveAgendaButton().hide();

						if (Clara
								.HasAnyPermissions([
										'ROLE_IRB_MEETING_OPERATOR',
										'ROLE_IRB_CHAIR' ])) {
							me.getStartMeetingButton().show();
						} else {
							me.getStartMeetingButton().hide();
						}

					},

					onAgendaItemSelect : function(gp, rec, item) {
						var me = this;
						clog(rec);
						me.selectedAgendaItem = rec;

						// Agenda item menu
						me.getAgendaItemMenu().setDisabled(false);
						me.getRemoveAgendaItemButton().setDisabled(false);
						if (me.selectedAgendaItem.get("category") == 'FULL_BOARD') {
							me.getAssignReviewersButton().setDisabled(false);
						} else {
							me.getAssignReviewersButton().setDisabled(true);
						}
					},

					onLaunch : function() {
						LoadJs(appContext
								+ '/static/js/app/common/data/NameMappings.js');
					}
				});
