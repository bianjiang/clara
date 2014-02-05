Ext.define('Clara.Review.view.ReviewNotePanel', {
	extend: 'Ext.grid.GridPanel',
	alias: 'widget.reviewnotepanel',
	requires:[],
	title:'Review Notes (by Committee)',
	border:false,
	hideHeaders:true,
	viewConfig: {
		stripeRows: true,
		trackOver:false,
		emptyText:'No review notes found for this form.'
	},
	loadMask:true,
	store: 'Clara.Review.store.ReviewNotes',
	features: [{
		ftype:'grouping',
		enableGroupingMenu:false,
        groupHeaderTpl: '{[values.rows[0].data.committeeDescription]}'
	}],
	cls:'reviewnotepanel',
	readOnly: true,
	isMyList: function(){
		return true;
	},
	commentActor: "REVIEWER", // can be (REVIEW, REVIEWER_IRB, MEETING_OPERATOR)
	
	
	dockedItems: [{
		dock: 'top',
		border:false,
		xtype: 'toolbar',
		items: ['->',{
			enableToggle: true,
			iconCls:'icn-ui-check-box-uncheck',
			text:'Show IRB Only',
			id:'btnToggleOnlyShowIRBNotes',
			pressed: false	
		},{
			enableToggle: true,
			iconCls:'icn-ui-check-box-uncheck',
			text:'Group by Committee',
			id:'btnToggleGroupNotesByCommittee',
			pressed: false	
		},'-',{
    		xtype:'button',
    		id:'btnPrintCommitteeNotes',
    		tooltip:'Print list (opens new window)',
    		tooltipType:'title',
    		iconCls:'icn-printer'
    	}]
	}],
	
	initComponent: function() { 
		var me = this;
		
		me.listeners = {
			afterrender:function(p){
				p.getStore().loadReviewNotes(Clara.Application.ReviewNoteController.selectedFormId);
			}
		};
		
		me.columns = [{
			dataIndex:'text',
			flex:1,
			renderer: function(v,p,r){
				var store = me.getStore(),
				    isGrouped = (typeof(store.groupField) != "undefined" && store.groupField == "committee"),
				    isPrivate = (r.get("isPrivate"))?true:false;
				    today = new Date(),
				    isMajor = (r.get('commentType') == 'CONTINGENCY_MAJOR' || r.get('commentType') == 'NOTE_MAJOR')?true:false,
				    html = "",
				    htmlTitleClass = (!isGrouped && !r.get('isPrivate'))?"review-comment-row-committee":"review-comment-row-committee review-comment-row-committee-grouped",
				    title = "";
				
				
				
				html = "<div class='review-comment-wrapper wrap "+(isPrivate?" private-comment":"")+"'><div class='review-comment-row review-comment-row-"+r.get("commentType")+" review-comment-row-"+r.get("commentType")+"-"+
					    r.get("inLetter")+"' id='review-comment-row-"+r.get("id")+"'>";
			    
				// redmine #2831: show name of commenter if PI, coverage, budget manager or budget reviewer
			    var displayedCommenterName = ( me.commentActor == "MEETING_OPERATOR" || ['PI','COVERAGE_REVIEW','BUDGET_REVIEW','BUDGET_MANAGER'].indexOf(r.get("committee")) > -1)?(r.get("userFullname")+" ("+r.get("committeeDescription")+")"):r.get("committeeDescription");
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
			    html += "<p class='review-comment-row-text'> "+r.get("text")+"</p>";
			    html += "<div class='review-comment-row-metadata'>";
			    html += "<span class='review-comment-row-time-ago'>";
			    html += moment(r.get("modified")).format("M/DD/YYYY, h:ma");
			    html += "</span>";

			    if (r.get('inLetter')){
			        html += " - <span style='font-weight:800;color:green;'>Attached to letter</span>";
			    }
			    
			    if (!me.readOnly && !(me.commentActor == "MEETING_OPERATOR")) html += "<span class='cpLinkComment'> - <a href='javascript:void(0)' onclick='Clara.Reviewer.showReplyBoxForCommentId("+r.get("id")+");'>Comment</a></span>";
			    
			    
			    
			    if (!me.readOnly) {
			    	html += " <span class='review-comment-row-actions'>";
			    	
			    	if (!me.readOnly && me.isMyList()){
			            html += " - <a href='javascript:;' onClick='Clara.Reviewer.editComment("+r.get("id")+", \""+me.getId()+"\");'>Edit</a>";
			        }
			    	
			        if (!(me.commentActor == "MEETING_OPERATOR") && (Clara.IsUser(r.get("userId")) && me.isMyList())
			                || claraInstance.HasAnyPermissions("CAN_DELETE_COMMENT","Delete note "+r.get("id"))
			                || (((me.commentActor == "REVIEWER_IRB") && !claraInstance.HasAnyPermissions(['ROLE_IRB_REVIEWER'])) && // redmine #2697 (prevent IRB_REVIEWER from deleting other IRB comments)
			                (
			                		r.get("committee") == "IRB_REVIEWER" ||
			                        r.get("committee") == "IRB_CONSENT_REVIEWER" ||
			                        r.get("committee") == "IRB_OFFICE" ||
			                        r.get("committee") == "IRB_EXEMPT_REVIEWER" ||
			                        r.get("committee") == "IRB_PREREVIEW" ||
			                        r.get("committee") == "IRB_EXPEDITED_REVIEWER"
			                    )
			                )){
			                html += " - <a href='javascript:;' onClick='Clara.Reviewer.removeComment("+r.get("id")+");'>Delete</a>";
			            }
			    	
			        // Add copy/move to "other committees" list
			        var canMove = claraInstance.HasAnyPermissions(requiredRoles.moveComments);
			        var canCopy = claraInstance.HasAnyPermissions(requiredRoles.copyComments);
			        if (!(me.commentActor == "MEETING_OPERATOR") && !me.readOnly && !me.isMyList() && canMove){
			            html += " - <a href='javascript:void(0)' onClick='Clara.Reviewer.moveComment("+r.get("id")+");'>Move</a>";
			        }
			        
			        if (!(me.commentActor == "MEETING_OPERATOR") && !me.readOnly && !me.isMyList() && canCopy &&
			                // Redmine #2683
			                !(
			                		(me.commentActor == "REVIEWER_IRB") &&
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
			                html += "<a href='javascript:void(0)' onClick='Clara.Reviewer.moveComment("+r.get("id")+",true);'>Copy</a>";
			            }
			            if (!me.readOnly && !me.isMyList() && (canCopy||canMove)) html += " to my list";
			        
			    	html += "</span>";
			    }
			    
			    html += "</div>";
			    
			    if (r.get("commentStatus") != '' && r.get("commentStatus") != null){
			        html += "<div class='review-comment-status review-comment-status-"+r.get("commentStatus")+"'>Marked as <strong>"+r.get("commentStatus").toLowerCase().replace("_"," ")+".</strong></div>";
			    }
			    
			    
				var replies = r.replies();
			    
			    if (replies.count() > 0) {
			        html = html + "<div class='review-comment-replies'>";
			        
			        replies.each(function(rec, idx){
			        	var idxCls = (idx == 0)?" first":"",
			        		displayedCommenterName = (['PI','COVERAGE_REVIEW','BUDGET_REVIEW'].indexOf(rec.get("committee")) > -1)?(rec.get("userFullname")+" ("+rec.get("committeeDescription")+")"):rec.get("committeeDescription");

			        	html += "<div class='review-comment-reply"+idxCls+"'><span class='review-comment-reply-fullname'>"+displayedCommenterName+"</span><span class='review-comment-reply-text'>"+rec.get("text")+"</span>";
				        html += "<div class='review-comment-reply-timeago'>";
				        

				        	html += moment(rec.get("modified")).format("M/DD/YYYY, h:ma");

			                
				        
				        if (!me.readOnly && !(me.commentActor == "MEETING_OPERATOR") &&
				                Clara.IsUser(rec.get("userId"))
				                || claraInstance.HasAnyPermissions("CAN_DELETE_COMMENT","Delete reply "+rec.get("id"))
				                || (( me.commentActor == "MEETING_OPERATOR" && !claraInstance.HasAnyPermissions(['ROLE_IRB_REVIEWER'])) && // redmine #2697 (prevent IRB_REVIEWER from deleting other IRB comments)
				                (
				                		r.get("committee") == "IRB_REVIEWER" ||
				                        r.get("committee") == "IRB_CONSENT_REVIEWER" ||
				                        r.get("committee") == "IRB_OFFICE" ||
				                        r.get("committee") == "IRB_EXEMPT_REVIEWER" ||
				                        r.get("committee") == "IRB_PREREVIEW" ||
				                        r.get("committee") == "IRB_EXPEDITED_REVIEWER"
				                    )
				                )){
				                html += " - <a href='javascript:;' onClick='Clara.Reviewer.removeComment("+rec.get("id")+");'>Delete</a>";
				            }

				            html += "</div></div>";
			        	
			        });

			        html += "</div>";
			    }
			    
			    html +=  "<div id='review-reply-for-comment-"+r.get("id")+"' class='review-reply hidden'>";
			    html += "<div id='review-reply-form-for-comment-"+r.get("id")+"' class='review-comment-reply review-reply-form'>";

			    if (!me.readOnly && !(me.commentActor == "MEETING_OPERATOR")) {
			    	html += "<button class='btncomment' onclick='Clara.Reviewer.submitReply("+r.get("id")+");'>Comment</button></div>";
			    }

			    return html + "</div></div></div>";
					
			}
		}];

		me.callParent();

	}
});