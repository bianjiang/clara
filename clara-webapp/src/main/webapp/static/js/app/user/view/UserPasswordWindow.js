Ext.define('Clara.User.view.UserPasswordWindow', {
	extend: 'Ext.window.Window',
	alias: 'widget.userpasswordwindow',
	border:false,
	layout: 'form',
	width:450,
	height:280,
	padding: 6,
    modal: true,


    initComponent: function() {
    	var t = this;

    	this.listeners = {
    			close:function(){

    			}
    	};
		this.buttons = [
		    {
		    	id: 'btnChange',
		        text: 'Change',
		        handler: function(){

		        	var verify = true;
	                var win = this.ownerCt.ownerCt;
	                var url = appContext+"/ajax/users/changepasswordfromuser";
	                win.items.each(function(item){
	                    verify = verify && item.validate();
	                });
		        	
		        	if (verify == true && Ext.getCmp("fldNewPassword1").getValue() == Ext.getCmp("fldNewPassword2").getValue()){
	
		        		jQuery.ajax({
		  				  type: 'POST',
		  				  async:false,
		  				  url: url,
		  				  data: {
		  					username:Ext.getCmp("fldUsername").getValue(),
		  					oldPassword:Ext.getCmp("fldOldPassword").getValue(),
		  					newPassword:Ext.getCmp("fldNewPassword1").getValue(),
		  				  },
		  				  success: function(){
		  					win.close();  
		  				  },
		  				  error: function(x,t,e){
		  					 alert("Error changing password");
		  					  clog("ERROR",x,t,e);
		  				  }
		        		});
		        		
		        	} else {
		        		alert("Missing value or new passwords did not match. Check fields and try again.");
		        	}

		        }
		    }
		];
        this.items = [
        {
        	xtype:'displayfield',
        	hideLabel:true,
        	value:'NOTE: This will only change your Clara password and is intended for <span style="font-weight:800;">Off-Campus Users Only.</span> If you have a UAMS userame and wish to change your password, contact UAMS IT Support.'
        },
				{
				    xtype: 'textfield',
				    id:'fldUsername',
				    fieldLabel: 'Username',
				    allowBlank:false,
				    anchor: '100%'
				},
				{
				    xtype: 'textfield',
				    id:'fldOldPassword',
				    fieldLabel: 'Current Password',
				    inputType:'password',
				    allowBlank:false,
				    anchor: '100%'
				},
				{
				    xtype: 'textfield',
				    id:'fldNewPassword1',
				    inputType:'password',
				    fieldLabel: 'New Password',
				    allowBlank:false,
				    anchor: '100%'
				},
				{
				    xtype: 'textfield',
				    id:'fldNewPassword2',
				    fieldLabel: 'New Password (again)',
				    inputType:'password',
				    allowBlank:false,
				    anchor: '100%'
				}
        ];
        this.callParent();
    }
});