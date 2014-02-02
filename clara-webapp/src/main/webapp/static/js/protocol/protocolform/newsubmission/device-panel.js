Ext.ns('Clara.NewSubmission');

Clara.NewSubmission.ReloadDevices = function(){
	Ext.getCmp("protocol-device-panel").loadDevices();
};

Clara.NewSubmission.ConfirmRemoveDevice = function(device){
	Ext.Msg.show({
		title:"WARNING: About to delete a device",
		msg:"Are you sure you want to delete this device?", 
		buttons:Ext.Msg.YESNOCANCEL,
		icon:Ext.MessageBox.WARNING,
		fn: function(btn){
			if (btn == 'yes'){
				if (device){
					url = appContext + "/ajax/protocols/" + claraInstance.id + "/protocol-forms/" + claraInstance.form.id + "/protocol-form-xml-datas/" + claraInstance.form.xmlDataId + "/xml-elements/delete";

					data = {	
							listPath: "/protocol/devices/device",
							elementId: device.id
					};
					
					jQuery.ajax({
						async: false,
						url: url,
						type: "POST",
						dataType: 'xml',
						data: data
					});
					Clara.NewSubmission.ReloadDevices();
					Ext.getCmp("protocol-device-panel").selectedDevice = {};
				}
			}
		}
		
	});
	return false;
};

Clara.NewSubmission.ProtocolDevicePanel = Ext.extend(Ext.grid.GridPanel, {
	id: 'protocol-device-panel',
	frame:false,
	stripeRows:true,
	height:250,
	selectedDevice:{},
	
	constructor:function(config){		
		Clara.NewSubmission.ProtocolDevicePanel.superclass.constructor.call(this, config);
	},	
	
	loadDevices:function(){
		this.getStore().removeAll();
		this.getStore().load({params:{listPath:'/protocol/devices/device'}});
	},
	initComponent: function() {
		var config = {
				store:new Ext.data.XmlStore({
					autoLoad:false,
					proxy: new Ext.data.HttpProxy({
						
						url: appContext + "/ajax/protocols/" + claraInstance.id + "/protocol-forms/" + claraInstance.form.id + "/protocol-form-xml-datas/" + claraInstance.form.xmlDataId + "/xml-elements/list",
						method:"GET",
						headers:{'Accept':'application/xml;charset=UTF-8'}
					}),
					record: 'device', 
					fields: [
						{name:'id', mapping:'@id'},
						{name:'name', mapping:'@name'},
						{name:'identifier',mapping:'@identifier'},
						{name:'manufacturer', mapping:'@manufacturer'},
						{name:'modelnumber', mapping:'@modelnumber'},
						{name:'approved', mapping:'@approved'},
						{name:'status', mapping:'@status'},
						{name:'riskpotential',mapping:'@riskpotential'},
						{name:'riskhealthimpact',mapping:'@riskhealthimpact'},
						{name:'risksustainlife',mapping:'@risksustainlife'},
						{name:'riskimplant',mapping:'@riskimplant'},
						{name:'ide',mapping:'@ide'}
					]
				}),
				viewConfig: {
					forceFit:true
				},
				tbar: new Ext.Toolbar({
					items:[{
				    	text: 'Add Device',
				    	iconCls:'icn-plus-button',
				    	handler: function(){
							new Clara.NewSubmission.ProtocolDeviceWindow().show();
				    	}},'->',
				    	{
					    	text: 'Remove Device',
					    	iconCls:'icn-minus-button',
					    	handler: function(){
				    		clog(Ext.getCmp("protocol-device-panel").selectedDevice);
				    			Clara.NewSubmission.ConfirmRemoveDevice(Ext.getCmp("protocol-device-panel").selectedDevice);
					    }}
					]
				}),
				sm: new Ext.grid.RowSelectionModel({singleSelect: true}),
		        loadMask: new Ext.LoadMask(Ext.getBody(), {msg:"Please wait..."}),
		        columns: [
		                  {
		                	  	header:'Device Name',
		                	  	dataIndex:'lastname',
		                	  	sortable:true,
		                	  	renderer:function(value, p, record){
				                      var str = "<div class='gpRowLabel'>"+record.get('name')+"</div><div class='gpRowDesc'>";
				                      //<ul><li>Manufacturer: "+record.get('manufacturer')+"</li><li>Model: </li></div>";
				                      str += "<dl class='gpRowDefinitionList'>";
					        			
				                      str += "<dt>Manufacturer</dt><dd>" + record.get('manufacturer') + "</dd>";
				                      str += "<dt>Model #</dt><dd>" + record.get('modelnumber') + "</dd>";
					        			
					        			str += "</dl></div>";
					        				return str;
				                },
		                	  	width:250
		                  },
		                  {
		                	  	header:'IDE',
		                	  	dataIndex:'ide',
		                	  	sortable:false,
		                	  	width:100
		                  },
		                  {
		                  	  	header:'Significant Risk?',
		                  	  	dataIndex:'riskimplant',
		                  	  	sortable:false,
		                  	  	renderer:function(value, p, record){
				                  	if (record.data.riskpotential == 'y' || record.data.riskhealthimpact == 'y' || record.data.risksustainlife == 'y' || record.data.riskimplant == 'y'){
				                		return '<strong>Yes</strong>';
				                	} else {
				                		return '<strong>No</strong>';
				                	}
				                },
		                  	  	width:200
		                  }
		        ],
		        
			    listeners:{
				    rowdblclick: function(grid, rowI, event)   {
						clog("dblclick!!");
						var devicedata = grid.getStore().getAt(rowI).data;
						clog(devicedata);
						var device = new Clara.NewSubmission.Device({
							id:devicedata.id,
							identifier:devicedata.identifier,
							ide:devicedata.ide,
							name:devicedata.name,
							status:devicedata.status,
							manufacturer:devicedata.manufacturer,
							modelnumber:devicedata.modelnumber,
							approved:devicedata.approved,
							riskpotential:(devicedata.riskpotential == 'y')?true:false,
							riskhealthimpact:(devicedata.riskhealthimpact == 'y')?true:false,
							risksustainlife:(devicedata.risksustainlife == 'y')?true:false,
							riskimplant:(devicedata.riskimplant == 'y')?true:false
							
						});
												
						new Clara.NewSubmission.ProtocolDeviceWindow({editing:true, device:device}).show();
						
				    },
				    rowclick: function(grid, rowI, event)   {
						var devicedata = grid.getStore().getAt(rowI).data;
						clog(devicedata);
						var device = new Clara.NewSubmission.Device({
							id:devicedata.id,
							identifier:devicedata.identifier,
							ide:devicedata.ide,
							name:devicedata.name,
							status:devicedata.status,
							manufacturer:devicedata.manufacturer,
							modelnumber:devicedata.modelnumber,
							approved:devicedata.approved,
							riskpotential:(devicedata.riskpotential == 'y')?true:false,
							riskhealthimpact:(devicedata.riskhealthimpact == 'y')?true:false,
							risksustainlife:(devicedata.risksustainlife == 'y')?true:false,
							riskimplant:(devicedata.riskimplant == 'y')?true:false

						});
						
						clog(device);
						Ext.getCmp("protocol-device-panel").selectedDevice = device;
						
				    }
			    }
		};
		
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		
		Clara.NewSubmission.ProtocolDevicePanel.superclass.initComponent.apply(this, arguments);
		
	}
	

});
Ext.reg('claraprotocoldevicepanel', Clara.NewSubmission.ProtocolDevicePanel);