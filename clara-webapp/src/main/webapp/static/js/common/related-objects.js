Ext.ns('Clara','Clara.ContractDashboard','Clara.Contracts','Clara.ProtocolDashboard','Clara.Protocols','Clara.RelatedObjects');

Clara.RelatedObjects.AddRelatedProtocolWindow = Ext.extend(Ext.Window,{
	constructor:function(config){		
		Clara.RelatedObjects.AddRelatedContractWindow.superclass.constructor.call(this, config);
	},
	width:600,
	height:400,
	title:'Choose a protocol',
	modal:true,
	layout:'border',
	listStore:Clara.Protocols.ProtocolListStore,
	relatedToObject:{},
	relatedFromObject:{},
	onAddSuccess:{},
	initComponent:function(){
			var t = this;
			if (typeof t.listStore == "undefined"){
				alert("Error. See console.");
				cerr("Clara.Protocols.ProtocolListStore not defined. Did you load 'protocol-base.js' in this page's views.xml?");
			}
		
			var config = {
				items:[{
			    	xtype:'uxsearchfield',
			    	store:t.listStore,
			    	emptyText:'Search title or IRB Number',
			    	paramName : 'keyword',
			    	region:'north',
			    	reloadAllAsClear:true
			    },{xtype:'claraprotocollistpanel', region:'center', split:true, clickableTitles:false}],
				buttons:[{
					text:'Add',
					disabled:true,
					id:'btnAddRelatedProtocol',
					handler:function(){
						clog(Clara.Protocols.SelectedProtocol);
						if (t.relatedFromObject.type == "protocol" && Clara.Protocols.SelectedProtocol.get("protocolId") == ""+t.relatedFromObject.id){
		    		    	   alert("You cannot relate a protocol to itself.");
		    		       } else {
		    		    		Ext.Ajax
		    					.request({
		    						url : appContext+"/ajax/add-related-object",
		    						method : 'POST',
		    						success : function() {
		    							t.onAddSuccess();
		    							t.close();
		    						},
		    						failure : function(response) {
		    							 alert("Error adding related protocol.");
		    						},
		    						params : {
		    							objectType: t.relatedFromObject.type,
		    							objectId:t.relatedFromObject.id, 
		    							relatedObjectId:Clara.Protocols.SelectedProtocol.get("protocolId"),
		    							relatedObjectType:'protocol',
		    							userId:claraInstance.user.id
		    						}
		    					});
		    		       }
		    		    
					}
				}]
			};
			Ext.apply(this, Ext.apply(this.initialConfig, config));
			Clara.RelatedObjects.AddRelatedContractWindow.superclass.initComponent.apply(this, arguments);
	}
});

Clara.RelatedObjects.AddRelatedContractWindow = Ext.extend(Ext.Window,{
	id:'winAddRelatedContract',
	constructor:function(config){		
		Clara.RelatedObjects.AddRelatedContractWindow.superclass.constructor.call(this, config);
	},
	width:600,
	height:400,
	title:'Choose a contract',
	modal:true,
	layout:'border',
	listStore:Clara.Contracts.ContractListStore,
	relatedToObject:{},
	relatedFromObject:{},
	onAddSuccess:{},
	initComponent:function(){
			var t = this;
			if (typeof t.listStore == "undefined"){
				alert("Error. See console.");
				cerr("Clara.Contracts.ContractListStore not defined. Did you load 'contract-base.js' in this page's views.xml?");
			}
		
			var config = {
				items:[{
			    	xtype:'uxsearchfield',
			    	store:t.listStore,
			    	emptyText:'Search Contract Number, Company/Entity Name, PI Name or IRB#',
			    	paramName : 'keyword',
			    	region:'north',
			    	reloadAllAsClear:true
			    },{xtype:'claracontractlistpanel', region:'center', split:true, clickableTitles:false}],
				buttons:[{
					text:'Add',
					disabled:true,
					id:'btnAddRelatedContract',
					handler:function(){
						clog(Clara.Contracts.SelectedContract);
						if (t.relatedFromObject.type == "contract" && Clara.Contracts.SelectedContract.get("contractId") == ""+t.relatedFromObject.id){
		    		    	   alert("You cannot relate a contract to itself.");
		    		       } else {
		    		    		Ext.Ajax
		    					.request({
		    						url : appContext+"/ajax/add-related-object",
		    						method : 'POST',
		    						success : function() {
		    							t.onAddSuccess();
		    							t.close();
		    						},
		    						failure : function(response) {
		    							 alert("Error adding related contract.");
		    						},
		    						params : {
		    							objectType: t.relatedFromObject.type,
		    							objectId:t.relatedFromObject.id, 
		    							relatedObjectId:Clara.Contracts.SelectedContract.get("contractId"),
		    							relatedObjectType:'contract',
		    							userId:claraInstance.user.id
		    						}
		    					});
		    		       }
		    		    
					}
				}]
			};
			Ext.apply(this, Ext.apply(this.initialConfig, config));
			Clara.RelatedObjects.AddRelatedContractWindow.superclass.initComponent.apply(this, arguments);
	}
});

