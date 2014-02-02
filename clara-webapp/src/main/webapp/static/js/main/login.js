Ext.ns('Clara');
Ext.QuickTips.init();

Clara.ShowWindowWithContentFromElement = function(elId, title){
	
	var html = jQuery("#"+elId).html();
	
	title = title || '';
	new Ext.Window({
		modal:true,
		padding:6,
		bodyStyle:'font-size:14px;',
		html:html,
		title:title,
		width:600,
		height:400
	}).show();
};

Clara.ChangePasswordWindow = Ext.extend(Ext.Window, {
	id:'winChangePassword',
	title: 'Password',
	width: 500,
	layout: 'form',
	border:true,
	padding:6,
	initComponent: function() {
		var t = this;

		this.buttons = [{
			id:'btnCloseWindow',
			text:'Cancel',
			handler: function(){
				t.close();
			}
		}];

		this.items = [
		               {
		            	  xtype:'displayfield',
		            	  hideLabel:true,
		            	  value:'<strong style="font-weight:800;">This is for off-campus users only.</strong> Users that log in with UAMS email accounts should contact IT support to reset or change their password.'
		              },{
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
		            				  if (fld.isValid() && jQuery.trim(fld.getValue()) != ""){
		            					  jQuery.ajax({
							  				  type: 'POST',
							  				  async:false,
							  				  url: appContext+"/ajax/users/resetpassword",
							  				  data: {
							  					username:fld.getValue()
							  				  },
							  				  success: function(){
							  					alert("An email has been sent. Check your email and sign in with the provided password.");
							  					t.close();
							  				  },
							  				  error: function(x,t,e){
							  					 alert("Error resetting password. Please try again in a few minutes.");
							  					 clog("ERROR",x,t,e);
							  				  }
							        		});
		            				  }else {
		            					  alert("Enter a username.");
		            				  }
		            			  }
		            	  }
		            	  ]
		              },
		              {
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
		            				  
		            				  if (!fldChange_CLARAUsername.isValid() || jQuery.trim(fldChange_CLARAUsername.getValue()) == ""){
		            					  alert("Enter a username.");
		            				  } else if (!fldChange_OldPass.isValid() || jQuery.trim(fldChange_OldPass.getValue()) == ""){
		            					  alert("Enter your old password.");
		            				  } else if (!fldChange_NewPass1.isValid() || jQuery.trim(fldChange_NewPass1.getValue()) == ""){
		            					  alert("Enter a new password.");
		            				  } else if (!fldChange_NewPass2.isValid() || jQuery.trim(fldChange_NewPass2.getValue()) == ""){
		            					  alert("Retype the new password.");
		            				  } else if (fldChange_NewPass1.getValue() != fldChange_NewPass2.getValue()){
		            					  alert("New passwords don't match.");
		            				  } else {
		            					  var url = appContext+"/ajax/users/changepasswordfromuser";
		            					  jQuery.ajax({
		            		  				  type: 'POST',
		            		  				  async:false,
		            		  				  url: url,
		            		  				  data: {
		            		  					username:fldChange_CLARAUsername.getValue(),
		            		  					oldPassword:fldChange_OldPass.getValue(),
		            		  					newPassword:fldChange_NewPass1.getValue(),
		            		  				  },
		            		  				  success: function(r){
		            		  					if (!r.error){
		            		  						alert("Password changed. You can log in with your new password.");
		            		  						t.close();  
		            		  					}
		            		  					
		            		  				  },
		            		  				  error: function(x,t,e){
		            		  					 alert("Error changing password. Try again in a few minutes.");
		            		  					  clog("ERROR",x,t,e);
		            		  				  }
		            		        		});
		            				  }
	
		            			  }
		            	  }
		            	  ]
		              }
		              ];

		Clara.ChangePasswordWindow.superclass.initComponent.call(this);
	}
});
