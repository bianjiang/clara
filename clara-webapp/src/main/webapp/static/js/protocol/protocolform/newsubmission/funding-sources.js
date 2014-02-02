Ext.ns('Clara.NewSubmission');
Clara.NewSubmission.MessageBus = new Ext.util.Observable();

Clara.NewSubmission.MessageBus.addEvents('sourceadded','sourcesloaded','sourceremoved','sourceupdated');

Clara.NewSubmission.Contact = function(o){
	this.id=				(o.id || '0');	
	this.name=				(o.name || '');
	this.address=			(o.address || '');
	this.title=				(o.title || '');
	this.phone=				(o.phone || '');
	this.cell=				(o.cell || '');
	this.fax=				(o.fax || '');
	this.email=				(o.email || '');
	this.role=				(o.role || '');
	
	this.toXML= function(){
		return "<contact id='"+this.id+"' name='"+Encoder.htmlEncode(this.name)+"' title='"+Encoder.htmlEncode(this.title)+"' role='"+Encoder.htmlEncode(this.role)+"' phone='"+Encoder.htmlEncode(this.phone)+"' cell='"+Encoder.htmlEncode(this.cell)+"' fax='"+Encoder.htmlEncode(this.fax)+"' email='"+Encoder.htmlEncode(this.email)+"' address='"+Encoder.htmlEncode(this.address)+"' />";
	};
};

Clara.NewSubmission.FundingSource = function(o){
	this.id=				(o.id || '');	
	this.entityid=			(o.entityid || '');
	this.entityname=		(o.entityname || '');
	this.entitytype=		(o.entitytype || '');
	this.notes=				(o.notes || '');
	this.type=				(o.type || '');
	this.subtype=			(o.subtype || '');
	this.name=				(o.name || '');
	this.department=		(o.department || '');	
	this.amount=			(o.amount || '');	
	this.contacts=			(o.contacts || []);
	this.projectid=			(o.projectid || 0);
	this.projectpi=			(o.projectpi || '');
	
	this.contactsXML= function(){
		var p = "";
		for (var i=0;i<this.contacts.length;i++){
			p += this.contacts[i].toXML();
		}
		return "<contacts>"+p+"</contacts>";
	};
	
	this.toXML= function(){
		return "<funding-source id='"+this.id+"' name='"+Encoder.htmlEncode(this.name)+"' entityid='"+Encoder.htmlEncode(this.entityid)+"' entityname='"+Encoder.htmlEncode(this.entityname)+"' projectid='"+Encoder.htmlEncode(this.projectid)+"' projectpi='"+Encoder.htmlEncode(this.projectpi)+"' entitytype='"+Encoder.htmlEncode(this.entitytype)+"' "+
		"type='"+Encoder.htmlEncode(this.type)+"' subtype='"+Encoder.htmlEncode(this.subtype)+"' department='"+Encoder.htmlEncode(this.department)+"' amount='"+Encoder.htmlEncode(this.amount)+"'>"+ 
		"<notes>"+Encoder.cdataWrap(this.notes)+"</notes>"+
			   this.contactsXML()+"</funding-source>";
	};
	
	this.fromData= function(fundingsourcedata){
			this.id=fundingsourcedata.id;
			this.entityid=fundingsourcedata.entityid;
			this.entityname=fundingsourcedata.entityname;
			this.entitytype=fundingsourcedata.entitytype;
			this.projectpi=fundingsourcedata.projectpi;
			this.projectid=fundingsourcedata.projectid;
			this.type=fundingsourcedata.type;
			this.subtype=fundingsourcedata.subtype;
			this.notes=fundingsourcedata.notes;
			this.name=fundingsourcedata.name;
			this.department=fundingsourcedata.department;
			this.amount=fundingsourcedata.amount;
			this.contacts=[];

		for (var i=0; i<fundingsourcedata.contacts.length;i++){
			this.contacts.push(new Clara.NewSubmission.Contact({
				id:fundingsourcedata.contacts[i].data.id,
				address:fundingsourcedata.contacts[i].data.address,
				name:fundingsourcedata.contacts[i].data.name,
				title:fundingsourcedata.contacts[i].data.title,
				phone:fundingsourcedata.contacts[i].data.phone,
				cell:fundingsourcedata.contacts[i].data.cell,
				fax:fundingsourcedata.contacts[i].data.fax,
				email:fundingsourcedata.contacts[i].data.email,
				role:fundingsourcedata.contacts[i].data.role
			}));
		}
		return this;
	};
};

Clara.NewSubmission.FundingAgencyStore = new Ext.data.Store({
	autoLoad:true,
	header :{
    	'Accept': 'application/json'
	},
	proxy: new Ext.data.HttpProxy({
		url: appContext + "/ajax/protocols/protocol-forms/new-submission/sponsors/list",
		method:"GET"
	}),
	reader: new Ext.data.JsonReader({
		root: 'sponsors',
		idProperty: 'id'
	}, [
		{name:'id'},
		{name:'name',mapping:'value'}
	])
});

Clara.NewSubmission.FundingResearchOrganizationStore = new Ext.data.Store({
	header :{
    	'Accept': 'application/json'
	},
	proxy: new Ext.data.HttpProxy({
		url: appContext + "/ajax/protocols/protocol-forms/new-submission/research-orgnizations/list",
		method:"GET"
	}),
	reader: new Ext.data.JsonReader({
		root: 'research-organization',
		idProperty: 'id'
	}, [
		{name:'id'},
		{name:'name',mapping:'value'}
	])
});

Clara.NewSubmission.EditFundingSource = function(fs){
	if (fs.type == 'External'){
		new Clara.NewSubmission.ExternalFundingWindow({editing:true, fundingsource:fs}).show();
	}else if (fs.type == 'Internal'){
		new Clara.NewSubmission.InternalFundingWindow({editing:true, fundingsource:fs}).show();
	}
};

