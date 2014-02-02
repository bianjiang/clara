Ext.ns('Clara.ProtocolForm','Clara.NewSubmission');

// Using proxy to get remote php. TODO: incorporate into Clara.
Clara.ProtocolForm.DOServiceUrl = appContext+'/ajax/terminology/list-codes-by-parent-id-and-type.jsontree';

Clara.ProtocolForm.SearchDiseaseStore = new Ext.data.Store({
	header :{
    	'Accept': 'application/json'
	},
	proxy: new Ext.data.HttpProxy({
		url: appContext + "/ajax/terminology/list-codes-by-name-and-type.jsontree",
		method:"GET"
	}),
	reader: new Ext.data.JsonReader({
		idProperty: 'id'
	}, [
		{name:'id'},
		{name:'text', mapping:'text'}
	])
});

Clara.ProtocolForm.ProtocolDiseaseTree = Ext.extend(Ext.tree.TreePanel, {
	studyNature:null,
	parentPanel:null,
	constructor:function(config){		
		Clara.ProtocolForm.ProtocolDiseaseTree.superclass.constructor.call(this, config);
	},	
	initComponent: function() {
		var t = this;
		var root = "disease";//(t.studyNature == "biomedical-clinical")?"disease":"condition";
		var rootId = "ROOT";//(t.studyNature == "biomedical-clinical")?"DOID:4":"ConID:0000000";
		var config = {
				tbar:['->',
				      {id:t.parentPanel.getId()+'_btnAddNodeToList',xtype:'button',iconAlign:'right',
					   iconCls:'icn-arrow',text:'Add to study',disabled:true,
					   handler: function(){
								var xml = '<'+root+' externalid="'+t.parentPanel.selectedDisease.externalid+'" text="'+t.parentPanel.selectedDisease.text+'"/>';
								var diseaseId = addXmlToProtocol( "/"+claraInstance.type+"/"+root+"s/"+root, xml, root);
								Ext.getCmp(t.parentPanel.getId()+'_protocol-disease-savedlist').getStore().removeAll();
								Ext.getCmp(t.parentPanel.getId()+'_protocol-disease-savedlist').getStore().load({params:{listPath:"/"+claraInstance.type+"/"+root+"s/"+root}});
								Ext.getCmp(t.parentPanel.getId()+'_btnAddNodeToList').setDisabled(true);
								t.selectPath("/"+rootId);
						}
				      }],
		        autoScroll:true,
		        useArrows:true,
		        border:false,
	
		        loader: new Ext.tree.TreeLoader({
		            dataUrl:Clara.ProtocolForm.DOServiceUrl,
		            requestMethod:'GET',
		            nodeParameter:'id'
		        }),
		        root:new Ext.tree.AsyncTreeNode({
		            text		: (t.studyNature == "biomedical-clinical")?"Diseases":"Conditions",
		            draggable	: false,
		            id		: rootId  
		        }),
		        listeners:{
					click: function(n){
						// if(n.getPath() != "/"+rootId){ // check if root node
							t.parentPanel.selectedDisease = {
									id:-1,
									externalid:n.attributes.id,
									text:n.attributes.text
							};
							Ext.getCmp(t.parentPanel.getId()+'_btnAddNodeToList').setDisabled(false);
						// }
					}
				}
		};
		Ext.apply(this, Ext.apply(this.initialConfig, config));		
		Clara.ProtocolForm.ProtocolDiseaseTree.superclass.initComponent.apply(this, arguments);
	}
});
Ext.reg('claraprotocoldiseasetree', Clara.ProtocolForm.ProtocolDiseaseTree);

