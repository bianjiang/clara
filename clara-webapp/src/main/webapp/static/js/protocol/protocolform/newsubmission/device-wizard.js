Ext.ns('Clara.NewSubmission');

Clara.NewSubmission.ProtocolDeviceSearchPanel = Ext.extend(Ext.Panel, {
	id: 'protocol-device-search-panel',
	title:'Search',
	layout:'border',
	border:false,
	constructor:function(config){		
		Clara.NewSubmission.ProtocolDeviceSearchPanel.superclass.constructor.call(this, config);
	},

	initComponent: function() {
		
		var config = {
				items: [{
					xtype:'uxsearchfield',
					store:Clara.NewSubmission.DeviceStore,
					allowBlank:false,
					region:'north',
					border:false,
					frame:false,
					height:24,
					name:'device-manufacturer',
					id:'device-manufacturer',
					emptyText:'Search by device manufacturer',
					paramName : 'name',
					listeners:{
				   		'render':function(c){
				   			c.getEl().on('keyup', function(e){
				   				if (jQuery.trim(c.getEl().getValue()) == '') {
				   					Ext.getCmp('protocol-device-detail-panel').setDisabled(true);
				   					Ext.getCmp('protocol-device-risk-panel').setDisabled(true);
				   					Ext.getCmp('btn-save-device').setDisabled(true);
				   				} else {
				   					Ext.getCmp('btn-save-device').setDisabled(false);
				   					Ext.getCmp('protocol-device-detail-panel').setDisabled(false);
				   					Ext.getCmp('protocol-device-risk-panel').setDisabled(false);
				   				}
				   			});
			   			}
			   		}
				},{
					xtype:'grid',
			    	frame:false,
			    	border:false,
			    	trackMouseOver:false,
			    	region: 'center',
			        store: Clara.NewSubmission.DeviceStore,
			        selModel: new Ext.grid.RowSelectionModel({
			        	singleSelect:true,
			        	listeners: {
			        		rowselect: function(grid,rowIndex,record){
			        					drec = record.data;
			        					var device = new Clara.NewSubmission.Device({
			        						manufacturer:drec.description,
			        						identifier:drec.id,
			        						approved:drec.approved
			        					});
			        					Ext.getCmp("protocol-device-window").device = device;
			        					jQuery('#device-manufacturer').val(device.manufacturer);
			        				}
			        	}
			        }),
			        loadMask: new Ext.LoadMask(Ext.getBody(), {msg:"Please wait..."}),
			        viewConfig: {
						forceFit:true,
			    		getRowClass: function(record, index){
			    			var approved = record.get('approved');
			    			if (approved) { return 'device-search-approved'; }
			    			else { return 'device-search-not-approved'; }
			    		}
			    	},
			    	
			        columns: [new Ext.grid.RowNumberer({width: 30}),
			                  {
			                	  	header:'Manufacturer',
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
						   boxLabel:"<span style='font-weight:800;font-size:14px;'>The device manufacturer wasn't found in the search results above.</span><br/>By checking this box, the name you entered above will be added, pending IRB approval.",
						   id:'custom-device-name',
						   name:'custom-device-name'
					   }]
				}],		
			    listeners:{}
		};
		
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		Clara.NewSubmission.ProtocolDeviceSearchPanel.superclass.initComponent.apply(this, arguments);
		Clara.NewSubmission.DeviceStore.removeAll();
	}
	

});
Ext.reg('claraprotocoldevicesearchpanel', Clara.NewSubmission.ProtocolDeviceSearchPanel);


