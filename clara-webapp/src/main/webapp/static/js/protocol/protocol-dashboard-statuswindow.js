Ext.ns('Clara.ProtocolDashboard');

Clara.ProtocolDashboard.FormStatusWindow = Ext.extend(Ext.Window, {
	id: 'winFormStatus',
	xtype: 'panel',
	selectedRevertId:'',
	modal:true,
	width:700,
	height:600,
    title: 'Approval Status',
    layout: 'border',
	constructor:function(config){		
		Clara.ProtocolDashboard.FormStatusWindow.superclass.constructor.call(this, config);
	},
	initComponent: function() {
		var config = {
				items: [{
					xtype:'panel',
					region:'north',
					height:400,
					html:'graph'
				},{
					xtype:'panel',
					region:'center',
					html:'details'
				}]
		};
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		Clara.ProtocolDashboard.FormStatusWindow.superclass.initComponent.apply(this, arguments);
	}
});
Ext.reg('claraprotocolformstatuswindow', Clara.ProtocolDashboard.FormStatusWindow);