Clara.ProtocolForm.SearchDiseasePanel = Ext.extend(Ext.Panel, {
	border:false,
	studyNature:null,
	parentPanel:null,
	layout:'border',
	trackMouseOver:false,	
	initComponent: function() {
		var t = this;
		if (typeof ignoreFields != "undefined" && ignoreFields) ignoreFields.push(t.parentPanel.getId()+'_search-disease-name');
		var config = {
				items:[{
					id:t.parentPanel.getId()+'_search-disease-name',
					name:t.parentPanel.getId()+'_search-disease-name',
					xtype:'uxsearchfield',
					store:Clara.ProtocolForm.SearchDiseaseStore,
					allowBlank:false,
					region:'north',
					border:false,
					frame:false,
					height:24,
					width:200,
					emptyText:'Search diseases by name',
					paramName : 'name',
					params: {
						codetype:(t.studyNature == "biomedical-clinical")?"DISEASE_ONTOLOGY":"CONDITION"
					}
				},{xtype:'grid',
					id:t.parentPanel.getId()+'_gpSearchDiseases',
					region:'center',
					border:false,
					store:Clara.ProtocolForm.SearchDiseaseStore,
				    columns: [{
				        header: 'ID',
				        width: 150,
				        dataIndex: 'id'
				    },{
				        header: 'Name',
				        width: 300,
				        dataIndex: 'text'
				    }],
				    listeners:{
						rowclick:function(g,idx,e){
							var data = g.getStore().getAt(idx).data;
							t.parentPanel.selectedDisease = {
									externalid:data.id,
									text:data.text
							};
							clog(t.parentPanel.selectedDisease);
							Ext.getCmp(t.parentPanel.getId()+'_btnAddSearchDisease').setDisabled(false);
						}
					}
				}],
				tbar:['->',{id:t.parentPanel.getId()+'_btnAddSearchDisease',xtype:'button',iconCls:'icn-arrow',text:'Add to study',iconAlign:'right',disabled:true, handler:function(){
					clog("adding");
					var xml = '<disease externalid="'+t.parentPanel.selectedDisease.externalid+'" text="'+t.parentPanel.selectedDisease.text+'"/>';
					var diseaseId = addXmlToProtocol( "/protocol/diseases/disease", xml, "disease");
					Ext.getCmp(t.parentPanel.getId()+'_protocol-disease-savedlist').getStore().removeAll();
					Ext.getCmp(t.parentPanel.getId()+'_protocol-disease-savedlist').getStore().load({params:{listPath:'/'+claraInstance.type+'/diseases/disease'}});
					//remove from search results to prevent duplicates
					var gp = Ext.getCmp(t.parentPanel.getId()+'_gpSearchDiseases');
					gp.getStore().removeAt(gp.getStore().indexOfId(t.parentPanel.selectedDisease.externalid));
					Ext.getCmp(t.parentPanel.getId()+'_btnAddSearchDisease').setDisabled(true);
				}}]
				
		};
		Ext.apply(this, Ext.apply(this.initialConfig, config));		
		Clara.ProtocolForm.SearchDiseasePanel.superclass.initComponent.apply(this, arguments);
	}
});
Ext.reg('clarasearchdiseasepanel', Clara.ProtocolForm.SearchDiseasePanel);

