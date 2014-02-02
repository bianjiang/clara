Ext.ns('Clara.ProtocolForm');

//Basic details
Clara.ProtocolForm.Rules.addRule(new Clara.ProtocolForm.Rule({
	id : Ext.id(),
	questionIds : [ 'question-project-take-place-other-desc' ],
	dependantQuestionPaths : [ '/hsrd/project-take-place' ],
	execute : function(answers) {
		var hide = true;

		if (answers['/hsrd/project-take-place'] == 'other') {
			hide = false;
		}
		this.hide(hide);
	}
}));

//Clara.ProtocolForm.Rules.addRule(new Clara.ProtocolForm.Rule({
//	id : Ext.id(),
//	questionIds : [ 'question-to-generalizable-knowledge' ],
//	dependantQuestionPaths : [ '/hsrd/basic-details/systematic-investigation' ],
//	execute : function(answers) {
//		var hide = true;
//		jQuery('#is-hsrd-qualified').val('n');
//		answers["/hsrd/is-hsrd-qualified"] = "n";
//		
//		
//		
//		if (answers['/hsrd/basic-details/systematic-investigation'] == 'n') {
//					
//			
//			
//			Ext.getCmp('clara-form-wizardpanel').nextTab = 'review';
//			var a = jQuery('<a href="javascript:;">Skip ahead to \'Review\'...</a>');
//			a.click(function(){submitXMLToNextPage( 'review');});			
//			jQuery('#btnNextPage').empty().append(a);
//		} 
//		
//		if (answers['/hsrd/basic-details/systematic-investigation'] == 'y'){
//			hide = false;
//
//			
//			jQuery('#is-hsrd-qualified').val('y');
//			answers["/hsrd/is-hsrd-qualified"] = "y";
//			
//			Ext.getCmp('clara-form-wizardpanel').nextTab = 'subject-data-collection';
//			var a = jQuery('<a href="javascript:;">Skip ahead to \'Data Collection\'...</a>');
//			a.click(function(){submitXMLToNextPage( 'subject-data-collection');});			
//			jQuery('#btnNextPage').empty().append(a);
//		}
//		this.hide(hide);
//	}
//}));

//Clara.ProtocolForm.Rules.addRule(new Clara.ProtocolForm.Rule({
//	id : Ext.id(),
//	questionIds : [ 'question-to-generalizable-knowledge' ],
//	dependantQuestionPaths : [ '/hsrd/basic-details/to-generalizable-knowledge' ],
//	execute : function(answers) {
//		
//		if (answers['/hsrd/basic-details/to-generalizable-knowledge'] == 'n') {
//			
//			jQuery("#tab-subject-data-collection a").addClass("notclickable").removeClass("clickable");
//			jQuery("#tab-subject-data-collection a").attr('onclick', 'javascript:;').click(function(){return false;}).unbind().removeClass("clickable").addClass("notclickable");
//			
//			jQuery('#is-hsrd-qualified').val('n');
//			answers["/hsrd/is-hsrd-qualified"] = "n";
//					
//			Ext.getCmp('clara-form-wizardpanel').nextTab = 'review';
//			var a = jQuery('<a href="javascript:;">Skip ahead to \'Review\'...</a>');
//			a.click(function(){submitXMLToNextPage( 'review');});			
//			jQuery('#btnNextPage').empty().append(a);
//		} 
//		
//		if (answers['/hsrd/basic-details/to-generalizable-knowledge'] == 'y') {
//			
//			jQuery("#tab-subject-data-collection").show();
//			jQuery("#tab-subject-data-collection a").removeClass("notclickable").addClass("clickable").click(function(){submitXMLToNextPage( 'subject-data-collection');});
//			
//			jQuery('#is-hsrd-qualified').val('y');
//			answers["/hsrd/is-hsrd-qualified"] = "y";
//			
//			Ext.getCmp('clara-form-wizardpanel').nextTab = 'subject-data-collection';
//			var a = jQuery('<a href="javascript:;">Skip ahead to \'Data Collection\'...</a>');
//			a.click(function(){submitXMLToNextPage( 'subject-data-collection');});			
//			jQuery('#btnNextPage').empty().append(a);
//		}
//
//	}
//}));

