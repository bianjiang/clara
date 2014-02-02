Ext.application({
    name: 'Clara.Super', 
    appFolder:appContext + '/static/js/app/super',
    models:['LiveUsers','MessagePost'],
    stores:['LiveUsers','MessagePosts'],
    autoCreateViewport: true,
    enableQuickTips: true,
    launch: function() {
    	Clara.Super.app = this;
        // This is fired as soon as the page is ready
    	clog("Ext.app launch: "+this.name);
    }
});