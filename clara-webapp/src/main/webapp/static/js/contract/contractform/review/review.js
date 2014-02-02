var viewport;

var requiredRoles = {
	contingency : ['ROLE_IRB_REVIEWER'],
	copyComments: ['COMMENT_CAN_COPY'],
	moveComments: ['COMMENT_CAN_MOVE'],
	addComments:  ['COMMENT_CAN_ADD']
};

var commentTypeData = [ [ 'NOTE', 'Note' ], [ 'CONTINGENCY', 'Contingency' ],
		[ 'COMMITTEE_PRIVATE_NOTE', 'Committee Private Note' ] ];

var contingencyTypeData = [ [ 'FORMATTING', 'Formatting Issues' ],
		[ 'OTHER', 'Other' ] ];

var rowExpander;

function popupProtocolFormSummary(protocolFormId) {
	var url = appContext + '/'+claraInstance.type+'/' + claraInstance.form.xmlDataId + '/summary';
	window
			.open(url, "Protocol Summary",
					'toolbar=no,status=no,width=1024,height=900,resizable=yes, scrollbars=yes');
}

function answered(checklistId, questionId, answer, rowIndex,
		contingencyTemplate) {

	jQuery
			.ajax({
				type : 'POST',
				async : false,
				url : appContext + "/ajax/"+claraInstance.type+"/"
						+ claraInstance.id
						+ "/"+claraInstance.type+"-forms/"
						+ claraInstance.form.id
						+ "/review/checklists/" + checklistId + "/questions/"
						+ questionId + "/answers/save",
				dataType : 'xml',
				data : {
					answer : answer,
					committee : claraInstance.user.committee,
					formType : claraInstance.form.type
				},
				success : function(response) {
					return true;
				},
				error : function(XMLHttpRequest, textStatus, errorThrown) {
					alert("error: " + textStatus + ": " + errorThrown);
					return;
				}
			});

	return true;
}

function completeReview(fId, cId, username, password, xml) {
	jQuery.ajax({
		url : appContext + "/ajax/"+claraInstance.type+"s/"
				+ claraInstance.id
				+ "/"+claraInstance.type+"-forms/"
				+ claraInstance.form.id
				+ "/review/complete",
		type : "POST",
		async : false,
		data : {
			xmlData : '', // xml,
			committee : cId,
			username : username,
			password : password,
			userId : claraInstance.user.id
		},
		success : function(data) {
			clog(data);
			Ext.getCmp("winCompleteReview").close();
		},
		error : function() {
			alert("Oops something went wrong.");
		}
	});
}

function submitChecklistComment(checklistId, questionId) {
	var checklistPanel = Ext.getCmp('checklist_item_action_form-' + checklistId
			+ '-' + questionId);
	var fldComment = Ext.getCmp('comment_text-' + checklistId + '-'
			+ questionId);
	var fldType = Ext.getCmp('comment_type-' + checklistId + '-' + questionId);
	var fldCType = Ext.getCmp('contingency_type-' + checklistId + '-'
			+ questionId);

	if (jQuery.trim(fldType.getValue()) != ""
			&& jQuery.trim(fldComment.getValue()) != "") {

		if (Clara.Reviewer.submitComment(
				fldType.getValue(), fldCType.getValue(), fldComment.getValue())) {
			clog(data);
			fldComment.reset();
			fldCType.reset();
			fldType.reset();
			// reloadContingencyGridPanel();
		}
		;

	} else {
		alert("Please enter a comment before submitting...");
	}
}



