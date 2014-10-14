Ext.define('Clara.DetailDashboard.view.related.AddProjectWindow', {
	extend: 'Ext.window.Window',
	alias: 'widget.addrelatedprojectwindow',
	title: 'Choose a Project',
	width:350,
	modal:true,
	height:100,
	bodyPadding:8,
	layout: {
		type: 'form'
	},

	initComponent: function() {
		var me = this;

		
		me.items = [{
			xtype:'textfield',
			id:'fldAddRelatedProjectPRN',
			emptyText:'Enter a valid PRN #',
			fieldLabel:'Project Number'
		}];
		me.buttons = [{text:'Close', handler:function(){me.close();}},{
			id:'btnAddSelectedProject',
			disabled:true,
			text:'Add Project'
		}];

		me.callParent();
	}
});