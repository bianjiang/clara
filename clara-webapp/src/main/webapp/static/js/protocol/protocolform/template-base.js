Ext.ns('Clara','Clara.NewSubmisssion');

Clara.TemplateGridPanel = Ext.extend(Ext.grid.GridPanel, {
	frame:false,
	xml:'',
	id:'gpTemplates',
	parent:{},
	stripeRows:true,
	height:250,
	templateStore: null,
	selectedTemplateId:0,
	selectedTemplate:null,
	constructor:function(config){		
		Clara.TemplateGridPanel.superclass.constructor.call(this, config);
	},	
	action:'',
	initComponent: function() {
		var me = this;
		var templatedesc = (me.templateStore.templateType == "STAFF")?"Staff Group":(me.templateStore.templateType == "DISEASE_ONTOLOGY")?"Disease Ontology":"Budget Template";
		var config = {
				border:false,
				id:'gpTemplates',
				store:me.templateStore,
				sm: new Ext.grid.RowSelectionModel({singleSelect: true}),
		        // loadMask: new Ext.LoadMask(Ext.getBody(), {msg:"Please wait..."}),
		        columns: [
		                  {
		                	   xtype: 'gridcolumn',
		                	  	header:templatedesc+' name',
		                	  	dataIndex:'name',
		                	  	sortable:true,
		                	  	renderer: function(v,s,r){
		                	  		var tm = "<div class='template-row'>";
		                	  		clog(r.get("created"));
		                	  		tm += "<h1>"+r.get("name")+"</h1><span style='font-style:italic;color:#888;'>Created by "+r.get("ownerName")+"</span>";
		                	  		return tm+"</div>";
		                	  	}
		                  },
		                    {
		                  	  	header:'Action',
		                  	  	dataIndex:'id',
		                  	    renderer: function(v,p,r) { 
		                  	    	var html = "<div>";
		                  	    	
		                  	    	if (me.action == "LOAD") html += "<button onclick='javascript:Ext.getCmp(\"gpTemplates\").parent.loadTemplate("+v+");'>Load</button>";
		                  	    	if (me.action == "SAVE") html += "<button onclick='javascript:Ext.getCmp(\"gpTemplates\").parent.replaceTemplate("+v+");'>Replace</button>";
		                  	    	if (r.get("userId") == claraInstance.user.id) html += "<button onclick='javascript:Ext.getCmp(\"gpTemplates\").getStore().removeTemplate("+v+");'>Delete</button>";
		                  	    	
		                  	    	return html+"</div>";
		                  	    },
			                  	sortable:false,
			                  	width:80
			                }
		        ],
		        viewConfig: {
					forceFit:true
				},
				listeners:{
				    rowclick: function(grid, rowI, event)   {
						var data = grid.getStore().getAt(rowI).data;
						me.selectedTemplate = grid.getStore().getAt(rowI);
						me.selectedTemplateId = me.selectedTemplate.get("id");
						clog("TEMPLATE gridpanel: Selected id "+me.selectedTemplate.get("id"));
				    }
			    }
		};
		
		Ext.apply(me, Ext.apply(me.initialConfig, config));
		Clara.TemplateGridPanel.superclass.initComponent.apply(me, arguments);
	}
});
Ext.reg('claratemplategridpanel', Clara.TemplateGridPanel);