Clara.NewSubmission.ConfirmRemoveFundingSource = function(fs){
	Ext.Msg.show({
		title:"WARNING: About to delete a funding source",
		msg:"Are you sure you want to delete this funding source (and its related contacts)?", 
		buttons:Ext.Msg.YESNOCANCEL,
		icon:Ext.MessageBox.WARNING,
		fn: function(btn){
			if (btn == 'yes'){
				if (fs){
					url = appContext + "/ajax/protocols/" + claraInstance.id + "/protocol-forms/" + claraInstance.form.id + "/protocol-form-xml-datas/" + claraInstance.form.xmlDataId + "/xml-elements/delete";

					data = {	
							listPath: "/protocol/funding/funding-source",
							elementId: fs.id
					};
					
					jQuery.ajax({
						async: false,
						url: url,
						type: "POST",
						dataType: 'xml',
						data: data
					});
					Clara.NewSubmission.MessageBus.fireEvent('sourceremoved', fs);  
					Ext.getCmp("protocol-fundingsource-panel").selectedFundingSource = {};
				}
			}
		}
		
	});
	return false;
};


Clara.NewSubmission.FundingSourcePanel = Ext.extend(Ext.grid.GridPanel, {
	id: 'protocol-fundingsource-panel',
	frame:false,
	trackMouseOver:false,
	selectedFundingSource:{},
	fundingStudyType:'',
	readOnly:false,
	formBaseTag:'',

	  
	 constructor:function(config){	
		
		Clara.NewSubmission.FundingSourcePanel.superclass.constructor.call(this, config);

	},	
	
	hasFunding:function(fundingAmount){
		return (this.getStore().find("amount",fundingAmount) > -1);
	},
	
	hasFundingType:function(fundingType){
		return (this.getStore().find("type",fundingType) > -1);
	},
	
	loadFundingSources:function(formBaseTag){
		var t = this;
		formBaseTag = (t.formBaseTag != '')?t.formBaseTag:"protocol";
		this.getStore().removeAll();
		this.getStore().load({params:{listPath:'/'+formBaseTag+'/funding/funding-source'}});
	},

	initComponent: function() {
		var t = this;

		var config = {
				border:false,				
				store:new Ext.data.GroupingStore({
					autoLoad:false,
					groupField: 'type',
					proxy: new Ext.data.HttpProxy({
						url: appContext + "/ajax/"+claraInstance.type+"s/" + claraInstance.id + "/"+claraInstance.type+"-forms/" + claraInstance.form.id + "/"+claraInstance.type+"-form-xml-datas/" + claraInstance.form.xmlDataId + "/xml-elements/list",
						method:"GET",
						headers:{'Accept':'application/xml;charset=UTF-8'}
					}),
					reader: new Ext.data.XmlReader({
					record: 'funding-source', 
					fields: [
						{name:'id', mapping:'@id'},
						{name:'entityid',mapping:'@entityid'},
						{name:'name', mapping:'@name'},
						{name:'entityname',mapping:'@entityname'},
						{name:'entitytype',mapping:'@entitytype'},
						{name:'projectid',mapping:'@projectid'},
						{name:'projectpi',mapping:'@projectpi'},
						{name:'title',mapping:'@title'},
						{name:'type',mapping:'@type'},
						{name:'subtype',mapping:'@subtype'},
						{name:'notes',mapping:'notes'},
						{name:'department',mapping:'@department'},
						{name:'amount',mapping:'@amount'},
						{name:'contacts',mapping:'contacts', convert:function(v,node){ 
							var fundingsourceContactReader = new Ext.data.XmlReader({
								record: 'contact',
								fields: [{name:'id', mapping:'@id'},{name:'name', mapping:'@name'},{name:'address', mapping:'@address'},{name:'title', mapping:'@title'},{name:'phone', mapping:'@phone'},{name:'cell', mapping:'@cell'},{name:'fax', mapping:'@fax'},{name:'email', mapping:'@email'},{name:'role', mapping:'@role'}]
							});
							return fundingsourceContactReader.readRecords(node).records;
						}}
					]})
				}),
				view: new Ext.grid.GroupingView({
		    		forceFit:true,
		    		showGroupName:false,
		    		enableGroupingMenu:false,
		    		startCollapsed : false,
		    		groupTextTpl:'<div class="fundinggroup fundinggroup-{text}">{text} <span class="fundinggroupcount fundinggroupcount-{text}">({[values.rs.length]} {[values.rs.length > 1 ? "sources" : "source"]})</span></div>'
		    	}),			
				tbar: (t.readOnly)?null:new Ext.Toolbar({
					items:[{
				    	text: 'Add Funding Source',
				    	iconCls:'icn-money--plus',
				    	menu:[{
							text:'External Funding Agency / Vendor..',
							iconCls:'icn-money',
							handler:function(){
				    			if (Ext.getCmp("protocol-fundingsource-panel").hasFundingType('None') == false) new Clara.NewSubmission.ExternalFundingWindow().show();
								else alert("You cannot add funding sources if you have a 'No anticipated funding' row. Remove the row and try again.");
							}
						},{
							iconCls:'icn-money',
							text:'Internal Funding...',
							handler:function(){
								if (Ext.getCmp("protocol-fundingsource-panel").hasFundingType('None') == false) new Clara.NewSubmission.InternalFundingWindow().show();
								else alert("You cannot add funding sources if you have a 'No anticipated funding' row. Remove the row and try again.");
							}
						},{
							iconCls:'icn-money',
							text:'Proposal previously submitted (through ARIA)..',
							handler:function(){
								if (Ext.getCmp("protocol-fundingsource-panel").hasFundingType('None') == false) new Clara.NewSubmission.ProjectFundingWindow().show();
								else alert("You cannot add funding sources if you have a 'No anticipated funding' row. Remove the row and try again.");
							}
						}]},{
							id:'btn-no-funding',
							iconCls:'icn-exclamation',
							text:'No anticipated funding...',
							handler:function(){
								var s = Ext.getCmp("protocol-fundingsource-panel").getStore();
								if (s.getCount() > 0){
									alert("You cannot add 'No Anticipated Funding' because you already have entered funding sources. Remove all funding sources before using 'No anticipated funding'.");
								}else {
									new Clara.NewSubmission.NoFundingWindow().show();
								}
							}
						},{
					    	text: 'Add CRO/SMO',
					    	iconCls:'icn-building--plus',
					    	menu:[{
								text:'Clinical Research Organization (CRO)..',
								iconCls:'icn-building',
								handler:function(){
					    			new Clara.NewSubmission.CROSMOWindow({type:'CRO'}).show();
								}
							},{
								iconCls:'icn-building',
								text:'Site Management Organization (SMO)...',
								handler:function(){
									new Clara.NewSubmission.CROSMOWindow({type:'SMO'}).show();
								}
							}]},'->',
				    	{
					    	text: 'Remove...',
					    	iconCls:'icn-minus-button',
					    	handler: function(){
				    			Clara.NewSubmission.ConfirmRemoveFundingSource(Ext.getCmp("protocol-fundingsource-panel").selectedFundingSource);
					    }}
					]
				}),
				sm: new Ext.grid.RowSelectionModel({singleSelect: true}),
		        loadMask: new Ext.LoadMask(Ext.getBody(), {msg:"Please wait..."}),
		        columns: [
		                  {
		                	  	header:'Funding Type',
		                	  	dataIndex:'type',
		                	  	hidden:true
		                  },
		                  {
		                	  	header:'Amount',
		                	  	dataIndex:'amount',
		                	  	sortable:true,
		                	  	width:30
		                  },
		                  {
		                	  	header:'Funding Details',
		                	  	dataIndex:'name',
		                	  	sortable:true,
		                	  	width:150,
		                	  	renderer:function(value, p, r){
		                	  		var d = r.data;
		                	  		var str = "<div class='fundingsource-row wrap'>";
		                	  		
		                	  		if (d.type == 'External'){
			                	  		str += "<h1>"+d.entityname+"</h1>";
			                	  		if (d.name && d.name != ''){
			                	  			str += "<h1>Project Title: "+d.name+"</h1>";
			                	  		}
		                	  		}else if (d.type == 'Internal'){
			                	  		str += "<h1>"+d.name+"</h1>";
			                	  		if (d.entityname && d.entityname != ''){
			                	  			str += "<h1>Cost center: "+d.entityname+"</h1>";
			                	  		}
		                	  		}else if (d.type == 'None'){
			                	  		if (d.subtype == "student") str += "<h1 style='color:red;text-decoration:underline;'>Student research</h1>";
			                	  		if (d.entityname && d.entityname != ''){
			                	  			str += "<h1>Department: "+d.department+"</h1>";
			                	  			str += "<h1>Fund: "+d.name+"</h1>";
			                	  			str += "<h1>Cost center: "+d.entityname+"</h1>";
			                	  		} else {
			                	  			str += "<h1>"+d.name+"</h1>";
			                	  		}
		                	  		}else if (d.type == 'Project'){
			                	  		str += "<h1>Title: "+d.name+"</h1><h1>Funding Agency: "+d.entityname+"</h1>";
			                	  		if (d.projectid && d.projectid != ''){
			                	  			str += "<h1>PRN: "+d.projectid+"</h1>";
			                	  			str += "<h1>PI: "+d.projectpi+"</h1>";
			                	  		}
		                	  		}else if (d.type == 'CRO' || d.type == 'SMO'){
			                	  		str += "<h1>"+d.entityname+"</h1>";
		                	  		}
		                	  		
		                	  		return str + "</div>";
		                  		}
		                  },
		                  {
		                  	  	header:'Contacts',
		                  	  	dataIndex:'contacts',
		                  	  	sortable:false,
		                  	  	renderer:function(value, p, record){
				                  	var outHTML='<ul style="margin-bottom:8px;">';
				                  	for (var i=0; i<record.data.contacts.length; i++) {
				                  		outHTML += "<li class='contact-row'>";
				                  		outHTML += (record.data.contacts[i].data.name != "")?('<a href="javascript:;" onclick="new Clara.NewSubmission.ContactWindow({editing:true, contactindex:'+i+', fundingsourcedataid:\''+record.id+'\'}).show();"><span class="contact-row-name">'+record.data.contacts[i].data.name+'</span></a>'):'';
				                  		outHTML += (record.data.contacts[i].data.title != "")?(", <span class='contact-row-title'>"+record.data.contacts[i].data.title+"</span> "):"";
				                  		outHTML += (record.data.contacts[i].data.role != "")?("<span class='contact-row-role'>("+record.data.contacts[i].data.role+")</span> "):"";
				                  		outHTML += "</li>";
				                  	}
				                  	outHTML+='</ul>';
				                  	if (t.readOnly == false) outHTML+='<a href="javascript:;" onclick="new Clara.NewSubmission.ContactWindow({fundingsourcedataid:\''+record.id+'\'}).show();">Add contact...</a>';
				                  	return outHTML;
				                }
		                  }
		        ],
		        

		        
		        
			    listeners:{
			
				    rowdblclick: function(grid, rowI, event)   {
						clog("dblclick!!");
						var fundingsourcedata = grid.getStore().getAt(rowI).data;
						var fundingsource = new Clara.NewSubmission.FundingSource({
							id:fundingsourcedata.id,
							entityid:fundingsourcedata.entityid,
							entityname:fundingsourcedata.entityname,
							entitytype:fundingsourcedata.entitytype,
							type:fundingsourcedata.type,
							name:fundingsourcedata.name,
							department:fundingsourcedata.department,
							amount:fundingsourcedata.amount,
							contacts:[]
						});
						
						for (var i=0; i<fundingsourcedata.contacts.length;i++){
							fundingsource.contacts.push(new Clara.NewSubmission.Contact({
								id:fundingsourcedata.contacts[i].data.id,
								address:fundingsourcedata.contacts[i].data.address,
								name:fundingsourcedata.contacts[i].data.name,
								title:fundingsourcedata.contacts[i].data.title,
								phone:fundingsourcedata.contacts[i].data.phone,
								cell:fundingsourcedata.contacts[i].data.cell,
								fax:fundingsourcedata.contacts[i].data.fax,
								email:fundingsourcedata.contacts[i].data.email,
								role:fundingsourcedata.contacts[i].data.role
								
							}));
						}
						clog(fundingsource);
						if (t.readOnly == false) Clara.NewSubmission.EditFundingSource(fundingsource);
						
				    }, 

				    rowclick: function(grid, rowI, event)   {
						var fundingsourcedata = grid.getStore().getAt(rowI).data;
						var fs = new Clara.NewSubmission.FundingSource({});
						fs.fromData(fundingsourcedata);
						clog(fs);
						Ext.getCmp("protocol-fundingsource-panel").selectedFundingSource = fs;
						
				    }
			    }
		};
		
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		
		Clara.NewSubmission.FundingSourcePanel.superclass.initComponent.apply(this, arguments);
		
		//var t =Ext.getCmp("protocol-fundingsource-panel");
		Clara.NewSubmission.MessageBus.on('sourceupdated', function(event){t.loadFundingSources(t.formBaseTag);}, t);
		Clara.NewSubmission.MessageBus.on('sourceremoved',  function(event){t.loadFundingSources(t.formBaseTag);}, t);
		Clara.NewSubmission.MessageBus.on('sourceadded', function(event){t.loadFundingSources(t.formBaseTag);}, t);
		
		this.loadFundingSources(this.formBaseTag);
	}
	

});
Ext.reg('claraprotocolfundingsourcepanel', Clara.NewSubmission.FundingSourcePanel);


