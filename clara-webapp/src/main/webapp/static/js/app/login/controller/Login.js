Ext.define('Clara.Login.controller.Login', {
	extend: 'Ext.app.Controller',

	refs: [],

	init: function() {
		var me = this;

		// Start listening for controller events

		// Start listening for events on views
		me.control({});
	},

	loadingMask: new Ext.LoadMask(Ext.getBody(), {msg:"Logging in, please wait..."})

});
