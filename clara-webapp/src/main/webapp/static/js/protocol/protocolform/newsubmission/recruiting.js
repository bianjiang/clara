Ext.ns('Clara.NewSubmission');

if (!Clara.NewSubmission.MessageBus) Clara.NewSubmission.MessageBus = new Ext.util.Observable();
Clara.NewSubmission.MessageBus.addEvents('criteriaupdated');

Clara.NewSubmission.InclusionCriteriaStore = new Ext.data.Store({
	id:'icStore',
	proxy: new Ext.data.HttpProxy({
		url: appContext + "/ajax/protocols/" + claraInstance.id + "/protocol-forms/" + claraInstance.form.id + "/protocol-form-xml-datas/" + claraInstance.form.xmlDataId + "/xml-elements/list",
		method:"GET",
		headers:{'Accept':'application/xml;charset=UTF-8'}
	}),
	reader: new Ext.data.XmlReader({
	record: 'inclusion-criteria', 
	fields: [
		{name:'id', mapping:'@id'},
		{name:'index', mapping:'@index'},
		{name:'value', mapping:'value'}
	]})
});

Clara.NewSubmission.ExclusionCriteriaStore = new Ext.data.Store({
	id:'ecStore',
	proxy: new Ext.data.HttpProxy({
		url: appContext + "/ajax/protocols/" + claraInstance.id + "/protocol-forms/" + claraInstance.form.id + "/protocol-form-xml-datas/" + claraInstance.form.xmlDataId + "/xml-elements/list",
		method:"GET",
		headers:{'Accept':'application/xml;charset=UTF-8'}
	}),
	reader: new Ext.data.XmlReader({
	record: 'exclusion-criteria', 
	fields: [
		{name:'id', mapping:'@id'},
		{name:'index', mapping:'@index'},
		{name:'value', mapping:'value'}
	]})
});

Clara.NewSubmission.ReorderCriteria = function(){
	clog("Clara.NewSubmissions.ReorderCriteria");
	var cr = [];
	var crxml = "<recruiting-criteria>";
	var idx = 0;
	var ist = Ext.getCmp("gpInclusionList").getStore();
	//ist.sort('index');
	ist.each(function(rec){
		idx++;
		crxml += '<inclusion-criteria id="'+rec.get("id")+'" index="'+idx+'"><value>'+Encoder.htmlEncode(rec.get("value"))+'</value></inclusion-criteria>';
	});
	
	var est = Ext.getCmp("gpExclusionList").getStore();
	//est.sort('index');
	est.each(function(rec){
		idx++;
		crxml += '<exclusion-criteria id="'+rec.get("id")+'" index="'+idx+'"><value>'+Encoder.htmlEncode(rec.get("value"))+'</value></exclusion-criteria>';
	});
	crxml += "</recruiting-criteria>";
	var crID = updateProtocolXml(crxml);
	Clara.NewSubmission.ReloadCriteria();
};

Clara.NewSubmission.ReloadCriteria = function(){

	var st = Clara.NewSubmission.InclusionCriteriaStore;
	st.rejectChanges();	// to clear local record add?
	st.removeAll();
	st.load({params:{listPath:'/protocol/recruiting-criteria/inclusion-criteria'}});
	
	var est = Clara.NewSubmission.ExclusionCriteriaStore;
	est.rejectChanges();	// to clear local record add?
	est.removeAll();
	est.load({params:{listPath:'/protocol/recruiting-criteria/exclusion-criteria'}});
	
	// Ext.getCmp("protocol-criteria-gridpanel").loadCriteria();		// BAAAD
};

Clara.NewSubmission.ConfirmRemoveCriteria = function(criteriaType,crit){
	Ext.Msg.show({
		title:"WARNING: About to delete a criteria",
		msg:"Are you sure you want to delete this "+criteriaType+" criteria?", 
		buttons:Ext.Msg.YESNOCANCEL,
		icon:Ext.MessageBox.WARNING,
		fn: function(btn){
			if (btn == 'yes'){
				if (crit){
					url = appContext + "/ajax/protocols/" + claraInstance.id + "/protocol-forms/" + claraInstance.form.id + "/protocol-form-xml-datas/" + claraInstance.form.xmlDataId + "/xml-elements/delete";

					data = {	
							listPath: "/protocol/recruiting-criteria/"+criteriaType+"-criteria",
							elementId: crit.get("id")
					};
					clog(data);
					jQuery.ajax({
						async: false,
						url: url,
						type: "POST",
						dataType: 'xml',
						data: data
					});
					Clara.NewSubmission.ReloadCriteria();
				}
			}
		}
		
	});
	return false;
};

