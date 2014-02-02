Ext.ns('Clara.ProtocolForms');

Clara.ProtocolForms.RenderFormCommitteeStatusRow = function(r,showActions){
	var row = "";
	var committeeHtml = (r.get("committee"))?("<span style='font-weight:100;'>"+r.get("committee")+" <span style='color:#6c6680'>&gt;</span></span> "):"";
	var status = (r.get("status") || "Unknown status");
	var ccStatus = r.get("priority").toLowerCase();
	row = "<div class='form-committeestatus-row'>";
	row +="<div class='form-committeestatus-date'>"+Ext.util.Format.date(r.get("modified"),'m/d/Y h:ia')+"</div>";
	row += "<div class='protocol-metadata'><div class='protocol-status protocol-status-"+ccStatus+"'>"+committeeHtml+status+"</div><div class='protocol-type'></div></div>";
	
	if (showActions){
		var a = r.get("actions");
		if (a.length > 0){
			row += "<ul class='form-committeestatus-actions'>";
			for(var i=0;i<a.length;i++){
				row += "<li><a href=\""+a[i].get("url")+"\">"+a[i].get("name")+"</a></li>";
			}
			row += "</ul>";
		}
	}
	
	return row + "</div>";
};


Clara.ProtocolForms.FormStatusGridPanel = Ext.extend(Ext.grid.GridPanel, {
	id: 'clara-forms-formstatusgridpanel',
	bodyStyle:'border-top: 1px solid #8DB2E3;',
	height:200,
	autoload: false,
	hideHeaders:true,
	disabled:true,
	showActions:true,
	formId:0,
	resetUrl: function(){
		var t = this;
		t.store.proxy.setApi(Ext.data.Api.actions.read, appContext+"/ajax/"+claraInstance.type+"s/"+claraInstance.id+"/"+claraInstance.type+"-forms/"+t.formId+"/review/committee-statuses/list.xml");
	},
	constructor:function(config){
		Clara.ProtocolForms.FormStatusGridPanel.superclass.constructor.call(this, config);
	},	
	
	initComponent: function(){
		
		var t = this;
		var formType = "";
		clog("formstatus formdata",t.formXmlData);

		var formMappingName = (claraInstance.type == "protocol")?"@protocolFormId":"@contractFormId";
		
		var config = {
				store: new Ext.data.Store({
					scope:this,
					//groupField:'parentCommittee',
					proxy: new Ext.data.HttpProxy({
						scope:this,
						url: appContext+"/ajax/"+claraInstance.type+"s/"+claraInstance.id+"/"+claraInstance.type+"-forms/"+t.formId+"/review/committee-statuses/list.xml",
						method: 'GET',
						headers: {'Accept':'application/xml'}				
					}),
					sortInfo: {
						field:'modified',
						direction: 'DESC'
					},
					reader: new Ext.data.XmlReader({
						root: "list",
						record: claraInstance.type+"-form-committee-status"
					},[
					    {name:'protocolFormCommitteeStatusId', mapping: '@id'},
					    {name:'protocolFormId', mapping: formMappingName},
					    {name:'committee', mapping: 'committee'},
					    {name:'parentCommittee', mapping: 'parent_committee_code'},
					    {name:'committee_code', mapping: 'committee_code'},
					    {name:'status', mapping: 'status'},
					    {name:'priority', mapping: 'status@priority'},
					    {name:'modified', mapping:'modified'},
					    {name:'actions',convert:function(v,node){
							return new Ext.data.XmlReader({
								record: 'action',
								fields: [{name:'type'},
								         {name:'name'},
								         {name:'url'}
								         ]
							}).readRecords(node).records; 
						}}
					]),
					autoLoad: false
				}),
		        loadMask: new Ext.LoadMask(Ext.getBody(), {msg:"Please wait..."}),        
		        view: new Ext.grid.GridView( {
		    		forceFit:true,
		    		//hideGroupedColumn:true,
		    		//showGroupName:false,
		    		emptyText:'No committee statuses found for this form.',
		    		//groupTextTpl: '{text} ({[values.rs.length]} {[values.rs.length > 1 ? "Items" : "Item"]})'
		    	}),
		    	listeners:{
		    		rowdblclick: function(grid, rowI, event)   {
						var record = grid.getStore().getAt(rowI);
						new Clara.ProtocolForms.StatusHistoryWindow({formId:t.formId, title:'Form status history: '+record.data.committee, committee:record.data.committee_code}).show();
		    		}
		    	},
		        columns: [
						{
							header : 'Committee',
							dataIndex : 'committee',
							sortable : true,
							align : 'left',
							//width : 300,
							renderer:function(v,p,r) { return Clara.ProtocolForms.RenderFormCommitteeStatusRow(r,t.showActions); }
						}
						
		        ]
		};
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		Clara.ProtocolForms.FormStatusGridPanel.superclass.initComponent.apply(this, arguments);
		
		if (t.autoload){
			t.enable();
			t.resetUrl();
			t.getStore().load();
		}
	}
});
Ext.reg('claraformstatusgridpanel', Clara.ProtocolForms.FormStatusGridPanel);


Clara.ProtocolForms.StatusHistoryWindow = Ext.extend(Ext.Window, {
	title: 'History',
    width: 500,
    height: 200,
    layout: 'fit',
    modal: true,
    committee:'',
    parentId:'',
    formId:0,
    id: 'winStatusHistory',
    initComponent: function() {
		var t = this;

		t.buttons = [
		    {
		        text: 'Close',
		        handler: function(){
					Ext.getCmp('winStatusHistory').close();
		        }
		    }
		];
        t.items = [{
        	xtype:'grid',
        	border:false,
        	id:'gpstatushistory',
        	loadMask:true,
			store: new Ext.data.Store({
				autoLoad:true,
				header :{
			  'Accept': 'application/json'
			  },
				proxy: new Ext.data.HttpProxy({
					url: appContext+"/ajax/"+claraInstance.type+"s/"+claraInstance.id+"/"+claraInstance.type+"-forms/"+t.formId+"/review/committee-statuses/"+t.committee+"/list",
					method:'GET'
				}),
				reader: new Ext.data.JsonReader({
					idProperty: 'id'
				}, [
					{name:'protocolFormCommitteeStatus', mapping:claraInstance.type+'FormCommitteeStatus'},
					{name:'note'},
					{name:'modifiedDateTime', type:'date', dateFormat:'m/d/Y g:i:s'}
				])
			}),
			colModel: new Ext.grid.ColumnModel({
		        defaults: {
		            width: 120,
		            sortable: true
		        },
		        columns: [
				            {
				                header: 'Date', width: 135, dataIndex: 'modifiedDateTime',
				                xtype: 'datecolumn', format: 'm/d/Y h:i'
				            },
				            {header: 'Status', dataIndex: 'protocolFormCommitteeStatus'},
				            {header: 'Note', dataIndex: 'note'}
		        ]
		    }),
		    viewConfig: {
		        forceFit: true
        	}
        }];
        Clara.ProtocolForms.StatusHistoryWindow.superclass.initComponent.call(t);
    }
});