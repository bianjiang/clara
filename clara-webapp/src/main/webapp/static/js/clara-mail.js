// Clara.Mail.MessageWindow
Ext.ns('Clara.Mail');


Clara.Mail.Message = function(o){ 
	this.to =	(o.to || '');
	this.cc =	(o.cc || '');
	this.bcc =	(o.bcc || '');
	this.subject =	(o.subject || '');
	this.body =	(o.body || '');
	this.parentMessageId = (o.parentMessageId || null);
};


Clara.Mail.SendMessage = function(action, message, successCallback) {
	
	clog(message);
	// protocolMetaData: pass  object (to relate
	// message to a protocol/form/version)
	// use {} if no protocolMetadata
	// from: string (email or clara userid that system uses to look up email
	// address/logging)
	// to: comma delimited string of email addresses
	// subject: string
	// body: string
	// successCallback: called if send was successful

	var url = (typeof message.sendUrl != "undefined")?message.sendUrl:(appContext+'/ajax/mail/sendmail');
	
	Ext.Ajax
			.request({
				url : url,
				method : 'POST',
				success : function() {
					successCallback();
				},
				failure : function(response) {
					alert("Mail send failed: " + response.status);
					clog(response);
				},
				params : {
					'committee' : claraInstance.user.committee,
					'action' : claraInstance.action,
					'protocolId' : claraInstance.id,
					'protocolFormId' : claraInstance.form.id,
					'protocolFormType' : claraInstance.form.type,
					'protocolFormXmlDataId' : claraInstance.form.xmlDataId,
					'subject' : message.subject,
					'to' : message.to,
					'cc' : message.cc,
					'bcc' : message.bcc,
					'from' : message.from,
					'body' : message.body,
					'parentMessageId':message.parentMessageId,
					'username': message.username || null,
					'password': message.password || null
				}
			});
};


Clara.Mail.SignAndSubmit = function(metadata, message, successCallback) {
	
	clog(message);
	// protocolMetaData: pass  object (to relate
	// message to a protocol/form/version)
	// use {} if no protocolMetadata
	// from: string (email or clara userid that system uses to look up email
	// address/logging)
	// to: comma delimited string of email addresses
	// subject: string
	// body: string
	// successCallback: called if send was successful

	var url = (typeof message.sendUrl != "undefined")?message.sendUrl:(appContext+'/ajax/mail/sendmail');
	
	var params = {
			'userId':claraInstance.user.id || null,
			'committee':claraInstance.user.committee || null,
			'note':(metadata)?metadata.action:null,
			'action':(metadata)?metadata.action:(claraInstance.action || null),
			'username': message.username || null,
			'password': message.password || null,
			'xmlData': message.xml || null
		}
		
	if (message.parentMessageId) params.parentId = message.parentMessageId;
	
	Ext.Ajax
			.request({
				url : url,
				method : 'POST',
				success : function(response) {
					var r = JSON.parse(response.responseText);
					if (r.error == false){
						successCallback();
					} else {
						alert("Error sending Mail: "+r.message);
					}
					
				},
				failure : function(response) {
					alert("Mail send failed: " + response.status);
					clog(response);
				},
				params : params
			});
};



