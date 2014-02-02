Ext.ns('Clara.Reviewer');

Clara.Reviewer.ReviewSignPanel = Ext.extend(Ext.Panel, {
    title: 'Sign this review',
    renderTo:'sign-review-form',
    width: 542,
    height: 78,
    frame: true,
    layout: 'absolute',
    initComponent: function() {
        this.items = [
            {
                xtype: 'textfield',
                style: 'font-size:15px;',
                id:'fldReviewSignUsername',
                name:'fldReviewSignUsername',
                x: 90,
                y: 10,
                width: 160
            },
            {
                xtype: 'textfield',
                style: 'font-size:15px;',
                id:'fldReviewSignPassword',
                name:'fldReviewSignPassword',
                inputType:'password',
                x: 360,
                y: 10,
                width: 160
            },
            {
                xtype: 'label',
                text: 'Username',
                style: 'font-size:15px;',
                x: 10,
                y: 10
            },
            {
                xtype: 'label',
                text: 'Password',
                x: 290,
                y: 10,
                style: 'font-size:15px;'
            }
        ];
        Clara.Reviewer.ReviewSignPanel.superclass.initComponent.call(this);
    }
});


Clara.Reviewer.GetFinalReviewXml =  function(metadata, message){
	
	var xmlData = "";
	if (metadata && metadata.action && metadata.committee){
		var msgXml = (typeof message == 'undefined')?"":message.xml;
		xmlData = "<committee-review><committee type=\""+metadata.committee +"\"><extra-content>";
		if ( Ext.getCmp("finalreviewpanel") && Ext.getCmp("finalreviewpanel").getXML()){
			xmlData += Ext.getCmp("finalreviewpanel").getXML();
		}
		xmlData += "</extra-content><actor>"+metadata.committee+"</actor>";
		xmlData += "<action>"+metadata.action+"</action>";
		xmlData += "<letter>"+msgXml+"</letter>";
		xmlData += "</committee></committee-review>";
	}
	return xmlData;
};

function signReviewForm(url, formXml, username, password, action, note){
	clog("DEPRECATED: Use Clara.Reviewer.SignReviewForm instead.");
	return Clara.Reviewer.SignReviewForm(url, formXml, username, password, action, note);
}

Clara.Reviewer.SignReviewForm = function(url, formXml, username, password, action, note){
	var valid = false;
	jQuery.ajax({
		type: 'POST',
		async: false,
		url: url, 
		dataType: 'xml',
		data: {
			committee: claraInstance.user.committee,
			userId: claraInstance.user.id,
			username: username,
			password: password,
			xmlData:formXml,
			action: action,
			note: note?note:''
		},
		success: function(response){
			valid = true;
		},
		error: function(XMLHttpRequest, textStatus, errorThrown){
			valid = !checkAjaxError(null,XMLHttpRequest, null);
			//valid = false;
		}
	});
	return valid;
};

function showEmailTemplate(action,delayedSendObject){
	if (action) claraInstance.action = action;
	var note = jQuery("#committee_notes").val();
	var templateUrl = appContext + '/ajax/'+claraInstance.type+'s/'+claraInstance.id+'/'+claraInstance.type+'-forms/'+claraInstance.form.id+'/email-templates/'+claraInstance.user.committee+'/'+claraInstance.action;
	
	//clog("delayedSendObject: " + delayedSendObject);
	var message = new Clara.Mail.MessageWindow({templateUrl:templateUrl,messageBody:note,delayedSend:true,delayedSendObject:delayedSendObject,metadata:claraInstance,title:'Edit Letter',modal:true,iconCls:'icn-mail--pencil'});
	message.show();	
}

function showEmailTemplateLink(obj){
	if(jQuery(obj).length > 0){
		var objValue = jQuery(obj).val();
		var emailTemplateLink = jQuery("#" + objValue + "-emailTemplateLink");
		var allEmailTemplateLink = jQuery("[id$='emailTemplateLink']");
		
		if (objValue != 'APPROVED'){
			if (allEmailTemplateLink.length > 0){
				allEmailTemplateLink.hide();
				emailTemplateLink.show();
			}else{
				emailTemplateLink.show();
			}	
		} else {
			allEmailTemplateLink.hide();
		}
	}
}