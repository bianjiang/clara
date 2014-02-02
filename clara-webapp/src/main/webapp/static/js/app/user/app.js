Ext.application({
    name: 'Clara.User', 
    appFolder:appContext + '/static/js/app/user',
    models:['LockedForm'],
    stores:['LockedForms'],
    autoCreateViewport: true,
    enableQuickTips: true,
    launch: function() {
        // This is fired as soon as the page is ready
    	clog("Ext.app launch: "+this.name);
    }
});