Clara.RelatedObjects.ListPanel = Ext.extend(Ext.grid.GridPanel, {
	height:350,
	iconCls:'icn-sitemap-application-blue',
	bodyStyle:'border-bottom: 1px solid #8DB2E3;',
	constructor:function(config){		
		Clara.RelatedObjects.ListPanel.superclass.constructor.call(this, config);
	},	
	relatedObject:{},
	selectedRelatedObject:{},
	addNewHandler:{},
	initComponent: function(){
		var t = this;
		var title = "Related "+t.relatedObject.type+"s";
		var winOptions = { relatedFromObject: { type:claraInstance.type, id:claraInstance.id }, onAddSuccess:function(){ Ext.getCmp(t.id).getStore().load(); } };
		
		if (t.relatedObject.type == "protocol"){
			t.addHandler = function(){
				 new Clara.RelatedObjects.AddRelatedProtocolWindow(winOptions).show();
			};
		}else if (t.relatedObject.type == "contract"){
			t.addHandler = function(){
				 new Clara.RelatedObjects.AddRelatedContractWindow(winOptions).show();
			};
		}
		var config = {
				title:title,
				tbar:{
					xtype:'toolbar',
					items:[
					       {iconCls:'icn-application--plus',text:'Add related '+t.relatedObject.type+'..',iconAlign:'top',handler:function(){t.addHandler();}},
					       {iconCls:'icn-application--minus',disabled:true,id:'btnRemoveRelated'+t.relatedObject.type,text:'Remove related '+t.relatedObject.type+'..',iconAlign:'top',handler:function(){
					    	   Ext.Msg.show({
					    		   title:'Remove related '+t.relatedObject.type+'?',
					    		   msg: 'Are you sure? This will NOT delete the related '+t.relatedObject.type+' from Clara.',
					    		   buttons: Ext.Msg.YESNOCANCEL,
					    		   fn: function(buttonId){
					    			   if (buttonId == 'yes'){
					    				   Ext.Ajax
					    					.request({
					    						url : appContext+"/ajax/delete-related-object",
					    						method : 'POST',
					    						success : function() {
					    							// reload 
					    		    				  t.getStore().load();
					    		    				  Ext.getCmp('btnRemoveRelated'+t.relatedObject.type).setDisabled(true);
					    		    				  t.selectedRelatedObject = {};
					    						},
					    						params : {
					    							objectId: claraInstance.id,
					    							objectType:claraInstance.type,
					    							relatedObjectType:t.relatedObject.type,
					    							relatedObjectId: t.selectedRelatedObject.get("id"), 
					    							userId:claraInstance.user.id
					    							}
					    					});
					    			   }
					    		   },
					    		   animEl: 'elId',
					    		   icon: Ext.MessageBox.QUESTION
					    		});
					       }}
			           	]},
				constructor:function(config){		
					Clara.RelatedObjects.ListPanel.superclass.constructor.call(this, config);
				},
				store: new Ext.data.XmlStore({
					scope:this,
					
					proxy: new Ext.data.HttpProxy({
						url: appContext + "/ajax/"+claraInstance.type+"s/related-"+t.relatedObject.type+"/list.xml?"+claraInstance.type+"Id="+claraInstance.id,
						method:"GET",
						headers:{'Accept':'application/xml;charset=UTF-8'}
					}),
					record: t.relatedObject.type, 
					autoLoad:false,
					root:'list',
					fields: [
					    {name:'id', mapping: '@id'},
						{name:'status', mapping:'status'},
						{name:'actions',convert:function(v,node){ return new Ext.data.XmlReader({record: 'action',fields: [{name:'type'},{name:'name'},{name:'url'}]}).readRecords(node).records; }}
					]
				}),
		        viewConfig: {
		    		forceFit:true,
		    		loadMask:true	// TODO: This isnt displaying, probably because we're deferring store loading until the tab is opened. FIX.
		    	},
		        columns: [
		            {header: 'Form Type', width: 270, sortable: true, dataIndex: 'formtype', renderer:function(value, p, record){
		        		var url=record.data.url;
		        		var formType = record.data.formtype;
		        		return "<span class='"+t.relatedObject.type+"-form-row-field "+t.relatedObject.type+"-form-type'>"+Ext.util.Format.capitalize(t.relatedObject.type)+" ID# "+record.get("id")+"</span>";
		            }},
		            {header: 'Last Modified', width: 95, sortable: true, renderer: function(value) { return "<span class='contract-form-row-field'>"+Ext.util.Format.date(value,'m/d/Y')+"</span>";}, dataIndex: 'statusModified'},
		            {header: 'Status', id:t.relatedObject.type+'-forms-status-column', width: 215, sortable: true, dataIndex: 'status', renderer:function(v,p,record){return "<span class='"+t.relatedObject.type+"-form-row-field "+t.relatedObject.type+"-form-status'>"+record.data.status+"</span>";}},
		            {header: 'Actions', id:t.relatedObject.type+'-forms-actions-column', width: 315, sortable: true, dataIndex: 'actions', 
		            	renderer:function(v,p,record){
		            		return '&nbsp;<a style="text-decoration:underline;" href="'+appContext+'/'+t.relatedObject.type+'s/' + record.get("id") + '/dashboard">Open</a>';
		            	}
		            }
		        ],
				listeners: {
		    		show: function(g){
		    			if (g.getStore().getCount() == 0) g.getStore().load();
		    		},
				    rowclick: function(grid, rowI, event)   {
						var record = grid.getStore().getAt(rowI);
						clog(record);
						t.selectedRelatedObject = record;
						Ext.getCmp('btnRemoveRelated'+t.relatedObject.type).setDisabled(false);
				    }
				}
		};
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		Clara.RelatedObjects.ListPanel.superclass.initComponent.apply(this, arguments);
	}
});
Ext.reg('clararelatedobjectlistpanel', Clara.RelatedObjects.ListPanel);