//Subject data collection
//Clara.ProtocolForm.Rules.addRule(new Clara.ProtocolForm.Rule({
//	id : Ext.id(),
//	questionIds : [ 'question-involve-obtaining-information' ],
//	dependantQuestionPaths : [ "/hsrd/subject-data-collection/involve-intervention-or-interaction" ],
//	execute : function(answers) {
//		var hide = true;
//		
//		jQuery('#is-hsrd-qualified').val('y');
//		answers["/hsrd/is-hsrd-qualified"] = "y";
//		
//		if (answers['/hsrd/subject-data-collection/involve-intervention-or-interaction'] == 'y') {
//			
//			Ext.getCmp('clara-form-wizardpanel').nextTab = 'review';
//			var a = jQuery('<a href="javascript:;">Skip ahead to \'Review\'...</a>');
//			a.click(function(){submitXMLToNextPage( 'review');});			
//			jQuery('#btnNextPage').empty().append(a);
//		} 
//		
//		if (answers['/hsrd/subject-data-collection/involve-intervention-or-interaction'] == 'n') {
//			hide = false;
//			
//			jQuery('#is-hsrd-qualified').val('n');
//			answers["/hsrd/is-hsrd-qualified"] = "n";
//			
//			Ext.getCmp('clara-form-wizardpanel').nextTab = 'documents';
//			var a = jQuery('<a href="javascript:;">Skip ahead to \'Documents\'...</a>');
//			a.click(function(){submitXMLToNextPage( 'documents');});			
//			jQuery('#btnNextPage').empty().append(a);
//		}
//		
//		this.hide(hide);
//	}
//}));
//
//Clara.ProtocolForm.Rules.addRule(new Clara.ProtocolForm.Rule({
//	id : Ext.id(),
//	questionIds : [ 'question-readily-ascertained-or-associated' ],
//	dependantQuestionPaths : [ "/hsrd/subject-data-collection/involve-obtaining-information" ],
//	execute : function(answers) {
//
//		var hide = true;
//
//		if (answers['/hsrd/subject-data-collection/involve-obtaining-information'] == 'y') {
//			hide = false;	
//			
//			jQuery('#is-hsrd-qualified').val('y');
//			answers["/hsrd/is-hsrd-qualified"] = "y";
//			
//			Ext.getCmp('clara-form-wizardpanel').nextTab = 'documents';
//			var a = jQuery('<a href="javascript:;">Skip ahead to \'Documents\'...</a>');
//			a.click(function(){submitXMLToNextPage( 'documents');});			
//			jQuery('#btnNextPage').empty().append(a);
//		} 
//		
//		if (answers['/hsrd/subject-data-collection/involve-obtaining-information'] == 'n') {
//			jQuery('#is-hsrd-qualified').val('n');
//			answers["/hsrd/is-hsrd-qualified"] = "n";
//			
//			Ext.getCmp('clara-form-wizardpanel').nextTab = 'review';
//			var a = jQuery('<a href="javascript:;">Skip ahead to \'Review\'...</a>');
//			a.click(function(){submitXMLToNextPage( 'review');});			
//			jQuery('#btnNextPage').empty().append(a);
//		}
//
//		this.hide(hide);
//
//	}
//}));
//
//Clara.ProtocolForm.Rules.addRule(new Clara.ProtocolForm.Rule({
//	id : Ext.id(),
//	questionIds : [ 'question-no-observation-or-recording' ],
//	dependantQuestionPaths : [ "/hsrd/subject-data-collection/readily-ascertained-or-associated" ],
//	execute : function(answers) {
//
//		var hide = true;
//
//		if (answers['/hsrd/subject-data-collection/readily-ascertained-or-associated'] == 'y') {
//			hide = false;
//			
//			jQuery('#is-hsrd-qualified').val('y');
//			answers["/hsrd/is-hsrd-qualified"] = "y";
//			
//			Ext.getCmp('clara-form-wizardpanel').nextTab = 'documents';
//			var a = jQuery('<a href="javascript:;">Skip ahead to \'Documents\'...</a>');
//			a.click(function(){submitXMLToNextPage( 'documents');});			
//			jQuery('#btnNextPage').empty().append(a);
//		} 
//		
//		if (answers['/hsrd/subject-data-collection/readily-ascertained-or-associated'] == 'n') {
//			jQuery('#is-hsrd-qualified').val('n');
//			answers["/hsrd/is-hsrd-qualified"] = "n";
//			
//			Ext.getCmp('clara-form-wizardpanel').nextTab = 'review';
//			var a = jQuery('<a href="javascript:;">Skip ahead to \'Review\'...</a>');
//			a.click(function(){submitXMLToNextPage( 'review');});			
//			jQuery('#btnNextPage').empty().append(a);
//		}
//
//		this.hide(hide);
//
//	}
//}));

//Clara.ProtocolForm.Rules.addRule(new Clara.ProtocolForm.Rule({
//	id : Ext.id(),
//	questionIds : [ 'question-specific-purposes' ],
//	dependantQuestionPaths : [ "/hsrd/subject-data-collection/no-observation-or-recording" ],
//	execute : function(answers) {
//
//		var hide = true;
//
//		if (answers['/hsrd/subject-data-collection/no-observation-or-recording'] == 'n') {
//			hide = false;
//		} 
//
//		this.hide(hide);
//
//	}
//}));

