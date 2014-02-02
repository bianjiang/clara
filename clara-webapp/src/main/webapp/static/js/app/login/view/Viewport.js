Ext.define('Clara.Login.view.Viewport',{
	extend: 'Ext.container.Viewport',
	border:false,
	requires:['Clara.Login.view.ResetPasswordWindow'],
	layout:'fit',
	bodyStyle: 'background:#333;',
	initComponent: function(){
		var me = this;
		me.listeners = {
			afterrender:function(v){
				Ext.getCmp("j_username").focus();
			}	
		};
		me.items =[{
				xtype:'panel',
				bodyStyle: 'background:#333; color:white;',
				border:false,
				layout: {
				       type: 'vbox',
				       align: 'center',
				       pack:'center'
				     },
				items:[{
					    	xtype:'container',
					    	html:'<img src="'+appContext+'/static/images/login-logo.png"/>',
					    	height:120,
					    	width:400
					    },{
					        xtype: 'form',
					        padding:12,
					        width:344,
					        title: 'Log in using your UAMS account',
					        frame:true,
					        buttons:[{
					        	text:'Reset password',
					        	handler:function(){
					        		Ext.create("Clara.Login.view.ResetPasswordWindow",{}).show();
					        	}
					        },{
					        	text:'Don\'t have an account?',
					        	handler:function(){
					            	Ext.Msg.show({
					            		title:"How do I get an account for Clara?",
					            		msg:'Please submit a <a target="_blank" href="' + appContext + '/static/docs/CLARA_Account_Request.doc">CLARA Account Request</a> to the IRB (irb@uams.edu).',
					            		buttons: Ext.Msg.OK
					            	});
					            }
					        },{
					        	text:'Log in',
					        	handler: function(){
					        		var formpanel = this.up("form");
					        		formpanel.getForm().standardSubmit=true;
					        		formpanel.getForm().submit({
					        		   url: appContext+"/j_spring_security_check",
					        		   standardSubmit: true,
					        		   method: 'POST'
					        		});
					        	}
					        }],
					        items:[{
						        fieldLabel: 'Username',
						        name: 'j_username',
						        id:'j_username',
						        labelWidth: 70,
						        width:310,
						        emptyText:'Your UAMS username',
						        allowBlank: false,
						        flex:1,
						        xtype:'textfield'
						    },{
						        fieldLabel: 'Password',
						        name: 'j_password',
						        inputType:'password',
						        labelWidth: 70,
						        width:310,
						        allowBlank: false,
						        flex:1,
						        xtype:'textfield',
						        enableKeyEvents:true,
						        listeners:{
						        	keypress: function(f,e){
						        		if (e.getCharCode() == Ext.EventObject.ENTER){
						        			var formpanel = this.up("form");
							        		formpanel.getForm().standardSubmit=true;
							        		formpanel.getForm().submit({
							        		   url: appContext+"/j_spring_security_check",
							        		   standardSubmit: true,
							        		   method: 'POST'
							        		});
						        		}
						        	}
						        }
						    }]
					    },{
					    	xtype:'container',
					    	contentEl:'login-message',
					    	style:'margin-top:16px;margin-bottom:16px;text-align:center;'
					    },{

							xtype:'gridpanel',
							hideHeaders:true,
							store:'Clara.Common.store.MessagePosts',
							collapsible:true,
							border:false,
							iconCls:'icn-newspaper',
							title:'CLARA News',
							width:800,
							columns:[{
								header:'',
								dataIndex: 'message',
								flex: 1,
								renderer:function(v,p,r){
									return "<div class='news-post wrap'><h1>"+r.get("title")+"</h1><div class='news-post-message'>"+r.get("message")+"</div></div>";
								}
							}]
						
					    },{
					    	xtype:'container',
					    	html:'<img src="'+appContext+'/static/images/uams-logo.png"/>',
					    	height:85,width:134
					    }]
			}];


			me.dockedItems =[{
				dock: 'top',
				xtype:'container',
				id:'browserWarningPanel',
				contentEl:'ie-login-message',
				style:'text-align:center;',
				hidden:true,
				border:false
			},{
				dock: 'bottom',
				xtype:'container',
				id:'loginFooter',
				border:false,
				contentEl:'login-footer',
				style:'text-align:center;'
			}];
						
						
		
				
		
		this.callParent();

		
	}

});