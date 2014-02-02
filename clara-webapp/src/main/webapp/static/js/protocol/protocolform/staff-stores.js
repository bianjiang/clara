var staffRoleReader;
var staffRespReader;
var staffstore;

function initializeStaffStores(){	
	
	var url = appContext + "/ajax/"+claraInstance.type+"s/" + claraInstance.id + "/"+claraInstance.type+"-forms/" + claraInstance.form.id + "/"+claraInstance.type+"-form-xml-datas/" + claraInstance.form.xmlDataId + "/";
	
	staffRoleReader = new Ext.data.XmlReader({
		record: 'role',
		fields: [{name:'role'}]
	});
	
	staffRespReader = new Ext.data.XmlReader({
		record: 'responsibility',
		fields: [{name:'responsibility'}]
	});
	
	staffCostReader = new Ext.data.XmlReader({
		record: 'cost',
		fields: [{name:'startdate', mapping:'@startdate', type:'date', dateFormat:'m/d/Y'},{name:'enddate', mapping:'@enddate', type:'date', dateFormat:'m/d/Y'},{name:'salary', mapping:'@salary', type:'float'},{name:'fte', mapping:'@fte', type:'float'}]
	});
	
	staffstore = new Ext.data.XmlStore({
		proxy: new Ext.data.HttpProxy({
			url: url + "xml-elements/list",
			method:"GET",
			headers:{'Accept':'application/xml;charset=UTF-8'}
		}),
        listeners: {
            exception: function(dp,type,action,opt,resp,arg) {
                cerr('load failed -- '+type+' .. '+action,dp,type,action,resp,arg);
            }
		},
		baseParams:{listPath: "/" + claraInstance.form.xmlBaseTag + '/staffs/staff'},
		record:'staff',
		root:'list',
		autoLoad:true,
		fields: [
			{name:'id', mapping:'@id'},
			{name:'notify', mapping:'notify'},
			{name:'userid',mapping:'user@id'},
			{name:'usersapid',mapping:'user@sap'},
			{name:'lastname', mapping:'user>lastname'},
			{name:'firstname',mapping:'user>firstname'},
			{name:'email',mapping:'user>email'},
			{name:'conflictofinterest',mapping:'conflict-of-interest'},
			{name:'conflictofinterestdesc',mapping:'conflict-of-interest-description'},
			{name:'costs',convert:function(v,node){ return staffCostReader.readRecords(node).records; }},
			{name:'roles',convert:function(v,node){ return staffRoleReader.readRecords(node).records; }},
			{name:'responsibilities',convert:function(v,node){ return staffRespReader.readRecords(node).records; }}
		]
	});
	

}