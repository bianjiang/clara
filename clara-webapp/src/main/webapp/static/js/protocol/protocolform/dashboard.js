// This is UI code for the main user dashboard

var protocolFormCommitteeStatusStore;
var protocolFormCommitteeStatusPanel;

function renderProtocolFormDashboardPage(currentProtocolForm){
	
	
	protocolFormCommitteeStatusStore = new Ext.data.XmlStore({
		proxy: new Ext.data.HttpProxy({
			//url: appContext + "/ajax/businesslogic/form/" + formId + "/listFormCommitteeStatus.xml",
			url: appContext+"/ajax/protocols/"+currentProtocolForm.protocolId+"/protocol-forms/"+currentProtocolForm.protocolFormId+"/review/committee-statuses/list.xml",
			method: 'GET',
			headers: {'Accept':'application/xml'}				
		}),
		root: "list",
		record: "protocol-form-committee-status",
		autoLoad: true,
		fields: [
		    {name:'protocolFormCommitteeStatusId', mapping: '@id'},
		    {name:'protocolFormId', mapping: '@protocolFormId'},
		    {name:'committee', mapping: 'committee'},
		    {name:'committee_code', mapping: 'committee_code'},
		    {name:'status', mapping: 'status'},
		    {name:'modified', mapping:'modified'},
		    {name:'actions', convert:function(v, record){		    	
		    	var actions = Ext.DomQuery.select('actions > action',record); //this is is where I start wondering what I should do
		        
		    	var actionsArray = new Array();
		    	for(var i=0;i <  actions.length; i++){

		    		actionsArray.push(actions[i].firstChild.nodeValue);
		    	}

		    	return actionsArray;
		    }}
		]
	});
	
	var renderActionsCell = function(value, metaData, record, rowIndex, colIndex, store){

		var htmlResult =  '<a href="javascript:;" onclick="javascript:alert(\'later...\');">Log Details</a>';		
		return htmlResult;
		
	};
 
	protocolFormCommitteeStatusPanel = new Ext.grid.GridPanel({
    	frame:false,
    	trackMouseOver:false,
    	title:'Committee Status',
    	renderTo: 'protocol-form-dashboard-panel',
        store: protocolFormCommitteeStatusStore,
        sm: new Ext.grid.RowSelectionModel({
            singleSelect: true
        }),
        loadMask: new Ext.LoadMask(Ext.getBody(), {msg:"Please wait..."}),        
        autoHeight:true,
        viewConfig: {
    		forceFit:true
    	},
        columns: [
          		{
					header : 'Committee',
					dataIndex : 'committee',
					sortable : true,
					align : 'left',
					width : 300,
					css : 'font-size:14px;font-weight:800;'
				},
				{
					header : 'Current Status',
					dataIndex : 'status',
					sortable : true,
					width : 300,
					css : 'font-size:14px;line-height:16px;white-space:nowrap !important;white-space:inherit !important;'
				},
				{
					header : 'Last Action Time',
					renderer: Ext.util.Format.dateRenderer('m/d/Y h:i:s'),
					dataIndex : 'modified',
					sortable : true,
					width : 150,
					css : 'white-space:nowrap !important;white-space:inherit !important;'
				},
				{
					header: '',
					dataIndex: 'actions',
					sortable: false,
					renderer: renderActionsCell
				}
				
        ]
    });

}