Clara.NewSubmission.ProtocolDeviceRiskPanel = Ext.extend(Ext.FormPanel, {
	id: 'protocol-device-risk-panel',
	title:'Risks',
	border:false,
	labelWidth: 150,
	padding:6,
	plain:true,
	collapsed:false,
	collapsible:false,
	disabled:true,
	constructor:function(config){		
		Clara.NewSubmission.ProtocolDeviceRiskPanel.superclass.constructor.call(this, config);
	},

	initComponent: function() {
		
		var config = {
				items:[
					    new Ext.form.RadioGroup({	
							id:'risk-implant',
						    fieldLabel: 'Is this device intended as an implant that presents a potential for serious risk to the health, safety, or welfare of a subject?',
						    columns:[50,50],
						    items:[
						           {boxLabel:'Yes',inputValue:'y',name: 'risk-implant',width:50},
						           {boxLabel:'No',inputValue:'n',name: 'risk-implant',width:50}
						           ]
					    }),
					    new Ext.form.RadioGroup({	
							id:'risk-sustain-life',
						    fieldLabel: 'Is this device purported or represented to be for use supporting or sustaining human life and presents a potential for serious risk to the health, safety, or welfare of a subject?',
						    columns:[50,50],
						    items:[
						           {boxLabel:'Yes',inputValue:'y',name: 'risk-sustain-life',width:50},
						           {boxLabel:'No',inputValue:'n',name: 'risk-sustain-life',width:50}
						           ]
					    }),
					    new Ext.form.RadioGroup({	
							id:'risk-health-impact',
						    fieldLabel: 'Is for a use of substantial importance of human health and presents a potential for serious risk to the health, safety, or welfare of a subject?',
						    columns:[50,50],
						    items:[
						           {boxLabel:'Yes',inputValue:'y',name: 'risk-health-impact',width:50},
						           {boxLabel:'No',inputValue:'n',name: 'risk-health-impact',width:50}
						           ]
					    }),
					    new Ext.form.RadioGroup({	
							id:'risk-potential',
						    fieldLabel: 'Or, otherwise presents a potential for serious risk to the health, safety, or welfare of a subject?',
						    columns:[50,50],
						    items:[
						           {boxLabel:'Yes',inputValue:'y',name: 'risk-potential',width:50},
						           {boxLabel:'No',inputValue:'n',name: 'risk-potential',width:50}
						           ]
					    })
		        ]
		};
		
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		Clara.NewSubmission.ProtocolDeviceRiskPanel.superclass.initComponent.apply(this, arguments);
		
	}
	

});
Ext.reg('claraprotocoldeviceriskpanel', Clara.NewSubmission.ProtocolDeviceRiskPanel);

Clara.NewSubmission.ProtocolDeviceDetailPanel = Ext.extend(Ext.FormPanel, {
	id: 'protocol-device-detail-panel',
	title:'Details',
	border:false,
	labelWidth: 200,
	padding:6,
	plain:true,
	collapsed:false,
	collapsible:false,
	disabled:true,
	constructor:function(config){		
		Clara.NewSubmission.ProtocolDeviceDetailPanel.superclass.constructor.call(this, config);
	},

	initComponent: function() {
		
		var config = {
				items:[
						new Ext.form.TextField({	
						    fieldLabel: 'What is the <span style="font-weight:800;">device name</span>?',
						    id: 'device-name',
						    name: 'device-name'
						}),
						new Ext.form.TextField({	
						    fieldLabel: 'What is the <span style="font-weight:800;">model number</span> for this device?',
						    id: 'device-model-number',
						    name: 'device-model-number'
						}),
						new Ext.form.TextField({	
						    fieldLabel: 'What is the <span style="font-weight:800;">IDE number</span>?',
						    id: 'device-ide-number',
						    name: 'device-ide-number'
						}),
			    	       new Ext.form.ComboBox({
				    	   		width:240,
				    	      	fieldLabel:"What type of device is this?",
				    	      	typeAhead:false,
					    	   	store:Clara.NewSubmission.DeviceTypeStore, 
					    	   	forceSelection:true,
					    	   	displayField:'desc', 
					        	valueField:'value',
					    	   	mode:'remote', 
					    	   	triggerAction:'all',
					    	   	editable:false,
					    	   	allowBlank:false,
					    	   	name:'device-type',
					    	   	id:'device-type'
				    	   }),
				    	   new Ext.form.RadioGroup({	
								id:'device-manual',
							    fieldLabel: 'Is there a device manual?',
							    columns:[50,50],
							    items:[
							           {boxLabel:'Yes',inputValue:'y',name: 'device-manual',width:50},
							           {boxLabel:'No',inputValue:'n',name: 'device-manual',width:50}
							           ]
						    })
		    	       
		    	]
		};
		
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		Clara.NewSubmission.ProtocolDeviceDetailPanel.superclass.initComponent.apply(this, arguments);
		
	}
	

});
Ext.reg('claraprotocoldevicedetailpanel', Clara.NewSubmission.ProtocolDeviceDetailPanel);




