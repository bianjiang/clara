Ext.ns('Clara.Forms');

var ignoreFields = ['cbChooseDepartmentNA','fldIncCriteriaText','fldExcCriteriaText','fldCriteriaType','fldCriteriaText','pagefragment', 'search-disease-name','question-external-review-bodies','/protocol/external-review-bodies/body','/continuing-review/external-review-bodies/body','/protocol/study-remotesites/site', '/protocol/study-toxins/toxin', '/protocol/study-subject-populations/population'];


function toggleCheckboxNAValue(naId, name){
	clog('checked? '+jQuery('#'+naId).is(':checked'));
	jQuery('input[name="'+name+'"]').removeAttr('disabled');
	jQuery(":button[value=other]").removeAttr('disabled');
	if(jQuery('#'+naId).is(':checked')){
		jQuery('input[name="'+name+'"]').attr('disabled',true);
		jQuery(":button[value=other]").attr('disabled',true);
	}	
	document.getElementById(naId).disabled=false;
}

function toggleTextAreaNACheckBox(taId, name){
	var naId = "cb_"+taId+"_na";
	clog(naId+' checked? '+jQuery('#'+naId).is(':checked'));
	jQuery('textarea[name="'+name+'"]').removeAttr('disabled');
	jQuery('textarea[name="'+name+'"]').val('');
	if(jQuery('#'+naId).is(':checked')){
		jQuery('textarea[name="'+name+'"]').val('N/A: Not Applicable');
		jQuery('textarea[name="'+name+'"]').attr('disabled',true);
	}
	clog(name+":new val:",jQuery('textarea[name="'+name+'"]').val());
}

function showFormHelpWindow(){
	new Ext.Window({
		title:'Page Help',
		width:400,
		height:400,
		autoScroll:true,
		layout:'fit',
		items:[{xtype:'panel',padding:6,border:false, html:jQuery("#clara-form-help").html()}]
	}).show();
}

function openSubSectionList(){
	// Create array store with subquestions, then create an Ext window that displays that list.
	clog("subsection called.");
	var questionArray = [];
	jQuery(".question").each(function(idx,value){
		var questionId = jQuery(value).attr("id");
		var subSectionTitle = jQuery(value).attr("data-subsection-title");
		var lbl = "";

        jQuery(value).find(".question-label-text").each(function(idx,qlVal){
            lbl +=  jQuery.trim(jQuery(qlVal).text()) + "<br/>";
        });

		var conditional = (jQuery(value).children().find("h1.conditional-question-label").length > 0)?true:false;
		
		if (!jQuery(value).hasClass("form-question-hidden")) questionArray.push([idx, questionId, subSectionTitle, conditional, lbl]);
		
	});
	//clog(questionArray);
	
	// Now make an arrayStore and send it to the subSectionWindow
	var questionArrayStore = new Ext.data.GroupingStore({
		
		data:questionArray,
		reader:new Ext.data.ArrayReader({
			fields:['id','questionId','subSectionTitle','conditional','questionText']
		}),
		remoteSort:true,
		groupField:'subSectionTitle',
		remoteGroup: true
	});
	
	new Clara.Forms.SubSectionWindow({questionStore:questionArrayStore}).show();
	
}

function convertDateFields() {
    var textFields = Ext.query('.extdatefield');
    var dateFields = [];
    Ext.each(textFields, function(textField) {
        var dateField = new Ext.form.DateField({
            allowBlank:false,
            width:200,
            format:'m/d/Y',
            applyTo: textField
        });
        dateFields.push(dateField);
    });
}

