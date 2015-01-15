Ext.Loader.setPath('Clara.Common', appContext+'/static/js/app/common');
Ext.Loader.setPath('Clara.Admin', appContext+'/static/js/app/admin');
Ext.Loader.setPath('Ext.ux', appContext+'/static/js/ext4/ux');

Ext.application({
    name: 'Clara.Super', 
    appFolder:appContext + '/static/js/app/super',
    controllers:['Clara.Admin.controller.UserVisitHistory'],
    models:['Clara.Admin.model.PiwikUserVisit','LiveUsers','MessagePost'],
    stores:['Clara.Admin.store.PiwikUserVisits','LiveUsers','MessagePosts'],
    autoCreateViewport: true,
    enableQuickTips: true,
    launch: function() {
    	Clara.Super.app = this;
        // This is fired as soon as the page is ready
    	clog("Ext.app launch: "+this.name);
    }
});