Clara.NewSubmission.ExternalFundingWindow = Ext.extend(Ext.Window, {
	id: 'clara-funding-window',
    width: 542,
    height: 170,
    layout: 'absolute',
    title:'External Funding Source',
	fundingsource:{},
	editing:false,
	modal:true,
	isValid: function(){
		return (Ext.getCmp("fldAmount").validate() && Ext.getCmp("fldAgency").validate());
	},
	saveOrUpdateSource: function(closeWindowAfter){
		var t = this;
		var fundingAmount = (jQuery("input:radio[name=fldAmount]:checked").val() == 'y')?'Full':'Partial';
		if (Ext.getCmp("protocol-fundingsource-panel").hasFunding('Full') === true  && !(t.editing === true && fundingAmount == 'Partial')){
			alert("This study already has full funding, you cannot add another.");
		} else if (Ext.getCmp("protocol-fundingsource-panel").hasFunding('Partial') === true && fundingAmount == 'Full'){
			alert("This study already has partial funding, you cannot add full funding.");
		} else {
			if (Ext.getCmp('clara-funding-window').isValid()){
				var agency = Ext.getCmp("fldAgency");
				var fundingsourceToSave = new Clara.NewSubmission.FundingSource({
					entityid:''+String(agency.getValue()),
					entityname:jQuery("#fldAgency").val(),
					entitytype:'Agency',
					type:'External',
					name:Ext.getCmp("fldProjectTitle").getValue(),
					department:'',
					amount:fundingAmount,
					contacts:(Ext.getCmp('clara-funding-window').fundingsource.contacts || [])
				});
				clog(fundingsourceToSave.toXML());
				if (Ext.getCmp('clara-funding-window').editing){
					fundingsourceToSave.id = t.fundingsource.id;
					var fsID = updateExistingXmlInProtocol("/protocol/funding/funding-source", fundingsourceToSave.id, fundingsourceToSave.toXML());
					Clara.NewSubmission.MessageBus.fireEvent('sourceupdated', fundingsourceToSave);  
				} else {
					var fsID = addXmlToProtocol( "/protocol/funding/funding-source", fundingsourceToSave.toXML(), "funding-source");
					Clara.NewSubmission.MessageBus.fireEvent('sourceadded', fundingsourceToSave);  
				}
				
				if (closeWindowAfter){
					Ext.getCmp('clara-funding-window').close();
				}else{
					Ext.getCmp("fldAgency").clearValue();
					Ext.getCmp("fldProjectTitle").reset();
				}
			} else {
				alert("Please choose an amount and agency.");
			}	
		}
	},
	constructor:function(config){		
		Clara.NewSubmission.ExternalFundingWindow.superclass.constructor.call(this, config);
	},
	
	initComponent: function() {
			clog("initing window.. fundingsource?",this.fundingsource);	
		var config = {
				items: [{
	                xtype: 'combo',
	                x: 100,
	                y: 10,
	                width: 420,
	                hiddenName: 'fldAgency',
	                store:Clara.NewSubmission.FundingAgencyStore,
	                id: 'fldAgency',
	                displayField:'name',
	                valueField:'id',
	    	   	   	typeAhead:true,
			       	forceSelection:true,
			       	mode:'local',
			       	tabIndex:1
	            },
	            {
	                xtype: 'label',
	                text: 'Agency / Vendor',
	                x: 10,
	                y: 10
	            },
	            {
	                xtype: 'label',
	                text: 'Project title',
	                x: 10,
	                y: 40
	            },
	            
	            {
	                xtype: 'radiogroup',
	                x: 100,
	                y: 70,
	                width:400,
	                allowBlank:false,
				    fieldLabel: 'Funding Amount',
				    id:'fldAmount',
				    columns:'auto',
				    items:[
				           {boxLabel:'Full',inputValue:'y',name: 'fldAmount',width:100,
						       	tabIndex:3},
				           {boxLabel:'Partial',inputValue:'n',name: 'fldAmount',width:130,
							       	tabIndex:4}
				           ]
				},
	            {
	                xtype: 'textfield',
	                x: 100,
	                y: 40,
	                width: 420,
	                name: 'fldProjectTitle',
	                id: 'fldProjectTitle',
			       	tabIndex:2
	            }],
				buttons: [
							{
								text:'Cancel',
								disabled:false,
								handler: function(){
									Ext.getCmp('clara-funding-window').close();
								}
							},
							{
								text:'Save and Add Another..',
								id:'btn-save-addanother',
								handler: function(){
									Ext.getCmp('clara-funding-window').saveOrUpdateSource(false);
								}
							},{
								text:'Save and Close',
								id:'btn-save',
								handler: function(){
									Ext.getCmp('clara-funding-window').saveOrUpdateSource(true);
								}
							}],		
			    listeners:{
					afterrender:function(){
						if (this.editing){
							clog("RENDER DONE. FILLING.");
							Ext.getCmp('fldProjectTitle').setValue(this.fundingsource.name);
							Ext.getCmp('fldAgency').setValue(this.fundingsource.entityname);
							Ext.getCmp('fldAmount').setValue((this.fundingsource.amount == 'Full')?"y":"n");
							
						}
					}
				}
		};
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		Clara.NewSubmission.ExternalFundingWindow.superclass.initComponent.apply(this, arguments);
	}
});
Ext.reg('claraprotocolfundingwindowexternal', Clara.NewSubmission.ExternalFundingWindow);







