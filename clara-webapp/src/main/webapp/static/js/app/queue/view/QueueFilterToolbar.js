Ext.define('Clara.Queue.view.QueueFilterToolbar',{
	extend: 'Ext.toolbar.Toolbar',
	alias: 'widget.tbqueuefilter',
	border:false,
	
	initComponent: function(){
		var me = this;
		me.items = [{
			xtype:'tbtext',
			text:'Show:'
		},{
			xtype:'button',
			text:'Assigned to me',
			iconCls:'icn-ui-check-box-uncheck',
			id:'btnTBShowMineOnly',
			enableToggle:true
		},{
			xtype:'button',
			iconCls:'icn-ui-check-box-uncheck',
			text:'Completed items',
			id:'btnTBShowHistory',
			enableToggle:true
		},'->',{
			xtype:'textfield',
			fieldLabel:'Filter',
			labelWidth:40,
			width:140,
			labelAlign:'right',
			id:'fldTBQueueTextFilterField',
			enableKeyEvents:true,
			listeners:{
				keyup:function(f){
				    var v = f.getValue();
					if (jQuery.trim(v) == "") {
						clog("nothing to search by");
						jQuery(".queueitemspanel .x-grid-row").show();
					}
				
					else {
						clog("filtering by "+v);
						jQuery(".queueitemspanel .x-grid-row").each(function(){
							var row = this;
							if (jQuery(row).text().toLowerCase().indexOf(jQuery.trim(v).toLowerCase()) == -1) jQuery(row).hide();
							else jQuery(row).show();
						});
					}
				}
			}
		},{
			xtype:'combo',
			width:120,
			cls: 'cbqueuelist',
			id:'fldTBQueueFormType',
			labelAlign:'right',
			displayField:'value',
			valueField:'value',
			queryMode:'local',
			value:'All Types',
			forceSelection:true,
			store: new Ext.data.ArrayStore({
				storeId:'QueueReviewFormTypeStore',
				fields: ['value'],
				data:[['All Types']]
			})
		},{
			xtype:'combo',
			width:120,
			cls: 'cbqueuelist',
			id:'fldTBQueueReviewStatus',
			labelAlign:'right',
			displayField:'value',
			valueField:'value',
			queryMode:'local',
			value:'All Statuses',
			forceSelection:true,
			store: new Ext.data.ArrayStore({
				storeId:'QueueReviewStatusStore',
				fields: ['value'],
				data:[['All Statuses']]
			})
		},{
			xtype:'combo',
			width:120,
			cls: 'cbqueuelist',
			id:'fldTBQueueReviewRole',
			labelAlign:'right',
			displayField:'value',
			valueField:'value',
			queryMode:'local',
			value:'All Roles',
			forceSelection:true,
			store: new Ext.data.ArrayStore({
				storeId:'QueueReviewRoleStore',
				fields: ['value'],
				data:[['All Roles']]
			})
		},
		{
			xtype:'button',
			id:'btnClearQueueFilter',
			iconCls:'icn-cross'
		}
		];
		me.callParent();
	}

});

