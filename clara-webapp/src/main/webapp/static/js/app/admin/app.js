var adminGlobals = {};

Ext.Loader.setPath('Clara.Common', appContext+'/static/js/app/common');
Ext.Loader.setPath('Ext.ux', appContext+'/static/js/ext4/ux');

Ext.application({
    name: 'Clara.Admin', 
    appFolder:appContext + '/static/js/app/admin',
    
    models:['PiwikUserVisit','Thing','Clara.Common.model.IrbRoster','Clara.Common.model.StudySite','Clara.Common.model.User','Clara.Common.model.College','Clara.Common.model.Department','Clara.Common.model.Subdepartment','LockedForm','Role','UserRole'],//,'Roster'],
    stores:['PiwikUserVisits','Things','Clara.Common.store.IrbRosters','Clara.Common.store.StudySites','Clara.Common.store.States','Clara.Common.store.Users','Clara.Common.store.Colleges','Clara.Common.store.Departments','Clara.Common.store.Subdepartments','LockedForms','Roles','UserRoles'],//,'Rosters'],
    controllers: ['Admin','UserVisitHistory'],
    
    autoCreateViewport: true,
    enableQuickTips: true,
    launch: function() {
    	Clara.Admin.app = this;
        // This is fired as soon as the page is ready
    	clog("Ext.app launch.");
    },
    listeners:{
    }
});