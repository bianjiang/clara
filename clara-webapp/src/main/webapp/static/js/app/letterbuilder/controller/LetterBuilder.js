Ext.define('Clara.LetterBuilder.controller.LetterBuilder', {
    extend: 'Ext.app.Controller',
    models: ['Clara.LetterBuilder.model.Recipient'],
    stores: ['Clara.LetterBuilder.store.Recipients','Clara.LetterBuilder.store.CCRecipients'],
    refs : [ {
		ref : 'letterBuilderWindow',
		selector : 'letterbuilderwindow'
	},{
		ref: 'username',
		selector: '#fldMessageUsername'
	},{
		ref: 'password',
		selector: '#fldMessagePassword'
	}],
   
    loadingMask: new Ext.LoadMask(Ext.getBody(), {msg:"Please wait..."}),
    templateURL: null,
    sendURL: null,
    
    onWindowClose: function(){
    	var me = this;
    	me.templateURL = null;
    	me.sendURL = null;
    },
    
    beforeInit: function(win){
    	var me = this;
    	
    	//load email template
    	
    	if (!me.templateURL) me.templateURL = appContext + '/ajax/'+claraInstance.type+'s/'+claraInstance.id+'/email-templates/'+win.templateId;
    	if (!me.sendURL) me.sendURL = appContext+'/ajax/'+claraInstance.type+'s/'+claraInstance.id+'/email-templates/'+win.templateId+'/send-letter';
    	clog("controller.LetterBuilder.beforeInit",win,me.templateURL);
		jQuery.ajax({
			  type: 'POST',
			  async:false,
			  url: me.templateURL,
			  data:{
				  userId:claraInstance.user.id
			  },
			  success: function(data){
				  win.template = (data.id)?data:data.data;
			  },
			  error: function(){
				  //need to handle errors
				  alert("Error loading message template: "+templateURL);
				  win.destroy();
			  },
			  dataType: 'json'
		});
		
		var userData = {recipients: [], cc:[]};
		

		if (typeof win.template != 'undefined' && win.template != null){
		
			if(win.template.to){
				var to = JSON.parse(win.template.to);
				
				userData.recipients = jQuery.merge(userData.recipients, to);
				for (var i = 0; i <  to.length; i ++){
					
					win.sendToValues.push( to[i].address );
				}
			}
			clog("mail: to.template",win.template,to,userData);
			if(win.template.cc){
				var cc = JSON.parse(win.template.cc);
				userData.cc = jQuery.merge(userData.cc, cc);
				for (var i = 0; i <  cc.length; i ++){							
					win.sendCCValues.push( cc[i].address );
				}
			}
			var recipientStore = Ext.data.StoreManager.lookup('Clara.LetterBuilder.store.Recipients'),
			    ccStore = Ext.data.StoreManager.lookup('Clara.LetterBuilder.store.CCRecipients');
			recipientStore.loadData(userData.recipients);
			ccStore.loadData(userData.cc);
			
			clog("RECIP store",recipientStore);
		}
    },
    
   
    
    cleanHtml: function(html){
		var xhtml = replaceAll(html,"<br>","<br/>");
		clog("cleaning",html,xhtml);
		if (Encoder) return Encoder.cdataWrap(xhtml);
		else {
			cerr("LetterBuilder.js: Need 'encoder.js' on this page to correctly wrap HTML in CDATA");
			return xhtml;
		}
	},
	
	getMessageXml: function(message){
		message.body = this.cleanHtml(message.body);
		if (Encoder) return "<message><to>"+message.to+"</to><cc>"+message.cc+"</cc><subject>"+Encoder.cdataWrap(message.subject)+"</subject><body>"+message.body+"</body></message>";
		else {
			cerr("LetterBuilder.js: Need 'encoder.js' on this page to correctly wrap HTML in CDATA");
			return "";
		}
	},
    
    sendMessage: function(options) {
    	clog("controller.LetterBuilder.sendMessage",options);
    	if (typeof options.action == "undefined" && typeof claraInstance.action == "undefined"){
    		cerr("options.action not defined (also checked claraInstance.action.");
    		if (typeof options.failureCallback != "undefined"){
    			options.failureCallback();
    		}
    	} else {
    		options.action = options.action || claraInstance.action;
    	}
    	if (typeof options.message == "undefined"){
    		cerr("options.message not defined.");
    		if (typeof options.failureCallback != "undefined"){
    			options.failureCallback();
    		}
    	}
    	if (typeof options.successCallback == "undefined"){
    		cwarn("options.successCallback not defined.");
    	}

    	var params = {
    			'userId':claraInstance.user.id || null,
    			'committee':claraInstance.user.committee || null,
    			'note':options.action,
    			'action':options.action,
    			'username': options.message.username || null,
    			'password': options.message.password || null,
    			'xmlData': options.message.xml || null
    		}
    		
    	if (options.message.parentMessageId) params.parentId = options.message.parentMessageId;
    	
    	Ext.Ajax
    			.request({
    				url : options.url,
    				method : 'POST',
    				success : function(response) {
    					var r = JSON.parse(response.responseText);
    					if (r.error == false){
    						options.successCallback();
    					} else {
    						options.failureCallback();
    					}
    				},
    				failure : function(response) {
    					alert("Mail send failed: " + response.status);
    					cwarn(response);
    				},
    				params : params
    			});
    },
    
    init: function() {
    	var me = this;
    	    
    	me.control({
    		'letterbuilderwindow':{
    			
    		},
    		'#btnSignAndSend':{
    			click: function(){
    				var messageWindow = me.getLetterBuilderWindow();
    				if (me.getUsername().getValue() == '' || me.getPassword().getValue() == ''){
    					alert("Please enter your Clara username and password before sending.");
    				} else {
    					var messageToSend = {
							to: Ext.getCmp('fldMessageTo').getValue(),
							cc: Ext.getCmp('fldMessageCC').getValue(),
							subject: Ext.getCmp('fldMessageSubject').getValue(),
							body: Ext.getCmp('fldMessageBody').getValue()
						};	
						
						if (messageWindow.parentMessageId){
							messageToSend.parentMessageId = messageWindow.parentMessageId;
						}
						
						messageToSend.username = Ext.getCmp("fldMessageUsername").getValue();
						messageToSend.password = Ext.getCmp("fldMessagePassword").getValue();												
	
						messageToSend.xml = me.getMessageXml(messageToSend);
						
						clog("claramessage",messageToSend);
						if (messageToSend.subject == "" || messageToSend.body == "") {
							alert("The message subject and body must not be empty.");
						} else {
							if (messageToSend.to == "") {
								alert("You must choose at least one person to send the message to.");
							} else {
								
								if (messageWindow.delayedSend == true){															
									messageWindow.delayedSendObject.metadata = messageWindow.metadata;
									messageWindow.delayedSendObject.message = messageToSend;
									messageWindow.close();
								} else {
								
										me.sendMessage({
											url: me.sendURL,
											action: messageWindow.action || claraInstance.action,
											message: messageToSend,
											successCallback: function(){
												messageWindow.onSuccess();
												messageWindow.close();
											},
											failureCallback: function(){
												cerr("Error sending message");
											}
										});														
									
								}
							}
						}
						
    				}
    			}
    		},
    		'#fldMessageBody':{
    			activate:function(f){
    				clog("ACTIVATE: f",f);
    				var messageWindow = me.getLetterBuilderWindow();
    				var pp = Ext.getCmp("fldMessageTemplate"),
	                    docEl = document.getElementById(f.iframeEl.id).contentWindow.document.body;
	                docEl.addEventListener('keyup', function(e) {
						pp.setValue(Ext.String.format(messageWindow.template.templateContent,f.getValue()));
	                });
    			}
    		}
    	});
    }
    
});