Ext.ns('Clara.Reviewer');

Clara.Reviewer.SelectedComment = {};

Clara.Reviewer.showReplyBoxForCommentId = function(id) {

	
	win = new Ext.Window({
	    title: 'Please enter your comment:',
	    height: 500,
	    width: 600,
	    modal:true,
	    layout: 'fit',
	    items: {  
	        xtype: 'textarea',
	        name: 'fldReplyCommentTextArea',
	        id: 'fldReplyCommentTextArea'
	    },
	    buttons:[{
	    	text:'Cancel',
	    	handler:function(){
	    		win.close();
	    	}
	    },{
	    	text:'Save',
	    	handler:function(){
	    		var comment = Ext.getCmp("fldReplyCommentTextArea").getValue();
	    		
				Clara.Reviewer.submitComment({
					commentType:'REPLY',
					inLetter:false,
					isPrivate:false,
					contingencySeverity:false,
					text:comment,
					replyToId:id
				});
				win.close();
	    	}
	    }]
	});
	
	win.show();
	
	
};

//added for note and contingency exchange redmine 3129
Clara.Reviewer.changeNotesToContingency = function(id,gridPanelId){
	var gp = Ext.getCmp(gridPanelId);
	var rec = gp.getStore().getById(id);
	//rec.data.commentType = "CONTINGENCY";
	var data = {
			committee:claraInstance.user.committee,
			protocolFormId: claraInstance.form.id,
			userId: claraInstance.user.id,
			isPrivate:false
		};
	
	if (rec.data.commentStatus != null) data.commentStatus = rec.data.commentStatus;
	if (rec.data.commentType != null &&rec.data.commentType.indexOf("NOTE") > -1){
		data.commentType = "CONTINGENCY_MINOR";
	}else if(rec.data.commentType != null &&rec.data.commentType.indexOf("CONTINGENCY") > -1){
		data.commentType = "NOTE_MINOR";
	}
	
	jQuery.ajax({
		url: appContext + "/ajax/"+claraInstance.type+"s/" + claraInstance.id + "/"+claraInstance.type+"-forms/"+claraInstance.form.id+"/review/committee-comments/"+rec.data.id+"/type/changetype",
		type: "POST",
		async: false,
		data: data,    								
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



Clara.Reviewer.editComment = function(id, gridPanelId){
    var gp = Ext.getCmp(gridPanelId);
    var rec = gp.getStore().getById(id);
    clog("opening window with rec",rec);
    if (!gp.readOnly) new Clara.Reviewer.AddNoteWindow({commentType:rec.get("commentType"),editing:true, record:rec, isActingAsIRB: gp.isActingAsIRB(), isMyList: gp.isMyList(), reviewAsCommittee:(gp.reviewAsPI?"PI":null)}).show();
};

Clara.Reviewer.removeComment = function(id){
	clog("Clara.Reviewer.removeComment: comment "+id);
	Ext.Msg.show({
		title:"WARNING: About to delete review comment",
		msg:"Are you sure you want to delete this comment?", 
		buttons:Ext.Msg.YESNOCANCEL,
		icon:Ext.MessageBox.WARNING,
		fn: function(btn){
			if (btn == 'yes'){
				var url = appContext + "/ajax/"+claraInstance.type+"s/" + claraInstance.id + "/"+claraInstance.type+"-forms/"+claraInstance.form.id+"/review/committee-comments/"+id+"/remove";
				var data = {
						committee:claraInstance.user.committee,
						protocolFormId: claraInstance.form.id,
						userId: claraInstance.user.id
					};
				jQuery.ajax({
					url: url,
					type: "GET",
					async: false,
					data: data,    								
					success: function(data){
						if(Clara.Reviewer.MessageBus){
							Clara.Reviewer.MessageBus.fireEvent('contingenciesupdated', this);
						}
						return true;
					},
					error: function(){
						alert("There was an error deleting the comment.");
						return false;
					}
				});
			}
		}
		
	});

	
};

Clara.Reviewer.moveComment = function(id, makeCopy){
	makeCopy = makeCopy || false;
	if (id == null){
		cerr("Clara.Reviewer.moveComment: id doesn't exist, cannot move.",comment);
	} else {
		var data = {
				committee:claraInstance.user.committee,
				protocolFormId: claraInstance.form.id,
				userId: claraInstance.user.id,
				makeCopy:makeCopy
			};

		jQuery.ajax({
			url: appContext + "/ajax/"+claraInstance.type+"s/" + claraInstance.id + "/"+claraInstance.type+"-forms/"+claraInstance.form.id+"/review/committee-comments/"+id+"/move",
			type: "POST",
			async: false,
			data: data,    								
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
	}
};


Clara.Reviewer.modifyComment = function(comment){
	if (typeof comment.id == 'undefined' || comment.id == null){
		cerr("Clara.Reviewer.modifyComment: comment.id doesn't exist, cannot update.",comment);
	} else {
		var data = {
				committee:claraInstance.user.committee,
				protocolFormId: claraInstance.form.id,
				userId: claraInstance.user.id,
				isPrivate:false
			};
		
		if (comment.commentStatus != null) data.commentStatus = comment.commentStatus;
		if (comment.commentType != null) data.commentType = comment.commentType;
		if (comment.text != null) data.text = comment.text;
		if (comment.inLetter != null) data.inLetter = comment.inLetter;
		if (comment.isPrivate != null) data.isPrivate = comment.isPrivate;
		data.version = (comment.version != null)?comment.version:false;
		
		jQuery.ajax({
			url: appContext + "/ajax/"+claraInstance.type+"s/" + claraInstance.id + "/"+claraInstance.type+"-forms/"+claraInstance.form.id+"/review/committee-comments/"+comment.id+"/update",
			type: "POST",
			async: false,
			data: data,    								
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
	}
};

 
Clara.Reviewer.changeCommentStatus = function(gridPanelId,id,status){
	clog("Changing status for "+gridPanelId+" ..");
	var store = Ext.getCmp(gridPanelId).getStore();
	clog(store);
	var commentStoreData = store.getById(id).data;
	clog(commentStoreData);

	//if (status != null)
		commentStoreData.commentStatus = status;
	
	Clara.Reviewer.modifyComment(commentStoreData);
};


Clara.Reviewer.CommentRenderer = function(v,p,r,options){
    clog("Clara.Reviewer.CommentRenderer START",v,p,r,options);
    var gpid = options.gpid;
    
    options.meetingView = options.meetingView || false;

    var t= Ext.getCmp(gpid),
        st = r.store;

    var readOnly = (typeof options.readOnly != "undefined")?options.readOnly:(typeof t == "undefined")?false:t.readOnly;
    clog("RENDERING THE COMMENT",r,options,readOnly);
    
    var isGrouped = (typeof(st.groupField) != "undefined" && st.groupField == "committee");
    var today = new Date();
    var isMajor = (r.get('commentType') == 'CONTINGENCY_MAJOR' || r.get('commentType') == 'NOTE_MAJOR')?true:false;
    var html 	= "<div class='review-comment-row review-comment-row-"+r.data.commentType+" review-comment-row-"+r.data.commentType+"-"+r.data.inLetter+(r.data.isPrivate?" private-comment":"")+" wrap' id='review-comment-row-"+r.data.id+"'>";

    var htmlTitleClass = (!isGrouped && !r.get('isPrivate'))?"review-comment-row-committee":"review-comment-row-committee review-comment-row-committee-grouped";

    var title = "";

    // redmine #2831: show name of commenter if PI, coverage, budget manager or budget reviewer OR IF YOU ARE AN IRB COMMITTEE
  
    var displayedCommenterName = r.get("committeeDescription");
	
	if (['PI','COVERAGE_REVIEW','BUDGET_REVIEW','BUDGET_MANAGER'].indexOf(r.get("committee")) > -1
			|| options.meetingView 
			|| claraInstance.HasAnyPermissionsLike('ROLE_IRB') === true
			|| claraInstance.HasAnyPermissions(["ROLE_RESEARCH_COMPLIANCE"],"Show full name of replier.") === true
	){
		displayedCommenterName = r.get("userFullname")+" ("+displayedCommenterName+")";
	}
    
    
    
    
    title = displayedCommenterName+"<span class='review-comment-light'> added a </span>";


    title += "<span class='review-comment-row-severity severity-"+isMajor+"'>";
    if (r.get('commentType') == 'CONTINGENCY_MAJOR' || r.get('commentType') == 'CONTINGENCY_MINOR') {
        title+=(isMajor)?"Major Contingency":"Minor Contingency";
    } else if (r.get('commentType') == 'STUDYWIDE') {
        title += "IRB Studywide note";
    } else {
        title+=(isMajor)?"Required Change":"Note";
    }

    title += "</span>";

    var htmlTitle = "<h5 class='"+htmlTitleClass+"'>"+title+"</h5>";

    html += htmlTitle;

    html += "<p class='review-comment-row-text'> "+r.data.text+"</p>";
    html += "<div class='review-comment-row-metadata'>";

    html += "<span class='review-comment-row-time-ago'>";
    html += r.data.modified.format("m/d/Y g:ia");
    html += "</span>";

    if (r.get('inLetter')){
        html += " - <span style='font-weight:800;color:green;'>Attached to letter</span>";
    }

    if (!readOnly && !options.meetingView) html += "<span class='cpLinkComment'> - <a href='javascript:void(0)' onclick='Clara.Reviewer.showReplyBoxForCommentId("+r.data.id+");'>Comment</a></span>";

    if (!readOnly) {
        html += " <span class='review-comment-row-actions'>";
        // PI EDIT MODE
        if (claraInstance.user.committee == 'PI' && !r.get('isPrivate') && t.isMyList()){
            html += " <span class='review-comment-row-actions'>";
            if (r.data.commentStatus == '' || r.data.commentStatus == null ||r.data.commentStatus == "NOT_MET")
                html += "- Mark as <a href='javascript:void(0)' onClick='Clara.Reviewer.changeCommentStatus(\""+gpid+"\","+r.data.id+",\"DONE\");'>complete</a>";
            else if (r.data.commentStatus == "DONE"){
                html += "- <a href='javascript:void(0)' onClick='Clara.Reviewer.changeCommentStatus(\""+gpid+"\","+r.data.id+",null);'>Clear status</a>";
            }
        }
      


        if (!options.meetingView && typeof(t) != "undefined" && typeof(t.isActingAsIRB) != "undefined" && (Clara.IsUser(r.data.userId) && t.isMyList())
            || claraInstance.HasAnyPermissions("CAN_DELETE_COMMENT","Delete note "+r.data.id)
            || ( 
            		(typeof(t) != "undefined" 
            			&& typeof(t.isActingAsIRB) != "undefined" 
            			&& t.isActingAsIRB()
            			&& (!claraInstance.HasAnyPermissions(['ROLE_IRB_REVIEWER']) || claraInstance.HasAnyPermissions(['ROLE_IRB_OFFICE','ROLE_IRB_EXEMPT_REVIEWER','ROLE_IRB_EXPEDITED_REVIEWER','ROLE_IRB_CONSENT_REVIEWER','ROLE_IRB_CHAIR'])) ) //reviewers that ALSO are IRB should/can edit
            	   && // redmine #2697 (prevent IRB_REVIEWER from deleting other IRB comments)
                (
                    r.get("committee") == "IRB_REVIEWER" ||
                    r.get("committee") == "IRB_CONSENT_REVIEWER" ||
                    r.get("committee") == "IRB_OFFICE" ||
                    r.get("committee") == "IRB_EXEMPT_REVIEWER" ||
                    r.get("committee") == "IRB_PREREVIEW" ||
                    r.get("committee") == "IRB_EXPEDITED_REVIEWER"
                ) && !readOnly
            )){
            html += " - <a href='javascript:;' onClick='Clara.Reviewer.editComment("+r.data.id+", \""+gpid+"\");'>Edit</a> - <a href='javascript:;' onClick='Clara.Reviewer.removeComment("+r.data.id+");'>Delete</a>";
        }

        // added for note and contingency exchange redmine 3129
        if (!options.meetingView && typeof(t) != "undefined" && typeof(t.isActingAsIRB) != "undefined" && Clara.IsUser(r.data.userId) && t.isMyList()){
        	if(claraInstance.HasAnyPermissions(['ROLE_IRB_EXPEDITED_REVIEWER','ROLE_IRB_PREREVIEW'])){
        		
        		
        		if(r.get("commentType") == "NOTE_MINOR"||r.get("commentType") == "NOTE_MAJOR"||r.get("commentType") == "NOTE"){
        			html += " - <a href='javascript:;' onClick='Clara.Reviewer.changeNotesToContingency("+r.data.id+", \""+gpid+"\");'>Change to Contingency</a>";
        		}else if(r.get("commentType") == "CONTINGENCY_MINOR"||r.get("commentType") == "CONTINGENCY_MAJOR"||r.get("commentType") == "CONTINGENCY"){
        			html += " - <a href='javascript:;' onClick='Clara.Reviewer.changeNotesToContingency("+r.data.id+", \""+gpid+"\");'>Change to Note</a> ";
        		}
        	}
        	
        }


        // Add copy/move to "other committees" list
        var canMove = claraInstance.HasAnyPermissions(requiredRoles.moveComments);
        var canCopy = claraInstance.HasAnyPermissions(requiredRoles.copyComments);
        if (!options.meetingView && typeof(t) != "undefined" && !t.readOnly && typeof(t.isActingAsIRB) != "undefined" && !t.isMyList() && canMove){
            html += " - <a href='javascript:void(0)' onClick='Clara.Reviewer.moveComment("+r.data.id+");'>Move</a>";
        }

        if (!options.meetingView && typeof(t) != "undefined" && !t.readOnly && typeof(t.isActingAsIRB) != "undefined" && !t.isMyList() && canCopy &&
            // Redmine #2683
            !(
                t.isActingAsIRB() &&
                    (
                        r.get("committee") == "IRB_REVIEWER" ||
                            r.get("committee") == "IRB_CONSENT_REVIEWER" ||
                            r.get("committee") == "IRB_OFFICE" ||
                            r.get("committee") == "IRB_EXEMPT_REVIEWER" ||
                            r.get("committee") == "IRB_PREREVIEW" ||
                            r.get("committee") == "IRB_EXPEDITED_REVIEWER"
                        )

                )
            ){
            if (!canMove) html += " - ";
            else html += " or ";
            html += "<a href='javascript:void(0)' onClick='Clara.Reviewer.moveComment("+r.data.id+",true);'>Copy</a>";
        }
        if (typeof(t) != "undefined" && !t.readOnly && typeof(t.isActingAsIRB) != "undefined" && !t.isMyList() && (canCopy||canMove)) html += " to my list";

        html += "</span>";
    } // end readonly
    html += "</div>";

    if (r.data.commentStatus != '' && r.data.commentStatus != null){
        html += "<div class='review-comment-status review-comment-status-"+r.data.commentStatus+"'>Marked as <strong>"+r.data.commentStatus.toLowerCase().replace("_"," ")+".</strong></div>";
    }


    if (r.data.replies.length > 0) {
        html = html + "<div class='review-comment-replies'>";
        for (var i=0; i<r.data.replies.length; i++) {
            var idxCls = (i == 0)?" first":"";

            
            var displayedCommenterName = r.data.replies[i].get("committeeDescription");
        	
    		if (['PI','COVERAGE_REVIEW','BUDGET_REVIEW','BUDGET_MANAGER'].indexOf(r.data.replies[i].get("committee")) > -1
    				|| claraInstance.HasAnyPermissionsLike('ROLE_IRB') === true
    				|| claraInstance.HasAnyPermissions(["ROLE_RESEARCH_COMPLIANCE"],"Show full name of replier.")
    		){
    			displayedCommenterName = r.data.replies[i].get("userFullname")+" ("+displayedCommenterName+")";
    		}
            
           

            html += "<div class='review-comment-reply"+idxCls+"'><span class='review-comment-reply-fullname'>"+displayedCommenterName+"</span><span class='review-comment-reply-text'>"+r.data.replies[i].data.text+"</span>";
            html += "<div class='review-comment-reply-timeago'>";



            if ((today.getYear() - new Date(r.data.replies[i].data.modified.format("m/d/Y")).getYear()) > 0)
                html = html + r.data.replies[i].data.modified.format("m/d/Y, g:ia");
            else
                html = html + r.data.replies[i].data.modified.format("F d, g:ia");


            if (!options.meetingView &&
                Clara.IsUser(r.data.replies[i].data.userId)
                || claraInstance.HasAnyPermissions("CAN_DELETE_COMMENT","Delete reply "+r.data.replies[i].data.id)
                || ((typeof(t) != "undefined" && typeof(t.isActingAsIRB) != "undefined" && t.isActingAsIRB() && !claraInstance.HasAnyPermissions(['ROLE_IRB_REVIEWER'])) && // redmine #2697 (prevent IRB_REVIEWER from deleting other IRB comments)
                (
                    r.get("committee") == "IRB_REVIEWER" ||
                        r.get("committee") == "IRB_CONSENT_REVIEWER" ||
                        r.get("committee") == "IRB_OFFICE" ||
                        r.get("committee") == "IRB_EXEMPT_REVIEWER" ||
                        r.get("committee") == "IRB_PREREVIEW" ||
                        r.get("committee") == "IRB_EXPEDITED_REVIEWER"
                    )
                )){
                html += " - <a href='javascript:;' onClick='Clara.Reviewer.removeComment("+r.data.replies[i].data.id+");'>Delete</a>";
            }

            html += "</div></div>";

        }
        html += "</div>";
    }

    html +=  "<div id='review-reply-for-comment-"+r.data.id+"' class='review-reply hidden'>";

    html += "<div id='review-reply-form-for-comment-"+r.data.id+"' class='review-comment-reply review-reply-form'>";

    if (!readOnly && !options.meetingView) {
    	html += "<button class='btncomment' onclick='Clara.Reviewer.submitReply("+r.data.id+");'>Comment</button></div>";
    }

    return html + "</div></div>";

};

Clara.Reviewer.ContingencyGridPanel = Ext.extend(Ext.grid.GridPanel, {
    autoScroll: true,
    trackMouseOver:false,
    border: false,
    agendaItemView:false,
    onReviewPage:false,
    reviewAsPI: false,
    readOnly:false,
    
    ddGroup:'reviewnote-dd',
	ddText:'Reorder this item.',
    viewConfig:{
    	selectedRowClass:''
    },
    autoLoadComments:true,
    committeeIncludeFilter:[],			// array of committees to include
    committeeExcludeFilter:[],			// array of committees to exclude
    disableSelection:false,
    bodyCssClass:'gridpanel-contingencies',
	stripeRows: true,
	constructor:function(config){		
		Clara.Reviewer.ContingencyGridPanel.superclass.constructor.call(this, config);
		if(Clara.Reviewer.MessageBus){
			Clara.Reviewer.MessageBus.on('contingenciesupdated', this.onContigenciesUpdated, this);
		}
	},
	

	
	isMyList: function(){
		if (typeof claraInstance.user.committee == "undefined" || claraInstance.user.committee == null) return false;
		else return (jQuery.inArray(claraInstance.user.committee, this.committeeIncludeFilter) > -1);
	},
	isActingAsPI: function(){
		return (claraInstance.user.committee === "PI");
	},
	isActingAsIRB: function(){
		return !this.isActingAsPI() && (
				(!this.onReviewPage && 
				claraInstance.HasAnyPermissions(['ROLE_IRB_REVIEWER','ROLE_IRB_EXPEDITED_REVIEWER','ROLE_IRB_OFFICE','ROLE_IRB_PREREVIEW'],"c.r.ContingencyGridPanel.isActingAsIRB()"))
				||
				(this.onReviewPage &&
				claraInstance.HasAnyPermissions(['ROLE_IRB_REVIEWER','ROLE_IRB_EXPEDITED_REVIEWER','ROLE_IRB_OFFICE','ROLE_IRB_PREREVIEW'],"c.r.ContingencyGridPanel.isActingAsIRB()") &&
				claraInstance.user.committee.indexOf("IRB_") !== -1)
		);
	},
	formId:null,
	resetUrl: function(){
		var t = this;
		var formMappingName = (claraInstance.type == "protocol")?"@protocolFormId":"@contractFormId";
		t.store.proxy.setApi(Ext.data.Api.actions.read, appContext+"/ajax/"+claraInstance.type+"s/"+claraInstance.id+"/"+claraInstance.type+"-forms/"+t.formId+"/review/committee-comments/list");
	},

	initComponent: function() {
		var t = this;
		
		
		clog("COMMENT PANEL, INIT. readOnly?",t.readOnly);
		if (!t.readOnly)	{	// if readOnly isnt set to true by the server variable on jspx..
			t.readOnly = (t.agendaItemView == true && (claraInstance.HasAnyPermissions(requiredRoles.addComments) == true || claraInstance.HasAnyPermissions(['ROLE_IRB_OFFICE']) == true))?false:true;
			t.readOnly = (t.agendaItemView == true && claraInstance.HasAnyPermissions(['VIEW_AGENDA_ONLY']))?true:t.readOnly;
			t.readOnly = (t.agendaItemView == false)?false:t.readOnly;
		}
		
		var canAddContingency = ( !t.isActingAsPI() && !t.readOnly && t.isMyList() && claraInstance.HasAnyPermissions(['CONTINGENCY_CAN_ADD']) 
				|| ( t.agendaItemView == true && !t.readOnly && t.isMyList() && claraInstance.HasAnyPermissions(['ROLE_IRB_OFFICE','ROLE_IRB_CHAIR','ROLE_IRB_MEETING_OPERATOR']))
		),
		    canAddNote = false;
		
		if (!t.readOnly && t.isMyList() && claraInstance.HasAnyPermissions("COMMENT_CAN_ADD", "canAddNote for t.isMyList() on review")){
			canAddNote = true;
		} else if (!t.readOnly && t.agendaItemView  && claraInstance.HasAnyPermissions("COMMENT_CAN_ADD", "canAddNote for agendaItemView")){
			canAddNote = true;
		} else if (!t.readOnly && t.isMyList() && t.isActingAsPI()){
			canAddNote = true;
		}
		
		var config = {
				plugins: [new Ext.ux.dd.GridDragDropRowOrder(
					    {
					    	dragDropEnabled:(claraInstance.HasAnyPermissions(['CAN_REORDER_COMMENTS']) && t.isMyList()),
					        copy: false, // false by default
					        scrollable: true, // enable scrolling support (default is false)
					        ddGroup:'reviewnote-dd',
					        targetCfg: { 
							    notifyDrop:function(dd,e,data){
					    			var grid = t;
					    			var ds = grid.store;
					    			var sm = grid.getSelectionModel();
					                var rows = sm.getSelections();
					                if(dd.getDragData(e)) {
					                    var cindex=dd.getDragData(e).rowIndex;
					                    if(typeof(cindex) != "undefined") {
					                        for(var i = 0; i <  rows.length; i++) {
					                        ds.remove(ds.getById(rows[i].id));
					                        }
					                        ds.insert(cindex,data.selections);
					                        sm.clearSelections();
					                       
					                     }
					                    grid.getView().refresh(false);
					                    // SORT, THEN UPDATE EPOCH HERE.
										var protocolFormCommitteeCommentIds = [];
										ds.each(function(rec){
											protocolFormCommitteeCommentIds.push(rec.get("id")); 
										});
										
										// AJAX CALL HERE
									
										Ext.Ajax.request({
											method : 'POST',
											url : appContext + "/ajax/protocols/"+claraInstance.id+"/protocol-forms/"+claraInstance.form.id+"/review/committee-comments/set-order",
											params : {
												protocolFormCommitteeCommentIds : protocolFormCommitteeCommentIds
											},
											success : function(response) {
												clog('reorder Review notes: Ext.Ajax success',
														response);
											},
											failure : function(error) {
												cwarn('reorder Review notes: Ext.Ajax failure',
														error);
											}
										});
										
										 sm.selectRecords(rows);  
					                 }
					    		}
					    	} // any properties to apply to the actual DropTarget
					    })],
				title:(t.title != "" && t.title != null)?t.title:(t.isMyList()?"My committee":"Other committees"),
				iconCls:t.isMyList()?"icn-sticky-note":"icn-sticky-notes-stack",
				view: new Ext.grid.GroupingView({
			        forceFit: true,
					headersDisabled: true,
					enableGroupingMenu:false,
			        groupTextTpl: '{text} ({[values.rs.length]} {[values.rs.length > 1 ? "Items" : "Item"]})',
			        getRowClass: function(record,index,rp,st){
						return (record.get("isPrivate"))?"review-comment-row-private":"";
					}
			    }),
				store: new Ext.data.GroupingStore({
					proxy: new Ext.data.HttpProxy({
						url: appContext + "/ajax/"+claraInstance.type+"s/" + claraInstance.id + "/"+claraInstance.type+"-forms/"+claraInstance.form.id+"/review/committee-comments/list",
						method:"GET",
						headers:{'Accept':'application/json;charset=UTF-8'}
					}),
					multiSortInfo:{ 
	                       sorters: [{field: 'displayOrder', direction: "ASC"}
	                                    ,{field: 'id', direction: "DESC"}], 
	                       direction: 'ASC'},

					baseParams: {
						userId:claraInstance.user.id,
						committee:claraInstance.user.committee
					},
					listeners:{
						load: function(st,recs,opts){
							var showRec = false;
							var recInCommitteeArray = false, recIsExcluded=false;
							var myList = t.isMyList();
							
							st.filter({fn:function(rec){
								showRec = (claraInstance.user.committee && claraInstance.user.committee.indexOf("IRB") > -1 && rec.get("commentType") == "STUDYWIDE");
								recInCommitteeArray = (jQuery.inArray(rec.get("committee"), t.committeeIncludeFilter) > -1);
								recIsExcluded = (jQuery.inArray(rec.get("committee"), t.committeeExcludeFilter) > -1);
								if (myList) {
									return showRec || recInCommitteeArray;
								} else {
									return !recIsExcluded;
								}
							}});

						}
					},
					autoLoad:(t.autoLoadComments),
					groupField: '',
					remoteGroup: false,
					reader: new Ext.data.JsonReader({
						idProperty: 'id',
						fields: [
						         {name:'id', mapping:'id'},
						         {name:'displayOrder', mapping:'displayOrder'},
						         {name:'committee', mapping:'committee'},
						         {name:'committeeDescription'},
						         {name:'modified', mapping:'modifiedDate', type: 'date', dateFormat: 'm/d/Y H:i:s'},
						         {name:'userFullname', mapping:'userFullname'},
						         {name:'userId', mapping:'userId'},
						         {name:'text', mapping:'text'},
                            {name:'timestamp', mapping:'@timestamp', type:'timestamp'},//'m/d/Y g:i:s'}
						         {name:'commentType', mapping:'commentType'},
						         {name:'inLetter', mapping:'inLetter'},
						         {name:'isPrivate'},
						         {name:'commentStatus', mapping:'commentStatus', convert:function(v){
						        	 return (v)?v:"No status";
						         }},
						         {name:'replies',mapping:'children',convert:function(v,node){ 
						        	 var replyReader = new Ext.data.JsonReader({
						        			root: 'children',
						        			fields: [
						        			         {name:'id', mapping:'id'},
						        					 {name:'committee', mapping:'committee', type:'string'},
						        					 {name:'committeeDescription'},
                                                     {name:'timestamp', mapping:'@timestamp', type:'timestamp'},//'m/d/Y g:i:s'}
						        					 {name:'modified', mapping:'modifiedDate', type: 'date', dateFormat: 'm/d/Y H:i:s'},
						        					 {name:'userId', mapping:'userId'},
						        					 {name:'userFullname', mapping:'userFullname', type:'string'},
						        					 {name:'text', mapping:'text', type:'string'}
						        			         ]
						        		});
						        	 return replyReader.readRecords(node).records; 
						         }}
						]
					})
				}),

				listeners:{

		    		show:function(g){
		    			if (g.getStore().getCount() == 0) g.getStore().load();
		    		}/*,
		    		render: function(grid){
						var ddrow = new Ext.dd.DropTarget(grid.container, {
							ddGroup : 'reviewnote-dd',
							copy:false,

							notifyDrop : function(dd, e, data){
								
							
								var ds = grid.store;

								var sm = grid.getSelectionModel();
								var rows = sm.getSelections();
								if(dd.getDragData(e)) {
									var cindex=dd.getDragData(e).rowIndex;
									if(typeof(cindex) != "undefined") {
										for(i = 0; i <  rows.length; i++) {
											ds.remove(ds.getById(rows[i].id));
										}
										ds.insert(cindex,data.selections);
										//sm.clearSelections();
									}
									// SORT, THEN UPDATE EPOCH HERE.
									var commentIds = [];
									ds.each(function(rec){
										commentIds.push(rec.get("id")); 
									});
									
									// AJAX CALL HERE
									clog("AJAX, REORDER: ",commentIds);

								}

							}
						});
					} */
		    	},
				
				tbar:new Ext.Toolbar({
		    		scope:this,
	    	    	items:[{
	    	    			iconCls : 'icn-flag--plus',
	    	    			text : 'New Contingency',
	    	    			hidden :  !canAddContingency,
	    	    			handler : function() {
	    	    				var winContingency = new Clara.Reviewer.AddNoteWindow({title: 'Add Contingency', commentType:'CONTINGENCY'});
	    	    				winContingency.show();
	    	    			}
	    	    	},{
	    	    		iconCls : 'icn-sticky-note--plus',
	    	    		text : t.isActingAsIRB()?'New Note / IRB Studywide':'New Note', 
	    	    		hidden:!canAddNote,
	    	    		handler : function() {
	    	    			
	    	    			var options = {};
	    	    			if (t.reviewAsPI) options.reviewAsCommittee = "PI";
	    	    			var winNote = new Clara.Reviewer.AddNoteWindow(options);
	    	    			
	    	    			
	    	    			
	    	    			winNote.show();
	    	    		}
	    	    	},
	    	    	'->',{

   	           	    	//id: 'btnIrbFilterCommittee',
   		           	 	iconCls:'icn-ui-check-box-uncheck',
   		           	 	text: 'Show IRB Only',
   		           	 	hidden:t.isMyList() || claraInstance.HasAnyPermissions('ROLE_IRB_REVIEWER'),
   			           	enableToggle: true,
   			            pressed: t.agendaItemView,
   			            toggleHandler: function(item, pressed){
   			            	var b = this;
   	    	        		if (pressed){
   	    	        			t.store.filter("committee", "IRB_");
   	    	        			b.setIconClass('icn-ui-check-box');
   	    	        		} else {
   	    	        			t.store.clearFilter();
   	    	        			b.setIconClass('icn-ui-check-box-uncheck');
   	    	        		}
   		 	    		}	
   		           	 	
   		           	 },{
	   		           	 	iconCls:'icn-users',
	   		           	 	text: 'Group by Committee',
	   		           	 	hidden:t.isMyList(),
	   			           	enableToggle: true,
	   			            pressed: false,
	   			            toggleHandler: function(item, pressed){
	   	    	        		if (pressed){
	   	    	        			t.store.groupBy("committee");
	   	    	        		} else {
	   	    	        			t.store.clearGrouping();
	   	    	        		}
	   		 	    		}	
	   		           	 	
	   		           	 },{
		   		           	 	iconCls:'icn-users',
		   		           	 	text: 'Group by Status',
		   		           	 	//hidden:t.isMyList(),
		   			           	enableToggle: true,
		   			            pressed: false,
		   			            toggleHandler: function(item, pressed){
		   			            	clog("Groupby status",t.store);
		   			            	
		   	    	        		if (pressed){
		   	    	        			t.store.groupBy("commentStatus");
		   	    	        		} else {
		   	    	        			t.store.clearGrouping();
		   	    	        		}
		   		 	    		}	
		   		           	 	
		   		           	 },'-',{
		   	    	    		scope:this,
		   		           	 	iconCls:'icn-printer',
		   		           	 	
		   			            handler: function(){
		   			            	
		   							Ext.ux.Printer.print(t,{ title:'Notes', keepWindowOpen:true });
		   			            	/*
		   			            	var w = window.open();
		   			                var html = "<html><head><link href='"+appContext+"/static/styles/ext/ext-all.css' type='text/css' rel='stylesheet'/><link href='"+appContext+"/static/styles/application.css'  type='text/css' rel='stylesheet'/><link href='"+appContext+"/static/styles/icons.css' type='text/css' rel='stylesheet'/><link href='"+appContext+"/static/js/ext/ux/SuperBoxSelect/superboxselect.css' type='text/css' rel='stylesheet'/><link href='"+appContext+"/static/styles/protocol/protocolform/review/review.css' type='text/css' rel='stylesheet'/><link href='"+appContext+"/static/styles/protocol/protocol-dashboard.css' type='text/css' rel='stylesheet'/></head><body style='background:transparent;background-color:white;'>";
		   			                html += jQuery(".gridpanel-contingencies .x-grid3-body:first").html();
		   			                
		   			           
		   			             w.document.open();
		   			                html+= "</body></html>";
		   			                w.document.write(html);
		   			             w.document.close();
		   			             w.print();
		   			             w.close();
		   			             */
		   		 	    		}	
		   		           	 	
		   		           	 },'-']
	    	    }),
				
				columns: [{
				    	id: 'committee',
				    	header:'Committee',
				    	dataIndex: 'committee',
				    	hidden: true,
				    	menuDisabled:true
				    },{
				    	id:'commentStatus',
				    	header:'Status',
				    	hidden:true,
				    	dataIndex:'commentStatus'
				    },
				    {
						id:'modified',
						dataIndex:'timestamp',
						sortable:true,
						width:500,
						renderer:{
				    		fn: function(v,p,r) { clog("ABOUT TO RENDER w OPTIONS",{gpid: this.id, meetingView:false, readOnly:(t.readOnly && !canAddContingency)});return Clara.Reviewer.CommentRenderer(v,p,r,{ gpid: this.id, meetingView:false, readOnly:(t.readOnly && !canAddContingency)}); },
				    		scope:this
				    	},
				    	menuDisabled:true
				    	
				}]

		};
		
		// apply config
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		  
		// call parent
		Clara.Reviewer.ContingencyGridPanel.superclass.initComponent.apply(this, arguments);
		

		
	},
	onRender:function(){
		Clara.Reviewer.ContingencyGridPanel.superclass.onRender.apply(this, arguments);
	},
	onContigenciesUpdated: function(source){
		this.store.reload();
	}
});

//register xtype
Ext.reg('reviewer-contingencygrid-panel', Clara.Reviewer.ContingencyGridPanel);
