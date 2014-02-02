Ext.ns('Clara.Reviewer');


Clara.Reviewer.showReplyBoxForCommentId = function(id) {
	jQuery("#review-reply-for-comment-"+id).toggle();
};

Clara.Reviewer.submitReply = function(id) {
	var comment = jQuery("#review-reply-textarea-for-comment-"+id).val();
	Clara.Reviewer.submitComment({
		commentType:'REPLY',
		inLetter:false,
		contingencySeverity:false,
		text:comment,
		replyToId:id
	});
};

Clara.Reviewer.removeComment = function(id){
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
				userId: claraInstance.user.id
			};
		
		if (comment.commentStatus != null) data.commentStatus = comment.commentStatus;
		if (comment.commentType != null) data.commentType = comment.commentType;
		if (comment.text != null) data.text = comment.text;
		if (comment.inLetter != null) data.inLetter = comment.inLetter;
		
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

 
Clara.Reviewer.changeCommentStatus = function(id,status){
	var comment = {id:id};
	if (status != null)
		comment.commentStatus = status;
	
	Clara.Reviewer.modifyComment(comment);
};


Clara.Reviewer.ContingencyGridPanel = Ext.extend(Ext.grid.GridPanel, {
    autoScroll: true,
    border: false,
    agendaItemView:false,
    readOnly:false,
    privateList:false,			// "true" for "My notes", "false" for "Other committee notes", enables different UI
    disableSelection:true,
    bodyCssClass:'gridpanel-contingencies',
	stripeRows: true,
	constructor:function(config){		
		Clara.Reviewer.ContingencyGridPanel.superclass.constructor.call(this, config);
				
		if(Clara.Reviewer.MessageBus){
			Clara.Reviewer.MessageBus.on('contingenciesupdated', this.onContigenciesUpdated, this);
		}
	},
	commentRenderer: function(v,p,r){
		var t= this;
		if (claraInstance.user.committee != r.data.committee && r.data.commentType == "COMMITTEE_PRIVATE_NOTE") return "";		
		
		var today = new Date();
		var modDate = new Date(r.data.modified.format("m/d/Y"));
		var isMajor = (r.get('commentType') == 'CONTINGENCY_MAJOR' || r.get('commentType') == 'NOTE_MAJOR')?true:false;
	
		var html 	= "<div class='review-comment-row review-comment-row-"+r.data.commentType+" review-comment-row-"+r.data.commentType+"-"+r.data.inLetter+"' id='review-comment-row-"+r.data.id+"'>";

		var htmlTitleClass = (this.store.groupField != "committee" && r.data.commentType != "COMMITTEE_PRIVATE_NOTE")?"review-comment-row-committee":"review-comment-row-committee review-comment-row-committee-grouped";
		
		var title = "";
		if (r.get("commentType") == "COMMITTEE_PRIVATE_NOTE") {
			title = r.data.committeeDescription+"<span class='review-comment-light'>added a <span style='color:black;'>committee private note</span>.</span>";
		} else if (this.store.groupField != "committee"){
			title = r.data.committeeDescription+"<span class='review-comment-light'> added a </span>";
		} else {
			title = "<span class='review-comment-light'>Added a</span> ";
		}
		if (r.get("commentType") != "COMMITTEE_PRIVATE_NOTE"){
			
			title += "<span class='review-comment-row-severity severity-"+isMajor+"'>";
			if (r.get('commentType') == 'CONTINGENCY_MAJOR' || r.get('commentType') == 'CONTINGENCY_MINOR') {
				title+=(isMajor)?"Major Contingency":"Minor Contingency";
			} else {
				title+=(isMajor)?"Required Change":"Suggestion/Note";
			}
			title += "</span>";
		}
		var htmlTitle = "<h5 class='"+htmlTitleClass+"'>"+title+"</h5>";
		
		html += htmlTitle;
			
			if (claraInstance.user.committee == 'IRB_OFFICE' && t.privateList){
				html = html + "<div style='float:right; text-align:right;'><a href='javascript:;' onClick='Clara.Reviewer.modifyComment({id:"+r.data.id+", commentType:\'"+((r.get("commentType").indexOf("_MAJOR") != -1)?r.get("commentType").replace("_MAJOR","_MINOR"):r.get("commentType").replace("_MINOR","_MAJOR"))+"\'});'>Change</a></div></div>";
			}
	
		
		html = html + "<p class='review-comment-row-text'> "+r.data.text+"</p>";
		html = html + "<div class='review-comment-row-metadata'>";
		html = html + "<span class='review-comment-row-time-ago'>";
		html = html + r.data.modified.format("m/d/Y g:ia");
		html = html +"</span>";

		if (!this.readOnly) html = html + " - <a href='javascript:void(0)' onclick='Clara.Reviewer.showReplyBoxForCommentId("+r.data.id+");'>Comment</a>";
							
		if (!this.readOnly) {
			html = html + " <span class='review-comment-row-actions'>";
			// PI EDIT MODE
			if (claraInstance.user.committee == 'PI' && r.data.commentType != "COMMITTEE_PRIVATE_NOTE" && t.privateList){
				html = html + " <span class='review-comment-row-actions'>";
				if (r.data.commentStatus == '' || r.data.commentStatus == null ||r.data.commentStatus == "NOT_MET")
					html = html +"- Mark as <a href='javascript:void(0)' onClick='Clara.Reviewer.changeCommentStatus("+r.data.id+",\"DONE\");'>complete</a>";
				else if (r.data.commentStatus == "DONE"){
					html = html +"- <a href='javascript:void(0)' onClick='Clara.Reviewer.changeCommentStatus("+r.data.id+",null);'>Clear status</a>";
				}
			}
			
			// COMMITTEE EDIT MODE
			
			else if (claraInstance.user.committee != null && claraInstance.user.committee != '' && claraInstance.user.committee != 'PI' && r.data.commentType != "COMMITTEE_PRIVATE_NOTE" && t.privateList){
				if (r.data.commentStatus == '' || r.data.commentStatus == null || r.data.commentStatus == 'DONE'){
					html = html + "- Mark as <a href='javascript:void(0)' onClick='Clara.Reviewer.changeCommentStatus("+r.data.id+",\"MET\");'>met</a> or <a href='javascript:void(0)' onClick='Clara.Reviewer.changeCommentStatus("+r.data.id+",\"NOT_MET\");'>not met</a>";
				}else{
					html = html + "- <a href='javascript:void(0)' onClick='Clara.Reviewer.changeCommentStatus("+r.data.id+",null);'>Clear status</a>";
				}

			}

			if (Clara.IsUser(r.data.userId) && t.privateList){
				html = html + " - <a href='javascript:void(0)' onClick='Clara.Reviewer.removeComment("+r.data.id+");'>Delete</a>";
			}
			
			
			// Add copy/move to "other committees" list
			var canMove = claraInstance.HasAnyPermissions(requiredRoles.moveComments);
			var canCopy = claraInstance.HasAnyPermissions(requiredRoles.copyComments);
			if (!t.privateList && canMove){
				html = html + " - <a href='javascript:void(0)' onClick='Clara.Reviewer.moveComment("+r.data.id+");'>Move</a>";
			}
			if (!t.privateList && canMove && canCopy) html += " or ";
			if (!t.privateList && canCopy){
				if (!canMove) html += " - ";
				html = html + "<a href='javascript:void(0)' onClick='Clara.Reviewer.moveComment("+r.data.id+",true);'>Copy</a>";
			}
			if (!t.privateList && (canCopy||canMove)) html += " to my list";
		
			html = html + "</span>";
		}
		html += "</div>";
		
		if (r.data.commentStatus != '' && r.data.commentStatus != null){
			html = html + "<div class='review-comment-status review-comment-status-"+r.data.commentStatus+"'>Marked as <strong>"+r.data.commentStatus.toLowerCase().replace("_"," ")+".</strong></div>";
		}
		
    	
		if (r.data.replies.length > 0) {
			html = html + "<div class='review-comment-replies'>";
	    	for (var i=0; i<r.data.replies.length; i++) {
	    		var idxCls = (i == 0)?" first":"";
	    		html = html + "<div class='review-comment-reply"+idxCls+"'><span class='review-comment-reply-fullname'>"+r.data.replies[i].data.userFullname+"</span><span class='review-comment-reply-text'>"+r.data.replies[i].data.text+"</span>";
	    		html = html + "<div class='review-comment-reply-timeago'>";
	    		
	    		if ((today.getYear() - new Date(r.data.replies[i].data.modified.format("m/d/Y")).getYear()) > 0)
	    			html = html + r.data.replies[i].data.modified.format("m/d/Y, g:ia");
	    		else
	    			html = html + r.data.replies[i].data.modified.format("F d, g:ia");
	    		
	    		html = html +"</div></div>";
	
	    	}
	    	html = html + "</div>";
		}
		
		html = html + "<div id='review-reply-for-comment-"+r.data.id+"' class='review-reply hidden'>";
		
		html = html + "<div id='review-reply-form-for-comment-"+r.data.id+"' class='review-comment-reply review-reply-form'>";
		
		if (!this.readOnly) html = html + "<textarea rows='2' id='review-reply-textarea-for-comment-"+r.data.id+"' class='review-reply-textarea'></textarea><button onclick='Clara.Reviewer.submitReply("+r.data.id+");'>Comment</button></div>";
		
		return html + "</div></div>";

	},
	initComponent: function() {
		var t = this;
		clog("COMMENT PANEL, INIT. readOnly?",t.readOnly);
		if (!t.readOnly)	{	// if readOnly isnt set to true by the server variable on jspx..
			t.readOnly = (t.agendaItemView == true && claraInstance.HasAnyPermissions(requiredRoles.addComments) == true)?false:true;
			t.readOnly = (t.agendaItemView == true && claraInstance.HasAnyPermissions(['VIEW_AGENDA_ONLY']))?true:t.readOnly;
			t.readOnly = (t.agendaItemView == false)?false:t.readOnly;
		}
		clog("t.agendaitemview",t.agendaItemView,t);
		var config = {
				title:(t.title != "")?t.title:((t.privateList)?"My committee":"Other committees"),
				iconCls:t.privateList?"icn-sticky-note":"icn-sticky-notes-stack",
				view: new Ext.grid.GroupingView({
			        forceFit: true,
					headersDisabled: true,
					rowOverCls:'',
					enableGroupingMenu:false,
					selectedRowClass:'review-comment-row-selected',
			        // custom grouping text template to display the number of items per group
					// groupMode: 'value',
			        groupTextTpl: '{text} ({[values.rs.length]} {[values.rs.length > 1 ? "Items" : "Item"]})'
			    }),
				store: new Ext.data.GroupingStore({
					proxy: new Ext.data.HttpProxy({
						url: appContext + "/ajax/"+claraInstance.type+"s/" + claraInstance.id + "/"+claraInstance.type+"-forms/"+claraInstance.form.id+"/review/committee-comments/list",
						method:"GET",
						headers:{'Accept':'application/json;charset=UTF-8'}
					}),
					sortInfo: {
						field:'modified',
						direction: 'DESC'
					},
					baseParams: {
						userId:claraInstance.user.id,
						committee:claraInstance.user.committee
					},
					listeners:{
						load: function(st,recs,opts){
							if (t.privateList) st.filter({fn:function(rec){
								return rec.get("committee") == claraInstance.user.committee;
							}});
							else st.filter({fn:function(rec){
								return rec.get("committee") != claraInstance.user.committee;
							}});
						}
					},
					autoLoad:false,
					groupField: '',
					remoteGroup: true,
					reader: new Ext.data.JsonReader({
						idProperty: 'id',
						fields: [
						         {name:'id', mapping:'id'},
						         {name:'committee', mapping:'committee'},
						         {name:'committeeDescription'},
						         {name:'modified', mapping:'modifiedDate', type: 'date', dateFormat: 'm/d/Y H:i:s'},
						         {name:'userFullname', mapping:'userFullname'},
						         {name:'userId', mapping:'userId'},
						         {name:'text', mapping:'text'},
						         {name:'isPrivate'},
						         {name:'commentType', mapping:'commentType'},
						         {name:'inLetter', mapping:'inLetter'},
						         {name:'commentStatus', mapping:'commentStatus'},
						         {name:'replies',mapping:'children',convert:function(v,node){ 
						        	 var replyReader = new Ext.data.JsonReader({
						        			root: 'children',
						        			fields: [
						        			         {name:'id', mapping:'id'},
						        					 {name:'committee', mapping:'committee', type:'string'},
						        					 {name:'modified', mapping:'modifiedDate', type: 'date', dateFormat: 'm/d/Y H:i:s'},
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
		    		rowdblclick:function(t,ridx,e){
		    			var rec = t.getStore().getAt(ridx);
		    			if (Clara.IsUser(rec.get("userId")) && !t.isReadOnly) new Clara.Reviewer.AddNoteWindow({commentType:rec.get("commentType"),editing:true, record:rec}).show();
		    		},
		    		show: function(g){
		    			clog("Clara.Reviewer.ContingencyGridPanel show()");
		    			g.getStore().load();
		    		}
		    	},
				
				tbar:new Ext.Toolbar({

	    	    	items:[{

	    	    			iconCls : 'icn-flag--plus',
	    	    			text : 'New Contingency',
	    	    			hidden : t.agendaItemView || (t.readOnly || !t.privateList || !(t.privateList && claraInstance.user.committee != "PI" && (claraInstance.HasAnyPermissions(requiredRoles.contingency)))),
	    	    			handler : function() {
	    	    				var winContingency = new Clara.Reviewer.AddNoteWindow({title: 'Add Contingency', commentType:'CONTINGENCY'});
	    	    				winContingency.show();
	    	    			}
	    	    	},{

	    	    		iconCls : 'icn-sticky-note--plus',
	    	    		text : 'New Note', 
	    	    		hidden:t.readOnly || (t.privateList == false),
	    	    		handler : function() {
	    	    			var winNote = new Clara.Reviewer.AddNoteWindow();
	    	    			winNote.show();
	    	    		}
	    	    	},
	    	    	       '->',{

   		           	 	iconCls:'icn-users',
   		           	 	text: 'Group by Committee',
   		           	 	hidden:t.privateList,
   			           	enableToggle: true,
   			            pressed: false,
   			            toggleHandler: function(item, pressed){
   	    	        		if (pressed){
   	    	        			t.store.groupBy("committee");
   	    	        		} else {
   	    	        			t.store.clearGrouping();
   	    	        		}
   		 	    		}	
   		           	 	
   		           	 }]
	    	    }),
				
				columns: [{
				    	id: 'committee',
				    	header:'Committee',
				    	dataIndex: 'committee',
				    	hidden: true,
				    	menuDisabled:true
				    },{
						id:'modified',
						dataIndex:'modified',
						sortable:true,
						width:500,
						renderer:{
				    		fn: this.commentRenderer,
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
