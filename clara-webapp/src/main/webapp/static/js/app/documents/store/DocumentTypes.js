Ext.define('Clara.Documents.store.DocumentTypes', {
    extend: 'Ext.data.Store',
    requires: 'Clara.Documents.model.DocumentType',    
    model: 'Clara.Documents.model.DocumentType',
    autoLoad: false,
    sorters:[{
        property: 'created',
        direction: 'DESC'
      }],
      
    loadDocumentTypes: function(){
    	var me = this,
	    type = claraInstance.type,
	    id = claraInstance.id,
	    formId = (claraInstance.form.id || 0),
	    url= appContext+"/ajax/"+type+"s/"+id+"/"+type+"-forms/"+formId+"/list-doc-types?userId="+claraInstance.user.id+"&committee="+(claraInstance.committee || '')+"&docAction="+(getUrlVars()["docAction"] || '');
	
		clog("loadDocumentTypes(): "+url);
		
		me.getProxy().url = url;
		me.load({
			callback: function(recs){
				Clara.Application.getController("Clara.Documents.controller.Documents").fireEvent("documentTypesLoaded", recs);
			}
		});
    }
});