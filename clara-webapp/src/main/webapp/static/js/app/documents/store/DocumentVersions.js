Ext.define('Clara.Documents.store.DocumentVersions', {
    extend: 'Clara.Documents.store.Documents',
    loadDocumentVersions: function(parentDocId){
    	var me = this,
	    type = claraInstance.type,
	    id = claraInstance.id,
	    url= appContext+"/ajax/"+type+"s/0/"+type+"-forms/0/"+type+"-form-xml-datas/0/documents/"+parentDocId+"/list-versions";
	
		clog("loadDocumentVersions(): "+url);
		
		me.getProxy().url = url;
		me.load({
			callback: function(recs){
				Clara.Application.getController("Clara.Documents.controller.Documents").fireEvent("documentVersionsLoaded", recs);
			}
		});
    }
});