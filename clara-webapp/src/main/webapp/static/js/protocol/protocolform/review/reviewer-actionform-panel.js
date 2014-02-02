Ext.ns('Clara.Reviewer');

Clara.Reviewer.submitComment = function(comment){ // comType, inLetter,conSeverity, comment, replyToID){
		var url = "";
		clog("submitting comment",comment);

		comment.userId= claraInstance.user.id;
		comment.committee=claraInstance.user.committee;
		if (comment.id){
			// UPDATE
			url = appContext + "/ajax/"+claraInstance.type+"s/" + claraInstance.id + "/"+claraInstance.type+"-forms/"+claraInstance.form.id+"/review/committee-comments/"+comment.id+"/update";
		} else {
			// SAVE
			url = appContext + "/ajax/"+claraInstance.type+"s/" + claraInstance.id + "/"+claraInstance.type+"-forms/"+claraInstance.form.id+"/review/committee-comments/save";
		}
		
		jQuery.ajax({
			url: url,
			type: "POST",
			async: false,
			data: comment,    								
			success: function(data){
				if(Clara.Reviewer.MessageBus){
					Clara.Reviewer.MessageBus.fireEvent('contingenciesupdated', this);
				}
				return true;
			},
			error: function(){
				return false;
			}
		});
};

Clara.Reviewer.AddNoteWindow = Ext.extend(Ext.Window, {
    url: '',
    id:'winAddNote',
    width: 650,
    layout: 'form',
    padding:6,
    resizable: true,
    modal: true,
    commentType:'',
    editing:false,
    record:null,
    isActingAsIRB: false,
    isMyList: false,
    reviewAsCommittee:null,
    initComponent: function() {
    	var t =this,
            noteText="";

        clog("winAddNote: rec", t.record);
        t.editing = (t.record !== null);
        t.setTitle(t.editing?"Edit Note":"Add Note");

        t.reviewAsCommittee = getURLParameter("committee") || null;
        
        if (t.editing){
            var html = t.record.get("text");
            var div = document.createElement("div");
            div.innerHTML = html;
            noteText = div.textContent || div.innerText || "";
        }

        t.commentType = t.editing?t.record.get("commentType"):t.commentType;
        var commentCategory = (t.commentType == "CONTINGENCY" || t.commentType == "CONTINGENCY_MAJOR" || t.commentType == "CONTINGENCY_MINOR")?"CONTINGENCY":"NOTE";
    	var severityLabelHigh =  (commentCategory == "CONTINGENCY")?'<span style="font-weight:800;">Major Contingency</span>':'<span style="font-weight:800;">Required Change</span>';
    	var severityLabelLow  =  (commentCategory == "CONTINGENCY")?'<span style="font-weight:800;">Minor Contingency</span>':'<span style="font-weight:800;">Note</span>';

        var isEditingAsPI = (t.editing && claraInstance.user.committee == 'PI');
        var canEdit = {
            commentType: (t.isActingAsIRB && t.isMyList),
            status: !t.editing ||
                    (
                    claraInstance.HasAnyPermissions(['CAN_EDIT_IRB_COMMENT_STATUS'], "canEdit.status check") &&
                        t.isMyList
                    ) ||
                    (
                        // Allow expedited reviewers to mark (all notes) and (all minor contingencies) made by reviewers.
                        claraInstance.HasAnyPermissions(['ROLE_IRB_EXPEDITED_REVIEWER'],"canEdit.status check EXPEDITED") &&
                            (
                                t.record.get("commentType").indexOf("_MINOR") != -1 ||
                                t.record.get("commentType").indexOf("NOTE_") != -1
                            ) &&
                            (
                                t.record.get("committee") == "IRB_REVIEWER" ||
                                // redmine #2681
                                //r.get("committee") == "IRB_CONSENT_REVIEWER" ||
                                //r.get("committee") == "IRB_OFFICE" ||
                                t.record.get("committee") == "IRB_EXPEDITED_REVIEWER"
                            )

                    )
        };


    	clog("Note Permissions",canEdit);
        clog("isEditingAsPI: "+isEditingAsPI);

    	t.buttons = [
	    			{
	    				text:'Close',
	    				disabled:false,
	    				handler: function(){
	    					Ext.getCmp("winAddNote").close();
	    				}
	    			},
	    			{
	    				text:'Save '+((commentCategory == "CONTINGENCY")?'Contingency':'Note'),
	    				id:'btn-save-note',
	    				disabled:isEditingAsPI,
	    				handler: function(){
	    					var commentSeverity = Ext.getCmp("fldNoteSeverity").getValue().getGroupValue();
	    					var noteType = (commentCategory == "CONTINGENCY")?"CONTINGENCY":"NOTE";
	    					
	    					var isPrivate = Ext.getCmp("fldPrivateNote").getValue() || false;
                            var noteStatus = (Ext.getCmp("fldNoteStatus").getValue())?Ext.getCmp("fldNoteStatus").getValue().getGroupValue():null;
                            noteStatus = (noteStatus == 'CLEAR')?null:noteStatus;
	    					
	    					if (commentSeverity == "STUDYWIDE"){
	    						noteType = "STUDYWIDE";
	    						}
		    					else {
		    						if (noteType == "CONTINGENCY" || noteType == "NOTE") noteType += '_' + commentSeverity;
		    					}
	    					
	    					var commentText = jQuery("#fldNote").val();
	    					
	    					var inLetter = Ext.getCmp("fldInLetter").getValue();
	    					var comment = {
	    							text:commentText,
	    							commentType:noteType,
	    							inLetter:inLetter,
	    							isPrivate:isPrivate,
                                    commentStatus:noteStatus
	    						};
	    					if (jQuery.trim(commentText) != ""){
	    						if (t.editing){
	    							comment.id = t.record.get("id");
	    						}
	    						Clara.Reviewer.submitComment(comment);
	    					}
	    					t.close();
	    				}
	    		}
       	];
    	
        t.items = [
			{
			    xtype: 'radiogroup',
			    id:'fldNoteSeverity',
			    columns:1,
                disabled: t.editing && !canEdit.commentType,
			    fieldLabel: 'What type of '+((commentCategory == "CONTINGENCY")?'contingency':'note')+' is this?',
			    items: [

			        {boxLabel: severityLabelHigh, name: 'note-sev', inputValue:'MAJOR', checked: (t.editing && t.record.data && t.record.data.commentType.indexOf("_MAJOR") != -1)},
					{boxLabel: severityLabelLow, name: 'note-sev', inputValue:'MINOR', checked: ((!t.editing && commentCategory != "CONTINGENCY" && commentCategory != "STUDYWIDE") || (t.editing && t.record.data && t.record.data.commentType.indexOf("_MINOR") != -1))},
					{boxLabel: 'IRB Studywide Note', name:'note-sev',inputValue:'STUDYWIDE',checked:(t.editing && t.record.data && t.record.data.commentType.indexOf("STUDYWIDE") != -1),
						hidden: (!claraInstance.HasAnyPermissions(['ROLE_IRB_REVIEWER','ROLE_IRB_EXPEDITED_REVIEWER','ROLE_IRB_OFFICE','ROLE_IRB_PREREVIEW']) || (commentCategory == "CONTINGENCY"))
					}

			        ]
			},
            {
                xtype: 'textarea',
                fieldLabel:'Comment',
                height: 250,
                itemId: 'fldNote',
                name: 'fldNote',
                id: 'fldNote',
                disabled: isEditingAsPI,
                style: 'width:96%;font-size:15px;',
                value: noteText
            },
            {
                xtype: 'checkbox',
                boxLabel: '<b>Make this note private</b> (visible to your committee only)</b>',
                x: 250,
                hidden:(t.reviewAsCommittee && t.reviewAsCommittee.indexOf("IRB_") > -1) && claraInstance.HasAnyPermissions(['VIEW_IRB_COMMENTS']),
                y: 10,
                itemId: 'fldPrivateNote',
                name: 'fldPrivateNote',
                id: 'fldPrivateNote',
                checked: (t.editing && t.record.data && t.record.data.isPrivate == true) || ((t.reviewAsCommittee && t.reviewAsCommittee.indexOf("IRB_") > -1) && claraInstance.HasAnyPermissions(['VIEW_IRB_COMMENTS']))
            },
            {
                xtype: 'checkbox',
                boxLabel: '<b>Attach to letter</b></b>',
                x: 250,
                y: 10,
                itemId: 'fldInLetter',
                name: 'fldInLetter',
                id: 'fldInLetter',
                hidden: isEditingAsPI,
                checked: (t.editing && t.record.data && t.record.data.inLetter)
            },
            {
                xtype: 'radiogroup',
                id:'fldNoteStatus',
                style:'border-top:1px solid black;',
                columns:3,
                disabled: !canEdit.status,
                hidden: !t.editing || isEditingAsPI,
                items: [
                    {boxLabel: "Mark as <span style='font-weight:800;color:green;'>Met</span>", name: 'note-status', inputValue:'MET', checked: (t.editing && t.record.data && t.record.get("commentStatus") && t.record.get("commentStatus").indexOf("MET") == 0)},
                    {boxLabel: "Mark as <span style='font-weight:800;color:red;'>Not Met</span>", name: 'note-status', inputValue:'NOT_MET', checked: (t.editing && t.record.data && t.record.get("commentStatus") && t.record.get("commentStatus").indexOf("NOT_MET") == 0)},
                    {boxLabel: "None (clear status)", name: 'note-status', inputValue:'CLEAR', checked: false}
                ]
            }
        ];
        Clara.Reviewer.AddNoteWindow.superclass.initComponent.call(this);
    }
});