function renderFormViewport(pageName, formItem){
	clog("Rendering form viewport..");
	// add class to all required questions
	jQuery(".required").closest(".question").addClass("required-question");
	
	// convert standalone ext fields
	convertDateFields();
	
	var vp = {};
	if (typeof formItem != 'undefined' && formItem != null){
		vp = {xtype:'panel',
				region:'center',
				id:'clara-form-pagecontent-panel',
				layout:'fit',
				border:false,
				autoScroll:true,
				items:[formItem]
			};
	}else{
		vp = {xtype:'panel',
				region:'center',
				id:'clara-form-pagecontent-panel',
				layout:'fit',
				border:false,
				autoScroll:true,
				items:[{xtype:'container',contentEl:'clara-form-pagecontent'}]
			
		};
	}
	if (!Encoder) clog("[WARN] NO Encoder defined.");
	var studyTitle = Encoder.htmlDecode(claraInstance.title);
	// if (shortenedTitle.length > 64) shortenedTitle = shortenedTitle.substring(0,63)+"...";
	var linkUrl = appContext+"/"+claraInstance.type+"s/"+claraInstance.id+"/dashboard";
	
	var formInfoHTML = '<div class="form-protocol-info" style="background: none repeat scroll 0 0 transparent;font-size: 12px;padding: 0px;color:#415877;"><strong style="font-weight:800;"><a href="'+linkUrl+'" target="_blank">'+((claraInstance.type == "protocol")?"IRB":"Contract")+' #'+claraInstance.id+'</a></strong>: '+studyTitle+'</div>'+pageName;
	
	
	new Ext.Viewport({
		layout:'border',
		items:[	{
				    region: 'north',
				    contentEl:'clara-header',
				    bodyStyle:{ backgroundColor:'transparent' },
				    height:48,
				    border: false
				},
				{	xtype:'panel',
					id:'clara-form-viewport-contentarea',
					region:'center',
					border:true,
					layout:'border',
					tbar: new Ext.Toolbar({
						items:[{
								xtype:'panel',
								html:formInfoHTML,
								padding:4,
								unstyled:true,
								bodyStyle:'font-size:15px;font-weight:100;background:transparent;',
								border:false
							   },'->',
							   {xtype:'button',text:"Reviewer notes",hidden:((claraInstance && claraInstance.user.committee !== "PI") || (claraInstance && claraInstance.type === "contract")),iconCls:'icn-sticky-notes-text',handler:function(){
								   var win = new Ext.Window({
									   title:'',
									   width:500,
									   height:500,
									   modal:true,
									   layout:'fit',
									   items:[{
							                id: 'reviewer-contingencygrid-panel',
							                xtype: 'reviewer-contingencygrid-panel',
							                autoLoadComments:true,
							                title:'Notes',
							                onReviewPage: false,
							                reviewAsPI: (claraInstance.user.committee === "PI"),
							                committeeIncludeFilter: [claraInstance.user.committee]
							            }]
								   });
								   win.show();
								   
							   }},{xtype: 'tbspacer', width: 30},
							   {xtype:'button',text:"<span style='font-size:16px;font-weight:600;color:blue;text-decoration:underline;'>I'm done editing this form.</span>",hidden:(claraInstance && claraInstance.form.editing == true),iconCls:'icn-door',handler:function(){
								   location.href = appContext+"/"+claraInstance.type+"s/"+claraInstance.id+"/"+claraInstance.type+"-forms/"+claraInstance.form.id+"/close";
							   }}
				           	]}),
				    bbar: new Ext.Toolbar({
				    	hidden:true,
				    	items:[{xtype:'panel',padding:6,unstyled:true,border:false,contentEl:'btnPrevPage'},'->',{xtype:'panel',padding:6,unstyled:true,border:false,contentEl:'btnNextPage'}]
				    }),
					items:[
					    {xtype:'panel',cls:'clara-form-pagelist-panel-wrapper', contentEl:'clara-form-pagelist', autoScroll:true, region:'east',width:200,bodyCssClass:'clara-form-pagelist-panel', border:false},vp
					]
				}
				

		       ]
	});
	

	
}


function processForm(validator, requiredAnswers, pageProcessor){

	if (typeOf(validator) == 'function'){
		// Run validation function
		validator();
	}
	//if (typeOf(options.requiredAnswers) == 'object' && options.requiredPaths instanceof Array){
		// get answers to required questions on previous pages
	//}
	if (typeOf(pageProcessor) == 'function') {
		pageProcessor(requiredAnswers);
	}

}

function gotoPage(nextPage){
	var baseUrl = "/"+claraInstance.type+"s/" + claraInstance.id + "/"+claraInstance.type+"-forms/" + claraInstance.form.id + "/"+claraInstance.form.urlName+"/"+claraInstance.type+"-form-xml-datas/" + claraInstance.form.xmlDataId + "/";
	
	var nextPagePath = nextPage;
	var nextPageUrl = appContext + baseUrl + nextPage;

	if (usingNoHeaderTemplate){
		nextPageUrl = nextPageUrl + "?noheader=true";
	}
	
	if (usingNoHeaderTemplate && typeof claraInstance.user.committee != 'undefined'){
		nextPageUrl += "&committee="+claraInstance.user.committee;
	}
	
	location.href=nextPageUrl;
}

