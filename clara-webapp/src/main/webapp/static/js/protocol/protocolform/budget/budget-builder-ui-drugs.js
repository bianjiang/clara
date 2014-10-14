Clara.BudgetBuilder.DispensingDrugPanel = Ext.extend(Ext.Panel, {
	id: 'dispensing-drug-panel',
	xtype: 'panel',
    title: 'Pharmacy Dispensing Fee',
    height: 340,
    layout: 'border',
	constructor:function(config){		
		Clara.BudgetBuilder.DispensingDrugPanel.superclass.constructor.call(this, config);
	},
	initComponent: function() {
		var parentWindow = this.ownerCt.ownerCt;
		var t = this;
		t.id = parentWindow.id + "_drugdispensing-procedure-panel";
		var config = {
				disabled:(parentWindow.editing == true)?true:false,
				items: [
				        {
						     xtype: 'grid',
						     viewConfig: {
						         forceFit: true,
						         rowOverCls:'',
						         emptyText: 'No pharmacy dispensing fees on this study.',
						         headersDisabled:true
						      },
						     disableSelection:true,
						     stripeRows:true,
						     loadMask:true,
						     region: 'center',
						     border: false,
						     store:parentWindow.pharmStore,
						     id: 'gpPharm',
						     sm: new Ext.grid.RowSelectionModel({singleSelect: true}),
						     
						     loadMask: new Ext.LoadMask(Ext.getBody(), {msg:"Please wait..."}),
						        columns: [
						                  {
						                	  	header:'Description',
						                	  	dataIndex:'description',
						                	  	sortable:true,
						                	  	renderer:function(value, p, record){
						                	  		var outHTML='<div class="wrap"><h1>'+value+'</h1><ul>';
								                  	for (var i=0; i<record.data.drugs.length; i++) {
								                  		outHTML = outHTML + "<li class='staff-row-role'>"+record.data.drugs[i].data.name+"</li>";
								                  	}
								                  	return outHTML+'</ul><div class="row-pharmacy-notes">'+record.data.notes+'</div></div>';
								                },
						                	  	width:200
						                  },
						                  {
						                	  	header:'Pharmacy cost',
						                	  	dataIndex:'cost',
						                	  	sortable:false,
						                	  	width:100
						                  },
						                  {
						                	  	header:'',
						                	  	dataIndex:'name',
						                	  	sortable:false,
						                	  	width:80,
						                	  	renderer:function(v,p,r,idx){
						                	  		var pname="";
						                	  		for (var i=0; i<r.data.drugs.length; i++) {
								                  		pname += r.data.drugs[i].data.name;
								                  		if (i != r.data.drugs.length-1) pname+=", ";
								                  	}
						                	   		return "<a class='proc-choose' href='javascript:Ext.getCmp(\""+parentWindow.id+"\").chooseDrug(\"dispensing\",\""+pname+"\","+idx+");'>Choose</a>";
						                  		}
						                  }
						        ]
						    }
					 ]
		};
		Ext.apply(this, Ext.apply(this.initialConfig, config));

		parentWindow.pharmStore.clearFilter();
		parentWindow.pharmStore.filter([{
			property     : 'type',
			value        : 'drug'
		}/*,{
			property     : 'waived',
			value        : 'false'
		}*/]);
		if (parentWindow.pharmStore.getCount() == 0){
			alert("No dispensing fees defined by the pharmacy.");
			parentWindow.close();
		}
		
		
		Clara.BudgetBuilder.DispensingDrugPanel.superclass.initComponent.apply(this, arguments);
	}
});
Ext.reg('claradrugdispensingprocedurepanel', Clara.BudgetBuilder.DispensingDrugPanel);