function renderFormReviewViewport() {
	
	// Ext.Ajax.request is not synchronized
	jQuery
			.ajax({
				type : 'GET',
				async : false,
				url : appContext + "/ajax/"+claraInstance.type+"s/"
						+ claraInstance.id
						+ "/"+claraInstance.type+"-forms/"
						+ claraInstance.form.id
						+ "/review/checklists/committee-checklist.xml",
				dataType : 'xml',
				contentType : 'application/xml',
				data : {
					committee : claraInstance.user.committee,
					formType : claraInstance.form.type
				},
				success : function(xmlData) {
					claraInstance.review.checklistXmlDocument = xmlData;
				},
				error : function(XMLHttpRequest, textStatus, errorThrown) {
					alert("error: " + textStatus + ": " + errorThrown);
					return;
				}
			});


	var reviewTabItems = [{id : 'reviewer-contingencygrid-panel',
		xtype : 'reviewer-contingencygrid-panel',
		privateList:true
	},{id : 'reviewer-othercontingencygrid-panel',
	xtype : 'reviewer-contingencygrid-panel',
	privateList:false
}];

	if (claraInstance.user.committee != "PI") {
		reviewTabItems
				.splice(
						0,
						0,
						{
							id : 'checklists',
							xtype : 'panel',
							iconCls:'icn-ui-check-boxes',
							title : 'Checklists',
							layout : 'fit',
							margins : '3 3 3 3',
							cmargins : '3 3 3 3',
							items : [ {
								id : 'reviewer-checklistgroup-panel',
								xtype : 'reviewer-checklistgroup-panel',
								border : false,

								checklistXmlData : claraInstance.review.checklistXmlDocument,
								checklistItemActionFormData : {
									stores : {
										commentTypeStoreData : commentTypeData,
										contingencyTypeStoreData : contingencyTypeData
									}
								}
							} ]
						});
	}

	var iframeHtml = '<iframe style="overflow:auto;width:100%;height:100%;" frameborder="0" id="formSummary" src="'
			+ appContext
			+ '/'+claraInstance.type+'s/'
			+ claraInstance.id
			+ '/'+claraInstance.type+'-forms/'
			+ claraInstance.form.id
			+ '/'
			+ claraInstance.form.urlName + '/summary';
	iframeHtml = iframeHtml + '?noheader=true&committee='
			+ claraInstance.user.committee + '" />';

	var bbarItems = [];

	if ( claraInstance.user.committee != 'IRB_REVIEWER') {
		
		bbarItems.push('->');
		bbarItems.push({
			text : 'Complete Review...',
			id : 'btnCompleteReview',
			height : 38,
			itemCls : 'review-toolbar-label',
			iconCls : 'icn-arrow',
			handler : function() {
				var finalReviewUrl = appContext + "/"+claraInstance.type+"s/"
						+ claraInstance.id
						+ "/"+claraInstance.type+"-forms/"
						+ claraInstance.form.id
						+ "/review/complete?committee="
						+ claraInstance.user.committee;
				clog(finalReviewUrl);
				location.href = finalReviewUrl;
			}
		});

	}

	viewport = new Ext.Viewport({
		// renderTo: 'protocol-form-review',
		layout : 'border',
		items : [ {
			region : 'north',
			contentEl : 'clara-header',
			bodyStyle : {
				backgroundColor : 'transparent'
			},
			height : 48,
			border : false,
			margins : '0 0 0 0'
		}, {

			id : 'left-review-tabpanel',
			xtype : 'tabpanel',
			activeItem : 0,
			split : true,
			region : 'center',
			margins : '3 3 3 3',
			cmargins : '3 3 3 3',
			items : [ {
				id : 'left-review-panel',
				xtype : 'panel',
				title : 'Form Summary',
				iconCls:'icn-application-form',
				border : false,
				html : iframeHtml,
				defaults : {
					layout : 'fit'
				}
			},{
				xtype : 'claradocumentpanel',
				title : 'Documents',
				iconCls:'icn-folder-open-document-text',
				border : false,
				readOnly : false,
				allDocs : true,
				debug : true
			}, {
				id : 'form-status-panel',
				xtype: 'claraformstatusgridpanel',
				title : 'Form Committee Status',			
				iconCls:'icn-users',
				autoload:true, border : false, showActions:false
			} ]
		}, {
			xtype : 'panel',
			bbar : new Ext.Toolbar({
				id : 'review-statusbar',
				height : 42,
				items : bbarItems
			}),
			region : 'east',
			width:'35%',
			border : true,
			split:true,
			collapsible:true,
			layout : 'fit',
			title:'Notes',
			items : [ {
				
				id : 'right-review-panel',
				xtype : 'tabpanel',
				border : false,
				activeItem : 0,
				items : [ reviewTabItems ]

			} ]
		} ]
	});
}