Clara.ProtocolForm.ProtocolDiseaseGridPanel = Ext.extend(Ext.grid.GridPanel, {
	parentPanel:null,
	border:true,
	trackMouseOver:false,
	constructor:function(config){		
		Clara.ProtocolForm.ProtocolDiseaseGridPanel.superclass.constructor.call(this, config);
	},	
	protocolDiseaseXMLString:'',
	confirmRemoveDisease: function(disease){
		var t = this;
		Ext.Msg.show({
			title:"WARNING: About to delete a disease/condition",
			msg:"Are you sure you want to delete this disease/condition?", 
			buttons:Ext.Msg.YESNOCANCEL,
			icon:Ext.MessageBox.WARNING,
			fn: function(btn){
				if (btn == 'yes'){
					if (disease){
						url = appContext + "/ajax/"+claraInstance.type+"s/" + claraInstance.id + "/"+claraInstance.type+"-forms/" + claraInstance.form.id + "/"+claraInstance.type+"-form-xml-datas/" + claraInstance.form.xmlDataId + "/xml-elements/delete";

						data = {	
								listPath: "/"+claraInstance.type+"/diseases/disease",
								elementId: disease.id
						};
						
						jQuery.ajax({
							async: false,
							url: url,
							type: "POST",
							dataType: 'xml',
							data: data
						});
						Ext.getCmp(t.parentPanel.getId()+'_protocol-disease-savedlist').getStore().removeAll();
						Ext.getCmp(t.parentPanel.getId()+'_protocol-disease-savedlist').getStore().load({params:{listPath:'/'+claraInstance.type+'/diseases/disease'}});
						t.parentPanel.selectedProtocolDisease = {};
					}
				}
			}
			
		});
		return false;
	},
	confirmRemoveAllDiseases: function(){
		var t =this;
		Ext.Msg.show({
			title:"WARNING: About to delete ALL diseases/conditions",
			msg:"Are you sure you want to delete all diseases/conditions?", 
			buttons:Ext.Msg.YESNOCANCEL,
			icon:Ext.MessageBox.WARNING,
			fn: function(btn){
				if (btn == 'yes'){

						url = appContext + "/ajax/"+claraInstance.type+"s/" + claraInstance.id + "/"+claraInstance.type+"-forms/" + claraInstance.form.id + "/"+claraInstance.type+"-form-xml-datas/" + claraInstance.form.xmlDataId + "/xml-elements/delete-all";

						data = {	
								listPath: "/"+claraInstance.type+"/diseases/disease"
						};
						
						jQuery.ajax({
							async: false,
							url: url,
							type: "POST",
							dataType: 'xml',
							data: data
						});
						Ext.getCmp(t.parentPanel.getId()+'_protocol-disease-savedlist').getStore().removeAll();
						Ext.getCmp(t.parentPanel.getId()+'_protocol-disease-savedlist').getStore().load({params:{listPath:'/'+claraInstance.type+'/diseases/disease'}});
						t.parentPanel.selectedProtocolDisease = {};
				}
			}
			
		});
		return false;
	},
	initComponent: function() {
		var t = this;
		var config = {
				id: t.parentPanel.getId()+'_protocol-disease-savedlist',
				store:new Ext.data.XmlStore({
					proxy: new Ext.data.HttpProxy({
						url: appContext + "/ajax/"+claraInstance.type+"s/" + claraInstance.id + "/"+claraInstance.type+"-forms/" + claraInstance.form.id + "/"+claraInstance.type+"-form-xml-datas/" + claraInstance.form.xmlDataId + "/xml-elements/list",
						method:"GET",
						headers:{'Accept':'application/xml;charset=UTF-8'}
					}),
					baseParams:{'listPath':'/protocol/diseases/disease'},
					autoLoad:true,
					record: 'disease', 
					listeners:{
						load: function(s){
							clog(s);
							t.protocolDiseaseXMLString = XMLObjectToString(s.reader.xmlData);
						}
					},
					fields: [
						{name:'id', mapping:'@id'},
						{name:'externalid',mapping:'@externalid'},
						{name:'text', mapping:'@text'},
						{name:'claratemplatename', mapping:'@clara-template-name'},
						{name:'claratemplateid', mapping:'@clara-template-id'}
					]
				}),

			    emptyText: '<div style="padding:16px;width:100%;text-align:center;">No diseases/conditions added to this study.</div>',
				tbar:[{id:t.parentPanel.getId()+'_btnRemoveDisease',xtype:'button',iconCls:'icn-minus-button',text:'Remove',disabled:true, handler:function(){
					t.confirmRemoveDisease(t.parentPanel.selectedProtocolDisease);
					
					Ext.getCmp(t.parentPanel.getId()+'_btnRemoveDisease').setDisabled(true);
				}},{id:t.parentPanel.getId()+'_btnRemoveAllDiseases',xtype:'button',iconCls:'icn-minus-button',text:'Remove All', handler:function(){
					t.confirmRemoveAllDiseases();
				}},{
					    	text: 'Templates',
					    	iconCls:'icn-wrench',
					    	menu:[{
				    			iconCls:'icn-script-import',
				    			text:'Load template..',
								handler:function(){
						    		new Clara.TemplateLoadWindow({
						    			templateStore:Clara.NewSubmission.DiseaseOntologyTemplateStore,
						    			loadTemplateCallback: function(xmlobj,templateId){
						    				var template = Clara.NewSubmission.DiseaseOntologyTemplateStore.getById(templateId);
						    				clog("loadTemplateCallback(xmlobj,template)",xmlobj,template);
						    				// add template metadata to disease list (to show which template was used)
						    				jQuery(xmlobj).find('disease').each(function(){
						    					jQuery(this).attr("clara-template-name",template.get("name"));
						    					jQuery(this).attr("clara-template-id",template.get("id"));
						    				});
						    				
						    				// merge existing rows to the output of the template load.
						    				t.getStore().each(function(r){
						    					var existingDiseaseXml = '<disease externalid="'+r.get("externalid")+'" id="'+r.get("id")+'" text="'+r.get("text")+'" clara-template-name="'+r.get("claratemplatename")+'" clara-template-id="'+r.get("claratemplateid")+'"/>';
						    					jQuery(xmlobj).find('diseases').append(jQuery(existingDiseaseXml));
						    				});
						    				
						    				var xml = XMLObjectToString(xmlobj);
						    				
						    				
						    				clog("Window: loadTemplateCallback XML",xml);

						    				var url = appContext + "/ajax/"+claraInstance.type+"s/" + claraInstance.id + "/"+claraInstance.type+"-forms/" + claraInstance.form.id + "/"+claraInstance.type+"-form-xml-datas/" + claraInstance.form.xmlDataId + "/update";
						    				var data = {
						    						pagefragment: "<"+claraInstance.type+">"+xml+"</"+claraInstance.type+">"
						    					};

						    				 jQuery.ajax({
						    					async: false,
						    					url: url,
						    					type: "POST",
						    					dataType: 'text',
						    					data: data,
						    					success: function(d){
						    						clog(d);
						    						clog("RELOADING AFTER TEMPLATE LOAD..");
						    						t.getStore().removeAll();
						    						t.getStore().load();
						    					}
						    				}); 
						    			
						    			}
						    		}).show();
								}
				    		},{
				    			iconCls:'icn-script-export',
				    			text:'Save template..',
								handler:function(){
				    				new Clara.TemplateSaveWindow({templateStore:Clara.NewSubmission.DiseaseOntologyTemplateStore, xml:"<diseases>" + t.protocolDiseaseXMLString + "</diseases>"}).show();
								}
				    		}]
					    	
					    }],
			    columns: [{
			        header: 'ID',
			        width: 150,
			        dataIndex: 'externalid'
			    },{
			        header: 'Name',
			        width: 450,
			        dataIndex: 'text',
			        renderer: function(v,p,r){
			        	var html = "<span style='font-weight:800;'>";
			        	if (r.get("claratemplatename") && r.get("claratemplatename") != "") html += "<span style='font-weight:100;color:#666;'>"+r.get("claratemplatename")+": </span>";
			        	html += (v+"</span>");
			        	return html;
			        }
			    }],
			    listeners:{
			    	afterrender:function(g){
			    		clog("afterrender GRIDPANEL!");
			    		g.doLayout();
			    	},
					rowclick:function(g,idx,e){
						var data = g.getStore().getAt(idx).data;
						t.parentPanel.selectedProtocolDisease = {
								id:data.id,
								doid:data.doid,
								text:data.text
						};
						Ext.getCmp(t.parentPanel.getId()+'_btnRemoveDisease').setDisabled(false);
					}
				}
		};
		Ext.apply(this, Ext.apply(this.initialConfig, config));		
		Clara.ProtocolForm.ProtocolDiseaseGridPanel.superclass.initComponent.apply(this, arguments);
		t.getStore().load();
		
	}
});
Ext.reg('claraprotocoldiseasegridpanel', Clara.ProtocolForm.ProtocolDiseaseGridPanel);

