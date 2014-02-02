Ext.ns('Clara');

Clara.TemplateLoadWindow = Ext.extend(Ext.Window, {
	templateStore: null,
	xml:"",
	selectedTemplateId:0,
	modal:true,
	width: 480,
    height: 440,
    layout: 'fit',
    labelAlign: 'top',
    supressCloseCallback:false,
    cancelCallback: function(){return true;},
    loadTemplateCallback: function(xml){return true;},
	initComponent: function() {
		var t = this;
		var templatedesc = (this.templateStore.templateType == "STAFF")?"Staff Group":"Budget Template";
		
		this.title = 'Load '+templatedesc;
		this.items = [{
			border:false,
			xtype:'grid',
			store:t.templateStore,
			sm: new Ext.grid.RowSelectionModel({singleSelect: true}),
	        loadMask: new Ext.LoadMask(Ext.getBody(), {msg:"Please wait..."}),
	        columns: [
	                  {
	                	   xtype: 'gridcolumn',
	                	  	header:templatedesc+' name',
	                	  	dataIndex:'name',
	                	  	sortable:true,
	                	  	renderer: function(v,s,r){
	                	  		var t = "<div class='template-row'>";
	                	  		t += "<h1>"+r.get("name")+"</h1><span>Created by "+r.get("creator")+" on "+Ext.Date.format(r.get("created"), 'm/d/Y')+"</span>";
	                	  		return t+"</div>";
	                	  	}
	                  }
	        ],
	        viewConfig: {
				forceFit:true
			},
			listeners:{
			    rowclick: function(grid, rowI, event)   {
					var data = grid.getStore().getAt(rowI).data;
					t.selectedTemplateId = data.id;
					Ext.getCmp("btnLoadTemplate").setDisabled(false);
					clog("TEMPLATE window: Selected id "+t.selectedTemplateId);
			    }
		    }
	                  
		}];
		this.buttons = [{
			id:'btnLoadTemplate',
			text:'Load',
			disabled:true,
			handler: function(){
				clog("Loading..");
				//if (t.templateStore.templateType == "STAFF")
					var x = t.templateStore.loadTemplate(t.selectedTemplateId);
				//else
				//	var x = t.templateStore.loadTemplate(t.selectedTemplateId,"xml");
				t.loadTemplateCallback(x);
				t.supressCloseCallback = true;
				t.close();
			}
		}];
		this.listeners={
			close: function(p)   {
				if (!t.supressCloseCallback){
					clog("Cancel callback");
					t.cancelCallback();
				} else {
					clog("Supressing cancel callback");
				}
		    }
	    };
		Clara.TemplateSaveWindow.superclass.initComponent.call(this);
	}
});


Clara.TemplateSaveWindow = Ext.extend(Ext.Window, {
	templateStore: null,
	xml:"",
	modal:true,
	width: 480,
    height: 140,
    layout: 'form',
    padding: 6,
    labelAlign: 'top',
    saveTemplateCallback: function(){return true;},
	initComponent: function() {
		var t = this;
		var templatedesc = (this.templateStore.templateType == "STAFF")?"Staff Group":"Budget Template";
		
		this.title = 'Save '+templatedesc;
		this.items = [{
			xtype: 'textfield',
			id:'fldTemplateName',
            fieldLabel: 'Enter a name or description for this '+templatedesc,
            anchor: '100%',
            labelStyle: 'font-weight:800;'
		}];
		this.buttons = [{
			id:'btnSaveTemplate',
			text:'Save',
			handler: function(){
				var txt = Ext.getCmp("fldTemplateName").getValue();
				if (jQuery.trim(txt) != ""){
					clog("Saving..");
					t.templateStore.saveTemplate(jQuery.trim(txt),t.xml);
					t.templateStore.removeAll();
					t.templateStore.load();
					t.saveTemplateCallback();
					t.close();
				}
			}
		}];
		Clara.TemplateSaveWindow.superclass.initComponent.call(this);
	}
});

Clara.TemplateStore = Ext.extend(Ext.data.Store, {
	autoLoad:true,
	templateType: "",
	userId: 0,
	proxy: new Ext.data.HttpProxy({
		url: appContext + "/ajax/protocols/protocol-forms/user-templates/list-by-type-and-user",
		method:"GET"
	}),
	reader: new Ext.data.JsonReader({
		idProperty: 'id'
	}, [
		{name:'id'},
		{name:'name', mapping:'templateName'},
		{name:'templateType'},
		{name:'created',mapping:'created', type: 'date', dateFormat: 'timestamp'},
		{name:'ownerName',mapping:'user.person.fullname'}
	]),
	saveTemplate: function(name, xmlstr){
		// Strip "<list>" tags first..
		xmlstr = xmlstr.replace("<list>","");
		xmlstr = xmlstr.replace("</list>","");
		clog("string xml: ");
		clog(xmlstr);
		jQuery.ajax({
			async: false,
			url: appContext + "/ajax/protocols/protocol-forms/user-templates/add",
			type: "POST",
			dataType: 'xml',
			data: {
				templateType: this.templateType,
				userId: this.userId,
				templateName:name,
				xmlData:xmlstr
			},
			success: function(out){
				clog("Success. Data: "+out);
			}
		});
	},
	loadTemplate: function(id, returntype){
		var rt = (returntype)?returntype:"text";
		var data= "";
		jQuery.ajax({
			async: false,
			url: appContext + "/ajax/protocols/protocol-forms/user-templates/"+id+"/get-xml-data",
			type: "POST",
			dataType: rt,
			data: {
				userId: this.userId
			},
			success: function(d){
				clog("success");
				data = d;
			}
		});
		return data;
	},
	removeTemplate: function(id){
		jQuery.ajax({
			async: false,
			url: appContext + "/ajax/protocols/protocol-forms/user-templates/"+id+"/remove",
			type: "POST",
			dataType: 'xml',
			data: {
				userId: this.userId
			},
			success: function(out){
				return out;
			}
		});
	},
	constructor:function(config){			
		Clara.TemplateStore.superclass.constructor.call(this, config);
	},
	initComponent: function() {
		var config = {
			baseParams: {
			templateType: this.templateType,
			userId: this.userId
		}	
		};
		Ext.apply(this, Ext.apply(this.initialConfig, config));		
		Clara.TemplateStore.superclass.constructor.call(this, config);
	}
});