function submitXMLToNextPage(nextPage, bypassValidation){
	bypassValidation = bypassValidation || false;
	clog("Bypass validation? "+bypassValidation);
	var baseUrl = "/"+claraInstance.type+"s/" + claraInstance.id + "/"+claraInstance.type+"-forms/" + claraInstance.form.id + "/"+claraInstance.form.urlName+"/"+claraInstance.type+"-form-xml-datas/" + claraInstance.form.xmlDataId + "/";

	var nextPagePath = nextPage;
	var nextPageUrl = appContext + baseUrl + nextPage;
	
	if (usingNoHeaderTemplate === true){
		nextPageUrl = nextPageUrl + "?noheader=true";
	}
	
	if (usingNoHeaderTemplate === true && typeof claraInstance.user.committee != 'undefined'){
		nextPageUrl += "&committee="+claraInstance.user.committee;
	}

	
	if (jQuery("#clara-form-wizardpanel-formwrapper").length == 0 && typeof Ext.getCmp("clara-form-wizardpanel") == "undefined"){
		clog("submitXMLToNextPage: bypass check, saving..");
		Ext.MessageBox.show({
			msg: 'Saving..',
			progressText:'Saving...',
			width:300,
			wait:true,
			animEl:'mb7'
		});
		location.href = nextPageUrl;
		return;
	} else {
		clog("submitXMLToNextPage: no bypass. will try rules?");
	}

	
	jQuery("#clara-form-wizardpanel-formwrapper").attr("action",nextPagePath);

	var pageXml = jQuery("#clara-form-wizardpanel-formwrapper").formToXml(ignoreFields);


	jQuery("#pagefragment").val(pageXml);

	var url = appContext + "/ajax/"+claraInstance.type+"s/" + claraInstance.id + "/"+claraInstance.type+"-forms/" + claraInstance.form.id + "/"+claraInstance.type+"-form-xml-datas/" + claraInstance.form.xmlDataId + "/update";
	
	var validFn = (typeof Ext.getCmp("clara-form-wizardpanel") == "undefined")?function(){
		clog("no wizard on this page, validating whole page");
		return (bypassValidation || jQuery("#clara-form-wizardpanel-formwrapper").valid());
	}:function(){
		clog("validating active card on wizard");
		return (bypassValidation || Ext.getCmp("clara-form-wizardpanel").validateActiveCard());
	};
	
	if(validFn()){
		// jQuery("#clara-form-wizardpanel-formwrapper").submit();
		jQuery.ajax({
			type: 'POST',
			url: url,
			async:false,
			dataType: 'xml',
			cache: false,
			data: {pagefragment: pageXml},
			beforeSend: function(){
				Ext.MessageBox.show({
					msg: 'Saving..',
					progressText:'Saving...',
					width:300,
					wait:true,
					animEl:'mb7'
				});
			},
			success: function(data){
				if (nextPage != ''){
					location.href = nextPageUrl;
				} else {
					Ext.MessageBox.hide();
				}
			}
		});
	}
}

function serializeForm(){
	var s = jQuery("#clara-form-wizardpanel-formwrapper").formToXml(ignoreFields);
	alert(s);
}

function updateFormXml(xml,block,successCallback, errorCallback){
		var formtype = claraInstance.type;
		block = (block || false);
		var url = appContext + "/ajax/"+formtype+"s/" + claraInstance.id + "/"+claraInstance.type+"-forms/" + claraInstance.form.id + "/"+formtype+"-form-xml-datas/" + claraInstance.form.xmlDataId + "/update";
		jQuery.ajax({
			type: 'POST',
			url: url,
			async:false,
			data: {pagefragment: "<"+formtype+">"+xml+"</"+formtype+">"},
			beforeSend: function(){
				if (block && Ext) Ext.MessageBox.show({
					msg: 'Saving..',
					progressText:'Saving...',
					width:300,
					wait:true,
					animEl:'mb7'
				});
			},
			success: function(data){
				if (typeof successCallback == "function") successCallback(data);
			},
			error: function(data){
				if (typeof errorCallback == "function") errorCallback(data);
			}
		});
	
}

function updateProtocolXml(xml, block){
	cwarn("[DEPRECETED] updateProtocolXml deprecated. Use updateFormXml.");
	updateFormXml(xml,block,function(){
		if (block && Ext) Ext.MessageBox.hide();
	});
}


function resetForm(formID){
	jQuery(formID)[0].reset();
}


