Ext.ns('Clara');

var currentToxinID = '';

// GUI, datastore elements
var selRecordStore;
var selectedNewToxin;
var selectedFormToxin;
var toxinPanel;

/**
 * Renders GUI elements for Sites page
 * @method renderPage
 * @property {String} protocolID the unique ID of the current protocol for the page
 */

function renderPage() {
    initializeToxinStores();
    clog("creating toxin panel");
    createToxinPanel();
}


winAddToxinUi = Ext.extend(Ext.Window, {
    title: 'Add Toxin',
    width: 503,
    height: 379,
    modal: true,
    layout: 'absolute',
    iconCls: 'icn-beaker',
    itemId: 'winAddToxin',
    id: 'winAddToxin',
    initComponent: function () {
    	var t = this;
        this.buttons = [{
            text: 'Close',
            disabled: false,
            handler: function () {
                Ext.getCmp("winAddToxin").close();
            }
        }, {
            text: 'Save Toxin',
            id: 'btn-save-toxin',
            disabled: true,
            handler: function () {
            	
            			var toxinxml = "<toxin id='"+selectedNewToxin.id+"'><toxin-name>"+selectedNewToxin.toxinName+"</toxin-name></toxin>";
            			addXmlToProtocol('/'+claraInstance.form.xmlBaseTag+'/study-toxins/toxin', toxinxml);
            			protocolToxinStore.load({params:{listPath:'/'+claraInstance.form.xmlBaseTag+'/study-toxins/toxin'}});
            		
            			Ext.getCmp("winAddToxin").close();

                	}
            }
        
           ];
        this.items = [{
            xtype: 'tabpanel',
            id: 'toxintabpanel',
            activeTab: 0,
            x: 0,
            y: 50,
            width: 490,
            height: 220,
            border: false,
            items: [
                    {
                xtype: 'panel',
                id: 'toxintp-approved',
                title: 'Find selected agents and Toxins',
                layout: 'absolute',
                height: 192,
                width: 494,
                listeners: {
                    activate: function () {
                        Ext.getCmp("gridSearchResults").getSelectionModel().clearSelections();
                       Ext.getCmp("btn-save-toxin").disable();
                    }
                    
                },
                
                items: [
                        {
		                    xtype: 'grid',
		                    store: approvedToxinStore,
		                    selModel: new Ext.grid.RowSelectionModel({
		                        singleSelect: true,
		                        listeners: {
		                            rowselect: function (grid, rowIndex, record) {
		                               
		                                selectedNewToxin = record.data;
		                                Ext.getCmp("btn-save-toxin").enable();
		                                
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
			                    columns: [{
			                        xtype: 'gridcolumn',
			                        dataIndex: 'toxinName',
			                        header: 'Name',
			                        sortable: true,
			                        width: 500
			                    
			               }
                    ]
                },
                new Ext.ux.form.SearchField({
                    store: approvedToxinStore,
                    paramName: 'keyword',
                    emptyText: 'Search by toxin agent name ',
                    width: 490,
                    style: 'font-size:14px;',
                    x: 0,
                    y: 0,
                    itemId: 'fldSearch',
                    id: 'fldSearch',
                    afterSearch: function () {

                       Ext.getCmp("btn-save-toxin").disable();

                    }
                })]

            }]
        }];

       

        winAddToxinUi.superclass.initComponent.call(this);
    }
});


function createToxinPanel() {
    toxinPanel = new Ext.grid.GridPanel({
        renderTo: 'regulated-toxins-list',
        store: protocolToxinStore,
        width: 646,
        height: 300,
        stripeRows: true,
        itemId: 'gridToxins',
        id: 'gridToxins',
        selModel: new Ext.grid.RowSelectionModel({
            singleSelect: true,
            listeners: {
            	
                rowselect: function (grid, rowIndex, record) {
                   
                    selectedFormToxin = record;
                    Ext.getCmp("btn-remove-toxin").enable();
                    
 
                }
 
            }
        }),
        
        columns: [{
            xtype: 'gridcolumn',
            dataIndex: 'toxin-name',
            header: 'Toxin Agent Name',
            sortable: true,
            width: 550
        }

        ],
        tbar: {
            xtype: 'toolbar',
            items: [

            {
                xtype: 'tbfill'
            }, {
                xtype: 'tbseparator'
            }, {
                xtype: 'button',
                text: 'New Toxin',
                iconCls: 'icn-beaker--plus',
                handler: function () {
                	approvedToxinStore.removeAll();
                    addProtocolToxinWindow = new winAddToxinUi();
                    addProtocolToxinWindow.show();
                }
            }, {
                xtype: 'tbseparator'
            }, {
                xtype: 'button',
                disabled: true,
                text: 'Remove Toxin',
                id: 'btn-remove-toxin',
                iconCls: 'icn-beaker--minus',
                handler: function () {

                    Ext.MessageBox.confirm('Remove Toxin', 'Are you sure you want to do remove this toxin?', function (a) {
                        if (a == "yes") {
                            removeXmlFromProtocol('/' + claraInstance.form.xmlBaseTag + '/study-toxins/toxin', selectedFormToxin.data.id);
                            protocolToxinStore.load({
                                params: {
                                    listPath: '/' + claraInstance.form.xmlBaseTag + '/study-toxins/toxin'
                                }
                            });
                            Ext.getCmp("btn-remove-toxin").disable();
                        }
                    });

                }
            }]
        }

    });

}