//Clara.ProtocolForm.Rules.addRule(new Clara.ProtocolForm.Rule({
//	id : Ext.id(),
//	questionIds : [ 'question-consist-existingdata-doc-records-specimens' ],
//	dependantQuestionPaths : ["/hsrd/subject-data-collection/specific-purposes" ],
//	execute : function(answers) {
//
//		var hide = true;
//
//		if (answers['/hsrd/subject-data-collection/specific-purposes'] == 'y' || answers['/hsrd/subject-data-collection/no-observation-or-recording'] == 'y'){
//			hide = false;	
//			
//		
//			Ext.getCmp('clara-form-wizardpanel').nextTab = 'documents';
//			var a = jQuery('<a href="javascript:;">Skip ahead to \'Documents\'...</a>');
//			a.click(function(){submitXMLToNextPage( 'documents');});			
//			jQuery('#btnNextPage').empty().append(a);
//		}
//		
//		if (answers['/hsrd/subject-data-collection/specific-purposes'] == 'n'){
//			jQuery('#is-hsrd-qualified').val('n');
//			answers["/hsrd/is-hsrd-qualified"] = "n";
//			
//			Ext.getCmp('clara-form-wizardpanel').nextTab = 'review';
//			var a = jQuery('<a href="javascript:;">Skip ahead to \'Review\'...</a>');
//			a.click(function(){submitXMLToNextPage( 'review');});			
//			jQuery('#btnNextPage').empty().append(a);
//		}
//
//		this.hide(hide);
//
//	}
//}));
//
//Clara.ProtocolForm.Rules.addRule(new Clara.ProtocolForm.Rule({
//	id : Ext.id(),
//	questionIds : [ 'question-publicly-available' ],
//	dependantQuestionPaths : [ "/hsrd/subject-data-collection/consist-existingdata-doc-records-specimens" ],
//	execute : function(answers) {
//
//		var hide = true;
//
//		if (answers['/hsrd/subject-data-collection/consist-existingdata-doc-records-specimens'] == 'y') {
//			hide = false;	
//			
//			jQuery('#is-hsrd-qualified').val('n');
//			answers["/hsrd/is-hsrd-qualified"] = "n";
//			
//			Ext.getCmp('clara-form-wizardpanel').nextTab = 'documents';
//			var a = jQuery('<a href="javascript:;">Skip ahead to \'Documents\'...</a>');
//			a.click(function(){submitXMLToNextPage( 'documents');});			
//			jQuery('#btnNextPage').empty().append(a);
//		} 
//		
//		if (answers['/hsrd/subject-data-collection/consist-existingdata-doc-records-specimens'] == 'n') {
//			jQuery('#is-hsrd-qualified').val('y');
//			answers["/hsrd/is-hsrd-qualified"] = "y";
//			
//			Ext.getCmp('clara-form-wizardpanel').nextTab = 'review';
//			var a = jQuery('<a href="javascript:;">Skip ahead to \'Review\'...</a>');
//			a.click(function(){submitXMLToNextPage( 'review');});			
//			jQuery('#btnNextPage').empty().append(a);
//		}
//
//		this.hide(hide);
//
//	}
//}));
//
//Clara.ProtocolForm.Rules.addRule(new Clara.ProtocolForm.Rule({
//	id : Ext.id(),
//	questionIds : [ 'question-be-recorded-by-investigator' ],
//	dependantQuestionPaths : [ "/hsrd/subject-data-collection/publicly-available" ],
//	execute : function(answers) {
//
//		var hide = true;
//
//		if (answers['/hsrd/subject-data-collection/publicly-available'] == 'n') {
//			hide = false;	
//			
//			jQuery('#is-hsrd-qualified').val('y');
//			answers["/hsrd/is-hsrd-qualified"] = "y";
//			
//			Ext.getCmp('clara-form-wizardpanel').nextTab = 'documents';
//			var a = jQuery('<a href="javascript:;">Skip ahead to \'Documents\'...</a>');
//			a.click(function(){submitXMLToNextPage( 'documents');});			
//			jQuery('#btnNextPage').empty().append(a);
//		} 
//		
//		if (answers['/hsrd/subject-data-collection/publicly-available'] == 'y') {
//			jQuery('#is-hsrd-qualified').val('n');
//			answers["/hsrd/is-hsrd-qualified"] = "n";
//			
//			Ext.getCmp('clara-form-wizardpanel').nextTab = 'review';
//			var a = jQuery('<a href="javascript:;">Skip ahead to \'Review\'...</a>');
//			a.click(function(){submitXMLToNextPage( 'review');});			
//			jQuery('#btnNextPage').empty().append(a);
//		}
//
//		this.hide(hide);
//
//	}
//}));
//
//Clara.ProtocolForm.Rules.addRule(new Clara.ProtocolForm.Rule({
//	id : Ext.id(),
//	questionIds : [ 'question-be-recorded-by-investigator' ],
//	dependantQuestionPaths : [ "/hsrd/subject-data-collection/be-recorded-by-investigator" ],
//	execute : function(answers) {
//
//		if (answers['/hsrd/subject-data-collection/be-recorded-by-investigator'] == 'y') {
//			
//			jQuery('#is-hsrd-qualified').val('n');
//			answers["/hsrd/is-hsrd-qualified"] = "n";
//		} 
//		
//		if (answers['/hsrd/subject-data-collection/be-recorded-by-investigator'] == 'n') {
//			jQuery('#is-hsrd-qualified').val('y');
//			answers["/hsrd/is-hsrd-qualified"] = "y";
//		}
//		
//		Ext.getCmp('clara-form-wizardpanel').nextTab = 'review';
//		var a = jQuery('<a href="javascript:;">Skip ahead to \'Review\'...</a>');
//		a.click(function(){submitXMLToNextPage( 'review');});			
//		jQuery('#btnNextPage').empty().append(a);
//
//	}
//}));