Clara.ProtocolForm.ProtocolDiseasePanel = Ext.extend(Ext.Panel, {
	trackMouseOver:false,
	studyNature:null,
	border:true,
	selectedDisease: {},
	selectedProtocolDisease:{},
	initComponent: function() {
		var t = this;
		if (Clara.ProtocolForm.DOServiceUrl.search("codetype=") < 0) {
			Clara.ProtocolForm.DOServiceUrl += "?codetype="+((t.studyNature == "biomedical-clinical")?"DISEASE_ONTOLOGY":"CONDITION");
		}
		var config = {
				layout:'border',
				items:[{xtype:'claraprotocoldiseasegridpanel',region:'center',split:true,title:'Diseases associated with this study', parentPanel:t, id:t.getId()+'_protocol-disease-savedlist'},
				       {
							region:'west',
							layout:'fit',
							border:false,
							split:true,
							width:450,
							items:[{
									xtype:'tabpanel',
									border:false,
									activeTab:1,
									items:[{xtype:'claraprotocoldiseasetree',title:'Browse',studyNature:t.studyNature, parentPanel:t},{xtype:'clarasearchdiseasepanel',title:'Search',studyNature:t.studyNature, parentPanel:t}]}]
				       	}
				]
		};
		Ext.apply(this, Ext.apply(this.initialConfig, config));		
		Clara.ProtocolForm.ProtocolDiseasePanel.superclass.initComponent.apply(this, arguments);
		t.doLayout();
	}
});
Ext.reg('claraprotocoldiseasepanel', Clara.ProtocolForm.ProtocolDiseasePanel);


