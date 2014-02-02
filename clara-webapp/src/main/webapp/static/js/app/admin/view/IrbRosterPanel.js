Ext.define('Clara.Admin.view.IrbRosterPanel', {
	extend: 'Ext.grid.Panel',
	requires: ['Ext.ux.form.SearchField'],
	alias: 'widget.irbrosterpanel',
	title:'IRB Rosters',
	iconCls:'icn-calendar-select-week',
	border:false,
	viewConfig:{
		trackOver:false
	},
	store:'Clara.Common.store.IrbRosters',
	listeners:{
		itemclick: function(gp,rec){
			adminGlobals.selectedIrbRoster = rec;
			Ext.getCmp("btnRemoveMember").setDisabled(false);
		}
	},
	initComponent: function() {
		var me = this;
		var irbRosterStore = Ext.data.StoreManager.lookup('Clara.Common.store.IrbRosters');

		me.dockedItems = [{
			dock: 'top',
			border:false,
			xtype: 'toolbar',
			items: ['->', {
				xtype: 'button',
				id:'btnAddMember',
				text: 'Add Member',
				disabled:false,
				iconCls:'icn-plus',
				handler: function(){
					Ext.create("Clara.Admin.view.NewIRBRosterMemberWindow", {}).show();
				}
			},{
				xtype: 'button',
				id:'btnRemoveMember',
				text: 'Remove Member',
				disabled:true,
				iconCls:'icn-minus',
				handler: function(){
					Ext.MessageBox.confirm('Confirm', 'Are you sure you want to remove this member?', function(btn){
						if (btn == 'yes'){
							jQuery.ajax({
					    		url: appContext + '/ajax/rosters/reviewers/delete',
					    		type: "GET",
					    		async: false,
					    		data: { reviewerId: adminGlobals.selectedIrbRoster.get("id") },
					    		success: function(data){
					    			me.fireEvent('irbrostermemberremoved');	// function is in controller
					    		}
					    	});
						}
					});
				}
			}
			]
		}];
		me.plugins = [
		              Ext.create('Ext.grid.plugin.RowEditing', {
		            	  clicksToEdit: 2
		              })
		              ];
		me.features= [{ftype:'grouping'}];
		me.columns = [{header: 'Roster', width: 80, sortable: true, dataIndex: 'irbRoster', visible:false,
			editor:{
				xtype:'combo',
				store: new Ext.data.ArrayStore({
					fields:['id'],
					data:[['WEEK_1'],['WEEK_2'],['WEEK_3'],['WEEK_4']]
				}),
				queryMode: 'local',
				displayField: 'id',
				valueField: 'id'
			}
		},
		{header: 'Type', width: 50, sortable: true, dataIndex: 'type',
			editor: {
				xtype: 'textfield',
				allowBlank: false
			}},
			{header: 'User', width: 100, sortable: true, dataIndex: 'username'},
			{header: 'Degree', width: 50, sortable: true, dataIndex: 'degree',
				editor: {
					xtype: 'textfield',
					allowBlank: true
				}
			},
			{header: 'Specialty', width: 100, sortable: true, dataIndex: 'specialty',
				editor: {
					xtype: 'textfield',
					allowBlank: true
				}
			},
			{header: 'Affiliated', width: 50, sortable: true, dataIndex: 'affiliated',
				editor: {
					xtype: 'checkbox',
					cls: 'x-grid-checkheader-editor'
				}
			},
			{header: 'Expedited', width: 50, sortable: true, dataIndex: 'expedited',
				editor: {
					xtype: 'checkbox',
					cls: 'x-grid-checkheader-editor'
				}
			},
			{header: 'Chair', width: 50, sortable: true, dataIndex: 'chair',
				editor: {
					xtype: 'checkbox',
					cls: 'x-grid-checkheader-editor'
				}
			},
			{header: 'Comment', flex:1, sortable: true, dataIndex: 'comment',
				editor: {
					xtype: 'textfield',
					allowBlank: true
				}
			}];
		me.callParent();

		me.on('edit', function(editor, e) {
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