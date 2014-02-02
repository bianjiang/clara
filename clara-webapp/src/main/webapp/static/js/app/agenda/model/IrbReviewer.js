Ext.define('Clara.Agenda.model.IrbReviewer', {
	extend : 'Ext.data.Model',

	fields : [ {
		name : 'id'
	}, {
		name : 'status'
	}, {
		name : 'reason',
		mapping : 'reason'
	}, {
		name : 'irbreviewerid',
		mapping : 'irbReviewer.id'
	}, {
		name : 'userid',
		mapping : 'irbReviewer.user.id'
	}, {
		name : 'username',
		mapping : 'irbReviewer.user.username'
	},

	{
		name : 'user',
		mapping : 'irbReviewer.user',
		convert : function(v, r) {

			var newUser = {
				accountNonExpired : v.accountNonExpired,
				accountNonLocked : v.accountNonLocked,
				concurrentVersion : v.concurrentVersion,
				credentialsNonExpired : v.credentialsNonExpired,
				data : v.data,
				enabled : v.enabled,
				id : v.id,
				password : v.password,
				person : {
					annualSalary : v.person.annualSalary,
					concurrentVersion : v.person.concurrentVersion,
					department : v.person.department,
					email : v.person.email,
					firstname : v.person.firstname,
					id : v.person.id,
					jobTitle : v.person.jobTitle,
					lastname : v.person.lastname,
					middlename : v.person.middlename,
					retired : v.person.retired,
					sap : v.person.sap,
					state : v.person.state,
					streetAddress : v.person.streetAddress,
					username : v.person.username,
					workphone : v.person.workphone,
					zipCode : v.person.zipCode
				},
				profile : v.profile,
				retired : v.retired,
				signaturePath : v.signaturePath,
				trained : v.trained,
				uploadedFile : v.uploadedFile,
				userPermissions : v.userPermissions,
				userType : v.userType,
				username : v.username,
			};

			return newUser;

		}
	}, {
		name : 'altuser',
		type : 'auto',
		mapping : 'alternateIRBReviewer',
		convert : function(obj, r) {

			if (obj) {
				v = obj.user;
				var newUser = {
					accountNonExpired : v.accountNonExpired,
					accountNonLocked : v.accountNonLocked,
					concurrentVersion : v.concurrentVersion,
					credentialsNonExpired : v.credentialsNonExpired,
					data : v.data,
					enabled : v.enabled,
					id : v.id,
					password : v.password,
					person : {
						annualSalary : v.person.annualSalary,
						concurrentVersion : v.person.concurrentVersion,
						department : v.person.department,
						email : v.person.email,
						firstname : v.person.firstname,
						id : v.person.id,
						jobTitle : v.person.jobTitle,
						lastname : v.person.lastname,
						middlename : v.person.middlename,
						retired : v.person.retired,
						sap : v.person.sap,
						state : v.person.state,
						streetAddress : v.person.streetAddress,
						username : v.person.username,
						workphone : v.person.workphone,
						zipCode : v.person.zipCode
					},
					profile : v.profile,
					retired : v.retired,
					signaturePath : v.signaturePath,
					trained : v.trained,
					uploadedFile : v.uploadedFile,
					userPermissions : v.userPermissions,
					userType : v.userType,
					username : v.username,
				};

				return newUser;
			} else
				return obj;

		}
	},

	{
		name : 'alternativeMember',
		mapping : 'irbReviewer.alternativeMember'
	}, {
		name : 'affiliated',
		mapping : 'irbReviewer.affiliated'
	}, {
		name : 'degree',
		mapping : 'irbReviewer.degree'
	}, {
		name : 'irbRoster',
		mapping : 'irbReviewer.irbRoster'
	}, {
		name : 'comment',
		mapping : 'irbReviewer.comment'
	}, {
		name : 'type',
		mapping : 'irbReviewer.type'
	} ],
	proxy : {
		type : 'ajax',
		url : appContext + "/ajax/agendas/XXX/agenda-irb-reviewers/list", // DYNAMIC
		reader : {
			type : 'json',
			idProperty : 'id'
		},
		headers : {
			'Accept' : 'application/json;charset=UTF-8'
		}
	}
});