
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
    initComponent: function() {
    	var t =this;
        clog("winAddNote: rec", t.record);
        t.editing = (t.record !== null);
        t.setTitle(t.editing?"Edit Note":"Add Note");

    	var commentCategory =    t.editing?t.record.get("commentType"):
                                    ((t.commentType == "CONTINGENCY" || t.commentType == "CONTINGENCY_MAJOR" || t.commentType == "CONTINGENCY_MINOR")?"CONTINGENCY":"NOTE");

    	var severityLabelHigh =  (commentCategory == "CONTINGENCY")?'<span style="font-weight:800;">Major Contingency</span>':'<span style="font-weight:800;">Required Change:</span> For notes that are serious enough to cause a contingency.';
    	var severityLabelLow  =  (commentCategory == "CONTINGENCY")?'<span style="font-weight:800;">Minor Contingency</span>':'<span style="font-weight:800;">Suggestion / Note:</span> For suggestions to the PI that are not serious.';



    	t.buttons = [
	    			{
	    				text:'Close',
	    				disabled:false,
	    				handler: function(){
	    					t.close();
	    				}
	    			},
	    			{
	    				text:'Save '+((commentCategory == "CONTINGENCY")?'Contingency':'Note'),
	    				id:'btn-save-note',
	    				disabled:false,
	    				handler: function(){
	    					var commentSeverity = Ext.getCmp("fldNoteSeverity").getValue().getGroupValue();
	    					var noteType = (commentCategory == "CONTINGENCY")?"CONTINGENCY":((Ext.getCmp("fldPrivateNote").getValue())?"COMMITTEE_PRIVATE_NOTE":"NOTE");

                            var noteStatus = Ext.getCmp("fldNoteStatus").getValue().getGroupValue();
	    					
	    					if (commentSeverity == "STUDYWIDE") noteType = "STUDYWIDE"
	    					else {
	    						if (noteType == "CONTINGENCY" || noteType == "NOTE") noteType += commentSeverity;
	    					}
	    					

	    					var commentText = jQuery("#fldNote").val();
	    					
	    					var inLetter = (commentCategory == "CONTINGENCY")?true:Ext.getCmp("fldInLetter").getValue();
	    					var comment = {
	    							text:commentText,
	    							commentType:noteType,
	    							inLetter:inLetter,
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
			    fieldLabel: 'What type of '+((commentCategory == "CONTINGENCY")?'contingency':'note')+' is this?',
			    items: [
			        {boxLabel: severityLabelHigh, name: 'note-sev', inputValue:'MAJOR', checked: (t.editing && t.record.data && t.record.data.commentType.indexOf("_MAJOR") != -1)},
					{boxLabel: severityLabelLow, name: 'note-sev', inputValue:'MINOR', checked: (t.editing && t.record.data && t.record.data.commentType.indexOf("_MINOR") != -1)},
					{boxLabel: 'IRB Studywide (visible to IRB only)', name:'note-sev',inputValue:'STUDYWIDE',checked:(t.editing && t.record.data && t.record.data.commentType.indexOf("STUDYWIDE") != -1),
						hidden: !claraInstance.HasAnyPermissions(['ROLE_IRB_REVIEWER','ROLE_IRB_EXPEDITED_REVIEWER','ROLE_IRB_OFFICE','ROLE_IRB_PREREVIEW'])
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
                style: 'width:96%;font-size:15px;',
                value: (t.editing && t.record.data && t.record.data.text)?t.record.data.text:''
            },
            {
                xtype: 'checkbox',
                boxLabel: '<b>Make this note private</b> (visible to '+claraInstance.user.committee+' only)</b>',
                x: 250,
                hidden:claraInstance.HasAnyPermissions(['VIEW_IRB_COMMENTS']),
                y: 10,
                itemId: 'fldPrivateNote',
                name: 'fldPrivateNote',
                id: 'fldPrivateNote',
                checked: (t.editing && t.record.data && t.record.data.commentType == "COMMITTEE_PRIVATE_NOTE")
            },
            {
                xtype: 'checkbox',
                boxLabel: '<b>Attach to letter</b></b>',
                x: 250,
                y: 10,
                itemId: 'fldInLetter',
                name: 'fldInLetter',
                id: 'fldInLetter',
                checked: (t.editing && t.record.data && t.record.data.inLetter)
            },
            {
                xtype: 'radiogroup',
                id:'fldNoteStatus',
                columns:1,
                hidden: !t.editing,
                fieldLabel: 'What is the status of this '+((commentCategory == "CONTINGENCY")?'contingency':'note')+'?',
                items: [
                    {boxLabel: "Met", name: 'note-status', inputValue:'MET', checked: (t.editing && t.record.data && t.record.data.commentStatus.indexOf("MET") == 0)},
                    {boxLabel: "Not met", name: 'note-status', inputValue:'NOT_MET', checked: (t.editing && t.record.data && t.record.data.commentStatus.indexOf("NOT_MET") == 0)}
                ]
            }
        ];
        Clara.Reviewer.AddNoteWindow.superclass.initComponent.call(this);
    }
});
