Ext.define('Clara.Documents.model.Document', {
	extend: 'Ext.data.Model',
	fields: [
	         {name:'id'},
	         {name:'id', mapping:'id'},
	         {name:'uploadedFileId', mapping:'uploadedFile.id'},
	         {name:'hashid', mapping:'uploadedFile.identifier'},
	         {name:'documentname', mapping:'uploadedFile.filename'},
	         {name:'category'},
	         {name:'title'},
	         {name:'versionId'},
	         {name:'status'},
	         {name:'path', mapping:'uploadedFile.path'},
	         {name:'parentid', mapping:(claraInstance.type == 'protocol')?'parentProtocolFormXmlDataDocumentId':'parentContractFormXmlDataDocumentId'},
	         {name:'parentFormId', mapping:(claraInstance.type == 'protocol')?'parentProtocolFormId':'parentContractFormId'},
	         {name:'committee', mapping:'committee'},
	         {name:'formId', mapping:(claraInstance.type == 'protocol')?'protocolFormId':'contractFormId'},
	         {name:'formType', mapping:(claraInstance.type == 'protocol')?'protocolFormType':'contractFormType'},
	         {name:'formTypeDesc', mapping:(claraInstance.type == 'protocol')?'protocolFormTypeDesc':'contractFormTypeDesc'},
	         {name:'formXmlDataId', mapping:(claraInstance.type == 'protocol')?'protocolFormXmlDataId':'contractFormXmlDataId'},
	         {name:'created', mapping:'createdDate', type: 'date', dateFormat: 'm/d/Y H:i:s'},
	         {name:'extension', mapping:'uploadedFile.extension'}
	         ],
	         proxy: {
	        	 type: 'ajax',
	        	 url: "",	
	        	 actionMethods: {
	                 read: 'GET'
	             },
	        	 headers:{'Accept':'application/json;charset=UTF-8'},
	        	 reader:{
	        		 idProperty: 'id'
	        	 }
	         }
});