Clara.BudgetBuilder.DrugPanel = Ext.extend(Ext.Panel, {
	id: 'bb-drug-panel',
	xtype: 'panel',
    title: 'Drug',
    height: 340,
    layout: 'border',
	constructor:function(config){		
		Clara.BudgetBuilder.DrugPanel.superclass.constructor.call(this, config);
	},
	initComponent: function() {
		var parentWindow = this.ownerCt.ownerCt;
		var t = this;
		t.id = parentWindow.id + "_drug-procedure-panel";
		var config = {
				disabled:(parentWindow.editing == true)?true:false,
				
				items: [
				        {
						     xtype: 'grid',
						     viewConfig: {
						         forceFit: true,
						         rowOverCls:'',
						         emptyText: 'No drugs on this study.',
						         headersDisabled:true
						      },
						     disableSelection:true,
						     stripeRows:true,
						     loadMask:true,
						     region: 'center',
						     border: false,
						     store:parentWindow.drugStore,
						     id: 'gpDrugs',
						     sm: new Ext.grid.RowSelectionModel({singleSelect: true}),
						     loadMask: new Ext.LoadMask(Ext.getBody(), {msg:"Please wait..."}),
						        columns: [
						                  {
						                	  header:'Type',
						                	  	dataIndex:'type',
						                	  	sortable:true,
						                	  	renderer:function(value, p, record){
								                      return Ext.util.Format.capitalize(value);
								                },
						                	  	width:90
						                  }, {
						                	  	header:'Name',
						                	  	dataIndex:'id',
						                	  	sortable:true,
						                	  	renderer:function(value, p, record){
								                      return String.format('<b>{1}</b><br/>{2}',value, record.data.name, record.data.status);
								                },
						                	  	width:195
						                  },
						                  {
						                	  	header:'How administered?',
						                	  	dataIndex:'administration',
						                	  	sortable:false,
						                	  	width:115
						                  },
						                  {
						                	  	header:'',
						                	  	dataIndex:'name',
						                	  	sortable:false,
						                	  	width:70,
						                	  	renderer:function(v,p,r,idx){
						                	   		return "<a class='proc-choose' href='javascript:Ext.getCmp(\""+parentWindow.id+"\").chooseDrug(\""+r.data.type+"\",\""+r.data.name+"\","+idx+");'>Choose</a>";
						                  		}
						                  }
						        ]
						    }
					 ]
		};
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		Clara.BudgetBuilder.DrugPanel.superclass.initComponent.apply(this, arguments);
	}
});
Ext.reg('claradrugprocedurepanel', Clara.BudgetBuilder.DrugPanel);


Clara.BudgetBuilder.DrugStore = new Ext.data.XmlStore({
	autoLoad:true,
	baseParams:{listPath:'/protocol/drugs/drug'},
	proxy: new Ext.data.HttpProxy({
		headers:{'Accept': 'application/xml'},
		url:appContext + "/ajax/protocols/" + claraInstance.id + "/protocol-forms/" + claraInstance.form.id + "/protocol-form-xml-datas/" + claraInstance.xmlDataId + "/xml-elements/list",
		method:'GET'
	}),
	record: 'drug', 
	fields: [
	 		{name:'id', mapping:'@id'},
	 		{name:'identifier',mapping:'@identifier'},
	 		{name:'name', mapping:'@name'},
	 		{name:'type', mapping:'@type'},
	 		{name:'status',mapping:'@status'},
	 		{name:'administration',mapping:'@admin'},
	 		{name:'isprovided',mapping:'@isprovided'},
	 		{name:'brochure',mapping:'@brochure'},
	 		{name:'insert',mapping:'@insert'},
	 		{name:'approved',mapping:'@approved'},
	 		{name:'ind',mapping:'@ind'},
	 		{name:'nsc',mapping:'@nsc'},
	 		{name:'provider',mapping:'@provider'},
	 		{name:'providerdosage',mapping:'@providerdosage'},
	 		{name:'storage',mapping:'storage'},
	 		{name:'prep',mapping:'prep'},
	 		{name:'toxicities',mapping:'toxicities'}
	 	]
});


Clara.BudgetBuilder.PharmacyFeeStore = new Ext.data.XmlStore({
	autoLoad:true,
	proxy: new Ext.data.HttpProxy({
		headers:{'Accept': 'application/xml'},
		url: appContext+"/ajax/protocols/"+claraInstance.id+"/protocol-forms/"+claraInstance.form.id+"/pharmacy/get",
		method:'GET'
	}),
	record: 'expense', 
	fields: [
	 		{name:'id', mapping:'@id'},
	 		{name:'cost',mapping:'@cost', convert: function(v,node){
	 			if (node.attributes["waived"].value == "true"){
	 				return 0;
	 			} else {
	 				return v;
	 			}
	 		}},
	 		{name:'type', mapping:'@type'},
	 		{name:'waived', mapping:'@waived'},
	 		{name:'description',mapping:'@description'},
	 		{name:'notes',mapping:'@notes'},
	 		{name:'drugs',mapping:'drugs', convert:function(v,node){ 
	 			var drugPharmacyReader = new Ext.data.XmlReader({
	 				record: 'drug',
	 				fields: [{name:'id', mapping:'@id'},{name:'name', mapping:'@name'}]
	 			});
	 			return drugPharmacyReader.readRecords(node).records;
	 		}}
	 	]
});