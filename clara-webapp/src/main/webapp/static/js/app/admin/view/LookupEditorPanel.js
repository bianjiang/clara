Ext.define('Clara.Admin.view.LookupEditorPanel', {
	extend: 'Ext.grid.Panel',
	requires: ['Ext.ux.form.SearchField'],
	alias: 'widget.lookupeditorpanel',
	title:'Lookup Editor',
	iconCls:'icn-database--pencil',
	border:false,
	viewConfig:{
		trackOver:false
	},
	store:'Things',
	listeners:{
		itemclick: function(gp,rec){
			adminGlobals.selectedLookupItem = rec;
			Ext.getCmp("btnRemoveItem").setDisabled(false);
		}
	},
	initComponent: function() {
		var t = this;
		var thingStore = Ext.data.StoreManager.lookup('Things');
		this.dockedItems = [{
			dock: 'top',
			border:false,
			xtype: 'toolbar',
			items: [{
				fieldLabel: 'Type',
				xtype:'combo',
				id:'fldThingCategory',
				value:'SPONSOR',
			    store: new Ext.data.ArrayStore({
			    	fields:['id'],
			    	data:[['DRUG'],['DEVICE'],['SPONSOR'],['ICD_9_PROC'],['ICD_9_DIAG'],['TOXIN'],['RESEARCH_ORGANIZATION']]
			    }),
			    listeners:{
			    	change:function(cv,v){
			    		clog("CHANGE",v);
			    		Ext.getCmp("fldSearchThings").params.type = v;
			    	}
			    },
			    queryMode: 'local',
			    displayField: 'id',
			    valueField: 'id'
			},{
				xtype:'searchfield',
				id:'fldSearchThings',
				store:thingStore,
				title:'Search category for value',
				emptyText:'Search category for value',
				paramName : 'keyword',
				reloadAllAsClear:false,
				flex:1,
				beforeSearch: function(){
              	  clog("beforesearch!");
              	  Ext.getCmp("btnRemoveItem").setDisabled(true);
              	  if (typeof thingStore.getProxy().extraParams.type == "undefined") Ext.getCmp("fldSearchThings").params.type = Ext.getCmp("fldThingCategory").getValue();	// only run first time
              	  else clog("WILL SEARCH FOR TYPE "+Ext.getCmp("fldSearchThings").params.type);
              	  clog("leaving beforesearch");
              	  return true;
                },
               
                afterClear:function(){
                	Ext.getCmp("btnRemoveItem").setDisabled(true);
                }
			},'->', {
				xtype: 'button',
				id:'btnAddItem',
				text: 'Add Lookup Item',
				disabled:false,
				hidden:false,
				iconCls:'icn-plus',
				handler: function(){
					Ext.create("Clara.Admin.view.NewLookupItemWindow", {}).show();
				}
			},{
				xtype: 'button',
				id:'btnRemoveItem',
				text: 'Remove Item',
				disabled:true,
				hidden:false,
				iconCls:'icn-minus',
				handler: function(){

					Ext.Msg.confirm('Delete?','Are you sure you want to delete this item?',
							function(b){
								if (b == "yes"){
									thingStore.remove(thingStore.getById(adminGlobals.selectedLookupItem.get("id")));
									thingStore.sync({ 
									    success: function (proxy, operations) {
									    	Ext.getCmp("btnRemoveItem").disable();
									    }, failure: function (proxy, operations) {
									        cwarn("Error deleting Thing",operations);
									        thingStore.rejectChanges();
									    }
									});
									
								}
							}
					);


				}
			}]
		}];
		t.selType = 'rowmodel';
	    t.plugins = [
	        Ext.create('Ext.grid.plugin.RowEditing', {
	            clicksToEdit: 2
	        })
	    ];
		t.columns = [
					{
					    xtype: 'gridcolumn',
					    dataIndex: 'type',
					    header: 'Type',
					    sortable: true,
					    flex:1
					},
                     {
                         xtype: 'gridcolumn',
                         dataIndex: 'value',
                         header: 'Value',
                         sortable: true,
                         flex:3,
                         editor: {
                             xtype: 'textfield',
                             allowBlank: false
                         }
                     },
                     {
                         xtype: 'gridcolumn',
                         dataIndex: 'description',
                         header: 'Description',
                         sortable: true,
                         flex:3,
                         editor: {
                             xtype: 'textfield',
                             allowBlank: false
                         }
                     },
                     
                     {
                         xtype: 'gridcolumn',
                         dataIndex: 'approved',
                         header: 'Approved?',
                         sortable: true,
                         width: 70,
                         editor: {
                        	 xtype: 'checkbox',
                        	 cls: 'x-grid-checkheader-editor'
                         }
                     }
                 ];
		t.callParent();
		t.on('edit', function(editor, e) {
		    // commit the changes right after editing finished
		    e.record.commit();
		    e.record.save({ 
		        success: function(record, operation)
		        {
		            clog("Update successful",record);
		        },
		        failure: function(record, operation)
		        {
		        	cwarn("Update FAILED",record);
		        }
		    });
		});
	}

});