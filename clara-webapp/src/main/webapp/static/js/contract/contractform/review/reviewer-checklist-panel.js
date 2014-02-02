Ext.ns('Clara.Reviewer');

Clara.Reviewer.submitChecklistComment = function(checklistId){
	
	var commentType = jQuery("#review-comment-type-select-"+checklistId).val();
	var commentSeverity = jQuery("#review-comment-severity-cb-"+checklistId).is(':checked');
	var comment = jQuery("#review-reply-textarea-for-checklist-item-"+checklistId).val();
	
	if(jQuery.trim(commentType) == ""){
		alert("Please choose a comment type");
	} else {
	
		var comType = (commentType != "NOTE" && commentType != "COMMITTEE_PRIVATE_NOTE")?"CONTINGENCY":commentType;
		var conType = (comType == "CONTINGENCY")?commentType:null;
		
		if(jQuery.trim(comment) == ""){
			alert("Please enter a comment");
		} else {
		
			Clara.Reviewer.submitComment(comType,conType,commentSeverity,comment);
			jQuery("#review-comment-type-select-"+checklistId).val("");
			jQuery("#review-comment-severity-cb-"+checklistId).removeAttr("checked");
			jQuery("#review-reply-textarea-for-checklist-item-"+checklistId).val("");
			jQuery("#review-reply-for-checklist-item-"+checklistId).slideUp();
			
		}
	}
		
};


Clara.Reviewer.ChecklistQuestionGridPanel = Ext.extend(Ext.grid.GridPanel, {
	autoScroll: true,
    border: false,
    autoExpandColumn: 'question',
    loadMask: true,
    
    stripeRows: true,
    checklistId: null,
    checklistXmlData: '',
    checklistItemActionFormData: {
		url: '',
		stores: {
			commentTypeStoreData: [],
			contingencyTypeStoreData: []
		}
	},
    constructor:function(config){		
		Clara.Reviewer.ChecklistQuestionGridPanel.superclass.constructor.call(this, config);
	},
	initComponent: function() {
		
		var checklistId = this.checklistId;
		var checklistItemActionFormData = this.checklistItemActionFormData;
		
				
		var checklistQuestionStore = new Ext.data.XmlStore({
			autoLoad: false,
			record: "checklist[@id='" + checklistId + "'] > question",
			fields: [
			    {name:'id', mapping: '@id'},
			    {name:'text', mapping: 'text'},
			    {name:'answer', mapping: 'answer'},
			    {name:'contingencyTemplate', mapping: 'contingency-template'}
			]
		});
		
		checklistQuestionStore.loadData(this.checklistXmlData);
		
		var config = {
				viewConfig:{
		    		emptyText:'No checklists defined for this committee.'
		    	},
				disableSelection:true,
				trackMouseOver:false,
			store: checklistQuestionStore,
			 columns: [
				        new Ext.grid.RowNumberer(),
				        {
				        	id: 'question',
				        	header: 'Question',
				        	resizable:false,
				        	dataIndex: 'text',
				        	renderer : function(v,p,r,rowIndex) {
				        		var html = '<div class="review-checklist-item">';
				        		html = html + '<div class="review-checklist-question" style="white-space:normal !important;">'+r.data.text+'</div>';
				        		html = html + '<div class="review-checklist-answer-form">';
				        		html = html + this.createRadioGroupButton(checklistId, r.data.id, 'YES', 'Yes', r.data.contingencyTemplate, r.data.answer, rowIndex);
				        		html = html + this.createRadioGroupButton(checklistId, r.data.id, 'NO', 'No', r.data.contingencyTemplate, r.data.answer, rowIndex);
				        		html = html + this.createRadioGroupButton(checklistId, r.data.id, 'NOT_CERTAIN', 'Not sure', r.data.contingencyTemplate, r.data.answer, rowIndex);
				        		html = html + this.createRadioGroupButton(checklistId, r.data.id, 'NOT_APPLICABLE', 'N/A', r.data.contingencyTemplate, r.data.answer, rowIndex);
				        		html = html + "<div class='review-checklist-item-actions'><a href='javascript:;' onclick='jQuery(\"#review-reply-for-checklist-item-"+r.data.id+"\").toggle();'>Comment</a></div>";
				        		html = html + '</div><div style="clear:both;"></div>';
				        		
				        		html = html + "<div id='review-reply-for-checklist-item-"+r.data.id+"' class='review-reply hidden'>";
				        		html = html + "<div id='review-reply-form-for-checklist-item-"+r.data.id+"' class='review-comment-reply review-reply-form'>";
				        		html = html + "<textarea rows='2' id='review-reply-textarea-for-checklist-item-"+r.data.id+"' class='review-reply-textarea'></textarea>";
				        		
				        		html = html + "<div class='review-checklist-comment-button'><button onclick='Clara.Reviewer.submitChecklistComment("+r.data.id+");'>Save Comment</button></div>";
				        		html = html + "<div class='review-checklist-comment-select'><select class='review-comment-type-select' id='review-comment-type-select-"+r.data.id+"'><option value=''>Choose a comment type..</option><option value='NOTE'>Public Note</option><option value='COMMITTEE_PRIVATE_NOTE'>Private Note</option></select></div>";
				        		html = html + "<div class='review-checklist-comment-major'><input class='review-comment-severity-cb' id='review-comment-severity-cb-"+r.data.id+"' type='checkbox'/>Major</div>";
				        		html = html + '<div style="clear:both;"></div></div></div>';
				        		
				        		html = html + '</div>';
				        		
				        		return html;//'<div class="review-checklist-question" style="white-space:normal !important;">'+ v +'</div>';
				        	},	        	
				    	    createRadioGroupButton: function(checklistId, questionId, value, description, contingencyTemplate, sValue, rowIndex){
				        		var onclickStr = "answered('" + checklistId + "', '" + questionId + "', jQuery(this).val(), " + rowIndex +  ", '" + contingencyTemplate + "');";
				        		onclickStr = onclickStr + ((value == "NO" || value == "NOT_CERTAIN")?"jQuery('#review-reply-for-checklist-item-"+questionId+"').show();":"jQuery('#review-reply-for-checklist-item-"+questionId+"').hide();");
				    			return '<input type="radio"' + ((value == sValue)?'checked="checked"':'') + ' name="answer-' + checklistId + '-' + questionId + '" id="answer-' + checklistId + '-' + questionId + '-'+value+'" value="' + value + '" onclick="'+onclickStr+'" /><label for="answer-' + checklistId + '-' + questionId + '-'+value+'">' + description + '</label>';
				    		}
				        }
				    ],
				    listeners: {
						render: function(grid) {
							//hide the header
				            grid.getView().el.select('.x-grid3-header').setStyle('display',    'none');
				        },
						cellclick: function(grid, rowIndex, colIndex, e){
							var target = e.getTarget('input');
							if(!target) return;
							var answer = e.getTarget().value;			
						
							var record = grid.getStore().getAt(rowIndex);
							
							var checklistId = grid.checklistId;
							var questionId = record.data.id;

							
							
							//let the reviewer makes contingencies
							// rowExpander.expandRow (rowIndex);
							
							//var questionActionForm = Ext.getCmp('checklist_item_action_form-'+checklistId + '-' + questionId);
							
							var commentType = Ext.getCmp('comment_type-'+checklistId + '-' + questionId);
							
							var commentText = Ext.getCmp('comment_text-'+checklistId + '-' + questionId);
							
							if(answer == 'NO'){								
								commentType.setValue('CONTINGENCY');
								commentText.setValue(record.data.contingencyTemplate);
							}else{
								commentType.setValue('NOTE');
								commentText.setValue('');
							}
							var r = commentType.findRecord(commentType.valueField, commentType.value);
							
							commentType.fireEvent('select', commentType, r, commentType.store.indexOf(r));
							
							//first save the answer
							//Ext.Ajax.request is not synchronized
							jQuery.ajax({
								type: 'POST',
								async: true,
								//url: appContext + "/ajax/businesslogic/checklist/listByCommitteeAndFormType.xml", //template...
								url: appContext + "/ajax/"+claraInstance.type+"s/" + claraInstance.id + "/"+claraInstance.type+"-forms/" + claraInstance.form.id + "/review/checklists/" + checklistId + "/questions/" + questionId + "/answers/save",
								dataType: 'json',
								data: {
									answer: answer,
									committee: claraInstance.user.committee,
									formType: claraInstance.form.type
								},
								success: function(response){
									
								},
								error: function(XMLHttpRequest, textStatus, errorThrown){
									alert("error: " + textStatus + ": " + errorThrown);
									return;
								}
							});
						}
					}
			
		};
		
		// apply config
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		  
		// call parent
		Clara.Reviewer.ChecklistQuestionGridPanel.superclass.initComponent.apply(this, arguments);
	
	}
});

