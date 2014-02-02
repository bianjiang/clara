Ext.define('Clara.Common.model.IrbRoster', {
    extend: 'Ext.data.Model',
    fields: [	{name:'reviewerId', mapping:'id', persist:false},
             	{name:'user', convert:function(v,r){
             		
             		
             		var newUser = {
             				accountNonExpired:v.accountNonExpired,
             				accountNonLocked:v.accountNonLocked,
             				concurrentVersion:v.concurrentVersion,
             				credentialsNonExpired:v.credentialsNonExpired,
             				data:v.data,
             				enabled:v.enabled,
             				id:v.id,
             				password:v.password,
             				person: {
             					annualSalary:v.person.annualSalary,
             					concurrentVersion:v.person.concurrentVersion,
             					department:v.person.department,
             					email:v.person.email,
             					firstname:v.person.firstname,
             					id:v.person.id,
             					jobTitle:v.person.jobTitle,
             					lastname:v.person.lastname,
             					middlename:v.person.middlename,
             					retired:v.person.retired,
             					sap:v.person.sap,
             					state:v.person.state,
             					streetAddress:v.person.streetAddress,
             					username:v.person.username,
             					workphone:v.person.workphone,
             					zipCode:v.person.zipCode
             				},
             				profile:v.profile,
             				retired:v.retired,
             				signaturePath:v.signaturePath,
             				trained:v.trained,
             				uploadedFile:v.uploadedFile,
             				userPermissions:v.userPermissions,
             				userType:v.userType,
             				username:v.username,
             		};
             		
             		return newUser;
             		
             	}},
				{name:'userid',mapping:'user.id', persist:false},
				{name:'username', mapping:'user.username', persist:false},
				{name:'alternativeMember'},
				{name:'affiliated'},
				{name:'chair'},
				{name:'expedited'},
				{name:'specialty'},
				{name:'degree'},
				{name:'irbRoster'},
				{name:'comment'},
				{name:'type'}],
    proxy: {
        type: 'ajax',
        url: appContext + '/ajax/rosters/list',
        reader: {
            type: 'json',
			idProperty: 'reviewerId'
        },

		actionMethods: {
		    read: 'GET',
		    update:'POST',
		    create:'POST',
		    destroy:'GET'
		},
		headers:{'Accept':'application/json;charset=UTF-8'},
		writer:{
			 writeAllFields:true,
			 allowSingle:true
		},
		api:{
			 'read':appContext + '/ajax/rosters/list', 
			 'update':appContext + '/ajax/rosters/reviewers/update', 
			 // 'create':appContext + '/ajax/rosters/reviewers/create', 
			 'delete':appContext + '/ajax/rosters/reviewers/delete', 
		}

    }


});