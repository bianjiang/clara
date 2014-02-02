Ext.ns('Clara.NewSubmission');

Clara.NewSubmission.ReloadCodes = function(){
	Ext.getCmp("protocol-icd9-panel").loadCodes();
};

Clara.NewSubmission.ConfirmRemoveCode = function(code){
	Ext.Msg.show({
		title:"WARNING: About to delete a code",
		msg:"Are you sure you want to delete this code?", 
		buttons:Ext.Msg.YESNOCANCEL,
		icon:Ext.MessageBox.WARNING,
		fn: function(btn){
			if (btn == 'yes'){
				if (code){
					url = appContext + "/ajax/protocols/" + claraInstance.id + "/protocol-forms/" + claraInstance.form.id + "/protocol-form-xml-datas/" + claraInstance.form.xmlDataId + "/xml-elements/delete";

					data = {	
							listPath: "/protocol/codes/code",
							elementId: code.id
					};
					
					jQuery.ajax({
						async: false,
						url: url,
						type: "POST",
						dataType: 'xml',
						data: data
					});
					Clara.NewSubmission.ReloadCodes();
					Ext.getCmp("protocol-icd9-panel").selectedCode = {};
				}
			}
		}
		
	});
	return false;
};

codeStore = new Ext.data.Store({
	header :{
           'Accept': 'application/json'
       },
	proxy: new Ext.data.HttpProxy({
		url: appContext+"/ajax/"+claraInstance.type+"s/"+claraInstance.type+"-forms/icd9/search",
		method:'GET'
	}),
	autoLoad:false,
	reader: new Ext.data.JsonReader({
		root: 'data',
		idProperty: 'code'
	}, [
	    {name:'code', mapping:'identifier'},
	    {name:'codeType', mapping:'type'},
	    {name:'desc', mapping:'description'},
	    {name:'shortdesc', mapping:'value'}
	])
});

Clara.NewSubmission.NewCodeWindow  = Ext.extend(Ext.Window, {
    title: 'Add Code',
    width: 503,
    height: 379,
    modal: true,
    layout: 'border',
    iconCls: 'icn-ui-button-default',
    itemId: 'winAddCode',
    id: 'winAddCode',
    selectedCode:{},
    initComponent: function() {
    	var t = this;
    	t.buttons = [
    	{
			text:'Add',
			id:'btn-save-code',
			disabled:true,
			handler: function(){
				var xml = "<code value='"+t.selectedCode.get("code")+"' code-type='ICD9' code-subtype='"+t.selectedCode.get("codeType")+"'><desc>"+t.selectedCode.get("desc")+"</desc></code>";
				addXmlToProtocol( '/'+claraInstance.form.xmlBaseTag+'/codes/code', xml);
				Ext.getCmp("protocol-icd9-panel").loadCodes();
				t.close();
				
			}
    	}];
    	t.items = [
                   new Ext.ux.form.SearchField({
                       store:codeStore,
                       region:'north',
                       paramName:'keyword',
                       emptyText:'Search by code or description',
                       style: 'font-size:14px;',
                       itemId: 'fldSearch',
                       id: 'fldSearch',
                       afterClear: function(){
              		   		
                      	 	Ext.getCmp("btn-save-code").disable();
              	   			
              	   		},
                       afterSearch: function(){
           		   		
                   	 	Ext.getCmp("btn-save-code").disable();
           	   			
           	   		}
                   }),{
    		region:'center',
            xtype: 'grid',
            store: codeStore,
            selModel: new Ext.grid.RowSelectionModel({
	        	singleSelect:true,
	        	listeners: {
	        		rowselect: function(grid,rowIndex,record){
            					t.selectedCode = record;
	        					Ext.getCmp("btn-save-code").enable();
	        				}
	        	}
	        }),
            height: 170,
            x: 0,
            y: 20,
            width: 490,
            border: false,
            itemId: 'gridSearchResults',
            loadMask: true,
            enableColumnResize: false,
            enableColumnMove: false,
            enableColumnHide: false,
            stripeRows: true,
            id: 'gridSearchResults',
            columns: [
                
                {
                    xtype: 'gridcolumn',
                    dataIndex: 'code',
                    header: 'Code',
                    sortable: true,
                    width: 80
                },
                {
                    xtype: 'gridcolumn',
                    dataIndex: 'codeType',
                    header: 'Type',
                    sortable: true,
                    width: 100
                },
                {
                    xtype: 'gridcolumn',
                    dataIndex: 'desc',
                    header: 'Description',
                    sortable: true,
                    width: 250
                }
            ]
        
    	}];
    	
    	Clara.NewSubmission.NewCodeWindow.superclass.initComponent.call(this);
    }

    
});


Clara.NewSubmission.ProtocolICD9Panel = Ext.extend(Ext.grid.GridPanel, {
	id: 'protocol-icd9-panel',
	frame:false,
	stripeRows:true,
	height:250,
	selectedCode:{},
    autoExpandColumn:'gpcodedesccol',
	autoExpandMax:3000,
	constructor:function(config){		
		Clara.NewSubmission.ProtocolICD9Panel.superclass.constructor.call(this, config);
	},	
    listeners:{
    	afterrender:function(t){
    		clog("afterrender");
    		t.loadCodes();
    	},
	    rowclick: function(grid, rowI, event)   {
			var code = grid.getStore().getAt(rowI).data;
			clog(code);
			Ext.getCmp("protocol-icd9-panel").selectedCode = code;
			
	    }
    },
	loadCodes:function(){
		this.getStore().removeAll();
		this.getStore().load({params:{listPath:'/protocol/codes/code'}});
	},
	initComponent: function() {
		var config = {
				view: new Ext.grid.GridView({
		    		forceFit:true
		    	}),
				store:new Ext.data.XmlStore({
					autoLoad:false,
					baseParams:{listPath:'/'+claraInstance.form.xmlBaseTag+'/codes/code'},
					proxy: new Ext.data.HttpProxy({
						
						url: appContext + "/ajax/protocols/" + claraInstance.id + "/protocol-forms/" + claraInstance.form.id + "/protocol-form-xml-datas/" + claraInstance.form.xmlDataId + "/xml-elements/list",
						method:"GET",
						headers:{'Accept':'application/xml;charset=UTF-8'}
					}),
					record: 'code', 
					fields: [
						{name:'id', mapping:'@id'},
						{name:'code', mapping:'@value'},
						{name:'codetype',mapping:'@code-type'},
						{name:'codesubtype',mapping:'@code-subtype'},
						{name:'desc',mapping:'desc'}
					]
				}),

				
				sm: new Ext.grid.RowSelectionModel({singleSelect: true}),
		        loadMask: new Ext.LoadMask(Ext.getBody(), {msg:"Please wait..."}),
		        columns: [
		                  {
		                	  	header:'Code',
		                	  	dataIndex:'code',
		                	  	sortable:true,
		                	  	width:100
		                  },
		                  {
		                	  	header:'Type',
		                	  	dataIndex:'codesubtype',
		                	  	sortable:true,
		                	  	width:100
		                  },
		                  {
		                	  	header:'Description',
		                	  	dataIndex:'desc',
		                	  	sortable:false,
		                	  	id:'gpcodedesccol',
		                	  	width:300,
		                	  	menuDisabled:true
		                  }
		        ]
		        

		};
		
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		Clara.NewSubmission.ProtocolICD9Panel.superclass.initComponent.apply(this, arguments);
		
	}
	

});
Ext.reg('claraprotocolicd9panel', Clara.NewSubmission.ProtocolICD9Panel);