Clara.NewSubmission.ProjectFundingWindow = Ext.extend(Ext.Window, {
	id: 'clara-funding-window',
    width: 542,
    height: 210,
    layout: 'absolute',
    title:'Proposal previously submitted (through ARIA)',
	fundingsource:{},
	editing:false,
	modal:true,
	isValid: function(){
		var t = this;
		
		var fundingAmount = (jQuery("input:radio[name=fldAmount]:checked").val() == 'y')?'Full':'Partial';
		if (Ext.getCmp("fldPRN").validate() ){
			//do ajax and check project info here
			
			url = appContext + "/ajax/protocols/protocol-forms/grant/" + Ext.getCmp("fldPRN").getValue();
			
			jQuery.ajax({
				async: false,
				url: url,
				type: "get",
				dataType: 'xml',
				error: function(data){
					clog("PRN AJAX ERROR:",data);
					
					var r = jQuery.parseJSON(data.responseText);
					if (r.error == false){
						if (r.data && r.data.id && r.data.id > 0){
							t.fundingsource = new Clara.NewSubmission.FundingSource({
								entityid:''+String(r.data.id),
								entityname:r.data.fundingAgency,
								entitytype:'Agency',
								type:'Project',
								projectid:jQuery.trim(r.data.prn),
								projectpi:jQuery.trim(r.data.piName),
								name:jQuery.trim(r.data.grantTitle),
								department:'',
								amount:fundingAmount,
								contacts:([])
							});
						}
					} else {
						alert(r.message);
						return false;
					}
			    }
			});
			
			return true;
		} else {
			return false;
		}

	},
	saveOrUpdateSource: function(closeWindowAfter){
		var t = this;
		var fundingAmount = (jQuery("input:radio[name=fldAmount]:checked").val() == 'y')?'Full':'Partial';
		if (Ext.getCmp("protocol-fundingsource-panel").hasFunding('Full') === true && !(t.editing === true && fundingAmount == 'Partial')){
			alert("This study already has full funding, you cannot add another.");
		} else if (Ext.getCmp("protocol-fundingsource-panel").hasFunding('Partial') === true && fundingAmount == 'Full'){
			alert("This study already has partial funding, you cannot add full funding.");
		} else {
			if (Ext.getCmp('clara-funding-window').isValid() && t.fundingsource && typeof(t.fundingsource.type) != "undefined"){
				// do stuff
				clog("SUCCESS. Will save:",t.fundingsource);
				var fsID = addXmlToProtocol( "/protocol/funding/funding-source", t.fundingsource.toXML(), "funding-source");
				Clara.NewSubmission.MessageBus.fireEvent('sourceadded', t.fundingsource);  
				Ext.getCmp('clara-funding-window').close();
			}	
		}
	},
	constructor:function(config){		
		Clara.NewSubmission.ProjectFundingWindow.superclass.constructor.call(this, config);
	},
	
	initComponent: function() {
				
		var config = {
				items: [{
	                xtype: 'label',
	                text: 'PRN',
	                x: 10,
	                y: 10,
	                width: 80
	            },
	            
	            {
	                xtype: 'radiogroup',
	                x: 100,
	                y: 120,
	                width:400,
	                allowBlank:false,
				    fieldLabel: 'Funding Amount',
				    id:'fldAmount',
				    columns:'auto',
				    items:[
				           {boxLabel:'Full',inputValue:'y',name: 'fldAmount',width:100,
						       	tabIndex:2},
				           {boxLabel:'Partial',inputValue:'n',name: 'fldAmount',width:130,
							       	tabIndex:3}
				           ]
				},
	            {
	                xtype: 'numberfield',
	                x: 100,
	                y: 10,
	                width: 420,
	                maxLength:20,
	                minLength:1,
	                allowBlank:false,
	                name: 'fldPRN',
	                id: 'fldPRN',
			       	tabIndex:1
	            },{
	            	xtype:'displayfield',
	            	x:100,
	            	y:30,
	            	width:420,
	            	value:'<strong>Was this project submitted through ARIA as a grant application?</strong> If so, list the 5 digit PRN/ARIA # assigned to it.'
	            }],
				buttons: [
							{
								text:'Cancel',
								disabled:false,
								handler: function(){
									Ext.getCmp('clara-funding-window').close();
								}
							},
							{
								text:'Save and Add Another..',
								id:'btn-save-addanother',
								hidden:true,
								handler: function(){
									Ext.getCmp('clara-funding-window').saveOrUpdateSource(false);
								}
							},{
								text:'Save and Close',
								id:'btn-save',
								handler: function(){
									Ext.getCmp('clara-funding-window').saveOrUpdateSource(true);
								}
							}]
		};
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		Clara.NewSubmission.ProjectFundingWindow.superclass.initComponent.apply(this, arguments);
	}
});
Ext.reg('claraprotocolfundingwindowproject', Clara.NewSubmission.ProjectFundingWindow);






