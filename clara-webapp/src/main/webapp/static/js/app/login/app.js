Ext.Loader.setConfig({enabled:true});
Ext.Loader.setPath('Clara.Common', appContext+'/static/js/app/common');
Ext.Loader.setPath('Ext.ux', appContext+'/static/js/ext4/ux');

Ext.application({
    name: 'Clara.Login', 
    appFolder:appContext + '/static/js/app/login',

    models:['Clara.Common.model.MessagePost'],
    stores:['Clara.Common.store.MessagePosts'],
    requires:[],
    controllers: ['Login'],
    
    autoCreateViewport: true,
    enableQuickTips: true,
    launch: function() {
    	Clara.Login.app = this;
        // This is fired as soon as the page is ready
    	clog("Ext.app launch.");

		
		if (Ext.isIE7m || (Ext.firefoxVersion > 0 && Ext.firefoxVersion < 13)
				|| (Ext.chromeVersion > 0 && Ext.chromeVersion < 20)
				|| (Ext.safariVersion > 0 && Ext.safariVersion < 5))
		{
			Ext.Msg.show({
				   title:'Your browser version may be out of date.',
				   msg: '<h2>Clara supports Internet Explorer 8, Firefox 13, Chrome 20, Safari 5 or later.</h2><span>Contact your IT department to request a browser upgrade.</span>',
				   buttons: Ext.Msg.OK,
				   icon: Ext.MessageBox.WARNING
				});
		} 
    	
    },
    listeners:{
    }
});