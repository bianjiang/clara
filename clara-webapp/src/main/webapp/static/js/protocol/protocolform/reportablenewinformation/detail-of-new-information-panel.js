Ext.ns('Clara.ReportableNewInformation');

Clara.ReportableNewInformation.ReloadNewInformation = function(){
	Ext.getCmp("reportable-new-information-panel").loadNewInformation();
};

Clara.ReportableNewInformation.ConfirmRemoveNewInformation = function(newinformation){
	Ext.Msg.show({
		title:"WARNING: About to delete a new information report",
		msg:"Are you sure you want to delete this new information?", 
		buttons:Ext.Msg.YESNO,
		icon:Ext.MessageBox.WARNING,
		fn: function(btn){
			if (btn == 'yes'){
				if (newinformation.id){
					url = appContext + "/ajax/protocols/" + claraInstance.id + "/protocol-forms/" + claraInstance.form.id + "/protocol-form-xml-datas/" + claraInstance.form.xmlDataId + "/xml-elements/delete";

					data = {	
							listPath: '/reportable-new-info/detail-of-new-information/events/event',
							elementId: newinformation.id
					};
					
					jQuery.ajax({
						async: false,
						url: url,
						type: "POST",
						dataType: 'xml',
						data: data
					});
					Clara.ReportableNewInformation.ReloadNewInformation();
					Ext.getCmp("reportable-new-information-panel").selectedNewInformation = {};
				}
			}
		}
		
	});
	return false;
};




Clara.ReportableNewInformation.NewInformationPanel = Ext.extend(Ext.grid.GridPanel, {
	id: 'reportable-new-information-panel',
	frame:false,
	trackMouseOver:false,
	height:250,
	selectedNewInformation:{},
	
	constructor:function(config){		
		Clara.ReportableNewInformation.NewInformationPanel.superclass.constructor.call(this, config);
	},	
	
	loadNewInformation:function(){
		this.getStore().removeAll();
		this.getStore().load({params:{listPath:'/reportable-new-info/detail-of-new-information/events/event'}});
	},	
	initComponent: function() {
		var t = this;
		
		var config = {
				store:Clara.ReportableNewInformation.EventStore,
				viewConfig: {
					forceFit:true
				},
				tbar: new Ext.Toolbar({
					items:[{
				    	text: 'Add a Event',
				    	iconCls:'icn-pill--plus',
				    	handler: function(){
							var dw = new Clara.ReportableNewInformation.DetailOfNewInformationWindow();
							dw.show();
				    	}},'-',				    	
				    	{
					    	text: 'Remove selected Event',
					    	iconCls:'icn-pill--minus',
					    	handler: function(){
					    		var selectedNewInformation = Ext.getCmp("reportable-new-information-panel").selectedNewInformation;
					    		
					    		if(selectedNewInformation.id){
					    			Clara.ReportableNewInformation.ConfirmRemoveNewInformation(Ext.getCmp("reportable-new-information-panel").selectedNewInformation);
					    		}else{
					    			alert("Please select the row you want to delete first!");
					    		}
					    }}
					]
				}),
				sm: new Ext.grid.RowSelectionModel({singleSelect: true}),
		        loadMask: new Ext.LoadMask(Ext.getBody(), {msg:"Please wait..."}),
		        columns: [
		                  /*{
		                	  	header:'Report #',
		                	  	dataIndex:'reportnumber',
		                	  	sortable:true,
		                	  	width:100
		                  },
		                  {
		                	  	header:'Report Type',
		                	  	dataIndex:'reporttype',
		                	  	sortable:false,
		                	  	width:100
		                  },
		                  {
		                	  	header:'Date of Report',
		                	  	dataIndex:'dateofreport',
		                	  	sortable:false,
		                	  	width:100
		                  }*/
		                  {
		                	  	header:'Date of Report',
		                	  	dataIndex:'dateofreport',
		                	  	sortable:true,
		                	  	width:100
		                  },
		                  {
		                	  	header:'Event Date',
		                	  	dataIndex:'dateofsaeonset',
		                	  	sortable:true,
		                	  	width:100
		                  },
		                  {
		                	  	header:'Participant Initials',
		                	  	dataIndex:'participantid',
		                	  	sortable:false,
		                	  	width:100
		                  },
		                  {
		                	  	header:'Additional Information',
		                	  	dataIndex:'additionalinformation',
		                	  	sortable:false,
		                	  	width:250
		                  }
		        ],
			    listeners:{
				    rowdblclick: function(grid, rowI, event)   {
						clog("dblclick!!");
						var newinformationdata = grid.getStore().getAt(rowI).data;

						var newinformation = new Clara.ReportableNewInformation.Event(newinformationdata);						
						
						Ext.getCmp("reportable-new-information-panel").selectedNewInformation = {};
						
						new Clara.ReportableNewInformation.DetailOfNewInformationWindow({editing:true, newinformation:newinformation}).show();
						
				    },
				    rowclick: function(grid, rowI, event)   {
						var newinformationdata = grid.getStore().getAt(rowI).data;
						//clog(newinformationdata);
						var newinformation = new Clara.ReportableNewInformation.Event(newinformationdata);
						
						
						Ext.getCmp("reportable-new-information-panel").selectedNewInformation = newinformation;
						
				    }
			    }
		};
		
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		
		Clara.ReportableNewInformation.NewInformationPanel.superclass.initComponent.apply(this, arguments);	
	}	

});
Ext.reg('clara-reportable-new-information-panel', Clara.ReportableNewInformation.NewInformationPanel);