Clara.NewSubmission.InternalFundingWindow = Ext.extend(Ext.Window, {
	id: 'clara-funding-window',
    width: 542,
    height: 240,
    layout: 'absolute',
    title:'Internal Funding Source',
	fundingsource:{},
	editing:false,
	modal:true,
	isValid: function(){
		return (Ext.getCmp("fldAmount").validate() && Ext.getCmp("fldCostCenter").validate() && Ext.getCmp("fldFund").validate());
	},
	saveOrUpdateSource: function(closeWindowAfter){
		var t = this;
		var fundingAmount = (jQuery("input:radio[name=fldAmount]:checked").val() == 'y')?'Full':'Partial';
		if (Ext.getCmp("protocol-fundingsource-panel").hasFunding('Full') === true && !(t.editing === true && fundingAmount == 'Partial')){
			alert("This study already has full funding, you cannot add another.");
		} else if (Ext.getCmp("protocol-fundingsource-panel").hasFunding('Partial') === true && fundingAmount == 'Full'){
			alert("This study already has partial funding, you cannot add full funding.");
		} else {
			if (Ext.getCmp('clara-funding-window').isValid()){
				var fundingsource = new Clara.NewSubmission.FundingSource({
					entityid:'',
					entityname:Ext.getCmp("fldCostCenter").getValue(),
					entitytype:'Cost Center',
					type:'Internal',
					name:Ext.getCmp("fldFund").getValue(),
					department:'',
					amount:fundingAmount,
					contacts:(Ext.getCmp('clara-funding-window').fundingsource.contacts || [])
				});
				clog(fundingsource.toXML());
				if (Ext.getCmp('clara-funding-window').editing){
					fundingsource.id = t.fundingsource.id;
					var fsID = updateExistingXmlInProtocol("/protocol/funding/funding-source", fundingsource.id, fundingsource.toXML());
					Clara.NewSubmission.MessageBus.fireEvent('sourceupdated', fundingsource);  
				} else {
					var fsID = addXmlToProtocol( "/protocol/funding/funding-source", fundingsource.toXML(), "funding-source");
					Clara.NewSubmission.MessageBus.fireEvent('sourceadded', fundingsource);  
				}
				
				if (closeWindowAfter){
					Ext.getCmp('clara-funding-window').close();
				}else{
					Ext.getCmp("fldCostCenter").reset();
					Ext.getCmp("fldFund").reset();
				}
			} else {
				alert("Please check the fund and cost center (fund is a 3-digit number; the cost center is a 7 digit number).");
			}	
		}
	},
	constructor:function(config){		
		Clara.NewSubmission.InternalFundingWindow.superclass.constructor.call(this, config);
	},
	
	initComponent: function() {
				
		var config = {
				items: [{
	                xtype: 'label',
	                text: 'Fund (must be 3 digits)',
	                x: 10,
	                y: 10,
	                width: 160
	            },
	            {
	                xtype: 'label',
	                text: 'Cost Center (must be 7 digits)',
	                x: 10,
	                y: 40,
	                width: 160
	            },
	           
	            {
	                xtype: 'radiogroup',
	                x: 100,
	                y: 120,
	                width:400,
	                allowBlank:false,
				    fieldLabel: 'Funding Amount',
				    id:'fldAmount',
				    columns:'auto',
				    items:[
				           {boxLabel:'Full',inputValue:'y',name: 'fldAmount',width:100,
						       	tabIndex:3},
				           {boxLabel:'Partial',inputValue:'n',name: 'fldAmount',width:130,
							       	tabIndex:4}
				           ]
				},
	            
				{
	                xtype: 'numberfield',
	                maxLength:10,
	                minLength:7,
	                allowDecimals:false,
	                allowNegative:false,
	                autoStripChars:true,
	                x: 180,
	                y: 40,
	                width: 340,
	                allowBlank:false,
	                name: 'fldCostCenter',
	                id: 'fldCostCenter',
			       	tabIndex:2
	            },
	            {
	                xtype: 'numberfield',
	                x: 180,
	                y: 10,
	                width: 340,
	                allowDecimals:false,
	                allowNegative:false,
	                autoStripChars:true,

	                maxLength:3,
	                minLength:3,
	                allowBlank:false,
	                name: 'fldFund',
	                id: 'fldFund',
			       	tabIndex:1
	            },
	            {
	                xtype: 'label',
	                text: 'For departmental funding, provide your home department cost center for any applicable charges/fees.',
	                x: 100,
	                y: 70,
	                width: 410,
	                style: 'font-size:13px;font-weight:800;'
	            }],
				buttons: [
							{
								text:'Cancel',
								disabled:false,
								handler: function(){
									Ext.getCmp('clara-funding-window').close();
								}
							},
							{
								text:'Save and Add Another..',
								id:'btn-save-addanother',
								handler: function(){
									Ext.getCmp('clara-funding-window').saveOrUpdateSource(false);
								}
							},{
								text:'Save and Close',
								id:'btn-save',
								handler: function(){
									Ext.getCmp('clara-funding-window').saveOrUpdateSource(true);
								}
							}],		
			    listeners:{
					afterrender:function(){
						if (this.editing){
							clog("RENDER DONE. FILLING.");
							Ext.getCmp('fldFund').setValue(this.fundingsource.name);
							Ext.getCmp('fldCostCenter').setValue(this.fundingsource.entityname);
							Ext.getCmp('fldAmount').setValue((this.fundingsource.amount == 'Full')?"y":"n");
							
						}
					}
				}
		};
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		Clara.NewSubmission.InternalFundingWindow.superclass.initComponent.apply(this, arguments);
	}
});
Ext.reg('claraprotocolfundingwindowinternal', Clara.NewSubmission.InternalFundingWindow);


