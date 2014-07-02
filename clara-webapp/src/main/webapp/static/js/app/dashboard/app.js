Ext.Loader.setPath('Clara.Common', appContext+'/static/js/app/common');
Ext.Loader.setPath('Ext.ux', appContext+'/static/js/ext4/ux');

Ext.application({
    name: 'Clara.Dashboard', 
    appFolder:appContext + '/static/js/app/dashboard',
    
    models:['Clara.Common.model.FundingSource','Clara.Common.model.User','Clara.Common.model.College','Clara.Common.model.Department','Clara.Common.model.Subdepartment'],
    stores:['Clara.Common.store.FundingSources','Clara.Common.store.Users','Clara.Common.store.Colleges','Clara.Common.store.Departments','Clara.Common.store.Subdepartments'],
    controllers: ['Dashboard'],
    
    autoCreateViewport: true,
    enableQuickTips: true,
    launch: function() {
    	Clara.Dashboard.app = this;
    	Clara.Dashboard.app = this;
        // This is fired as soon as the page is ready
    	clog("Ext.app launch.");
    	if (piwik_enabled()){
			_paq.push(['trackEvent', 'DB', 'Launch']);
		}
    }
});