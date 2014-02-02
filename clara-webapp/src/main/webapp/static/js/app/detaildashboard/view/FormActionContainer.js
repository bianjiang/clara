Ext.define('Clara.DetailDashboard.view.FormActionContainer',{
	extend: 'Ext.container.Container',
	alias: 'widget.formactioncontainer',
	border:false,
	layout:'fit',
	padding:12,

	style:'border-bottom:1px solid #96baea;width:100%;background-color:#dee8f7;',
	html:'<div id="protocolform-norowselected">Choose a form to see available actions.</div>',
	
	initComponent: function(){
		var me = this;
		me.callParent();
	}

	
});