Clara.NewSubmission.ProtocolDeviceWindow = Ext.extend(Ext.Window, {
	id: 'protocol-device-window',
	height:400,
	width:500,
	device:{},
	editing:false,
	modal:true,
	layout:'fit',
	border:false,
	constructor:function(config){		
		Clara.NewSubmission.ProtocolDeviceWindow.superclass.constructor.call(this, config);
	},

	

	initComponent: function() {
		

		
		var config = {
				items: [{
					xtype:'tabpanel',
					id:'tpDeviceWizard',
					//region:'center',
					deferredRender:false,
					activeTab:0,
					items:[{xtype:'claraprotocoldevicesearchpanel'},{xtype:'claraprotocoldevicedetailpanel'},{xtype:'claraprotocoldeviceriskpanel'}]
				}],
				buttons: [
							{
								text:'Close',
								disabled:false,
								handler: function(){
									Ext.getCmp('protocol-device-window').close();
								}
							},
							{
								text:'Save Device',
								id:'btn-save-device',
								disabled:true,
								handler: function(){
									var device = Ext.getCmp('protocol-device-window').device;
									
									if (typeof device.manufacturer != undefined && device.manufacturer !=''){
										var d = new Clara.NewSubmission.Device({
											id:device.id,
											identifier:device.identifier,
											approved:device.approved || false,
											name:jQuery("#device-name").val(),
											ide:jQuery("#device-ide-number").val(),
											devicemanual:jQuery("#device-manual").val(),
											manufacturer:jQuery("#device-manufacturer").val(),
											modelnumber:jQuery("#device-model-number").val(),
											status:jQuery("#device-type").val(),
											riskpotential:jQuery("input:radio[name=risk-potential]:checked").val(),
											riskhealthimpact:jQuery("input:radio[name=risk-health-impact]:checked").val(),
											risksustainlife:jQuery("input:radio[name=risk-sustain-life]:checked").val(),
											riskimplant:jQuery("input:radio[name=risk-implant]:checked").val(),
										});
																				
										clog(d);
										
										if (Ext.getCmp('protocol-device-window').editing){
											var deviceID = updateExistingXmlInProtocol("/protocol/devices/device", d.id, d.toXML());
										} else {
											var deviceID = addXmlToProtocol( "/protocol/devices/device", d.toXML(), "device");
										}
										Clara.NewSubmission.ReloadDevices();
										Ext.getCmp('protocol-device-window').close();
									} else {
										alert('Choose a device name and manufacturer first.');
									}
								}
				}],		
			    listeners:{
					afterrender:function(){
						if (this.editing){
							clog("FILLING OUT DEVICE");
							clog(this.device);
							jQuery("#device-ide-number").val(this.device.ide);
							jQuery("#device-manufacturer").val(this.device.manufacturer);
							jQuery("#device-manual").val(this.device.devicemanual);
							jQuery("#device-model-number").val(this.device.modelnumber);
							jQuery("#device-type").val(this.device.status);
							jQuery("#device-name").val(this.device.name);
							
							Ext.getCmp('risk-potential').setValue((this.device.riskpotential == true)?"y":"n");
							Ext.getCmp('risk-health-impact').setValue((this.device.riskhealthimpact == true)?"y":"n");
							Ext.getCmp('risk-sustain-life').setValue((this.device.risksustainlife == true)?"y":"n");
							Ext.getCmp('risk-implant').setValue((this.device.riskimplant == true)?"y":"n");


							
							// disable search tab (to prevent swapping unapproved for approved device names)
							
							Ext.getCmp("protocol-device-search-panel").disable();
							Ext.getCmp("protocol-device-detail-panel").enable();
							Ext.getCmp("protocol-device-risk-panel").enable();
							Ext.getCmp("btn-save-device").enable();
							Ext.getCmp("tpDeviceWizard").setActiveTab(1);
						}
					}
				}
		};
		
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		Clara.NewSubmission.ProtocolDeviceWindow.superclass.initComponent.apply(this, arguments);
		

		
	}
	

});
Ext.reg('claraprotocoldevicewindow', Clara.NewSubmission.ProtocolDeviceWindow);



