Ext.define('Clara.Admin.controller.Admin', {
    extend: 'Ext.app.Controller',
    models: ['UserRole'],
    stores: ['UserRoles'],
    
    init: function() {
        // Start listening for events on views
        this.control({
        	'adminlockedformspanel':{
        		beforeshow:this.onLockedFormsUpdate
        	},
        	'addrolewindow':{
        		userroleadded:this.onRoleUpdate
        	},
        	'newirbrostermemberwindow':{
        		irbrostermemberadded: this.onIRBRosterUpdate
        	},
        	'studysitespanel':{
        		beforeshow:this.onSitesUpdate
        	},
        	'irbrosterpanel':{
        		irbrostermemberremoved: this.onIRBRosterUpdate,
        		beforeshow:this.onIRBRosterUpdate
        	},
        	'userroleswindow':{
        		userroleremoved:this.onRoleUpdate
        	},
        	'button[action=create_clara_account]':{
        		click: function(){ this.createClaraAccountForCampusUser(adminGlobals.selectedUser); }
        	},
        	'button[action=create_off_campus_account]':{
        		click: function(){ this.showOffCampusUserWindow(); }
        	}
        });
    },
    
    
    onLockedFormsUpdate: function(){
    	clog("onLockedFormsUpdate");
    	var st = Ext.data.StoreManager.lookup('LockedForms');
    	st.load();
    },
    
    onSitesUpdate: function(){
    	clog("onSitesUpdate");
    	var st = Ext.data.StoreManager.lookup('Clara.Common.store.StudySites');
    	st.load();
    },
    
    onIRBRosterUpdate: function(){
    	clog("onIRBRosterUpdate");
    	var rosterStore = Ext.data.StoreManager.lookup('Clara.Common.store.IrbRosters');
    	rosterStore.load();
    	adminGlobals.selectedIrbRoster = {};
    	Ext.getCmp("btnRemoveMember").setDisabled(true);
    },
    
    onRoleUpdate: function(user){
    	clog("onRoleUpdate",user);
    	var roleStore = Ext.data.StoreManager.lookup('UserRoles');
    	roleStore.loadUserRoles(user.get("userid"));
    },
    
    showOffCampusUserWindow: function(){
    	Ext.create("Clara.Admin.view.NewOffCampusUserWindow", {}).show();
    },
    
    createClaraAccountForCampusUser: function(user){
    	var t = this;
    	clog("createClaraAccountForUser",user);
    	var username = user.get("username");
    	jQuery.ajax({
    		url: appContext + "/ajax/users/createuseraccount",
    		type: "GET",
    		dataType: "json",
    		data: "username="+username,
    		async: false,
    		success: function(data){
    			data.userid = data.userId;				// We're no longer getting a user object like from the search results.
    			data.firstname = data.username;		// THIS LINE just for show, it doesnt affect data
    			data.lastname = "";					// Ditto
    			//adminSelectedUser = data;
    			clog(data);
    			user.set("id",data.id);
    			user.set("userid",data.id);
    			
    			user.commit();
    			t.onUserAdded(user);
    		}
    	});
    },
    
    onUserAdded: function(user){
    	clog("onUserAdded",user);
    	adminGlobals.selectedUser = user;
    	var userPanel = Ext.ComponentQuery.query('userspanel')[0];
		var row = userPanel.getStore().find("username",user.get("username"));
		var rec = userPanel.getStore().getAt(row);
		rec.set("id",user.get("id"));
		clog("rec",rec);
		rec.commit();
		userPanel.getView().select(row);
		
		clog("refreshing?");
		userPanel.getView().refresh();
	},  
    
    onLaunch: function() {
  
    }
});
