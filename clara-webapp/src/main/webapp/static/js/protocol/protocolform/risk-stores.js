var protocolToxinStore;
var approvedToxinStore;
function initializeToxinStores(){
	var url = appContext + "/ajax/"+claraInstance.type+"s/" + claraInstance.id + "/"+claraInstance.type+"-forms/" + claraInstance.form.id + "/"+claraInstance.type+"-form-xml-datas/" + claraInstance.form.xmlDataId;

	approvedToxinStore = new Ext.data.Store({
		header :{
	           'Accept': 'application/json'
	       },
		proxy: new Ext.data.HttpProxy({
			url: appContext+'/ajax/'+claraInstance.type+'s/'+claraInstance.type+'-forms/new-submission/risks/toxins/search',
			method:'GET'
		}),
		autoLoad:false,
		reader: new Ext.data.JsonReader({
			root: 'toxins',
			idProperty: 'id'
		}, [
		    {name:'id', mapping: 'id'},
		    {name:'toxinName', mapping:'value'}		    
		])
	});
	
	protocolToxinStore = new Ext.data.XmlStore({
        proxy: new Ext.data.HttpProxy({
            url: url + "/xml-elements/list",
            method: "GET",
            headers: {
                'Accept': 'application/xml;'
            }
        }),
        record: 'toxin',
        autoLoad: true,
        root: 'list',
        baseParams: {
            listPath: '/' + claraInstance.form.xmlBaseTag + '/risks/toxins/toxin'
        },
        fields: [
                 {name: 'id', mapping: '@id'}, 
                 {name: 'toxin-name', mapping: 'toxin-name'}
                 ]
    });
	
}