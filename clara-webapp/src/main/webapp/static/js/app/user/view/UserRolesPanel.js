Ext.define('Clara.User.view.UserRolesPanel', {
	extend: 'Ext.grid.Panel',
	alias: 'widget.userrolespanel',
	autoScroll: true,
    border: false,
    stripeRows: true,
    hideHeaders:true,
	initComponent: function() { 
        this.columns = [
            {sortable: true, width:'100%',dataIndex: 'roledesc', id:'user-role',renderer:function(value, p, record){
        		var rdesc = record.data.roledesc;
        		var html = "<h1 class='admin-user-role-row user-role-desc'>"+rdesc+"</h1>";
        		html = html + "<h3 class='admin-user-role-row user-role-depts'>"+((record.data.collegename != null)?record.data.collegename:"");
        		html = html + ((record.data.deptname != null)?(" - "+record.data.deptname):"");
        		html = html +((record.data.subdeptname != null)?(" - "+record.data.subdeptname):"")+"</h3>";
        		// html = html + "<a href='javascript:;' onclick='adminUserRemoveRole("+record.data.id+");'>Remove Role..</a>";
        		return html;
            }}
        ];
		this.store = new Ext.data.Store({
			autoLoad: true,
			header :{
		           'Accept': 'application/json'
		       },
			proxy: new Ext.data.HttpProxy({
				url: appContext + '/ajax/users/'+profile.id+'/user-roles/list',
				method:'GET'
			}),
			reader: {
				type:'json',
				idProperty: 'id'
			}, 
			fields:[
				{name:'id'},
				{name:'rolename', mapping:'role.name'},
				{name:'roleid', mapping:'role.id'},
				{name:'roledesc', mapping:'role.displayName'},
				{name:'roledeptlevel', mapping:'role.departmentLevel'},
				{name:'collegeid', mapping:function(v){ return (v.college)?v.college.id:null;}},
				{name:'collegename', mapping:function(v){ return (v.college)?v.college.name:null;}},
				{name:'deptid', mapping:function(v){ return (v.department)?v.department.id:null;}},
				{name:'deptname',mapping:function(v){ return (v.department)?v.department.name:null;}}, 
				{name:'subdeptid', mapping:function(v){ return (v.subDepartment)?v.subDepartment.id:null;}},
				{name:'subdeptname', mapping:function(v){ return (v.subDepartment)?v.subDepartment.name:null;}}
			]
		});
		this.callParent();
	}
});