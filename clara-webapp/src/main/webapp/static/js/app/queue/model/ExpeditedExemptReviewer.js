Ext.define('Clara.Queue.model.ExpeditedExemptReviewer', {
    extend: 'Ext.data.Model',
    fields: [	    {name:'id'},
            	    {name:'userId', mapping:'user.id'},
            	    {name:'roleName', mapping:'role.name'},
            	    {name:'roleIdentifier', mapping:'role.rolePermissionIdentifier'},
            	    {name:'username', mapping:'user.username'},
            	    {name:'email', mapping:'user.person.email'},
            	    {name:'firstname', mapping:'user.person.firstname'},
            	    {name:'lastname', mapping:'user.person.lastname'},
            	    {name:'middlename', mapping:'user.person.middlename'}],
    proxy: {
        type: 'ajax',
        extraParams: {roles:['ROLE_IRB_EXPEDITED_REVIEWER','ROLE_IRB_EXEMPT_REVIEWER']},
        url: appContext + "/ajax/users/list-user-role-by-roles",
        reader: {
            type: 'json'
        }
    }
});