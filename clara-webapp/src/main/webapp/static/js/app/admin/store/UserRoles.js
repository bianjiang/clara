Ext.define('Clara.Admin.store.UserRoles', {
    extend: 'Ext.data.Store',
    requires: 'Clara.Admin.model.UserRole',    
    model: 'Clara.Admin.model.UserRole',
    autoLoad: false,
    loadUserRoles: function(id){
    	var t = this;
    	t.getProxy().url = appContext + '/ajax/users/'+id+'/user-roles/list';
    	t.load();
    }
});