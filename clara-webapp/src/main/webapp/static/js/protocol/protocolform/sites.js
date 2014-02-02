var currentSiteID = '';

// GUI, datastore elements
var protocolSiteStore;
var selRecordStore;
var selectedNewSite;
var selectedFormSite;
var sitePanel;

function siteJsonToXml(j){
	var xml = "<site site-id=\"" + j.id + "\" approved=\"" + j.approved + "\">";
	xml = xml + "<site-name>"+Encoder.cdataWrap(j.siteName)+"</site-name>";
	xml = xml + "<address>"+Encoder.cdataWrap(j.address)+"</address>";
	xml = xml + "<city>"+Encoder.cdataWrap(j.city)+"</city>";
	xml = xml + "<state>"+Encoder.cdataWrap(j.state)+"</state>";
	xml = xml + "<zip>"+Encoder.cdataWrap(j.zip)+"</zip>";
	xml = xml + "<site-contact>"+Encoder.cdataWrap(j.siteContact)+"</site-contact>";
	return xml+"</site>";
}

winAddSiteUi = Ext.extend(Ext.Window, {
    title: 'Add Site',
    width: 503,
    height: 379,
    modal: true,
    siteId:null,
    site:{},
    layout: 'absolute',
    iconCls: 'icn-building',
    itemId: 'winAddSite',
    id: 'winAddSite',
    initComponent: function() {
    	var t = this;
    	var editing = (t.site && t.site.data && t.site.get("id"))?true:false;
    	clog("editing?",editing);
		this.buttons = [
		    			{
		    				text:'Close',
		    				disabled:false,
		    				handler: function(){
		    				Ext.getCmp("winAddSite").close();
		    				}
		    			},
		    			{
		    				text:'Save Site',
		    				id:'btn-save-site',
		    				disabled:(editing == false)?true:false,
		    				handler: function(){
		    					var tp = Ext.getCmp("sitetabpanel");
		    					if (tp.getActiveTab().getId() == 'sitetp-unapproved'){
		    						// Check for empty fields
		    						var error = '';
		    						if (!Ext.getCmp("fldsiteName").isValid()) error = error+"Enter a site name.\n";
		    						if (!Ext.getCmp("fldAddress").isValid()) error = error+"Enter an address.\n";
		    						if (!Ext.getCmp("fldCity").isValid()) error = error+"Enter a city.\n";
		    						if (!Ext.getCmp("fldState").isValid()) error = error+"Enter a state.\n";
		    						if (!Ext.getCmp("fldZIP").isValid()) error = error+"Enter a ZIP code.\n";
		    						
		    						if (error == ''){
		    							
		    							var site = {
												siteName: jQuery("#fldsiteName").val(),		    									
	    										address: jQuery("#fldAddress").val(),
	    										city: jQuery("#fldCity").val(),
	    										state: jQuery("#fldState").val(),
	    										zip: jQuery("#fldZIP").val()
										};
		    							
		    							jQuery.ajax({
		    								url: (editing)?appContext+'/ajax/protocols/protocol-forms/sites/save':appContext+'/ajax/protocols/protocol-forms/sites/save',
		    								type: "POST",
		    								async: false,
		    								contentType: 'application/json;charset=UTF-8',
		    								dataType: 'json',
		    								data: JSON.stringify(site),    								
		    								success: function(data){
		    									data.siteContact = jQuery("#fldContact").val();
		    									var sitexml = siteJsonToXml(data);
		    									if(editing) updateExistingXmlInProtocol('/'+claraInstance.form.xmlBaseTag+'/study-sites/site', t.site.get("id"), sitexml);
		    									else addXmlToProtocol( '/'+claraInstance.form.xmlBaseTag+'/study-sites/site', sitexml);
		    									protocolSiteStore.load({params:{listPath:'/'+claraInstance.form.xmlBaseTag+'/study-sites/site'}});
		    									Ext.getCmp("winAddSite").close();
		    								}
		    							});

		    							
		    						} else {
		    							alert(error);
		    						}
		    						
		    							
		    					} else {
		    						
		    						if(editing) {
		    							selectedNewSite = {json:{
	    									id:t.site.get("site-id"),
	    									approved:t.site.get("approved"),
	    									siteName:t.site.get("site-name"),
	    									city:t.site.get("city"),
	    									address:t.site.get("address"),
	    									state:t.site.get("state"),
	    									zip:t.site.get("zip"),
	    									contact:t.site.get("contact")		    									
		    							}};
		    						}
		    						selectedNewSite.json.siteContact = jQuery("#fldContact").val();
		    						var sitexml = siteJsonToXml(selectedNewSite.json);

		    						if(editing) updateExistingXmlInProtocol('/'+claraInstance.form.xmlBaseTag+'/study-sites/site', t.site.get("id"), sitexml);
									else addXmlToProtocol( '/'+claraInstance.form.xmlBaseTag+'/study-sites/site', sitexml);
									protocolSiteStore.load({params:{listPath:'/'+claraInstance.form.xmlBaseTag+'/study-sites/site'}});
		    						Ext.getCmp("winAddSite").close();
		    					}
		    					
		    					Ext.getCmp("gridSites").getSelectionModel().clearSelections();
	                    		Ext.getCmp("btn-remove-site").disable();

		    				}
		    		}
	                ],
        this.items = [
            {
                xtype: 'tabpanel',
                id:'sitetabpanel',
                activeTab: 0,
                x: 0,
                y: 50,
                width: 490,
                height: 220,
                border: false,
                items: [
                    {
                        xtype: 'panel',
                        id:'sitetp-approved',
                        title: 'Select IRB-Approved Site',
                        layout: 'absolute',
                        height: 192,
                        width: 494,
                        listeners: {
	                        activate: function() {
                    			Ext.getCmp("gridSearchResults").getSelectionModel().clearSelections();
	                    		if (editing == false) Ext.getCmp("btn-save-site").disable();
	                        }
                    	},
                        items: [
                            {
                                xtype: 'grid',
                                store: approvedSiteStore,
                                disabled:editing,
                                selModel: new Ext.grid.RowSelectionModel({
                    	        	singleSelect:true,
                    	        	listeners: {
                    	        		rowselect: function(grid,rowIndex,record){
                                					selectedNewSite = record;
                    	        					Ext.getCmp("btn-save-site").enable();
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
                                        dataIndex: 'siteName',
                                        header: 'Name',
                                        sortable: true,
                                        width: 290
                                    },
                                    {
                                        xtype: 'gridcolumn',
                                        dataIndex: 'city',
                                        header: 'City',
                                        sortable: true,
                                        width: 150
                                    },
                                    {
                                        xtype: 'gridcolumn',
                                        dataIndex: 'state',
                                        header: 'State',
                                        sortable: true,
                                        width: 47
                                    }
                                ]
                            },
                             new Ext.ux.form.SearchField({
                                store:approvedSiteStore,
                                disabled:editing,
                                paramName:'keyword',
                                emptyText:'Search by site name or location',
                                width: 490,
                                style: 'font-size:14px;',
                                x: 0,
                                y: 0,
                                itemId: 'fldSearch',
                                id: 'fldSearch',
                                afterSearch: function(){
                    		   		
                            	 	Ext.getCmp("btn-save-site").disable();
                    	   			
                    	   		}
                            })
                        ]
                    },
                    {
                        xtype: 'panel',
                        id:'sitetp-unapproved',
                        title: 'Create New Site',
                        disabled:(editing && (t.site && t.site.data && t.site.get("approved") == 'true')),
                        layout: 'absolute',
                        height: 194,
                        listeners: {
	                        activate: function() {
                    			Ext.getCmp("gridSearchResults").getSelectionModel().clearSelections();
	                    		Ext.getCmp("btn-save-site").enable();
	                        }
                    	},
                        items: [
                            {
                                xtype: 'displayfield',
                                value: 'Site Name',
                                x: 0,
                                y: 50,
                                style: 'font-size:14px;text-align:right;',
                                width: 70,
                                itemId: 'lblsiteName',
                                id: 'lblsiteName'
                            },
                            {
                                xtype: 'displayfield',
                                value: 'Address',
                                x: -4,
                                y: 120,
                                style: 'font-size:14px;text-align:right;',
                                width: 70,
                                itemId: 'lblAddress',
                                id: 'lblAddress'
                            },
                            {
                                xtype: 'displayfield',
                                value: 'City',
                                x: -3,
                                y: 150,
                                style: 'font-size:14px;text-align:right;',
                                width: 70,
                                itemId: 'lblCity',
                                id: 'lblCity'
                            },
                            {
                                xtype: 'displayfield',
                                value: 'State',
                                x: 240,
                                y: 150,
                                style: 'font-size:14px;text-align:right;',
                                width: 40,
                                itemId: 'lblState',
                                id: 'lblState'
                            },
                            {
                                xtype: 'displayfield',
                                value: 'ZIP',
                                x: 340,
                                y: 150,
                                style: 'font-size:14px;text-align:right;',
                                width: 30,
                                itemId: 'lblZIP',
                                id: 'lblZIP'
                            },
                            {
                                xtype: 'textfield',
                                x: 80,
                                y: 50,
                                width: 400,
                                style: 'font-size:14px;',
                                allowBlank: false,
                                itemId: 'fldsiteName',
                                value:(editing)?t.site.get("site-name"):'',
                                id: 'fldsiteName'
                            },
                            {
                                xtype: 'textfield',
                                x: 80,
                                y: 120,
                                width: 400,
                                style: 'font-size:14px;',
                                allowBlank: false,
                                itemId: 'fldAddress',
                                value:(editing)?t.site.get("address"):'',
                                id: 'fldAddress'
                            },
                            {
                                xtype: 'textfield',
                                x: 80,
                                y: 150,
                                width: 160,
                                style: 'font-size:14px;',
                                allowBlank: false,
                                itemId: 'fldCity',
                                value:(editing)?t.site.get("city"):'',
                                id: 'fldCity'
                            },
                            {
                                xtype: 'textfield',
                                x: 380,
                                y: 150,
                                width: 100,
                                style: 'font-size:14px;',
                                allowBlank: false,
                                itemId: 'fldZIP',
                                value:(editing)?t.site.get("zip"):'',
                                id: 'fldZIP'
                            },
                            {
                                xtype: 'combo',
                                x: 290,
                                y: 150,
                                width: 50,
                                typeAhead:false,
                                forceSelection:true,
                                itemId: 'fldState',
                                store: stateStore,
                                displayField:'abbr', 
                                editable:false,
        			        	allowBlank:false,
        			        	value:(editing)?t.site.get("state"):'',
                                mode:'local', 
        			        	triggerAction:'all',
                                id: 'fldState'
                            },
                            {
                                xtype: 'container',
                                width: 490,
                                height: 40,
                                html: '<h3>Unapproved sites will be reviewed by the IRB for accuracy.</h3>',
                                itemId: 'unapprovedSiteHeader',
                                x: 0,
                                y: 0,
                                id: 'unapprovedSiteHeader'
                            },
                            {
                                xtype: 'displayfield',
                                value: 'Use the full name of the site. Add city name in the site name to distinguish between campuses or company locations (i.e. "Acme University - Little Rock")',
                                x: 80,
                                y: 70,
                                width: 400
                            }
                        ]
                    }
                ]
            },
            {
                xtype: 'container',
                x: 0,
                y: 0,
                width: 490,
                height: 50,
                html: '<h1>Select an institution that has designated your IRB as the IRB of Record for their locations, or create a new location.</h1>',
                itemId: 'winHeader',
                id: 'winHeader'
            },
            {
                xtype: 'displayfield',
                value: 'Site contact for this study',
                x: 10,
                y: 280,
                style: 'font-size:14px;',
                itemId: 'lblContact',
                id: 'lblContact'
            },
            {
                xtype: 'textfield',
                x: 180,
                y: 280,
                width: 300,
                itemId: 'fldContact',
                id: 'fldContact',
                value:(editing)?t.site.get("contact"):''
            }
        ];
        
        approvedSiteStore.load({params:{common:true}});
        
        winAddSiteUi.superclass.initComponent.call(this);
    }
});


/**
* Renders GUI elements for Sites page
* @method renderPage
* @property {String} protocolID the unique ID of the current protocol for the page
*/
function renderPage(){
	
	initializeSitesStores();
	createSitePanel();
}

function createSitePanel(){

	sitePanel =  new Ext.grid.GridPanel({
		renderTo: 'external-site-list',
	    store: protocolSiteStore,
	    width: 646,
	    height: 300,
	    stripeRows: true,
	    itemId: 'gridSites',
	    id: 'gridSites',
	    view: new Ext.grid.GridView({
	    	getRowClass: function(r,idx,rp,ds){
	    		if (r.get('approved') == "false") return "siterow-unapproved";
	    		else return "siterow-approved";
	    	}
	    }),
	    selModel: new Ext.grid.RowSelectionModel({
        	singleSelect:true,
        	listeners: {
        		rowselect: function(grid,rowIndex,record){
        					selectedFormSite = record;
        					Ext.getCmp("btn-remove-site").enable();
        				}

        	}
        }),
        listeners:{
		    rowdblclick: function(grid, rowI, event)   {
				clog("dblclick!!");
				var record = grid.getStore().getAt(rowI);
				selectedFormSite = record;
				new winAddSiteUi({site:selectedFormSite}).show();
		    },
		    rowclick: function(grid, rowI, event)   {
				var record = grid.getStore().getAt(rowI);
				selectedFormSite = record;
				Ext.getCmp("btn-remove-site").enable();
		    }
	    },
	    columns: [
	            {
	                xtype: 'gridcolumn',
	                dataIndex: 'site-name',
	                header: 'Site Name',
	                sortable: true,
	                renderer: function(v,p,r){
	                	var h = "<div class='external-site'>";
	                	h += "<h1>" + r.get("site-name") + "</h1>";
	                	h += "<span>" + r.get("address") + "</span><br/>";
	                	h += "<span>" + r.get("city") + ", " + r.get("state") + " " + r.get("zip") + "</span><br/>";
	                	
	                	return h;
	                },
	                width: 350
	            },
	            {
	                xtype: 'gridcolumn',
	                header: 'Contact',
	                dataIndex: 'contact',
	                sortable: true,
	                width: 202
	            },
	            
                {
                    xtype: 'gridcolumn',
                    dataIndex: 'approved',
                    header: 'Approved?',
                    sortable: true,
                    width: 90,
                    renderer:function(val){
                		return (val == "true")?"Yes":"<span style='color:red;font-weight:800;'>No</span>";
                	}
                }
	        ],
	    tbar: {
	            xtype: 'toolbar',
	            items: [
	                {
	                    xtype: 'button',
	                    text: 'New Site',
	                    iconCls: 'icn-building--plus',
	                    handler: function(){
	                    	approvedSiteStore.removeAll();
	                	 	addProtocolSiteWindow = new winAddSiteUi();
	                	 	addProtocolSiteWindow.show();
	                	}
	                },
	                {
	                    xtype: 'tbseparator'
	                },
	                {
	                    xtype: 'button',
	                    disabled:true,
	                    text: 'Remove Site',
	                    id:'btn-remove-site',
	                    iconCls: 'icn-building--minus',
	                    handler: function(){
	                	
	                		Ext.MessageBox.confirm('Remove Site', 'Are you sure you want to do remove this site?', function(a){
	                			if (a == "yes") {
	                				removeXmlFromProtocol('/'+claraInstance.form.xmlBaseTag+'/study-sites/site', selectedFormSite.data.id);
	                				protocolSiteStore.load({params:{listPath:'/'+claraInstance.form.xmlBaseTag+'/study-sites/site'}});
	                				Ext.getCmp("btn-remove-site").disable();
	                			}
	                			
	                		});
	                		
	                	}
	                }
	            ]
	        }

	});

}