Clara.NewSubmission.NoFundingWindow = Ext.extend(Ext.Window, {
	id: 'clara-funding-window',
    width: 542,
    height: 330,
    layout: 'absolute',
    title:'No Anticipated Funding',
    iconCls:'icn-exclamation',
	fundingsource:{},
	editing:false,
	modal:true,
	isValid: function(){
		return (Ext.getCmp("fldDept").validate() && Ext.getCmp("fldCostCenter").validate() && Ext.getCmp("fldFund").validate());
	},
	saveOrUpdateSource: function(closeWindowAfter){
		if (Ext.getCmp('clara-funding-window').isValid()){
			var fundingsource = new Clara.NewSubmission.FundingSource({
				entityid:'',
				entityname:Ext.getCmp("fldCostCenter").getValue(),
				entitytype:'Cost Center',
				type:'None',
				subtype:(Ext.getCmp("fldIsStudent").getValue())?"student":"", 
				name:Ext.getCmp("fldFund").getValue(),
				department:Ext.getCmp("fldDept").getValue(),
				contacts:(Ext.getCmp('clara-funding-window').fundingsource.contacts || [])
			});
			clog(fundingsource.toXML());
			if (Ext.getCmp('clara-funding-window').editing){
				var fsID = updateExistingXmlInProtocol("/protocol/funding/funding-source", fundingsource.id, fundingsource.toXML());
				Clara.NewSubmission.MessageBus.fireEvent('sourceupdated', fundingsource);  
			} else {
				var fsID = addXmlToProtocol( "/protocol/funding/funding-source", fundingsource.toXML(), "funding-source");
				Clara.NewSubmission.MessageBus.fireEvent('sourceadded', fundingsource);  
			}
			
			if (closeWindowAfter){
				Ext.getCmp('clara-funding-window').close();
			}else{
				Ext.getCmp("fldCostCenter").reset();
				Ext.getCmp("fldDept").reset();
				Ext.getCmp("fldFund").reset();
			}
		} else {
			alert("Please enter a fund, department and cost center.");
		}	
	},
	constructor:function(config){		
		Clara.NewSubmission.NoFundingWindow.superclass.constructor.call(this, config);
	},
	
	initComponent: function() {
			var fundingStudyType = Ext.getCmp("protocol-fundingsource-panel").fundingStudyType;	
			clog(fundingStudyType);
		var config = {
				items: [{
	                xtype: 'label',
	                text: 'Department',
	                x: 10,
	                y: 10,
	                width: 80
	            },
	            {
	                xtype: 'label',
	                text: 'Fund (must be 3 digits)',
	                x: 10,
	                y: 40,
	                width: 80
	            },
	            {
	                xtype: 'label',
	                text: 'Cost Center (must be 7 digits)',
	                x: 10,
	                y: 70,
	                width: 80
	            },
	            {
	                xtype: 'textfield',
	                x: 100,
	                y: 70,
	                width: 420,
	                maxLength:10,
	                minLength:7,
	                name: 'fldCostCenter',
	                id: 'fldCostCenter',
	                allowBlank:(fundingStudyType == "student-fellow-resident-post-doc"),
			       	tabIndex:3
	            },
	            {
	                xtype: 'textfield',
	                x: 100,
	                y: 10,
	                width: 420,
	                name: 'fldDept',
	                id: 'fldDept',
	                allowBlank:(fundingStudyType == "student-fellow-resident-post-doc"),
			       	tabIndex:1
	            },
	            {
	                xtype: 'checkbox',
	                x: 100,
	                y: 90,
	                width: 420,
	                name: 'fldIsStudent',
	                id: 'fldIsStudent',
	                checked:(fundingStudyType == "student-fellow-resident-post-doc"),
			       	tabIndex:4,
			       	boxLabel:"This is student research"
	            },
	            {
	                xtype: 'numberfield',
	                x: 100,
	                y: 40,
maxLength:3,
minLength:3,
	                width: 420,
	                name: 'fldFund',
	                id: 'fldFund',
	                allowBlank:(fundingStudyType == "student-fellow-resident-post-doc"),
			       	tabIndex:2
	            },
	            {
	                xtype: 'label',
	                html: 'Most studies will incur expenses. If other funding is not indicated, a department name and cost center number shall be provided. Expenses related to the study will be charged to this department cost center. For more information, contact the business manager of the responsible department, or contact the Research Support Center at 526-6876.',
	                x: 110,
	                y: 130,
	                width: 410,
	                style: 'font-size:12px;font-weight:800;'
	            }],
				buttons: [
							{
								text:'Cancel',
								disabled:false,
								handler: function(){
									Ext.getCmp('clara-funding-window').close();
								}
							},
							{
								text:'Save and Close',
								id:'btn-save',
								handler: function(){
									Ext.getCmp('clara-funding-window').saveOrUpdateSource(true);
								}
							}],		
			    listeners:{
					afterrender:function(){
						if (this.editing){
							clog("RENDER DONE. FILLING.");
							Ext.getCmp('fldFund').setValue(this.fundingsource.name);
							Ext.getCmp('fldCostCenter').setValue(this.fundingsource.entityname);
							Ext.getCmp('fldDept').setValue(this.fundingsource.department);
						}
					}
				}
		};
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		Clara.NewSubmission.NoFundingWindow.superclass.initComponent.apply(this, arguments);
	}
});
Ext.reg('claraprotocolfundingwindownone', Clara.NewSubmission.NoFundingWindow);