Clara.Mail.MessageWindow = Ext
		.extend(
				Ext.Window,
				{
					title : 'Send Message',
					iconCls:'icn-mail-open',
					width : 880,
					height : 450,
					layout : 'border',
					padding : 6,
					template: {},
					metadata : {},
					action: null,
					requireSignature:false,
					parentMessageId:null,
					message: {},
					onSuccess:null,
					delayedSend:false,
					delayedSendObject:{},
					userStore: {},
					sendUrl:'',
					templateUrl:'',
					sendFunction: Clara.Mail.SendMessage,
					cleanHtmlMessage: function(html){
						
						var xhtml = replaceAll(html,"<br>","<br/>");
						clog("cleaning",html,xhtml);
						if (Encoder) return Encoder.cdataWrap(xhtml);
						else {
							cerr("clara-mail.js: Need 'encoder.js' on this page to correctly wrap HTML in CDATA");
							return xhtml;
						}
					},
					getMessageXml: function(message){
						if(typeof message == "undefined"){
							message = this.message;
						}
						message.body = this.cleanHtmlMessage(message.body);
						return "<message><to>"+message.to+"</to><cc>"+message.cc+"</cc><subject>"+Encoder.cdataWrap(message.subject)+"</subject><body>"+message.body+"</body></message>";
					},
					messageBody:'',
					initComponent : function() {
						var t = this;
			
							clog("metadata",t.metadata);
							if (!t.action || !claraInstance.user.committee){
								cwarn("WARNING displaying message window: No action or committee selected.");
							}
							t.userStore = new Ext.data.JsonStore({
								url : appContext + '/ajax/email-templates/recipients/search', // '/ajax/mailusersearch',
								autoLoad : false,
								root : 'recipients',
								fields : [ 'type', 'desc', 'address' ]
							});
	
							
							t.buttons = [
											{
												text : 'Cancel',
												handler : function() {
													t.close();
												}
											},
											{
												text : (t.requireSignature)?'Sign and Send':'Save and Close',
												id:'btnSendLetter',
												handler : function() {
													
													if (t.requireSignature && (Ext.getCmp("fldMailUsername").getValue() == "" || Ext.getCmp("fldMailPassword").getValue() == "")){
														alert("Please enter your Clara username and password before sending.");
													} else {
														var message = new Clara.Mail.Message({
															to: Ext.getCmp('fldMessageTo').getValue(),
															cc: Ext.getCmp('fldMessageCC').getValue(),
															subject: Ext.getCmp('fldMessageSubject').getValue(),
															body: Ext.getCmp('fldMessageBody').getValue()
														});	
														
														if (t.parentMessageId){
															message.parentMessageId = t.parentMessageId;
														}
														
														if (t.sendUrl != ''){
															message.sendUrl = t.sendUrl;
														}
														
														if (t.requireSignature){
															message.username = Ext.getCmp("fldMailUsername").getValue();
															message.password = Ext.getCmp("fldMailPassword").getValue();												
														}
														
														message.xml = t.getMessageXml(message);
														
														
														clog("claramessage",message);
														if (message.subject == ""
																|| message.messagebody == "") {
															alert("The message subject and body must not be empty.");
														} else {
															if (message.to == "") {
																alert("You must choose at least one person to send the message to.");
															} else {
																
																if (t.delayedSend == true){															
																	
																	t.delayedSendObject.metadata = t.metadata;
																	t.delayedSendObject.message = message;
																	
																	t.close();
																} else {
																																	
																		t.sendFunction( t.action, message, function() { 
																			if (t.onSuccess != null) t.onSuccess();
																			t.close();
																		});
																	
																}
															}
														}
													}
												}
											} ];
							
							var templateurl = t.templateUrl;
							
							clog(templateurl);
		
				
						
							//load email template
							jQuery.ajax({
								  type: 'POST',
								  async:false,
								  url: templateurl,
								  data:{
									  //id: 510,
									  userId:claraInstance.user.id
								  },
								  success: function(data){
									  t.template = (data.id)?data:data.data;
								  },
								  error: function(){
									  //need to handle errors
									  alert("Error loading message template: "+templateurl);
									  t.destroy();
								  },
								  dataType: 'json'
							});
							
							var userData = {recipients: []};
							
							var toString = "";	
							
							if (typeof t.template != 'undefined' && t.template != null){
							
							if(t.template.to){
								clog("mail: to.template",t.template);
								var to = JSON.parse(t.template.to);
								userData.recipients = jQuery.merge(userData.recipients, to);
								for (var i = 0; i <  to.length; i ++){
									
									toString += to[i].address + ",";
								}
							}
							
							var ccString ="";
							if(t.template.cc){
								var cc = JSON.parse(t.template.cc);
								userData.recipients = jQuery.merge(userData.recipients, cc);
								for (var i = 0; i <  cc.length; i ++){							
									ccString += cc[i].address + ",";
								}
							}
							
							t.userStore.loadData(userData);
							
							
							t.items = [ 
							            {	
							            	xtype:'panel',
							            	region:'east',
							            	width:420,
							            	border:false,
							            	layout:'border',
							            	split:true,
							            	items:[{xtype:'panel',border:false,unstyled:true,html:'Message Preview',height:32,region:'north',style:'border-left:1px solid #9abde7;border-bottom:1px solid #999;background:#eee;color:#666;padding:6px;font-size:15px;font-weight:100;'},{
												xtype : 'htmleditor',
												anchor : '100%',
												region:'center',
												width:420,
												border:false,
												readOnly: true,
												hideLabel:true,
												
												ctCls:'mailContainer',
												value : t.template.templateContent,
												id : 'fldMessageTemplate',
												listeners:{
													afterrender:function(mt){
														// Replace notes placeholder
														clog("fldMessageTemplate: afterrender",mt,t.template);
														mt.setValue(String.format(t.template.templateContent,Ext.getCmp("fldMessageBody").getValue()));
														mt.iframePad = 6;
														mt.wrap.addClass("mailTemplateWrap");
														mt.getToolbar().hide();
													}
												}
											}]
							            },
							           
							            
							            {
							            	xtype:'form',
							            	region:'center',
							            	labelWidth:55,
							            	padding:6,
							            	border:false,
							            	style:'border-right:1px solid #9abde7',
							            	items:[{
												xtype : 'superboxselect',
												anchor : '100%',
												fieldLabel : 'To',
												store : t.userStore,
												displayField : 'desc',
												displayFieldTpl : '{desc}',
												valueField : 'address',
												forceSelection : true,
												value : toString,
												id : 'fldMessageTo',
												emptyText: 'Enter or select the email recipients...',
												allowEmpty : false
											}, {
												xtype : 'superboxselect',
												anchor : '100%',
												store : t.userStore,
												displayField : 'desc',
												displayFieldTpl : '{desc}',
												valueField : 'address',
												forceSelection : true,
												fieldLabel : 'CC',
												value : ccString,
												emptyText: 'Enter or select the email recipients...',
												id : 'fldMessageCC',
												allowEmpty : false
											},{
												xtype : 'textfield',
												anchor : '100%',
												fieldLabel : 'Subject',
												value : t.template.realSubject || t.template.subject,
												id : 'fldMessageSubject',
												emptyText: 'Enter the email title...',
												allowEmpty : false
											} , {
												xtype : 'htmleditor',
												height : 250,
												anchor : '100%',
												fieldLabel : 'Notes',
												value : t.messageBody,
												id : 'fldMessageBody',
												emptyText: 'Enter the email content...',
												listeners:{'activate': function(f) {
													var pp = Ext.getCmp("fldMessageTemplate");
									                var docEl = Ext.get(f.iframe.contentWindow.document.body);
									                docEl.on('keyup', function(e) {
														pp.setValue(String.format(t.template.templateContent,f.getValue()));
									                });
									            }}
													
											}]
							            }
							            
							            
	
							];				
	
							
							
							Clara.Mail.MessageWindow.superclass.initComponent
									.call(this);
							
							if (t.requireSignature){
								t.add({
									xtype:'form',
									frame:true,
									title:'Please enter your username and password before sending this letter',
									region:'south',
									height:100,
									padding:2,
									items:[{
										xtype:'textfield',
										id:'fldMailUsername',
										fieldLabel:'Username'
									},{
										xtype:'textfield',
										id:'fldMailPassword',
										fieldLabel:'Password',
										inputType:'password'
									}]
								});
								t.height = 520;
							}
						
						} else {
						alert("Oops! Something's wrong with the mail window.");
						cwarn("Clara.Mail: t.template error",t.template);
						t.destroy();
					}
					}
				});
