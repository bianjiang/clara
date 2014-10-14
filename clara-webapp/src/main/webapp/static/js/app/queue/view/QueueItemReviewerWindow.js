Ext.define('Clara.Queue.view.QueueItemReviewerWindow', {
	extend: 'Ext.window.Window',
	alias: 'widget.queueitemreviewerwindow',
	layout: {
		type: 'border'
	},
	title: 'Assign reviewers',
	modal:true,
	border:false,
	width:750,
	height:500,
	
	formQueueItem:null,
	formQueueType:null,
	triggerWorkflow:null,

	initComponent: function() {
		var me = this;
		
		
		var controller = Clara.Application.getController("QueueAssign");
		var assignedReviewersStore = Ext.data.StoreManager.lookup("Clara.Queue.store.AssignedReviewers");
		var reviewersStore = Ext.data.StoreManager.lookup("Clara.Queue.store.Reviewers");
		
		clog("Initing window",me,"formQueueItem",me.formQueueItem," queueAssign:queueItem",controller.queueItem);
		
		me.buttons = [{
			text:'Save Assignment',
			id:'btnSaveAssignment'
		}];
		me.items = [{
			xtype:'form',
			border:false,
			
			bodyPadding:6,
			region:'north',
			height:34,
			bodyStyle:'border-bottom:1px solid #96baea;',
			items:[{
				xtype:'combo',
				fieldLabel:'What kind of reviewer',
				typeAhead:false,
				labelWidth:140,
				forceSelection:true,
				store: controller.getActorStore(me.formQueueItem, me.formQueueType, me.triggerWorkflow),
				displayField:'roledisplayname', 
				valueField:'roleid',
				editable:false,
				allowBlank:false,
				mode:'local', 
				id: 'fldReviewerType',
				triggerAction:'all'
			}]
		},{
			region:'center',
			split:true,
			xtype:'grid',
			id:'gpQueueItemAvailableReviewers',
			hideHeaders: true,
			store:reviewersStore,
			title:'Available Reviewers',

			columns: [{dataIndex: 'id',renderer:function(v,p,r){
				return "<div class='reviewer-row'>"+
				"<h2>"+r.get("firstname")+" "+r.get("lastname")+"</h2><span>"+r.get("email")+"</span></div>";
			}, flex:1}],
			dockedItems: [{
				dock: 'bottom',
				border:false,
				xtype: 'toolbar',
				items: ['->',{
					id:'btnAssignReviewerToQueueItem',
					disabled:true,
					text:'Assign',
					iconAlign:'right',
					iconCls:'icn-arrow'
				}]
			}]
		},{
			region:'east',
			width:350,
			split:true,
			xtype:'grid',
			id:'gpQueueItemAssignedReviewers',
			hideHeaders: true,
			store:assignedReviewersStore,
			title:'Reviewers assigned to this item',
			columns: [{dataIndex: 'id',renderer:function(v,p,r){
				return "<div class='reviewer-row'>"+
				"<h2>"+r.get("name")+"<span style='font-weight:100;'> &gt; "+Clara_HumanReadableRoleName(r.get("userrole"))+"</span></h2></div>";
			}, flex:1}],
			dockedItems: [{
				dock: 'bottom',
				border:false,
				xtype: 'toolbar',
				items: ['->',{
					id:'btnRemoveReviewerFromQueueItem',
					disabled:true,
					iconAlign:'left',
					iconCls:'icn-minus',
					text:'Remove',
				}]
			}]
		}];

		me.callParent();

	}
});