Clara.TemplateLoadWindow = Ext.extend(Ext.Window, {
	templateStore: null,
	xml:"",

	modal:true,
	width: 480,
    height: 440,
    layout: 'fit',
    labelAlign: 'top',
    supressCloseCallback:false,
    cancelCallback: function(){return true;},
    loadTemplateCallback: function(xml, template){return true;},
    loadTemplate: function(selectedTemplateId){
    	var t = this;
    	var x = t.templateStore.loadTemplate(selectedTemplateId);
		t.loadTemplateCallback(x, selectedTemplateId);
		t.supressCloseCallback = true;
		t.close();
    },
	initComponent: function() {
		var t = this;
		var templatedesc = (this.templateStore.templateType == "STAFF")?"Staff Group":(this.templateStore.templateType == "DISEASE_ONTOLOGY")?"Disease Ontology":"Budget Template";
		this.title = 'Load '+templatedesc;
		this.items = [{xtype:'claratemplategridpanel',templateStore:t.templateStore, action:'LOAD', parent:t}];
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
    height: 430,
    layout: 'border',
    padding: 6,
    labelAlign: 'top',
    saveTemplateCallback: function(){return true;},
    replaceTemplate: function(id){
    	var t = this;
    	var rec = t.templateStore.getById(id);
    	Ext.Msg.confirm("Replace template?","Are you sure you want to replace this template?", function(btn){
    		if (btn == "yes"){
    			Ext.Msg.prompt('New Name', 'Please enter a template name:', function(btn, text){
    	    	    if (btn == 'ok'){
    	    	    	t.templateStore.replaceTemplate(id, text, Ext.getCmp("gpTemplates").xml);
    	    	    	t.templateStore.removeAll();
    					t.templateStore.load();
    	    	    	t.saveTemplateCallback();
    	    			t.close();
    	    	    }
    	    	},this,false,rec.get("name"));
    		}
    	});
   
    },
	initComponent: function() {
		var t = this;
		var templatedesc = (t.templateStore.templateType == "STAFF")?"Staff Group":(t.templateStore.templateType == "DISEASE_ONTOLOGY")?"Disease Ontology":"Budget Template";
		clog("Clara.TemplateSaveWindow: init with XML", t.xml);
		this.title = 'Save';
		this.items = [{
			xtype:'form',
			region:'north',
			border:false,
			padding:6,
			title:'Create a new '+templatedesc,
			height:88,
			items:[
			    {
			    	xtype: 'textfield',
					id:'fldTemplateName',
		            fieldLabel: 'Template name',
		            anchor: '100%',
		            labelStyle: 'font-weight:800;'
            	},
            {
    			id:'btnSaveTemplate',
    			xtype:'button',
    			anchor: '100%',
    			text:'Save new template',
    			handler: function(){
    				var txt = Ext.getCmp("fldTemplateName").getValue();
    				if (jQuery.trim(txt) != ""){
    					clog("Saving..");
    					Ext.getCmp("gpTemplates").setDisabled(true);
    					t.templateStore.saveTemplate(jQuery.trim(txt),t.xml);
    					t.templateStore.removeAll();
    					t.templateStore.load();
    					t.saveTemplateCallback();
    					t.close();
    				}
    			}
            }
			]
		},{xtype:'claratemplategridpanel',xml:t.xml,region:'center',templateStore:t.templateStore, action:'SAVE',title:'Replace an existing '+templatedesc, parent:t}];
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
		{name:'created',mapping:'created', type: 'date', dateFormat: 'U'},
		{name:'ownerName',mapping:'user.person.fullname'},
		{name:'userId',mapping:'user.id'}
	]),
	prepareXmlString: function(xmlstr){
		// remove template-metadata attribute
		var xml = jQuery.parseXML(xmlstr);
		jQuery(xml).find("*").removeAttr("clara-template-name").removeAttr("clara-template-id");
		xmlstr = XMLObjectToString(xml);
		
		// Strip "<list>" tags..
		xmlstr = xmlstr.replace("<list>","");
		xmlstr = xmlstr.replace("</list>","");
		clog("string xml: ");
		clog(xmlstr);
		return xmlstr;
	},
	replaceTemplate: function(id, name, xmlstr){
		xmlstr = this.prepareXmlString(xmlstr);
		jQuery.ajax({
			async: false,
			url: appContext + "/ajax/protocols/protocol-forms/user-templates/"+id+"/update",
			type: "POST",
			dataType: 'xml',
			data: {
				userId: this.userId,
				xmlData:xmlstr,
				name:name
			},
			success: function(out){
				clog("Success. Data: "+out);
			}
		});
	},
	saveTemplate: function(name, xmlstr){
		xmlstr = this.prepareXmlString(xmlstr);
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
	loadTemplate: function(id){
		var x = "";
		jQuery.ajax({
			async: false,
			url: appContext + "/ajax/protocols/protocol-forms/user-templates/"+id+"/get-xml-data",
			type: "POST",
			dataType: 'xml',
			data: {
				userId: this.userId
			},
			success: function(xml){
				clog("SUCCESSFULLY LOADED CML TEMPLATE",xml);
				x = xml;
			}
		});
		return x;
	},
	removeTemplate: function(id){
		var t =this;
		
		Ext.Msg.show({
			title:"Warning",
			msg:"Are you sure you want to remove this template?",
			buttons: Ext.Msg.YESNOCANCEL,
			fn:function(btn){
				if (btn == "yes"){
					jQuery.ajax({
						async: false,
						url: appContext + "/ajax/protocols/protocol-forms/user-templates/"+id+"/remove",
						type: "POST",
						dataType: 'xml',
						//data: {
						//	userId: this.userId
						//},
						success: function(out){
							clog("Remove complete.");
							t.load();
						}
					});
				}
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