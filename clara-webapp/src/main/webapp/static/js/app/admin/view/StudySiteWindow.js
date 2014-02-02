Ext.define('Clara.Admin.view.StudySiteWindow', {
	extend: 'Ext.window.Window',
	alias: 'widget.studysitewindow',
	layout: 'form',
	title: 'Study Site',
	modal:true,
	width:500,
	bodyPadding:6,
	modal: true,
	site:{},
	initComponent: function() {
		var me = this;
		var site = me.site;
		var stateStore = Ext.data.StoreManager.lookup('Clara.Common.store.States');
		var studySitesStore = Ext.data.StoreManager.lookup('Clara.Common.store.StudySites');
		me.buttons = [
		              {
		            	  text: 'Save',
		            	  id:'btnSaveSite',
		            	  disabled:true,
		            	  handler: function(){

		    						// Check for empty fields
		    						var error = '';
		    						if (!Ext.getCmp("fldsiteName").isValid()) error = error+"Enter a site name.\n";
		    						if (!Ext.getCmp("fldAddress").isValid()) error = error+"Enter an address.\n";
		    						if (!Ext.getCmp("fldCity").isValid()) error = error+"Enter a city.\n";
		    						if (!Ext.getCmp("fldState").isValid()) error = error+"Enter a state.\n";
		    						if (!Ext.getCmp("fldZIP").isValid()) error = error+"Enter a ZIP code.\n";
		    						
		    						if (error == ''){
		    							
		    							var siteData = {
												siteName: Ext.getCmp("fldsiteName").getValue(),		    									
												address: Ext.getCmp("fldAddress").getValue(),
												city: Ext.getCmp("fldCity").getValue(),
												state: Ext.getCmp("fldState").getValue(),
												zip: Ext.getCmp("fldZIP").getValue()
										};
		    							
		    							if (Ext.getCmp("fldFwaObtained").getValue() == true){
		    								siteData.fwaObtained = true;
		    								siteData.fwaNumber = Ext.getCmp("fldFwaNumber").getValue();
		    							}
		    							
		    							if (me.site != null){
		    								siteData.id = me.site.get("id");
		    								siteData.approved = me.site.get("approved");
		    							}
		    							
		    							jQuery.ajax({
		    								url: appContext+'/ajax/protocols/protocol-forms/sites/save',
		    								type: "POST",
		    								async: false,
		    								contentType: 'application/json;charset=UTF-8',
		    								dataType: 'json',
		    								data: JSON.stringify(siteData),    								
		    								success: function(data){
		    									studySitesStore.load({params:{admin:true}});
		    									me.close();
		    								}
		    							});
		
		    							
		    						} else {
		    							alert(error);
		    						}
	    					

			    		}
		              }
		              ];
		me.items = [
		                    
		                    {
		                        xtype: 'textfield',
		                        style: 'font-size:14px;',
		                        allowBlank: false,
		                        fieldLabel:'Site name',
		                        itemId: 'fldsiteName',
		                        id: 'fldsiteName',
		                        listeners: {
		                            change: function(t,nv,ov) {
		                        		Ext.getCmp("btnSaveSite").setDisabled((jQuery.trim(nv)==""));
		                            }
		                    	}
		                    },
		                    {
		                        xtype: 'displayfield',
		                        value: 'Use the full name of the site. Add city name in the site name to distinguish between campuses or company locations (i.e. "Acme University - Little Rock")'
		                    },
		                    {
		                        xtype: 'textfield',
		                        fieldLabel:'Address',
		                        allowBlank: false,
		                        itemId: 'fldAddress',
		                        id: 'fldAddress'
		                    },
		                    {
		                        xtype: 'textfield',
		                        fieldLabel:'City',
		                        allowBlank: false,
		                        itemId: 'fldCity',
		                        id: 'fldCity'
		                    },

		                    {
		                        xtype: 'combo',
		                        fieldLabel:'State',
		                        typeAhead:false,
		                        forceSelection:true,
		                        itemId: 'fldState',
		                        store: stateStore,
		                        displayField:'abbr', 
		                        editable:false,
					        	allowBlank:false,
		                        mode:'local', 
					        	triggerAction:'all',
		                        id: 'fldState'
		                    },
		                    {
		                        xtype: 'textfield',
		                        fieldLabel:'ZIP',
		                        allowBlank: false,
		                        itemId: 'fldZIP',
		                        id: 'fldZIP'
		                    },
		                    {
		                        xtype: 'checkbox',
		                        fieldLabel:'FWA was obtained',
		                        itemId: 'fldFwaObtained',
		                        id: 'fldFwaObtained'
		                    },
		                    {
		                        xtype: 'textfield',
		                        fieldLabel:'FWA Number',
		                        itemId: 'fldFwaNumber',
		                        id: 'fldFwaNumber'
		                    }
		                   
		               
		        ];

		me.callParent();
		if (me.site != null){
    		Ext.getCmp("fldsiteName").setValue(me.site.get("siteName"));
    		Ext.getCmp("fldAddress").setValue(me.site.get("address"));
    		Ext.getCmp("fldCity").setValue(me.site.get("city"));
    		Ext.getCmp("fldState").setValue(me.site.get("state"));
    		Ext.getCmp("fldZIP").setValue(me.site.get("zip"));
    		Ext.getCmp("fldFwaNumber").setValue(me.site.get("fwaNumber"));
    		Ext.getCmp("fldFwaObtained").setValue(me.site.get("fwaObtained"));
    		Ext.getCmp("btnSaveSite").setDisabled(false);
    	}
	}
});