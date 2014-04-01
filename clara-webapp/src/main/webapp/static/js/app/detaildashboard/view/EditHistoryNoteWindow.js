Ext.define('Clara.DetailDashboard.view.EditHistoryNoteWindow', {
	extend: 'Ext.window.Window',
	requires:[],
	alias: 'widget.edithistorynotewindow',
	title: 'Editing history note..',
	width:650,
	modal:true,
	height:360,
	bodyPadding:6,
	historyRecord:null,
	layout: {
		type: 'form'
	},

	initComponent: function() {
		var me = this;

		me.items = [{
			xtype:'textarea',
			name:'fldEditedHistoryNote',
			id:'fldEditedHistoryNote',
			allowBlank:false,
			fieldLabel:'Note',
			height:282,
	        anchor    : '100% 100%',
	        value: me.historyRecord.get("reviewNoteBody")
		}];
		me.buttons = [{text:'Cancel', handler:function(){me.close();}},
		              {text:'Save', handler:function(){
		            	  if (jQuery.trim(Ext.getCmp("fldEditedHistoryNote").getValue()) !== ""){
		            		  Clara.Application.DashboardController.updateHistoryRecord(me.historyRecord, Ext.getCmp("fldEditedHistoryNote").getValue());
		            		  me.close();
		            	  } else {
		            		  alert("Edited notes cannot be empty. If you want to delete, choose 'Delete' action instead.");
		            	  }
		            	  
		              }}];

		me.callParent();
	}
});