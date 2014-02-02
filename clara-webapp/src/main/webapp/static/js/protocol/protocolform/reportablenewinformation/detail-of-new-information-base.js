Ext.ns('Clara.ReportableNewInformation');

Clara.ReportableNewInformation.Event = function(o){
	this.id=					(o.id || '0');
	this.type=					(o.type || '');
	this.dateofreport=			(o.dateofreport || '');
	this.dateofsponsornotified=	(o.dateofsponsornotified || '');
	this.dateofsaeonset=		(o.dateofsaeonset || '');
	this.reporttype=			(o.reporttype || '');
	this.reportnumber=			(o.reportnumber || '');
	//this.dateofinitialreport=	(o.dateofinitialreport || ''); 
	//this.eventoutcome=			(o.eventoutcome || '');
	this.participantid=			(o.participantid || '');
	//this.dob=					(o.dob || '');
	//this.gender=				(o.gender || '');
	//this.actiontaken=			(o.actiontaken || '');
	this.changetoprotocol=		(o.changetoprotocol || '');
	this.changetolocalconsent=	(o.changetolocalconsent || '');
	this.chagnetoglobalconsent=	(o.chagnetoglobalconsent || '');
	this.additionalinformation=			(o.additionalinformation || '');
	
	this.toXML= function(){
		return "<event id='"+this.id+"' type='"+this.type+"'" +
				" dateofreport='"+this.dateofreport+"' dateofsponsornotified='"+this.dateofsponsornotified+"'" +
				" dateofsaeonset='"+this.dateofsaeonset+"'" +
				" reporttype='"+this.reporttype+"' reportnumber='"+this.reportnumber+"'" +
				//" dateofinitialreport='"+this.dateofinitialreport+"' eventoutcome='"+this.eventoutcome+"'" +
				" participantid='"+this.participantid+"'" +
				" changetoprotocol='"+this.changetoprotocol+"' changetolocalconsent='"+this.changetolocalconsent+"'" +
				" chagnetoglobalconsent='"+this.chagnetoglobalconsent+"' additionalinformation='"+this.additionalinformation+"' />";
	};
};


Clara.ReportableNewInformation.EventStore = new Ext.data.XmlStore({
	proxy: new Ext.data.HttpProxy({
		url: appContext + "/ajax/protocols/" + claraInstance.id + "/protocol-forms/" + claraInstance.form.id + "/protocol-form-xml-datas/" + claraInstance.form.xmlDataId + "/xml-elements/list",
		method:"GET",
		headers:{'Accept':'application/xml;charset=UTF-8'}
	}),
	baseParams:{'listPath':'/reportable-new-info/detail-of-new-information/events/event'},
	record: 'event', 
	fields: [
		{name:'id', mapping:'@id'},
		{name:'type',mapping:'@type'},
		{name:'dateofreport', mapping:'@dateofreport'},
		{name:'dateofsponsornotified',mapping:'@dateofsponsornotified'},
		{name:'dateofsaeonset',mapping:'@dateofsaeonset'},
		{name:'reporttype',mapping:'@reporttype'},
		{name:'reportnumber',mapping:'@reportnumber'},
		//{name:'dateofinitialreport',mapping:'@dateofinitialreport'},
		//{name:'eventoutcome',mapping:'@eventoutcome'},
		{name:'participantid',mapping:'@participantid'},
		//{name:'dob',mapping:'@dob'},
		//{name:'gender',mapping:'@gender'},
		//{name:'actiontaken',mapping:'@actiontaken'},
		{name:'changetoprotocol',mapping:'@changetoprotocol'},
		{name:'changetolocalconsent',mapping:'@changetolocalconsent'},
		{name:'chagnetoglobalconsent',mapping:'@chagnetoglobalconsent'},
		{name:'additionalinformation',mapping:'@additionalinformation'}
	]
});