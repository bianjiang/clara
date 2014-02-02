Ext.define('Clara.Queue.view.IRBAssignmentWindow', {
	extend: 'Ext.window.Window',
	alias: 'widget.irbassignmentwindow',
	layout: {
		type: 'border'
	},
	title: 'Assign to IRB Agenda / Reviewer',
	modal:true,
    width: 400,
    height:240,
    layout: 'border',
    bodyCssClass:'queues-assign-window-body',
    requires:['Clara.Agenda.store.Agendas'],

	initComponent: function() {
		var me = this;
		clog("Initing window",me);
		
		var queueController = Clara.Queue.app.getController("Queue"),
		    queueItemController = Clara.Queue.app.getController("QueueItem"),
		    expeditedExemptReviewerStore = Ext.data.StoreManager.lookup("Clara.Queue.store.ExpeditedExemptReviewers"),
		    irbPrereviewDetermination = "";

		if (queueItemController.selectedQueueItem.get("irbSuggestedType") != "") { irbPrereviewDetermination = queueItemController.selectedQueueItem.get("irbSuggestedType"); }
		irbPrereviewDetermination = (irbPrereviewDetermination == "")?"<span style='font-weight:100;color:#999;'>None</span>":irbPrereviewDetermination;
		
		me.buttons = [{
			text:'Cancel',
			handler:function(){ me.close(); }
		},{
			text:'Assign',
			id:'btnSaveAgendaAssignment'
		}];
		
		me.items = [{
      	  xtype:'panel',
      	  region:'north',
      	  border:false,
      	  bodyPadding:6,
      	  html:'<h1 style="font-size:14px;">IRB#'+queueItemController.selectedQueueItem.get("identifier")+'</h1>'+
      	  		'<h2 style="font-weight:100;">'+queueItemController.selectedQueueItem.get("formType")+'</h2>'+
      	  		'<h2 style="font-weight:100;">Prereview determination: <strong>'+irbPrereviewDetermination+'</strong></h2>'
		},{

      	  xtype:'form',
      	  bodyPadding:6,
      	  border:false,
      	  region:'center',
      	  items:[{
                xtype: 'combo',
                fieldLabel:'Item Type',
                typeAhead: false,
                border:false,
                triggerAction: 'all',
                store: new Ext.data.ArrayStore({
                	fields:['type', 'enumtype', 'formTypes'],
                	filters:[function(r){
                		// only show types matching the current form type
                		clog(queueItemController.selectedQueueItem.get("formTypeId")+": filtering r",r);
                		return (r.get("formTypes").indexOf(queueItemController.selectedQueueItem.get("formTypeId")) > -1);
                	}],
                	data: [['Full Board','FULL_BOARD','new-submission,reportable-new-information,humanitarian-use-device,humanitarian-use-device-renewal,emergency-use,continuing-review,modification,audit,staff'],
                		    ['Expedited','EXPEDITED','new-submission,reportable-new-information,humanitarian-use-device,humanitarian-use-device-renewal,continuing-review,modification,study-closure,staff'],
                		    ['Exempt','EXEMPT','new-submission,continuing-review'],
                		    ['Reported','REPORTED','safety-report']]
                }),
                allowBlank:false,
                displayField:'type',
                valueField:'enumtype',
                mode:'local',
                triggerAction:'all',
                id: 'fldAssignAgendaItemType'
            },{
            	xtype:'combo',
            	fieldLabel:'IRB Agenda Date',
            	id: 'fldAssignAgendaItemDate',
            	typeAhead: false,
                forceSelection:true,
                store: 'Clara.Agenda.store.Agendas',
                allowBlank:false,
                displayField:'date',
                tpl:new Ext.XTemplate(
              		  '<tpl for="."><div class="x-boundlist-item">{date:date("m/d/Y")} - {irbRoster}</div></tpl>'
                      ),
                displayTpl: new Ext.XTemplate('<tpl for=".">{date:date("m/d/Y")} - {irbRoster}</tpl>'),
                valueField:'id',
                mode:'local',
                disabled:true
            },
            {
                xtype: 'combo',
                fieldLabel:'Expedited/Exempt Reviewer',
                typeAhead: false,
                width:367,
                forceSelection:true,
                store: 'Clara.Queue.store.ExpeditedExemptReviewers',
                allowBlank:false,
                displayField:'username',
                tpl:new Ext.XTemplate(
              		  '<tpl for="."><div class="x-boundlist-item">{lastname}, {firstname} ({email})</div></tpl>'
                      ),
                valueField:'id',
                mode:'local',
                disabled:true,
                name: 'fldAssignItemReviewer',
                id: 'fldAssignItemReviewer'
            }
           ]
        
		}];

		me.callParent();

	}
});