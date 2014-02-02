Ext.ns('Clara.ContractForm');

Clara.ContractForm.SponsorWindow = Ext
		.extend(
				Ext.Window,
				{
					title : 'Add Contract Entities',
					width : 400,
					height : 300,
					layout : 'form',
					itemId : 'winSponsor',
					modal : true,
					id : 'winSponsor',
					editing : false,
					rec : {},
					padding : 6,
					initComponent : function() {
						var url = appContext + "/ajax/contracts/"
								+ claraInstance.id + "/contract-forms/"
								+ claraInstance.form.id
								+ "/contract-form-xml-datas/"
								+ claraInstance.form.xmlDataId + "/";
						var t = this;
						var rec = t.rec;
						var config = {
							buttons : [ {
								id : 'btnSave',
								text : 'Save',
								handler : function() {

									var name = jQuery("#fldName").val();
									var company = jQuery("#fldCompany").val();
									var department = jQuery("#fldDepartment")
											.val();
									var title = jQuery("#fldTitle").val();
									var phone = jQuery("#fldPhone").val();
									var fax = jQuery("#fldFax").val();
									var email = jQuery("#fldEmail").val();
									var address = jQuery("#fldAddress").val();

									if (name != "" && phone != "" && email != "" && fax != "" && address != "") {

										var xml = "<sponsor>";

										xml += "<name>" + Encoder.htmlEncode(name) + "</name>";
										xml += "<company>" + Encoder.htmlEncode(company)
												+ "</company>";
										xml += "<title>" + Encoder.htmlEncode(title) + "</title>";
										xml += "<department>" + Encoder.htmlEncode(department)
												+ "</department>";
										xml += "<phone>" + Encoder.htmlEncode(phone) + "</phone>";
										xml += "<fax>" + Encoder.htmlEncode(fax) + "</fax>";
										xml += "<email>" + Encoder.htmlEncode(email) + "</email>";
										xml += "<address>" + Encoder.htmlEncode(address)  
												+ "</address>";

										xml += "</sponsor>";

										url += "xml-elements/"
												+ ((rec
														&& typeof rec.data != "undefined" && rec.data.id) ? "update"
														: "add");
										clog("REC", rec);
										var data = {
											listPath : '/contract/sponsors/sponsor',
											elementXml : xml
										};

										if (rec && rec.data && rec.data.id) {
											data.elementId = rec.data.id;
										}

										jQuery
												.ajax({
													async : false,
													url : url,
													type : "POST",
													dataType : 'xml',
													data : data,
													success : function(data) {
														jQuery(data)
																.find('sponsor')
																.each(
																		function() {
																			clog(
																					"Sponsor saved.",
																					jQuery(
																							this)
																							.attr(
																									'id'));
																		});
													}
												});

										Ext.getCmp("contract-sponsor-panel")
												.reloadSponsors();

										t.close();
									} else {
										alert("Please enter a name, phone, fax, email and address to continue.");
									}
								}
							} ],
							items : [
									{
										xtype : 'textfield',
										id : 'fldCompany',
										fieldLabel : 'Contract Entity Name',
										anchor : '100%',
										value : (rec && rec.data && rec.data.company) ? rec.data.company
												: ""
									},
									{
										xtype : 'textfield',
										id : 'fldDepartment',
										fieldLabel : 'Department',
										anchor : '100%',
										value : (rec && rec.data && rec.data.department) ? rec.data.department
												: ""
									},
									{
										xtype : 'textfield',
										id : 'fldName',
										fieldLabel : 'Contact Name',
										allowEmpty:false,
										anchor : '100%',
										value : (rec && rec.data && rec.data.name) ? rec.data.name
												: ""
									},
									{
										xtype : 'textfield',
										id : 'fldTitle',
										fieldLabel : 'Contact Title',
										anchor : '100%',
										value : (rec && rec.data && rec.data.title) ? rec.data.title
												: ""
									},
									{
										xtype : 'textfield',
										id : 'fldEmail',
										allowEmpty:false,
										fieldLabel : 'Contact Email',
										anchor : '100%',
										value : (rec && rec.data && rec.data.email) ? rec.data.email
												: ""
									},
									{
										xtype : 'textfield',
										id : 'fldPhone',
										allowEmpty:false,
										fieldLabel : 'Contact Phone',
										anchor : '100%',
										value : (rec && rec.data && rec.data.phone) ? rec.data.phone
												: ""
									},
									{
										xtype : 'textfield',
										id : 'fldFax',
										allowEmpty:false,
										fieldLabel : 'Contact Fax',
										anchor : '100%',
										value : (rec && rec.data && rec.data.fax) ? rec.data.fax
												: ""
									},
									{
										xtype : 'textfield',
										id : 'fldAddress',
										allowEmpty:false,
										fieldLabel : 'Contact Address',
										anchor : '100%',
										value : (rec && rec.data && rec.data.address) ? rec.data.address
												: ""
									} ]
						};

						Ext.apply(this, Ext.apply(this.initialConfig, config));
						Clara.ContractForm.SponsorWindow.superclass.initComponent
								.apply(this, arguments);

					}

				});