Clara.NewSubmission.CriteriaWindow = Ext.extend(Ext.Window, {
	title: 'Add Criteria',
	criteriaType:'INCLUSION',
    width: 502,
    height: 262,
    layout: 'fit',
    border:false,
    itemId: 'winCriteria',
    modal: true,
    id: 'winCriteria',
    initComponent: function() {
    	var t = this;
		this.buttons = [
		    {
		    	id: 'btnAdd',
		        text: 'Save',
		        handler: function(){
		        	var crText = Encoder.htmlEncode(Ext.getCmp("fldText").getValue());
		        	var crXML = '<criteria type="'+t.criteriaType+'"><value>'+crText+'</value></criteria>';
		        	var crID = addXmlToProtocol( "/protocol/recruiting-criteria/criteria", crXML, "criteria");
		        	Clara.NewSubmission.ReloadCriteria();
		        	t.close();
		        }
		    }
		];
        this.items = [
        {xtype:"textarea",id:"fldText"}];
        this.title = (this.criteriaType == "INCLUSION")?"Enter the Inclusion Criteria below..":"Enter the Exclusion Criteria below..";
        Clara.NewSubmission.CriteriaWindow.superclass.initComponent.call(this);
    }
});



Clara.NewSubmission.SubjectCriteriaPanel = Ext.extend(Ext.TabPanel, {
	id: 'protocol-criteria-panel',
	border:true,
	selectedCriteria:{},

	constructor:function(config){		
		Clara.NewSubmission.SubjectCriteriaPanel.superclass.constructor.call(this, config);
	},	
	
	initComponent: function() {
		var t = this;

		var config = {

				items:[{
					xtype:'panel',
					title:'Inclusion Criteria',
					iconCls:'icn-thumb-up',
					layout:'border',
					items:[{
						region:'north',
						xtype:'panel',
						layout:'hbox',
						border:false,
						height:80,
						padding:6,
						items:[
						       {
						    	   xtype:'textarea',
						    	   hideLabel:true,
						    	   flex:6,
						    	   id:'fldIncCriteriaText'
						       },{
						    	   xtype:'button',
						    	   text:'Add', 
						    	   flex:1,
						    	   iconCls:'icn-plus',
						    	   handler:function(){
						    		   var gp = Ext.getCmp("gpInclusionList");
						    		   var ctype = "inclusion";
						    			   var cvalue = Ext.getCmp("fldIncCriteriaText").getValue();
						    		   var cindex = parseInt(gp.getMaxIndex())+1;
						    		   clog("adding",ctype,cvalue);
						    		   var crText = Encoder.htmlEncode(cvalue);
						    		   var crXML = '<inclusion-criteria index="'+cindex+'"><value>'+crText+'</value></inclusion-criteria>';
						    		   var crID = addXmlToProtocol( "/protocol/recruiting-criteria/inclusion-criteria", crXML, "inclusion-criteria");
						    		   Clara.NewSubmission.ReloadCriteria();
						    		   Ext.getCmp("fldIncCriteriaText").setValue("");
						    	   }
						       }]
					},{			
						xtype:'claraprotocolcriteriagridpanel',
						region:'center',
						id:'gpInclusionList',
						store:Clara.NewSubmission.InclusionCriteriaStore,
						filterType:'inclusion'
				
					}
					]
				},{
					xtype:'panel',
					title:'Exclusion Criteria',
					iconCls:'icn-thumb',
					layout:'border',
					items:[{
						region:'north',
						xtype:'panel',
						layout:'hbox',
						border:false,
						height:80,
						padding:6,
						items:[
						       {
						    	   xtype:'textarea',
						    	   hideLabel:true,
						    	   flex:6,
						    	   id:'fldExcCriteriaText'
						       },{
						    	   xtype:'button',
						    	   text:'Add', 
						    	   flex:1,
						    	   iconCls:'icn-plus',
						    	   handler:function(){
						    		   var gp = Ext.getCmp("gpExclusionList");
						    		   var ctype = "exclusion";
						    		   var cvalue = Ext.getCmp("fldExcCriteriaText").getValue();
						    		   var cindex = parseInt(gp.getMaxIndex())+1;
						    		   clog("adding",ctype,cvalue);
						    		   var crText = Encoder.htmlEncode(cvalue);
						    		   var crXML = '<exclusion-criteria index="'+cindex+'"><value>'+crText+'</value></exclusion-criteria>';
						    		   var crID = addXmlToProtocol( "/protocol/recruiting-criteria/exclusion-criteria", crXML, "exclusion-criteria");
						    		   Clara.NewSubmission.ReloadCriteria();
						    		   Ext.getCmp("fldExcCriteriaText").setValue("");
						    	   }
						       }]
					},{			
						xtype:'claraprotocolcriteriagridpanel',
						region:'center',
						store:Clara.NewSubmission.ExclusionCriteriaStore,
						id:'gpExclusionList',
						filterType:'exclusion'
				
					}
					]
				}
				]
		};
		
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		Clara.NewSubmission.SubjectCriteriaPanel.superclass.initComponent.apply(this, arguments);
		
	}
	

});
Ext.reg('claraprotocolcriteriapanel', Clara.NewSubmission.SubjectCriteriaPanel);


