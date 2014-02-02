Ext.define('Clara.Documents.store.Documents', {
    extend: 'Ext.data.Store',
    requires: 'Clara.Documents.model.Document',    
    model: 'Clara.Documents.model.Document',
    autoLoad: false,
    sorters:[{
        property: 'created',
        direction: 'DESC'
      }],
    loadDocuments: function(filters){
    	var me = this,
	    type = claraInstance.type,
	    id = claraInstance.id,
	    url= appContext+"/ajax/"+type+"s/"+id+"/documents/list";
	
		clog("loadDocuments(): "+url);
		
		me.getProxy().url = url;
		me.load({
			callback: function(recs){
				Clara.Application.getController("Clara.Documents.controller.Documents").fireEvent("documentsLoaded", recs);
			}
		});
    }
});