Ext.ns('Clara.NewSubmission');

Clara.NewSubmission.DDMessageBus = new Ext.util.Observable();
Clara.NewSubmission.DDMessageBus.addEvents('pharmacyrequestmade','pharmacystatusupdated');

Clara.NewSubmission.Drug = function(o){
	this.id=				(o.id || '');	
	this.type=				(o.type || '');	
	this.name=				(o.name || '');	
	this.identifier=		(o.identifier || 0);
	this.status=			(o.status || '');	
	this.administration=	(o.administration || '');	
	this.approved=			(o.approved || false);
	this.isprovided=		(o.isprovided || false);
	this.insert=			(o.insert || false);
	this.brochure=			(o.brochure || false);
	this.ind=				(o.ind || '');
	this.nsc=				(o.nsc || '');
	this.provider=			(o.provider || '');	
	this.providerdosage=	(o.providerdosage || '');	
	this.storage=			(o.storage || '');	
	this.prep=				(o.prep || '');	
	this.toxicities=		(o.toxicities || '');
	this.treatmentcenterlocation=	(o.treatmentcenterlocation || '');

	
	this.fromRecord= function(r){
		var t = this;
		t.id = r.get("id");
		t.name = r.get("name");
		t.type = r.get("type");
		t.identifier= r.get("identifier");
		t.status = r.get("status");
		t.administration= r.get("administration");
		t.approved= r.get("approved");
		t.isprovided= r.get("isprovided");
		t.insert = r.get("insert");
		t.brochure = r.get("brochure");
		t.ind= r.get("ind");
		t.nsc= r.get("nsc");
		t.provider= r.get("provider");
		t.providerdosage=r.get("providerdosage");
		t.storage= r.get("storage");
		t.prep= r.get("prep");
		t.toxicities = r.get("toxicities");
		t.treatmentcenterlocation = r.get("treatmentcenterlocation");
	};
	
	this.toXML= function(){
		Encoder.EncodeType = "entity";
		return "<drug id='"+this.id+"' type='"+this.type+"' identifier='"+this.identifier+"' name='"+Encoder.htmlEncode(this.name)+"' status='"+this.status+"' admin='"+this.administration+"' insert='"+this.insert+"' isprovided='"+this.isprovided+"' brochure='"+this.brochure+"' approved='"+this.approved+"' ind='"+this.ind+"' nsc='"+this.nsc+"'"+
			   " provider='"+Encoder.htmlEncode(this.provider)+"'><providerdosage>"+Encoder.htmlEncode(this.providerdosage)+"</providerdosage>"+
			   "<storage>"+Encoder.htmlEncode(this.storage)+"</storage><prep>"+Encoder.htmlEncode(this.prep)+"</prep><toxicities>"+Encoder.htmlEncode(this.toxicities)+"</toxicities>"+
			   "<treatmentcenterlocation>"+Encoder.htmlEncode(this.treatmentcenterlocation)+"</treatmentcenterlocation></drug>";
	};
};

Clara.NewSubmission.Device = function(o){
	this.id=				(o.id || '');	
	this.name=				(o.name || '');	
	this.manufacturer=		(o.manufacturer || '');	
	this.modelnumber=		(o.modelnumber || 0);
	this.identifier=		(o.identifier || 0);
	this.status=			(o.status || '');	
	this.approved=			(o.approved || false);
	this.riskpotential=		(o.riskpotential || false);	
	this.riskhealthimpact=	(o.riskhealthimpact || false);	
	this.risksustainlife=	(o.risksustainlife || false);	
	this.riskimplant=		(o.riskimplant || false);	
	this.ide=				(o.ide || '');	
	
	this.toXML= function(){
		return "<device id='"+this.id+"' identifier='"+this.identifier+"' name='"+Encoder.htmlEncode(this.name)+"' manufacturer='"+Encoder.htmlEncode(this.manufacturer)+"' approved='"+this.approved+"' modelnumber='"+Encoder.htmlEncode(this.modelnumber)+"' status='"+this.status+"' riskpotential='"+this.riskpotential+"' riskhealthimpact='"+this.riskhealthimpact+"' risksustainlife='"+this.risksustainlife+"' riskimplant='"+this.riskimplant+"'"+
			   " ide='"+Encoder.htmlEncode(this.ide)+"' />";
	};
};

Clara.NewSubmission.ProtocolDrugStore = new Ext.data.XmlStore({
	proxy: new Ext.data.HttpProxy({
		url: appContext + "/ajax/protocols/" + claraInstance.id + "/protocol-forms/" + claraInstance.form.id + "/protocol-form-xml-datas/" + claraInstance.form.xmlDataId + "/xml-elements/list",
		method:"GET",
		headers:{'Accept':'application/xml;charset=UTF-8'}
	}),
	baseParams:{'listPath':'/protocol/drugs/drug'},
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
		{name:'providerdosage',mapping:'providerdosage'},
		{name:'storage',mapping:'storage'},
		{name:'prep',mapping:'prep'},
		{name:'toxicities',mapping:'toxicities'},
		{name:'treatmentcenterlocation',mapping:'treatmentcenterlocation'}
	]
});

Clara.NewSubmission.DrugStatusStore = new Ext.data.XmlStore({
	proxy: new Ext.data.HttpProxy({
		url: appContext+"/static/xml/lookup.xml",
		method:"GET",
		headers:{'Accept':'application/xml;charset=UTF-8'}
	}),
	record:"drug-status>option",
	fields:[{name:'value'},{name:'desc'}]
});

Clara.NewSubmission.DrugAdminStore = new Ext.data.XmlStore({
	proxy: new Ext.data.HttpProxy({
		url: appContext+"/static/xml/lookup.xml",
		method:"GET",
		headers:{'Accept':'application/xml;charset=UTF-8'}
	}),
	record:"drug-administration>option",
	fields:[{name:'value'},{name:'desc'}]
});


Clara.NewSubmission.DeviceTypeStore = new Ext.data.XmlStore({
	proxy: new Ext.data.HttpProxy({
		url: appContext+"/static/xml/lookup.xml",
		method:"GET",
		headers:{'Accept':'application/xml;charset=UTF-8'}
	}),
	record:"device-type>option",
	fields:['value','desc']
});

Clara.NewSubmission.DrugStore = new Ext.data.Store({
	header :{
    	'Accept': 'application/json'
	},
	proxy: new Ext.data.HttpProxy({
		url: appContext + "/ajax/protocols/protocol-forms/new-submission/drugs/search",
		method:"GET"
	}),
	reader: new Ext.data.JsonReader({
		root: 'drugs',
		idProperty: 'id'
	}, [
		{name:'id'},
		{name:'approved',mapping:'approved'},
		{name:'description', mapping:'description'}
	])
});

Clara.NewSubmission.DeviceStore = new Ext.data.Store({
	header :{
    	'Accept': 'application/json'
	},
	proxy: new Ext.data.HttpProxy({
		url: appContext + "/ajax/protocols/protocol-forms/new-submission/devices/search",
		method:"GET"
	}),
	reader: new Ext.data.JsonReader({
		root: 'devices',
		idProperty: 'id'
	}, [
		{name:'id'},
		{name:'approved',mapping:'approved'},
		{name:'description', mapping:'description'}
	])
});