function addCustomValue(name,index,id,wrapperelement){
	newRowHTML = "<div id='custom_"+id+"_"+index+"'><label for='custom_"+id+"_"+index+"'>";
	newRowHTML += "<input name='"+name+"' id='custom_"+id+"_"+index+"' data-attributes=\"'type':'custom'\" class='form-custom-value question-el'/>";
	newRowHTML += "<button id='btnRemove_"+id+"_"+index+"' onclick=\"confirmAndRemoveCustomValue('#custom_"+id+"_"+index+"');return false;\">Remove</button></label> </div>";
	jQuery(wrapperelement).append(newRowHTML);
	if (piwik_enabled()){
		_paq.push(['trackEvent', 'FORM', 'Adding custom value: '+name+' '+id]);
	}
}

function confirmAndRemoveCustomValue(customValueElement){
	customValue = jQuery(customValueElement+" input").val();
	if (customValue != ''){
		var c = confirm("Are you sure you want to remove '"+customValue+"' from this list?");
		if (c) jQuery(customValueElement).remove();
	} else {
		jQuery(customValueElement).remove();
	}
	if (piwik_enabled()){
		_paq.push(['trackEvent', 'FORM', 'Removing custom value: '+customValue+' from '+customValueElement]);
	}
}

function xmlToForm(formSelector, xmlData, root){

	// returns: ID of the xmldata element

	xml_id = jQuery(xmlData).find(root).attr('id');

	var form = jQuery(formSelector);

	// iterate through form elements, fill values with XML data.
	var els = jQuery(formSelector+" :input");

	if (!els) return;

	for(var i=0, max=els.length; i < max; i++) {

		var el = els[i];

		// ASSUME: form element name is an XPATH to the element's value.
		var n = el.name;
		var t = el.type;
		var jqueryName = n.replace(/\//g," ");


		if (!n) continue;
		var v =  jQuery(xmlData).find(jqueryName).each(function(){
			var id_text = jQuery(this).attr('id');
			var value_text = jQuery(this).text();


			// we have the value, no set the input to that value..
			if (t == "text" || t == "hidden" || t == "textarea" || t == "select-one"){
				jQuery(el).val(value_text);
			}
			if (t == "radio"){
				if( jQuery(el).val() == value_text) {
					jQuery(el).attr("checked","checked");
				}
			}
			if (t == "checkbox"){
				if( jQuery(el).val() == value_text) {
					jQuery(el).attr("checked",true);
				}
			}

		});


		//	$.fieldValue(el, true);

	}

	return xml_id;

}



//
// FOR SAVING XML ELEMENTS TO FORMS
//



function addXmlToProtocol(xmlPath, xmlobj){
	//var xml = XMLObjectToString(xmlobj);
	clog("addXmlToProtocol START: ",xmlobj);
	var savedID='';
	var url = appContext + "/ajax/"+claraInstance.type+"s/" + claraInstance.id + "/"+claraInstance.type+"-forms/" + claraInstance.form.id + "/"+claraInstance.type+"-form-xml-datas/" + claraInstance.form.xmlDataId + "/xml-elements/add";
	var recordname = xmlPath.split("/")[xmlPath.split("/").length - 1];
	var data = {
			listPath: xmlPath,
			elementXml: xmlobj
		};
	jQuery.ajax({
		async: false,
		url: url,
		type: "POST",
		dataType: 'xml',
		data: data,
		success: function(data){
			// Save successful, get the new staff's ID
			jQuery(data).find(recordname).each(function(){
				savedID = jQuery(this).attr('id');
			});
			
		}
	});
	
	return savedID;
}

function updateExistingXmlInProtocol(xmlPath, elementID, xml){
	var savedID='';
	var url = appContext + "/ajax/"+claraInstance.type+"s/" + claraInstance.id + "/"+claraInstance.type+"-forms/" + claraInstance.form.id + "/"+claraInstance.type+"-form-xml-datas/" + claraInstance.form.xmlDataId + "/xml-elements/update";
	var recordname = xmlPath.split("/")[xmlPath.split("/").length - 1];
	var data = {
			listPath: xmlPath,
			elementXml: xml,
			elementId: elementID
		};
	jQuery.ajax({
		async: false,
		url: url,
		type: "POST",
		dataType: 'xml',
		data: data,
		success: function(data){
			// Save successful, get the new staff's ID
			jQuery(data).find(recordname).each(function(){
				savedID = jQuery(this).attr('id');
			});
			
		}
	});
	
	return savedID;
}



function addSubElementToElementIdentifiedByXpath(xmlPath, xml){
	var url = appContext + "/ajax/"+claraInstance.type+"s/" + claraInstance.id + "/"+claraInstance.type+"-forms/" + claraInstance.form.id + "/"+claraInstance.type+"-form-xml-datas/" + claraInstance.form.xmlDataId + "/xml-elements/add-by-xpath";
	var data = {
			parentElementXPath: xmlPath,
			elementXml: xml
		};
	jQuery.ajax({
		async: false,
		url: url,
		type: "POST",
		dataType: 'xml',
		data: data,
		success: function(data){
			// Save successful, get the new staff's ID
			clog(data);

		}
	});
}

function removeXmlFromProtocol(path,id){

	if (id != ""){
		var url = appContext + "/ajax/"+claraInstance.type+"s/" + claraInstance.id + "/"+claraInstance.type+"-forms/" + claraInstance.form.id + "/"+claraInstance.type+"-form-xml-datas/" + claraInstance.form.xmlDataId + "/xml-elements/delete";
		data = {	
				listPath: path,
				elementId: id
		};
		
		jQuery.ajax({
			async: false,
			url: url,
			type: "POST",
			dataType: 'xml',
			data: data
		});
		
	}
}

function processOther(obj, otherId, otherType, otherTextInstancePath, otherOptionValue, otherTextValidation){
	
	if(jQuery(obj).length > 0){
		
		if(!otherOptionValue) {
			var otherOptionValue = 'other';
		}
		var objValue = jQuery(obj).val();
		
		
		var otherTextId = otherId + "_text";
		var otherText = jQuery('#' + otherTextId);

		//whether otherText already exist...
		if(otherText.length > 0) {
			
			//checkbox
			if(jQuery(obj).attr("type") == 'checkbox') {
				if(objValue && objValue == otherOptionValue){
					if(jQuery(obj).is(":checked") == true) {
						otherText.show();	
						otherText.addClass(otherTextValidation);
					} else {
						otherText.hide();
						otherText.removeClass(otherTextValidation);
						jQuery(".error").hide();
					}
				}else{
					//the otherOptionValue has not been touched.
					return;
				}				
			}else{ //radio button
				if(objValue && objValue == otherOptionValue){
					otherText.show();
					if(otherTextValidation){
						otherText.addClass(otherTextValidation);
					}
				}else{
					otherText.hide();
					if(otherTextValidation){
						otherText.removeClass(otherTextValidation);
					}
					jQuery(".error").hide();
				}
			}
			
			otherText.focus();
			return;
		}else{
			
			if(objValue == otherOptionValue){
							
				if(jQuery(obj).attr("type") == 'checkbox') {
					
					if(jQuery(obj).is(":checked") == false) {
						return;
					}
				}
				
				//alert(objValue);
				
				if(otherType == 'textarea') {
					var el = jQuery('<textarea></textarea>');	
				}else if(otherType == "text") {
					var el = jQuery('<input type="text"></input>');
				}
				
				el.attr("id", otherTextId);
				el.attr("name", otherTextInstancePath);
				el.addClass("question-el");
				el.addClass("question-el-radio-other");
				
				if(otherTextValidation){
					el.addClass(otherTextValidation);
				}
				
				
				jQuery(obj).parent().parent().append("<br/>").append(el);
				jQuery(el).focus();
			}
		}
		
			
	}
	
}

function processYesNoExplain(obj, textId, textInstancePath, textValidation, valueToShowTextArea){
	
	if(jQuery(obj).length > 0){
		var objValue = jQuery(obj).val();
		
		var textObj = jQuery('#' + textId + '_text');
		
		var textAreaDivId = 'otherTextArea_' + textId + '_div';
		var textAreaDiv = jQuery('#' + textAreaDivId);
		//var newTextAreaTitle = jQuery('div[id*=textarea-title]');
		
		
		
		if(textAreaDiv.length > 0){
			
			if(objValue && objValue == valueToShowTextArea){
				textAreaDiv.show();
				//newText.show();
				if(textValidation){
					textObj.addClass(textValidation);
				}
			}else{
				textAreaDiv.hide();
				//newText.hide();
				if(textValidation){
					textObj.removeClass(textValidation);
				}
			}
			
			textObj.focus();
					
			return;
		}
	}
}