Clara.ContractForm.SponsorPanel = Ext
		.extend(
				Ext.grid.GridPanel,
				{
					id : 'contract-sponsor-panel',
					frame : false,
					trackMouseOver : false,
					selectedSponsor : {},

					constructor : function(config) {
						Clara.ContractForm.SponsorPanel.superclass.constructor
								.call(this, config);
					},

					reloadSponsors : function() {
						clog("loading sponsors");
						this.getStore().removeAll();
						this.getStore().load({
							params : {
								listPath : '/contract/sponsors/sponsor'
							}
						});
						Ext.getCmp("btnRemoveSponsor").setDisabled(true);
					},

					removeSelectedSponsor : function() {
						var ajaxBaseUrl = appContext + "/ajax/contracts/"
								+ claraInstance.id + "/contract-forms/"
								+ claraInstance.form.id
								+ "/contract-form-xml-datas/"
								+ claraInstance.form.xmlDataId + "/";

						var sponsorId = this.selectedSponsor.get("id");
						if (sponsorId != "") {
							url = ajaxBaseUrl + "xml-elements/delete";
							data = {
								listPath : "/contract/sponsors/sponsor",
								elementId : sponsorId
							};

							jQuery.ajax({
								async : false,
								url : url,
								type : "POST",
								dataType : 'xml',
								data : data
							});

						}
						this.reloadSponsors();
					},

					initComponent : function() {
						var url = appContext + "/ajax/contracts/"
								+ claraInstance.id + "/contract-forms/"
								+ claraInstance.form.id
								+ "/contract-form-xml-datas/"
								+ claraInstance.form.xmlDataId + "/";
						var t = this;
						var config = {
							store : new Ext.data.XmlStore({
								proxy : new Ext.data.HttpProxy({
									url : url + "xml-elements/list",
									method : "GET",
									headers : {
										'Accept' : 'application/xml;'
									}
								}),
								listeners : {
									exception : function(dp, type, action, opt,
											resp, arg) {
										alert('load failed -- ' + type + ' .. '
												+ action);
									}
								},
								baseParams : {
									listPath : '/contract/sponsors/sponsor'
								},
								record : 'sponsor',
								root : 'list',
								autoLoad : true,
								fields : [ {
									name : 'id',
									mapping : '@id'
								}, {
									name : 'company'
								}, {
									name : 'name'
								}, {
									name : 'title'
								}, {
									name : 'phone'
								}, {
									name : 'department'
								}, {
									name : 'email'
								}, {
									name : 'fax'
								}, {
									name : 'address'
								} ]
							}),
							viewConfig : {
								forceFit : true
							},
							tbar : new Ext.Toolbar(
									{
										items : [
												{
													text : 'Add Contract Entity',
													iconCls : 'icn-user--plus',
													handler : function() {
														var dw = new Clara.ContractForm.SponsorWindow();
														dw.show();
													}
												},
												'-',
												{
													text : 'Remove Contract Entity',
													id : 'btnRemoveSponsor',
													disabled : true,
													iconCls : 'icn-user--minus',
													handler : function() {
														Ext.Msg
																.show({
																	title : 'Remove contract entity?',
																	msg : 'Are you sure you want to remove this contract entity?',
																	buttons : Ext.Msg.YESNOCANCEL,
																	fn : function(
																			btn) {
																		if (btn == 'yes') {
																			t
																					.removeSelectedSponsor();
																		}
																	},
																	animEl : 'elId',
																	icon : Ext.MessageBox.WARNING
																});
													}
												} ]
									}),
							sm : new Ext.grid.RowSelectionModel({
								singleSelect : true
							}),
							loadMask : new Ext.LoadMask(Ext.getBody(), {
								msg : "Please wait..."
							}),
							columns : [

									{
										header : 'Contract entity',
										dataIndex : 'company',
										sortable : true,
										renderer : function(value, p, record) {
											var s = "";
											s += "<div class='sponsor-name'>"
													+ record.get("name");
											s += (record.get("title") != "") ? (" ("
													+ record.get("title") + ")")
													: ")";
											s += "</div>";
											s += "<div class='sponsor-company'>"
													+ record.get("company");
											s += (record.get("department") != "") ? (": " + record
													.get("department"))
													: "";
											s += "</div>";
											return s;
										}
									},
									{
										header : 'Email',
										dataIndex : 'email',
										sortable : false,
										width : 100,
										renderer : function(v) {
											return "<a href='mailto:" + v
													+ "'>" + v + "</a>";
										}
									}, {
										header : 'Phone',
										dataIndex : 'phone',
										sortable : false,
										width : 80
									}, {
										header : 'Fax',
										dataIndex : 'fax',
										sortable : false,
										width : 80
									}, {
										header : 'Address',
										dataIndex : 'address',
										sortable : false,
										width : 100
									} ],
							listeners : {
								rowdblclick : function(grid, rowI, event) {
									new Clara.ContractForm.SponsorWindow({
										editing : true,
										rec : grid.getStore().getAt(rowI)
									}).show();
								},
								rowclick : function(grid, rowI, event) {
									Ext.getCmp("btnRemoveSponsor").setDisabled(
											false);
									Ext.getCmp("contract-sponsor-panel").selectedSponsor = grid
											.getStore().getAt(rowI);
								}
							}
						};

						Ext.apply(this, Ext.apply(this.initialConfig, config));
						Clara.ContractForm.SponsorPanel.superclass.initComponent
								.apply(this, arguments);

					}

				});
Ext.reg('claracontractsponsorpanel', Clara.ContractForm.SponsorPanel);