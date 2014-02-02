Ext.ns('Clara.ProtocolDashboard');

Clara.ProtocolDashboard.ContractsPanel = Ext.extend(Ext.grid.GridPanel, {
	id: 'clara-protocol-db-contractgridpanel',
	height:350,
	bodyStyle:'border-bottom: 1px solid #8DB2E3;',
	constructor:function(config){		
		Clara.ProtocolDashboard.ContractsPanel.superclass.constructor.call(this, config);
	},	
	
	initComponent: function(){
		var t = this;
		var config = {
				store: new Ext.data.XmlStore({
					scope:this,
					
					proxy: new Ext.data.HttpProxy({
						url: appContext + "/ajax/contracts/search-by-protocol/list.xml?protocolId="+claraInstance.id,
						method:"GET",
						headers:{'Accept':'application/xml;charset=UTF-8'}
					}),
					record: 'contract', 
					autoLoad:false,
					root:'list',
					fields: [
					    {name:'id', mapping: '@id'},
					    
						{name:'status', mapping:'status'},
						//{name:'statusModified',mapping:'status>modified', type: 'date', dateFormat: 'Y-m-d H:i:s.u'},
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
		        		return "<span class='protocol-form-row-field protocol-form-type'>Contract ID# "+record.get("id")+"</span>";
		        		//return "<span class='protocol-form-row-field protocol-form-type'>"+formType+"</span>";
		            }},
		            {header: 'Last Modified', width: 95, sortable: true, renderer: function(value) { return "<span class='protocol-form-row-field'>"+Ext.util.Format.date(value,'m/d/Y')+"</span>";}, dataIndex: 'statusModified'},
		            {header: 'Status', id:'protocol-forms-status-column', width: 215, sortable: true, dataIndex: 'status', renderer:function(v,p,record){return "<span class='protocol-form-row-field protocol-form-status'>"+record.data.status+"</span>";}},
		            {header: 'Actions', id:'protocol-forms-actions-column', width: 315, sortable: true, dataIndex: 'actions', 
		            	renderer:function(v,p,record){
		            		// TODO: Fix actions in contract metadata 
		            		
		            		return '&nbsp;<a style="text-decoration:underline;" href="'+appContext+'/contracts/' + record.get("id") + '/dashboard">Open</a>';

		            	}
		            }
		        ],
				listeners: {
		    
				    rowclick: function(grid, rowI, event)   {
						var record = grid.getStore().getAt(rowI);
						Ext.getCmp("clara-protocol-db-formstatusgridpanel").enable();
						Ext.getCmp("clara-protocol-db-formstatusgridpanel").currentProtocolForm.protocolFormId = record.data.protocolFormId;
						Ext.getCmp("clara-protocol-db-formstatusgridpanel").resetUrl();
						Ext.getCmp("clara-protocol-db-formstatusgridpanel").getStore().load();
				    }
				}
		};
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		Clara.ProtocolDashboard.ContractsPanel.superclass.initComponent.apply(this, arguments);
	}
});
Ext.reg('claraprotocolcontractspanel', Clara.ProtocolDashboard.ContractsPanel);