Clara.NewSubmission.ContactWindow = Ext.extend(Ext.Window, {
	id: 'clara-contact-window',
    width: 542,
    height: 340,
    padding:10,
    layout: 'form',
    title:'Add Contact',
    iconCls:'icn-exclamation',
	fundingsource:{},
	fundingsourcedataid:0,
	contactindex:0,
	editing:false,
	modal:true,
	isValid: function(){
		return (Ext.getCmp("fldContactName").validate());
	},
	saveOrUpdateSource: function(closeWindowAfter){
		if (Ext.getCmp('clara-contact-window').isValid()){
			var contact = new Clara.NewSubmission.Contact({
				id:'',
				name:Ext.getCmp("fldContactName").getValue(),
				title:Ext.getCmp("fldContactTitle").getValue(),
				role:Ext.getCmp("fldContactRole").getValue(),
				phone:Ext.getCmp("fldContactPhone").getValue(),
				cell:Ext.getCmp("fldContactCellPhone").getValue(),
				fax:Ext.getCmp("fldContactFax").getValue(),
				email:Ext.getCmp("fldContactEmail").getValue(),
				address:Ext.getCmp("fldContactAddress").getValue()
			});
			
			if (this.editing){
				this.fundingsource.contacts[this.contactindex] = contact;
			}else {
				this.fundingsource.contacts.push(contact);
			}
			var fsID = updateExistingXmlInProtocol("/protocol/funding/funding-source", this.fundingsource.id, this.fundingsource.toXML());
			Clara.NewSubmission.MessageBus.fireEvent('sourceupdated', this.fundingsource);  

			
			if (closeWindowAfter){
				Ext.getCmp('clara-contact-window').close();
			}else{
				Ext.getCmp("fldContactName").reset();
				Ext.getCmp("fldContactTitle").reset();
				Ext.getCmp("fldContactRole").reset();
				Ext.getCmp("fldContactPhone").reset();
				Ext.getCmp("fldContactCellPhone").reset();
				Ext.getCmp("fldContactFax").reset();
				Ext.getCmp("fldContactEmail").reset();
				Ext.getCmp("fldContactAddress").reset();
			}
		} else {
			alert("Please enter a name.");
		}	
	},
	constructor:function(config){		
		Clara.NewSubmission.ContactWindow.superclass.constructor.call(this, config);
	},
	
	initComponent: function() {
		var t = this;
		// Get fundingsource from store
		var fundingsourcedata = Ext.getCmp("protocol-fundingsource-panel").getStore().getById(this.fundingsourcedataid).data;
		var fs = new Clara.NewSubmission.FundingSource({});
		fs.fromData(fundingsourcedata);
		clog(fs);
		this.fundingsource = fs;
		
		var config = {
				items: [
			            {
			                xtype: 'textfield',
			                name:'fldContactName',
			                id:'fldContactName',
			                allowEmpty:false,
			                fieldLabel: 'Name',
			                anchor: '100%',
					       	tabIndex:1
			            },
			            {
			                xtype: 'textfield',
			                fieldLabel: 'Title',
			                name:'fldContactTitle',
			                id:'fldContactTitle',
			                anchor: '100%',
					       	tabIndex:2
			            },
			            {
			                xtype: 'textfield',
			                fieldLabel: 'Role',
			                name:'fldContactRole',
			                id:'fldContactRole',
			                anchor: '100%',
					       	tabIndex:3
			            },
			            {
			                xtype: 'textfield',
			                name:'fldContactPhone',
			                id:'fldContactPhone',
			                fieldLabel: 'Phone',
			                anchor: '100%',
					       	tabIndex:4
			            },
			            {
			                xtype: 'textfield',
			                fieldLabel: 'Cell Phone',
			                name:'fldContactCellPhone',
			                id:'fldContactCellPhone',
			                anchor: '100%',
					       	tabIndex:5
			            },
			            {
			                xtype: 'textfield',
			                name:'fldContactFax',
			                id:'fldContactFax',
			                fieldLabel: 'Fax',
			                anchor: '100%',
					       	tabIndex:6
			            },
			            {
			                xtype: 'textfield',
			                name:'fldContactEmail',
			                id:'fldContactEmail',
			                fieldLabel: 'Email',
			                anchor: '100%',
					       	tabIndex:7
			            },
			            {
			                xtype: 'textarea',
			                name:'fldContactAddress',
			                id:'fldContactAddress',
			                anchor: '100%',
			                fieldLabel: 'Address',
					       	tabIndex:8
			            }],
					    listeners:{
							afterrender:function(){
								if (this.editing){
									clog("RENDER DONE. FILLING.");
									var c = t.fundingsource.contacts[t.contactindex];
									Ext.getCmp('fldContactName').setValue(c.name);
									Ext.getCmp('fldContactTitle').setValue(c.title);
									Ext.getCmp('fldContactRole').setValue(c.role);
									Ext.getCmp('fldContactPhone').setValue(c.phone);
									Ext.getCmp('fldContactCellPhone').setValue(c.cell);
									Ext.getCmp('fldContactFax').setValue(c.fax);
									Ext.getCmp('fldContactEmail').setValue(c.email);
									Ext.getCmp('fldContactAddress').setValue(c.address);
								}
							}
						},
				buttons: [
							{
								text:'Cancel',
								disabled:false,
								handler: function(){
									Ext.getCmp('clara-contact-window').close();
								}
							},{
								text:'Remove Contact',
								hidden:!t.editing,
								handler: function(){
									Ext.Msg.show({
										title:"Remove a contact?",
										msg:"Are you sure you want to delete this contact?", 
										buttons:Ext.Msg.YESNOCANCEL,
										icon:Ext.MessageBox.WARNING,
										fn: function(btn){
											if (btn == 'yes'){
													t.fundingsource.contacts.splice(t.contactindex, 1);
													var fsID = updateExistingXmlInProtocol("/protocol/funding/funding-source", t.fundingsource.id, t.fundingsource.toXML());
													Clara.NewSubmission.MessageBus.fireEvent('sourceupdated', t.fundingsource);  
													Ext.getCmp('clara-contact-window').close();
												}
											}
										});
								}
							},
							{
								text:'Save and Add Another..',
								id:'btn-save-addanother',
								handler: function(){
									Ext.getCmp('clara-contact-window').saveOrUpdateSource(false);
								}
							},
							{
								text:'Save and Close',
								id:'btn-save',
								handler: function(){
									Ext.getCmp('clara-contact-window').saveOrUpdateSource(true);
								}
							}]
		};
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		Clara.NewSubmission.ContactWindow.superclass.initComponent.apply(this, arguments);
	}
});
Ext.reg('claraprotocolcontactwindow', Clara.NewSubmission.ContactWindow);


