Ext.define('Clara.Login.view.ResetPasswordWindow', {
	extend: 'Ext.window.Window',
	alias: 'widget.resetpasswordwindow',
	layout: 'form',
	title: 'Password',
	modal:true,
	padding: 6,
	bodyPadding:6,
	width: 500,
	initComponent: function() {
		var me = this;
		var loadingMask = new Ext.LoadMask(Ext.getBody(), {msg:"Please wait..."});
		me.buttons = [
		              {
		            	  text: 'Cancel',
		            	  handler: function(){
		            		  me.close();
		            	  }
		              }
		              ];
		me.items = [{
			xtype:'displayfield',
			hideLabel:true,
			value:'<strong style="font-weight:800;">This is for off-campus users only.</strong> Users that log in with UAMS email accounts should contact IT support to reset or change their password.'
		},
		{
			xtype:'fieldset',
			title: 'I forgot my CLARA password',
			items:[{
				xtype:'displayfield',
				hideLabel:true,
				value:'Enter your username and we will send you a new password via email.'
			},
			{
				xtype:'textfield',
				fieldLabel:'Your CLARA Username',
				allowEmpty:false,
				labelStyle: 'width:150px;',
				id:'fldForgot_CLARAUsername'
			},
			{
				xtype:'button',
				text:'Reset password via email',
				handler:function(){
					
					var fld = Ext.getCmp("fldForgot_CLARAUsername");
					if (fld.isValid()){
					
						loadingMask.show();
						Ext.Ajax.request({
							method:'POST',
							url: appContext+"/ajax/users/resetpassword",
							params: {username:fld.getValue()},
							success: function(response){
								clog('reset password: Ext.Ajax success',response);
								loadingMask.hide();
								alert("An email has been sent. Check your email and sign in with the provided password.");
			  					me.close();
							},
							failure: function(error) {
								alert("Error resetting password. Please try again in a few minutes.");
								clog('reset password: Ext.Ajax failure',error);
								loadingMask.hide();
							}
						});
						
						
					}else {
						alert("Enter a username.");
					}
					
				}
			}
			]
		},{

			xtype:'fieldset',
			title: 'I want to change my CLARA password',
			items:[{
				xtype:'displayfield',
				hideLabel:true,
				value:'Enter the information below to change your password.'
			},
			{
				xtype:'textfield',
				fieldLabel:'Your CLARA Username',
				allowEmpty:false,
				labelStyle: 'width:150px;',
				id:'fldChange_CLARAUsername'
			},
			{
				xtype:'textfield',
				fieldLabel:'Your old CLARA password',
				inputType:'password',
				allowEmpty:false,
				labelStyle: 'width:150px;',
				id:'fldChange_OldPass'
			},
			{
				xtype:'textfield',
				fieldLabel:'New CLARA password',
				inputType:'password',
				allowEmpty:false,
				labelStyle: 'width:150px;',
				id:'fldChange_NewPass1'
			},
			{
				xtype:'textfield',
				fieldLabel:'New CLARA password (again)',
				inputType:'password',
				allowEmpty:false,
				labelStyle: 'width:150px;',
				id:'fldChange_NewPass2'
			},
			{
				xtype:'button',
				text:'Save Changes',
				handler:function(){

					var fldChange_CLARAUsername = Ext.getCmp("fldChange_CLARAUsername");
					var fldChange_OldPass = Ext.getCmp("fldChange_OldPass");
					var fldChange_NewPass1 = Ext.getCmp("fldChange_NewPass1");
					var fldChange_NewPass2 = Ext.getCmp("fldChange_NewPass2");

					if (!fldChange_CLARAUsername.isValid()){
						alert("Enter a username.");
					} else if (!fldChange_OldPass.isValid()){
						alert("Enter your old password.");
					} else if (!fldChange_NewPass1.isValid()){
						alert("Enter a new password.");
					} else if (!fldChange_NewPass2.isValid()){
						alert("Retype the new password.");
					} else if (fldChange_NewPass1.getValue() != fldChange_NewPass2.getValue()){
						alert("New passwords don't match.");
					} else {

						loadingMask.show();
						Ext.Ajax.request({
							method:'POST',
							url: appContext+"/ajax/users/changepasswordfromuser",
							params: {
								username:fldChange_CLARAUsername.getValue(),
								oldPassword:fldChange_OldPass.getValue(),
								newPassword:fldChange_NewPass1.getValue()
							},
							success: function(r){
								clog('reset password: Ext.Ajax success',r);
								loadingMask.hide();
								var data = Ext.decode(r.responseText);
								if (!data.error){
									alert("Password changed. You can log in with your new password.");
									me.close();
								}
			  					
							},
							failure: function(error) {
								alert("Error changing password. Please try again in a few minutes.");
								clog('reset password: Ext.Ajax failure',error);
								loadingMask.hide();
							}
						});
					}

				}
			}
			]

		}

		];

		me.callParent();
	}
});