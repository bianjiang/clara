Ext.ns('Clara.NewSubmission');

Clara.NewSubmission.ReloadDrugs = function(){
	Ext.getCmp("protocol-drug-panel").loadDrugs();
};

Clara.NewSubmission.ConfirmRemoveDrug = function(drug){
	Ext.Msg.show({
		title:"WARNING: About to delete a drug",
		msg:"Are you sure you want to delete this drug?", 
		buttons:Ext.Msg.YESNOCANCEL,
		icon:Ext.MessageBox.WARNING,
		fn: function(btn){
			if (btn == 'yes'){
				if (drug){
					url = appContext + "/ajax/protocols/" + claraInstance.id + "/protocol-forms/" + claraInstance.form.id + "/protocol-form-xml-datas/" + claraInstance.form.xmlDataId + "/xml-elements/delete";

					data = {	
							listPath: "/protocol/drugs/drug",
							elementId: drug.id
					};
					
					jQuery.ajax({
						async: false,
						url: url,
						type: "POST",
						dataType: 'xml',
						data: data
					});
					Clara.NewSubmission.ReloadDrugs();
					Ext.getCmp("protocol-drug-panel").selectedDrug = {};
				}
			}
		}
		
	});
	return false;
};




Clara.NewSubmission.ProtocolDrugPanel = Ext.extend(Ext.grid.GridPanel, {
	id: 'protocol-drug-panel',
	frame:false,
	stripeRows:true,
	height:250,
	selectedDrug:{},
	editable:true,
	pharmacyStatus:null,
	constructor:function(config){		
		Clara.NewSubmission.ProtocolDrugPanel.superclass.constructor.call(this, config);
		if(Clara.NewSubmission.DDMessageBus){
			Clara.NewSubmission.DDMessageBus.on('pharmacystatusupdated', this.onPharmacyUpdated, this);
			Clara.NewSubmission.DDMessageBus.on('pharmacyrequestmade', this.onPharmacyRequested, this);
		}
	},	
	
	onPharmacyRequested: function(status){
		var btn = Ext.getCmp("btnRequestPharmacy");
		btn.setText("Pharmacy review requested.");
		//disable drug panel
		this.setDisabled(true);
	},
	
	setEditable: function(editable){
		this.editable = editable;
		Ext.getCmp("btnAddInvDrug").setDisabled(!editable);
		Ext.getCmp("btnAddConvDrug").setDisabled(!editable);
		Ext.getCmp("btnRemoveDrug").setDisabled(!editable);
		
	},
	
	onPharmacyUpdated: function(status){
		status = status.toUpperCase();
		var prettyStatus = (status == "" || status == null)?"None":status;
		
		if (status == "IN_REVIEW_REQUESTED") prettyStatus = "Under Review";
		else if (status == "IN_WAIVER_REQUESTED") prettyStatus = "Under Review (waiver)";
		else if (status == "NO_PHARMACY_REVIEW") prettyStatus = "No pharmacy review required for this study.";

		this.pharmacyStatus = status;
		clog("onPharmacyUpdated: status is "+status);
		var disableButton = (status == "IN_REVIEW_REQUESTED" || status == "IN_WAIVER_REQUESTED");
		jQuery("#pharmacy-review-status").text(prettyStatus);
		var btn = Ext.getCmp("btnRequestPharmacy");
		var label = (disableButton)?prettyStatus:"Request Pharmacy Review";
		btn.setDisabled(disableButton);
		this.setEditable((status == "") || (status == "NO_PHARMACY_REVIEW") || claraInstance.user.committee == "PHARMACY_REVIEW");
		btn.setText(label);
		btn.setVisible((status != "NO_PHARMACY_REVIEW"));
	},
	
	loadDrugs:function(){
		this.getStore().removeAll();
		this.getStore().load({params:{listPath:'/protocol/drugs/drug'}});
	},

	confirmRequestPharmacyReviewWindow:function(){
		var t = this;
		new Ext.Window({
			id:'winConfirmRequestPharmacyReviewWindow',
			width:500,
			title:"Request Pharmacy Review",
			layout:"form",
			bodyPadding:6,
			labelAlign:'top',
			labelSeparator:'',
			modal:true,
			padding:6,
			buttons:[{
				text:'Cancel',
				handler:function(){
					Ext.getCmp("winConfirmRequestPharmacyReviewWindow").close();
				}
			},{
				text:'Make Request',
				handler:function(){
					clog(Ext.getCmp("cbPharmacyWaiver").getValue());
					t.requestPharmacyReview(Ext.getCmp("cbPharmacyWaiver").getValue());
					Ext.getCmp("winConfirmRequestPharmacyReviewWindow").close();
				}
			}],
			items:[{
				xtype:'displayfield',
				hideLabel:true,
				value:'<span style="font-size:16px;">While the pharmacy reviews your submission, your ability to edit the drugs will be disabled.</span>'
			},{
				xtype:'textarea',
				id:'taPharmacyNotes',
				anchor:'100%',
				fieldLabel:'<span style="font-weight:800;">Note to pharmacy</span><br/>This will appear in the history for this form.'
			},{
				xtype:'checkbox',
				id:'cbPharmacyWaiver',
				hideLabel:true,
				boxLabel:'<span style="font-weight:800;">Request a waiver for pharmacy fees</span>'
			}]
		}).show();
	},
	
	requestPharmacyReview:function(requestWaiver){
		requestWaiver = requestWaiver || false;
		var status="";
		url = appContext + "/ajax/protocols/" + claraInstance.id + "/protocol-forms/" + claraInstance.form.id + "/review/new-submission/request-review";
		data = {	
				committee: "PI",
				committeeRequest: "PHARMACY_REVIEW",
				action:(requestWaiver === true)?"REQUEST_WAIVER":"REQUEST_REVIEW",
				userId:claraInstance.user.id,
				notes:Ext.getCmp("taPharmacyNotes").getValue()
		};
		
		Ext.Ajax.request({
			url:url,
			success: function(response){
				status = jQuery(jQuery.parseXML(response.responseText)).find("result").text();
				Ext.getCmp("btnRequestPharmacy").setText("Request to Pharmacy sent.");
				Ext.getCmp("btnRequestPharmacy").setDisabled(true);
				Clara.NewSubmission.DDMessageBus.fireEvent('pharmacystatusupdated', status);
			},
			params:data,
			method:'POST'
		});
		
		return status;
	},
	
	checkPharmacyStatus:function(){
		var status=""; 
		url = appContext + "/ajax/protocols/" + claraInstance.id + "/protocol-forms/" + claraInstance.form.id +"/review/review-status";

		data = {	
				committee: "PHARMACY_REVIEW"
		};
		
		Ext.Ajax.request({
			url:url,
			success: function(response){
				var statusmxl = jQuery.parseXML(response.responseText);
				status = jQuery(statusmxl).find("result").text().toLowerCase();
				clog("checkPharmacyStatus: returned status",statusmxl,status);
				Clara.NewSubmission.DDMessageBus.fireEvent('pharmacystatusupdated', status);
			},
			params:data,
			method:'GET'
		});
	},
	
	initComponent: function() {
		var t = this;

		var config = {
				store:Clara.NewSubmission.ProtocolDrugStore,
				viewConfig: {
					forceFit:true
				},
				tbar: new Ext.Toolbar({
					items:[{
						id:'btnAddInvDrug',
				    	text: 'Add Investigational Drug..',
				    	iconCls:'icn-pill--plus',
				    	handler: function(){
							// TODO: open drug-wizard window here
							var dw = new Clara.NewSubmission.ProtocolDrugWindow({drugType:'investigational'});
							dw.show();
				    	}},{
				    		id:'btnAddConvDrug',
					    	text: 'Add Conventional Drug..',
					    	iconCls:'icn-pill-small',
					    	handler: function(){
								// TODO: open drug-wizard window here
								var dw = new Clara.NewSubmission.ProtocolDrugWindow({drugType:'conventional'});
								dw.show();
					    	}},'-',

				    	{
					    	text: 'Remove Drug',
					    	id:'btnRemoveDrug',
					    	iconCls:'icn-pill--minus',
					    	handler: function(){
				    			Clara.NewSubmission.ConfirmRemoveDrug(Ext.getCmp("protocol-drug-panel").selectedDrug);
					    }},'->',{
					    	xtype:'tbtext',
					    	text:'Pharmacy status: <span id="pharmacy-review-status" style="font-weight:800;">Checking...</span>'
					    },
					    {
					    	text: 'Request Pharmacy Review',
					    	id:'btnRequestPharmacy',
					    	iconCls:'icn-user-gray',
					    	hidden:true,
					    	handler: function(){
					    			t.confirmRequestPharmacyReviewWindow();
					    	}
					    }
					]
				}),
				sm: new Ext.grid.RowSelectionModel({singleSelect: true}),
		        loadMask: new Ext.LoadMask(Ext.getBody(), {msg:"Please wait..."}),
		        columns: [
		                  {
		                	  	header:'Drug',
		                	  	dataIndex:'name',
		                	  	sortable:true,
		                	  	renderer:function(value, p, record){
		                	  		var str = "<div class='gpRowLabel'>"+value+"</div><div class='gpRowDesc'>";
		                	  		//str+='<ul class="gpRowList">';
				                  	//for (var i=0; i<record.get("pharmacies").length; i++) {
				                  	//	str += "<li class='gpRowListItem'>"+record.get("pharmacies")[i].get("name")+"</li>";
				                  	//}
				                  	//str+='</ul></div>';
				                  	
				                    return str+"</div>";
				                },
		                	  	width:300
		                  },
                    {
                        header:'Status',
                        dataIndex:'status',
                        sortable:true,
                        width:130
                    },{
                        header:'Type',
                        dataIndex:'type',
                        sortable:true,
                        renderer:function(value, p, record){
                            return Ext.util.Format.capitalize(value);
                        },
                        width:60
                    },
		                  {
		                	  	header:'IND',
		                	  	dataIndex:'ind',
		                	  	sortable:false,
		                	  	width:60
		                  }
		        ],
			    listeners:{
				    rowdblclick: function(grid, rowI, event)   {
						clog("dblclick!!");
						var drugdata = grid.getStore().getAt(rowI).data;
						clog(drugdata);
						var drug = new Clara.NewSubmission.Drug({
							id:drugdata.id,
							identifier:drugdata.identifier,
							name:drugdata.name,
							type:drugdata.type,
							status:drugdata.status,
							administration:drugdata.administration,
							isprovided:(drugdata.isprovided == 'y')?true:false,
							brochure:(drugdata.brochure == 'y')?true:false,
							insert:(drugdata.insert == 'y')?true:false,
							approved:drugdata.approved,
							ind:drugdata.ind,
							nsc:drugdata.nsc,
							provider:drugdata.provider,
							providerdosage:drugdata.providerdosage,
							storage:drugdata.storage,
							prep:drugdata.prep,
							toxicities:drugdata.toxicities,
							treatmentcenterlocation:drugdata.treatmentcenterlocation
						});
							
						new Clara.NewSubmission.ProtocolDrugWindow({readOnly:!t.editable,editing:true, drug:drug}).show();
						
				    },
				    rowclick: function(grid, rowI, event)   {
						var drugdata = grid.getStore().getAt(rowI).data;
						var drug = new Clara.NewSubmission.Drug({
							id:drugdata.id,
							identifier:drugdata.identifier,
							name:drugdata.name,
							status:drugdata.status,
							administration:drugdata.administration,
							isprovided:(drugdata.isprovided == 'y')?true:false,
							brochure:(drugdata.brochure == 'y')?true:false,
							insert:(drugdata.insert == 'y')?true:false,
							approved:drugdata.approved,
							ind:drugdata.ind,
							nsc:drugdata.nsc,
							provider:drugdata.provider,
							providerdosage:drugdata.providerdosage,
							storage:drugdata.storage,
							prep:drugdata.prep,
							toxicities:drugdata.toxicities,
							treatmentcenterlocation:drugdata.treatmentcenterlocation
						});

						Ext.getCmp("protocol-drug-panel").selectedDrug = drug;
						Ext.getCmp("btnRemoveDrug").setDisabled(!t.editable);
				    }
			    }
		};
		
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		//this.addEvents('drugsloaded','drugadded', 'drugedited', 'drugremoved');
		
		Clara.NewSubmission.ProtocolDrugPanel.superclass.initComponent.apply(this, arguments);
		var pharmacyStatus=t.checkPharmacyStatus();
		clog("Pharmacy status: '"+pharmacyStatus+"'");
	}
	

});
Ext.reg('claraprotocoldrugpanel', Clara.NewSubmission.ProtocolDrugPanel);