Clara.NewSubmission.SubjectGridCriteriaPanel = Ext.extend(Ext.grid.GridPanel, {
	border:false,
	selectedCriteria:{},
	filterType:"inclusion",
	constructor:function(config){		
		Clara.NewSubmission.SubjectGridCriteriaPanel.superclass.constructor.call(this, config);
		if(Clara.NewSubmission.MessageBus){
			Clara.NewSubmission.MessageBus.on('criteriaupdated', this.loadCriteria, this);
		}
	}, 
	
	getMaxIndex:function(){
		var st = this.getStore();
		var maxidx = 0;
		st.each(function(rec){
			maxidx = (rec.get("index") && parseFloat(rec.get("index")) > maxidx)?parseFloat(rec.get("index")):maxidx;
		});
		return maxidx;
	},
	
	loadCriteria:function(){
		clog("loadCriteria called");
	},

	initComponent: function() {
		var t = this;
		
		var config = {
				tbar:['->',
				      {
					      text:'Remove',
					      id:'btnRemove-'+t.filterType,
					      iconCls:'icn-minus-button',
					      disabled:true,
					      handler:function(){
					    	  Clara.NewSubmission.ConfirmRemoveCriteria(t.filterType,t.selectedCriteria);
					      }
				      }
				],
				hideHeaders:true,
				trackMouseOver:false,
				sm: new Ext.grid.RowSelectionModel({singleSelect: true}),
		        loadMask: new Ext.LoadMask(Ext.getBody(), {msg:"Please wait..."}),
		        viewConfig: {
		        	forceFit:true
		        },
		    	plugins: [new Ext.ux.dd.GridDragDropRowOrder(
		    		    {
		    		        copy: false, // false by default
		    		        scrollable: true, // enable scrolling support (default is false)
		    		        ddGroup:'criteria-dd',
		    		        targetCfg: { 
		    				    notifyDrop:function(dd,e,data){
		    		    			var grid = t;
		    		    			var ds = grid.store;
		    		    			var sm = grid.getSelectionModel();
		    		                var rows = sm.getSelections();
		    		                if(dd.getDragData(e)) {
		    		                    var cindex=dd.getDragData(e).rowIndex;
		    		                    if(typeof(cindex) != "undefined") {
		    		                        for(var i = 0; i <  rows.length; i++) {
		    		                        ds.remove(ds.getById(rows[i].id));
		    		                        }
		    		                        ds.insert(cindex,data.selections);
		    		                        sm.clearSelections();
		    		                     }
		    		                    grid.getView().refresh(false);
		    		                    Clara.NewSubmission.ReorderCriteria();
		    		                 }
		    		    		}
		    		    	} // any properties to apply to the actual DropTarget
		    		    })],
		        columns: [
		                  new Ext.grid.RowNumberer(),
		                  {
		                	  	header:'Criteria',
		                	  	dataIndex:'value',
		                	  	sortable:false
		                  }
		        ],
		        
			    listeners:{
				    rowclick: function(grid, rowI, event)   {
						var criteria = grid.getStore().getAt(rowI);
						clog(criteria);
						t.selectedCriteria = criteria;
						Ext.getCmp('btnRemove-'+t.filterType).setDisabled(false);
				    }
			}
		};
		
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		Clara.NewSubmission.SubjectGridCriteriaPanel.superclass.initComponent.apply(this, arguments);
		
	}
	

});
Ext.reg('claraprotocolcriteriagridpanel', Clara.NewSubmission.SubjectGridCriteriaPanel);