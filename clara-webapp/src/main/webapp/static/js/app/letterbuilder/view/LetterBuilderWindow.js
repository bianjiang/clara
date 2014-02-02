Ext.define('Clara.LetterBuilder.view.LetterBuilderWindow', {
    extend: 'Ext.window.Window',
    requires:['Clara.LetterBuilder.ux.RecipientField'],
    alias: 'widget.letterbuilderwindow',
    title: 'Send Message',
    width:880,
    height:520,
    modal:true,
    iconCls:'icn-mail',
    layout: {
        type: 'border'
    },
    
    templateId: null,
    requireSignature: false,
    parentMessageId: null,
    message: null,
    action:null,
    sendToValues: [],
    sendCCValues: [],
    onSuccess: function() { clog('LetterBuilderWindow: Success!'); },
    delaySend: false,
    delaySendMessage: null,
    
    initComponent: function() {
    	var me = this,
    	    controller = Clara.Application.getController('Clara.LetterBuilder.controller.LetterBuilder');
    	
    	controller.beforeInit(me);
    	
    	me.buttons = [{
			text : 'Cancel',
			handler : function() {
				me.close();
			}
		},
		{
			text : 'Sign and Send',
			id:'btnSignAndSend'
		}];
    	
    	me.listeners = {
    			close: function(){
    				Clara.Application.getController('Clara.LetterBuilder.controller.LetterBuilder').onWindowClose();
    			}
    	};
    	
    	me.items = [{
    		xtype:'form',
    		border:0,
    		bodyPadding:6,
    		layout: 'anchor',
    	    defaults: {
    	        anchor: '100%',
    	        	labelWidth:55
    	    },
    		split:true,
    		region:'center',
    		style:'border-right:1px solid #9abde7',
    		items:[{
    			fieldLabel:'To',
    			anchor : '100%',
    			xtype:'recipientcombofield',
    			id:'fldMessageTo',
    			value:me.sendToValues
    		},
    		{
    			fieldLabel:'CC',
    			anchor : '100%',
    			xtype:'recipientcombofield',
    			id:'fldMessageCC',
    			value:me.sendCCValues
    		},
    		{
    			fieldLabel:'Subject',
    			anchor : '100%',
    			xtype:'textfield',
    			value:(typeof me.template != "undefined")?me.template.realSubject:"",
    			id:'fldMessageSubject'
    				
    		}, {
				xtype : 'htmleditor',
				height : 250,
				anchor : '100%',
				fieldLabel : 'Notes',
				value : '',
				id : 'fldMessageBody'
					
			}]
    	},{
    		
        	xtype:'panel',
        	region:'east',
        	width:420,
        	border:false,
        	layout:'fit',
        	split:true,
        	title:'Message Preview',
        	items:[{
				xtype : 'htmleditor',
				anchor : '100%',
				border:false,
				readOnly: true,
				hideLabel:true,
				enableFont: false,
				enableFontSize: false,
				enableFormat: false,
				enableLinks: false,
				enableLists: false,
				enableSourceEdit: false,
				enableColors: false,
		        enableAlignments: false,
				ctCls:'mailContainer',
				value : me.template.templateContent,
				id : 'fldMessageTemplate'
			}]
        
    	},{
    		xtype:'form',
    		region:'south',
    		border:0,
    		bodyPadding:6,
    		title:'Please enter your username and password before sending',
    		items:[{xtype:'textfield',fieldLabel:'Username', id:'fldMessageUsername'},
    		       {xtype:'textfield',fieldLabel:'Password',inputType:'password', id:'fldMessagePassword'}]
    	}];

    	me.callParent();
    	
    }
});