//register xtype
Ext.reg('reviewer-checklistquestiongrid-panel', Clara.Reviewer.ChecklistQuestionGridPanel);

Clara.Reviewer.ChecklistGroupPanel = Ext.extend(Ext.Panel, {
	checklistXmlData: '',
	checklistItemActionFormData: {
		stores: {
			commentTypeStoreData: [],
			contingencyTypeStoreData: []
		}
	},
	constructor:function(config){		
		Clara.Reviewer.ChecklistQuestionGridPanel.superclass.constructor.call(this, config);
	},
	initComponent: function() {
		var checklistGroupStore = new Ext.data.XmlStore({		
			record: 'checklist', 
			autoLoad:false,
			root:'checklists',
			fields: [
			    {name:'id', mapping: '@id'},
			    {name:'title', mapping: '@title'}
			]
		});
		
		var items = [];
		
		checklistGroupStore.loadData(this.checklistXmlData);		
		
		var checklistXmlData = this.checklistXmlData;		
		var checklistItemActionFormData = this.checklistItemActionFormData;
		
		checklistGroupStore.each(function(record){
			thisChecklistGroupPanel = new Ext.Panel({
				id: record.data.id,
				title: "<div class='review-checklist-title'>"+record.data.title+"</div>",
				autoScroll: false,
				border: false,
				layout: 'fit',
				items: [{
					xtype: 'reviewer-checklistquestiongrid-panel',
					checklistId: record.data.id,
				    checklistXmlData: checklistXmlData,
				    checklistItemActionFormData: {
						url: appContext + "/ajax/businesslogic/form/" + claraInstance.form.id + "/committeeComment/save",
						stores: {
							commentTypeStoreData: checklistItemActionFormData.stores.commentTypeStoreData,
							contingencyTypeStoreData: checklistItemActionFormData.stores.contingencyTypeStoreData
						}
					}
				}]
			});
			items.push(thisChecklistGroupPanel);
		});
		
		var config = {
			layout: 'accordion',	    	
	    	items:items	
		};
		
		// apply config
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		  
		// call parent
		Clara.Reviewer.ChecklistGroupPanel.superclass.initComponent.apply(this, arguments);
	
	}
});

Ext.reg('reviewer-checklistgroup-panel', Clara.Reviewer.ChecklistGroupPanel);