Clara.NewSubmission.CROSMOWindow = Ext.extend(Ext.Window, {
	id: 'clara-funding-window',
    width: 542,
    height: 170,
    layout: 'absolute',
    title:'External Funding Source',
	fundingsource:{},
	type:'',
	editing:false,
	modal:true,
	isValid: function(){
		return (Ext.getCmp("fldAgency").validate());
	},
	saveOrUpdateSource: function(closeWindowAfter){
		var t = this;
		if (t.isValid()){
			var agency = Ext.getCmp("fldAgency");
			var fundingsource = new Clara.NewSubmission.FundingSource({
				entityid:''+String(agency.getValue()),
				entityname:jQuery("#fldAgency").val(),
				entitytype:'Agency',
				type:t.type,
				name:'',
				department:'',
				contacts:(t.fundingsource.contacts || [])
			});
			clog(fundingsource.toXML());
			if (Ext.getCmp('clara-funding-window').editing){
				var fsID = updateExistingXmlInProtocol("/protocol/funding/funding-source", fundingsource.id, fundingsource.toXML());
				Clara.NewSubmission.MessageBus.fireEvent('sourceupdated', fundingsource);  
			} else {
				var fsID = addXmlToProtocol( "/protocol/funding/funding-source", fundingsource.toXML(), "funding-source");
				Clara.NewSubmission.MessageBus.fireEvent('sourceadded', fundingsource);  
			}
			
			if (closeWindowAfter){
				Ext.getCmp('clara-funding-window').close();
			}else{
				Ext.getCmp("fldAgency").clearValue();
			}
		} else {
			alert("Please choose an agency.");
		}	
	},
	constructor:function(config){		
		Clara.NewSubmission.CROSMOWindow.superclass.constructor.call(this, config);
	},
	
	initComponent: function() {
		this.title = "Add "+this.type;
		var config = {
				items: [{
	                xtype: 'combo',
	                x: 100,
	                y: 10,
	                width: 420,
	                hiddenName: 'fldAgency',
	                store:Clara.NewSubmission.FundingResearchOrganizationStore,
	                id: 'fldAgency',
	                displayField:'name',
	                valueField:'id',
	    	   	   	typeAhead:false,
			       	forceSelection:true,
			       	mode:'remote', 
			       	triggerAction:'all',
			       	editable:false,
			        allowBlank:false
	            },
	            {
	                xtype: 'label',
	                text: 'Agency',
	                x: 10,
	                y: 10
	            }],
				buttons: [
							{
								text:'Cancel',
								disabled:false,
								handler: function(){
									Ext.getCmp('clara-funding-window').close();
								}
							},
							{
								text:'Save and Add Another..',
								id:'btn-save-addanother',
								handler: function(){
									Ext.getCmp('clara-funding-window').saveOrUpdateSource(false);
								}
							},{
								text:'Save and Close',
								id:'btn-save',
								handler: function(){
									Ext.getCmp('clara-funding-window').saveOrUpdateSource(true);
								}
							}],		
			    listeners:{
					afterrender:function(){
						if (this.editing){
							clog("RENDER DONE. FILLING.");
							Ext.getCmp('fldAgency').setValue(this.fundingsource.entityname);
						}
					}
				}
		};
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		Clara.NewSubmission.CROSMOWindow.superclass.initComponent.apply(this, arguments);
	}
});
Ext.reg('claraprotocolfundingwindowcrosmo', Clara.NewSubmission.CROSMOWindow);