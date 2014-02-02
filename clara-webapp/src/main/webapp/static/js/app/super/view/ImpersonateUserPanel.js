Ext.define('Clara.Super.view.ImpersonateUserPanel', {
    extend: 'Ext.form.Panel',
    alias: 'widget.impersonateUserPanel',
    title: 'Impersonate',
    layout: 'anchor',
    bodyPadding:6,
    width: 350,
    standardSubmit:true,
    method:'POST',
    defaultType: 'textfield',
    items: [{
        fieldLabel: 'Username',
        name: 'username',
        allowBlank: false
    },{
    	xtype: 'button',
        text : 'Swith To',
        listeners: {
            click: function() {
            	var form = this.up('form').getForm();
            	if(form.isValid()){
            		var username = this.up('form').getForm().getFieldValues()['username'];
            		window.location = "/clara-webapp/super/j_spring_security_switch_user?j_username=" + username;
            	}
            }
        }
    },{
    	xtype: 'button',
        text : 'Reset',
        listeners: {
            click: function() {
                // this == the button, as we are in the local scope
                window.location = "/clara-webapp/super/j_spring_security_exit_user";
            }
        }
    }],
    initComponent: function() {
    	
        this.callParent();
    }
});